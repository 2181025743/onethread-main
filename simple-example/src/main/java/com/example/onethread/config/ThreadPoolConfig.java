package com.example.onethread.config;

import com.nageoffer.onethread.core.toolkit.ThreadPoolExecutorBuilder;
import com.nageoffer.onethread.spring.base.DynamicThreadPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置类
 * 
 * 使用 @DynamicThreadPool 注解标注的线程池会被 oneThread 接管，
 * 支持通过配置中心或控制台动态调整参数
 */
@Slf4j
@Configuration
public class ThreadPoolConfig {

    /**
     * 业务处理线程池
     * 
     * threadPoolId 必须与 Nacos 配置文件中的 thread-pool-id 完全匹配
     */
    @Bean
    @DynamicThreadPool  // 👈 关键注解：标记这是一个动态线程池
    public ThreadPoolExecutor businessThreadPool() {
        log.info("正在创建业务线程池：business-thread-pool");
        
        return ThreadPoolExecutorBuilder.builder()
                .threadPoolId("business-thread-pool")  // 线程池唯一标识
                .threadFactory("business-pool")        // 线程名称前缀
                .corePoolSize(5)                       // 核心线程数
                .maximumPoolSize(10)                   // 最大线程数
                .workQueueCapacity(100)                // 队列容量
                .keepAliveTime(60L)                    // 空闲线程存活时间（秒）
                .rejectedHandler(new ThreadPoolExecutor.CallerRunsPolicy())  // 拒绝策略
                .dynamicPool()                         // 标记为动态线程池，支持运行时参数调整
                .build();
    }

    /**
     * 异步通知线程池（示例：用于发送邮件、短信等）
     */
    @Bean
    @DynamicThreadPool
    public ThreadPoolExecutor notifyThreadPool() {
        log.info("正在创建通知线程池：notify-thread-pool");

        return ThreadPoolExecutorBuilder.builder()
                .threadPoolId("notify-thread-pool")
                .threadFactory("notify-pool")
                .corePoolSize(2)
                .maximumPoolSize(5)
                .workQueueCapacity(50)
                .keepAliveTime(60L)
                .rejectedHandler(new ThreadPoolExecutor.AbortPolicy())
                .dynamicPool()                         // 标记为动态线程池，支持运行时参数调整
                .build();
    }
}

