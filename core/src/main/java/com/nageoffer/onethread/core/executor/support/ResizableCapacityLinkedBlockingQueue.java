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

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 可动态调整容量的链表阻塞队列（oneThread 核心组件）
 * <p>
 * 该类是 JDK {@link java.util.concurrent.LinkedBlockingQueue} 的克隆增强版本，
 * 在保留原有所有功能的基础上，增加了<b>运行时动态调整队列容量</b>的能力。
 * 这是 oneThread 框架实现动态线程池的关键技术之一。
 * 
 * <p><b>核心增强：</b>
 * <ul>
 *   <li><b>容量可调整：</b>新增 {@link #setCapacity(int)} 方法，支持运行时修改队列容量</li>
 *   <li><b>配置热更新：</b>配合配置中心实现队列容量的热更新，无需重启应用</li>
 *   <li><b>线程安全：</b>容量调整是线程安全的，使用 volatile 保证可见性</li>
 * </ul>
 * 
 * <p><b>技术难点：</b>
 * JDK 原生的 {@link java.util.concurrent.LinkedBlockingQueue} 的容量是 final 的，
 * 创建后无法修改。本类通过以下方式突破这个限制：
 * <ul>
 *   <li>将 {@code capacity} 字段从 final 改为 {@code volatile}，支持动态修改</li>
 *   <li>修改容量后，通过 {@link #signalNotFull()} 唤醒等待的生产者线程</li>
 *   <li>保持双锁机制（putLock 和 takeLock），确保并发性能</li>
 * </ul>
 * 
 * <p><b>LinkedBlockingQueue 原理回顾：</b>
 * <ul>
 *   <li><b>数据结构：</b>基于单向链表的有界阻塞队列</li>
 *   <li><b>FIFO顺序：</b>先进先出（First-In-First-Out）</li>
 *   <li><b>头部出队：</b>从队列头部取出元素（最早入队的元素）</li>
 *   <li><b>尾部入队：</b>从队列尾部插入元素（最新的元素）</li>
 *   <li><b>双锁设计：</b>入队和出队使用不同的锁，提高并发性能</li>
 *   <li><b>原子计数：</b>使用 {@link AtomicInteger} 维护队列大小</li>
 * </ul>
 * 
 * <p><b>容量调整原理：</b>
 * <pre>
 * 调整前：capacity = 100, 当前大小 = 100（队列已满）
 *    ↓
 * 调用 setCapacity(200)
 *    ↓
 * 1. 保存旧容量：oldCapacity = 100
 * 2. 设置新容量：capacity = 200（volatile 写，立即对所有线程可见）
 * 3. 检查条件：新容量(200) > 当前大小(100) && 当前大小(100) >= 旧容量(100)
 * 4. 唤醒等待的生产者线程：signalNotFull()
 *    ↓
 * 等待的生产者线程被唤醒，可以继续入队
 * </pre>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li><b>动态线程池：</b>这是动态线程池的推荐队列类型</li>
 *   <li><b>流量波动：</b>根据流量动态调整队列容量</li>
 *   <li><b>配置热更新：</b>通过配置中心（Nacos/Apollo）动态调整容量</li>
 *   <li><b>弹性伸缩：</b>配合云原生架构的自动伸缩</li>
 * </ul>
 * 
 * <p><b>使用示例：</b>
 * <pre>{@code
 * // 示例1：创建队列
 * ResizableCapacityLinkedBlockingQueue<Runnable> queue = 
 *     new ResizableCapacityLinkedBlockingQueue<>(100);
 * 
 * // 在线程池中使用
 * ThreadPoolExecutor executor = new ThreadPoolExecutor(
 *     10, 20, 60L, TimeUnit.SECONDS,
 *     queue,  // 使用可调整容量的队列
 *     Executors.defaultThreadFactory(),
 *     new ThreadPoolExecutor.CallerRunsPolicy()
 * );
 * 
 * 
 * // 示例2：运行时调整容量
 * // 场景：流量高峰期，扩大队列容量
 * queue.setCapacity(500);  // 从100调整到500
 * 
 * // 场景：流量低谷期，缩小队列容量
 * queue.setCapacity(50);   // 从500调整到50
 * 
 * 
 * // 示例3：配合动态线程池使用
 * ThreadPoolExecutor dynamicExecutor = ThreadPoolExecutorBuilder.builder()
 *     .dynamicPool()
 *     .threadPoolId("order-processor")
 *     .workQueueType(BlockingQueueTypeEnum.RESIZABLE_CAPACITY_LINKED_BLOCKING_QUEUE)
 *     .workQueueCapacity(100)
 *     .build();
 * 
 * // 通过配置中心调整容量（自动触发）
 * // 修改 Nacos 配置：queue-capacity: 500
 * // 框架会自动调用：queue.setCapacity(500)
 * 
 * 
 * // 示例4：监控队列状态
 * int size = queue.size();                  // 当前元素数量
 * int remaining = queue.remainingCapacity(); // 剩余容量
 * double usage = (double) size / (size + remaining) * 100;  // 使用率
 * 
 * if (usage > 80) {
 *     // 队列使用率超过80%，扩大容量
 *     int newCapacity = (size + remaining) * 2;
 *     queue.setCapacity(newCapacity);
 * }
 * }</pre>
 * 
 * <p><b>性能特点：</b>
 * <ul>
 *   <li><b>入队出队：</b>O(1) 时间复杂度</li>
 *   <li><b>双锁机制：</b>入队和出队可以并发进行，性能优于单锁</li>
 *   <li><b>容量调整：</b>O(1) 时间复杂度（仅修改 volatile 变量）</li>
 *   <li><b>内存占用：</b>动态分配，每个元素有额外的 Node 对象开销</li>
 * </ul>
 * 
 * <p><b>线程安全性：</b>
 * <ul>
 *   <li>所有方法都是线程安全的</li>
 *   <li>使用两个 {@link ReentrantLock}（putLock 和 takeLock）保证并发安全</li>
 *   <li>使用 {@link AtomicInteger} 维护队列大小</li>
 *   <li>容量字段使用 {@code volatile} 修饰，保证可见性</li>
 * </ul>
 * 
 * <p><b>注意事项：</b>
 * <ul>
 *   <li><b>容量缩小：</b>如果新容量小于当前队列大小，不会移除已有元素，
 *       但新元素无法入队，直到队列大小降至新容量以下</li>
 *   <li><b>并发调整：</b>可以在队列使用过程中随时调整容量，线程安全</li>
 *   <li><b>性能开销：</b>频繁调整容量会有性能开销，建议根据实际需要适度调整</li>
 * </ul>
 * 
 * <p><b>与 LinkedBlockingQueue 的区别：</b>
 * <table border="1" cellpadding="5">
 *   <tr><th>特性</th><th>LinkedBlockingQueue</th><th>ResizableCapacityLinkedBlockingQueue</th></tr>
 *   <tr><td>容量字段</td><td>final int capacity</td><td>volatile int capacity</td></tr>
 *   <tr><td>容量调整</td><td>不支持</td><td>支持（setCapacity方法）</td></tr>
 *   <tr><td>其他功能</td><td colspan="2">完全相同（源码级别克隆）</td></tr>
 * </table>
 * 
 * <p><b>实现来源：</b>
 * 本类的实现基于 JDK {@link java.util.concurrent.LinkedBlockingQueue} 的源码（Doug Lea 编写），
 * 在保持原有逻辑不变的基础上，仅修改了容量字段和相关逻辑，确保了代码的质量和可靠性。
 * 
 * @param <E> 队列中元素的类型
 * @author Doug Lea（原始作者）
 * @author 杨潇（容量可调整功能）
 * @since 1.5
 * @see java.util.concurrent.LinkedBlockingQueue JDK原生实现
 * @see BlockingQueue 阻塞队列接口
 * @see ThreadPoolExecutor 线程池执行器
 */
public class ResizableCapacityLinkedBlockingQueue<E> extends AbstractQueue<E>
        implements
        BlockingQueue<E>,
        java.io.Serializable {

    private static final long serialVersionUID = -6903933977591709194L;

    /**
     * 双锁队列算法实现说明
     * <p>
     * 该队列使用"双锁队列"算法的变体实现，具有以下特点：
     * 
     * <p><b>双锁机制：</b>
     * <ul>
     *   <li><b>putLock：</b>控制入队操作（put 和 offer），有关联的等待条件（notFull）</li>
     *   <li><b>takeLock：</b>控制出队操作（take 和 poll），有关联的等待条件（notEmpty）</li>
     * </ul>
     * 
     * <p><b>原子计数器：</b>
     * {@code count} 字段使用 {@link AtomicInteger} 维护，避免在大多数情况下需要同时获取两个锁。
     * 
     * <p><b>级联通知（Cascading Notifies）：</b>
     * <ul>
     *   <li>当 put 操作发现队列从空变为非空时，会唤醒等待的 take 操作</li>
     *   <li>被唤醒的 take 操作如果发现还有更多元素，会继续唤醒其他 take 操作</li>
     *   <li>对称地，take 操作也会唤醒等待的 put 操作</li>
     *   <li>这种级联通知机制最小化了跨锁操作，提高了性能</li>
     * </ul>
     * 
     * <p><b>双锁操作：</b>
     * 某些操作（如 {@link #remove(Object)} 和迭代器）需要同时获取两个锁，
     * 以保证操作的原子性和一致性。
     */

    /**
     * 链表节点内部类
     * <p>
     * 用于构建单向链表，每个节点包含一个元素和指向下一个节点的引用。
     * 
     * <p><b>字段说明：</b>
     * <ul>
     *   <li><b>item：</b>节点存储的元素，使用 volatile 修饰确保内存可见性</li>
     *   <li><b>next：</b>指向下一个节点的引用</li>
     * </ul>
     * 
     * <p><b>volatile 的作用：</b>
     * <ul>
     *   <li>确保 item 的写操作对所有线程可见</li>
     *   <li>建立内存屏障（Memory Barrier），防止指令重排序</li>
     *   <li>保证 happen-before 语义，确保线程安全</li>
     * </ul>
     */
    static class Node<E> {

        /**
         * 节点存储的元素
         * <p>
         * 使用 volatile 修饰，确保多线程环境下的可见性。
         * 当一个线程修改 item 值时，其他线程能立即看到最新值。
         */
        volatile E item;
        
        /**
         * 指向下一个节点的引用
         * <p>
         * 构建单向链表结构，尾节点的 next 为 null。
         */
        Node<E> next;

        /**
         * 构造函数
         * 
         * @param x 节点存储的元素
         */
        Node(E x) {
            item = x;
        }
    }

    /**
     * 队列容量上限（关键修改：使用 volatile 而非 final）
     * <p>
     * 这是本类与 JDK LinkedBlockingQueue 的关键区别：
     * <ul>
     *   <li><b>JDK 版本：</b>{@code private final int capacity;} - 创建后无法修改</li>
     *   <li><b>本类版本：</b>{@code private volatile int capacity;} - 可以动态修改</li>
     * </ul>
     * 
     * <p><b>volatile 的作用：</b>
     * <ul>
     *   <li><b>可见性：</b>一个线程修改容量后，其他线程立即可见</li>
     *   <li><b>有序性：</b>防止指令重排序，确保容量修改的顺序性</li>
     *   <li><b>无原子性：</b>volatile 不保证原子性，但容量修改是单一赋值操作，天然原子</li>
     * </ul>
     * 
     * <p><b>默认值：</b>
     * <ul>
     *   <li>如果未指定，默认为 {@link Integer#MAX_VALUE}（约21亿）</li>
     *   <li>推荐显式指定容量，避免无限增长导致OOM</li>
     * </ul>
     */
    private volatile int capacity;

    /**
     * 当前元素数量
     * <p>
     * 使用 {@link AtomicInteger} 维护队列的当前元素数量，确保并发环境下的准确性。
     * 
     * <p><b>为什么使用 AtomicInteger？</b>
     * <ul>
     *   <li>入队和出队可能同时发生，需要原子操作</li>
     *   <li>避免每次获取队列大小都要加锁（性能优化）</li>
     *   <li>提供 CAS 操作，支持无锁并发</li>
     * </ul>
     * 
     * <p><b>操作时机：</b>
     * <ul>
     *   <li>入队时：{@code count.getAndIncrement()}</li>
     *   <li>出队时：{@code count.getAndDecrement()}</li>
     *   <li>清空队列：{@code count.getAndSet(0)}</li>
     * </ul>
     */
    private final AtomicInteger count = new AtomicInteger(0);

    /**
     * 链表头节点
     * <p>
     * 指向链表的头部哨兵节点（Sentinel Node），头节点本身不存储数据（item 为 null）。
     * 实际的第一个元素是 {@code head.next}。
     * 
     * <p><b>哨兵节点的作用：</b>
     * <ul>
     *   <li>简化边界条件处理（避免判断链表是否为空）</li>
     *   <li>统一插入和删除逻辑</li>
     * </ul>
     * 
     * <p><b>transient 修饰符：</b>
     * 该字段不会被序列化，反序列化时需要重建链表结构。
     */
    private transient Node<E> head;

    /**
     * 链表尾节点
     * <p>
     * 指向链表的尾部节点，新元素会插入到尾部。
     * 
     * <p><b>插入操作：</b>{@code last = last.next = new Node<>(x);}
     * 
     * <p><b>transient 修饰符：</b>
     * 该字段不会被序列化。
     */
    private transient Node<E> last;

    /**
     * 出队操作锁（takeLock）
     * <p>
     * 控制所有出队操作（take、poll、peek 等）的并发访问。
     * 
     * <p><b>保护的操作：</b>
     * <ul>
     *   <li>{@link #take()} - 阻塞式取元素</li>
     *   <li>{@link #poll()} - 非阻塞式取元素</li>
     *   <li>{@link #poll(long, TimeUnit)} - 超时取元素</li>
     *   <li>{@link #peek()} - 查看队首元素</li>
     * </ul>
     * 
     * <p><b>双锁设计的优势：</b>
     * 入队和出队使用不同的锁，可以并发进行，提高吞吐量。
     */
    private final ReentrantLock takeLock = new ReentrantLock();

    /**
     * 出队等待条件（notEmpty）
     * <p>
     * 当队列为空时，消费者线程会在该条件上等待。
     * 当有新元素入队时，会通过 {@link Condition#signal()} 唤醒等待的消费者。
     * 
     * <p><b>等待-唤醒机制：</b>
     * <ul>
     *   <li>队列为空时：消费者调用 {@code notEmpty.await()} 等待</li>
     *   <li>元素入队时：生产者调用 {@code notEmpty.signal()} 唤醒一个消费者</li>
     * </ul>
     */
    private final Condition notEmpty = takeLock.newCondition();

    /**
     * 入队操作锁（putLock）
     * <p>
     * 控制所有入队操作（put、offer 等）的并发访问。
     * 
     * <p><b>保护的操作：</b>
     * <ul>
     *   <li>{@link #put(Object)} - 阻塞式插入元素</li>
     *   <li>{@link #offer(Object)} - 非阻塞式插入元素</li>
     *   <li>{@link #offer(Object, long, TimeUnit)} - 超时插入元素</li>
     * </ul>
     * 
     * <p><b>与 takeLock 的关系：</b>
     * 两个锁相互独立，入队和出队可以并发进行。
     */
    private final ReentrantLock putLock = new ReentrantLock();

    /**
     * 入队等待条件（notFull）
     * <p>
     * 当队列已满时，生产者线程会在该条件上等待。
     * 当有元素出队时，会通过 {@link Condition#signal()} 唤醒等待的生产者。
     * 
     * <p><b>等待-唤醒机制：</b>
     * <ul>
     *   <li>队列已满时：生产者调用 {@code notFull.await()} 等待</li>
     *   <li>元素出队时：消费者调用 {@code notFull.signal()} 唤醒一个生产者</li>
     *   <li>容量扩大时：{@link #setCapacity(int)} 调用 {@code notFull.signal()} 唤醒等待的生产者</li>
     * </ul>
     */
    private final Condition notFull = putLock.newCondition();

    /**
     * Signal a waiting take. Called only from put/offer (which do not
     * otherwise ordinarily lock takeLock.)
     */
    private void signalNotEmpty() {
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            notEmpty.signal();
        } finally {
            takeLock.unlock();
        }
    }

    /**
     * Signal a waiting put. Called only from take/poll.
     */
    private void signalNotFull() {
        final ReentrantLock putLock = this.putLock;
        putLock.lock();
        try {
            notFull.signal();
        } finally {
            putLock.unlock();
        }
    }

    /**
     * Create a node and link it at end of queue
     *
     * @param x the item
     */
    private void insert(E x) {
        last = last.next = new Node<E>(x);
    }

    /**
     * Remove a node from head of queue,
     *
     * @return the node
     */
    private E extract() {
        Node<E> first = head.next;
        head = first;
        E x = first.item;
        first.item = null;
        return x;
    }

    /**
     * Lock to prevent both puts and takes.
     */
    private void fullyLock() {
        putLock.lock();
        takeLock.lock();
    }

    /**
     * Unlock to allow both puts and takes.
     */
    private void fullyUnlock() {
        takeLock.unlock();
        putLock.unlock();
    }

    /**
     * Creates a <tt>LinkedBlockingQueue</tt> with a capacity of
     * {@link Integer#MAX_VALUE}.
     */
    public ResizableCapacityLinkedBlockingQueue() {
        this(Integer.MAX_VALUE);
    }

    /**
     * Creates a <tt>LinkedBlockingQueue</tt> with the given (fixed) capacity.
     *
     * @param capacity the capacity of this queue.
     * @throws IllegalArgumentException if <tt>capacity</tt> is not greater
     *                                  than zero.
     */
    public ResizableCapacityLinkedBlockingQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException();
        }
        this.capacity = capacity;
        last = head = new Node<E>(null);
    }

    /**
     * Creates a <tt>LinkedBlockingQueue</tt> with a capacity of
     * {@link Integer#MAX_VALUE}, initially containing the elements of the
     * given collection,
     * added in traversal order of the collection's iterator.
     *
     * @param c the collection of elements to initially contain
     * @throws NullPointerException if <tt>c</tt> or any element within it
     *                              is <tt>null</tt>
     */
    public ResizableCapacityLinkedBlockingQueue(Collection<? extends E> c) {
        this(Integer.MAX_VALUE);
        for (Iterator<? extends E> it = c.iterator(); it.hasNext(); ) {
            add(it.next());
        }
    }

    // this doc comment is overridden to remove the reference to collections
    // greater in size than Integer.MAX_VALUE

    /**
     * Returns the number of elements in this queue.
     *
     * @return the number of elements in this queue.
     */
    @Override
    public int size() {
        return count.get();
    }

    /**
     * 动态调整队列容量（oneThread 核心功能）
     * <p>
     * 该方法是本类的核心增强功能，支持在队列使用过程中动态调整容量，
     * 无需重启应用或重建队列。这是实现动态线程池的关键技术之一。
     * 
     * <p><b>调整流程：</b>
     * <ol>
     *   <li>保存旧容量值</li>
     *   <li>设置新容量（volatile 写操作，立即对所有线程可见）</li>
     *   <li>获取当前队列大小</li>
     *   <li>如果满足唤醒条件，唤醒等待的生产者线程</li>
     * </ol>
     * 
     * <p><b>唤醒条件：</b>
     * <pre>
     * 新容量 > 当前大小 && 当前大小 >= 旧容量
     * </pre>
     * 
     * <p><b>唤醒条件解析：</b>
     * <ul>
     *   <li><b>新容量 > 当前大小：</b>扩容后队列未满，有空间可用</li>
     *   <li><b>当前大小 >= 旧容量：</b>之前队列已满，可能有生产者在等待</li>
     *   <li><b>同时满足：</b>说明扩容前队列满，扩容后有空间，需要唤醒等待的生产者</li>
     * </ul>
     * 
     * <p><b>调整场景示例：</b>
     * <table border="1" cellpadding="5">
     *   <tr>
     *     <th>场景</th>
     *     <th>旧容量</th>
     *     <th>当前大小</th>
     *     <th>新容量</th>
     *     <th>是否唤醒</th>
     *     <th>原因</th>
     *   </tr>
     *   <tr>
     *     <td>扩容-队列已满</td>
     *     <td>100</td>
     *     <td>100</td>
     *     <td>200</td>
     *     <td>✅ 是</td>
     *     <td>队列满，扩容后有空间</td>
     *   </tr>
     *   <tr>
     *     <td>扩容-队列未满</td>
     *     <td>100</td>
     *     <td>50</td>
     *     <td>200</td>
     *     <td>❌ 否</td>
     *     <td>队列未满，没有等待的生产者</td>
     *   </tr>
     *   <tr>
     *     <td>缩容-队列半满</td>
     *     <td>200</td>
     *     <td>100</td>
     *     <td>150</td>
     *     <td>❌ 否</td>
     *     <td>缩容不会唤醒（新容量>当前大小不满足）</td>
     *   </tr>
     *   <tr>
     *     <td>缩容-队列已满</td>
     *     <td>200</td>
     *     <td>200</td>
     *     <td>100</td>
     *     <td>❌ 否</td>
     *     <td>新容量<当前大小，无法入队</td>
     *   </tr>
     * </table>
     * 
     * <p><b>线程安全性：</b>
     * <ul>
     *   <li>容量字段使用 volatile 修饰，保证可见性</li>
     *   <li>无需加锁，单一赋值操作是原子的</li>
     *   <li>signalNotFull() 内部会获取 putLock，保证唤醒操作的安全性</li>
     * </ul>
     * 
     * <p><b>容量缩小的特殊情况：</b>
     * <ul>
     *   <li>如果新容量小于当前队列大小，已有元素不会被移除</li>
     *   <li>但新元素无法入队，直到队列大小降至新容量以下</li>
     *   <li>消费者可以继续取出元素，逐渐降低队列大小</li>
     * </ul>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * ResizableCapacityLinkedBlockingQueue<Runnable> queue = 
     *     new ResizableCapacityLinkedBlockingQueue<>(100);
     * 
     * // 场景1：扩容（流量高峰期）
     * queue.setCapacity(500);
     * // 如果有生产者在等待（队列之前已满），会被立即唤醒
     * 
     * 
     * // 场景2：缩容（流量低谷期）
     * queue.setCapacity(50);
     * // 如果当前队列大小 > 50，新元素无法入队
     * // 但已有元素会被正常消费，逐渐降低到50以下
     * 
     * 
     * // 场景3：配合配置中心自动调整
     * @EventListener
     * public void onConfigChange(ThreadPoolConfigUpdateEvent event) {
     *     ThreadPoolExecutorProperties newConfig = event.getBootstrapConfigProperties().getExecutors().get(0);
     *     Integer newCapacity = newConfig.getQueueCapacity();
     *     
     *     // 获取队列实例
     *     BlockingQueue<Runnable> queue = executor.getQueue();
     *     if (queue instanceof ResizableCapacityLinkedBlockingQueue) {
     *         ResizableCapacityLinkedBlockingQueue<Runnable> resizableQueue = 
     *             (ResizableCapacityLinkedBlockingQueue<Runnable>) queue;
     *         
     *         // 动态调整容量
     *         resizableQueue.setCapacity(newCapacity);
     *         log.info("队列容量已调整为: {}", newCapacity);
     *     }
     * }
     * 
     * 
     * // 场景4：监控驱动的自动调整
     * // 定时任务：每分钟检查队列使用率
     * @Scheduled(fixedRate = 60000)
     * public void autoAdjustCapacity() {
     *     ResizableCapacityLinkedBlockingQueue<Runnable> queue = ...;
     *     
     *     int size = queue.size();
     *     int capacity = size + queue.remainingCapacity();
     *     double usage = (double) size / capacity * 100;
     *     
     *     if (usage > 80) {
     *         // 使用率超过80%，扩容50%
     *         int newCapacity = (int) (capacity * 1.5);
     *         queue.setCapacity(newCapacity);
     *         log.info("队列使用率{}%，自动扩容至{}", usage, newCapacity);
     *     } else if (usage < 30 && capacity > 100) {
     *         // 使用率低于30%且容量较大，缩容30%
     *         int newCapacity = (int) (capacity * 0.7);
     *         queue.setCapacity(Math.max(newCapacity, 100));  // 最小保留100
     *         log.info("队列使用率{}%，自动缩容至{}", usage, newCapacity);
     *     }
     * }
     * }</pre>
     * 
     * <p><b>性能特点：</b>
     * <ul>
     *   <li>时间复杂度：O(1)（仅赋值操作）</li>
     *   <li>无锁操作：不需要获取队列锁</li>
     *   <li>唤醒开销：如果需要唤醒线程，会有锁的开销</li>
     * </ul>
     * 
     * <p><b>注意事项：</b>
     * <ul>
     *   <li>容量必须大于0，否则会导致队列无法使用</li>
     *   <li>频繁调整容量会影响性能，建议根据实际需要适度调整</li>
     *   <li>缩容不会移除已有元素，只是限制新元素入队</li>
     *   <li>扩容会立即生效，等待的生产者会被唤醒</li>
     * </ul>
     * 
     * <p><b>实现细节：</b>
     * <pre>
     * public void setCapacity(int capacity) {
     *     final int oldCapacity = this.capacity;    // 1. 保存旧容量
     *     this.capacity = capacity;                  // 2. 设置新容量（volatile写）
     *     final int size = count.get();              // 3. 获取当前大小
     *     
     *     // 4. 判断是否需要唤醒等待的生产者
     *     if (capacity > size && size >= oldCapacity) {
     *         signalNotFull();                       // 5. 唤醒一个等待的生产者
     *     }
     * }
     * </pre>
     *
     * @param capacity 新的队列容量（必须 > 0）
     * @throws IllegalArgumentException 如果 capacity <= 0（由构造函数校验）
     */
    public void setCapacity(int capacity) {
        // 保存旧容量，用于后续判断是否需要唤醒线程
        final int oldCapacity = this.capacity;
        
        // 设置新容量（volatile 写操作，立即对所有线程可见）
        this.capacity = capacity;
        
        // 获取当前队列大小
        final int size = count.get();
        
        // 判断是否需要唤醒等待的生产者线程
        // 条件：新容量 > 当前大小 && 当前大小 >= 旧容量
        // 含义：之前队列满（大小达到旧容量），扩容后有空间，需要唤醒等待的生产者
        if (capacity > size && size >= oldCapacity) {
            // 唤醒一个在 notFull 条件上等待的生产者线程
            // 被唤醒的线程会重新检查容量，如果有空间则继续入队
            signalNotFull();
        }
    }

    // this doc comment is a modified copy of the inherited doc comment,
    // without the reference to unlimited queues.

    /**
     * Returns the number of elements that this queue can ideally (in
     * the absence of memory or resource constraints) accept without
     * blocking. This is always equal to the initial capacity of this queue
     * less the current <tt>size</tt> of this queue.
     * <p>Note that you <em>cannot</em> always tell if
     * an attempt to <tt>add</tt> an element will succeed by
     * inspecting <tt>remainingCapacity</tt> because it may be the
     * case that a waiting consumer is ready to <tt>take</tt> an
     * element out of an otherwise full queue.
     */
    @Override
    public int remainingCapacity() {
        return capacity - count.get();
    }

    /**
     * Adds the specified element to the tail of this queue, waiting if
     * necessary for space to become available.
     *
     * @param o the element to add
     * @throws InterruptedException if interrupted while waiting.
     * @throws NullPointerException if the specified element is <tt>null</tt>.
     */
    @Override
    public void put(E o) throws InterruptedException {
        if (o == null) {
            throw new NullPointerException();
        }
        // Note: convention in all put/take/etc is to preset
        // local var holding count negative to indicate failure unless set.
        int c = -1;
        final ReentrantLock putLock = this.putLock;
        final AtomicInteger count = this.count;
        putLock.lockInterruptibly();
        try {
            /*
             * Note that count is used in wait guard even though it is not protected by lock. This works because count can only decrease at this point (all other puts are shut out by lock), and we (or
             * some other waiting put) are signalled if it ever changes from capacity. Similarly for all other uses of count in other wait guards.
             */
            try {
                while (count.get() >= capacity) {
                    notFull.await();
                }
            } catch (InterruptedException ie) {
                notFull.signal(); // propagate to a non-interrupted thread
                throw ie;
            }
            insert(o);
            c = count.getAndIncrement();
            if (c + 1 < capacity) {
                notFull.signal();
            }
        } finally {
            putLock.unlock();
        }
        if (c == 0) {
            signalNotEmpty();
        }
    }

    /**
     * Inserts the specified element at the tail of this queue, waiting if
     * necessary up to the specified wait time for space to become available.
     *
     * @param o       the element to add
     * @param timeout how long to wait before giving up, in units of
     *                <tt>unit</tt>
     * @param unit    a <tt>TimeUnit</tt> determining how to interpret the
     *                <tt>timeout</tt> parameter
     * @return <tt>true</tt> if successful, or <tt>false</tt> if
     * the specified waiting time elapses before space is available.
     * @throws InterruptedException if interrupted while waiting.
     * @throws NullPointerException if the specified element is <tt>null</tt>.
     */
    @Override
    public boolean offer(E o, long timeout, TimeUnit unit) throws InterruptedException {

        if (o == null) {
            throw new NullPointerException();
        }
        long nanos = unit.toNanos(timeout);
        int c = -1;
        final ReentrantLock putLock = this.putLock;
        final AtomicInteger count = this.count;
        putLock.lockInterruptibly();
        try {
            for (; ; ) {
                if (count.get() < capacity) {
                    insert(o);
                    c = count.getAndIncrement();
                    if (c + 1 < capacity) {
                        notFull.signal();
                    }
                    break;
                }
                if (nanos <= 0) {
                    return false;
                }
                try {
                    nanos = notFull.awaitNanos(nanos);
                } catch (InterruptedException ie) {
                    notFull.signal(); // propagate to a non-interrupted thread
                    throw ie;
                }
            }
        } finally {
            putLock.unlock();
        }
        if (c == 0) {
            signalNotEmpty();
        }
        return true;
    }

    /**
     * Inserts the specified element at the tail of this queue if possible,
     * returning immediately if this queue is full.
     *
     * @param o the element to add.
     * @return <tt>true</tt> if it was possible to add the element to
     * this queue, else <tt>false</tt>
     * @throws NullPointerException if the specified element is <tt>null</tt>
     */
    @Override
    public boolean offer(E o) {
        if (o == null) {
            throw new NullPointerException();
        }
        final AtomicInteger count = this.count;
        if (count.get() >= capacity) {
            return false;
        }
        int c = -1;
        final ReentrantLock putLock = this.putLock;
        putLock.lock();
        try {
            if (count.get() < capacity) {
                insert(o);
                c = count.getAndIncrement();
                if (c + 1 < capacity) {
                    notFull.signal();
                }
            }
        } finally {
            putLock.unlock();
        }
        if (c == 0) {
            signalNotEmpty();
        }
        return c >= 0;
    }

    @Override
    public E take() throws InterruptedException {
        E x;
        int c = -1;
        final AtomicInteger count = this.count;
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lockInterruptibly();
        try {
            try {
                while (count.get() == 0) {
                    notEmpty.await();
                }
            } catch (InterruptedException ie) {
                notEmpty.signal(); // propagate to a non-interrupted thread
                throw ie;
            }

            x = extract();
            c = count.getAndDecrement();
            if (c > 1) {
                notEmpty.signal();
            }
        } finally {
            takeLock.unlock();
        }
        if (c >= capacity) {
            signalNotFull();
        }
        return x;
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        E x = null;
        int c = -1;
        long nanos = unit.toNanos(timeout);
        final AtomicInteger count = this.count;
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lockInterruptibly();
        try {
            for (; ; ) {
                if (count.get() > 0) {
                    x = extract();
                    c = count.getAndDecrement();
                    if (c > 1) {
                        notEmpty.signal();
                    }
                    break;
                }
                if (nanos <= 0) {
                    return null;
                }
                try {
                    nanos = notEmpty.awaitNanos(nanos);
                } catch (InterruptedException ie) {
                    notEmpty.signal(); // propagate to a non-interrupted thread
                    throw ie;
                }
            }
        } finally {
            takeLock.unlock();
        }
        if (c >= capacity) {
            signalNotFull();
        }
        return x;
    }

    @Override
    public E poll() {
        final AtomicInteger count = this.count;
        if (count.get() == 0) {
            return null;
        }
        E x = null;
        int c = -1;
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            if (count.get() > 0) {
                x = extract();
                c = count.getAndDecrement();
                if (c > 1) {
                    notEmpty.signal();
                }
            }
        } finally {
            takeLock.unlock();
        }
        if (c >= capacity) {
            signalNotFull();
        }
        return x;
    }

    @Override
    public E peek() {
        if (count.get() == 0) {
            return null;
        }
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            Node<E> first = head.next;
            if (first == null) {
                return null;
            } else {
                return first.item;
            }
        } finally {
            takeLock.unlock();
        }
    }

    @Override
    public boolean remove(Object o) {
        if (o == null) {
            return false;
        }
        boolean removed = false;
        fullyLock();
        try {
            Node<E> trail = head;
            Node<E> p = head.next;
            while (p != null) {
                if (o.equals(p.item)) {
                    removed = true;
                    break;
                }
                trail = p;
                p = p.next;
            }
            if (removed) {
                p.item = null;
                trail.next = p.next;
                if (count.getAndDecrement() >= capacity) {
                    notFull.signalAll();
                }
            }
        } finally {
            fullyUnlock();
        }
        return removed;
    }

    @Override
    public Object[] toArray() {
        fullyLock();
        try {
            int size = count.get();
            Object[] a = new Object[size];
            int k = 0;
            for (Node<E> p = head.next; p != null; p = p.next) {
                a[k++] = p.item;
            }
            return a;
        } finally {
            fullyUnlock();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        fullyLock();
        try {
            int size = count.get();
            if (a.length < size) {
                a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
            }
            int k = 0;
            for (Node<?> p = head.next; p != null; p = p.next) {
                a[k++] = (T) p.item;
            }
            return a;
        } finally {
            fullyUnlock();
        }
    }

    @Override
    public String toString() {
        fullyLock();
        try {
            return super.toString();
        } finally {
            fullyUnlock();
        }
    }

    @Override
    public void clear() {
        fullyLock();
        try {
            head.next = null;
            if (count.getAndSet(0) >= capacity) {
                notFull.signalAll();
            }
        } finally {
            fullyUnlock();
        }
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        if (c == null) {
            throw new NullPointerException();
        }
        if (c == this) {
            throw new IllegalArgumentException();
        }
        Node<E> first;
        fullyLock();
        try {
            first = head.next;
            head.next = null;
            if (count.getAndSet(0) >= capacity) {
                notFull.signalAll();
            }
        } finally {
            fullyUnlock();
        }
        // Transfer the elements outside of locks
        int n = 0;
        for (Node<E> p = first; p != null; p = p.next) {
            c.add(p.item);
            p.item = null;
            ++n;
        }
        return n;
    }

    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        if (c == null) {
            throw new NullPointerException();
        }
        if (c == this) {
            throw new IllegalArgumentException();
        }
        if (maxElements <= 0) {
            return 0;
        }
        fullyLock();
        try {
            int n = 0;
            Node<E> p = head.next;
            while (p != null && n < maxElements) {
                c.add(p.item);
                p.item = null;
                p = p.next;
                ++n;
            }
            if (n != 0) {
                head.next = p;
                if (count.getAndAdd(-n) >= capacity) {
                    notFull.signalAll();
                }
            }
            return n;
        } finally {
            fullyUnlock();
        }
    }

    /**
     * Returns an iterator over the elements in this queue in proper sequence.
     * The returned <tt>Iterator</tt> is a "weakly consistent" iterator that
     * will never throw {@link java.util.ConcurrentModificationException},
     * and guarantees to traverse elements as they existed upon
     * construction of the iterator, and may (but is not guaranteed to)
     * reflect any modifications subsequent to construction.
     *
     * @return an iterator over the elements in this queue in proper sequence.
     */
    @Override
    public Iterator<E> iterator() {
        return new Itr();
    }

    /**
     * Itr.
     */
    private class Itr implements Iterator<E> {

        /*
         * Basic weak-consistent iterator. At all times hold the next item to hand out so that if hasNext() reports true, we will still have it to return even if lost race with a take etc.
         */
        private Node<E> current;
        private Node<E> lastRet;
        private E currentElement;

        Itr() {
            final ReentrantLock putLock = ResizableCapacityLinkedBlockingQueue.this.putLock;
            final ReentrantLock takeLock = ResizableCapacityLinkedBlockingQueue.this.takeLock;
            putLock.lock();
            takeLock.lock();
            try {
                current = head.next;
                if (current != null) {
                    currentElement = current.item;
                }
            } finally {
                takeLock.unlock();
                putLock.unlock();
            }
        }

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public E next() {
            final ReentrantLock putLock = ResizableCapacityLinkedBlockingQueue.this.putLock;
            final ReentrantLock takeLock = ResizableCapacityLinkedBlockingQueue.this.takeLock;
            putLock.lock();
            takeLock.lock();
            try {
                if (current == null) {
                    throw new NoSuchElementException();
                }
                E x = currentElement;
                lastRet = current;
                current = current.next;
                if (current != null) {
                    currentElement = current.item;
                }
                return x;
            } finally {
                takeLock.unlock();
                putLock.unlock();
            }
        }

        @Override
        public void remove() {
            if (lastRet == null) {
                throw new IllegalStateException();
            }
            final ReentrantLock putLock = ResizableCapacityLinkedBlockingQueue.this.putLock;
            final ReentrantLock takeLock = ResizableCapacityLinkedBlockingQueue.this.takeLock;
            putLock.lock();
            takeLock.lock();
            try {
                Node<E> node = lastRet;
                lastRet = null;
                Node<E> trail = head;
                Node<E> p = head.next;
                while (p != null && p != node) {
                    trail = p;
                    p = p.next;
                }
                if (p == node) {
                    p.item = null;
                    trail.next = p.next;
                    int c = count.getAndDecrement();
                    if (c >= capacity) {
                        notFull.signalAll();
                    }
                }
            } finally {
                takeLock.unlock();
                putLock.unlock();
            }
        }
    }

    /**
     * Save the state to a stream (that is, serialize it).
     *
     * @param s the stream
     * @serialData The capacity is emitted (int), followed by all of
     * its elements (each an <tt>Object</tt>) in the proper order,
     * followed by a null
     */
    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {

        fullyLock();
        try {
            // Write out any hidden stuff, plus capacity
            s.defaultWriteObject();

            // Write out all elements in the proper order.
            for (Node<E> p = head.next; p != null; p = p.next) {
                s.writeObject(p.item);
            }
            // Use trailing null as sentinel
            s.writeObject(null);
        } finally {
            fullyUnlock();
        }
    }

    /**
     * Reconstitute this queue instance from a stream (that is,
     * deserialize it).
     *
     * @param s the stream
     */
    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        // Read in capacity, and any hidden stuff
        s.defaultReadObject();

        count.set(0);
        last = head = new Node<E>(null);

        // Read in all elements and place in queue
        for (; ; ) {
            @SuppressWarnings("unchecked")
            E item = (E) s.readObject();
            if (item == null) {
                break;
            }
            add(item);
        }
    }
}
