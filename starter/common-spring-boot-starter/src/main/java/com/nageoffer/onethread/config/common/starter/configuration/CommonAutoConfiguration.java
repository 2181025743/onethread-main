package com.nageoffer.onethread.config.common.starter.configuration;

import com.nageoffer.onethread.config.common.starter.refresher.DynamicThreadPoolRefreshListener;
import com.nageoffer.onethread.core.config.BootstrapConfigProperties;
import com.nageoffer.onethread.core.notification.service.NotifierDispatcher;
import com.nageoffer.onethread.spring.base.configuration.OneThreadBaseConfiguration;
import com.nageoffer.onethread.spring.base.enable.MarkerConfiguration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

/**
 * 基于配置中心的公共自动装配配置
 * <p>
 * 作者：杨潇
 * 开发时间：2025-04-28
 */
@ConditionalOnBean(MarkerConfiguration.Marker.class)
@Import(OneThreadBaseConfiguration.class)
@AutoConfigureAfter(OneThreadBaseConfiguration.class)
@ConditionalOnProperty(prefix = BootstrapConfigProperties.PREFIX, value = "enable", matchIfMissing = true, havingValue = "true")
public class CommonAutoConfiguration {

    @Bean
    public BootstrapConfigProperties bootstrapConfigProperties(Environment environment) {
        BootstrapConfigProperties bootstrapConfigProperties = Binder.get(environment)
                .bind(BootstrapConfigProperties.PREFIX, Bindable.of(BootstrapConfigProperties.class))
                .get();
        BootstrapConfigProperties.setInstance(bootstrapConfigProperties);
        return bootstrapConfigProperties;
    }

    @Bean
    public DynamicThreadPoolRefreshListener dynamicThreadPoolRefreshListener(NotifierDispatcher notifierDispatcher) {
        return new DynamicThreadPoolRefreshListener(notifierDispatcher);
    }

    @Bean
    public OneThreadBannerHandler oneThreadBannerHandler(ObjectProvider<BuildProperties> buildProperties) {
        return new OneThreadBannerHandler(buildProperties.getIfAvailable());
    }
}
