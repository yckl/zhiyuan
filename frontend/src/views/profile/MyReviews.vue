<template>
  <div class="my-reviews">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <h2>我的评价</h2>
          <span class="total">共 {{ total }} 条评价</span>
        </div>
      </template>

      <el-empty v-if="reviews.length === 0" description="暂无评价记录" />

      <el-timeline v-else>
        <el-timeline-item
          v-for="review in reviews"
          :key="review.id"
          :timestamp="formatDate(review.createTime)"
          placement="top"
        >
          <el-card shadow="hover" class="review-card">
            <div class="activity-info">
              <span class="label">评价活动：</span>
              <el-link type="primary" :underline="false" @click="goToActivity(review.activityId)">
                {{ review.activityTitle }}
              </el-link>
            </div>

            <div class="review-main">
              <div class="score-line">
                <el-rate v-model="review.score" disabled show-score text-color="#ff9900" />
                <el-tag v-if="review.isAnonymous" size="small" type="info" class="anon-tag">匿名</el-tag>
              </div>
              <p class="content">{{ review.content }}</p>
            </div>

            <div v-if="review.replyContent" class="reply-box">
              <div class="reply-header">
                <el-icon><ChatDotRound /></el-icon>
                <span>组织者回复：</span>
                <span class="reply-time">{{ formatDate(review.replyTime) }}</span>
              </div>
              <p class="reply-content">{{ review.replyContent }}</p>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>

      <div class="pagination" v-if="total > pageSize">
        <el-pagination
          v-model:current-page="pageNum"
          :page-size="pageSize"
          layout="prev, pager, next"
          :total="total"
          @current-change="fetchReviews"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ChatDotRound } from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import dayjs from 'dayjs'

const router = useRouter()
const reviews = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const fetchReviews = async () => {
  try {
    const res: any = await request.get('/volunteer/review/my', {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    reviews.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('获取评价失败', error)
  }
}

const formatDate = (date: string) => {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const goToActivity = (id: number) => {
  router.push(`/activity/${id}`)
}

onMounted(() => {
  fetchReviews()
})
</script>

<style scoped>
.my-reviews {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}
.page-card {
  min-height: 500px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-header h2 {
  margin: 0;
  font-size: 20px;
}
.total {
  color: #909399;
  font-size: 14px;
}

.review-card {
  border: none;
  background: #fdfdfd;
}

.activity-info {
  margin-bottom: 12px;
  font-size: 15px;
}
.activity-info .label {
  color: #606266;
}

.score-line {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}
.anon-tag {
  margin-left: 10px;
}

.content {
  color: #303133;
  line-height: 1.6;
  margin: 10px 0;
  font-size: 14px;
  white-space: pre-wrap;
}

.reply-box {
  margin-top: 15px;
  padding: 12px 16px;
  background-color: #f4f4f5;
  border-radius: 6px;
  position: relative;
}
.reply-box::before {
  content: '';
  position: absolute;
  top: -10px;
  left: 20px;
  border-width: 5px;
  border-style: solid;
  border-color: transparent transparent #f4f4f5 transparent;
}

.reply-header {
  display: flex;
  align-items: center;
  color: #606266;
  font-size: 13px;
  margin-bottom: 8px;
}
.reply-header .el-icon {
  margin-right: 5px;
  font-size: 16px;
}
.reply-time {
  margin-left: auto;
  color: #909399;
  font-size: 12px;
}

.reply-content {
  color: #555;
  font-size: 14px;
  margin: 0;
  line-height: 1.5;
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}
</style>
