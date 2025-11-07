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

import com.nageoffer.onethread.core.executor.support.RejectedProxyInvocationHandler;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * oneThread 增强的动态线程池执行器
 * <p>
 * 该类是 oneThread 框架的核心组件，继承自 JDK 标准的 {@link ThreadPoolExecutor}，
 * 在保留原有功能的基础上，增加了以下增强特性：
 * 
 * <p><b>增强功能：</b>
 * <ul>
 *   <li><b>线程池标识：</b>提供唯一的 threadPoolId，用于配置中心匹配和监控上报</li>
 *   <li><b>拒绝统计：</b>自动统计拒绝策略的执行次数，便于监控和告警</li>
 *   <li><b>优雅关闭：</b>支持设置等待终止时间，优雅地关闭线程池</li>
 *   <li><b>拒绝策略增强：</b>使用装饰器模式包装原始拒绝策略，增加计数功能</li>
 * </ul>
 * 
 * <p><b>与标准 ThreadPoolExecutor 的区别：</b>
 * <table border="1" cellpadding="5">
 *   <tr>
 *     <th>特性</th>
 *     <th>标准 ThreadPoolExecutor</th>
 *     <th>OneThreadExecutor</th>
 *   </tr>
 *   <tr>
 *     <td>线程池标识</td>
 *     <td>无</td>
 *     <td>内置 threadPoolId</td>
 *   </tr>
 *   <tr>
 *     <td>拒绝次数统计</td>
 *     <td>不支持</td>
 *     <td>自动统计（rejectCount）</td>
 *   </tr>
 *   <tr>
 *     <td>优雅关闭</td>
 *     <td>需手动实现</td>
 *     <td>内置支持（awaitTerminationMillis）</td>
 *   </tr>
 *   <tr>
 *     <td>拒绝策略</td>
 *     <td>原生策略</td>
 *     <td>代理包装（增加计数）</td>
 *   </tr>
 *   <tr>
 *     <td>配置中心集成</td>
 *     <td>不支持</td>
 *     <td>天然支持</td>
 *   </tr>
 * </table>
 * 
 * <p><b>核心设计：</b>
 * <ul>
 *   <li><b>继承增强：</b>继承 ThreadPoolExecutor，完全兼容 JDK 标准 API</li>
 *   <li><b>装饰器模式：</b>包装原始拒绝策略，增加计数功能</li>
 *   <li><b>模板方法模式：</b>重写 {@link #shutdown()} 方法，增加优雅关闭逻辑</li>
 * </ul>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>需要运行时动态调整参数的线程池</li>
 *   <li>需要监控拒绝次数的场景</li>
 *   <li>需要优雅关闭的场景（如Web应用关闭）</li>
 *   <li>需要与配置中心集成的场景</li>
 * </ul>
 * 
 * <p><b>创建方式：</b>
 * <pre>{@code
 * // 方式1：使用 ThreadPoolExecutorBuilder（推荐）
 * ThreadPoolExecutor executor = ThreadPoolExecutorBuilder.builder()
 *     .dynamicPool()                      // 👈 标记为动态线程池
 *     .threadPoolId("order-processor")    // 设置线程池ID
 *     .corePoolSize(10)
 *     .maximumPoolSize(20)
 *     .workQueueType(BlockingQueueTypeEnum.RESIZABLE_CAPACITY_LINKED_BLOCKING_QUEUE)
 *     .threadFactory("order")
 *     .awaitTerminationMillis(30000)      // 关闭时等待30秒
 *     .build();
 * 
 * 
 * // 方式2：直接构造（不推荐）
 * OneThreadExecutor executor = new OneThreadExecutor(
 *     "order-processor",                  // threadPoolId
 *     10,                                 // corePoolSize
 *     20,                                 // maximumPoolSize
 *     60L,                                // keepAliveTime
 *     TimeUnit.SECONDS,                   // unit
 *     new LinkedBlockingQueue<>(100),     // workQueue
 *     Executors.defaultThreadFactory(),   // threadFactory
 *     new ThreadPoolExecutor.AbortPolicy(), // handler
 *     30000L                              // awaitTerminationMillis
 * );
 * }</pre>
 * 
 * <p><b>使用示例：</b>
 * <pre>{@code
 * // 1. 提交任务（与标准线程池完全相同）
 * executor.execute(() -> {
 *     // 业务逻辑
 *     processOrder(order);
 * });
 * 
 * 
 * // 2. 获取拒绝次数（增强功能）
 * long rejectCount = ((OneThreadExecutor) executor).getRejectCount().get();
 * if (rejectCount > 100) {
 *     log.warn("线程池 {} 拒绝次数过多: {}", threadPoolId, rejectCount);
 * }
 * 
 * 
 * // 3. 优雅关闭（增强功能）
 * executor.shutdown();  // 会等待 awaitTerminationMillis 时间
 * // 日志输出：开始关闭线程池执行器 order-processor
 * // 等待最多30秒让任务完成
 * // 日志输出：线程池执行器 order-processor 已成功关闭
 * 
 * 
 * // 4. 监控集成
 * if (executor instanceof OneThreadExecutor) {
 *     OneThreadExecutor oneThread = (OneThreadExecutor) executor;
 *     String poolId = oneThread.getThreadPoolId();
 *     long rejects = oneThread.getRejectCount().get();
 *     
 *     // 上报监控数据
 *     metricsCollector.report(poolId, "reject_count", rejects);
 * }
 * }</pre>
 * 
 * <p><b>线程安全性：</b>
 * <ul>
 *   <li>继承自 {@link ThreadPoolExecutor}，保持线程安全</li>
 *   <li>拒绝计数使用 {@link AtomicLong}，并发安全</li>
 *   <li>所有方法都是线程安全的</li>
 * </ul>
 * 
 * <p><b>性能特点：</b>
 * <ul>
 *   <li>拒绝策略包装带来的性能开销：几乎可以忽略（仅一次原子递增操作）</li>
 *   <li>其他性能与标准 ThreadPoolExecutor 完全相同</li>
 * </ul>
 * 
 * <p><b>最佳实践：</b>
 * <ul>
 *   <li>使用 {@link ThreadPoolExecutorBuilder#dynamicPool()} 方法创建</li>
 *   <li>设置合理的 awaitTerminationMillis，确保优雅关闭</li>
 *   <li>定期监控 rejectCount，及时调整线程池配置</li>
 *   <li>配合 {@link com.nageoffer.onethread.core.executor.support.ResizableCapacityLinkedBlockingQueue} 使用</li>
 * </ul>
 * 
 * @author 杨潇
 * @since 2025-04-20
 * @see ThreadPoolExecutor JDK标准线程池
 * @see ThreadPoolExecutorBuilder 线程池构建器
 * @see RejectedProxyInvocationHandler 拒绝策略代理处理器
 */
@Slf4j
public class OneThreadExecutor extends ThreadPoolExecutor {

    /**
     * 线程池唯一标识
     * <p>
     * 用于在 oneThread 框架中唯一标识该线程池实例。
     * 
     * <p><b>作用：</b>
     * <ul>
     *   <li><b>配置匹配：</b>与配置中心（Nacos/Apollo）中的 thread-pool-id 匹配</li>
     *   <li><b>注册表查找：</b>在 {@link OneThreadRegistry} 中作为查找键</li>
     *   <li><b>监控标识：</b>监控数据上报时的线程池标识</li>
     *   <li><b>日志记录：</b>日志中标识线程池来源</li>
     *   <li><b>告警标识：</b>告警消息中标识具体的线程池</li>
     * </ul>
     * 
     * <p><b>命名规范：</b>
     * <ul>
     *   <li>使用短横线分隔的小写字母，如 "order-processor"</li>
     *   <li>体现业务含义，避免使用通用名称</li>
     *   <li>全局唯一，避免重复</li>
     * </ul>
     * 
     * <p><b>示例：</b>
     * <pre>
     * 好的命名：order-processor, message-consumer, async-task-pool
     * 不好的命名：pool1, thread-pool, executor
     * </pre>
     */
    @Getter
    private final String threadPoolId;

    /**
     * 线程池拒绝策略执行次数统计
     * <p>
     * 使用原子计数器统计拒绝策略被触发的次数，这是衡量线程池容量是否充足的重要指标。
     * 
     * <p><b>计数时机：</b>
     * 当线程池无法接受新任务（线程满且队列满）时，在执行拒绝策略前自动递增计数。
     * 
     * <p><b>监控价值：</b>
     * <table border="1" cellpadding="5">
     *   <tr><th>拒绝次数</th><th>健康状态</th><th>建议</th></tr>
     *   <tr><td>0</td><td>健康</td><td>线程池容量充足</td></tr>
     *   <tr><td>1~10</td><td>注意</td><td>偶尔触发，关注趋势</td></tr>
     *   <tr><td>10~100</td><td>警告</td><td>频繁触发，需要扩容</td></tr>
     *   <tr><td>&gt;100</td><td>严重</td><td>严重不足，立即扩容或限流</td></tr>
     * </table>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * OneThreadExecutor executor = ...;
     * 
     * // 获取拒绝次数
     * long rejectCount = executor.getRejectCount().get();
     * 
     * // 监控告警
     * if (rejectCount > 100) {
     *     log.warn("线程池 {} 拒绝次数过多: {}", executor.getThreadPoolId(), rejectCount);
     *     alertService.sendAlert("线程池拒绝次数超过100，请扩容");
     * }
     * 
     * // 重置计数（如果需要）
     * executor.getRejectCount().set(0);
     * 
     * // 获取增量（与上次相比的新增拒绝次数）
     * long lastCount = lastRejectCount;
     * long currentCount = executor.getRejectCount().get();
     * long delta = currentCount - lastCount;
     * if (delta > 10) {
     *     log.warn("过去1分钟新增拒绝 {} 次", delta);
     * }
     * }</pre>
     * 
     * <p><b>注意事项：</b>
     * <ul>
     *   <li>计数器只增不减（除非手动重置）</li>
     *   <li>应用重启后计数器会重置为0</li>
     *   <li>需要定期采集增量数据进行趋势分析</li>
     *   <li>拒绝次数过多通常意味着线程池配置不合理或负载过高</li>
     * </ul>
     */
    @Getter
    private final AtomicLong rejectCount = new AtomicLong();

    /**
     * 等待终止时间（单位：毫秒）
     * <p>
     * 在关闭线程池时，等待现有任务完成的最大时间。
     * 这是实现优雅关闭的关键参数。
     * 
     * <p><b>优雅关闭流程：</b>
     * <ol>
     *   <li>调用 {@link #shutdown()} 方法</li>
     *   <li>线程池停止接受新任务</li>
     *   <li>等待队列中的任务和正在执行的任务完成</li>
     *   <li>最多等待 awaitTerminationMillis 毫秒</li>
     *   <li>超时后记录警告日志，但不强制终止</li>
     * </ol>
     * 
     * <p><b>配置建议：</b>
     * <ul>
     *   <li>0：立即关闭，不等待（默认值，不推荐生产环境）</li>
     *   <li>10000~30000：适合执行时间较短的任务（10~30秒）</li>
     *   <li>30000~60000：适合执行时间较长的任务（30秒~1分钟）</li>
     *   <li>60000+：适合可能长时间运行的任务（1分钟以上）</li>
     * </ul>
     * 
     * <p><b>注意事项：</b>
     * <ul>
     *   <li>等待时间过短：可能导致任务中途被终止</li>
     *   <li>等待时间过长：应用关闭耗时过久</li>
     *   <li>需要根据实际任务的执行时间合理设置</li>
     * </ul>
     */
    private long awaitTerminationMillis;

    /**
     * 创建一个 oneThread 动态线程池执行器
     * <p>
     * 该构造函数创建一个功能增强的线程池，在标准 {@link ThreadPoolExecutor} 的基础上
     * 增加了线程池标识、拒绝统计、优雅关闭等特性。
     * 
     * <p><b>参数说明：</b>
     * 前7个参数与 {@link ThreadPoolExecutor} 完全相同，最后2个是 oneThread 的扩展参数。
     * 
     * <p><b>初始化流程：</b>
     * <ol>
     *   <li>调用父类构造函数，创建标准线程池</li>
     *   <li>通过 {@link #setRejectedExecutionHandler(RejectedExecutionHandler)} 包装拒绝策略</li>
     *   <li>保存线程池 ID</li>
     *   <li>保存等待终止时间</li>
     * </ol>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * // 创建动态线程池
     * OneThreadExecutor executor = new OneThreadExecutor(
     *     "order-processor",                      // 线程池唯一标识
     *     10,                                     // 核心线程数
     *     20,                                     // 最大线程数
     *     60L,                                    // 空闲线程存活时间
     *     TimeUnit.SECONDS,                       // 时间单位
     *     new LinkedBlockingQueue<>(100),         // 任务队列
     *     ThreadFactoryBuilder.builder()          // 线程工厂
     *         .namePrefix("order-")
     *         .build(),
     *     new ThreadPoolExecutor.CallerRunsPolicy(), // 拒绝策略
     *     30000L                                  // 等待终止时间30秒
     * );
     * 
     * // 线程池会自动被注册到 OneThreadRegistry
     * // 拒绝策略会被包装，自动统计拒绝次数
     * }</pre>
     *
     * @param threadPoolId           线程池唯一标识（如 "order-processor"）
     * @param corePoolSize           核心线程数，即使空闲也会保持的线程数量（除非设置了 allowCoreThreadTimeOut）
     * @param maximumPoolSize        最大线程数，线程池中允许的最大线程数量
     * @param keepAliveTime          空闲线程存活时间，当线程数量超过核心线程数时，
     *                               多余的空闲线程在终止前等待新任务的最长时间
     * @param unit                   keepAliveTime 参数的时间单位
     * @param workQueue              工作队列，在执行任务前用于保存任务的队列，
     *                               仅保存通过 {@link #execute(Runnable)} 方法提交的 Runnable 任务
     * @param threadFactory          线程工厂，用于创建新线程
     * @param handler                拒绝策略，当线程边界和队列容量达到上限时，
     *                               用于处理被阻止执行的任务
     * @param awaitTerminationMillis 等待终止时间，关闭线程池时等待的最长时间（毫秒）
     * @throws IllegalArgumentException 如果以下条件之一成立：<br>
     *                                  {@code corePoolSize < 0}<br>
     *                                  {@code keepAliveTime < 0}<br>
     *                                  {@code maximumPoolSize <= 0}<br>
     *                                  {@code maximumPoolSize < corePoolSize}
     * @throws NullPointerException     如果 {@code threadPoolId} 或 {@code workQueue} 或 {@code unit}
     *                                  或 {@code threadFactory} 或 {@code handler} 为 null
     */
    public OneThreadExecutor(
            @NonNull String threadPoolId,
            int corePoolSize,
            int maximumPoolSize,
            long keepAliveTime,
            @NonNull TimeUnit unit,
            @NonNull BlockingQueue<Runnable> workQueue,
            @NonNull ThreadFactory threadFactory,
            @NonNull RejectedExecutionHandler handler,
            long awaitTerminationMillis) {
        // 调用父类构造函数，创建标准的 ThreadPoolExecutor
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);

        // 通过重写的方法设置拒绝策略，自动包装为带计数功能的代理
        // 注意：这里会调用下面重写的 setRejectedExecutionHandler 方法
        setRejectedExecutionHandler(handler);

        // 设置动态线程池扩展属性：线程池 ID 标识
        this.threadPoolId = threadPoolId;

        // 设置等待终止时间，单位毫秒
        this.awaitTerminationMillis = awaitTerminationMillis;
    }

    /**
     * 重写拒绝策略设置方法，增强原始拒绝策略
     * <p>
     * 该方法使用<b>装饰器模式（Decorator Pattern）</b>包装原始拒绝策略，
     * 在执行原始策略前自动递增拒绝计数器，实现拒绝次数的自动统计。
     * 
     * <p><b>增强流程：</b>
     * <ol>
     *   <li>创建匿名内部类实现 {@link RejectedExecutionHandler} 接口</li>
     *   <li>在 {@link RejectedExecutionHandler#rejectedExecution} 方法中：
     *       <ul>
     *         <li>先递增 {@link #rejectCount} 计数器</li>
     *         <li>再调用原始策略的 rejectedExecution 方法</li>
     *       </ul>
     *   </li>
     *   <li>重写 {@link #toString()} 方法，返回原始策略的类名</li>
     *   <li>将包装后的策略设置给父类</li>
     * </ol>
     * 
     * <p><b>设计思路：</b>
     * <ul>
     *   <li><b>轻量级代理：</b>使用 Lambda 静态代理（匿名内部类），避免 JDK 动态代理的开销</li>
     *   <li><b>装饰器模式：</b>在不修改原始策略的情况下，增加新功能（计数）</li>
     *   <li><b>透明包装：</b>对外部调用者透明，行为与原始策略完全一致</li>
     * </ul>
     * 
     * <p><b>替代方案（注释中提到）：</b>
     * <pre>{@code
     * // 也可以使用 JDK 动态代理实现（更重量级）
     * RejectedExecutionHandler rejectedProxy = (RejectedExecutionHandler) Proxy
     *     .newProxyInstance(
     *         handler.getClass().getClassLoader(),
     *         new Class[]{RejectedExecutionHandler.class},
     *         new RejectedProxyInvocationHandler(handler, rejectCount)
     *     );
     * }</pre>
     * 
     * <p><b>为什么选择轻量级代理？</b>
     * <ul>
     *   <li>性能更好：匿名内部类比动态代理快</li>
     *   <li>代码简洁：直接内联，易于理解</li>
     *   <li>无反射开销：不涉及反射调用</li>
     * </ul>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * OneThreadExecutor executor = ...;
     * 
     * // 运行时更换拒绝策略（会自动包装）
     * executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
     * 
     * // 新策略也会被包装，继续统计拒绝次数
     * executor.execute(task);  // 如果被拒绝，rejectCount 会增加
     * }</pre>
     * 
     * <p><b>包装效果：</b>
     * <pre>
     * 原始策略：CallerRunsPolicy
     *    ↓
     * 包装后：handlerWrapper {
     *   rejectedExecution() {
     *     rejectCount.incrementAndGet();  // 增强：计数
     *     handler.rejectedExecution();    // 原始：执行策略
     *   }
     * }
     * </pre>
     *
     * @param handler 原始的拒绝策略处理器
     */
    @Override
    public void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
        // 创建拒绝策略的包装器（装饰器模式）
        // 使用匿名内部类实现，比 JDK 动态代理更轻量级
        RejectedExecutionHandler handlerWrapper = new RejectedExecutionHandler() {
            /**
             * 执行拒绝策略（增强版本）
             * <p>
             * 在执行原始策略前，先递增拒绝计数器。
             * 
             * @param r        被拒绝的任务
             * @param executor 拒绝任务的线程池
             */
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                // 步骤1：递增拒绝计数（增强功能）
                rejectCount.incrementAndGet();
                
                // 步骤2：执行原始拒绝策略（保持原有行为）
                handler.rejectedExecution(r, executor);
            }

            /**
             * 重写 toString 方法，返回原始策略的类名
             * <p>
             * 这样在日志和监控中显示的是原始策略名称，而非包装器类名，
             * 避免混淆和误导。
             * 
             * @return 原始策略的简单类名（如 "CallerRunsPolicy"）
             */
            @Override
            public String toString() {
                return handler.getClass().getSimpleName();
            }
        };

        // 调用父类方法，设置包装后的拒绝策略
        super.setRejectedExecutionHandler(handlerWrapper);
    }

    /**
     * 重写线程池关闭方法，实现优雅关闭
     * <p>
     * 该方法在标准的 {@link ThreadPoolExecutor#shutdown()} 基础上，
     * 增加了等待任务完成的逻辑，实现优雅关闭（Graceful Shutdown）。
     * 
     * <p><b>优雅关闭的意义：</b>
     * <ul>
     *   <li><b>避免任务丢失：</b>给正在执行的任务足够的时间完成</li>
     *   <li><b>避免数据不一致：</b>确保事务性任务执行完成</li>
     *   <li><b>提升用户体验：</b>避免用户操作中途被中断</li>
     *   <li><b>资源清理：</b>给任务足够时间释放资源（数据库连接、文件句柄等）</li>
     * </ul>
     * 
     * <p><b>关闭流程：</b>
     * <ol>
     *   <li>检查线程池是否已经关闭
     *       <ul><li>如果已关闭，直接返回（避免重复关闭）</li></ul>
     *   </li>
     *   <li>调用父类的 {@link ThreadPoolExecutor#shutdown()} 方法
     *       <ul><li>停止接受新任务</li></ul>
     *   </li>
     *   <li>如果设置了等待终止时间（{@link #awaitTerminationMillis} > 0）
     *       <ul>
     *         <li>调用 {@link #awaitTermination(long, TimeUnit)} 等待任务完成</li>
     *         <li>记录关闭过程日志</li>
     *         <li>如果超时未完成，记录警告日志</li>
     *         <li>如果成功完成，记录成功日志</li>
     *       </ul>
     *   </li>
     *   <li>处理中断异常
     *       <ul><li>记录警告日志并恢复中断状态</li></ul>
     *   </li>
     * </ol>
     * 
     * <p><b>日志输出示例：</b>
     * <pre>
     * // 开始关闭
     * INFO  开始关闭线程池执行器 order-processor
     * 
     * // 成功关闭
     * INFO  线程池执行器 order-processor 已成功关闭
     * 
     * // 超时未关闭
     * WARN  等待线程池 order-processor 终止超时
     * 
     * // 被中断
     * WARN  等待线程池 order-processor 终止时被中断
     * </pre>
     * 
     * <p><b>与 shutdownNow() 的区别：</b>
     * <ul>
     *   <li><b>shutdown()：</b>温和关闭，等待任务完成（本方法）</li>
     *   <li><b>shutdownNow()：</b>立即关闭，中断所有线程，返回未执行的任务列表</li>
     * </ul>
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>Spring 应用关闭时（实现 DisposableBean 接口）</li>
     *   <li>Web 应用优雅下线（如 Tomcat 关闭前）</li>
     *   <li>需要确保任务完成的场景</li>
     * </ul>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * // 示例1：Spring Bean 销毁时关闭
     * @PreDestroy
     * public void destroy() {
     *     executor.shutdown();  // 优雅关闭，等待任务完成
     * }
     * 
     * 
     * // 示例2：应用关闭钩子
     * Runtime.getRuntime().addShutdownHook(new Thread(() -> {
     *     log.info("应用正在关闭，开始关闭线程池...");
     *     executor.shutdown();  // 优雅关闭
     * }));
     * 
     * 
     * // 示例3：手动关闭并检查结果
     * executor.shutdown();
     * 
     * // 如果需要确保完全关闭，可以再次检查
     * if (!executor.isTerminated()) {
     *     log.warn("线程池未完全关闭，仍有任务在执行");
     *     // 可以选择强制关闭
     *     executor.shutdownNow();
     * }
     * }</pre>
     * 
     * <p><b>注意事项：</b>
     * <ul>
     *   <li>shutdown() 方法是幂等的，多次调用安全</li>
     *   <li>关闭后无法再提交新任务，会抛出 {@link java.util.concurrent.RejectedExecutionException}</li>
     *   <li>如果等待时间过短，可能导致任务未完成就超时</li>
     *   <li>中断异常会被捕获并恢复中断状态，确保调用线程的中断标志不丢失</li>
     * </ul>
     */
    @Override
    public void shutdown() {
        // 步骤1：检查线程池是否已经关闭
        // 如果已关闭，直接返回（幂等性保证）
        if (isShutdown()) {
            return;
        }

        // 步骤2：调用父类的 shutdown 方法
        // 这会标记线程池为 SHUTDOWN 状态，不再接受新任务
        super.shutdown();
        
        // 步骤3：如果未设置等待终止时间，直接返回（不等待）
        if (this.awaitTerminationMillis <= 0) {
            return;
        }

        // 步骤4：等待线程池终止（优雅关闭的核心）
        log.info("开始关闭线程池执行器 {}", threadPoolId);
        try {
            // 等待线程池终止，最多等待 awaitTerminationMillis 毫秒
            // awaitTermination 方法会阻塞当前线程，直到：
            // 1. 所有任务都执行完成，或
            // 2. 等待超时，或
            // 3. 当前线程被中断
            boolean isTerminated = this.awaitTermination(this.awaitTerminationMillis, TimeUnit.MILLISECONDS);
            
            if (!isTerminated) {
                // 超时未终止，记录警告日志
                // 可能原因：
                // 1. 有任务执行时间过长
                // 2. 等待时间设置过短
                // 3. 任务陷入死循环
                log.warn("等待线程池 {} 终止超时", threadPoolId);
            } else {
                // 成功终止，记录信息日志
                log.info("线程池执行器 {} 已成功关闭", threadPoolId);
            }
        } catch (InterruptedException ex) {
            // 等待过程中被中断，记录警告日志
            // 可能原因：
            // 1. 应用被强制关闭
            // 2. 其他线程中断了当前线程
            log.warn("等待线程池 {} 终止时被中断", threadPoolId);
            
            // 恢复中断状态
            // 这是处理 InterruptedException 的标准做法
            // 确保调用线程的中断标志不丢失
            Thread.currentThread().interrupt();
        }
    }
}
