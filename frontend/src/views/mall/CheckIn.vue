<template>
  <div class="checkin-page">
    <div class="page-header">
      <h2>📅 每日签到</h2>
      <p class="subtitle">坚持签到，积分多多！</p>
    </div>

    <div class="checkin-content">
      <!-- 左侧：签到信息 -->
      <div class="checkin-info-card">
        <div class="streak-display">
          <span class="label">已连续签到</span>
          <span class="days">{{ continuousDays }}</span>
          <span class="unit">天</span>
        </div>

        <div class="stats-row">
          <div class="stat-item">
            <span class="stat-value">{{ monthlySignins }}</span>
            <span class="stat-label">本月签到</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ totalSignins }}</span>
            <span class="stat-label">累计签到</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ availablePoints }}</span>
            <span class="stat-label">当前积分</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ makeupCardCount }}</span>
            <span class="stat-label">补签卡</span>
          </div>
        </div>

        <div class="reward-info">
          <h4>签到奖励规则</h4>
          <ul>
            <li>基础签到：<strong>5 积分</strong></li>
            <li>连续 7 天：<strong>10 积分</strong>（双倍）</li>
            <li>连续 30 天：<strong>15 积分</strong>（三倍）</li>
          </ul>
        </div>

        <!-- 签到按钮 -->
        <el-button
          type="primary"
          size="large"
          class="checkin-btn"
          :disabled="signedToday || loading"
          :loading="loading"
          @click="handleSignin"
        >
          <template v-if="signedToday">
            <el-icon><Check /></el-icon>
            今日已签到
          </template>
          <template v-else>
            立即签到
          </template>
        </el-button>

        <!-- 补签 -->
        <div v-if="canMakeup" class="makeup-section">
          <p>昨日未签到？使用补签卡恢复连续天数</p>
          <el-button type="warning" size="small" @click="handleMakeup">
            使用补签卡 ({{ makeupCardCount }}张)
          </el-button>
        </div>
      </div>

      <!-- 右侧：签到日历 -->
      <div class="calendar-card">
        <el-calendar v-model="calendarValue">
          <template #date-cell="{ data }">
            <div 
              class="calendar-cell" 
              :class="{ 
                'is-signed': isSignedDate(data.day),
                'can-makeup': isPastUnsignedDate(data.day)
              }"
              @click="handleDateClick(data)"
            >
              <span class="date-num">{{ data.day.split('-')[2] }}</span>
              <el-icon v-if="isSignedDate(data.day)" class="check-icon"><Check /></el-icon>
            </div>
          </template>
        </el-calendar>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import { request } from '@/utils/request'

const loading = ref(false)
const signedToday = ref(false)
const continuousDays = ref(0)
const monthlySignins = ref(0)
const totalSignins = ref(0)
const availablePoints = ref(0)
const canMakeup = ref(false)
const makeupCardCount = ref(0)
const calendarValue = ref(new Date())
const signedDates = ref<string[]>([])

const isSignedDate = (dateStr: string) => {
  return signedDates.value.includes(dateStr)
}

const isPastUnsignedDate = (dateStr: string) => {
  const todayStr = new Date().toISOString().split('T')[0]
  return dateStr < todayStr && !isSignedDate(dateStr)
}

const fetchStatus = async () => {
  try {
    const res = await request.get('/checkin/status')
    if (res.code === 200 && res.data) {
      const data = res.data
      signedToday.value = data.signedToday || false
      continuousDays.value = data.continuousDays || 0
      monthlySignins.value = data.monthlySignins || 0
      totalSignins.value = data.totalSignins || 0
      availablePoints.value = data.availablePoints || 0
      canMakeup.value = data.canMakeup || false
      makeupCardCount.value = data.makeupCardCount || 0
    }
  } catch (error) {
    console.error('获取签到状态失败:', error)
  }
}

const fetchCalendar = async () => {
  const year = calendarValue.value.getFullYear()
  const month = calendarValue.value.getMonth() + 1
  
  try {
    const res = await request.get('/checkin/calendar', {
      params: { year, month }
    })
    if (res.code === 200 && res.data) {
      // 确保日期格式统一
      signedDates.value = res.data.map((item: any) => {
        const date = item.signinDate
        // 如果是数组 [2026, 2, 8]
        if (Array.isArray(date)) {
          const y = date[0]
          const m = String(date[1]).padStart(2, '0')
          const d = String(date[2]).padStart(2, '0')
          return `${y}-${m}-${d}`
        }
        // 如果是字符串，直接返回（假设是 yyyy-MM-dd）
        return String(date)
      })
      console.log('Signed dates:', signedDates.value)
    }
  } catch (error) {
    console.error('获取签到日历失败:', error)
  }
}

const handleSignin = async () => {
  if (signedToday.value || loading.value) return

  loading.value = true
  try {
    const res = await request.post('/checkin/signin')
    if (res.code === 200 && res.data?.success) {
      ElMessage.success(res.data.message || '签到成功！')
      signedToday.value = true
      continuousDays.value = res.data.continuousDays || continuousDays.value + 1
      availablePoints.value = res.data.availablePoints || availablePoints.value
      fetchCalendar()
      fetchStatus()
    } else {
      ElMessage.error(res.message || '签到失败')
    }
  } catch (error) {
    ElMessage.error('签到失败，请重试')
  } finally {
    loading.value = false
  }
}

const handleMakeup = async (date: string = '') => {
  if (loading.value) return

  loading.value = true
  try {
    const res = await request.post('/checkin/makeup', {}, {
      params: { date }
    })
    if (res.code === 200 && res.data?.success) {
      ElMessage.success(res.data.message || '补签成功！')
      fetchStatus()
      fetchCalendar()
    } else {
      ElMessage.error(res.message || '补签失败')
    }
  } catch (error) {
    ElMessage.error('补签失败，请重试')
  } finally {
    loading.value = false
  }
}

const handleDateClick = (data: any) => {
  const dateStr = data.day
  if (isPastUnsignedDate(dateStr)) {
    if (makeupCardCount.value <= 0) {
      ElMessage.warning('您当前没有补签卡，请前往任务中心或积分商城获取')
      return
    }

    ElMessageBox.confirm(
      `确定消耗 1 张补签卡为 ${dateStr} 补签吗？`,
      '补签确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(() => {
      handleMakeup(dateStr)
    }).catch(() => {})
  }
}

watch(calendarValue, fetchCalendar)

onMounted(() => {
  fetchStatus()
  fetchCalendar()
})
</script>

<style lang="scss" scoped>
.checkin-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;

  h2 {
    margin: 0 0 8px;
    font-size: 24px;
  }

  .subtitle {
    margin: 0;
    color: #999;
  }
}

.checkin-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;

  @media (max-width: 900px) {
    grid-template-columns: 1fr;
  }
}

.checkin-info-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 32px;
  color: #fff;

  .streak-display {
    text-align: center;
    margin-bottom: 24px;

    .label {
      display: block;
      font-size: 14px;
      opacity: 0.8;
      margin-bottom: 8px;
    }

    .days {
      font-size: 72px;
      font-weight: bold;
      line-height: 1;
    }

    .unit {
      font-size: 24px;
      margin-left: 8px;
    }
  }

  .stats-row {
    display: flex;
    justify-content: space-around;
    margin-bottom: 24px;
    padding: 16px 0;
    border-top: 1px solid rgba(255, 255, 255, 0.2);
    border-bottom: 1px solid rgba(255, 255, 255, 0.2);

    .stat-item {
      text-align: center;

      .stat-value {
        display: block;
        font-size: 24px;
        font-weight: bold;
      }

      .stat-label {
        font-size: 12px;
        opacity: 0.8;
      }
    }
  }

  .reward-info {
    background: rgba(255, 255, 255, 0.1);
    border-radius: 8px;
    padding: 16px;
    margin-bottom: 24px;

    h4 {
      margin: 0 0 12px;
      font-size: 14px;
    }

    ul {
      margin: 0;
      padding-left: 20px;
      font-size: 13px;
      
      li {
        margin-bottom: 6px;
      }
    }
  }

  .checkin-btn {
    width: 100%;
    height: 56px;
    font-size: 20px;
    font-weight: bold;
    border-radius: 28px;
    background: #fff;
    color: #667eea;
    border: none;

    &:hover:not(:disabled) {
      background: #f0f0f0;
    }

    &:disabled {
      background: rgba(255, 255, 255, 0.5);
      color: #666;
    }
  }

  .makeup-section {
    margin-top: 16px;
    text-align: center;

    p {
      margin: 0 0 12px;
      font-size: 13px;
      opacity: 0.8;
    }
  }
}

.calendar-card {
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);

  :deep(.el-calendar) {
    .el-calendar__header {
      padding: 12px 16px;
    }

    .el-calendar-table td {
      border: none;
    }

    .el-calendar-day {
      height: 60px;
      padding: 4px;
    }
  }

  .calendar-cell {
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    border-radius: 8px;
    transition: all 0.3s;

    &.is-signed {
      background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
      color: #fff;

      .check-icon {
        color: #fff;
        font-size: 16px;
        margin-top: 4px;
      }
    }

    &.can-makeup {
      cursor: pointer;
      &:hover {
        background: rgba(230, 162, 60, 0.1);
        border: 1px dashed #e6a23c;
      }
    }

    .date-num {
      font-size: 14px;
      font-weight: 500;
    }
  }
}
</style>
