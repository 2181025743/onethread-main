package com.nageoffer.onethread.dashboard.dev.server.remote.dto;

import lombok.Data;

/**
 * Nacos 配置明细响应实体
 * <p>
 * 作者：杨潇
 * 开发时间：2025-05-18
 */
@Data
public class NacosConfigRespDTO {

    /**
     * 配置的 Data ID
     */
    private String dataId;

    /**
     * 配置所属分组，例如：DEFAULT_GROUP
     * Nacos v3 admin API 使用 groupName，这里做别名映射
     */
    @com.alibaba.fastjson2.annotation.JSONField(alternateNames = {"groupName"})
    private String group;

    /**
     * 应用名
     */
    private String appName;

    /**
     * 配置中心内容（注意：列表接口通常不返回，需要再调用明细接口获取）
     */
    private String content;
}
