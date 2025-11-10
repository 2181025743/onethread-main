package com.nageoffer.onethread.core.executor.support;

import cn.hutool.core.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * 支持动态修改容量的阻塞队列实现
 *
 * <p>说明：JDK 原生 LinkedBlockingQueue 的 capacity 字段为 final，
 * 无法直接变更，因此通过反射方式动态调整
 */
@Slf4j
public class ResizableCapacityLinkedBlockingQueueV1<E> extends LinkedBlockingQueue<E> {

    public ResizableCapacityLinkedBlockingQueueV1(int capacity) {
        super(capacity);
    }

    /**
     * 动态设置队列容量
     *
     * @param newCapacity 新的容量值
     * @return 设置是否成功
     */
    public boolean setCapacity(Integer newCapacity) {
        if (newCapacity == null || newCapacity <= 0) {
            log.warn("非法容量值: {}", newCapacity);
            return false;
        }

        try {
            // 通过反射修改 final 字段
            ReflectUtil.setFieldValue(this, "capacity", newCapacity);
            log.info("成功修改阻塞队列容量为: {}", newCapacity);
            return true;
        } catch (Exception ex) {
            log.error("动态修改阻塞队列容量失败，newCapacity={}", newCapacity, ex);
            return false;
        }
    }
}