package com.example.onethread.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 业务任务服务
 * 
 * 模拟实际业务中使用线程池处理任务的场景
 */
@Slf4j
@Service
public class TaskService {

    @Resource(name = "businessThreadPool")
    private ThreadPoolExecutor businessThreadPool;

    @Resource(name = "notifyThreadPool")
    private ThreadPoolExecutor notifyThreadPool;

    // 任务计数器
    private final AtomicInteger taskCounter = new AtomicInteger(0);
    private final AtomicInteger completedCounter = new AtomicInteger(0);

    /**
     * 提交业务任务
     */
    public void submitBusinessTask(int taskCount) {
        log.info("开始提交 {} 个业务任务", taskCount);
        
        for (int i = 0; i < taskCount; i++) {
            int taskId = taskCounter.incrementAndGet();
            
            businessThreadPool.execute(() -> {
                try {
                    log.info("任务 {} 开始执行，当前线程：{}", taskId, Thread.currentThread().getName());
                    
                    // 模拟业务处理（耗时 2-5 秒）
                    Thread.sleep(2000 + (long) (Math.random() * 3000));
                    
                    completedCounter.incrementAndGet();
                    log.info("任务 {} 执行完成", taskId);
                    
                    // 任务完成后发送通知
                    sendNotification(taskId);
                    
                } catch (InterruptedException e) {
                    log.error("任务 {} 被中断", taskId, e);
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        log.info("已提交 {} 个任务到线程池", taskCount);
    }

    /**
     * 发送通知（使用独立的通知线程池）
     */
    private void sendNotification(int taskId) {
        notifyThreadPool.execute(() -> {
            try {
                log.info("发送任务 {} 完成通知，当前线程：{}", taskId, Thread.currentThread().getName());
                // 模拟发送通知（耗时 500ms）
                Thread.sleep(500);
            } catch (InterruptedException e) {
                log.error("通知发送失败", e);
                Thread.currentThread().interrupt();
            }
        });
    }

    /**
     * 获取线程池状态信息
     */
    public String getThreadPoolStatus() {
        return String.format("""
            
            ========== 业务线程池状态 ==========
            线程池标识：business-thread-pool
            核心线程数：%d
            最大线程数：%d
            当前线程数：%d
            活跃线程数：%d
            队列容量：%d
            当前队列任务数：%d
            已完成任务数：%d
            总提交任务数：%d
            
            ========== 通知线程池状态 ==========
            线程池标识：notify-thread-pool
            核心线程数：%d
            最大线程数：%d
            当前线程数：%d
            活跃线程数：%d
            队列容量：%d
            当前队列任务数：%d
            已完成任务数：%d
            
            ========== 任务统计 ==========
            已提交任务数：%d
            已完成任务数：%d
            ====================================
            """,
                // 业务线程池
                businessThreadPool.getCorePoolSize(),
                businessThreadPool.getMaximumPoolSize(),
                businessThreadPool.getPoolSize(),
                businessThreadPool.getActiveCount(),
                businessThreadPool.getQueue().size() + businessThreadPool.getQueue().remainingCapacity(),
                businessThreadPool.getQueue().size(),
                businessThreadPool.getCompletedTaskCount(),
                taskCounter.get(),
                // 通知线程池
                notifyThreadPool.getCorePoolSize(),
                notifyThreadPool.getMaximumPoolSize(),
                notifyThreadPool.getPoolSize(),
                notifyThreadPool.getActiveCount(),
                notifyThreadPool.getQueue().size() + notifyThreadPool.getQueue().remainingCapacity(),
                notifyThreadPool.getQueue().size(),
                notifyThreadPool.getCompletedTaskCount(),
                // 统计
                taskCounter.get(),
                completedCounter.get()
        );
    }

    /**
     * 重置计数器
     */
    public void resetCounters() {
        taskCounter.set(0);
        completedCounter.set(0);
        log.info("计数器已重置");
    }
}