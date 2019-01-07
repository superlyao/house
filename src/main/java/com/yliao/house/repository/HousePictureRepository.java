package com.yliao.house.repository;

import com.yliao.house.entity.HousePicture;
import com.yliao.house.entity.HouseTag;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface HousePictureRepository extends CrudRepository<HousePicture, Long> {
    List<HousePicture> findAllByHouseId(Long id);
}
