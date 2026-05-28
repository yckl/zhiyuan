<template>
  <div class="user-management">
    <el-page-header @back="router.back()">
      <template #content>
        <span class="page-title">用户管理</span>
      </template>
    </el-page-header>

    <el-card class="user-card" shadow="never">
      <template #header>
        <div class="card-header" :class="{ 'is-mobile': isMobile }">
          <span>用户列表</span>
          <div class="header-actions">
            <!-- 搜索栏 -->
            <el-input
              v-model="searchKeyword"
              placeholder="搜索用户名/姓名"
              clearable
              class="search-input"
              :prefix-icon="Search"
              @keyup.enter="handleSearch"
              @clear="handleSearch"
            />
            <el-select v-model="roleFilter" placeholder="角色" clearable class="filter-select" @change="handleSearch">
              <el-option label="志愿者" value="VOLUNTEER" />
              <el-option label="组织者" value="ORGANIZER" />
              <el-option label="管理员" value="ADMIN" />
            </el-select>
            <el-select v-model="statusFilter" placeholder="状态" clearable class="filter-select" @change="handleSearch">
              <el-option label="正常" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
            <div class="action-buttons-group">
              <el-button type="primary" :icon="Search" @click="handleSearch">
                <span>搜索</span>
              </el-button>
              <el-button :icon="Refresh" @click="handleReset">
                <span>重置</span>
              </el-button>
              <el-button type="success" :icon="Plus" @click="handleAddUser">
                <span>新增</span>
              </el-button>
            </div>
          </div>
        </div>
      </template>

      <!-- 统计卡片 -->
      <div class="stats-row" :class="{ 'is-mobile': isMobile }">
        <div class="stat-card">
          <div class="stat-icon stat-total">
            <el-icon :size="24"><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.total }}</div>
            <div class="stat-label">总用户</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon stat-volunteers">
            <el-icon :size="24"><UserFilled /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.volunteers }}</div>
            <div class="stat-label">志愿者</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon stat-organizers">
            <el-icon :size="24"><OfficeBuilding /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.organizers }}</div>
            <div class="stat-label">组织者</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon stat-disabled">
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
        class="hidden-sm-and-down custom-table"
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
              <!-- 积分调整 (仅志愿者? -->
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

      <!-- 移动端卡片列?-->
      <div class="hidden-md-and-up mobile-card-list">
        <div v-for="item in users" :key="item.id" class="mobile-card">
          <div class="card-header-mobile">
            <div class="user-info-mobile">
              <el-avatar :size="36" :src="item.avatar"><el-icon><User /></el-icon></el-avatar>
              <div class="info">
                <span class="username">{{ item.username }}</span>
                <el-tag :type="getRoleType(item.role)" size="small">{{ getRoleLabel(item.role) }}</el-tag>
              </div>
            </div>
            <el-tag :type="item.status === 1 ? 'success' : 'danger'" size="small">
              {{ item.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </div>
          
          <div class="card-body-mobile" @click="handleViewDetail(item)">
            <p v-if="item.realName"><label>姓名：</label>{{ item.realName }}</p>
            <p v-if="item.phone"><label>电话：</label>{{ item.phone }}</p>
            <p><label>注册：</label>{{ formatDate(item.createTime) }}</p>
          </div>
          
          <div class="card-footer-mobile">
            <el-button 
              v-if="item.role === 'VOLUNTEER'"
              type="warning" 
              link 
              size="small" 
              @click="handleAdjustPoints(item)"
            >
              积分
            </el-button>
            <el-button type="primary" link size="small" @click="handleViewDetail(item)">详情</el-button>
            
            <el-popconfirm
              v-if="item.status === 1"
              title="确定禁用吗？"
              @confirm="handleToggleStatus(item)"
            >
              <template #reference>
                <el-button type="warning" link size="small" :loading="item.statusLoading">禁用</el-button>
              </template>
            </el-popconfirm>
            <el-popconfirm
              v-else
              title="确定启用吗？"
              @confirm="handleToggleStatus(item)"
            >
              <template #reference>
                <el-button type="success" link size="small" :loading="item.statusLoading">启用</el-button>
              </template>
            </el-popconfirm>
             <el-popconfirm
                title="确定删除吗？"
                @confirm="handleDeleteUser(item)"
              >
                <template #reference>
                  <el-button type="danger" link size="small">删除</el-button>
                </template>
              </el-popconfirm>
          </div>
        </div>
      </div>

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
          :layout="isMobile ? 'prev, pager, next' : 'total, sizes, prev, pager, next, jumper'"
          :pager-count="isMobile ? 5 : 7"
          :small="isMobile"
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
                <el-input v-model="addForm.className" placeholder="例如：软件 401" />
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="addVisible = false">取消</el-button>
                <el-button type="primary" :loading="addLoading" :disabled="addLoading" @click="submitAddUser">提交</el-button>
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
              <el-input-number v-model="pointsForm.points" :step="1" placeholder="正数增加，负数扣减" />
              <div class="form-tip">输入正数增加积分，输入负数扣除积分</div>
            </el-form-item>
            <el-form-item label="调整原因" prop="reason">
                <el-input v-model="pointsForm.reason" type="textarea" :rows="3" placeholder="请输入调整原因（必填）" />
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="pointsVisible = false">取消</el-button>
                <el-button type="primary" :loading="pointsLoading" :disabled="pointsLoading" @click="submitAdjustPoints">确认调整</el-button>
            </span>
        </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
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

const windowWidth = ref(window.innerWidth)
const isMobile = ref(window.innerWidth < 768)

const handleResize = () => {
  windowWidth.value = window.innerWidth
  isMobile.value = windowWidth.value < 768
}

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
    await request.put(`/user/${row.id}/status`, {}, {
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

const validatePass = (_rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else {
    if (addForm.confirmPassword !== '') {
      if (!addFormRef.value) return
      addFormRef.value.validateField('confirmPassword', () => {})
    }
    callback()
  }
}
const validatePass2 = (_rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== addForm.password) {
    callback(new Error('两次输入密码不一致'))
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
            } catch (error: any) {
                const msg = error.response?.data?.message || '新增志愿者失败，请稍后重试'
                ElMessage.error(msg)
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
    } catch (error: any) {
        const msg = error.response?.data?.message || '删除失败，请稍后重试'
        ElMessage.error(msg)
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
            } catch (error: any) {
                const msg = error.response?.data?.message || '积分调整失败，请稍后重试'
                ElMessage.error(msg)
                console.error(error)
            } finally {
                pointsLoading.value = false
            }
        }
    })
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  fetchUsers()
  fetchStats()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style lang="scss" scoped>
.user-management {
  background: var(--bg-page);
  min-height: calc(100vh - 84px);
  padding: 20px;

  .page-title {
    font-size: 18px;
    font-weight: 600;
    color: var(--text-primary);
  }

  .user-card {
    margin-top: 20px;
    border-radius: 12px;
    background: var(--bg-card);
    border: 1px solid var(--border-light);

    :deep(.el-card__header) {
      border-bottom: 1px solid var(--border-light);
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: 12px;
      color: var(--text-primary);

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
        background: var(--bg-card-hover);
        border: 1px solid var(--border-light);
        border-radius: 10px;
        transition: all 0.3s;

        &:hover {
          background: var(--bg-page);
          transform: translateY(-2px);
          box-shadow: var(--shadow-light);
        }

        .stat-icon {
          width: 50px;
          height: 50px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: white;
          
          &.stat-total { background: #409eff; }
          &.stat-volunteers { background: #67c23a; }
          &.stat-organizers { background: #e6a23c; }
          &.stat-disabled { background: #f56c6c; }
        }

        .stat-info {
          .stat-value {
            font-size: 24px;
            font-weight: 700;
            color: var(--text-primary);
          }

          .stat-label {
            font-size: 13px;
            color: var(--text-secondary);
          }
        }
      }
    }

    .custom-table {
      :deep(.el-table__header-wrapper th) {
        background: var(--bg-page) !important;
        color: var(--text-primary);
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
          color: var(--text-primary);
        }

        .realname {
          font-size: 12px;
          color: var(--text-muted);
        }
      }
    }

    .time-cell {
      display: flex;
      align-items: center;
      gap: 6px;
      color: var(--text-secondary);
      font-size: 13px;
      
      .el-icon {
        color: var(--el-color-primary);
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
      margin-top: 20px;
      padding-top: 20px;
      border-top: 1px solid var(--border-light);
      display: flex;
      justify-content: flex-end;
      
      @media (max-width: 768px) {
        justify-content: center;
        overflow: hidden; // Strictly prevent scroll
        width: 100%;
        
        :deep(.el-pagination) {
          flex-wrap: wrap;
          justify-content: center;
          width: 100%;
          overflow: hidden;

          .el-pager li {
            min-width: 24px;
          }
          
          button {
            min-width: 24px;
          }
        }
      }
    }
  }
}

.form-tip {
    font-size: 12px;
    color: var(--text-muted);
    line-height: 1.5;
    margin-top: 4px;
}

// Global mobile tweaks for this page
@media (max-width: 768px) {
  .user-management {
    overflow-x: hidden; // Prevent horizontal scroll on page
    padding: 10px;
  }
  
  .user-card {
    overflow: hidden; // Ensure card doesn't overflow
    :deep(.el-card__body) {
      padding: 10px !important;
      overflow: hidden;
    }
  }
  .user-card {
      .card-header.is-mobile {
        flex-direction: column;
        align-items: stretch;
        gap: 12px;
        
        .header-actions {
           width: 100%;
           flex-direction: column;
           gap: 10px;
           
           .search-input { width: 100% !important; }
           .filter-select { width: 100% !important; }
           
           .action-buttons-group {
              display: flex;
              width: 100%;
              gap: 8px;
              
              .el-button {
                flex: 1;
                margin-left: 0 !important;
                height: 36px;
                border-radius: 4px; // Force rectangular
                
                span { display: inline-block !important; }
              }
           }
        }
      }
      
      .stats-row.is-mobile {
         flex-wrap: wrap;
         gap: 10px;
         .stat-card {
            flex: 1 1 calc(50% - 10px); 
            min-width: 0;
            padding: 12px;
            .stat-icon { width: 40px; height: 40px; }
            .stat-info .stat-value { font-size: 18px; }
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
          
          .card-header-mobile {
             display: flex;
             justify-content: space-between;
             align-items: center;
             border-bottom: 1px solid var(--border-light);
             padding-bottom: 8px;
             margin-bottom: 8px;
             
             .user-info-mobile {
                display: flex;
                align-items: center;
                gap: 8px;
                .info {
                   display: flex;
                   flex-direction: column;
                   .username { font-weight: bold; font-size: 14px; color: var(--text-primary); }
                }
             }
          }

          .card-body-mobile {
             padding-bottom: 8px;
             p {
                margin: 4px 0;
                font-size: 13px;
                color: var(--text-secondary);
                display: flex;
                label { color: var(--text-muted); width: 60px; }
             }
          }
          
          .card-footer-mobile {
             border-top: 1px solid var(--border-light);
             padding-top: 8px;
             display: flex;
             justify-content: flex-end;
             gap: 8px;
             flex-wrap: wrap;
          }
        }
      }
    }
    
    :deep(.el-pagination) {
       justify-content: center;
       .el-pagination__sizes { display: none; }
       .el-pagination__jump { display: none; }
    }
  }

</style>
