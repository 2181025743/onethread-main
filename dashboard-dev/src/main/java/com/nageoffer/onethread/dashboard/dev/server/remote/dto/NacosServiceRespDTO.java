package com.nageoffer.onethread.dashboard.dev.server.remote.dto;

import lombok.Data;

/**
 * Nacos 服务明细实体
 * <p>
 * 作者：杨潇
 * 开发时间：2025-05-18
 */
@Data
public class NacosServiceRespDTO {

    /**
     * IP
     */
    private String ip;

    /**
     * Port
     */
    private Integer port;
}
