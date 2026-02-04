<template>
  <button
    class="favorite-btn"
    :class="{ 'is-favorited': isFavorited, 'is-animating': isAnimating }"
    :disabled="loading"
    @click="handleToggle"
  >
    <span class="heart-icon">
      <svg
        v-if="isFavorited"
        class="heart filled"
        viewBox="0 0 24 24"
        fill="currentColor"
      >
        <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
      </svg>
      <svg
        v-else
        class="heart outline"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
      >
        <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
      </svg>
    </span>
    <span v-if="showLabel" class="label">{{ isFavorited ? '已收藏' : '收藏' }}</span>
    <el-icon v-if="loading" class="loading-icon"><Loading /></el-icon>
  </button>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { request } from '@/utils/request'

interface Props {
  targetId: number | string
  targetType: 'ACTIVITY' | 'COURSE' | 'NOTICE' | 'EXPERIENCE'
  modelValue?: boolean
  showLabel?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  showLabel: true
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'change', value: boolean): void
}>()

const isFavorited = ref(props.modelValue)
const loading = ref(false)
const isAnimating = ref(false)

// 同步外部值变化
watch(() => props.modelValue, (val) => {
  isFavorited.value = val
})

const handleToggle = async () => {
  if (loading.value) return

  loading.value = true
  
  try {
    const res = await request.post('/collection/toggle', {
      targetId: props.targetId,
      targetType: props.targetType
    })
    
    if (res.code === 200) {
      isFavorited.value = res.data
      emit('update:modelValue', res.data)
      emit('change', res.data)
      
      // 触发心跳动画
      isAnimating.value = true
      setTimeout(() => {
        isAnimating.value = false
      }, 600)
      
      ElMessage.success(res.data ? '收藏成功' : '已取消收藏')
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    ElMessage.error('网络错误，请重试')
  } finally {
    loading.value = false
  }
}

// 初始化检查收藏状态
const checkStatus = async () => {
  try {
    const res = await request.get('/collection/check', {
      params: {
        targetId: props.targetId,
        targetType: props.targetType
      }
    })
    if (res.code === 200) {
      isFavorited.value = res.data
      emit('update:modelValue', res.data)
    }
  } catch (error) {
    console.error('检查收藏状态失败:', error)
  }
}

// 暴露方法供父组件调用
defineExpose({
  checkStatus,
  toggle: handleToggle
})
</script>

<style lang="scss" scoped>
.favorite-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 8px 16px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  background: #fff;
  color: #909399;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  outline: none;
  
  &:hover:not(:disabled) {
    border-color: #f56c6c;
    color: #f56c6c;
    background: #fef0f0;
  }
  
  &:disabled {
    cursor: not-allowed;
    opacity: 0.6;
  }
  
  &.is-favorited {
    border-color: #f56c6c;
    color: #f56c6c;
    background: linear-gradient(135deg, #fff5f5 0%, #ffe4e4 100%);
    
    .heart.filled {
      color: #f56c6c;
    }
  }
  
  // 心跳动画
  &.is-animating .heart-icon {
    animation: heartbeat 0.6s ease-in-out;
  }
}

.heart-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  
  svg {
    width: 100%;
    height: 100%;
    transition: transform 0.2s ease;
  }
  
  .heart.outline {
    color: #909399;
  }
  
  .heart.filled {
    color: #f56c6c;
  }
}

.label {
  font-weight: 500;
}

.loading-icon {
  animation: spin 1s linear infinite;
}

// 心跳动画关键帧
@keyframes heartbeat {
  0% {
    transform: scale(1);
  }
  15% {
    transform: scale(1.3);
  }
  30% {
    transform: scale(1);
  }
  45% {
    transform: scale(1.2);
  }
  60% {
    transform: scale(1);
  }
  100% {
    transform: scale(1);
  }
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
