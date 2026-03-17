<template>
  <div class="home-page" :class="{ 'is-mobile': isMobile }">

    <!-- ==================== PC 英雄 Banner ==================== -->
    <div v-if="!isMobile" class="hero-banner">
      <el-carousel
        :interval="5000"
        height="420px"
        arrow="hover"
        class="hero-carousel"
      >
        <el-carousel-item v-for="item in heroBanners" :key="item.id">
          <div class="hero-slide">
            <el-image :src="item.image || item.coverImage || '/default-cover.jpg'" fit="cover" class="hero-bg" lazy>
              <template #error><div class="hero-fallback"></div></template>
            </el-image>
            <div class="slide-gradient"></div>
          </div>
        </el-carousel-item>
        <el-carousel-item v-if="heroBanners.length === 0">
          <div class="hero-slide hero-default">
            <div class="slide-gradient"></div>
          </div>
        </el-carousel-item>
      </el-carousel>

      <!-- 浮动文字 (不影响轮播) -->
      <div class="hero-overlay" @click.stop>
        <div class="hero-content">
          <h1 class="hero-title">让志愿精神温暖每一个人</h1>
          <p class="hero-subtitle">传递爱心 · 奉献社会 · 汇聚青春力量</p>
          <button class="hero-cta" @click="router.push('/activity')">
            <span>立即加入</span>
            <el-icon><ArrowRight /></el-icon>
          </button>
        </div>
      </div>

      <!-- 底部弧形分割 -->
      <div class="hero-curve">
        <svg viewBox="0 0 1440 60" preserveAspectRatio="none">
          <path d="M0,60 C480,0 960,0 1440,60 L1440,60 L0,60 Z" :fill="isMobile ? '#fff' : 'var(--app-bg)'" />
        </svg>
      </div>
    </div>

    <!-- ==================== 移动端：Aurora Glass Home ==================== -->
    <MobileHome
      v-if="isMobile"
      :banners="heroBanners"
      :nav-items="kingkongItems"
      :activities="displayedActivities"
      :loading="loadingActivities"
      :active-filter="activityFilter"
      @filter-change="(val: string) => activityFilter = val"
      @join="handleNav('/activity')"
      @nav="handleNav"
      @more="handleNav('/activity')"
      @detail="(id) => router.push(`/activity/${id}`)"
    />

    <!-- ==================== PC端内?(原来的逻辑保持不变) ==================== -->
    <template v-else>
      <!-- PC端：悬浮渐变快捷卡片 -->
      <div class="pc-quick-section">
        <div class="pc-quick-grid">
          <div
            v-for="(item, i) in pcQuickLinks"
            :key="item.label"
            class="quick-card"
            :style="{ animationDelay: `${i * 0.1}s` }"
            @click="handleNav(item.path)"
          >
            <div class="qc-icon" :style="{ background: item.color }">
              <el-icon :size="28"><component :is="item.icon" /></el-icon>
            </div>
            <div class="qc-text">
              <span class="qc-label">{{ item.label }}</span>
              <span class="qc-arrow"></span>
            </div>
          </div>
        </div>
      </div>

      <!-- 热门活动?(仅PC) -->
      <div class="activity-section">
        <div class="section-header">
          <div class="section-title-area">
            <span class="section-title">🔥 为你推荐</span>
            <span class="section-subtitle">基于多模态算法，发现最适合你的志愿机会</span>
          </div>
          <div class="section-actions">
            <el-select
              v-model="activityFilter"
              placeholder="筛选"
              class="custom-filter-select"
              @change="handleFilterChange"
            >
              <el-option label="全部" value="" />
              <el-option label="热门" value="hot" />
              <el-option label="推荐" value="recommend" />
            </el-select>
            <button class="view-more-btn" @click="router.push('/activity')">
              查看更多 <el-icon><ArrowRight /></el-icon>
            </button>
          </div>
        </div>

        <!-- 骨架屏 -->
        <div v-if="loadingActivities" class="skeleton-grid">
          <div v-for="i in 6" :key="i" class="skeleton-card">
            <el-skeleton animated>
              <template #template>
                <el-skeleton-item variant="image" style="height: 160px" />
                <div style="padding: 14px">
                  <el-skeleton-item variant="h3" style="width: 80%" />
                  <el-skeleton-item variant="text" style="width: 60%; margin-top: 12px" />
                  <el-skeleton-item variant="text" style="width: 40%; margin-top: 8px" />
                </div>
              </template>
            </el-skeleton>
          </div>
        </div>

        <!-- 活动网格 (PC) -->
        <div v-else>
          <TransitionGroup name="card-fade" tag="div" class="pc-activity-grid">
          <div v-for="item in displayedActivities" :key="item.id" class="grid-item recommend-card-wrapper">
              <div v-if="item.algoTag" class="algo-tag-float">
                <el-tooltip :content="item.reason" placement="top">
                  <span class="tag-content"><el-icon><Opportunity /></el-icon> {{ item.algoTag }}</span>
                </el-tooltip>
              </div>
              <ActivityCard :activity="item" />
            </div>
          </TransitionGroup>
          <el-empty v-if="displayedActivities.length === 0" description="暂无推荐活动" :image-size="80" />
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { request } from '@/utils/request'
import {
  ArrowRight, Reading, Shop, Histogram, Opportunity,
  Calendar, Bell, ChatDotRound, Setting, Clock, Promotion
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import ActivityCard from '@/components/ActivityCard.vue'
import MobileHome from './MobileHome.vue'

import { useUserStore } from '@/stores/user'
const router = useRouter()
const userStore = useUserStore()

// 统一导航处理（处理游客拦截）
const handleNav = (path: string) => {
  // 定义需要登录才能访问的资源
  const privatePaths = ['/feedback', '/profile', '/user/volunteer-record', '/experience/create', '/mall/backpack', '/training/my']
  const needsAuth = privatePaths.some(p => path.startsWith(p))

  if (needsAuth && !userStore.isLoggedIn) {
    ElMessage.info('该功能需登录后使用')
    router.push({ path: '/login', query: { redirect: path } })
    return
  }
  router.push(path)
}

// ================== 响应式判断 ==================
const windowWidth = ref(window.innerWidth)
const isMobile = computed(() => windowWidth.value < 768)
onMounted(() => {
  window.addEventListener('resize', () => { windowWidth.value = window.innerWidth })
})

// ================== 金刚区配置 ==================
const kingkongItems = [
  { label: '签到',  path: '/mall/checkin',    icon: Calendar,     color: 'linear-gradient(135deg, #FF6B6B, #EE5A24)' },
  { label: '商城',  path: '/mall/index',      icon: Shop,         color: 'linear-gradient(135deg, #FFA502, #E67E22)' },
  { label: '培训',  path: '/training/index',  icon: Reading,      color: 'linear-gradient(135deg, #1DD1A1, #10AC84)' },
  { label: '圈子',  path: '/experience',      icon: Promotion,    color: 'linear-gradient(135deg, #54A0FF, #2E86DE)' },
  { label: '反馈',  path: '/feedback',        icon: ChatDotRound, color: 'linear-gradient(135deg, #A29BFE, #6C5CE7)' },
  { label: '公告',  path: '/notice/list',     icon: Bell,         color: 'linear-gradient(135deg, #FD79A8, #E84393)' },
  { label: '时长',  path: '/profile/history', icon: Clock,        color: 'linear-gradient(135deg, #00CEC9, #00B894)' },
  { label: '设置',  path: '/profile/info',    icon: Setting,      color: 'linear-gradient(135deg, #636E72, #2D3436)' },
]

// ================== PC端快捷入口 ==================
const pcQuickLinks = [
  { label: '活动中心', path: '/activity',       icon: Opportunity, color: 'linear-gradient(135deg, #409eff, #2e86de)' },
  { label: '培训学院', path: '/training/index',  icon: Reading,     color: 'linear-gradient(135deg, #67c23a, #10ac84)' },
  { label: '积分商城', path: '/mall/index',      icon: Shop,        color: 'linear-gradient(135deg, #e6a23c, #e67e22)' },
  { label: '志愿记录', path: '/profile/history', icon: Histogram,   color: 'linear-gradient(135deg, #f56c6c, #ee5a24)' },
]

// ================== 数据状态 ==================
const banners = ref<any[]>([])
const featuredActivities = ref<any[]>([])
const activities = ref<any[]>([])
const recommendations = ref<any[]>([])
const loadingActivities = ref(false)
const activityFilter = ref('')

const heroBanners = computed(() => {
  if (banners.value.length) return banners.value
  if (featuredActivities.value.length) return featuredActivities.value
  return []
})

const displayedActivities = computed(() => {
  if (activityFilter.value === 'recommend') {
    return recommendations.value.length > 0 ? recommendations.value : activities.value.filter(a => a.isFeatured || a.isRecommended)
  }
  if (activityFilter.value === 'hot') {
    return [...activities.value].sort((a, b) => (b.viewCount || 0) - (a.viewCount || 0))
  }
  return activities.value
})

const handleFilterChange = () => { /* computed handled */ }

// ================== 接口请求 ==================
const fetchBanners = async () => {
  try { banners.value = (await request.get('/banner/list')).data || [] }
  catch (e) { console.error('获取轮播图失败', e) }
}

const fetchFeaturedActivities = async () => {
  try {
    const res = await request.get('/activity/list', { page: 1, size: 5, isFeatured: 1, status: 2 })
    featuredActivities.value = res.data?.records || []
  } catch (e) { console.error('获取精选活动失败', e) }
}

const fetchActivities = async () => {
  loadingActivities.value = true
  try {
    const res = await request.get('/activity/list', { page: 1, size: 9, status: 2, orderBy: 'viewCount' })
    activities.value = res.data?.records || []
  } catch (e) { console.error('获取活动列表失败:', e) }
  finally { loadingActivities.value = false }
}

const fetchRecommendations = async () => {
  try {
    const res = await request.get('/recommendation/home')
    if (res.code === 200) {
      recommendations.value = res.data || []
    }
  } catch (e) {
    console.debug('获取推荐失败，将展示热门活动')
  }
}

onMounted(() => { 
  fetchBanners()
  fetchFeaturedActivities()
  fetchActivities()
  fetchRecommendations()
})
</script>

<style lang="scss" scoped>
// ================================================================
// 全局动画
// ================================================================
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(24px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.04); }
}

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

// ================================================================
// 页面容器
// ================================================================
.home-page {
  background: var(--app-bg);
  padding-bottom: 40px;

  &.is-mobile {
    padding: 0 0 20px;
  }
}

// ================================================================
// 英雄 Banner
// ================================================================
.hero-banner {
  position: relative;
  overflow: hidden;
}

.hero-carousel {
  // 正常文档流，el-carousel height prop 决定高度

  // 指示?- 白色圆点/胶囊
  :deep(.el-carousel__indicators) {
    z-index: 15;
    bottom: 16px;

    .el-carousel__button {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.5);
      opacity: 1;
    }

    .el-carousel__indicator.is-active .el-carousel__button {
      background: #fff;
      width: 20px;
      border-radius: 4px;
    }
  }

  // 箭头
  :deep(.el-carousel__arrow) {
    z-index: 15;
    background: rgba(255, 255, 255, 0.25);
    backdrop-filter: blur(8px);
    &:hover { background: rgba(255, 255, 255, 0.45); }
  }
}

.hero-slide {
  width: 100%;
  height: 100%;
  position: relative;

  .hero-bg {
    width: 100%;
    height: 100%;
    display: block;

    :deep(img) {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }
  }
}

// 每张 slide 自带渐变蒙层
.slide-gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to bottom,
    rgba(0, 60, 50, 0.3) 0%,
    rgba(0, 80, 70, 0.15) 50%,
    rgba(0, 0, 0, 0.35) 100%
  );
  pointer-events: none;
}

.hero-default {
  background: linear-gradient(135deg, #00BFA6 0%, #009688 40%, #00695C 100%);
}

.hero-fallback {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #00BFA6 0%, #009688 40%, #00695C 100%);
}

// 浮动文字?- 不阻挡轮播交?
.hero-overlay {
  position: absolute;
  inset: 0;
  z-index: 12;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none; // 让点击穿透到轮播?
}

.hero-content {
  text-align: center;
  color: #fff;
  animation: fadeInUp 0.8s ease-out;
  pointer-events: auto; // 恢复文字/按钮的点?

  .hero-title {
    font-size: 36px;
    font-weight: 800;
    margin: 0 0 10px;
    text-shadow: 0 2px 16px rgba(0, 0, 0, 0.3);
    letter-spacing: 2px;
  }

  .hero-subtitle {
    font-size: 16px;
    opacity: 0.9;
    margin: 0 0 24px;
    text-shadow: 0 1px 8px rgba(0, 0, 0, 0.2);
    letter-spacing: 4px;
  }
}

.hero-cta {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 32px;
  border: none;
  border-radius: 28px;
  background: #fff;
  color: #00BFA6;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  transition: all 0.3s;
  animation: pulse 3s ease-in-out infinite;

  &:hover {
    transform: scale(1.06);
    box-shadow: 0 8px 28px rgba(0, 191, 166, 0.35);
  }

  .el-icon { font-size: 18px; transition: transform 0.3s; }
  &:hover .el-icon { transform: translateX(4px); }
}

// --- 弧形分割 ---
.hero-curve {
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 100%;
  z-index: 20;
  line-height: 0;

  svg { width: 100%; height: 40px; }
}

// --- 移动端 Hero (CLEANED) ---

// --- 移动端金刚区 (CLEANED) ---

// ================================================================
// PC 悬浮渐变快捷卡片
// ================================================================
.pc-quick-section {
  max-width: 1440px;
  margin: -40px auto 0;
  padding: 0 24px;
  position: relative;
  z-index: 30;

  @media (min-width: 1200px) and (max-width: 1600px) { max-width: 90%; }
  @media (min-width: 1601px) { max-width: 1600px; }
}

.pc-quick-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.quick-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  animation: fadeInUp 0.5s ease-out both;

  &:hover {
    transform: translateY(-6px);
    box-shadow: 0 16px 40px rgba(0, 0, 0, 0.12);

    .qc-icon { transform: scale(1.08) rotate(5deg); }
    .qc-arrow { transform: translateX(4px); opacity: 1; }
  }

  .qc-icon {
    width: 56px;
    height: 56px;
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    flex-shrink: 0;
    transition: transform 0.3s;
  }

  .qc-text {
    flex: 1;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .qc-label {
    font-size: 17px;
    font-weight: 600;
    color: #333;
  }

  .qc-arrow {
    font-size: 18px;
    color: #ccc;
    opacity: 0;
    transition: all 0.3s;
  }
}

// ================================================================
// 热门活动?
// ================================================================
.activity-section {
  max-width: 1440px;
  margin: 40px auto 0;
  padding: 0 24px;

  @media (min-width: 1200px) and (max-width: 1600px) { max-width: 90%; }
  @media (min-width: 1601px) { max-width: 1600px; }

  &.section-mobile {
    margin-top: 10px;
    padding: 0;
  }
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;
  padding: 0 4px;

  .section-title-area {
    .section-title {
      display: block;
      -webkit-line-clamp: 2;
      line-clamp: 2;
      -webkit-box-orient: vertical;
      font-size: 24px;
      font-weight: 700;
      color: #333;
      margin-bottom: 4px;
    }

    .section-subtitle {
      font-size: 14px;
      color: #999;
    }
  }

  .section-actions {
    display: flex;
    align-items: center;
    gap: 12px;

    // 筛选下拉框 - 和谐统一版设计 (Pill shape)
    .custom-filter-select {
      width: 110px;

      :deep(.el-input__wrapper),
      :deep(.el-select__wrapper) {
        height: 42px !important;
        min-height: 42px !important;
        line-height: 42px !important;
        padding: 0 16px !important;
        box-shadow: 0 0 0 1.5px #DCDFE6 inset !important;
        border-radius: 50px !important; 
        background-color: #fff !important;
        transition: all 0.28s cubic-bezier(0.4, 0, 0.2, 1);
        display: flex !important;
        align-items: center !important;

        &:hover, &.is-focus {
          box-shadow: 0 0 0 1.5px var(--primary-color, #00BFA6) inset, 0 4px 12px rgba(0, 191, 166, 0.15) !important;
        }

        .el-select__selection {
          height: 100% !important;
          display: flex !important;
          align-items: center !important;
        }
      }

      :deep(.el-input__inner),
      :deep(.el-select__placeholder),
      :deep(.el-select__selected-item),
      :deep(.el-select__selection-item),
      :deep(span) {
        color: #000 !important; 
        -webkit-text-fill-color: #000 !important;
        font-weight: 600 !important;
        font-size: 14px !important;
        opacity: 1 !important;
        visibility: visible !important;
        display: inline-block !important;
        line-height: 1 !important;
      }

      // 下拉箭头图标
      :deep(.el-input__suffix .el-icon),
      :deep(.el-select__caret),
      :deep(.el-icon) {
        color: #999 !important;
        font-size: 13px !important;
        opacity: 1 !important;
      }
    }

    // "查看更多" - 白底主题色边框按钮
    .view-more-btn {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 4px;
      height: 42px;
      padding: 0 28px;
      border: 1.5px solid var(--primary-color, #00BFA6);
      border-radius: 50px;
      background: #fff;
      color: var(--primary-color, #00BFA6);
      font-size: 14px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.28s cubic-bezier(0.4, 0, 0.2, 1);
      white-space: nowrap;
      letter-spacing: 0.5px;

      .el-icon { font-size: 14px; transition: transform 0.28s; }

      &:hover {
        background: var(--primary-color, #00BFA6);
        color: #fff;
        box-shadow: 0 4px 14px rgba(0, 191, 166, 0.25);
        border-color: transparent;
        .el-icon { transform: translateX(3px); }
      }

      &:active {
        transform: scale(0.98);
        box-shadow: 0 2px 8px rgba(0, 191, 166, 0.15);
      }
    }
  }
}

// --- 推荐标签样式 ---
.recommend-card-wrapper {
  position: relative;
  
  .algo-tag-float {
    position: absolute;
    top: 12px;
    right: 12px;
    z-index: 10;
    
    .tag-content {
      background: rgba(0, 191, 166, 0.9);
      backdrop-filter: blur(4px);
      color: #fff;
      padding: 4px 10px;
      border-radius: 20px;
      font-size: 11px;
      font-weight: 600;
      display: flex;
      align-items: center;
      gap: 4px;
      box-shadow: 0 4px 12px rgba(0, 191, 166, 0.3);
      border: 1px solid rgba(255, 255, 255, 0.2);
      cursor: help;
    }
  }
}

.recommend-item-wrapper {
  position: relative;
  margin-bottom: 4px;

  .algo-badge-mini {
    position: absolute;
    top: 10px;
    left: 10px;
    z-index: 5;
    background: var(--primary-color);
    color: #fff;
    padding: 2px 8px;
    border-radius: 4px;
    font-size: 10px;
    font-weight: 600;
    box-shadow: 0 2px 6px rgba(0, 191, 166, 0.2);
  }
}

// --- 骨架屏 ---
.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.skeleton-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}

// --- PC 活动网格 ---
.pc-activity-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;

  .grid-item {
    animation: fadeInUp 0.4s ease-out both;

    @for $i from 1 through 9 {
      &:nth-child(#{$i}) { animation-delay: #{$i * 0.05}s; }
    }
  }
}

// --- TransitionGroup ---
.card-fade-enter-active { transition: all 0.4s ease; }
.card-fade-leave-active { transition: all 0.2s ease; }
.card-fade-enter-from { opacity: 0; transform: translateY(20px); }
.card-fade-leave-to { opacity: 0; transform: scale(0.95); }
.card-fade-move { transition: transform 0.4s ease; }

// --- 移动端信息流 ---
.mobile-activity-list {
  display: flex;
  flex-direction: column;
  background: #fff;
}

</style>
