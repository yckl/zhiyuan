<template>
  <div class="backpack-page">
    <div class="page-header">
      <div class="header-content">
        <div>
          <h2>🎒 我的背包</h2>
          <p class="subtitle">管理您的道具和兑换物品</p>
        </div>
        <el-button type="primary" @click="$router.push('/mall/index')">
          <el-icon><Shop /></el-icon>
          去购物
        </el-button>
      </div>
    </div>

    <!-- 分类标签 -->
    <el-tabs v-model="activeTab" class="backpack-tabs">
      <el-tab-pane label="全部物品" name="all" />
      <el-tab-pane label="功能卡片" name="card" />
      <el-tab-pane label="实物周边" name="physical" />
    </el-tabs>

    <!-- 物品列表 -->
    <div class="items-container" v-loading="loading">
      <div v-if="filteredItems.length > 0" class="items-grid">
        <div
          v-for="item in filteredItems"
          :key="item.id"
          class="item-card"
          :class="{ 'is-used': item.status === 1 }"
        >
          <div class="item-image">
            <el-image
              :src="item.propImage || getDefaultImage(item.propType)"
              fit="cover"
            >
              <template #error>
                <div class="image-fallback">
                  <el-icon><Box /></el-icon>
                </div>
              </template>
            </el-image>
            <div v-if="item.quantity > 1" class="quantity-badge">
              x{{ item.quantity }}
            </div>
            <el-tag
              v-if="item.status === 1"
              type="info"
              class="status-tag"
              size="small"
            >
              已使用
            </el-tag>
          </div>

          <div class="item-content">
            <h4 class="item-name">{{ item.propName }}</h4>
            <p class="item-source">
              <el-icon><Ticket /></el-icon>
              {{ getSourceText(item.source) }}
            </p>
            <p class="item-type">
              <el-tag :type="getTypeTagType(item.propType)" size="small">
                {{ getTypeText(item.propType) }}
              </el-tag>
            </p>

            <!-- 操作按钮 -->
            <div class="item-actions">
              <template v-if="item.status === 0">
                <!-- 功能卡类 -->
                <template v-if="isCardType(item.propType)">
                  <el-button
                    type="primary"
                    size="small"
                    @click="goUseCard(item)"
                  >
                    <el-icon><Promotion /></el-icon>
                    去使用
                  </el-button>
                </template>

                <!-- 实物类 - 查看核销码 -->
                <template v-else-if="item.propType === 'GOODS' || item.propType === 'PHYSICAL'">
                  <el-popover
                    placement="top"
                    :width="280"
                    trigger="click"
                  >
                    <template #reference>
                      <el-button type="warning" size="small">
                        <el-icon><Ticket /></el-icon>
                        查看核销码
                      </el-button>
                    </template>
                    <div class="pickup-popover">
                      <p class="pickup-title">{{ item.propName }}</p>
                      <div class="pickup-code-display">
                        <span class="code">{{ item.pickupCode || getOrderCode(item) }}</span>
                      </div>
                      <p class="pickup-tip">请凭此码到指定地点领取</p>
                    </div>
                  </el-popover>
                </template>

                <!-- 虚拟内容类 -->
                <template v-else-if="item.propContent">
                  <el-button
                    type="success"
                    size="small"
                    @click="showContent(item)"
                  >
                    <el-icon><View /></el-icon>
                    查看内容
                  </el-button>
                </template>
              </template>

              <template v-else>
                <span class="used-text">已使用</span>
              </template>
            </div>
          </div>
        </div>
      </div>

      <el-empty v-else :description="emptyText" :image-size="120">
        <el-button type="primary" @click="$router.push('/mall/index')">
          去商城逛逛
        </el-button>
      </el-empty>
    </div>

    <!-- 订单记录区 -->
    <div class="orders-section" v-if="orders.length > 0">
      <h3>📦 兑换记录</h3>
      <el-table :data="orders" stripe size="small">
        <el-table-column label="商品" min-width="180">
          <template #default="{ row }">
            <div class="order-goods">
              <el-image :src="row.goodsImage" style="width: 40px; height: 40px; border-radius: 4px" />
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
            <el-tag v-if="row.pickupCode" type="warning" effect="plain">{{ row.pickupCode }}</el-tag>
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

    <!-- 内容查看弹窗 -->
    <el-dialog v-model="showContentDialog" title="内容详情" width="400px">
      <div class="content-display">
        {{ currentContent }}
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Shop, Box, Ticket, Promotion, View } from '@element-plus/icons-vue'
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

const emptyText = computed(() => {
  if (activeTab.value === 'card') return '暂无功能卡片'
  if (activeTab.value === 'physical') return '暂无实物周边'
  return '背包空空如也~'
})

const filteredItems = computed(() => {
  if (activeTab.value === 'all') return backpackItems.value
  if (activeTab.value === 'card') {
    return backpackItems.value.filter(i =>
      ['MAKEUP_CARD', 'SKIP_REVIEW_CARD', 'CARD'].includes(i.propType) ||
      i.propName.includes('卡')
    )
  }
  if (activeTab.value === 'physical') {
    return backpackItems.value.filter(i =>
      i.propType === 'GOODS' || i.propType === 'PHYSICAL'
    )
  }
  return backpackItems.value
})

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

const getTypeTagType = (type: string) => {
  if (type.includes('CARD') || type === 'MAKEUP_CARD') return 'primary'
  if (type === 'GOODS' || type === 'PHYSICAL') return 'warning'
  return 'info'
}

const isCardType = (type: string) => {
  return ['MAKEUP_CARD', 'SKIP_REVIEW_CARD', 'CARD'].includes(type) ||
    type.includes('卡')
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

const showContent = (item: BackpackItem) => {
  currentContent.value = item.propContent || '暂无内容'
  showContentDialog.value = true
}

onMounted(() => {
  fetchBackpack()
  fetchOrders()
})
</script>

<style lang="scss" scoped>
.backpack-page {
  padding: 20px;
  background: var(--el-bg-color-page);
  min-height: 100vh;
}

.page-header {
  margin-bottom: 24px;

  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
  }

  h2 {
    margin: 0 0 8px;
    font-size: 24px;
    color: var(--el-text-color-primary);
  }

  .subtitle {
    margin: 0;
    color: var(--el-text-color-secondary);
  }
}

.backpack-tabs {
  background: var(--el-bg-color-overlay);
  border-radius: 12px;
  padding: 0 20px;
  margin-bottom: 20px;
}

.items-container {
  min-height: 300px;
}

.items-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;

  @media (max-width: 1200px) {
    grid-template-columns: repeat(3, 1fr);
  }
  @media (max-width: 900px) {
    grid-template-columns: repeat(2, 1fr);
  }
  @media (max-width: 500px) {
    grid-template-columns: 1fr;
  }
}

.item-card {
  background: var(--el-bg-color-overlay);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  }

  &.is-used {
    opacity: 0.6;
  }

  .item-image {
    position: relative;
    height: 160px;

    :deep(.el-image) {
      width: 100%;
      height: 100%;
    }

    .image-fallback {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      background: var(--el-fill-color-light);
      font-size: 48px;
      color: var(--el-text-color-placeholder);
    }

    .quantity-badge {
      position: absolute;
      top: 8px;
      left: 8px;
      background: var(--el-color-primary);
      color: #fff;
      padding: 2px 8px;
      border-radius: 10px;
      font-size: 12px;
      font-weight: bold;
    }

    .status-tag {
      position: absolute;
      top: 8px;
      right: 8px;
    }
  }

  .item-content {
    padding: 16px;

    .item-name {
      margin: 0 0 8px;
      font-size: 15px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }

    .item-source {
      margin: 0 0 8px;
      font-size: 12px;
      color: var(--el-text-color-secondary);
      display: flex;
      align-items: center;
      gap: 4px;
    }

    .item-type {
      margin: 0 0 12px;
    }

    .item-actions {
      .used-text {
        color: var(--el-text-color-secondary);
        font-size: 13px;
      }
    }
  }
}

.pickup-popover {
  text-align: center;

  .pickup-title {
    margin: 0 0 12px;
    font-weight: 500;
    color: var(--el-text-color-primary);
  }

  .pickup-code-display {
    background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
    padding: 16px;
    border-radius: 8px;
    margin-bottom: 12px;

    .code {
      font-size: 28px;
      font-weight: bold;
      color: #e65100;
      letter-spacing: 4px;
    }
  }

  .pickup-tip {
    margin: 0;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
}

.orders-section {
  margin-top: 40px;
  background: var(--el-bg-color-overlay);
  border-radius: 12px;
  padding: 20px;

  h3 {
    margin: 0 0 16px;
    font-size: 16px;
    color: var(--el-text-color-primary);
  }

  .order-goods {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .points-cost {
    color: var(--el-color-danger);
    font-weight: 500;
  }

  .no-code {
    color: var(--el-text-color-placeholder);
  }
}

.content-display {
  padding: 20px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
  white-space: pre-wrap;
  word-break: break-all;
  color: var(--el-text-color-primary);
}
</style>
