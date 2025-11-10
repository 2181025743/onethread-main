package com.nageoffer.onethread.dashboard.dev.server.remote.dto;

import lombok.Data;

/**
 * Nacos 配置详情响应实体
 * <p>
 * 作者：杨潇
 * 开发时间：2025-05-18
 */
@Data
public class NacosConfigDetailRespDTO {

    /**
     * 配置 ID（唯一标识）
     */
    private String id;

    /**
     * 配置的 Data ID
     */
    private String dataId;

    /**
     * 配置所属分组，例如：DEFAULT_GROUP
     * v3 admin 返回字段为 groupName
     */
    @com.alibaba.fastjson2.annotation.JSONField(alternateNames = {"groupName"})
    private String group;

    /**
     * 配置的完整内容（YAML、JSON 等）
     */
    private String content;

    /**
     * 配置内容的 MD5 值，用于校验变更
     */
    private String md5;

    /**
     * 加密数据密钥（如未启用加密则为空）
     */
    private String encryptedDataKey;

    /**
     * 租户标识（通常是命名空间）
     */
    private String tenant;

    /**
     * 应用名称，例如 onethread-framework
     */
    private String appName;

    /**
     * 配置类型（yaml、properties 等）
     */
    private String type;

    /**
     * 创建时间（时间戳，单位毫秒）
     */
    private Long createTime;

    /**
     * 修改时间（时间戳，单位毫秒）
     */
    private Long modifyTime;

    /**
     * 创建用户（若有记录）
     */
    private String createUser;

    /**
     * 创建 IP（可能是本地或远程地址）
     */
    private String createIp;

    /**
     * 配置描述（可选）
     */
    private String desc;

    /**
     * 配置用途（可选）
     */
    private String use;

    /**
     * 生效环境（可选）
     */
    private String effect;

    /**
     * 配置 schema 定义（用于 JSON/YAML 校验，通常为空）
     */
    private String schema;

    /**
     * 配置标签（用于搜索过滤，可能为 null）
     */
    private String configTags;
}
