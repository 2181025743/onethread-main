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

import cn.hutool.core.collection.CollectionUtil;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Properties 配置文件解析器
 * <p>
 * 该类负责解析传统的 Java Properties 格式配置文件，将键值对形式的配置文本
 * 转换为 Map 结构，供后续的配置绑定使用。
 * 
 * <p><b>支持的配置格式：</b>
 * <pre>
 * # Properties 格式示例
 * onethread.config-file-type=properties
 * onethread.executors[0].thread-pool-id=business-pool
 * onethread.executors[0].core-pool-size=10
 * onethread.executors[0].maximum-pool-size=20
 * </pre>
 * 
 * <p><b>格式特点：</b>
 * <ul>
 *   <li>使用 {@code key=value} 格式表示配置项</li>
 *   <li>使用 {@code #} 或 {@code !} 开头表示注释</li>
 *   <li>数组使用索引表示，如 {@code executors[0]}</li>
 *   <li>嵌套属性使用点号分隔，如 {@code onethread.executors[0].core-pool-size}</li>
 * </ul>
 * 
 * <p><b>优势：</b>
 * <ul>
 *   <li>简单直观，易于理解</li>
 *   <li>与传统Java配置完全兼容</li>
 *   <li>文件体积小，加载快速</li>
 * </ul>
 * 
 * <p><b>局限性：</b>
 * <ul>
 *   <li>不支持复杂的层次结构</li>
 *   <li>数组和集合配置较为繁琐</li>
 *   <li>不支持多行值（除非使用反斜杠转义）</li>
 * </ul>
 * 
 * <p><b>使用场景：</b>适用于配置项较少、结构简单的场景，或需要与传统Java项目兼容的情况。
 * 
 * <p><b>实现原理：</b>
 * 使用 JDK 自带的 {@link Properties} 类进行解析，确保与Java标准完全兼容。
 * 
 * @author 杨潇
 * @since 2025-04-24
 * @see Properties JDK标准Properties类
 * @see AbstractConfigParser 配置解析器抽象基类
 * @see ConfigFileTypeEnum#PROPERTIES Properties类型枚举
 */
public class PropertiesConfigParser extends AbstractConfigParser {

    /**
     * 解析 Properties 格式的配置内容
     * <p>
     * 该方法将 Properties 格式的文本内容解析为键值对 Map。
     * 使用 JDK 标准的 {@link Properties#load(java.io.Reader)} 方法进行解析，
     * 确保与Java标准行为完全一致。
     * 
     * <p><b>解析流程：</b>
     * <ol>
     *   <li>创建 {@link Properties} 对象</li>
     *   <li>将配置文本内容包装为 {@link StringReader}</li>
     *   <li>调用 {@link Properties#load(java.io.Reader)} 加载配置</li>
     *   <li>直接返回 Properties 对象（Properties 继承自 Hashtable，本身就是 Map）</li>
     * </ol>
     * 
     * <p><b>输入示例：</b>
     * <pre>
     * onethread.config-file-type=properties
     * onethread.executors[0].thread-pool-id=pool1
     * onethread.executors[0].core-pool-size=10
     * </pre>
     * 
     * <p><b>输出结果：</b>
     * <pre>{@code
     * Map<Object, Object> result = {
     *     "onethread.config-file-type": "properties",
     *     "onethread.executors[0].thread-pool-id": "pool1",
     *     "onethread.executors[0].core-pool-size": "10"
     * }
     * }</pre>
     * 
     * <p><b>注意事项：</b>
     * <ul>
     *   <li>所有值都会被解析为字符串类型</li>
     *   <li>Properties 会自动处理转义字符（如 \n、\t等）</li>
     *   <li>以 # 或 ! 开头的行会被视为注释并忽略</li>
     *   <li>键值对可以使用 = 或 : 分隔</li>
     * </ul>
     *
     * @param content Properties 格式的配置文件内容（完整文本）
     * @return 解析后的键值对 Map，键和值均为字符串类型
     * @throws IOException 当配置内容格式错误或无法读取时抛出
     * @see Properties#load(java.io.Reader) JDK Properties加载方法
     */
    @Override
    public Map<Object, Object> doParse(String content) throws IOException {
        // 创建 Properties 对象用于加载配置
        Properties properties = new Properties();
        
        // 使用 StringReader 将字符串包装为 Reader 接口
        // Properties.load() 方法需要 Reader 参数
        properties.load(new StringReader(content));
        
        // Properties 继承自 Hashtable<Object,Object>，本身就是一个 Map
        // 直接返回即可，无需额外转换
        return properties;
    }

    /**
     * 获取当前解析器支持的配置文件类型列表
     * <p>
     * 该方法返回 {@link ConfigFileTypeEnum#PROPERTIES}，表示仅支持 Properties 格式。
     * 
     * <p><b>返回值：</b>
     * <ul>
     *   <li>只包含一个元素：{@link ConfigFileTypeEnum#PROPERTIES}</li>
     * </ul>
     *
     * @return 包含 PROPERTIES 类型的列表
     */
    @Override
    public List<ConfigFileTypeEnum> getConfigFileTypes() {
        return CollectionUtil.newArrayList(ConfigFileTypeEnum.PROPERTIES);
    }
}
