package com.laowan.oauth2.resources.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * http://localhost:8090/hello?access_token=680687ba-b155-4d1a-a4eb-845d71df37f5
 *
 * @program: resource-server
 * @description: 测试类
 * @author: wanli
 * @create: 2019-06-05 16:19
 **/
@RestController
@Slf4j
public class HelleController {

    @GetMapping("/hello")
    public  String hello(){
        return "我是资源服务器的保护资源";
    }

}
