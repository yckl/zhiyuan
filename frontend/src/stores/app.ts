import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 全局应用状态 — 路由切换加载 & 过渡动画
 */
export const useAppStore = defineStore('app', () => {
  /** 全局路由加载态（显示骨架/蒙层） */
  const routeLoading = ref(false)

  /** 当前过渡动画名 */
  const transitionName = ref('page-fade')

  const startLoading = () => { routeLoading.value = true }
  const stopLoading = () => { routeLoading.value = false }

  return { routeLoading, transitionName, startLoading, stopLoading }
})
