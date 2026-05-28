<template>
  <div class="app-container">
    <el-card shadow="never" class="main-card">
       <div class="filter-container">
          <el-input 
             v-model="queryParams.operator" 
             placeholder="操作人" 
             class="filter-item search-input"
             @keyup.enter="handleQuery"
             clearable
          />
          <el-date-picker
             v-model="dateRange"
             type="daterange"
             range-separator="至"
             start-placeholder="开始日期"
             end-placeholder="结束日期"
             value-format="YYYY-MM-DD"
             class="filter-item date-picker"
             @change="handleDateChange"
          />
          <el-button type="primary" class="filter-item search-btn" @click="handleQuery">搜索</el-button>
       </div>

       <!-- Desktop Table -->
       <div class="hidden-sm-and-down">
          <el-table v-loading="loading" :data="logList" border class="log-table">
             <el-table-column prop="id" label="ID" width="80" align="center" />
             <el-table-column prop="operatorName" label="操作人" width="120" />
             <el-table-column prop="role" label="角色" width="100" />
             <el-table-column prop="operation" label="操作内容" min-width="200" show-overflow-tooltip />
             <el-table-column prop="path" label="请求路径" width="200" show-overflow-tooltip />
             <el-table-column prop="ip" label="IP地址" width="140" />
             <el-table-column prop="createTime" label="操作时间" width="160">
                <template #default="scope">{{ formatTime(scope.row.createTime) }}</template>
             </el-table-column>
          </el-table>
       </div>

       <!-- Mobile Card List -->
       <div class="mobile-card-list hidden-md-and-up">
          <div v-for="item in logList" :key="item.id" class="mobile-card">
             <div class="card-header">
                <span class="card-title">{{ item.operation }}</span>
                <el-tag size="small">{{ item.role }}</el-tag>
             </div>
             <div class="card-body">
                <p><label>操作人：</label> {{ item.operatorName }}</p>
                <p><label>请求路径:</label> {{ item.path }}</p>
                <p><label>IP地址:</label> {{ item.ip }}</p>
                <p><label>时间:</label> {{ formatTime(item.createTime) }}</p>
             </div>
          </div>
          <el-empty v-if="!loading && logList.length === 0" description="暂无记录" />
       </div>

       <div class="pagination-container">
          <el-pagination
             v-model:current-page="queryParams.page"
             v-model:page-size="queryParams.size"
             :total="total"
             :pager-count="5"
             layout="total, prev, pager, next"
             background
             @current-change="handleQuery"
          />
       </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import dayjs from 'dayjs'

interface OpLog {
    id: number;
    operatorName: string;
    role: string;
    operation: string;
    path: string;
    ip: string;
    createTime: string;
}

const loading = ref(false)
const logList = ref<OpLog[]>([])
const total = ref(0)
const queryParams = reactive({
    page: 1,
    size: 20,
    operator: '',
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
        const res = await request.get('/admin/system/logs', { params: queryParams })
        logList.value = res.data.records
        total.value = res.data.total
    } finally {
        loading.value = false
    }
}

const handleQuery = () => {
    queryParams.page = 1
    fetchData()
}

const formatTime = (t: string) => dayjs(t).format('YYYY-MM-DD HH:mm:ss')

onMounted(fetchData)
</script>

<style scoped>
.app-container { 
  padding: 20px;
  background: var(--bg-page);
  min-height: calc(100vh - 84px);
}

.main-card {
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: 12px;
}

.filter-container {
  margin-bottom: 20px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.log-table {
  margin-top: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .app-container {
    padding: 12px;
  }
  
  .filter-container {
    flex-direction: column;
    align-items: stretch;
    
    .search-input, .date-picker, .search-btn {
      width: 100% !important;
      margin-left: 0 !important;
    }
  }
  
  .pagination-container {
    justify-content: center;
  }
}

/* Scroll behavior for cards list is handled by global .mobile-card-list or similar */
</style>
