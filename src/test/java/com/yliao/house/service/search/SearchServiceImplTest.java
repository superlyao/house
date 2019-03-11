package com.yliao.house.service.search;

import com.yliao.house.HouseApplicationTests;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class SearchServiceImplTest extends HouseApplicationTests {

    @Autowired
    private ISearchService searchService;

    @Test
    public void testIndex() {
        searchService.index(17L);
    }

    @Test
    public void testRemove() {
        searchService.remove(35L);
    }
}
