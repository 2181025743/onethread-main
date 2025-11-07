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

/**
 * 配置解析器抽象基类
 * <p>
 * 该抽象类为所有配置解析器提供通用的实现逻辑，减少子类的重复代码。
 * 子类只需实现 {@link #doParse(String)} 和 {@link #getConfigFileTypes()} 方法即可。
 * 
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>提供 {@link #supports(ConfigFileTypeEnum)} 方法的默认实现</li>
 *   <li>统一解析器的类型判断逻辑</li>
 *   <li>简化子类的实现复杂度</li>
 * </ul>
 * 
 * <p><b>设计模式：</b>模板方法模式（Template Method Pattern）
 * <br>在抽象类中定义算法骨架（supports方法），具体实现由子类完成
 * 
 * <p><b>继承关系：</b>
 * <pre>
 * AbstractConfigParser (抽象类)
 *    ├─ YamlConfigParser (YAML解析器)
 *    └─ PropertiesConfigParser (Properties解析器)
 * </pre>
 * 
 * @author 杨潇
 * @since 2025-04-23
 * @see ConfigParser 配置解析器接口
 * @see YamlConfigParser YAML解析器实现
 * @see PropertiesConfigParser Properties解析器实现
 */
public abstract class AbstractConfigParser implements ConfigParser {

    /**
     * 判断当前解析器是否支持指定类型的配置文件解析
     * <p>
     * 该方法通过检查子类返回的支持类型列表来判断是否支持目标类型。
     * 这是一个模板方法，子类无需重写。
     * 
     * <p><b>实现逻辑：</b>
     * <ol>
     *   <li>调用子类实现的 {@link #getConfigFileTypes()} 获取支持的类型列表</li>
     *   <li>检查列表中是否包含目标类型</li>
     *   <li>返回判断结果</li>
     * </ol>
     *
     * @param type 配置文件类型枚举（如 YAML、PROPERTIES）
     * @return {@code true} 表示支持该类型，{@code false} 表示不支持
     */
    @Override
    public boolean supports(ConfigFileTypeEnum type) {
        return getConfigFileTypes().contains(type);
    }
}
