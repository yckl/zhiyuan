<template>
  <div class="dashboard-container" v-loading="loading">
    <!-- 1. 顶部数据卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="12" :md="12" :lg="6">
        <el-card shadow="hover" class="stats-card blue-card">
          <div class="stats-content">
            <div class="icon-wrapper">
              <el-icon><User /></el-icon>
            </div>
            <div class="info">
              <div class="label">总志愿者</div>
              <div class="value">{{ summary.totalVolunteers || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="12" :lg="6">
        <el-card shadow="hover" class="stats-card green-card">
          <div class="stats-content">
            <div class="icon-wrapper">
              <el-icon><Timer /></el-icon>
            </div>
            <div class="info">
              <div class="label">累计服务时长</div>
              <div class="value">{{ summary.totalServiceHours || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="12" :lg="6">
        <el-card shadow="hover" class="stats-card purple-card">
          <div class="stats-content">
            <div class="icon-wrapper">
              <el-icon><Flag /></el-icon>
            </div>
            <div class="info">
              <div class="label">进行中活动</div>
              <div class="value">{{ summary.ongoingActivities || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="12" :lg="6">
        <el-card shadow="hover" class="stats-card red-card">
          <div class="stats-content">
            <div class="icon-wrapper">
              <el-icon><Bell /></el-icon>
            </div>
            <div class="info">
              <div class="label">待办事项</div>
              <div class="value">{{ summary.pendingAudits || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 2. 中间图表区 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :xs="24" :sm="24" :md="14" :lg="14">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>近 7 天活动报名趋势</span>
            </div>
          </template>
          <div ref="lineChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="10" :lg="10">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>活动分类分布</span>
            </div>
          </template>
          <div ref="pieChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 3. 底部任务列表 -->
    <el-row class="tasks-row">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>待处理审批</span>
              <el-button text type="primary">查看全部</el-button>
            </div>
          </template>
          <el-table :data="pendingTasks" style="width: 100%" stripe>
            <el-table-column prop="type" label="任务类型" width="120">
              <template #default="scope">
                <el-tag :type="scope.row.type === '组织认证' ? 'warning' : 'danger'">
                  {{ scope.row.type }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="source" label="来源" min-width="150" />
            <el-table-column prop="submitTime" label="申请时间" width="180">
              <template #default="scope">
                {{ formatTime(scope.row.submitTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                 <el-tag size="small" type="info">{{ scope.row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button link type="primary" @click="handleAction(scope.row)">处理</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { User, Timer, Flag, Bell } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import request from '@/utils/request'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(true)

// Data Refs
const summary = ref({
  totalVolunteers: 0,
  totalServiceHours: 0,
  ongoingActivities: 0,
  pendingAudits: 0
})

const pendingTasks = ref([])

// Charts Refs
const lineChartRef = ref<HTMLElement | null>(null)
const pieChartRef = ref<HTMLElement | null>(null)
let lineChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

// Utils
const formatTime = (time: string) => dayjs(time).format('YYYY-MM-DD HH:mm')

// Fetch Data
const fetchData = async () => {
  loading.value = true
  try {
    // 1. Summary
    const resSummary = await request.get('/admin/dashboard/stats/summary')
    summary.value = resSummary.data

    // 2. Tasks
    const resTasks = await request.get('/admin/dashboard/tasks/pending')
    pendingTasks.value = resTasks.data
    
    // 3. Charts Data
    const resCharts = await request.get('/admin/dashboard/stats/charts')
    initCharts(resCharts.data)
  } catch (error) {
    console.error('Fetch dashboard data failed', error)
  } finally {
    loading.value = false
  }
}

// Charts Init
const initCharts = (data: any) => {
  if (!lineChartRef.value || !pieChartRef.value) return

  // Line Chart
  lineChart = echarts.init(lineChartRef.value)
  const lineOption = {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: data.trendDates },
    yAxis: { type: 'value' },
    series: [
      {
        name: '报名人次',
        type: 'line',
        smooth: true,
        data: data.trendValues,
        areaStyle: { opacity: 0.1 },
        itemStyle: { color: '#409EFF' },
        lineStyle: { color: '#409EFF' }
      }
    ]
  }
  lineChart.setOption(lineOption)

  // Pie Chart
  pieChart = echarts.init(pieChartRef.value)
  const pieOption = {
    tooltip: { trigger: 'item' },
    legend: { bottom: '0%', left: 'center' },
    series: [
      {
        name: '活动分类',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: { show: false, position: 'center' },
        emphasis: {
          label: { show: true, fontSize: 20, fontWeight: 'bold' }
        },
        data: data.pieData
      }
    ]
  }
  pieChart.setOption(pieOption)
}

// Resize Handler
const handleResize = () => {
  lineChart?.resize()
  pieChart?.resize()
}

// Dark Mode Handling (Simple Observer)
let observer: MutationObserver | null = null

onMounted(() => {
  fetchData()
  window.addEventListener('resize', handleResize)
  
  // Observe html class for dark mode
  const html = document.documentElement
  observer = new MutationObserver((mutations) => {
    mutations.forEach((mutation) => {
      if (mutation.attributeName === 'class') {
         // const isDark = html.classList.contains('dark')
         // TODO: Switch ECharts theme if necessary
         // Currently ECharts transparent background adapts well with CSS handling container bg.
      }
    })
  })
  observer.observe(html, { attributes: true })
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  observer?.disconnect()
  lineChart?.dispose()
  pieChart?.dispose()
})

const handleAction = (task: any) => {
  if (task.actionUrl) {
    router.push(task.actionUrl)
  }
}
</script>

<style scoped lang="scss">
.dashboard-container {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stats-card {
  height: 100px;
  display: flex;
  align-items: center;
  border: none;
  transition: transform 0.3s;
  
  &:hover {
    transform: translateY(-5px);
  }

  .stats-content {
    display: flex;
    align-items: center;
    padding: 0 20px;
    width: 100%;
  }

  .icon-wrapper {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 15px;
    font-size: 28px;
    color: white;
  }

  .info {
    flex: 1;
    .label {
      font-size: 14px;
      color: rgba(255, 255, 255, 0.9);
      margin-bottom: 5px;
    }
    .value {
      font-size: 24px;
      font-weight: bold;
      color: white;
    }
  }
}

// Card Colors
.blue-card {
  background: linear-gradient(135deg, #409EFF, #36D1DC);
  .icon-wrapper { background: rgba(255, 255, 255, 0.2); }
}

.green-card {
  background: linear-gradient(135deg, #67C23A, #85E89D);
  .icon-wrapper { background: rgba(255, 255, 255, 0.2); }
}

.purple-card {
  background: linear-gradient(135deg, #a18cd1, #CA72D1); // Purple
  .icon-wrapper { background: rgba(255, 255, 255, 0.2); }
}

.red-card {
  background: linear-gradient(135deg, #F56C6C, #fbc2eb);
  .icon-wrapper { background: rgba(255, 255, 255, 0.2); }
}

.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  .chart-container {
    height: 350px;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

// Dark Mode Adaptation
:deep(.dark) {
  .stats-card {
    opacity: 0.9;
  }
  .chart-card {
    background-color: var(--el-bg-color-overlay);
  }
}

// 移动端响应式适配 - 2x2 田字格布局
@media (max-width: 768px) {
  .dashboard-container {
    padding: 8px;
  }
  
  .stats-row {
    margin-bottom: 12px;
    
    // 减小 gutter
    .el-col {
      padding-left: 6px !important;
      padding-right: 6px !important;
    }
  }
  
  .stats-card {
    height: 70px;
    margin-bottom: 10px;
    border-radius: 8px;
    
    :deep(.el-card__body) {
      padding: 8px !important;
    }
    
    .stats-content {
      padding: 0 8px;
    }
    
    .icon-wrapper {
      width: 36px;
      height: 36px;
      font-size: 18px;
      margin-right: 8px;
    }
    
    .info {
      .label { font-size: 11px; }
      .value { font-size: 16px; }
    }
  }
  
  .chart-card {
    margin-bottom: 12px;
    
    .chart-container {
      height: 220px;
    }
  }
}
</style>
