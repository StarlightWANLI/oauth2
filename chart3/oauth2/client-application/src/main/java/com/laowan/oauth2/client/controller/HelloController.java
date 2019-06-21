package com.laowan.oauth2.client.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class HelloController {

    @GetMapping("/index")
    public String welcome() {
        return "欢迎来到王者峡谷！";
    }

    @GetMapping("/back")
    public String back(String code) {
        return "client系统授权回调，授权码是：" + code;
    }

}
