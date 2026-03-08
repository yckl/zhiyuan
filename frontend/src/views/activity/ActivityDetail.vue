<template>
  <div class="activity-detail-page" :class="{ 'is-mobile': isMobile }" v-loading="loading">

    <!-- ==================== 移动端：沉浸式单行 ==================== -->
    <template v-if="isMobile && activity.id">
      <!-- 沉浸式顶部导航（滚动变色） -->
      <header class="immersive-header" :class="{ scrolled: headerScrolled }">
        <el-icon class="header-back" @click="router.back()"><ArrowLeft /></el-icon>
        <span class="header-title" v-show="headerScrolled">{{ activity.title }}</span>
        <el-icon class="header-share" @click="handleShare"><Share /></el-icon>
      </header>

      <!-- 可滚动内容区 -->
      <div class="immersive-body" ref="scrollRef" @scroll="onScroll">
        <!-- Hero 大图 -->
        <div class="hero-image">
          <el-image :src="getImageUrl(activity.coverImage || activity.image, defaultCover)" fit="cover" class="hero-img">
            <template #error>
              <div class="hero-fallback"><el-icon :size="48"><Picture /></el-icon></div>
            </template>
          </el-image>
          <!-- 状态角 -->
          <el-tag class="hero-status" :type="getStatusType(activity.status)" effect="dark" size="small">
            {{ activity.statusName }}
          </el-tag>
        </div>

        <!-- 内容卡片（覆盖大图） -->
        <div class="content-card">
          <!-- 标题 -->
          <h1 class="detail-title">{{ activity.title }}</h1>
          <div class="tag-row">
            <el-tag size="small" round>{{ activity.categoryName || '未分类' }}</el-tag>
            <el-tag v-if="activity.isFeatured" type="warning" size="small" round>精选</el-tag>
          </div>

          <!-- 信息列表 -->
          <div class="info-grid">
            <div class="info-row">
              <el-icon color="#00BFA6"><Clock /></el-icon>
              <div>
                <div class="info-main">{{ formatDate(activity.startTime) }}</div>
                <div class="info-sub">- {{ formatDate(activity.endTime) }}</div>
              </div>
            </div>
            <div class="info-row">
              <el-icon color="#FF6B6B"><Location /></el-icon>
              <div class="info-main">{{ activity.location || '待定' }}</div>
            </div>
            <div class="info-row">
              <el-icon color="#409EFF"><OfficeBuilding /></el-icon>
              <div class="info-main">{{ activity.organizerName || '未知组织' }}</div>
            </div>
            <div class="info-row" v-if="activity.contactPhone || activity.contactInfo">
              <el-icon color="#E6A23C"><Phone /></el-icon>
              <div class="info-main">{{ activity.contactPhone || activity.contactInfo }}</div>
            </div>
          </div>

          <!-- 报名进度 -->
          <div class="register-progress-section">
            <div class="progress-header">
              <span class="progress-label">报名进度</span>
              <span class="progress-nums">
                <strong>{{ activity.currentParticipants || 0 }}</strong> / {{ activity.maxParticipants || '不限' }}
              </span>
            </div>
            <el-progress
              :percentage="getProgress(activity.currentParticipants, activity.maxParticipants)"
              :stroke-width="8"
              :color="progressColors"
              :show-text="false"
            />
          </div>

          <!-- 奖励信息 -->
          <div class="rewards-bar">
            <div class="reward-chip">
              <el-icon color="#409EFF"><Timer /></el-icon>
              <span>{{ activity.serviceHours || 0 }}h 时长</span>
            </div>
            <div class="reward-chip">
              <el-icon color="#E6A23C"><Medal /></el-icon>
              <span>{{ activity.points || activity.pointsReward || 0 }} 积分</span>
            </div>
          </div>

          <!-- 截止提醒 -->
          <div class="deadline-tip" v-if="activity.deadline || activity.registerEnd">
            ⏱ 报名截止：{{ formatDate(activity.deadline || activity.registerEnd) }}
          </div>

          <!-- 活动简介 -->
          <div class="section-block" v-if="activity.summary">
            <h3 class="section-heading">活动简介</h3>
            <p class="section-text">{{ activity.summary }}</p>
          </div>

          <!-- 活动详情 -->
          <div class="section-block">
            <h3 class="section-heading">活动详情</h3>
            <div class="rich-content" v-html="activity.content || '暂无详情'"></div>
          </div>

          <!-- 参与要求 -->
          <div class="section-block" v-if="activity.requirements">
            <h3 class="section-heading">参与要求</h3>
            <p class="section-text">{{ activity.requirements }}</p>
          </div>

          <!-- 参与者头像墙 -->
          <div class="section-block" v-if="mockParticipants.length">
            <h3 class="section-heading">已报名参与</h3>
            <div class="avatar-wall">
              <el-avatar
                v-for="(p, idx) in mockParticipants.slice(0, 8)"
                :key="idx"
                :size="36"
                :style="{ background: p.color }"
              >{{ p.name.charAt(0) }}</el-avatar>
              <span class="avatar-more" v-if="(activity.currentParticipants || 0) > 8">
                +{{ (activity.currentParticipants || 0) - 8 }}
              </span>
            </div>
          </div>

          <!-- 评论区 -->
          <CommentSection class="section-block" targetType="ACTIVITY" :targetId="Number(route.params.id)" />

          <!-- 推荐活动 (响应式网格) -->
          <div class="recommend-section" v-if="recommendedActivities.length">
            <div class="section-header">
              <el-icon><StarFilled /></el-icon>
              <span>为你推荐</span>
            </div>
            
            <div class="recommend-responsive-grid">
              <div 
                class="grid-card" 
                v-for="item in recommendedActivities.slice(0, 4)" 
                :key="item.id"
                @click="router.push(`/activity/${item.id}`)"
              >
                <div class="card-cover">
                  <el-image :src="item.coverImage || defaultCover" fit="cover">
                    <template #error><div class="rec-cover-fallback">{{ item.title.slice(0, 2) }}</div></template>
                  </el-image>
                  <div class="status-badge" v-if="item.algoTag">{{ item.algoTag }}</div>
                </div>
                
                <div class="card-info">
                  <div class="card-title">{{ item.title }}</div>
                  <div class="card-time">{{ formatDate(item.startTime) }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 底部安全区占位 -->
          <div class="bottom-spacer"></div>
        </div>
      </div>

      <!-- 底部固定操作栏 -->
      <div class="mobile-bottom-bar">
        <div class="bottom-icons">
          <div class="icon-btn" @click="handleToggleFavorite">
            <el-icon :size="22" :color="isFavorite ? '#FF6B6B' : '#999'">
              <StarFilled v-if="isFavorite" /><Star v-else />
            </el-icon>
            <span>{{ isFavorite ? '已收藏' : '收藏' }}</span>
          </div>
          <div class="icon-btn" @click="handleShare">
            <el-icon :size="22" color="#999"><Share /></el-icon>
            <span>分享</span>
          </div>
        </div>
        <button
          class="register-big-btn"
          :class="{ disabled: !canRegister, registered: hasRegistered }"
          :disabled="!canRegister && !(!isLoggedIn)"
          @click="handleRegister()"
        >
          {{ registerBtnText }}
        </button>
      </div>
    </template>

    <!-- ==================== 电脑端 ==================== -->
    <template v-else-if="activity.id">
      <div class="pc-container">
        <!-- 页面后退导航 -->
        <el-page-header @back="router.back()" class="pc-page-header">
          <template #content>
            <span class="page-title">返回</span>
          </template>
        </el-page-header>

        <!-- 沉浸式 Hero Banner -->
        <div class="pc-hero-banner">
          <el-image :src="getImageUrl(activity.coverImage || activity.image, defaultCover)" fit="cover" class="pc-hero-bg">
            <template #error>
              <div class="hero-fallback"><el-icon :size="60"><Picture /></el-icon></div>
            </template>
          </el-image>
          <div class="pc-hero-overlay"></div>
          <div class="pc-hero-content">
            <el-tag v-if="activity.isFeatured" type="warning" effect="dark" class="hero-badge">官方认证</el-tag>
            <el-tag v-else effect="dark" class="hero-badge" color="#FF9800" style="border:none;">限时报名</el-tag>
            
            <h1 class="hero-title">{{ activity.title }}</h1>
            
            <div class="hero-meta">
              <div class="meta-item"><el-icon><Clock /></el-icon> {{ formatDate(activity.startTime) }} - {{ formatDate(activity.endTime) }}</div>
              <div class="meta-item"><el-icon><Location /></el-icon> {{ activity.location || '待定' }}</div>
              <div class="meta-item"><el-icon><OfficeBuilding /></el-icon> {{ activity.organizerName || '未知组织' }}</div>
            </div>
          </div>
        </div>

        <div class="pc-content-wrapper">
          <el-row :gutter="30">
            <!-- 左侧主内容 (70%) -->
            <el-col :span="16">
              <div class="pc-left-column">
                
                <!-- 简介 -->
                <div class="pc-section" v-if="activity.summary">
                  <h3><el-icon><InfoFilled /></el-icon> 活动简介</h3>
                  <div class="section-card">
                    <p>{{ activity.summary }}</p>
                  </div>
                </div>

                <!-- 详情 -->
                <div class="pc-section">
                  <h3><el-icon><Document /></el-icon> 活动详情</h3>
                  <div class="section-card rich-content" v-html="activity.content || '暂无详情'"></div>
                </div>

                <!-- 参与要求 -->
                <div class="pc-section" v-if="activity.requirements">
                  <h3><el-icon><Warning /></el-icon> 参与要求</h3>
                  <div class="section-card">
                    <p>{{ activity.requirements }}</p>
                  </div>
                </div>

                <!-- 评论区 -->
                <CommentSection class="pc-comment-section" targetType="ACTIVITY" :targetId="Number(route.params.id)" />
              </div>
            </el-col>

            <!-- 右侧边栏 (30%) -->
            <el-col :span="8">
              <div class="pc-right-sidebar sticky-sidebar">
                
                <!-- 报名卡片 -->
                <div class="registration-card">
                  <div class="reg-card-header">
                    <span class="reg-price">志愿招募</span>
                    <el-tag :type="getStatusType(activity.status)" effect="dark" round>{{ activity.statusName }}</el-tag>
                  </div>

                  <!-- 进度可视化 -->
                  <div class="progress-visualization">
                    <div class="progress-stats">
                      <div class="stat-box primary">
                        <span class="stat-num">{{ activity.currentParticipants || 0 }}</span>
                        <span class="stat-label">已报名</span>
                      </div>
                      <div class="stat-divider">/</div>
                      <div class="stat-box warning">
                        <span class="stat-num">{{ activity.maxParticipants || '不限' }}</span>
                        <span class="stat-label">招募集结</span>
                      </div>
                    </div>
                    
                    <div class="gradient-progress-wrap">
                      <el-progress
                        :percentage="getProgress(activity.currentParticipants, activity.maxParticipants)"
                        :stroke-width="12"
                        :show-text="false"
                        class="gradient-progress"
                      />
                    </div>
                    
                    <div class="deadline-countdown" v-if="activity.deadline || activity.registerEnd">
                      <el-icon><Timer /></el-icon> 报名截止: {{ formatDeadline(activity.deadline || activity.registerEnd) }}
                    </div>
                  </div>

                  <!-- 头像墙 -->
                  <div class="avatar-wall-container" v-if="mockParticipants.length">
                    <div class="avatar-wall-title">已有 {{ activity.currentParticipants || 0 }} 人报名参与</div>
                    <div class="avatar-scroll-wrapper">
                      <div class="avatar-item" v-for="(p, idx) in mockParticipants.slice(0, 15)" :key="idx">
                        <el-avatar :size="40" :style="{ background: p.color }">{{ p.name.charAt(0) }}</el-avatar>
                        <span class="avatar-name">{{ p.name }}</span>
                      </div>
                    </div>
                  </div>

                  <!-- 奖励信息 -->
                  <div class="rewards-dashboard">
                    <div class="reward-box">
                      <el-icon class="icon-pulse" color="#00b4b6"><Timer /></el-icon>
                      <div class="r-text">{{ activity.serviceHours || 0 }}h 志愿时长</div>
                    </div>
                    <div class="reward-box">
                      <el-icon class="icon-pulse" color="#FF9800"><Medal /></el-icon>
                      <div class="r-text">{{ activity.points || activity.pointsReward || 0 }} 积分奖励</div>
                    </div>
                  </div>

                  <!-- 免审核卡 -->
                  <div class="skip-card-box" v-if="isLoggedIn && canRegister">
                    <el-checkbox v-model="useSkipCard" :disabled="skipCardCount === 0">
                      使用免审核卡
                      <el-tag :type="skipCardCount > 0 ? 'success' : 'info'" size="small">剩余 {{ skipCardCount }} 张</el-tag>
                    </el-checkbox>
                    <p class="skip-tip" v-if="skipCardCount > 0">💡 使用后可跳过审核直接报名成功</p>
                    <p class="skip-tip" v-else>💡 <el-link type="primary" @click="router.push('/mall/index')">积分商城</el-link> 兑换</p>
                  </div>

                  <!-- 操作按钮组 -->
                  <div class="interactive-actions">
                    <el-button
                      type="primary"
                      class="btn-gradient-primary btn-hover-grow"
                      :loading="registering"
                      :disabled="!canRegister"
                      @click="handleRegister()"
                    >
                      {{ useSkipCard ? '🎫 免审核立即报名' : registerBtnText }}
                    </el-button>
                    
                    <el-button
                      v-if="isLoggedIn"
                      class="btn-favorite btn-hover-grow"
                      :class="{ 'is-active': isFavorite }"
                      :loading="favoriting"
                      @click="handleToggleFavorite"
                    >
                      <el-icon><StarFilled v-if="isFavorite" /><Star v-else /></el-icon>
                      {{ isFavorite ? '已收藏' : '收藏活动' }}
                    </el-button>
                  </div>
                </div>

                <!-- 推荐活动模块 -->
                <div class="recommend-sidebar" v-if="recommendedActivities.length">
                  <div class="sidebar-title">
                    <el-icon><StarFilled /></el-icon> 推荐活动
                  </div>
                  <div class="recommend-list">
                    <div 
                      class="recommend-item" 
                      v-for="item in recommendedActivities.slice(0, 3)" 
                      :key="item.id"
                      @click="router.push(`/activity/${item.id}`)"
                    >
                      <el-image :src="item.coverImage || defaultCover" fit="cover" class="rec-img"></el-image>
                      <div class="rec-info">
                        <div class="rec-title">{{ item.title }}</div>
                        <div class="rec-bottom">
                          <span class="rec-time">{{ formatDate(item.startTime).split(' ')[0] }}</span>
                          <span class="rec-spots">热招中</span>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div class="more-activities-wrapper">
                    <button class="btn-more-activities-premium" @click="router.push('/activity')">
                      <span>浏览更多活动</span>
                      <el-icon><Right /></el-icon>
                    </button>
                  </div>
                </div>

              </div>
            </el-col>
          </el-row>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft, Star, StarFilled, Share, Picture, Clock, Location,
  OfficeBuilding, Phone, Timer, Medal,
  InfoFilled, Document, Warning, Right
} from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import dayjs from 'dayjs'
import { getImageUrl } from '@/utils/image'
import CommentSection from '@/components/CommentSection.vue'

const router = useRouter()
const route = useRoute()

// ================== 响应式 ==================
const windowWidth = ref(window.innerWidth)
const isMobile = computed(() => windowWidth.value < 768)
onMounted(() => window.addEventListener('resize', () => { windowWidth.value = window.innerWidth }))

// ================== 数据 ==================
const defaultCover = '/default-cover.jpg'
const loading = ref(false)
const registering = ref(false)
const favoriting = ref(false)
const activity = ref<any>({})
const hasRegistered = ref(false)
const isFavorite = ref(false)
const useSkipCard = ref(false)
const skipCardCount = ref(0)
const scrollRef = ref<HTMLElement | null>(null)
const headerScrolled = ref(false)
const currentUserVolunteerId = ref<number | null>(null)

const progressColors = [
  { color: '#00BFA6', percentage: 70 },
  { color: '#e6a23c', percentage: 90 },
  { color: '#f56c6c', percentage: 100 }
]

const isLoggedIn = computed(() => !!localStorage.getItem('token'))

const isVolunteer = computed(() => {
  if (!isLoggedIn.value) return false
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo) {
    try {
      const parsed = JSON.parse(userInfo)
      return parsed.role === 'VOLUNTEER'
    } catch (e) { return false }
  }
  return false
})

const canRegister = computed(() => {
  if (hasRegistered.value) return false
  if (activity.value.status !== 2 && activity.value.status !== 3) return false
  
  const deadline = activity.value.registerEnd || activity.value.deadline
  if (deadline && new Date(deadline) < new Date()) return false
  
  if (activity.value.maxParticipants > 0 &&
      activity.value.currentParticipants >= activity.value.maxParticipants) return false

  // 如果未登录，返回 true 让按钮可见并可用（显示“登录并报名”）
  if (!isLoggedIn.value) return true
  
  // 已登录则检查身份
  if (!isVolunteer.value) return false
  
  return true
})

const isDeadlinePassed = computed(() => {
  const deadline = activity.value.registerEnd || activity.value.deadline
  return deadline && new Date(deadline) < new Date()
})

/*
const registerBtnType = computed(() => {
  if (hasRegistered.value) return 'success'
  if (!canRegister.value) return 'info'
  return 'primary'
})
*/

const registerBtnText = computed(() => {
  if (!isLoggedIn.value) return '登录并报名'
  if (!isVolunteer.value) return '仅志愿账号可报名'
  if (hasRegistered.value) return '已报名'
  if (activity.value.status === 3) return '活动进行中'
  if (activity.value.status === 4) return '活动已结束'
  if (activity.value.status === 5) return '活动已取消'
  if (isDeadlinePassed.value) return '报名已截止'
  if (activity.value.maxParticipants > 0 &&
      activity.value.currentParticipants >= activity.value.maxParticipants) return '名额已满'
  return '立即报名'
})

// ================== 滚动监听 ==================
const onScroll = () => {
  if (!scrollRef.value) return
  headerScrolled.value = scrollRef.value.scrollTop > 200
}

// ================== 工具函数 ==================
const formatDate = (date: string) => date ? dayjs(date).format('MM-DD HH:mm') : ''
const formatDeadline = (date: string) => date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '不限'
const getStatusType = (status: number) => {
  const types: Record<number, string> = { 0: 'info', 1: 'warning', 2: 'success', 3: 'primary', 4: 'info', 5: 'danger' }
  return types[status] || 'info'
}
const getProgress = (current: number, max: number) => {
  if (!max || max <= 0) return 0
  return Math.min(Math.round((current / max) * 100), 100)
}

// ================== API ==================
const fetchActivity = async () => {
  loading.value = true
  try {
    const res = await request.get(`/activity/${route.params.id}`)
    activity.value = res.data || {}
  } catch (e) {
    ElMessage.error('获取活动详情失败')
  } finally { loading.value = false }
}

const checkRegistration = async () => {
  if (!isLoggedIn.value) return
  try {
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      const parsed = JSON.parse(userInfo)
      if (parsed.role && parsed.role !== 'VOLUNTEER') return
    }
    const res = await request.get(`/registration/check/${route.params.id}`)
    hasRegistered.value = res.data || false
  } catch (e) { console.error('检查报名状态失败', e) }
}

const checkFavorite = async () => {
  if (!isLoggedIn.value) return
  try {
    const res = await request.get('/collection/check', { targetId: route.params.id, targetType: 'ACTIVITY' })
    isFavorite.value = res.data === true
  } catch (e) { console.error('检查收藏状态失败', e) }
}

const fetchCurrentUserVolunteerInfo = async () => {
  if (!isLoggedIn.value) return
  try {
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      const parsed = JSON.parse(userInfo)
      if (parsed.role && parsed.role !== 'VOLUNTEER') return
    }
    const res = await request.get('/volunteer/profile')
    if (res.code === 200 && res.data) {
      currentUserVolunteerId.value = res.data.id
    }
  } catch (e) { console.debug('获取当前志愿者信息失败') }
}

const fetchSkipCardCount = async () => {
  if (!isLoggedIn.value) return
  try {
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      const parsed = JSON.parse(userInfo)
      if (parsed.role && parsed.role !== 'VOLUNTEER') return
    }
  } catch (e) { /* */ }
  try {
    const res = await request.get('/mall/backpack')
    if (res.code === 200 && res.data) {
      const card = res.data.find((item: any) =>
        item.propName?.includes('免审核') || item.propType === 'SKIP_REVIEW_CARD'
      )
      skipCardCount.value = card?.quantity || 0
    }
  } catch (e) { console.debug('获取免审核卡数量失败:', e) }
}

// ================== 事件处理 ==================
const handleRegister = async (isAuto = false) => {
  if (!isLoggedIn.value) {
    const redirectUrl = `${route.fullPath}${route.fullPath.includes('?') ? '&' : '?'}autoEnroll=true`
    router.push({ path: '/login', query: { redirect: redirectUrl } })
    return
  }
  if (!canRegister.value) return

  const doRegister = async () => {
    registering.value = true
    try {
      const url = useSkipCard.value ? '/registration?useSkipCard=true' : '/registration'
      await request.post(url, { activityId: route.params.id })
      ElMessage.success(useSkipCard.value ? '免审核报名成功！' : '报名成功！请等待审核')
      hasRegistered.value = true
      if (useSkipCard.value) skipCardCount.value = Math.max(0, skipCardCount.value - 1)
      useSkipCard.value = false
      fetchActivity()
      
      // 清理 autoEnroll 标记
      if (route.query.autoEnroll) {
        router.replace({ path: route.path, query: { ...route.query, autoEnroll: undefined } })
      }
    } catch (error: any) {
      ElMessage.error(error.message || '报名失败')
    } finally { registering.value = false }
  }

  if (isAuto) {
    doRegister()
  } else {
    ElMessageBox.confirm(
      `确定要报名参加「${activity.value.title}」吗？`,
      '确认报名',
      { confirmButtonText: '确定报名', cancelButtonText: '再想想', type: 'info' }
    ).then(doRegister).catch(() => {})
  }
}

const handleToggleFavorite = async () => {
  if (!isLoggedIn.value) {
    ElMessage.info('请先登录后再进行收藏')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  favoriting.value = true
  try {
    await request.post('/collection/toggle', { targetId: Number(route.params.id), targetType: 'ACTIVITY' })
    isFavorite.value = !isFavorite.value
    ElMessage.success(isFavorite.value ? '收藏成功' : '已取消收藏')
  } catch (e) { console.error('操作收藏失败:', e) }
  finally { favoriting.value = false }
}

const handleShare = () => {
  if (navigator.share) {
    navigator.share({ title: activity.value.title, url: window.location.href })
  } else {
    navigator.clipboard?.writeText(window.location.href)
    ElMessage.success('链接已复制')
  }
}

const mockParticipants = computed(() => {
  const names = ['张三', '李四', '王五', '赵六', '孙七', '钱八']
  const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#00BFA6']
  const count = Math.min(activity.value.currentParticipants || 5, names.length)
  return names.slice(0, count).map((name, i) => ({ name, color: colors[i] }))
})


const recommendedActivities = ref<any[]>([])

const fetchRecommendations = async () => {
  try {
    const res = await request.get(`/recommendation/detail/${route.params.id}`)
    if (res.code === 200) {
      recommendedActivities.value = res.data || []
    }
  } catch (e) {
    console.debug('获取关联推荐失败')
  }
}

onMounted(() => {
  fetchActivity()
  fetchRecommendations()
  
  // 仅在已登录状态下调用权限相关的接口，避免游客访问时触发 401 拦截
  if (isLoggedIn.value) {
    checkRegistration()
    checkFavorite()
    fetchSkipCardCount()
    fetchCurrentUserVolunteerInfo()
    
    // 自动触发报名（如果是从登录回跳带标记的）
    if (route.query.autoEnroll === 'true') {
      setTimeout(() => {
        if (canRegister.value) handleRegister(true)
      }, 500) // 稍微延迟确保数据加载
    }
  }
})
</script>

<style lang="scss" scoped>
// ================================================================
// 移动端 - 沉浸式 (iOS Frosted Glass)
// ================================================================
.activity-detail-page.is-mobile {
  position: relative;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #E0F7FA 0%, #E1F5FE 50%, #F3E5F5 100%);
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

// --- 沉浸式顶部 ---
.immersive-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: var(--nav-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 12px;
  z-index: 200;
  transition: all 0.3s;
  background: transparent;

  &.scrolled {
    background: rgba(255, 255, 255, 0.4);
    backdrop-filter: blur(25px);
    -webkit-backdrop-filter: blur(25px);
    border-bottom: 0.5px solid rgba(255, 255, 255, 0.5);

    .header-back, .header-share { 
      background: transparent; 
      color: #333; 
    }
    .header-title { opacity: 1; }
  }

  .header-back, .header-share {
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    background: rgba(0, 0, 0, 0.3);
    color: #fff;
    font-size: 18px;
    cursor: pointer;
    backdrop-filter: blur(4px);
    transition: all 0.3s;
  }

  .header-title {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    opacity: 0;
    transition: opacity 0.3s;
    max-width: 60%;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

// --- 滚动区 ---
.immersive-body {
  flex: 1;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

// --- Hero 大图 ---
.hero-image {
  position: relative;
  height: 300px;
  overflow: hidden;

  .hero-img {
    width: 100%;
    height: 100%;
  }

  // 渐变蒙层 - 增强沉浸感
  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(
      to bottom,
      transparent 40%,
      rgba(0, 0, 0, 0.3) 100%
    );
    pointer-events: none;
  }

  .hero-status {
    position: absolute;
    top: 60px;
    right: 12px;
    z-index: 2;
  }

  .hero-fallback {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #E8E8E8, #D5D5D5);
    color: #bbb;
  }
}



// --- 内容卡片 ---
.content-card {
  position: relative;
  margin-top: -20px;
  margin-left: 12px;
  margin-right: 12px;
  background: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.07);
  border-radius: 16px 16px 0 0;
  padding: 24px 16px;
  min-height: 50vh;
  animation: slideUp 0.4s ease-out;

  .detail-title {
    font-size: 20px;
    font-weight: 700;
    color: #1C1C1E;
    margin: 0 0 10px;
    line-height: 1.4;
  }

  .tag-row {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
    margin-bottom: 16px;
  }
}

// --- 信息网格 ---
.info-grid {
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 12px;
  padding: 14px;
  margin-bottom: 20px;
}

.info-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 8px 0;

  &:not(:last-child) {
    border-bottom: 0.5px solid rgba(255, 255, 255, 0.6);
  }

  .el-icon { margin-top: 2px; font-size: 18px; flex-shrink: 0; }
  .info-main { font-size: 14px; color: #333; font-weight: 500; }
  .info-sub { font-size: 12px; color: #999; margin-top: 2px; }
}

// --- 报名进度 ---
.register-progress-section {
  margin-bottom: 16px;

  .progress-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;

    .progress-label { font-size: 13px; color: #999; }
    .progress-nums { font-size: 13px; color: #666; strong { color: var(--primary-color); font-size: 16px; } }
  }
}

// --- 奖励栏 ---
.rewards-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.reward-chip {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 12px;
  font-size: 13px;
  font-weight: 500;
  color: #333;
}

.deadline-tip {
  padding: 10px 14px;
  background: rgba(255, 248, 225, 0.5);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 12px;
  font-size: 13px;
  color: #E6A23C;
  margin-bottom: 24px;
}

// --- 内容区块 ---
.section-block {
  margin-bottom: 24px;
  background: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(15px);
  -webkit-backdrop-filter: blur(15px);
  border-radius: 16px;
  padding: 20px;
  border: 1px solid rgba(255, 255, 255, 0.5);

  .section-heading {
    font-size: 16px;
    font-weight: 600;
    color: #1c1c1e;
    margin: 0 0 12px;
    padding-bottom: 8px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.6);
  }

  .section-text {
    font-size: 14px;
    line-height: 1.8;
    color: #666;
    margin: 0;
  }
}

.rich-content {
  font-size: 14px;
  line-height: 1.8;
  color: #333;
  word-break: break-word;

  :deep(img) {
    max-width: 100%;
    height: auto;
    border-radius: 8px;
    margin: 8px 0;
  }

  :deep(table) { max-width: 100%; overflow-x: auto; display: block; }
}

.bottom-spacer { height: 80px; }

// --- 底部固定栏 ---
.mobile-bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  padding: 8px 16px;
  padding-bottom: calc(8px + var(--safe-area-bottom));
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(25px);
  -webkit-backdrop-filter: blur(25px);
  border-top: 0.5px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 -8px 32px rgba(31, 38, 135, 0.05);
  z-index: 200;
  gap: 16px;

  .bottom-icons {
    display: flex;
    gap: 16px;
  }

  .icon-btn {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 2px;
    cursor: pointer;
    -webkit-tap-highlight-color: transparent;

    span { font-size: 10px; color: #999; }
  }

  .register-big-btn {
    flex: 1;
    height: 48px;
    border: 1px solid rgba(255, 255, 255, 0.5);
    border-radius: 24px;
    font-size: 16px;
    font-weight: 600;
    color: #fff;
    background: linear-gradient(135deg, rgba(0, 191, 166, 0.9), rgba(0, 150, 136, 0.9));
    box-shadow: inset 0 1px 1px rgba(255, 255, 255, 0.4), 0 4px 12px rgba(0, 191, 166, 0.3);
    cursor: pointer;
    transition: all 0.2s;
    letter-spacing: 1px;

    &:active { transform: scale(0.97); }
    &.disabled { background: #ccc; cursor: not-allowed; }
    &.registered { background: linear-gradient(135deg, #67C23A, #4CAF50); }

    // 报名按钮脉动动画
    &:not(.disabled):not(.registered) {
      animation: btnPulse 2.5s ease-in-out infinite;
    }
  }
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes btnPulse {
  0%, 100% { box-shadow: 0 4px 16px rgba(0, 191, 166, 0.3); }
  50% { box-shadow: 0 4px 24px rgba(0, 191, 166, 0.6); }
}


// ================================================================
// 电脑端
// ================================================================
.page-title {
  font-size: 18px;
  font-weight: 600;
}

.pc-main-card {
  border-radius: var(--radius-md);
  margin-bottom: 20px;
  transition: box-shadow 0.3s;

  &:hover { box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08) !important; }

  :deep(.el-card__body) { padding: 0; }

  .pc-cover {
    position: relative;
    .pc-cover-img { width: 100%; height: 320px; border-radius: var(--radius-md) var(--radius-md) 0 0; }

    // 封面渐变遮罩
    &::after {
      content: '';
      position: absolute;
      inset: 0;
      background: linear-gradient(to bottom, transparent 60%, rgba(0, 0, 0, 0.2) 100%);
      border-radius: var(--radius-md) var(--radius-md) 0 0;
      pointer-events: none;
    }
  }

  .pc-title-section {
    padding: 20px 24px 0;

    h1 { font-size: 24px; font-weight: 700; margin: 0 0 12px; color: #333; }
    .tag-row { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 20px; }
  }

  .pc-section {
    padding: 0 24px 24px;

    h3 {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: 600;
      margin: 0 0 12px;
      padding-bottom: 8px;
      border-bottom: 2px solid var(--primary-color);
      color: #333;
    }

    p { color: #666; line-height: 1.8; margin: 0; }
  }
}

.pc-sidebar { position: sticky; top: 80px; }

.pc-info-card {
  border-radius: var(--radius-md);
  margin-bottom: 16px;

  :deep(.el-card__header) {
    padding: 14px 20px;
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
    border-bottom: 1px dashed rgba(0, 0, 0, 0.06);

    &:last-child { border-bottom: none; }
    .label { color: #999; font-size: 13px; }
    .value { color: #333; font-weight: 500; font-size: 13px; }
  }
}

.register-stats {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 30px;
  margin-bottom: 16px;

  .stat-item { text-align: center; }
  .stat-value { font-size: 28px; font-weight: 700; color: var(--primary-color); }
  .stat-label { font-size: 12px; color: #999; margin-top: 4px; }
  .stat-divider { width: 1px; height: 40px; background: rgba(0, 0, 0, 0.06); }
}

.skip-card-box {
  background: #F8F8FA;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 16px;

  .skip-tip { margin: 8px 0 0; font-size: 12px; color: #999; padding-left: 24px; }
}

.action-buttons {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;

  .register-btn { flex: 1; height: 48px; font-size: 16px; }
  .favorite-btn {
    height: 48px;
    min-width: 100px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 4px;
  }
}

.register-tip { text-align: center; font-size: 13px; color: #999; margin: 0; }

// ================================================================
// 参与者头像墙
// ================================================================
.avatar-wall {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;

  :deep(.el-avatar) {
    border: 2px solid #fff;
    font-size: 13px;
    font-weight: 600;
    cursor: default;
    transition: transform 0.2s;

    &:hover { transform: scale(1.1); z-index: 2; }
  }

  .avatar-more {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;
    border-radius: 50%;
    background: #F0F2F5;
    font-size: 11px;
    font-weight: 600;
    color: #666;
  }

  &.pc {
    :deep(.el-avatar) { border-width: 3px; }
    .avatar-more { width: 40px; height: 40px; }
  }
}

// 评论区样式由 CommentSection.vue 维护

// --- 评价输入框 ---
.comment-input-box {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 12px;
  margin-bottom: 20px;

  &.pc {
    margin: 0 0 24px;
  }

  :deep(.el-textarea__inner) {
    border: none;
    background: transparent;
    padding: 0;
    box-shadow: none !important;
    resize: none;
    font-size: 14px;
  }

  .comment-actions {
    display: flex;
    justify-content: flex-end;
    margin-top: 10px;
    padding-top: 10px;
    border-top: 1px solid rgba(0, 0, 0, 0.05);
  }
}

// --- 评价项操作 ---
.comment-item-actions {
  display: flex;
  gap: 20px;
  margin-top: 8px;

  .action-item {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: #999;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      color: var(--primary-color);
    }

    &.like {
      &.active { color: #FF4D4F; font-weight: 600; }
      &:hover { color: #FF4D4F; }
    }

    &.delete {
      &:hover { color: #FF4D4F; }
    }

    .el-icon {
      font-size: 14px;
    }
  }
}

// --- 展开按钮 ---
.comment-expand-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 12px;
  color: var(--primary-color);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  background: rgba(0, 191, 166, 0.04);
  margin-top: 10px;
  border-radius: 8px;
  transition: all 0.2s;

  &:hover {
    background: rgba(0, 191, 166, 0.08);
  }
}

.comment-skeleton {
  .skeleton-comment:not(:last-child) {
    border-bottom: 1px solid rgba(0, 0, 0, 0.04);
  }
}

// ================================================================
// 推荐活动 (响应式布局组件式样式)
// ================================================================
.recommend-section {
  margin-top: 24px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(15px);
  -webkit-backdrop-filter: blur(15px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.5);

  &.pc-recommend {
    padding: 0;
    background: transparent;
    backdrop-filter: none;
    border: none;
    margin-top: 40px;
    margin-bottom: 20px;
  }
}

.section-header {
  font-size: 16px;
  font-weight: 700;
  color: #333;
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 16px;
  .el-icon { color: #F59E0B; font-size: 18px; }
}

.recommend-responsive-grid {
  display: grid;
  gap: 12px;

  @media (max-width: 767px) {
    grid-template-columns: repeat(2, 1fr);
  }

  @media (min-width: 768px) {
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
  }
}

.grid-card {
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(31,38,135,0.05);
  display: flex;
  flex-direction: column;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;

  &:active { transform: scale(0.98); }
  
  @media (min-width: 768px) {
    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 12px 24px rgba(0,0,0,0.08);
    }
  }
}

.card-cover {
  width: 100%;
  aspect-ratio: 16/10;
  position: relative;
  background: #f0f0f0;
  overflow: hidden;

  .el-image {
    width: 100%;
    height: 100%;
    transition: transform 0.5s;
  }

  &:hover .el-image { transform: scale(1.05); }
}

.rec-cover-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--primary-color), #00d2ff);
  color: #fff;
  font-size: 20px;
  font-weight: 700;
}

.status-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  font-size: 10px;
  padding: 2px 8px;
  border-radius: 4px;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  backdrop-filter: blur(4px);
  z-index: 2;
  font-weight: 600;
}

.card-info {
  padding: 10px 12px;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.card-title {
  font-size: 13px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  line-clamp: 1;
  -webkit-box-orient: vertical;
}

.card-time {
  font-size: 11px;
  color: #94a3b8;
}
// ================================================================
// PC 推荐活动网格
// ================================================================
.pc-recommend {
  margin-top: 20px;

  h3 {
    font-size: 18px;
    font-weight: 600;
    margin: 0 0 14px;
    color: #333;
  }
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.rec-cover {
  height: 90px;
  width: 100%;
}

.rec-cover-fallback {
  height: 90px;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--primary-color), #00d2ff);
  color: #fff;
  font-size: 24px;
  font-weight: 700;
}

.rec-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 4px;

  .cat {
    font-size: 10px;
    background: #f0f2f5;
    color: #666;
    padding: 1px 4px;
    border-radius: 2px;
  }
  
  .time { font-size: 10px; color: #999; }
}

.algo-badge-mini {
  position: absolute;
  top: 6px;
  left: 6px;
  z-index: 5;
  background: rgba(0, 191, 166, 0.9);
  color: #fff;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 600;
}

.algo-tag-float {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 5;
  
  .tag-content {
    background: rgba(0, 0, 0, 0.6);
    backdrop-filter: blur(4px);
    color: #fff;
    padding: 2px 8px;
    border-radius: 20px;
    font-size: 10px;
  }
}

// ================================================================
// PC 端沉浸式 UI 升级 (Activity Detail PC Redesign)
// ================================================================
.pc-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  position: relative;
}

.pc-page-header {
  margin-bottom: 20px;
  :deep(.el-page-header__content) {
    color: #1e293b;
    font-weight: 600;
  }
}

/* 沉浸式 Hero Banner */
.pc-hero-banner {
  position: relative;
  width: 100%;
  height: 380px;
  border-radius: 20px;
  overflow: hidden;
  margin-bottom: 30px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);

  .pc-hero-bg {
    width: 100%;
    height: 100%;
    transition: transform 0.8s ease;
  }
  
  &:hover .pc-hero-bg {
    transform: scale(1.05); /* 微动效：背景缓慢悬浮放大 */
  }

  .pc-hero-overlay {
    position: absolute;
    top: 0; left: 0; right: 0; bottom: 0;
    /* 半透明深色遮罩 */
    background: linear-gradient(to bottom, rgba(0,0,0,0.2) 0%, rgba(0,0,0,0.8) 100%);
    z-index: 1;
  }

  .pc-hero-content {
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    padding: 40px;
    z-index: 2;
    color: #fff;

    .hero-badge {
      position: absolute;
      top: 40px;
      right: 40px;
      font-size: 14px;
      padding: 6px 12px;
      border-radius: 8px;
    }

    .hero-title {
      font-size: 36px;
      font-weight: 800;
      margin: 0 0 16px;
      text-shadow: 0 2px 8px rgba(0,0,0,0.5);
      line-height: 1.3;
      font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
    }

    .hero-meta {
      display: flex;
      gap: 24px;
      font-size: 16px;
      font-weight: 500;
      text-shadow: 0 1px 4px rgba(0,0,0,0.6);

      .meta-item {
        display: flex;
        align-items: center;
        gap: 6px;
        opacity: 0.9;
        
        .el-icon {
          font-size: 18px;
          color: #00b4b6; /* 主题极光青点缀 */
        }
      }
    }
  }
}

/* 70/30 布局细节 */
.pc-content-wrapper {
  margin-top: 20px;
}

.pc-left-column {
  margin-bottom: 40px;
  background: transparent;
  
  .pc-section {
    background: #fff;
    border-radius: 16px;
    padding: 30px;
    margin-bottom: 24px;
    box-shadow: 0 2px 12px rgba(0,0,0,0.03);
    
    h3 {
      font-size: 20px;
      font-weight: 700;
      color: #1e293b;
      margin: 0 0 20px;
      display: flex;
      align-items: center;
      gap: 8px;
      
      .el-icon { color: #00b4b6; font-size: 24px; }
    }
    
    .section-card {
      font-size: 15px;
      line-height: 1.8;
      color: #475569;
      
      p { margin: 0; }
    }
  }
  
  .pc-comment-section {
    background: #fff;
    border-radius: 16px;
    padding: 30px;
    margin-bottom: 24px;
    box-shadow: 0 2px 12px rgba(0,0,0,0.03);
  }
}

/* 右侧吸顶卡片 */
.sticky-sidebar {
  position: sticky;
  top: 80px;
  z-index: 10;
}

.registration-card {
  background: #fff;
  border-radius: 20px;
  padding: 30px;
  box-shadow: 0 8px 30px rgba(0,0,0,0.05);
  margin-bottom: 24px;
  border: 1px solid rgba(0, 180, 182, 0.1);
  
  .reg-card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    .reg-price {
      font-size: 28px;
      font-weight: 800;
      color: #00C853;
      letter-spacing: -1px;
    }
  }

  .progress-visualization {
    margin-bottom: 30px;
    background: #f8f9fa;
    border-radius: 12px;
    padding: 20px;
    
    .progress-stats {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      
      .stat-box {
        text-align: center;
        flex: 1;
        
        .stat-num {
          display: block;
          font-size: 24px;
          font-weight: 700;
          margin-bottom: 4px;
        }
        .stat-label {
          font-size: 13px;
          color: #94a3b8;
        }
        
        &.primary .stat-num { color: #00b4b6; }
        &.warning .stat-num { color: #FF9800; }
      }
      
      .stat-divider {
        color: #e2e8f0;
        font-size: 24px;
        font-weight: 300;
      }
    }
    
    .gradient-progress-wrap {
      margin-bottom: 12px;
      
      :deep(.el-progress-bar__inner) {
        background: linear-gradient(90deg, #00b4b6 0%, #00C853 100%) !important;
        transition: width 1s ease-in-out;
      }
    }
    
    .deadline-countdown {
      text-align: center;
      font-size: 12px;
      color: #f56c6c;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 4px;
      font-weight: 600;
      background: rgba(245, 108, 108, 0.1);
      border-radius: 12px;
      padding: 6px 0;
    }
  }

  .avatar-wall-container {
    margin-bottom: 24px;
    
    .avatar-wall-title {
      font-size: 14px;
      font-weight: 600;
      color: #1e293b;
      margin-bottom: 12px;
    }
    
    .avatar-scroll-wrapper {
      display: flex;
      gap: 12px;
      overflow-x: auto;
      padding-bottom: 10px;
      
      &::-webkit-scrollbar { height: 4px; }
      &::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 2px; }
      
      .avatar-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 6px;
        flex-shrink: 0;
        
        .el-avatar { border: 2px solid #fff; box-shadow: 0 2px 6px rgba(0,0,0,0.1); }
        .avatar-name { font-size: 11px; color: #64748b; max-width: 50px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
      }
    }
  }
}

/* 交互按钮细节 */
.interactive-actions {
  display: flex;
  flex-direction: column;
  gap: 16px;
  
  .btn-hover-grow {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    height: 48px;
    font-size: 16px;
    border-radius: 12px;
    font-weight: 600;
    
    &:hover:not(:disabled) {
      transform: translateY(-2px);
    }
    &:active:not(:disabled) {
      transform: translateY(1px);
    }
  }
  
  .btn-gradient-primary {
    background: linear-gradient(135deg, #00b4b6 0%, #00C853 100%) !important;
    border: none !important;
    color: white !important;
    box-shadow: 0 6px 16px rgba(0, 180, 182, 0.3);
    
    &:hover:not(:disabled) {
      box-shadow: 0 8px 20px rgba(0, 180, 182, 0.4);
    }
    &.is-disabled {
      background: #e2e8f0 !important;
      color: #94a3b8 !important;
      box-shadow: none !important;
      transform: none !important;
    }
  }
  
  .btn-favorite {
    background: #f8f9fa;
    border: 1px solid #e2e8f0;
    color: #64748b;
    margin-left: 0 !important;
    
    &:hover {
      border-color: #cbd5e1;
      color: #1e293b;
    }
    
    &.is-active {
      background: rgba(255, 107, 107, 0.1);
      border-color: #FF6B6B;
      color: #FF6B6B;
    }
  }
}

.rewards-dashboard {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  
  .reward-box {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px;
    background: rgba(0, 180, 182, 0.04);
    border-radius: 10px;
    border: 1px dashed rgba(0, 180, 182, 0.2);
    
    .r-text { font-size: 13px; font-weight: 600; color: #334155; }
    .icon-pulse { animation: gently-pulse 2s infinite ease-in-out; }
  }
}

@keyframes gently-pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

.recommend-sidebar {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.03);
  
  .sidebar-title {
    font-size: 16px;
    font-weight: 700;
    color: #1e293b;
    margin-bottom: 20px;
    display: flex;
    align-items: center;
    gap: 8px;
    .el-icon { color: #FF9800; }
  }
  
  .recommend-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
    margin-bottom: 20px;
    
    .recommend-item {
      display: flex;
      gap: 12px;
      cursor: pointer;
      padding: 8px;
      border-radius: 12px;
      transition: background 0.3s;
      
      &:hover { background: #f8f9fa; }
      
      .rec-img {
        width: 80px;
        height: 60px;
        border-radius: 8px;
        flex-shrink: 0;
      }
      
      .rec-info {
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        
        .rec-title {
          font-size: 13px;
          font-weight: 600;
          color: #334155;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
          line-height: 1.4;
        }
        
        .rec-bottom {
          display: flex;
          justify-content: space-between;
          font-size: 11px;
          color: #94a3b8;
          
          .rec-spots { color: #00C853; font-weight: 500; }
        }
      }
    }
  }
  
  .more-activities-wrapper {
    margin-top: 20px;
    padding: 0 4px;
    
    .btn-more-activities-premium {
      width: 100%;
      height: 44px;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 10px;
      padding: 0 24px;
      background: linear-gradient(135deg, #00b4b6, #00d2d4);
      border: none;
      border-radius: 22px; /* 药丸 */
      color: #fff;
      font-size: 14px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
      box-shadow: 0 4px 12px rgba(0, 180, 182, 0.2);
      
      .el-icon {
        font-size: 16px;
        transition: transform 0.3s;
      }
      
      &:hover:not(:disabled) {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(0, 180, 182, 0.35);
        filter: brightness(1.05);
        
        .el-icon {
          transform: translateX(4px);
        }
      }
      
      &:active:not(:disabled) {
        transform: scale(0.96);
        box-shadow: 0 2px 8px rgba(0, 180, 182, 0.2);
      }
      
      &:disabled {
        opacity: 0.6;
        cursor: not-allowed;
      }
    }
  }
}
</style>

