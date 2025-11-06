/*
 * @Descripttion: 线程池管理
 * @version: 1.0.0
 * @Author: fulei🐰
 * @Date: 2025-06-02 16:56:13
 * @LastEditors: fulei🐰
 * @LastEditTime: 2025-06-15 13:29:28
 */
import { requestClient } from '#/api/request';

export namespace threadPoolsWebApi {
  export interface ListItem {
    namespace: String, // 命名空间
    serviceName: String, // 服务名称
    dataId: String, // 数据ID
    group?: String, // 分组标识
    webContainerName: String,// Web容器名称
    corePoolSize: Number, // 核心线程数
    maximumPoolSize: Number, // 最大线程数
    keepAliveTime: Number, // 线程空闲存活时间（单位：秒）
    instanceCount: Number, // 实例数量
    notify?: { // 通知配置
      receives: string, // 接收人集合
    }
  }
}
export namespace InstancesApi {
  export interface ListItem {
    webContainerName: String, // Web容器名称
    networkAddress: String // 实例标识,
    corePoolSize: Number // 核心线程数,
    maximumPoolSize: Number // 最大线程数,
    keepAliveTime: Number// 线程空闲存活时间,
    workQueueName: String// 阻塞队列类型,
    workQueueCapacity: Number // 队列容量,
    workQueueSize: Number// 队列元素数量,
    workQueueRemainingCapacity: Number // 队列剩余容量,
    rejectedHandlerName: String // 拒绝策略,
    activeProfile: String // 当前环境,
  }
}


/**
 * 获取线程池列表
 */
async function getThreadPoolsWebList(
  data: {
    namespace: String, // 命名空间
    serviceName: String, // 服务名称
  }
) {
  return requestClient.get<Array<threadPoolsWebApi.ListItem>>(
    '/web/thread-pools' + `?namespace=${data.namespace}&serviceName=${data.serviceName}`,
  );
}

/**
 * 线程池变更
 */
async function updateThreadPool(
  data: Omit<threadPoolsWebApi.ListItem, ''>,
) {
  return requestClient.put(`/web/thread-pool`, data);
}


/**
 * 获取线程池实例列表
 */

async function getInstancesList(
  data: {
    namespace: String, // 命名空间
    serviceName: String // 服务名称
  },
) {
  return requestClient.get<Array<InstancesApi.ListItem>>(`/web/thread-pools/${data.namespace}/${data.serviceName}/basic-metrics`
  );
}

/**
 * 获取线程池实例详情
 */

async function getInstancesDetail(
  data: {
    networkAddress: String, // 网络地址
  },
) {
  return requestClient.get<Array<InstancesApi.ListItem>>(`/web/thread-pool/${data.networkAddress}`
  );
}
export { getThreadPoolsWebList, updateThreadPool, getInstancesList, getInstancesDetail };
