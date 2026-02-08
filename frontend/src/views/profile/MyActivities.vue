<template>
  <div class="my-activities">
    <el-page-header @back="router.back()">
      <template #content>
        <span class="page-title">我的报名管理</span>
      </template>
    </el-page-header>

    <el-card class="activities-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>报名记录</span>
          <el-tag type="info">共 {{ total }} 条记录</el-tag>
        </div>
      </template>

      <el-table 
        :data="registrations" 
        v-loading="loading" 
        stripe 
        border
        style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
      >
        <el-table-column prop="activityTitle" label="活动名称" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <el-link type="primary" @click="router.push(`/activity/${row.activityId}`)">
              {{ row.activityTitle }}
            </el-link>
          </template>
        </el-table-column>
        
        <el-table-column label="活动时间" width="180">
          <template #default="{ row }">
            <div class="time-cell">
              <el-icon><Clock /></el-icon>
              <span>{{ formatDate(row.activityStartTime) }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="报名时间" width="180">
          <template #default="{ row }">
            <div class="time-cell">
              <el-icon><Calendar /></el-icon>
              <span>{{ formatDate(row.createTime) }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="当前状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="light">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="备注/原因" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.status === 6 && row.remark" class="text-danger">{{ row.remark }}</span>
            <span v-else>{{ row.remark || '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="获得评分" width="140" align="center">
          <template #default="{ row }">
            <div v-if="row.rating" class="rating-cell">
               <el-tooltip :content="row.ratingComment || '暂无评语'" placement="top">
                 <el-rate
                    v-model="row.rating"
                    disabled
                    show-score
                    text-color="#ff9900"
                    score-template="{value}"
                  />
               </el-tooltip>
            </div>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-buttons">
              <!-- 签到：仅当已通过(1)或已通过需签到 且活动并未结束 -->
              <el-button 
                v-if="row.status === 1"
                type="success" 
                link 
                size="small"
                @click="openCheckin(row)"
              >
                <el-icon><Location /></el-icon> 签到
              </el-button>

              <!-- 重新报名：仅当已拒绝(6) 或 已取消(4) -->
              <el-button 
                v-if="row.status === 6 || row.status === 4"
                type="primary" 
                link 
                size="small"
                @click="router.push(`/activity/${row.activityId}`)"
              >
                <el-icon><RefreshRight /></el-icon> 重新报名
              </el-button>
              
              <!-- 取消报名：仅当待审核或已通过且活动未开始时 -->
              <el-popconfirm
                v-if="canCancel(row)"
                title="确定要取消报名吗？此操作不可恢复"
                confirm-button-text="确定取消"
                cancel-button-text="再想想"
                confirm-button-type="danger"
                @confirm="handleCancel(row.id)"
              >
                <template #reference>
                  <el-button type="danger" link size="small">
                    <el-icon><Close /></el-icon> 取消报名
                  </el-button>
                </template>
              </el-popconfirm>
              
              <!-- 发布心得：仅当已完成时 -->
              <el-button 
                v-if="row.status === 3"
                type="success" 
                link 
                size="small"
                @click="goCreateExperience(row.activityId)"
              >
                <el-icon><Edit /></el-icon> 发布心得
              </el-button>

              <!-- 评价活动：仅当已完成时 -->
              <el-button 
                v-if="row.status === 3"
                type="warning" 
                link 
                size="small"
                @click="handleReview(row.activityId)"
              >
                <el-icon><ChatLineRound /></el-icon> 评价
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 签到弹窗 -->
      <el-dialog
        v-model="checkinVisible"
        title="活动签到"
        width="360px"
        center
        destroy-on-close
      >
        <div class="checkin-content">
          <p class="checkin-tip">请输入活动现场公布的6位签到码</p>
          <el-input
            v-model="checkinCode"
            maxlength="6"
            placeholder="请输入签到码"
            size="large"
            style="letter-spacing: 2px; text-align: center;"
            input-style="text-align: center; font-size: 18px; font-weight: bold;"
            @keyup.enter="submitCheckin"
          >
            <template #prefix>
              <el-icon><Key /></el-icon>
            </template>
          </el-input>
        </div>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="checkinVisible = false">取消</el-button>
            <el-button type="primary" @click="submitCheckin" :loading="checkingIn" :disabled="checkinCode.length !== 6">
              确认签到
            </el-button>
          </span>
        </template>
      </el-dialog>

      <review-dialog ref="reviewDialogRef" @success="fetchRegistrations" />

      <el-empty v-if="!loading && registrations.length === 0" description="暂无报名记录" :image-size="120" />

      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="fetchRegistrations"
          @current-change="fetchRegistrations"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Clock, Calendar, Close, Edit, ChatLineRound, Location, Key, RefreshRight } from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import ReviewDialog from '@/components/ReviewDialog.vue'
import dayjs from 'dayjs'

const router = useRouter()

const loading = ref(false)
const registrations = ref<any[]>([])
const total = ref(0)
const reviewDialogRef = ref()
const checkinVisible = ref(false)
const checkinCode = ref('')
const checkingIn = ref(false)
const currentActivityId = ref<number | null>(null)

const queryParams = reactive({
  page: 1,
  size: 10
})

// 状态字典：数字 -> 中文
const STATUS_MAP: Record<number, { label: string; type: string }> = {
  0: { label: '待审核', type: 'warning' },
  1: { label: '已通过', type: 'success' },
  2: { label: '已签到', type: 'primary' },
  3: { label: '已完成', type: 'info' },
  4: { label: '已取消', type: 'info' },
  5: { label: '缺席', type: 'danger' },
  6: { label: '已拒绝', type: 'danger' }
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

// 判断是否可以取消报名
const canCancel = (row: any) => {
  // 状态为待审核(0)或已通过(1)，且活动开始时间晚于当前时间
  if (row.status !== 0 && row.status !== 1) return false
  if (!row.activityStartTime) return true
  return dayjs(row.activityStartTime).isAfter(dayjs())
}

const fetchRegistrations = async () => {
  loading.value = true
  try {
    const res = await request.get('/registration/my', queryParams)
    registrations.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('获取报名记录失败:', error)
  } finally {
    loading.value = false
  }
}

const handleCancel = async (id: number) => {
  try {
    await request.post(`/registration/cancel/${id}`)
    ElMessage.success('取消报名成功')
    fetchRegistrations()
  } catch (error) {
    console.error('取消失败:', error)
  }
}

const goCreateExperience = (activityId: number) => {
  router.push({
    path: '/experience/create',
    query: { activityId }
  })
}

const handleReview = (activityId: number) => {
  reviewDialogRef.value?.show(activityId)
}

const openCheckin = (row: any) => {
  checkinVisible.value = true
  checkinCode.value = ''
  currentActivityId.value = row.activityId // 其实此处可能不需要，因为是验证码
}

const submitCheckin = async () => {
  if (checkinCode.value.length !== 6) {
    ElMessage.warning('请输入6位签到码')
    return
  }
  
  checkingIn.value = true
  try {
    await request.post('/registration/signin/code', { code: checkinCode.value })
    ElMessage.success('签到成功！')
    checkinVisible.value = false
    fetchRegistrations() // 刷新列表
  } catch (e: any) {
    console.error('签到失败:', e)
    ElMessage.error(e.msg || '签到失败，请检查签到码')
  } finally {
    checkingIn.value = false
  }
}

onMounted(() => {
  fetchRegistrations()
})
</script>

<style lang="scss" scoped>
.my-activities {
  .page-title {
    font-size: 18px;
    font-weight: 600;
    color: #333;
  }

  .activities-card {
    margin-top: 20px;
    border-radius: 12px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
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

  .checkin-content {
    text-align: center;
    padding: 10px 0;
    
    .checkin-tip {
      margin-bottom: 20px;
      color: #606266;
    }
  }

  .text-danger {
    color: #f56c6c;
  }
}
</style>
