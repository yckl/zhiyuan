<template>
  <div class="my-experiences">
    <!-- 统一头部 -->
    <div class="list-header">
      <h2>我的心得</h2>
      <el-button type="primary" icon="Plus" circle class="floating-add-btn" @click="router.push('/experience/create')" />
    </div>

    <div v-loading="loading" class="article-list-container">
      <div 
        v-for="item in experiences" 
        :key="item.id" 
        class="article-card anim-section"
        @click="router.push(`/experience/${item.id}`)"
      >
        <div class="card-main">
          <h3 class="article-title">{{ item.title }}</h3>
          <p class="article-excerpt">{{ item.content.substring(0, 100) }}...</p>
          
          <div class="article-meta">
            <span class="meta-tag" v-if="item.status === 1">已发</span>
            <span class="meta-tag draft" v-else-if="item.status === 0">待审</span>
            <span class="meta-tag rejected" v-else>已拒</span>
            
            <span class="meta-item"><el-icon><View /></el-icon> {{ item.viewCount }}</span>
            <span class="meta-item"><el-icon><Star /></el-icon> {{ item.likeCount }}</span>
            <span class="meta-date">{{ item.createTime?.substring(0, 10) }}</span>
          </div>
        </div>

        <div class="exp-cover" v-if="getCover(item.images)">
          <img :src="getCover(item.images)" alt="cover" @error="(e) => ((e.target as HTMLImageElement).src = '/default-cover.jpg')" />
        </div>

        <div class="card-actions" @click.stop>
          <el-button type="danger" link @click="handleDelete(item.id)">
            <el-icon><Delete /></el-icon> 删除
          </el-button>
        </div>
      </div>

      <el-empty v-if="!loading && experiences.length === 0" description="暂无心得记录，快去发布第一篇吧" :image-size="120" />
    </div>

    <div class="pagination-wrapper">
      <el-pagination
        v-if="total > 0"
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.size"
        :total="total"
        :layout="isMobile ? 'prev, pager, next' : 'total, prev, pager, next'"
        class="dc-pagination"
        @current-change="fetchExperiences"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, View, Star, Delete } from '@element-plus/icons-vue'
import { request } from '@/utils/request'

const router = useRouter()

const loading = ref(false)
const experiences = ref<any[]>([])
const total = ref(0)
const queryParams = reactive({ page: 1, size: 10 })

// 解析图片JSON
const getCover = (imagesJson: string) => {
  try {
    const imgs = JSON.parse(imagesJson || '[]')
    return imgs.length > 0 ? imgs[0] : null
  } catch (e) {
    return null
  }
}

const fetchExperiences = async () => {
  loading.value = true
  try {
    const res = await request.get('/experience/my', queryParams)
    experiences.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('获取心得列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定要永久删除这篇心得吗？', '确认删除', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning',
    buttonSize: 'default'
  }).then(async () => {
    try {
      await request.delete(`/experience/${id}`)
      ElMessage.success('删除成功')
      fetchExperiences()
    } catch (error) {
      console.error('删除失败:', error)
    }
  }).catch(() => {})
}

const isMobile = ref(window.innerWidth < 768)
const handleResize = () => { isMobile.value = window.innerWidth < 768 }

onMounted(() => {
  window.addEventListener('resize', handleResize)
  fetchExperiences()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style lang="scss" scoped>
@keyframes fadeUp { from { opacity: 0; transform: translateY(16px); } to { opacity: 1; transform: translateY(0); } }
.anim-section { animation: fadeUp 0.5s cubic-bezier(0.2, 0.8, 0.2, 1) both; }

.my-experiences {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px 16px 80px;
  background: #f8fafc;
  min-height: 100vh;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  
  h2 {
    font-size: 24px;
    font-weight: 800;
    color: #1e293b;
    margin: 0;
  }

  .floating-add-btn {
    width: 48px;
    height: 48px;
    font-size: 20px;
    box-shadow: 0 4px 12px rgba(0, 147, 233, 0.3);
    transition: all 0.3s;
    &:hover { transform: rotate(90deg) scale(1.1); }
  }
}

.article-list-container {
  display: flex;
  flex-direction: column;
  gap: 1px; // Divider effect
  background: #fff;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03);
  border: 1px solid rgba(0,0,0,0.05);
}

.article-card {
  background: #fff;
  padding: 24px;
  display: flex;
  gap: 20px;
  transition: all 0.2s;
  cursor: pointer;
  border-bottom: 1px solid #f1f5f9;
  position: relative;
  
  &:last-child { border-bottom: none; }
  &:hover { 
    background: #f8fafc;
    .article-title { color: #0093E9; }
  }

  .card-main {
    flex: 1;
    display: flex;
    flex-direction: column;
  }

  .article-title {
    font-size: 18px;
    font-weight: 700;
    color: #1e293b;
    margin: 0 0 10px;
    line-height: 1.4;
    transition: color 0.2s;
  }

  .article-excerpt {
    font-size: 14px;
    color: #64748b;
    line-height: 1.6;
    margin: 0 0 16px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .article-meta {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-top: auto;

    .meta-tag {
      font-size: 11px;
      padding: 2px 8px;
      border-radius: 4px;
      background: #f0fdf4;
      color: #16a34a;
      font-weight: 600;
      
      &.draft { background: #f1f5f9; color: #64748b; }
      &.rejected { background: #fef2f2; color: #ef4444; }
    }

    .meta-item {
      display: flex;
      align-items: center;
      gap: 4px;
      color: #94a3b8;
      font-size: 13px;
    }

    .meta-date {
      margin-left: auto;
      font-size: 12px;
      color: #cbd5e1;
    }
  }

  .card-cover {
    width: 140px;
    height: 90px;
    border-radius: 12px;
    overflow: hidden;
    flex-shrink: 0;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s;
    }
  }
  
  &:hover .card-cover img { transform: scale(1.08); }

  .card-actions {
    position: absolute;
    right: 24px;
    top: 24px;
    opacity: 0;
    transition: opacity 0.2s;
  }
  
  &:hover .card-actions { opacity: 1; }
}

.pagination-wrapper {
  margin-top: 32px;
  display: flex;
  justify-content: center;
}

:deep(.dc-pagination) {
  .el-pager li.is-active { background-color: #0093E9 !important; color: #fff !important; }
}

@media (max-width: 768px) {
  .my-experiences { padding: 16px 12px 100px; }
  .article-card {
    padding: 16px;
    flex-direction: column-reverse;
    gap: 12px;
    
    .card-cover { width: 100%; height: 160px; }
    .card-actions { opacity: 1; top: unset; bottom: 16px; right: 16px; }
    .meta-date { display: none; }
  }
}
</style>
