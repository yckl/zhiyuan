<template>
  <div class="experience-plaza">
    <div class="page-header">
      <div class="header-content">
        <div>
          <h2>📖 心得广场</h2>
          <p class="subtitle">分享志愿服务的感悟与收获</p>
        </div>
        <el-button type="primary" @click="goToPublish" v-if="isLoggedIn">
          <el-icon><Edit /></el-icon>
          发布心得
        </el-button>
      </div>
    </div>

    <!-- 筛选和搜索 -->
    <div class="filter-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索心得内容..."
        prefix-icon="Search"
        clearable
        style="width: 300px"
        @input="handleSearch"
      />
      <el-radio-group v-model="sortBy" @change="fetchExperiences">
        <el-radio-button label="latest">最新发布</el-radio-button>
        <el-radio-button label="popular">最多浏览</el-radio-button>
        <el-radio-button label="likes">最多点赞</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 瀑布流卡片 -->
    <div class="waterfall-container" v-loading="loading">
      <div class="waterfall-grid">
        <div
          v-for="exp in filteredExperiences"
          :key="exp.id"
          class="experience-card"
          @click="goToDetail(exp.id)"
        >
          <!-- 封面图 -->
          <div class="card-cover" v-if="exp.coverImage">
            <el-image :src="exp.coverImage" fit="cover" lazy>
              <template #error>
                <div class="cover-error">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </div>

          <!-- 卡片内容 -->
          <div class="card-content">
            <h3 class="card-title">{{ exp.title }}</h3>
            <p class="card-summary">{{ exp.summary || exp.content?.substring(0, 100) || '暂无摘要' }}</p>

            <!-- 作者信息 -->
            <div class="card-author">
              <el-avatar :size="32" :src="getAvatarUrl(exp.authorAvatar)">
                {{ exp.authorName?.charAt(0) || '匿' }}
              </el-avatar>
              <div class="author-info">
                <span class="author-name">{{ exp.authorName || exp.volunteerName || '匿名用户' }}</span>
                <span class="publish-time">{{ formatDate(exp.createTime) }}</span>
              </div>
            </div>

            <!-- 统计数据 -->
            <div class="card-stats">
              <span class="stat-item">
                <el-icon><View /></el-icon>
                {{ exp.viewCount || 0 }}
              </span>
              <span class="stat-item">
                <el-icon><Star /></el-icon>
                {{ exp.likeCount || exp.likes || 0 }}
              </span>
              <el-tag v-if="exp.activityTitle" size="small" type="info">
                {{ exp.activityTitle }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>

      <el-empty v-if="!loading && filteredExperiences.length === 0" description="暂无心得分享" />
    </div>

    <!-- 加载更多 -->
    <div class="load-more" v-if="hasMore">
      <el-button :loading="loadingMore" @click="loadMore">
        加载更多
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Edit, View, Star, Picture } from '@element-plus/icons-vue'
import { request } from '@/utils/request'

interface Experience {
  id: number
  title: string
  content?: string
  summary?: string
  coverImage?: string
  authorId?: number
  authorName?: string
  authorAvatar?: string
  volunteerName?: string
  activityTitle?: string
  viewCount?: number
  likeCount?: number
  likes?: number
  createTime: string
}

const router = useRouter()
const loading = ref(false)
const loadingMore = ref(false)
const searchKeyword = ref('')
const sortBy = ref('latest')
const experienceList = ref<Experience[]>([])
const currentPage = ref(1)
const pageSize = 12
const hasMore = ref(true)

const isLoggedIn = computed(() => !!localStorage.getItem('token'))

const filteredExperiences = computed(() => {
  if (!searchKeyword.value) return experienceList.value
  const keyword = searchKeyword.value.toLowerCase()
  return experienceList.value.filter(exp =>
    exp.title.toLowerCase().includes(keyword) ||
    exp.content?.toLowerCase().includes(keyword)
  )
})

const getAvatarUrl = (url?: string) => {
  if (!url) return ''
  // 添加时间戳避免缓存
  return url.includes('?') ? url : `${url}?t=${Date.now()}`
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  if (days < 30) return `${Math.floor(days / 7)}周前`
  return date.toLocaleDateString('zh-CN')
}

const handleSearch = () => {
  // 通过 computed 自动过滤
}

const fetchExperiences = async (reset = true) => {
  if (reset) {
    currentPage.value = 1
    experienceList.value = []
    hasMore.value = true
  }

  loading.value = true
  try {
    const res = await request.get('/experience/public/list', {
      params: {
        pageNum: currentPage.value,
        pageSize,
        sortBy: sortBy.value
      }
    })
    if (res.code === 200) {
      const list = res.data?.records || res.data || []
      if (reset) {
        experienceList.value = list
      } else {
        experienceList.value.push(...list)
      }
      hasMore.value = list.length >= pageSize
    }
  } catch (error) {
    console.error('获取心得列表失败:', error)
    // 使用模拟数据
    experienceList.value = generateMockData()
  } finally {
    loading.value = false
  }
}

const loadMore = async () => {
  if (loadingMore.value || !hasMore.value) return
  loadingMore.value = true
  currentPage.value++
  await fetchExperiences(false)
  loadingMore.value = false
}

const generateMockData = (): Experience[] => {
  return [
    {
      id: 1,
      title: '社区义诊活动心得',
      summary: '参与社区义诊让我深刻体会到志愿服务的意义，看到老人们露出的笑容，一切付出都值得...',
      coverImage: 'https://picsum.photos/seed/exp1/400/300',
      authorName: '张小明',
      authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=1',
      activityTitle: '社区健康义诊',
      viewCount: 234,
      likeCount: 56,
      createTime: '2026-01-18'
    },
    {
      id: 2,
      title: '环保清洁日记',
      summary: '今天和小伙伴们一起参加了河岸清洁活动，虽然有点累但看到干净的河堤心里特别满足...',
      authorName: '李华',
      authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=2',
      activityTitle: '绿色河岸行动',
      viewCount: 189,
      likeCount: 42,
      createTime: '2026-01-17'
    },
    {
      id: 3,
      title: '敬老院送温暖',
      summary: '陪爷爷奶奶们聊天、下棋，听他们讲过去的故事，感觉自己收获的比付出的更多...',
      coverImage: 'https://picsum.photos/seed/exp3/400/300',
      authorName: '王芳',
      authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=3',
      activityTitle: '敬老院慰问',
      viewCount: 312,
      likeCount: 89,
      createTime: '2026-01-15'
    },
    {
      id: 4,
      title: '支教帮扶感悟',
      summary: '山区孩子们求知的眼神让我动容，我要继续努力，为他们带去更多知识和希望...',
      authorName: '赵强',
      authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=4',
      viewCount: 456,
      likeCount: 128,
      createTime: '2026-01-12'
    }
  ]
}

const goToDetail = (id: number) => {
  router.push(`/experience/detail/${id}`)
}

const goToPublish = () => {
  router.push('/profile/experiences')
}

onMounted(fetchExperiences)
</script>

<style lang="scss" scoped>
.experience-plaza {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 24px;

  .header-content {
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
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 12px;
}

.waterfall-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.experience-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    transform: translateY(-6px);
    box-shadow: 0 12px 30px rgba(0, 0, 0, 0.12);
  }

  .card-cover {
    height: 180px;
    overflow: hidden;

    :deep(.el-image) {
      width: 100%;
      height: 100%;
    }

    .cover-error {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f5f5f5;
      color: #ccc;
      font-size: 40px;
    }
  }

  .card-content {
    padding: 16px;

    .card-title {
      font-size: 16px;
      font-weight: 600;
      margin: 0 0 8px;
      color: #333;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .card-summary {
      font-size: 13px;
      color: #666;
      margin: 0 0 12px;
      line-height: 1.5;
      display: -webkit-box;
      -webkit-line-clamp: 3;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .card-author {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 12px;

      .author-info {
        display: flex;
        flex-direction: column;

        .author-name {
          font-size: 13px;
          font-weight: 500;
          color: #333;
        }

        .publish-time {
          font-size: 11px;
          color: #999;
        }
      }
    }

    .card-stats {
      display: flex;
      align-items: center;
      gap: 16px;

      .stat-item {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 12px;
        color: #999;
      }
    }
  }
}

.load-more {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}
</style>
