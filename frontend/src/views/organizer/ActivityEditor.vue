<template>
  <div class="activity-editor-premium" v-loading="pageLoading">
    <div class="editor-layout">
      <!-- ==================== 左侧：表单输入区 ==================== -->
      <div class="form-pane" :class="{ 'full-width': isMobile }">
        <div class="pane-header">
          <div class="header-breadcrumb">
            <el-button link @click="router.back()" class="back-btn">
              <el-icon><ArrowLeft /></el-icon> 返回列表
            </el-button>
          </div>
          <h2 class="pane-title">{{ isEdit ? '编辑活动' : '发布新志愿者活动' }}</h2>
          <p class="pane-desc">请完善以下信息，发布后将进入系统审核流程</p>
        </div>

        <el-form ref="formRef" :model="formData" :rules="formRules" label-position="top" size="large">
          <!-- 1. 基础信息 -->
          <el-card class="form-section-card" shadow="never">
            <template #header><div class="card-header"><span>💎 基础信息</span></div></template>
            <el-row :gutter="24">
              <el-col :span="24">
                <el-form-item label="活动标题" prop="title">
                  <el-input 
                    v-model="formData.title" 
                    placeholder="请输入吸引人的活动标题 (如：2024夏季支教志愿者招募)" 
                    maxlength="60" 
                    show-word-limit 
                  />
                </el-form-item>
              </el-col>
              <el-col :span="24" :md="12">
                <el-form-item label="活动分类" prop="categoryId">
                  <el-select v-model="formData.categoryId" placeholder="选择分类" style="width: 100%">
                    <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="24" :md="12">
                <el-form-item label="封面图片" prop="coverImage">
                  <el-upload
                    class="premium-uploader"
                    :action="uploadUrl"
                    :headers="uploadHeaders"
                    :show-file-list="false"
                    :on-success="handleUploadSuccess"
                    :before-upload="beforeUpload"
                  >
                    <div v-if="formData.coverImage" class="uploader-preview">
                      <img :src="formData.coverImage" @error="(e) => ((e.target as HTMLImageElement).src = '/default-cover.jpg')" />
                      <div class="mask"><el-icon><Edit /></el-icon> 更换</div>
                    </div>
                    <div v-else class="uploader-trigger">
                      <el-icon><Plus /></el-icon>
                      <span>上传封面 (建议 16:9)</span>
                    </div>
                  </el-upload>
                </el-form-item>
              </el-col>
            </el-row>
          </el-card>

          <!-- 2. 时间安排 -->
          <el-card class="form-section-card" shadow="never">
            <template #header><div class="card-header"><span>📅 时间安排</span></div></template>
            <el-row :gutter="24">
              <el-col :span="24" :md="12">
                <el-form-item label="活动开始时间" prop="startTime">
                  <el-date-picker v-model="formData.startTime" type="datetime" placeholder="开始时间" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="24" :md="12">
                <el-form-item label="活动结束时间" prop="endTime">
                  <el-date-picker v-model="formData.endTime" type="datetime" placeholder="结束时间" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="报名截止 (留空则默认同开始时间)" prop="registerEnd">
                  <el-date-picker v-model="formData.registerEnd" type="datetime" placeholder="报名截止时间" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-card>

          <!-- 3. 招募参数 -->
          <el-card class="form-section-card" shadow="never">
            <template #header><div class="card-header"><span>📊 招募参数</span></div></template>
            <el-row :gutter="24">
              <el-col :span="8">
                <el-form-item label="人数上限 (0为不限)" prop="maxParticipants">
                  <el-input-number v-model="formData.maxParticipants" :min="0" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="志愿时长 (h)" prop="serviceHours">
                  <el-input-number v-model="formData.serviceHours" :min="0" :step="0.5" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="积分奖励" prop="pointsReward">
                  <el-input-number v-model="formData.pointsReward" :min="0" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-card>

          <!-- 4. 详细内容 -->
          <el-card class="form-section-card last-card" shadow="never">
            <template #header><div class="card-header"><span>📍 地点与描述</span></div></template>
            <el-form-item label="活动地点" prop="location">
              <el-input v-model="formData.location" placeholder="具体的活动地址，如：图书馆二楼大厅" />
            </el-form-item>
            <el-form-item label="联系方式" prop="contactInfo">
              <el-input v-model="formData.contactInfo" placeholder="请输入手机号、群号或负责人称呼" />
            </el-form-item>
            <el-form-item label="活动摘要" prop="summary">
              <el-input 
                v-model="formData.summary" 
                type="textarea" 
                :rows="2" 
                placeholder="在列表中展示的简短描述，吸引用户点击..." 
              />
            </el-form-item>
            <el-form-item label="详细介绍" prop="content">
              <el-input 
                v-model="formData.content" 
                type="textarea" 
                :rows="12" 
                placeholder="详细讲述活动流程、工作内容、要求、报名须知、常见问题等..." 
              />
            </el-form-item>
          </el-card>

          <div class="form-footer-sticky">
            <el-button size="large" @click="router.back()">取消并返回</el-button>
            <el-button type="primary" size="large" class="submit-btn" :loading="submitting" @click="handleSubmit">
              <el-icon v-if="!submitting" style="margin-right: 8px"><Checked /></el-icon>
              {{ isEdit ? '保存所有修改' : '预览无误，立即发布' }}
            </el-button>
          </div>
        </el-form>
      </div>

      <!-- ==================== 右侧：实时模拟器 ==================== -->
      <div class="preview-pane" v-if="!isMobile">
        <div class="simulator-wrapper">
          <div class="simulator-label">
            <div class="pulse-icon"></div>
            实时效果预览 (Mobile Simulator)
          </div>
          
          <div class="phone-frame">
            <div class="phone-notch"></div>
            <div class="phone-screen">
              <!-- Simulator Content (Replicating ActivityDetail.vue UI) -->
              <div class="sim-content">
                <div class="sim-header">
                  <el-icon><ArrowLeft /></el-icon>
                  <span class="sim-header-title">活动详情预览</span>
                  <el-icon><Share /></el-icon>
                </div>
                
                <div class="sim-scroll-area hide-scrollbar">
                  <!-- Hero Image -->
                  <div class="sim-hero">
                    <img :src="formData.coverImage || '/default-cover.jpg'" @error="(e) => ((e.target as HTMLImageElement).src = '/default-cover.jpg')" />
                    <div class="sim-status-tag">招募</div>
                  </div>
                  
                  <div class="sim-main-card">
                    <h2 class="sim-title">{{ formData.title || '活动标题将在此实时同步' }}</h2>
                    <div class="sim-tags">
                      <span class="sim-category">{{ getCategoryName(formData.categoryId) }}</span>
                    </div>
                    
                    <div class="sim-info-grid">
                      <div class="sim-info-item">
                        <el-icon color="#00BFA6"><Clock /></el-icon>
                        <div class="text">
                          <div class="main">{{ formatSimDate(formData.startTime) }}</div>
                          <div class="sub">至 {{ formatSimDate(formData.endTime) }}</div>
                        </div>
                      </div>
                      <div class="sim-info-item">
                        <el-icon color="#FF6B6B"><Location /></el-icon>
                        <div class="text main">{{ formData.location || '具体活动地点待定' }}</div>
                      </div>
                      <div class="sim-info-item">
                        <el-icon color="#409EFF"><OfficeBuilding /></el-icon>
                        <div class="text main">所属组织工作台</div>
                      </div>
                    </div>
                    
                    <div class="sim-progress-box">
                      <div class="header"><span>报名进度</span><span>0 / {{ formData.maxParticipants || '不限' }}</span></div>
                      <div class="bar"><div class="fill" style="width: 15%"></div></div>
                    </div>
                    
                    <div class="sim-rewards-bar">
                      <div class="reward-pill"><el-icon><Timer /></el-icon>{{ formData.serviceHours }}h 时长</div>
                      <div class="reward-pill"><el-icon><Medal /></el-icon>{{ formData.pointsReward }} 积分</div>
                    </div>
                    
                    <div class="sim-section" v-if="formData.summary">
                      <h4 class="title">活动简介</h4>
                      <p class="desc">{{ formData.summary }}</p>
                    </div>
                    
                    <div class="sim-section">
                      <h4 class="title">详细详情</h4>
                      <div class="rich-text" v-html="previewContent"></div>
                    </div>
                  </div>
                </div>
                
                <div class="sim-bottom-bar">
                  <div class="left"><el-icon><Star /></el-icon><span>收藏</span></div>
                  <div class="right-btn btn-locked">立即报名</div>
                </div>
              </div>
            </div>
            <div class="phone-home-indicator"></div>
          </div>
          
          <div class="simulator-footer-hint">
            模拟器基于志愿者端移动 UI 渲染，实际效果以发布后为准。
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules, UploadProps } from 'element-plus'
import { 
  Plus, ArrowLeft, Edit, Checked, 
  Share, Clock, Location, OfficeBuilding,
  Timer, Medal, Star
} from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import { useMobile } from '@/composables/useMobile'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()
const { isMobile } = useMobile()

const formRef = ref<FormInstance>()
const pageLoading = ref(false)
const submitting = ref(false)
const categories = ref<any[]>([])

const isEdit = computed(() => !!route.params.id)
const uploadUrl = '/api/file/upload/cover'
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token')}`
}))

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

const formRules = reactive<FormRules>({
  title: [{ required: true, message: '请填写活动标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择活动分类', trigger: 'change' }],
  startTime: [{ required: true, message: '请设置开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请设置结束时间', trigger: 'change' }],
  location: [{ required: true, message: '请填写活动地点', trigger: 'blur' }],
  content: [{ required: true, message: '请填写详细内容', trigger: 'blur' }]
})

// 工具：去标签
const stripHtml = (html: string) => {
  if (!html) return ''
  const tmp = document.createElement("DIV")
  tmp.innerHTML = html
  return tmp.textContent || tmp.innerText || ""
}

// 预览内容处理（换行转?<p>?
const previewContent = computed(() => {
  if (!formData.content) return '请输入详细内容以供预览...'
  return formData.content.split('\n').map(l => `<p>${l}</p>`).join('')
})

const getCategoryName = (id: number | null) => {
  if (!id) return '未分类'
  const cat = categories.value.find(c => c.id === id)
  return cat ? cat.name : '未分类'
}

const formatSimDate = (date: string) => date ? dayjs(date).format('MM-DD HH:mm') : '待定'

const fetchCategories = async () => {
  try {
    const res = await request.get('/category/list')
    categories.value = res.data || []
  } catch (error) { console.error(error) }
}

const fetchActivity = async () => {
  if (!isEdit.value) return
  pageLoading.value = true
  try {
    const res = await request.get(`/activity/${route.params.id}`)
    const data = res.data || {}
    Object.keys(formData).forEach(key => {
      if (data[key] !== undefined) (formData as any)[key] = data[key]
    })
    // 自动清洗内容 HTML
    formData.content = stripHtml(formData.content)
  } catch (error) {
    ElMessage.error('读取数据失败')
  } finally { pageLoading.value = false }
}

const handleUploadSuccess: UploadProps['onSuccess'] = (res) => {
  if (res.code === 200 && res.data) {
    formData.coverImage = res.data
    ElMessage.success('封面上传成功')
  } else ElMessage.error('封面上传失败')
}

const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) ElMessage.error('只能上传图片文件')
  if (!isLt5M) ElMessage.error('图片文件不能超过 5MB')
  return isImage && isLt5M
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.warning('请检查表单中必填项')
      return
    }
    submitting.value = true
    try {
      if (isEdit.value) {
        await request.put('/activity', formData)
        ElMessage.success('修改已保存')
      } else {
        await request.post('/activity', formData)
        ElMessage.success('活动发布成功，正等待审核')
      }
      setTimeout(() => router.push('/organizer/activity/list'), 1000)
    } catch (e) {
      console.error(e)
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
.activity-editor-premium {
  min-height: 100vh;
  background-color: #f6f8fb;
  padding: 30px;
}

.editor-layout {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  gap: 40px;
  align-items: flex-start;
}

/* ==================== Left Pane (Form) ==================== */
.form-pane {
  flex: 1;
  min-width: 0;
  padding-bottom: 120px !important; /* 关键：确保底部有足够高度把内容顶上去 */
  
  &.full-width { max-width: 100%; padding: 0 0 140px !important; }

  .pane-header {
    margin-bottom: 30px;
    .back-btn { font-size: 14px; color: #64748b; margin-bottom: 12px; }
    .pane-title { font-size: 28px; font-weight: 800; color: #1e293b; margin: 0 0 8px; }
    .pane-desc { font-size: 14px; color: #94a3b8; }
  }
}

.form-section-card {
  border-radius: 20px;
  border: 1px solid rgba(255,255,255,0.5);
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 24px rgba(0,0,0,0.04);
  margin-bottom: 24px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 32px rgba(0,0,0,0.08);
    border-color: rgba(0, 147, 233, 0.3);
  }
  
  .card-header {
    font-size: 16px; font-weight: 800; color: #334155;
    display: flex; align-items: center; gap: 8px;
  }
  
  :deep(.el-card__header) { border-bottom: 1px solid rgba(0,0,0,0.05); padding: 18px 24px; }
  :deep(.el-card__body) { padding: 24px; }
}

.premium-uploader {
  :deep(.el-upload) {
    width: 100%; border: 2px dashed #e2e8f0; border-radius: 12px; height: 120px;
    display: flex; align-items: center; justify-content: center; overflow: hidden; transition: 0.3s;
    &:hover { border-color: #0093E9; }
  }
  .uploader-trigger {
    display: flex; flex-direction: column; align-items: center; gap: 8px; color: #94a3b8;
    .el-icon { font-size: 24px; }
    span { font-size: 13px; }
  }
  .uploader-preview {
    position: relative; width: 100%; height: 120px;
    img { width: 100%; height: 100%; object-fit: cover; }
    .mask {
      position: absolute; inset: 0; background: rgba(0,0,0,0.4); color: #fff;
      display: flex; align-items: center; justify-content: center; gap: 6px; font-size: 14px;
      opacity: 0; transition: 0.3s;
    }
    &:hover .mask { opacity: 1; }
  }
}

.form-footer-sticky {
  position: sticky;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(15px);
  padding: 24px 40px;
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  z-index: 100;
  margin-top: 40px;
  border-top: 1px solid rgba(0,0,0,0.05);
  box-shadow: 0 -10px 40px rgba(0,0,0,0.05);
  transition: all 0.3s ease;

  .submit-btn {
    padding: 0 40px; border-radius: 12px; background: linear-gradient(135deg, #0093E9, #80D0C7); border: none;
    box-shadow: 0 8px 16px rgba(0, 147, 233, 0.2); transition: all 0.3s;
    &:hover { transform: translateY(-2px); box-shadow: 0 12px 24px rgba(0, 147, 233, 0.3); }
  }
}

/* 适配移动端和侧边栏折叠状态?*/
@media (max-width: 992px) {
  .form-footer-sticky {
    position: relative;
    padding: 20px;
    margin-top: 20px;
    flex-direction: column;
    .submit-btn { width: 100%; padding: 12px 0; height: 50px; }
  }
}

.is-collapsed .form-footer-sticky {
  left: 72px;
}

/* ==================== Right Pane (Simulator) ==================== */
.preview-pane {
  width: 380px; position: sticky; top: 30px; flex-shrink: 0;
}

.simulator-wrapper {
  .simulator-label {
    display: flex; align-items: center; gap: 10px; margin-bottom: 20px; font-size: 14px; font-weight: 700; color: #64748b;
    .pulse-icon { width: 8px; height: 8px; background: #34C759; border-radius: 50%; box-shadow: 0 0 0 4px rgba(52, 199, 89, 0.2); animation: pulse 2s infinite; }
  }
}

@keyframes pulse { 0% { box-shadow: 0 0 0 0 rgba(52, 199, 89, 0.7); } 70% { box-shadow: 0 0 0 10px rgba(52, 199, 89, 0); } 100% { box-shadow: 0 0 0 0 rgba(52, 199, 89, 0); } }

.phone-frame {
  width: 375px; height: 760px; background: #1a1a1a; border-radius: 55px; padding: 12px; 
  box-shadow: 0 30px 60px rgba(0,0,0,0.2), inset 0 0 2px 2px rgba(255,255,255,0.1);
  position: relative; overflow: hidden;
  border: 2px solid rgba(255,255,255,0.05);
  
  .phone-notch {
    position: absolute; top: 0; left: 50%; transform: translateX(-50%); width: 160px; height: 34px; background: #1a1a1a;
    border-radius: 0 0 20px 20px; z-index: 20;
    &::after { 
      content: ''; position: absolute; top: 12px; right: 40px; width: 40px; height: 6px; 
      background: #333; border-radius: 10px;
    }
  }
  
  .phone-screen {
    width: 100%; height: 100%; background: #fff; border-radius: 43px; overflow: hidden; position: relative;
  }
  
  .phone-home-indicator {
    position: absolute; bottom: 18px; left: 50%; transform: translateX(-50%); width: 120px; height: 5px; background: rgba(0,0,0,0.1); border-radius: 10px;
  }
}

/* Simulator View Elements */
.sim-content {
  display: flex; flex-direction: column; height: 100%; background: #F2F2F7;
  
  .sim-header {
    height: 90px; padding: 45px 20px 0; display: flex; justify-content: space-between; align-items: center;
    background: #fff; border-bottom: 1px solid rgba(0,0,0,0.05);
    .sim-header-title { font-size: 15px; font-weight: 700; }
  }
  
  .sim-scroll-area { flex: 1; overflow-y: auto; padding-bottom: 80px; }
  
  .sim-hero {
    position: relative; height: 210px;
    img { width: 100%; height: 100%; object-fit: cover; }
    .sim-status-tag { 
      position: absolute; bottom: 15px; right: 15px; background: #0093E9; color: #fff; 
      font-size: 11px; padding: 4px 12px; border-radius: 20px; font-weight: 700;
    }
  }
  
  .sim-main-card {
    background: #fff; border-radius: 24px; margin: -20px 12px 12px; position: relative; z-index: 5; padding: 20px;
    box-shadow: 0 4px 15px rgba(0,0,0,0.05);

    .sim-title { font-size: 19px; font-weight: 800; color: #1c1c1e; line-height: 1.4; margin-bottom: 12px; }
    .sim-category { font-size: 11px; padding: 3px 10px; background: #f2f2f7; color: #8e8e93; border-radius: 10px; font-weight: 600; }
    
    .sim-info-grid {
      display: flex; flex-direction: column; gap: 12px; margin-top: 20px;
      .sim-info-item {
        display: flex; gap: 10px; align-items: flex-start;
        .text { font-size: 13px; &.main { color: #1c1c1e; font-weight: 600; } .sub { font-size: 11px; color: #8e8e93; } }
      }
    }
    
    .sim-progress-box {
      margin-top: 20px; padding: 15px; background: #f9f9fb; border-radius: 14px;
      .header { display: flex; justify-content: space-between; font-size: 11px; font-weight: 700; margin-bottom: 8px; }
      .bar { height: 6px; background: #e5e5ea; border-radius: 10px; .fill { height: 100%; background: #0093E9; border-radius: 10px; } }
    }
    
    .sim-rewards-bar {
      display: flex; gap: 10px; margin-top: 15px;
      .reward-pill { flex: 1; padding: 8px; background: rgba(0, 147, 233, 0.05); color: #0093E9; border-radius: 10px; font-size: 11px; font-weight: 700; display: flex; align-items: center; justify-content: center; gap: 5px; }
    }
    
    .sim-section {
      margin-top: 25px; border-top: 1px solid #f2f2f7; padding-top: 20px;
      h4 { font-size: 15px; font-weight: 800; color: #1c1c1e; margin: 0 0 10px; }
      .desc { font-size: 13px; color: #64748b; line-height: 1.6; }
      .rich-text { font-size: 13px; color: #1c1c1e; line-height: 1.8; :deep(p) { margin: 8px 0; } }
    }
  }
}

.sim-bottom-bar {
  position: absolute; bottom: 0; left: 0; right: 0; height: 84px; background: #fff;
  border-top: 1px solid #f2f2f7; display: flex; align-items: center; padding: 0 20px 20px; gap: 15px;
  .left { display: flex; flex-direction: column; align-items: center; gap: 2px; color: #8e8e93; font-size: 10px; font-weight: 600; }
  .right-btn { flex: 1; height: 48px; background: #0093E9; color: #fff; border-radius: 24px; display: flex; align-items: center; justify-content: center; font-weight: 800; font-size: 15px; }
  .btn-locked { background: #e5e5ea; color: #aeaeb2; }
}

.simulator-footer-hint {
  margin-top: 20px; font-size: 12px; color: #94a3b8; text-align: center; font-style: italic; line-height: 1.5;
}

/* ==================== Resvponsive ==================== */
@media (max-width: 992px) {
  .editor-layout { flex-direction: column; padding: 15px; }
  .activity-editor-premium { padding: 15px; }
  .form-footer-sticky { bottom: 15px; }
}

:deep(.el-form-item__label) { font-weight: 700; color: #475569; padding-bottom: 8px; }
:deep(.el-input__wrapper), :deep(.el-textarea__inner) { border-radius: 12px; background: #fff; box-shadow: 0 0 0 1px #e2e8f0 inset; transition: 0.3s; &:hover { box-shadow: 0 0 0 1px #0093E9 inset; } }
:deep(.el-input__inner), :deep(.el-textarea__inner) { font-size: 14px; color: #1e293b; }
</style>
