package com.nageoffer.onethread.web.starter.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Web 容器线程池动态参数变更配置类
 * <p>
 * 作者：杨潇
 * 开发时间：2025-05-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebThreadPoolConfig {

    /**
     * 核心线程数
     */
    private Integer corePoolSize;

    /**
     * 最大线程数
     */
    private Integer maximumPoolSize;

    /**
     * 空闲线程最大保活时间
     */
    private Long keepAliveTime;
}
