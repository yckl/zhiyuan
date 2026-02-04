<template>
  <div class="my-favorite-page">
    <div class="page-header">
      <h2>❤️ 我的收藏</h2>
      <p class="subtitle">您收藏的活动、课程和公告</p>
    </div>

    <!-- 分类标签 -->
    <el-tabs v-model="activeTab" class="favorite-tabs">
      <el-tab-pane label="活动收藏" name="activity">
        <div v-if="activityList.length > 0" class="list-container">
          <div
            v-for="item in activityList"
            :key="item.id"
            class="favorite-item"
            @click="goToDetail('activity', item.targetId)"
          >
            <img :src="item.coverImage || '/placeholder.png'" :alt="item.title" class="item-cover" />
            <div class="item-info">
              <h4 class="item-title">{{ item.title }}</h4>
              <p class="item-desc">{{ item.summary || '暂无描述' }}</p>
              <div class="item-meta">
                <span class="item-date">
                  <el-icon><Calendar /></el-icon>
                  {{ formatDate(item.createTime) }}
                </span>
                <el-button
                  type="danger"
                  text
                  size="small"
                  @click.stop="handleUnfavorite(item, 'ACTIVITY')"
                >
                  取消收藏
                </el-button>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无活动收藏" :image-size="120" />
      </el-tab-pane>

      <el-tab-pane label="课程收藏" name="course">
        <div v-if="courseList.length > 0" class="list-container">
          <div
            v-for="item in courseList"
            :key="item.id"
            class="favorite-item"
            @click="goToDetail('course', item.targetId)"
          >
            <img :src="item.coverImage || '/placeholder.png'" :alt="item.title" class="item-cover" />
            <div class="item-info">
              <h4 class="item-title">{{ item.title }}</h4>
              <p class="item-desc">{{ item.summary || '暂无描述' }}</p>
              <div class="item-meta">
                <span class="item-date">
                  <el-icon><Calendar /></el-icon>
                  {{ formatDate(item.createTime) }}
                </span>
                <el-button
                  type="danger"
                  text
                  size="small"
                  @click.stop="handleUnfavorite(item, 'COURSE')"
                >
                  取消收藏
                </el-button>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无课程收藏" :image-size="120" />
      </el-tab-pane>

      <el-tab-pane label="公告收藏" name="notice">
        <div v-if="noticeList.length > 0" class="list-container">
          <div
            v-for="item in noticeList"
            :key="item.id"
            class="favorite-item notice-item"
            @click="goToDetail('notice', item.targetId)"
          >
            <div class="item-info full-width">
              <h4 class="item-title">{{ item.title }}</h4>
              <p class="item-desc">{{ item.summary || '暂无描述' }}</p>
              <div class="item-meta">
                <span class="item-date">
                  <el-icon><Calendar /></el-icon>
                  {{ formatDate(item.createTime) }}
                </span>
                <el-button
                  type="danger"
                  text
                  size="small"
                  @click.stop="handleUnfavorite(item, 'NOTICE')"
                >
                  取消收藏
                </el-button>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无公告收藏" :image-size="120" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Calendar } from '@element-plus/icons-vue'
import { request } from '@/utils/request'

interface FavoriteItem {
  id: number
  targetId: number
  targetType: string
  title?: string
  coverImage?: string
  summary?: string
  createTime: string
}

const router = useRouter()
const activeTab = ref('activity')

const activityList = ref<FavoriteItem[]>([])
const courseList = ref<FavoriteItem[]>([])
const noticeList = ref<FavoriteItem[]>([])

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

const fetchFavorites = async (type: string) => {
  try {
    const res = await request.get('/collection/list', {
      targetType: type.toUpperCase(), pageNum: 1, pageSize: 50
    })
    if (res.code === 200 && res.data?.records) {
      return res.data.records
    }
    return []
  } catch (error) {
    console.error('获取收藏列表失败:', error)
    return []
  }
}

const loadCurrentTab = async () => {
  const typeMap: Record<string, string> = {
    activity: 'ACTIVITY',
    course: 'COURSE',
    notice: 'NOTICE'
  }
  
  const type = typeMap[activeTab.value]
  const list = await fetchFavorites(type)
  
  switch (activeTab.value) {
    case 'activity':
      activityList.value = list
      break
    case 'course':
      courseList.value = list
      break
    case 'notice':
      noticeList.value = list
      break
  }
}

const goToDetail = (type: string, id: number) => {
  const routeMap: Record<string, string> = {
    activity: `/activity/${id}`,
    course: `/training/detail/${id}`,
    notice: `/notice/detail/${id}`
  }
  router.push(routeMap[type])
}

const handleUnfavorite = async (item: FavoriteItem, type: string) => {
  try {
    await ElMessageBox.confirm('确定取消收藏吗？', '提示', { type: 'warning' })
    
    const res = await request.post('/collection/toggle', {
      targetId: item.targetId,
      targetType: type
    })

    if (res.code === 200) {
      ElMessage.success('已取消收藏')
      loadCurrentTab()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

watch(activeTab, loadCurrentTab)

onMounted(loadCurrentTab)
</script>

<style lang="scss" scoped>
.my-favorite-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;

  h2 {
    margin: 0 0 8px;
    font-size: 24px;
  }

  .subtitle {
    margin: 0;
    color: #999;
  }
}

.favorite-tabs {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.list-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.favorite-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #f9f9f9;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    background: #f0f0f0;
    transform: translateX(4px);
  }

  &.notice-item {
    .full-width {
      width: 100%;
    }
  }

  .item-cover {
    width: 120px;
    height: 80px;
    border-radius: 6px;
    object-fit: cover;
    flex-shrink: 0;
  }

  .item-info {
    flex: 1;
    display: flex;
    flex-direction: column;

    .item-title {
      margin: 0 0 8px;
      font-size: 16px;
      font-weight: 500;
      color: #333;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .item-desc {
      margin: 0 0 8px;
      font-size: 13px;
      color: #999;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      flex: 1;
    }

    .item-meta {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .item-date {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 12px;
        color: #999;
      }
    }
  }
}
</style>
