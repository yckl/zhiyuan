<template>
  <div class="activity-list-page" :class="{ 'is-mobile': isMobile }">

    <!-- ==================== 移动端 ==================== -->
    <template v-if="isMobile">
      <!-- 顶部搜索栏 -->
      <div class="mobile-search-bar">
        <el-popover
          v-model:visible="mobileSearchVisible"
          placement="bottom-start"
          :width="'100%'"
          trigger="click"
          popper-class="elite-autocomplete mobile-elite-autocomplete custom-search-popover"
          :teleported="false"
        >
          <template #reference>
            <el-input
              v-model="queryParams.title"
              placeholder="搜索活动"
              clearable
              :prefix-icon="Search"
              class="search-input"
              @keyup.enter="handleSearch"
              @clear="handleSearch"
              @input="handleSearchInput"
              @focus="handleSearchFocus"
            />
          </template>
          
          <div class="custom-suggestion-container" v-if="mobileSearchVisible">
            <div v-if="showClearHistory" class="elite-clear-action" @click.stop="handleClearAll">
              <el-icon><Delete /></el-icon>
              <span>清空历史</span>
            </div>
            
            <transition-group name="list-slide" tag="div" class="suggestion-list-group">
              <div 
                v-for="item in suggestionList" 
                :key="item.id || item.value"
                class="elite-suggestion-item" 
                :class="{ 'is-history': item.isHistory }"
                @click="handleSelectSuggestion(item)"
              >
                <div class="elite-left-media">
                  <el-image v-if="item.coverImage" :src="item.coverImage" fit="cover" class="elite-thumbnail" />
                  <div v-else class="elite-icon-fallback" :style="{ backgroundColor: getCategoryMeta(item.categoryName).color }">
                    <el-icon color="#fff" :size="16"><component :is="getCategoryMeta(item.categoryName).icon" /></el-icon>
                  </div>
                </div>
                <div class="elite-center-content">
                  <div class="elite-row-1" v-html="highlightKeyword(item.value, queryParams.title)"></div>
                  <div class="elite-row-2">
                    <template v-if="item.isHistory"><span class="history-tag">历史记录</span></template>
                    <template v-else>
                      <span class="elite-org">{{ item.organizerName }}</span>
                      <span class="elite-divider">|</span>
                      <span class="elite-time">{{ formatDate(item.startTime) }}</span>
                    </template>
                  </div>
                </div>
                <div class="elite-right-action">
                  <el-icon v-if="item.isHistory" class="elite-delete-btn" @click.stop="handleDeleteItem(item.value)"><Close /></el-icon>
                  <el-icon v-else class="elite-arrow"><Right /></el-icon>
                </div>
              </div>
            </transition-group>

            <div v-if="suggestionList.length === 0 && !showClearHistory" class="elite-empty-state">
              <el-icon class="empty-icon"><Picture /></el-icon>
              <div class="empty-text">没找到相关活动</div>
            </div>
          </div>
        </el-popover>
        <div class="filter-trigger" @click="showFilterSheet = true">
          <el-icon :size="20"><Filter /></el-icon>
          <span v-if="hasActiveFilter" class="filter-dot"></span>
        </div>
      </div>

      <!-- 底部弹出筛选面板 (Bottom Sheet) -->
      <el-drawer
        v-model="showFilterSheet"
        direction="btt"
        size="auto"
        :show-close="false"
        class="filter-drawer"
      >
        <div class="sheet-header">
          <span class="sheet-title">筛选条件</span>
          <el-button type="primary" link @click="handleReset">重置</el-button>
        </div>

        <div class="sheet-body">
          <!-- 状态 -->
          <div class="sheet-group">
            <label class="sheet-label">活动状态</label>
            <div class="pill-options">
              <span
                class="pill"
                :class="{ active: queryParams.status === null }"
                @click="queryParams.status = null"
              >全部</span>
              <span
                class="pill"
                :class="{ active: queryParams.status === 2 }"
                @click="queryParams.status = 2"
              >报名</span>
              <span
                class="pill"
                :class="{ active: queryParams.status === 3 }"
                @click="queryParams.status = 3"
              >进行中</span>
              <span
                class="pill"
                :class="{ active: queryParams.status === 4 }"
                @click="queryParams.status = 4"
              >已结</span>
            </div>
          </div>

          <!-- 排序 -->
          <div class="sheet-group">
            <label class="sheet-label">排序方式</label>
            <div class="pill-options">
              <span
                class="pill"
                :class="{ active: queryParams.orderBy === 'newest' }"
                @click="queryParams.orderBy = 'newest'"
              >最新发</span>
              <span
                class="pill"
                :class="{ active: queryParams.orderBy === 'viewCount' }"
                @click="queryParams.orderBy = 'viewCount'"
              >最多浏</span>
              <span
                class="pill"
                :class="{ active: queryParams.orderBy === 'deadline' }"
                @click="queryParams.orderBy = 'deadline'"
              >即将截止</span>
            </div>
          </div>

          <!-- 分类 -->
          <div class="sheet-group">
            <label class="sheet-label">活动分类</label>
            <div class="pill-options">
              <span
                class="pill"
                :class="{ active: queryParams.categoryId === null }"
                @click="queryParams.categoryId = null"
              >全部</span>
              <span
                v-for="cat in categories"
                :key="cat.id"
                class="pill"
                :class="{ active: queryParams.categoryId === cat.id }"
                @click="queryParams.categoryId = cat.id"
              >{{ cat.name }}</span>
            </div>
          </div>
        </div>

        <div class="sheet-footer">
          <el-button round size="large" class="sheet-btn" @click="showFilterSheet = false">取消</el-button>
          <el-button round size="large" type="primary" class="sheet-btn" @click="applyFilter">确定</el-button>
        </div>
      </el-drawer>

      <!-- 无限滚动列表 -->
      <div class="mobile-feed" ref="feedRef" @scroll="handleScroll">
        <!-- 骨架屏 -->
        <div v-if="loading && activities.length === 0" class="skeleton-mobile">
          <div v-for="i in 3" :key="i" class="skeleton-row">
            <el-skeleton animated>
              <template #template>
                <div style="display:flex;gap:12px;padding:12px">
                  <el-skeleton-item variant="image" style="width:120px;height:90px;border-radius:8px" />
                  <div style="flex:1">
                    <el-skeleton-item variant="h3" style="width:80%" />
                    <el-skeleton-item variant="text" style="width:60%;margin-top:10px" />
                    <el-skeleton-item variant="text" style="width:40%;margin-top:8px" />
                  </div>
                </div>
              </template>
            </el-skeleton>
          </div>
        </div>

        <!-- 卡片信息 -->
        <TransitionGroup v-else name="card-fade" tag="div">
          <ActivityCard v-for="item in activities" :key="item.id" :activity="item" />
        </TransitionGroup>

        <!-- 加载状态 -->
        <div v-if="loadingMore" class="feed-status">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>加载中...</span>
        </div>
        <div v-else-if="noMore && activities.length > 0" class="feed-status end">
          <span>— 没有更多了 —</span>
        </div>

        <el-empty v-if="!loading && activities.length === 0" description="暂无活动" :image-size="120" />
      </div>
    </template>

    <!-- ==================== 电脑端 ==================== -->
    <template v-else>
      <!-- 筛选区域 -->
      <div class="pc-filter-bar">
        <div class="filter-left">
          <!-- 胶囊分类 Pill Tabs -->
          <div class="pill-tabs">
            <span
              class="pill-tab"
              :class="{ active: queryParams.categoryId === null }"
              @click="handleCategoryChange(null)"
            >全部</span>
            <span
              v-for="cat in categories"
              :key="cat.id"
              class="pill-tab"
              :class="{ active: queryParams.categoryId === cat.id }"
              @click="handleCategoryChange(cat.id)"
            >{{ cat.name }}</span>
          </div>
        </div>

        <div class="filter-right elite-filter-group">
          <!-- 升级1：Autocomplete 精确等宽对齐与间距 -->
          <el-popover
            v-model:visible="pcSearchVisible"
            placement="bottom-start"
            :width="420"
            trigger="click"
            popper-class="elite-autocomplete pc-exact-match custom-search-popover"
            :teleported="false"
          >
            <template #reference>
              <el-input
                v-model="queryParams.title"
                placeholder="搜索活动名称..."
                clearable
                style="width: 420px"
                :prefix-icon="Search"
                class="elite-search-input"
                @keyup.enter="handleSearch"
                @clear="handleSearch"
                @input="handleSearchInput"
                @focus="handleSearchFocus"
              />
            </template>
            
            <div class="custom-suggestion-container" v-if="pcSearchVisible">
              <!-- 清空历史 -->
              <div v-if="showClearHistory" class="elite-clear-action" @click.stop="handleClearAll">
                <el-icon><Delete /></el-icon>
                <span>清空搜索历史</span>
              </div>
              
              <!-- 列表 -->
              <transition-group name="list-slide" tag="div" class="suggestion-list-group">
                <div 
                  v-for="item in suggestionList" 
                  :key="item.id || item.value"
                  class="elite-suggestion-item" 
                  :class="{ 'is-history': item.isHistory }"
                  @click="handleSelectSuggestion(item)"
                >
                  <div class="elite-left-media">
                    <el-image
                      v-if="item.coverImage"
                      :src="item.coverImage"
                      fit="cover"
                      class="elite-thumbnail"
                    />
                    <div v-else class="elite-icon-fallback" :style="{ backgroundColor: getCategoryMeta(item.categoryName).color }">
                      <el-icon color="#fff" :size="20"><component :is="getCategoryMeta(item.categoryName).icon" /></el-icon>
                    </div>
                  </div>
                  
                  <div class="elite-center-content">
                    <div class="elite-row-1" v-html="highlightKeyword(item.value, queryParams.title)"></div>
                    <div class="elite-row-2">
                      <template v-if="item.isHistory">
                        <span class="history-tag">历史记录</span>
                      </template>
                      <template v-else>
                        <span class="elite-org">{{ item.organizerName }}</span>
                        <span class="elite-divider">|</span>
                        <span class="elite-time">{{ formatDate(item.startTime) }}</span>
                      </template>
                    </div>
                  </div>

                  <div class="elite-right-action">
                    <el-icon v-if="item.isHistory" class="elite-delete-btn" @click.stop="handleDeleteItem(item.value)"><Close /></el-icon>
                    <div v-else class="elite-enter-hint">
                      <span class="hint-text">Enter 进入</span>
                      <el-icon class="elite-arrow"><Right /></el-icon>
                    </div>
                  </div>
                </div>
              </transition-group>

              <!-- 空状态 -->
              <div v-if="suggestionList.length === 0 && !showClearHistory" class="elite-empty-state">
                <el-icon class="empty-icon"><Picture /></el-icon>
                <div class="empty-text">没找到相关活动，试试搜索 "支教" 吧</div>
              </div>
            </div>
          </el-popover>

          <!-- 升级2：高级下拉组合框 -->
          <el-select
            v-model="queryParams.organizerId"
            placeholder="全部主办方"
            clearable
            filterable
            class="elite-select organizer-select"
            popper-class="elite-select-popper elite-org-popper"
            @change="handleSearch"
            style="width: 260px;"
            :fit-input-width="true"
          >
            <el-option
              v-for="item in organizers"
              :key="item.id"
              :label="item.orgName"
              :value="item.id"
            >
              <div class="elite-option-item">
                <el-icon class="opt-icon"><School /></el-icon>
                <div class="opt-text-box">
                  <div class="opt-title">{{ item.orgName }}</div>
                  <div class="opt-sub">{{ item.orgName.includes('学院') ? '学院级组织' : '志愿服务组织' }}</div>
                </div>
              </div>
            </el-option>
          </el-select>

          <!-- 升级3：状态筛选环 -->
          <el-select
            v-model="queryParams.status"
            placeholder="全部状态"
            clearable
            class="elite-select status-select"
            popper-class="elite-select-popper"
            style="width: 150px;"
            @change="handleSearch"
          >
            <el-option label="报名中" :value="2">
              <div class="elite-option-item">
                <span class="status-dot status-green"></span>
                <span class="opt-title">报名中</span>
                <span class="hot-badge">HOT</span>
              </div>
            </el-option>
            <el-option label="进行中" :value="3">
              <div class="elite-option-item">
                <span class="status-dot status-orange"></span>
                <span class="opt-title">进行中</span>
              </div>
            </el-option>
            <el-option label="已结束" :value="4">
              <div class="elite-option-item">
                <span class="status-dot status-gray"></span>
                <span class="opt-title">已结束</span>
              </div>
            </el-option>
          </el-select>

          <!-- 升级4：动画悬浮按钮 -->
          <div class="elite-actions">
            <el-button class="elite-btn btn-refresh" :icon="Refresh" @click="handleReset" circle></el-button>
            <el-tooltip content="猜你喜欢" placement="top" effect="light">
              <el-button 
                class="elite-btn btn-guess" 
                :icon="recommendationLoading ? Loading : StarFilled" 
                :loading="recommendationLoading"
                @click="handleGuess" 
                circle
              ></el-button>
            </el-tooltip>
          </div>
        </div>
      </div>

      <!-- 活动网格 -->
      <div class="pc-activity-grid">
        <!-- 骨架屏 -->
        <el-row v-if="loading && activities.length === 0" :gutter="24">
          <el-col v-for="i in 6" :key="i" :sm="12" :lg="8" :xl="6">
            <div class="skeleton-pc-card">
              <el-skeleton animated>
                <template #template>
                  <el-skeleton-item variant="image" style="height:180px" />
                  <div style="padding:16px">
                    <el-skeleton-item variant="h3" style="width:70%" />
                    <el-skeleton-item variant="text" style="width:50%;margin-top:12px" />
                    <el-skeleton-item variant="text" style="width:90%;margin-top:8px" />
                    <el-skeleton-item variant="text" style="width:60%;margin-top:8px" />
                  </div>
                </template>
              </el-skeleton>
            </div>
          </el-col>
        </el-row>

        <!-- 卡片网格 -->
        <TransitionGroup v-else name="card-fade" tag="div" class="pc-grid-inner">
          <div v-for="(item, i) in activities" :key="item.id" class="pc-grid-item" :style="{ animationDelay: `${i * 0.04}s` }">
            <div v-if="recommendationMode" class="recommendation-badge-wrapper">
              <span class="recommendation-tag">{{ item.tag || 'AI 匹配' }}</span>
            </div>
            <ActivityCard :activity="item" />
            <div v-if="recommendationMode && item.recommendationReason" class="recommendation-reason">
               <el-icon><Guide /></el-icon>
               <span>{{ item.recommendationReason }}</span>
            </div>
          </div>
        </TransitionGroup>

        <el-empty v-if="!loading && activities.length === 0" description="暂无活动" :image-size="150" />
      </div>

      <!-- 分页 -->
      <div class="pc-pagination" v-if="total > 0">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[12, 24, 48]"
          layout="total, sizes, prev, pager, next"
          background
          @size-change="fetchActivities"
          @current-change="fetchActivities"
        />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { 
  Search, Filter, Refresh, Loading, Close, Delete, Right,
  User, Star, Picture, Guide, Connection, Medal, Sunny, School, StarFilled
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { request } from '@/utils/request'
import ActivityCard from '@/components/ActivityCard.vue'
import { useSearchHistory } from '@/composables/useSearchHistory'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()

// ================== 响应式判断 ==================
const windowWidth = ref(window.innerWidth)
const isMobile = computed(() => windowWidth.value < 768)
const handleResize = () => { windowWidth.value = window.innerWidth }
onMounted(() => window.addEventListener('resize', handleResize))
onUnmounted(() => window.removeEventListener('resize', handleResize))

// ================== 数据状态 ==================
const { searchHistory, addHistory, removeHistory, clearHistory, getSuggestionList } = useSearchHistory()
const loading = ref(false)
const loadingMore = ref(false)
const activities = ref<any[]>([])
const categories = ref<any[]>([])
const organizers = ref<any[]>([])
const total = ref(0)
const showFilterSheet = ref(false)
const feedRef = ref<HTMLElement | null>(null)
const recommendationMode = ref(false)
const recommendationLoading = ref(false)

let searchTimer: ReturnType<typeof setTimeout> | null = null

const pcSearchVisible = ref(false)
const mobileSearchVisible = ref(false)
const suggestionList = ref<any[]>([])

const showClearHistory = computed(() => !queryParams.title && suggestionList.value.some(item => item.isHistory))

const queryParams = reactive({
  page: 1,
  size: 12,
  title: '',
  categoryId: null as number | null,
  organizerId: null as number | null,
  status: null as number | null,
  orderBy: 'newest' as string
})

const noMore = computed(() => activities.value.length >= total.value && total.value > 0)
const hasActiveFilter = computed(() =>
  queryParams.status !== null || queryParams.categoryId !== null || queryParams.orderBy !== 'newest'
)

// ================== API 请求 ==================
const fetchCategories = async () => {
  try {
    const res = await request.get('/category/list')
    categories.value = res.data || []
  } catch (e) { console.error('获取分类失败:', e) }
}

const fetchOrganizers = async () => {
  try {
    const res = await request.get('/activity/organizers')
    organizers.value = res.data || []
  } catch (e) { console.error('获取主办方失败', e) }
}

const fetchActivities = async () => {
  loading.value = true
  try {
    const params: any = { page: queryParams.page, size: queryParams.size }
    if (queryParams.title) params.title = queryParams.title
    if (queryParams.categoryId) params.categoryId = queryParams.categoryId
    if (queryParams.organizerId) params.organizerId = queryParams.organizerId
    if (queryParams.status) params.status = queryParams.status
    if (queryParams.orderBy === 'viewCount') params.orderBy = 'viewCount'

    const res = await request.get('/activity/list', params)
    activities.value = res.data?.records || []
    total.value = res.data?.total || 0
    recommendationMode.value = false // 正常搜索/筛选时关闭推荐模式
  } catch (e) { console.error('获取活动列表失败:', e) }
  finally { loading.value = false }
}

const handleRecommend = async () => {
  recommendationLoading.value = true
  recommendationMode.value = true
  try {
    const res = await request.get('/recommendation/home')
    if (res.data) {
      activities.value = res.data.map((item: any) => ({
        ...item,
        isRecommendation: true,
        tag: item.algoTag,
        recommendationReason: item.reason
      }))
      total.value = activities.value.length
      ElMessage({
        message: '已为您精准匹配最适合的活动',
        type: 'success',
        icon: StarFilled,
        duration: 2000
      })
    }
  } catch (e) {
    console.error('获取推荐失败:', e)
    ElMessage.error('推荐引擎暂时开小差了，请稍后再试')
  } finally {
    recommendationLoading.value = false
  }
}

// 移动端追加加载
const fetchMoreActivities = async () => {
  if (loadingMore.value || noMore.value) return
  loadingMore.value = true
  queryParams.page++
  try {
    const params: any = { page: queryParams.page, size: queryParams.size }
    if (queryParams.title) params.title = queryParams.title
    if (queryParams.categoryId) params.categoryId = queryParams.categoryId
    if (queryParams.status) params.status = queryParams.status
    if (queryParams.orderBy === 'viewCount') params.orderBy = 'viewCount'

    const res = await request.get('/activity/list', params)
    const newRecords = res.data?.records || []
    activities.value.push(...newRecords)
    total.value = res.data?.total || 0
  } catch (e) {
    queryParams.page-- // 失败回退
    console.error('加载更多失败:', e)
  } finally {
    loadingMore.value = false
  }
}

// ================== 事件处理 ==================
const handleSearch = () => {
  if (queryParams.title) {
    addHistory(queryParams.title)
  }
  queryParams.page = 1
  activities.value = [] 
  pcSearchVisible.value = false
  mobileSearchVisible.value = false
  fetchActivities()
}

// 实时搜索（不加历史记录）
const handleRealtimeSearch = () => {
  if (searchTimer) clearTimeout(searchTimer)
  queryParams.page = 1
  activities.value = [] 
  fetchActivities()
}

const fetchSuggestionsList = async () => {
  const queryString = queryParams.title
  if (!queryString) {
    suggestionList.value = getSuggestionList('').map(item => ({ 
      ...item, 
      isHistory: true,
      id: `hist-${item.value}` 
    }))
    return
  }

  try {
    const res = await request.get('/activity/suggestions', { keyword: queryString })
    const records = (res.data || []).map((item: any) => ({
      value: item.title,
      categoryName: item.categoryName,
      organizerName: item.organizerName || '未知组织',
      startTime: item.startTime,
      coverImage: item.coverImage,
      isHistory: false,
      id: `act-${item.title}`
    }))
    
    // 合并历史记录中匹配的部分
    const history = getSuggestionList(queryString).map(item => ({ 
      ...item, 
      isHistory: true,
      id: `hist-${item.value}` 
    }))
    
    suggestionList.value = [...history, ...records]
  } catch (e) {
    console.error('获取建议失败:', e)
  }
}

const handleSearchFocus = () => {
  fetchSuggestionsList()
  if (isMobile.value) mobileSearchVisible.value = true
  else pcSearchVisible.value = true
}

const handleSearchInput = () => {
  fetchSuggestionsList()
}

const handleSelectSuggestion = (item: any) => {
  queryParams.title = item.value
  pcSearchVisible.value = false
  mobileSearchVisible.value = false
  handleSearch()
}

// 监听关键词变化实现防抖搜索
watch(() => queryParams.title, (newVal, oldVal) => {
  if (newVal === oldVal) return
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    handleRealtimeSearch()
  }, 600)
})

// 监听搜索历史变更，保持视图实时响应更新
watch(() => searchHistory.value, () => {
  if (pcSearchVisible.value || mobileSearchVisible.value) {
    fetchSuggestionsList()
  }
}, { deep: true })

const handleClearAll = () => {
  ElMessageBox.confirm('确定要清空所有搜索历史吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    clearHistory()
    ElMessage.success('已清空历史记录')
    fetchSuggestionsList()
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
const handleReset = () => {
  queryParams.title = ''
  queryParams.categoryId = null
  queryParams.organizerId = null
  queryParams.status = null
  queryParams.orderBy = 'newest'
  queryParams.page = 1
  fetchActivities()
}

const handleGuess = () => {
  ElMessage.success({ message: 'AI 引擎正在分析您的志愿偏好...', icon: Connection })
  // 清空筛选条件，切换到推荐模式
  queryParams.categoryId = null
  queryParams.organizerId = null
  queryParams.status = null
  queryParams.orderBy = 'newest'
  queryParams.page = 1
  handleRecommend()
}

const handleCategoryChange = (id: number | null) => {
  queryParams.categoryId = id
  handleSearch()
}

// 动态关键字高亮逻辑
const highlightKeyword = (text: string, keyword: string) => {
  if (!text || !keyword) return text
  
  // 去除输入中的空格并将原字符打散以匹配 fuzzy search 的结果片段
  const cleanKeyword = keyword.trim().replace(/\s+/g, '')
  if (!cleanKeyword) return text

  const chars = cleanKeyword.split('')
  // 创建正则表达式: 对输入的拆散字符全局忽略大小写替换
  const regexStr = chars.map(c => c.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')).join('|')
  const regex = new RegExp(`(${regexStr})`, 'gi')
  
  // 将找到的匹配项包裹上发光的绿色 span
  return text.replace(regex, '<span style="color: #00b4b6; font-weight: bold;">$1</span>')
}



const formatDate = (date: any) => {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

// 获取分类图标和颜色
const getCategoryMeta = (categoryName: string) => {
  const name = categoryName || ''
  if (name.includes('支教')) return { icon: Guide, color: '#FF9800', bg: 'rgba(255, 152, 0, 0.1)' }
  if (name.includes('科普')) return { icon: Connection, color: '#9C27B0', bg: 'rgba(156, 39, 176, 0.1)' }
  if (name.includes('环保')) return { icon: Sunny, color: '#4CAF50', bg: 'rgba(76, 175, 80, 0.1)' }
  if (name.includes('赛会') || name.includes('比赛')) return { icon: Medal, color: '#2196F3', bg: 'rgba(33, 150, 243, 0.1)' }
  if (name.includes('助老') || name.includes('敬老')) return { icon: User, color: '#E91E63', bg: 'rgba(233, 30, 99, 0.1)' }
  return { icon: Star, color: '#00b4b6', bg: 'rgba(0, 180, 182, 0.1)' }
}

// 底部弹窗确认筛选
const applyFilter = () => {
  showFilterSheet.value = false
  queryParams.page = 1
  activities.value = [] // 清空旧数据
  fetchActivities()
}

// ================== 无限滚动 ==================
const handleScroll = () => {
  const el = feedRef.value
  if (!el) return
  // 距离底部 200px 时触发加载
  if (el.scrollHeight - el.scrollTop - el.clientHeight < 200) {
    fetchMoreActivities()
  }
}

// ================== 初始化 ==================
onMounted(() => {
  fetchCategories()
  fetchOrganizers()
  if (route.query.filter === 'recommended') {
    handleRecommend()
  } else {
    fetchActivities()
  }
})
</script>

<style lang="scss" scoped>
// ================================================================
// 移动端
// ================================================================

// --- 搜索栏 ---
.mobile-search-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: #fff;

  .search-input {
    flex: 1;
  }

  .filter-trigger {
    position: relative;
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 10px;
    background: var(--app-bg);
    cursor: pointer;
    flex-shrink: 0;
    color: #666;

    .filter-dot {
      position: absolute;
      top: 6px;
      right: 6px;
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: var(--primary-color);
    }
  }
}

// --- 搜索建议样式 ---
.history-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 0;
  width: 100%;

  .history-text {
    flex: 1;
    font-size: 14px;
    color: #333;
  }

  .delete-icon {
    font-size: 14px;
    color: #bbb;
    padding: 4px;
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
  transition: all 0.2s;

  &:hover {
    color: var(--primary-color);
    background: rgba(0, 191, 166, 0.05);
  }
}

// --- Bottom Sheet 筛选面板 ---
:deep(.filter-drawer) {
  border-radius: 16px 16px 0 0 !important;

  .el-drawer__header {
    display: none !important;
  }

  .el-drawer__body {
    padding: 0 !important;
  }
}

.sheet-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px 8px;

  .sheet-title {
    font-size: 17px;
    font-weight: 600;
    color: #333;
  }
}

.sheet-body {
  padding: 8px 20px 16px;
}

.sheet-group {
  margin-bottom: 20px;

  .sheet-label {
    display: block;
    font-size: 13px;
    color: #999;
    margin-bottom: 10px;
    font-weight: 500;
  }
}

.pill-options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.pill {
  display: inline-flex;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  background: #f5f5f5;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
  -webkit-tap-highlight-color: transparent;

  &.active {
    background: rgba(0, 191, 166, 0.12);
    color: var(--primary-color);
    font-weight: 600;
  }
}

.sheet-footer {
  display: flex;
  gap: 12px;
  padding: 12px 20px;
  padding-bottom: calc(12px + var(--safe-area-bottom));
  border-top: 0.5px solid rgba(0, 0, 0, 0.06);

  .sheet-btn {
    flex: 1;
  }
}

// --- 无限滚动信息流 ---
.mobile-feed {
  flex: 1;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  padding-bottom: 80px;
  background: #fff;
}

.feed-status {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 20px;
  color: #999;
  font-size: 13px;

  &.end {
    color: #ccc;
  }
}

// ================================================================
// 电脑端
// ================================================================

// --- 筛选栏 ---
.pc-filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap; // 允许在缩放过大时换行
  gap: 16px;
  margin-bottom: 24px;
  padding: 16px 20px;
  background: #fff;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  min-height: 72px; // 保持稳定高度
}

.filter-left {
  flex: 1;
  min-width: 300px; // 确保标签区域不会缩到没法看
}

.pill-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.pill-tab {
  display: inline-flex;
  padding: 6px 18px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  background: #f5f5f5;
  color: #666;
  cursor: pointer;
  transition: all 0.25s;
  white-space: nowrap;

  &:hover {
    background: #ebebeb;
    color: #333;
  }

  &.active {
    background: var(--primary-color);
    color: #fff;
    font-weight: 600;
    box-shadow: 0 2px 8px rgba(0, 191, 166, 0.3);
  }
}

.filter-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

// --- 活动网格 ---
.pc-activity-grid {
  min-height: 400px;
}

.pc-grid-inner {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;

  @media (max-width: 1200px) { grid-template-columns: repeat(2, 1fr); }
}

.pc-grid-item {
  animation: cardFadeIn 0.4s ease-out both;
}

@keyframes cardFadeIn {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}

// 骨架屏卡片
.skeleton-pc-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 24px;
}

.skeleton-mobile {
  .skeleton-row {
    border-bottom: 0.5px solid rgba(0, 0, 0, 0.04);
  }
}

// --- 分页 ---
.pc-pagination {
  display: flex;
  justify-content: center;
  padding: 24px 0;
}

// --- TransitionGroup 动画 ---
.card-fade-enter-active { transition: all 0.35s ease-out; }
.card-fade-leave-active { transition: all 0.2s ease-in; }
.card-fade-enter-from { opacity: 0; transform: translateY(16px); }
.card-fade-leave-to { opacity: 0; transform: scale(0.96); }
.card-fade-move { transition: transform 0.3s ease; }

// ================================================================
// 移动端整体容器 (App Shell / main)
// ================================================================
.activity-list-page.is-mobile {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #fff;

  .mobile-feed {
    flex: 1;
    overflow-y: auto;
  }
}
</style>

<style>
/* ================================================================ */
/* 搜索下拉框终极美化 (Elite Autocomplete) */
/* ================================================================ */
.elite-autocomplete {
  border-radius: 12px !important;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1) !important;
  border: 1px solid rgba(0, 180, 182, 0.15) !important;
  overflow: hidden;
  animation: eliteDropFade 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes eliteDropFade {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

.elite-autocomplete .el-autocomplete-suggestion__wrap {
  padding: 8px !important;
}

.elite-autocomplete .el-autocomplete-suggestion__list li {
  padding: 0 !important;
  margin-bottom: 4px;
  border-radius: 10px;
  transition: all 0.2s;
  line-height: normal;
}

.elite-autocomplete .el-autocomplete-suggestion__list li:hover {
  background: transparent !important;
}

/* 列表项总体容器 */
.elite-suggestion-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px; /* 统一收窄一点 */
  border-radius: 10px;
  transition: all 0.2s ease;
  background: #fff;
}

.elite-suggestion-item:hover,
.elite-autocomplete .el-autocomplete-suggestion__list li.highlighted .elite-suggestion-item {
  transform: translateY(-2px);
  background: #f8fbfa;
  box-shadow: 0 4px 12px rgba(0, 180, 182, 0.08);
}

/* 左侧图标/图片兜底 */
.elite-left-media {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  flex-shrink: 0;
  overflow: hidden;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
  transition: transform 0.2s;
}

.elite-left-media .elite-thumbnail {
  width: 100%;
  height: 100%;
}

.elite-left-media .elite-icon-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 分类小色块 */
.elite-suggestion-item:hover .elite-left-media {
  transform: scale(1.05); /* 左侧图标轻微放大 */
}

/* 中部文本 */
.elite-center-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
}

.elite-row-1 {
  font-size: 14px;
  font-weight: 700;
  color: #1e293b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.elite-row-2 {
  font-size: 12px;
  color: #94a3b8;
  display: flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.elite-row-2 .elite-time {
  color: #FF9800; /* 时间用橙色增加 urgency */
  font-weight: 500;
}

.history-tag {
  background: #f1f5f9;
  color: #64748b;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 11px;
}

/* 右侧行为按钮 */
.elite-right-action {
  flex-shrink: 0;
  display: flex;
  align-items: center;
}

.elite-enter-hint {
  display: flex;
  align-items: center;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s;
}

.elite-enter-hint .hint-text {
  font-size: 11px;
  color: #00b4b6;
  font-weight: 600;
}

.elite-arrow {
  color: #cbd5e1;
  font-size: 16px;
  transition: all 0.2s;
}

.elite-suggestion-item:hover .elite-enter-hint,
.elite-autocomplete .el-autocomplete-suggestion__list li.highlighted .elite-enter-hint {
  opacity: 1;
}

.elite-suggestion-item:hover .elite-arrow,
.elite-autocomplete .el-autocomplete-suggestion__list li.highlighted .elite-arrow {
  color: #00b4b6;
  font-weight: bold;
  transform: translateX(2px);
}

.elite-delete-btn {
  color: #cbd5e1;
  font-size: 16px;
  padding: 4px;
}
.elite-delete-btn:hover {
  color: #f56c6c;
}

/* 空状态 */
.elite-empty-state {
  padding: 40px 0;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}
.elite-empty-state .empty-icon {
  font-size: 48px;
  color: #e2e8f0;
}
.elite-empty-state .empty-text {
  color: #94a3b8;
  font-size: 13px;
}

/* 清除历史 */
.elite-clear-action {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 12px;
  color: #94a3b8;
  font-size: 13px;
  cursor: pointer;
  transition: color 0.2s;
}
.elite-clear-action:hover {
  color: #f56c6c;
}

/* PC 端宽度覆盖 */
.elite-autocomplete.pc-exact-match {
  width: 420px !important;
}

/* ================================================================ */
/* 筛选期组合框美化顶级版 (Elite Selects & Actions) */
/* ================================================================ */
.elite-filter-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 搜索输入框与选择框高度统一 */
.elite-search-input :deep(.el-input__wrapper),
.elite-select :deep(.el-input__wrapper) {
  height: 44px;
  box-sizing: border-box;
}

.elite-search-input :deep(.el-input__wrapper) {
  border-radius: 12px !important;
  box-shadow: 0 0 0 1px rgba(0, 180, 182, 0.2) inset !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.elite-search-input :deep(.el-input__wrapper.is-focus),
.elite-search-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #00b4b6 inset, 0 4px 12px rgba(0, 180, 182, 0.1) !important;
  background: #f8fbfa;
}

:deep(.elite-select) .el-input__wrapper {
  border-radius: 12px !important;
  box-shadow: 0 0 0 1px rgba(0, 180, 182, 0.2) inset !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: #fff;
}

:deep(.elite-select) .el-input__wrapper.is-focus,
:deep(.elite-select) .el-input__wrapper:hover {
  box-shadow: 0 0 0 1px #00b4b6 inset, 0 4px 12px rgba(0, 180, 182, 0.1) !important;
  background: #f8fbfa;
}

:deep(.elite-select) .el-input__inner {
  color: #00b4b6 !important;
  font-weight: 600;
  font-size: 14px;
}

.elite-search-input :deep(.el-input__inner) {
  font-weight: 600;
  font-size: 14px;
  color: #1e293b;
}

/* 旋转小箭头 */
:deep(.elite-select) .el-select__caret {
  color: #00b4b6;
  font-weight: bold;
}

/* 选择框下拉 Popper 动画与阴影 */
.elite-select-popper {
  border-radius: 12px !important;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12) !important;
  border: 1px solid rgba(0, 180, 182, 0.1) !important;
  animation: eliteDropFade 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 8px !important;
}

.elite-select-popper .el-select-dropdown__item {
  border-radius: 8px;
  margin-bottom: 4px;
  height: auto !important;
  min-height: 56px !important; /* 让选择框的高度也接近搜索建议的高度 */
  padding: 8px 16px !important;
  display: flex;
  align-items: center;
  transition: all 0.2s;
}

.elite-select-popper .el-select-dropdown__item.hover,
.elite-select-popper .el-select-dropdown__item:hover {
  background-color: #f8fbfa !important;
  transform: translateX(4px);
}

.elite-select-popper .el-select-dropdown__item.selected {
  background-color: #E6F7EB !important;
  color: #00C853 !important;
  font-weight: 600;
}

/* 主办方选项的内容排版 */
.elite-org-popper {
  min-width: 260px !important;
}

.elite-option-item {
  display: flex;
  align-items: center;
  gap: 10px;
  line-height: 1.4;
}

.elite-option-item .opt-icon {
  font-size: 20px;
  color: #00b4b6;
  padding: 6px;
  background: rgba(0, 180, 182, 0.1);
  border-radius: 8px;
}

.elite-option-item .opt-text-box {
  display: flex;
  flex-direction: column;
}

.elite-option-item .opt-title {
  font-size: 14px;
  font-weight: 700;
  color: #333;
}
.el-select-dropdown__item.selected .elite-option-item .opt-title {
  color: #00C853;
}

.elite-option-item .opt-sub {
  font-size: 11px;
  color: #94a3b8;
}

/* 状态选项小圆点 */
.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  box-shadow: 0 0 0 2px rgba(255,255,255,0.5);
}
.status-green { background: #00C853; }
.status-orange { background: #FF9800; }
.status-gray { background: #94a3b8; }

.hot-badge {
  background: #ff4d4f;
  color: white;
  font-size: 10px;
  padding: 0 4px;
  border-radius: 4px;
  margin-left: auto;
  font-weight: bold;
}

/* 操作按钮组 */
.elite-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.elite-btn {
  border: none !important;
  background: #f1f5f9 !important;
  color: #64748b !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 16px;
}

.elite-btn.btn-refresh:hover {
  background: rgba(245, 108, 108, 0.1) !important;
  color: #f56c6c !important;
  transform: rotate(180deg);
}
.elite-btn.btn-refresh:active {
  animation: eliteShake 0.4s;
}

.elite-btn.btn-guess {
  background: rgba(255, 105, 180, 0.1) !important;
  color: #FF69B4 !important;
  animation: gentleBreathe 2s infinite;
}
.elite-btn.btn-guess:hover {
  transform: scale(1.1) translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 105, 180, 0.3);
  background: #FF69B4 !important;
  color: #fff !important;
  animation: none;
}

@keyframes eliteShake {
  0% { transform: translateX(0); }
  25% { transform: translateX(-4px) rotate(180deg); }
  50% { transform: translateX(4px) rotate(180deg); }
  75% { transform: translateX(-4px) rotate(180deg); }
  100% { transform: translateX(0) rotate(180deg); }
}

/* 动态提示搜索框 */
.custom-search-popover {
  padding: 8px !important;
  border-radius: 12px !important;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1) !important;
  border: 1px solid rgba(0, 180, 182, 0.15) !important;
  animation: popFadeIn 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes popFadeIn {
  0% { opacity: 0; transform: translateY(-5px); }
  100% { opacity: 1; transform: translateY(0); }
}

.custom-suggestion-container {
  overflow: hidden;
}

/* ================================================================ */
/* 过渡动画 - 历史记录滑出滑入 (list-slide) */
/* ================================================================ */
.suggestion-list-group {
  position: relative;
  display: flex;
  flex-direction: column;
}

.list-slide-move, /* 使其他元素位置移动生效 */
.list-slide-enter-active,
.list-slide-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.list-slide-enter-from,
.list-slide-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

.list-slide-leave-active {
  position: absolute;
  width: 100%;
  z-index: 0;
}

/* 推荐模式样式 */
.recommendation-badge-wrapper {
  position: absolute;
  top: 12px;
  left: 12px;
  z-index: 10;
}
.recommendation-tag {
  background: linear-gradient(135deg, #FF69B4, #ff4d4f);
  color: #fff;
  font-size: 11px;
  font-weight: bold;
  padding: 4px 10px;
  border-radius: 20px 20px 20px 4px;
  box-shadow: 0 4px 12px rgba(255, 105, 180, 0.4);
}

.recommendation-reason {
  margin-top: 10px;
  padding: 8px 12px;
  background: rgba(0, 180, 182, 0.05);
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #00b4b6;
  font-size: 12px;
  font-weight: 500;
  border: 1px dashed rgba(0, 180, 182, 0.2);
}
.recommendation-reason .el-icon {
  font-size: 14px;
}
.pc-grid-item {
  position: relative; /* 为推荐标签定位 */
}


</style>
