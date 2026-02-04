<template>
  <div class="app-container">
    <el-card>
       <div class="filter-container">
          <el-button type="primary" @click="handleAdd">新增轮播图</el-button>
       </div>

       <el-table v-loading="loading" :data="bannerList" border style="margin-top: 20px">
          <el-table-column label="图片预览" width="150" align="center">
             <template #default="scope">
                <el-image 
                   style="width: 120px; height: 60px" 
                   :src="scope.row.image" 
                   fit="cover"
                   :preview-src-list="[scope.row.image]"
                   preview-teleported
                />
             </template>
          </el-table-column>
          <el-table-column prop="title" label="标题" width="150" />
          <el-table-column prop="link" label="跳转链接" min-width="200" show-overflow-tooltip />
          <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
          <el-table-column label="状态" width="100" align="center">
             <template #default="scope">
                <el-tag type="success">启用</el-tag>
             </template>
          </el-table-column>
          <el-table-column label="操作" width="150" align="center">
             <template #default="scope">
                <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
                <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
             </template>
          </el-table-column>
       </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
       <el-form label-width="80px" :model="form">
          <el-form-item label="标题">
             <el-input v-model="form.title" placeholder="可选" />
          </el-form-item>
          <el-form-item label="图片" required>
             <el-upload
                class="avatar-uploader"
                action="/api/file/upload"
                :show-file-list="false"
                :on-success="handleUploadSuccess"
                :headers="uploadHeaders"
                name="file"
             >
                <img v-if="form.image" :src="form.image" class="avatar" />
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
             </el-upload>
             <div class="el-upload__tip">只能上传jpg/png文件，且不超过2MB</div>
          </el-form-item>
          <el-form-item label="跳转链接">
             <el-input v-model="form.link" placeholder="例如 /activity/123" />
          </el-form-item>
          <el-form-item label="排序">
             <el-input-number v-model="form.sortOrder" :min="0" />
          </el-form-item>
       </el-form>
       <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">保存</el-button>
       </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// Assume token is in localStorage for upload headers if needed. 
// Standard setup usually puts token in headers implicitly by browser for cookies or explicitly.
// But el-upload needs explicit headers if token-based auth.
// I'll grab token from localStorage if 'token' key exists.
const token = localStorage.getItem('token')
const uploadHeaders = { Authorization: token }

const loading = ref(false)
const bannerList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = reactive({
    id: undefined,
    title: '',
    image: '',
    link: '',
    sortOrder: 0
})

const fetchData = async () => {
    loading.value = true
    try {
        const res = await request.get('/admin/banner/list')
        bannerList.value = res.data
    } finally {
        loading.value = false
    }
}

const handleAdd = () => {
    dialogTitle.value = '新增轮播图'
    form.id = undefined
    form.title = ''
    form.image = ''
    form.link = ''
    form.sortOrder = 0
    dialogVisible.value = true
}

const handleEdit = (row: any) => {
    dialogTitle.value = '编辑轮播图'
    Object.assign(form, row)
    dialogVisible.value = true
}

const handleUploadSuccess = (res: any) => {
   if (res.code === 200) {
      form.image = res.data.url
   } else {
      ElMessage.error(res.msg || '上传失败')
   }
}

const submitForm = async () => {
    if (!form.image) return ElMessage.warning('请上传图片')
    try {
        const url = form.id ? '/admin/banner/update' : '/admin/banner/add'
        if (form.id) await request.put(url, form)
        else await request.post(url, form)
        ElMessage.success('保存成功')
        dialogVisible.value = false
        fetchData()
    } catch {}
}

const handleDelete = async (row: any) => {
    try {
        await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
        await request.delete(`/admin/banner/delete/${row.id}`)
        ElMessage.success('删除成功')
        fetchData()
    } catch {}
}

onMounted(fetchData)
</script>

<style scoped>
.app-container { padding: 20px; }
.avatar-uploader .avatar { width: 178px; height: 100px; display: block; object-fit: cover; }
.avatar-uploader .el-upload { border: 1px dashed var(--el-border-color); border-radius: 6px; cursor: pointer; position: relative; overflow: hidden; transition: var(--el-transition-duration-fast); }
.avatar-uploader .el-upload:hover { border-color: var(--el-color-primary); }
.avatar-uploader-icon { font-size: 28px; color: #8c939d; width: 178px; height: 100px; text-align: center; line-height: 100px; }
</style>
