<template>
  <el-dialog
    v-model="visible"
    title="活动服务评价"
    width="500px"
    @closed="handleClosed"
    append-to-body
    destroy-on-close
  >
    <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
      <el-form-item label="活动评分" prop="score">
        <el-rate 
          v-model="form.score" 
          show-text 
          :texts="['极差', '失望', '一般', '满意', '非常满意']"
        />
      </el-form-item>
      
      <el-form-item label="评价内容" prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="4"
          placeholder="说说你的活动感受，您的评价对组织者非常重要..."
          maxlength="200"
          show-word-limit
        />
      </el-form-item>
      
      <el-form-item prop="isAnonymous">
        <el-checkbox v-model="form.isAnonymous">匿名评价</el-checkbox>
        <div class="tip">勾选后，组织者将看到“匿名同学”</div>
      </el-form-item>
    </el-form>
    
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          提交评价
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { request } from '@/utils/request'

const emit = defineEmits(['success'])

const visible = ref(false)
const submitting = ref(false)
const formRef = ref()
const activityId = ref<number | null>(null)

const form = reactive({
  score: 5,
  content: '',
  isAnonymous: false
})

const rules = {
  score: [{ required: true, message: '请打个分', trigger: 'change' }],
  content: [{ required: true, message: '请输入评价内容', trigger: 'blur' }]
}

// 展示评价弹窗
const show = (id: number) => {
  activityId.value = id
  visible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      submitting.value = true
      try {
        await request.post('/volunteer/review/add', {
          activityId: activityId.value,
          ...form
        })
        ElMessage.success('感谢您的评价')
        visible.value = false
        emit('success')
      } catch (error: any) {
        ElMessage.error(error.message || '评价失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleClosed = () => {
  form.score = 5
  form.content = ''
  form.isAnonymous = false
  activityId.value = null
}

defineExpose({
  show
})
</script>

<style scoped>
.tip {
  font-size: 12px;
  color: #909399;
  margin-left: 24px;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style>
