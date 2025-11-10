package com.nageoffer.onethread.dashboard.dev.server.controller;

import com.nageoffer.onethread.dashboard.dev.server.common.Result;
import com.nageoffer.onethread.dashboard.dev.server.common.Results;
import com.nageoffer.onethread.dashboard.dev.server.dto.WebThreadPoolBaseMetricsRespDTO;
import com.nageoffer.onethread.dashboard.dev.server.dto.WebThreadPoolStateRespDTO;
import com.nageoffer.onethread.dashboard.dev.server.service.WebThreadPoolInstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Web 线程池实例控制层
 * <p>
 * 作者：杨潇
 * 开发时间：2025-05-23
 */
@RestController
@RequiredArgsConstructor
public class WebThreadPoolInstanceController {

    private final WebThreadPoolInstanceService webThreadPoolInstanceService;

    /**
     * 获取线程池列表
     */
    @GetMapping("/api/onethread-dashboard/web/thread-pools/{namespace}/{serviceName}/basic-metrics")
    public Result<List<WebThreadPoolBaseMetricsRespDTO>> listBasicMetrics(
            @PathVariable String namespace,
            @PathVariable String serviceName) {
        return Results.success(webThreadPoolInstanceService.listBasicMetrics(namespace, serviceName));
    }

    /**
     * 获取线程池的完整运行时状态
     */
    @GetMapping("/api/onethread-dashboard/web/thread-pool/{networkAddress}")
    public Result<WebThreadPoolStateRespDTO> getRuntimeState(@PathVariable String networkAddress) {
        return Results.success(webThreadPoolInstanceService.getRuntimeState(networkAddress));
    }
}
