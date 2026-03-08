<template>
  <div class="experience-list">

    <!-- ==================== 绿色渐变 Hero ==================== -->
    <div class="plaza-hero">
      <div class="hero-inner">
        <h1 class="hero-title">📖 心得广场</h1>
        <p class="hero-sub">分享志愿服务心得，传递温暖与感动</p>
      </div>
      <button v-if="isLoggedIn" class="publish-btn" @click="router.push('/experience/create')">
        <el-icon><Edit /></el-icon> 发布心得
      </button>
    </div>

    <el-autocomplete
      v-model="searchKeyword"
      :fetch-suggestions="getExperienceSuggestions"
      placeholder="搜索心得..."
      clearable
      :prefix-icon="Search"
      class="search-bar"
      @keyup.enter="handleSearch"
      @clear="handleSearch"
      @select="handleSelectSuggestion"
      :trigger-on-focus="true"
    >
      <template #default="{ item }">
        <template v-if="item.isClear">
          <div class="clear-history-action" @click.stop="handleClearAll">
            <el-icon><Delete /></el-icon>
            <span>清空搜索历史</span>
          </div>
        </template>
        <div v-else class="suggestion-item">
          <el-icon v-if="item.isHistory"><Clock /></el-icon>
          <el-icon v-else><Search /></el-icon>
          <span class="suggestion-text">{{ item.value }}</span>
          <el-icon v-if="item.isHistory" class="delete-icon" @click.stop="handleDeleteItem(item.value)"><Close /></el-icon>
        </div>
      </template>
    </el-autocomplete>

    <!-- ==================== 分类 Tab ==================== -->
    <div class="filter-tabs">
      <span
        v-for="tab in sortTabs"
        :key="tab.value"
        class="filter-tab"
        :class="{ active: activeSort === tab.value }"
        @click="activeSort = tab.value; fetchExperiences()"
      >{{ tab.label }}</span>
    </div>

    <!-- ==================== 心得网格 ==================== -->
    <div class="waterfall-container" v-loading="loading">
      <!-- 骨架屏 -->
      <div v-if="loading && experiences.length === 0" class="waterfall-grid">
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
          v-for="(item, idx) in experiences"
          :key="item.id"
          class="experience-card"
          :style="{ animationDelay: `${idx * 0.04}s` }"
          @click="router.push(`/experience/${item.id}`)"
        >
          <!-- 封面 -->
          <div class="card-cover">
            <el-image
              :src="item.coverImage || getDefaultCover(item.id)"
              fit="cover"
              class="cover-image"
              lazy
            >
              <template #error>
                <div class="image-placeholder">
                  <el-icon :size="36"><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="cover-gradient"></div>
          </div>

          <!-- 卡片内容 -->
          <div class="card-body">
            <h3 class="card-title">{{ item.title }}</h3>
            <p class="card-summary">{{ getPlainText(item.content) }}</p>

            <!-- 作者信息 -->
            <div class="author-info">
              <el-avatar :size="28" :src="item.volunteerAvatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <span class="author-name">{{ item.volunteerName || '匿名用户' }}</span>
            </div>

            <!-- 底部统计 + 按钮 -->
            <div class="card-footer">
              <div class="stats">
                <span class="stat-item">
                  <el-icon><Star /></el-icon>
                  {{ item.likeCount || 0 }}
                </span>
                <span class="stat-item">
                  <el-icon><ChatDotRound /></el-icon>
                  {{ item.commentCount || 0 }}
                </span>
              </div>
              <button class="detail-btn" @click.stop="router.push(`/experience/${item.id}`)">查看详情</button>
            </div>
          </div>
        </div>
      </TransitionGroup>

      <el-empty
        v-if="!loading && experiences.length === 0"
        description="暂无心得分享，快来发布第一篇吧~"
        :image-size="150"
      />
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.size"
        :total="total"
        :page-sizes="[8, 12, 24, 48]"
        layout="total, prev, pager, next"
        background
        @size-change="fetchExperiences"
        @current-change="fetchExperiences"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Edit, Star, ChatDotRound, User, Picture, Search, Clock, Close, Delete } from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import { useSearchHistory } from '@/composables/useSearchHistory'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import { ElMessage, ElMessageBox } from 'element-plus'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()

const loading = ref(false)
const experiences = ref<any[]>([])
const total = ref(0)
const searchKeyword = ref('')
const activeSort = ref('latest')

const queryParams = reactive({
  page: 1,
  size: 12
})

const isLoggedIn = computed(() => !!localStorage.getItem('token'))

const sortTabs = [
  { label: '全部', value: 'latest' },
  { label: '🔥 热门', value: 'hot' },
  { label: '⭐ 推荐', value: 'recommend' },
  { label: '📝 我的', value: 'mine' }
]

const { addHistory, removeHistory, clearHistory, getSuggestionList } = useSearchHistory()
let searchTimer: any = null

const handleSearch = () => {
  if (searchKeyword.value) {
    addHistory(searchKeyword.value)
  }
  queryParams.page = 1
  experiences.value = [] // 强制清空旧数据触发骨架屏
  fetchExperiences()
}

// 实时搜索
const handleRealtimeSearch = () => {
  if (searchTimer) clearTimeout(searchTimer)
  queryParams.page = 1
  experiences.value = []
  fetchExperiences()
}

// 防抖监听
watch(() => searchKeyword.value, (newVal, oldVal) => {
  if (newVal === oldVal) return
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    handleRealtimeSearch()
  }, 600)
})

const handleClearAll = () => {
  ElMessageBox.confirm('确定要清空所有搜索历史吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    clearHistory()
    ElMessage.success('已清空历史记录')
    nextTick(() => { document.body.click() })
  }).catch(() => {})
}

const handleDeleteItem = (val: string) => {
  if (removeHistory(val)) {
    ElMessage({
      message: '已删除该条记录',
      type: 'success',
      duration: 1000
    })
  }
}

const getExperienceSuggestions = async (queryString: string, cb: any) => {
  if (!queryString) {
    const history = getSuggestionList('').map(i => ({ ...i, isHistory: true }))
    if (history.length > 0) {
      history.push({ value: '', isHistory: false, isClear: true } as any)
    }
    cb(history)
    return
  }
  try {
    const res = await request.get('/experience/suggestions', { keyword: queryString })
    const suggestions = (res.data || []).map((s: string) => ({ value: s, isHistory: false }))
    
    const history = getSuggestionList(queryString).map(i => ({ ...i, isHistory: true }))
    const combined = [...history]
    suggestions.forEach((s: any) => {
      if (!combined.find(c => c.value === s.value)) combined.push(s)
    })
    cb(combined.slice(0, 10))
  } catch (e) {
    cb(getSuggestionList(queryString))
  }
}

const handleSelectSuggestion = (item: any) => {
  if (item.isClear) return
  searchKeyword.value = item.value
  handleSearch()
}

// 默认封面图（基于ID生成不同颜色的）
const getDefaultCover = (id: number) => {
  const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#9b59b6', '#1abc9c']
  const index = id % colors.length
  return `data:image/svg+xml,${encodeURIComponent(`
    <svg xmlns="http://www.w3.org/2000/svg" width="400" height="200">
      <defs>
        <linearGradient id="grad" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" style="stop-color:${colors[index]};stop-opacity:1" />
          <stop offset="100%" style="stop-color:${colors[(index + 1) % colors.length]};stop-opacity:0.8" />
        </linearGradient>
      </defs>
      <rect width="400" height="200" fill="url(#grad)"/>
      <text x="200" y="110" font-family="Arial" font-size="48" fill="white" text-anchor="middle" opacity="0.5">封面</text>
    </svg>
  `)}`
}

// 提取纯文本
const getPlainText = (html: string) => {
  if (!html) return ''
  const text = html.replace(/<[^>]+>/g, '').replace(/&nbsp;/g, ' ')
  return text.length > 80 ? text.slice(0, 80) + '...' : text
}

const fetchExperiences = async () => {
  loading.value = true
  try {
    const params: any = {
      pageNum: queryParams.page,
      pageSize: queryParams.size,
      type: activeSort.value
    }
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    const res = await request.get('/experience/public/list', params)
    
    let list = res.data?.records || res.data || []
    
    // 字段映射：解析图片作为封面
    list = list.map((item: any) => {
      let parsedCover = ''
      if (item.images) {
        try {
          const imgArray = JSON.parse(item.images)
          if (imgArray && imgArray.length > 0) parsedCover = imgArray[0]
        } catch (e) {
          parsedCover = item.images
        }
      }
      return {
        ...item,
        coverImage: parsedCover,
        volunteerName: item.volunteerName || '匿名志愿者'
      }
    })
    
    experiences.value = list
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('获取心得列表失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchExperiences()
})
</script>

<style lang="scss" scoped>
@keyframes cardSlideIn {
  from { opacity: 0; transform: translateX(30px); }
  to { opacity: 1; transform: translateX(0); }
}

.experience-list {
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
  width: calc(100% - 32px);

  :deep(.el-input__wrapper) {
    border-radius: 10px;
    background: #fff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04) !important;
  }
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 0;
  width: 100%;

  .el-icon {
    color: #999;
    font-size: 14px;
  }

  .suggestion-text {
    flex: 1;
    font-size: 14px;
    color: #333;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .delete-icon {
    padding: 4px;
    color: #bbb;
    border-radius: 4px;
    transition: all 0.2s;
    &:hover {
      background: #f0f0f0;
      color: #f56c6c;
    }
  }
}

.clear-history-action {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 0;
  color: #999;
  font-size: 13px;
  border-top: 1px solid #f5f5f5;
  margin-top: 4px;
  cursor: pointer;
  
  &:hover {
    color: var(--app-bg); // Assuming app-bg is darker or themed
    color: #00BFA6;
    background: rgba(0, 191, 166, 0.05);
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
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  animation: cardSlideIn 0.4s ease-out both;

  &:hover {
    transform: translateY(-6px);
    box-shadow: 0 12px 28px rgba(0, 0, 0, 0.1);
    .card-cover .cover-image { transform: scale(1.05); }
    .detail-btn { box-shadow: 0 2px 12px rgba(0, 191, 166, 0.4); }
  }

  &:active { transform: scale(0.97); }

  .card-cover {
    height: 160px;
    overflow: hidden;
    position: relative;

    .cover-image {
      width: 100%;
      height: 100%;
      transition: transform 0.4s ease;
    }

    .cover-gradient {
      position: absolute;
      inset: 0;
      background: linear-gradient(to bottom, transparent 50%, rgba(82, 196, 26, 0.4) 100%);
      pointer-events: none;
    }

    .image-placeholder {
      width: 100%; height: 100%;
      display: flex; align-items: center; justify-content: center;
      background: linear-gradient(135deg, #e0e5ec, #c9d1dc);
      color: #a8b2bd;
    }
  }

  .card-body {
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
      line-height: 1.5;
      margin: 0 0 10px;
      height: 36px;
      overflow: hidden;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }

    .author-info {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 10px;

      .author-name {
        font-size: 12px;
        color: #666;
        font-weight: 500;
      }
    }

    .card-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-top: 10px;
      border-top: 1px solid #f5f5f5;

      .stats {
        display: flex;
        gap: 12px;

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
}

.detail-btn {
  border: none;
  background: linear-gradient(135deg, #00BFA6, #43e97b);
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
    box-shadow: 0 4px 12px rgba(0, 191, 166, 0.35);
  }

  &:active { transform: scale(0.95); }
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 24px 0;
}

// ================================================================
// TransitionGroup 从右侧滑入
// ================================================================
.card-fade-enter-active { transition: opacity 0.35s ease-out, transform 0.35s ease-out; }
.card-fade-leave-active { transition: opacity 0.2s ease-in, transform 0.2s ease-in; }
.card-fade-enter-from { opacity: 0; transform: translateX(24px); }
.card-fade-leave-to { opacity: 0; transform: translateX(-8px) scale(0.96); }
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

  // PC hover already handled in base .experience-card

  .search-bar { max-width: 1200px; margin: 16px auto 0; padding: 0 24px; }
  .filter-tabs { max-width: 1200px; margin: 12px auto; padding: 0 24px; }
  .pagination-wrapper { max-width: 1200px; margin: 0 auto; }
}
</style>
