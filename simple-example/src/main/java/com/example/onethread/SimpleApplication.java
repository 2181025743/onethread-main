package com.example.onethread;

import com.nageoffer.onethread.spring.base.enable.EnableOneThread;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * oneThread 简单示例应用
 * 
 * @EnableOneThread 注解会启用动态线程池管理功能
 */
@EnableOneThread  // 👈 关键注解：启用 oneThread 动态线程池管理
@SpringBootApplication
public class SimpleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimpleApplication.class, args);
        System.out.println("""
            
            ========================================
            🎉 应用启动成功！
            ========================================
            本地访问地址：http://localhost:8080
            
            测试接口：
            1. 提交任务：http://localhost:8080/task/submit?count=10
            2. 查看状态：http://localhost:8080/task/status
            
            控制台管理：
            1. 启动 dashboard-dev (端口 9999)
            2. 启动前端控制台 (端口 5777)
            3. 登录后查看线程池监控
            ========================================
            """);
    }
}

