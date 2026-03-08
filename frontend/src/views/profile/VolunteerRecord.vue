<template>
  <div class="volunteer-record-page">
    <!-- ==================== 极光青统计头部 ==================== -->
    <div class="record-header aurora-gradient">
      <div class="header-back">
        <el-button link @click="router.back()">
          <el-icon><ArrowLeft /></el-icon> 返回
        </el-button>
      </div>
      <div class="header-content">
        <div class="stat-item anim-up" style="animation-delay: 0.1s">
          <span class="num">{{ totalHours }}</span>
          <span class="label">累计志愿工时</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item anim-up" style="animation-delay: 0.2s">
          <span class="num">{{ activityCount }}</span>
          <span class="label">参与活动数</span>
        </div>
      </div>
    </div>

    <!-- ==================== 记录时间线 ==================== -->
    <div class="record-body">
      <div v-loading="loading" class="timeline-wrapper">
        <el-timeline v-if="finishedList.length > 0">
          <el-timeline-item
            v-for="(item, index) in finishedList"
            :key="item.id"
            :timestamp="formatDate(item.activityEndTime || item.finishTime || item.createTime)"
            placement="top"
            color="#0093E9"
            class="record-item-anim"
            :style="{ animationDelay: `${index * 0.1}s` }"
          >
            <div class="record-card shadow-hover">
              <div class="card-header">
                <h3 class="article-title">{{ item.activityTitle || item.title }}</h3>
                <el-tag type="success" effect="dark" round>
                  +{{ item.actualHours || 0 }} 小时
                </el-tag>
              </div>
              <div class="card-body">
                <div class="info-row">
                  <el-icon><Location /></el-icon>
                  <span>服务地点：{{ item.address || item.location || '校内指定区域' }}</span>
                </div>
                <div class="info-row" v-if="item.rating">
                  <el-icon><Star /></el-icon>
                  <span>获得评价：<el-rate v-model="item.rating" disabled text-color="#ff9900" /></span>
                </div>
                
                <div class="card-footer" v-if="item.hasCertificate || item.certificateId">
                  <el-button type="primary" link size="small" @click="viewCert(item)">
                    查看荣誉证书 <el-icon><ArrowRight /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else-if="!loading" description="尚无完成的志愿记录，坚持就是胜利！" :image-size="120" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Location, Star, ArrowRight } from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import dayjs from 'dayjs'

const router = useRouter()
const loading = ref(false)
const finishedList = ref<any[]>([])
const stats = ref({ totalHours: 0, serviceCount: 0 })

const totalHours = computed(() => stats.value.totalHours || 0)
const activityCount = computed(() => stats.value.serviceCount || 0)

const formatDate = (date: string) => date ? dayjs(date).format('YYYY年MM月DD日') : '-'

const fetchRecords = async () => {
  loading.value = true
  try {
    // 获取我的报名记录，并过滤状态为 3 (已完成)
    const res = await request.get('/registration/my', { page: 1, size: 100, status: 3 })
    if (res.code === 200) {
      finishedList.value = res.data?.records || []
      // 按时间倒序
      finishedList.value.sort((a, b) => {
        const t1 = a.activityEndTime || a.finishTime || a.createTime
        const t2 = b.activityEndTime || b.finishTime || b.createTime
        return dayjs(t2).unix() - dayjs(t1).unix()
      })
    }
    
    // 获取统计数据
    const statsRes = await request.get('/volunteer/statistics')
    if (statsRes.code === 200) {
      stats.value = statsRes.data
    }
  } catch (error) {
    console.error('获取记录失败:', error)
  } finally {
    loading.value = false
  }
}

const viewCert = (_item: any) => {
  router.push('/profile/honor')
}

onMounted(() => {
  fetchRecords()
})
</script>

<style lang="scss" scoped>
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

.record-item-anim {
  animation: fadeInUp 0.5s ease-out both;
}

.anim-up {
  animation: fadeInUp 0.6s cubic-bezier(0.2, 0.8, 0.2, 1) both;
}

.volunteer-record-page {
  min-height: 100vh;
  background: #f8fafc;
}

.record-header {
  height: 160px;
  background: linear-gradient(135deg, #00BFA6 0%, #0093E9 100%);
  padding: 20px;
  color: #fff;
  display: flex;
  flex-direction: column;
  position: relative;

  .header-back {
    margin-bottom: 10px;
    :deep(.el-button) { color: #fff; font-size: 14px; }
  }

  .header-content {
    flex: 1;
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 40px;

    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      .num { font-size: 32px; font-weight: 800; line-height: 1.2; }
      .label { font-size: 13px; opacity: 0.8; margin-top: 4px; }
    }

    .stat-divider {
      width: 1px;
      height: 30px;
      background: rgba(255, 255, 255, 0.2);
    }
  }
}

.record-body {
  max-width: 800px;
  margin: -20px auto 0;
  padding: 0 16px 40px;
  position: relative;
  z-index: 10;
}

.timeline-wrapper {
  padding: 0 10px;
}

.record-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;

  &.shadow-hover:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    h3 {
      font-size: 17px;
      font-weight: 700;
      color: #1e293b;
      margin: 0;
      flex: 1;
      margin-right: 12px;
    }
  }

  .card-body {
    .info-row {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 8px;
      font-size: 14px;
      color: #64748b;
      
      .el-icon { color: #94a3b8; }
    }
  }

  .card-footer {
    border-top: 1px solid #f1f5f9;
    margin-top: 16px;
    padding-top: 12px;
    display: flex;
    justify-content: flex-end;
  }
}

/* 适配移动端 */
@media (max-width: 768px) {
  .record-header { height: 140px; }
  .record-body { margin-top: -15px; }
  .record-card { padding: 16px; }
}
</style>
