<template>
  <div class="app-container">
    <el-card>
      <div slot="header" class="clearfix">
        <span>待审核活动</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="fetchData">刷新</el-button>
      </div>

      <el-table v-loading="loading" :data="list" border style="margin-top: 10px">
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

       <div class="pagination-container">
          <el-pagination
             v-model:current-page="queryParams.page"
             v-model:page-size="queryParams.size"
             :total="total"
             layout="total, prev, pager, next"
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
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const list = ref([])
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
        await ElMessageBox.confirm(`确认通过活动 "${row.title}" 吗?`, '提示', { type: 'success' })
        await request.put('/admin/activity/audit', {
            id: row.id,
            pass: true
        })
        ElMessage.success('已通过')
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

onMounted(fetchData)
</script>

<style scoped>
.app-container { padding: 20px; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
