package com.yliao.house.service.search;

public class HouseIndexMessage {
    // 索引操作类型 创建还是删除
    public static final String INDEX = "index";
    public static final String REMOVE = "remove";
    // 重试的最大次数
    public static final int MAX_RETRY = 3;
    private Long houseId;
    private String operation;
    // 重试的次数
    private int retry = 0;

    public HouseIndexMessage() {

    }

    public HouseIndexMessage(Long houseId, String operration, int retry) {
        this.houseId = houseId;
        this.operation = operration;
        this.retry = retry;
    }

    public Long getHouseId() {
        return houseId;
    }

    public void setHouseId(Long houseId) {
        this.houseId = houseId;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
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
                ", operration='" + operation + '\'' +
                ", retry=" + retry +
                '}';
    }
}
