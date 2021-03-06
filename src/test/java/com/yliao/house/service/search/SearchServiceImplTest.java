package com.yliao.house.service.search;

import com.yliao.house.HouseApplicationTests;
import com.yliao.house.entity.House;
import com.yliao.house.repository.HouseRepository;
import com.yliao.house.service.ServiceMultiResult;
import com.yliao.house.service.house.IHouseService;
import com.yliao.house.web.form.RentSearch;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class SearchServiceImplTest extends HouseApplicationTests {

    @Autowired
    private ISearchService searchService;

    @Autowired
    private IHouseService houseService;

    @Autowired
    private HouseRepository houseRepository;

    @Test
    public void testIndex() {
        searchService.index(17L);
    }

    @Test
    public void testRemove() {
        searchService.remove(35L);
    }

    @Test
    public void query() {
        RentSearch search = new RentSearch();
        search.setCityEnName("bj");
        search.setStart(0);
        search.setSize(10);
        ServiceMultiResult<Long> query = searchService.query(search);
        Assert.assertEquals(query.getTotal(), 10);
    }

    @Test
    public void createAllTest() {
        Iterable<House> all = houseRepository.findAll();
        for (House house : all) {
            searchService.index(house.getId());
        }
    }
}
