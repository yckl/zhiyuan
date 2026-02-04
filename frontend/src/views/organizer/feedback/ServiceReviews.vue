<template>
  <div class="org-service-reviews">
    <!-- 统计看板 -->
    <div class="stats-board" v-loading="statsLoading">
      <div class="stat-card main-score">
        <div class="stat-content">
          <div class="score-display">
            <span class="score-value">{{ stats.averageScore?.toFixed(1) || '0.0' }}</span>
            <span class="score-unit">分</span>
          </div>
          <el-rate :model-value="stats.averageScore || 0" disabled show-score />
        </div>
        <div class="stat-label">综合评分</div>
      </div>

      <div class="stat-card">
        <div class="stat-content">
          <el-progress
            type="dashboard"
            :percentage="stats.positiveRate || 0"
            :width="100"
            :stroke-width="10"
            :color="getProgressColor(stats.positiveRate)"
          />
        </div>
        <div class="stat-label">好评率</div>
      </div>

      <div class="stat-card">
        <div class="stat-content">
          <span class="stat-number total">{{ stats.totalReviews || 0 }}</span>
        </div>
        <div class="stat-label">总评价数</div>
      </div>

      <div class="stat-card">
        <div class="stat-content">
          <span class="stat-number pending">{{ stats.pendingReplyCount || 0 }}</span>
        </div>
        <div class="stat-label">待回复</div>
      </div>

      <div class="stat-card">
        <div class="stat-content">
          <span class="stat-number negative">{{ stats.negativeCount || 0 }}</span>
        </div>
        <div class="stat-label">差评数</div>
      </div>
    </div>

    <!-- 评价列表 -->
    <el-card shadow="hover" class="list-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon :size="18"><ChatDotRound /></el-icon>
            <span class="title">评价管理</span>
          </div>
          <el-radio-group v-model="filterType" size="default" @change="handleFilterChange">
            <el-radio-button label="ALL">全部评价</el-radio-button>
            <el-radio-button label="PENDING">
              待回复 
              <el-badge :value="stats.pendingReplyCount" :hidden="!stats.pendingReplyCount" />
            </el-radio-button>
            <el-radio-button label="NEGATIVE">差评</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <div class="review-list" v-loading="listLoading">
        <div v-for="item in reviews" :key="item.id" class="review-item">
          <div class="review-header">
            <div class="user-info">
              <el-avatar :size="44" :src="item.isAnonymous ? null : item.volunteerAvatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <div class="user-detail">
                <div class="user-name">{{ item.isAnonymous ? '匿名同学' : item.volunteerName }}</div>
                <div class="review-meta">
                  <el-tag type="primary" size="small" effect="plain">{{ item.activityTitle }}</el-tag>
                  <span class="review-time">{{ formatDate(item.createTime) }}</span>
                </div>
              </div>
            </div>
            <div class="review-score">
              <el-rate :model-value="item.score" disabled />
            </div>
          </div>

          <div class="review-content">
            {{ item.content }}
          </div>

          <!-- 已回复 -->
          <div class="reply-section" v-if="item.replyContent">
            <div class="reply-box">
              <div class="reply-header">
                <el-icon><ChatLineRound /></el-icon>
                <span>您的回复</span>
                <span class="reply-time">{{ formatDate(item.replyTime) }}</span>
              </div>
              <div class="reply-content">{{ item.replyContent }}</div>
            </div>
          </div>

          <!-- 操作区 -->
          <div class="review-actions" v-else>
            <el-button type="primary" size="small" @click="openReplyDialog(item)">
              <el-icon><ChatLineRound /></el-icon> 回复
            </el-button>
            <el-button
              v-if="item.score <= 2"
              type="warning"
              size="small"
              plain
              @click="handleAppeal(item)"
            >
              <el-icon><Warning /></el-icon> 申诉
            </el-button>
          </div>
        </div>

        <el-empty v-if="!listLoading && reviews.length === 0" description="暂无评价记录" />
      </div>

      <!-- 分页 -->
      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
          @size-change="fetchReviews"
          @current-change="fetchReviews"
        />
      </div>
    </el-card>

    <!-- 回复弹窗 -->
    <el-dialog v-model="replyDialogVisible" title="回复评价" width="500px">
      <div class="reply-dialog-content" v-if="currentReview">
        <div class="original-review">
          <div class="review-user">
            <el-avatar :size="32">{{ currentReview.volunteerName?.[0] }}</el-avatar>
            <span>{{ currentReview.isAnonymous ? '匿名同学' : currentReview.volunteerName }}</span>
            <el-rate :model-value="currentReview.score" disabled size="small" />
          </div>
          <div class="review-text">{{ currentReview.content }}</div>
        </div>

        <el-divider />

        <el-input
          v-model="replyContent"
          type="textarea"
          :rows="4"
          placeholder="请输入您的回复内容..."
          maxlength="300"
          show-word-limit
        />
      </div>

      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply" :loading="replying">
          发送回复
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatDotRound, ChatLineRound, User, Warning } from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import dayjs from 'dayjs'

// 状态
const statsLoading = ref(false)
const listLoading = ref(false)
const replying = ref(false)
const replyDialogVisible = ref(false)
const currentReview = ref<any>(null)
const replyContent = ref('')

// 数据
const stats = reactive({
  averageScore: 0,
  positiveRate: 0,
  totalReviews: 0,
  negativeCount: 0,
  pendingReplyCount: 0
})
const reviews = ref<any[]>([])
const total = ref(0)

// 分页和筛选
const pageNum = ref(1)
const pageSize = ref(10)
const filterType = ref('ALL')

// 获取进度条颜色
const getProgressColor = (rate: number) => {
  if (rate >= 80) return '#67c23a'
  if (rate >= 60) return '#e6a23c'
  return '#f56c6c'
}

// 格式化日期
const formatDate = (date: string) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-'
}

// 获取统计数据
const fetchStats = async () => {
  statsLoading.value = true
  try {
    const res = await request.get('/review/stats')
    if (res.data) {
      Object.assign(stats, res.data)
    }
  } catch (e) {
    console.error('获取统计失败:', e)
  } finally {
    statsLoading.value = false
  }
}

// 获取评价列表
const fetchReviews = async () => {
  listLoading.value = true
  try {
    const res = await request.get('/review/list', {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      type: filterType.value
    })
    reviews.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error('获取评价列表失败:', e)
  } finally {
    listLoading.value = false
  }
}

// 筛选类型变化
const handleFilterChange = () => {
  pageNum.value = 1
  fetchReviews()
}

// 打开回复弹窗
const openReplyDialog = (item: any) => {
  currentReview.value = item
  replyContent.value = ''
  replyDialogVisible.value = true
}

// 提交回复
const submitReply = async () => {
  if (!replyContent.value.trim()) {
    return ElMessage.warning('请输入回复内容')
  }

  replying.value = true
  try {
    await request.post('/review/reply', {
      reviewId: currentReview.value.id,
      content: replyContent.value
    })
    
    ElMessage.success('回复成功')
    replyDialogVisible.value = false
    
    // 局部更新：直接更新该条数据的回复状态
    const index = reviews.value.findIndex(r => r.id === currentReview.value.id)
    if (index !== -1) {
      reviews.value[index].replyContent = replyContent.value
      reviews.value[index].replyTime = new Date().toISOString()
    }
    
    // 更新待回复统计
    fetchStats()
  } catch (e) {
    console.error('回复失败:', e)
    ElMessage.error('回复失败，请重试')
  } finally {
    replying.value = false
  }
}

// 申诉
const handleAppeal = async (item: any) => {
  try {
    await ElMessageBox.confirm(
      '确定要对此评价发起申诉吗？申诉期间评价将被暂时隐藏。',
      '发起申诉',
      { type: 'warning' }
    )
    
    await request.post('/review/appeal', null, { params: { reviewId: item.id } })
    ElMessage.success('申诉已提交，请等待管理员处理')
    fetchReviews()
  } catch (e) {
    // cancel
  }
}

// 初始化加载
onMounted(() => {
  Promise.all([fetchStats(), fetchReviews()])
})
</script>

<style scoped lang="scss">
.org-service-reviews {
  padding: 10px;

  // 统计看板
  .stats-board {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 16px;
    margin-bottom: 20px;

    .stat-card {
      background: white;
      border-radius: 12px;
      padding: 20px;
      text-align: center;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);

      &.main-score {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;

        .score-display {
          .score-value {
            font-size: 48px;
            font-weight: 700;
          }
          .score-unit {
            font-size: 18px;
            opacity: 0.8;
          }
        }

        .stat-label {
          color: rgba(255, 255, 255, 0.9);
        }

        :deep(.el-rate) {
          .el-rate__icon {
            color: rgba(255, 255, 255, 0.3) !important;
          }
          .el-rate__icon.is-active {
            color: #ffd93d !important;
          }
        }
      }

      .stat-content {
        margin-bottom: 8px;

        .stat-number {
          font-size: 36px;
          font-weight: 700;

          &.total { color: var(--el-color-primary); }
          &.pending { color: var(--el-color-warning); }
          &.negative { color: var(--el-color-danger); }
        }
      }

      .stat-label {
        font-size: 13px;
        color: var(--el-text-color-secondary);
      }
    }
  }

  // 列表卡片
  .list-card {
    border-radius: 12px;
    border: none;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .header-left {
        display: flex;
        align-items: center;
        gap: 8px;

        .title {
          font-size: 16px;
          font-weight: 600;
        }
      }
    }
  }

  // 评价列表
  .review-list {
    .review-item {
      padding: 20px;
      border-bottom: 1px solid var(--el-border-color-lighter);

      &:last-child {
        border-bottom: none;
      }

      .review-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        margin-bottom: 12px;

        .user-info {
          display: flex;
          gap: 12px;

          .user-detail {
            .user-name {
              font-weight: 600;
              margin-bottom: 4px;
            }

            .review-meta {
              display: flex;
              align-items: center;
              gap: 8px;

              .review-time {
                font-size: 12px;
                color: var(--el-text-color-secondary);
              }
            }
          }
        }
      }

      .review-content {
        font-size: 14px;
        line-height: 1.6;
        color: var(--el-text-color-regular);
        margin-bottom: 12px;
      }

      .reply-section {
        .reply-box {
          background: var(--el-fill-color-light);
          padding: 12px 16px;
          border-radius: 8px;
          border-left: 3px solid var(--el-color-primary);

          .reply-header {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 13px;
            color: var(--el-text-color-secondary);
            margin-bottom: 8px;

            .reply-time {
              margin-left: auto;
              font-size: 12px;
            }
          }

          .reply-content {
            font-size: 14px;
            color: var(--el-text-color-primary);
          }
        }
      }

      .review-actions {
        display: flex;
        gap: 8px;
      }
    }
  }

  .pagination-wrapper {
    display: flex;
    justify-content: center;
    padding-top: 20px;
    border-top: 1px solid var(--el-border-color-lighter);
  }

  // 回复弹窗
  .reply-dialog-content {
    .original-review {
      background: var(--el-fill-color-light);
      padding: 16px;
      border-radius: 8px;

      .review-user {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 8px;
      }

      .review-text {
        font-size: 14px;
        color: var(--el-text-color-regular);
      }
    }
  }
}
</style>
