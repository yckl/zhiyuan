<template>
  <div class="app-container">
    <el-card>
      <!-- 搜索与筛选 -->
      <div class="filter-container" :class="{ 'is-mobile': isMobile }">
        <el-input
          v-model="queryParams.name"
          placeholder="搜索组织名称"
          class="filter-item search-input"
          @keyup.enter="handleQuery"
          clearable
          @clear="handleQuery"
        />
        <el-select
          v-model="queryParams.status"
          placeholder="状态筛选"
          clearable
          class="filter-item status-select"
          @change="handleQuery"
        >
          <el-option label="待审" :value="0" />
          <el-option label="正常" :value="1" />
          <el-option label="已禁用" :value="2" />
        </el-select>
        <div class="action-buttons">
          <el-button type="primary" class="filter-item" @click="handleQuery" :icon="Search">
            <span>搜索</span>
          </el-button>
          <el-button class="filter-item" @click="resetQuery" :icon="Refresh">
             <span>重置</span>
          </el-button>
        </div>
      </div>

      <!-- 数据表格 (PC端) -->
      <el-table
        v-loading="loading"
        :data="organizerList"
        style="width: 100%"
        border
        class="hidden-sm-and-down"
      >
        <el-table-column label="Logo" width="80" align="center">
          <template #default="scope">
            <el-avatar :size="40" :src="scope.row.logo || ''">
              {{ scope.row.orgName?.charAt(0) }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="orgName" label="组织名称" min-width="150" />
        <el-table-column prop="orgType" label="类型" width="100" />
        <el-table-column prop="contactPerson" label="负责人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="120" />
        <el-table-column prop="activityCount" label="发布活动" width="100" align="center" />
        <el-table-column prop="createTime" label="入驻时间" width="160">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row)">
              {{ getStatusLabel(scope.row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="scope">
            <template v-if="scope.row.verified === 0">
              <el-button link type="primary" @click="handleAudit(scope.row)">审核</el-button>
            </template>
            <template v-else>
              <el-switch
                v-model="scope.row.statusBool"
                active-text="启用"
                inactive-text="禁用"
                inline-prompt
                :loading="scope.row.statusLoading"
                @change="handleStatusChange(scope.row)"
              />
            </template>
          </template>
        </el-table-column>
      </el-table>

      <!-- 移动端卡片列表 -->
      <div class="hidden-md-and-up mobile-card-list">
        <div v-for="item in organizerList" :key="item.id" class="mobile-card">
          <div class="card-header">
            <div class="org-info">
              <el-avatar :size="40" :src="item.logo || ''" shape="square">
                {{ item.orgName?.charAt(0) }}
              </el-avatar>
              <div class="org-detail">
                <span class="org-name">{{ item.orgName }}</span>
                <el-tag :type="getStatusType(item)" size="small">{{ getStatusLabel(item) }}</el-tag>
              </div>
            </div>
          </div>
          <div class="card-body">
            <p><label>类型：</label>{{ item.orgType }}</p>
            <p><label>负责人：</label>{{ item.contactPerson }} ({{ item.contactPhone }})</p>
            <p><label>发布活动：</label>{{ item.activityCount || 0 }}</p>
            <p><label>入驻时间：</label>{{ formatTime(item.createTime) }}</p>
          </div>
          <div class="card-footer">
             <template v-if="item.verified === 0">
              <el-button type="primary" size="small" @click="handleAudit(item)">审核认证</el-button>
            </template>
            <template v-else>
               <span class="status-switch">
                 状态：
                 <el-switch
                  v-model="item.statusBool"
                  inline-prompt
                  active-text="启用"
                  inactive-text="禁用"
                  :loading="item.statusLoading"
                  @change="handleStatusChange(item)"
                />
               </span>
            </template>
          </div>
        </div>
        <el-empty v-if="!loading && organizerList.length === 0" description="暂无数据" />
      </div>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          :layout="isMobile ? 'prev, pager, next' : 'total, sizes, prev, pager, next, jumper'"
          @size-change="handleQuery"
          @current-change="handleQuery"
        />
      </div>
    </el-card>

    <!-- 审核弹窗 -->
    <el-dialog
      v-model="auditDialogVisible"
      title="组织认证审核"
      width="500px"
      append-to-body
    >
      <div class="audit-content" v-if="currentAuditOrg">
        <div class="info-item">
          <label>组织名称：</label><span>{{ currentAuditOrg.orgName }}</span>
        </div>
        <div class="info-item">
          <label>组织类型：</label><span>{{ currentAuditOrg.orgType }}</span>
        </div>
        <div class="info-item">
          <label>负责人：</label><span>{{ currentAuditOrg.contactPerson }}</span>
        </div>
        <div class="info-item">
          <label>联系电话：</label><span>{{ currentAuditOrg.contactPhone }}</span>
        </div>
        <div class="info-item">
          <label>简介：</label>
          <p>{{ currentAuditOrg.description }}</p>
        </div>
      </div>
      <el-form label-width="80px" style="margin-top: 20px">
         <el-form-item label="驳回原因" v-if="auditAction === 'reject'">
            <el-input v-model="auditReason" type="textarea" placeholder="请输入驳回原因" />
         </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="auditDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="submitAudit(2)">驳回</el-button>
          <el-button type="success" @click="submitAudit(1)">通过</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, reactive } from 'vue'
import request from '@/utils/request'
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'

const loading = ref(false)
const organizerList = ref<any[]>([])
const total = ref(0)
const queryParams = reactive({
  page: 1,
  size: 10,
  name: '',
  status: undefined as number | undefined
})

const windowWidth = ref(window.innerWidth)
const isMobile = ref(window.innerWidth < 768)

const handleResize = () => {
  windowWidth.value = window.innerWidth
  isMobile.value = windowWidth.value < 768
}

// Audit
const auditDialogVisible = ref(false)
const currentAuditOrg = ref<any>(null)
const auditReason = ref('')
const auditAction = ref('') // 'pass' or 'reject' logic handled in buttons

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/organizer/list', {
      params: queryParams
    })
    organizerList.value = res.data.records.map((item: any) => ({
      ...item,
      statusBool: item.status === 1,
      statusLoading: false
    }))
    total.value = res.data.total
  } catch (error) {
    console.error('Fetch error', error)
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
    queryParams.page = 1
  fetchData()
}

const resetQuery = () => {
  queryParams.page = 1
  queryParams.name = ''
  queryParams.status = undefined
  fetchData()
}

const formatTime = (time: string) => dayjs(time).format('YYYY-MM-DD HH:mm')

// Status Logic
const getStatusType = (row: any) => {
  if (row.verified === 0) return 'warning'
  if (row.verified === 2) return 'danger'
  if (row.status === 0) return 'danger' // Disabled but verified
  return 'success'
}

const getStatusLabel = (row: any) => {
  if (row.verified === 0) return '待审核'
  if (row.verified === 2) return '已驳回'
  if (row.status === 0) return '已禁用'
  return '正常'
}

// Actions
const handleAudit = (row: any) => {
  currentAuditOrg.value = row
  auditReason.value = ''
  auditDialogVisible.value = true
}

const submitAudit = async (status: number) => {
  // If rejecting, check reason
  // User prompt: reason param exists.
  try {
    await request.post('/admin/organizer/audit', {
      id: currentAuditOrg.value.id,
      status: status,
      reason: auditReason.value
    })
    ElMessage.success('操作成功')
    auditDialogVisible.value = false
    fetchData()
  } catch (error) {
    // error handled by interceptor
  }
}

const handleStatusChange = async (row: any) => {
  const newStatus = row.statusBool
  const actionText = newStatus ? '启用' : '禁用'
  
  try {
    await ElMessageBox.confirm(`确定${actionText}该组织者吗？`, '提示', {
        type: newStatus ? 'success' : 'warning'
    })
    
    row.statusLoading = true
    await request.put('/admin/organizer/status', {
      id: row.id,
      enabled: newStatus
    })
    ElMessage.success('已' + actionText)
    row.status = newStatus ? 1 : 0
  } catch (error) {
    if (error !== 'cancel') {
        // Create error. user cancelled.
    }
    // Revert visual change if failed or cancelled
    row.statusBool = !newStatus
  } finally {
    row.statusLoading = false
  }
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  fetchData()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.app-container {
  padding: 20px;
  background: var(--bg-page);
  min-height: calc(100vh - 84px);
}

:deep(.el-card) {
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: 12px;
}

.filter-container {
  margin-bottom: 20px;
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;

  .search-input { width: 220px; }
  .status-select { width: 150px; }
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.info-item {
  margin-bottom: 15px;
  color: var(--text-primary);
  
  label {
    font-weight: bold;
    margin-right: 10px;
    color: var(--text-secondary);
  }
}

@media only screen and (max-width: 768px) {
  .app-container {
    padding: 10px;
  }
  
  .filter-container {
     &.is-mobile {
        flex-direction: column;
        align-items: stretch;
        gap: 10px;
        
        .filter-item {
           margin-right: 0;
           width: 100% !important;
        }
        
        .action-buttons {
           display: flex;
           gap: 10px;
           
           .el-button {
             flex: 1;
             height: 36px;
             border-radius: 4px;
             span { display: inline-block !important; }
           }
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
           border-bottom: 1px solid var(--border-light);
           padding-bottom: 10px;
           margin-bottom: 10px;
           
           .org-info {
              display: flex;
              align-items: center;
              gap: 12px;
              
              .org-detail {
                 display: flex;
                 flex-direction: column;
                 gap: 4px;
                 .org-name { font-weight: bold; font-size: 15px; color: var(--text-primary); }
              }
           }
        }
        
        .card-body {
           p {
              margin: 8px 0;
              font-size: 13px;
              color: var(--text-secondary);
              label { color: var(--text-muted); width: 70px; display: inline-block; }
           }
        }
        
        .card-footer {
           border-top: 1px solid var(--border-light);
           padding-top: 12px;
           margin-top: 10px;
           display: flex;
           justify-content: flex-end;
           
           .status-switch {
              display: flex;
              align-items: center;
              gap: 8px;
              font-size: 13px;
              color: var(--text-secondary);
           }
           
           .el-button { margin-left: 0; width: 100%; }
        }
     }
  }
  
  .pagination-container {
     justify-content: center;
  }
}
</style>
