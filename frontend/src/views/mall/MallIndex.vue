<template>
  <div class="mall-page" :class="{ 'is-mobile': isMobile }">
    <div class="main-container">
      <!-- ==================== 积分统计 Banner (Refactored to Card) ==================== -->
      <div class="points-header-card">
        <div class="points-hero-inner">
          <div class="points-display">
            <span class="points-label">当前积分</span>
            <span class="points-number">{{ isLoggedIn ? userPoints : '---' }}</span>
          </div>
          <div class="hero-actions">
            <div class="hero-btn" @click="router.push('/mall/checkin')">
              <el-icon><Calendar /></el-icon>
              <span>签到</span>
            </div>
            <div class="hero-btn" @click="router.push('/mall/backpack')">
              <el-icon><Box /></el-icon>
              <span>背包</span>
            </div>
          </div>
        </div>

        <!-- 游客遮罩 -->
        <div v-if="!isLoggedIn" class="guest-header-mask">
          <div class="mask-content">
            <el-icon :size="20"><Lock /></el-icon>
            <span>登录查看我的积分与背包</span>
            <el-button size="small" round class="mask-login-btn" @click="router.push({ path: '/login', query: { redirect: '/mall' } })">
              立即登录
            </el-button>
          </div>
        </div>
      </div>

      <!-- 筛选与搜索区域 -->
      <div class="mall-toolbar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索您心仪的商品"
          clearable
          :prefix-icon="Search"
          class="search-bar"
        />

        <!-- ==================== 分类胶囊导航 ==================== -->
        <div class="category-bar hide-scrollbar">
          <div
            v-for="cat in categories"
            :key="cat"
            class="category-item"
            :class="{ active: activeCategory === cat }"
            @click="handleCategoryChange(cat)"
          >
            {{ cat || '全部' }}
          </div>
        </div>
      </div>

      <!-- ==================== 商品展示区域 ==================== -->
      <div class="goods-section" v-loading="loading">
        <!-- 骨架屏 -->
        <div v-if="loading && goodsList.length === 0" class="goods-grid">
          <div v-for="i in 8" :key="i" class="skeleton-card">
            <el-skeleton animated>
              <template #template>
                <el-skeleton-item variant="image" style="height: 160px" />
                <div style="padding: 10px">
                  <el-skeleton-item variant="h3" style="width: 70%" />
                  <el-skeleton-item variant="text" style="width: 40%; margin-top: 10px" />
                </div>
              </template>
            </el-skeleton>
          </div>
        </div>

      <!-- 商品卡片 -->
      <TransitionGroup v-else name="card-fade" tag="div" class="goods-grid">
        <div
          v-for="(item, idx) in displayGoods"
          :key="item.id"
          class="goods-card"
          :style="{ animationDelay: `${idx * 0.04}s` }"
          @click="goToDetail(item.id)"
        >
          <!-- 商品卡片 -->
          <div class="goods-img-wrapper">
            <el-image :src="item.coverImage || getPlaceholder(item.id)" fit="cover" class="goods-img" lazy>
              <template #placeholder>
                <div class="img-loading"><el-icon class="is-loading"><Loading /></el-icon></div>
              </template>
            </el-image>
            <div class="goods-gradient"></div>
            <el-tag
              :type="item.goodsType === 1 ? 'success' : 'warning'"
              size="small" effect="dark"
              class="type-badge"
            >{{ item.goodsType === 1 ? '虚拟' : '实物' }}</el-tag>
            <div v-if="item.stock === 0" class="sold-out-overlay">
              <span>已售</span>
            </div>
          </div>

          <!-- 商品信息 -->
          <div class="goods-info">
            <h4 class="goods-name">{{ item.name }}</h4>
            <div class="goods-bottom">
              <span class="goods-price">
                <el-icon><Coin /></el-icon>
                {{ item.pointsPrice }}
              </span>
              <button class="exchange-btn" @click.stop="openBuySheet(item)">
                {{ item.stock === 0 ? '已售罄' : '兑换' }}
              </button>
            </div>
          </div>
        </div>
      </TransitionGroup>

      <el-empty v-if="!loading && displayGoods.length === 0" description="暂无商品" :image-size="100" />
    </div>

    <!-- PC 端分页 -->
      <div class="pagination-wrapper" v-if="!isMobile && total > pagination.size">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="total"
          :page-sizes="[12, 24, 48]"
          layout="total, prev, pager, next"
          background
          @size-change="fetchGoods"
          @current-change="fetchGoods"
        />
      </div>
    </div> <!-- end main-container -->

    <!-- ==================== 底部确认面板 (Bottom Sheet) ==================== -->
    <el-drawer
      v-model="showBuySheet"
      direction="btt"
      size="auto"
      :show-close="false"
      class="buy-drawer"
      :close-on-click-modal="true"
    >
      <div class="buy-sheet" v-if="selectedGoods">
        <!-- 商品摘要 -->
        <div class="buy-header">
          <el-image :src="selectedGoods.coverImage || getPlaceholder(selectedGoods.id)" fit="cover" class="buy-thumb">
  <template #error>
    <img :src="'/default-cover.jpg'" style="width:100%;height:100%;object-fit:cover"/>
  </template>
</el-image>
          <div class="buy-meta">
            <h3 class="buy-name">{{ selectedGoods.name }}</h3>
            <p class="buy-desc">{{ selectedGoods.description || '暂无描述' }}</p>
            <span class="buy-price"><el-icon style="color: #00BFA6"><Coin /></el-icon> {{ selectedGoods.pointsPrice }} 积分</span>
          </div>
          <el-icon class="buy-close" @click="showBuySheet = false"><Close /></el-icon>
        </div>

        <!-- 数量选择 -->
          <div class="buy-quantity">
            <span class="qty-label">数量</span>
            <div class="premium-qty-control">
              <button type="button" class="qty-btn" @click="buyQuantity > 1 && buyQuantity--">
                <el-icon><Minus /></el-icon>
              </button>
              <input type="number" v-model.number="buyQuantity" class="qty-input" readonly />
              <button type="button" class="qty-btn" @click="buyQuantity < maxBuyQty && buyQuantity++">
                <el-icon><Plus /></el-icon>
              </button>
            </div>
          </div>

        <!-- 小计 -->
        <div class="buy-total">
          <span>合计</span>
          <span class="total-price">
            <el-icon style="color: #00BFA6"><Coin /></el-icon>
            {{ selectedGoods.pointsPrice * buyQuantity }}
            <span class="balance-hint" :class="{ insufficient: userPoints < selectedGoods.pointsPrice * buyQuantity }">
              (余额: {{ userPoints }})
            </span>
          </span>
        </div>

        <!-- 确认按钮 -->
        <button
          class="buy-confirm-btn"
          :class="{ disabled: !canBuy }"
          :disabled="!canBuy"
          @click="handleBuy"
        >
          {{ buyBtnText }}
        </button>
      </div>
    </el-drawer>

    <!-- ==================== 兑换成功弹窗 ==================== -->
    <el-dialog v-model="showSuccessDialog" :title="successTitle" width="360px" center>
      <div class="success-body">
        <el-icon class="success-icon" :style="{ color: successType === 'virtual' ? '#67C23A' : '#E6A23C' }">
          <CircleCheck />
        </el-icon>
        <p class="success-msg">{{ successMessage }}</p>
        <div v-if="pickupCode" class="pickup-box">
          <p class="code-label">核销</p>
          <p class="code-value">{{ pickupCode }}</p>
          <p class="code-tip">请凭此码到指定地点领</p>
        </div>
      </div>
      <template #footer>
        <el-button v-if="successType === 'virtual'" type="primary" round @click="goToBackpack">去背包查看</el-button>
        <el-button v-else type="primary" round @click="showSuccessDialog = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Calendar, Box, Coin, Loading, CircleCheck, Close, Search, Minus, Plus, Lock
} from '@element-plus/icons-vue'
import { request } from '@/utils/request'

interface Goods {
  id: number; name: string; coverImage?: string; description?: string
  pointsPrice: number; stock: number; goodsType: number; category?: string
}

const router = useRouter()
const route = useRoute()

// ================== 响应式 ==================
const windowWidth = ref(window.innerWidth)
const isMobile = computed(() => windowWidth.value < 768)
const isLoggedIn = computed(() => !!localStorage.getItem('token'))
onMounted(() => window.addEventListener('resize', () => { windowWidth.value = window.innerWidth }))

// ================== 状态 ==================
const loading = ref(false)
const userPoints = ref(0)
const activeCategory = ref('')
const goodsList = ref<Goods[]>([])
const total = ref(0)
const pagination = reactive({ page: 1, size: 24 })
const searchKeyword = ref('')

// 分类列表
const categories = ['', '功能卡片', '装扮道具', '权益兑换', '学习资源', '实物周边']

// ==================== 购买面板 ====================
const showBuySheet = ref(false)
const selectedGoods = ref<Goods | null>(null)
const buyQuantity = ref(1)

const maxBuyQty = computed(() => {
  if (!selectedGoods.value) return 1
  if (selectedGoods.value.stock === -1) return 99
  return Math.max(1, selectedGoods.value.stock)
})

const canBuy = computed(() => {
  if (!selectedGoods.value) return false
  if (selectedGoods.value.stock === 0) return false
  return userPoints.value >= selectedGoods.value.pointsPrice * buyQuantity.value
})

const buyBtnText = computed(() => {
  if (!selectedGoods.value) return '立即兑换'
  if (selectedGoods.value.stock === 0) return '已售罄'
  if (userPoints.value < selectedGoods.value.pointsPrice * buyQuantity.value) return '积分不足'
  return '立即兑换'
})

// 成功弹窗
const showSuccessDialog = ref(false)
const successTitle = ref('')
const successMessage = ref('')
const successType = ref<'virtual' | 'physical'>('virtual')
const pickupCode = ref('')

// ================== 占位图 ==================
const getPlaceholder = (id: number) => {
  const colors = ['667eea', 'f093fb', '4facfe', '43e97b', 'fa709a']
  return `https://via.placeholder.com/300x300/${colors[id % 5]}/fff?text=${encodeURIComponent('商品')}`
}

// ================== API ==================
const fetchUserPoints = async () => {
  if (!isLoggedIn.value) return
  try {
    const res = await request.get('/checkin/status')
    if (res.code === 200 && res.data) userPoints.value = res.data.availablePoints || 0
  } catch (e) { console.error('获取积分失败:', e) }
}

const fetchGoods = async () => {
  loading.value = true
  try {
    const params: any = { status: 1, page: pagination.page, size: pagination.size }
    if (activeCategory.value) params.category = activeCategory.value
    if (searchKeyword.value) params.name = searchKeyword.value

    const res = await request.get('/mall/goods/list', { params })
    if (res.code === 200) {
      let list = res.data?.records || res.data || []
      // 后端不支持时前端兜底过滤 (可选，但建议后端实现)
      if (searchKeyword.value && (!res.data?.records)) {
        list = list.filter((g: any) => g.name.toLowerCase().includes(searchKeyword.value.toLowerCase()))
      }
      goodsList.value = list
      total.value = res.data?.total || list.length
    }
  } catch (e) { console.error('获取商品列表失败:', e) }
  finally { loading.value = false }
}

import { watch } from 'vue'
let searchTimer: any = null
watch(searchKeyword, () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    pagination.page = 1
    fetchGoods()
  }, 400)
})

const handleCategoryChange = (cat: string) => {
  activeCategory.value = cat
  pagination.page = 1
  fetchGoods()
}

const displayGoods = computed(() => {
  let list = goodsList.value
  // 前端兜底过滤，防止后端接口分类参数无效
  if (activeCategory.value) {
    list = list.filter(g => g.category === activeCategory.value)
  }
  return list
})

// ================== 购买流程 ==================
const openBuySheet = (item: Goods) => {
  if (!isLoggedIn.value) {
    ElMessage.info('请先登录后进行兑换')
    // 记忆回跳：把 autoBuy 参数放进 详情页 redirect 里面
    const redirectUrl = `/mall/item/${item.id}?autoBuy=true`
    router.push({ 
      path: '/login', 
      query: { 
        redirect: redirectUrl
      } 
    })
    return
  }
  selectedGoods.value = item
  buyQuantity.value = 1
  showBuySheet.value = true
}

const goToDetail = (id: number) => {
  router.push(`/mall/item/${id}`)
}

const handleBuy = async () => {
  if (!selectedGoods.value || !canBuy.value) return

  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请登录后再参与换购')
    router.push({ path: '/login', query: { redirect: '/mall/index' } })
    return
  }

  const item = selectedGoods.value
  
  try {
    const res = await request.post('/mall/buy', { goodsId: item.id, quantity: buyQuantity.value })

    if (res.code === 200 && res.data?.success) {
      showBuySheet.value = false
      userPoints.value = res.data.remainingPoints || userPoints.value - item.pointsPrice * buyQuantity.value

      if (item.goodsType === 1) {
        successType.value = 'virtual'
        successTitle.value = '🎉 兑换成功'
        successMessage.value = `「${item.name}」已放入您的背包`
        pickupCode.value = ''
      } else {
        successType.value = 'physical'
        successTitle.value = '🎁 兑换成功'
        successMessage.value = `请凭核销码领取「${item.name}」`
        pickupCode.value = res.data.pickupCode || ''
      }
      showSuccessDialog.value = true
      fetchGoods()
    } else {
      ElMessage.error(res.message || '兑换失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const goToBackpack = () => { showSuccessDialog.value = false; router.push('/mall/backpack') }

onMounted(async () => { 
  fetchUserPoints(); 
  await fetchGoods(); 

  // 处理从登录回跳的 autoBuy
  const autoBuyId = route.query.autoBuy
  if (autoBuyId && isLoggedIn.value) {
    const item = goodsList.value.find(g => g.id === Number(autoBuyId))
    if (item) {
      setTimeout(() => {
        openBuySheet(item)
        // 清理 url 参数
        router.replace({ path: route.path, query: { ...route.query, autoBuy: undefined } })
      }, 500)
    }
  }
})
</script>

<style lang="scss" scoped>
.mall-page {
  min-height: 100vh;
  background: var(--app-bg);
}

.main-container {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  padding: 20px 0;
  display: flex;
  flex-direction: column;
}

// ================================================================
// 积分统计 Banner (Refactored to Card)
// ================================================================
.points-header-card {
  background: linear-gradient(135deg, #00C9A7 0%, #00B38F 100%) !important;
  margin: 0 16px 24px;
  padding: 36px 24px;
  border-radius: 28px;
  box-shadow: 0 12px 32px rgba(0, 201, 167, 0.25);
  position: relative;
  overflow: hidden;
  border: none !important;

  &::before {
    content: '';
    position: absolute;
    top: -20%;
    right: -10%;
    width: 200px;
    height: 200px;
    background: radial-gradient(circle, rgba(255,255,255,0.2) 0%, transparent 70%);
    border-radius: 50%;
  }
}

.points-hero-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  z-index: 1;
}

.points-display {
  flex: 1;
  .points-label {
    display: block;
    font-size: 14px;
    color: rgba(255, 255, 255, 0.9);
    margin-bottom: 8px;
    font-weight: 700;
  }

  .points-number {
    font-size: 56px;
    font-weight: 900;
    color: #fff;
    text-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
    letter-spacing: -2px;
    line-height: 1;
  }
}

.hero-actions {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
  align-items: center;
}

.hero-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 76px;
  height: 76px;
  background: rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(16px);
  border: 1.5px solid rgba(255, 255, 255, 0.35);
  border-radius: 18px;
  color: #fff;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);

  .el-icon { 
    font-size: 26px;
    filter: drop-shadow(0 2px 4px rgba(0,0,0,0.1));
  }

  span {
    font-size: 13px;
    font-weight: 700;
    opacity: 0.95;
  }

  &:hover {
    background: rgba(255, 255, 255, 0.3);
    transform: translateY(-4px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
  }

  &:active { transform: scale(0.92); }

  @media (max-width: 768px) {
    width: 60px;
    height: 60px;
    border-radius: 12px;
    gap: 2px;
    border-width: 1px;
    .el-icon { font-size: 20px; }
    span { font-size: 10px; }
  }
}

@media (max-width: 768px) {
  .points-hero-inner {
    padding: 0 10px;
    gap: 12px;
    justify-content: space-between;
  }
  .points-number {
    font-size: 38px !important;
  }
  .hero-actions {
    gap: 10px;
  }
}

@keyframes heroPulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(255, 255, 255, 0.2); }
  50% { box-shadow: 0 0 0 10px rgba(255, 255, 255, 0); }
}

.guest-header-mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 191, 166, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  animation: fadeIn 0.4s ease-out;

  .mask-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 10px;
    color: #fff;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);

    span {
      font-size: 14px;
      font-weight: 600;
      letter-spacing: 0.5px;
    }

    .mask-login-btn {
      background: #fff;
      color: #00BFA6;
      border: none;
      font-weight: 700;
      padding: 8px 20px;
      box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
      transition: all 0.3s;

      &:hover {
        transform: scale(1.05);
        box-shadow: 0 6px 20px rgba(0, 191, 166, 0.3);
      }
    }
  }
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

// ================================================================
// 搜索和分类区域
// ================================================================
.mall-toolbar {
  margin: 0 16px 12px;
}

.search-bar {
  margin-bottom: 16px;

  :deep(.el-input__wrapper) {
    height: 48px;
    border-radius: 24px;
    background: #fff;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03) !important;
    border: 1px solid rgba(0, 0, 0, 0.04);
    
    &.is-focus {
      border-color: #00C9A7 !important;
      box-shadow: 0 0 0 1px #00C9A7 !important;
    }
  }
  :deep(.el-input__prefix-icon) { color: #00C9A7; }
}

// ================================================================
// 分类导航
// ================================================================
.category-bar {
  display: flex;
  justify-content: center;
  gap: 12px;
  padding: 12px 16px;
  overflow-x: auto;
  white-space: nowrap;
  background: transparent;

  &::-webkit-scrollbar { display: none; }
  
  @media (max-width: 768px) {
    justify-content: flex-start;
    padding: 8px 16px;
  }
}

  .category-item {
    padding: 8px 24px;
    border-radius: 50px;
    font-size: 14px;
    font-weight: 600;
    color: #00BFA6;
    cursor: pointer;
    background: #fff;
    border: 1px solid rgba(0, 201, 167, 0.4);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    &:hover {
      background: #f0fdfa;
      border-color: #00C9A7;
      transform: translateY(-2px);
    }

    &.active {
      background: linear-gradient(135deg, #00C9A7 0%, #00B38F 100%);
      color: #fff !important;
      font-weight: 800;
      box-shadow: 0 6px 16px rgba(0, 201, 167, 0.3);
      border-color: transparent;
      transform: scale(1.05);
    }

    @media (max-width: 768px) {
      padding: 6px 18px;
      font-size: 13px;
      &.active { transform: scale(1.02); }
    }
  }

// ================================================================
// 商品网格
// ================================================================
.goods-section {
  padding: 0 16px 32px;
  min-height: 300px;
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  padding: 10px 0;
}

.goods-card {
  background: #fff;
  border-radius: 20px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
  box-shadow: 0 8px 24px rgba(0, 201, 167, 0.08);
  border: 1px solid rgba(0, 0, 0, 0.03);

  &:hover {
    transform: translateY(-8px);
    box-shadow: 0 16px 40px rgba(0, 201, 167, 0.18);
    border-color: rgba(0, 201, 167, 0.2);
  }

  &:active { transform: scale(0.96); }
}

.goods-img-wrapper {
  position: relative;
  height: 220px;
  overflow: hidden;
  background: #f8fafc;

  .goods-img {
    width: 100%;
    height: 100%;
  }

  .type-badge {
    position: absolute;
    top: 8px;
    left: 8px;
  }

  .sold-out-overlay {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.55);
    display: flex;
    align-items: center;
    justify-content: center;

    span {
      color: #fff;
      font-size: 16px;
      font-weight: 700;
      padding: 6px 16px;
      border: 2px solid #fff;
      border-radius: 6px;
    }
  }

  .img-loading {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f5f5;
    color: #ccc;
    font-size: 24px;
  }
}

.goods-gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(to bottom, transparent 50%, rgba(0, 0, 0, 0.2) 100%);
  pointer-events: none;
}

.skeleton-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}

.goods-info {
  padding: 16px;
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 12px;

  .goods-name {
    margin: 0;
    font-size: 16px;
    font-weight: 800;
    color: #1e293b;
    line-height: 1.4;
    height: 44px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .goods-bottom {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 10px;
  }

  .goods-price {
    display: flex;
    align-items: center;
    gap: 3px;
    color: #00BFA6;
    font-size: 20px;
    font-weight: 800;

    .el-icon { font-size: 16px; }
  }
}

.exchange-btn {
  border: none;
  background: linear-gradient(135deg, #00C9A7, #00B38F);
  color: #fff;
  padding: 6px 18px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.25s;
  white-space: nowrap;

  &:hover {
    transform: scale(1.08);
    box-shadow: 0 4px 12px rgba(0, 201, 167, 0.35);
  }

  &:active { transform: scale(0.95); }
}

// ================================================================
// 底部购买面板
// ================================================================
:deep(.buy-drawer) {
  border-radius: 16px 16px 0 0 !important;
  .el-drawer__header { display: none !important; }
  .el-drawer__body { padding: 0 !important; }
}

.buy-sheet {
  padding: 20px 16px;
  padding-bottom: calc(16px + env(safe-area-inset-bottom));
}

.buy-header {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  position: relative;

  .buy-thumb {
    width: 80px;
    height: 80px;
    border-radius: 10px;
    flex-shrink: 0;
  }

  .buy-meta {
    flex: 1;
    min-width: 0;

    .buy-name {
      font-size: 16px;
      font-weight: 600;
      color: #333;
      margin: 0 0 4px;
    }

    .buy-desc {
      font-size: 12px;
      color: #999;
      margin: 0 0 8px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .buy-price {
      display: flex;
      align-items: center;
      gap: 4px;
      color: #00BFA6;
      font-size: 20px;
      font-weight: 800;
    }
  }

  .buy-close {
    position: absolute;
    top: 0;
    right: 0;
    font-size: 20px;
    color: #ccc;
    cursor: pointer;
  }
}

.buy-quantity {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  border-top: 1px solid rgba(0, 201, 167, 0.12);
  border-bottom: 1px solid rgba(0, 201, 167, 0.12);

  .qty-label {
    font-size: 16px;
    color: #1e293b;
    font-weight: 700;
  }
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
    &::-webkit-inner-spin-button,
    &::-webkit-outer-spin-button {
      -webkit-appearance: none;
      margin: 0;
    }
  }
}

.buy-total {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 0;

  span:first-child { font-size: 14px; color: #333; }

  .total-price {
    display: flex;
    align-items: center;
    gap: 4px;
    color: #00BFA6;
    font-size: 24px;
    font-weight: 800;

    .balance-hint {
      font-size: 12px;
      color: #999;
      font-weight: 400;
      margin-left: 4px;

      &.insufficient { color: #f87171; }
    }
  }
}

.buy-confirm-btn {
  width: 100%;
  height: 52px;
  border: none;
  border-radius: 26px;
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, #00C9A7, #00B38F);
  box-shadow: 0 6px 16px rgba(0, 201, 167, 0.3);
  cursor: pointer;
  transition: all 0.3s;
  margin-top: 12px;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(0, 201, 167, 0.4);
  }

  &:active { transform: scale(0.97); }
  &.disabled { background: #e5e7eb; color: #9ca3af; box-shadow: none; cursor: not-allowed; }
}

// ================================================================
// 成功弹窗
// ================================================================
.success-body {
  text-align: center;
  padding: 16px 0;

  .success-icon { font-size: 56px; margin-bottom: 12px; }
  .success-msg { font-size: 15px; color: #333; margin: 0 0 16px; }
}

.pickup-box {
  background: linear-gradient(135deg, rgba(0, 201, 167, 0.05), rgba(0, 201, 167, 0.1));
  padding: 16px;
  border-radius: 12px;

  .code-label { margin: 0 0 6px; color: #666; font-size: 13px; }
  .code-value { font-size: 32px; font-weight: 800; color: #E65100; letter-spacing: 4px; margin: 0 0 6px; }
  .code-tip { margin: 0; font-size: 12px; color: #999; }
}

// ================================================================
// 分页
// =================================================================
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px;
}

// ================================================================
// PC 适配
// ================================================================
// ================================================================
// PC 端适配 (Rule 5: spacing +30%)
// ================================================================
@media (min-width: 769px) {
  .main-container {
    max-width: 1400px;
    padding: 40px 40px;
  }

  .points-header-card {
    margin: 0 0 40px;
  }

  .mall-toolbar {
    margin: 0 24px 20px;
  }

  .goods-section {
    padding: 0 24px 40px;
  }

  .goods-grid {
    grid-template-columns: repeat(4, 1fr);
    gap: 40px;
    padding: 20px 0;
  }

  .goods-card {
    border-radius: 24px;
    box-shadow: 0 12px 32px rgba(0, 201, 167, 0.08);
    &:hover {
      transform: translateY(-10px);
      box-shadow: 0 20px 48px rgba(0, 201, 167, 0.2);
    }
  }
}

// ================================================================
// TransitionGroup
// ================================================================
@keyframes cardIn {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

.goods-card { animation: cardIn 0.35s ease-out both; }

.card-fade-enter-active { transition: all 0.3s ease-out; }
.card-fade-leave-active { transition: all 0.2s ease-in; }
.card-fade-enter-from { opacity: 0; transform: translateY(12px); }
.card-fade-leave-to { opacity: 0; transform: scale(0.96); }
.card-fade-move { transition: transform 0.3s ease; }
</style>
