package com.nageoffer.onethread.dashboard.dev.server.service;

import com.nageoffer.onethread.dashboard.dev.server.dto.ThreadPoolDetailRespDTO;
import com.nageoffer.onethread.dashboard.dev.server.dto.ThreadPoolListReqDTO;
import com.nageoffer.onethread.dashboard.dev.server.dto.ThreadPoolUpdateReqDTO;

import java.util.List;

/**
 * 线程池管理接口层
 * <p>
 * 作者：杨潇
 * 开发时间：2025-05-19
 */
public interface ThreadPoolManagerService {

    /**
     * 查询线程池集合
     *
     * @param requestParam 请求参数
     * @return 线程池集合
     */
    List<ThreadPoolDetailRespDTO> listThreadPool(ThreadPoolListReqDTO requestParam);

    /**
     * 全局修改线程池参数
     *
     * @param requestParam 请求参数
     */
    void updateGlobalThreadPool(ThreadPoolUpdateReqDTO requestParam);
}
