<template>
  <div class="app-container">
    <el-card>
      <!-- Filter -->
      <div class="filter-container" :class="{ 'is-mobile': isMobile }">
        <el-input
          v-model="queryParams.title"
          placeholder="活动标题"
          class="filter-item search-input"
          @keyup.enter="handleQuery"
          clearable
        />
        <el-input
          v-model="queryParams.organizerName"
          placeholder="发布组织"
          class="filter-item search-input"
          @keyup.enter="handleQuery"
          clearable
        />
        <el-select
          v-model="queryParams.status"
          placeholder="状态"
          clearable
          class="filter-item status-select"
          @change="handleQuery"
        >
          <el-option label="待审核" :value="1" />
          <el-option label="招募中" :value="2" />
          <el-option label="进行中" :value="3" />
          <el-option label="已结束" :value="4" />
          <el-option label="已下架" :value="5" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          class="filter-item date-range"
          value-format="YYYY-MM-DD"
          @change="handleDateChange"
        />
        <div class="action-buttons">
            <el-button type="primary" class="filter-item" @click="handleQuery" :icon="Search">
                <span>搜索</span>
            </el-button>
            <el-button class="filter-item" @click="resetQuery" :icon="Refresh">
                <span>重置</span>
            </el-button>
        </div>
      </div>

      <!-- Desktop Table -->
      <el-table
        v-loading="loading"
        :data="activityList"
        style="width: 100%"
        border
        class="hidden-sm-and-down custom-table"
      >
        <el-table-column label="封面" width="100" align="center">
          <template #default="scope">
            <el-image
              style="width: 80px; height: 50px"
              :src="scope.row.coverImage"
              fit="cover"
              :preview-src-list="[scope.row.coverImage]"
              preview-teleported>
  <template #error>
    <img :src="'/default-cover.jpg'" style="width:100%;height:100%;object-fit:cover"/>
  </template>
</el-image>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="活动标题" min-width="150" show-overflow-tooltip />
        <el-table-column prop="organizerName" label="发布方" width="120" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column label="活动时间" width="280">
          <template #default="scope">
            {{ formatTime(scope.row.startTime) }} ~ {{ formatTime(scope.row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column label="参与人数" width="120" align="center">
          <template #default="scope">
             {{ scope.row.currentParticipants }} / {{ scope.row.maxParticipants || '不限' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="首页置顶" width="100" align="center">
          <template #default="scope">
             <el-switch
                v-model="scope.row.isTopBool"
                inline-prompt
                active-text="是"
                inactive-text="否"
                :loading="scope.row.topLoading"
                @change="handleTopChange(scope.row)"
             />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="handleView(scope.row)">详情</el-button>
            <el-button 
               v-if="scope.row.status !== 5 && scope.row.status !== 4"
               link 
               type="danger" 
               @click="handleOffline(scope.row)"
            >
              强制下架
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Mobile Card List -->
      <div class="hidden-md-and-up mobile-card-list">
        <div v-for="item in activityList" :key="item.id" class="mobile-card">
           <div class="card-header">
              <div class="header-left">
                 <el-tag :type="getStatusType(item.status)" size="small" class="status-tag">
                    {{ getStatusLabel(item.status) }}
                 </el-tag>
                 <span class="card-title">{{ item.title }}</span>
              </div>
           </div>
           <div class="card-body">
              <el-image :src="item.coverImage" class="card-cover" fit="cover">
  <template #error>
    <img :src="'/default-cover.jpg'" style="width:100%;height:100%;object-fit:cover"/>
  </template>
</el-image>
              <div class="card-info">
                 <p><label>发布方：</label>{{ item.organizerName }}</p>
                 <p><label>时间：</label>{{ formatTime(item.startTime) }}</p>
                 <p><label>人数：</label>{{ item.currentParticipants }}/{{ item.maxParticipants || '不限' }}</p>
              </div>
           </div>
           <div class="card-footer">
              <div class="footer-left">
                 <span class="top-label">置顶</span>
                 <el-switch
                    v-model="item.isTopBool"
                    inline-prompt
                    active-text="是"
                    inactive-text="否"
                    :loading="item.topLoading"
                    @change="handleTopChange(item)"
                    size="small"
                 />
              </div>
              <div class="footer-right">
                 <el-button size="small" @click="handleView(item)">详情</el-button>
                 <el-button 
                    v-if="item.status !== 5 && item.status !== 4"
                    size="small" 
                    type="danger" 
                    @click="handleOffline(item)"
                 >
                   下架
                 </el-button>
              </div>
           </div>
        </div>
        <el-empty v-if="!loading && activityList.length === 0" description="暂无数据" />
      </div>

      <!-- Pagination -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          :layout="isMobile ? 'prev, pager, next' : 'total, sizes, prev, pager, next, jumper'"
          :pager-count="isMobile ? 5 : 7"
          :small="isMobile"
          background
          @size-change="handleQuery"
          @current-change="handleQuery"
        />
      </div>
    </el-card>

    <!-- Detail Dialog -->
    <el-dialog v-model="detailVisible" title="活动详情" width="600px">
      <div v-if="currentActivity" class="activity-detail">
        <!-- Simple detail view -->
         <h3>{{ currentActivity.title }}</h3>
         <p><strong>发布方:</strong> {{ currentActivity.organizerName }}</p>
         <p><strong>分类:</strong> {{ currentActivity.categoryName }}</p>
         <p><strong>时间:</strong> {{ formatTime(currentActivity.startTime) }}</p>
         <p><strong>状态:</strong> {{ getStatusLabel(currentActivity.status) }}</p>
         <!-- Add more fields as needed or full content preview -->
      </div>
    </el-dialog>

    <!-- Offline Dialog -->
    <el-dialog v-model="offlineVisible" title="强制下架确认" width="400px">
       <el-form label-width="80px">
         <el-form-item label="下架原因">
             <el-input v-model="offlineReason" type="textarea" placeholder="请输入强制下架原因" />
         </el-form-item>
       </el-form>
       <template #footer>
          <el-button @click="offlineVisible = false">取消</el-button>
          <el-button type="danger" @click="submitOffline">确认下架</el-button>
       </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import request from '@/utils/request'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'

const windowWidth = ref(window.innerWidth)
const isMobile = ref(window.innerWidth < 768)

const handleResize = () => {
  windowWidth.value = window.innerWidth
  isMobile.value = windowWidth.value < 768
}

const loading = ref(false)
const activityList = ref<any[]>([])
const total = ref(0)
const queryParams = reactive({
  page: 1,
  size: 10,
  title: '',
  organizerName: '',
  status: undefined as number | undefined,
  startTime: '',
  endTime: ''
})
const dateRange = ref([])

const handleDateChange = (val: any) => {
  if (val && val.length === 2) {
    queryParams.startTime = val[0]
    queryParams.endTime = val[1]
  } else {
    queryParams.startTime = ''
    queryParams.endTime = ''
  }
  handleQuery()
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/activity/list', { params: queryParams })
    activityList.value = res.data.records.map((item: any) => ({
       ...item,
       isTopBool: item.isTop === 1,
       topLoading: false
    }))
    total.value = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
    queryParams.page = 1
    fetchData()
}

const resetQuery = () => {
    queryParams.title = ''
    queryParams.organizerName = ''
    queryParams.status = undefined
    queryParams.startTime = ''
    queryParams.endTime = ''
    dateRange.value = []
    handleQuery()
}

const formatTime = (time: string) => dayjs(time).format('YYYY-MM-DD HH:mm')

const getStatusType = (status: number) => {
    // 1-待审核(warning), 2-招募(success), 3-进行(primary), 4-结束(info), 5-下架(danger)
    switch(status) {
        case 2: return 'success'
        case 3: return 'primary'
        case 4: return 'info'
        case 5: return 'danger'
        case 1: return 'warning'
        default: return 'info'
    }
}

const getStatusLabel = (status: number) => {
    switch(status) {
        case 1: return '待审核'
        case 2: return '招募中'
        case 3: return '进行中'
        case 4: return '已结束'
        case 5: return '已下架'
        case 0: return '草稿'
        default: return '未知'
    }
}

// Top Switch
const handleTopChange = async (row: any) => {
   const newState = row.isTopBool
   row.topLoading = true
   try {
      await request.put('/admin/activity/recommend', {
         id: row.id,
         isTop: newState
      })
      ElMessage.success('操作成功')
      row.isTop = newState ? 1 : 0
   } catch {
      row.isTopBool = !newState
   } finally {
      row.topLoading = false
   }
}

// Detail
const detailVisible = ref(false)
const currentActivity = ref<any>(null)
const handleView = (row: any) => {
   currentActivity.value = row
   detailVisible.value = true
}

// Offline
const offlineVisible = ref(false)
const offlineReason = ref('')
const currentOfflineId = ref<number | null>(null)

const handleOffline = (row: any) => {
    currentOfflineId.value = row.id
    offlineReason.value = ''
    offlineVisible.value = true
}

const submitOffline = async () => {
   if (!offlineReason.value) {
      ElMessage.warning('请输入下架原因')
      return
   }
   try {
      await request.put('/admin/activity/offline', {
         id: currentOfflineId.value,
         reason: offlineReason.value
      })
      ElMessage.success('已强制下架')
      offlineVisible.value = false
      fetchData()
   } catch {}
}

onMounted(() => {
    window.addEventListener('resize', handleResize)
    fetchData()
})

onUnmounted(() => {
    window.removeEventListener('resize', handleResize)
})
</script>

<style scoped lang="scss">
.app-container {
  padding: 20px;
  background: var(--bg-page);
  min-height: calc(100vh - 84px);
}

:deep(.el-card) {
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: 12px;
}

.filter-container {
  margin-bottom: 20px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;

  .search-input { width: 220px; }
  .status-select { width: 120px; }
  .date-range { width: 240px; }
}

.pagination-container {
   margin-top: 20px;
   display: flex;
   justify-content: flex-end;
}

.activity-detail {
  padding: 10px;
  color: var(--text-primary);
  
  h3 { margin-bottom: 15px; color: var(--text-primary); }
  p { margin: 8px 0; color: var(--text-secondary); }
}

@media only screen and (max-width: 768px) {
  .app-container { 
    padding: 10px; 
    overflow-x: hidden; // Prevent horizontal scroll
  }
  
  .filter-container {
     &.is-mobile {
        flex-direction: column;
        align-items: stretch;
        gap: 10px;
        
        .filter-item {
           width: 100% !important;
           margin-right: 0;
        }
        
        .action-buttons {
           display: flex;
           gap: 10px;
           
           .el-button {
             flex: 1;
             height: 36px;
             border-radius: 4px;
             margin: 0;
             span { display: inline-block !important; }
           }
        }
     }
  }
  
  .mobile-card-list {
     background: var(--bg-page);
     padding: 4px;
     
     .mobile-card {
        background: var(--bg-card);
        border-radius: 8px;
        padding: 12px;
        margin-bottom: 12px;
        box-shadow: var(--shadow-light);
        border: 1px solid var(--border-light);
        
        .card-header {
           padding-bottom: 8px;
           border-bottom: 1px solid var(--border-light);
           margin-bottom: 8px;
           
           .header-left {
              display: flex;
              align-items: center;
              gap: 8px;
              
              .status-tag { flex-shrink: 0; }
              .card-title {
                 font-weight: bold;
                 font-size: 15px;
                 overflow: hidden;
                 text-overflow: ellipsis;
                 display: -webkit-box;
                 -webkit-line-clamp: 1;
                 line-clamp: 1;
                 -webkit-box-orient: vertical;
                 color: var(--text-primary);
              }
           }
        }
        
        .card-body {
           display: flex;
           gap: 12px;
           margin-bottom: 8px;
           
           .card-cover {
              width: 80px;
              height: 60px;
              border-radius: 4px;
              flex-shrink: 0;
           }
           
           .card-info {
              flex: 1;
              overflow: hidden; // Prevent text overflow
              p {
                 margin: 4px 0;
                 font-size: 13px;
                 color: var(--text-secondary);
                 white-space: nowrap; // Prevent wrapping
                 overflow: hidden;
                 text-overflow: ellipsis;
                 label { color: var(--text-muted); }
              }
           }
        }
        
        .card-footer {
           display: flex;
           justify-content: space-between;
           align-items: center;
           border-top: 1px solid var(--border-light);
           padding-top: 8px;
           
           .footer-left {
              display: flex;
              align-items: center;
              font-size: 12px;
              color: var(--text-secondary);
              .top-label { margin-right: 4px; }
           }
           
           .footer-right {
              button { margin-left: 8px; }
           }
        }
     }
  }
  
  .pagination-container {
     justify-content: center;
     overflow: hidden; // Strictly prevent scroll
     width: 100%;
     padding-bottom: 0; // Remove overflow padding if any
     
     :deep(.el-pagination) {
        flex-wrap: wrap; 
        justify-content: center;
        width: 100%;
        overflow: hidden;
        
        .el-pager li {
           min-width: 24px; // Reduce button width on mobile
        }
        
        button {
           min-width: 24px;
        }
     }
  }
}

// Global mobile tweaks for this page
@media (max-width: 768px) {
  :deep(.el-card__body) {
    padding: 12px; // Match mobile card padding
  }
}
</style>
