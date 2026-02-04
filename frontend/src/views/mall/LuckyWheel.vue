<template>
  <div class="lucky-wheel-page">
    <div class="page-header">
      <h2>🎰 幸运转盘</h2>
      <p class="subtitle">消耗 {{ costPoints }} 积分抽奖一次，赢取丰厚奖励！</p>
    </div>

    <div class="wheel-section">
      <!-- 积分显示 -->
      <div class="points-display">
        <span class="label">当前积分</span>
        <span class="value">{{ availablePoints }}</span>
      </div>

      <!-- 转盘容器 -->
      <div class="wheel-container">
        <div
          class="wheel"
          :style="{ transform: `rotate(${rotation}deg)`, background: wheelBackground }"
        >
          <!-- 奖品标签 -->
          <div
            v-for="(prize, index) in prizes"
            :key="index"
            class="prize-label"
            :style="getLabelStyle(index)"
          >
            <span class="prize-icon">{{ prize.icon }}</span>
            <span class="prize-name">{{ prize.name }}</span>
          </div>
        </div>

        <!-- 指针 -->
        <div class="wheel-pointer" :class="{ disabled: spinning || availablePoints < costPoints }" @click="handleSpin">
          <span v-if="!spinning">GO</span>
          <span v-else>...</span>
        </div>
      </div>

      <!-- 提示 -->
      <p class="tip">每次抽奖消耗 {{ costPoints }} 积分</p>
    </div>

    <!-- 中奖弹窗 -->
    <el-dialog v-model="showResult" :title="resultTitle" width="320px" center>
      <div class="result-content">
        <span class="result-icon">{{ lastPrize?.icon || '🎁' }}</span>
        <p class="result-name">{{ lastPrize?.name || '谢谢参与' }}</p>
        <p class="result-tip">{{ resultMessage }}</p>
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
  id?: number
  name: string
  icon: string
  color: string
}

const costPoints = 20
const availablePoints = ref(0)
const rotation = ref(0)
const spinning = ref(false)
const showResult = ref(false)
const lastPrize = ref<Prize | null>(null)
const resultTitle = ref('')
const resultMessage = ref('')

// 默认奖品配置 - 6个均分区域
const prizes = ref<Prize[]>([
  { name: '谢谢参与', icon: '😊', color: '#FFE4B5' },
  { name: '10积分', icon: '💰', color: '#87CEEB' },
  { name: '补签卡', icon: '📅', color: '#98FB98' },
  { name: '50积分', icon: '💎', color: '#DDA0DD' },
  { name: '谢谢参与', icon: '😢', color: '#FFDAB9' },
  { name: '100积分', icon: '🏆', color: '#F0E68C' }
])

const segmentAngle = computed(() => 360 / prizes.value.length)

// 生成conic-gradient背景
const wheelBackground = computed(() => {
  const segments = prizes.value.map((prize, index) => {
    const start = (index / prizes.value.length) * 100
    const end = ((index + 1) / prizes.value.length) * 100
    return `${prize.color} ${start}% ${end}%`
  })
  return `conic-gradient(from 0deg, ${segments.join(', ')})`
})

// 计算每个标签的位置样式
const getLabelStyle = (index: number) => {
  const angle = segmentAngle.value
  // 标签放在扇形中心：起始角度 + 半个扇形角度
  const labelAngle = angle * index + angle / 2
  // 转盘直径320px，半径160px，将文字推向半径的约65%位置 (约104px)
  const radius = 105 
  
  return {
    transform: `rotate(${labelAngle}deg) translateY(-${radius}px)`
  }
}

const fetchPoints = async () => {
  try {
    const res = await request.get('/checkin/status')
    if (res.code === 200) {
      availablePoints.value = res.data?.availablePoints || 0
    }
  } catch (error) {
    console.error('获取积分失败:', error)
  }
}

const handleSpin = async () => {
  if (spinning.value) return

  if (availablePoints.value < costPoints) {
    ElMessage.warning('积分不足')
    return
  }

  spinning.value = true

  try {
    const res = await request.post('/checkin/lottery/spin', {
      pointsCost: costPoints
    })

    if (res.code === 200 && res.data) {
      availablePoints.value = res.data.remainingPoints || 0
      
      // 确定中奖位置
      let targetIndex = 0 // 默认指向谢谢参与
      if (res.data.won && res.data.prizeIndex !== undefined) {
        targetIndex = res.data.prizeIndex
      } else {
        // 随机选择一个谢谢参与的位置
        const noWinIndexes = prizes.value
          .map((p, i) => p.name === '谢谢参与' ? i : -1)
          .filter(i => i >= 0)
        targetIndex = noWinIndexes[Math.floor(Math.random() * noWinIndexes.length)] || 0
      }

      // 计算旋转角度 - 指针在顶部(12点方向)
      const extraRotations = 5
      const targetAngle = 360 - (targetIndex * segmentAngle.value) - segmentAngle.value / 2
      const newRotation = rotation.value + extraRotations * 360 + targetAngle + (360 - (rotation.value % 360))
      rotation.value = newRotation

      // 等待动画完成
      setTimeout(() => {
        spinning.value = false
        
        if (res.data.won) {
          lastPrize.value = {
            name: res.data.prizeName || '神秘奖品',
            icon: '🎉',
            color: ''
          }
          resultTitle.value = '🎉 恭喜中奖！'
          resultMessage.value = '奖励已自动发放到您的账户'
        } else {
          lastPrize.value = { name: '谢谢参与', icon: '😊', color: '' }
          resultTitle.value = '😢 再接再厉'
          resultMessage.value = '下次一定能中奖！'
        }
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

onMounted(fetchPoints)
</script>

<style lang="scss" scoped>
.lucky-wheel-page {
  padding: 20px;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.page-header {
  text-align: center;
  margin-bottom: 30px;

  h2 {
    color: #fff;
    font-size: 28px;
    margin: 0 0 10px;
  }

  .subtitle {
    color: rgba(255, 255, 255, 0.8);
    margin: 0;
  }
}

.wheel-section {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.points-display {
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 12px 24px;
  margin-bottom: 30px;

  .label {
    color: rgba(255, 255, 255, 0.8);
    margin-right: 12px;
  }

  .value {
    color: #FFD700;
    font-size: 24px;
    font-weight: bold;
  }
}

.wheel-container {
  position: relative;
  width: 320px;
  height: 320px;
}

.wheel {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  position: relative;
  border: 8px solid #fff;
  box-shadow: 0 0 30px rgba(0, 0, 0, 0.3);
  transition: transform 4s cubic-bezier(0.17, 0.67, 0.12, 0.99);
}

.prize-label {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 100px;
  margin-left: -50px;
  margin-top: -36px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  pointer-events: none;
  z-index: 5;

  .prize-icon {
    font-size: 32px;
    line-height: 1;
    margin-bottom: 2px;
  }

  .prize-name {
    font-size: 13px;
    font-weight: bold;
    color: #334155;
    white-space: nowrap;
    background: rgba(255, 255, 255, 0.4);
    padding: 2px 8px;
    border-radius: 10px;
    backdrop-filter: blur(4px);
  }
}

.wheel-pointer {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
  font-weight: bold;
  box-shadow: 0 4px 20px rgba(238, 90, 36, 0.5);
  transition: transform 0.2s, box-shadow 0.2s;
  z-index: 10;

  &::before {
    content: '';
    position: absolute;
    top: -20px;
    left: 50%;
    transform: translateX(-50%);
    border: 15px solid transparent;
    border-bottom: 25px solid #ff6b6b;
  }

  &:hover:not(.disabled) {
    transform: translate(-50%, -50%) scale(1.1);
    box-shadow: 0 6px 25px rgba(238, 90, 36, 0.6);
  }

  &.disabled {
    cursor: not-allowed;
    opacity: 0.7;
  }
}

.tip {
  margin-top: 24px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.result-content {
  text-align: center;
  padding: 20px;

  .result-icon {
    font-size: 64px;
    display: block;
    margin-bottom: 16px;
  }

  .result-name {
    font-size: 24px;
    font-weight: bold;
    color: #333;
    margin: 0 0 12px;
  }

  .result-tip {
    font-size: 14px;
    color: #666;
    margin: 0;
  }
}
</style>

