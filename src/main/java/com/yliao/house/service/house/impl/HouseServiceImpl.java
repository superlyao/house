package com.yliao.house.service.house.impl;

import com.google.common.collect.Maps;
import com.yliao.house.base.HouseSort;
import com.yliao.house.base.HouseStatus;
import com.yliao.house.base.LoginUserUtil;
import com.yliao.house.entity.*;
import com.yliao.house.repository.*;
import com.yliao.house.service.ServiceMultiResult;
import com.yliao.house.service.ServiceResult;
import com.yliao.house.service.house.IHouseService;
import com.yliao.house.service.house.IQiNiuService;
import com.yliao.house.service.search.ISearchService;
import com.yliao.house.web.dto.HouseDTO;
import com.yliao.house.web.dto.HouseDetailDTO;
import com.yliao.house.web.dto.HousePictureDTO;
import com.yliao.house.web.form.DataTableSearch;
import com.yliao.house.web.form.HouseForm;
import com.yliao.house.web.form.PhotoForm;
import com.yliao.house.web.form.RentSearch;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.*;

@Service
@Transactional
public class HouseServiceImpl implements IHouseService {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private HouseRepository houseRepository;

    @Autowired
    private SubwayRepository subwayRepository;

    @Autowired
    private SubwayStationRepository subwayStationRepository;

    @Autowired
    private HouseDetailRepository houseDetailRepository;

    @Autowired
    private HousePictureRepository housePictureRepository;


    @Autowired
    private HouseTagRepository houseTagRepository;

    @Autowired
    private ISearchService searchService;

    @Value("${qiniu.cdn.prefix}")
    private String prefix;

    @Transactional
    @Override
    public ServiceResult<HouseDTO> save(HouseForm houseForm) {
        // 房屋详细信息实体
        HouseDetail detail = new HouseDetail();
        ServiceResult<HouseDTO> subwayValidtionResult = warpperSubwayInfo(detail, houseForm);

        if (subwayValidtionResult != null) {
            return subwayValidtionResult;
        }

        House house = new House();
        modelMapper.map(houseForm, house);
        Date now = new Date();
        house.setCreateTime(now);
        house.setLastUpdateTime(now);
        house.setAdminId(LoginUserUtil.getUserId());
        // 存储房源
        house = houseRepository.save(house);

        detail.setHouseId(house.getId());
        // 存储房源详细信息
        detail = houseDetailRepository.save(detail);

        // 获取从页面传过来的图片信息
        List<HousePicture> pictures = generatePictures(houseForm, house.getId());

        // 存储图片
        Iterable<HousePicture> housePictures = housePictureRepository.save(pictures);

        HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);
        HouseDetailDTO houseDetailDTO = modelMapper.map(detail, HouseDetailDTO.class);
        houseDTO.setHouseDetail(houseDetailDTO);

        List<HousePictureDTO> pictureDTOS = new ArrayList<>();
        housePictures.forEach(housePicture -> pictureDTOS.add(modelMapper.map(housePicture, HousePictureDTO.class)));

        houseDTO.setPictures(pictureDTOS);
        houseDTO.setCover(this.prefix + houseDTO.getCover());

        List<String> tags = houseForm.getTags();
        if (tags !=null && !tags.isEmpty()) {
            List<HouseTag> houseTags = new ArrayList<>();
            for (String tag : tags) {
                houseTags.add(new HouseTag(house.getId(), tag));
            }
            houseTagRepository.save(houseTags);
            houseDTO.setTags(tags);
        }
        return new ServiceResult<>(true, null, houseDTO);
    }

    @Override
    public ServiceResult update(HouseForm houseForm) {
        House house = this.houseRepository.findOne(houseForm.getId());
        if (house == null) {
            return ServiceResult.notFound();
        }
        HouseDetail detail = this.houseDetailRepository.findByHouseId(houseForm.getId());
        ServiceResult wrapperRrsult = warpperSubwayInfo(detail, houseForm);
        if (wrapperRrsult != null) {
            return wrapperRrsult;
        }
        houseDetailRepository.save(detail);
        List<HousePicture> pictures = generatePictures(houseForm, houseForm.getId());
        housePictureRepository.save(pictures);

        if (houseForm.getCover() == null) {
            houseForm.setCover(house.getCover());
        }
        modelMapper.map(houseForm, house);
        house.setLastUpdateTime(new Date());
        houseRepository.save(house);
        // 如果是更新且是上架状态就创建索引
        if (house.getStatus() == HouseStatus.PASSES.getValue()) {
            searchService.index(house.getId());
        }
        return ServiceResult.success();
    }

    @Override
    public ServiceMultiResult<HouseDTO> adminQuery(DataTableSearch searchBody) {
        List<HouseDTO> houseDTOS = new ArrayList<>();

        Sort sort = new Sort(Sort.Direction.fromString(searchBody.getDirection()), searchBody.getOrderBy());
        int page = searchBody.getStart() / searchBody.getLength();
        Pageable pageable = new PageRequest(page, searchBody.getLength(), sort);

        Specification<House> specification = (root, query, cb) -> {
            Predicate predicate = cb.equal(root.get("adminId"), LoginUserUtil.getUserId());
            // 逻辑上被删除的房源不被查询出来
            predicate = cb.and(predicate, cb.notEqual(root.get("status"), HouseStatus.DELETED.getValue()));

            if (searchBody.getCity() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("cityEnName"), searchBody.getCity()));
            }
            if (searchBody.getStatus() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), searchBody.getStatus()));
            }
            if (searchBody.getCreateTimeMin() != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("createTime"), searchBody.getCreateTimeMin()));
            }
            if (searchBody.getCreateTimeMax() != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("createTime"), searchBody.getCreateTimeMax()));
            }
            if (searchBody.getTitle() != null) {
                predicate = cb.and(predicate, cb.like(root.get("title"), "%" + searchBody.getTitle() + "%"));
            }
            return predicate;
        };

        Page<House> houses = houseRepository.findAll(specification, pageable);
        houses.forEach(house -> {
            HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);
            houseDTO.setCover(this.prefix + house.getCover());
            houseDTOS.add(houseDTO);
        });

        return new ServiceMultiResult<>(houses.getTotalElements(), houseDTOS);
    }

    @Override
    public ServiceResult<HouseDTO> findCompleteOne(Long id) {
        House house = houseRepository.findOne(id);
        if (house == null) {
            return  ServiceResult.notFound();
        }
        // 查询房屋的详细信息
        HouseDetail detail = houseDetailRepository.findByHouseId(id);
        // 查询房屋的图片
        List<HousePicture> pictures = housePictureRepository.findAllByHouseId(id);
        // 查询房屋所有的标签
        List<HouseTag> tags = houseTagRepository.findAllByHouseId(id);

        HouseDetailDTO houseDetailDTO = modelMapper.map(detail, HouseDetailDTO.class);
        List<HousePictureDTO> pictureDTOS = new ArrayList<>();
        for (HousePicture picture : pictures) {
            HousePictureDTO pictureDTO = modelMapper.map(picture, HousePictureDTO.class);
            pictureDTOS.add(pictureDTO);
        }

        List<String> tagList = new ArrayList<>();

        for (HouseTag tag : tags) {
            tagList.add(tag.getName());
        }
        HouseDTO result = modelMapper.map(house, HouseDTO.class);
        result.setHouseDetail(houseDetailDTO);
        result.setPictures(pictureDTOS);
        result.setTags(tagList);
        return ServiceResult.of(result);
    }

    @Override
    public ServiceResult updateStatus(Long id, int status) {
        House house = houseRepository.findOne(id);
        if (house == null) {
            return ServiceResult.notFound();
        }
        if (house.getStatus() == status) {
            return new ServiceResult(false, "状态没有发生变化");
        }
        if(house.getStatus() == HouseStatus.RENTED.getValue()) {
            return new ServiceResult(false, "已出租的房源不允许修改状态");
        }
        if (house.getStatus() == HouseStatus.DELETED.getValue()) {
            return new ServiceResult(false, "删除房源不允许操作");
        }

        houseRepository.updateStatus(id, status);
        // 上架更新索引 其他情况删除索引
        if (status == HouseStatus.PASSES.getValue()) {
            searchService.index(id);
        } else {
            searchService.remove(id);
        }
        return ServiceResult.success();
    }

    private List<HouseDTO> wrapperHouseResult(List<Long> houseIds) {
        List<HouseDTO> result = new ArrayList<>();
        Map<Long, HouseDTO> idToHouseMap = new HashMap<>();
        Iterable<House> houses = houseRepository.findAll(houseIds);
        houses.forEach(house -> {
            HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);
            houseDTO.setCover(this.prefix + house.getCover());
            idToHouseMap.put(house.getId(), houseDTO);
        });
        warppreHosueList(houseIds, idToHouseMap);
        // 矫正数据
        for (Long houseId : houseIds) {
            result.add(idToHouseMap.get(houseId));
        }
        return result;
    }

    @Override
    public ServiceMultiResult<HouseDTO> query(RentSearch rentSearch) {
        if (rentSearch.getKeywords() != null && !rentSearch.getKeywords().isEmpty()) {
            ServiceMultiResult<Long> esSearch = searchService.query(rentSearch);
            if (esSearch.getTotal() == 0) {
                return new ServiceMultiResult<>(0, new ArrayList<>());
            }
            return new ServiceMultiResult<>(esSearch.getTotal(), wrapperHouseResult(
                    esSearch.getResult()
            ));
        }
        return simpleQuery(rentSearch);
    }

    private ServiceMultiResult<HouseDTO> simpleQuery(RentSearch rentSearch) {
        Sort sort = HouseSort.generateSort(rentSearch.getOrderBy(), rentSearch.getOrderDirection());
        int page = rentSearch.getStart() / rentSearch.getSize();
        Pageable pageable = new PageRequest(page, rentSearch.getSize(), sort);
        Specification<House> specification = (root, criteriaQuery, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.equal(root.get("status"), HouseStatus.PASSES.getValue());
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("cityEnName"), rentSearch.getCityEnName()));

            if(HouseSort.DISTANCE_TO_SUBWAY_KEY.equals(rentSearch.getOrderBy())) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.gt(root.get(HouseSort.DISTANCE_TO_SUBWAY_KEY), -1));
            }
            return predicate;
        };

        Page<House> houses = houseRepository.findAll(specification, pageable);
        List<HouseDTO> houseDTOS = new ArrayList<>();
        List<Long> houseIds = new ArrayList<>();
        Map<Long, HouseDTO> idToHouseMap = Maps.newHashMap();
        houses.forEach((house -> {
            HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);
            houseDTO.setCover(this.prefix + house.getCover());
            houseDTOS.add(houseDTO);
            houseIds.add(house.getId());
            idToHouseMap.put(house.getId(), houseDTO);
        }));


        warppreHosueList(houseIds, idToHouseMap);
        return new ServiceMultiResult<>(houses.getTotalElements(), houseDTOS);
    }

    /**
     * 渲染详细信息和标签
     * @param houseIds
     * @param idToHouseMap
     */
    private void warppreHosueList(List<Long> houseIds, Map<Long, HouseDTO> idToHouseMap) {
        List<HouseDetail> details = houseDetailRepository.findAllByHouseIdIn(houseIds);
        details.forEach((houseDetail -> {
            HouseDTO houseDTO = idToHouseMap.get(houseDetail.getHouseId());
            HouseDetailDTO detailDTO = modelMapper.map(houseDetail, HouseDetailDTO.class);
            houseDTO.setHouseDetail(detailDTO);
        }));
        List<HouseTag> tags = houseTagRepository.findAllByHouseIdIn(houseIds);
        tags.forEach((houseTag -> {
            HouseDTO houseDTO = idToHouseMap.get(houseTag.getHouseId());
            houseDTO.getTags().add(houseTag.getName());
        }));
    }

    private List<HousePicture> generatePictures(HouseForm houseForm, Long houseId) {
        List<HousePicture> pictures = new ArrayList<>();
        if (houseForm.getPhotos() == null || houseForm.getPhotos().isEmpty()) {
            return pictures;
        }
        for (PhotoForm photo : houseForm.getPhotos()) {
            HousePicture picture = new HousePicture();
            picture.setHouseId(houseId);
            picture.setCdnPrefix(prefix);
            picture.setPath(photo.getPath());
            picture.setWidth(photo.getWidth());
            picture.setHeight(photo.getHeight());
            pictures.add(picture);
        }
        return pictures;
    }
    private ServiceResult<HouseDTO> warpperSubwayInfo(HouseDetail houseDetail, HouseForm houseForm) {
        Subway subway = subwayRepository.findOne(houseForm.getSubwayLineId());
        if (subway == null) {
            return new ServiceResult<>(false, "not valid subway line");
        }

        SubwayStation subwayStation = subwayStationRepository.findOne(houseForm.getSubwayStationId());
        if (subwayStation == null || subway.getId() != subwayStation.getSubwayId()) {
            return new ServiceResult<>(false, "not valid subway station line");
        }
        houseDetail.setSubwayLineId(subway.getId());
        houseDetail.setSubwayLineName(subway.getName());

        houseDetail.setSubwayStationId(subwayStation.getId());
        houseDetail.setSubwayStationName(subwayStation.getName());

        houseDetail.setDescription(houseForm.getDescription());
        houseDetail.setDetailAddress(houseForm.getDetailAddress());
        houseDetail.setLayoutDesc(houseForm.getLayoutDesc());
        houseDetail.setRentWay(houseForm.getRentWay());
        houseDetail.setRoundService(houseForm.getRoundService());
        houseDetail.setTraffic(houseForm.getTraffic());
        return null;
    }
}
