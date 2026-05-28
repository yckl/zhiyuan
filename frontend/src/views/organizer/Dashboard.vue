<template>
    <!-- ================== 1. PC 端 (Bento Grid Flagship UI) ================== -->
    <div v-if="!isMobile" class="pc-bento-layout">
      <!-- TopRow: Stats Bento -->
      <div class="stats-bento-row">
        <div v-for="item in stats" :key="item.title" class="bento-card stat-bento">
          <div class="stat-content">
            <div class="stat-label">{{ item.title }}</div>
            <div class="stat-main">
              <span class="stat-value inter-font">{{ item.value }}</span>
              <div class="sparkline-box">
                <!-- 极简趋势图 (Sparkline) -->
                <svg viewBox="0 0 100 30" class="sparkline">
                  <path 
                    :d="generateSparkline(item.trend > 0)" 
                    :class="item.trend > 0 ? 'path-up' : 'path-down'" 
                  />
                </svg>
                <span :class="['trend-tag', item.trend > 0 ? 'up' : 'down']">
                  {{ item.trend > 0 ? '+' : '' }}{{ item.trend }}%
                </span>
              </div>
            </div>
          </div>
          <div class="stat-icon-bg">
            <el-icon><component :is="item.icon" /></el-icon>
          </div>
        </div>
      </div>

      <!-- QuickActions Bento -->
      <div class="quick-actions-bento">
        <div 
          v-for="action in quickActions" 
          :key="action.text" 
          class="bento-card action-card"
          @click="handleAction(action.path)"
        >
          <div class="action-icon-wrap">
            <el-icon><component :is="action.icon" /></el-icon>
          </div>
          <div class="action-text">
            <span class="label">{{ action.text }}</span>
            <span class="desc">立即进入快捷通道</span>
          </div>
        </div>
      </div>

      <!-- MainRow: Activity & Reviews -->
      <div class="main-bento-grid">
        <!-- 进行中的活动 (Large Bento) -->
        <div class="bento-card activity-bento">
          <div class="bento-header">
            <div class="title-group">
              <h3>进行中的活动</h3>
              <p class="subtitle">实时监控活动招募与服务进步</p>
            </div>
            <el-button link class="premium-link" @click="handleAction('/organizer/activity/list')">
              查看全部 <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>

          <div class="activity-feed">
            <div v-for="act in activeActivities" :key="act.id" class="activity-row-premium" @click="handleAction(`/organizer/activity/edit/${act.id}`)">
              <div class="act-thumb">
                <el-image :src="act.coverImage || '/default-cover.jpg'" fit="cover" class="act-img-el">
                  <template #error>
                    <div class="img-error-slot"><el-icon><Picture /></el-icon></div>
                  </template>
                </el-image>
                <div class="status-overlay">{{ getStatusLabel(act.status) }}</div>
              </div>
              <div class="act-details">
                <h4 class="act-title">{{ act.title }}</h4>
                <div class="prog-container">
                  <div class="prog-info">
                    <span class="label">招募进度</span>
                    <span class="val">{{ act.current }} / {{ act.total }} 题</span>
                  </div>
                  <div class="premium-progress-bar">
                    <div class="fill" :style="{ width: getProgress(act) + '%' }"></div>
                  </div>
                </div>
              </div>
              <div class="act-actions-hover">
                <button class="manage-pill">管理详情</button>
              </div>
            </div>
            <el-empty v-if="activeActivities.length === 0" description="暂无活跃活动" :image-size="80" />
          </div>
        </div>

        <!-- 最新评价 (Tall Bento) -->
        <div class="bento-card review-bento">
          <div class="bento-header">
            <h3>最新评价</h3>
          </div>
          <div class="review-bubbles">
            <div v-for="review in latestReviews" :key="review.id" class="bubble-item">
              <div class="bubble-user">
                <el-avatar :size="28" :src="review.avatar" />
                <span class="name">{{ review.nickname }}</span>
                <div class="stars">
                  <el-icon v-for="i in 5" :key="i" :class="{ active: i <= review.rating }"><StarFilled /></el-icon>
                </div>
              </div>
              <div class="bubble-content">
                {{ review.content }}
                <div class="bubble-tail"></div>
              </div>
            </div>
            <div v-if="latestReviews.length === 0" class="empty-3d-placeholder">
              <div class="illustration-3d">💬</div>
              <p>暂无评价数据</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ================== 2. 移动端 (iOS Premium - 保持之前重构的高品质) ================== -->
    <div v-else class="m-dashboard">
      <!-- 顶部：欢迎区域 (纯净清新排版) -->
      <div class="mobile-welcome-section">
        <div class="welcome-brand-content">
          <div class="brand-left">
            <div class="brand-avatar-wrapper">
              <div class="mascot-box">
                <!-- Using a cute CSS mascot to match the blue theme -->
                <div class="mascot-face-blue">
                   <div class="eyes">^^</div>
                   <div class="mouth">m</div>
                </div>
              </div>
            </div>
          </div>
          <div class="brand-right">
            <div class="greeting-white-text">{{ greetingText }} 组织者</div>
            <div class="identity-badge">
              <span class="badge-text">项目负责人</span>
            </div>
          </div>
        </div>
        <div class="status-subtitle-white">
          今天有 {{ pendingCount }} 项待处理任务
        </div>
      </div>

      <!-- 统计数据矩阵 (悬浮于Header 之上) -->
      <div class="m-stats-container">
        <div class="matrix-grid">
            <div v-for="item in stats" :key="item.title" class="glass-matrix-card">
              <div class="card-top">
                <div class="matrix-icon-box" :style="{ background: getIconBg(item.color) }">
                  <el-icon :style="{ color: item.color }"><component :is="item.icon" /></el-icon>
                </div>
                <div :class="['matrix-trend', item.trend > 0 ? 'up' : 'down']">
                  {{ item.trend > 0 ? '+' : '-' }} {{ Math.abs(item.trend) }}%
                </div>
              </div>
              <div class="matrix-data">
                <div class="value-row">
                  <span class="matrix-value DIN-font">{{ item.value }}</span>
                  <span class="matrix-label">{{ item.title }}</span>
                </div>
              </div>
          </div>
      </div>

      <!-- 金刚区 -->
      <div class="m-portal-section">
        <div class="portal-grid">
          <div
            v-for="action in mobileActions"
            :key="action.text"
            class="portal-item"
            @click="handleAction(action.path)"
          >
            <div class="squircle-icon" :style="{ background: action.gradient || getActionGradient(action) }">
              <el-icon><component :is="action.icon" /></el-icon>
            </div>
            <span class="portal-text">{{ action.text }}</span>
          </div>
        </div>
      </div>

      <!-- 列表区 -->
      <div class="m-list-section">
        <div class="list-header">
          <h3 class="list-title">进行中的活动</h3>
          <span class="list-count" v-if="activeActivities.length">活跃 ({{ activeActivities.length }})</span>
        </div>
        
        <div class="card-stream">
          <div
            v-for="act in activeActivities"
            :key="act.id"
            class="stream-card"
            @click="handleAction(`/organizer/activity/edit/${act.id}`)"
          >
            <div class="card-left-accent" :style="{ background: getStatusColor(act.status) }"></div>
            <div class="card-main">
              <div class="card-top-info">
                <span class="card-tag">{{ act.status }}</span>
                <span class="card-time">进行中</span>
              </div>
              <h4 class="card-title">{{ act.title }}</h4>
              <div class="card-progress-box">
                <div class="prog-meta">
                  <span class="prog-label">招募进度</span>
                  <span class="prog-val">{{ act.current }}/{{ act.total }} 题</span>
                </div>
                <div class="premium-progress">
                  <div class="prog-bar-bg">
                    <div class="prog-bar-fill" :style="{ width: getProgress(act) + '%', background: getStatusColor(act.status) }"></div>
                  </div>
                </div>
              </div>
            </div>
            <div class="card-arrow">
              <el-icon><ArrowRight /></el-icon>
            </div>
          </div>
          <el-empty v-if="activeActivities.length === 0" description="暂无进行中的活动" :image-size="80" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Promotion, Timer, User, Star, Plus, FullScreen,
  Checked, Document, Setting, ChatDotRound, Bell, Notebook, ArrowRight, StarFilled
} from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import { useMobile } from '@/composables/useMobile'

const router = useRouter()
const loading = ref(false)
const { isMobile } = useMobile()

const stats = ref([
  { title: '进行中活动', value: 0, icon: Promotion, trend: 12, key: 'activeActivities', color: '#0093E9' },
  { title: '累计服务人次', value: 0, icon: Timer, trend: 8, key: 'totalServiceCount', color: '#80D0C7' },
  { title: '待审核报名', value: 0, icon: User, trend: -3, key: 'pendingAudit', color: '#FBBF24' },
  { title: '综合评分', value: 5.0, icon: Star, trend: 1, key: 'avgRating', color: '#FF3B30' }
])

const pendingCount = computed(() => {
  const item = stats.value.find(s => s.key === 'pendingAudit')
  return item ? item.value : 0
})

const getIconBg = (color: string) => {
  // Return color with 0.15 opacity for better saturation as requested
  if (color.startsWith('#')) {
    const r = parseInt(color.slice(1, 3), 16)
    const g = parseInt(color.slice(3, 5), 16)
    const b = parseInt(color.slice(5, 7), 16)
    return `rgba(${r}, ${g}, ${b}, 0.15)`
  }
  return 'rgba(0, 147, 233, 0.15)'
}

const quickActions = [
  { text: '发布活动', icon: Plus, path: '/organizer/activity/create' },
  { text: '扫码签到', icon: FullScreen, path: '/organizer/personnel/checkin' },
  { text: '人员审核', icon: Checked, path: '/organizer/personnel/audit' },
  { text: '发送通知', icon: Document, path: '/organizer/notification/send' }
]

const mobileActions = [
  { text: '发布活动', icon: Plus, path: '/organizer/activity/create', gradient: 'linear-gradient(135deg, #60A5FA, #3B82F6)' },
  { text: '扫码签到', icon: FullScreen, path: '/organizer/personnel/checkin', gradient: 'linear-gradient(135deg, #34D399, #10B981)' },
  { text: '人员审核', icon: Checked, path: '/organizer/personnel/audit', gradient: 'linear-gradient(135deg, #FBBF24, #F59E0B)' },
  { text: '发送通知', icon: Bell, path: '/organizer/notification/send', gradient: 'linear-gradient(135deg, #F87171, #EF4444)' },
  { text: '评价管理', icon: ChatDotRound, path: '/organizer/feedback/reviews', gradient: 'linear-gradient(135deg, #818CF8, #6366F1)' },
  { text: '组织设置', icon: Setting, path: '/organizer/setting', gradient: 'linear-gradient(135deg, #94A3B8, #475569)' },
  { text: '工时录入', icon: Timer, path: '/organizer/feedback/hours', gradient: 'linear-gradient(135deg, #2DD4BF, #0D9488)' },
  { text: '通知记录', icon: Notebook, path: '/organizer/notification/history', gradient: 'linear-gradient(135deg, #FACC15, #CA8A04)' }
]

const activeActivities = ref<any[]>([])
const latestReviews = ref<any[]>([])

const greetingText = computed(() => {
  const h = new Date().getHours()
  if (h < 12) return '早上好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const handleAction = (path: string) => router.push(path)

const getStatusLabel = (status: any) => {
  if (typeof status === 'string') return status
  const m: Record<number, string> = { 0: '草稿', 1: '待审核', 2: '招募中', 3: '进行中', 4: '已结束', 5: '已取消' }
  return m[status] || '未知'
}

const getStatusColor = (status: any) => {
  const label = getStatusLabel(status)
  if (label === '进行中') return '#0093E9'
  if (label === '招募中') return '#34C759'
  return '#FF9500'
}

const getActionGradient = (action: any) => action.bg || '#f1f5f9'

const getProgress = (act: any) => {
  if (!act.total || act.total === 0) return 30 // Mock defaults
  return Math.min(Math.round((act.current / act.total) * 100), 100)
}

const generateSparkline = (isUp: boolean) => {
  if (isUp) return 'M0,25 C10,22 20,28 30,15 C40,2 50,18 60,12 C70,6 80,18 90,5 C95,2 100,5 100,5'
  return 'M0,5 C10,8 20,2 30,15 C40,28 50,12 60,18 C70,24 80,12 90,25 C95,28 100,25 100,25'
}

const fetchData = async () => {
  loading.value = true
  try {
    const [coreRes, activitiesRes, reviewsRes] = await Promise.all([
      request.get('/organizer/statistics/core'),
      request.get('/organizer/statistics/activities/active'),
      request.get('/organizer/statistics/reviews/latest')
    ])
    if (coreRes.data) {
      stats.value.forEach(item => {
        if (coreRes.data[item.key] !== undefined) item.value = coreRes.data[item.key]
      })
    }
    // [Debug] Log activity data to verify image field name
    console.log('Dashboard Activities:', activitiesRes.data)
    activeActivities.value = activitiesRes.data || []
    latestReviews.value = reviewsRes.data || []
  } catch (e) {
    console.error('获取数据失败:', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.org-dashboard {
  min-height: 100vh;
  background-color: #F0F2F5;
  background-image: 
    radial-gradient(circle at 0% 0%, rgba(0, 147, 233, 0.05) 0%, transparent 50%),
    radial-gradient(circle at 100% 100%, rgba(128, 208, 199, 0.05) 0%, transparent 50%);
  padding-bottom: 40px;
}

/* ================== PC Bento Grid ================== */
.pc-bento-layout {
  max-width: 1440px;
  margin: 0 auto;
  padding: 32px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.bento-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.03);
  transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
  overflow: hidden;

  &:hover {
    transform: translateY(-6px);
    box-shadow: 0 12px 48px rgba(0, 147, 233, 0.1);
    background: rgba(255, 255, 255, 0.85);
  }
}

/* 1. Stats Row */
.stats-bento-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-bento {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;

  .stat-label { font-size: 14px; color: #64748b; font-weight: 600; margin-bottom: 8px; }
  .stat-value { font-size: 32px; font-weight: 800; color: #1e293b; letter-spacing: -1px; display: block; }
  
  .sparkline-box {
    display: flex; align-items: center; gap: 8px; margin-top: 4px;
    .sparkline { width: 60px; height: 18px; fill: none; stroke-width: 2.5; stroke-linecap: round; }
    .path-up { stroke: #34C759; }
    .path-down { stroke: #FF3B30; }
    .trend-tag { 
      font-size: 10px; font-weight: 800; padding: 2px 6px; border-radius: 100px; 
      &.up { color: #34C759; background: rgba(52, 199, 89, 0.1); }
      &.down { color: #FF3B30; background: rgba(255, 59, 48, 0.1); }
    }
  }

  .stat-icon-bg {
    font-size: 40px; color: #0093E9; opacity: 0.1; position: absolute; right: -10px; bottom: -10px;
  }
}

/* 2. Main Grid (Bento Mix) */
.main-bento-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
}

.bento-header {
  display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px;
  h3 { font-size: 20px; font-weight: 800; color: #1e293b; margin: 0; }
  .subtitle { font-size: 12px; color: #94a3b8; margin: 4px 0 0; }
  .premium-link { color: #0093E9; font-weight: 700; font-size: 14px; }
}

/* Activity Component */
.activity-feed {
  display: flex; flex-direction: column; gap: 16px;
}

.activity-row-premium {
  display: flex; align-items: center; gap: 20px;
  padding: 16px; border-radius: 16px;
  background: rgba(255,255,255,0.4);
  cursor: pointer; transition: all 0.3s;
  position: relative;

  &:hover {
    background: #fff;
    .act-actions-hover { opacity: 1; transform: translateX(0); }
  }

  .act-thumb {
    width: 80px; height: 60px; border-radius: 12px; overflow: hidden; position: relative;
    background: #f1f5f9;

    .act-img-el { width: 100%; height: 100%; transition: transform 0.5s; }
    .img-error-slot { 
      width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; 
      background: #e2e8f0; color: #94a3b8; font-size: 20px;
    }

    .status-overlay { 
      position: absolute; inset: 0; background: rgba(0,0,0,0.3); color: #fff; 
      font-size: 10px; display: flex; align-items: center; justify-content: center; 
      font-weight: 700; z-index: 2; pointer-events: none;
    }
  }

  &:hover .act-img-el { transform: scale(1.1); }

  .act-details {
    flex: 1; min-width: 0;
    .act-title { font-size: 16px; font-weight: 700; color: #1e293b; margin: 0 0 10px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
  }

  .prog-container {
    .prog-info { display: flex; justify-content: space-between; font-size: 11px; margin-bottom: 6px; .label { color: #94a3b8; } .val { color: #1e293b; font-weight: 700; } }
    .premium-progress-bar {
      height: 6px; background: #E2E8F0; border-radius: 10px; overflow: hidden;
      .fill { height: 100%; background: linear-gradient(90deg, #0093E9, #80D0C7); transition: width 1s; border-radius: 10px; }
    }
  }

  .act-actions-hover {
    opacity: 0; transform: translateX(10px); transition: all 0.3s;
    .manage-pill { background: #1e293b; color: #fff; border: none; padding: 6px 14px; border-radius: 100px; font-size: 12px; font-weight: 600; cursor: pointer; }
  }
}

/* Review Component (Bubbles) */
.review-bubbles {
  display: flex; flex-direction: column; gap: 24px;
}

.bubble-item {
  .bubble-user {
    display: flex; align-items: center; gap: 10px; margin-bottom: 8px;
    .name { font-size: 13px; font-weight: 700; color: #334155; }
    .stars { display: flex; gap: 2px; .el-icon { font-size: 12px; color: #cbd5e1; &.active { color: #FFD700; } } }
  }
  .bubble-content {
    background: #fff; padding: 12px 16px; border-radius: 4px 16px 16px 16px;
    font-size: 13px; color: #475569; border: 1px solid rgba(0,0,0,0.02);
    box-shadow: 0 2px 8px rgba(0,0,0,0.02); position: relative;
    line-height: 1.5;

    .bubble-tail {
      position: absolute; left: -8px; top: 0; 
      width: 0; height: 0; border-style: solid; border-width: 0 8px 8px 0;
      border-color: transparent #fff transparent transparent;
    }
  }
}

.empty-3d-placeholder {
  text-align: center; padding: 40px 0;
  .illustration-3d { font-size: 48px; margin-bottom: 12px; filter: drop-shadow(0 10px 10px rgba(0,0,0,0.1)); }
  p { font-size: 14px; color: #94a3b8; }
}

/* 3. Quick Actions row */
.quick-actions-bento {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px;
}

.action-card {
  display: flex; align-items: center; gap: 16px; cursor: pointer;
  
  .action-icon-wrap {
    width: 48px; height: 48px; border-radius: 14px; 
    background: linear-gradient(135deg, rgba(0, 147, 233, 0.1), rgba(128, 208, 199, 0.1));
    display: flex; align-items: center; justify-content: center;
    color: #0093E9; font-size: 24px; transition: all 0.3s;
  }

  &:hover .action-icon-wrap {
    background: linear-gradient(135deg, #0093E9, #80D0C7); color: #fff; transform: rotate(8deg);
  }

  .action-text {
    display: flex; flex-direction: column;
    .label { font-size: 15px; font-weight: 700; color: #1e293b; }
    .desc { font-size: 11px; color: #94a3b8; }
  }
}

/* ================== Mobile styles (iOS Theme) ================== */
.m-dashboard {
  background: #F2F2F7; padding-bottom: 120px;

  .mobile-welcome-section {
    padding: 32px 20px 80px;
    background: linear-gradient(135deg, #0093E9 0%, #80D0C7 100%);
    position: relative;
    overflow: hidden;
    border-radius: 0 0 32px 32px;

    &::before {
      content: '';
      position: absolute;
      right: -10%;
      top: -10%;
      width: 150px;
      height: 150px;
      background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
      pointer-events: none;
    }
    
    .welcome-brand-content {
      display: flex;
      align-items: center;
      gap: 16px;
      position: relative;
      z-index: 2;
    }

    .brand-avatar-wrapper {
      width: 64px;
      height: 64px;
      background: #FFF;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15);
      
      .mascot-box {
        width: 44px;
        height: 44px;
        background: #0093E9;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        
        .mascot-face-blue {
          color: #FFF;
          font-family: inherit;
          text-align: center;
          font-weight: 800;
          .eyes { font-size: 16px; line-height: 1; }
          .mouth { font-size: 14px; line-height: 1; margin-top: -2px; }
        }
      }
    }

    .greeting-white-text {
      font-size: 24px;
      font-weight: 800;
      color: #FFF;
      margin-bottom: 6px;
      letter-spacing: 0.5px;
      text-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    .identity-badge {
      display: inline-flex;
      background: rgba(255, 255, 255, 0.2);
      backdrop-filter: blur(8px);
      -webkit-backdrop-filter: blur(8px);
      padding: 4px 12px;
      border-radius: 100px;
      border: 1.5px solid rgba(255, 255, 255, 0.3);
      
      .badge-text {
        font-size: 12px;
        color: #FFF;
        font-weight: 700;
      }
    }
    
    .status-subtitle-white {
      margin-top: 16px;
      font-size: 13px;
      color: rgba(255, 255, 255, 0.85);
      font-weight: 500;
      padding-left: 2px;
      position: relative;
      z-index: 2;
    }
  }

  .m-stats-container {
    margin-top: -48px; // 增强层叠感
    padding: 0 20px;
    position: relative;
    z-index: 10;
  }

  .matrix-grid {
    display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px;
    .glass-matrix-card {
      background: rgba(255,255,255,0.8); backdrop-filter: blur(20px); border-radius: 20px; padding: 16px;
      border: 1px solid rgba(255,255,255,0.4); display: flex; flex-direction: column; gap: 14px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.02);
      
      .card-top { display: flex; justify-content: space-between; align-items: center; }
      .matrix-icon-box { 
        width: 36px; height: 36px; // 略微调大
        background: #fff; border-radius: 12px; // 调大圆角 (和卡片呼应)
        display: flex; align-items: center; justify-content: center; 
        color: #0093E9;
        transition: transform 0.3s ease;
      }
      .matrix-trend { 
        font-size: 10px; font-weight: 800; padding: 2px 8px; border-radius: 8px; 
        box-shadow: 0 2px 6px rgba(0,0,0,0.05); // 增加微阴影
        &.up { color: #15803d; background: rgba(52,199,89,0.12); } // 更深的绿色
        &.down { color: #b91c1c; background: rgba(255,59,48,0.12); } // 更深的红色
      }
      .matrix-data {
        .value-row {
          display: flex;
          align-items: center; // 水平居中对齐重心
          gap: 8px; // 固定 8px 间距
        }
      }
      .matrix-value { font-size: 26px; font-weight: 800; color: #1C1C1E; line-height: 1; }
      .matrix-label { font-size: 12px; color: #8E8E93; font-weight: 600; }
    }
  }

  .m-portal-section { padding: 30px 16px 0; .portal-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; } }
  .portal-item {
    display: flex; flex-direction: column; align-items: center; gap: 8px;
    .squircle-icon { 
      width: 54px; height: 54px; border-radius: 16px; display: flex; align-items: center; justify-content: center; 
      color: #fff; font-size: 24px; box-shadow: 0 4px 16px rgba(0,0,0,0.08); transition: transform 0.2s;
    }
    &:active .squircle-icon { transform: scale(0.9); }
    .portal-text { font-size: 11px; font-weight: 600; color: #1C1C1E; }
  }

  .m-list-section {
    padding: 30px 16px 0;
    .list-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; h3 { font-size: 18px; font-weight: 800; } }
    .card-stream { display: flex; flex-direction: column; gap: 12px; }
  }

  .stream-card {
    background: #fff; border-radius: 20px; padding: 16px; display: flex; align-items: center; gap: 12px;
    &:active { transform: scale(0.97); }
    .card-left-accent { width: 4px; height: 40px; border-radius: 4px; }
    .card-main { flex: 1; min-width: 0; }
    .card-title { font-size: 15px; font-weight: 700; margin: 4px 0 8px; }
    .premium-progress { height: 6px; background: #F2F2F7; border-radius: 10px; overflow: hidden; .prog-bar-fill { height: 100%; transition: width 0.5s; } }
  }
}

.inter-font { font-family: 'Inter', system-ui, -apple-system, sans-serif; }
</style>
