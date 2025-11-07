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

package com.nageoffer.onethread.core.monitor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson2.JSON;
import com.nageoffer.onethread.core.config.ApplicationProperties;
import com.nageoffer.onethread.core.config.BootstrapConfigProperties;
import com.nageoffer.onethread.core.executor.OneThreadExecutor;
import com.nageoffer.onethread.core.executor.OneThreadRegistry;
import com.nageoffer.onethread.core.executor.ThreadPoolExecutorHolder;
import com.nageoffer.onethread.core.toolkit.ThreadFactoryBuilder;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池运行时监控器
 * <p>
 * 该类负责定时采集所有动态线程池的运行时状态数据，并根据配置选择不同的输出方式：
 * <ul>
 *   <li><b>日志方式（log）：</b>将监控数据以 JSON 格式输出到日志文件</li>
 *   <li><b>Micrometer方式（micrometer）：</b>将监控数据暴露为 Prometheus 指标，供 Grafana 展示</li>
 * </ul>
 * 
 * <p><b>核心功能：</b>
 * <ul>
 *   <li><b>定时采集：</b>每隔配置的时间间隔（默认10秒）采集一次所有线程池的状态</li>
 *   <li><b>多种输出：</b>支持日志和 Micrometer 两种监控数据输出方式</li>
 *   <li><b>增量计算：</b>计算任务完成数和拒绝次数的增量值</li>
 *   <li><b>Prometheus集成：</b>暴露为 Gauge 指标，支持 Grafana 可视化</li>
 * </ul>
 * 
 * <p><b>监控指标：</b>
 * <table border="1" cellpadding="5">
 *   <tr><th>类别</th><th>指标</th><th>含义</th></tr>
 *   <tr rowspan="5"><td><b>线程信息</b></td><td>core.size</td><td>核心线程数</td></tr>
 *   <tr><td>maximum.size</td><td>最大线程数</td></tr>
 *   <tr><td>current.size</td><td>当前线程数</td></tr>
 *   <tr><td>active.size</td><td>活跃线程数</td></tr>
 *   <tr><td>largest.size</td><td>历史最大线程数</td></tr>
 *   <tr rowspan="3"><td><b>队列信息</b></td><td>queue.size</td><td>队列当前大小</td></tr>
 *   <tr><td>queue.capacity</td><td>队列总容量</td></tr>
 *   <tr><td>queue.remaining.capacity</td><td>队列剩余容量</td></tr>
 *   <tr rowspan="2"><td><b>任务统计</b></td><td>completed.task.count</td><td>完成任务数增量</td></tr>
 *   <tr><td>reject.count</td><td>拒绝次数增量</td></tr>
 * </table>
 * 
 * <p><b>采集方式对比：</b>
 * <table border="1" cellpadding="5">
 *   <tr><th>方式</th><th>优点</th><th>缺点</th><th>适用场景</th></tr>
 *   <tr>
 *     <td>log（日志）</td>
 *     <td>简单，无依赖，易于调试</td>
 *     <td>数据分散，不易分析和可视化</td>
 *     <td>开发环境、问题排查</td>
 *   </tr>
 *   <tr>
 *     <td>micrometer（指标）</td>
 *     <td>标准化，支持 Prometheus/Grafana</td>
 *     <td>需要额外依赖和部署</td>
 *     <td>生产环境、系统监控</td>
 *   </tr>
 * </table>
 * 
 * <p><b>配置示例：</b>
 * <pre>
 * onethread:
 *   monitor:
 *     enable: true             # 启用监控
 *     collect-type: micrometer # 采集方式：log 或 micrometer
 *     collect-interval: 10     # 采集间隔（秒）
 * </pre>
 * 
 * <p><b>Prometheus 指标示例：</b>
 * <pre>
 * # 核心线程数
 * dynamic_thread_pool_core_size{
 *   dynamic_thread_pool_id="order-processor",
 *   application_name="order-service"
 * } 10
 * 
 * # 活跃线程数
 * dynamic_thread_pool_active_size{
 *   dynamic_thread_pool_id="order-processor",
 *   application_name="order-service"
 * } 8
 * 
 * # 队列大小
 * dynamic_thread_pool_queue_size{
 *   dynamic_thread_pool_id="order-processor",
 *   application_name="order-service"
 * } 350
 * 
 * # 完成任务数增量（过去10秒完成的任务数）
 * dynamic_thread_pool_completed_task_count{
 *   dynamic_thread_pool_id="order-processor",
 *   application_name="order-service"
 * } 500
 * </pre>
 * 
 * <p><b>Grafana 可视化示例：</b>
 * <pre>
 * 活跃线程率面板：
 *   查询：dynamic_thread_pool_active_size / dynamic_thread_pool_maximum_size * 100
 *   显示：折线图，Y轴为百分比
 * 
 * 队列使用率面板：
 *   查询：dynamic_thread_pool_queue_size / dynamic_thread_pool_queue_capacity * 100
 *   显示：仪表盘，超过80%变红
 * 
 * TPS（每秒任务数）面板：
 *   查询：rate(dynamic_thread_pool_completed_task_count[1m])
 *   显示：柱状图
 * </pre>
 * 
 * <p><b>使用示例：</b>
 * <pre>{@code
 * // 示例1：启动监控器
 * ThreadPoolMonitor monitor = new ThreadPoolMonitor();
 * monitor.start();  // 开始定时采集
 * 
 * // 应用运行期间，监控器会自动工作
 * 
 * 
 * // 示例2：在 Spring 中使用
 * @Component
 * public class MonitorManager {
 *     private ThreadPoolMonitor monitor;
 *     
 *     @PostConstruct
 *     public void init() {
 *         monitor = new ThreadPoolMonitor();
 *         monitor.start();
 *     }
 *     
 *     @PreDestroy
 *     public void destroy() {
 *         monitor.stop();
 *     }
 * }
 * 
 * 
 * // 示例3：查看日志输出（collect-type: log）
 * // 日志输出：
 * // [ThreadPool Monitor] order-processor | Content: {
 * //   "threadPoolId":"order-processor",
 * //   "corePoolSize":10,
 * //   "activePoolSize":8,
 * //   "workQueueSize":350,
 * //   "rejectCount":0
 * // }
 * }</pre>
 * 
 * <p><b>性能考虑：</b>
 * <ul>
 *   <li>采集间隔默认10秒，平衡实时性和性能</li>
 *   <li>某些线程池 API（如 getActiveCount）有锁，避免高频调用</li>
 *   <li>使用单线程调度器，避免并发采集的竞争</li>
 *   <li>增量计算使用 {@link DeltaWrapper}，性能开销小</li>
 * </ul>
 * 
 * <p><b>最佳实践：</b>
 * <ul>
 *   <li>生产环境使用 micrometer 方式，配合 Prometheus + Grafana</li>
 *   <li>开发环境使用 log 方式，便于调试</li>
 *   <li>合理设置采集间隔（5~30秒）</li>
 *   <li>监控关键指标：活跃线程率、队列使用率、拒绝次数</li>
 * </ul>
 * 
 * @author 杨潇
 * @since 2025-05-05
 * @see ThreadPoolRuntimeInfo 运行时信息实体
 * @see DeltaWrapper 增量计算包装器
 * @see BootstrapConfigProperties.MonitorConfig 监控配置
 * @see io.micrometer.core.instrument.Metrics Micrometer指标工具类
 */
@Slf4j
public class ThreadPoolMonitor {

    /**
     * 定时调度器
     * <p>
     * 使用单线程定时调度器，定期采集线程池状态。
     * 
     * <p><b>配置：</b>
     * <ul>
     *   <li>线程池大小：1（单线程）</li>
     *   <li>线程名称：scheduler_thread-pool_monitor</li>
     *   <li>调度方式：固定延迟</li>
     * </ul>
     */
    private ScheduledExecutorService scheduler;
    
    /**
     * Micrometer 监控数据缓存
     * <p>
     * 用于 Micrometer 方式的监控数据缓存，避免重复注册 Gauge 指标。
     * 
     * <p><b>数据结构：</b>
     * <ul>
     *   <li>键：线程池 ID</li>
     *   <li>值：{@link ThreadPoolRuntimeInfo} 对象（会被 Gauge 引用）</li>
     * </ul>
     * 
     * <p><b>为什么需要缓存？</b>
     * <ul>
     *   <li>Micrometer Gauge 需要引用一个对象，通过方法引用获取值</li>
     *   <li>每次采集时更新缓存对象的属性，Gauge 会自动读取最新值</li>
     *   <li>避免重复注册 Gauge（注册一次，持续更新数据）</li>
     * </ul>
     */
    private Map<String, ThreadPoolRuntimeInfo> micrometerMonitorCache;
    
    /**
     * 拒绝次数增量计算缓存
     * <p>
     * 用于计算拒绝次数的增量值（过去一个采集周期内的新增拒绝次数）。
     * 
     * <p><b>数据结构：</b>
     * <ul>
     *   <li>键：线程池 ID</li>
     *   <li>值：{@link DeltaWrapper} 对象（用于增量计算）</li>
     * </ul>
     * 
     * <p><b>示例：</b>
     * <pre>
     * 第1次采集：rejectCount = 10, delta = 0
     * 第2次采集：rejectCount = 15, delta = 5（过去10秒新增5次拒绝）
     * 第3次采集：rejectCount = 15, delta = 0（过去10秒没有新拒绝）
     * </pre>
     */
    private Map<String, DeltaWrapper> rejectCountDeltaMap;
    
    /**
     * 完成任务数增量计算缓存
     * <p>
     * 用于计算完成任务数的增量值（过去一个采集周期内完成的任务数）。
     * 
     * <p><b>用途：</b>
     * <ul>
     *   <li>计算 TPS（每秒任务数）</li>
     *   <li>分析线程池的处理能力</li>
     *   <li>监控任务处理速度的趋势</li>
     * </ul>
     * 
     * <p><b>示例：</b>
     * <pre>
     * 第1次采集：completedCount = 1000, delta = 0
     * 第2次采集：completedCount = 1500, delta = 500（过去10秒完成500个任务）
     * TPS = 500 / 10 = 50 任务/秒
     * </pre>
     */
    private Map<String, DeltaWrapper> completedTaskDeltaMap;

    /**
     * Prometheus 指标名称前缀
     * <p>
     * 所有 oneThread 框架的 Prometheus 指标都以该前缀开头。
     * 
     * <p><b>值：</b>"dynamic.thread-pool"
     * 
     * <p><b>完整指标名示例：</b>
     * <ul>
     *   <li>dynamic.thread-pool.core.size</li>
     *   <li>dynamic.thread-pool.active.size</li>
     *   <li>dynamic.thread-pool.queue.size</li>
     * </ul>
     */
    private static final String METRIC_NAME_PREFIX = "dynamic.thread-pool";
    
    /**
     * 线程池 ID 标签名称
     * <p>
     * Prometheus 标签名，用于区分不同的线程池。
     * 
     * <p><b>值：</b>"dynamic.thread-pool.id"
     * 
     * <p><b>示例：</b>
     * <pre>
     * dynamic_thread_pool_active_size{dynamic_thread_pool_id="order-processor"} 8
     * </pre>
     */
    private static final String DYNAMIC_THREAD_POOL_ID_TAG = METRIC_NAME_PREFIX + ".id";
    
    /**
     * 应用名称标签名称
     * <p>
     * Prometheus 标签名，用于区分不同的应用。
     * 
     * <p><b>值：</b>"application.name"
     * 
     * <p><b>示例：</b>
     * <pre>
     * dynamic_thread_pool_active_size{
     *   dynamic_thread_pool_id="order-processor",
     *   application_name="order-service"
     * } 8
     * </pre>
     */
    private static final String APPLICATION_NAME_TAG = "application.name";

    /**
     * 启动监控定时任务
     * <p>
     * 根据配置启动定时采集任务，定期采集所有线程池的运行时状态。
     * 
     * <p><b>启动流程：</b>
     * <ol>
     *   <li>读取监控配置（enable、collectType、collectInterval）</li>
     *   <li>如果未启用监控，直接返回</li>
     *   <li>初始化监控相关的缓存 Map</li>
     *   <li>创建单线程定时调度器</li>
     *   <li>启动定时任务（scheduleWithFixedDelay）</li>
     * </ol>
     * 
     * <p><b>定时任务逻辑：</b>
     * <pre>
     * 每个采集周期：
     * 1. 从 OneThreadRegistry 获取所有线程池
     * 2. 遍历每个线程池
     * 3. 构建运行时信息（buildThreadPoolRuntimeInfo）
     * 4. 根据 collectType 选择输出方式：
     *    - log：输出到日志（logMonitor）
     *    - micrometer：更新 Prometheus 指标（micrometerMonitor）
     * </pre>
     * 
     * <p><b>配置检查：</b>
     * 如果 {@code monitor.enable = false}，监控器不会启动。
     */
    public void start() {
        // 获取监控配置
        BootstrapConfigProperties.MonitorConfig monitorConfig = BootstrapConfigProperties.getInstance().getMonitor();
        
        // 检查是否启用监控
        if (!monitorConfig.getEnable()) {
            return;
        }

        // 初始化监控相关资源
        micrometerMonitorCache = new ConcurrentHashMap<>();
        rejectCountDeltaMap = new ConcurrentHashMap<>();
        completedTaskDeltaMap = new ConcurrentHashMap<>();
        
        // 创建单线程定时调度器
        scheduler = Executors.newScheduledThreadPool(
                1,
                ThreadFactoryBuilder.builder()
                        .namePrefix("scheduler_thread-pool_monitor")
                        .build()
        );

        // 启动定时采集任务
        // 每 collectInterval 秒检查一次，初始延迟0秒
        scheduler.scheduleWithFixedDelay(() -> {
            // 获取所有线程池
            Collection<ThreadPoolExecutorHolder> holders = OneThreadRegistry.getAllHolders();
            
            // 遍历每个线程池进行监控
            for (ThreadPoolExecutorHolder holder : holders) {
                // 构建运行时信息
                ThreadPoolRuntimeInfo runtimeInfo = buildThreadPoolRuntimeInfo(holder);

                // 根据采集类型判断输出方式
                if (Objects.equals(monitorConfig.getCollectType(), "log")) {
                    // 日志方式：输出到日志文件
                    logMonitor(runtimeInfo);
                } else if (Objects.equals(monitorConfig.getCollectType(), "micrometer")) {
                    // Micrometer方式：更新 Prometheus 指标
                    micrometerMonitor(runtimeInfo);
                }
            }
        }, 0, monitorConfig.getCollectInterval(), TimeUnit.SECONDS);
    }

    /**
     * 停止监控器
     * <p>
     * 优雅地关闭定时调度器，停止监控采集任务。
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>应用关闭时停止监控</li>
     *   <li>临时禁用监控功能</li>
     * </ul>
     */
    public void stop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    /**
     * 日志方式监控
     * <p>
     * 将线程池运行时信息以 JSON 格式输出到日志文件。
     * 
     * <p><b>日志格式：</b>
     * <pre>
     * [ThreadPool Monitor] order-processor | Content: {
     *   "threadPoolId":"order-processor",
     *   "corePoolSize":10,
     *   "maximumPoolSize":20,
     *   "currentPoolSize":15,
     *   "activePoolSize":12,
     *   "largestPoolSize":18,
     *   "completedTaskCount":50000,
     *   "workQueueName":"LinkedBlockingQueue",
     *   "workQueueCapacity":500,
     *   "workQueueSize":350,
     *   "workQueueRemainingCapacity":150,
     *   "rejectedHandlerName":"CallerRunsPolicy",
     *   "rejectCount":5
     * }
     * </pre>
     * 
     * <p><b>优势：</b>
     * <ul>
     *   <li>简单直观，易于查看和调试</li>
     *   <li>无需额外依赖</li>
     *   <li>便于问题排查</li>
     * </ul>
     * 
     * <p><b>劣势：</b>
     * <ul>
     *   <li>数据分散在日志文件中，不易分析</li>
     *   <li>无法可视化展示</li>
     *   <li>难以进行趋势分析和告警</li>
     * </ul>
     *
     * @param runtimeInfo 线程池运行时信息
     */
    private void logMonitor(ThreadPoolRuntimeInfo runtimeInfo) {
        log.info("[ThreadPool Monitor] {} | Content: {}", runtimeInfo.getThreadPoolId(), JSON.toJSON(runtimeInfo));
    }

    /**
     * Micrometer 方式监控（Prometheus 集成）
     * <p>
     * 将线程池运行时信息注册为 Prometheus Gauge 指标，供 Prometheus 抓取和 Grafana 展示。
     * 
     * <p><b>首次注册流程：</b>
     * <ol>
     *   <li>检查线程池是否已注册（通过缓存判断）</li>
     *   <li>如果未注册：
     *       <ul>
     *         <li>创建标签（线程池ID、应用名称）</li>
     *         <li>创建运行时信息对象并加入缓存</li>
     *         <li>注册所有 Gauge 指标（10个总量指标 + 2个增量指标）</li>
     *         <li>创建增量计算器并加入缓存</li>
     *       </ul>
     *   </li>
     *   <li>如果已注册：
     *       <ul>
     *         <li>直接更新缓存对象的属性（BeanUtil.copyProperties）</li>
     *         <li>Gauge 会自动读取最新值</li>
     *       </ul>
     *   </li>
     *   <li>更新增量计算器的值</li>
     * </ol>
     * 
     * <p><b>注册的 Gauge 指标：</b>
     * <ul>
     *   <li><b>总量指标（10个）：</b>直接从线程池获取的累计值</li>
     *   <li><b>增量指标（2个）：</b>通过 DeltaWrapper 计算的增量值</li>
     * </ul>
     * 
     * <p><b>Gauge 工作原理：</b>
     * <pre>
     * // 注册 Gauge（只执行一次）
     * Metrics.gauge(
     *     "dynamic.thread-pool.active.size",  // 指标名
     *     tags,                                // 标签
     *     runtimeInfo,                         // 引用的对象
     *     ThreadPoolRuntimeInfo::getActivePoolSize  // 值提取方法
     * );
     * 
     * // 每次 Prometheus 抓取时：
     * 1. Prometheus 调用 Gauge 的值提取方法
     * 2. 方法引用会执行：runtimeInfo.getActivePoolSize()
     * 3. 返回最新的活跃线程数
     * 
     * // 每次采集时：
     * 1. 更新 runtimeInfo 对象的属性
     * 2. Gauge 下次抓取时会读取到最新值
     * </pre>
     * 
     * <p><b>增量指标计算：</b>
     * <pre>
     * // 每次采集时更新增量计算器
     * completedTaskDeltaMap.get(threadPoolId).update(currentCompletedCount);
     * 
     * // Prometheus 抓取时：
     * delta = currentValue - lastValue
     * // 得到过去一个采集周期内完成的任务数
     * </pre>
     * 
     * <p><b>性能优化：</b>
     * <ul>
     *   <li>只在首次采集时注册 Gauge（避免重复注册）</li>
     *   <li>后续采集只更新对象属性（无需重新注册）</li>
     *   <li>使用 BeanUtil.copyProperties 高效复制属性</li>
     * </ul>
     *
     * @param runtimeInfo 线程池运行时信息
     */
    private void micrometerMonitor(ThreadPoolRuntimeInfo runtimeInfo) {
        String threadPoolId = runtimeInfo.getThreadPoolId();
        ThreadPoolRuntimeInfo existingRuntimeInfo = micrometerMonitorCache.get(threadPoolId);

        // 检查是否已注册（只在首次采集时注册 Gauge）
        if (existingRuntimeInfo == null) {
            // 创建 Prometheus 标签
            Iterable<Tag> tags = CollectionUtil.newArrayList(
                    Tag.of(DYNAMIC_THREAD_POOL_ID_TAG, threadPoolId),
                    Tag.of(APPLICATION_NAME_TAG, ApplicationProperties.getApplicationName())
            );

            // 创建运行时信息对象并加入缓存
            ThreadPoolRuntimeInfo registerRuntimeInfo = BeanUtil.toBean(runtimeInfo, ThreadPoolRuntimeInfo.class);
            micrometerMonitorCache.put(threadPoolId, registerRuntimeInfo);

            // 注册总量指标（10个 Gauge）
            Metrics.gauge(metricName("core.size"), tags, registerRuntimeInfo, ThreadPoolRuntimeInfo::getCorePoolSize);
            Metrics.gauge(metricName("maximum.size"), tags, registerRuntimeInfo, ThreadPoolRuntimeInfo::getMaximumPoolSize);
            Metrics.gauge(metricName("current.size"), tags, registerRuntimeInfo, ThreadPoolRuntimeInfo::getCurrentPoolSize);
            Metrics.gauge(metricName("largest.size"), tags, registerRuntimeInfo, ThreadPoolRuntimeInfo::getLargestPoolSize);
            Metrics.gauge(metricName("active.size"), tags, registerRuntimeInfo, ThreadPoolRuntimeInfo::getActivePoolSize);
            Metrics.gauge(metricName("queue.size"), tags, registerRuntimeInfo, ThreadPoolRuntimeInfo::getWorkQueueSize);
            Metrics.gauge(metricName("queue.capacity"), tags, registerRuntimeInfo, ThreadPoolRuntimeInfo::getWorkQueueCapacity);
            Metrics.gauge(metricName("queue.remaining.capacity"), tags, registerRuntimeInfo, ThreadPoolRuntimeInfo::getWorkQueueRemainingCapacity);

            // 注册增量指标：已完成任务数增量
            DeltaWrapper completedDelta = new DeltaWrapper();
            completedTaskDeltaMap.put(threadPoolId, completedDelta);
            Metrics.gauge(metricName("completed.task.count"), tags, completedDelta, DeltaWrapper::getDelta);

            // 注册增量指标：拒绝次数增量
            DeltaWrapper rejectDelta = new DeltaWrapper();
            rejectCountDeltaMap.put(threadPoolId, rejectDelta);
            Metrics.gauge(metricName("reject.count"), tags, rejectDelta, DeltaWrapper::getDelta);
        } else {
            // 已注册，只更新属性（避免重复注册 Gauge）
            // BeanUtil.copyProperties 会将 runtimeInfo 的所有属性复制到 existingRuntimeInfo
            // Gauge 引用的是 existingRuntimeInfo，会自动读取最新值
            BeanUtil.copyProperties(runtimeInfo, existingRuntimeInfo);
        }

        // 每次都更新增量计算器的值
        completedTaskDeltaMap.get(threadPoolId).update(runtimeInfo.getCompletedTaskCount());
        rejectCountDeltaMap.get(threadPoolId).update(runtimeInfo.getRejectCount());
    }

    /**
     * 构建完整的 Prometheus 指标名称
     * <p>
     * 将指标名称前缀和具体指标名拼接为完整的指标名。
     * 
     * <p><b>拼接规则：</b>
     * <pre>
     * 前缀 + "." + 指标名
     * </pre>
     * 
     * <p><b>示例：</b>
     * <pre>
     * metricName("core.size")      → "dynamic.thread-pool.core.size"
     * metricName("active.size")    → "dynamic.thread-pool.active.size"
     * metricName("queue.size")     → "dynamic.thread-pool.queue.size"
     * </pre>
     *
     * @param name 指标名称（如 "core.size"）
     * @return 完整的指标名称（如 "dynamic.thread-pool.core.size"）
     */
    private String metricName(String name) {
        return String.join(".", METRIC_NAME_PREFIX, name);
    }

    /**
     * 构建线程池运行时信息
     * <p>
     * 从线程池实例中采集所有运行时状态数据，构建 {@link ThreadPoolRuntimeInfo} 对象。
     * 
     * <p><b>采集的数据：</b>
     * <ul>
     *   <li>线程信息：核心数、最大数、当前数、活跃数、历史最大数</li>
     *   <li>队列信息：类型、容量、大小、剩余容量</li>
     *   <li>任务统计：已完成任务数</li>
     *   <li>拒绝统计：拒绝策略类型、拒绝次数</li>
     * </ul>
     * 
     * <p><b>性能注意：</b>
     * 某些线程池 API 内部有锁（如 getActiveCount、getPoolSize），
     * 避免高频率调用，已在注释中标注。
     *
     * @param holder 线程池包装对象
     * @return 线程池运行时信息对象
     */
    @SneakyThrows
    private ThreadPoolRuntimeInfo buildThreadPoolRuntimeInfo(ThreadPoolExecutorHolder holder) {
        ThreadPoolExecutor executor = holder.getExecutor();
        BlockingQueue<?> queue = executor.getQueue();

        // 获取拒绝次数（只有 OneThreadExecutor 支持）
        long rejectCount = -1L;
        if (executor instanceof OneThreadExecutor) {
            rejectCount = ((OneThreadExecutor) executor).getRejectCount().get();
        }

        // 获取队列信息
        int workQueueSize = queue.size();
        int remainingCapacity = queue.remainingCapacity();
        
        // 构建运行时信息对象
        return ThreadPoolRuntimeInfo.builder()
                .threadPoolId(holder.getThreadPoolId())
                .corePoolSize(executor.getCorePoolSize())
                .maximumPoolSize(executor.getMaximumPoolSize())
                .activePoolSize(executor.getActiveCount())  // API 有锁，避免高频率调用
                .currentPoolSize(executor.getPoolSize())  // API 有锁，避免高频率调用
                .completedTaskCount(executor.getCompletedTaskCount())  // API 有锁，避免高频率调用
                .largestPoolSize(executor.getLargestPoolSize())  // API 有锁，避免高频率调用
                .workQueueName(queue.getClass().getSimpleName())
                .workQueueSize(workQueueSize)
                .workQueueRemainingCapacity(remainingCapacity)
                .workQueueCapacity(workQueueSize + remainingCapacity)
                .rejectedHandlerName(executor.getRejectedExecutionHandler().toString())
                .rejectCount(rejectCount)
                .build();
    }
}
