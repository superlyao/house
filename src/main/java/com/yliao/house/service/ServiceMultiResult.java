package com.yliao.house.service;

import java.util.List;

/**
 * 通用数据结构
 * @param <T>
 */
public class ServiceMultiResult<T> {
    public long total;
    public List<T> result;

    public long getTotal() {
        return total;
    }

    public ServiceMultiResult(long total, List<T> result) {
        this.total = total;
        this.result = result;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<T> getResult() {
        return result;
    }

    public void setResult(List<T> result) {
        this.result = result;
    }

    public int getResulrtSize() {
        if (this.result == null) {
            return 0;
        }
        return this.result.size();
    }
}
