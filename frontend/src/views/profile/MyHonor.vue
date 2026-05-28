<template>
  <div class="my-honor-page">

    <!-- ==================== 极光流体 Hero ==================== -->
    <div class="honor-hero">
      <div class="hero-left">
        <el-progress
          type="circle"
          :percentage="levelPercent"
          :width="72"
          :stroke-width="6"
          color="#fff"
          class="level-circle"
        >
          <template #default>
            <span class="level-text">Lv.{{ volunteerLevel }}</span>
          </template>
        </el-progress>
      </div>
      <div class="hero-center">
        <span class="hero-num">{{ animCount }}</span>
        <span class="hero-label">荣誉证书</span>
      </div>
      <div class="hero-right">
        <span class="hero-sub">🏆</span>
      </div>
    </div>

    <!-- ==================== 证书网格 ==================== -->
    <div class="honor-container" v-loading="loading">
      <!-- 骨架屏?-->
      <div v-if="loading && honorList.length === 0" class="honor-grid">
        <div v-for="i in 6" :key="i" class="skeleton-card">
          <el-skeleton animated>
            <template #template>
              <el-skeleton-item variant="image" style="height: 200px" />
              <div style="padding: 14px">
                <el-skeleton-item variant="h3" style="width: 60%" />
                <el-skeleton-item variant="text" style="width: 40%; margin-top: 8px" />
              </div>
            </template>
          </el-skeleton>
        </div>
      </div>

      <!-- 证书卡片 -->
      <TransitionGroup v-else name="card-fade" tag="div" class="honor-grid">
        <div
          v-for="(honor, idx) in honorList"
          :key="honor.id"
          class="honor-card"
          :style="{ animationDelay: `${idx * 0.06}s` }"
          @click="openPreview(honor)"
        >
          <div class="certificate-wrapper">
            <div class="certificate-streamer"></div>
            <div class="paper-texture"></div>
            <el-image
              :src="honor.image"
              fit="contain"
              class="certificate-image"
            >
              <template #placeholder>
                <div class="image-placeholder">
                  <el-icon :size="36"><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="cover-gradient"></div>
            <div class="certificate-overlay">
              <el-icon><ZoomIn /></el-icon>
              点击查看
            </div>
          </div>
          <div class="honor-info">
            <h4 class="honor-name">{{ honor.name }}</h4>
            <p class="honor-date">
              <el-icon><Calendar /></el-icon>
              {{ honor.date }}
            </p>
            <button class="view-btn" @click.stop="openPreview(honor)">查看证书</button>
          </div>
        </div>
      </TransitionGroup>

      <el-empty v-if="!loading && honorList.length === 0" description="暂无荣誉证书，继续努力吧~" :image-size="120" />
    </div>

    <!-- ==================== 全屏证书预览 ==================== -->
    <el-dialog
      v-model="previewVisible"
      :title="currentHonor?.name || '证书预览'"
      fullscreen
      class="preview-dialog"
      destroy-on-close
    >
      <div class="preview-content" v-if="currentHonor">
        <el-image
          :src="currentHonor.image"
          fit="contain"
          class="preview-image">
  <template #error>
    <img :src="'/default-cover.jpg'" style="width:100%;height:100%;object-fit:cover"/>
  </template>
</el-image>
        <div class="preview-info">
          <h2>{{ currentHonor.name }}</h2>
          <p><el-icon><Calendar /></el-icon> {{ currentHonor.date }}</p>
          <p class="preview-desc">恭喜您获得此荣誉证书！这是对您志愿服务贡献的认可和肯定</p>
        </div>
      </div>
      <template #footer>
        <el-button round @click="previewVisible = false">关闭</el-button>
        <el-button type="success" round @click="handleDownload">
          <el-icon><Download /></el-icon> 下载证书
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Picture, ZoomIn, Calendar, Download } from '@element-plus/icons-vue'
import { request } from '@/utils/request'

interface Honor {
  id: number
  name: string
  image: string
  date: string
}

const loading = ref(false)
const honorList = ref<Honor[]>([])
const previewVisible = ref(false)
const currentHonor = ref<Honor | null>(null)
const animCount = ref(0)

const volunteerLevel = computed(() => {
  const count = honorList.value.length
  if (count >= 10) return 5
  if (count >= 6) return 4
  if (count >= 3) return 3
  if (count >= 1) return 2
  return 1
})

const levelPercent = computed(() => Math.min(volunteerLevel.value * 20, 100))

const openPreview = (honor: Honor) => {
  currentHonor.value = honor
  previewVisible.value = true
}

const handleDownload = () => {
  if (!currentHonor.value) return
  // 模拟下载
  const link = document.createElement('a')
  link.href = currentHonor.value.image
  link.download = `${currentHonor.value.name}.png`
  link.click()
  ElMessage.success('证书下载中...')
}

const animateNumber = (target: number, setter: (v: number) => void, duration = 600) => {
  const start = performance.now()
  const step = (now: number) => {
    const p = Math.min((now - start) / duration, 1)
    setter(Math.round((1 - Math.pow(1 - p, 3)) * target))
    if (p < 1) requestAnimationFrame(step)
  }
  requestAnimationFrame(step)
}

function generateCertificate(title: string, subtitle: string): string {
  const colors = ['#667eea', '#e6a23c', '#67c23a', '#f56c6c', '#409eff']
  const color = colors[Math.floor(Math.random() * colors.length)]
  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" width="400" height="280" viewBox="0 0 400 280">
      <defs>
        <linearGradient id="bg" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" style="stop-color:#fefefe"/>
          <stop offset="100%" style="stop-color:#f5f5f5"/>
        </linearGradient>
      </defs>
      <rect width="400" height="280" fill="url(#bg)" rx="8"/>
      <rect x="10" y="10" width="380" height="260" fill="none" stroke="${color}" stroke-width="3" rx="4"/>
      <rect x="18" y="18" width="364" height="244" fill="none" stroke="${color}" stroke-width="1" stroke-dasharray="4,2" rx="2"/>
      <text x="200" y="60" text-anchor="middle" font-size="16" fill="#999">荣誉证书</text>
      <text x="200" y="120" text-anchor="middle" font-size="28" font-weight="bold" fill="${color}">${title}</text>
      <text x="200" y="160" text-anchor="middle" font-size="16" fill="#666">${subtitle}</text>
      <text x="200" y="200" text-anchor="middle" font-size="14" fill="#999">特发此证 以资鼓励</text>
      <circle cx="200" cy="240" r="20" fill="none" stroke="${color}" stroke-width="2"/>
      <text x="200" y="245" text-anchor="middle" font-size="12" fill="${color}">优质志愿者</text>
    </svg>
  `
  return 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svg)
}

const fetchHonors = async () => {
  loading.value = true
  try {
    const res = await request.get('/volunteer/stats')
    if (res.code === 200) {
      honorList.value = (res.data.honors || []).map((h: any) => ({
        ...h,
        image: h.image.length < 50 ? generateCertificate(h.name, h.image) : h.image
      }))
      animateNumber(honorList.value.length, v => { animCount.value = v })
    }
  } catch (error) {
    console.error('获取荣誉失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(fetchHonors)
</script>

<style lang="scss" scoped>
@keyframes cardIn {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes pulse {
  0%, 100% { box-shadow: 0 2px 8px rgba(0, 201, 167, 0.2); }
  50% { box-shadow: 0 2px 16px rgba(0, 201, 167, 0.5); }
}

@keyframes streamerFlow {
  0% { transform: translateX(-100%) translateY(-100%) rotate(45deg); }
  100% { transform: translateX(200%) translateY(200%) rotate(45deg); }
}

.my-honor-page {
  min-height: 100vh;
  background: #f5f7fa;
}

// ================================================================
// Hero
// ================================================================
.honor-hero {
  background: linear-gradient(135deg, #00C9A7 0%, #05D5B3 100%);
  padding: 32px 20px 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    top: -20%; left: -10%; width: 50%; height: 100%;
    background: linear-gradient(135deg, rgba(255,255,255,0.3) 0%, transparent 70%);
    transform: skewX(-20deg);
    pointer-events: none;
  }
}

.hero-left {
  .level-circle {
    :deep(.el-progress__text) { font-size: 0 !important; }
    :deep(.el-progress-circle__track) { stroke: rgba(255, 255, 255, 0.2); }
  }

  .level-text {
    font-size: 16px;
    font-weight: 800;
    color: #fff;
  }
}

.hero-center {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;

  .hero-num {
    font-size: 40px;
    font-weight: 900;
    color: #fff;
    text-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
    line-height: 1;
  }

  .hero-label {
    font-size: 13px;
    color: rgba(255, 255, 255, 0.75);
  }
}

.hero-right {
  .hero-sub {
    font-size: 40px;
    opacity: 0.5;
  }
}

// ================================================================
// 证书网格
// ================================================================
.honor-container {
  padding: 16px;
  min-height: 300px;
}

.honor-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.skeleton-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}

.honor-card {
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  position: relative;
  transition: all 0.3s;
  animation: cardIn 0.35s ease-out both;

  &:hover {
    transform: translateY(-8px);
    box-shadow: 0 16px 40px rgba(0, 0, 0, 0.12);

    .certificate-overlay { opacity: 1; }
    .certificate-image { transform: scale(1.05); }
    .view-btn { animation: pulse 1.5s infinite; }
    
    .certificate-streamer { opacity: 1; }
  }

  .certificate-wrapper {
    position: relative;
    padding: 16px;
    background: linear-gradient(135deg, #fffdf5 0%, #f9f3e8 100%);
    overflow: hidden;

    .certificate-streamer {
      position: absolute;
      top: 0; left: 0; width: 100%; height: 100%;
      background: linear-gradient(135deg, transparent 45%, rgba(255, 215, 0, 0.4) 50%, transparent 55%);
      z-index: 5;
      opacity: 0;
      pointer-events: none;
      transition: opacity 0.3s;
      animation: streamerFlow 2.5s infinite linear;
    }

    .paper-texture {
      position: absolute;
      inset: 0;
      background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.65' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)'/%3E%3C/svg%3E");
      opacity: 0.05;
      pointer-events: none;
      mix-blend-mode: multiply;
      z-index: 2;
    }

    .certificate-image {
      width: 100%;
      height: 200px;
      transition: transform 0.4s;
      z-index: 1;
    }

    .cover-gradient {
      position: absolute;
      inset: 0;
      background: linear-gradient(to bottom, transparent 60%, rgba(250, 189, 22, 0.25) 100%);
      pointer-events: none;
      z-index: 3;
    }

    .image-placeholder {
      width: 100%;
      height: 200px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f5f5f5;
      color: #ccc;
    }

    .certificate-overlay {
      position: absolute;
      inset: 16px;
      background: rgba(0, 0, 0, 0.4);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 14px;
      gap: 8px;
      opacity: 0;
      transition: opacity 0.3s;
      border-radius: 8px;

      .el-icon { font-size: 28px; }
    }
  }

  .honor-info {
    padding: 14px 16px;

    .honor-name {
      margin: 0 0 6px;
      font-size: 15px;
      font-weight: 600;
      color: #333;
    }

    .honor-date {
      margin: 0 0 12px;
      font-size: 12px;
      color: #999;
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }
}

.view-btn {
  width: 100%;
  border: none;
  background: linear-gradient(135deg, #00C9A7, #05D5B3);
  color: #fff;
  padding: 8px 0;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s;

  &:hover {
    transform: scale(1.03);
    box-shadow: 0 4px 12px rgba(0, 201, 167, 0.35);
  }

  &:active { transform: scale(0.97); }
}

// ================================================================
// 全屏预览
// ================================================================
.preview-dialog {
  :deep(.el-dialog__body) {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 50vh;
  }
}

.preview-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
  max-width: 600px;
  width: 100%;

  .preview-image {
    width: 100%;
    max-height: 50vh;
    border-radius: 12px;
    border: 2px solid #f0f0f0;
  }

  .preview-info {
    text-align: center;

    h2 {
      margin: 0 0 8px;
      font-size: 22px;
      font-weight: 700;
      color: #333;
    }

    p {
      font-size: 14px;
      color: #888;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 4px;
      margin: 4px 0;
    }

    .preview-desc {
      margin-top: 12px;
      font-size: 14px;
      color: #666;
      line-height: 1.6;
    }
  }
}

// ================================================================
// TransitionGroup
// ================================================================
.card-fade-enter-active { transition: all 0.3s ease-out; }
.card-fade-leave-active { transition: all 0.2s ease-in; }
.card-fade-enter-from { opacity: 0; transform: translateY(12px); }
.card-fade-leave-to { opacity: 0; transform: scale(0.96); }

// ================================================================
// 手机适配
// ================================================================
@media only screen and (max-width: 768px) {
  .honor-container { padding: 12px; }

  .honor-grid {
    grid-template-columns: 1fr;
    gap: 14px;
  }

  .honor-card .certificate-wrapper {
    padding: 10px;
    .certificate-image, .image-placeholder { height: 160px; }
  }

  .honor-hero { padding: 20px 16px 24px; }
  .hero-center .hero-num { font-size: 32px; }
}

// ================================================================
// PC 适配
// ================================================================
@media (min-width: 769px) {
  .honor-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px 24px;
  }

  .honor-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
