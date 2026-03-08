<template>
  <div class="login-wrapper">
    <!-- 响应式背景资产 (使用 img 标签提高加载优先级并防止白闪) -->
    <img 
      :src="bgSrc" 
      class="auth-bg" 
      :class="{ 'is-loaded': bgLoaded }"
      @load="bgLoaded = true"
      alt="background"
    >

    <!-- 毛玻璃登录卡片 (电报风) -->
    <div class="glass-card">
      <div class="mascot-container">
        <VolunteerMascot ref="mascotRef" />
      </div>

      <div class="header-section">
        <h1 class="main-title">校园志愿者系统</h1>
        <p class="sub-title">加入我们，成为光荣的志愿者</p>
      </div>

      <el-form :model="formData" class="premium-form" @keyup.enter="handleLogin">
        <div class="form-item-wrapper">
          <input 
            type="text" 
            v-model="formData.username" 
            placeholder="账号 / 学号" 
            class="custom-input"
            @focus="onInputFocus"
            @blur="onInputBlur"
            @input="onTypingInput"
          >
        </div>

        <div class="form-item-wrapper">
          <div class="password-wrapper">
             <input 
                :type="showPassword ? 'text' : 'password'" 
                v-model="formData.password" 
                placeholder="密码" 
                class="custom-input"
                @focus="onPasswordFocus"
                @blur="onPasswordBlur"
              >
              <el-icon class="toggle-eye" @click="togglePasswordVisibility">
                <component :is="showPassword ? View : Hide" />
              </el-icon>
          </div>
        </div>

        <div class="policy-wrapper">
          <el-checkbox v-model="formData.agreeProtocol">
            我已阅读并同意 <span class="link-text" @click.stop="showProtocol">《用户服务协议》</span>
          </el-checkbox>
        </div>

        <el-button 
          class="login-submit-btn" 
          :loading="loading" 
          @click="handleLogin"
        >
          {{ loading ? '进入中...' : '立即登录' }}
        </el-button>

        <div class="form-footer">
          <span class="footer-link">忘记密码？</span>
          <span class="footer-register" @click="router.push('/register')">没有账号？立即注册</span>
        </div>
      </el-form>
      
      <!-- 极简服务器配置入口 -->
      <div class="server-entry" @click="showServerConfig = true">
        <el-icon><Setting /></el-icon>
      </div>
    </div>

    <!-- 服务器配置弹窗 -->
    <el-dialog v-model="showServerConfig" title="API 服务器配置" width="300px" append-to-body>
      <el-input v-model="tempApiUrl" placeholder="http://ip:port" />
      <template #footer>
        <el-button size="small" @click="showServerConfig = false">取消</el-button>
        <el-button size="small" type="primary" @click="saveServerConfig">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { View, Hide, Setting } from '@element-plus/icons-vue'
import VolunteerMascot from '@/components/VolunteerMascot.vue'
import { useUserStore } from '@/stores/user'
import { request } from '@/utils/request'

// 导入背景图片资源，确保 Vite 正确解析
import pcBg from '@/assets/images/pc-login-bg.jpg'
import mobileBg from '@/assets/images/mobile-login-bg.jpg'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const showPassword = ref(false)
const showServerConfig = ref(false)
const mascotRef = ref<InstanceType<typeof VolunteerMascot>>()
const tempApiUrl = ref(localStorage.getItem('apiBaseUrl') || '')

// 响应式判断手机端
const windowWidth = ref(window.innerWidth)
const isMobile = computed(() => windowWidth.value <= 768)
const handleResize = () => { windowWidth.value = window.innerWidth }
onMounted(() => window.addEventListener('resize', handleResize))
onUnmounted(() => window.removeEventListener('resize', handleResize))

const bgSrc = computed(() => isMobile.value ? mobileBg : pcBg)
const bgLoaded = ref(false)

const formData = reactive({
  username: '',
  password: '',
  agreeProtocol: false
})

const handleLogin = async () => {
  if (!formData.agreeProtocol) {
    alert('请先阅读并同意用户服务协议')
    return
  }
  
  if (!formData.username || !formData.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }

  loading.value = true
  try {
    const res = await request.post('/auth/login', {
      username: formData.username,
      password: formData.password
    })
    
    userStore.setUser({
      userId: res.data.userId,
      username: res.data.username,
      role: res.data.role,
      avatar: res.data.avatar,
      nickname: res.data.nickname
    }, res.data.token)
    
    ElMessage.success('登录成功')
    
    // 🚦 记忆回跳逻辑：优先从 query 获取完整的 redirect
    const redirectPath = route.query.redirect as string
    if (redirectPath) {
      // 如果 redirect 包含多个参数（比如 /activity/1?autoEnroll=true），
      // router.replace(redirectPath) 在 Vue Router 4 中能很好处理全路径字符串
      router.replace(redirectPath)
    } else {
      router.replace('/')
    }
    console.log('User Data:', formData)
  } catch (err) {
    console.error('Login Failed:', err)
  } finally {
    loading.value = false
  }
}

const showProtocol = () => ElMessage.info('服务协议内容开发中...')

const saveServerConfig = () => {
  if (tempApiUrl.value) {
    localStorage.setItem('apiBaseUrl', tempApiUrl.value)
    ElMessage.success('配置已保存，重启生效')
    setTimeout(() => window.location.reload(), 800)
  }
}

// ====== 吉祥物交互逻辑 ======
let isPasswordFocused = false
const onInputFocus = () => mascotRef.value?.setTyping(formData.username.length)
const onTypingInput = () => mascotRef.value?.setTyping(formData.username.length)
const onInputBlur = () => { if (!isPasswordFocused) mascotRef.value?.setIdle() }

const onPasswordFocus = () => {
  isPasswordFocused = true
  showPassword.value ? mascotRef.value?.setPeek() : mascotRef.value?.setPassword()
}

const onPasswordBlur = () => {
  isPasswordFocused = false
  mascotRef.value?.setIdle()
}

const togglePasswordVisibility = () => {
  showPassword.value = !showPassword.value
  if (isPasswordFocused) {
    showPassword.value ? mascotRef.value?.setPeek() : mascotRef.value?.setFromPeek()
  }
}
</script>

<style scoped lang="scss">
.login-wrapper {
  min-height: 100vh;
  width: 100vw;
  display: flex;
  position: relative;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  background-color: #334155; /* 深蓝灰色底色，接近背景主视觉，防止白闪 */
  
  /* PC端布局 (右侧悬浮) */
  justify-content: flex-end;
  align-items: center;
  padding-right: 12%;
  
  @media (max-width: 768px) {
    /* 移动端布局 (靠下居中) */
    justify-content: center;
    align-items: flex-end;
    padding-right: 0;
    padding-bottom: 8vh;
  }
}

.auth-bg {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  z-index: 0;
  opacity: 0;
  transition: opacity 0.5s ease-in;
  pointer-events: none;
  
  &.is-loaded {
    opacity: 1;
  }
}

.glass-card {
  width: 400px;
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 24px;
  padding: 40px 32px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.08);
  position: relative;
  animation: cardFlyIn 0.8s cubic-bezier(0.16, 1, 0.3, 1);
  
  @media (max-width: 768px) {
    width: 88%;
    background: rgba(255, 255, 255, 0.82);
    padding: 32px 24px;
  }
}

.mascot-container {
  display: flex;
  justify-content: center;
  margin-top: -30px;
  margin-bottom: -10px;
}

.header-section {
  text-align: center;
  margin-bottom: 35px;
  
  .main-title {
    font-size: 24px;
    font-weight: 800;
    color: #1a1a1a;
    margin: 0;
    letter-spacing: 0.5px;
  }
  
  .sub-title {
    font-size: 13px;
    color: #999;
    margin-top: 6px;
  }
}

.premium-form {
  .form-item-wrapper {
    margin-bottom: 20px;
  }
  
  .custom-input {
    width: 100%;
    height: 48px;
    border-radius: 12px;
    border: none;
    background: #f5f5f5;
    padding: 0 16px;
    font-size: 15px;
    color: #333;
    transition: all 0.3s;
    outline: none;
    
    &::placeholder { color: #ccc; }
    
    &:focus {
      background: #fff;
      box-shadow: 0 0 0 1px #1DE9B6, 0 0 0 3px rgba(29, 233, 182, 0.15);
    }
  }
}

.password-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  
  .toggle-eye {
    position: absolute;
    right: 14px;
    cursor: pointer;
    color: #bbb;
    font-size: 18px;
    transition: color 0.2s;
    &:hover { color: #1DE9B6; }
  }
}

.policy-wrapper {
  margin: 10px 0 28px;
  
  :deep(.el-checkbox) {
    height: auto;
    white-space: normal;
    .el-checkbox__inner { border-radius: 4px; }
    .el-checkbox__label {
      font-size: 12px;
      color: #999;
    }
    &.is-checked .el-checkbox__inner {
      background-color: #1DE9B6;
      border-color: #1DE9B6;
    }
  }
  
  .link-text {
    color: #00B4DB;
    cursor: pointer;
    transition: color 0.2s;
    &:hover { color: #1DE9B6; }
  }
}

.login-submit-btn {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #1DE9B6 0%, #00B4DB 100%) !important;
  border: none !important;
  color: #fff !important;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 1px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
  box-shadow: 0 4px 15px rgba(29, 233, 182, 0.2) !important;
  
  &:hover {
    opacity: 0.9;
    box-shadow: 0 6px 20px rgba(29, 233, 182, 0.3) !important;
  }
  
  &:active {
    transform: scale(0.98);
  }
}

.form-footer {
  margin-top: 24px;
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  
  .footer-link {
    color: #aaa;
    cursor: pointer;
    &:hover { color: #00B4DB; }
  }
  
  .footer-register {
    color: #1DE9B6;
    font-weight: 700;
    cursor: pointer;
    transition: color 0.2s;
    &:hover { color: #00B4DB; }
  }
}

.server-entry {
  position: absolute;
  top: 16px;
  right: 16px;
  color: #ddd;
  cursor: pointer;
  transition: color 0.3s;
  &:hover { color: #1DE9B6; }
}

@keyframes cardFlyIn {
  from { opacity: 0; transform: translateY(60px) scale(0.95); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}
</style>
