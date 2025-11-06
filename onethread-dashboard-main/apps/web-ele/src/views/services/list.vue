<!--
 * @Descripttion: 
 * @version: 1.0.0
 * @Author: fulei🐰
 * @Date: 2025-06-14 23:00:51
 * @LastEditors: fulei🐰
 * @LastEditTime: 2025-06-14 23:13:31
-->
<template>
  <Page auto-content-height>
    <Grid table-title="项目列表">
    </Grid>
  </Page>
</template>
<script setup lang="ts">
import {  ElMessage} from 'element-plus';
import { Page} from '@vben/common-ui';
import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getProjectsList } from '#/api/services';
import type { projectsApi } from "#/api/services"
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import { useColumns} from "./data"
import { useRouter } from 'vue-router'

const router = useRouter()
const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          const res:any = await getProjectsList();
          const result = res.items.map((item:any) => {
            return {
              ...item,
            }
          })
          res.items = result
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

</script>
