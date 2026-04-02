import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'
import './style.css'
import App from './App.vue'
import router from './router'
import { LazyLoadDirective } from './directives'

let app: ReturnType<typeof createApp> | null = null
let pinia: ReturnType<typeof createPinia> | null = null

const errorHandler = (err: unknown, instance: any, info: string) => {
  console.error('Vue Error:', err)
  console.error('Error Info:', info)
}

const warnHandler = (msg: string, instance: any, trace: string) => {
  console.warn('Vue Warning:', msg)
  console.warn('Trace:', trace)
}

const globalErrorHandler = (event: ErrorEvent) => {
  console.error('Global Error:', event.error)
}

const unhandledRejectionHandler = (event: PromiseRejectionEvent) => {
  console.error('Unhandled Promise Rejection:', event.reason)
}

function cleanup() {
  if (app) {
    app.unmount()
    app = null
  }
  
  window.removeEventListener('error', globalErrorHandler)
  window.removeEventListener('unhandledrejection', unhandledRejectionHandler)
}

function initApp() {
  cleanup()
  
  app = createApp(App)
  pinia = createPinia()
  
  app.use(pinia)
  app.use(router)
  app.use(ElementPlus, { locale: zhCn })
  
  app.directive('lazy', LazyLoadDirective)
  
  for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
  }
  
  app.config.errorHandler = errorHandler
  app.config.warnHandler = warnHandler
  
  window.addEventListener('error', globalErrorHandler)
  window.addEventListener('unhandledrejection', unhandledRejectionHandler)
  
  app.mount('#app')
}

if (import.meta.hot) {
  import.meta.hot.dispose(() => {
    cleanup()
  })
}

initApp()
