package com.nageoffer.onethread.dashboard.dev.server.controller;

import com.nageoffer.onethread.dashboard.dev.server.common.Result;
import com.nageoffer.onethread.dashboard.dev.server.common.Results;
import com.nageoffer.onethread.dashboard.dev.server.dto.ThreadPoolBaseMetricsRespDTO;
import com.nageoffer.onethread.dashboard.dev.server.dto.ThreadPoolStateRespDTO;
import com.nageoffer.onethread.dashboard.dev.server.service.ThreadPoolInstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 线程池实例控制层
 * <p>
 * 作者：杨潇
 * 开发时间：2025-05-23
 */
@RestController
@RequiredArgsConstructor
public class ThreadPoolInstanceController {

    private final ThreadPoolInstanceService threadPoolInstanceService;

    /**
     * 获取动态线程池列表
     */
    @GetMapping("/api/onethread-dashboard/thread-pools/{namespace}/{serviceName}/{threadPoolId}/basic-metrics")
    public Result<List<ThreadPoolBaseMetricsRespDTO>> listBasicMetrics(
            @PathVariable String namespace,
            @PathVariable String serviceName,
            @PathVariable String threadPoolId) {
        return Results.success(threadPoolInstanceService.listBasicMetrics(namespace, serviceName, threadPoolId));
    }

    /**
     * 获取动态线程池的完整运行时状态
     */
    @GetMapping("/api/onethread-dashboard/thread-pool/{threadPoolId}/{networkAddress}")
    public Result<ThreadPoolStateRespDTO> getRuntimeState(
            @PathVariable String threadPoolId,
            @PathVariable String networkAddress) {
        return Results.success(threadPoolInstanceService.getRuntimeState(threadPoolId, networkAddress));
    }
}
