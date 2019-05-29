package com.laowan.oauth.server.oauth2_server.mapper;

import com.laowan.oauth.server.oauth2_server.model.AuthUser;
import org.apache.ibatis.annotations.Param;

public interface AuthUserMapper {

    AuthUser selectByUsername(@Param("username") String username);
}