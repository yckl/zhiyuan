<template>
  <div class="notice-detail-page">
    <!-- 返回按钮 -->
    <div class="back-bar">
      <el-button type="primary" text @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回公告列表
      </el-button>
    </div>

    <!-- 公告内容卡片 -->
    <div class="notice-card" v-loading="loading">
      <!-- 标题区 -->
      <div class="notice-header">
        <div class="tags" v-if="notice.isTop || notice.type">
          <el-tag v-if="notice.isTop" type="danger" size="small" effect="dark">
            置顶
          </el-tag>
          <el-tag :type="getTypeTag(notice.type)" size="small">
            {{ getTypeText(notice.type) }}
          </el-tag>
        </div>
        <h1 class="notice-title">{{ notice.title }}</h1>
        <div class="notice-meta">
          <span class="meta-item">
            <el-icon><Calendar /></el-icon>
            发布时间：{{ formatDate(notice.createTime) }}
          </span>
          <span class="meta-item">
            <el-icon><OfficeBuilding /></el-icon>
            发布部门：{{ notice.department || '志愿者管理中心' }}
          </span>
          <span class="meta-item">
            <el-icon><View /></el-icon>
            阅读量：{{ notice.viewCount || 0 }}
          </span>
        </div>
      </div>

      <el-divider />

      <!-- 正文区 -->
      <div class="notice-content" v-html="notice.content || defaultContent"></div>

      <!-- 底部操作 -->
      <div class="notice-footer">
        <div class="footer-left">
          <span class="update-time">
            最后更新：{{ formatDate(notice.updateTime || notice.createTime) }}
          </span>
        </div>
        <div class="footer-right">
          <el-button type="primary" plain @click="handleCollect">
            <el-icon><Star /></el-icon>
            {{ isCollected ? '已收藏' : '收藏公告' }}
          </el-button>
          <el-button @click="handleShare">
            <el-icon><Share /></el-icon>
            分享
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Calendar, OfficeBuilding, View, Star, Share } from '@element-plus/icons-vue'
import { request } from '@/utils/request'

interface Notice {
  id: number
  title: string
  content?: string
  type?: string
  department?: string
  isTop?: boolean
  viewCount?: number
  createTime: string
  updateTime?: string
}

const route = useRoute()
const router = useRouter()
const noticeId = ref(Number(route.params.id))

const loading = ref(false)
const notice = ref<Notice>({
  id: 0,
  title: '',
  createTime: ''
})
const isCollected = ref(false)

// 默认富文本内容
const defaultContent = `
<p>尊敬的志愿者同学们：</p>

<p>为进一步提升我校志愿服务水平，丰富志愿者的服务经验，现将有关事项通知如下：</p>

<h3>一、活动背景</h3>
<p>志愿服务是培养大学生社会责任感、奉献精神的重要途径。本次活动旨在通过系统化的培训和实践，提升志愿者的专业服务能力。</p>

<h3>二、活动安排</h3>
<ul>
  <li><strong>时间：</strong>2026年1月20日至2026年2月20日</li>
  <li><strong>地点：</strong>各社区服务站点、校内活动中心</li>
  <li><strong>对象：</strong>全体注册志愿者</li>
</ul>

<h3>三、参与方式</h3>
<ol>
  <li>登录志愿者管理系统，完成在线报名</li>
  <li>关注系统通知，了解具体活动安排</li>
  <li>按时参加培训和活动，完成服务任务</li>
</ol>

<h3>四、奖励机制</h3>
<p>参与本次活动的志愿者将获得：</p>
<ul>
  <li>志愿服务时长认定</li>
  <li>积分奖励（可在积分商城兑换礼品）</li>
  <li>优秀志愿者证书（表现突出者）</li>
</ul>

<blockquote style="background: #f0f9eb; padding: 16px; border-left: 4px solid #67c23a; margin: 20px 0;">
  <p style="margin: 0; color: #67c23a;"><strong>温馨提示：</strong>请各位志愿者提前做好时间规划，积极参与活动，共同为社会贡献力量！</p>
</blockquote>

<p style="text-align: right; margin-top: 40px;">
  <strong>志愿者管理中心</strong><br/>
  2026年1月18日
</p>
`

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getTypeTag = (type?: string) => {
  const map: Record<string, string> = {
    'SYSTEM': 'danger',
    'ACTIVITY': 'success',
    'NOTICE': 'info',
    'NEWS': 'warning'
  }
  return map[type || ''] || 'info'
}

const getTypeText = (type?: string) => {
  const map: Record<string, string> = {
    'SYSTEM': '系统公告',
    'ACTIVITY': '活动通知',
    'NOTICE': '普通公告',
    'NEWS': '新闻资讯'
  }
  return map[type || ''] || '通知'
}

const fetchNotice = async () => {
  loading.value = true
  try {
    const res = await request.get(`/notice/${noticeId.value}`)
    if (res.code === 200 && res.data) {
      notice.value = res.data
    }
  } catch (error) {
    console.error('获取公告详情失败:', error)
    // 使用模拟数据
    notice.value = {
      id: noticeId.value,
      title: '关于2026年寒假志愿服务活动报名的通知',
      type: 'ACTIVITY',
      department: '校团委志愿者工作部',
      isTop: true,
      viewCount: 1234,
      createTime: '2026-01-18T10:00:00',
      updateTime: '2026-01-18T14:30:00'
    }
  } finally {
    loading.value = false
  }
}

const checkCollected = async () => {
  try {
    const res = await request.get('/collection/check', {
      targetId: noticeId.value, targetType: 'NOTICE'
    })
    if (res.code === 200) {
      isCollected.value = res.data === true
    }
  } catch (error) {
    console.error('检查收藏状态失败:', error)
  }
}

const handleCollect = async () => {
  try {
    const res = await request.post('/collection/toggle', {
      targetId: noticeId.value,
      targetType: 'NOTICE'
    })
    if (res.code === 200) {
      isCollected.value = !isCollected.value
      ElMessage.success(isCollected.value ? '收藏成功' : '已取消收藏')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleShare = () => {
  const url = window.location.href
  navigator.clipboard?.writeText(url)
  ElMessage.success('链接已复制到剪贴板')
}

const goBack = () => {
  router.push('/notice/list')
}

onMounted(() => {
  fetchNotice()
  checkCollected()
})
</script>

<style lang="scss" scoped>
.notice-detail-page {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

.back-bar {
  margin-bottom: 16px;
}

.notice-card {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.notice-header {
  text-align: center;
  margin-bottom: 24px;

  .tags {
    display: flex;
    justify-content: center;
    gap: 8px;
    margin-bottom: 16px;
  }

  .notice-title {
    font-size: 28px;
    font-weight: bold;
    color: #333;
    margin: 0 0 20px;
    line-height: 1.4;
  }

  .notice-meta {
    display: flex;
    justify-content: center;
    flex-wrap: wrap;
    gap: 24px;

    .meta-item {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 14px;
      color: #999;
    }
  }
}

.notice-content {
  font-size: 16px;
  line-height: 1.8;
  color: #333;

  :deep(h3) {
    font-size: 18px;
    color: #333;
    margin: 24px 0 12px;
    padding-left: 12px;
    border-left: 4px solid #409eff;
  }

  :deep(p) {
    margin: 12px 0;
    text-indent: 2em;
  }

  :deep(ul), :deep(ol) {
    margin: 12px 0;
    padding-left: 2em;

    li {
      margin: 8px 0;
    }
  }

  :deep(blockquote) {
    margin: 20px 0;
    padding: 16px 20px;
    background: #f9f9f9;
    border-left: 4px solid #ddd;
    color: #666;

    p {
      text-indent: 0;
      margin: 0;
    }
  }

  :deep(strong) {
    color: #333;
  }
}

.notice-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid #eee;

  .footer-left {
    .update-time {
      font-size: 13px;
      color: #999;
    }
  }

  .footer-right {
    display: flex;
    gap: 12px;
  }
}
</style>
