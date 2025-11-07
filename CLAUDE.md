# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

oneThread 是一个基于配置中心的动态可观测 Java 线程池框架，实现线程池参数的在线动态调整、运行时监控与告警功能。项目包含 Java 后端组件库和 Vue 3 前端控制台。

**核心价值**：解决 JDK 原生线程池参数配置不灵活的问题，支持运行时参数热更新、状态监控与异常告警，提升系统稳定性和可运维性。

**项目特点**：生产级可用、代码质量高（1.34万行含注释）、设计模式丰富、配套可视化控制台，是企业级动态线程池管理的优秀实践。

### 技术栈
**后端：**
- Java 17
- Spring Boot 3.0.7
- Spring Cloud 2022.0.3
- Spring Cloud Alibaba 2022.0.0.0-RC2
- Hutool 5.8.37
- FastJSON2 2.0.57
- Guava 32.1.3-jre
- Nacos / Apollo 配置中心
- Prometheus + Grafana（监控指标）

**前端：**
- Vue 3 + TypeScript
- Element Plus UI
- Vite 构建工具
- pnpm 包管理器（版本 >= 9.12.0）
- Node.js >= 20.10.0

## 项目结构

```
.
├── core/                   # 核心模块：线程池基础类定义、监控告警逻辑
├── spring-base/            # Spring 基础模块：扫描动态线程池、Banner 打印
├── starter/                # 配置中心组件包
│   ├── common-spring-boot-starter/          # 公共监听逻辑抽象
│   ├── nacos-cloud-spring-boot-starter/     # Nacos 配置中心集成
│   ├── apollo-spring-boot-starter/          # Apollo 配置中心集成
│   ├── dashboard-dev-spring-boot-starter/   # 控制台 API
│   └── adapter/
│       └── web-spring-boot-starter/         # Web 容器线程池适配（Tomcat/Jetty）
├── example/                # 示例项目
│   ├── nacos-cloud-example/                 # Nacos 示例
│   └── apollo-example/                      # Apollo 示例
├── dashboard-dev/          # 独立控制台后端服务（仅用于开发环境）
└── onethread-dashboard-main/  # 前端控制台（Vue 3 + Element Plus）
```

## 本地开发快速启动

### 完整系统启动顺序
1. **启动控制台后端**（dashboard-dev，端口 9999）
2. **启动示例应用**（nacos-cloud-example，端口 18080）
3. **启动前端控制台**（onethread-dashboard-main，端口 5777）

### 默认端口
- **控制台后端**：9999
- **Nacos 示例应用**：18080
- **前端控制台**：5777

### 控制台登录凭据
默认用户（在 dashboard-dev/application.yaml 中配置）：
- 用户名：`admin` / 密码：`admin`
- 用户名：`test` / 密码：`test`

## 构建与运行命令

### Java 后端

#### 构建项目
```bash
# 构建整个项目（推荐）
mvn clean package -DskipTests

# 构建指定模块
mvn clean package -pl example/nacos-cloud-example -am

# 应用代码格式化（Spotless）
mvn spotless:apply

# 代码格式检查
mvn spotless:check

# 跳过 Spotless 检查（构建失败时使用）
mvn clean package -DskipTests -Dspotless.check.skip=true
```

#### 运行示例应用

**Nacos 示例：**
```bash
cd example/nacos-cloud-example
mvn spring-boot:run

# 或直接运行 JAR
java --add-opens=java.base/java.util.concurrent=ALL-UNNAMED \
     -jar target/nacos-cloud-example-0.0.1-SNAPSHOT.jar
```

**Apollo 示例：**
```bash
cd example/apollo-example
mvn spring-boot:run
```

**控制台后端服务：**
```bash
cd dashboard-dev
mvn spring-boot:run
```

#### 运行测试
```bash
# 运行所有测试
mvn test

# 运行指定模块测试
mvn test -pl core

# 运行指定测试类
mvn test -Dtest=ThreadPoolExecutorBuilderTest
```

#### 重要启动参数
**必须添加反射权限参数**（Java 17 环境下强制要求）：
```bash
--add-opens=java.base/java.util.concurrent=ALL-UNNAMED
```

**IDE 配置**：在 IDEA 的 Run/Debug Configurations → VM options 中添加上述参数。

**为什么必需**：oneThread 通过反射修改 ThreadPoolExecutor 私有字段实现参数热更新，Java 17 模块系统限制访问，必须显式开放权限。

### 前端控制台

#### 安装依赖
```bash
cd onethread-dashboard-main
pnpm install
```

#### 开发运行
```bash
# 运行 Element Plus 版本（默认）
pnpm dev:ele

# 或使用根目录命令
pnpm -F @vben/web-ele run dev
```
默认端口：5777

#### 构建生产版本
```bash
# 构建 Element Plus 版本
pnpm build:ele

# 或
pnpm run build --filter=@vben/web-ele
```

#### 其他命令
```bash
# 代码格式化
pnpm format

# 代码检查
pnpm lint

# 类型检查
pnpm check:type

# 清理依赖并重新安装
pnpm reinstall
```

## 核心架构设计

### 配置中心集成架构
- **模板方法模式**：`AbstractDynamicThreadPoolRefresher` 抽象类定义刷新流程，子类（`NacosCloudRefresherHandler`、`ApolloRefresherHandler`）实现配置中心特定的监听逻辑
- **观察者模式**：配置变更触发 Spring 事件 `ThreadPoolConfigUpdateEvent`，由 `DynamicThreadPoolRefreshListener` 处理
- **配置解析**：支持 YAML 和 Properties 格式，通过 `ConfigParser` 接口统一解析

### 线程池增强机制
- **动态代理增强拒绝策略**：使用 JDK `InvocationHandler` 代理 `RejectedExecutionHandler`，拦截 `rejectedExecution` 方法并统计拒绝次数
- **自定义可调整容量队列**：`ResizableCapacityLinkedBlockingQueue` 支持运行时调整队列容量
- **反射修改核心参数**：通过反射访问 `ThreadPoolExecutor` 的私有字段实现核心线程数、最大线程数等参数的热更新

### 监控与告警
- **定时监控**：`ThreadPoolMonitor` 定时采集线程池状态指标
- **阈值告警**：`ThreadPoolAlarmChecker` 检查队列使用率和活跃线程率，超过阈值触发告警
- **告警限流**：`AlarmRateLimiter` 防止告警频繁触发
- **通知渠道**：支持钉钉机器人通知（`DingTalkMessageService`）
- **Prometheus 集成**：通过 Spring Boot Actuator 暴露 `/actuator/prometheus` 端点

### Web 容器线程池适配
- **适配器模式**：`AbstractWebThreadPoolService` 抽象类，`TomcatWebThreadPoolService` 和 `JettyWebThreadPoolService` 实现具体容器的线程池管理
- **反射获取容器线程池**：通过 Spring `WebServerApplicationContext` 获取内嵌容器的线程池实例

### 关键设计模式
- **模板方法模式**：配置中心刷新流程的统一抽象
- **观察者模式**：配置变更事件的发布订阅机制
- **代理模式**：拒绝策略的增强和统计
- **适配器模式**：不同 Web 容器的线程池适配
- **构建者模式**：`ThreadPoolExecutorBuilder` 构建线程池参数

## 配置示例

### Nacos 配置文件示例
在 Nacos 配置中心创建配置文件（如 `onethread-nacos-cloud-example.yaml`）：
```yaml
onethread:
  config-file-type: yaml
  web:
    core-pool-size: 10
    maximum-pool-size: 200
    keep-alive-time: 60
  notify-platforms:
    platform: DING
    url: https://oapi.dingtalk.com/robot/send?access_token=xxx
  executors:
    - thread-pool-id: onethread-producer
      core-pool-size: 12
      maximum-pool-size: 24
      queue-capacity: 10000
      work-queue: ResizableCapacityLinkedBlockingQueue
      rejected-handler: CallerRunsPolicy
      keep-alive-time: 60
      alarm:
        enable: true
        queue-threshold: 80
        active-threshold: 80
```

### 应用启用动态线程池
在 Spring Boot 启动类添加注解：
```java
@EnableOneThread
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 定义动态线程池 Bean
```java
@Configuration
public class DynamicThreadPoolConfiguration {

    @Bean
    @DynamicThreadPool  // 必须标注该注解才能被 oneThread 管理和监控
    public ThreadPoolExecutor oneThreadProducer() {
        return ThreadPoolExecutorBuilder.builder()
            .threadPoolId("onethread-producer")  // 线程池唯一标识，对应配置中的 thread-pool-id
            .threadFactory("onethread-producer") // 线程工厂名称
            .build();
    }
}
```

**关键点**：
- `@DynamicThreadPool` 注解标识的 Bean 会被 oneThread 自动发现和监控
- `threadPoolId` 必须与配置中心配置文件中的 `thread-pool-id` 完全匹配
- 构建器模式简化线程池参数设置，支持所有核心参数配置

## 开发注意事项

### 代码规范
- 项目使用 Spotless 插件进行代码格式化，编译时自动应用格式化
- 所有 Java 文件头部包含版权声明（通过 Spotless 自动添加）

### 配置中心连接
- **Nacos 示例**默认连接到 `43.139.76.84:8848`
  - 用户名：`nacos`
  - 密码：`A1472580369Z`
  - 命名空间：`onethread-dev`
- **Apollo 示例**默认连接到 `http://127.0.0.1:8080`（需要本地部署 Apollo）
- 修改配置文件 `application.yaml` 中的连接地址以适配本地环境

### 监控指标暴露
- 需要在 `application.yaml` 中启用 Prometheus 端点：
  ```yaml
  management:
    endpoints:
      web:
        exposure:
          include: prometheus
    metrics:
      prometheus:
        metrics:
          export:
            enabled: true
  ```

### 前端开发代理配置
- 前端开发时 API 请求代理到 `/api`（见 `.env.development`）
- 需要配置 Vite 代理到后端服务（通常是 `http://localhost:18080`）

### 模块依赖关系
**依赖层次**：
- `spring-base` 依赖 `core`
- `starter/*` 依赖 `spring-base`
- `example/*` 依赖具体的 `starter` 模块
- `dashboard-dev` 是独立的后端服务，不应用于生产环境（仅用于开发调试）

**关键依赖**：
- spring-base: 包含 `@EnableOneThread` 和 `@DynamicThreadPool` 注解，用于启用和标记动态线程池
- core: 核心线程池增强逻辑，包含反射修改、监控告警等功能
- starter/*: 不同配置中心的具体实现，采用模板方法模式统一流程

### 学习资源
项目包含多个学习指南文档，建议按顺序阅读：
- `新手学习指南.md` - 适合初次接触项目的开发者
- `oneThread学习指南.md` - 深入理解项目核心机制
- `项目学习指南.md` - 项目整体架构和实现细节
- `oneThread前端架构指南.md` - 前端控制台架构说明
- `线程池列表获取流程详解.md` / `线程池列表获取流程详解-后端重点版.md` - 核心流程分析

### Windows 环境注意事项
- 项目在 Windows 环境下运行时，路径分隔符使用反斜杠 `\`
- 使用 PowerShell 或 CMD 运行 Maven 命令
- 如果遇到编码问题，确保终端编码设置为 UTF-8：`chcp 65001`

### 前端 API 代理配置
前端开发时需要配置 API 代理，在 `onethread-dashboard-main/.env.development` 中已配置：
```
VITE_GLOB_API_URL=/api
```
前端会自动代理 `/api` 请求到控制台后端（默认 `http://localhost:9999`）

### 常见问题

#### Spotless 插件构建错误
如果遇到 Spotless 相关的构建错误（如 BOM 格式问题），可以：
1. 跳过 Spotless 检查：`mvn clean package -Dspotless.check.skip=true`
2. 或在特定模块的 pom.xml 中临时禁用 Spotless（参考 dashboard-dev 模块的配置）

#### 反射权限错误
Java 17 对反射访问有严格限制，必须添加以下 JVM 参数：
```
--add-opens=java.base/java.util.concurrent=ALL-UNNAMED
```
在 IDE（如 IDEA）中配置方法：Run/Debug Configurations → VM options

**调试技巧**：如果配置后仍有问题，检查 JVM 启动日志是否有 `WARNING: Illegal reflective access` 相关信息。

#### Nacos 连接失败
如果无法连接到默认 Nacos 服务器（43.139.76.84:8848），可以：
1. 使用本地 Nacos：下载并启动 Nacos Server
2. 修改 `application.yaml` 中的 `spring.cloud.nacos.config.server-addr` 和 `spring.cloud.nacos.discovery.server-addr`
3. 创建对应的命名空间 `onethread-dev` 并配置相应的配置文件

### 其他常见启动失败

#### 端口冲突
默认端口可能被占用，可通过以下方式修改：
- 控制台后端（9999）：修改 `dashboard-dev/application.yaml` 中的 `server.port`
- Nacos 示例应用（18080）：修改 `example/nacos-cloud-example/application.yaml` 中的 `server.port`
- 前端控制台（5777）：修改 `onethread-dashboard-main/web-ele/vite.config.ts` 中的端口配置

#### 依赖冲突
如果遇到 Spring Boot 版本冲突，确保：
1. Java 版本为 17 或更高版本
2. Maven 依赖版本统一（参考根 pom.xml 中的 dependencyManagement）
3. 检查是否有重复的 Spring Boot Starter 依赖

#### 配置文件格式错误
YAML 配置文件格式敏感，注意：
- 使用空格缩进，不要使用 Tab
- 冒号后要有空格
- 特殊字符可能需要引号包裹
