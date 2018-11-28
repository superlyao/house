package com.yliao.house.web.controller;

import com.yliao.house.base.ApiResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Author: yliao
 * @Date: Created in 2018/11/22
 */
@Controller
public class HomeController {
    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/get")
    @ResponseBody
    public ApiResponse get() {
        return ApiResponse.ofMessage(200, "ok");
    }
}
