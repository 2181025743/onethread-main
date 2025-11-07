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

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 拒绝策略类型枚举
 * <p>
 * 该枚举定义了线程池支持的所有拒绝策略类型，并提供了统一的策略创建接口。
 * 当线程池和队列都满时，会根据配置的拒绝策略处理新提交的任务。
 * 
 * <p><b>什么是拒绝策略？</b>
 * <br>当线程池的任务队列已满，且线程数已达到最大线程数时，线程池无法接受新任务。
 * 此时需要一个策略来处理这些被拒绝的任务，这就是拒绝策略（Rejected Execution Handler）。
 * 
 * <p><b>触发条件：</b>
 * <ul>
 *   <li>线程池中的线程数已达到最大线程数（maximumPoolSize）</li>
 *   <li>任务队列已满（对于有界队列）</li>
 *   <li>新任务被提交到线程池</li>
 * </ul>
 * 
 * <p><b>核心功能：</b>
 * <ul>
 *   <li><b>类型安全：</b>使用枚举避免字符串硬编码</li>
 *   <li><b>统一创建：</b>提供统一的策略创建接口</li>
 *   <li><b>易于扩展：</b>新增策略只需添加枚举项</li>
 *   <li><b>配置友好：</b>支持从配置文件通过字符串名称创建策略</li>
 * </ul>
 * 
 * <p><b>四种内置策略对比：</b>
 * <table border="1" cellpadding="5">
 *   <tr>
 *     <th>策略名称</th>
 *     <th>行为描述</th>
 *     <th>适用场景</th>
 *     <th>优点</th>
 *     <th>缺点</th>
 *   </tr>
 *   <tr>
 *     <td>AbortPolicy</td>
 *     <td>抛出异常</td>
 *     <td>关键任务，不允许丢失</td>
 *     <td>立即发现问题，便于监控</td>
 *     <td>可能影响系统稳定性</td>
 *   </tr>
 *   <tr>
 *     <td>CallerRunsPolicy</td>
 *     <td>调用者线程执行</td>
 *     <td>任务重要，需要执行</td>
 *     <td>不丢失任务，自然降速</td>
 *     <td>可能阻塞调用线程</td>
 *   </tr>
 *   <tr>
 *     <td>DiscardPolicy</td>
 *     <td>静默丢弃</td>
 *     <td>任务不重要，可丢失</td>
 *     <td>不影响系统，简单</td>
 *     <td>任务丢失无感知</td>
 *   </tr>
 *   <tr>
 *     <td>DiscardOldestPolicy</td>
 *     <td>丢弃最旧任务</td>
 *     <td>新任务更重要</td>
 *     <td>保证新任务执行</td>
 *     <td>可能丢失重要任务</td>
 *   </tr>
 * </table>
 * 
 * <p><b>选择建议：</b>
 * <table border="1" cellpadding="5">
 *   <tr><th>场景</th><th>推荐策略</th><th>理由</th></tr>
 *   <tr><td>订单处理</td><td>AbortPolicy</td><td>不能丢失订单，抛异常后可以重试或告警</td></tr>
 *   <tr><td>日志记录</td><td>DiscardPolicy</td><td>日志丢失影响小，避免影响主流程</td></tr>
 *   <tr><td>消息推送</td><td>CallerRunsPolicy</td><td>重要消息不能丢，降速可接受</td></tr>
 *   <tr><td>实时数据</td><td>DiscardOldestPolicy</td><td>只关心最新数据</td></tr>
 * </table>
 * 
 * <p><b>设计模式：</b>
 * <ul>
 *   <li><b>策略模式（Strategy Pattern）：</b>不同的拒绝策略代表不同的处理策略</li>
 *   <li><b>单例模式：</b>每个策略的枚举实例是全局唯一的</li>
 * </ul>
 * 
 * <p><b>使用示例：</b>
 * <pre>{@code
 * // 示例1：通过枚举创建策略
 * RejectedExecutionHandler handler1 = 
 *     RejectedPolicyTypeEnum.CALLER_RUNS_POLICY.getRejectedHandler();
 * 
 * 
 * // 示例2：通过字符串名称创建策略（配置文件场景）
 * String policyName = "CallerRunsPolicy";
 * RejectedExecutionHandler handler2 = 
 *     RejectedPolicyTypeEnum.createPolicy(policyName);
 * 
 * 
 * // 示例3：在线程池构建器中使用
 * ThreadPoolExecutor executor = ThreadPoolExecutorBuilder.builder()
 *     .rejectedHandler(RejectedPolicyTypeEnum.ABORT_POLICY.getRejectedHandler())
 *     .build();
 * 
 * 
 * // 示例4：从配置文件读取
 * // 配置文件：onethread.executors[0].rejected-handler=CallerRunsPolicy
 * String policyName = config.getString("rejected-handler");
 * RejectedExecutionHandler handler = RejectedPolicyTypeEnum.createPolicy(policyName);
 * }</pre>
 * 
 * <p><b>监控建议：</b>
 * <ul>
 *   <li>监控拒绝策略的触发次数，及时发现线程池容量不足</li>
 *   <li>对于 AbortPolicy，捕获 {@link java.util.concurrent.RejectedExecutionException} 并记录日志</li>
 *   <li>对于 DiscardPolicy，通过计数器统计丢弃的任务数</li>
 *   <li>配合告警系统，拒绝次数超过阈值时发送告警</li>
 * </ul>
 * 
 * @author 杨潇
 * @since 2025-04-20
 * @see RejectedExecutionHandler JDK拒绝策略接口
 * @see ThreadPoolExecutor JDK线程池
 * @see ThreadPoolExecutorBuilder 线程池构建器
 */
public enum RejectedPolicyTypeEnum {

    /**
     * 调用者运行策略（推荐，适合大多数场景）
     * <p>
     * 当线程池无法接受新任务时，不会抛出异常也不会丢弃任务，
     * 而是让提交任务的线程（调用者线程）自己执行这个任务。
     * 
     * <p><b>行为特点：</b>
     * <ul>
     *   <li>任务由调用 {@code execute()} 方法的线程执行</li>
     *   <li>不丢失任务，保证任务一定会被执行</li>
     *   <li>自然降速：调用线程被占用，无法继续提交任务</li>
     *   <li>提供反压（Back Pressure）机制</li>
     * </ul>
     * 
     * <p><b>执行流程：</b>
     * <pre>
     * 1. 调用线程提交任务：executor.execute(task)
     * 2. 线程池满，触发拒绝策略
     * 3. CallerRunsPolicy 让调用线程执行 task.run()
     * 4. 调用线程在执行完任务前，无法提交新任务
     * 5. 自然形成流量控制
     * </pre>
     * 
     * <p><b>优势：</b>
     * <ul>
     *   <li><b>任务不丢失：</b>保证任务一定会被执行，适合重要业务</li>
     *   <li><b>自然降速：</b>生产者被迫变成消费者，降低任务提交速度</li>
     *   <li><b>平滑处理：</b>不会抛出异常，不影响系统稳定性</li>
     *   <li><b>简单有效：</b>无需额外的队列或缓冲机制</li>
     * </ul>
     * 
     * <p><b>劣势：</b>
     * <ul>
     *   <li><b>阻塞调用线程：</b>如果任务耗时长，会长时间占用调用线程</li>
     *   <li><b>性能下降：</b>调用线程可能是重要的业务线程（如Tomcat请求线程）</li>
     *   <li><b>无法完全避免过载：</b>只是延缓，不能根本解决容量问题</li>
     * </ul>
     * 
     * <p><b>适用场景：</b>
     * <ul>
     *   <li><b>重要任务：</b>任务不能丢失，必须执行（如订单处理、支付回调）</li>
     *   <li><b>同步调用：</b>调用方可以等待任务执行完成</li>
     *   <li><b>短时任务：</b>任务执行时间较短，不会长时间占用调用线程</li>
     *   <li><b>需要反压：</b>希望通过占用调用线程来自然降低提交速度</li>
     * </ul>
     * 
     * <p><b>注意事项：</b>
     * <ul>
     *   <li>不适合长时间任务（会长时间阻塞调用线程）</li>
     *   <li>调用线程可能是Tomcat/Jetty等容器的请求线程，被占用会影响并发能力</li>
     *   <li>任务执行中的异常会影响调用线程</li>
     * </ul>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * // 场景：订单处理系统
     * ThreadPoolExecutor orderExecutor = ThreadPoolExecutorBuilder.builder()
     *     .corePoolSize(10)
     *     .maximumPoolSize(20)
     *     .workQueueCapacity(100)
     *     .rejectedHandler(new ThreadPoolExecutor.CallerRunsPolicy())
     *     .threadFactory("order-processor")
     *     .build();
     * 
     * // 提交订单处理任务
     * orderExecutor.execute(() -> {
     *     // 处理订单逻辑
     *     processOrder(order);
     * });
     * // 如果线程池满，当前线程会执行这个任务，保证订单不丢失
     * }</pre>
     * 
     * @see ThreadPoolExecutor.CallerRunsPolicy JDK实现类
     */
    CALLER_RUNS_POLICY("CallerRunsPolicy", new ThreadPoolExecutor.CallerRunsPolicy()),

    /**
     * 中止策略（默认策略，快速失败）
     * <p>
     * 当线程池无法接受新任务时，直接抛出 {@link java.util.concurrent.RejectedExecutionException} 异常。
     * 这是 JDK 线程池的默认拒绝策略。
     * 
     * <p><b>行为特点：</b>
     * <ul>
     *   <li>立即抛出 {@code RejectedExecutionException} 异常</li>
     *   <li>任务不会被执行，也不会被保存</li>
     *   <li>异常会传播到调用方，由调用方处理</li>
     *   <li>快速失败（Fail-Fast）机制</li>
     * </ul>
     * 
     * <p><b>执行流程：</b>
     * <pre>
     * 1. 调用线程提交任务：executor.execute(task)
     * 2. 线程池满，触发拒绝策略
     * 3. AbortPolicy 抛出 RejectedExecutionException
     * 4. 调用方捕获异常并处理（重试、记录、告警等）
     * </pre>
     * 
     * <p><b>优势：</b>
     * <ul>
     *   <li><b>立即感知：</b>通过异常立即发现线程池容量不足的问题</li>
     *   <li><b>便于监控：</b>异常可以被捕获、记录和统计</li>
     *   <li><b>可重试：</b>调用方可以捕获异常后实现重试逻辑</li>
     *   <li><b>简单直接：</b>行为明确，易于理解</li>
     * </ul>
     * 
     * <p><b>劣势：</b>
     * <ul>
     *   <li><b>任务丢失：</b>被拒绝的任务不会被执行</li>
     *   <li><b>影响稳定性：</b>如果不捕获异常，可能导致调用方异常退出</li>
     *   <li><b>需要额外处理：</b>调用方需要捕获异常并实现重试或降级逻辑</li>
     * </ul>
     * 
     * <p><b>适用场景：</b>
     * <ul>
     *   <li><b>关键任务：</b>任务非常重要，不能静默丢失，需要明确的失败信号</li>
     *   <li><b>需要监控：</b>希望通过异常监控线程池状态，及时发现容量问题</li>
     *   <li><b>有重试机制：</b>调用方实现了重试逻辑，可以在捕获异常后重试</li>
     *   <li><b>调试阶段：</b>开发和测试阶段，快速发现线程池配置问题</li>
     * </ul>
     * 
     * <p><b>注意事项：</b>
     * <ul>
     *   <li>必须捕获 {@code RejectedExecutionException}，否则会导致调用线程异常退出</li>
     *   <li>需要实现重试或降级逻辑，否则任务会丢失</li>
     *   <li>高并发下可能产生大量异常，影响性能</li>
     * </ul>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * // 场景：支付回调处理
     * ThreadPoolExecutor paymentExecutor = ThreadPoolExecutorBuilder.builder()
     *     .corePoolSize(5)
     *     .maximumPoolSize(10)
     *     .workQueueCapacity(50)
     *     .rejectedHandler(new ThreadPoolExecutor.AbortPolicy())  // 默认策略
     *     .threadFactory("payment-callback")
     *     .build();
     * 
     * // 提交支付回调任务，捕获拒绝异常
     * try {
     *     paymentExecutor.execute(() -> {
     *         // 处理支付回调
     *         processPaymentCallback(callback);
     *     });
     * } catch (RejectedExecutionException e) {
     *     // 线程池满，任务被拒绝
     *     log.error("支付回调任务被拒绝，将重试", e);
     *     
     *     // 方案1：重试（延迟后再次提交）
     *     scheduleRetry(callback);
     *     
     *     // 方案2：存入数据库，后续处理
     *     saveToDatabase(callback);
     *     
     *     // 方案3：发送告警
     *     alertService.sendAlert("支付回调线程池已满");
     * }
     * }</pre>
     * 
     * @see ThreadPoolExecutor.AbortPolicy JDK实现类
     * @see java.util.concurrent.RejectedExecutionException 拒绝执行异常
     */
    ABORT_POLICY("AbortPolicy", new ThreadPoolExecutor.AbortPolicy()),

    /**
     * 丢弃策略（静默丢弃，适合不重要任务）
     * <p>
     * 当线程池无法接受新任务时，静默地丢弃任务，不抛出异常，也不执行任务。
     * 这是一种"鸵鸟策略"，假装什么都没发生。
     * 
     * <p><b>行为特点：</b>
     * <ul>
     *   <li>静默丢弃任务，不抛出异常</li>
     *   <li>调用方无感知（execute方法正常返回）</li>
     *   <li>任务不会被执行，也不会被保存</li>
     *   <li>对系统运行无影响</li>
     * </ul>
     * 
     * <p><b>执行流程：</b>
     * <pre>
     * 1. 调用线程提交任务：executor.execute(task)
     * 2. 线程池满，触发拒绝策略
     * 3. DiscardPolicy 什么都不做，直接返回
     * 4. 调用方继续执行，不知道任务已被丢弃
     * </pre>
     * 
     * <p><b>优势：</b>
     * <ul>
     *   <li><b>简单直接：</b>实现最简单（空方法）</li>
     *   <li><b>无副作用：</b>不抛异常，不影响调用方</li>
     *   <li><b>性能好：</b>无任何额外操作</li>
     *   <li><b>系统稳定：</b>不会因为拒绝策略导致系统问题</li>
     * </ul>
     * 
     * <p><b>劣势：</b>
     * <ul>
     *   <li><b>任务丢失：</b>任务静默丢失，调用方无感知</li>
     *   <li><b>难以监控：</b>没有异常、日志或其他信号，难以发现问题</li>
     *   <li><b>数据不一致：</b>对于重要任务，可能导致业务数据不一致</li>
     *   <li><b>调试困难：</b>问题难以排查和定位</li>
     * </ul>
     * 
     * <p><b>适用场景：</b>
     * <ul>
     *   <li><b>不重要任务：</b>任务丢失不影响业务（如统计、打点、日志）</li>
     *   <li><b>允许丢失：</b>业务容忍部分任务丢失</li>
     *   <li><b>实时性优先：</b>只关心最新数据，旧数据可以丢弃</li>
     *   <li><b>避免影响主流程：</b>希望即使线程池满也不影响主业务流程</li>
     * </ul>
     * 
     * <p><b>注意事项：</b>
     * <ul>
     *   <li>仅用于不重要的任务，重要任务禁止使用此策略</li>
     *   <li>建议配合计数器统计丢弃的任务数，便于监控</li>
     *   <li>需要在文档中明确说明任务可能被丢弃</li>
     * </ul>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * // 场景：访问日志记录（不重要，可丢失）
     * ThreadPoolExecutor logExecutor = ThreadPoolExecutorBuilder.builder()
     *     .corePoolSize(2)
     *     .maximumPoolSize(5)
     *     .workQueueCapacity(100)
     *     .rejectedHandler(new ThreadPoolExecutor.DiscardPolicy())
     *     .threadFactory("access-log")
     *     .build();
     * 
     * // 记录访问日志
     * logExecutor.execute(() -> {
     *     // 记录日志到文件或数据库
     *     logToFile(accessLog);
     * });
     * // 如果线程池满，日志会被丢弃，但不影响主业务
     * 
     * 
     * // 改进：添加计数器监控丢弃的任务数
     * AtomicLong discardedCount = new AtomicLong(0);
     * RejectedExecutionHandler customHandler = (r, executor) -> {
     *     discardedCount.incrementAndGet();
     *     // 静默丢弃
     * };
     * }</pre>
     * 
     * @see ThreadPoolExecutor.DiscardPolicy JDK实现类
     */
    DISCARD_POLICY("DiscardPolicy", new ThreadPoolExecutor.DiscardPolicy()),

    /**
     * 丢弃最旧任务策略（新任务优先）
     * <p>
     * 当线程池无法接受新任务时，从队列头部移除（丢弃）最旧的任务，
     * 然后尝试重新提交当前任务。如果再次失败，会继续丢弃旧任务，直到成功或队列为空。
     * 
     * <p><b>行为特点：</b>
     * <ul>
     *   <li>丢弃队列头部最旧的任务</li>
     *   <li>为新任务腾出空间</li>
     *   <li>新任务优先级高于旧任务</li>
     *   <li>不抛出异常</li>
     * </ul>
     * 
     * <p><b>执行流程：</b>
     * <pre>
     * 1. 调用线程提交新任务：executor.execute(newTask)
     * 2. 线程池满，触发拒绝策略
     * 3. DiscardOldestPolicy 从队列头部移除最旧的任务
     * 4. 重新提交新任务到线程池
     * 5. 如果仍然失败，继续丢弃旧任务
     * </pre>
     * 
     * <p><b>优势：</b>
     * <ul>
     *   <li><b>新任务优先：</b>保证新任务能够被执行</li>
     *   <li><b>实时性好：</b>适合只关心最新数据的场景</li>
     *   <li><b>不抛异常：</b>不影响调用方</li>
     *   <li><b>自动腾空间：</b>自动为新任务腾出队列空间</li>
     * </ul>
     * 
     * <p><b>劣势：</b>
     * <ul>
     *   <li><b>旧任务丢失：</b>队列中的旧任务可能被丢弃</li>
     *   <li><b>不公平：</b>破坏了FIFO顺序，可能导致某些任务永远不被执行</li>
     *   <li><b>难以预测：</b>哪些任务会被丢弃不可预测</li>
     *   <li><b>潜在数据问题：</b>如果旧任务很重要，会导致问题</li>
     * </ul>
     * 
     * <p><b>适用场景：</b>
     * <ul>
     *   <li><b>实时数据：</b>只关心最新的数据或状态（如股票行情、传感器数据）</li>
     *   <li><b>状态更新：</b>后面的更新会覆盖前面的更新</li>
     *   <li><b>新任务优先：</b>新任务的优先级明确高于旧任务</li>
     *   <li><b>允许丢失旧数据：</b>业务容忍旧数据丢失</li>
     * </ul>
     * 
     * <p><b>注意事项：</b>
     * <ul>
     *   <li>不适合顺序敏感的任务（会破坏执行顺序）</li>
     *   <li>不适合重要任务（旧任务可能很重要）</li>
     *   <li>需要评估丢弃旧任务的业务影响</li>
     *   <li>建议配合监控统计丢弃的任务数</li>
     * </ul>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * // 场景1：股票行情推送（只关心最新价格）
     * ThreadPoolExecutor stockExecutor = ThreadPoolExecutorBuilder.builder()
     *     .corePoolSize(5)
     *     .maximumPoolSize(10)
     *     .workQueueCapacity(50)
     *     .rejectedHandler(new ThreadPoolExecutor.DiscardOldestPolicy())
     *     .threadFactory("stock-price")
     *     .build();
     * 
     * // 推送股票价格更新
     * stockExecutor.execute(() -> {
     *     // 推送最新股票价格
     *     pushStockPrice(latestPrice);
     * });
     * // 如果线程池满，会丢弃最旧的价格推送任务，保证最新价格能被推送
     * 
     * 
     * // 场景2：传感器数据采集（只关心最新数据）
     * ThreadPoolExecutor sensorExecutor = ThreadPoolExecutorBuilder.builder()
     *     .corePoolSize(3)
     *     .maximumPoolSize(5)
     *     .workQueueCapacity(20)
     *     .rejectedHandler(new ThreadPoolExecutor.DiscardOldestPolicy())
     *     .threadFactory("sensor-data")
     *     .build();
     * 
     * // 处理传感器数据
     * sensorExecutor.execute(() -> {
     *     // 处理最新的传感器读数
     *     processSensorData(latestReading);
     * });
     * }</pre>
     * 
     * <p><b>危险示例（错误用法）：</b>
     * <pre>{@code
     * // ❌ 错误：用于订单处理（旧订单很重要，不能丢弃）
     * ThreadPoolExecutor orderExecutor = ThreadPoolExecutorBuilder.builder()
     *     .rejectedHandler(new ThreadPoolExecutor.DiscardOldestPolicy())  // 错误！
     *     .build();
     * 
     * // 处理订单
     * orderExecutor.execute(() -> processOrder(order));
     * // 危险：如果线程池满，旧订单可能被丢弃，导致订单丢失！
     * }</pre>
     * 
     * @see ThreadPoolExecutor.DiscardOldestPolicy JDK实现类
     */
    DISCARD_OLDEST_POLICY("DiscardOldestPolicy", new ThreadPoolExecutor.DiscardOldestPolicy());

    /**
     * 策略类型的字符串名称
     * <p>
     * 用于配置文件、日志记录、前端展示等场景。
     * 该名称与 JDK 类名保持一致，确保可读性。
     */
    @Getter
    private String name;

    /**
     * 拒绝策略处理器实例
     * <p>
     * 每个枚举项持有一个对应的 {@link RejectedExecutionHandler} 实例。
     * 这些实例在枚举初始化时创建，全局共享使用。
     * 
     * <p><b>注意：</b>所有 JDK 内置的拒绝策略都是无状态的，可以安全地共享使用。
     */
    @Getter
    private RejectedExecutionHandler rejectedHandler;

    /**
     * 枚举构造函数
     * 
     * @param rejectedPolicyName 策略名称
     * @param rejectedHandler    策略处理器实例
     */
    RejectedPolicyTypeEnum(String rejectedPolicyName, RejectedExecutionHandler rejectedHandler) {
        this.name = rejectedPolicyName;
        this.rejectedHandler = rejectedHandler;
    }

    /**
     * 名称到枚举的映射表
     * <p>
     * 用于根据字符串名称快速查找对应的枚举实例。
     * 在静态初始化块中构建，避免运行时查找开销。
     */
    private static final Map<String, RejectedPolicyTypeEnum> NAME_TO_ENUM_MAP;

    /**
     * 静态初始化块：构建名称到枚举的映射表
     * <p>
     * 在类加载时执行一次，将所有枚举项的名称映射到对应的枚举实例。
     * 使用 HashMap 存储，查找效率为 O(1)。
     */
    static {
        final RejectedPolicyTypeEnum[] values = RejectedPolicyTypeEnum.values();
        NAME_TO_ENUM_MAP = new HashMap<>(values.length);
        for (RejectedPolicyTypeEnum value : values) {
            NAME_TO_ENUM_MAP.put(value.name, value);
        }
    }

    /**
     * 根据策略名称创建拒绝策略处理器
     * <p>
     * 这是创建拒绝策略的统一入口，支持从配置文件通过字符串名称创建策略。
     * 内部通过名称查找对应的枚举实例，然后返回枚举持有的处理器实例。
     * 
     * <p><b>执行流程：</b>
     * <ol>
     *   <li>根据策略名称从 {@link #NAME_TO_ENUM_MAP} 查找对应的枚举</li>
     *   <li>如果找到，返回枚举持有的 {@link RejectedExecutionHandler} 实例</li>
     *   <li>如果未找到，抛出 {@link IllegalArgumentException} 异常</li>
     * </ol>
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>从配置文件读取策略类型：{@code rejected-handler: CallerRunsPolicy}</li>
     *   <li>从前端接口接收策略类型参数</li>
     *   <li>动态创建不同类型的拒绝策略</li>
     * </ul>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * // 从配置文件读取
     * String policyName = config.getString("onethread.executors[0].rejected-handler");
     * RejectedExecutionHandler handler = 
     *     RejectedPolicyTypeEnum.createPolicy(policyName);
     * 
     * // 输出示例：policyName = "CallerRunsPolicy"
     * // 结果：返回 ThreadPoolExecutor.CallerRunsPolicy 实例
     * 
     * 
     * // 在线程池构建中使用
     * ThreadPoolExecutor executor = new ThreadPoolExecutor(
     *     5, 10, 60L, TimeUnit.SECONDS,
     *     new LinkedBlockingQueue<>(100),
     *     Executors.defaultThreadFactory(),
     *     RejectedPolicyTypeEnum.createPolicy(policyName)  // 动态创建策略
     * );
     * }</pre>
     * 
     * <p><b>错误处理示例：</b>
     * <pre>{@code
     * try {
     *     RejectedExecutionHandler handler = 
     *         RejectedPolicyTypeEnum.createPolicy("InvalidPolicy");
     * } catch (IllegalArgumentException e) {
     *     // 处理无效的策略名称
     *     log.error("无效的拒绝策略名称: {}", policyName, e);
     *     // 使用默认策略
     *     handler = new ThreadPoolExecutor.AbortPolicy();
     * }
     * }</pre>
     *
     * @param rejectedPolicyName 拒绝策略类型名称（如 "CallerRunsPolicy"、"AbortPolicy"）
     * @return 对应的 {@link RejectedExecutionHandler} 实例
     * @throws IllegalArgumentException 如果策略类型名称不存在
     */
    public static RejectedExecutionHandler createPolicy(String rejectedPolicyName) {
        RejectedPolicyTypeEnum rejectedPolicyTypeEnum = NAME_TO_ENUM_MAP.get(rejectedPolicyName);
        if (rejectedPolicyTypeEnum != null) {
            return rejectedPolicyTypeEnum.rejectedHandler;
        }

        throw new IllegalArgumentException("No matching type of rejected execution was found: " + rejectedPolicyName);
    }
}
