<template>
  <div class="app-container">
    <el-card>
      <div class="filter-container">
        <el-input 
           v-model="queryParams.orderNo" 
           placeholder="订单号搜索" 
           style="width: 200px" 
           class="filter-item"
           @keyup.enter="handleQuery"
           clearable
        />
        <el-input 
           v-model="queryParams.pickupCode" 
           placeholder="核销码搜索" 
           style="width: 200px; margin-left: 10px" 
           class="filter-item"
           @keyup.enter="handleQuery"
           clearable
        />
        <el-button type="primary" class="filter-item" style="margin-left: 10px" @click="handleQuery">搜索</el-button>
      </div>

      <el-table v-loading="loading" :data="orderList" border style="margin-top: 20px">
        <el-table-column prop="orderNo" label="订单编号" width="180" />
        <el-table-column prop="receiverName" label="收货人" width="100" />
        <el-table-column label="兑换商品" min-width="150" show-overflow-tooltip>
           <template #default="scope">
              {{ scope.row.goodsName }} x{{ scope.row.quantity }}
           </template>
        </el-table-column>
        <el-table-column prop="pointsCost" label="消耗积分" width="100" />
        <el-table-column prop="pickupCode" label="核销码" width="100" align="center" />
        <el-table-column prop="createTime" label="兑换时间" width="160">
           <template #default="scope">{{ formatTime(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
           <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                 {{ getStatusLabel(scope.row.status) }}
              </el-tag>
           </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center">
           <template #default="scope">
              <el-button 
                 v-if="scope.row.status === 0" 
                 type="primary" 
                 size="small" 
                 @click="handleDeliver(scope.row)"
              >
                 确认发放
              </el-button>
           </template>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const loading = ref(false)
const orderList = ref([])
const total = ref(0)
const queryParams = reactive({
    page: 1,
    size: 10,
    orderNo: '',
    pickupCode: ''
})

const fetchData = async () => {
    loading.value = true
    try {
        const res = await request.get('/admin/mall/exchange/list', { params: queryParams })
        orderList.value = res.data.records
        total.value = res.data.total
    } finally {
        loading.value = false
    }
}

const handleQuery = () => {
    queryParams.page = 1
    fetchData()
}

const formatTime = (t: string) => dayjs(t).format('YYYY-MM-DD HH:mm')

const getStatusType = (s: number) => {
    if (s === 1) return 'success'
    if (s === 2) return 'success'
    if (s === 3) return 'info'
    return 'warning'
}

const getStatusLabel = (s: number) => {
    if (s === 0) return '待发放'
    if (s === 1) return '已发放'
    if (s === 2) return '已完成'
    if (s === 3) return '已取消'
    return '未知'
}

const handleDeliver = async (row: any) => {
    try {
        await ElMessageBox.confirm(`确认发放商品给 ${row.receiverName} 吗？`, '提示', { type: 'success' })
        await request.put('/admin/mall/exchange/deliver', { id: row.id })
        ElMessage.success('发放成功')
        fetchData()
    } catch {}
}

onMounted(fetchData)
</script>

<style scoped>
.app-container { padding: 20px; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
