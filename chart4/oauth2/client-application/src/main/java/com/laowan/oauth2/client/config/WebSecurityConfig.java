package com.laowan.oauth2.client.config;

import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableOAuth2Sso
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.
                // 禁用 CSRF 跨站伪造请求，便于测试
                        csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .and()
                // 验证所有请求
                .authorizeRequests()
                //允许访问的放在之前才有效,放开back是为了接收授权码，通过授权码获取token，然后获取资源服务器信息
                .antMatchers("/back").permitAll()
                .anyRequest()
                .authenticated()
                //允许访问首页
                // .antMatchers("/","/login").permitAll()
                .and()// 设置登出URL为 /logout
                .formLogin().successForwardUrl("/index")
                .and()
                // 设置登出URL为 /logout
                .logout().logoutUrl("/logout").permitAll()
                .logoutSuccessUrl("/")
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }
}
