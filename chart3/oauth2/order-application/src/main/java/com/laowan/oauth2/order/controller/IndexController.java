package com.laowan.oauth2.order.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {

    @GetMapping("/index")
    public String welcome(String code) {
        return "欢迎订单系统！";
    }


    @GetMapping("/back")
    public String back(String code) {
        return "订单系统授权回调，授权码是：" + code;
    }
}
