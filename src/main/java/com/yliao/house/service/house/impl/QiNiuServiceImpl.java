package com.yliao.house.service.house.impl;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.yliao.house.service.house.IQiNiuService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;

/**
 * @Author: yliao
 * @Date: Created in 2018/12/11
 */
@Service
public class QiNiuServiceImpl implements IQiNiuService, InitializingBean {
    @Autowired
    private UploadManager uploadManager;

    @Autowired
    private BucketManager bucketManager;

    @Autowired
    private Auth auth;

    @Value("${qiniu.Bucket}")
    private String bucket;

    private StringMap putPolicy;

    @Override
    public Response uploadFile(File file) throws QiniuException {
        Response response = this.uploadManager.put(file, null, getAuthToken());
        // 重试次数
        int retry = 0;
        while (response.needRetry() && retry < 3) {
            response = this.uploadManager.put(file, null, getAuthToken());
            retry++;
        }
        return response;
    }

    @Override
    public Response uploadFile(InputStream inputStream) throws QiniuException {
        return null;
    }

    @Override
    public Response delete(String key) throws QiniuException {
        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.putPolicy = new StringMap();
        putPolicy.put("returnBody", "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"bucket\":\"$(bucket)\",\"width\":$(imageInfo.width), \"height\":${imageInfo.height}}");
    }

    /**
     * 获取上传凭证
     */
    private String getAuthToken() {
        return this.auth.uploadToken(bucket, null, 3600, putPolicy);
    }
}
