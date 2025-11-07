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

package com.nageoffer.onethread.core.notification.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 线程池告警速率限流器（告警防骚扰）
 * <p>
 * 该类负责控制告警通知的发送频率，防止同一个告警在短时间内重复发送，
 * 造成通知骚扰。通过时间窗口限流机制，确保同一线程池的同一类型告警
 * 在指定的时间间隔内只发送一次。
 * 
 * <p><b>核心功能：</b>
 * <ul>
 *   <li><b>告警限流：</b>基于时间窗口的限流机制</li>
 *   <li><b>防止骚扰：</b>避免频繁告警打扰接收人</li>
 *   <li><b>细粒度控制：</b>按线程池ID和告警类型分别限流</li>
 *   <li><b>线程安全：</b>使用 ConcurrentHashMap 和 compute 方法保证并发安全</li>
 * </ul>
 * 
 * <p><b>限流原理：</b>
 * <pre>
 * 告警记录表：
 * {
 *   "order-processor|Capacity": 1714471845000,  // 上次队列堆积告警时间
 *   "order-processor|Activity": 1714471850000,  // 上次线程满载告警时间
 *   "message-consumer|Reject": 1714471860000    // 上次拒绝告警时间
 * }
 * 
 * 限流判断逻辑：
 * 1. 构建告警键：threadPoolId + "|" + alarmType
 * 2. 获取上次告警时间：lastTime = ALARM_RECORD.get(key)
 * 3. 计算时间差：delta = currentTime - lastTime
 * 4. 判断是否允许：delta > interval × 60 × 1000
 * 5. 如果允许：更新告警时间为当前时间，返回 true
 * 6. 如果不允许：保持原告警时间，返回 false
 * </pre>
 * 
 * <p><b>限流示例：</b>
 * <pre>
 * 线程池：order-processor
 * 告警类型：Capacity（队列堆积）
 * 限流间隔：5分钟
 * 
 * 时间轴：
 * 15:00:00  队列使用率85%  → 允许告警 ✅（首次）
 * 15:01:00  队列使用率88%  → 拒绝告警 ❌（距离上次仅1分钟）
 * 15:03:00  队列使用率90%  → 拒绝告警 ❌（距离上次仅3分钟）
 * 15:05:01  队列使用率92%  → 允许告警 ✅（距离上次超过5分钟）
 * </pre>
 * 
 * <p><b>设计亮点：</b>
 * <ul>
 *   <li><b>原子操作：</b>使用 {@link ConcurrentHashMap#compute} 原子地检查和更新，避免竞态条件</li>
 *   <li><b>函数式编程：</b>使用 Lambda 表达式简化逻辑</li>
 *   <li><b>无锁设计：</b>利用 ConcurrentHashMap 的并发特性，无需额外同步</li>
 *   <li><b>巧妙判断：</b>通过返回值是否等于当前时间判断是否允许（避免额外变量）</li>
 * </ul>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>在 {@link NotifierDispatcher#sendAlarmMessage} 中调用，控制告警频率</li>
 *   <li>防止告警风暴（短时间内大量告警）</li>
 *   <li>提升用户体验（避免频繁打扰）</li>
 * </ul>
 * 
 * <p><b>使用示例：</b>
 * <pre>{@code
 * // 示例1：基本用法
 * boolean allow = AlarmRateLimiter.allowAlarm(
 *     "order-processor",  // 线程池ID
 *     "Capacity",         // 告警类型
 *     5                   // 间隔5分钟
 * );
 * 
 * if (allow) {
 *     // 允许发送告警
 *     dingTalkService.send(alarmMessage);
 * } else {
 *     // 限流，不发送
 *     log.debug("告警被限流，跳过发送");
 * }
 * 
 * 
 * // 示例2：在通知分发器中使用
 * @Override
 * public void sendAlarmMessage(ThreadPoolAlarmNotifyDTO alarm) {
 *     // 限流检查
 *     boolean allow = AlarmRateLimiter.allowAlarm(
 *         alarm.getThreadPoolId(),
 *         alarm.getAlarmType(),
 *         alarm.getInterval()
 *     );
 *     
 *     if (allow) {
 *         // 只有通过限流检查，才延迟加载数据并发送
 *         ThreadPoolAlarmNotifyDTO resolved = alarm.resolve();
 *         dingTalkService.send(buildMessage(resolved));
 *     }
 * }
 * 
 * 
 * // 示例3：多个告警类型独立限流
 * // 同一线程池的不同告警类型，独立计算限流
 * AlarmRateLimiter.allowAlarm("order-processor", "Capacity", 5);   // 队列堆积
 * AlarmRateLimiter.allowAlarm("order-processor", "Activity", 5);   // 线程满载
 * AlarmRateLimiter.allowAlarm("order-processor", "Reject", 5);     // 任务拒绝
 * // 这三个告警是独立限流的，互不影响
 * }</pre>
 * 
 * <p><b>性能特点：</b>
 * <ul>
 *   <li>时间复杂度：O(1)（HashMap 查找和更新）</li>
 *   <li>空间复杂度：O(n)，n 为线程池数量 × 告警类型数量</li>
 *   <li>并发性能：ConcurrentHashMap 的并发读写性能优异</li>
 *   <li>内存占用：每条记录约 50 字节（键 + Long值 + Map开销）</li>
 * </ul>
 * 
 * <p><b>线程安全性：</b>
 * <ul>
 *   <li>使用 {@link ConcurrentHashMap} 保证并发安全</li>
 *   <li>使用 {@link ConcurrentHashMap#compute} 原子操作，避免竞态条件</li>
 *   <li>支持多线程同时调用 {@link #allowAlarm}</li>
 * </ul>
 * 
 * <p><b>注意事项：</b>
 * <ul>
 *   <li>告警记录会一直保存在内存中，不会自动清理</li>
 *   <li>如果线程池数量很多，需要注意内存占用</li>
 *   <li>应用重启后，限流状态会丢失（所有告警都会重新发送一次）</li>
 *   <li>系统时间回拨可能导致限流失效（极少见）</li>
 * </ul>
 * 
 * @author 杨潇
 * @since 2025-05-05
 * @see NotifierDispatcher 通知分发器
 * @see ThreadPoolAlarmNotifyDTO 告警通知DTO
 */
public class AlarmRateLimiter {

    /**
     * 告警记录缓存
     * <p>
     * 用于记录每个告警的上次发送时间，实现限流功能。
     * 
     * <p><b>数据结构：</b>
     * <ul>
     *   <li><b>键（Key）：</b>threadPoolId + "|" + alarmType
     *       <br>示例："order-processor|Capacity"、"message-consumer|Activity"</li>
     *   <li><b>值（Value）：</b>上次告警发送的时间戳（毫秒）
     *       <br>示例：1714471845000（2025-04-30 15:30:45）</li>
     * </ul>
     * 
     * <p><b>为什么使用 ConcurrentHashMap？</b>
     * <ul>
     *   <li>支持并发读写，多个告警检查线程可以同时访问</li>
     *   <li>compute 方法提供原子性操作，避免竞态条件</li>
     *   <li>无需额外同步，性能优异</li>
     * </ul>
     * 
     * <p><b>键格式说明：</b>
     * <pre>
     * {threadPoolId}|{alarmType}
     * 
     * 示例：
     * - "order-processor|Capacity"   → 订单处理线程池的队列堆积告警
     * - "order-processor|Activity"   → 订单处理线程池的线程满载告警
     * - "order-processor|Reject"     → 订单处理线程池的任务拒绝告警
     * - "message-consumer|Capacity"  → 消息消费线程池的队列堆积告警
     * </pre>
     * 
     * <p><b>内存占用估算：</b>
     * <pre>
     * 假设：
     * - 线程池数量：50个
     * - 告警类型：3种（Capacity、Activity、Reject）
     * - 总记录数：50 × 3 = 150条
     * - 每条记录：约50字节（键字符串 + Long值 + Map开销）
     * - 总内存：150 × 50 = 7.5 KB（可以忽略）
     * </pre>
     */
    private static final Map<String, Long> ALARM_RECORD = new ConcurrentHashMap<>();

    /**
     * 检查是否允许发送告警（限流判断）
     * <p>
     * 该方法是限流器的核心，通过原子操作检查和更新告警记录，
     * 判断是否允许发送告警。
     * 
     * <p><b>判断逻辑：</b>
     * <ol>
     *   <li>构建告警键：threadPoolId + "|" + alarmType</li>
     *   <li>获取当前时间戳</li>
     *   <li>使用 {@link ConcurrentHashMap#compute} 原子地执行：
     *       <ul>
     *         <li>如果是首次告警（lastTime == null）：允许发送，记录当前时间</li>
     *         <li>如果距离上次告警超过间隔时间：允许发送，更新为当前时间</li>
     *         <li>如果在间隔时间内：拒绝发送，保持原时间</li>
     *       </ul>
     *   </li>
     *   <li>判断返回值是否等于当前时间：
     *       <ul>
     *         <li>相等 → 说明记录被更新了 → 允许发送</li>
     *         <li>不相等 → 说明记录未更新 → 拒绝发送</li>
     *       </ul>
     *   </li>
     * </ol>
     * 
     * <p><b>原子性保证：</b>
     * {@link ConcurrentHashMap#compute} 方法保证了"检查-更新"操作的原子性，
     * 即使多个线程同时调用，也不会出现竞态条件。
     * 
     * <p><b>限流效果：</b>
     * <pre>
     * 配置间隔：5分钟
     * 
     * 第1次调用（15:00:00）：
     *   上次时间：null（首次）
     *   当前时间：15:00:00
     *   判断：首次告警，允许 ✅
     *   更新：记录时间为 15:00:00
     * 
     * 第2次调用（15:02:00）：
     *   上次时间：15:00:00
     *   当前时间：15:02:00
     *   时间差：2分钟 < 5分钟
     *   判断：在间隔内，拒绝 ❌
     *   更新：保持 15:00:00 不变
     * 
     * 第3次调用（15:05:01）：
     *   上次时间：15:00:00
     *   当前时间：15:05:01
     *   时间差：5分1秒 > 5分钟
     *   判断：超过间隔，允许 ✅
     *   更新：记录时间为 15:05:01
     * </pre>
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>队列使用率持续超过80%，每5分钟告警一次（而非每5秒一次）</li>
     *   <li>活跃线程率持续在高位，避免告警轰炸</li>
     *   <li>拒绝次数持续增加，控制告警频率</li>
     * </ul>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * // 示例1：在告警检查器中使用
     * public void checkQueueUsage() {
     *     if (queueUsage > 80) {
     *         // 限流检查
     *         boolean allow = AlarmRateLimiter.allowAlarm(
     *             "order-processor",  // 线程池ID
     *             "Capacity",         // 告警类型：队列堆积
     *             5                   // 间隔5分钟
     *         );
     *         
     *         if (allow) {
     *             sendAlarmMessage();  // 发送告警
     *             log.info("队列堆积告警已发送");
     *         } else {
     *             log.debug("队列堆积告警被限流，跳过发送");
     *         }
     *     }
     * }
     * 
     * 
     * // 示例2：不同告警类型独立限流
     * // 队列堆积告警
     * boolean allow1 = AlarmRateLimiter.allowAlarm("pool1", "Capacity", 5);
     * 
     * // 线程满载告警（独立限流，不受队列堆积告警影响）
     * boolean allow2 = AlarmRateLimiter.allowAlarm("pool1", "Activity", 5);
     * 
     * // 同一线程池可以同时发送不同类型的告警
     * 
     * 
     * // 示例3：不同间隔时间
     * // 队列堆积：5分钟告警一次（不太紧急）
     * AlarmRateLimiter.allowAlarm("pool1", "Capacity", 5);
     * 
     * // 任务拒绝：1分钟告警一次（非常紧急）
     * AlarmRateLimiter.allowAlarm("pool1", "Reject", 1);
     * 
     * 
     * // 示例4：在通知分发器中使用
     * @Override
     * public void sendAlarmMessage(ThreadPoolAlarmNotifyDTO alarm) {
     *     // 限流检查
     *     boolean allow = AlarmRateLimiter.allowAlarm(
     *         alarm.getThreadPoolId(),
     *         alarm.getAlarmType(),
     *         alarm.getInterval()
     *     );
     *     
     *     if (allow) {
     *         // 通过限流，延迟加载数据并发送
     *         ThreadPoolAlarmNotifyDTO resolved = alarm.resolve();
     *         notifierService.send(resolved);
     *     } else {
     *         log.debug("告警被限流：threadPoolId={}, alarmType={}", 
     *             alarm.getThreadPoolId(), alarm.getAlarmType());
     *     }
     * }
     * }</pre>
     * 
     * <p><b>compute 方法解析：</b>
     * <pre>{@code
     * ALARM_RECORD.compute(key, (k, lastTime) -> {
     *     // 参数：
     *     // k - 告警键（threadPoolId|alarmType）
     *     // lastTime - 上次告警时间（可能为null）
     *     
     *     if (lastTime == null || (currentTime - lastTime) > intervalMinutes * 60 * 1000L) {
     *         // 条件1：首次告警（lastTime == null）
     *         // 条件2：超过间隔时间
     *         // 操作：返回当前时间（更新记录）
     *         return currentTime;
     *     }
     *     
     *     // 在间隔时间内
     *     // 操作：返回原时间（不更新）
     *     return lastTime;
     * }) == currentTime;  // 如果返回值等于当前时间，说明允许告警
     * }</pre>
     * 
     * <p><b>线程安全性证明：</b>
     * <pre>
     * 假设两个线程同时检查同一个告警：
     * 
     * 线程A：AlarmRateLimiter.allowAlarm("pool1", "Capacity", 5)
     * 线程B：AlarmRateLimiter.allowAlarm("pool1", "Capacity", 5)
     * 
     * ConcurrentHashMap.compute 保证：
     * 1. 只有一个线程能够执行 compute 的 Lambda
     * 2. 另一个线程会等待
     * 3. 假设线程A先执行，更新时间为 T1
     * 4. 线程B后执行，发现距离 T1 很短（几毫秒），拒绝告警
     * 5. 结果：只有一个线程的告警会被发送，符合预期
     * </pre>
     * 
     * <p><b>性能优化建议：</b>
     * <ul>
     *   <li>如果告警记录过多（>10000条），考虑定期清理过期记录</li>
     *   <li>如果需要持久化限流状态，可以使用 Redis 替代内存 Map</li>
     *   <li>如果需要分布式限流，使用 Redis + Lua 脚本实现</li>
     * </ul>
     *
     * @param threadPoolId    线程池唯一标识（如 "order-processor"）
     * @param alarmType       告警类型（如 "Capacity"、"Activity"、"Reject"）
     * @param intervalMinutes 限流间隔时间（单位：分钟），如 5 表示5分钟内只告警一次
     * @return {@code true} 表示允许发送告警，{@code false} 表示需要限流（抑制告警）
     */
    public static boolean allowAlarm(String threadPoolId, String alarmType, int intervalMinutes) {
        // 1. 构建告警键（threadPoolId|alarmType）
        String key = buildKey(threadPoolId, alarmType);
        
        // 2. 获取当前时间戳（毫秒）
        long currentTime = System.currentTimeMillis();

        // 3. 原子地检查和更新告警记录
        // compute 方法保证了"检查-更新"操作的原子性
        return ALARM_RECORD.compute(key, (k, lastTime) -> {
            // 判断是否允许告警
            if (lastTime == null || (currentTime - lastTime) > intervalMinutes * 60 * 1000L) {
                // 情况1：首次告警（lastTime == null）
                // 情况2：距离上次告警超过间隔时间
                // → 允许告警，返回当前时间（更新记录）
                return currentTime;
            }
            
            // 在间隔时间内
            // → 拒绝告警，返回原时间（不更新记录）
            return lastTime;
        }) == currentTime;  // 巧妙判断：如果返回值等于当前时间，说明记录被更新了，允许告警
    }

    /**
     * 构建告警记录的键
     * <p>
     * 将线程池 ID 和告警类型组合为唯一的键，用于在限流记录中查找。
     * 
     * <p><b>键格式：</b>
     * <pre>
     * {threadPoolId}|{alarmType}
     * </pre>
     * 
     * <p><b>示例：</b>
     * <pre>
     * buildKey("order-processor", "Capacity")   → "order-processor|Capacity"
     * buildKey("message-consumer", "Activity")  → "message-consumer|Activity"
     * buildKey("async-task-pool", "Reject")     → "async-task-pool|Reject"
     * </pre>
     * 
     * <p><b>为什么使用 "|" 分隔符？</b>
     * <ul>
     *   <li>"|" 不太可能出现在 threadPoolId 中（通常使用短横线）</li>
     *   <li>便于调试时阅读和理解</li>
     *   <li>便于日志记录和问题排查</li>
     * </ul>
     *
     * @param threadPoolId 线程池唯一标识
     * @param alarmType    告警类型
     * @return 组合后的唯一键
     */
    private static String buildKey(String threadPoolId, String alarmType) {
        return threadPoolId + "|" + alarmType;
    }
}
