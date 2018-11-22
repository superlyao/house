package com.yliao.house.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

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
}
