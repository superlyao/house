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
    /**
     * 文件上传
     * @param file
     * @return
     * @throws QiniuException
     */
    Response uploadFile(File file) throws QiniuException;

    /**
     * 文件流上传
     * @param inputStream
     * @return
     * @throws QiniuException
     */
    Response uploadFile(InputStream inputStream) throws  QiniuException;

    /**
     * 删除
     * @param key
     * @return
     * @throws QiniuException
     */
    Response delete(String key) throws QiniuException;
}
