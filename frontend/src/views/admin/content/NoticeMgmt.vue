<template>
  <div class="app-container">
    <el-card>
      <div class="filter-container" :class="{ 'is-mobile': isMobile }">
        <el-input 
          v-model="queryParams.title" 
          placeholder="公告标题" 
          style="width: 200px" 
          class="filter-item"
          @keyup.enter="handleQuery"
          clearable
        />
        <el-button type="primary" class="filter-item" @click="handleQuery" :icon="Search">搜索</el-button>
        <el-button type="success" class="filter-item" @click="handleAdd" :icon="Plus">发布公告</el-button>
      </div>

      <!-- Desktop Table -->
      <el-table v-loading="loading" :data="noticeList" border style="margin-top: 20px" class="hidden-sm-and-down">
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column label="置顶" width="80" align="center">
           <template #default="scope">
              <el-tag :type="scope.row.isTop === 1 ? 'danger' : 'info'">
                 {{ scope.row.isTop === 1 ? '是' : '否' }}
              </el-tag>
           </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览量" width="100" align="center" />
        <el-table-column prop="publishTime" label="发布时间" width="160">
           <template #default="scope">
              {{ formatTime(scope.row.publishTime) }}
           </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
           <template #default="scope">
              <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
           </template>
        </el-table-column>
      </el-table>
      
      <!-- Mobile Card List -->
      <div class="hidden-md-and-up mobile-card-list">
        <div v-for="item in noticeList" :key="item.id" class="mobile-card">
           <div class="card-header">
              <span class="card-title">{{ item.title }}</span>
              <el-tag v-if="item.isTop === 1" type="danger" size="small" effect="dark">置顶</el-tag>
           </div>
           <div class="card-body">
              <p><label>浏览量：</label>{{ item.viewCount }}</p>
              <p><label>发布时间：</label>{{ formatTime(item.publishTime) }}</p>
           </div>
           <div class="card-footer">
              <el-button size="small" type="primary" :icon="Edit" @click="handleEdit(item)">编辑</el-button>
              <el-button size="small" type="danger" :icon="Delete" @click="handleDelete(item)">删除</el-button>
           </div>
        </div>
        <el-empty v-if="!loading && noticeList.length === 0" description="暂无公告" />
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

    <!-- Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" :width="isMobile ? '90%' : '600px'">
       <el-form label-width="80px" :model="form" ref="formRef">
          <el-form-item label="标题" prop="title">
             <el-input v-model="form.title" placeholder="请输入标题" />
          </el-form-item>
          <el-form-item label="内容" prop="content">
             <el-input 
                v-model="form.content" 
                type="textarea" 
                rows="10" 
                placeholder="请输入公告内容" 
             />
          </el-form-item>
          <el-form-item label="置顶">
             <el-switch v-model="form.isTopBool" active-text="是" inactive-text="否" />
          </el-form-item>
       </el-form>
       <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">发布</el-button>
       </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { Search, Plus, Edit, Delete } from '@element-plus/icons-vue'

const windowWidth = ref(window.innerWidth)
const isMobile = ref(window.innerWidth < 768)

const handleResize = () => {
  windowWidth.value = window.innerWidth
  isMobile.value = windowWidth.value < 768
}

interface Notice {
  id: number
  title: string
  content: string
  isTop: number
  viewCount: number
  publishTime: string
}

const loading = ref(false)
const noticeList = ref<Notice[]>([])
const total = ref(0)
const queryParams = reactive({ page: 1, size: 10, title: '' })

const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = reactive({
    id: undefined,
    title: '',
    content: '',
    isTopBool: false
})

const fetchData = async () => {
    loading.value = true
    try {
        const res = await request.get('/admin/notice/list', { params: queryParams })
        noticeList.value = res.data.records
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
    dialogTitle.value = '发布公告'
    form.id = undefined
    form.title = ''
    form.content = ''
    form.isTopBool = false
    dialogVisible.value = true
}

const handleEdit = (row: any) => {
    dialogTitle.value = '编辑公告'
    form.id = row.id
    form.title = row.title
    form.content = row.content
    form.isTopBool = row.isTop === 1
    dialogVisible.value = true
}

const submitForm = async () => {
    if(!form.title || !form.content) return ElMessage.warning('请填写完整内容')
    try {
        const url = form.id ? '/admin/notice/update' : '/admin/notice/add'
        const data = {
            ...form,
            isTop: form.isTopBool ? 1 : 0
        }
        if (form.id) await request.put(url, data)
        else await request.post(url, data)
        
        ElMessage.success('操作成功')
        dialogVisible.value = false
        fetchData()
    } catch {}
}

const handleDelete = async (row: any) => {
    try {
        await ElMessageBox.confirm('确定要删除吗?', '提示', { type: 'warning' })
        await request.delete(`/admin/notice/delete/${row.id}`)
        ElMessage.success('删除成功')
        fetchData()
    } catch {}
}

const formatTime = (t: string) => dayjs(t).format('YYYY-MM-DD HH:mm')

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
.filter-container { display: flex; gap: 10px; margin-bottom: 20px; flex-wrap: wrap; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }

@media only screen and (max-width: 768px) {
  .app-container { padding: 10px; }
  
  .filter-container {
     &.is-mobile {
        flex-direction: column;
        align-items: stretch;
        
        .filter-item {
           width: 100% !important;
           margin: 0;
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
        border: 1px solid var(--border-light);
        
        .card-header {
           display: flex;
           justify-content: space-between;
           align-items: center;
           border-bottom: 1px solid var(--border-light);
           padding-bottom: 8px;
           margin-bottom: 8px;
           
           .card-title { font-weight: bold; font-size: 15px; color: var(--text-primary); }
        }
        
        .card-body {
           p { margin: 6px 0; font-size: 13px; color: var(--text-secondary); label { color: var(--text-muted); } }
        }
        
        .card-footer {
           border-top: 1px solid var(--border-light);
           padding-top: 10px;
           margin-top: 8px;
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
