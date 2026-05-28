<template>
  <div class="training-index">

    <!-- 顶部 Hero -->
    <div class="hero-section">
      <div class="hero-inner">
        <h1 class="hero-title">🎓 培训学院</h1>
        <p class="hero-sub">提升技能，获取积分奖励</p>
      </div>
      <router-link to="/training/my" class="exam-entry">
        <el-icon><DataAnalysis /></el-icon>
        <span>考试记录</span>
      </router-link>
    </div>

    <!-- 搜索栏?-->
    <el-input
      v-model="searchKeyword"
      placeholder="搜索课程"
      clearable
      :prefix-icon="Search"
      class="search-bar"
      @input="handleSearch"
      @clear="handleSearch"
    />

    <!-- 分类 Tab -->
    <div class="filter-tabs">
      <div
        v-for="tab in tabs"
        :key="tab.value"
        class="filter-tab"
        :class="{ active: activeTab === tab.value }"
        @click="activeTab = tab.value"
      >
        <el-icon v-if="tab.icon" class="tab-icon"><component :is="tab.icon" /></el-icon>
        <span class="tab-label">{{ tab.label }}</span>
      </div>
    </div>

    <!-- 1. 学习数据概览 -->
    <div class="stats-section" v-if="activeTab === 'all'">
      <div class="stats-row">
        <div v-for="(stat, idx) in stats" :key="stat.label" class="stat-card" :style="{ animationDelay: `${idx * 0.05}s` }">
          <el-icon :size="36" :style="{ color: stat.color }">
            <component :is="stat.icon" />
          </el-icon>
          <div class="stat-info">
            <div class="stat-value">{{ (statsData as any)[stat.key] }}{{ stat.unit }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </div>
      </div>

      <!-- 游客遮罩 -->
      <div v-if="!isLoggedIn" class="guest-stats-mask">
        <div class="mask-content">
          <el-icon :size="24"><Lock /></el-icon>
          <span>登录查看详细学习进度与获得证书</span>
          <el-button type="primary" size="small" round @click="router.push({ path: '/login', query: { redirect: '/training' } })">
            立即登录
          </el-button>
        </div>
      </div>
    </div>

    <!-- 2. 继续学习卡片 -->
    <div class="resume-card" v-if="resumeCourse && activeTab === 'all'">
      <el-image :src="resumeCourse.coverImage" class="resume-cover" fit="cover" lazy>
        <template #error>
          <div class="image-placeholder"><el-icon><Picture /></el-icon></div>
        </template>
      </el-image>
      <div class="resume-main">
        <h3 class="resume-title">{{ resumeCourse.title }}</h3>
        <div style="display: flex; align-items: center; justify-content: space-between; gap: 10px; margin: 8px 0;">
          <el-progress :percentage="resumeCourse.progress || 0" :color="resumeCourse.progress === 100 ? '#67C23A' : '#00BFA6'" :stroke-width="6" style="flex: 1" />
        </div>
        <p class="resume-meta">讲师: {{ resumeCourse.instructor }}</p>
      </div>
      <button class="resume-btn" @click="goLearning(resumeCourse.id)" :style="{ background: resumeCourse.progress === 100 ? 'linear-gradient(135deg, #67C23A, #85ce61)' : '' }">
        {{ resumeCourse.progress === 100 ? '再次学习' : '继续学习' }}
      </button>
    </div>

    <!-- 骨架屏?-->
    <div v-if="loading && mandatoryCourses.length === 0" class="course-grid">
      <div v-for="i in 6" :key="i" class="skeleton-card">
        <el-skeleton animated>
          <template #template>
            <el-skeleton-item variant="image" style="height: 160px" />
            <div style="padding: 14px">
              <el-skeleton-item variant="h3" style="width: 80%" />
              <el-skeleton-item variant="text" style="width: 50%; margin-top: 10px" />
              <el-skeleton-item variant="text" style="width: 60%; margin-top: 8px" />
            </div>
          </template>
        </el-skeleton>
      </div>
    </div>

    <!-- 3. 课程网格 -->
    <div v-else>
      <!-- 全部页签：分类展示?-->
      <div v-if="activeTab === 'all'">
        <!-- 必修课程预览 -->
        <div class="section-header" v-if="requiredPreview.length > 0">
          <h3 class="section-title">📌 必修课程</h3>
          <span class="more-link" @click="activeTab = 'required'; currentPage = 1" style="cursor: pointer;">
            更多课程 <el-icon><ArrowRight /></el-icon>
          </span>
        </div>
        <div class="course-grid" v-if="requiredPreview.length > 0">
          <div
            v-for="(course, idx) in requiredPreview"
            :key="course.id"
            class="course-card"
            :style="{ animationDelay: `${idx * 0.04}s` }"
            @click="goLearning(course.id)"
          >
            <div class="card-cover">
              <el-image :src="course.coverImage" fit="cover" lazy class="cover-img">
                <template #error><div class="image-placeholder"><el-icon><Picture /></el-icon></div></template>
              </el-image>
              <div class="cover-gradient"></div>
              <el-tag class="type-badge" size="small" type="danger" effect="dark">必修</el-tag>
            </div>
            <div class="card-body">
              <h4 class="card-title">{{ course.title }}</h4>
              <div class="card-meta">
                <span class="instructor">{{ course.instructor || '培训讲师' }}</span>
              </div>
              <el-progress :percentage="course.progress || 0" :stroke-width="5" :show-text="false" :color="course.passed ? '#67C23A' : '#00BFA6'" style="margin: 8px 0" />
              <button class="learn-btn" @click.stop="goLearning(course.id)">{{ course.passed ? '再次学习' : '开始学习' }}</button>
            </div>
          </div>
        </div>

        <!-- 选修课程预览 -->
        <div class="section-header" style="margin-top: 24px;" v-if="electivePreview.length > 0">
          <h3 class="section-title">📖 选修课程</h3>
          <span class="more-link" @click="activeTab = 'elective'; currentPage = 1" style="cursor: pointer;">
            更多选修 <el-icon><ArrowRight /></el-icon>
          </span>
        </div>
        <div class="course-grid" v-if="electivePreview.length > 0">
          <div
            v-for="(course, idx) in electivePreview"
            :key="course.id"
            class="course-card"
            :style="{ animationDelay: `${idx * 0.04}s` }"
            @click="goLearning(course.id)"
          >
            <div class="card-cover">
              <el-image :src="course.coverImage" fit="cover" lazy class="cover-img">
                <template #error><div class="image-placeholder"><el-icon><Picture /></el-icon></div></template>
              </el-image>
              <div class="cover-gradient"></div>
              <el-tag class="type-badge" size="small" type="success" effect="dark">选修</el-tag>
            </div>
            <div class="card-body">
              <h4 class="card-title">{{ course.title }}</h4>
              <div class="card-meta">
                <span class="instructor">{{ course.instructor || '培训讲师' }}</span>
              </div>
              <el-progress :percentage="course.progress || 0" :stroke-width="5" :show-text="false" :color="course.passed ? '#67C23A' : '#00BFA6'" style="margin: 8px 0" />
              <button class="learn-btn" @click.stop="goLearning(course.id)">{{ course.passed ? '再次学习' : '开始学习' }}</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 特定分类：分页展示?-->
      <div v-else>
        <TransitionGroup name="card-fade" tag="div" class="course-grid">
          <div
            v-for="(course, idx) in displayCourses"
            :key="course.id"
            class="course-card"
            :style="{ animationDelay: `${idx * 0.04}s` }"
            @click="goLearning(course.id)"
          >
            <div class="card-cover">
              <el-image :src="course.coverImage" fit="cover" lazy class="cover-img">
                <template #error>
                  <div class="image-placeholder"><el-icon><Picture /></el-icon></div>
                </template>
              </el-image>
              <div class="cover-gradient"></div>
              <el-tag v-if="course.courseType" class="type-badge" size="small" :type="course.courseType === 'required' ? 'danger' : 'success'" effect="dark">{{ course.courseType === 'required' ? '必修' : '选修' }}</el-tag>
            </div>
            <div class="card-body">
              <h4 class="card-title">{{ course.title }}</h4>
              <div class="card-meta">
                <span class="instructor">{{ course.instructor || '培训讲师' }}</span>
              </div>
              <el-progress
                :percentage="course.progress || 0"
                :stroke-width="5" :show-text="false"
                :color="course.passed ? '#67C23A' : '#00BFA6'"
                style="margin: 8px 0"
              />
              <button class="learn-btn" @click.stop="goLearning(course.id)">
                {{ course.passed ? '再次学习' : '开始学习' }}
              </button>
            </div>
          </div>
        </TransitionGroup>

        <el-empty v-if="displayCourses.length === 0 && !loading" description="暂无课程" :image-size="100" />
        
        <div class="pagination-container" v-if="totalCourses > pageSize">
          <el-pagination
            background
            layout="prev, pager, next"
            :total="totalCourses"
            :page-size="pageSize"
            v-model:current-page="currentPage"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>

    <div class="recommend-section" v-if="recommendedCourses.length > 0 && activeTab === 'all'">
      <div class="section-header">
        <h3 class="section-title"><el-icon style="vertical-align: middle; margin-right: 4px; color: #f5222d;"><StarFilled /></el-icon>猜你喜欢</h3>
        <span class="refresh-btn" @click="fetchRecommendedCourses">
          <el-icon><Refresh /></el-icon> 换一批
        </span>
      </div>
      <div class="recommend-grid-container">
        <div class="recommend-grid">
          <div
            v-for="(rc, idx) in displayRecommended"
            :key="rc.id"
            class="recommend-card"
            :style="{ animationDelay: `${idx * 0.05}s` }"
            @click="goLearning(rc.id, true)"
          >
            <div class="rec-cover">
              <el-image :src="rc.coverImage" fit="cover" lazy>
                <template #error>
                  <div class="rec-placeholder" :style="{ background: rc.color }">
                    <span>{{ rc.title?.slice(0, 2) }}</span>
                  </div>
                </template>
              </el-image>
              <div class="rec-badge" v-if="rc.category">{{ rc.category }}</div>
            </div>
            <div class="rec-info">
              <h5 class="rec-title">{{ rc.title }}</h5>
              <div class="rec-meta">
                <el-icon><View /></el-icon> {{ rc.viewCount || 0 }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 4. 待考提醒 -->
    <div class="exam-section" v-if="pendingExams.length > 0 && activeTab === 'all'">
      <h3 class="section-title"><el-icon style="vertical-align: middle; margin-right: 4px; color: #faad14;"><Warning /></el-icon>待考提醒</h3>
      <div class="exam-list">
        <div v-for="exam in pendingExams" :key="exam.id" class="exam-alert">
          <div class="exam-text">
            <span>《{{ exam.courseTitle || '相关课程' }}》考试未通过</span>
          </div>
          <el-button type="warning" size="small" round @click="goExam(exam.id)">去查看</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Timer, Collection, Trophy, DataAnalysis,
  Picture, ArrowRight, Search, Refresh, Lock,
  Menu, Flag, Notebook, CircleCheck, StarFilled, Warning
} from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import { useMobile } from '@/composables/useMobile'

const { isMobile } = useMobile()
const router = useRouter()
const loading = ref(false)
const searchKeyword = ref('')
const activeTab = ref('all')
const isLoggedIn = computed(() => !!localStorage.getItem('token'))

const tabs = [
  { label: '全部', value: 'all', icon: Menu },
  { label: '必修', value: 'required', icon: Flag },
  { label: '选修', value: 'elective', icon: Notebook },
  { label: '已完成', value: 'passed', icon: CircleCheck }
]

// 1. 数据概览
const statsData = ref({
  totalHours: 0,
  completedCount: 0,
  certificateCount: 0,
  averageScore: 0
})

const stats = ref([
  { label: '累计学时', key: 'totalHours', unit: 'h', icon: Timer, color: '#409eff' },
  { label: '课程完成', key: 'completedCount', unit: '个', icon: Collection, color: '#67c23a' },
  { label: '获得证书', key: 'certificateCount', unit: '本', icon: Trophy, color: '#e6a23c' },
  { label: '考试均分', key: 'averageScore', unit: '分', icon: DataAnalysis, color: '#909399' }
])

// 2. 继续学习
const resumeCourse = ref<any>(null)

// 3. 课程
const mandatoryCourses = ref<any[]>([])

// 4. 待考提醒?
const pendingExams = ref<any[]>([])

const currentPage = ref(1)
const pageSize = ref(8)
const totalCourses = ref(0)

const filteredList = computed(() => {
  let list = mandatoryCourses.value

  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    list = list.filter(c => c.title?.toLowerCase().includes(kw))
  }

  switch (activeTab.value) {
    case 'required':
      return list.filter(c => c.courseType === 'required')
    case 'elective':
      return list.filter(c => c.courseType === 'elective')
    case 'passed':
      return list.filter(c => c.passed)
    default:
      return list
  }
})

const displayCourses = computed(() => {
  totalCourses.value = filteredList.value.length
  const start = (currentPage.value - 1) * pageSize.value
  return filteredList.value.slice(start, start + pageSize.value)
})

const requiredPreview = computed(() => {
  return mandatoryCourses.value.filter(c => c.courseType === 'required').slice(0, 4)
})

const electivePreview = computed(() => {
  return mandatoryCourses.value.filter(c => c.courseType === 'elective').slice(0, 4)
})

import { watch } from 'vue'
watch(activeTab, () => {
  currentPage.value = 1
  searchKeyword.value = ''
})

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  window.scrollTo({ top: 300, behavior: 'smooth' })
}

// 核心逻辑：手机端强制 2x2 (4个)?，PC 端一行?10 个?
const displayRecommended = computed(() => {
  if (isMobile.value) {
    return recommendedCourses.value.slice(0, 4)
  }
  return recommendedCourses.value.slice(0, 8)
})

const handleSearch = () => { /* computed handles it */ }

const fetchData = async () => {
  if (!isLoggedIn.value) {
    // 游客：只加载基础必修课程列表
    loading.value = true
    try {
      const res = await request.get('/course/mandatory')
      if (res.code === 200) mandatoryCourses.value = res.data || []
    } catch (e) { console.error('游客获取课程失败', e) }
    finally { loading.value = false }
    return
  }

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

const recommendedCourses = ref<any[]>([])

const fetchRecommendedCourses = async () => {
  try {
    const res = await request.get('/course/recommend', { params: { limit: 10 } })
    if (res.code === 200 && res.data?.length) {
      const colors = [
        'linear-gradient(135deg, #667eea, #764ba2)',
        'linear-gradient(135deg, #f093fb, #f5576c)',
        'linear-gradient(135deg, #4facfe, #00f2fe)',
        'linear-gradient(135deg, #43e97b, #38f9d7)'
      ]
      recommendedCourses.value = res.data.map((c: any, i: number) => ({
        ...c,
        color: colors[i % colors.length]
      }))
    }
  } catch (e) {
    console.error('\u83b7\u53d6\u63a8\u8350\u8bfe\u7a0b\u5931\u8d25:', e)
  }
}

const goLearning = async (id: number, isRecommend = false) => {
  // 1. 追踪点击 (只要点击了课程就追踪，无论是否来自推荐位)
  try {
    await request.post('/course/recommend/click', {}, { params: { courseId: id } })
  } catch (e) {
    console.error('Track click failed', e)
  }

  // 2. 跳转
  router.push(`/training/detail/${id}`)

  // 3. 如果是从推荐位点进去的，刷新一下推荐列表，这样回来时能看到更新
  if (isRecommend) {
    fetchRecommendedCourses()
  }
}

const goExam = (_id: number) => {
  router.push('/training/my')
}

onMounted(() => {
  fetchData()
  fetchRecommendedCourses()
})
</script>

<style scoped lang="scss">
@keyframes slideUp {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes cardFadeIn {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

.training-index {
  padding: 0;
  background: #f5f7fa;
  min-height: 100vh;
}

// ================================================================
// Hero
// ================================================================
.hero-section {
  background: linear-gradient(135deg, #00BFA6 0%, #009688 50%, #00695C 100%);
  padding: 24px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  animation: slideUp 0.3s ease-out;
}

.hero-inner {
  .hero-title {
    font-size: 22px;
    font-weight: 700;
    color: #fff;
    margin: 0 0 4px;
  }

  .hero-sub {
    font-size: 13px;
    color: rgba(255, 255, 255, 0.8);
    margin: 0;
  }
}

.exam-entry {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #fff;
  font-size: 13px;
  text-decoration: none;
  padding: 6px 14px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 16px;
  font-weight: 500;
  backdrop-filter: blur(4px);
  transition: background 0.2s;

  &:hover { background: rgba(255, 255, 255, 0.25); }
}

// ================================================================
// 搜索
// ================================================================
.search-bar {
  margin: 16px;
  margin-bottom: 0;

  :deep(.el-input__wrapper) {
    border-radius: 10px;
    background: #fff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04) !important;
  }
}

// ================================================================
// Tab
// ================================================================
.filter-tabs {
  display: flex;
  gap: 12px;
  padding: 0 16px;
  margin: 16px 0;
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
    transition: transform 0.3s;
  }

  &.active {
    color: #00BFA6;
    background: rgba(0, 191, 166, 0.08);
    border-color: rgba(0, 191, 166, 0.2);
    font-weight: 700;
    box-shadow: 0 4px 12px rgba(0, 191, 166, 0.1);

    .tab-icon {
      transform: scale(1.1);
    }
  }

  &:hover:not(.active) {
    background: #f8fafc;
    color: #1e293b;
    transform: translateY(-1px);
  }
}

// ================================================================
// Stats
// ================================================================
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  padding: 0 16px;
  margin-bottom: 16px;

  @media (max-width: 768px) { grid-template-columns: repeat(2, 1fr); }
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s;
  animation: slideUp 0.4s ease-out both;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
  }

  .stat-value {
    font-size: 22px;
    font-weight: bold;
    color: #303133;
  }

  .stat-label {
    font-size: 12px;
    color: #909399;
    margin-top: 2px;
  }
}

// ================================================================
// Guest Stats Mask
// ================================================================
.stats-section {
  position: relative;
  margin-bottom: 16px;
}

.guest-stats-mask {
  position: absolute;
  inset: 0 16px;
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  border: 1px solid rgba(255, 255, 255, 0.5);
  animation: fadeIn 0.4s ease-out;

  .mask-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    color: #444;

    span {
      font-size: 13px;
      font-weight: 500;
    }

    .el-button {
      margin-top: 4px;
      padding: 8px 16px;
      font-weight: 600;
      box-shadow: 0 4px 12px rgba(0, 191, 166, 0.2);
    }
  }
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

// ================================================================
// Resume Card
// ================================================================
.resume-card {
  margin: 0 16px 16px;
  padding: 16px;
  background: #fff;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  animation: slideUp 0.4s ease-out 0.1s both;
  transition: box-shadow 0.3s;

  &:hover { box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08); }

  .resume-cover {
    width: 100px;
    height: 70px;
    border-radius: 8px;
    flex-shrink: 0;
  }

  .resume-main {
    flex: 1;
    min-width: 0;

    .resume-title {
      font-size: 15px;
      font-weight: 600;
      margin: 0 0 6px;
      color: #333;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .resume-meta {
      font-size: 12px;
      color: #999;
      margin: 4px 0 0;
    }
  }
}

.resume-btn {
  border: none;
  background: linear-gradient(135deg, #00BFA6, #43e97b);
  color: #fff;
  padding: 8px 20px;
  border-radius: 18px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  flex-shrink: 0;
  transition: all 0.25s;

  &:hover {
    transform: scale(1.05);
    box-shadow: 0 4px 14px rgba(0, 191, 166, 0.3);
  }
}

// ================================================================
// Section Header
// ================================================================
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
  margin-bottom: 12px;
}

.section-title {
  font-size: 17px;
  font-weight: 700;
  color: #333;
  margin: 0;
}

.more-link {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 13px;
  color: #00BFA6;
  text-decoration: none;
  font-weight: 500;
}

// ================================================================
// Course Grid & Pagination
// ================================================================
.course-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  padding: 0 16px;
  margin-bottom: 16px;
  
  @media (min-width: 992px) {
    grid-template-columns: repeat(4, 1fr); // PC 4 cards per row
    gap: 16px;
  }
}

.pagination-container {
  display: flex;
  justify-content: center;
  padding: 16px;
  margin-bottom: 24px;
}

.skeleton-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}

.course-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  animation: cardFadeIn 0.4s ease-out both;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  &:active { transform: scale(0.97); }
}

.card-cover {
  position: relative;
  height: 120px;
  overflow: hidden;

  .cover-img { width: 100%; height: 100%; }

  .cover-gradient {
    position: absolute;
    inset: 0;
    background: linear-gradient(to bottom, transparent 50%, rgba(0, 0, 0, 0.25) 100%);
    pointer-events: none;
  }

  .type-badge {
    position: absolute;
    top: 8px;
    left: 8px;
    z-index: 2;
  }
}

.card-body {
  padding: 10px 12px 12px;

  .card-title {
    font-size: 14px;
    font-weight: 600;
    color: #333;
    margin: 0 0 6px;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    line-height: 1.4;
    min-height: 39px;
  }

  .card-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .instructor { font-size: 11px; color: #999; }
    .reward { font-size: 11px; color: #F7971E; font-weight: 600; }
  }
}

.learn-btn {
  width: 100%;
  border: none;
  background: linear-gradient(135deg, #00BFA6, #43e97b);
  color: #fff;
  padding: 7px 0;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 191, 166, 0.3);
  }

  &:active { transform: scale(0.97); }
}

// ================================================================
// Exam Alerts
// ================================================================
.exam-section {
  padding: 0 16px;
  margin-bottom: 24px;

  .section-title { margin-bottom: 12px; }
}

.exam-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.exam-alert {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #FFF8E1;
  border-radius: 10px;
  padding: 12px 16px;
  animation: slideUp 0.3s ease-out both;

  .exam-text {
    font-size: 13px;
    color: #E6A23C;
    font-weight: 500;
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
// PC
// ================================================================
@media (min-width: 769px) {
  .training-index {
    max-width: 1440px;
    margin: 0 auto;
    padding: 0 0 40px;
  }

  .hero-section { border-radius: 0 0 16px 16px; }

  .course-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
  }

  .course-card {
    transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);

    &:hover {
      transform: translateY(-6px);
      box-shadow: 0 12px 28px rgba(0, 0, 0, 0.12);
    }

    .card-cover { height: 160px; }
  }

  .resume-card {
    margin: 0 16px 20px;
    padding: 20px;
    gap: 24px;

    .resume-cover { width: 180px; height: 100px; }
  }
}

// ================================================================
// 移动端?
// ================================================================
@media (max-width: 768px) {
  .resume-card {
    flex-wrap: wrap;

    .resume-cover { width: 80px; height: 56px; }
    .resume-btn { width: 100%; text-align: center; margin-top: 4px; }
  }
}

// ================================================================
// Recommend Section (Responsive Grid)
// ================================================================
.recommend-section {
  padding: 0 16px;
  margin-top: 12px;
  margin-bottom: 24px;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
  }

  .refresh-btn {
    font-size: 13px;
    color: #00BFA6;
    font-weight: 600;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 4px;
    transition: opacity 0.2s;
    &:hover { opacity: 0.8; }
  }
}

.recommend-grid-container {
  width: 100%;
  overflow: hidden; // 禁止滑动
  padding-bottom: 8px;
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr); // 手机端两两排列?
  gap: 12px;
  
  @media (min-width: 992px) {
    grid-template-columns: repeat(8, 1fr); // PC端单行?个，不可滑动
    gap: 16px;
  }
}

.recommend-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  animation: cardFadeIn 0.4s ease both;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
    .rec-cover :deep(img) { transform: scale(1.1); }
  }

  .rec-cover {
    position: relative;
    height: 80px;
    background: #eee;
    overflow: hidden;

    @media (min-width: 992px) { height: 110px; }

    :deep(.el-image) { width: 100%; height: 100%; }
    :deep(img) { transition: transform 0.5s; }

    .rec-placeholder {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-weight: bold;
      font-size: 16px;
    }

    .rec-badge {
      position: absolute;
      top: 6px;
      right: 6px;
      background: rgba(0, 0, 0, 0.4);
      backdrop-filter: blur(4px);
      color: #fff;
      font-size: 10px;
      padding: 2px 6px;
      border-radius: 4px;
    }
  }

  .rec-info {
    padding: 8px 10px;

    .rec-title {
      font-size: 13px;
      margin: 0 0 4px;
      font-weight: 600;
      color: #333;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .rec-meta {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 11px;
      color: #999;
    }
  }
}

.recommend-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  animation: cardFadeIn 0.4s ease both;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
  }

  .rec-cover {
    height: 70px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: rgba(255, 255, 255, 0.9);
    font-size: 28px;
    font-weight: 800;
    
    @media (min-width: 1024px) {
      height: 100px; // PC端放大卡片图片?
      font-size: 36px;
    }

    .rec-icon {
      filter: drop-shadow(0 2px 4px rgba(0,0,0,0.1));
    }
  }

  .rec-info {
    padding: 8px 10px;

    .rec-title {
      font-size: 13px;
      margin: 0 0 4px;
      font-weight: 600;
      color: #333;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .rec-meta {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 11px;
      color: #999;
      
      .rec-dot { font-weight: bold; }
    }
  }
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #ccc;
}

/* 暗黑模式 */
html.dark .training-index {
  background-color: #0f172a !important;

  .stat-card { background: #1e293b !important; }
  .stat-value { color: #e2e8f0 !important; }
  .resume-card { background: #1e293b !important; }
  .course-card { background: #1e293b !important; }
  .card-title { color: #f1f5f9 !important; }
  .section-title { color: #f1f5f9 !important; }
}
</style>
