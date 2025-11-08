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
 * {@linkplain java.util.concurrent.LinkedBlockingQueue} 的克隆版本
 * 增加了 {@link #setCapacity(int)} 方法，允许我们在使用队列时
 * 更改队列的容量。<p>
 * <p>
 * 以下是 LinkedBlockingQueue 的文档说明...<p>
 * <p>
 * 一个基于链节点的可选有界 {@linkplain BlockingQueue 阻塞队列}。
 * 此队列按 FIFO（先进先出）的方式对元素进行排序。
 * 队列的<em>头部</em>是队列中存在时间最长的元素。
 * 队列的<em>尾部</em>是队列中存在时间最短的元素。
 * 新元素插入到队列的尾部，队列检索操作获取队列头部的元素。
 * 链式队列通常比基于数组的队列具有更高的吞吐量，
 * 但在大多数并发应用程序中性能不太可预测。
 *
 * <p>可选的容量绑定构造函数参数用作防止队列过度扩展的方法。
 * 如果未指定容量，则等于 {@link Integer#MAX_VALUE}。
 * 除非插入会使队列超过容量，否则在每次插入时都会动态创建链节点。
 *
 * <p>此类实现了 {@link Collection} 和 {@link Iterator} 接口的所有<em>可选</em>方法。
 *
 * <p>此类是
 * <a href="{@docRoot}/../guide/collections/index.html">
 * Java Collections Framework</a> 的成员。
 *
 * @param <E> 此集合中保存的元素类型
 * @author Doug Lea
 * @since 1.5
 **/
public class ResizableCapacityLinkedBlockingQueue<E> extends AbstractQueue<E>
        implements
        BlockingQueue<E>,
        java.io.Serializable {

    private static final long serialVersionUID = -6903933977591709194L;

    /*
     * "双锁队列"算法的一个变体。putLock 控制 put（和 offer）的入口，
     * 并有关联的条件用于等待 put 操作。takeLock 类似。
     * 它们所依赖的 "count" 字段被维护为原子变量，
     * 以避免在大多数情况下需要获取两个锁。
     * 此外，为了最小化 put 获取 takeLock 和反之的需求，使用了级联通知。
     * 当 put 注意到它已启用至少一个 take 时，它会通知 taker。
     * 如果自信号发出以来输入了更多项，该 taker 反过来会通知其他人。
     * 对于 take 通知 put 也是对称的。
     * remove(Object) 和迭代器等操作会获取两个锁。
     */

    /**
     * 链表节点类
     */
    static class Node<E> {

        /**
         * 节点项，使用 volatile 确保写入和读取之间的内存屏障
         */
        volatile E item;
        Node<E> next;

        Node(E x) {
            item = x;
        }
    }

    /**
     * 容量边界，如果没有则为 Integer.MAX_VALUE
     */
    private volatile int capacity;

    /**
     * 当前元素数量
     */
    private final AtomicInteger count = new AtomicInteger(0);

    /**
     * 链表头部
     */
    private transient Node<E> head;

    /**
     * 链表尾部
     */
    private transient Node<E> last;

    /**
     * 由 take、poll 等方法持有的锁
     */
    private final ReentrantLock takeLock = new ReentrantLock();

    /**
     * 等待 take 操作的等待队列
     */
    private final Condition notEmpty = takeLock.newCondition();

    /**
     * 由 put、offer 等方法持有的锁
     */
    private final ReentrantLock putLock = new ReentrantLock();

    /**
     * 等待 put 操作的等待队列
     */
    private final Condition notFull = putLock.newCondition();

    /**
     * 通知等待的 take 操作。仅从 put/offer 调用（这些方法通常不会锁定 takeLock）。
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
     * 通知等待的 put 操作。仅从 take/poll 调用。
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
     * 创建一个节点并将其链接到队列末尾
     *
     * @param x 要插入的项
     */
    private void insert(E x) {
        last = last.next = new Node<E>(x);
    }

    /**
     * 从队列头部移除一个节点，
     *
     * @return 节点项
     */
    private E extract() {
        Node<E> first = head.next;
        head = first;
        E x = first.item;
        first.item = null;
        return x;
    }

    /**
     * 锁定以防止 put 和 take 操作。
     */
    private void fullyLock() {
        putLock.lock();
        takeLock.lock();
    }

    /**
     * 解锁以允许 put 和 take 操作。
     */
    private void fullyUnlock() {
        takeLock.unlock();
        putLock.unlock();
    }

    /**
     * 创建一个容量为 {@link Integer#MAX_VALUE} 的 <tt>LinkedBlockingQueue</tt>。
     */
    public ResizableCapacityLinkedBlockingQueue() {
        this(Integer.MAX_VALUE);
    }

    /**
     * 创建一个具有给定（固定）容量的 <tt>LinkedBlockingQueue</tt>。
     *
     * @param capacity 此队列的容量。
     * @throws IllegalArgumentException 如果 <tt>capacity</tt> 不大于零。
     */
    public ResizableCapacityLinkedBlockingQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException();
        }
        this.capacity = capacity;
        last = head = new Node<E>(null);
    }

    /**
     * 创建一个容量为 {@link Integer#MAX_VALUE} 的 <tt>LinkedBlockingQueue</tt>，
     * 最初包含给定集合的元素，按集合迭代器的遍历顺序添加。
     *
     * @param c 最初包含的元素集合
     * @throws NullPointerException 如果 <tt>c</tt> 或其中任何元素为 <tt>null</tt>
     */
    public ResizableCapacityLinkedBlockingQueue(Collection<? extends E> c) {
        this(Integer.MAX_VALUE);
        for (Iterator<? extends E> it = c.iterator(); it.hasNext(); ) {
            add(it.next());
        }
    }

    // 此文档注释已被重写以移除对大于 Integer.MAX_VALUE 的集合的引用

    /**
     * 返回此队列中的元素数量。
     *
     * @return 此队列中的元素数量。
     */
    @Override
    public int size() {
        return count.get();
    }

    /**
     * 为队列设置新容量。
     *
     * @param capacity 队列的新容量
     */
    public void setCapacity(int capacity) {
        final int oldCapacity = this.capacity;
        this.capacity = capacity;
        final int size = count.get();
        if (capacity > size && size >= oldCapacity) {
            signalNotFull();
        }
    }

    // 此文档注释是继承文档注释的修改版本，
    // 移除了对无限制队列的引用。

    /**
     * 返回此队列在理想情况下（在没有内存或资源约束的情况下）
     * 可以接受而不阻塞的元素数量。这始终等于此队列的初始容量减去此队列的当前 <tt>size</tt>。
     * <p>请注意，您<em>不能</em>总是通过检查 <tt>remainingCapacity</tt>
     * 来判断尝试 <tt>add</tt> 元素是否会成功，
     * 因为可能有一个等待的消费者准备从其他方面已满的队列中 <tt>take</tt> 元素。
     */
    @Override
    public int remainingCapacity() {
        return capacity - count.get();
    }

    /**
     * 将指定元素添加到此队列的尾部，如有必要则等待空间变为可用。
     *
     * @param o 要添加的元素
     * @throws InterruptedException 如果在等待时被中断。
     * @throws NullPointerException 如果指定元素为 <tt>null</tt>。
     */
    @Override
    public void put(E o) throws InterruptedException {
        if (o == null) {
            throw new NullPointerException();
        }
        // 注意：在所有 put/take/etc 中的约定是预设
        // 持有计数的局部变量为负值以表示失败，除非被设置。
        int c = -1;
        final ReentrantLock putLock = this.putLock;
        final AtomicInteger count = this.count;
        putLock.lockInterruptibly();
        try {
            /*
             * 请注意，尽管 count 不受锁保护，但它仍用于等待保护条件。
             * 这是因为在此时 count 只能减少（所有其他 put 操作都被锁排除），
             * 如果它从容量发生变化，我们会（或其他等待的 put）收到信号。
             * 对于在其他等待保护条件中使用 count 的所有其他情况也是如此。
             */
            try {
                while (count.get() >= capacity) {
                    notFull.await();
                }
            } catch (InterruptedException ie) {
                notFull.signal(); // 传播到未中断的线程
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
     * 在此队列的尾部插入指定元素，如有必要则等待指定的等待时间以使空间变为可用。
     *
     * @param o       要添加的元素
     * @param timeout 放弃前等待的时间，以 <tt>unit</tt> 为单位
     * @param unit    一个 <tt>TimeUnit</tt>，用于确定如何解释 <tt>timeout</tt> 参数
     * @return <tt>true</tt> 如果成功，或者在空间可用之前指定的等待时间已过则返回 <tt>false</tt>。
     * @throws InterruptedException 如果在等待时被中断。
     * @throws NullPointerException 如果指定元素为 <tt>null</tt>。
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
                    notFull.signal(); // 传播到未中断的线程
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
     * 如果可能，在此队列的尾部插入指定元素，如果此队列已满则立即返回。
     *
     * @param o 要添加的元素。
     * @return <tt>true</tt> 如果可以将元素添加到此队列，否则 <tt>false</tt>
     * @throws NullPointerException 如果指定元素为 <tt>null</tt>
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
     * 返回在此队列中的元素上按适当顺序的迭代器。
     * 返回的 <tt>Iterator</tt> 是一个"弱一致性"迭代器，
     * 它永远不会抛出 {@link java.util.ConcurrentModificationException}，
     * 并保证遍历构造迭代器时存在的元素，
     * 可能（但不保证）反映构造后的任何修改。
     *
     * @return 在此队列中的元素上按适当顺序的迭代器。
     */
    @Override
    public Iterator<E> iterator() {
        return new Itr();
    }

    /**
     * 迭代器实现。
     */
    private class Itr implements Iterator<E> {

        /*
         * 基本的弱一致性迭代器。始终持有下一个要分发的项，
         * 以便如果 hasNext() 报告为 true，即使与 take 等操作发生竞争，我们仍然有它可返回。
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
     * 将状态保存到流中（即序列化它）。
     *
     * @param s 流
     * @serialData 发出容量（int），然后是所有元素（每个都是 <tt>Object</tt>）
     * 按适当顺序排列，最后是一个 null
     */
    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {

        fullyLock();
        try {
            // 写出任何隐藏内容，加上容量
            s.defaultWriteObject();

            // 按适当顺序写出所有元素。
            for (Node<E> p = head.next; p != null; p = p.next) {
                s.writeObject(p.item);
            }
            // 使用尾随 null 作为哨兵
            s.writeObject(null);
        } finally {
            fullyUnlock();
        }
    }

    /**
     * 从流中重构此队列实例（即反序列化它）。
     *
     * @param s 流
     */
    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        // 读取容量和任何隐藏内容
        s.defaultReadObject();

        count.set(0);
        last = head = new Node<E>(null);

        // 读取所有元素并放入队列
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
