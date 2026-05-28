<template>
  <div class="page-container">
    <!-- 评分概览卡片 (App Store 风格) -->
    <div class="score-summary-card" v-loading="statsLoading">
      <div class="left-score">
        <span class="big-num">{{ stats.averageScore?.toFixed(1) || '0.0' }}</span>
        <div class="stars">
          <el-rate :model-value="Number(stats.averageScore) || 0" disabled text-color="#ff9900" />
        </div>
        <span class="total-text">{{ stats.totalReviews || 0 }} 条评论</span>
      </div>
      <div class="right-bars">
        <div class="bar-row" v-for="i in [5,4,3,2,1]" :key="i">
          <span class="star-label">{{ i }}</span>
          <div class="progress-track">
            <div 
              class="progress-fill" 
              :style="{ 
                width: getDistPercentage(i) + '%',
                background: getDistColor(i)
              }"
            ></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 筛选标签 -->
    <div class="filter-tabs">
      <div 
        v-for="tab in filterTabs" 
        :key="tab.value"
        class="tab-item" 
        :class="{ active: filterType === tab.value }"
        @click="filterType = tab.value; handleFilterChange()"
      >
        {{ tab.label }}
        <span class="badge" v-if="tab.badge">{{ tab.badge }}</span>
      </div>
    </div>

    <!-- 评价列表 -->
    <div class="reviews-list" v-loading="listLoading">
      <div class="review-card" v-for="item in reviews" :key="item.id">
        <div class="user-row">
          <el-avatar :size="36" :src="item.isAnonymous ? undefined : item.volunteerAvatar">
            <el-icon><User /></el-icon>
          </el-avatar>
          <div class="user-info">
            <div class="name-time">
              <span class="name">{{ item.isAnonymous ? '匿名同学' : item.volunteerName }}</span>
              <span class="time">{{ formatShortDate(item.createTime) }}</span>
            </div>
            <el-rate :model-value="item.score" disabled size="small" />
          </div>
        </div>
        
        <div class="review-content">{{ item.content }}</div>
        <div class="review-source">活动：{{ item.activityTitle }}</div>
        
        <!-- 组织者回复气?-->
        <div class="reply-bubble" v-if="item.replyContent">
          <div class="reply-header">
            <el-icon><Service /></el-icon> 组织者回复
          </div>
          <div class="reply-text">{{ item.replyContent }}</div>
        </div>
        
        <!-- 操作按钮 -->
        <div class="card-actions">
          <el-button v-if="!item.replyContent" size="small" round class="reply-btn" @click="isMobile ? openMobileReply(item) : openReplyDialog(item)">
            回复
          </el-button>
          <el-button v-if="item.score <= 2" size="small" round class="appeal-btn" @click="handleAppeal(item)">
            申诉
          </el-button>
        </div>
      </div>

      <el-empty v-if="!listLoading && reviews.length === 0" description="暂无评价" />
      
      <!-- 分页 -->
      <div class="pag-section" v-if="total > pageSize">
        <el-pagination 
          v-model:current-page="pageNum" 
          :total="total" 
          :page-size="pageSize" 
          :small="isMobile"
          layout="prev, pager, next" 
          background 
          @current-change="fetchReviews" 
        />
      </div>
    </div>

    <!-- PC 回复弹窗 -->
    <el-dialog v-model="replyDialogVisible" title="回复评价" width="460px" destroy-on-close>
      <div class="reply-ctx" v-if="currentReview">
        <span class="ctx-user">关于 {{ currentReview.isAnonymous ? '匿名同学' : currentReview.volunteerName }} 的评价</span>
        <p class="ctx-text">{{ currentReview.content }}</p>
      </div>
      <el-input v-model="replyContent" type="textarea" :rows="4" placeholder="真诚表达您的感谢或解释..." maxlength="300" show-word-limit />
      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply" :loading="replying">确认发送</el-button>
      </template>
    </el-dialog>

    <!-- 移动端回复面?-->
    <Transition name="slide-up">
      <div class="m-reply-overlay" v-if="mobileReplyVisible" @click.self="mobileReplyVisible = false">
        <div class="m-reply-panel">
          <div class="m-rp-header">
            <span>回复 {{ currentReview?.isAnonymous ? '匿名同学' : currentReview?.volunteerName }}</span>
            <button class="m-rp-close" @click="mobileReplyVisible = false">&times;</button>
          </div>
          <div class="m-rp-quote">"{{ currentReview?.content?.substring(0, 60) }}..."</div>
          <div class="m-rp-input-row">
            <textarea v-model="replyContent" placeholder="输入回复内容..." rows="3" ref="mobileTextareaRef"></textarea>
            <button class="m-rp-send" @click="submitReply" :disabled="replying || !replyContent.trim()">
              {{ replying ? '...' : '发送' }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Service } from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import dayjs from 'dayjs'
import { useMobile } from '@/composables/useMobile'

const statsLoading = ref(false)
const listLoading = ref(false)
const replying = ref(false)
const replyDialogVisible = ref(false)
const mobileReplyVisible = ref(false)
const currentReview = ref<any>(null)
const replyContent = ref('')
const mobileTextareaRef = ref<HTMLTextAreaElement | null>(null)

const { isMobile } = useMobile()

interface ReviewStats {
  averageScore: number; positiveRate: number; totalReviews: number
  negativeCount: number; pendingReplyCount: number; scoreDistribution?: Record<string, number>
}

const stats = reactive<ReviewStats>({
  averageScore: 0, positiveRate: 0, totalReviews: 0, negativeCount: 0, pendingReplyCount: 0,
  scoreDistribution: { '5': 0, '4': 0, '3': 0, '2': 0, '1': 0 }
})

const reviews = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const filterType = ref('ALL')

const filterTabs = computed(() => [
  { label: '全部', value: 'ALL', badge: null },
  { label: '待回复', value: 'PENDING', badge: stats.pendingReplyCount || null },
  { label: '差评', value: 'NEGATIVE', badge: stats.negativeCount || null }
])

const getDistPercentage = (star: number) => {
  if (!stats.totalReviews) return 0
  return Math.round(((stats.scoreDistribution?.[star.toString()] || 0) / stats.totalReviews) * 100)
}
const getDistColor = (star: number) => { 
  if (star >= 4) return '#FF9900'
  if (star === 3) return '#FFB84D'
  return '#FFCC80' 
}
const formatDate = (date?: string) => date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-'
const formatShortDate = (date?: string) => {
  if (!date) return '-'
  const d = dayjs(date); const now = dayjs()
  if (d.isSame(now, 'day')) return d.format('HH:mm')
  if (d.isSame(now, 'year')) return d.format('MM-DD')
  return d.format('YY-MM-DD')
}

const fetchStats = async () => {
  statsLoading.value = true
  try {
    const res = await request.get('/review/stats')
    if (res.data) { 
      Object.assign(stats, res.data)
      if (!stats.scoreDistribution) {
        stats.scoreDistribution = { '5': 0, '4': 0, '3': 0, '2': 0, '1': 0 }
      }
    }
  } catch (e) {} finally { statsLoading.value = false }
}

const fetchReviews = async () => {
  listLoading.value = true
  try {
    const res = await request.get('/review/list', { pageNum: pageNum.value, pageSize: pageSize.value, type: filterType.value })
    reviews.value = res.data?.records || []; total.value = res.data?.total || 0
  } catch (e) {} finally { listLoading.value = false }
}

const handleFilterChange = () => { pageNum.value = 1; fetchReviews() }

const openReplyDialog = (item: any) => { currentReview.value = item; replyContent.value = ''; replyDialogVisible.value = true }
const openMobileReply = (item: any) => {
  currentReview.value = item; replyContent.value = ''; mobileReplyVisible.value = true
  nextTick(() => mobileTextareaRef.value?.focus())
}

const submitReply = async () => {
  if (!replyContent.value.trim()) return ElMessage.warning('请输入内容')
  replying.value = true
  try {
    await request.post('/review/reply', { reviewId: currentReview.value.id, content: replyContent.value })
    ElMessage.success('回复成功')
    replyDialogVisible.value = false; mobileReplyVisible.value = false
    fetchReviews(); fetchStats()
  } finally { replying.value = false }
}

const handleAppeal = async (item: any) => {
  try {
    await ElMessageBox.confirm('申诉期间该评价将对公众隐藏，确认申诉？', '评价申诉', { type: 'warning' })
    await request.post('/review/appeal', {}, { params: { reviewId: item.id } })
    ElMessage.success('已提交申诉'); fetchReviews()
  } catch {}
}

onMounted(() => { fetchStats(); fetchReviews() })
</script>

<style scoped lang="scss">
.page-container { 
  background: #F5F7FA; 
  min-height: 100vh; 
  padding: 16px; 
  padding-bottom: 80px; 
  max-width: 800px;
  margin: 0 auto;
}

/* 概览卡片 (App Store 风格) */
.score-summary-card {
  background: #fff; 
  border-radius: 20px; 
  padding: 24px;
  display: flex; 
  gap: 32px; 
  box-shadow: 0 4px 15px rgba(0,0,0,0.03);
  margin-bottom: 24px;
  align-items: center;

  @media (max-width: 480px) {
    gap: 20px;
    padding: 20px;
  }
}

.left-score { 
  text-align: center; 
  display: flex; 
  flex-direction: column; 
  align-items: center; 
  justify-content: center; 
  min-width: 100px;
}

.big-num { 
  font-size: 56px; 
  font-weight: 800; 
  color: #333; 
  line-height: 1; 
  letter-spacing: -2px; 
}

.stars {
  margin-top: 8px;
  :deep(.el-rate) {
    height: 20px;
    .el-rate__icon { margin-right: 2px; }
  }
}

.total-text { font-size: 13px; color: #999; margin-top: 4px; font-weight: 500; }

.right-bars { 
  flex: 1; 
  display: flex; 
  flex-direction: column; 
  justify-content: center; 
  gap: 6px; 
}

.bar-row { 
  display: flex; 
  align-items: center; 
  gap: 10px; 
  font-size: 11px; 
  color: #999; 
}

.star-label { min-width: 8px; font-weight: 600; text-align: center; }

.progress-track { 
  flex: 1; 
  height: 6px; 
  background: #F2F2F7; 
  border-radius: 3px; 
  overflow: hidden; 
}

.progress-fill { 
  height: 100%; 
  background: #FF9900; 
  border-radius: 3px; 
  transition: width 0.6s ease;
}

/* 筛选标签 */
.filter-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  overflow-x: auto;
  padding-bottom: 4px;
  &::-webkit-scrollbar { display: none; }
}

.tab-item {
  padding: 8px 16px;
  background: #fff;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  color: #666;
  cursor: pointer;
  white-space: nowrap;
  box-shadow: 0 2px 6px rgba(0,0,0,0.02);
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 4px;

  &.active {
    background: #409EFF;
    color: #fff;
  }

  .badge {
    background: rgba(255,255,255,0.2);
    color: inherit;
    font-size: 11px;
    padding: 0 6px;
    border-radius: 10px;
    &.active { background: rgba(0,0,0,0.1); }
  }
}

/* 评论卡片 */
.review-card {
  background: #fff; 
  border-radius: 16px; 
  padding: 16px; 
  margin-bottom: 16px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.02);
  transition: transform 0.2s;
  
  &:active { transform: scale(0.99); }
}

.user-row { 
  display: flex; 
  gap: 12px; 
  margin-bottom: 12px; 
}

.user-info { flex: 1; }

.name-time { 
  display: flex; 
  justify-content: space-between; 
  align-items: center; 
  margin-bottom: 2px; 
}

.name { font-weight: 700; font-size: 15px; color: #333; }
.time { font-size: 12px; color: #BBB; }

.review-content { 
  font-size: 15px; 
  color: #333; 
  line-height: 1.6; 
  margin-bottom: 10px; 
  word-break: break-all;
}

.review-source { 
  font-size: 12px; 
  color: #8E8E93; 
  margin-bottom: 12px; 
  background: #F8F9FA; 
  padding: 6px 10px; 
  border-radius: 8px; 
  display: inline-block; 
  font-weight: 500;
}

/* 官方回复气泡 */
.reply-bubble {
  background: #F0F9FF;
  border: 1px solid rgba(64,158,255,0.15);
  border-radius: 14px; 
  padding: 14px;
  position: relative;
  margin-top: 8px;
  margin-bottom: 8px;
  
  &::before {
    content: '';
    position: absolute;
    top: -6px;
    left: 20px;
    width: 10px;
    height: 10px;
    background: #F0F9FF;
    border-left: 1px solid rgba(64,158,255,0.15);
    border-top: 1px solid rgba(64,158,255,0.15);
    transform: rotate(45deg);
  }
}

.reply-header { 
  font-size: 13px; 
  font-weight: 800; 
  color: #409EFF; 
  display: flex; 
  align-items: center; 
  gap: 6px; 
  margin-bottom: 6px; 
}

.reply-text { font-size: 14px; color: #444; line-height: 1.5; }

.card-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 12px;
  
  .reply-btn {
    border-color: #409EFF;
    color: #409EFF;
    &:hover { background: #ECF5FF; }
  }
  
  .appeal-btn {
    border-color: #E6A23C;
    color: #E6A23C;
    &:hover { background: #FCF6ED; }
  }
}

.pag-section {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

/* 回复弹窗相关 */
.reply-ctx { 
  background: #F8F9FA; 
  padding: 16px; 
  border-radius: 12px; 
  margin-bottom: 20px;
  border-left: 4px solid #409EFF;
  .ctx-user { font-size: 13px; font-weight: 700; color: #475569; } 
  .ctx-text { font-size: 13px; color: #64748b; margin: 6px 0 0; line-height: 1.5; }
}

/* 移动端回复面板 */
.m-reply-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.4); z-index: 2000;
  display: flex; align-items: flex-end;
}
.m-reply-panel {
  width: 100%; background: #fff; border-radius: 20px 20px 0 0; padding: 20px;
  padding-bottom: max(20px, env(safe-area-inset-bottom));

  .m-rp-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;
    span { font-size: 16px; font-weight: 700; color: #000; }
    .m-rp-close { border: none; background: transparent; font-size: 20px; color: #8e8e93; cursor: pointer; padding: 4px; }
  }
  .m-rp-quote { font-size: 14px; color: #8e8e93; font-style: italic; margin-bottom: 16px; padding: 10px 14px; background: #f2f2f7; border-radius: 10px; }
  .m-rp-input-row { display: flex; flex-direction: column; gap: 12px;
    textarea { width: 100%; border: 1px solid #e5e5ea; border-radius: 12px; padding: 12px; font-size: 15px; resize: none; outline: none; font-family: inherit;
      &:focus { border-color: #409EFF; }
    }
    .m-rp-send { background: #409EFF; color: #fff; border: none; border-radius: 12px; padding: 12px; font-size: 16px; font-weight: 700; cursor: pointer;
      &:disabled { opacity: 0.4; }
      &:active { opacity: 0.8; }
    }
  }
}

/* Transitions */
.slide-up-enter-active, .slide-up-leave-active { transition: all 0.3s cubic-bezier(0.25, 0.1, 0.25, 1); }
.slide-up-enter-from, .slide-up-leave-to { opacity: 0; .m-reply-panel { transform: translateY(100%); } }

:deep(.el-rate__text) { font-size: 14px; font-weight: 700; }
</style>
