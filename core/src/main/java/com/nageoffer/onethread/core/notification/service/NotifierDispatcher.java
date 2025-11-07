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

import com.nageoffer.onethread.core.config.BootstrapConfigProperties;
import com.nageoffer.onethread.core.notification.dto.ThreadPoolAlarmNotifyDTO;
import com.nageoffer.onethread.core.notification.dto.ThreadPoolConfigChangeDTO;
import com.nageoffer.onethread.core.notification.dto.WebThreadPoolConfigChangeDTO;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 通知分发器（调度器）
 * <p>
 * 该类是 oneThread 框架通知系统的核心组件，负责统一管理和路由各类通知发送器
 * （如钉钉、飞书、企业微信等）。它屏蔽了具体通知平台的实现细节，
 * 对上层调用者提供统一的通知发送入口。
 * 
 * <p><b>核心功能：</b>
 * <ul>
 *   <li><b>统一入口：</b>为所有通知操作提供统一的 API</li>
 *   <li><b>自动路由：</b>根据配置自动选择合适的通知平台</li>
 *   <li><b>告警限流：</b>集成告警限流机制，防止告警风暴</li>
 *   <li><b>延迟加载：</b>只有通过限流检查后才加载详细数据，提高性能</li>
 *   <li><b>易于扩展：</b>新增通知平台只需实现接口并注册</li>
 * </ul>
 * 
 * <p><b>设计模式：</b>
 * <ul>
 *   <li><b>外观模式（Facade Pattern）：</b>为复杂的通知系统提供简单的接口</li>
 *   <li><b>工厂模式（Factory Pattern）：</b>根据配置创建具体的通知服务</li>
 *   <li><b>策略模式（Strategy Pattern）：</b>不同的通知平台代表不同的策略</li>
 *   <li><b>代理模式（Proxy Pattern）：</b>在调用实际通知服务前进行限流检查</li>
 * </ul>
 * 
 * <p><b>架构设计：</b>
 * <pre>
 * 上层调用（告警检查器、配置监听器等）
 *    ↓ 调用
 * NotifierDispatcher（本类）
 *    ├─ 查询配置：获取通知平台类型
 *    ├─ 路由选择：根据平台类型选择实现
 *    ├─ 限流检查：告警消息进行限流
 *    └─ 转发调用：调用具体的通知服务
 *         ↓
 * 具体通知服务实现
 *    ├─ DingTalkMessageService（钉钉）
 *    ├─ EmailMessageService（邮件，扩展）
 *    └─ WeChatMessageService（企业微信，扩展）
 * </pre>
 * 
 * <p><b>通知平台注册：</b>
 * <pre>{@code
 * static {
 *     // 在静态初始化块中注册所有支持的通知平台
 *     NOTIFIER_SERVICE_MAP.put("DING", new DingTalkMessageService());
 *     
 *     // 扩展其他平台（示例）
 *     // NOTIFIER_SERVICE_MAP.put("EMAIL", new EmailMessageService());
 *     // NOTIFIER_SERVICE_MAP.put("WECHAT", new WeChatMessageService());
 *     // NOTIFIER_SERVICE_MAP.put("FEISHU", new FeiShuMessageService());
 * }
 * }</pre>
 * 
 * <p><b>配置驱动：</b>
 * <pre>
 * # 在配置文件中指定通知平台
 * onethread:
 *   notify-platforms:
 *     platform: DING  # 使用钉钉
 *     url: https://oapi.dingtalk.com/robot/send?access_token=xxx
 * 
 * # 分发器会自动根据 platform 配置选择对应的实现
 * </pre>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>配置监听器发送配置变更通知</li>
 *   <li>告警检查器发送运行时告警</li>
 *   <li>Web线程池适配器发送Web配置变更通知</li>
 * </ul>
 * 
 * <p><b>使用示例：</b>
 * <pre>{@code
 * // 示例1：发送配置变更通知
 * @Component
 * public class ConfigChangeNotifier {
 *     @Autowired
 *     private NotifierDispatcher notifierDispatcher;
 *     
 *     public void notifyConfigChange(ThreadPoolConfigChangeDTO changeDTO) {
 *         // 自动路由到配置的通知平台（如钉钉）
 *         notifierDispatcher.sendChangeMessage(changeDTO);
 *         // 无需关心具体是哪个平台，分发器会自动处理
 *     }
 * }
 * 
 * 
 * // 示例2：发送告警通知（自动限流）
 * @Component
 * public class AlarmSender {
 *     @Autowired
 *     private NotifierDispatcher notifierDispatcher;
 *     
 *     public void sendAlarm(ThreadPoolAlarmNotifyDTO alarm) {
 *         // 分发器会自动进行限流检查
 *         // 只有通过限流的告警才会真正发送
 *         notifierDispatcher.sendAlarmMessage(alarm);
 *     }
 * }
 * 
 * 
 * // 示例3：扩展新的通知平台
 * // 步骤1：实现 NotifierService 接口
 * public class EmailMessageService implements NotifierService {
 *     // ... 实现各个方法
 * }
 * 
 * // 步骤2：在 NotifierDispatcher 的静态初始化块中注册
 * static {
 *     NOTIFIER_SERVICE_MAP.put("EMAIL", new EmailMessageService());
 * }
 * 
 * // 步骤3：在配置中指定使用邮件通知
 * // onethread.notify-platforms.platform: EMAIL
 * }</pre>
 * 
 * <p><b>优势：</b>
 * <ul>
 *   <li>上层调用者无需关心具体的通知平台</li>
 *   <li>切换通知平台只需修改配置，无需修改代码</li>
 *   <li>自动集成告警限流，避免重复实现</li>
 *   <li>统一的错误处理和日志记录</li>
 * </ul>
 * 
 * <p><b>线程安全性：</b>
 * <ul>
 *   <li>静态 Map 在类加载时初始化，之后只读，线程安全</li>
 *   <li>所有方法都是无状态的，线程安全</li>
 *   <li>{@link AlarmRateLimiter} 使用 ConcurrentHashMap，线程安全</li>
 * </ul>
 * 
 * @author 杨潇
 * @since 2025-05-04
 * @see NotifierService 通知服务接口
 * @see DingTalkMessageService 钉钉通知服务
 * @see AlarmRateLimiter 告警限流器
 */
public class NotifierDispatcher implements NotifierService {

    /**
     * 通知服务注册表
     * <p>
     * 存储所有已注册的通知服务实现，键为平台类型，值为服务实例。
     * 
     * <p><b>数据结构：</b>
     * <pre>
     * {
     *   "DING": DingTalkMessageService实例,
     *   "EMAIL": EmailMessageService实例（扩展）,
     *   "WECHAT": WeChatMessageService实例（扩展）
     * }
     * </pre>
     * 
     * <p><b>注册方式：</b>
     * 在静态初始化块中注册，确保类加载时所有服务都已可用。
     * 
     * <p><b>扩展方式：</b>
     * 新增通知平台只需在静态初始化块中添加一行代码：
     * <pre>{@code
     * NOTIFIER_SERVICE_MAP.put("NEW_PLATFORM", new NewPlatformMessageService());
     * }</pre>
     */
    private static final Map<String, NotifierService> NOTIFIER_SERVICE_MAP = new HashMap<>();

    /**
     * 静态初始化块：注册所有支持的通知平台
     * <p>
     * 在类加载时执行，注册所有可用的通知服务实现。
     * 采用简单工厂模式，集中管理所有通知平台的创建。
     */
    static {
        // 注册钉钉通知服务（当前唯一实现）
        NOTIFIER_SERVICE_MAP.put("DING", new DingTalkMessageService());
        
        /**
         * 后续可以轻松扩展其他通知渠道（示例代码）：
         * 
         * // 邮件通知
         * NOTIFIER_SERVICE_MAP.put("EMAIL", new EmailMessageService());
         * 
         * // 企业微信通知
         * NOTIFIER_SERVICE_MAP.put("WECHAT", new WeChatMessageService());
         * 
         * // 飞书通知
         * NOTIFIER_SERVICE_MAP.put("FEISHU", new FeiShuMessageService());
         * 
         * // Slack 通知
         * NOTIFIER_SERVICE_MAP.put("SLACK", new SlackMessageService());
         */
    }

    /**
     * 发送动态线程池配置变更通知
     * <p>
     * 根据配置自动选择通知服务并发送配置变更通知。
     * 
     * <p><b>执行流程：</b>
     * <ol>
     *   <li>调用 {@link #getNotifierService()} 获取配置的通知服务</li>
     *   <li>如果服务存在，调用其 {@link NotifierService#sendChangeMessage} 方法</li>
     *   <li>如果服务不存在（未配置或配置错误），不发送通知</li>
     * </ol>
     * 
     * <p><b>使用 Optional 的优势：</b>
     * <ul>
     *   <li>优雅处理空值，避免 NullPointerException</li>
     *   <li>链式调用，代码简洁</li>
     *   <li>未配置通知平台时，静默跳过（不抛异常）</li>
     * </ul>
     *
     * @param configChange 配置变更信息对象
     */
    @Override
    public void sendChangeMessage(ThreadPoolConfigChangeDTO configChange) {
        getNotifierService().ifPresent(service -> service.sendChangeMessage(configChange));
    }

    /**
     * 发送 Web 线程池配置变更通知
     * <p>
     * 根据配置自动选择通知服务并发送 Web 线程池配置变更通知。
     * 
     * <p><b>执行流程：</b>与 {@link #sendChangeMessage} 相同，只是调用不同的方法。
     *
     * @param configChange Web 线程池配置变更信息对象
     */
    @Override
    public void sendWebChangeMessage(WebThreadPoolConfigChangeDTO configChange) {
        getNotifierService().ifPresent(service -> service.sendWebChangeMessage(configChange));
    }

    /**
     * 发送线程池运行时告警通知（集成限流机制）
     * <p>
     * 该方法在发送告警前会进行限流检查，只有通过限流的告警才会真正发送。
     * 这是分发器相比直接调用通知服务的核心增强功能。
     * 
     * <p><b>执行流程：</b>
     * <ol>
     *   <li>调用 {@link #getNotifierService()} 获取配置的通知服务</li>
     *   <li>如果服务存在：
     *       <ul>
     *         <li>调用 {@link AlarmRateLimiter#allowAlarm} 进行限流检查</li>
     *         <li>如果通过限流：
     *             <ul>
     *               <li>调用 {@link ThreadPoolAlarmNotifyDTO#resolve()} 延迟加载详细数据</li>
     *               <li>调用通知服务发送告警</li>
     *             </ul>
     *         </li>
     *         <li>如果被限流：静默跳过，不发送</li>
     *       </ul>
     *   </li>
     *   <li>如果服务不存在：不发送通知</li>
     * </ol>
     * 
     * <p><b>限流机制：</b>
     * <pre>
     * 调用 AlarmRateLimiter.allowAlarm()
     *    ├─ 构建键：threadPoolId + "|" + alarmType
     *    ├─ 检查上次告警时间
     *    ├─ 判断是否超过间隔时间
     *    └─ 返回是否允许告警
     *         ↓
     * 如果允许（true）
     *    ├─ 调用 alarm.resolve() 延迟加载数据
     *    └─ 发送告警
     *         ↓
     * 如果限流（false）
     *    └─ 跳过发送
     * </pre>
     * 
     * <p><b>延迟加载设计：</b>
     * {@link ThreadPoolAlarmNotifyDTO} 使用 Supplier 模式延迟加载详细数据，
     * 只有通过限流检查后才调用 {@link ThreadPoolAlarmNotifyDTO#resolve()} 加载数据。
     * 这样可以避免在告警被限流时浪费性能采集数据。
     * 
     * <p><b>性能优化：</b>
     * <pre>
     * 场景：队列使用率持续超过80%，每5秒检查一次，限流间隔5分钟
     * 
     * 15:00:00  检查 → 允许告警 → 加载数据 → 发送  ✅
     * 15:00:05  检查 → 限流 → 跳过加载 → 不发送  ❌（节省性能）
     * 15:00:10  检查 → 限流 → 跳过加载 → 不发送  ❌
     * ... （5分钟内共约60次检查，只有1次发送）
     * 15:05:05  检查 → 允许告警 → 加载数据 → 发送  ✅
     * 
     * 性能节省：59次数据加载操作（包含多个线程池API调用）
     * </pre>
     *
     * @param alarm 告警信息对象，使用 Supplier 模式延迟加载详细数据
     */
    @Override
    public void sendAlarmMessage(ThreadPoolAlarmNotifyDTO alarm) {
        getNotifierService().ifPresent(service -> {
            // 步骤1：告警限流检查
            // 检查该告警是否在限流时间窗口内
            boolean allowSend = AlarmRateLimiter.allowAlarm(
                    alarm.getThreadPoolId(),   // 线程池ID
                    alarm.getAlarmType(),      // 告警类型（Capacity/Activity/Reject）
                    alarm.getInterval()        // 限流间隔（分钟）
            );

            // 步骤2：如果通过限流检查，发送告警
            if (allowSend) {
                // 延迟加载详细数据（只有通过限流才加载，避免性能浪费）
                // resolve() 方法会调用 Supplier 获取完整的告警数据
                ThreadPoolAlarmNotifyDTO resolvedAlarm = alarm.resolve();
                
                // 发送告警到通知平台
                service.sendAlarmMessage(resolvedAlarm);
            }
            // 如果被限流，静默跳过，不发送（避免告警骚扰）
        });
    }

    /**
     * 根据配置获取对应的通知服务实现
     * <p>
     * 该方法是简单工厂模式的核心，根据配置文件中的 platform 配置，
     * 自动选择合适的通知服务实现。
     * 
     * <p><b>查找流程：</b>
     * <ol>
     *   <li>获取全局配置实例：{@link BootstrapConfigProperties#getInstance()}</li>
     *   <li>获取通知平台配置：{@link BootstrapConfigProperties#getNotifyPlatforms()}</li>
     *   <li>获取平台类型：{@link BootstrapConfigProperties.NotifyPlatformsConfig#getPlatform()}</li>
     *   <li>从注册表查找：{@link #NOTIFIER_SERVICE_MAP}.get(platform)</li>
     *   <li>返回 Optional 包装的服务实例</li>
     * </ol>
     * 
     * <p><b>使用 Optional 链式调用：</b>
     * <pre>{@code
     * Optional.ofNullable(配置对象)           // 可能为null
     *     .map(config -> config.getPlatform())  // 获取平台类型
     *     .map(platform -> MAP.get(platform))   // 查找服务实现
     *     // 结果：Optional<NotifierService>
     * }</pre>
     * 
     * <p><b>返回值情况：</b>
     * <table border="1" cellpadding="5">
     *   <tr><th>情况</th><th>返回值</th><th>原因</th></tr>
     *   <tr><td>正常配置</td><td>Optional.of(service)</td><td>找到对应的服务</td></tr>
     *   <tr><td>未配置</td><td>Optional.empty()</td><td>notifyPlatforms为null</td></tr>
     *   <tr><td>平台不存在</td><td>Optional.empty()</td><td>platform在MAP中不存在</td></tr>
     * </table>
     * 
     * <p><b>配置示例：</b>
     * <pre>
     * # 配置钉钉通知
     * onethread:
     *   notify-platforms:
     *     platform: DING  # 会返回 DingTalkMessageService
     *     url: https://oapi.dingtalk.com/...
     * 
     * # 未配置通知平台
     * onethread:
     *   # notify-platforms 未配置  # 会返回 Optional.empty()
     * 
     * # 配置了不存在的平台
     * onethread:
     *   notify-platforms:
     *     platform: UNKNOWN  # 会返回 Optional.empty()
     * </pre>
     *
     * @return Optional 包装的通知服务实例，如果未配置或不存在则返回 {@link Optional#empty()}
     */
    private Optional<NotifierService> getNotifierService() {
        return Optional.ofNullable(BootstrapConfigProperties.getInstance().getNotifyPlatforms())
                .map(BootstrapConfigProperties.NotifyPlatformsConfig::getPlatform)
                .map(platform -> NOTIFIER_SERVICE_MAP.get(platform));
    }
}
