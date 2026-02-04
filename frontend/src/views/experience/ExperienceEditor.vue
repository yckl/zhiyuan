<template>
  <div class="experience-editor">
    <el-page-header @back="router.back()">
      <template #content>
        <span class="page-title">{{ isEdit ? '编辑心得' : '发布心得' }}</span>
      </template>
    </el-page-header>

    <div class="editor-container" v-loading="loading">
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-position="top"
        class="editor-form"
      >
        <el-card shadow="never" class="form-card">
          <el-form-item label="心得标题" prop="title">
            <el-input v-model="form.title" placeholder="给你的心得起一个吸引人的标题吧" maxlength="100" show-word-limit />
          </el-form-item>

          <el-form-item label="关联活动" prop="activityId">
            <el-select 
              v-model="form.activityId" 
              placeholder="选择你参加过的活动（仅限已完成活动）" 
              clearable 
              style="width: 100%"
            >
              <el-option 
                v-for="act in myActivities" 
                :key="act.activityId" 
                :label="act.activityTitle" 
                :value="act.activityId" 
              />
            </el-select>
            <p class="form-tip">只能为已完成并确认时长的活动撰写心得哦</p>
          </el-form-item>

          <el-form-item label="心得内容" prop="content">
            <el-input 
              v-model="form.content" 
              type="textarea" 
              :rows="12" 
              placeholder="分享你在志愿服务中的所见、所闻、所感..." 
            />
          </el-form-item>

          <div class="form-footer">
            <el-button @click="router.back()">取消</el-button>
            <el-button type="primary" :loading="submitting" @click="handleSubmit">
              {{ isEdit ? '保存修改' : '确认发布' }}
            </el-button>
          </div>
        </el-card>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { request } from '@/utils/request'

const router = useRouter()
const route = useRoute()

const formRef = ref<FormInstance>()
const isEdit = computed(() => !!route.params.id)
const loading = ref(false)
const submitting = ref(false)
const myActivities = ref<any[]>([])

const form = reactive({
  id: undefined as number | undefined,
  title: '',
  activityId: null as number | null,
  content: '',
  images: '[]'
})

const rules = reactive<FormRules>({
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 5, max: 100, message: '长度在 5 到 100 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入内容', trigger: 'blur' },
    { min: 20, message: '至少写 20 个字吧，分享更多真情实感', trigger: 'blur' }
  ]
})

const fetchMyActivities = async () => {
  try {
    // 状态 3 通常表示已完成
    const res = await request.get('/registration/my', { page: 1, size: 100, status: 3 })
    myActivities.value = res.data?.records || []
    
    // 如果有携带 activityId 参数，自动选中
    if (route.query.activityId) {
      form.activityId = Number(route.query.activityId)
    }
  } catch (error) {
    console.error('获取报名记录失败:', error)
  }
}

const fetchDetail = async () => {
  if (!isEdit.value) return
  loading.value = true
  try {
    const res = await request.get(`/experience/${route.params.id}`)
    if (res.code === 200) {
      Object.assign(form, res.data)
    }
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取心得详情失败')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const url = '/experience'
        const method = isEdit.value ? 'put' : 'post'
        const res = await (request as any)[method](url, form)
        
        if (res.code === 200) {
          ElMessage.success(isEdit.value ? '修改成功' : '发布成功')
          router.push('/experience')
        }
      } catch (error: any) {
        // request.ts 已经处理了通用的报错
        console.error('提交失败:', error)
      } finally {
        submitting.value = false
      }
    }
  })
}

onMounted(() => {
  fetchMyActivities()
  fetchDetail()
})
</script>

<style lang="scss" scoped>
.experience-editor {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;

  .page-title {
    font-size: 18px;
    font-weight: 600;
  }

  .editor-container {
    margin-top: 24px;

    .form-card {
      border-radius: 12px;
      padding: 10px;
    }

    .form-tip {
      font-size: 12px;
      color: #999;
      margin-top: 4px;
    }

    .form-footer {
      display: flex;
      justify-content: center;
      gap: 20px;
      margin-top: 40px;
      padding-top: 20px;
      border-top: 1px solid #f0f0f0;

      .el-button {
        width: 120px;
      }
    }
  }
}
</style>
