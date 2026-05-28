<template>
  <div class="mascot-wrapper">
    <svg
      viewBox="0 0 200 200"
      xmlns="http://www.w3.org/2000/svg"
      class="mascot-svg"
    >
      <defs>
        <!-- 阴影滤镜 -->
        <filter id="dropShadow" x="-20%" y="-20%" width="140%" height="140%">
          <feDropShadow dx="0" dy="2" stdDeviation="3" flood-color="rgba(0,0,0,0.12)" />
        </filter>
        <filter id="softShadow" x="-20%" y="-20%" width="140%" height="140%">
          <feDropShadow dx="0" dy="1" stdDeviation="2" flood-color="rgba(0,0,0,0.08)" />
        </filter>
        <!-- 红色马甲渐变 -->
        <linearGradient id="vestGrad" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stop-color="#E53935" />
          <stop offset="100%" stop-color="#B71C1C" />
        </linearGradient>
        <!-- 帽子渐变 -->
        <linearGradient id="capGrad" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stop-color="#42A5F5" />
          <stop offset="100%" stop-color="#1E88E5" />
        </linearGradient>
        <!-- 身体毛色渐变 -->
        <radialGradient id="furGrad" cx="50%" cy="40%" r="50%">
          <stop offset="0%" stop-color="#FFFFFF" />
          <stop offset="100%" stop-color="#F5F5F5" />
        </radialGradient>
        <!-- 帽檐阴影 -->
        <linearGradient id="brimShadow" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stop-color="rgba(0,0,0,0.06)" />
          <stop offset="100%" stop-color="rgba(0,0,0,0)" />
        </linearGradient>
      </defs>

      <!-- ====== 身体?====== -->
      <g id="body-group" filter="url(#dropShadow)">
        <ellipse cx="100" cy="150" rx="48" ry="40" fill="url(#furGrad)" stroke="#E0E0E0" stroke-width="0.5" />
        <!-- 红色马甲 -->
        <path
          d="M 60 130 Q 60 118 72 112 L 80 110 Q 100 106 120 110 L 128 112 Q 140 118 140 130 L 138 165 Q 130 172 100 174 Q 70 172 62 165 Z"
          fill="url(#vestGrad)"
          stroke="#C62828"
          stroke-width="0.5"
        />
        <path d="M 88 110 L 100 128 L 112 110" fill="none" stroke="#FFCDD2" stroke-width="1.5" stroke-linecap="round" />
        <line x1="100" y1="128" x2="100" y2="170" stroke="#C62828" stroke-width="0.8" stroke-dasharray="3,2" />
        <rect x="70" y="142" width="16" height="12" rx="2" fill="none" stroke="#FFCDD2" stroke-width="0.8" />
        <rect x="114" y="142" width="16" height="12" rx="2" fill="none" stroke="#FFCDD2" stroke-width="0.8" />
        <text x="100" y="140" text-anchor="middle" fill="#FFFFFF" font-size="10" font-weight="bold" font-family="Arial">V</text>
        <path d="M 96 141 Q 96 138 100 141 Q 104 138 104 141 Q 104 145 100 148 Q 96 145 96 141 Z" fill="#FFCDD2" opacity="0.7" />
      </g>

      <!-- ====== 头部?====== -->
      <g ref="headGroupRef" filter="url(#softShadow)">
        <!-- 头部基础 -->
        <circle cx="100" cy="82" r="42" fill="url(#furGrad)" stroke="#E0E0E0" stroke-width="0.5" />

        <!-- 耳朵 -->
        <g ref="leftEarRef">
          <circle cx="66" cy="52" r="14" fill="url(#furGrad)" stroke="#E0E0E0" stroke-width="0.5" />
          <circle cx="66" cy="52" r="8" fill="#FFB6C1" opacity="0.6" />
        </g>
        <g ref="rightEarRef">
          <circle cx="134" cy="52" r="14" fill="url(#furGrad)" stroke="#E0E0E0" stroke-width="0.5" />
          <circle cx="134" cy="52" r="8" fill="#FFB6C1" opacity="0.6" />
        </g>

        <!-- 帽子 -->
        <g>
          <path d="M 62 72 Q 62 42 100 38 Q 138 42 138 72 Z" fill="url(#capGrad)" />
          <ellipse cx="114" cy="72" rx="34" ry="7" fill="#1565C0" />
          <ellipse cx="118" cy="70" rx="20" ry="3" fill="rgba(255,255,255,0.15)" />
          <circle cx="100" cy="40" r="4" fill="#1565C0" stroke="#0D47A1" stroke-width="0.5" />
          <ellipse cx="100" cy="76" rx="30" ry="4" fill="url(#brimShadow)" />
        </g>

        <!-- 鼻子 -->
        <ellipse cx="100" cy="90" rx="6" ry="4.5" fill="#3E2723" />
        <ellipse cx="102" cy="88" rx="2" ry="1.2" fill="rgba(255,255,255,0.4)" />

        <!-- 嘴巴 -->
        <path
          ref="mouthRef"
          :d="mouthPath"
          fill="none"
          stroke="#5D4037"
          stroke-width="1.8"
          stroke-linecap="round"
        />

        <!-- 红晕 -->
        <ellipse cx="74" cy="94" rx="8" ry="5" fill="#FFB6C1" opacity="0.35" />
        <ellipse cx="126" cy="94" rx="8" ry="5" fill="#FFB6C1" opacity="0.35" />
      </g>

      <!-- ====== 眼睛?====== -->
      <g ref="eyeGroupRef">
        <g>
          <ellipse cx="84" cy="80" rx="10" ry="10" fill="white" stroke="#E0E0E0" stroke-width="0.3" />
          <circle :cx="84 + pupilOffsetX" :cy="80 + pupilOffsetY" r="5" fill="#2C2C2C" />
          <circle :cx="86 + pupilOffsetX * 0.5" :cy="78 + pupilOffsetY * 0.5" r="1.8" fill="white" />
          <ellipse
            cx="84" cy="80" rx="11" ry="11"
            fill="url(#furGrad)"
            :style="{ transformOrigin: '84px 80px', transform: `scaleY(${eyelidScale})` }"
          />
        </g>
        <g>
          <ellipse cx="116" cy="80" rx="10" ry="10" fill="white" stroke="#E0E0E0" stroke-width="0.3" />
          <circle :cx="116 + pupilOffsetX" :cy="80 + pupilOffsetY" r="5" fill="#2C2C2C" />
          <circle :cx="118 + pupilOffsetX * 0.5" :cy="78 + pupilOffsetY * 0.5" r="1.8" fill="white" />
          <ellipse
            cx="116" cy="80" rx="11" ry="11"
            fill="url(#furGrad)"
            :style="{ transformOrigin: '116px 80px', transform: `scaleY(${eyelidScale})` }"
          />
        </g>
      </g>

      <!-- ====== 手部?(最顶层) ====== -->
      <g>
        <g :style="{ transform: `translate(${leftHandX}px, ${leftHandY}px)` }">
          <ellipse cx="62" cy="160" rx="18" ry="14" fill="url(#furGrad)" stroke="#E0E0E0" stroke-width="0.5" filter="url(#softShadow)" />
          <ellipse cx="62" cy="162" rx="6" ry="4" fill="#FFB6C1" opacity="0.4" />
          <circle cx="55" cy="156" r="3" fill="#FFB6C1" opacity="0.3" />
          <circle cx="62" cy="153" r="3" fill="#FFB6C1" opacity="0.3" />
          <circle cx="69" cy="156" r="3" fill="#FFB6C1" opacity="0.3" />
        </g>
        <g :style="{ transform: `translate(${rightHandX}px, ${rightHandY}px)` }">
          <ellipse cx="138" cy="160" rx="18" ry="14" fill="url(#furGrad)" stroke="#E0E0E0" stroke-width="0.5" filter="url(#softShadow)" />
          <ellipse cx="138" cy="162" rx="6" ry="4" fill="#FFB6C1" opacity="0.4" />
          <circle cx="131" cy="156" r="3" fill="#FFB6C1" opacity="0.3" />
          <circle cx="138" cy="153" r="3" fill="#FFB6C1" opacity="0.3" />
          <circle cx="145" cy="156" r="3" fill="#FFB6C1" opacity="0.3" />
        </g>
      </g>
    </svg>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import gsap from 'gsap'

type MascotState = 'idle' | 'typing' | 'password' | 'peek'

const currentState = ref<MascotState>('idle')

// SVG refs
const headGroupRef = ref<SVGGElement>()
const leftEarRef = ref<SVGGElement>()
const rightEarRef = ref<SVGGElement>()
const mouthRef = ref<SVGPathElement>()

// 动画状态变?
const pupilOffsetX = ref(0)
const pupilOffsetY = ref(0)
const eyelidScale = ref(0)
const leftHandX = ref(0)
const leftHandY = ref(0)
const rightHandX = ref(0)
const rightHandY = ref(0)
const headOffsetY = ref(0)
const headRotation = ref(0)
const earRotation = ref(0)

// 嘴巴路径
const smilePath = 'M 90 97 Q 100 105 110 97' // 微笑
const surprisePath = 'M 95 97 Q 95 105 100 105 Q 105 105 105 97 Q 105 93 100 93 Q 95 93 95 97' // O ?
const mouthPath = ref(smilePath)

// 定时?
let blinkTimer: ReturnType<typeof setTimeout> | null = null
let breathTween: gsap.core.Tween | null = null

// ==================== 基础待机动画 ====================
const doBlink = () => {
  if (currentState.value === 'password') return // 遮眼时不眨眼
  gsap.to(eyelidScale, {
    value: 1,
    duration: 0.08,
    yoyo: true,
    repeat: 1,
    ease: 'power1.inOut'
  })
}

const scheduleNextBlink = () => {
  const delay = 2000 + Math.random() * 3000 // 更加频繁眨眼 2-5?
  blinkTimer = setTimeout(() => {
    doBlink()
    scheduleNextBlink()
  }, delay)
}

const startBreathing = () => {
  breathTween = gsap.to(headOffsetY, {
    value: -2,
    duration: 2.2,
    yoyo: true,
    repeat: -1,
    ease: 'sine.inOut' // 平缓呼吸
  })
}

// ==================== 状态切换控?(供外部调? ====================

// 暴露给父组件调用
const setIdle = () => {
  if (currentState.value === 'idle') return
  currentState.value = 'idle'
  
  // 更加 Q?的恢复动?
  gsap.to([leftHandX, rightHandX], { value: 0, duration: 0.6, ease: 'elastic.out(1, 0.6)' })
  gsap.to([leftHandY, rightHandY], { value: 0, duration: 0.6, ease: 'elastic.out(1, 0.6)' })
  
  gsap.to(eyelidScale, { value: 0, duration: 0.3, ease: 'power2.out' })
  gsap.to(earRotation, { value: 0, duration: 0.6, ease: 'elastic.out(1, 0.5)' })
  mouthPath.value = smilePath
  
  gsap.to(pupilOffsetX, { value: 0, duration: 0.4, ease: 'power2.out' })
  gsap.to(pupilOffsetY, { value: 0, duration: 0.4, ease: 'power2.out' })
  gsap.to(headRotation, { value: 0, duration: 0.4, ease: 'power2.out' })
}

const setTyping = (textLength: number) => {
  if (currentState.value === 'password' || currentState.value === 'peek') {
    // 如果是从密码切回来的，需要让手放下去
    gsap.to([leftHandX, rightHandX], { value: 0, duration: 0.5, ease: 'back.out(1.5)' })
    gsap.to([leftHandY, rightHandY], { value: 0, duration: 0.5, ease: 'back.out(1.5)' })
  }
  
  currentState.value = 'typing'
  
  // 眼皮睁开，耳朵正常
  gsap.to(eyelidScale, { value: 0, duration: 0.25, ease: 'power2.out' })
  gsap.to(earRotation, { value: 0, duration: 0.3 })
  mouthPath.value = smilePath

  // 根据文本长度计算眼球和头部的跟随
  const maxLen = 24
  const ratio = Math.min(textLength, maxLen) / maxLen
  const targetX = -6 + ratio * 12 // 范围扩大?-6 ?+6
  const targetY = 2 // 稍微多往下看一点点

  gsap.to(pupilOffsetX, {
    value: Math.min(Math.max(targetX, -6), 6),
    duration: 0.3,
    ease: 'power2.out'
  })
  gsap.to(pupilOffsetY, {
    value: targetY,
    duration: 0.3,
    ease: 'power2.out'
  })

  // 头部随之轻微转动
  const headR = ((targetX / 6) * 3)
  gsap.to(headRotation, {
    value: Math.min(Math.max(headR, -4), 4),
    duration: 0.4,
    ease: 'back.out(1.2)' // 让其在打字时头部移动有一点惯?
  })
}

const setPassword = () => {
  currentState.value = 'password'

  // 双手猛烈弹起捂脸（极?Q??Spring 效果?
  gsap.to(leftHandX, { value: 18, duration: 0.6, ease: 'elastic.out(1, 0.6)' })
  gsap.to(leftHandY, { value: -78, duration: 0.6, ease: 'elastic.out(1, 0.6)' })
  gsap.to(rightHandX, { value: -18, duration: 0.6, ease: 'elastic.out(1, 0.6)' })
  gsap.to(rightHandY, { value: -78, duration: 0.6, ease: 'elastic.out(1, 0.6)' })

  // 紧闭双眼
  gsap.to(eyelidScale, { value: 1, duration: 0.2, delay: 0.1, ease: 'power2.in' })

  // 耳朵不仅下垂，还带有 Q?震动
  gsap.to(earRotation, { value: 16, duration: 0.7, ease: 'elastic.out(1.2, 0.4)' })

  // O 型嘴?
  setTimeout(() => {
    mouthPath.value = surprisePath
  }, 100)

  // 头复?
  gsap.to(pupilOffsetX, { value: 0, duration: 0.3 })
  gsap.to(pupilOffsetY, { value: 0, duration: 0.3 })
  gsap.to(headRotation, { value: 0, duration: 0.4, ease: 'back.out(1.5)' })
}

const setPeek = () => {
  currentState.value = 'peek'

  // 右手稍微下移并外翻露出眼?
  gsap.to(rightHandX, { value: -32, duration: 0.5, ease: 'back.out(1.4)' })
  gsap.to(rightHandY, { value: -66, duration: 0.5, ease: 'back.out(1.4)' })

  // 偷偷睁开一点点眼睛
  gsap.to(eyelidScale, { value: 0.35, duration: 0.3, ease: 'power2.out' })
  mouthPath.value = smilePath
}

const setFromPeek = () => {
  // 恢复到紧张捂脸状态?
  gsap.to(rightHandX, { value: -18, duration: 0.5, ease: 'elastic.out(1, 0.6)' })
  gsap.to(rightHandY, { value: -78, duration: 0.5, ease: 'elastic.out(1, 0.6)' })
  gsap.to(eyelidScale, { value: 1, duration: 0.2, ease: 'power2.in' })
  mouthPath.value = surprisePath
  currentState.value = 'password'
}

// 暴露方法给父组件
defineExpose({
  setIdle,
  setTyping,
  setPassword,
  setPeek,
  setFromPeek
})

// ==================== 物理变化监控 ====================
// 使用 watch 绑定 transform，避免每次模板渲染触?
watch([headOffsetY, headRotation], () => {
  if (headGroupRef.value) {
    headGroupRef.value.style.transform = `translateY(${headOffsetY.value}px) rotate(${headRotation.value}deg)`
    headGroupRef.value.style.transformOrigin = '100px 82px'
  }
})

watch(earRotation, (val) => {
  if (leftEarRef.value) {
    leftEarRef.value.style.transform = `rotate(${val}deg)`
    leftEarRef.value.style.transformOrigin = '75px 60px'
  }
  if (rightEarRef.value) {
    rightEarRef.value.style.transform = `rotate(-${val}deg)`
    rightEarRef.value.style.transformOrigin = '125px 60px'
  }
})

onMounted(() => {
  nextTick(() => {
    startBreathing()
    scheduleNextBlink()
  })
})

onUnmounted(() => {
  if (blinkTimer) clearTimeout(blinkTimer)
  if (breathTween) breathTween.kill()
})
</script>

<style scoped>
.mascot-wrapper {
  display: flex;
  justify-content: center;
  margin-top: -10px;
  margin-bottom: -10px;
  position: relative;
  z-index: 2;
}

.mascot-svg {
  width: 180px;
  height: 180px;
  filter: drop-shadow(0 4px 12px rgba(0,0,0,0.08));
}

@media (max-width: 480px) {
  .mascot-svg {
    width: 150px;
    height: 150px;
  }
}
</style>
