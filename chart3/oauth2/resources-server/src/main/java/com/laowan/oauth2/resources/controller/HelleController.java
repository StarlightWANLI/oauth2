package com.laowan.oauth2.resources.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

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

    @Autowired
    ResourceServerTokenServices tokenServices;


    @GetMapping("/hello")
    public  String hello(){
        return "我是资源服务器的保护资源";
    }



    @GetMapping("/user")
    public Principal userInfo(HttpServletRequest request){
        BearerTokenExtractor bearerTokenExtractor = new BearerTokenExtractor();
        Authentication authentication = bearerTokenExtractor.extract(request);
        String token = (String)authentication.getPrincipal();
        return tokenServices.loadAuthentication(token);
    }

}
