<template>
  <div class="course-list-page">
    <div class="page-header">
      <div class="header-row">
        <div>
          <h2>📚 培训学院</h2>
          <p class="subtitle">提升技能，获取积分奖励</p>
        </div>
        <router-link to="/training/my" class="exam-link">
          <el-icon><Document /></el-icon>
          查看我的考试记录
        </router-link>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="filter-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索课程名称..."
        prefix-icon="Search"
        clearable
        style="width: 300px"
        @input="handleSearch"
      />
      <el-radio-group v-model="activeCategory" @change="fetchCourses" class="category-filters">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="志愿服务基础">志愿服务基础</el-radio-button>
        <el-radio-button value="急救技能">急救技能</el-radio-button>
        <el-radio-button value="助老服务">助老服务</el-radio-button>
        <el-radio-button value="支教培训">支教培训</el-radio-button>
        <el-radio-button value="心理辅导">心理辅导</el-radio-button>
        <el-radio-button value="法律知识">法律知识</el-radio-button>
        <el-radio-button value="安全培训">安全培训</el-radio-button>
        <el-radio-button value="团队管理">团队管理</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 课程统计 -->
    <div class="stats-bar">
      <span class="count">共 {{ filteredCourses.length }} 门课程</span>
    </div>

    <!-- 课程列表 -->
    <div class="course-grid" v-loading="loading">
      <div
        v-for="course in filteredCourses"
        :key="course.id"
        class="course-card"
        @click="goToDetail(course.id)"
      >
        <div class="course-cover">
          <el-image :src="course.coverImage || getPlaceholder(course.id)" :alt="course.title" fit="cover" lazy>
            <template #error>
              <div class="cover-placeholder">
                <el-icon :size="40"><VideoCamera /></el-icon>
              </div>
            </template>
          </el-image>
          <div class="duration-badge">
            <el-icon><VideoCamera /></el-icon>
            {{ course.duration || 30 }}分钟
          </div>
          <el-tag
            :type="getCategoryType(course.category)"
            class="category-tag"
            size="small"
            effect="dark"
          >
            {{ course.category || '通识' }}
          </el-tag>
          <div v-if="course.passed" class="passed-badge">
            <el-icon><CircleCheck /></el-icon>
            已通过
          </div>
        </div>
        <div class="course-info">
          <h3 class="course-title">{{ course.title }}</h3>
          <p class="course-instructor">
            <el-icon><User /></el-icon>
            {{ course.instructor || '志愿者培训中心' }}
          </p>
          <div class="course-meta">
            <span class="difficulty">
              <span v-for="i in 3" :key="i" :class="{ active: i <= (course.difficulty || 1) }">★</span>
            </span>
            <span class="reward">
              <el-icon><Trophy /></el-icon>
              通过奖励 {{ course.creditHours || course.points || 20 }} 积分
            </span>
          </div>
        </div>
      </div>

      <el-empty v-if="!loading && filteredCourses.length === 0" description="暂无课程" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { VideoCamera, User, Trophy, Document, CircleCheck } from '@element-plus/icons-vue'
import { request } from '@/utils/request'

interface Course {
  id: number
  title: string
  coverImage?: string
  category?: string
  instructor?: string
  duration?: number
  difficulty?: number
  creditHours?: number
  points?: number
  passed?: boolean
}

const router = useRouter()
const loading = ref(false)
const activeCategory = ref('')
const searchKeyword = ref('')
const courseList = ref<Course[]>([])

const placeholderImages = [
  'https://picsum.photos/seed/course1/400/225',
  'https://picsum.photos/seed/course2/400/225',
  'https://picsum.photos/seed/course3/400/225',
  'https://picsum.photos/seed/course4/400/225',
  'https://picsum.photos/seed/course5/400/225',
]

const getPlaceholder = (id: number) => {
  return placeholderImages[id % placeholderImages.length]
}

const getCategoryType = (category?: string) => {
  const map: Record<string, string> = {
    '志愿服务基础': 'primary',
    '急救技能': 'danger',
    '助老服务': 'success',
    '支教培训': 'warning',
    '心理辅导': 'primary',
    '法律知识': 'info',
    '安全培训': 'danger',
    '团队管理': 'warning'
  }
  return map[category || ''] || 'info'
}

const filteredCourses = computed(() => {
  if (!searchKeyword.value) return courseList.value
  const keyword = searchKeyword.value.toLowerCase()
  return courseList.value.filter(course =>
    course.title.toLowerCase().includes(keyword)
  )
})

const handleSearch = () => {
  // 搜索通过 computed 自动过滤
}

const fetchCourses = async () => {
  loading.value = true
  try {
    const params: any = {}
    if (activeCategory.value) {
      params.category = activeCategory.value
    }
    const res = await request.get('/course/list', params)
    if (res.code === 200) {
      courseList.value = res.data || []
    }
  } catch (error) {
    console.error('获取课程列表失败:', error)
    // 使用模拟数据
    courseList.value = generateMockCourses()
  } finally {
    loading.value = false
  }
}

const generateMockCourses = (): Course[] => {
  const categories = ['通识', '技能', '安全']
  const titles = [
    '志愿服务入门培训', '急救知识培训', '沟通技巧提升',
    '团队协作训练', '公共场所安全', '服务礼仪规范',
    '心理援助基础', '环保知识普及', '社区服务技巧'
  ]
  return titles.map((title, index) => ({
    id: index + 1,
    title,
    category: categories[index % 3],
    instructor: '培训讲师',
    duration: 20 + index * 5,
    difficulty: (index % 3) + 1,
    creditHours: 20 + index * 5,
    passed: index < 3
  }))
}

const goToDetail = (id: number) => {
  router.push(`/training/detail/${id}`)
}

onMounted(fetchCourses)
</script>

<style lang="scss" scoped>
.course-list-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;

  .header-row {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
  }

  h2 {
    margin: 0 0 8px;
    font-size: 24px;
  }

  .subtitle {
    margin: 0;
    color: #999;
  }

  .exam-link {
    display: flex;
    align-items: center;
    gap: 6px;
    color: #409eff;
    font-size: 14px;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }
}

.filter-bar {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 16px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 12px;

  .category-filters {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;

    :deep(.el-radio-button__inner) {
      border-radius: 20px !important;
      border: 1px solid #dcdfe6 !important;
      margin-right: 8px;
      margin-bottom: 8px;
    }

    :deep(.el-radio-button:first-child .el-radio-button__inner) {
      border-radius: 20px !important;
    }

    :deep(.el-radio-button:last-child .el-radio-button__inner) {
      border-radius: 20px !important;
    }
  }
}

.stats-bar {
  margin-bottom: 16px;

  .count {
    color: #999;
    font-size: 14px;
  }
}

.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.course-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    transform: translateY(-6px);
    box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
  }

  .course-cover {
    position: relative;
    height: 180px;
    overflow: hidden;

    :deep(.el-image) {
      width: 100%;
      height: 100%;
    }

    .cover-placeholder {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: rgba(255,255,255,0.5);
    }

    &:hover :deep(.el-image) img {
      transform: scale(1.05);
    }

    .duration-badge {
      position: absolute;
      bottom: 10px;
      left: 10px;
      background: rgba(0, 0, 0, 0.7);
      color: #fff;
      padding: 4px 10px;
      border-radius: 4px;
      font-size: 12px;
      display: flex;
      align-items: center;
      gap: 4px;
    }

    .category-tag {
      position: absolute;
      top: 10px;
      right: 10px;
    }

    .passed-badge {
      position: absolute;
      top: 10px;
      left: 10px;
      background: #67c23a;
      color: #fff;
      padding: 4px 10px;
      border-radius: 4px;
      font-size: 12px;
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }

  .course-info {
    padding: 16px;

    .course-title {
      font-size: 16px;
      font-weight: 600;
      margin: 0 0 8px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      color: #333;
    }

    .course-instructor {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 13px;
      color: #666;
      margin: 0 0 12px;
    }

    .course-meta {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .difficulty {
        color: #ddd;
        
        .active {
          color: #f7ba2a;
        }
      }

      .reward {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 13px;
        color: #e6a23c;
        font-weight: 500;
      }
    }
  }
}
</style>
