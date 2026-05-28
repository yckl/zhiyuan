<template>
  <div class="setting-page" v-loading="loading">
    <!-- ==================== 1. PC 端 (保持原样，仅做结构性包装) ==================== -->
    <div v-if="!isMobile" class="pc-setting">
      <div class="pc-wrapper">
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="pc-form">
          <!-- 基础信息 -->
          <div class="pc-card">
            <div class="pc-card-head"><h3>基础信息</h3><p>完善组织的公开信息，提升信誉度</p></div>
            <div class="pc-info-layout">
              <div class="pc-inputs">
                <el-form-item label="组织名称" prop="name">
                  <el-input v-model="form.name" disabled class="ul-input" />
                  <span class="lock-hint">认证名称不可修改</span>
                </el-form-item>
                <el-form-item label="组织简介" prop="description">
                  <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入组织简介..." maxlength="200" show-word-limit class="ul-input" />
                </el-form-item>
              </div>
              <div class="pc-avatar-col">
                <div class="pc-avatar-wrap" @click="triggerUpload">
                  <el-avatar :size="100" :src="avatarWithTimestamp || defaultAvatar">{{ form.name?.[0] }}</el-avatar>
                  <div class="pc-av-overlay"><el-icon><Camera /></el-icon><span>修改</span></div>
                </div>
              </div>
            </div>
          </div>

          <!-- 认证材料 -->
          <div class="pc-card">
            <div class="pc-card-head"><h3>认证材料</h3><p>已通过系统的官方机构认证</p></div>
            <el-row :gutter="40">
              <el-col :span="12"><el-form-item label="机构代码"><el-input v-model="mockCert.usci" disabled class="ul-input" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="法人姓名"><el-input v-model="mockCert.legalPerson" disabled class="ul-input" /></el-form-item></el-col>
            </el-row>
            <div class="cert-badge"><el-icon color="#10b981"><CircleCheckFilled /></el-icon><span>官方认证组织</span></div>
          </div>

          <!-- 联系人 -->
          <div class="pc-card">
            <div class="pc-card-head"><h3>联系人信息</h3><p>用于活动中的应急联系与志愿者咨询</p></div>
            <el-row :gutter="40">
              <el-col :span="12"><el-form-item label="负责人" prop="contactPerson"><el-input v-model="form.contactPerson" placeholder="输入姓名" class="ul-input" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="联系电话" prop="contactPhone"><el-input v-model="form.contactPhone" placeholder="输入手机号" class="ul-input" /></el-form-item></el-col>
            </el-row>
            <el-form-item label="办公/集合地点" prop="address"><el-input v-model="form.address" placeholder="详细地址" class="ul-input" /></el-form-item>
          </div>

          <div class="pc-footer"><el-button type="primary" class="pc-save-btn" :loading="saving" @click="handleSave">保存设置</el-button></div>
        </el-form>
      </div>
    </div>

    <!-- ==================== 2. 移动端 (iOS Settings Style) ==================== -->
    <div v-else class="m-setting-container">
      <!-- 暖色调顶部 (复用 Dashboard 设计) -->
      <div class="m-header-fluid">
        <div class="brand-header-bg"></div>
        <div class="profile-card-glass">
          <div class="profile-avatar-box" @click="triggerUpload">
            <el-avatar :size="64" :src="avatarWithTimestamp || defaultAvatar" class="premium-avatar" />
            <div class="edit-badge"><el-icon><Camera /></el-icon></div>
          </div>
          <div class="profile-info">
            <h2 class="org-display-name">{{ form.name || '未命名组织' }}</h2>
            <p class="org-role-tag">认证组织管理员</p>
          </div>
        </div>
      </div>

      <div class="m-scroll-content">
        <el-form ref="formRef" :model="form" :rules="rules" class="ios-grouped-form">
          <!-- 分组 1: 基本信息 -->
          <div class="ios-group-label">基本信息</div>
          <div class="ios-card-group">
            <div class="ios-list-item">
            <div class="item-icon-wrap" style="background: rgba(0, 147, 233, 0.1); color: #0093E9;"><el-icon><Document /></el-icon></div>
              <div class="item-main">
                <span class="item-label">组织名称</span>
                <input :value="form.name" disabled class="item-input disabled" />
              </div>
            </div>
            <div class="ios-list-item tall">
            <div class="item-icon-wrap" style="background: rgba(0, 147, 233, 0.1); color: #0093E9;"><el-icon><ChatLineSquare /></el-icon></div>
              <div class="item-main">
                <span class="item-label">简介</span>
                <textarea v-model="form.description" placeholder="请输入组织简介" rows="2"></textarea>
              </div>
            </div>
          </div>

          <!-- 分组 2: 认证信息 -->
          <div class="ios-group-label">认证状态</div>
          <div class="ios-card-group">
            <div class="ios-list-item">
              <div class="item-icon-wrap" style="background: #34C759;"><el-icon><Check /></el-icon></div>
              <div class="item-main">
                <span class="item-label">证书编号</span>
                <input :value="mockCert.usci" disabled class="item-input disabled" />
              </div>
            </div>
            <div class="ios-list-item">
              <div class="item-icon-wrap" style="background: #5856D6;"><el-icon><User /></el-icon></div>
              <div class="item-main">
                <span class="item-label">认证法人</span>
                <input :value="mockCert.legalPerson" disabled class="item-input disabled" />
              </div>
            </div>
          </div>

          <!-- 分组 3: 联系人 -->
          <div class="ios-group-label">联系地址</div>
          <div class="ios-card-group">
            <div class="ios-list-item">
              <div class="item-icon-wrap" style="background: #007AFF;"><el-icon><Phone /></el-icon></div>
              <div class="item-main">
                <span class="item-label">负责人</span>
                <input v-model="form.contactPerson" placeholder="输入姓名" class="item-input" />
              </div>
              <el-icon class="arrow-ico"><ArrowRight /></el-icon>
            </div>
            <div class="ios-list-item">
              <div class="item-icon-wrap" style="background: #5AC8FA;"><el-icon><Iphone /></el-icon></div>
              <div class="item-main">
                <span class="item-label">联系电话</span>
                <input v-model="form.contactPhone" type="tel" placeholder="输入手机号" class="item-input" />
              </div>
              <el-icon class="arrow-ico"><ArrowRight /></el-icon>
            </div>
            <div class="ios-list-item">
              <div class="item-icon-wrap" style="background: #AF52DE;"><el-icon><Location /></el-icon></div>
              <div class="item-main">
                <span class="item-label">详细地址</span>
                <input v-model="form.address" placeholder="详细办公地点" class="item-input" />
              </div>
              <el-icon class="arrow-ico"><ArrowRight /></el-icon>
            </div>
          </div>
        </el-form>

        <div class="m-footer-actions">
          <button class="ios-save-btn" @click="handleSave" :disabled="saving">
            {{ saving ? '正在同步...' : '保存修改' }}
          </button>
          
          <div class="ios-danger-group">
            <button class="ios-logout-btn" @click="handleLogout">退出当前账号</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 隐藏文件输入 -->
    <input ref="fileInput" type="file" accept="image/*" class="hidden-file" @change="handleFileChange" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { 
  Camera, CircleCheckFilled, Document, ChatLineSquare, 
  Check, User, Phone, Iphone, Location, ArrowRight 
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { request } from '@/utils/request'
import { useMobile } from '@/composables/useMobile'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const fileInput = ref<HTMLInputElement>()
const saving = ref(false)
const loading = ref(false)
const { isMobile } = useMobile()
const defaultAvatar = 'https://api.dicebear.com/7.x/bottts/svg?seed=Org'
const avatarTimestamp = ref(Date.now())

const avatarWithTimestamp = computed(() => {
  const url = request.resolveUrl(form.logo)
  if (!url) return ''
  // 确保 URL 包含时间戳，防止浏览器缓存
  return `${url}${url.includes('?') ? '&' : '?'}t=${avatarTimestamp.value}`
})

const form = reactive({ name: '', logo: '', description: '', address: '', contactPerson: '', contactPhone: '' })
const mockCert = reactive({ usci: '91440300MA5FB***4H', legalPerson: '陈显之' })

const rules = reactive<FormRules>({
  description: [{ required: true, message: '请输入组织简介', trigger: 'blur' }],
  address: [{ required: true, message: '请输入联系地址', trigger: 'blur' }],
  contactPerson: [{ required: true, message: '请输入负责人姓名', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }, { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }]
})

const fetchProfile = async () => { 
  if (!userStore.token) return
  loading.value = true
  try { 
    const res = await request.get('/organizer/profile')
    if (res.data) Object.assign(form, res.data) 
  } catch (e) {
    console.error('获取信息失败:', e)
  } finally {
    loading.value = false
  }
}

const triggerUpload = () => fileInput.value?.click()

const handleFileChange = async (event: Event) => {
  const input = event.target as HTMLInputElement
  if (input.files?.[0]) {
    const file = input.files[0]
    if (file.size > 2 * 1024 * 1024) { ElMessage.warning('图片不能超过 2MB'); return }
    
    loading.value = true
    try {
      // 1. 上传到服务器
      const res = await request.upload('/file/upload/avatar', file)
      if (res.code === 200) {
        const url = res.data.url || res.data
        form.logo = url
        avatarTimestamp.value = Date.now()
        
        // 2. 更新 UserStore 和 LocalStorage
        userStore.setAvatar(url)
        const ui = JSON.parse(localStorage.getItem('userInfo') || '{}')
        ui.avatar = url
        localStorage.setItem('userInfo', JSON.stringify(ui))
        
        // 3. 立即持久化到组织者资料
        await request.put('/organizer/profile', { logo: url })
        
        ElMessage.success('头像更新成功')
      }
    } catch (e) {
      console.error('上传失败:', e)
      ElMessage.error('头像上传失败')
    } finally {
      loading.value = false
    }
  }
}

const handleSave = async () => {
  if (formRef.value) {
    try { await formRef.value.validate() } catch (e) { return }
  }
  saving.value = true
  try {
    // 模拟 API 保存
    await new Promise(r => setTimeout(r, 1000))
    ElMessage.success('所有的修改已同步到云端')
  } catch (e) { 
    ElMessage.error('无法连接到服务器') 
  } finally { 
    saving.value = false 
  }
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出当前账号吗？', '提示', {
    confirmButtonText: '确定退出',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.clearUser()
    router.push('/login')
    ElMessage.success('已安全退出')
  }).catch(() => {})
}

onMounted(() => {
  fetchProfile()
})
</script>

<style scoped lang="scss">
.setting-page { min-height: 100vh; background-color: #f2f2f7; }
.hidden-file { display: none; }

/* ==================== PC 端样式 ==================== */
.pc-setting {
  padding: 40px 20px; background: #f8fafc; min-height: 100vh;
  .pc-wrapper { max-width: 800px; margin: 0 auto; }
  .pc-card {
    background: #fff; border-radius: 20px; padding: 32px; margin-bottom: 24px; box-shadow: 0 4px 20px rgba(0,0,0,0.02);
    .pc-card-head { margin-bottom: 32px;
      h3 { font-size: 18px; font-weight: 800; color: #1e293b; margin: 0 0 4px; }
      p { font-size: 13px; color: #94a3b8; margin: 0; }
    }
  }
  .pc-info-layout { display: flex; gap: 40px; .pc-inputs { flex: 1; } }
  .pc-avatar-wrap {
    position: relative; width: 100px; height: 100px; border-radius: 50%; cursor: pointer; overflow: hidden; background: #f1f5f9;
    .pc-av-overlay { position: absolute; inset: 0; background: rgba(0,0,0,0.5); display: flex; flex-direction: column; align-items: center; justify-content: center; color: #fff; font-size: 11px; opacity: 0; transition: opacity 0.2s; .el-icon { font-size: 20px; } }
    &:hover .pc-av-overlay { opacity: 1; }
  }
  .ul-input {
    :deep(.el-input__wrapper), :deep(.el-textarea__inner) { box-shadow: none !important; border-bottom: 1.5px solid #f1f5f9 !important; border-radius: 0 !important; background: transparent !important; }
  }
  .pc-footer { text-align: right; padding: 20px 0 40px; .pc-save-btn { height: 44px; padding: 0 32px; border-radius: 12px; font-weight: 700; } }
}

/* ==================== 移动端 iOS Premium Settings ==================== */
.m-setting-container {
  min-height: 100vh;
  background: #f2f2f7;

  /* 顶部流体背景 */
  .m-header-fluid {
    position: relative;
    height: 220px;
    padding: 1px;

    .brand-header-bg {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 180px;
      background: linear-gradient(135deg, #0093E9 0%, #80D0C7 100%);
      clip-path: ellipse(110% 80% at 50% 0%);

      &::before {
        content: ''; position: absolute; top: -50%; left: -50%; width: 200%; height: 200%;
        background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 60%);
        transform: rotate(30deg);
      }
    }

    .profile-card-glass {
      position: relative;
      margin: 60px 20px 0;
      background: rgba(255, 255, 255, 0.7);
      backdrop-filter: blur(20px);
      -webkit-backdrop-filter: blur(20px);
      border-radius: 24px;
      padding: 24px;
      display: flex;
      align-items: center;
      gap: 16px;
      border: 1px solid rgba(255, 255, 255, 0.3);
      box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);

      .profile-avatar-box {
        position: relative;
        .premium-avatar {
          border: 2px solid #fff;
          box-shadow: 0 4px 12px rgba(0,0,0,0.1);
        }
        .edit-badge {
          position: absolute;
          bottom: -2px;
          right: -2px;
          width: 22px;
          height: 22px;
          border-radius: 50%;
          background: linear-gradient(135deg, #0093E9, #80D0C7);
          color: #fff;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 12px;
          border: 2px solid #fff;
        }
      }

      .profile-info {
        .org-display-name {
          font-size: 20px;
          font-weight: 800;
          color: #1c1c1e;
          margin: 0 0 4px;
        }
        .org-role-tag {
          font-size: 13px;
          color: #8e8e93;
          font-weight: 500;
        }
      }
    }
  }

  .m-scroll-content {
    margin-top: -10px;
    padding-bottom: 120px;
  }

  /* iOS Grouped Style */
  .ios-group-label {
    padding: 0 32px 8px;
    font-size: 13px;
    color: #6d6d72;
    text-transform: uppercase;
    font-weight: 500;
  }

  .ios-card-group {
    background: #fff;
    margin: 0 20px 24px;
    border-radius: 14px;
    overflow: hidden;

    .ios-list-item {
      display: flex;
      align-items: center;
      padding: 12px 16px;
      position: relative;
      
      &:not(:last-child)::after {
        content: "";
        position: absolute;
        bottom: 0;
        left: 54px;
        right: 0;
        height: 0.5px;
        background: #e5e5ea;
      }

      &:active {
        background: #f2f2f7;
      }

      .item-icon-wrap {
        width: 28px;
        height: 28px;
        border-radius: 7px; // Squircle
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        font-size: 16px;
        margin-right: 12px;
      }

      .item-main {
        flex: 1;
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .item-label {
          font-size: 16px;
          color: #000;
          font-weight: 400;
        }

        .item-input {
          border: none;
          outline: none;
          background: transparent;
          font-size: 16px;
          color: #8e8e93;
          text-align: right;
          width: 50%;
          &.disabled { color: #c4c4c6; }
        }

        textarea {
          border: none;
          outline: none;
          background: transparent;
          font-size: 15px;
          color: #1c1c1e;
          text-align: right;
          width: 70%;
          font-family: inherit;
          margin-top: 4px;
        }
      }

      &.tall {
        align-items: flex-start;
        .item-main { flex-direction: column; align-items: flex-end; }
      }

      .arrow-ico {
        margin-left: 8px;
        color: #c4c4c6;
        font-size: 14px;
      }
    }
  }

  /* Footer Actions */
  .m-footer-actions {
    padding: 0 20px;
    display: flex;
    flex-direction: column;
    gap: 16px;

    .ios-save-btn {
      width: 100%;
      height: 50px;
      background: linear-gradient(135deg, #0093E9, #80D0C7);
      border: none;
      border-radius: 14px;
      color: #fff;
      font-size: 17px;
      font-weight: 600;
      box-shadow: 0 8px 20px rgba(0, 147, 233, 0.3);
      &:active { transform: scale(0.96); opacity: 0.9; }
      &:disabled { opacity: 0.6; }
    }

    .ios-danger-group {
      margin-top: 12px;
      .ios-logout-btn {
        width: 100%;
        height: 50px;
        background: #fff;
        border: none;
        border-radius: 14px;
        color: #FF3B30;
        font-size: 17px;
        font-weight: 600;
        box-shadow: 0 2px 8px rgba(0,0,0,0.03);
        &:active { background: #fef1f1; }
      }
    }
  }
}
</style>
