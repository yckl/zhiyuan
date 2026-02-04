<template>
  <div class="activity-list">
    <!-- 搜索筛选栏 -->
    <el-card class="filter-card" shadow="never">
      <div class="filter-row">
        <!-- 搜索框 -->
        <el-input
          v-model="queryParams.title"
          placeholder="搜索活动标题"
          clearable
          style="width: 220px"
          :prefix-icon="Search"
          @input="handleInputSearch"
          @clear="handleSearch"
        />

        <!-- 分类下拉 -->
        <el-select
          v-model="queryParams.categoryId"
          placeholder="全部分类"
          clearable
          style="width: 150px"
          @change="handleSearch"
        >
          <el-option
            v-for="item in categories"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>

        <!-- 主办方下拉 -->
        <el-select
          v-model="queryParams.organizerId"
          placeholder="全部主办方"
          clearable
          filterable
          style="width: 180px"
          @change="handleSearch"
        >
          <el-option
            v-for="item in organizers"
            :key="item.id"
            :label="item.orgName"
            :value="item.id"
          >
            <div class="organizer-option">
              <span>{{ item.orgName }}</span>
              <el-tag size="small" type="info" v-if="item.orgType">{{ item.orgType }}</el-tag>
            </div>
          </el-option>
        </el-select>

        <!-- 状态下拉 -->
        <el-select
          v-model="queryParams.status"
          placeholder="全部状态"
          clearable
          style="width: 120px"
          @change="handleSearch"
        >
          <el-option label="报名中" :value="2" />
          <el-option label="进行中" :value="3" />
          <el-option label="已结束" :value="4" />
        </el-select>

        <!-- 重置按钮 -->
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      </div>
    </el-card>

    <!-- 活动列表 -->
    <div class="activity-grid" v-loading="loading">
      <el-row :gutter="20">
        <el-col v-for="item in activities" :key="item.id" :xs="24" :sm="12" :lg="8" :xl="6">
          <el-card class="activity-card" shadow="hover" @click="goToDetail(item.id)">
            <div class="card-cover">
              <el-image :src="item.coverImage || defaultCover" fit="cover" class="cover-image">
                <template #error>
                  <div class="image-placeholder">
                    <el-icon :size="40"><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <el-tag class="status-tag" :type="getStatusType(item.status)">{{ item.statusName }}</el-tag>
            </div>
            
            <div class="card-body">
              <div class="card-category">
                <el-tag size="small" effect="plain">{{ item.categoryName || '未分类' }}</el-tag>
              </div>
              <h3 class="card-title">{{ item.title }}</h3>
              
              <div class="card-info">
                <p><el-icon><Clock /></el-icon> {{ formatDate(item.startTime) }}</p>
                <p><el-icon><Location /></el-icon> {{ item.location || '待定' }}</p>
              </div>

              <!-- 报名进度 -->
              <div class="card-progress">
                <div class="progress-text">
                  <span>报名进度</span>
                  <span>{{ item.currentParticipants }} / {{ item.maxParticipants || '不限' }}</span>
                </div>
                <el-progress
                  :percentage="getProgress(item.currentParticipants, item.maxParticipants)"
                  :stroke-width="6"
                  :show-text="false"
                  :color="getProgressColor(item.currentParticipants, item.maxParticipants)"
                />
              </div>
            </div>

            <div class="card-footer">
              <div class="rewards">
                <span><el-icon><Timer /></el-icon> {{ item.serviceHours || 0 }}h</span>
                <span><el-icon><Medal /></el-icon> {{ item.pointsReward || 0 }}分</span>
              </div>
              <el-button type="primary" size="small" @click.stop="goToDetail(item.id)">查看详情</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-empty v-if="!loading && activities.length === 0" description="暂无活动" :image-size="150" />
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.size"
        :total="total"
        :page-sizes="[8, 12, 24, 48]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @size-change="fetchActivities"
        @current-change="fetchActivities"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Refresh } from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import dayjs from 'dayjs'

const router = useRouter()

const defaultCover = '/default-cover.jpg'
const loading = ref(false)
const activities = ref<any[]>([])
const categories = ref<any[]>([])
const organizers = ref<any[]>([])
const total = ref(0)

// 防抖定时器
let searchTimer: ReturnType<typeof setTimeout> | null = null

const queryParams = reactive({
  page: 1,
  size: 12,
  title: '',
  categoryId: null as number | null,
  organizerId: null as number | null,
  status: null as number | null
})

const formatDate = (date: string) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : ''
}

const getStatusType = (status: number) => {
  const types: Record<number, string> = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: 'primary',
    4: 'info',
    5: 'danger'
  }
  return types[status] || 'info'
}

const getProgress = (current: number, max: number) => {
  if (!max || max <= 0) return 0
  return Math.min(Math.round((current / max) * 100), 100)
}

const getProgressColor = (current: number, max: number) => {
  const progress = getProgress(current, max)
  if (progress >= 90) return '#f56c6c'
  if (progress >= 70) return '#e6a23c'
  return '#409eff'
}

// 获取分类列表
const fetchCategories = async () => {
  try {
    const res = await request.get('/category/list')
    categories.value = res.data || []
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

// 获取主办方列表
const fetchOrganizers = async () => {
  try {
    const res = await request.get('/activity/organizers')
    organizers.value = res.data || []
  } catch (error) {
    console.error('获取主办方列表失败:', error)
  }
}

// 获取活动列表
const fetchActivities = async () => {
  loading.value = true
  try {
    const params: any = {
      page: queryParams.page,
      size: queryParams.size
    }
    if (queryParams.title) params.title = queryParams.title
    if (queryParams.categoryId) params.categoryId = queryParams.categoryId
    if (queryParams.organizerId) params.organizerId = queryParams.organizerId
    if (queryParams.status) params.status = queryParams.status

    const res = await request.get('/activity/list', params)
    activities.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('获取活动列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索（重置页码）
const handleSearch = () => {
  queryParams.page = 1
  fetchActivities()
}

// 输入框防抖搜索（300ms）
const handleInputSearch = () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  searchTimer = setTimeout(() => {
    handleSearch()
  }, 300)
}

// 重置所有筛选条件
const handleReset = () => {
  queryParams.title = ''
  queryParams.categoryId = null
  queryParams.organizerId = null
  queryParams.status = null
  queryParams.page = 1
  fetchActivities()
}

const goToDetail = (id: number) => {
  router.push(`/activity/${id}`)
}

onMounted(() => {
  fetchCategories()
  fetchOrganizers()
  fetchActivities()
})
</script>

<style lang="scss" scoped>
.activity-list {
  .filter-card {
    margin-bottom: 20px;
    border-radius: 8px;

    .filter-row {
      display: flex;
      flex-wrap: wrap;
      gap: 12px;
      align-items: center;
    }
  }

  .organizer-option {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;
  }

  .activity-grid {
    min-height: 400px;
  }

  .activity-card {
    cursor: pointer;
    margin-bottom: 20px;
    border-radius: 12px;
    overflow: hidden;
    transition: all 0.3s;

    &:hover {
      transform: translateY(-8px);
      box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
    }

    :deep(.el-card__body) {
      padding: 0;
    }

    .card-cover {
      position: relative;
      height: 180px;

      .cover-image {
        width: 100%;
        height: 100%;
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

      .status-tag {
        position: absolute;
        top: 12px;
        right: 12px;
      }
    }

    .card-body {
      padding: 16px;

      .card-category {
        margin-bottom: 8px;
      }

      .card-title {
        font-size: 16px;
        font-weight: 600;
        margin: 0 0 12px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        color: #333;
      }

      .card-info {
        margin-bottom: 12px;

        p {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 13px;
          color: #666;
          margin: 6px 0;

          .el-icon {
            color: #409eff;
          }
        }
      }

      .card-progress {
        .progress-text {
          display: flex;
          justify-content: space-between;
          font-size: 12px;
          color: #999;
          margin-bottom: 6px;
        }
      }
    }

    .card-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 16px;
      border-top: 1px solid #f0f0f0;
      background: #fafafa;

      .rewards {
        display: flex;
        gap: 16px;
        font-size: 13px;
        color: #666;

        span {
          display: flex;
          align-items: center;
          gap: 4px;
        }
      }
    }
  }

  .pagination-wrapper {
    display: flex;
    justify-content: center;
    padding: 20px 0;
  }
}
</style>
