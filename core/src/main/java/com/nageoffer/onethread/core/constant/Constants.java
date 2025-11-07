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

package com.nageoffer.onethread.core.constant;

/**
 * oneThread 框架常量类
 * <p>
 * 该类集中定义了 oneThread 框架中使用的所有常量，包括日志格式、通知消息模板等。
 * 使用常量可以提高代码的可维护性、避免硬编码、统一格式标准。
 * 
 * <p><b>常量分类：</b>
 * <ul>
 *   <li><b>日志常量：</b>用于线程池参数变更的日志输出格式</li>
 *   <li><b>钉钉消息模板：</b>用于钉钉机器人通知的消息格式</li>
 *   <li><b>格式化常量：</b>用于参数变更前后对比的分隔符</li>
 * </ul>
 * 
 * <p><b>设计原则：</b>
 * <ul>
 *   <li>所有常量使用 {@code public static final} 修饰</li>
 *   <li>常量名使用全大写，单词间用下划线分隔</li>
 *   <li>字符串常量使用文本块（Text Block）提高可读性</li>
 *   <li>消息模板支持参数占位符（{@code %s}、{@code %d}），使用 {@link String#format} 填充</li>
 * </ul>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li><b>配置变更日志：</b>记录线程池参数变更的详细信息</li>
 *   <li><b>钉钉通知：</b>发送配置变更和告警通知到钉钉群</li>
 *   <li><b>监控告警：</b>格式化告警消息</li>
 * </ul>
 * 
 * @author 杨潇
 * @since 2025-04-30
 * @see String#format(String, Object...) 字符串格式化方法
 */
public class Constants {

    /**
     * 线程池参数变更日志格式模板
     * <p>
     * 用于在控制台和日志文件中打印线程池参数变更信息。
     * 该格式提供了清晰的参数对比视图，方便运维人员和开发人员追踪线程池配置的变化。
     * 
     * <p><b>占位符说明：</b>
     * <ol>
     *   <li>{@code {}} - 线程池唯一标识（threadPoolId）</li>
     *   <li>{@code {}} - 核心线程数变更（旧值 => 新值）</li>
     *   <li>{@code {}} - 最大线程数变更（旧值 => 新值）</li>
     *   <li>{@code {}} - 队列容量变更（旧值 => 新值）</li>
     *   <li>{@code {}} - 线程存活时间变更（旧值 => 新值）</li>
     *   <li>{@code {}} - 拒绝策略变更（旧值 => 新值）</li>
     *   <li>{@code {}} - 核心线程超时设置变更（旧值 => 新值）</li>
     * </ol>
     * 
     * <p><b>输出示例：</b>
     * <pre>
     * [order-processor] Dynamic thread pool parameter changed:
     *     corePoolSize: 5 => 10
     *     maximumPoolSize: 10 => 20
     *     capacity: 100 => 500
     *     keepAliveTime: 60 => 120
     *     rejectedType: AbortPolicy => CallerRunsPolicy
     *     allowCoreThreadTimeOut: false => true
     * </pre>
     * 
     * <p><b>使用方式：</b>
     * <pre>{@code
     * log.info(CHANGE_THREAD_POOL_TEXT,
     *     threadPoolId,
     *     String.format(CHANGE_DELIMITER, oldCoreSize, newCoreSize),
     *     String.format(CHANGE_DELIMITER, oldMaxSize, newMaxSize),
     *     String.format(CHANGE_DELIMITER, oldCapacity, newCapacity),
     *     String.format(CHANGE_DELIMITER, oldKeepAlive, newKeepAlive),
     *     String.format(CHANGE_DELIMITER, oldRejected, newRejected),
     *     String.format(CHANGE_DELIMITER, oldAllowTimeout, newAllowTimeout)
     * );
     * }</pre>
     * 
     * @see #CHANGE_DELIMITER 参数变更格式化分隔符
     */
    public static final String CHANGE_THREAD_POOL_TEXT = """
            [{}] Dynamic thread pool parameter changed:\
            
                corePoolSize: {}\
            
                maximumPoolSize: {}\
            
                capacity: {}\
            
                keepAliveTime: {}\
            
                rejectedType: {}\
            
                allowCoreThreadTimeOut: {}""";

    /**
     * 线程池参数变更前后值的分隔符格式
     * <p>
     * 用于格式化参数变更的"旧值 => 新值"格式，提供直观的变更对比。
     * 
     * <p><b>占位符说明：</b>
     * <ul>
     *   <li>第一个 {@code %s} - 旧值（变更前的参数值）</li>
     *   <li>第二个 {@code %s} - 新值（变更后的参数值）</li>
     * </ul>
     * 
     * <p><b>格式化示例：</b>
     * <pre>{@code
     * String.format(CHANGE_DELIMITER, 5, 10)           // 输出：5 => 10
     * String.format(CHANGE_DELIMITER, "AbortPolicy", "CallerRunsPolicy")
     *                                                   // 输出：AbortPolicy => CallerRunsPolicy
     * String.format(CHANGE_DELIMITER, false, true)     // 输出：false => true
     * }</pre>
     * 
     * <p><b>设计理由：</b>
     * <ul>
     *   <li>使用 "=>" 箭头符号直观表示变更方向</li>
     *   <li>支持任意类型的参数（通过 %s 格式化为字符串）</li>
     *   <li>统一格式，便于日志解析和监控</li>
     * </ul>
     */
    public static final String CHANGE_DELIMITER = "%s => %s";

    /**
     * 钉钉机器人 - 动态线程池配置变更通知消息模板
     * <p>
     * 用于向钉钉群发送线程池配置变更通知，采用 Markdown 格式，支持富文本显示。
     * 该模板使用了钉钉机器人支持的 Markdown 语法，包括颜色、字体大小等。
     * 
     * <p><b>消息特点：</b>
     * <ul>
     *   <li>使用醒目的绿色标题（{@code #2a9d8f}），表示配置变更通知</li>
     *   <li>包含详细的变更参数对比信息</li>
     *   <li>支持 @人 功能，可以 @ 指定的负责人</li>
     *   <li>使用分隔线增强可读性</li>
     *   <li>提示信息说明通知频率（无限制，实时通知）</li>
     * </ul>
     * 
     * <p><b>占位符说明（共13个）：</b>
     * <ol>
     *   <li>{@code %s} - 应用名称（如 "订单服务"）</li>
     *   <li>{@code %s} - 线程池ID（如 "order-processor"）</li>
     *   <li>{@code %s} - 应用实例地址（如 "192.168.1.100:8080"）</li>
     *   <li>{@code %s} - 核心线程数变更（如 "5 => 10"）</li>
     *   <li>{@code %s} - 最大线程数变更（如 "10 => 20"）</li>
     *   <li>{@code %s} - 线程存活时间变更（如 "60 => 120"）</li>
     *   <li>{@code %s} - 队列类型（如 "LinkedBlockingQueue"）</li>
     *   <li>{@code %s} - 队列容量变更（如 "100 => 500"）</li>
     *   <li>{@code %s} - 旧拒绝策略（如 "AbortPolicy"）</li>
     *   <li>{@code %s} - 新拒绝策略（如 "CallerRunsPolicy"）</li>
     *   <li>{@code %s} - 负责人（如 "张三"，用于 @ 通知）</li>
     *   <li>{@code %s} - 变更时间（如 "2025-04-30 15:30:45"）</li>
     * </ol>
     * 
     * <p><b>钉钉消息效果：</b>
     * <pre>
     * ┌─────────────────────────────────────┐
     * │ [通知] 订单服务 - 动态线程池参数变更  │
     * ├─────────────────────────────────────┤
     * │ 线程池ID：order-processor            │
     * │ 应用实例：192.168.1.100:8080        │
     * │ 核心线程数：5 => 10                  │
     * │ 最大线程数：10 => 20                 │
     * │ 线程存活时间：60 => 120              │
     * │ 队列类型：LinkedBlockingQueue       │
     * │ 队列容量：100 => 500                 │
     * │ 旧拒绝策略：AbortPolicy              │
     * │ 新拒绝策略：CallerRunsPolicy         │
     * │ OWNER：@张三                        │
     * │ 提示：动态线程池配置变更实时通知      │
     * ├─────────────────────────────────────┤
     * │ 变更时间：2025-04-30 15:30:45       │
     * └─────────────────────────────────────┘
     * </pre>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * String message = String.format(
     *     DING_CONFIG_CHANGE_MESSAGE_TEXT,
     *     "订单服务",                        // 应用名称
     *     "order-processor",                // 线程池ID
     *     "192.168.1.100:8080",            // 应用实例
     *     "5 => 10",                        // 核心线程数
     *     "10 => 20",                       // 最大线程数
     *     "60 => 120",                      // 存活时间
     *     "LinkedBlockingQueue",            // 队列类型
     *     "100 => 500",                     // 队列容量
     *     "AbortPolicy",                    // 旧拒绝策略
     *     "CallerRunsPolicy",               // 新拒绝策略
     *     "张三",                           // 负责人
     *     DateUtil.now()                    // 变更时间
     * );
     * dingTalkService.send(message);
     * }</pre>
     * 
     * <p><b>注意事项：</b>
     * <ul>
     *   <li>钉钉 Markdown 支持的颜色有限，建议使用常见颜色值</li>
     *   <li>@ 功能需要钉钉群成员的手机号或用户ID</li>
     *   <li>消息长度不应超过钉钉限制（20KB）</li>
     *   <li>使用文本块（Text Block）语法，保持格式清晰</li>
     * </ul>
     */
    public static final String DING_CONFIG_CHANGE_MESSAGE_TEXT = """
            **<font color=#2a9d8f>[通知] </font>%s - 动态线程池参数变更**
            
             ---
            
            <font color='#708090' size=2>线程池ID：%s</font>
            
            <font color='#708090' size=2>应用实例：%s</font>
            
            <font color='#708090' size=2>核心线程数：%s</font>
            
            <font color='#708090' size=2>最大线程数：%s</font>
            
            <font color='#708090' size=2>线程存活时间：%s</font>
            
            <font color='#708090' size=2>队列类型：%s</font>
            
            <font color='#708090' size=2>队列容量：%s</font>
            
            <font color='#708090' size=2>旧拒绝策略：%s</font>
            
            <font color='#708090' size=2>新拒绝策略：%s</font>
            
            <font color='#708090' size=2>OWNER：@%s</font>
            
            <font color='#708090' size=2>提示：动态线程池配置变更实时通知（无限制）</font>
            
             ---
            
            **变更时间：%s**
            """;

    /**
     * 钉钉机器人 - Web容器线程池配置变更通知消息模板
     * <p>
     * 用于向钉钉群发送 Web 容器线程池（Tomcat、Jetty等）的配置变更通知。
     * 与动态线程池相比，Web容器线程池的参数相对较少，因此模板更简洁。
     * 
     * <p><b>适用场景：</b>
     * <ul>
     *   <li>Tomcat 线程池参数调整（如连接器线程池）</li>
     *   <li>Jetty 线程池参数调整</li>
     *   <li>Undertow 线程池参数调整</li>
     *   <li>其他嵌入式 Web 容器的线程池配置变更</li>
     * </ul>
     * 
     * <p><b>占位符说明（共8个）：</b>
     * <ol>
     *   <li>{@code %s} - 应用名称（如 "订单服务"）</li>
     *   <li>{@code %s} - 容器类型（如 "Tomcat"、"Jetty"）</li>
     *   <li>{@code %s} - 应用实例地址（如 "192.168.1.100:8080"）</li>
     *   <li>{@code %s} - 核心线程数变更（如 "10 => 20"）</li>
     *   <li>{@code %s} - 最大线程数变更（如 "200 => 500"）</li>
     *   <li>{@code %s} - 线程存活时间变更（如 "60 => 120"）</li>
     *   <li>{@code %s} - 负责人（用于 @ 通知）</li>
     *   <li>{@code %s} - 容器类型（重复，用于提示信息）</li>
     *   <li>{@code %s} - 变更时间</li>
     * </ol>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * String message = String.format(
     *     DING_CONFIG_WEB_CHANGE_MESSAGE_TEXT,
     *     "订单服务",                        // 应用名称
     *     "Tomcat",                         // 容器类型
     *     "192.168.1.100:8080",            // 应用实例
     *     "10 => 20",                       // 核心线程数
     *     "200 => 500",                     // 最大线程数
     *     "60 => 120",                      // 存活时间
     *     "张三",                           // 负责人
     *     "Tomcat",                         // 容器类型（提示）
     *     DateUtil.now()                    // 变更时间
     * );
     * dingTalkService.send(message);
     * }</pre>
     */
    public static final String DING_CONFIG_WEB_CHANGE_MESSAGE_TEXT = """
            **<font color=#2a9d8f>[通知] </font>%s - %s线程池参数变更**
            
             ---
            
            <font color='#708090' size=2>应用实例：%s</font>
            
            <font color='#708090' size=2>核心线程数：%s</font>
            
            <font color='#708090' size=2>最大线程数：%s</font>
            
            <font color='#708090' size=2>线程存活时间：%s</font>
            
            <font color='#708090' size=2>OWNER：@%s</font>
            
            <font color='#708090' size=2>提示：%s线程池配置变更实时通知（无限制）</font>
            
             ---
            
            **变更时间：%s**
            """;

    /**
     * 钉钉机器人 - 动态线程池运行告警消息模板
     * <p>
     * 用于向钉钉群发送线程池运行状态告警通知，包括队列堆积、线程池满载等异常情况。
     * 该模板包含了最详细的线程池运行时信息，帮助快速定位和解决问题。
     * 
     * <p><b>告警触发条件：</b>
     * <ul>
     *   <li><b>队列堆积告警：</b>队列使用率超过阈值（如 80%）</li>
     *   <li><b>线程池满载告警：</b>活跃线程率超过阈值（如 80%）</li>
     *   <li><b>拒绝策略触发告警：</b>拒绝策略执行次数超过阈值</li>
     *   <li><b>自定义告警：</b>根据业务规则触发的告警</li>
     * </ul>
     * 
     * <p><b>消息特点：</b>
     * <ul>
     *   <li>使用醒目的红色标题（{@code #FF0000}），表示告警</li>
     *   <li>包含完整的线程池运行时状态</li>
     *   <li>分组展示线程信息、队列信息、拒绝策略信息</li>
     *   <li>高亮显示关键指标（如拒绝次数使用红色）</li>
     *   <li>包含告警限流提示（避免频繁告警骚扰）</li>
     * </ul>
     * 
     * <p><b>占位符说明（共19个）：</b>
     * <ol>
     *   <li>{@code %s} - 应用名称</li>
     *   <li>{@code %s} - 线程池ID</li>
     *   <li>{@code %s} - 应用实例地址</li>
     *   <li>{@code %s} - 告警类型（如 "队列堆积告警"、"线程池满载告警"）</li>
     *   <li>{@code %d} - 核心线程数</li>
     *   <li>{@code %d} - 最大线程数</li>
     *   <li>{@code %d} - 当前线程数</li>
     *   <li>{@code %d} - 活跃线程数</li>
     *   <li>{@code %d} - 历史最大线程数</li>
     *   <li>{@code %d} - 线程池任务总量（已完成 + 队列中 + 执行中）</li>
     *   <li>{@code %s} - 队列类型</li>
     *   <li>{@code %d} - 队列容量</li>
     *   <li>{@code %d} - 队列当前元素个数</li>
     *   <li>{@code %d} - 队列剩余容量</li>
     *   <li>{@code %s} - 拒绝策略类型</li>
     *   <li>{@code %d} - 拒绝策略执行次数（红色高亮）</li>
     *   <li>{@code %s} - 负责人</li>
     *   <li>{@code %d} - 告警间隔时间（分钟）</li>
     *   <li>{@code %s} - 告警时间</li>
     * </ol>
     * 
     * <p><b>钉钉消息效果：</b>
     * <pre>
     * ┌──────────────────────────────────────┐
     * │ [警报] 订单服务 - 动态线程池运行告警   │
     * ├──────────────────────────────────────┤
     * │ 线程池ID：order-processor             │
     * │ 应用实例：192.168.1.100:8080         │
     * │ 告警类型：队列堆积告警                 │
     * ├──────────────────────────────────────┤
     * │ 核心线程数：10                        │
     * │ 最大线程数：20                        │
     * │ 当前线程数：20                        │
     * │ 活跃线程数：20                        │
     * │ 历史最大线程数：20                    │
     * │ 线程池任务总量：5000                  │
     * ├──────────────────────────────────────┤
     * │ 队列类型：LinkedBlockingQueue        │
     * │ 队列容量：500                         │
     * │ 队列元素个数：450                     │
     * │ 队列剩余个数：50                      │
     * ├──────────────────────────────────────┤
     * │ 拒绝策略：AbortPolicy                 │
     * │ 拒绝策略执行次数：10                  │ ← 红色
     * │ OWNER：@张三                         │
     * │ 提示：5分钟内此线程池不会重复告警      │
     * ├──────────────────────────────────────┤
     * │ 告警时间：2025-04-30 15:30:45        │
     * └──────────────────────────────────────┘
     * </pre>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * ThreadPoolExecutor executor = ...;
     * 
     * String message = String.format(
     *     DING_ALARM_NOTIFY_MESSAGE_TEXT,
     *     "订单服务",                                  // 应用名称
     *     "order-processor",                          // 线程池ID
     *     "192.168.1.100:8080",                      // 实例地址
     *     "队列堆积告警",                             // 告警类型
     *     executor.getCorePoolSize(),                 // 核心线程数
     *     executor.getMaximumPoolSize(),              // 最大线程数
     *     executor.getPoolSize(),                     // 当前线程数
     *     executor.getActiveCount(),                  // 活跃线程数
     *     executor.getLargestPoolSize(),              // 历史最大线程数
     *     executor.getTaskCount(),                    // 任务总量
     *     "LinkedBlockingQueue",                      // 队列类型
     *     500,                                        // 队列容量
     *     executor.getQueue().size(),                 // 队列元素数
     *     executor.getQueue().remainingCapacity(),    // 队列剩余
     *     "AbortPolicy",                              // 拒绝策略
     *     10,                                         // 拒绝次数
     *     "张三",                                     // 负责人
     *     5,                                          // 告警间隔
     *     DateUtil.now()                              // 告警时间
     * );
     * dingTalkService.send(message);
     * }</pre>
     * 
     * <p><b>注意事项：</b>
     * <ul>
     *   <li>告警消息应配合限流机制，避免频繁发送</li>
     *   <li>拒绝次数使用红色字体高亮，引起注意</li>
     *   <li>提示信息中说明告警间隔，让接收者了解告警频率</li>
     *   <li>包含详细的运行时数据，便于快速定位问题</li>
     * </ul>
     */
    public static final String DING_ALARM_NOTIFY_MESSAGE_TEXT = """
            **<font color=#FF0000>[警报] </font>%s - 动态线程池运行告警**
            
             ---
            
            <font color='#708090' size=2>线程池ID：%s</font>
            
            <font color='#708090' size=2>应用实例：%s</font>
            
            <font color='#708090' size=2>告警类型：%s</font>
            
             ---
            
            <font color='#708090' size=2>核心线程数：%d</font>
            
            <font color='#708090' size=2>最大线程数：%d</font>
            
            <font color='#708090' size=2>当前线程数：%d</font>
            
            <font color='#708090' size=2>活跃线程数：%d</font>
            
            <font color='#708090' size=2>同存最大线程数：%d</font>
            
            <font color='#708090' size=2>线程池任务总量：%d</font>
            
             ---
            
            <font color='#708090' size=2>队列类型：%s</font>
            
            <font color='#708090' size=2>队列容量：%d</font>
            
            <font color='#708090' size=2>队列元素个数：%d</font>
            
            <font color='#708090' size=2>队列剩余个数：%d</font>
            
             ---
            
            <font color='#708090' size=2>拒绝策略：%s</font>
            
            <font color='#708090' size=2>拒绝策略执行次数：</font><font color='#FF0000' size=2>%d</font>
            
            <font color='#708090' size=2>OWNER：@%s</font>
            
            <font color='#708090' size=2>提示：%d分钟内此线程池不会重复告警（可配置）</font>
            
             ---
            
            **告警时间：%s**
            """;
}
