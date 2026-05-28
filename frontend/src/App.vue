<template>
  <div class="app-root">
    <!-- 全局路由切换骨架蒙层 -->
    <div class="route-skeleton-overlay" :class="{ 'is-visible': showSkeleton }">
      <div class="skeleton-inner">
        <el-skeleton animated :rows="6" />
      </div>
    </div>

    <!-- 路由视图 -->
    <router-view v-slot="{ Component, route }">
      <transition :name="currentTransition">
        <keep-alive :include="cachedViews">
          <component :is="Component" :key="route.fullPath" />
        </keep-alive>
      </transition>
    </router-view>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

// ================== 响应式 ==================
const windowWidth = ref(window.innerWidth)
const isMobile = computed(() => windowWidth.value < 768)
const handleResize = () => { windowWidth.value = window.innerWidth }
onMounted(() => window.addEventListener('resize', handleResize))
onUnmounted(() => window.removeEventListener('resize', handleResize))

// ================== 路由过渡动画 ==================
const cachedViews = ref<string[]>(['Home', 'ActivityList', 'ExperienceList'])

const currentTransition = computed(() => {
  return (route.meta._transition as string) || 'page-fade'
})

// ================== 骨架屏防白闪 ==================
const showSkeleton = ref(false)
let skeletonTimer: ReturnType<typeof setTimeout> | null = null

watch(() => route.path, (newPath) => {
  // 登录和注册页不显示骨架屏，防止白闪
  if (['/login', '/register'].includes(newPath)) {
    showSkeleton.value = false
    return
  }
  
  showSkeleton.value = true
  if (skeletonTimer) clearTimeout(skeletonTimer)
  skeletonTimer = setTimeout(() => {
    showSkeleton.value = false
  }, 180)
})

// ================== 状态栏颜色适配 ==================
const setStatusBarColor = (color: string) => {
  let meta = document.querySelector('meta[name="theme-color"]') as HTMLMetaElement
  if (!meta) {
    meta = document.createElement('meta')
    meta.name = 'theme-color'
    document.head.appendChild(meta)
  }
  meta.content = color

  // @ts-ignore
  if (window.plus) {
    // @ts-ignore
    plus.navigator.setStatusBarBackground(color)
  }
}

onMounted(() => {
  setStatusBarColor('#00BFA6')
})

// ================== H5+ 物理返回键处理 ==================
let lastBackButtonPress = 0

const handleBackButton = () => {
  // @ts-ignore
  if (window.plus) {
    // @ts-ignore
    plus.key.addEventListener('backbutton', () => {
      const currentRoute = router.currentRoute.value.path
      const isHome = ['/', '/home', '/login'].includes(currentRoute)

      if (isHome) {
        const now = Date.now()
        if (now - lastBackButtonPress < 2000) {
          // @ts-ignore
          plus.runtime.quit()
        } else {
          lastBackButtonPress = now
          ElMessage({
            message: '再按一次退出应用',
            type: 'info',
            duration: 2000,
            offset: 50
          })
        }
      } else {
        router.back()
      }
    }, false)
  }
}

onMounted(() => {
  document.addEventListener('plusready', handleBackButton, false)
})

onUnmounted(() => {
  document.removeEventListener('plusready', handleBackButton)
})
</script>

<style lang="scss">
// 基础布局
html, body, #app {
  height: 100%;
  margin: 0;
  padding: 0;
  overflow-x: hidden;
}

.app-root {
  height: 100%;
  position: relative;
}

// ================== 骨架蒙层 ==================
.route-skeleton-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background: rgba(245, 247, 250, 0.85);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 80px;
  pointer-events: none;
  opacity: 0;
  transition: opacity 0.15s ease-in;

  &.is-visible {
    opacity: 1;
    transition: opacity 0.1s ease-out;
  }
}

.skeleton-inner {
  width: 90%;
  max-width: 600px;
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  pointer-events: none;
}

// ================== 路由动画 ==================
.page-fade-enter-active {
  transition: opacity 0.28s cubic-bezier(0.4, 0, 0.2, 1),
              transform 0.28s cubic-bezier(0.4, 0, 0.2, 1);
}
.page-fade-leave-active {
  transition: opacity 0.15s cubic-bezier(0.4, 0, 1, 1);
}
.page-fade-enter-from { opacity: 0; transform: translateY(12px); }
.page-fade-enter-to { opacity: 1; transform: translateY(0); }
.page-fade-leave-to { opacity: 0; }

.slide-left-enter-active, .slide-left-leave-active,
.slide-right-enter-active, .slide-right-leave-active {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1), opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  will-change: transform, opacity;
}

.slide-left-enter-from { transform: translateX(24%); opacity: 0; }
.slide-left-leave-to { transform: translateX(-8%); opacity: 0; }

.slide-right-enter-from { transform: translateX(-24%); opacity: 0; }
.slide-right-leave-to { transform: translateX(8%); opacity: 0; }

.layout-fade-enter-active { transition: opacity 0.25s ease-out, transform 0.25s ease-out; }
.layout-fade-enter-from { opacity: 0; transform: translateY(8px); }

.layout-slide-enter-active { transition: opacity 0.28s ease-out, transform 0.28s ease-out; }
.layout-slide-enter-from { opacity: 0; transform: translateX(20px); }

// ================================================================
// Telegram "Liquid Glass" Global Design Tokens
// ================================================================
:root {
  --radius-lg: 16px;
  --radius-pill: 32px;
  --glass-bg: rgba(255, 255, 255, 0.75);
  --glass-border: rgba(255, 255, 255, 0.4);
  --tg-blue: #2481CC;
  --tg-blue-grad: linear-gradient(135deg, #2481CC, #3498DB);
  --aurora-teal: #00b4b6;
  --aurora-grad: linear-gradient(135deg, #00b4b6, #00d2d4);
}

body {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  line-height: 1.7 !important;
  color: #1c1c1e;
  background: #F8F9FA;
}

/* 全局卡片圆角与阴影 */
.el-card, .card, .glass-card, [class*="-card"], .hour-item-card, .hours-header-section {
  border-radius: var(--radius-lg) !important;
  border: 1px solid rgba(255, 255, 255, 0.6) !important;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04) !important;
  background: rgba(255, 255, 255, 0.8) !important;
  backdrop-filter: blur(12px);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:hover {
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.08) !important;
    transform: translateY(-2px);
  }
}

/* 全局按钮美化 */
.el-button {
  border-radius: 12px !important;
  font-weight: 600 !important;
  letter-spacing: 0.5px;
  transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1) !important;
  
  &.el-button--primary {
    background: var(--aurora-grad) !important;
    border: none !important;
    box-shadow: 0 4px 12px rgba(0, 180, 182, 0.3) !important;
    color: #fff !important;
    
    &:hover {
      transform: scale(1.02) translateY(-1px);
      box-shadow: 0 6px 16px rgba(0, 180, 182, 0.4) !important;
    }
    
    &:active { transform: scale(0.96); }
  }

  &.el-button--default {
    background: rgba(255, 255, 255, 0.6) !important;
    backdrop-filter: blur(8px);
    border: 1px solid rgba(0, 0, 0, 0.05) !important;
    
    &:hover {
      background: #fff !important;
      color: var(--aurora-teal) !important;
      border-color: var(--aurora-teal) !important;
    }
  }
}

/* 统一输入框美化 */
.el-input__wrapper, .el-textarea__inner {
  border-radius: 12px !important;
  background: rgba(255, 255, 255, 0.9) !important;
  box-shadow: 0 0 0 1px #e2e8f0 inset !important;
  transition: all 0.3s !important;
  
  &:hover, &.is-focus {
    box-shadow: 0 0 0 1px var(--aurora-teal) inset !important;
  }
}

/* 隐藏滚动条但保留功能 - 极细设计 */
::-webkit-scrollbar {
  width: 4px;
}
::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 10px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
</style>
