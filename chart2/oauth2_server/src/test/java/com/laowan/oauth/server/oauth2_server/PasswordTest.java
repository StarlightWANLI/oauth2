package com.laowan.oauth.server.oauth2_server;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.LdapShaPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;

@Slf4j
public class PasswordTest {

    @Test
    public void testPasswordEncoder(){
/*        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        System.out.println(passwordEncoder.encode("123456"));
        DelegatingPasswordEncoder delegatingPasswordEncoder = new DelegatingPasswordEncoder();*/

        //DelegatingPasswordEncoder本质是根据encodingId找到对应的PasswordEncoder
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        String encode = passwordEncoder.encode("password");
        log.info("加密后的密码:" + encode);
        log.info("bcrypt密码对比:" + passwordEncoder.matches("password", encode));
        String md5Password = "{MD5}88e2d8cd1e92fd5544c8621508cd706b";//MD5加密前的密码为:password
        log.info("MD5密码对比:" + passwordEncoder.matches("password", encode));


        //直接声明对应的PasswordEncoder，如果使用通用的PasswordEncoder匹配需要加密后的字符串前面指明encodingId
         PasswordEncoder encoder = new Pbkdf2PasswordEncoder();
         encode = "{pbkdf2}"+encoder.encode("password");
        log.info("Pbkdf2加密后的密码:" + encode);
        log.info("Pbkdf2密码对比:" + passwordEncoder.matches("password", encode));


        //或者直接声明具体的PasswordEncoder，并用具体的PasswordEncoder去解密
        PasswordEncoder encoder1 =  new SCryptPasswordEncoder();
        encode = encoder1.encode("password");
        log.info("SCrypt加密后的密码:" + encode);
        log.info("SCrypt密码对比:" + encoder1.matches("password", encode));


    }

}
