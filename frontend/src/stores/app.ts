import { defineStore } from 'pinia'
import { ref, reactive } from 'vue'
import request from '@/utils/request'

/**
 * 全局应用状态 — 路由切换加载 & 过渡动画 & 系统配置
 */
export const useAppStore = defineStore('app', () => {
  /** 全局路由加载态（显示骨架/蒙层） */
  const routeLoading = ref(false)

  /** 当前过渡动画名 */
  const transitionName = ref('page-fade')

  /** 系统全局配置 */
  const systemConfig = reactive({
    site_name: '校园志愿者管理系统',
    site_logo: ''
  })

  const startLoading = () => { routeLoading.value = true }
  const stopLoading = () => { routeLoading.value = false }

  /** 获取系统配置 */
  const fetchSystemConfig = async () => {
    try {
      const res = await request.get('/system/config')
      if (res.data) {
        systemConfig.site_name = res.data.site_name || '校园志愿者管理系统'
        systemConfig.site_logo = res.data.site_logo || ''

        // 同步更新页面 Title
        if (systemConfig.site_name) {
          document.title = systemConfig.site_name
        }
      }
    } catch (error) {
      console.error('Failed to fetch system config:', error)
    }
  }

  return {
    routeLoading,
    transitionName,
    systemConfig,
    startLoading,
    stopLoading,
    fetchSystemConfig
  }
})
