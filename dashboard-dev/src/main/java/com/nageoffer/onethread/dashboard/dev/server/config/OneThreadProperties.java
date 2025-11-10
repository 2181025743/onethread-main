package com.nageoffer.onethread.dashboard.dev.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * oneThread DashBoard 配置文件
 * <p>
 * 作者：杨潇
 * 开发时间：2025-05-17
 */
@Data
@Component
@ConfigurationProperties(prefix = "onethread")
public class OneThreadProperties {

    /**
     * 用户集合
     */
    private List<String> users;

    /**
     * Nacos 命名空间
     */
    private List<String> namespaces;

    /**
     * 默认查询的 namespace
     */
    private String defaultNamespace = "onethread-dev";

    /**
     * 快速模式（只查询默认 namespace）
     */
    private Boolean quickMode = true;
}
