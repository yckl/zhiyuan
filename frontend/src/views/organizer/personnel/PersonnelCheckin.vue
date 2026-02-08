<template>
  <div class="org-checkin-page">
    <!-- 活动选择器 -->
    <el-card v-if="!activityId" class="selector-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon :size="20"><VideoCamera /></el-icon>
          <span class="title">选择活动进行现场签到</span>
        </div>
      </template>
      <el-table :data="activities" v-loading="activitiesLoading" stripe>
        <el-table-column prop="title" label="活动名称" min-width="200" />
        <el-table-column label="开展时间" width="180">
          <template #default="{ row }">
            <div class="time-cell">
              <el-icon><Clock /></el-icon>
              {{ formatDate(row.startTime) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button type="success" @click="selectActivity(row.id)">
              <el-icon><VideoPlay /></el-icon> 开始
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 签到主界面 - 左右分栏 -->
    <template v-else>
      <el-page-header @back="exitCheckin" class="mb-20">
        <template #content>
          <span class="page-title">{{ activity.title }} - 现场签到</span>
        </template>
        <template #extra>
          <el-tag type="success" size="large" effect="dark">
            <el-icon><Timer /></el-icon> 自动刷新中
          </el-tag>
        </template>
      </el-page-header>

      <el-row :gutter="24">
        <!-- 左侧：扫码展示区 -->
        <el-col :span="10">
          <el-card class="qr-card" shadow="hover">
            <div class="qr-zone">
              <h2 class="qr-title">扫码签到</h2>
              <div class="qr-code-wrapper" v-loading="qrLoading">
                <div class="qr-code-box" v-if="checkinData.checkinCode">
                  <!-- 二维码图片 (如果不方便引入库，可暂时用图片占位，重点展示签到码) -->
                  <!-- 这里假设后端返回的是 token，前端生成 QR，或者后端直接返回 QR 图片Url -->
                  <!-- 简化方案：直接展示大号签到码 -->
                  <div class="check-code-display">
                    <div class="label">活动签到码</div>
                    <div class="code">{{ checkinData.checkinCode }}</div>
                  </div>
                  
                  <div class="qr-placeholder" v-if="checkinData.qrcodeContent">
                    <!-- 这里可以使用 qrcode.vue 组件，如果项目中引入了的话 -->
                    <!-- 暂时保留样式，但提示这是二维码区域 -->
                    <div class="qr-grid">
                      <div v-for="i in 121" :key="i" 
                           :class="['qr-cell', Math.random() > 0.5 ? 'filled' : '']">
                      </div>
                    </div>
                    <div class="qr-logo">
                      <el-icon :size="32"><VideoCamera /></el-icon>
                    </div>
                  </div>
                  <div class="qr-hint">
                    请志愿者输入签到码或扫码签到
                    <el-button link type="primary" size="small" @click="fetchCheckinCode">刷新</el-button>
                  </div>
                </div>
                <div v-else class="qr-error">
                  <el-empty description="无法获取签到码" />
                  <el-button type="primary" @click="fetchCheckinCode">重新获取</el-button>
                </div>
              </div>

              <!-- 进度统计 -->
              <div class="stats-section">
                <el-progress
                  type="dashboard"
                  :percentage="checkinPercentage"
                  :width="160"
                  :stroke-width="12"
                  :color="progressColors"
                >
                  <template #default>
                    <div class="progress-content">
                      <div class="progress-value">{{ checkedInCount }}</div>
                      <div class="progress-label">已签到</div>
                    </div>
                  </template>
                </el-progress>
                <div class="stats-detail">
                  <div class="stat-item">
                    <span class="stat-value total">{{ totalApproved }}</span>
                    <span class="stat-label">总报名</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-value pending">{{ pendingCount }}</span>
                    <span class="stat-label">待签到</span>
                  </div>
                </div>
              </div>

              <!-- 最近签到动态 -->
              <div class="recent-checkins" v-if="recentCheckins.length > 0">
                <h4>最近签到</h4>
                <el-scrollbar height="120px">
                  <div v-for="item in recentCheckins" :key="item.id" class="recent-item">
                    <el-avatar :size="24" :src="item.avatar">{{ item.name?.[0] }}</el-avatar>
                    <span class="name">{{ item.name }}</span>
                    <span class="time">{{ item.time }}</span>
                  </div>
                </el-scrollbar>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧：人工补签列表 -->
        <el-col :span="14">
          <el-card class="manual-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <div class="header-left">
                  <el-icon><Edit /></el-icon>
                  <span class="title">人工补签通道</span>
                  <el-tag type="info" size="small">共 {{ filteredList.length }} 人</el-tag>
                </div>
                <el-input
                  v-model="searchKeyword"
                  placeholder="搜索姓名/学号"
                  style="width: 200px"
                  clearable
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
              </div>
            </template>

            <el-table
              :data="filteredList"
              v-loading="loading"
              stripe
              height="500"
              style="width: 100%"
            >
              <el-table-column label="志愿者" min-width="150">
                <template #default="{ row }">
                  <div class="volunteer-cell">
                    <el-avatar :size="36" :src="row.avatar">
                      <el-icon><User /></el-icon>
                    </el-avatar>
                    <div class="info">
                      <div class="name">{{ row.volunteerName || '未知' }}</div>
                      <div class="student-id">{{ row.studentId || '-' }}</div>
                    </div>
                  </div>
                </template>
              </el-table-column>

              <el-table-column label="签到状态" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.signInTime ? 'success' : 'warning'" effect="light">
                    {{ row.signInTime ? '已签到' : '未签到' }}
                  </el-tag>
                </template>
              </el-table-column>

              <el-table-column label="签到时间" width="100">
                <template #default="{ row }">
                  <span v-if="row.signInTime">{{ formatTime(row.signInTime) }}</span>
                  <span v-else class="text-muted">-</span>
                </template>
              </el-table-column>

              <el-table-column label="操作" width="120" align="center" fixed="right">
                <template #default="{ row }">
                  <el-button
                    v-if="!row.signInTime"
                    type="success"
                    size="small"
                    @click="handleManualCheckin(row)"
                    :loading="processingId === row.id"
                  >
                    <el-icon><Check /></el-icon> 确认到场
                  </el-button>
                  <span v-else class="text-success">
                    <el-icon><CircleCheck /></el-icon>
                  </span>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  VideoCamera, Clock, VideoPlay, Timer, Edit, Search, User, Check, CircleCheck
} from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import dayjs from 'dayjs'

// 状态
const activityId = ref<number | null>(null)
const activities = ref<any[]>([])
const activitiesLoading = ref(false)
const activity = ref<any>({})
const registrations = ref<any[]>([])
const loading = ref(false)
const processingId = ref<number | null>(null)
const searchKeyword = ref('')
const refreshTimer = ref<number | null>(null)
const checkinData = ref<any>({})
const qrLoading = ref(false)

// 状态映射
const STATUS_MAP: Record<number, { label: string; type: string }> = {
  0: { label: '草稿', type: 'info' },
  1: { label: '待审核', type: 'warning' },
  2: { label: '已发布', type: 'success' },
  3: { label: '进行中', type: 'primary' },
  4: { label: '已结束', type: 'info' },
  5: { label: '已取消', type: 'danger' }
}

const getStatusLabel = (status: number) => STATUS_MAP[status]?.label || '未知'
const getStatusType = (status: number) => STATUS_MAP[status]?.type || 'info'

// 计算属性
const filteredList = computed(() => {
  if (!searchKeyword.value) return registrations.value
  const kw = searchKeyword.value.toLowerCase()
  return registrations.value.filter(
    r => (r.volunteerName && r.volunteerName.toLowerCase().includes(kw)) ||
         (r.studentId && r.studentId.includes(kw))
  )
})

const totalApproved = computed(() => registrations.value.length)
const checkedInCount = computed(() => registrations.value.filter(r => r.signInTime).length)
const pendingCount = computed(() => totalApproved.value - checkedInCount.value)
const checkinPercentage = computed(() => {
  if (totalApproved.value === 0) return 0
  return Math.round((checkedInCount.value / totalApproved.value) * 100)
})

// 进度条颜色
const progressColors = [
  { color: '#f56c6c', percentage: 30 },
  { color: '#e6a23c', percentage: 60 },
  { color: '#67c23a', percentage: 100 }
]

// 最近签到记录 (模拟)
const recentCheckins = computed(() => {
  return registrations.value
    .filter(r => r.signInTime)
    .sort((a, b) => new Date(b.signInTime).getTime() - new Date(a.signInTime).getTime())
    .slice(0, 5)
    .map(r => ({
      id: r.id,
      name: r.volunteerName,
      avatar: r.volunteerAvatar,
      time: dayjs(r.signInTime).format('HH:mm:ss')
    }))
})

const formatDate = (date: string) => date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-'
const formatTime = (date: string) => date ? dayjs(date).format('HH:mm:ss') : '-'

// API 调用
const fetchActivities = async () => {
  activitiesLoading.value = true
  try {
    const res = await request.get('/activity/my', { page: 1, size: 100 })
    // 只显示可以签到的活动 (已发布或进行中)
    activities.value = (res.data?.records || []).filter(
      (a: any) => a.status === 2 || a.status === 3
    )
  } catch (e) {
    console.error('获取活动列表失败:', e)
  } finally {
    activitiesLoading.value = false
  }
}

const fetchActivity = async () => {
  if (!activityId.value) return
  try {
    const res = await request.get(`/activity/${activityId.value}`)
    activity.value = res.data || {}
  } catch (e) {
    console.error('获取活动信息失败:', e)
  }
}

const fetchRegistrations = async () => {
  if (!activityId.value) return
  loading.value = true
  try {
    // 获取已通过审核(1)和已签到(2)的报名列表
    const res = await request.get(`/registration/activity/${activityId.value}`, {
      page: 1, size: 1000
    })
    // 过滤出状态为1或2的记录
    const allRecords = res.data?.records || res.data || []
    registrations.value = allRecords.filter((r: any) => r.status === 1 || r.status === 2)
  } catch (e) {
    console.error('获取报名列表失败:', e)
  } finally {
    loading.value = false
  }
}

const fetchCheckinCode = async () => {
  if (!activityId.value) return
  qrLoading.value = true
  try {
    const res = await request.get(`/organizer/checkin/activity/${activityId.value}/qrcode`)
    checkinData.value = res.data || {}
    console.log('签到码获取成功:', checkinData.value)
  } catch (e) {
    console.error('获取签到码失败:', e)
    ElMessage.error('获取签到码失败')
  } finally {
    qrLoading.value = false
  }
}

const handleManualCheckin = async (row: any) => {
  processingId.value = row.id
  try {
    await request.post(`/registration/signin/${row.id}`)
    ElMessage.success(`${row.volunteerName} 签到成功！`)
    
    // 更新本地状态
    row.signInTime = new Date().toISOString()
  } catch (e) {
    console.error('签到失败:', e)
    ElMessage.error('签到失败，请重试')
  } finally {
    processingId.value = null
  }
}

const selectActivity = (id: number) => {
  activityId.value = id
}

const exitCheckin = () => {
  activityId.value = null
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value)
    refreshTimer.value = null
  }
}

// 自动刷新机制 - 每5秒刷新
const startAutoRefresh = () => {
  if (refreshTimer.value) clearInterval(refreshTimer.value)
  refreshTimer.value = window.setInterval(() => {
    if (activityId.value) {
      fetchRegistrations()
    }
  }, 5000)
}

watch(activityId, (newId) => {
  if (newId) {
    fetchActivity()
    fetchRegistrations()
    fetchCheckinCode() // 获取签到码
    startAutoRefresh()
  } else {
    fetchActivities()
  }
})

onMounted(() => {
  fetchActivities()
})

onUnmounted(() => {
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value)
  }
})
</script>

<style scoped lang="scss">
.org-checkin-page {
  padding: 10px;

  .selector-card {
    border-radius: 12px;
    .card-header {
      display: flex;
      align-items: center;
      gap: 8px;
      .title { font-weight: 600; font-size: 16px; }
    }
  }

  .time-cell {
    display: flex;
    align-items: center;
    gap: 6px;
    .el-icon { color: var(--el-color-primary); }
  }

  .page-title {
    font-size: 18px;
    font-weight: 600;
  }

  // 左侧 QR 区域
  .qr-card {
    border-radius: 12px;
    background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
    
    :deep(.el-card__body) {
      padding: 30px;
    }
  }

  .qr-zone {
    text-align: center;
    color: white;

    .qr-title {
      font-size: 24px;
      margin: 0 0 24px;
      font-weight: 700;
    }

    .qr-code-wrapper {
      margin-bottom: 30px;
    }

    .qr-placeholder {
      width: 220px;
      height: 220px;
      margin: 0 auto 16px;
      background: white;
      border-radius: 12px;
      padding: 15px;
      position: relative;
      display: flex;
      align-items: center;
      justify-content: center;

      .qr-grid {
        display: grid;
        grid-template-columns: repeat(11, 1fr);
        gap: 2px;
        width: 100%;
        height: 100%;
      }

      .qr-cell {
        background: #eee;
        border-radius: 1px;
        &.filled { background: #1a1a2e; }
      }

      .qr-logo {
        position: absolute;
        width: 50px;
        height: 50px;
        background: white;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        .el-icon { color: #409eff; }
      }
    }

    .check-code-display {
      margin-bottom: 20px;
      padding: 15px;
      background: rgba(255,255,255,0.1);
      border-radius: 8px;
      
      .label {
        font-size: 14px;
        opacity: 0.8;
        margin-bottom: 5px;
      }
      
      .code {
        font-size: 42px;
        font-weight: 800;
        letter-spacing: 4px;
        color: #ffd04b;
        text-shadow: 0 2px 4px rgba(0,0,0,0.3);
      }
    }

    .qr-hint {
      font-size: 14px;
      opacity: 0.8;
    }

    .stats-section {
      margin-top: 30px;
      padding-top: 30px;
      border-top: 1px solid rgba(255,255,255,0.1);

      :deep(.el-progress) {
        margin-bottom: 20px;
      }

      .progress-content {
        .progress-value {
          font-size: 36px;
          font-weight: 700;
          color: #67c23a;
        }
        .progress-label {
          font-size: 14px;
          color: rgba(255,255,255,0.7);
        }
      }

      .stats-detail {
        display: flex;
        justify-content: center;
        gap: 40px;

        .stat-item {
          display: flex;
          flex-direction: column;
          align-items: center;

          .stat-value {
            font-size: 28px;
            font-weight: 700;
            &.total { color: #409eff; }
            &.pending { color: #e6a23c; }
          }

          .stat-label {
            font-size: 12px;
            opacity: 0.7;
          }
        }
      }
    }

    .recent-checkins {
      margin-top: 24px;
      text-align: left;
      background: rgba(255,255,255,0.05);
      border-radius: 8px;
      padding: 12px;

      h4 {
        margin: 0 0 12px;
        font-size: 14px;
        opacity: 0.9;
      }

      .recent-item {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 6px 0;
        border-bottom: 1px solid rgba(255,255,255,0.05);

        &:last-child { border-bottom: none; }

        .name {
          flex: 1;
          font-size: 13px;
        }

        .time {
          font-size: 12px;
          opacity: 0.6;
        }
      }
    }
  }

  // 右侧列表
  .manual-card {
    border-radius: 12px;
    height: 100%;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .header-left {
        display: flex;
        align-items: center;
        gap: 8px;

        .title {
          font-weight: 600;
          font-size: 15px;
        }
      }
    }
  }

  .volunteer-cell {
    display: flex;
    align-items: center;
    gap: 10px;

    .info {
      .name {
        font-weight: 600;
        font-size: 14px;
      }
      .student-id {
        font-size: 12px;
        color: var(--el-text-color-secondary);
      }
    }
  }

  .text-muted {
    color: var(--el-text-color-placeholder);
  }

  .text-success {
    color: var(--el-color-success);
    font-size: 20px;
  }
}
</style>
