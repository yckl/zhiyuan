<template>
  <div class="profile-page" v-loading="loading">
    <!-- ==================== 个人中心 (所有角色) ==================== -->
    <template v-if="userRole">
      <!-- ===== 顶部英雄区：头像 + 姓名 + 积分 ===== -->
      <div class="hero-section" ref="heroRef">
        <el-row :gutter="24" align="middle">
          <el-col :xs="24" :sm="8" :md="6">
            <!-- 头像上传（悬停轻glow） -->
            <div class="avatar-wrapper">
              <el-upload
                class="avatar-uploader"
                action="/api/file/upload/avatar"
                :headers="uploadHeaders"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
              >
                <el-avatar :size="140" :src="avatarWithTimestamp" class="hero-avatar">
                  <el-icon :size="56"><User /></el-icon>
                </el-avatar>
                <div class="avatar-overlay">
                  <el-icon><Camera /></el-icon>
                  <span>更换头像</span>
                </div>
              </el-upload>
            </div>
          </el-col>
          <el-col :xs="24" :sm="16" :md="18">
            <div class="hero-info">
              <div class="name-row">
                <h1 class="user-name">{{ formData.name || formData.username || '未设置' }}</h1>
                <el-tag :type="userRole === 'ADMIN' ? 'danger' : 'warning'" size="large" class="role-badge">
                  {{ getRoleName(userRole) }}
                </el-tag>
              </div>
              <div class="points-display" v-if="isVolunteer">
                <span class="points-label">累计积分</span>
                <span class="points-value">{{ profile.totalPoints || 0 }}</span>
              </div>
              <p class="bio-preview">{{ formData.bio || '这个人很懒，什么都没写~' }}</p>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- ===== 统计横排4小卡（仅志愿者） ===== -->
      <el-row :gutter="16" class="stats-row" v-if="isVolunteer" ref="statsRef">
        <el-col :xs="12" :sm="6">
          <div class="stat-card">
            <el-icon class="stat-icon" style="color: #409eff"><Timer /></el-icon>
            <div class="stat-content">
              <span class="stat-value blue">{{ profile.totalHours || 0 }}</span>
              <span class="stat-label">志愿时长(h)</span>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card">
            <el-icon class="stat-icon" style="color: #67c23a"><Calendar /></el-icon>
            <div class="stat-content">
              <span class="stat-value green">{{ profile.serviceCount || profile.activityCount || 0 }}</span>
              <span class="stat-label">服务次数</span>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card">
            <el-icon class="stat-icon" style="color: #e6a23c"><Trophy /></el-icon>
            <div class="stat-content">
              <span class="stat-value orange">{{ profile.totalPoints || 0 }}</span>
              <span class="stat-label">累计积分</span>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card">
            <el-icon class="stat-icon" style="color: #f56c6c"><Star /></el-icon>
            <div class="stat-content">
              <span class="stat-value red">Lv.{{ volunteerLevel }}</span>
              <span class="stat-label">志愿等级</span>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- ===== 主区两列布局 ===== -->
      <el-row :gutter="24" class="main-content" ref="mainRef">
        <!-- 右侧：大表单区（全宽） -->
        <el-col :span="24">
          <div class="glass-card form-section">
            <div class="form-header">
              <h3 class="section-title"><el-icon><EditPen /></el-icon> 基本资料</h3>
            </div>

            <el-form
              ref="formRef"
              :model="formData"
              :rules="rules"
              label-position="top"
              class="profile-form"
            >
              <!-- 输入框统一 + 布局优化这里：基本资料两列 -->
              <el-row :gutter="20">
                <el-col :xs="24" :sm="12">
                  <el-form-item label="用户名" class="unified-input">
                    <el-input v-model="formData.username" disabled placeholder="用户名" />
                  </el-form-item>
                </el-col>
                <el-col :xs="24" :sm="12" v-if="isVolunteer">
                  <el-form-item label="真实姓名" prop="name" class="unified-input">
                    <el-input v-model="formData.name" placeholder="请输入真实姓名" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="20">
                <el-col :xs="24" :sm="12">
                  <!-- 输入框统一 + 布局优化这里 -->
                  <el-form-item label="手机号" prop="phone" class="unified-input">
                    <el-input v-model="formData.phone" placeholder="请输入手机号" />
                  </el-form-item>
                </el-col>
                <el-col :xs="24" :sm="12">
                  <el-form-item label="邮箱" class="unified-input">
                    <el-input v-model="formData.email" placeholder="请输入邮箱" />
                  </el-form-item>
                </el-col>
              </el-row>

              <!-- 志愿者专有字段 -->
              <template v-if="isVolunteer">
                <el-row :gutter="20">
                  <el-col :xs="24" :sm="12">
                    <!-- 输入框统一 + 布局优化这里 -->
                    <el-form-item label="学院" class="unified-input">
                      <el-select v-model="formData.college" placeholder="请选择学院" @change="handleCollegeChange">
                        <el-option
                          v-for="college in Object.keys(COLLEGE_MAJORS)"
                          :key="college"
                          :label="college"
                          :value="college"
                        />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :xs="24" :sm="12">
                    <el-form-item label="专业" class="unified-input">
                      <el-select v-model="formData.major" placeholder="请选择专业" :disabled="!formData.college">
                        <el-option
                          v-for="major in COLLEGE_MAJORS[formData.college as keyof typeof COLLEGE_MAJORS] || []"
                          :key="major"
                          :label="major"
                          :value="major"
                        />
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>

                <!-- 输入框统一 + 布局优化这里：简介大文本 -->
                <el-form-item label="个人简介" class="unified-input bio-input">
                  <el-input
                    v-model="formData.bio"
                    type="textarea"
                    :rows="4"
                    placeholder="介绍一下自己吧，让大家更了解你~"
                    maxlength="200"
                    show-word-limit
                  />
                </el-form-item>

                <el-form-item label="兴趣标签" class="unified-input">
                  <el-select
                    v-model="interestTags"
                    multiple
                    placeholder="选择感兴趣的志愿领域（用于推荐活动）"
                  >
                    <el-option
                      v-for="item in categoryOptions"
                      :key="item.id"
                      :label="item.name"
                      :value="item.name"
                    />
                  </el-select>
                  <div class="input-tip">提示：系统会根据您的兴趣为您推荐相关活动</div>
                </el-form-item>

                <!-- 技能芯片标签（原有色） -->
                <el-form-item label="技能标签" class="unified-input">
                  <div class="skill-chips">
                    <el-tag
                      v-for="(tag, index) in skillTags"
                      :key="index"
                      closable
                      @close="removeTag(index)"
                      class="skill-chip"
                      effect="dark"
                      type="success"
                    >
                      {{ tag }}
                    </el-tag>
                    <el-input
                      v-if="inputVisible"
                      ref="tagInputRef"
                      v-model="inputValue"
                      class="chip-input"
                      size="default"
                      @keyup.enter="handleInputConfirm"
                      @blur="handleInputConfirm"
                    />
                    <el-button v-else class="add-chip-btn" size="default" @click="showInput">
                      <el-icon><Plus /></el-icon> 添加技能
                    </el-button>
                  </div>
                </el-form-item>
              </template>
            </el-form>
          </div>
        </el-col>
      </el-row>

      <!-- ===== 底部固定保存按钮 ===== -->
      <div class="fixed-save-bar" ref="saveRef">
        <el-button
          type="primary"
          size="large"
          :loading="saving"
          :disabled="saving"
          @click="handleSave"
          class="save-btn"
        >
          <el-icon><Check /></el-icon>
          {{ saving ? '保存中...' : '保存修改' }}
        </el-button>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, nextTick, onMounted } from 'vue'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { request } from '@/utils/request'
import { useUserStore } from '@/stores/user'
import {
  User, Camera, EditPen, Calendar, Plus, Check,
  Timer, Trophy, Star
} from '@element-plus/icons-vue'



// ==================== Refs ====================
const formRef = ref<FormInstance>()
const tagInputRef = ref()
const heroRef = ref()
const statsRef = ref()
const mainRef = ref()
const saveRef = ref()

const loading = ref(false)
const saving = ref(false)
const profile = ref<any>({})
const inputVisible = ref(false)
const inputValue = ref('')
const skillTags = ref<string[]>([])
const interestTags = ref<string[]>([])
const avatarTimestamp = ref(Date.now())

// ==================== 学院专业数据 ====================
const COLLEGE_MAJORS = {
  '计算机学院': ['软件工程', '计算机科学', '人工智能'],
  '电子工程学院': ['电子信息', '通信工程', '微电子'],
  '机械工程学院': ['机械设计', '自动化', '车辆工程'],
  '经济管理学院': ['工商管理', '会计学', '金融学'],
  '外国语学院': ['英语', '日语', '翻译'],
  '法学院': ['法学', '知识产权', '国际法'],
  '艺术学院': ['美术学', '设计学', '音乐学'],
  '医学院': ['临床医学', '护理学', '药学'],
  '理学院': ['数学', '物理学', '化学'],
  '建筑学院': ['建筑学', '城乡规划', '风景园林']
}

const categoryOptions = ref<any[]>([])

// ==================== 表单数据 ====================
const formData = reactive({
  username: '',
  name: '',
  gender: 0,
  phone: '',
  email: '',
  college: '',
  major: '',
  bio: '',
  avatar: '',
  skills: '',
  interestTags: ''
})

const handleCollegeChange = () => {
  formData.major = ''
}

const rules: FormRules = {
  name: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }]
}

// ==================== Computed ====================
const userRole = computed(() => {
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo) {
    try {
      return JSON.parse(userInfo).role || ''
    } catch {
      return ''
    }
  }
  return ''
})

const isVolunteer = computed(() => userRole.value === 'VOLUNTEER')

const getRoleName = (role: string) => {
  const roleMap: Record<string, string> = {
    'ADMIN': '系统管理员',
    'ORGANIZER': '活动组织者',
    'VOLUNTEER': '志愿者'
  }
  return roleMap[role] || '未知角色'
}

const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token')}`
}))

const avatarWithTimestamp = computed(() => {
  const url = formData.avatar
  if (!url) return ''
  const separator = url.includes('?') ? '&' : '?'
  return `${url}${separator}t=${avatarTimestamp.value}`
})

const volunteerLevel = computed(() => {
  const hours = profile.value.totalHours || 0
  if (hours >= 100) return 4
  if (hours >= 50) return 3
  if (hours >= 20) return 2
  return 1
})

// ==================== API 调用 ====================
const fetchCategories = async () => {
  try {
    const res = await request.get('/category/list')
    categoryOptions.value = res.data || []
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

const fetchProfile = async () => {
  loading.value = true
  try {
    if (isVolunteer.value) {
      const res = await request.get('/volunteer/profile')
      profile.value = res.data || {}
      
      Object.assign(formData, {
        username: res.data.username || '',
        name: res.data.name || '',
        gender: res.data.gender || 0,
        phone: res.data.phone || '',
        email: res.data.email || '',
        college: res.data.college || '',
        major: res.data.major || '',
        bio: res.data.bio || '',
        avatar: res.data.avatar || '',
        skills: res.data.skills || ''
      })

      if (res.data.skills) {
        skillTags.value = res.data.skills.split(',').filter((s: string) => s.trim())
      }

      if (res.data.interestTags) {
        try {
          interestTags.value = JSON.parse(res.data.interestTags)
        } catch (e) {
          interestTags.value = []
        }
      } else {
        interestTags.value = []
      }
    } else {
      const res = await request.get('/auth/current')
      if (res.code === 200 && res.data) {
        Object.assign(formData, {
          username: res.data.username || '',
          email: res.data.email || '',
          phone: res.data.phone || '',
          avatar: res.data.avatar || ''
        })
      }
    }
  } catch (error) {
    console.error('获取个人信息失败:', error)
  } finally {
    loading.value = false
  }
}

const handleAvatarSuccess = (response: any) => {
  if (response.code === 200) {
    let newAvatarUrl = response.data.url || response.data
    
    // 【关键修复】如果返回的是相对路径，拼接完整 URL
    if (newAvatarUrl && newAvatarUrl.startsWith('/')) {
      // 从当前 origin 或配置获取 API 基础地址
      const apiBase = import.meta.env.VITE_API_BASE || 'http://localhost:8080'
      newAvatarUrl = apiBase + newAvatarUrl
    }
    
    formData.avatar = newAvatarUrl
    avatarTimestamp.value = Date.now()
    
    // 使用 Pinia Store 更新全局状态，解决右上角头像不同步问题
    const userStore = useUserStore()
    userStore.setAvatar(newAvatarUrl)
    
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const beforeAvatarUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

const showInput = () => {
  inputVisible.value = true
  nextTick(() => {
    tagInputRef.value?.focus()
  })
}

const handleInputConfirm = () => {
  if (inputValue.value.trim()) {
    if (!skillTags.value.includes(inputValue.value.trim())) {
      skillTags.value.push(inputValue.value.trim())
    }
  }
  inputVisible.value = false
  inputValue.value = ''
}

const removeTag = (index: number) => {
  skillTags.value.splice(index, 1)
}

const handleSave = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    saving.value = true
    try {
      if (isVolunteer.value) {
        formData.skills = skillTags.value.join(',')
        formData.interestTags = JSON.stringify(interestTags.value)
        await request.put('/volunteer/profile', formData)
      } else {
        await request.put('/user/profile', {
          avatar: formData.avatar,
          email: formData.email,
          phone: formData.phone
        })
      }
      
      ElMessage.success('保存成功')
      
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      userInfo.avatar = formData.avatar
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    } catch (error) {
      console.error('保存失败:', error)
    } finally {
      saving.value = false
    }
  })
}

// ==================== 入场动画 ====================
// ==================== 入场动画 ====================
const initAnimations = () => {
  // 简单的CSS动画替代GSAP
  setTimeout(() => {
    heroRef.value?.classList?.add('animate-in')
  }, 100)
  setTimeout(() => {
    // el-row 是组件，需要访问 $el 获取 DOM 元素
    statsRef.value?.$el?.classList?.add('animate-in')
  }, 200)
  setTimeout(() => {
    // el-row 是组件，需要访问 $el 获取 DOM 元素
    mainRef.value?.$el?.classList?.add('animate-in')
  }, 300)
  setTimeout(() => {
    // div 直接访问
    saveRef.value?.classList?.add('animate-in')
  }, 400)
}

onMounted(() => {
  fetchCategories()
  fetchProfile()
  initAnimations()
})
</script>

<style lang="scss" scoped>
/* ==================== 基础变量 ==================== */
.profile-page {
  padding: 24px;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e9f2 100%);
  padding-bottom: 100px;
}

/* ==================== 玻璃卡片基础样式 ==================== */
.glass-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  padding: 24px;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 16px 48px rgba(0, 0, 0, 0.12);
  }
}

/* ==================== 入场动画（默认可见） ==================== */
.hero-section,
.stats-row,
.main-content,
.fixed-save-bar {
  opacity: 1;
  transform: translateY(0);
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

/* ==================== 顶部英雄区 ==================== */
.hero-section {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(16px);
  border-radius: 20px;
  padding: 32px;
  margin-bottom: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
}

.avatar-wrapper {
  display: flex;
  justify-content: center;
  
  .avatar-uploader {
    position: relative;
    cursor: pointer;
    
    .hero-avatar {
      border: 5px solid #fff;
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
      transition: all 0.3s ease;
    }
    
    &:hover .hero-avatar {
      box-shadow: 0 0 0 4px rgba(64, 158, 255, 0.3), 0 8px 32px rgba(0, 0, 0, 0.2);
    }
    
    .avatar-overlay {
      position: absolute;
      top: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 140px;
      height: 140px;
      border-radius: 50%;
      background: rgba(0, 0, 0, 0.6);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: #fff;
      opacity: 0;
      transition: opacity 0.3s ease;
      
      span {
        font-size: 13px;
        margin-top: 6px;
      }
    }
    
    &:hover .avatar-overlay {
      opacity: 1;
    }
  }
}

.hero-info {
  padding: 16px 0;
  
  .name-row {
    display: flex;
    align-items: center;
    gap: 16px;
    flex-wrap: wrap;
    margin-bottom: 12px;
  }
  
  .user-name {
    font-size: 28px;
    font-weight: 700;
    color: #1e293b;
    margin: 0;
  }
  
  .role-badge {
    font-weight: 600;
    padding: 6px 16px;
  }
  
  .points-display {
    margin-bottom: 12px;
    
    .points-label {
      font-size: 14px;
      color: #64748b;
      margin-right: 8px;
    }
    
    .points-value {
      font-size: 32px;
      font-weight: 800;
      background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
  }
  
  .bio-preview {
    color: #64748b;
    font-size: 15px;
    margin: 0;
    line-height: 1.6;
  }
}

/* ==================== 统计卡片 ==================== */
.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  }
  
  .stat-icon {
    font-size: 36px;
  }
  
  .stat-content {
    display: flex;
    flex-direction: column;
  }
  
  .stat-value {
    font-size: 28px;
    font-weight: 700;
    
    &.blue { color: #409eff; }
    &.green { color: #67c23a; }
    &.orange { color: #e6a23c; }
    &.red { color: #f56c6c; }
  }
  
  .stat-label {
    font-size: 13px;
    color: #94a3b8;
    margin-top: 4px;
  }
}

/* ==================== 主内容区 ==================== */
.main-content {
  margin-bottom: 24px;
}

.side-section {
  margin-bottom: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 20px;
}

.quick-actions {
  .action-buttons {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }
  
  .action-btn {
    width: 100%;
    height: 48px;
    font-size: 15px;
    justify-content: flex-start;
    padding-left: 20px;
    border-radius: 12px;
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateX(4px);
    }
  }
}

/* ==================== 输入框统一 + 布局优化这里 ==================== */
.form-section {
  .form-header {
    margin-bottom: 24px;
  }
}

.profile-form {
  /* 输入框统一 + 布局优化这里：统一样式 */
  .unified-input {
    margin-bottom: 24px;
    
    :deep(.el-form-item__label) {
      font-weight: 600;
      color: #334155;
      font-size: 14px;
      margin-bottom: 8px;
    }
    
    /* 输入框统一 + 布局优化这里：100%宽 + 52px高 + 12px圆角 */
    :deep(.el-input__wrapper),
    :deep(.el-textarea__inner),
    :deep(.el-select) {
      width: 100% !important;
    }
    
    :deep(.el-input__wrapper) {
      height: 52px;
      border-radius: 12px;
      background: rgba(241, 245, 249, 0.8);
      backdrop-filter: blur(8px);
      box-shadow: none;
      border: 2px solid transparent;
      transition: all 0.3s ease;
      
      &:hover {
        background: rgba(241, 245, 249, 1);
      }
      
      &.is-focus {
        border-color: #409eff;
        background: #fff;
        box-shadow: 0 0 0 4px rgba(64, 158, 255, 0.15);
      }
    }
    
    :deep(.el-input__inner) {
      height: 48px;
      font-size: 15px;
      color: #1e293b;
    }
    
    :deep(.el-textarea__inner) {
      border-radius: 12px;
      background: rgba(241, 245, 249, 0.8);
      backdrop-filter: blur(8px);
      border: 2px solid transparent;
      padding: 16px;
      font-size: 15px;
      transition: all 0.3s ease;
      
      &:hover {
        background: rgba(241, 245, 249, 1);
      }
      
      &:focus {
        border-color: #409eff;
        background: #fff;
        box-shadow: 0 0 0 4px rgba(64, 158, 255, 0.15);
      }
    }
    
    :deep(.el-select) {
      width: 100%;
      
      .el-select__wrapper {
        height: 52px;
        border-radius: 12px;
        background: rgba(241, 245, 249, 0.8);
        border: 2px solid transparent;
        
        &:hover {
          background: rgba(241, 245, 249, 1);
        }
        
        &.is-focused {
          border-color: #409eff;
          background: #fff;
          box-shadow: 0 0 0 4px rgba(64, 158, 255, 0.15);
        }
      }
    }
  }
  
  .input-tip {
    font-size: 12px;
    color: #94a3b8;
    margin-top: 8px;
  }
}

/* ==================== 技能芯片标签 ==================== */
.skill-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  
  .skill-chip {
    font-size: 14px;
    padding: 8px 14px;
    border-radius: 20px;
    transition: all 0.3s ease;
    
    &:hover {
      transform: scale(1.05);
    }
  }
  
  .chip-input {
    width: 120px;
    
    :deep(.el-input__wrapper) {
      height: 36px;
      border-radius: 20px;
    }
  }
  
  .add-chip-btn {
    border-style: dashed;
    border-radius: 20px;
    height: 36px;
    padding: 0 16px;
  }
}

/* ==================== 底部固定保存按钮 ==================== */
.fixed-save-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px 24px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(16px);
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: center;
  z-index: 100;
  
  .save-btn {
    min-width: 200px;
    height: 52px;
    font-size: 16px;
    font-weight: 600;
    border-radius: 12px;
    transition: all 0.3s ease;
    
    &:hover:not(:disabled) {
      transform: scale(1.02);
      box-shadow: 0 8px 24px rgba(64, 158, 255, 0.4);
    }
    
    &:active:not(:disabled) {
      transform: scale(0.98);
    }
  }
}

/* ==================== 暗黑模式适配（高对比文字/背景） ==================== */
html.dark .profile-page {
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  
  .hero-section,
  .glass-card,
  .stat-card {
    background: rgba(30, 41, 59, 0.85) !important;
    border-color: rgba(71, 85, 105, 0.5);
  }
  
  .user-name,
  .section-title {
    color: #f1f5f9 !important;
  }
  
  .bio-preview,
  .stat-label,
  .input-tip {
    color: #94a3b8 !important;
  }
  
  .profile-form {
    :deep(.el-form-item__label) {
      color: #e2e8f0 !important;
    }
    
    :deep(.el-input__wrapper) {
      background: rgba(51, 65, 85, 0.8) !important;
      
      &.is-focus {
        background: rgba(51, 65, 85, 1) !important;
      }
    }
    
    :deep(.el-input__inner) {
      color: #f1f5f9 !important;
    }
    
    :deep(.el-textarea__inner) {
      background: rgba(51, 65, 85, 0.8) !important;
      color: #f1f5f9 !important;
      
      &:focus {
        background: rgba(51, 65, 85, 1) !important;
      }
    }
    
    :deep(.el-select .el-select__wrapper) {
      background: rgba(51, 65, 85, 0.8) !important;
    }
  }
  
  .fixed-save-bar {
    background: rgba(15, 23, 42, 0.95) !important;
    border-top-color: rgba(51, 65, 85, 0.5);
  }
}

/* ==================== 移动端响应式 ==================== */
@media (max-width: 768px) {
  .profile-page {
    padding: 16px;
    padding-bottom: 90px;
  }
  
  .hero-section {
    padding: 20px;
    text-align: center;
    
    .hero-info {
      .name-row {
        justify-content: center;
      }
      
      .points-display {
        text-align: center;
      }
    }
  }
  
  .stat-card {
    padding: 16px;
    
    .stat-icon {
      font-size: 28px;
    }
    
    .stat-value {
      font-size: 22px;
    }
  }
  
  .glass-card {
    padding: 20px;
  }
  
  .profile-form {
    :deep(.el-input__wrapper) {
      height: 48px;
    }
  }
  
  .fixed-save-bar {
    padding: 12px 16px;
    
    .save-btn {
      width: 100%;
      height: 48px;
    }
  }
}
</style>
