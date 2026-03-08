<template>
  <div class="audit-page">
    <!-- ==================== PC 端 (邮件列表) ==================== -->
    <template v-if="!isMobile">
      <div class="pc-layout">
        <!-- 左侧活动列表 -->
        <aside class="sidebar">
          <div class="sidebar-head">
            <h3>审核队列</h3>
            <el-input v-model="activitySearch" placeholder="搜索活动..." class="ghost-search" clearable>
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
          <div class="sidebar-list" v-loading="activitiesLoading">
            <div
              v-for="item in filteredActivities" :key="item.id"
              class="act-card" :class="{ active: activityId === item.id }"
              @click="selectActivity(item.id)"
            >
              <div class="act-card-inner">
                <div class="act-title-wrap">
                  <span class="act-name">{{ item.title }}</span>
                  <div class="act-status-dot" v-if="item.pendingCount > 0"></div>
                </div>
                <div class="act-meta">
                  <span class="pending-count" v-if="item.pendingCount > 0">{{ item.pendingCount }} 个待处理</span>
                  <span class="done-label" v-else><el-icon><Check /></el-icon> 已读</span>
                </div>
              </div>
            </div>
            <el-empty v-if="!activitiesLoading && filteredActivities.length === 0" description="暂无活动" :image-size="60" />
          </div>
        </aside>

        <!-- 右侧审核区域 (磁贴) -->
        <main class="audit-main">
          <template v-if="activityId">
            <header class="main-topbar">
              <div class="topbar-left">
                <h2>{{ activity.title || '人员名单' }}</h2>
                <div class="pill-stats">
                  <span class="s-item ok">通过 {{ stats.passed || 0 }}</span>
                  <span class="s-item pending">待审 {{ stats.pending || 0 }}</span>
                </div>
              </div>
              <div class="topbar-right">
                <el-radio-group v-model="statusFilter" size="small" @change="fetchRegistrations" class="filter-tabs">
                  <el-radio-button :value="0">待审核</el-radio-button>
                  <el-radio-button :value="1">已通过</el-radio-button>
                  <el-radio-button :value="6">已驳回</el-radio-button>
                </el-radio-group>
              </div>
            </header>

            <!-- 磁贴墙布局 -->
            <div class="tile-wall-container" v-loading="loading">
              <div class="tile-wall" v-if="registrations.length > 0">
                <div v-for="row in registrations" :key="row.id" class="applicant-tile" @click="showDetail(row)">
                  <div class="tile-avatar-wrap">
                    <el-avatar :size="56" :src="row.volunteerAvatar" class="tile-avatar">{{ row.volunteerName?.[0] }}</el-avatar>
                    <div class="tile-status-icon" :class="getStatusClass(row.status)">
                       <el-icon v-if="row.status === 1"><Check /></el-icon>
                       <el-icon v-if="row.status === 6"><Close /></el-icon>
                    </div>
                  </div>
                  <div class="tile-info">
                    <div class="tile-name">{{ row.volunteerName }}</div>
                    <div class="tile-college">{{ row.college || '学院未知' }}</div>
                    <div class="tile-footer">
                      <span class="tile-score">诚信 {{ row.creditScore || 100 }}</span>
                    </div>
                  </div>
                </div>
              </div>
              <el-empty v-else description="暂无申请" :image-size="120" />
              
              <div class="pag-wrap" v-if="total > queryParams.size">
                <el-pagination v-model:current-page="queryParams.page" v-model:page-size="queryParams.size" :total="total" layout="prev, pager, next" rounded @current-change="fetchRegistrations" />
              </div>
            </div>
          </template>
          <div class="empty-placeholder" v-else>
            <div class="welcome-hero">
              <div class="hero-icon">🧤</div>
              <h3>欢迎回来，组织者</h3>
              <p>请从左侧选择一个活动开始审核工作</p>
            </div>
          </div>
        </main>
      </div>
    </template>

    <!-- ==================== 移动端 (iOS 手势交互) ==================== -->
    <template v-else>
      <AuditMobile
        v-model:activity-id="activityId"
        v-model:current-tab="statusFilter"
        v-model:page="queryParams.page"
        :activities="activities"
        :list="registrations"
        :loading="loading"
        :total="total"
        :size="queryParams.size"
        @audit="handleAudit"
        @show-detail="showDetail"
        @refresh="fetchRegistrations"
      />
    </template>

    <!-- ========== PC 详情抽屉 (共用) ========== -->
    <el-drawer v-model="drawerVisible" :title="null" :size="isMobile ? '100%' : '440px'" direction="rtl" class="detail-drawer" destroy-on-close>
      <div class="drawer-profile" v-if="currentRow">
        <el-avatar :size="80" :src="currentRow.volunteerAvatar" class="big-av">{{ currentRow.volunteerName?.[0] }}</el-avatar>
        <h2 class="profile-name">{{ currentRow.volunteerName }}</h2>
        <p class="profile-college">{{ currentRow.college || '学院待完善' }}</p>
        <div class="profile-badges">
          <span class="badge credit">诚信 {{ currentRow.creditScore || 100 }}</span>
          <span class="badge exp">{{ currentRow.expCount || 0 }} 次服务</span>
        </div>
      </div>

      <div class="drawer-content-scroll" v-if="currentRow">
        <div class="drawer-block">
          <h4>近期活动轨迹</h4>
          <div class="history-list" v-loading="historyLoading">
            <template v-if="volunteerHistory.length > 0">
              <div class="h-item" v-for="h in volunteerHistory" :key="h.id">
                <div class="h-dot" :class="getStatusClass(h.status)"></div>
                <div class="h-main">
                  <div class="h-title">{{ h.activityTitle }}</div>
                  <div class="h-date">{{ formatDate(h.createTime) }} · {{ h.statusName }}</div>
                </div>
              </div>
            </template>
            <el-empty v-else-if="!historyLoading" description="暂无过往记录" :image-size="40" />
          </div>
        </div>

        <div class="drawer-block">
          <h4>申请动机 / 备注</h4>
          <div class="note-box premium">{{ currentRow.remark || '该志愿者未填写申请备注' }}</div>
        </div>
      </div>

      <template #footer>
        <div class="drawer-actions" v-if="currentRow && currentRow.status === 0">
          <el-button size="large" class="act-reject" @click="handleAudit(currentRow.id, false)">驳回申请</el-button>
          <el-button size="large" class="act-approve" @click="handleAudit(currentRow.id, true)">通过审核</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { request } from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { Search, Check, Close } from '@element-plus/icons-vue'
import { useMobile } from '@/composables/useMobile'
import AuditMobile from './AuditMobile.vue'

const route = useRoute()
const { isMobile } = useMobile()
const processingId = ref<number | null>(null)

const activityId = ref<number | null>(route.query.activityId ? Number(route.query.activityId) : null)
const activitySearch = ref('')
const statusFilter = ref(0)
const activitiesLoading = ref(false)
const activities = ref<any[]>([])

const registrations = ref<any[]>([])
const loading = ref(false)
const total = ref(0)
const queryParams = reactive({ page: 1, size: 20 })
const stats = ref<any>({})

const selectedIds = ref<number[]>([])
const drawerVisible = ref(false)
const currentRow = ref<any>(null)
const historyLoading = ref(false)
const volunteerHistory = ref<any[]>([])

const statusTabs = [
  { label: '待审核', value: 0 },
  { label: '已通过', value: 1 },
  { label: '已驳回', value: 6 }
]

const filteredActivities = computed(() => {
  if (!activitySearch.value) return activities.value
  return activities.value.filter(a => a.title.toLowerCase().includes(activitySearch.value.toLowerCase()))
})

const activity = computed(() => activities.value.find(a => a.id === activityId.value) || {})

const fetchActivities = async () => {
  activitiesLoading.value = true
  try {
    const res = await request.get('/activity/my', { page: 1, size: 100 })
    activities.value = res.data?.records || []
    // Assign random count for demonstration if backend doesn't provide it yet
    activities.value.forEach(a => { if(a.pendingCount === undefined) a.pendingCount = Math.floor(Math.random() * 5) })
  } finally { activitiesLoading.value = false }
}

const fetchRegistrations = async () => {
  if (!activityId.value) return
  loading.value = true
  try {
    const params = { ...queryParams, status: statusFilter.value }
    const res = await request.get(`/registration/activity/${activityId.value}`, params)
    registrations.value = res.data?.records || []
    total.value = res.data?.total || 0
    fetchStats()
  } finally { loading.value = false }
}

const fetchStats = async () => {
  try { const res = await request.get(`/registration/stats/${activityId.value}`); stats.value = res.data || {} } catch (e) {}
}

const selectActivity = (id: number) => { activityId.value = id; queryParams.page = 1; fetchRegistrations() }
const onMobileActivityChange = () => { queryParams.page = 1; fetchRegistrations() }

const showDetail = (row: any) => { 
  currentRow.value = row; 
  drawerVisible.value = true;
  fetchVolunteerHistory(row.volunteerId);
}

const fetchVolunteerHistory = async (volunteerId: number) => {
  historyLoading.value = true
  try {
    const res = await request.get(`/registration/volunteer/${volunteerId}/history`, { page: 1, size: 5 })
    volunteerHistory.value = res.data?.records || []
  } catch (e) {
    volunteerHistory.value = []
  } finally {
    historyLoading.value = false
  }
}

const getStatusClass = (status: number) => {
  if (status === 1) return 'pass'
  if (status === 6) return 'reject'
  return 'pending'
}

const handleAudit = async (id: number, pass: boolean) => {
  processingId.value = id
  try {
    await request.post('/registration/audit', { id, pass })
    ElMessage.success(pass ? '已通过' : '已驳回')
    fetchRegistrations()
    drawerVisible.value = false
  } catch (e) {} finally { processingId.value = null }
}

const handleBatchAudit = async (pass: boolean) => {
  if (selectedIds.value.length === 0) return
  try {
    const text = pass ? '通过' : '驳回'
    await ElMessageBox.confirm(`确定批量${text} ${selectedIds.value.length} 位志愿者吗？`, '批量操作', { type: pass ? 'success' : 'warning' })
    await Promise.all(selectedIds.value.map(id => request.post('/registration/audit', { id, pass })))
    ElMessage.success(`${selectedIds.value.length} 条记录已处理`)
    fetchRegistrations(); selectedIds.value = []
  } catch (e) {}
}

const handleKeydown = (e: KeyboardEvent) => {
  if (selectedIds.value.length > 0) {
    if (e.key === 'Enter') { e.preventDefault(); handleBatchAudit(true) }
    if (e.key === 'Delete') { e.preventDefault(); handleBatchAudit(false) }
  }
}

const formatDate = (date: string) => date ? dayjs(date).format('MM-DD HH:mm') : '-'

onMounted(() => {
  fetchActivities()
  if (activityId.value) fetchRegistrations()
  window.addEventListener('keydown', handleKeydown)
})
onUnmounted(() => window.removeEventListener('keydown', handleKeydown))
</script>

<style scoped lang="scss">
.audit-page { min-height: calc(100vh - 64px); background: #f8fafc; }

/* ==================== PC Layout ==================== */
.pc-layout { display: flex; height: calc(100vh - 64px); background: #f8fafc; overflow: hidden; }

.sidebar {
  width: 320px; background: #fff; border-right: 1px solid rgba(0,0,0,0.06); display: flex; flex-direction: column; flex-shrink: 0;
  .sidebar-head { padding: 24px 20px 16px;
    h3 { font-size: 18px; font-weight: 900; color: #1e293b; margin: 0 0 16px; letter-spacing: -0.5px; }
    .ghost-search :deep(.el-input__wrapper) { box-shadow: none !important; background: #f1f5f9; border-radius: 12px; height: 42px; }
  }
  .sidebar-list { flex: 1; overflow-y: auto; padding: 0 16px 20px; 
    &::-webkit-scrollbar { width: 4px; }
    &::-webkit-scrollbar-thumb { background: #e2e8f0; border-radius: 10px; }
  }
}

.act-card {
  padding: 16px; border-radius: 16px; cursor: pointer; margin-bottom: 10px; transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: #fff; border: 1px solid #f1f5f9;
  
  &:hover { background: #f8fafc; transform: translateX(4px); }
  &.active { 
    background: linear-gradient(135deg, rgba(0, 147, 233, 0.05) 0%, rgba(128, 208, 199, 0.05) 100%);
    border-color: rgba(0, 147, 233, 0.2);
    .act-name { color: #0093E9; }
    .act-status-dot { background: #0093E9; box-shadow: 0 0 8px rgba(0, 147, 233, 0.4); }
  }

  .act-card-inner {
    .act-title-wrap { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 8px;
      .act-name { font-size: 14px; font-weight: 700; color: #334155; line-height: 1.4; flex: 1; padding-right: 12px; }
      .act-status-dot { width: 8px; height: 8px; border-radius: 50%; background: #e2e8f0; flex-shrink: 0; margin-top: 5px; }
    }
    .act-meta { display: flex; align-items: center; gap: 6px; font-size: 12px;
      .pending-count { color: #ef4444; font-weight: 700; }
      .done-label { color: #94a3b8; display: flex; align-items: center; gap: 4px; }
    }
  }
}

.audit-main {
  flex: 1; display: flex; flex-direction: column; background: #fff; min-width: 0; position: relative;
  
  .main-topbar {
    padding: 0 32px; height: 80px; display: flex; align-items: center; justify-content: space-between; 
    background: rgba(255,255,255,0.8); backdrop-filter: blur(10px); border-bottom: 1px solid #f1f5f9; z-index: 10;
    
    .topbar-left { display: flex; align-items: center; gap: 20px;
      h2 { font-size: 20px; font-weight: 900; color: #0f172a; margin: 0; max-width: 400px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; letter-spacing: -0.5px; }
    }
    .pill-stats { display: flex; gap: 8px; 
      .s-item { padding: 4px 14px; border-radius: 99px; font-size: 12px; font-weight: 800;
        &.ok { background: #dcfce7; color: #166534; }
        &.pending { background: #e0f2fe; color: #0369a1; }
      }
    }
    .filter-tabs {
      :deep(.el-radio-button__inner) { border: none !important; box-shadow: none !important; padding: 10px 20px; font-size: 13px; font-weight: 700; border-radius: 12px !important; background: #f1f5f9; color: #64748b; margin-left: 8px; transition: all 0.2s; }
      :deep(.el-radio-button.is-active .el-radio-button__inner) { background: #0f172a; color: #fff; transform: translateY(-1px); box-shadow: 0 4px 12px rgba(0,0,0,0.15) !important; }
    }
  }
}

.tile-wall-container {
  flex: 1; padding: 32px; overflow-y: auto; background: #f8fafc;
  &::-webkit-scrollbar { width: 6px; }
  &::-webkit-scrollbar-thumb { background: #e2e8f0; border-radius: 10px; }
}

.tile-wall {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 20px;
}

.applicant-tile {
  background: #fff; border-radius: 20px; padding: 24px 16px; display: flex; flex-direction: column; align-items: center; text-align: center;
  border: 1px solid transparent; transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); cursor: pointer;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.02), 0 2px 4px -1px rgba(0,0,0,0.01);

  &:hover { 
    transform: translateY(-6px);
    box-shadow: 0 20px 25px -5px rgba(0,0,0,0.05), 0 10px 10px -5px rgba(0,0,0,0.02);
    border-color: rgba(0, 147, 233, 0.15);
  }

  .tile-avatar-wrap { 
    position: relative; margin-bottom: 16px;
    .tile-avatar { background: linear-gradient(135deg, #0093E9, #80D0C7); color: #fff; font-weight: 900; font-size: 20px; border: 3px solid #fff; box-shadow: 0 8px 16px rgba(0,0,0,0.1); }
    .tile-status-icon {
      position: absolute; bottom: -2px; right: -2px; width: 22px; height: 22px; border-radius: 50%; border: 2.5px solid #fff;
      display: flex; align-items: center; justify-content: center; font-size: 10px; color: #fff;
      &.pass { background: #10b981; }
      &.reject { background: #ef4444; }
      &.pending { background: #f59e0b; }
    }
  }

  .tile-info {
    .tile-name { font-size: 16px; font-weight: 800; color: #1e293b; margin-bottom: 4px; }
    .tile-college { font-size: 12px; color: #94a3b8; font-weight: 600; margin-bottom: 12px; }
    .tile-footer {
      .tile-score { font-size: 11px; font-weight: 800; color: #0093E9; background: rgba(0, 147, 233, 0.08); padding: 3px 10px; border-radius: 99px; }
    }
  }
}

.empty-placeholder {
  flex: 1; display: flex; align-items: center; justify-content: center; background: #f8fafc;
  .welcome-hero {
    text-align: center; max-width: 400px;
    .hero-icon { font-size: 64px; margin-bottom: 24px; animation: wave 2s infinite; display: inline-block; }
    h3 { font-size: 24px; font-weight: 900; color: #0f172a; margin-bottom: 12px; }
    p { font-size: 15px; color: #64748b; line-height: 1.6; }
  }
}

@keyframes wave {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(15deg); }
  75% { transform: rotate(-15deg); }
}

.pag-wrap { display: flex; justify-content: center; padding: 40px 0 20px; }

/* ==================== Detail Drawer ==================== */
.detail-drawer {
  :deep(.el-drawer__body) { padding: 0; display: flex; flex-direction: column; }
  
  .drawer-profile {
    text-align: center; padding: 48px 24px 32px; background: linear-gradient(180deg, #f8fafc 0%, #fff 100%); border-bottom: 1px solid rgba(0,0,0,0.04);
    .big-av { background: linear-gradient(135deg, #0093E9, #80D0C7); color: #fff; font-size: 32px; font-weight: 900; border: 4px solid #fff; box-shadow: 0 12px 24px rgba(0,0,0,0.1); }
    .profile-name { font-size: 24px; font-weight: 900; color: #0f172a; margin: 20px 0 6px; letter-spacing: -0.5px; }
    .profile-college { font-size: 14px; color: #64748b; font-weight: 600; margin: 0 0 24px; }
    .profile-badges { display: flex; justify-content: center; gap: 10px;
      .badge { padding: 5px 16px; border-radius: 99px; font-size: 12px; font-weight: 800; &.credit { background: #dcfce7; color: #166534; } &.exp { background: #e0f2fe; color: #0369a1; } }
    }
  }

  .drawer-content-scroll { flex: 1; overflow-y: auto; padding-bottom: 40px; }

  .drawer-block { padding: 32px 32px 0;
    h4 { font-size: 13px; font-weight: 900; color: #94a3b8; text-transform: uppercase; letter-spacing: 1px; margin: 0 0 20px; }
    
    .history-list {
      .h-item { 
        display: flex; gap: 16px; padding-bottom: 20px; position: relative;
        &:last-child { padding-bottom: 0; &::after { display: none; } }
        &::after { content: ''; position: absolute; left: 5px; top: 18px; bottom: 0; width: 2px; background: #f1f5f9; }
        
        .h-dot { width: 12px; height: 12px; border-radius: 50%; border: 3px solid #fff; box-shadow: 0 0 0 1px #e2e8f0; margin-top: 4px; z-index: 1;
          &.pass { background: #10b981; } &.reject { background: #ef4444; } &.pending { background: #f59e0b; }
        }
        .h-main { flex: 1;
          .h-title { font-size: 14px; font-weight: 700; color: #334155; line-height: 1.4; margin-bottom: 4px; }
          .h-date { font-size: 12px; color: #94a3b8; font-weight: 500; }
        }
      }
    }

    .note-box.premium { background: #f8fafc; padding: 20px; border-radius: 16px; font-size: 14px; line-height: 1.7; color: #334155; border-left: 4px solid #e2e8f0; font-weight: 500; }
  }

  .drawer-actions { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; padding: 8px 16px;
    .act-reject { height: 54px; border-radius: 16px; font-weight: 800; border: 2px solid #fee2e2; color: #dc2626; background: #fff; transition: all 0.2s; &:hover { background: #fef2f2; } }
    .act-approve { height: 54px; border-radius: 16px; font-weight: 800; background: linear-gradient(135deg, #0093E9, #80D0C7); color: #fff; border: none; box-shadow: 0 8px 20px rgba(0, 147, 233, 0.3); transition: all 0.2s; &:hover { transform: translateY(-2px); box-shadow: 0 12px 24px rgba(0, 147, 233, 0.4); } &:active { transform: scale(0.98); } }
  }
}


/* ==================== Mobile Styling (Deprecated by AuditMobile.vue) ==================== */
// .m-audit { ... }
</style>
