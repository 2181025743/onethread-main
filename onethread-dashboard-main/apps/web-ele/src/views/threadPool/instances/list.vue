<template>
  <Page auto-content-height>
    <Drawer class="w-[800px]" title="查看详情">
      <Detail ref="detailRef" />
      <template #footer>
        <div>
          <el-button type="primary" @click="onRefresh">刷新</el-button>
          <el-button @click="onClose">关闭</el-button>
        </div>
      </template>
    </Drawer>
    <Grid table-title="线程池实例列表"></Grid>
  </Page>
</template>

<script setup lang="ts">
import { ElButton, ElMessage } from 'element-plus';
import { Page, useVbenDrawer } from '@vben/common-ui';
import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getInstancesList, getInstancesDetail } from '#/api/threadPools';
import { $t } from '#/locales';
import { ref, nextTick, onMounted } from "vue"
import { useColumns } from "./data"
import { useRoute } from 'vue-router';
import Detail from './detail.vue';
import { paginate } from "#/utils/index"

const route = useRoute();
const detailRef = ref<any>();

const [Drawer, drawerApi] = useVbenDrawer();
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
      {
        component: 'Input',
        fieldName: 'threadPoolId',
        label: $t('threadPool.index.threadPoolId'),
      },
    ],
    submitOnChange: false,
    submitOnEnter: false,
    handleReset: handleReset,
    handleSubmit: queryList
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
      autoLoad: false,
      ajax: {
        query: async ({ page }: any) => {
          const dataRef = await gridApi.formApi.getValues();
          const { namespace, serviceName, threadPoolId } = dataRef;
          if (!namespace || !serviceName || !threadPoolId) {
            return { items: [], total: 0 };
          }
          const res = await getInstancesList({
            namespace: namespace,
            serviceName: serviceName,
            threadPoolId: threadPoolId,
          });
          const { paginatedData } = paginate(res.items, page.currentPage, page.pageSize)
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
  }
});

const queryForm = ref({
  threadPoolId: "",
  networkAddress: "",
});

function queryList(values: any) {
  if (!values.namespace || !values.serviceName || !values.threadPoolId) {
    return ElMessage({ type: 'warning', message: "请先输入查询条件！" });
  }
  gridApi.query(values);
}

// 修复重置函数
function handleReset() {
  // 安全地清空表格数据
  gridApi.query({});
}

const initFormValues = () => {
  const { namespace, serviceName, threadPoolId } = route.query;
  const formValues = {
    namespace: namespace || '',
    serviceName: serviceName || '',
    threadPoolId: threadPoolId || '',
  };

  nextTick(async () => {
    await gridApi.formApi.setValues(formValues);

    if (namespace && serviceName && threadPoolId) {
      gridApi.query(formValues);
    }
  });
};

onMounted(() => {
  initFormValues();
});

const onClose = () => {
  drawerApi.close();
};

const onRefresh = () => {
  getDetail();
};

function onActionClick(params: any) {
  if (params.code === 'viewDetail') {
    queryForm.value.threadPoolId = params.row.threadPoolId;
    queryForm.value.networkAddress = params.row.networkAddress;
    drawerApi.open();
    getDetail();
  }
}

function getDetail() {
  getInstancesDetail(queryForm.value)
    .then((res: any) => {
      detailRef.value.setValues(res);
    });
}
</script>