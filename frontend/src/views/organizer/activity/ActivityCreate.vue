<template>
  <div class="org-activity-create">
    <!-- ==================== PC ?==================== -->
    <template v-if="!isMobile">
      <!-- 步进引导 -->
      <div class="step-container">
        <el-steps :active="currentStep" finish-status="success" align-center>
          <el-step title="基础信息" />
          <el-step title="招募条件" />
          <el-step title="内容详情" />
        </el-steps>
      </div>

      <div class="pc-form-area" v-loading="pageLoading">
        <el-form ref="formRef" :model="formData" :rules="formRules" label-position="top" class="pc-form">
          <!-- Step 1 -->
          <div v-show="currentStep === 0" class="form-step">
            <div class="step-badge">Step 01</div>
            <h2 class="step-title">告诉大家这是一个什么样的活</h2>
            <el-form-item label="活动标题" prop="title">
              <el-input v-model="formData.title" placeholder="请输入动人心弦的标题" class="underline-input big" maxlength="50" />
            </el-form-item>
            <el-row :gutter="40">
              <el-col :md="12">
                <el-form-item label="所属分? prop="categoryId">
                  <el-select v-model="formData.categoryId" placeholder="选择分类" class="underline-select">
                    <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :md="12">
                <el-form-item label="活动地点" prop="location">
                  <el-input v-model="formData.location" placeholder="详细地址或线上链? class="underline-input" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="活动时间" prop="startTime">
              <el-date-picker v-model="timeRange" type="datetimerange" range-separator="? start-placeholder="开始时? end-placeholder="结束时间" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DD HH:mm:ss" @change="handleTimeChange" class="underline-select" />
            </el-form-item>
            <div class="cover-zone">
              <span class="cz-label">展示封面</span>
              <el-upload class="cover-uploader" :action="'/api/file/upload/cover'" :headers="uploadHeaders" :show-file-list="false" :on-success="handleUploadSuccess">
                <img v-if="formData.coverImage" :src="formData.coverImage" class="cover-preview" @error="(e) => ((e.target as HTMLImageElement).src = '/default-cover.jpg')" />
                <div v-else class="cover-placeholder">
                  <el-icon :size="32"><Picture /></el-icon>
                  <span>点击上传 16:9 封面</span>
                </div>
              </el-upload>
            </div>
          </div>

          <!-- Step 2 -->
          <div v-show="currentStep === 1" class="form-step">
            <div class="step-badge">Step 02</div>
            <h2 class="step-title">招募与奖励设</h2>
            <el-row :gutter="40">
              <el-col :md="12">
                <el-form-item label="招募人数上限 (0为不?">
                  <el-slider v-model="formData.maxParticipants" :max="200" show-input />
                </el-form-item>
              </el-col>
              <el-col :md="12">
                <el-form-item label="志愿服务时长">
                  <el-input-number v-model="formData.serviceHours" :min="0" :precision="1" :step="0.5" />
                  <span class="unit">小时</span>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="40">
              <el-col :md="12">
                <el-form-item label="联系方式" prop="contactInfo">
                  <el-input v-model="formData.contactInfo" placeholder="微信息?手机号? class="underline-input" />
                </el-form-item>
              </el-col>
              <el-col :md="12">
                <el-form-item label="积分奖励">
                  <el-input-number v-model="formData.pointsReward" :min="0" />
                  <span class="unit">积分</span>
                </el-form-item>
              </el-col>
            </el-row>
          </div>

          <!-- Step 3 -->
          <div v-show="currentStep === 2" class="form-step">
            <div class="step-badge">Step 03</div>
            <h2 class="step-title">详细说明</h2>
            <div class="rich-editor-sim">
              <div class="editor-toolbar">
                <button type="button" class="tool-btn"><b>B</b></button>
                <button type="button" class="tool-btn"><i>I</i></button>
                <button type="button" class="tool-btn"><el-icon><Picture /></el-icon></button>
                <span class="mode-label">可视化编</span>
              </div>
              <el-input v-model="formData.content" type="textarea" :autosize="{ minRows: 15, maxRows: 30 }" placeholder="请输入活动的具体流程、要?.." class="editor-textarea" />
            </div>
          </div>
        </el-form>
      </div>

      <!-- PC 底部操作栏?-->
      <div class="pc-bottom-bar">
        <div class="bar-inner">
          <el-button link @click="saveDraft" :loading="submitting">保存草稿</el-button>
          <div class="bar-right">
            <el-button v-if="currentStep > 0" @click="currentStep--">上一题</el-button>
            <el-button v-if="currentStep < 2" type="primary" @click="nextStep">下一题</el-button>
            <el-button v-if="currentStep === 2" type="primary" @click="handleSubmit" :loading="submitting" class="pub-btn">
              {{ isEdit ? '保存修改' : '立即发布' }}
            </el-button>
          </div>
        </div>
      </div>
    </template>

    <!-- ==================== 移动端?(iOS Settings 风格) ==================== -->
    <template v-else>
      <div class="m-create" v-loading="pageLoading">
        <!-- 黄色警告?-->
        <div class="m-warn-banner">
          <span class="warn-icon">⚠️</span>
          <span>手机端仅支持修改基础信息，详情请在电脑端编辑</span>
        </div>

        <!-- iOS 设置列表 -->
        <el-form ref="formRef" :model="formData" :rules="formRules" class="m-ios-form">

          <!-- 基础信息 Section -->
          <div class="m-section-label">基础信息</div>
          <div class="m-settings-group">
            <div class="m-row">
              <label>标题</label>
              <input v-model="formData.title" placeholder="请输入活动标? />
            </div>
            <div class="m-row">
              <label>分类</label>
              <el-select v-model="formData.categoryId" placeholder="选择" class="m-inline-select">
                <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </div>
            <div class="m-row">
              <label>地点</label>
              <input v-model="formData.location" placeholder="详细地址" />
            </div>
          </div>

          <!-- 时间 Section (拆分为两个独立选择? -->
          <div class="m-section-label">活动时间</div>
          <div class="m-settings-group">
            <div class="m-row">
              <label>开始时?/label>
              <el-date-picker
                v-model="formData.startTime"
                type="datetime"
                placeholder="选择开始时?
                format="MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm:ss"
                class="m-date-picker"
              />
            </div>
            <div class="m-row">
              <label>结束时间</label>
              <el-date-picker
                v-model="formData.endTime"
                type="datetime"
                placeholder="选择结束时间"
                format="MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm:ss"
                class="m-date-picker"
              />
            </div>
          </div>

          <!-- 招募设置 Section -->
          <div class="m-section-label">招募设置</div>
          <div class="m-settings-group">
            <div class="m-row">
              <label>招募人数</label>
              <input v-model.number="formData.maxParticipants" type="number" placeholder="0为不? />
            </div>
            <div class="m-row">
              <label>服务时长</label>
              <div class="m-number-wrap">
                <input v-model.number="formData.serviceHours" type="number" step="0.5" />
                <span class="m-unit">小时</span>
              </div>
            </div>
            <div class="m-row">
              <label>联系方式</label>
              <input v-model="formData.contactInfo" placeholder="微信/手机" />
            </div>
            <div class="m-row">
              <label>积分奖励</label>
              <input v-model.number="formData.pointsReward" type="number" placeholder="0" />
            </div>
          </div>

          <!-- 详情降级 Section -->
          <div class="m-section-label">活动详情</div>
          <div class="m-settings-group">
            <div class="m-textarea-row">
              <el-input
                v-model="formData.content"
                type="textarea"
                :autosize="{ minRows: 5, maxRows: 12 }"
                placeholder="简要描述活动内容（富文本排版请使用电脑端）"
                class="m-textarea"
              />
            </div>
          </div>

          <!-- 封面 -->
          <div class="m-section-label">封面图片</div>
          <div class="m-settings-group">
            <div class="m-cover-row">
              <el-upload
                :action="'/api/file/upload/cover'"
                :headers="uploadHeaders"
                :show-file-list="false"
                :on-success="handleUploadSuccess"
                class="m-cover-upload"
              >
                <img v-if="formData.coverImage" :src="formData.coverImage" class="m-cover-img" @error="(e) => ((e.target as HTMLImageElement).src = '/default-cover.jpg')" />
                <div v-else class="m-cover-empty">
                  <el-icon><Picture /></el-icon>
                  <span>点击上传</span>
                </div>
              </el-upload>
            </div>
          </div>
        </el-form>

        <!-- 底部固定双按?-->
        <div class="m-bottom-bar">
          <button class="m-bar-btn draft" @click="saveDraft" :disabled="submitting">存草?/button>
          <button class="m-bar-btn publish" @click="handleSubmit" :disabled="submitting">
            {{ isEdit ? '保存修改' : '发布' }}
          </button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Picture } from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import { ElMessage } from 'element-plus'
import { useMobile } from '@/composables/useMobile'

const router = useRouter()
const route = useRoute()
const formRef = ref()
const currentStep = ref(0)
const { isMobile } = useMobile()

const stripHtml = (html: string) => {
  if (!html) return ''
  const tmp = document.createElement("DIV")
  tmp.innerHTML = html
  return tmp.textContent || tmp.innerText || ""
}

const pageLoading = ref(false)
const submitting = ref(false)
const categories = ref<any[]>([])
const timeRange = ref<any[]>([])

const isEdit = computed(() => !!route.params.id)

const formData = reactive({
  id: null as number | null,
  title: '',
  categoryId: null as number | null,
  coverImage: '',
  startTime: '',
  endTime: '',
  maxParticipants: 10,
  serviceHours: 1,
  pointsReward: 0,
  location: '',
  summary: '',
  content: '',
  contactInfo: '',
  status: 1
})

const formRules = {
  title: [{ required: true, message: '请输入标?, trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  location: [{ required: true, message: '请填写地?, trigger: 'blur' }]
}

const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token')}`
}))

const fetchCategories = async () => {
  try {
    const res = await request.get('/category/list')
    categories.value = res.data || []
  } catch (e) { /* silent */ }
}

const fetchActivity = async () => {
  if (!isEdit.value) return
  pageLoading.value = true
  try {
    const res = await request.get(`/activity/${route.params.id}`)
    Object.assign(formData, res.data)
    // 核心修复：清?content 字段以移?HTML 标签
    formData.content = stripHtml(formData.content)
    
    if (formData.startTime && formData.endTime) {
      timeRange.value = [formData.startTime, formData.endTime]
    }
  } catch (e) {
    ElMessage.error('获取活动数据失败')
  } finally {
    pageLoading.value = false
  }
}

const handleTimeChange = (val: any) => {
  if (val && val.length === 2) {
    formData.startTime = val[0]
    formData.endTime = val[1]
  } else {
    formData.startTime = ''
    formData.endTime = ''
  }
}

const handleUploadSuccess = (res: any) => {
  if (res.code === 200) {
    formData.coverImage = res.data
    ElMessage.success('封面上传成功')
  }
}

const nextStep = async () => {
  if (currentStep.value === 0) {
    try {
      await formRef.value.validateField(['title', 'categoryId', 'location'])
    } catch (e) { return }
  }
  currentStep.value++
}

const saveDraft = async () => {
  formData.status = 0
  doSubmit('已存入草稿箱')
}

const handleSubmit = async () => {
  if (!isMobile.value) {
    try { await formRef.value.validate() } catch (e) { return }
  }
  formData.status = 1
  doSubmit(isEdit.value ? '修改成功' : '发布成功，等待审?)
}

const doSubmit = async (msg: string) => {
  submitting.value = true
  try {
    if (isEdit.value) {
      await request.put('/activity', formData)
    } else {
      await request.post('/activity', formData)
    }
    ElMessage.success(msg)
    router.push('/organizer/activity/list')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchCategories()
  fetchActivity()
})
</script>

<style scoped lang="scss">
.org-activity-create { min-height: 100vh; background: #fff; display: flex; flex-direction: column; }

/* ==================== PC ?==================== */
.step-container { padding: 40px 0 20px; max-width: 800px; margin: 0 auto; width: 100%; }

.pc-form-area {
  flex: 1; 
  padding: 20px 40px 120px !important; /* 关键：确保底部有足够间隙 */
  max-width: 900px; 
  margin: 0 auto; 
  width: 100%;
  background-color: #F5F7FA; /* 确保背景色一行?*/
  
  .form-step { margin-bottom: 40px; animation: fadeUp 0.4s ease; }
  .step-badge { font-size: 12px; font-weight: 700; color: #3b82f6; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 8px; }
  .step-title { font-size: 28px; font-weight: 700; color: #111827; margin: 0 0 32px; line-height: 1.3; }
}

:deep(.underline-input) {
  .el-input__wrapper { box-shadow: none !important; border: none; border-bottom: 1px solid #e5e7eb; border-radius: 0; padding: 8px 0;
    &.is-focus { border-bottom-color: #3b82f6; border-bottom-width: 2px; }
  }
  .el-input__inner { font-size: 16px; color: #111827; }
  &.big .el-input__inner { font-size: 24px; font-weight: 600; }
}
:deep(.underline-select) { width: 100%;
  .el-input__wrapper { box-shadow: none !important; border: none; border-bottom: 1px solid #e5e7eb; border-radius: 0; padding: 8px 0; }
}
.unit { margin-left: 8px; font-size: 14px; color: #6b7280; }

.cover-zone {
  margin-top: 40px;
  .cz-label { font-size: 14px; color: #374151; font-weight: 600; display: block; margin-bottom: 16px; }
  .cover-uploader :deep(.el-upload) { width: 100%; height: 280px; background: #f9fafb; border: 2px dashed #e5e7eb; border-radius: 16px; cursor: pointer; overflow: hidden; &:hover { border-color: #3b82f6; } }
  .cover-preview { width: 100%; height: 100%; object-fit: cover; }
  .cover-placeholder { display: flex; flex-direction: column; align-items: center; gap: 12px; color: #9ca3af; span { font-size: 14px; } }
}

.rich-editor-sim {
  border: 1px solid #e5e7eb; border-radius: 12px; overflow: hidden;
  .editor-toolbar { padding: 8px 16px; background: #f9fafb; border-bottom: 1px solid #e5e7eb; display: flex; align-items: center; gap: 12px;
    .tool-btn { background: none; border: none; cursor: pointer; padding: 4px 8px; border-radius: 4px; color: #4b5563; font-size: 16px; &:hover { background: #e5e7eb; } }
    .mode-label { font-size: 12px; color: #9ca3af; margin-left: auto; }
  }
  :deep(.editor-textarea .el-textarea__inner) { border: none; box-shadow: none; padding: 20px; font-size: 15px; line-height: 1.6; color: #374151; resize: none; }
}

.pc-bottom-bar {
  position: fixed; 
  bottom: 0; 
  left: auto; /* 靠右定位，避免直接遮挡侧边栏 */
  right: 0; 
  width: calc(100% - 240px); /* 默认减去侧边栏宽?*/
  height: 80px; 
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px); 
  border-top: 1px solid rgba(0,0,0,0.05); 
  display: flex; 
  align-items: center; 
  z-index: 999;
  box-shadow: 0 -4px 20px rgba(0,0,0,0.05);
  transition: width 0.3s ease; /* 适配侧边栏折?*/
  
  .bar-inner { max-width: 900px; margin: 0 auto; width: 100%; padding: 0 40px; display: flex; justify-content: space-between; }
  .bar-right { display: flex; gap: 12px; }
  .pub-btn { padding: 0 32px; border-radius: 8px; font-weight: 700; }
}

/* 适配侧边栏折叠状态?(通常通过 body 类名或全局变量，这里尝试适配) */
@media only screen and (min-width: 992px) {
  .is-collapsed & {
    width: calc(100% - 72px);
  }
}

@keyframes fadeUp { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }

/* ==================== 移动端?(iOS Settings) ==================== */
.m-create {
  background: #f2f2f7; 
  min-height: 100vh; 
  padding-bottom: 140px !important; /* 关键：留出充足空?*/

  /* 警告横幅 */
  .m-warn-banner {
    background: #fef3c7; padding: 12px 16px; display: flex; align-items: center; gap: 8px;
    font-size: 13px; color: #92400e; font-weight: 500;
    .warn-icon { font-size: 16px; }
  }

  /* Section Label */
  .m-section-label {
    padding: 28px 20px 8px; font-size: 13px; font-weight: 600;
    color: #6d6d72; text-transform: uppercase;
  }

  /* Settings Group (白色圆角? */
  .m-settings-group {
    background: #fff; margin: 0 16px; border-radius: 12px; overflow: hidden;
  }

  /* 单行：左 label ?input */
  .m-row {
    display: flex; align-items: center; justify-content: space-between;
    padding: 14px 16px; border-bottom: 0.5px solid #e5e5ea;
    min-height: 44px;
    &:last-child { border-bottom: none; }

    label { font-size: 15px; color: #000; flex-shrink: 0; width: 80px; }

    input {
      flex: 1; border: none; outline: none; background: transparent;
      font-size: 15px; text-align: right; color: #000;
      &::placeholder { color: #c7c7cc; }
      &[type="number"] { -moz-appearance: textfield; &::-webkit-inner-spin-button { -webkit-appearance: none; } }
    }

    .m-inline-select {
      flex: 1;
      :deep(.el-input__wrapper) { box-shadow: none !important; background: transparent; justify-content: flex-end; }
      :deep(.el-input__inner) { text-align: right; }
    }

    .m-date-picker {
      flex: 1;
      :deep(.el-input__wrapper) { box-shadow: none !important; background: transparent; justify-content: flex-end; }
      :deep(.el-input__inner) { text-align: right; font-size: 15px; }
    }

    .m-number-wrap {
      display: flex; align-items: center; gap: 4px;
      input { width: 60px; text-align: right; }
      .m-unit { font-size: 14px; color: #8e8e93; }
    }
  }

  /* Textarea row */
  .m-textarea-row {
    padding: 12px 16px;
    .m-textarea :deep(.el-textarea__inner) {
      border: none; box-shadow: none; padding: 0; font-size: 15px;
      line-height: 1.6; color: #000; resize: none; background: transparent;
    }
  }

  /* 封面上传 */
  .m-cover-row {
    padding: 16px;
    .m-cover-upload :deep(.el-upload) { 
      width: 100%; height: 160px; background: #fff; border-radius: 16px; overflow: hidden; 
      border: 2px dashed #E5E5EA; transition: border-color 0.3s;
      &:hover { border-color: #007aff; }
    }
    .m-cover-img { width: 100%; height: 100%; object-fit: cover; }
    .m-cover-empty { display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 8px; height: 100%; color: #c7c7cc; font-size: 14px; }
  }

  .m-bottom-bar {
    position: fixed; 
    bottom: 0; /* 修改为置?*/
    left: 0; 
    right: 0; 
    height: 90px; 
    z-index: 999;
    display: flex; 
    background: rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(15px);
    border-top: 0.5px solid rgba(0, 0, 0, 0.05);
    padding: 12px 20px 30px; /* 增加底部 padding 适配全面?*/
    gap: 12px;
    box-shadow: 0 -4px 20px rgba(0,0,0,0.05);

    .m-bar-btn {
      flex: 1; border: none; border-radius: 12px; font-size: 16px;
      font-weight: 700; cursor: pointer; transition: opacity 0.15s;
      &:active { opacity: 0.7; }
      &:disabled { opacity: 0.5; }
      &.draft { background: #f2f2f7; color: #000; }
      &.publish { background: #007aff; color: #fff; }
    }
  }
}
</style>
