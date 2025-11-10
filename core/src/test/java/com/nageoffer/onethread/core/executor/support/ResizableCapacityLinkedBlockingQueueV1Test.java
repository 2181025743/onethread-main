package com.nageoffer.onethread.core.executor.support;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ResizableCapacityLinkedBlockingQueueV1Test {

    /**
     * 注意：如果想在 IDEA 里跑这个单元测试，需要在 IDEA 单元测试中设置 VM 参数：
     * --add-opens java.base/java.util.concurrent=ALL-UNNAMED
     */
    public static void main(String[] args) throws Exception {
        ResizableCapacityLinkedBlockingQueueV1<String> queue = new ResizableCapacityLinkedBlockingQueueV1<>(2);

        // 填充队列至满
        queue.put("Element 1");
        System.out.println("入队列成功，当前大小：" + queue.size());
        queue.put("Element 2");
        System.out.println("入队列成功，当前大小：" + queue.size());

        // 尝试添加第三个元素，预期阻塞
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                System.out.println("尝试添加 Element 3，队列已满，线程将被阻塞");
                queue.put("Element 3");
                System.out.println("成功添加 Element 3，队列大小：" + queue.size());
            } catch (InterruptedException e) {
                System.out.println("添加 Element 3 失败");
            }
        });

        // 等待 2 秒，确保线程阻塞
        TimeUnit.SECONDS.sleep(2);

        // 通过反射修改容量
        try {
            queue.setCapacity(3);
            System.out.println("通过反射修改容量为：3");
        } catch (Exception e) {
            System.out.println("反射修改容量失败：" + e.getMessage());
        }

        // 等待 2 秒，观察是否成功添加
        TimeUnit.SECONDS.sleep(2);

        executor.shutdownNow();
    }
}