package com.nageoffer.onethread.dashboard.dev.server.controller;

import com.nageoffer.onethread.dashboard.dev.server.common.Result;
import com.nageoffer.onethread.dashboard.dev.server.common.Results;
import com.nageoffer.onethread.dashboard.dev.server.dto.UserDetailRespDTO;
import com.nageoffer.onethread.dashboard.dev.server.dto.UserLoginReqDTO;
import com.nageoffer.onethread.dashboard.dev.server.dto.UserLoginRespDTO;
import com.nageoffer.onethread.dashboard.dev.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户业务控制层
 * <p>
 * 作者：杨潇
 * 开发时间：2025-05-17
 */
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/api/onethread-dashboard/auth/login")
    public Result<UserLoginRespDTO> login(@RequestBody UserLoginReqDTO requestParam) {
        return Results.success(userService.login(requestParam));
    }

    /**
     * 查询用户信息
     */
    @GetMapping("/api/onethread-dashboard/user")
    public Result<UserDetailRespDTO> getUser() {
        return Results.success(userService.getUser());
    }
}
