<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import UserLayout from '@/components/layout/UserLayout.vue'
import {
  Location,
  Goods,
  Ticket,
  Warning,
  ShoppingCart,
  Medal
} from '@element-plus/icons-vue'
import { getAddressList } from '@/api/user'
import { getCartList } from '@/api/cart'
import { getProductDetail } from '@/api/product'
import { cartCheckout, buyNow, createVipOrder, createPointsExchangeOrder, getUserOrders } from '@/api/order'
import { getApplicableCoupons, getBestCoupon, getCurrentMemberDay } from '@/api/marketing'
import { useUserStore } from '@/stores/user'
import { formatMoney } from '@/utils/format'
import type { Address, CartItem, UserCoupon, MemberDay } from '@/types/api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const MAX_PENDING_ORDERS = 5

const loading = ref(false)
const submitting = ref(false)
const addresses = ref<Address[]>([])
const selectedAddress = ref<Address | null>(null)
const cartItems = ref<CartItem[]>([])
const remark = ref('')

const applicableCoupons = ref<UserCoupon[]>([])
const selectedCoupon = ref<UserCoupon | null>(null)
const showCouponDialog = ref(false)
const memberDay = ref<MemberDay | null>(null)

const showConfirmDialog = ref(false)
const pendingOrderCount = ref(0)
const checkingPendingOrders = ref(false)

const isVipOrder = computed(() => !!route.query.vipType)
const isPointsOrder = computed(() => !!route.query.pointsProductId)

const vipType = computed(() => (route.query.vipType ? Number(route.query.vipType) : null))
const vipPrice = computed(() => (route.query.price ? Number(route.query.price) : 0))
const vipName = computed(() => (route.query.name as string) || '')

const cartIds = computed(() => {
  const ids = route.query.cartIds as string
  return ids ? ids.split(',').map(Number) : []
})

const productId = computed(() => (route.query.productId ? Number(route.query.productId) : null))
const quantity = computed(() => (route.query.quantity ? Number(route.query.quantity) : 1))

const orderTypeLabel = computed(() => {
  if (isVipOrder.value) return 'VIP/SVIP会员订单'
  if (isPointsOrder.value) return '积分兑换订单'
  return '商品订单'
})

const productAmount = computed(() => {
  if (isVipOrder.value || isPointsOrder.value) {
    return vipPrice.value
  }
  return cartItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
})

const freightAmount = computed(() => {
  if (isVipOrder.value || isPointsOrder.value) {
    return 0
  }
  const maxFreight = cartItems.value.reduce((max, item) => {
    const itemFreight = (item as any).freight ?? 0
    return Math.max(max, itemFreight)
  }, 0)
  return maxFreight
})

const discountAmount = computed(() => {
  if (isVipOrder.value || isPointsOrder.value) {
    return 0
  }
  if (selectedCoupon.value) {
    if (productAmount.value >= selectedCoupon.value.minAmount) {
      return selectedCoupon.value.value
    }
  }
  return 0
})

const memberDayDiscount = computed(() => {
  if (isVipOrder.value || isPointsOrder.value) {
    return 0
  }
  if (memberDay.value && memberDay.value.status === 1) {
    return productAmount.value * (1 - memberDay.value.discountRate)
  }
  return 0
})

const totalDiscount = computed(() => {
  return discountAmount.value + memberDayDiscount.value
})

const totalAmount = computed(() => {
  return Math.max(0, productAmount.value - totalDiscount.value + freightAmount.value)
})

const canSubmit = computed(() => {
  if (isVipOrder.value || isPointsOrder.value) {
    return true
  }
  return !!(selectedAddress.value && cartItems.value.length > 0)
})

const isOverPendingLimit = computed(() => pendingOrderCount.value >= MAX_PENDING_ORDERS)

const fetchAddresses = async () => {
  try {
    addresses.value = await getAddressList()
    const defaultAddr = addresses.value.find(a => a.isDefault === 1)
    if (defaultAddr) {
      selectedAddress.value = defaultAddr
    } else if (addresses.value.length > 0) {
      selectedAddress.value = addresses.value[0]
    }
  } catch (error) {
    console.error('获取地址失败', error)
  }
}

const fetchCartItems = async () => {
  if (isVipOrder.value || isPointsOrder.value) {
    return
  }

  loading.value = true
  try {
    if (productId.value) {
      const product = await getProductDetail(productId.value)
      cartItems.value = [{
        id: 0,
        productId: product.id,
        productName: product.name,
        productImage: product.mainImage,
        price: product.price,
        quantity: quantity.value,
        stock: product.stock,
        freight: product.freight ?? 0,
        selected: true
      }]
    } else if (cartIds.value.length > 0) {
      const allItems = await getCartList()
      cartItems.value = allItems.filter(item => cartIds.value.includes(item.id))
    }
  } catch (error) {
    console.error('获取商品信息失败', error)
  } finally {
    loading.value = false
  }
}

const fetchCoupons = async () => {
  if (isVipOrder.value || isPointsOrder.value) {
    return
  }

  try {
    applicableCoupons.value = await getApplicableCoupons({
      orderAmount: productAmount.value
    })

    const bestCoupon = await getBestCoupon({
      orderAmount: productAmount.value
    })
    if (bestCoupon) {
      selectedCoupon.value = bestCoupon
    }
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

const checkPendingOrders = async () => {
  checkingPendingOrders.value = true
  try {
    const res = await getUserOrders({ status: 0, page: 1, size: 1 })
    pendingOrderCount.value = res.total || 0
  } catch (error) {
    console.error('查询待支付订单数量失败', error)
    pendingOrderCount.value = 0
  } finally {
    checkingPendingOrders.value = false
  }
}

const selectCoupon = (coupon: UserCoupon) => {
  if (productAmount.value < coupon.minAmount) {
    ElMessage.warning(`该优惠券需要满${coupon.minAmount}元才能使用`)
    return
  }
  selectedCoupon.value = coupon
  showCouponDialog.value = false
}

const removeCoupon = () => {
  selectedCoupon.value = null
}

const formatCouponValue = (coupon: UserCoupon) => {
  if (coupon.type === 2 && coupon.discount) {
    return `${coupon.discount}折`
  }
  return `¥${coupon.value}`
}

const handlePreSubmit = async () => {
  if (!canSubmit.value) {
    if (!isVipOrder.value && !isPointsOrder.value && !selectedAddress.value) {
      ElMessage.warning('请选择收货地址')
      return
    }
    return
  }

  if (isOverPendingLimit.value) {
    ElMessage.warning(`您已有 ${pendingOrderCount.value} 笔待支付订单（上限 ${MAX_PENDING_ORDERS} 笔），请先完成或取消现有订单后再下单`)
    return
  }

  showConfirmDialog.value = true
}

const handleSubmit = async () => {
  showConfirmDialog.value = false

  if (isVipOrder.value || isPointsOrder.value) {
    await handleVipOrPointsOrder()
  } else {
    await handleNormalOrder()
  }
}

const handleNormalOrder = async () => {
  if (!selectedAddress.value) {
    ElMessage.warning('请选择收货地址')
    return
  }

  submitting.value = true
  try {
    let orderNo: string
    if (productId.value) {
      const result = await buyNow({
        productId: productId.value,
        quantity: quantity.value,
        addressId: selectedAddress.value.id,
        couponId: selectedCoupon.value?.id,
        remark: remark.value
      })
      orderNo = result.orderNo
    } else {
      const result = await cartCheckout({
        addressId: selectedAddress.value.id,
        remark: remark.value,
        userCouponId: selectedCoupon.value?.id,
        cartItemIds: cartIds.value
      } as any)
      orderNo = result.orderNo
    }

    ElMessage.success('订单创建成功，请在24小时内完成支付')
    router.push(`/orders/${orderNo}`)
  } catch (error: any) {
    const msg = error?.response?.data?.msg || error?.message || '提交订单失败'
    ElMessage.error(msg)
  } finally {
    submitting.value = false
  }
}

const handleVipOrPointsOrder = async () => {
  submitting.value = true
  try {
    let orderId: number

    if (isVipOrder.value && vipType.value) {
      const result = await createVipOrder({
        vipType: vipType.value,
        paymentMethod: 'DIRECT'
      })
      orderId = result.orderId
    } else if (isPointsOrder.value) {
      const pointsProductId = Number(route.query.pointsProductId)
      const result = await createPointsExchangeOrder({
        productId: pointsProductId,
        quantity: quantity.value
      })
      orderId = result.orderId
    } else {
      throw new Error('无效的订单类型')
    }

    ElMessage.success('订单创建成功，请在24小时内完成支付')
    router.push(`/orders/${orderId}`)
  } catch (error: any) {
    const msg = error?.response?.data?.msg || error?.message || '下单失败'
    ElMessage.error(msg)
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  if (!userStore.token) {
    router.push('/login')
    return
  }

  await Promise.all([
    fetchAddresses(),
    fetchCartItems(),
    checkPendingOrders()
  ])
  await Promise.all([
    fetchCoupons(),
    fetchMemberDay()
  ])
})
</script>

<template>
  <UserLayout>
    <div class="checkout-page animate-fade-in">
      <div class="page-header">
        <h1 class="header-title">
          <ShoppingCart class="title-icon" />
          确认{{ orderTypeLabel }}
        </h1>
      </div>

      <div v-loading="loading" class="checkout-grid">
        <div class="checkout-main">
          <!-- 收货地址 -->
          <section v-if="!isVipOrder && !isPointsOrder" class="checkout-section">
            <h2 class="section-title">
              <el-icon><Location /></el-icon>
              收货地址
            </h2>

            <template v-if="addresses.length > 0">
              <div class="address-list">
                <div
                  v-for="address in addresses"
                  :key="address.id"
                  :class="['address-card', { active: selectedAddress?.id === address.id }]"
                  @click="selectedAddress = address"
                >
                  <div class="address-top">
                    <span class="address-name">{{ address.receiverName }}</span>
                    <span class="address-phone">{{ address.receiverPhone }}</span>
                    <el-tag v-if="address.isDefault === 1" type="success" size="small">默认</el-tag>
                  </div>
                  <div class="address-detail">
                    {{ address.province }}{{ address.city }}{{ address.region }}{{ address.detailAddress }}
                  </div>
                </div>
              </div>
              <div class="address-actions">
                <el-button type="primary" link @click="router.push('/profile')">管理收货地址</el-button>
              </div>
            </template>

            <el-empty v-else description="暂无收货地址">
              <el-button type="primary" @click="router.push('/profile')">添加地址</el-button>
            </el-empty>
          </section>

          <!-- 商品信息 -->
          <section class="checkout-section">
            <h2 class="section-title">
              <el-icon><Goods /></el-icon>
              {{ isVipOrder || isPointsOrder ? '商品信息' : '商品清单' }}
            </h2>

            <template v-if="isVipOrder || isPointsOrder">
              <div class="vip-product-card">
                <div class="vip-icon-box" :class="vipType === 3 ? 'svip' : 'vip'">
                  <el-icon :size="32"><Medal /></el-icon>
                  <span class="vip-label">{{ vipType === 3 ? 'SVIP' : 'VIP' }}</span>
                </div>
                <div class="vip-info">
                  <h3 class="vip-name">{{ vipName || orderTypeLabel }}</h3>
                  <p class="vip-desc">{{ isVipOrder ? '享受会员专属权益和优惠' : '使用积分兑换商品' }}</p>
                </div>
                <div class="vip-price">{{ formatMoney(productAmount) }}</div>
              </div>
              <div class="notice-bar">
                <el-icon><Warning /></el-icon>
                {{ isVipOrder ? 'VIP/SVIP购买不享受优惠券及会员折扣优惠' : '积分兑换商品不享受优惠券及会员折扣优惠' }}
              </div>
            </template>

            <template v-else>
              <div class="product-list">
                <div
                  v-for="item in cartItems"
                  :key="item.id"
                  class="product-item"
                >
                  <img :src="item.productImage" :alt="item.productName" class="product-img" />
                  <div class="product-info">
                    <h3 class="product-name">{{ item.productName }}</h3>
                  </div>
                  <div class="product-price">× {{ item.quantity }}</div>
                  <div class="product-total">{{ formatMoney(item.price * item.quantity) }}</div>
                </div>
              </div>
            </template>
          </section>

          <!-- 优惠券 -->
          <section v-if="!isVipOrder && !isPointsOrder" class="checkout-section">
            <h2 class="section-title">
              <el-icon><Ticket /></el-icon>
              优惠券
            </h2>

            <div v-if="selectedCoupon" class="coupon-selected">
              <div class="coupon-value">{{ formatCouponValue(selectedCoupon) }}</div>
              <div class="coupon-info">
                <div class="coupon-name">{{ selectedCoupon.couponName }}</div>
                <div class="coupon-condition">满{{ selectedCoupon.minAmount }}可用</div>
              </div>
              <el-button type="danger" link size="small" @click="removeCoupon">不使用</el-button>
            </div>

            <div v-else-if="applicableCoupons.length > 0" class="coupon-empty">
              <span>有 {{ applicableCoupons.length }} 张可用优惠券</span>
              <el-button type="primary" link size="small" @click="showCouponDialog = true">选择优惠券</el-button>
            </div>

            <div v-else class="coupon-empty-none">暂无可用优惠券</div>
          </section>

          <!-- 备注 -->
          <section class="checkout-section">
            <h2 class="section-title">备注</h2>
            <el-input
              v-model="remark"
              type="textarea"
              :rows="3"
              placeholder="选填，可以告诉卖家您的特殊需求"
              resize="none"
            />
          </section>
        </div>

        <!-- 右侧结算栏 -->
        <div class="checkout-sidebar">
          <div class="settlement-card">
            <h2 class="settlement-title">订单结算</h2>

            <!-- 待支付订单提示 -->
            <div v-if="pendingOrderCount > 0" class="pending-tip" :class="{ over: isOverPendingLimit }">
              <span class="pending-count">当前有 <strong>{{ pendingOrderCount }}</strong> 笔待支付订单</span>
              <span class="pending-limit">（上限 {{ MAX_PENDING_ORDERS }} 笔）</span>
              <router-link v-if="!isOverPendingLimit" to="/orders?status=0" class="pending-link">去查看</router-link>
              <span v-else class="pending-block">已达上限，请先处理</span>
            </div>

            <div class="price-list">
              <div class="price-row">
                <span class="price-label">商品金额</span>
                <span class="price-value">{{ formatMoney(productAmount) }}</span>
              </div>
              <div v-if="freightAmount > 0" class="price-row">
                <span class="price-label">运费</span>
                <span class="price-value">{{ formatMoney(freightAmount) }}</span>
              </div>
              <div v-if="memberDayDiscount > 0" class="price-row discount">
                <span class="price-label">会员日优惠</span>
                <span class="price-value">-{{ formatMoney(memberDayDiscount) }}</span>
              </div>
              <div v-if="discountAmount > 0" class="price-row discount">
                <span class="price-label">优惠券抵扣</span>
                <span class="price-value">-{{ formatMoney(discountAmount) }}</span>
              </div>
              <el-divider />
              <div class="price-row total">
                <span class="price-label total-label">应付金额</span>
                <span class="price-value total-value">{{ formatMoney(totalAmount) }}</span>
              </div>
            </div>

            <!-- 提交按钮 -->
            <div class="submit-area">
              <el-button
                type="danger"
                size="large"
                class="submit-btn"
                :loading="submitting"
                :disabled="!canSubmit || isOverPendingLimit"
                @click="handlePreSubmit"
              >
                提交订单
              </el-button>
              <div class="submit-tips">
                <p class="tip-text">提交后请在 <strong>24小时内</strong> 完成支付，超时订单将自动取消</p>
                <p class="agreement-text">提交订单即表示您同意<a href="#">《服务协议》</a></p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 确认订单对话框 -->
    <el-dialog
      v-model="showConfirmDialog"
      title="确认订单信息"
      width="520px"
      :close-on-click-modal="false"
    >
      <div class="confirm-content">
        <div class="confirm-items">
          <div class="confirm-item">
            <span class="confirm-label">订单类型</span>
            <span class="confirm-value">{{ orderTypeLabel }}</span>
          </div>
          <div class="confirm-item">
            <span class="confirm-label">商品金额</span>
            <span class="confirm-value highlight">{{ formatMoney(productAmount) }}</span>
          </div>
          <div v-if="freightAmount > 0" class="confirm-item">
            <span class="confirm-label">运费</span>
            <span class="confirm-value">{{ formatMoney(freightAmount) }}</span>
          </div>
          <div v-if="totalDiscount > 0" class="confirm-item discount">
            <span class="confirm-label">优惠减免</span>
            <span class="confirm-value">-{{ formatMoney(totalDiscount) }}</span>
          </div>
          <el-divider />
          <div class="confirm-item total">
            <span class="confirm-label">应付总额</span>
            <span class="confirm-value total-highlight">{{ formatMoney(totalAmount) }}</span>
          </div>
        </div>
        <div class="confirm-notice">
          <el-icon><Warning /></el-icon>
          订单提交后将进入待支付状态，请在 24 小时内完成支付，超时未支付将自动取消并释放库存。
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button size="large" @click="showConfirmDialog = false">取消</el-button>
          <el-button type="danger" size="large" :loading="submitting" @click="handleSubmit">
            确认提交
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 优惠券选择对话框 -->
    <el-dialog
      v-model="showCouponDialog"
      title="选择优惠券"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="coupon-list">
        <div
          v-for="coupon in applicableCoupons"
          :key="coupon.id"
          :class="['coupon-card', { selected: selectedCoupon?.id === coupon.id }]"
          @click="selectCoupon(coupon)"
        >
          <div class="coupon-left">{{ formatCouponValue(coupon) }}</div>
          <div class="coupon-right">
            <div class="coupon-name">{{ coupon.couponName }}</div>
            <div class="coupon-condition">满{{ coupon.minAmount }}可用</div>
          </div>
        </div>
      </div>
    </el-dialog>
  </UserLayout>
</template>

<style scoped>
.checkout-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  min-height: calc(100vh - 120px);
}

.page-header {
  margin-bottom: 28px;
}

.header-title {
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0;
}

.title-icon {
  width: 26px;
  height: 26px;
  color: #1677ff;
}

.checkout-grid {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 24px;
  align-items: start;
}

@media (max-width: 960px) {
  .checkout-grid {
    grid-template-columns: 1fr;
  }
}

/* 主区域 */
.checkout-main {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.checkout-section {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #374151;
  margin: 0 0 18px 0;
}

/* 地址 */
.address-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.address-card {
  padding: 16px;
  border: 2px solid #e5e7eb;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s ease;
}

.address-card:hover {
  border-color: #93c5fd;
  background: #f8fafc;
}

.address-card.active {
  border-color: #1677ff;
  background: #eff6ff;
}

.address-top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.address-name {
  font-weight: 600;
  font-size: 15px;
  color: #1f2937;
}

.address-phone {
  color: #6b7280;
  font-size: 14px;
}

.address-detail {
  color: #9ca3af;
  font-size: 13px;
  line-height: 1.5;
}

.address-actions {
  margin-top: 12px;
}

/* VIP 商品卡片 */
.vip-product-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: linear-gradient(135deg, #fff7ed 0%, #fef3c7 100%);
  border: 1px solid #fcd34d;
  border-radius: 12px;
}

.vip-icon-box {
  width: 80px;
  height: 80px;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
  gap: 2px;
}

.vip-icon-box.vip {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}

.vip-icon-box.svip {
  background: linear-gradient(135deg, #722ed1 0%, #531dab 100%);
}

.vip-label {
  font-weight: 800;
  font-size: 12px;
  letter-spacing: 1px;
}

.vip-info {
  flex: 1;
  min-width: 0;
}

.vip-name {
  font-size: 17px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 4px 0;
}

.vip-desc {
  font-size: 13px;
  color: #9ca3af;
  margin: 0;
}

.vip-price {
  font-size: 22px;
  font-weight: 800;
  color: #f59e0b;
  white-space: nowrap;
}

.notice-bar {
  margin-top: 14px;
  padding: 12px 16px;
  background: #fef3c7;
  border-radius: 8px;
  font-size: 13px;
  color: #b45309;
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 商品列表 */
.product-list {
  display: flex;
  flex-direction: column;
  border-top: 1px solid #f3f4f6;
}

.product-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid #f3f4f6;
}

.product-item:last-child {
  border-bottom: none;
}

.product-img {
  width: 72px;
  height: 72px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid #e5e7eb;
  flex-shrink: 0;
}

.product-info {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 15px;
  font-weight: 500;
  color: #374151;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  color: #6b7280;
  font-size: 14px;
  white-space: nowrap;
}

.product-total {
  font-size: 16px;
  font-weight: 700;
  color: #ef4444;
  white-space: nowrap;
  min-width: 80px;
  text-align: right;
}

/* 优惠券 */
.coupon-selected {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  background: linear-gradient(135deg, #fff7ed 0%, #fef3c7 100%);
  border: 1px solid #fcd34d;
  border-radius: 10px;
}

.coupon-value {
  width: 48px;
  height: 48px;
  background: #f59e0b;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 700;
  font-size: 15px;
  flex-shrink: 0;
}

.coupon-info {
  flex: 1;
}

.coupon-name {
  font-weight: 600;
  font-size: 14px;
  color: #1f2937;
}

.coupon-condition {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 2px;
}

.coupon-empty {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: #f9fafb;
  border-radius: 8px;
  color: #6b7280;
  font-size: 14px;
}

.coupon-empty-none {
  text-align: center;
  padding: 20px;
  color: #d1d5db;
  font-size: 14px;
}

/* 侧边栏结算 */
.settlement-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding: 24px;
  position: sticky;
  top: 80px;
}

.settlement-title {
  font-size: 17px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 18px 0;
}

/* 待支付订单提示 */
.pending-tip {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
  padding: 10px 14px;
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 8px;
  font-size: 13px;
  color: #1e40af;
  margin-bottom: 16px;
}

.pending-tip.over {
  background: #fef2f2;
  border-color: #fecaca;
  color: #991b1b;
}

.pending-count strong {
  font-weight: 700;
}

.pending-limit {
  color: #6b7280;
}

.pending-link {
  color: #1677ff;
  text-decoration: none;
  font-weight: 500;
  margin-left: auto;
}

.pending-link:hover {
  text-decoration: underline;
}

.pending-block {
  color: #ef4444;
  font-weight: 600;
  margin-left: auto;
}

/* 价格明细 */
.price-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  font-size: 14px;
}

.price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price-label {
  color: #6b7280;
}

.price-value {
  color: #374151;
  font-weight: 500;
}

.price-row.discount .price-value {
  color: #ef4444;
}

.price-row.total {
  margin-top: 4px;
}

.price-row.total .total-label {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}

.price-row.total .total-value {
  font-size: 22px;
  font-weight: 800;
  color: #ef4444;
}

/* 提交按钮 */
.submit-area {
  margin-top: 20px;
  padding-top: 18px;
  border-top: 1px solid #f3f4f6;
}

.submit-btn {
  width: 100%;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  letter-spacing: 1px;
}

.submit-tips {
  margin-top: 10px;
}

.tip-text {
  font-size: 12px;
  color: #f59e0b;
  margin: 0 0 6px 0;
  text-align: center;
}

.tip-text strong {
  color: #d97706;
}

.agreement-text {
  text-align: center;
  font-size: 12px;
  color: #d1d5db;
  margin: 0;
}

.agreement-text a {
  color: #1677ff;
  text-decoration: none;
}

/* 确认对话框 */
.confirm-content {
  padding: 8px 0;
}

.confirm-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.confirm-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.confirm-label {
  color: #6b7280;
}

.confirm-value {
  color: #374151;
  font-weight: 500;
}

.confirm-value.highlight {
  color: #f59e0b;
  font-weight: 700;
  font-size: 16px;
}

.confirm-item.discount .confirm-value {
  color: #10b981;
}

.confirm-item.total {
  margin-top: 4px;
}

.confirm-item.total .confirm-label {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}

.confirm-item.total .total-highlight {
  font-size: 22px;
  font-weight: 800;
  color: #ef4444;
}

.confirm-notice {
  margin-top: 16px;
  padding: 12px 14px;
  background: #fef3c7;
  border-radius: 8px;
  font-size: 13px;
  color: #92400e;
  line-height: 1.8;
  display: flex;
  align-items: flex-start;
  gap: 6px;
}

.confirm-notice .el-icon {
  margin-top: 2px;
  flex-shrink: 0;
  color: #d97706;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 优惠券列表 */
.coupon-list {
  max-height: 360px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.coupon-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  border: 2px solid #e5e7eb;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.coupon-card:hover {
  border-color: #f59e0b;
}

.coupon-card.selected {
  border-color: #f59e0b;
  background: #fffbeb;
}
</style>
