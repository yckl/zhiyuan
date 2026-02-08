<template>
  <div class="comment-section">
    <div class="comment-header">
      <h3 class="section-title">
        <el-icon><ChatLineRound /></el-icon>
        全部评论 ({{ count }})
      </h3>
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

    <!-- 评论列表 -->
    <div class="comment-list" v-loading="loading">
      <div v-for="comment in comments" :key="comment.id" class="comment-item">
        <el-avatar :size="40" :src="comment.userAvatar" class="avatar">
          {{ comment.username?.charAt(0) || '匿' }}
        </el-avatar>
        <div class="content">
          <div class="user-info">
            <span class="username">{{ comment.username || '匿名用户' }}</span>
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
              title="确定要删除这条评论吗？"
              @confirm="handleDelete(comment.id)"
            >
              <template #reference>
                <span class="action delete">删除</span>
              </template>
            </el-popconfirm>
          </div>

          <!-- 子评论列表 -->
          <div v-if="comment.children && comment.children.length" class="reply-list">
            <div v-for="reply in comment.children" :key="reply.id" class="reply-item">
              <el-avatar :size="28" :src="reply.userAvatar" class="avatar">
                {{ reply.username?.charAt(0) || '匿' }}
              </el-avatar>
              <div class="content">
                <div class="user-info">
                  <span class="username">{{ reply.username || '匿名用户' }}</span>
                  <span v-if="reply.replyToUsername" class="reply-to">
                    回复 <span class="target">@{{ reply.replyToUsername }}</span>
                  </span>
                  <span class="time">{{ formatDate(reply.createTime) }}</span>
                </div>
                <p class="text">{{ reply.content }}</p>
                <div class="item-actions">
                  <span class="action" @click="handleReply(comment, reply)">
                    <el-icon><ChatDotRound /></el-icon> 回复
                  </span>
                  <span class="action" @click="handleLike(reply)">
                    <el-icon><Star /></el-icon> {{ reply.likeCount || 0 }}
                  </span>
                  <el-popconfirm
                    v-if="canDelete(reply)"
                    title="确定要删除这条回复吗？"
                    @confirm="handleDelete(reply.id)"
                  >
                    <template #reference>
                      <span class="action delete">删除</span>
                    </template>
                  </el-popconfirm>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-if="!loading && comments.length === 0" description="暂无评论，快来抢沙发吧~" />
    </div>

    <!-- 回复对话框 -->
    <el-dialog
      v-model="replyDialog.visible"
      :title="'回复 @' + replyDialog.targetUsername"
      width="500px"
      append-to-body
    >
      <el-input
        v-model="replyDialog.content"
        type="textarea"
        :rows="4"
        :placeholder="'回复 @' + replyDialog.targetUsername + '...'"
        resize="none"
      />
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ChatLineRound, User, ChatDotRound, Star } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { request } from '@/utils/request'
import dayjs from 'dayjs'

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

const loading = ref(false)
const submitting = ref(false)
const comments = ref<any[]>([])
const newComment = ref('')

const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const userAvatar = computed(() => userInfo.avatar)

const replyDialog = reactive({
  visible: false,
  submitting: false,
  content: '',
  parentId: 0,
  targetUserId: 0,
  targetUsername: '',
  replyToId: 0
})

const fetchComments = async () => {
  loading.value = true
  try {
    const res = await request.get('/comment/list', {
      targetType: props.targetType,
      targetId: props.targetId
    })
    comments.value = res.data || []
  } catch (error) {
    console.error('获取评论列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!newComment.value.trim()) return
  submitting.value = true
  try {
    const res = await request.post('/comment', {
      targetType: props.targetType,
      targetId: props.targetId,
      content: newComment.value,
      parentId: 0
    })
    if (res.code === 200) {
      ElMessage.success('发布完成')
      newComment.value = ''
      fetchComments()
      emit('refresh')
    }
  } catch (error) {
    console.error('发表评论失败:', error)
  } finally {
    submitting.value = false
  }
}

const handleReply = (parent: any, replyTo?: any) => {
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
  replyDialog.visible = true
}

const submitReply = async () => {
  if (!replyDialog.content.trim()) return
  replyDialog.submitting = true
  try {
    const res = await request.post('/comment', {
      targetType: props.targetType,
      targetId: props.targetId,
      content: replyDialog.content,
      parentId: replyDialog.parentId,
      replyToUserId: replyDialog.targetUserId,
      replyToId: replyDialog.replyToId > 0 ? replyDialog.replyToId : null
    })
    if (res.code === 200) {
      ElMessage.success('回复成功')
      replyDialog.visible = false
      fetchComments()
      emit('refresh')
    }
  } catch (error) {
    console.error('回复失败:', error)
  } finally {
    replyDialog.submitting = false
  }
}

const handleLike = async (comment: any) => {
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
  fetchComments()
})
</script>

<style lang="scss" scoped>
.comment-section {
  margin-top: 30px;
  background: #fff;
  padding: 24px;
  border-radius: 12px;

  .comment-header {
    margin-bottom: 24px;
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
  }

  .publish-box {
    display: flex;
    gap: 16px;
    margin-bottom: 30px;

    .input-area {
      flex: 1;

      .actions {
        display: flex;
        justify-content: flex-end;
        margin-top: 12px;
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

          .username {
            font-weight: 500;
            color: #333;
            font-size: 14px;
          }

          .time {
            font-size: 12px;
            color: #999;
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

        .reply-list {
          margin-top: 16px;
          padding: 12px 16px;
          background: #f8f9fb;
          border-radius: 8px;

          .reply-item {
            display: flex;
            gap: 12px;
            padding: 12px 0;
            border-bottom: 1px solid #ebedf0;

            &:last-child {
              border-bottom: none;
              padding-bottom: 0;
            }

            &:first-child {
              padding-top: 0;
            }

            .reply-to {
              font-size: 12px;
              color: #999;
              
              .target {
                color: #409eff;
                font-weight: 500;
              }
            }
          }
        }
      }
    }
  }
}

/* 暗黑模式适配 */
html.dark .comment-section {
  background-color: #1e293b !important;
  
  .section-title {
    color: #f1f5f9 !important;
  }
  
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
      
      .reply-item {
        border-bottom-color: #1e293b !important;
      }
    }
  }
  
  :deep(.el-textarea__inner) {
    background-color: #334155 !important;
    color: #e2e8f0 !important;
    border-color: #475569 !important;
  }
}
</style>
