package com.nageoffer.onethread.core.monitor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson2.JSON;
import com.nageoffer.onethread.core.config.ApplicationProperties;
import com.nageoffer.onethread.core.config.BootstrapConfigProperties;
import com.nageoffer.onethread.core.executor.OneThreadExecutor;
import com.nageoffer.onethread.core.executor.OneThreadRegistry;
import com.nageoffer.onethread.core.executor.ThreadPoolExecutorHolder;
import com.nageoffer.onethread.core.toolkit.ThreadFactoryBuilder;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池运行时监控器
 * <p>
 * 作者：杨潇
 * 开发时间：2025-05-05
 */
@Slf4j
public class ThreadPoolMonitor {

    private ScheduledExecutorService scheduler;
    private Map<String, ThreadPoolRuntimeInfo> micrometerMonitorCache;
    private Map<String, DeltaWrapper> rejectCountDeltaMap;
    private Map<String, DeltaWrapper> completedTaskDeltaMap;

    private static final String METRIC_NAME_PREFIX = "dynamic.thread-pool";
    private static final String DYNAMIC_THREAD_POOL_ID_TAG = METRIC_NAME_PREFIX + ".id";
    private static final String APPLICATION_NAME_TAG = "application.name";

    /**
     * 启动定时检查任务
     */
    public void start() {
        BootstrapConfigProperties.MonitorConfig monitorConfig = BootstrapConfigProperties.getInstance().getMonitor();
        if (!monitorConfig.getEnable()) {
            return;
        }

        // 初始化监控相关资源
        micrometerMonitorCache = new ConcurrentHashMap<>();
        rejectCountDeltaMap = new ConcurrentHashMap<>();
        completedTaskDeltaMap = new ConcurrentHashMap<>();
        scheduler = Executors.newScheduledThreadPool(
                1,
                ThreadFactoryBuilder.builder()
                        .namePrefix("scheduler_thread-pool_monitor")
                        .build()
        );

        // 每指定时间检查一次，初始延迟0秒
        scheduler.scheduleWithFixedDelay(() -> {
            Collection<ThreadPoolExecutorHolder> holders = OneThreadRegistry.getAllHolders();
            for (ThreadPoolExecutorHolder holder : holders) {
                ThreadPoolRuntimeInfo runtimeInfo = buildThreadPoolRuntimeInfo(holder);

                // 根据采集类型判断
                if (Objects.equals(monitorConfig.getCollectType(), "log")) {
                    logMonitor(runtimeInfo);
                } else if (Objects.equals(monitorConfig.getCollectType(), "micrometer")) {
                    micrometerMonitor(runtimeInfo);
                }
            }
        }, 0, monitorConfig.getCollectInterval(), TimeUnit.SECONDS);
    }

    /**
     * 停止报警检查
     */
    public void stop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    private void logMonitor(ThreadPoolRuntimeInfo runtimeInfo) {
        log.info("[ThreadPool Monitor] {} | Content: {}", runtimeInfo.getThreadPoolId(), JSON.toJSON(runtimeInfo));
    }

    /**
     * 采集 Micrometer 指标
     */
    private void micrometerMonitor(ThreadPoolRuntimeInfo runtimeInfo) {
        String threadPoolId = runtimeInfo.getThreadPoolId();
        ThreadPoolRuntimeInfo existingRuntimeInfo = micrometerMonitorCache.get(threadPoolId);

        // 只在首次注册时绑定 Gauge
        if (existingRuntimeInfo == null) {
            Iterable<Tag> tags = CollectionUtil.newArrayList(
                    Tag.of(DYNAMIC_THREAD_POOL_ID_TAG, threadPoolId),
                    Tag.of(APPLICATION_NAME_TAG, ApplicationProperties.getApplicationName())
            );

            ThreadPoolRuntimeInfo registerRuntimeInfo = BeanUtil.toBean(runtimeInfo, ThreadPoolRuntimeInfo.class);
            micrometerMonitorCache.put(threadPoolId, registerRuntimeInfo);

            // 注册总量指标
            Metrics.gauge(metricName("core.size"), tags, registerRuntimeInfo, ThreadPoolRuntimeInfo::getCorePoolSize);
            Metrics.gauge(metricName("maximum.size"), tags, registerRuntimeInfo, ThreadPoolRuntimeInfo::getMaximumPoolSize);
            Metrics.gauge(metricName("current.size"), tags, registerRuntimeInfo, ThreadPoolRuntimeInfo::getCurrentPoolSize);
            Metrics.gauge(metricName("largest.size"), tags, registerRuntimeInfo, ThreadPoolRuntimeInfo::getLargestPoolSize);
            Metrics.gauge(metricName("active.size"), tags, registerRuntimeInfo, ThreadPoolRuntimeInfo::getActivePoolSize);
            Metrics.gauge(metricName("queue.size"), tags, registerRuntimeInfo, ThreadPoolRuntimeInfo::getWorkQueueSize);
            Metrics.gauge(metricName("queue.capacity"), tags, registerRuntimeInfo, ThreadPoolRuntimeInfo::getWorkQueueCapacity);
            Metrics.gauge(metricName("queue.remaining.capacity"), tags, registerRuntimeInfo, ThreadPoolRuntimeInfo::getWorkQueueRemainingCapacity);

            // 注册 delta 指标
            DeltaWrapper completedDelta = new DeltaWrapper();
            completedTaskDeltaMap.put(threadPoolId, completedDelta);
            Metrics.gauge(metricName("completed.task.count"), tags, completedDelta, DeltaWrapper::getDelta);

            DeltaWrapper rejectDelta = new DeltaWrapper();
            rejectCountDeltaMap.put(threadPoolId, rejectDelta);
            Metrics.gauge(metricName("reject.count"), tags, rejectDelta, DeltaWrapper::getDelta);
        } else {
            // 更新属性（避免重新注册 Gauge）
            BeanUtil.copyProperties(runtimeInfo, existingRuntimeInfo);
        }

        // 每次都更新 delta 值
        completedTaskDeltaMap.get(threadPoolId).update(runtimeInfo.getCompletedTaskCount());
        rejectCountDeltaMap.get(threadPoolId).update(runtimeInfo.getRejectCount());
    }

    private String metricName(String name) {
        return String.join(".", METRIC_NAME_PREFIX, name);
    }

    @SneakyThrows
    private ThreadPoolRuntimeInfo buildThreadPoolRuntimeInfo(ThreadPoolExecutorHolder holder) {
        ThreadPoolExecutor executor = holder.getExecutor();
        BlockingQueue<?> queue = executor.getQueue();

        long rejectCount = -1L;
        if (executor instanceof OneThreadExecutor) {
            rejectCount = ((OneThreadExecutor) executor).getRejectCount().get();
        }

        int workQueueSize = queue.size();
        int remainingCapacity = queue.remainingCapacity();
        return ThreadPoolRuntimeInfo.builder()
                .threadPoolId(holder.getThreadPoolId())
                .corePoolSize(executor.getCorePoolSize())
                .maximumPoolSize(executor.getMaximumPoolSize())
                .activePoolSize(executor.getActiveCount())  // API 有锁，避免高频率调用
                .currentPoolSize(executor.getPoolSize())  // API 有锁，避免高频率调用
                .completedTaskCount(executor.getCompletedTaskCount())  // API 有锁，避免高频率调用
                .largestPoolSize(executor.getLargestPoolSize())  // API 有锁，避免高频率调用
                .workQueueName(queue.getClass().getSimpleName())
                .workQueueSize(workQueueSize)
                .workQueueRemainingCapacity(remainingCapacity)
                .workQueueCapacity(workQueueSize + remainingCapacity)
                .rejectedHandlerName(executor.getRejectedExecutionHandler().toString())
                .rejectCount(rejectCount)
                .build();
    }
}
