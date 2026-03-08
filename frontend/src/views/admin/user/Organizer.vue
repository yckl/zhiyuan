<template>
  <div class="app-container">
    <el-card>
      <!-- 搜索与筛选 -->
      <div class="filter-container">
        <el-input
          v-model="queryParams.name"
          placeholder="搜索组织名称"
          style="width: 200px"
          class="filter-item"
          @keyup.enter="handleQuery"
          clearable
          @clear="handleQuery"
        />
        <el-select
          v-model="queryParams.status"
          placeholder="状态筛选"
          clearable
          style="width: 150px"
          class="filter-item"
          @change="handleQuery"
        >
          <el-option label="待审" :value="0" />
          <el-option label="正常" :value="1" />
          <el-option label="已禁用" :value="2" />
        </el-select>
        <el-button type="primary" class="filter-item" @click="handleQuery">搜索</el-button>
        <el-button class="filter-item" @click="resetQuery">重置</el-button>
      </div>

      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="organizerList"
        style="width: 100%"
        border
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

      <!-- 分页 -->
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
        <!-- 证明文件 (Hypothetical, assuming logical requirement even if not in table view explicitly) -->
        <!-- User prompt said "Show details (including proof file)". But Organizer entity doesn't strictly have 'proofFile'. -->
        <!-- Assuming logo or description is enough or maybe 'address'. -->
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
import { ref, onMounted, reactive } from 'vue'
import request from '@/utils/request'
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const organizerList = ref<any[]>([])
const total = ref(0)
const queryParams = reactive({
  page: 1,
  size: 10,
  name: '',
  status: undefined as number | undefined
})

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
  // If rejecting, check reason?
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
  
  // Revert UI first to wait for confirmation (Switch toggles immediately by default, we can prevent or just confirm)
  // Element Plus switch `before-change` prop is good but here we use simple flow.
  // Actually, v-model changed it.
  
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
  fetchData()
})
</script>

<style scoped lang="scss">
.app-container {
  padding: 20px;
}

.filter-container {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.info-item {
  margin-bottom: 10px;
  label {
    font-weight: bold;
    margin-right: 10px;
  }
}
</style>
