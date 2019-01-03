package com.yliao.house.service.house.impl;

import com.sun.org.apache.xpath.internal.FoundIndex;
import com.yliao.house.base.LoginUserUtil;
import com.yliao.house.entity.*;
import com.yliao.house.repository.*;
import com.yliao.house.service.ServiceResult;
import com.yliao.house.service.house.IHouseService;
import com.yliao.house.web.dto.HouseDTO;
import com.yliao.house.web.dto.HouseDetailDTO;
import com.yliao.house.web.dto.HousePictureDTO;
import com.yliao.house.web.form.HouseForm;
import com.yliao.house.web.form.PhotoForm;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
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
        house.setCover(this.prefix + houseDTO.getCover());

        List<String> tags = houseForm.getTags();
        if (tags !=null || !tags.isEmpty()) {
            List<HouseTag> houseTags = new ArrayList<>();
            for (String tag : tags) {
                houseTags.add(new HouseTag(house.getId(), tag));
            }
            houseTagRepository.save(houseTags);
            houseDTO.setTags(tags);
        }
        return new ServiceResult<>(true, null, houseDTO);
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
