/*
 * 动态线程池（oneThread）基础组件项目
 *
 * 版权所有 (C) [2024-至今] [山东流年网络科技有限公司]
 *
 * 保留所有权利。
 *
 * 1. 定义和解释
 *    本文件（包括其任何修改、更新和衍生内容）是由[山东流年网络科技有限公司]及相关人员开发的。
 *    "软件"指的是与本文件相关的任何代码、脚本、文档和相关的资源。
 *
 * 2. 使用许可
 *    本软件的使用、分发和解释均受中华人民共和国法律的管辖。只有在遵守以下条件的前提下，才允许使用和分发本软件：
 *    a. 未经[山东流年网络科技有限公司]的明确书面许可，不得对本软件进行修改、复制、分发、出售或出租。
 *    b. 任何未授权的复制、分发或修改都将被视为侵犯[山东流年网络科技有限公司]的知识产权。
 *
 * 3. 免责声明
 *    本软件按"原样"提供，没有任何明示或暗示的保证，包括但不限于适销性、特定用途的适用性和非侵权性的保证。
 *    在任何情况下，[山东流年网络科技有限公司]均不对任何直接、间接、偶然、特殊、典型或间接的损害（包括但不限于采购替代商品或服务；使用、数据或利润损失）承担责任。
 *
 * 4. 侵权通知与处理
 *    a. 如果[山东流年网络科技有限公司]发现或收到第三方通知，表明存在可能侵犯其知识产权的行为，公司将采取必要的措施以保护其权利。
 *    b. 对于任何涉嫌侵犯知识产权的行为，[山东流年网络科技有限公司]可能要求侵权方立即停止侵权行为，并采取补救措施，包括但不限于删除侵权内容、停止侵权产品的分发等。
 *    c. 如果侵权行为持续存在或未能得到妥善解决，[山东流年网络科技有限公司]保留采取进一步法律行动的权利，包括但不限于发出警告信、提起民事诉讼或刑事诉讼。
 *
 * 5. 其他条款
 *    a. [山东流年网络科技有限公司]保留随时修改这些条款的权利。
 *    b. 如果您不同意这些条款，请勿使用本软件。
 *
 * 未经[山东流年网络科技有限公司]的明确书面许可，不得使用此文件的任何部分。
 *
 * 本软件受到[山东流年网络科技有限公司]及其许可人的版权保护。
 */

package com.nageoffer.onethread.core.executor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 线程池属性参数配置类
 * <p>
 * 该类封装了动态线程池的所有可配置参数，包括基础参数（核心线程数、最大线程数等）、
 * 队列配置、拒绝策略、告警配置和通知配置。这些参数可以通过配置中心（Nacos/Apollo）
 * 进行动态调整，无需重启应用即可生效。
 * 
 * <p><b>核心作用：</b>
 * <ul>
 *   <li><b>配置承载：</b>作为配置中心和线程池实例之间的数据传输对象（DTO）</li>
 *   <li><b>参数绑定：</b>通过 Spring Boot 的 Binder 机制从配置文件绑定参数</li>
 *   <li><b>配置对比：</b>配置变更时对比新旧配置，判断哪些参数发生了变化</li>
 *   <li><b>元数据保存：</b>保存队列类型、拒绝策略等无法从线程池实例直接获取的元数据</li>
 * </ul>
 * 
 * <p><b>配置来源：</b>
 * <ul>
 *   <li><b>配置中心：</b>从 Nacos/Apollo 拉取的 YAML/Properties 配置</li>
 *   <li><b>本地配置：</b>从 application.yml 读取的本地配置</li>
 *   <li><b>前端控制台：</b>通过 Web 界面修改后推送的配置</li>
 * </ul>
 * 
 * <p><b>配置文件示例（YAML格式）：</b>
 * <pre>
 * onethread:
 *   executors:
 *     - thread-pool-id: order-processor
 *       core-pool-size: 10
 *       maximum-pool-size: 20
 *       queue-capacity: 100
 *       work-queue: LinkedBlockingQueue
 *       rejected-handler: CallerRunsPolicy
 *       keep-alive-time: 60
 *       allow-core-thread-time-out: false
 *       alarm:
 *         enable: true
 *         queue-threshold: 80
 *         active-threshold: 80
 *       notify:
 *         receives: "张三,李四"
 *         interval: 5
 * </pre>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li><b>配置解析：</b>从配置文件解析为对象</li>
 *   <li><b>参数传递：</b>在配置监听器、更新逻辑之间传递参数</li>
 *   <li><b>配置对比：</b>新旧配置对比，判断变更项</li>
 *   <li><b>配置持久化：</b>序列化为 YAML/JSON 保存到配置中心</li>
 * </ul>
 * 
 * <p><b>设计特点：</b>
 * <ul>
 *   <li><b>Builder模式：</b>支持建造者模式，便于构建对象</li>
 *   <li><b>链式调用：</b>使用 {@code @Accessors(chain = true)} 支持链式 setter</li>
 *   <li><b>嵌套配置：</b>包含 {@link NotifyConfig} 和 {@link AlarmConfig} 内嵌类</li>
 *   <li><b>默认值：</b>部分字段提供合理的默认值</li>
 * </ul>
 * 
 * <p><b>使用示例：</b>
 * <pre>{@code
 * // 示例1：使用Builder构建
 * ThreadPoolExecutorProperties properties = ThreadPoolExecutorProperties.builder()
 *     .threadPoolId("order-processor")
 *     .corePoolSize(10)
 *     .maximumPoolSize(20)
 *     .queueCapacity(100)
 *     .workQueue("LinkedBlockingQueue")
 *     .rejectedHandler("CallerRunsPolicy")
 *     .keepAliveTime(60L)
 *     .allowCoreThreadTimeOut(false)
 *     .build();
 * 
 * 
 * // 示例2：使用链式调用
 * ThreadPoolExecutorProperties properties = new ThreadPoolExecutorProperties()
 *     .setThreadPoolId("message-consumer")
 *     .setCorePoolSize(5)
 *     .setMaximumPoolSize(10);
 * 
 * 
 * // 示例3：从配置中心解析
 * String yamlContent = nacosClient.getConfig("onethread-app.yaml");
 * Map<Object, Object> configMap = yamlParser.parse(yamlContent);
 * Binder binder = new Binder(new MapConfigurationPropertySource(configMap));
 * BootstrapConfigProperties config = binder.bind("onethread", ...).get();
 * List<ThreadPoolExecutorProperties> executors = config.getExecutors();
 * }</pre>
 * 
 * @author 杨潇
 * @since 2025-04-20
 * @see ThreadPoolExecutorHolder 线程池包装类
 * @see OneThreadRegistry 线程池注册表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ThreadPoolExecutorProperties {

    /**
     * 线程池唯一标识
     * <p>
     * 用于唯一标识线程池实例，必须在应用中保持全局唯一。
     * 该 ID 用于配置中心配置匹配、监控数据上报、日志记录等场景。
     * 
     * <p><b>配置示例：</b>{@code thread-pool-id: order-processor}
     * 
     * <p><b>命名建议：</b>使用短横线分隔的小写字母，体现业务含义
     */
    private String threadPoolId;

    /**
     * 核心线程数
     * <p>
     * 线程池中始终保持的最小线程数量，即使这些线程处于空闲状态。
     * 
     * <p><b>配置示例：</b>{@code core-pool-size: 10}
     * 
     * <p><b>配置建议：</b>
     * <ul>
     *   <li>CPU密集型：核心数 = CPU核心数</li>
     *   <li>IO密集型：核心数 = CPU核心数 * 2</li>
     * </ul>
     * 
     * <p><b>注意：</b>
     * <ul>
     *   <li>必须 <= 最大线程数</li>
     *   <li>如果设置了 {@link #allowCoreThreadTimeOut} 为 true，核心线程空闲后也会被回收</li>
     * </ul>
     */
    private Integer corePoolSize;

    /**
     * 最大线程数
     * <p>
     * 线程池允许创建的最大线程数量。当队列满时，会创建新线程直到达到此值。
     * 
     * <p><b>配置示例：</b>{@code maximum-pool-size: 20}
     * 
     * <p><b>配置建议：</b>
     * <ul>
     *   <li>稳定负载：设置为与核心数相同</li>
     *   <li>波动负载：设置为核心数的 1.5~2 倍</li>
     *   <li>避免过大，否则上下文切换开销增加</li>
     * </ul>
     * 
     * <p><b>注意：</b>必须 >= 核心线程数
     */
    private Integer maximumPoolSize;

    /**
     * 队列容量
     * <p>
     * 任务队列的最大容量，决定了有多少任务可以排队等待执行。
     * 
     * <p><b>配置示例：</b>{@code queue-capacity: 100}
     * 
     * <p><b>配置建议：</b>
     * <ul>
     *   <li>设置为有限值（如 100~10000），避免无限堆积</li>
     *   <li>配合监控和告警，及时发现队列堆积问题</li>
     * </ul>
     * 
     * <p><b>影响：</b>
     * <ul>
     *   <li>容量过小：容易触发拒绝策略</li>
     *   <li>容量过大：任务堆积，响应时间变长，可能OOM</li>
     * </ul>
     * 
     * <p><b>动态调整：</b>
     * 如果使用 {@link com.nageoffer.onethread.core.executor.support.ResizableCapacityLinkedBlockingQueue}，
     * 此参数可以运行时动态调整。
     */
    private Integer queueCapacity;

    /**
     * 阻塞队列类型
     * <p>
     * 指定使用哪种类型的阻塞队列，不同类型有不同的性能特征和适用场景。
     * 
     * <p><b>配置示例：</b>{@code work-queue: LinkedBlockingQueue}
     * 
     * <p><b>可选值：</b>
     * <ul>
     *   <li>{@code LinkedBlockingQueue} - 通用场景，推荐</li>
     *   <li>{@code ArrayBlockingQueue} - 内存敏感场景</li>
     *   <li>{@code SynchronousQueue} - 快速响应场景</li>
     *   <li>{@code ResizableCapacityLinkedBlockingQueue} - 动态线程池推荐</li>
     *   <li>{@code PriorityBlockingQueue} - 需要任务优先级</li>
     * </ul>
     * 
     * <p><b>注意：</b>该值为字符串类型，通过 {@link com.nageoffer.onethread.core.executor.support.BlockingQueueTypeEnum}
     * 转换为实际的队列实例。
     * 
     * @see com.nageoffer.onethread.core.executor.support.BlockingQueueTypeEnum 队列类型枚举
     */
    private String workQueue;

    /**
     * 拒绝策略类型
     * <p>
     * 当线程池和队列都满时，对新任务的处理策略。
     * 
     * <p><b>配置示例：</b>{@code rejected-handler: CallerRunsPolicy}
     * 
     * <p><b>可选值：</b>
     * <ul>
     *   <li>{@code CallerRunsPolicy} - 调用者线程执行（推荐）</li>
     *   <li>{@code AbortPolicy} - 抛出异常（默认）</li>
     *   <li>{@code DiscardPolicy} - 静默丢弃</li>
     *   <li>{@code DiscardOldestPolicy} - 丢弃最旧任务</li>
     * </ul>
     * 
     * <p><b>选择建议：</b>
     * <ul>
     *   <li>重要任务：CallerRunsPolicy 或 AbortPolicy</li>
     *   <li>可丢失任务：DiscardPolicy</li>
     *   <li>实时数据：DiscardOldestPolicy</li>
     * </ul>
     * 
     * <p><b>注意：</b>该值为字符串类型，通过 {@link com.nageoffer.onethread.core.executor.support.RejectedPolicyTypeEnum}
     * 转换为实际的策略实例。
     * 
     * @see com.nageoffer.onethread.core.executor.support.RejectedPolicyTypeEnum 拒绝策略枚举
     */
    private String rejectedHandler;

    /**
     * 线程空闲存活时间（单位：秒）
     * <p>
     * 当线程数超过核心线程数时，多余的空闲线程在被终止前等待新任务的最长时间。
     * 
     * <p><b>配置示例：</b>{@code keep-alive-time: 60}（60秒）
     * 
     * <p><b>配置建议：</b>
     * <ul>
     *   <li>短时任务：60秒</li>
     *   <li>长时任务：300秒</li>
     *   <li>如果核心数=最大数，该参数无实际作用</li>
     * </ul>
     * 
     * <p><b>作用：</b>控制线程池的弹性收缩能力，在流量降低后回收多余线程。
     * 
     * <p><b>注意：</b>默认只对超过核心线程数的线程生效，除非设置了 {@link #allowCoreThreadTimeOut}。
     */
    private Long keepAliveTime;

    /**
     * 是否允许核心线程超时
     * <p>
     * 控制核心线程在空闲时是否会被回收。
     * 
     * <p><b>配置示例：</b>{@code allow-core-thread-time-out: false}
     * 
     * <p><b>默认值：</b>false（核心线程常驻）
     * 
     * <p><b>行为差异：</b>
     * <ul>
     *   <li><b>false：</b>核心线程始终存活，即使空闲也不回收</li>
     *   <li><b>true：</b>核心线程空闲超过 {@link #keepAliveTime} 后也会被回收</li>
     * </ul>
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>设为 true：任务量波动大，希望完全回收线程节省资源</li>
     *   <li>设为 false：任务量稳定，保持核心线程常驻</li>
     * </ul>
     * 
     * <p><b>注意：</b>如果设为 true，线程池可能在空闲期间完全没有线程（降为0）。
     */
    private Boolean allowCoreThreadTimeOut = false;

    /**
     * 通知配置
     * <p>
     * 配置线程池参数变更时的通知行为，包括接收人和通知间隔。
     * 
     * <p><b>配置示例：</b>
     * <pre>
     * notify:
     *   receives: "张三,李四"  # 接收人列表，逗号分隔
     *   interval: 5           # 通知间隔，单位分钟
     * </pre>
     * 
     * <p><b>作用：</b>
     * <ul>
     *   <li>配置变更时发送钉钉/邮件通知</li>
     *   <li>通知相关人员关注配置变更</li>
     * </ul>
     * 
     * <p><b>注意：</b>
     * <ul>
     *   <li>如果未配置，变更通知功能可能不生效</li>
     *   <li>接收人格式需要与通知平台（钉钉）的要求一致</li>
     * </ul>
     * 
     * @see NotifyConfig 通知配置内嵌类
     */
    private NotifyConfig notify;

    /**
     * 告警配置
     * <p>
     * 配置线程池运行状态告警的规则，包括是否启用告警、队列阈值和活跃线程阈值。
     * 
     * <p><b>配置示例：</b>
     * <pre>
     * alarm:
     *   enable: true           # 是否启用告警
     *   queue-threshold: 80    # 队列使用率阈值（百分比）
     *   active-threshold: 80   # 活跃线程率阈值（百分比）
     * </pre>
     * 
     * <p><b>默认值：</b>默认启用告警，阈值均为 80%
     * 
     * <p><b>作用：</b>
     * <ul>
     *   <li>当队列使用率超过阈值时，触发告警</li>
     *   <li>当活跃线程率超过阈值时，触发告警</li>
     *   <li>帮助及时发现线程池容量不足的问题</li>
     * </ul>
     * 
     * <p><b>告警触发条件：</b>
     * <ul>
     *   <li>队列使用率 = (队列当前大小 / 队列容量) * 100 > queueThreshold</li>
     *   <li>活跃线程率 = (活跃线程数 / 最大线程数) * 100 > activeThreshold</li>
     * </ul>
     * 
     * @see AlarmConfig 告警配置内嵌类
     */
    private AlarmConfig alarm = new AlarmConfig();

    /**
     * 通知配置内嵌类
     * <p>
     * 封装了线程池参数变更通知的相关配置，包括接收人列表和通知间隔。
     * 
     * <p><b>配置说明：</b>
     * <ul>
     *   <li><b>receives：</b>接收通知的人员列表，多个人用逗号分隔</li>
     *   <li><b>interval：</b>通知间隔时间，避免频繁通知骚扰，单位为分钟</li>
     * </ul>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * NotifyConfig notify = new NotifyConfig();
     * notify.setReceives("张三,李四,王五");  // 三个接收人
     * notify.setInterval(5);                // 5分钟通知一次
     * 
     * properties.setNotify(notify);
     * }</pre>
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotifyConfig {

        /**
         * 接收人集合
         * <p>
         * 配置变更通知的接收人列表，多个接收人使用逗号分隔。
         * 
         * <p><b>格式说明：</b>
         * <ul>
         *   <li>多个接收人用逗号（,）分隔，如 "张三,李四,王五"</li>
         *   <li>钉钉通知：可以使用手机号或钉钉用户ID</li>
         *   <li>邮件通知：使用邮箱地址</li>
         * </ul>
         * 
         * <p><b>配置示例：</b>
         * <pre>
         * receives: "13800138000,13900139000"  # 钉钉手机号
         * receives: "zhangsan@company.com"     # 邮箱地址
         * receives: "张三,李四"                 # 钉钉姓名（需要@功能支持）
         * </pre>
         * 
         * <p><b>注意：</b>
         * <ul>
         *   <li>接收人格式需要与通知平台的要求匹配</li>
         *   <li>如果为空，可能不发送通知</li>
         *   <li>长度不应超过 64 个字符（防止配置过长）</li>
         * </ul>
         */
        private String receives;

        /**
         * 告警间隔时间（单位：分钟）
         * <p>
         * 配置变更通知的最小间隔时间，用于防止频繁通知骚扰。
         * 
         * <p><b>配置示例：</b>{@code interval: 5}（5分钟）
         * 
         * <p><b>默认值：</b>5分钟
         * 
         * <p><b>作用：</b>
         * <ul>
         *   <li>限制通知频率，避免在短时间内多次变更导致频繁通知</li>
         *   <li>同一个线程池在间隔时间内只会发送一次通知</li>
         * </ul>
         * 
         * <p><b>配置建议：</b>
         * <ul>
         *   <li>开发环境：1~2分钟（快速感知变更）</li>
         *   <li>生产环境：5~10分钟（避免频繁骚扰）</li>
         * </ul>
         * 
         * <p><b>注意：</b>
         * <ul>
         *   <li>取值范围建议为 1~30 分钟</li>
         *   <li>间隔过短可能导致通知风暴</li>
         *   <li>间隔过长可能错过重要变更</li>
         * </ul>
         */
        private Integer interval = 5;
    }

    /**
     * 告警配置内嵌类
     * <p>
     * 封装了线程池运行状态告警的相关配置，包括是否启用告警、
     * 队列使用率阈值和活跃线程率阈值。
     * 
     * <p><b>告警机制：</b>
     * <ul>
     *   <li><b>定时检查：</b>定时任务（如每分钟）检查所有线程池的状态</li>
     *   <li><b>阈值对比：</b>将实际使用率与配置的阈值对比</li>
     *   <li><b>触发告警：</b>超过阈值时发送钉钉告警消息</li>
     *   <li><b>限流机制：</b>同一线程池在间隔时间内只告警一次</li>
     * </ul>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * AlarmConfig alarm = new AlarmConfig();
     * alarm.setEnable(true);          // 启用告警
     * alarm.setQueueThreshold(80);    // 队列使用率超过80%告警
     * alarm.setActiveThreshold(80);   // 活跃线程率超过80%告警
     * 
     * properties.setAlarm(alarm);
     * }</pre>
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlarmConfig {

        /**
         * 是否启用告警
         * <p>
         * 控制是否对该线程池进行状态监控和告警。
         * 
         * <p><b>配置示例：</b>{@code enable: true}
         * 
         * <p><b>默认值：</b>true（默认开启告警）
         * 
         * <p><b>作用：</b>
         * <ul>
         *   <li>true：启用告警，定时检查线程池状态</li>
         *   <li>false：禁用告警，不进行状态检查</li>
         * </ul>
         * 
         * <p><b>使用场景：</b>
         * <ul>
         *   <li>重要线程池：设为 true，及时发现问题</li>
         *   <li>测试线程池：设为 false，避免干扰</li>
         * </ul>
         */
        private Boolean enable = Boolean.TRUE;

        /**
         * 队列使用率告警阈值（百分比）
         * <p>
         * 当队列使用率超过该阈值时，触发告警。
         * 
         * <p><b>配置示例：</b>{@code queue-threshold: 80}（80%）
         * 
         * <p><b>默认值：</b>80（即 80%）
         * 
         * <p><b>计算公式：</b>
         * <pre>
         * 队列使用率 = (当前队列大小 / 队列容量) × 100
         * 
         * 例如：队列容量 = 100，当前队列大小 = 85
         *      队列使用率 = (85 / 100) × 100 = 85%
         *      如果阈值为 80，则触发告警
         * </pre>
         * 
         * <p><b>告警含义：</b>
         * <ul>
         *   <li>队列堆积严重，任务处理速度跟不上提交速度</li>
         *   <li>可能导致任务积压、响应时间变长</li>
         *   <li>接近触发拒绝策略的临界点</li>
         * </ul>
         * 
         * <p><b>配置建议：</b>
         * <ul>
         *   <li>敏感业务：60~70%（提前预警）</li>
         *   <li>一般业务：80%（平衡预警和误报）</li>
         *   <li>容忍堆积：90%（仅在临界时告警）</li>
         * </ul>
         * 
         * <p><b>取值范围：</b>0~100（通常设置为 50~90）
         * 
         * <p><b>处理建议：</b>
         * <ul>
         *   <li>增加最大线程数，提高并发处理能力</li>
         *   <li>增加队列容量，提供更多缓冲空间</li>
         *   <li>优化任务处理逻辑，提升处理速度</li>
         *   <li>考虑限流，控制任务提交速度</li>
         * </ul>
         */
        private Integer queueThreshold = 80;

        /**
         * 活跃线程率告警阈值（百分比）
         * <p>
         * 当活跃线程率超过该阈值时，触发告警。
         * 
         * <p><b>配置示例：</b>{@code active-threshold: 80}（80%）
         * 
         * <p><b>默认值：</b>80（即 80%）
         * 
         * <p><b>计算公式：</b>
         * <pre>
         * 活跃线程率 = (活跃线程数 / 最大线程数) × 100
         * 
         * 例如：最大线程数 = 20，活跃线程数 = 18
         *      活跃线程率 = (18 / 20) × 100 = 90%
         *      如果阈值为 80，则触发告警
         * </pre>
         * 
         * <p><b>告警含义：</b>
         * <ul>
         *   <li>线程池接近满载，处理能力即将达到上限</li>
         *   <li>可能即将触发拒绝策略</li>
         *   <li>系统负载较高，需要关注</li>
         * </ul>
         * 
         * <p><b>配置建议：</b>
         * <ul>
         *   <li>保守配置：60~70%（提前预警，留足缓冲）</li>
         *   <li>标准配置：80%（平衡预警和误报）</li>
         *   <li>激进配置：90%（仅在临界时告警）</li>
         * </ul>
         * 
         * <p><b>取值范围：</b>0~100（通常设置为 60~90）
         * 
         * <p><b>处理建议：</b>
         * <ul>
         *   <li>增加最大线程数，提供更多处理能力</li>
         *   <li>增加核心线程数，减少线程创建开销</li>
         *   <li>优化任务逻辑，减少执行时间</li>
         *   <li>考虑扩容或限流</li>
         * </ul>
         * 
         * <p><b>与队列阈值的区别：</b>
         * <table border="1">
         *   <tr><th>指标</th><th>含义</th><th>关注点</th></tr>
         *   <tr><td>队列阈值</td><td>任务堆积程度</td><td>任务等待队列的拥堵情况</td></tr>
         *   <tr><td>活跃线程阈值</td><td>线程繁忙程度</td><td>线程池的处理能力使用情况</td></tr>
         * </table>
         */
        private Integer activeThreshold = 80;
    }
}
