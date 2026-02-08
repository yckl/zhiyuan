<template>
  <div class="home-page">
    <!-- 顶部大屏轮播图 -->
    <div class="carousel-container">
      <el-carousel :interval="5000" height="400px" indicator-position="outside">
        <!-- 管理员配置的Banner轮播图 -->
        <el-carousel-item v-for="item in banners" :key="'banner-' + item.id">
          <div class="carousel-item" @click="item.link && router.push(item.link)">
            <el-image :src="item.image" fit="cover" class="carousel-image">
              <template #error>
                <div class="image-placeholder">
                  <el-icon :size="60"><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="carousel-overlay" v-if="item.title">
              <div class="carousel-text">
                <h2>{{ item.title }}</h2>
              </div>
            </div>
          </div>
        </el-carousel-item>
        <!-- 精选活动轮播 (作为后备) -->
        <el-carousel-item v-for="item in featuredActivities" :key="'activity-' + item.id" v-if="banners.length === 0">
          <div class="carousel-item" @click="router.push(`/activity/${item.id}`)">
            <el-image :src="item.coverImage || '/default-cover.jpg'" fit="cover" class="carousel-image">
              <template #error>
                <div class="image-placeholder">
                  <el-icon :size="60"><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="carousel-overlay">
              <div class="carousel-text">
                <el-tag type="danger" effect="dark" class="featured-tag">精选</el-tag>
                <h2>{{ item.title }}</h2>
                <p>{{ item.summary }}</p>
                <el-button type="primary" size="large">立即参与</el-button>
              </div>
            </div>
          </div>
        </el-carousel-item>
        <!-- 默认欢迎页 -->
        <el-carousel-item v-if="banners.length === 0 && featuredActivities.length === 0">
          <div class="carousel-item welcome-slide">
            <div class="welcome-text">
              <h1>欢迎来到校园志愿者系统 👋</h1>
              <p>传递爱心，奉献社会，让志愿精神温暖每一个人</p>
              <el-button type="primary" size="large" @click="router.push('/activity')">开始探索</el-button>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 快捷入口 -->
    <div class="quick-links">
      <div class="link-item" @click="router.push('/activity')">
        <div class="link-icon" style="background: #409eff"><el-icon><Opportunity /></el-icon></div>
        <span>活动中心</span>
      </div>
      <div class="link-item" @click="router.push('/training')">
        <div class="link-icon" style="background: #67c23a"><el-icon><Reading /></el-icon></div>
        <span>培训学院</span>
      </div>
      <div class="link-item" @click="router.push('/mall')">
        <div class="link-icon" style="background: #e6a23c"><el-icon><Shop /></el-icon></div>
        <span>积分商城</span>
      </div>
      <div class="link-item" @click="router.push('/profile/history')">
        <div class="link-icon" style="background: #f56c6c"><el-icon><Histogram /></el-icon></div>
        <span>志愿记录</span>
      </div>
    </div>

    <!-- 热门活动板块 -->
    <div class="section-container">
      <div class="section-header">
        <div class="title-area">
          <span class="main-title">🔥 热门活动</span>
          <span class="sub-title">实时更新，只为发现更好的你</span>
        </div>
        <el-button type="primary" link @click="router.push('/activity')">
          查看更多 <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>

      <el-row :gutter="24" v-loading="loadingActivities">
        <el-col v-for="item in activities" :key="item.id" :xs="24" :sm="12" :lg="8">
          <ActivityCard :activity="item" />
        </el-col>
      </el-row>
      <el-empty v-if="!loadingActivities && activities.length === 0" description="暂无热门活动" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { request } from '@/utils/request'
import {
  Reading,
  Shop,
  Histogram,
  Opportunity,
  ArrowRight,
  Picture
} from '@element-plus/icons-vue'
import ActivityCard from '@/components/ActivityCard.vue'

const router = useRouter()

const banners = ref<any[]>([])
const featuredActivities = ref<any[]>([])
const activities = ref<any[]>([])
const loadingActivities = ref(false)

// 获取管理员配置的轮播图
const fetchBanners = async () => {
  try {
    const res = await request.get('/banner/list')
    banners.value = res.data || []
  } catch (error) {
    console.error('获取轮播图失败:', error)
  }
}

const fetchFeaturedActivities = async () => {
  try {
    const res = await request.get('/activity/list', { page: 1, size: 5, isFeatured: 1, status: 2 })
    featuredActivities.value = res.data?.records || []
  } catch (error) {
    console.error('获取精选活动失败:', error)
  }
}

const fetchActivities = async () => {
  loadingActivities.value = true
  try {
    // 获取热门活动（目前以浏览量排序或者最新发布的报名中活动）
    const res = await request.get('/activity/list', { page: 1, size: 6, status: 2, orderBy: 'viewCount' })
    activities.value = res.data?.records || []
  } catch (error) {
    console.error('获取活动列表失败:', error)
  } finally {
    loadingActivities.value = false
  }
}

onMounted(() => {
  fetchBanners()
  fetchFeaturedActivities()
  fetchActivities()
})
</script>

<style lang="scss" scoped>
.home-page {
  padding-bottom: 40px;

  .carousel-container {
    margin-bottom: 30px;
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);

    .carousel-item {
      position: relative;
      height: 100%;
      cursor: pointer;

      .carousel-image {
        width: 100%;
        height: 100%;
      }

      .carousel-overlay {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        height: 100%;
        background: linear-gradient(to top, rgba(0, 0, 0, 0.8) 0%, rgba(0, 0, 0, 0.2) 60%, transparent 100%);
        display: flex;
        align-items: flex-end;
        padding: 40px;

        .carousel-text {
          color: #fff;
          max-width: 800px;

          .featured-tag {
            margin-bottom: 12px;
          }

          h2 {
            font-size: 32px;
            margin: 0 0 12px;
            font-weight: 700;
          }

          p {
            font-size: 18px;
            margin-bottom: 24px;
            opacity: 0.9;
            overflow: hidden;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            line-clamp: 2;
            -webkit-box-orient: vertical;
          }
        }
      }

      &.welcome-slide {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        display: flex;
        align-items: center;
        justify-content: center;
        text-align: center;
        color: #fff;

        .welcome-text {
          padding: 0 40px;
          h1 { font-size: 36px; margin-bottom: 16px; }
          p { font-size: 20px; opacity: 0.9; margin-bottom: 30px; }
        }
      }
    }
  }

  .quick-links {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin-bottom: 40px;

    .link-item {
      background: #fff;
      padding: 24px;
      border-radius: 16px;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 12px;
      cursor: pointer;
      transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);

      &:hover {
        transform: translateY(-8px);
        box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
        
        .link-icon {
          transform: scale(1.1);
        }
      }

      .link-icon {
        width: 64px;
        height: 64px;
        border-radius: 20px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        font-size: 28px;
        transition: transform 0.3s;
      }

      span {
        font-size: 16px;
        font-weight: 600;
        color: #333;
      }
    }
  }

  .section-container {
    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-end;
      margin-bottom: 24px;
      padding: 0 4px;

      .title-area {
        .main-title {
          display: block;
          font-size: 24px;
          font-weight: 700;
          color: #333;
          margin-bottom: 4px;
        }
        .sub-title {
          font-size: 14px;
          color: #999;
        }
      }
    }

    .activity-card {
      margin-bottom: 24px;
      border-radius: 16px;
      overflow: hidden;
      border: none;
      transition: all 0.3s;

      .activity-cover-wrapper {
        position: relative;
        height: 200px;
        overflow: hidden;

        .activity-cover {
          width: 100%;
          height: 100%;
          transition: transform 0.5s;
        }

        .activity-status {
          position: absolute;
          top: 12px;
          right: 12px;
        }
      }

      &:hover {
        .activity-cover {
          transform: scale(1.1);
        }
      }

      .activity-content {
        padding: 20px;

        .category-info {
          margin-bottom: 12px;
        }

        .activity-title {
          font-size: 18px;
          font-weight: 600;
          color: #333;
          margin: 0 0 16px;
          line-height: 1.4;
          height: 50px;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }

        .activity-footer {
          display: flex;
          justify-content: space-between;
          border-top: 1px solid #f0f0f0;
          padding-top: 16px;

          .meta-item {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 13px;
            color: #666;

            .el-icon {
              color: #409eff;
            }
          }
        }
      }
    }
  }

  .image-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f7fa;
    color: #c0c4cc;
  }
}

/* 移动端响应式适配 */
@media (max-width: 768px) {
  .home-page {
    .carousel-container {
      :deep(.el-carousel) {
        height: 200px !important;
      }
      
      .carousel-item {
        .carousel-image {
          height: 200px;
        }
      }
    }
    
    /* 快捷入口 2x2 田字格布局 */
    .quick-links {
      grid-template-columns: repeat(2, 1fr);
      gap: 12px;
      margin-bottom: 24px;
      padding: 0 8px;
      
      .link-item {
        padding: 16px 12px;
        border-radius: 12px;
        
        .link-icon {
          width: 48px;
          height: 48px;
          border-radius: 14px;
          font-size: 22px;
        }
        
        span {
          font-size: 13px;
        }
      }
    }
    
    .section-container {
      .section-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 12px;
        margin-bottom: 16px;
        
        .title-area {
          .main-title {
            font-size: 18px;
          }
          .sub-title {
            font-size: 12px;
          }
        }
      }
      
      .el-row {
        margin: 0 -8px !important;
        
        .el-col {
          padding: 0 8px !important;
        }
      }
    }
  }
}

/* 超小屏幕 (iPhone SE 等) */
@media (max-width: 400px) {
  .home-page {
    .carousel-container {
      :deep(.el-carousel) {
        height: 160px !important;
      }
    }
    
    .section-container {
      padding: 0 12px;
      
      .section-header {
        .title-area {
          .main-title {
            font-size: 16px;
          }
          .sub-title {
            font-size: 11px;
          }
        }
      }
    }
  }
}
</style>
