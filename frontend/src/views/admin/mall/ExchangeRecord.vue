<template>
  <div class="app-container">
    <el-card>
      <div class="filter-container" :class="{ 'is-mobile': isMobile }">
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

      <el-table v-loading="loading" :data="orderList" border style="margin-top: 20px" class="hidden-sm-and-down">
        <el-table-column prop="orderNo" label="订单编号" width="180" class-name="hide-on-mobile" />
        <el-table-column prop="volunteerId" label="志愿者ID" width="100" align="center" class-name="hide-on-mobile" />
        <el-table-column prop="receiverName" label="收货人" width="100" />
        <el-table-column label="兑换商品" min-width="150" show-overflow-tooltip>
           <template #default="scope">
              {{ scope.row.goodsName }} x{{ scope.row.quantity }}
           </template>
        </el-table-column>
        <el-table-column prop="pointsCost" label="消耗积分" width="100" class-name="hide-on-mobile" />
        <el-table-column prop="pickupCode" label="核销码" width="100" align="center" class-name="hide-on-mobile" />
        <el-table-column prop="createTime" label="兑换时间" width="160" class-name="hide-on-mobile">
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

      <!-- Mobile Card List -->
      <div class="hidden-md-and-up mobile-card-list">
         <div v-for="item in orderList" :key="item.id" class="mobile-card">
            <div class="card-header">
               <span class="card-title">{{ item.orderNo }}</span>
               <el-tag :type="getStatusType(item.status)" size="small">{{ getStatusLabel(item.status) }}</el-tag>
            </div>
            <div class="card-body">
               <p><label>收货人：</label>{{ item.receiverName }}</p>
               <p><label>商品：</label>{{ item.goodsName }} x{{ item.quantity }}</p>
               <p><label>积分：</label>{{ item.pointsCost }}</p>
               <p><label>时间：</label>{{ formatTime(item.createTime) }}</p>
               <p v-if="item.pickupCode"><label>核销码：</label><span class="pickup-code">{{ item.pickupCode }}</span></p>
            </div>
            <div class="card-footer" v-if="item.status === 0">
               <el-button type="primary" size="small" @click="handleDeliver(item)">确认发放</el-button>
            </div>
         </div>
      </div>

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
            @size-change="fetchData"
            @current-change="fetchData"
         />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import 'element-plus/theme-chalk/display.css' // Ensure display classes are available

const windowWidth = ref(window.innerWidth)
const isMobile = ref(window.innerWidth < 768)

const handleResize = () => {
  windowWidth.value = window.innerWidth
  isMobile.value = windowWidth.value < 768
}

const loading = ref(false)
const orderList = ref<any[]>([])
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
  .w-100 { width: 100%; }
  
  .filter-container {
     &.is-mobile {
        flex-direction: column;
        align-items: stretch;
        
        .filter-item { width: 100% !important; margin: 0 !important; }
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
        border: 1px solid var(--border-color);
        
        .card-header {
           display: flex;
           justify-content: space-between;
           align-items: center;
           border-bottom: 1px solid var(--border-light);
           padding-bottom: 8px;
           margin-bottom: 8px;
           
           .card-title { font-size: 13px; color: var(--text-secondary); font-family: monospace; }
        }
        
        .card-body {
           margin-bottom: 8px;
           p { margin: 6px 0; font-size: 14px; color: var(--text-primary); display: flex; label { min-width: 60px; color: var(--text-muted); } }
           .pickup-code { font-family: monospace; font-weight: bold; background: var(--el-color-success-light-9); color: var(--el-color-success); padding: 0 6px; border-radius: 4px; border: 1px solid var(--el-color-success-light-5); }
        }
        
        .card-footer {
           border-top: 1px solid var(--border-light);
           padding-top: 10px;
           text-align: right;
        }
     }
  }
  
  .pagination-container {
     justify-content: center;
  }
}
</style>
