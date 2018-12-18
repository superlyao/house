package com.yliao.house.web.dto;

public final class QiNiuPutRet {
    public String key;
    public String hash;
    public String bucket;
    public int height;
    public int width;

    @Override
    public String toString() {
        return "QiNiuPutRet{" +
                "key='" + key + '\'' +
                ", hash='" + hash + '\'' +
                ", bucket='" + bucket + '\'' +
                ", height=" + height +
                ", width=" + width +
                '}';
    }
}
