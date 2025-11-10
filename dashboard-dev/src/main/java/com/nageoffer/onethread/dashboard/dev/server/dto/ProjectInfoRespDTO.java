
package com.nageoffer.onethread.dashboard.dev.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目接口返回实体类
 * <p>
 * 作者：杨潇
 * 开发时间：2025-05-18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectInfoRespDTO {

    /**
     * 命名空间
     */
    private String namespace;

    /**
     * 项目名 / 服务名
     */
    private String serviceName;

    /**
     * 实例数量
     */
    private Integer instanceCount;

    /**
     * 线程池数量（executors 数组的 size）
     */
    private Integer threadPoolCount;

    /**
     * 是否配置了 Web 线程池（判断是否包含 web.core-pool-size）
     */
    private Boolean hasWebThreadPool;

    /**
     * 修改时间
     */
    private String updateTime;
}
