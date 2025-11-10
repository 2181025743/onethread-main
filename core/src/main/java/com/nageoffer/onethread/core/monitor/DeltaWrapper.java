package com.nageoffer.onethread.core.monitor;

/**
 * 用于计算两个周期之间的指标差值（如：任务完成数、拒绝次数等）
 * 通常用于 Micrometer Gauge 指标中，暴露单位时间内的变化量
 * <p>
 * 作者：杨潇
 * 开发时间：2025-07-30
 */

/**
 * 用于计算指标的周期差值，如任务完成数、拒绝数等。
 */
public class DeltaWrapper {

    private Long lastValue;
    private Long currentValue;

    /**
     * 更新最新值，并记录上一次的值，便于计算 delta
     *
     * @param newValue 当前周期采集到的原始指标值
     */
    public synchronized void update(long newValue) {
        this.lastValue = (this.currentValue == null) ? newValue : this.currentValue;
        this.currentValue = newValue;
    }

    /**
     * 获取当前周期与上一周期之间的增量值
     *
     * @return 周期内的差值；首次为 0
     */
    public synchronized long getDelta() {
        if (currentValue == null || lastValue == null) {
            return 0;
        }
        return currentValue - lastValue;
    }
}
