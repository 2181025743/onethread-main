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
 * Web 容器线程池配置变更通知数据传输对象（DTO）
 * <p>
 * 该类封装了 Web 容器（Tomcat、Jetty、Undertow）线程池配置变更通知所需的所有信息。
 * 与 {@link ThreadPoolConfigChangeDTO} 类似，但专门用于 Web 容器线程池的配置变更。
 * 
 * <p><b>核心功能：</b>
 * <ul>
 *   <li><b>数据封装：</b>封装 Web 容器线程池配置变更的所有数据</li>
 *   <li><b>容器标识：</b>标识具体的容器类型（Tomcat、Jetty 等）</li>
 *   <li><b>变更对比：</b>通过 {@link ChangePair} 保存参数的旧值和新值</li>
 * </ul>
 * 
 * <p><b>与动态线程池配置变更的区别：</b>
 * <table border="1" cellpadding="5">
 *   <tr><th>特性</th><th>ThreadPoolConfigChangeDTO</th><th>WebThreadPoolConfigChangeDTO</th></tr>
 *   <tr><td>适用对象</td><td>应用自定义线程池</td><td>Web容器线程池</td></tr>
 *   <tr><td>线程池标识</td><td>threadPoolId</td><td>webContainerName</td></tr>
 *   <tr><td>队列信息</td><td>有workQueue字段</td><td>无（Web容器不需要）</td></tr>
 *   <tr><td>配置参数</td><td>更多（队列、拒绝策略等）</td><td>较少（核心数、最大数等）</td></tr>
 * </table>
 * 
 * <p><b>支持的容器类型：</b>
 * <ul>
 *   <li><b>Tomcat：</b>最常用的 Web 容器</li>
 *   <li><b>Jetty：</b>轻量级 Web 容器</li>
 *   <li><b>Undertow：</b>高性能 Web 容器</li>
 * </ul>
 * 
 * <p><b>数据结构：</b>
 * <pre>
 * WebThreadPoolConfigChangeDTO {
 *   webContainerName: "Tomcat",
 *   applicationName: "order-service",
 *   activeProfile: "production",
 *   identify: "192.168.1.100",
 *   receives: "13800138000",
 *   changes: {
 *     "corePoolSize": ChangePair(10, 20),       // 10 → 20
 *     "maximumPoolSize": ChangePair(200, 500),  // 200 → 500
 *     "keepAliveTime": ChangePair(60, 120)      // 60 → 120
 *   },
 *   updateTime: "2025-04-30 15:30:45"
 * }
 * </pre>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>Web 容器线程池配置变更时发送通知</li>
 *   <li>优化 Tomcat 并发连接数后通知相关人员</li>
 *   <li>记录 Web 容器调优历史</li>
 * </ul>
 * 
 * <p><b>构建示例：</b>
 * <pre>{@code
 * // 准备变更对比数据
 * Map<String, ChangePair<?>> changes = new HashMap<>();
 * changes.put("corePoolSize", new ChangePair<>(10, 20));
 * changes.put("maximumPoolSize", new ChangePair<>(200, 500));
 * changes.put("keepAliveTime", new ChangePair<>(60L, 120L));
 * 
 * // 使用 Builder 构建 DTO
 * WebThreadPoolConfigChangeDTO changeDTO = WebThreadPoolConfigChangeDTO.builder()
 *     .webContainerName("Tomcat")
 *     .applicationName("order-service")
 *     .activeProfile("production")
 *     .identify("192.168.1.100")
 *     .receives("13800138000")
 *     .changes(changes)
 *     .updateTime("2025-04-30 15:30:45")
 *     .build();
 * 
 * // 发送通知
 * notifierDispatcher.sendWebChangeMessage(changeDTO);
 * }</pre>
 * 
 * <p><b>生成的通知消息：</b>
 * <pre>
 * [通知] PRODUCTION - Tomcat线程池参数变更
 * ---
 * 应用实例：192.168.1.100:order-service
 * 核心线程数：10 ➲ 20
 * 最大线程数：200 ➲ 500
 * 线程存活时间：60 ➲ 120
 * OWNER：@13800138000
 * 提示：Tomcat线程池配置变更实时通知
 * ---
 * 变更时间：2025-04-30 15:30:45
 * </pre>
 * 
 * @author 杨潇
 * @since 2025-05-12
 * @see ThreadPoolConfigChangeDTO 动态线程池配置变更DTO
 * @see NotifierService 通知服务接口
 * @see DingTalkMessageService 钉钉通知服务
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebThreadPoolConfigChangeDTO {

    /**
     * Web 容器名称
     * <p>
     * 标识具体的 Web 容器类型。
     * 
     * <p><b>可能的值：</b>
     * <ul>
     *   <li>"Tomcat" - Apache Tomcat</li>
     *   <li>"Jetty" - Eclipse Jetty</li>
     *   <li>"Undertow" - JBoss Undertow</li>
     * </ul>
     * 
     * <p><b>获取方式：</b>
     * 通过检测 Spring Boot 的 WebServerApplicationContext 类型判断：
     * <ul>
     *   <li>TomcatWebServer → "Tomcat"</li>
     *   <li>JettyWebServer → "Jetty"</li>
     *   <li>UndertowWebServer → "Undertow"</li>
     * </ul>
     */
    private String webContainerName;

    /**
     * 环境标识
     * <p>
     * 标识配置变更发生在哪个运行环境。
     * 
     * <p><b>示例：</b>"dev"、"test"、"production"
     */
    private String activeProfile;

    /**
     * 应用名称
     * <p>
     * 标识配置变更来源于哪个应用。
     * 
     * <p><b>示例：</b>"order-service"
     */
    private String applicationName;

    /**
     * 应用实例唯一标识（IP地址）
     * <p>
     * 标识配置变更来源于哪个具体的应用实例。
     * 
     * <p><b>示例：</b>"192.168.1.100"
     */
    private String identify;

    /**
     * 通知接收人
     * <p>
     * 配置变更通知的接收人列表，多个接收人用逗号分隔。
     * 
     * <p><b>格式：</b>"13800138000,13900139000"（钉钉手机号）
     */
    private String receives;

    /**
     * 配置项变更集合
     * <p>
     * 存储所有发生变更的配置项及其变更前后的值。
     * 
     * <p><b>Web 线程池常见配置项：</b>
     * <ul>
     *   <li>"corePoolSize" - 核心线程数</li>
     *   <li>"maximumPoolSize" - 最大线程数</li>
     *   <li>"keepAliveTime" - 线程存活时间</li>
     * </ul>
     * 
     * <p><b>注意：</b>Web 容器线程池的配置项较少，通常不包括队列和拒绝策略配置。
     */
    private Map<String, ChangePair<?>> changes;

    /**
     * 配置变更时间
     * <p>
     * 配置实际生效的时间。
     * 
     * <p><b>示例：</b>"2025-04-30 15:30:45"
     */
    private String updateTime;

    /**
     * 配置项变更前后值对（内嵌类）
     * <p>
     * 该类封装了某个配置项变更前后的值。
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * // 核心线程数变更：10 → 20
     * ChangePair<Integer> change = new ChangePair<>(10, 20);
     * 
     * System.out.println(change.getBefore());  // 10
     * System.out.println(change.getAfter());   // 20
     * }</pre>
     * 
     * @param <T> 配置值的类型
     */
    @Data
    @AllArgsConstructor
    public static class ChangePair<T> {
        
        /**
         * 变更前的旧值
         */
        private T before;
        
        /**
         * 变更后的新值
         */
        private T after;
    }
}
