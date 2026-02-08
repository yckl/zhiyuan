<template>
  <div class="my-experiences">
    <el-page-header @back="router.back()" title="返回" content="我的心得" />

    <div class="header-actions">
      <el-button type="primary" @click="router.push('/experience/create')">
        <el-icon><Plus /></el-icon> 发布心得
      </el-button>
    </div>

    <el-table :data="experiences" v-loading="loading" stripe>
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column prop="activityTitle" label="关联活动" width="180" />
      <el-table-column prop="viewCount" label="浏览" width="80" />
      <el-table-column prop="likeCount" label="点赞" width="80" />
      <el-table-column prop="statusName" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.statusName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="发布时间" width="180" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="router.push(`/experience/${row.id}`)">查看</el-button>
          <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-if="total > 0"
      v-model:current-page="queryParams.page"
      v-model:page-size="queryParams.size"
      :total="total"
      layout="total, prev, pager, next"
      class="pagination"
      @current-change="fetchExperiences"
    />

    <!-- 发布心得对话框 -->
    <el-dialog v-model="showCreateDialog" title="发布心得" width="600px">
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="createForm.title" placeholder="请输入心得标题" />
        </el-form-item>
        <el-form-item label="关联活动">
          <el-select v-model="createForm.activityId" placeholder="选择关联活动" clearable style="width: 100%">
            <el-option v-for="act in myActivities" :key="act.activityId" :label="act.activityTitle" :value="act.activityId" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input v-model="createForm.content" type="textarea" :rows="6" placeholder="分享你的志愿心得..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" :disabled="submitting" @click="handleCreate">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { request } from '@/utils/request'

const router = useRouter()

const loading = ref(false)
const experiences = ref<any[]>([])
const myActivities = ref<any[]>([])
const total = ref(0)
const showCreateDialog = ref(false)
const submitting = ref(false)

const queryParams = reactive({ page: 1, size: 10 })

const createForm = reactive({
  title: '',
  activityId: null as number | null,
  content: '',
  images: '[]'
})

const fetchExperiences = async () => {
  loading.value = true
  try {
    const res = await request.get('/experience/my', queryParams)
    experiences.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('获取心得列表失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchMyActivities = async () => {
  try {
    const res = await request.get('/registration/my', { page: 1, size: 100, status: 3 })
    myActivities.value = res.data?.records || []
  } catch (error) {
    console.error('获取活动失败:', error)
  }
}

const handleCreate = async () => {
  if (!createForm.title || !createForm.content) {
    ElMessage.warning('请填写标题和内容')
    return
  }
  submitting.value = true
  try {
    await request.post('/experience', createForm)
    ElMessage.success('发布成功')
    showCreateDialog.value = false
    createForm.title = ''
    createForm.content = ''
    createForm.activityId = null
    fetchExperiences()
  } catch (error) {
    console.error('发布失败:', error)
  } finally {
    submitting.value = false
  }
}

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定要删除该心得吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/experience/${id}`)
      ElMessage.success('删除成功')
      fetchExperiences()
    } catch (error) {
      console.error('删除失败:', error)
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchExperiences()
  fetchMyActivities()
})
</script>

<style lang="scss" scoped>
.my-experiences {
  .header-actions { margin: 20px 0; }
  .pagination { margin-top: 20px; justify-content: center; }
}
</style>
