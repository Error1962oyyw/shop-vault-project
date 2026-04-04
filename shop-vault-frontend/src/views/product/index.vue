<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import UserLayout from '@/components/layout/UserLayout.vue'
import { FavoriteButton } from '@/components/common'
import { getProductDetail, getCommentList } from '@/api/product'
import { addToCart } from '@/api/cart'
import { getApplicableCoupons, getCurrentMemberDay } from '@/api/marketing'
import { getProductSkus } from '@/api/admin'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import type { Product, Comment, PageResult, ProductSku, UserCoupon, MemberDay } from '@/types/api'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const loading = ref(true)
const product = ref<Product | null>(null)
const currentImage = ref('')
const quantity = ref(1)
const activeTab = ref('detail')

const comments = ref<Comment[]>([])
const commentLoading = ref(false)
const commentPagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const skus = ref<ProductSku[]>([])
const selectedSku = ref<ProductSku | null>(null)
const specValues = ref<Record<string, string>>({})

const applicableCoupons = ref<UserCoupon[]>([])
const memberDay = ref<MemberDay | null>(null)

const productId = computed(() => Number(route.params.id))

const currentPrice = computed(() => {
  if (selectedSku.value) {
    return selectedSku.value.price
  }
  return product.value?.price || 0
})

const currentStock = computed(() => {
  if (selectedSku.value) {
    return selectedSku.value.stock
  }
  return product.value?.stock || 0
})

const discountPrice = computed(() => {
  if (memberDay.value && memberDay.value.status === 1) {
    return currentPrice.value * memberDay.value.discountRate
  }
  return currentPrice.value
})

const availableSpecs = computed(() => {
  if (skus.value.length === 0) return []
  
  const specs: { name: string; values: string[] }[] = []
  const specMap = new Map<string, Set<string>>()
  
  skus.value.forEach(sku => {
    try {
      const specObj = JSON.parse(sku.specJson)
      Object.entries(specObj).forEach(([key, value]) => {
        if (!specMap.has(key)) {
          specMap.set(key, new Set())
        }
        specMap.get(key)!.add(String(value))
      })
    } catch {
    }
  })
  
  specMap.forEach((values, name) => {
    specs.push({ name, values: Array.from(values) })
  })
  
  return specs
})

const fetchProduct = async () => {
  loading.value = true
  try {
    product.value = await getProductDetail(productId.value)
    currentImage.value = product.value.mainImage
    await Promise.all([
      fetchComments(),
      fetchSkus(),
      fetchCoupons(),
      fetchMemberDay()
    ])
  } catch (error) {
    console.error('获取商品详情失败', error)
    ElMessage.error('商品不存在')
    router.push('/products')
  } finally {
    loading.value = false
  }
}

const fetchComments = async () => {
  commentLoading.value = true
  try {
    const res: PageResult<Comment> = await getCommentList(productId.value, {
      pageNum: commentPagination.current,
      pageSize: commentPagination.size
    })
    comments.value = res.records
    commentPagination.total = res.total
  } catch (error) {
    console.error('获取评价失败', error)
  } finally {
    commentLoading.value = false
  }
}

const handleCommentPageChange = (val: number) => {
  commentPagination.current = val
  fetchComments()
}

const fetchSkus = async () => {
  try {
    skus.value = await getProductSkus(productId.value)
    if (skus.value.length > 0 && !selectedSku.value) {
      selectedSku.value = skus.value[0]
      try {
        const specObj = JSON.parse(skus.value[0].specJson)
        Object.entries(specObj).forEach(([key, value]) => {
          specValues.value[key] = String(value)
        })
      } catch {
      }
    }
  } catch (error) {
    console.error('获取SKU失败', error)
  }
}

const fetchCoupons = async () => {
  if (!userStore.token) return
  try {
    applicableCoupons.value = await getApplicableCoupons({
      orderAmount: product.value?.price || 0,
      productId: productId.value,
      categoryId: product.value?.categoryId
    })
  } catch (error) {
    console.error('获取优惠券失败', error)
  }
}

const fetchMemberDay = async () => {
  try {
    memberDay.value = await getCurrentMemberDay()
  } catch (error) {
    console.error('获取会员日活动失败', error)
  }
}

const selectSpecValue = (specName: string, value: string) => {
  specValues.value[specName] = value
  
  const matchedSku = skus.value.find(sku => {
    try {
      const specObj = JSON.parse(sku.specJson)
      return Object.entries(specValues.value).every(
        ([key, val]) => String(specObj[key]) === val
      )
    } catch {
      return false
    }
  })
  
  if (matchedSku) {
    selectedSku.value = matchedSku
    if (matchedSku.image) {
      currentImage.value = matchedSku.image
    }
  }
}

const handleAddToCart = async () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  if (skus.value.length > 0 && !selectedSku.value) {
    ElMessage.warning('请选择商品规格')
    return
  }
  
  try {
    await addToCart({ 
      productId: productId.value, 
      quantity: quantity.value,
      skuId: selectedSku.value?.id
    })
    ElMessage.success('已加入购物车')
    cartStore.fetchCartList()
  } catch (error) {
    console.error('加入购物车失败', error)
  }
}

const handleBuyNow = async () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  if (skus.value.length > 0 && !selectedSku.value) {
    ElMessage.warning('请选择商品规格')
    return
  }

  const productName = product.value?.name || '商品'
  const price = currentPrice.value
  const qty = quantity.value

  try {
    await ElMessageBox.confirm(
      `商品名称：${productName}\n\n单价：¥${price.toFixed(2)}\n数量：${qty}\n\n合计：¥${(price * qty).toFixed(2)}`,
      '确认购买',
      {
        confirmButtonText: '确认购买',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch {
    return
  }

  try {
    await addToCart({
      productId: productId.value,
      quantity: quantity.value,
      skuId: selectedSku.value?.id
    })
    router.push('/checkout')
  } catch (error) {
    console.error('立即购买失败', error)
  }
}

const formatCouponValue = (coupon: UserCoupon) => {
  if (coupon.type === 2 && coupon.discount) {
    return `${coupon.discount}折`
  }
  return `¥${coupon.value}`
}

onMounted(() => {
  fetchProduct()
})
</script>

<template>
  <UserLayout>
    <div v-loading="loading" class="product-detail-page animate-fade-in">
      <template v-if="product">
        <div class="page-container">
          <div class="product-card">
            <div class="product-layout">
              <div class="product-gallery">
                <div class="main-image">
                  <img 
                    :src="currentImage || product.mainImage" 
                    :alt="product.name"
                    class="image"
                  />
                </div>
                <div class="thumbnails">
                  <div 
                    v-for="(img, index) in [product.mainImage, ...product.images]" 
                    :key="index"
                    class="thumbnail"
                    :class="{ active: currentImage === img }"
                    @click="currentImage = img"
                  >
                    <img :src="img" :alt="`${product.name} ${index}`" class="thumbnail-image" />
                  </div>
                  <div 
                    v-for="sku in skus.filter(s => s.image)" 
                    :key="sku.id"
                    class="thumbnail"
                    :class="{ active: selectedSku?.id === sku.id }"
                    @click="currentImage = sku.image"
                  >
                    <img :src="sku.image" :alt="sku.skuName" class="thumbnail-image" />
                  </div>
                </div>
              </div>

              <div class="product-info">
                <h1 class="product-title">{{ product.name }}</h1>
                
                <div class="price-section">
                  <div class="price-row">
                    <span class="current-price">¥{{ discountPrice.toFixed(2) }}</span>
                    <span v-if="selectedSku && selectedSku.originalPrice > currentPrice" class="original-price">
                      ¥{{ selectedSku.originalPrice.toFixed(2) }}
                    </span>
                    <span v-else-if="product.originalPrice > product.price" class="original-price">
                      ¥{{ product.originalPrice.toFixed(2) }}
                    </span>
                    <el-tag v-if="memberDay && memberDay.status === 1" type="danger" size="small">
                      会员日{{ memberDay.discountRate }}折
                    </el-tag>
                    <el-tag v-else-if="product.originalPrice > product.price || (selectedSku && selectedSku.originalPrice > currentPrice)" type="danger" size="small">
                      限时优惠
                    </el-tag>
                  </div>
                  
                  <div v-if="memberDay && memberDay.status === 1 && discountPrice < currentPrice" class="saving-tip">
                    <el-icon><Present /></el-icon>
                    会员日专享价，已为您节省 ¥{{ (currentPrice - discountPrice).toFixed(2) }}
                  </div>
                </div>

                <div v-if="availableSpecs.length > 0" class="spec-section">
                  <div 
                    v-for="spec in availableSpecs" 
                    :key="spec.name"
                    class="spec-item"
                  >
                    <span class="spec-label">{{ spec.name }}</span>
                    <div class="spec-values">
                      <div 
                        v-for="value in spec.values" 
                        :key="value"
                        class="spec-value"
                        :class="{ active: specValues[spec.name] === value }"
                        @click="selectSpecValue(spec.name, value)"
                      >
                        {{ value }}
                      </div>
                    </div>
                  </div>
                </div>

                <div class="info-section">
                  <div class="info-item">
                    <span class="info-label">销量</span>
                    <span class="info-value">{{ product.sales }} 件</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">库存</span>
                    <span class="info-value">{{ currentStock }} 件</span>
                    <span v-if="selectedSku" class="info-tip">
                      (SKU: {{ selectedSku.skuCode }})
                    </span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">分类</span>
                    <el-tag>{{ product.categoryName }}</el-tag>
                  </div>
                </div>

                <div v-if="applicableCoupons.length > 0" class="coupon-section">
                  <div class="coupon-header">
                    <el-icon class="coupon-icon"><Ticket /></el-icon>
                    <span class="coupon-title">可用优惠券</span>
                  </div>
                  <div class="coupon-list">
                    <el-tooltip 
                      v-for="coupon in applicableCoupons.slice(0, 3)" 
                      :key="coupon.id"
                      :content="`满${coupon.minAmount}减${coupon.value}`"
                      placement="top"
                    >
                      <el-tag type="warning" size="small">
                        {{ formatCouponValue(coupon) }}
                      </el-tag>
                    </el-tooltip>
                    <el-tag v-if="applicableCoupons.length > 3" type="info" size="small">
                      +{{ applicableCoupons.length - 3 }}张
                    </el-tag>
                  </div>
                </div>

                <div class="quantity-section">
                  <span class="info-label">数量</span>
                  <el-input-number 
                    v-model="quantity" 
                    :min="1" 
                    :max="currentStock"
                    size="large"
                  />
                  <span class="info-tip">库存 {{ currentStock }} 件</span>
                </div>

                <div class="action-section">
                  <el-button 
                    type="primary" 
                    size="large" 
                    class="buy-now-btn"
                    :disabled="currentStock === 0"
                    @click="handleBuyNow"
                  >
                    立即购买
                  </el-button>
                  <el-button 
                    type="warning" 
                    size="large" 
                    class="add-cart-btn"
                    :disabled="currentStock === 0"
                    @click="handleAddToCart"
                  >
                    <el-icon><ShoppingCart /></el-icon>
                    加入购物车
                  </el-button>
                  <FavoriteButton 
                    :product-id="product.id" 
                    :is-favorite="product.isFavorite"
                  />
                </div>
              </div>
            </div>
          </div>

          <div class="tabs-card">
            <el-tabs v-model="activeTab" class="product-tabs">
              <el-tab-pane label="商品详情" name="detail">
                <div class="detail-content" v-html="product.detail || product.description"></div>
              </el-tab-pane>
              
              <el-tab-pane label="用户评价" name="comments">
                <div v-loading="commentLoading" class="comments-section">
                  <template v-if="comments.length > 0">
                    <div class="comments-list">
                      <div 
                        v-for="comment in comments" 
                        :key="comment.id" 
                        class="comment-item"
                      >
                        <div class="comment-header">
                          <el-avatar :src="comment.userAvatar" :size="48">
                            {{ comment.userName?.charAt(0) }}
                          </el-avatar>
                          <div class="comment-user-info">
                            <div class="comment-user">
                              <span class="user-name">{{ comment.userName }}</span>
                              <el-rate :model-value="comment.rating" disabled size="small" />
                              <span class="comment-time">{{ comment.createTime }}</span>
                            </div>
                            <p class="comment-content">{{ comment.content }}</p>
                            <div v-if="comment.images?.length" class="comment-images">
                              <el-image 
                                v-for="(img, index) in comment.images" 
                                :key="index"
                                :src="img"
                                :preview-src-list="comment.images"
                                :initial-index="index"
                                fit="cover"
                                class="comment-image"
                              />
                            </div>
                            <div class="comment-stats">
                              <span class="stat-item">
                                <el-icon><Pointer /></el-icon>
                                {{ comment.likes }} 人觉得有用
                              </span>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                    <div class="pagination-container">
                      <el-pagination
                        :current-page="commentPagination.current"
                        :page-size="commentPagination.size"
                        :total="commentPagination.total"
                        layout="prev, pager, next"
                        @current-change="handleCommentPageChange"
                      />
                    </div>
                  </template>
                  <el-empty v-else description="暂无评价" />
                </div>
              </el-tab-pane>
            </el-tabs>
          </div>
        </div>
      </template>
    </div>
  </UserLayout>
</template>

<style scoped>
.product-detail-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f0f5ff 0%, #e6f7ff 50%, #f5f7fa 100%);
  padding: 24px 0;
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.product-card {
  background: #ffffff;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  padding: 32px;
  margin-bottom: 24px;
}

.product-layout {
  display: flex;
  gap: 48px;
}

.product-gallery {
  width: 440px;
  flex-shrink: 0;
}

.main-image {
  width: 100%;
  aspect-ratio: 1;
  border-radius: var(--radius-md);
  overflow: hidden;
  background: #f5f7fa;
  margin-bottom: 16px;
}

.image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.main-image:hover .image {
  transform: scale(1.05);
}

.thumbnails {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding: 4px 0;
}

.thumbnail {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-sm);
  border: 2px solid transparent;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.thumbnail:hover {
  border-color: var(--primary-color);
  transform: translateY(-2px);
}

.thumbnail.active {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px var(--primary-opacity-10);
}

.thumbnail-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  flex: 1;
}

.product-title {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 24px;
  line-height: 1.4;
}

.price-section {
  background: linear-gradient(135deg, #fff7ed 0%, #fef3c7 100%);
  border-radius: var(--radius-md);
  padding: 24px;
  margin-bottom: 24px;
  border: 1px solid rgba(245, 158, 11, 0.2);
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 16px;
  margin-bottom: 8px;
}

.current-price {
  font-size: 36px;
  font-weight: 800;
  color: #ef4444;
}

.original-price {
  font-size: 18px;
  color: #9ca3af;
  text-decoration: line-through;
}

.saving-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #ea580c;
  font-size: 14px;
}

.spec-section {
  margin-bottom: 24px;
}

.spec-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;
}

.spec-label {
  width: 80px;
  color: #6b7280;
  padding-top: 6px;
  flex-shrink: 0;
}

.spec-values {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.spec-value {
  padding: 10px 20px;
  border: 1.5px solid #e5e7eb;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
  color: #374151;
}

.spec-value:hover {
  border-color: var(--primary-light);
  background: var(--primary-opacity-10);
}

.spec-value.active {
  border-color: var(--primary-color);
  background: var(--primary-opacity-10);
  color: var(--primary-color);
  font-weight: 600;
}

.info-section {
  margin-bottom: 24px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}

.info-label {
  width: 80px;
  color: #6b7280;
  flex-shrink: 0;
}

.info-value {
  color: #374151;
}

.info-tip {
  color: #9ca3af;
  font-size: 13px;
}

.coupon-section {
  background: #fff7ed;
  border-radius: var(--radius-md);
  padding: 20px;
  margin-bottom: 24px;
}

.coupon-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.coupon-icon {
  color: #f59e0b;
  font-size: 20px;
}

.coupon-title {
  font-weight: 600;
  color: #78350f;
}

.coupon-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.quantity-section {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.action-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.buy-now-btn,
.add-cart-btn {
  flex: 1;
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--radius-md);
  transition: all 0.3s ease;
}

.buy-now-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(22, 119, 255, 0.3);
}

.add-cart-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(245, 158, 11, 0.3);
}

.tabs-card {
  background: #ffffff;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}

.product-tabs {
  padding: 8px 32px 32px;
}

.detail-content {
  padding: 24px 0;
}

.comments-section {
  padding: 24px 0;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.comment-item {
  padding-bottom: 24px;
  border-bottom: 1px solid #f3f4f6;
}

.comment-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.comment-header {
  display: flex;
  gap: 16px;
}

.comment-user-info {
  flex: 1;
}

.comment-user {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.user-name {
  font-weight: 600;
  color: #1f2937;
}

.comment-time {
  color: #9ca3af;
  font-size: 13px;
}

.comment-content {
  color: #4b5563;
  line-height: 1.6;
  margin-bottom: 12px;
}

.comment-images {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.comment-image {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-sm);
  cursor: pointer;
}

.comment-stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #9ca3af;
  font-size: 13px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

@media (max-width: 1024px) {
  .product-layout {
    flex-direction: column;
    gap: 32px;
  }
  
  .product-gallery {
    width: 100%;
  }
}

@media (max-width: 640px) {
  .product-detail-page {
    padding: 12px 0;
  }
  
  .product-card {
    padding: 20px;
  }
  
  .product-title {
    font-size: 22px;
  }
  
  .current-price {
    font-size: 28px;
  }
  
  .action-section {
    flex-direction: column;
  }
  
  .product-tabs {
    padding: 8px 20px 20px;
  }
}
</style>
