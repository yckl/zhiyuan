<template>
  <div class="home-page">
    <!-- 顶部大屏轮播图 -->
    <div class="carousel-container">
      <el-carousel :interval="5000" height="400px" indicator-position="outside">
        <el-carousel-item v-for="item in featuredActivities" :key="item.id">
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
        <el-carousel-item v-if="featuredActivities.length === 0">
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
          <el-card class="activity-card" :body-style="{ padding: '0px' }" shadow="hover" @click="router.push(`/activity/${item.id}`)">
            <div class="activity-cover-wrapper">
              <el-image :src="item.coverImage || '/default-cover.jpg'" class="activity-cover" fit="cover" lazy />
              <div class="activity-status">
                <el-tag :type="getStatusType(item.status)" size="small" effect="dark">{{ item.statusName || '报名中' }}</el-tag>
              </div>
            </div>
            <div class="activity-content">
              <div class="category-info">
                <el-tag size="small" type="info" effect="plain">{{ item.categoryName || '未分类' }}</el-tag>
              </div>
              <h3 class="activity-title">{{ item.title }}</h3>
              <div class="activity-footer">
                <div class="meta-item">
                  <el-icon><Location /></el-icon>
                  <span>{{ item.location }}</span>
                </div>
                <div class="meta-item">
                  <el-icon><Medal /></el-icon>
                  <span>{{ item.pointsReward }}积分</span>
                </div>
              </div>
            </div>
          </el-card>
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
  Clock,
  Location,
  ArrowRight,
  Picture,
  Medal
} from '@element-plus/icons-vue'

const router = useRouter()

const featuredActivities = ref<any[]>([])
const activities = ref<any[]>([])
const loadingActivities = ref(false)

const getStatusType = (status: number) => {
  const types: Record<number, string> = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: 'primary',
    4: 'info',
    5: 'danger'
  }
  return types[status] || 'info'
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
</style>
