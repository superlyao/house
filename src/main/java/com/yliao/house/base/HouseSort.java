package com.yliao.house.base;

import com.google.common.collect.Sets;
import org.springframework.data.domain.Sort;

import java.util.Set;

/**
 * 排序生成器
 */
public class HouseSort {
    public static final String DEFAULT_SORT_KEY = "lastUpdateTime";
    public static final String DISTANCE_TO_SUBWAY_KEY = "distanceToSubway";

    private static final Set<String> SORT_KEYS = Sets.newHashSet(
            DEFAULT_SORT_KEY,
            DISTANCE_TO_SUBWAY_KEY,
            "createTime",
            "price",
            "area"
    );

    public static Sort generateSort(String key, String directionkey) {
        key = getSortKey(key);
        Sort.Direction direction = Sort.Direction.fromStringOrNull(directionkey);
        return new Sort(direction, key);
    }

    public static String getSortKey(String key) {
        if (!SORT_KEYS.contains(key)) {
            key = DEFAULT_SORT_KEY;
        }
        return key;
    }
}
