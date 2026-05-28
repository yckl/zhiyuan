<template>
  <div class="app-container">
    <el-card>

        <div class="filter-container" :class="{ 'is-mobile': isMobile }">
           <el-input 
              v-model="queryParams.operator" 
              placeholder="操作人" 
              style="width: 200px" 
              class="filter-item"
              @keyup.enter="handleQuery"
              clearable
           />
           <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="?
              start-placeholder="开始日?
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="margin-left: 10px"
              class="filter-item date-picker"
              @change="handleDateChange"
           />
           <el-button type="primary" class="filter-item" style="margin-left: 10px" @click="handleQuery" :icon="Search">搜索</el-button>
        </div>
 
        <!-- Desktop Table -->
        <el-table v-loading="loading" :data="logList" border style="margin-top: 20px" class="hidden-sm-and-down">
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

         <!-- Mobile Card List -->
         <div class="hidden-md-and-up mobile-card-list">
            <div v-for="item in logList" :key="item.id" class="mobile-card">
               <div class="card-header">
                  <span class="card-title">{{ item.operation }}</span>
                  <el-tag size="small" type="info">{{ item.role }}</el-tag>
               </div>
               <div class="card-body">
                  <p><label>操作人：</label>{{ item.operatorName }}</p>
                  <p><label>时间：</label>{{ formatTime(item.createTime) }}</p>
                  <p><label>IP：</label>{{ item.ip }}</p>
               </div>
            </div>
            <el-empty v-if="!loading && logList.length === 0" description="暂无日志" />
         </div>
 
        <div class="pagination-container">
           <el-pagination
              v-model:current-page="queryParams.page"
              v-model:page-size="queryParams.size"
              :total="total"
              layout="total, prev, pager, next"
              @current-change="handleQuery"
           />
        </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import request from '@/utils/request'
import dayjs from 'dayjs'
import { Search } from '@element-plus/icons-vue'

const windowWidth = ref(window.innerWidth)
const isMobile = ref(window.innerWidth < 768)

const handleResize = () => {
  windowWidth.value = window.innerWidth
  isMobile.value = windowWidth.value < 768
}

const loading = ref(false)
const logList = ref<any[]>([])
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

onMounted(() => {
    window.addEventListener('resize', handleResize)
    fetchData()
})

onUnmounted(() => {
    window.removeEventListener('resize', handleResize)
})
</script>

<style scoped lang="scss">
.app-container { padding: 20px; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
.filter-container { display: flex; flex-wrap: wrap; gap: 10px; margin-bottom: 20px; }

@media only screen and (max-width: 768px) {
  .app-container { padding: 10px; }
  
  .filter-container {
     &.is-mobile {
        flex-direction: column;
        align-items: stretch;
        
        .filter-item { width: 100% !important; margin: 0 !important; }
        .date-picker { width: 100% !important; margin: 0 !important; }
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
           display: flex;
           justify-content: space-between;
           align-items: center;
           border-bottom: 1px solid var(--border-light);
           padding-bottom: 8px;
           margin-bottom: 8px;
           
           .card-title { font-weight: bold; font-size: 15px; color: var(--text-primary); }
        }
        
        .card-body {
           font-size: 14px; color: var(--text-secondary);
           p { margin: 6px 0; display: flex; label { min-width: 60px; color: var(--text-muted); } }
        }
     }
  }
  
  .pagination-container {
     justify-content: center;
  }
}
</style>
