<template>
  <div class="app-container">
    <el-card>
      <div class="filter-container" :class="{ 'is-mobile': isMobile }">
        <el-input 
           v-model="queryParams.name" 
           placeholder="商品名称" 
           style="width: 200px" 
           class="filter-item"
           @keyup.enter="handleQuery"
           clearable
        />
        <el-select v-model="queryParams.status" placeholder="状态筛选" clearable style="width: 120px" class="filter-item status-select">
           <el-option label="上架" :value="1" />
           <el-option label="下架" :value="0" />
        </el-select>
        <div class="action-buttons">
           <el-button type="primary" class="filter-item" @click="handleQuery">搜索</el-button>
           <el-button type="primary" class="filter-item" @click="handleAdd">新增商品</el-button>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="productList"
        border
        style="width: 100%; margin-top: 20px"
        class="hidden-sm-and-down"
      >
        <el-table-column label="商品图片" width="100" align="center">
           <template #default="scope">
              <el-image 
                 style="width: 60px; height: 60px" 
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
        <el-table-column prop="name" label="商品名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="pointsPrice" label="积分价格" width="100" align="center" class-name="hide-on-mobile" />
        <el-table-column label="库存" width="100" align="center" class-name="hide-on-mobile">
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

      <!-- Mobile Card List -->
      <div class="hidden-md-and-up mobile-card-list">
         <div v-for="item in productList" :key="item.id" class="mobile-card">
            <div class="card-content-wrapper">
               <el-image 
                  class="card-image"
                  :src="item.coverImage" 
                  fit="cover" 
                  :preview-src-list="[item.coverImage]"
                  preview-teleported>
  <template #error>
    <img :src="'/default-cover.jpg'" style="width:100%;height:100%;object-fit:cover"/>
  </template>
</el-image>
               <div class="card-info">
                  <div class="card-header-row">
                     <span class="card-title">{{ item.name }}</span>
                     <el-tag :type="item.status === 1 ? 'success' : 'info'" size="small">
                        {{ item.status === 1 ? '上架' : '下架' }}
                     </el-tag>
                  </div>
                  <div class="card-meta">
                     <p><span class="label">价格:</span> <span class="price">{{ item.pointsPrice }} 积分</span></p>
                     <p>
                        <span class="label">库存:</span> 
                        <span :class="{ 'text-danger': item.stock < 5 && item.stock !== -1 }">
                           {{ item.stock === -1 ? '无限' : item.stock }}
                        </span>
                     </p>
                  </div>
               </div>
            </div>
            
            <div class="card-footer">
               <el-button link type="primary" size="small" @click="handleEdit(item)">编辑</el-button>
               <el-button 
                  link 
                  :type="item.status === 1 ? 'warning' : 'success'" 
                  size="small"
                  @click="handleStatusChange(item)"
               >
                  {{ item.status === 1 ? '下架' : '上架' }}
               </el-button>
               <el-button link type="danger" size="small" @click="handleDelete(item)">删除</el-button>
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
            @size-change="handleSizeChange"
            @current-change="fetchData"
         />
      </div>
    </el-card>

    <!-- Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" :width="isMobile ? '90%' : '600px'">
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
                <img v-if="form.coverImage" :src="form.coverImage" class="avatar" @error="(e) => ((e.target as any).src = '/default-cover.jpg')" />
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
             </el-upload>
          </el-form-item>
          <el-form-item label="积分价格" prop="pointsPrice" required>
             <el-input-number v-model="form.pointsPrice" :min="0" />
          </el-form-item>
          <el-form-item label="库存数量" prop="stock" required>
             <el-input-number v-model="form.stock" :min="-1" placeholder="-1表示无限" />
             <span style="margin-left: 10px; color: #999">* -1 代表无限库存</span>
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
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import request from '@/utils/request'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import 'element-plus/theme-chalk/display.css' // Ensure display classes are available

const windowWidth = ref(window.innerWidth)
const isMobile = ref(window.innerWidth < 768)

const handleResize = () => {
  windowWidth.value = window.innerWidth
  isMobile.value = windowWidth.value < 768
}

const token = localStorage.getItem('token')
const uploadHeaders = { Authorization: token }

const loading = ref(false)
const productList = ref<any[]>([])
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

const handleSizeChange = () => {
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
        await ElMessageBox.confirm('确认删除?', '提示', { type: 'warning' })
        await request.delete(`/admin/mall/product/delete/${row.id}`)
        ElMessage.success('删除成功')
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
.avatar-uploader .avatar { width: 100px; height: 100px; display: block; object-fit: cover; }
.avatar-uploader .el-upload { border: 1px dashed var(--el-border-color); border-radius: 6px; cursor: pointer; position: relative; overflow: hidden; }
.avatar-uploader-icon { font-size: 28px; color: #8c939d; width: 100px; height: 100px; text-align: center; line-height: 100px; }
.filter-container { display: flex; align-items: center; flex-wrap: wrap; gap: 10px; margin-bottom: 20px; }

@media only screen and (max-width: 768px) {
  .app-container { padding: 10px; }
  
  .filter-container {
     &.is-mobile {
        flex-direction: column;
        align-items: stretch;
        
        .filter-item { width: 100% !important; margin: 0 !important; }
        .status-select { width: 100% !important; margin: 0 !important; }
        
        .action-buttons {
           display: flex;
           gap: 10px;
           .filter-item { flex: 1; }
        }
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
        
        .card-content-wrapper {
           display: flex;
           gap: 12px;
           margin-bottom: 12px;
           
           .card-image {
              width: 80px;
              height: 80px;
              border-radius: 4px;
              flex-shrink: 0;
           }
           
           .card-info {
              flex: 1;
              min-width: 0;
              
              .card-header-row {
                 display: flex;
                 justify-content: space-between;
                 align-items: flex-start;
                 margin-bottom: 8px;
                 
                 .card-title {
                    font-weight: bold;
                    font-size: 15px;
                    color: var(--text-primary);
                    margin-right: 8px;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    display: -webkit-box;
                    -webkit-line-clamp: 2;
                    line-clamp: 2;
                    -webkit-box-orient: vertical;
                 }
              }
              
              .card-meta {
                 font-size: 13px;
                 color: var(--text-secondary);
                 
                 p {
                    margin: 4px 0;
                    display: flex;
                    align-items: center;
                    
                    .label { color: var(--text-muted); margin-right: 4px; }
                    .price { color: #f56c6c; font-weight: bold; }
                    .text-danger { color: #f56c6c; font-weight: bold; }
                 }
              }
           }
        }
        
        .card-footer {
           border-top: 1px solid var(--border-light);
           padding-top: 10px;
           display: flex;
           justify-content: flex-end;
           gap: 10px;
           
           button { margin: 0; }
        }
     }
  }
  
  .pagination-container {
     justify-content: center;
  }
}
</style>
