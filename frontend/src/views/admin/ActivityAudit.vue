<template>
  <div class="app-container">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <span>待审核活动</span>
          <el-button link type="primary" @click="fetchData">刷新</el-button>
        </div>
      </template>

      <!-- Desktop Table -->
      <el-table v-loading="loading" :data="list" border style="margin-top: 10px" class="hidden-sm-and-down">
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
        <el-table-column prop="title" label="活动标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="organizerName" label="组织者" width="150" />
        <el-table-column label="提交时间" width="160">
           <template #default="scope">{{ formatTime(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
           <template #default="scope">
              <el-button type="success" size="small" @click="handleAudit(scope.row, 1)">通过</el-button>
              <el-button type="danger" size="small" @click="handleReject(scope.row)">拒绝</el-button>
           </template>
        </el-table-column>
      </el-table>

      <!-- Mobile Card List -->
      <div class="hidden-md-and-up mobile-card-list">
        <div v-for="item in list" :key="item.id" class="mobile-card">
           <div class="card-content">
              <el-image 
                 class="card-img"
                 :src="item.coverImage" 
                 fit="cover">
  <template #error>
    <img :src="'/default-cover.jpg'" style="width:100%;height:100%;object-fit:cover"/>
  </template>
</el-image>
              <div class="card-info">
                 <div class="card-title">{{ item.title }}</div>
                 <div class="card-meta">
                    <span>{{ item.organizerName }}</span>
                    <span>{{ formatTime(item.createTime) }}</span>
                 </div>
              </div>
           </div>
           <div class="card-footer">
              <el-button type="success" size="small" @click="handleAudit(item, 1)">通过</el-button>
              <el-button type="danger" size="small" @click="handleReject(item)">拒绝</el-button>
           </div>
        </div>
        <el-empty v-if="!loading && list.length === 0" description="暂无待审核活动" />
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

    <!-- Reject Dialog -->
    <el-dialog v-model="rejectVisible" title="拒绝原因" width="400px">
       <el-input v-model="rejectReason" type="textarea" placeholder="请输入拒绝原因" rows="3" />
       <template #footer>
          <el-button @click="rejectVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReject">确认拒绝</el-button>
       </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import request from '@/utils/request'
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox } from 'element-plus'

const windowWidth = ref(window.innerWidth)
const isMobile = ref(window.innerWidth < 768)

const handleResize = () => {
  windowWidth.value = window.innerWidth
  isMobile.value = windowWidth.value < 768
}

interface Activity {
  id: number
  title: string
  coverImage: string
  organizerName: string
  createTime: string
}

const loading = ref(false)
const list = ref<Activity[]>([])
const total = ref(0)
const queryParams = reactive({
    page: 1,
    size: 10,
    status: 1 // 1: Pending Audit
})

const rejectVisible = ref(false)
const rejectReason = ref('')
const currentId = ref<number | null>(null)

const fetchData = async () => {
    loading.value = true
    try {
        // Query activity list with audit_status = 1 (Pending)
        // or ensure backend supports status=1 filter
        const res = await request.get('/admin/activity/list', { params: queryParams })
        list.value = res.data.records
        total.value = res.data.total
    } finally {
        loading.value = false
    }
}

const formatTime = (t: string) => dayjs(t).format('YYYY-MM-DD HH:mm')

const handleAudit = async (row: any, pass: number) => {
    try {
        await ElMessageBox.confirm(`确认${pass === 1 ? '通过' : '拒绝'}活动 "${row.title}" ?`, '提示', { 
            type: pass === 1 ? 'success' : 'warning' 
        })
        await request.put('/admin/activity/audit', {
            id: row.id,
            pass: pass === 1
        })
        ElMessage.success(pass === 1 ? '已通过' : '已拒绝')
        fetchData()
    } catch {}
}

const handleReject = (row: any) => {
    currentId.value = row.id
    rejectReason.value = ''
    rejectVisible.value = true
}

const submitReject = async () => {
    if (!rejectReason.value) return ElMessage.warning('请输入原因')
    try {
        await request.put('/admin/activity/audit', {
            id: currentId.value,
            pass: false,
            remark: rejectReason.value
        })
        ElMessage.success('已拒绝')
        rejectVisible.value = false
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: var(--text-primary);
  font-weight: bold;
}

.pagination-container { 
  margin-top: 20px; 
  display: flex; 
  justify-content: flex-end; 
}

@media only screen and (max-width: 768px) {
  .app-container { padding: 10px; }
  
  .mobile-card-list {
     background: var(--bg-page);
     padding: 4px;
  }
  
  .mobile-card {
     background: var(--bg-card);
     border-radius: 8px;
     padding: 12px;
     margin-bottom: 12px;
     box-shadow: var(--shadow-light);
     border: 1px solid var(--border-light);
  }
  
  .card-content {
     display: flex;
     gap: 12px;
     margin-bottom: 12px;
  }
  
  .card-img {
     width: 80px;
     height: 60px;
     border-radius: 4px;
     flex-shrink: 0;
  }
  
  .card-info {
     flex: 1;
     display: flex;
     flex-direction: column;
     justify-content: space-between;
  }
  
  .card-title {
     font-size: 15px;
     font-weight: bold;
     color: var(--text-primary);
     margin-bottom: 4px;
     display: -webkit-box;
     -webkit-line-clamp: 2;
     line-clamp: 2;
     -webkit-box-orient: vertical;
     overflow: hidden;
  }
  
  .card-meta {
     font-size: 12px;
     color: var(--text-muted);
     display: flex;
     flex-direction: column;
     gap: 2px;
  }
  
  .card-footer {
     border-top: 1px solid var(--border-light);
     padding-top: 10px;
     display: flex;
     gap: 10px;
  }
  
  .card-footer button {
     flex: 1;
     margin: 0;
     height: 32px;
     border-radius: 4px;
  }
  
  .pagination-container {
     justify-content: center;
  }
}
</style>
