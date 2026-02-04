<template>
  <div class="experience-detail-page">
    <!-- 返回按钮 -->
    <div class="back-bar">
      <el-button type="primary" text @click="$router.push('/experience')">
        <el-icon><ArrowLeft /></el-icon>
        返回心得广场
      </el-button>
    </div>

    <div class="detail-content" v-loading="loading">
      <el-row :gutter="24">
        <el-col :xs="24" :lg="16">
          <el-card class="main-card" shadow="never">
            <!-- 封面图 -->
            <div class="cover-section" v-if="experience.coverImage">
              <el-image :src="experience.coverImage" fit="cover" />
            </div>

            <!-- 标题 -->
            <h1 class="exp-title">{{ experience.title }}</h1>

            <!-- 元信息 -->
            <div class="exp-meta">
              <div class="author-info">
                <el-avatar :size="40" :src="getAvatarUrl(experience.authorAvatar)">
                  {{ experience.authorName?.charAt(0) || '匿' }}
                </el-avatar>
                <div class="author-text">
                  <span class="author-name">{{ experience.authorName || experience.volunteerName || '匿名用户' }}</span>
                  <span class="publish-time">{{ formatDate(experience.createTime) }}</span>
                </div>
              </div>
              <div class="stats">
                <span class="stat-item">
                  <el-icon><View /></el-icon>
                  {{ experience.viewCount || 0 }} 阅读
                </span>
                <span class="stat-item">
                  <el-icon><Star /></el-icon>
                  {{ experience.likeCount || 0 }} 点赞
                </span>
              </div>
            </div>

            <!-- 关联活动 -->
            <div class="activity-tag" v-if="experience.activityTitle">
              <el-tag type="info">
                <el-icon><Flag /></el-icon>
                {{ experience.activityTitle }}
              </el-tag>
            </div>

            <el-divider />

            <!-- 正文内容 -->
            <div class="exp-content" v-html="experience.content || defaultContent"></div>

            <el-divider />

            <!-- 底部操作 -->
            <div class="exp-actions">
              <el-button
                :type="hasLiked ? 'danger' : 'default'"
                size="large"
                :loading="liking"
                @click="handleLike"
              >
                <el-icon><Star /></el-icon>
                {{ hasLiked ? '已点赞' : '点赞' }} ({{ experience.likeCount || 0 }})
              </el-button>
              <el-button size="large" @click="handleShare">
                <el-icon><Share /></el-icon>
                分享
              </el-button>
            </div>

            <!-- 评论区 -->
            <comment-section
              target-type="EXPERIENCE"
              :target-id="expId"
              :count="experience.commentCount || 0"
              @refresh="fetchExperience"
            />
          </el-card>
        </el-col>

        <!-- 右侧：更多心得 -->
        <el-col :xs="24" :lg="8">
          <el-card class="related-card" shadow="never">
            <template #header>
              <span>📝 更多精彩心得</span>
            </template>
            <div class="related-list">
              <div
                v-for="item in relatedList"
                :key="item.id"
                class="related-item"
                @click="goToDetail(item.id)"
              >
                <h4>{{ item.title }}</h4>
                <div class="related-meta">
                  <span>{{ item.authorName || '匿名' }}</span>
                  <span>{{ item.likeCount || 0 }} 赞</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, View, Star, Flag, Share } from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import CommentSection from '@/components/CommentSection.vue'

interface Experience {
  id: number
  title: string
  content?: string
  coverImage?: string
  authorName?: string
  authorAvatar?: string
  volunteerName?: string
  activityTitle?: string
  viewCount?: number
  likeCount?: number
  commentCount?: number
  createTime: string
}

const route = useRoute()
const router = useRouter()
const expId = Number(route.params.id)

const loading = ref(false)
const liking = ref(false)
const hasLiked = ref(false)
const experience = ref<Experience>({ id: 0, title: '', createTime: '' })
const relatedList = ref<Experience[]>([])

const defaultContent = `
<p>这次志愿服务活动让我收获颇丰。</p>
<p>通过参与，我不仅帮助了他人，也让自己得到了成长。看到服务对象脸上的笑容，我深刻体会到志愿服务的意义所在。</p>
<p>未来我会继续投身志愿服务，用自己的行动传递温暖和正能量。</p>
`

const getAvatarUrl = (url?: string) => {
  if (!url) return ''
  return url.includes('?') ? url : `${url}?t=${Date.now()}`
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const fetchExperience = async () => {
  loading.value = true
  try {
    const res = await request.get(`/experience/${expId}`)
    if (res.code === 200 && res.data) {
      experience.value = res.data
    }
  } catch (error) {
    console.error('获取心得详情失败:', error)
    // 模拟数据
    experience.value = {
      id: expId,
      title: '我的志愿服务心得',
      authorName: '志愿者小明',
      authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=1',
      activityTitle: '社区义诊活动',
      viewCount: 256,
      likeCount: 42,
      createTime: '2026-01-15'
    }
  } finally {
    loading.value = false
  }
}

const fetchRelated = async () => {
  try {
    const res = await request.get('/experience/public/list', {
      pageNum: 1, pageSize: 5
    })
    if (res.code === 200) {
      const list = res.data?.records || res.data || []
      relatedList.value = list.filter((e: Experience) => e.id !== expId).slice(0, 4)
    }
  } catch (error) {
    console.error('获取相关心得失败:', error)
  }
}

const handleLike = async () => {
  liking.value = true
  try {
    const res = await request.post(`/experience/${expId}/like`)
    if (res.code === 200) {
      hasLiked.value = res.data === true
      // 根据返回结果更新点赞数
      if (hasLiked.value) {
        experience.value.likeCount = (experience.value.likeCount || 0) + 1
        ElMessage.success('点赞成功！')
      } else {
        experience.value.likeCount = Math.max(0, (experience.value.likeCount || 1) - 1)
        ElMessage.success('已取消点赞')
      }
    }
  } catch (error: any) {
    ElMessage.error('操作失败，请登录后重试')
  } finally {
    liking.value = false
  }
}

const handleShare = () => {
  const url = window.location.href
  navigator.clipboard?.writeText(url)
  ElMessage.success('链接已复制到剪贴板')
}

const goToDetail = (id: number) => {
  router.push(`/experience/${id}`)
  // 重新加载数据
  setTimeout(() => {
    fetchExperience()
    fetchRelated()
  }, 100)
}

const checkLikeStatus = async () => {
  try {
    const res = await request.get(`/experience/${expId}/like/check`)
    if (res.code === 200) {
      hasLiked.value = res.data === true
    }
  } catch (error) {
    // 忽略错误，默认未点赞
  }
}

onMounted(() => {
  fetchExperience()
  fetchRelated()
  checkLikeStatus()
})
</script>

<style lang="scss" scoped>
.experience-detail-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.back-bar {
  margin-bottom: 16px;
}

.main-card {
  border-radius: 12px;
}

.cover-section {
  margin: -20px -20px 20px;
  height: 300px;
  overflow: hidden;

  :deep(.el-image) {
    width: 100%;
    height: 100%;
  }
}

.exp-title {
  font-size: 24px;
  font-weight: bold;
  margin: 0 0 20px;
  color: #333;
}

.exp-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  .author-info {
    display: flex;
    align-items: center;
    gap: 12px;

    .author-text {
      display: flex;
      flex-direction: column;

      .author-name {
        font-weight: 500;
        color: #333;
      }

      .publish-time {
        font-size: 12px;
        color: #999;
      }
    }
  }

  .stats {
    display: flex;
    gap: 16px;

    .stat-item {
      display: flex;
      align-items: center;
      gap: 4px;
      color: #666;
      font-size: 14px;
    }
  }
}

.activity-tag {
  margin-bottom: 16px;

  :deep(.el-tag) {
    display: inline-flex;
    align-items: center;
    gap: 4px;
  }
}

.exp-content {
  font-size: 16px;
  line-height: 1.8;
  color: #333;

  :deep(p) {
    margin: 16px 0;
    text-indent: 2em;
  }

  :deep(img) {
    max-width: 100%;
    border-radius: 8px;
    margin: 16px 0;
  }
}

.exp-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.related-card {
  border-radius: 12px;
  position: sticky;
  top: 20px;

  .related-list {
    .related-item {
      padding: 12px 0;
      border-bottom: 1px solid #eee;
      cursor: pointer;
      transition: all 0.2s;

      &:last-child {
        border-bottom: none;
      }

      &:hover {
        color: #409eff;
      }

      h4 {
        margin: 0 0 6px;
        font-size: 14px;
        font-weight: 500;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .related-meta {
        display: flex;
        justify-content: space-between;
        font-size: 12px;
        color: #999;
      }
    }
  }
}
</style>
