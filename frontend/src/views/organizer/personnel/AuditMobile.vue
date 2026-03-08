<template>
  <div class="page-container">
    <!-- 极光青渐变顶部导航 -->
    <div class="aurora-header">
      <div class="activity-trigger" @click="showActivityDrawer = true">
        <span class="title-text truncate">{{ currentActivityTitle }}</span>
        <el-icon><CaretBottom /></el-icon>
      </div>
    </div>

    <!-- iOS 分段控制器 -->
    <div class="segment-wrapper">
      <div class="ios-segment">
        <div 
          v-for="tab in tabs" :key="tab.key"
          class="segment-item"
          :class="{ active: currentTab === tab.key }"
          @click="handleTabChange(tab.key)"
        >
          {{ tab.label }}
          <span v-if="tab.count > 0" class="badge">{{ tab.count }}</span>
        </div>
      </div>
    </div>

    <!-- 审核列表 Area -->
    <div class="audit-list" v-loading="loading">
        <div 
          v-for="(user, index) in (list as any[])" 
          :key="user.id"
          class="swipe-card"
          @touchstart="handleTouchStart($event, index)"
          @touchmove="handleTouchMove($event, index)"
          @touchend="handleTouchEnd($event, index)"
        >
        <!-- 侧滑内容区 -->
        <div 
          class="card-content"
          :style="{ transform: `translateX(${swipeOffsets[index] || 0}px)` }"
          @click="$emit('show-detail', user)"
        >
          <el-avatar :size="40" :src="user.volunteerAvatar">{{ user.volunteerName?.charAt(0) }}</el-avatar>
          <div class="info">
            <div class="row-1">
              <span class="name">{{ user.volunteerName }}</span>
              <span class="score-tag">诚信息?{{ user.creditScore || 100 }}</span>
            </div>
            <div class="row-2">{{ user.studentId || '学号未填' }} | {{ user.college || '未知学院' }}</div>
          </div>
        </div>
        
        <!-- 侧滑操作栏 -->
        <div class="swipe-actions" v-if="currentTab === 0">
          <div class="action-btn pass" @click.stop="handlePass(user, index)">通过</div>
          <div class="action-btn reject" @click.stop="handleReject(user, index)">驳回</div>
        </div>
        <div class="swipe-actions static" v-else>
          <div class="status-label" :class="user.status === 1 ? 'passed' : 'rejected'">
            {{ user.status === 1 ? '已通过' : '已驳回' }}
          </div>
        </div>
      </div>

      <el-empty v-if="!loading && list.length === 0" description="暂无申请" :image-size="80" />
      
      <!-- 分页 (仅移动端简单版) -->
      <div class="m-pagination" v-if="total > size">
        <el-pagination 
          layout="prev, pager, next" 
          :total="total" 
          :page-size="size" 
          :current-page="page"
          small
          @current-change="val => $emit('update:page', val)"
        />
      </div>
    </div>

    <!-- 活动切换抽屉 (Bottom Sheet) -->
    <el-drawer
      v-model="showActivityDrawer"
      title="选择活动"
      direction="btt"
      size="60%"
      class="activity-drawer"
      :with-header="false"
    >
      <div class="drawer-handle"></div>
      <div class="drawer-title">切换活动审核队列</div>
      <div class="activity-options">
        <div 
          v-for="act in activities" 
          :key="act.id"
          class="act-option"
          :class="{ active: act.id === activityId }"
          @click="selectActivity(act.id)"
        >
          <div class="act-info">
            <div class="act-name">{{ act.title }}</div>
            <div class="act-count" v-if="act.pendingCount > 0">{{ act.pendingCount }} 个待审核</div>
          </div>
          <el-icon v-if="act.id === activityId"><Check /></el-icon>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, watch } from 'vue'
import { CaretBottom, Check } from '@element-plus/icons-vue'

const props = defineProps({
  activities: { type: Array as any, default: () => [] },
  activityId: { type: [Number, null] as any, default: null },
  list: { type: Array as any, default: () => [] },
  loading: { type: Boolean, default: false },
  total: { type: Number, default: 0 },
  page: { type: Number, default: 1 },
  size: { type: Number, default: 20 },
  currentTab: { type: Number, default: 0 }
})

const emit = defineEmits([
  'update:activityId', 
  'update:currentTab', 
  'update:page',
  'audit', 
  'show-detail',
  'refresh'
])

const showActivityDrawer = ref(false)
const swipeOffsets = reactive<Record<string, number>>({})
const startX = ref(0)
const isSwiping = ref(false)

const tabs = computed(() => [
  { label: '待审核', key: 0, count: 0 },
  { label: '已通过', key: 1, count: 0 },
  { label: '已驳回', key: 6, count: 0 }
])

const currentActivityTitle = computed(() => {
  const act = props.activities.find((a: any) => a.id === props.activityId)
  return act ? act.title : '请选择活动'
})

// 活动选择
const selectActivity = (id: number) => {
  emit('update:activityId', id)
  showActivityDrawer.value = false
  emit('refresh')
}

// Tab 切换
const handleTabChange = (key: number) => {
  emit('update:currentTab', key)
  // 重置所有滑动状态
  Object.keys(swipeOffsets).forEach(k => delete swipeOffsets[Number(k)])
  emit('refresh')
}

// 触摸交互实现
const handleTouchStart = (e: TouchEvent, index: number) => {
  if (props.currentTab !== 0) return // 只有待审核支持滑动手势
  startX.value = e.touches[0].clientX
  isSwiping.value = true
}

const handleTouchMove = (e: TouchEvent, index: number) => {
  if (!isSwiping.value) return
  const currentX = e.touches[0].clientX
  const diff = currentX - startX.value
  
  // 限制滑动范围 (向左滑，diff 为负)
  if (diff < 0) {
    swipeOffsets[index] = Math.max(diff, -140) // 两个按钮 140px
  } else {
    swipeOffsets[index] = Math.min(diff, 0)
  }
}

const handleTouchEnd = (e: TouchEvent, index: number) => {
  isSwiping.value = false
  const offset = swipeOffsets[index] || 0
  
  if (offset < -70) {
    swipeOffsets[index] = -140 // 展开
  } else {
    swipeOffsets[index] = 0 // 回弹
  }

  // 关闭其他已打开的卡片
  Object.keys(swipeOffsets).forEach(k => {
    if (Number(k) !== index) swipeOffsets[Number(k)] = 0
  })
}

const handlePass = (user: any, index: number) => {
  swipeOffsets[index] = 0
  emit('audit', user.id, true)
}

const handleReject = (user: any, index: number) => {
  swipeOffsets[index] = 0
  emit('audit', user.id, false)
}

// 监听活动/Tab 变化重置偏移量
watch(() => [props.activityId, props.currentTab], () => {
  Object.keys(swipeOffsets).forEach(k => swipeOffsets[Number(k)] = 0)
})
</script>

<style scoped lang="scss">
.page-container { background: #F2F2F7; min-height: 100vh; padding-bottom: 100px; }

.aurora-header {
  height: 140px; padding-top: 50px;
  background: linear-gradient(135deg, #0093E9 0%, #80D0C7 100%);
  color: #fff; text-align: center;
  border-radius: 0 0 32px 32px;
  box-shadow: 0 10px 20px rgba(0, 147, 233, 0.15);
}

.activity-trigger {
  display: inline-flex; align-items: center; gap: 8px;
  background: rgba(255,255,255,0.25); 
  padding: 10px 20px; 
  border-radius: 24px;
  font-weight: 700; 
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255,255,255,0.3);
  max-width: 85%;
  
  .title-text { font-size: 15px; }
  .truncate { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
}

/* 分段控制器 */
.segment-wrapper { padding: 0 16px; margin-top: -36px; margin-bottom: 24px; }
.ios-segment {
  background: rgba(118, 118, 128, 0.12); 
  padding: 3px; 
  border-radius: 12px;
  display: flex; 
  height: 42px;
  backdrop-filter: blur(20px);
}
.segment-item {
  flex: 1; display: flex; align-items: center; justify-content: center;
  font-size: 14px; color: #636366; border-radius: 9px; transition: all 0.3s cubic-bezier(0.2, 0.8, 0.5, 1);
  position: relative; font-weight: 500;
  
  .badge {
    position: absolute; top: 4px; right: 8px;
    background: #FF3B30; color: #fff; font-size: 10px;
    min-width: 16px; height: 16px; border-radius: 8px;
    display: flex; align-items: center; justify-content: center;
    padding: 0 4px;
  }
}
.segment-item.active {
  background: #fff; color: #000; font-weight: 700;
  box-shadow: 0 3px 8px rgba(0,0,0,0.1), 0 3px 1px rgba(0,0,0,0.04);
}

/* 审核列表 (Inset Grouped) */
.audit-list { padding: 0 16px; }
.swipe-card {
  background: #fff; border-radius: 14px; margin-bottom: 12px;
  position: relative; overflow: hidden;
  display: flex;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.card-content {
  padding: 16px; flex: 1; display: flex; gap: 14px; align-items: center;
  background: #fff; z-index: 2;
  transition: transform 0.3s cubic-bezier(0.18, 0.89, 0.32, 1.28);
  
  .info {
    flex: 1;
    .row-1 { 
      display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px;
      .name { font-size: 16px; font-weight: 700; color: #1c1c1e; }
      .score-tag { font-size: 11px; background: #FFF7ED; color: #F97316; padding: 3px 8px; border-radius: 6px; font-weight: 600; }
    }
    .row-2 { font-size: 13px; color: #8e8e93; }
  }
}

/* 侧滑按钮 */
.swipe-actions {
  position: absolute; right: 0; top: 0; bottom: 0; display: flex; z-index: 1;
}
.action-btn {
  width: 70px; display: flex; align-items: center; justify-content: center;
  color: #fff; font-weight: 700; font-size: 15px;
}
.action-btn.pass { background: #34C759; }
.action-btn.reject { background: #FF3B30; }

.status-label {
  padding: 0 16px; display: flex; align-items: center; font-size: 14px; font-weight: 600;
  &.passed { color: #34C759; }
  &.rejected { color: #FF3B30; }
}

/* 抽屉样式 */
.activity-drawer {
  border-radius: 20px 20px 0 0 !important;
  .drawer-handle {
    width: 36px; height: 5px; background: #C7C7CC; border-radius: 3px;
    margin: 12px auto;
  }
  .drawer-title {
    text-align: center; font-size: 17px; font-weight: 700; color: #1c1c1e;
    padding: 10px 0 20px;
  }
}
.activity-options {
  padding: 0 16px 40px;
  .act-option {
    display: flex; justify-content: space-between; align-items: center;
    padding: 16px; border-radius: 12px; margin-bottom: 8px;
    background: #F2F2F7; transition: all 0.2s;
    
    &.active { background: #E5E5EA; .act-name { color: #007AFF; } .el-icon { color: #007AFF; } }
    
    .act-name { font-size: 15px; font-weight: 600; color: #1c1c1e; margin-bottom: 4px; }
    .act-count { font-size: 12px; color: #8e8e93; }
  }
}

.m-pagination { display: flex; justify-content: center; padding: 20px 0; }
</style>
