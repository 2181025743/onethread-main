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

import com.nageoffer.onethread.core.notification.dto.ThreadPoolAlarmNotifyDTO;
import com.nageoffer.onethread.core.notification.dto.ThreadPoolConfigChangeDTO;
import com.nageoffer.onethread.core.notification.dto.WebThreadPoolConfigChangeDTO;

/**
 * 通知服务接口
 * <p>
 * 该接口定义了 oneThread 框架中所有通知功能的标准规范，用于发送线程池配置变更通知
 * 和运行时告警通知。不同的通知平台（钉钉、邮件、企业微信等）实现该接口，
 * 提供具体的通知发送逻辑。
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li><b>定义通知规范：</b>统一各种通知平台的接口</li>
 *   <li><b>支持多平台：</b>钉钉、邮件、企业微信等</li>
 *   <li><b>三种通知类型：</b>动态线程池配置变更、Web线程池配置变更、运行时告警</li>
 * </ul>
 * 
 * <p><b>通知类型说明：</b>
 * <table border="1" cellpadding="5">
 *   <tr><th>通知类型</th><th>方法</th><th>触发时机</th><th>用途</th></tr>
 *   <tr>
 *     <td>动态线程池配置变更</td>
 *     <td>{@link #sendChangeMessage}</td>
 *     <td>线程池参数变更时</td>
 *     <td>通知运维人员配置已变更</td>
 *   </tr>
 *   <tr>
 *     <td>Web线程池配置变更</td>
 *     <td>{@link #sendWebChangeMessage}</td>
 *     <td>Web容器线程池变更时</td>
 *     <td>通知Tomcat/Jetty配置变更</td>
 *   </tr>
 *   <tr>
 *     <td>运行时告警</td>
 *     <td>{@link #sendAlarmMessage}</td>
 *     <td>队列堆积、线程满载等</td>
 *     <td>及时发现和处理异常</td>
 *   </tr>
 * </table>
 * 
 * <p><b>实现类：</b>
 * <ul>
 *   <li><b>DingTalkMessageService：</b>钉钉机器人通知实现（已实现）</li>
 *   <li><b>EmailMessageService：</b>邮件通知实现（扩展）</li>
 *   <li><b>WeChatMessageService：</b>企业微信通知实现（扩展）</li>
 *   <li><b>NotifierDispatcher：</b>通知分发器，路由到具体实现（聚合）</li>
 * </ul>
 * 
 * <p><b>设计模式：</b>
 * <ul>
 *   <li><b>策略模式（Strategy Pattern）：</b>不同的实现代表不同的通知策略</li>
 *   <li><b>工厂模式（Factory Pattern）：</b>通过 {@link NotifierDispatcher} 根据配置创建具体实现</li>
 * </ul>
 * 
 * <p><b>使用示例：</b>
 * <pre>{@code
 * // 示例1：发送配置变更通知
 * public class ConfigChangeListener {
 *     @Autowired
 *     private NotifierService notifierService;
 *     
 *     public void onConfigChange(ThreadPoolConfigChangeDTO changeDTO) {
 *         // 发送配置变更通知
 *         notifierService.sendChangeMessage(changeDTO);
 *     }
 * }
 * 
 * 
 * // 示例2：发送告警通知
 * public class AlarmChecker {
 *     @Autowired
 *     private NotifierService notifierService;
 *     
 *     public void checkAndAlarm(ThreadPoolAlarmNotifyDTO alarmDTO) {
 *         // 检查队列使用率
 *         if (queueUsage > 80) {
 *             notifierService.sendAlarmMessage(alarmDTO);
 *         }
 *     }
 * }
 * 
 * 
 * // 示例3：实现自定义通知平台
 * public class EmailMessageService implements NotifierService {
 *     @Override
 *     public void sendChangeMessage(ThreadPoolConfigChangeDTO configChange) {
 *         String subject = "线程池配置变更通知";
 *         String content = buildEmailContent(configChange);
 *         emailClient.send(configChange.getReceives(), subject, content);
 *     }
 *     
 *     @Override
 *     public void sendWebChangeMessage(WebThreadPoolConfigChangeDTO configChange) {
 *         // 实现Web线程池配置变更邮件通知
 *     }
 *     
 *     @Override
 *     public void sendAlarmMessage(ThreadPoolAlarmNotifyDTO alarm) {
 *         // 实现告警邮件通知
 *     }
 * }
 * }</pre>
 * 
 * <p><b>扩展指南：</b>
 * <ol>
 *   <li>实现 {@link NotifierService} 接口</li>
 *   <li>实现三个通知方法</li>
 *   <li>在 {@link NotifierDispatcher} 中注册新实现</li>
 *   <li>在配置文件中指定通知平台类型</li>
 * </ol>
 * 
 * @author 杨潇
 * @since 2025-05-03
 * @see NotifierDispatcher 通知分发器（默认实现）
 * @see DingTalkMessageService 钉钉通知实现
 * @see ThreadPoolConfigChangeDTO 配置变更DTO
 * @see ThreadPoolAlarmNotifyDTO 告警通知DTO
 */
public interface NotifierService {

    /**
     * 发送线程池配置变更通知
     * <p>
     * 当动态线程池的配置参数发生变更时（如核心线程数、队列容量等），
     * 调用该方法发送通知，告知相关人员配置已更新。
     * 
     * <p><b>触发时机：</b>
     * <ul>
     *   <li>配置中心（Nacos/Apollo）推送配置变更</li>
     *   <li>前端控制台手动修改配置</li>
     *   <li>配置参数实际生效后</li>
     * </ul>
     * 
     * <p><b>通知内容：</b>
     * <ul>
     *   <li>线程池 ID</li>
     *   <li>变更的参数对比（旧值 => 新值）</li>
     *   <li>变更时间</li>
     *   <li>应用实例地址</li>
     * </ul>
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>通知运维人员关注配置变更</li>
     *   <li>记录配置变更历史</li>
     *   <li>配合审计系统追踪变更记录</li>
     * </ul>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * // 构建配置变更 DTO
     * ThreadPoolConfigChangeDTO changeDTO = ThreadPoolConfigChangeDTO.builder()
     *     .threadPoolId("order-processor")
     *     .applicationName("order-service")
     *     .identify("192.168.1.100:8080")
     *     .changes(changes)  // 参数变更对比
     *     .updateTime("2025-04-30 15:30:45")
     *     .build();
     * 
     * // 发送通知
     * notifierService.sendChangeMessage(changeDTO);
     * }</pre>
     *
     * @param configChange 配置变更信息对象，包含变更的详细数据
     * @see ThreadPoolConfigChangeDTO 配置变更数据传输对象
     */
    void sendChangeMessage(ThreadPoolConfigChangeDTO configChange);

    /**
     * 发送 Web 容器线程池配置变更通知
     * <p>
     * 当 Web 容器（Tomcat、Jetty、Undertow）的线程池配置发生变更时，
     * 调用该方法发送通知。
     * 
     * <p><b>触发时机：</b>
     * <ul>
     *   <li>配置中心推送 Web 线程池配置变更</li>
     *   <li>前端控制台修改 Web 线程池配置</li>
     *   <li>Tomcat/Jetty 连接器线程池参数调整后</li>
     * </ul>
     * 
     * <p><b>通知内容：</b>
     * <ul>
     *   <li>容器类型（Tomcat、Jetty 等）</li>
     *   <li>变更的参数对比</li>
     *   <li>变更时间</li>
     *   <li>应用实例地址</li>
     * </ul>
     * 
     * <p><b>与动态线程池通知的区别：</b>
     * <ul>
     *   <li>动态线程池：应用自定义的业务线程池</li>
     *   <li>Web线程池：Web容器的连接器线程池</li>
     * </ul>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * // 构建 Web 线程池配置变更 DTO
     * WebThreadPoolConfigChangeDTO changeDTO = WebThreadPoolConfigChangeDTO.builder()
     *     .containerType("Tomcat")  // 容器类型
     *     .applicationName("order-service")
     *     .changes(changes)
     *     .updateTime("2025-04-30 15:30:45")
     *     .build();
     * 
     * // 发送通知
     * notifierService.sendWebChangeMessage(changeDTO);
     * }</pre>
     *
     * @param configChange Web 线程池配置变更信息对象
     * @see WebThreadPoolConfigChangeDTO Web线程池配置变更DTO
     */
    void sendWebChangeMessage(WebThreadPoolConfigChangeDTO configChange);

    /**
     * 发送线程池运行时告警通知
     * <p>
     * 当线程池运行状态异常时（如队列堆积、线程池满载、拒绝次数增加等），
     * 调用该方法发送告警通知，帮助及时发现和处理问题。
     * 
     * <p><b>触发时机：</b>
     * <ul>
     *   <li>队列使用率超过阈值（默认80%）</li>
     *   <li>活跃线程率超过阈值（默认80%）</li>
     *   <li>拒绝次数有新增</li>
     *   <li>自定义告警规则触发</li>
     * </ul>
     * 
     * <p><b>告警类型：</b>
     * <table border="1" cellpadding="5">
     *   <tr><th>类型</th><th>alarmType值</th><th>含义</th><th>处理建议</th></tr>
     *   <tr>
     *     <td>队列堆积</td>
     *     <td>Capacity</td>
     *     <td>队列使用率过高</td>
     *     <td>扩大队列容量或增加线程数</td>
     *   </tr>
     *   <tr>
     *     <td>线程满载</td>
     *     <td>Activity</td>
     *     <td>活跃线程率过高</td>
     *     <td>增加最大线程数</td>
     *   </tr>
     *   <tr>
     *     <td>任务拒绝</td>
     *     <td>Reject</td>
     *     <td>有任务被拒绝</td>
     *     <td>紧急扩容或限流</td>
     *   </tr>
     * </table>
     * 
     * <p><b>通知内容：</b>
     * <ul>
     *   <li>告警类型和严重程度</li>
     *   <li>线程池完整运行时状态</li>
     *   <li>告警时间和应用信息</li>
     *   <li>接收人（支持 @ 功能）</li>
     *   <li>告警限流提示</li>
     * </ul>
     * 
     * <p><b>告警限流：</b>
     * 同一个线程池的同一类型告警，在配置的间隔时间内只会发送一次，
     * 避免频繁告警骚扰。通过 {@link AlarmRateLimiter} 实现。
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * // 示例1：队列堆积告警
     * ThreadPoolAlarmNotifyDTO alarm = ThreadPoolAlarmNotifyDTO.builder()
     *     .alarmType("Capacity")            // 告警类型：队列堆积
     *     .threadPoolId("order-processor")
     *     .interval(5)                      // 告警间隔5分钟
     *     .build();
     * 
     * // 设置延迟加载的数据
     * alarm.setSupplier(() -> {
     *     alarm.setApplicationName("order-service");
     *     alarm.setIdentify("192.168.1.100:8080");
     *     alarm.setCorePoolSize(10);
     *     alarm.setMaximumPoolSize(20);
     *     alarm.setActivePoolSize(20);
     *     alarm.setWorkQueueSize(450);
     *     alarm.setWorkQueueCapacity(500);
     *     // ... 设置其他字段
     *     return alarm;
     * });
     * 
     * // 发送告警（会自动进行限流检查）
     * notifierService.sendAlarmMessage(alarm);
     * 
     * 
     * // 示例2：活跃线程率告警
     * ThreadPoolAlarmNotifyDTO alarm = ThreadPoolAlarmNotifyDTO.builder()
     *     .alarmType("Activity")            // 告警类型：线程满载
     *     .threadPoolId("message-consumer")
     *     .interval(10)                     // 告警间隔10分钟
     *     .build();
     * 
     * alarm.setSupplier(() -> {
     *     // 填充详细数据
     *     return alarm;
     * });
     * 
     * notifierService.sendAlarmMessage(alarm);
     * }</pre>
     * 
     * <p><b>实现注意事项：</b>
     * <ul>
     *   <li>发送前应进行告警限流检查</li>
     *   <li>发送失败应记录日志，但不抛出异常</li>
     *   <li>支持批量发送（如果通知平台支持）</li>
     *   <li>考虑异步发送，避免阻塞主流程</li>
     * </ul>
     * 
     * <p><b>扩展示例：</b>
     * <pre>{@code
     * // 实现邮件通知
     * public class EmailMessageService implements NotifierService {
     *     @Autowired
     *     private JavaMailSender mailSender;
     *     
     *     @Override
     *     public void sendChangeMessage(ThreadPoolConfigChangeDTO configChange) {
     *         MimeMessage message = mailSender.createMimeMessage();
     *         MimeMessageHelper helper = new MimeMessageHelper(message, true);
     *         
     *         helper.setTo(configChange.getReceives().split(","));
     *         helper.setSubject("[配置变更] " + configChange.getApplicationName());
     *         helper.setText(buildHtmlContent(configChange), true);
     *         
     *         mailSender.send(message);
     *     }
     *     
     *     @Override
     *     public void sendAlarmMessage(ThreadPoolAlarmNotifyDTO alarm) {
     *         // 实现告警邮件发送
     *     }
     *     
     *     @Override
     *     public void sendWebChangeMessage(WebThreadPoolConfigChangeDTO configChange) {
     *         // 实现Web配置变更邮件发送
     *     }
     * }
     * }</pre>
     * 
     * @author 杨潇
     * @since 2025-05-03
     * @see DingTalkMessageService 钉钉通知服务实现
     * @see NotifierDispatcher 通知分发器
 * @see AlarmRateLimiter 告警限流器
 */
    void sendAlarmMessage(ThreadPoolAlarmNotifyDTO alarm);
}
