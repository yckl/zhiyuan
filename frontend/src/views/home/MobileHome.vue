<template>
  <div class="mobile-home-aurora">
    <!-- ==================== 沉浸式背景 ==================== -->
    <div class="aurora-bg"></div>

    <!-- ==================== 组织者风格头?& 扫码 ==================== -->
    <div class="dashboard-header anim-fade-in">
      <div class="user-info-row">
        <!-- 极光水晶装饰图标 -->
        <div class="glass-decorative-sphere">
          <div class="sphere-inner">
            <el-icon :size="28"><StarFilled /></el-icon>
          </div>
          <div class="sphere-glow"></div>
        </div>
        
        <!-- 用户信息 -->
        <div class="info-content">
          <div class="greeting-line">
            <span class="greeting-text">{{ timeGreeting }}</span>
            <span class="user-name">{{ userStore.userInfo?.name || userStore.userInfo?.username || '志愿者' }}</span>
          </div>
          
          <div class="role-badge-glass" v-if="isVolunteer">
            <el-icon><School /></el-icon>
            <span>{{ userStore.userInfo?.college || '计算机学院' }}</span>
          </div>
        </div>
      </div>
      
      <!-- 沉浸式扫码 -->
      <div class="scan-btn-premium" @click="$emit('nav', '/scan')">
        <div class="scan-icon-glass">
          <el-icon :size="22"><FullScreen /></el-icon>
        </div>
        <span class="scan-label">签到</span>
      </div>
    </div>

    <!-- ==================== 顶部英雄卡片 ==================== -->
    <div class="hero-section anim-fade-in">
      <el-carousel
        :interval="5000"
        height="220px"
        arrow="never"
        class="hero-carousel-glass"
      >
        <el-carousel-item v-for="item in banners" :key="item.id">
          <div class="hero-card">
            <el-image :src="getImageUrl(item.image || item.coverImage, '/default-cover.jpg')" fit="cover" class="hero-img">
  <template #error>
    <img :src="'/default-cover.jpg'" style="width:100%;height:100%;object-fit:cover"/>
  </template>
</el-image>
            <div class="hero-overlay-glass">
              <h1 class="hero-title">让志愿精神温暖每一个人</h1>
              <p class="hero-subtitle">传递爱心 · 奉献社会</p>
              <button class="glass-cta-capsule" @click="$emit('join')">
                <span>立即加入</span>
                <el-icon><ArrowRight /></el-icon>
              </button>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- ==================== 金刚区：中央悬浮玻璃面板 ==================== -->
    <div class="nav-dock-container anim-slide-up">
      <div class="nav-grid-glass">
        <div
          v-for="item in navItems"
          :key="item.label"
          class="nav-item-neumorph"
          @click="$emit('nav', item.path)"
        >
          <div class="icon-sphere" :style="{ background: item.color }">
            <el-icon :size="24"><component :is="item.icon" /></el-icon>
          </div>
          <span class="nav-label">{{ item.label }}</span>
        </div>
      </div>
    </div>

    <!-- ==================== 猜你喜欢：磨砂卡片流 ==================== -->
    <div class="recommend-section anim-slide-up-delayed">
      <div class="section-header-glass">
        <div class="header-left">
          <h2 class="section-title">🎯 猜你喜欢</h2>
          <span class="section-subtitle">智能算法 · 实时推荐</span>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click" @command="(val: string) => $emit('filter-change', val)">
            <div class="filter-capsule">
              <span>{{ filterLabel }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="">全部</el-dropdown-item>
                <el-dropdown-item command="hot">热门</el-dropdown-item>
                <el-dropdown-item command="recommend">推荐</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <button class="glass-more-btn" @click="$emit('more')">
            查看更多
          </button>
        </div>
      </div>

      <div v-loading="loading" class="activity-glass-list">
        <div
          v-for="(item, index) in activities"
          :key="item.id"
          class="activity-card-glass shadow-hover"
          :style="{ animationDelay: `${index * 0.1}s` }"
          @click="$emit('detail', item.id)"
        >
          <div class="card-visual">
            <el-image :src="getImageUrl(item.coverImage || item.image, '/default-activity.jpg')" fit="cover" class="card-img">
  <template #error>
    <img :src="'/default-cover.jpg'" style="width:100%;height:100%;object-fit:cover"/>
  </template>
</el-image>
            <div v-if="item.algoTag" class="glass-tag">
              {{ item.algoTag }}
            </div>
          </div>
          <div class="card-content">
            <h3 class="card-title">{{ item.title }}</h3>
            <div class="card-meta">
              <span class="meta-item"><el-icon><Location /></el-icon> {{ item.location || '待定' }}</span>
              <span class="meta-item"><el-icon><Timer /></el-icon> {{ formatDate(item.startTime) }}</span>
            </div>
          </div>
        </div>
        
        <el-empty v-if="!loading && activities.length === 0" description="暂无推荐活动" :image-size="80" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ArrowRight, Location, Timer, School, FullScreen, ArrowDown, StarFilled } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { getImageUrl } from '@/utils/image'
import { useUserStore } from '@/stores/user'
import { computed } from 'vue'

const props = defineProps<{
  banners: any[]
  navItems: any[]
  activities: any[]
  loading: boolean
  activeFilter: string
}>()

const emit = defineEmits(['join', 'nav', 'more', 'detail', 'filter-change'])

const filterLabel = computed(() => {
  const map: any = { '': '全部', 'hot': '热门', 'recommend': '推荐' }
  return map[props.activeFilter || ''] || '全部'
})

const formatDate = (date: string) => date ? dayjs(date).format('MM-DD') : '-'

const userStore = useUserStore()

const isVolunteer = computed(() => userStore.userInfo?.role === 'VOLUNTEER')

const timeGreeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})
</script>

<style lang="scss" scoped>
/* ==================== 动画定义 ==================== */
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

.anim-fade-in { animation: fadeInUp 0.8s cubic-bezier(0.2, 0.8, 0.2, 1) both; }
.anim-slide-up { animation: fadeInUp 0.8s cubic-bezier(0.2, 0.8, 0.2, 1) 0.2s both; }
.anim-slide-up-delayed { animation: fadeInUp 0.8s cubic-bezier(0.2, 0.8, 0.2, 1) 0.4s both; }

/* ==================== 布局与背?==================== */
.mobile-home-aurora {
  position: relative;
  min-height: 100vh;
  padding: 16px 16px 100px;
  overflow-x: hidden;
}

.aurora-bg {
  position: fixed;
  inset: 0;
  z-index: -1;
  background: linear-gradient(180deg, #E0F7FA 0%, #F0F4F8 100%);
  &::before {
    content: '';
    position: absolute;
    top: -10%; left: -10%;
    width: 60%; height: 40%;
    background: radial-gradient(circle, rgba(0, 147, 233, 0.1), transparent 70%);
    filter: blur(40px);
  }
}

/* ==================== Dashboard Header (Organizer Style) ==================== */
.dashboard-header {
  margin: -16px -16px 20px -16px;
  padding: 24px 20px 24px 20px;
  border-radius: 0 0 24px 24px;
  background: linear-gradient(135deg, #00C9A7 0%, #05D5B3 100%);
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 4px 20px rgba(0, 147, 233, 0.15);
  position: relative;
  z-index: 10;
}

.user-info-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

/* ==================== 极光水晶装饰图标 ==================== */
.glass-decorative-sphere {
  position: relative;
  width: 56px;
  height: 56px;
  flex-shrink: 0;

  .sphere-inner {
    width: 100%;
    height: 100%;
    background: rgba(255, 255, 255, 0.2);
    backdrop-filter: blur(15px);
    border: 1px solid rgba(255, 255, 255, 0.4);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    box-shadow: inset 0 0 15px rgba(255,255,255,0.3);
    z-index: 2;
    position: relative;
    animation: float 3s ease-in-out infinite;

    .el-icon {
      filter: drop-shadow(0 0 8px rgba(255,255,255,0.8));
    }
  }

  .sphere-glow {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 80%;
    height: 80%;
    background: #fff;
    filter: blur(20px);
    opacity: 0.3;
    z-index: 1;
  }
}

.info-content {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.greeting-line {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.role-badge-glass {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  padding: 4px 14px;
  border-radius: 20px;
  font-size: 13px;
  border: 1px solid rgba(255, 255, 255, 0.25);
  font-weight: 500;
  color: rgba(255,255,255,0.9);
  width: fit-content;
}

.scan-btn-premium {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  transition: all 0.2s;

  &:active { transform: scale(0.9); }

  .scan-icon-glass {
    width: 44px;
    height: 44px;
    border-radius: 14px;
    background: rgba(255, 255, 255, 0.2);
    backdrop-filter: blur(12px);
    border: 1px solid rgba(255, 255, 255, 0.3);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    box-shadow: 0 8px 16px rgba(0,0,0,0.1);
  }

  .scan-label {
    font-size: 11px;
    font-weight: 700;
    letter-spacing: 0.5px;
    opacity: 0.9;
  }
}

/* ==================== Hero Banner ==================== */
.hero-section {
  margin-bottom: 24px;

}

.hero-carousel-glass {
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
}

.hero-card {
  position: relative;
  height: 100%;
  .hero-img { width: 100%; height: 100%; }
}

.hero-overlay-glass {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0,0,0,0.4) 0%, transparent 60%);
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 24px;
  color: #fff;

  .hero-title { font-size: 20px; font-weight: 800; margin: 0 0 4px; letter-spacing: 0.5px; }
  .hero-subtitle { font-size: 13px; opacity: 0.8; margin: 0 0 16px; }
}

.glass-cta-capsule {
  align-self: flex-start;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 18px;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 20px;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  &:active { transform: scale(0.95); background: rgba(255, 255, 255, 0.4); }
}

/* ==================== 金刚区玻璃面板 ==================== */
.nav-dock-container {
  margin-top: -36px;
  position: relative;
  z-index: 10;
  margin-bottom: 24px;
}

.nav-grid-glass {
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(25px);
  -webkit-backdrop-filter: blur(25px);
  border-radius: 28px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 10px 40px rgba(31, 38, 135, 0.06);
  padding: 24px 12px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px 10px;
}

.nav-item-neumorph {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;

  .icon-sphere {
    width: 48px;
    height: 48px;
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    box-shadow: 0 6px 12px rgba(0, 0, 0, 0.1);
    transition: transform 0.2s;
    .el-icon { filter: drop-shadow(0 2px 4px rgba(0,0,0,0.1)); }
  }

  .nav-label { font-size: 12px; color: #1e293b; font-weight: 600; }

  &:active {
    transform: translateY(2px);
    .icon-sphere { transform: scale(0.9); }
  }
}

/* ==================== 猜你喜欢卡片 ==================== */
.section-header-glass {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 16px;
  padding: 0 4px;

  .section-title { font-size: 18px; font-weight: 800; color: #1e293b; margin: 0; }
  .section-subtitle { font-size: 11px; color: #94a3b8; margin-top: 2px; }

  .header-right {
    display: flex;
    align-items: center;
    gap: 8px;

    .filter-capsule {
      display: flex;
      align-items: center;
      gap: 4px;
      padding: 4px 12px;
      background: rgba(255, 255, 255, 0.6);
      border: 1px solid rgba(0, 147, 233, 0.2);
      border-radius: 20px;
      color: #0093E9;
      font-size: 13px;
      font-weight: 700;
      cursor: pointer;
    }

    .glass-more-btn {
      padding: 4px 12px;
      background: rgba(0, 147, 233, 0.1);
      border: 1px solid rgba(0, 147, 233, 0.2);
      border-radius: 12px;
      color: #0093E9;
      font-size: 11px;
      font-weight: 700;
    }
  }
}

.activity-glass-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.activity-card-glass {
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(15px);
  -webkit-backdrop-filter: blur(15px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  padding: 12px;
  display: flex;
  gap: 14px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.03);
  transition: all 0.3s cubic-bezier(0.2, 0.8, 0.2, 1);
  animation: fadeInUp 0.6s ease-out both;

  &:active { transform: scale(0.98); }

  .card-visual {
    position: relative;
    width: 100px;
    height: 100px;
    border-radius: 14px;
    overflow: hidden;
    flex-shrink: 0;

    .card-img { width: 100%; height: 100%; }

    .glass-tag {
      position: absolute;
      top: 6px; left: 6px;
      padding: 2px 6px;
      background: rgba(0, 191, 166, 0.8);
      backdrop-filter: blur(4px);
      color: #fff;
      font-size: 9px;
      font-weight: 700;
      border-radius: 6px;
    }
  }

  .card-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;

    .card-title {
      font-size: 16px;
      font-weight: 700;
      color: #1e293b;
      margin: 0 0 8px;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .card-meta {
      display: flex;
      flex-direction: column;
      gap: 4px;
      .meta-item {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 12px;
        color: #64748b;
        .el-icon { color: #94a3b8; }
      }
    }
  }
}
</style>
