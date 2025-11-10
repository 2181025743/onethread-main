package com.nageoffer.onethread.dashboard.dev.server.controller;

import com.nageoffer.onethread.dashboard.dev.server.common.Result;
import com.nageoffer.onethread.dashboard.dev.server.common.Results;
import com.nageoffer.onethread.dashboard.dev.server.dto.ProjectInfoRespDTO;
import com.nageoffer.onethread.dashboard.dev.server.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 项目控制器
 * <p>
 * 作者：杨潇
 * 开发时间：2025-05-18
 */
@RestController
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /**
     * 查看包含动态线程池项目列表
     */
    @GetMapping("/api/onethread-dashboard/projects")
    public Result<List<ProjectInfoRespDTO>> listProjects() {
        return Results.success(projectService.listProject());
    }
}
