package com.laowan.oauth2.auth.mapper;

import com.laowan.oauth2.auth.model.AuthRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AuthRoleMapper{

    List<AuthRole> selectByUserId(@Param("userId") Long id);
}