<template>
  <el-card class="activity-card" shadow="hover" @click="handleClick">
    <div class="card-cover">
      <el-image :src="activity.coverImage || defaultCover" fit="cover" class="cover-image" lazy>
        <template #error>
          <div class="image-placeholder">
            <el-icon :size="40"><Picture /></el-icon>
          </div>
        </template>
      </el-image>
      
      <!-- 状态标签 -->
      <el-tag class="status-tag" :type="getStatusType(activity.status)">{{ activity.statusName }}</el-tag>

      <!-- 推荐角标 (右上角倾斜) -->
      <div v-if="activity.isRecommended" class="ribbon ribbon-top-right">
        <span>🎯 猜你喜欢</span>
      </div>

      <!-- 最近浏览 (左上角) -->
      <div v-if="activity.isRecentlyViewed" class="recent-tag">
        <span>👀 最近看过</span>
      </div>
    </div>
    
    <div class="card-body">
      <div class="card-category">
        <el-tag size="small" effect="plain">{{ activity.categoryName || '未分类' }}</el-tag>
      </div>
      <h3 class="card-title" :title="activity.title">{{ activity.title }}</h3>
      
      <div class="card-info">
        <p><el-icon><Clock /></el-icon> {{ formatDate(activity.startTime) }}</p>
        <p><el-icon><Location /></el-icon> {{ activity.location || '待定' }}</p>
      </div>

      <!-- 报名进度 -->
      <div class="card-progress">
        <div class="progress-text">
          <span>报名进度</span>
          <span>{{ activity.currentParticipants }} / {{ activity.maxParticipants || '不限' }}</span>
        </div>
        <el-progress
          :percentage="getProgress(activity.currentParticipants, activity.maxParticipants)"
          :stroke-width="6"
          :show-text="false"
          :color="getProgressColor(activity.currentParticipants, activity.maxParticipants)"
        />
      </div>
    </div>

    <div class="card-footer">
      <div class="rewards">
        <span><el-icon><Timer /></el-icon> {{ activity.serviceHours || 0 }}h</span>
        <span><el-icon><Medal /></el-icon> {{ activity.pointsReward || 0 }}分</span>
      </div>
      <el-button type="primary" size="small" @click.stop="handleClick">查看详情</el-button>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { Picture, Clock, Location, Timer, Medal } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const props = defineProps({
  activity: {
    type: Object,
    required: true
  }
})

const router = useRouter()
const defaultCover = '/default-cover.jpg'

const handleClick = () => {
  router.push(`/activity/${props.activity.id}`)
}

const formatDate = (date: string) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : ''
}

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

const getProgress = (current: number, max: number) => {
  if (!max || max <= 0) return 0
  return Math.min(Math.round((current / max) * 100), 100)
}

const getProgressColor = (current: number, max: number) => {
  const progress = getProgress(current, max)
  if (progress >= 90) return '#f56c6c'
  if (progress >= 70) return '#e6a23c'
  return '#409eff'
}
</script>

<style lang="scss" scoped>
.activity-card {
  cursor: pointer;
  margin-bottom: 20px;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.12);
  }

  :deep(.el-card__body) {
    padding: 0;
  }

  .card-cover {
    position: relative;
    height: 180px;
    overflow: hidden;

    .cover-image {
      width: 100%;
      height: 100%;
      transition: transform 0.5s;
    }

    &:hover .cover-image {
      transform: scale(1.05);
    }

    .image-placeholder {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      background: var(--el-fill-color-light);
      color: var(--el-text-color-placeholder);
    }

    .status-tag {
      position: absolute;
      bottom: 12px;
      right: 12px;
      z-index: 2;
    }

    /* 推荐角标 */
    .ribbon {
      width: 120px;
      height: 120px;
      overflow: hidden;
      position: absolute;
      top: -10px;
      right: -10px;
      z-index: 1;

      &::before,
      &::after {
        position: absolute;
        z-index: -1;
        content: '';
        display: block;
        border: 5px solid #bd1e2a;
        border-top-color: transparent;
        border-right-color: transparent;
      }

      &::before {
        top: 0;
        left: 0;
      }
      &::after {
        bottom: 0;
        right: 0;
      }

      span {
        position: absolute;
        display: block;
        width: 180px;
        padding: 5px 0;
        background-color: #ff4d4f;
        background-image: linear-gradient(45deg, #ff4d4f 0%, #ff7875 100%);
        box-shadow: 0 5px 10px rgba(0, 0, 0, 0.1);
        color: #fff;
        font-size: 12px;
        font-weight: 700;
        text-shadow: 0 1px 1px rgba(0, 0, 0, 0.2);
        text-transform: uppercase;
        text-align: center;
        left: -20px;
        top: 30px;
        transform: rotate(45deg);
      }
    }

    /* 最近浏览标签 */
    .recent-tag {
      position: absolute;
      top: 12px;
      left: 12px;
      background: rgba(0, 0, 0, 0.4);
      backdrop-filter: blur(4px);
      color: #fff;
      padding: 4px 8px;
      border-radius: 4px;
      font-size: 12px;
      z-index: 2;
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }

  .card-body {
    padding: 16px;

    .card-category {
      margin-bottom: 8px;
    }

    .card-title {
      font-size: 16px;
      font-weight: 600;
      margin: 0 0 12px;
      height: 24px; // 限制高度
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      color: var(--el-text-color-primary);
    }

    .card-info {
      margin-bottom: 12px;

      p {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 13px;
        color: var(--el-text-color-secondary);
        margin: 6px 0;

        .el-icon {
          color: var(--el-color-primary);
        }
      }
    }

    .card-progress {
      .progress-text {
        display: flex;
        justify-content: space-between;
        font-size: 12px;
        color: var(--el-text-color-secondary);
        margin-bottom: 6px;
      }
    }
  }

  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    border-top: 1px solid var(--el-border-color-lighter);
    background: var(--el-fill-color-lighter);

    .rewards {
      display: flex;
      gap: 16px;
      font-size: 13px;
      color: var(--el-text-color-secondary);

      span {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }
  }
}

/* 移动端响应式适配 */
@media (max-width: 768px) {
  .activity-card {
    margin-bottom: 16px;
    
    .card-cover {
      height: 140px;
      
      .ribbon {
        width: 100px;
        height: 100px;
        
        span {
          width: 140px;
          font-size: 10px;
          padding: 3px 0;
          top: 24px;
          left: -18px;
        }
      }
      
      .recent-tag {
        font-size: 10px;
        padding: 3px 6px;
      }
    }
    
    .card-body {
      padding: 12px;
      
      .card-title {
        font-size: 14px;
      }
      
      .card-info p {
        font-size: 12px;
      }
      
      .card-progress .progress-text {
        font-size: 11px;
      }
    }
    
    .card-footer {
      padding: 10px 12px;
      flex-wrap: wrap;
      gap: 8px;
      
      .rewards {
        gap: 12px;
        font-size: 12px;
      }
    }
  }
}

/* 超小屏幕 (iPhone SE 等) */
@media (max-width: 400px) {
  .activity-card {
    .card-cover {
      height: 120px;
      
      .ribbon span {
        width: 120px;
        font-size: 9px;
        top: 20px;
        left: -20px;
      }
    }
    
    .card-body {
      padding: 10px;
      
      .card-title {
        font-size: 13px;
        margin-bottom: 8px;
      }
      
      .card-info p {
        font-size: 11px;
        margin: 4px 0;
      }
    }
    
    .card-footer {
      padding: 8px 10px;
      
      .rewards {
        font-size: 11px;
        gap: 10px;
      }
      
      .el-button {
        font-size: 12px;
        padding: 6px 10px;
      }
    }
  }
}
</style>
