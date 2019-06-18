package com.laowan.oauth.resourceserver.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 *
 * @Order(-20)   不能随便加，会影响security和oauth2的校验顺序
 * @author cdov
 */
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    // 拦截所有请求,使用httpBasic方式登陆
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/getClientToken").permitAll()
                .antMatchers("/**").fullyAuthenticated()
                .and()
                .httpBasic() //拦截所有请求 通过httpBasic进行认证
                .and()
                .csrf().disable()
        ;


    }
}