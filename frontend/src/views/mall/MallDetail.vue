<template>
  <div class="mall-detail-page" :class="{ 'is-mobile': isMobile }" v-loading="loading">
    <!-- 移动端沉浸式导航 -->
    <header v-if="isMobile" class="immersive-header" :class="{ scrolled: headerScrolled }">
      <el-icon class="header-back" @click="router.back()"><ArrowLeft /></el-icon>
      <span class="header-title" v-show="headerScrolled">{{ goods.name }}</span>
      <el-icon class="header-share" @click="handleShare"><Share /></el-icon>
    </header>

    <div class="detail-scroll-container" ref="scrollRef">
      <!-- 移动端 Banner -->
      <div v-if="isMobile" class="mobile-banner">
        <el-image :src="goods.coverImage || getPlaceholder(goods.id)" fit="cover" class="banner-img" />
        <div class="banner-overlay"></div>
        <el-tag :type="goods.goodsType === 1 ? 'success' : 'warning'" effect="dark" class="mobile-type-badge">
          {{ goods.goodsType === 1 ? '虚拟商品' : '实物周边' }}
        </el-tag>
      </div>

      <div class="main-content">
        <el-row :gutter="40" class="detail-row">
          <!-- PC 端左侧：图片展示 -->
          <el-col v-if="!isMobile" :span="12">
            <div class="pc-image-card">
              <el-image :src="goods.coverImage || getPlaceholder(goods.id)" fit="cover" class="pc-main-img">
                 <template #placeholder>
                  <div class="img-loading"><el-icon class="is-loading"><Loading /></el-icon></div>
                </template>
              </el-image>
            </div>
          </el-col>

          <!-- 右侧：详情信息 -->
          <el-col :span="isMobile ? 24 : 12">
            <div class="info-container">
              <div class="info-header">
                <span class="category-pill">{{ goods.category || '精选优选' }}</span>
                <h1 class="goods-title">{{ goods.name }}</h1>
                <div class="price-section">
                  <div class="current-price">
                    <el-icon><Coin /></el-icon>
                    <span class="price-value">{{ goods.pointsPrice }}</span>
                    <span class="price-label">积分</span>
                  </div>
                  <div class="stock-status">
                    <span class="status-dot" :class="{ 'out-of-stock': goods.stock === 0 }"></span>
                    {{ goods.stock === 0 ? '已售罄' : (goods.stock === -1 ? '库存充足' : `剩余 ${goods.stock} 件`) }}
                  </div>
                </div>
              </div>

              <el-divider />

              <div class="detail-section">
                <h3 class="section-title">商品简介</h3>
                <p class="goods-desc">{{ goods.description || '该商品暂无详细介绍，快来兑换体验吧！' }}</p>
              </div>

              <div class="detail-section">
                <h3 class="section-title">兑换规则</h3>
                <ul class="rule-list">
                  <li>虚拟商品兑换后直接发放至“我的背包”</li>
                  <li>实物商品需凭核销码到学校团委办公室领取</li>
                  <li>积分一经扣除，非质量问题不予退还</li>
                </ul>
              </div>

              <!-- PC 端操作区 -->
              <div v-if="!isMobile" class="pc-action-box">
                <div class="quantity-row">
                  <span>兑换数量</span>
                  <div class="premium-qty-control">
                    <button type="button" class="qty-btn" @click="buyQuantity > 1 && buyQuantity--" :disabled="buyQuantity <= 1">
                      <el-icon><Minus /></el-icon>
                    </button>
                    <input type="number" v-model.number="buyQuantity" class="qty-input" readonly />
                    <button type="button" class="qty-btn" @click="buyQuantity < maxBuyQty && buyQuantity++" :disabled="buyQuantity >= maxBuyQty">
                      <el-icon><Plus /></el-icon>
                    </button>
                  </div>
                </div>
                <div class="total-row">
                  <span>合计消耗</span>
                  <span class="total-points">
                    <el-icon><Coin /></el-icon>
                    {{ goods.pointsPrice * buyQuantity }}
                  </span>
                </div>
                <el-button 
                  type="primary" 
                  class="pc-exchange-btn" 
                  :disabled="goods.stock === 0"
                  @click="handleExchange"
                >
                  {{ buyBtnText }}
                </el-button>
              </div>
            </div>
          </el-col>
        </el-row>

        <!-- 推荐商品 -->
        <div class="recommend-area">
           <h3 class="area-title">猜你喜欢</h3>
           <div class="recommend-grid">
              <div v-for="item in recommendations" :key="item.id" class="rec-card" @click="goToItem(item.id)">
                <el-image :src="item.coverImage || getPlaceholder(item.id)" fit="cover" />
                <div class="rec-info">
                  <p class="rec-name">{{ item.name }}</p>
                  <p class="rec-price"><el-icon><Coin /></el-icon> {{ item.pointsPrice }}</p>
                </div>
              </div>
           </div>
        </div>
      </div>
    </div>

    <!-- 移动端底部固定栏 -->
    <div v-if="isMobile" class="mobile-bottom-bar">
      <div class="bar-left">
        <div class="total-info">
          <span class="label">合计:</span>
          <span class="val"><el-icon><Coin /></el-icon> {{ goods.pointsPrice * buyQuantity }}</span>
        </div>
      </div>
      <button 
        class="mobile-buy-btn" 
        :class="{ disabled: goods.stock === 0 }"
        :disabled="goods.stock === 0"
        @click="openBuySheet"
      >
        {{ buyBtnText }}
      </button>
    </div>

    <!-- 底部购买面板 (移动端触发) -->
    <el-drawer
      v-model="showBuySheet"
      direction="btt"
      size="auto"
      :show-close="false"
      class="buy-drawer"
    >
      <div class="buy-sheet" v-if="goods">
        <div class="buy-header">
          <el-image :src="goods.coverImage || getPlaceholder(goods.id)" fit="cover" class="buy-thumb" />
          <div class="buy-meta">
            <p class="buy-name">{{ goods.name }}</p>
            <p class="buy-price"><el-icon><Coin /></el-icon> {{ goods.pointsPrice }}</p>
          </div>
          <el-icon class="buy-close" @click="showBuySheet = false"><Close /></el-icon>
        </div>
        <div class="buy-quantity">
          <span class="qty-label">兑换数量</span>
          <div class="premium-qty-control">
            <button type="button" class="qty-btn" @click="buyQuantity > 1 && buyQuantity--" :disabled="buyQuantity <= 1">
              <el-icon><Minus /></el-icon>
            </button>
            <input type="number" v-model.number="buyQuantity" class="qty-input" readonly />
            <button type="button" class="qty-btn" @click="buyQuantity < maxBuyQty && buyQuantity++" :disabled="buyQuantity >= maxBuyQty">
              <el-icon><Plus /></el-icon>
            </button>
          </div>
        </div>
        <div class="buy-total">
          <span>预计消耗积分</span>
          <span class="total-price">
            <el-icon><Coin /></el-icon>
            {{ goods.pointsPrice * buyQuantity }}
          </span>
        </div>
        <el-button type="primary" class="buy-confirm-btn" @click="handleConfirmExchange">
           确认兑换
        </el-button>
      </div>
    </el-drawer>

    <!-- 成功弹窗 -->
    <el-dialog v-model="showSuccessDialog" :title="successTitle" width="360px" center>
      <div class="success-body">
        <el-icon class="success-icon" color="#67C23A"><CircleCheck /></el-icon>
        <p class="success-msg">{{ successMessage }}</p>
      </div>
      <template #footer>
        <el-button type="primary" round @click="goToBackpack">去背包查看</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ArrowLeft, Share, Coin, Loading, Close, CircleCheck
} from '@element-plus/icons-vue'
import { request } from '@/utils/request'

interface Goods {
  id: number; name: string; coverImage?: string; description?: string
  pointsPrice: number; stock: number; goodsType: number; category?: string
}

const route = useRoute()
const router = useRouter()
const goodsId = computed(() => Number(route.params.id))

// 状态
const loading = ref(false)
const goods = ref<Goods>({ id: 0, name: '加载中...', pointsPrice: 0, stock: 0, goodsType: 1 })
const recommendations = ref<Goods[]>([])
const buyQuantity = ref(1)
const userPoints = ref(0)
const showBuySheet = ref(false)
const showSuccessDialog = ref(false)
const successTitle = ref('兑换成功')
const successMessage = ref('')
const headerScrolled = ref(false)

// 响应式
const windowWidth = ref(window.innerWidth)
const isMobile = computed(() => windowWidth.value < 768)
const isLoggedIn = computed(() => !!localStorage.getItem('token'))

const maxBuyQty = computed(() => {
  if (goods.value.stock === -1) return 99
  return Math.max(1, goods.value.stock)
})

const buyBtnText = computed(() => {
  if (!isLoggedIn.value) return '登录并兑换'
  if (goods.value.stock === 0) return '已售罄'
  if (userPoints.value < goods.value.pointsPrice * buyQuantity.value) return '积分不足'
  return '立即兑换'
})

// 方法
const getPlaceholder = (id: number) => {
  const colors = ['667eea', 'f093fb', '4facfe', '43e97b', 'fa709a']
  return `https://via.placeholder.com/600x600/${colors[id % 5]}/fff?text=Goods`
}

const fetchGoods = async () => {
  loading.value = true
  try {
    const res = await request.get(`/mall/goods/${goodsId.value}`)
    if (res.code === 200 && res.data) {
      goods.value = res.data
    }
  } catch (e) {
    ElMessage.error('获取商品详情失败')
  } finally {
    loading.value = false
  }
}

const fetchUserPoints = async () => {
  if (!isLoggedIn.value) return
  try {
    const res = await request.get('/checkin/status')
    if (res.code === 200 && res.data) userPoints.value = res.data.availablePoints || 0
  } catch (e) {}
}

const fetchRecommendations = async () => {
  try {
    const res = await request.get('/mall/goods/list', { params: { size: 4 } })
    if (res.code === 200) {
      const list = res.data?.records || res.data || []
      recommendations.value = list.filter((i: any) => i.id !== goodsId.value)
    }
  } catch (e) {}
}

const handleExchange = () => {
  if (!isLoggedIn.value) {
    ElMessage.info('请先登录后进行兑换')
    // 记忆回跳: 带着 autoBuy=true 标记回来
    const redirectUrl = `${route.fullPath}${route.fullPath.includes('?') ? '&' : '?'}autoBuy=true`
    router.push({ path: '/login', query: { redirect: redirectUrl } })
    return
  }
  
  if (isMobile.value) {
    showBuySheet.value = true
  } else {
    // PC 端增加二次确认，防止误触扣除积分
    ElMessageBox.confirm(
      `确认支出 ${goods.value.pointsPrice * buyQuantity.value} 积分兑换「${goods.value.name}」吗？`,
      '确认兑换',
      {
        confirmButtonText: '确定兑换',
        cancelButtonText: '取消',
        type: 'warning',
        roundButton: true
      }
    ).then(() => {
      handleConfirmExchange()
    }).catch(() => {})
  }
}

const openBuySheet = () => {
  if (!isLoggedIn.value) {
    handleExchange()
    return
  }
  showBuySheet.value = true
}

const handleConfirmExchange = async () => {
  if (userPoints.value < goods.value.pointsPrice * buyQuantity.value) {
    ElMessage.warning('积分不足')
    return
  }

  try {
    const res = await request.post('/mall/buy', { 
      goodsId: goods.value.id, 
      quantity: buyQuantity.value 
    })
    if (res.code === 200 && res.data?.success) {
      successMessage.value = `「${goods.value.name}」兑换成功！可在背包查看`
      showSuccessDialog.value = true
      showBuySheet.value = false
      fetchUserPoints()
      fetchGoods()
      
      // 清理 autoBuy 标记 if present
      if (route.query.autoBuy) {
        router.replace({ path: route.path, query: { ...route.query, autoBuy: undefined } })
      }
    } else {
      ElMessage.error(res.message || '兑换失败')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  }
}

const handleShare = () => {
  import('@/utils/clipboard').then(({ copyToClipboard }) => {copyToClipboard(window.location.href, '详情链接已复制')})
}

const goToItem = (id: number) => {
  router.push(`/mall/item/${id}`)
}

const goToBackpack = () => {
  showSuccessDialog.value = false
  router.push('/mall/backpack')
}

const handleScroll = () => {
  headerScrolled.value = window.scrollY > 100
}

onMounted(async () => {
  window.addEventListener('resize', () => { windowWidth.value = window.innerWidth })
  window.addEventListener('scroll', handleScroll)
  
  await fetchGoods()
  fetchUserPoints()
  fetchRecommendations()
  
  // 处理记忆回跳
  if (route.query.autoBuy === 'true' && isLoggedIn.value) {
    setTimeout(() => {
      handleExchange()
    }, 500)
  }
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style lang="scss" scoped>
.mall-detail-page {
  min-height: 100vh;
  background: #f8fafc;
  
  &.is-mobile {
    background: #fff;
    padding-bottom: 80px;
  }
}

// 沉浸式导航
.immersive-header {
  position: fixed;
  top: 0; left: 0; right: 0;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  z-index: 1000;
  transition: all 0.3s;
  
  &.scrolled {
    background: rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(10px);
    border-bottom: 1px solid rgba(0,0,0,0.05);
    .header-back, .header-share { color: #333; background: transparent; }
  }
  
  .header-back, .header-share {
    width: 34px; height: 34px;
    background: rgba(0,0,0,0.2);
    border-radius: 50%;
    color: #fff;
    display: flex; align-items: center; justify-content: center;
    font-size: 20px;
  }
  
  .header-title {
    font-size: 16px; font-weight: 600; color: #333;
    max-width: 60%; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  }
}

// 内容容器
.detail-container {
  max-width: 1100px;
  margin: 0 auto;
  padding: 40px 20px;
}

.pc-image-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.05);
  .pc-main-img {
    width: 100%; aspect-ratio: 1; border-radius: 12px;
  }
}

.info-container {
  padding: 10px 0;
  
  .category-pill {
    display: inline-block;
    padding: 4px 12px;
    background: rgba(0, 191, 166, 0.1);
    color: #00BFA6;
    border-radius: 14px;
    font-size: 12px;
    font-weight: 600;
    margin-bottom: 12px;
  }
  
  .goods-title {
    font-size: 32px; font-weight: 800; color: #1e293b; margin: 0 0 20px;
  }
  
  .price-section {
    display: flex; align-items: flex-end; justify-content: space-between;
    margin-bottom: 15px;
    
    .current-price {
      display: flex; align-items: center; gap: 6px;
      color: #00BFA6;
      
      .el-icon { font-size: 24px; }
      .price-value { font-size: 40px; font-weight: 800; line-height: 1; }
      .price-label { font-size: 14px; margin-left: 2px; color: #64748b; font-weight: 500; }
    }
    
    .stock-status {
      font-size: 14px; color: #64748b; display: flex; align-items: center; gap: 6px;
      
      .status-dot {
        width: 8px; height: 8px; border-radius: 50%; background: #10b981;
        &.out-of-stock { background: #ef4444; }
      }
    }
  }
}

.detail-section {
  margin: 25px 0;
  
  .section-title {
    font-size: 16px; font-weight: 700; color: #1e293b; margin-bottom: 10px;
  }
  
  .goods-desc {
    color: #64748b; line-height: 1.7; font-size: 15px;
  }
  
  .rule-list {
    padding-left: 18px; color: #94a3b8; font-size: 13px; line-height: 2;
  }
}

// PC Action Box
.pc-action-box {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  margin-top: 30px;
  border: 1px solid rgba(0,0,0,0.05);
  box-shadow: 0 4px 15px rgba(0,0,0,0.02);
  
  .quantity-row, .total-row {
    display: flex; align-items: center; justify-content: space-between;
    margin-bottom: 16px;
    span:first-child { color: #64748b; font-size: 14px; }
  }
  
  .total-row .total-points {
    color: #1e293b; font-size: 20px; font-weight: 700; display: flex; align-items: center; gap: 4px;
    .el-icon { color: #00BFA6; }
  }
  
  .pc-exchange-btn {
    width: 100%; height: 54px; font-size: 18px; font-weight: 700; border-radius: 12px;
    background: linear-gradient(135deg, #00C9A7, #00B38F); border: none;
    &:hover { transform: translateY(-2px); box-shadow: 0 8px 20px rgba(0, 191, 166, 0.3); }
  }
}

// 移动端专用样式
.mobile-banner {
  position: relative;
  width: 100%; aspect-ratio: 1;
  .banner-img { width: 100%; height: 100%; }
  .banner-overlay {
    position: absolute; bottom: 0; left: 0; right: 0; height: 100px;
    background: linear-gradient(to top, rgba(255,255,255,1), transparent);
  }
  .mobile-type-badge {
    position: absolute; top: 16px; right: 16px; border: none;
  }
}

.main-content {
  padding: 0 20px 40px;
  .is-mobile & { padding-top: 20px; }
}

.mobile-bottom-bar {
  position: fixed; bottom: 0; left: 0; right: 0;
  height: 72px; background: #fff; border-top: 1px solid #f1f5f9;
  display: flex; align-items: center; padding: 0 16px; gap: 16px;
  z-index: 1000; padding-bottom: env(safe-area-inset-bottom);
  
  .bar-left {
    flex: 1;
    .total-info {
      display: flex; align-items: center; gap: 4px;
      .label { color: #64748b; font-size: 12px; }
      .val { color: #00BFA6; font-size: 20px; font-weight: 800; display: flex; align-items: center; gap: 2px; }
    }
  }
  
  .mobile-buy-btn {
    flex: 2; height: 48px; border: none; border-radius: 24px;
    background: linear-gradient(135deg, #00C9A7, #00B38F); color: #fff;
    font-size: 16px; font-weight: 700;
    &.disabled { background: #cbd5e1; }
  }
}

// 推荐列表
.recommend-area {
  margin-top: 60px;
  .area-title { font-size: 20px; font-weight: 700; color: #1e293b; margin-bottom: 20px; }
  .recommend-grid {
    display: grid; grid-template-columns: repeat(auto-fill, minmax(140px, 1fr)); gap: 16px;
  }
  .rec-card {
    background: #fff; border-radius: 12px; overflow: hidden; cursor: pointer; border: 1px solid #f1f5f9;
    .el-image { width: 100%; aspect-ratio: 1; }
    .rec-info { padding: 10px; }
    .rec-name { font-size: 13px; font-weight: 600; margin: 0 0 4px; color: #334155; }
    .rec-price { font-size: 14px; font-weight: 700; color: #00BFA6; margin: 0; display: flex; align-items: center; gap: 2px; }
  }
}

// Drawer & Dialog
:deep(.buy-drawer) {
  border-radius: 20px 20px 0 0;
  .el-drawer__body { padding: 0; }
}
.buy-sheet {
  padding: 24px;
  .buy-header {
    display: flex; gap: 16px; margin-bottom: 24px; position: relative;
    .buy-thumb { width: 80px; height: 80px; border-radius: 10px; }
    .buy-meta {
      flex: 1;
      .buy-name { font-size: 18px; font-weight: 700; margin: 4px 0 8px; }
      .buy-price { color: #00BFA6; font-size: 22px; font-weight: 800; }
    }
    .buy-close { position: absolute; top: 0; right: 0; font-size: 24px; color: #94a3b8; }
  }
  .buy-quantity {
    display: flex; align-items: center; justify-content: space-between;
    margin: 20px 0; padding: 15px 0; border-top: 1px solid #f1f5f9; border-bottom: 1px solid #f1f5f9;
  }
  .buy-total {
    display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px;
    span:first-child { color: #64748b; font-size: 14px; }
    .total-price { font-size: 24px; font-weight: 800; color: #00BFA6; display: flex; align-items: center; gap: 4px; }
  }
  .buy-confirm-btn { width: 100%; height: 50px; border-radius: 25px; font-size: 16px; font-weight: 700; border: none; }
}

.success-body {
  display: flex; flex-direction: column; align-items: center; padding: 20px 0;
  .success-icon { font-size: 64px; margin-bottom: 16px; }
  .success-msg { font-size: 16px; font-weight: 600; color: #334155; text-align: center; }
}

// Premium Quantity Control
.premium-qty-control {
  display: flex;
  align-items: center;
  background: #f1f5f9;
  border-radius: 12px;
  padding: 4px;
  border: 1px solid #e2e8f0;

  .qty-btn {
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    border: none;
    background: #fff;
    border-radius: 8px;
    color: #1e293b;
    cursor: pointer;
    transition: all 0.2s;
    box-shadow: 0 2px 4px rgba(0,0,0,0.05);

    &:hover:not(:disabled) {
      background: #f8fafc;
      color: #00BFA6;
      transform: translateY(-1px);
    }

    &:active:not(:disabled) {
      transform: scale(0.95);
    }

    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
      background: transparent;
      box-shadow: none;
    }
  }

  .qty-input {
    width: 50px;
    border: none;
    background: transparent;
    text-align: center;
    font-size: 16px;
    font-weight: 700;
    color: #1e293b;
    outline: none;
    // Hide arrows
    &::-webkit-inner-spin-button,
    &::-webkit-outer-spin-button {
      -webkit-appearance: none;
      margin: 0;
    }
  }
}

@media (max-width: 767px) {
  .immersive-header { .header-back, .header-share { background: rgba(0,0,0,0.3) !important; } }
  
  .premium-qty-control {
    .qty-btn { width: 36px; height: 36px; }
    .qty-input { width: 60px; font-size: 18px; }
  }
}
</style>
