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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 线程池运行时监控信息实体
 * <p>
 * 该类封装了线程池在某一时刻的完整运行状态数据，包括线程信息、队列信息、
 * 任务统计等，用于监控数据采集、日志记录、Prometheus 指标上报等场景。
 * 
 * <p><b>核心作用：</b>
 * <ul>
 *   <li><b>数据快照：</b>保存线程池在某一时刻的状态快照</li>
 *   <li><b>监控上报：</b>作为监控数据的传输对象（DTO）</li>
 *   <li><b>日志记录：</b>序列化为 JSON 记录到日志文件</li>
 *   <li><b>Prometheus集成：</b>作为 Gauge 指标的数据源</li>
 * </ul>
 * 
 * <p><b>数据分类：</b>
 * <table border="1" cellpadding="5">
 *   <tr><th>分类</th><th>字段</th><th>说明</th></tr>
 *   <tr>
 *     <td rowspan="5"><b>线程信息</b></td>
 *     <td>corePoolSize</td><td>核心线程数</td>
 *   </tr>
 *   <tr><td>maximumPoolSize</td><td>最大线程数</td></tr>
 *   <tr><td>currentPoolSize</td><td>当前线程数</td></tr>
 *   <tr><td>activePoolSize</td><td>活跃线程数</td></tr>
 *   <tr><td>largestPoolSize</td><td>历史最大线程数</td></tr>
 *   <tr>
 *     <td rowspan="4"><b>队列信息</b></td>
 *     <td>workQueueName</td><td>队列类型</td>
 *   </tr>
 *   <tr><td>workQueueCapacity</td><td>队列总容量</td></tr>
 *   <tr><td>workQueueSize</td><td>当前队列大小</td></tr>
 *   <tr><td>workQueueRemainingCapacity</td><td>队列剩余容量</td></tr>
 *   <tr>
 *     <td rowspan="3"><b>任务统计</b></td>
 *     <td>completedTaskCount</td><td>已完成任务数</td>
 *   </tr>
 *   <tr><td>rejectedHandlerName</td><td>拒绝策略类型</td></tr>
 *   <tr><td>rejectCount</td><td>拒绝次数</td></tr>
 *   <tr>
 *     <td><b>标识信息</b></td>
 *     <td>threadPoolId</td><td>线程池唯一标识</td>
 *   </tr>
 * </table>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li><b>监控采集：</b>{@link ThreadPoolMonitor} 定时采集线程池状态</li>
 *   <li><b>日志记录：</b>序列化为 JSON 记录到日志文件</li>
 *   <li><b>Prometheus：</b>作为 Gauge 指标的数据源</li>
 *   <li><b>Web接口：</b>返回给前端控制台展示</li>
 *   <li><b>数据分析：</b>导出为 CSV 进行离线分析</li>
 * </ul>
 * 
 * <p><b>使用示例：</b>
 * <pre>{@code
 * // 示例1：构建运行时信息
 * ThreadPoolExecutor executor = ...;
 * BlockingQueue<?> queue = executor.getQueue();
 * 
 * ThreadPoolRuntimeInfo info = ThreadPoolRuntimeInfo.builder()
 *     .threadPoolId("order-processor")
 *     .corePoolSize(executor.getCorePoolSize())
 *     .maximumPoolSize(executor.getMaximumPoolSize())
 *     .currentPoolSize(executor.getPoolSize())
 *     .activePoolSize(executor.getActiveCount())
 *     .largestPoolSize(executor.getLargestPoolSize())
 *     .completedTaskCount(executor.getCompletedTaskCount())
 *     .workQueueName(queue.getClass().getSimpleName())
 *     .workQueueSize(queue.size())
 *     .workQueueCapacity(queue.size() + queue.remainingCapacity())
 *     .workQueueRemainingCapacity(queue.remainingCapacity())
 *     .rejectedHandlerName(executor.getRejectedExecutionHandler().toString())
 *     .rejectCount(0L)
 *     .build();
 * 
 * 
 * // 示例2：日志记录
 * log.info("线程池监控数据: {}", JSON.toJSONString(info));
 * // 输出：{"threadPoolId":"order-processor","corePoolSize":10,"activePoolSize":8,...}
 * 
 * 
 * // 示例3：计算派生指标
 * int activePoolSize = info.getActivePoolSize();
 * int maximumPoolSize = info.getMaximumPoolSize();
 * double activeRate = (double) activePoolSize / maximumPoolSize * 100;
 * System.out.println("活跃线程率: " + activeRate + "%");
 * 
 * int queueSize = info.getWorkQueueSize();
 * int queueCapacity = info.getWorkQueueCapacity();
 * double queueUsage = (double) queueSize / queueCapacity * 100;
 * System.out.println("队列使用率: " + queueUsage + "%");
 * }</pre>
 * 
 * <p><b>JSON序列化示例：</b>
 * <pre>
 * {
 *   "threadPoolId": "order-processor",
 *   "corePoolSize": 10,
 *   "maximumPoolSize": 20,
 *   "currentPoolSize": 15,
 *   "activePoolSize": 12,
 *   "largestPoolSize": 18,
 *   "completedTaskCount": 50000,
 *   "workQueueName": "LinkedBlockingQueue",
 *   "workQueueCapacity": 500,
 *   "workQueueSize": 350,
 *   "workQueueRemainingCapacity": 150,
 *   "rejectedHandlerName": "CallerRunsPolicy",
 *   "rejectCount": 5
 * }
 * </pre>
 * 
 * @author 杨潇
 * @since 2025-05-05
 * @see ThreadPoolMonitor 线程池监控器
 * @see java.util.concurrent.ThreadPoolExecutor 线程池执行器
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreadPoolRuntimeInfo {

    /**
     * 线程池唯一标识
     * <p>
     * 用于标识具体的线程池实例，与配置中心的 thread-pool-id 对应。
     * 
     * <p><b>示例：</b>"order-processor"、"message-consumer"
     */
    private String threadPoolId;

    /**
     * 核心线程数
     * <p>
     * 线程池中始终保持的最小线程数量（即使这些线程处于空闲状态）。
     * 
     * <p><b>来源：</b>{@link java.util.concurrent.ThreadPoolExecutor#getCorePoolSize()}
     * 
     * <p><b>监控意义：</b>
     * <ul>
     *   <li>核心线程数是线程池的基准配置</li>
     *   <li>用于计算活跃线程率的基准值</li>
     *   <li>配置变更后，该值会相应变化</li>
     * </ul>
     */
    private Integer corePoolSize;

    /**
     * 最大线程数
     * <p>
     * 线程池允许创建的最大线程数量，也是线程池的容量上限。
     * 
     * <p><b>来源：</b>{@link java.util.concurrent.ThreadPoolExecutor#getMaximumPoolSize()}
     * 
     * <p><b>监控意义：</b>
     * <ul>
     *   <li>线程池的最大处理能力</li>
     *   <li>用于计算活跃线程率（活跃数/最大数）</li>
     *   <li>如果当前线程数接近最大数，说明线程池负载高</li>
     * </ul>
     */
    private Integer maximumPoolSize;

    /**
     * 当前线程数
     * <p>
     * 线程池中当前存在的线程总数（包括空闲和活跃的线程）。
     * 
     * <p><b>来源：</b>{@link java.util.concurrent.ThreadPoolExecutor#getPoolSize()}
     * 
     * <p><b>监控意义：</b>
     * <ul>
     *   <li>反映线程池的实际线程数量</li>
     *   <li>取值范围：[0, maximumPoolSize]</li>
     *   <li>如果 = maximumPoolSize，说明线程池已扩容到上限</li>
     *   <li>如果 = corePoolSize，说明线程池处于稳定状态</li>
     * </ul>
     * 
     * <p><b>注意：</b>该方法内部有锁，避免高频率调用。
     */
    private Integer currentPoolSize;

    /**
     * 活跃线程数
     * <p>
     * 正在执行任务的线程数量（不包括空闲等待的线程）。
     * 
     * <p><b>来源：</b>{@link java.util.concurrent.ThreadPoolExecutor#getActiveCount()}
     * 
     * <p><b>监控意义：</b>
     * <ul>
     *   <li>反映线程池的实时工作负载</li>
     *   <li>活跃线程率 = (活跃数 / 最大数) × 100</li>
     *   <li>如果活跃率持续高于80%，说明负载较高</li>
     *   <li>如果活跃率很低，说明可能配置过多线程</li>
     * </ul>
     * 
     * <p><b>注意：</b>
     * <ul>
     *   <li>该值是近似值，因为线程状态是动态变化的</li>
     *   <li>该方法内部有锁，避免高频率调用</li>
     * </ul>
     */
    private Integer activePoolSize;

    /**
     * 历史最大线程数
     * <p>
     * 线程池从创建到现在，曾经同时存在的最大线程数。
     * 
     * <p><b>来源：</b>{@link java.util.concurrent.ThreadPoolExecutor#getLargestPoolSize()}
     * 
     * <p><b>监控意义：</b>
     * <ul>
     *   <li>反映线程池历史的峰值负载</li>
     *   <li>用于评估最大线程数配置是否合理</li>
     *   <li>如果 = maximumPoolSize，说明线程池曾达到满载</li>
     *   <li>如果远小于 maximumPoolSize，说明最大线程数可能配置过高</li>
     * </ul>
     * 
     * <p><b>注意：</b>该方法内部有锁，避免高频率调用。
     */
    private Integer largestPoolSize;

    /**
     * 已完成任务总数
     * <p>
     * 线程池从创建到现在，已完成的任务总数（累计值）。
     * 
     * <p><b>来源：</b>{@link java.util.concurrent.ThreadPoolExecutor#getCompletedTaskCount()}
     * 
     * <p><b>监控意义：</b>
     * <ul>
     *   <li>反映线程池的总体工作量</li>
     *   <li>用于计算任务处理速率（TPS）</li>
     *   <li>通过增量计算得到时间段内的任务完成数</li>
     * </ul>
     * 
     * <p><b>计算示例：</b>
     * <pre>
     * 时刻T1：completedTaskCount = 1000
     * 时刻T2（T1后60秒）：completedTaskCount = 1500
     * 时间段任务数 = 1500 - 1000 = 500
     * TPS = 500 / 60 ≈ 8.33 任务/秒
     * </pre>
     * 
     * <p><b>注意：</b>该方法内部有锁，避免高频率调用。
     */
    private Long completedTaskCount;

    /**
     * 阻塞队列类型名称
     * <p>
     * 队列的类名（简单类名），如 "LinkedBlockingQueue"、"ArrayBlockingQueue"。
     * 
     * <p><b>来源：</b>{@code queue.getClass().getSimpleName()}
     * 
     * <p><b>监控意义：</b>
     * <ul>
     *   <li>了解线程池使用的队列类型</li>
     *   <li>不同队列类型有不同的性能特征</li>
     *   <li>用于问题排查和性能分析</li>
     * </ul>
     * 
     * <p><b>常见值：</b>
     * <ul>
     *   <li>"LinkedBlockingQueue" - 最常用</li>
     *   <li>"ArrayBlockingQueue" - 内存敏感场景</li>
     *   <li>"SynchronousQueue" - 快速响应场景</li>
     *   <li>"ResizableCapacityLinkedBlockingQueue" - 动态线程池</li>
     * </ul>
     */
    private String workQueueName;

    /**
     * 队列总容量
     * <p>
     * 队列能够容纳的最大元素数量。
     * 
     * <p><b>计算公式：</b>
     * <pre>
     * 队列总容量 = 当前队列大小 + 剩余容量
     * </pre>
     * 
     * <p><b>监控意义：</b>
     * <ul>
     *   <li>队列的配置容量</li>
     *   <li>用于计算队列使用率（大小/容量）</li>
     *   <li>评估队列配置是否合理</li>
     * </ul>
     * 
     * <p><b>特殊值：</b>
     * <ul>
     *   <li>0：SynchronousQueue（无缓冲）</li>
     *   <li>Integer.MAX_VALUE：无界队列</li>
     * </ul>
     */
    private Integer workQueueCapacity;

    /**
     * 队列当前元素数量
     * <p>
     * 队列中当前等待执行的任务数量。
     * 
     * <p><b>来源：</b>{@link java.util.concurrent.BlockingQueue#size()}
     * 
     * <p><b>监控意义：</b>
     * <ul>
     *   <li>反映任务堆积程度</li>
     *   <li>如果持续增长，说明任务处理速度跟不上提交速度</li>
     *   <li>如果接近容量，可能即将触发拒绝策略</li>
     * </ul>
     * 
     * <p><b>健康标准：</b>
     * <ul>
     *   <li>< 容量的30%：健康</li>
     *   <li>30%~60%：正常</li>
     *   <li>60%~80%：注意</li>
     *   <li>> 80%：警告</li>
     * </ul>
     */
    private Integer workQueueSize;

    /**
     * 队列剩余容量
     * <p>
     * 队列还能容纳多少个元素。
     * 
     * <p><b>来源：</b>{@link java.util.concurrent.BlockingQueue#remainingCapacity()}
     * 
     * <p><b>计算关系：</b>
     * <pre>
     * 剩余容量 = 总容量 - 当前大小
     * </pre>
     * 
     * <p><b>监控意义：</b>
     * <ul>
     *   <li>反映队列的剩余缓冲空间</li>
     *   <li>如果剩余容量很小，说明队列即将满</li>
     *   <li>用于判断是否需要扩容</li>
     * </ul>
     */
    private Integer workQueueRemainingCapacity;

    /**
     * 拒绝策略类型名称
     * <p>
     * 拒绝策略的类名（简单类名），如 "CallerRunsPolicy"、"AbortPolicy"。
     * 
     * <p><b>来源：</b>{@code executor.getRejectedExecutionHandler().toString()}
     * 
     * <p><b>监控意义：</b>
     * <ul>
     *   <li>了解线程池的拒绝策略配置</li>
     *   <li>不同策略对系统的影响不同</li>
     *   <li>配合拒绝次数分析任务丢失情况</li>
     * </ul>
     * 
     * <p><b>常见值：</b>
     * <ul>
     *   <li>"CallerRunsPolicy" - 调用者运行</li>
     *   <li>"AbortPolicy" - 抛出异常</li>
     *   <li>"DiscardPolicy" - 静默丢弃</li>
     *   <li>"DiscardOldestPolicy" - 丢弃最旧</li>
     * </ul>
     */
    private String rejectedHandlerName;

    /**
     * 拒绝策略执行次数
     * <p>
     * 从线程池创建到现在，拒绝策略被触发的总次数（累计值）。
     * 
     * <p><b>来源：</b>
     * <ul>
     *   <li>OneThreadExecutor：{@link OneThreadExecutor#getRejectCount()}</li>
     *   <li>标准 ThreadPoolExecutor：-1（不支持统计）</li>
     * </ul>
     * 
     * <p><b>监控意义：</b>
     * <ul>
     *   <li>反映线程池容量是否充足</li>
     *   <li>拒绝次数越多，说明容量越不足</li>
     *   <li>通过增量计算得到时间段内的拒绝次数</li>
     * </ul>
     * 
     * <p><b>健康标准：</b>
     * <table border="1" cellpadding="5">
     *   <tr><th>拒绝次数</th><th>健康状态</th><th>建议</th></tr>
     *   <tr><td>0</td><td>健康</td><td>容量充足</td></tr>
     *   <tr><td>1~10</td><td>注意</td><td>偶尔触发，关注趋势</td></tr>
     *   <tr><td>10~100</td><td>警告</td><td>频繁触发，需要扩容</td></tr>
     *   <tr><td>&gt;100</td><td>严重</td><td>严重不足，立即处理</td></tr>
     * </table>
     * 
     * <p><b>特殊值：</b>
     * <ul>
     *   <li>-1：表示线程池不支持拒绝统计（标准 ThreadPoolExecutor）</li>
     *   <li>0：表示从未触发拒绝策略</li>
     *   <li>> 0：表示触发过拒绝策略</li>
     * </ul>
     */
    private Long rejectCount;
}
