<template>
  <div class="msg-center" :class="{ 'is-mobile': isMobile }">

    <!-- ==================== 极光流体顶部 ==================== -->
    <div class="msg-topbar-aurora">
      <div class="topbar-content">
        <h1 class="msg-page-title">消息中心</h1>
        <div class="topbar-right">
          <div v-if="unreadCount > 0" class="unread-pill">
            <span class="unread-num">{{ unreadCount }}</span>
            <span class="unread-label">未读</span>
          </div>
          <el-button
            type="primary" class="read-all-btn"
            :loading="readAllLoading"
            :disabled="unreadCount === 0"
            @click="handleReadAll"
          >全部已读</el-button>
        </div>
      </div>
    </div>

    <!-- ==================== Tab 筛选 ==================== -->
    <div class="msg-tabs hide-scrollbar">
      <span
        v-for="tab in tabs"
        :key="tab.key"
        class="tab-pill"
        :class="{ active: activeTab === tab.key }"
        @click="switchTab(tab.key)"
      >
        {{ tab.label }}
        <el-badge
          v-if="tab.key === 'ALL' && unreadCount > 0"
          :value="unreadCount"
          :max="99"
          class="tab-badge"
        />
      </span>
    </div>

    <!-- ==================== 消息列表 ==================== -->
    <div class="msg-list" v-loading="loading">
      <TransitionGroup name="msg-fade">
        <div
          v-for="msg in messageList"
          :key="msg.id"
          class="msg-row"
          :class="{ unread: msg.isRead === 0 }"
          @click="handleRead(msg)"
        >
          <!-- 左侧头像 -->
          <div class="msg-avatar-area">
            <div class="msg-avatar" :class="getTypeClass(msg.type)">
              <el-icon :size="20"><component :is="getTypeIcon(msg.type)" /></el-icon>
            </div>
            <el-badge v-if="msg.isRead === 0" :value="1" class="avatar-badge" />
          </div>

          <!-- 中间内容 -->
          <div class="msg-content">
            <div class="msg-title-line">
              <span class="msg-title">{{ msg.title }}</span>
            </div>
            <p class="msg-summary">{{ formatContent(msg.content) }}</p>

            <!-- 快捷操作 -->
            <div class="msg-quick-action" v-if="hasAction(msg)" @click.stop>
              <el-button
                v-if="msg.content.includes('兑换成功')"
                type="primary" link size="small"
                @click="router.push('/mall/backpack')"
              >查看背包 </el-button>
              <el-button
                v-if="msg.content.includes('催签') || msg.title.includes('签到')"
                type="success" link size="small"
                @click="router.push('/my-activities')"
              >前往签到 </el-button>
              <el-button
                v-if="msg.type === 'INTERACTION'"
                type="primary" link size="small"
                @click="router.push('/my-experiences')"
              >查看心得 </el-button>
            </div>
          </div>

          <!-- 右侧时间 -->
          <span class="msg-time">{{ formatTime(msg.createTime) }}</span>
        </div>
      </TransitionGroup>

      <!-- 空状态 -->
      <el-empty v-if="!loading && messageList.length === 0" :description="getEmptyText()" :image-size="80" />

      <!-- 加载更多 -->
      <div v-if="hasMore" class="load-more-area">
        <el-button link @click="loadMore" :loading="loading">加载更多</el-button>
      </div>
      <div v-else-if="messageList.length > 0" class="list-end">~没有更多消息啦~</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Bell, Flag, Present, ChatDotRound, Service } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { request } from '@/utils/request'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()
const userStore = useUserStore()

// ================== 响应式 ==================
const windowWidth = ref(window.innerWidth)
const isMobile = computed(() => windowWidth.value < 768)
onMounted(() => window.addEventListener('resize', () => { windowWidth.value = window.innerWidth }))

// ================== 状态 ==================
const loading = ref(false)
const readAllLoading = ref(false)
const activeTab = ref('ALL')
const messageList = ref<any[]>([])
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)

const unreadCount = computed({
  get: () => userStore.unreadCount,
  set: (val) => { userStore.unreadCount = val }
})

const hasMore = computed(() => messageList.value.length < total.value)

const tabs = [
  { key: 'ALL', label: '全部' },
  { key: 'ACTIVITY', label: '活动' },
  { key: 'INTERACTION', label: '互动' },
  { key: 'MALL_SYSTEM', label: '系统' },
]

// ================== 图标/样式映射 ==================
const getTypeClass = (type: string) => {
  if (type === 'MALL') return 'type-mall'
  if (type === 'ACTIVITY') return 'type-activity'
  if (type === 'INTERACTION') return 'type-interaction'
  return 'type-system'
}

const getTypeIcon = (type: string) => {
  if (type === 'MALL') return Present
  if (type === 'ACTIVITY') return Flag
  if (type === 'INTERACTION') return ChatDotRound
  return Bell
}

// ================== 工具函数 ==================
const formatTime = (time: string) => {
  const d = dayjs(time)
  const now = dayjs()
  if (now.diff(d, 'day') === 0) return d.format('HH:mm')
  if (now.diff(d, 'day') === 1) return '昨天'
  if (now.diff(d, 'day') < 7) return d.format('ddd')
  return d.format('MM/DD')
}

const formatContent = (content: string) => {
  if (!content) return ''
  const tmp = document.createElement('DIV')
  tmp.innerHTML = content
  let text = (tmp.textContent || tmp.innerText || '').trim().replace(/\s+/g, ' ')
  return text.length > 60 ? text.substring(0, 60) + '...' : text
}

const hasAction = (msg: any) => {
  return msg.content?.includes('兑换成功') || msg.content?.includes('催签') ||
         msg.title?.includes('签到') || msg.type === 'INTERACTION'
}

const getEmptyText = () => {
  const map: Record<string, string> = {
    ALL: '暂无消息', ACTIVITY: '暂无活动动态',
    INTERACTION: '暂无互动消息', MALL_SYSTEM: '暂无系统通知'
  }
  return map[activeTab.value] || '暂无消息'
}

// ================== API ==================
const fetchMessages = async (append = false) => {
  loading.value = true
  try {
    const params: any = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (activeTab.value === 'ACTIVITY') params.type = 'ACTIVITY'
    else if (activeTab.value === 'INTERACTION') params.type = 'INTERACTION'
    else if (activeTab.value === 'MALL_SYSTEM') params.type = 'SYSTEM,MALL,NOTICE,URGENT'

    const res = await request.get('/message/list', params)
    const records = res.data?.records || []
    messageList.value = append ? [...messageList.value, ...records] : records
    total.value = res.data?.total || 0
    userStore.fetchUnreadCount()
  } catch (e) { console.error('获取消息失败:', e) }
  finally { loading.value = false }
}

const switchTab = (key: string) => {
  activeTab.value = key
  pageNum.value = 1
  fetchMessages()
}

const loadMore = () => { pageNum.value++; fetchMessages(true) }

const handleRead = async (msg: any) => {
  if (msg.isRead === 1) return
  msg.isRead = 1
  unreadCount.value = Math.max(0, unreadCount.value - 1)
  try { await request.post(`/message/read/${msg.id}`) }
  catch (e) { msg.isRead = 0; unreadCount.value++ }
}

const handleReadAll = async () => {
  readAllLoading.value = true
  try {
    await request.post('/message/readAll')
    messageList.value.forEach(m => m.isRead = 1)
    unreadCount.value = 0
    ElMessage.success('已全部标记为已读')
  } catch (e) { ElMessage.error('操作失败') }
  finally { readAllLoading.value = false }
}

onMounted(fetchMessages)
</script>

<style lang="scss" scoped>
.msg-center {
  min-height: 100vh;
  background: var(--app-bg);
}

// ================================================================
// 顶部
// ================================================================
.msg-center {
  min-height: 100vh;
  background: #f5f7fa;
}

// ================================================================
// 极光顶部
// ================================================================
.msg-topbar-aurora {
  background: linear-gradient(135deg, #00C9A7 0%, #05D5B3 100%);
  padding: 32px 16px 20px;
  position: relative;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    top: -20%; left: -10%; width: 50%; height: 100%;
    background: linear-gradient(135deg, rgba(255,255,255,0.3) 0%, transparent 70%);
    transform: skewX(-20deg);
    pointer-events: none;
  }

  .topbar-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    position: relative;
    z-index: 2;
  }

  .msg-page-title {
    font-size: 26px;
    font-weight: 800;
    color: #fff;
    margin: 0;
    text-shadow: 0 2px 10px rgba(0,0,0,0.1);
  }

  .topbar-right {
    display: flex;
    align-items: center;
    gap: 12px;

    .unread-pill {
      background: rgba(255, 255, 255, 0.2);
      backdrop-filter: blur(10px);
      padding: 4px 12px;
      border-radius: 20px;
      display: flex;
      align-items: center;
      gap: 4px;
      border: 1px solid rgba(255, 255, 255, 0.3);

      .unread-num { color: #fff; font-weight: 800; font-size: 14px; }
      .unread-label { color: rgba(255,255,255,0.8); font-size: 11px; }
    }

    .read-all-btn {
      background: #fff;
      color: #00C9A7;
      border: none;
      padding: 8px 16px;
      border-radius: 12px;
      font-weight: 700;
      font-size: 13px;
      height: auto;
      
      &:hover { background: rgba(255,255,255,0.9); transform: scale(1.05); }
      &:disabled { background: rgba(255,255,255,0.5); color: rgba(255,255,255,0.8); }
    }
  }
}

// ================================================================
// Tab 筛选
// ================================================================
.msg-tabs {
  display: flex;
  gap: 10px;
  padding: 16px;
  background: #f5f7fa;
  overflow-x: auto;
}

.tab-pill {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  background: #fff;
  color: #64748b;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0,0,0,0.03);
  border: 1px solid transparent;

  &.active {
    background: #00C9A7;
    color: #fff;
    font-weight: 700;
    box-shadow: 0 4px 12px rgba(0, 201, 167, 0.2);
  }

  .tab-badge {
    margin-left: 4px;
    :deep(.el-badge__content) {
      background: #f43f5e;
      border: none;
      box-shadow: 0 2px 4px rgba(244, 63, 94, 0.3);
    }
  }
}

// ================================================================
// 消息列表
// ================================================================
.msg-list {
  padding: 4px 0;
}

.msg-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  background: #fff;
  cursor: pointer;
  transition: background 0.15s;
  -webkit-tap-highlight-color: transparent;
  border-bottom: 0.5px solid rgba(0, 0, 0, 0.03);

  &:active { background: #F9F9F9; }

  &.unread {
    .msg-title { font-weight: 700; color: #1C1C1E; }
    .msg-summary { color: #333; }
  }
}

// --- 头像 ---
.msg-avatar-area {
  position: relative;
  flex-shrink: 0;
}

.msg-avatar {
  width: 52px;
  height: 52px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);

  &.type-activity { background: linear-gradient(135deg, #00d2ff 0%, #3a7bd5 100%); }
  &.type-mall { background: linear-gradient(135deg, #f6d365 0%, #fda085 100%); }
  &.type-interaction { background: linear-gradient(135deg, #8E2DE2 0%, #4A00E0 100%); }
  &.type-system { background: linear-gradient(135deg, #00C9A7 0%, #05D5B3 100%); }
}

.avatar-badge {
  position: absolute;
  top: 0;
  right: 0;
  :deep(.el-badge__content) {
    border: 2px solid #fff;
    box-shadow: 0 2px 6px rgba(245, 108, 108, 0.3);
  }
}

// --- 内容 ---
.msg-content {
  flex: 1;
  min-width: 0;
}

.msg-title-line {
  margin-bottom: 4px;
}

.msg-title {
  font-size: 15px;
  font-weight: 500;
  color: #333;
}

.msg-summary {
  font-size: 13px;
  color: #999;
  line-height: 1.4;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.msg-quick-action {
  margin-top: 6px;
}

// --- 时间 ---
.msg-time {
  font-size: 11px;
  color: #C7C7CC;
  white-space: nowrap;
  flex-shrink: 0;
  margin-top: 2px;
}

// --- 底部 ---
.load-more-area {
  text-align: center;
  padding: 16px;
}

.list-end {
  text-align: center;
  padding: 16px;
  font-size: 12px;
  color: #ddd;
}

// --- 过渡动画 ---
.msg-fade-enter-active { transition: all 0.3s ease; }
.msg-fade-leave-active { transition: all 0.2s ease; }
.msg-fade-enter-from { opacity: 0; transform: translateX(-20px); }
.msg-fade-leave-to { opacity: 0; transform: translateX(20px); }

// ================================================================
// PC 适配
// ================================================================
@media (min-width: 769px) {
  .msg-center {
    max-width: 800px;
    margin: 0 auto;
    padding: 0;
    background: #f5f7fa;
  }

  .msg-topbar-aurora {
    border-radius: 0;
    padding: 40px 24px 24px;
  }

  .msg-tabs {
    border-radius: 0;
  }

  .msg-list {
    background: #fff;
    border-radius: 0 0 12px 12px;
    overflow: hidden;
  }

  .msg-row {
    &:hover { background: #FAFAFA; }
  }
}
</style>
