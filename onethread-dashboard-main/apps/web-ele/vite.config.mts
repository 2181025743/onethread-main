/*
 * @Descripttion:
 * @version: 1.0.0
 * @Author: fulei🐰
 * @Date: 2025-06-07 09:41:08
 * @LastEditors: fulei🐰
 * @LastEditTime: 2025-06-29 16:45:49
 */
import { defineConfig } from '@vben/vite-config';
import ElementPlus from 'unplugin-element-plus/vite';

export default defineConfig(async () => {
  return {
    application: {},
    vite: {
      plugins: [
        ElementPlus({
          format: 'esm',
        }),
      ],
      server: {
        host: '127.0.0.1',
        strictPort: false,
        proxy: {
          // 精确匹配所有/api开头的请求
          '^/api/': {
            target: 'http://localhost:19890', // 仅保留基础URL
            changeOrigin: true,
            // 路径重写：移除/api前缀，替换为/api/onethread-dashboard
            rewrite: (path) => path.replace(/^\/api/, '/api/onethread-dashboard'),
            ws: true,
          }
        },
      },
    },
  };
});
