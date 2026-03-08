<template>
  <div class="course-discover" :class="{ 'is-mobile': isMobile }">

    <!-- ==================== 头部 ==================== -->
    <div class="discover-header">
      <div class="header-top">
        <div>
          <h1 class="header-title">📚 培训学院</h1>
          <p class="header-sub">提升技能，获取积分奖励</p>
        </div>
        <router-link to="/training/my" class="exam-entry">
          <el-icon><Document /></el-icon>
          <span>考试记录</span>
        </router-link>
      </div>

      <!-- 搜索栏 -->
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
    </div>

    <!-- ==================== 骨架屏 ==================== -->
    <div v-if="loading && courseList.length === 0" class="skeleton-section">
      <div class="course-grid">
        <div v-for="i in (isMobile ? 3 : 6)" :key="i" class="skeleton-card">
          <el-skeleton animated>
            <template #template>
              <el-skeleton-item variant="image" style="height: 180px" />
              <div style="padding: 14px">
                <el-skeleton-item variant="h3" style="width: 80%" />
                <el-skeleton-item variant="text" style="width: 60%; margin-top: 10px" />
                <el-skeleton-item variant="text" style="width: 40%; margin-top: 8px" />
              </div>
            </template>
          </el-skeleton>
        </div>
      </div>
    </div>

    <!-- ==================== 课程网格 ==================== -->
    <div v-else class="course-section">
      <div class="section-head" v-if="searchKeyword">
        <span class="section-title">🔍 搜索结果</span>
        <span class="section-count">{{ displayCourses.length }} 门课程</span>
      </div>

      <TransitionGroup name="card-fade" tag="div" class="course-grid">
        <div
          v-for="(course, idx) in displayCourses"
          :key="course.id"
          class="course-card"
          :style="{ animationDelay: `${idx * 0.04}s` }"
          @click="goToDetail(course.id)"
        >
          <!-- 封面 -->
          <div class="card-cover">
            <el-image :src="course.coverImage || getPlaceholder(course.id)" fit="cover" lazy>
  <template #error>
    <img :src="'/default-cover.jpg'" style="width:100%;height:100%;object-fit:cover"/>
  </template>
</el-image>
            <div class="cover-gradient"></div>
            <div class="duration-chip">
              <el-icon><VideoCamera /></el-icon> {{ course.duration || 30 }}min
            </div>
            <div v-if="course.passed" class="passed-badge">✅ 已通过</div>
          </div>

          <!-- 信息 -->
          <div class="card-body">
            <h4 class="card-title">{{ course.title }}</h4>
            <div class="card-tags">
              <el-tag size="small" type="info" effect="plain">{{ course.category || '通识' }}</el-tag>
              <span class="card-reward">🏆 {{ course.creditHours || course.points || 20 }}</span>
            </div>
            <el-progress
              :percentage="course.passed ? 100 : Math.floor(Math.random() * 50)"
              :stroke-width="5" :show-text="false"
              :color="course.passed ? '#67C23A' : '#00BFA6'"
              style="margin: 8px 0"
            />
            <div class="card-footer">
              <span class="card-instructor">{{ course.instructor || '培训中心' }}</span>
              <button class="learn-btn" @click.stop="goToDetail(course.id)">
                {{ course.passed ? '再次学习' : '开始学习' }}
              </button>
            </div>
          </div>
        </div>
      </TransitionGroup>

      <el-empty v-if="!loading && displayCourses.length === 0" description="暂无课程" :image-size="100" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, VideoCamera, Document, Menu, Flag, Notebook, CircleCheck } from '@element-plus/icons-vue'
import { request } from '@/utils/request'

interface Course {
  id: number; title: string; coverImage?: string; category?: string
  instructor?: string; duration?: number; difficulty?: number
  creditHours?: number; points?: number; passed?: boolean
  courseType?: string // 'required' | 'elective'
}

const router = useRouter()

// ================== 响应式 ==================
const windowWidth = ref(window.innerWidth)
const isMobile = computed(() => windowWidth.value < 768)
onMounted(() => window.addEventListener('resize', () => { windowWidth.value = window.innerWidth }))

// ================== 状态 ==================
const loading = ref(false)
const searchKeyword = ref('')
const courseList = ref<Course[]>([])
const activeTab = ref('all')

const tabs = [
  { label: '全部', value: 'all', icon: Menu },
  { label: '必修', value: 'required', icon: Flag },
  { label: '选修', value: 'elective', icon: Notebook },
  { label: '已完成', value: 'passed', icon: CircleCheck }
]

// ================== 分区 ==================
const requiredCategories = ['志愿服务基础', '安全培训', '法律知识']
const electiveCategories = ['助老服务', '支教培训', '心理辅导', '团队管理']

const displayCourses = computed(() => {
  let list = courseList.value

  // 搜索过滤
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    list = list.filter(c => c.title.toLowerCase().includes(kw))
  }

  // Tab 过滤
  switch (activeTab.value) {
    case 'required':
      return list.filter(c => c.courseType === 'required' || requiredCategories.includes(c.category || ''))
    case 'elective':
      return list.filter(c => c.courseType === 'elective' || electiveCategories.includes(c.category || ''))
    case 'passed':
      return list.filter(c => c.passed)
    default:
      return list
  }
})

// ================== 占位图 ==================
const getPlaceholder = (id: number) => {
  const colors = ['667eea', 'f093fb', '4facfe', '43e97b', 'fa709a', '00BFA6', 'FF6B6B', 'FFA502']
  return `https://via.placeholder.com/400x240/${colors[id % colors.length]}/fff?text=${encodeURIComponent('课程')}`
}

// ================== API ==================
const fetchCourses = async () => {
  loading.value = true
  try {
    const res = await request.get('/course/list')
    if (res.code === 200) courseList.value = res.data || []
  } catch (e) {
    console.error('获取课程列表失败:', e)
    courseList.value = generateMockCourses()
  } finally { loading.value = false }
}

const generateMockCourses = (): Course[] => {
  const titles = [
    '志愿服务入门培训', '急救知识培训', '沟通技巧提升',
    '团队协作训练', '公共场所安全', '服务礼仪规范'
  ]
  const cats = ['志愿服务基础', '急救技能', '助老服务', '支教培训', '安全培训', '法律知识']
  return titles.map((title, i) => ({
    id: i + 1, title, category: cats[i], instructor: '培训讲师',
    duration: 20 + i * 5, difficulty: (i % 3) + 1,
    creditHours: 20 + i * 5, passed: i < 3
  }))
}

const handleSearch = () => { /* computed handles it */ }

const goToDetail = (id: number) => { router.push(`/training/detail/${id}`) }

onMounted(fetchCourses)
</script>

<style lang="scss" scoped>
@keyframes cardFadeIn {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}

.course-discover {
  min-height: 100vh;
  background: var(--app-bg);
  padding-bottom: 40px;
}

// ================================================================
// 头部
// ================================================================
.discover-header {
  background: #fff;
  padding: 16px;
  padding-bottom: 0;
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;

  .header-title {
    font-size: 22px;
    font-weight: 700;
    color: #333;
    margin: 0 0 2px;
  }

  .header-sub {
    font-size: 13px;
    color: #999;
    margin: 0;
  }
}

.exam-entry {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--primary-color);
  font-size: 13px;
  text-decoration: none;
  white-space: nowrap;
  padding: 6px 12px;
  background: rgba(0, 191, 166, 0.08);
  border-radius: 16px;
  font-weight: 500;
}

.search-bar {
  margin-bottom: 12px;
  :deep(.el-input__wrapper) {
    border-radius: 10px;
    background: #F5F5F5;
    box-shadow: none !important;
  }
}

// ================================================================
// 分类 Tab
// ================================================================
.filter-tabs {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  padding: 4px 0 12px;

  &::-webkit-scrollbar { display: none; }
}

.filter-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #f8fafc;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid transparent;

  .tab-icon {
    font-size: 16px;
  }

  &.active {
    color: var(--primary-color);
    background: rgba(0, 191, 166, 0.1);
    border-color: rgba(0, 191, 166, 0.2);
    font-weight: 700;
  }
}

// ================================================================
// 骨架屏
// ================================================================
.skeleton-section {
  padding: 16px;
}

.skeleton-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}

// ================================================================
// 课程网格
// ================================================================
.course-section {
  padding: 16px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;

  .section-title {
    font-size: 17px;
    font-weight: 700;
    color: #333;
  }

  .section-count {
    font-size: 13px;
    color: #999;
  }
}

.course-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.course-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  animation: cardFadeIn 0.4s ease-out both;

  &:active { transform: scale(0.97); }
}

// ================================================================
// 卡片封面
// ================================================================
.card-cover {
  position: relative;
  height: 140px;
  overflow: hidden;

  :deep(.el-image) { width: 100%; height: 100%; }

  .cover-gradient {
    position: absolute;
    inset: 0;
    background: linear-gradient(to bottom, transparent 40%, rgba(0, 0, 0, 0.3) 100%);
    pointer-events: none;
  }

  .duration-chip {
    position: absolute;
    bottom: 8px;
    left: 8px;
    background: rgba(0, 0, 0, 0.6);
    color: #fff;
    padding: 2px 8px;
    border-radius: 4px;
    font-size: 11px;
    display: flex;
    align-items: center;
    gap: 3px;
    backdrop-filter: blur(4px);
  }

  .passed-badge {
    position: absolute;
    top: 8px;
    right: 8px;
    background: #67C23A;
    color: #fff;
    padding: 2px 8px;
    border-radius: 4px;
    font-size: 11px;
    font-weight: 600;
  }
}

// ================================================================
// 卡片主体
// ================================================================
.card-body {
  padding: 12px;

  .card-title {
    font-size: 14px;
    font-weight: 600;
    color: #333;
    margin: 0 0 8px;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    line-height: 1.4;
    min-height: 39px;
  }

  .card-tags {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 4px;

    .card-reward {
      font-size: 11px;
      color: #F7971E;
      font-weight: 600;
      white-space: nowrap;
    }
  }
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .card-instructor {
    font-size: 11px;
    color: #999;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 60px;
  }
}

.learn-btn {
  border: none;
  background: linear-gradient(135deg, var(--primary-color), #43e97b);
  color: #fff;
  padding: 5px 14px;
  border-radius: 14px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s;
  white-space: nowrap;

  &:hover {
    transform: scale(1.06);
    box-shadow: 0 4px 12px rgba(0, 191, 166, 0.3);
  }

  &:active { transform: scale(0.97); }
}

// ================================================================
// TransitionGroup 动画
// ================================================================
.card-fade-enter-active { transition: all 0.35s ease-out; }
.card-fade-leave-active { transition: all 0.2s ease-in; }
.card-fade-enter-from { opacity: 0; transform: translateY(16px); }
.card-fade-leave-to { opacity: 0; transform: scale(0.96); }
.card-fade-move { transition: transform 0.3s ease; }

// ================================================================
// PC 适配
// ================================================================
@media (min-width: 769px) {
  .course-discover {
    max-width: 1440px;
    margin: 0 auto;
    padding: 20px;
    background: transparent;

    @media (min-width: 1200px) and (max-width: 1600px) { max-width: 90%; }
    @media (min-width: 1601px) { max-width: 1600px; }
  }

  .discover-header {
    border-radius: 12px;
    margin-bottom: 8px;
  }

  .course-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
  }

  .course-card {
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
    transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);

    &:hover {
      transform: translateY(-6px);
      box-shadow: 0 12px 28px rgba(0, 0, 0, 0.12);
    }

    .card-cover { height: 180px; }
  }
}

// ================================================================
// 移动端微调
// ================================================================
.is-mobile {
  .course-grid { grid-template-columns: 1fr; }
  .card-cover { height: 160px; }

  .card-footer {
    .card-instructor { max-width: 120px; }
  }

  .learn-btn {
    padding: 6px 18px;
    font-size: 13px;
  }
}
</style>
