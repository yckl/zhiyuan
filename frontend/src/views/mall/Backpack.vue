<template>
  <div class="backpack-page">
    <!-- 橙色渐变 Banner -->
    <div class="backpack-hero">
      <div class="hero-inner">
        <div class="hero-content">
          <div class="hero-stats">
            <span class="count-num">{{ backpackItems.length }}</span>
            <span class="count-unit">件物品</span>
          </div>
        </div>
        <div class="hero-actions">
          <div class="hero-btn" @click="$router.push('/mall/index')">
            <el-icon><Shop /></el-icon>
            <span>去购物</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <el-input
      v-model="searchKeyword"
      placeholder="搜索物品"
      clearable
      :prefix-icon="SearchIcon"
      class="search-bar"
    />

    <!-- 分类标签 -->
    <div class="filter-tabs hide-scrollbar">
      <div
        v-for="tab in filterTabs"
        :key="tab.value"
        class="filter-tab"
        :class="{ active: activeTab === tab.value }"
        @click="activeTab = tab.value"
      >{{ tab.label }}</div>
    </div>

    <!-- 物品列表 -->
    <div class="items-container" v-loading="loading">
      <!-- 骨架屏 -->
      <div v-if="loading && backpackItems.length === 0" class="items-grid">
        <div v-for="i in 8" :key="i" class="skeleton-card">
          <el-skeleton animated>
            <template #template>
              <el-skeleton-item variant="image" style="height: 140px" />
              <div style="padding: 12px">
                <el-skeleton-item variant="h3" style="width: 70%" />
                <el-skeleton-item variant="text" style="width: 50%; margin-top: 8px" />
              </div>
            </template>
          </el-skeleton>
        </div>
      </div>
      <TransitionGroup v-else-if="displayItems.length > 0" name="card-fade" tag="div" class="items-grid">
        <div
          v-for="(item, idx) in displayItems"
          :key="item.id"
          class="item-card-slot"
          :class="{ 'is-used': item.status === 1 }"
          :style="{ animationDelay: `${idx * 0.04}s` }"
          @click="openDetailDrawer(item)"
        >
          <div class="slot-visual">
            <el-image
              :src="item.propImage || getDefaultImage(item.propType)"
              fit="contain"
              class="slot-image"
            >
              <template #error>
                <div class="image-fallback">
                  <el-icon><Box /></el-icon>
                </div>
              </template>
            </el-image>
            
            <div v-if="item.quantity > 1" class="slot-badge">
              x{{ item.quantity }}
            </div>

            <div v-if="item.status === 1" class="slot-used-mark">
              已使用
            </div>
          </div>
          <div class="slot-name">{{ item.propName }}</div>
        </div>
      </TransitionGroup>

      <el-empty v-else :description="emptyText" :image-size="120">
        <el-button type="primary" @click="$router.push('/mall/index')">
          去商城逛逛
        </el-button>
      </el-empty>
    </div>

    <!-- 物品详情弹窗 (精致游戏道具) -->
    <el-dialog
      v-model="showDetailDrawer"
      width="340px"
      :show-close="false"
      class="item-detail-dialog"
      align-center
    >
      <template v-if="detailItem">
        <div class="item-card-premium">
          <!-- 背景装饰：散落的粒子 -->
          <div class="glow-particles">
            <div v-for="i in 6" :key="i" class="particle"></div>
          </div>

          <!-- 顶部关闭按钮 -->
          <div class="card-close" @click="showDetailDrawer = false">
            <el-icon><Close /></el-icon>
          </div>

          <!-- 顶部核心图标：发光效果 -->
          <div class="item-header-glow">
            <div class="glow-ring"></div>
            <div class="glow-ray"></div>
            <div class="item-icon-wrapper">
              <el-image
                :src="detailItem.propImage || getDefaultImage(detailItem.propType)"
                fit="cover"
                class="premium-icon">
  <template #error>
    <img :src="'/default-cover.jpg'" style="width:100%;height:100%;object-fit:cover"/>
  </template>
</el-image>
            </div>
          </div>

          <!-- 核心信息 -->
          <div class="item-main-info">
            <h2 class="premium-name">{{ detailItem.propName }}</h2>
            <div class="premium-tags">
              <span class="p-tag">{{ getTypeText(detailItem.propType) }}</span>
              <span class="p-divider">|</span>
              <span class="p-tag">拥有: {{ detailItem.quantity }}</span>
              <span class="p-divider">|</span>
              <span class="p-tag">{{ getSourceText(detailItem.source) }}</span>
            </div>
          </div>

          <!-- 内容/操作栏 -->
          <div class="item-action-area">
            <template v-if="detailItem.status === 0">
              <!-- 如果是功能卡 -->
              <template v-if="isCardType(detailItem.propType)">
                <button class="streamer-btn" @click="goUseCard(detailItem)">
                  <span>立即使用</span>
                  <div class="streamer-light"></div>
                </button>
              </template>
              
              <!-- 如果是实物核销 -->
              <template v-else-if="detailItem.propType === 'GOODS' || detailItem.propType === 'PHYSICAL'">
                <div class="ticket-container" @click="copyCode(detailItem.pickupCode || getOrderCode(detailItem))">
                  <div class="ticket-top">
                    <span class="ticket-label">兑换核销</span>
                  </div>
                  <div class="ticket-split">
                    <div class="dot left"></div>
                    <div class="dash-line"></div>
                    <div class="dot right"></div>
                  </div>
                  <div class="ticket-bottom">
                    <div class="code-text-mono">
                      {{ detailItem.pickupCode || getOrderCode(detailItem) }}
                    </div>
                    <span class="copy-hint">(点击复制)</span>
                  </div>
                </div>
              </template>
            </template>
            <div v-else class="item-used-overlay">
              <div class="used-stamp">已使用</div>
            </div>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- 订单记录 -->
    <div class="orders-section" v-if="orders.length > 0">
      <h3>📦 兑换记录</h3>
      
      <!-- PC端表格 -->
      <div class="hidden-sm-and-down">
        <el-table :data="orders" stripe size="small">
          <el-table-column label="商品" min-width="180">
            <template #default="{ row }">
              <div class="order-goods">
                <el-image :src="row.goodsImage" style="width: 40px; height: 40px; border-radius: 4px">
  <template #error>
    <img :src="'/default-cover.jpg'" style="width:100%;height:100%;object-fit:cover"/>
  </template>
</el-image>
                <span>{{ row.goodsName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="pointsCost" label="消耗积分" width="100" align="center">
            <template #default="{ row }">
              <span class="points-cost">-{{ row.pointsCost }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="pickupCode" label="核销码" width="120" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.pickupCode" type="success" effect="plain" class="cyan-tag">{{ row.pickupCode }}</el-tag>
              <span v-else class="no-code">-</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getOrderStatusType(row.status)" size="small">
                {{ getOrderStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="时间" width="160" align="center">
            <template #default="{ row }">
              {{ formatDate(row.createTime) }}
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 移动端订单卡片 -->
      <div class="hidden-md-and-up mobile-order-list">
        <div v-for="order in orders" :key="order.id" class="mobile-order-card">
          <div class="order-header">
            <div class="goods-info">
              <el-image :src="order.goodsImage" class="goods-img" fit="cover">
  <template #error>
    <img :src="'/default-cover.jpg'" style="width:100%;height:100%;object-fit:cover"/>
  </template>
</el-image>
              <span class="goods-name">{{ order.goodsName }}</span>
            </div>
            <el-tag :type="getOrderStatusType(order.status)" size="small" effect="light">
              {{ getOrderStatusText(order.status) }}
            </el-tag>
          </div>
          
          <div class="order-body">
            <div class="info-item">
              <span class="label">消耗积分</span>
              <span class="value points">-{{ order.pointsCost }}</span>
            </div>
            <div class="info-item">
              <span class="label">兑换时间:</span>
              <span class="value">{{ formatDate(order.createTime) }}</span>
            </div>
            <div class="info-item" v-if="order.pickupCode">
              <span class="label">核销码</span>
              <el-tag type="success" size="small" effect="plain" class="cyan-tag">{{ order.pickupCode }}</el-tag>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 内容查看弹窗 -->
    <el-dialog v-model="showContentDialog" title="内容详情" width="400px">
      <div class="content-display">
        {{ currentContent }}
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Shop, Box, Search as SearchIcon, Close } from '@element-plus/icons-vue'
import { request } from '@/utils/request'

interface BackpackItem {
  id: number
  propName: string
  propImage?: string
  propType: string
  propContent?: string
  quantity: number
  status: number
  source?: string
  pickupCode?: string
}

interface Order {
  id: number
  orderNo: string
  goodsName: string
  goodsImage?: string
  pointsCost: number
  pickupCode?: string
  status: number
  createTime: string
}

const router = useRouter()
const loading = ref(false)
const activeTab = ref('all')
const backpackItems = ref<BackpackItem[]>([])
const orders = ref<Order[]>([])

const showContentDialog = ref(false)
const currentContent = ref('')
const searchKeyword = ref('')
const showDetailDrawer = ref(false)
const detailItem = ref<BackpackItem | null>(null)

const filterTabs = [
  { label: '全部', value: 'all' },
  { label: '可使用', value: 'unused' },
  { label: '已使用', value: 'used' }
]

const emptyText = computed(() => {
  if (activeTab.value === 'card') return '暂无功能卡片'
  if (activeTab.value === 'physical') return '暂无实物周边'
  return '背包空空如也~'
})

const filteredItems = computed(() => {
  if (activeTab.value === 'all') return backpackItems.value
  if (activeTab.value === 'unused') {
    return backpackItems.value.filter(i => i.status === 0)
  }
  if (activeTab.value === 'used') {
    return backpackItems.value.filter(i => i.status === 1)
  }
  return backpackItems.value
})

const displayItems = computed(() => {
  let list = filteredItems.value
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    list = list.filter(i => i.propName.toLowerCase().includes(kw))
  }
  return list
})

const openDetailDrawer = (item: BackpackItem) => {
  detailItem.value = item
  showDetailDrawer.value = true
}

const getDefaultImage = (type: string) => {
  if (type.includes('CARD') || type === 'MAKEUP_CARD') {
    return 'https://via.placeholder.com/200/667eea/fff?text=Card'
  }
  return 'https://via.placeholder.com/200/e6a23c/fff?text=Item'
}

const getSourceText = (source?: string) => {
  const map: Record<string, string> = {
    'EXCHANGE': '积分兑换',
    'LOTTERY': '抽奖获得',
    'ACTIVITY': '活动奖励',
    'SYSTEM': '系统赠送'
  }
  return map[source || ''] || '未知来源'
}

const getTypeText = (type: string) => {
  const map: Record<string, string> = {
    'GOODS': '实物商品',
    'PHYSICAL': '实物商品',
    'COUPON': '优惠券',
    'CARD_KEY': '卡密',
    'BADGE': '徽章',
    'MAKEUP_CARD': '补签卡',
    'SKIP_REVIEW_CARD': '免审核卡'
  }
  return map[type] || type
}

const isCardType = (type: string) => {
  return ['MAKEUP_CARD', 'SKIP_REVIEW_CARD', 'CARD'].includes(type) ||
    type.includes('CARD')
}

const getOrderCode = (item: BackpackItem) => {
  // 从订单中查找核销码
  const order = orders.value.find(o =>
    o.goodsName === item.propName && o.pickupCode
  )
  return order?.pickupCode || generateTempCode()
}

const generateTempCode = () => {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'
  let code = ''
  for (let i = 0; i < 6; i++) {
    code += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  return code
}

const getOrderStatusType = (status: number) => {
  const map: Record<number, string> = {
    0: 'warning',
    1: 'primary',
    2: 'success',
    3: 'info'
  }
  return map[status] || 'info'
}

const getOrderStatusText = (status: number) => {
  const map: Record<number, string> = {
    0: '待领取',
    1: '已核销',
    2: '已完成',
    3: '已取消'
  }
  return map[status] || '未知'
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

const fetchBackpack = async () => {
  loading.value = true
  try {
    const res = await request.get('/mall/backpack')
    if (res.code === 200) {
      backpackItems.value = res.data || []
    }
  } catch (error) {
    console.error('获取背包失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchOrders = async () => {
  try {
    const res = await request.get('/mall/orders')
    if (res.code === 200) {
      orders.value = res.data || []
    }
  } catch (error) {
    console.error('获取订单失败:', error)
  }
}

const goUseCard = (item: BackpackItem) => {
  if (item.propType === 'MAKEUP_CARD' || item.propName.includes('补签')) {
    router.push('/mall/checkin')
  } else if (item.propType === 'SKIP_REVIEW_CARD' || item.propName.includes('免审核')) {
    ElMessage.info('报名活动时可勾选"使用免审核卡"')
    router.push('/activity')
  } else {
    router.push('/activity')
  }
}

const copyCode = (code: string) => {
  if (!code) return
  import('@/utils/clipboard').then(({ copyToClipboard }) => {
    copyToClipboard(code, '核销码已复制到剪贴板')
  })
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  fetchBackpack()
  fetchOrders()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

const windowWidth = ref(window.innerWidth)
const isMobile = ref(window.innerWidth < 768)

const handleResize = () => {
  windowWidth.value = window.innerWidth
  isMobile.value = windowWidth.value < 768
}
</script>

<style lang="scss" scoped>
.backpack-page {
  padding: 0;
  background: var(--el-bg-color-page);
  min-height: 100vh;
}

// ================================================================
// Hero Banner
// ================================================================
.backpack-hero {
  position: relative;
  background: linear-gradient(135deg, #00C9A7 0%, #00B38F 100%);
  padding: 36px 16px 44px;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -20%;
    left: -10%;
    width: 60%;
    height: 100%;
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.4) 0%, transparent 80%);
    transform: skewX(-20deg);
    pointer-events: none;
  }
}

.hero-inner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 800px;
  margin: 0 auto;
}

  .hero-stats {
    display: flex;
    align-items: baseline;
    gap: 8px;
    margin-top: 12px;
  }

  .count-num {
    font-size: 64px;
    font-weight: 900;
    color: #fff;
    text-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
    letter-spacing: -2px;
    line-height: 1;
  }

  .count-unit {
    color: rgba(255, 255, 255, 0.85);
    letter-spacing: 0.5px;
  }

.hero-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 10px 16px;
  background: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(8px);
  border-radius: 12px;
  color: #fff;
  cursor: pointer;
  transition: background 0.2s;
  font-size: 12px;
  font-weight: 500;

  .el-icon { font-size: 20px; }
  &:active { background: rgba(255, 255, 255, 0.5); }
}

.search-bar {
  margin: 12px 16px 0;

  :deep(.el-input__wrapper) {
    height: 48px;
    border-radius: 24px;
    background: #fff;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03) !important;
    border: 1px solid rgba(0, 0, 0, 0.04);
  }
}

.filter-tabs {
  display: flex;
  gap: 12px;
  padding: 0 16px;
  margin-bottom: 24px;
  overflow-x: auto;

  .filter-tab {
    padding: 10px 22px;
    border-radius: 20px;
    background: #f1f5f9;
    color: #64748b;
    font-size: 14px;
    font-weight: 700;
    white-space: nowrap;
    cursor: pointer;
    transition: all 0.3s;
    border: 1px solid transparent;

    &.active {
      background: #00C9A7;
      color: #fff;
      box-shadow: 0 4px 12px rgba(0, 201, 167, 0.25);
    }
    
    &:hover:not(.active) {
      background: #e2e8f0;
      color: #1e293b;
    }
  }
}

.items-container {
  min-height: 300px;
  padding: 0 16px;
}

.order-section {
  margin-top: 48px;
  padding: 0 16px;

  .section-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 24px;
    h3 { margin: 0; font-size: 22px; font-weight: 800; color: #1e293b; }
  }
}

.order-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.items-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  padding: 0 16px;
}

.item-card-slot {
  background: #fff;
  border-radius: 24px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
  box-shadow: 0 8px 20px rgba(0, 201, 167, 0.06);
  border: 1px solid rgba(0, 201, 167, 0.04);
  text-align: center;

  &:hover {
    transform: translateY(-8px);
    box-shadow: 0 16px 36px rgba(0, 201, 167, 0.16);
    border-color: rgba(0, 201, 167, 0.2);
  }

  &.is-used { opacity: 0.5; filter: grayscale(0.5); }

  .slot-visual {
    position: relative;
    aspect-ratio: 1 / 1;
    background: #f8fafc;
    border-radius: 18px;
    margin-bottom: 12px;
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .slot-image {
      width: 100%;
      height: 100%;
      border-radius: 8px;
    }

    .image-fallback {
      font-size: 28px;
      color: #cbd5e1;
    }

    .slot-badge {
      position: absolute;
      top: 8px;
      right: 8px;
      background: #00C9A7;
      color: #fff;
      padding: 4px 10px;
      border-radius: 12px;
      font-size: 13px;
      font-weight: 800;
      box-shadow: 0 4px 8px rgba(0, 201, 167, 0.3);
      z-index: 2;
    }

    .slot-used-mark {
      position: absolute;
      inset: 0;
      background: rgba(0, 201, 167, 0.15);
      backdrop-filter: blur(4px);
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-weight: 800;
      font-size: 14px;
      z-index: 3;
      border-radius: 18px;
    }
  }

  .slot-name {
    font-size: 14px;
    font-weight: 700;
    color: #1e293b;
  }
}

.orders-section {
  margin-top: 40px;
  background: var(--el-bg-color-overlay);
  border-radius: 12px;
  padding: 20px;

    h3 {
      margin: 0 0 20px;
      font-size: 18px;
      font-weight: 700;
      color: #333;
      display: flex;
      align-items: center;
      gap: 8px;
      &::before {
        content: '';
        width: 4px;
        height: 18px;
        background: #00C9A7;
        margin-right: 8px;
        border-radius: 2px;
      }
    }
}

.records-table-container {
  background: #fff;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(0, 201, 167, 0.08);
  border: 1px solid rgba(0, 201, 167, 0.1);

  :deep(.el-table) {
    --el-table-header-bg-color: #334155;
    --el-table-header-text-color: #fff;
    --el-table-row-hover-bg-color: rgba(0, 201, 167, 0.05);

    th.el-table__cell {
      padding: 18px 0;
      font-weight: 800;
      font-size: 15px;
      letter-spacing: 0.5px;
    }

    td.el-table__cell {
      padding: 16px 0;
      font-size: 14px;
      color: #334155;
    }

    .el-table__row--striped td {
      background-color: #f0fdfa !important;
    }

    .points-cost {
      color: var(--el-color-danger);
      font-weight: 500;
    }

    .no-code {
      color: var(--el-text-color-placeholder);
    }
  }
}

.history-badge {
  padding: 6px 14px;
  border-radius: 20px;
  font-weight: 800;
  font-size: 12px;
}

.content-display {
  padding: 20px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
  white-space: pre-wrap;
  word-break: break-all;
  color: var(--el-text-color-primary);
}

@media only screen and (max-width: 768px) {
  .backpack-page {
    padding: 12px;
  }
  
  .page-header {
    margin-bottom: 16px;
    
    h2 { font-size: 20px; }
  }
  
  .backpack-tabs {
    padding: 0 10px;
    margin-bottom: 16px;
    
    :deep(.el-tabs__item) {
      padding: 0 10px;
      font-size: 13px;
    }
  }
  
  .items-grid {
    gap: 12px;
  }

  /* 移动端订单卡片样式 */
  .mobile-order-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .mobile-order-card {
    background: #fff;
    border-radius: 8px;
    padding: 12px;
    border: 1px solid #EBEEF5;
    
    .order-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
      padding-bottom: 12px;
      border-bottom: 1px solid rgba(0, 201, 167, 0.12);
      
      .goods-info {
        display: flex;
        align-items: center;
        gap: 8px;
        
        .goods-img {
          width: 32px;
          height: 32px;
          border-radius: 4px;
        }
        
        .goods-name {
          font-weight: 600;
          font-size: 14px;
          color: #333;
        }
      }
    }
    
    .order-body {
      .info-item {
        display: flex;
        align-items: center;
        margin-bottom: 6px;
        font-size: 13px;
        
        .label {
          color: #909399;
          width: 70px;
        }
        
        .value {
          color: #606266;
          
          &.points {
            color: #F56C6C;
            font-weight: 500;
          }
        }
      }
    }
  }
}

// ================================================================
// 精致游戏道具?(iOS 玻璃?
// ================================================================
:deep(.item-detail-dialog) {
  .el-dialog {
    background: transparent !important;
    box-shadow: none !important;
    border: none !important;
    .el-dialog__header { display: none; }
    .el-dialog__body { padding: 0; }
  }
}

.item-card-premium {
  position: relative;
  width: 100%;
  background: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(30px);
  -webkit-backdrop-filter: blur(30px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 32px;
  padding: 30px 20px;
  text-align: center;
  overflow: hidden;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.15);
  
  // 底部透出的紫青渐变光?
  &::before {
    content: '';
    position: absolute;
    inset: -50px;
    background: radial-gradient(circle at 20% 20%, rgba(0, 242, 255, 0.15), transparent 40%),
                radial-gradient(circle at 80% 80%, rgba(162, 155, 254, 0.2), transparent 40%);
    z-index: -1;
  }
}

/* 粒子动画 */
.glow-particles {
  position: absolute;
  inset: 0;
  pointer-events: none;
  .particle {
    position: absolute;
    width: 4px;
    height: 4px;
    background: #00F2FF;
    border-radius: 50%;
    filter: blur(1px);
    opacity: 0;
    box-shadow: 0 0 10px #00F2FF;
    animation: particleFloat 4s infinite ease-in-out;
  }
  @for $i from 1 through 6 {
    .particle:nth-child(#{$i}) {
      left: random(100) * 1%;
      top: random(100) * 1%;
      animation-delay: #{$i * 0.5}s;
    }
  }
}

@keyframes particleFloat {
  0% { transform: translateY(0) scale(0); opacity: 0; }
  50% { opacity: 0.6; }
  100% { transform: translateY(-30px) scale(1.5); opacity: 0; }
}

.card-close {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 30px;
  height: 30px;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #64748b;
  z-index: 10;
  transition: all 0.2s;
  &:hover { background: rgba(0, 0, 0, 0.1); transform: rotate(90deg); }
}

/* 顶部图标发光 */
.item-header-glow {
  position: relative;
  width: 100px;
  height: 100px;
  margin: 0 auto 20px;
  display: flex;
  align-items: center;
  justify-content: center;

  .glow-ring {
    position: absolute;
    width: 100%;
    height: 100%;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(0, 242, 255, 0.3) 0%, transparent 70%);
    animation: pulseGlow 2s infinite alternate;
  }

  .glow-ray {
    position: absolute;
    width: 140%;
    height: 140%;
    background: conic-gradient(from 0deg, transparent, rgba(0, 242, 255, 0.1), transparent 30%);
    animation: rotateRay 6s linear infinite;
  }

  .item-icon-wrapper {
    position: relative;
    width: 80px;
    height: 80px;
    background: rgba(255, 255, 255, 0.8);
    border-radius: 20px;
    padding: 12px;
    box-shadow: 0 8px 16px rgba(0, 242, 255, 0.2), inset 0 2px 4px rgba(255, 255, 255, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    
    .premium-icon {
      width: 100%;
      height: 100%;
      filter: drop-shadow(0 4px 8px rgba(0,0,0,0.1));
    }
  }
}

@keyframes pulseGlow {
  from { transform: scale(0.9); opacity: 0.5; }
  to { transform: scale(1.1); opacity: 0.8; }
}

@keyframes rotateRay {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 标题与描?*/
.item-main-info {
  margin-bottom: 20px;
  .premium-name {
    font-size: 22px;
    font-weight: 800;
    color: #1e293b;
    margin: 0 0 8px;
    letter-spacing: -0.5px;
  }
  .status-capsule {
    display: inline-block;
    padding: 2px 10px;
    border-radius: 10px;
    font-size: 11px;
    font-weight: 700;
    &.active { background: rgba(0, 242, 255, 0.1); color: #0093E9; }
    &.expired { background: #f1f5f9; color: #94a3b8; }
  }
}

.premium-meta {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 24px;
  
  .p-meta-item { font-size: 13px; color: #64748b; }
  .p-meta-divider { width: 1px; height: 10px; background: #e2e8f0; }
}

/* 操作区域：票?*/
.premium-ticket {
  position: relative;
  background: #f0fdfa;
  border: 1px solid rgba(0, 201, 167, 0.2);
  border-radius: 16px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s;
  overflow: hidden;

  &:hover {
    border-color: #00C9A7;
    background: #fff;
    .code-display-mono { color: #00C9A7; }
  }

  .ticket-inner {
    .ticket-label { font-size: 11px; color: #94a3b8; margin-bottom: 8px; }
    .code-display-mono {
      font-family: 'SF Mono', 'Roboto Mono', monospace;
      font-size: 26px;
      font-weight: 800;
      color: #334155;
      letter-spacing: 2px;
      margin-bottom: 6px;
    }
    .ticket-tip { font-size: 10px; color: #94a3b8; }
  }

  .ticket-hole {
    position: absolute;
    top: 50%;
    width: 14px;
    height: 14px;
    border-radius: 50%;
    background: #fff; // 视觉上切开
    border: 1px solid rgba(0, 201, 167, 0.2);
    transform: translateY(-50%);
    &.left { left: -8px; box-shadow: inset -2px 0 4px rgba(0,0,0,0.02); }
    &.right { right: -8px; box-shadow: inset 2px 0 4px rgba(0,0,0,0.02); }
  }
}

/* 胶囊流光按钮 */
.streamer-btn-capsule {
  position: relative;
  width: 100%;
  height: 50px;
  border-radius: 25px;
  background: linear-gradient(135deg, #00C9A7 0%, #00B38F 100%);
  border: none;
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  overflow: hidden;
  box-shadow: 0 10px 20px rgba(0, 201, 167, 0.3);
  transition: all 0.2s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 12px 24px rgba(0, 201, 167, 0.4);
  }

  &:active { transform: scale(0.97); }

  .btn-glare {
    position: absolute;
    top: -100%;
    left: -100%;
    width: 50%;
    height: 300%;
    background: linear-gradient(to right, transparent, rgba(255, 255, 255, 0.4), transparent);
    transform: rotate(45deg);
    animation: flowLight 3s infinite linear;
  }
}

@keyframes flowLight {
  from { left: -100%; }
  to { left: 200%; }
}

.used-overlay-premium {
  .stamp-box {
    border: 3px double #cbd5e1;
    color: #cbd5e1;
    font-size: 24px;
    font-weight: 900;
    padding: 10px 20px;
    display: inline-block;
    transform: rotate(-12deg);
    border-radius: 8px;
    opacity: 0.8;
  }
}

/* 保证卡片流一行?*/
// ================================================================
// PC ?(Rule 5: spacing +30%)
// ================================================================
@media (min-width: 769px) {
  .backpack-page {
    max-width: 1400px;
    margin: 0 auto;
    padding: 40px;
  }

  .backpack-hero {
    margin: 0 0 40px;
    padding: 60px 50px;
    border-radius: 40px;

    .hero-count {
      font-size: 96px;
    }
  }

  .items-grid {
    grid-template-columns: repeat(4, 1fr);
    gap: 40px;
    padding: 0;
  }

  .filter-tabs {
    padding: 0;
    margin-bottom: 40px;
    gap: 16px;
    .filter-tab {
      padding: 12px 30px;
      font-size: 16px;
    }
  }

  .order-section {
    padding: 0;
    margin-top: 80px;
  }
}
</style>
