<template>
  <div class="my-reviews-page">

    <!-- ==================== 极光流体 Hero ==================== -->
    <div class="reviews-hero">
      <div class="hero-left">
        <span class="hero-num">{{ animTotal }}</span>
        <span class="hero-label">条评</span>
      </div>
      <div class="hero-right">
        <div class="avg-box">
          <span class="hero-avg-label">平均评分</span>
          <el-rate :model-value="avgScore" disabled :colors="['#ffd700','#ffc107','#ffb300']" size="large" class="hero-rate" />
          <span class="hero-avg-num">{{ avgScore.toFixed(1) }}</span>
        </div>
      </div>
    </div>

    <!-- ==================== 评价列表 ==================== -->
    <div class="reviews-container" v-loading="loading && reviews.length === 0">
      <!-- 骨架屏 4 -->
      <div v-if="loading && reviews.length === 0" class="reviews-grid">
        <div v-for="i in 4" :key="i" class="skeleton-card">
          <el-skeleton animated>
            <template #template>
              <div style="padding: 16px">
                <el-skeleton-item variant="h3" style="width: 50%" />
                <el-skeleton-item variant="text" style="width: 30%; margin-top: 10px" />
                <el-skeleton-item variant="text" style="margin-top: 14px" />
                <el-skeleton-item variant="text" style="width: 80%; margin-top: 6px" />
                <el-skeleton-item variant="button" style="width: 100%; margin-top: 14px; height: 32px" />
              </div>
            </template>
          </el-skeleton>
        </div>
      </div>

      <!-- 卡片 -->
      <TransitionGroup v-else name="card-fade" tag="div" class="reviews-grid">
        <div
          v-for="(review, idx) in reviews"
          :key="review.id"
          class="review-card"
          :class="{ 'just-replied': review._justReplied }"
          :style="{ animationDelay: `${idx * 0.05}s` }"
        >
          <!-- 头部：活动名 + 时间 -->
          <div class="review-head">
            <span class="rv-activity" @click="goToActivity(review.activityId)">{{ review.activityTitle }}</span>
            <span class="rv-time">{{ formatDate(review.createTime) }}</span>
          </div>

          <!-- 评分（点击闪烁） -->
          <div class="score-row" :class="{ 'rate-sparkle': review._sparkle }" @click="handleRateClick(review)">
            <el-rate v-model="review.score" disabled show-score text-color="#ff9900" />
            <el-tag v-if="review.isAnonymous" size="small" type="info" round>匿名</el-tag>
          </div>

          <!-- 内容 -->
          <p class="rv-content">{{ review.content }}</p>

          <!-- 组织者回复 -->
          <div v-if="review.replyContent" class="reply-box">
            <div class="reply-head">
              <el-icon><ChatDotRound /></el-icon>
              <span>组织者回复</span>
              <span class="reply-time">{{ formatDate(review.replyTime) }}</span>
            </div>
            <p class="reply-text">{{ review.replyContent }}</p>
          </div>

          <!-- 操作 -->
          <div class="rv-actions">
            <button class="rv-btn rv-btn-green" @click="goToActivity(review.activityId)">查看活动</button>
            <button class="rv-btn rv-btn-detail" @click="openReplyModal(review)">查看详情</button>
          </div>
        </div>
      </TransitionGroup>

      <el-empty v-if="!loading && reviews.length === 0" description="暂无评价记录" :image-size="120" />

      <!-- 加载更多 / 没有更多 提示 -->
      <div v-if="reviews.length > 0" class="load-more-tip">
        <template v-if="loadingMore">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>加载中...</span>
        </template>
        <template v-else-if="noMore">
          <span class="no-more-text">到底啦，没有更多评价啦</span>
        </template>
      </div>
    </div>

    <!-- ==================== 回复详情 Modal (slide-up) ==================== -->
    <transition name="modal-slide-up">
      <div v-if="replyModalVisible" class="modal-overlay" @click.self="closeReplyModal">
        <div class="reply-modal">
          <div class="modal-header">
            <span class="modal-title">评价详情</span>
            <button class="modal-close" @click="closeReplyModal">&times;</button>
          </div>

          <div class="modal-body">
            <!-- 评价信息 -->
            <div class="modal-review-info">
              <h4 class="modal-activity-name">{{ currentReview?.activityTitle }}</h4>
              <div class="modal-score-row">
                <el-rate v-model="currentReview!.score" disabled show-score text-color="#ff9900" />
                <span class="modal-time">{{ formatDate(currentReview?.createTime) }}</span>
              </div>
              <p class="modal-content">{{ currentReview?.content }}</p>
            </div>

            <!-- 回复区域 -->
            <div v-if="currentReview?.replyContent" class="modal-reply-section">
              <div class="modal-reply-label">
                <el-icon><ChatDotRound /></el-icon>
                组织者回复
              </div>
              <p class="modal-reply-text">{{ currentReview.replyContent }}</p>
              <span class="modal-reply-time">{{ formatDate(currentReview.replyTime) }}</span>
            </div>

            <div v-else class="modal-no-reply">
              <el-icon><ChatDotRound /></el-icon>
              <span>暂无组织者回复</span>
            </div>
          </div>

          <div class="modal-footer">
            <button class="rv-btn rv-btn-green" @click="closeReplyModal">关闭</button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ChatDotRound, Loading } from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import dayjs from 'dayjs'

const router = useRouter()
const reviews = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const loadingMore = ref(false)
const animTotal = ref(0)

// ==================== 回复 Modal ====================
const replyModalVisible = ref(false)
const currentReview = ref<any>(null)

const openReplyModal = (review: any) => {
  currentReview.value = review
  replyModalVisible.value = true
}

const closeReplyModal = () => {
  replyModalVisible.value = false
  setTimeout(() => { currentReview.value = null }, 300)
}

// ==================== 无限滚动（手动监听window scroll） ====================
const noMore = computed(() => reviews.value.length >= total.value && total.value > 0)

const loadMore = async () => {
  if (noMore.value || loadingMore.value || loading.value) return
  loadingMore.value = true
  pageNum.value++
  try {
    const res: any = await request.get('/volunteer/review/my', { pageNum: pageNum.value, pageSize: pageSize.value })
    const newItems = res.data?.records || []
    reviews.value.push(...newItems)
  } catch (e) {
    console.error('加载更多评价失败', e)
    pageNum.value--
  } finally {
    loadingMore.value = false
  }
}

const handleScroll = () => {
  if (noMore.value || loadingMore.value || loading.value) return
  const scrollTop = window.scrollY || document.documentElement.scrollTop
  const windowHeight = window.innerHeight
  const docHeight = document.documentElement.scrollHeight
  // 距底部150px 时触发加载
  if (scrollTop + windowHeight >= docHeight - 150) {
    loadMore()
  }
}

// ==================== 星级闪烁 ====================
const handleRateClick = (review: any) => {
  review._sparkle = true
  setTimeout(() => { review._sparkle = false }, 200)
}

// ==================== 平均评分 ====================
const avgScore = computed(() => {
  if (reviews.value.length === 0) return 0
  const sum = reviews.value.reduce((a, r) => a + (r.score || 0), 0)
  return sum / reviews.value.length
})

// ==================== 数字动画 ====================
const animateNumber = (target: number, setter: (v: number) => void, duration = 600) => {
  const start = performance.now()
  const step = (now: number) => {
    const p = Math.min((now - start) / duration, 1)
    setter(Math.round((1 - Math.pow(1 - p, 3)) * target))
    if (p < 1) requestAnimationFrame(step)
  }
  requestAnimationFrame(step)
}

// ==================== 首次加载 ====================
const fetchReviews = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/volunteer/review/my', { pageNum: pageNum.value, pageSize: pageSize.value })
    reviews.value = res.data?.records || []
    total.value = res.data?.total || 0
    animateNumber(total.value, v => { animTotal.value = v })
  } catch (e) { console.error('获取评价失败', e) }
  finally { loading.value = false }
}

const formatDate = (d: string) => d ? dayjs(d).format('YYYY-MM-DD HH:mm') : ''
const goToActivity = (id: number) => { router.push(`/activity/${id}`) }

onMounted(() => {
  fetchReviews()
  window.addEventListener('scroll', handleScroll, { passive: true })
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style lang="scss" scoped>
@keyframes cardIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
@keyframes sparkle {
  0% { transform: scale(1); filter: brightness(1); }
  50% { transform: scale(1.12); filter: brightness(1.5) drop-shadow(0 0 6px rgba(255, 215, 0, 0.7)); }
  100% { transform: scale(1); filter: brightness(1); }
}
@keyframes fadeInHighlight {
  from { opacity: 0.3; background: rgba(0, 201, 167, 0.08); }
  to { opacity: 1; background: #fff; }
}

.my-reviews-page { min-height: 100vh; background: #f5f7fa; }

// ================================================================
// Hero
// ================================================================
.reviews-hero {
  background: linear-gradient(135deg, #00C9A7 0%, #05D5B3 100%);
  padding: 32px 20px 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
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
}

.hero-left {
  display: flex; align-items: baseline; gap: 4px;
  .hero-num { font-size: 52px; font-weight: 900; color: #fff; line-height: 1; text-shadow: 0 4px 12px rgba(0,0,0,0.15); }
  .hero-label { font-size: 14px; color: rgba(255,255,255,0.8); }
}

.hero-right {
  display: flex; flex-direction: column; align-items: flex-end;
  
  .avg-box {
    background: rgba(255,255,255,0.15);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255,255,255,0.2);
    border-radius: 16px;
    padding: 8px 16px;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 2px;
  }

  .hero-avg-label { font-size: 11px; color: rgba(255,255,255,0.9); font-weight: 600; }
  .hero-rate { :deep(.el-rate__icon) { font-size: 18px; } }
  .hero-avg-num { font-size: 24px; font-weight: 900; color: #ffd700; text-shadow: 0 2px 8px rgba(0,0,0,0.1); }
}

// ================================================================
// 评价列表
// ================================================================
.reviews-container { padding: 16px; }

.reviews-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 14px; }

.skeleton-card { background: #fff; border-radius: 14px; overflow: hidden; }

.review-card {
  background: #fff; border-radius: 14px; overflow: hidden; box-shadow: 0 4px 16px rgba(0,0,0,0.03); animation: cardIn 0.35s ease-out both; transition: all 0.3s;
  display: flex; flex-direction: column; padding: 20px;

  &:hover { 
    transform: translateY(-6px); 
    box-shadow: 0 12px 32px rgba(0,0,0,0.08); 
    .rv-activity { color: #00C9A7; }
  }

  &.just-replied { animation: fadeInHighlight 0.4s ease-out both; }

  .review-head {
    display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px;
    .rv-activity { font-size: 14px; font-weight: 600; color: #333; cursor: pointer; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-right: 8px;
      &:hover { color: #00C9A7; }
    }
    .rv-time { font-size: 11px; color: #bbb; white-space: nowrap; }
  }

  .score-row {
    display: flex; align-items: center; gap: 8px; margin-bottom: 8px; cursor: pointer; transition: transform 0.2s;
    &.rate-sparkle { animation: sparkle 0.2s ease-out; }
  }

  .rv-content { margin: 0 0 12px; font-size: 13px; color: #666; line-height: 1.6; white-space: pre-wrap; flex: 1; }

  .reply-box {
    margin-bottom: 12px; padding: 10px 12px; background: #f9fafb; border-radius: 10px; border-left: 3px solid #05D5B3;
    .reply-head { display: flex; align-items: center; gap: 5px; font-size: 12px; color: #888; margin-bottom: 4px;
      .reply-time { margin-left: auto; font-size: 11px; color: #bbb; }
    }
    .reply-text { margin: 0; font-size: 12px; color: #666; line-height: 1.5; }
  }

  .rv-actions { display: flex; gap: 8px; }
}

.rv-btn {
  flex: 1; border: none; padding: 7px 0; border-radius: 10px; font-size: 12px; font-weight: 600; cursor: pointer; transition: all 0.25s;
  &:active { transform: scale(0.96); }
}

.rv-btn-green {
  background: linear-gradient(135deg, #00C9A7, #05D5B3); color: #fff;
  &:hover { transform: scale(1.03); box-shadow: 0 4px 12px rgba(0, 201, 167, 0.35); }
}

.rv-btn-detail {
  background: #f5f7fa; color: #666; border: 1px solid #e8e8e8;
  &:hover { color: #00C9A7; border-color: #00C9A7; background: rgba(0, 201, 167, 0.04); }
}

// ================================================================
// TransitionGroup
// ================================================================
.card-fade-enter-active { transition: all 0.4s ease-out; }
.card-fade-leave-active { transition: all 0.2s ease-in; }
.card-fade-enter-from { opacity: 0; transform: translateY(16px); }
.card-fade-leave-to { opacity: 0; transform: scale(0.96); }

// ================================================================
// 加载更多提示
// ================================================================
.load-more-tip {
  display: flex; align-items: center; justify-content: center; gap: 6px;
  padding: 20px 0; color: #bbb; font-size: 13px;

  .is-loading { animation: spin 1s linear infinite; font-size: 16px; }
  .no-more-text { letter-spacing: 2px; }
}

@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }

// ================================================================
// 回复 Modal (slide-up)
// ================================================================
.modal-overlay {
  position: fixed; inset: 0; z-index: 2000;
  background: rgba(0, 0, 0, 0.45);
  display: flex; align-items: flex-end; justify-content: center;
  backdrop-filter: blur(2px);
}

.reply-modal {
  width: 100%; max-width: 540px;
  background: #fff; border-radius: 20px 20px 0 0;
  max-height: 80vh; display: flex; flex-direction: column;
  box-shadow: 0 -8px 40px rgba(0,0,0,0.12);
}

.modal-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 18px 20px 12px; border-bottom: 1px solid #f0f0f0;

  .modal-title { font-size: 16px; font-weight: 700; color: #333; }
  .modal-close {
    width: 30px; height: 30px; border-radius: 50%; border: none; background: #f5f5f5;
    font-size: 14px; color: #999; cursor: pointer; display: flex; align-items: center; justify-content: center;
    transition: all 0.2s;
    &:hover { background: #eee; color: #333; }
  }
}

.modal-body {
  padding: 16px 20px; overflow-y: auto; flex: 1;

  .modal-review-info { margin-bottom: 16px; }

  .modal-activity-name { margin: 0 0 10px; font-size: 15px; font-weight: 600; color: #333; }

  .modal-score-row {
    display: flex; align-items: center; gap: 12px; margin-bottom: 10px;
    .modal-time { font-size: 12px; color: #bbb; margin-left: auto; }
  }

  .modal-content { margin: 0; font-size: 14px; color: #555; line-height: 1.7; white-space: pre-wrap; }

  .modal-reply-section {
    margin-top: 16px; padding: 14px 16px; background: #f9fafb; border-radius: 12px; border-left: 3px solid #05D5B3;

    .modal-reply-label {
      display: flex; align-items: center; gap: 5px; font-size: 13px; color: #888; font-weight: 600; margin-bottom: 8px;
    }
    .modal-reply-text { margin: 0; font-size: 13px; color: #555; line-height: 1.6; }
    .modal-reply-time { display: block; margin-top: 8px; font-size: 11px; color: #bbb; text-align: right; }
  }

  .modal-no-reply {
    margin-top: 16px; padding: 24px; text-align: center; color: #ccc; font-size: 13px;
    display: flex; flex-direction: column; align-items: center; gap: 8px;
    .el-icon { font-size: 28px; }
  }
}

.modal-footer { padding: 12px 20px 20px; }
</style>

<style lang="scss">
/* modal slide-up 过渡 (必须放在非scoped 块中，否则transition 根节点匹配不到) */
.modal-slide-up-enter-active { transition: opacity 0.3s ease-out; }
.modal-slide-up-enter-active .reply-modal { transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1); }
.modal-slide-up-leave-active { transition: opacity 0.25s ease-in; }
.modal-slide-up-leave-active .reply-modal { transition: transform 0.25s cubic-bezier(0.4, 0, 1, 1); }
.modal-slide-up-enter-from { opacity: 0; }
.modal-slide-up-enter-from .reply-modal { transform: translateY(100%); }
.modal-slide-up-enter-to { opacity: 1; }
.modal-slide-up-enter-to .reply-modal { transform: translateY(0); }
.modal-slide-up-leave-from { opacity: 1; }
.modal-slide-up-leave-from .reply-modal { transform: translateY(0); }
.modal-slide-up-leave-to { opacity: 0; }
.modal-slide-up-leave-to .reply-modal { transform: translateY(100%); }

/* 响应式 */
@media (max-width: 768px) {
  .my-reviews-page .reviews-container { padding: 12px; }
  .my-reviews-page .reviews-grid { grid-template-columns: 1fr; }
  .my-reviews-page .hero-left .hero-num { font-size: 36px; }
  .my-reviews-page .hero-right .hero-rate .el-rate__icon { font-size: 16px; }
  .my-reviews-page .reply-modal { max-width: 100%; }
}

@media (min-width: 769px) {
  .my-reviews-page .reviews-container { max-width: 1000px; margin: 0 auto; padding: 20px 24px; }
  .my-reviews-page .reply-modal { border-radius: 20px; margin-bottom: 10vh; }
  .my-reviews-page .modal-overlay { align-items: center; }
}
</style>
