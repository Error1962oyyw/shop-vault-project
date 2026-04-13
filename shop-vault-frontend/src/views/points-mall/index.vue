<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Coin, Present, Ticket, Medal, Timer, ZoomIn, Warning } from '@element-plus/icons-vue'
import UserLayout from '@/components/layout/UserLayout.vue'
import { getPointsProducts, exchangePointsProduct } from '@/api/vip'
import { getProfile } from '@/api/user'
import type { PointsProduct } from '@/api/vip'

const userInfo = ref({ points: 0 })
const products = ref<PointsProduct[]>([])
const loading = ref(false)
const exchangeLoading = ref<number | null>(null)
const activeTab = ref('all')
const previewVisible = ref(false)
const previewImage = ref('')
const previewImages = ref<string[]>([])
const previewIndex = ref(0)

const filteredProducts = computed(() => {
  if (activeTab.value === 'all') return products.value
  if (activeTab.value === 'vip') {
    return products.value.filter(p => p.type === 3 || p.type === 4)
  }
  return products.value.filter(p => p.type === parseInt(activeTab.value))
})

const productTypes = [
  { value: 'all', label: '全部', icon: Present },
  { value: '1', label: '小商品', icon: Present },
  { value: '2', label: '优惠券', icon: Ticket },
  { value: 'vip', label: 'VIP', icon: Medal }
]

const fetchUserInfo = async () => {
  try {
    const res = await getProfile()
    userInfo.value.points = res.points || 0
  } catch (error) {
    console.error('获取用户信息失败', error)
  }
}

const fetchProducts = async () => {
  loading.value = true
  try {
    products.value = await getPointsProducts()
  } catch (error) {
    console.error('获取积分商品失败', error)
    ElMessage.error('获取积分商品失败')
  } finally {
    loading.value = false
  }
}

const getProductImages = (product: PointsProduct): string[] => {
  if (product.images) {
    try {
      const imgs = JSON.parse(product.images)
      return Array.isArray(imgs) ? imgs : [imgs]
    } catch {
      return []
    }
  }
  if (product.mainImage) return [product.mainImage]
  if (product.image) return [product.image]
  return []
}

const handleImageClick = (product: PointsProduct, index: number = 0) => {
  const images = getProductImages(product)
  if (images.length > 0) {
    previewImages.value = images
    previewIndex.value = index
    previewImage.value = images[index]
    previewVisible.value = true
  }
}

const handlePreviewChange = (delta: number) => {
  const newIndex = previewIndex.value + delta
  if (newIndex >= 0 && newIndex < previewImages.value.length) {
    previewIndex.value = newIndex
    previewImage.value = previewImages.value[newIndex]
  }
}

const handleExchange = async (product: PointsProduct) => {
  if (userInfo.value.points < product.pointsCost) {
    ElMessage.warning(`积分不足，还需 ${product.pointsCost - userInfo.value.points} 积分`)
    return
  }
  
  const isPhysicalProduct = product.type === 1
  
  try {
    const extraNotice = isPhysicalProduct ? '\n\n注意：积分兑换实物商品不支持退换货等售后服务。' : ''

    await ElMessageBox.confirm(
      `兑换商品：${product.name}\n所需积分：${product.pointsCost}${extraNotice}`,
      '确认兑换',
      {
        confirmButtonText: '确认兑换',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch {
    return
  }
  
  exchangeLoading.value = product.id
  try {
    const result = await exchangePointsProduct(product.id)
    ElMessage.success('兑换成功')
    await fetchUserInfo()
    await fetchProducts()
    
    if (result.startsWith('VIP_')) {
      ElMessage.success('VIP会员已激活，享受98折优惠！')
    }
  } catch (error: any) {
    const msg = error?.response?.data?.msg || error?.message || '兑换失败'
    ElMessage.error(msg)
  } finally {
    exchangeLoading.value = null
  }
}

const getProductTypeLabel = (type: number) => {
  const labels: Record<number, string> = {
    1: '小商品',
    2: '优惠券',
    3: 'VIP月卡',
    4: 'VIP年卡'
  }
  return labels[type] || '其他'
}

const getProductTypeColor = (type: number): 'success' | 'warning' | 'primary' | 'danger' | 'info' => {
  const colors: Record<number, 'success' | 'warning' | 'primary' | 'danger'> = {
    1: 'success',
    2: 'warning',
    3: 'primary',
    4: 'danger'
  }
  return colors[type] || 'info'
}

const formatPrice = (price: number) => {
  return price.toFixed(2)
}

onMounted(() => {
  fetchUserInfo()
  fetchProducts()
})
</script>

<template>
  <UserLayout>
    <div class="points-mall-page animate-fade-in">
      <div class="page-container">
        <div class="page-header">
          <div class="header-bg">
            <div class="points-display">
              <div class="points-value">
                <Coin class="coin-icon" />
                <span class="points-number">{{ userInfo.points }}</span>
              </div>
              <div class="points-label">可用积分</div>
            </div>
          </div>
          <div class="header-content">
            <div class="header-left">
              <h1 class="header-title">
                <Present class="title-icon" />
                积分商城
              </h1>
              <p class="header-subtitle">用积分兑换精彩好礼</p>
            </div>
          </div>
        </div>

        <div class="notice-banner">
          <Warning class="notice-icon" />
          <span>积分兑换商品不支持退换货等售后服务，请确认后再兑换</span>
        </div>

        <div class="filter-section">
          <div class="filter-tabs">
            <div 
              v-for="type in productTypes" 
              :key="type.value"
              :class="['filter-tab', { active: activeTab === type.value }]"
              @click="activeTab = type.value"
            >
              <component :is="type.icon" class="tab-icon" />
              {{ type.label }}
            </div>
          </div>
        </div>

        <div v-loading="loading" class="products-grid">
          <div 
            v-for="product in filteredProducts" 
            :key="product.id"
            class="product-card"
          >
            <div class="product-image" @click="handleImageClick(product, 0)">
              <img v-if="product.mainImage || product.image" :src="product.mainImage || product.image" :alt="product.name" />
              <div v-else class="product-placeholder">
                <Present class="placeholder-icon" />
              </div>
              <div v-if="getProductImages(product).length > 1" class="image-count">
                <ZoomIn class="zoom-icon" />
                {{ getProductImages(product).length }}张
              </div>
              <el-tag 
                :type="getProductTypeColor(product.type)" 
                size="small" 
                class="product-tag"
              >
                {{ getProductTypeLabel(product.type) }}
              </el-tag>
            </div>
            <div v-if="getProductImages(product).length > 1" class="thumbnail-list">
              <div 
                v-for="(img, idx) in getProductImages(product).slice(0, 4)" 
                :key="idx"
                :class="['thumbnail-item', { active: idx === 0 }]"
                @click="handleImageClick(product, idx)"
              >
                <img :src="img" :alt="`${product.name} - ${idx + 1}`" />
              </div>
              <div v-if="getProductImages(product).length > 4" class="thumbnail-more">
                +{{ getProductImages(product).length - 4 }}
              </div>
            </div>
            <div class="product-info">
              <h3 class="product-name">{{ product.name }}</h3>
              <p class="product-desc">{{ product.description }}</p>
              <div class="product-meta">
                <div class="product-price">
                  <span class="original-price">¥{{ formatPrice(product.originalPrice) }}</span>
                </div>
                <div class="product-stock">
                  <Timer class="stock-icon" />
                  库存: {{ product.stock }}
                </div>
              </div>
              <div class="product-footer">
                <div class="points-cost">
                  <Coin class="cost-icon" />
                  <span class="cost-value">{{ product.pointsCost }}</span>
                  <span class="cost-label">积分</span>
                </div>
                <el-button 
                  type="warning" 
                  :loading="exchangeLoading === product.id"
                  :disabled="product.stock <= 0 || userInfo.points < product.pointsCost"
                  class="exchange-btn"
                  @click="handleExchange(product)"
                >
                  {{ product.stock <= 0 ? '已兑完' : userInfo.points < product.pointsCost ? '积分不足' : '立即兑换' }}
                </el-button>
              </div>
              <div v-if="product.type === 1" class="after-sales-notice">
                <Warning class="notice-icon-small" />
                积分兑换商品不支持售后
              </div>
            </div>
          </div>
        </div>

        <el-empty v-if="!loading && filteredProducts.length === 0" description="暂无商品" />
      </div>
    </div>

    <el-dialog 
      v-model="previewVisible" 
      title="图片预览" 
      width="800px"
      class="preview-dialog"
      destroy-on-close
    >
      <div class="preview-container">
        <el-icon class="preview-arrow prev" @click="handlePreviewChange(-1)">
          <svg viewBox="0 0 1024 1024"><path fill="currentColor" d="M604.8 533.248L384 754.4l45.248 45.248L695.296 533.6 429.248 267.552 384 312.8l220.8 220.448z"/></svg>
        </el-icon>
        <img :src="previewImage" class="preview-image" alt="商品预览图" />
        <el-icon class="preview-arrow next" @click="handlePreviewChange(1)">
          <svg viewBox="0 0 1024 1024"><path fill="currentColor" d="M604.8 533.248L384 754.4l45.248 45.248L695.296 533.6 429.248 267.552 384 312.8l220.8 220.448z"/></svg>
        </el-icon>
      </div>
      <div class="preview-indicator">
        {{ previewIndex + 1 }} / {{ previewImages.length }}
      </div>
    </el-dialog>
  </UserLayout>
</template>

<style scoped>
.points-mall-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f7f8fa 0%, #fff7e6 50%, #f0f5ff 100%);
  padding-bottom: 40px;
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.page-header {
  position: relative;
  border-radius: 20px;
  overflow: hidden;
  margin-bottom: 24px;
  margin-top: 24px;
}

.header-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #fa8c16 0%, #ffc53d 50%, #ffd591 100%);
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 40px;
}

.header-content {
  position: relative;
  display: flex;
  justify-content: flex-start;
  align-items: center;
  padding: 40px;
  color: #fff;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.header-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0;
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12px;
}

.title-icon {
  width: 28px;
  height: 28px;
  flex-shrink: 0;
}

.title-icon :deep(svg) {
  width: 28px;
  height: 28px;
}

.header-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
}

.points-display {
  position: relative;
  z-index: 2;
  text-align: center;
  background: rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  padding: 20px 32px;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  min-width: 160px;
  color: #fff;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.points-value {
  font-size: 36px;
  font-weight: 800;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: 8px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.points-number {
  font-size: 36px;
  font-weight: 800;
  line-height: 1;
  color: #fff;
}

.coin-icon {
  width: 24px;
  height: 24px;
  flex-shrink: 0;
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.2));
}

.coin-icon :deep(svg) {
  width: 24px;
  height: 24px;
}

.points-label {
  font-size: 14px;
  font-weight: 500;
  margin-top: 8px;
  color: #fff;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.25);
  letter-spacing: 0.5px;
}

.notice-banner {
  display: flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(135deg, #fff7e6 0%, #ffe7ba 100%);
  border: 1px solid #ffd591;
  border-radius: 12px;
  padding: 12px 20px;
  margin-bottom: 24px;
  color: #ad6800;
  font-size: 14px;
}

.notice-banner .notice-icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}

.filter-section {
  margin-bottom: 24px;
}

.filter-tabs {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  gap: 12px;
  background: #fff;
  padding: 8px;
  border-radius: 12px;
  box-shadow: var(--shadow-sm);
}

.filter-tab {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: #6b7280;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.filter-tab:hover {
  background: #f3f4f6;
  color: #1f2937;
}

.filter-tab.active {
  background: linear-gradient(135deg, #fa8c16 0%, #ffc53d 100%);
  color: #fff;
}

.tab-icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.tab-icon :deep(svg) {
  width: 16px;
  height: 16px;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.product-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  transition: all 0.3s ease;
  position: relative;
  z-index: 1;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  z-index: 10;
}

.product-image {
  position: relative;
  height: 180px;
  background: #f9fafb;
  cursor: pointer;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.product-image:hover img {
  transform: scale(1.05);
}

.product-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #fff7e6 0%, #ffd591 100%);
}

.placeholder-icon {
  width: 48px;
  height: 48px;
  color: #fa8c16;
  opacity: 0.5;
}

.placeholder-icon :deep(svg) {
  width: 48px;
  height: 48px;
}

.image-count {
  position: absolute;
  bottom: 8px;
  right: 8px;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.zoom-icon {
  width: 14px;
  height: 14px;
}

.product-tag {
  position: absolute;
  top: 12px;
  left: 12px;
}

.thumbnail-list {
  display: flex;
  gap: 8px;
  padding: 8px 12px;
  background: #f9fafb;
  border-top: 1px solid #f3f4f6;
}

.thumbnail-item {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s ease;
}

.thumbnail-item:hover,
.thumbnail-item.active {
  border-color: #fa8c16;
}

.thumbnail-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumbnail-more {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #6b7280;
}

.product-info {
  padding: 16px;
}

.product-name {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-desc {
  font-size: 13px;
  color: #6b7280;
  margin: 0 0 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
  height: 39px;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.original-price {
  font-size: 14px;
  color: #9ca3af;
  text-decoration: line-through;
}

.product-stock {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #6b7280;
}

.stock-icon {
  width: 14px;
  height: 14px;
  flex-shrink: 0;
}

.stock-icon :deep(svg) {
  width: 14px;
  height: 14px;
}

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f3f4f6;
}

.points-cost {
  display: flex;
  flex-direction: row;
  align-items: baseline;
  gap: 4px;
}

.cost-icon {
  width: 16px;
  height: 16px;
  color: #fa8c16;
  flex-shrink: 0;
}

.cost-icon :deep(svg) {
  width: 16px;
  height: 16px;
}

.cost-value {
  font-size: 20px;
  font-weight: 700;
  color: #fa8c16;
}

.cost-label {
  font-size: 12px;
  color: #9ca3af;
}

.exchange-btn {
  border-radius: 8px;
  font-weight: 600;
  font-size: 13px;
  background: linear-gradient(135deg, #fa8c16 0%, #ffc53d 100%);
  border: none;
}

.exchange-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #d46b08 0%, #d48806 100%);
}

.after-sales-notice {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed #f3f4f6;
  font-size: 12px;
  color: #f56c6c;
}

.notice-icon-small {
  width: 14px;
  height: 14px;
}

.preview-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.preview-container {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  background: #1f2937;
  min-height: 400px;
}

.preview-image {
  max-width: 100%;
  max-height: 500px;
  object-fit: contain;
}

.preview-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #fff;
  font-size: 20px;
  transition: all 0.2s ease;
}

.preview-arrow:hover {
  background: rgba(255, 255, 255, 0.3);
}

.preview-arrow.prev {
  left: 16px;
}

.preview-arrow.next {
  right: 16px;
}

.preview-indicator {
  text-align: center;
  padding: 12px;
  background: #1f2937;
  color: #fff;
  font-size: 14px;
}

@media (max-width: 1200px) {
  .products-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 900px) {
  .products-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .header-content {
    flex-direction: column;
    text-align: center;
    gap: 20px;
    padding: 24px;
  }

  .header-title {
    font-size: 24px;
  }

  .filter-tabs {
    flex-wrap: wrap;
  }

  .filter-tab {
    flex: 1;
    min-width: calc(50% - 6px);
    justify-content: center;
  }

  .products-grid {
    grid-template-columns: 1fr;
  }
}
</style>
