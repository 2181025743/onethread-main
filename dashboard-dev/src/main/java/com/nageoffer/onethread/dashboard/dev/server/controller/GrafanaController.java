package com.nageoffer.onethread.dashboard.dev.server.controller;

import com.nageoffer.onethread.dashboard.dev.server.common.Result;
import com.nageoffer.onethread.dashboard.dev.server.common.Results;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Grafana 控制器
 * <p>
 * 作者：杨潇
 * 开发时间：2025-06-11
 */
@RestController
public class GrafanaController {

    @Value("${onethread.grafana.url}")
    private String grafanaUrl;

    /**
     * 控制台获取 Grafana 预览地址
     */
    @GetMapping("/api/onethread-dashboard/grafana")
    public Result<String> getGrafanaUrl() {
        return Results.success(grafanaUrl);
    }
}
