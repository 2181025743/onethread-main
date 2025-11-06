/*
 * @Descripttion: 线程池管理
 * @version: 1.0.0
 * @Author: fulei🐰
 * @Date: 2025-06-02 16:56:13
 * @LastEditors: fulei🐰
 * @LastEditTime: 2025-06-14 23:13:04
 */
import { requestClient } from '#/api/request';

export namespace projectsApi {
  export interface ListItem {
    namespace: String, // 命名空间
    serviceName: String, // 服务名称
    instanceCount: Number, // 实例数量
    threadPoolCount: Number, // 线程池数量
    hasWebThreadPool: Boolean,// Web线程池
    updateTime: String,// 修改时间
  }
}

/**
 * 获取线项目列表
 */
async function getProjectsList() {
  return requestClient.get<Array<projectsApi.ListItem>>(
    '/projects',
  );
}

export { getProjectsList };
