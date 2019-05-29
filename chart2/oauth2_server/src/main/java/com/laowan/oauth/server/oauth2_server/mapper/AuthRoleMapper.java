package com.laowan.oauth.server.oauth2_server.mapper;

import com.laowan.oauth.server.oauth2_server.model.AuthRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AuthRoleMapper{

    List<AuthRole> selectByUserId(@Param("userId") Long id);
}