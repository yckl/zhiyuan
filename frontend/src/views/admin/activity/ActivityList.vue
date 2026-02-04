<template>
  <div class="app-container">
    <el-card>
      <!-- Filter -->
      <div class="filter-container">
        <el-input
          v-model="queryParams.title"
          placeholder="活动标题"
          style="width: 200px"
          class="filter-item"
          @keyup.enter="handleQuery"
          clearable
        />
        <el-input
          v-model="queryParams.organizerName"
          placeholder="发布组织"
          style="width: 200px"
          class="filter-item"
          @keyup.enter="handleQuery"
          clearable
        />
        <el-select
          v-model="queryParams.status"
          placeholder="状态"
          clearable
          style="width: 120px"
          class="filter-item"
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
          class="filter-item"
          value-format="YYYY-MM-DD"
          @change="handleDateChange"
        />
        <el-button type="primary" class="filter-item" @click="handleQuery">搜索</el-button>
        <el-button class="filter-item" @click="resetQuery">重置</el-button>
      </div>

      <!-- Table -->
      <el-table
        v-loading="loading"
        :data="activityList"
        style="width: 100%"
        border
      >
        <el-table-column label="封面" width="100" align="center">
          <template #default="scope">
            <el-image
              style="width: 80px; height: 50px"
              :src="scope.row.coverImage"
              fit="cover"
              :preview-src-list="[scope.row.coverImage]"
              preview-teleported
            />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="活动标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="organizerName" label="发布人" width="150" show-overflow-tooltip />
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
        <el-table-column label="状态" width="100" align="center">
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

      <!-- Pagination -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
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
         <p><strong>发布人:</strong> {{ currentActivity.organizerName }}</p>
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
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'

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
    fetchData()
})
</script>

<style scoped lang="scss">
.app-container {
  padding: 20px;
}
.filter-container {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.pagination-container {
   margin-top: 20px;
   display: flex;
   justify-content: flex-end;
}
</style>
