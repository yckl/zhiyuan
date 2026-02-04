<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
      <div class="blob blob-3"></div>
    </div>
    <div class="login-container">
      <div class="login-header">
        <div class="logo-wrapper">
          <el-icon :size="48" class="logo-icon"><Aim /></el-icon>
        </div>
        <h1>校园志愿者管理系统</h1>
        <p>Campus Volunteer Management System</p>
      </div>

      <el-form
          ref="formRef"
          :model="formData"
          :rules="rules"
          class="login-form"
          size="large"
      >
        <el-form-item prop="username">
          <el-input
              v-model="formData.username"
              placeholder="请输入用户名/学号"
              :prefix-icon="User"
              clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
              v-model="formData.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
              @keyup.enter="handleLogin"
          />
        </el-form-item>

        <div class="remember-forget">
          <el-checkbox v-model="rememberMe">记住我</el-checkbox>
          <el-link type="primary" :underline="false">忘记密码?</el-link>
        </div>

        <el-form-item>
          <el-button
              type="primary"
              :loading="loading"
              class="login-btn"
              @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>

        <div class="login-footer">
          <span>还没有账号？</span>
          <el-link type="primary" @click="router.push('/register')">立即注册</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { User, Lock, Aim } from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()

const formRef = ref<FormInstance>()
const loading = ref(false)
const rememberMe = ref(false)

const formData = reactive({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 30, message: '用户名长度2-30个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 4, max: 20, message: '密码长度4-20个字符', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const res = await request.post('/auth/login', formData)

      // 使用 Pinia store 保存状态
      const userStore = useUserStore()
      const userInfo = {
        userId: res.data.userId,
        username: res.data.username,
        role: res.data.role,
        avatar: res.data.avatar,
        nickname: res.data.nickname
      }
      userStore.setUser(userInfo, res.data.token)

      if (rememberMe.value) {
        localStorage.setItem('rememberedUser', formData.username)
      } else {
        localStorage.removeItem('rememberedUser')
      }

      ElMessage.success('登录成功，欢迎回来！')

      const redirect = (route.query.redirect as string) || '/'
      router.push(redirect)
    } catch (error: any) {
      console.error('登录失败:', error)
    } finally {
      loading.value = false
    }
  })
}

onMounted(() => {
  const rememberedUser = localStorage.getItem('rememberedUser')
  if (rememberedUser) {
    formData.username = rememberedUser
    rememberMe.value = true
  }
})
</script>

<style lang="scss" scoped>
:root {
  --primary-color: #00BFA6;
  --primary-hover: #00dbad;
  --glass-bg: rgba(255, 255, 255, 0.75);
}

.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background-color: #f0f7f6;
}

.login-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #e0f2f1 0%, #00BFA6 100%);
  z-index: 0;
  
  .blob {
    position: absolute;
    border-radius: 50%;
    filter: blur(80px);
    z-index: 0;
    opacity: 0.6;
    animation: move 20s infinite alternate;
  }

  .blob-1 {
    width: 400px;
    height: 400px;
    background: #4db6ac;
    top: -100px;
    left: -100px;
  }

  .blob-2 {
    width: 500px;
    height: 500px;
    background: #80cbc4;
    bottom: -150px;
    right: -100px;
    animation-delay: -5s;
  }

  .blob-3 {
    width: 300px;
    height: 300px;
    background: #b2dfdb;
    top: 50%;
    left: 40%;
    animation-delay: -10s;
  }
}

@keyframes move {
  0% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -50px) scale(1.1); }
  66% { transform: translate(-20px, 20px) scale(0.9); }
  100% { transform: translate(0, 0) scale(1); }
}

.login-container {
  width: 420px;
  padding: 48px 40px;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 24px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.15);
  position: relative;
  z-index: 1;
  animation: fadeInUp 0.8s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 32px;

  .logo-wrapper {
    width: 80px;
    height: 80px;
    background: linear-gradient(135deg, #00BFA6, #4db6ac);
    border-radius: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 20px;
    box-shadow: 0 10px 20px rgba(0, 191, 166, 0.3);
    
    .logo-icon {
      color: white;
    }
  }

  h1 {
    margin: 0 0 8px;
    font-size: 24px;
    color: #2c3e50;
    font-weight: 700;
  }

  p {
    color: #7f8c8d;
    font-size: 13px;
    letter-spacing: 0.5px;
    text-transform: uppercase;
  }
}

.login-form {
  .remember-forget {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    :deep(.el-checkbox__label) {
      color: #7f8c8d;
    }
  }

  .login-btn {
    width: 100%;
    height: 50px;
    font-size: 16px;
    font-weight: 600;
    background: linear-gradient(135deg, #00BFA6, #26a69a);
    border: none;
    border-radius: 12px;
    transition: all 0.3s;
    letter-spacing: 4px;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 20px rgba(0, 191, 166, 0.3);
    }
    
    &:active {
      transform: translateY(0);
    }
  }

  :deep(.el-input__wrapper) {
    background: rgba(255, 255, 255, 0.8);
    border-radius: 12px;
    box-shadow: none !important;
    border: 1.5px solid #eee;
    transition: all 0.3s;
    padding: 2px 15px;

    &.is-focus {
      border-color: #00BFA6;
      background: white;
      box-shadow: 0 0 0 4px rgba(0, 191, 166, 0.1) !important;
    }
  }
}

.login-footer {
  text-align: center;
  color: #7f8c8d;
  font-size: 14px;
  margin-top: 24px;
  
  .el-link {
    font-weight: 600;
    margin-left: 4px;
    color: #00BFA6;
    
    &:hover {
      color: #00dbad;
    }
  }
}
</style>
