<template>
  <div class="app-root">
    <!-- 鍏ㄥ眬璺敱鍒囨崲楠ㄦ灦钂欏眰 鈥?绾?CSS 杩囨浮锛屼笉浣跨敤 Vue Transition 閬垮厤鍗℃ -->
    <div class="route-skeleton-overlay" :class="{ 'is-visible': showSkeleton }">
      <div class="skeleton-inner">
        <el-skeleton animated :rows="6" />
      </div>
    </div>

    <!-- 璺敱瑙嗗浘 -->
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

// ================== 鍝嶅簲寮?==================
const windowWidth = ref(window.innerWidth)
const isMobile = computed(() => windowWidth.value < 768)
const handleResize = () => { windowWidth.value = window.innerWidth }
onMounted(() => window.addEventListener('resize', handleResize))
onUnmounted(() => window.removeEventListener('resize', handleResize))

// ================== 璺敱杩囨浮鍔ㄧ敾 ==================
const cachedViews = ref<string[]>(['Home', 'ActivityList', 'ExperienceList'])

// 浠庤矾鐢卞畧鍗紶鏉ョ殑 transition 鍚?const currentTransition = computed(() => {
  return (route.meta._transition as string) || 'page-fade'
})

// ================== 楠ㄦ灦灞忛槻鐧介棯锛堢函 CSS锛屼笉鐢?Vue Transition锛?==================
const showSkeleton = ref(false)
let skeletonTimer: ReturnType<typeof setTimeout> | null = null

// 璺敱鍙樺寲鏃堕棯鐜伴鏋?watch(() => route.fullPath, () => {
  showSkeleton.value = true
  if (skeletonTimer) clearTimeout(skeletonTimer)
  skeletonTimer = setTimeout(() => {
    showSkeleton.value = false
  }, 180)
})

// ================== 鐘舵€佹爮棰滆壊閫傞厤 ==================
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

// ================== H5+ 鐗╃悊杩斿洖閿鐞?==================
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
            message: '鍐嶆寜涓€娆￠€€鍑哄簲鐢?,
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
// 鍩虹甯冨眬
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

// ================== 楠ㄦ灦钂欏眰锛堢函 CSS 杩囨浮锛屼笉渚濊禆 Vue Transition锛?==================
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

// ================== PC: 娣″叆涓婃诞 ==================
.page-fade-enter-active {
  transition: opacity 0.28s cubic-bezier(0.4, 0, 0.2, 1),
              transform 0.28s cubic-bezier(0.4, 0, 0.2, 1);
}
.page-fade-leave-active {
  transition: opacity 0.15s cubic-bezier(0.4, 0, 1, 1);
}
.page-fade-enter-from {
  opacity: 0;
  transform: translateY(12px);
}
.page-fade-enter-to {
  opacity: 1;
  transform: translateY(0);
}
.page-fade-leave-from { opacity: 1; }
.page-fade-leave-to { opacity: 0; }

// ================== 鎵嬫満: 宸︽粦杩涘叆 ==================
.slide-left-enter-active,
.slide-left-leave-active {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1),
              opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  will-change: transform, opacity;
  backface-visibility: hidden;
}
.slide-left-enter-from { transform: translateX(24%); opacity: 0; }
.slide-left-enter-to { transform: translateX(0); opacity: 1; }
.slide-left-leave-from { transform: translateX(0); opacity: 1; }
.slide-left-leave-to { transform: translateX(-8%); opacity: 0; }

// ================== 鎵嬫満: 鍙虫粦閫€鍑?==================
.slide-right-enter-active,
.slide-right-leave-active {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1),
              opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  will-change: transform, opacity;
  backface-visibility: hidden;
}
.slide-right-enter-from { transform: translateX(-24%); opacity: 0; }
.slide-right-enter-to { transform: translateX(0); opacity: 1; }
.slide-right-leave-from { transform: translateX(0); opacity: 1; }
.slide-right-leave-to { transform: translateX(8%); opacity: 0; }

// ================== Layout 瀛愯矾鐢辫繃娓?==================
.layout-fade-enter-active {
  transition: opacity 0.25s ease-out, transform 0.25s ease-out;
}
.layout-fade-leave-active {
  transition: opacity 0.12s ease-in;
}
.layout-fade-enter-from { opacity: 0; transform: translateY(8px); }
.layout-fade-enter-to { opacity: 1; transform: translateY(0); }
.layout-fade-leave-from { opacity: 1; }
.layout-fade-leave-to { opacity: 0; }

.layout-slide-enter-active {
  transition: opacity 0.28s ease-out, transform 0.28s ease-out;
}
.layout-slide-leave-active {
  transition: opacity 0.12s ease-in;
}
.layout-slide-enter-from { opacity: 0; transform: translateX(20px); }
.layout-slide-enter-to { opacity: 1; transform: translateX(0); }
.layout-slide-leave-from { opacity: 1; }
.layout-slide-leave-to { opacity: 0; }
</style>
