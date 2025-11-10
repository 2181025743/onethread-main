package com.nageoffer.onethread.core.notification.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 线程池告警速率限流器
 * <p>
 * 作者：杨潇
 * 开发时间：2025-05-05
 */
public class AlarmRateLimiter {

    /**
     * 报警记录缓存 key: threadPoolId + "|" + alarmType
     */
    private static final Map<String, Long> ALARM_RECORD = new ConcurrentHashMap<>();

    /**
     * 检查是否允许发送报警
     *
     * @param threadPoolId    线程池ID
     * @param alarmType       报警类型
     * @param intervalMinutes 间隔分钟数
     * @return true-允许发送，false-需要抑制
     */
    public static boolean allowAlarm(String threadPoolId, String alarmType, int intervalMinutes) {
        String key = buildKey(threadPoolId, alarmType);
        long currentTime = System.currentTimeMillis();

        return ALARM_RECORD.compute(key, (k, lastTime) -> {
            if (lastTime == null || (currentTime - lastTime) > intervalMinutes * 60 * 1000L) {
                return currentTime; // 更新时间为当前时间
            }
            return lastTime; // 保持原时间
        }) == currentTime; // 返回值等于当前时间说明允许发送
    }

    private static String buildKey(String threadPoolId, String alarmType) {
        return threadPoolId + "|" + alarmType;
    }
}
