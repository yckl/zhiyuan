<template>
  <div class="my-activities">

    <!-- ==================== 极简白底 Header ==================== -->
    <div class="page-header">
      <div class="header-top">
        <el-button link class="back-btn" @click="router.back()">
          <el-icon><ArrowLeft /></el-icon> 返回
        </el-button>
        <span class="total-count">共 {{ total }} 项</span>
      </div>
    </div>

    <!-- 核心居中容器 -->
    <div class="content-container">
      <!-- 统计卡片 -->
      <div class="stats-section">
        <div class="stat-card" v-for="s in summaryStats" :key="s.label">
          <div class="num">{{ s.value }}</div>
          <div class="label">{{ s.label }}</div>
        </div>
      </div>

      <!-- 列表内容区卡片 -->
      <div class="list-section-card">
        <!-- ==================== 搜索 + 筛选 ==================== -->
        <div class="search-bar-wrapper">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索活动名称..."
            clearable
            :prefix-icon="Search"
            class="search-bar"
          />
        </div>

        <div class="filter-tabs">
          <span
            v-for="tab in filterTabList"
            :key="tab.value"
            class="filter-tab"
            :class="{ active: activeFilter === tab.value }"
            @click="activeFilter = tab.value"
          >{{ tab.label }}</span>
        </div>

        <!-- ==================== 列表区域 ==================== -->
        <div class="list-container" v-loading="loading">

      <!-- 骨架屏 -->
      <div v-if="loading && registrations.length === 0" class="skeleton-list">
        <div v-for="i in 5" :key="i" class="skeleton-row">
          <el-skeleton animated>
            <template #template>
              <div style="display:flex;align-items:center;gap:12px;padding:16px">
                <el-skeleton-item variant="text" style="width:40%;height:18px" />
                <el-skeleton-item variant="text" style="width:20%;height:14px" />
                <el-skeleton-item variant="text" style="width:15%;height:14px" />
                <el-skeleton-item variant="button" style="width:80px;height:28px" />
              </div>
            </template>
          </el-skeleton>
        </div>
      </div>

      <!-- PC端表格 -->
      <div v-else class="hidden-sm-and-down">
        <el-table
          :data="filteredRegistrations"
          stripe
          style="width: 100%"
          :header-cell-style="{ background: '#f9fafb', color: '#555', fontWeight: 600 }"
          @row-click="openDrawer"
        >
          <el-table-column prop="activityTitle" label="活动名称" min-width="220" show-overflow-tooltip>
            <template #default="{ row }">
              <el-link class="activity-title-link" type="primary" @click.stop="router.push(`/activity/${row.activityId}`)">
                {{ row.activityTitle }}
              </el-link>
            </template>
          </el-table-column>

          <el-table-column label="活动时间" width="130">
            <template #default="{ row }">
              <div class="time-cell">
                <div class="date-part">{{ formatDateOnly(row.activityStartTime) }}</div>
                <div class="time-part">{{ formatTimeOnly(row.activityStartTime) }}</div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="当前状态" width="110" align="center">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" effect="light" round size="small">
                {{ getStatusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="评分" width="120" align="center">
            <template #default="{ row }">
              <el-rate v-if="row.rating" v-model="row.rating" disabled size="small" />
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="160" fixed="right" align="center">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button v-if="row.status === 1" type="success" link size="small" @click.stop="openCheckin(row)">
                  <el-icon><Location /></el-icon> 签到
                </el-button>
                <el-button type="primary" link size="small" @click.stop="openDrawer(row)">查看详情</el-button>
                <el-button v-if="canCancel(row)" type="danger" link size="small" @click.stop="promptCancel(row)">取消报名</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 移动端卡片 -->
      <div class="hidden-md-and-up mobile-list">
        <div v-for="row in filteredRegistrations" :key="row.id" class="mobile-card" @click="openDrawer(row)">
          <div class="mobile-card-head">
            <span class="mobile-title">{{ row.activityTitle }}</span>
            <el-tag :type="getStatusType(row.status)" size="small" effect="light" round>
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </div>
          <div class="mobile-card-body">
            <span class="mobile-time"><el-icon><Clock /></el-icon> {{ formatDate(row.activityStartTime) }}</span>
            <span class="mobile-time"><el-icon><Calendar /></el-icon> 报名：{{ formatDate(row.createTime) }}</span>
          </div>
          <div class="mobile-card-foot">
            <el-rate v-if="row.rating" v-model="row.rating" disabled size="small" />
            <span v-else class="text-muted">暂无评分</span>
            <div class="mobile-actions">
              <el-button v-if="row.status === 1" type="success" size="small" round plain @click.stop="openCheckin(row)">签到</el-button>
              <el-button v-if="canCancel(row)" type="danger" size="small" round plain @click.stop="promptCancel(row)">取消</el-button>
              <el-button v-if="row.status === 3" type="success" size="small" round plain @click.stop="goCreateExperience(row.activityId)">心得</el-button>
            </div>
          </div>
        </div>
      </div>

      <el-empty v-if="!loading && filteredRegistrations.length === 0" description="暂无报名记录" :image-size="120" />

      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          :layout="isMobile ? 'prev, pager, next' : 'total, sizes, prev, pager, next'"
          :pager-count="isMobile ? 5 : 7"
          :small="isMobile"
          background
          @size-change="fetchRegistrations"
          @current-change="fetchRegistrations"
        />
      </div>
      </div>
      </div>
    </div>

    <!-- ==================== 详情抽屉 ==================== -->
    <el-drawer
      v-model="drawerVisible"
      :show-close="true"
      :with-header="false"
      direction="rtl"
      :size="isMobile ? '100%' : '480px'"
      class="detail-drawer"
    >
      <div class="drawer-content" v-if="currentRow">
        
        <!-- 头部 Banner -->
        <div class="drawer-banner">
          <div class="banner-overlay"></div>
          <el-button class="drawer-back-btn" link @click="drawerVisible = false">
            <el-icon><ArrowLeft /></el-icon> 返回
          </el-button>
          <div class="banner-content">
            <h2 class="banner-title">{{ currentRow.activityTitle }}</h2>
            <el-tag :type="getStatusType(currentRow.status)" effect="dark" round size="small">
              {{ getStatusLabel(currentRow.status) }}
            </el-tag>
          </div>
        </div>

        <div class="drawer-body">
          <!-- 状态流 (Timeline) -->
          <div class="drawer-timeline">
            <el-steps :active="getStepActive(currentRow.status)" align-center finish-status="success">
              <el-step title="已报名" />
              <el-step title="待审核" />
              <el-step title="已通过" />
              <el-step title="已签到" />
              <el-step title="已完成" />
            </el-steps>
          </div>

          <!-- 内容填充 (Description List) -->
          <div class="drawer-descriptions">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="活动时间" label-align="right" align="left">
                <el-icon><Clock /></el-icon> {{ formatDateRange(currentRow.activityStartTime, currentRow.activityEndTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="活动地点" label-align="right" align="left">
                <el-icon><Location /></el-icon> {{ currentRow.address || '青年志愿者协会指定集合点' }}
              </el-descriptions-item>
              <el-descriptions-item label="获得学时" label-align="right" align="left">
                <el-tag type="warning" size="small" effect="light">{{ currentRow.hours || 2 }} 小时</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="组织单位" label-align="right" align="left">
                {{ currentRow.organizerName || '青年志愿者协会' }}
              </el-descriptions-item>
              <el-descriptions-item label="报名时间" label-align="right" align="left">
                {{ formatDateTimeExact(currentRow.createTime) }}
              </el-descriptions-item>
              <el-descriptions-item v-if="currentRow.remark" label="备注说明" label-align="right" align="left">
                <span :class="{ 'text-danger': currentRow.status === 6 }">{{ currentRow.remark }}</span>
              </el-descriptions-item>
              <el-descriptions-item v-if="currentRow.rating" label="获得评分" label-align="right" align="left">
                <el-rate v-model="currentRow.rating" disabled show-score text-color="#ff9900" />
              </el-descriptions-item>
              <el-descriptions-item v-if="currentRow.ratingComment" label="导师评语" label-align="right" align="left">
                {{ currentRow.ratingComment }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
      </div>

      <!-- 底部操作栏 (Footer) -->
      <template #footer>
        <div class="drawer-footer-actions" v-if="currentRow">
          <el-button v-if="canCancel(currentRow)" type="danger" plain size="large" class="footer-btn" @click="promptCancel(currentRow)">
            取消报名
          </el-button>
          
          <el-button v-if="currentRow.status === 1" type="success" size="large" class="footer-btn action-btn-primary" @click="openCheckin(currentRow)">
            立即签到
          </el-button>
          <el-button v-else-if="currentRow.status === 3" type="warning" size="large" class="footer-btn action-btn-primary" @click="goCreateExperience(currentRow.activityId)">
            发布心得
          </el-button>
          
          <el-button type="primary" size="large" class="footer-btn" :class="{ 'action-btn-primary': currentRow.status !== 1 && currentRow.status !== 3 }" @click="router.push(`/activity/${currentRow.activityId}`)">
            查看详情
          </el-button>
        </div>
      </template>
    </el-drawer>

    <!-- ==================== 签到弹窗 ==================== -->
    <el-dialog v-model="checkinVisible" title="活动签到" width="90%" class="checkin-dialog" center destroy-on-close>
      <div class="checkin-content">
        <p class="checkin-tip">请输入活动现场公布的6位签到码</p>
        <el-input
          v-model="checkinCode"
          maxlength="6"
          placeholder="请输入签到码"
          size="large"
          input-style="text-align: center; font-size: 18px; font-weight: bold;"
          @keyup.enter="submitCheckin"
        >
          <template #prefix><el-icon><Key /></el-icon></template>
        </el-input>
      </div>
      <template #footer>
        <el-button @click="checkinVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCheckin" :loading="checkingIn" :disabled="checkinCode.length !== 6">确认签到</el-button>
      </template>
    </el-dialog>

    <review-dialog ref="reviewDialogRef" @success="fetchRegistrations" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, Calendar, Close, Edit, ChatLineRound, Location, Key, RefreshRight, ArrowRight, ArrowLeft, Warning, Star, Search } from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import ReviewDialog from '@/components/ReviewDialog.vue'
import dayjs from 'dayjs'

const router = useRouter()

const loading = ref(false)
const registrations = ref<any[]>([])
const total = ref(0)
const reviewDialogRef = ref()
const checkinVisible = ref(false)
const checkinCode = ref('')
const checkingIn = ref(false)
const currentActivityId = ref<number | null>(null)
const drawerVisible = ref(false)
const currentRow = ref<any>(null)
const searchKeyword = ref('')
const activeFilter = ref('all')

const queryParams = reactive({ page: 1, size: 10 })

const windowWidth = ref(window.innerWidth)
const isMobile = computed(() => windowWidth.value < 768)

const filterTabList = [
  { label: '全部', value: 'all' },
  { label: '待审核', value: '0' },
  { label: '已通过', value: '1' },
  { label: '已完成', value: '3' }
]

// ================== 统计数据 ==================
const animTotal = ref(0)
const animPending = ref(0)
const animActive = ref(0)
const animDone = ref(0)

const summaryStats = computed(() => [
  { label: '已报名', value: animTotal.value },
  { label: '待审核', value: animPending.value },
  { label: '进行中', value: animActive.value },
  { label: '已完成', value: animDone.value }
])

const animateNumber = (target: number, setter: (v: number) => void, duration = 600) => {
  const start = performance.now()
  const step = (now: number) => {
    const p = Math.min((now - start) / duration, 1)
    setter(Math.round((1 - Math.pow(1 - p, 3)) * target))
    if (p < 1) requestAnimationFrame(step)
  }
  requestAnimationFrame(step)
}

const updateSummary = () => {
  const all = registrations.value
  animateNumber(all.length, v => { animTotal.value = v })
  animateNumber(all.filter(r => r.status === 0).length, v => { animPending.value = v })
  animateNumber(all.filter(r => r.status === 1 || r.status === 2).length, v => { animActive.value = v })
  animateNumber(all.filter(r => r.status === 3).length, v => { animDone.value = v })
}

// ================== 过滤 ==================
const filteredRegistrations = computed(() => {
  let list = registrations.value
  if (activeFilter.value !== 'all') {
    list = list.filter(r => String(r.status) === activeFilter.value)
  }
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    list = list.filter(r => r.activityTitle?.toLowerCase().includes(kw))
  }
  return list
})

// ================== 状态?==================
const STATUS_MAP: Record<number, { label: string; type: string }> = {
  0: { label: '待审核', type: 'warning' },
  1: { label: '已通过', type: 'primary' },
  2: { label: '已签到', type: 'primary' },
  3: { label: '已完成', type: 'success' },
  4: { label: '已取消', type: 'info' },
  5: { label: '缺席', type: 'danger' },
  6: { label: '已驳回', type: 'info' }
}

const getStatusLabel = (s: number) => STATUS_MAP[s]?.label || '未知'
const getStatusType = (s: number) => STATUS_MAP[s]?.type || 'info'
const formatDate = (d: string) => d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '-'
const formatDateOnly = (d: string) => d ? dayjs(d).format('YYYY-MM-DD') : '-'
const formatTimeOnly = (d: string) => d ? dayjs(d).format('HH:mm') : ''
const formatDateTimeExact = (d: string) => d ? dayjs(d).format('YYYY-MM-DD HH:mm:ss') : '-'
const formatDateRange = (start: string, end: string) => {
  if (!start) return '-'
  const s = dayjs(start).format('YYYY-MM-DD HH:mm')
  if (!end) return s
  const e = dayjs(end)
  if (dayjs(start).isSame(e, 'day')) {
    return `${s} ~ ${e.format('HH:mm')}`
  }
  return `${s} ~ ${e.format('YYYY-MM-DD HH:mm')}`
}

const getStepActive = (status: number) => {
  // 0: 待审, 1: 已通过, 2: 已签到, 3: 已完成
  if (status === 0) return 1 // 已报名, 待审核
  if (status === 1) return 3 // 已通过, 等待签到
  if (status === 2) return 4 // 已签到, 等待完成
  if (status === 3) return 5 // 已完成
  return 0
}

const canCancel = (row: any) => {
  if (row.status !== 0 && row.status !== 1) return false
  if (!row.activityStartTime) return true
  return dayjs(row.activityStartTime).isAfter(dayjs())
}

// ================== API ==================
const fetchRegistrations = async () => {
  loading.value = true
  try {
    const res = await request.get('/registration/my', queryParams)
    registrations.value = res.data?.records || []
    total.value = res.data?.total || 0
    updateSummary()
  } catch (error) {
    console.error('获取报名记录失败:', error)
  } finally {
    loading.value = false
  }
}

const handleCancel = async (id: number) => {
  try {
    await request.post(`/registration/cancel/${id}`)
    ElMessage.success('取消报名成功')
    drawerVisible.value = false
    fetchRegistrations()
  } catch (error) {
    console.error('取消失败:', error)
  }
}

const promptCancel = (row: any) => {
  ElMessageBox.confirm('确定要取消该活动的报名吗？取消后可能无法再次报名', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    handleCancel(row.id)
  }).catch(() => {})
}

const goCreateExperience = (activityId: number) => {
  router.push({ path: '/experience/create', query: { activityId } })
}

const handleReview = (activityId: number) => {
  reviewDialogRef.value?.show(activityId)
}

const openDrawer = (row: any) => {
  currentRow.value = row
  drawerVisible.value = true
}

const openCheckin = (row: any) => {
  checkinVisible.value = true
  checkinCode.value = ''
  currentActivityId.value = row.activityId
}

const submitCheckin = async () => {
  if (checkinCode.value.length !== 6) {
    ElMessage.warning('请输?位签到码')
    return
  }
  checkingIn.value = true
  try {
    await request.post('/registration/signin/code', { code: checkinCode.value })
    ElMessage.success('签到成功！')
    checkinVisible.value = false
    fetchRegistrations()
  } catch (e: any) {
    console.error('签到失败:', e)
    ElMessage.error(e.msg || '签到失败，请检查签到码')
  } finally {
    checkingIn.value = false
  }
}

const handleResize = () => { windowWidth.value = window.innerWidth }

onMounted(() => {
  window.addEventListener('resize', handleResize)
  fetchRegistrations()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style lang="scss" scoped>
.my-activities {
  min-height: 100vh;
  background: #f5f7fa;
}

// ================================================================
// Header & Stats
// ================================================================
.page-header {
  padding: 24px 16px 16px;
  background: transparent;
  max-width: 1200px;
  margin: 0 auto;
}

.header-top {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;

  .page-title {
    flex: 1;
    margin: 0;
    font-size: 24px;
    font-weight: 800;
    color: #333;
  }

  .total-count {
    font-size: 13px;
    color: #888;
  }
}

.back-btn {
  font-size: 14px;
  color: #666;
  &:hover { color: #00C9A7; }
}

.content-container {
  max-width: 1200px; /* 核心宽度限制 */
  margin: 0 auto;    /* 居中核心 */
  width: 100%;
  box-sizing: border-box;
}

.stats-section {
  display: grid;
  grid-template-columns: repeat(4, 1fr); /* 4等分 */
  gap: 20px; /* 卡片间距 */
  margin-bottom: 20px;
  padding: 0 16px;
}

.stat-card {
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  text-align: center;
  transition: transform 0.2s;

  &:hover { transform: translateY(-3px); }

  .num { font-size: 28px; font-weight: bold; color: #333; font-family: 'Helvetica Neue', sans-serif;}
  .label { color: #666; font-size: 13px; margin-top: 6px; }
}

@media (max-width: 768px) {
  .stats-section {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
}

// ================================================================
// 列表卡片
// ================================================================
.list-section-card {
  background: #fff;
  border-radius: 8px; /* 圆角与上方卡片保持一行 */
  padding: 24px 16px; /* 内边距 */
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  margin: 0 16px 20px;
}

.search-bar-wrapper {
  margin-bottom: 20px;
  width: 100%; /* 撑满容器 */
}

.search-bar {
  width: 100%;

  :deep(.el-input__wrapper) {
    border-radius: 10px;
    background: #f9f9f9;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04) !important;
  }
}

.filter-tabs {
  display: flex;
  padding: 0 16px;
  margin: 10px 0;
  overflow-x: auto;

  &::-webkit-scrollbar { display: none; }
}

.filter-tab {
  padding: 7px 14px;
  font-size: 13px;
  font-weight: 500;
  color: #666;
  cursor: pointer;
  white-space: nowrap;
  position: relative;
  transition: color 0.25s;

  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 50%;
    transform: translateX(-50%) scaleX(0);
    width: 20px;
    height: 3px;
    border-radius: 2px;
    background: linear-gradient(90deg, #00C9A7, #05D5B3);
    transition: transform 0.25s;
  }

  &.active {
    color: #00C9A7;
    font-weight: 600;
    &::after { transform: translateX(-50%) scaleX(1); }
  }
}

// ================================================================
// 列表容器
// ================================================================
.list-container {
  padding: 0 16px;
  min-height: 200px;
}

.skeleton-list {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}

.skeleton-row {
  border-bottom: 1px solid #f5f5f5;
  &:last-child { border-bottom: none; }
}

.activity-title-link {
  font-weight: 600;
  font-size: 15px;
}

.time-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
  .date-part {
    font-size: 13px;
    color: #333;
    font-weight: 500;
  }
  .time-part {
    font-size: 12px;
    color: #999;
  }
}

:deep(.el-table__row) {
  height: 64px;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 6px;
}

.detail-btn {
  border: none;
  background: linear-gradient(135deg, #00C9A7, #05D5B3);
  color: #fff;
  padding: 4px 12px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s;
  white-space: nowrap;

  &:hover {
    transform: scale(1.08);
    box-shadow: 0 4px 12px rgba(0, 201, 167, 0.35);
  }
}

.text-muted { color: #ccc; font-size: 12px; }
.text-danger { color: #f56c6c; }

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

// ================================================================
// 移动端卡片
// ================================================================
.mobile-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.mobile-card {
  background: #fff;
  border-radius: 12px;
  padding: 14px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: all 0.2s;

  &:active { transform: scale(0.98); }

  .mobile-card-head {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 8px;
    margin-bottom: 8px;

    .mobile-title {
      font-size: 14px;
      font-weight: 600;
      color: #333;
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }
  }

  .mobile-card-body {
    display: flex;
    flex-direction: column;
    gap: 4px;
    margin-bottom: 10px;

    .mobile-time {
      font-size: 12px;
      color: #999;
      display: flex;
      align-items: center;
      gap: 4px;
      .el-icon { font-size: 12px; }
    }
  }

  .mobile-card-foot {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 10px;
    border-top: 1px solid #f5f5f5;

    .mobile-actions {
      display: flex;
      gap: 6px;
    }
  }
}

// ================================================================
// 详情抽屉重构
// ================================================================
:deep(.el-drawer) {
  .el-drawer__body {
    padding: 0;
    display: flex;
    flex-direction: column;
  }
}

.drawer-content {
  flex: 1;
  overflow-y: auto;
  background: #f5f7fa;
}

.drawer-banner {
  position: relative;
  height: 140px;
  background: linear-gradient(135deg, #00C9A7 0%, #05D5B3 100%);
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 20px;
  color: #fff;

  .drawer-back-btn {
    position: absolute;
    top: 16px;
    left: 16px;
    color: #fff;
    font-size: 14px;
    z-index: 2;

    &:hover { color: rgba(255,255,255,0.8); }
  }

  .banner-overlay {
    position: absolute;
    top: 0; left: 0; right: 0; bottom: 0;
    background: linear-gradient(to bottom, rgba(0,0,0,0.1), rgba(0,0,0,0.5));
  }

  .banner-content {
    position: relative;
    z-index: 1;

    .banner-title {
      margin: 0 0 8px 0;
      font-size: 20px;
      font-weight: 800;
      line-height: 1.4;
      text-shadow: 0 2px 4px rgba(0,0,0,0.3);
    }
  }
}

.drawer-body {
  padding: 24px;
}

.drawer-timeline {
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.03);
  margin-bottom: 20px;

  :deep(.el-step__title) {
    font-size: 12px;
  }
}

.drawer-descriptions {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.03);
  overflow: hidden;

  :deep(.el-descriptions__body) {
    background: transparent;
  }

  :deep(.el-descriptions__label) {
    width: 100px;
    color: #666;
    background-color: #fafafa !important;
    font-weight: 500;
  }

  :deep(.el-descriptions__content) {
    color: #333;
    font-weight: 500;
  }
}

.drawer-footer-actions {
  display: flex;
  gap: 12px;
  padding: 16px 24px;
  background: #fff;
  box-shadow: 0 -4px 16px rgba(0,0,0,0.05);

  .footer-btn {
    flex: 1;
    font-weight: bold;
    border-radius: 8px;
  }

  .action-btn-primary {
    flex: 2;
  }
}

// ================================================================
// 签到弹窗
// ================================================================
.checkin-content {
  text-align: center;
  padding: 10px 0;

  .checkin-tip {
    margin-bottom: 20px;
    color: #606266;
  }
}
</style>
