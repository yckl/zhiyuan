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

    <!-- 2. 中间图表 -->
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
              <span>待处理审核</span>
              <el-button text type="primary">查看全部</el-button>
            </div>
          </template>
          <el-table :data="pendingTasks" style="width: 100%" stripe class="hidden-sm-and-down">
            <el-table-column prop="type" label="任务类型" width="120">
              <template #default="scope">
                <el-tag :type="scope.row.type === '组织认证' ? 'warning' : 'danger'">
                  {{ scope.row.type }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="source" label="来源" min-width="150" show-overflow-tooltip />
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

          <!-- Mobile Card List -->
          <div class="hidden-md-and-up mobile-task-list">
             <div v-for="(task, index) in pendingTasks" :key="index" class="mobile-task-card">
                <div class="card-header">
                   <div class="header-left">
                      <el-tag :type="task.type === '组织认证' ? 'warning' : 'danger'" size="small">
                         {{ task.type }}
                      </el-tag>
                      <span class="source-text">{{ task.source }}</span>
                   </div>
                   <el-tag size="small" type="info">{{ task.status }}</el-tag>
                </div>
                
                <div class="card-body">
                   <div class="info-row">
                      <span class="label">申请时间:</span>
                      <span class="value">{{ formatTime(task.submitTime) }}</span>
                   </div>
                </div>

                <div class="card-footer">
                   <el-button link type="primary" size="small" @click="handleAction(task)">
                      立即处理 <el-icon class="el-icon--right"><ArrowRight /></el-icon>
                   </el-button>
                </div>
             </div>
             <el-empty v-if="pendingTasks.length === 0" description="暂无待办事项" :image-size="60" />
          </div>
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

interface Task {
  type: string;
  source: string;
  status: string;
  submitTime: string;
  actionUrl?: string;
}

// Data Refs
const summary = ref({
  totalVolunteers: 0,
  totalServiceHours: 0,
  ongoingActivities: 0,
  pendingAudits: 0
})

const pendingTasks = ref<Task[]>([])

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
    grid: { 
       left: '3%', 
       right: '4%', 
       bottom: '3%', 
       top: '15%', // 增加顶部间距防止标题重叠
       containLabel: true 
    },
    xAxis: { 
      type: 'category', 
      boundaryGap: false, 
      data: data.trendDates,
      axisLabel: { color: '#909399' }
    },
    yAxis: { 
      type: 'value',
      axisLabel: { color: '#909399' },
      splitLine: {
        lineStyle: {
          color: 'rgba(255, 255, 255, 0.1)' // 弱化分割?
        }
      }
    },
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
  const isMobile = window.innerWidth < 768
  const pieOption = {
    tooltip: { trigger: 'item' },
    legend: { 
       bottom: '0%', 
       left: 'center',
       itemWidth: 14,
       itemHeight: 14,
       textStyle: { color: '#909399' }
    },
    series: [
      {
        name: '活动分类',
        type: 'pie',
        // 移动端缩小半径防止重?
        radius: isMobile ? ['35%', '60%'] : ['40%', '70%'],
        center: ['50%', '45%'], // 稍微上移留给图例空间
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: 'var(--bg-card)', // 使用变量适配主题
          borderWidth: 2
        },
        label: { show: false, position: 'center' },
        emphasis: {
          label: { show: true, fontSize: isMobile ? 16 : 20, fontWeight: 'bold' }
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
  
  // 重新计算饼图 Radius
  if (pieChart) {
     const isMobile = window.innerWidth < 768
     pieChart.setOption({
        series: [{
           radius: isMobile ? ['35%', '60%'] : ['40%', '70%'],
           emphasis: {
              label: { fontSize: isMobile ? 16 : 20 }
           }
        }]
     })
  }
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
  border: none !important;
  transition: transform 0.3s;
  box-shadow: var(--shadow-light);
  overflow: hidden;
  background-color: transparent !important;

  :deep(.el-card__body) {
    padding: 0 !important;
    height: 100%;
    display: flex;
    align-items: center;
    background-color: transparent;
  }
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: var(--shadow-main);
  }

  .stats-content {
    display: flex;
    align-items: center;
    padding: 0 20px;
    width: 100%;
    height: 100%;
  }

  .icon-wrapper {
    width: 54px;
    height: 54px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 15px;
    font-size: 24px;
    color: white !important;
    background: rgba(255, 255, 255, 0.2) !important;
  }

  .info {
    flex: 1;
    .label {
      font-size: 14px;
      color: rgba(255, 255, 255, 0.9) !important;
      margin-bottom: 5px;
    }
    .value {
      font-size: 24px;
      font-weight: bold;
      color: white !important;
    }
  }
}

// Card Colors - apply gradients to body
.blue-card :deep(.el-card__body) {
  background: linear-gradient(135deg, #409EFF, #36D1DC) !important;
}

.green-card :deep(.el-card__body) {
  background: linear-gradient(135deg, #67C23A, #85E89D) !important;
}

.purple-card :deep(.el-card__body) {
  background: linear-gradient(135deg, #a18cd1, #CA72D1) !important;
}

.red-card :deep(.el-card__body) {
  background: linear-gradient(135deg, #F56C6C, #fbc2eb) !important;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: 12px;
  box-shadow: var(--shadow-light);
  overflow: hidden;

  .chart-container {
    height: 350px;
    width: 100%;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  color: var(--text-primary);
}

// Tasks row table card
.tasks-row {
   :deep(.el-card) {
      background: var(--bg-card);
      border: 1px solid var(--border-light);
      border-radius: 12px;
      box-shadow: var(--shadow-light);
   }
}

// Dark Mode Adaptation (Global handle)
html.dark {
  .stats-card {
    opacity: 0.85; // Slightly lower opacity for gradients in dark mode
    border-color: rgba(255, 255, 255, 0.1);
  }
}

/* 移动端响应式适配 */
@media (max-width: 768px) {
  .dashboard-container {
    padding: 20px 10px 10px 10px; // Increased top padding to separate from header
  }
  
  .stats-row {
    margin-bottom: 12px;
    
    .el-col {
      padding-left: 5px !important;
      padding-right: 5px !important;
      margin-bottom: 10px;
    }
  }
  
  .stats-card {
    height: 80px;
    border-radius: 8px;
    
    :deep(.el-card__body) {
      padding: 0 !important;
    }
    
    .stats-content {
      padding: 0 10px 0 24px; // Left padding to align icons, roughly centered
      height: 100%; 
      display: flex; 
      align-items: center; 
      justify-content: flex-start; // Align left to ensure icons line up
    }
    
    .icon-wrapper {
      width: 40px;
      height: 40px;
      font-size: 20px;
      margin-right: 12px; // Slightly increase gap
      flex-shrink: 0; // Prevent shrinking
    }
    
    .info {
      flex: initial; // Allow centering
      display: flex;
      flex-direction: column;
      justify-content: center;
      .label { 
         font-size: 11px; 
         text-align: left; // Keep text left-aligned relative to itself
         margin-bottom: 2px;
      }
      .value { 
         font-size: 18px; 
         line-height: 1.2;
         text-align: left;
      }
    }
  }
  
  .chart-card {
    margin-bottom: 12px;
    border-radius: 8px;
    
    .chart-container {
      height: 260px; // Slightly increased for mobile to accommodate legend
    }
  }

  /* Mobile Task List */
  .mobile-task-list {
     background: var(--bg-page);
     padding: 4px;

     .mobile-task-card {
        background: var(--bg-card);
        border: 1px solid var(--border-light);
        border-radius: 8px;
        padding: 12px;
        margin-bottom: 12px;
        box-shadow: var(--shadow-light);

        .card-header {
           display: flex;
           justify-content: space-between;
           align-items: center;
           margin-bottom: 12px;
           padding-bottom: 8px;
           border-bottom: 1px solid var(--border-light);

           .header-left {
              display: flex;
              align-items: center;
              gap: 8px;
              
              .source-text {
                 font-weight: bold;
                 font-size: 14px;
                 color: var(--text-primary);
              }
           }
        }

        .card-body {
           margin-bottom: 12px;
           .info-row {
              display: flex;
              justify-content: space-between;
              font-size: 13px;
              color: var(--text-secondary);
              
              .value { color: var(--text-primary); }
           }
        }

        .card-footer {
           display: flex;
           justify-content: flex-end;
           border-top: 1px solid var(--border-light);
           padding-top: 8px;
        }
     }
  }
}

/* 按钮文字高对比度修复：全部强制白?*/
.dashboard-container {
  :deep(.el-button--primary),
  :deep(.el-button--success) {
    color: #ffffff !important;
  }

  .card-header {
    :deep(.el-button) {
      color: #ffffff !important;
      font-weight: 500;
    }
  }
}
</style>
