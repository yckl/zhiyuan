<template>
  <div class="experience-list">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon class="title-icon"><Notebook /></el-icon>
          心得广场
        </h1>
        <p class="page-subtitle">分享志愿服务心得，传递温暖与感动</p>
      </div>
      <el-button 
        v-if="isLoggedIn" 
        type="primary" 
        size="large"
        class="publish-btn"
        @click="router.push('/experience/create')"
      >
        <el-icon><Edit /></el-icon> 发布心得
      </el-button>
    </div>

    <!-- 心得卡片网格 -->
    <div class="experience-grid" v-loading="loading">
      <el-row :gutter="20">
        <el-col 
          v-for="item in experiences" 
          :key="item.id" 
          :xs="24" 
          :sm="12" 
          :md="8" 
          :lg="6"
        >
          <el-card 
            class="experience-card" 
            shadow="hover" 
            @click="router.push(`/experience/${item.id}`)"
          >
            <!-- 封面图 -->
            <div class="card-cover">
              <el-image 
                :src="item.coverImage || getDefaultCover(item.id)" 
                fit="cover" 
                class="cover-image"
                lazy
              >
                <template #error>
                  <div class="image-placeholder">
                    <el-icon :size="36"><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
            </div>

            <!-- 卡片内容 -->
            <div class="card-body">
              <!-- 标题 -->
              <h3 class="card-title">{{ item.title }}</h3>
              
              <!-- 摘要 -->
              <p class="card-summary">{{ getPlainText(item.content) }}</p>

              <!-- 作者信息 -->
              <div class="author-info">
                <el-avatar :size="28" :src="item.volunteerAvatar" class="author-avatar">
                  <el-icon><User /></el-icon>
                </el-avatar>
                <span class="author-name">{{ item.volunteerName || '匿名用户' }}</span>
              </div>

              <!-- 底部统计 -->
              <div class="card-footer">
                <div class="stats">
                  <span class="stat-item">
                    <el-icon><Star /></el-icon>
                    {{ item.likeCount || 0 }}
                  </span>
                  <span class="stat-item">
                    <el-icon><ChatDotRound /></el-icon>
                    {{ item.commentCount || 0 }}
                  </span>
                </div>
                <span class="publish-time">{{ formatRelativeTime(item.createTime) }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-empty 
        v-if="!loading && experiences.length === 0" 
        description="暂无心得分享，快来发布第一篇吧~"
        :image-size="150"
      />
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.size"
        :total="total"
        :page-sizes="[8, 12, 24, 48]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @size-change="fetchExperiences"
        @current-change="fetchExperiences"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Edit, Star, ChatDotRound, User, Picture, Notebook } from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()

const loading = ref(false)
const experiences = ref<any[]>([])
const total = ref(0)

const queryParams = reactive({
  page: 1,
  size: 12
})

const isLoggedIn = computed(() => !!localStorage.getItem('token'))

// 默认封面图（基于ID生成不同颜色）
const getDefaultCover = (id: number) => {
  const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#9b59b6', '#1abc9c']
  const index = id % colors.length
  return `data:image/svg+xml,${encodeURIComponent(`
    <svg xmlns="http://www.w3.org/2000/svg" width="400" height="200">
      <defs>
        <linearGradient id="grad" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" style="stop-color:${colors[index]};stop-opacity:1" />
          <stop offset="100%" style="stop-color:${colors[(index + 1) % colors.length]};stop-opacity:0.8" />
        </linearGradient>
      </defs>
      <rect width="400" height="200" fill="url(#grad)"/>
      <text x="200" y="110" font-family="Arial" font-size="48" fill="white" text-anchor="middle" opacity="0.5">♥</text>
    </svg>
  `)}`
}

// 提取纯文本
const getPlainText = (html: string) => {
  if (!html) return ''
  const text = html.replace(/<[^>]+>/g, '').replace(/&nbsp;/g, ' ')
  return text.length > 80 ? text.slice(0, 80) + '...' : text
}

// 相对时间
const formatRelativeTime = (date: string) => {
  if (!date) return ''
  return dayjs(date).fromNow()
}

const fetchExperiences = async () => {
  loading.value = true
  try {
    const res = await request.get('/experience/list', queryParams)
    experiences.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('获取心得列表失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchExperiences()
})
</script>

<style lang="scss" scoped>
.experience-list {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24px 0;
    margin-bottom: 20px;
    border-bottom: 1px solid #f0f0f0;

    .header-content {
      .page-title {
        display: flex;
        align-items: center;
        gap: 10px;
        font-size: 28px;
        font-weight: 600;
        margin: 0;
        color: #333;

        .title-icon {
          color: #409eff;
        }
      }

      .page-subtitle {
        margin: 8px 0 0;
        color: #999;
        font-size: 14px;
      }
    }

    .publish-btn {
      height: 44px;
      padding: 0 24px;
      border-radius: 22px;
      font-size: 15px;
    }
  }

  .experience-grid {
    min-height: 400px;
  }

  .experience-card {
    cursor: pointer;
    margin-bottom: 20px;
    border-radius: 12px;
    overflow: hidden;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-8px);
      box-shadow: 0 16px 32px rgba(0, 0, 0, 0.12);

      .card-cover .cover-image {
        transform: scale(1.05);
      }
    }

    :deep(.el-card__body) {
      padding: 0;
    }

    .card-cover {
      height: 160px;
      overflow: hidden;

      .cover-image {
        width: 100%;
        height: 100%;
        transition: transform 0.4s ease;
      }

      .image-placeholder {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        background: linear-gradient(135deg, #e0e5ec 0%, #c9d1dc 100%);
        color: #a8b2bd;
      }
    }

    .card-body {
      padding: 16px;

      .card-title {
        font-size: 16px;
        font-weight: 600;
        margin: 0 0 10px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        color: #333;
      }

      .card-summary {
        font-size: 13px;
        color: #666;
        line-height: 1.6;
        height: 42px;
        overflow: hidden;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        line-clamp: 2;
        -webkit-box-orient: vertical;
        margin: 0 0 12px;
      }

      .author-info {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 12px;

        .author-avatar {
          border: 2px solid #f0f0f0;
        }

        .author-name {
          font-size: 13px;
          color: #666;
          font-weight: 500;
        }
      }

      .card-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding-top: 12px;
        border-top: 1px solid #f5f5f5;

        .stats {
          display: flex;
          gap: 16px;

          .stat-item {
            display: flex;
            align-items: center;
            gap: 4px;
            font-size: 13px;
            color: #999;
            transition: color 0.2s;

            &:hover {
              color: #409eff;
            }
          }
        }

        .publish-time {
          font-size: 12px;
          color: #bbb;
        }
      }
    }
  }

  .pagination-wrapper {
    display: flex;
    justify-content: center;
    padding: 30px 0;
  }
}
</style>
