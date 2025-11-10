package com.nageoffer.onethread.spring.base.configuration;

import com.nageoffer.onethread.core.alarm.ThreadPoolAlarmChecker;
import com.nageoffer.onethread.core.config.BootstrapConfigProperties;
import com.nageoffer.onethread.core.monitor.ThreadPoolMonitor;
import com.nageoffer.onethread.core.notification.service.NotifierDispatcher;
import com.nageoffer.onethread.spring.base.support.ApplicationContextHolder;
import com.nageoffer.onethread.spring.base.support.OneThreadBeanPostProcessor;
import com.nageoffer.onethread.spring.base.support.SpringPropertiesLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * 动态线程池基础 Spring 配置类
 * <p>
 * 作者：杨潇
 * 开发时间：2025-04-23
 */
@Configuration
public class OneThreadBaseConfiguration {

    @Bean
    public ApplicationContextHolder applicationContextHolder() {
        return new ApplicationContextHolder();
    }

    @Bean
    @DependsOn("applicationContextHolder")
    public OneThreadBeanPostProcessor oneThreadBeanPostProcessor(BootstrapConfigProperties properties) {
        return new OneThreadBeanPostProcessor(properties);
    }

    @Bean
    public NotifierDispatcher notifierDispatcher() {
        return new NotifierDispatcher();
    }

    @Bean
    public SpringPropertiesLoader springPropertiesLoader() {
        return new SpringPropertiesLoader();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public ThreadPoolAlarmChecker threadPoolAlarmChecker(NotifierDispatcher notifierDispatcher) {
        return new ThreadPoolAlarmChecker(notifierDispatcher);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public ThreadPoolMonitor threadPoolMonitor() {
        return new ThreadPoolMonitor();
    }
}
