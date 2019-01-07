package com.yliao.house.repository;

import com.yliao.house.entity.House;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface HouseRepository extends PagingAndSortingRepository<House, Long> {
}
