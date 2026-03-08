<template>
  <div class="data-statistics-page">
    <div class="page-header">
      <h2>📊 数据统计</h2>
      <p class="subtitle">系统运营数据概览</p>
    </div>

    <!-- 核心指标卡片 -->
    <div class="stats-cards" v-loading="loading.core">
      <div class="stat-card">
        <div class="stat-icon volunteer">
          <el-icon><User /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ statsData.volunteerCount }}</span>
          <span class="stat-label">志愿者总数</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon activity">
          <el-icon><Calendar /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ statsData.activityCount }}</span>
          <span class="stat-label">活动总数</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon hours">
          <el-icon><Timer /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ formatHours(statsData.totalHours) }}</span>
          <span class="stat-label">服务总时长</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon registration">
          <el-icon><DocumentAdd /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ statsData.newRegistration }}</span>
          <span class="stat-label">今日报名</span>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-row">
      <!-- 活动分类饼图 -->
      <div class="chart-card" v-loading="loading.category">
        <h3>活动分类分布</h3>
        <div ref="pieChartRef" class="chart-container"></div>
      </div>

      <!-- 学院排行柱状图 -->
      <div class="chart-card" v-loading="loading.college">
        <h3>Top 10 活跃学院</h3>
        <div ref="barChartRef" class="chart-container"></div>
      </div>
    </div>

    <!-- 趋势?-->
    <div class="trend-card" v-loading="loading.trend">
      <h3>近 6 个月报名趋势</h3>
      <div ref="lineChartRef" class="chart-container-wide"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { User, Calendar, Timer, DocumentAdd } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { request } from '@/utils/request'

// 图表 DOM 引用
const pieChartRef = ref<HTMLElement | null>(null)
const barChartRef = ref<HTMLElement | null>(null)
const lineChartRef = ref<HTMLElement | null>(null)

// 图表实例
let pieChart: echarts.ECharts | null = null
let barChart: echarts.ECharts | null = null
let lineChart: echarts.ECharts | null = null

// 加载状态
const loading = reactive({
  core: false,
  category: false,
  college: false,
  trend: false
})

// 核心指标数据
const statsData = reactive({
  volunteerCount: 0,
  activityCount: 0,
  totalHours: 0,
  newRegistration: 0,
  monthlyActivities: 0,
  monthlyRegistrations: 0
})

// 饼图数据
const pieData = ref<{ name: string; value: number }[]>([])

// 柱状图数据
const barData = reactive({
  colleges: [] as string[],
  counts: [] as number[]
})

// 趋势数据
const trendData = ref<{ month: string; count: number }[]>([])

// 格式化时长
const formatHours = (hours: number) => {
  if (hours >= 10000) {
    return (hours / 10000).toFixed(1) + '万h'
  }
  return hours.toLocaleString() + 'h'
}

// 获取核心指标
const fetchCoreStats = async () => {
  loading.core = true
  try {
    const res = await request.get('/statistics/core')
    if (res.code === 200 && res.data) {
      Object.assign(statsData, res.data)
    }
  } catch (error) {
    console.error('获取核心指标失败:', error)
  } finally {
    loading.core = false
  }
}

// 获取分类统计
const fetchCategoryStats = async () => {
  loading.category = true
  try {
    const res = await request.get('/statistics/category')
    if (res.code === 200 && res.data) {
      pieData.value = res.data
    }
  } catch (error) {
    console.error('获取分类统计失败:', error)
  } finally {
    loading.category = false
  }
}

// 获取学院排行
const fetchCollegeStats = async () => {
  loading.college = true
  try {
    const res = await request.get('/statistics/college')
    if (res.code === 200 && res.data) {
      barData.colleges = res.data.colleges || []
      barData.counts = res.data.counts || []
    }
  } catch (error) {
    console.error('获取学院统计失败:', error)
  } finally {
    loading.college = false
  }
}

// 获取趋势数据
const fetchTrendStats = async () => {
  loading.trend = true
  try {
    const res = await request.get('/statistics/trend')
    if (res.code === 200 && res.data) {
      trendData.value = res.data
    }
  } catch (error) {
    console.error('获取趋势数据失败:', error)
  } finally {
    loading.trend = false
  }
}

// 初始化饼图
const initPieChart = () => {
  if (!pieChartRef.value || pieData.value.length === 0) return

  const isMobile = window.innerWidth < 768
  pieChart = echarts.init(pieChartRef.value)
  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: isMobile ? {
      bottom: '0%',
      left: 'center',
      itemWidth: 14,
      itemHeight: 14
    } : {
      orient: 'vertical',
      right: 20,
      top: 'middle'
    },
    color: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272'],
    series: [{
      name: '活动分类',
      type: 'pie',
      radius: isMobile ? ['35%', '60%'] : ['40%', '70%'],
      center: isMobile ? ['50%', '45%'] : ['40%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false
      },
      emphasis: {
        label: {
          show: true,
          fontSize: isMobile ? 16 : 20,
          fontWeight: 'bold'
        }
      },
      data: pieData.value
    }]
  }
  pieChart.setOption(option)
}

// 初始化柱状图
const initBarChart = () => {
  if (!barChartRef.value || barData.colleges.length === 0) return

  barChart = echarts.init(barChartRef.value)
  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      axisLabel: { color: '#909399' }
    },
    yAxis: {
      type: 'category',
      data: barData.colleges.slice().reverse(),
      axisLabel: { color: '#909399' }
    },
    series: [{
      name: '报名人次',
      type: 'bar',
      data: barData.counts.slice().reverse(),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#667eea' },
          { offset: 1, color: '#764ba2' }
        ]),
        borderRadius: [0, 4, 4, 0]
      },
      label: {
        show: true,
        position: 'right',
        formatter: '{c}'
      }
    }]
  }
  barChart.setOption(option)
}

// 初始化趋势图
const initLineChart = () => {
  if (!lineChartRef.value || trendData.value.length === 0) return

  lineChart = echarts.init(lineChartRef.value)
  const months = trendData.value.map(item => item.month)
  const counts = trendData.value.map(item => item.count)

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: months,
      axisLabel: { color: '#909399' }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#909399' }
    },
    series: [{
      name: '报名人次',
      type: 'line',
      smooth: true,
      data: counts,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(102, 126, 234, 0.5)' },
          { offset: 1, color: 'rgba(102, 126, 234, 0.05)' }
        ])
      },
      lineStyle: {
        color: '#667eea',
        width: 3
      },
      itemStyle: {
        color: '#667eea'
      }
    }]
  }
  lineChart.setOption(option)
}

// 监听数据变化更新图表
watch(pieData, () => {
  nextTick(initPieChart)
}, { deep: true })

watch(() => [barData.colleges, barData.counts], () => {
  nextTick(initBarChart)
}, { deep: true })

watch(trendData, () => {
  nextTick(initLineChart)
}, { deep: true })

// 窗口调整
const handleResize = () => {
  pieChart?.resize()
  barChart?.resize()
  lineChart?.resize()
  
  // 重新计算饼图布局
  if (pieChart) {
     const isMobile = window.innerWidth < 768
     pieChart.setOption({
        legend: isMobile ? {
           bottom: '0%',
           left: 'center',
           orient: 'horizontal',
           right: 'auto',
           top: 'auto'
        } : {
           orient: 'vertical',
           right: 20,
           top: 'middle',
           bottom: 'auto',
           left: 'auto'
        },
        series: [{
           radius: isMobile ? ['35%', '60%'] : ['40%', '70%'],
           center: isMobile ? ['50%', '45%'] : ['40%', '50%'],
           emphasis: {
              label: { fontSize: isMobile ? 16 : 20 }
           }
        }]
     })
  }
}

onMounted(async () => {
  // 并发请求所有数据
  await Promise.all([
    fetchCoreStats(),
    fetchCategoryStats(),
    fetchCollegeStats(),
    fetchTrendStats()
  ])

  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  pieChart?.dispose()
  barChart?.dispose()
  lineChart?.dispose()
})
</script>

<style lang="scss" scoped>
.data-statistics-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;

  h2 {
    margin: 0 0 8px;
    font-size: 24px;
    color: var(--text-primary);
  }

  .subtitle {
    margin: 0;
    color: var(--text-muted);
  }

  @media (max-width: 768px) {
    h2 { font-size: 20px; }
    .subtitle { font-size: 13px; }
    margin-bottom: 16px;
  }
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;

  @media (max-width: 1200px) {
    grid-template-columns: repeat(2, 1fr);
  }

  @media (max-width: 600px) {
    grid-template-columns: 1fr;
    gap: 12px;
  }
}

.stat-card {
  background: var(--bg-card);
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: var(--shadow-light);
  border: 1px solid var(--border-light);
  transition: all 0.3s;

  &:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow-main);
  }

  .stat-icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    color: #fff;

    &.volunteer {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }

    &.activity {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    }

    &.hours {
      background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    }

    &.registration {
      background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
    }
  }

  .stat-info {
    .stat-value {
      display: block;
      font-size: 28px;
      font-weight: bold;
      color: var(--text-primary);
      line-height: 1.2;
    }

    .stat-label {
      font-size: 14px;
      color: var(--text-secondary);
    }
  }
}

.charts-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
  margin-bottom: 24px;

  @media (max-width: 900px) {
    grid-template-columns: 1fr;
  }
}

.chart-card, .trend-card {
  background: var(--bg-card);
  border-radius: 12px;
  padding: 24px;
  box-shadow: var(--shadow-light);
  border: 1px solid var(--border-light);
  overflow: hidden; // Prevent scrollbars

  h3 {
    margin: 0 0 16px;
    font-size: 16px;
    color: var(--text-primary);
  }
}

.chart-container {
  height: 300px;
  width: 100%;
}

.chart-container-wide {
  height: 280px;
  width: 100%;
}

@media (max-width: 768px) {
  .chart-container {
    height: 320px; // Increase height for mobile to fit legend
  }
  
  .chart-card, .trend-card {
     padding: 16px; // Reduce padding on mobile
  }
}
</style>
