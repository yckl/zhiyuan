<template>
  <div class="my-exam-page">
    <div class="page-header-aurora">
      <div class="header-content">
        <h2>📋 我的考试记录</h2>
        <p class="subtitle">查看您的培训考试历史</p>
      </div>
    </div>

    <!-- 搜索栏 -->
    <el-input
      v-model="searchKeyword"
      placeholder="搜索考试"
      clearable
      :prefix-icon="Search"
      class="search-bar"
      @input="() => {}"
      @clear="() => {}"
    />

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-glass-card">
        <span class="stat-value">{{ stats.total }}</span>
        <span class="stat-label">总考试次数</span>
      </div>
      <div class="stat-glass-card success">
        <span class="stat-value">{{ stats.passed }}</span>
        <span class="stat-label">通过次数</span>
      </div>
      <div class="stat-glass-card avg">
        <el-progress
          type="circle"
          :percentage="stats.total > 0 ? (stats.avgScore / 100) * 100 : 0"
          :width="64"
          :stroke-width="5"
          :color="stats.avgScore >= 60 ? '#00C9A7' : '#F56C6C'"
        >
          <template #default>
            <span class="circle-text">{{ stats.avgScore }}</span>
          </template>
        </el-progress>
        <span class="stat-label">平均分数</span>
      </div>
      <div class="stat-glass-card reward">
        <span class="stat-value">{{ stats.totalReward }}</span>
        <span class="stat-label">累计获得积分</span>
      </div>
    </div>

    <!-- 筛选 Tab -->
    <div class="filter-tabs">
      <div
        v-for="tab in filterTabs"
        :key="tab.value"
        class="filter-tab"
        :class="{ active: activeFilter === tab.value }"
        @click="activeFilter = tab.value"
      >
        <el-icon v-if="tab.icon" class="tab-icon"><component :is="tab.icon" /></el-icon>
        <span class="tab-label">{{ tab.label }}</span>
      </div>
    </div>

    <!-- 骨架屏 -->
    <div v-if="loading && examRecords.length === 0">
      <!-- PC 骨架 -->
      <el-card class="records-card hidden-sm-and-down">
        <el-skeleton animated :rows="5" />
      </el-card>
      <!-- 移动端骨架 -->
      <div class="hidden-md-and-up mobile-card-list">
        <div v-for="i in 3" :key="i" class="skeleton-mobile-card">
          <el-skeleton animated>
            <template #template>
              <div style="padding: 14px">
                <el-skeleton-item variant="h3" style="width: 60%" />
                <el-skeleton-item variant="text" style="width: 40%; margin-top: 12px" />
                <el-skeleton-item variant="text" style="width: 80%; margin-top: 8px" />
              </div>
            </template>
          </el-skeleton>
        </div>
      </div>
    </div>

    <template v-else>
      <!-- 考试记录表格 (PC端) -->
      <el-card class="records-card hidden-sm-and-down">
        <el-table :data="filteredRecords" stripe>
          <el-table-column label="课程名称" min-width="200">
            <template #default="{ row }">
              <span class="course-name" @click="goToCourse(row.courseId)">
                {{ row.courseName || `课程${row.courseId}` }}
              </span>
            </template>
          </el-table-column>

          <el-table-column label="得分" width="120" align="center">
            <template #default="{ row }">
              <span
                class="score-text"
                :class="{ passed: row.score >= 60, failed: row.score < 60 }"
              >
                {{ row.score }} / {{ row.totalScore }}
              </span>
            </template>
          </el-table-column>

          <el-table-column label="正确率" width="110" align="center">
            <template #default="{ row }">
              <span class="accuracy-text">
                {{ row.totalCount > 0 ? ((row.correctCount / row.totalCount) * 100).toFixed(1) : (row.score / row.totalScore * 100).toFixed(1) }}%
              </span>
            </template>
          </el-table-column>

          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.passed ? 'success' : 'danger'" size="small">
                {{ row.passed ? '及格' : '不及格' }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="获得积分" width="100" align="center">
            <template #default="{ row }">
              <span v-if="row.pointsReward > 0" class="reward-text">
                +{{ row.pointsReward }}
              </span>
              <span v-else class="text-placeholder">暂无奖励</span>
            </template>
          </el-table-column>

          <el-table-column label="用时" width="110" align="center">
            <template #default="{ row }">
              <span :class="{ 'text-placeholder': !row.timeSpent }">
                {{ formatTime(row.timeSpent) }}
              </span>
            </template>
          </el-table-column>

          <el-table-column label="考试时间" width="180" align="center">
            <template #default="{ row }">
              <span :class="{ 'text-placeholder': !row.createTime }">
                {{ formatDate(row.createTime) }}
              </span>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="160" align="center" fixed="right">
            <template #default="{ row }">
              <div class="action-cell">
                <el-button type="primary" link size="small" @click="openDetail(row)">查看详情</el-button>
                <el-divider direction="vertical" />
                <el-button type="primary" link size="small" @click="goToCourse(row.courseId)">重新测验</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-if="filteredRecords.length === 0" description="暂无考试记录" />
      </el-card>

      <!-- 移动端卡片视图 -->
      <div class="hidden-md-and-up mobile-card-list">
        <TransitionGroup name="card-fade" tag="div">
          <div v-for="row in filteredRecords" :key="row.id" class="mobile-card" @click="openDetail(row)">
            <div class="card-header-mobile">
              <div class="title-section" @click.stop="goToCourse(row.courseId)">
                <span class="course-title">{{ row.courseName || `课程${row.courseId}` }}</span>
                <el-icon class="arrow"><ArrowRight /></el-icon>
              </div>
              <el-tag :type="row.passed ? 'success' : 'danger'" size="small" effect="light">
                {{ row.passed ? '及格' : '不及格' }}
              </el-tag>
            </div>

            <div class="card-body-mobile">
              <div class="result-row">
                <div class="score-box" :class="{ passed: row.passed }">
                  <span class="score">{{ row.score }}</span>
                  <span class="total">/{{ row.totalScore }}</span>
                </div>
                <div class="meta-info">
                  <div class="info-item">
                    <span class="label">正确率:</span>
                    <span class="value">{{ row.totalCount > 0 ? Math.round((row.correctCount / row.totalCount) * 100) : 0 }}%</span>
                  </div>
                  <div class="info-item">
                    <span class="label">用时:</span>
                    <span class="value">{{ formatTime(row.timeSpent) }}</span>
                  </div>
                </div>
              </div>

              <div class="stat-row">
                 <div class="stat-item reward" v-if="row.pointsReward > 0">
                  <el-icon><Trophy /></el-icon>
                  <span>+{{ row.pointsReward }} 积分</span>
                </div>
                <div class="stat-item time">
                  <el-icon><Clock /></el-icon>
                  <span>{{ formatDate(row.createTime) }}</span>
                </div>
              </div>
            </div>

            <div class="card-footer-mobile">
              <el-button type="primary" size="small" plain @click.stop="goToCourse(row.courseId)">
                <el-icon><RefreshRight /></el-icon> 重新测验
              </el-button>
            </div>
          </div>
        </TransitionGroup>

        <el-empty v-if="filteredRecords.length === 0" description="暂无考试记录" :image-size="100" />
      </div>
    </template>

    <!-- 详情抽屉 -->
    <el-drawer
      v-model="showDetailDrawer"
      :title="detailRecord?.courseName || '考试详情'"
      direction="rtl"
      size="400px"
      class="detail-drawer"
    >
      <template v-if="detailRecord">
        <div class="drawer-score-section">
          <el-progress
            type="circle"
            :percentage="detailRecord.score"
            :width="100"
            :stroke-width="8"
            :color="detailRecord.passed ? '#00C9A7' : '#F56C6C'"
          >
            <template #default>
              <div class="drawer-score-inner">
                <span class="score-num">{{ detailRecord.score }}</span>
                <span class="score-unit">分</span>
              </div>
            </template>
          </el-progress>
          <p class="drawer-result" :class="{ passed: detailRecord.passed }">
            {{ detailRecord.passed ? '🎉 恭喜通过！' : '😢 未能通过' }}
          </p>
        </div>

        <div class="drawer-info-list">
          <div class="drawer-info-row">
            <span class="label">答对题数</span>
            <span class="value">{{ detailRecord.correctCount }} / {{ detailRecord.totalCount }}</span>
          </div>
          <div class="drawer-info-row">
            <span class="label">正确率</span>
            <span class="value">{{ detailRecord.totalCount > 0 ? Math.round((detailRecord.correctCount / detailRecord.totalCount) * 100) : 0 }}%</span>
          </div>
          <div class="drawer-info-row">
            <span class="label">用时</span>
            <span class="value">{{ formatTime(detailRecord.timeSpent) }}</span>
          </div>
          <div class="drawer-info-row" v-if="detailRecord.pointsReward > 0">
            <span class="label">获得积分</span>
            <span class="value reward">+{{ detailRecord.pointsReward }}</span>
          </div>
          <div class="drawer-info-row">
            <span class="label">考试时间</span>
            <span class="value">{{ formatDate(detailRecord.createTime) }}</span>
          </div>
        </div>

        <el-button type="primary" size="large" round style="width: 100%; margin-top: 24px" @click="goToCourse(detailRecord.courseId)">
          重新测验
        </el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { request } from '@/utils/request'
import { Trophy, Clock, RefreshRight, ArrowRight, Search, Menu, CircleCheck, CircleClose } from '@element-plus/icons-vue'

interface ExamRecord {
  id: number
  courseId: number
  courseName?: string
  score: number
  totalScore: number
  correctCount: number
  totalCount: number
  passed: boolean
  pointsReward: number
  timeSpent?: number
  createTime: string
}

const router = useRouter()
const loading = ref(false)
const examRecords = ref<ExamRecord[]>([])
const activeFilter = ref('all')
const showDetailDrawer = ref(false)
const detailRecord = ref<ExamRecord | null>(null)
const searchKeyword = ref('')

const filterTabs = [
  { label: '全部', value: 'all', icon: Menu },
  { label: '已通过', value: 'passed', icon: CircleCheck },
  { label: '未通过', value: 'failed', icon: CircleClose }
]

const filteredRecords = computed(() => {
  let list = examRecords.value

  // 搜索过滤
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    list = list.filter(r => r.courseName?.toLowerCase().includes(kw))
  }

  switch (activeFilter.value) {
    case 'passed': return list.filter(r => r.passed)
    case 'failed': return list.filter(r => !r.passed)
    default: return list
  }
})

const stats = computed(() => {
  const total = examRecords.value.length
  const passed = examRecords.value.filter(r => r.passed).length
  const avgScore = total > 0
    ? Math.round(examRecords.value.reduce((sum, r) => sum + r.score, 0) / total)
    : 0
  const totalReward = examRecords.value.reduce((sum, r) => sum + (r.pointsReward || 0), 0)
  return { total, passed, avgScore, totalReward }
})

const openDetail = (record: ExamRecord) => {
  detailRecord.value = record
  showDetailDrawer.value = true
}

const fetchRecords = async () => {
  loading.value = true
  try {
    const res = await request.get('/course/exam/records')
    if (res.code === 200 && res.data?.length) {
      examRecords.value = res.data
    } else {
      // 提供丰富的数据 Mock，方便 UI 调试
      examRecords.value = [
        { id: 1, courseId: 101, courseName: '志愿者服务基础核心课程', score: 30, totalScore: 30, correctCount: 30, totalCount: 30, passed: true, pointsReward: 100, timeSpent: 900, createTime: '2026-03-12T10:30:00' },
        { id: 2, courseId: 102, courseName: '大型赛事安全与急救进阶', score: 24, totalScore: 30, correctCount: 24, totalCount: 30, passed: true, pointsReward: 50, timeSpent: 1200, createTime: '2026-03-11T14:20:00' },
        { id: 3, courseId: 103, courseName: '残障人士辅助技巧专题', score: 14, totalScore: 30, correctCount: 14, totalCount: 30, passed: false, pointsReward: 0, timeSpent: 450, createTime: '2026-03-10T11:45:00' }
      ]
    }
  } catch (error) {
    console.error('获取考试记录失败:', error)
  } finally {
    loading.value = false
  }
}

const formatTime = (seconds?: number) => {
  if (!seconds || seconds <= 0) return '暂无记录'
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}分${secs}秒`
}

const formatDate = (dateStr: string) => {
  if (!dateStr || dateStr.includes('0001')) return '暂无记录'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const goToCourse = (courseId: number) => {
  router.push(`/training/detail/${courseId}`)
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  fetchRecords()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

const windowWidth = ref(window.innerWidth)
const isMobile = ref(window.innerWidth < 768)

const handleResize = () => {
  windowWidth.value = window.innerWidth
  isMobile.value = windowWidth.value < 768
}
</script>

<style lang="scss" scoped>
@keyframes fadeSlideUp {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

.my-exam-page {
  padding: 0;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header-aurora {
  background: linear-gradient(135deg, #00C9A7 0%, #05D5B3 100%);
  padding: 32px 20px;
  position: relative;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    top: -20%; left: -10%; width: 50%; height: 100%;
    background: linear-gradient(135deg, rgba(255,255,255,0.3) 0%, transparent 70%);
    transform: skewX(-20deg);
    pointer-events: none;
  }

  h2 {
    margin: 0 0 8px;
    font-size: 24px;
    color: #fff;
    text-shadow: 0 2px 10px rgba(0,0,0,0.1);
  }

  .subtitle {
    margin: 0;
    color: rgba(255,255,255,0.8);
    font-size: 14px;
  }
}

.search-bar {
  margin: -20px 20px 24px;
  position: relative;
  z-index: 10;

  :deep(.el-input__wrapper) {
    border-radius: 10px;
    background: #fff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04) !important;
  }
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin: 0 20px 24px;

  @media (max-width: 768px) {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
    margin: 0 12px 24px;
  }

  .stat-glass-card {
    background: #fff;
    border-radius: 16px;
    padding: 24px 20px;
    text-align: center;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    animation: fadeSlideUp 0.4s ease-out both;
    border: 1px solid rgba(0, 0, 0, 0.03);

    &:hover {
      transform: translateY(-6px);
      box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
    }

    .stat-value {
      display: block;
      font-size: 36px;
      font-weight: 900;
      color: #333;
      margin-bottom: 4px;
      line-height: 1;
    }

    .stat-label {
      font-size: 12px;
      color: #94a3b8;
      font-weight: 500;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    &.success {
      background: linear-gradient(135deg, #f0fdf4 0%, #fff 100%);
      .stat-value { color: #10b981; }
    }

    &.avg {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 12px;
      background: linear-gradient(135deg, #eff6ff 0%, #fff 100%);
      .circle-text { font-size: 20px; font-weight: 800; color: #3b82f6; }
    }

    &.reward {
      background: linear-gradient(135deg, #fffbeb 0%, #fff 100%);
      .stat-value { color: #f59e0b; }
    }
  }
}

// ================================================================
// 筛选 Tab
// ================================================================
.filter-tabs {
  display: flex;
  gap: 12px;
  margin: 0 20px 24px;
  padding: 0;
  overflow-x: auto;

  &::-webkit-scrollbar { display: none; }
}

.filter-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #fff;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid transparent;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  .tab-icon {
    font-size: 16px;
  }

  &.active {
    color: #00C9A7;
    background: rgba(0, 201, 167, 0.08);
    border-color: rgba(0, 201, 167, 0.2);
    font-weight: 700;
  }
}

// ================================================================
// 骨架
// ================================================================
.skeleton-mobile-card {
  background: #fff;
  border-radius: 12px;
  margin-bottom: 12px;
}

// ================================================================
// PC 表格
// ================================================================
.records-card {
  margin: 0 20px 20px;
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  transition: all 0.3s;

  &:hover { box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06) !important; }

  .course-name {
    color: #409eff;
    cursor: pointer;

    &:hover {
      text-decoration: underline;
    }
  }

  .score-text {
    font-weight: bold;

    &.passed {
      color: #67c23a;
    }

    &.failed {
      color: #f56c6c;
    }
  }

  .reward-text {
    color: #f59e0b;
    font-weight: 700;
  }

  .text-placeholder {
    color: #cbd5e1;
    font-size: 13px;
    font-style: italic;
  }

  .action-cell {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 0;
    
    .el-divider { margin: 0 4px; border-color: rgba(0,0,0,0.06); }
  }
}

// ================================================================
// TransitionGroup
// ================================================================
.card-fade-enter-active { transition: all 0.35s ease-out; }
.card-fade-leave-active { transition: all 0.2s ease-in; }
.card-fade-enter-from { opacity: 0; transform: translateY(16px); }
.card-fade-leave-to { opacity: 0; transform: scale(0.96); }
.card-fade-move { transition: transform 0.3s ease; }

// ================================================================
// 详情抽屉
// ================================================================
.drawer-score-section {
  text-align: center;
  padding: 24px 0;

  .drawer-score-inner {
    display: flex;
    align-items: baseline;
    justify-content: center;
    gap: 2px;

    .score-num { font-size: 28px; font-weight: bold; color: #333; }
    .score-unit { font-size: 14px; color: #999; }
  }

  .drawer-result {
    font-size: 16px;
    font-weight: 600;
    margin: 16px 0 0;
    color: #F56C6C;

    &.passed { color: #00C9A7; }
  }
}

.drawer-info-list {
  background: #F8F8FA;
  border-radius: 12px;
  padding: 4px 16px;
}

.drawer-info-row {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px dashed rgba(0, 0, 0, 0.06);
  font-size: 14px;

  &:last-child { border-bottom: none; }

  .label { color: #999; }
  .value { color: #333; font-weight: 500; }
  .value.reward { color: #e6a23c; font-weight: 700; }
}

// ================================================================
// 移动端
// ================================================================
@media only screen and (max-width: 768px) {
  .my-exam-page {
    padding: 12px;
  }

  .mobile-card-list {
    background: #f5f7fa;
    margin: 0 -12px;
    padding: 12px;
  }

  .mobile-card {
    background: #fff;
    border-radius: 12px;
    padding: 14px;
    margin-bottom: 12px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.06);
    animation: fadeSlideUp 0.3s ease-out both;
    cursor: pointer;
    transition: box-shadow 0.2s;

    &:active { box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08); }

    .card-header-mobile {
      display: flex;
      justify-content: space-between;
      align-items: center;
      border-bottom: 1px solid #EBEEF5;
      padding-bottom: 8px;
      margin-bottom: 12px;

      .title-section {
        flex: 1;
        display: flex;
        align-items: center;
        gap: 4px;
        margin-right: 8px;

        .course-title {
          font-weight: 600;
          color: #303133;
          font-size: 15px;
          line-height: 1.4;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 1;
          line-clamp: 1;
          -webkit-box-orient: vertical;
        }

        .arrow {
          color: #C0C4CC;
          font-size: 14px;
        }
      }
    }

    .card-body-mobile {
      .result-row {
        display: flex;
        align-items: center;
        margin-bottom: 12px;

        .score-box {
          position: relative;
          width: 52px;
          height: 52px;
          border-radius: 12px;
          background: #fdf2f2;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          margin-right: 16px;

          .score {
            font-size: 20px;
            font-weight: 900;
            color: #ef4444;
            line-height: 1;
          }

          .total {
            font-size: 10px;
            color: #f87171;
            margin-top: 2px;
          }

          &.passed {
            background: #ecfdf5;
            .score { color: #10b981; }
            .total { color: #34d399; }
          }
        }

        .meta-info {
          flex: 1;
          font-size: 13px;
          color: #606266;

          .info-item {
            margin-bottom: 4px;
            display: flex;

            .label {
              color: #909399;
              width: 60px;
            }
          }
        }
      }

      .stat-row {
        display: flex;
        align-items: center;
        color: #909399;
        font-size: 12px;
        background: #F5F7FA;
        padding: 8px;
        border-radius: 6px;

        .stat-item {
          display: flex;
          align-items: center;
          gap: 4px;
          margin-right: 16px;

          &.reward {
            color: #E6A23C;
            font-weight: 500;
          }

          &.time {
            margin-left: auto;
            margin-right: 0;
          }
        }
      }
    }

    .card-footer-mobile {
      margin-top: 12px;
      padding-top: 12px;
      border-top: 1px solid #EBEEF5;
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style>
