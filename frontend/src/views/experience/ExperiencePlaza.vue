<template>
  <div class="experience-plaza">

    <!-- ==================== 绿色渐变 Hero ==================== -->
    <div class="plaza-hero">
      <div class="hero-inner">
        <h1 class="hero-title">📖 心得广场</h1>
        <p class="hero-sub">分享志愿服务的感悟与收获</p>
      </div>
      <button v-if="isLoggedIn" class="publish-btn" @click="goToPublish">
        <el-icon><Edit /></el-icon> 发布心得
      </button>
    </div>

    <!-- ==================== 搜索栏 ==================== -->
    <el-autocomplete
      v-model="searchKeyword"
      :fetch-suggestions="getSuggestionList"
      placeholder="搜索心得内容..."
      clearable
      :prefix-icon="Search"
      class="search-bar"
      @select="handleSelectHistory"
      @keyup.enter="handleSearch"
      @clear="handleSearch"
    >
      <template #default="{ item }">
        <div class="history-item">
          <el-icon><Clock /></el-icon>
          <span class="history-text">{{ item.value }}</span>
          <el-icon class="delete-icon" @click.stop="removeHistory(item.value)"><Close /></el-icon>
        </div>
      </template>
      <template #append v-if="searchHistory.length > 0 && !searchKeyword">
        <div class="clear-history" @click.stop="clearHistory">清空历史记录</div>
      </template>
    </el-autocomplete>

    <!-- ==================== 分类 Tab ==================== -->
    <div class="filter-tabs">
      <span
        v-for="tab in sortTabs"
        :key="tab.value"
        class="filter-tab"
        :class="{ active: queryParams.type === tab.value }"
        @click="handleTabClick(tab.value)"
      >{{ tab.label }}</span>
    </div>

    <!-- ==================== 商品网格 ==================== -->
    <div class="waterfall-container" v-loading="loading">
      <!-- 骨架屏 -->
      <div v-if="loading && experienceList.length === 0" class="waterfall-grid">
        <div v-for="i in 8" :key="i" class="skeleton-card">
          <el-skeleton animated>
            <template #template>
              <el-skeleton-item variant="image" style="height: 160px" />
              <div style="padding: 14px">
                <el-skeleton-item variant="h3" style="width: 80%" />
                <el-skeleton-item variant="text" style="width: 100%; margin-top: 8px" />
                <el-skeleton-item variant="text" style="width: 60%; margin-top: 6px" />
              </div>
            </template>
          </el-skeleton>
        </div>
      </div>

      <!-- 卡片列表 -->
      <TransitionGroup v-else name="card-fade" tag="div" class="waterfall-grid">
        <div
          v-for="(exp, idx) in experienceList"
          :key="exp.id"
          class="experience-card"
          :style="{ animationDelay: `${idx * 0.04}s` }"
          @click="goToDetail(exp.id)"
        >
          <!-- 封面 -->
          <div class="card-cover" v-if="exp.coverImage">
            <el-image :src="exp.coverImage" fit="cover" lazy>
              <template #error>
                <div class="cover-error">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="cover-gradient"></div>
          </div>

          <!-- 卡片内容 -->
          <div class="card-content">
            <h3 class="card-title">{{ exp.title }}</h3>
            <p class="card-summary">{{ exp.summary || exp.content?.substring(0, 100) || '暂无摘要' }}</p>

            <!-- 作者信息 -->
            <div class="card-author">
              <el-avatar :size="28" :src="getAvatarUrl(exp.authorAvatar)">
                {{ exp.authorName?.charAt(0) || '匿' }}
              </el-avatar>
              <div class="author-info">
                <span class="author-name">{{ exp.authorName || exp.volunteerName || '匿名用户' }}</span>
                <span class="publish-time">{{ formatDate(exp.createTime) }}</span>
              </div>
            </div>

            <!-- 统计 + 按钮 -->
            <div class="card-bottom">
              <div class="card-stats">
                <span class="stat-item">
                  <el-icon><View /></el-icon>
                  {{ exp.viewCount || 0 }}
                </span>
                <span class="stat-item">
                  <el-icon><Star /></el-icon>
                  {{ exp.likeCount || exp.likes || 0 }}
                </span>
              </div>
              <button class="detail-btn" @click.stop="goToDetail(exp.id)">查看详情</button>
            </div>
          </div>
        </div>
      </TransitionGroup>

      <el-empty v-if="!loading && experienceList.length === 0" description="暂无心得分享" />
    </div>

    <!-- 加载更多 -->
    <div class="load-more" v-if="hasMore && !loading">
      <el-button :loading="loadingMore" round @click="loadMore">
        加载更多
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Edit, View, Star, Picture, Search, Clock, Close } from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import { useSearchHistory } from '@/composables/useSearchHistory'

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
const { searchHistory, addHistory, removeHistory, clearHistory, getSuggestionList } = useSearchHistory()
const searchKeyword = ref('')
const experienceList = ref<Experience[]>([])
const hasMore = ref(true)

const queryParams = ref({
  pageNum: 1,
  pageSize: 12,
  type: 'recommend'
})

const isLoggedIn = computed(() => !!localStorage.getItem('token'))

const sortTabs = [
  { label: '🔥 热门', value: 'hot' },
  { label: '👍 推荐', value: 'recommend' },
  { label: '📝 我的', value: 'mine' }
]

// 移除本地 computed 过滤
let searchTimer: any = null

const getAvatarUrl = (url?: string) => {
  if (!url) return ''
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

const handleTabClick = (type: string) => {
  queryParams.value.type = type
  fetchExperiences(true)
}

const handleSearch = () => {
  if (searchKeyword.value) {
    addHistory(searchKeyword.value)
  }
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    fetchExperiences(true)
  }, 300)
}

const handleSelectHistory = (item: any) => {
  searchKeyword.value = item.value
  handleSearch()
}

const fetchExperiences = async (reset = true) => {
  if (reset) {
    queryParams.value.pageNum = 1
    experienceList.value = []
    hasMore.value = true
  }

  loading.value = true
  try {
    const params: any = { ...queryParams.value }
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    const res = await request.get('/experience/public/list', {
      params
    })
    if (res.code === 200) {
      let list = res.data?.records || res.data || []
      // 字段映射适配：后端是 volunteerName，前端是 authorName；后端是 images，前端需要封面图
      list = list.map((item: any) => {
        let parsedCover = ''
        if (item.images) {
          try {
            const imgArray = JSON.parse(item.images)
            if (imgArray && imgArray.length > 0) parsedCover = imgArray[0]
          } catch (e) {
            parsedCover = item.images // fallback如果已经是单URL
          }
        }
        
        // 提取摘要
        let pureText = ''
        if (item.content) {
          pureText = item.content.replace(/<[^>]+>/g, '')
          pureText = pureText.length > 50 ? pureText.substring(0, 50) + '...' : pureText
        }

        return {
          ...item,
          authorName: item.volunteerName || '匿名志愿者',
          authorAvatar: item.volunteerAvatar,
          summary: item.summary || pureText,
          coverImage: parsedCover || `https://picsum.photos/seed/exp${item.id}/400/300`
        }
      })

      if (reset) {
        experienceList.value = list
      } else {
        experienceList.value.push(...list)
      }
      hasMore.value = list.length >= queryParams.value.pageSize
    }
  } catch (error) {
    console.error('获取心得列表失败:', error)
    experienceList.value = generateMockData()
  } finally {
    loading.value = false
  }
}

const loadMore = async () => {
  if (loadingMore.value || !hasMore.value) return
  loadingMore.value = true
  queryParams.value.pageNum++
  await fetchExperiences(false)
  loadingMore.value = false
}

const generateMockData = (): Experience[] => {
  return [
    { id: 1, title: '社区义诊活动心得', summary: '参与社区义诊让我深刻体会到志愿服务的意义，看到老人们露出的笑容，一切付出都值得...', coverImage: 'https://picsum.photos/seed/exp1/400/300', authorName: '张小明', authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=1', activityTitle: '社区健康义诊', viewCount: 234, likeCount: 56, createTime: '2026-01-18' },
    { id: 2, title: '环保清洁日记', summary: '今天和小伙伴们一起参加了河岸清洁活动，虽然有点累但看到干净的河堤心里特别满足...', authorName: '李华', authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=2', activityTitle: '绿色河岸行动', viewCount: 189, likeCount: 42, createTime: '2026-01-17' },
    { id: 3, title: '敬老院送温暖', summary: '陪爷爷奶奶们聊天、下棋，听他们讲过去的故事，感觉自己收获的比付出的更多...', coverImage: 'https://picsum.photos/seed/exp3/400/300', authorName: '王芳', authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=3', activityTitle: '敬老院慰问', viewCount: 312, likeCount: 89, createTime: '2026-01-15' },
    { id: 4, title: '支教帮扶感悟', summary: '山区孩子们求知的眼神让我动容，我要继续努力，为他们带去更多知识和希望...', authorName: '赵强', authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=4', viewCount: 456, likeCount: 128, createTime: '2026-01-12' }
  ]
}

const goToDetail = (id: number) => { router.push(`/experience/detail/${id}`) }
const goToPublish = () => { router.push('/profile/experiences') }

onMounted(fetchExperiences)
</script>

<style lang="scss" scoped>
@keyframes cardIn {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

.experience-plaza {
  min-height: 100vh;
  background: #f5f7fa;
}

// ================================================================
// Hero
// ================================================================
.plaza-hero {
  background: linear-gradient(135deg, #00BFA6 0%, #009688 50%, #00695C 100%);
  padding: 28px 20px 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.hero-inner {
  .hero-title {
    margin: 0 0 6px;
    font-size: 24px;
    font-weight: 800;
    color: #fff;
    text-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  }

  .hero-sub {
    margin: 0;
    font-size: 13px;
    color: rgba(255, 255, 255, 0.7);
  }
}

.publish-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  border: none;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(8px);
  color: #fff;
  padding: 10px 20px;
  border-radius: 24px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s;
  white-space: nowrap;

  &:hover { background: rgba(255, 255, 255, 0.4); transform: scale(1.04); }
  &:active { transform: scale(0.96); }

  .el-icon { font-size: 16px; }
}

// ================================================================
// 搜索栏
// ================================================================
.search-bar {
  margin: 12px 16px 0;

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
  gap: 0;
  padding: 0 16px;
  margin: 12px 0;
  overflow-x: auto;

  &::-webkit-scrollbar { display: none; }
}

.filter-tab {
  padding: 8px 16px;
  font-size: 14px;
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
    width: 22px;
    height: 3px;
    border-radius: 2px;
    background: linear-gradient(90deg, #00BFA6, #43e97b);
    transition: transform 0.25s;
  }

  &.active {
    color: #00BFA6;
    font-weight: 600;
    &::after { transform: translateX(-50%) scaleX(1); }
  }
}

// ================================================================
// 瀑布流
// ================================================================
.waterfall-container {
  padding: 0 16px;
  min-height: 300px;
}

.waterfall-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.skeleton-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}

.experience-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: all 0.3s;
  animation: cardIn 0.35s ease-out both;

  &:active { transform: scale(0.97); }

  .card-cover {
    height: 140px;
    overflow: hidden;
    position: relative;

    :deep(.el-image) { width: 100%; height: 100%; }

    .cover-gradient {
      position: absolute;
      inset: 0;
      background: linear-gradient(to bottom, transparent 50%, rgba(0, 191, 166, 0.3) 100%);
      pointer-events: none;
    }

    .cover-error {
      width: 100%; height: 100%;
      display: flex; align-items: center; justify-content: center;
      background: #f5f5f5; color: #ccc; font-size: 32px;
    }
  }

  .card-content {
    padding: 12px;

    .card-title {
      font-size: 14px;
      font-weight: 600;
      margin: 0 0 6px;
      color: #333;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .card-summary {
      font-size: 12px;
      color: #888;
      margin: 0 0 10px;
      line-height: 1.5;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .card-author {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 8px;

      .author-info {
        display: flex;
        flex-direction: column;
        min-width: 0;

        .author-name { font-size: 12px; font-weight: 500; color: #333; }
        .publish-time { font-size: 10px; color: #bbb; }
      }
    }

    .card-stats {
      display: flex;
      align-items: center;
      gap: 12px;
      flex-wrap: wrap;

      .stat-item {
        display: flex;
        align-items: center;
        gap: 3px;
        font-size: 11px;
        color: #bbb;
      }
    }
  }
}

.load-more {
  display: flex;
  justify-content: center;
  margin: 24px 0;
}

// ================================================================
// TransitionGroup
// ================================================================
.card-fade-enter-active { transition: all 0.3s ease-out; }
.card-fade-leave-active { transition: all 0.2s ease-in; }
.card-fade-enter-from { opacity: 0; transform: translateY(12px); }
.card-fade-leave-to { opacity: 0; transform: scale(0.96); }
.card-fade-move { transition: transform 0.3s ease; }

// ================================================================
// PC 适配
// ================================================================
@media (min-width: 769px) {
  .plaza-hero { padding: 36px 24px 40px; }

  .waterfall-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 24px;
  }

  .waterfall-grid {
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
  }

  .experience-card {
    &:hover {
      transform: translateY(-6px);
      box-shadow: 0 12px 28px rgba(0, 0, 0, 0.1);
    }
  }

  .search-bar { max-width: 1200px; margin: 16px auto 0; padding: 0 24px; }
  .filter-tabs { max-width: 1200px; margin: 12px auto; padding: 0 24px; }
}

// ================================================================
// 暗黑模式
// ================================================================
html.dark .experience-plaza {
  background-color: #0f172a !important;

  .experience-card {
    background-color: #1e293b !important;
    .card-title { color: #f1f5f9 !important; }
    .card-summary { color: #94a3b8 !important; }
    .author-name { color: #e2e8f0 !important; }
    .publish-time, .stat-item { color: #64748b !important; }
    .cover-error { background-color: #334155 !important; }
  }
}
</style>
