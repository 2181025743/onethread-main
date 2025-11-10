package com.nageoffer.onethread.core.notification.service;

import com.nageoffer.onethread.core.config.BootstrapConfigProperties;
import com.nageoffer.onethread.core.notification.dto.ThreadPoolAlarmNotifyDTO;
import com.nageoffer.onethread.core.notification.dto.ThreadPoolConfigChangeDTO;
import com.nageoffer.onethread.core.notification.dto.WebThreadPoolConfigChangeDTO;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 通知调度器，用于统一管理和路由各类通知发送器（如钉钉、飞书、企业微信等）
 * <p>
 * 该类屏蔽了具体通知平台的实现细节，对上层调用者提供统一的通知发送入口
 * 内部根据配置自动初始化可用的 Notifier 实现，并在发送通知时根据平台类型动态路由到对应的发送器
 * <p>
 * 作者：杨潇
 * 开发时间：2025-05-04
 */
public class NotifierDispatcher implements NotifierService {

    private static final Map<String, NotifierService> NOTIFIER_SERVICE_MAP = new HashMap<>();

    static {
        // 在简单工厂中注册不同的通知实现
        NOTIFIER_SERVICE_MAP.put("DING", new DingTalkMessageService());
        /**
         * 后续可以轻松扩展其他通知渠道
         * NOTIFIER_SERVICE_MAP.put("WECHAT", new WeChatMessageService());
         * NOTIFIER_SERVICE_MAP.put("EMAIL", new EmailMessageService());
         */
    }

    @Override
    public void sendChangeMessage(ThreadPoolConfigChangeDTO configChange) {
        getNotifierService().ifPresent(service -> service.sendChangeMessage(configChange));
    }

    @Override
    public void sendWebChangeMessage(WebThreadPoolConfigChangeDTO configChange) {
        getNotifierService().ifPresent(service -> service.sendWebChangeMessage(configChange));
    }

    @Override
    public void sendAlarmMessage(ThreadPoolAlarmNotifyDTO alarm) {
        getNotifierService().ifPresent(service -> {
            // 频率检查
            boolean allowSend = AlarmRateLimiter.allowAlarm(
                    alarm.getThreadPoolId(),
                    alarm.getAlarmType(),
                    alarm.getInterval()
            );

            // 满足频率发送告警
            if (allowSend) {
                service.sendAlarmMessage(alarm.resolve());
            }
        });
    }

    /**
     * 根据配置获取对应的通知服务实现
     * 简单工厂模式的核心方法
     */
    private Optional<NotifierService> getNotifierService() {
        return Optional.ofNullable(BootstrapConfigProperties.getInstance().getNotifyPlatforms())
                .map(BootstrapConfigProperties.NotifyPlatformsConfig::getPlatform)
                .map(platform -> NOTIFIER_SERVICE_MAP.get(platform));
    }
}
