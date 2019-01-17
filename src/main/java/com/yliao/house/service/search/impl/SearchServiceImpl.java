package com.yliao.house.service.search.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yliao.house.entity.House;
import com.yliao.house.entity.HouseDetail;
import com.yliao.house.entity.HouseTag;
import com.yliao.house.repository.HouseDetailRepository;
import com.yliao.house.repository.HouseRepository;
import com.yliao.house.repository.HouseTagRepository;
import com.yliao.house.service.search.HouseIndexKey;
import com.yliao.house.service.search.HouseIndexTemplate;
import com.yliao.house.service.search.ISearchService;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;
import org.elasticsearch.rest.RestStatus;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchServiceImpl implements ISearchService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchServiceImpl.class);

    private static final String INDEX_NAME = "xunwu";

    private static final String INDEX_TYPE ="house";

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

    /**
     * 增加索引
     * @param houseId
     * @return
     */
    @Override
    public boolean index(Long houseId) {
        House house = houseRepository.findOne(houseId);
        if (house == null) {
            LOGGER.error("INDEX hosue {} dose not exist!", houseId);
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
            success = deleteAndCreate(totalHits, template);
        }
        if (success) {
            LOGGER.debug("index success ");
        }
        return success;
    }

    /**
     * 创建一个索引
     * @param template
     * @return
     */
    private boolean create(HouseIndexTemplate template) {
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
     * 修改文档
     * @param esId
     * @param template
     * @return
     */
    private boolean update(String esId, HouseIndexTemplate template) {
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

    private boolean deleteAndCreate(long totalHit, HouseIndexTemplate template) {
        DeleteByQueryRequestBuilder builder = DeleteByQueryAction.INSTANCE
                .newRequestBuilder(client)
                .filter(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, template.getHouseId()))
                .source(INDEX_NAME);

        LOGGER.debug("delete by query for hosue:" + builder);

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
        DeleteByQueryRequestBuilder builder = DeleteByQueryAction.INSTANCE
                .newRequestBuilder(client)
                .filter(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, houseId))
                .source(INDEX_NAME);

        LOGGER.debug("delete by query for hosue:" + builder);

        BulkByScrollResponse response = builder.get();

        long deleted = response.getDeleted();
        LOGGER.debug("delete total :" + deleted);
    }
}
