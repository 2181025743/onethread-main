
package com.nageoffer.onethread.dashboard.dev.server.dto;

import lombok.Data;

/**
 * 用户登录请求参数实体
 * <p>
 * 作者：杨潇
 * 开发时间：2025-05-17
 */
@Data
public class UserLoginReqDTO {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;
}
