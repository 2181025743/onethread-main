package com.nageoffer.onethread.core.parser;

/**
 * 配置解析器抽象类
 * <p>
 * 作者：杨潇
 * 开发时间：2025-04-23
 */
public abstract class AbstractConfigParser implements ConfigParser {

    @Override
    public boolean supports(ConfigFileTypeEnum type) {
        return getConfigFileTypes().contains(type);
    }
}
