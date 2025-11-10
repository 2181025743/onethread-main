package com.nageoffer.onethread.dashboard.dev.server.remote.dto;

import lombok.Data;

import java.util.List;

/**
 * Nacos 配置集合响应实体
 * <p>
 * 作者：杨潇
 * 开发时间：2025-05-18
 */
@Data
public class NacosConfigListRespDTO {

    /**
     * 总配置项数量
     */
    private Integer totalCount;

    /**
     * 当前页码（从1开始）
     */
    private Integer pageNumber;

    /**
     * 总页数
     */
    private Integer pagesAvailable;

    /**
     * 当前页的配置项列表
     */
    private List<NacosConfigRespDTO> pageItems;
}
