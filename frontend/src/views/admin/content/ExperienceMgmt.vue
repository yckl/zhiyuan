<template>
  <div class="app-container">
    <el-card>
       <div class="filter-container">
          <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 120px" @change="handleQuery">
             <el-option label="待审核" :value="0" />
             <el-option label="已发布" :value="1" />
             <el-option label="已拒绝" :value="2" />
          </el-select>
          <el-button type="primary" @click="handleQuery" style="margin-left:10px">刷新</el-button>
       </div>

       <el-table v-loading="loading" :data="list" border style="margin-top: 20px">
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
                      preview-teleported
                   />
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
const list = ref([])
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
        await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
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

onMounted(fetchData)
</script>

<style scoped>
.app-container { padding: 20px; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
