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

package com.nageoffer.onethread.core.alarm;

import cn.hutool.core.date.DateUtil;
import com.nageoffer.onethread.core.config.ApplicationProperties;
import com.nageoffer.onethread.core.executor.OneThreadExecutor;
import com.nageoffer.onethread.core.executor.OneThreadRegistry;
import com.nageoffer.onethread.core.executor.ThreadPoolExecutorHolder;
import com.nageoffer.onethread.core.executor.ThreadPoolExecutorProperties;
import com.nageoffer.onethread.core.notification.dto.ThreadPoolAlarmNotifyDTO;
import com.nageoffer.onethread.core.notification.service.NotifierDispatcher;
import com.nageoffer.onethread.core.toolkit.ThreadFactoryBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池运行状态告警检查器
 * <p>
 * 该类负责定时检查所有动态线程池的运行状态，当发现异常情况（如队列堆积、线程池满载、
 * 拒绝次数增加等）时，自动触发告警通知（钉钉消息、邮件等）。
 * 
 * <p><b>核心功能：</b>
 * <ul>
 *   <li><b>定时巡检：</b>每隔5秒检查一次所有线程池的运行状态</li>
 *   <li><b>多维度检查：</b>检查队列使用率、活跃线程率、拒绝次数等多个维度</li>
 *   <li><b>自动告警：</b>超过阈值时自动发送告警通知</li>
 *   <li><b>增量检测：</b>拒绝次数使用增量检测，避免重复告警</li>
 * </ul>
 * 
 * <p><b>检查维度：</b>
 * <table border="1" cellpadding="5">
 *   <tr><th>检查项</th><th>计算公式</th><th>告警条件</th><th>含义</th></tr>
 *   <tr>
 *     <td>队列使用率</td>
 *     <td>(队列大小/队列容量)×100</td>
 *     <td>≥ queueThreshold</td>
 *     <td>队列堆积严重</td>
 *   </tr>
 *   <tr>
 *     <td>活跃线程率</td>
 *     <td>(活跃线程数/最大线程数)×100</td>
 *     <td>≥ activeThreshold</td>
 *     <td>线程池接近满载</td>
 *   </tr>
 *   <tr>
 *     <td>拒绝次数</td>
 *     <td>当前拒绝次数 - 上次拒绝次数</td>
 *     <td>> 0（增量检测）</td>
 *     <td>有新的任务被拒绝</td>
 *   </tr>
 * </table>
 * 
 * <p><b>告警触发流程：</b>
 * <pre>
 * 定时任务（每5秒）
 *    ↓
 * 遍历所有线程池
 *    ↓
 * 检查是否启用告警（alarm.enable = true）
 *    ↓
 * 并行检查三个维度
 *    ├─ checkQueueUsage()    → 队列使用率 ≥ 80%？
 *    ├─ checkActiveRate()    → 活跃线程率 ≥ 80%？
 *    └─ checkRejectCount()   → 拒绝次数增加？
 *         ↓
 * 触发告警条件
 *    ↓
 * 构建告警消息（ThreadPoolAlarmNotifyDTO）
 *    ↓
 * 发送告警（钉钉/邮件）
 *    ↓
 * 告警限流（同一线程池N分钟内只告警一次）
 * </pre>
 * 
 * <p><b>告警示例：</b>
 * <pre>
 * [警报] 订单服务 - 动态线程池运行告警
 * ---
 * 线程池ID：order-processor
 * 应用实例：192.168.1.100:8080
 * 告警类型：队列堆积告警
 * ---
 * 核心线程数：10
 * 最大线程数：20
 * 当前线程数：20
 * 活跃线程数：20
 * ---
 * 队列类型：LinkedBlockingQueue
 * 队列容量：500
 * 队列元素个数：450
 * 队列剩余个数：50
 * ---
 * 拒绝策略：CallerRunsPolicy
 * 拒绝策略执行次数：10
 * ---
 * 告警时间：2025-04-30 15:30:45
 * </pre>
 * 
 * <p><b>设计特点：</b>
 * <ul>
 *   <li><b>单线程调度：</b>使用单线程定时调度器，避免并发检查导致的竞争</li>
 *   <li><b>增量检测：</b>拒绝次数使用增量检测，只有新增拒绝才告警</li>
 *   <li><b>延迟执行：</b>使用 {@link ScheduledExecutorService#scheduleWithFixedDelay} 避免任务堆积</li>
 *   <li><b>异常隔离：</b>单个线程池检查失败不影响其他线程池</li>
 * </ul>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>生产环境的线程池监控和预警</li>
 *   <li>及时发现线程池容量不足的问题</li>
 *   <li>防止系统因线程池满载而出现故障</li>
 *   <li>为容量规划提供数据支持</li>
 * </ul>
 * 
 * <p><b>使用示例：</b>
 * <pre>{@code
 * // 示例1：启动告警检查器
 * NotifierDispatcher notifierDispatcher = ...;
 * ThreadPoolAlarmChecker alarmChecker = new ThreadPoolAlarmChecker(notifierDispatcher);
 * alarmChecker.start();  // 开始定时检查
 * 
 * // 应用运行期间，告警检查器会自动工作
 * // 当发现异常情况时，自动发送钉钉告警
 * 
 * 
 * // 示例2：在Spring中使用
 * @Component
 * public class AlarmCheckerManager {
 *     @Autowired
 *     private NotifierDispatcher notifierDispatcher;
 *     
 *     private ThreadPoolAlarmChecker alarmChecker;
 *     
 *     @PostConstruct
 *     public void init() {
 *         alarmChecker = new ThreadPoolAlarmChecker(notifierDispatcher);
 *         alarmChecker.start();
 *     }
 *     
 *     @PreDestroy
 *     public void destroy() {
 *         alarmChecker.stop();
 *     }
 * }
 * 
 * 
 * // 示例3：告警配置（在application.yml中）
 * onethread:
 *   executors:
 *     - thread-pool-id: order-processor
 *       alarm:
 *         enable: true          # 启用告警
 *         queue-threshold: 80   # 队列使用率阈值80%
 *         active-threshold: 80  # 活跃线程率阈值80%
 *       notify:
 *         receives: "张三,李四"  # 告警接收人
 *         interval: 5           # 告警间隔5分钟
 * }</pre>
 * 
 * <p><b>性能考虑：</b>
 * <ul>
 *   <li>检查间隔：默认5秒，可以根据需要调整</li>
 *   <li>单线程执行：避免并发检查的开销</li>
 *   <li>延迟调度：使用 fixedDelay 而非 fixedRate，避免任务堆积</li>
 *   <li>API调用：某些线程池 API 有锁，注释中特别标注避免高频调用</li>
 * </ul>
 * 
 * <p><b>最佳实践：</b>
 * <ul>
 *   <li>生产环境必须启用告警检查</li>
 *   <li>合理设置告警阈值（推荐80%）</li>
 *   <li>配置告警接收人（钉钉或邮件）</li>
 *   <li>配置告警限流（避免频繁骚扰）</li>
 *   <li>定期查看告警历史，优化线程池配置</li>
 * </ul>
 * 
 * @author 杨潇
 * @since 2025-05-04
 * @see NotifierDispatcher 告警通知分发器
 * @see ThreadPoolAlarmNotifyDTO 告警消息DTO
 * @see OneThreadRegistry 线程池注册表
 */
@Slf4j
@RequiredArgsConstructor
public class ThreadPoolAlarmChecker {

    /**
     * 通知分发器
     * <p>
     * 用于发送告警消息到各种通知渠道（钉钉、邮件等）。
     * 当检测到告警条件时，会调用该分发器发送告警通知。
     */
    private final NotifierDispatcher notifierDispatcher;

    /**
     * 定时调度器
     * <p>
     * 使用单线程的定时调度器，每隔5秒执行一次告警检查任务。
     * 
     * <p><b>配置：</b>
     * <ul>
     *   <li>线程池大小：1（单线程，避免并发检查）</li>
     *   <li>线程名称：scheduler_thread-pool_alarm_checker</li>
     *   <li>调度方式：固定延迟（scheduleWithFixedDelay）</li>
     *   <li>检查间隔：5秒</li>
     * </ul>
     * 
     * <p><b>为什么使用单线程？</b>
     * <ul>
     *   <li>避免多个检查任务并发执行导致的竞争</li>
     *   <li>简化实现，无需考虑并发安全</li>
     *   <li>检查任务执行时间短，单线程足够</li>
     * </ul>
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(
            1,
            ThreadFactoryBuilder.builder()
                    .namePrefix("scheduler_thread-pool_alarm_checker")
                    .build()
    );
    
    /**
     * 上次拒绝次数记录表
     * <p>
     * 用于记录每个线程池上次检查时的拒绝次数，便于计算增量。
     * 
     * <p><b>数据结构：</b>
     * <ul>
     *   <li>键：线程池ID</li>
     *   <li>值：上次检查时的拒绝次数</li>
     * </ul>
     * 
     * <p><b>用途：</b>
     * <ul>
     *   <li>计算增量：当前拒绝次数 - 上次拒绝次数</li>
     *   <li>避免重复告警：只有新增拒绝才触发告警</li>
     * </ul>
     * 
     * <p><b>为什么用 ConcurrentHashMap？</b>
     * 虽然使用单线程调度器，但为了安全起见使用线程安全的 Map。
     */
    private final Map<String, Long> lastRejectCountMap = new ConcurrentHashMap<>();

    /**
     * 启动告警检查定时任务
     * <p>
     * 启动一个定时任务，每隔5秒检查一次所有线程池的运行状态。
     * 
     * <p><b>调度配置：</b>
     * <ul>
     *   <li><b>初始延迟：</b>0秒（启动后立即执行第一次检查）</li>
     *   <li><b>检查间隔：</b>5秒（固定延迟，任务完成后等待5秒再执行下一次）</li>
     *   <li><b>调度方式：</b>scheduleWithFixedDelay（固定延迟）</li>
     * </ul>
     * 
     * <p><b>检查流程：</b>
     * <ol>
     *   <li>从 {@link OneThreadRegistry} 获取所有线程池</li>
     *   <li>遍历每个线程池</li>
     *   <li>检查是否启用告警（{@code alarm.enable = true}）</li>
     *   <li>如果启用，执行三项检查：
     *       <ul>
     *         <li>{@link #checkQueueUsage} - 队列使用率检查</li>
     *         <li>{@link #checkActiveRate} - 活跃线程率检查</li>
     *         <li>{@link #checkRejectCount} - 拒绝次数检查</li>
     *       </ul>
     *   </li>
     *   <li>如果超过阈值，发送告警通知</li>
     * </ol>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * // Spring Bean初始化时启动
     * @PostConstruct
     * public void init() {
     *     alarmChecker.start();
     *     log.info("线程池告警检查器已启动");
     * }
     * }</pre>
     */
    public void start() {
        // 每5秒检查一次，初始延迟0秒
        scheduler.scheduleWithFixedDelay(this::checkAlarm, 0, 5, TimeUnit.SECONDS);
    }

    /**
     * 停止告警检查器
     * <p>
     * 优雅地关闭定时调度器，停止告警检查任务。
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>应用关闭时停止告警检查</li>
     *   <li>临时禁用告警功能</li>
     * </ul>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * // Spring Bean销毁时停止
     * @PreDestroy
     * public void destroy() {
     *     alarmChecker.stop();
     *     log.info("线程池告警检查器已停止");
     * }
     * }</pre>
     */
    public void stop() {
        if (!scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    /**
     * 告警检查核心逻辑
     * <p>
     * 该方法是定时任务的执行入口，负责遍历所有线程池并执行告警检查。
     * 
     * <p><b>执行流程：</b>
     * <ol>
     *   <li>从注册表获取所有线程池</li>
     *   <li>遍历每个线程池</li>
     *   <li>检查是否启用告警</li>
     *   <li>如果启用，执行三项检查</li>
     * </ol>
     * 
     * <p><b>异常处理：</b>
     * 单个线程池检查失败不会影响其他线程池的检查（遍历会继续）。
     */
    private void checkAlarm() {
        // 获取所有已注册的线程池
        Collection<ThreadPoolExecutorHolder> holders = OneThreadRegistry.getAllHolders();
        
        // 遍历每个线程池进行检查
        for (ThreadPoolExecutorHolder holder : holders) {
            // 检查是否启用告警
            if (holder.getExecutorProperties().getAlarm().getEnable()) {
                // 执行三项检查
                checkQueueUsage(holder);     // 队列使用率检查
                checkActiveRate(holder);     // 活跃线程率检查
                checkRejectCount(holder);    // 拒绝次数检查
            }
        }
    }

    /**
     * 检查队列使用率
     * <p>
     * 计算队列的使用率（队列大小/队列容量），如果超过配置的阈值则触发告警。
     * 
     * <p><b>计算公式：</b>
     * <pre>
     * 队列使用率 = (当前队列大小 / 队列总容量) × 100
     * 
     * 其中：队列总容量 = 当前队列大小 + 剩余容量
     * </pre>
     * 
     * <p><b>告警条件：</b>
     * <pre>
     * 队列使用率 >= queueThreshold（默认80%）
     * </pre>
     * 
     * <p><b>告警含义：</b>
     * <ul>
     *   <li>队列堆积严重，任务处理速度跟不上提交速度</li>
     *   <li>可能很快会触发拒绝策略</li>
     *   <li>需要增加线程数或队列容量</li>
     * </ul>
     * 
     * <p><b>处理建议：</b>
     * <ul>
     *   <li>增加最大线程数，提高并发处理能力</li>
     *   <li>增加队列容量，提供更多缓冲空间</li>
     *   <li>优化任务处理逻辑，提升处理速度</li>
     *   <li>考虑限流，控制任务提交速度</li>
     * </ul>
     * 
     * <p><b>特殊情况处理：</b>
     * <ul>
     *   <li>如果队列容量为0（如 SynchronousQueue），直接返回，不检查</li>
     *   <li>使用四舍五入（Math.round）计算使用率，确保精度</li>
     * </ul>
     *
     * @param holder 线程池包装对象
     */
    private void checkQueueUsage(ThreadPoolExecutorHolder holder) {
        ThreadPoolExecutor executor = holder.getExecutor();
        ThreadPoolExecutorProperties properties = holder.getExecutorProperties();

        // 获取队列信息
        BlockingQueue<?> queue = executor.getQueue();
        int queueSize = queue.size();                   // 当前队列大小
        int capacity = queueSize + queue.remainingCapacity();  // 队列总容量

        // 特殊情况：容量为0（如 SynchronousQueue），不检查
        if (capacity == 0) {
            return;
        }

        // 计算队列使用率（百分比，四舍五入）
        int usageRate = (int) Math.round((queueSize * 100.0) / capacity);
        
        // 获取配置的阈值
        int threshold = properties.getAlarm().getQueueThreshold();

        // 如果使用率达到或超过阈值，发送告警
        if (usageRate >= threshold) {
            sendAlarmMessage("Capacity", holder);
        }
    }

    /**
     * 检查活跃线程率
     * <p>
     * 计算活跃线程率（活跃线程数/最大线程数），如果超过配置的阈值则触发告警。
     * 
     * <p><b>计算公式：</b>
     * <pre>
     * 活跃线程率 = (活跃线程数 / 最大线程数) × 100
     * </pre>
     * 
     * <p><b>告警条件：</b>
     * <pre>
     * 活跃线程率 >= activeThreshold（默认80%）
     * </pre>
     * 
     * <p><b>告警含义：</b>
     * <ul>
     *   <li>线程池接近满载，处理能力即将达到上限</li>
     *   <li>可能即将触发拒绝策略</li>
     *   <li>系统负载较高，需要关注</li>
     * </ul>
     * 
     * <p><b>处理建议：</b>
     * <ul>
     *   <li>增加最大线程数，提供更多处理能力</li>
     *   <li>增加核心线程数，减少线程创建开销</li>
     *   <li>优化任务逻辑，减少执行时间</li>
     *   <li>考虑扩容或限流</li>
     * </ul>
     * 
     * <p><b>特殊情况处理：</b>
     * <ul>
     *   <li>如果最大线程数为0（不太可能），直接返回，不检查</li>
     *   <li>使用四舍五入计算活跃率，确保精度</li>
     * </ul>
     * 
     * <p><b>性能注意：</b>
     * {@link ThreadPoolExecutor#getActiveCount()} 方法内部有锁，避免高频率调用。
     *
     * @param holder 线程池包装对象
     */
    private void checkActiveRate(ThreadPoolExecutorHolder holder) {
        ThreadPoolExecutor executor = holder.getExecutor();
        ThreadPoolExecutorProperties properties = holder.getExecutorProperties();

        // 获取活跃线程数（注意：该API内部有锁，避免高频调用）
        int activeCount = executor.getActiveCount();
        int maximumPoolSize = executor.getMaximumPoolSize();

        // 特殊情况：最大线程数为0，不检查
        if (maximumPoolSize == 0) {
            return;
        }

        // 计算活跃线程率（百分比，四舍五入）
        int activeRate = (int) Math.round((activeCount * 100.0) / maximumPoolSize);
        
        // 获取配置的阈值
        int threshold = properties.getAlarm().getActiveThreshold();

        // 如果活跃率达到或超过阈值，发送告警
        if (activeRate >= threshold) {
            sendAlarmMessage("Activity", holder);
        }
    }

    /**
     * 检查拒绝策略执行次数（增量检测）
     * <p>
     * 检查拒绝次数是否有新增，只有新增的拒绝才触发告警。
     * 这种增量检测方式避免了重复告警同一批拒绝事件。
     * 
     * <p><b>增量检测原理：</b>
     * <pre>
     * 第一次检查：
     *   当前拒绝次数 = 10
     *   上次拒绝次数 = 0（首次）
     *   增量 = 10 - 0 = 10 > 0  → 触发告警
     *   更新上次拒绝次数 = 10
     * 
     * 第二次检查（5秒后）：
     *   当前拒绝次数 = 10（没有新拒绝）
     *   上次拒绝次数 = 10
     *   增量 = 10 - 10 = 0  → 不告警
     * 
     * 第三次检查（又过5秒）：
     *   当前拒绝次数 = 15（新增5次拒绝）
     *   上次拒绝次数 = 10
     *   增量 = 15 - 10 = 5 > 0  → 触发告警
     *   更新上次拒绝次数 = 15
     * </pre>
     * 
     * <p><b>告警含义：</b>
     * <ul>
     *   <li>有新的任务被拒绝</li>
     *   <li>线程池容量严重不足</li>
     *   <li>可能导致业务数据丢失或错误</li>
     * </ul>
     * 
     * <p><b>处理建议：</b>
     * <ul>
     *   <li>紧急扩容（增加线程数和队列容量）</li>
     *   <li>检查是否有异常流量</li>
     *   <li>分析拒绝的原因和影响范围</li>
     *   <li>考虑限流或熔断</li>
     * </ul>
     * 
     * <p><b>特殊处理：</b>
     * <ul>
     *   <li>只检查 {@link OneThreadExecutor} 类型的线程池（普通线程池不支持拒绝统计）</li>
     *   <li>首次检查时会记录当前拒绝次数，并触发告警（如果有拒绝）</li>
     * </ul>
     *
     * @param holder 线程池包装对象
     */
    private void checkRejectCount(ThreadPoolExecutorHolder holder) {
        ThreadPoolExecutor executor = holder.getExecutor();
        String threadPoolId = holder.getThreadPoolId();

        // 只处理 OneThreadExecutor 类型（标准 ThreadPoolExecutor 不支持拒绝统计）
        if (!(executor instanceof OneThreadExecutor)) {
            return;
        }

        // 获取当前拒绝次数
        OneThreadExecutor oneThreadExecutor = (OneThreadExecutor) executor;
        long currentRejectCount = oneThreadExecutor.getRejectCount().get();
        
        // 获取上次记录的拒绝次数（如果没有记录，默认为0）
        long lastRejectCount = lastRejectCountMap.getOrDefault(threadPoolId, 0L);

        // 增量检测：只有拒绝次数增加时才触发告警
        if (currentRejectCount > lastRejectCount) {
            // 发送告警
            sendAlarmMessage("Reject", holder);
            
            // 更新最后记录值，避免下次重复告警
            lastRejectCountMap.put(threadPoolId, currentRejectCount);
        }
    }

    /**
     * 发送告警消息
     * <p>
     * 构建告警消息并通过通知分发器发送到钉钉、邮件等渠道。
     * 
     * <p><b>构建流程：</b>
     * <ol>
     *   <li>创建告警 DTO 对象</li>
     *   <li>设置基本信息（告警类型、线程池ID、告警间隔）</li>
     *   <li>设置延迟加载的 Supplier（获取详细运行时数据）</li>
     *   <li>调用通知分发器发送告警</li>
     * </ol>
     * 
     * <p><b>延迟加载设计：</b>
     * 使用 Supplier 模式延迟获取详细数据，只有在真正需要发送告警时才采集数据。
     * 这样可以避免在告警限流时采集不必要的数据，提高性能。
     * 
     * <p><b>采集的数据：</b>
     * <ul>
     *   <li>应用实例地址（IP地址）</li>
     *   <li>线程池参数（核心数、最大数等）</li>
     *   <li>线程池状态（活跃数、当前数等）</li>
     *   <li>队列信息（类型、大小、容量）</li>
     *   <li>拒绝策略和拒绝次数</li>
     *   <li>告警时间和接收人</li>
     * </ul>
     *
     * @param alarmType 告警类型（"Capacity" - 队列堆积、"Activity" - 线程满载、"Reject" - 拒绝告警）
     * @param holder    线程池包装对象
     */
    private void sendAlarmMessage(String alarmType, ThreadPoolExecutorHolder holder) {
        ThreadPoolExecutorProperties properties = holder.getExecutorProperties();
        String threadPoolId = holder.getThreadPoolId();

        // 创建告警 DTO 对象
        ThreadPoolAlarmNotifyDTO alarm = ThreadPoolAlarmNotifyDTO.builder()
                .alarmType(alarmType)
                .threadPoolId(threadPoolId)
                .interval(properties.getNotify().getInterval())
                .build();

        // 设置延迟加载的 Supplier（延迟获取详细数据）
        // 只有在通过告警限流检查后，才会调用 Supplier 获取详细数据
        alarm.setSupplier(() -> {
            // 获取应用实例地址（IP地址）
            try {
                alarm.setIdentify(InetAddress.getLocalHost().getHostAddress());
            } catch (UnknownHostException e) {
                log.warn("Error in obtaining HostAddress", e);
            }

            ThreadPoolExecutor executor = holder.getExecutor();
            BlockingQueue<?> queue = executor.getQueue();

            // 获取队列信息
            int size = queue.size();
            int remaining = queue.remainingCapacity();
            
            // 获取拒绝次数（仅 OneThreadExecutor 支持）
            long rejectCount = (executor instanceof OneThreadExecutor)
                    ? ((OneThreadExecutor) executor).getRejectCount().get()
                    : -1L;

            // 填充告警消息的详细数据
            alarm.setCorePoolSize(executor.getCorePoolSize())
                    .setMaximumPoolSize(executor.getMaximumPoolSize())
                    .setActivePoolSize(executor.getActiveCount())  // API 有锁，避免高频率调用
                    .setCurrentPoolSize(executor.getPoolSize())  // API 有锁，避免高频率调用
                    .setCompletedTaskCount(executor.getCompletedTaskCount())  // API 有锁，避免高频率调用
                    .setLargestPoolSize(executor.getLargestPoolSize())  // API 有锁，避免高频率调用
                    .setWorkQueueName(queue.getClass().getSimpleName())
                    .setWorkQueueSize(size)
                    .setWorkQueueRemainingCapacity(remaining)
                    .setWorkQueueCapacity(size + remaining)
                    .setRejectedHandlerName(executor.getRejectedExecutionHandler().toString())
                    .setRejectCount(rejectCount)
                    .setCurrentTime(DateUtil.now())
                    .setApplicationName(ApplicationProperties.getApplicationName())
                    .setActiveProfile(ApplicationProperties.getActiveProfile())
                    .setReceives(properties.getNotify().getReceives());
            return alarm;
        });

        // 通过通知分发器发送告警消息
        // 分发器内部会进行限流检查，避免频繁告警
        notifierDispatcher.sendAlarmMessage(alarm);
    }
}
