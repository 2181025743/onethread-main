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

package com.nageoffer.onethread.core.executor.support;

import lombok.AllArgsConstructor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 拒绝策略动态代理处理器
 * <p>
 * 该类实现了 JDK 动态代理的 {@link InvocationHandler} 接口，用于包装
 * {@link RejectedExecutionHandler}，在不修改原始拒绝策略的情况下，
 * 增加拒绝次数统计功能。
 * 
 * <p><b>核心功能：</b>
 * <ul>
 *   <li><b>透明代理：</b>对原始拒绝策略进行透明包装，行为完全一致</li>
 *   <li><b>拒绝统计：</b>拦截 {@code rejectedExecution} 方法，统计拒绝次数</li>
 *   <li><b>方法转发：</b>将所有方法调用转发给原始策略</li>
 *   <li><b>toString增强：</b>重写 toString 方法，返回原始策略的类名</li>
 * </ul>
 * 
 * <p><b>设计模式：</b>
 * <ul>
 *   <li><b>代理模式（Proxy Pattern）：</b>为原始对象提供代理，控制对它的访问</li>
 *   <li><b>装饰器模式（Decorator Pattern）：</b>动态地为对象添加新功能（拒绝计数）</li>
 * </ul>
 * 
 * <p><b>工作原理：</b>
 * <pre>
 * 客户端代码
 *    ↓ 调用方法
 * 代理对象（Proxy）
 *    ↓ invoke()
 * RejectedProxyInvocationHandler
 *    ├─ 如果是 rejectedExecution 方法
 *    │    ├─ 递增拒绝计数器
 *    │    └─ 转发给原始策略
 *    ├─ 如果是 toString 方法
 *    │    └─ 返回原始策略类名
 *    └─ 其他方法
 *         └─ 直接转发给原始策略
 * </pre>
 * 
 * <p><b>使用示例：</b>
 * <pre>{@code
 * // 原始拒绝策略
 * RejectedExecutionHandler originalHandler = new ThreadPoolExecutor.CallerRunsPolicy();
 * 
 * // 拒绝次数计数器
 * AtomicLong rejectCount = new AtomicLong(0);
 * 
 * // 创建代理处理器
 * InvocationHandler proxyHandler = new RejectedProxyInvocationHandler(
 *     originalHandler,
 *     rejectCount
 * );
 * 
 * // 创建代理对象
 * RejectedExecutionHandler proxyRejectedHandler = (RejectedExecutionHandler) Proxy.newProxyInstance(
 *     originalHandler.getClass().getClassLoader(),
 *     new Class[]{RejectedExecutionHandler.class},
 *     proxyHandler
 * );
 * 
 * // 在线程池中使用代理策略
 * ThreadPoolExecutor executor = new ThreadPoolExecutor(
 *     5, 10, 60L, TimeUnit.SECONDS,
 *     new LinkedBlockingQueue<>(100),
 *     Executors.defaultThreadFactory(),
 *     proxyRejectedHandler  // 使用代理策略
 * );
 * 
 * // 当任务被拒绝时，rejectCount 会自动递增
 * executor.execute(task);
 * 
 * // 查询拒绝次数
 * long count = rejectCount.get();
 * System.out.println("拒绝次数: " + count);
 * }</pre>
 * 
 * <p><b>与轻量级代理的对比：</b>
 * <table border="1" cellpadding="5">
 *   <tr><th>特性</th><th>JDK动态代理（本类）</th><th>匿名内部类代理</th></tr>
 *   <tr><td>实现方式</td><td>InvocationHandler + Proxy</td><td>匿名内部类</td></tr>
 *   <tr><td>性能</td><td>较慢（反射调用）</td><td>较快（直接调用）</td></tr>
 *   <tr><td>灵活性</td><td>高（可拦截所有方法）</td><td>低（需显式实现）</td></tr>
 *   <tr><td>代码量</td><td>较多</td><td>较少</td></tr>
 *   <tr><td>使用场景</td><td>需要拦截多个方法</td><td>只拦截一两个方法</td></tr>
 * </table>
 * 
 * <p><b>注意：</b>
 * 在 {@link OneThreadExecutor} 中，为了性能考虑，实际使用的是更轻量级的匿名内部类方式，
 * 而非本类的动态代理方式。本类作为替代方案保留，供需要更复杂代理逻辑的场景使用。
 * 
 * @author 杨潇
 * @since 2025-05-05
 * @see InvocationHandler JDK动态代理接口
 * @see RejectedExecutionHandler 拒绝策略接口
 * @see OneThreadExecutor oneThread动态线程池
 */
@AllArgsConstructor
public class RejectedProxyInvocationHandler implements InvocationHandler {

    /**
     * 被代理的目标对象（原始拒绝策略）
     * <p>
     * 指向实际的拒绝策略实例，所有方法调用最终会转发给这个对象。
     * 
     * <p><b>类型：</b>通常是 {@link RejectedExecutionHandler} 的实现类，如：
     * <ul>
     *   <li>{@link ThreadPoolExecutor.CallerRunsPolicy}</li>
     *   <li>{@link ThreadPoolExecutor.AbortPolicy}</li>
     *   <li>{@link ThreadPoolExecutor.DiscardPolicy}</li>
     *   <li>{@link ThreadPoolExecutor.DiscardOldestPolicy}</li>
     * </ul>
     */
    private final Object target;

    /**
     * 拒绝次数计数器
     * <p>
     * 使用 {@link AtomicLong} 保证多线程环境下的原子性。
     * 每次 {@code rejectedExecution} 方法被调用时，该计数器会递增。
     * 
     * <p><b>用途：</b>
     * <ul>
     *   <li>监控线程池拒绝任务的频率</li>
     *   <li>触发告警（拒绝次数超过阈值）</li>
     *   <li>分析线程池容量是否充足</li>
     * </ul>
     */
    private final AtomicLong rejectCount;

    /**
     * 拒绝执行方法的名称常量
     * <p>
     * 用于方法名匹配，判断当前调用的是否为拒绝执行方法。
     * 
     * <p><b>值：</b>"rejectedExecution"
     */
    private static final String REJECT_METHOD = "rejectedExecution";

    /**
     * 代理方法调用处理器
     * <p>
     * 这是 JDK 动态代理的核心方法，所有对代理对象的方法调用都会经过此方法。
     * 该方法负责拦截特定方法、增强功能、转发调用。
     * 
     * <p><b>处理逻辑：</b>
     * <ol>
     *   <li><b>拦截 rejectedExecution 方法：</b>
     *       <ul>
     *         <li>检查方法名是否为 "rejectedExecution"</li>
     *         <li>检查参数个数是否为 2</li>
     *         <li>检查参数类型是否为 (Runnable, ThreadPoolExecutor)</li>
     *         <li>如果都匹配，递增拒绝计数器</li>
     *       </ul>
     *   </li>
     *   <li><b>拦截 toString 方法：</b>
     *       <ul>
     *         <li>如果调用的是无参的 toString 方法</li>
     *         <li>直接返回原始策略的简单类名（如 "CallerRunsPolicy"）</li>
     *         <li>避免返回代理类名导致混淆</li>
     *       </ul>
     *   </li>
     *   <li><b>转发其他方法：</b>
     *       <ul>
     *         <li>使用反射调用原始对象的方法</li>
     *         <li>返回方法的执行结果</li>
     *         <li>如果方法抛出异常，提取真实异常并重新抛出</li>
     *       </ul>
     *   </li>
     * </ol>
     * 
     * <p><b>方法调用流程：</b>
     * <pre>
     * // 调用代理对象的方法
     * proxyHandler.rejectedExecution(task, executor)
     *    ↓
     * invoke(proxy, rejectedExecutionMethod, [task, executor])
     *    ↓
     * 1. 匹配到 rejectedExecution 方法
     * 2. rejectCount.incrementAndGet()  ← 增强：统计拒绝次数
     * 3. method.invoke(target, args)    ← 转发：调用原始策略
     *    ↓
     * 原始策略执行（如 CallerRunsPolicy.rejectedExecution()）
     * </pre>
     * 
     * <p><b>参数校验：</b>
     * 拦截 rejectedExecution 方法时会进行严格的参数校验：
     * <ul>
     *   <li>方法名必须为 "rejectedExecution"</li>
     *   <li>参数不能为 null</li>
     *   <li>参数个数必须为 2</li>
     *   <li>第一个参数必须是 {@link Runnable} 类型</li>
     *   <li>第二个参数必须是 {@link ThreadPoolExecutor} 类型</li>
     * </ul>
     * 
     * <p><b>异常处理：</b>
     * <ul>
     *   <li>使用 {@code try-catch} 捕获 {@link InvocationTargetException}</li>
     *   <li>提取真实的异常（{@link InvocationTargetException#getCause()}）</li>
     *   <li>重新抛出真实异常，确保调用方能正确捕获</li>
     * </ul>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * // 完整的代理创建过程
     * RejectedExecutionHandler original = new ThreadPoolExecutor.CallerRunsPolicy();
     * AtomicLong rejectCount = new AtomicLong(0);
     * 
     * // 创建代理
     * RejectedExecutionHandler proxy = (RejectedExecutionHandler) Proxy.newProxyInstance(
     *     original.getClass().getClassLoader(),
     *     new Class[]{RejectedExecutionHandler.class},
     *     new RejectedProxyInvocationHandler(original, rejectCount)
     * );
     * 
     * // 使用代理
     * proxy.rejectedExecution(task, executor);
     * // 内部流程：
     * // 1. invoke() 方法被调用
     * // 2. 检测到是 rejectedExecution 方法
     * // 3. rejectCount 递增
     * // 4. 转发给原始策略执行
     * 
     * // toString 方法增强
     * String name = proxy.toString();
     * // 返回 "CallerRunsPolicy" 而非 "$Proxy0"
     * }</pre>
     *
     * @param proxy  代理对象实例（由 JDK 动态代理生成）
     * @param method 被调用的方法对象
     * @param args   方法参数数组
     * @return 方法执行结果（如果方法有返回值）
     * @throws Throwable 如果原始方法抛出异常
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 1. 拦截 rejectedExecution 方法，增加拒绝计数
        // 严格校验方法签名，确保只拦截正确的方法
        if (REJECT_METHOD.equals(method.getName()) &&
                args != null &&                                  // 参数不为空
                args.length == 2 &&                              // 参数个数为2
                args[0] instanceof Runnable &&                   // 第一个参数是Runnable
                args[1] instanceof ThreadPoolExecutor) {         // 第二个参数是ThreadPoolExecutor
            
            // 递增拒绝计数器（线程安全的原子操作）
            rejectCount.incrementAndGet();
        }

        // 2. 拦截 toString 方法，返回原始策略的类名
        // 避免返回代理类名（如 "$Proxy0"），提高可读性
        if (method.getName().equals("toString") && method.getParameterCount() == 0) {
            return target.getClass().getSimpleName();
        }

        // 3. 转发所有方法调用给原始目标对象
        try {
            // 使用反射调用原始对象的方法
            return method.invoke(target, args);
        } catch (InvocationTargetException ex) {
            // 提取并抛出真实异常
            // InvocationTargetException 是反射调用的包装异常
            // 需要通过 getCause() 获取真实的异常
            throw ex.getCause();
        }
    }
}
