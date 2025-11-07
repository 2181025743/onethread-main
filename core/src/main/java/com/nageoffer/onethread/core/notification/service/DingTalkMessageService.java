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

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.nageoffer.onethread.core.config.BootstrapConfigProperties;
import com.nageoffer.onethread.core.notification.dto.ThreadPoolAlarmNotifyDTO;
import com.nageoffer.onethread.core.notification.dto.ThreadPoolConfigChangeDTO;
import com.nageoffer.onethread.core.notification.dto.WebThreadPoolConfigChangeDTO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nageoffer.onethread.core.constant.Constants.DING_ALARM_NOTIFY_MESSAGE_TEXT;
import static com.nageoffer.onethread.core.constant.Constants.DING_CONFIG_CHANGE_MESSAGE_TEXT;
import static com.nageoffer.onethread.core.constant.Constants.DING_CONFIG_WEB_CHANGE_MESSAGE_TEXT;

/**
 * 钉钉消息通知服务
 * <p>
 * 该类实现了 {@link NotifierService} 接口，提供通过钉钉机器人发送通知的功能。
 * 支持发送线程池配置变更通知和运行时告警通知，消息格式使用钉钉支持的 Markdown 格式。
 * 
 * <p><b>核心功能：</b>
 * <ul>
 *   <li><b>配置变更通知：</b>发送线程池参数变更的详细对比信息</li>
 *   <li><b>告警通知：</b>发送线程池运行状态异常告警</li>
 *   <li><b>Markdown格式：</b>使用钉钉支持的 Markdown 格式，支持富文本显示</li>
 *   <li><b>@功能：</b>支持 @ 指定的接收人，确保消息及时查看</li>
 * </ul>
 * 
 * <p><b>钉钉机器人配置：</b>
 * <ol>
 *   <li>在钉钉群中添加自定义机器人</li>
 *   <li>安全设置选择"自定义关键词"（如 "oneThread"）</li>
 *   <li>或选择"加签"（更安全，需要实现签名逻辑）</li>
 *   <li>复制 WebHook 地址</li>
 *   <li>在配置文件中配置 WebHook 地址</li>
 * </ol>
 * 
 * <p><b>配置示例（application.yml）：</b>
 * <pre>
 * onethread:
 *   notify-platforms:
 *     platform: DING
 *     url: https://oapi.dingtalk.com/robot/send?access_token=xxxxxx
 * </pre>
 * 
 * <p><b>消息格式：</b>
 * 钉钉支持多种消息类型，本类使用 Markdown 类型：
 * <ul>
 *   <li>支持标题、加粗、颜色、字体大小等</li>
 *   <li>支持 @ 功能（需要手机号）</li>
 *   <li>支持分隔线、列表等格式</li>
 * </ul>
 * 
 * <p><b>@ 功能说明：</b>
 * <ul>
 *   <li>需要提供接收人的手机号</li>
 *   <li>多个接收人用逗号分隔</li>
 *   <li>被 @ 的人会收到特别提醒</li>
 *   <li>示例："13800138000,13900139000"</li>
 * </ul>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>生产环境的线程池监控告警</li>
 *   <li>配置变更通知</li>
 *   <li>紧急问题的快速通知</li>
 * </ul>
 * 
 * <p><b>使用示例：</b>
 * <pre>{@code
 * // 示例1：发送配置变更通知
 * DingTalkMessageService service = new DingTalkMessageService();
 * 
 * ThreadPoolConfigChangeDTO changeDTO = ThreadPoolConfigChangeDTO.builder()
 *     .threadPoolId("order-processor")
 *     .applicationName("order-service")
 *     .activeProfile("production")
 *     .identify("192.168.1.100")
 *     .receives("13800138000,13900139000")  // @ 这两个人
 *     .changes(changes)
 *     .updateTime("2025-04-30 15:30:45")
 *     .build();
 * 
 * service.sendChangeMessage(changeDTO);
 * 
 * 
 * // 示例2：发送告警通知
 * ThreadPoolAlarmNotifyDTO alarmDTO = ThreadPoolAlarmNotifyDTO.builder()
 *     .threadPoolId("order-processor")
 *     .alarmType("Capacity")
 *     .receives("13800138000")
 *     .interval(5)
 *     .build();
 * 
 * alarmDTO.setSupplier(() -> {
 *     // 填充详细数据
 *     alarmDTO.setCorePoolSize(10);
 *     alarmDTO.setActivePoolSize(20);
 *     // ... 其他字段
 *     return alarmDTO;
 * });
 * 
 * service.sendAlarmMessage(alarmDTO.resolve());
 * }</pre>
 * 
 * <p><b>错误处理：</b>
 * <ul>
 *   <li>发送失败会记录错误日志，但不抛出异常</li>
 *   <li>网络异常会被捕获并记录</li>
 *   <li>钉钉返回错误码时会记录错误信息</li>
 * </ul>
 * 
 * <p><b>性能特点：</b>
 * <ul>
 *   <li>使用 HTTP POST 请求发送消息</li>
 *   <li>网络耗时约 100-500ms（取决于网络状况）</li>
 *   <li>建议异步发送，避免阻塞主流程</li>
 * </ul>
 * 
 * <p><b>注意事项：</b>
 * <ul>
 *   <li>钉钉机器人有频率限制（通常 20次/分钟）</li>
 *   <li>消息长度有限制（建议不超过 20KB）</li>
 *   <li>@ 功能需要提供手机号，且该手机号需要在钉钉群中</li>
 *   <li>access_token 是敏感信息，需要妥善保管</li>
 * </ul>
 * 
 * @author 杨潇
 * @since 2025-04-30
 * @see NotifierService 通知服务接口
 * @see NotifierDispatcher 通知分发器
 * @see ThreadPoolAlarmNotifyDTO 告警通知DTO
 * @see ThreadPoolConfigChangeDTO 配置变更DTO
 */
@Slf4j
public class DingTalkMessageService implements NotifierService {

    /**
     * 发送线程池配置变更通知到钉钉机器人
     * <p>
     * 当动态线程池的配置参数发生变更时，发送详细的变更对比信息到钉钉群。
     * 
     * <p><b>消息内容：</b>
     * <ul>
     *   <li>应用名称和环境</li>
     *   <li>线程池 ID</li>
     *   <li>应用实例地址</li>
     *   <li>核心线程数变更（旧值 ➲ 新值）</li>
     *   <li>最大线程数变更</li>
     *   <li>线程存活时间变更</li>
     *   <li>队列类型和容量变更</li>
     *   <li>拒绝策略变更</li>
     *   <li>接收人（支持 @ 功能）</li>
     *   <li>变更时间</li>
     * </ul>
     * 
     * <p><b>消息效果：</b>
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
     * OWNER：@13800138000
     * ---
     * 变更时间：2025-04-30 15:30:45
     * </pre>
     * 
     * <p><b>实现步骤：</b>
     * <ol>
     *   <li>从 DTO 中提取变更数据</li>
     *   <li>使用消息模板格式化为 Markdown 文本</li>
     *   <li>解析接收人列表（逗号分隔）</li>
     *   <li>调用 {@link #sendDingTalkMarkdownMessage} 发送消息</li>
     * </ol>
     *
     * @param configChangeDTO 线程池配置变更数据传输对象，包含变更详情和接收人信息
     */
    @Override
    public void sendChangeMessage(ThreadPoolConfigChangeDTO configChangeDTO) {
        // 获取参数变更对比数据
        Map<String, ThreadPoolConfigChangeDTO.ChangePair<?>> changes = configChangeDTO.getChanges();
        
        // 使用消息模板格式化消息文本
        String text = String.format(
                DING_CONFIG_CHANGE_MESSAGE_TEXT,
                configChangeDTO.getActiveProfile().toUpperCase(),      // 环境（大写）
                configChangeDTO.getThreadPoolId(),                     // 线程池ID
                configChangeDTO.getIdentify() + ":" + configChangeDTO.getApplicationName(),  // 实例:应用名
                changes.get("corePoolSize").getBefore() + " ➲ " + changes.get("corePoolSize").getAfter(),
                changes.get("maximumPoolSize").getBefore() + " ➲ " + changes.get("maximumPoolSize").getAfter(),
                changes.get("keepAliveTime").getBefore() + " ➲ " + changes.get("keepAliveTime").getAfter(),
                configChangeDTO.getWorkQueue(),                        // 队列类型
                changes.get("queueCapacity").getBefore() + " ➲ " + changes.get("queueCapacity").getAfter(),
                changes.get("rejectedHandler").getBefore(),            // 旧拒绝策略
                changes.get("rejectedHandler").getAfter(),             // 新拒绝策略
                configChangeDTO.getReceives(),                         // 接收人
                configChangeDTO.getUpdateTime()                        // 变更时间
        );

        // 解析接收人列表（逗号分隔的手机号）
        List<String> atMobiles = CollectionUtil.newArrayList(configChangeDTO.getReceives().split(","));
        
        // 发送钉钉消息
        sendDingTalkMarkdownMessage("动态线程池通知", text, atMobiles);
    }

    /**
     * 发送 Web 容器线程池配置变更通知到钉钉机器人
     * <p>
     * 当 Web 容器（Tomcat、Jetty 等）的线程池配置发生变更时，
     * 发送变更通知到钉钉群。
     * 
     * <p><b>消息内容：</b>
     * <ul>
     *   <li>容器类型（Tomcat、Jetty 等）</li>
     *   <li>应用名称和环境</li>
     *   <li>应用实例地址</li>
     *   <li>核心线程数变更</li>
     *   <li>最大线程数变更</li>
     *   <li>线程存活时间变更</li>
     *   <li>接收人</li>
     *   <li>变更时间</li>
     * </ul>
     * 
     * <p><b>消息效果：</b>
     * <pre>
     * [通知] PRODUCTION - Tomcat线程池参数变更
     * ---
     * 应用实例：192.168.1.100:order-service
     * 核心线程数：10 ➲ 20
     * 最大线程数：200 ➲ 500
     * 线程存活时间：60 ➲ 120
     * OWNER：@13800138000
     * ---
     * 变更时间：2025-04-30 15:30:45
     * </pre>
     *
     * @param configChangeDTO Web 线程池配置变更数据传输对象
     */
    @Override
    public void sendWebChangeMessage(WebThreadPoolConfigChangeDTO configChangeDTO) {
        // 获取参数变更对比数据
        Map<String, WebThreadPoolConfigChangeDTO.ChangePair<?>> changes = configChangeDTO.getChanges();
        String webContainerName = configChangeDTO.getWebContainerName();
        
        // 使用消息模板格式化消息文本
        String text = String.format(
                DING_CONFIG_WEB_CHANGE_MESSAGE_TEXT,
                configChangeDTO.getActiveProfile().toUpperCase(),      // 环境（大写）
                webContainerName,                                      // 容器类型（Tomcat/Jetty）
                configChangeDTO.getIdentify() + ":" + configChangeDTO.getApplicationName(),
                changes.get("corePoolSize").getBefore() + " ➲ " + changes.get("corePoolSize").getAfter(),
                changes.get("maximumPoolSize").getBefore() + " ➲ " + changes.get("maximumPoolSize").getAfter(),
                changes.get("keepAliveTime").getBefore() + " ➲ " + changes.get("keepAliveTime").getAfter(),
                configChangeDTO.getReceives(),                         // 接收人
                webContainerName,                                      // 容器类型（提示）
                configChangeDTO.getUpdateTime()                        // 变更时间
        );

        // 解析接收人列表
        List<String> atMobiles = CollectionUtil.newArrayList(configChangeDTO.getReceives().split(","));
        
        // 发送钉钉消息
        sendDingTalkMarkdownMessage(webContainerName + "线程池通知", text, atMobiles);
    }

    /**
     * 发送线程池运行时告警通知到钉钉机器人
     * <p>
     * 当线程池运行状态异常时（队列堆积、线程满载、任务拒绝等），
     * 发送包含完整运行时信息的告警消息到钉钉群。
     * 
     * <p><b>消息内容：</b>
     * <ul>
     *   <li>应用名称和环境</li>
     *   <li>线程池 ID</li>
     *   <li>应用实例地址</li>
     *   <li>告警类型（队列堆积/线程满载/任务拒绝）</li>
     *   <li>线程池详细状态（核心数、最大数、当前数、活跃数等）</li>
     *   <li>队列详细状态（类型、容量、大小、剩余）</li>
     *   <li>拒绝策略和拒绝次数（红色高亮）</li>
     *   <li>接收人</li>
     *   <li>告警间隔提示</li>
     *   <li>告警时间</li>
     * </ul>
     * 
     * <p><b>消息效果：</b>
     * <pre>
     * [警报] PRODUCTION - 动态线程池运行告警
     * ---
     * 线程池ID：order-processor
     * 应用实例：192.168.1.100:order-service
     * 告警类型：队列堆积告警
     * ---
     * 核心线程数：10
     * 最大线程数：20
     * 当前线程数：20
     * 活跃线程数：20
     * 历史最大线程数：20
     * 线程池任务总量：50000
     * ---
     * 队列类型：LinkedBlockingQueue
     * 队列容量：500
     * 队列元素个数：450
     * 队列剩余个数：50
     * ---
     * 拒绝策略：CallerRunsPolicy
     * 拒绝策略执行次数：10  ← 红色高亮
     * OWNER：@13800138000
     * 提示：5分钟内此线程池不会重复告警
     * ---
     * 告警时间：2025-04-30 15:30:45
     * </pre>
     *
     * @param alarm 告警信息对象，包含线程池的完整运行时状态
     */
    @Override
    public void sendAlarmMessage(ThreadPoolAlarmNotifyDTO alarm) {
        // 使用消息模板格式化告警消息文本
        String text = String.format(
                DING_ALARM_NOTIFY_MESSAGE_TEXT,
                alarm.getActiveProfile().toUpperCase(),          // 环境（大写）
                alarm.getThreadPoolId(),                         // 线程池ID
                alarm.getIdentify() + ":" + alarm.getApplicationName(),  // 实例:应用名
                alarm.getAlarmType(),                            // 告警类型
                alarm.getCorePoolSize(),                         // 核心线程数
                alarm.getMaximumPoolSize(),                      // 最大线程数
                alarm.getCurrentPoolSize(),                      // 当前线程数
                alarm.getActivePoolSize(),                       // 活跃线程数
                alarm.getLargestPoolSize(),                      // 历史最大线程数
                alarm.getCompletedTaskCount(),                   // 已完成任务数
                alarm.getWorkQueueName(),                        // 队列类型
                alarm.getWorkQueueCapacity(),                    // 队列容量
                alarm.getWorkQueueSize(),                        // 队列当前大小
                alarm.getWorkQueueRemainingCapacity(),           // 队列剩余容量
                alarm.getRejectedHandlerName(),                  // 拒绝策略
                alarm.getRejectCount(),                          // 拒绝次数
                alarm.getReceives(),                             // 接收人
                alarm.getInterval(),                             // 告警间隔
                alarm.getCurrentTime()                           // 告警时间
        );

        // 解析接收人列表
        List<String> atMobiles = CollectionUtil.newArrayList(alarm.getReceives().split(","));
        
        // 发送钉钉消息
        sendDingTalkMarkdownMessage("线程池告警通知", text, atMobiles);
    }

    /**
     * 通用的钉钉 Markdown 消息发送方法
     * <p>
     * 该方法封装了钉钉机器人的 HTTP API 调用逻辑，构建符合钉钉要求的 JSON 请求体，
     * 并发送到钉钉服务器。
     * 
     * <p><b>钉钉 Markdown 消息格式：</b>
     * <pre>{@code
     * {
     *   "msgtype": "markdown",
     *   "markdown": {
     *     "title": "消息标题",
     *     "text": "Markdown格式的消息内容"
     *   },
     *   "at": {
     *     "atMobiles": ["13800138000", "13900139000"]
     *   }
     * }
     * }</pre>
     * 
     * <p><b>执行流程：</b>
     * <ol>
     *   <li>构建 markdown 对象（包含 title 和 text）</li>
     *   <li>构建 at 对象（指定要 @ 的手机号列表）</li>
     *   <li>构建完整的请求体（包含 msgtype、markdown、at）</li>
     *   <li>从配置中获取钉钉 WebHook 地址</li>
     *   <li>使用 HttpUtil 发送 POST 请求</li>
     *   <li>解析响应，检查是否成功</li>
     *   <li>记录错误日志（如果失败）</li>
     * </ol>
     * 
     * <p><b>钉钉 API 响应：</b>
     * <pre>{@code
     * // 成功
     * {
     *   "errcode": 0,
     *   "errmsg": "ok"
     * }
     * 
     * // 失败
     * {
     *   "errcode": 310000,
     *   "errmsg": "关键词不在内容中"
     * }
     * }</pre>
     * 
     * <p><b>常见错误码：</b>
     * <table border="1" cellpadding="5">
     *   <tr><th>错误码</th><th>含义</th><th>解决方案</th></tr>
     *   <tr><td>0</td><td>成功</td><td>-</td></tr>
     *   <tr><td>310000</td><td>关键词不在内容中</td><td>检查安全设置的关键词</td></tr>
     *   <tr><td>300001</td><td>无效的access_token</td><td>检查WebHook地址</td></tr>
     *   <tr><td>300002</td><td>机器人被停用</td><td>重新启用机器人</td></tr>
     * </table>
     * 
     * <p><b>错误处理：</b>
     * <ul>
     *   <li>使用 try-catch 捕获所有异常，避免影响主流程</li>
     *   <li>网络异常、JSON解析异常都会被记录到日志</li>
     *   <li>钉钉返回错误码时会记录错误信息</li>
     *   <li>不会抛出异常到调用方</li>
     * </ul>
     *
     * @param title     消息标题（会显示在钉钉消息列表中）
     * @param text      Markdown 格式的消息内容
     * @param atMobiles 要 @ 的手机号列表
     */
    private void sendDingTalkMarkdownMessage(String title, String text, List<String> atMobiles) {
        // 1. 构建 markdown 对象
        Map<String, Object> markdown = new HashMap<>();
        markdown.put("title", title);  // 消息标题
        markdown.put("text", text);    // Markdown格式的消息内容

        // 2. 构建 at 对象（@ 功能）
        Map<String, Object> at = new HashMap<>();
        at.put("atMobiles", atMobiles);  // 要 @ 的手机号列表

        // 3. 构建完整的请求体
        Map<String, Object> request = new HashMap<>();
        request.put("msgtype", "markdown");  // 消息类型：markdown
        request.put("markdown", markdown);   // markdown 内容
        request.put("at", at);               // @ 信息

        try {
            // 4. 从配置中获取钉钉 WebHook 地址
            String serverUrl = BootstrapConfigProperties.getInstance().getNotifyPlatforms().getUrl();
            
            // 5. 发送 HTTP POST 请求
            // 将请求对象序列化为 JSON 字符串
            String responseBody = HttpUtil.post(serverUrl, JSON.toJSONString(request));
            
            // 6. 解析响应
            DingRobotResponse response = JSON.parseObject(responseBody, DingRobotResponse.class);
            
            // 7. 检查响应结果
            if (response.getErrcode() != 0) {
                // 钉钉返回错误码，记录错误日志
                log.error("Ding failed to send message, reason: {}", response.errmsg);
            }
            // 成功发送，不记录日志（避免日志过多）
            
        } catch (Exception ex) {
            // 捕获所有异常（网络异常、JSON解析异常等）
            // 记录错误日志，但不抛出异常，避免影响主流程
            log.error("Ding failed to send message.", ex);
        }
    }

    /**
     * 钉钉机器人 API 响应实体
     * <p>
     * 封装了钉钉机器人 API 的响应数据。
     * 
     * <p><b>响应格式：</b>
     * <pre>{@code
     * {
     *   "errcode": 0,      // 错误码，0表示成功
     *   "errmsg": "ok"     // 错误信息
     * }
     * }</pre>
     * 
     * <p><b>字段说明：</b>
     * <ul>
     *   <li><b>errcode：</b>错误码，0 表示成功，非0 表示失败</li>
     *   <li><b>errmsg：</b>错误描述信息，成功时为 "ok"</li>
     * </ul>
     */
    @Data
    static class DingRobotResponse {

        /**
         * 错误码
         * <p>
         * 0 表示成功，非0 表示失败。
         * 
         * <p><b>常见错误码：</b>
         * <ul>
         *   <li>0 - 成功</li>
         *   <li>310000 - 关键词不在内容中</li>
         *   <li>300001 - 无效的 access_token</li>
         *   <li>300002 - 机器人被停用</li>
         * </ul>
         */
        private Long errcode;

        /**
         * 错误描述信息
         * <p>
         * 成功时为 "ok"，失败时为具体的错误描述。
         * 
         * <p><b>示例：</b>
         * <ul>
         *   <li>"ok" - 成功</li>
         *   <li>"关键词不在内容中" - 安全设置校验失败</li>
         *   <li>"invalid access_token" - token 无效</li>
         * </ul>
         */
        private String errmsg;
    }
}
