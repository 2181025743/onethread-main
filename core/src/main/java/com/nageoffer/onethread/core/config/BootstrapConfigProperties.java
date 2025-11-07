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

import com.nageoffer.onethread.core.executor.ThreadPoolExecutorProperties;
import com.nageoffer.onethread.core.parser.ConfigFileTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * oneThread 框架启动配置属性类
 * <p>
 * 该类是 oneThread 框架的核心配置载体，包含了框架运行所需的所有配置信息，
 * 包括配置中心连接配置、线程池配置、监控配置、告警配置等。
 * 
 * <p><b>核心作用：</b>
 * <ul>
 *   <li><b>配置承载：</b>承载从配置文件或配置中心加载的所有配置</li>
 *   <li><b>配置绑定：</b>通过 Spring Boot 的 Binder 机制从配置文件绑定</li>
 *   <li><b>全局访问：</b>使用单例模式提供全局配置访问</li>
 *   <li><b>配置传递：</b>在配置刷新流程中传递配置数据</li>
 * </ul>
 * 
 * <p><b>配置层次结构：</b>
 * <pre>
 * onethread:                              ← BootstrapConfigProperties
 *   enable: true                          ← 是否启用框架
 *   config-file-type: yaml                ← 配置文件格式
 *   
 *   nacos:                                ← NacosConfig
 *     data-id: app.yaml
 *     group: DEFAULT_GROUP
 *   
 *   web:                                  ← WebThreadPoolExecutorConfig
 *     core-pool-size: 10
 *     maximum-pool-size: 200
 *   
 *   monitor:                              ← MonitorConfig
 *     enable: true
 *     collect-type: micrometer
 *     collect-interval: 10
 *   
 *   notify-platforms:                     ← NotifyPlatformsConfig
 *     platform: DING
 *     url: https://oapi.dingtalk.com/...
 *   
 *   executors:                            ← List<ThreadPoolExecutorProperties>
 *     - thread-pool-id: pool1
 *       core-pool-size: 10
 *       ...
 * </pre>
 * 
 * <p><b>配置加载流程：</b>
 * <pre>
 * 1. 应用启动
 *    ↓
 * 2. 从 bootstrap.yml 读取基础配置
 *    ↓
 * 3. 连接配置中心（Nacos/Apollo）
 *    ↓
 * 4. 拉取远程配置文件
 *    ↓
 * 5. 解析配置（YAML/Properties）
 *    ↓
 * 6. 绑定到 BootstrapConfigProperties
 *    ↓
 * 7. 注册配置监听器
 *    ↓
 * 8. 配置变更时重新绑定
 * </pre>
 * 
 * <p><b>单例模式：</b>
 * <ul>
 *   <li>使用静态字段 {@link #INSTANCE} 保存唯一实例</li>
 *   <li>通过 {@link #getInstance()} 获取实例</li>
 *   <li>通过 {@link #setInstance(BootstrapConfigProperties)} 更新实例</li>
 * </ul>
 * 
 * <p><b>使用示例：</b>
 * <pre>{@code
 * // 示例1：获取配置
 * BootstrapConfigProperties config = BootstrapConfigProperties.getInstance();
 * 
 * // 获取线程池配置列表
 * List<ThreadPoolExecutorProperties> executors = config.getExecutors();
 * 
 * // 获取监控配置
 * MonitorConfig monitor = config.getMonitor();
 * if (monitor.getEnable()) {
 *     startMonitor(monitor.getCollectInterval());
 * }
 * 
 * 
 * // 示例2：配置变更时更新实例
 * public void onConfigChange(String newConfigText) {
 *     // 解析新配置
 *     Map<Object, Object> configMap = yamlParser.parse(newConfigText);
 *     Binder binder = new Binder(new MapConfigurationPropertySource(configMap));
 *     BootstrapConfigProperties newConfig = 
 *         binder.bind("onethread", Bindable.of(BootstrapConfigProperties.class)).get();
 *     
 *     // 更新全局实例
 *     BootstrapConfigProperties.setInstance(newConfig);
 *     
 *     // 发布配置变更事件
 *     publishEvent(new ThreadPoolConfigUpdateEvent(this, newConfig));
 * }
 * 
 * 
 * // 示例3：判断是否启用框架
 * if (BootstrapConfigProperties.getInstance().getEnable()) {
 *     // 启用 oneThread 框架
 *     initializeFramework();
 * }
 * }</pre>
 * 
 * <p><b>线程安全性：</b>
 * <ul>
 *   <li>getInstance() 和 setInstance() 不是线程安全的</li>
 *   <li>但在实际使用中，设置操作只在启动时和配置变更时发生，频率很低</li>
 *   <li>读取操作是线程安全的（静态字段的读取）</li>
 * </ul>
 * 
 * <p><b>最佳实践：</b>
 * <ul>
 *   <li>使用配置中心管理配置，避免硬编码</li>
 *   <li>为所有配置提供合理的默认值</li>
 *   <li>配置变更后通过事件机制通知相关组件</li>
 * </ul>
 * 
 * @author 杨潇
 * @since 2025-04-23
 * @see ThreadPoolExecutorProperties 线程池配置属性
 * @see ConfigFileTypeEnum 配置文件类型枚举
 */
@Data
public class BootstrapConfigProperties {

    /**
     * 配置前缀常量
     * <p>
     * 所有 oneThread 框架的配置项都以 "onethread" 作为前缀。
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * // 绑定配置时使用
     * Binder binder = new Binder(sources);
     * BootstrapConfigProperties config = 
     *     binder.bind(BootstrapConfigProperties.PREFIX, ...).get();
     * }</pre>
     */
    public static final String PREFIX = "onethread";

    /**
     * 是否启用 oneThread 框架
     * <p>
     * 全局开关，控制是否启用动态线程池功能。
     * 
     * <p><b>配置示例：</b>{@code onethread.enable: true}
     * 
     * <p><b>默认值：</b>true（默认启用）
     * 
     * <p><b>作用：</b>
     * <ul>
     *   <li>false：完全禁用 oneThread 框架，线程池退化为标准 ThreadPoolExecutor</li>
     *   <li>true：启用动态线程池、监控、告警等所有功能</li>
     * </ul>
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>临时禁用框架功能进行问题排查</li>
     *   <li>灰度发布时逐步启用</li>
     *   <li>特定环境下禁用（如测试环境）</li>
     * </ul>
     */
    private Boolean enable = Boolean.TRUE;

    /**
     * Nacos 配置中心配置
     * <p>
     * 当使用 Nacos 作为配置中心时，需要配置此项。
     * 
     * <p><b>配置示例：</b>
     * <pre>
     * onethread:
     *   nacos:
     *     data-id: onethread-app.yaml
     *     group: DEFAULT_GROUP
     * </pre>
     * 
     * <p><b>注意：</b>
     * <ul>
     *   <li>Nacos 服务器地址等配置在 Spring Cloud Nacos 配置中设置</li>
     *   <li>data-id 通常为 ${spring.application.name}.yaml</li>
     * </ul>
     * 
     * @see NacosConfig Nacos配置内嵌类
     */
    private NacosConfig nacos;

    /**
     * Apollo 配置中心配置
     * <p>
     * 当使用 Apollo 作为配置中心时，需要配置此项。
     * 
     * <p><b>配置示例：</b>
     * <pre>
     * onethread:
     *   apollo:
     *     namespace: application
     * </pre>
     * 
     * @see ApolloConfig Apollo配置内嵌类
     */
    private ApolloConfig apollo;

    /**
     * Web 容器线程池配置
     * <p>
     * 配置 Web 容器（Tomcat、Jetty、Undertow）内嵌线程池的参数。
     * 
     * <p><b>配置示例：</b>
     * <pre>
     * onethread:
     *   web:
     *     core-pool-size: 10
     *     maximum-pool-size: 200
     *     keep-alive-time: 60
     * </pre>
     * 
     * <p><b>作用：</b>
     * <ul>
     *   <li>动态调整 Tomcat/Jetty 的连接线程池</li>
     *   <li>优化 Web 容器的并发处理能力</li>
     * </ul>
     * 
     * @see WebThreadPoolExecutorConfig Web线程池配置内嵌类
     */
    private WebThreadPoolExecutorConfig web;

    /**
     * 配置文件格式类型
     * <p>
     * 指定配置中心的配置文件格式（YAML 或 Properties）。
     * 
     * <p><b>配置示例：</b>{@code config-file-type: yaml}
     * 
     * <p><b>可选值：</b>
     * <ul>
     *   <li>YAML - 推荐使用</li>
     *   <li>YML - 与 YAML 相同</li>
     *   <li>PROPERTIES - 传统格式</li>
     * </ul>
     * 
     * @see ConfigFileTypeEnum 配置文件类型枚举
     */
    private ConfigFileTypeEnum configFileType;

    /**
     * 通知平台配置
     * <p>
     * 配置告警通知的平台信息（如钉钉机器人）。
     * 
     * <p><b>配置示例：</b>
     * <pre>
     * onethread:
     *   notify-platforms:
     *     platform: DING
     *     url: https://oapi.dingtalk.com/robot/send?access_token=xxx
     * </pre>
     * 
     * @see NotifyPlatformsConfig 通知平台配置内嵌类
     */
    private NotifyPlatformsConfig notifyPlatforms;

    /**
     * 监控配置
     * <p>
     * 配置线程池监控的行为，包括是否启用监控、采集方式、采集间隔等。
     * 
     * <p><b>配置示例：</b>
     * <pre>
     * onethread:
     *   monitor:
     *     enable: true
     *     collect-type: micrometer
     *     collect-interval: 10
     * </pre>
     * 
     * <p><b>默认值：</b>启用监控，使用 micrometer 方式，每10秒采集一次
     * 
     * @see MonitorConfig 监控配置内嵌类
     */
    private MonitorConfig monitor = new MonitorConfig();

    /**
     * 线程池配置集合
     * <p>
     * 配置所有动态线程池的参数列表，每个元素代表一个线程池的完整配置。
     * 
     * <p><b>配置示例：</b>
     * <pre>
     * onethread:
     *   executors:
     *     - thread-pool-id: order-processor
     *       core-pool-size: 10
     *       maximum-pool-size: 20
     *       queue-capacity: 100
     *       alarm:
     *         enable: true
     *         queue-threshold: 80
     *     
     *     - thread-pool-id: message-consumer
     *       core-pool-size: 5
     *       maximum-pool-size: 10
     *       ...
     * </pre>
     * 
     * @see ThreadPoolExecutorProperties 线程池配置属性
     */
    private List<ThreadPoolExecutorProperties> executors;

    /**
     * 通知平台配置内嵌类
     * <p>
     * 封装了告警通知平台的配置信息，如钉钉机器人的 WebHook 地址。
     * 
     * <p><b>支持的平台：</b>
     * <ul>
     *   <li><b>DING：</b>钉钉机器人</li>
     *   <li><b>EMAIL：</b>邮件通知（扩展）</li>
     *   <li><b>WECHAT：</b>企业微信（扩展）</li>
     * </ul>
     * 
     * <p><b>配置示例：</b>
     * <pre>
     * notify-platforms:
     *   platform: DING
     *   url: https://oapi.dingtalk.com/robot/send?access_token=xxx
     * </pre>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * NotifyPlatformsConfig notifyConfig = config.getNotifyPlatforms();
     * String platform = notifyConfig.getPlatform();    // "DING"
     * String webhookUrl = notifyConfig.getUrl();       // 钉钉机器人URL
     * 
     * // 发送通知
     * if ("DING".equals(platform)) {
     *     dingTalkService.send(webhookUrl, message);
     * }
     * }</pre>
     */
    @Data
    public static class NotifyPlatformsConfig {

        /**
         * 通知平台类型
         * <p>
         * 指定使用哪种通知平台发送告警消息。
         * 
         * <p><b>配置示例：</b>{@code platform: DING}
         * 
         * <p><b>可选值：</b>
         * <ul>
         *   <li>"DING" - 钉钉机器人（当前支持）</li>
         *   <li>"EMAIL" - 邮件通知（扩展）</li>
         *   <li>"WECHAT" - 企业微信（扩展）</li>
         * </ul>
         */
        private String platform;

        /**
         * 通知平台的完整 WebHook 地址
         * <p>
         * 通知平台的回调地址，用于发送告警消息。
         * 
         * <p><b>钉钉机器人 URL 格式：</b>
         * <pre>
         * https://oapi.dingtalk.com/robot/send?access_token=xxxxxx
         * </pre>
         * 
         * <p><b>获取方式：</b>
         * <ul>
         *   <li>在钉钉群中添加自定义机器人</li>
         *   <li>安全设置选择"自定义关键词"或"加签"</li>
         *   <li>复制生成的 WebHook 地址</li>
         * </ul>
         * 
         * <p><b>注意：</b>
         * <ul>
         *   <li>URL 中包含敏感的 access_token，需要妥善保管</li>
         *   <li>建议使用环境变量或加密存储</li>
         *   <li>定期更新 token，提高安全性</li>
         * </ul>
         */
        private String url;
    }

    /**
     * 监控配置内嵌类
     * <p>
     * 封装了线程池监控的配置信息，包括是否启用监控、采集方式、采集间隔等。
     * 
     * <p><b>配置示例：</b>
     * <pre>
     * monitor:
     *   enable: true              # 是否启用监控
     *   collect-type: micrometer  # 采集方式
     *   collect-interval: 10      # 采集间隔（秒）
     * </pre>
     * 
     * <p><b>采集方式对比：</b>
     * <table border="1" cellpadding="5">
     *   <tr><th>方式</th><th>说明</th><th>优点</th><th>缺点</th></tr>
     *   <tr>
     *     <td>log</td>
     *     <td>输出到日志文件</td>
     *     <td>简单，无依赖</td>
     *     <td>不易分析</td>
     *   </tr>
     *   <tr>
     *     <td>micrometer</td>
     *     <td>通过 Micrometer 暴露指标</td>
     *     <td>标准化，易集成</td>
     *     <td>需要依赖</td>
     *   </tr>
     * </table>
     */
    @Data
    public static class MonitorConfig {

        /**
         * 是否启用监控
         * <p>
         * 控制是否启动监控采集任务。
         * 
         * <p><b>配置示例：</b>{@code enable: true}
         * 
         * <p><b>默认值：</b>true（默认启用）
         * 
         * <p><b>作用：</b>
         * <ul>
         *   <li>true：启动监控定时任务，定期采集线程池状态</li>
         *   <li>false：禁用监控，不采集数据</li>
         * </ul>
         */
        private Boolean enable = Boolean.TRUE;

        /**
         * 监控数据采集方式
         * <p>
         * 指定如何采集和输出监控数据。
         * 
         * <p><b>配置示例：</b>{@code collect-type: micrometer}
         * 
         * <p><b>默认值：</b>"micrometer"
         * 
         * <p><b>可选值：</b>
         * <ul>
         *   <li><b>"log"：</b>输出到日志文件（JSON格式），简单但不易分析</li>
         *   <li><b>"micrometer"：</b>通过 Micrometer 暴露指标，支持 Prometheus、Grafana 等</li>
         * </ul>
         * 
         * <p><b>推荐：</b>生产环境使用 "micrometer"，配合 Prometheus + Grafana 实现可视化监控。
         */
        private String collectType = "micrometer";

        /**
         * 监控数据采集间隔（单位：秒）
         * <p>
         * 定时任务的执行间隔，决定了监控数据的更新频率。
         * 
         * <p><b>配置示例：</b>{@code collect-interval: 10}
         * 
         * <p><b>默认值：</b>10秒
         * 
         * <p><b>配置建议：</b>
         * <ul>
         *   <li><b>5~10秒：</b>适合大多数场景，平衡实时性和性能</li>
         *   <li><b>1~5秒：</b>需要高实时性的场景（注意性能开销）</li>
         *   <li><b>30~60秒：</b>对实时性要求不高的场景</li>
         * </ul>
         * 
         * <p><b>性能考虑：</b>
         * <ul>
         *   <li>间隔越短，监控数据越实时，但性能开销越大</li>
         *   <li>采集操作涉及线程池 API 调用（部分有锁）</li>
         *   <li>建议根据实际需要平衡实时性和性能</li>
         * </ul>
         */
        private Long collectInterval = 10L;
    }

    /**
     * Nacos 配置中心配置内嵌类
     * <p>
     * 封装了 Nacos 配置中心的连接信息。
     * 
     * <p><b>配置示例：</b>
     * <pre>
     * nacos:
     *   data-id: onethread-order-service.yaml
     *   group: DEFAULT_GROUP
     * </pre>
     * 
     * <p><b>配置说明：</b>
     * <ul>
     *   <li><b>data-id：</b>配置文件的唯一标识，通常为 ${spring.application.name}.yaml</li>
     *   <li><b>group：</b>配置分组，用于隔离不同的配置集</li>
     * </ul>
     */
    @Data
    public static class NacosConfig {

        /**
         * Nacos 配置文件的 Data ID
         * <p>
         * 配置文件在 Nacos 中的唯一标识。
         * 
         * <p><b>命名建议：</b>
         * <ul>
         *   <li>使用应用名作为前缀：onethread-${spring.application.name}</li>
         *   <li>包含文件扩展名：.yaml 或 .properties</li>
         *   <li>示例："onethread-order-service.yaml"</li>
         * </ul>
         */
        private String dataId;

        /**
         * Nacos 配置分组
         * <p>
         * 配置文件所属的分组，用于逻辑隔离。
         * 
         * <p><b>常用值：</b>
         * <ul>
         *   <li>"DEFAULT_GROUP" - 默认分组</li>
         *   <li>自定义分组名 - 如 "ORDER_GROUP"、"PAYMENT_GROUP"</li>
         * </ul>
         */
        private String group;
    }

    /**
     * Apollo 配置中心配置内嵌类
     * <p>
     * 封装了 Apollo 配置中心的连接信息。
     * 
     * <p><b>配置示例：</b>
     * <pre>
     * apollo:
     *   namespace: application
     * </pre>
     */
    @Data
    public static class ApolloConfig {

        /**
         * Apollo 命名空间
         * <p>
         * 指定从哪个命名空间读取配置。
         * 
         * <p><b>常用值：</b>
         * <ul>
         *   <li>"application" - 默认命名空间</li>
         *   <li>自定义命名空间 - 如 "onethread"</li>
         * </ul>
         */
        private String namespace;
    }

    /**
     * Web 容器线程池配置内嵌类
     * <p>
     * 封装了 Web 容器（Tomcat、Jetty 等）线程池的配置参数。
     * 
     * <p><b>配置示例：</b>
     * <pre>
     * web:
     *   core-pool-size: 10
     *   maximum-pool-size: 200
     *   keep-alive-time: 60
     *   notify:
     *     receives: "张三"
     * </pre>
     * 
     * <p><b>作用：</b>
     * <ul>
     *   <li>动态调整 Tomcat 的 Connector 线程池</li>
     *   <li>优化 Web 容器的并发能力</li>
     * </ul>
     */
    @Data
    public static class WebThreadPoolExecutorConfig {

        /**
         * 核心线程数
         * <p>
         * Web 容器线程池的核心线程数。
         * 
         * <p><b>配置建议：</b>10~50（根据并发量）
         */
        private Integer corePoolSize;

        /**
         * 最大线程数
         * <p>
         * Web 容器线程池的最大线程数，决定了最大并发连接数。
         * 
         * <p><b>配置建议：</b>
         * <ul>
         *   <li>小型应用：200</li>
         *   <li>中型应用：500</li>
         *   <li>大型应用：1000+</li>
         * </ul>
         */
        private Integer maximumPoolSize;

        /**
         * 线程空闲存活时间（单位：秒）
         * <p>
         * 超过核心线程数的空闲线程的存活时间。
         * 
         * <p><b>配置建议：</b>60秒
         */
        private Long keepAliveTime;

        /**
         * 通知配置
         * <p>
         * Web 线程池配置变更时的通知配置。
         */
        private NotifyConfig notify;
    }

    /**
     * 通知配置内嵌类
     * <p>
     * 封装了通知接收人信息。
     * 
     * <p><b>配置示例：</b>
     * <pre>
     * notify:
     *   receives: "张三,李四"
     * </pre>
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotifyConfig {

        /**
         * 接收人集合
         * <p>
         * 配置变更通知的接收人列表，多个接收人用逗号分隔。
         * 
         * <p><b>格式：</b>"接收人1,接收人2,接收人3"
         * 
         * <p><b>示例：</b>"张三,李四"、"13800138000,13900139000"
         */
        private String receives;
    }

    /**
     * 全局单例实例
     * <p>
     * 保存 BootstrapConfigProperties 的全局唯一实例。
     * 
     * <p><b>初始值：</b>空的 BootstrapConfigProperties 对象
     * 
     * <p><b>更新时机：</b>
     * <ul>
     *   <li>应用启动时，从配置文件加载后设置</li>
     *   <li>配置变更时，解析新配置后更新</li>
     * </ul>
     */
    private static BootstrapConfigProperties INSTANCE = new BootstrapConfigProperties();

    /**
     * 获取全局配置实例
     * <p>
     * 返回全局唯一的配置实例，供整个应用使用。
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * BootstrapConfigProperties config = BootstrapConfigProperties.getInstance();
     * List<ThreadPoolExecutorProperties> executors = config.getExecutors();
     * }</pre>
     *
     * @return BootstrapConfigProperties 的全局实例
     */
    public static BootstrapConfigProperties getInstance() {
        return INSTANCE;
    }

    /**
     * 设置全局配置实例
     * <p>
     * 更新全局配置实例，通常在配置变更时调用。
     * 
     * <p><b>调用时机：</b>
     * <ul>
     *   <li>应用启动时，首次加载配置后设置</li>
     *   <li>配置中心推送配置变更时，解析新配置后更新</li>
     * </ul>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * // 解析新配置
     * BootstrapConfigProperties newConfig = parseConfig(configText);
     * 
     * // 更新全局实例
     * BootstrapConfigProperties.setInstance(newConfig);
     * 
     * // 其他组件通过 getInstance() 会获取到最新配置
     * }</pre>
     * 
     * <p><b>注意：</b>该方法不是线程安全的，但由于调用频率很低，实际不会有问题。
     *
     * @param properties 新的配置实例
     */
    public static void setInstance(BootstrapConfigProperties properties) {
        INSTANCE = properties;
    }
}
