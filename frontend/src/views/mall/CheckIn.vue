<template>
  <div class="checkin-page" :class="{ 'is-mobile': isMobile }">

    <!-- ==================== 头部：连续签到 ==================== -->
    <div class="checkin-hero">
      <!-- 游客遮罩 -->
      <div v-if="!isLoggedIn" class="guest-mask">
        <div class="mask-content">
          <div class="lock-icon">
            <el-icon><Lock /></el-icon>
          </div>
          <h3>登录后查看签到数据</h3>
          <p>记录您的每一天，赢取丰厚积分奖励</p>
          <el-button type="primary" round class="mask-login-btn" @click="router.push({ path: '/login', query: { redirect: route.fullPath } })">
            立即登录
          </el-button>
        </div>
      </div>

      <div class="hero-inner">
        <div class="streak-area">
          <span class="streak-label">已连续签到</span>
          <div class="streak-big">
            <span class="streak-num">{{ continuousDays }}</span>
            <span class="streak-unit"></span>
          </div>
        </div>
        <div class="hero-stats">
          <div class="hero-stat">
            <span class="hs-val">{{ monthlySignins }}</span>
            <span class="hs-lbl">本月</span>
          </div>
          <div class="hero-stat">
            <span class="hs-val">{{ totalSignins }}</span>
            <span class="hs-lbl">累计</span>
          </div>
          <div class="hero-stat">
            <span class="hs-val">{{ availablePoints }}</span>
            <span class="hs-lbl">积分</span>
          </div>
          <div class="hero-stat level-stat">
            <el-progress
              type="circle" :percentage="levelProgress" :width="42" :stroke-width="4"
              :color="'#43e97b'"
            >
              <template #default>
                <span class="level-text">Lv{{ userLevel }}</span>
              </template>
            </el-progress>
            <span class="hs-lbl">等级</span>
          </div>
        </div>
      </div>

      <!-- 签到按钮 -->
      <button
        class="checkin-btn"
        :class="{ signed: signedToday, loading: loading }"
        :disabled="signedToday || loading"
        @click="handleSignin"
      >
        <template v-if="loading">签到?..</template>
        <template v-else-if="signedToday">今日已签到</template>
        <template v-else>🎯 立即签到</template>
      </button>

      <!-- 补签提示 -->
      <div v-if="canMakeup && makeupCardCount > 0" class="makeup-tip">
        <span>昨日未签</span>
        <el-button type="warning" size="small" round @click="handleMakeup()">
          使用补签卡 ({{ makeupCardCount }})
        </el-button>
      </div>
    </div>

    <!-- ==================== 奖励规则 ==================== -->
    <div class="reward-rules">
      <div class="rule-item">
        <span class="rule-icon"></span>
        <span class="rule-text">基础签到 <strong>+5积分</strong></span>
      </div>
      <div class="rule-item">
        <span class="rule-icon">🔥</span>
        <span class="rule-text">连续7天 <strong>+10积分</strong></span>
      </div>
      <div class="rule-item">
        <span class="rule-icon">🏆</span>
        <span class="rule-text">连续30天 <strong>+15积分</strong></span>
      </div>
    </div>

    <!-- ==================== 签到日历 ==================== -->
    <div class="calendar-section">
      <div class="calendar-header">
        <el-button text @click="changeMonth(-1)"><el-icon><ArrowLeft /></el-icon></el-button>
        <span class="calendar-title">{{ calendarTitle }}</span>
        <el-button text @click="changeMonth(1)"><el-icon><ArrowRight /></el-icon></el-button>
      </div>

      <!-- 星期 -->
      <div class="weekday-row">
        <span v-for="d in ['日','一','二','三','四','五','六']" :key="d" class="weekday-cell">{{ d }}</span>
      </div>

      <!-- 骨架屏 -->
      <div v-if="calendarLoading" class="date-grid">
        <div v-for="i in 35" :key="i" class="date-cell is-skeleton">
          <el-skeleton-item variant="circle" style="width: 28px; height: 28px" />
        </div>
      </div>

      <!-- 日期网格 -->
      <div v-else class="date-grid">
        <el-tooltip
          v-for="(cell, idx) in calendarCells"
          :key="idx"
          :content="getDateTooltip(cell)"
          placement="top"
          :disabled="cell.otherMonth"
          :show-after="300"
        >
          <div
            class="date-cell"
            :class="{
              'is-signed': cell.signed,
              'is-today': cell.isToday,
              'is-other-month': cell.otherMonth,
              'can-makeup': cell.canMakeup
            }"
            @click="handleDateClick(cell)"
          >
            <span class="date-num">{{ cell.day }}</span>
            <span v-if="cell.signed" class="signed-dot"></span>
          </div>
        </el-tooltip>
      </div>
    </div>

    <!-- ==================== 签到成功庆祝弹窗 ==================== -->
    <Transition name="celebrate-fade">
      <div v-if="showCelebration" class="celebration-overlay" @click="showCelebration = false">
        <!-- 金币/撒花粒子 -->
        <div class="particles-container">
          <span
            v-for="i in 30"
            :key="i"
            class="particle"
            :class="[`p-${i % 5}`]"
            :style="{
              left: `${Math.random() * 100}%`,
              animationDelay: `${Math.random() * 0.6}s`,
              animationDuration: `${1.5 + Math.random() * 1.5}s`
            }"
          ></span>
        </div>

        <!-- 中央弹窗 -->
        <div class="celebrate-modal" @click.stop>
          <div class="celebrate-coin">🪙</div>
          <h2 class="celebrate-title">签到成功！</h2>
          <p class="celebrate-points">+{{ earnedPoints }} 积分</p>
          <p class="celebrate-streak">已连续签到 <strong>{{ continuousDays }}</strong> 天</p>
          <button class="celebrate-btn" @click="showCelebration = false">太棒了</button>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, ArrowRight, Lock } from '@element-plus/icons-vue'
import { request } from '@/utils/request'

const router = useRouter()
const route = useRoute()
const isLoggedIn = computed(() => !!localStorage.getItem('token'))

// ================== 响应式 ==================
const windowWidth = ref(window.innerWidth)
const isMobile = computed(() => windowWidth.value < 768)
onMounted(() => window.addEventListener('resize', () => { windowWidth.value = window.innerWidth }))

// ================== 状态 ==================
const loading = ref(false)
const signedToday = ref(false)
const continuousDays = ref(0)
const monthlySignins = ref(0)
const totalSignins = ref(0)
const availablePoints = ref(0)
const canMakeup = ref(false)
const makeupCardCount = ref(0)
const signedDates = ref<string[]>([])
const showCelebration = ref(false)
const earnedPoints = ref(5)
const calendarLoading = ref(true)

// 等级计算
const userLevel = computed(() => Math.min(10, Math.floor(totalSignins.value / 30) + 1))
const levelProgress = computed(() => {
  const daysInLevel = totalSignins.value % 30
  return Math.round((daysInLevel / 30) * 100)
})

const getDateTooltip = (cell: CalCell) => {
  if (cell.signed) return `✅ 已签到 +5积分`
  if (cell.isToday && !signedToday.value) return `🎯 点击签到 +5积分`
  if (cell.canMakeup) return `📌 可补签`
  return `${cell.dateStr}`
}

// ================== 日历逻辑 ==================
const currentYear = ref(new Date().getFullYear())
const currentMonth = ref(new Date().getMonth()) // 0-based

const calendarTitle = computed(() => `${currentYear.value}年${currentMonth.value + 1}月`)

const changeMonth = (delta: number) => {
  const d = new Date(currentYear.value, currentMonth.value + delta, 1)
  currentYear.value = d.getFullYear()
  currentMonth.value = d.getMonth()
}

interface CalCell {
  day: number; dateStr: string; signed: boolean
  isToday: boolean; otherMonth: boolean; canMakeup: boolean
}

const calendarCells = computed<CalCell[]>(() => {
  const year = currentYear.value
  const month = currentMonth.value
  const firstDay = new Date(year, month, 1).getDay() // 0=Sunday
  const daysInMonth = new Date(year, month + 1, 0).getDate()
  const prevDays = new Date(year, month, 0).getDate()
  const todayStr = new Date().toISOString().split('T')[0]

  const cells: CalCell[] = []

  // 上月填充
  for (let i = firstDay - 1; i >= 0; i--) {
    const d = prevDays - i
    const ds = formatDateStr(year, month - 1, d)
    cells.push({ day: d, dateStr: ds, signed: isSignedDate(ds), isToday: false, otherMonth: true, canMakeup: false })
  }

  // 当月
  for (let d = 1; d <= daysInMonth; d++) {
    const ds = formatDateStr(year, month, d)
    const past = ds < todayStr
    cells.push({
      day: d, dateStr: ds,
      signed: isSignedDate(ds),
      isToday: ds === todayStr,
      otherMonth: false,
      canMakeup: past && !isSignedDate(ds)
    })
  }

  // 下月填充 42 (6 rows)
  const remaining = 42 - cells.length
  for (let d = 1; d <= remaining; d++) {
    const ds = formatDateStr(year, month + 1, d)
    cells.push({ day: d, dateStr: ds, signed: isSignedDate(ds), isToday: false, otherMonth: true, canMakeup: false })
  }

  return cells
})

const formatDateStr = (year: number, month: number, day: number) => {
  const d = new Date(year, month, day)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const isSignedDate = (dateStr: string) => signedDates.value.includes(dateStr)

// ================== API ==================
const fetchStatus = async () => {
  try {
    const res = await request.get('/checkin/status')
    if (res.code === 200 && res.data) {
      signedToday.value = res.data.signedToday || false
      continuousDays.value = res.data.continuousDays || 0
      monthlySignins.value = res.data.monthlySignins || 0
      totalSignins.value = res.data.totalSignins || 0
      availablePoints.value = res.data.availablePoints || 0
      canMakeup.value = res.data.canMakeup || false
      makeupCardCount.value = res.data.makeupCardCount || 0
    }
  } catch (e) { console.error('获取签到状态失败', e) }
}

const fetchCalendar = async () => {
  try {
    const res = await request.get('/checkin/calendar', {
      params: { year: currentYear.value, month: currentMonth.value + 1 }
    })
    if (res.code === 200 && res.data) {
      signedDates.value = res.data.map((item: any) => {
        const date = item.signinDate || item.createTime || item.date
        if (Array.isArray(date)) {
          return `${date[0]}-${String(date[1]).padStart(2, '0')}-${String(date[2]).padStart(2, '0')}`
        }
        // 防止后端返回带时区时间的格式如 "2026-02-12T00:00:00"
        return String(date).substring(0, 10)
      })
    }
  } catch (e) { console.error('获取签到日历失败:', e) }
}

const handleSignin = async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录后再进行签到')
    router.push('/login')
    return
  }
  if (signedToday.value || loading.value) return
  loading.value = true
  try {
    const res = await request.post('/checkin/signin')
    if (res.code === 200 && res.data?.success) {
      signedToday.value = true
      continuousDays.value = res.data.continuousDays || continuousDays.value + 1

      // 计算获得积分
      if (continuousDays.value >= 30) earnedPoints.value = 15
      else if (continuousDays.value >= 7) earnedPoints.value = 10
      else earnedPoints.value = 5

      availablePoints.value = res.data.availablePoints || availablePoints.value

      // 🎉 弹出庆祝动效
      showCelebration.value = true

      fetchCalendar()
      fetchStatus()
    } else {
      ElMessage.error(res.message || '签到失败')
    }
  } catch (e) {
    ElMessage.error('签到失败，请重新尝试')
  } finally { loading.value = false }
}

const handleMakeup = async (date: string = '') => {
  if (loading.value) return
  loading.value = true
  try {
    const res = await request.post('/checkin/makeup', {}, { params: { date } })
    if (res.code === 200 && res.data?.success) {
      ElMessage.success(res.data.message || '补签成功')
      fetchStatus()
      fetchCalendar()
    } else { ElMessage.error(res.message || '补签失败') }
  } catch (e) { ElMessage.error('补签失败') }
  finally { loading.value = false }
}

const handleDateClick = (cell: CalCell) => {
  if (!cell.canMakeup) return
  if (makeupCardCount.value <= 0) {
    ElMessage.warning('没有补签卡，去积分商城兑换吧')
    return
  }
  ElMessageBox.confirm(`消耗 1 张补签卡，对 ${cell.dateStr} 补签？`, '补签确认', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(() => handleMakeup(cell.dateStr)).catch(() => {})
}

watch([currentYear, currentMonth], () => {
  calendarLoading.value = true
  fetchCalendar().then(() => { calendarLoading.value = false })
})

onMounted(async () => {
  await Promise.all([fetchStatus(), fetchCalendar()])
  calendarLoading.value = false
})
</script>

<style lang="scss" scoped>
.checkin-page {
  min-height: 100vh;
  background: var(--app-bg);
  padding-bottom: 40px;
}

// ================================================================
// 头部区域
// ================================================================
.checkin-hero {
  background: linear-gradient(135deg, #00BFA6 0%, #009688 50%, #00695C 100%);
  padding: 24px 16px 28px;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  overflow: hidden;

  // 游客遮罩
  .guest-mask {
    position: absolute;
    inset: 0;
    z-index: 100;
    background: rgba(0, 191, 166, 0.2);
    backdrop-filter: blur(12px);
    -webkit-backdrop-filter: blur(12px);
    display: flex;
    align-items: center;
    justify-content: center;
    text-align: center;
    animation: fadeIn 0.4s ease-out;

    .mask-content {
      color: #fff;
      padding: 0 40px;

      .lock-icon {
        width: 60px;
        height: 60px;
        background: rgba(255, 255, 255, 0.2);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin: 0 auto 16px;
        font-size: 28px;
        border: 1px solid rgba(255, 255, 255, 0.3);
      }

      h3 {
        margin: 0 0 8px;
        font-size: 18px;
        font-weight: 600;
        text-shadow: 0 2px 4px rgba(0,0,0,0.1);
      }

      p {
        margin: 0 0 24px;
        font-size: 13px;
        opacity: 0.9;
      }

      .mask-login-btn {
        padding: 10px 32px;
        font-weight: bold;
        background: #fff !important;
        color: #00BFA6 !important;
        border: none !important;
        box-shadow: 0 4px 15px rgba(0,0,0,0.1);
        
        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 6px 20px rgba(0,0,0,0.15);
        }
      }
    }
  }
}

.hero-inner {
  width: 100%;
  max-width: 400px;
  text-align: center;
  margin-bottom: 20px;
}

.streak-area {
  margin-bottom: 16px;

  .streak-label {
    display: block;
    font-size: 13px;
    color: rgba(255, 255, 255, 0.7);
    margin-bottom: 4px;
  }
}

.streak-big {
  .streak-num {
    font-size: 64px;
    font-weight: 800;
    color: #fff;
    text-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
    line-height: 1;
  }

  .streak-unit {
    font-size: 20px;
    color: rgba(255, 255, 255, 0.9);
    margin-left: 4px;
  }
}

.hero-stats {
  display: flex;
  justify-content: space-around;
  padding: 12px 0;
  border-top: 1px solid rgba(255, 255, 255, 0.15);
}

.hero-stat {
  text-align: center;

  .hs-val {
    display: block;
    font-size: 20px;
    font-weight: 700;
    color: #fff;
  }

  .hs-lbl {
    font-size: 11px;
    color: rgba(255, 255, 255, 0.65);
  }
}

// --- 签到按钮 ---
.checkin-btn {
  width: 90%;
  max-width: 320px;
  height: 52px;
  border: none;
  border-radius: 26px;
  font-size: 18px;
  font-weight: 700;
  color: #00BFA6;
  background: #fff;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  transition: all 0.25s;
  letter-spacing: 1px;

  &:active:not(:disabled) { transform: scale(0.95); }
  &.signed { background: rgba(255, 255, 255, 0.4); color: #fff; box-shadow: none; cursor: default; }
  &.loading { opacity: 0.7; cursor: wait; }
}

.makeup-tip {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 13px;
}

// ================================================================
// 奖励规则
// ================================================================
.reward-rules {
  display: flex;
  gap: 0;
  margin: 16px;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}

.rule-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 14px 8px;

  &:not(:last-child) {
    border-right: 0.5px solid rgba(0, 0, 0, 0.04);
  }

  .rule-icon { font-size: 20px; }
  .rule-text { font-size: 11px; color: #666; text-align: center; strong { color: #F7971E; } }
}

// ================================================================
// 日历
// ================================================================
.calendar-section {
  margin: 0 16px;
  background: #fff;
  border-radius: 12px;
  padding: 16px;
}

.calendar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;

  .calendar-title {
    font-size: 16px;
    font-weight: 600;
    color: #333;
  }
}

.weekday-row {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  margin-bottom: 8px;

  .weekday-cell {
    text-align: center;
    font-size: 12px;
    color: #999;
    font-weight: 500;
    padding: 4px 0;
  }
}

.date-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}

.date-cell {
  aspect-ratio: 1 / 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  cursor: default;
  position: relative;
  transition: all 0.2s;

  .date-num {
    font-size: 14px;
    font-weight: 500;
    color: #333;
  }

  .signed-dot {
    font-size: 10px;
    color: #fff;
    margin-top: 1px;
  }

  &.is-other-month .date-num {
    color: #ddd;
  }

  &.is-today {
    border: 2px solid var(--primary-color);

    .date-num { color: var(--primary-color); font-weight: 700; }
  }

  &.is-signed {
    background: linear-gradient(135deg, #00BFA6, #009688);

    .date-num { color: #fff; }
  }

  &.can-makeup {
    cursor: pointer;

    &:hover, &:active {
      background: rgba(230, 162, 60, 0.1);
      border: 1px dashed #E6A23C;
    }
  }
}

// ================================================================
// 庆祝弹窗 + 粒子动画
// ================================================================
.celebration-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background: rgba(0, 0, 0, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
}

.celebrate-fade-enter-active { animation: fadeIn 0.3s ease; }
.celebrate-fade-leave-active { animation: fadeOut 0.25s ease; }

// --- 粒子 ---
.particles-container {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.particle {
  position: absolute;
  top: -20px;
  width: 10px;
  height: 10px;
  border-radius: 2px;
  animation: fall linear forwards;
  opacity: 0.9;

  &.p-0 { background: #FFD700; width: 12px; height: 12px; border-radius: 50%; } // 金币
  &.p-1 { background: #FF6B6B; }
  &.p-2 { background: #54A0FF; transform: rotate(45deg); }
  &.p-3 { background: #1DD1A1; border-radius: 50%; }
  &.p-4 { background: #FFA502; width: 8px; height: 14px; }
}

@keyframes fall {
  0% { transform: translateY(-20px) rotate(0deg); opacity: 1; }
  100% { transform: translateY(100vh) rotate(720deg); opacity: 0; }
}

// --- 弹窗 ---
.celebrate-modal {
  position: relative;
  z-index: 1;
  background: #fff;
  border-radius: 20px;
  padding: 32px 28px;
  text-align: center;
  width: 280px;
  animation: popIn 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.celebrate-coin {
  font-size: 56px;
  margin-bottom: 8px;
  animation: coinBounce 0.6s ease 0.3s;
}

.celebrate-title {
  font-size: 22px;
  font-weight: 700;
  color: #333;
  margin: 0 0 8px;
}

.celebrate-points {
  font-size: 28px;
  font-weight: 800;
  color: #F7971E;
  margin: 0 0 4px;
}

.celebrate-streak {
  font-size: 14px;
  color: #999;
  margin: 0 0 20px;

  strong { color: var(--primary-color); font-size: 18px; }
}

.celebrate-btn {
  width: 100%;
  height: 44px;
  border: none;
  border-radius: 22px;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, var(--primary-color), #009688);
  cursor: pointer;
  transition: transform 0.2s;

  &:active { transform: scale(0.95); }
}

@keyframes popIn {
  0% { transform: scale(0.3); opacity: 0; }
  100% { transform: scale(1); opacity: 1; }
}

@keyframes coinBounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-16px); }
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes fadeOut {
  from { opacity: 1; }
  to { opacity: 0; }
}

// ================================================================
// PC 端适配
// ================================================================
@media (min-width: 769px) {
  .checkin-page {
    max-width: 640px;
    margin: 0 auto;
    padding-top: 20px;
    background: transparent;
  }

  .checkin-hero { border-radius: 16px; }

  .date-cell {
    &:hover:not(.is-signed):not(.is-other-month) {
      background: rgba(0, 191, 166, 0.06);
    }
  }
}

// ================================================================
// 等级
// ================================================================
.level-stat {
  :deep(.el-progress) { margin-bottom: 2px; }
  .level-text {
    font-size: 11px;
    font-weight: 700;
    color: #43e97b;
  }
}

// ================================================================
// 骨架日历
// ================================================================
.date-cell.is-skeleton {
  display: flex;
  align-items: center;
  justify-content: center;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
</style>
