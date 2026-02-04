<template>
  <div class="lucky-wheel-container">
    <div class="wheel-wrapper">
      <!-- 转盘 -->
      <div
        class="wheel"
        :style="{ transform: `rotate(${rotation}deg)` }"
        ref="wheelRef"
      >
        <div
          v-for="(prize, index) in prizes"
          :key="index"
          class="wheel-segment"
          :style="getSegmentStyle(index)"
        >
          <div class="segment-content">
            <img v-if="prize.image" :src="prize.image" :alt="prize.name" class="prize-image" />
            <span class="prize-name">{{ prize.name }}</span>
          </div>
        </div>
      </div>

      <!-- 中心指针 -->
      <div class="wheel-pointer" @click="handleSpin">
        <div class="pointer-inner">
          <span v-if="!spinning">抽奖</span>
          <span v-else>...</span>
        </div>
      </div>
    </div>

    <!-- 积分和抽奖按钮 -->
    <div class="wheel-info">
      <div class="points-display">
        <span class="label">当前积分:</span>
        <span class="value">{{ availablePoints }}</span>
      </div>
      <div class="cost-info">消耗 {{ costPoints }} 积分/次</div>
    </div>

    <!-- 中奖结果弹窗 -->
    <el-dialog
      v-model="showResult"
      :title="lastResult?.won ? '🎉 恭喜中奖！' : '😢 再接再厉'"
      width="320px"
      center
    >
      <div class="result-content">
        <template v-if="lastResult?.won">
          <img v-if="lastResult.prize?.image" :src="lastResult.prize.image" class="result-image" />
          <p class="result-name">{{ lastResult.prizeName }}</p>
          <p class="result-tip">奖励已自动发放到您的账户</p>
        </template>
        <template v-else>
          <p class="result-tip">很遗憾，本次未中奖</p>
          <p class="result-tip">再试一次吧！</p>
        </template>
      </div>
      <template #footer>
        <el-button type="primary" @click="showResult = false">知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { request } from '@/utils/request'

interface Prize {
  id: number
  name: string
  image?: string
  probability?: number
  prizeType?: number
  prizeValue?: number
}

interface LotteryResult {
  success: boolean
  won: boolean
  prize?: Prize
  prizeIndex?: number
  prizeName?: string
  remainingPoints?: number
  message?: string
}

const props = defineProps<{
  costPoints?: number
}>()

const costPoints = computed(() => props.costPoints || 10)

const prizes = ref<Prize[]>([])
const availablePoints = ref(0)
const rotation = ref(0)
const spinning = ref(false)
const showResult = ref(false)
const lastResult = ref<LotteryResult | null>(null)

// 颜色配置
const colors = ['#FF6B6B', '#4ECDC4', '#FFE66D', '#95E1D3', '#F38181', '#AA96DA', '#FCBAD3', '#FFC4A3']

const getSegmentStyle = (index: number) => {
  const count = prizes.value.length || 8
  const angle = 360 / count
  const rotate = angle * index
  const skew = 90 - angle

  return {
    transform: `rotate(${rotate}deg) skewY(-${skew}deg)`,
    backgroundColor: colors[index % colors.length]
  }
}

const fetchPrizes = async () => {
  try {
    const res = await request.get('/checkin/lottery/prizes')
    if (res.code === 200 && res.data) {
      prizes.value = res.data
    }
  } catch (error) {
    console.error('获取奖品失败:', error)
  }
}

const fetchPoints = async () => {
  try {
    const res = await request.get('/checkin/status')
    if (res.code === 200 && res.data) {
      availablePoints.value = res.data.availablePoints || 0
    }
  } catch (error) {
    console.error('获取积分失败:', error)
  }
}

const handleSpin = async () => {
  if (spinning.value) return

  if (availablePoints.value < costPoints.value) {
    ElMessage.warning('积分不足')
    return
  }

  spinning.value = true

  try {
    const res = await request.post('/checkin/lottery/spin', {
      pointsCost: costPoints.value
    })

    if (res.code === 200 && res.data) {
      lastResult.value = res.data
      availablePoints.value = res.data.remainingPoints || 0

      // 计算旋转角度
      const prizeCount = prizes.value.length
      const segmentAngle = 360 / prizeCount
      let targetIndex = res.data.won ? res.data.prizeIndex : Math.floor(Math.random() * prizeCount)
      
      // 旋转到目标位置：多转几圈 + 目标角度
      const extraRotations = 5 // 多转5圈
      const targetAngle = 360 - (targetIndex * segmentAngle) - segmentAngle / 2
      const finalRotation = rotation.value + extraRotations * 360 + targetAngle + (360 - (rotation.value % 360))

      rotation.value = finalRotation

      // 等待动画完成后显示结果
      setTimeout(() => {
        spinning.value = false
        showResult.value = true
      }, 4000)
    } else {
      ElMessage.error(res.message || '抽奖失败')
      spinning.value = false
    }
  } catch (error) {
    console.error('抽奖失败:', error)
    ElMessage.error('网络错误，请重试')
    spinning.value = false
  }
}

onMounted(() => {
  fetchPrizes()
  fetchPoints()
})

defineExpose({
  refresh: () => {
    fetchPrizes()
    fetchPoints()
  }
})
</script>

<style lang="scss" scoped>
.lucky-wheel-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
}

.wheel-wrapper {
  position: relative;
  width: 320px;
  height: 320px;
}

.wheel {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  position: relative;
  overflow: hidden;
  border: 8px solid #333;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.3);
  transition: transform 4s cubic-bezier(0.17, 0.67, 0.12, 0.99);
}

.wheel-segment {
  position: absolute;
  width: 50%;
  height: 50%;
  left: 50%;
  top: 0;
  transform-origin: 0% 100%;

  .segment-content {
    position: absolute;
    left: -20px;
    top: 30px;
    transform: skewY(45deg) rotate(20deg);
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;

    .prize-image {
      width: 32px;
      height: 32px;
      object-fit: contain;
    }

    .prize-name {
      font-size: 11px;
      font-weight: 600;
      color: #fff;
      text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
      white-space: nowrap;
      max-width: 60px;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
}

.wheel-pointer {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.5);
  transition: transform 0.2s, box-shadow 0.2s;
  z-index: 10;

  &::before {
    content: '';
    position: absolute;
    top: -20px;
    left: 50%;
    transform: translateX(-50%);
    border: 15px solid transparent;
    border-bottom: 25px solid #667eea;
  }

  &:hover:not(:active) {
    transform: translate(-50%, -50%) scale(1.05);
    box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
  }

  &:active {
    transform: translate(-50%, -50%) scale(0.95);
  }

  .pointer-inner {
    color: #fff;
    font-size: 18px;
    font-weight: bold;
    text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.3);
  }
}

.wheel-info {
  margin-top: 24px;
  text-align: center;

  .points-display {
    font-size: 16px;
    margin-bottom: 8px;

    .label {
      color: #666;
    }

    .value {
      color: #e6a23c;
      font-weight: bold;
      font-size: 20px;
      margin-left: 8px;
    }
  }

  .cost-info {
    font-size: 14px;
    color: #999;
  }
}

.result-content {
  text-align: center;
  padding: 20px;

  .result-image {
    width: 80px;
    height: 80px;
    object-fit: contain;
    margin-bottom: 16px;
  }

  .result-name {
    font-size: 20px;
    font-weight: bold;
    color: #333;
    margin-bottom: 12px;
  }

  .result-tip {
    font-size: 14px;
    color: #666;
    margin: 4px 0;
  }
}

// 转盘旋转动画
@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
