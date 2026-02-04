<template>
  <div class="org-notification-send">
    <el-card shadow="hover" class="main-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon :size="20"><Bell /></el-icon>
            <span class="title">发送通知</span>
          </div>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-position="top"
        class="send-form"
      >
        <el-row :gutter="24">
          <!-- 左侧：选择目标 -->
          <el-col :span="10">
            <div class="section">
              <h3 class="section-title">
                <el-tag type="primary" effect="dark" round>1</el-tag>
                选择发送对象
              </h3>

              <el-form-item label="选择活动" prop="activityId">
                <el-select
                  v-model="formData.activityId"
                  placeholder="请选择活动"
                  filterable
                  style="width: 100%"
                  @change="handleActivityChange"
                >
                  <el-option
                    v-for="item in activities"
                    :key="item.id"
                    :label="item.title"
                    :value="item.id"
                  >
                    <div class="activity-option">
                      <span>{{ item.title }}</span>
                      <el-tag size="small" type="info">{{ item.currentParticipants || 0 }}人</el-tag>
                    </div>
                  </el-option>
                </el-select>
              </el-form-item>

              <el-form-item label="接收群体" prop="targetGroup">
                <el-radio-group v-model="formData.targetGroup" class="target-group" @change="updateEstimate">
                  <el-radio-button label="all">
                    <el-icon><User /></el-icon> 全部报名者
                  </el-radio-button>
                  <el-radio-button label="approved">
                    <el-icon><Check /></el-icon> 仅审核通过者
                  </el-radio-button>
                  <el-radio-button label="checkedIn">
                    <el-icon><CircleCheck /></el-icon> 已签到者
                  </el-radio-button>
                  <el-radio-button label="notCheckedIn">
                    <el-icon><WarningFilled /></el-icon> 未签到者(催签)
                  </el-radio-button>
                </el-radio-group>
              </el-form-item>

              <!-- 预览卡片 -->
              <div class="preview-card" v-if="formData.activityId">
                <div class="preview-icon">
                  <el-icon :size="32"><Promotion /></el-icon>
                </div>
                <div class="preview-content">
                  <div class="preview-text">
                    将向 <strong>{{ selectedActivityName }}</strong> 的
                    <el-tag type="warning" size="small">{{ targetGroupLabel }}</el-tag>
                    发送通知
                  </div>
                  <div class="preview-count">
                    预计覆盖人数: <strong class="count">{{ estimatedCount }}</strong> 人
                  </div>
                </div>
              </div>
            </div>
          </el-col>

          <!-- 右侧：通知内容 -->
          <el-col :span="14">
            <div class="section">
              <h3 class="section-title">
                <el-tag type="primary" effect="dark" round>2</el-tag>
                编辑通知内容
              </h3>

              <el-form-item label="通知标题" prop="title">
                <el-input
                  v-model="formData.title"
                  placeholder="请输入通知标题"
                  maxlength="50"
                  show-word-limit
                >
                  <template #prefix>
                    <el-icon><Edit /></el-icon>
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item label="通知内容" prop="content">
                <el-input
                  v-model="formData.content"
                  type="textarea"
                  :rows="8"
                  placeholder="请输入通知内容，支持换行..."
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>

              <!-- 快捷模板 -->
              <div class="quick-templates">
                <span class="label">快捷模板:</span>
                <el-button size="small" @click="useTemplate('remind')">签到提醒</el-button>
                <el-button size="small" @click="useTemplate('change')">活动变更</el-button>
                <el-button size="small" @click="useTemplate('cancel')">活动取消</el-button>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="action-buttons">
              <el-button size="large" @click="handleReset">
                <el-icon><RefreshLeft /></el-icon> 重置
              </el-button>
              <el-button
                type="primary"
                size="large"
                @click="handleSend"
                :loading="sending"
                :disabled="!formData.activityId || !formData.title || !formData.content"
              >
                <el-icon><Promotion /></el-icon> 发送通知 ({{ estimatedCount }}人)
              </el-button>
            </div>
          </el-col>
        </el-row>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import {
  Bell, User, Check, CircleCheck, WarningFilled, Promotion, Edit, RefreshLeft
} from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const formRef = ref()
const activities = ref<any[]>([])
const activitiesLoading = ref(false)
const sending = ref(false)
const estimatedCount = ref(0)

const formData = reactive({
  activityId: null as number | null,
  targetGroup: 'approved',
  title: '',
  content: ''
})

const formRules = {
  activityId: [{ required: true, message: '请选择活动', trigger: 'change' }],
  targetGroup: [{ required: true, message: '请选择接收群体', trigger: 'change' }],
  title: [{ required: true, message: '请输入通知标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入通知内容', trigger: 'blur' }]
}

const TARGET_GROUP_MAP: Record<string, string> = {
  all: '全部报名者',
  approved: '审核通过者',
  checkedIn: '已签到者',
  notCheckedIn: '未签到者'
}

const selectedActivityName = computed(() => {
  const activity = activities.value.find(a => a.id === formData.activityId)
  return activity?.title || '未选择'
})

const targetGroupLabel = computed(() => TARGET_GROUP_MAP[formData.targetGroup] || '未知')

// 获取活动列表
const fetchActivities = async () => {
  activitiesLoading.value = true
  try {
    const res = await request.get('/activity/my', { page: 1, size: 100 })
    activities.value = res.data?.records || []
  } catch (e) {
    console.error('获取活动列表失败:', e)
  } finally {
    activitiesLoading.value = false
  }
}

// 更新预估人数 (模拟)
const updateEstimate = () => {
  if (!formData.activityId) {
    estimatedCount.value = 0
    return
  }
  
  const activity = activities.value.find(a => a.id === formData.activityId)
  const total = activity?.currentParticipants || 0
  
  // 模拟不同群体的人数比例
  switch (formData.targetGroup) {
    case 'all':
      estimatedCount.value = total + Math.floor(Math.random() * 5)
      break
    case 'approved':
      estimatedCount.value = total
      break
    case 'checkedIn':
      estimatedCount.value = Math.floor(total * 0.7) + Math.floor(Math.random() * 3)
      break
    case 'notCheckedIn':
      estimatedCount.value = Math.floor(total * 0.3) + Math.floor(Math.random() * 3)
      break
    default:
      estimatedCount.value = total
  }
}

const handleActivityChange = () => {
  updateEstimate()
}

// 快捷模板
const useTemplate = (type: string) => {
  switch (type) {
    case 'remind':
      formData.title = '活动签到提醒'
      formData.content = '亲爱的志愿者：\n\n您报名的活动即将开始，请及时前往签到。\n\n活动地点：[地点]\n活动时间：[时间]\n\n如有问题请及时联系组织者。'
      break
    case 'change':
      formData.title = '活动信息变更通知'
      formData.content = '亲爱的志愿者：\n\n您报名的活动信息有所变更，请注意查收：\n\n变更内容：\n[请在此处填写变更详情]\n\n给您带来的不便，敬请谅解。'
      break
    case 'cancel':
      formData.title = '活动取消通知'
      formData.content = '亲爱的志愿者：\n\n非常抱歉通知您，由于[原因]，原定于[日期]举行的活动已取消。\n\n如有疑问请联系组织者。再次感谢您的理解与支持。'
      break
  }
}

// 重置表单
const handleReset = () => {
  formData.activityId = null
  formData.targetGroup = 'approved'
  formData.title = ''
  formData.content = ''
  estimatedCount.value = 0
}

// 发送通知
const handleSend = async () => {
  try {
    await formRef.value.validate()
    
    await ElMessageBox.confirm(
      `确定要向 ${estimatedCount.value} 名志愿者发送此通知吗？`,
      '发送确认',
      { type: 'info' }
    )

    sending.value = true
    await request.post('/notification/broadcast', {
      activityId: formData.activityId,
      targetGroup: formData.targetGroup,
      title: formData.title,
      content: formData.content
    })

    ElMessage.success(`通知发送成功，已覆盖 ${estimatedCount.value} 人`)
    handleReset()
  } catch (error: any) {
    if (error !== 'cancel' && error !== false) {
      console.error('发送失败:', error)
      ElMessage.error('发送失败，请重试')
    }
  } finally {
    sending.value = false
  }
}

watch(() => formData.targetGroup, updateEstimate)

onMounted(() => {
  fetchActivities()
})
</script>

<style scoped lang="scss">
.org-notification-send {
  padding: 10px;

  .main-card {
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
        font-size: 16px;
        font-weight: 600;
      }
    }
  }

  .section {
    .section-title {
      display: flex;
      align-items: center;
      gap: 10px;
      margin: 0 0 20px;
      font-size: 15px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }
  }

  .activity-option {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
  }

  .target-group {
    display: flex;
    flex-direction: column;
    gap: 8px;

    :deep(.el-radio-button) {
      width: 100%;
      
      .el-radio-button__inner {
        width: 100%;
        text-align: left;
        display: flex;
        align-items: center;
        gap: 8px;
        border-radius: 8px !important;
        border: 1px solid var(--el-border-color) !important;
        box-shadow: none !important;
      }

      &.is-active .el-radio-button__inner {
        background: var(--el-color-primary-light-9);
        border-color: var(--el-color-primary) !important;
        color: var(--el-color-primary);
      }
    }
  }

  .preview-card {
    display: flex;
    gap: 16px;
    padding: 16px;
    background: linear-gradient(135deg, #e3f2fd 0%, #f3e5f5 100%);
    border-radius: 12px;
    margin-top: 20px;

    .preview-icon {
      width: 56px;
      height: 56px;
      background: white;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: var(--el-color-primary);
      box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    }

    .preview-content {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: center;

      .preview-text {
        font-size: 14px;
        color: var(--el-text-color-regular);
        margin-bottom: 8px;

        strong {
          color: var(--el-color-primary);
        }
      }

      .preview-count {
        font-size: 13px;
        color: var(--el-text-color-secondary);

        .count {
          font-size: 20px;
          color: var(--el-color-success);
        }
      }
    }
  }

  .quick-templates {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-top: 12px;

    .label {
      font-size: 13px;
      color: var(--el-text-color-secondary);
    }
  }

  .action-buttons {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    margin-top: 24px;
    padding-top: 24px;
    border-top: 1px solid var(--el-border-color-lighter);
  }
}
</style>
