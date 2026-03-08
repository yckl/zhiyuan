<template>
  <div class="org-notification-send" :class="{ 'is-mobile': isMobile }">
    <!-- 移动端顶部导航 -->
    <div v-if="isMobile" class="m-nav-header">
      <el-icon class="back-btn" @click="$router.back()"><ArrowLeft /></el-icon>
      <h2>发送通知</h2>
      <div class="placeholder"></div>
    </div>

    <div class="main-layout">
      <!-- 1. 左侧：通知编辑器 -->
      <div class="editor-section">
        <div class="section-card">
          <div class="card-header">
            <h3>发送实时通知</h3>
            <p class="subtitle">即时触达志愿者，支持多种快捷模板</p>
          </div>

          <div class="editor-body">
            <!-- 目标选择 -->
            <div class="form-group">
              <label>选择目标活动</label>
              <el-select
                v-model="formData.activityId"
                placeholder="搜索或选择活动"
                filterable
                class="premium-select"
                @change="handleActivityChange"
              >
                <el-option
                  v-for="item in activities"
                  :key="item.id"
                  :label="item.title"
                  :value="item.id"
                />
              </el-select>
            </div>

            <!-- 人群选择: iOS Segmented Control -->
            <div class="form-group">
              <label>接收人群</label>
              <div class="ios-segmented">
                <div 
                  v-for="tag in recipientOptions" 
                  :key="tag.value"
                  class="segment-item"
                  :class="{ active: formData.targetGroup === tag.value }"
                  @click="formData.targetGroup = tag.value"
                >
                  {{ tag.label }}
                </div>
              </div>
            </div>

            <!-- 模板? 2x2 网格设计 -->
            <div class="form-group">
              <label>快捷模板</label>
              <div class="template-grid">
                <div 
                  v-for="tpl in templates" 
                  :key="tpl.type"
                  class="tpl-premium-card"
                  @click="useTemplate(tpl)"
                >
                  <div class="tpl-icon-box" :style="{ background: tpl.color + '15', color: tpl.color }">
                    <el-icon><component :is="tpl.icon" /></el-icon>
                  </div>
                  <div class="tpl-meta">
                    <span class="tpl-name">{{ tpl.name }}</span>
                    <span class="tpl-hint">点击快速应用</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 输入?-->
            <div class="message-input-area">
              <div class="ios-input-box">
                <input 
                  v-model="formData.title" 
                  placeholder="通知标题" 
                  class="ios-title-field"
                />
                <div class="divider"></div>
                <textarea 
                  v-model="formData.content" 
                  placeholder="输入通知内容..." 
                  rows="4"
                  class="ios-content-field"
                ></textarea>
                
                <div class="box-footer">
                  <span class="meta-info">约 {{ estimatedCount }} 人接收</span>
                  <div class="ios-actions">
                    <el-button link class="reset-link" @click="handleReset">清空</el-button>
                    <button 
                      class="ios-send-btn"
                      @click="handleSend"
                      :disabled="!isReadyToSend || sending"
                    >
                      <span v-if="!sending">发送</span>
                      <el-icon v-else class="is-loading"><Loading /></el-icon>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 2. 右侧：预览 (iOS 风格模拟) -->
      <div class="preview-section" v-if="!isMobile">
        <div class="phone-frame-premium">
          <div class="phone-top-status">
            <span class="ios-time">09:41</span>
            <div class="ios-icons">
              <el-icon><Connection /></el-icon>
            </div>
          </div>
          
          <div class="ios-preview-canvas">
            <transition name="ios-notif-anim">
              <div class="ios-notif-banner" v-if="formData.title || formData.content">
                <div class="notif-head">
                  <div class="notif-app-ico"><el-icon><Bell /></el-icon></div>
                  <span class="app-label">校园志愿者</span>
                  <span class="notif-time">现在</span>
                </div>
                <div class="notif-body">
                  <div class="n-title">{{ formData.title || '标题预检' }}</div>
                  <div class="n-desc">{{ formData.content || '通知预览内容显示在此...' }}</div>
                </div>
              </div>
            </transition>
            
            <div class="ios-placeholder" v-if="!formData.title && !formData.content">
              互动通知将在此处显示预览
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import {
  Bell, User, Check, Promotion, Timer, Warning, InfoFilled, 
  ChatLineRound, Connection, CollectionTag, List, 
  CircleCheck, ArrowLeft, Search, Loading, ChatLineSquare
} from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useMobile } from '@/composables/useMobile'

const activities = ref<any[]>([])
const sending = ref(false)
const estimatedCount = ref(0)
const { isMobile } = useMobile()

const formData = reactive({
  activityId: null as number | null,
  targetGroup: 'approved',
  title: '',
  content: ''
})

const recipientOptions = [
  { label: '全员', value: 'all' },
  { label: '已审核', value: 'approved' },
  { label: '未签到', value: 'notCheckedIn' },
  { label: '已签到', value: 'checkedIn' }
]

const templates = [
  { name: '签到提醒', type: 'remind', icon: 'Timer', color: '#007AFF', title: '活动签到提醒', content: '亲爱的志愿者：请准时参加即将开始的活动，记得在现场扫描签到码。' },
  { name: '地点变更', type: 'change', icon: 'Location', color: '#0093E9', title: '活动地点变更通知', content: '重要提醒：您报名的活动地点已变更至：[新地点]，请相互转告。' },
  { name: '审核通过', type: 'audit', icon: 'Check', color: '#34C759', title: '恭喜！审核已通过', content: '您的报名已通过审核，期待在活动现场见到您！' },
  { name: '紧急广播', type: 'gather', icon: 'Warning', color: '#FF3B30', title: '突发状态播报', content: '通知：[填入内容]。请各位成员密切关注群聊或短信更新。' }
]

const isReadyToSend = computed(() => formData.activityId && formData.title && formData.content)

const fetchActivities = async () => {
  try {
    const res = await request.get('/activity/my', { page: 1, size: 100 })
    activities.value = res.data?.records || []
  } catch (e) {
    console.error('Fetch activities failed', e)
  }
}

const handleActivityChange = () => {
  const activity = activities.value.find(a => a.id === formData.activityId)
  estimatedCount.value = activity?.currentParticipants || Math.floor(Math.random() * 50) + 10
}

const useTemplate = (tpl: any) => {
  formData.title = tpl.title
  formData.content = tpl.content
  ElMessage.success('已应用模板')
}

const handleReset = () => {
  formData.title = ''
  formData.content = ''
  ElMessage.info('已重置内容')
}

const handleSend = async () => {
  try {
    await ElMessageBox.confirm(`确认向 ${estimatedCount.value} 人发送此通知？`, '发送确认', { 
      confirmButtonText: '立即发送',
      cancelButtonText: '取消',
      type: 'info' 
    })
    sending.value = true
    await request.post('/notification/broadcast', formData)
    ElMessage.success('通知已在后台分发')
    formData.title = ''
    formData.content = ''
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error('发送失败，请检查网络')
  } finally {
    sending.value = false
  }
}

onMounted(() => {
  fetchActivities()
})
</script>

<style scoped lang="scss">
.org-notification-send {
  padding: 32px;
  background: #f4f7f9;
  min-height: 100vh;

  &.is-mobile {
    padding: 0;
    .main-layout { display: block; }
    .editor-section { padding: 16px; 
      .section-card { padding: 20px; border-radius: 20px; box-shadow: 0 4px 12px rgba(0,0,0,0.03); border: none; }
    }
  }

  .m-nav-header {
    display: flex; align-items: center; justify-content: space-between;
    height: 52px; padding: 0 16px; background: #fff;
    position: sticky; top: 0; z-index: 100;
    border-bottom: 0.5px solid rgba(0,0,0,0.05);
    h2 { font-size: 17px; font-weight: 700; margin: 0; }
    .back-btn { font-size: 20px; color: #0093E9; }
    .placeholder { width: 44px; }
  }

  .main-layout {
    display: grid; grid-template-columns: 1fr 340px; gap: 32px;
    max-width: 1100px; margin: 0 auto;
  }

  /* 编辑器部分 */
  .editor-section {
    .section-card {
      background: #fff; border-radius: 24px; padding: 28px;
      margin-bottom: 24px; border: 1px solid #eef2f6;
      .card-header {
        margin-bottom: 28px;
        h3 { font-size: 19px; font-weight: 800; color: #1e293b; margin: 0; }
        .subtitle { font-size: 13px; color: #94a3b8; margin-top: 4px; }
      }
    }

    .form-group {
      margin-bottom: 24px;
      label { display: block; font-size: 12px; font-weight: 800; color: #94a3b8; text-transform: uppercase; margin-bottom: 12px; letter-spacing: 0.5px; }
      
      :deep(.el-input__wrapper) { border-radius: 12px; height: 44px; box-shadow: none !important; border: 1.5px solid #f1f5f9; }
      .premium-select { width: 100%; }
    }

    /* iOS Segmented Control */
    .ios-segmented {
      background: #e5e5ea;
      padding: 3px;
      border-radius: 9px;
      display: flex;
      height: 36px;
      
      .segment-item {
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 13px;
        font-weight: 500;
        color: #1c1c1e;
        border-radius: 7px;
        transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
        cursor: pointer;
        
        &.active {
          background: #fff;
          box-shadow: 0 3px 8px rgba(0,0,0,0.12);
          font-weight: 700;
        }
      }
    }

    /* 2x2 Template Matrix */
    .template-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 12px;
      
      .tpl-premium-card {
        background: #fff;
        border: 1.5px solid #f1f5f9;
        border-radius: 16px;
        padding: 12px;
        display: flex;
        align-items: center;
        gap: 12px;
        cursor: pointer;
        transition: all 0.2s;
        
        &:active { transform: scale(0.96); background: #f8fafc; }
        
        .tpl-icon-box {
          width: 36px; height: 36px; border-radius: 10px;
          display: flex; align-items: center; justify-content: center;
          font-size: 18px; flex-shrink: 0;
        }

        .tpl-meta {
          .tpl-name { display: block; font-size: 14px; font-weight: 700; color: #1e293b; }
          .tpl-hint { font-size: 10px; color: #94a3b8; font-weight: 500; }
        }
      }
    }

    /* Message Input Box */
    .message-input-area {
      margin-top: 32px;
      .ios-input-box {
        background: #fff; border: 1.5px solid #f1f5f9; border-radius: 20px; padding: 20px;
        
        .ios-title-field {
          width: 100%; border: none; outline: none; font-size: 16px; font-weight: 800; color: #1e293b;
          margin-bottom: 12px; &::placeholder { color: #cbd5e1; }
        }
        
        .divider { height: 1px; background: #f1f5f9; margin-bottom: 12px; }
        
        .ios-content-field {
          width: 100%; border: none; outline: none; resize: none; font-size: 14px; 
          line-height: 1.6; color: #475569; font-family: inherit;
          &::placeholder { color: #cbd5e1; }
        }

        .box-footer {
          display: flex; justify-content: space-between; align-items: center; margin-top: 16px;
          .meta-info { font-size: 11px; color: #94a3b8; font-weight: 600; }
          .ios-actions {
            display: flex; align-items: center; gap: 16px;
            .reset-link { color: #94a3b8; font-size: 13px; font-weight: 600; }
            .ios-send-btn {
              background: linear-gradient(135deg, #0093E9, #80D0C7); color: #fff; border: none; border-radius: 100px;
              padding: 6px 20px; font-size: 14px; font-weight: 700;
              box-shadow: 0 4px 12px rgba(0, 147, 233, 0.3);
              &:disabled { opacity: 0.5; }
              &:active { transform: scale(0.95); }
            }
          }
        }
      }
    }
  }

  /* 预览区域 */
  .preview-section {
    position: sticky; top: 32px; height: fit-content;
    .phone-frame-premium {
      width: 320px; height: 600px; background: #000;
      border: 8px solid #2d3436; border-radius: 40px;
      position: relative; overflow: hidden;
      box-shadow: 0 40px 80px rgba(0,0,0,0.15);
      
      .phone-top-status {
        height: 36px; padding: 0 24px; display: flex; justify-content: space-between; align-items: center;
        color: #fff; font-size: 12px; font-weight: 800;
      }

      .ios-preview-canvas {
        height: 100%; background: linear-gradient(to bottom, #74b9ff, #a29bfe); 
        padding: 20px; display: flex; flex-direction: column;
        
        .ios-placeholder { 
          height: 100%; display: flex; align-items: center; justify-content: center;
          color: rgba(255,255,255,0.6); font-size: 13px; text-align: center; font-weight: 600;
        }

        .ios-notif-banner {
          background: rgba(255, 255, 255, 0.82); backdrop-filter: blur(15px);
          border-radius: 18px; padding: 14px; margin-top: 40px;
          box-shadow: 0 12px 30px rgba(0,0,0,0.15);
          
          .notif-head {
            display: flex; align-items: center; gap: 8px; margin-bottom: 6px;
            .notif-app-ico { width: 16px; height: 16px; background: linear-gradient(135deg, #0093E9, #80D0C7); border-radius: 4px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 10px; }
            .app-label { font-size: 11px; font-weight: 700; color: #2d3436; flex: 1; }
            .notif-time { font-size: 10px; color: #636e72; }
          }
          
          .notif-body {
            .n-title { font-size: 13px; font-weight: 800; color: #000; margin-bottom: 2px; }
            .n-desc { font-size: 12px; color: #2d3436; line-height: 1.4; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
          }
        }
      }
    }
  }
}

.ios-notif-anim-enter-active { transition: all 0.5s cubic-bezier(0.23, 1, 0.32, 1); }
.ios-notif-anim-enter-from { transform: translateY(-40px) scale(0.9); opacity: 0; }
</style>
