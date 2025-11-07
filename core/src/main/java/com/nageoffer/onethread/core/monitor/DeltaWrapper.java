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

package com.nageoffer.onethread.core.monitor;

/**
 * 指标增量计算包装器
 * <p>
 * 该类用于计算两个周期之间的指标差值（增量），常用于监控系统中计算
 * 单位时间内的变化量，如每分钟完成的任务数、每分钟的拒绝次数等。
 * 
 * <p><b>核心功能：</b>
 * <ul>
 *   <li><b>增量计算：</b>计算当前值与上次值的差值</li>
 *   <li><b>值保存：</b>保存当前值和上次值，供下次计算使用</li>
 *   <li><b>线程安全：</b>使用 synchronized 保证并发安全</li>
 *   <li><b>首次处理：</b>首次调用返回0，避免异常的大值</li>
 * </ul>
 * 
 * <p><b>应用场景：</b>
 * <table border="1" cellpadding="5">
 *   <tr><th>指标</th><th>累计值示例</th><th>增量值</th><th>监控意义</th></tr>
 *   <tr>
 *     <td>任务完成数</td>
 *     <td>1000 → 1500</td>
 *     <td>500</td>
 *     <td>这个周期内完成了500个任务</td>
 *   </tr>
 *   <tr>
 *     <td>拒绝次数</td>
 *     <td>10 → 15</td>
 *     <td>5</td>
 *     <td>这个周期内新增5次拒绝</td>
 *   </tr>
 *   <tr>
 *     <td>异常次数</td>
 *     <td>50 → 52</td>
 *     <td>2</td>
 *     <td>这个周期内发生了2次异常</td>
 *   </tr>
 * </table>
 * 
 * <p><b>为什么需要增量？</b>
 * <ul>
 *   <li><b>累计值：</b>JDK 线程池提供的是累计值（如 completedTaskCount），从创建到现在的总数</li>
 *   <li><b>增量值：</b>监控系统更关心单位时间内的变化量，如"过去1分钟完成了多少任务"</li>
 *   <li><b>趋势分析：</b>增量值更能反映趋势和异常（如突然增多的拒绝次数）</li>
 *   <li><b>告警触发：</b>基于增量值的告警更准确（避免累计值持续告警）</li>
 * </ul>
 * 
 * <p><b>工作原理：</b>
 * <pre>
 * 初始状态：
 *   lastValue = null
 *   currentValue = null
 * 
 * 第1次 update(1000)：
 *   lastValue = 1000（首次，直接设为当前值）
 *   currentValue = 1000
 *   getDelta() = 0（首次返回0）
 * 
 * 第2次 update(1500)：
 *   lastValue = 1000（保存上次的 currentValue）
 *   currentValue = 1500
 *   getDelta() = 1500 - 1000 = 500
 * 
 * 第3次 update(1800)：
 *   lastValue = 1500
 *   currentValue = 1800
 *   getDelta() = 1800 - 1500 = 300
 * </pre>
 * 
 * <p><b>Prometheus集成示例：</b>
 * <pre>{@code
 * // 创建 DeltaWrapper 用于任务完成数的增量计算
 * DeltaWrapper completedDelta = new DeltaWrapper();
 * 
 * // 注册为 Prometheus Gauge 指标
 * Metrics.gauge(
 *     "thread_pool_completed_task_delta",  // 指标名
 *     tags,                                 // 标签（线程池ID、应用名等）
 *     completedDelta,                       // 数据源对象
 *     DeltaWrapper::getDelta                // 值提取方法
 * );
 * 
 * // 定时更新（每分钟）
 * @Scheduled(fixedRate = 60000)
 * public void collectMetrics() {
 *     long completedCount = executor.getCompletedTaskCount();
 *     completedDelta.update(completedCount);
 *     
 *     // Prometheus 抓取时会调用 getDelta() 获取增量值
 *     // 第1分钟：completedCount=1000, delta=0
     *     // 第2分钟：completedCount=1500, delta=500（过去1分钟完成500个任务）
     *     // 第3分钟：completedCount=1800, delta=300（过去1分钟完成300个任务）
 * }
 * }</pre>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li><b>Prometheus：</b>暴露增量指标给 Prometheus</li>
 *   <li><b>Grafana：</b>在 Grafana 中展示 TPS（每秒任务数）、拒绝率等</li>
 *   <li><b>告警：</b>基于增量值的告警（如每分钟拒绝超过10次）</li>
 *   <li><b>性能分析：</b>分析单位时间的处理能力</li>
 * </ul>
 * 
 * <p><b>线程安全性：</b>
 * <ul>
 *   <li>所有方法使用 synchronized 修饰，保证线程安全</li>
 *   <li>支持多线程并发调用 update 和 getDelta</li>
 * </ul>
 * 
 * <p><b>注意事项：</b>
 * <ul>
 *   <li>首次调用 getDelta() 返回0（避免异常的大值）</li>
 *   <li>适用于单调递增的累计指标（如任务完成数、拒绝次数）</li>
 *   <li>不适用于可能减少的指标（如队列大小）</li>
 *   <li>update 和 getDelta 应该配对使用，先 update 再 getDelta</li>
 * </ul>
 * 
 * @author 杨潇
 * @since 2025-07-30
 * @see ThreadPoolMonitor 线程池监控器
 * @see io.micrometer.core.instrument.Gauge Micrometer Gauge指标
 */
public class DeltaWrapper {

    /**
     * 上次的值
     * <p>
     * 保存上一个周期的指标值，用于计算增量。
     * 
     * <p><b>初始值：</b>null（首次 update 时会设置）
     */
    private Long lastValue;
    
    /**
     * 当前的值
     * <p>
     * 保存当前周期的指标值。
     * 
     * <p><b>初始值：</b>null（首次 update 时会设置）
     */
    private Long currentValue;

    /**
     * 更新最新值，并记录上一次的值
     * <p>
     * 该方法应该在每个监控周期调用一次，传入当前周期采集到的累计值。
     * 
     * <p><b>更新逻辑：</b>
     * <ol>
     *   <li>如果是首次调用（currentValue 为 null）：
     *       <ul>
     *         <li>lastValue = newValue（首次设为当前值，避免异常的大增量）</li>
     *         <li>currentValue = newValue</li>
     *       </ul>
     *   </li>
     *   <li>如果不是首次调用：
     *       <ul>
     *         <li>lastValue = currentValue（保存上次的值）</li>
     *         <li>currentValue = newValue（更新为新值）</li>
     *       </ul>
     *   </li>
     * </ol>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * DeltaWrapper delta = new DeltaWrapper();
     * 
     * // 第1次采集（T1时刻）
     * long completedCount1 = executor.getCompletedTaskCount();  // 1000
     * delta.update(completedCount1);
     * 
     * // 第2次采集（T2时刻，1分钟后）
     * long completedCount2 = executor.getCompletedTaskCount();  // 1500
     * delta.update(completedCount2);
     * long delta1 = delta.getDelta();  // 1500 - 1000 = 500
     * 
     * // 第3次采集（T3时刻，再过1分钟）
     * long completedCount3 = executor.getCompletedTaskCount();  // 1800
     * delta.update(completedCount3);
     * long delta2 = delta.getDelta();  // 1800 - 1500 = 300
     * }</pre>
     *
     * @param newValue 当前周期采集到的原始指标累计值
     */
    public synchronized void update(long newValue) {
        // 如果是首次调用（currentValue 为 null）
        // 将 lastValue 也设为 newValue，避免首次计算出异常的大增量
        this.lastValue = (this.currentValue == null) ? newValue : this.currentValue;
        
        // 更新当前值
        this.currentValue = newValue;
    }

    /**
     * 获取当前周期与上一周期之间的增量值
     * <p>
     * 计算 {@code currentValue - lastValue}，得到两次 update 之间的差值。
     * 
     * <p><b>计算逻辑：</b>
     * <ul>
     *   <li>如果 currentValue 或 lastValue 为 null：返回 0（首次或异常情况）</li>
     *   <li>否则：返回 currentValue - lastValue（增量值）</li>
     * </ul>
     * 
     * <p><b>返回值说明：</b>
     * <ul>
     *   <li>0：首次调用，或两次值相同（没有变化）</li>
     *   <li>> 0：指标增加了（正常情况）</li>
     *   <li>< 0：指标减少了（异常情况，如线程池重启）</li>
     * </ul>
     * 
     * <p><b>使用示例：</b>
     * <pre>{@code
     * // 在 Prometheus Gauge 中使用
     * Metrics.gauge(
     *     "thread_pool_completed_task_delta",
     *     tags,
     *     completedDelta,
     *     DeltaWrapper::getDelta  // Prometheus 抓取时会调用此方法
     * );
     * 
     * // 直接获取增量
     * long delta = completedDelta.getDelta();
     * if (delta > 1000) {
     *     log.info("过去1分钟完成了 {} 个任务，处理速度很快", delta);
     * }
     * }</pre>
     * 
     * <p><b>注意事项：</b>
     * <ul>
     *   <li>首次调用返回0（避免异常的大增量）</li>
     *   <li>如果累计值被重置（如应用重启），可能出现负值</li>
     *   <li>需要定期调用 update 方法，否则 getDelta 会返回旧数据</li>
     * </ul>
     *
     * @return 当前周期与上一周期之间的增量值，首次调用返回 0
     */
    public synchronized long getDelta() {
        // 如果 currentValue 或 lastValue 为 null，返回 0
        if (currentValue == null || lastValue == null) {
            return 0;
        }
        
        // 返回增量值（当前值 - 上次值）
        return currentValue - lastValue;
    }
}
