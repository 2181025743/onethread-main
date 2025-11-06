/*
 * @Descripttion: 
 * @version: 1.0.0
 * @Author: fulei🐰
 * @Date: 2025-06-08 15:43:03
 * @LastEditors: fulei🐰
 * @LastEditTime: 2025-06-15 13:33:26
 */
import type { OnActionClickFn, VxeTableGridOptions } from '#/adapter/vxe-table';
import type { threadPoolsApi } from "#/api/threadPools"
import { h } from 'vue';
import { ElTag } from 'element-plus';

import { $t } from '#/locales';


export function useColumns<T = threadPoolsApi.ListItem>(
  onActionClick: OnActionClickFn<T>,
): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'webContainerName',
      title: "Web容器名称",
      width: 220,
    },
    {
      field: 'networkAddress',
      title: $t('threadPool.instances.networkAddress'),
      width: 220,
    },
    {
      field: 'activeProfile',
      title: $t('threadPool.instances.activeProfile'),
      width: 150,
      slots: {
        default: ({ row }) => {
          const statusMap: Record<string, 'success' | 'warning' | 'info' | 'primary' | 'danger'> = {
            DEV: 'info',
            TEST: 'success',
            UAT: 'warning',
            PROD: 'danger',
          };
          const type = statusMap[row.activeProfile] || 'primary';
          return h(ElTag, { type }, () => row.activeProfile);
        }
      }
    },
    {
      field: 'corePoolSize',
      title: $t('threadPool.instances.corePoolSize'),
      width: 120,
      slots: {
        default: ({ row }) => {
          return h('span', { style: { color: '#007BFF' } }, row.corePoolSize);
        },
      },
    },
    {
      field: 'maximumPoolSize',
      title: $t('threadPool.instances.maximumPoolSize'),
      width: 120,
      slots: {
        default: ({ row }) => {
          return h('span', { style: { color: '#007BFF' } }, row.maximumPoolSize);
        },
      },
    },
    {
      field: 'workQueueName',
      title: $t('threadPool.instances.workQueueName'),
      minWidth: 300,
    },
    {
      field: 'workQueueCapacity',
      title: $t('threadPool.instances.workQueueCapacity'),
      minWidth: 180,
      slots: {
        default: ({ row }) => {
          const remaining = row.workQueueRemainingCapacity ?? '-';
          const size = row.workQueueSize ?? '-';
          return `${remaining} / ${size}`;
        },
      },
    },
    {
      field: 'rejectedHandlerName',
      title: $t('threadPool.instances.rejectedHandlerName'),
      width: 200,
    },
    {
      align: 'center',
      cellRender: {
        attrs: {
          nameField: 'threadPoolId',
          nameTitle: $t('threadPool.index.threadPoolId'),
          onClick: onActionClick,
        },
        options: [
          {
            code: 'viewDetail',
            text: true,
            btnName: '查看详情',
          },
        ],
        name: 'CellOperation',
      },
      field: 'operation',
      fixed: 'right',
      title: $t('threadPool.index.operation'),
      width: 130,
    },
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
