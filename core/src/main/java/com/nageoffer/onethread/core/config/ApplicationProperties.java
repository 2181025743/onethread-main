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

package com.nageoffer.onethread.core.config;

import lombok.Getter;
import lombok.Setter;

/**
 * 应用属性配置类
 * <p>
 * 该类使用静态字段保存应用的全局配置信息，如应用名称和运行环境标识。
 * 这些信息在应用启动时从 Spring 环境中读取并设置，供整个应用使用。
 * 
 * <p><b>核心作用：</b>
 * <ul>
 *   <li><b>全局配置：</b>提供应用级别的全局配置访问</li>
 *   <li><b>日志标识：</b>在日志中标识应用和环境</li>
 *   <li><b>监控标签：</b>作为监控数据的标签（Tag）</li>
 *   <li><b>告警信息：</b>在告警消息中标识应用来源</li>
 * </ul>
 * 
 * <p><b>数据来源：</b>
 * <ul>
 *   <li><b>应用名称：</b>从 {@code spring.application.name} 配置项读取</li>
 *   <li><b>环境标识：</b>从 {@code spring.profiles.active} 配置项读取</li>
 * </ul>
 * 
 * <p><b>配置示例（application.yml）：</b>
 * <pre>
 * spring:
 *   application:
 *     name: order-service        # 应用名称
 *   profiles:
 *     active: production         # 运行环境
 * </pre>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li><b>监控上报：</b>Prometheus 指标的 application.name 标签</li>
 *   <li><b>告警消息：</b>钉钉告警消息中的应用标识</li>
 *   <li><b>日志记录：</b>日志中标识应用和环境</li>
 *   <li><b>配置变更：</b>通知消息中标识哪个应用的配置发生了变更</li>
 * </ul>
 * 
 * <p><b>使用示例：</b>
 * <pre>{@code
 * // 示例1：在监控器中使用
 * public void reportMetrics() {
 *     String appName = ApplicationProperties.getApplicationName();
 *     String profile = ApplicationProperties.getActiveProfile();
 *     
 *     // 添加到监控标签
 *     Metrics.gauge("thread_pool_active_count", 
 *         Tags.of("application.name", appName, "profile", profile),
 *         executor,
 *         ThreadPoolExecutor::getActiveCount
 *     );
 * }
 * 
 * 
 * // 示例2：在告警消息中使用
 * public void sendAlarm() {
 *     String message = String.format(
 *         "[%s-%s] 线程池告警：队列使用率超过80%%",
 *         ApplicationProperties.getApplicationName(),    // order-service
 *         ApplicationProperties.getActiveProfile()       // production
 *     );
 *     dingTalkService.send(message);
 * }
 * 
 * 
 * // 示例3：在日志中使用
 * log.info("[{}][{}] 线程池参数已更新",
 *     ApplicationProperties.getApplicationName(),
 *     ApplicationProperties.getActiveProfile()
 * );
 * // 输出：[order-service][production] 线程池参数已更新
 * 
 * 
 * // 示例4：在应用启动时设置
 * @Component
 * public class ApplicationPropertiesInitializer implements ApplicationRunner {
 *     @Autowired
 *     private Environment environment;
 *     
 *     @Override
 *     public void run(ApplicationArguments args) {
 *         // 从 Spring 环境中读取并设置
 *         String appName = environment.getProperty("spring.application.name", "unknown");
 *         String profile = environment.getProperty("spring.profiles.active", "dev");
 *         
 *         ApplicationProperties.setApplicationName(appName);
 *         ApplicationProperties.setActiveProfile(profile);
 *         
 *         log.info("应用属性已初始化：name={}, profile={}", appName, profile);
 *     }
 * }
 * }</pre>
 * 
 * <p><b>设计模式：</b>
 * <ul>
 *   <li><b>全局访问点：</b>使用静态字段提供全局访问能力</li>
 *   <li><b>单一数据源：</b>避免在多个地方重复读取 Environment</li>
 * </ul>
 * 
 * <p><b>线程安全性：</b>
 * <ul>
 *   <li>静态字段在应用启动时设置一次，之后只读</li>
 *   <li>虽然使用了静态字段，但由于只在启动时设置，实际是线程安全的</li>
 *   <li>如果需要运行时修改，需要考虑并发安全（当前不支持）</li>
 * </ul>
 * 
 * <p><b>注意事项：</b>
 * <ul>
 *   <li>应该在应用启动早期设置这些值（如 ApplicationRunner）</li>
 *   <li>如果未设置，getter 方法会返回 null</li>
 *   <li>建议提供默认值（如 "unknown"、"dev"）避免 null</li>
 * </ul>
 * 
 * @author 杨潇
 * @since 2025-05-05
 * @see org.springframework.core.env.Environment Spring环境对象
 * @see org.springframework.boot.ApplicationRunner 应用启动回调
 */
public class ApplicationProperties {

    /**
     * 应用名称
     * <p>
     * 从 Spring Boot 配置项 {@code spring.application.name} 读取。
     * 
     * <p><b>用途：</b>
     * <ul>
     *   <li>监控数据的 application.name 标签</li>
     *   <li>告警消息中的应用标识</li>
     *   <li>日志中的应用名称</li>
     *   <li>配置中心的数据 ID 前缀</li>
     * </ul>
     * 
     * <p><b>配置示例：</b>
     * <pre>
     * spring:
     *   application:
     *     name: order-service
     * </pre>
     * 
     * <p><b>命名建议：</b>
     * <ul>
     *   <li>使用短横线分隔的小写字母，如 "order-service"</li>
     *   <li>体现业务含义，避免通用名称</li>
     *   <li>全局唯一，避免重复</li>
     * </ul>
     * 
     * <p><b>示例值：</b>"order-service"、"payment-service"、"user-service"
     */
    @Getter
    @Setter
    private static String applicationName;

    /**
     * 环境标识（运行环境）
     * <p>
     * 从 Spring Boot 配置项 {@code spring.profiles.active} 读取，
     * 标识应用当前运行的环境（开发、测试、生产等）。
     * 
     * <p><b>用途：</b>
     * <ul>
     *   <li>区分不同环境的监控数据</li>
     *   <li>告警消息中标识环境</li>
     *   <li>日志中区分环境</li>
     *   <li>根据环境执行不同的逻辑（如开发环境不发告警）</li>
     * </ul>
     * 
     * <p><b>配置示例：</b>
     * <pre>
     * spring:
     *   profiles:
     *     active: production  # 或 dev、test、staging
     * </pre>
     * 
     * <p><b>常见值：</b>
     * <ul>
     *   <li>"dev" - 开发环境</li>
     *   <li>"test" - 测试环境</li>
     *   <li>"staging" - 预发布环境</li>
     *   <li>"production" - 生产环境</li>
     * </ul>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * // 根据环境执行不同逻辑
     * String profile = ApplicationProperties.getActiveProfile();
     * if ("dev".equals(profile)) {
     *     // 开发环境：不发送告警，只记录日志
     *     log.warn("线程池告警（开发环境不发送）");
     * } else if ("production".equals(profile)) {
     *     // 生产环境：发送告警
     *     dingTalkService.sendAlarm(message);
     * }
     * }</pre>
     */
    @Getter
    @Setter
    private static String activeProfile;
}
