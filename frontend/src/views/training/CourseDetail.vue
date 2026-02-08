<template>
  <div class="course-detail-page">
    <!-- 返回按钮 -->
    <div class="back-bar">
      <el-button type="primary" text @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>
        返回课程列表
      </el-button>
    </div>

    <div class="detail-content" v-loading="loading">
      <el-row :gutter="24">
        <!-- 左侧：视频播放器 -->
        <el-col :xs="24" :lg="16">
          <div class="video-section">
            <div class="course-header">
              <h1>{{ course.title }}</h1>
              <div class="course-meta">
                <el-tag :type="getCategoryType(course.category)" size="small">
                  {{ course.category || '通识' }}
                </el-tag>
                <span class="meta-item">
                  <el-icon><Clock /></el-icon>
                  {{ course.duration || 30 }}分钟
                </span>
                <span class="meta-item">
                  <el-icon><User /></el-icon>
                  {{ course.instructor || '培训讲师' }}
                </span>
                <span class="meta-item">
                  <el-icon><View /></el-icon>
                  {{ course.viewCount || 0 }} 次观看
                </span>
              </div>
            </div>

            <!-- HTML5 视频播放器 -->
            <div class="video-player">
              <video
                ref="videoRef"
                :src="course.videoUrl || defaultVideoUrl"
                controls
                preload="metadata"
                @ended="handleVideoEnded"
                @timeupdate="handleTimeUpdate"
              >
                您的浏览器不支持视频播放
              </video>
              <div v-if="!course.videoUrl" class="video-overlay">
                <div class="play-hint">
                  <el-icon :size="48"><VideoPlay /></el-icon>
                  <p>示例视频</p>
                </div>
              </div>
            </div>

            <!-- 视频进度 -->
            <div class="video-progress" v-if="videoProgress > 0">
              <el-progress
                :percentage="videoProgress"
                :stroke-width="6"
                :color="videoProgress >= 80 ? '#67c23a' : '#409eff'"
              />
              <span class="progress-text">观看进度: {{ videoProgress }}%</span>
            </div>

            <!-- 课程描述 -->
            <div class="course-summary">
              <h3>课程简介</h3>
              <div class="summary-content" v-html="course.description || course.summary || defaultDescription"></div>
            </div>
          </div>
        </el-col>

        <!-- 右侧：考试区域 -->
        <el-col :xs="24" :lg="8">
          <div class="exam-section">
            <el-card class="exam-card" shadow="never">
              <template #header>
                <div class="card-header">
                  <span>📝 课后测验</span>
                  <el-tag v-if="hasPassed" type="success" effect="dark">已通过</el-tag>
                </div>
              </template>

              <!-- 未开始考试状态 -->
              <div v-if="examState === 'idle'" class="exam-idle">
                <div class="exam-info">
                  <div class="info-row">
                    <span class="label">题目数量</span>
                    <span class="value">{{ questionCount }} 道单选题</span>
                  </div>
                  <div class="info-row">
                    <span class="label">及格分数</span>
                    <span class="value">{{ passScore }} 分</span>
                  </div>
                  <div class="info-row">
                    <span class="label">通过奖励</span>
                    <span class="value reward">
                      <el-icon><Trophy /></el-icon>
                      {{ course.creditHours || course.points || 20 }} 积分
                    </span>
                  </div>
                </div>

                <el-button
                  type="primary"
                  size="large"
                  class="start-btn"
                  :loading="loadingQuestions"
                  @click="startExam"
                >
                  {{ hasPassed ? '再次挑战' : '开始考试' }}
                </el-button>

                <p class="exam-tip">⚠️ 建议先完整观看视频再参加测验</p>
              </div>

              <!-- 考试进行中 -->
              <div v-else-if="examState === 'testing'" class="exam-testing">
                <div class="question-progress">
                  <span>第 {{ currentQuestionIndex + 1 }} / {{ questions.length }} 题</span>
                  <el-progress
                    :percentage="((currentQuestionIndex + 1) / questions.length) * 100"
                    :show-text="false"
                    :stroke-width="4"
                  />
                </div>

                <div class="question-content">
                  <h4 class="question-title">
                    {{ currentQuestionIndex + 1 }}. {{ currentQuestion?.question }}
                  </h4>

                  <el-radio-group v-model="userAnswers[currentQuestionIndex]" class="options-group">
                    <el-radio :label="'A'" class="option-item">
                      A. {{ currentQuestion?.optionA }}
                    </el-radio>
                    <el-radio :label="'B'" class="option-item">
                      B. {{ currentQuestion?.optionB }}
                    </el-radio>
                    <el-radio :label="'C'" class="option-item">
                      C. {{ currentQuestion?.optionC }}
                    </el-radio>
                    <el-radio :label="'D'" class="option-item">
                      D. {{ currentQuestion?.optionD }}
                    </el-radio>
                  </el-radio-group>
                </div>

                <div class="question-actions">
                  <el-button
                    v-if="currentQuestionIndex > 0"
                    @click="prevQuestion"
                  >
                    上一题
                  </el-button>
                  <el-button
                    v-if="currentQuestionIndex < questions.length - 1"
                    type="primary"
                    :disabled="!userAnswers[currentQuestionIndex]"
                    @click="nextQuestion"
                  >
                    下一题
                  </el-button>
                  <el-button
                    v-else
                    type="success"
                    :disabled="!allAnswered"
                    :loading="submitting"
                    @click="submitExam"
                  >
                    提交试卷
                  </el-button>
                </div>
              </div>

              <!-- 考试结果 -->
              <div v-else-if="examState === 'result'" class="exam-result">
                <div class="result-icon" :class="{ passed: examResult.passed }">
                  <el-icon :size="64">
                    <CircleCheck v-if="examResult.passed" />
                    <CircleClose v-else />
                  </el-icon>
                </div>

                <div class="result-score">{{ examResult.score }} 分</div>

                <div class="result-text">
                  {{ examResult.passed ? '🎉 恭喜通过测验！' : '😢 未能通过，再接再厉' }}
                </div>

                <div class="result-detail">
                  <div class="detail-row">
                    <span>答对题数</span>
                    <span>{{ examResult.correctCount }} / {{ questions.length }}</span>
                  </div>
                  <div class="detail-row" v-if="examResult.passed && examResult.pointsEarned">
                    <span>获得积分</span>
                    <span class="points">+{{ examResult.pointsEarned }}</span>
                  </div>
                </div>

                <div class="result-actions">
                  <el-button type="primary" @click="resetExam">
                    {{ examResult.passed ? '再次挑战' : '重新测验' }}
                  </el-button>
                  <el-button @click="$router.push('/training/my')">
                    查看记录
                  </el-button>
                </div>
              </div>
            </el-card>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 通过弹窗 -->
    <el-dialog v-model="showSuccessDialog" title="🎉 恭喜通过！" width="400px" center>
      <div class="success-dialog-content">
        <div class="success-icon">🏆</div>
        <p class="success-text">您已成功通过「{{ course.title }}」的测验</p>
        <div class="success-reward">
          <span class="label">获得积分</span>
          <span class="value">+{{ examResult.pointsEarned }}</span>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showSuccessDialog = false">太棒了！</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft, Clock, User, View, VideoPlay, Trophy,
  CircleCheck, CircleClose
} from '@element-plus/icons-vue'
import { request } from '@/utils/request'

interface Course {
  id: number
  title: string
  category?: string
  duration?: number
  instructor?: string
  viewCount?: number
  videoUrl?: string
  description?: string
  summary?: string
  creditHours?: number
  points?: number
}

interface Question {
  id: number
  question: string
  optionA: string
  optionB: string
  optionC: string
  optionD: string
  correctAnswer?: string
}

interface ExamResult {
  score: number
  passed: boolean
  correctCount: number
  pointsEarned?: number
}

const route = useRoute()
const courseId = Number(route.params.id)

const loading = ref(false)
const course = ref<Course>({ id: 0, title: '' })
const videoRef = ref<HTMLVideoElement | null>(null)
const videoProgress = ref(0)

// 考试状态
const examState = ref<'idle' | 'testing' | 'result'>('idle')
const loadingQuestions = ref(false)
const submitting = ref(false)
const hasPassed = ref(false)
const questions = ref<Question[]>([])
const userAnswers = ref<string[]>([])
const currentQuestionIndex = ref(0)
const examResult = ref<ExamResult>({ score: 0, passed: false, correctCount: 0 })
const showSuccessDialog = ref(false)

const questionCount = ref(5)
const passScore = 60

const defaultVideoUrl = 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4'
const defaultDescription = `
  <p>本课程将帮助您掌握志愿服务的核心技能和知识，提升您的服务能力。</p>
  <p><strong>学习目标：</strong></p>
  <ul>
    <li>了解志愿服务的基本概念和意义</li>
    <li>掌握与服务对象沟通的技巧</li>
    <li>学习处理突发情况的方法</li>
    <li>培养团队协作精神</li>
  </ul>
`

const currentQuestion = computed(() => questions.value[currentQuestionIndex.value])
const allAnswered = computed(() => userAnswers.value.every(a => a))

const getCategoryType = (category?: string) => {
  const map: Record<string, string> = { '通识': 'primary', '技能': 'success', '安全': 'danger' }
  return map[category || ''] || 'info'
}

const fetchCourse = async () => {
  loading.value = true
  try {
    const res = await request.get(`/course/${courseId}`)
    if (res.code === 200 && res.data) {
      course.value = res.data
    }
  } catch (error) {
    console.error('获取课程详情失败:', error)
    // 使用模拟数据
    course.value = {
      id: courseId,
      title: '志愿服务培训课程',
      category: '通识',
      duration: 30,
      instructor: '培训讲师',
      viewCount: 1234,
      creditHours: 20
    }
  } finally {
    loading.value = false
  }
}

const checkPassStatus = async () => {
  try {
    const res = await request.get(`/course/exam/passed?courseId=${courseId}`)
    if (res.code === 200) {
      hasPassed.value = res.data === true
    }
  } catch (error) {
    console.error('检查通过状态失败:', error)
  }
}

const handleVideoEnded = () => {
  videoProgress.value = 100
  ElMessage.success('视频观看完毕，可以参加测验了！')
}

const handleTimeUpdate = () => {
  if (videoRef.value) {
    const duration = videoRef.value.duration
    const currentTime = videoRef.value.currentTime
    if (duration > 0) {
      videoProgress.value = Math.round((currentTime / duration) * 100)
    }
  }
}

const startExam = async () => {
  loadingQuestions.value = true
  try {
    const res = await request.get(`/course/exam/paper?courseId=${courseId}`)
    if (res.code === 200 && res.data?.questions?.length > 0) {
      questions.value = res.data.questions
      questionCount.value = res.data.questions.length
    } else {
      // 使用模拟题目
      questions.value = generateMockQuestions()
    }
    userAnswers.value = new Array(questions.value.length).fill('')
    currentQuestionIndex.value = 0
    examState.value = 'testing'
  } catch (error) {
    console.error('获取试题失败:', error)
    questions.value = generateMockQuestions()
    userAnswers.value = new Array(questions.value.length).fill('')
    currentQuestionIndex.value = 0
    examState.value = 'testing'
  } finally {
    loadingQuestions.value = false
  }
}

const generateMockQuestions = (): Question[] => {
  return [
    {
      id: 1,
      question: '志愿服务的核心精神是什么？',
      optionA: '追求个人利益',
      optionB: '奉献、友爱、互助、进步',
      optionC: '强制性服务',
      optionD: '获得报酬',
      correctAnswer: 'B'
    },
    {
      id: 2,
      question: '在志愿服务中遇到紧急情况应该首先做什么？',
      optionA: '立即离开现场',
      optionB: '先拍照发朋友圈',
      optionC: '保持冷静，评估情况并寻求帮助',
      optionD: '置之不理',
      correctAnswer: 'C'
    },
    {
      id: 3,
      question: '以下哪项不属于志愿者的基本权利？',
      optionA: '获得培训的权利',
      optionB: '获得必要的安全保障',
      optionC: '无条件获得报酬',
      optionD: '了解服务内容的权利',
      correctAnswer: 'C'
    }
  ]
}

const prevQuestion = () => {
  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value--
  }
}

const nextQuestion = () => {
  if (currentQuestionIndex.value < questions.value.length - 1) {
    currentQuestionIndex.value++
  }
}

const submitExam = async () => {
  submitting.value = true
  try {
    const res = await request.post('/course/exam/submit', {
      courseId,
      answers: questions.value.map((q, i) => ({
        questionId: q.id,
        userAnswer: userAnswers.value[i]
      }))
    })

    if (res.code === 200 && res.data) {
      examResult.value = {
        score: res.data.score || 0,
        passed: res.data.passed || false,
        correctCount: res.data.correctCount || 0,
        pointsEarned: res.data.pointsEarned || 0
      }

      if (res.data.passed) {
        hasPassed.value = true
        showSuccessDialog.value = true
      }
    } else {
      // 本地计算结果（模拟）
      calculateLocalResult()
    }
    examState.value = 'result'
  } catch (error) {
    console.error('提交试卷失败:', error)
    calculateLocalResult()
    examState.value = 'result'
  } finally {
    submitting.value = false
  }
}

const calculateLocalResult = () => {
  let correct = 0
  questions.value.forEach((q, i) => {
    if (userAnswers.value[i] === q.correctAnswer) {
      correct++
    }
  })
  const score = Math.round((correct / questions.value.length) * 100)
  const passed = score >= passScore
  examResult.value = {
    score,
    passed,
    correctCount: correct,
    pointsEarned: passed ? (course.value.creditHours || 20) : 0
  }

  if (passed) {
    hasPassed.value = true
    showSuccessDialog.value = true
  }
}

const resetExam = () => {
  examState.value = 'idle'
  userAnswers.value = []
  currentQuestionIndex.value = 0
}

onMounted(() => {
  fetchCourse()
  checkPassStatus()
})
</script>

<style lang="scss" scoped>
.course-detail-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.back-bar {
  margin-bottom: 16px;
}

.video-section {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.course-header {
  margin-bottom: 20px;

  h1 {
    margin: 0 0 12px;
    font-size: 22px;
  }

  .course-meta {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 16px;

    .meta-item {
      display: flex;
      align-items: center;
      gap: 4px;
      color: #666;
      font-size: 14px;
    }
  }
}

.video-player {
  position: relative;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 16px;

  video {
    width: 100%;
    height: 400px;
    display: block;
  }

  .video-overlay {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.3);
    display: flex;
    align-items: center;
    justify-content: center;
    pointer-events: none;

    .play-hint {
      text-align: center;
      color: rgba(255, 255, 255, 0.8);

      p {
        margin: 8px 0 0;
      }
    }
  }
}

.video-progress {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;

  :deep(.el-progress) {
    flex: 1;
  }

  .progress-text {
    font-size: 13px;
    color: #666;
    white-space: nowrap;
  }
}

.course-summary {
  h3 {
    margin: 0 0 12px;
    font-size: 16px;
    color: #333;
  }

  .summary-content {
    font-size: 14px;
    line-height: 1.8;
    color: #666;

    :deep(ul) {
      padding-left: 20px;
    }

    :deep(li) {
      margin: 8px 0;
    }
  }
}

.exam-section {
  position: sticky;
  top: 20px;
}

.exam-card {
  border-radius: 12px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

.exam-idle {
  .exam-info {
    margin-bottom: 20px;

    .info-row {
      display: flex;
      justify-content: space-between;
      padding: 12px 0;
      border-bottom: 1px dashed #eee;

      &:last-child {
        border-bottom: none;
      }

      .label {
        color: #999;
      }

      .value {
        font-weight: 500;
        color: #333;

        &.reward {
          display: flex;
          align-items: center;
          gap: 4px;
          color: #e6a23c;
        }
      }
    }
  }

  .start-btn {
    width: 100%;
    height: 48px;
    font-size: 16px;
  }

  .exam-tip {
    text-align: center;
    font-size: 12px;
    color: #999;
    margin: 12px 0 0;
  }
}

.exam-testing {
  .question-progress {
    margin-bottom: 20px;
    font-size: 14px;
    color: #666;
  }

  .question-content {
    margin-bottom: 24px;

    .question-title {
      font-size: 16px;
      margin: 0 0 16px;
      line-height: 1.5;
      color: #333;
    }

    .options-group {
      display: flex;
      flex-direction: column;
      gap: 12px;

      .option-item {
        display: block;
        padding: 12px 16px;
        background: #f9f9f9;
        border-radius: 8px;
        margin: 0;
        transition: all 0.2s;

        &:hover {
          background: #f0f7ff;
        }
      }
    }
  }

  .question-actions {
    display: flex;
    gap: 12px;
    justify-content: flex-end;
  }
}

.exam-result {
  text-align: center;

  .result-icon {
    color: #f56c6c;
    margin-bottom: 16px;

    &.passed {
      color: #67c23a;
    }
  }

  .result-score {
    font-size: 48px;
    font-weight: bold;
    color: #333;
    margin-bottom: 8px;
  }

  .result-text {
    font-size: 16px;
    color: #666;
    margin-bottom: 20px;
  }

  .result-detail {
    background: #f9f9f9;
    border-radius: 8px;
    padding: 16px;
    margin-bottom: 20px;

    .detail-row {
      display: flex;
      justify-content: space-between;
      padding: 8px 0;

      .points {
        color: #e6a23c;
        font-weight: bold;
      }
    }
  }

  .result-actions {
    display: flex;
    gap: 12px;
    justify-content: center;
  }
}

.success-dialog-content {
  text-align: center;
  padding: 20px 0;

  .success-icon {
    font-size: 64px;
    margin-bottom: 16px;
  }

  .success-text {
    font-size: 16px;
    color: #333;
    margin-bottom: 20px;
  }

  .success-reward {
    background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
    padding: 16px;
    border-radius: 12px;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .label {
      color: #666;
    }

    .value {
      font-size: 28px;
      font-weight: bold;
      color: #e65100;
    }
  }
}

/* 暗黑模式适配 */
html.dark .course-detail-page {
  background-color: #0f172a !important;
  
  .video-section {
    background-color: #1e293b !important;
    
    h1, h3 {
      color: #f1f5f9 !important;
    }
    
    .meta-item {
      color: #94a3b8 !important;
    }
    
    .summary-content {
      color: #cbd5e1 !important;
    }
  }
  
  .exam-card {
    background-color: #1e293b !important;
    border-color: #334155 !important;
    
    .card-header span {
      color: #f1f5f9 !important;
    }
    
    .info-row {
      border-bottom-color: #334155 !important;
      
      .label {
        color: #94a3b8 !important;
      }
      
      .value {
        color: #e2e8f0 !important;
      }
    }
    
    .exam-tip {
      color: #94a3b8 !important;
    }
  }
  
  .question-title {
    color: #f1f5f9 !important;
  }
  
  .option-item {
    background-color: #334155 !important;
    color: #e2e8f0 !important;
    
    &:hover {
      background-color: #1e3a5f !important;
    }
  }
  
  .result-score {
    color: #f1f5f9 !important;
  }
  
  .result-detail {
    background-color: #1e293b !important;
    color: #e2e8f0 !important;
  }
}
</style>
