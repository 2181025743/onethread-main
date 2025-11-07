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

package com.nageoffer.onethread.core.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 配置解析器管理器（处理器）
 * <p>
 * 该类负责管理所有的配置解析器实例，根据配置文件类型自动选择合适的解析器。
 * 采用<b>单例模式</b>确保全局只有一个管理器实例，提高性能并简化使用。
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>注册和管理所有的配置解析器（YAML、Properties等）</li>
 *   <li>根据配置文件类型自动选择合适的解析器</li>
 *   <li>提供统一的配置解析入口</li>
 * </ul>
 * 
 * <p><b>设计模式：</b>
 * <ul>
 *   <li><b>单例模式（Singleton Pattern）：</b>使用静态内部类实现懒加载单例</li>
 *   <li><b>策略模式（Strategy Pattern）：</b>根据类型选择不同的解析策略</li>
 *   <li><b>外观模式（Facade Pattern）：</b>为多个解析器提供统一的访问接口</li>
 * </ul>
 * 
 * <p><b>线程安全性：</b>该类是线程安全的，使用静态内部类实现的单例模式保证了线程安全。
 * 
 * <p><b>使用示例：</b>
 * <pre>{@code
 * // 获取单例实例
 * ConfigParserHandler handler = ConfigParserHandler.getInstance();
 * 
 * // 解析 YAML 配置
 * String yamlContent = "onethread:\n  config-file-type: yaml";
 * Map<Object, Object> config = handler.parseConfig(yamlContent, ConfigFileTypeEnum.YAML);
 * 
 * // 解析 Properties 配置
 * String propsContent = "onethread.config-file-type=properties";
 * Map<Object, Object> config2 = handler.parseConfig(propsContent, ConfigFileTypeEnum.PROPERTIES);
 * }</pre>
 * 
 * @author 杨潇
 * @since 2025-04-24
 * @see ConfigParser 配置解析器接口
 * @see YamlConfigParser YAML解析器
 * @see PropertiesConfigParser Properties解析器
 */
public final class ConfigParserHandler {

    /**
     * 配置解析器列表
     * <p>
     * 存储所有已注册的解析器实例，在构造函数中初始化。
     * 解析时会遍历该列表，找到第一个支持目标类型的解析器。
     * 
     * <p><b>注册顺序：</b>
     * <ol>
     *   <li>YamlConfigParser - YAML格式解析器</li>
     *   <li>PropertiesConfigParser - Properties格式解析器</li>
     * </ol>
     */
    private static final List<ConfigParser> PARSERS = new ArrayList<>();

    /**
     * 私有构造函数，防止外部实例化
     * <p>
     * 在构造时注册所有支持的配置解析器。
     * 使用私有构造函数是单例模式的标准实现方式。
     * 
     * <p><b>初始化流程：</b>
     * <ol>
     *   <li>创建 YamlConfigParser 实例并添加到解析器列表</li>
     *   <li>创建 PropertiesConfigParser 实例并添加到解析器列表</li>
     * </ol>
     * 
     * <p><b>扩展方式：</b>如果需要支持新的配置格式，只需：
     * <ol>
     *   <li>实现 {@link ConfigParser} 接口</li>
     *   <li>在此构造函数中添加新的解析器实例</li>
     * </ol>
     */
    private ConfigParserHandler() {
        PARSERS.add(new YamlConfigParser());
        PARSERS.add(new PropertiesConfigParser());
    }

    /**
     * 解析配置内容
     * <p>
     * 根据指定的配置文件类型，自动选择合适的解析器进行解析。
     * 这是整个配置解析流程的统一入口。
     * 
     * <p><b>执行流程：</b>
     * <ol>
     *   <li>遍历已注册的解析器列表（{@link #PARSERS}）</li>
     *   <li>调用每个解析器的 {@link ConfigParser#supports(ConfigFileTypeEnum)} 方法判断是否支持目标类型</li>
     *   <li>找到第一个支持的解析器后，调用其 {@link ConfigParser#doParse(String)} 方法执行解析</li>
     *   <li>如果没有找到支持的解析器，返回空 Map</li>
     * </ol>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * String yamlContent = """
     *     onethread:
     *       executors:
     *         - thread-pool-id: pool1
     *           core-pool-size: 10
     *     """;
     * Map<Object, Object> config = parseConfig(yamlContent, ConfigFileTypeEnum.YAML);
     * }</pre>
     *
     * @param content 配置文件的完整文本内容（如从 Nacos 拉取的配置）
     * @param type    配置文件类型枚举（YAML、PROPERTIES等）
     * @return 解析后的键值对 Map，如果没有找到支持的解析器则返回空 Map
     * @throws IOException 当配置内容格式错误或解析失败时抛出
     */
    public Map<Object, Object> parseConfig(String content, ConfigFileTypeEnum type) throws IOException {
        // 遍历所有已注册的解析器
        for (ConfigParser parser : PARSERS) {
            // 找到第一个支持目标类型的解析器
            if (parser.supports(type)) {
                return parser.doParse(content);
            }
        }
        // 如果没有找到支持的解析器，返回空 Map（避免返回 null）
        return Collections.emptyMap();
    }

    /**
     * 获取 ConfigParserHandler 的单例实例
     * <p>
     * 使用静态内部类实现的懒加载单例模式，线程安全且延迟初始化。
     * 
     * <p><b>单例实现原理：</b>
     * <ul>
     *   <li>JVM 保证类的静态变量只会被初始化一次</li>
     *   <li>静态内部类只有在被调用时才会加载</li>
     *   <li>类加载过程由 JVM 保证线程安全</li>
     * </ul>
     * 
     * <p>这种方式比传统的双重检查锁（DCL）更简洁且性能更好。
     *
     * @return ConfigParserHandler 的全局唯一实例
     */
    public static ConfigParserHandler getInstance() {
        return ConfigParserHandlerHolder.INSTANCE;
    }

    /**
     * 静态内部类，用于实现单例模式的懒加载
     * <p>
     * 该类只有在 {@link #getInstance()} 方法被调用时才会被 JVM 加载，
     * 从而实现了延迟初始化。JVM 的类加载机制保证了线程安全。
     * 
     * <p><b>设计优势：</b>
     * <ul>
     *   <li><b>延迟加载：</b>只有在需要时才创建实例</li>
     *   <li><b>线程安全：</b>无需使用 synchronized，由 JVM 保证</li>
     *   <li><b>高性能：</b>避免了同步带来的性能开销</li>
     * </ul>
     */
    private static class ConfigParserHandlerHolder {

        /**
         * ConfigParserHandler 的唯一实例
         * <p>
         * 该实例在 ConfigParserHandlerHolder 类被加载时创建，
         * JVM 保证了静态变量的初始化是线程安全的。
         */
        private static final ConfigParserHandler INSTANCE = new ConfigParserHandler();
    }
}
