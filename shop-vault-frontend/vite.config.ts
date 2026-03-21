import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // 如果后端接口本身没有 /api 前缀，则需要 rewrite
        // 但根据你的说明，后端接口统一带 /api，所以这里不需要 rewrite
      },
    },
  },
})