<template>
  <div class="user-management">
    <el-page-header @back="router.back()">
      <template #content>
        <span class="page-title">用户管理</span>
      </template>
    </el-page-header>

    <el-card class="user-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>用户列表</span>
          <div class="header-actions">
            <!-- 搜索栏 -->
            <el-input
              v-model="searchKeyword"
              placeholder="搜索用户名/姓名"
              clearable
              style="width: 200px"
              :prefix-icon="Search"
              @keyup.enter="handleSearch"
              @clear="handleSearch"
            />
            <el-select v-model="roleFilter" placeholder="全部角色" clearable style="width: 120px" @change="handleSearch">
              <el-option label="志愿者" value="VOLUNTEER" />
              <el-option label="组织者" value="ORGANIZER" />
              <el-option label="管理员" value="ADMIN" />
            </el-select>
            <el-select v-model="statusFilter" placeholder="全部状态" clearable style="width: 120px" @change="handleSearch">
              <el-option label="正常" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
            <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
            
            <!-- 新增按钮 -->
            <el-button type="success" :icon="Plus" @click="handleAddUser">新增志愿者</el-button>
          </div>
        </div>
      </template>

      <!-- 统计卡片 -->
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-icon" style="background: #409eff;">
            <el-icon :size="24"><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.total }}</div>
            <div class="stat-label">总用户数</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background: #67c23a;">
            <el-icon :size="24"><UserFilled /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.volunteers }}</div>
            <div class="stat-label">志愿者</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background: #e6a23c;">
            <el-icon :size="24"><OfficeBuilding /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.organizers }}</div>
            <div class="stat-label">组织者</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background: #f56c6c;">
            <el-icon :size="24"><Lock /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.disabled }}</div>
            <div class="stat-label">已禁用</div>
          </div>
        </div>
      </div>

      <el-table
        :data="users"
        v-loading="loading"
        stripe
        border
        style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
      >
        <!-- 用户信息 -->
        <el-table-column label="用户" width="200">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :size="40" :src="row.avatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <div class="user-detail">
                <span class="username">{{ row.username }}</span>
                <span class="realname">{{ row.realName || '-' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <!-- 角色 -->
        <el-table-column label="角色" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)">
              {{ getRoleLabel(row.role) }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 联系方式 -->
        <el-table-column prop="phone" label="手机号" width="130">
          <template #default="{ row }">
            {{ row.phone || '-' }}
          </template>
        </el-table-column>

        <!-- 状态 -->
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="light">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 注册时间 -->
        <el-table-column label="注册时间" width="180">
          <template #default="{ row }">
            <div class="time-cell">
              <el-icon><Calendar /></el-icon>
              <span>{{ formatDate(row.createTime) }}</span>
            </div>
          </template>
        </el-table-column>

        <!-- 操作 -->
        <el-table-column label="操作" min-width="240" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-buttons">
              <!-- 积分调整 (仅志愿者) -->
              <el-button 
                v-if="row.role === 'VOLUNTEER'"
                type="warning" 
                link 
                size="small" 
                @click="handleAdjustPoints(row)"
              >
                <el-icon><Coin /></el-icon> 积分
              </el-button>

              <el-button type="primary" link size="small" @click="handleViewDetail(row)">
                <el-icon><View /></el-icon> 详情
              </el-button>

              <el-popconfirm
                v-if="row.status === 1"
                title="确定要禁用该用户吗？"
                confirm-button-text="确定"
                cancel-button-text="取消"
                confirm-button-type="warning"
                @confirm="handleToggleStatus(row)"
              >
                <template #reference>
                  <el-button type="warning" link size="small" :loading="row.statusLoading">
                    <el-icon><Lock /></el-icon> 禁用
                  </el-button>
                </template>
              </el-popconfirm>

              <el-popconfirm
                v-else
                title="确定要启用该用户吗？"
                confirm-button-text="确定"
                cancel-button-text="取消"
                confirm-button-type="success"
                @confirm="handleToggleStatus(row)"
              >
                <template #reference>
                  <el-button type="success" link size="small" :loading="row.statusLoading">
                    <el-icon><Unlock /></el-icon> 启用
                  </el-button>
                </template>
              </el-popconfirm>

              <el-popconfirm
                title="确定要永久删除该用户吗？此操作不可恢复！"
                confirm-button-text="删除"
                cancel-button-text="取消"
                confirm-button-type="danger"
                @confirm="handleDeleteUser(row)"
              >
                <template #reference>
                  <el-button type="danger" link size="small">
                    <el-icon><Delete /></el-icon> 删除
                  </el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-empty
        v-if="!loading && users.length === 0"
        description="暂无用户数据"
        :image-size="120"
      />

      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="fetchUsers"
          @current-change="fetchUsers"
        />
      </div>
    </el-card>

    <!-- 用户详情弹窗 -->
    <el-dialog v-model="detailVisible" title="用户详情" width="500px">
      <el-descriptions :column="1" border v-if="currentUser">
        <el-descriptions-item label="用户名">{{ currentUser.username }}</el-descriptions-item>
        <el-descriptions-item label="真实姓名">{{ currentUser.realName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag :type="getRoleType(currentUser.role)">{{ getRoleLabel(currentUser.role) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentUser.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentUser.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentUser.status === 1 ? 'success' : 'danger'">
            {{ currentUser.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="最后登录">{{ formatDate(currentUser.lastLoginTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ formatDate(currentUser.createTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 新增志愿者弹窗 -->
    <el-dialog v-model="addVisible" title="新增志愿者" width="500px" @close="resetAddForm">
        <el-form ref="addFormRef" :model="addForm" :rules="addRules" label-width="100px">
            <el-form-item label="用户名" prop="username">
                <el-input v-model="addForm.username" placeholder="请输入用户名（学号）" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
                <el-input v-model="addForm.password" type="password" placeholder="请输入密码" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="addForm.confirmPassword" type="password" placeholder="再度输入密码" show-password />
            </el-form-item>
            <el-form-item label="真实姓名" prop="name">
                <el-input v-model="addForm.name" placeholder="请输入真实姓名" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
                <el-input v-model="addForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="专业班级" prop="className">
                <el-input v-model="addForm.className" placeholder="例如：软件2401" />
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="addVisible = false">取消</el-button>
                <el-button type="primary" :loading="addLoading" @click="submitAddUser">提交</el-button>
            </span>
        </template>
    </el-dialog>

    <!-- 积分调整弹窗 -->
    <el-dialog v-model="pointsVisible" title="积分调整" width="450px" @close="resetPointsForm">
        <el-form ref="pointsFormRef" :model="pointsForm" :rules="pointsRules" label-width="80px">
            <el-form-item label="当前对象">
                <span style="font-weight: bold">{{ currentVolunteer?.realName }} ({{ currentVolunteer?.username }})</span>
            </el-form-item>
            <el-form-item label="变动分值" prop="points">
              <el-input-number v-model="pointsForm.points" :step="1" placeholder="正数增加，负数扣除" />
              <div class="form-tip">输入正数增加积分，输入负数扣除积分</div>
            </el-form-item>
            <el-form-item label="调整原因" prop="reason">
                <el-input v-model="pointsForm.reason" type="textarea" :rows="3" placeholder="请输入调整原因（必填）" />
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="pointsVisible = false">取消</el-button>
                <el-button type="primary" :loading="pointsLoading" @click="submitAdjustPoints">确认调整</el-button>
            </span>
        </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, FormInstance } from 'element-plus'
import {
  Search, Refresh, User, UserFilled, OfficeBuilding, Lock,
  Unlock, Calendar, View, Coin, Plus, Delete
} from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import dayjs from 'dayjs'

const router = useRouter()

const loading = ref(false)
const users = ref<any[]>([])
const total = ref(0)
const searchKeyword = ref('')
const roleFilter = ref<string | null>(null)
const statusFilter = ref<number | null>(null)
const detailVisible = ref(false)
const currentUser = ref<any>(null)

const queryParams = reactive({
  page: 1,
  size: 10
})

// 统计数据
const stats = reactive({
  total: 0,
  volunteers: 0,
  organizers: 0,
  disabled: 0
})

// 角色映射
const ROLE_MAP: Record<string, { label: string; type: string }> = {
  'VOLUNTEER': { label: '志愿者', type: 'success' },
  'ORGANIZER': { label: '组织者', type: 'warning' },
  'ADMIN': { label: '管理员', type: 'danger' }
}

const getRoleLabel = (role: string) => ROLE_MAP[role]?.label || role
const getRoleType = (role: string) => ROLE_MAP[role]?.type || 'info'

const formatDate = (date: string) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-'
}

const handleSearch = () => {
  queryParams.page = 1
  fetchUsers()
}

const handleReset = () => {
  searchKeyword.value = ''
  roleFilter.value = null
  statusFilter.value = null
  queryParams.page = 1
  fetchUsers()
}

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true
  try {
    const params: any = {
      page: queryParams.page,
      size: queryParams.size
    }
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    if (roleFilter.value) {
      params.role = roleFilter.value
    }
    if (statusFilter.value !== null) {
      params.status = statusFilter.value
    }

    const res = await request.get('/user/list', params)
    
    if (res.data) {
      users.value = res.data.records || []
      total.value = res.data.total || 0
    } else {
      users.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    users.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 获取用户统计
const fetchStats = async () => {
  try {
    const res = await request.get('/user/stats')
    if (res.data) {
      stats.total = res.data.total || 0
      stats.volunteers = res.data.volunteers || 0
      stats.organizers = res.data.organizers || 0
      stats.disabled = res.data.disabled || 0
    }
  } catch (error) {
    console.error('获取用户统计失败:', error)
  }
}

const handleViewDetail = (row: any) => {
  currentUser.value = row
  detailVisible.value = true
}

// 切换用户状态
const handleToggleStatus = async (row: any) => {
  const newStatus = row.status === 1 ? 0 : 1
  const statusName = newStatus === 1 ? '启用' : '禁用'
  
  row.statusLoading = true
  
  try {
    await request.put(`/user/${row.id}/status`, null, {
      params: { status: newStatus }
    })
    
    row.status = newStatus
    ElMessage.success(`用户已${statusName}`)
    fetchStats()
  } catch (error) {
    console.error('操作失败:', error)
  } finally {
    row.statusLoading = false
  }
}

// ================= 新增志愿者逻辑 =================
const addVisible = ref(false)
const addLoading = ref(false)
const addFormRef = ref<FormInstance>()
const addForm = reactive({
    username: '',
    password: '',
    confirmPassword: '',
    name: '',
    phone: '',
    className: ''
})

const validatePass = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else {
    if (addForm.confirmPassword !== '') {
      if (!addFormRef.value) return
      addFormRef.value.validateField('confirmPassword', () => null)
    }
    callback()
  }
}
const validatePass2 = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== addForm.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const addRules = reactive({
    username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
    password: [{ validator: validatePass, trigger: 'blur' }],
    confirmPassword: [{ validator: validatePass2, trigger: 'blur' }],
    name: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }]
})

const handleAddUser = () => {
    addVisible.value = true
}

const resetAddForm = () => {
    addFormRef.value?.resetFields()
    addForm.username = ''
    addForm.password = ''
    addForm.confirmPassword = ''
    addForm.name = ''
    addForm.phone = ''
    addForm.className = ''
}

const submitAddUser = async () => {
    if (!addFormRef.value) return
    await addFormRef.value.validate(async (valid) => {
        if (valid) {
            addLoading.value = true
            try {
                await request.post('/admin/volunteer/add', addForm)
                ElMessage.success('新增志愿者成功')
                addVisible.value = false
                fetchUsers()
                fetchStats()
            } catch (error) {
                console.error(error)
            } finally {
                addLoading.value = false
            }
        }
    })
}

// ================= 删除用户逻辑 =================
const handleDeleteUser = async (row: any) => {
    try {
        await request.delete(`/admin/volunteer/delete/${row.id}`)
        ElMessage.success('删除成功')
        fetchUsers()
        fetchStats()
    } catch (error) {
        console.error(error)
    }
}

// ================= 积分调整逻辑 =================
const pointsVisible = ref(false)
const pointsLoading = ref(false)
const currentVolunteer = ref<any>(null)
const pointsFormRef = ref<FormInstance>()
const pointsForm = reactive({
    userId: 0,
    points: 0,
    reason: ''
})

const pointsRules = reactive({
    points: [{ required: true, message: '请输入分值', trigger: 'blur' }],
    reason: [{ required: true, message: '请输入调整原因', trigger: 'blur' }]
})

const handleAdjustPoints = (row: any) => {
    currentVolunteer.value = row
    pointsForm.userId = row.id
    pointsVisible.value = true
}

const resetPointsForm = () => {
    pointsFormRef.value?.resetFields()
    pointsForm.points = 0
    pointsForm.reason = ''
}

const submitAdjustPoints = async () => {
    if (!pointsFormRef.value) return
    await pointsFormRef.value.validate(async (valid) => {
        if (valid) {
            if (pointsForm.points === 0) {
                ElMessage.warning('积分变动不能为0')
                return
            }
            pointsLoading.value = true
            try {
                await request.post('/admin/volunteer/points/adjust', pointsForm)
                ElMessage.success('积分调整成功')
                pointsVisible.value = false
                // 不需要刷新整个列表，或者刷新积分（列表里没有积分字段，所以这里纯通知）
            } catch (error) {
                console.error(error)
            } finally {
                pointsLoading.value = false
            }
        }
    })
}

onMounted(() => {
  fetchUsers()
  fetchStats()
})
</script>

<style lang="scss" scoped>
.user-management {
  .page-title {
    font-size: 18px;
    font-weight: 600;
    color: #333;
  }

  .user-card {
    margin-top: 20px;
    border-radius: 12px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: 12px;

      .header-actions {
        display: flex;
        align-items: center;
        gap: 8px;
        flex-wrap: wrap;
      }
    }

    .stats-row {
      display: flex;
      gap: 20px;
      margin-bottom: 24px;
      flex-wrap: wrap;

      .stat-card {
        flex: 1;
        min-width: 150px;
        display: flex;
        align-items: center;
        gap: 16px;
        padding: 16px;
        background: #f9f9f9;
        border-radius: 10px;
        transition: all 0.3s;

        &:hover {
          background: #f0f0f0;
          transform: translateY(-2px);
        }

        .stat-icon {
          width: 50px;
          height: 50px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: white;
        }

        .stat-info {
          .stat-value {
            font-size: 24px;
            font-weight: 700;
            color: #333;
          }

          .stat-label {
            font-size: 13px;
            color: #999;
          }
        }
      }
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 10px;

      .user-detail {
        display: flex;
        flex-direction: column;

        .username {
          font-weight: 500;
          color: #333;
        }

        .realname {
          font-size: 12px;
          color: #999;
        }
      }
    }

    .time-cell {
      display: flex;
      align-items: center;
      gap: 6px;
      color: #666;
      font-size: 13px;
      
      .el-icon {
        color: #409eff;
      }
    }

    .action-buttons {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      flex-wrap: wrap;
    }

    .pagination-wrapper {
      display: flex;
      justify-content: center;
      margin-top: 20px;
      padding-top: 20px;
      border-top: 1px solid #f0f0f0;
    }
  }
}
.form-tip {
    font-size: 12px;
    color: #909399;
    line-height: 1.5;
    margin-top: 4px;
}
</style>
