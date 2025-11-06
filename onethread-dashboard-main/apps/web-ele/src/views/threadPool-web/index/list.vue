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
import { getThreadPoolsWebList, updateThreadPool } from '#/api/threadPools-web';
import type { threadPoolsWebApi } from "#/api/threadPools-web"
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
    columns: useColumns(onActionClick),
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
          const res: any = await getThreadPoolsWebList({
            namespace: formValues.namespace || "",
            serviceName: formValues.serviceName || "",
          });
          const result = res.items.map((item: any) => {
            return {
              ...item,
            }
          })
          // res.items = result
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
  } as VxeTableGridOptions<threadPoolsWebApi.ListItem>
});
// 当前点击行
const defaultRow: TableRow = {
  namespace: '',
  serviceName: '',
  dataId: '',
  group: '',
  webContainerName: "",
  corePoolSize: 0,
  maximumPoolSize: 0,
  keepAliveTime: 0,
  instanceCount: 0,
  notify: {
    receives: "",
  },
}
const currentRow = ref<TableRow>({
  ...defaultRow
})
function onActionClick(params: OnActionClickParams<threadPoolsWebApi.ListItem>) {
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
    receives: currentRow.value?.notify?.receives || "",
  }
  formApi.setValues(result);
}
function routeTo(row: any) {
  router.push({
    path: '/thread-pools/web/instances',
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
    notify: {
      receives: values.receives,
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
  ElMessage.success('web线程池参数修改成功')
  drawerApi.close()
  gridApi.query()
  currentRow.value = {
    ...defaultRow
  }
}
</script>
