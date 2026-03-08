<template>
  <div class="register-wrapper">
    <!-- 响应式背景资产 (使用 img 标签提高加载优先级并防止白闪) -->
    <img 
      :src="bgSrc" 
      class="auth-bg" 
      :class="{ 'is-loaded': bgLoaded }"
      @load="bgLoaded = true"
      alt="background"
    >
    
    <div class="glass-card">
      <!-- ====== SVG 吉祥物 ====== -->
      <VolunteerMascot ref="mascotRef" />

      <div class="register-header">
        <h1>志愿者注</h1>
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
              <el-input 
                v-model="formData.username" 
                placeholder="请输入学号" 
                :prefix-icon="CreditCard"
                @focus="onInputFocus(formData.username)"
                @blur="onInputBlur"
                @input="onTypingInput(formData.username)"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="真实姓名" prop="name">
              <el-input 
                v-model="formData.name" 
                placeholder="请输入真实姓名" 
                :prefix-icon="User"
                @focus="onInputFocus(formData.name)"
                @blur="onInputBlur"
                @input="onTypingInput(formData.name)"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="密码" prop="password">
              <el-input 
                v-model="formData.password" 
                type="password" 
                placeholder="请输入密码" 
                :prefix-icon="Lock" 
                show-password 
                @focus="onPasswordFocus"
                @blur="onPasswordBlur"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input 
                v-model="formData.confirmPassword" 
                type="password" 
                placeholder="请再次输入密码" 
                :prefix-icon="Lock" 
                show-password 
                @focus="onPasswordFocus"
                @blur="onPasswordBlur"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学院" prop="college">
              <el-select 
                v-model="formData.college" 
                placeholder="请选择学院" 
                @change="handleCollegeChange" 
                @focus="onInputFocus(formData.college)"
                @blur="onInputBlur"
                style="width: 100%"
              >
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
              <el-select 
                v-model="formData.major" 
                placeholder="请选择专业" 
                :disabled="!formData.college" 
                @focus="onInputFocus(formData.major)"
                @blur="onInputBlur"
                style="width: 100%"
              >
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
            <el-form-item label="手机号码" prop="phone">
              <el-input 
                v-model="formData.phone" 
                placeholder="请输入手机号" 
                :prefix-icon="Phone" 
                @focus="onInputFocus(formData.phone)"
                @blur="onInputBlur"
                @input="onTypingInput(formData.phone)"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input 
                v-model="formData.email" 
                placeholder="请输入邮箱(选填)" 
                :prefix-icon="Message" 
                @focus="onInputFocus(formData.email)"
                @blur="onInputBlur"
                @input="onTypingInput(formData.email)"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="terms-row">
          <el-checkbox v-model="agreeTerms">
            我已阅读并同意<el-link type="primary" :underline="false">《用户服务协议》</el-link>
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
          <span>已有账号</span>
          <el-link type="primary" @click="router.push('/login')">立即登录</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { User, Lock, Phone, Message, CreditCard, School, Reading } from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import VolunteerMascot from '@/components/VolunteerMascot.vue'

// 导入背景图片资源，确保 Vite 正确解析
import pcBg from '@/assets/images/pc-login-bg.jpg'
import mobileBg from '@/assets/images/mobile-login-bg.jpg'

const router = useRouter()

const formRef = ref<FormInstance>()
const loading = ref(false)
const agreeTerms = ref(false)

// 响应式判断手机端
const windowWidth = ref(window.innerWidth)
const isMobile = computed(() => windowWidth.value <= 768)
const handleResize = () => { windowWidth.value = window.innerWidth }
onMounted(() => window.addEventListener('resize', handleResize))
onUnmounted(() => window.removeEventListener('resize', handleResize))

const bgSrc = computed(() => isMobile.value ? mobileBg : pcBg)
const bgLoaded = ref(false)

const COLLEGE_MAJORS = {
  '计算机学院': ['软件工程', '计算机科学', '人工智能'],
  '电子工程学院': ['电子信息', '通信工程', '微电子学'],
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
    callback(new Error('请输入正确的手机号码'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  username: [
    { required: true, message: '请输入学号或用户名', trigger: 'blur' },
    { min: 4, max: 30, message: '长度在4-30个字符之间', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在2-20个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符之间', trigger: 'blur' }
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

// ==================== 吉祥物交?====================
const mascotRef = ref<InstanceType<typeof VolunteerMascot>>()
let isPasswordFocused = false

const onInputFocus = (val: string) => {
  mascotRef.value?.setTyping(val ? val.length : 0)
}

const onTypingInput = (val: string) => {
  mascotRef.value?.setTyping(val ? val.length : 0)
}

const onInputBlur = () => {
  if (!isPasswordFocused) {
    mascotRef.value?.setIdle()
  }
}

const onPasswordFocus = () => {
  isPasswordFocused = true
  mascotRef.value?.setPassword()
}

const onPasswordBlur = () => {
  isPasswordFocused = false
  mascotRef.value?.setIdle()
}

</script>

<style lang="scss" scoped>
/* ====== Premium 背景与布局 (与登录页同步) ====== */
.register-wrapper {
  min-height: 100vh;
  width: 100vw;
  display: flex;
  position: relative;
  overflow-y: auto;
  overflow-x: hidden;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  background-color: #334155; /* 深蓝灰色底色 */
  justify-content: center;
  align-items: center;
  padding: 64px 24px;
  
  @media (max-width: 768px) {
    align-items: flex-end;
    padding-bottom: 5vh;
    padding-top: 5vh;
  }
}

.auth-bg {
  position: fixed;
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

/* ====== Premium Glassmorphism 卡片 ====== */
.glass-card {
  width: 680px;
  max-width: 100%;
  padding: 10px 48px 40px;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 24px;
  box-shadow: 0 15px 50px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 1;
  animation: cardFlyIn 0.8s cubic-bezier(0.16, 1, 0.3, 1);
  
  @media (max-width: 768px) {
    width: 92%;
    background: rgba(255, 255, 255, 0.85);
    padding: 16px 24px 32px;
  }
}

@keyframes cardFlyIn {
  from { opacity: 0; transform: translateY(60px) scale(0.95); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

.register-header {
  text-align: center;
  margin-bottom: 24px;

  h1 {
    margin: 0 0 4px;
    font-size: 24px;
    color: #1a1a1a;
    font-weight: 800;
  }

  p {
    color: #999;
    font-size: 13px;
    letter-spacing: 0.5px;
  }
}

/* ====== 表单美化 ====== */
.register-form {
  :deep(.el-form-item__label) {
    font-weight: 700;
    color: #444;
    padding-bottom: 4px;
    font-size: 13px;
  }

  :deep(.el-input__wrapper), :deep(.el-select__wrapper) {
    background: rgba(0, 0, 0, 0.04);
    border-radius: 12px;
    box-shadow: none !important;
    border: none;
    transition: all 0.3s;
    padding: 2px 12px;

    &.is-focus, &:hover {
      background: white;
      box-shadow: 0 0 0 1px #1DE9B6, 0 0 0 3px rgba(29, 233, 182, 0.15) !important;
    }
  }

  .terms-row {
    margin: 8px 0 24px;
    
    :deep(.el-checkbox__label) {
      color: #999;
      font-size: 13px;
    }
    
    .el-link {
      font-size: 13px;
      color: #00B4DB;
      font-weight: 700;
      &:hover { color: #1DE9B6; }
    }
    
    :deep(.el-checkbox.is-checked .el-checkbox__inner) {
      background-color: #1DE9B6;
      border-color: #1DE9B6;
    }
  }

  .register-btn {
    width: 100%;
    height: 48px;
    font-size: 16px;
    font-weight: 700;
    background: linear-gradient(135deg, #1DE9B6 0%, #00B4DB 100%);
    border: none;
    border-radius: 14px;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    box-shadow: 0 4px 15px rgba(29, 233, 182, 0.2);
    color: white;

    &:hover:not(:disabled) {
      opacity: 0.9;
      transform: translateY(-2px);
      box-shadow: 0 8px 25px rgba(29, 233, 182, 0.3);
    }
    
    &:active:not(:disabled) {
      transform: translateY(0);
      transform: scale(0.98);
    }
  }
}

.register-footer {
  text-align: center;
  color: #aaa;
  font-size: 14px;
  margin-top: 20px;
  
  .el-link {
    font-weight: 700;
    color: #1DE9B6;
    margin-left: 4px;
    &:hover { color: #00B4DB; }
  }
}

@media (max-width: 768px) {
  :deep(.el-row) {
    display: flex;
    flex-direction: column;
  }
  
  :deep(.el-col) {
    width: 100% !important;
    max-width: 100% !important;
    padding-left: 0 !important;
    padding-right: 0 !important;
  }
}
</style>
