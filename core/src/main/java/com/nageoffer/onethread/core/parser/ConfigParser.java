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
import java.util.List;
import java.util.Map;

/**
 * 配置解析器接口
 * <p>
 * 该接口定义了配置文件解析的统一规范，用于将不同格式的配置文件（YAML、Properties等）
 * 解析为统一的键值对结构，方便后续的配置绑定和参数提取。
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>定义配置文件解析的标准方法</li>
 *   <li>支持多种配置文件格式的扩展</li>
 *   <li>提供格式支持判断能力</li>
 * </ul>
 * 
 * <p><b>设计模式：</b>策略模式（Strategy Pattern）
 * <br>不同的实现类代表不同的解析策略（如 YamlConfigParser、PropertiesConfigParser）
 * 
 * <p><b>使用示例：</b>
 * <pre>{@code
 * ConfigParser parser = new YamlConfigParser();
 * if (parser.supports(ConfigFileTypeEnum.YAML)) {
 *     Map<Object, Object> config = parser.doParse(yamlContent);
 * }
 * }</pre>
 * 
 * @author 杨潇
 * @since 2025-04-23
 * @see YamlConfigParser YAML格式解析器实现
 * @see PropertiesConfigParser Properties格式解析器实现
 * @see ConfigParserHandler 解析器管理器
 */
public interface ConfigParser {

    /**
     * 判断当前解析器是否支持指定类型的配置文件解析
     * <p>
     * 该方法用于运行时判断解析器能力，支持解析器的动态选择。
     * 通常在 {@link ConfigParserHandler} 中用于选择合适的解析器。
     *
     * @param type 配置文件类型枚举（如 YAML、PROPERTIES）
     * @return {@code true} 表示支持该类型，{@code false} 表示不支持
     * @see ConfigFileTypeEnum 配置文件类型枚举
     */
    boolean supports(ConfigFileTypeEnum type);

    /**
     * 解析配置内容字符串为键值对 Map
     * <p>
     * 将配置文件的文本内容解析为扁平化的键值对结构，支持嵌套属性的展开。
     * 
     * <p><b>解析示例：</b>
     * <pre>
     * 输入 YAML：
     *   onethread:
     *     executors:
     *       - thread-pool-id: pool1
     *         core-pool-size: 10
     * 
     * 输出 Map：
     *   {
     *     "onethread.executors[0].thread-pool-id": "pool1",
     *     "onethread.executors[0].core-pool-size": 10
     *   }
     * </pre>
     *
     * @param content 配置文件的完整文本内容（如从 Nacos 拉取的 YAML 文本）
     * @return 解析后的键值对 Map，键为扁平化的属性路径，值为配置值
     * @throws IOException 当配置内容格式错误或解析失败时抛出
     */
    Map<Object, Object> doParse(String content) throws IOException;

    /**
     * 获取当前解析器支持的配置文件类型列表
     * <p>
     * 返回该解析器能够处理的所有配置文件格式。
     * 一个解析器可以支持多种文件类型（例如 YAML 解析器可能同时支持 .yml 和 .yaml）。
     *
     * @return 支持的配置文件类型集合，不会为 {@code null}
     * @see ConfigFileTypeEnum 配置文件类型枚举
     */
    List<ConfigFileTypeEnum> getConfigFileTypes();
}
