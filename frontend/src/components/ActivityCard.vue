<template>
  <div
    class="activity-card"
    :class="{ 'card-mobile': isMobile, 'card-pc': !isMobile }"
    @click="handleClick"
  >
    <!-- ==================== 移动端：左图右文 ==================== -->
    <template v-if="isMobile">
      <div class="mobile-cover">
        <el-image :src="getImageUrl(activity.coverImage || activity.image, defaultCover)" fit="cover" class="mobile-img" lazy>
          <template #error>
            <div class="img-fallback"><el-icon :size="24"><Picture /></el-icon></div>
          </template>
        </el-image>
        <div class="status-glass-pill" :class="`status-${activity.status}`">
          {{ activity.statusName }}
        </div>
      </div>

      <div class="mobile-body">
        <h3 class="mobile-title">{{ activity.title }}</h3>
        <div class="mobile-meta">
          <span><el-icon><Clock /></el-icon> {{ formatDate(activity.startTime) }}</span>
          <span><el-icon><Location /></el-icon> {{ activity.location || '待定' }}</span>
        </div>
        <div class="mobile-bottom">
          <el-tag size="small" round>{{ activity.categoryName || '未分类' }}</el-tag>
          <div class="mobile-rewards">
            <span><el-icon><Timer /></el-icon>{{ activity.serviceHours || 0 }}h</span>
            <span><el-icon><Medal /></el-icon>{{ activity.pointsReward || 0 }}</span>
          </div>
        </div>
      </div>
    </template>

    <!-- ==================== PC端：上图下文 ==================== -->
    <template v-else>
      <div class="pc-cover">
        <el-image :src="getImageUrl(activity.coverImage || activity.image, defaultCover)" fit="cover" class="pc-img" lazy>
          <template #error>
            <div class="img-fallback"><el-icon :size="40"><Picture /></el-icon></div>
          </template>
        </el-image>
        <el-tag class="status-float" :type="getStatusType(activity.status)" size="small" effect="dark">
          {{ activity.statusName }}
        </el-tag>
        <!-- 推荐角标 -->
        <div v-if="activity.isRecommended" class="recommend-badge">🎯 猜你喜欢</div>
        <!-- 最近浏览 -->
        <div v-if="activity.isRecentlyViewed" class="recent-badge">👀 最近看过</div>
      </div>

      <div class="pc-body">
        <div class="pc-category">
          <el-tag size="small" effect="plain">{{ activity.categoryName || '未分类' }}</el-tag>
        </div>
        <h3 class="pc-title">{{ activity.title }}</h3>
        <div class="pc-info">
          <p><el-icon><Clock /></el-icon> {{ formatDate(activity.startTime) }}</p>
          <p><el-icon><Location /></el-icon> {{ activity.location || '待定' }}</p>
        </div>
        <div class="pc-progress">
          <div class="progress-text">
            <span>报名进度</span>
            <span>{{ activity.currentParticipants || 0 }} / {{ activity.maxParticipants || '不限' }}</span>
          </div>
          <el-progress
            :percentage="getProgress(activity.currentParticipants, activity.maxParticipants)"
            :stroke-width="5"
            :show-text="false"
            :color="getProgressColor(activity.currentParticipants, activity.maxParticipants)"
          />
        </div>
      </div>

      <div class="pc-footer">
        <div class="rewards">
          <span><el-icon><Timer /></el-icon> {{ activity.serviceHours || 0 }}h</span>
          <span><el-icon><Medal /></el-icon> {{ activity.pointsReward || 0 }}</span>
        </div>
        <button class="register-btn" @click.stop="handleClick">
          {{ activity.status === 2 ? '立即报名' : '查看详情' }}
        </button>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Picture, Clock, Location, Timer, Medal } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { getImageUrl } from '@/utils/image'

const props = defineProps({
  activity: { type: Object, required: true }
})

const router = useRouter()
const defaultCover = '/default-cover.jpg'

// 简化响应式检测（CSS 也做了，这里主要控制模板分支）
const isMobile = computed(() => window.innerWidth < 768)

const handleClick = () => router.push(`/activity/${props.activity.id}`)

const formatDate = (date: string) => date ? dayjs(date).format('MM-DD HH:mm') : ''

const getStatusType = (status: number) => {
  const m: Record<number, string> = { 0: 'info', 1: 'warning', 2: 'success', 3: 'primary', 4: 'info', 5: 'danger' }
  return m[status] || 'info'
}

const getProgress = (current: number, max: number) => {
  if (!max || max <= 0) return 0
  return Math.min(Math.round((current / max) * 100), 100)
}

const getProgressColor = (current: number, max: number) => {
  const p = getProgress(current, max)
  if (p >= 90) return '#F56C6C'
  if (p >= 70) return '#E6A23C'
  return '#00BFA6'
}
</script>

<style lang="scss" scoped>
// ================================================================
// 公共
// ================================================================
.activity-card {
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
}

.img-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F5F5F5;
  color: #ccc;
}

.status-glass-pill {
  position: absolute;
  top: 6px;
  right: 6px;
  z-index: 2;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 10px;
  font-weight: 600;
  color: #fff;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border: 0.5px solid rgba(255, 255, 255, 0.3);

  &.status-0, &.status-4 { background: rgba(100, 116, 139, 0.65); } // 草稿 / 已结束
  &.status-1 { background: rgba(217, 119, 6, 0.65); } // 待审核
  &.status-2 { background: rgba(0, 191, 166, 0.75); } // 招募中(Aurora Teal)
  &.status-3 { background: rgba(37, 99, 235, 0.65); } // 进行中
  &.status-5 { background: rgba(220, 38, 38, 0.65); } // 已取消
}

// ================================================================
// 移动端 -- 通栏左图右文
// ================================================================
.card-mobile {
  display: flex;
  align-items: stretch;
  background: #fff;
  padding: 12px;
  margin-bottom: 12px;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  gap: 12px;
  transition: transform 0.2s, box-shadow 0.2s;

  &:active {
    transform: scale(0.98);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  }
}

.mobile-cover {
  position: relative;
  width: 120px;
  height: 90px;
  flex-shrink: 0;
  border-radius: 12px;
  overflow: hidden;

  .mobile-img {
    width: 100%;
    height: 100%;
  }
}

.mobile-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;

  .mobile-title {
    font-size: 15px;
    font-weight: 600;
    color: #1C1C1E;
    margin: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    line-height: 1.3;
  }

  .mobile-meta {
    display: flex;
    flex-direction: column;
    gap: 2px;
    margin: 4px 0;

    span {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 12px;
      color: #999;

      .el-icon { font-size: 12px; color: var(--primary-color); }
    }
  }

  .mobile-bottom {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .mobile-rewards {
    display: flex;
    gap: 8px;

    span {
      display: flex;
      align-items: center;
      gap: 2px;
      font-size: 11px;
      color: #999;

      .el-icon { font-size: 11px; }
    }
  }
}

// ================================================================
// PC -- 圆角卡片上图下文
// ================================================================
.card-pc {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);

  &:hover {
    transform: translateY(-6px);
    box-shadow: 0 12px 28px rgba(0, 0, 0, 0.12);

    .pc-img { transform: scale(1.05); }
  }
}

.pc-cover {
  position: relative;
  height: 180px;
  overflow: hidden;

  // 渐变蒙层
  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(to bottom, transparent 50%, rgba(0, 0, 0, 0.25) 100%);
    pointer-events: none;
    z-index: 1;
  }

  .pc-img {
    width: 100%;
    height: 100%;
    transition: transform 0.5s;
  }

  .recommend-badge {
    position: absolute;
    top: 0;
    right: 0;
    background: linear-gradient(135deg, #FF4D4F, #FF7875);
    color: #fff;
    font-size: 11px;
    font-weight: 600;
    padding: 4px 12px 4px 8px;
    border-radius: 0 0 0 12px;
    z-index: 2;
  }

  .recent-badge {
    position: absolute;
    top: 10px;
    left: 10px;
    background: rgba(0, 0, 0, 0.55);
    backdrop-filter: blur(4px);
    color: #fff;
    padding: 3px 8px;
    border-radius: 4px;
    font-size: 11px;
    z-index: 2;
  }
}

.pc-body {
  padding: 14px 16px 8px;

  .pc-category { margin-bottom: 6px; }

  .pc-title {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin: 0 0 10px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .pc-info {
    margin-bottom: 10px;

    p {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      color: #999;
      margin: 4px 0;

      .el-icon { color: var(--primary-color); font-size: 14px; }
    }
  }

  .pc-progress {
    .progress-text {
      display: flex;
      justify-content: space-between;
      font-size: 12px;
      color: #999;
      margin-bottom: 4px;
    }
  }
}

.pc-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px 14px;

  .rewards {
    display: flex;
    gap: 14px;
    font-size: 12px;
    color: #999;

    span {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }
}

.register-btn {
  border: none;
  background: linear-gradient(135deg, var(--primary-color), #43e97b);
  color: #fff;
  padding: 6px 18px;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s;
  white-space: nowrap;

  &:hover {
    transform: scale(1.06);
    box-shadow: 0 4px 14px rgba(0, 191, 166, 0.35);
  }

  &:active { transform: scale(0.97); }
}

// 移动端封面渐变蒙?
.mobile-cover {
  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(to bottom, transparent 60%, rgba(0, 0, 0, 0.2) 100%);
    pointer-events: none;
    border-radius: 8px;
    z-index: 1;
  }
}

// ================================================================
// 超小屏幕
// ================================================================
@media (max-width: 400px) {
  .mobile-cover {
    width: 100px;
    height: 75px;
  }

  .mobile-body .mobile-title { font-size: 14px; }
}
</style>
