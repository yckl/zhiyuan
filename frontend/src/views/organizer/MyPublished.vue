<template>
  <div class="my-published">
    <el-page-header @back="router.back()">
      <template #content>
        <span class="page-title">我发布的活动</span>
      </template>
      <template #extra>
        <el-button type="primary" @click="router.push('/organizer/activity/create')">
          <el-icon><Plus /></el-icon> 发布新活动
        </el-button>
      </template>
    </el-page-header>

    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>活动列表</span>
          <el-tag type="info">共 {{ total }} 个活动</el-tag>
        </div>
      </template>

      <el-table 
        :data="activities" 
        v-loading="loading" 
        stripe 
        border
        style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
      >
        <!-- 封面图 -->
        <el-table-column label="封面" width="100" align="center">
          <template #default="{ row }">
            <el-image 
              :src="row.coverImage || defaultCover" 
              fit="cover" 
              class="cover-thumb"
              :preview-src-list="row.coverImage ? [row.coverImage] : []"
            >
              <template #error>
                <div class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>

        <!-- 标题 -->
        <el-table-column prop="title" label="活动标题" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <el-link type="primary" @click="router.push(`/activity/${row.id}`)">
              {{ row.title }}
            </el-link>
          </template>
        </el-table-column>

        <!-- 状态 -->
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="light">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 报名人数 -->
        <el-table-column label="报名情况" width="140" align="center">
          <template #default="{ row }">
            <div class="quota-info">
              <span class="current">{{ row.currentParticipants || 0 }}</span>
              <span class="separator">/</span>
              <span class="max">{{ row.maxParticipants || '不限' }}</span>
            </div>
            <el-progress 
              :percentage="getQuotaPercentage(row)" 
              :stroke-width="4"
              :show-text="false"
              :color="getProgressColor(row)"
            />
          </template>
        </el-table-column>

        <!-- 活动时间 -->
        <el-table-column label="活动时间" width="180">
          <template #default="{ row }">
            <div class="time-cell">
              <el-icon><Clock /></el-icon>
              <span>{{ formatDate(row.startTime) }}</span>
            </div>
          </template>
        </el-table-column>

        <!-- 操作 -->
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button 
                type="primary" 
                link 
                size="small"
                @click="handleEdit(row.id)"
              >
                <el-icon><Edit /></el-icon> 编辑
              </el-button>
              
              <el-button 
                type="success" 
                link 
                size="small"
                @click="handleAudit(row.id)"
              >
                <el-icon><User /></el-icon> 人员管理
              </el-button>
              
              <el-popconfirm
                title="确定要下架此活动吗？"
                confirm-button-text="确定"
                cancel-button-text="取消"
                confirm-button-type="danger"
                @confirm="handleDelete(row.id)"
              >
                <template #reference>
                  <el-button type="danger" link size="small">
                    <el-icon><Delete /></el-icon> 下架
                  </el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && activities.length === 0" description="暂无发布的活动" :image-size="120">
        <el-button type="primary" @click="router.push('/organizer/activity/create')">
          发布第一个活动
        </el-button>
      </el-empty>

      <div class="pagination-wrapper" v-if="total > 0">
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
import { ElMessage } from 'element-plus'
import { Plus, Picture, Clock, Edit, User, Delete } from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import dayjs from 'dayjs'

const router = useRouter()

const loading = ref(false)
const activities = ref<any[]>([])
const total = ref(0)
const defaultCover = '/default-cover.jpg'

const queryParams = reactive({
  page: 1,
  size: 10
})

// 状态字典
const STATUS_MAP: Record<number, { label: string; type: string }> = {
  0: { label: '草稿', type: 'info' },
  1: { label: '待审核', type: 'warning' },
  2: { label: '已发布', type: 'success' },
  3: { label: '进行中', type: 'primary' },
  4: { label: '已结束', type: 'info' },
  5: { label: '已取消', type: 'danger' }
}

const getStatusLabel = (status: number) => {
  return STATUS_MAP[status]?.label || '未知'
}

const getStatusType = (status: number) => {
  return STATUS_MAP[status]?.type || 'info'
}

const formatDate = (date: string) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-'
}

const getQuotaPercentage = (row: any) => {
  if (!row.maxParticipants || row.maxParticipants <= 0) return 0
  return Math.min(Math.round((row.currentParticipants / row.maxParticipants) * 100), 100)
}

const getProgressColor = (row: any) => {
  const percentage = getQuotaPercentage(row)
  if (percentage >= 90) return '#f56c6c'
  if (percentage >= 70) return '#e6a23c'
  return '#409eff'
}

const fetchActivities = async () => {
  loading.value = true
  try {
    const res = await request.get('/activity/my', queryParams)
    activities.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('获取活动列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleEdit = (id: number) => {
  router.push(`/organizer/activity/edit/${id}`)
}

const handleAudit = (id: number) => {
  router.push(`/organizer/audit/${id}`)
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

onMounted(() => {
  fetchActivities()
})
</script>

<style lang="scss" scoped>
.my-published {
  .page-title {
    font-size: 18px;
    font-weight: 600;
    color: #333;
  }

  .table-card {
    margin-top: 20px;
    border-radius: 12px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .cover-thumb {
      width: 60px;
      height: 40px;
      border-radius: 4px;
    }

    .image-placeholder {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f5f7fa;
      color: #c0c4cc;
    }

    .quota-info {
      margin-bottom: 4px;
      font-size: 13px;

      .current {
        color: #409eff;
        font-weight: 600;
      }

      .separator {
        margin: 0 4px;
        color: #999;
      }

      .max {
        color: #666;
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
      gap: 4px;
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
</style>
