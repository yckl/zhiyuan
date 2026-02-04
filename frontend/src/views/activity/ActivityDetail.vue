<template>
  <div class="activity-detail" v-loading="loading">
    <el-page-header @back="router.back()">
      <template #content>
        <span class="page-title">活动详情</span>
      </template>
    </el-page-header>

    <div class="detail-content" v-if="activity.id">
      <el-row :gutter="24">
        <!-- 左侧：活动信息 -->
        <el-col :xs="24" :lg="16">
          <el-card class="main-card" shadow="never">
            <!-- 封面图 -->
            <div class="cover-section">
              <el-image :src="activity.coverImage || defaultCover" fit="cover" class="cover-image">
                <template #error>
                  <div class="image-placeholder">
                    <el-icon :size="60"><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
            </div>

            <!-- 标题和标签 -->
            <div class="title-section">
              <h1 class="activity-title">{{ activity.title }}</h1>
              <div class="tag-row">
                <el-tag>{{ activity.categoryName || '未分类' }}</el-tag>
                <el-tag :type="getStatusType(activity.status)">{{ activity.statusName }}</el-tag>
                <el-tag v-if="activity.isFeatured" type="warning">精选活动</el-tag>
              </div>
            </div>

            <!-- 活动简介 -->
            <div class="section" v-if="activity.summary">
              <h3 class="section-title"><el-icon><InfoFilled /></el-icon> 活动简介</h3>
              <p class="summary-text">{{ activity.summary }}</p>
            </div>

            <!-- 活动详情 -->
            <div class="section">
              <h3 class="section-title"><el-icon><Document /></el-icon> 活动详情</h3>
              <div class="rich-content" v-html="activity.content || '暂无详情'"></div>
            </div>

            <!-- 参与要求 -->
            <div class="section" v-if="activity.requirements">
              <h3 class="section-title"><el-icon><Warning /></el-icon> 参与要求</h3>
              <p class="requirements-text">{{ activity.requirements }}</p>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧：信息卡片 -->
        <el-col :xs="24" :lg="8">
          <div class="sidebar">
            <!-- 基本信息卡 -->
            <el-card class="info-card" shadow="never">
              <template #header>
                <span><el-icon><Calendar /></el-icon> 活动信息</span>
              </template>

              <div class="info-list">
                <div class="info-item">
                  <span class="label">活动时间</span>
                  <span class="value">{{ formatDate(activity.startTime) }}</span>
                </div>
                <div class="info-item">
                  <span class="label">结束时间</span>
                  <span class="value">{{ formatDate(activity.endTime) }}</span>
                </div>
                <div class="info-item">
                  <span class="label">报名截止</span>
                  <span class="value">{{ formatDeadline(activity.deadline || activity.registerEnd) }}</span>
                </div>
                <div class="info-item">
                  <span class="label">活动地点</span>
                  <span class="value">{{ activity.location || '待定' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">主办方</span>
                  <span class="value">{{ activity.organizerName || '未知组织' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">联系方式</span>
                  <span class="value">{{ activity.contactPhone || activity.contactInfo || '暂无' }}</span>
                </div>
                <div class="info-item" v-if="activity.organizerContactPerson">
                  <span class="label">联系人</span>
                  <span class="value">{{ activity.organizerContactPerson }}</span>
                </div>
              </div>

            </el-card>

            <!-- 报名信息卡 -->
            <el-card class="register-card" shadow="never">
              <template #header>
                <span><el-icon><User /></el-icon> 报名信息</span>
              </template>

              <div class="register-stats">
                <div class="stat-item">
                  <div class="stat-value">{{ activity.currentParticipants || 0 }}</div>
                  <div class="stat-label">已报名</div>
                </div>
                <div class="stat-divider"></div>
                <div class="stat-item">
                  <div class="stat-value">{{ activity.maxParticipants || '不限' }}</div>
                  <div class="stat-label">招募人数</div>
                </div>
              </div>

              <el-progress
                :percentage="getProgress(activity.currentParticipants, activity.maxParticipants)"
                :stroke-width="10"
                :color="progressColors"
                class="register-progress"
              />

              <div class="rewards-row">
                <div class="reward-item">
                  <el-icon :size="20" color="#409eff"><Timer /></el-icon>
                  <div>
                    <div class="reward-value">{{ activity.serviceHours || 0 }} 小时</div>
                    <div class="reward-label">志愿时长</div>
                  </div>
                </div>
                <div class="reward-item">
                  <el-icon :size="20" color="#e6a23c"><Medal /></el-icon>
                  <div>
                    <div class="reward-value">{{ activity.points || activity.pointsReward || 0 }} 积分</div>
                    <div class="reward-label">积分奖励</div>
                  </div>
                </div>
              </div>

              <!-- 免审核卡选项 -->
              <div class="skip-card-option" v-if="isLoggedIn && canRegister">
                <el-checkbox
                  v-model="useSkipCard"
                  :disabled="skipCardCount === 0"
                >
                  <span class="skip-card-label">
                    使用免审核卡
                    <el-tag :type="skipCardCount > 0 ? 'success' : 'info'" size="small">
                      剩余 {{ skipCardCount }} 张
                    </el-tag>
                  </span>
                </el-checkbox>
                <p class="skip-card-tip" v-if="skipCardCount > 0">
                  ✨ 使用后可跳过审核直接报名成功
                </p>
                <p class="skip-card-tip" v-else>
                  去 <el-link type="primary" @click="$router.push('/mall/index')">积分商城</el-link> 兑换
                </p>
              </div>

              <!-- 操作按钮 -->
              <div class="action-buttons">
                <el-button
                  :type="registerBtnType"
                  size="large"
                  class="register-btn"
                  :loading="registering"
                  :disabled="!canRegister"
                  @click="handleRegister"
                >
                  {{ useSkipCard ? '🎫 免审核报名' : registerBtnText }}
                </el-button>

                <el-button
                  v-if="isLoggedIn"
                  :type="isFavorite ? 'warning' : 'default'"
                  size="large"
                  class="favorite-btn"
                  :loading="favoriting"
                  @click="handleToggleFavorite"
                >
                  <el-icon :size="18">
                    <StarFilled v-if="isFavorite" />
                    <Star v-else />
                  </el-icon>
                  {{ isFavorite ? '已收藏' : '收藏' }}
                </el-button>
              </div>

              <p class="register-tip" v-if="!isLoggedIn">
                <el-link type="primary" @click="router.push('/login')">登录</el-link> 后即可报名参加
              </p>

            </el-card>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star, StarFilled } from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()

const defaultCover = '/default-cover.jpg'
const loading = ref(false)
const registering = ref(false)
const activity = ref<any>({})
const hasRegistered = ref(false)
const isFavorite = ref(false)
const favoriting = ref(false)
const useSkipCard = ref(false)
const skipCardCount = ref(0)


const progressColors = [
  { color: '#409eff', percentage: 70 },
  { color: '#e6a23c', percentage: 90 },
  { color: '#f56c6c', percentage: 100 }
]

const isLoggedIn = computed(() => !!localStorage.getItem('token'))

const canRegister = computed(() => {
  if (!isLoggedIn.value) return false
  if (hasRegistered.value) return false
  if (activity.value.status !== 2) return false // 非报名中状态
  // 检查人数限制
  if (activity.value.maxParticipants > 0 && 
      activity.value.currentParticipants >= activity.value.maxParticipants) {
    return false
  }
  return true
})

const registerBtnType = computed(() => {
  if (hasRegistered.value) return 'success'
  if (!canRegister.value) return 'info'
  return 'primary'
})

const registerBtnText = computed(() => {
  if (!isLoggedIn.value) return '请先登录'
  if (hasRegistered.value) return '✓ 已报名'
  if (activity.value.status === 3) return '活动进行中'
  if (activity.value.status === 4) return '活动已结束'
  if (activity.value.status === 5) return '活动已取消'
  if (activity.value.maxParticipants > 0 && 
      activity.value.currentParticipants >= activity.value.maxParticipants) {
    return '名额已满'
  }
  return '立即报名'
})

const formatDate = (date: string) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : ''
}

const formatDeadline = (date: string) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '不限'
}


const getStatusType = (status: number) => {
  const types: Record<number, string> = {
    0: 'info', 1: 'warning', 2: 'success', 3: 'primary', 4: 'info', 5: 'danger'
  }
  return types[status] || 'info'
}

const getProgress = (current: number, max: number) => {
  if (!max || max <= 0) return 0
  return Math.min(Math.round((current / max) * 100), 100)
}

const fetchActivity = async () => {
  loading.value = true
  try {
    const res = await request.get(`/activity/${route.params.id}`)
    activity.value = res.data || {}
  } catch (error) {
    console.error('获取活动详情失败:', error)
    ElMessage.error('获取活动详情失败')
  } finally {
    loading.value = false
  }
}

const checkRegistration = async () => {
  if (!isLoggedIn.value) return

  try {
    const res = await request.get(`/registration/check/${route.params.id}`)
    hasRegistered.value = res.data || false
  } catch (error) {
    console.error('检查报名状态失败:', error)
  }
}

const handleRegister = async () => {
  if (!isLoggedIn.value) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }

  if (!canRegister.value) return

  ElMessageBox.confirm(
    `确定要报名参加「${activity.value.title}」吗？`,
    '确认报名',
    {
      confirmButtonText: '确定报名',
      cancelButtonText: '再想想',
      type: 'info'
    }
  ).then(async () => {
    registering.value = true
    try {
      const url = useSkipCard.value
        ? `/registration?useSkipCard=true`
        : '/registration'
      await request.post(url, { activityId: route.params.id })

      if (useSkipCard.value) {
        ElMessage.success('使用免审核卡报名成功！已直接录用')
        skipCardCount.value = Math.max(0, skipCardCount.value - 1)
      } else {
        ElMessage.success('报名成功！请等待审核')
      }
      hasRegistered.value = true
      useSkipCard.value = false
      // 刷新活动信息
      fetchActivity()
    } catch (error: any) {
      ElMessage.error(error.message || '报名失败')
      console.error('报名失败:', error)
    } finally {
      registering.value = false
    }
  }).catch(() => {})
}

const checkFavorite = async () => {
  if (!isLoggedIn.value) return

  try {
    const res = await request.get('/collection/check', {
      targetId: route.params.id, targetType: 'ACTIVITY'
    })
    isFavorite.value = res.data === true
  } catch (error) {
    console.error('检查收藏状态失败:', error)
  }
}

const fetchSkipCardCount = async () => {
  if (!isLoggedIn.value) return

  // Only volunteers have backpacks; admins/organizers don't have volunteer profiles
  try {
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      const parsed = JSON.parse(userInfo)
      const role = parsed.role || parsed.userRole
      if (role && role !== 'VOLUNTEER') {
        // Non-volunteer users don't have backpacks
        return
      }
    }
  } catch (e) {
    // Ignore parse errors
  }

  try {
    const res = await request.get('/mall/backpack')
    if (res.code === 200 && res.data) {
      const card = res.data.find((item: any) =>
        item.propName?.includes('免审核') || item.propType === 'SKIP_REVIEW_CARD'
      )
      skipCardCount.value = card?.quantity || 0
    }
  } catch (error) {
    // Silently ignore - user may not be a volunteer
    console.debug('获取免审核卡数量失败 (可能非志愿者):', error)
  }
}

const handleToggleFavorite = async () => {
  if (!isLoggedIn.value) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }

  favoriting.value = true
  try {
    await request.post('/collection/toggle', {
      targetId: Number(route.params.id),
      targetType: 'ACTIVITY'
    })
    isFavorite.value = !isFavorite.value
    ElMessage.success(isFavorite.value ? '收藏成功' : '已取消收藏')
  } catch (error) {
    console.error('操作收藏失败:', error)
  } finally {
    favoriting.value = false
  }
}

onMounted(() => {
  fetchActivity()
  checkRegistration()
  checkFavorite()
  fetchSkipCardCount()
})
</script>

<style lang="scss" scoped>
.activity-detail {
  .page-title {
    font-size: 18px;
    font-weight: 600;
  }

  .detail-content {
    margin-top: 20px;
  }

  .main-card {
    border-radius: 12px;

    .cover-section {
      margin: -20px -20px 20px;
      
      .cover-image {
        width: 100%;
        height: 300px;
        border-radius: 12px 12px 0 0;
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
    }

    .title-section {
      margin-bottom: 24px;

      .activity-title {
        font-size: 24px;
        font-weight: 600;
        margin: 0 0 12px;
        color: #333;
      }

      .tag-row {
        display: flex;
        gap: 8px;
        flex-wrap: wrap;
      }
    }

    .section {
      margin-bottom: 24px;

      .section-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 16px;
        font-weight: 600;
        margin: 0 0 12px;
        padding-bottom: 8px;
        border-bottom: 2px solid #409eff;
        color: #333;
      }

      .summary-text,
      .requirements-text {
        color: #666;
        line-height: 1.8;
        margin: 0;
      }

      .rich-content {
        color: #333;
        line-height: 1.8;

        :deep(img) {
          max-width: 100%;
          border-radius: 8px;
        }
      }
    }
  }

  .sidebar {
    position: sticky;
    top: 80px;
  }

  .info-card,
  .register-card {
    border-radius: 12px;
    margin-bottom: 20px;

    :deep(.el-card__header) {
      padding: 16px 20px;
      font-weight: 600;
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }

  .info-list {
    .info-item {
      display: flex;
      justify-content: space-between;
      padding: 10px 0;
      border-bottom: 1px dashed #eee;

      &:last-child {
        border-bottom: none;
      }

      .label {
        color: #999;
      }

      .value {
        color: #333;
        font-weight: 500;
      }
    }
  }

  .register-card {
    .register-stats {
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 30px;
      margin-bottom: 16px;

      .stat-item {
        text-align: center;

        .stat-value {
          font-size: 28px;
          font-weight: bold;
          color: #409eff;
        }

        .stat-label {
          font-size: 12px;
          color: #999;
          margin-top: 4px;
        }
      }

      .stat-divider {
        width: 1px;
        height: 40px;
        background: #eee;
      }
    }

    .register-progress {
      margin-bottom: 20px;
    }

    .rewards-row {
      display: flex;
      justify-content: space-around;
      margin-bottom: 20px;
      padding: 16px;
      background: #f9f9f9;
      border-radius: 8px;

      .reward-item {
        display: flex;
        align-items: center;
        gap: 10px;

        .reward-value {
          font-size: 16px;
          font-weight: 600;
          color: #333;
        }

        .reward-label {
          font-size: 12px;
          color: #999;
        }
      }
    }

    .skip-card-option {
      background: linear-gradient(135deg, #e8f4fd 0%, #f0e8ff 100%);
      border-radius: 8px;
      padding: 12px 16px;
      margin-bottom: 16px;
      border: 1px solid #e0e0ff;

      .skip-card-label {
        display: flex;
        align-items: center;
        gap: 8px;
        font-weight: 500;
      }

      .skip-card-tip {
        margin: 8px 0 0;
        font-size: 12px;
        color: #666;
        padding-left: 24px;
      }
    }

    .action-buttons {
      display: flex;
      gap: 12px;
      margin-bottom: 12px;

      .register-btn {
        flex: 1;
        height: 48px;
        font-size: 16px;
      }

      .favorite-btn {
        height: 48px;
        min-width: 100px;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 4px;
        font-size: 14px;
        transition: all 0.3s ease;

        &:hover {
          transform: scale(1.02);
        }
      }
    }

    .register-tip {
      text-align: center;
      font-size: 13px;
      color: #999;
      margin: 0;
    }
  }
}
</style>
