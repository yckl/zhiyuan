<template>
  <div class="admin-profile-container">
    <div class="profile-header-bg"></div>
    <div class="profile-content">
      <el-card class="glass-card" :body-style="{ padding: '0' }">
        <div class="user-header">
          <div class="avatar-box" @click="triggerUpload">
            <el-avatar :size="100" :src="avatarWithTimestamp" class="premium-avatar">
              <el-icon :size="40"><UserIcon /></el-icon>
            </el-avatar>
            <div class="camera-icon"><el-icon><Camera /></el-icon></div>
            <input ref="fileInput" type="file" accept="image/*" class="hidden-file" @change="handleFileChange" />
          </div>
          <h2 class="user-title">{{ form.nickname || 'Admin' }}</h2>
          <div class="user-badge">{{ getRoleName }}</div>
        </div>
        <div class="form-body">
          <el-form :model="form" label-position="top">
            <div class="section-title">基础信息</div>
            <el-row :gutter="20">
              <el-col :span="12" :xs="24">
                <el-form-item label="昵称">
                  <el-input v-model="form.nickname" class="premium-input" placeholder="请输入昵称" />
                </el-form-item>
              </el-col>
              <el-col :span="12" :xs="24">
                <el-form-item label="联系电话">
                  <el-input v-model="form.phone" class="premium-input" placeholder="请输入电话" />
                </el-form-item>
              </el-col>
            </el-row>
            <div class="section-title mt-4">安全设置</div>
            <el-form-item label="新密码">
              <el-input v-model="form.newPassword" type="password" show-password class="premium-input" placeholder="不修改请留空" />
            </el-form-item>
            <div class="action-area">
              <el-button type="primary" class="save-btn is-round" :loading="loading" @click="handleSave">
                保存更改
              </el-button>
            </div>
          </el-form>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Camera, User as UserIcon } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { request } from '@/utils/request'
import { useUserStore } from '@/stores/user'

const loading = ref(false)
const fileInput = ref<HTMLInputElement>()
const avatarTimestamp = ref(Date.now())
const userStore = useUserStore()

const form = reactive({
  avatar: '',
  nickname: '',
  phone: '',
  newPassword: '',
  username: ''
})

const getRoleName = computed(() => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return userInfo.role === 'ADMIN' ? '系统管理员' : '普通管理员'
})

const avatarWithTimestamp = computed(() => {
  const url = request.resolveUrl(form.avatar)
  if (!url) return ''
  return `${url}${url.includes('?') ? '&' : '?'}t=${avatarTimestamp.value}`
})

const fetchProfile = async () => {
  try {
    const res = await request.get('/auth/current')
    if (res.code === 200 && res.data) {
      Object.assign(form, {
        avatar: res.data.avatar || '',
        nickname: res.data.nickname || res.data.username || '管理员',
        phone: res.data.phone || '',
        username: res.data.username
      })
    }
  } catch (e) {
    console.error('获取基本信息失败:', e)
  }
}

const triggerUpload = () => fileInput.value?.click()

const handleFileChange = async (event: Event) => {
  const input = event.target as HTMLInputElement
  if (input.files?.[0]) {
    const file = input.files[0]
    if (file.size > 2 * 1024 * 1024) {
      ElMessage.warning('图片不能超过 2MB')
      return
    }

    loading.value = true
    try {
      const res = await request.upload('/file/upload/avatar', file)
      if (res.code === 200) {
        const url = res.data.url || res.data
        form.avatar = url
        avatarTimestamp.value = Date.now()
        
        // 同步到 Store 和 LocalStorage
        userStore.setAvatar(url)
        const ui = JSON.parse(localStorage.getItem('userInfo') || '{}')
        ui.avatar = url
        localStorage.setItem('userInfo', JSON.stringify(ui))
        
        ElMessage.success('头像上传成功')
      }
    } catch (e) {
      console.error('上传失败:', e)
      ElMessage.error('上传失败')
    } finally {
      loading.value = false
    }
  }
}

const handleSave = async () => {
  loading.value = true
  try {
    // 1. 更新基本资料
    await request.put('/user/profile', {
      avatar: form.avatar,
      nickname: form.nickname,
      phone: form.phone
    })
    
    // 2. 如果有新密码同步更新 (假设后端支持)
    if (form.newPassword) {
      // 这里的 API 视后端实际情况而定，暂且只处理 profile
    }

    ElMessage.success('个人资料已保存')
  } catch (e) {
    console.error('提交失败:', e)
    ElMessage.error('保存失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchProfile()
})
</script>

<style scoped lang="scss">
.hidden-file { display: none; }

.admin-profile-container {
  min-height: calc(100vh - 100px);
  position: relative;
  overflow: hidden;
  background-color: #f8faff;

  .profile-header-bg {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 240px;
    background: linear-gradient(135deg, #00bfa6 0%, #009688 100%);
    clip-path: ellipse(80% 60% at 50% 0%);
    z-index: 0;
  }

  .profile-content {
    position: relative;
    z-index: 1;
    max-width: 800px;
    margin: 80px auto 40px;
    padding: 0 20px;

    .glass-card {
      background: rgba(255, 255, 255, 0.85);
      backdrop-filter: blur(20px);
      border: 1px solid rgba(255, 255, 255, 0.5);
      border-radius: 24px;
      box-shadow: 0 12px 40px rgba(0, 0, 0, 0.08);
      overflow: hidden;
    }

    .user-header {
      text-align: center;
      padding: 40px 0 30px;
      background: linear-gradient(to bottom, rgba(255, 255, 255, 0.4), transparent);

      .avatar-box {
        position: relative;
        display: inline-block;
        margin-bottom: 16px;

        .premium-avatar {
          border: 4px solid #fff;
          box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
          transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
          cursor: pointer;

          &:hover {
            transform: scale(1.05);
          }
        }

        .camera-icon {
          position: absolute;
          bottom: 4px;
          right: 4px;
          width: 28px;
          height: 28px;
          background: #00bfa6;
          border-radius: 50%;
          color: #fff;
          display: flex;
          align-items: center;
          justify-content: center;
          border: 2px solid #fff;
          font-size: 14px;
          box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
          cursor: pointer;
        }
      }

      .user-title {
        font-size: 24px;
        font-weight: 800;
        color: #2c3e50;
        margin: 0 0 8px;
        letter-spacing: 0.5px;
      }

      .user-badge {
        display: inline-block;
        padding: 4px 14px;
        background: rgba(0, 191, 166, 0.12);
        color: #00bfa6;
        border-radius: 100px;
        font-size: 13px;
        font-weight: 600;
      }
    }

    .form-body {
      padding: 0 40px 40px;

      .section-title {
        font-size: 15px;
        font-weight: 700;
        color: #2c3e50;
        margin: 24px 0 16px;
        padding-left: 12px;
        border-left: 4px solid #00bfa6;
        line-height: 1;

        &.mt-4 {
          margin-top: 32px;
        }
      }

      :deep(.el-form-item__label) {
        font-weight: 600;
        color: #606266;
        padding-bottom: 8px;
      }

      .premium-input {
        :deep(.el-input__wrapper) {
          border-radius: 12px;
          background-color: #f8f9fa;
          box-shadow: none !important;
          border: 1px solid transparent;
          padding: 8px 16px;
          transition: all 0.3s;

          &:hover {
            background-color: #f1f3f5;
          }

          &.is-focus {
            background-color: #fff;
            border-color: #00bfa6;
            box-shadow: 0 0 0 3px rgba(0, 191, 166, 0.1) !important;
          }
        }
      }

      .action-area {
        margin-top: 40px;
        text-align: center;

        .save-btn {
          width: 200px;
          height: 48px;
          font-size: 16px;
          background: linear-gradient(135deg, #00bfa6 0%, #009688 100%) !important;
          border: none !important;
          font-weight: 600;
          box-shadow: 0 6px 20px rgba(0, 191, 166, 0.3);
          transition: all 0.3s;

          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 25px rgba(0, 191, 166, 0.4);
          }

          &:active {
            transform: translateY(0);
          }
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .admin-profile-container {
    .profile-content {
      margin-top: 60px;
      padding: 0 12px;

      .form-body {
        padding: 0 20px 30px;
      }

      .action-area {
        .save-btn {
          width: 100%;
        }
      }
    }
  }
}
</style>
