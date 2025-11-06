<template>
  <Page auto-content-height>
    <Drawer class="w-[600px]" title="编辑">
      <Form />
      <template #footer>
        <div></div>
      </template>
    </Drawer>
    <Grid :table-title="$t('threadPool.index.title')">
    </Grid>
  </Page>
</template>
<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus';
import { useVbenForm } from '#/adapter/form';
import { Page, useVbenDrawer } from '@vben/common-ui';
import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getThreadPoolsList, updateThreadPool } from '#/api/threadPools';
import type { threadPoolsApi } from "#/api/threadPools"
import { $t } from '#/locales';
import { ref } from "vue"
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import { useColumns, useSchema } from "./data"
import { useRouter } from 'vue-router'
import type { TableRow } from './data'
import { paginate } from "#/utils/index"
const router = useRouter()
const [Drawer, drawerApi] = useVbenDrawer();
const [Form, formApi] = useVbenForm({
  commonConfig: {
    // 所有表单项
    componentProps: {
      class: 'w-full',
    },
  },
  resetButtonOptions: {
    content: "关闭"
  },
  layout: 'horizontal',
  handleSubmit: (values) => {
    onSubmit(values)
  },
  handleReset: () => {
    drawerApi.close()
  },
  schema: useSchema()
});
const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: [
      {
        component: 'Input',
        fieldName: 'namespace',
        label: $t('threadPool.index.namespace'),
      },
      {
        component: 'Input',
        fieldName: 'serviceName',
        label: $t('threadPool.index.serviceName'),
      },
    ],
    showCollapseButton: false,
    submitOnChange: false,
  },
  gridOptions: {
    columns: useColumns(onActionClick, onStatusChange),
    height: 'auto',
    keepSource: true,
    pagerConfig: {
      enabled: true, // 启用分页
      pageSize: 20, // 每页显示数量
      pageSizes: [10, 20, 50, 100], // 可选的每页显示数量
      currentPage: 1, // 当前页码
    },
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          const res: any = await getThreadPoolsList({
            namespace: formValues.namespace || "",
            serviceName: formValues.serviceName || "",
          });
          const result = res.items.map((item: any) => {
            return {
              ...item,
              enable: item.alarm.enable,
            }
          })
          const { paginatedData } = paginate(result, page.currentPage, page.pageSize)
          res.items = paginatedData
          return res
        },
      },
    },
    rowConfig: {
      keyField: 'threadPoolId',
    },

    toolbarConfig: {
      custom: true,
      export: false,
      refresh: { code: 'query' },
      search: true,
      zoom: true,
    },
  } as VxeTableGridOptions<threadPoolsApi.ListItem>
});
// 当前点击行
const defaultRow: TableRow = {
  namespace: '',
  serviceName: '',
  dataId: '',
  group: '',
  threadPoolId: '',
  corePoolSize: 0,
  maximumPoolSize: 0,
  queueCapacity: 0,
  workQueue: '',
  rejectedHandler: '',
  keepAliveTime: 0,
  allowCoreThreadTimeOut: false,
  enable: false,
  alarm: {
    enable: false,
    queueThreshold: 0,
    activeThreshold: 0,
  },
  notify: {
    receives: "",
    interval: 0,
  },
}
const currentRow = ref<TableRow>({
  ...defaultRow
})
function onActionClick(params: OnActionClickParams<threadPoolsApi.ListItem>) {
  if (params.code === 'edit') {
    onEdit(params.row)
  } else {
    if (params.row.instanceCount === 0 || params.row.instanceCount === null) {
      return ElMessage({ type: 'warning', message: "实例数量为空，无法查看实例详情" });
    }
    routeTo(params.row)
  }
}
function onEdit(row: any) {
  currentRow.value = row
  setFormValues()
  drawerApi.open()
}
function setFormValues() {
  const result = {
    ...currentRow.value,
    allowCoreThreadTimeOut: currentRow.value.allowCoreThreadTimeOut ? 1 : 0,
    enable: currentRow.value?.enable ? 1 : 0,
    receives: currentRow.value?.notify?.receives || "",
    interval: currentRow.value?.notify?.interval || "",
    activeThreshold: currentRow.value?.alarm?.activeThreshold || 0,
    queueThreshold: currentRow.value?.alarm?.queueThreshold || 0
  }
  formApi.setValues(result);
}
function routeTo(row: any) {
  router.push({
    path: '/thread-pool/instances',
    query: {
      namespace: row.namespace,
      serviceName: row.serviceName,
      threadPoolId: row.threadPoolId,
    },
  })
}
function onSubmit(values: any) {
  const result = {
    ...currentRow.value,
    ...values,
    allowCoreThreadTimeOut: !!values.allowCoreThreadTimeOut,
    alarm: {
      ...values.alarm,
      enable: !!values.enable,
      activeThreshold: values.activeThreshold,
      queueThreshold: values.queueThreshold,
    },
    notify: {
      receives: values.receives,
      interval: values.interval,
    }
  }
  updateThreadPool(result)
    .then(() => {
      onChangeAffter()
    })
    .catch(error => {
      ElMessage.error(error.message)
    })
}
function onChangeAffter() {
  ElMessage.success('线程池参数修改成功')
  drawerApi.close()
  gridApi.query()
  currentRow.value = {
    ...defaultRow
  }
}
/**
 * 状态开关即将改变
 * @param newStatus 期望改变的状态值
 * @param row 行数据
 * @returns 返回false则中止改变，返回其他值（undefined、true）则允许改变
 */
async function onStatusChange(
  newStatus: any,
  row: any,
) {
  console.log(`output->newStatus`, newStatus)
  ElMessageBox.confirm(
    `你要将告警配置的状态切换为 【${row.enable ? '不告警' : '告警'}】 吗？`,
    `切换状态`,
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(() => {
      const result = {
        ...row,
        alarm: {
          ...row.alarm,
          enable: !!row.enable,
        },
      }
      updateThreadPool(result)
        .then(() => {
          onChangeAffter()
        })
        .catch(error => {
          ElMessage.error(error.message)
        })
    })
    .catch(() => {
      row.enable = !row.enable
    })
}
</script>
