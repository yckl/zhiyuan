<template>
  <div class="org-hours-entry">
    <el-card shadow="hover" class="main-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon :size="20"><Timer /></el-icon>
            <span class="title">工时录入与结算</span>
          </div>
        </div>
      </template>

      <!-- 顶部操作栏 -->
      <div class="action-bar">
        <div class="action-left">
          <el-select
            v-model="selectedActivityId"
            placeholder="选择活动"
            filterable
            style="width: 300px"
            @change="handleActivityChange"
          >
            <el-option
              v-for="item in activities"
              :key="item.id"
              :label="item.title"
              :value="item.id"
            >
              <div class="activity-option">
                <span>{{ item.title }}</span>
                <el-tag :type="item.status === 4 ? 'info' : 'primary'" size="small">
                  {{ item.status === 4 ? '已结束' : '进行中' }}
                </el-tag>
              </div>
            </el-option>
          </el-select>

          <div class="activity-info" v-if="selectedActivity">
            <el-tag type="info">标准工时: <strong>{{ selectedActivity.serviceHours || 3 }}</strong> 小时</el-tag>
            <el-tag type="success">积分奖励: <strong>{{ selectedActivity.pointsReward || 10 }}</strong> 分</el-tag>
          </div>
        </div>

        <div class="action-right">
          <el-button @click="fillAllHours" :disabled="!selectedActivityId || attendees.length === 0">
            <el-icon><EditPen /></el-icon> 一键填充
          </el-button>
          <el-button
            type="primary"
            @click="submitAll"
            :loading="submitting"
            :disabled="!selectedActivityId || attendees.length === 0"
          >
            <el-icon><Check /></el-icon> 全部提交
          </el-button>
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="stats-bar" v-if="selectedActivityId && attendees.length > 0">
        <div class="stat-item">
          <span class="stat-value">{{ attendees.length }}</span>
          <span class="stat-label">已签到人数</span>
        </div>
        <el-divider direction="vertical" />
        <div class="stat-item">
          <span class="stat-value pending">{{ pendingCount }}</span>
          <span class="stat-label">待录入</span>
        </div>
        <el-divider direction="vertical" />
        <div class="stat-item">
          <span class="stat-value settled">{{ settledCount }}</span>
          <span class="stat-label">已结算</span>
        </div>
        <el-divider direction="vertical" />
        <div class="stat-item">
          <span class="stat-value total">{{ totalHours.toFixed(1) }}</span>
          <span class="stat-label">总工时</span>
        </div>
      </div>

      <!-- 人员列表 -->
      <el-table
        :data="attendees"
        v-loading="loading"
        stripe
        style="width: 100%"
        class="hours-table"
      >
        <el-table-column label="志愿者" min-width="180">
          <template #default="{ row }">
            <div class="volunteer-cell">
              <el-avatar :size="40" :src="row.avatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <div class="info">
                <div class="name">{{ row.name }}</div>
                <div class="student-id">{{ row.studentId }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="签到时间" width="110" align="center">
          <template #default="{ row }">
            <span class="time-text">{{ row.checkIn || '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="签退时间" width="110" align="center">
          <template #default="{ row }">
            <span class="time-text">{{ row.checkOut || '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="确认工时" width="140" align="center">
          <template #default="{ row }">
            <el-input-number
              v-model="row.hours"
              :min="0"
              :max="24"
              :precision="1"
              :step="0.5"
              size="small"
              :disabled="row.status === 'SETTLED'"
              class="hours-input"
            />
          </template>
        </el-table-column>

        <el-table-column label="服务评价" width="160" align="center">
          <template #default="{ row }">
            <el-rate
              v-model="row.rating"
              :disabled="row.status === 'SETTLED'"
              show-score
              text-color="#ff9900"
            />
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SETTLED' ? 'success' : 'warning'" effect="light">
              {{ row.status === 'SETTLED' ? '已结算' : '待录入' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.status !== 'SETTLED'"
              type="success"
              size="small"
              @click="submitSingle(row)"
              :loading="row.submitting"
            >
              结算
            </el-button>
            <span v-else class="text-success">
              <el-icon><CircleCheck /></el-icon>
            </span>
          </template>
        </el-table-column>
      </el-table>

      <el-empty
        v-if="!loading && selectedActivityId && attendees.length === 0"
        description="暂无已签到的志愿者"
      />

      <el-empty
        v-if="!selectedActivityId"
        description="请先选择一个活动"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Timer, EditPen, Check, User, CircleCheck } from '@element-plus/icons-vue'
import { request } from '@/utils/request'

const loading = ref(false)
const submitting = ref(false)
const activities = ref<any[]>([])
const selectedActivityId = ref<number | null>(null)
const selectedActivity = ref<any>(null)
const attendees = ref<any[]>([])

// 统计
const pendingCount = computed(() => attendees.value.filter(a => a.status !== 'SETTLED').length)
const settledCount = computed(() => attendees.value.filter(a => a.status === 'SETTLED').length)
const totalHours = computed(() => attendees.value.reduce((sum, a) => sum + (a.hours || 0), 0))

// 获取活动列表 (已发布、进行中、已完成的)
const fetchActivities = async () => {
  try {
    const res = await request.get('/activity/my', { page: 1, size: 100 })
    // 筛选已发布(2)、进行中(3)、已结束(4)的活动
    activities.value = (res.data?.records || []).filter(
      (a: any) => a.status === 2 || a.status === 3 || a.status === 4
    )
  } catch (e) {
    console.error('获取活动列表失败:', e)
  }
}

// 加载已签到志愿者
const fetchAttendees = async () => {
  if (!selectedActivityId.value) {
    attendees.value = []
    return
  }

  loading.value = true
  try {
    // 获取所有报名记录，然后筛选已签到(2)和已完成(3)的
    const res = await request.get(`/registration/activity/${selectedActivityId.value}`, {
      page: 1,
      size: 1000
    })

    const records = res.data?.records || res.data || []
    
    // 只保留已签到(2)和已完成(3)的记录
    attendees.value = records
      .filter((r: any) => r.status === 2 || r.status === 3)
      .map((r: any) => ({
        id: r.id,
        volunteerId: r.volunteerId,
        name: r.volunteerName || '未知',
        studentId: r.studentNo || r.studentId || '-',
        avatar: r.volunteerAvatar,
        checkIn: r.signInTime ? formatTime(r.signInTime) : null,
        checkOut: r.signOutTime ? formatTime(r.signOutTime) : null,
        hours: r.actualHours || selectedActivity.value?.serviceHours || 0,
        rating: r.rating || 5,
        status: r.status === 3 ? 'SETTLED' : 'PENDING', // 已完成=已结算
        submitting: false
      }))
  } catch (e) {
    console.error('获取签到列表失败:', e)
    attendees.value = []
  } finally {
    loading.value = false
  }
}

// 生成 Mock 数据
const generateMockData = () => {
  const names = ['张三', '李四', '王五', '赵六', '陈七', '刘八', '周九', '吴十', '郑十一', '孙十二']
  return names.map((name, i) => ({
    id: i + 1,
    volunteerId: 1000 + i,
    name,
    studentId: `2024${String(i + 1).padStart(3, '0')}`,
    avatar: null,
    checkIn: `0${8 + Math.floor(i / 3)}:${String(Math.floor(Math.random() * 60)).padStart(2, '0')}`,
    checkOut: i % 3 === 0 ? null : `1${1 + Math.floor(i / 3)}:${String(Math.floor(Math.random() * 60)).padStart(2, '0')}`,
    hours: 0,
    rating: 5,
    status: 'PENDING',
    submitting: false
  }))
}

const formatTime = (datetime: string) => {
  if (!datetime) return null
  const date = new Date(datetime)
  return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const handleActivityChange = () => {
  selectedActivity.value = activities.value.find(a => a.id === selectedActivityId.value)
  fetchAttendees()
}

// 一键填充标准工时
const fillAllHours = () => {
  const standardHours = selectedActivity.value?.serviceHours || 3
  attendees.value.forEach(a => {
    if (a.status !== 'SETTLED') {
      a.hours = standardHours
    }
  })
  ElMessage.success(`已将所有待录入工时设置为 ${standardHours} 小时`)
}

// 单个结算
const submitSingle = async (row: any) => {
  if (row.hours <= 0) {
    return ElMessage.warning('请先填写工时')
  }

  row.submitting = true
  try {
    await request.post('/organizer/settlement/settle', {
      registrationId: row.id,
      hours: row.hours,
      rating: row.rating
    })
    row.status = 'SETTLED'
    ElMessage.success(`${row.name} 结算成功，已发放积分`)
  } catch (e: any) {
    console.error('结算失败:', e)
    ElMessage.error(e.msg || '结算失败，请重试')
  } finally {
    row.submitting = false
  }
}

// 全部提交
const submitAll = async () => {
  const pending = attendees.value.filter(a => a.status !== 'SETTLED')
  const noHours = pending.filter(a => a.hours <= 0)

  if (noHours.length > 0) {
    try {
      await ElMessageBox.confirm(
        `有 ${noHours.length} 人工时为0，确定提交吗？工时为0的将不会结算。`,
        '提示',
        { type: 'warning' }
      )
    } catch {
      return
    }
  }

  const toSettle = pending.filter(a => a.hours > 0)
  if (toSettle.length === 0) {
    return ElMessage.warning('没有可结算的记录')
  }

  submitting.value = true
  try {
    // 批量结算 - 使用正确的API
    const res = await request.post('/organizer/settlement/batch', {
      items: toSettle.map(a => ({
        registrationId: a.id,
        hours: a.hours,
        rating: a.rating
      }))
    })
    toSettle.forEach(a => a.status = 'SETTLED')
    const batchResult = res.data || {}
    ElMessage.success(`结算成功！已为 ${batchResult.success || toSettle.length} 人发放积分`)
    // 刷新列表
    await fetchAttendees()
  } catch (e: any) {
    console.error('批量结算失败:', e)
    ElMessage.error(e.msg || '批量结算失败，请重试')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchActivities()
})
</script>

<style scoped lang="scss">
.org-hours-entry {
  padding: 10px;

  .main-card {
    border-radius: 12px;
    border: none;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-left {
      display: flex;
      align-items: center;
      gap: 8px;

      .title {
        font-size: 16px;
        font-weight: 600;
      }
    }
  }

  .action-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 16px;
    border-bottom: 1px solid var(--el-border-color-lighter);

    .action-left {
      display: flex;
      align-items: center;
      gap: 16px;

      .activity-info {
        display: flex;
        gap: 8px;

        strong {
          color: var(--el-color-primary);
        }
      }
    }

    .action-right {
      display: flex;
      gap: 10px;
    }
  }

  .activity-option {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
  }

  .stats-bar {
    display: flex;
    align-items: center;
    gap: 24px;
    padding: 16px 24px;
    background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
    border-radius: 8px;
    margin-bottom: 20px;

    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;

      .stat-value {
        font-size: 24px;
        font-weight: 700;
        color: var(--el-text-color-primary);

        &.pending { color: var(--el-color-warning); }
        &.settled { color: var(--el-color-success); }
        &.total { color: var(--el-color-primary); }
      }

      .stat-label {
        font-size: 12px;
        color: var(--el-text-color-secondary);
      }
    }
  }

  .hours-table {
    .volunteer-cell {
      display: flex;
      align-items: center;
      gap: 12px;

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

    .time-text {
      font-family: 'Consolas', monospace;
      font-size: 13px;
      color: var(--el-text-color-regular);
    }

    .hours-input {
      width: 100px;

      :deep(.el-input__inner) {
        text-align: center;
        font-weight: 600;
      }
    }

    .text-success {
      color: var(--el-color-success);
      font-size: 20px;
    }
  }
}
</style>
