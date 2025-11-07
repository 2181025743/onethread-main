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

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import org.yaml.snakeyaml.Yaml;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

/**
 * YAML 配置文件解析器
 * <p>
 * 该类负责解析 YAML 格式的配置文件，将层次化的 YAML 结构转换为扁平化的键值对 Map，
 * 以便与 Spring Boot 的配置绑定机制无缝集成。
 * 
 * <p><b>核心功能：</b>
 * <ul>
 *   <li><b>层次结构扁平化：</b>将嵌套的 YAML 结构转换为点号分隔的键路径</li>
 *   <li><b>数组索引化：</b>将 YAML 数组转换为带索引的键（如 {@code executors[0]}）</li>
 *   <li><b>类型安全：</b>对解析结果进行严格的类型检查和转换</li>
 * </ul>
 * 
 * <p><b>解析示例：</b>
 * <pre>
 * <b>输入 YAML：</b>
 * onethread:
 *   config-file-type: yaml
 *   executors:
 *     - thread-pool-id: pool1
 *       core-pool-size: 10
 *       maximum-pool-size: 20
 *     - thread-pool-id: pool2
 *       core-pool-size: 5
 * 
 * <b>输出 Map：</b>
 * {
 *   "onethread.config-file-type": "yaml",
 *   "onethread.executors[0].thread-pool-id": "pool1",
 *   "onethread.executors[0].core-pool-size": "10",
 *   "onethread.executors[0].maximum-pool-size": "20",
 *   "onethread.executors[1].thread-pool-id": "pool2",
 *   "onethread.executors[1].core-pool-size": "5"
 * }
 * </pre>
 * 
 * <p><b>设计亮点：</b>
 * <ul>
 *   <li><b>函数式编程：</b>使用 {@link Optional} 和 Stream API 实现优雅的空值处理和集合操作</li>
 *   <li><b>递归解析：</b>通过递归方法处理任意深度的嵌套结构</li>
 *   <li><b>路径规范化：</b>自动处理点号和数组索引的组合，生成符合 Spring Boot 规范的属性路径</li>
 * </ul>
 * 
 * <p><b>依赖库：</b>
 * <ul>
 *   <li>SnakeYAML：用于基础的 YAML 解析</li>
 *   <li>Hutool：提供实用的工具方法</li>
 * </ul>
 * 
 * <p><b>使用场景：</b>推荐用于配置项较多、结构复杂的项目，特别是需要管理多个线程池配置的场景。
 * 
 * @author 杨潇
 * @since 2025-04-24
 * @see AbstractConfigParser 配置解析器抽象基类
 * @see ConfigFileTypeEnum#YAML YAML类型枚举
 * @see ConfigFileTypeEnum#YML YML类型枚举
 */
public class YamlConfigParser extends AbstractConfigParser {

    /**
     * 数组索引左括号
     * <p>
     * 用于构建数组元素的键路径，如 {@code executors[0]}
     */
    private static final String INDEX_PREFIX = "[";

    /**
     * 数组索引右括号
     * <p>
     * 用于构建数组元素的键路径，如 {@code executors[0]}
     */
    private static final String INDEX_SUFFIX = "]";

    /**
     * 属性路径分隔符
     * <p>
     * 用于连接嵌套属性的键路径，如 {@code onethread.executors[0].core-pool-size}
     */
    private static final String PATH_SEPARATOR = ".";

    /**
     * 解析 YAML 配置内容为扁平化的键值对 Map
     * <p>
     * 这是解析流程的入口方法，使用函数式编程风格实现优雅的链式调用：
     * <ol>
     *   <li>空值检查：如果配置内容为空，直接返回空 Map</li>
     *   <li>YAML 解析：调用 {@link #parseYamlDocument} 将 YAML 文本解析为嵌套 Map</li>
     *   <li>结构扁平化：调用 {@link #normalizeHierarchy} 将嵌套结构转换为扁平键值对</li>
     * </ol>
     * 
     * <p><b>函数式编程优势：</b>
     * <ul>
     *   <li>使用 {@link Optional} 优雅处理空值，避免 {@code null} 判断</li>
     *   <li>使用 {@code map()} 方法进行函数式转换，代码简洁</li>
     *   <li>使用 {@code filter()} 进行条件过滤，逻辑清晰</li>
     * </ul>
     *
     * @param configuration YAML 格式的配置文件内容（完整文本）
     * @return 解析后的扁平化键值对 Map，如果输入为空则返回空 Map
     * @see #parseYamlDocument 第一步：YAML文档解析
     * @see #normalizeHierarchy 第二步：层次结构扁平化
     */
    @Override
    public Map<Object, Object> doParse(String configuration) {
        return Optional.ofNullable(configuration)
                .filter(StrUtil::isNotEmpty)             // 过滤空字符串
                .map(this::parseYamlDocument)            // 解析YAML为Map
                .map(this::normalizeHierarchy)           // 扁平化层次结构
                .orElseGet(Collections::emptyMap);       // 默认返回空Map
    }

    /**
     * 获取当前解析器支持的配置文件类型列表
     * <p>
     * 该方法返回 {@link ConfigFileTypeEnum#YAML} 和 {@link ConfigFileTypeEnum#YML}，
     * 表示同时支持 .yaml 和 .yml 两种文件扩展名。
     * 
     * <p><b>说明：</b>
     * <ul>
     *   <li>.yaml 和 .yml 是相同格式的不同扩展名</li>
     *   <li>解析逻辑完全相同，只是文件命名习惯不同</li>
     * </ul>
     *
     * @return 包含 YAML 和 YML 类型的列表
     */
    @Override
    public List<ConfigFileTypeEnum> getConfigFileTypes() {
        return List.of(ConfigFileTypeEnum.YAML, ConfigFileTypeEnum.YML);
    }

    /**
     * 解析 YAML 文档为嵌套的 Map 结构
     * <p>
     * 使用 SnakeYAML 库将 YAML 文本解析为 Java 对象（Map 结构）。
     * 该方法是 YAML 解析的第一步，将文本转换为内存中的数据结构。
     * 
     * <p><b>解析流程：</b>
     * <ol>
     *   <li>使用 SnakeYAML 的 {@link Yaml#load} 方法解析文本</li>
     *   <li>进行类型安全检查（确保结果是 Map 类型）</li>
     *   <li>强制类型转换为 {@code Map<Object, Object>}</li>
     *   <li>过滤空 Map，返回有效结果或空 Map</li>
     * </ol>
     * 
     * <p><b>类型安全性：</b>
     * SnakeYAML 的 {@code load()} 方法返回 {@code Object} 类型，可能是以下几种情况：
     * <ul>
     *   <li>{@code Map<Object, Object>}：正常的YAML对象（最常见）</li>
     *   <li>{@code List<?>}：YAML数组</li>
     *   <li>基本类型：纯文本或数值（不符合配置格式）</li>
     * </ul>
     * 因此需要进行类型检查和安全转换。
     * 
     * <p><b>示例：</b>
     * <pre>
     * <b>输入：</b>
     * onethread:
     *   config-file-type: yaml
     *   executors:
     *     - thread-pool-id: pool1
     * 
     * <b>输出：</b>
     * {
     *   "onethread": {
     *     "config-file-type": "yaml",
     *     "executors": [
     *       {"thread-pool-id": "pool1"}
     *     ]
     *   }
     * }
     * </pre>
     *
     * @param content YAML 格式的文本内容
     * @return 解析后的嵌套 Map 结构，如果解析失败或结果为空则返回空 Map
     * @see Yaml#load SnakeYAML的解析方法
     */
    private Map<Object, Object> parseYamlDocument(String content) {
        return Optional.ofNullable(new Yaml().load(content))
                .filter(obj -> obj instanceof Map)       // 类型安全检查：确保是Map
                .map(obj -> (Map<Object, Object>) obj)   // 安全类型转换
                .filter(map -> !MapUtil.isEmpty(map))    // 过滤空Map
                .orElseGet(Collections::emptyMap);       // 默认返回空Map
    }

    /**
     * 将嵌套的层次结构转换为扁平化的键值对
     * <p>
     * 这是 YAML 解析的第二步（也是核心步骤），将多层嵌套的 Map 结构
     * 转换为一级的键值对 Map，键使用点号和数组索引表示层次关系。
     * 
     * <p><b>转换逻辑：</b>
     * <ul>
     *   <li>创建新的 {@link LinkedHashMap} 存储扁平化结果（保持插入顺序）</li>
     *   <li>调用递归方法 {@link #processNestedElements} 处理嵌套元素</li>
     *   <li>返回扁平化后的 Map</li>
     * </ul>
     * 
     * <p><b>示例：</b>
     * <pre>
     * <b>输入（嵌套结构）：</b>
     * {
     *   "onethread": {
     *     "executors": [
     *       {"thread-pool-id": "pool1", "core-pool-size": 10}
     *     ]
     *   }
     * }
     * 
     * <b>输出（扁平结构）：</b>
     * {
     *   "onethread.executors[0].thread-pool-id": "pool1",
     *   "onethread.executors[0].core-pool-size": "10"
     * }
     * </pre>
     *
     * @param nestedData 嵌套的 Map 结构（从 {@link #parseYamlDocument} 返回的结果）
     * @return 扁平化的键值对 Map
     * @see #processNestedElements 递归处理嵌套元素
     */
    private Map<Object, Object> normalizeHierarchy(Map<Object, Object> nestedData) {
        Map<Object, Object> flattenedData = new LinkedHashMap<>();
        processNestedElements(flattenedData, nestedData, null);
        return flattenedData;
    }

    /**
     * 递归处理嵌套元素（核心递归方法）
     * <p>
     * 这是整个扁平化流程的核心方法，通过递归遍历处理各种数据类型：
     * <ul>
     *   <li><b>Map：</b>递归处理每个键值对</li>
     *   <li><b>Iterable（数组/列表）：</b>为每个元素生成索引并递归处理</li>
     *   <li><b>基本类型（叶子节点）：</b>直接存储到结果 Map</li>
     * </ul>
     * 
     * <p><b>递归逻辑：</b>
     * <pre>
     * processNestedElements(target, current, path)
     *   ├─ if (current is Map)
     *   │    └─ for each (key, value) in current:
     *   │         processNestedElements(target, value, path + "." + key)
     *   │
     *   ├─ if (current is Iterable)
     *   │    └─ for each (index, element) in current:
     *   │         processNestedElements(target, element, path + "[" + index + "]")
     *   │
     *   └─ else (基本类型)
     *        └─ target.put(path, current.toString())
     * </pre>
     * 
     * <p><b>路径构建示例：</b>
     * <pre>
     * 初始调用：processNestedElements(target, data, null)
     * 
     * 第1层：处理 "onethread" 键
     *   → currentPath = "onethread"
     * 
     * 第2层：处理 "executors" 键
     *   → currentPath = "onethread.executors"
     * 
     * 第3层：处理数组索引 0
     *   → currentPath = "onethread.executors[0]"
     * 
     * 第4层：处理 "thread-pool-id" 键
     *   → currentPath = "onethread.executors[0].thread-pool-id"
     *   → 到达叶子节点，存储键值对
     * </pre>
     *
     * @param target      目标 Map，用于存储扁平化后的键值对
     * @param current     当前正在处理的节点（可能是 Map、List 或基本类型）
     * @param currentPath 当前节点的路径（如 "onethread.executors[0]"），根节点为 {@code null}
     * @see #handleMapEntries 处理Map类型节点
     * @see #handleCollectionItems 处理数组类型节点
     * @see #persistLeafValue 存储叶子节点值
     */
    private void processNestedElements(Map<Object, Object> target, Object current, String currentPath) {
        if (current instanceof Map) {
            // 处理 Map 类型：递归处理每个键值对
            handleMapEntries(target, (Map<?, ?>) current, currentPath);
        } else if (current instanceof Iterable) {
            // 处理数组/列表类型：为每个元素生成索引并递归处理
            handleCollectionItems(target, (Iterable<?>) current, currentPath);
        } else {
            // 基本类型（叶子节点）：直接存储到结果 Map
            persistLeafValue(target, currentPath, current);
        }
    }

    /**
     * 处理 Map 类型的嵌套元素
     * <p>
     * 遍历 Map 的每个键值对，为每个值递归调用 {@link #processNestedElements}。
     * 在递归调用时，会将当前键添加到路径中，形成完整的属性路径。
     * 
     * <p><b>路径构建：</b>
     * <ul>
     *   <li>如果 parentPath 为 null：新路径 = key</li>
     *   <li>如果 parentPath 不为 null：新路径 = parentPath + "." + key</li>
     * </ul>
     * 
     * <p><b>示例：</b>
     * <pre>
     * 输入 Map：{"executors": [...], "config-file-type": "yaml"}
     * 当前路径：
"onethread"
     * 
     * 处理过程：
     * 1. 处理 "executors" 键
     *    → 递归调用：processNestedElements(target, [...], "onethread.executors")
     * 
     * 2. 处理 "config-file-type" 键
     *    → 递归调用：processNestedElements(target, "yaml", "onethread.config-file-type")
     * </pre>
     *
     * @param target     目标 Map，用于存储扁平化结果
     * @param entries    待处理的 Map（当前节点）
     * @param parentPath 父节点的路径
     * @see #buildPathSegment 构建新的路径
     */
    private void handleMapEntries(Map<Object, Object> target, Map<?, ?> entries, String parentPath) {
        entries.forEach((key, value) ->
                processNestedElements(target, value, buildPathSegment(parentPath, key))
        );
    }

    /**
     * 处理数组/列表类型的嵌套元素
     * <p>
     * 将 {@link Iterable} 转换为 {@link List}，然后为每个元素生成索引（从 0 开始），
     * 并递归处理每个元素。数组索引会添加到路径中，形如 {@code [0]}、{@code [1]} 等。
     * 
     * <p><b>实现步骤：</b>
     * <ol>
     *   <li>将 Iterable 转换为 List（方便获取索引）</li>
     *   <li>使用 {@link IntStream#range} 生成索引序列</li>
     *   <li>为每个元素调用 {@link #processNestedElements}，路径加上索引</li>
     * </ol>
     * 
     * <p><b>示例：</b>
     * <pre>
     * 输入数组：[{thread-pool-id: "pool1"}, {thread-pool-id: "pool2"}]
     * 当前路径："onethread.executors"
     * 
     * 处理过程：
     * 索引 0：processNestedElements(target, {thread-pool-id: "pool1"}, "onethread.executors[0]")
     * 索引 1：processNestedElements(target, {thread-pool-id: "pool2"}, "onethread.executors[1]")
     * </pre>
     *
     * @param target   目标 Map，用于存储扁平化结果
     * @param items    待处理的数组/列表（当前节点）
     * @param basePath 当前数组的基础路径
     * @see #createIndexedPath 创建带索引的路径
     */
    private void handleCollectionItems(Map<Object, Object> target, Iterable<?> items, String basePath) {
        // 将 Iterable 转换为 List，方便索引访问
        List<?> elements = StreamSupport.stream(items.spliterator(), false)
                .collect(Collectors.toList());
        
        // 为每个元素生成索引并递归处理
        IntStream.range(0, elements.size())
                .forEach(index -> processNestedElements(
                        target,
                        elements.get(index),
                        createIndexedPath(basePath, index)  // 路径 + [index]
                ));
    }

    /**
     * 构建属性路径片段
     * <p>
     * 将父路径和当前键组合成新的路径。如果父路径为 null（表示根节点），
     * 则直接返回键；否则使用点号连接父路径和键。
     * 
     * <p><b>路径构建规则：</b>
     * <ul>
     *   <li>如果 existingPath == null：返回 key.toString()</li>
     *   <li>如果 existingPath != null：返回 existingPath + "." + key</li>
     * </ul>
     * 
     * <p><b>示例：</b>
     * <pre>
     * buildPathSegment(null, "onethread")          → "onethread"
     * buildPathSegment("onethread", "executors")   → "onethread.executors"
     * buildPathSegment("onethread.executors[0]", "core-pool-size") 
     *                                              → "onethread.executors[0].core-pool-size"
     * </pre>
     *
     * @param existingPath 已有的路径（可能为 null）
     * @param key          当前键
     * @return 组合后的新路径
     */
    private String buildPathSegment(String existingPath, Object key) {
        return existingPath == null ?
                key.toString() :
                existingPath + PATH_SEPARATOR + key;
    }

    /**
     * 创建带数组索引的路径
     * <p>
     * 将基础路径与数组索引组合，形成完整的数组元素路径。
     * 索引格式为 {@code [index]}，如 {@code [0]}、{@code [1]} 等。
     * 
     * <p><b>示例：</b>
     * <pre>
     * createIndexedPath("onethread.executors", 0)   → "onethread.executors[0]"
     * createIndexedPath("onethread.executors", 1)   → "onethread.executors[1]"
     * createIndexedPath("config.items", 5)          → "config.items[5]"
     * </pre>
     *
     * @param basePath 基础路径（数组所在的属性路径）
     * @param index    数组索引（从 0 开始）
     * @return 带索引的完整路径
     */
    private String createIndexedPath(String basePath, int index) {
        return basePath + INDEX_PREFIX + index + INDEX_SUFFIX;
    }

    /**
     * 存储叶子节点的值到目标 Map
     * <p>
     * 当递归到达叶子节点（非 Map 也非 Iterable 的基本类型值）时，
     * 将该值以字符串形式存储到目标 Map 中。
     * 
     * <p><b>路径规范化：</b>
     * 在存储前会进行路径规范化处理，将 {@code .[} 替换为 {@code [}，
     * 以符合 Spring Boot 的属性路径规范。
     * 
     * <p><b>规范化示例：</b>
     * <pre>
     * 原路径：    "onethread.executors.[0].core-pool-size"
     * 规范化后：  "onethread.executors[0].core-pool-size"
     * </pre>
     * 
     * <p><b>空值处理：</b>
     * <ul>
     *   <li>如果 path 为 null，不进行存储（避免无效键）</li>
     *   <li>如果 value 为 null，存储字符串 "null"</li>
     *   <li>如果 value 不为 null，调用 {@code toString()} 转换为字符串</li>
     * </ul>
     * 
     * <p><b>示例：</b>
     * <pre>
     * persistLeafValue(target, "onethread.config-file-type", "yaml")
     *   → target.put("onethread.config-file-type", "yaml")
     * 
     * persistLeafValue(target, "onethread.executors[0].core-pool-size", 10)
     *   → target.put("onethread.executors[0].core-pool-size", "10")
     * 
     * persistLeafValue(target, "onethread.executors[0].description", null)
     *   → target.put("onethread.executors[0].description", null)
     * </pre>
     *
     * @param target 目标 Map，用于存储扁平化结果
     * @param path   属性的完整路径
     * @param value  属性值（可能为 null）
     */
    private void persistLeafValue(Map<Object, Object> target, String path, Object value) {
        if (path != null) {
            // 路径规范化：将 ".[" 替换为 "["
            // 例如：onethread.executors.[0] → onethread.executors[0]
            String normalizedPath = path.replace(PATH_SEPARATOR + INDEX_PREFIX, INDEX_PREFIX);
            
            // 存储值：null 保持为 null，其他值转换为字符串
            target.put(normalizedPath, value != null ? value.toString() : null);
        }
    }
}
