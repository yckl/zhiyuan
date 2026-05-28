<template>
  <div class="feedback-page">
    <div class="background-gradient"></div>

    <div class="content-wrapper">

      <!-- ==================== 顶部 ==================== -->
      <div class="topbar">
        <span class="topbar-title">📮 问题反馈</span>
      </div>

      <!-- ==================== 主表单卡片 ==================== -->
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
            <!-- 反馈类型 -->
            <div class="form-section stagger-1">
              <label class="section-label">您想反馈哪类问题？</label>
              <div class="type-grid-premium">
                <div
                  v-for="item in typeOptions"
                  :key="item.value"
                  class="type-card-premium"
                  :class="{ active: form.type === item.value }"
                  @click="form.type = item.value"
                >
                  <div class="icon-box" :style="{ background: item.bg }">
                    <el-icon :size="24"><component :is="item.icon" /></el-icon>
                  </div>
                  <span class="type-label">{{ item.label }}</span>
                  <div class="selection-indicator"></div>
                </div>
              </div>
            </div>

            <!-- 反馈内容 -->
            <div class="form-section stagger-2">
              <label class="section-label">反馈内容</label>
              <div class="input-wrapper">
                <el-input
                  v-model="form.content"
                  type="textarea"
                  :rows="8"
                  placeholder="请详细描述您遇到的问题或建议..."
                  maxlength="1000"
                  class="custom-textarea"
                />
                <div class="word-count-bar">
                  <el-tag
                    :type="wordCountType"
                    size="small"
                    round
                    effect="plain"
                  >{{ form.content.length }} / 1000 </el-tag>
                </div>
              </div>
            </div>

            <!-- 联系方式 -->
            <div class="form-section stagger-3">
              <label class="section-label">联系方式</label>
              <div class="input-wrapper">
                <el-input
                  v-model="form.contactInfo"
                  placeholder="手机号或邮箱，方便我们反馈处理结果"
                  class="custom-input"
                >
                  <template #prefix>
                    <el-icon class="input-icon"><Iphone /></el-icon>
                  </template>
                </el-input>
              </div>
            </div>

            <!-- 提交区域 (合并至卡片内部) -->
            <div class="form-submit-area stagger-4">
              <div class="agreement-area">
                <el-checkbox v-model="agreement" size="small">
                  我已阅读并同意<span class="link">《用户反馈协议》</span>
                </el-checkbox>
              </div>
              <button
                type="button"
                class="submit-btn-premium"
                :disabled="!agreement || submitting"
                @click="handleSubmit"
              >
                <el-icon class="btn-icon"><Position /></el-icon>
                <span>{{ submitting ? '提交中...' : '提交反馈' }}</span>
              </button>
            </div>
          </el-form>
        </div>
      </transition>

      <!-- 温馨提示 -->
      <transition name="fade-slide-up" appear>
        <div class="glass-card tips-card stagger-5">
          <div class="tips-header">
            <el-icon color="#409eff"><InfoFilled /></el-icon>
            <span>温馨提示</span>
          </div>
          <div class="tips-content">
            <p>我们会认真对待每一条反馈，处理结果将通过站内信通知您</p>
            <p>如遇紧急账号安全问题，请直接联系人工客服：400-888-8888</p>
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { request } from '@/utils/request'
import { ElMessage } from 'element-plus'
import {
  ChatDotRound, Position, Iphone, InfoFilled,
  Opportunity, CircleClose, Warning, MoreFilled
} from '@element-plus/icons-vue'

const submitting = ref(false)
const agreement = ref(true)

const form = reactive({
  type: 'SUGGESTION',
  content: '',
  contactInfo: ''
})

const typeOptions = [
  { label: '功能建议', value: 'SUGGESTION', icon: Opportunity, bg: 'linear-gradient(135deg, #FFD93D, #FF9100)' },
  { label: '问题BUG', value: 'BUG', icon: CircleClose, bg: 'linear-gradient(135deg, #FF6B6B, #EE5A24)' },
  { label: '投诉反馈', value: 'COMPLAINT', icon: Warning, bg: 'linear-gradient(135deg, #A29BFE, #6C5CE7)' },
  { label: '其他事项', value: 'OTHER', icon: MoreFilled, bg: 'linear-gradient(135deg, #95A5A6, #7F8C8D)' }
]

const wordCountType = computed(() => {
  if (form.content.length > 900) return 'danger'
  if (form.content.length > 700) return 'warning'
  return 'info'
})

const handleSubmit = async () => {
  try {
    if (!form.content.trim()) return ElMessage.warning('请填写反馈内容')
    if (!form.contactInfo.trim()) return ElMessage.warning('请填写联系方式')
    submitting.value = true
    await request.post('/feedback/submit', form)
    ElMessage.success({
      message: '🎉 反馈提交成功！我们会尽快处理',
      duration: 3000,
      showClose: true
    })
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
$primary-color: #00BF9E; // Updated primary color to teal
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
  padding: 0 20px 100px; /* 确保不被底部导航栏遮挡 */
  overflow-y: auto;

  .background-gradient {
    position: fixed;
    top: 0; left: 0;
    width: 100vw; height: 100vh;
    background: linear-gradient(135deg, #e0eafe 0%, #f0fdf4 100%);
    z-index: -1;
  }
}

// ================================================================
// 顶部?
// ================================================================
.topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0 12px;

  .topbar-title {
    font-size: 18px;
    font-weight: 700;
    color: $text-main;
  }
}

.content-wrapper {
  width: 100%;
  max-width: 800px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.glass-card {
  background: $card-bg;
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: $card-border;
  border-radius: 24px;
  box-shadow: $card-shadow;
  padding: 32px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;

  &:hover { box-shadow: 0 12px 40px 0 rgba(31, 38, 135, 0.1); }
}

.main-card {
  .card-header {
    text-align: center;
    margin-bottom: 32px;

    .icon-wrapper {
      width: 56px; height: 56px;
      background: linear-gradient(135deg, $primary-color, lighten($primary-color, 15%));
      border-radius: 16px;
      display: flex; align-items: center; justify-content: center;
      color: white;
      margin: 0 auto 16px;
      box-shadow: 0 8px 16px rgba($primary-color, 0.3);
      animation: float 6s ease-in-out infinite;
    }

    .title { font-size: 24px; color: $text-main; margin: 0 0 8px; font-weight: 700; }
    .subtitle { font-size: 14px; color: $text-secondary; margin: 0; }
  }
}

.feedback-form {
  .form-section {
    margin-bottom: 24px;

    .section-label {
      display: block;
      font-size: 14px;
      font-weight: 600;
      color: $text-main;
      margin-bottom: 12px;
    }
  }
}

.type-grid-premium {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;

  @media (max-width: 600px) { grid-template-columns: repeat(2, 1fr); }
}

.type-card-premium {
  position: relative;
  background: rgba(255, 255, 255, 0.6);
  border: 1.5px solid transparent;
  border-radius: 18px;
  padding: 16px 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.2, 0.8, 0.2, 1);

  .icon-box {
    width: 48px;
    height: 48px;
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    transition: transform 0.3s;
  }

  .type-label {
    font-size: 13px;
    font-weight: 600;
    color: #475569;
  }

  &.active {
    background: #fff;
    border-color: var(--primary-color);
    box-shadow: 0 8px 24px rgba(0, 191, 166, 0.12);
    transform: translateY(-4px);

    .icon-box { transform: scale(1.1); }
    .type-label { color: var(--primary-color); }

    .selection-indicator {
      position: absolute;
      top: 8px;
      right: 8px;
      width: 8px;
      height: 8px;
      background: var(--primary-color);
      border-radius: 50%;
      box-shadow: 0 0 10px rgba(0, 191, 166, 0.6);
    }
  }

  &:hover:not(.active) {
    background: rgba(255, 255, 255, 0.9);
    transform: translateY(-2px);
  }
}

.custom-textarea :deep(.el-textarea__inner),
.custom-input :deep(.el-input__wrapper) {
  background: #F5F7FA !important;
  border: 1.5px solid transparent !important;
  border-radius: 14px !important;
  box-shadow: none !important;
  transition: all 0.25s !important;
  font-size: 14px;
  padding: 14px;

  &:hover { background: #EEF1F6 !important; }

  &.is-focus, &:focus-within {
    background: white !important;
    border-color: var(--primary-color) !important;
    box-shadow: 0 0 0 4px rgba(0, 191, 166, 0.1) !important;
  }
}

.custom-textarea :deep(.el-textarea__inner) { min-height: 160px !important; resize: none; }
.custom-input :deep(.el-input__wrapper) { height: 48px; }
.input-icon { font-size: 18px; color: #9aa5b1; margin-right: 6px; }

.word-count-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 6px;
}

// ================================================================
// 表单提交区域 (PC 与 移动端适配)
// ================================================================
.form-submit-area {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px dashed #ebeef5; /* 增加层次感 */

  .agreement-area {
    font-size: 13px;
    color: #909399;
    .link {
      color: var(--el-color-primary);
      cursor: pointer;
      font-weight: 600;
      transition: opacity 0.2s;
      &:hover { opacity: 0.8; }
    }
    :deep(.el-checkbox__label) {
      font-size: 13px;
      color: #909399;
    }
  }

  .submit-btn-premium {
    width: 140px;
    height: 40px;
    border: none;
    background: var(--el-color-primary);
    color: #fff;
    border-radius: 8px;
    font-size: 16px;
    font-weight: 600;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    cursor: pointer;
    transition: all 0.3s;
    box-shadow: none; /* 移除绿色发光阴影 */

    &:hover:not(:disabled) {
      filter: brightness(1.1);
      box-shadow: 0 4px 12px rgba(0, 180, 182, 0.2); /* 轻微底部阴影 */
      transform: translateY(-1px);
    }
    &:active:not(:disabled) { transform: translateY(0); }
    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }
}

/* 移动端适配 */
@media (max-width: 768px) {
  .form-submit-area {
    flex-direction: column;
    align-items: stretch;
    margin-top: 24px;
    border-top: none;
    padding-top: 0;

    .agreement-area {
      display: flex;
      justify-content: center;
      margin-bottom: 16px;
      text-align: center;
    }

    .submit-btn-premium {
      width: 100%;
      height: 48px;
      border-radius: 24px; /* 胶囊形状 */
      max-width: none;
    }
  }
}

.tips-card {
  padding: 20px 24px;
  border-radius: 20px;
  .tips-header { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: $text-main; }
  .tips-content { font-size: 13px; color: $text-secondary; line-height: 1.6; p { margin: 4px 0; } }
}



// ================================================================
// Animations
// ================================================================
@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

@keyframes pulse {
  0% { opacity: 0.4; transform: scale(0.8); }
  50% { opacity: 0.8; transform: scale(1.1); }
  100% { opacity: 0.4; transform: scale(0.8); }
}

.fade-slide-up-enter-active { transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1); }
.fade-slide-up-enter-from { opacity: 0; transform: translateY(40px); }

.stagger-1 { animation: slideIn 0.6s ease-out 0.1s both; }
.stagger-2 { animation: slideIn 0.6s ease-out 0.2s both; }
.stagger-3 { animation: slideIn 0.6s ease-out 0.3s both; }
.stagger-4 { animation: slideIn 0.6s ease-out 0.4s both; }
.stagger-5 { animation: slideIn 0.6s ease-out 0.6s both; }

@keyframes slideIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

// ================================================================
// 响应式
// ================================================================
@media (max-width: 768px) {
  .feedback-page { padding: 0 16px 32px; }
  .glass-card { padding: 20px 16px; border-radius: 20px; }
  .main-card .card-header .title { font-size: 20px !important; }

  .preview-drawer {
    :deep(.el-drawer) { width: 85% !important; }
  }

  .main-card { margin-bottom: 20px; }
  
  .feedback-footer-glass {
    flex-direction: column;
    gap: 16px;
    padding: 20px;
    .submit-btn-premium { max-width: 100%; width: 100%; }
    .agreement-area { margin-right: 0; }
  }
}

// ================================================================
// 暗黑模式
// ================================================================
.dark {
  .background-gradient { background: linear-gradient(135deg, #1a1c22 0%, #29323c 100%); }

  .glass-card {
    background: rgba(30, 30, 30, 0.6);
    border-color: rgba(255, 255, 255, 0.1);
    .title { color: #e5eaf3; }
    .subtitle, .section-label { color: #a3a6ad; }
  }

  .type-card {
    background: rgba(255, 255, 255, 0.05);
    border-color: rgba(255, 255, 255, 0.1);
    .type-label { color: #a3a6ad; }
    &.active { background: rgba($primary-color, 0.2); .type-label { color: $primary-color; } }
  }

  .custom-textarea :deep(.el-textarea__inner),
  .custom-input :deep(.el-input__wrapper) {
    background: rgba(0, 0, 0, 0.2);
    border-color: rgba(255, 255, 255, 0.1);
    color: #e5eaf3;
    &:hover { background: rgba(0, 0, 0, 0.4); }
    &.is-focus { background: rgba(0, 0, 0, 0.5); border-color: $primary-color; }
  }
}
</style>
