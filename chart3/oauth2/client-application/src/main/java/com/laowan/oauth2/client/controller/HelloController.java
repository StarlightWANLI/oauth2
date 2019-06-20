package com.laowan.oauth2.client.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class HelloController {

    @GetMapping("/index")
    public String welcome(String code) {
        return "欢迎来到王者峡谷！获取的授权码是"  +  code;
    }

}
