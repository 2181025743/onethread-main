/**
 * oneThread 核心包
 * <p>
 * 该包是 oneThread 动态线程池框架的核心模块，包含了框架运行所需的所有核心组件。
 * 
 * <p><b>命名说明：</b>
 * <br>本包相关类源自动态线程池功能，为提升命名可读性与项目辨识度，
 * 统一使用项目代号 <b>oneThread</b> 作为前缀，替代原 DynamicThreadPool 前缀。
 * 
 * <p><b>示例重命名如下：</b>
 * <ul>
 *   <li>DynamicThreadPoolExecutor → {@link com.nageoffer.onethread.core.executor.OneThreadExecutor}</li>
 *   <li>DynamicThreadPoolRegistry → {@link com.nageoffer.onethread.core.executor.OneThreadRegistry}</li>
 *   <li>DynamicThreadPoolBuilder → {@link com.nageoffer.onethread.core.toolkit.ThreadPoolExecutorBuilder}</li>
 * </ul>
 * 
 * <p><b>核心模块结构：</b>
 * <pre>
 * core/
 * ├── executor/              线程池执行器相关
 * │   ├── OneThreadExecutor          动态线程池执行器
 * │   ├── OneThreadRegistry          线程池注册表
 * │   ├── ThreadPoolExecutorHolder   线程池包装类
 * │   ├── ThreadPoolExecutorProperties  线程池配置属性
 * │   └── support/                   支持类
 * │       ├── BlockingQueueTypeEnum     队列类型枚举
 * │       ├── RejectedPolicyTypeEnum    拒绝策略枚举
 * │       └── ResizableCapacityLinkedBlockingQueue  可调整容量队列
 * │
 * ├── toolkit/               工具类
 * │   ├── ThreadPoolExecutorBuilder  线程池构建器
 * │   └── ThreadFactoryBuilder       线程工厂构建器
 * │
 * ├── parser/                配置解析器
 * │   ├── ConfigParser               解析器接口
 * │   ├── YamlConfigParser           YAML解析器
 * │   └── PropertiesConfigParser     Properties解析器
 * │
 * ├── monitor/               监控相关
 * │   ├── ThreadPoolMonitor          线程池监控器
 * │   ├── ThreadPoolRuntimeInfo      运行时信息实体
 * │   └── DeltaWrapper               增量计算包装器
 * │
 * ├── alarm/                 告警相关
 * │   └── ThreadPoolAlarmChecker     告警检查器
 * │
 * ├── notification/          通知相关
 * │   ├── service/
 * │   │   ├── NotifierService        通知服务接口
 * │   │   ├── NotifierDispatcher     通知分发器
 * │   │   ├── DingTalkMessageService 钉钉通知服务
 * │   │   └── AlarmRateLimiter       告警限流器
 * │   └── dto/
 * │       ├── ThreadPoolAlarmNotifyDTO         告警通知DTO
 * │       ├── ThreadPoolConfigChangeDTO        配置变更DTO
 * │       └── WebThreadPoolConfigChangeDTO     Web配置变更DTO
 * │
 * ├── config/                配置相关
 * │   ├── BootstrapConfigProperties  启动配置属性
 * │   └── ApplicationProperties      应用属性
 * │
 * └── constant/              常量定义
 *     └── Constants                  框架常量
 * </pre>
 * 
 * <p><b>核心功能：</b>
 * <ul>
 *   <li><b>动态线程池：</b>支持运行时调整线程池参数，无需重启应用</li>
 *   <li><b>配置中心集成：</b>与 Nacos/Apollo 无缝集成，配置热更新</li>
 *   <li><b>监控采集：</b>定时采集线程池状态，支持 Prometheus/Grafana</li>
 *   <li><b>告警通知：</b>自动检测异常状态并发送钉钉告警</li>
 *   <li><b>可调整队列：</b>队列容量可运行时动态调整</li>
 *   <li><b>拒绝统计：</b>自动统计拒绝次数，便于监控</li>
 * </ul>
 * 
 * <p><b>设计模式应用：</b>
 * <ul>
 *   <li><b>建造者模式：</b>ThreadPoolExecutorBuilder、ThreadFactoryBuilder</li>
 *   <li><b>工厂方法模式：</b>BlockingQueueTypeEnum、RejectedPolicyTypeEnum</li>
 *   <li><b>注册表模式：</b>OneThreadRegistry</li>
 *   <li><b>单例模式：</b>ConfigParserHandler、ApplicationProperties</li>
 *   <li><b>策略模式：</b>ConfigParser、NotifierService</li>
 *   <li><b>装饰器模式：</b>拒绝策略包装（增加计数功能）</li>
 *   <li><b>观察者模式：</b>配置变更事件发布订阅</li>
 * </ul>
 * 
 * <p><b>技术亮点：</b>
 * <ul>
 *   <li><b>双锁队列：</b>ResizableCapacityLinkedBlockingQueue 使用读写分离锁，性能优异</li>
 *   <li><b>volatile 容量：</b>将 final 容量改为 volatile，实现运行时调整</li>
 *   <li><b>原子操作：</b>使用 AtomicLong、AtomicInteger 保证并发安全</li>
 *   <li><b>延迟加载：</b>告警数据使用 Supplier 延迟加载，提高性能</li>
 *   <li><b>增量计算：</b>DeltaWrapper 计算监控指标的增量值</li>
 *   <li><b>限流机制：</b>AlarmRateLimiter 防止告警风暴</li>
 * </ul>
 * 
 * <p><b>扩展性：</b>
 * <ul>
 *   <li>开发者可根据喜好自定义类名前缀（如 Hippo4jExecutor），以符合项目命名规范或个人偏好</li>
 *   <li>新增队列类型只需在 BlockingQueueTypeEnum 中添加枚举项</li>
 *   <li>新增通知平台只需实现 NotifierService 并在 NotifierDispatcher 中注册</li>
 *   <li>新增配置格式只需实现 ConfigParser 并在 ConfigParserHandler 中注册</li>
 * </ul>
 * 
 * <p><b>使用示例：</b>
 * <pre>{@code
 * // 1. 创建动态线程池
 * ThreadPoolExecutor executor = ThreadPoolExecutorBuilder.builder()
 *     .dynamicPool()
 *     .threadPoolId("order-processor")
 *     .corePoolSize(10)
 *     .maximumPoolSize(20)
 *     .workQueueType(BlockingQueueTypeEnum.RESIZABLE_CAPACITY_LINKED_BLOCKING_QUEUE)
 *     .threadFactory("order")
 *     .build();
 * 
 * // 2. 线程池自动注册到 OneThreadRegistry
 * // 3. 监控器自动采集状态
 * // 4. 告警检查器自动检测异常
 * // 5. 配置变更时自动更新参数
 * }</pre>
 * 
 * @author 杨潇
 * @since 2025-04-20
 * @see com.nageoffer.onethread.core.executor.OneThreadExecutor 核心执行器
 * @see com.nageoffer.onethread.core.executor.OneThreadRegistry 线程池注册表
 * @see com.nageoffer.onethread.core.toolkit.ThreadPoolExecutorBuilder 线程池构建器
 */
package com.nageoffer.onethread.core;
