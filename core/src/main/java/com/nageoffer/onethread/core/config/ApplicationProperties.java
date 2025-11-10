package com.nageoffer.onethread.core.config;

import lombok.Getter;
import lombok.Setter;

/**
 * 应用属性配置
 * <p>
 * 作者：杨潇
 * 开发时间：2025-05-05
 */
public class ApplicationProperties {

    /**
     * 应用名
     */
    @Getter
    @Setter
    private static String applicationName;

    /**
     * 环境标识
     */
    @Getter
    @Setter
    private static String activeProfile;
}
