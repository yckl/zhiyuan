<template>
  <div class="app-container">
    <el-card>
      <div class="header-actions">
        <el-button type="primary" @click="handleAdd">新增分类</el-button>
      </div>
      
      <el-table
        v-loading="loading"
        :data="categoryList"
        style="width: 100%; margin-top: 20px"
        border
      >
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column label="分类图标" width="100" align="center">
           <template #default="scope">
              <el-icon v-if="scope.row.icon" size="20">
                  <component :is="scope.row.icon" />
              </el-icon>
              <span v-else>-</span>
           </template>
        </el-table-column>
        <el-table-column prop="name" label="分类名称" min-width="150" />
        <el-table-column prop="status" label="状态" width="100" align="center">
           <template #default="scope">
              <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
                 {{ scope.row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
           </template>
        </el-table-column>
        <el-table-column label="操作" width="250" align="center">
           <template #default="scope">
              <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button 
                 link 
                 :type="scope.row.status === 1 ? 'warning' : 'success'" 
                 @click="handleToggleStatus(scope.row)"
              >
                 {{ scope.row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
           </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
       <el-form label-width="80px" :model="form" ref="formRef">
          <el-form-item label="分类名称" prop="name">
             <el-input v-model="form.name" placeholder="请输入分类名称" />
          </el-form-item>
          <el-form-item label="排序值" prop="sortOrder">
             <el-input-number v-model="form.sortOrder" :min="0" />
          </el-form-item>
          <el-form-item label="图标类名" prop="icon">
             <el-input v-model="form.icon" placeholder="例如: Check / User / ElementPlusIcon" />
          </el-form-item>
       </el-form>
       <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
       </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const categoryList = ref<any[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = reactive({
    id: undefined as number | undefined,
    name: '',
    sortOrder: 0,
    status: 1,
    icon: ''
})

const fetchData = async () => {
    loading.value = true
    try {
        const res = await request.get('/admin/category/list')
        categoryList.value = res.data
    } catch {}
    finally {
        loading.value = false
    }
}

const handleAdd = () => {
    dialogTitle.value = '新增分类'
    form.id = undefined
    form.name = ''
    form.sortOrder = 0
    form.status = 1
    dialogVisible.value = true
}

const handleEdit = (row: any) => {
    dialogTitle.value = '编辑分类'
    Object.assign(form, row)
    dialogVisible.value = true
}

const submitForm = async () => {
    if (!form.name) return ElMessage.warning('请输入名称')
    
    try {
        const url = form.id ? '/admin/category/update' : '/admin/category/add'
        // For Update uses Put, Add uses Post usually.
        // My controller: Add=Post, Update=Put.
        if (form.id) {
            await request.put(url, form)
        } else {
            await request.post(url, form)
        }
        ElMessage.success('操作成功')
        dialogVisible.value = false
        fetchData()
    } catch {}
}

const handleToggleStatus = async (row: any) => {
    const newStatus = row.status === 1 ? 0 : 1
    try {
        await request.put('/admin/category/status', {
            id: row.id,
            status: newStatus
        })
        row.status = newStatus
        ElMessage.success('状态已更新')
    } catch {}
}

const handleDelete = async (row: any) => {
    try {
        await ElMessageBox.confirm('确定要删除该分类吗？', '提示', { type: 'warning' })
        await request.delete(`/admin/category/delete/${row.id}`)
        ElMessage.success('删除成功')
        fetchData()
    } catch {}
}

onMounted(() => {
    fetchData()
})
</script>

<style scoped lang="scss">
.app-container {
    padding: 20px;
}
</style>
