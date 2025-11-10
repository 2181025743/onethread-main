package com.nageoffer.onethread.dashboard.dev.server.service.handler;

import lombok.Getter;

/**
 * 配置解析器接口抽象
 * <p>
 * 作者：杨潇
 * 开发时间：2025-04-23
 */
@Getter
public enum ConfigFileTypeEnum {

    /**
     * PROPERTIES
     */
    PROPERTIES("properties"),

    /**
     * YML
     */
    YML("yml"),

    /**
     * YAML
     */
    YAML("yaml");

    private final String value;

    ConfigFileTypeEnum(String value) {
        this.value = value;
    }

    public static ConfigFileTypeEnum of(String value) {
        for (ConfigFileTypeEnum typeEnum : ConfigFileTypeEnum.values()) {
            if (typeEnum.value.equals(value)) {
                return typeEnum;
            }
        }
        return PROPERTIES;
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
