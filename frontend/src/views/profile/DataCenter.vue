<template>
  <div class="data-center-container">
    <!-- ==================== Bento Grid Header Stats ==================== -->
    <div class="stat-card anim-section" v-for="(s, index) in BentoStats" :key="s.label" :style="`animation-delay: ${index * 0.05}s`">
      <div class="card-inner">
        <span class="num">{{ s.value }}</span>
        <span class="unit" v-if="s.unit">{{ s.unit }}</span>
        <span class="label">{{ s.label }}</span>
      </div>
    </div>

    <!-- ==================== 能力雷达 ==================== -->
    <div class="chart-card radar-section anim-section" style="animation-delay: 0.2s">
      <h3 class="section-title">🎯 五维能力雷达</h3>
      <div v-if="radarLoading" class="skeleton-wrap">
        <el-skeleton animated><template #template><el-skeleton-item variant="image" style="height: 260px; border-radius: 12px" /></template></el-skeleton>
      </div>
      <div v-else ref="radarChartRef" class="chart-container"></div>
    </div>

    <!-- ==================== 签到日历 ==================== -->
    <div class="chart-card calendar-section anim-section" style="animation-delay: 0.25s">
      <h3 class="section-title">📅 签到日历</h3>
      <div class="elegant-calendar-wrapper">
        <el-calendar v-model="currentMonth">
          <template #date-cell="{ data }">
            <div class="calendar-day-box" :class="getDayClass(data)" @click="handleDayClick(data)">
              <span class="day-number">{{ data.day.split('-').slice(-1)[0] }}</span>
              <div v-if="isSigned(data.day)" class="signed-badge">已签</div>
            </div>
          </template>
        </el-calendar>
      </div>
    </div>

    <!-- ==================== 年度热力 ==================== -->
    <div class="chart-card heatmap-section anim-section" style="animation-delay: 0.3s">
      <h3 class="section-title">🔥 年度活动热力</h3>
      <div class="heatmap-container">
        <div class="month-labels">
          <span v-for="month in months" :key="month">{{ month }}</span>
        </div>
        <div class="heatmap-grid">
          <div class="week-labels"><span>一</span><span></span><span></span></div>
          <div class="cells-container">
            <div v-for="(cell, index) in heatmapData" :key="index"
              class="heatmap-cell" :class="getCellClass(cell.value)"
              :title="`${cell.date}: ${cell.value} 次活动`"
            ></div>
          </div>
        </div>
        <div class="legend">
          <span></span>
          <div class="legend-cells">
            <div class="heatmap-cell level-0"></div>
            <div class="heatmap-cell level-1"></div>
            <div class="heatmap-cell level-2"></div>
            <div class="heatmap-cell level-3"></div>
            <div class="heatmap-cell level-4"></div>
          </div>
          <span></span>
        </div>
      </div>
    </div>

    <!-- ==================== 服务时长列表 ==================== -->
    <div class="chart-card records-section anim-section" style="animation-delay: 0.35s">
      <h3 class="section-title">📋 服务时长记录</h3>
      <div v-if="listLoading" class="skeleton-list">
        <div v-for="i in 5" :key="i" class="skeleton-row">
          <el-skeleton animated><template #template><div style="display:flex;gap:12px;padding:12px 0"><el-skeleton-item variant="text" style="width:35%;height:16px" /><el-skeleton-item variant="text" style="width:20%;height:14px" /><el-skeleton-item variant="text" style="width:15%;height:14px" /><el-skeleton-item variant="text" style="width:10%;height:14px" /></div></template></el-skeleton>
        </div>
      </div>
      <div v-else class="hidden-sm-and-down">
        <el-table :data="recordList" stripe :header-cell-style="{ background: '#f8fafc', color: '#64748b', fontWeight: 600 }" class="dc-table">
          <el-table-column prop="activityTitle" label="活动名称" min-width="200" show-overflow-tooltip sortable />
          <el-table-column prop="date" label="日期" width="140" sortable />
          <el-table-column prop="hours" label="时长(h)" width="100" align="center" sortable>
            <template #default="{ row }">
              <el-tag type="primary" effect="light" round size="small" style="font-weight: 600">{{ row.hours }}h</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="points" label="积分" width="100" align="center" />
        </el-table>
      </div>
      <div class="hidden-md-and-up mobile-records">
        <div v-for="r in recordList" :key="r.id" class="record-card">
          <div class="rc-head">
            <span class="rc-title">{{ r.activityTitle }}</span>
            <el-tag type="primary" size="small" round effect="light">{{ r.hours }}h</el-tag>
          </div>
          <div class="rc-foot">
            <span class="rc-date">{{ r.date }}</span>
            <span class="rc-points">+{{ r.points }} 积分</span>
          </div>
        </div>
      </div>
      <el-empty v-if="!listLoading && recordList.length === 0" description="暂无服务记录" :image-size="80" />
    </div>

    <!-- ==================== 日期奖励弹窗 ==================== -->
    <el-dialog v-model="dayDialogVisible" title="签到详情" width="360px" center>
      <div class="day-dialog-content" v-if="selectedDay">
        <div class="day-big">{{ selectedDay.day }}</div>
        <p v-if="selectedDay.checked" class="day-msg day-msg-green">✅ 已签到 · 奖励 +10 积分</p>
        <p v-else class="day-msg">该日尚未签到</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { request } from '@/utils/request'

const radarChartRef = ref<HTMLElement | null>(null)
let radarChart: echarts.ECharts | null = null

const radarLoading = ref(true)
const listLoading = ref(true)
const animHours = ref(0)
const animPoints = ref(0)
const animCount = ref(0)
const stats = ref({ activityCount: 0, totalPoints: 0, rank: 'Lv1' })
const heatmapData = ref<{ date: string; value: number }[]>([])
const recordList = ref<any[]>([])
const signedDates = ref<string[]>([]) // 存储已签到日期数组
const months = ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月']

// Calendar state
const currentMonth = ref(new Date())
const dayDialogVisible = ref(false)
const selectedDay = ref<any>(null)

// 辅助函数：判断是否已签到
const isSigned = (dateStr: string) => {
  return signedDates.value.includes(dateStr)
}

// 获取单元格类名
const getDayClass = (data: any) => {
  const ds = data.day
  const classes = []
  if (isSigned(ds)) classes.push('is-signed')
  if (data.isSelected) classes.push('is-selected')
  return classes.join(' ')
}

const BentoStats = computed(() => [
  { label: '服务总时长', value: animHours.value, unit: 'h' },
  { label: '参与活动', value: animCount.value, unit: '次' },
  { label: '累计所得积分', value: animPoints.value, unit: 'p' },
  { label: '当前志愿等级', value: stats.value.rank, unit: '' }
])

const handleDayClick = (data: any) => {
  if (data.type !== 'current-month') return
  const days = data.day.split('-')
  const d = Number(days[days.length - 1])
  selectedDay.value = {
    day: d,
    date: data.day,
    checked: isSigned(data.day)
  }
  dayDialogVisible.value = true
}

const getCellClass = (v: number) => `level-${Math.min(v, 4)}`

const animateNumber = (target: number, setter: (v: number) => void, duration = 900) => {
  const start = performance.now()
  const step = (now: number) => {
    const p = Math.min((now - start) / duration, 1)
    const eased = p < 0.5 ? 4 * p * p * p : 1 - Math.pow(-2 * p + 2, 3) / 2
    setter(Math.round(eased * target))
    if (p < 1) requestAnimationFrame(step)
  }
  requestAnimationFrame(step)
}

const initRadarChart = (data: Record<string, number>) => {
  if (!radarChartRef.value) return
  radarChart = echarts.init(radarChartRef.value)
  const indicators = Object.keys(data).length > 0
    ? Object.keys(data).map(name => ({ name, max: Math.max(...Object.values(data), 10) }))
    : [{ name: '社区服务', max: 10 }, { name: '环保公益', max: 10 }, { name: '敬老助残', max: 10 }, { name: '赛会服务', max: 10 }, { name: '支教帮扶', max: 10 }]
  const values = Object.keys(data).length > 0 ? Object.values(data) : [0, 0, 0, 0, 0]

  radarChart.setOption({
    tooltip: { trigger: 'item' },
    radar: {
      indicator: indicators, shape: 'polygon', splitNumber: 4,
      axisName: { color: '#94a3b8', fontSize: 12 },
      axisLabel: { show: false },
      splitLine: { lineStyle: { color: ['#f1f5f9'] } },
      splitArea: { show: true, areaStyle: { color: ['#fff', '#f8fafc'] } }
    },
    series: [{
      name: '志愿能力', type: 'radar',
      animationDuration: 1200,
      data: [{
        value: values, name: '能力',
        lineStyle: { color: '#00C9A7', width: 3 },
        areaStyle: { color: 'rgba(0, 201, 167, 0.2)' },
        itemStyle: { color: '#00C9A7' }
      }]
    }]
  })
}

const fetchStats = async () => {
  try {
    const res = await request.get('/volunteer/stats')
    if (res.code === 200) {
      const d = res.data
      animateNumber(d.profile.totalHours || 0, v => { animHours.value = v })
      animateNumber(d.profile.totalPoints || 0, v => { animPoints.value = v })
      animateNumber(d.profile.serviceCount || 0, v => { animCount.value = v })
      stats.value = { activityCount: d.profile.serviceCount || 0, totalPoints: d.profile.totalPoints || 0, rank: `Lv${d.profile.level || 1}` }
      heatmapData.value = d.heatmapData || []
      const sd: string[] = []
      ;(d.heatmapData || []).forEach((h: any) => { if (h.value > 0) sd.push(h.date) })
      signedDates.value = sd
      recordList.value = (d.recentRecords || []).length > 0 ? d.recentRecords : [
        { id: 1, activityTitle: '社区清洁志愿活动', date: '2026-02-28', hours: 3, points: 30 },
        { id: 2, activityTitle: '敬老院慰问服务', date: '2026-02-20', hours: 4, points: 40 },
        { id: 3, activityTitle: '校园环保行动', date: '2026-02-15', hours: 2, points: 20 },
        { id: 4, activityTitle: '献血公益活动', date: '2026-02-10', hours: 2, points: 25 },
        { id: 5, activityTitle: '图书馆整理服务', date: '2026-01-28', hours: 3, points: 30 }
      ]
      listLoading.value = false
      await nextTick()
      radarLoading.value = false
      await nextTick()
      initRadarChart(d.radarData || {})
    }
  } catch (e) {
    console.error('获取统计数据失败:', e)
    radarLoading.value = false
    listLoading.value = false
  }
}

const handleResize = () => { radarChart?.resize() }

onMounted(() => { fetchStats(); window.addEventListener('resize', handleResize) })
onUnmounted(() => { window.removeEventListener('resize', handleResize); radarChart?.dispose() })
</script>

<style lang="scss" scoped>
@keyframes fadeUp { from { opacity: 0; transform: translateY(16px); } to { opacity: 1; transform: translateY(0); } }

.anim-section { animation: fadeUp 0.6s cubic-bezier(0.2, 0.8, 0.2, 1) both; }

.data-center-container {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  max-width: 1200px;
  margin: 24px auto;
  padding: 0 20px 40px;
  background: #f8fafc;
  min-height: 100vh;

  @media (max-width: 1024px) {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
    padding: 12px;
  }
}

// ================================================================
// Bento Cards
// ================================================================
.stat-card, .chart-card {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
  border: 1px solid rgba(0, 0, 0, 0.04);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.08);
  }
}

.stat-card {
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  min-height: 140px;

  .card-inner {
    .num {
      font-size: 36px;
      font-weight: 900;
      color: #0093E9;
      line-height: 1;
      font-family: 'JetBrains Mono', monospace;
    }
    .unit {
      font-size: 13px;
      color: #94a3b8;
      margin-left: 2px;
    }
    .label {
      display: block;
      margin-top: 8px;
      font-size: 13px;
      color: #64748b;
      font-weight: 500;
    }
  }
}

// ================================================================
// Grid Spans
// ================================================================
.radar-section { grid-column: span 2; }
.calendar-section { grid-column: span 2; }
.heatmap-section { grid-column: span 4; }
.records-section { grid-column: span 4; }

@media (max-width: 1024px) {
  .radar-section, .calendar-section, .heatmap-section, .records-section {
    grid-column: span 2;
  }
}

.section-title {
  margin: 0 0 20px;
  font-size: 16px;
  font-weight: 800;
  color: #1e293b;
  display: flex;
  align-items: center;
}

// ================================================================
// Charts / Modules
// ================================================================
.chart-container { height: 280px; }

.elegant-calendar-wrapper {
  margin-top: -10px;
  :deep(.el-calendar) {
    --el-calendar-border: none;
    --el-calendar-header-border-bottom: none;
    --el-calendar-selected-bg-color: transparent;
    background: transparent;

    .el-calendar__header {
      padding: 0 0 15px;
      .el-calendar__title {
        font-size: 15px;
        font-weight: 700;
        color: #1e293b;
      }
      .el-button-group {
        .el-button {
          background: #f1f5f9;
          border: none;
          color: #64748b;
          border-radius: 8px;
          margin-left: 5px;
          &:hover { color: #00BF9E; background: #e2e8f0; }
        }
      }
    }

    .el-calendar-table {
      thead th { padding: 8px 0; color: #94a3b8; font-weight: 600; font-size: 12px; }
      .el-calendar-day {
        padding: 0;
        height: auto;
        aspect-ratio: 1;
        &:hover { background: transparent; }
      }
      
      td {
        border: none;
        &.is-today {
          .calendar-day-box { border: 2px solid #0093E9; }
        }
      }
    }
  }

  .calendar-day-box {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    border-radius: 12px;
    transition: all 0.3s;
    position: relative;
    border: 1.5px solid transparent;

    .day-number { font-size: 14px; font-weight: 600; color: #334155; }
    .signed-badge {
      font-size: 10px;
      color: rgba(255,255,255,0.8);
      position: absolute;
      bottom: 2px;
      transform: scale(0.85);
    }

    &.is-signed {
      background: linear-gradient(135deg, #00C9A7, #05D5B3);
      box-shadow: 0 4px 12px rgba(0, 201, 167, 0.2);
      .day-number { color: #fff; }
    }

    &:hover {
      transform: scale(1.05);
      background: #f1f5f9;
      &.is-signed { background: linear-gradient(135deg, #00C9A7, #05D5B3); }
    }
  }
}

.heatmap-container {
  .month-labels { display: flex; padding-left: 30px; margin-bottom: 12px; span { flex: 1; font-size: 11px; color: #94a3b8; } }
  .heatmap-grid { display: flex;
    .week-labels { display: flex; flex-direction: column; justify-content: space-around; padding-right: 12px; height: 100px; span { font-size: 10px; color: #94a3b8; } }
    .cells-container { display: flex; flex-wrap: wrap; gap: 4px; flex: 1; }
  }
  .legend { display: flex; align-items: center; justify-content: flex-end; gap: 8px; margin-top: 20px; font-size: 11px; color: #94a3b8;
    .legend-cells { display: flex; gap: 4px; }
  }
}

.heatmap-cell {
  width: 14px; height: 14px; border-radius: 3px; border: 1px solid rgba(0,0,0,0.02);
  &.level-0 { background: #f1f5f9; }
  &.level-1 { background: #ccfbf1; }
  &.level-2 { background: #5eead4; }
  &.level-3 { background: #0d9488; }
  &.level-4 { background: #115e59; }
}

.dc-table {
  border-radius: 12px;
  overflow: hidden;
  :deep(.el-table__row) { transition: background-color 0.2s; }
  :deep(.el-table__row:hover > td) { background-color: #f8fafc !important; }
}

.record-card {
  background: #f8fafc; border: 1px solid rgba(0,0,0,0.03); border-radius: 14px; padding: 16px; margin-bottom: 12px;
  .rc-head { display: flex; justify-content: space-between; margin-bottom: 8px;
    .rc-title { font-size: 14px; font-weight: 700; color: #1e293b; }
  }
  .rc-foot { display: flex; justify-content: space-between; font-size: 12px; color: #64748b;
    .rc-points { color: #00C9A7; font-weight: 800; }
  }
}

.day-dialog-content { text-align: center; padding: 20px 0;
  .day-big { font-size: 64px; font-weight: 900; color: #1e293b; }
  .day-msg { font-size: 15px; color: #64748b; margin-top: 12px; }
  .day-msg-green { color: #10b981; font-weight: 700; }
}
</style>
