import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
// 导入 Element Plus
import ElementPlus from 'element-plus'
// 导入自定义主题色配置
import './styles/element-variables.scss'
// // 导入磨砂玻璃效果样式
// import './styles/glass-effects.scss'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
// 导入初始化样式
import './assets/global.css'
// 中文
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

// 添加全局错误处理器来抑制ResizeObserver警告
const originalConsoleError = console.error
console.error = (...args) => {
  if (args[0] && args[0].includes && args[0].includes('ResizeObserver loop')) {
    return
  }
  originalConsoleError(...args)
}

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(ElementPlus, {
  locale: zhCn
})
// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
app.mount('#app')
