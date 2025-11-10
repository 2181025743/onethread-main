package com.nageoffer.onethread.dashboard.dev.server.controller;

import com.nageoffer.onethread.dashboard.dev.server.common.Result;
import com.nageoffer.onethread.dashboard.dev.server.common.Results;
import com.nageoffer.onethread.dashboard.dev.server.dto.ThreadPoolDetailRespDTO;
import com.nageoffer.onethread.dashboard.dev.server.dto.ThreadPoolListReqDTO;
import com.nageoffer.onethread.dashboard.dev.server.dto.ThreadPoolUpdateReqDTO;
import com.nageoffer.onethread.dashboard.dev.server.service.ThreadPoolManagerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 线程池管理控制器层
 * <p>
 * 作者：杨潇
 * 开发时间：2025-05-19
 */
@RestController
@RequiredArgsConstructor
public class ThreadPoolManagerController {

    private final ThreadPoolManagerService threadPoolManagerService;

    /**
     * 查询线程池集合
     */
    @GetMapping("/api/onethread-dashboard/thread-pools")
    public Result<List<ThreadPoolDetailRespDTO>> listThreadPool(ThreadPoolListReqDTO requestParam) {
        return Results.success(threadPoolManagerService.listThreadPool(requestParam));
    }

    /**
     * 更新线程池
     */
    @PutMapping("/api/onethread-dashboard/thread-pool")
    public Result<Void> updateGlobalThreadPool(@RequestBody @Valid ThreadPoolUpdateReqDTO requestParam) {
        threadPoolManagerService.updateGlobalThreadPool(requestParam);
        return Results.success();
    }
}
