/*
 * @Descripttion: 
 * @version: 1.0.0
 * @Author: fulei🐰
 * @Date: 2025-06-08 15:43:03
 * @LastEditors: fulei🐰
 * @LastEditTime: 2025-06-15 13:21:42
 */
import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridOptions } from '#/adapter/vxe-table';
import type { threadPoolsApi } from "#/api/threadPools"
import { h } from 'vue';

import { $t } from '#/locales';
export const useSchema = () => {
  return [
    {
      component: 'InputNumber',
      fieldName: 'corePoolSize',
      label: "核心线程",
      rules: 'required',
    },

    {
      component: 'InputNumber',
      fieldName: 'maximumPoolSize',
      label: "最大线程",
      rules: 'required',
    },
    {
      component: 'InputNumber',
      fieldName: 'keepAliveTime',
      label: "线程空闲回收",
      rules: 'required',
    },
    {
      component: 'Input',
      fieldName: 'receives',
      label: "接收人集合",
      rules: 'required',
    },

  ]
}


export function useGridFormSchema(): VbenFormSchema[] {
  return []
}

export function useColumns<T = threadPoolsApi.ListItem>(
  onActionClick: OnActionClickFn<T>,
  onStatusChange?: (newStatus: any, row: T) => PromiseLike<boolean | undefined>,
): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'namespace',
      title: "命名空间 / 服务名称",
      width: 360,
      slots: {
        default: ({ row }) => {
          const namespace = row.namespace ?? '-';
          const serviceName = row.serviceName ?? '-';
          return h('div', {
            innerHTML: `
        <span style="color: #409EFF; font-weight: 500;">
          <i class="icon-namespace" style="margin-right: 4px;"></i>${namespace}
        </span>
        <span style="margin: 0 6px; color: #ccc;">/</span>
        <span>
          <i class="icon-service" style="margin-right: 4px;"></i>${serviceName}
        </span>
      `,
          });
        },
      },
    },
    {
      field: 'dataId',
      minWidth: 230,
      title: "数据 ID",
    },
    {
      field: 'group',
      minWidth: 230,
      title: "分组标识",
    },
    {
      field: 'webContainerName',
      minWidth: 230,
      title: "Web容器名称",
    },
    {
      field: 'corePoolSize',
      title: "核心线程数",
      minWidth: 120,
      slots: {
        default: ({ row }) => {
          return h('span', { style: { color: '#007BFF' } }, row.corePoolSize);
        },
      },
    },
    {
      field: 'maximumPoolSize',
      title: "最大线程数",
      minWidth: 120,
      slots: {
        default: ({ row }) => {
          return h('span', { style: { color: '#007BFF' } }, row.maximumPoolSize);
        },
      },
    },
    {
      field: 'keepAliveTime',
      minWidth: 230,
      title: "线程空闲存活时间（单位：秒）",
    },
    {
      field: 'instanceCount',
      minWidth: 120,
      title: "实例数量",
      slots: {
        default: ({ row }) => {
          return h('span', { style: { color: '#28a745', fontWeight: 'bold' } }, row.instanceCount);
        },
      },
    },
    {
      align: 'center',
      cellRender: {
        attrs: {
          nameField: 'namespace',
          nameTitle: $t('threadPool.index.namespace'),
          onClick: onActionClick,
        },
        options: [
          {
            code: 'append',
            text: true,
            btnName: "实例列表"
          },
          {
            code: 'edit',
            text: true,
            btnName: "编辑"
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
  webContainerName: String,// Web容器名称
  corePoolSize: Number, // 核心线程数
  maximumPoolSize: Number, // 最大线程数
  keepAliveTime: Number, // 线程空闲存活时间（单位：秒）
  instanceCount: Number, // 实例数量
  notify?: { // 通知配置
    receives: string, // 接收人集合
  }
}
export type tableDataType = TableRow[];
