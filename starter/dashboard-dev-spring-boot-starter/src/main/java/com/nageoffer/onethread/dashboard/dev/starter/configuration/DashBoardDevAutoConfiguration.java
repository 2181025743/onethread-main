package com.nageoffer.onethread.dashboard.dev.starter.configuration;

import com.nageoffer.onethread.dashboard.dev.starter.controller.DynamicThreadPoolController;
import com.nageoffer.onethread.dashboard.dev.starter.controller.WebThreadPoolController;
import com.nageoffer.onethread.dashboard.dev.starter.service.DynamicThreadPoolService;
import com.nageoffer.onethread.dashboard.dev.starter.service.WebThreadPoolService;
import org.springframework.context.annotation.Bean;

/**
 * 基于配置中心的公共自动装配配置
 * <p>
 * 作者：杨潇
 * 开发时间：2025-05-15
 */
public class DashBoardDevAutoConfiguration {

    @Bean
    public DynamicThreadPoolService dynamicThreadPoolService() {
        return new DynamicThreadPoolService();
    }

    @Bean
    public DynamicThreadPoolController dynamicThreadPoolController(DynamicThreadPoolService dynamicThreadPoolService) {
        return new DynamicThreadPoolController(dynamicThreadPoolService);
    }

    @Bean
    public WebThreadPoolService webThreadPoolService(com.nageoffer.onethread.web.starter.core.executor.WebThreadPoolService webThreadPoolService) {
        return new WebThreadPoolService(webThreadPoolService);
    }

    @Bean
    public WebThreadPoolController webThreadPoolController(WebThreadPoolService webThreadPoolService) {
        return new WebThreadPoolController(webThreadPoolService);
    }
}
