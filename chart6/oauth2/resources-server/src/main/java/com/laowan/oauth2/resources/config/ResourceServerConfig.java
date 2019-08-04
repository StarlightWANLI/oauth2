package com.laowan.oauth2.resources.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.security.oauth2.authserver.AuthorizationServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.jwt.crypto.sign.*;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @program: resource-server
 * @description: 资源服务器配置
 * @author: wanli
 * @create: 2019-06-05 16:01
 **/
@Configuration
@EnableResourceServer
@Slf4j
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Autowired
    ResourceServerProperties resourceServerProperties;

    /**
     * 核心配置一：   ResourceServerSecurityConfigurer
     * 配置资源服务的id，密钥管理等
     * @param resources
     */
    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources.stateless(false)
                .tokenServices(tokenServices())
                //resourceId也是资源服务器有效性判断的元素之一
                .resourceId(resourceServerProperties.getResourceId());
    }

    /**
     * 配置token服务信息，用来进行token认证
     * @return
     */
    @Bean
    @Primary
    public ResourceServerTokenServices tokenServices() {
        DefaultTokenServices tokenServices = new DefaultTokenServices();
        //只需要配置token的解析方式即可
        tokenServices.setTokenStore(tokenStore());
        return tokenServices;
    }


    /**
     * 必须配置
     * @return
     */
    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(accessTokenConverter());
    }

    //与授权服务器使用共同的密钥进行解析
    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
       // converter.setSigningKey("123");
        //设置用于解码的非对称加密的公钥
        converter.setVerifierKey(getPubKey());
       // converter.setSigner(new RsaSigner("123456"));
        return converter;
    }


    private String getPubKey() {
        Resource resource = new ClassPathResource("publicKey.txt");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            System.out.println("本地公钥");
            return br.lines().collect(Collectors.joining("\n"));
        } catch (IOException ioe) {
            return getKeyFromAuthorizationServer();
        }
    }

    private String getKeyFromAuthorizationServer() {
        ObjectMapper objectMapper = new ObjectMapper();
        String pubKey = new RestTemplate().getForObject(resourceServerProperties.getJwt().getKeyUri(), String.class);
        try {
            Map map = objectMapper.readValue(pubKey, Map.class);
            System.out.println("联网公钥");
            return map.get("value").toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        RsaVerifier rsaVerifier =  new RsaVerifier("-----Begin Public Key-----\n" +
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhS1va39HWg+Qx5xSSOpQ22JW72isKQAP\n" +
                "L6gyGzDWfgkRUUZzA1LyeXYy3m6VrKS1dL1gVdsMhFHm5zaujNsURWqT4eqcku47cGNwwStX8/+P\n" +
                "DjYhrZbN3JKtEjkeioUx/tUmsFAGyy9JOpDeMP/t/mYJEkktvbZ5Z8DM5+cw//XUBmPVduh9K7t7\n" +
                "YcXP7F5uhaZX4Ly1A19I71N4EI1i+ikMm7FNX4cwknFips775FTXNLyA175d900bw8Ys7jNVOiu3\n" +
                "DCb0DGmn5E2mZB2vKY4ZYbQd+Z6J20wypPu9Ms08e2ItVMKrF9cWXO01WFjyqd2ZGeQQqpwebkob\n" +
                "m7AdhwIDAQAB\n" +
                "-----End Public Key-----");
        log.info(rsaVerifier.toString());
    }

}
