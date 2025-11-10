package com.nageoffer.onethread.dashboard.dev.server.controller;

import com.nageoffer.onethread.dashboard.dev.server.common.Result;
import com.nageoffer.onethread.dashboard.dev.server.common.Results;
import com.nageoffer.onethread.dashboard.dev.server.dto.WebThreadPoolDetailRespDTO;
import com.nageoffer.onethread.dashboard.dev.server.dto.WebThreadPoolListReqDTO;
import com.nageoffer.onethread.dashboard.dev.server.dto.WebThreadPoolUpdateReqDTO;
import com.nageoffer.onethread.dashboard.dev.server.service.WebThreadPoolManagerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Web 线程池管理控制器层
 * <p>
 * 作者：杨潇
 * 开发时间：2025-05-23
 */
@RestController
@RequiredArgsConstructor
public class WebThreadPoolManagerController {

    private final WebThreadPoolManagerService webThreadPoolManagerService;

    /**
     * 查询线程池集合
     */
    @GetMapping("/api/onethread-dashboard/web/thread-pools")
    public Result<List<WebThreadPoolDetailRespDTO>> listThreadPool(WebThreadPoolListReqDTO requestParam) {
        return Results.success(webThreadPoolManagerService.listThreadPool(requestParam));
    }

    /**
     * 更新线程池
     */
    @PutMapping("/api/onethread-dashboard/web/thread-pool")
    public Result<Void> updateGlobalThreadPool(@RequestBody @Valid WebThreadPoolUpdateReqDTO requestParam) {
        webThreadPoolManagerService.updateGlobalThreadPool(requestParam);
        return Results.success();
    }
}
