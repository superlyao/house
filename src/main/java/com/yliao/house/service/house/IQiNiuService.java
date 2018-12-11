package com.yliao.house.service.house;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;

import java.io.File;
import java.io.InputStream;

/**
 * @Author: yliao
 * @Date: Created in 2018/12/11
 */
public interface IQiNiuService {
    Response uploadFile(File file) throws QiniuException;

    Response uploadFile(InputStream inputStream) throws  QiniuException;

    Response delete(String key) throws QiniuException;
}
