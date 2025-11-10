package com.nageoffer.onethread.core.parser;

import cn.hutool.core.collection.CollectionUtil;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Properties 类型配置文件解析器
 * <p>
 * 作者：杨潇
 * 开发时间：2025-04-24
 */
public class PropertiesConfigParser extends AbstractConfigParser {

    @Override
    public Map<Object, Object> doParse(String content) throws IOException {
        Properties properties = new Properties();
        properties.load(new StringReader(content));
        return properties;
    }

    @Override
    public List<ConfigFileTypeEnum> getConfigFileTypes() {
        return CollectionUtil.newArrayList(ConfigFileTypeEnum.PROPERTIES);
    }
}
