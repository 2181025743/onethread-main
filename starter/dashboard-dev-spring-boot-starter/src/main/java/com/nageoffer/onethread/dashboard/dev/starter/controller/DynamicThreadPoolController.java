package com.nageoffer.onethread.dashboard.dev.starter.controller;

import com.nageoffer.onethread.dashboard.dev.starter.core.Result;
import com.nageoffer.onethread.dashboard.dev.starter.core.Results;
import com.nageoffer.onethread.dashboard.dev.starter.dto.ThreadPoolDashBoardDevBaseMetricsRespDTO;
import com.nageoffer.onethread.dashboard.dev.starter.dto.ThreadPoolDashBoardDevRespDTO;
import com.nageoffer.onethread.dashboard.dev.starter.service.DynamicThreadPoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 动态线程池控制器
 * <p>
 * 作者：杨潇
 * 开发时间：2025-05-17
 */
@RestController
@RequiredArgsConstructor
public class DynamicThreadPoolController {

    private final DynamicThreadPoolService dynamicThreadPoolService;

    /**
     * 获取线程池的轻量级运行指标
     */
    @GetMapping("/dynamic/thread-pool/{threadPoolId}/basic-metrics")
    public Result<ThreadPoolDashBoardDevBaseMetricsRespDTO> getBasicMetrics(@PathVariable String threadPoolId) {
        return Results.success(dynamicThreadPoolService.getBasicMetrics(threadPoolId));
    }

    /**
     * 获取线程池的完整运行时状态
     */
    @GetMapping("/dynamic/thread-pool/{threadPoolId}")
    public Result<ThreadPoolDashBoardDevRespDTO> getRuntimeInfo(@PathVariable String threadPoolId) {
        return Results.success(dynamicThreadPoolService.getRuntimeInfo(threadPoolId));
    }
}
