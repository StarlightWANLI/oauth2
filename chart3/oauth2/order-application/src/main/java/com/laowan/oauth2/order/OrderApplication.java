package com.laowan.oauth2.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * 访问：
 * http://localhost:8080/oauth/authorize?client_id=client&response_type=code&scope=app&state=BFSnIT'
 *
 */
@SpringBootApplication
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }


}
