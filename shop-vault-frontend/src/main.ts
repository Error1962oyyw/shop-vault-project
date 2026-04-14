import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'element-plus/dist/index.css'
import './style.css'
import App from './App.vue'
import router from './router'
import { LazyLoadDirective } from './directives'

let app: ReturnType<typeof createApp> | null = null
let pinia: ReturnType<typeof createPinia> | null = null

const errorHandler = (err: unknown, _instance: any, _info: string) => {
  if (import.meta.env.DEV) {
    console.error('Vue Error:', err)
  }
}

const warnHandler = (_msg: string, _instance: any, _trace: string) => {
}

const globalErrorHandler = (event: ErrorEvent) => {
  if (import.meta.env.DEV) {
    console.error('Global Error:', event.error)
  }
  event.preventDefault()
}

const unhandledRejectionHandler = (event: PromiseRejectionEvent) => {
  if (import.meta.env.DEV) {
    console.error('Unhandled Promise Rejection:', event.reason)
  }
  event.preventDefault()
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
