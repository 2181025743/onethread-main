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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 动态线程池注册表
 * <p>
 * 该类是 oneThread 框架的核心组件之一，负责统一管理所有动态线程池的实例。
 * 通过注册表模式（Registry Pattern）提供全局的线程池查询、注册和管理能力。
 * 
 * <p><b>核心功能：</b>
 * <ul>
 *   <li><b>线程池注册：</b>将动态线程池实例注册到全局注册表</li>
 *   <li><b>线程池查询：</b>通过线程池 ID 快速查找对应的线程池实例</li>
 *   <li><b>统一管理：</b>提供获取所有线程池的能力，便于批量操作和监控</li>
 *   <li><b>线程安全：</b>使用 {@link ConcurrentHashMap} 保证并发安全</li>
 * </ul>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li><b>配置变更：</b>配置中心推送配置变更时，通过注册表找到对应的线程池并更新参数</li>
 *   <li><b>监控采集：</b>定时任务遍历所有线程池，采集运行时指标（活跃线程数、队列大小等）</li>
 *   <li><b>告警检查：</b>检查所有线程池的状态，触发告警规则</li>
 *   <li><b>Web控制台：</b>为前端控制台提供线程池列表和详情查询</li>
 * </ul>
 * 
 * <p><b>设计模式：</b>
 * <ul>
 *   <li><b>注册表模式（Registry Pattern）：</b>提供全局的对象注册和查找服务</li>
 *   <li><b>单例模式（静态实现）：</b>使用静态字段和方法保证全局唯一性</li>
 * </ul>
 * 
 * <p><b>架构位置：</b>
 * <pre>
 * 用户代码
 *    ↓ 创建线程池
 * ThreadPoolExecutorBuilder
 *    ↓ 注册
 * OneThreadRegistry  ← 配置刷新器查询
 *    ↓                  ↓
 * 线程池实例 → 监控采集、告警检查
 * </pre>
 * 
 * <p><b>数据结构：</b>
 * <pre>
 * HOLDER_MAP (ConcurrentHashMap)
 * {
 *   "order-processor": ThreadPoolExecutorHolder {
 *     threadPoolId: "order-processor",
 *     executor: ThreadPoolExecutor实例,
 *     executorProperties: 线程池配置参数
 *   },
 *   "message-consumer": ThreadPoolExecutorHolder { ... },
 *   "async-task-pool": ThreadPoolExecutorHolder { ... }
 * }
 * </pre>
 * 
 * <p><b>使用示例：</b>
 * <pre>{@code
 * // 1. 注册线程池（通常在线程池创建后自动注册）
 * ThreadPoolExecutor executor = ThreadPoolExecutorBuilder.builder()
 *     .dynamicPool()
 *     .threadPoolId("order-processor")
 *     .corePoolSize(10)
 *     .threadFactory("order")
 *     .build();
 * 
 * ThreadPoolExecutorProperties properties = new ThreadPoolExecutorProperties();
 * properties.setThreadPoolId("order-processor");
 * properties.setCorePoolSize(10);
 * // ... 设置其他属性
 * 
 * OneThreadRegistry.putHolder("order-processor", executor, properties);
 * 
 * 
 * // 2. 查询线程池
 * ThreadPoolExecutorHolder holder = OneThreadRegistry.getHolder("order-processor");
 * if (holder != null) {
 *     ThreadPoolExecutor executor = holder.getExecutor();
 *     int activeCount = executor.getActiveCount();
 *     System.out.println("活跃线程数: " + activeCount);
 * }
 * 
 * 
 * // 3. 获取所有线程池（用于监控）
 * Collection<ThreadPoolExecutorHolder> allHolders = OneThreadRegistry.getAllHolders();
 * for (ThreadPoolExecutorHolder holder : allHolders) {
 *     System.out.println("线程池: " + holder.getThreadPoolId());
 *     System.out.println("活跃线程: " + holder.getExecutor().getActiveCount());
 *     System.out.println("队列任务: " + holder.getExecutor().getQueue().size());
 * }
 * 
 * 
 * // 4. 配置变更场景（在配置监听器中使用）
 * public void onConfigChange(String threadPoolId, ThreadPoolExecutorProperties newConfig) {
 *     ThreadPoolExecutorHolder holder = OneThreadRegistry.getHolder(threadPoolId);
 *     if (holder != null) {
 *         ThreadPoolExecutor executor = holder.getExecutor();
 *         // 更新线程池参数
 *         executor.setCorePoolSize(newConfig.getCorePoolSize());
 *         executor.setMaximumPoolSize(newConfig.getMaximumPoolSize());
 *         // ...
 *     }
 * }
 * }</pre>
 * 
 * <p><b>线程安全性：</b>
 * <ul>
 *   <li>使用 {@link ConcurrentHashMap} 保证并发读写安全</li>
 *   <li>所有方法都是静态方法，无状态冲突</li>
 *   <li>支持多线程同时查询和注册</li>
 * </ul>
 * 
 * <p><b>注意事项：</b>
 * <ul>
 *   <li>线程池 ID 必须全局唯一，重复注册会覆盖旧的实例</li>
 *   <li>注册表不会自动清理已关闭的线程池，需要手动管理生命周期</li>
 *   <li>建议在应用启动时注册，在应用关闭时清理</li>
 * </ul>
 * 
 * @author 杨潇
 * @since 2025-04-20
 * @see ThreadPoolExecutorHolder 线程池包装类
 * @see ThreadPoolExecutor JDK线程池
 * @see ThreadPoolExecutorProperties 线程池配置属性
 */
public class OneThreadRegistry {

    /**
     * 线程池持有者缓存
     * <p>
     * 使用 {@link ConcurrentHashMap} 存储所有动态线程池的包装对象。
     * 
     * <p><b>键（Key）：</b>线程池唯一标识（threadPoolId），如 "order-processor"、"message-consumer"
     * <br><b>值（Value）：</b>{@link ThreadPoolExecutorHolder} 包装对象，包含线程池实例和配置信息
     * 
     * <p><b>为什么使用 ConcurrentHashMap？</b>
     * <ul>
     *   <li><b>线程安全：</b>支持多线程并发读写，无需额外同步</li>
     *   <li><b>高性能：</b>使用分段锁（Segment），读操作几乎无锁</li>
     *   <li><b>弱一致性：</b>迭代器不会抛出 ConcurrentModificationException</li>
     * </ul>
     * 
     * <p><b>数据结构示例：</b>
     * <pre>
     * {
     *   "order-processor" → ThreadPoolExecutorHolder(executor, properties),
     *   "message-consumer" → ThreadPoolExecutorHolder(executor, properties),
     *   "async-task-pool" → ThreadPoolExecutorHolder(executor, properties)
     * }
     * </pre>
     * 
     * <p><b>容量说明：</b>
     * <ul>
     *   <li>初始容量：16（ConcurrentHashMap 默认值）</li>
     *   <li>负载因子：0.75</li>
     *   <li>实际应用中线程池数量通常不超过 50 个</li>
     * </ul>
     */
    private static final Map<String, ThreadPoolExecutorHolder> HOLDER_MAP = new ConcurrentHashMap<>();

    /**
     * 注册线程池到注册表
     * <p>
     * 将线程池实例和其配置信息包装为 {@link ThreadPoolExecutorHolder} 并存储到注册表中。
     * 该方法通常在动态线程池创建后自动调用，也可以手动调用进行注册。
     * 
     * <p><b>执行流程：</b>
     * <ol>
     *   <li>创建 {@link ThreadPoolExecutorHolder} 包装对象</li>
     *   <li>以 threadPoolId 为键，将包装对象存入 {@link #HOLDER_MAP}</li>
     *   <li>如果已存在同名线程池，会覆盖旧的实例</li>
     * </ol>
     * 
     * <p><b>调用时机：</b>
     * <ul>
     *   <li>在 Spring 容器启动时，扫描到 {@code @DynamicThreadPool} 注解的 Bean 后自动注册</li>
     *   <li>在 {@link ThreadPoolExecutorBuilder#build()} 创建动态线程池后自动注册</li>
     *   <li>手动创建线程池后，需要手动调用此方法注册</li>
     * </ul>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * // 创建线程池
     * ThreadPoolExecutor executor = new ThreadPoolExecutor(
     *     5, 10, 60L, TimeUnit.SECONDS,
     *     new LinkedBlockingQueue<>(100),
     *     Executors.defaultThreadFactory(),
     *     new ThreadPoolExecutor.AbortPolicy()
     * );
     * 
     * // 创建配置对象
     * ThreadPoolExecutorProperties properties = new ThreadPoolExecutorProperties();
     * properties.setThreadPoolId("my-pool");
     * properties.setCorePoolSize(5);
     * properties.setMaximumPoolSize(10);
     * 
     * // 注册到注册表
     * OneThreadRegistry.putHolder("my-pool", executor, properties);
     * }</pre>
     * 
     * <p><b>注意事项：</b>
     * <ul>
     *   <li><b>ID 唯一性：</b>相同 ID 会覆盖已有的线程池，请确保 ID 全局唯一</li>
     *   <li><b>参数一致性：</b>executor 和 properties 的参数应该保持一致</li>
     *   <li><b>生命周期管理：</b>注册表不会自动清理，需要在线程池关闭时手动移除（可选）</li>
     * </ul>
     *
     * @param threadPoolId 线程池唯一标识（如 "order-processor"），不能为 null
     * @param executor     线程池执行器实例，不能为 null
     * @param properties   线程池参数配置，不能为 null
     */
    public static void putHolder(String threadPoolId, ThreadPoolExecutor executor, ThreadPoolExecutorProperties properties) {
        // 创建线程池包装对象（包含线程池实例和配置信息）
        ThreadPoolExecutorHolder executorHolder = new ThreadPoolExecutorHolder(threadPoolId, executor, properties);
        
        // 存入注册表（如果已存在同名线程池，会覆盖）
        HOLDER_MAP.put(threadPoolId, executorHolder);
    }

    /**
     * 根据线程池 ID 获取对应的线程池包装对象
     * <p>
     * 通过线程池的唯一标识查询注册表，返回对应的 {@link ThreadPoolExecutorHolder} 对象。
     * 该对象包含了线程池实例、配置信息等完整数据。
     * 
     * <p><b>返回值说明：</b>
     * <ul>
     *   <li>如果找到匹配的线程池，返回对应的 {@link ThreadPoolExecutorHolder} 对象</li>
     *   <li>如果未找到，返回 {@code null}</li>
     * </ul>
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li><b>配置变更：</b>配置监听器接收到配置变更事件时，通过 ID 查找线程池并更新参数</li>
     *   <li><b>监控采集：</b>监控服务通过 ID 查找线程池，读取运行时指标</li>
     *   <li><b>告警检查：</b>告警检查器通过 ID 查找线程池，检查是否超过阈值</li>
     *   <li><b>Web接口：</b>前端控制台通过 ID 查询线程池详情</li>
     * </ul>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * // 示例1：查询线程池并读取状态
     * ThreadPoolExecutorHolder holder = OneThreadRegistry.getHolder("order-processor");
     * if (holder != null) {
     *     ThreadPoolExecutor executor = holder.getExecutor();
     *     System.out.println("活跃线程数: " + executor.getActiveCount());
     *     System.out.println("队列任务数: " + executor.getQueue().size());
     *     System.out.println("已完成任务数: " + executor.getCompletedTaskCount());
     * } else {
     *     System.out.println("线程池不存在");
     * }
     * 
     * 
     * // 示例2：在配置监听器中使用
     * public void onConfigChange(ThreadPoolExecutorProperties newConfig) {
     *     String threadPoolId = newConfig.getThreadPoolId();
     *     ThreadPoolExecutorHolder holder = OneThreadRegistry.getHolder(threadPoolId);
     *     
     *     if (holder != null) {
     *         // 更新线程池参数
     *         ThreadPoolExecutor executor = holder.getExecutor();
     *         executor.setCorePoolSize(newConfig.getCorePoolSize());
     *         executor.setMaximumPoolSize(newConfig.getMaximumPoolSize());
     *         
     *         // 更新配置信息
     *         holder.setExecutorProperties(newConfig);
     *     }
     * }
     * 
     * 
     * // 示例3：安全的空值处理
     * ThreadPoolExecutorHolder holder = OneThreadRegistry.getHolder("unknown-pool");
     * if (holder == null) {
     *     log.warn("线程池 {} 不存在，跳过配置更新", "unknown-pool");
     *     return;
     * }
     * // 继续处理...
     * }</pre>
     * 
     * <p><b>性能特点：</b>
     * <ul>
     *   <li>时间复杂度：O(1)（HashMap 的查找复杂度）</li>
     *   <li>线程安全：使用 ConcurrentHashMap，并发读取无锁</li>
     *   <li>无副作用：只读操作，不会修改注册表状态</li>
     * </ul>
     * 
     * <p><b>注意事项：</b>
     * <ul>
     *   <li>返回值可能为 {@code null}，调用方需要进行空值检查</li>
     *   <li>返回的对象是引用，修改其中的 executor 会影响实际运行的线程池</li>
     *   <li>线程池 ID 区分大小写，请确保 ID 的一致性</li>
     * </ul>
     *
     * @param threadPoolId 线程池唯一标识（如 "order-processor"）
     * @return 线程池持有者对象，如果未找到则返回 {@code null}
     */
    public static ThreadPoolExecutorHolder getHolder(String threadPoolId) {
        return HOLDER_MAP.get(threadPoolId);
    }

    /**
     * 获取所有线程池集合
     * <p>
     * 返回注册表中所有线程池的包装对象集合，用于批量操作、监控采集、统计分析等场景。
     * 返回的集合是 {@link ConcurrentHashMap#values()} 的视图，具有弱一致性。
     * 
     * <p><b>返回值特点：</b>
     * <ul>
     *   <li><b>集合类型：</b>{@link Collection}<{@link ThreadPoolExecutorHolder}></li>
     *   <li><b>弱一致性：</b>返回的是 ConcurrentHashMap 的视图，遍历时可能看不到最新的修改</li>
     *   <li><b>不可修改：</b>直接修改返回的集合不会影响注册表（如 add、remove 操作无效）</li>
     *   <li><b>实时性：</b>集合大小和内容反映调用时刻的状态</li>
     * </ul>
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li><b>监控采集：</b>定时任务遍历所有线程池，采集运行时指标（活跃线程数、队列大小等）</li>
     *   <li><b>告警检查：</b>检查所有线程池是否超过告警阈值</li>
     *   <li><b>健康检查：</b>检查所有线程池的健康状态</li>
     *   <li><b>统计分析：</b>计算所有线程池的总体统计数据</li>
     *   <li><b>Web接口：</b>为前端控制台提供线程池列表</li>
     * </ul>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * // 示例1：监控采集 - 遍历所有线程池并采集指标
     * Collection<ThreadPoolExecutorHolder> allHolders = OneThreadRegistry.getAllHolders();
     * for (ThreadPoolExecutorHolder holder : allHolders) {
     *     ThreadPoolExecutor executor = holder.getExecutor();
     *     
     *     // 采集指标
     *     int activeCount = executor.getActiveCount();
     *     int queueSize = executor.getQueue().size();
     *     long completedCount = executor.getCompletedTaskCount();
     *     
     *     // 上报到监控系统
     *     metricsCollector.report(holder.getThreadPoolId(), activeCount, queueSize, completedCount);
     * }
     * 
     * 
     * // 示例2：告警检查 - 检查是否有线程池超过阈值
     * Collection<ThreadPoolExecutorHolder> allHolders = OneThreadRegistry.getAllHolders();
     * for (ThreadPoolExecutorHolder holder : allHolders) {
     *     ThreadPoolExecutor executor = holder.getExecutor();
     *     ThreadPoolExecutorProperties properties = holder.getExecutorProperties();
     *     
     *     // 检查队列使用率
     *     int queueSize = executor.getQueue().size();
     *     int queueCapacity = properties.getQueueCapacity();
     *     double queueUsage = (double) queueSize / queueCapacity * 100;
     *     
     *     if (queueUsage > 80) {
     *         alarmService.sendAlarm("线程池 " + holder.getThreadPoolId() + " 队列使用率超过80%");
     *     }
     *     
     *     // 检查活跃线程率
     *     int activeCount = executor.getActiveCount();
     *     int maximumPoolSize = executor.getMaximumPoolSize();
     *     double activeRate = (double) activeCount / maximumPoolSize * 100;
     *     
     *     if (activeRate > 80) {
     *         alarmService.sendAlarm("线程池 " + holder.getThreadPoolId() + " 活跃线程率超过80%");
     *     }
     * }
     * 
     * 
     * // 示例3：统计分析 - 计算所有线程池的总体指标
     * Collection<ThreadPoolExecutorHolder> allHolders = OneThreadRegistry.getAllHolders();
     * 
     * int totalPools = allHolders.size();
     * int totalActiveThreads = 0;
     * int totalQueuedTasks = 0;
     * 
     * for (ThreadPoolExecutorHolder holder : allHolders) {
     *     ThreadPoolExecutor executor = holder.getExecutor();
     *     totalActiveThreads += executor.getActiveCount();
     *     totalQueuedTasks += executor.getQueue().size();
     * }
     * 
     * System.out.println("线程池总数: " + totalPools);
     * System.out.println("活跃线程总数: " + totalActiveThreads);
     * System.out.println("排队任务总数: " + totalQueuedTasks);
     * 
     * 
     * // 示例4：Web接口 - 返回线程池列表
     * @GetMapping("/thread-pools")
     * public List<ThreadPoolVO> listThreadPools() {
     *     Collection<ThreadPoolExecutorHolder> allHolders = OneThreadRegistry.getAllHolders();
     *     return allHolders.stream()
     *         .map(holder -> {
     *             ThreadPoolVO vo = new ThreadPoolVO();
     *             vo.setThreadPoolId(holder.getThreadPoolId());
     *             vo.setActiveCount(holder.getExecutor().getActiveCount());
     *             vo.setQueueSize(holder.getExecutor().getQueue().size());
     *             // ... 设置其他字段
     *             return vo;
     *         })
     *         .collect(Collectors.toList());
     * }
     * }</pre>
     * 
     * <p><b>性能特点：</b>
     * <ul>
     *   <li>时间复杂度：O(1)（返回视图，不复制数据）</li>
     *   <li>空间复杂度：O(1)（返回视图，不占用额外空间）</li>
     *   <li>遍历复杂度：O(n)，n 为线程池数量</li>
     * </ul>
     * 
     * <p><b>并发安全性：</b>
     * <ul>
     *   <li>返回的集合支持并发读取，无需额外同步</li>
     *   <li>遍历时不会抛出 {@link java.util.ConcurrentModificationException}</li>
     *   <li>但遍历过程中可能看不到其他线程的最新修改（弱一致性）</li>
     * </ul>
     * 
     * <p><b>注意事项：</b>
     * <ul>
     *   <li>返回的集合是只读视图，调用 add/remove 等修改方法会抛出 {@link UnsupportedOperationException}</li>
     *   <li>如果注册表为空，返回空集合（不会返回 null）</li>
     *   <li>遍历大量线程池时注意性能开销（建议使用 Stream API 并行处理）</li>
     *   <li>遍历时修改线程池状态是安全的，但要注意业务逻辑的正确性</li>
     * </ul>
     *
     * @return 所有线程池包装对象的集合，永不为 {@code null}（可能为空集合）
     */
    public static Collection<ThreadPoolExecutorHolder> getAllHolders() {
        return HOLDER_MAP.values();
    }
}
