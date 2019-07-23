package com.laowan.oauth2.auth.config;

import com.laowan.oauth2.auth.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.cache.SpringCacheBasedUserCache;

@Slf4j
public class MyUserCache implements UserCache {
    private static final Log logger = LogFactory.getLog(SpringCacheBasedUserCache.class);

    private final RedisService redisService;

    public MyUserCache(RedisService redisService) throws Exception {
        this.redisService = redisService;
    }

    @Override
    public UserDetails getUserFromCache(String username) {

        Object element = username != null ? this.redisService.get(username) : null;

        if (log.isDebugEnabled()) {
            logger.debug("Cache hit: " + (element != null) + "; username: " + username);
        }

        return element == null ? null : (UserDetails)element;
    }

    @Override
    public void putUserInCache(UserDetails user) {
        if (logger.isDebugEnabled()) {
            logger.debug("Cache put: " + user.getUsername());
        }

        this.redisService.set(user.getUsername(), user);

    }

    @Override
    public void removeUserFromCache(String username) {
        if (logger.isDebugEnabled()) {
            logger.debug("Cache remove: " + username);
        }
        this.redisService.remove(username);
    }
}
