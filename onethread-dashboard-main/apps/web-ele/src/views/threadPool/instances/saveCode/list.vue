<!--
 * @Descripttion: 
 * @version: 1.0.0
 * @Author: fulei🐰
 * @Date: 2025-06-07 10:34:27
 * @LastEditors: fulei🐰
 * @LastEditTime: 2025-06-08 17:20:21
-->
<script lang="ts" setup>
import { Page, useVbenDrawer } from '@vben/common-ui';
import Detail from './detail.vue';
import {
  ElCard,
  ElTable,
  ElButton,
  ElTableColumn,
  ElForm,
  ElFormItem,
  ElInput,
  ElRow,
  ElCol,
  ElMessage,
  ElPagination
} from 'element-plus';
import { $t } from '#/locales';
import { ref } from "vue"
import { useRoute } from 'vue-router';
import { getInstancesList, getInstancesDetail } from '#/api/threadPools';
import type { tableDataType } from './type'
const page = ref(1) // 当前页码
const pageSize = ref(10) // 每页条数
const total = ref(0) // 总条数
const route = useRoute();
const queryParams: any = route.query // 获取路由参数
const [Drawer, drawerApi] = useVbenDrawer(); // 详情弹窗
const detailRef = ref<any>(); // 详情弹窗实例
const form: any = ref({ // 查询表单入参
  namespace: "",
  serviceName: "",
  threadPoolId: "",
})
form.value.namespace = queryParams?.namespace || ""
form.value.serviceName = queryParams?.serviceName || ""
form.value.threadPoolId = queryParams?.threadPoolId || ""

const loading = ref(false)
const tableData = ref<tableDataType>([]);
const queryForm = ref({ // 详情接口入参
  threadPoolId: "",
  networkAddress: "",
})
// 查看详情
const handleClick = (row: any) => {
  queryForm.value.threadPoolId = row.threadPoolId
  queryForm.value.networkAddress = row.networkAddress
  drawerApi.open()
  getDetail()
}
function getDetail() {
  getInstancesDetail(queryForm.value)
    .then((res: any) => {
      // 添加数据有效性检查
      if (!res?.data) {
        throw new Error('接口返回数据格式异常')
      }
      
      detailRef.value?.setValues(res.data);
    })
    .catch(error => {
      console.error('获取详情失败:', error);
      ElMessage.error('加载线程池详情失败');
    });
}

// 查询列表
const getList = () => {
  if (form.value.namespace && form.value.serviceName && form.value.threadPoolId) {
    getInstancesList(form.value).then((res) => {
      tableData.value = paginatedTableData(res.items)
    })
  } else {
    tableData.value = []
    ElMessage.error('请输入必填信息')
  }
}
const resetForm = () => {
  form.value.namespace = ""
  form.value.serviceName = ""
  form.value.threadPoolId = ""
}
const onRefresh = () => {
  getDetail()
}
const paginatedTableData = (tableData: any) => {
  const start = (page.value - 1) * pageSize.value
  const end = start + pageSize.value
  return tableData.slice(start, end)
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
const onClose = () => {
  queryForm.value.threadPoolId = ""
  queryForm.value.networkAddress = ""
  drawerApi.close()
}
if(form.value.namespace && form.value.serviceName && form.value.threadPoolId){
  getList()
}
</script>
<template>
  <Page title="线程池列表">
    <Drawer class="w-[800px]" title="查看详情">
      <Detail ref="detailRef" />
      <template #footer>
        <el-button type="primary" @click="onRefresh">刷新</el-button>
        <el-button @click="onClose">关闭</el-button>
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
          <el-col :span="4">
            <el-form-item label="线程池ID">
              <el-input v-model="form.threadPoolId" placeholder="请输入线程池ID" />
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
      <el-table :data="tableData" stripe>
        <el-table-column label="序号" type="index" align="center" width="90" flxed="left" />
        <el-table-column :label="$t('threadPool.instances.threadPoolId')" prop="threadPoolId" />
        <el-table-column :label="$t('threadPool.instances.activeProfile')" prop="activeProfile" />
        <el-table-column :label="$t('threadPool.instances.networkAddress')" prop="networkAddress" />
        <el-table-column :label="$t('threadPool.instances.corePoolSize')" prop="corePoolSize" />
        <el-table-column :label="$t('threadPool.instances.maximumPoolSize')" prop="maximumPoolSize" />
        <el-table-column :label="$t('threadPool.instances.workQueueName')" prop="workQueueName" />
        <el-table-column :label="$t('threadPool.instances.workQueueSize')" prop="workQueueSize" />
        <el-table-column :label="$t('threadPool.instances.workQueueRemainingCapacity')"
          prop="workQueueRemainingCapacity" />
        <el-table-column :label="$t('threadPool.instances.rejectedHandlerName')" prop="rejectedHandlerName" />
        <el-table-column :label="$t('threadPool.instances.rejectCount')" prop="rejectCount" />
        <el-table-column fixed="right" label="操作" width="200" align="center" flxed="right">
          <template #default="scope">
            <el-button type="primary" @click="handleClick(scope.row)" size="small">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
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
