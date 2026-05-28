<template>
  <div class="hours-entry-page">
    <!-- Simplified Header Area (No sticky to avoid breadcrumb overlap) -->
    <div class="page-header-container">
      <div class="hero-title-row">
        <h1 class="page-title">工时录入 · 组织者</h1>
        <div class="user-badge-mini">师</div>
      </div>
    </div>

    <main class="main-content">
      <!-- Activity Selector -->
      <div class="selector-section">
        <div class="section-label-row">
          <label class="section-label">结算活动</label>
        </div>
        
        <!-- Search Bar -->
        <div class="search-bar-container" :class="{ 'is-open': isDropdownOpen }">
          <div class="search-bar-trigger" @click="toggleDropdown">
            <el-icon class="search-icon"><Search /></el-icon>
            <div class="search-placeholder">
              <span v-if="!selectedActivityId" class="placeholder-text">搜索或选择需要结算的活动</span>
              <span v-else class="selected-text">{{ selectedActivityTitle }}</span>
            </div>
            <el-icon class="arrow-icon" :class="{ 'is-active': isDropdownOpen }"><ArrowDown /></el-icon>
          </div>

          <!-- Dropdown Panel -->
          <transition name="dropdown-fade">
            <div v-if="isDropdownOpen" class="dropdown-panel shadow-premium">
              <!-- Loading -->
              <div v-if="isLoading" class="loading-state">
                <div class="spinner"></div>
                <p>正在加载活动数据...</p>
              </div>

              <!-- List -->
              <div v-else class="activity-list">
                <div 
                  v-for="item in activities" 
                  :key="item.id"
                  class="activity-card"
                  :class="{ 'is-selected': selectedActivityId === item.id }"
                  @click="handleSelect(item)"
                >
                  <!-- Active Indicator -->
                  <div class="active-indicator"></div>
                  
                  <!-- Pending Badge (Top-Right) -->
                  <div class="pending-badge">待结算</div>

                  <!-- Thumbnail (72x72) -->
                  <div class="card-image">
                    <img :src="item.coverImage" />
                  </div>

                  <!-- Info -->
                  <div class="card-info">
                    <h4 class="card-title">{{ item.title }}</h4>
                    <p class="card-subtitle">
                      {{ item.organizerName }} · {{ item.summary || '暂无简介' }}
                    </p>
                    <div class="card-bottom">
                      <span class="time">{{ dayjs(item.startTime).format('YYYY-MM-DD') }}</span>
                    </div>
                  </div>

                  <!-- Checkmark -->
                  <div v-if="selectedActivityId === item.id" class="check-box">
                    <el-icon><Check /></el-icon>
                  </div>
                </div>
              </div>
            </div>
          </transition>
        </div>
      </div>

      <!-- Empty State -->
      <div v-if="!selectedActivityId" class="empty-state-container">
        <div class="empty-illustration">
          <div class="blob-bg-1"></div>
          <div class="blob-bg-2"></div>
          <div class="illustration-box">
            <el-icon><Box /></el-icon>
          </div>
        </div>
        <div class="empty-text">
          <h3>还没有待结算活动哦～</h3>
          <p>请先选择一个需要录入工时的活动来开始您的工作</p>
        </div>
      </div>

      <!-- Details View (Real Volunteer List) -->
      <div v-else class="details-view-container">
        <!-- Stats Summary -->
        <div class="stats-row animate-[fadeIn_0.5s_ease-out]">
          <div class="stat-card">
            <span class="label">已签到人数</span>
            <span class="value">{{ attendeeStats.totalCount }}</span>
          </div>
          <div class="stat-card highlight">
            <span class="label">待结算人数</span>
            <span class="value">{{ attendeeStats.totalCount - attendeeStats.settledCount }}</span>
          </div>
          <div class="stat-card success">
            <span class="label">已结算人数</span>
            <span class="value">{{ attendeeStats.settledCount }}</span>
          </div>
          <button @click="handleBatchSettle" class="batch-settle-btn" :disabled="!hasUnsettled">
            <el-icon><Check /></el-icon>
            一键批量结算
          </button>
        </div>

        <div v-if="attendeeLoading" class="list-loading">
          <div class="spinner"></div>
          <p>正在获取 {{ selectedActivityTitle }} 的志愿者名单...</p>
        </div>

        <div v-else-if="attendees.length === 0" class="empty-attendees">
          <el-icon class="icon"><User /></el-icon>
          <p>暂无志愿者签到记录</p>
        </div>

        <!-- Volunteer Cards -->
        <div v-else class="volunteer-list text-left">
          <div 
            v-for="volunteer in attendees" 
            :key="volunteer.registrationId"
            class="volunteer-card shadow-premium"
            :class="{ 'is-settled': volunteer.settled }"
          >
            <div class="vol-avatar">
              {{ volunteer.volunteerName?.substring(0, 1) }}
            </div>
            <div class="vol-info">
              <div class="primary-row">
                <span class="vol-name">{{ volunteer.volunteerName }}</span>
                <span class="vol-college">{{ volunteer.college }}</span>
              </div>
              <div class="secondary-row">
                <span class="vol-id">学号: {{ volunteer.studentId }}</span>
                <span class="vol-time">签到: {{ dayjs(volunteer.signInTime).format('MM-DD HH:mm') }}</span>
              </div>
            </div>

            <div class="vol-actions">
              <div v-if="volunteer.settled" class="settled-info">
                <div class="hours-badge">+{{ volunteer.actualHours }}h</div>
                <div class="points-badge">{{ volunteer.actualPoints }}pt</div>
                <span class="status-text">已结算</span>
              </div>
              <div v-else class="unsettled-controls">
                <div class="hours-input-box">
                  <input 
                    type="number" 
                    v-model="volunteer.settleHours" 
                    placeholder="工时"
                    min="0"
                    step="0.5"
                  />
                  <span class="unit">h</span>
                </div>
                <button @click="settleAttendee(volunteer)" class="settle-btn">
                  结算
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { 
  Search, ArrowDown, Check, Box, User
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { request } from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const isDropdownOpen = ref(false)
const isLoading = ref(false)
const attendeeLoading = ref(false)
const selectedActivityId = ref<number | null>(null)
const activities = ref<any[]>([])
const attendees = ref<any[]>([])
const attendeeStats = ref({
  totalCount: 0,
  settledCount: 0
})

const hasUnsettled = computed(() => {
  return attendees.value.some(a => !a.settled)
})

const fetchActivities = async () => {
  try {
    const res = await request.get('/activity/my', { size: 100 })
    activities.value = res.data.records.map((item: any) => ({
      ...item,
      // Map database fields to UI fields if needed
    }))
  } catch (error) {
    console.error('Fetch activities error:', error)
  }
}

const fetchAttendees = async (activityId: number) => {
  attendeeLoading.value = true
  try {
    const res = await request.get(`/organizer/settlement/activity/${activityId}/attendees`)
    attendees.value = res.data.attendees.map((a: any) => ({
      ...a,
      settleHours: a.actualHours || res.data.activity.serviceHours || 0
    }))
    attendeeStats.value = {
      totalCount: res.data.totalCount,
      settledCount: res.data.settledCount
    }
  } catch (error) {
    console.error('Fetch attendees error:', error)
  } finally {
    attendeeLoading.value = false
  }
}

const settleAttendee = async (volunteer: any) => {
  if (volunteer.settleHours <= 0) {
    return ElMessage.warning('请输入有效的结算工时')
  }
  try {
    await request.post('/organizer/settlement/settle', {
      registrationId: volunteer.registrationId,
      hours: volunteer.settleHours,
      rating: 5
    })
    ElMessage.success(`${volunteer.volunteerName} 的工时已完成结算`)
    fetchAttendees(selectedActivityId.value!)
  } catch (error) {
    console.error('Settle error:', error)
  }
}

const handleBatchSettle = async () => {
  const unsettled = attendees.value.filter(a => !a.settled)
  if (unsettled.length === 0) return

  try {
    await ElMessageBox.confirm(
      `确定要为当前符合条件的 ${unsettled.length} 位志愿者批量结算吗？`,
      '批量结算确认',
      { confirmButtonText: '确定结算', cancelButtonText: '取消', type: 'info' }
    )

    const items = unsettled.map(a => ({
      registrationId: a.registrationId,
      hours: a.settleHours,
      rating: 5
    }))

    await request.post('/organizer/settlement/batch', { items })
    ElMessage.success('批量结算任务已完成')
    fetchAttendees(selectedActivityId.value!)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Batch settle error:', error)
    }
  }
}

onMounted(() => {
  fetchActivities()
})

const selectedActivityTitle = computed(() => {
  const activity = activities.value.find(a => a.id === selectedActivityId.value)
  return activity ? activity.title : ''
})

const toggleDropdown = () => {
  if (isDropdownOpen.value) {
    isDropdownOpen.value = false
  } else {
    isLoading.value = true
    isDropdownOpen.value = true
    setTimeout(() => {
      isLoading.value = false
    }, 1200)
  }
}

const handleSelect = (item: any) => {
  selectedActivityId.value = item.id
  fetchAttendees(item.id)
  setTimeout(() => {
    isDropdownOpen.value = false
  }, 400)
}
</script>

<style scoped lang="scss">
.hours-entry-page {
  min-height: 100vh;
  background-color: #F8F9FA;
  padding-bottom: 120px;
  opacity: 0;
  animation: fadeIn 0.6s ease-out forwards;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.shadow-premium {
  box-shadow: 0 10px 40px -10px rgba(0, 0, 0, 0.1);
}

/* Header */
.page-header-container {
  max-width: 960px;
  margin: 0 auto;
  padding: 24px 24px 0;
  position: relative;
}

.hero-title-row {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 16px;
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 900;
  color: #1e293b;
  margin: 0;
  letter-spacing: -0.5px;
}

.user-badge-mini {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #00C853, #69f0ae);
  border-radius: 50%;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 10px rgba(0, 200, 83, 0.3);
}

/* Main Content */
.main-content {
  max-width: 960px;
  margin: 0 auto;
  padding: 12px 24px 32px;
}

.selector-section {
  position: relative;
  margin-bottom: 48px;
}

.section-label-row {
  margin-bottom: 12px;
  padding-left: 4px;
}

.section-label {
  font-size: 15px;
  font-weight: 800;
  color: #334155;
  display: flex;
  align-items: center;
  gap: 8px;
  &::before {
    content: '';
    width: 4px;
    height: 16px;
    background: #00C853;
    border-radius: 4px;
  }
}

/* Search Bar */
.search-bar-container {
  position: relative;
  &.is-open {
    .search-bar-trigger {
      border-color: #00C853;
      box-shadow: 0 10px 40px -10px rgba(0, 200, 83, 0.25);
      background: #fff;
      transform: translateY(-2px);
    }
  }
}

.search-bar-trigger {
  height: 72px;
  background: #fff;
  border: 2px solid transparent;
  border-radius: 20px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.1);
  box-shadow: 0 8px 30px rgba(0,0,0,0.03);
  
  &:hover {
    box-shadow: 0 12px 40px rgba(0,0,0,0.06);
    transform: translateY(-1px);
  }

  .search-icon {
    font-size: 24px;
    color: #2481CC;
    margin-right: 16px;
  }

  .search-placeholder {
    flex: 1;
    .placeholder-text { color: #94a3b8; font-size: 16px; font-weight: 500; }
    .selected-text { color: #0f172a; font-size: 16px; font-weight: 800; }
  }

  .arrow-icon {
    font-size: 20px;
    color: #94a3b8;
    transition: all 0.5s;
    &.is-active {
      transform: rotate(180deg);
      color: #00C853;
    }
  }
}

/* Dropdown */
.dropdown-panel {
  position: absolute;
  top: calc(100% + 12px);
  left: 0;
  right: 0;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 16px;
  max-height: 600px;
  overflow-y: auto;
  border: 1px solid #f1f5f9;
}

.activity-list {
  display: grid;
  grid-template-columns: repeat(1, 1fr);
  gap: 16px;
  
  @media (min-width: 768px) {
    grid-template-columns: repeat(2, 1fr);
  }
}

.dropdown-fade-enter-active {
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.dropdown-fade-leave-active {
  transition: all 0.2s ease;
}
.dropdown-fade-enter-from {
  opacity: 0;
  transform: translateY(10px) scale(0.95);
}

.loading-state {
  padding: 60px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  .spinner {
    width: 48px;
    height: 48px;
    border: 4px solid #f1f5f9;
    border-top: 4px solid #00C853;
    border-radius: 50%;
    animation: spin 1s linear infinite;
  }
  p { margin-top: 16px; color: #64748b; font-weight: 600; }
}

@keyframes spin { 100% { transform: rotate(360deg); } }

/* List Item (Refined) */
.activity-card {
  height: 124px;
  padding: 16px;
  border-radius: 20px;
  background: #f8fafc;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  position: relative;
  overflow: hidden;
  border: 1.5px solid transparent;

  &:hover {
    background: #fff;
    transform: scale(1.02);
    box-shadow: 0 20px 40px rgba(0,0,0,0.08);
    border-color: rgba(0, 200, 83, 0.15);
    .card-image img { transform: scale(1.1); }
  }

  &.is-selected {
    background: #fff;
    border-color: #00C853;
    box-shadow: 0 10px 30px rgba(0, 200, 83, 0.1);
  }

  .card-image {
    width: 72px;
    height: 72px;
    border-radius: 12px;
    overflow: hidden;
    flex-shrink: 0;
    img { width: 100%; height: 100%; object-fit: cover; transition: 0.6s; }
  }

  .card-info {
    flex: 1;
    margin-left: 16px;
    min-width: 0;
    height: 100%;
    display: flex;
    flex-direction: column;
    justify-content: center;
    position: relative;
  }

  .card-title {
    font-size: 18px;
    font-weight: 850;
    color: #0f172a;
    margin: 0 0 4px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .card-subtitle {
    font-size: 14px;
    color: #64748b;
    font-weight: 500;
    line-height: 1.4;
    margin: 0;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .card-bottom {
    margin-top: auto;
    display: flex;
    justify-content: flex-end;
    .time {
      font-size: 11px;
      font-weight: 800;
      color: #FF9800;
      letter-spacing: 0.5px;
    }
  }

  .pending-badge {
    position: absolute;
    top: 12px;
    right: 12px;
    padding: 2px 8px;
    background: #FF9800;
    color: #fff;
    font-size: 11px;
    font-weight: 900;
    border-radius: 6px;
    z-index: 2;
    box-shadow: 0 4px 10px rgba(255, 152, 0, 0.3);
  }

  .check-box {
    position: absolute;
    top: 12px;
    right: 12px;
    width: 24px;
    height: 24px;
    background: #00C853;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 14px;
    z-index: 3;
    transform: scale(0);
    transition: 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  }

  &.is-selected .check-box {
    transform: scale(1);
  }
}

@keyframes pulse {
  0% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.05); opacity: 0.8; }
  100% { transform: scale(1); opacity: 1; }
}

/* Empty State */
.empty-state-container {
  padding: 60px 0;
  text-align: center;
  animation: fadeInUp 0.8s ease-out;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.empty-illustration {
  position: relative;
  width: 200px;
  height: 200px;
  margin: 0 auto 32px;
  .illustration-box {
    position: relative;
    z-index: 2;
    background: #fff;
    width: 120px;
    height: 120px;
    border-radius: 32px;
    margin: 40px auto;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 64px;
    color: #e2e8f0;
    box-shadow: 0 20px 50px rgba(0,0,0,0.05);
  }
  .blob-bg-1, .blob-bg-2 {
    position: absolute;
    width: 140px;
    height: 140px;
    border-radius: 50%;
    filter: blur(40px);
    opacity: 0.1;
    z-index: 1;
  }
  .blob-bg-1 { background: #00C853; top: 0; left: 0; animation: blobMove 7s infinite; }
  .blob-bg-2 { background: #2481CC; bottom: 0; right: 0; animation: blobMove 7s infinite reverse; }
}

@keyframes blobMove {
  0% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(20px, 30px) scale(1.1); }
  100% { transform: translate(0, 0) scale(1); }
}

.empty-text {
  h3 { font-size: 18px; font-weight: 800; color: #64748b; margin-bottom: 8px; }
  p { font-size: 14px; color: #94a3b8; }
}

/* Ready Card */
.details-view-container {
  padding-bottom: 40px;
  animation: fadeInUp 0.5s ease-out;
}

.stats-row {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
  
  .stat-card {
    flex: 1;
    min-width: 140px;
    background: #fff;
    padding: 16px;
    border-radius: 20px;
    display: flex;
    flex-direction: column;
    gap: 4px;
    box-shadow: 0 4px 15px rgba(0,0,0,0.03);
    
    .label { font-size: 13px; color: #64748b; font-weight: 600; }
    .value { font-size: 24px; font-weight: 900; color: #1e293b; }
    
    &.highlight {
      background: rgba(255, 152, 0, 0.05);
      .value { color: #FF9800; }
    }
    
    &.success {
      background: rgba(0, 200, 83, 0.05);
      .value { color: #00C853; }
    }
  }

  .batch-settle-btn {
    height: 64px;
    padding: 0 24px;
    background: #00C853;
    color: #fff;
    border: none;
    border-radius: 20px;
    font-size: 16px;
    font-weight: 800;
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    transition: all 0.3s;
    box-shadow: 0 10px 25px rgba(0, 200, 83, 0.3);
    
    &:hover:not(:disabled) {
      transform: translateY(-2px);
      box-shadow: 0 15px 35px rgba(0, 200, 83, 0.4);
    }
    
    &:disabled {
      background: #e2e8f0;
      color: #94a3b8;
      box-shadow: none;
      cursor: not-allowed;
    }
    
    .el-icon { font-size: 20px; }
  }
}

.list-loading {
  padding: 80px 0;
  text-align: center;
  .spinner {
    width: 40px;
    height: 40px;
    border: 3px solid #f1f5f9;
    border-top-color: #00C853;
    border-radius: 50%;
    margin: 0 auto 16px;
    animation: spin 1s linear infinite;
  }
  p { color: #64748b; font-weight: 600; }
}

.empty-attendees {
  padding: 100px 0;
  text-align: center;
  background: #fff;
  border-radius: 32px;
  .icon { font-size: 48px; color: #e2e8f0; margin-bottom: 16px; }
  p { color: #94a3b8; font-weight: 600; }
}

.volunteer-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.volunteer-card {
  background: #fff;
  border-radius: 24px;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 20px;
  transition: all 0.3s;
  border: 1px solid transparent;

  &.is-settled {
    background: #fcfdfd;
    opacity: 0.9;
    .vol-avatar { background: #f1f5f9; color: #94a3b8; }
  }

  &:hover:not(.is-settled) {
    transform: translateX(4px);
    border-color: rgba(0, 200, 83, 0.1);
    box-shadow: 0 10px 30px rgba(0,0,0,0.05);
  }

  .vol-avatar {
    width: 52px;
    height: 52px;
    background: rgba(0, 200, 83, 0.1);
    color: #00C853;
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    font-weight: 900;
    flex-shrink: 0;
  }

  .vol-info {
    flex: 1;
    min-width: 0;
    .primary-row {
      margin-bottom: 4px;
      .vol-name { font-size: 16px; font-weight: 800; color: #1e293b; margin-right: 8px; }
      .vol-college { font-size: 12px; font-weight: 700; color: #94a3b8; background: #f1f5f9; padding: 1px 6px; border-radius: 4px; }
    }
    .secondary-row {
      display: flex;
      gap: 16px;
      font-size: 13px;
      color: #64748b;
      .vol-id { font-family: monospace; }
    }
  }

  .vol-actions {
    flex-shrink: 0;
    
    .settled-info {
      display: flex;
      align-items: center;
      gap: 8px;
      .hours-badge { padding: 4px 10px; background: rgba(36, 129, 204, 0.1); color: #2481CC; border-radius: 10px; font-size: 12px; font-weight: 800; }
      .points-badge { padding: 4px 10px; background: rgba(255, 152, 0, 0.1); color: #FF9800; border-radius: 10px; font-size: 12px; font-weight: 800; }
      .status-text { font-size: 12px; color: #94a3b8; font-weight: 700; margin-left: 4px; }
    }

    .unsettled-controls {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .hours-input-box {
        position: relative;
        input {
          width: 80px;
          height: 48px;
          background: #f8fafc;
          border: 1px solid #e2e8f0;
          border-radius: 14px;
          padding: 0 28px 0 12px;
          font-size: 16px;
          font-weight: 800;
          color: #1e293b;
          text-align: center;
          &:focus { border-color: #2481CC; outline: none; background: #fff; }
        }
        .unit { position: absolute; right: 12px; top: 50%; transform: translateY(-50%); color: #94a3b8; font-size: 13px; font-weight: 700; }
      }
      
      .settle-btn {
        height: 48px;
        padding: 0 20px;
        background: #0f172a;
        color: #fff;
        border: none;
        border-radius: 14px;
        font-size: 14px;
        font-weight: 800;
        cursor: pointer;
        transition: all 0.3s;
        &:hover { background: #00C853; transform: scale(1.05); }
        &:active { transform: scale(0.95); }
      }
    }
  }
}

@keyframes dotPulse { from { transform: scale(1); opacity: 1; } to { transform: scale(0.6); opacity: 0.3; } }

/* Bottom Nav */
.bottom-floating-nav {
  position: fixed;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  width: 90%;
  max-width: 500px;
  height: 72px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 28px;
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 0 20px;
  z-index: 1000;
  box-shadow: 0 20px 60px rgba(0,0,0,0.1);
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  position: relative;
  transition: all 0.3s;
  
  .icon-circle {
    width: 44px;
    height: 44px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 14px;
    font-size: 24px;
    color: #94a3b8;
    transition: all 0.3s;
  }
  
  .nav-label {
    font-size: 11px;
    font-weight: 800;
    color: #94a3b8;
    margin-top: 2px;
    transition: all 0.3s;
    opacity: 0;
  }

  &.is-active {
    .icon-circle { background: rgba(0, 200, 83, 0.1); color: #00C853; }
    .nav-label { opacity: 1; color: #00C853; }
    .active-dot {
      position: absolute;
      top: -4px;
      width: 4px;
      height: 4px;
      background: #00C853;
      border-radius: 50%;
    }
  }

  &:hover:not(.is-active) {
    transform: translateY(-2px);
    .icon-circle { color: #1e293b; }
    .nav-label { opacity: 1; color: #64748b; }
  }
}
</style>
