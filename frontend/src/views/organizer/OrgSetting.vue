<template>
  <div class="org-setting">
    <el-card class="setting-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon :size="20"><Setting /></el-icon>
            <span class="title">组织基本信息设置</span>
          </div>
          <el-button type="primary" :loading="saving" @click="handleSave">
            <el-icon><Select /></el-icon> 保存修改
          </el-button>
        </div>
      </template>

      <div class="setting-content">
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          class="setting-form"
        >
          <div class="form-layout">
            <!-- 左侧表单区域 -->
            <div class="form-main">
              <el-form-item label="组织名称" prop="name">
                <el-input v-model="form.name" disabled placeholder="组织名称">
                  <template #prefix>
                    <el-icon><OfficeBuilding /></el-icon>
                  </template>
                </el-input>
                <div class="form-tip">组织名称属于认证信息，如需修改请联系管理员</div>
              </el-form-item>

              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="负责人姓名" prop="contactPerson">
                    <el-input v-model="form.contactPerson" placeholder="请输入负责人姓名">
                      <template #prefix>
                        <el-icon><User /></el-icon>
                      </template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="联系电话" prop="contactPhone">
                    <el-input v-model="form.contactPhone" placeholder="请输入联系电话">
                      <template #prefix>
                        <el-icon><Phone /></el-icon>
                      </template>
                    </el-input>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item label="办公/联系地点" prop="address">
                <el-input v-model="form.address" placeholder="志愿者领奖或集合的固定地点">
                  <template #prefix>
                    <el-icon><Location /></el-icon>
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item label="组织简介" prop="description">
                <el-input
                  v-model="form.description"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入组织简介，展示在组织主页..."
                  maxlength="200"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item label="高级设置">
                <div class="advanced-setting">
                  <div class="setting-item">
                    <div class="setting-label">
                      <span>开启报名自动审核</span>
                      <el-tooltip content="开启后，志愿者报名将自动通过，无需人工审核" placement="top">
                        <el-icon class="info-icon"><InfoFilled /></el-icon>
                      </el-tooltip>
                    </div>
                    <el-switch v-model="form.autoAudit" />
                  </div>
                </div>
              </el-form-item>
            </div>

            <!-- 右侧头像上传区域 -->
            <div class="form-sidebar">
              <div class="avatar-uploader">
                <div class="avatar-wrapper">
                  <el-avatar :size="120" :src="form.logo || defaultAvatar" class="org-avatar">
                   {{ form.name?.[0] }}
                  </el-avatar>
                  <div class="upload-mask" @click="triggerUpload">
                    <el-icon><Camera /></el-icon>
                    <span>更换Logo</span>
                  </div>
                </div>
                <div class="avatar-tip">建议上传 400x400 像素的 PNG/JPG 图片</div>
                <!-- 隐藏的文件上传 input -->
                <input
                  ref="fileInput"
                  type="file"
                  accept="image/*"
                  style="display: none"
                  @change="handleFileChange"
                />
              </div>
            </div>
          </div>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import {
  Setting,
  Select,
  OfficeBuilding,
  User,
  Phone,
  Location,
  InfoFilled,
  Camera
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { request } from '@/utils/request'

const userStore = useUserStore()
const formRef = ref<FormInstance>()
const fileInput = ref<HTMLInputElement>()
const saving = ref(false)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const form = reactive({
  name: '',
  logo: '',
  description: '',
  address: '',
  contactPerson: '',
  contactPhone: '',
  autoAudit: false
})

const rules = reactive<FormRules>({
  description: [
    { required: true, message: '请输入组织简介', trigger: 'blur' },
    { min: 10, message: '简介最少 10 个字符', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入办公/联系地点', trigger: 'blur' }
  ],
  contactPerson: [
    { required: true, message: '请输入负责人姓名', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
})

// 初始化数据
onMounted(() => {
  // 模拟从后端获取数据，实际开发中调用 GET /organizer/profile
  // 这里暂时使用 userStore 的基本信息作为 mock
  const user = userStore.userInfo || {}
  
  form.name = user.name || '示例志愿组织' // 组织名通常是 user.name 或 extra info
  form.address = '学生活动中心 302'
  form.contactPerson = user.realName || '张老师'
  form.contactPhone = user.phone || '13800138000'
  form.description = '致力于校园志愿服务，传递爱心与温暖。我们定期组织各类公益活动，欢迎同学们加入！'
  form.logo = user.avatar
  form.autoAudit = false
  
  // 尝试调用真实接口填充
  fetchProfile()
})

const fetchProfile = async () => {
  try {
    // 假设后端有这个接口，如果没有则会报错并在 catch 中忽略
    const res = await request.get('/organizer/profile')
    if (res.data) {
      Object.assign(form, res.data)
    }
  } catch (e) {
    console.log('使用 Mock 数据展示')
  }
}

// 图片上传处理
const triggerUpload = () => {
  fileInput.value?.click()
}

const handleFileChange = async (event: Event) => {
  const input = event.target as HTMLInputElement
  if (input.files && input.files[0]) {
    const file = input.files[0]
    
    // 验证文件大小 (2MB)
    if (file.size > 2 * 1024 * 1024) {
      ElMessage.warning('图片大小不能超过 2MB')
      return
    }

    // 模拟上传
    const reader = new FileReader()
    reader.onload = (e) => {
      form.logo = e.target?.result as string
      ElMessage.success('Logo 上传成功')
    }
    reader.readAsDataURL(file)
    
    // 实际上传逻辑
    /*
    const formData = new FormData()
    formData.append('file', file)
    try {
      const res = await request.post('/upload', formData)
      form.logo = res.data.url
    } catch (e) {
      ElMessage.error('上传失败')
    }
    */
  }
}

// 保存修改
const handleSave = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true
      try {
        // 模拟 API 请求延时
        await new Promise(resolve => setTimeout(resolve, 800))
        
        // await request.put('/organizer/profile', form)
        
        ElMessage.success('保存成功')
        // 更新 store 中的头像等信息
        if (userStore.userInfo) {
           // 这是一个简单的 mock 更新
           // userStore.userInfo.avatar = form.logo
        }
      } catch (e) {
        ElMessage.error('保存失败，请稍后重试')
      } finally {
        saving.value = false
      }
    } else {
      ElMessage.warning('请检查表单填写是否正确')
      return false
    }
  })
}
</script>

<style scoped lang="scss">
.org-setting {
  padding: 10px;

  .setting-card {
    border-radius: 12px;
    border: none;
    min-height: calc(100vh - 140px);
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-left {
      display: flex;
      align-items: center;
      gap: 8px;

      .title {
        font-size: 16px;
        font-weight: 600;
        color: var(--el-text-color-primary);
      }
    }
  }

  .form-layout {
    display: flex;
    gap: 40px;
    padding-top: 10px;

    .form-main {
      flex: 1;
      max-width: 600px;
    }

    .form-sidebar {
      width: 200px;
      padding-top: 10px;
      display: flex;
      justify-content: center;
    }
  }

  .form-tip {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    margin-top: 4px;
    line-height: 1.4;
  }

  .advanced-setting {
    background: var(--el-fill-color-light);
    border-radius: 8px;
    padding: 16px;

    .setting-item {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .setting-label {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 14px;
        color: var(--el-text-color-regular);

        .info-icon {
          color: var(--el-text-color-secondary);
          cursor: help;
        }
      }
    }
  }

  .avatar-uploader {
    text-align: center;

    .avatar-wrapper {
      position: relative;
      width: 120px;
      height: 120px;
      margin: 0 auto 16px;
      border-radius: 50%;
      overflow: hidden;
      cursor: pointer;
      border: 4px solid var(--el-color-white);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);

      .org-avatar {
        width: 100%;
        height: 100%;
        font-size: 40px;
        font-weight: bold;
        background: var(--el-color-primary-light-9);
        color: var(--el-color-primary);
      }

      .upload-mask {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0, 0, 0, 0.5);
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        color: #fff;
        opacity: 0;
        transition: opacity 0.3s;
        font-size: 12px;

        .el-icon {
          font-size: 24px;
          margin-bottom: 4px;
        }
      }

      &:hover .upload-mask {
        opacity: 1;
      }
    }

    .avatar-tip {
      font-size: 12px;
      color: var(--el-text-color-secondary);
      line-height: 1.4;
    }
  }
}

// 响应式调整
@media (max-width: 768px) {
  .form-layout {
    flex-direction: column-reverse;
    gap: 20px;

    .form-sidebar {
      width: 100%;
      padding-bottom: 20px;
      border-bottom: 1px solid var(--el-border-color-lighter);
    }

    .form-main {
      max-width: 100%;
    }
  }
}
</style>
