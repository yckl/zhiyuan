<template>
  <div class="profile-page" v-loading="loading">

    <!-- ==================== Header 区域 (渐变背景) ==================== -->
    <div class="dashboard-header">
      <div class="user-info-row">
        <!-- 头像 -->
        <div class="avatar-wrapper">
          <el-upload
            class="avatar-uploader"
            action="/api/file/upload/avatar"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
          >
            <div class="avatar-ring-flat">
              <el-avatar :size="64" :src="avatarWithTimestamp" class="user-avatar">
                <el-icon :size="28"><User /></el-icon>
              </el-avatar>
              <div class="avatar-edit-badge-small">
                <el-icon :size="10"><Camera /></el-icon>
              </div>
            </div>
          </el-upload>
        </div>
        
        <!-- 用户信息 -->
        <div class="info-content">
          <div class="greeting-line">
            <span class="greeting-text">{{ timeGreeting }}</span>
            <span class="user-name">{{ formData.name || formData.username || '未设置' }}</span>
          </div>
          
          <div class="role-badge" v-if="isVolunteer">
            <el-icon><School /></el-icon>
            <span>{{ formData.college ? formData.college + ' · ' + (formData.major || '未填') : '尚未选择学院' }}</span>
          </div>
          <div class="role-badge" v-else>
            <el-icon><User /></el-icon>
            <span>{{ getRoleName(userRole) }}</span>
          </div>
        </div>
      </div>

      <!-- 统计数据卡片 -->
      <div class="stats-grid" v-if="isVolunteer">
        <div class="stat-item" v-for="(s, idx) in statItems" :key="s.label">
          <div class="stat-content">
            <span class="stat-num">{{ s.display }}</span>
            <span class="stat-label">{{ s.label }}</span>
          </div>
          <div v-if="idx < statItems.length - 1" class="stat-divider"></div>
        </div>
      </div>
    </div>

    <!-- ==================== 宫格菜单 ==================== -->
    <div class="menu-section">
      <!-- 骨架屏 -->
      <div v-if="menuLoading" class="menu-grid">
        <div v-for="i in 8" :key="i" class="skeleton-item">
          <el-skeleton animated>
            <template #template>
              <div style="display:flex;flex-direction:column;align-items:center;gap:8px;padding:20px 0">
                <el-skeleton-item variant="circle" style="width:40px;height:40px" />
                <el-skeleton-item variant="text" style="width:50px" />
              </div>
            </template>
          </el-skeleton>
        </div>
      </div>

      <!-- 实际菜单 -->
      <div v-else class="menu-grid">
        <div
          v-for="(menu, idx) in menuItems"
          :key="idx"
          class="menu-card"
          :style="{ animationDelay: `${idx * 0.04}s` }"
          @click="menu.action()"
        >
          <el-badge :value="menu.badge" :hidden="!menu.badge" :max="99">
            <span class="menu-icon" :style="{ background: menu.bg }">
              <el-icon><component :is="menu.icon" /></el-icon>
            </span>
          </el-badge>
          <span class="menu-label">{{ menu.label }}</span>
        </div>
      </div>
    </div>

    <!-- ==================== 退出登录 ==================== -->
    <div class="logout-section">
      <button class="logout-btn" @click="handleLogout">退出登录</button>
    </div>

    <!-- ==================== 编辑资料弹窗 ==================== -->
    <el-drawer
      v-model="showEditProfile"
      :title="'编辑资料'"
      :size="isMobile ? '100%' : '480px'"
      :direction="isMobile ? 'btt' : 'rtl'"
      class="edit-drawer"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-position="top"
        class="edit-form"
      >
        <el-form-item label="用户名">
          <el-input v-model="formData.username" disabled />
        </el-form-item>

        <el-form-item label="真实姓名" prop="name" v-if="isVolunteer">
          <el-input v-model="formData.name" placeholder="请输入真实姓名" />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号" />
        </el-form-item>

        <el-form-item label="邮箱">
          <el-input v-model="formData.email" placeholder="请输入邮箱" />
        </el-form-item>

        <template v-if="isVolunteer">
          <el-form-item label="学院">
            <el-select v-model="formData.college" placeholder="请选择学院" @change="handleCollegeChange">
              <el-option
                v-for="college in Object.keys(COLLEGE_MAJORS)"
                :key="college" :label="college" :value="college"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="专业">
            <el-select v-model="formData.major" placeholder="请选择专业" :disabled="!formData.college">
              <el-option
                v-for="major in COLLEGE_MAJORS[formData.college as keyof typeof COLLEGE_MAJORS] || []"
                :key="major" :label="major" :value="major"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="个人简介">
            <el-input v-model="formData.bio" type="textarea" :rows="3" placeholder="介绍一下自己吧~" maxlength="200" show-word-limit />
          </el-form-item>

          <el-form-item label="兴趣标签">
            <el-select v-model="interestTags" multiple placeholder="选择感兴趣的志愿领域">
              <el-option v-for="item in categoryOptions" :key="item.id" :label="item.name" :value="item.name" />
            </el-select>
          </el-form-item>

          <el-form-item label="技能标签">
            <div class="skill-chips">
              <el-tag
                v-for="(tag, index) in skillTags" :key="index"
                closable effect="dark" type="success" class="skill-chip"
                @close="removeTag(index)"
              >{{ tag }}</el-tag>
              <el-input
                v-if="inputVisible" ref="tagInputRef" v-model="inputValue"
                size="small" style="width: 100px"
                @keyup.enter="handleInputConfirm" @blur="handleInputConfirm"
              />
              <el-button v-else size="small" @click="showInput">
                <el-icon><Plus /></el-icon> 添加
              </el-button>
            </div>
          </el-form-item>
        </template>
      </el-form>

      <div class="drawer-footer-glass">
        <el-button class="cancel-btn" @click="showEditProfile = false">取消</el-button>
        <button class="save-btn" :disabled="saving" @click="handleSave">
          <el-icon v-if="saving" class="is-loading"><Loading /></el-icon>
          {{ saving ? '正在保存...' : '确认保存' }}
        </button>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, nextTick, onMounted, markRaw } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { request } from '@/utils/request'
import { useUserStore } from '@/stores/user'
import {
  User, Camera, Calendar, Timer, Trophy, Star, Box, EditPen, Reading,
  Setting, DataLine, ChatDotRound, Service, Plus, School
} from '@element-plus/icons-vue'

const router = useRouter()

// ================== 响应式 ==================
const windowWidth = ref(window.innerWidth)
const isMobile = computed(() => windowWidth.value < 768)
onMounted(() => window.addEventListener('resize', () => { windowWidth.value = window.innerWidth }))

// ================== 状态 ==================
const loading = ref(false)
const saving = ref(false)
const menuLoading = ref(true)
const showEditProfile = ref(false)
const formRef = ref<FormInstance>()
const tagInputRef = ref()
const profile = ref<any>({})
const inputVisible = ref(false)
const inputValue = ref('')
const skillTags = ref<string[]>([])
const interestTags = ref<string[]>([])
const avatarTimestamp = ref(Date.now())
const categoryOptions = ref<any[]>([])

const stats = ref({ totalHours: 0, serviceCount: 0, totalPoints: 0, availablePoints: 0 })

// 动画数字
const animHours = ref(0)
const animPoints = ref(0)
const animCount = ref(0)

const formData = reactive({
  username: '', name: '', gender: 0, phone: '', email: '',
  college: '', major: '', bio: '', avatar: '', skills: '', interestTags: ''
})

// ================== 学院专业 ==================
const COLLEGE_MAJORS: Record<string, string[]> = {
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

const handleCollegeChange = () => { formData.major = '' }

const rules: FormRules = {
  name: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }]
}

// ================== Computed ==================
const userRole = computed(() => {
  try { return JSON.parse(localStorage.getItem('userInfo') || '{}').role || '' }
  catch { return '' }
})

const isVolunteer = computed(() => userRole.value === 'VOLUNTEER')

const timeGreeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const getRoleName = (role: string) => {
  const map: Record<string, string> = { ADMIN: '系统管理员', ORGANIZER: '活动组织者', VOLUNTEER: '志愿者' }
  return map[role] || '普通用户'
}

const uploadHeaders = computed(() => ({ Authorization: `Bearer ${localStorage.getItem('token')}` }))

const avatarWithTimestamp = computed(() => {
  const url = request.resolveUrl(formData.avatar)
  if (!url) return ''
  return `${url}${url.includes('?') ? '&' : '?'}t=${avatarTimestamp.value}`
})

const volunteerLevel = computed(() => {
  const hours = profile.value.totalHours || 0
  if (hours >= 100) return 4
  if (hours >= 50) return 3
  if (hours >= 20) return 2
  return 1
})

const statItems = computed(() => [
  { label: '时长(h)', display: animHours.value },
  { label: '服务次数', display: animCount.value },
  { label: '积分', display: animPoints.value },
  { label: '等级', display: `Lv.${volunteerLevel.value}` }
])

// ================== 宫格菜单 ==================
const menuItems = computed(() => [
  { label: '我的活动', icon: markRaw(Calendar), bg: 'linear-gradient(135deg, #54A0FF, #2E86DE)', action: () => router.push('/my-activities'), badge: 0 },
  { label: '志愿记录', icon: markRaw(Timer), bg: 'linear-gradient(135deg, #00CEC9, #00B894)', action: () => router.push('/user/volunteer-record'), badge: 0 },
  { label: '荣誉证书', icon: markRaw(Trophy), bg: 'linear-gradient(135deg, #FFA502, #E67E22)', action: () => router.push('/profile/honor'), badge: 0 },
  { label: '我的背包', icon: markRaw(Box), bg: 'linear-gradient(135deg, #A29BFE, #6C5CE7)', action: () => router.push('/mall/backpack'), badge: 0 },
  { label: '我的心得', icon: markRaw(EditPen), bg: 'linear-gradient(135deg, #FD79A8, #E84393)', action: () => router.push('/my-experiences'), badge: 0 },
  { label: '我的收藏', icon: markRaw(Star), bg: 'linear-gradient(135deg, #FF6B6B, #EE5A24)', action: () => router.push('/profile/favorite'), badge: 0 },
  { label: '我的考试', icon: markRaw(Reading), bg: 'linear-gradient(135deg, #1DD1A1, #10AC84)', action: () => router.push('/training/my'), badge: 0 },
  { label: '数据中心', icon: markRaw(DataLine), bg: 'linear-gradient(135deg, #636E72, #2D3436)', action: () => router.push('/profile/stats'), badge: 0 },
  { label: '我的评价', icon: markRaw(ChatDotRound), bg: 'linear-gradient(135deg, #FDCB6E, #F39C12)', action: () => router.push('/profile/reviews'), badge: 0 },
  { label: '问题反馈', icon: markRaw(Service), bg: 'linear-gradient(135deg, #74B9FF, #0984E3)', action: () => router.push('/feedback'), badge: 0 },
  { label: '编辑资料', icon: markRaw(Setting), bg: 'linear-gradient(135deg, #B2BEC3, #636E72)', action: () => { showEditProfile.value = true }, badge: 0 }
])

// ================== 数字跳动动画 ==================
const animateNumber = (target: number, setter: (v: number) => void, duration = 800) => {
  const start = performance.now()
  const step = (now: number) => {
    const progress = Math.min((now - start) / duration, 1)
    const eased = 1 - Math.pow(1 - progress, 3) // easeOutCubic
    setter(Math.round(eased * target))
    if (progress < 1) requestAnimationFrame(step)
  }
  requestAnimationFrame(step)
}

// ================== API ==================
const fetchCategories = async () => {
  try { categoryOptions.value = (await request.get('/category/list')).data || [] }
  catch (e) { console.error('获取分类失败:', e) }
}

const fetchProfile = async () => {
  loading.value = true
  try {
    if (isVolunteer.value) {
      const res = await request.get('/volunteer/profile')
      profile.value = res.data || {}
      Object.assign(formData, {
        username: res.data.username || '', name: res.data.name || '',
        gender: res.data.gender || 0, phone: res.data.phone || '',
        email: res.data.email || '', college: res.data.college || '',
        major: res.data.major || '', bio: res.data.bio || '',
        avatar: res.data.avatar || '', skills: res.data.skills || ''
      })
      if (res.data.skills) skillTags.value = res.data.skills.split(',').filter((s: string) => s.trim())
      try { interestTags.value = res.data.interestTags ? JSON.parse(res.data.interestTags) : [] }
      catch { interestTags.value = [] }
    } else {
      const res = await request.get('/auth/current')
      if (res.code === 200 && res.data) {
        Object.assign(formData, {
          username: res.data.username || '', email: res.data.email || '',
          phone: res.data.phone || '', avatar: res.data.avatar || ''
        })
      }
    }
  } catch (e) { console.error('获取个人信息失败:', e) }
  finally { loading.value = false }
}

const fetchStats = async () => {
  if (!isVolunteer.value) return
  try {
    const res = await request.get('/volunteer/statistics')
    if (res.code === 200) {
      stats.value = res.data
      // 启动计数动画
      animateNumber(res.data.totalHours || 0, v => { animHours.value = v })
      animateNumber(res.data.totalPoints || 0, v => { animPoints.value = v })
      animateNumber(res.data.serviceCount || 0, v => { animCount.value = v })
    }
  } catch (e) { console.error('获取统计数据失败:', e) }
}

// ================== 头像上传 ==================
const handleAvatarSuccess = (response: any) => {
  if (response.code === 200) {
    let url = response.data.url || response.data
    // 确保 URL 是以 / 开头的相对路径，以便通过 Vite 代理访问
    if (url && !url.startsWith('http') && !url.startsWith('/')) {
      url = '/' + url
    }
    formData.avatar = url
    avatarTimestamp.value = Date.now()
    useUserStore().setAvatar(url)
    
    // 同步本地存储，防止刷新后失效
    const ui = JSON.parse(localStorage.getItem('userInfo') || '{}')
    ui.avatar = url
    localStorage.setItem('userInfo', JSON.stringify(ui))
    
    ElMessage.success('头像上传成功')
  } else { ElMessage.error(response.message || '上传失败') }
}

const beforeAvatarUpload = (file: File) => {
  if (!file.type.startsWith('image/')) { ElMessage.error('只能上传图片'); return false }
  if (file.size / 1024 / 1024 > 2) { ElMessage.error('图片不能超过 2MB'); return false }
  return true
}

// ================== 技能标签 ==================
const showInput = () => { inputVisible.value = true; nextTick(() => tagInputRef.value?.focus()) }
const handleInputConfirm = () => {
  if (inputValue.value.trim() && !skillTags.value.includes(inputValue.value.trim()))
    skillTags.value.push(inputValue.value.trim())
  inputVisible.value = false; inputValue.value = ''
}
const removeTag = (index: number) => { skillTags.value.splice(index, 1) }

// ================== 保存 ==================
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
        await request.put('/user/profile', { avatar: formData.avatar, email: formData.email, phone: formData.phone })
      }
      ElMessage.success('保存成功')
      showEditProfile.value = false
      const ui = JSON.parse(localStorage.getItem('userInfo') || '{}')
      ui.avatar = formData.avatar
      localStorage.setItem('userInfo', JSON.stringify(ui))
    } catch (e) { console.error('保存失败:', e) }
    finally { saving.value = false }
  })
}

// ================== 退出登录 ==================
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '退出', cancelButtonText: '取消', type: 'warning', roundButton: true
  }).then(() => {
    useUserStore().clearUser()
    ElMessage.success('已安全退出')
    router.push('/login')
  }).catch(() => {})
}

onMounted(async () => {
  await fetchCategories()
  await fetchProfile()
  await fetchStats()
  // 关闭骨架屏
  setTimeout(() => { menuLoading.value = false }, 300)
})
</script>

<style lang="scss" scoped>
@keyframes cardIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.profile-page {
  background: #f5f7fa;
  min-height: 100vh;
  padding-bottom: 40px;
}

// ================================================================
// Dashboard Header (Organizer Style)
// ================================================================
.dashboard-header {
  background: linear-gradient(135deg, #00C9A7 0%, #05D5B3 100%); 
  padding: 30px 24px;
  border-radius: 0 0 24px 24px;
  color: #fff;
  margin-bottom: 20px;
  box-shadow: 0 4px 20px rgba(0, 147, 233, 0.15);
}

.user-info-row {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
}

.avatar-ring-flat {
  position: relative;
  cursor: pointer;
  transition: transform 0.3s;
  
  &:hover { transform: scale(1.05); }

  .user-avatar {
    border: 3px solid rgba(255, 255, 255, 0.6);
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    background: #fff;
    color: #0093E9;
  }

  .avatar-edit-badge-small {
    position: absolute;
    bottom: 0;
    right: 0;
    width: 20px;
    height: 20px;
    border-radius: 50%;
    background: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #0093E9;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }
}

.info-content {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.greeting-line {
  font-size: 22px;
  font-weight: bold;
  margin-bottom: 8px;
  text-shadow: 0 2px 4px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  gap: 4px;
}

.role-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(5px);
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 13px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  font-weight: 500;
}

// --- 统计区 (透明卡片 + 分隔线) ---
.stats-grid {
  display: flex;
  align-items: center;
  justify-content: space-around;
  width: 100%;
  max-width: 500px;
  margin-top: 24px;
  padding: 0 10px;
}

.stat-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;

  .stat-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 2px;
  }

  .stat-num {
    font-size: 24px;
    font-weight: 800;
    color: #fff;
    letter-spacing: -0.5px;
  }

  .stat-label {
    font-size: 11px;
    color: rgba(255, 255, 255, 0.85);
    font-weight: 500;
  }

  .stat-divider {
    position: absolute;
    right: 0;
    height: 24px;
    width: 1px;
    background: rgba(255, 255, 255, 0.2);
  }
}

// ================================================================
// 宫格菜单
// ================================================================
.menu-section {
  padding: 16px;
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.skeleton-item {
  background: #fff;
  border-radius: 12px;
}

.menu-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.2, 0.8, 0.2, 1);
  animation: cardIn 0.4s ease-out both;
  padding: 12px 0;

  &:hover {
    transform: translateY(-6px);
    .menu-icon {
      transform: scale(1.1);
      box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
    }
    .menu-label { color: var(--primary-color); }
  }

  &:active { transform: scale(0.92); }

  .menu-icon {
    width: 52px;
    height: 52px;
    border-radius: 18px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 22px;
    box-shadow: 0 8px 16px rgba(0, 0, 0, 0.08);
    transition: all 0.3s cubic-bezier(0.2, 0.8, 0.2, 1);
    position: relative;
    overflow: hidden;

    // 增加一个图标内的高光感
    &::after {
      content: '';
      position: absolute;
      top: -50%;
      left: -50%;
      width: 100%;
      height: 100%;
      background: linear-gradient(135deg, rgba(255, 255, 255, 0.3) 0%, transparent 80%);
      transform: rotate(45deg);
    }
  }

  .menu-label {
    font-size: 12.5px;
    color: #475569;
    font-weight: 600;
    transition: color 0.2s;
  }
}

// ================================================================
// 退出登录
// ================================================================
.logout-section {
  padding: 24px 16px;
  display: flex;
  justify-content: center;
}

.logout-btn {
  width: 80%;
  max-width: 320px;
  border: none;
  background: transparent;
  color: #FF3B30;
  font-size: 15px;
  font-weight: 500;
  padding: 12px 0;
  border-radius: 24px;
  cursor: pointer;
  border: 1px solid rgba(255, 59, 48, 0.3);
  transition: all 0.25s;

  &:hover {
    background: #FF3B30;
    color: #fff;
    box-shadow: 0 4px 12px rgba(255, 59, 48, 0.3);
  }

  &:active { transform: scale(0.96); }
}

// ================================================================
// 编辑资料抽屉
// ================================================================
:deep(.edit-drawer) {
  .el-drawer__header {
    font-weight: 600;
    font-size: 17px;
    padding: 16px 20px;
    margin-bottom: 0;
    border-bottom: 0.5px solid rgba(0, 0, 0, 0.06);
  }

  .el-drawer__body {
    padding: 0 !important;
  }
}

.drawer-footer-glass {
  position: sticky;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  padding-bottom: calc(90px + env(safe-area-inset-bottom)); /* Lifted footer to clear TabBar (80px + 10px) */
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-top: 1px solid rgba(255, 255, 255, 0.5);
  z-index: 10;

  .cancel-btn {
    flex: 1;
    height: 46px;
    border-radius: 23px;
    font-size: 15px;
    border: 1px solid #e2e8f0;
    color: #64748b;
  }

  .save-btn {
    flex: 2;
    height: 46px;
    border: none;
    background: linear-gradient(135deg, var(--primary-color), #43e97b);
    color: #fff;
    border-radius: 23px;
    font-size: 15px;
    font-weight: 700;
    cursor: pointer;
    box-shadow: 0 4px 12px rgba(0, 191, 166, 0.3);
    transition: all 0.25s;

    &:hover { transform: translateY(-2px); box-shadow: 0 6px 16px rgba(0, 191, 166, 0.4); }
    &:active { transform: scale(0.96); }
    &:disabled { opacity: 0.6; cursor: not-allowed; }
  }
}

// 统一输入框样式
.edit-form {
  padding: 24px 20px 40px;

  :deep(.el-form-item__label) {
    font-size: 14px;
    font-weight: 600;
    color: #475569;
    padding-left: 4px;
    margin-bottom: 8px;
  }

  :deep(.el-input__wrapper), :deep(.el-textarea__inner), :deep(.el-select .el-input__wrapper) {
    background: #F5F7FA !important;
    border: 1.5px solid transparent !important;
    border-radius: 14px !important;
    box-shadow: none !important;
    padding: 10px 14px !important;
    transition: all 0.25s !important;

    &:hover { background: #EEF1F6 !important; }
    &.is-focus, &:focus {
      background: #fff !important;
      border-color: var(--primary-color) !important;
      box-shadow: 0 0 0 4px rgba(0, 191, 166, 0.1) !important;
    }
  }

  :deep(.el-textarea__inner) {
    border-radius: 16px !important;
    padding: 14px !important;
  }

  :deep(.el-input.is-disabled .el-input__wrapper) {
    background: #F1F5F9 !important;
    color: #94a3b8 !important;
  }
}

.skill-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;

  .skill-chip {
    border-radius: 16px;
  }
}

// ================================================================
// 手机适配
// ================================================================
@media (max-width: 768px) {
  .menu-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }

  .stats-grid { gap: 8px; }
  .stat-card .stat-num { font-size: 17px; }
}

// ================================================================
// 电脑端适配
// ================================================================
@media (min-width: 769px) {
  .profile-page {
    max-width: 640px;
    margin: 0 auto;
    padding-top: 20px;
    background: transparent;
  }

  .profile-header {
    border-radius: 16px;
  }

  .menu-section { padding: 16px 0; }

  .menu-card {
    border-radius: 12px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  }
}
</style>
