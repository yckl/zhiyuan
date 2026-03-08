import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/theme-chalk/dark/css-vars.css' // 必须引入这一行！
import App from './App.vue'
import router from './router'

import './styles/index.scss'

const app = createApp(App)

// 注册所有 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}

// 使用插件
app.use(createPinia())
app.use(router)
app.use(ElementPlus, {
    locale: zhCn,
})

// 全局图片加载错误处理
window.addEventListener('error', (event) => {
    const target = event.target
    if (target instanceof HTMLImageElement && !target.dataset.errorHandled) {
        target.dataset.errorHandled = 'true'
        // 使用 Data URI 以避免网络请求失败
        target.src = 'data:image/svg+xml;charset=UTF-8,%3Csvg width="400" height="300" xmlns="http://www.w3.org/2000/svg"%3E%3Crect width="100%25" height="100%25" fill="%23ccc"/%3E%3Ctext x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" font-family="sans-serif" font-size="20" fill="%23666"%3EImage Load Error%3C/text%3E%3C/svg%3E'
    }
}, true)

app.mount('#app')
