<template>
  <div class="comment-section">
    <div class="comment-header">
      <h3 class="section-title">
        <el-icon><ChatLineRound /></el-icon>
        全部评论 ({{ totalRealComments }})
      </h3>
      <!-- 平均评分摘要 -->
      <div v-if="averageRating > 0" class="rating-summary">
        <el-rate
          :model-value="averageRating"
          disabled
          show-score
          score-template="{value} 分"
          :colors="ratingColors"
        />
        <span class="rating-count">{{ ratingCountText }}</span>
      </div>
    </div>

    <!-- 发表主评论 -->
    <div class="publish-box">
      <el-avatar :size="40" :src="userAvatar">
        <el-icon><User /></el-icon>
      </el-avatar>
      <div class="input-area">
        <el-input
          v-model="newComment"
          type="textarea"
          :rows="3"
          placeholder="说点什么吧..."
          resize="none"
        />
        <div class="actions">
          <div class="rating-picker">
            <span class="rating-label">评分</span>
            <el-rate
              v-model="newRating"
              :colors="ratingColors"
              show-text
              :texts="ratingTexts"
            />
          </div>
          <el-button 
            type="primary" 
            :loading="submitting" 
            :disabled="!newComment.trim()"
            @click="handleSubmit"
          >
            发表评论
          </el-button>
        </div>
      </div>
    </div>

    <!-- 初始加载骨架屏 -->
    <div v-if="loading && comments.length === 0" class="comment-skeletons">
      <div v-for="i in 3" :key="i" class="cmt-skeleton-item">
        <el-skeleton animated>
          <template #template>
            <div style="display: flex; gap: 16px;">
              <el-skeleton-item variant="circle" style="width: 40px; height: 40px" />
              <div style="flex: 1;">
                <el-skeleton-item variant="text" style="width: 30%; height: 20px" />
                <el-skeleton-item variant="text" style="width: 80%; margin-top: 12px" />
                <el-skeleton-item variant="text" style="width: 60%; margin-top: 8px" />
              </div>
            </div>
          </template>
        </el-skeleton>
      </div>
    </div>

    <!-- 评论列表 -->
    <div 
      v-else
      class="comment-list" 
      v-infinite-scroll="loadMore" 
      :infinite-scroll-disabled="loading || !hasMore" 
      :infinite-scroll-distance="100"
      :infinite-scroll-immediate="false"
    >
      <transition-group name="list-fade">
        <div v-for="comment in comments" :key="comment.id" class="comment-item">
        <el-avatar :size="40" :src="comment.userAvatar" class="avatar">
          {{ comment.username?.charAt(0) || '?' }}
        </el-avatar>
        <div class="content">
          <div class="user-info">
            <span class="username">{{ comment.username || '匿名用户' }}</span>
            <el-rate
              v-if="comment.rating"
              :model-value="comment.rating"
              disabled
              :colors="ratingColors"
              class="inline-rating"
            />
            <span class="time">{{ formatDate(comment.createTime) }}</span>
          </div>
          <p class="text">{{ comment.content }}</p>
          <div class="item-actions">
            <span class="action" @click="handleReply(comment)">
              <el-icon><ChatDotRound /></el-icon> 回复
            </span>
            <span class="action" :class="{ liked: comment.isLiked }" @click="handleLike(comment)">
              <el-icon><Star /></el-icon> {{ comment.likeCount || 0 }}
            </span>
            <el-popconfirm
              v-if="canDelete(comment)"
              title="确定要删除这条评论吗?"
              @confirm="handleDelete(comment.id)"
            >
              <template #reference>
                <span class="action delete">删除</span>
              </template>
            </el-popconfirm>
          </div>

          <!-- 子评论列表 -->
          <div v-if="comment.totalReplies > 0" class="reply-container">
            <div 
              class="reply-toggle" 
              @click="toggleReplies(comment)"
              @mouseenter="comment.isHovered = true"
              @mouseleave="comment.isHovered = false"
            >
              <el-icon><CaretBottom v-if="!comment.showReplies" /><CaretTop v-else /></el-icon>
              {{ comment.showReplies ? '隐藏回复' : `${comment.totalReplies} 条回复` }}
            </div>
            
            <el-collapse-transition>
              <div v-show="comment.showReplies && !isMobile" class="reply-list">
                <div class="root-trunk-line" v-if="comment.childrenTree && comment.childrenTree.length > 0"></div>
                <CommentReplyNode 
                  v-for="reply in comment.childrenTree" 
                  :key="reply.id"
                  :reply="reply"
                  :root-user-id="comment.userId"
                  @reply="(r) => handleReply(comment, r)"
                  @like="handleLike"
                  @delete="handleDelete"
                />
              </div>
            </el-collapse-transition>
          </div>
          </div> <!-- end content -->
        </div> <!-- end comment-item -->
      </transition-group>
      
      <!-- 加载提示尾部 -->
      <div class="loading-footer" v-if="comments.length > 0 || loading">
        <template v-if="loading">
          <div class="premium-loader">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>奋力加载中...</span>
          </div>
        </template>
        <template v-else-if="!hasMore">
          <div class="no-more-divider">
            <span class="dot"></span>
            <span class="text">已经到底啦，留下你的精彩评论吧</span>
            <span class="dot"></span>
          </div>
        </template>
        <template v-else>
          <div class="load-more-tip" @click="loadMore">
            点击或上滑加载更多
          </div>
        </template>
      </div>
      
      <el-empty 
        v-if="!loading && comments.length === 0" 
        :image-size="120"
        description="暂无评论，快来抢沙发吧~" 
      />
    </div>

    <!-- 回复对话框 -->
    <el-dialog
      v-model="replyDialog.visible"
      :title="'回复 @' + replyDialog.targetUsername"
      :width="isMobile ? '90%' : '500px'"
      append-to-body
    >
      <el-input
        v-model="replyDialog.content"
        type="textarea"
        :rows="4"
        :placeholder="'回复 @' + replyDialog.targetUsername + '...'"
        resize="none"
      />
      <div class="reply-rating-row">
        <span class="rating-label">评分</span>
        <el-rate
          v-model="replyDialog.rating"
          :colors="ratingColors"
          show-text
          :texts="ratingTexts"
        />
      </div>
      <template #footer>
        <el-button @click="replyDialog.visible = false">取消</el-button>
        <el-button 
          type="primary" 
          :loading="replyDialog.submitting" 
          :disabled="!replyDialog.content.trim()"
          @click="submitReply"
        >
          确定回复
        </el-button>
      </template>
    </el-dialog>

    <!-- 移动端回复抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      direction="btt"
      size="85%"
      :with-header="false"
      append-to-body
      class="mobile-reply-drawer"
    >
      <div class="drawer-header">
        <h3>回复详情</h3>
        <el-icon class="close-btn" @click="drawerVisible = false"><Close /></el-icon>
      </div>
      <div class="drawer-body" v-if="activeComment">
        <!-- 渲染主评论摘要 -->
        <div class="main-comment-summary">
          <el-avatar :size="40" :src="activeComment.userAvatar" class="avatar">
            {{ activeComment.username?.charAt(0) || '?' }}
          </el-avatar>
          <div class="content">
            <div class="user-info">
              <span class="username">{{ activeComment.username || '匿名用户' }}</span>
              <span class="time">{{ formatDate(activeComment.createTime) }}</span>
            </div>
            <p class="text">{{ activeComment.content }}</p>
            <div class="item-actions">
              <span class="action" @click="handleReply(activeComment)">
                <el-icon><ChatDotRound /></el-icon> 回复
              </span>
            </div>
          </div>
        </div>
        <div class="drawer-divider"></div>
        <!-- 渲染回复列表 -->
        <div class="drawer-reply-list">
          <div class="root-trunk-line" v-if="activeComment.childrenTree && activeComment.childrenTree.length > 0"></div>
          <CommentReplyNode 
            v-for="reply in activeComment.childrenTree" 
            :key="reply.id"
            :reply="reply"
            :root-user-id="activeComment.userId"
            @reply="(r) => handleReply(activeComment, r)"
            @like="handleLike"
            @delete="(id) => { handleDelete(id); if (activeComment.totalReplies === 1) drawerVisible = false; }"
          />
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ChatLineRound, User, ChatDotRound, Star, CaretBottom, CaretTop, Close } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { request } from '@/utils/request'
import dayjs from 'dayjs'
import CommentReplyNode from './CommentReplyNode.vue'

const router = useRouter()
const route = useRoute()

const props = defineProps({
  targetType: {
    type: String,
    required: true
  },
  targetId: {
    type: Number,
    required: true
  },
  count: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['update:count', 'refresh'])

const totalRealComments = ref(0)
const loading = ref(false)
const submitting = ref(false)
const comments = ref<any[]>([])
const newComment = ref('')
const newRating = ref(0)
const averageRating = ref(0)
const ratedCount = ref(0)

const isMobile = ref(false)
const drawerVisible = ref(false)
const activeComment = ref<any>(null)

const checkMobile = () => {
  isMobile.value = window.innerWidth < 768
}

const toggleReplies = (comment: any) => {
  if (isMobile.value) {
    activeComment.value = comment
    drawerVisible.value = true
  } else {
    comment.showReplies = !comment.showReplies
  }
}

// ----------------- 分页相关状态 -----------------
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const hasMore = computed(() => {
  // 如果当前已加载的根评论数 < total，则还有下一页
  // 注意：comments 里只存了根评论，子评论嵌套在 children 里，而后端的 total 返回的仅是根评论总数
  return comments.value.length < total.value
})

const ratingColors = ['#F7BA2A', '#F7BA2A', '#FF9900']
const ratingTexts = ['极差', '一般', '不错', '很好', '非常好']

const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const userAvatar = computed(() => userInfo.avatar)

const ratingCountText = computed(() => {
  if (ratedCount.value === 0) return ''
  return `(${ratedCount.value} 人评价)`
})

const replyDialog = reactive({
  visible: false,
  submitting: false,
  content: '',
  parentId: 0,
  targetUserId: 0,
  targetUsername: '',
  replyToId: 0,
  rating: 0
})

const buildReplyTree = (flatReplies: any[], rootId: number) => {
  const map: Record<number, any> = {}
  const roots: any[] = []
  
  // Shallow copy items and initialize children array
  flatReplies.forEach((r: any) => {
    map[r.id] = { ...r, children: [] }
  })
  
  flatReplies.forEach((r: any) => {
    if (r.replyToId && r.replyToId !== rootId && map[r.replyToId]) {
      map[r.replyToId].children.push(map[r.id])
    } else {
      roots.push(map[r.id])
    }
  })
  return roots
}

const fetchComments = async (isLoadMore = false) => {
  if (loading.value) return
  loading.value = true
  
  if (!isLoadMore) {
    currentPage.value = 1
  }

  try {
    const res = await request.get('/comment/list', {
      targetType: props.targetType,
      targetId: props.targetId,
      page: currentPage.value,
      size: pageSize.value
    })
    
    const expandedIds = comments.value.filter(c => c.showReplies).map(c => c.id)
    if (replyDialog.parentId && !expandedIds.includes(replyDialog.parentId)) {
      expandedIds.push(replyDialog.parentId)
    }
    
    // 我们使用 Page 对象，返回结构里包含 records 和 total
    const pageData = res.data || {}
    const newRecords = pageData.records || []
    
    newRecords.forEach((c: any) => {
      if (expandedIds.includes(c.id)) {
        c.showReplies = true
      }
      
      if (c.children && c.children.length > 0) {
        c.totalReplies = c.children.length
        c.childrenTree = buildReplyTree(c.children, c.id)
      } else {
        c.totalReplies = 0
        c.childrenTree = []
      }
    })
    
    if (isLoadMore) {
      comments.value.push(...newRecords)
    } else {
      comments.value = newRecords
    }

    if (activeComment.value) {
      const refreshed = comments.value.find(c => c.id === activeComment.value.id)
      if (refreshed) {
        activeComment.value = refreshed
      }
    }
    
    total.value = pageData.total || 0

    // 拉取绝对真实的深层总评论数
    const countRes = await request.get('/comment/count', {
      targetType: props.targetType,
      targetId: props.targetId
    })
    totalRealComments.value = countRes.data || 0

    // 计算有评分的评论数（粗略计算当前已加载的）
    const rated = comments.value.filter((c: any) => c.rating && c.rating > 0)
    ratedCount.value = rated.length
  } catch (error) {
    console.error('获取评论列表失败:', error)
  } finally {
    loading.value = false
  }
}

const loadMore = () => {
  if (loading.value || !hasMore.value) return
  currentPage.value += 1
  fetchComments(true)
}

const fetchAverageRating = async () => {
  try {
    const res = await request.get('/comment/rating', {
      targetType: props.targetType,
      targetId: props.targetId
    })
    averageRating.value = res.data || 0
  } catch (error) {
    console.error('获取平均评分失败:', error)
  }
}

const handleSubmit = async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请登录后再发表评论')
    router.push('/login')
    return
  }
  if (!newComment.value.trim()) return
  submitting.value = true
  try {
    const payload: any = {
      targetType: props.targetType,
      targetId: props.targetId,
      content: newComment.value,
      parentId: 0
    }
    if (newRating.value > 0) {
      payload.rating = newRating.value
    }
    const res = await request.post('/comment', payload)
    if (res.code === 200) {
      ElMessage.success('发布完成')
      newComment.value = ''
      newRating.value = 0
      fetchComments()
      fetchAverageRating()
      emit('refresh')
    } else {
      ElMessage.warning(res.msg || '发表评论失败')
    }
  } catch (error: any) {
    const msg = error?.response?.data?.msg || error?.message || '发表评论失败'
    ElMessage.warning(msg)
  } finally {
    submitting.value = false
  }
}

const handleReply = (parent: any, replyTo?: any) => {
  if (!localStorage.getItem('token')) {
    ElMessage.warning('请登录后再回复')
    router.push('/login')
    return
  }
  replyDialog.parentId = parent.id
  if (replyTo) {
    replyDialog.targetUserId = replyTo.userId
    replyDialog.targetUsername = replyTo.username
    replyDialog.replyToId = replyTo.id
  } else {
    replyDialog.targetUserId = parent.userId
    replyDialog.targetUsername = parent.username
    replyDialog.replyToId = 0
  }
  replyDialog.content = ''
  replyDialog.rating = 0
  replyDialog.visible = true
}

const submitReply = async () => {
  if (!replyDialog.content.trim()) return
  replyDialog.submitting = true
  try {
    const payload: any = {
      targetType: props.targetType,
      targetId: props.targetId,
      content: replyDialog.content,
      parentId: replyDialog.parentId,
      replyToUserId: replyDialog.targetUserId,
      replyToId: replyDialog.replyToId > 0 ? replyDialog.replyToId : null
    }
    if (replyDialog.rating > 0) {
      payload.rating = replyDialog.rating
    }
    const res = await request.post('/comment', payload)
    if (res.code === 200) {
      ElMessage.success('回复成功')
      replyDialog.visible = false
      fetchComments()
      fetchAverageRating()
      emit('refresh')
    } else {
      ElMessage.warning(res.msg || '回复失败')
    }
  } catch (error: any) {
    const msg = error?.response?.data?.msg || error?.message || '回复失败'
    ElMessage.warning(msg)
  } finally {
    replyDialog.submitting = false
  }
}

const handleLike = async (comment: any) => {
  if (!localStorage.getItem('token')) {
    ElMessage.warning('请登录后再点赞')
    router.push('/login')
    return
  }
  try {
    const res = await request.post(`/comment/like/${comment.id}`)
    if (res.code === 200) {
      // 切换点赞状态
      const wasLiked = comment.isLiked
      comment.isLiked = !wasLiked
      comment.likeCount = wasLiked 
        ? Math.max(0, (comment.likeCount || 1) - 1) 
        : (comment.likeCount || 0) + 1
    }
  } catch (error) {
    console.error('点赞失败:', error)
  }
}

const handleDelete = async (id: number) => {
  try {
    await request.delete(`/comment/${id}`)
    ElMessage.success('删除成功')
    fetchComments()
    fetchAverageRating()
    emit('refresh')
  } catch (error) {
    console.error('删除失败:', error)
  }
}

const canDelete = (comment: any) => {
  // 兼容多种用户ID字段
  const currentUserId = userInfo.id || userInfo.userId || userInfo.volunteerId
  const commentUserId = comment.userId || comment.volunteerId || comment.authorId
  return currentUserId && commentUserId && currentUserId === commentUserId
}

const formatDate = (date: string) => {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  fetchComments()
  fetchAverageRating()
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<style lang="scss" scoped>
// ================================================================
// 加载提示尾部样式 (Premium)
// ================================================================
.loading-footer {
  padding: 30px 0;
  color: #909399;
  font-size: 14px;
  display: flex;
  justify-content: center;

  .premium-loader {
    display: flex;
    align-items: center;
    gap: 10px;
    background: rgba(64, 158, 255, 0.05);
    padding: 10px 24px;
    border-radius: 30px;
    color: #409eff;
    font-weight: 500;
    
    .is-loading {
      animation: rotating 2s linear infinite;
    }
  }

  .no-more-divider {
    display: flex;
    align-items: center;
    gap: 15px;
    opacity: 0.6;

    .dot {
      width: 4px;
      height: 4px;
      background: #dcdfe6;
      border-radius: 50%;
    }

    .text {
      font-size: 12px;
      letter-spacing: 1px;
    }
  }

  .load-more-tip {
    cursor: pointer;
    padding: 8px 20px;
    border-radius: 20px;
    border: 1px solid #dcdfe6;
    transition: all 0.3s;
    
    &:hover {
      color: #409eff;
      border-color: #409eff;
      background: rgba(64, 158, 255, 0.02);
    }
  }
}

.comment-skeletons {
  .cmt-skeleton-item {
    padding: 24px 0;
    border-bottom: 1px solid #f0f0f0;
    &:last-child { border-bottom: none; }
  }
}

// 列表动画
.list-fade-enter-active,
.list-fade-leave-active {
  transition: all 0.4s ease;
}
.list-fade-enter-from {
  opacity: 0;
  transform: translateY(20px);
}
.list-fade-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

html.dark .loading-footer {
   .premium-loader {
     background: rgba(64, 158, 255, 0.1);
   }
   .no-more-divider .dot {
     background: #4c4d4f;
   }
   .load-more-tip {
     border-color: #4c4d4f;
   }
}

.comment-section {
  margin-top: 30px;
  background: #fff;
  padding: 24px;
  border-radius: 12px;
  
  @media (max-width: 767px) {
    padding: 16px 14px;
    margin-top: 16px;
  }

  .comment-header {
    margin-bottom: 24px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 12px;

    .section-title {
      font-size: 18px;
      font-weight: 600;
      color: #333;
      display: flex;
      align-items: center;
      gap: 8px;
      margin: 0;

      .el-icon {
        color: #409eff;
      }
    }

    .rating-summary {
      display: flex;
      align-items: center;
      gap: 8px;
      background: linear-gradient(135deg, #fff9e6, #fff3cc);
      padding: 6px 16px;
      border-radius: 20px;
      border: 1px solid #ffe58f;

      .rating-count {
        font-size: 12px;
        color: #999;
      }
    }
  }

  .publish-box {
    display: flex;
    gap: 16px;
    margin-bottom: 30px;

    .input-area {
      flex: 1;

      .actions {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-top: 12px;

        .rating-picker {
          display: flex;
          align-items: center;
          gap: 8px;

          .rating-label {
            font-size: 13px;
            color: #666;
            white-space: nowrap;
          }
        }
      }
    }
  }

  .comment-list {
    .comment-item {
      display: flex;
      gap: 16px;
      padding-bottom: 24px;
      margin-bottom: 24px;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
        margin-bottom: 0;
      }

      .content {
        flex: 1;

        .user-info {
          display: flex;
          align-items: center;
          gap: 12px;
          margin-bottom: 8px;
          flex-wrap: wrap;

          .username {
            font-weight: 500;
            color: #333;
            font-size: 14px;
          }

          .time {
            font-size: 12px;
            color: #999;
          }

          .inline-rating {
            height: 20px;
            :deep(.el-rate__icon) {
              font-size: 14px !important;
              margin-right: 2px;
            }
          }

          .inline-rating--small {
            :deep(.el-rate__icon) {
              font-size: 12px !important;
            }
          }
        }

        .text {
          font-size: 14px;
          color: #444;
          line-height: 1.6;
          margin: 0 0 12px;
          white-space: pre-wrap;
        }

        .item-actions {
          display: flex;
          gap: 20px;
          font-size: 13px;
          color: #999;

          .action {
            display: flex;
            align-items: center;
            gap: 4px;
            cursor: pointer;
            transition: color 0.2s;

            &:hover {
              color: #409eff;
            }

            &.delete:hover {
              color: #f56c6c;
            }

            &.liked {
              color: #409eff;
            }
          }
        }

        .reply-container {
          margin-top: 12px;

          .reply-toggle {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 6px 16px;
            border-radius: 20px;
            color: #065fd4;
            font-size: 14px;
            font-weight: 500;
            cursor: pointer;
            transition: all 0.2s;
            margin-bottom: 8px;

            &:hover {
              background: #deebff;
            }
            .el-icon {
              font-size: 16px;
            }
          }

          .reply-list {
            position: relative;
            padding-left: 36px;
            margin-top: 12px;

            .root-trunk-line {
              position: absolute;
              left: 16px;
              top: -12px;
              height: 22px;
              width: 2px;
              background: #e5e5e5;
              z-index: 1;
            }
          }
        }
      }
    }
  }
}

.reply-rating-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 16px;

  .rating-label {
    font-size: 13px;
    color: #666;
    white-space: nowrap;
  }
}

/* 暗黑模式适配 */
html.dark .comment-section {
  background-color: #1e293b !important;
  
  .section-title {
    color: #f1f5f9 !important;
  }

  .rating-summary {
    background: linear-gradient(135deg, #2d2a1a, #3a3520) !important;
    border-color: #5c4d1a !important;

    .rating-count {
      color: #94a3b8 !important;
    }
  }

  .publish-box .actions .rating-picker .rating-label,
  .reply-rating-row .rating-label {
    color: #94a3b8 !important;
  }

  .reply-toggle {
    color: #409eff !important;
    &:hover {
      background: rgba(64,158,255,0.1) !important;
    }
  }

  .reply-list .reply-item::before {
    background: #4c4d4f !important;
  }
  .reply-list .reply-item::after {
    border-color: #4c4d4f !important;
  }
  .reply-list.is-hovered .reply-item::before {
    background: #6b7280 !important;
  }
  .reply-list.is-hovered .reply-item::after {
    border-color: #6b7280 !important;
  }
}



html.dark .comment-section {
  .comment-item {
    border-bottom-color: #334155 !important;
    
    .username {
      color: #e2e8f0 !important;
    }
    
    .text {
      color: #cbd5e1 !important;
    }
    
    .time,
    .item-actions .action {
      color: #94a3b8 !important;
    }
    
    .reply-list {
      background-color: #0f172a !important;
      .root-trunk-line { background: #4c4d4f !important; }
    }
  }
  
  :deep(.el-textarea__inner) {
    background-color: #334155 !important;
    color: #e2e8f0 !important;
    border-color: #475569 !important;
  }
}
</style>

<style lang="scss">
/* 全局作用域：由于 el-drawer 设置了 append-to-body，它的样式必须放在非 scoped block 里 */
.mobile-reply-drawer {
  .el-drawer__body {
    padding: 0;
    display: flex;
    flex-direction: column;
  }
  
  .drawer-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 20px;
    border-bottom: 1px solid #f0f0f0;
    
    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }
    
    .close-btn {
      font-size: 20px;
      color: #999;
      cursor: pointer;
    }
  }

  .drawer-body {
    flex: 1;
    overflow-y: auto;
    padding: 20px;

    .main-comment-summary {
      display: flex;
      gap: 12px;
      
      .content {
        flex: 1;
        .user-info {
          display: flex;
          align-items: center;
          gap: 12px;
          margin-bottom: 6px;
          .username { font-weight: 500; font-size: 14px; }
          .time { font-size: 12px; color: #999; }
        }
        .text {
          font-size: 14px;
          line-height: 1.5;
          margin: 0 0 12px 0;
          color: #333;
        }
        .item-actions {
          color: #999;
          font-size: 13px;
          .action {
            display: flex; align-items: center; gap: 4px; cursor: pointer;
            &:hover { color: #409eff; }
          }
        }
      }
    }

    .drawer-divider {
      height: 10px;
      background: #f5f7fa;
      margin: 20px -20px;
    }

    .drawer-reply-list {
      position: relative;
      margin-left: 0;
      padding-left: 36px;

      .root-trunk-line {
        position: absolute;
        left: 16px;
        top: -12px;
        height: 22px;
        width: 2px;
        background: #e5e5e5;
        z-index: 1;
      }
    }
  }
}

html.dark .mobile-reply-drawer {
  background: #1e293b;
  .drawer-header { border-color: #334155; h3 { color: #fff; } }
  .drawer-divider { background: #0f172a; }
  .drawer-reply-list .root-trunk-line { background: #4c4d4f !important; }
}
</style>
