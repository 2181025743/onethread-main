package com.nageoffer.onethread.spring.base.enable;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 标记配置类
 * 用于在 Spring 容器中注入标记对象，作为是否启用动态线程池的条件判断依据
 * <p>
 * 作者：杨潇
 * 开发时间：2025-04-23
 */
@Configuration
public class MarkerConfiguration {

    @Bean
    public Marker dynamicThreadPoolMarkerBean() {
        return new Marker();
    }

    /**
     * 标记类
     * 可用于条件装配（@ConditionalOnBean 等）中作为存在性的判断依据
     * <p>
     * 作者：杨潇
     * 开发时间：2025-04-23
     */
    public class Marker {

    }
}

