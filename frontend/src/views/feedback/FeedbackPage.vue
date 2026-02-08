<template>
  <div class="feedback-page">
    <div class="background-gradient"></div>
    
    <div class="content-wrapper">
      <!-- 主反馈卡片 -->
      <transition name="fade-slide-up" appear>
        <div class="glass-card main-card">
          <div class="card-header">
            <div class="icon-wrapper">
              <el-icon :size="28"><ChatDotRound /></el-icon>
            </div>
            <div class="header-text">
              <h1 class="title">您的声音，是我们进步的动力</h1>
              <p class="subtitle">无论是建议、吐槽还是Bug反馈，我们都渴望倾听</p>
            </div>
          </div>

          <el-form :model="form" label-position="top" class="feedback-form" ref="formRef">
            <!-- 布局填空荡优化这里：反馈类型 -->
            <div class="form-section stagger-1">
              <label class="section-label">您想反馈哪类问题？</label>
              <div class="type-grid">
                <div 
                  v-for="item in typeOptions" 
                  :key="item.value"
                  class="type-card glass-button"
                  :class="{ active: form.type === item.value }"
                  @click="form.type = item.value"
                >
                  <span class="type-label">{{ item.label }}</span>
                  <div class="active-glow" v-if="form.type === item.value"></div>
                </div>
              </div>
            </div>

            <!-- 布局填空荡优化这里：反馈内容 -->
            <div class="form-section stagger-2">
              <div class="input-wrapper">
                <el-input
                  v-model="form.content"
                  type="textarea"
                  :rows="8"
                  placeholder="请详细描述您遇到的问题或建议，您的详细描述有助于我们要快速定位问题..."
                  maxlength="1000"
                  show-word-limit
                  class="custom-textarea"
                />
              </div>
            </div>

            <!-- 布局填空荡优化这里：联系方式 -->
            <div class="form-section stagger-3">
              <div class="input-wrapper">
                <el-input
                  v-model="form.contactInfo"
                  placeholder="请留下您的联系方式（手机号/邮箱），方便我们向您反馈处理结果"
                  class="custom-input"
                >
                  <template #prefix>
                    <el-icon class="input-icon"><iphone /></el-icon>
                  </template>
                </el-input>
              </div>
            </div>

            <!-- 布局填空荡优化这里：提交按钮 -->
            <div class="form-footer stagger-4">
              <div class="agreement">
                <el-checkbox v-model="agreement" size="large">
                  我已阅读并同意 <span class="link">《用户反馈协议》</span>
                </el-checkbox>
              </div>
              
              <el-button 
                type="primary" 
                size="large" 
                class="submit-btn" 
                @click="handleSubmit" 
                :loading="submitting"
                :disabled="!agreement"
              >
                <el-icon class="btn-icon"><Position /></el-icon>
                <span>提交反馈</span>
                <div class="btn-glow"></div>
              </el-button>
            </div>
          </el-form>
        </div>
      </transition>

      <!-- 提示卡片下移 -->
      <transition name="fade-slide-up" appear>
        <div class="glass-card tips-card stagger-5">
          <div class="tips-header">
            <el-icon color="#409eff"><InfoFilled /></el-icon>
            <span>温馨提示</span>
          </div>
          <div class="tips-content">
            <p>我们会认真对待每一条反馈，处理结果将通过站内信通知您。</p>
            <p>如遇紧急账号安全问题，请直接联系人工客服：400-888-8888</p>
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { request } from '@/utils/request'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Position, Iphone, InfoFilled } from '@element-plus/icons-vue'

const submitting = ref(false)
const agreement = ref(true)
const formRef = ref()

const form = reactive({
  type: 'SUGGESTION',
  content: '',
  contactInfo: ''
})

const typeOptions = [
  { label: '💡 功能建议', value: 'SUGGESTION' },
  { label: '🐛 问题BUG', value: 'BUG' },
  { label: '😠 投诉反馈', value: 'COMPLAINT' },
  { label: '📝 其他事项', value: 'OTHER' }
]

const handleSubmit = async () => {
  if (!form.content.trim()) {
    return ElMessage.warning('请填写反馈内容')
  }
  if (!form.contactInfo.trim()) {
    return ElMessage.warning('请填写联系方式')
  }

  submitting.value = true
  try {
    await request.post('/feedback/submit', form)
    ElMessage.success({
      message: '反馈提交成功！我们会尽快处理',
      duration: 3000,
      showClose: true
    })
    // 重置表单
    form.content = ''
    form.contactInfo = ''
    form.type = 'SUGGESTION'
  } catch (error: any) {
    ElMessage.error(error.message || '提交失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
// 变量定义
$primary-color: #00BFA6;
$gradient-bg: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
$card-bg: rgba(255, 255, 255, 0.65);
$card-border: 1px solid rgba(255, 255, 255, 0.4);
$card-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.07);
$text-main: #2c3e50;
$text-secondary: #606266;

.feedback-page {
  position: relative;
  min-height: 100vh;
  width: 100%;
  display: flex;
  justify-content: center;
  padding: 40px 20px;
  overflow-y: auto;
  
  // 背景渐变
  .background-gradient {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    background: linear-gradient(135deg, #e0eafe 0%, #f0fdf4 100%); // 浅灰青渐变
    z-index: -1;
  }
}

.content-wrapper {
  width: 100%;
  max-width: 800px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

// 通用玻璃卡片样式
.glass-card {
  background: $card-bg;
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: $card-border;
  border-radius: 32px;
  box-shadow: $card-shadow;
  padding: 40px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;

  &:hover {
    box-shadow: 0 12px 40px 0 rgba(31, 38, 135, 0.1);
  }
}

// 主卡片头部
.main-card {
  .card-header {
    text-align: center;
    margin-bottom: 40px;
    
    .icon-wrapper {
      width: 64px;
      height: 64px;
      background: linear-gradient(135deg, $primary-color, lighten($primary-color, 20%));
      border-radius: 20px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      margin: 0 auto 20px;
      box-shadow: 0 10px 20px rgba($primary-color, 0.3);
      animation: float 6s ease-in-out infinite;
    }

    .header-text {
      .title {
        font-size: 28px;
        color: $text-main;
        margin: 0 0 12px;
        font-weight: 700;
        letter-spacing: 0.5px;
      }
      
      .subtitle {
        font-size: 16px;
        color: $text-secondary;
        margin: 0;
      }
    }
  }
}

// 表单区域
.feedback-form {
  .form-section {
    margin-bottom: 32px;
    
    .section-label {
      display: block;
      font-size: 16px;
      font-weight: 600;
      color: $text-main;
      margin-bottom: 16px;
    }
  }
}

// 反馈类型选择
.type-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;

  @media (max-width: 768px) {
    grid-template-columns: repeat(2, 1fr);
  }
}

.type-card {
  position: relative;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(0, 0, 0, 0.05);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;

  .type-label {
    font-size: 15px;
    color: $text-secondary;
    font-weight: 500;
    z-index: 2;
    transition: color 0.3s;
  }

  &.active {
    background: white;
    border-color: $primary-color;
    transform: translateY(-2px);
    box-shadow: 0 8px 16px rgba($primary-color, 0.15);

    .type-label {
      color: $primary-color;
      font-weight: 600;
    }

    .active-glow {
      position: absolute;
      inset: 0;
      background: radial-gradient(circle at center, rgba($primary-color, 0.1) 0%, transparent 70%);
      animation: pulse 2s infinite;
    }
  }

  &:hover:not(.active) {
    background: rgba(255, 255, 255, 0.8);
  }
}

// 输入框统一样式
.custom-textarea :deep(.el-textarea__inner),
.custom-input :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(0, 0, 0, 0.05);
  border-radius: 16px;
  box-shadow: none;
  transition: all 0.3s ease;
  font-size: 15px;
  padding: 16px;

  &:hover {
    background: rgba(255, 255, 255, 0.8);
  }

  &.is-focus, &:focus-within {
    background: white;
    border-color: $primary-color;
    box-shadow: 0 0 0 4px rgba($primary-color, 0.1);
  }
}

.custom-textarea :deep(.el-textarea__inner) {
  min-height: 180px !important; // 固定高度
  resize: none;
}

.custom-input :deep(.el-input__wrapper) {
  height: 52px;
}

.input-icon {
  font-size: 20px;
  color: #9aa5b1;
  margin-right: 8px;
}

// 底部按钮区
.form-footer {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
  margin-top: 40px;

  .agreement {
    .link {
      color: $primary-color;
      cursor: pointer;
      &:hover { text-decoration: underline; }
    }
  }

  .submit-btn {
    width: 100%;
    height: 56px;
    border-radius: 28px; // 全圆角
    font-size: 18px;
    font-weight: 600;
    background: linear-gradient(135deg, $primary-color, darken($primary-color, 10%));
    border: none;
    position: relative;
    overflow: hidden;
    transition: transform 0.2s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.2s;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 12px 30px rgba($primary-color, 0.4);
    }

    &:active {
      transform: scale(0.98);
    }

    .btn-icon {
      margin-right: 8px;
      font-size: 20px;
    }

    .btn-glow {
      position: absolute;
      top: -50%;
      left: -50%;
      width: 200%;
      height: 200%;
      background: radial-gradient(circle, rgba(255,255,255,0.3) 0%, transparent 60%);
      transform: scale(0);
      transition: transform 0.5s;
    }

    &:hover .btn-glow {
      transform: scale(1);
      animation: ripple 1s linear infinite;
    }
  }
}

// 底部提示小卡片
.tips-card {
  padding: 24px 32px;
  border-radius: 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;

  .tips-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    color: $text-main;
  }

  .tips-content {
    font-size: 14px;
    color: $text-secondary;
    line-height: 1.6;
    margin: 0;
    p { margin: 4px 0; }
  }
}

// 动画定义
@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

@keyframes pulse {
  0% { opacity: 0.4; transform: scale(0.8); }
  50% { opacity: 0.8; transform: scale(1.1); }
  100% { opacity: 0.4; transform: scale(0.8); }
}

@keyframes ripple {
  0% { transform: scale(0.8); opacity: 1; }
  100% { transform: scale(2); opacity: 0; }
}

// 进场动画
.fade-slide-up-enter-active {
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}
.fade-slide-up-enter-from {
  opacity: 0;
  transform: translateY(40px);
}

// 级联动画延迟
.stagger-1 { animation: slideIn 0.6s ease-out 0.1s both; }
.stagger-2 { animation: slideIn 0.6s ease-out 0.2s both; }
.stagger-3 { animation: slideIn 0.6s ease-out 0.3s both; }
.stagger-4 { animation: slideIn 0.6s ease-out 0.4s both; }
.stagger-5 { animation: slideIn 0.6s ease-out 0.6s both; }

@keyframes slideIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

// 响应式微调
@media (max-width: 768px) {
  .feedback-page { 
    padding: 20px 16px; 
  }
  
  .glass-card {
    padding: 24px 20px;
    border-radius: 24px;
  }

  .header-text .title { font-size: 24px !important; }
}

// 暗黑模式适配 (简单反色 + 调整透明度)
.dark {
  .background-gradient {
    background: linear-gradient(135deg, #1a1c22 0%, #29323c 100%);
  }
  
  .glass-card {
    background: rgba(30, 30, 30, 0.6);
    border-color: rgba(255, 255, 255, 0.1);
    
    .header-text .title { color: #e5eaf3; }
    .header-text .subtitle { color: #a3a6ad; }
    .section-label { color: #e5eaf3; }
  }
  
  .type-card {
    background: rgba(255, 255, 255, 0.05);
    border-color: rgba(255, 255, 255, 0.1);
    .type-label { color: #a3a6ad; }

    &.active {
      background: rgba($primary-color, 0.2);
      .type-label { color: $primary-color; }
    }
  }

  .custom-textarea :deep(.el-textarea__inner),
  .custom-input :deep(.el-input__wrapper) {
    background: rgba(0, 0, 0, 0.2);
    border-color: rgba(255, 255, 255, 0.1);
    color: #e5eaf3;
    
    &:hover { background: rgba(0, 0, 0, 0.4); }
    &.is-focus { 
      background: rgba(0, 0, 0, 0.5); 
      border-color: $primary-color;
    }
  }

  .tips-card {
    .tips-header span { color: #e5eaf3; }
    .tips-content { color: #a3a6ad; }
  }
}
</style>
