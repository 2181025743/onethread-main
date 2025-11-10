
package com.nageoffer.onethread.dashboard.dev.server.dto;

import lombok.Data;

/**
 * Web 线程池控制台查询请求实体
 * <p>
 * 作者：杨潇
 * 开发时间：2025-05-23
 */
@Data
public class WebThreadPoolListReqDTO {

    /**
     * 命名空间
     */
    private String namespace;

    /**
     * 服务名
     */
    private String serviceName;
}
