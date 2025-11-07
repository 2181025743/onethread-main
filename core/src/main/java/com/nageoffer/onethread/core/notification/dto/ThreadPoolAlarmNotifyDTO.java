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

package com.nageoffer.onethread.core.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.function.Supplier;

/**
 * 线程池运行时告警通知数据传输对象（DTO）
 * <p>
 * 该类封装了线程池运行时告警所需的所有信息，包括告警类型、线程池状态、
 * 队列状态、拒绝统计等。使用 <b>Supplier 模式</b>实现延迟加载，
 * 只有在真正需要发送告警时才采集详细数据，提高性能。
 * 
 * <p><b>核心功能：</b>
 * <ul>
 *   <li><b>数据承载：</b>封装告警所需的所有数据</li>
 *   <li><b>延迟加载：</b>使用 Supplier 模式延迟加载详细数据</li>
 *   <li><b>链式调用：</b>支持链式设置属性（@Accessors(chain = true)）</li>
 *   <li><b>Builder模式：</b>支持建造者模式构建对象</li>
 * </ul>
 * 
 * <p><b>延迟加载设计：</b>
 * <pre>
 * 创建 DTO 时：
 *   只设置基本信息（threadPoolId、alarmType、interval）
 *   设置 Supplier 函数（用于延迟加载详细数据）
 *    ↓
 * 告警限流检查：
 *   AlarmRateLimiter.allowAlarm() → false（被限流）
 *    ↓
 * 不调用 resolve()：
 *   Supplier 不被执行，节省性能 ✅
 * 
 * ───────────────────────────────────────
 * 
 * 创建 DTO 时：
 *   只设置基本信息
 *   设置 Supplier 函数
 *    ↓
 * 告警限流检查：
 *   AlarmRateLimiter.allowAlarm() → true（通过限流）
 *    ↓
 * 调用 resolve()：
 *   Supplier 被执行，采集详细数据 ✅
 *    ↓
 * 发送告警：
 *   使用完整数据发送消息
 * </pre>
 * 
 * <p><b>性能优化效果：</b>
 * <pre>
 * 场景：队列使用率持续超过80%，每5秒检查一次，限流间隔5分钟
 * 
 * 5分钟内共检查 60 次：
 *   - 第1次：通过限流 → 调用 resolve() → 采集数据 → 发送告警
 *   - 第2-60次：被限流 → 不调用 resolve() → 不采集数据 → 不发送
 * 
 * 性能节省：
 *   - 59次数据采集操作（每次涉及多个线程池API调用，部分有锁）
 *   - 59次消息发送操作（HTTP请求，耗时100-500ms）
 * </pre>
 * 
 * <p><b>告警类型：</b>
 * <table border="1" cellpadding="5">
 *   <tr><th>alarmType值</th><th>告警名称</th><th>触发条件</th></tr>
 *   <tr><td>Capacity</td><td>队列堆积告警</td><td>队列使用率 ≥ queueThreshold</td></tr>
 *   <tr><td>Activity</td><td>线程满载告警</td><td>活跃线程率 ≥ activeThreshold</td></tr>
 *   <tr><td>Reject</td><td>任务拒绝告警</td><td>拒绝次数有新增</td></tr>
 * </table>
 * 
 * <p><b>使用示例：</b>
 * <pre>{@code
 * // 示例1：创建告警 DTO（延迟加载模式）
 * ThreadPoolAlarmNotifyDTO alarm = ThreadPoolAlarmNotifyDTO.builder()
 *     .threadPoolId("order-processor")
 *     .alarmType("Capacity")
 *     .interval(5)
 *     .build();
 * 
 * // 设置延迟加载的 Supplier
 * alarm.setSupplier(() -> {
 *     // 采集详细数据（仅在 resolve() 被调用时执行）
     *     alarm.setApplicationName("order-service");
 *     alarm.setActiveProfile("production");
 *     alarm.setIdentify("192.168.1.100");
 *     alarm.setCorePoolSize(executor.getCorePoolSize());
 *     alarm.setMaximumPoolSize(executor.getMaximumPoolSize());
 *     alarm.setActivePoolSize(executor.getActiveCount());
 *     // ... 设置其他字段
 *     return alarm;
 * });
 * 
 * // 发送告警（会先限流检查，通过才调用 resolve()）
 * notifierDispatcher.sendAlarmMessage(alarm);
 * 
 * 
 * // 示例2：立即加载模式（不推荐）
 * ThreadPoolAlarmNotifyDTO alarm = ThreadPoolAlarmNotifyDTO.builder()
 *     .threadPoolId("order-processor")
 *     .alarmType("Capacity")
 *     .applicationName("order-service")
 *     .corePoolSize(10)
 *     // ... 直接设置所有字段
 *     .build();
 * 
 * // 这种方式会立即采集数据，即使最终被限流也会浪费性能
 * 
 * 
 * // 示例3：手动调用 resolve()
 * ThreadPoolAlarmNotifyDTO alarm = ...;  // 设置了 Supplier
 * 
 * // 只有通过限流检查，才调用 resolve()
 * if (AlarmRateLimiter.allowAlarm(...)) {
 *     ThreadPoolAlarmNotifyDTO resolved = alarm.resolve();  // 触发 Supplier 执行
 *     sendToNotifier(resolved);  // 使用完整数据发送
 * }
 * }</pre>
 * 
 * <p><b>最佳实践：</b>
 * <ul>
 *   <li>使用 Supplier 模式延迟加载数据</li>
 *   <li>只设置必要的基本信息（threadPoolId、alarmType、interval）</li>
 *   <li>在 Supplier 中采集详细的运行时数据</li>
 *   <li>让 {@link NotifierDispatcher} 决定何时调用 resolve()</li>
 * </ul>
 * 
 * @author 杨潇
 * @since 2025-05-05
 * @see AlarmRateLimiter 告警限流器
 * @see NotifierDispatcher 通知分发器
 * @see DingTalkMessageService 钉钉通知服务
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ThreadPoolAlarmNotifyDTO {

    /**
     * 线程池唯一标识
     * <p>
     * 用于标识告警来源于哪个线程池。
     * 
     * <p><b>示例：</b>"order-processor"、"message-consumer"
     */
    private String threadPoolId;

    /**
     * 应用名称
     * <p>
     * 标识告警来源于哪个应用。
     * 
     * <p><b>来源：</b>{@link ApplicationProperties#getApplicationName()}
     * 
     * <p><b>示例：</b>"order-service"、"payment-service"
     */
    private String applicationName;

    /**
     * 环境标识
     * <p>
     * 标识告警来源于哪个运行环境。
     * 
     * <p><b>来源：</b>{@link ApplicationProperties#getActiveProfile()}
     * 
     * <p><b>示例：</b>"dev"、"test"、"production"
     */
    private String activeProfile;

    /**
     * 应用实例唯一标识（IP地址）
     * <p>
     * 标识告警来源于哪个具体的应用实例。
     * 
     * <p><b>来源：</b>{@link java.net.InetAddress#getLocalHost()}.getHostAddress()
     * 
     * <p><b>示例：</b>"192.168.1.100"、"10.0.0.50"
     */
    private String identify;

    /**
     * 通知接收人
     * <p>
     * 告警消息的接收人列表，多个接收人用逗号分隔。
     * 
     * <p><b>格式：</b>
     * <ul>
     *   <li>钉钉：手机号，如 "13800138000,13900139000"</li>
     *   <li>邮件：邮箱地址，如 "user1@company.com,user2@company.com"</li>
     * </ul>
     * 
     * <p><b>@ 功能：</b>钉钉会 @ 这些手机号的对应用户
     */
    private String receives;

    /**
     * 告警类型
     * <p>
     * 标识具体的告警原因。
     * 
     * <p><b>可选值：</b>
     * <ul>
     *   <li>"Capacity" - 队列堆积告警</li>
     *   <li>"Activity" - 线程满载告警</li>
     *   <li>"Reject" - 任务拒绝告警</li>
     * </ul>
     */
    private String alarmType;

    /**
     * 核心线程数
     * <p>
     * 线程池配置的核心线程数。
     * 
     * <p><b>来源：</b>{@link java.util.concurrent.ThreadPoolExecutor#getCorePoolSize()}
     */
    private Integer corePoolSize;

    /**
     * 最大线程数
     * <p>
     * 线程池配置的最大线程数。
     * 
     * <p><b>来源：</b>{@link java.util.concurrent.ThreadPoolExecutor#getMaximumPoolSize()}
     */
    private Integer maximumPoolSize;

    /**
     * 当前线程数
     * <p>
     * 线程池中当前存在的线程总数。
     * 
     * <p><b>来源：</b>{@link java.util.concurrent.ThreadPoolExecutor#getPoolSize()}
     */
    private Integer currentPoolSize;

    /**
     * 活跃线程数
     * <p>
     * 正在执行任务的线程数量。
     * 
     * <p><b>来源：</b>{@link java.util.concurrent.ThreadPoolExecutor#getActiveCount()}
     */
    private Integer activePoolSize;

    /**
     * 历史最大线程数
     * <p>
     * 线程池从创建到现在，曾经同时存在的最大线程数。
     * 
     * <p><b>来源：</b>{@link java.util.concurrent.ThreadPoolExecutor#getLargestPoolSize()}
     */
    private Integer largestPoolSize;

    /**
     * 已完成任务总数
     * <p>
     * 线程池从创建到现在，已完成的任务总数（累计值）。
     * 
     * <p><b>来源：</b>{@link java.util.concurrent.ThreadPoolExecutor#getCompletedTaskCount()}
     */
    private Long completedTaskCount;

    /**
     * 阻塞队列类型名称
     * <p>
     * 队列的类名，如 "LinkedBlockingQueue"。
     * 
     * <p><b>来源：</b>{@code queue.getClass().getSimpleName()}
     */
    private String workQueueName;

    /**
     * 队列总容量
     * <p>
     * 队列能够容纳的最大元素数量。
     * 
     * <p><b>计算：</b>队列大小 + 剩余容量
     */
    private Integer workQueueCapacity;

    /**
     * 队列当前元素数量
     * <p>
     * 队列中当前等待执行的任务数量。
     * 
     * <p><b>来源：</b>{@link java.util.concurrent.BlockingQueue#size()}
     */
    private Integer workQueueSize;

    /**
     * 队列剩余容量
     * <p>
     * 队列还能容纳多少个元素。
     * 
     * <p><b>来源：</b>{@link java.util.concurrent.BlockingQueue#remainingCapacity()}
     */
    private Integer workQueueRemainingCapacity;

    /**
     * 拒绝策略类型名称
     * <p>
     * 拒绝策略的类名，如 "CallerRunsPolicy"。
     * 
     * <p><b>来源：</b>{@code executor.getRejectedExecutionHandler().toString()}
     */
    private String rejectedHandlerName;

    /**
     * 拒绝策略执行次数
     * <p>
     * 从线程池创建到现在，拒绝策略被触发的总次数。
     * 
     * <p><b>来源：</b>{@link com.nageoffer.onethread.core.executor.OneThreadExecutor#getRejectCount()}
     * 
     * <p><b>特殊值：</b>-1 表示线程池不支持拒绝统计
     */
    private Long rejectCount;

    /**
     * 当前时间
     * <p>
     * 告警发生的时间，格式如 "2025-04-30 15:30:45"。
     * 
     * <p><b>来源：</b>{@link cn.hutool.core.date.DateUtil#now()}
     */
    private String currentTime;

    /**
     * 告警间隔时间（单位：分钟）
     * <p>
     * 用于告警限流，同一个线程池的同一类型告警在该时间内只发送一次。
     * 
     * <p><b>作用：</b>传递给 {@link AlarmRateLimiter#allowAlarm} 进行限流检查
     * 
     * <p><b>配置示例：</b>
     * <ul>
     *   <li>5 - 5分钟内只告警一次</li>
     *   <li>10 - 10分钟内只告警一次</li>
     * </ul>
     */
    private Integer interval;

    /**
     * 延迟加载数据的 Supplier 函数
     * <p>
     * 该字段是延迟加载机制的核心，保存一个函数，用于在需要时采集详细数据。
     * 
     * <p><b>设计目的：</b>
     * <ul>
     *   <li>避免在告警被限流时浪费性能采集数据</li>
     *   <li>将数据采集逻辑与告警判断逻辑解耦</li>
     *   <li>提高系统整体性能</li>
     * </ul>
     * 
     * <p><b>字段注解：</b>
     * <ul>
     *   <li><b>@ToString.Exclude：</b>在 toString() 时排除该字段（Supplier 不可序列化）</li>
     *   <li><b>transient：</b>序列化时跳过该字段（Supplier 是临时的函数）</li>
     * </ul>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * alarm.setSupplier(() -> {
     *     // 采集线程池运行时数据
     *     ThreadPoolExecutor executor = holder.getExecutor();
     *     
     *     alarm.setCorePoolSize(executor.getCorePoolSize());
     *     alarm.setActivePoolSize(executor.getActiveCount());
     *     alarm.setWorkQueueSize(executor.getQueue().size());
     *     // ... 设置其他字段
     *     
     *     return alarm;  // 返回填充完整数据的 alarm 对象
     * });
     * }</pre>
     */
    @ToString.Exclude
    private transient Supplier<ThreadPoolAlarmNotifyDTO> supplier;

    /**
     * 解析（延迟加载）告警详细数据
     * <p>
     * 该方法触发 Supplier 的执行，采集线程池的详细运行时数据。
     * 只有在告警通过限流检查后，才应该调用此方法。
     * 
     * <p><b>执行逻辑：</b>
     * <ul>
     *   <li>如果 supplier 不为 null：调用 {@code supplier.get()} 执行数据采集函数</li>
     *   <li>如果 supplier 为 null：直接返回当前对象（说明数据已经设置好）</li>
     * </ul>
     * 
     * <p><b>调用时机：</b>
     * <pre>
     * ❌ 错误：在限流检查前调用
     * ThreadPoolAlarmNotifyDTO resolved = alarm.resolve();  // 浪费性能
     * if (AlarmRateLimiter.allowAlarm(...)) {
     *     send(resolved);
     * }
     * 
     * ✅ 正确：在限流检查后调用
     * if (AlarmRateLimiter.allowAlarm(...)) {
     *     ThreadPoolAlarmNotifyDTO resolved = alarm.resolve();  // 只有通过才采集
     *     send(resolved);
     * }
     * </pre>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * // 在 NotifierDispatcher 中的正确用法
     * @Override
     * public void sendAlarmMessage(ThreadPoolAlarmNotifyDTO alarm) {
     *     // 步骤1：限流检查
     *     boolean allow = AlarmRateLimiter.allowAlarm(
     *         alarm.getThreadPoolId(),
     *         alarm.getAlarmType(),
     *         alarm.getInterval()
     *     );
     *     
     *     if (allow) {
     *         // 步骤2：只有通过限流，才调用 resolve() 加载数据
     *         ThreadPoolAlarmNotifyDTO resolved = alarm.resolve();
     *         
     *         // 步骤3：发送告警
     *         service.sendAlarmMessage(resolved);
     *     }
     *     // 如果被限流，不调用 resolve()，节省性能
     * }
     * }</pre>
     *
     * @return 填充完整数据的告警 DTO 对象
     */
    public ThreadPoolAlarmNotifyDTO resolve() {
        return supplier != null ? supplier.get() : this;
    }
}
