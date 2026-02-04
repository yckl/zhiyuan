<template>
  <div class="notice-list-page">
    <div class="page-header">
      <h2>📢 通知公告</h2>
      <p class="subtitle">查看最新的系统公告和通知</p>
    </div>

    <!-- 公告时间轴 -->
    <div class="notice-container" v-loading="loading">
      <el-timeline>
        <el-timeline-item
          v-for="notice in noticeList"
          :key="notice.id"
          :type="notice.isTop ? 'primary' : 'info'"
          :hollow="!notice.isTop"
          :timestamp="formatDate(notice.createTime)"
          placement="top"
        >
          <div class="notice-card" :class="{ 'is-top': notice.isTop }" @click="goToDetail(notice.id)">
            <div class="notice-header">
              <el-tag v-if="notice.isTop" type="danger" size="small" effect="dark">
                置顶
              </el-tag>
              <el-tag :type="getTypeTag(notice.type)" size="small">
                {{ getTypeText(notice.type) }}
              </el-tag>
            </div>
            <h3 class="notice-title">{{ notice.title }}</h3>
            <p class="notice-summary">{{ notice.summary || notice.content?.substring(0, 100) || '暂无摘要' }}</p>
            <div class="notice-footer">
              <span class="department">
                <el-icon><OfficeBuilding /></el-icon>
                {{ notice.department || '志愿者管理中心' }}
              </span>
              <span class="read-more">
                查看详情
                <el-icon><ArrowRight /></el-icon>
              </span>
            </div>
          </div>
        </el-timeline-item>
      </el-timeline>

      <el-empty v-if="!loading && noticeList.length === 0" description="暂无公告" />
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="fetchNotices"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { OfficeBuilding, ArrowRight } from '@element-plus/icons-vue'
import { request } from '@/utils/request'

interface Notice {
  id: number
  title: string
  content?: string
  summary?: string
  type?: string
  department?: string
  isTop?: boolean
  createTime: string
}

const router = useRouter()
const loading = ref(false)
const noticeList = ref<Notice[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
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

const fetchNotices = async () => {
  loading.value = true
  try {
    const res = await request.get('/notice/list', {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })
    if (res.code === 200) {
      const data = res.data?.records || res.data || []
      // 置顶排序
      noticeList.value = data.sort((a: Notice, b: Notice) => {
        if (a.isTop && !b.isTop) return -1
        if (!a.isTop && b.isTop) return 1
        return 0
      })
      total.value = res.data?.total || data.length
    }
  } catch (error) {
    console.error('获取公告列表失败:', error)
    // 使用模拟数据
    noticeList.value = generateMockData()
  } finally {
    loading.value = false
  }
}

const generateMockData = (): Notice[] => {
  return [
    {
      id: 1,
      title: '关于2026年寒假志愿服务活动报名的通知',
      summary: '各位志愿者同学：为丰富同学们的寒假生活，培养社会责任感，现开放2026年寒假志愿服务活动报名...',
      type: 'ACTIVITY',
      department: '校团委志愿者工作部',
      isTop: true,
      createTime: '2026-01-18'
    },
    {
      id: 2,
      title: '志愿者管理系统维护公告',
      summary: '为提升系统性能和用户体验，系统将于本周六凌晨2:00-6:00进行升级维护，届时将暂停服务...',
      type: 'SYSTEM',
      department: '信息中心',
      isTop: true,
      createTime: '2026-01-17'
    },
    {
      id: 3,
      title: '新增积分商城功能上线通知',
      summary: '亲爱的志愿者们，积分商城功能已正式上线！您可以使用志愿服务获得的积分兑换精美礼品...',
      type: 'NEWS',
      department: '志愿者管理中心',
      isTop: false,
      createTime: '2026-01-15'
    },
    {
      id: 4,
      title: '关于开展志愿者培训的通知',
      summary: '为提高志愿者服务技能和综合素质，现开展在线培训课程，请各位志愿者积极参加学习...',
      type: 'NOTICE',
      department: '培训部',
      isTop: false,
      createTime: '2026-01-10'
    },
    {
      id: 5,
      title: '2025年度优秀志愿者表彰名单公示',
      summary: '根据年度志愿服务评选工作安排，经个人申报、单位推荐、评审委员会评审，现将拟表彰名单公示...',
      type: 'NOTICE',
      department: '志愿者管理中心',
      isTop: false,
      createTime: '2026-01-05'
    }
  ]
}

const goToDetail = (id: number) => {
  router.push(`/notice/detail/${id}`)
}

onMounted(fetchNotices)
</script>

<style lang="scss" scoped>
.notice-list-page {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 32px;

  h2 {
    margin: 0 0 8px;
    font-size: 24px;
  }

  .subtitle {
    margin: 0;
    color: #999;
  }
}

.notice-container {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.notice-card {
  background: #f9f9f9;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
  border-left: 4px solid transparent;

  &:hover {
    background: #f0f0f0;
    transform: translateX(4px);
  }

  &.is-top {
    background: linear-gradient(135deg, #ecf5ff 0%, #f0f9eb 100%);
    border-left-color: #409eff;
  }

  .notice-header {
    display: flex;
    gap: 8px;
    margin-bottom: 12px;
  }

  .notice-title {
    margin: 0 0 12px;
    font-size: 18px;
    font-weight: 600;
    color: #333;
    line-height: 1.4;
  }

  .notice-summary {
    margin: 0 0 16px;
    font-size: 14px;
    color: #666;
    line-height: 1.6;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .notice-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 13px;

    .department {
      display: flex;
      align-items: center;
      gap: 4px;
      color: #999;
    }

    .read-more {
      display: flex;
      align-items: center;
      gap: 4px;
      color: #409eff;
      font-weight: 500;

      &:hover {
        text-decoration: underline;
      }
    }
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
