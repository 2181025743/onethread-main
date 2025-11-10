package com.nageoffer.onethread.core.executor;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 增强的动态、报警和受监控的线程池 oneThread
 * <p>
 * 作者：杨潇
 * 开发时间：2025-04-20
 * <p>
 * 这个类扩展了标准的 ThreadPoolExecutor，提供了以下增强功能：
 * 1. 线程池唯一标识(threadPoolId) - 用于动态变更参数等操作
 * 2. 拒绝策略执行计数(rejectCount) - 监控线程池拒绝任务的情况
 * 3. 优雅关闭机制 - 支持设置等待终止时间
 * 4. 增强的拒绝策略处理 - 通过代理包装原始拒绝策略，统计拒绝次数
 */
@Slf4j
public class OneThreadExecutor extends ThreadPoolExecutor {

    /**
     * 线程池唯一标识，用来动态变更参数等
     * 通过这个ID可以唯一识别和管理线程池实例
     */
    @Getter
    private final String threadPoolId;

    /**
     * 线程池拒绝策略执行次数
     * 使用原子计数器来统计被拒绝执行的任务数量
     * 这对于监控线程池健康状况非常有用
     */
    @Getter
    private final AtomicLong rejectCount = new AtomicLong();

    /**
     * 等待终止时间，单位毫秒
     * 在关闭线程池时，等待现有任务完成的最大时间
     */
    private long awaitTerminationMillis;

    /**
     * 创建一个新的可扩展线程池执行器，带有指定的初始参数
     *
     * @param threadPoolId           线程池唯一标识
     * @param corePoolSize           核心线程数，即使空闲也会保持的线程数量（除非设置了allowCoreThreadTimeOut）
     * @param maximumPoolSize        最大线程数，线程池中允许的最大线程数量
     * @param keepAliveTime          空闲线程存活时间，当线程数量超过核心线程数时，
     *                               多余的空闲线程在终止前等待新任务的最长时间
     * @param unit                   keepAliveTime参数的时间单位
     * @param workQueue              工作队列，在执行任务前用于保存任务的队列，
     *                               仅保存通过execute方法提交的Runnable任务
     * @param threadFactory          线程工厂，用于创建新线程
     * @param handler                拒绝策略，当线程边界和队列容量达到上限时，
     *                               用于处理被阻止执行的任务
     * @param awaitTerminationMillis 等待终止时间，关闭线程池时等待的最长时间（毫秒）
     * @throws IllegalArgumentException 如果以下条件之一成立：<br>
     *                                  {@code corePoolSize < 0}<br>
     *                                  {@code keepAliveTime < 0}<br>
     *                                  {@code maximumPoolSize <= 0}<br>
     *                                  {@code maximumPoolSize < corePoolSize}
     * @throws NullPointerException     如果 {@code workQueue} 或 {@code unit}
     *                                  或 {@code threadFactory} 或 {@code handler} 为null
     */
    public OneThreadExecutor(
            @NonNull String threadPoolId,
            int corePoolSize,
            int maximumPoolSize,
            long keepAliveTime,
            @NonNull TimeUnit unit,
            @NonNull BlockingQueue<Runnable> workQueue,
            @NonNull ThreadFactory threadFactory,
            @NonNull RejectedExecutionHandler handler,
            long awaitTerminationMillis) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);

        // 通过动态代理设置拒绝策略执行次数
        setRejectedExecutionHandler(handler);

        // 设置动态线程池扩展属性：线程池 ID 标识
        this.threadPoolId = threadPoolId;

        // 设置等待终止时间，单位毫秒
        this.awaitTerminationMillis = awaitTerminationMillis;
    }

    /**
     * 重写拒绝策略设置方法，增强原始拒绝策略
     * 当前采用轻量级的 Lambda 静态代理方式实现增强，同时也支持使用基于 JDK 动态代理机制的拒绝策略替换方案
     * <pre>
     * RejectedExecutionHandler rejectedProxy = (RejectedExecutionHandler) Proxy
     *         .newProxyInstance(
     *                 handler.getClass().getClassLoader(),
     *                 new Class[]{RejectedExecutionHandler.class},
     *                 new RejectedProxyInvocationHandler(handler, rejectCount)
     *         );
     * </pre>
     * 
     * 增强功能：
     * 1. 在执行原始拒绝策略前，增加拒绝计数
     * 2. 保持原始拒绝策略的行为不变
     */
    @Override
    public void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
        // 创建拒绝策略的包装器，用于统计拒绝次数
        RejectedExecutionHandler handlerWrapper = new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                // 增加拒绝计数
                rejectCount.incrementAndGet();
                // 执行原始拒绝策略
                handler.rejectedExecution(r, executor);
            }

            @Override
            public String toString() {
                return handler.getClass().getSimpleName();
            }
        };

        super.setRejectedExecutionHandler(handlerWrapper);
    }

    /**
     * 重写线程池关闭方法，实现优雅关闭
     * 在关闭线程池时，等待现有任务完成指定的时间
     * 
     * 关闭流程：
     * 1. 检查线程池是否已经关闭，如果已关闭则直接返回
     * 2. 调用父类的shutdown方法，不再接受新任务
     * 3. 如果设置了等待终止时间，则等待现有任务完成
     * 4. 记录关闭过程中的日志信息
     */
    @Override
    public void shutdown() {
        // 如果线程池已经关闭，则直接返回
        if (isShutdown()) {
            return;
        }

        // 调用父类的shutdown方法，停止接受新任务
        super.shutdown();
        
        // 如果未设置等待终止时间，则直接返回
        if (this.awaitTerminationMillis <= 0) {
            return;
        }

        log.info("开始关闭线程池执行器 {}", threadPoolId);
        try {
            // 等待线程池终止，最多等待awaitTerminationMillis毫秒
            boolean isTerminated = this.awaitTermination(this.awaitTerminationMillis, TimeUnit.MILLISECONDS);
            if (!isTerminated) {
                // 超时未终止，记录警告日志
                log.warn("等待线程池 {} 终止超时", threadPoolId);
            } else {
                // 成功终止，记录信息日志
                log.info("线程池执行器 {} 已成功关闭", threadPoolId);
            }
        } catch (InterruptedException ex) {
            // 等待过程中被中断，记录警告日志并恢复中断状态
            log.warn("等待线程池 {} 终止时被中断", threadPoolId);
            Thread.currentThread().interrupt();
        }
    }
}