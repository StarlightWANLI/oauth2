package com.laowan.oauth.resourceserver.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.authserver.AuthorizationServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;

import javax.annotation.Resource;

/**
 * @program: resource-server
 * @description: 资源服务器配置
 * @author: wanli
 * @create: 2019-06-05 16:01
 **/
@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Autowired
    ResourceServerProperties resourceServerProperties;


    /**
     * 认证配置属性
     */
    @Resource(name = "authServerProp")
    private AuthorizationServerProperties authorizationServerProperties;

    @Bean(name = "authServerProp")
    public AuthorizationServerProperties authorizationServerProperties(){
        return new AuthorizationServerProperties();
    }

    /**
     * 核心配置一：   ResourceServerSecurityConfigurer
     * 配置资源服务的id，密钥管理等
     * @param resources
     */
    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
            /*security.oauth2.resource.id=payAgentService
              security.oauth2.resource.user-info-uri=http://localhost:12570/user
              security.oauth2.resource.prefer-token-info=false*/
        //  resources.tokenServices();//资源服务的token处理办法
        //  resources.resourceId();
        resources.stateless(false)
                .tokenServices(tokenServices())
                .resourceId(resourceServerProperties.getResourceId());
    }


    /**
     * 配置token服务信息，用来进行token认证
     * @return
     */
    @Bean
    @Primary
    public RemoteTokenServices tokenServices() {
        RemoteTokenServices services = new RemoteTokenServices();
        services.setCheckTokenEndpointUrl(resourceServerProperties.getTokenInfoUri());
        services.setClientId(resourceServerProperties.getClientId());
        services.setClientSecret(resourceServerProperties.getClientSecret());
        services.setCheckTokenEndpointUrl(authorizationServerProperties.getCheckTokenAccess());
        return services;
    }

}
