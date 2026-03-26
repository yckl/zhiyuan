<template>
  <!-- ========== DOM 结构一：C端移动端 (App Shell) ========== -->
  <div v-if="isVolunteer && isMobile" class="mobile-shell">
    <!-- 顶部导航?-->
    <header class="mobile-header" v-if="!route.meta.hideHeader">
      <div class="mobile-header-left">
        <el-icon v-if="canGoBack" class="back-btn" @click="router.back()"><ArrowLeft /></el-icon>
      </div>
      <h1 class="mobile-header-title">{{ route.path === '/home' || route.path === '/' ? appStore.systemConfig.site_name : (route.meta.title || '首页') }}</h1>
      <div class="mobile-header-right">
        <template v-if="isLoggedIn">
          <!-- 消息铃铛 (Frosted Glass Module) -->
          <div class="bell-glass-container" @click="router.push('/notice/my')">
            <el-badge :value="userStore.unreadCount" :hidden="!userStore.unreadCount" :max="99" class="msg-badge-premium">
              <el-icon class="silver-bell"><Bell /></el-icon>
            </el-badge>
          </div>
          

          <!-- 头像 & 菜单 -->
          <el-dropdown 
            @command="handleCommand" 
            trigger="click" 
            popper-class="user-dropdown-popper" 
            placement="bottom-end"
            :teleported="true"
          >
            <el-avatar :size="38" :src="userInfo?.avatar" class="header-avatar header-avatar-with-border">
              <el-icon :size="20"><User /></el-icon>
            </el-avatar>
            <template #dropdown>
              <el-dropdown-menu class="user-dropdown">

                <!-- 2. 个人资料 -->
                <el-dropdown-item command="profile">
                  <div class="menu-item-content">
                    <el-icon><User /></el-icon>
                    <span>我的资料</span>
                  </div>
                </el-dropdown-item>

                <!-- 3. 退出登录 -->
                <el-dropdown-item divided command="logout" class="logout-item">
                  <div class="menu-item-content">
                    <el-icon><SwitchButton /></el-icon>
                    <span>退出登录</span>
                  </div>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <div class="mobile-guest-actions">
            <el-button link type="primary" size="small" class="guest-login-btn" @click="router.push('/login')">登录</el-button>
          </div>
        </template>
      </div>
    </header>

    <!-- 角色专享状态栏 (Phase 15 追加) -->
    <div v-if="isOrganizerRoute || isAdminRoute" class="role-status-banner">
      <div class="banner-content">
        <template v-if="isAdminRoute">
          <el-icon><Management /></el-icon>
          <span>管理员模式 · 系统运行中</span>
        </template>
        <template v-else>
          <span>早上好，{{ userInfo?.nickname || '老师' }} 👋 本月已录入工时统计中</span>
        </template>
      </div>
    </div>

    <!-- 内容区域 -->
    <main class="mobile-main hide-scrollbar" :class="{ 'has-tabbar': !route.meta.hideTabBar }">
      <router-view v-slot="{ Component, route: childRoute }">
        <transition :name="transitionName" mode="out-in">
          <component :is="Component" :key="childRoute.fullPath" />
        </transition>
      </router-view>
    </main>

    <!-- 底部 TabBar (Telegram Floating Liquid Glass 风格) -->
    <nav v-if="!route.meta.hideTabBar" class="mobile-floating-nav">
      <div class="nav-content">
        <!-- 滑动胶囊背景 -->
        <div class="slider-wrapper volunteer" :style="volunteerSliderStyle">
          <div class="visual-pill"></div>
        </div>
        <div
          v-for="tab in mobileTabItems"
          :key="tab.path"
          class="nav-item"
          :class="{ active: isTabActive(tab.path) }"
          @click="router.push(tab.path)"
        >
          <div class="icon-box">
            <el-icon class="nav-icon"><component :is="tab.icon" /></el-icon>
            <!-- 消息提醒红点 -->
            <div v-if="tab.path === '/notice/my' && userStore.unreadCount > 0" class="nav-badge"></div>
          </div>
          <span class="nav-label">{{ tab.label }}</span>
        </div>
      </div>
    </nav>
  </div>

  <!-- ========== DOM 结构二：C端电脑端 ========== -->
  <div v-else-if="isVolunteer && !isMobile" class="pc-shell">
    <!-- 顶部 Sticky Header -->
    <header class="pc-header">
      <div class="pc-header-inner">
        <!-- Logo -->
        <div class="pc-logo" @click="router.push('/home')">
          <div class="logo-icon-box-white">
            <template v-if="appStore.systemConfig.site_logo">
              <img :src="appStore.systemConfig.site_logo" class="dynamic-logo-img" alt="logo" />
            </template>
            <el-icon v-else :size="22"><Aim /></el-icon>
          </div>
          <span class="logo-text-white">{{ appStore.systemConfig.site_name }}</span>
        </div>

        <!-- 横向导航 -->
        <el-menu
          :default-active="activeMenu"
          mode="horizontal"
          :router="true"
          class="pc-nav-menu"
          :ellipsis="false"
        >
          <el-menu-item index="/home">首页</el-menu-item>
          <el-menu-item index="/activity">活动中心</el-menu-item>
          <el-menu-item index="/training/index">培训学院</el-menu-item>
          <el-menu-item index="/mall/index">积分商城</el-menu-item>
          <el-menu-item index="/experience">心得分享</el-menu-item>
        </el-menu>

        <!-- 右侧操作栏?-->
        <div class="pc-header-actions">
          <template v-if="isLoggedIn">
            <!-- 消息铃铛 (Premium Frosted Glass) -->
            <div class="bell-glass-container" @click="router.push('/notice/my')">
              <el-badge :value="userStore.unreadCount" :hidden="!userStore.unreadCount" :max="99" class="msg-badge-premium">
                <el-icon class="silver-bell"><Bell /></el-icon>
              </el-badge>
            </div>

            <el-dropdown 
              @command="handleCommand" 
              trigger="click" 
              popper-class="user-dropdown-popper" 
              placement="bottom-end"
              :teleported="true"
            >
              <div class="user-avatar-trigger">
                <el-avatar :size="34" :src="userInfo?.avatar" class="header-avatar-with-border">
                  <el-icon><User /></el-icon>
                </el-avatar>
                <div class="user-id-wrapper">
                  <span class="user-id">{{ userInfo?.username || '2024000001' }}</span>
                  <el-icon class="arrow-icon"><ArrowDown /></el-icon>
                </div>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="user-dropdown">

                  <el-dropdown-item command="profile">
                    <div class="menu-item-content">
                      <el-icon><User /></el-icon>
                      <span>个人中心</span>
                    </div>
                  </el-dropdown-item>
                  <el-dropdown-item command="myActivities">
                    <div class="menu-item-content">
                      <el-icon><Calendar /></el-icon>
                      <span>我的活动</span>
                    </div>
                  </el-dropdown-item>
                  <el-dropdown-item command="backpack">
                    <div class="menu-item-content">
                      <el-icon><Box /></el-icon>
                      <span>我的背包</span>
                    </div>
                  </el-dropdown-item>
                  <el-dropdown-item command="honor">
                    <div class="menu-item-content">
                      <el-icon><Trophy /></el-icon>
                      <span>荣誉证书</span>
                    </div>
                  </el-dropdown-item>
                  <el-dropdown-item command="feedback">
                    <div class="menu-item-content">
                      <el-icon><ChatDotRound /></el-icon>
                      <span>问题反馈</span>
                    </div>
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout" class="logout-item">
                    <div class="menu-item-content">
                      <el-icon><SwitchButton /></el-icon>
                      <span>退出登录</span>
                    </div>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" round size="small" @click="router.push('/login')">登录</el-button>
            <el-button round size="small" @click="router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </header>

    <!-- 内容区域 -->
    <main class="pc-main">
      <div class="pc-content-wrapper">
        <router-view v-slot="{ Component, route: childRoute }">
          <transition :name="transitionName" mode="out-in">
            <component :is="Component" :key="childRoute.fullPath" />
          </transition>
        </router-view>
      </div>
    </main>

    <!-- 底部 Footer -->
    <footer class="pc-footer">
      <span>© 2026 {{ appStore.systemConfig.site_name }}</span>
      <span class="dot">·</span>
      <span>用科技传递温</span>
    </footer>
  </div>

  <!-- ========== DOM 结构三：B端(组织者/管理员) ========== -->
  <el-container v-else class="admin-shell" :class="{ 'is-collapsed': isCollapsed }">

    <!-- ===== 管理员手机端：顶部极简?===== -->
    <div v-if="isMobile && (isAdminRoute || isOrganizerRoute)" class="admin-mobile-header">
      <div v-if="!isOrganizerRoute" class="menu-trigger" @click="showAdminDrawer = true">
        <div class="hamburger-lines">
          <span></span>
          <span></span>
          <span></span>
        </div>
      </div>
      <span class="admin-title">{{ appStore.systemConfig.site_name }}</span>
      <div class="header-right-action">



        <el-dropdown 
          @command="handleCommand" 
          trigger="click" 
          popper-class="user-dropdown-popper" 
          placement="bottom-end"
          :teleported="true"
        >
          <el-avatar :size="30" :src="userInfo?.avatar" class="header-avatar">
            <el-icon><User /></el-icon>
          </el-avatar>
          <template #dropdown>
            <el-dropdown-menu class="user-dropdown">
              <el-dropdown-item command="profile">
                <div class="menu-item-content">
                  <el-icon><User /></el-icon>
                  <span>个人资料</span>
                </div>
              </el-dropdown-item>
              <el-dropdown-item divided command="logout" class="logout-item">
                <div class="menu-item-content">
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出系统</span>
                </div>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- ===== 管理员手机端：侧滑抽屉菜?===== -->
    <el-drawer
      v-model="showAdminDrawer"
      direction="ltr"
      size="78%"
      :with-header="false"
      :close-on-click-modal="true"
      :show-close="false"
      :append-to-body="false"
      :destroy-on-close="true"
      class="admin-menu-drawer"
    >
      <div class="drawer-inner" @click="showAdminDrawer = false">
        <!-- 抽屉顶部用户信息?- 点击此处阻止关闭 (stop) -->
        <div class="drawer-profile" @click.stop>
          <div class="drawer-profile-bg"></div>
          <div class="drawer-profile-content">
            <el-avatar :size="48" :src="userInfo?.avatar" class="drawer-avatar">
              <el-icon :size="24"><User /></el-icon>
            </el-avatar>
            <div class="drawer-user-info">
              <span class="drawer-username">{{ userInfo?.nickname || userInfo?.username || '管理员' }}</span>
              <span class="drawer-role-badge">系统管理员</span>
            </div>
          </div>
        </div>

        <div class="drawer-menu-wrapper">
          <el-menu
            :default-active="activeMenu"
            :router="true"
            class="drawer-menu"
            @select="showAdminDrawer = false"
            @click.stop
          >
            <template v-for="menu in currentMenus" :key="menu.path">
              <el-sub-menu v-if="menu.children" :index="menu.path">
                <template #title>
                  <el-icon><component :is="menu.icon" /></el-icon>
                  <span>{{ menu.name }}</span>
                </template>
                <el-menu-item v-for="child in menu.children" :key="child.path" :index="child.path">
                  {{ child.name }}
                </el-menu-item>
              </el-sub-menu>
              <el-menu-item v-else :index="menu.path">
                <el-icon><component :is="menu.icon" /></el-icon>
                <template #title>{{ menu.name }}</template>
              </el-menu-item>
            </template>
          </el-menu>
          
          <!-- 底部空白占位符：点击关闭抽屉 -->
          <div class="drawer-bottom-spacer" style="flex: 1; min-height: 100px;" @click="showAdminDrawer = false"></div>
        </div>
      </div>
    </el-drawer>

    <!-- 侧边?(桌面?or 组织者手机端抽屉? -->
    <el-aside v-show="!isMobile" :width="sidebarWidth" class="admin-aside">
      <div class="aside-inner">
        <!-- Logo -->
        <div class="admin-logo" @click="handleLogoClick">
          <div class="logo-icon-box">
            <template v-if="appStore.systemConfig.site_logo">
              <img :src="appStore.systemConfig.site_logo" class="dynamic-logo-img" alt="logo" />
            </template>
            <el-icon v-else :size="24"><Aim /></el-icon>
          </div>
          <span v-show="!isCollapsed" class="logo-text">{{ appStore.systemConfig.site_name }}</span>
        </div>

        <!-- 菜单 -->
        <div class="menu-relative-wrapper">
          <div class="sidebar-pill-indicator" :style="sidebarIndicatorStyle"></div>
          <el-menu
            :default-active="activeMenu"
            :collapse="isCollapsed"
            :router="true"
            :collapse-transition="false"
            class="admin-menu"
          >
          <template v-for="menu in currentMenus" :key="menu.path">
            <el-sub-menu v-if="menu.children" :index="menu.path">
              <template #title>
                <el-icon><component :is="menu.icon" /></el-icon>
                <span>{{ menu.name }}</span>
              </template>
              <el-menu-item v-for="child in menu.children" :key="child.path" :index="child.path">
                {{ child.name }}
              </el-menu-item>
            </el-sub-menu>
            <el-menu-item v-else :index="menu.path">
              <el-icon><component :is="menu.icon" /></el-icon>
              <template #title>{{ menu.name }}</template>
            </el-menu-item>
          </template>
          </el-menu>
        </div>
      </div>
    </el-aside>

    <!-- 右侧主区?-->
    <el-container class="admin-main-container" :class="{ 'is-mobile': isMobile }">
      <!-- 顶栏 (桌面端显?/ 组织者手机端也隐? -->
      <el-header v-show="!isMobile" class="admin-header" height="56px">
        <div class="admin-header-left">
          <el-icon class="collapse-toggle" @click="isCollapsed = !isCollapsed">
            <component :is="isCollapsed ? Expand : Fold" />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="admin-header-right">
          <div class="role-tag hidden-sm-and-down">
            <el-icon><component :is="roleInfo.icon" /></el-icon>
            <span>{{ roleInfo.label }}</span>
          </div>


          <el-dropdown 
            @command="handleCommand" 
            trigger="click" 
            popper-class="user-dropdown-popper" 
            placement="bottom-end"
            :teleported="true"
          >
            <div class="user-avatar-trigger">
              <el-avatar :size="32" :src="userInfo?.avatar" class="header-avatar-with-border">
                <el-icon><User /></el-icon>
              </el-avatar>
              <span class="user-nickname hidden-sm-and-down">{{ userInfo?.nickname || userInfo?.username || '用户' }}</span>
              <el-icon class="arrow-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="user-dropdown">
                <el-dropdown-item command="profile">
                  <div class="menu-item-content">
                    <el-icon><User /></el-icon>
                    <span>个人中心</span>
                  </div>
                </el-dropdown-item>
                <el-dropdown-item divided command="logout" class="logout-item">
                  <div class="menu-item-content">
                    <el-icon><SwitchButton /></el-icon>
                    <span>退出系统</span>
                  </div>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容 -->
      <el-main class="admin-main" :class="{ 'admin-mobile-padding': isMobile && (isAdminRoute || isOrganizerRoute) }">
        <router-view v-slot="{ Component, route: childRoute }">
          <transition :name="transitionName" mode="out-in">
            <component :is="Component" :key="childRoute.fullPath" />
          </transition>
        </router-view>
      </el-main>

      <!-- 移动端 B端底部 TabBar (Telegram Floating Liquid Glass - 核心复刻) -->
      <nav v-if="isMobile && isOrganizerRoute" class="mobile-floating-nav admin-floating" :class="{ 'nav-hidden': !showTabBar }">
        <div class="nav-content">
          <div class="slider-wrapper" :style="sliderStyle">
            <div class="visual-pill"></div>
          </div>
          <div 
            v-for="(tab, index) in tgTabs" 
            :key="tab.path"
            class="nav-item" 
            :class="{ active: activeIndex === index }"
            @click="router.push(tab.path)"
          >
            <div class="icon-box">
              <el-avatar v-if="tab.isProfile" :size="26" :src="userInfo?.avatar" class="nav-avatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <el-icon v-else class="nav-icon"><component :is="tab.icon" /></el-icon>
            </div>
            <span class="nav-label">{{ tab.label }}</span>
          </div>
        </div>
      </nav>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Aim, Calendar, User, ArrowDown, SwitchButton, Bell,
  Fold, Expand, ArrowLeft, Box, Trophy, ChatDotRound,
  Star, Connection, Management, UserFilled, CircleCheck
} from '@element-plus/icons-vue'
import { adminMenus } from '@/config/adminMenus'
import { orgMenus } from '@/config/orgMenus'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { useMobile } from '@/composables/useMobile'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()

// --- 静态导航配置 (提前初始化以供 watch 使用) ---
const mobileTabItems = [
  { path: '/home', label: '工作台', icon: Aim },
  { path: '/activity', label: '活动', icon: Calendar },
  { path: '/scan', label: '签到', icon: CircleCheck },
  { path: '/notice/my', label: '通知', icon: Bell },
  { path: '/profile', label: '我的', icon: UserFilled }
]

const tgTabs = [
  { label: '工作台', path: '/organizer/dashboard', icon: 'Odometer' },
  { label: '活动', path: '/organizer/activity/list', icon: 'List' },
  { label: '签到', path: '/organizer/personnel/checkin', icon: 'FullScreen' },
  { label: '通知', path: '/organizer/notification/history', icon: 'Bell' },
  { label: '我的', path: '/organizer/setting', icon: 'User', isProfile: true }
]

// ================== Telegram 2026 同款丝滑切?==================
const transitionName = ref('page-push')

// 深度计算：通过路径中的 / 数量判断 (简易实现)
const getDepth = (path: string) => {
  if (path === '/' || path === '/home') return 0
  return path.split('/').filter(Boolean).length
}

watch(() => route.path, (to, from) => {
  const toDepth = getDepth(to)
  const fromDepth = getDepth(from || '/')
  
  // 1. Tab 切换判断
  const isToTab = mobileTabItems.some(tab => to === tab.path)
  const isFromTab = mobileTabItems.some(tab => from === tab.path)
  
  if (isToTab && isFromTab) {
    transitionName.value = 'tab-fade'
  } else if (toDepth > fromDepth) {
    // 2. 进页面 (Push)
    transitionName.value = 'page-push'
  } else {
    // 3. 退页面 (Pop)
    transitionName.value = 'page-pop'
  }
}, { immediate: true })

// 初始化主题
onMounted(() => {
  // 确保初始加载时清除可能残留的 dark class
  document.documentElement.classList.remove('dark')
})

// ================== 响应式?(统一 992px 断点) ==================
const { isMobile } = useMobile()
const isCollapsed = ref(false)

// ================== 路由分流：管理员 vs 组织者?==================
const isAdminRoute = computed(() => route.path.startsWith('/admin'))
const isOrganizerRoute = computed(() => route.path.startsWith('/organizer'))

// 管理员手机端抽屉菜单控制
const showAdminDrawer = ref(false)

// ================== 底部导航栏智慧显?==================
const showTabBar = ref(true)
let lastScrollTop = 0

const handleScroll = () => {
  const currentScroll = window.pageYOffset || document.documentElement.scrollTop
  // 缓冲阈值：防止轻微抖动导致闪烁
  if (Math.abs(currentScroll - lastScrollTop) < 10) return

  if (currentScroll > lastScrollTop && currentScroll > 60) {
    showTabBar.value = false // 向下滚动 -> 隐藏
  } else {
    showTabBar.value = true // 向上滚动 -> 显示
  }
  lastScrollTop = currentScroll <= 0 ? 0 : currentScroll
}

// ================== 用户角色 ==================
const userInfo = computed(() => userStore.userInfo)
const isLoggedIn = computed(() => userStore.isLoggedIn)
const isVolunteer = computed(() => !userStore.role || userStore.role === 'VOLUNTEER')
const isOrganizer = computed(() => userStore.role === 'ORGANIZER')
const isAdmin = computed(() => userStore.role === 'ADMIN')

const roleMap: Record<string, { label: string; icon: any }> = {
  ADMIN: { label: '系统管理员', icon: Management },
  ORGANIZER: { label: '项目负责人', icon: Connection },
  VOLUNTEER: { label: '注册志愿者', icon: Star }
}
const roleInfo = computed(() => roleMap[userStore.role] || { label: '普通用户', icon: User })

// B端菜?
const currentMenus = computed(() => isAdmin.value ? adminMenus : orgMenus)

// ================== PC Sidebar 垂直流动胶囊核心逻辑 ==================
const sidebarIndicatorStyle = ref({ top: '0px', height: '0px', opacity: 0 })
const activeMenu = computed(() => route.path)

const updateSidebarIndicator = () => {
  if (isMobile.value) return
  setTimeout(() => {
    const activeItem = document.querySelector('.admin-menu .el-menu-item.is-active') || 
                       document.querySelector('.admin-menu .el-sub-menu.is-active > .el-sub-menu__title')
    if (activeItem && document.querySelector('.admin-menu')) {
      const rect = activeItem.getBoundingClientRect()
      const parentRect = document.querySelector('.admin-menu')!.getBoundingClientRect()
      sidebarIndicatorStyle.value = {
        top: `${rect.top - parentRect.top + 5}px`, // 居中微调
        height: `${rect.height - 10}px`,
        opacity: 1
      }
    } else {
      sidebarIndicatorStyle.value.opacity = 0
    }
  }, 50)
}

watch([() => route.path, isCollapsed], () => {
  updateSidebarIndicator()
})

// 路由状态辅?
const canGoBack = computed(() => {
  const homePaths = ['/home', '/', '/activity', '/profile', '/scan']
  return !homePaths.includes(route.path)
})

const sidebarWidth = computed(() => isCollapsed.value ? '72px' : '240px')

const isTabActive = (path: string) => {
  if (path === '/home') return route.path === '/home' || route.path === '/'
  return route.path.startsWith(path)
}

// ================== TG 同款流动胶囊导航栏核心逻辑 ==================

const activeIndex = computed(() => {
  return tgTabs.findIndex(t => route.path.includes(t.path))
})

const volunteerActiveIndex = computed(() => {
  return mobileTabItems.findIndex(tab => isTabActive(tab.path))
})

const volunteerSliderStyle = computed(() => {
  if (volunteerActiveIndex.value === -1) return { opacity: 0 }
  return {
    transform: `translateX(${volunteerActiveIndex.value * 100}%)`,
    opacity: 1
  }
})

const sliderStyle = computed(() => {
  if (activeIndex.value === -1) return { opacity: 0 }
  return {
    transform: `translateX(${activeIndex.value * 100}%)`,
    opacity: 1
  }
})

// ================== 事件处理 ==================
const handleLogoClick = () => {
  if (isAdmin.value) router.push('/admin/dashboard/overview')
  else if (isOrganizer.value) router.push('/organizer/dashboard')
  else router.push('/home')
}

const handleCommand = (command: string) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出系统吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      roundButton: true,
      confirmButtonClass: 'danger-text-btn'
    }).then(() => {
      userStore.clearUser()
      ElMessage.success('已安全退出')
      router.push('/login')
    }).catch(() => {})
  } else if (command === 'profile') {
    // 🚦 身份分流：不同角色去不同的个人页
    if (isAdminRoute.value) {
      router.push('/admin/profile')
    } else if (isOrganizerRoute.value) {
      router.push('/organizer/setting')
    } else {
      router.push('/profile/info')
    }
  } else {
    const commandMap: Record<string, string> = {
      myActivities: '/my-activities',
      backpack: '/mall/backpack',
      honor: '/profile/honor',
      feedback: '/feedback'
    }
    if (commandMap[command]) {
      router.push(commandMap[command])
    }
  }
}


// 获取未读消息
onMounted(() => {
  // 仅在真实登录且为志愿者账号时尝试获取未读消息数
  const token = localStorage.getItem('token')
  if (token && isVolunteer.value) {
    userStore.fetchUnreadCount()
  }
  appStore.fetchSystemConfig() // 获取系统全局配置
  window.addEventListener('scroll', handleScroll, { passive: true })
  updateSidebarIndicator() // 初始化侧边栏指示
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

// 路由变化时关闭可能的 overlay
watch(() => route.path, () => {
  if (isMobile.value) {
    isCollapsed.value = true
    showAdminDrawer.value = false // 路由切换时关闭管理员抽屉
  }
})
</script>

<style lang="scss" scoped>
// ================================================================
// DOM 结构一：C?移动端?App Shell
// ================================================================
.mobile-shell {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #F8F9FA; // 统一浅灰背景
  overflow: hidden;
}

/* 全级头像边框增强 */
.header-avatar-with-border {
  border: 1.5px solid #00C9A7 !important;
  transition: all 0.3s ease;
  &:hover {
    border-color: #00B38F !important;
    box-shadow: 0 0 8px rgba(0, 201, 167, 0.2);
  }
}

.action-icon {
  color: #475569 !important; /* 统一深灰/蓝色调 */
  &.is-active {
    color: var(--primary-color) !important;
  }
}

.icon-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.theme-toggle-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: var(--tg-text-color);
  
  &:hover {
    background: var(--bg-hover);
  }
}

.mobile-header {
  height: var(--header-height, 56px);
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 0;
  z-index: 1000;
  color: #fff;
  background: linear-gradient(135deg, #00C9A7 0%, #05D5B3 100%);
  box-shadow: 0 4px 20px rgba(0, 201, 167, 0.15);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: none;
  overflow: visible !important;
  
  .mobile-header-left {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-shrink: 0;
  }

  .mobile-header-title {
    flex: 1;
    margin-left: 8px;
    font-size: 19px;
    font-weight: 800;
    color: #fff;
    letter-spacing: -0.5px;
  }

  .mobile-header-right {
    display: flex;
    align-items: center;
    gap: 14px;
    flex-shrink: 0;
  }
}

/* 图标变形过度 */
.icon-morph-enter-active,
.icon-morph-leave-active {
  transition: all 0.45s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.icon-morph-enter-from {
  opacity: 0;
  transform: scale(0.4) rotate(-180deg);
}
.icon-morph-leave-to {
  opacity: 0;
  transform: scale(0.4) rotate(180deg);
}

.theme-toggle-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  cursor: pointer;
  transition: background 0.2s;
  color: var(--tg-text-color);
  
  &:hover {
    background: var(--bg-hover);
  }
  
  .el-icon {
    font-size: 20px;
    transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  }
  
  &:active .el-icon {
    transform: scale(0.7) rotate(25deg);
  }
}

.tg-icon-spin .el-icon {
  animation: tg-icon-pop 0.45s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes tg-icon-pop {
  0% { transform: scale(0.5) rotate(-30deg); opacity: 0; }
  100% { transform: scale(1) rotate(0); opacity: 1; }
}

.header-avatar {
  cursor: pointer;
  transition: transform 0.2s;
  &:active { transform: scale(0.9); }
}

// --- 移动端内容区 ---
.mobile-main {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  -webkit-overflow-scrolling: touch;
  padding: 0; // 强制 0 padding，让首页轮播等内容通栏贴边
  
  &.has-tabbar {
    padding-bottom: 100px; // 为底部浮动导航留出空间
  }
}

// --- 移动端底?TabBar ---
.mobile-tabbar {
  height: var(--tabbar-height);
  min-height: var(--tabbar-height);
  padding-bottom: var(--safe-area-bottom);
  display: flex;
  align-items: center;
  justify-content: space-around;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: saturate(180%) blur(20px);
  -webkit-backdrop-filter: saturate(180%) blur(20px);
  border-top: 0.5px solid rgba(0, 0, 0, 0.06);
  z-index: 100;

  /* PC 端隐藏 */
  @media screen and (min-width: 769px) {
    display: none !important;
  }
}

// ================================================================
// Telegram Floating Liquid Glass 核心样式
// ================================================================
.mobile-floating-nav {
  position: fixed;
  bottom: 0px; // 占位高度由 padding-bottom 处理
  left: 0;
  width: 100%;
  padding: 0 16px 14px; // 离屏幕底部留出 14px 间隙
  z-index: 2000;
  transition: transform 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  pointer-events: none; // 容器穿透，仅内容区响应

  &.nav-hidden {
    transform: translateY(120%);
  }

  .nav-content {
    pointer-events: auto;
    width: 100%;
    height: 68px;
    background: rgba(255, 255, 255, 0.75);
    backdrop-filter: blur(25px) saturate(180%);
    -webkit-backdrop-filter: blur(25px) saturate(180%);
    border-radius: 34px; // 药丸形卡片
    display: flex;
    align-items: center;
    justify-content: space-around;
    position: relative;
    border: 1px solid rgba(255, 255, 255, 0.4);
    box-shadow: 0 12px 36px rgba(0, 0, 0, 0.1), 0 2px 8px rgba(0, 0, 0, 0.05);
  }

  .nav-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 2px;
    cursor: pointer;
    color: #8e8e93;
    transition: all 0.3s;
    -webkit-tap-highlight-color: transparent;

    &.active {
      color: #2481CC; // Telegram 清爽蓝

      .nav-icon {
        transform: scale(1.15);
      }
      .nav-label {
        font-weight: 700;
        transform: scale(1.05);
      }
    }

    &:active .icon-box {
      transform: scale(0.9);
    }
  }

  .icon-box {
    width: 48px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1;
    transition: all 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
    position: relative;
  }

  .nav-icon {
    font-size: 26px; // 柔和线条风格
    transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  }

  .nav-label {
    font-size: 11px;
    font-weight: 500;
    line-height: 1;
    transition: all 0.2s;
  }

  .nav-badge {
    position: absolute;
    top: 2px;
    right: 8px;
    width: 8px;
    height: 8px;
    background: #ff3b30;
    border: 2px solid #fff;
    border-radius: 50%;
    animation: badge-shake 2s infinite;
  }

  /* 胶囊背景 */
  .slider-wrapper {
    position: absolute;
    top: 10px; left: 0;
    width: 20%; // 5个Tab -> 100/5
    height: 48px;
    z-index: 0;
    pointer-events: none;
    display: flex;
    justify-content: center;
    transition: transform 0.4s cubic-bezier(0.2, 0.8, 0.2, 1);
    
    .visual-pill {
      width: 56px;
      height: 48px;
      background: rgba(36, 129, 204, 0.1);
      border-radius: 18px;
    }
  }

  &.admin-floating .slider-wrapper {
    width: 20%; // B端默认5个Tab -> 100/5
  }

  .nav-avatar {
    border: 2px solid transparent;
    transition: all 0.3s;
  }
  .nav-item.active .nav-avatar {
    border-color: #2481CC;
    box-shadow: 0 0 10px rgba(36, 129, 204, 0.3);
  }
}

/* --- Push: 进页面 --- */
.page-push-enter-active, .page-push-leave-active {
  transition: all 280ms cubic-bezier(0.32, 0.72, 0, 1);
}
.page-push-enter-from {
  opacity: 0;
  transform: translateX(100%) scale(0.95);
}
.page-push-leave-to {
  transform: translateX(-12px);
  filter: blur(8px);
  opacity: 0.8;
}

/* --- Pop: 退页面 --- */
.page-pop-enter-active, .page-pop-leave-active {
  transition: all 280ms cubic-bezier(0.32, 0.72, 0, 1);
}
.page-pop-enter-from {
  transform: translateX(-12px);
  opacity: 0.8;
  filter: blur(8px);
}
.page-pop-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

/* --- Tab: TabBar 切换 --- */
.tab-fade-enter-active {
  transition: opacity 180ms ease-out;
}
.tab-fade-leave-active {
  transition: opacity 80ms ease-in;
}
.tab-fade-enter-from, .tab-fade-leave-to {
  opacity: 0;
}

// ================================================================
// 角色状态栏样式
// ================================================================
.role-status-banner {
  height: 32px;
  background: rgba(36, 129, 204, 0.05);
  display: flex;
  align-items: center;
  padding: 0 16px;
  font-size: 12px;
  color: #2481CC;
  border-bottom: 0.5px solid rgba(36, 129, 204, 0.1);
  backdrop-filter: blur(10px);
  
  .banner-content {
    display: flex;
    align-items: center;
    gap: 6px;
    font-weight: 500;
  }
}

// ================================================================
// 全局微动效与交互细节
// ================================================================
:global(.el-card), :global(.glass-card) {
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1), box-shadow 0.3s ease !important;
  &:hover {
    transform: translateY(-4px) !important;
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.08) !important;
  }
}

:global(.el-button) {
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1) !important;
  &:active {
    transform: scale(0.95) !important;
  }
}

@keyframes badge-shake {
  0%, 90% { transform: scale(1); }
  92% { transform: scale(1.2) rotate(5deg); }
  94% { transform: scale(0.9) rotate(-5deg); }
  96% { transform: scale(1.1) rotate(5deg); }
  100% { transform: scale(1); }
}


// ================================================================
// DOM 结构二：C?电脑?
// ================================================================
.pc-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #F8F9FA; // 统一浅灰背景
}

// --- PC 顶部 Header ---
.pc-header {
  height: 64px;
  background: linear-gradient(135deg, #00C9A7 0%, #00B38F 100%);
  color: #fff;
  position: sticky;
  top: 0;
  z-index: 1000;
  backdrop-filter: blur(20px);
  box-shadow: 0 4px 30px rgba(0, 201, 167, 0.18);
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);

  .pc-header-inner {
    max-width: 1440px;
    height: 100%;
    margin: 0 auto;
    padding: 0 24px;
    display: flex;
    align-items: center;
    justify-content: space-between;

    @media (max-width: 1600px) { max-width: 95%; }
  }

  .pc-logo {
    display: flex;
    align-items: center;
    gap: 12px;
    cursor: pointer;
    flex-shrink: 0;
  }

  .pc-nav-menu {
    flex: 1;
    background: transparent !important;
    border: none !important;
    margin: 0 40px;
    
    :deep(.el-menu-item) {
      color: rgba(255, 255, 255, 0.85) !important;
      font-weight: 600;
      height: 64px;
      border: none !important;
      font-size: 15px;
      transition: all 0.3s;
      
      &:hover, &.is-active {
        color: #fff !important;
        background: rgba(255, 255, 255, 0.1) !important;
      }
    }
  }

  .pc-header-actions {
    display: flex;
    align-items: center;
    gap: 16px;
  }
}

/* 消息铃铛毛玻璃容器 */
.bell-glass-container {
  width: 38px;
  height: 38px;
  background: rgba(255, 255, 255, 0.22);
  backdrop-filter: blur(24px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05), inset 0 0 10px rgba(255, 255, 255, 0.2);
  
  .el-badge {
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &:hover {
    transform: scale(1.1) rotate(5deg);
    background: rgba(255, 255, 255, 0.32);
    box-shadow: 0 8px 25px rgba(0, 201, 167, 0.3), inset 0 0 15px rgba(255, 255, 255, 0.3);
  }

  &:active { transform: scale(0.92); }
}

/* 银色实心铃铛渲染 */
.silver-bell {
  font-size: 20px;
  color: #f8fafc !important;
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.2));
  background: linear-gradient(135deg, #ffffff 0%, #cbd5e1 100%);
  -webkit-background-clip: text;
  background-clip: text;
  filter: brightness(1.1) contrast(1.1) drop-shadow(0 0 1px rgba(255,255,255,0.8));
}

/* 醒目红橙渐变角标 */
.msg-badge-premium {
  :deep(.el-badge__content) {
    background: linear-gradient(135deg, #ff4d4f 0%, #e53935 100%) !important;
    border: 1.5px solid #fff !important;
    box-shadow: 0 4px 10px rgba(229, 57, 53, 0.45) !important;
    font-weight: 800;
    height: 18px;
    padding: 0 5px;
    font-size: 11px;
    transform: translate(12px, -12px);
  }
}

.user-avatar-trigger {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 30px;
  transition: all 0.3s;

  &:hover {
    background: rgba(255, 255, 255, 0.25);
    border-color: rgba(255, 255, 255, 0.4);
    transform: translateY(-1px);
  }

  .user-id-wrapper {
    display: flex;
    align-items: center;
    gap: 4px;
    
    .user-id {
      font-size: 14px;
      font-weight: 700;
      color: #fff;
      font-family: 'JetBrains Mono', 'Monospace', sans-serif;
      letter-spacing: 0.5px;
    }
    
    .arrow-icon {
      font-size: 12px;
      color: rgba(255, 255, 255, 0.7);
    }
  }
}

// --- PC 内容区?---
.pc-main {
  flex: 1;
  padding: 24px;

  .pc-content-wrapper {
    max-width: 1440px;
    margin: 0 auto;

    // 响应式宽度适配
    @media (min-width: 1200px) and (max-width: 1600px) {
      max-width: 90%;
    }
    @media (min-width: 1601px) {
      max-width: 1600px;
    }
  }
}

// --- PC 底部 Footer ---
.pc-footer {
  text-align: center;
  padding: 20px;
  color: #999;
  font-size: 13px;

  .dot {
    margin: 0 8px;
    opacity: 0.4;
  }
}


// ================================================================
// DOM 结构三：B?(组织者?管理员?
// ================================================================
.admin-shell {
  height: 100vh;
}

// --- 侧边?---
.admin-aside {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(25px);
  -webkit-backdrop-filter: blur(25px);
  border-right: 1px solid rgba(0, 0, 0, 0.05);
  box-shadow: 4px 0 15px rgba(0, 0, 0, 0.02);
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  z-index: 100;

  .aside-inner {
    height: 100%;
    display: flex;
    flex-direction: column;
    overflow-y: auto;
    overflow-x: hidden;
  }
}

.admin-logo {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 12px;
  cursor: pointer;
  flex-shrink: 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.03);

  .logo-icon-box {
    width: 34px;
    height: 34px;
    background: linear-gradient(135deg, #00C9A7, #00B38F);
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    flex-shrink: 0;
  }

  .logo-text {
    font-size: 16px;
    font-weight: 700;
    color: var(--text-primary);
    white-space: nowrap;
  }
}

// --- B端菜?---
.admin-menu {
  border-right: none !important;
  flex: 1;
  padding: 8px;

  :deep(.el-menu-item),
  :deep(.el-sub-menu__title) {
    height: 44px;
    line-height: 44px;
    border-radius: var(--radius-sm);
    margin-bottom: 2px;
    color: #333;
    transition: all 0.2s;

    &:hover {
      background: rgba(0, 191, 166, 0.06) !important;
      color: var(--primary-color) !important;
    }
  }

  :deep(.el-menu-item.is-active) {
    background: transparent !important;
    color: var(--primary-color) !important;
    font-weight: 700;
  }

  :deep(.el-menu--inline) {
    .el-menu-item {
      padding-left: 52px !important;
      height: 40px;
      line-height: 40px;
      font-size: 13px;
    }
  }
}

// --- B端顶?---
.admin-main-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #F8F9FA; // 统一浅灰背景
}

.menu-relative-wrapper {
  position: relative;
  flex: 1;
}

.sidebar-pill-indicator {
  position: absolute;
  left: 0;
  width: 4px;
  background: linear-gradient(180deg, #00C9A7 0%, #00B38F 100%);
  border-radius: 0 4px 4px 0;
  z-index: 10;
  pointer-events: none;
  transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
  box-shadow: 0 0 12px rgba(0, 147, 233, 0.4);
}

.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: #fff;
  border-bottom: none;
  overflow: visible !important;
  z-index: 200;
  box-shadow: 0 1px 0 rgba(0, 0, 0, 0.04);

  .admin-header-left {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .admin-header-right {
    display: flex;
    align-items: center;
    gap: 20px;
  }

  .collapse-toggle {
    font-size: 20px;
    cursor: pointer;
    color: #666;
    padding: 6px;
    border-radius: 6px;
    transition: all 0.2s;

    &:hover {
      background: rgba(0, 0, 0, 0.04);
      color: var(--primary-color);
    }
  }

  .role-tag {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 4px 12px;
    border-radius: 16px;
    background: linear-gradient(135deg, #00C9A7, #00B38F);
    color: #fff;
    font-size: 12px;
    font-weight: 500;
  }
}

// --- B端内?---
.admin-main {
  flex: 1;
  padding: 20px;
  overflow-y: auto;

  &.mobile-padding {
    padding: 0 !important;
    padding-bottom: 120px !important;
    background: #f8fafc;
  }

  // B端手机端：统一留出顶部 header (56px) 空间
  &.admin-mobile-padding {
    padding: 0 !important;
    padding-top: 56px !important;
    // 同时也留出底部空间，兼容组织者端的 TabBar
    padding-bottom: 120px !important;
    background: #f8fafc;
  }
}

  .logo-icon-box-white {
    width: 34px;
    height: 34px;
    background: #fff;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #00C9A7;
    flex-shrink: 0;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.08);
  }

  .logo-text-white {
    font-size: 18px;
    font-weight: 800;
    color: #fff;
    white-space: nowrap;
    letter-spacing: 0.5px;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }

// ================================================================
// 公共：退出登录高?
// ================================================================
:deep(.logout-item) {
  color: #F56C6C !important;

  .el-icon {
    color: #F56C6C !important;
  }
}

/* --- TG 同款流动胶囊导航?(精准复刻) --- */
// 已合并至 .mobile-floating-nav

// ================================================================
// 管理员手机端布局 ?玻璃拟?+ iOS 风格
// ================================================================
.admin-mobile-header {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 56px;
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: saturate(200%) blur(24px);
  -webkit-backdrop-filter: saturate(200%) blur(24px);
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 14px;
  border-bottom: none;

  // 底部渐变高光线（替代 box-shadow?
  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    height: 1px;
    background: linear-gradient(
      90deg,
      transparent 0%,
      rgba(0, 191, 166, 0.25) 30%,
      rgba(0, 191, 166, 0.35) 50%,
      rgba(0, 191, 166, 0.25) 70%,
      transparent 100%
    );
  }

  .menu-trigger {
    width: 38px;
    height: 38px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 11px;
    cursor: pointer;
    background: rgba(0, 0, 0, 0.04);
    transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
    -webkit-tap-highlight-color: transparent;

    &:active {
      background: rgba(0, 0, 0, 0.1);
      transform: scale(0.92);
    }

    .hamburger-lines {
      display: flex;
      flex-direction: column;
      gap: 4.5px;
      width: 18px;

      span {
        display: block;
        height: 2px;
        border-radius: 2px;
        background: #333;
        transition: all 0.25s;

        &:nth-child(2) {
          width: 70%;
        }
      }
    }
  }

  .admin-title {
    font-weight: 700;
    font-size: 17px;
    color: var(--text-primary, #1a1a1a);
    letter-spacing: 0.5px;
  }

  .header-right-action {
    width: 38px;
    display: flex;
    align-items: center;
    justify-content: center;

    .header-avatar {
      cursor: pointer;
      border: 2px solid rgba(0, 191, 166, 0.3);
      transition: border-color 0.2s;

      &:active {
        border-color: var(--primary-color);
      }
    }
  }
}

// --- 管理员侧滑抽?(iOS 设置风格) ---
.admin-menu-drawer {
  :deep(.el-drawer) {
    border-radius: 0 20px 20px 0;
    overflow: hidden;
    box-shadow: 8px 0 40px rgba(0, 0, 0, 0.15);
  }

  :deep(.el-drawer__body) {
    padding: 0;
  }

  .drawer-inner {
    height: 100%;
    display: flex;
    flex-direction: column;
    background: #f2f2f7;
  }

  // --- 顶部用户信息卡片 ---
  .drawer-profile {
    position: relative;
    flex-shrink: 0;
    overflow: hidden;

    .drawer-profile-bg {
      position: absolute;
      inset: 0;
      background: linear-gradient(145deg, var(--primary-color, #00BFA6), #009688, #00796B);

      &::after {
        content: '';
        position: absolute;
        inset: 0;
        background:
          radial-gradient(circle at 20% 80%, rgba(255,255,255,0.15) 0%, transparent 50%),
          radial-gradient(circle at 80% 20%, rgba(255,255,255,0.1) 0%, transparent 40%);
      }
    }

    .drawer-profile-content {
      position: relative;
      z-index: 1;
      padding: 36px 20px 24px;
      display: flex;
      align-items: center;
      gap: 14px;
    }

    .drawer-avatar {
      border: 2.5px solid rgba(255, 255, 255, 0.6);
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
      flex-shrink: 0;
    }

    .drawer-user-info {
      display: flex;
      flex-direction: column;
      gap: 6px;
      overflow: hidden;
    }

    .drawer-username {
      font-size: 18px;
      font-weight: 700;
      color: #fff;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      text-shadow: 0 1px 3px rgba(0,0,0,0.15);
    }

    .drawer-role-badge {
      display: inline-flex;
      align-items: center;
      padding: 2px 10px;
      border-radius: 20px;
      background: rgba(255, 255, 255, 0.22);
      backdrop-filter: blur(8px);
      color: rgba(255, 255, 255, 0.95);
      font-size: 11px;
      font-weight: 500;
      width: fit-content;
    }
  }

  // --- iOS 菜单包裹?---
  .drawer-menu-wrapper {
    flex: 1;
    overflow-y: auto;
    -webkit-overflow-scrolling: touch;
    padding: 12px;
    display: flex;
    flex-direction: column;
  }

  .drawer-menu {
    border-right: none !important;
    background: transparent !important;
    padding: 0;

    // 一级菜单项 & 子菜单标题：白色浮动胶囊
    :deep(.el-menu-item),
    :deep(.el-sub-menu__title) {
      height: 50px;
      line-height: 50px;
      border-radius: 12px;
      margin-bottom: 6px;
      font-size: 15px;
      font-weight: 500;
      color: #1c1c1e;
      background: #fff !important;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
      transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
      padding-left: 20px !important;
      border: none;

      .el-icon {
        font-size: 18px;
        color: #636366;
        transition: color 0.2s;
      }

      &:hover {
        background: #fff !important;
        color: var(--primary-color) !important;
        transform: translateX(2px);
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

        .el-icon {
          color: var(--primary-color);
        }
      }

      &:active {
        transform: scale(0.98);
      }
    }

    // 激活态：白色浮起 + 投影 + 左侧色条
    :deep(.el-menu-item.is-active) {
      background: #ffffff !important;
      color: var(--primary-color) !important;
      font-weight: 600;
      box-shadow: 0 4px 14px rgba(0, 0, 0, 0.07);
      position: relative;

      &::before {
        content: '';
        position: absolute;
        left: 6px;
        top: 50%;
        transform: translateY(-50%);
        width: 4px;
        height: 22px;
        border-radius: 4px;
        background: linear-gradient(180deg, var(--primary-color), #009688);
      }

      .el-icon {
        color: var(--primary-color) !important;
      }
    }

    // 子菜单展开后的嵌套?
    :deep(.el-menu--inline) {
      background: transparent !important;

      .el-menu-item {
        padding-left: 52px !important;
        height: 44px;
        line-height: 44px;
        font-size: 14px;
        font-weight: 400;
        background: rgba(255, 255, 255, 0.75) !important;
        box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
        margin-left: 8px;
        margin-right: 0;
        border-radius: 10px;
      }
    }

    // 子菜单标题微?
    :deep(.el-sub-menu) {
      .el-sub-menu__title {
        &::after {
          display: none;
        }
      }

      &.is-opened > .el-sub-menu__title {
        color: var(--primary-color) !important;
        background: rgba(0, 191, 166, 0.06) !important;
        box-shadow: none;

        .el-icon {
          color: var(--primary-color);
        }
      }
    }
  }
}

// ================================================================
// 全局按钮文字高对比度修复 (管理员移动端)
// ================================================================
.admin-main.admin-mobile-padding {
  // 所?primary / success 类型按钮强制白字
  :deep(.el-button--primary),
  :deep(.el-button--success) {
    color: #ffffff !important;
  }

  // 带渐变或深色背景的自定义按钮
  :deep(.el-button) {
    font-weight: 500;

    // 检?background 为非白非透明时，强制白字
    &[style*="background"] {
      color: #ffffff !important;
    }
  }

  // 针对常见自定义按?class
  :deep(.view-all-btn),
  :deep(.handle-btn),
  :deep(.action-btn),
  :deep(.submit-btn) {
    color: #ffffff !important;
  }
}
</style>

<!-- 全局样式：处?Element Plus append-to-body 的弹?抽屉 -->
<style lang="scss">
/* 全局样式：增?Element Plus 抽屉/弹窗的交互与视觉 */
.el-overlay {
  backdrop-filter: blur(4px) !important;
  -webkit-backdrop-filter: blur(4px) !important;
  pointer-events: auto !important;
}

/* 确保抽屉点击区域响应正常 */
.admin-menu-drawer {
  outline: none !important;
}

/* ================================================================ */
/* 极光水晶面板 - 用户下拉菜单精修 (Crystal Dropdown) */
/* ================================================================ */

/* 1. 容器：极光玻璃化 */
.el-dropdown-menu.user-dropdown {
  background: rgba(255, 255, 255, 0.85) !important;
  backdrop-filter: blur(20px) !important;
  -webkit-backdrop-filter: blur(20px) !important;
  border: 1px solid rgba(255, 255, 255, 0.6) !important;
  border-radius: 20px !important;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.08) !important;
  padding: 8px !important;
  overflow: hidden;
  width: 160px !important;
  min-width: 160px !important;
  margin-right: -10px !important;
  
  /* 动画起点：稍微缩小 */
  transform-origin: top right;
  animation: menu-pop 0.30s cubic-bezier(0.175, 0.885, 0.32, 1.275); /* 果冻弹跳曲线 */


}

/* ================================================================ */
/* 7. Theme Reveal Animation (View Transitions) */
/* ================================================================ */
::view-transition-old(root),
::view-transition-new(root) {
  animation: none;
  mix-blend-mode: normal;
}

::view-transition-old(root) {
  z-index: 1;
}
::view-transition-new(root) {
  z-index: 9999;
}

.dark::view-transition-old(root) {
  z-index: 9999;
}
.dark::view-transition-new(root) {
  z-index: 1;
}

/* 防止动画过程中出现白闪 */
html {
  background-color: var(--tg-bg-color);
}

/* 2. 菜单项：圆角悬浮 */
.el-dropdown-menu.user-dropdown .el-dropdown-menu__item {
  border-radius: 10px;
  margin: 4px 0;
  color: #444 !important;
  font-weight: 500;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex; 
  align-items: center; // 垂直居中对齐
  gap: 12px;
  padding: 12px 20px !important; // 左右边距一致，增加高度

  .el-icon, i, svg { 
    transition: transform 0.25s cubic-bezier(0.34, 1.56, 0.64, 1); 
    color: #888; 
    font-size: 18px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  span {
    line-height: 1; // 修复文字偏上的问题
  }

  /* Hover 特效：右?+ 变色 + 极光青背?*/
  &:hover {
    background: rgba(0, 147, 233, 0.08) !important;
    color: #0093E9 !important;
    padding-left: 20px !important; 

    .el-icon, i, svg { 
      color: #0093E9; 
      transform: scale(1.15) rotate(5deg); /* 图标跳动 */
    }
  }

  /* 选中状态?(如果 Element Plus 用到? */
  &.is-active {
    background: rgba(0, 147, 233, 0.05);
    color: #0093E9;
  }
}

/* 3. 退出登录按钮：独立危险样式 (Divider 风格) */
.el-dropdown-menu.user-dropdown .logout-item {
  border-top: 1px dashed rgba(0,0,0,0.06) !important;
  margin-top: 8px !important;
  padding-top: 12px !important;
  
  &:hover {
    background: rgba(255, 71, 87, 0.08) !important;
    color: #ff4757 !important;
    
    .el-icon, i, svg { 
      color: #ff4757; 
      transform: scale(1.15) rotate(-5deg);
    }
  }
}

/* 弹跳动画定义 */
@keyframes menu-pop {
  0% { 
    opacity: 0; 
    transform: scale(0.85) translateY(-10px); 
    filter: blur(10px);
  }
  100% { 
    opacity: 1; 
    transform: scale(1) translateY(0); 
    filter: blur(0);
  }
}

/* 移除 Element 默认的小三角 (彻底重合对齐) */
.el-popper.is-light.el-dropdown__popper.user-dropdown-popper {
  border: none !important;
  background: transparent !important;
  box-shadow: none !important;
  .el-popper__arrow {
    display: none !important; // 隐藏小三角，避免偏移误导
  }
}
</style>
