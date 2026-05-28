<template>
  <div class="my-favorite-page">

    <!-- ==================== 绿渐变 Hero ==================== -->
    <div class="fav-hero">
      <div class="hero-inner">
        <h1 class="hero-title">❤️ 我的收藏</h1>
        <p class="hero-sub">{{ totalCount }} 个收藏项</p>
      </div>
    </div>

    <!-- 搜索栏 -->
    <el-input
      v-model="searchKeyword"
      placeholder="搜索收藏..."
      clearable
      :prefix-icon="Search"
      class="search-bar"
      @keyup.enter="() => {}"
    />

    <!-- 分类 Tab 绿渐变下划线 -->
    <div class="filter-tabs">
      <span
        v-for="tab in tabList"
        :key="tab.value"
        class="filter-tab"
        :class="{ active: activeTab === tab.value }"
        @click="activeTab = tab.value"
      >{{ tab.label }}</span>
    </div>

    <!-- ==================== 收藏列表 ==================== -->
    <div class="fav-container" v-loading="loading">
      <!-- 骨架屏 8 个 -->
      <div v-if="loading && currentList.length === 0" class="fav-grid">
        <div v-for="i in 8" :key="i" class="skeleton-card">
          <el-skeleton animated>
            <template #template>
              <el-skeleton-item variant="image" style="height: 140px" />
              <div style="padding: 12px">
                <el-skeleton-item variant="h3" style="width: 60%" />
                <el-skeleton-item variant="text" style="width: 80%; margin-top: 8px" />
                <el-skeleton-item variant="button" style="width: 100%; margin-top: 12px; height: 28px" />
              </div>
            </template>
          </el-skeleton>
        </div>
      </div>

      <!-- 卡片网格 -->
      <TransitionGroup v-else name="card-fade" tag="div" class="fav-grid">
        <div
          v-for="(item, idx) in filteredList"
          :key="item.id"
          class="fav-card"
          :style="{ animationDelay: `${idx * 0.04}s` }"
        >
          <!-- 封面 -->
          <div class="card-cover" v-if="item.coverImage && activeTab !== 'notice'">
            <el-image :src="item.coverImage" fit="cover" lazy class="cover-img">
  <template #error>
    <img :src="'/default-cover.jpg'" style="width:100%;height:100%;object-fit:cover"/>
  </template>
</el-image>
            <div class="cover-gradient"></div>
            <el-tag class="type-badge" size="small" effect="dark" round>
              {{ getTypeLabel(item.targetType || activeTab) }}
            </el-tag>
          </div>
          <div class="card-cover card-cover-empty" v-else>
            <div class="cover-placeholder">📄</div>
            <div class="cover-gradient"></div>
            <el-tag class="type-badge" size="small" effect="dark" round>
              {{ getTypeLabel(item.targetType || activeTab) }}
            </el-tag>
          </div>

          <!-- 信息 -->
          <div class="card-body">
            <h4 class="card-title">{{ item.title }}</h4>
            <p class="card-desc">{{ stripHtml(item.summary) || '暂无描述' }}</p>
            <div class="card-meta">
              <span class="card-date"><el-icon><Calendar /></el-icon> {{ formatDate(item.createTime) }}</span>
            </div>
          </div>

          <!-- 底部操作 -->
          <div class="card-actions">
            <button class="detail-btn" @click="goToDetail(activeTab, item.targetId)">查看详情</button>
            <button
              class="heart-btn"
              :class="{ hearted: true }"
              @click.stop="handleUnfavorite(item)"
            >
              <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
              </svg>
            </button>
          </div>
        </div>
      </TransitionGroup>

      <el-empty v-if="!loading && filteredList.length === 0" :description="`暂无${currentTabLabel}收藏`" :image-size="120" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Calendar, Search } from '@element-plus/icons-vue'
import { request } from '@/utils/request'

interface FavoriteItem {
  id: number; targetId: number; targetType: string
  title?: string; coverImage?: string; summary?: string; createTime: string
}

const router = useRouter()
const activeTab = ref('activity')
const loading = ref(false)
const searchKeyword = ref('')
const activityList = ref<FavoriteItem[]>([])
const courseList = ref<FavoriteItem[]>([])
const noticeList = ref<FavoriteItem[]>([])

const tabList = [
  { label: '全部', value: 'all' },
  { label: '活动', value: 'activity' },
  { label: '课程', value: 'course' },
  { label: '公告', value: 'notice' }
]

const currentTabLabel = computed(() => tabList.find(t => t.value === activeTab.value)?.label || '')

const currentList = computed(() => {
  if (activeTab.value === 'all') return [...activityList.value, ...courseList.value, ...noticeList.value]
  if (activeTab.value === 'activity') return activityList.value
  if (activeTab.value === 'course') return courseList.value
  return noticeList.value
})

const filteredList = computed(() => {
  if (!searchKeyword.value) return currentList.value
  const kw = searchKeyword.value.toLowerCase()
  return currentList.value.filter(i => i.title?.toLowerCase().includes(kw))
})

const totalCount = computed(() => activityList.value.length + courseList.value.length + noticeList.value.length)

const formatDate = (d: string) => d ? new Date(d).toLocaleDateString('zh-CN') : '-'
const stripHtml = (html?: string) => html?.replace(/<[^>]+>/g, '') || ''

const getTypeLabel = (type: string) => {
  const map: Record<string, string> = { activity: '活动', ACTIVITY: '活动', course: '课程', COURSE: '课程', notice: '公告', NOTICE: '公告' }
  return map[type] || '收藏'
}

const fetchFavorites = async (type: string) => {
  try {
    const res = await request.get('/collection/list', { targetType: type.toUpperCase(), pageNum: 1, pageSize: 50 })
    return res.code === 200 && res.data?.records ? res.data.records : []
  } catch { return [] }
}

const loadAll = async () => {
  loading.value = true
  const [a, c, n] = await Promise.all([
    fetchFavorites('ACTIVITY'),
    fetchFavorites('COURSE'),
    fetchFavorites('NOTICE')
  ])
  activityList.value = a
  courseList.value = c
  noticeList.value = n
  loading.value = false
}

const goToDetail = (type: string, id: number) => {
  const tab = type === 'all' ? 'activity' : type
  const map: Record<string, string> = { activity: `/activity/${id}`, course: `/training/detail/${id}`, notice: `/notice/detail/${id}` }
  router.push(map[tab] || `/activity/${id}`)
}

const handleUnfavorite = async (item: FavoriteItem) => {
  try {
    await ElMessageBox.confirm('确定取消收藏吗？', '提示', { type: 'warning' })
    const targetType = item.targetType || activeTab.value.toUpperCase()
    const res = await request.post('/collection/toggle', { targetId: item.targetId, targetType })
    if (res.code === 200) { ElMessage.success('已取消收藏'); loadAll() }
  } catch (e: any) { if (e !== 'cancel') ElMessage.error('操作失败') }
}

onMounted(loadAll)
</script>

<style lang="scss" scoped>
@keyframes cardIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
@keyframes pulse { 0%,100% { box-shadow: 0 2px 8px rgba(0, 201, 167, 0.2); } 50% { box-shadow: 0 2px 16px rgba(0, 201, 167, 0.5); } }

.my-favorite-page { min-height: 100vh; background: #f5f7fa; }

// ================================================================
// Hero
// ================================================================
.fav-hero {
  background: linear-gradient(135deg, #00C9A7 0%, #05D5B3 100%);
  padding: 28px 20px 32px;
  text-align: center;
  .hero-title { margin: 0 0 6px; font-size: 22px; font-weight: 800; color: #fff; text-shadow: 0 2px 8px rgba(0,0,0,0.1); }
  .hero-sub { margin: 0; font-size: 13px; color: rgba(255,255,255,0.75); }
}

// ================================================================
// 搜索 + Tab
// ================================================================
.search-bar { margin: 12px 16px 0;
  :deep(.el-input__wrapper) { border-radius: 10px; background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.04) !important; }
}

.filter-tabs { display: flex; padding: 0 16px; margin: 10px 0; overflow-x: auto; &::-webkit-scrollbar { display: none; } }

.filter-tab {
  padding: 7px 18px; font-size: 14px; font-weight: 500; color: #666; cursor: pointer; white-space: nowrap; position: relative; transition: color 0.25s;
  &::after { content: ''; position: absolute; bottom: 0; left: 50%; transform: translateX(-50%) scaleX(0); width: 22px; height: 3px; border-radius: 2px; background: linear-gradient(90deg, #00C9A7, #05D5B3); transition: transform 0.25s; }
  &.active { color: #00C9A7; font-weight: 600; &::after { transform: translateX(-50%) scaleX(1); } }
}

// ================================================================
// 收藏网格
// ================================================================
.fav-container { padding: 0 16px; min-height: 200px; }

.fav-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; }
.skeleton-card { background: #fff; border-radius: 14px; overflow: hidden; }

.fav-card {
  background: #fff; border-radius: 14px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.05); transition: all 0.3s; animation: cardIn 0.3s ease-out both;
  display: flex; flex-direction: column;

  &:hover { transform: translateY(-4px); box-shadow: 0 10px 28px rgba(0,0,0,0.12);
    .detail-btn { animation: pulse 1.5s infinite; }
    .cover-img { transform: scale(1.05); }
  }

  .card-cover { position: relative; height: 140px; overflow: hidden;
    .cover-img { width: 100%; height: 100%; transition: transform 0.4s; }
    .cover-gradient { position: absolute; inset: 0; background: linear-gradient(to bottom, transparent 40%, rgba(0, 201, 167, 0.4) 100%); pointer-events: none; }
    .type-badge { position: absolute; top: 10px; left: 10px; }
  }

  .card-cover-empty { background: linear-gradient(135deg, #e8f5e9, #c8e6c9); display: flex; align-items: center; justify-content: center;
    .cover-placeholder { font-size: 40px; z-index: 1; }
  }

  .card-body { padding: 12px 14px; flex: 1;
    .card-title { margin: 0 0 6px; font-size: 14px; font-weight: 600; color: #333; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .card-desc { margin: 0 0 8px; font-size: 12px; color: #999; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; line-height: 1.5; min-height: 36px; }
    .card-meta { .card-date { font-size: 11px; color: #bbb; display: flex; align-items: center; gap: 3px; } }
  }

  .card-actions { display: flex; gap: 8px; padding: 0 14px 12px; align-items: center; }
}

.detail-btn {
  flex: 1; border: none; background: linear-gradient(135deg, #00C9A7, #05D5B3); color: #fff; padding: 7px 0; border-radius: 10px; font-size: 12px; font-weight: 600; cursor: pointer; transition: all 0.25s;
  &:hover { transform: scale(1.03); box-shadow: 0 4px 12px rgba(0, 201, 167, 0.35); }
  &:active { transform: scale(0.97); }
}

.heart-btn {
  width: 34px; height: 34px; border: none; border-radius: 50%; background: #fff0f0; color: #ff4757; display: flex; align-items: center; justify-content: center; cursor: pointer; transition: all 0.3s;
  &:hover { background: #ff4757; color: #fff; transform: scale(1.15); }
  &:active { transform: scale(0.9); }
  &.hearted { color: #ff4757; }
}

// ================================================================
// TransitionGroup
// ================================================================
.card-fade-enter-active { transition: all 0.3s ease-out; }
.card-fade-leave-active { transition: all 0.2s ease-in; }
.card-fade-enter-from { opacity: 0; transform: translateY(12px); }
.card-fade-leave-to { opacity: 0; transform: scale(0.96); }

// ================================================================
// 响应式
// ================================================================
@media (max-width: 768px) {
  .fav-grid { grid-template-columns: 1fr; }
  .fav-container { padding: 0 12px; }
}

@media (min-width: 769px) and (max-width: 1200px) {
  .fav-grid { grid-template-columns: repeat(3, 1fr); }
}

@media (min-width: 769px) {
  .fav-container { max-width: 1200px; margin: 0 auto; padding: 0 24px; }
}
</style>
