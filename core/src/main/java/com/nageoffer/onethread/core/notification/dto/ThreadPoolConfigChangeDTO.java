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

import java.util.Map;

/**
 * 动态线程池配置变更通知数据传输对象（DTO）
 * <p>
 * 该类封装了动态线程池配置变更通知所需的所有信息，包括变更的参数对比、
 * 应用信息、接收人等。用于在配置变更时发送通知到钉钉、邮件等平台。
 * 
 * <p><b>核心功能：</b>
 * <ul>
 *   <li><b>数据封装：</b>封装配置变更通知的所有数据</li>
 *   <li><b>变更对比：</b>通过 {@link ChangePair} 保存参数的旧值和新值</li>
 *   <li><b>平台无关：</b>与具体的通知平台解耦，由通知服务负责格式转换</li>
 * </ul>
 * 
 * <p><b>数据结构：</b>
 * <pre>
 * ThreadPoolConfigChangeDTO {
 *   threadPoolId: "order-processor",
 *   applicationName: "order-service",
 *   activeProfile: "production",
 *   identify: "192.168.1.100",
 *   receives: "13800138000,13900139000",
 *   workQueue: "LinkedBlockingQueue",
 *   changes: {
 *     "corePoolSize": ChangePair(5, 10),        // 5 → 10
 *     "maximumPoolSize": ChangePair(10, 20),    // 10 → 20
 *     "queueCapacity": ChangePair(100, 500),    // 100 → 500
 *     "keepAliveTime": ChangePair(60, 120),     // 60 → 120
 *     "rejectedHandler": ChangePair("AbortPolicy", "CallerRunsPolicy")
 *   },
 *   updateTime: "2025-04-30 15:30:45"
 * }
 * </pre>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>配置监听器检测到配置变更后，构建该 DTO 并发送通知</li>
 *   <li>前端控制台修改配置后，后端构建该 DTO 并发送通知</li>
 *   <li>记录配置变更历史</li>
 * </ul>
 * 
 * <p><b>构建示例：</b>
 * <pre>{@code
 * // 准备变更对比数据
 * Map<String, ChangePair<?>> changes = new HashMap<>();
 * changes.put("corePoolSize", new ChangePair<>(5, 10));
 * changes.put("maximumPoolSize", new ChangePair<>(10, 20));
 * changes.put("queueCapacity", new ChangePair<>(100, 500));
 * changes.put("keepAliveTime", new ChangePair<>(60L, 120L));
 * changes.put("rejectedHandler", new ChangePair<>("AbortPolicy", "CallerRunsPolicy"));
 * 
 * // 使用 Builder 构建 DTO
 * ThreadPoolConfigChangeDTO changeDTO = ThreadPoolConfigChangeDTO.builder()
 *     .threadPoolId("order-processor")
 *     .applicationName("order-service")
 *     .activeProfile("production")
 *     .identify("192.168.1.100")
 *     .receives("13800138000,13900139000")
 *     .workQueue("LinkedBlockingQueue")
 *     .changes(changes)
 *     .updateTime("2025-04-30 15:30:45")
 *     .build();
 * 
 * // 发送通知
 * notifierDispatcher.sendChangeMessage(changeDTO);
 * }</pre>
 * 
 * <p><b>生成的通知消息：</b>
 * <pre>
 * [通知] PRODUCTION - 动态线程池参数变更
 * ---
 * 线程池ID：order-processor
 * 应用实例：192.168.1.100:order-service
 * 核心线程数：5 ➲ 10
 * 最大线程数：10 ➲ 20
 * 线程存活时间：60 ➲ 120
 * 队列类型：LinkedBlockingQueue
 * 队列容量：100 ➲ 500
 * 旧拒绝策略：AbortPolicy
 * 新拒绝策略：CallerRunsPolicy
 * OWNER：@13800138000,@13900139000
 * ---
 * 变更时间：2025-04-30 15:30:45
 * </pre>
 * 
 * @author 杨潇
 * @since 2025-04-30
 * @see NotifierService 通知服务接口
 * @see DingTalkMessageService 钉钉通知服务
 * @see NotifierDispatcher 通知分发器
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThreadPoolConfigChangeDTO {

    /**
     * 线程池唯一标识
     * <p>
     * 标识哪个线程池的配置发生了变更。
     * 
     * <p><b>示例：</b>"order-processor"、"message-consumer"
     */
    private String threadPoolId;

    /**
     * 环境标识
     * <p>
     * 标识配置变更发生在哪个运行环境。
     * 
     * <p><b>来源：</b>{@link ApplicationProperties#getActiveProfile()}
     * 
     * <p><b>示例：</b>"dev"、"test"、"production"
     * 
     * <p><b>用途：</b>在通知消息中标识环境，便于区分
     */
    private String activeProfile;

    /**
     * 应用名称
     * <p>
     * 标识配置变更来源于哪个应用。
     * 
     * <p><b>来源：</b>{@link ApplicationProperties#getApplicationName()}
     * 
     * <p><b>示例：</b>"order-service"、"payment-service"
     */
    private String applicationName;

    /**
     * 应用实例唯一标识（IP地址）
     * <p>
     * 标识配置变更来源于哪个具体的应用实例。
     * 
     * <p><b>来源：</b>{@link java.net.InetAddress#getLocalHost()}.getHostAddress()
     * 
     * <p><b>示例：</b>"192.168.1.100"、"10.0.0.50"
     * 
     * <p><b>用途：</b>区分同一应用的不同实例
     */
    private String identify;

    /**
     * 通知接收人
     * <p>
     * 配置变更通知的接收人列表，多个接收人用逗号分隔。
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
     * 阻塞队列类型
     * <p>
     * 线程池使用的队列类型名称。
     * 
     * <p><b>示例：</b>"LinkedBlockingQueue"、"ArrayBlockingQueue"
     * 
     * <p><b>用途：</b>在通知消息中展示队列类型
     */
    private String workQueue;

    /**
     * 配置项变更集合
     * <p>
     * 存储所有发生变更的配置项及其变更前后的值。
     * 
     * <p><b>数据结构：</b>
     * <ul>
     *   <li><b>键（Key）：</b>配置项名称（如 "corePoolSize"、"maximumPoolSize"）</li>
     *   <li><b>值（Value）：</b>{@link ChangePair} 对象，包含变更前后的值</li>
     * </ul>
     * 
     * <p><b>示例数据：</b>
     * <pre>{@code
     * {
     *   "corePoolSize": ChangePair(5, 10),           // 核心线程数：5 → 10
     *   "maximumPoolSize": ChangePair(10, 20),       // 最大线程数：10 → 20
     *   "queueCapacity": ChangePair(100, 500),       // 队列容量：100 → 500
     *   "keepAliveTime": ChangePair(60L, 120L),      // 存活时间：60 → 120
     *   "rejectedHandler": ChangePair("AbortPolicy", "CallerRunsPolicy")
     * }
     * }</pre>
     * 
     * <p><b>常见的配置项键：</b>
     * <ul>
     *   <li>"corePoolSize" - 核心线程数</li>
     *   <li>"maximumPoolSize" - 最大线程数</li>
     *   <li>"queueCapacity" - 队列容量</li>
     *   <li>"keepAliveTime" - 线程存活时间</li>
     *   <li>"rejectedHandler" - 拒绝策略</li>
     * </ul>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * // 获取核心线程数的变更
     * ChangePair<?> corePoolSizeChange = changes.get("corePoolSize");
     * Object oldValue = corePoolSizeChange.getBefore();  // 5
     * Object newValue = corePoolSizeChange.getAfter();   // 10
     * 
     * // 格式化为字符串
     * String changeText = oldValue + " → " + newValue;   // "5 → 10"
     * }</pre>
     */
    private Map<String, ChangePair<?>> changes;

    /**
     * 配置变更时间
     * <p>
     * 配置实际生效的时间，格式如 "2025-04-30 15:30:45"。
     * 
     * <p><b>来源：</b>{@link cn.hutool.core.date.DateUtil#now()}
     * 
     * <p><b>示例：</b>"2025-04-30 15:30:45"
     */
    private String updateTime;

    /**
     * 配置项变更前后值对（内嵌类）
     * <p>
     * 该类封装了某个配置项变更前后的值，用于对比和展示。
     * 
     * <p><b>泛型说明：</b>
     * <ul>
     *   <li>{@code ChangePair<Integer>} - 整数类型的变更（如核心线程数）</li>
     *   <li>{@code ChangePair<Long>} - 长整数类型的变更（如存活时间）</li>
     *   <li>{@code ChangePair<String>} - 字符串类型的变更（如拒绝策略）</li>
     * </ul>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * // 核心线程数变更：5 → 10
     * ChangePair<Integer> corePoolChange = new ChangePair<>(5, 10);
     * 
     * // 拒绝策略变更：AbortPolicy → CallerRunsPolicy
     * ChangePair<String> rejectedChange = new ChangePair<>(
     *     "AbortPolicy", 
     *     "CallerRunsPolicy"
     * );
     * 
     * // 格式化显示
     * String text = corePoolChange.getBefore() + " → " + corePoolChange.getAfter();
     * // 输出：5 → 10
     * }</pre>
     * 
     * @param <T> 配置值的类型（Integer、Long、String等）
     */
    @Data
    @AllArgsConstructor
    public static class ChangePair<T> {
        
        /**
         * 变更前的旧值
         * <p>
         * 配置变更之前的参数值。
         * 
         * <p><b>示例：</b>
         * <ul>
         *   <li>核心线程数：5</li>
         *   <li>拒绝策略："AbortPolicy"</li>
         * </ul>
         */
        private T before;
        
        /**
         * 变更后的新值
         * <p>
         * 配置变更之后的参数值。
         * 
         * <p><b>示例：</b>
         * <ul>
         *   <li>核心线程数：10</li>
         *   <li>拒绝策略："CallerRunsPolicy"</li>
         * </ul>
         */
        private T after;
    }
}
