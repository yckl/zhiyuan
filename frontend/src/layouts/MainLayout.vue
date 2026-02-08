<template>
  <el-container class="main-layout" :class="{ 'is-mobile': isMobile }">
    <!-- 移动端遮罩层 -->
    <div v-if="isMobile && !isCollapsed" class="mobile-overlay" @click="isCollapsed = true"></div>
    
    <el-aside :width="sidebarWidth" class="sidebar" :class="{ 'sidebar-mobile': isMobile, 'sidebar-hidden': isMobile && isCollapsed }">
      <div class="sidebar-wrapper">
        <div class="logo-section" @click="handleLogoClick">
          <div class="logo-icon-box">
            <el-icon :size="28"><Aim /></el-icon>
          </div>
          <span v-show="!isCollapsed || isMobile" class="logo-text">志愿者系统</span>
        </div>

        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapsed"
          :router="true"
          class="sidebar-menu"
          :collapse-transition="false"
          background-color="#1e293b"
          text-color="#ffffff"
          active-text-color="#ffffff"
        >
          <!-- 1. 基础功能 (仅限志愿者) -->
          <template v-if="isVolunteer">
            <el-menu-item index="/home" class="core-group">
              <el-icon><HomeFilled /></el-icon>
              <template #title>首页概览</template>
            </el-menu-item>

            <el-menu-item index="/notice/list" class="core-group">
              <el-icon><Bell /></el-icon>
              <template #title>通知公告</template>
            </el-menu-item>

            <el-menu-item index="/notice/my" class="core-group">
              <el-icon><Message /></el-icon>
              <template #title>消息中心</template>
            </el-menu-item>

            <el-menu-item index="/activity" class="core-group">
              <el-icon><Calendar /></el-icon>
              <template #title>活动中心</template>
            </el-menu-item>
            
            <el-sub-menu index="/mall" class="mall-group">
              <template #title>
                <el-icon><Shop /></el-icon>
                <span>积分商城</span>
              </template>
              <el-menu-item index="/mall/index">商城首页</el-menu-item>
              <el-menu-item index="/mall/backpack">我的背包</el-menu-item>
              <el-menu-item index="/mall/wheel">幸运转盘</el-menu-item>
              <el-menu-item index="/mall/checkin">每日签到</el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="/training" class="training-group">
              <template #title>
                <el-icon><Reading /></el-icon>
                <span>培训学院</span>
              </template>
              <el-menu-item index="/training/index">学院首页</el-menu-item>
              <el-menu-item index="/training/list">课程列表</el-menu-item>
              <el-menu-item index="/training/my">我的考试</el-menu-item>
            </el-sub-menu>

            <el-menu-item index="/experience" class="core-group">
              <el-icon><Document /></el-icon>
              <template #title>心得分享</template>
            </el-menu-item>

            <el-menu-item index="/feedback" class="core-group">
              <el-icon><ChatDotRound /></el-icon>
              <template #title>问题反馈</template>
            </el-menu-item>

            <el-sub-menu index="my" class="profile-group">
              <template #title>
                <el-icon><User /></el-icon>
                <span>个人中心</span>
              </template>
              <el-menu-item index="/profile/info">我的资料</el-menu-item>
              <el-menu-item index="/profile/stats">数据中心</el-menu-item>
              <el-menu-item index="/profile/reviews">我的评价</el-menu-item>
              <el-menu-item index="/profile/honor">荣誉证书</el-menu-item>
              <el-menu-item index="/profile/favorite">我的收藏</el-menu-item>
              <el-menu-item index="/my-activities">我的活动</el-menu-item>
              <el-menu-item index="/my-experiences">我的心得</el-menu-item>
            </el-sub-menu>
          </template>

          <!-- 2. 组织者管理后台 -->
          <template v-if="isOrganizer">
            <div v-show="!isCollapsed" class="menu-divider">管理平台</div>
            <template v-for="menu in orgMenus" :key="menu.path">
              <el-sub-menu v-if="menu.children" :index="menu.path" class="core-group">
                <template #title>
                  <el-icon><component :is="menu.icon" /></el-icon>
                  <span>{{ menu.name }}</span>
                </template>
                <el-menu-item v-for="child in menu.children" :key="child.path" :index="child.path">
                  {{ child.name }}
                </el-menu-item>
              </el-sub-menu>
              <el-menu-item v-else :index="menu.path" class="core-group">
                <el-icon><component :is="menu.icon" /></el-icon>
                <template #title>{{ menu.name }}</template>
              </el-menu-item>
            </template>
          </template>

          <!-- 3. 管理员后台 -->
          <template v-if="isAdmin">
            <div v-show="!isCollapsed" class="menu-divider">系统管理</div>
            <template v-for="menu in adminMenus" :key="menu.path">
              <el-sub-menu v-if="menu.children" :index="menu.path" class="core-group">
                <template #title>
                  <el-icon><component :is="menu.icon" /></el-icon>
                  <span>{{ menu.name }}</span>
                </template>
                <el-menu-item v-for="child in menu.children" :key="child.path" :index="child.path">
                  {{ child.name }}
                </el-menu-item>
              </el-sub-menu>
              <el-menu-item v-else :index="menu.path" class="core-group">
                <el-icon><component :is="menu.icon" /></el-icon>
                <template #title>{{ menu.name }}</template>
              </el-menu-item>
            </template>
          </template>
        </el-menu>
      </div>
    </el-aside>

    <el-container class="main-container">
      <el-header class="header">
        <div class="header-left">
          <div class="collapse-btn-wrapper" @click="isCollapsed = !isCollapsed">
            <el-icon class="collapse-btn">
              <component :is="isCollapsed ? Expand : Fold" />
            </el-icon>
          </div>
          <el-breadcrumb v-if="!isMobile" separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
          <span v-else class="mobile-page-title">{{ route.meta.title || '首页' }}</span>
        </div>

        <div class="header-right">
          <theme-button 
            :value="isDark ? 'dark' : 'light'" 
            size="1.4" 
            @change="handleThemeChange"
          ></theme-button>

          <div v-if="isLoggedIn" class="role-badge-wrapper">
            <div class="role-badge" :style="badgeStyle">
              <el-icon class="badge-icon"><component :is="roleInfo.icon" /></el-icon>
              <span class="badge-text">{{ roleInfo.label }}</span>
            </div>
          </div>

          <template v-if="isLoggedIn">
            <el-dropdown @command="handleCommand">
              <div class="user-info">
                <el-avatar :size="32" :src="userInfo?.avatar" class="user-avatar">
                  <el-icon><User /></el-icon>
                </el-avatar>
                <div class="name-box">
                  <span class="username">{{ userInfo?.nickname || userInfo?.username || '用户' }}</span>
                </div>
                <el-icon class="arrow-icon"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="custom-dropdown-menu">
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon> 个人资料
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout" class="logout-item">
                    <el-icon><SwitchButton /></el-icon> 登出系统
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" size="small" rounded @click="router.push('/login')">登录</el-button>
            <el-button size="small" rounded @click="router.push('/register')">注册</el-button>
          </template>
        </div>
      </el-header>

      <el-main class="main-content">
        <div class="content-wrapper">
          <router-view v-slot="{ Component }">
            <transition name="page-fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
      </el-main>

      <el-footer class="footer" height="50px">
        <div class="footer-content">
          <span>© 2026 校园志愿者管理系统</span>
          <span class="footer-dot">•</span>
          <span>用科技传递温暖</span>
        </div>
      </el-footer>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Aim, HomeFilled, Calendar, Document, User, 
  Fold, Expand, ArrowDown, SwitchButton, Shop, Reading, Bell, Message,
  Star, Connection, Management
} from '@element-plus/icons-vue'
import { adminMenus } from '@/config/adminMenus'
import { orgMenus } from '@/config/orgMenus'
import { useUserStore } from '@/stores/user'
import '@/utils/themeButton.js'
import gsap from 'gsap'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 响应式状态
const windowWidth = ref(window.innerWidth)
const MOBILE_BREAKPOINT = 768
const TABLET_BREAKPOINT = 992

const isMobile = computed(() => windowWidth.value < MOBILE_BREAKPOINT)
const isTablet = computed(() => windowWidth.value >= MOBILE_BREAKPOINT && windowWidth.value < TABLET_BREAKPOINT)

const isCollapsed = ref(window.innerWidth < MOBILE_BREAKPOINT)
const isDark = ref(localStorage.getItem('theme-mode') === 'dark' || document.documentElement.classList.contains('dark'))

// 侧边栏宽度计算
const sidebarWidth = computed(() => {
  if (isMobile.value) return '240px' // 移动端固定宽度（抽屉模式）
  return isCollapsed.value ? '72px' : '240px'
})

// 处理窗口大小变化
const handleResize = () => {
  windowWidth.value = window.innerWidth
  // 切换到移动端时自动折叠侧边栏
  if (windowWidth.value < MOBILE_BREAKPOINT) {
    isCollapsed.value = true
  }
}

// Logo点击处理（移动端关闭侧边栏后跳转）
const handleLogoClick = () => {
  if (isMobile.value) {
    isCollapsed.value = true
  }
  router.push('/')
}

const activeMenu = computed(() => route.path)
const userInfo = computed(() => userStore.userInfo)
const isLoggedIn = computed(() => userStore.isLoggedIn)

const isVolunteer = computed(() => userStore.role === 'VOLUNTEER')
const isOrganizer = computed(() => userStore.role === 'ORGANIZER')
const isAdmin = computed(() => userStore.role === 'ADMIN')

const roleMap: Record<string, { type: 'success' | 'warning' | 'danger', label: string, icon: any }> = {
  ADMIN: { type: 'danger', label: '系统管理员', icon: Management },
  ORGANIZER: { type: 'warning', label: '项目负责人', icon: Connection },
  VOLUNTEER: { type: 'success', label: '注册志愿者', icon: Star }
}

const roleInfo = computed(() => {
  return roleMap[userStore.role] || { type: 'info', label: '普通用户', icon: User }
})

const badgeStyle = computed(() => {
  if (isDark.value) {
    return {
      background: 'linear-gradient(135deg, #00897B 0%, #00BFA6 100%)',
      boxShadow: '0 4px 15px rgba(0, 191, 166, 0.4)'
    }
  }
  return {
    background: 'linear-gradient(135deg, #00BFA6 0%, #26a69a 100%)',
    boxShadow: '0 4px 15px rgba(0, 191, 166, 0.25)'
  }
})

const handleThemeChange = (e: any) => {
  const theme = e.detail
  isDark.value = theme === 'dark'
  localStorage.setItem('theme-mode', theme)
  
  if (isDark.value) {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  handleResize() // 初始化
  
  gsap.from('.header', {
    y: -20,
    opacity: 1,
    duration: 0.8,
    ease: 'power3.out'
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

// 路由变化时自动关闭移动端侧边栏
watch(() => route.path, () => {
  if (isMobile.value) {
    isCollapsed.value = true
  }
})

const handleCommand = (command: string) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出系统吗？', '提示', {
      confirmButtonText: '登出',
      cancelButtonText: '取消',
      type: 'warning',
      roundButton: true
    }).then(() => {
      userStore.clearUser()
      ElMessage.success('已安全退出')
      router.push('/login')
    }).catch(() => {})
  } else if (command === 'profile') {
    router.push('/profile/info')
  }
}
</script>

<style lang="scss" scoped>
.main-layout {
  height: 100vh;
  background-color: #f8fafc;
}

html.dark .main-layout {
  background-color: #0f172a;
}

.sidebar {
  background: #1e293b;
  box-shadow: 4px 0 20px rgba(0, 0, 0, 0.1);
  transition: width 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  z-index: 1001;
  position: relative;

  .sidebar-wrapper {
    height: 100%;
    display: flex;
    flex-direction: column;
    background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
    overflow-y: auto;
    overflow-x: hidden;
    
    /* 自定义滚动条样式 */
    &::-webkit-scrollbar {
      width: 6px;
    }
    &::-webkit-scrollbar-track {
      background: transparent;
    }
    &::-webkit-scrollbar-thumb {
      background: rgba(255, 255, 255, 0.2);
      border-radius: 3px;
      
      &:hover {
        background: rgba(255, 255, 255, 0.3);
      }
    }
  }

  .logo-section {
    height: 72px;
    display: flex;
    align-items: center;
    padding: 0 20px;
    gap: 12px;
    cursor: pointer;
    transition: all 0.3s;
    
    .logo-icon-box {
      width: 36px;
      height: 36px;
      background: linear-gradient(135deg, #00BFA6, #26a69a);
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      box-shadow: 0 4px 10px rgba(0, 191, 166, 0.3);
    }

    .logo-text {
      font-size: 18px;
      font-weight: 700;
      color: #fff;
      letter-spacing: 1px;
      white-space: nowrap;
    }
  }

  .sidebar-menu {
    border-right: none;
    flex: 1;
    background: transparent;
    padding: 10px;

    :deep(.el-menu-item), :deep(.el-sub-menu__title) {
      height: 50px;
      line-height: 50px;
      margin-bottom: 4px;
      border-radius: 10px;
      color: #ffffff !important; /* Force pure white for maximum visibility */
      opacity: 1 !important; /* Ensure no animation overrides visibility */
      transition: all 0.3s;

      &:hover {
        color: #fff !important;
        background: rgba(255, 255, 255, 0.15) !important;
      }

      .el-icon {
        font-size: 20px;
        transition: transform 0.3s, color 0.3s;
        opacity: 1 !important;
      }
    }

    /* Group-based Icon Colors - Extra Bright */
    :deep(.core-group) > .el-icon, :deep(.core-group) > .el-sub-menu__title > .el-icon { color: #60a5fa !important; }
    :deep(.mall-group) > .el-icon, :deep(.mall-group) > .el-sub-menu__title > .el-icon { color: #facc15 !important; }
    :deep(.training-group) > .el-icon, :deep(.training-group) > .el-sub-menu__title > .el-icon { color: #34d399 !important; }
    :deep(.profile-group) > .el-icon, :deep(.profile-group) > .el-sub-menu__title > .el-icon { color: #c084fc !important; }

    /* Sub-menu styling */
    :deep(.el-menu--inline) {
      background: rgba(0, 0, 0, 0.35) !important;
      border-radius: 8px;
      margin: 4px 8px;
      padding: 4px;

      .el-menu-item {
        margin-bottom: 2px;
        height: 48px;
        line-height: 48px;
        padding-left: 48px !important;
        font-size: 15px;
        color: #ffffff !important;
        opacity: 1 !important;

        &:hover {
          color: #fff !important;
          background: rgba(255, 255, 255, 0.1) !important;
        }

        &.is-active {
          color: #ffffff !important;
          background: #409eff !important;
          font-weight: 800;
        }
      }
    }

    /* Active Item Polish */
    :deep(.el-menu-item.is-active), :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
      color: #ffffff !important;
      opacity: 1 !important;
      font-weight: 800;
      background: rgba(64, 158, 255, 0.2) !important;
      
      .el-icon {
        color: #ffffff !important;
        transform: scale(1.1);
      }
    }

    :deep(.el-menu-item.is-active) {
      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 0;
        height: 100%;
        width: 6px;
        background: #409eff;
        box-shadow: 0 0 15px #409eff;
      }
    }

    .menu-divider {
      padding: 20px 20px 10px;
      font-size: 11px;
      font-weight: 600;
      color: #64748b;
      text-transform: uppercase;
      letter-spacing: 1.5px;
    }
  }
}

.main-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  height: 72px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(226, 232, 240, 0.8);
  z-index: 1000;

  .header-left {
    display: flex;
    align-items: center;
    gap: 20px;

    .collapse-btn-wrapper {
      width: 36px;
      height: 36px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.2s;
      background: #f1f5f9;

      &:hover {
        background: #e2e8f0;
        color: #00BFA6;
      }

      .collapse-btn {
        font-size: 20px;
      }
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 24px;

    .role-badge {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 6px 14px;
      border-radius: 20px;
      color: white;
      font-size: 12px;
      font-weight: 600;
      cursor: default;
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 6px 12px;
      border-radius: 12px;
      transition: all 0.3s;
      cursor: pointer;

      &:hover {
        background: #f1f5f9;
        
        .user-avatar {
          transform: scale(1.05);
          box-shadow: 0 0 10px rgba(0, 191, 166, 0.2);
        }
      }

      .user-avatar {
        transition: transform 0.3s;
      }

      .username {
        font-weight: 600;
        color: #334155;
        font-size: 14px;
      }

      .arrow-icon {
        font-size: 12px;
        color: #94a3b8;
      }
    }
  }
}

html.dark .header {
  background: rgba(15, 23, 42, 0.8);
  border-bottom: 1px solid rgba(51, 65, 85, 0.8);
  
  .header-left .collapse-btn-wrapper {
    background: #1e293b;
    &:hover { background: #334155; }
  }
  
  .header-right .user-info:hover {
    background: #1e293b;
  }
  
  .header-right .user-info .username {
    color: #e2e8f0;
  }
}

.main-content {
  padding: 24px;
  background: #f8fafc;
  overflow-y: auto;
  
  .content-wrapper {
    max-width: 1400px;
    margin: 0 auto;
  }
}

html.dark .main-content {
  background: #0f172a;
}

.footer {
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  border-top: 1px solid #e2e8f0;

  .footer-content {
    color: #94a3b8;
    font-size: 12px;
    display: flex;
    align-items: center;
    gap: 10px;
    
    .footer-dot {
      opacity: 0.5;
    }
  }
}

html.dark .footer {
  background: #0f172a;
  border-top: 1px solid #1e293b;
}

/* Page Transition */
.page-fade-enter-active,
.page-fade-leave-active {
  transition: all 0.3s ease;
}

.page-fade-enter-from {
  opacity: 0;
  transform: translateY(15px);
}

.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-15px);
}

/* 移动端遮罩层 */
.mobile-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* 移动端侧边栏样式 */
.sidebar-mobile {
  position: fixed !important;
  top: 0;
  left: 0;
  height: 100vh;
  z-index: 1001;
  transform: translateX(0);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.sidebar-hidden {
  transform: translateX(-100%) !important;
}

/* 移动端页面标题 */
.mobile-page-title {
  font-size: 16px;
  font-weight: 600;
  color: #334155;
}

html.dark .mobile-page-title {
  color: #e2e8f0;
}

/* 响应式适配 - 平板端 */
@media (max-width: 992px) {
  .main-content {
    padding: 16px !important;
  }
  
  .header {
    padding: 0 16px;
    
    .header-right {
      gap: 16px;
      
      .role-badge {
        display: none; /* 平板上隐藏角色标签 */
      }
    }
  }
}

/* 响应式适配 - 手机端 */
@media (max-width: 768px) {
  .main-layout.is-mobile {
    .main-container {
      margin-left: 0 !important;
    }
  }
  
  .header {
    height: 56px;
    padding: 0 12px;
    
    .header-left {
      gap: 12px;
      
      .collapse-btn-wrapper {
        width: 40px;
        height: 40px;
      }
    }
    
    .header-right {
      gap: 8px;
      
      .role-badge-wrapper {
        display: none;
      }
      
      .user-info {
        padding: 4px 8px;
        gap: 8px;
        
        .name-box {
          display: none; /* 手机上隐藏用户名 */
        }
        
        .arrow-icon {
          display: none;
        }
      }
    }
  }
  
  .main-content {
    padding: 12px !important;
    
    .content-wrapper {
      max-width: 100%;
    }
  }
  
  .footer {
    height: 40px !important;
    
    .footer-content {
      font-size: 10px;
      gap: 6px;
    }
  }
}

/* 超小屏幕 - iPhone SE 等 */
@media (max-width: 400px) {
  .header {
    padding: 0 8px;
    
    .header-left {
      gap: 8px;
    }
    
    .header-right {
      gap: 4px;
    }
  }
  
  .main-content {
    padding: 8px !important;
  }
  
  .mobile-page-title {
    font-size: 14px;
  }
}
</style>
