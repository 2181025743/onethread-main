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

import com.nageoffer.onethread.core.config.BootstrapConfigProperties;
import lombok.Getter;

/**
 * 配置文件类型枚举
 * <p>
 * 定义了 oneThread 框架支持的所有配置文件格式类型。
 * 该枚举用于配置解析器的类型判断和自动选择，确保不同格式的配置文件
 * 能够被正确的解析器处理。
 * 
 * <p><b>支持的配置格式：</b>
 * <ul>
 *   <li><b>YAML：</b>推荐使用的配置格式，支持层次结构和复杂数据类型</li>
 *   <li><b>YML：</b>YAML的另一种文件扩展名，与YAML功能完全相同</li>
 *   <li><b>PROPERTIES：</b>传统的Java配置格式，简单但不支持复杂结构</li>
 * </ul>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>在 {@link BootstrapConfigProperties} 中配置 {@code config-file-type}</li>
 *   <li>在 {@link ConfigParser} 中判断解析器是否支持特定类型</li>
 *   <li>在 {@link ConfigParserHandler} 中选择合适的解析器</li>
 * </ul>
 * 
 * <p><b>使用示例：</b>
 * <pre>{@code
 * // 通过字符串值获取枚举
 * ConfigFileTypeEnum type = ConfigFileTypeEnum.of("yaml");
 * 
 * // 判断类型
 * if (type == ConfigFileTypeEnum.YAML) {
 *     // 处理YAML格式配置
 * }
 * }</pre>
 * 
 * @author 杨潇
 * @since 2025-04-23
 * @see ConfigParser 配置解析器接口
 * @see ConfigParserHandler 解析器管理器
 */
@Getter
public enum ConfigFileTypeEnum {

    /**
     * Properties 配置文件格式
     * <p>
     * 传统的 Java 配置文件格式，使用键值对表示配置。
     * 
     * <p><b>格式示例：</b>
     * <pre>
     * onethread.config-file-type=properties
     * onethread.executors[0].thread-pool-id=pool1
     * onethread.executors[0].core-pool-size=10
     * </pre>
     * 
     * <p><b>特点：</b>
     * <ul>
     *   <li>简单易读，适合简单配置</li>
     *   <li>不支持层次结构和复杂数据类型</li>
     *   <li>数组表示较为复杂（需要使用索引）</li>
     * </ul>
     */
    PROPERTIES("properties"),

    /**
     * YML 配置文件格式（YAML的别名）
     * <p>
     * 与 {@link #YAML} 完全相同，仅文件扩展名不同（.yml vs .yaml）。
     * 某些项目习惯使用 .yml 作为扩展名，框架同时支持这两种形式。
     * 
     * <p><b>文件扩展名：</b>.yml
     */
    YML("yml"),

    /**
     * YAML 配置文件格式（推荐）
     * <p>
     * YAML（YAML Ain't Markup Language）是一种人性化的数据序列化格式，
     * 广泛用于现代配置管理。
     * 
     * <p><b>格式示例：</b>
     * <pre>
     * onethread:
     *   config-file-type: yaml
     *   executors:
     *     - thread-pool-id: pool1
     *       core-pool-size: 10
     *       maximum-pool-size: 20
     * </pre>
     * 
     * <p><b>特点：</b>
     * <ul>
     *   <li>支持层次结构，配置清晰直观</li>
     *   <li>支持数组、对象等复杂数据类型</li>
     *   <li>使用缩进表示层级关系</li>
     *   <li>与 Spring Boot 配置完美集成</li>
     * </ul>
     * 
     * <p><b>文件扩展名：</b>.yaml
     */
    YAML("yaml");

    /**
     * 配置文件类型的字符串值
     * <p>
     * 用于配置文件中指定配置格式，如：
     * <pre>
     * onethread:
     *   config-file-type: yaml  # 这里的 "yaml" 就是 value 字段的值
     * </pre>
     */
    private final String value;

    /**
     * 构造函数
     * 
     * @param value 配置文件类型的字符串值
     */
    ConfigFileTypeEnum(String value) {
        this.value = value;
    }

    /**
     * 根据字符串值获取对应的枚举实例
     * <p>
     * 该方法用于将配置文件中的字符串类型值转换为枚举实例。
     * 如果找不到匹配的枚举，默认返回 {@link #PROPERTIES}。
     * 
     * <p><b>查找逻辑：</b>
     * <ol>
     *   <li>遍历所有枚举值</li>
     *   <li>比较每个枚举的 {@code value} 字段与输入值</li>
     *   <li>找到匹配项立即返回</li>
     *   <li>未找到则返回默认值 PROPERTIES</li>
     * </ol>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * // 从配置中读取类型字符串
     * String typeValue = properties.getProperty("onethread.config-file-type");
     * 
     * // 转换为枚举
     * ConfigFileTypeEnum type = ConfigFileTypeEnum.of(typeValue);
     * 
     * // 根据类型选择解析器
     * ConfigParser parser = getParserForType(type);
     * }</pre>
     *
     * @param value 配置文件类型的字符串值（如 "yaml"、"properties"）
     * @return 对应的枚举实例，如果未找到则返回 {@link #PROPERTIES}
     */
    public static ConfigFileTypeEnum of(String value) {
        // 遍历所有枚举值查找匹配项
        for (ConfigFileTypeEnum typeEnum : ConfigFileTypeEnum.values()) {
            if (typeEnum.value.equals(value)) {
                return typeEnum;
            }
        }
        // 默认返回 PROPERTIES 类型（向后兼容）
        return PROPERTIES;
    }
}
