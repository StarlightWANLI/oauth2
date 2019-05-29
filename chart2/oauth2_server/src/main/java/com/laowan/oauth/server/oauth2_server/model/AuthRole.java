package com.laowan.oauth.server.oauth2_server.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @program: ICS
 * @description: 角色实体
 * @author: wanli
 * @create: 2018-06-15 15:49
 **/
@Data
@NoArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler", "fieldHandler"}, ignoreUnknown = true)
public class AuthRole {
    /**
     * 主键
     */
    private Integer id;

    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空！")
    private String roleName;

    /**
     * 角色描述
     */
    private String description;

}
