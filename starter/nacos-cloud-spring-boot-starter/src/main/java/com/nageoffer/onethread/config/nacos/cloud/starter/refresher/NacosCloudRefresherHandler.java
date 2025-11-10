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

package com.nageoffer.onethread.config.nacos.cloud.starter.refresher;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.nageoffer.onethread.config.common.starter.refresher.AbstractDynamicThreadPoolRefresher;
import com.nageoffer.onethread.core.executor.support.BlockingQueueTypeEnum;
import com.nageoffer.onethread.core.toolkit.ThreadPoolExecutorBuilder;
import com.nageoffer.onethread.core.config.BootstrapConfigProperties;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Nacos Cloud 版本刷新处理器
 * <p>
 * 【核心职责】
 * 1. 实现 Nacos 配置中心的监听机制,监听线程池配置变更
 * 2. 继承 AbstractDynamicThreadPoolRefresher 抽象类,复用配置刷新的通用逻辑
 * 3. 当 Nacos 配置发生变化时,自动触发线程池参数的热更新
 * <p>
 * 【设计模式】
 * - 模板方法模式: 继承 AbstractDynamicThreadPoolRefresher,父类定义刷新流程,子类实现配置监听
 * - 观察者模式: 通过 Nacos Listener 监听配置变更事件
 * <p>
 * 【工作流程】
 * 1. 应用启动时调用 registerListener() 注册 Nacos 监听器
 * 2. Nacos 配置变更时触发 receiveConfigInfo() 回调
 * 3. 调用父类的 refreshThreadPoolProperties() 方法解析配置并更新线程池
 * <p>
 * 作者：杨潇
 * 开发时间：2025-04-24
 */
@Slf4j(topic = "OneThreadConfigRefresher")
public class NacosCloudRefresherHandler extends AbstractDynamicThreadPoolRefresher {

    /**
     * Nacos 配置服务客户端
     * 用于与 Nacos 配置中心交互,添加监听器、获取配置等
     */
    private ConfigService configService;

    /**
     * 构造函数
     * 
     * @param configService Nacos 配置服务客户端,由 Spring 容器注入
     * @param properties 启动配置属性,包含 Nacos 连接信息(dataId、group 等)
     */
    public NacosCloudRefresherHandler(ConfigService configService, BootstrapConfigProperties properties) {
        super(properties); // 调用父类构造函数,初始化配置属性
        this.configService = configService;
    }

    /**
     * 注册 Nacos 配置监听器
     * <p>
     * 【核心逻辑】
     * 1. 从配置属性中获取 Nacos 的 dataId 和 group
     * 2. 向 Nacos 注册监听器,监听指定配置文件的变更
     * 3. 配置变更时自动触发回调,执行线程池参数刷新
     * <p>
     * 【监听器设计】
     * - getExecutor(): 返回一个专用线程池,用于异步处理配置变更事件
     * - receiveConfigInfo(): 配置变更时的回调方法,接收最新配置内容
     * 
     * @throws NacosException 当 Nacos 连接失败或注册监听器失败时抛出
     */
    public void registerListener() throws NacosException {
        // 获取 Nacos 配置信息(dataId、group 等)
        BootstrapConfigProperties.NacosConfig nacosConfig = properties.getNacos();
        
        // 向 Nacos 注册监听器,监听指定的配置文件
        configService.addListener(
                nacosConfig.getDataId(),  // 配置文件的 dataId,如 "onethread-nacos-cloud-example.yaml"
                nacosConfig.getGroup(),   // 配置文件的分组,如 "DEFAULT_GROUP"
                new Listener() {

                    /**
                     * 返回用于处理配置变更事件的线程池
                     * <p>
                     * 【线程池配置说明】
                     * - corePoolSize=1, maximumPoolSize=1: 单线程处理,保证配置变更顺序执行
                     * - keepAliveTime=9999L: 线程空闲时长期保活,避免频繁创建销毁
                     * - SynchronousQueue: 同步队列,不缓存任务,直接交给线程处理
                     * - CallerRunsPolicy: 拒绝策略,线程池满时由调用线程执行,保证配置不丢失
                     * <p>
                     * 【为什么使用单线程】
                     * 1. 配置变更需要顺序处理,避免并发修改线程池参数导致状态不一致
                     * 2. 配置变更频率低,单线程足够处理
                     * 
                     * @return 配置变更处理线程池
                     */
                    @Override
                    public Executor getExecutor() {
                        return ThreadPoolExecutorBuilder.builder()
                                .corePoolSize(1)                                      // 核心线程数为 1
                                .maximumPoolSize(1)                                   // 最大线程数为 1
                                .keepAliveTime(9999L)                                 // 线程空闲保活时间(秒)
                                .workQueueType(BlockingQueueTypeEnum.SYNCHRONOUS_QUEUE) // 同步队列,不缓存任务
                                .threadFactory("clod-nacos-refresher-thread_")        // 线程名称前缀(注意:原代码拼写错误 "clod" 应为 "cloud")
                                .rejectedHandler(new ThreadPoolExecutor.CallerRunsPolicy()) // 拒绝策略:调用者运行
                                .build();
                    }

                    /**
                     * 配置变更回调方法
                     * <p>
                     * 【执行流程】
                     * 1. Nacos 检测到配置文件变更
                     * 2. 触发此回调方法,传入最新的配置内容(YAML 或 Properties 格式的字符串)
                     * 3. 调用父类的 refreshThreadPoolProperties() 方法:
                     *    - 解析配置内容(支持 YAML 和 Properties 格式)
                     *    - 提取线程池参数(核心线程数、最大线程数、队列容量等)
                     *    - 通过反射修改运行中的线程池参数
                     *    - 发布 ThreadPoolConfigUpdateEvent 事件通知监控组件
                     * <p>
                     * 【注意事项】
                     * - 此方法在 getExecutor() 返回的线程池中异步执行
                     * - 配置解析失败不会影响应用运行,只会记录错误日志
                     * 
                     * @param configInfo Nacos 推送的最新配置内容(完整的配置文件字符串)
                     */
                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        // 调用父类方法刷新线程池配置
                        // 父类会解析 configInfo,提取线程池参数,并通过反射更新运行中的线程池
                        refreshThreadPoolProperties(configInfo);
                    }
                });

        // 记录监听器注册成功日志
        log.info("Dynamic thread pool refresher, add nacos cloud listener success. data-id: {}, group: {}", 
                nacosConfig.getDataId(), nacosConfig.getGroup());
    }
}
