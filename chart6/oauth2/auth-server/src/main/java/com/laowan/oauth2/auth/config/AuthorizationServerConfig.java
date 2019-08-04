package com.laowan.oauth2.auth.config;

import com.laowan.oauth2.auth.mapper.AuthPermissionMapper;
import com.laowan.oauth2.auth.mapper.AuthRoleMapper;
import com.laowan.oauth2.auth.mapper.AuthUserMapper;
import com.laowan.oauth2.auth.model.AuthUser;
import com.laowan.oauth2.auth.model.AuthPermission;
import com.laowan.oauth2.auth.model.AuthRole;
import com.laowan.oauth2.auth.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheManager;
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
import org.springframework.security.core.userdetails.cache.SpringCacheBasedUserCache;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.security.oauth2.provider.token.store.jwk.JwkTokenStore;

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
@Slf4j
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    UserDetailsService userDetailsService;

    @Autowired
    AuthUserMapper authUserMapper;
    @Autowired
    AuthRoleMapper authRoleMapper;
    @Autowired
    AuthPermissionMapper authPermissionMapper;

    @Autowired
    RedisCacheManager redisCacheManager;


    @Autowired
    private TokenStore tokenStore;

    @Autowired
    RedisService redisService;


    // accessToken有效期
    private int accessTokenValiditySeconds = 7200; // 两小时
    private int refreshTokenValiditySeconds = 7200; // 两小时


    /**
     * token的存储策略
     * @return
     */
    @Bean
    public TokenStore tokenStore(){
        return new JwtTokenStore(jwtAccessTokenConverter());
    }

    //使用同一个密钥来编码 JWT 中的  OAuth2 令牌
    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(new ClassPathResource("mytest.jks"), "mypass".toCharArray());
        converter.setKeyPair(keyStoreKeyFactory.getKeyPair("mytest"));
    //    converter.setSigningKey("123");
        return converter;
    }


    /**
     * 配置认证客户端ClientDetailsService
     * @param clients
     * @throws Exception
     */
    @Override
    public void configure( ClientDetailsServiceConfigurer clients ) throws Exception {
        //这里主要配置的是客户端的信息，而不是认证用户的信息
        //添加客户端信息     用户授权的时候，将客户端信息
        clients.inMemory()                  // 使用in-memory存储客户端信息
                .withClient("client")       // client_id
                .secret("{noop}123456")                   // client_secret
                .redirectUris("http://localhost:8100/back")     //应该是客户端内部自己的地址,用来接收返回的授权码，进行接下来的请求
                 .authorizedGrantTypes("authorization_code","password","client_credentials","refresh_token","implicit")// 该client允许的授权类型
                .scopes("app")                                   // 允许的授权范围
                .accessTokenValiditySeconds(accessTokenValiditySeconds)  //有效期时间
                .refreshTokenValiditySeconds(refreshTokenValiditySeconds)
                .autoApprove(true)         //登录后绕过批准询问(/oauth/confirm_access)
        .and()
                .withClient("order")       // client_id
                .secret("{noop}123456")                   // client_secret
                .redirectUris("http://localhost:8220/back")     //应该是客户端内部自己的地址,用来接收返回的授权码，进行接下来的请求
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
        oauthServer
                //允许所有资源服务器访问公钥端点（/oauth/token_key）
                //只允许验证用户访问令牌解析端点（/oauth/check_token）
                .tokenKeyAccess("permitAll()").checkTokenAccess("isAuthenticated()")
                // 允许客户端发送表单来进行权限认证来获取令牌
                .allowFormAuthenticationForClients();
    }



    /**
     * 配置访问端口endpoints
     * @param endpoints
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        tokenEnhancerChain.setTokenEnhancers(
                Arrays.asList(tokenEnhancer(), jwtAccessTokenConverter()));

        endpoints.authenticationManager(authenticationManager())
        // 这里一定要指定userDetailsService，不然进行refresh_token令牌是会出现  Handling error: IllegalStateException, UserDetailsService is required.
        .userDetailsService(userDetailsService())//设置token的保存方式
        .tokenStore(tokenStore())
        //向token中添加属性
        .tokenEnhancer(tokenEnhancerChain)
                //允许get，post方法访问 （默认获取token只能post方法）
               // .allowedTokenEndpointRequestMethods(HttpMethod.GET,HttpMethod.POST)
        ;
    }

    @Bean
    public TokenEnhancer tokenEnhancer() {
        return new CustomTokenEnhancer();
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
    public AuthenticationProvider daoAuhthenticationProvider()  {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService());
        daoAuthenticationProvider.setHideUserNotFoundExceptions(false);
        daoAuthenticationProvider.setPasswordEncoder(PasswordEncoderFactories.createDelegatingPasswordEncoder());

        //实现User信息的缓存
        try {
            // ConcurrentMap<String, Cache>   根据名称获取缓存对象
          //  daoAuthenticationProvider.setUserCache(new SpringCacheBasedUserCache(redisCacheManager.getCache("redis")));
            daoAuthenticationProvider.setUserCache(new MyUserCache(redisService));
        } catch (Exception e) {
           log.error("出现异常,{}",e);
        }
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
