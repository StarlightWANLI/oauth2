package com.laowan.oauth2.auth.mapper;

import com.laowan.oauth2.auth.model.AuthUser;
import org.apache.ibatis.annotations.Param;

public interface AuthUserMapper {

    AuthUser selectByUsername(@Param("username") String username);
}