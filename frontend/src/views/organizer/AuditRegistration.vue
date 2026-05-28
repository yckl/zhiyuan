<template>
  <div class="audit-registration">
    <el-card class="main-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon><List /></el-icon>
            <span class="title">报名审核</span>
          </div>
        </div>
      </template>

      <!-- 筛选区 -->
      <div class="filter-bar">
        <el-select
          v-model="selectedActivityId"
          placeholder="选择活动"
          filterable
          clearable
          style="width: 280px"
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
              <el-badge
                v-if="item.pendingCount > 0"
                :value="item.pendingCount"
                type="warning"
                class="pending-badge"
              />
            </div>
          </el-option>
        </el-select>

        <el-input
          v-model="searchKeyword"
          placeholder="搜索姓名/学号"
          style="width: 180px"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <el-radio-group v-model="statusFilter" size="default" @change="fetchRegistrations">
          <el-radio-button :label="null">全部</el-radio-button>
          <el-radio-button :label="0">待审核</el-radio-button>
          <el-radio-button :label="1">已通过</el-radio-button>
          <el-radio-button :label="2">已签到</el-radio-button>
          <el-radio-button :label="6">已拒绝</el-radio-button>
        </el-radio-group>

        <div class="batch-actions">
          <el-button
            type="success"
            :disabled="selectedRows.length === 0"
            @click="handleBatchAudit(true)"
          >
            <el-icon><Check /></el-icon> 批量通过 ({{ selectedRows.length }})
          </el-button>
          <el-button
            type="danger"
            :disabled="selectedRows.length === 0"
            @click="handleBatchAudit(false)"
          >
            <el-icon><Close /></el-icon> 批量驳回
          </el-button>
        </div>
      </div>

      <!-- 活动概览 -->
      <div class="activity-overview" v-if="activity.id">
        <div class="overview-left">
          <h3>{{ activity.title }}</h3>
          <div class="meta">
            <span><el-icon><Clock /></el-icon> {{ formatDate(activity.startTime) }}</span>
            <span><el-icon><Location /></el-icon> {{ activity.location || '待定' }}</span>
          </div>
        </div>
        <div class="overview-stats">
          <div class="stat">
            <span class="value success">{{ activity.currentParticipants || 0 }}</span>
            <span class="label">已通过</span>
          </div>
          <el-divider direction="vertical" />
          <div class="stat">
            <span class="value">{{ activity.maxParticipants || '-' }}</span>
            <span class="label">招募上限</span>
          </div>
          <el-divider direction="vertical" />
          <div class="stat">
            <span class="value warning">{{ pendingCount }}</span>
            <span class="label">待审</span>
          </div>
        </div>
      </div>

      <!-- 表格 -->
      <el-table
        ref="tableRef"
        :data="filteredRegistrations"
        v-loading="loading"
        stripe
        style="width: 100%"
        class="hidden-sm-and-down"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" :selectable="row => row.status === 0" />

        <el-table-column label="志愿者" min-width="180">
          <template #default="{ row }">
            <div class="volunteer-cell">
              <el-avatar :size="40" :src="row.volunteerAvatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <div class="info">
                <div class="name">{{ row.volunteerName || '未知' }}</div>
                <div class="student-id">{{ row.studentId || '-' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="phone" label="联系电话" width="130">
          <template #default="{ row }">
            <span>{{ row.phone || '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="学院/专业" min-width="160">
          <template #default="{ row }">
            <div class="college-cell">
              <div>{{ row.college || '-' }}</div>
              <div class="major">{{ row.major || '' }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="报名时间" width="160">
          <template #default="{ row }">
            <div class="time-cell">
              <el-icon><Calendar /></el-icon>
              <span>{{ formatDate(row.createTime) }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="REG_STATUS_MAP[row.status]?.type" effect="light">
              {{ REG_STATUS_MAP[row.status]?.label }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button
                type="success"
                size="small"
                @click="handleAudit(row.id, true)"
                :loading="auditingId === row.id"
              >
                <el-icon><Check /></el-icon> 通过
              </el-button>
              <el-button
                type="danger"
                size="small"
                @click="showRejectDialog(row)"
                :loading="auditingId === row.id"
              >
                <el-icon><Close /></el-icon> 驳回
              </el-button>
            </template>
            <span v-else class="text-muted">已处理</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- Mobile Card List -->
      <div class="hidden-md-and-up mobile-card-list">
         <div v-for="item in filteredRegistrations" :key="item.id" class="mobile-card">
            <div class="card-header-mobile">
               <div class="volunteer-info">
                  <el-avatar :size="36" :src="item.volunteerAvatar">
                     <el-icon><User /></el-icon>
                  </el-avatar>
                  <span class="volunteer-name">{{ item.volunteerName || '未知' }}</span>
               </div>
               <el-tag :type="REG_STATUS_MAP[item.status]?.type" size="small" effect="light">
                  {{ REG_STATUS_MAP[item.status]?.label }}
               </el-tag>
            </div>
            
            <div class="card-body-mobile">
               <div class="info-row">
                  <span class="label">学号:</span>
                  <span class="value">{{ item.studentId || '-' }}</span>
               </div>
               <div class="info-row">
                  <span class="label">电话:</span>
                  <span class="value">{{ item.phone || '-' }}</span>
               </div>
               <div class="info-row">
                  <span class="label">学院:</span>
                  <span class="value">{{ item.college || '-' }} {{ item.major ? ` / ${item.major}` : '' }}</span>
               </div>
               <div class="info-row">
                  <span class="label">时间:</span>
                  <span class="value">{{ formatDate(item.createTime) }}</span>
               </div>
            </div>

            <div class="card-footer-mobile" v-if="item.status === 0">
               <el-button
                  type="success"
                  size="small"
                  @click="handleAudit(item.id, true)"
                  :loading="auditingId === item.id"
                  class="action-btn"
               >
                  <el-icon><Check /></el-icon> 通过
               </el-button>
               <el-button
                  type="danger"
                  size="small"
                  @click="showRejectDialog(item)"
                  :loading="auditingId === item.id"
                  class="action-btn"
               >
                  <el-icon><Close /></el-icon> 驳回
               </el-button>
            </div>
         </div>
      </div>

      <el-empty
        v-if="!loading && filteredRegistrations.length === 0"
        :description="selectedActivityId ? '暂无报名记录' : '请先选择一个活动'"
        :image-size="100"
      />

      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          :layout="isMobile ? 'prev, pager, next' : 'total, sizes, prev, pager, next, jumper'"
          :pager-count="isMobile ? 5 : 7"
          :small="isMobile"
          background
          @size-change="fetchRegistrations"
          @current-change="fetchRegistrations"
        />
      </div>
    </el-card>

    <!-- 驳回理由弹窗 -->
    <el-dialog v-model="rejectDialogVisible" title="驳回理由" width="400px">
      <el-input
        v-model="rejectReason"
        type="textarea"
        :rows="3"
        placeholder="请输入驳回理由（可选）"
      />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject" :loading="auditingId !== null">
          确认驳回
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  List, Search, Check, Close, Clock, Location, User, Calendar
} from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import dayjs from 'dayjs'

import { useMobile } from '@/composables/useMobile'
const { isMobile } = useMobile()

const route = useRoute()
const tableRef = ref()
const loading = ref(false)

// 活动列表
const activities = ref<any[]>([])
const selectedActivityId = ref<number | null>(
  route.query.activityId ? Number(route.query.activityId) : null
)
const activity = ref<any>({})

// 报名列表
const registrations = ref<any[]>([])
const total = ref(0)
const pendingCount = ref(0)

// 筛选?
const statusFilter = ref<number | null>(0) // 默认显示待审核
const searchKeyword = ref('')
const selectedRows = ref<any[]>([])
const auditingId = ref<number | null>(null)

// 驳回弹窗
const rejectDialogVisible = ref(false)
const rejectReason = ref('')
const currentRejectRow = ref<any>(null)

const queryParams = reactive({
  page: 1,
  size: 20
})

const REG_STATUS_MAP: Record<number, { label: string; type: string }> = {
  0: { label: '待审核', type: 'warning' },
  1: { label: '已通过', type: 'success' },
  2: { label: '已签到', type: 'primary' },
  3: { label: '已完成', type: 'info' },
  4: { label: '已取消', type: 'info' },
  5: { label: '缺席', type: 'danger' },
  6: { label: '已拒绝', type: 'danger' }
}

// 搜索过滤
const filteredRegistrations = computed(() => {
  if (!searchKeyword.value) return registrations.value
  const keyword = searchKeyword.value.toLowerCase()
  return registrations.value.filter(
    r =>
      (r.volunteerName && r.volunteerName.toLowerCase().includes(keyword)) ||
      (r.studentId && r.studentId.includes(keyword))
  )
})

const formatDate = (date: string) => (date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-')

// 获取我的活动列表
const fetchActivities = async () => {
  try {
    const res = await request.get('/activity/my', { page: 1, size: 100 })
    activities.value = res.data?.records || []
    
    // 如果没有预选活动但有活动列表，自动选第一行
    if (!selectedActivityId.value && activities.value.length > 0) {
      selectedActivityId.value = activities.value[0].id
    }
  } catch (error) {
    console.error('获取活动列表失败:', error)
  }
}

// 获取活动详情
const fetchActivity = async () => {
  if (!selectedActivityId.value) {
    activity.value = {}
    return
  }
  try {
    const res = await request.get(`/activity/${selectedActivityId.value}`)
    activity.value = res.data || {}
  } catch (error) {
    console.error('获取活动信息失败:', error)
  }
}

// 获取报名列表
const fetchRegistrations = async () => {
  if (!selectedActivityId.value) {
    registrations.value = []
    total.value = 0
    return
  }
  
  loading.value = true
  try {
    const params: any = { ...queryParams }
    if (statusFilter.value !== null) {
      params.status = statusFilter.value
    }

    const res = await request.get(`/registration/activity/${selectedActivityId.value}`, params)
    registrations.value = res.data?.records || res.data || []
    total.value = res.data?.total || registrations.value.length
    
    // 统计待审核数
    if (statusFilter.value === 0) {
      pendingCount.value = total.value
    } else {
      // 额外请求统计待审核数
      const pendingRes = await request.get(`/registration/activity/${selectedActivityId.value}`, { status: 0, page: 1, size: 1 })
      pendingCount.value = pendingRes.data?.total || 0
    }
  } catch (error) {
    console.error('获取报名列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleActivityChange = () => {
  queryParams.page = 1
  selectedRows.value = []
  fetchActivity()
  fetchRegistrations()
}

const handleSearch = () => {
  // 前端过滤，无需请求
}

const handleSelectionChange = (rows: any[]) => {
  selectedRows.value = rows.filter(r => r.status === 0)
}

// 单个审核
const handleAudit = async (id: number, pass: boolean) => {
  try {
    await ElMessageBox.confirm(
      `确定${pass ? '通过' : '驳回'}该志愿者的报名申请吗？`,
      '审核确认',
      { type: pass ? 'success' : 'warning' }
    )

    auditingId.value = id
    await request.post('/registration/audit', { id, pass })
    ElMessage.success(`${pass ? '通过' : '驳回'}报名申请`)
    fetchRegistrations()
    fetchActivity()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('审核失败:', error)
    }
  } finally {
    auditingId.value = null
  }
}

// 显示驳回弹窗
const showRejectDialog = (row: any) => {
  currentRejectRow.value = row
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

// 确认驳回
const confirmReject = async () => {
  if (!currentRejectRow.value) return
  
  try {
    auditingId.value = currentRejectRow.value.id
    await request.post('/registration/audit', {
      id: currentRejectRow.value.id,
      pass: false,
      reason: rejectReason.value
    })
    ElMessage.success('已驳回报名申请')
    rejectDialogVisible.value = false
    fetchRegistrations()
    fetchActivity()
  } catch (error) {
    console.error('驳回失败:', error)
  } finally {
    auditingId.value = null
  }
}

// 批量审核
const handleBatchAudit = async (pass: boolean) => {
  if (selectedRows.value.length === 0) return
  
  const action = pass ? '通过' : '驳回'
  try {
    await ElMessageBox.confirm(
      `确定要批${action}选中 ${selectedRows.value.length} 条报名吗？`,
      '批量审核',
      { type: pass ? 'success' : 'warning' }
    )

    const ids = selectedRows.value.map(r => r.id)
    await request.post('/registration/audit/batch', { ids, pass })
    ElMessage.success(`已批${action} ${ids.length} 条报名`)
    selectedRows.value = []
    fetchRegistrations()
    fetchActivity()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('批量审核失败:', error)
    }
  }
}

watch(selectedActivityId, (newId) => {
  if (newId) {
    handleActivityChange()
  }
})

onMounted(() => {
  fetchActivities()
  if (selectedActivityId.value) {
    fetchActivity()
    fetchRegistrations()
  }
})
</script>

<style lang="scss" scoped>
.audit-registration {
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

  .filter-bar {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 16px;
    border-bottom: 1px solid var(--el-border-color-lighter);

    .batch-actions {
      margin-left: auto;
      display: flex;
      gap: 8px;
    }
  }

  .activity-option {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
  }

  .activity-overview {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    background: linear-gradient(135deg, #0093E9 0%, #80D0C7 100%);
    border-radius: 10px;
    margin-bottom: 20px;
    color: white;
    box-shadow: 0 8px 20px rgba(0, 147, 233, 0.15);

    .overview-left {
      h3 {
        margin: 0 0 8px;
        font-size: 18px;
      }

      .meta {
        display: flex;
        gap: 16px;
        font-size: 13px;
        opacity: 0.9;

        span {
          display: flex;
          align-items: center;
          gap: 4px;
        }
      }
    }

    .overview-stats {
      display: flex;
      align-items: center;
      gap: 16px;
      background: rgba(255, 255, 255, 0.15);
      padding: 12px 20px;
      border-radius: 8px;

      .stat {
        text-align: center;

        .value {
          font-size: 24px;
          font-weight: 700;

          &.success { color: #95f3c3; }
          &.warning { color: #ffd93d; }
        }

        .label {
          font-size: 12px;
          opacity: 0.8;
        }
      }

      .el-divider {
        background: rgba(255,255,255,0.3);
        height: 30px;
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

  .college-cell {
    .major {
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }
  }

  .time-cell {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: var(--el-text-color-regular);

    .el-icon {
      color: var(--el-color-primary);
    }
  }

  .text-muted {
    color: var(--el-text-color-placeholder);
    font-size: 12px;
  }

  .pagination-wrapper {
    display: flex;
    justify-content: center;
    margin-top: 20px;
    padding-top: 16px;
    border-top: 1px solid var(--el-border-color-lighter);
  }

  /* Mobile Adaptation */
  @media only screen and (max-width: 768px) {
    .audit-registration { padding: 4px; }
    
    .filter-bar {
      flex-direction: column;
      align-items: stretch;
      gap: 10px;
      
      .el-select, .el-input { width: 100% !important; }
      
      .batch-actions {
        width: 100%;
        display: flex;
        gap: 10px;
        
        button { flex: 1; }
      }
    }
    
    .activity-overview {
      flex-direction: column;
      gap: 12px;
      text-align: center;
      
      .overview-left {
        h3 { font-size: 16px; }
        .meta { 
          flex-direction: column; 
          gap: 4px; 
          align-items: center; 
        }
      }
      
      .overview-stats { width: 100%; justify-content: space-around; }
    }
    
    .mobile-card-list {
      background: var(--bg-page);
      padding: 4px;
      
      .mobile-card {
        background: var(--bg-card);
        border: 1px solid var(--border-light);
        border-radius: 8px;
        padding: 12px;
        margin-bottom: 12px;
        box-shadow: var(--shadow-light);
        
        .card-header-mobile {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 12px;
          padding-bottom: 12px;
          border-bottom: 1px solid var(--border-light);
          
          .volunteer-info {
            display: flex;
            align-items: center;
            gap: 8px;
            
            .volunteer-name {
              font-weight: bold;
              font-size: 14px;
            }
          }
        }
        
        .card-body-mobile {
          .info-row {
            display: flex;
            margin-bottom: 8px;
            font-size: 13px;
            
            .label {
              color: var(--text-secondary);
              width: 50px;
              flex-shrink: 0;
            }
            
            .value {
              color: var(--text-primary);
              flex: 1;
              word-break: break-all;
            }
          }
        }
        
        .card-footer-mobile {
          margin-top: 12px;
          padding-top: 12px;
          border-top: 1px solid var(--border-light);
          display: flex;
          gap: 10px;
          
          .action-btn { flex: 1; }
        }
      }
    }
  }
}
</style>
