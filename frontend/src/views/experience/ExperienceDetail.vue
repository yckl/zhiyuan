<template>
  <div class="experience-detail-page">
    <!-- 返回按钮 -->
    <div class="back-bar">
      <button class="pill-back-btn" @click="$router.push('/experience')">
        返回心得广场
        <el-icon><ArrowRight /></el-icon>
      </button>
    </div>

    <!-- 骨架屏 -->
    <div v-if="loading" class="detail-skeleton">
      <el-skeleton animated>
        <template #template>
          <el-skeleton-item variant="image" style="height: 240px; border-radius: 12px" />
          <div style="padding: 20px">
            <el-skeleton-item variant="h1" style="width: 60%; height: 28px" />
            <div style="display:flex;gap:12px;margin-top:16px;align-items:center">
              <el-skeleton-item variant="circle" style="width:40px;height:40px" />
              <el-skeleton-item variant="text" style="width:120px" />
            </div>
            <el-skeleton-item variant="text" style="margin-top:20px" />
            <el-skeleton-item variant="text" style="margin-top:8px" />
            <el-skeleton-item variant="text" style="width: 80%; margin-top:8px" />
            <el-skeleton-item variant="text" style="margin-top:8px" />
            <el-skeleton-item variant="text" style="width: 60%; margin-top:8px" />
          </div>
        </template>
      </el-skeleton>
    </div>

    <div v-else class="detail-content">
      <el-row :gutter="24">
        <el-col :xs="24" :lg="16">
          <el-card class="main-card" shadow="never">
            <!-- 封面图 -->
            <div class="cover-section section-anim stagger-1" v-if="experience.coverImage">
              <el-image :src="experience.coverImage" fit="cover">
  <template #error>
    <img :src="'/default-cover.jpg'" style="width:100%;height:100%;object-fit:cover"/>
  </template>
</el-image>
              <div class="cover-gradient"></div>
            </div>

            <!-- 标题 -->
            <h1 class="exp-title section-anim stagger-2">{{ experience.title }}</h1>

            <!-- 元信息 -->
            <div class="exp-meta section-anim stagger-3">
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
            <div class="activity-tag section-anim stagger-3" v-if="experience.activityTitle">
              <el-tag type="info">
                <el-icon><Flag /></el-icon>
                {{ experience.activityTitle }}
              </el-tag>
            </div>

            <el-divider />

            <!-- 正文内容 -->
            <div class="exp-content section-anim stagger-4" v-html="experience.content || defaultContent"></div>

            <el-divider />

            <!-- 底部操作 -->
            <div class="exp-actions section-anim stagger-5">
              <button
                class="action-btn like-btn"
                :class="{ liked: hasLiked }"
                :disabled="liking"
                @click="handleLike"
              >
                <el-icon><Star /></el-icon>
                {{ hasLiked ? '已点赞' : '点赞' }} ({{ experience.likeCount || 0 }})
              </button>
              <button class="action-btn share-btn" @click="handleShare">
                <el-icon><Share /></el-icon>
                分享
              </button>
            </div>

            <!-- 评论区 -->
            <div class="section-anim stagger-6">
              <comment-section
                target-type="EXPERIENCE"
                :target-id="expId"
                :count="experience.commentCount || 0"
                @refresh="fetchExperience"
              />
            </div>
          </el-card>
        </el-col>

        <!-- 右侧：更多心得 -->
        <el-col :xs="24" :lg="8">
          <el-card class="related-card section-anim stagger-5" shadow="never">
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
                <div class="related-thumb" v-if="item.coverImage">
                  <el-image :src="item.coverImage" fit="cover" style="width:48px;height:48px;border-radius:8px">
  <template #error>
    <img :src="'/default-cover.jpg'" style="width:100%;height:100%;object-fit:cover"/>
  </template>
</el-image>
                </div>
                <div class="related-text">
                  <h4>{{ item.title }}</h4>
                  <div class="related-meta">
                    <span>{{ item.authorName || '匿名' }}</span>
                    <span><el-icon><Star /></el-icon> {{ item.likeCount || 0 }}</span>
                  </div>
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
import { ArrowRight, View, Star, Flag, Share } from '@element-plus/icons-vue'
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
<p>未来我会继续投身志愿服务，用自己的行动传递温暖 and 正能量。</p>
`

// 解析图片JSON
const getCover = (imagesJson: string) => {
  try {
    const imgs = JSON.parse(imagesJson || '[]')
    return imgs.length > 0 ? imgs[0] : null
  } catch (e) {
    return null
  }
}

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
      const d = res.data
      // 映射封面
      if (!d.coverImage && d.images) {
        d.coverImage = getCover(d.images)
      }
      experience.value = d
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
// ================================================================
// Section stagger animations
// ================================================================
@keyframes sectionFadeUp {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}

.section-anim { animation: sectionFadeUp 0.4s ease-out both; }
.stagger-1 { animation-delay: 0.05s; }
.stagger-2 { animation-delay: 0.12s; }
.stagger-3 { animation-delay: 0.18s; }
.stagger-4 { animation-delay: 0.25s; }
.stagger-5 { animation-delay: 0.35s; }
.stagger-6 { animation-delay: 0.45s; }

.experience-detail-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.back-bar {
  margin-bottom: 20px;
  display: flex;
}

.pill-back-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 24px;
  border: 1.5px solid #00BFA6;
  background: #fff;
  color: #00BFA6;
  border-radius: 50px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 191, 166, 0.08);

  .el-icon {
    font-size: 16px;
    transition: transform 0.3s;
  }

  &:hover {
    background: rgba(0, 191, 166, 0.04);
    box-shadow: 0 4px 12px rgba(0, 191, 166, 0.15);
    
    .el-icon {
      transform: translateX(3px);
    }
  }

  &:active {
    transform: scale(0.96);
  }
}

.detail-skeleton {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  max-width: 900px;
}

.main-card {
  border-radius: 12px;
}

.cover-section {
  margin: -20px -20px 20px;
  height: 300px;
  overflow: hidden;
  position: relative;

  :deep(.el-image) {
    width: 100%;
    height: 100%;
  }

  .cover-gradient {
    position: absolute;
    inset: 0;
    background: linear-gradient(to bottom, transparent 50%, rgba(0, 191, 166, 0.3) 100%);
    pointer-events: none;
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

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  border: none;
  padding: 10px 24px;
  border-radius: 24px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s;

  .el-icon { font-size: 16px; }

  &:active { transform: scale(0.95); }
}

.like-btn {
  background: #f0f0f0;
  color: #666;

  &.liked {
    background: linear-gradient(135deg, #00BFA6, #43e97b);
    color: #fff;
    box-shadow: 0 4px 12px rgba(0, 191, 166, 0.3);
  }

  &:hover:not(.liked) { background: #e0e0e0; }
}

.share-btn {
  background: #f0f0f0;
  color: #666;

  &:hover { background: #e0e0e0; }
}

.related-card {
  border-radius: 12px;
  position: sticky;
  top: 20px;

  .related-list {
    .related-item {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 10px 0;
      border-bottom: 1px solid #f0f0f0;
      cursor: pointer;
      transition: all 0.2s;

      &:last-child { border-bottom: none; }
      &:hover { color: var(--primary-color, #00BFA6); }

      .related-thumb {
        flex-shrink: 0;
      }

      .related-text {
        flex: 1;
        min-width: 0;

        h4 {
          margin: 0 0 4px;
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

          span {
            display: flex;
            align-items: center;
            gap: 2px;
          }
        }
      }
    }
  }
}

/* 暗黑模式适配 */
html.dark .experience-detail-page {
  background-color: #0f172a !important;
  
  .main-card,
  .related-card {
    background-color: #1e293b !important;
    border-color: #334155 !important;
  }

  .pill-back-btn {
    background-color: transparent !important;
    border-color: #00BFA6 !important;
    color: #00BFA6 !important;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2) !important;

    &:hover {
      background-color: rgba(0, 191, 166, 0.1) !important;
    }
  }
  
  .exp-title {
    color: #f1f5f9 !important;
  }
  
  .author-name {
    color: #e2e8f0 !important;
  }
  
  .publish-time,
  .stat-item {
    color: #94a3b8 !important;
  }
  
  .exp-content {
    color: #cbd5e1 !important;
  }
  
  .related-item {
    border-bottom-color: #334155 !important;
    
    h4 {
      color: #f1f5f9 !important;
    }
  }
}
</style>
