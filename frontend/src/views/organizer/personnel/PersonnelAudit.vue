<template>
  <div class="org-personnel-audit">
    <!-- 如果没有选择活动，显示活动选择器 -->
    <el-card v-if="!activityId" class="activity-selector-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="title">选择活动进行审核</span>
        </div>
      </template>
      <el-table :data="activities" v-loading="activitiesLoading" stripe style="width: 100%">
        <el-table-column prop="title" label="活动名称" min-width="200" />
        <el-table-column label="待审核人数" width="120" align="center">
          <template #default="{ row }">
            <el-badge :value="row.pendingCount || 0" :hidden="!row.pendingCount" type="danger">
              <el-tag :type="row.pendingCount > 0 ? 'warning' : 'info'">{{ row.pendingCount || 0 }} 人</el-tag>
            </el-badge>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="selectActivity(row.id)">进入审核</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 如果选择了活动，显示审核界面 -->
    <template v-else>
      <el-page-header @back="activityId = null" class="mb-20">
        <template #content>
          <span class="page-title">报名审核 - {{ activity.title }}</span>
        </template>
      </el-page-header>

      <el-row :gutter="20">
        <!-- 左侧统计 -->
        <el-col :span="6">
          <el-card shadow="hover" class="stat-sidebar">
            <div class="stat-group">
              <div class="stat-label">招募人数</div>
              <div class="stat-value">{{ activity.maxParticipants || '不限' }}</div>
            </div>
            <el-divider />
            <div class="stat-group">
              <div class="stat-label">当前已通过</div>
              <div class="stat-value success">{{ activity.currentParticipants || 0 }}</div>
            </div>
            <el-divider />
            <div class="stat-group">
              <div class="stat-label">待审核</div>
              <div class="stat-value warning">{{ totalPending }}</div>
            </div>
          </el-card>
        </el-col>

        <!-- 表格主体 -->
        <el-col :span="18">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <el-radio-group v-model="statusFilter" size="small" @change="fetchRegistrations">
                  <el-radio-button :label="null">全部</el-radio-button>
                  <el-radio-button :label="0">待审核</el-radio-button>
                  <el-radio-button :label="1">已通过</el-radio-button>
                  <el-radio-button :label="2">已签到</el-radio-button>
                  <el-radio-button :label="6">已拒绝</el-radio-button>
                </el-radio-group>
              </div>
            </template>

            <el-table :data="registrations" v-loading="loading" stripe style="width: 100%">
              <el-table-column label="志愿者" min-width="150">
                <template #default="{ row }">
                  <div class="volunteer-cell">
                    <el-avatar :size="32" :src="row.volunteerAvatar" />
                    <div class="info">
                      <div class="name">{{ row.volunteerName }}</div>
                      <div class="college">{{ row.college || '-' }}</div>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="phone" label="联系电话" width="130" />
              <el-table-column label="状态" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="REG_STATUS_MAP[row.status]?.type">{{ REG_STATUS_MAP[row.status]?.label }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="报名时间" width="160">
                <template #default="{ row }">
                  {{ formatDate(row.createTime) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150" fixed="right" align="center">
                <template #default="{ row }">
                  <template v-if="row.status === 0">
                    <el-button link type="success" @click="handleAudit(row.id, true)">通过</el-button>
                    <el-button link type="danger" @click="handleAudit(row.id, false)">拒绝</el-button>
                  </template>
                  <span v-else class="text-muted">已处理</span>
                </template>
              </el-table-column>
            </el-table>

            <div class="pagination-container mt-20">
              <el-pagination
                v-model:current-page="queryParams.page"
                v-model:page-size="queryParams.size"
                :total="total"
                layout="total, prev, pager, next"
                background
                @current-change="fetchRegistrations"
              />
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { request } from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const route = useRoute()
const activityId = ref<number | null>(route.query.activityId ? Number(route.query.activityId) : null)

// 列表模式数据
const activities = ref([])
const activitiesLoading = ref(false)

// 审核模式数据
const activity = ref<any>({})
const registrations = ref([])
const loading = ref(false)
const total = ref(0)
const totalPending = ref(0)
const statusFilter = ref<number | null>(0) // 默认看板待审核

const queryParams = reactive({
  page: 1,
  size: 10
})

const REG_STATUS_MAP: any = {
  0: { label: '待审核', type: 'warning' },
  1: { label: '已通过', type: 'success' },
  2: { label: '已签到', type: 'primary' },
  3: { label: '已完成', type: 'info' },
  4: { label: '已取消', type: 'info' },
  5: { label: '缺席', type: 'danger' },
  6: { label: '已拒绝', type: 'danger' }
}

const fetchActivitiesWithPending = async () => {
  activitiesLoading.value = true
  try {
    // 这里需要一个能返回待审核人数的接口，或者前端过滤
    // 暂时复用原来获取我的活动的接口
    const res = await request.get('/activity/my', { page: 1, size: 100 })
    // 这里后端 Activity 对象如果没有 pendingCount，可能需要后端支持
    // 或者我们直接显示列表，pendingCount 默认为 - (待后端完善)
    activities.value = res.data?.records || []
  } catch (error) {
    console.error('获取活动列表失败:', error)
  } finally {
    activitiesLoading.value = false
  }
}

const fetchActivity = async () => {
  if (!activityId.value) return
  try {
    const res = await request.get(`/activity/${activityId.value}`)
    activity.value = res.data || {}
  } catch (error) {
    console.error('获取活动信息失败:', error)
  }
}

const fetchRegistrations = async () => {
  if (!activityId.value) return
  loading.value = true
  try {
    const params: any = { ...queryParams }
    if (statusFilter.value !== null) params.status = statusFilter.value
    
    const res = await request.get(`/registration/activity/${activityId.value}`, params)
    registrations.value = res.data?.records || []
    total.value = res.data?.total || 0
    
    // 如果是看全部，统计下待审核
    if (statusFilter.value === null || statusFilter.value === 0) {
        // 简单模拟统计，实际最好后端返回
        totalPending.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取报名列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleAudit = async (id: number, pass: boolean) => {
  try {
    const action = pass ? '通过' : '拒绝'
    await ElMessageBox.confirm(`确定要${action}该志愿者的报名吗？`, '审核确认', {
      type: pass ? 'success' : 'warning'
    })
    
    await request.post('/registration/audit', { id, pass })
    ElMessage.success(`操作成功：已${action}`)
    fetchRegistrations()
    fetchActivity()
  } catch (e) {
    // cancel
  }
}

const selectActivity = (id: number) => {
  activityId.value = id
}

const formatDate = (date: string) => date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-'

watch(() => activityId.value, (newId) => {
  if (newId) {
    fetchActivity()
    fetchRegistrations()
  } else {
    fetchActivitiesWithPending()
  }
})

onMounted(() => {
  if (activityId.value) {
    fetchActivity()
    fetchRegistrations()
  } else {
    fetchActivitiesWithPending()
  }
})
</script>

<style scoped lang="scss">
.org-personnel-audit {
  padding: 10px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    .title { font-weight: bold; }
  }

  .stat-sidebar {
    .stat-group {
      padding: 10px 0;
      text-align: center;
      .stat-label { font-size: 13px; color: var(--el-text-color-secondary); margin-bottom: 8px; }
      .stat-value { 
        font-size: 24px; font-weight: bold; 
        &.success { color: var(--el-color-success); }
        &.warning { color: var(--el-color-warning); }
      }
    }
  }

  .volunteer-cell {
    display: flex;
    align-items: center;
    gap: 10px;
    .info {
      .name { font-weight: 600; font-size: 13px; }
      .college { font-size: 11px; color: var(--el-text-color-secondary); }
    }
  }

  .text-muted { color: var(--el-text-color-placeholder); font-size: 12px; }

  .pagination-container { display: flex; justify-content: center; }
}
</style>
