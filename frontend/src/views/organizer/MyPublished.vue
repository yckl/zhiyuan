<template>
  <div class="my-published">
    <el-page-header @back="router.back()" class="premium-header">
      <template #content>
        <div class="header-title-wrap">
          <span class="page-title">所有发</span>
          <el-tag effect="dark" round class="count-tag">{{ total }}</el-tag>
        </div>
      </template>
      <template #extra>
        <el-button type="primary" class="publish-btn" @click="router.push('/organizer/activity/create')">
          <el-icon><Plus /></el-icon> 发布新活?
        </el-button>
      </template>
    </el-page-header>

    <div class="card-table-container" v-loading="loading">
      <div v-if="activities.length > 0" class="activity-list">
        <div 
          v-for="row in activities" 
          :key="row.id" 
          class="activity-card-row"
        >
          <!-- 左侧缩略?-->
          <div class="thumb-wrap" @click="router.push(`/activity/${row.id}`)">
            <el-image 
              :src="row.coverImage" 
              fit="cover" 
              class="cover-thumb"
            >
              <template #error>
                <div class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="status-overlay">
              <el-tag :type="getStatusType(row.status)" effect="dark" size="small" round>
                {{ getStatusLabel(row.status) }}
              </el-tag>
            </div>
          </div>

          <!-- 主体信息 -->
          <div class="content-wrap">
            <div class="info-top">
              <h3 class="act-title" @click="router.push(`/activity/${row.id}`)">{{ row.title }}</h3>
              <div class="time-badge">
                <el-icon><Clock /></el-icon>
                <span>{{ formatDate(row.startTime) }}</span>
              </div>
            </div>

            <div class="info-bottom">
              <!-- 进度条区?-->
              <div class="progress-section">
                <div class="quota-text">
                  <span>已报酬</span>
                  <span class="nums"><b>{{ row.currentParticipants || 0 }}</b> / {{ row.maxParticipants || '不限' }}</span>
                </div>
                <!-- 重点：极光青渐变进度?-->
                <div class="custom-progress">
                  <div class="progress-track">
                    <div 
                      class="progress-fill" 
                      :style="{ width: `${getQuotaPercentage(row)}%` }"
                    ></div>
                  </div>
                </div>
              </div>

              <!-- 悬浮操作栏?-->
              <div class="hover-actions">
                <el-button 
                  class="action-btn edit" 
                  @click.stop="handleEdit(row.id)"
                  v-if="row.status !== 4 && row.status !== 5"
                >
                  <el-icon><Edit /></el-icon>
                  <span>编辑</span>
                </el-button>
                
                <el-button 
                  class="action-btn audit" 
                  @click.stop="handleAudit(row.id)"
                >
                  <el-icon><User /></el-icon>
                  <span>人员管理</span>
                </el-button>
                
                <el-popconfirm
                  title="确定要下架此活动吗？"
                  confirm-button-text="确定"
                  cancel-button-text="取消"
                  confirm-button-type="danger"
                  @confirm="handleDelete(row.id)"
                >
                  <template #reference>
                    <el-button class="action-btn delete" @click.stop>
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </template>
                </el-popconfirm>
              </div>
            </div>
          </div>
        </div>
      </div>

      <el-empty v-else-if="!loading" description="暂无发布的活? :image-size="160">
        <template #image>
          <img src="/3d-hand.png" alt="empty" @error="(e) => ((e.target as HTMLImageElement).style.display='none')" />
          <div style="font-size: 64px" v-if="true">🏖</div>
        </template>
        <el-button type="primary" class="publish-btn" @click="router.push('/organizer/activity/create')" size="large">
          开启你的第一个活?
        </el-button>
      </el-empty>

      <div class="pagination-wrapper" v-if="total > queryParams.size">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
          @size-change="fetchActivities"
          @current-change="fetchActivities"
        />
      </div>
    </div>
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
/* defaultCover no longer needed as we use the image placeholder, but keep for fallback if needed */

const queryParams = reactive({
  page: 1,
  size: 10
})

// 状态字?
const STATUS_MAP: Record<number, { label: string; type: string }> = {
  0: { label: '草稿', type: 'info' },
  1: { label: '待审?, type: 'warning' },
  2: { label: '预热?, type: 'primary' },
  3: { label: '进行中?, type: 'success' },
  4: { label: '已结?, type: 'info' },
  5: { label: '已取?, type: 'danger' }
}

const getStatusLabel = (status: number) => {
  return STATUS_MAP[status]?.label || '其他'
}

const getStatusType = (status: number) => {
  return STATUS_MAP[status]?.type || 'info'
}

const formatDate = (date: string) => {
  return date ? dayjs(date).format('MM月DD日?HH:mm') : '时间待定'
}

const getQuotaPercentage = (row: any) => {
  if (!row.maxParticipants || row.maxParticipants <= 0) return 0
  return Math.min(Math.round((row.currentParticipants / row.maxParticipants) * 100), 100)
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
    ElMessage.success('活动已下?)
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
  padding: 24px 32px;
  background: #f8fafc;
  min-height: calc(100vh - 64px);

  .premium-header {
    margin-bottom: 32px;
    :deep(.el-page-header__left) { align-items: center; }
    
    .header-title-wrap {
      display: flex; align-items: center; gap: 12px;
      .page-title { font-size: 24px; font-weight: 900; color: #0f172a; letter-spacing: -0.5px; }
      .count-tag { background: #1e293b; border: none; font-weight: 800; }
    }
  }

  .publish-btn {
    background: linear-gradient(135deg, #0093E9 0%, #80D0C7 100%);
    border: none; border-radius: 99px; padding: 0 24px; font-weight: 700;
    box-shadow: 0 8px 16px rgba(0, 147, 233, 0.2); transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    &:hover { transform: translateY(-2px); box-shadow: 0 12px 24px rgba(0, 147, 233, 0.3); }
  }

  .card-table-container {
    max-width: 1000px; margin: 0 auto;
  }

  .activity-list {
    display: flex; flex-direction: column; gap: 16px;
  }

  /* 去线条化，独立磨砂圆角卡?*/
  .activity-card-row {
    display: flex;
    background: rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(20px);
    border: 1px solid rgba(255, 255, 255, 0.5);
    border-radius: 20px;
    padding: 16px;
    gap: 20px;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.02), 0 2px 4px -1px rgba(0, 0, 0, 0.01);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    overflow: hidden;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.05), 0 10px 10px -5px rgba(0, 0, 0, 0.02);
      border-color: rgba(0, 147, 233, 0.2);

      /* 激活悬浮操作栏 */
      .hover-actions {
        opacity: 1;
        transform: translateX(0);
        pointer-events: auto;
      }
    }

    /* 左侧微缩?(16:9) */
    .thumb-wrap {
      width: 160px; height: 90px;
      border-radius: 12px;
      overflow: hidden;
      position: relative;
      flex-shrink: 0;
      cursor: pointer;

      .cover-thumb {
        width: 100%; height: 100%;
        transition: transform 0.3s;
        &:hover { transform: scale(1.05); }
      }

      .image-placeholder {
        width: 100%; height: 100%;
        background: linear-gradient(135deg, #f1f5f9, #e2e8f0);
        display: flex; align-items: center; justify-content: center;
        color: #94a3b8; font-size: 24px;
      }

      .status-overlay {
        position: absolute; top: 6px; left: 6px;
        :deep(.el-tag) { border: none; font-weight: 700; box-shadow: 0 2px 8px rgba(0,0,0,0.15); backdrop-filter: blur(4px); background-color: rgba(0,0,0,0.6); }
      }
    }

    /* 主体信息 */
    .content-wrap {
      flex: 1; display: flex; flex-direction: column; justify-content: space-between; min-width: 0;
      padding: 4px 0;

      .info-top {
        display: flex; justify-content: space-between; align-items: flex-start;
        .act-title { 
          margin: 0; font-size: 18px; font-weight: 800; color: #1e293b; cursor: pointer;
          white-space: nowrap; overflow: hidden; text-overflow: ellipsis; padding-right: 16px; transition: color 0.2s;
          &:hover { color: #0093E9; }
        }
        .time-badge {
          display: flex; align-items: center; gap: 4px; font-size: 12px; font-weight: 700; color: #64748b;
          background: #f1f5f9; padding: 4px 10px; border-radius: 6px; flex-shrink: 0;
        }
      }

      .info-bottom {
        display: flex; align-items: flex-end; justify-content: space-between; position: relative;

        .progress-section {
          flex: 1; max-width: 400px;
          .quota-text { 
            display: flex; justify-content: space-between; font-size: 12px; font-weight: 600; color: #64748b; margin-bottom: 6px;
            .nums b { color: #0f172a; font-size: 14px; }
          }
          
          /* 渐变进度?*/
          .custom-progress {
            height: 6px; background: #e2e8f0; border-radius: 99px; overflow: hidden;
            .progress-fill {
              height: 100%; border-radius: 99px;
              background: linear-gradient(90deg, #0093E9 0%, #80D0C7 100%);
              transition: width 0.8s cubic-bezier(0.34, 1.56, 0.64, 1);
            }
          }
        }

        /* 悬浮操作栏?*/
        .hover-actions {
          display: flex; gap: 8px;
          opacity: 0; transform: translateX(20px); pointer-events: none;
          transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
          background: linear-gradient(90deg, transparent, rgba(255,255,255,0.9) 10%, #fff 20%);
          padding-left: 30px; /* Fade edge */

          .action-btn {
            border: none; background: #f1f5f9; border-radius: 10px; height: 36px; padding: 0 16px;
            font-weight: 700; font-size: 13px; color: #475569; transition: all 0.2s;
            display: flex; align-items: center; gap: 6px;
            
            &:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.08); }
            &.edit:hover { background: #e0f2fe; color: #0284c7; }
            &.audit { background: rgba(0, 147, 233, 0.1); color: #0093E9; }
            &.audit:hover { background: #0093E9; color: #fff; box-shadow: 0 4px 12px rgba(0, 147, 233, 0.3); }
            &.delete { width: 36px; padding: 0; justify-content: center; }
            &.delete:hover { background: #fee2e2; color: #dc2626; }
          }
        }
      }
    }
  }

  .pagination-wrapper { margin-top: 32px; display: flex; justify-content: center; }

  /* 移动端适配 */
  @media (max-width: 768px) {
    padding: 16px;
    .premium-header { flex-direction: column; align-items: flex-start; gap: 16px; }
    .activity-card-row {
      flex-direction: column; gap: 12px; padding: 12px;
      .thumb-wrap { width: 100%; height: 180px; }
      .content-wrap .info-bottom {
        flex-direction: column; align-items: stretch; gap: 16px;
        .progress-section { max-width: 100%; }
      }
      .hover-actions {
        opacity: 1; transform: none; pointer-events: auto; padding-left: 0;
        background: none; justify-content: flex-end;
      }
    }
  }
}
</style>
