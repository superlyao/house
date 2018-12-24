package com.yliao.house.repository;

import com.yliao.house.entity.House;
import org.springframework.data.repository.CrudRepository;

public interface HouseRepository extends CrudRepository<House, Long> {
}
