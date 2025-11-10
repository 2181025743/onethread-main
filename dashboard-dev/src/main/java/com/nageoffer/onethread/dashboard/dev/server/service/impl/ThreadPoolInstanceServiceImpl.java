package com.nageoffer.onethread.dashboard.dev.server.service.impl;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.nageoffer.onethread.dashboard.dev.server.common.Result;
import com.nageoffer.onethread.dashboard.dev.server.dto.ThreadPoolBaseMetricsRespDTO;
import com.nageoffer.onethread.dashboard.dev.server.dto.ThreadPoolStateRespDTO;
import com.nageoffer.onethread.dashboard.dev.server.remote.client.NacosProxyClient;
import com.nageoffer.onethread.dashboard.dev.server.remote.dto.NacosServiceListRespDTO;
import com.nageoffer.onethread.dashboard.dev.server.service.ThreadPoolInstanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 线程池实例接口实现层
 * <p>
 * 作者：杨潇
 * 开发时间：2025-05-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThreadPoolInstanceServiceImpl implements ThreadPoolInstanceService {

    private final NacosProxyClient nacosProxyClient;

    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors() * 10,
            Runtime.getRuntime().availableProcessors() * 10,
            60,
            TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            new ThreadPoolExecutor.DiscardOldestPolicy()
    );

    @Override
    public List<ThreadPoolBaseMetricsRespDTO> listBasicMetrics(String namespace, String serviceName, String threadPoolId) {
        NacosServiceListRespDTO serviceListResponse = nacosProxyClient.getService(namespace, serviceName);
        if (serviceListResponse == null || serviceListResponse.getCount() == 0) {
            return List.of();
        }

        List<CompletableFuture<ThreadPoolBaseMetricsRespDTO>> futures = serviceListResponse.getServiceList().stream()
                .map(service -> CompletableFuture.supplyAsync(() -> {
                    try {
                        // 调用 DashBoard Starter 提供的内置接口，获取线程池运行信息
                        String networkAddress = service.getIp() + ":" + service.getPort();
                        String resultStr = HttpUtil.get("http://" + networkAddress + "/dynamic/thread-pool/" + threadPoolId + "/basic-metrics");
                        Result<ThreadPoolBaseMetricsRespDTO> result = JSON.parseObject(resultStr, new TypeReference<>() {
                        });
                        return result.getData();
                    } catch (Exception e) {
                        log.error("Error fetching metrics from {}", service.getIp(), e);
                        return null;
                    }
                }, threadPoolExecutor))
                .collect(Collectors.toList());

        return futures.stream()
                .map(CompletableFuture::join) // 等待每个 Future 完成
                .filter(Objects::nonNull) // 过滤掉异常导致的 null 值
                .collect(Collectors.toList());
    }

    @Override
    public ThreadPoolStateRespDTO getRuntimeState(String threadPoolId, String networkAddress) {
        String resultStr = HttpUtil.get("http://" + networkAddress + "/dynamic/thread-pool/" + threadPoolId);
        Result<ThreadPoolStateRespDTO> result = JSON.parseObject(resultStr, new TypeReference<>() {
        });
        return result.getData();
    }
}
