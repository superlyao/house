package com.yliao.house.base;

/**
 * 房源状态
 */
public enum  HouseStatus {
    NOT_AUDITED(0),
    PASSES(0),
    RENTED(2),
    DELETED(3);

    private int value;

    HouseStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}


