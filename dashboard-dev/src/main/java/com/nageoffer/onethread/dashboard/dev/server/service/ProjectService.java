package com.nageoffer.onethread.dashboard.dev.server.service;

import com.nageoffer.onethread.dashboard.dev.server.dto.ProjectInfoRespDTO;

import java.util.List;

/**
 * 项目接口层
 * <p>
 * 作者：杨潇
 * 开发时间：2025-05-18
 */
public interface ProjectService {

    /**
     * 查询项目集合
     *
     * @return 项目集合数据
     */
    List<ProjectInfoRespDTO> listProject();
}
