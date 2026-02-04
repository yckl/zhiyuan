<template>
  <div class="org-activity-list">
    <el-card class="table-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon><List /></el-icon>
            <span class="title">活动管理</span>
            <el-tag type="info" class="ml-10">共 {{ total }} 个活动</el-tag>
          </div>
          <el-button type="primary" @click="router.push('/organizer/activity/create')">
            <el-icon><Plus /></el-icon> 发布活动
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar mb-20">
        <el-input
          v-model="queryParams.title"
          placeholder="搜索活动名称"
          style="width: 240px"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="queryParams.status" placeholder="活动状态" clearable style="width: 140px" @change="handleSearch">
          <el-option v-for="(val, key) in STATUS_MAP" :key="key" :label="val.label" :value="key" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </div>

      <el-table 
        v-loading="loading"
        :data="activities" 
        stripe 
        style="width: 100%"
        class="custom-table"
      >
        <el-table-column label="封面" width="120">
          <template #default="{ row }">
            <el-image 
              :src="row.coverImage || '/default-cover.jpg'" 
              fit="cover" 
              class="cover-thumb"
              :preview-src-list="row.coverImage ? [row.coverImage] : []"
              preview-teleported
            >
              <template #error>
                <div class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>

        <el-table-column prop="title" label="活动名称" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="activity-title" @click="router.push(`/activity/${row.id}`)">{{ row.title }}</div>
            <div class="activity-meta">ID: {{ row.id }} | {{ row.categoryName }}</div>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="STATUS_MAP[row.status]?.type || 'info'" effect="light">
              {{ STATUS_MAP[row.status]?.label || '未知' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="报名进度" width="160">
          <template #default="{ row }">
            <div class="progress-info">
              <span>{{ row.currentParticipants || 0 }} / {{ row.maxParticipants || '∞' }}</span>
              <el-progress 
                :percentage="getQuotaPercentage(row)" 
                :show-text="false"
                :status="row.currentParticipants >= row.maxParticipants && row.maxParticipants > 0 ? 'success' : ''"
              />
            </div>
          </template>
        </el-table-column>

        <el-table-column label="开展时间" width="180">
          <template #default="{ row }">
            <div class="time-cell">
              <el-icon><Calendar /></el-icon>
              <span>{{ formatDate(row.startTime) }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row.id)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button link type="success" size="small" @click="handleAudit(row.id)">
              <el-icon><User /></el-icon> 审核
            </el-button>
            <el-popconfirm title="确定下架该活动吗？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button link type="danger" size="small">
                  <el-icon><Delete /></el-icon> 下架
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container mt-20">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="fetchActivities"
          @current-change="fetchActivities"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, List, Search, Picture, Calendar, Edit, User, Delete } from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const router = useRouter()
const loading = ref(false)
const activities = ref([])
const total = ref(0)

const STATUS_MAP = {
  0: { label: '草稿', type: 'info' },
  1: { label: '待审核', type: 'warning' },
  2: { label: '已发布', type: 'success' },
  3: { label: '进行中', type: 'primary' },
  4: { label: '已结束', type: 'info' },
  5: { label: '已取消', type: 'danger' }
}

const queryParams = reactive({
  page: 1,
  size: 10,
  title: '',
  status: null as number | null
})

const fetchActivities = async () => {
  loading.value = true
  try {
    // 之前是用 /activity/my，现在为了统一，其实可以复用原来的接口或者新建
    // 考虑到重构，我们直接调用之前的 /activity/my 接口，但带上分页参数
    const res = await request.get('/activity/my', queryParams)
    activities.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('获取活动列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.page = 1
  fetchActivities()
}

const resetQuery = () => {
  queryParams.title = ''
  queryParams.status = null
  handleSearch()
}

const handleEdit = (id: number) => {
  router.push(`/organizer/activity/edit/${id}`)
}

const handleAudit = (id: number) => {
  // 目前路由对应的是 /organizer/personnel/audit，但这里是指具体活动的报名审核
  // 我们可以跳转到一个通用的审核页面，并带上活动ID
  router.push(`/organizer/personnel/audit?activityId=${id}`)
}

const handleDelete = async (id: number) => {
  try {
    await request.delete(`/activity/${id}`)
    ElMessage.success('活动已下架')
    fetchActivities()
  } catch (error) {
    console.error('下架失败:', error)
  }
}

const getQuotaPercentage = (row: any) => {
  if (!row.maxParticipants || row.maxParticipants <= 0) return 0
  return Math.min(Math.round(((row.currentParticipants || 0) / row.maxParticipants) * 100), 100)
}

const formatDate = (date: string) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-'
}

onMounted(() => {
  fetchActivities()
})
</script>

<style scoped lang="scss">
.org-activity-list {
  padding: 10px;

  .table-card {
    border-radius: 12px;
    border: none;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-left {
      display: flex;
      align-items: center;
      gap: 8px;
      .title {
        font-weight: bold;
        font-size: 16px;
      }
    }
  }

  .search-bar {
    display: flex;
    gap: 12px;
  }

  .cover-thumb {
    width: 100px;
    height: 60px;
    border-radius: 8px;
    cursor: pointer;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  }

  .image-placeholder {
    width: 100%;
    height: 100%;
    background: var(--el-fill-color-light);
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--el-text-color-placeholder);
    font-size: 24px;
  }

  .activity-title {
    font-weight: 600;
    color: var(--el-color-primary);
    cursor: pointer;
    margin-bottom: 4px;
    &:hover {
      text-decoration: underline;
    }
  }

  .activity-meta {
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }

  .progress-info {
    display: flex;
    flex-direction: column;
    gap: 4px;
    span {
      font-size: 12px;
      color: var(--el-text-color-regular);
    }
  }

  .time-cell {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: var(--el-text-color-regular);
    .el-icon {
      color: var(--el-color-primary);
    }
  }

  .pagination-container {
    display: flex;
    justify-content: center;
  }
}
</style>
