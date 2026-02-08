<template>
  <div class="mall-index">
    <!-- 顶部积分展示 -->
    <div class="points-header">
      <div class="points-card">
        <div class="points-info">
          <span class="label">💰 我的积分</span>
          <span class="value">{{ userPoints }}</span>
        </div>
        <div class="header-actions">
          <el-button type="primary" size="small" @click="$router.push('/mall/checkin')">
            <el-icon><Calendar /></el-icon>
            签到领积分
          </el-button>
          <el-button type="warning" size="small" @click="$router.push('/mall/backpack')">
            <el-icon><Box /></el-icon>
            我的背包
          </el-button>
        </div>
      </div>
    </div>

    <!-- 搜索和分类筛选 -->
    <div class="filter-bar">
      <div class="filter-left">
        <!-- 搜索框 -->
        <el-input
          v-model="searchKeyword"
          placeholder="搜索商品名称"
          clearable
          style="width: 200px"
          :prefix-icon="Search"
          @input="handleSearch"
          @clear="handleSearch"
        />
        <!-- 分类筛选 -->
        <el-radio-group v-model="activeCategory" @change="handleCategoryChange">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="功能卡片">功能卡片</el-radio-button>
          <el-radio-button label="装扮道具">装扮道具</el-radio-button>
          <el-radio-button label="权益兑换">权益兑换</el-radio-button>
          <el-radio-button label="学习资源">学习资源</el-radio-button>
          <el-radio-button label="实物周边">实物周边</el-radio-button>
        </el-radio-group>
      </div>
      <span class="goods-count">共 {{ total }} 件商品</span>
    </div>

    <!-- 商品列表 -->
    <div class="goods-grid" v-loading="loading">
      <div
        v-for="item in goodsList"
        :key="item.id"
        class="goods-card"
      >
        <div class="goods-image">
          <el-image
            :src="item.coverImage || getPlaceholder(item.id)"
            :alt="item.name"
            fit="cover"
            lazy
          >
            <template #placeholder>
              <div class="image-loading">
                <el-icon class="loading-icon"><Loading /></el-icon>
              </div>
            </template>
          </el-image>
          <el-tag
            :type="item.goodsType === 1 ? 'success' : 'warning'"
            class="type-tag"
            size="small"
            effect="dark"
          >
            {{ item.goodsType === 1 ? '虚拟' : '实物' }}
          </el-tag>
          <div v-if="item.stock === 0" class="sold-out-mask">
            <span>已售罄</span>
          </div>
        </div>

        <div class="goods-content">
          <h3 class="goods-name">{{ item.name }}</h3>
          <p class="goods-desc">{{ item.description || '暂无描述' }}</p>

          <div class="goods-footer">
            <div class="price-info">
              <span class="price">
                <el-icon><Coin /></el-icon>
                {{ item.pointsPrice }}
              </span>
              <span class="stock" :class="{ 'low': item.stock > 0 && item.stock < 10 }">
                库存: {{ item.stock === -1 ? '∞' : item.stock }}
              </span>
            </div>

            <el-button
              type="primary"
              size="small"
              :disabled="item.stock === 0 || userPoints < item.pointsPrice"
              :loading="buyingId === item.id"
              @click="handleBuy(item)"
            >
              {{ item.stock === 0 ? '已售罄' : (userPoints < item.pointsPrice ? '积分不足' : '立即兑换') }}
            </el-button>
          </div>
        </div>
      </div>

      <el-empty v-if="!loading && goodsList.length === 0" description="暂无商品" />
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="total"
        :page-sizes="[12, 24, 48, 96]"
        layout="total, sizes, prev, pager, next"
        background
        @size-change="fetchGoods"
        @current-change="fetchGoods"
      />
    </div>

    <!-- 购买成功弹窗 -->
    <el-dialog v-model="showSuccessDialog" :title="successTitle" width="400px" center>
      <div class="success-content">
        <el-icon class="success-icon" :style="{ color: successType === 'virtual' ? '#67c23a' : '#e6a23c' }">
          <CircleCheck />
        </el-icon>
        <p class="success-message">{{ successMessage }}</p>
        <div v-if="pickupCode" class="pickup-code-box">
          <p class="code-label">您的核销码</p>
          <p class="code-value">{{ pickupCode }}</p>
          <p class="code-tip">请凭此码到指定地点领取</p>
        </div>
      </div>
      <template #footer>
        <el-button v-if="successType === 'virtual'" type="primary" @click="goToBackpack">
          去背包查看
        </el-button>
        <el-button v-else type="primary" @click="showSuccessDialog = false">
          我知道了
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Calendar, Box, Coin, Loading, CircleCheck, Search } from '@element-plus/icons-vue'
import { request } from '@/utils/request'

interface Goods {
  id: number
  name: string
  coverImage?: string
  description?: string
  pointsPrice: number
  stock: number
  goodsType: number // 0=实物, 1=虚拟
  category?: string
}

const router = useRouter()
const loading = ref(false)
const userPoints = ref(0)
const searchKeyword = ref('')
const activeCategory = ref('')
const goodsList = ref<Goods[]>([])
const total = ref(0)
const buyingId = ref<number | null>(null)

// 分页
const pagination = reactive({
  page: 1,
  size: 12
})

// 防抖定时器
let searchTimer: ReturnType<typeof setTimeout> | null = null

// 成功弹窗状态
const showSuccessDialog = ref(false)
const successTitle = ref('')
const successMessage = ref('')
const successType = ref<'virtual' | 'physical'>('virtual')
const pickupCode = ref('')

// 占位图
const getPlaceholder = (id: number) => {
  const colors = ['667eea', 'f093fb', '4facfe', '43e97b', 'fa709a']
  return `https://via.placeholder.com/300x200/${colors[id % 5]}/fff?text=${encodeURIComponent('商品')}`
}

// 获取用户积分
const fetchUserPoints = async () => {
  try {
    const res = await request.get('/checkin/status')
    if (res.code === 200 && res.data) {
      userPoints.value = res.data.availablePoints || 0
    }
  } catch (error) {
    console.error('获取积分失败:', error)
  }
}

// 获取商品列表
const fetchGoods = async () => {
  loading.value = true
  try {
    const params: any = {
      status: 1,
      page: pagination.page,
      size: pagination.size
    }
    
    // 搜索关键词
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    
    // 分类筛选
    if (activeCategory.value) {
      params.category = activeCategory.value
    }

    const res = await request.get('/mall/goods/list', { params })
    if (res.code === 200) {
      // 前端过滤（如果后端不支持参数）
      let list = res.data?.records || res.data || []
      
      // 按关键词过滤
      if (searchKeyword.value) {
        const keyword = searchKeyword.value.toLowerCase()
        list = list.filter((item: Goods) => 
          item.name.toLowerCase().includes(keyword) ||
          (item.description || '').toLowerCase().includes(keyword)
        )
      }
      
      // 按分类字段过滤
      if (activeCategory.value) {
        list = list.filter((item: Goods) => item.category === activeCategory.value)
      }
      
      goodsList.value = list
      total.value = res.data?.total || list.length
    }
  } catch (error) {
    console.error('获取商品列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 防抖搜索
const handleSearch = () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  searchTimer = setTimeout(() => {
    pagination.page = 1
    fetchGoods()
  }, 300)
}

// 分类变更
const handleCategoryChange = () => {
  pagination.page = 1
  fetchGoods()
}

// 购买商品
const handleBuy = async (item: Goods) => {
  try {
    await ElMessageBox.confirm(
      `确定使用 ${item.pointsPrice} 积分兑换「${item.name}」吗？`,
      '确认兑换',
      {
        type: 'warning',
        confirmButtonText: '确定兑换',
        cancelButtonText: '取消'
      }
    )

    buyingId.value = item.id

    const res = await request.post('/mall/buy', {
      goodsId: item.id,
      quantity: 1
    })

    if (res.code === 200 && res.data?.success) {
      // 更新积分
      userPoints.value = res.data.remainingPoints || userPoints.value - item.pointsPrice

      // 显示成功弹窗
      if (item.goodsType === 1) {
        // 虚拟商品
        successType.value = 'virtual'
        successTitle.value = '🎉 兑换成功！'
        successMessage.value = `「${item.name}」已放入您的背包`
        pickupCode.value = ''
      } else {
        // 实物商品
        successType.value = 'physical'
        successTitle.value = '🎁 兑换成功！'
        successMessage.value = `请凭核销码领取「${item.name}」`
        pickupCode.value = res.data.pickupCode || ''
      }
      showSuccessDialog.value = true

      // 刷新商品列表（更新库存）
      fetchGoods()
    } else {
      ElMessage.error(res.message || '兑换失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  } finally {
    buyingId.value = null
  }
}

const goToBackpack = () => {
  showSuccessDialog.value = false
  router.push('/mall/backpack')
}

onMounted(() => {
  fetchUserPoints()
  fetchGoods()
})
</script>

<style lang="scss" scoped>
.mall-index {
  padding: 20px;
  background: var(--el-bg-color-page);
  min-height: 100vh;
}

.points-header {
  margin-bottom: 24px;
}

.points-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 28px 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.35);

  .points-info {
    .label {
      color: rgba(255, 255, 255, 0.85);
      font-size: 16px;
      display: block;
      margin-bottom: 8px;
    }
    .value {
      color: #fff;
      font-size: 48px;
      font-weight: bold;
      text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
    }
  }

  .header-actions {
    display: flex;
    gap: 12px;
  }
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  background: var(--el-bg-color-overlay);
  padding: 16px 20px;
  border-radius: 12px;
  flex-wrap: wrap;
  gap: 12px;

  .filter-left {
    display: flex;
    align-items: center;
    gap: 16px;
    flex-wrap: wrap;
  }

  .goods-count {
    color: var(--el-text-color-secondary);
    font-size: 14px;
  }
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;

  @media (max-width: 1400px) {
    grid-template-columns: repeat(3, 1fr);
  }
  @media (max-width: 1000px) {
    grid-template-columns: repeat(2, 1fr);
  }
  @media (max-width: 600px) {
    grid-template-columns: 1fr;
  }
}

.goods-card {
  background: var(--el-bg-color-overlay);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-8px);
    box-shadow: 0 16px 40px rgba(0, 0, 0, 0.12);
  }

  .goods-image {
    position: relative;
    height: 200px;
    overflow: hidden;

    :deep(.el-image) {
      width: 100%;
      height: 100%;
    }

    .image-loading {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      background: var(--el-fill-color-light);

      .loading-icon {
        font-size: 32px;
        color: var(--el-text-color-placeholder);
        animation: rotate 1s linear infinite;
      }
    }

    .type-tag {
      position: absolute;
      top: 12px;
      right: 12px;
    }

    .sold-out-mask {
      position: absolute;
      inset: 0;
      background: rgba(0, 0, 0, 0.6);
      display: flex;
      align-items: center;
      justify-content: center;

      span {
        color: #fff;
        font-size: 20px;
        font-weight: bold;
        padding: 8px 20px;
        border: 2px solid #fff;
        border-radius: 8px;
      }
    }
  }

  .goods-content {
    padding: 16px;

    .goods-name {
      font-size: 16px;
      font-weight: 600;
      margin: 0 0 8px;
      color: var(--el-text-color-primary);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .goods-desc {
      font-size: 13px;
      color: var(--el-text-color-secondary);
      margin: 0 0 16px;
      height: 36px;
      overflow: hidden;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }

    .goods-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .price-info {
        .price {
          display: flex;
          align-items: center;
          gap: 4px;
          color: var(--el-color-warning);
          font-size: 20px;
          font-weight: bold;
        }

        .stock {
          font-size: 12px;
          color: var(--el-text-color-secondary);
          margin-top: 4px;

          &.low {
            color: var(--el-color-danger);
          }
        }
      }
    }
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 24px 0;
}

.success-content {
  text-align: center;
  padding: 20px 0;

  .success-icon {
    font-size: 64px;
    margin-bottom: 16px;
  }

  .success-message {
    font-size: 16px;
    color: var(--el-text-color-primary);
    margin: 0 0 20px;
  }

  .pickup-code-box {
    background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
    padding: 20px;
    border-radius: 12px;

    .code-label {
      margin: 0 0 8px;
      color: var(--el-text-color-regular);
      font-size: 14px;
    }

    .code-value {
      font-size: 36px;
      font-weight: bold;
      color: #e65100;
      letter-spacing: 6px;
      margin: 0 0 8px;
    }

    .code-tip {
      margin: 0;
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }
  }
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
