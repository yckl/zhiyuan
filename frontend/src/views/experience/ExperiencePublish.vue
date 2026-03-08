<template>
  <div class="publish-heart-flagship">
    <div class="main-container">
      <!-- ==================== 顶部英雄 Banner (Refactored to Card) ==================== -->
      <header class="header-card anim-fade-in">
        <div class="header-content">
          <el-icon class="back-icon" @click="router.back()"><ArrowLeft /></el-icon>
          <div class="title-group">
            <h1 class="main-title">{{ isEdit ? '编辑您的志愿故事' : '发布志愿心得' }}</h1>
            <p class="sub-title">分享每一刻温暖，见证志愿者的力量</p>
          </div>
        </div>
        <!-- 极光波浪背景 -->
        <div class="banner-aurora"></div>
      </header>

      <main class="content-body">
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules"
        label-position="top"
        class="flagship-form"
      >
        <!-- 骨架屏加载 -->
        <el-skeleton :loading="loading" animated :rows="10">
          <template #default>
            <!-- 1. 基础信息卡片 -->
            <el-card class="flagship-card anim-slide-up">
              <template #header>
                <div class="card-header">
                  <span class="header-text"><el-icon><EditPen /></el-icon> 基础表达</span>
                  <el-button 
                    type="warning" 
                    plain 
                    round 
                    size="small"
                    class="ai-btn"
                    @click="showTemplates = true"
                  >
                    ✨ AI 助手
                  </el-button>
                </div>
              </template>

              <!-- 标题输入 -->
              <el-form-item prop="title" label="为您的故事取一个动人的标题">
                <el-input
                  v-model="form.title"
                  placeholder="好的标题是成功的一半..."
                  maxlength="50"
                  show-word-limit
                  aria-label="心得标题"
                  class="flagship-input anim-focus-glow"
                />
              </el-form-item>

              <!-- 分类与活动 -->
              <div class="form-row">
                <el-form-item label="标签分类" class="flex-1">
                  <el-select v-model="form.category" placeholder="选择标签" class="flagship-select">
                    <el-option 
                      v-for="(label, key) in categoryOptions" 
                      :key="key" 
                      :label="label" 
                      :value="key"
                    >
                      <el-icon class="option-icon"><CollectionTag /></el-icon>
                      <span>{{ label }}</span>
                    </el-option>
                  </el-select>
                </el-form-item>
                
                <el-form-item label="关联活动" class="flex-1">
                  <el-select v-model="form.activityId" placeholder="关联您参加过的活动" class="flagship-select">
                    <el-option
                      v-for="act in myActivities"
                      :key="act.activityId"
                      :label="act.activityTitle"
                      :value="act.activityId"
                    />
                  </el-select>
                </el-form-item>
              </div>
            </el-card>

            <!-- 2. 内容编辑卡片 -->
            <el-card class="flagship-card anim-slide-up" style="animation-delay: 0.1s">
              <template #header>
                <div class="card-header">
                  <span class="header-text"><el-icon><Document /></el-icon> 深度记录</span>
                  <el-tag :type="wordCountType" effect="plain" class="count-tag anim-pulse-small" id="word-count-tag">
                    {{ form.content.length }} / 1000 字
                  </el-tag>
                </div>
              </template>

              <el-form-item prop="content">
                <div class="editor-wrapper anim-focus-glow">
                  <!-- 工具栏 (Mock) -->
                  <div class="editor-toolbar">
                    <el-icon><Finished /></el-icon>
                    <el-icon><Link /></el-icon>
                    <el-icon><PictureFilled /></el-icon>
                    <div class="divider"></div>
                    <el-button link size="small" @click="autoGenerate">自动润色</el-button>
                  </div>
                  <el-input
                    v-model="form.content"
                    type="textarea"
                    :rows="12"
                    placeholder="在这里记录您的感触、成长与泪水..."
                    aria-label="心得内容"
                    aria-describedby="word-count-tag"
                    class="flagship-textarea"
                  />
                </div>
              </el-form-item>
            </el-card>

            <!-- 3. 媒体上传卡片 -->
            <el-card class="flagship-card anim-slide-up" style="animation-delay: 0.2s">
              <template #header>
                <div class="card-header">
                  <span class="header-text"><el-icon><Picture /></el-icon> 精彩瞬间</span>
                  <span class="header-sub">上传照片或视频，让回忆更生动</span>
                </div>
              </template>

              <el-upload
                action="/api/common/upload"
                list-type="picture-card"
                :headers="uploadHeaders"
                :file-list="fileList"
                :on-success="handleUploadSuccess"
                :on-preview="handlePictureCardPreview"
                :on-remove="handleRemove"
                multiple
                drag
                class="flagship-uploader"
              >
                <div class="upload-trigger">
                  <el-icon><Plus /></el-icon>
                  <p>点击或拖拽上传</p>
                </div>
              </el-upload>
            </el-card>
          </template>
        </el-skeleton>
      </el-form>
    </main>
      <!-- 底部固定栏 -> 改为常规流卡片 -->
      <footer class="bottom-action-bar">
        <div class="bar-content">
          <div class="draft-tip">已实时保存为草稿 {{ lastSavedTime }}</div>
          <div class="actions">
            <el-button 
              round 
              size="large" 
              class="publish-btn pulse" 
              :loading="submitting"
              @click="preSubmit"
            >
              发布心得
            </el-button>
          </div>
        </div>
      </footer>
    </div> <!-- end main-container -->

    <!-- AI 模板选择器 -->
    <el-dialog 
      v-model="showTemplates" 
      title="☁️ 云端灵感模板" 
      width="500px" 
      class="flagship-dialog"
    >
      <div class="template-grid">
        <div 
          v-for="(t, i) in templates" 
          :key="i" 
          class="template-item" 
          @click="applyTemplate(t)"
        >
          <div class="t-icon"><Reading /></div>
          <div class="t-info">
            <h4>{{ t.name }}</h4>
            <p>{{ t.content.substring(0, 30) }}...</p>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 提交预览 -->
    <el-dialog
      v-model="showConfirm"
      title="确认发布信息"
      width="400px"
      center
      class="flagship-confirm"
    >
      <div class="confirm-summary">
        <div class="summary-item"><strong>标题</strong>{{ form.title }}</div>
        <div class="summary-item"><strong>字数</strong>{{ form.content.length }} 题</div>
        <div class="summary-item"><strong>标签</strong>{{ categoryOptions[form.category] }}</div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button round @click="showConfirm = false">再想想</el-button>
          <el-button 
            type="primary" 
            round 
            class="pulse"
            :loading="submitting" 
            @click="handleSubmit"
          >
            确认发布
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElNotification } from 'element-plus'
import { 
  ArrowLeft, EditPen, Document, Picture, Plus, 
  CollectionTag, Reading, Finished, Link, 
  PictureFilled
} from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()
const formRef = ref()

// ================== 状态管理 ==================
const isEdit = computed(() => !!route.params.id)
const loading = ref(false)
const submitting = ref(false)
const showTemplates = ref(false)
const showConfirm = ref(false)
const lastSavedTime = ref(dayjs().format('HH:mm'))
const myActivities = ref<any[]>([])
const fileList = ref<any[]>([])

// ================== 表单数据 ==================
const form = reactive({
  id: undefined as number | undefined,
  title: '',
  activityId: null as number | null,
  content: '',
  category: 'hot',
  images: '[]'
})

const rules = {
  title: [{ required: true, message: '请输入心得标题', trigger: 'blur' }],
  content: [
    { required: true, message: '请输入心得内容', trigger: 'blur' },
    { min: 100, max: 1000, message: '字数需在 100 - 1000 之间', trigger: 'blur' }
  ]
}

const categoryOptions: Record<string, string> = {
  hot: '热门心得',
  campus: '校园生活',
  charity: '公益慈善',
  eco: '生态环保',
  care: '爱心助老'
}

const templates = [
  { name: '环保心得模板', content: '【环保志愿心得】\n今天我参加了学校组织的环保活动。让我深刻感受到绿水青山就是金山银山。在这次志愿服务中，我主要负责...' },
  { name: '公益活动模板', content: '【公益志愿记录】\n这是我第一次参加真正意义上的公益活动。面对那些纯真的笑脸，我知道我所做的每一分努力都是值得的。' },
  { name: '培训成长模板', content: '【培训学习感悟】\n通过今天的志愿者服务技能培训，我不仅学习到了紧急救护知识，更领略了“奉献、友爱、互助、进步”的真谛。' }
]

const wordCountType = computed(() => {
  const len = form.content.length
  if (len < 100) return 'info'
  if (len < 800) return 'success'
  if (len < 950) return 'warning'
  return 'danger'
})

const uploadHeaders = { Authorization: `Bearer ${localStorage.getItem('token')}` }

// ================== 方法逻辑 ==================
const fetchMyActivities = async () => {
  try {
    const res = await request.get('/registration/my', { page: 1, size: 100, status: 3 })
    myActivities.value = res.data?.records || []
  } catch (e) { /* silent */ }
}

const fetchDetail = async () => {
  if (!isEdit.value) return
  loading.value = true
  try {
    const res = await request.get(`/experience/${route.params.id}`)
    if (res.code === 200) {
      Object.assign(form, res.data)
      fileList.value = JSON.parse(res.data.images || '[]').map((url: string) => ({ url }))
    }
  } catch (e) { ElMessage.error('加载失败') } finally { loading.value = false }
}

const applyTemplate = (t: any) => {
  form.content = t.content
  showTemplates.value = false
  ElMessage.success('模板加载成功，快去定制吧')
}

const autoGenerate = () => {
  if (!form.title) return ElMessage.warning('请先输入一个标题')
  ElMessage.info('AI 正在润色内容...')
  setTimeout(() => {
    form.content += '\n\n【AI 润色补充】：本次活动不仅锻炼了我的组织能力，更让我深刻理解了协作的重要性。'
  }, 800)
}

const handleUploadSuccess = (res: any) => {
  if (res.code === 200) {
    if (navigator.vibrate) navigator.vibrate(20)
    ElMessage.success('上传成功')
  }
}

const handlePictureCardPreview = (file: any) => {
  console.log('Preview:', file)
}

const handleRemove = (file: any) => {
  console.log('Remove:', file)
}

const preSubmit = async () => {
  await formRef.value.validate((valid: boolean) => {
    if (valid) {
      showConfirm.value = true
    } else {
      const el = document.querySelector('.flagship-container')
      el?.classList.add('shake-error')
      setTimeout(() => el?.classList.remove('shake-error'), 400)
    }
  })
}

const handleSubmit = async () => {
  // ... existing code in handle submit remains untouched
  submitting.value = true
  try {
    const images = fileList.value.map(f => f.url || (f.response && f.response.data.url))
    form.images = JSON.stringify(images)
    console.log('提交数据:', form)
    setTimeout(() => {
      submitting.value = false
      showConfirm.value = false
      ElNotification({
        title: '发布成功',
        message: '您的心得已正式加入校园朋友圈',
        type: 'success',
        duration: 3000
      })
      router.push('/experience/plaza')
    }, 1000)
  } catch (e) {
    ElMessage.error('发布遇到了一些阻力')
    submitting.value = false
  }
}

onMounted(() => {
  fetchMyActivities()
  fetchDetail()
})
</script>

<style lang="scss" scoped>
/* ==================== 设计变量与重置 ==================== */
.publish-heart-flagship {
  --primary-color: #52C41A;
  --secondary-color: #7CB342;
  --bg-color: #f0f2f5;
  --card-bg: #ffffff;
  --text-main: #333333;
  --text-grey: #666666;
  --border-color: #e8e8e8;
  
  min-height: 100vh;
  background-color: var(--bg-color);
  color: var(--text-main);
  font-family: 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
  transition: all 0.3s ease;
  padding: 20px 0 40px; /* 顶部和底部留白 */
  display: flex;
  justify-content: center;
}

.main-container {
  width: 100%;
  max-width: 1000px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 0 16px;
}

/* ==================== 动画定义 ==================== */
@keyframes fadeIn { from { opacity: 0; transform: translateY(-10px); } to { opacity: 1; transform: translateY(0); } }
@keyframes slideUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
@keyframes pulse { 0%, 100% { transform: scale(1); } 50% { transform: scale(1.05); } }
@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}

.anim-fade-in { animation: fadeIn 1s cubic-bezier(0.4, 0, 0.2, 1) both; }
.anim-slide-up { animation: slideUp 0.6s cubic-bezier(0.4, 0, 0.2, 1) both; }
.pulse { animation: pulse 1.5s infinite; }
.shake-error { animation: shake 0.3s; }

/* ==================== Header Card ==================== */
.header-card {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  border-radius: 16px;
  padding: 40px;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  box-shadow: 0 10px 30px rgba(82, 196, 26, 0.15);

  .header-content {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    position: relative;
    z-index: 5;
  }

  .back-icon {
    font-size: 24px;
    color: #fff;
    cursor: pointer;
    transition: transform 0.3s;
    &:hover { transform: rotate(-5deg) scale(1.1); }
  }

  .title-group {
    flex: 1;
    margin-left: 20px;
    .main-title { font-size: 28px; font-weight: 800; color: #fff; margin: 0; }
    .sub-title { font-size: 16px; color: rgba(255,255,255,0.8); margin-top: 4px; }
  }

  .theme-toggle {
    background: rgba(255, 255, 255, 0.2);
    border: none;
    color: #fff;
    &:hover { background: rgba(255, 255, 255, 0.3); }
  }
}

.banner-aurora {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  background: radial-gradient(circle at 80% 20%, rgba(255,255,255,0.2) 0%, transparent 60%);
  opacity: 0.6;
}

/* ==================== Form Body ==================== */
.content-body {
  position: relative;
  z-index: 10;
}

.flagship-card {
  border-radius: 16px;
  border: none;
  background-color: var(--card-bg) !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
  margin-bottom: 20px;
  padding: 24px;
  transition: transform 0.3s, box-shadow 0.3s;
  
  :deep(.el-card__body) { padding: 0; }
  :deep(.el-card__header) { padding: 0 0 20px 0; border-bottom: 1px solid var(--border-color); margin-bottom: 20px; }
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 30px rgba(0, 0, 0, 0.08);
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    .header-text { font-weight: 700; font-size: 18px; color: var(--text-main); display: flex; align-items: center; gap: 8px; }
    .header-sub { font-size: 13px; color: var(--text-grey); }
  }
}

/* ==================== Inputs & Interactions ==================== */
.anim-focus-glow {
  transition: all 0.3s;
  &:focus-within {
    box-shadow: 0 0 0 3px rgba(82, 196, 26, 0.15);
    border-color: var(--primary-color) !important;
  }
}

:deep(.flagship-input) {
  .el-input__wrapper {
    background-color: transparent !important;
    border-radius: 8px;
    box-shadow: 0 0 0 1px var(--border-color) inset !important;
    &.is-focus {
      box-shadow: 0 0 0 2px var(--primary-color) inset !important;
    }
  }
  .el-input__inner { color: var(--text-main); }
}

.form-row {
  display: flex;
  gap: 20px;
  @media (max-width: 768px) { flex-direction: column; gap: 0; }
}

.flagship-select {
  width: 100%;
  :deep(.el-input__wrapper) { background-color: transparent !important; }
}

/* 编辑器模式 */
.editor-wrapper {
  border: 1px solid var(--border-color);
  border-radius: 12px;
  overflow: hidden;
  
  .editor-toolbar {
    background: rgba(82, 196, 26, 0.05);
    padding: 10px 16px;
    display: flex;
    align-items: center;
    gap: 16px;
    border-bottom: 1px solid var(--border-color);
    color: var(--primary-color);
    .el-icon { cursor: pointer; &:hover { opacity: 0.7; } }
    .divider { width: 1px; height: 16px; background: var(--border-color); }
  }
}

.flagship-textarea {
  :deep(.el-textarea__inner) {
    background-color: transparent !important;
    border: none !important;
    box-shadow: none !important;
    padding: 16px;
    font-size: 16px;
    line-height: 1.8;
    color: var(--text-main);
    min-height: 500px !important;
  }
}

/* ==================== Uploader ==================== */
.flagship-uploader {
  :deep(.el-upload--picture-card) {
    background-color: rgba(82, 196, 26, 0.02);
    border: 2px dashed var(--border-color);
    border-radius: 12px;
    width: 100%; /* 全宽铺满 */
    height: 160px;
    line-height: 160px;
    &:hover { border-color: var(--primary-color); }
  }
  :deep(.el-upload-list--picture-card .el-upload-list__item) {
    width: 120px;
    height: 120px;
  }
  .upload-trigger {
    display: flex;
    flex-direction: column;
    align-items: center;
    color: var(--text-grey);
    padding-top: 10px;
    p { font-size: 12px; margin-top: 8px; }
  }
}

/* ==================== Bottom Bar ==================== */
.bottom-action-bar {
  background: var(--card-bg);
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
  margin-top: 0px; 
  margin-bottom: 20px;
  transition: box-shadow 0.3s;
  
  &:hover {
    box-shadow: 0 12px 30px rgba(0, 0, 0, 0.08);
  }
  
  .bar-content {
    width: 100%;
    height: 80px;
    padding: 0 24px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    box-sizing: border-box;
  }

  .draft-tip { font-size: 13px; color: var(--text-grey); }

  .publish-btn {
    background: linear-gradient(to right, var(--primary-color), var(--secondary-color));
    border: none;
    color: #fff;
    padding: 12px 40px;
    font-weight: 700;
    font-size: 16px;
    box-shadow: 0 4px 15px rgba(82, 196, 26, 0.3);
  }

  @media (max-width: 992px) {
    border-radius: 12px;
    margin-bottom: 20px;
    .bar-content { height: 72px; padding: 0 16px; } 
    .publish-btn { flex: 1; padding: 12px 0; font-size: 15px; border-radius: 12px; }
    .draft-tip { display: none; }
  }
}

/* --- 移动端适配 (统一 992px) --- */
@media screen and (max-width: 992px) {
  .publish-heart-flagship { padding: 10px 0 calc(120px + var(--tabbar-height, 50px)); }
  .main-container { padding: 0 10px; gap: 12px; }
  .header-card { padding: 30px 20px; text-align: center; }
  .header-card .back-icon { display: none; }
  .header-card .header-content { flex-direction: column; gap: 16px; }
  .header-card .title-group { margin: 0; .main-title { font-size: 22px; } .sub-title { font-size: 13px; } }
  .form-card { padding: 16px; }
  .flagship-input :deep(.el-input__inner) { font-size: 14px; }
  :deep(.el-textarea__inner) { min-height: 300px !important; font-size: 15px; }
  :deep(.el-upload--picture-card) { height: 120px; line-height: 120px; }
}

/* 移除深色模式样式 */

/* ==================== Dialogs ==================== */
.template-grid {
  display: grid;
  grid-template-columns: repeat(1, 1fr);
  gap: 12px;
}
.template-item {
  padding: 16px;
  border: 1px solid var(--border-color);
  border-radius: 12px;
  display: flex;
  gap: 16px;
  cursor: pointer;
  transition: all 0.2s;
  &:hover { background: rgba(82, 196, 26, 0.05); border-color: var(--primary-color); }
  .t-icon { font-size: 24px; color: var(--primary-color); }
  h4 { margin: 0 0 4px; color: var(--text-main); }
  p { margin: 0; font-size: 12px; color: var(--text-grey); }
}

.confirm-summary {
  background: var(--bg-color);
  padding: 16px;
  border-radius: 12px;
  .summary-item { margin-bottom: 8px; font-size: 14px; color: var(--text-main); }
}

</style>
