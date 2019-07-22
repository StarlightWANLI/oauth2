package com.laowan.oauth2.auth.mapper;


import com.laowan.oauth2.auth.model.AuthPermission;
import com.laowan.oauth2.auth.model.AuthRole;

import java.util.List;

public interface AuthPermissionMapper{

    List<AuthPermission> selectByRoles(List<AuthRole> authRoles);
}