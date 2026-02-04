<template>
  <div class="app-container">
    <el-card>
       <div class="filter-container">
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
             range-separator="至"
             start-placeholder="开始日期"
             end-placeholder="结束日期"
             value-format="YYYY-MM-DD"
             style="margin-left: 10px"
             @change="handleDateChange"
          />
          <el-button type="primary" style="margin-left: 10px" @click="handleQuery">搜索</el-button>
       </div>

       <el-table v-loading="loading" :data="logList" border style="margin-top: 20px">
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
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import dayjs from 'dayjs'

const loading = ref(false)
const logList = ref([])
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
.app-container { padding: 20px; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
