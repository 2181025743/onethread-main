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

package com.nageoffer.onethread.core.toolkit;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 线程工厂构建器
 * <p>
 * 该类使用<b>建造者模式（Builder Pattern）</b>构建自定义的 {@link ThreadFactory}，
 * 支持灵活配置线程的各种属性，如线程名称、优先级、守护线程标识、异常处理器等。
 * 
 * <p><b>核心功能：</b>
 * <ul>
 *   <li><b>自定义线程命名：</b>为线程池中的每个线程设置有意义的名称（如 "onethread-pool-1"）</li>
 *   <li><b>线程属性配置：</b>设置线程优先级、守护线程标识等</li>
 *   <li><b>异常处理：</b>配置未捕获异常处理器，防止线程因异常静默退出</li>
 *   <li><b>线程计数：</b>自动为线程添加递增的序号，便于问题排查</li>
 * </ul>
 * 
 * <p><b>为什么需要自定义线程工厂？</b>
 * <ul>
 *   <li><b>可读性：</b>JDK 默认线程名为 "pool-1-thread-1"，无法区分业务用途；
 *       自定义名称如 "order-process-thread-1" 可以快速定位问题</li>
 *   <li><b>监控：</b>通过线程名可以在 jstack、jvisualvm 等工具中快速识别线程用途</li>
 *   <li><b>异常处理：</b>可以统一处理线程中未捕获的异常，记录日志或告警</li>
 *   <li><b>守护线程控制：</b>守护线程不会阻止 JVM 退出，适合后台任务</li>
 * </ul>
 * 
 * <p><b>使用示例：</b>
 * <pre>{@code
 * // 示例1：基础用法 - 只设置线程名前缀
 * ThreadFactory factory1 = ThreadFactoryBuilder.builder()
 *     .namePrefix("business-pool-")
 *     .build();
 * 
 * // 生成的线程名：business-pool-0, business-pool-1, business-pool-2...
 * 
 * 
 * // 示例2：完整配置 - 设置所有属性
 * ThreadFactory factory2 = ThreadFactoryBuilder.builder()
 *     .namePrefix("order-processor-")        // 线程名前缀
 *     .daemon(true)                          // 设为守护线程
 *     .priority(Thread.MAX_PRIORITY)         // 最高优先级
 *     .uncaughtExceptionHandler((t, e) -> {  // 异常处理器
 *         log.error("线程 {} 发生未捕获异常", t.getName(), e);
 *     })
 *     .build();
 * 
 * 
 * // 示例3：在线程池中使用
 * ThreadPoolExecutor executor = new ThreadPoolExecutor(
 *     5, 10, 60L, TimeUnit.SECONDS,
 *     new LinkedBlockingQueue<>(100),
 *     ThreadFactoryBuilder.builder()
 *         .namePrefix("async-task-")
 *         .daemon(false)
 *         .build(),
 *     new ThreadPoolExecutor.CallerRunsPolicy()
 * );
 * }</pre>
 * 
 * <p><b>设计模式：</b>建造者模式（Builder Pattern）
 * <br>通过链式调用逐步配置对象属性，最后调用 {@link #build()} 方法构建最终对象。
 * 
 * <p><b>线程安全性：</b>构建器本身不是线程安全的，但构建出的 {@link ThreadFactory} 是线程安全的。
 * 
 * @author 杨潇
 * @since 2025-04-20
 * @see ThreadFactory JDK线程工厂接口
 * @see ThreadPoolExecutorBuilder 线程池构建器
 * @see Thread.UncaughtExceptionHandler 未捕获异常处理器
 */
public class ThreadFactoryBuilder {

    /**
     * 基础线程工厂
     * <p>
     * 用于实际创建线程的底层工厂，默认使用 {@link Executors#defaultThreadFactory()}。
     * 可以通过 {@link #threadFactory(ThreadFactory)} 方法自定义基础工厂。
     * 
     * <p><b>作用：</b>代理模式的体现，在基础工厂创建的线程上叠加自定义属性。
     */
    private ThreadFactory backingThreadFactory;

    /**
     * 线程名前缀
     * <p>
     * 用于生成线程名称的前缀字符串，实际线程名为 {@code namePrefix + 计数器}。
     * 
     * <p><b>示例：</b>
     * <ul>
     *   <li>namePrefix = "onethread-" → 线程名：onethread-0, onethread-1, onethread-2...</li>
     *   <li>namePrefix = "async-task-" → 线程名：async-task-0, async-task-1...</li>
     * </ul>
     * 
     * <p><b>必填字段：</b>该字段不能为空，否则构建时会抛出异常。
     */
    private String namePrefix;

    /**
     * 是否为守护线程
     * <p>
     * 守护线程（Daemon Thread）是一种特殊类型的线程，当 JVM 中只剩下守护线程时，
     * JVM 会自动退出，不会等待守护线程执行完毕。
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li><b>设为 true（守护线程）：</b>适合后台任务，如定时清理、心跳检测等，
     *       不希望这些线程阻止 JVM 退出</li>
     *   <li><b>设为 false（用户线程，默认）：</b>适合业务任务，需要等待任务完成后才能退出 JVM</li>
     * </ul>
     * 
     * <p><b>注意：</b>如果未设置（为 null），则使用基础线程工厂的默认值。
     */
    private Boolean daemon;

    /**
     * 线程优先级
     * <p>
     * 线程优先级用于向线程调度器提示该线程的重要性，取值范围为 1~10。
     * 
     * <p><b>优先级常量：</b>
     * <ul>
     *   <li>{@link Thread#MIN_PRIORITY} = 1：最低优先级</li>
     *   <li>{@link Thread#NORM_PRIORITY} = 5：默认优先级</li>
     *   <li>{@link Thread#MAX_PRIORITY} = 10：最高优先级</li>
     * </ul>
     * 
     * <p><b>注意事项：</b>
     * <ul>
     *   <li>线程优先级只是向操作系统的"建议"，不保证一定生效</li>
     *   <li>不同操作系统对优先级的支持程度不同</li>
     *   <li>过度依赖优先级可能导致低优先级线程"饥饿"</li>
     *   <li>如果未设置（为 null），则使用基础线程工厂的默认值</li>
     * </ul>
     */
    private Integer priority;

    /**
     * 未捕获异常处理器
     * <p>
     * 当线程中抛出未被捕获的异常（Uncaught Exception）时，会调用该处理器进行处理。
     * 这对于防止线程静默退出、记录异常日志、触发告警等非常重要。
     * 
     * <p><b>典型用途：</b>
     * <ul>
     *   <li><b>日志记录：</b>记录异常信息到日志系统</li>
     *   <li><b>告警通知：</b>发送告警消息（如钉钉、邮件）</li>
     *   <li><b>异常统计：</b>统计异常发生频率和类型</li>
     *   <li><b>资源清理：</b>清理线程持有的资源</li>
     * </ul>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * .uncaughtExceptionHandler((thread, exception) -> {
     *     log.error("线程 {} 发生未捕获异常", thread.getName(), exception);
     *     // 可以在这里发送告警、统计异常等
     * })
     * }</pre>
     * 
     * <p><b>注意：</b>如果未设置（为 null），则使用 JVM 的默认处理器或线程组的处理器。
     */
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    /**
     * 创建 ThreadFactoryBuilder 实例
     * <p>
     * 这是构建器模式的入口方法，返回一个新的构建器实例，供链式调用配置。
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * ThreadFactory factory = ThreadFactoryBuilder.builder()
     *     .namePrefix("my-thread-")
     *     .daemon(true)
     *     .build();
     * }</pre>
     *
     * @return ThreadFactoryBuilder 的新实例
     */
    public static ThreadFactoryBuilder builder() {
        return new ThreadFactoryBuilder();
    }

    /**
     * 设置基础线程工厂
     * <p>
     * 指定用于实际创建线程的底层工厂。如果不设置，默认使用 {@link Executors#defaultThreadFactory()}。
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>需要在已有的线程工厂基础上叠加自定义属性</li>
     *   <li>需要使用特殊的线程创建逻辑（如虚拟线程、特定线程组等）</li>
     * </ul>
     *
     * @param backingThreadFactory 基础线程工厂
     * @return 当前构建器实例（支持链式调用）
     */
    public ThreadFactoryBuilder threadFactory(ThreadFactory backingThreadFactory) {
        this.backingThreadFactory = backingThreadFactory;
        return this;
    }

    /**
     * 设置线程名前缀
     * <p>
     * 该方法设置线程名称的前缀，实际线程名会在前缀后附加递增的计数器。
     * 
     * <p><b>命名规范建议：</b>
     * <ul>
     *   <li>使用有意义的业务名称，如 "order-processor-"、"payment-handler-"</li>
     *   <li>使用短横线或下划线分隔单词，如 "async-task-"</li>
     *   <li>避免过长的名称（建议不超过 20 个字符）</li>
     *   <li>建议以短横线或下划线结尾，与计数器区分（如 "thread-" 而非 "thread"）</li>
     * </ul>
     * 
     * <p><b>必填字段：</b>该字段不能为空，否则 {@link #build()} 时会抛出异常。
     *
     * @param namePrefix 线程名前缀（如 "onethread-"）
     * @return 当前构建器实例（支持链式调用）
     */
    public ThreadFactoryBuilder namePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
        return this;
    }

    /**
     * 设置是否为守护线程
     * <p>
     * 守护线程不会阻止 JVM 退出，适合后台任务。
     * 
     * <p><b>选择建议：</b>
     * <ul>
     *   <li><b>设为 true：</b>后台任务（监控、清理、心跳等），可以随 JVM 退出</li>
     *   <li><b>设为 false：</b>业务任务（订单处理、数据同步等），需要等待任务完成</li>
     * </ul>
     * 
     * <p><b>注意：</b>守护线程创建的子线程默认也是守护线程。
     *
     * @param daemon {@code true} 表示守护线程，{@code false} 表示用户线程
     * @return 当前构建器实例（支持链式调用）
     * @see Thread#setDaemon(boolean)
     */
    public ThreadFactoryBuilder daemon(boolean daemon) {
        this.daemon = daemon;
        return this;
    }

    /**
     * 设置线程优先级
     * <p>
     * 线程优先级用于向线程调度器提示该线程的重要性，但不保证一定生效。
     * 
     * <p><b>优先级范围：</b>1~10（{@link Thread#MIN_PRIORITY} ~ {@link Thread#MAX_PRIORITY}）
     * 
     * <p><b>使用建议：</b>
     * <ul>
     *   <li>大多数情况下使用默认优先级（5）即可</li>
     *   <li>仅在极少数场景下调整优先级（如实时任务）</li>
     *   <li>避免过度依赖优先级，应通过合理的线程池配置和任务设计来保证性能</li>
     * </ul>
     *
     * @param priority 线程优先级（1~10）
     * @return 当前构建器实例（支持链式调用）
     * @throws IllegalArgumentException 如果优先级不在 1~10 范围内
     * @see Thread#setPriority(int)
     */
    public ThreadFactoryBuilder priority(int priority) {
        // 参数校验：优先级必须在合法范围内
        if (priority < Thread.MIN_PRIORITY || priority > Thread.MAX_PRIORITY) {
            throw new IllegalArgumentException("The thread priority must be between 1 and 10.");
        }
        this.priority = priority;
        return this;
    }

    /**
     * 设置未捕获异常处理器
     * <p>
     * 当线程抛出未被捕获的异常时，会调用该处理器进行处理。
     * 这对于防止线程静默退出、记录异常日志、触发告警等非常重要。
     * 
     * <p><b>典型实现：</b>
     * <pre>{@code
     * .uncaughtExceptionHandler((thread, exception) -> {
     *     // 1. 记录日志
     *     log.error("线程 {} 发生未捕获异常", thread.getName(), exception);
     *     
     *     // 2. 发送告警（可选）
     *     alertService.sendAlert("线程异常", thread.getName(), exception);
     *     
     *     // 3. 统计异常（可选）
     *     metricsCollector.incrementExceptionCount(exception.getClass().getName());
     * })
     * }</pre>
     * 
     * <p><b>注意：</b>
     * <ul>
     *   <li>处理器中不应抛出异常，否则会导致异常被忽略</li>
     *   <li>处理器的执行是同步的，应避免执行耗时操作</li>
     *   <li>如果未设置，使用 JVM 默认处理器（通常只打印到 stderr）</li>
     * </ul>
     *
     * @param handler 未捕获异常处理器
     * @return 当前构建器实例（支持链式调用）
     * @see Thread#setUncaughtExceptionHandler(Thread.UncaughtExceptionHandler)
     */
    public ThreadFactoryBuilder uncaughtExceptionHandler(Thread.UncaughtExceptionHandler handler) {
        this.uncaughtExceptionHandler = handler;
        return this;
    }

    /**
     * 构建线程工厂实例
     * <p>
     * 根据配置的属性创建一个 {@link ThreadFactory} 实例。
     * 该工厂会在每次创建线程时应用所有配置的属性（线程名、优先级、守护线程标识等）。
     * 
     * <p><b>构建流程：</b>
     * <ol>
     *   <li>确定基础线程工厂（如果未设置则使用 {@link Executors#defaultThreadFactory()}）</li>
     *   <li>校验线程名前缀不能为空</li>
     *   <li>创建线程计数器（如果设置了线程名前缀）</li>
     *   <li>返回一个 Lambda 表达式实现的 ThreadFactory，在创建线程时应用所有配置</li>
     * </ol>
     * 
     * <p><b>线程创建过程：</b>
     * <pre>
     * 每次调用 factory.newThread(runnable) 时：
     * 1. 使用基础工厂创建线程
     * 2. 如果设置了线程名前缀：设置线程名为 "前缀 + 计数器"
     * 3. 如果设置了守护线程标识：调用 thread.setDaemon()
     * 4. 如果设置了优先级：调用 thread.setPriority()
     * 5. 如果设置了异常处理器：调用 thread.setUncaughtExceptionHandler()
     * 6. 返回配置完成的线程
     * </pre>
     * 
     * <p><b>线程安全性：</b>
     * <ul>
     *   <li>构建出的 ThreadFactory 是线程安全的（使用 {@link AtomicLong} 保证计数器原子性）</li>
     *   <li>可以在多线程环境中安全地调用 {@code newThread()} 方法</li>
     * </ul>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * ThreadFactory factory = ThreadFactoryBuilder.builder()
     *     .namePrefix("worker-")
     *     .daemon(true)
     *     .priority(Thread.NORM_PRIORITY)
     *     .build();
     * 
     * // 创建线程
     * Thread t1 = factory.newThread(() -> System.out.println("Task 1"));
     * // 线程名：worker-0
     * 
     * Thread t2 = factory.newThread(() -> System.out.println("Task 2"));
     * // 线程名：worker-1
     * }</pre>
     *
     * @return 配置完成的 ThreadFactory 实例
     * @throws IllegalArgumentException 如果线程名前缀为空或空字符串
     */
    public ThreadFactory build() {
        // 确定基础线程工厂：如果未设置则使用 JDK 默认工厂
        final ThreadFactory factory = (this.backingThreadFactory != null) 
                ? this.backingThreadFactory 
                : Executors.defaultThreadFactory();
        
        // 校验：线程名前缀不能为空（这是必填字段）
        Assert.notEmpty(namePrefix, "The thread name prefix cannot be empty or an empty string.");
        
        // 创建线程计数器（用于生成递增的线程序号）
        // 使用 AtomicLong 保证多线程环境下的原子性
        final AtomicLong count = (StrUtil.isNotBlank(namePrefix)) ? new AtomicLong(0) : null;

        // 返回 Lambda 表达式实现的 ThreadFactory
        // 每次调用 newThread() 时都会执行这个 Lambda
        return runnable -> {
            // 1. 使用基础工厂创建线程
            Thread thread = factory.newThread(runnable);

            // 2. 设置线程名（如果配置了前缀）
            if (count != null) {
                // 线程名 = 前缀 + 递增计数器
                // getAndIncrement() 返回当前值并自增，保证每个线程有唯一序号
                thread.setName(namePrefix + count.getAndIncrement());
            }

            // 3. 设置是否为守护线程（如果配置了）
            if (daemon != null) {
                thread.setDaemon(daemon);
            }

            // 4. 设置线程优先级（如果配置了）
            if (priority != null) {
                thread.setPriority(priority);
            }

            // 5. 设置未捕获异常处理器（如果配置了）
            if (uncaughtExceptionHandler != null) {
                thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
            }

            // 6. 返回配置完成的线程
            return thread;
        };
    }
}
