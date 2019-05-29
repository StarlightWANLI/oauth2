package com.laowan.oauth.server.oauth2_server.config;

import com.laowan.oauth.server.oauth2_server.mapper.AuthPermissionMapper;
import com.laowan.oauth.server.oauth2_server.mapper.AuthRoleMapper;
import com.laowan.oauth.server.oauth2_server.mapper.AuthUserMapper;
import com.laowan.oauth.server.oauth2_server.model.AuthPermission;
import com.laowan.oauth.server.oauth2_server.model.AuthRole;
import com.laowan.oauth.server.oauth2_server.model.AuthUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @program: oauth2_server
 * @description: AuthorizationServer配置
 * @author: wanli
 * @create: 2019-05-22 16:00
 **/
@EnableAuthorizationServer
@Configuration
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    UserDetailsService userDetailsService;

    @Autowired
    AuthUserMapper authUserMapper;
    @Autowired
    AuthRoleMapper authRoleMapper;
    @Autowired
    AuthPermissionMapper authPermissionMapper;


    // accessToken有效期
    private int accessTokenValiditySeconds = 7200; // 两小时
    private int refreshTokenValiditySeconds = 7200; // 两小时

    /**
     * 配置认证客户端ClientDetailsService
     * @param clients
     * @throws Exception
     */
    @Override
    public void configure( ClientDetailsServiceConfigurer clients ) throws Exception {
        //这里主要配置的是客户端的信息，而不是认证用户的信息
        //添加客户端信息
        clients.inMemory()                  // 使用in-memory存储客户端信息
                .withClient("client")       // client_id
                .secret("{noop}123456")                   // client_secret
                .redirectUris("http://www.baidu.com")
                 .authorizedGrantTypes("authorization_code","password","client_credentials","refresh_token","implicit")// 该client允许的授权类型
                .scopes("app")                                   // 允许的授权范围
                .accessTokenValiditySeconds(accessTokenValiditySeconds)  //有效期时间
                .refreshTokenValiditySeconds(refreshTokenValiditySeconds)
        ;
    }

    /**
     * 配置认证服务  oauthServer
     * @param oauthServer
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) {
        // 允许表单认证
        oauthServer.allowFormAuthenticationForClients();
        // 允许check_token访问
        oauthServer.checkTokenAccess("permitAll()");
    }



    /**
     * 配置访问端口endpoints
     * @param endpoints
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints.authenticationManager(authenticationManager())
                // 这里一定要指定userDetailsService，不然进行refresh_token令牌是会出现  Handling error: IllegalStateException, UserDetailsService is required.
                .userDetailsService(userDetailsService())
                //允许get，post方法访问 （默认获取token只能post方法）
               // .allowedTokenEndpointRequestMethods(HttpMethod.GET,HttpMethod.POST)
        ;
    }

    @Bean
    AuthenticationManager authenticationManager() {
        AuthenticationManager authenticationManager = new AuthenticationManager() {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                return daoAuhthenticationProvider().authenticate(authentication);
            }
        };
        return authenticationManager;
    }


    @Bean
    public AuthenticationProvider daoAuhthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService());
        daoAuthenticationProvider.setHideUserNotFoundExceptions(false);
        daoAuthenticationProvider.setPasswordEncoder(PasswordEncoderFactories.createDelegatingPasswordEncoder());
        return daoAuthenticationProvider;
    }

    // 设置添加用户信息,正常应该从数据库中读取
    @Bean
    UserDetailsService userDetailsService() {
       //内存保存
/*      InMemoryUserDetailsManager userDetailsService = new InMemoryUserDetailsManager();
        userDetailsService.createUser(User.withUsername("user1").password("{noop}123456")
                .authorities("ROLE_USER").build());
        userDetailsService.createUser(User.withUsername("user2").password("{noop}1234567")
                .authorities("ROLE_USER").build());*/

        //数据库加载
        UserDetailsService userDetailsService = new UserDetailsService(){
            //根据username加载用户基本信息，权限信息，角色信息   校验可以在自定义的daoAuhthenticationProvider中做
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                AuthUser authUser = authUserMapper.selectByUsername(username);
                if (authUser == null) {
                    throw new UsernameNotFoundException("账户不存在");
                }
               // MyUserDetails myUserDetails = new MyUserDetails(authUser) ;
                //校验    这里只加载用户信息，尽量不要做校验   校验可以在自定义的daoAuhthenticationProvider中做
         /*       if (myUserDetails==null || myUserDetails.getAuthorities().isEmpty()) {
                    throw new UsernameNotFoundException("用户未分配任何权限");
                }*/

               //可以通过继承User来进行属性的扩展
                return new User(authUser.getUsername(),authUser.getPassword(),this.getAuthorities(authUser));
            }


            /**
             * 获取用户权限
             * @param authUser
             * @return
             */
            public Collection<GrantedAuthority> getAuthorities(AuthUser authUser) {
                Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
                //查用户角色
                List<AuthRole> authRoles = authRoleMapper.selectByUserId(authUser.getId());
                //查用户权限
                List<AuthPermission> authPermissions = authPermissionMapper.selectByRoles(authRoles);

                if (authRoles != null)
                {
                    //角色权限
                    Set<String> roleStrs =   authRoles.stream().map(AuthRole::getRoleName).collect(Collectors.toSet());
                    for (String code : roleStrs) {
                        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(code);
                        authorities.add(authority);
                    }
                    //方法权限
                    Set<String> authStrs =   authPermissions.stream().map(AuthPermission::getCode).collect(Collectors.toSet());
                    for (String code : authStrs) {
                        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(code);
                        authorities.add(authority);
                    }
                }
                return authorities;
            }
        };
        return userDetailsService;
    }



}
