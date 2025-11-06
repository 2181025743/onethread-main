import { $t } from '#/locales';

export const useSchema = () => {
  return [
    {
      component: 'InputNumber',
      fieldName: 'corePoolSize',
      label: $t('threadPool.index.corePoolSize'),
      rules: 'required',
    },

    {
      component: 'InputNumber',
      fieldName: 'maximumPoolSize',
      label: $t('threadPool.index.maximumPoolSize'),
      rules: 'required',
    },
    {
      component: 'Select',
      fieldName: 'workQueue',
      label: $t('threadPool.index.workQueue'),
      rules: 'required',
      componentProps: {
        filterable: true,
        options: [
          { value: 'ArrayBlockingQueue', label: 'ArrayBlockingQueue' },
          { value: 'LinkedBlockingQueue', label: 'LinkedBlockingQueue' },
          { value: 'LinkedBlockingDeque', label: 'LinkedBlockingDeque' },
          { value: 'SynchronousQueue', label: 'SynchronousQueue' },
          { value: 'LinkedTransferQueue', label: 'LinkedTransferQueue' },
          { value: 'PriorityBlockingQueue', label: 'PriorityBlockingQueue' },
          { value: 'ResizableCapacityLinkedBlockingQueue', label: 'ResizableCapacityLinkedBlockingQueue' },
        ],
      },
    },
    {
      component: 'InputNumber',
      fieldName: 'queueCapacity',
      label: $t('threadPool.index.queueCapacity'),
      rules: 'required',
    },
    {
      component: 'InputNumber',
      fieldName: 'keepAliveTime',
      label: $t('threadPool.index.kxhs'),
      rules: 'required',
    },
    {
      component: 'RadioGroup',
      fieldName: 'allowCoreThreadTimeOut',
      label: $t('threadPool.index.allowCoreThreadTimeOut'),
      rules: 'required',
      componentProps: {
        isButton: true,
        options: [
          { label: $t('threadPool.index.overTime'), value: 1 },
          { label: $t('threadPool.index.notOverTime'), value: 0 },
        ],
      },
    },
    {
      component: 'RadioGroup',
      componentProps: {
        isButton: true,
        options: [
          { label: $t('threadPool.index.call'), value: 1 },
          { label: $t('threadPool.index.notCall'), value: 0 },
        ],
        optionType: 'button',
      },
      rules: 'required',
      fieldName: 'enable',
      label: $t('threadPool.index.enable'),
    },
    {
      component: 'RadioGroup',
      rules: 'required',
      componentProps: {
        isButton: true,
        options: [
          { label: $t('threadPool.index.notCall'), value: 0 },
          { label: $t('threadPool.index.60Percent'), value: 60 },
          { label: $t('threadPool.index.80Percent'), value: 80 },
          { label: $t('threadPool.index.90Percent'), value: 90 },
        ],
        optionType: 'button',
      },
      fieldName: 'alarm.activeThreshold',
      label: $t('threadPool.index.hybj'),
    },
    {
      component: 'RadioGroup',
      rules: 'required',
      componentProps: {
        isButton: true,
        options: [
          { label: $t('threadPool.index.notCall'), value: 0 },
          { label: $t('threadPool.index.60Percent'), value: 60 },
          { label: $t('threadPool.index.80Percent'), value: 80 },
          { label: $t('threadPool.index.90Percent'), value: 90 },
        ],
        optionType: 'button',
      },
      fieldName: 'alarm.queueThreshold',
      label: $t('threadPool.index.rlbj'),
    },
    {
      component: 'Select',
      fieldName: 'rejectedHandler',
      label: $t('threadPool.index.rejectedHandler'),
      rules: 'required',
      componentProps: {
        filterable: true,
        options: [
          { value: 'CallerRunsPolicy', label: 'CallerRunsPolicy' },
          { value: 'AbortPolicy', label: 'AbortPolicy' },
          { value: 'DiscardPolicy', label: 'DiscardPolicy' },
          { value: 'DiscardOldestPolicy', label: 'DiscardOldestPolicy' },
          { value: 'AbortPolicyWithReport', label: 'AbortPolicyWithReport' },
        ],
      },
    },
    {
      component: 'Input',
      fieldName: 'notify.receives',
      label: $t('threadPool.index.receives'),
      rules: 'required',
    },
    {
      component: 'InputNumber',
      fieldName: 'notify.interval',
      label: $t('threadPool.index.interval'),
      rules: 'required',
    },
  ]
}

export interface TableRow{
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
