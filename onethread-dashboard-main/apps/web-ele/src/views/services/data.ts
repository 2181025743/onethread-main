/*
 * @Descripttion: 
 * @version: 1.0.0
 * @Author: fulei🐰
 * @Date: 2025-06-08 15:43:03
 * @LastEditors: fulei🐰
 * @LastEditTime: 2025-06-14 23:16:29
 */
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import { h } from 'vue';
export function useColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'namespace',
      title: "命名空间",
      minWidth: "10%",
    },
    {
      field: 'serviceName',
      title: "服务名",
      minWidth: "10%",
    },
    {
      field: 'instanceCount',
      title: "实例数量",
      minWidth: 120,
      slots: {
        default: ({ row }) => {
          return h('span', { style: { color: '#007BFF' } }, row.instanceCount);
        },
      },
    },
    {
      field: 'threadPoolCount',
      title: "线程池数量",
      minWidth: 120,
      slots: {
        default: ({ row }) => {
          return h('span', { style: { color: '#007BFF' } }, row.threadPoolCount);
        },
      },
    },
    {
      field: 'hasWebThreadPool',
      minWidth: 120,
      align: 'center',
      title: 'Web线程池', 
      
      slots: {
        default: ({ row }) => {
          const isDynamic = row.hasWebThreadPool;
          
          return h(
            'div',
            { 
              class: 'inline-flex items-center justify-center py-1 px-3 rounded-lg transition-all duration-300',
              style: {
                // 优化为柔和的半透明色彩
                background: isDynamic 
                  ? 'rgba(167, 243, 208, 0.25)'  // 柔光绿
                  : 'rgba(243, 244, 246, 0.4)',  // 柔光灰
                boxShadow: '0 1px 3px rgba(0,0,0,0.03)'
              }
            },
            [
              h('div', {
                class: `w-2.5 h-2.5 rounded-full mr-2 ${
                  isDynamic 
                    ? 'bg-teal-400 animate-pulse'  // 柔和的蓝绿色
                    : 'bg-gray-400 bg-opacity-70'  // 半透明灰色
                }`,
                style: {
                  boxShadow: isDynamic 
                    ? '0 0 5px rgba(94, 234, 212, 0.5)'  // 柔和光晕
                    : 'none'
                }
              }),
              h('span', {
                style: {
                  fontWeight: 500,
                  textShadow: '0 1px 1px rgba(255,255,255,0.8)'  // 提升可读性
                }
              }, isDynamic ? '已配置' : '未配置')
            ]
          );
        }
      }
    }

  ];
}


export interface TableRow {
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
  enable?: Boolean, // 是否启用
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
export type tableDataType = TableRow[];
