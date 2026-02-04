<template>
  <div class="activity-editor">
    <el-page-header @back="router.back()">
      <template #content>
        <span class="page-title">{{ isEdit ? '编辑活动' : '发布新活动' }}</span>
      </template>
    </el-page-header>

    <el-card class="editor-card" shadow="never" v-loading="pageLoading">
      <el-form 
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        label-position="right"
        size="large"
      >
        <!-- 基本信息 -->
        <div class="form-section">
          <h3 class="section-title">
            <el-icon><Document /></el-icon>
            基本信息
          </h3>

          <el-form-item label="活动标题" prop="title">
            <el-input 
              v-model="formData.title" 
              placeholder="请输入活动标题"
              maxlength="100"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="活动分类" prop="categoryId">
            <el-select 
              v-model="formData.categoryId" 
              placeholder="请选择活动分类"
              style="width: 100%"
            >
              <el-option
                v-for="item in categories"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="封面图片" prop="coverImage">
            <div class="cover-upload">
              <el-upload
                class="cover-uploader"
                :action="uploadUrl"
                :headers="uploadHeaders"
                :show-file-list="false"
                :on-success="handleUploadSuccess"
                :on-error="handleUploadError"
                :before-upload="beforeUpload"
                accept="image/*"
              >
                <el-image
                  v-if="formData.coverImage"
                  :src="formData.coverImage"
                  fit="cover"
                  class="cover-preview"
                />
                <div v-else class="upload-placeholder">
                  <el-icon :size="40"><Plus /></el-icon>
                  <span>上传封面</span>
                </div>
              </el-upload>
              <div class="upload-tip">
                <p>建议尺寸：800 x 450 像素</p>
                <p>支持 JPG、PNG 格式，不超过 5MB</p>
              </div>
            </div>
          </el-form-item>
        </div>

        <!-- 时间设置 -->
        <div class="form-section">
          <h3 class="section-title">
            <el-icon><Clock /></el-icon>
            时间设置
          </h3>

          <el-form-item label="活动时间" prop="startTime" required>
            <el-col :span="11">
              <el-form-item prop="startTime" style="margin-bottom: 0">
                <el-date-picker
                  v-model="formData.startTime"
                  type="datetime"
                  placeholder="开始时间"
                  style="width: 100%"
                  format="YYYY-MM-DD HH:mm"
                  value-format="YYYY-MM-DD HH:mm:ss"
                />
              </el-form-item>
            </el-col>
            <el-col :span="2" class="text-center">
              <span class="time-separator">至</span>
            </el-col>
            <el-col :span="11">
              <el-form-item prop="endTime" style="margin-bottom: 0">
                <el-date-picker
                  v-model="formData.endTime"
                  type="datetime"
                  placeholder="结束时间"
                  style="width: 100%"
                  format="YYYY-MM-DD HH:mm"
                  value-format="YYYY-MM-DD HH:mm:ss"
                />
              </el-form-item>
            </el-col>
          </el-form-item>

          <el-form-item label="报名截止" prop="registerEnd">
            <el-date-picker
              v-model="formData.registerEnd"
              type="datetime"
              placeholder="选择报名截止时间"
              style="width: 100%"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
        </div>

        <!-- 招募设置 -->
        <div class="form-section">
          <h3 class="section-title">
            <el-icon><User /></el-icon>
            招募设置
          </h3>

          <el-form-item label="招募人数" prop="maxParticipants">
            <el-input-number
              v-model="formData.maxParticipants"
              :min="0"
              :max="9999"
              :step="1"
              placeholder="0表示不限制"
              controls-position="right"
            />
            <span class="input-suffix">人（0 表示不限制）</span>
          </el-form-item>

          <el-form-item label="志愿时长" prop="serviceHours">
            <el-input-number
              v-model="formData.serviceHours"
              :min="0"
              :max="999"
              :step="0.5"
              :precision="1"
              controls-position="right"
            />
            <span class="input-suffix">小时</span>
          </el-form-item>

          <el-form-item label="积分奖励" prop="pointsReward">
            <el-input-number
              v-model="formData.pointsReward"
              :min="0"
              :max="9999"
              :step="10"
              controls-position="right"
            />
            <span class="input-suffix">积分</span>
          </el-form-item>
        </div>

        <!-- 活动详情 -->
        <div class="form-section">
          <h3 class="section-title">
            <el-icon><Location /></el-icon>
            活动详情
          </h3>

          <el-form-item label="活动地点" prop="location">
            <el-input 
              v-model="formData.location" 
              placeholder="请输入活动地点"
              maxlength="200"
            />
          </el-form-item>

          <el-form-item label="联系方式" prop="contactInfo">
            <el-input 
              v-model="formData.contactInfo" 
              placeholder="请输入联系方式（电话/邮箱）"
              maxlength="100"
            />
          </el-form-item>

          <el-form-item label="活动简介" prop="summary">
            <el-input 
              v-model="formData.summary" 
              type="textarea"
              :rows="3"
              placeholder="请输入活动简介（100字以内）"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="活动详情" prop="content">
            <el-input 
              v-model="formData.content" 
              type="textarea"
              :rows="8"
              placeholder="请输入活动详细描述，可包含参与要求、注意事项等"
              maxlength="5000"
              show-word-limit
            />
          </el-form-item>
        </div>

        <!-- 提交按钮 -->
        <div class="form-actions">
          <el-button @click="router.back()">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ isEdit ? '保存修改' : '发布活动' }}
          </el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules, UploadProps } from 'element-plus'
import { Document, Clock, User, Location, Plus } from '@element-plus/icons-vue'
import { request } from '@/utils/request'

const router = useRouter()
const route = useRoute()

const formRef = ref<FormInstance>()
const pageLoading = ref(false)
const submitting = ref(false)
const categories = ref<any[]>([])

// 判断是编辑还是新增
const isEdit = computed(() => !!route.params.id)

// 上传配置
const uploadUrl = '/api/file/upload/cover'
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token')}`
}))

// 表单数据
const formData = reactive({
  id: null as number | null,
  title: '',
  categoryId: null as number | null,
  coverImage: '',
  startTime: '',
  endTime: '',
  registerEnd: '',
  maxParticipants: 0,
  serviceHours: 0,
  pointsReward: 0,
  location: '',
  contactInfo: '',
  summary: '',
  content: ''
})

// 表单验证规则
const formRules = reactive<FormRules>({
  title: [
    { required: true, message: '请输入活动标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择活动分类', trigger: 'change' }
  ],
  startTime: [
    { required: true, message: '请选择活动开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择活动结束时间', trigger: 'change' }
  ],
  location: [
    { required: true, message: '请输入活动地点', trigger: 'blur' }
  ]
})

// 获取分类列表
const fetchCategories = async () => {
  try {
    const res = await request.get('/category/list')
    categories.value = res.data || []
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

// 获取活动详情（编辑模式）
const fetchActivity = async () => {
  if (!isEdit.value) return
  
  pageLoading.value = true
  try {
    const res = await request.get(`/activity/${route.params.id}`)
    const data = res.data || {}
    
    Object.keys(formData).forEach(key => {
      if (data[key] !== undefined) {
        (formData as any)[key] = data[key]
      }
    })
  } catch (error) {
    console.error('获取活动详情失败:', error)
    ElMessage.error('获取活动详情失败')
  } finally {
    pageLoading.value = false
  }
}

// 上传前校验
const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

// 上传成功
const handleUploadSuccess: UploadProps['onSuccess'] = (response) => {
  if (response.code === 200 && response.data) {
    formData.coverImage = response.data
    ElMessage.success('封面上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 上传失败
const handleUploadError = () => {
  ElMessage.error('封面上传失败，请重试')
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.warning('请完善表单信息')
      return
    }

    submitting.value = true
    try {
      if (isEdit.value) {
        await request.put('/activity', formData)
        ElMessage.success('活动修改成功')
      } else {
        await request.post('/activity', formData)
        ElMessage.success('活动发布成功')
      }
      router.push('/organizer/activities')
    } catch (error) {
      console.error('保存失败:', error)
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => {
  fetchCategories()
  fetchActivity()
})
</script>

<style lang="scss" scoped>
.activity-editor {
  .page-title {
    font-size: 18px;
    font-weight: 600;
    color: #333;
  }

  .editor-card {
    margin-top: 20px;
    border-radius: 12px;
    max-width: 900px;
  }

  .form-section {
    margin-bottom: 32px;
    padding-bottom: 24px;
    border-bottom: 1px dashed #e8e8e8;

    &:last-of-type {
      border-bottom: none;
    }

    .section-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: 600;
      color: #333;
      margin: 0 0 20px;
      padding-left: 12px;
      border-left: 3px solid #409eff;
    }
  }

  .cover-upload {
    display: flex;
    gap: 20px;
    align-items: flex-start;

    .cover-uploader {
      :deep(.el-upload) {
        border: 2px dashed #d9d9d9;
        border-radius: 8px;
        cursor: pointer;
        overflow: hidden;
        transition: border-color 0.3s;

        &:hover {
          border-color: #409eff;
        }
      }
    }

    .cover-preview {
      width: 200px;
      height: 112px;
    }

    .upload-placeholder {
      width: 200px;
      height: 112px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 8px;
      color: #999;
      background: #fafafa;
    }

    .upload-tip {
      color: #999;
      font-size: 12px;
      line-height: 1.8;

      p {
        margin: 0;
      }
    }
  }

  .text-center {
    text-align: center;
  }

  .time-separator {
    color: #999;
    line-height: 40px;
  }

  .input-suffix {
    margin-left: 12px;
    color: #999;
    font-size: 14px;
  }

  .form-actions {
    display: flex;
    justify-content: center;
    gap: 16px;
    padding-top: 24px;
    border-top: 1px solid #f0f0f0;

    .el-button {
      min-width: 120px;
    }
  }
}
</style>
