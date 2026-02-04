<template>
  <div class="register-page">
    <div class="register-bg">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
      <div class="blob blob-3"></div>
    </div>
    <div class="register-container">
      <div class="register-header">
        <div class="logo-wrapper">
          <el-icon :size="40" class="logo-icon"><Aim /></el-icon>
        </div>
        <h1>志愿者注册</h1>
        <p>加入我们，成为光荣的志愿者</p>
      </div>

      <el-form
          ref="formRef"
          :model="formData"
          :rules="rules"
          class="register-form"
          size="large"
          label-position="top"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学号/用户名" prop="username">
              <el-input v-model="formData.username" placeholder="请输入学号" :prefix-icon="CreditCard" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="真实姓名" prop="name">
              <el-input v-model="formData.name" placeholder="请输入真实姓名" :prefix-icon="User" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="密码" prop="password">
              <el-input v-model="formData.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="formData.confirmPassword" type="password" placeholder="请再次输入密码" :prefix-icon="Lock" show-password />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学院" prop="college">
              <el-select v-model="formData.college" placeholder="请选择学院" @change="handleCollegeChange" style="width: 100%">
                <template #prefix>
                  <el-icon><School /></el-icon>
                </template>
                <el-option
                  v-for="college in Object.keys(COLLEGE_MAJORS)"
                  :key="college"
                  :label="college"
                  :value="college"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="专业" prop="major">
              <el-select v-model="formData.major" placeholder="请选择专业" :disabled="!formData.college" style="width: 100%">
                <template #prefix>
                  <el-icon><Reading /></el-icon>
                </template>
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

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="formData.phone" placeholder="请输入手机号" :prefix-icon="Phone" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input v-model="formData.email" placeholder="请输入邮箱 (选填)" :prefix-icon="Message" />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="terms-row">
          <el-checkbox v-model="agreeTerms">
            我已阅读并同意 <el-link type="primary" :underline="false">《用户服务协议》</el-link>
          </el-checkbox>
        </div>

        <el-form-item>
          <el-button
              type="primary"
              :loading="loading"
              :disabled="!agreeTerms"
              class="register-btn"
              @click="handleRegister"
          >
            {{ loading ? '注册中...' : '注册账号' }}
          </el-button>
        </el-form-item>

        <div class="register-footer">
          <span>已有账号？</span>
          <el-link type="primary" @click="router.push('/login')">立即登录</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { User, Lock, Phone, Message, CreditCard, School, Reading, Aim } from '@element-plus/icons-vue'
import { request } from '@/utils/request'

const router = useRouter()

const formRef = ref<FormInstance>()
const loading = ref(false)
const agreeTerms = ref(false)

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

const formData = reactive({
  username: '',
  name: '',
  password: '',
  confirmPassword: '',
  college: '',
  major: '',
  phone: '',
  email: ''
})

const handleCollegeChange = () => {
  formData.major = ''
}

const validateConfirmPassword = (_rule: any, value: string, callback: any) => {
  if (value !== formData.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const validatePhone = (_rule: any, value: string, callback: any) => {
  if (!value) {
    callback(new Error('请输入手机号'))
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  username: [
    { required: true, message: '请输入学号/用户名', trigger: 'blur' },
    { min: 4, max: 30, message: '长度为4-30个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度2-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  phone: [
    { required: true, validator: validatePhone, trigger: 'blur' }
  ],
  college: [
    { required: true, message: '请选择学院', trigger: 'change' }
  ],
  major: [
    { required: true, message: '请选择专业', trigger: 'change' }
  ]
}

const handleRegister = async () => {
  if (!formRef.value) return
  if (!agreeTerms.value) {
    ElMessage.warning('请先阅读并同意服务协议')
    return
  }

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      await request.post('/auth/register/volunteer', formData)
      ElMessage.success('注册成功！请登录')
      router.push('/login')
    } catch (error) {
      console.error('注册失败:', error)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style lang="scss" scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  padding: 40px 20px;
  background-color: #f0f7f6;
}

.register-bg {
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
    opacity: 0.5;
    animation: move 20s infinite alternate;
  }

  .blob-1 {
    width: 600px;
    height: 600px;
    background: #4db6ac;
    top: -200px;
    right: -100px;
  }

  .blob-2 {
    width: 500px;
    height: 500px;
    background: #80cbc4;
    bottom: -150px;
    left: -100px;
    animation-delay: -5s;
  }

  .blob-3 {
    width: 300px;
    height: 300px;
    background: #b2dfdb;
    top: 40%;
    left: 20%;
    animation-delay: -10s;
  }
}

@keyframes move {
  0% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(40px, -60px) scale(1.1); }
  66% { transform: translate(-30px, 30px) scale(0.9); }
  100% { transform: translate(0, 0) scale(1); }
}

.register-container {
  width: 600px;
  padding: 40px 48px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 28px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.15);
  position: relative;
  z-index: 1;
  animation: scaleIn 0.6s cubic-bezier(0.165, 0.84, 0.44, 1);
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.95) translateY(20px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.register-header {
  text-align: center;
  margin-bottom: 32px;

  .logo-wrapper {
    width: 70px;
    height: 70px;
    background: linear-gradient(135deg, #00BFA6, #4db6ac);
    border-radius: 18px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 16px;
    box-shadow: 0 8px 16px rgba(0, 191, 166, 0.25);
    
    .logo-icon {
      color: white;
    }
  }

  h1 {
    margin: 0 0 6px;
    font-size: 24px;
    color: #2c3e50;
    font-weight: 700;
  }

  p {
    color: #7f8c8d;
    font-size: 14px;
  }
}

.register-form {
  :deep(.el-form-item__label) {
    font-weight: 600;
    color: #4a5568;
    padding-bottom: 4px;
  }

  :deep(.el-input__wrapper), :deep(.el-select__wrapper) {
    background: rgba(255, 255, 255, 0.9);
    border-radius: 12px;
    box-shadow: none !important;
    border: 1.5px solid #edf2f7;
    transition: all 0.3s;
    padding: 2px 12px;

    &.is-focus, &:hover {
      border-color: #00BFA6;
      background: white;
      box-shadow: 0 0 0 4px rgba(0, 191, 166, 0.08) !important;
    }
  }

  .terms-row {
    margin: 8px 0 24px;
    
    :deep(.el-checkbox__label) {
      color: #718096;
      font-size: 13px;
    }
    
    .el-link {
      font-size: 13px;
      color: #00BFA6;
      font-weight: 600;
      vertical-align: baseline;
    }
  }

  .register-btn {
    width: 100%;
    height: 50px;
    font-size: 16px;
    font-weight: 600;
    background: linear-gradient(135deg, #00BFA6, #26a69a);
    border: none;
    border-radius: 14px;
    transition: all 0.3s;

    &:hover:not(:disabled) {
      transform: translateY(-2px);
      box-shadow: 0 10px 20px rgba(0, 191, 166, 0.3);
    }
    
    &:active:not(:disabled) {
      transform: translateY(0);
    }
  }
}

.register-footer {
  text-align: center;
  color: #718096;
  font-size: 14px;
  margin-top: 20px;
  
  .el-link {
    font-weight: 600;
    color: #00BFA6;
    margin-left: 4px;
  }
}
</style>
