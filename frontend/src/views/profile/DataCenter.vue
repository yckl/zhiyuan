<template>
  <div class="data-center-page">
    <div class="page-header">
      <h2>📊 数据中心</h2>
      <p class="subtitle">您的志愿服务数据一览</p>
    </div>

    <!-- 上方两栏布局 -->
    <div class="top-section">
      <!-- 左侧：五维能力雷达图 -->
      <div class="radar-card">
        <h3>五维能力雷达图</h3>
        <div ref="radarChartRef" class="chart-container"></div>
      </div>

      <!-- 右侧：累计服务时长 -->
      <div class="hours-card">
        <div class="hours-content">
          <span class="hours-label">累计服务时长</span>
          <div class="hours-value">
            <span class="number">{{ totalHours }}</span>
            <span class="unit">小时</span>
          </div>
          <div class="hours-stats">
            <div class="stat-item">
              <span class="stat-value">{{ stats.activityCount }}</span>
              <span class="stat-label">参与活动</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ stats.totalPoints }}</span>
              <span class="stat-label">累计积分</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ stats.rank }}</span>
              <span class="stat-label">志愿等级</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 下方通栏：活跃热力图 -->
    <div class="heatmap-card">
      <h3>📅 年度活动热力图</h3>
      <div class="heatmap-container">
        <div class="month-labels">
          <span v-for="month in months" :key="month">{{ month }}</span>
        </div>
        <div class="heatmap-grid">
          <div class="week-labels">
            <span>一</span>
            <span>三</span>
            <span>五</span>
          </div>
          <div class="cells-container">
            <div
              v-for="(cell, index) in heatmapData"
              :key="index"
              class="heatmap-cell"
              :class="getCellClass(cell.value)"
              :title="`${cell.date}: ${cell.value} 次活动`"
            ></div>
          </div>
        </div>
        <div class="legend">
          <span>少</span>
          <div class="legend-cells">
            <div class="heatmap-cell level-0"></div>
            <div class="heatmap-cell level-1"></div>
            <div class="heatmap-cell level-2"></div>
            <div class="heatmap-cell level-3"></div>
            <div class="heatmap-cell level-4"></div>
          </div>
          <span>多</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { request } from '@/utils/request'

const radarChartRef = ref<HTMLElement | null>(null)
let radarChart: echarts.ECharts | null = null

const totalHours = ref(0)
const stats = ref({
  activityCount: 0,
  totalPoints: 0,
  rank: 'Lv1'
})

const months = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']

// 由后端提供热力图数据
const heatmapData = ref<{ date: string; value: number }[]>([])

const getCellClass = (value: number) => {
  return `level-${Math.min(value, 4)}`
}

const initRadarChart = (data: Record<string, number>) => {
  if (!radarChartRef.value) return

  radarChart = echarts.init(radarChartRef.value)

  const indicators = Object.keys(data).length > 0 
    ? Object.keys(data).map(name => ({ name, max: Math.max(...Object.values(data), 10) }))
    : [
        { name: '社区服务', max: 10 },
        { name: '环保公益', max: 10 },
        { name: '敬老助残', max: 10 },
        { name: '赛会服务', max: 10 },
        { name: '支教帮扶', max: 10 }
      ]

  const values = Object.keys(data).length > 0 ? Object.values(data) : [0, 0, 0, 0, 0]

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      data: ['能力值'],
      bottom: 0
    },
    radar: {
      indicator: indicators,
      shape: 'polygon',
      splitNumber: 4,
      axisName: {
        color: '#666'
      },
      axisTick: {
        show: false
      },
      axisLabel: {
        show: false
      },
      splitLine: {
        lineStyle: {
          color: ['#e5e5e5']
        }
      },
      splitArea: {
        show: true,
        areaStyle: {
          color: ['rgba(102, 126, 234, 0.05)', 'rgba(102, 126, 234, 0.1)']
        }
      }
    },
    series: [{
      name: '志愿能力',
      type: 'radar',
      data: [
        {
          value: values,
          name: '能力值',
          lineStyle: {
            color: '#667eea',
            width: 2
          },
          areaStyle: {
            color: 'rgba(102, 126, 234, 0.3)'
          },
          itemStyle: {
            color: '#667eea'
          }
        }
      ]
    }]
  }

  radarChart.setOption(option)
}

const handleResize = () => {
  radarChart?.resize()
}

const fetchStats = async () => {
  try {
    const res = await request.get('/volunteer/stats')
    if (res.code === 200) {
      const data = res.data
      totalHours.value = data.profile.totalHours || 0
      stats.value = {
        activityCount: data.profile.serviceCount || 0,
        totalPoints: data.profile.totalPoints || 0,
        rank: `Lv${data.profile.level || 1}`
      }
      
      // 填充热力图
      heatmapData.value = data.heatmapData || []
      
      // 初始化雷达图
      initRadarChart(data.radarData || {})
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

onMounted(() => {
  fetchStats()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  radarChart?.dispose()
})
</script>

<style lang="scss" scoped>
.data-center-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;

  h2 {
    margin: 0 0 8px;
    font-size: 24px;
  }

  .subtitle {
    margin: 0;
    color: #999;
  }
}

.top-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 24px;

  @media (max-width: 900px) {
    grid-template-columns: 1fr;
  }
}

.radar-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);

  h3 {
    margin: 0 0 16px;
    font-size: 16px;
    color: #333;
  }

  .chart-container {
    height: 300px;
  }
}

.hours-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  justify-content: center;

  .hours-content {
    text-align: center;
    color: #fff;

    .hours-label {
      display: block;
      font-size: 14px;
      opacity: 0.8;
      margin-bottom: 12px;
    }

    .hours-value {
      margin-bottom: 24px;

      .number {
        font-size: 72px;
        font-weight: bold;
        line-height: 1;
      }

      .unit {
        font-size: 24px;
        margin-left: 8px;
      }
    }

    .hours-stats {
      display: flex;
      gap: 32px;
      justify-content: center;
      padding-top: 16px;
      border-top: 1px solid rgba(255, 255, 255, 0.2);

      .stat-item {
        text-align: center;

        .stat-value {
          display: block;
          font-size: 24px;
          font-weight: bold;
        }

        .stat-label {
          font-size: 12px;
          opacity: 0.8;
        }
      }
    }
  }
}

.heatmap-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);

  h3 {
    margin: 0 0 20px;
    font-size: 16px;
    color: #333;
  }
}

.heatmap-container {
  .month-labels {
    display: flex;
    padding-left: 24px;
    margin-bottom: 8px;

    span {
      flex: 1;
      font-size: 12px;
      color: #999;
    }
  }

  .heatmap-grid {
    display: flex;

    .week-labels {
      display: flex;
      flex-direction: column;
      justify-content: space-around;
      padding-right: 8px;
      height: 84px;

      span {
        font-size: 10px;
        color: #999;
      }
    }

    .cells-container {
      display: flex;
      flex-wrap: wrap;
      gap: 3px;
      flex: 1;
    }
  }

  .legend {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 6px;
    margin-top: 12px;
    font-size: 12px;
    color: #999;

    .legend-cells {
      display: flex;
      gap: 3px;
    }
  }
}

.heatmap-cell {
  width: 12px;
  height: 12px;
  border-radius: 2px;
  transition: transform 0.1s;

  &:hover {
    transform: scale(1.3);
  }

  &.level-0 {
    background: #ebedf0;
  }
  &.level-1 {
    background: #9be9a8;
  }
  &.level-2 {
    background: #40c463;
  }
  &.level-3 {
    background: #30a14e;
  }
  &.level-4 {
    background: #216e39;
  }
}
</style>
