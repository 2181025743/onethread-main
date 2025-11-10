# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

oneThread is a production-grade dynamic thread pool framework for Java that enables runtime adjustment of thread pool parameters, real-time monitoring, and threshold-based alerting. It integrates with configuration centers (Nacos, Apollo) to provide hot-reloading capabilities for thread pool configuration.

**Key Characteristics:**
- Multi-module Maven project with ~13.4k lines of code (5k pure Java)
- Built on Spring Boot 3.0.7 with Java 17
- Supports Nacos and Apollo configuration centers
- Includes Vue 3 + Element Plus dashboard for visualization
- Based on Hippo4j open-source project patterns

## Build & Development Commands

### Java Backend

```powershell
# Build entire project (applies Spotless formatting)
.\mvnw clean package -DskipTests

# Build specific module
.\mvnw clean package -pl example/nacos-cloud-example -am

# Apply code formatting (required before commits)
.\mvnw spotless:apply

# Check code formatting
.\mvnw spotless:check

# Run all tests
.\mvnw test

# Run tests for specific module
.\mvnw test -pl core

# Run specific test class
.\mvnw test -Dtest=ThreadPoolExecutorBuilderTest
```

### Running Examples

**Nacos Example:**
```powershell
cd example\nacos-cloud-example
.\..\..\mvnw spring-boot:run

# Or run JAR directly (requires Java 9+ reflection permission)
java --add-opens=java.base/java.util.concurrent=ALL-UNNAMED `
     -jar target\nacos-cloud-example-0.0.1-SNAPSHOT.jar
```

**Apollo Example:**
```powershell
cd example\apollo-example
.\..\..\mvnw spring-boot:run
```

**Dashboard Backend (Development Only):**
```powershell
cd dashboard-dev
.\..\mvnw spring-boot:run
```

### Frontend Dashboard

```powershell
cd onethread-dashboard-main

# Install dependencies (first time)
pnpm install

# Run development server (Element Plus version)
pnpm dev:ele
# Access at http://localhost:5777

# Build for production
pnpm build:ele

# Code formatting
pnpm format

# Linting
pnpm lint

# Type checking
pnpm check:type
```

## Architecture Overview

### Module Structure

```
core/                           # Core primitives: thread pool builders, monitoring, alarms
├── alarm/                      # Alarm checking & rate limiting
├── config/                     # Configuration models
├── executor/                   # Custom ThreadPoolExecutor & queue implementations
├── monitor/                    # Thread pool metrics collection
├── notification/               # Notification channels (DingTalk)
└── parser/                     # Configuration parsers (YAML/Properties)

spring-base/                    # Spring integration layer
├── DynamicThreadPool.java      # Annotation for marking dynamic thread pools
├── enable/EnableOneThread      # Main framework activation annotation
└── support/                    # Bean post-processor, context holder

starter/
├── common-spring-boot-starter/ # Abstract refresh logic & event handling
│   └── refresher/
│       ├── AbstractDynamicThreadPoolRefresher    # Template method base
│       ├── DynamicThreadPoolRefreshListener      # Event-driven refresh
│       └── ThreadPoolConfigUpdateEvent           # Configuration change event
├── nacos-cloud-spring-boot-starter/              # Nacos listener implementation
├── apollo-spring-boot-starter/                   # Apollo listener implementation
├── dashboard-dev-spring-boot-starter/            # REST API for dashboard
└── adapter/web-spring-boot-starter/              # Tomcat/Jetty adapter

example/                        # Runnable integration examples
└── nacos-cloud-example/        # Demonstrates Nacos-based refresh

dashboard-dev/                  # Standalone dashboard service (dev only)
onethread-dashboard-main/       # Vue 3 frontend console
```

### Core Design Patterns

**1. Template Method Pattern (Configuration Refresh)**
- `AbstractDynamicThreadPoolRefresher` defines the refresh flow skeleton
- Subclasses implement config-center-specific listeners
- Enables pluggable support for multiple configuration centers

**2. Observer Pattern (Configuration Updates)**
- Configuration changes emit `ThreadPoolConfigUpdateEvent`
- `DynamicThreadPoolRefreshListener` reacts to events
- Decouples configuration source from thread pool update logic

**3. Dynamic Proxy (Rejection Handler Enhancement)**
- `RejectedProxyInvocationHandler` wraps `RejectedExecutionHandler`
- Intercepts `rejectedExecution()` calls to track rejection counts
- Enables alerting without modifying JDK rejection policies

**4. Builder Pattern (Thread Pool Construction)**
- `ThreadPoolExecutorBuilder` provides fluent API
- Centralizes thread pool creation with sensible defaults

### Runtime Parameter Refresh Mechanism

1. Configuration center detects YAML/Properties change
2. Config center listener triggers refresh in respective starter module
3. Starter parses config and publishes `ThreadPoolConfigUpdateEvent`
4. `DynamicThreadPoolRefreshListener` receives event
5. Uses reflection to modify `ThreadPoolExecutor` private fields:
   - Core pool size (via `setCorePoolSize()`)
   - Max pool size (via `setMaximumPoolSize()`)
   - Keep-alive time (via `setKeepAliveTime()`)
   - Queue capacity (if using `ResizableCapacityLinkedBlockingQueue`)

### Monitoring & Alerting Flow

1. `ThreadPoolMonitor` periodically collects metrics (active threads, queue size, etc.)
2. `ThreadPoolAlarmChecker` evaluates thresholds (queue %, active %)
3. If threshold exceeded, `AlarmRateLimiter` checks if alert interval passed
4. `DingTalkMessageService` sends notification to configured webhook
5. Metrics exposed via `/actuator/prometheus` for Grafana

### Web Container Adaptation

- `AbstractWebThreadPoolService` provides base for container integration
- `TomcatWebThreadPoolService` / `JettyWebThreadPoolService` use reflection to access container thread pools
- Allows dynamic adjustment of servlet container thread pools (not just application pools)

## Configuration Requirements

### Enabling Dynamic Thread Pools

**1. Annotate main application class:**
```java
@EnableOneThread
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

**2. Define thread pool beans:**
```java
@Configuration
public class DynamicThreadPoolConfiguration {
    
    @Bean
    @DynamicThreadPool
    public ThreadPoolExecutor myThreadPool() {
        return ThreadPoolExecutorBuilder.builder()
            .threadPoolId("my-thread-pool")
            .threadFactory("my-pool")
            .build();
    }
}
```

**3. Configure in Nacos/Apollo:**
```yaml
onethread:
  config-file-type: yaml
  web:
    core-pool-size: 10
    maximum-pool-size: 200
  notify-platforms:
    platform: DING
    url: https://oapi.dingtalk.com/robot/send?access_token=xxxx
  executors:
    - thread-pool-id: my-thread-pool
      core-pool-size: 12
      maximum-pool-size: 24
      queue-capacity: 10000
      work-queue: ResizableCapacityLinkedBlockingQueue
      rejected-handler: CallerRunsPolicy
      alarm:
        enable: true
        queue-threshold: 80
        active-threshold: 80
```

### Critical Java Runtime Parameters

When running on Java 9+, **always** include:
```
--add-opens=java.base/java.util.concurrent=ALL-UNNAMED
```
This grants reflection access to `ThreadPoolExecutor` internals. Add to:
- IDE run configurations (VM options)
- Maven plugin configuration
- Docker/production startup scripts

### Prometheus Integration

Ensure `application.yaml` includes:
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

## Development Guidelines

### Code Style Enforcement

- **Spotless Maven plugin** auto-formats on compile
- All Java files must include copyright header (auto-added by Spotless)
- Run `.\mvnw spotless:apply` before committing
- 4-space indentation, UTF-8 encoding

### Naming Conventions

- Thread pool IDs: `kebab-case` (e.g., `my-thread-pool`)
- Java classes: `UpperCamelCase`
- Beans/methods: `lowerCamelCase`

### Testing Practices

- Tests located in `src/test/java` mirroring package structure
- JUnit 5 with AssertJ assertions
- Test concurrency-sensitive code paths (rejection, queue resizing)
- Verify refresh logic doesn't break running pools

### Module Dependency Rules

- `core` has no Spring dependencies (pure Java)
- `spring-base` depends on `core`
- `starter/*` modules depend on `spring-base`
- `example/*` depends on specific starters
- `dashboard-dev` is isolated (dev environment only)

### Configuration Center Setup

**Nacos:**
- Default example connects to `43.139.76.84:8848` (requires credentials)
- Modify `application.yaml` for local Nacos instance

**Apollo:**
- Default expects `http://127.0.0.1:8080`
- Requires local Apollo deployment for testing

### Secrets Handling

- Never commit real DingTalk webhook URLs or tokens
- Use placeholder values (e.g., `xxxx`) in examples
- Store sensitive config in environment variables or secure vaults

## Common Development Workflows

### Adding a New Configuration Center

1. Create `starter/<center>-spring-boot-starter/` module
2. Extend `AbstractDynamicThreadPoolRefresher`
3. Implement config center listener registration
4. Add Spring Boot auto-configuration
5. Create example in `example/<center>-example/`

### Modifying Refresh Logic

1. Edit `AbstractDynamicThreadPoolRefresher` for common behavior
2. Test with both Nacos and Apollo examples
3. Verify dashboard reflects changes

### Adding Custom Work Queues

1. Create queue class in `core/executor/`
2. Implement capacity adjustment if needed (see `ResizableCapacityLinkedBlockingQueue`)
3. Register in configuration parser

### Dashboard Development

- Backend API: `dashboard-dev-spring-boot-starter/`
- Frontend: `onethread-dashboard-main/apps/web-ele/`
- API proxy configured in `.env.development` (points to `/api`)
- Backend typically runs on `localhost:18080`

## Important Constraints

- **dashboard-dev module is NOT production-ready** - it's a development convenience tool
- Thread pool parameter changes apply immediately; no rollback mechanism
- Alarm rate limiting prevents notification storms but may delay critical alerts
- Queue resizing only works with `ResizableCapacityLinkedBlockingQueue`
- Web container thread pool adjustment requires matching container (Tomcat/Jetty detection)

## Copyright & Licensing

- Project is **private** (not open source per README)
- Code headers required by Spotless reference copyright files
- Unauthorized redistribution prohibited
