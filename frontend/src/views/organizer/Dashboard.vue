<template>
  <div class="org-dashboard" v-loading="loading" element-loading-text="加载中...">
    <!-- 1. 顶部数据卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col v-for="item in stats" :key="item.title" :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-content">
            <div class="stats-icon" :style="{ backgroundColor: item.bg }">
              <el-icon :color="item.color" :size="24">
                <component :is="item.icon" />
              </el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-title">{{ item.title }}</div>
              <div class="stats-value">{{ item.value }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 2. 快捷入口 -->
    <el-card shadow="hover" class="quick-actions-card mb-20">
      <template #header>
        <div class="card-header">
          <span class="title-with-icon">
            <el-icon><Pointer /></el-icon> 快捷访问
          </span>
        </div>
      </template>
      <div class="quick-actions">
        <el-button 
          v-for="action in quickActions" 
          :key="action.text" 
          :type="action.type" 
          plain 
          class="action-btn"
          @click="handleAction(action.path)"
        >
          <el-icon><component :is="action.icon" /></el-icon>
          <span>{{ action.text }}</span>
        </el-button>
      </div>
    </el-card>

    <el-row :gutter="20">
      <!-- 3. 进行中的活动 -->
      <el-col :xs="24" :md="16">
        <el-card shadow="hover" class="active-activities-card">
          <template #header>
            <div class="card-header">
              <span class="title-with-icon">
                <el-icon><List /></el-icon> 进行中的活动
              </span>
              <el-button link type="primary" @click="handleAction('/organizer/activity/list')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="activeActivities" style="width: 100%">
            <el-table-column prop="title" label="活动名称" min-width="150" show-overflow-tooltip />
            <el-table-column label="报名进度" width="180">
              <template #default="{ row }">
                <div class="progress-wrap">
                  <el-progress 
                    :percentage="Math.round((row.current / row.total) * 100)" 
                    :status="row.current >= row.total ? 'success' : ''"
                  />
                  <span class="progress-text">{{ row.current }}/{{ row.total }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.statusType">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="120" />
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="handleAction(`/organizer/activity/edit/${row.id}`)">管理</el-button>
                <el-button link type="success" size="small" @click="handleAction('/organizer/personnel/checkin')">签到</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!loading && activeActivities.length === 0" description="暂无进行中的活动" :image-size="80" />
        </el-card>
      </el-col>

      <!-- 4. 最新评价 -->
      <el-col :xs="24" :md="8">
        <el-card shadow="hover" class="reviews-card">
          <template #header>
            <div class="card-header">
              <span class="title-with-icon">
                <el-icon><ChatDotSquare /></el-icon> 最新评价
              </span>
            </div>
          </template>
          <div class="reviews-list">
            <template v-if="latestReviews.length > 0">
              <div v-for="review in latestReviews" :key="review.id" class="review-item">
                <div class="review-header">
                  <el-avatar :size="32" :src="review.avatar || '/default-avatar.png'">
                    <el-icon><User /></el-icon>
                  </el-avatar>
                <div class="review-user">
                  <span class="nickname">{{ review.nickname }}</span>
                  <el-rate v-model="review.rating" disabled size="small" />
                </div>
                <span class="review-time">{{ review.time }}</span>
              </div>
              <p class="review-content">{{ review.content }}</p>
            </div>
            </template>
            <el-empty v-else description="暂无评价" :image-size="60" />
            <div class="more-reviews" v-if="latestReviews.length > 0">
              <el-link type="primary" :underline="false" @click="handleAction('/organizer/feedback/reviews')">
                查看全部评价 <el-icon><ArrowRight /></el-icon>
              </el-link>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  User, Flag, DataLine, Star, Plus, FullScreen, Bell, Setting, 
  Pointer, List, ChatDotSquare, ArrowRight 
} from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)

interface ActiveActivity {
  id: number
  title: string
  current: number
  total: number
  status: string
  statusType: string
  createTime: string
}

interface Review {
  id: number
  nickname: string
  avatar: string
  rating: number
  content: string
  time: string
}

// 1. 统计数据
const stats = ref([
  { title: '待办审核', value: 0, icon: User, color: '#E6A23C', bg: '#fdf6ec', key: 'pendingAudit' },
  { title: '进行中活动', value: 0, icon: Flag, color: '#409EFF', bg: '#ecf5ff', key: 'activeActivities' },
  { title: '累计服务人次', value: 0, icon: DataLine, color: '#67C23A', bg: '#f0f9eb', key: 'totalServiceCount' },
  { title: '综合评分', value: 0, icon: Star, color: '#fa8c16', bg: '#fff7e6', key: 'avgRating' }
])

// 2. 快捷动作
const quickActions = [
  { text: '发布活动', icon: Plus, type: 'primary', path: '/organizer/activity/create' },
  { text: '现场签到', icon: FullScreen, type: 'success', path: '/organizer/personnel/checkin' },
  { text: '发送通知', icon: Bell, type: 'warning', path: '/organizer/notification/send' },
  { text: '账号设置', icon: Setting, type: 'info', path: '/organizer/setting' }
]

const handleAction = (path: string) => {
  router.push(path)
}

// 3. 活动数据
const activeActivities = ref<ActiveActivity[]>([])

// 4. 评价数据
const latestReviews = ref<Review[]>([])

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    // 聚合请求
    const [coreRes, activitiesRes, reviewsRes] = await Promise.all([
      request.get('/organizer/statistics/core'),
      request.get('/organizer/statistics/activities/active'),
      request.get('/organizer/statistics/reviews/latest')
    ])

    // 更新核心指标
    if (coreRes.data) {
      stats.value.forEach(item => {
        if (coreRes.data[item.key] !== undefined) {
          item.value = coreRes.data[item.key]
        }
      })
    }

    // 更新活动列表
    if (activitiesRes.data) {
      activeActivities.value = activitiesRes.data
    }

    // 更新评价列表
    if (reviewsRes.data) {
      latestReviews.value = reviewsRes.data
    }
  } catch (error) {
    console.error('获取工作台数据失败:', error)
    ElMessage.error('获取工作台数据失败，请稍后重试')
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
  padding: 10px;

  .stats-row {
    margin-bottom: 20px;
  }

  .stats-card {
    border: none;
    border-radius: 12px;
    
    .stats-content {
      display: flex;
      align-items: center;
      gap: 15px;
    }

    .stats-icon {
      width: 48px;
      height: 48px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .stats-info {
      .stats-title {
        font-size: 13px;
        color: #909399;
        margin-bottom: 4px;
      }
      .stats-value {
        font-size: 20px;
        font-weight: bold;
        color: var(--el-text-color-primary);
      }
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .title-with-icon {
      display: flex;
      align-items: center;
      gap: 6px;
      font-weight: bold;
      color: var(--el-text-color-primary);
    }
  }

  .quick-actions {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    
    .action-btn {
      height: 40px;
      padding: 0 20px;
      border-radius: 8px;
      
      .el-icon {
        margin-right: 6px;
      }
    }
  }

  .progress-wrap {
    display: flex;
    flex-direction: column;
    gap: 4px;
    
    .progress-text {
      font-size: 12px;
      color: #909399;
      text-align: right;
    }
  }

  .reviews-list {
    .review-item {
      padding: 12px 0;
      border-bottom: 1px solid var(--el-border-color-lighter);
      
      &:last-child {
        border-bottom: none;
      }

      .review-header {
        display: flex;
        align-items: flex-start;
        gap: 10px;
        margin-bottom: 8px;

        .review-user {
          flex: 1;
          display: flex;
          flex-direction: column;
          
          .nickname {
            font-size: 13px;
            font-weight: bold;
            margin-bottom: 2px;
          }
        }

        .review-time {
          font-size: 12px;
          color: #909399;
        }
      }

      .review-content {
        font-size: 13px;
        color: var(--el-text-color-regular);
        line-height: 1.6;
        margin: 0;
        padding-left: 42px;
      }
    }

    .more-reviews {
      text-align: center;
      margin-top: 15px;
      
      .el-link {
        font-size: 13px;
      }
    }
  }
}

// 移动端适配
@media (max-width: 768px) {
  .stats-card {
    margin-bottom: 10px;
  }
  
  .quick-actions {
    .action-btn {
      flex: 1 1 calc(50% - 12px);
    }
  }
}
</style>
