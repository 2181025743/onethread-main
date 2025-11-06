/*
 * @Descripttion: 线程池管理
 * @version: 1.0.0
 * @Author: fulei🐰
 * @Date: 2025-06-02 16:56:13
 * @LastEditors: fulei🐰
 * @LastEditTime: 2025-06-07 20:33:23
 */
import { requestClient } from '#/api/request';

export namespace threadPoolsApi {
  export interface ListItem {
    namespace: String, // 命名空间
    serviceName: String, // 服务名称
    dataId: String, // 数据ID
    group?: String, // 分组标识
    threadPoolId: String,// 线程池唯一标识
    corePoolSize: Number, // 核心线程数
    maximumPoolSize: Number, // 最大线程数
    queueCapacity: Number, // 队列容量
    workQueue: String, // 阻塞队列类型
    rejectedHandler: String, // 拒绝策略类型
    keepAliveTime: Number, // 线程空闲存活时间（单位：秒）
    allowCoreThreadTimeOut: Boolean, // 是否允许核心线程超时
    notify?: { // 通知配置
      receives: string, // 接收人集合
      interval: Number, // 告警间隔，单位分钟
    },
    alarm?: { // 告警配置
      enable: Boolean, // 默认开启报警配配置
      queueThreshold: Number, // 队列阈值
      activeThreshold: Number, // 活跃线程阈值
    }
  }
}
export namespace InstancesApi {
  export interface ListItem {
    threadPoolId: String // 线程池唯一标识,
    activeProfile: String // 当前环境,
    networkAddress: String // 实例标识,
    corePoolSize: Number // 核心线程数,
    maximumPoolSize: Number // 最大线程数,
    keepAliveTime: Number// 线程空闲存活时间,
    workQueueName: String// 阻塞队列类型,
    workQueueCapacity: Number // 队列容量,
    workQueueSize: Number// 队列元素数量,
    workQueueRemainingCapacity: Number // 队列剩余容量,
    rejectedHandlerName: String // 拒绝策略,
    rejectCount: Number// 执行拒绝策略次数,
  }
}

/**
 * 获取Grafana地址
 */
async function getGrafanaUrl() {
  return requestClient.get(`/grafana`);
}

/**
 * 获取线程池列表
 */
async function getThreadPoolsList(
  data: {
    namespace: String, // 命名空间
    serviceName: String, // 服务名称
  }
) {
  return requestClient.get<Array<threadPoolsApi.ListItem>>(
    '/thread-pools' + `?namespace=${data.namespace}&serviceName=${data.serviceName}`,
  );
}

/**
 * 线程池变更
 */
async function updateThreadPool(
  data: Omit<threadPoolsApi.ListItem, ''>,
) {
  return requestClient.put(`/thread-pool`, data);
}


/**
 * 获取线程池实例列表
 */

async function getInstancesList(
  data: {
    namespace: String, // 命名空间
    serviceName: String, // 服务名称
    threadPoolId: String, // 线程池唯一标识
  },
) {
  return requestClient.get<Array<InstancesApi.ListItem>>(`/thread-pools/${data.namespace}/${data.serviceName}/${data.threadPoolId}/basic-metrics`
  );
}

/**
 * 获取线程池实例详情
 */

async function getInstancesDetail(
  data: {
    networkAddress: String, // 网络地址
    threadPoolId: String, // 线程池唯一标识
  },
) {
  return requestClient.get<Array<InstancesApi.ListItem>>(`/thread-pool/${data.threadPoolId}/${data.networkAddress}`
  );
}
export { getThreadPoolsList, updateThreadPool, getInstancesList, getInstancesDetail, getGrafanaUrl };
