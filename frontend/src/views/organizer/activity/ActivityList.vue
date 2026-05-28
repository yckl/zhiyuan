<template>
  <div class="org-activity-list" v-loading="loading">
    <!-- ==================== 1. PC 端 (保持原样，仅做结构性包装) ==================== -->
    <div v-if="!isMobile" class="pc-container">
      <div class="pc-header">
        <div class="hd-left">
          <h2 class="page-title">活动管理</h2>
          <span class="total-count">{{ total }} 个活动</span>
        </div>
        <div class="hd-right">
          <div class="search-box">
            <el-input
              v-model="queryParams.title"
              placeholder="搜索活动..."
              class="ghost-search"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-select v-model="queryParams.status" placeholder="状态" clearable class="ghost-status" @change="handleSearch">
              <el-option v-for="(val, key) in STATUS_MAP" :key="key" :label="val.label" :value="key" />
            </el-select>
          </div>
          <el-button class="create-btn" type="primary" @click="router.push('/organizer/activity/create')">
            <el-icon><Plus /></el-icon> 发布活动
          </el-button>
        </div>
      </div>

      <div v-if="activities.length > 0" class="card-list-container">
        <div 
          v-for="row in activities" 
          :key="row.id" 
          class="activity-card-row"
        >
          <!-- 左侧缩略图 (16:9) -->
          <div class="thumb-wrap" @click.stop="handleAudit(row.id)">
            <el-image :src="row.coverImage || '/default-cover.jpg'" fit="cover" class="cover-thumb">
  <template #error>
    <img :src="'/default-cover.jpg'" style="width:100%;height:100%;object-fit:cover"/>
  </template>
</el-image>
            <div class="status-overlay">
              <span class="status-pill" :style="{ backgroundColor: STATUS_MAP[row.status]?.bg, color: STATUS_MAP[row.status]?.color }">
                {{ STATUS_MAP[row.status]?.label || '未知' }}
              </span>
            </div>
          </div>

          <!-- 主体信息 -->
          <div class="content-wrap">
            <div class="info-top">
              <h3 class="act-title" @click.stop="handleAudit(row.id)">{{ row.title }}</h3>
              <div class="act-sub-info">
                <span class="cat-tag">{{ row.categoryName || '未分类' }}</span>
                <span class="act-id">ID: {{ row.id }}</span>
              </div>
            </div>

            <div class="info-bottom">
              <!-- 进度条区 -->
              <div class="progress-section">
                <div class="quota-text">
                  <span class="label">报名进度</span>
                  <span class="nums"><b>{{ row.currentParticipants || 0 }}</b> / {{ row.maxParticipants || '不限' }}</span>
                </div>
                <!-- 极光青渐变进度条 -->
                <div class="custom-progress">
                  <div class="progress-track">
                    <div 
                      class="progress-fill" 
                      :style="{ width: `${getQuotaPercentage(row)}%`, background: isFull(row) ? '#10b981' : 'linear-gradient(90deg, #0093E9 0%, #80D0C7 100%)' }"
                    ></div>
                  </div>
                </div>
              </div>

              <!-- 时间展示 & 常驻操作栏容易 -->
              <div class="action-space">
                <div class="time-display">
                  <el-icon><Calendar /></el-icon>
                  <span>{{ dayjs(row.startTime).format('MM-DD HH:mm') }}</span>
                </div>

                <!-- 常驻操作栏 -->
                <div class="actions-group">
                  <el-button class="action-btn audit" @click.stop="handleAudit(row.id)">
                    <el-icon><User /></el-icon>
                    <span>审核</span>
                  </el-button>
                  <el-button class="action-btn edit" @click.stop="handleEdit(row.id)">
                    <el-icon><EditPen /></el-icon>
                    <span>编辑</span>
                  </el-button>
                  <el-button class="action-btn preview" @click.stop="router.push(`/activity/${row.id}`)">
                    <el-icon><View /></el-icon>
                    <span>预览</span>
                  </el-button>
                  <el-button class="action-btn delete" @click.stop="confirmDelete(row.id)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <el-empty v-else-if="!loading" description="暂无符合条件的活动" :image-size="120">
        <template #image>
          <div style="font-size: 60px; line-height: 120px; text-align: center;">🏖</div>
        </template>
        <el-button type="primary" class="create-btn empty-btn" @click.stop="router.push('/organizer/activity/create')" size="large">
          发布你的第一个活动
        </el-button>
      </el-empty>

      <div class="pag-wrap" v-if="total > queryParams.size">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :total="total"
          layout="total, prev, pager, next"
          background
          @current-change="fetchActivities"
        />
      </div>
    </div>

    <!-- ==================== 2. 移动端 (iOS Card Stream) ==================== -->
    <div v-else class="m-activity-container">
      <!-- 顶部：iOS 风格黏性胶囊栏 -->
      <div class="m-sticky-header">
        <div class="m-search-wrapper">
          <div class="m-capsule-input">
            <el-icon class="m-prefix-icon"><Search /></el-icon>
            <input
              v-model="queryParams.title"
              placeholder="搜索我的活动"
              @keyup.enter="handleSearch"
            />
          </div>
          <el-dropdown trigger="click" @command="(val: number | null) => { queryParams.status = val; handleSearch() }">
            <div class="m-filter-pill">
              <span>{{ STATUS_MAP[queryParams.status as number]?.label || '筛选' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item :command="null">全部状态</el-dropdown-item>
                <el-dropdown-item v-for="(val, key) in STATUS_MAP" :key="key" :command="Number(key)">
                  {{ val.label }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 活动流：Instagram 风格卡片 -->
      <div class="m-card-stream">
        <div
          v-for="row in activities"
          :key="row.id"
          class="m-premium-card"
        >
          <!-- 封面图带状态标签 -->
          <div class="m-card-media" @click.stop="handleAudit(row.id)">
            <el-image :src="row.coverImage || '/default-cover.jpg'" fit="cover" class="m-main-image">
  <template #error>
    <img :src="'/default-cover.jpg'" style="width:100%;height:100%;object-fit:cover"/>
  </template>
</el-image>
            <div class="m-status-float" :style="{ backgroundColor: getStatusBg(row.status), color: getStatusColor(row.status) }">
              {{ STATUS_MAP[row.status]?.label }}
            </div>
          </div>

          <!-- 卡片文字内容 -->
          <div class="m-card-details">
            <div class="m-card-top-row">
              <h3 class="m-card-name" @click.stop="handleAudit(row.id)">{{ row.title }}</h3>
            </div>
            
            <div class="m-meta-stack">
              <div class="m-meta-item">
                <el-icon><Calendar /></el-icon>
                <span>{{ dayjs(row.startTime).format('YYYY-MM-DD HH:mm') }}</span>
              </div>
              <div class="m-meta-item">
                <el-icon><User /></el-icon>
                <span>已报 {{ row.currentParticipants || 0 }}/{{ row.maxParticipants || '不限' }} 题</span>
              </div>
            </div>

            <!-- 操作动作 -->
            <div class="m-card-actions">
              <el-button link class="m-action-btn primary" @click.stop="handleEdit(row.id)">管理</el-button>
              <el-button link class="m-action-btn" @click.stop="handleAudit(row.id)">审核</el-button>
              <el-dropdown trigger="click" @command="(cmd: string) => handleCommand(cmd, row)">
                <el-button link class="m-action-btn more">
                  <el-icon><MoreFilled /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="view">查看预览</el-dropdown-item>
                    <el-dropdown-item command="delete" class="del-text">下架活动</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>
        
        <el-empty v-if="activities.length === 0" description="暂无符合条件的活动" :image-size="100" />
      </div>

      <!-- 分页控制 -->
      <div class="m-pagination-footer" v-if="total > queryParams.size">
        <el-pagination
          v-model:current-page="queryParams.page"
          :total="total"
          :page-size="queryParams.size"
          layout="prev, pager, next"
          small
          @current-change="fetchActivities"
        />
      </div>

      <!-- 悬浮发布按钮 -->
      <div class="fab-ios" @click.stop="router.push('/organizer/activity/create')">
        <el-icon><Plus /></el-icon>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Plus, Search, EditPen, MoreFilled,
  User, Delete, Calendar, ArrowDown, View
} from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { useMobile } from '@/composables/useMobile'

const router = useRouter()
const loading = ref(false)
const { isMobile } = useMobile()

const STATUS_MAP: Record<number, { label: string; color: string; bg: string }> = {
  0: { label: '草稿', color: '#64748b', bg: '#f1f5f9' },
  1: { label: '待审核', color: '#d97706', bg: '#fef3c7' },
  2: { label: '招募中', color: '#059669', bg: '#d1fae5' },
  3: { label: '进行中', color: '#2563eb', bg: '#dbeafe' },
  4: { label: '已结束', color: '#475569', bg: '#f1f5f9' },
  5: { label: '已取消', color: '#dc2626', bg: '#fee2e2' }
}

const queryParams = reactive({ page: 1, size: 10, title: '', status: null as number | null })
const activities = ref<any[]>([])
const total = ref(0)

const fetchActivities = async () => {
  loading.value = true
  try {
    const res = await request.get('/activity/my', queryParams)
    activities.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    ElMessage.error('获取活动列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { 
  queryParams.page = 1; 
  activities.value = []; // 强制清空旧数据以触发骨架屏(或让页面完全刷新)
  fetchActivities() 
}
const handleEdit = (id: number) => router.push(`/organizer/activity/edit/${id}`)
const handleAudit = (id: number) => router.push(`/organizer/personnel/audit?activityId=${id}`)

const handleCommand = (cmd: string, row: any) => {
  switch (cmd) {
    case 'audit': handleAudit(row.id); break
    case 'view': router.push(`/activity/${row.id}`); break
    case 'delete': confirmDelete(row.id); break
  }
}

const confirmDelete = (id: number) => {
  ElMessageBox.confirm('确定要下架此活动吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      await request.delete(`/activity/${id}`)
      ElMessage.success('活动已下架')
      fetchActivities()
    } catch (e) { ElMessage.error('操作失败') }
  }).catch(() => {})
}

const getQuotaPercentage = (row: any) => {
  if (!row.maxParticipants) return 0
  return Math.min(Math.round((row.currentParticipants || 0) / row.maxParticipants * 100), 100)
}

const getStatusColor = (status: number) => STATUS_MAP[status]?.color || '#999'
const getStatusBg = (status: number) => STATUS_MAP[status]?.bg || '#f9f9f9'

const isFull = (row: any) => row.maxParticipants && row.currentParticipants >= row.maxParticipants

onMounted(() => {
  fetchActivities()
})
</script>

<style scoped lang="scss">
.org-activity-list { min-height: 100vh; background: #f8fafc; }

/* ==================== PC 端样式 ==================== */
.pc-container {
  padding: 32px; max-width: 1100px; margin: 0 auto;

  .pc-header {
    display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 32px;
    .hd-left {
      .page-title { font-size: 24px; font-weight: 900; color: #1e293b; margin: 0 0 6px; letter-spacing: -0.5px; }
      .total-count { font-size: 14px; color: #64748b; font-weight: 600; }
    }
    .hd-right { display: flex; align-items: center; gap: 16px; }
    .search-box {
      display: flex; background: #fff; border-radius: 12px; padding: 4px; box-shadow: 0 2px 10px rgba(0,0,0,0.02);
      .ghost-search { width: 220px;
        :deep(.el-input__wrapper) { background: transparent; box-shadow: none; }
      }
      .ghost-status { width: 120px;
        :deep(.el-input__wrapper) { background: transparent; box-shadow: none; }
      }
    }
    .create-btn { height: 42px; padding: 0 24px; border-radius: 12px; font-weight: 700; 
      background: linear-gradient(135deg, #0093E9 0%, #80D0C7 100%); border: none;
      box-shadow: 0 8px 16px rgba(0, 147, 233, 0.2); transition: all 0.3s;
      &:hover { transform: translateY(-2px); box-shadow: 0 12px 24px rgba(0, 147, 233, 0.3); }
    }
  }

  .card-list-container {
    display: flex; flex-direction: column; gap: 16px;
  }

  /* 去线条化，独立磨砂圆角卡片 */
  .activity-card-row {
    display: flex;
    background: rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(20px);
    border: 1px solid rgba(255, 255, 255, 0.6);
    border-radius: 20px;
    padding: 16px;
    gap: 20px;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.02), 0 2px 4px -1px rgba(0, 0, 0, 0.01);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    overflow: hidden;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.05), 0 10px 10px -5px rgba(0, 0, 0, 0.02);
      border-color: rgba(0, 147, 233, 0.2);
    }

    /* 左侧微缩?(16:9) */
    .thumb-wrap {
      width: 160px; height: 90px;
      border-radius: 12px;
      overflow: hidden;
      position: relative;
      flex-shrink: 0;
      cursor: pointer;

      .cover-thumb {
        width: 100%; height: 100%;
        transition: transform 0.3s;
        &:hover { transform: scale(1.05); }
      }

      .status-overlay {
        position: absolute; top: 6px; left: 6px;
        .status-pill {
          padding: 4px 10px; border-radius: 20px; font-size: 11px; font-weight: 800;
          box-shadow: 0 2px 8px rgba(0,0,0,0.15); backdrop-filter: blur(4px);
        }
      }
    }

    /* 主体信息 */
    .content-wrap {
      flex: 1; display: flex; flex-direction: column; justify-content: space-between; min-width: 0;
      padding: 4px 0;

      .info-top {
        display: flex; justify-content: space-between; align-items: flex-start;
        .act-title { 
          margin: 0; font-size: 18px; font-weight: 800; color: #1e293b; cursor: pointer;
          white-space: nowrap; overflow: hidden; text-overflow: ellipsis; padding-right: 16px; transition: color 0.2s;
          &:hover { color: #0093E9; }
        }
        .act-sub-info {
          display: flex; align-items: center; gap: 8px; font-size: 12px; font-weight: 700; color: #64748b;
          .cat-tag { background: #f1f5f9; padding: 4px 8px; border-radius: 6px; }
          .act-id { opacity: 0.7; }
        }
      }

      .info-bottom {
        display: flex; align-items: center; justify-content: space-between; position: relative;

        .progress-section {
          flex: 1; max-width: 320px;
          .quota-text { 
            display: flex; justify-content: space-between; font-size: 12px; font-weight: 600; color: #64748b; margin-bottom: 6px;
            .nums b { color: #0f172a; font-size: 14px; }
          }
          
          /* 渐变进度条 */
          .custom-progress {
            height: 6px; background: #e2e8f0; border-radius: 99px; overflow: hidden;
            .progress-fill {
              height: 100%; border-radius: 99px;
              transition: width 0.8s cubic-bezier(0.34, 1.56, 0.64, 1);
            }
          }
        }

        .action-space {
          display: flex; align-items: center; justify-content: flex-end; gap: 20px;
          position: relative; overflow: hidden; height: 42px; width: auto;
          
          .time-display {
            display: flex; align-items: center; gap: 6px; font-size: 13px; color: #64748b; font-weight: 700;
          }
          
          /* 常驻操作栏 */
          .actions-group {
            display: flex; gap: 8px;

            .action-btn {
              border: none; background: #f1f5f9; border-radius: 10px; height: 36px; padding: 0 16px;
              font-weight: 700; font-size: 13px; color: #475569; transition: all 0.2s;
              display: flex; align-items: center; gap: 6px; margin: 0;
              
              &:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.08); }
              &.edit:hover { background: #e0f2fe; color: #0284c7; }
              &.preview:hover { background: #fae8ff; color: #c026d3; }
              &.audit { background: rgba(0, 147, 233, 0.1); color: #0093E9; }
              &.audit:hover { background: #0093E9; color: #fff; box-shadow: 0 4px 12px rgba(0, 147, 233, 0.3); }
              &.delete { width: 36px; padding: 0; justify-content: center; }
              &.delete:hover { background: #fee2e2; color: #dc2626; }
            }
          }
        }
      }
    }
  }

  .pag-wrap { margin-top: 32px; display: flex; justify-content: center; }
}



/* ==================== 移动端?iOS Premium Card Stream ==================== */
.m-activity-container {
  background: #f4f7f9;
  min-height: 100vh;
  padding-bottom: 110px;

  /* 顶部胶囊搜索栏 */
  .m-sticky-header {
    position: sticky;
    top: 0;
    z-index: 100;
    background: rgba(244, 247, 249, 0.85);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    padding: 12px 16px;
    border-bottom: 0.5px solid rgba(0, 0, 0, 0.05);

    .m-search-wrapper {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .m-capsule-input {
      flex: 1;
      height: 40px;
      background: #fff;
      border-radius: 20px;
      display: flex;
      align-items: center;
      padding: 0 14px;
      box-shadow: 0 2px 10px rgba(0, 0, 0, 0.03);
      
      .m-prefix-icon { color: #94a3b8; font-size: 18px; margin-right: 8px; }
      input {
        border: none;
        background: transparent;
        outline: none;
        font-size: 14px;
        width: 100%;
        color: #1e293b;
        &::placeholder { color: #cbd5e1; }
      }
    }

    .m-filter-pill {
      height: 40px;
      padding: 0 16px;
      background: #fff;
      border-radius: 20px;
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 14px;
      font-weight: 600;
      color: #3b82f6;
      box-shadow: 0 2px 10px rgba(0, 0, 0, 0.03);
      cursor: pointer;
      &:active { transform: scale(0.95); }
    }
  }

  /* 卡片流媒体列 */
  .m-card-stream {
    padding: 16px;
    display: flex;
    flex-direction: column;
    gap: 16px;

    .m-premium-card {
      background: #fff;
      border-radius: 20px;
      overflow: hidden;
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.04);
      transition: transform 0.2s;
      &:active { transform: scale(0.985); }

      .m-card-media {
        position: relative;
        height: 160px;
        width: 100%;
        
        .m-main-image { width: 100%; height: 100%; }

        .m-status-float {
          position: absolute;
          top: 12px;
          right: 12px;
          padding: 4px 12px;
          border-radius: 20px;
          font-size: 11px;
          font-weight: 800;
          backdrop-filter: blur(8px);
          -webkit-backdrop-filter: blur(8px);
          box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
        }
      }

      .m-card-details {
        padding: 16px;

        .m-card-name {
          font-size: 17px;
          font-weight: 800;
          color: #1e293b;
          margin: 0 0 12px;
          line-height: 1.4;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }

        .m-meta-stack {
          display: flex;
          flex-direction: column;
          gap: 6px;
          margin-bottom: 16px;

          .m-meta-item {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 13px;
            color: #64748b;
            font-weight: 500;
            .el-icon { color: #94a3b8; font-size: 15px; }
          }
        }

        .m-card-actions {
          display: flex;
          align-items: center;
          border-top: 1px solid #f1f5f9;
          padding-top: 12px;
          gap: 12px;

          .m-action-btn {
            margin: 0;
            font-size: 14px;
            font-weight: 700;
            color: #64748b;
            
            &.primary { color: #3b82f6; }
            &.more { 
              margin-left: auto;
              width: 32px; height: 32px; border-radius: 50%; background: #f8fafc; 
              display: flex; align-items: center; justify-content: center;
            }
            &:active { opacity: 0.6; }
          }
        }
      }
    }
  }

  .m-pagination-footer {
    display: flex;
    justify-content: center;
    padding: 24px 0;
  }

  .fab-ios {
    position: fixed;
    bottom: 90px;
    right: 20px;
    width: 56px;
    height: 56px;
    border-radius: 50%;
    background: #3b82f6;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 26px;
    box-shadow: 0 8px 25px rgba(59, 130, 246, 0.4);
    z-index: 200;
    cursor: pointer;
    &:active { transform: scale(0.9); }
  }
}

.del-text { color: #ef4444 !important; font-weight: 600; }
</style>
