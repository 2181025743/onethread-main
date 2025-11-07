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
import lombok.Data;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池执行器持有者对象（包装类）
 * <p>
 * 该类是线程池的包装器（Wrapper），将线程池实例和其配置信息组合在一起，
 * 便于统一管理和操作。这是一个简单的数据传输对象（DTO），用于在
 * {@link OneThreadRegistry} 中存储线程池的完整信息。
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li><b>数据封装：</b>将线程池实例和配置信息封装为一个对象</li>
 *   <li><b>统一管理：</b>便于在注册表中统一存储和查询</li>
 *   <li><b>配置追踪：</b>保存线程池的配置信息，方便对比配置变更</li>
 *   <li><b>完整上下文：</b>提供线程池的完整上下文信息（实例 + 配置 + ID）</li>
 * </ul>
 * 
 * <p><b>为什么需要包装类？</b>
 * <ul>
 *   <li><b>配置对比：</b>配置变更时需要对比新旧配置，包装类保存了原始配置</li>
 *   <li><b>标识绑定：</b>将线程池 ID 与线程池实例绑定，避免分散管理</li>
 *   <li><b>元数据保存：</b>除了线程池实例，还需要保存告警配置、通知配置等元数据</li>
 *   <li><b>类型安全：</b>通过对象封装避免使用多个 Map 分别存储不同信息</li>
 * </ul>
 * 
 * <p><b>数据结构：</b>
 * <pre>
 * ThreadPoolExecutorHolder {
 *   threadPoolId: "order-processor"          // 唯一标识
 *   executor: ThreadPoolExecutor实例          // 线程池实例
 *   executorProperties: {                     // 配置信息
 *     corePoolSize: 10,
 *     maximumPoolSize: 20,
 *     queueCapacity: 100,
 *     alarm: { enable: true, queueThreshold: 80 },
 *     notify: { receives: "张三", interval: 5 }
 *   }
 * }
 * </pre>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li><b>注册表存储：</b>在 {@link OneThreadRegistry} 中作为存储单元</li>
 *   <li><b>配置变更：</b>对比新旧配置，判断是否需要更新线程池</li>
 *   <li><b>监控采集：</b>同时获取线程池实例和配置信息，便于数据上报</li>
 *   <li><b>告警检查：</b>根据配置的阈值检查线程池状态</li>
 * </ul>
 * 
 * <p><b>设计模式：</b>
 * <ul>
 *   <li><b>组合模式（Composite Pattern）：</b>将多个相关对象组合为一个整体</li>
 *   <li><b>包装器模式（Wrapper Pattern）：</b>对线程池进行包装，增加额外信息</li>
 * </ul>
 * 
 * <p><b>使用示例：</b>
 * <pre>{@code
 * // 示例1：创建线程池并包装
 * ThreadPoolExecutor executor = ThreadPoolExecutorBuilder.builder()
 *     .threadPoolId("order-processor")
 *     .corePoolSize(10)
 *     .threadFactory("order")
 *     .build();
 * 
 * ThreadPoolExecutorProperties properties = new ThreadPoolExecutorProperties();
 * properties.setThreadPoolId("order-processor");
 * properties.setCorePoolSize(10);
 * properties.setMaximumPoolSize(20);
 * 
 * ThreadPoolExecutorHolder holder = new ThreadPoolExecutorHolder(
 *     "order-processor",
 *     executor,
 *     properties
 * );
 * 
 * 
 * // 示例2：从注册表获取并使用
 * ThreadPoolExecutorHolder holder = OneThreadRegistry.getHolder("order-processor");
 * 
 * // 获取线程池实例
 * ThreadPoolExecutor executor = holder.getExecutor();
 * executor.execute(task);
 * 
 * // 获取配置信息
 * ThreadPoolExecutorProperties properties = holder.getExecutorProperties();
 * Integer corePoolSize = properties.getCorePoolSize();
 * 
 * // 获取线程池ID
 * String threadPoolId = holder.getThreadPoolId();
 * 
 * 
 * // 示例3：配置变更对比
 * ThreadPoolExecutorHolder holder = OneThreadRegistry.getHolder("order-processor");
 * ThreadPoolExecutorProperties oldConfig = holder.getExecutorProperties();
 * ThreadPoolExecutorProperties newConfig = parseFromConfigCenter();
 * 
 * // 对比配置是否变更
 * if (!oldConfig.getCorePoolSize().equals(newConfig.getCorePoolSize())) {
 *     // 核心线程数发生变更，更新线程池
 *     holder.getExecutor().setCorePoolSize(newConfig.getCorePoolSize());
 *     // 更新配置信息
 *     holder.setExecutorProperties(newConfig);
 * }
 * }</pre>
 * 
 * <p><b>线程安全性：</b>
 * <ul>
 *   <li>该类本身是一个简单的数据对象，不是线程安全的</li>
 *   <li>但 {@link #executor} 字段指向的 {@link ThreadPoolExecutor} 是线程安全的</li>
 *   <li>在 {@link OneThreadRegistry} 中通过 {@link java.util.concurrent.ConcurrentHashMap} 保证注册表的线程安全</li>
 *   <li>修改 {@link #executorProperties} 时需要注意并发安全</li>
 * </ul>
 * 
 * <p><b>最佳实践：</b>
 * <ul>
 *   <li>通过 {@link OneThreadRegistry} 统一管理，避免直接创建此对象</li>
 *   <li>配置变更后，及时更新 {@link #executorProperties} 字段</li>
 *   <li>三个字段应该保持一致性（ID 匹配，配置与实例一致）</li>
 * </ul>
 * 
 * @author 杨潇
 * @since 2025-04-20
 * @see OneThreadRegistry 线程池注册表
 * @see ThreadPoolExecutor JDK线程池
 * @see ThreadPoolExecutorProperties 线程池配置属性
 */
@Data
@AllArgsConstructor
public class ThreadPoolExecutorHolder {

    /**
     * 线程池唯一标识
     * <p>
     * 用于在全局范围内唯一标识一个线程池实例，作为注册表的查找键。
     * 
     * <p><b>命名规范：</b>
     * <ul>
     *   <li>使用短横线分隔的小写字母，如 "order-processor"</li>
     *   <li>体现业务含义，如 "message-consumer"、"async-task-pool"</li>
     *   <li>避免使用特殊字符和空格</li>
     *   <li>长度建议不超过 50 个字符</li>
     * </ul>
     * 
     * <p><b>用途：</b>
     * <ul>
     *   <li>配置中心配置项匹配（如 Nacos 中的 thread-pool-id）</li>
     *   <li>注册表查找键（{@link OneThreadRegistry#getHolder(String)}）</li>
     *   <li>日志记录和监控标识</li>
     *   <li>告警消息中的线程池标识</li>
     * </ul>
     * 
     * <p><b>注意：</b>该值必须与 {@link #executorProperties} 中的 threadPoolId 保持一致。
     */
    private String threadPoolId;

    /**
     * 线程池执行器实例
     * <p>
     * 实际的线程池对象，可以是 JDK 标准的 {@link ThreadPoolExecutor}，
     * 也可以是 oneThread 的 {@link OneThreadExecutor}。
     * 
     * <p><b>作用：</b>
     * <ul>
     *   <li><b>任务提交：</b>通过 {@link ThreadPoolExecutor#execute(Runnable)} 提交任务</li>
     *   <li><b>参数调整：</b>通过 setter 方法动态调整线程池参数</li>
     *   <li><b>状态查询：</b>获取活跃线程数、队列大小等运行时状态</li>
     *   <li><b>生命周期管理：</b>关闭和销毁线程池</li>
     * </ul>
     * 
     * <p><b>常用操作：</b>
     * <pre>{@code
     * // 获取运行时状态
     * int activeCount = executor.getActiveCount();           // 活跃线程数
     * int poolSize = executor.getPoolSize();                 // 当前线程数
     * int queueSize = executor.getQueue().size();            // 队列任务数
     * long completedCount = executor.getCompletedTaskCount(); // 已完成任务数
     * 
     * // 动态调整参数
     * executor.setCorePoolSize(newCoreSize);
     * executor.setMaximumPoolSize(newMaxSize);
     * executor.setKeepAliveTime(newKeepAlive, TimeUnit.SECONDS);
     * 
     * // 提交任务
     * executor.execute(() -> processTask());
     * }</pre>
     * 
     * <p><b>线程安全性：</b>{@link ThreadPoolExecutor} 是线程安全的，可以在多线程环境中安全使用。
     */
    private ThreadPoolExecutor executor;

    /**
     * 线程池属性参数配置
     * <p>
     * 保存了线程池的所有配置参数，包括核心线程数、最大线程数、队列容量、
     * 告警配置、通知配置等。这些参数可能来自配置中心或本地配置文件。
     * 
     * <p><b>用途：</b>
     * <ul>
     *   <li><b>配置对比：</b>配置变更时对比新旧配置，判断哪些参数发生了变化</li>
     *   <li><b>配置同步：</b>将最新的配置信息同步回配置中心</li>
     *   <li><b>告警阈值：</b>提供告警检查所需的阈值配置</li>
     *   <li><b>通知配置：</b>提供发送通知所需的接收人和间隔配置</li>
     *   <li><b>元数据保存：</b>保存队列类型、拒绝策略等元数据</li>
     * </ul>
     * 
     * <p><b>配置示例：</b>
     * <pre>{@code
     * ThreadPoolExecutorProperties properties = new ThreadPoolExecutorProperties();
     * properties.setThreadPoolId("order-processor");
     * properties.setCorePoolSize(10);
     * properties.setMaximumPoolSize(20);
     * properties.setQueueCapacity(100);
     * properties.setWorkQueue("LinkedBlockingQueue");
     * properties.setRejectedHandler("CallerRunsPolicy");
     * properties.setKeepAliveTime(60L);
     * properties.setAllowCoreThreadTimeOut(false);
     * 
     * // 设置告警配置
     * ThreadPoolExecutorProperties.AlarmConfig alarm = new ThreadPoolExecutorProperties.AlarmConfig();
     * alarm.setEnable(true);
     * alarm.setQueueThreshold(80);
     * alarm.setActiveThreshold(80);
     * properties.setAlarm(alarm);
     * 
     * // 设置通知配置
     * ThreadPoolExecutorProperties.NotifyConfig notify = new ThreadPoolExecutorProperties.NotifyConfig();
     * notify.setReceives("张三,李四");
     * notify.setInterval(5);
     * properties.setNotify(notify);
     * }</pre>
     * 
     * <p><b>配置来源：</b>
     * <ul>
     *   <li>从配置中心（Nacos/Apollo）拉取的配置</li>
     *   <li>从本地配置文件（application.yml）读取的配置</li>
     *   <li>从前端控制台接收的配置变更</li>
     * </ul>
     * 
     * <p><b>注意：</b>
     * <ul>
     *   <li>该配置对象应该与 {@link #executor} 的实际参数保持一致</li>
     *   <li>配置变更后，需要同时更新此对象和 executor 实例</li>
     *   <li>该对象不是线程安全的，修改时需要注意并发问题</li>
     * </ul>
     * 
     * @author 杨潇
 * @since 2025-04-20
     * @see ThreadPoolExecutorProperties 线程池配置属性类
     * @see OneThreadRegistry 线程池注册表
     * @see ThreadPoolExecutor JDK线程池
     */
@Data
@AllArgsConstructor
public class ThreadPoolExecutorHolder {

    /**
     * 线程池唯一标识
     * <p>
     * 用于在全局范围内唯一标识一个线程池实例，作为注册表的查找键。
     * 
     * <p><b>命名规范：</b>
     * <ul>
     *   <li>使用短横线分隔的小写字母，如 "order-processor"</li>
     *   <li>体现业务含义，如 "message-consumer"、"async-task-pool"</li>
     *   <li>避免使用特殊字符和空格</li>
     *   <li>长度建议不超过 50 个字符</li>
     * </ul>
     * 
     * <p><b>用途：</b>
     * <ul>
     *   <li>配置中心配置项匹配（如 Nacos 中的 thread-pool-id）</li>
     *   <li>注册表查找键（{@link OneThreadRegistry#getHolder(String)}）</li>
     *   <li>日志记录和监控标识</li>
     *   <li>告警消息中的线程池标识</li>
     * </ul>
     * 
     * <p><b>注意：</b>该值必须与 {@link #executorProperties} 中的 threadPoolId 保持一致。
     */
    private String threadPoolId;

    /**
     * 线程池执行器实例
     * <p>
     * 实际的线程池对象，可以是 JDK 标准的 {@link ThreadPoolExecutor}，
     * 也可以是 oneThread 的 {@link OneThreadExecutor}。
     * 
     * <p><b>作用：</b>
     * <ul>
     *   <li><b>任务提交：</b>通过 {@link ThreadPoolExecutor#execute(Runnable)} 提交任务</li>
     *   <li><b>参数调整：</b>通过 setter 方法动态调整线程池参数</li>
     *   <li><b>状态查询：</b>获取活跃线程数、队列大小等运行时状态</li>
     *   <li><b>生命周期管理：</b>关闭和销毁线程池</li>
     * </ul>
     * 
     * <p><b>常用操作：</b>
     * <pre>{@code
     * // 获取运行时状态
     * int activeCount = executor.getActiveCount();           // 活跃线程数
     * int poolSize = executor.getPoolSize();                 // 当前线程数
     * int queueSize = executor.getQueue().size();            // 队列任务数
     * long completedCount = executor.getCompletedTaskCount(); // 已完成任务数
     * 
     * // 动态调整参数
     * executor.setCorePoolSize(newCoreSize);
     * executor.setMaximumPoolSize(newMaxSize);
     * executor.setKeepAliveTime(newKeepAlive, TimeUnit.SECONDS);
     * 
     * // 提交任务
     * executor.execute(() -> processTask());
     * }</pre>
     * 
     * <p><b>线程安全性：</b>{@link ThreadPoolExecutor} 是线程安全的，可以在多线程环境中安全使用。
     */
    private ThreadPoolExecutor executor;

    /**
     * 线程池属性参数配置
     * <p>
     * 保存了线程池的所有配置参数，包括核心线程数、最大线程数、队列容量、
     * 告警配置、通知配置等。这些参数可能来自配置中心或本地配置文件。
     * 
     * <p><b>用途：</b>
     * <ul>
     *   <li><b>配置对比：</b>配置变更时对比新旧配置，判断哪些参数发生了变化</li>
     *   <li><b>配置同步：</b>将最新的配置信息同步回配置中心</li>
     *   <li><b>告警阈值：</b>提供告警检查所需的阈值配置</li>
     *   <li><b>通知配置：</b>提供发送通知所需的接收人和间隔配置</li>
     *   <li><b>元数据保存：</b>保存队列类型、拒绝策略等元数据</li>
     * </ul>
     * 
     * <p><b>配置来源：</b>
     * <ul>
     *   <li>从配置中心（Nacos/Apollo）拉取的配置</li>
     *   <li>从本地配置文件（application.yml）读取的配置</li>
     *   <li>从前端控制台接收的配置变更</li>
     * </ul>
     * 
     * <p><b>注意：</b>
     * <ul>
     *   <li>该配置对象应该与 {@link #executor} 的实际参数保持一致</li>
     *   <li>配置变更后，需要同时更新此对象和 executor 实例</li>
     *   <li>该对象不是线程安全的，修改时需要注意并发问题</li>
     * </ul>
     * 
     * @see ThreadPoolExecutorProperties 线程池配置属性类
     */
    private ThreadPoolExecutorProperties executorProperties;
}
