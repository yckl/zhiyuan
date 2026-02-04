<template>
  <div class="my-honor-page">
    <div class="page-header">
      <h2>🏆 荣誉证书</h2>
      <p class="subtitle">您获得的志愿服务荣誉</p>
    </div>

    <!-- 荣誉墙 -->
    <div class="honor-grid" v-loading="loading">
      <div
        v-for="honor in honorList"
        :key="honor.id"
        class="honor-card"
      >
        <div class="certificate-wrapper">
          <el-image
            :src="honor.image"
            :preview-src-list="[honor.image]"
            fit="contain"
            class="certificate-image"
          >
            <template #placeholder>
              <div class="image-placeholder">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-image>
          <div class="certificate-overlay">
            <el-icon><ZoomIn /></el-icon>
            点击查看大图
          </div>
        </div>
        <div class="honor-info">
          <h4 class="honor-name">{{ honor.name }}</h4>
          <p class="honor-date">
            <el-icon><Calendar /></el-icon>
            {{ honor.date }}
          </p>
        </div>
      </div>

      <el-empty v-if="!loading && honorList.length === 0" description="暂无荣誉证书" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Picture, ZoomIn, Calendar } from '@element-plus/icons-vue'

interface Honor {
  id: number
  name: string
  image: string
  date: string
}

const loading = ref(false)
const honorList = ref<Honor[]>([])
// 由后端逻辑计算并在此显示

// 使用 SVG 生成模拟证书图片
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
      <text x="200" y="245" text-anchor="middle" font-size="12" fill="${color}">印</text>
    </svg>
  `
  
  return 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svg)
}

import { request } from '@/utils/request'

const fetchHonors = async () => {
  loading.value = true
  try {
    const res = await request.get('/volunteer/stats')
    if (res.code === 200) {
      honorList.value = (res.data.honors || []).map((h: any) => ({
        ...h,
        // 如果后端传的是副标题，则在这里生成预览图
        image: h.image.length < 50 ? generateCertificate(h.name, h.image) : h.image
      }))
    }
  } catch (error) {
    console.error('获取荣誉失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchHonors()
})
</script>

<style lang="scss" scoped>
.my-honor-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;

  h2 {
    margin: 0 0 8px;
    font-size: 24px;
  }

  .subtitle {
    margin: 0;
    color: #999;
  }
}

.honor-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.honor-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);

    .certificate-overlay {
      opacity: 1;
    }
  }

  .certificate-wrapper {
    position: relative;
    padding: 16px;
    background: linear-gradient(135deg, #f8f8f8 0%, #f0f0f0 100%);

    .certificate-image {
      width: 100%;
      height: 200px;
      cursor: pointer;
    }

    .image-placeholder {
      width: 100%;
      height: 200px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f5f5f5;
      color: #ccc;
      font-size: 48px;
    }

    .certificate-overlay {
      position: absolute;
      top: 16px;
      left: 16px;
      right: 16px;
      bottom: 16px;
      background: rgba(0, 0, 0, 0.5);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 14px;
      gap: 8px;
      opacity: 0;
      transition: opacity 0.3s;
      cursor: pointer;

      .el-icon {
        font-size: 32px;
      }
    }
  }

  .honor-info {
    padding: 16px;

    .honor-name {
      margin: 0 0 8px;
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }

    .honor-date {
      margin: 0;
      font-size: 13px;
      color: #999;
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }
}
</style>
