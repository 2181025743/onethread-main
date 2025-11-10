package com.nageoffer.onethread.nacos.cloud.example.configuration;

import com.nageoffer.onethread.core.executor.support.BlockingQueueTypeEnum;
import com.nageoffer.onethread.core.toolkit.ThreadPoolExecutorBuilder;
import com.nageoffer.onethread.spring.base.DynamicThreadPool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 动态线程池配置
 * <p>
 * 作者：杨潇
 * 开发时间：2025-04-24
 */
@Configuration
public class DynamicThreadPoolConfiguration {

    @Bean
    @DynamicThreadPool
    public ThreadPoolExecutor onethreadProducer() {
        return ThreadPoolExecutorBuilder.builder()
                .threadPoolId("onethread-producer")
                .corePoolSize(2)
                .maximumPoolSize(4)
                .keepAliveTime(9999L)
                .awaitTerminationMillis(5000L)
                .workQueueType(BlockingQueueTypeEnum.SYNCHRONOUS_QUEUE)
                .threadFactory("onethread-producer_")
                .rejectedHandler(new ThreadPoolExecutor.CallerRunsPolicy())
                .dynamicPool()
                .build();
    }

    @Bean
    @DynamicThreadPool
    public ThreadPoolExecutor onethreadConsumer() {
        return ThreadPoolExecutorBuilder.builder()
                .threadPoolId("onethread-consumer")
                .corePoolSize(4)
                .maximumPoolSize(6)
                .keepAliveTime(9999L)
                .workQueueType(BlockingQueueTypeEnum.SYNCHRONOUS_QUEUE)
                .threadFactory("onethread-consumer_")
                .rejectedHandler(new ThreadPoolExecutor.CallerRunsPolicy())
                .dynamicPool()
                .build();
    }
}
