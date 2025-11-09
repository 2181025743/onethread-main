package com.nageoffer.onethread.core.toolkit;

import cn.hutool.core.lang.Assert;
import com.nageoffer.onethread.core.executor.OneThreadExecutor;
import com.nageoffer.onethread.core.executor.support.BlockingQueueTypeEnum;
import lombok.Getter;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 动态线程池构建器
 * <p>
 * 作者：杨潇
 * 开发时间：2025-04-20
 */
@Getter
public class ThreadPoolExecutorBuilder {

    /**
     * 线程池唯一标识
     */
    private String threadPoolId;

    /**
     * 核心线程数
     */
    private Integer corePoolSize = Runtime.getRuntime().availableProcessors();

    /**
     * 最大线程数
     */
    private Integer maximumPoolSize = corePoolSize + (corePoolSize >> 1);

    /**
     * 阻塞队列类型
     */
    private BlockingQueueTypeEnum workQueueType = BlockingQueueTypeEnum.LINKED_BLOCKING_QUEUE;

    /**
     * 队列容量
     */
    private Integer workQueueCapacity = 4096;

    /**
     * 拒绝策略
     */
    private RejectedExecutionHandler rejectedHandler = new ThreadPoolExecutor.AbortPolicy();

    /**
     * 线程工厂
     */
    private ThreadFactory threadFactory;

    /**
     * 线程空闲存活时间（单位：秒）
     */
    private Long keepAliveTime = 30000L;

    /**
     * 是否允许核心线程超时
     */
    private boolean allowCoreThreadTimeOut = false;

    /**
     * 动态线程池标识
     */
    private boolean dynamicPool = false;

    /**
     * 最大等待时间
     */
    private long awaitTerminationMillis = 0L;

    /**
     * 设置构建线程池为动态线程池
     */
    public ThreadPoolExecutorBuilder dynamicPool() {
        this.dynamicPool = true;
        return this;
    }

    /**
     * 设置线程池唯一标识
     *
     * @param threadPoolId 线程池唯一标识
     */
    public ThreadPoolExecutorBuilder threadPoolId(String threadPoolId) {
        this.threadPoolId = threadPoolId;
        return this;
    }

    /**
     * 设置核心线程数
     *
     * @param corePoolSize 核心线程数
     */
    public ThreadPoolExecutorBuilder corePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
        return this;
    }

    /**
     * 设置最大线程数
     *
     * @param maximumPoolSize 最大线程数
     */
    public ThreadPoolExecutorBuilder maximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
        return this;
    }

    /**
     * 设置阻塞队列容量
     *
     * @param workQueueCapacity 阻塞队列容量
     */
    public ThreadPoolExecutorBuilder workQueueCapacity(int workQueueCapacity) {
        this.workQueueCapacity = workQueueCapacity;
        return this;
    }

    /**
     * 设置阻塞队列类型
     *
     * @param workQueueType 阻塞队列类型（如 LinkedBlockingQueue、ArrayBlockingQueue）
     */
    public ThreadPoolExecutorBuilder workQueueType(BlockingQueueTypeEnum workQueueType) {
        this.workQueueType = workQueueType;
        return this;
    }

    /**
     * 设置线程工厂
     *
     * @param namePrefix 线程名前缀，如 "onethread-"，线程名形如：onethread-1
     */
    public ThreadPoolExecutorBuilder threadFactory(String namePrefix) {
        this.threadFactory = ThreadFactoryBuilder.builder()
                .namePrefix(namePrefix)
                .build();
        return this;
    }

    /**
     * 快速设置线程工厂，封装常用参数以降低构建门槛
     * <p>
     * 出于实用主义，仅暴露常用的 namePrefix 和 daemon 参数
     * 若你对线程优先级、异常处理器等有更细颗粒度需求，可选择扩展重载构造链
     *
     * @param namePrefix 线程名前缀（e.g. "onethread-"），最终线程名为：onethread-1、onethread-2...
     * @param daemon     是否为守护线程（true 表示不会阻止 JVM 退出）
     */
    public ThreadPoolExecutorBuilder threadFactory(String namePrefix, Boolean daemon) {
        this.threadFactory = ThreadFactoryBuilder.builder()
                .namePrefix(namePrefix)
                .daemon(daemon)
                .build();
        return this;
    }

    /**
     * 设置线程工厂
     *
     * @param threadFactory 线程工厂
     */
    public ThreadPoolExecutorBuilder threadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        return this;
    }

    /**
     * 设置拒绝策略
     *
     * @param rejectedHandler 拒绝策略（如 AbortPolicy、CallerRunsPolicy）
     */
    public ThreadPoolExecutorBuilder rejectedHandler(RejectedExecutionHandler rejectedHandler) {
        this.rejectedHandler = rejectedHandler;
        return this;
    }

    /**
     * 设置线程空闲存活时间
     *
     * @param keepAliveTime 存活时间（单位：秒）
     */
    public ThreadPoolExecutorBuilder keepAliveTime(long keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
        return this;
    }

    /**
     * 设置是否允许核心线程超时
     *
     * @param allowCoreThreadTimeOut 是否允许核心线程超时
     */
    public ThreadPoolExecutorBuilder allowCoreThreadTimeOut(boolean allowCoreThreadTimeOut) {
        this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
        return this;
    }

    /**
     * 设置最大等待时间
     *
     * @param awaitTerminationMillis 最大等待时间
     */
    public ThreadPoolExecutorBuilder awaitTerminationMillis(long awaitTerminationMillis) {
        this.awaitTerminationMillis = awaitTerminationMillis;
        return this;
    }

    /**
     * 创建线程池构建器
     */
    public static ThreadPoolExecutorBuilder builder() {
        return new ThreadPoolExecutorBuilder();
    }

    /**
     * 构建线程池实例
     * 
     * @return ThreadPoolExecutor 线程池实例，根据配置决定是普通线程池还是动态线程池
     */
    public ThreadPoolExecutor build() {
        // 创建指定类型和容量的阻塞队列
        BlockingQueue<Runnable> blockingQueue = BlockingQueueTypeEnum.createBlockingQueue(workQueueType.getName(), workQueueCapacity);
        // 设置拒绝策略，如果未指定则使用默认的AbortPolicy

        // 使用Optional.ofNullable来安全处理可能为null的拒绝策略
        // ofNullable方法可以接受null值，如果this.rejectedHandler为null，则返回空的Optional
        // 然后通过orElseGet提供默认的拒绝策略ThreadPoolExecutor.AbortPolicy
        RejectedExecutionHandler rejectedHandler = Optional.ofNullable(this.rejectedHandler)
                .orElseGet(ThreadPoolExecutor.AbortPolicy::new);

        // 验证线程工厂不能为空
        Assert.notNull(threadFactory, "The thread factory cannot be null.");

        ThreadPoolExecutor threadPoolExecutor;
        // 根据是否为动态线程池创建不同类型的线程池实例
        if (dynamicPool) {
            threadPoolExecutor = new OneThreadExecutor(
                    threadPoolId,
                    corePoolSize,
                    maximumPoolSize,
                    keepAliveTime,
                    TimeUnit.SECONDS,
                    blockingQueue,
                    threadFactory,
                    rejectedHandler,
                    awaitTerminationMillis
            );
        } else {
            threadPoolExecutor = new ThreadPoolExecutor(
                    corePoolSize,
                    maximumPoolSize,
                    keepAliveTime,
                    TimeUnit.SECONDS,
                    blockingQueue,
                    threadFactory,
                    rejectedHandler
            );
        }

        // 设置核心线程是否允许超时
        threadPoolExecutor.allowCoreThreadTimeOut(allowCoreThreadTimeOut);
        return threadPoolExecutor;
    }
}
