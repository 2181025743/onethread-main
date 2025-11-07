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
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.SynchronousQueue;

/**
 * 阻塞队列类型枚举
 * <p>
 * 该枚举定义了线程池支持的所有阻塞队列类型，并提供了统一的队列创建接口。
 * 通过枚举模式封装队列创建逻辑，避免了大量的 if-else 判断，提高了代码的可维护性和扩展性。
 * 
 * <p><b>核心功能：</b>
 * <ul>
 *   <li><b>类型安全：</b>使用枚举避免字符串硬编码，编译期检查类型错误</li>
 *   <li><b>统一创建：</b>提供统一的队列创建接口，简化线程池构建逻辑</li>
 *   <li><b>易于扩展：</b>新增队列类型只需添加枚举项，无需修改其他代码</li>
 *   <li><b>配置友好：</b>支持从配置文件通过字符串名称创建队列</li>
 * </ul>
 * 
 * <p><b>支持的队列类型对比：</b>
 * <table border="1" cellpadding="5">
 *   <tr>
 *     <th>队列类型</th>
 *     <th>底层结构</th>
 *     <th>容量</th>
 *     <th>特点</th>
 *     <th>适用场景</th>
 *   </tr>
 *   <tr>
 *     <td>ArrayBlockingQueue</td>
 *     <td>数组</td>
 *     <td>有界（必须指定）</td>
 *     <td>创建时分配内存，性能稳定</td>
 *     <td>内存敏感、性能要求高</td>
 *   </tr>
 *   <tr>
 *     <td>LinkedBlockingQueue</td>
 *     <td>链表</td>
 *     <td>有界或无界</td>
 *     <td>动态分配内存，灵活性高</td>
 *     <td>通用场景，最常用</td>
 *   </tr>
 *   <tr>
 *     <td>LinkedBlockingDeque</td>
 *     <td>双向链表</td>
 *     <td>有界或无界</td>
 *     <td>支持双端操作</td>
 *     <td>需要双端队列特性</td>
 *   </tr>
 *   <tr>
 *     <td>SynchronousQueue</td>
 *     <td>无缓冲</td>
 *     <td>0（无容量）</td>
 *     <td>生产者直接交付给消费者</td>
 *     <td>快速响应，任务立即执行</td>
 *   </tr>
 *   <tr>
 *     <td>LinkedTransferQueue</td>
 *     <td>链表</td>
 *     <td>无界</td>
 *     <td>支持transfer操作</td>
 *     <td>高性能场景</td>
 *   </tr>
 *   <tr>
 *     <td>PriorityBlockingQueue</td>
 *     <td>堆</td>
 *     <td>无界</td>
 *     <td>按优先级排序</td>
 *     <td>需要任务优先级</td>
 *   </tr>
 *   <tr>
 *     <td>ResizableCapacityLBQ</td>
 *     <td>链表</td>
 *     <td>可动态调整</td>
 *     <td>支持运行时调整容量</td>
 *     <td>动态线程池</td>
 *   </tr>
 * </table>
 * 
 * <p><b>设计模式：</b>
 * <ul>
 *   <li><b>工厂方法模式：</b>每个枚举项实现 {@link #of(Integer)} 方法，负责创建对应类型的队列</li>
 *   <li><b>策略模式：</b>不同的队列类型代表不同的任务缓冲策略</li>
 *   <li><b>单例模式：</b>每个队列类型的枚举实例是全局唯一的</li>
 * </ul>
 * 
 * <p><b>使用示例：</b>
 * <pre>{@code
 * // 示例1：通过枚举创建队列
 * BlockingQueue<Runnable> queue1 = 
 *     BlockingQueueTypeEnum.LINKED_BLOCKING_QUEUE.of(100);
 * 
 * 
 * // 示例2：通过字符串名称创建队列（配置文件场景）
 * String queueType = "LinkedBlockingQueue";
 * int capacity = 100;
 * BlockingQueue<Runnable> queue2 = 
 *     BlockingQueueTypeEnum.createBlockingQueue(queueType, capacity);
 * 
 * 
 * // 示例3：在线程池构建器中使用
 * ThreadPoolExecutor executor = ThreadPoolExecutorBuilder.builder()
 *     .workQueueType(BlockingQueueTypeEnum.ARRAY_BLOCKING_QUEUE)
 *     .workQueueCapacity(500)
 *     .build();
 * 
 * 
 * // 示例4：创建无界队列
 * BlockingQueue<Runnable> unbounded = 
 *     BlockingQueueTypeEnum.LINKED_BLOCKING_QUEUE.of();
 * }</pre>
 * 
 * <p><b>性能特点：</b>
 * <table border="1" cellpadding="5">
 *   <tr><th>操作</th><th>ArrayBlockingQueue</th><th>LinkedBlockingQueue</th><th>SynchronousQueue</th></tr>
 *   <tr><td>入队</td><td>O(1)</td><td>O(1)</td><td>O(1)</td></tr>
 *   <tr><td>出队</td><td>O(1)</td><td>O(1)</td><td>O(1)</td></tr>
 *   <tr><td>内存占用</td><td>固定（预分配）</td><td>动态（按需分配）</td><td>极小</td></tr>
 *   <tr><td>锁机制</td><td>单锁</td><td>双锁（读写分离）</td><td>无锁</td></tr>
 * </table>
 * 
 * @author 杨潇
 * @since 2025-04-20
 * @see BlockingQueue JDK阻塞队列接口
 * @see ThreadPoolExecutorBuilder 线程池构建器
 */
public enum BlockingQueueTypeEnum {

    /**
     * 数组阻塞队列
     * <p>
     * 基于数组实现的有界阻塞队列，必须在创建时指定容量。
     * 队列内部使用循环数组存储元素，创建时一次性分配所有内存空间。
     * 
     * <p><b>底层实现：</b>
     * <ul>
     *   <li>使用固定大小的数组存储元素</li>
     *   <li>使用 ReentrantLock 实现线程安全（单锁，读写共用）</li>
     *   <li>使用两个 Condition 实现等待/通知机制（notEmpty、notFull）</li>
     * </ul>
     * 
     * <p><b>特点：</b>
     * <ul>
     *   <li><b>有界队列：</b>容量固定，防止内存溢出</li>
     *   <li><b>FIFO顺序：</b>先进先出，保证公平性</li>
     *   <li><b>内存预分配：</b>创建时分配所有内存，避免运行时GC</li>
     *   <li><b>性能稳定：</b>数组访问速度快，性能可预测</li>
     * </ul>
     * 
     * <p><b>优势：</b>
     * <ul>
     *   <li>内存占用可控，适合内存敏感场景</li>
     *   <li>性能稳定，无GC抖动</li>
     *   <li>简单直观，易于理解</li>
     * </ul>
     * 
     * <p><b>劣势：</b>
     * <ul>
     *   <li>容量固定，无法动态调整</li>
     *   <li>单锁机制，高并发下性能不如 LinkedBlockingQueue</li>
     *   <li>大容量时创建开销大</li>
     * </ul>
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>任务数量相对稳定的场景</li>
     *   <li>内存敏感的应用</li>
     *   <li>对性能稳定性要求高的场景</li>
     * </ul>
     * 
     * @see ArrayBlockingQueue JDK实现类
     */
    ARRAY_BLOCKING_QUEUE("ArrayBlockingQueue") {
        @Override
        <T> BlockingQueue<T> of(Integer capacity) {
            return new ArrayBlockingQueue<>(capacity);
        }

        @Override
        <T> BlockingQueue<T> of() {
            return new ArrayBlockingQueue<>(DEFAULT_CAPACITY);
        }
    },

    /**
     * 链表阻塞队列（推荐，最常用）
     * <p>
     * 基于链表实现的阻塞队列，可以是有界或无界的。
     * 这是线程池中最常用的队列类型，兼顾了性能和灵活性。
     * 
     * <p><b>底层实现：</b>
     * <ul>
     *   <li>使用单向链表存储元素</li>
     *   <li>使用两个 ReentrantLock 分别控制入队和出队（读写分离）</li>
     *   <li>使用原子变量 AtomicInteger 记录元素数量</li>
     * </ul>
     * 
     * <p><b>特点：</b>
     * <ul>
     *   <li><b>灵活容量：</b>可指定容量（有界）或不指定（无界，容量为 Integer.MAX_VALUE）</li>
     *   <li><b>FIFO顺序：</b>先进先出</li>
     *   <li><b>动态内存：</b>按需分配内存，不会预分配</li>
     *   <li><b>双锁机制：</b>入队和出队使用不同的锁，提高并发性能</li>
     * </ul>
     * 
     * <p><b>优势：</b>
     * <ul>
     *   <li>高并发性能好（读写分离锁）</li>
     *   <li>灵活性高，容量可选</li>
     *   <li>内存按需分配，启动快</li>
     *   <li>通用性强，适合大多数场景</li>
     * </ul>
     * 
     * <p><b>劣势：</b>
     * <ul>
     *   <li>链表节点有额外内存开销</li>
     *   <li>无界模式可能导致内存溢出（如果不指定容量）</li>
     * </ul>
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>通用场景，适合大多数线程池应用</li>
     *   <li>任务数量波动较大的场景</li>
     *   <li>高并发场景</li>
     * </ul>
     * 
     * <p><b>注意：</b>强烈建议指定容量，避免使用无界模式导致内存溢出。
     * 
     * @see LinkedBlockingQueue JDK实现类
     */
    LINKED_BLOCKING_QUEUE("LinkedBlockingQueue") {
        @Override
        <T> BlockingQueue<T> of(Integer capacity) {
            return new LinkedBlockingQueue<>(capacity);
        }

        @Override
        <T> BlockingQueue<T> of() {
            // 无界队列，容量为 Integer.MAX_VALUE
            return new LinkedBlockingQueue<>();
        }
    },

    /**
     * 双向链表阻塞队列
     * <p>
     * 基于双向链表实现的阻塞队列，支持从两端插入和移除元素。
     * 可以作为队列（FIFO）或栈（LIFO）使用。
     * 
     * <p><b>底层实现：</b>
     * <ul>
     *   <li>使用双向链表存储元素</li>
     *   <li>使用单个 ReentrantLock 控制并发</li>
     *   <li>支持从队首和队尾操作</li>
     * </ul>
     * 
     * <p><b>特点：</b>
     * <ul>
     *   <li><b>双端操作：</b>支持 addFirst/addLast、removeFirst/removeLast</li>
     *   <li><b>灵活容量：</b>可指定容量或使用无界模式</li>
     *   <li><b>多用途：</b>可作为队列、栈或双端队列使用</li>
     * </ul>
     * 
     * <p><b>优势：</b>
     * <ul>
     *   <li>功能丰富，支持多种操作模式</li>
     *   <li>灵活性高</li>
     * </ul>
     * 
     * <p><b>劣势：</b>
     * <ul>
     *   <li>双向链表节点内存开销更大</li>
     *   <li>单锁机制，并发性能不如 LinkedBlockingQueue</li>
     * </ul>
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>需要双端队列特性的场景</li>
     *   <li>工作窃取（Work Stealing）算法</li>
     *   <li>需要LIFO（后进先出）语义的场景</li>
     * </ul>
     * 
     * @see LinkedBlockingDeque JDK实现类
     */
    LINKED_BLOCKING_DEQUE("LinkedBlockingDeque") {
        @Override
        <T> BlockingQueue<T> of(Integer capacity) {
            return new LinkedBlockingDeque<>(capacity);
        }

        @Override
        <T> BlockingQueue<T> of() {
            return new LinkedBlockingDeque<>();
        }
    },

    /**
     * 同步队列（无缓冲队列）
     * <p>
     * 一种特殊的阻塞队列，容量为0，不存储任何元素。
     * 生产者线程必须等待消费者线程来取走元素，反之亦然。
     * 
     * <p><b>底层实现：</b>
     * <ul>
     *   <li>不使用传统的队列结构</li>
     *   <li>使用栈或队列模式的传输器（Transferer）</li>
     *   <li>支持公平和非公平模式</li>
     * </ul>
     * 
     * <p><b>特点：</b>
     * <ul>
     *   <li><b>零容量：</b>不缓存任何元素</li>
     *   <li><b>直接交付：</b>生产者直接将任务交给消费者</li>
     *   <li><b>高吞吐：</b>适合高吞吐量场景</li>
     *   <li><b>无等待队列：</b>任务不会在队列中等待</li>
     * </ul>
     * 
     * <p><b>优势：</b>
     * <ul>
     *   <li>任务立即执行，响应速度快</li>
     *   <li>无内存占用（不存储元素）</li>
     *   <li>适合任务数量少但要求快速响应的场景</li>
     * </ul>
     * 
     * <p><b>劣势：</b>
     * <ul>
     *   <li>无缓冲能力，任务无法排队</li>
     *   <li>容易触发拒绝策略</li>
     *   <li>需要足够的线程资源</li>
     * </ul>
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>任务执行时间很短的场景</li>
     *   <li>要求快速响应的场景</li>
     *   <li>配合 CachedThreadPool 使用</li>
     *   <li>生产者消费者数量相当的场景</li>
     * </ul>
     * 
     * <p><b>注意：</b>
     * 使用时容量参数无效，队列始终为0容量。
     * 建议配合 {@link java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy} 拒绝策略使用。
     * 
     * @see SynchronousQueue JDK实现类
     */
    SYNCHRONOUS_QUEUE("SynchronousQueue") {
        @Override
        <T> BlockingQueue<T> of(Integer capacity) {
            // 注意：容量参数对 SynchronousQueue 无效，始终为0
            return new SynchronousQueue<>();
        }

        @Override
        <T> BlockingQueue<T> of() {
            return new SynchronousQueue<>();
        }
    },

    /**
     * 链表传输队列
     * <p>
     * 基于链表实现的无界阻塞队列，实现了 {@link java.util.concurrent.TransferQueue} 接口。
     * 支持 transfer 操作，可以让生产者等待消费者取走元素。
     * 
     * <p><b>底层实现：</b>
     * <ul>
     *   <li>使用无锁算法（CAS）实现</li>
     *   <li>基于"松弛双队列"（Dual Queue with Slack）算法</li>
     *   <li>性能优于 LinkedBlockingQueue</li>
     * </ul>
     * 
     * <p><b>特点：</b>
     * <ul>
     *   <li><b>无界队列：</b>容量不受限制</li>
     *   <li><b>transfer操作：</b>支持生产者等待消费者</li>
     *   <li><b>高性能：</b>使用无锁算法，性能优异</li>
     *   <li><b>FIFO顺序：</b>先进先出</li>
     * </ul>
     * 
     * <p><b>优势：</b>
     * <ul>
     *   <li>性能优于 LinkedBlockingQueue</li>
     *   <li>无锁算法，并发性能好</li>
     *   <li>支持特殊的 transfer 语义</li>
     * </ul>
     * 
     * <p><b>劣势：</b>
     * <ul>
     *   <li>无界队列，可能导致内存溢出</li>
     *   <li>实现复杂，难以理解</li>
     * </ul>
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>高性能要求的场景</li>
     *   <li>需要 transfer 语义的场景</li>
     *   <li>生产者消费者模式</li>
     * </ul>
     * 
     * <p><b>注意：</b>容量参数对此队列无效，始终为无界。
     * 
     * @see LinkedTransferQueue JDK实现类
     */
    LINKED_TRANSFER_QUEUE("LinkedTransferQueue") {
        @Override
        <T> BlockingQueue<T> of(Integer capacity) {
            // 注意：容量参数对 LinkedTransferQueue 无效，始终为无界
            return new LinkedTransferQueue<>();
        }

        @Override
        <T> BlockingQueue<T> of() {
            return new LinkedTransferQueue<>();
        }
    },

    /**
     * 优先级阻塞队列
     * <p>
     * 基于堆实现的无界阻塞队列，元素按优先级排序。
     * 元素必须实现 {@link Comparable} 接口或提供 {@link java.util.Comparator}。
     * 
     * <p><b>底层实现：</b>
     * <ul>
     *   <li>使用二叉堆存储元素</li>
     *   <li>使用 ReentrantLock 实现线程安全</li>
     *   <li>扩容机制：容量不足时自动扩容</li>
     * </ul>
     * 
     * <p><b>特点：</b>
     * <ul>
     *   <li><b>优先级排序：</b>元素按优先级出队，不是FIFO</li>
     *   <li><b>无界队列：</b>容量不受限制，自动扩容</li>
     *   <li><b>不接受null：</b>不允许插入null元素</li>
     *   <li><b>无序迭代：</b>迭代器不保证顺序</li>
     * </ul>
     * 
     * <p><b>优势：</b>
     * <ul>
     *   <li>支持任务优先级</li>
     *   <li>高优先级任务优先执行</li>
     *   <li>自动扩容，灵活性高</li>
     * </ul>
     * 
     * <p><b>劣势：</b>
     * <ul>
     *   <li>入队和出队操作复杂度为 O(log n)</li>
     *   <li>无界队列，可能导致内存溢出</li>
     *   <li>不保证相同优先级元素的FIFO顺序</li>
     * </ul>
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>需要任务优先级的场景</li>
     *   <li>VIP用户优先处理</li>
     *   <li>紧急任务优先执行</li>
     *   <li>任务调度系统</li>
     * </ul>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * // 任务类实现 Comparable 接口
     * class Task implements Comparable<Task>, Runnable {
     *     private int priority;
     *     
     *     @Override
     *     public int compareTo(Task other) {
     *         // 优先级高的排在前面
     *         return Integer.compare(other.priority, this.priority);
     *     }
     *     
     *     @Override
     *     public void run() {
     *         // 任务逻辑
     *     }
     * }
     * 
     * // 创建优先级队列
     * BlockingQueue<Task> queue = 
     *     BlockingQueueTypeEnum.PRIORITY_BLOCKING_QUEUE.of(100);
     * }</pre>
     * 
     * <p><b>注意：</b>
     * <ul>
     *   <li>元素必须可比较（实现Comparable或提供Comparator）</li>
     *   <li>容量参数只是初始容量，队列会自动扩容</li>
     *   <li>无界队列，需要注意内存使用</li>
     * </ul>
     * 
     * @see PriorityBlockingQueue JDK实现类
     */
    PRIORITY_BLOCKING_QUEUE("PriorityBlockingQueue") {
        @Override
        <T> BlockingQueue<T> of(Integer capacity) {
            // 注意：容量只是初始容量，队列会自动扩容
            return new PriorityBlockingQueue<>(capacity);
        }

        @Override
        <T> BlockingQueue<T> of() {
            return new PriorityBlockingQueue<>();
        }
    },

    /**
     * 可调整容量的链表阻塞队列（oneThread 特有）
     * <p>
     * 这是 oneThread 框架自定义的阻塞队列，继承自 {@link LinkedBlockingQueue}，
     * 核心特性是<b>支持运行时动态调整队列容量</b>，这是动态线程池的关键组件。
     * 
     * <p><b>核心功能：</b>
     * <ul>
     *   <li><b>动态调整容量：</b>运行时可以通过 {@link ResizableCapacityLinkedBlockingQueue#setCapacity(int)} 调整队列容量</li>
     *   <li><b>配置热更新：</b>配合配置中心（Nacos/Apollo）实现队列容量的热更新</li>
     *   <li><b>无需重启：</b>调整容量无需重启应用，立即生效</li>
     * </ul>
     * 
     * <p><b>实现原理：</b>
     * <ul>
     *   <li>通过反射修改 {@link LinkedBlockingQueue} 的 {@code capacity} 字段</li>
     *   <li>使用 {@link java.util.concurrent.atomic.AtomicInteger} 保证线程安全</li>
     *   <li>容量调整时会检查当前队列大小，确保调整合理</li>
     * </ul>
     * 
     * <p><b>特点：</b>
     * <ul>
     *   <li>继承自 LinkedBlockingQueue，保持原有性能和特性</li>
     *   <li>新增容量调整能力，满足动态调整需求</li>
     *   <li>线程安全，支持并发调整</li>
     * </ul>
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li><b>动态线程池：</b>这是动态线程池的推荐队列类型</li>
     *   <li><b>流量波动大：</b>需要根据流量动态调整队列容量的场景</li>
     *   <li><b>弹性伸缩：</b>配合云原生架构的弹性伸缩</li>
     * </ul>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * // 创建可调整容量的队列
     * ResizableCapacityLinkedBlockingQueue<Runnable> queue = 
     *     (ResizableCapacityLinkedBlockingQueue<Runnable>) 
     *     BlockingQueueTypeEnum.RESIZABLE_CAPACITY_LINKED_BLOCKING_QUEUE.of(100);
     * 
     * // 运行时动态调整容量
     * queue.setCapacity(500);  // 从100调整到500
     * 
     * 
     * // 在动态线程池中使用（推荐）
     * ThreadPoolExecutor executor = ThreadPoolExecutorBuilder.builder()
     *     .dynamicPool()  // 标记为动态线程池
     *     .workQueueType(BlockingQueueTypeEnum.RESIZABLE_CAPACITY_LINKED_BLOCKING_QUEUE)
     *     .workQueueCapacity(100)
     *     .build();
     * 
     * // 通过配置中心调整容量（自动触发）
     * // 修改Nacos配置后，框架会自动调用 queue.setCapacity(newCapacity)
     * }</pre>
     * 
     * <p><b>优势：</b>
     * <ul>
     *   <li>支持运行时调整，灵活性极高</li>
     *   <li>无需重启应用，业务无感知</li>
     *   <li>配合配置中心实现自动化运维</li>
     * </ul>
     * 
     * <p><b>注意事项：</b>
     * <ul>
     *   <li>调整容量时，如果新容量小于当前队列大小，不会移除已有元素</li>
     *   <li>容量调整是线程安全的，但频繁调整会有性能开销</li>
     *   <li>建议只在动态线程池中使用，普通线程池使用 LinkedBlockingQueue 即可</li>
     * </ul>
     * 
     * @see ResizableCapacityLinkedBlockingQueue oneThread自定义实现
     */
    RESIZABLE_CAPACITY_LINKED_BLOCKING_QUEUE("ResizableCapacityLinkedBlockingQueue") {
        @Override
        <T> BlockingQueue<T> of(Integer capacity) {
            return new ResizableCapacityLinkedBlockingQueue<>(capacity);
        }

        @Override
        <T> BlockingQueue<T> of() {
            return new ResizableCapacityLinkedBlockingQueue<>();
        }
    };

    /**
     * 队列类型的字符串名称
     * <p>
     * 用于配置文件、日志记录、前端展示等场景。
     * 该名称与枚举的实际类名保持一致，确保可读性。
     */
    @Getter
    private final String name;

    /**
     * 枚举构造函数
     * 
     * @param name 队列类型名称
     */
    BlockingQueueTypeEnum(String name) {
        this.name = name;
    }

    /**
     * 创建指定容量的阻塞队列
     * <p>
     * 这是一个抽象方法，由每个枚举项实现具体的队列创建逻辑。
     * 采用工厂方法模式，将队列创建逻辑封装在枚举内部。
     * 
     * <p><b>实现要求：</b>
     * <ul>
     *   <li>根据队列类型创建相应的 {@link BlockingQueue} 实例</li>
     *   <li>使用指定的容量参数（如果队列支持容量限制）</li>
     *   <li>确保线程安全（BlockingQueue 本身是线程安全的）</li>
     * </ul>
     * 
     * <p><b>注意：</b>
     * <ul>
     *   <li>对于无界队列（如 LinkedTransferQueue），容量参数可能被忽略</li>
     *   <li>对于零容量队列（如 SynchronousQueue），容量参数无效</li>
     * </ul>
     *
     * @param capacity 队列容量（有界队列使用，无界队列忽略）
     * @param <T>      队列元素类型
     * @return 指定类型和容量的阻塞队列实例
     */
    abstract <T> BlockingQueue<T> of(Integer capacity);

    /**
     * 创建默认容量的阻塞队列
     * <p>
     * 对于有界队列，使用 {@link #DEFAULT_CAPACITY} 作为默认容量。
     * 对于无界队列，创建无容量限制的队列。
     * 
     * <p><b>默认容量：</b>4096（适中的容量，平衡内存占用和任务缓冲能力）
     *
     * @param <T> 队列元素类型
     * @return 默认容量的阻塞队列实例
     */
    abstract <T> BlockingQueue<T> of();

    /**
     * 名称到枚举的映射表
     * <p>
     * 用于根据字符串名称快速查找对应的枚举实例。
     * 在静态初始化块中构建，避免运行时查找开销。
     * 
     * <p><b>用途：</b>
     * <ul>
     *   <li>从配置文件读取队列类型名称后，快速定位枚举</li>
     *   <li>避免使用 {@link #valueOf(String)} 方法的 IllegalArgumentException</li>
     *   <li>支持大小写不敏感的查找（如果需要）</li>
     * </ul>
     */
    private static final Map<String, BlockingQueueTypeEnum> NAME_TO_ENUM_MAP;

    /**
     * 静态初始化块：构建名称到枚举的映射表
     * <p>
     * 在类加载时执行一次，将所有枚举项的名称映射到对应的枚举实例。
     * 使用 HashMap 存储，查找效率为 O(1)。
     */
    static {
        final BlockingQueueTypeEnum[] values = BlockingQueueTypeEnum.values();
        NAME_TO_ENUM_MAP = new HashMap<>(values.length);
        for (BlockingQueueTypeEnum value : values) {
            NAME_TO_ENUM_MAP.put(value.name, value);
        }
    }

    /**
     * 根据名称和容量创建阻塞队列（公共工厂方法）
     * <p>
     * 这是创建阻塞队列的统一入口，支持从配置文件通过字符串名称创建队列。
     * 内部通过名称查找对应的枚举实例，然后调用枚举的 {@link #of(Integer)} 方法创建队列。
     * 
     * <p><b>执行流程：</b>
     * <ol>
     *   <li>根据队列名称从 {@link #NAME_TO_ENUM_MAP} 查找对应的枚举</li>
     *   <li>如果找到，调用枚举的 {@link #of(Integer)} 或 {@link #of()} 方法创建队列</li>
     *   <li>如果未找到，抛出 {@link IllegalArgumentException} 异常</li>
     * </ol>
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>从配置文件读取队列类型：{@code work-queue: LinkedBlockingQueue}</li>
     *   <li>从前端接口接收队列类型参数</li>
     *   <li>动态创建不同类型的队列</li>
     * </ul>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * // 从配置文件读取
     * String queueType = config.getString("onethread.executors[0].work-queue");
     * Integer capacity = config.getInt("onethread.executors[0].queue-capacity");
     * BlockingQueue<Runnable> queue = 
     *     BlockingQueueTypeEnum.createBlockingQueue(queueType, capacity);
     * 
     * // 输出示例：queueType = "LinkedBlockingQueue", capacity = 100
     * // 结果：创建容量为100的LinkedBlockingQueue
     * }</pre>
     *
     * @param blockingQueueName 队列类型名称（如 "LinkedBlockingQueue"）
     * @param capacity          队列容量（可以为 null，此时使用无界或默认容量）
     * @param <T>               队列元素类型
     * @return 指定类型和容量的阻塞队列实例
     * @throws IllegalArgumentException 如果队列类型名称不存在
     */
    public static <T> BlockingQueue<T> createBlockingQueue(String blockingQueueName, Integer capacity) {
        final BlockingQueue<T> queue = of(blockingQueueName, capacity);
        if (queue != null) {
            return queue;
        }

        throw new IllegalArgumentException("No matching type of blocking queue was found: " + blockingQueueName);
    }

    /**
     * 根据名称和容量创建阻塞队列（内部方法）
     * <p>
     * 这是一个内部辅助方法，封装了队列创建的核心逻辑。
     * 
     * <p><b>处理逻辑：</b>
     * <ul>
     *   <li>如果 capacity 为 null，调用 {@link #of()} 创建默认容量或无界队列</li>
     *   <li>如果 capacity 不为 null，调用 {@link #of(Integer)} 创建指定容量的队列</li>
     *   <li>如果队列类型不存在，返回 null</li>
     * </ul>
     *
     * @param blockingQueueName 队列类型名称
     * @param capacity          队列容量（可以为 null）
     * @param <T>               队列元素类型
     * @return 阻塞队列实例，如果队列类型不存在则返回 null
     */
    private static <T> BlockingQueue<T> of(String blockingQueueName, Integer capacity) {
        final BlockingQueueTypeEnum typeEnum = NAME_TO_ENUM_MAP.get(blockingQueueName);
        if (typeEnum == null) {
            return null;
        }

        // 根据容量参数选择创建方法
        return Objects.isNull(capacity) ? typeEnum.of() : typeEnum.of(capacity);
    }

    /**
     * 默认队列容量
     * <p>
     * 当创建有界队列但未指定容量时使用的默认值。
     * 
     * <p><b>容量选择理由：</b>
     * <ul>
     *   <li>4096 = 2^12，是2的幂次方，对内存分配友好</li>
     *   <li>对于大多数应用，4096个任务的缓冲足够应对突发流量</li>
     *   <li>内存占用适中（假设每个任务引用8字节，约32KB）</li>
     *   <li>避免设置过小导致频繁触发拒绝策略</li>
     *   <li>避免设置过大导致内存浪费或OOM</li>
     * </ul>
     * 
     * <p><b>建议：</b>在生产环境中，应根据实际业务情况配置合适的队列容量，而非使用默认值。
     */
    private static final int DEFAULT_CAPACITY = 4096;
}
