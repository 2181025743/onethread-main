package com.nageoffer.onethread.dashboard.dev.server.remote.dto;

import cn.hutool.core.collection.CollUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Nacos 服务集合响应实体
 * <p>
 * 作者：杨潇
 * 开发时间：2025-05-18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NacosServiceListRespDTO {

    /**
     * 服务实例数量
     */
    private Integer count;

    /**
     * 服务实例明细
     */
    private List<NacosServiceRespDTO> list;

    /**
     * 服务实例明细
     */
    private List<NacosServiceRespDTO> serviceList;

    /**
     * 适配 Nacos 跨版本之间参数值变更
     */
    public List<NacosServiceRespDTO> getServiceList() {
        return CollUtil.isEmpty(serviceList) ? list : serviceList;
    }
}
