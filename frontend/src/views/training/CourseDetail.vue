<template>
  <div class="course-detail-page">
    <!-- 沉浸式渐变头部 -->
    <div class="hero-header">
      <div class="hero-inner">
        <div class="hero-back" @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回</span>
        </div>
        <h1 class="hero-title">{{ course.title || '课程详情' }}</h1>
        <div class="hero-meta">
          <el-tag effect="dark" size="small" style="background: rgba(255,255,255,0.2); border: none">
            {{ course.category || '通识' }}
          </el-tag>
          <span class="meta-chip">
            <el-icon><Clock /></el-icon> {{ course.duration || 30 }}分钟
          </span>
          <span class="meta-chip">
            <el-icon><User /></el-icon> {{ course.instructor || '培训讲师' }}
          </span>
          <span class="meta-chip">
            <el-icon><View /></el-icon> {{ course.viewCount || 0 }} 次观看
          </span>
        </div>
      </div>
      <el-button
        class="hero-collect"
        :type="isCollected ? 'warning' : ''"
        round
        @click="toggleCollection"
        :loading="collectLoading"
      >
        <el-icon><component :is="isCollected ? StarFilled : Star" /></el-icon>
        {{ isCollected ? '已收藏' : '收藏' }}
      </el-button>
    </div>

    <div class="detail-content" v-loading="loading">
      <el-row :gutter="24">
        <!-- 左侧：视频播放器 -->
        <el-col :xs="24" :lg="16">
          <div class="video-section">

            <!-- HTML5 视频播放器 -->
            <div class="video-player">
              <!-- 试看结束遮罩 -->
              <div v-if="showPreviewEnd" class="preview-end-mask">
                <div class="end-content">
                  <el-icon :size="48"><Lock /></el-icon>
                  <h3>试看已结束</h3>
                  <p>登录后即可观看完整视频并参加课后测验</p>
                  <el-button type="primary" size="large" round @click="router.push({ path: '/login', query: { redirect: route.fullPath } })">
                    立即登录
                  </el-button>
                </div>
              </div>

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
              <!-- 游客锁定遮罩 -->
              <div v-if="!isLoggedIn" class="exam-guest-mask">
                <div class="lock-content">
                  <el-icon :size="32"><Lock /></el-icon>
                  <h4>课后测验已锁定</h4>
                  <p>请先观看完整视频并登录</p>
                </div>
              </div>

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
                    <span class="value">{{ passScore }} 题</span>
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
                  <el-icon :size="48">
                    <CircleCheck v-if="examResult.passed" />
                    <CircleClose v-else />
                  </el-icon>
                </div>

                <div class="result-score">{{ examResult.score }} 题</div>

                <div class="result-text">
                  {{ examResult.passed ? '🎉 恭喜通过测验！课程已完成' : '😢 未能通过，请查看正确答案后重新答题' }}
                </div>

                <div class="result-detail">
                  <div class="detail-row">
                    <span>答对题数</span>
                    <span>{{ examResult.correctCount }} / {{ questions.length }}</span>
                  </div>
                </div>

                <!-- 答案解析 -->
                <div class="answer-review">
                  <h4 class="review-title">📋 答案解析</h4>
                  <div
                    v-for="(q, idx) in questions"
                    :key="q.id"
                    class="review-item"
                    :class="{ correct: userAnswers[idx] === q.correctAnswer, wrong: userAnswers[idx] !== q.correctAnswer }"
                  >
                    <div class="review-question">{{ idx + 1 }}. {{ q.question }}</div>
                    <div class="review-answers">
                      <div class="review-answer">
                        <span class="answer-label">你的答案：</span>
                        <span :class="userAnswers[idx] === q.correctAnswer ? 'text-success' : 'text-danger'">
                          {{ userAnswers[idx] || '未作答' }}
                        </span>
                      </div>
                      <div class="review-answer">
                        <span class="answer-label">正确答案：</span>
                        <span class="text-success">{{ q.correctAnswer }}</span>
                      </div>
                    </div>
                    <div class="review-explanation" v-if="q.explanation">
                      💡 {{ q.explanation }}
                    </div>
                  </div>
                </div>

                <div class="result-actions">
                  <el-button type="primary" @click="resetExam">
                    {{ examResult.passed ? '再次挑战' : '重新答题' }}
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

      <!-- 评论区 -->
      <CommentSection targetType="COURSE" :targetId="courseId" :count="0" />

      <!-- 推荐课程 -->
      <div class="recommend-section" v-if="recommendedCourses.length">
        <div class="section-header">
          <h3 class="section-title">✨ 猜你喜欢</h3>
        </div>
        <div class="recommend-grid-container">
          <div class="recommend-grid">
            <div
              v-for="(rc, idx) in (isMobile ? recommendedCourses.slice(0, 4) : recommendedCourses.slice(0, 8))"
              :key="rc.id"
              class="recommend-card"
              :style="{ animationDelay: `${idx * 0.05}s` }"
              @click="goToCourse(rc.id)"
            >
              <div class="rec-cover">
                <el-image :src="rc.coverImage" fit="cover" lazy>
                  <template #error>
                    <div class="rec-placeholder" :style="{ background: rc.color || 'linear-gradient(135deg, #667eea, #764ba2)' }">
                      <span>{{ rc.title?.slice(0, 2) }}</span>
                    </div>
                  </template>
                </el-image>
                <div class="rec-badge" v-if="rc.category">{{ rc.category }}</div>
              </div>
              <div class="rec-info">
                <h5 class="rec-title">{{ rc.title }}</h5>
                <div class="rec-meta">
                  <el-icon><View /></el-icon> {{ rc.viewCount || 0 }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 固定底部操作栏 -->
    <div class="fixed-bottom-bar">
      <div class="bar-icons">
        <div class="bar-icon" @click="toggleCollection">
          <el-icon :size="20" :color="isCollected ? '#E6A23C' : '#999'">
            <StarFilled v-if="isCollected" /><Star v-else />
          </el-icon>
          <span>{{ isCollected ? '已收藏' : '收藏' }}</span>
        </div>
        <div class="bar-icon" @click="handleShare">
          <el-icon :size="20" color="#999"><Share /></el-icon>
          <span>分享</span>
        </div>
      </div>
      <button class="bar-btn" @click="handleBarButtonClick">
        {{ hasPassed ? '再次挑战' : '开始学习' }}
      </button>
    </div>

    <!-- 通过弹窗 -->
    <el-dialog v-model="showSuccessDialog" title="🎉 恭喜通过！" width="400px" center>
      <div class="success-dialog-content">
        <div class="success-icon">🏆</div>
        <p class="success-text">您已成功通过「{{ course.title }}」的测验</p>
      </div>
      <template #footer>
        <el-button type="primary" @click="showSuccessDialog = false">太棒了！</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Clock, User, View, VideoPlay, Trophy, CircleCheck, CircleClose, Star, StarFilled, Share, Lock } from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import CommentSection from '@/components/CommentSection.vue'

const isLoggedIn = computed(() => !!localStorage.getItem('token'))
const showPreviewEnd = ref(false)

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
  explanation?: string
}

interface ExamResult {
  score: number
  passed: boolean
  correctCount: number
  pointsEarned?: number
}

const route = useRoute()
const router = useRouter()
const courseId = ref(Number(route.params.id))

const windowWidth = ref(window.innerWidth)
const isMobile = computed(() => windowWidth.value < 992)
onMounted(() => window.addEventListener('resize', () => { windowWidth.value = window.innerWidth }))

const loading = ref(false)
const course = ref<Course>({ id: 0, title: '' })
const videoRef = ref<HTMLVideoElement | null>(null)
const videoProgress = ref(0)
const isCollected = ref(false)
const collectLoading = ref(false)

const checkCollectionStatus = async () => {
    try {
        const res = await request.get('/collection/check', {
            targetId: courseId.value,
            targetType: 'COURSE'
        })
        if (res.code === 200) {
            isCollected.value = res.data
        }
    } catch (e) {
        console.error('获取收藏状态失败', e)
    }
}

const toggleCollection = async () => {
    collectLoading.value = true
    try {
        const res = await request.post('/collection/toggle', {
            targetId: courseId.value,
            targetType: 'COURSE'
        })
        if (res.code === 200) {
            isCollected.value = !isCollected.value
            ElMessage.success(isCollected.value ? '收藏成功' : '已取消收藏')
        }
    } catch (e) {
        ElMessage.error('操作失败')
    } finally {
        collectLoading.value = false
    }
}

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

const questionCount = ref(3)
const passScore = 30

// 默认测试视频 (带兜底)
const defaultVideoUrl = 'https://outin-f8d5ab408e0611ea89ac00163e1c94a4.oss-cn-shanghai.aliyuncs.com/sv/84e1bba/84e1bba-1-16.mp4'
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

const fetchCourse = async () => {
  loading.value = true
  try {
    const res = await request.get(`/course/${courseId.value}`)
    if (res.code === 200 && res.data) {
      course.value = res.data
    }
  } catch (error) {
    console.error('获取课程详情失败:', error)
    // 使用模拟数据
    course.value = {
      id: courseId.value,
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
    const res = await request.get(`/course/exam/passed?courseId=${courseId.value}`)
    if (res.code === 200) {
      hasPassed.value = res.data === true
    }
  } catch (error) {
    console.error('检查通过状态失败', error)
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
    
    // 🚦 30秒试看逻辑
    if (!isLoggedIn.value && currentTime >= 30) {
      videoRef.value.pause()
      videoRef.value.currentTime = 30
      showPreviewEnd.value = true
    }

    if (duration > 0) {
      videoProgress.value = Math.round((currentTime / duration) * 100)
    }
  }
}

const startExam = async () => {
  loadingQuestions.value = true
  try {
    const res = await request.get(`/course/exam/paper?courseId=${courseId.value}`)
    if (res.code === 200 && res.data?.questions?.length > 0) {
      // 映射后端格式 (content + options[]) => 前端格式 (question + optionA/B/C/D)
      questions.value = res.data.questions.map((q: any) => ({
        id: q.id,
        question: q.content,
        optionA: q.options?.[0] || '',
        optionB: q.options?.[1] || '',
        optionC: q.options?.[2] || '',
        optionD: q.options?.[3] || '',
        correctAnswer: q.correctAnswer || '',
        explanation: q.explanation || ''
      }))
      questionCount.value = questions.value.length
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
      correctAnswer: 'B',
      explanation: '志愿服务的核心精神是"奉献、友爱、互助、进步"，这是中国志愿服务精神的高度概括。'
    },
    {
      id: 2,
      question: '在志愿服务中遇到紧急情况应该首先做什么？',
      optionA: '立即离开现场',
      optionB: '先拍照发朋友圈',
      optionC: '保持冷静，评估情况并寻求帮助',
      optionD: '置之不理',
      correctAnswer: 'C',
      explanation: '遇到紧急情况应该首先保持冷静，评估情况并寻求帮助，确保自身和他人安全。'
    },
    {
      id: 3,
      question: '以下哪项不属于志愿者的基本权利？',
      optionA: '获得培训的权利',
      optionB: '获得必要的安全保障',
      optionC: '无条件获得报酬',
      optionD: '了解服务内容的权利',
      correctAnswer: 'C',
      explanation: '志愿服务是自愿、无偿的。“无条件获得报酬”不属于志愿者的基本权利。'
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
      courseId: courseId.value,
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

// ================== 推荐课程 ==================
const recommendedCourses = ref<any[]>([])

const handleShare = () => {
  if (navigator.share) {
    navigator.share({ title: course.value.title, url: window.location.href })
  } else {
    import('@/utils/clipboard').then(({ copyToClipboard }) => {
      copyToClipboard(window.location.href, '详情链接已复制')
    })
  }
}

const scrollToExam = () => {
  const el = document.querySelector('.exam-section')
  el?.scrollIntoView({ behavior: 'smooth' })
}

const handleBarButtonClick = () => {
  if (!isLoggedIn.value) {
    ElMessage.info('请先登录后开始学习')
    // 记忆回跳：统一把参数放进 redirect 字符串中
    const redirectUrl = `${route.fullPath}${route.fullPath.includes('?') ? '&' : '?'}autoStart=true`
    router.push({ 
      path: '/login', 
      query: { 
        redirect: redirectUrl
      } 
    })
    return
  }
  scrollToExam()
}

const goToCourse = (id: number) => {
  router.push(`/training/detail/${id}`)
}

const fetchRecommendedCourses = async () => {
  try {
    const res = await request.get('/course/recommend', { excludeId: courseId.value, limit: 10 })
    if (res.code === 200 && res.data?.length) {
      const colors = [
        'linear-gradient(135deg, #667eea, #764ba2)',
        'linear-gradient(135deg, #f093fb, #f5576c)',
        'linear-gradient(135deg, #4facfe, #00f2fe)',
        'linear-gradient(135deg, #43e97b, #38f9d7)',
        'linear-gradient(135deg, #fa709a, #fee140)',
        'linear-gradient(135deg, #fdfbfb, #ebedee)',
        'linear-gradient(135deg, #ff9a9e, #fecfef)',
        'linear-gradient(135deg, #a18cd1, #fbc2eb)'
      ]
      recommendedCourses.value = res.data.map((c: any, i: number) => ({
        ...c,
        color: colors[i % colors.length]
      }))
    }
  } catch (e) {
    console.error('获取推荐课程失败:', e)
  }
}

const trackCourseView = async () => {
  try {
    await request.post('/course/recommend/click', {}, { params: { courseId: courseId.value } })
  } catch (e) {
    console.error('Track course view failed', e)
  }
}

const initData = async () => {
  loading.value = true
  hasPassed.value = false
  examState.value = 'idle'
  videoProgress.value = 0
  
  await fetchCourse()
  if (isLoggedIn.value) {
    await trackCourseView()
    checkPassStatus()
    checkCollectionStatus()

    // 处理记忆回跳 autoStart
    if (route.query.autoStart === 'true') {
      setTimeout(() => {
        scrollToExam()
        // 清理清理 url 参数
        router.replace({ path: route.path, query: { ...route.query, autoStart: undefined } })
      }, 500)
    }
  }
  fetchRecommendedCourses()
}

watch(() => route.params.id, (newId) => {
  if (newId && route.path.includes('/training/detail')) {
    courseId.value = Number(newId)
    initData()
  }
})

onMounted(() => {
  initData()
})
</script>

<style lang="scss" scoped>
.course-detail-page {
  padding: 0;
  background: var(--el-bg-color-page);
  min-height: 100vh;
}

// ================================================================
// 沉浸式渐变头部
// ================================================================
.hero-header {
  background: linear-gradient(135deg, #00BFA6 0%, #009688 40%, #00695C 100%);
  padding: 24px;
  padding-top: 20px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  animation: fadeIn 0.4s ease-out;
}

.hero-inner {
  flex: 1;
}

.hero-back {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 13px;
  cursor: pointer;
  margin-bottom: 12px;
  transition: color 0.2s;

  &:hover { color: #fff; }
}

.hero-title {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 10px;
  text-shadow: 0 1px 8px rgba(0, 0, 0, 0.15);
}

.hero-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.meta-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: rgba(255, 255, 255, 0.85);
  font-size: 13px;
}

.hero-collect {
  flex-shrink: 0;
  margin-top: 8px;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-8px); }
  to { opacity: 1; transform: translateY(0); }
}

// ================================================================
// 内容区
// ================================================================
.detail-content {
  padding: 20px;
}

.video-section {
  background: var(--el-bg-color);
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: box-shadow 0.3s;
  animation: fadeIn 0.4s ease-out 0.1s both;

  &:hover { box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08); }
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

  .video-progress {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    z-index: 5;
  }

  .preview-end-mask {
    position: absolute;
    inset: 0;
    z-index: 20;
    background: rgba(0, 0, 0, 0.85);
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
    display: flex;
    align-items: center;
    justify-content: center;
    text-align: center;
    color: #fff;
    animation: fadeIn 0.4s ease-out;

    .end-content {
      padding: 0 20px;
      h3 { margin: 16px 0 8px; font-size: 20px; }
      p { margin: 0 0 24px; font-size: 14px; opacity: 0.8; }
    }
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
    color: var(--el-text-color-secondary);
    white-space: nowrap;
  }
}

.course-summary {
  h3 {
    margin: 0 0 12px;
    font-size: 16px;
    color: var(--el-text-color-primary);
  }

  .summary-content {
    font-size: 14px;
    line-height: 1.8;
    color: var(--el-text-color-regular);

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
  transition: box-shadow 0.3s;

  &:hover { box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08) !important; }

  .exam-guest-mask {
    position: absolute;
    inset: 0;
    z-index: 10;
    background: rgba(255, 255, 255, 0.6);
    backdrop-filter: blur(4px);
    -webkit-backdrop-filter: blur(4px);
    display: flex;
    align-items: center;
    justify-content: center;
    text-align: center;
    border-radius: 12px;

    .lock-content {
      color: #909399;
      h4 { margin: 12px 0 4px; font-size: 16px; color: #606266; }
      p { margin: 0; font-size: 13px; }
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

@keyframes bounceIn {
  0% { opacity: 0; transform: scale(0.3); }
  50% { opacity: 1; transform: scale(1.05); }
  70% { transform: scale(0.95); }
  100% { transform: scale(1); }
}

@keyframes glowPulse {
  0%, 100% { box-shadow: 0 4px 16px rgba(64, 158, 255, 0.25); }
  50% { box-shadow: 0 4px 24px rgba(64, 158, 255, 0.5); }
}

.exam-idle {
  .exam-info {
    margin-bottom: 20px;

    .info-row {
      display: flex;
      justify-content: space-between;
      padding: 12px 0;
      border-bottom: 1px dashed var(--el-border-color);

      &:last-child {
        border-bottom: none;
      }

      .label {
        color: var(--el-text-color-secondary);
      }

      .value {
        font-weight: 500;
        color: var(--el-text-color-primary);

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
    animation: glowPulse 2.5s ease-in-out infinite;
  }

  .exam-tip {
    text-align: center;
    font-size: 12px;
    color: var(--el-text-color-secondary);
    margin: 12px 0 0;
  }
}

.exam-testing {
  .question-progress {
    margin-bottom: 20px;
    font-size: 14px;
    color: var(--el-text-color-regular);
  }

  .question-content {
    margin-bottom: 24px;

    .question-title {
      font-size: 16px;
      margin: 0 0 16px;
      line-height: 1.5;
      color: var(--el-text-color-primary);
    }

    .options-group {
      display: flex;
      flex-direction: column;
      gap: 12px;

      .option-item {
        display: block;
        padding: 12px 16px;
        background: var(--el-fill-color-light);
        border-radius: 8px;
        margin: 0;
        transition: all 0.2s;
        color: var(--el-text-color-regular);

        &:hover {
          background: var(--el-color-primary-light-9);
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
    animation: bounceIn 0.5s ease-out;

    &.passed {
      color: #67c23a;
    }
  }

  .result-score {
    font-size: 48px;
    font-weight: bold;
    color: var(--el-text-color-primary);
    margin-bottom: 8px;
    animation: bounceIn 0.6s ease-out 0.1s both;
  }

  .result-text {
    font-size: 16px;
    color: var(--el-text-color-regular);
    margin-bottom: 20px;
  }

  .result-detail {
    background: var(--el-fill-color-light);
    border-radius: 8px;
    padding: 16px;
    margin-bottom: 20px;

    .detail-row {
      display: flex;
      justify-content: space-between;
      padding: 8px 0;
      color: var(--el-text-color-regular);

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

  .answer-review {
    text-align: left;
    margin-bottom: 20px;

    .review-title {
      font-size: 15px;
      font-weight: 600;
      color: var(--el-text-color-primary);
      margin: 0 0 12px;
    }

    .review-item {
      background: var(--el-fill-color-lighter);
      border-radius: 8px;
      padding: 12px;
      margin-bottom: 10px;
      border-left: 3px solid #dcdfe6;
      transition: all 0.2s;

      &.correct { border-left-color: #67c23a; background: rgba(103, 194, 58, 0.05); }
      &.wrong { border-left-color: #f56c6c; background: rgba(245, 108, 108, 0.05); }
    }

    .review-question {
      font-size: 13px;
      font-weight: 600;
      color: var(--el-text-color-primary);
      margin-bottom: 8px;
    }

    .review-answers {
      display: flex;
      gap: 16px;
      flex-wrap: wrap;
      margin-bottom: 6px;
    }

    .review-answer {
      font-size: 13px;

      .answer-label {
        color: #909399;
      }

      .text-success { color: #67c23a; font-weight: 600; }
      .text-danger { color: #f56c6c; font-weight: 600; }
    }

    .review-explanation {
      font-size: 12px;
      color: #909399;
      padding-top: 6px;
      border-top: 1px dashed #ebeef5;
      margin-top: 4px;
    }
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

// ================================================================
// 评论区
// ================================================================
.comment-section, .recommend-section {
  padding: 0 20px 20px;
}

.section-heading {
  font-size: 17px;
  font-weight: 700;
  color: #333;
  margin: 0 0 14px;
  padding-bottom: 8px;
  border-bottom: 2px solid #00BFA6;
}

.comment-list {
  .comment-item {
    display: flex;
    gap: 10px;
    padding: 12px 0;
    border-bottom: 1px solid rgba(0, 0, 0, 0.04);

    &:last-child { border-bottom: none; }
    :deep(.el-avatar) { flex-shrink: 0; font-weight: 600; }
  }

  .comment-body { flex: 1; }

  .comment-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 4px;
  }

  .comment-user { font-size: 13px; font-weight: 600; color: #333; }
  .comment-time { font-size: 11px; color: #C0C4CC; }

  .comment-text {
    margin: 0;
    font-size: 14px;
    line-height: 1.6;
    color: #666;
  }
}

.comment-empty {
  text-align: center;
  color: #C0C4CC;
  font-size: 14px;
  padding: 20px 0;
  margin: 0;
}

.comment-skeleton {
  .skeleton-comment:not(:last-child) {
    border-bottom: 1px solid rgba(0, 0, 0, 0.04);
  }
}

// ================================================================
// 推荐课程
// ================================================================
.recommend-section {
  padding: 0 16px;
  margin-top: 24px;
  margin-bottom: 24px;

  .section-header {
    margin-bottom: 12px;
  }
  .section-title {
    font-size: 18px;
    font-weight: 700;
    color: #333;
    margin: 0;
  }
}

.recommend-grid-container {
  width: 100%;
  overflow: hidden; // 禁止滑动
  padding-bottom: 8px;
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr); // 手机端两两排列
  gap: 12px;
  
  @media (min-width: 992px) {
    grid-template-columns: repeat(8, 1fr); // PC端单行?个，不可滑动
    gap: 16px;
  }
}

.recommend-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  animation: cardFadeIn 0.4s ease both;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
    .rec-cover :deep(img) { transform: scale(1.1); }
  }

  .rec-cover {
    position: relative;
    height: 80px;
    background: #eee;
    overflow: hidden;

    @media (min-width: 992px) { height: 110px; }

    :deep(.el-image) { width: 100%; height: 100%; }
    :deep(img) { transition: transform 0.5s; }

    .rec-placeholder {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-weight: bold;
      font-size: 16px;
    }

    .rec-badge {
      position: absolute;
      top: 6px;
      right: 6px;
      background: rgba(0, 0, 0, 0.4);
      backdrop-filter: blur(4px);
      color: #fff;
      font-size: 10px;
      padding: 2px 6px;
      border-radius: 4px;
    }
  }

  .rec-info {
    padding: 8px 10px;

    .rec-title {
      font-size: 13px;
      margin: 0 0 4px;
      font-weight: 600;
      color: #333;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .rec-meta {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 11px;
      color: #999;
    }
  }
}

// ================================================================
// 固定底部操作栏
// ================================================================
.fixed-bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  padding: 10px 16px;
  padding-bottom: calc(10px + env(safe-area-inset-bottom, 0));
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  z-index: 100;
  gap: 12px;
}

.bar-icons {
  display: flex;
  gap: 16px;
}

.bar-icon {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  transition: transform 0.2s;

  &:active { transform: scale(0.9); }

  span { font-size: 10px; color: #999; }
}

.bar-btn {
  flex: 1;
  height: 42px;
  border: none;
  border-radius: 21px;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, #00BFA6, #43e97b);
  cursor: pointer;
  transition: all 0.25s;
  letter-spacing: 1px;
  animation: btnGlow 2.5s ease-in-out infinite;

  &:hover {
    box-shadow: 0 6px 18px rgba(0, 191, 166, 0.4);
  }

  &:active { transform: scale(0.97); }
}

@keyframes btnGlow {
  0%, 100% { box-shadow: 0 4px 14px rgba(0, 191, 166, 0.3); }
  50% { box-shadow: 0 4px 24px rgba(0, 191, 166, 0.5); }
}

// 底部占位
.detail-content {
  padding-bottom: 80px;
}
</style>

