<template>
  <div class="reply-node">
    <div class="reply-item">
      <el-avatar :size="28" :src="reply.userAvatar" class="avatar">
        {{ reply.username?.charAt(0) || '?' }}
      </el-avatar>
      <div class="content">
        <div class="user-info">
          <span class="username">{{ reply.username || '匿名用户' }}</span>
          <span class="time">{{ formatDate(reply.createTime) }}</span>
        </div>
        <p class="text">
          <span v-if="reply.replyToUsername && reply.replyToUserId !== rootUserId" class="reply-to-text">
            @{{ reply.replyToUsername }}
          </span>
          {{ reply.content }}
        </p>
        <div class="item-actions">
          <span class="action" @click="$emit('reply', reply)">
            <el-icon><ChatDotRound /></el-icon> 回复
          </span>
          <span class="action" :class="{ liked: reply.isLiked }" @click="$emit('like', reply)">
            <el-icon><Star /></el-icon> {{ reply.likeCount || 0 }}
          </span>
          <el-popconfirm
            v-if="canDelete(reply)"
            title="确定要删除这条回复吗?"
            @confirm="$emit('delete', reply.id)"
          >
            <template #reference>
              <span class="action delete">删除</span>
            </template>
          </el-popconfirm>
        </div>
      </div>
    </div>
    
    <div class="reply-children" v-if="reply.children && reply.children.length > 0">
      <CommentReplyNode
        v-for="child in reply.children"
        :key="child.id"
        :reply="child"
        :root-user-id="rootUserId"
        @reply="$emit('reply', $event)"
        @like="$emit('like', $event)"
        @delete="$emit('delete', $event)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ChatDotRound, Star } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
// Vue 3 SFC automatically resolves self-references based on the file name

const props = defineProps({
  reply: { type: Object, required: true },
  rootUserId: { type: Number, required: true }
})

defineEmits(['reply', 'like', 'delete'])

const formatDate = (date: any) => {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const canDelete = (item: any) => {
  const userInfoStr = localStorage.getItem('userInfo')
  if (!userInfoStr) return false
  try {
    const userInfo = JSON.parse(userInfoStr)
    return userInfo.id === item.userId || userInfo.role === 'ADMIN'
  } catch {
    return false
  }
}
</script>

<style scoped lang="scss">
.reply-node {
  position: relative;
  
  .reply-item {
    display: flex;
    gap: 12px;
    padding: 10px 0;
    position: relative;
    
    .avatar { flex-shrink: 0; }
    
    .content {
      flex: 1;
      .user-info {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 4px;
        .username { font-size: 13px; font-weight: 500; color: #333; }
        .time { font-size: 12px; color: #999; }
      }
      .text {
        font-size: 13px;
        color: #444;
        line-height: 1.5;
        margin: 0 0 8px;
        .reply-to-text { color: #065fd4; margin-right: 4px; font-weight: 500; }
      }
      .item-actions {
        display: flex;
        gap: 16px;
        font-size: 12px;
        color: #999;
        .action { display: flex; align-items: center; gap: 4px; cursor: pointer; transition: color 0.2s; }
        .action:hover { color: #409eff; }
        .liked { color: #409eff; }
        .delete:hover { color: #f56c6c; }
      }
    }
    
    /* Trunk connecting downwards */
    &::after {
      content: '';
      position: absolute;
      left: -20px;
      top: 24px; /* avatar is 28px, 10px padding -> center is 24px */
      bottom: -10px; /* extends to start of next sibling padding */
      width: 2px;
      background: #e5e5e5;
      z-index: 1;
    }
    
    /* L shape branch */
    &::before {
      content: '';
      position: absolute;
      left: -20px;
      top: -10px; /* connect to parent's trunk above */
      width: 14px; /* leaves 6px gap to avatar */
      height: 34px; /* 10px padding + 24px to avatar center */
      border-left: 2px solid #e5e5e5;
      border-bottom: 2px solid #e5e5e5;
      border-bottom-left-radius: 10px;
      box-sizing: border-box;
      z-index: 1;
    }
  }

  /* Do not draw a trunk continuing from the last sibling */
  &:last-child > .reply-item::after {
    display: none;
  }

  .reply-children {
    position: relative;
    margin-left: 32px; /* Indent sub-replies */
  }
}

/* Dark mode compatibility */
html.dark .reply-node {
  .reply-item {
    .user-info .username { color: #e2e8f0; }
    .text { color: #cbd5e1; }
    &::after { background: #4c4d4f; }
    &::before { border-color: #4c4d4f; }
  }
}
</style>
