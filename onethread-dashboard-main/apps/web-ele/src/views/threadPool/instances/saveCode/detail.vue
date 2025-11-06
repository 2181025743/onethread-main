<!--
 * @Descripttion: 
 * @version: 1.0.0
 * @Author: fulei🐰
 * @Date: 2025-06-07 10:34:27
 * @LastEditors: fulei🐰
 * @LastEditTime: 2025-06-07 19:03:06
-->
<script lang="ts" setup>
import {
  ElDescriptions,
  ElDescriptionsItem,
  ElTag
} from 'element-plus';
import { ref, defineExpose } from "vue"
import type { detailDataType } from "./type"
const detailData = ref<detailDataType>({
  "threadPoolId": "",
  "activeProfile": "",
  "status": "",
  "ip": "",
  "port": "",
  "currentLoad": "",
  "peakLoad": "",
  "freeMemory": "",
  "memoryUsagePercentage": "",
  "corePoolSize": 0,
  "maximumPoolSize": 20,
  "currentPoolSize": 0,
  "activePoolSize": 0,
  "largestPoolSize": 0,
  "keepAliveTime": 0,
  "completedTaskCount": 0,
  "workQueueName": "",
  "workQueueCapacity": 0,
  "workQueueSize": 0,
  "workQueueRemainingCapacity": 0,
  "rejectedHandlerName": "",
  "rejectCount": 0,
  "currentTime": ""
})
const setValues = (data: detailDataType) => {
  console.log(`output->data`, data)
  detailData.value = data
}
defineExpose({
  setValues
})
</script>
<template>
  <div>
    <el-descriptions title="基础信息" :column="3" border>
      <el-descriptions-item label="线程池" label-align="left">
        {{ detailData.threadPoolId }}
      </el-descriptions-item>
      <el-descriptions-item label="环境标识" label-align="left">
        <el-tag>
          {{ detailData.activeProfile }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="运行状态" label-align="left">
        {{ detailData.status }}
      </el-descriptions-item>
      <el-descriptions-item label="实例Host" label-align="left">
        {{ detailData.ip + ":" + detailData.port }}
      </el-descriptions-item>
      <!-- <el-descriptions-item label="实例标识" label-align="left">
        {{ detailData.ip }}
      </el-descriptions-item> -->
    </el-descriptions>
    <el-descriptions title="负载信息" :column="3" border>
      <el-descriptions-item label="当前负载" label-align="left">
        {{ detailData.currentLoad }}
      </el-descriptions-item>
      <el-descriptions-item label="峰值负载" label-align="left">
        {{ detailData.peakLoad }}
      </el-descriptions-item>
      <el-descriptions-item label="剩余内存" label-align="left">
        {{ detailData.freeMemory }}
      </el-descriptions-item>
      <el-descriptions-item label="内存占比" label-align="left">
        {{ detailData.memoryUsagePercentage }}
      </el-descriptions-item>
    </el-descriptions>
    <el-descriptions title="线程信息" :column="3" border>
      <el-descriptions-item label="核心线程" label-align="left">
        {{ detailData.corePoolSize }}
      </el-descriptions-item>
      <el-descriptions-item label="当前线程" label-align="left">
        {{ detailData.currentPoolSize }}
      </el-descriptions-item>
      <el-descriptions-item label="最大线程" label-align="left">
        {{ detailData.maximumPoolSize }}
      </el-descriptions-item>
      <el-descriptions-item label="活跃线程" label-align="left">
        {{ detailData.activePoolSize }}
      </el-descriptions-item>
      <el-descriptions-item label="同存最大线程" label-align="left">
        {{ detailData.largestPoolSize }}
      </el-descriptions-item>
    </el-descriptions>
    <el-descriptions title="队列信息" :column="3" border>
      <el-descriptions-item label="队列容量" label-align="left">
        {{ detailData.corePoolSize }}
      </el-descriptions-item>
      <el-descriptions-item label="队列元素" label-align="left">
        {{ detailData.currentPoolSize }}
      </el-descriptions-item>
      <el-descriptions-item label="队列剩余容量" label-align="left">
        {{ detailData.maximumPoolSize }}
      </el-descriptions-item>
      <el-descriptions-item label="阻塞队列" label-align="left">
        {{ detailData.workQueueName }}
      </el-descriptions-item>
    </el-descriptions>
    <el-descriptions title="其他信息" :column="3" border>
      <el-descriptions-item label="任务总量" label-align="left">
        {{ detailData.completedTaskCount }}
      </el-descriptions-item>
      <el-descriptions-item label="拒绝次数" label-align="left">
        {{ detailData.rejectCount }}
      </el-descriptions-item>
      <el-descriptions-item label="最后更新时间" label-align="left">
        {{ detailData.currentTime }}
      </el-descriptions-item>
      <el-descriptions-item label="拒绝策略" label-align="left">
        {{ detailData.rejectedHandlerName }}
      </el-descriptions-item>
    </el-descriptions>
  </div>

</template>
<style lang="scss" scoped>
.el-descriptions {
  margin-bottom: 40px;
}
</style>
