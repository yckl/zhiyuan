<template>
  <div class="org-activity-create">
    <el-card class="editor-card" shadow="hover" v-loading="pageLoading">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button link @click="router.back()">
              <el-icon><ArrowLeft /></el-icon> 返回
            </el-button>
            <el-divider direction="vertical" />
            <span class="title">{{ isEdit ? '编辑活动' : '发布新活动' }}</span>
          </div>
          <div class="header-right">
            <el-button @click="router.back()">取消</el-button>
            <el-button type="primary" @click="handleSubmit" :loading="submitting">
              {{ isEdit ? '保存修改' : '立即发布' }}
            </el-button>
          </div>
        </div>
      </template>

      <el-form 
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        label-position="top"
        class="custom-form"
      >
        <el-row :gutter="40">
          <!-- 左侧：基本信息 -->
          <el-col :span="16">
            <div class="form-section">
              <h3 class="section-title">基本信息</h3>
              <el-form-item label="活动标题" prop="title">
                <el-input v-model="formData.title" placeholder="请输入动人心弦的标题" maxlength="100" show-word-limit />
              </el-form-item>
              
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="活动分类" prop="categoryId">
                    <el-select v-model="formData.categoryId" placeholder="请选择分类" style="width: 100%">
                      <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="活动地点" prop="location">
                    <el-input v-model="formData.location" placeholder="详细地址或在线平台" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item label="活动简介" prop="summary">
                <el-input v-model="formData.summary" type="textarea" :rows="3" placeholder="简要描述活动亮点" maxlength="200" show-word-limit />
              </el-form-item>

              <el-form-item label="详细内容" prop="content">
                <el-input v-model="formData.content" type="textarea" :rows="10" placeholder="详细说明活动流程、要求及注意事项" />
              </el-form-item>
            </div>
          </el-col>

          <!-- 右侧：属性设置 -->
          <el-col :span="8">
            <div class="form-section">
              <h3 class="section-title">封面设置</h3>
              <el-form-item prop="coverImage">
                <el-upload
                  class="cover-uploader"
                  :action="'/api/file/upload/cover'"
                  :headers="uploadHeaders"
                  :show-file-list="false"
                  :on-success="handleUploadSuccess"
                  :before-upload="beforeUpload"
                >
                  <img v-if="formData.coverImage" :src="formData.coverImage" class="cover-image" />
                  <div v-else class="upload-placeholder">
                    <el-icon :size="28"><Plus /></el-icon>
                    <span>上传封面图</span>
                  </div>
                </el-upload>
                <div class="upload-tip">建议比例 16:9，大小不超过 5MB</div>
              </el-form-item>
            </div>

            <div class="form-section mt-20">
              <h3 class="section-title">时间与奖励</h3>
              <el-form-item label="活动时间" required>
                <el-date-picker
                  v-model="timeRange"
                  type="datetimerange"
                  range-separator="至"
                  start-placeholder="开始"
                  end-placeholder="结束"
                  format="YYYY-MM-DD HH:mm"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  @change="handleTimeChange"
                  style="width: 100%"
                />
              </el-form-item>

              <el-row :gutter="10">
                <el-col :span="12">
                  <el-form-item label="招募人数" prop="maxParticipants">
                    <el-input-number v-model="formData.maxParticipants" :min="0" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="志愿时长" prop="serviceHours">
                    <el-input-number v-model="formData.serviceHours" :min="0" :precision="1" style="width: 100%" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item label="积分奖励" prop="pointsReward">
                <el-input-number v-model="formData.pointsReward" :min="0" style="width: 100%" />
              </el-form-item>
              
              <el-form-item label="联系方式" prop="contactInfo">
                <el-input v-model="formData.contactInfo" placeholder="手机号、微信或邮箱" />
              </el-form-item>
            </div>
          </el-col>
        </el-row>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft, Plus } from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const formRef = ref()
const pageLoading = ref(false)
const submitting = ref(false)
const categories = ref([])
const timeRange = ref([])

const isEdit = computed(() => !!route.params.id)

const formData = reactive({
  id: null,
  title: '',
  categoryId: null,
  coverImage: '',
  startTime: '',
  endTime: '',
  maxParticipants: 0,
  serviceHours: 0,
  pointsReward: 0,
  location: '',
  summary: '',
  content: '',
  contactInfo: ''
})

const formRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  location: [{ required: true, message: '请输入地点', trigger: 'blur' }]
}

const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token')}`
}))

const fetchCategories = async () => {
  const res = await request.get('/category/list')
  categories.value = res.data || []
}

const fetchActivity = async () => {
  if (!isEdit.value) return
  pageLoading.value = true
  try {
    const res = await request.get(`/activity/${route.params.id}`)
    const data = res.data
    Object.assign(formData, data)
    if (data.startTime && data.endTime) {
      timeRange.value = [data.startTime, data.endTime]
    }
  } catch (error) {
    console.error('获取活动失败:', error)
  } finally {
    pageLoading.value = false
  }
}

const handleTimeChange = (val) => {
  if (val && val.length === 2) {
    formData.startTime = val[0]
    formData.endTime = val[1]
  } else {
    formData.startTime = ''
    formData.endTime = ''
  }
}

const beforeUpload = (file) => {
  const isImg = file.type.startsWith('image/')
  if (!isImg) ElMessage.error('只能上传图片!')
  return isImg
}

const handleUploadSuccess = (res) => {
  if (res.code === 200) {
    formData.coverImage = res.data
    ElMessage.success('封面上传成功')
  }
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    if (!formData.startTime) return ElMessage.warning('请选择活动时间')

    submitting.value = true
    try {
      if (isEdit.value) {
        await request.put('/activity', formData)
        ElMessage.success('保存成功')
      } else {
        await request.post('/activity', formData)
        ElMessage.success('发布活动成功，等待审核')
      }
      router.push('/organizer/activity/list')
    } catch (error) {
      console.error('提交失败:', error)
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

<style scoped lang="scss">
.org-activity-create {
  padding: 10px;

  .editor-card {
    border-radius: 12px;
    border: none;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    .header-left {
      display: flex;
      align-items: center;
      .title {
        font-weight: bold;
        font-size: 16px;
      }
    }
  }

  .form-section {
    background: var(--el-fill-color-blank);
    .section-title {
      font-size: 15px;
      font-weight: 600;
      margin-bottom: 20px;
      padding-bottom: 10px;
      border-bottom: 1px solid var(--el-border-color-lighter);
      color: var(--el-text-color-primary);
    }
  }

  .cover-uploader {
    :deep(.el-upload) {
      width: 100%;
      height: 180px;
      border: 2px dashed var(--el-border-color);
      border-radius: 8px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      transition: var(--el-transition-duration);
      &:hover {
        border-color: var(--el-color-primary);
      }
    }
    .cover-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    .upload-placeholder {
      height: 100%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: var(--el-text-color-placeholder);
      gap: 10px;
    }
  }

  .upload-tip {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    margin-top: 8px;
    text-align: center;
  }
}
</style>
