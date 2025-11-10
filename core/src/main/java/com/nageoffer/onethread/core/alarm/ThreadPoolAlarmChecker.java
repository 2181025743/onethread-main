package com.nageoffer.onethread.core.alarm;

import cn.hutool.core.date.DateUtil;
import com.nageoffer.onethread.core.config.ApplicationProperties;
import com.nageoffer.onethread.core.executor.OneThreadExecutor;
import com.nageoffer.onethread.core.executor.OneThreadRegistry;
import com.nageoffer.onethread.core.executor.ThreadPoolExecutorHolder;
import com.nageoffer.onethread.core.executor.ThreadPoolExecutorProperties;
import com.nageoffer.onethread.core.notification.dto.ThreadPoolAlarmNotifyDTO;
import com.nageoffer.onethread.core.notification.service.NotifierDispatcher;
import com.nageoffer.onethread.core.toolkit.ThreadFactoryBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池运行状态报警检查器
 * <p>
 * 作者：杨潇
 * 开发时间：2025-05-04
 */
@Slf4j
@RequiredArgsConstructor
public class ThreadPoolAlarmChecker {

    private final NotifierDispatcher notifierDispatcher;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(
            1,
            ThreadFactoryBuilder.builder()
                    .namePrefix("scheduler_thread-pool_alarm_checker")
                    .build()
    );
    private final Map<String, Long> lastRejectCountMap = new ConcurrentHashMap<>();

    /**
     * 启动定时检查任务
     */
    public void start() {
        // 每10秒检查一次，初始延迟0秒
        scheduler.scheduleWithFixedDelay(this::checkAlarm, 0, 5, TimeUnit.SECONDS);
    }

    /**
     * 停止报警检查
     */
    public void stop() {
        if (!scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    /**
     * 报警检查核心逻辑
     */
    private void checkAlarm() {
        Collection<ThreadPoolExecutorHolder> holders = OneThreadRegistry.getAllHolders();
        for (ThreadPoolExecutorHolder holder : holders) {
            if (holder.getExecutorProperties().getAlarm().getEnable()) {
                checkQueueUsage(holder);
                checkActiveRate(holder);
                checkRejectCount(holder);
            }
        }
    }

    /**
     * 检查队列使用率
     */
    private void checkQueueUsage(ThreadPoolExecutorHolder holder) {
        ThreadPoolExecutor executor = holder.getExecutor();
        ThreadPoolExecutorProperties properties = holder.getExecutorProperties();

        BlockingQueue<?> queue = executor.getQueue();
        int queueSize = queue.size();
        int capacity = queueSize + queue.remainingCapacity();

        if (capacity == 0) {
            return;
        }

        int usageRate = (int) Math.round((queueSize * 100.0) / capacity);
        int threshold = properties.getAlarm().getQueueThreshold();

        if (usageRate >= threshold) {
            sendAlarmMessage("Capacity", holder);
        }
    }

    /**
     * 检查线程活跃度（活跃线程数 / 最大线程数）
     */
    private void checkActiveRate(ThreadPoolExecutorHolder holder) {
        ThreadPoolExecutor executor = holder.getExecutor();
        ThreadPoolExecutorProperties properties = holder.getExecutorProperties();

        int activeCount = executor.getActiveCount(); // API 有锁，避免高频率调用
        int maximumPoolSize = executor.getMaximumPoolSize();

        if (maximumPoolSize == 0) {
            return;
        }

        int activeRate = (int) Math.round((activeCount * 100.0) / maximumPoolSize);
        int threshold = properties.getAlarm().getActiveThreshold();

        if (activeRate >= threshold) {
            sendAlarmMessage("Activity", holder);
        }
    }

    /**
     * 检查拒绝策略执行次数
     */
    private void checkRejectCount(ThreadPoolExecutorHolder holder) {
        ThreadPoolExecutor executor = holder.getExecutor();
        String threadPoolId = holder.getThreadPoolId();

        // 只处理自定义线程池类型
        if (!(executor instanceof OneThreadExecutor)) {
            return;
        }

        OneThreadExecutor oneThreadExecutor = (OneThreadExecutor) executor;
        long currentRejectCount = oneThreadExecutor.getRejectCount().get();
        long lastRejectCount = lastRejectCountMap.getOrDefault(threadPoolId, 0L);

        // 首次初始化或拒绝次数增加时触发
        if (currentRejectCount > lastRejectCount) {
            sendAlarmMessage("Reject", holder);
            // 更新最后记录值
            lastRejectCountMap.put(threadPoolId, currentRejectCount);
        }
    }

    private void sendAlarmMessage(String alarmType, ThreadPoolExecutorHolder holder) {
        ThreadPoolExecutorProperties properties = holder.getExecutorProperties();
        String threadPoolId = holder.getThreadPoolId();

        ThreadPoolAlarmNotifyDTO alarm = ThreadPoolAlarmNotifyDTO.builder()
                .alarmType(alarmType)
                .threadPoolId(threadPoolId)
                .interval(properties.getNotify().getInterval())
                .build();

        alarm.setSupplier(() -> {
            try {
                alarm.setIdentify(InetAddress.getLocalHost().getHostAddress());
            } catch (UnknownHostException e) {
                log.warn("Error in obtaining HostAddress", e);
            }

            ThreadPoolExecutor executor = holder.getExecutor();
            BlockingQueue<?> queue = executor.getQueue();

            int size = queue.size();
            int remaining = queue.remainingCapacity();
            long rejectCount = (executor instanceof OneThreadExecutor)
                    ? ((OneThreadExecutor) executor).getRejectCount().get()
                    : -1L;

            alarm.setCorePoolSize(executor.getCorePoolSize())
                    .setMaximumPoolSize(executor.getMaximumPoolSize())
                    .setActivePoolSize(executor.getActiveCount())  // API 有锁，避免高频率调用
                    .setCurrentPoolSize(executor.getPoolSize()) // API 有锁，避免高频率调用
                    .setCompletedTaskCount(executor.getCompletedTaskCount()) // API 有锁，避免高频率调用
                    .setLargestPoolSize(executor.getLargestPoolSize()) // API 有锁，避免高频率调用
                    .setWorkQueueName(queue.getClass().getSimpleName())
                    .setWorkQueueSize(size)
                    .setWorkQueueRemainingCapacity(remaining)
                    .setWorkQueueCapacity(size + remaining)
                    .setRejectedHandlerName(executor.getRejectedExecutionHandler().toString())
                    .setRejectCount(rejectCount)
                    .setCurrentTime(DateUtil.now())
                    .setApplicationName(ApplicationProperties.getApplicationName())
                    .setActiveProfile(ApplicationProperties.getActiveProfile())
                    .setReceives(properties.getNotify().getReceives());
            return alarm;
        });

        notifierDispatcher.sendAlarmMessage(alarm);
    }
}
