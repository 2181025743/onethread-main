/*
 * @Descripttion: 
 * @version: 1.0.0
 * @Author: fulei🐰
 * @Date: 2025-06-07 14:52:29
 * @LastEditors: fulei🐰
 * @LastEditTime: 2025-06-15 13:30:05
 */
export interface queryParms {
  namespace:String , 
  serviceName: String, 
}

export interface TableRow {
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

export interface detailDataType {
  threadPoolId: String,
  activeProfile:String ,
  status:String ,
  ip: String,
  port:String ,
  currentLoad: String,
  peakLoad: String,
  freeMemory: String,
  memoryUsagePercentage: String,
  corePoolSize: Number,
  maximumPoolSize: Number,
  currentPoolSize: Number,
  activePoolSize: Number,
  largestPoolSize: Number,
  keepAliveTime: Number,
  completedTaskCount: Number,
  workQueueName: String,
  workQueueCapacity: Number,
  workQueueSize: Number,
  workQueueRemainingCapacity: Number,
  rejectedHandlerName: String,
  rejectCount: Number,
  currentTime: String
}


export type tableDataType = TableRow[];
