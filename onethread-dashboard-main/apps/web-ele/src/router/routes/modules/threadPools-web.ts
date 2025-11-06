/*
 * @Descripttion: 
 * @version: 1.0.0
 * @Author: fulei🐰
 * @Date: 2025-06-02 15:25:36
 * @LastEditors: fulei🐰
 * @LastEditTime: 2025-06-15 13:24:21
 */
import type { RouteRecordRaw } from 'vue-router';
const routes: RouteRecordRaw[] = [
  {
    meta: {
      icon: 'lucide:layout-template',
      order: 9999,
      title: "Web线程池管理",
    },
    name: 'threadPools-web',
    path: '/threadPools-web',
    children: [
      {
        path: '/thread-pools/web',
        name: 'threadPoolsWeb',
        meta: {
          title: "线程池列表",
        },
        component: () => import('#/views/threadPool-web/index/list.vue'),
      },
      {
        path: '/thread-pools/web/instances',
        name: 'threadPoolsWebInstances',
        meta: {
          title: "线程池实例",
        },
        component: () => import('#/views/threadPool-web/instances/list.vue'),
      },
    ],
  },

];

export default routes;
