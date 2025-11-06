/*
 * @Descripttion: 
 * @version: 1.0.0
 * @Author: fulei🐰
 * @Date: 2025-06-02 15:25:36
 * @LastEditors: fulei🐰
 * @LastEditTime: 2025-06-08 21:44:54
 */
import type { RouteRecordRaw } from 'vue-router';

import { $t } from '#/locales';

const routes: RouteRecordRaw[] = [
  {
    meta: {
      icon: 'lucide:layout-dashboard',
      order: 9997,
      title: $t('threadPool.title'),
    },
    name: 'threadPools',
    path: '/threadPools',
    children: [
      {
        path: '/thread-pools',
        name: 'threadPoolsIndex',
        meta: {
          title: $t('threadPool.index.title'),
          badgeType: 'normal',
          badge: 'Hot',
          badgeVariants: 'primary'
        },
        component: () => import('#/views/threadPool/index/list.vue'),
      },
      {
        path: '/thread-pool/instances',
        name: 'threadPoolsInstances',
        meta: {
          title: $t('threadPool.instances.title'),
        },
        component: () => import('#/views/threadPool/instances/list.vue'),
      },
    ],
  },
  {
    meta: {
      icon: 'lucide:area-chart',
      order: 9998,
      title: $t('threadPool.grafana.title'),
      badgeType: 'dot',
      badgeVariants: 'destructive'
    },
    name: 'grafana',
    path: '/thread-pool/grafana',
    component: () => import('#/views/threadPool/grafana/list.vue'),
  },
];

export default routes;
