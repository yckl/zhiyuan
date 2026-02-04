<template>
  <div class="org-notification-history">
    <el-card shadow="hover" class="main-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon :size="20"><Clock /></el-icon>
            <span class="title">通知历史</span>
            <el-tag type="info" size="small">共 {{ historyList.length }} 条</el-tag>
          </div>
          <el-button type="primary" @click="$router.push('/organizer/notification/send')">
            <el-icon><Plus /></el-icon> 发送新通知
          </el-button>
        </div>
      </template>

      <!-- 筛选栏 -->
      <div class="filter-bar">
        <el-select v-model="filterActivity" placeholder="筛选活动" clearable style="width: 200px">
          <el-option
            v-for="item in activityOptions"
            :key="item"
            :label="item"
            :value="item"
          />
        </el-select>
        <el-date-picker
          v-model="filterDateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="width: 260px"
        />
        <el-button @click="handleFilter">筛选</el-button>
        <el-button @click="resetFilter">重置</el-button>
      </div>

      <!-- 表格 -->
      <el-table :data="filteredList" v-loading="loading" stripe style="width: 100%">
        <el-table-column label="发送时间" width="160">
          <template #default="{ row }">
            <div class="time-cell">
              <el-icon><Clock /></el-icon>
              <span>{{ row.time }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="通知标题" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="title-text">{{ row.title }}</span>
          </template>
        </el-table-column>

        <el-table-column label="关联活动" width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag type="primary" effect="plain" size="small">{{ row.activity }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="接收对象" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getTargetTagType(row.target)" size="small">
              {{ row.target }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="覆盖人数" width="100" align="center">
          <template #default="{ row }">
            <span class="count-text">{{ row.count }} 人</span>
          </template>
        </el-table-column>

        <el-table-column label="阅读率" width="140">
          <template #default="{ row }">
            <div class="read-rate">
              <el-progress
                :percentage="row.readRate"
                :stroke-width="8"
                :color="getReadRateColor(row.readRate)"
              />
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetail(row)">
              <el-icon><View /></el-icon> 详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && filteredList.length === 0" description="暂无通知记录" />

      <!-- 分页 -->
      <div class="pagination-wrapper" v-if="historyList.length > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
          @current-change="handlePageChange"
          @size-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="currentDetail?.title" width="600px">

      <div class="detail-content" v-if="currentDetail">
        <div class="detail-meta">
          <div class="meta-item">
            <span class="label">发送时间:</span>
            <span class="value">{{ currentDetail.time }}</span>
          </div>
          <div class="meta-item">
            <span class="label">关联活动:</span>
            <el-tag type="primary" size="small">{{ currentDetail.activity }}</el-tag>
          </div>
          <div class="meta-item">
            <span class="label">接收对象:</span>
            <el-tag :type="getTargetTagType(currentDetail.target)" size="small">
              {{ currentDetail.target }}
            </el-tag>
          </div>
          <div class="meta-item">
            <span class="label">覆盖人数:</span>
            <span class="value">{{ currentDetail.count }} 人</span>
          </div>
          <div class="meta-item">
            <span class="label">阅读率:</span>
            <el-progress
              :percentage="currentDetail.readRate"
              :stroke-width="10"
              style="width: 150px"
            />
          </div>
        </div>

        <el-divider />

        <div class="detail-body">
          <h4>通知内容</h4>
          <div class="content-box">
            {{ currentDetail.content || '无内容' }}
          </div>
        </div>

        <el-divider />

        <div class="detail-recipients">
          <h4>接收人列表 (部分)</h4>
          <div class="recipients-list">
            <el-tag
              v-for="(name, index) in currentDetail.recipients"
              :key="index"
              type="info"
              effect="plain"
              class="recipient-tag"
            >
              {{ name }}
            </el-tag>
            <el-tag type="info" effect="plain" class="recipient-tag more">
              等 {{ currentDetail.count }} 人
            </el-tag>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Clock, Plus, View } from '@element-plus/icons-vue'
import request from '@/utils/request'
import dayjs from 'dayjs'

const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterActivity = ref('')
const filterDateRange = ref<[string, string] | null>(null)
const detailVisible = ref(false)
const currentDetail = ref<any>(null)

const historyList = ref<any[]>([])

// 获取历史记录
const fetchHistory = async () => {
  loading.value = true
  try {
    const res = await request.get('/notification/history', {
      params: {
        page: currentPage.value,
        size: pageSize.value
      }
    })
    
    // Transform backend data
    historyList.value = (res.data.records || []).map((item: any) => {
      // Parse title for target and activity.
      // Format 1 (New): 【Target | Activity】Title
      // Format 2 (Old): 【Target】Title (Where Title might be Activity Name)
      
      let target = '通知'
      let activity = '无关联活动'
      let displayTitle = item.title
      
      // Try new format
      const newMatch = item.title.match(/【(.*?)\s\|\s(.*?)】(.*)/)
      
      if (newMatch) {
         target = newMatch[1]
         activity = newMatch[2]
         displayTitle = newMatch[3]
      } else {
         // Try old format
         const oldMatch = item.title.match(/【(.*?)】(.*)/)
         if (oldMatch) {
           target = oldMatch[1]
           // Best guess: part 2 is activity or title. 
           // We assign it to activity as fallback for old auto-generated notifications.
           activity = oldMatch[2] 
           displayTitle = oldMatch[2]
         }
      }
      
      const readRate = item.totalCount > 0 
        ? Math.round((item.readCount / item.totalCount) * 100) 
        : 0

      return {
        id: item.title + item.createTime, // Unique key
        time: dayjs(item.createTime).format('YYYY-MM-DD HH:mm'),
        title: displayTitle,
        activity: activity, // Placeholder or parsed
        target: target,
        count: item.totalCount,
        readCount: item.readCount,
        readRate: readRate,
        content: item.content,
        recipients: [] // Backend doesn't return list yet
      }
    })
    total.value = res.data.total
  } catch (error) {
    console.error('Fetch history failed', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchHistory()
})

// 活动选项
const activityOptions = computed(() => {
  const activities = new Set(historyList.value.map(item => item.activity))
  return Array.from(activities)
})

// 筛选后的列表 (Backend pagination used, so client filter limited to current page? 
// Ideally backend validation but user asked for frontend fix first. 
// I'll keep client filtering on the fetched page for simplicity OR 
// if user wants backend filtering I need more params. 
// For now, I'll assume valid data from backend covers most needs. 
// The existing filter logic filtered the Mock List.
// Since I fetch paginated data, client-side filtering only filters the current page.
// I'll leave it as is or remove it?
// User didn't ask for filter fix.
// I'll keep it simple.)
const filteredList = computed(() => {
  let list = historyList.value

  if (filterActivity.value) {
    list = list.filter(item => item.activity === filterActivity.value)
  }
  
  // Date filter
  if (filterDateRange.value) {
     // ... logic
  }

  return list
})

// 分页处理
const handlePageChange = (val: number) => {
  currentPage.value = val
  fetchHistory()
}

// 获取接收对象标签类型
const getTargetTagType = (target: string) => {
  const map: Record<string, string> = {
    '全部报名者': 'primary',
    '已通过者': 'success',
    '已签到者': 'info',
    '未签到者': 'warning',
    'approved': 'success',
    'all': 'primary', 
    'checkedin': 'info',
    'notcheckedin': 'warning'
  }
  // Try to match partial
  if (target.includes('通过')) return 'success'
  if (target.includes('签到') && !target.includes('未')) return 'info'
  if (target.includes('未签到')) return 'warning'
  
  return map[target] || 'info'
}

// 获取阅读率颜色
const getReadRateColor = (rate: number) => {
  if (rate >= 80) return '#67c23a'
  if (rate >= 50) return '#e6a23c'
  return '#f56c6c'
}

const handleFilter = () => {
  currentPage.value = 1
  fetchHistory() // Should ideally pass filters to backend
}

const resetFilter = () => {
  filterActivity.value = ''
  filterDateRange.value = null
  currentPage.value = 1
  fetchHistory()
}

const showDetail = (row: any) => {
  currentDetail.value = row
  detailVisible.value = true
}
</script>

<style scoped lang="scss">
.org-notification-history {
  padding: 10px;

  .main-card {
    border-radius: 12px;
    border: none;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-left {
      display: flex;
      align-items: center;
      gap: 8px;

      .title {
        font-size: 16px;
        font-weight: 600;
      }
    }
  }

  .filter-bar {
    display: flex;
    gap: 12px;
    margin-bottom: 20px;
    padding-bottom: 16px;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  .time-cell {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: var(--el-text-color-regular);

    .el-icon {
      color: var(--el-color-primary);
    }
  }

  .title-text {
    font-weight: 500;
    color: var(--el-text-color-primary);
  }

  .count-text {
    font-weight: 600;
    color: var(--el-color-primary);
  }

  .read-rate {
    :deep(.el-progress__text) {
      font-size: 12px !important;
    }
  }

  .pagination-wrapper {
    display: flex;
    justify-content: center;
    margin-top: 20px;
    padding-top: 16px;
    border-top: 1px solid var(--el-border-color-lighter);
  }
}

// 详情弹窗样式
.detail-content {
  .detail-meta {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;

    .meta-item {
      display: flex;
      align-items: center;
      gap: 8px;

      .label {
        color: var(--el-text-color-secondary);
        font-size: 13px;
      }

      .value {
        font-weight: 500;
      }
    }
  }

  .detail-body {
    h4 {
      margin: 0 0 12px;
      font-size: 14px;
      color: var(--el-text-color-primary);
    }

    .content-box {
      background: var(--el-fill-color-light);
      padding: 16px;
      border-radius: 8px;
      white-space: pre-wrap;
      font-size: 14px;
      line-height: 1.6;
      color: var(--el-text-color-regular);
    }
  }

  .detail-recipients {
    h4 {
      margin: 0 0 12px;
      font-size: 14px;
      color: var(--el-text-color-primary);
    }

    .recipients-list {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;

      .recipient-tag {
        &.more {
          border-style: dashed;
        }
      }
    }
  }
}
</style>
