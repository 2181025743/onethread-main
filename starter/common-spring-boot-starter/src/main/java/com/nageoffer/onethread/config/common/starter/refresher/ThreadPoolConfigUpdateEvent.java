package com.nageoffer.onethread.config.common.starter.refresher;

import com.nageoffer.onethread.core.config.BootstrapConfigProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * 配置中心刷新线程池参数变更事件
 * <p>
 * 作者：杨潇
 * 开发时间：2025-05-12
 */
public class ThreadPoolConfigUpdateEvent extends ApplicationEvent {

    @Getter
    @Setter
    private BootstrapConfigProperties bootstrapConfigProperties;

    public ThreadPoolConfigUpdateEvent(Object source, BootstrapConfigProperties bootstrapConfigProperties) {
        super(source);
        this.bootstrapConfigProperties = bootstrapConfigProperties;
    }
}
