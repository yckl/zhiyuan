<template>
  <div class="app-container">
    <el-card>
      <div class="filter-container">
        <el-input 
           v-model="queryParams.name" 
           placeholder="商品名称" 
           style="width: 200px" 
           class="filter-item"
           @keyup.enter="handleQuery"
           clearable
        />
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 120px; margin-left: 10px">
           <el-option label="上架" :value="1" />
           <el-option label="下架" :value="0" />
        </el-select>
        <el-button type="primary" class="filter-item" style="margin-left: 10px" @click="handleQuery">搜索</el-button>
        <el-button type="primary" class="filter-item" @click="handleAdd">新增商品</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="productList"
        border
        style="width: 100%; margin-top: 20px"
      >
        <el-table-column label="商品图片" width="100" align="center">
           <template #default="scope">
              <el-image 
                 style="width: 60px; height: 60px" 
                 :src="scope.row.coverImage" 
                 fit="cover"
                 :preview-src-list="[scope.row.coverImage]"
                 preview-teleported 
              />
           </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="pointsPrice" label="积分价格" width="100" align="center" />
        <el-table-column label="库存" width="100" align="center">
           <template #default="scope">
              <span :style="scope.row.stock < 5 ? 'color: red; font-weight: bold' : ''">
                 {{ scope.row.stock === -1 ? '无限' : scope.row.stock }}
              </span>
           </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
           <template #default="scope">
              <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
                 {{ scope.row.status === 1 ? '已上架' : '已下架' }}
              </el-tag>
           </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
           <template #default="scope">
              <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button 
                 link 
                 :type="scope.row.status === 1 ? 'warning' : 'success'" 
                 @click="handleStatusChange(scope.row)"
              >
                 {{ scope.row.status === 1 ? '下架' : '上架' }}
              </el-button>
              <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
           </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
         <el-pagination
            v-model:current-page="queryParams.page"
            v-model:page-size="queryParams.size"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleQuery"
            @current-change="handleQuery"
         />
      </div>
    </el-card>

    <!-- Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
       <el-form label-width="100px" :model="form" ref="formRef">
          <el-form-item label="商品名称" prop="name" required>
             <el-input v-model="form.name" />
          </el-form-item>
          <el-form-item label="封面图片" prop="coverImage" required>
             <el-upload
                class="avatar-uploader"
                action="/api/file/upload/cover"
                :show-file-list="false"
                :on-success="handleUploadSuccess"
                :headers="uploadHeaders"
                name="file"
             >
                <img v-if="form.coverImage" :src="form.coverImage" class="avatar" />
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
             </el-upload>
          </el-form-item>
          <el-form-item label="积分价格" prop="pointsPrice" required>
             <el-input-number v-model="form.pointsPrice" :min="0" />
          </el-form-item>
          <el-form-item label="库存数量" prop="stock" required>
             <el-input-number v-model="form.stock" :min="-1" placeholder="-1表示无限" />
             <span style="margin-left: 10px; color: #999">填 -1 代表无限库存</span>
          </el-form-item>
          <el-form-item label="排序" prop="sortOrder">
             <el-input-number v-model="form.sortOrder" :min="0" />
          </el-form-item>
          <el-form-item label="商品描述">
             <el-input v-model="form.description" type="textarea" rows="3" />
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

const token = localStorage.getItem('token')
const uploadHeaders = { Authorization: token }

const loading = ref(false)
const productList = ref([])
const total = ref(0)
const queryParams = reactive({
    page: 1,
    size: 10,
    name: '',
    status: undefined
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = reactive({
    id: undefined,
    name: '',
    coverImage: '',
    pointsPrice: 0,
    stock: 0,
    sortOrder: 0,
    description: '',
    status: 0
})

const fetchData = async () => {
    loading.value = true
    try {
        const res = await request.get('/admin/mall/product/list', { params: queryParams })
        productList.value = res.data.records
        total.value = res.data.total
    } finally {
        loading.value = false
    }
}

const handleQuery = () => {
    queryParams.page = 1
    fetchData()
}

const handleAdd = () => {
    dialogTitle.value = '新增商品'
    form.id = undefined
    form.name = ''
    form.coverImage = ''
    form.pointsPrice = 0
    form.stock = 0
    form.sortOrder = 0
    form.description = ''
    form.status = 0
    dialogVisible.value = true
}

const handleEdit = (row: any) => {
    dialogTitle.value = '编辑商品'
    Object.assign(form, row)
    dialogVisible.value = true
}

const handleUploadSuccess = (res: any) => {
    if (res.code === 200) {
        form.coverImage = res.data.url
    } else {
        ElMessage.error(res.msg || '上传失败')
    }
}

const submitForm = async () => {
    if (!form.name || !form.coverImage) return ElMessage.warning('请填写完整信息')
    try {
        const url = form.id ? '/admin/mall/product/update' : '/admin/mall/product/add'
        if (form.id) await request.put(url, form)
        else await request.post(url, form)
        ElMessage.success('保存成功')
        dialogVisible.value = false
        fetchData()
    } catch {}
}

const handleStatusChange = async (row: any) => {
    const newStatus = row.status === 1 ? 0 : 1
    try {
        await request.put('/admin/mall/product/status', {
            id: row.id,
            status: newStatus
        })
        row.status = newStatus
        ElMessage.success('操作成功')
    } catch {}
}

const handleDelete = async (row: any) => {
    try {
        await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
        await request.delete(`/admin/mall/product/delete/${row.id}`)
        ElMessage.success('删除成功')
        fetchData()
    } catch {}
}

onMounted(fetchData)
</script>

<style scoped>
.app-container { padding: 20px; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
.avatar-uploader .avatar { width: 100px; height: 100px; display: block; object-fit: cover; }
.avatar-uploader .el-upload { border: 1px dashed var(--el-border-color); border-radius: 6px; cursor: pointer; position: relative; overflow: hidden; }
.avatar-uploader-icon { font-size: 28px; color: #8c939d; width: 100px; height: 100px; text-align: center; line-height: 100px; }
</style>
