/*
 * @Descripttion: 
 * @version: 1.0.0
 * @Author: fulei🐰
 * @Date: 2025-06-02 15:25:36
 * @LastEditors: fulei🐰
 * @LastEditTime: 2025-06-15 12:57:55
 */
import type { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    meta: {
      icon: 'lucide:laptop-minimal',
      order: 9995,
      title: "项目列表",
    },
    name: 'services',
    path: '/services',
    component: () => import('#/views/services/list.vue'),
  },
];

export default routes;
