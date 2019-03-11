package com.yliao.house.service.search;

public class HouseIndexMessage {
    // 索引操作类型 创建还是删除
    public static final String INDEX = "index";
    public static final String REMOVE = "remove";
    public static final int MAX_RETRY = 3;
    private Long houseId;
    private String operration;
    private int retry = 0;

    public HouseIndexMessage() {

    }

    public HouseIndexMessage(Long houseId, String operration, int retry) {
        this.houseId = houseId;
        this.operration = operration;
        this.retry = retry;
    }

    public Long getHouseId() {
        return houseId;
    }

    public void setHouseId(Long houseId) {
        this.houseId = houseId;
    }

    public String getOperration() {
        return operration;
    }

    public void setOperration(String operration) {
        this.operration = operration;
    }

    public int getRetry() {
        return retry;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }

    @Override
    public String toString() {
        return "HouseIndexMessage{" +
                "houseId=" + houseId +
                ", operration='" + operration + '\'' +
                ", retry=" + retry +
                '}';
    }
}
