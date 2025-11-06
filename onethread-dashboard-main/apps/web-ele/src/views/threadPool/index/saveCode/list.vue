<!--
 * @Descripttion: 线程池列表
 * @version: 1.0.0
 * @Author: fulei🐰
 * @Date: 2025-06-07 10:34:27
 * @LastEditors: fulei🐰
 * @LastEditTime: 2025-06-08 17:20:00
-->
<script lang="ts" setup>
import { useSchema } from "./data"
import type { tableDataType, TableRow } from './data'
import { Page, useVbenDrawer } from '@vben/common-ui';
import {
  ElCard,
  ElTable,
  ElButton,
  ElTableColumn,
  ElMessage,
  ElSwitch,
  ElPagination,
  ElForm,
  ElFormItem,
  ElInput,
  ElRow,
  ElCol,
} from 'element-plus';
import { useVbenForm } from '#/adapter/form';
import { $t } from '#/locales';
import { getThreadPoolsList, updateThreadPool } from '#/api/threadPools';
import { ref } from "vue"
import { useRouter } from 'vue-router'
const router = useRouter()
let                                                                                                                                                            copyRow: any = {}
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
const curtrentRow = ref<TableRow>({
  ...defaultRow
})
// useVbenForm生成抽屉表单
const [Form, formApi] = useVbenForm({
  commonConfig: {
    // 所有表单项
    componentProps: {
      class: 'w-full',
    },
  },
  resetButtonOptions: {
    content:"关闭"
  },
  layout: 'horizontal',
  handleSubmit: (values) => {
    onSubmit(values)
  },
  handleReset: () => {
    drawerApi.close()
    // ElMessage.success('重置成功')
  },
  schema: useSchema()
});

const page = ref(1) // 当前页码
const pageSize = ref(10) // 每页条数
const total = ref(0) // 总条数
const form = ref({
  namespace: '',
  serviceName: '',
})
// 表单项
const [Drawer, drawerApi] = useVbenDrawer();
const tableData = ref<tableDataType>([]) // 表格数据
function setFormValues() {
  const result = {
    ...curtrentRow.value,
    allowCoreThreadTimeOut: curtrentRow.value.allowCoreThreadTimeOut ? 1 : 0,
    alarm: {
      ...curtrentRow.value.alarm,
    },
    enable: curtrentRow.value?.alarm?.enable ? 1 : 0,
  }
  formApi.setValues(result);
}
function onSubmit(values: any) {
  const result = {
    ...curtrentRow.value,
    ...values,
    allowCoreThreadTimeOut: !!values.allowCoreThreadTimeOut,
    alarm: {
      ...values.alarm,
      enable: !!values.enable,
    },
    notify: {
      receives: values.notify.receives,
      interval: values.notify.interval,
    }
  }
  updateThreadPool(result)
    .then(() => {
      ElMessage.success('修改成功')
      drawerApi.close()
      getList()
      curtrentRow.value = {
        ...defaultRow
      }
      copyRow.value = {}
    })
    .catch(error => {
      ElMessage.error(error.message)
  })
}
const handleClick = (row: any) => {
  curtrentRow.value = row
  setFormValues()
  drawerApi.open()
}
const getList = () => {
  getThreadPoolsList({
    namespace:form.value.namespace,
    serviceName:form.value.serviceName,
  }).then((res) => {
    tableData.value = paginatedTableData(res.items)
    total.value = res.items.length
  })
}
const paginatedTableData = (tableData: any) => {
  const start = (page.value - 1) * pageSize.value
  const end = start + pageSize.value
  return tableData.slice(start, end)
}
const routeTo = (row: any) => {
  router.push({
    path: '/thread-pool/instances',
    query: {
      namespace: row.namespace,
      serviceName: row.serviceName,
      threadPoolId: row.threadPoolId,
    },
  })
}
function handleSizeChange(val: number) {
  pageSize.value = val
  page.value = 1
  getList()
}
function handleCurrentChange(val: number) {
  page.value = val
  getList()
}
const changeEnable = (row: any) => {
  const result = {
    ...row,
    alarm: {
      ...row.alarm,
      enable:row.alarm.enable,
    }
  }
  updateThreadPool(result)
    .then(() => {
      ElMessage.success('修改成功')
      getList()
    })
    .catch(error => {
      ElMessage.error(error.message)
  })
}
const resetForm = () => {
  form.value = {
    namespace: '',
    serviceName: '',
  }
  page.value = 1
  pageSize.value = 10
  getList()
}
getList()
</script>
<template>
  <Page title="线程池列表">
    <Drawer class="w-[600px]" title="编辑">
      <Form />
      <template #footer>
        <div></div>
      </template>
    </Drawer>
    <ElCard class="mb-5">
      <el-form :model="form" label-width="auto">
        <el-row>
          <el-col :span="4">
            <el-form-item label="命名空间">
              <el-input v-model="form.namespace" placeholder="请输入命名空间" />
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="服务名">
              <el-input v-model="form.serviceName" placeholder="请输入服务名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <div class="flex-left">
              <el-button type="primary" @click="getList">查询</el-button>
              <el-button @click="resetForm">重置</el-button>
            </div>
          </el-col>
        </el-row>
      </el-form>
      <ElTable :data="tableData" stripe>
        <el-table-column label="序号" type="index" align="center" width="90" flxed="left" />
        <el-table-column :label="$t('threadPool.index.namespace')" prop="namespace" align="center" flxed="left" />
        <el-table-column :label="$t('threadPool.index.serviceName')" prop="serviceName" flxed="left" />
        <el-table-column :label="$t('threadPool.index.threadPoolId')" prop="threadPoolId" flxed="left" />
        <el-table-column :label="$t('threadPool.index.corePoolSize')" prop="corePoolSize" align="center" />
        <el-table-column :label="$t('threadPool.index.maximumPoolSize')" prop="maximumPoolSize" align="center" />
        <el-table-column :label="$t('threadPool.index.workQueue')" prop="workQueue" />
        <el-table-column :label="$t('threadPool.index.queueCapacity')" prop="queueCapacity" align="center" />
        <el-table-column :label="$t('threadPool.index.rejectedHandler')" prop="rejectedHandler" />
        <el-table-column :label="$t('threadPool.index.alarm')" prop="alarm">
          <template #default="scope">
            <el-switch v-model="scope.row.alarm.enable"
              style="--el-switch-on-color: #13ce66; --el-switch-off-color: #ff4949" @click="changeEnable(scope.row)"/>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="200" align="center" flxed="right">
          <template #default="scope">
            <el-button type="primary" @click="handleClick(scope.row)" size="small">修改</el-button>
            <el-button size="small" @click="routeTo(scope.row)">实例列表</el-button>
          </template>
        </el-table-column>
      </ElTable>
      <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange" :current-page="page"
        :page-sizes="[10, 20, 30, 50]" :page-size="pageSize" layout="total, sizes, prev, pager, next, jumper"
        :total="total">
      </el-pagination>
    </ElCard>
  </Page>
</template>
<style lang="scss" scoped>
.flex-left {
  margin-left: 50px;
  display: flex;
  justify-content: left;
}
</style>
