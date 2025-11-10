package com.nageoffer.onethread.dashboard.dev.server.service;

import com.nageoffer.onethread.dashboard.dev.server.dto.UserDetailRespDTO;
import com.nageoffer.onethread.dashboard.dev.server.dto.UserLoginReqDTO;
import com.nageoffer.onethread.dashboard.dev.server.dto.UserLoginRespDTO;

/**
 * 用户业务接口层
 * <p>
 * 作者：杨潇
 * 开发时间：2025-05-17
 */
public interface UserService {

    /**
     * 用户登录
     *
     * @param requestParam 用户名、密码
     * @return 用户登录返回信息
     */
    UserLoginRespDTO login(UserLoginReqDTO requestParam);

    /**
     * 获取用户明细信息
     *
     * @return 用户明细信息
     */
    UserDetailRespDTO getUser();
}
