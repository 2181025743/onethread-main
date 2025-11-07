package com.nageoffer.onethread.core.toolkit;

import cn.hutool.core.lang.Assert;
import com.nageoffer.onethread.core.executor.OneThreadExecutor;
import com.nageoffer.onethread.core.executor.support.BlockingQueueTypeEnum;
import lombok.Getter;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池构建器
 * <p>
 * 该类使用<b>建造者模式（Builder Pattern）</b>简化 {@link ThreadPoolExecutor} 的创建过程，
 * 提供链式调用的 API，支持创建普通线程池和 oneThread 框架的动态线程池。
 * 
 * <p><b>核心功能：</b>
 * <ul>
 *   <li><b>简化线程池创建：</b>通过链式调用避免冗长的构造参数</li>
 *   <li><b>智能默认值：</b>根据 CPU 核心数自动计算合理的线程池参数</li>
 *   <li><b>支持动态线程池：</b>创建可运行时调整参数的 {@link OneThreadExecutor}</li>
 *   <li><b>类型安全：</b>通过枚举类型避免队列类型和拒绝策略的配置错误</li>
 * </ul>
 * 
 * <p><b>默认参数说明：</b>
 * <table border="1">
 *   <tr><th>参数</th><th>默认值</th><th>计算逻辑</th></tr>
 *   <tr><td>核心线程数</td><td>CPU核心数</td><td>{@code Runtime.getRuntime().availableProcessors()}</td></tr>
 *   <tr><td>最大线程数</td><td>核心数 * 1.5</td><td>{@code corePoolSize + (corePoolSize >> 1)}</td></tr>
 *   <tr><td>队列类型</td><td>LinkedBlockingQueue</td><td>有界阻塞队列</td></tr>
 *   <tr><td>队列容量</td><td>4096</td><td>适中容量，避免内存溢出</td></tr>
 *   <tr><td>拒绝策略</td><td>AbortPolicy</td><td>抛出异常，避免任务静默丢失</td></tr>
 *   <tr><td>空闲存活时间</td><td>30000秒</td><td>约8.3小时</td></tr>
 *   <tr><td>允许核心线程超时</td><td>false</td><td>核心线程常驻</td></tr>
 * </table>
 * 
 * <p><b>使用示例：</b>
 * <pre>{@code
 * // 示例1：创建最简单的线程池（使用所有默认值）
 * ThreadPoolExecutor executor1 = ThreadPoolExecutorBuilder.builder()
 *     .threadFactory("simple-pool")  // 只需指定线程名前缀
 *     .build();
 * 
 * 
 * // 示例2：创建自定义配置的线程池
 * ThreadPoolExecutor executor2 = ThreadPoolExecutorBuilder.builder()
 *     .corePoolSize(10)                    // 核心线程数
 *     .maximumPoolSize(20)                 // 最大线程数
 *     .workQueueCapacity(500)              // 队列容量
 *     .workQueueType(BlockingQueueTypeEnum.ARRAY_BLOCKING_QUEUE)  // 数组阻塞队列
 *     .threadFactory("order-processor")    // 线程名前缀
 *     .rejectedHandler(new ThreadPoolExecutor.CallerRunsPolicy())  // 拒绝策略
 *     .keepAliveTime(120L)                 // 空闲时间120秒
 *     .allowCoreThreadTimeOut(true)        // 允许核心线程超时
 *     .build();
 * 
 * 
 * // 示例3：创建oneThread动态线程池（可运行时调整参数）
 * ThreadPoolExecutor dynamicExecutor = ThreadPoolExecutorBuilder.builder()
 *     .dynamicPool()                       // 👈 关键：标记为动态线程池
 *     .threadPoolId("onethread-producer")  // 线程池唯一标识（动态池必需）
 *     .corePoolSize(8)
 *     .maximumPoolSize(16)
 *     .workQueueType(BlockingQueueTypeEnum.RESIZABLE_CAPACITY_LINKED_BLOCKING_QUEUE)  // 可调整容量的队列
 *     .threadFactory("producer-thread")
 *     .build();
 * 
 * 
 * // 示例4：为线程工厂配置更多属性
 * ThreadPoolExecutor executor4 = ThreadPoolExecutorBuilder.builder()
 *     .corePoolSize(5)
 *     .threadFactory("async-task", true)   // 设置为守护线程
 *     .build();
 * }</pre>
 * 
 * <p><b>动态线程池 vs 普通线程池：</b>
 * <table border="1">
 *   <tr><th>特性</th><th>普通线程池</th><th>动态线程池（{@link OneThreadExecutor}）</th></tr>
 *   <tr><td>参数调整</td><td>部分参数可调（核心数、最大数）</td><td>所有参数可运行时调整</td></tr>
 *   <tr><td>配置中心集成</td><td>不支持</td><td>支持 Nacos/Apollo</td></tr>
 *   <tr><td>监控告警</td><td>需手动实现</td><td>内置监控和告警</td></tr>
 *   <tr><td>队列容量调整</td><td>不支持</td><td>支持（使用 ResizableCapacityLinkedBlockingQueue）</td></tr>
 *   <tr><td>性能开销</td><td>无额外开销</td><td>有轻微的监控开销</td></tr>
 * </table>
 * 
 * <p><b>设计模式：</b>建造者模式（Builder Pattern）
 * <br>通过链式调用逐步配置线程池参数，提高代码可读性和可维护性。
 * 
 * <p><b>线程安全性：</b>构建器本身不是线程安全的，但构建出的 {@link ThreadPoolExecutor} 是线程安全的。
 * 
 * @author 杨潇
 * @since 2025-04-20
 * @see ThreadPoolExecutor JDK线程池
 * @see OneThreadExecutor oneThread动态线程池
 * @see ThreadFactoryBuilder 线程工厂构建器
 * @see BlockingQueueTypeEnum 阻塞队列类型枚举
 */
@Getter
public class ThreadPoolExecutorBuilder {

    /**
     * 线程池唯一标识
     * <p>
     * 用于在 oneThread 框架中标识和管理线程池，特别是动态线程池必须设置该值。
     * 该标识会用于：
     * <ul>
     *   <li>配置中心配置项匹配（如 Nacos 中的 thread-pool-id）</li>
     *   <li>监控数据上报和日志记录</li>
     *   <li>线程池注册表（{@link com.nageoffer.onethread.core.executor.OneThreadRegistry}）查找</li>
     * </ul>
     * 
     * <p><b>命名建议：</b>使用短横线分隔的小写字母，如 "order-processor"、"message-consumer"
     * 
     * <p><b>注意：</b>动态线程池（{@link OneThreadExecutor}）必须设置该值。
     */
    private String threadPoolId;

    /**
     * 核心线程数
     * <p>
     * 线程池中始终保持的最小线程数量（即使这些线程处于空闲状态）。
     * 
     * <p><b>默认值：</b>CPU核心数（{@code Runtime.getRuntime().availableProcessors()}）
     * 
     * <p><b>配置建议：</b>
     * <ul>
     *   <li><b>CPU密集型任务：</b>核心数 = CPU核心数 或 CPU核心数+1</li>
     *   <li><b>IO密集型任务：</b>核心数 = CPU核心数 * 2 或更多</li>
     *   <li><b>混合型任务：</b>根据实际测试结果调整</li>
     * </ul>
     * 
     * <p><b>注意：</b>
     * <ul>
     *   <li>如果设置了 {@link #allowCoreThreadTimeOut} 为 true，核心线程空闲超时后也会被回收</li>
     *   <li>核心线程数必须 <= 最大线程数</li>
     * </ul>
     */
    private Integer corePoolSize = Runtime.getRuntime().availableProcessors();

    /**
     * 最大线程数
     * <p>
     * 线程池允许创建的最大线程数量。当队列满时，会创建新线程直到达到此值。
     * 
     * <p><b>默认值：</b>核心数 * 1.5（{@code corePoolSize + (corePoolSize >> 1)}）
     * 
     * <p><b>作用：</b>
     * <ul>
     *   <li>作为线程池的"弹性缓冲"，应对突发流量</li>
     *   <li>防止无限制地创建线程导致系统崩溃</li>
     * </ul>
     * 
     * <p><b>配置建议：</b>
     * <ul>
     *   <li>对于稳定负载，可以设置为与核心数相同</li>
     *   <li>对于波动负载，设置为核心数的 1.5~2 倍</li>
     *   <li>避免设置过大，否则可能导致上下文切换开销增加</li>
     * </ul>
     */
    private Integer maximumPoolSize = corePoolSize + (corePoolSize >> 1);

    /**
     * 阻塞队列类型
     * <p>
     * 用于存放待执行任务的阻塞队列类型。不同类型的队列有不同的特性和适用场景。
     * 
     * <p><b>默认值：</b>{@link BlockingQueueTypeEnum#LINKED_BLOCKING_QUEUE}（有界链表队列）
     * 
     * <p><b>队列类型对比：</b>
     * <table border="1">
     *   <tr><th>队列类型</th><th>底层结构</th><th>特点</th><th>适用场景</th></tr>
     *   <tr><td>LinkedBlockingQueue</td><td>链表</td><td>有界，默认容量Integer.MAX_VALUE</td><td>通用场景</td></tr>
     *   <tr><td>ArrayBlockingQueue</td><td>数组</td><td>有界，创建时指定容量</td><td>内存敏感场景</td></tr>
     *   <tr><td>SynchronousQueue</td><td>无缓冲</td><td>容量为0，直接交付</td><td>任务立即执行</td></tr>
     *   <tr><td>PriorityBlockingQueue</td><td>堆</td><td>优先级队列，无界</td><td>需要优先级</td></tr>
     *   <tr><td>ResizableCapacityLinkedBlockingQueue</td><td>链表</td><td>可动态调整容量</td><td>动态线程池</td></tr>
     * </table>
     * 
     * @see BlockingQueueTypeEnum 队列类型枚举
     */
    private BlockingQueueTypeEnum workQueueType = BlockingQueueTypeEnum.LINKED_BLOCKING_QUEUE;

    /**
     * 队列容量
     * <p>
     * 阻塞队列的最大容量，决定了有多少任务可以排队等待执行。
     * 
     * <p><b>默认值：</b>4096
     * 
     * <p><b>影响：</b>
     * <ul>
     *   <li><b>容量过小：</b>容易触发拒绝策略，可能丢失任务</li>
     *   <li><b>容量过大：</b>任务堆积严重，响应时间变长，可能导致内存溢出</li>
     * </ul>
     * 
     * <p><b>配置建议：</b>
     * <ul>
     *   <li>根据任务处理速度和内存容量综合考虑</li>
     *   <li>建议设置为有限值（如 100~10000），避免无限堆积</li>
     *   <li>配合监控和告警，及时发现队列堆积问题</li>
     * </ul>
     * 
     * <p><b>注意：</b>如果使用 {@link BlockingQueueTypeEnum#SYNCHRONOUS_QUEUE}，该值无效（容量为0）。
     */
    private Integer workQueueCapacity = 4096;

    /**
     * 拒绝策略
     * <p>
     * 当线程池和队列都满时，对新任务的处理策略。
     * 
     * <p><b>默认值：</b>{@link ThreadPoolExecutor.AbortPolicy}（抛出异常）
     * 
     * <p><b>JDK 内置策略：</b>
     * <table border="1">
     *   <tr><th>策略</th><th>行为</th><th>适用场景</th></tr>
     *   <tr><td>AbortPolicy</td><td>抛出 RejectedExecutionException</td><td>快速失败，适合关键任务</td></tr>
     *   <tr><td>CallerRunsPolicy</td><td>由调用线程执行任务</td><td>降低提交速度，防止丢失</td></tr>
     *   <tr><td>DiscardPolicy</td><td>静默丢弃任务</td><td>对任务丢失不敏感的场景</td></tr>
     *   <tr><td>DiscardOldestPolicy</td><td>丢弃队列最旧的任务</td><td>新任务优先级更高的场景</td></tr>
     * </table>
     * 
     * <p><b>选择建议：</b>
     * <ul>
     *   <li><b>重要任务：</b>使用 AbortPolicy 或 CallerRunsPolicy，确保任务不丢失</li>
     *   <li><b>可丢失任务：</b>使用 DiscardPolicy，避免影响系统稳定性</li>
     *   <li><b>自定义策略：</b>实现 {@link RejectedExecutionHandler} 接口</li>
     * </ul>
     */
    private RejectedExecutionHandler rejectedHandler = new ThreadPoolExecutor.AbortPolicy();

    /**
     * 线程工厂
     * <p>
     * 用于创建新线程的工厂，控制线程的名称、优先级、守护线程标识等属性。
     * 
     * <p><b>重要性：</b>
     * <ul>
     *   <li><b>可读性：</b>设置有意义的线程名，便于问题排查</li>
     *   <li><b>监控：</b>通过线程名在 jstack、jvisualvm 等工具中识别线程</li>
     *   <li><b>异常处理：</b>统一处理未捕获的异常</li>
     * </ul>
     * 
     * <p><b>注意：</b>该字段为必填项，必须通过 {@link #threadFactory} 方法设置，否则构建时会抛出异常。
     * 
     * @see ThreadFactoryBuilder 线程工厂构建器
     */
    private ThreadFactory threadFactory;

    /**
     * 线程空闲存活时间
     * <p>
     * 当线程数超过核心线程数时，多余的空闲线程在被终止前等待新任务的最长时间。
     * 
     * <p><b>默认值：</b>30000秒（约8.3小时）
     * 
     * <p><b>单位：</b>秒（在 {@link #build()} 方法中会转换为 {@link TimeUnit#SECONDS}）
     * 
     * <p><b>作用：</b>
     * <ul>
     *   <li>控制线程池的"弹性收缩"能力</li>
     *   <li>在流量降低后回收多余线程，节省系统资源</li>
     * </ul>
     * 
     * <p><b>配置建议：</b>
     * <ul>
     *   <li><b>短时任务：</b>设置较短时间（如 60秒），快速回收线程</li>
     *   <li><b>长时任务：</b>设置较长时间（如 300秒），避免频繁创建销毁</li>
     *   <li>如果核心线程数 = 最大线程数，该参数无实际作用</li>
     * </ul>
     * 
     * <p><b>注意：</b>默认情况下，该参数只对超过核心线程数的线程生效。
     * 如果设置了 {@link #allowCoreThreadTimeOut} 为 true，核心线程也会应用此超时规则。
     */
    private Long keepAliveTime = 30000L;

    /**
     * 是否允许核心线程超时
     * <p>
     * 控制核心线程在空闲时是否会被回收。
     * 
     * <p><b>默认值：</b>false（核心线程常驻，不会超时回收）
     * 
     * <p><b>行为差异：</b>
     * <ul>
     *   <li><b>false（默认）：</b>核心线程始终存活，即使空闲也不回收</li>
     *   <li><b>true：</b>核心线程空闲超过 {@link #keepAliveTime} 后也会被回收</li>
     * </ul>
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li><b>设为 true：</b>任务量波动大，希望在空闲时完全回收线程，节省资源</li>
     *   <li><b>设为 false：</b>任务量相对稳定，希望保持核心线程常驻，减少创建销毁开销</li>
     * </ul>
     * 
     * <p><b>注意：</b>如果设为 true，线程池可能在空闲期间完全没有线程（线程数降为0）。
     */
    private boolean allowCoreThreadTimeOut = false;

    /**
     * 动态线程池标识
     * <p>
     * 标记当前构建的是否为 oneThread 框架的动态线程池（{@link OneThreadExecutor}）。
     * 
     * <p><b>默认值：</b>false（构建普通的 {@link ThreadPoolExecutor}）
     * 
     * <p><b>动态线程池特性：</b>
     * <ul>
     *   <li>支持运行时调整所有参数（核心数、最大数、队列容量、拒绝策略等）</li>
     *   <li>与配置中心（Nacos/Apollo）集成，支持远程配置下发</li>
     *   <li>内置监控和告警功能</li>
     *   <li>支持优雅关闭和最大等待时间</li>
     * </ul>
     * 
     * <p><b>使用方法：</b>调用 {@link #dynamicPool()} 方法将此字段设为 true。
     */
    private boolean dynamicPool = false;

    /**
     * 最大等待时间（毫秒）
     * <p>
     * 线程池关闭时等待所有任务完成的最长时间（毫秒）。
     * 仅在动态线程池（{@link OneThreadExecutor}）中有效。
     * 
     * <p><b>默认值：</b>0（不等待，立即关闭）
     * 
     * <p><b>作用：</b>
     * <ul>
     *   <li>提供优雅关闭能力，给正在执行的任务一定的完成时间</li>
     *   <li>超时后强制关闭，防止无限等待</li>
     * </ul>
     * 
     * <p><b>配置建议：</b>
     * <ul>
     *   <li>根据任务的平均执行时间设置合理的等待时间</li>
     *   <li>如 设置为 30000（30秒），适合执行时间较短的任务</li>
     * </ul>
     */
    private long awaitTerminationMillis = 0L;

    /**
     * 设置构建线程池为动态线程池
     * <p>
     * 调用该方法后，{@link #build()} 会创建 {@link OneThreadExecutor} 而非普通的 {@link ThreadPoolExecutor}。
     * 动态线程池支持运行时调整参数、监控告警等高级特性。
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * ThreadPoolExecutor executor = ThreadPoolExecutorBuilder.builder()
     *     .dynamicPool()                       // 👈 标记为动态线程池
     *     .threadPoolId("onethread-producer")  // 动态池必须设置ID
     *     .corePoolSize(10)
     *     .threadFactory("producer")
     *     .build();
     * }</pre>
     * 
     * <p><b>注意：</b>
     * <ul>
     *   <li>动态线程池必须设置 {@link #threadPoolId}</li>
     *   <li>建议使用 {@link BlockingQueueTypeEnum#RESIZABLE_CAPACITY_LINKED_BLOCKING_QUEUE} 队列</li>
     * </ul>
     *
     * @return 当前构建器实例（支持链式调用）
     */
    public ThreadPoolExecutorBuilder dynamicPool() {
        this.dynamicPool = true;
        return this;
    }

    /**
     * 设置线程池唯一标识
     * <p>
     * 该标识用于在 oneThread 框架中识别和管理线程池，特别是动态线程池必须设置。
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>动态线程池的唯一标识（必填）</li>
     *   <li>配置中心配置项匹配（如 Nacos 中的 thread-pool-id）</li>
     *   <li>监控数据上报的标识</li>
     *   <li>日志记录中的线程池标识</li>
     * </ul>
     *
     * @param threadPoolId 线程池唯一标识（如 "order-processor"、"message-consumer"）
     * @return 当前构建器实例（支持链式调用）
     */
    public ThreadPoolExecutorBuilder threadPoolId(String threadPoolId) {
        this.threadPoolId = threadPoolId;
        return this;
    }

    /**
     * 设置核心线程数
     * <p>
     * 线程池中始终保持的最小线程数量。
     * 
     * <p><b>配置建议：</b>
     * <ul>
     *   <li>CPU密集型：核心数 = CPU核心数</li>
     *   <li>IO密集型：核心数 = CPU核心数 * 2</li>
     * </ul>
     *
     * @param corePoolSize 核心线程数（必须 > 0）
     * @return 当前构建器实例（支持链式调用）
     */
    public ThreadPoolExecutorBuilder corePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
        return this;
    }

    /**
     * 设置最大线程数
     * <p>
     * 线程池允许创建的最大线程数量。
     * 
     * <p><b>注意：</b>必须 >= 核心线程数
     *
     * @param maximumPoolSize 最大线程数（必须 >= corePoolSize）
     * @return 当前构建器实例（支持链式调用）
     */
    public ThreadPoolExecutorBuilder maximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
        return this;
    }

    /**
     * 设置阻塞队列容量
     * <p>
     * 决定了有多少任务可以排队等待执行。
     * 
     * <p><b>配置建议：</b>设置为有限值（如 100~10000），避免无限堆积导致内存溢出。
     *
     * @param workQueueCapacity 阻塞队列容量（必须 >= 0）
     * @return 当前构建器实例（支持链式调用）
     */
    public ThreadPoolExecutorBuilder workQueueCapacity(int workQueueCapacity) {
        this.workQueueCapacity = workQueueCapacity;
        return this;
    }

    /**
     * 设置阻塞队列类型
     * <p>
     * 不同类型的队列有不同的性能特征和适用场景。
     * 
     * <p><b>推荐选择：</b>
     * <ul>
     *   <li>通用场景：LinkedBlockingQueue</li>
     *   <li>动态线程池：ResizableCapacityLinkedBlockingQueue</li>
     *   <li>无缓冲：SynchronousQueue</li>
     * </ul>
     *
     * @param workQueueType 阻塞队列类型枚举
     * @return 当前构建器实例（支持链式调用）
     * @see BlockingQueueTypeEnum 队列类型枚举
     */
    public ThreadPoolExecutorBuilder workQueueType(BlockingQueueTypeEnum workQueueType) {
        this.workQueueType = workQueueType;
        return this;
    }

    /**
     * 设置线程工厂（简化版本，只需指定线程名前缀）
     * <p>
     * 内部会使用 {@link ThreadFactoryBuilder} 创建线程工厂。
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * .threadFactory("order-processor")
     * // 生成的线程名：order-processor-0, order-processor-1...
     * }</pre>
     *
     * @param namePrefix 线程名前缀（如 "onethread-"）
     * @return 当前构建器实例（支持链式调用）
     */
    public ThreadPoolExecutorBuilder threadFactory(String namePrefix) {
        this.threadFactory = ThreadFactoryBuilder.builder()
                .namePrefix(namePrefix)
                .build();
        return this;
    }

    /**
     * 设置线程工厂（支持设置守护线程标识）
     * <p>
     * 快速设置线程工厂，封装常用参数以降低构建门槛。
     * 出于实用主义，仅暴露常用的 namePrefix 和 daemon 参数。
     * 若需要更细粒度的控制（如优先级、异常处理器），请使用 {@link #threadFactory(ThreadFactory)}。
     *
     * @param namePrefix 线程名前缀（如 "onethread-"），最终线程名为：onethread-0、onethread-1...
     * @param daemon     是否为守护线程（true 表示不会阻止 JVM 退出）
     * @return 当前构建器实例（支持链式调用）
     */
    public ThreadPoolExecutorBuilder threadFactory(String namePrefix, Boolean daemon) {
        this.threadFactory = ThreadFactoryBuilder.builder()
                .namePrefix(namePrefix)
                .daemon(daemon)
                .build();
        return this;
    }

    /**
     * 设置线程工厂（自定义 ThreadFactory 实例）
     * <p>
     * 使用完全自定义的线程工厂，提供最大的灵活性。
     *
     * @param threadFactory 自定义线程工厂
     * @return 当前构建器实例（支持链式调用）
     */
    public ThreadPoolExecutorBuilder threadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        return this;
    }

    /**
     * 设置拒绝策略
     * <p>
     * 当线程池和队列都满时，对新任务的处理策略。
     * 
     * <p><b>常用策略：</b>
     * <ul>
     *   <li>AbortPolicy - 抛出异常（默认）</li>
     *   <li>CallerRunsPolicy - 由调用线程执行</li>
     *   <li>DiscardPolicy - 静默丢弃</li>
     *   <li>DiscardOldestPolicy - 丢弃最旧任务</li>
     * </ul>
     *
     * @param rejectedHandler 拒绝策略实例
     * @return 当前构建器实例（支持链式调用）
     * @see ThreadPoolExecutor.AbortPolicy 抛出异常策略
     * @see ThreadPoolExecutor.CallerRunsPolicy 调用者运行策略
     * @see ThreadPoolExecutor.DiscardPolicy 丢弃策略
     * @see ThreadPoolExecutor.DiscardOldestPolicy 丢弃最旧策略
     */
    public ThreadPoolExecutorBuilder rejectedHandler(RejectedExecutionHandler rejectedHandler) {
        this.rejectedHandler = rejectedHandler;
        return this;
    }

    /**
     * 设置线程空闲存活时间
     * <p>
     * 当线程数超过核心线程数时，多余的空闲线程在被终止前等待新任务的最长时间。
     *
     * @param keepAliveTime 存活时间（单位：秒）
     * @return 当前构建器实例（支持链式调用）
     */
    public ThreadPoolExecutorBuilder keepAliveTime(long keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
        return this;
    }

    /**
     * 设置是否允许核心线程超时
     * <p>
     * 控制核心线程在空闲时是否会被回收。
     * 
     * <p><b>注意：</b>如果设为 true，线程池可能在空闲期间完全没有线程。
     *
     * @param allowCoreThreadTimeOut true 表示允许核心线程超时回收
     * @return 当前构建器实例（支持链式调用）
     */
    public ThreadPoolExecutorBuilder allowCoreThreadTimeOut(boolean allowCoreThreadTimeOut) {
        this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
        return this;
    }

    /**
     * 设置最大等待时间
     * <p>
     * 线程池关闭时等待所有任务完成的最长时间。
     * 仅在动态线程池中有效。
     *
     * @param awaitTerminationMillis 最大等待时间（毫秒）
     * @return 当前构建器实例（支持链式调用）
     */
    public ThreadPoolExecutorBuilder awaitTerminationMillis(long awaitTerminationMillis) {
        this.awaitTerminationMillis = awaitTerminationMillis;
        return this;
    }

    /**
     * 创建线程池构建器实例
     * <p>
     * 这是建造者模式的入口方法，返回一个新的构建器实例。
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * ThreadPoolExecutor executor = ThreadPoolExecutorBuilder.builder()
     *     .corePoolSize(10)
     *     .threadFactory("my-pool")
     *     .build();
     * }</pre>
     *
     * @return ThreadPoolExecutorBuilder 的新实例
     */
    public static ThreadPoolExecutorBuilder builder() {
        return new ThreadPoolExecutorBuilder();
    }

    /**
     * 构建线程池实例
     * <p>
     * 根据配置的参数创建 {@link ThreadPoolExecutor} 或 {@link OneThreadExecutor} 实例。
     * 
     * <p><b>构建流程：</b>
     * <ol>
     *   <li>根据队列类型和容量创建阻塞队列</li>
     *   <li>处理拒绝策略（如果未设置则使用默认的 AbortPolicy）</li>
     *   <li>校验线程工厂不能为空</li>
     *   <li>根据 {@link #dynamicPool} 标识创建相应类型的线程池</li>
     *   <li>设置核心线程超时规则</li>
     *   <li>返回配置完成的线程池</li>
     * </ol>
     * 
     * <p><b>普通线程池 vs 动态线程池：</b>
     * <ul>
     *   <li><b>dynamicPool = false：</b>创建 {@link ThreadPoolExecutor}（JDK 标准线程池）</li>
     *   <li><b>dynamicPool = true：</b>创建 {@link OneThreadExecutor}（oneThread 动态线程池）</li>
     * </ul>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * // 创建普通线程池
     * ThreadPoolExecutor normalExecutor = ThreadPoolExecutorBuilder.builder()
     *     .corePoolSize(10)
     *     .threadFactory("normal-pool")
     *     .build();
     * 
     * // 创建动态线程池
     * ThreadPoolExecutor dynamicExecutor = ThreadPoolExecutorBuilder.builder()
     *     .dynamicPool()
     *     .threadPoolId("dynamic-pool")
     *     .corePoolSize(10)
     *     .threadFactory("dynamic-pool")
     *     .build();
     * }</pre>
     * 
     * <p><b>注意事项：</b>
     * <ul>
     *   <li>线程工厂（{@link #threadFactory}）必须设置，否则抛出异常</li>
     *   <li>动态线程池建议设置 {@link #threadPoolId}</li>
     *   <li>动态线程池建议使用 {@link BlockingQueueTypeEnum#RESIZABLE_CAPACITY_LINKED_BLOCKING_QUEUE}</li>
     * </ul>
     *
     * @return 配置完成的线程池实例（{@link ThreadPoolExecutor} 或 {@link OneThreadExecutor}）
     * @throws IllegalArgumentException 如果线程工厂为 null
     */
    public ThreadPoolExecutor build() {
        // 1. 创建指定类型和容量的阻塞队列
        // BlockingQueueTypeEnum 会根据队列类型创建相应的队列实例
        BlockingQueue<Runnable> blockingQueue = BlockingQueueTypeEnum.createBlockingQueue(
                workQueueType.getName(), 
                workQueueCapacity
        );

        // 2. 设置拒绝策略，如果未指定则使用默认的 AbortPolicy
        // 使用 Optional.ofNullable 来安全处理可能为 null 的拒绝策略
        // ofNullable 方法可以接受 null 值，如果 this.rejectedHandler 为 null，则返回空的 Optional
        // 然后通过 orElseGet 提供默认的拒绝策略 ThreadPoolExecutor.AbortPolicy
        RejectedExecutionHandler rejectedHandler = Optional.ofNullable(this.rejectedHandler)
                .orElseGet(ThreadPoolExecutor.AbortPolicy::new);

        // 3. 验证线程工厂不能为空（这是必填参数）
        // 线程工厂负责创建线程，没有它线程池无法运行
        Assert.notNull(threadFactory, "The thread factory cannot be null.");

        ThreadPoolExecutor threadPoolExecutor;
        
        // 4. 根据是否为动态线程池创建不同类型的线程池实例
        if (dynamicPool) {
            // 创建 oneThread 动态线程池（支持运行时调整参数、监控告警等高级特性）
            threadPoolExecutor = new OneThreadExecutor(
                    threadPoolId,              // 线程池唯一标识（用于配置中心匹配和监控）
                    corePoolSize,              // 核心线程数
                    maximumPoolSize,           // 最大线程数
                    keepAliveTime,             // 空闲存活时间
                    TimeUnit.SECONDS,          // 时间单位（秒）
                    blockingQueue,             // 阻塞队列
                    threadFactory,             // 线程工厂
                    rejectedHandler,           // 拒绝策略
                    awaitTerminationMillis     // 最大等待时间（优雅关闭使用）
            );
        } else {
            // 创建 JDK 标准线程池（性能更好，但不支持动态调整）
            threadPoolExecutor = new ThreadPoolExecutor(
                    corePoolSize,              // 核心线程数
                    maximumPoolSize,           // 最大线程数
                    keepAliveTime,             // 空闲存活时间
                    TimeUnit.SECONDS,          // 时间单位（秒）
                    blockingQueue,             // 阻塞队列
                    threadFactory,             // 线程工厂
                    rejectedHandler            // 拒绝策略
            );
        }

        // 5. 设置核心线程是否允许超时
        // 如果设为 true，核心线程空闲超过 keepAliveTime 后也会被回收
        threadPoolExecutor.allowCoreThreadTimeOut(allowCoreThreadTimeOut);
        
        // 6. 返回配置完成的线程池
        return threadPoolExecutor;
    }
}
