<template>
  <div class="training-index">
    <!-- 1. 学习数据概览 -->
    <el-row :gutter="20" class="stats-row" v-loading="loading">
      <el-col :span="6" v-for="stat in stats" :key="stat.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon :size="40" :style="{ color: stat.color }">
              <component :is="stat.icon" />
            </el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ (statsData as any)[stat.key] }}{{ stat.unit }}</div>
              <div class="stat-label">{{ stat.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 2. 继续学习卡片 -->
    <el-card shadow="hover" class="resume-card" v-if="resumeCourse" v-loading="loading">
      <div class="resume-wrapper">
        <el-image :src="resumeCourse.coverImage" class="resume-cover" fit="cover">
          <template #error>
            <div class="image-placeholder"><el-icon><Picture /></el-icon></div>
          </template>
        </el-image>
        <div class="resume-main">
          <h3 class="resume-title">{{ resumeCourse.title }}</h3>
          <div class="resume-progress">
            <span class="progress-label">学习进度</span>
            <el-progress :percentage="60" color="#409eff" />
          </div>
          <p class="resume-last">讲师: {{ resumeCourse.instructor }}</p>
        </div>
        <div class="resume-action">
          <el-button type="primary" size="large" round @click="goLearning(resumeCourse.id)">
            继续学习
          </el-button>
        </div>
      </div>
    </el-card>

    <el-row :gutter="20" v-loading="loading">
      <!-- 3. 必修课程推荐 -->
      <el-col :span="18">
        <div class="section-container">
          <div class="section-header">
            <h3 class="section-title">🎓 必修课程 (岗前培训)</h3>
            <el-link type="primary" :underline="false" @click="router.push('/training/list')">
              更多课程 <el-icon><ArrowRight /></el-icon>
            </el-link>
          </div>
          <el-row :gutter="20">
            <el-col :span="6" v-for="course in mandatoryCourses" :key="course.id">
              <el-card shadow="hover" :body-style="{ padding: '0px' }" class="course-card">
                <el-image :src="course.coverImage" class="course-cover" fit="cover">
                  <template #error>
                    <div class="image-placeholder"><el-icon><Picture /></el-icon></div>
                  </template>
                </el-image>
                <div class="course-info">
                  <h4 class="course-title">{{ course.title }}</h4>
                  <div class="course-meta">
                    <span class="instructor">讲师: {{ course.instructor }}</span>
                    <el-tag size="small" type="danger" effect="plain">必修</el-tag>
                  </div>
                  <el-button type="primary" block class="start-btn" @click="goLearning(course.id)">开始学习</el-button>
                </div>
              </el-card>
            </el-col>
          </el-row>
          <el-empty v-if="mandatoryCourses.length === 0" description="暂无必修课程" />
        </div>
      </el-col>

      <!-- 4. 待考提醒 -->
      <el-col :span="6">
        <div class="section-container">
          <h3 class="section-title">待考提醒</h3>
          <div class="exam-list">
            <el-alert
              v-for="exam in pendingExams"
              :key="exam.id"
              :title="'考试未通过'"
              type="warning"
              :closable="false"
              class="exam-alert"
            >
              <template #default>
                <div class="exam-content">
                  <p>您最近参加的《{{ exam.courseTitle || '相关课程' }}》考试未通过，请复习后再次挑战！</p>
                  <el-button type="warning" size="small" plain @click="goExam(exam.id)">去查看</el-button>
                </div>
              </template>
            </el-alert>
            <el-empty v-if="pendingExams.length === 0" description="暂无待考提醒" :image-size="60" />
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  Timer, Collection, Trophy, DataAnalysis, 
  Picture, ArrowRight 
} from '@element-plus/icons-vue'
import { request } from '@/utils/request'

const router = useRouter()
const loading = ref(false)

// 1. 数据概览
const statsData = ref({
  totalHours: 0,
  completedCount: 0,
  certificateCount: 0,
  averageScore: 0
})

const stats = ref([
  { label: '累计学时', key: 'totalHours', unit: 'h', icon: Timer, color: '#409eff' },
  { label: '课程完成', key: 'completedCount', unit: ' 门', icon: Collection, color: '#67c23a' },
  { label: '获得证书', key: 'certificateCount', unit: ' 张', icon: Trophy, color: '#e6a23c' },
  { label: '考试均分', key: 'averageScore', unit: ' 分', icon: DataAnalysis, color: '#909399' }
])

// 2. 继续学习
const resumeCourse = ref<any>(null)

// 3. 必修课程
const mandatoryCourses = ref<any[]>([])

// 4. 待考提醒
const pendingExams = ref<any[]>([])

const fetchData = async () => {
  loading.value = true
  try {
    const [statsRes, resumeRes, mandatoryRes, pendingRes] = await Promise.all([
      request.get('/course/stats'),
      request.get('/course/resume'),
      request.get('/course/mandatory'),
      request.get('/course/exams/pending')
    ])

    if (statsRes.code === 200) statsData.value = statsRes.data
    if (resumeRes.code === 200) resumeCourse.value = resumeRes.data
    if (mandatoryRes.code === 200) mandatoryCourses.value = mandatoryRes.data || []
    if (pendingRes.code === 200) pendingExams.value = pendingRes.data || []
  } catch (error) {
    console.error('获取学习中心数据失败:', error)
  } finally {
    loading.value = false
  }
}

const goLearning = (id: number) => {
  router.push(`/training/detail/${id}`)
}

const goExam = (_id: number) => {
  router.push('/training/my')
}

onMounted(fetchData)
</script>

<style scoped lang="scss">
.training-index {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 100px);

  .stats-row {
    margin-bottom: 24px;
  }

  .stat-card {
    border-radius: 12px;
    .stat-content {
      display: flex;
      align-items: center;
      gap: 16px;
    }
    .stat-value {
      font-size: 24px;
      font-weight: bold;
      color: #303133;
    }
    .stat-label {
      font-size: 14px;
      color: #909399;
      margin-top: 4px;
    }
  }

  .resume-card {
    margin-bottom: 32px;
    border-radius: 16px;
    overflow: hidden;
    
    .resume-wrapper {
      display: flex;
      align-items: center;
      gap: 24px;
    }
    
    .resume-cover {
      width: 240px;
      height: 140px;
      border-radius: 8px;
    }

    .resume-main {
      flex: 1;
      .resume-title {
        font-size: 20px;
        margin: 0 0 16px 0;
        color: #303133;
      }
      .resume-progress {
        margin-bottom: 12px;
        .progress-label {
          font-size: 13px;
          color: #606266;
          margin-bottom: 8px;
          display: block;
        }
      }
      .resume-last {
        font-size: 14px;
        color: #909399;
      }
    }

    .resume-action {
      padding-right: 20px;
    }
  }

  .section-container {
    margin-bottom: 32px;
    
    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
    }
    
    .section-title {
      font-size: 18px;
      font-weight: bold;
      margin: 0;
      color: #303133;
    }
  }

  .course-card {
    border-radius: 12px;
    transition: transform 0.3s;
    
    &:hover {
      transform: translateY(-5px);
    }

    .course-cover {
      width: 100%;
      height: 160px;
    }

    .course-info {
      padding: 16px;
      
      .course-title {
        font-size: 15px;
        margin: 0 0 12px 0;
        height: 40px;
        line-height: 20px;
        overflow: hidden;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        line-clamp: 2;
        -webkit-box-orient: vertical;
        color: #303133;
      }

      .course-meta {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;
        
        .instructor {
          font-size: 13px;
          color: #909399;
        }
      }

      .start-btn {
        width: 100%;
        border-radius: 8px;
      }
    }
  }

  .exam-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .exam-alert {
    border-radius: 12px;
    padding: 16px;
    
    .exam-content {
      margin-top: 8px;
      p {
        margin: 0 0 12px 0;
        font-size: 13px;
        line-height: 1.5;
      }
    }
  }

  .image-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #f5f7fa;
    color: #909399;
    font-size: 24px;
  }
}
</style>
