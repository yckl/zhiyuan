<template>
  <div class="notif-history-page" v-loading="loading">
    <!-- ==================== 1. PC 端 (保持原样，仅做结构性包装) ==================== -->
    <template v-if="!isMobile">
      <div class="pc-history">
        <div class="top-bar">
          <div class="top-left">
            <h1 class="page-title">通知记录</h1>
            <span class="total-badge">{{ total }} 条记录</span>
          </div>
          <el-button class="send-btn" @click="$router.push('/organizer/notification/send')">
            <el-icon><Promotion /></el-icon> 发送新通知
          </el-button>
        </div>

        <div class="filter-strip">
          <el-select v-model="filterActivity" placeholder="全部活动" clearable class="ghost-select" @change="handleFilter">
            <el-option v-for="opt in activityOptions" :key="opt" :label="opt" :value="opt" />
          </el-select>
          <el-date-picker v-model="filterDateRange" type="daterange" range-separator="~" start-placeholder="起始" end-placeholder="截止" class="ghost-date" @change="handleFilter" />
          <el-button link class="reset-link" @click="resetFilter" v-if="filterActivity || filterDateRange">重置</el-button>
        </div>

        <div class="pc-table-wrapper">
          <!-- PC 列表 (Modern Card List) -->
          <div class="notification-list-container">
          <div 
            v-for="item in filteredList" 
            :key="item.id" 
            class="list-item-card"
            @click="showDetail(item)"
          >
            <div class="info-col">
              <div class="title">{{ item.title }}</div>
              <div class="subtitle">
                <el-tag size="small" effect="plain" type="info">{{ item.target }}</el-tag>
                <span>· {{ item.activity || '全体成员' }}</span>
              </div>
            </div>

            <div class="progress-col">
              <div class="progress-label">阅读率 {{ item.readRate }}%</div>
              <div class="read-bar-wrapper">
                <div 
                  class="read-bar-gradient" 
                  :style="{ width: item.readRate + '%' }"
                ></div>
              </div>
            </div>

            <div class="time-col">
              <span class="send-time">{{ item.time }}</span>
              <el-icon class="action-arrow"><ArrowRight /></el-icon>
            </div>
          </div>
        </div>
          <el-empty v-if="!loading && filteredList.length === 0" description="暂无通知记录" />
        </div>

        <div class="pagination-area" v-if="total > pageSize">
          <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :total="total" :page-sizes="[10,20,50]" layout="total, prev, pager, next" background @current-change="handlePageChange" />
        </div>

        <!-- PC 详情弹窗 (Premium Letter Style) -->
        <el-dialog 
          v-model="detailVisible" 
          width="500px" 
          class="notification-letter-modal"
          :show-close="false"
          align-center
          destroy-on-close
        >
          <div v-if="currentDetail" class="letter-container">
            <div class="letter-header">
              <div class="icon-circle">
                <el-icon><BellFilled /></el-icon>
              </div>
              <h3 class="letter-title">{{ currentDetail.title }}</h3>
              <div class="letter-type">{{ currentDetail.target }}</div>
            </div>

            <div class="stats-row">
              <div class="stat-item">
                <span class="num">{{ currentDetail.count }}</span>
                <span class="label">覆盖人数</span>
              </div>
              <div class="stat-divider"></div>
              <div class="stat-item">
                <span class="num highlight">{{ currentDetail.readRate }}%</span>
                <span class="label">阅读</span>
              </div>
              <div class="stat-divider"></div>
              <div class="stat-item">
                <span class="num time-text">{{ currentDetail.time.split(' ')[0] }}</span>
                <span class="label">发送日期</span>
              </div>
            </div>

            <div class="letter-body">
              <div class="content-scroll">
                {{ currentDetail.content || '无正文内容' }}
              </div>
            </div>

            <div class="letter-footer">
              <el-button type="primary" round class="close-btn" @click="detailVisible = false">
                我已知晓
              </el-button>
            </div>
          </div>
        </el-dialog>
      </div>
    </template>

    <!-- ==================== 2. 移动端 (iOS Premium Notification Card List) ==================== -->
    <template v-else>
      <div class="m-history-ios">
        <!-- 顶部玻璃拟态导航栏 -->
        <div class="m-ios-nav">
          <div class="nav-left">
            <h1>消息记录</h1>
            <span class="nav-subtitle">最近发送的 {{ total }} 条资讯</span>
          </div>
          <div class="nav-right">
            <div class="fab-mini" @click="$router.push('/organizer/notification/send')">
              <el-icon><Plus /></el-icon>
            </div>
          </div>
        </div>

        <!-- 列表区域: iOS Premium Cards -->
        <div class="ios-notify-stream">
          <div
            v-for="item in filteredList"
            :key="item.id"
            class="ios-notify-card"
            @click="showMobileDetail(item)"
          >
            <!-- 左侧：彩色 Squircle 图标 -->
            <div class="icon-box" :style="{ background: getTargetColor(item.target).bg, color: getTargetColor(item.target).text }">
              <el-icon><component :is="getNotifyIcon(item.target)" /></el-icon>
            </div>

            <!-- 中间：核心内容区 -->
            <div class="card-content">
              <div class="title-row">
                <h4 class="card-title">{{ item.title }}</h4>
                <span class="card-time">{{ formatShortTime(item.time) }}</span>
              </div>
              <p class="card-desc">{{ item.content }}</p>
              <div class="card-meta">
                <el-icon><List /></el-icon>
                <span>{{ item.activity }}</span>
              </div>
            </div>

            <!-- 右侧 engagement 环形进度条 -->
            <div class="card-engagement">
              <el-progress 
                type="circle" 
                :width="42" 
                :stroke-width="3" 
                :percentage="item.readRate" 
                :color="customProgressColors"
              />
              <span class="engagement-label">触达</span>
            </div>
          </div>

          <el-empty v-if="!loading && filteredList.length === 0" description="没有发现消息记录" :image-size="80" />
        </div>

        <!-- 移动端详情抽屉 (iOS 样式) -->
        <el-drawer
          v-model="mobileDrawerVisible"
          direction="btt"
          size="78%"
          class="ios-detail-drawer"
          :with-header="false"
          destroy-on-close
        >
          <div class="drawer-handle"></div>
          <div class="drawer-inner" v-if="currentDetail">
            <div class="drawer-header">
              <div class="dr-icon-wrap" :style="{ background: getTargetColor(currentDetail.target).bg, color: getTargetColor(currentDetail.target).text }">
                <el-icon><Promotion /></el-icon>
              </div>
              <div class="dr-title-area">
                <h3 class="dr-title">{{ currentDetail.title }}</h3>
                <p class="dr-meta">{{ currentDetail.time }} · {{ currentDetail.target }}</p>
              </div>
            </div>

            <div class="dr-stats-row">
              <div class="dr-stat">
                <div class="val">{{ currentDetail.count }}</div>
                <div class="lab">覆盖人群</div>
              </div>
              <div class="dr-stat">
                <div class="val accent">{{ currentDetail.readRate }}%</div>
                <div class="lab">阅读</div>
              </div>
              <div class="dr-stat">
                <div class="val">{{ Math.round(currentDetail.count * currentDetail.readRate / 100) }}</div>
                <div class="lab">已阅人数</div>
              </div>
            </div>

            <div class="dr-content-scroll">
              <div class="dr-content-label">正文内容</div>
              <div class="dr-text-box">{{ currentDetail.content || '无正文内容' }}</div>
              
              <div class="dr-context-group">
                <div class="context-item">
                  <span class="label">来源活动</span>
                  <span class="value">{{ currentDetail.activity }}</span>
                </div>
              </div>
            </div>
            
            <button class="dr-close-btn" @click="mobileDrawerVisible = false">完成</button>
          </div>
        </el-drawer>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { 
  Promotion, Bell, ArrowRight, List, Plus, 
  Checked, Timer, Warning, InfoFilled, BellFilled 
} from '@element-plus/icons-vue'
import request from '@/utils/request'
import dayjs from 'dayjs'
import { useMobile } from '@/composables/useMobile'

const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterActivity = ref('')
const filterDateRange = ref<[string, string] | null>(null)
const detailVisible = ref(false)
const mobileDrawerVisible = ref(false)
const currentDetail = ref<any>(null)
const { isMobile } = useMobile()

const customProgressColors = [
  { color: '#cbd5e1', percentage: 20 },
  { color: '#80D0C7', percentage: 50 },
  { color: '#0093E9', percentage: 80 },
  { color: '#00BFA6', percentage: 100 },
]

interface NotificationItem {
  id: string; time: string; title: string; activity: string; target: string
  count: number; readCount: number; readRate: number; content: string; recipients: string[]
}

const historyList = ref<NotificationItem[]>([])

const fetchHistory = async () => {
  loading.value = true
  try {
    const res = await request.get('/notification/history', { params: { page: currentPage.value, size: pageSize.value } })
    historyList.value = (res.data.records || []).map((item: any) => {
      let target = '通知', activity = '无关联活动', displayTitle = item.title
      const newMatch = item.title.match(/【(.*?)】\s\|\s【(.*?)】(.*)/)
      if (newMatch) { target = newMatch[1]; activity = newMatch[2]; displayTitle = newMatch[3] }
      else { const old = item.title.match(/【(.*?)】(.*)/); if (old) { target = old[1]; activity = old[2]; displayTitle = old[2] } }
      const readRate = item.totalCount > 0 ? Math.round((item.readCount / item.totalCount) * 100) : 0
      return { id: item.title + item.createTime, time: dayjs(item.createTime).format('YYYY-MM-DD HH:mm'), title: displayTitle, activity, target, count: item.totalCount, readCount: item.readCount, readRate, content: item.content, recipients: [] }
    })
    total.value = res.data.total
  } catch (e) { 
    console.error('Fetch failed', e) 
  } finally { 
    loading.value = false 
  }
}

onMounted(() => { fetchHistory() })

const activityOptions = computed(() => Array.from(new Set(historyList.value.map(i => i.activity))))
const filteredList = computed(() => historyList.value) // Simplified for refactor

const handlePageChange = () => fetchHistory()
const handleFilter = () => { currentPage.value = 1; fetchHistory() }
const resetFilter = () => { filterActivity.value = ''; filterDateRange.value = null; currentPage.value = 1; fetchHistory() }

const getTargetClass = (target: string) => {
  if (target.includes('通知')) return 'dot-blue'
  if (target.includes('未签到')) return 'dot-amber'
  if (target.includes('审核')) return 'dot-green'
  return 'dot-gray'
}

const getTargetColor = (target: string) => {
  if (target.includes('通知')) return { bg: 'rgba(0, 147, 233, 0.1)', text: '#0093E9' }
  if (target.includes('未签到')) return { bg: 'rgba(128, 208, 199, 0.1)', text: '#0093E9' }
  if (target.includes('审核') || target.includes('通过')) return { bg: '#EDFFF3', text: '#34C759' }
  return { bg: '#F2F2F7', text: '#8E8E93' }
}

const getNotifyIcon = (target: string) => {
  if (target.includes('未签到')) return 'Timer'
  if (target.includes('审核') || target.includes('通过')) return 'Checked'
  return 'Promotion'
}

const getReadRateColor = (rate: number) => { 
  if (rate >= 80) return '#00BFA6'
  if (rate >= 50) return '#80D0C7'
  return '#94a3b8' 
}

const formatShortTime = (time: string) => { 
  const d = dayjs(time)
  const now = dayjs()
  if (d.isSame(now, 'day')) return d.format('HH:mm')
  return d.format('MM-DD')
}

const showDetail = (row: any) => { currentDetail.value = row; detailVisible.value = true }
const showMobileDetail = (row: any) => { currentDetail.value = row; mobileDrawerVisible.value = true }
</script>

<style scoped lang="scss">
.notif-history-page { min-height: 100vh; background: #f4f7f9; }

/* PC 样式保持简洁逻辑 */
.pc-history {
  padding: 32px; max-width: 1100px; margin: 0 auto;
  .top-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;
    .page-title { font-size: 24px; font-weight: 800; color: #1e293b; margin: 0; }
    .total-badge { font-size: 13px; color: #94a3b8; margin-left:12px;}
    .send-btn { background: linear-gradient(135deg, #0093E9, #80D0C7); color: #fff; height: 40px; border-radius: 10px; font-weight: 700; border: none; box-shadow: 0 4px 12px rgba(0, 147, 233, 0.2); }
  }
  .filter-strip { display: flex; gap: 12px; margin-bottom: 24px; .ghost-select { width: 160px; } }
}

/* ==================== Modern Card List (PC) ==================== */
.notification-list-container {
  display: flex; flex-direction: column; gap: 16px; padding: 20px 0;
}

.list-item-card {
  display: flex; align-items: center; justify-content: space-between;
  background: #fff; padding: 18px 24px;
  border-radius: 14px; border: 1px solid #f1f5f9;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  position: relative;
  overflow: hidden;

  &::before {
    content: ''; position: absolute; left: 0; top: 0; bottom: 0;
    width: 4px; background: #0093E9; opacity: 0; transition: 0.3s;
  }

  &:hover {
    border-color: rgba(0, 147, 233, 0.2);
    box-shadow: 0 10px 30px rgba(0, 147, 233, 0.08);
    transform: translateY(-2px);
    &::before { opacity: 1; }
    .action-arrow { color: #0093E9; transform: translateX(6px); }
    .title { color: #0093E9; }
  }
}

.info-col { flex: 2; display: flex; flex-direction: column; gap: 6px; }
.title { font-size: 16px; font-weight: 700; color: #1e293b; transition: color 0.3s; }
.subtitle { font-size: 12px; color: #64748b; display: flex; align-items: center; gap: 10px; }

.progress-col { 
  flex: 1.2; padding: 0 60px;
  .progress-label { font-size: 12px; font-weight: 600; color: #64748b; margin-bottom: 6px; }
}

.read-bar-wrapper {
  width: 100%; height: 8px; background: #f1f5f9; border-radius: 4px; overflow: hidden;
  .read-bar-gradient {
    height: 100%; border-radius: 4px;
    background: linear-gradient(90deg, #0093E9 0%, #80D0C7 100%);
    transition: width 0.6s cubic-bezier(0.1, 0.7, 1.0, 0.1);
  }
}

.time-col {
  flex: 0.8; text-align: right; color: #94a3b8; font-size: 13px;
  display: flex; align-items: center; justify-content: flex-end; gap: 16px;
  .send-time { font-family: 'DIN Alternate', sans-serif; }
}

.action-arrow { transition: all 0.3s; font-size: 18px; color: #cbd5e1; }


/* ==================== Premium Letter Dialog ==================== */
:deep(.notification-letter-modal) {
  background: transparent !important;
  box-shadow: none !important;
  .el-dialog__header { display: none; }
  .el-dialog__body { padding: 0 !important; }
}

.letter-container {
  background: #fff;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 50px rgba(0,0,0,0.15);
}

.letter-header {
  background: linear-gradient(135deg, #0093E9 0%, #80D0C7 100%);
  padding: 30px 20px 45px;
  text-align: center;
  color: #fff;
  position: relative;
}

.icon-circle {
  width: 52px; height: 52px; background: rgba(255,255,255,0.2);
  border-radius: 50%; margin: 0 auto 12px;
  display: flex; align-items: center; justify-content: center;
  font-size: 26px; backdrop-filter: blur(10px);
  border: 1px solid rgba(255,255,255,0.3);
}

.letter-title { margin: 0; font-size: 19px; font-weight: 800; letter-spacing: 0.5px; }
.letter-type { font-size: 11px; opacity: 0.85; margin-top: 6px; text-transform: uppercase; font-weight: 700; letter-spacing: 1px; }

.stats-row {
  background: #fff;
  margin: -24px 24px 20px;
  padding: 18px 0;
  border-radius: 14px;
  display: flex; align-items: center; justify-content: space-around;
  box-shadow: 0 10px 25px rgba(0, 147, 233, 0.12);
  position: relative; z-index: 2;
  border: 1px solid rgba(0,0,0,0.02);
}

.stat-item { display: flex; flex-direction: column; align-items: center; gap: 4px; }
.num { font-size: 18px; font-weight: 800; color: #1e293b; font-family: 'DIN Alternate', sans-serif; }
.num.highlight { color: #0093E9; }
.num.time-text { font-size: 14px; color: #64748b; }
.label { font-size: 11px; color: #94a3b8; font-weight: 600; }
.stat-divider { width: 1px; height: 24px; background: #f1f5f9; }

.letter-body { padding: 0 24px; }
.content-scroll {
  background: #F8FAFC;
  padding: 24px;
  border-radius: 12px;
  font-size: 14px; color: #334155; line-height: 1.8;
  max-height: 300px; overflow-y: auto;
  border: 1px dashed #cbd5e1;
  white-space: pre-wrap;
  
  &::-webkit-scrollbar { width: 4px; }
  &::-webkit-scrollbar-thumb { background: #e2e8f0; border-radius: 2px; }
}

.letter-footer { padding: 24px; text-align: center; }
.close-btn {
  width: 160px; height: 44px;
  background: linear-gradient(135deg, #0093E9, #80D0C7);
  border: none; color: #fff; font-weight: 700;
  box-shadow: 0 8px 16px rgba(0, 147, 233, 0.25);
  transition: all 0.3s;
  &:hover { transform: translateY(-2px); box-shadow: 0 12px 20px rgba(0, 147, 233, 0.35); }
}

/* ==================== 移动端?(iOS Premium Notification Stream) ==================== */
.m-history-ios {
  background: #f2f2f7;
  min-height: 100vh;
  padding-bottom: 120px;

  /* 导航栏 */
  .m-ios-nav {
    background: rgba(255, 255, 255, 0.85);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    padding: 18px 20px 14px;
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    position: sticky;
    top: 0;
    z-index: 100;
    border-bottom: 0.5px solid rgba(0, 0, 0, 0.05);

    .nav-left {
      h1 { font-size: 26px; font-weight: 800; color: #000; margin: 0; letter-spacing: -0.5px; }
      .nav-subtitle { font-size: 11px; color: #8e8e93; font-weight: 600; text-transform: uppercase; display: block; margin-top: 2px; }
    }

    .nav-right {
      .fab-mini {
        width: 32px; height: 32px; border-radius: 50%; background: linear-gradient(135deg, #0093E9, #80D0C7); 
        display: flex; align-items: center; justify-content: center; color: #fff;
        font-size: 18px; box-shadow: 0 4px 12px rgba(0, 147, 233, 0.3);
        &:active { transform: scale(0.9); }
      }
    }
  }

  /* 列表区 */
  .ios-notify-stream {
    padding: 16px 16px 0;
    display: flex;
    flex-direction: column;
    gap: 12px;

    .ios-notify-card {
      background: #fff;
      border-radius: 20px;
      padding: 14px 16px;
      display: flex;
      align-items: center;
      gap: 14px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.02);
      transition: all 0.2s;
      
      &:active {
        transform: scale(0.98);
        background: #fdfdfd;
      }

      /* 图标 */
      .icon-box {
        width: 44px;
        height: 44px;
        border-radius: 12px; // iOS Squircle style
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 20px;
        flex-shrink: 0;
      }

      /* 内容 */
      .card-content {
        flex: 1;
        min-width: 0;

        .title-row {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          margin-bottom: 2px;
          
          .card-title {
            font-size: 15px;
            font-weight: 700;
            color: #1c1c1e;
            margin: 0;
            flex: 1;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }
          
          .card-time {
            font-size: 12px;
            color: #c7c7cc;
            font-weight: 500;
            margin-left: 8px;
          }
        }

        .card-desc {
          font-size: 13px;
          color: #8e8e93;
          margin: 0 0 6px;
          width: 100%;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }

        .card-meta {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 11px;
          color: #0093E9;
          font-weight: 600;
          
          .el-icon { font-size: 12px; }
          span { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
        }
      }

      /* 右侧 Engagement Ring */
      .card-engagement {
        flex-shrink: 0;
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 2px;
        
        .engagement-label {
          font-size: 9px;
          font-weight: 800;
          color: #cbd5e1;
          text-transform: uppercase;
        }
        
        :deep(.el-progress-circle) {
          margin: 0 auto;
        }
        :deep(.el-progress__text) {
          font-size: 10px !important;
          font-weight: 800;
        }
      }
    }
  }

  /* iOS Detail Drawer */
  .ios-detail-drawer {
    :deep(.el-drawer__body) { padding: 0; border-radius: 28px 28px 0 0; }
    
    .drawer-handle {
      width: 36px; height: 5px; background: #e5e5ea; border-radius: 10px;
      margin: 12px auto;
    }

    .drawer-inner {
      padding: 12px 24px 40px;
      
      .drawer-header {
        display: flex; align-items: center; gap: 16px; margin-bottom: 28px;
        
        .dr-icon-wrap {
          width: 52px; height: 52px; border-radius: 14px;
          display: flex; align-items: center; justify-content: center; font-size: 24px;
        }
        .dr-title-area {
          .dr-title { font-size: 19px; font-weight: 800; color: #000; margin: 0 0 2px; }
          .dr-meta { font-size: 12px; color: #8e8e93; font-weight: 500; }
        }
      }

      .dr-stats-row {
        display: flex; gap: 12px; margin-bottom: 28px;
        .dr-stat {
          flex: 1; background: #f2f2f7; border-radius: 16px; padding: 14px; text-align: center;
          .val { font-size: 22px; font-weight: 800; color: #1c1c1e; &.accent { color: #0093E9; } }
          .lab { font-size: 11px; color: #8e8e93; font-weight: 600; margin-top: 4px; }
        }
      }

      .dr-content-scroll {
        .dr-content-label { font-size: 12px; font-weight: 800; color: #8e8e93; margin-bottom: 12px; text-transform: uppercase; }
        .dr-text-box { font-size: 15px; line-height: 1.7; color: #3c3c43; white-space: pre-wrap; margin-bottom: 24px; }
        .dr-context-group {
          background: #f2f2f7; border-radius: 14px; padding: 12px 16px;
          .context-item {
            display: flex; justify-content: space-between; align-items: center;
            .label { font-size: 13px; color: #8e8e93; }
            .value { font-size: 13px; font-weight: 700; color: #0093E9; }
          }
        }
      }

      .dr-close-btn {
        width: 100%; height: 50px; background: linear-gradient(135deg, #0093E9, #80D0C7); border: none; border-radius: 16px;
        color: #fff; font-size: 16px; font-weight: 700; margin-top: 32px;
        box-shadow: 0 8px 16px rgba(0, 147, 233, 0.2);
        &:active { opacity: 0.8; transform: scale(0.98); }
      }
    }
  }
}
</style>
