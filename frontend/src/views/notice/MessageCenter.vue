<template>
  <div class="message-center">
    <!-- Header Area -->
    <div class="page-header">
      <div class="header-left">
        <h2>消息中心</h2>
        <span class="unread-badge" v-if="unreadCount > 0">{{ unreadCount }}条未读</span>
      </div>
      <el-button 
        type="primary" 
        plain 
        size="small" 
        @click="handleReadAll" 
        :loading="readAllLoading"
        :disabled="unreadCount === 0"
      >
        <el-icon class="el-icon--left"><Check /></el-icon>
        一键已读
      </el-button>
    </div>

    <!-- Tabs Layout -->
    <el-tabs v-model="activeTab" class="message-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="全部消息" name="ALL" />
      <el-tab-pane label="活动动态" name="ACTIVITY" />
      <el-tab-pane label="商城与系统" name="MALL_SYSTEM" />
    </el-tabs>

    <!-- Message List (Card Layout) -->
    <div class="message-list" v-loading="loading">
      <transition-group name="list">
        <el-card 
          v-for="msg in messageList" 
          :key="msg.id" 
          class="message-card"
          :class="{ 'is-unread': msg.isRead === 0 }"
          shadow="hover"
          @click="handleRead(msg)"
        >
          <div class="card-content">
            <!-- Icon Column -->
            <div class="icon-wrapper" :class="getTypeClass(msg.type)">
              <el-icon :size="24">
                <component :is="getTypeIcon(msg.type)" />
              </el-icon>
            </div>

            <!-- Main Content -->
            <div class="text-content">
              <div class="msg-header">
                <span class="msg-title">{{ msg.title }}</span>
                <span class="msg-time">{{ formatTime(msg.createTime) }}</span>
              </div>
              <div class="msg-body" v-html="highlightKeywords(msg.content)"></div>
              
              <!-- Action Buttons (Conditional) -->
              <div class="msg-actions" v-if="hasAction(msg)">
                <el-button 
                  v-if="msg.content.includes('兑换成功')" 
                  type="primary" 
                  link 
                  size="small"
                  @click.stop="router.push('/mall/backpack')"
                >
                  查看领取地址 <el-icon><ArrowRight /></el-icon>
                </el-button>
                <el-button 
                  v-if="msg.content.includes('催签到') || msg.title.includes('签到')" 
                  type="success" 
                  link 
                  size="small"
                  @click.stop="router.push('/mall/checkin')"
                >
                  立即签到 <el-icon><ArrowRight /></el-icon>
                </el-button>
              </div>
            </div>

            <!-- Unread Dot -->
            <div class="unread-dot" v-if="msg.isRead === 0"></div>
          </div>
        </el-card>
      </transition-group>

      <!-- Empty State -->
      <el-empty 
        v-if="!loading && messageList.length === 0" 
        :description="getEmptyText()" 
        :image-size="120"
      />
      
      <!-- Load More / Pagination -->
      <div class="load-more" v-if="hasMore">
        <el-button link @click="loadMore" :loading="loading">加载更多</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Check, Flag, Present, ArrowRight, Service } from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()

// Data
const activeTab = ref('ALL')
const messageList = ref<any[]>([])
const loading = ref(false)
const readAllLoading = ref(false)
const unreadCount = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)

// Helper: Icons & Colors
const getTypeClass = (type: string) => {
  if (type === 'MALL') return 'icon-mall' // Orange
  if (type === 'ACTIVITY') return 'icon-activity' // Blue
  return 'icon-system' // Gray
}

const getTypeIcon = (type: string) => {
  if (type === 'MALL') return Present
  if (type === 'ACTIVITY') return Flag
  return Service // Or Bell
}

// Helper: Time
const formatTime = (time: string) => {
  return dayjs(time).fromNow()
}

// Helper: Highlight
const highlightKeywords = (content: string) => {
  if (!content) return ''
  // 简单的高亮逻辑，实际可以更复杂
  return content
    .replace(/(地点|时间|领取码)：(.*?)(，|。|\s|$)/g, '<b>$1：$2</b>$3')
}

// Helper: Check Actions
const hasAction = (msg: any) => {
  return msg.content.includes('兑换成功') || msg.content.includes('催签到') || msg.title.includes('签到')
}

// Empty Text
const getEmptyText = () => {
  const map: Record<string, string> = {
    'ALL': '暂无消息',
    'ACTIVITY': '暂无活动动态',
    'MALL_SYSTEM': '暂无系统通知'
  }
  return map[activeTab.value]
}

const hasMore = computed(() => {
  return messageList.value.length < total.value
})

// Fetch Data
const fetchMessages = async (isLoadMore = false) => {
  loading.value = true
  try {
    const params: any = {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    
    // Convert Tab to API Type
    if (activeTab.value === 'ACTIVITY') {
      params.type = 'ACTIVITY'
    } else if (activeTab.value === 'MALL_SYSTEM') {
      params.type = 'SYSTEM,MALL,NOTICE,URGENT' // Pass multiple types
    }

    const res = await request.get('/message/list', params)
    const newData = res.data.records || []

    if (isLoadMore) {
      messageList.value = [...messageList.value, ...newData]
    } else {
      messageList.value = newData
    }
    
    total.value = res.data.total
    
    // Also fetch unread count
    fetchUnreadCount()
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const fetchUnreadCount = async () => {
  try {
    const res = await request.get('/message/unreadCount')
    unreadCount.value = res.data
  } catch (e) {}
}

const loadMore = () => {
  pageNum.value++
  fetchMessages(true)
}

const handleTabChange = () => {
  pageNum.value = 1
  fetchMessages()
}

// Actions
const handleRead = async (msg: any) => {
  if (msg.isRead === 1) return
  
  // Optimistic update
  msg.isRead = 1
  unreadCount.value = Math.max(0, unreadCount.value - 1)
  
  try {
    await request.post(`/message/read/${msg.id}`)
  } catch (e) {
    msg.isRead = 0 // Revert
    unreadCount.value++
  }
}

const handleReadAll = async () => {
    readAllLoading.value = true
    try {
        await request.post('/message/readAll')
        messageList.value.forEach(m => m.isRead = 1)
        unreadCount.value = 0
        ElMessage.success('已全部标记为已读')
    } catch (e) {
        ElMessage.error('操作失败')
    } finally {
        readAllLoading.value = false
    }
}

onMounted(() => {
  fetchMessages()
})
</script>

<style lang="scss" scoped>
.message-center {
  max-width: 800px;
  margin: 20px auto;
  padding: 0 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
    
    h2 {
      margin: 0;
      font-size: 24px;
      color: var(--el-text-color-primary);
    }
    
    .unread-badge {
      background: #f56c6c;
      color: #fff;
      font-size: 12px;
      padding: 2px 8px;
      border-radius: 10px;
    }
  }
}

.message-tabs {
  margin-bottom: 20px;
  
  :deep(.el-tabs__header) {
    margin-bottom: 20px;
  }
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-card {
  transition: all 0.2s;
  cursor: pointer;
  border: none;
  background-color: var(--el-bg-color);
  
  &.is-unread {
    border-left: 4px solid #67c23a; // Green vertical border
    
    .msg-title {
      font-weight: bold;
    }
  }
}

.card-content {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  position: relative;
}

.icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  
  &.icon-mall {
    background: #fdf5e6;
    color: #e6a23c;
  }
  
  &.icon-activity {
    background: #ecf5ff;
    color: #409eff;
  }
  
  &.icon-system {
    background: #f4f4f5;
    color: #909399;
  }
}

.text-content {
  flex: 1;
  min-width: 0;
}

.msg-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  
  .msg-title {
    font-size: 16px;
    color: var(--el-text-color-primary);
  }
  
  .msg-time {
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
}

.msg-body {
  font-size: 14px;
  color: var(--el-text-color-regular);
  line-height: 1.6;
  margin-bottom: 8px;
  
  :deep(b) {
    color: var(--el-color-primary);
    font-weight: 600;
  }
}

.msg-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

.unread-dot {
  position: absolute;
  top: 0;
  right: 0;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #f56c6c;
}

.load-more {
  text-align: center;
  margin-top: 20px;
}

/* Dark Mode Adaptation */
:deep(html.dark) {
  .message-card.is-unread {
    background-color: #1d1e1f;
  }
}
</style>
