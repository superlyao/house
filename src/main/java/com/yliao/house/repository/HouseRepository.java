package com.yliao.house.repository;

import com.yliao.house.entity.House;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface HouseRepository extends PagingAndSortingRepository<House, Long>,
        JpaSpecificationExecutor<House> {

    @Modifying
    @Query("update House as house set house.status = :status where house.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") int status);
}
