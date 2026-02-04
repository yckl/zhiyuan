<template>
  <div class="app-container">
    <el-card>
       <el-tabs v-model="activeTab" @tab-click="handleQuery">
          <el-tab-pane label="待处理" name="pending" />
          <el-tab-pane label="已处理" name="handled" />
       </el-tabs>

       <el-table v-loading="loading" :data="list" border style="margin-top: 10px">
          <el-table-column prop="content" label="反馈内容" min-width="300" />
          <el-table-column prop="contactInfo" label="联系方式" width="150" />
          <el-table-column prop="createTime" label="提交时间" width="160">
             <template #default="scope">{{ formatTime(scope.row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
             <template #default="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'">
                   {{ scope.row.status === 1 ? '已处理' : '待处理' }}
                </el-tag>
             </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center">
             <template #default="scope">
                <el-button 
                   v-if="scope.row.status === 0" 
                   type="primary" 
                   size="small" 
                   @click="handleReply(scope.row)"
                >
                   处理/回复
                </el-button>
                <el-button 
                   v-else 
                   type="info" 
                   size="small" 
                   @click="handleViewReply(scope.row)"
                >
                   查看回复
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
             @current-change="getData"
          />
       </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
       <div v-if="readOnly" style="padding: 10px 0">
          <div style="font-weight:bold; margin-bottom:5px">我的回复：</div>
          <div>{{ currentReply }}</div>
       </div>
       <el-form v-else>
          <el-form-item label="回复内容">
             <el-input 
                v-model="replyContent" 
                type="textarea" 
                rows="5" 
                placeholder="请输入回复内容，用户将收到系统通知"
             />
          </el-form-item>
       </el-form>
       <template #footer>
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button v-if="!readOnly" type="primary" @click="submitReply">确认回复</el-button>
       </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const activeTab = ref('pending')
const loading = ref(false)
const list = ref([])
const total = ref(0)
const queryParams = reactive({ page: 1, size: 10 })

// Dialog
const dialogVisible = ref(false)
const dialogTitle = ref('')
const readOnly = ref(false)
const currentId = ref<number | null>(null)
const replyContent = ref('')
const currentReply = ref('')

const getData = async () => {
    loading.value = true
    try {
        const status = activeTab.value === 'pending' ? 0 : 1
        const res = await request.get('/admin/feedback/list', { 
            params: { ...queryParams, status } 
        })
        list.value = res.data.records
        total.value = res.data.total
    } finally {
        loading.value = false
    }
}

const handleQuery = () => {
    queryParams.page = 1
    getData()
}

const handleReply = (row: any) => {
    dialogTitle.value = '处理反馈'
    readOnly.value = false
    currentId.value = row.id
    replyContent.value = ''
    dialogVisible.value = true
}

const handleViewReply = (row: any) => {
    dialogTitle.value = '查看回复'
    readOnly.value = true
    currentReply.value = row.replyContent || '无回复内容'
    dialogVisible.value = true
}

const submitReply = async () => {
    if (!replyContent.value) return ElMessage.warning('请输入回复内容')
    try {
        await request.put('/admin/feedback/reply', {
            id: currentId.value,
            replyContent: replyContent.value
        })
        ElMessage.success('处理成功')
        dialogVisible.value = false
        getData()
    } catch {}
}

const formatTime = (t: string) => dayjs(t).format('YYYY-MM-DD HH:mm')

onMounted(getData)
</script>

<style scoped>
.app-container { padding: 20px; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
