package com.laowan.oauth2.auth.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthTestController {

    @RequestMapping("hello")
    public  String hello(){
        return  "hello";
    }
}
