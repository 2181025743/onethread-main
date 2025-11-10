# oneThread Simple Example

A simple quick-start example demonstrating how to use the oneThread dynamic thread pool framework.

## Quick Start

### 1. Configure Nacos Configuration Center

#### Login to Nacos Console
- URL: http://43.139.76.84:8848/nacos
- Username: `nacos`
- Password: `A1472580369Z`

#### Create Configuration File
1. Go to "Configuration Management" -> "Configuration List"
2. Select namespace: `onethread-dev`
3. Click "+" button to create configuration:
   - **Data ID**: `onethread-simple-example.yaml`
   - **Group**: `DEFAULT_GROUP`
   - **Format**: `YAML`
   - **Content**: Copy content from `nacos-config-example.yaml`

4. Click "Publish" to save

### 2. Start Application

#### Method 1: Start with Maven
```bash
cd simple-example
mvn spring-boot:run
```

#### Method 2: Start with IDEA
1. Open `SimpleApplication.java`
2. Click Run/Debug button
3. Make sure to add VM options:
   ```
   --add-opens=java.base/java.util.concurrent=ALL-UNNAMED
   ```

### 3. Verify Running

#### Test Endpoints
Application runs on port **18082**

Test endpoints:
- Health check: http://localhost:18082/actuator/health
- Prometheus metrics: http://localhost:18082/actuator/prometheus

#### Check Logs
Startup logs should show:
```
Creating business thread pool: business-thread-pool
Creating notification thread pool: notify-thread-pool
oneThread dynamic thread pool framework started
```

## Configuration Description

### Application Configuration File
- **application.yml**: Local application configuration (port, Nacos connection info)

### Nacos Configuration File
- **nacos-config-example.yaml**: Nacos configuration center example
  - Contains thread pool parameter configuration
  - Contains alarm threshold configuration
  - Contains DingTalk notification configuration

## Core Features Demo

### 1. Dynamic Thread Pool Definition
Two dynamic thread pools are defined in `ThreadPoolConfig.java`:

```java
@Bean
@DynamicThreadPool  // Mark as dynamic thread pool
public ThreadPoolExecutor businessThreadPool() {
    return ThreadPoolExecutorBuilder.builder()
        .threadPoolId("business-thread-pool")  // Thread pool unique ID
        .threadFactory("business-pool")
        .corePoolSize(5)
        .maximumPoolSize(10)
        .workQueueCapacity(100)
        .build();
}
```

### 2. Dynamic Parameter Adjustment
After modifying configuration in Nacos console, thread pool parameters will be automatically updated without restarting the application.

Adjustable parameters:
- `core-pool-size`: Core thread count
- `maximum-pool-size`: Maximum thread count
- `queue-capacity`: Queue capacity
- `keep-alive-time`: Thread idle keep-alive time
- `alarm.queue-threshold`: Queue usage rate alarm threshold
- `alarm.active-threshold`: Active thread rate alarm threshold

### 3. Monitoring and Alerts
- **Prometheus Monitoring**: Visit `/actuator/prometheus` to view metrics
- **Alert Notification**: When queue or active threads exceed threshold, alerts will be sent via DingTalk robot

## Port Description

- **Application Port**: 18082
- **Other Example Ports**:
  - nacos-cloud-example: 18081
  - dashboard-dev: 9999

## Common Issues

### 1. Cannot Connect to Nacos
Verify Nacos server is accessible:
```bash
curl http://43.139.76.84:8848/nacos
```

If inaccessible, modify `server-addr` in `application.yml` to local Nacos.

### 2. Startup Error: Illegal reflective access
Need to add JVM parameter:
```
--add-opens=java.base/java.util.concurrent=ALL-UNNAMED
```

### 3. Configuration Not Working
Check:
1. Nacos configuration Data ID is `onethread-simple-example.yaml`
2. Namespace is `onethread-dev`
3. Thread pool `thread-pool-id` matches `threadPoolId` in code

## Next Steps

1. Try modifying Nacos configuration and observe thread pool parameters update dynamically
2. Configure DingTalk robot to receive alert notifications
3. Use Prometheus + Grafana to build monitoring dashboard
4. View dashboard-dev console (port 9999) for visual management

## Related Documentation

- Project Learning Guide (in parent directory)
- Beginner's Guide (in parent directory)
- CLAUDE.md (in parent directory)
