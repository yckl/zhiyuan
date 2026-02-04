<template>
  <div class="profile-page" v-loading="loading">
    <!-- 个人中心 (所有角色) -->
    <template v-if="userRole">
      <el-row :gutter="24">
        <!-- 左侧：头像和角色 -->
        <el-col :xs="24" :lg="8">
          <el-card class="profile-card" shadow="never">
            <!-- 头像上传 -->
            <div class="avatar-section">
              <el-upload
                class="avatar-uploader"
                action="/api/file/upload/avatar"
                :headers="uploadHeaders"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
              >
                <el-avatar :size="120" :src="avatarWithTimestamp" class="avatar">
                  <el-icon :size="48"><User /></el-icon>
                </el-avatar>
                <div class="avatar-overlay">
                  <el-icon><Camera /></el-icon>
                  <span>更换头像</span>
                </div>
              </el-upload>
              <h2 class="user-name">{{ formData.name || formData.username || '未设置' }}</h2>
              <el-tag :type="userRole === 'ADMIN' ? 'danger' : 'warning'" size="large" class="level-tag">
                {{ getRoleName(userRole) }}
              </el-tag>
            </div>

            <!-- 统计数据 (仅志愿者展示) -->
            <div v-if="isVolunteer" class="stats-section">
              <div class="stat-item">
                <div class="stat-value">{{ profile.totalHours || 0 }}</div>
                <div class="stat-label">志愿时长(h)</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ profile.serviceCount || profile.activityCount || 0 }}</div>
                <div class="stat-label">服务次数</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ profile.totalPoints || 0 }}</div>
                <div class="stat-label">累计积分</div>
              </div>
            </div>

            <!-- 快捷入口 (仅志愿者展示) -->
            <div v-if="isVolunteer" class="quick-links">
              <el-button @click="router.push('/my-activities')" class="link-btn">
                <el-icon><Calendar /></el-icon> 我的活动
              </el-button>
              <el-button @click="router.push('/my-experiences')" class="link-btn">
                <el-icon><Document /></el-icon> 我的心得
              </el-button>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧：资料表单 -->
        <el-col :xs="24" :lg="16">
          <el-card class="form-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span><el-icon><EditPen /></el-icon> 基本资料</span>
                <el-button type="primary" :loading="saving" @click="handleSave">保存修改</el-button>
              </div>
            </template>

            <el-form
              ref="formRef"
              :model="formData"
              :rules="rules"
              label-width="100px"
              label-position="right"
            >
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="用户名">
                    <el-input v-model="formData.username" disabled />
                  </el-form-item>
                </el-col>
                <el-col :span="12" v-if="isVolunteer">
                  <el-form-item label="真实姓名" prop="name">
                    <el-input v-model="formData.name" placeholder="请输入真实姓名" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="手机号" prop="phone">
                    <el-input v-model="formData.phone" placeholder="请输入手机号" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="邮箱">
                    <el-input v-model="formData.email" placeholder="请输入邮箱" />
                  </el-form-item>
                </el-col>
              </el-row>

              <!-- 志愿者专有字段 -->
              <template v-if="isVolunteer">
                <el-row :gutter="24">
                  <el-col :span="12">
                    <el-form-item label="学院">
                      <el-select v-model="formData.college" placeholder="请选择学院" @change="handleCollegeChange" style="width: 100%">
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
                    <el-form-item label="专业">
                      <el-select v-model="formData.major" placeholder="请选择专业" :disabled="!formData.college" style="width: 100%">
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

                <el-form-item label="个人简介">
                  <el-input
                    v-model="formData.bio"
                    type="textarea"
                    :rows="4"
                    placeholder="介绍一下自己吧，让大家更了解你~"
                    maxlength="200"
                    show-word-limit
                  />
                </el-form-item>

                <el-form-item label="技能标签">
                  <div class="tags-container">
                    <el-tag
                      v-for="(tag, index) in skillTags"
                      :key="index"
                      closable
                      @close="removeTag(index)"
                      class="skill-tag"
                    >
                      {{ tag }}
                    </el-tag>
                    <el-input
                      v-if="inputVisible"
                      ref="tagInputRef"
                      v-model="inputValue"
                      class="tag-input"
                      size="small"
                      @keyup.enter="handleInputConfirm"
                      @blur="handleInputConfirm"
                    />
                    <el-button v-else class="add-tag-btn" size="small" @click="showInput">
                      <el-icon><Plus /></el-icon> 添加技能
                    </el-button>
                  </div>
                </el-form-item>
              </template>
            </el-form>
          </el-card>
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { request } from '@/utils/request'

const router = useRouter()

const formRef = ref<FormInstance>()
const tagInputRef = ref()
const loading = ref(false)
const saving = ref(false)
const profile = ref<any>({})
const inputVisible = ref(false)
const inputValue = ref('')
const skillTags = ref<string[]>([])
const skillInput = ref('')
const avatarTimestamp = ref(Date.now())

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
  gender: 0,
  phone: '',
  email: '',
  college: '',
  major: '',
  bio: '',
  avatar: '',
  skills: ''
})

const handleCollegeChange = () => {
  formData.major = ''
}

const rules: FormRules = {
  name: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }]
}

// 获取用户角色
const userRole = computed(() => {
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo) {
    try {
      return JSON.parse(userInfo).role || ''
    } catch {
      return ''
    }
  }
  return ''
})

// 判断是否为志愿者
const isVolunteer = computed(() => {
  return userRole.value === 'VOLUNTEER'
})

// 获取角色名称
const getRoleName = (role: string) => {
  const roleMap: Record<string, string> = {
    'ADMIN': '系统管理员',
    'ORGANIZER': '活动组织者',
    'VOLUNTEER': '志愿者'
  }
  return roleMap[role] || '未知角色'
}

const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token')}`
}))

// 头像URL加时间戳避免缓存
const avatarWithTimestamp = computed(() => {
  const url = formData.avatar
  if (!url) return ''
  const separator = url.includes('?') ? '&' : '?'
  return `${url}${separator}t=${avatarTimestamp.value}`
})

// 计算志愿者等级 基于累计时长
// Lv1: 0-20h, Lv2: 20-50h, Lv3: 50-100h, Lv4: 100+h
const volunteerLevel = computed(() => {
  const hours = profile.value.totalHours || 0
  if (hours >= 100) return 4
  if (hours >= 50) return 3
  if (hours >= 20) return 2
  return 1
})

const fetchProfile = async () => {
  loading.value = true
  try {
    if (isVolunteer.value) {
      const res = await request.get('/volunteer/profile')
      profile.value = res.data || {}
      
      // 填充表单
      Object.assign(formData, {
        username: res.data.username || '',
        name: res.data.name || '',
        gender: res.data.gender || 0,
        phone: res.data.phone || '',
        email: res.data.email || '',
        college: res.data.college || '',
        major: res.data.major || '',
        bio: res.data.bio || '',
        avatar: res.data.avatar || '',
        skills: res.data.skills || ''
      })

      // 解析技能标签
      if (res.data.skills) {
        skillTags.value = res.data.skills.split(',').filter((s: string) => s.trim())
      }
    } else {
      // 非志愿者：获取基础账号信息
      const res = await request.get('/auth/current')
      if (res.code === 200 && res.data) {
        Object.assign(formData, {
          username: res.data.username || '',
          email: res.data.email || '',
          phone: res.data.phone || '',
          avatar: res.data.avatar || ''
        })
      }
    }
  } catch (error) {
    console.error('获取个人信息失败:', error)
  } finally {
    loading.value = false
  }
}

const handleAvatarSuccess = (response: any) => {
  if (response.code === 200) {
    const newAvatarUrl = response.data.url || response.data
    formData.avatar = newAvatarUrl
    // 更新时间戳强制刷新图片
    avatarTimestamp.value = Date.now()
    
    // 同时更新 localStorage 中的用户信息
    try {
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      userInfo.avatar = newAvatarUrl
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    } catch (e) {
      console.error('更新用户信息失败:', e)
    }
    
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const beforeAvatarUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

const showInput = () => {
  inputVisible.value = true
  nextTick(() => {
    tagInputRef.value?.focus()
  })
}

const handleInputConfirm = () => {
  if (inputValue.value.trim()) {
    if (!skillTags.value.includes(inputValue.value.trim())) {
      skillTags.value.push(inputValue.value.trim())
    }
  }
  inputVisible.value = false
  inputValue.value = ''
}

const removeTag = (index: number) => {
  skillTags.value.splice(index, 1)
}

const handleSave = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    saving.value = true
    try {
      if (isVolunteer.value) {
        // 组装技能标签
        formData.skills = skillTags.value.join(',')
        await request.put('/volunteer/profile', formData)
      } else {
        // 非志愿者：更新基础账号信息
        await request.put('/user/profile', {
          avatar: formData.avatar,
          email: formData.email,
          phone: formData.phone
        })
      }
      
      ElMessage.success('保存成功')
      
      // 更新 localStorage 中的用户信息
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      userInfo.avatar = formData.avatar
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    } catch (error) {
      console.error('保存失败:', error)
    } finally {
      saving.value = false
    }
  })
}

onMounted(() => {
  fetchProfile()
})
</script>

<style lang="scss" scoped>
.profile-page {
  .profile-card {
    border-radius: 12px;
    text-align: center;

    .avatar-section {
      padding: 20px 0;

      .avatar-uploader {
        position: relative;
        display: inline-block;
        cursor: pointer;

        .avatar {
          border: 4px solid #fff;
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        }

        .avatar-overlay {
          position: absolute;
          top: 0;
          left: 50%;
          transform: translateX(-50%);
          width: 120px;
          height: 120px;
          border-radius: 50%;
          background: rgba(0, 0, 0, 0.5);
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          color: #fff;
          opacity: 0;
          transition: opacity 0.3s;

          &:hover {
            opacity: 1;
          }

          span {
            font-size: 12px;
            margin-top: 4px;
          }
        }
      }

      .user-name {
        font-size: 20px;
        margin: 16px 0 8px;
        color: #333;
      }

      .user-id {
        font-size: 14px;
        color: #999;
        margin: 0 0 12px;
      }

      .level-tag {
        font-size: 14px;
      }
    }

    .stats-section {
      display: flex;
      justify-content: space-around;
      padding: 20px 0;
      border-top: 1px solid #f0f0f0;
      border-bottom: 1px solid #f0f0f0;

      .stat-item {
        .stat-value {
          font-size: 24px;
          font-weight: bold;
          color: #409eff;
        }

        .stat-label {
          font-size: 12px;
          color: #999;
          margin-top: 4px;
        }
      }
    }

    .quick-links {
      padding: 20px;
      display: flex;
      flex-direction: column;
      gap: 12px;

      .link-btn {
        width: 100%;
        justify-content: center;
      }
    }
  }

  .form-card {
    border-radius: 12px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      span {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 16px;
        font-weight: 600;
      }
    }

    .tags-container {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      align-items: center;

      .skill-tag {
        font-size: 13px;
      }

      .tag-input {
        width: 100px;
      }

      .add-tag-btn {
        border-style: dashed;
      }
    }

    .tags-tip {
      font-size: 12px;
      color: #999;
      margin-top: 8px;
    }
  }
}
</style>
