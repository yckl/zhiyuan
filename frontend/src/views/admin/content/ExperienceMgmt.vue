<template>
  <div class="app-container">
    <el-card>
       <div class="filter-container" :class="{ 'is-mobile': isMobile }">
          <el-select v-model="queryParams.status" placeholder="状态筛选" clearable style="width: 120px" class="filter-item" @change="handleQuery">
             <el-option label="待审核" :value="0" />
             <el-option label="已发布" :value="1" />
             <el-option label="已拒绝" :value="2" />
          </el-select>
          <el-button type="primary" @click="handleQuery" class="filter-item refresh-btn">刷新</el-button>
       </div>

       <!-- Desktop Table -->
       <el-table v-loading="loading" :data="list" border style="margin-top: 20px" class="hidden-sm-and-down">
          <el-table-column label="标题/内容" min-width="300">
             <template #default="scope">
                <div style="font-weight:bold">{{ scope.row.title || '无标题' }}</div>
                <div style="color:#666; font-size:12px; margin-top:5px">{{ truncate(scope.row.content) }}</div>
                <div v-if="scope.row.images" style="margin-top:8px">
                   <el-image 
                      v-for="(img, idx) in parseImages(scope.row.images)" 
                      :key="idx"
                      :src="img"
                      style="width: 50px; height: 50px; margin-right: 5px"
                      :preview-src-list="parseImages(scope.row.images)"
                      preview-teleported>
  <template #error>
    <img :src="'/default-cover.jpg'" style="width:100%;height:100%;object-fit:cover"/>
  </template>
</el-image>
                </div>
             </template>
          </el-table-column>
          <el-table-column prop="createTime" label="提交时间" width="160">
             <template #default="scope">{{ formatTime(scope.row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
             <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                   {{ getStatusLabel(scope.row.status) }}
                </el-tag>
             </template>
          </el-table-column>
          <el-table-column label="操作" width="200" align="center">
             <template #default="scope">
                <template v-if="scope.row.status === 0">
                   <el-button type="success" size="small" @click="handleAudit(scope.row, 1)">通过</el-button>
                   <el-button type="warning" size="small" @click="handleAudit(scope.row, 2)">拒绝</el-button>
                </template>
                <el-button type="danger" size="small" link @click="handleDelete(scope.row)">删除</el-button>
             </template>
          </el-table-column>
       </el-table>

       <!-- Mobile Card List -->
       <div class="hidden-md-and-up mobile-card-list">
          <div v-for="item in list" :key="item.id" class="mobile-card">
             <div class="card-header">
                <span class="card-title">{{ item.title || '无标题' }}</span>
                <el-tag :type="getStatusType(item.status)" size="small">
                   {{ getStatusLabel(item.status) }}
                </el-tag>
             </div>
             <div class="card-body">
                <div class="card-content">{{ truncate(item.content) }}</div>
                <div class="card-images" v-if="item.images && parseImages(item.images).length">
                   <el-image 
                      v-for="(img, idx) in parseImages(item.images)" 
                      :key="idx"
                      :src="img"
                      class="img-item"
                      fit="cover"
                      :preview-src-list="parseImages(item.images)"
                      preview-teleported>
  <template #error>
    <img :src="'/default-cover.jpg'" style="width:100%;height:100%;object-fit:cover"/>
  </template>
</el-image>
                </div>
                <div class="card-time">提交周期: {{ formatTime(item.createTime) }}</div>
             </div>
             <div class="card-footer">
                <div class="footer-left">
                   <el-button v-if="item.status === 0" size="small" type="success" @click="handleAudit(item, 1)">通过</el-button>
                   <el-button v-if="item.status === 0" size="small" type="warning" @click="handleAudit(item, 2)">拒绝</el-button>
                </div>
                <div class="footer-right">
                   <el-button size="small" type="danger" @click="handleDelete(item)">删除</el-button>
                </div>
             </div>
          </div>
          <el-empty v-if="!loading && list.length === 0" description="暂无心得" />
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
              @size-change="handleQuery"
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

const windowWidth = ref(window.innerWidth)
const isMobile = ref(window.innerWidth < 768)

const handleResize = () => {
  windowWidth.value = window.innerWidth
  isMobile.value = windowWidth.value < 768
}

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const queryParams = reactive({
    page: 1,
    size: 10,
    status: undefined
})

const fetchData = async () => {
    loading.value = true
    try {
        const res = await request.get('/admin/experience/list', { params: queryParams })
        list.value = res.data.records
        total.value = res.data.total
    } finally {
        loading.value = false
    }
}

const handleQuery = () => {
    queryParams.page = 1
    fetchData()
}

const handleAudit = async (row: any, status: number) => {
    try {
        await request.put('/admin/experience/audit', {
            id: row.id,
            status: status
        })
        ElMessage.success('操作成功')
        fetchData()
    } catch {}
}

const handleDelete = async (row: any) => {
    try {
        await ElMessageBox.confirm('确认删除?', '提示', { type: 'warning' })
        await request.delete(`/admin/experience/delete/${row.id}`)
        ElMessage.success('删除成功')
        fetchData()
    } catch {}
}

const formatTime = (t: string) => dayjs(t).format('YYYY-MM-DD HH:mm')
const truncate = (s: string) => (s && s.length > 50) ? s.substring(0, 50) + '...' : s

const parseImages = (jsonStr: string) => {
    try {
        return JSON.parse(jsonStr) || []
    } catch {
        return []
    }
}

const getStatusType = (s: number) => {
    if (s === 1) return 'success'
    if (s === 2) return 'danger'
    return 'warning'
}

const getStatusLabel = (s: number) => {
    if (s === 1) return '已发布'
    if (s === 2) return '已拒绝'
    return '待审核'
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
.filter-container { margin-bottom: 20px; display: flex; align-items: center; }

@media only screen and (max-width: 768px) {
  .app-container { padding: 10px; }
  
  .filter-container {
     &.is-mobile {
        justify-content: space-between;
        .filter-item { margin: 0; }
        .refresh-btn { margin-left: auto; }
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
           align-items: flex-start;
           border-bottom: 1px solid var(--border-light);
           padding-bottom: 8px;
           margin-bottom: 8px;
           
           .card-title { font-weight: bold; font-size: 15px; flex: 1; margin-right: 8px; color: var(--text-primary); }
        }
        
        .card-body {
           margin-bottom: 8px;
           
           .card-content {
              font-size: 14px; color: var(--text-secondary); margin-bottom: 8px;
              display: -webkit-box; -webkit-line-clamp: 3; line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden;
           }
           
           .card-images {
              display: grid;
              grid-template-columns: repeat(3, 1fr);
              gap: 6px;
              margin-bottom: 8px;
              
              .img-item {
                 width: 100%;
                 aspect-ratio: 1;
                 border-radius: 4px;
              }
           }
           
           .card-time { font-size: 12px; color: var(--text-muted); }
        }
        
        .card-footer {
           border-top: 1px solid var(--border-light);
           padding-top: 10px;
           display: flex;
           justify-content: space-between;
           align-items: center;
           
           .footer-left { display: flex; gap: 8px; }
           .footer-right { margin-left: auto; }
           
           button { margin: 0; }
        }
     }
  }
  
  .pagination-container {
     justify-content: center;
  }
}
</style>
