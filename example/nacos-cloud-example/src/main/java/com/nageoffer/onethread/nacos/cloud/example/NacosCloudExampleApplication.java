package com.nageoffer.onethread.nacos.cloud.example;

import com.nageoffer.onethread.spring.base.enable.EnableOneThread;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Nacos Cloud 版本示例应用程序
 * Java 9+ 的模块系统（JPMS）默认禁止通过反射访问 JDK 内部 API 的私有字段，所以需要配置开放反射权限
 * 在启动命令中增加以下参数，显式开放 java.util.concurrent 包
 * IDE 中通过在 VM options 中添加参数：--add-opens=java.base/java.util.concurrent=ALL-UNNAMED
 * <p>
 * 项目启动成功后，修改 Nacos 配置文件中的动态线程池配置，观察控制台是否有日志打印
 * 示例日志打印：
 * [onethread-producer] Dynamic thread pool parameter changed:
 * corePoolSize: 12 => 12
 * maximumPoolSize: 24 => 24
 * capacity: 10000 => 10000
 * keepAliveTime: 19999 => 9999
 * rejectedType: CallerRunsPolicy => CallerRunsPolicy
 * allowCoreThreadTimeOut: false => false
 * <p>
 * 作者：杨潇
 * 开发时间：2025-04-24
 */
@EnableOneThread
@SpringBootApplication
public class NacosCloudExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(NacosCloudExampleApplication.class, args);
    }
}
