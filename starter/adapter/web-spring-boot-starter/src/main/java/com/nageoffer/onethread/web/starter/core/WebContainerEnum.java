package com.nageoffer.onethread.web.starter.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Web 容器类型枚举
 * <p>
 * 作者：杨潇
 * 开发时间：2025-05-12
 */
@RequiredArgsConstructor
public enum WebContainerEnum {

    /**
     * Tomcat
     */
    TOMCAT("Tomcat"),

    /**
     * Jetty
     */
    JETTY("Jetty"),

    /**
     * Undertow
     */
    UNDERTOW("Undertow");

    @Getter
    private final String name;

    @Override
    public String toString() {
        return getName();
    }
}
