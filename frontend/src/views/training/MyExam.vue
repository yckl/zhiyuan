<template>
  <div class="my-exam-page">
    <div class="page-header">
      <h2>📋 我的考试记录</h2>
      <p class="subtitle">查看您的培训考试历史</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card">
        <span class="stat-value">{{ stats.total }}</span>
        <span class="stat-label">总考试次数</span>
      </div>
      <div class="stat-card passed">
        <span class="stat-value">{{ stats.passed }}</span>
        <span class="stat-label">通过次数</span>
      </div>
      <div class="stat-card">
        <span class="stat-value">{{ stats.avgScore }}</span>
        <span class="stat-label">平均分数</span>
      </div>
      <div class="stat-card reward">
        <span class="stat-value">{{ stats.totalReward }}</span>
        <span class="stat-label">累计获得积分</span>
      </div>
    </div>

    <!-- 考试记录表格 -->
    <el-card class="records-card">
      <el-table :data="examRecords" v-loading="loading" stripe>
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

        <el-table-column label="正确率" width="100" align="center">
          <template #default="{ row }">
            {{ row.totalCount > 0 ? Math.round((row.correctCount / row.totalCount) * 100) : 0 }}%
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
            <span v-else class="no-reward">-</span>
          </template>
        </el-table-column>

        <el-table-column label="用时" width="100" align="center">
          <template #default="{ row }">
            {{ formatTime(row.timeSpent) }}
          </template>
        </el-table-column>

        <el-table-column label="考试时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="goToCourse(row.courseId)">
              重新测验
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && examRecords.length === 0" description="暂无考试记录" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { request } from '@/utils/request'

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

const stats = computed(() => {
  const total = examRecords.value.length
  const passed = examRecords.value.filter(r => r.passed).length
  const avgScore = total > 0
    ? Math.round(examRecords.value.reduce((sum, r) => sum + r.score, 0) / total)
    : 0
  const totalReward = examRecords.value.reduce((sum, r) => sum + (r.pointsReward || 0), 0)
  return { total, passed, avgScore, totalReward }
})

const fetchRecords = async () => {
  loading.value = true
  try {
    const res = await request.get('/course/exam/records')
    if (res.code === 200) {
      examRecords.value = res.data || []
    }
  } catch (error) {
    console.error('获取考试记录失败:', error)
  } finally {
    loading.value = false
  }
}

const formatTime = (seconds?: number) => {
  if (!seconds) return '-'
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}分${secs}秒`
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
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

onMounted(fetchRecords)
</script>

<style lang="scss" scoped>
.my-exam-page {
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

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;

  @media (max-width: 768px) {
    grid-template-columns: repeat(2, 1fr);
  }

  .stat-card {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    text-align: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

    .stat-value {
      display: block;
      font-size: 32px;
      font-weight: bold;
      color: #333;
      margin-bottom: 4px;
    }

    .stat-label {
      font-size: 13px;
      color: #999;
    }

    &.passed {
      .stat-value {
        color: #67c23a;
      }
    }

    &.reward {
      .stat-value {
        color: #e6a23c;
      }
    }
  }
}

.records-card {
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
    color: #e6a23c;
    font-weight: 500;
  }

  .no-reward {
    color: #ccc;
  }
}
</style>
