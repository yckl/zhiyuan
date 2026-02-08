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
          <ActivityCard :activity="item" />
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
import { Search, Refresh } from '@element-plus/icons-vue'
import { request } from '@/utils/request'


import ActivityCard from '@/components/ActivityCard.vue'




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
        background: var(--el-fill-color-light);
        color: var(--el-text-color-placeholder);
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
        color: var(--el-text-color-primary);
      }

      .card-info {
        margin-bottom: 12px;

        p {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 13px;
          color: var(--el-text-color-secondary);
          margin: 6px 0;

          .el-icon {
            color: var(--el-color-primary);
          }
        }
      }

      .card-progress {
        .progress-text {
          display: flex;
          justify-content: space-between;
          font-size: 12px;
          color: var(--el-text-color-secondary);
          margin-bottom: 6px;
        }
      }
    }

    .card-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 16px;
      border-top: 1px solid var(--el-border-color-lighter);
      background: var(--el-fill-color-lighter);

      .rewards {
        display: flex;
        gap: 16px;
        font-size: 13px;
        color: var(--el-text-color-secondary);

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
