package com.yliao.house.service.house;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.yliao.house.HouseApplicationTests;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

public class QiNiuServiceTests extends HouseApplicationTests {

    @Autowired
    private IQiNiuService iQiNiuService;

    @Test
    public void testUploadFile() {
        String fileName = "E:\\1.jpg";
        File file = new File(fileName);
        Assert.assertTrue(file.exists());

        try {
            Response response = iQiNiuService.uploadFile(file);
            Assert.assertTrue(response.isOK());
        } catch (QiniuException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testDelete() {
        String key = "FsjG2lfFTYgDaoq60-rQVtLqoUj9";
        try {
            Response response = iQiNiuService.delete(key);
            Assert.assertTrue(response.isOK());
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }
}
