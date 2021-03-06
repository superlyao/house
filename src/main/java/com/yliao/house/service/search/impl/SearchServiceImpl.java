package com.yliao.house.service.search.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;
import com.yliao.house.base.HouseSort;
import com.yliao.house.base.RentValueBlock;
import com.yliao.house.entity.House;
import com.yliao.house.entity.HouseDetail;
import com.yliao.house.entity.HouseTag;
import com.yliao.house.repository.HouseDetailRepository;
import com.yliao.house.repository.HouseRepository;
import com.yliao.house.repository.HouseTagRepository;
import com.yliao.house.service.ServiceMultiResult;
import com.yliao.house.service.ServiceResult;
import com.yliao.house.service.search.*;
import com.yliao.house.web.form.RentSearch;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeAction;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequestBuilder;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SearchServiceImpl implements ISearchService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchServiceImpl.class);

    // 索引名
    private static final String INDEX_NAME = "xunwu";

    // 索引类型
    private static final String INDEX_TYPE ="house";

    // kafka消息队列名称
    private static final String index_topic = "xunwu";

    @Autowired
    private HouseRepository houseRepository;

    @Autowired
    private HouseDetailRepository houseDetailRepository;

    @Autowired
    private HouseTagRepository houseTagRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private TransportClient client;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = index_topic)
    private void handleMessage(String content) {
        LOGGER.info("消费消息:" + content);
        try {
            HouseIndexMessage message = objectMapper.readValue(content, HouseIndexMessage.class);
            switch (message.getOperation()) {
                case HouseIndexMessage.INDEX:
                    this.createOrUpdateIndex(message);
                    break;
                case HouseIndexMessage.REMOVE:
                    this.removeIndex(message);
                    break;
                default:
                    LOGGER.warn("没有找到相关操作:" + content);
                    break;
            }
        } catch (IOException e) {
            LOGGER.error("序列化失败:" + content, e);
        }
    }

    /**
     * 修改或则更新索引
     * @param message
     */
    private void createOrUpdateIndex(HouseIndexMessage message) {
        LOGGER.info(message.toString());
        long houseId = message.getHouseId();
        House house = houseRepository.findOne(houseId);
        if (house == null) {
            LOGGER.error("INDEX house {} dose not exist!", houseId);
            this.index(houseId, message.getRetry() + 1);
            return;
        }
        HouseIndexTemplate template = new HouseIndexTemplate();
        modelMapper.map(house, template);

        HouseDetail houseDetail = houseDetailRepository.findByHouseId(houseId);
        if (houseDetail == null) {
            //todo
        }
        modelMapper.map(houseDetail, template);
        List<HouseTag> houseTags = houseTagRepository.findAllByHouseId(houseId);

        if (houseTags != null && !houseTags.isEmpty()) {
            List<String> tagStrings = new ArrayList<>();
            houseTags.forEach(tag ->{
                tagStrings.add(tag.getName());
            });
            template.setTags(tagStrings);
        }

        SearchRequestBuilder requestBuilder = this.client.prepareSearch(INDEX_NAME).setTypes(INDEX_TYPE)
                .setQuery(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, houseId));
        LOGGER.debug(requestBuilder.toString());
        SearchResponse searchResponse = requestBuilder.get();

        boolean success;
        long totalHits = searchResponse.getHits().getTotalHits();
        if (totalHits == 0) {
            success = create(template);
        } else if (totalHits == 1) {
            String esId = searchResponse.getHits().getAt(0).getId();
            success = update(esId, template);
        } else {
            // 如果存在多个就删除原来的新建一个
            success = deleteAndCreate(totalHits, template);
        }
        if (success) {
            LOGGER.debug("index success ");
        }
    }

    /**
     * 删除索引
     * @param message
     */
    private void removeIndex(HouseIndexMessage message) {
        Long houseId = message.getHouseId();
        DeleteByQueryRequestBuilder builder = DeleteByQueryAction.INSTANCE
                .newRequestBuilder(client)
                .filter(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, houseId))
                .source(INDEX_NAME);

        LOGGER.debug("delete by query for hosue:" + builder);

        BulkByScrollResponse response = builder.get();

        long deleted = response.getDeleted();
        if (deleted <= 0) {
            this.remove(houseId, message.getRetry() + 1);
        }
        LOGGER.debug("delete total :" + deleted);
    }

    /**
     * 暴露给用户的接口
     * 增加索引
     * @param houseId
     * @return
     */
    @Override
    public void index(Long houseId) {
        this.index(houseId, 0);
    }

    private void index(Long houseId, int retry) {
        // 超过最大次数 退出构建索引
        if (retry > HouseIndexMessage.MAX_RETRY) {
            LOGGER.error("创建索引失败已经超过3次,houseID为:" + houseId);
            return;
        }
        HouseIndexMessage message = new HouseIndexMessage(houseId, HouseIndexMessage.INDEX, retry);
        try {
            kafkaTemplate.send(index_topic, objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            LOGGER.error("发送消息时序列化失败" + message);
        }
    }

    /**
     * 创建一个索引
     * @param template
     * @return
     */
    private boolean create(HouseIndexTemplate template) {
        if (!updateSuggest(template)) {
            return false;
        }
        try {
            IndexResponse response = this.client.prepareIndex(INDEX_NAME, INDEX_TYPE)
                    .setSource(objectMapper.writeValueAsBytes(template), XContentType.JSON)
                    .get();
            LOGGER.debug("create index with house: " + template.getHouseId());
            if (response.status() == RestStatus.CREATED) {
                return true;
            } else {
                return false;
            }
        } catch (JsonProcessingException e) {
            LOGGER.error("error to index house" + template.getHouseId(), e);
            return false;
        }
    }

    /**
     * 更新索引
     * @param esId
     * @param template
     * @return
     */
    private boolean update(String esId, HouseIndexTemplate template) {
        if (!updateSuggest(template)) {
            return false;
        }
        try {
            UpdateResponse response = this.client.prepareUpdate(INDEX_NAME, INDEX_TYPE, esId)
                    .setDoc(objectMapper.writeValueAsBytes(template), XContentType.JSON)
                    .get();
            LOGGER.debug("update index with house: " + template.getHouseId());
            if (response.status() == RestStatus.OK) {
                return true;
            } else {
                return false;
            }
        } catch (JsonProcessingException e) {
            LOGGER.error("error to index house" + template.getHouseId(), e);
            return false;
        }
    }

    /**
     * 删除索引和创建索引
     * @param totalHit
     * @param template
     * @return
     */
    private boolean deleteAndCreate(long totalHit, HouseIndexTemplate template) {
        DeleteByQueryRequestBuilder builder = DeleteByQueryAction.INSTANCE
                .newRequestBuilder(client)
                .filter(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, template.getHouseId()))
                .source(INDEX_NAME);

        LOGGER.debug("delete by query for house:" + builder);

        BulkByScrollResponse response = builder.get();

        long deleted = response.getDeleted();
        if (deleted != totalHit) {
            LOGGER.warn("need delete {} , but {} was deleted!", totalHit, deleted);
            return false;
        } else {
            return create(template);
        }
    }

    /**
     * 删除索引
     * @param houseId
     */
    @Override
    public void remove(Long houseId) {
        this.remove(houseId, 0);
    }


    /**
     * 查询索引
     * @param rentSearch
     * @return
     */
    @Override
    public ServiceMultiResult<Long> query(RentSearch rentSearch) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(
                QueryBuilders.termQuery(HouseIndexKey.CITY_EN_NAME, rentSearch.getCityEnName())

        );
        if (rentSearch.getRegionEnName() != null && !"*".equals(rentSearch.getRegionEnName())) {
            boolQueryBuilder.filter(
                    QueryBuilders.termQuery(HouseIndexKey.REGION_EN_NAME, rentSearch.getRegionEnName())
            );
        }

        RentValueBlock area = RentValueBlock.matchArea(rentSearch.getAreaBlock());
        if (!RentValueBlock.ALL.equals(area)) {
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(HouseIndexKey.AREA);
            if (area.getMax() > 0) {
                rangeQueryBuilder.lte(area.getMax());
            }
            if (area.getMin() > 0) {
                rangeQueryBuilder.gte(area.getMin());
            }
            boolQueryBuilder.filter(rangeQueryBuilder);
         }

        RentValueBlock price = RentValueBlock.matchPrich(rentSearch.getPriceBlock());
        if (!RentValueBlock.ALL.equals(price)) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery(HouseIndexKey.PRICE);
            if (price.getMax() > 0) {
                rangeQuery.lte(price.getMax());
            }
            if (price.getMin() > 0) {
                rangeQuery.gte(price.getMin());
            }
            boolQueryBuilder.filter(rangeQuery);
        }

        if (rentSearch.getDirection() > 0) {
            boolQueryBuilder.filter(QueryBuilders.termQuery(HouseIndexKey.DIRECTION, rentSearch.getDirection()));
        }

        if (rentSearch.getRentWay() > -1) {
            boolQueryBuilder.filter(QueryBuilders.termQuery(HouseIndexKey.RENT_WAY, rentSearch.getRentWay()));
        }


        boolQueryBuilder.must(
                QueryBuilders.multiMatchQuery(
                        rentSearch.getKeywords(),
                        HouseIndexKey.TITLE,
                        HouseIndexKey.TRAFFIC,
                        HouseIndexKey.ROUND_SERVICE,
                        HouseIndexKey.SUBWAY_LINE_NAME,
                        HouseIndexKey.SUBWAY_STATION_NAME
                        ));

        SearchRequestBuilder builder = this.client.prepareSearch(INDEX_NAME)
                .setTypes(INDEX_TYPE)
                .setQuery(boolQueryBuilder)

                .addSort(
                        HouseSort.getSortKey(rentSearch.getOrderBy()),
                        SortOrder.fromString(rentSearch.getOrderDirection()))
                .setFrom(rentSearch.getStart())
                .setSize(rentSearch.getSize());
        LOGGER.debug(builder.toString());
        List<Long> houseIds = new ArrayList<>();
        SearchResponse response = builder.get();
        if (response.status() != RestStatus.OK) {
            LOGGER.warn("没有在es中查询到相关数据的索引");
            return new ServiceMultiResult<>(0, houseIds);
        }
        for (SearchHit hit : response.getHits()) {
            houseIds.add(
                    Longs.tryParse(String.valueOf(hit.getSource().get(HouseIndexKey.HOUSE_ID)))
            );
        }
        return new ServiceMultiResult<>(response.getHits().getTotalHits(), houseIds);
    }

    @Override
    public ServiceResult<List<String>> suggest(String prefix) {
        CompletionSuggestionBuilder suggestion = SuggestBuilders.completionSuggestion("suggests").prefix(prefix).size(5);

        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion("autocomplete", suggestion);

        SearchRequestBuilder requestBuilder = this.client.prepareSearch(INDEX_NAME)
                .setTypes(INDEX_TYPE)
                .suggest(suggestBuilder);
        LOGGER.debug(requestBuilder.toString());

        SearchResponse response = requestBuilder.get();
        Suggest suggest = response.getSuggest();
        if (suggest == null) {
            return ServiceResult.of(new ArrayList<>());
        }
        Suggest.Suggestion result = suggest.getSuggestion("autocomplete");

        int maxSuggest = 0;
        Set<String> suggestSet = new HashSet<>();

        for (Object term : result.getEntries()) {
            if (term instanceof CompletionSuggestion.Entry) {
                CompletionSuggestion.Entry item = (CompletionSuggestion.Entry) term;

                if (item.getOptions().isEmpty()) {
                    continue;
                }

                for (CompletionSuggestion.Entry.Option option : item.getOptions()) {
                    String tip = option.getText().string();
                    if (suggestSet.contains(tip)) {
                        continue;
                    }
                    suggestSet.add(tip);
                    maxSuggest++;
                }
            }

            if (maxSuggest > 5) {
                break;
            }
        }
        List<String> suggests = Lists.newArrayList(suggestSet.toArray(new String[]{}));
        return ServiceResult.of(suggests);
    }

    private boolean updateSuggest(HouseIndexTemplate indexTemplate) {
        AnalyzeRequestBuilder requestBuilder = new AnalyzeRequestBuilder(
                this.client, AnalyzeAction.INSTANCE, INDEX_NAME, indexTemplate.getTitle(),
                indexTemplate.getLayoutDesc(), indexTemplate.getRoundService(),
                indexTemplate.getDescription(), indexTemplate.getSubwayLineName(),
                indexTemplate.getSubwayStationName());

        requestBuilder.setAnalyzer("ik_smart");

        AnalyzeResponse response = requestBuilder.get();
        List<AnalyzeResponse.AnalyzeToken> tokens = response.getTokens();
        if (tokens == null) {
            LOGGER.warn("Can not analyze token for house: " + indexTemplate.getHouseId());
            return false;
        }

        List<HouseSuggest> suggests = new ArrayList<>();
        for (AnalyzeResponse.AnalyzeToken token : tokens) {
            // 排序数字类型 & 小于2个字符的分词结果
            if ("<NUM>".equals(token.getType()) || token.getTerm().length() < 2) {
                continue;
            }

            HouseSuggest suggest = new HouseSuggest();
            suggest.setInput(token.getTerm());
            suggests.add(suggest);
        }

        // 定制化小区自动补全
        HouseSuggest suggest = new HouseSuggest();
        suggest.setInput(indexTemplate.getDistrict());
        suggests.add(suggest);

        indexTemplate.setSuggests(suggests);
        return true;
    }

    private void remove(Long houseId, int retry) {
        if (retry > HouseIndexMessage.MAX_RETRY) {
            LOGGER.error("删除索引次数超过3次,houseId为：" + houseId);
            return;
        }
        HouseIndexMessage message = new HouseIndexMessage(houseId, HouseIndexMessage.REMOVE, retry);
        try {
            this.kafkaTemplate.send(index_topic, objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            LOGGER.error("删除时序列化数据失败" + message, e);
        }
    }
}
