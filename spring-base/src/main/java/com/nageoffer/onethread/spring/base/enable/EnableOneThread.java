package com.nageoffer.onethread.spring.base.enable;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 动态启用 oneThread 动态线程池开关注解
 * <p>
 * 作者：杨潇
 * 开发时间：2025-04-23
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(MarkerConfiguration.class)
public @interface EnableOneThread {
}
