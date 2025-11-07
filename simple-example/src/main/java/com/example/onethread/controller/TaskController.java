package com.example.onethread.controller;

import com.example.onethread.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 任务测试 Controller
 * 
 * 提供简单的接口来测试动态线程池功能
 */
@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /**
     * 提交任务
     * 
     * @param count 任务数量
     * @return 提示信息
     */
    @GetMapping("/submit")
    public String submitTasks(@RequestParam(defaultValue = "10") int count) {
        if (count <= 0 || count > 1000) {
            return "❌ 任务数量必须在 1-1000 之间";
        }
        
        taskService.submitBusinessTask(count);
        return String.format("✅ 成功提交 %d 个任务到线程池！\n\n访问 /task/status 查看线程池状态", count);
    }

    /**
     * 查看线程池状态
     * 
     * @return 线程池详细状态
     */
    @GetMapping("/status")
    public String getStatus() {
        return taskService.getThreadPoolStatus();
    }

    /**
     * 重置计数器
     * 
     * @return 提示信息
     */
    @PostMapping("/reset")
    public String reset() {
        taskService.resetCounters();
        return "✅ 计数器已重置";
    }

    /**
     * 首页欢迎信息
     */
    @GetMapping("/")
    public String home() {
        return """
            ========================================
            🎉 欢迎使用 oneThread 动态线程池示例
            ========================================
            
            📌 测试接口：
            
            1. 提交任务
               GET /task/submit?count=10
               参数：count - 任务数量（1-1000）
            
            2. 查看线程池状态
               GET /task/status
            
            3. 重置计数器
               POST /task/reset
            
            ========================================
            💡 使用说明：
            
            1. 提交任务后，观察控制台日志输出
            2. 访问 /task/status 查看线程池实时状态
            3. 通过 Nacos 修改配置，观察参数动态生效
            4. 启动控制台查看可视化监控数据
            
            ========================================
            🔧 动态调整线程池参数：
            
            方式 1：通过 Nacos 配置中心
              - 修改配置文件中的 executors 配置
              - 配置会自动推送并生效
            
            方式 2：通过前端控制台
              - 启动 dashboard-dev (端口 9999)
              - 启动前端控制台 (端口 5777)
              - 登录后在界面上调整参数
            
            ========================================
            """;
    }
}

