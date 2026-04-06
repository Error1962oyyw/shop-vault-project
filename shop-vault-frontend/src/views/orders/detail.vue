<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  Clock,
  Document,
  Money,
  Wallet,
  CreditCard,
  CircleCheck,
  Warning,
  Close
} from '@element-plus/icons-vue'
import { getOrderDetail, payOrder, cancelOrder } from '@/api/order'
import { getProfile } from '@/api/user'
import { formatMoney, formatDateTime } from '@/utils/format'
import type { OrderDetail } from '@/types/api'
import UserLayout from '@/components/layout/UserLayout.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const order = ref<OrderDetail | null>(null)
const paying = ref(false)
const selectedPaymentMethod = ref<'BALANCE' | 'DIRECT'>('BALANCE')
const userBalance = ref(0)

const orderId = computed(() => Number(route.params.id))

const orderAmount = computed(() => order.value?.payAmount || order.value?.totalAmount || 0)

const isExpired = computed(() => {
  if (!order.value) return false
  if (order.value.status !== 0) return false
  if (!order.value.expireTime) return false
  return new Date(order.value.expireTime) <= new Date()
})

const isOrderPayable = computed(() => {
  if (!order.value) return false
  return order.value.status === 0 && !isExpired.value
})

const isBalanceSufficient = computed(() => userBalance.value >= orderAmount.value)

const balanceShortfall = computed(() => Math.max(0, orderAmount.value - userBalance.value))

const balancePaymentAmount = computed(() => {
  if (selectedPaymentMethod.value === 'BALANCE') {
    return isBalanceSufficient.value ? orderAmount.value : userBalance.value
  }
  return 0
})

const directPaymentAmount = computed(() => {
  if (selectedPaymentMethod.value === 'DIRECT') {
    return orderAmount.value
  }
  if (selectedPaymentMethod.value === 'BALANCE') {
    return isBalanceSufficient.value ? 0 : balanceShortfall.value
  }
  return 0
})

const effectivePaymentMethod = computed(() => {
  if (selectedPaymentMethod.value === 'DIRECT') return 'DIRECT'
  if (isBalanceSufficient.value) return 'BALANCE'
  return 'COMBO'
})

const closeReasonText = computed(() => {
  if (!order.value) return '该订单已被取消'
  if (isExpired.value) return '用户在24小时内未支付，系统自动取消订单'
  return order.value.closeReason || '该订单已被取消'
})

const statusMap: Record<number, { label: string; class: string; icon: any }> = {
  0: { label: '待付款', class: 'status-pending', icon: Clock },
  1: { label: '待发货', class: 'status-processing', icon: Document },
  2: { label: '待收货', class: 'status-shipping', icon: Document },
  3: { label: '已完成', class: 'status-completed', icon: CircleCheck },
  4: { label: '已关闭', class: 'status-closed', icon: Close }
}

const getOrderDisplayName = (orderData: OrderDetail) => {
  if (orderData.productName && orderData.productName !== 'VIP会员') return orderData.productName
  if (orderData.orderType === 1) return 'VIP会员'
  if (orderData.orderType === 2) return 'SVIP年卡'
  if (orderData.orderType === 3) return '积分兑换商品'
  return '商品'
}

const getOrderTypeLabel = (type: number) => {
  switch (type) {
    case 1: return 'VIP购买'
    case 2: return 'VIP购买'
    case 3: return '积分兑换'
    default: return '普通商品'
  }
}

const fetchOrderDetail = async () => {
  loading.value = true
  try {
    order.value = await getOrderDetail(orderId.value)
    const profile = await getProfile()
    userBalance.value = profile.balance ?? 0
  } catch (error) {
    console.error('获取订单详情失败', error)
    ElMessage.error('订单不存在或加载失败')
    router.push('/orders')
  } finally {
    loading.value = false
  }
}

const handlePay = async () => {
  if (!order.value) return

  const method = effectivePaymentMethod.value

  let htmlContent = ''
  if (method === 'BALANCE') {
    htmlContent = `
      <div style="padding: 8px 0;">
        <div style="display:flex;justify-content:space-between;padding:8px 0;font-size:14px;"><span style="color:#6b7280;">支付方式</span><span style="color:#374151;font-weight:500;">余额支付</span></div>
        <div style="display:flex;justify-content:space-between;padding:8px 0;font-size:14px;"><span style="color:#6b7280;">订单金额</span><span style="color:#f59e0b;font-weight:700;font-size:16px;">${formatMoney(orderAmount.value)}</span></div>
        <div style="display:flex;justify-content:space-between;padding:8px 0;font-size:14px;margin-top:4px;border-top:1px solid #f3f4f6;padding-top:12px;"><span style="color:#1f2937;font-weight:600;font-size:15px;">余额变动</span><span style="color:#ef4444;font-weight:700;font-size:16px;">${formatMoney(userBalance.value)} → ${formatMoney(userBalance.value - orderAmount.value)}</span></div>
      </div>
      <div style="margin-top:16px;padding:12px 14px;background:#fef3c7;border-radius:8px;font-size:13px;color:#92400e;line-height:1.6;display:flex;align-items:flex-start;gap:6px;">
        支付后账户余额将变为 <strong>${formatMoney(userBalance.value - orderAmount.value)}</strong>
      </div>
    `
  } else if (method === 'COMBO') {
    htmlContent = `
      <div style="padding: 8px 0;">
        <div style="display:flex;justify-content:space-between;padding:8px 0;font-size:14px;"><span style="color:#6b7280;">支付方式</span><span style="color:#f59e0b;font-weight:600;">组合支付（余额不足）</span></div>
        <div style="margin:12px 0;padding:14px 18px;background:#fffbeb;border:1px solid #fcd34d;border-radius:10px;">
          <div style="display:flex;justify-content:space-between;padding:4px 0;font-size:14px;color:#374151;"><span>余额扣除</span><span>${formatMoney(balancePaymentAmount.value)}</span></div>
          <div style="display:flex;justify-content:space-between;padding:4px 0;font-size:14px;color:#374151;"><span>直接支付</span><span>${formatMoney(directPaymentAmount.value)}</span></div>
          <div style="border-top:1px solid #f3f4f6;margin:8px 0;"></div>
          <div style="display:flex;justify-content:space-between;padding:4px 0;font-size:15px;color:#1f2937;font-weight:700;"><span>合计应付</span><span>${formatMoney(orderAmount.value)}</span></div>
        </div>
      </div>
      <div style="margin-top:16px;padding:12px 14px;background:#fef3c7;border-radius:8px;font-size:13px;color:#92400e;line-height:1.6;">
        当前余额 ${formatMoney(userBalance.value)}，还差 ${formatMoney(balanceShortfall.value)}<br/>系统将自动采用组合支付方式完成订单。
      </div>
    `
  } else {
    htmlContent = `
      <div style="padding: 8px 0;">
        <div style="display:flex;justify-content:space-between;padding:8px 0;font-size:14px;"><span style="color:#6b7280;">支付方式</span><span style="color:#374151;font-weight:500;">直接支付</span></div>
        <div style="display:flex;justify-content:space-between;padding:8px 0;font-size:14px;margin-top:4px;border-top:1px solid #f3f4f6;padding-top:12px;"><span style="color:#1f2937;font-weight:600;font-size:15px;">应付金额</span><span style="color:#ef4444;font-weight:700;font-size:18px;">${formatMoney(orderAmount.value)}</span></div>
      </div>
    `
  }

  try {
    await ElMessageBox.confirm(htmlContent, '支付确认', {
      confirmButtonText: '确认支付',
      cancelButtonText: '取消',
      type: method === 'COMBO' ? 'warning' : 'info',
      dangerouslyUseHTMLString: true,
      customClass: 'pay-confirm-box'
    })

    paying.value = true
    await payOrder(orderId.value, { paymentMethod: method })
    ElMessage.success('支付成功')
    const profile = await getProfile()
    userBalance.value = profile.balance ?? 0
    await fetchOrderDetail()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('支付失败', error)
      const msg = error?.response?.data?.msg || error?.message || '支付失败'
      ElMessage.error(msg)
    }
  } finally {
    paying.value = false
  }
}

const handleCancel = async () => {
  if (!order.value) return

  try {
    await ElMessageBox.confirm('确认取消该订单？取消后无法恢复。', '取消确认', {
      confirmButtonText: '确认取消',
      cancelButtonText: '再想想',
      type: 'warning'
    })

    await cancelOrder(orderId.value)
    ElMessage.success('订单已取消')
    await fetchOrderDetail()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('取消订单失败', error)
      const msg = error?.response?.data?.msg || error?.message || '取消失败'
      ElMessage.error(msg)
    }
  }
}

const goBack = () => {
  router.push('/orders')
}

onMounted(() => {
  fetchOrderDetail()
})
</script>

<template>
  <UserLayout>
    <div class="detail-page animate-fade-in">
      <div class="page-header">
        <div class="header-left">
          <el-button :icon="ArrowLeft" @click="goBack">返回订单列表</el-button>
          <el-divider direction="vertical" />
          <span class="order-no-text">{{ order?.orderNo || '加载中...' }}</span>
        </div>
        <h1 class="header-title">
          <Document class="title-icon" />
          订单详情
        </h1>
      </div>

      <div v-loading="loading" class="detail-content">
        <template v-if="order">
          <!-- 状态卡片 -->
          <el-card class="status-card" shadow="never">
            <div class="status-header">
              <div class="status-left">
                <div :class="['status-badge', isExpired ? 'status-expired' : statusMap[order.status]?.class]">
                  <el-icon><component :is="isExpired ? Warning : statusMap[order.status]?.icon" /></el-icon>
                  {{ isExpired ? '已过期' : (statusMap[order.status]?.label || '未知') }}
                </div>
                <p class="order-no">订单号：{{ order.orderNo }}</p>
              </div>
              <div class="status-right">
                <span v-if="order.status === 0" class="amount-highlight">
                  待付款：<em>{{ formatMoney(order.payAmount || order.totalAmount || 0) }}</em>
                </span>
                <span v-else-if="order.status === 3" class="amount-success">
                  实付金额：<em>{{ formatMoney(order.payAmount || order.totalAmount || 0) }}</em>
                </span>
              </div>
            </div>
          </el-card>

          <!-- 商品信息 + 订单信息 -->
          <div class="info-grid">
            <el-card class="product-card" shadow="never">
              <template #header>
                <div class="card-title">
                  <el-icon><Document /></el-icon>
                  商品信息
                </div>
              </template>
              <div class="product-detail">
                <img
                  :src="order.productImage"
                  :alt="getOrderDisplayName(order)"
                  class="product-image"
                />
                <div class="product-info">
                  <h3 class="product-name">{{ getOrderDisplayName(order) }}</h3>
                  <p class="product-meta">
                    <span>单价：{{ formatMoney(order.productPrice ?? (order.totalAmount && order.quantity ? Math.round((order.totalAmount / order.quantity) * 100) / 100 : 0)) }}</span>
                    <span>数量：{{ order.quantity || 1 }}</span>
                  </p>
                  <el-tag size="small" :type="order.orderType === 3 ? 'warning' : 'info'">
                    {{ getOrderTypeLabel(order.orderType || 0) }}
                  </el-tag>
                </div>
                <div class="product-total">
                  <span class="total-label">合计</span>
                  <span class="total-value">{{ formatMoney(order.totalAmount || 0) }}</span>
                </div>
              </div>
            </el-card>

            <el-card class="meta-card" shadow="never">
              <template #header>
                <div class="card-title">
                  <el-icon><Clock /></el-icon>
                  订单信息
                </div>
              </template>
              <div class="meta-list">
                <div class="meta-item">
                  <span class="meta-label">下单时间</span>
                  <span class="meta-value">{{ formatDateTime(order.createTime) }}</span>
                </div>
                <div v-if="order.payTime" class="meta-item">
                  <span class="meta-label">支付时间</span>
                  <span class="meta-value">{{ formatDateTime(order.payTime) }}</span>
                </div>
                <div v-if="order.expireTime && order.status === 0" class="meta-item expire-item">
                  <span class="meta-label"><el-icon><Warning /></el-icon> 过期时间</span>
                  <span class="meta-value expire-text">{{ formatDateTime(order.expireTime) }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">订单类型</span>
                  <span class="meta-value">{{ getOrderTypeLabel(order.orderType || 0) }}</span>
                </div>
                <div v-if="order.paymentMethodName && [1, 2].includes(order.status)" class="meta-item">
                  <span class="meta-label">支付方式</span>
                  <span class="meta-value">{{ order.paymentMethodName }}</span>
                </div>
                <div v-if="order.receiverName" class="meta-item">
                  <span class="meta-label">收货人</span>
                  <span class="meta-value">{{ order.receiverName }} {{ order.receiverPhone }}</span>
                </div>
                <div v-if="order.receiverAddress" class="meta-item">
                  <span class="meta-label">收货地址</span>
                  <span class="meta-value address-value">{{ order.receiverAddress }}</span>
                </div>
                <div v-if="order.trackingNo" class="meta-item">
                  <span class="meta-label">物流单号</span>
                  <span class="meta-value tracking-value">{{ order.trackingCompany }} {{ order.trackingNo }}</span>
                </div>
                <div v-if="order.remark" class="meta-item remark-item">
                  <span class="meta-label">备注</span>
                  <span class="meta-value remark-value">{{ order.remark }}</span>
                </div>
              </div>
            </el-card>
          </div>

          <!-- 价格明细 -->
          <el-card class="price-card" shadow="never">
            <template #header>
              <div class="card-title">
                <el-icon><Money /></el-icon>
                价格明细
              </div>
            </template>
            <div class="price-list">
              <div class="price-row">
                <span class="price-label">商品金额</span>
                <span class="price-value">{{ formatMoney(order.totalAmount || 0) }}</span>
              </div>
              <div v-if="(order.discountAmount ?? 0) > 0" class="price-row discount-row">
                <span class="price-label">优惠减免</span>
                <span class="price-value discount-value">-{{ formatMoney(order.discountAmount ?? 0) }}</span>
              </div>
              <div v-if="(order.freightAmount ?? 0) > 0" class="price-row">
                <span class="price-label">运费</span>
                <span class="price-value">{{ formatMoney(order.freightAmount ?? 0) }}</span>
              </div>
              <el-divider />
              <div class="price-row total-row">
                <span class="price-label total-label">实付金额</span>
                <span class="price-value total-value">{{ order.status === 4 ? formatMoney(0) : formatMoney(order.payAmount || order.totalAmount || 0) }}</span>
              </div>
            </div>
          </el-card>

          <!-- 支付区域 -->
          <template v-if="isOrderPayable">
            <el-card class="payment-card" shadow="never">
              <template #header>
                <div class="card-title">
                  <el-icon><Wallet /></el-icon>
                  选择支付方式
                </div>
              </template>

              <!-- 余额显示 -->
              <div class="balance-display">
                <span class="balance-label">账户余额</span>
                <span :class="['balance-amount', { insufficient: !isBalanceSufficient }]">{{ formatMoney(userBalance) }}</span>
              </div>

              <!-- 支付方式选项 -->
              <div class="payment-methods">
                <div
                  :class="['method-option', { active: selectedPaymentMethod === 'BALANCE' }]"
                  @click="selectedPaymentMethod = 'BALANCE'"
                >
                  <el-icon class="method-icon"><Wallet /></el-icon>
                  <div class="method-info">
                    <span class="method-name">余额支付</span>
                    <span class="method-desc">(当前余额: {{ formatMoney(userBalance) }})</span>
                  </div>
                  <div class="method-check">
                    <span v-if="selectedPaymentMethod === 'BALANCE'" class="check-dot"></span>
                  </div>
                </div>
                <div
                  :class="['method-option', { active: selectedPaymentMethod === 'DIRECT' }]"
                  @click="selectedPaymentMethod = 'DIRECT'"
                >
                  <el-icon class="method-icon"><CreditCard /></el-icon>
                  <div class="method-info">
                    <span class="method-name">直接支付</span>
                    <span class="method-desc">模拟付款（测试用）</span>
                  </div>
                  <div class="method-check">
                    <span v-if="selectedPaymentMethod === 'DIRECT'" class="check-dot"></span>
                  </div>
                </div>
              </div>

              <!-- 金额明细 -->
              <div v-if="effectivePaymentMethod === 'COMBO'" class="combo-detail">
                <div class="combo-row">
                  <span>余额支付</span>
                  <span>{{ formatMoney(balancePaymentAmount) }}</span>
                </div>
                <div class="combo-row">
                  <span>直接支付</span>
                  <span>{{ formatMoney(directPaymentAmount) }}</span>
                </div>
                <el-divider style="margin: 8px 0" />
                <div class="combo-row total">
                  <span>合计应付</span>
                  <span>{{ formatMoney(orderAmount) }}</span>
                </div>
              </div>

              <!-- 余额不足提示 -->
              <div v-if="!isBalanceSufficient && selectedPaymentMethod === 'BALANCE'" class="insufficient-tip">
                <el-alert type="warning" :closable="false" show-icon>
                  <template #title>余额不足</template>
                  当前余额 {{ formatMoney(userBalance) }}，还差 {{ formatMoney(balanceShortfall) }}
                  <br />
                  将自动采用组合支付：余额扣除 {{ formatMoney(userBalance) }} + 直接支付 {{ formatMoney(balanceShortfall) }}
                </el-alert>
              </div>

              <!-- 操作按钮 -->
              <div class="payment-actions">
                <el-button size="large" @click="handleCancel">取消订单</el-button>
                <el-button
                  type="primary"
                  size="large"
                  :loading="paying"
                  class="pay-btn"
                  @click="handlePay"
                >
                  立即支付 {{ formatMoney(orderAmount) }}
                </el-button>
              </div>
            </el-card>
          </template>

          <!-- 已关闭/已过期提示 -->
          <el-card v-if="order.status === 4 || isExpired" class="closed-card" shadow="never">
            <el-result :icon="isExpired ? 'warning' : 'info'" :title="isExpired ? '订单已过期' : '订单已关闭'" :sub-title="closeReasonText">
              <template #extra>
                <el-button type="primary" @click="router.push('/products')">去购物</el-button>
              </template>
            </el-result>
          </el-card>

          <!-- 过期但状态未更新的提示 -->
          <div v-if="isExpired && order.status === 0" class="expire-notice">
            <el-alert type="warning" :closable="false" show-icon>
              <template #title>订单已过期</template>
              该订单因超时未付款已自动关闭，无法继续支付。
            </el-alert>
          </div>
        </template>

        <el-empty v-else-if="!loading" description="订单不存在" />
      </div>
    </div>
  </UserLayout>
</template>

<style scoped>
.detail-page {
  max-width: 960px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
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
  width: 24px;
  height: 24px;
  color: #1677ff;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 状态卡片 */
.status-card {
  border-radius: 12px;
  border: none;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.status-card :deep(.el-card__body) {
  padding: 28px 32px;
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-left {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  border-radius: 24px;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  width: fit-content;
}

.status-badge.status-pending {
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(8px);
}

.status-badge.status-processing {
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(8px);
}

.status-badge.status-shipping {
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(8px);
}

.status-badge.status-completed {
  background: rgba(34, 197, 94, 0.3);
}

.status-badge.status-closed {
  background: rgba(156, 163, 175, 0.3);
}

.status-badge.status-expired {
  background: rgba(245, 158, 11, 0.3);
}

.order-no {
  color: rgba(255, 255, 255, 0.85);
  font-size: 14px;
  margin: 0;
}

.status-right .amount-highlight em,
.status-right .amount-success em {
  font-style: normal;
  font-size: 26px;
  font-weight: 800;
  color: #fff;
}

.status-right .amount-highlight,
.status-right .amount-success {
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
}

/* 信息网格 */
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
}

.card-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 600;
  color: #374151;
}

/* 商品卡片 */
.product-card,
.meta-card,
.price-card,
.payment-card {
  border-radius: 12px;
  border: 1px solid #e5e7eb;
}

.product-card :deep(.el-card__header),
.meta-card :deep(.el-card__header),
.price-card :deep(.el-card__header),
.payment-card :deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #f3f4f6;
  background: #fafbfc;
}

.product-detail {
  display: flex;
  align-items: center;
  gap: 16px;
}

.product-image {
  width: 88px;
  height: 88px;
  border-radius: 10px;
  object-fit: cover;
  border: 1px solid #e5e7eb;
  flex-shrink: 0;
}

.product-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.product-name {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-meta {
  display: flex;
  gap: 12px;
  font-size: 13px;
  color: #6b7280;
  margin: 0;
}

.product-total {
  text-align: right;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: center;
}

.total-label {
  font-size: 12px;
  color: #9ca3af;
}

.total-value {
  font-size: 18px;
  font-weight: 700;
  color: #ef4444;
}

/* 元信息列表 */
.meta-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.meta-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  font-size: 14px;
}

.meta-label {
  color: #9ca3af;
  white-space: nowrap;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 4px;
}

.meta-value {
  color: #374151;
  text-align: right;
  word-break: break-all;
}

.expire-item .expire-text {
  color: #f59e0b;
  font-weight: 500;
}

.address-value {
  max-width: 220px;
}

.tracking-value {
  color: #1677ff;
  cursor: pointer;
}

.remark-item .remark-value {
  color: #6b7280;
  font-size: 13px;
  font-style: italic;
}

/* 价格明细 */
.price-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 15px;
}

.price-label {
  color: #6b7280;
}

.price-value {
  color: #374151;
  font-weight: 500;
}

.discount-row .discount-value {
  color: #ef4444;
}

.total-row {
  margin-top: 4px;
}

.total-row .total-label {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.total-row .total-value {
  font-size: 22px;
  font-weight: 800;
  color: #ef4444;
}

/* 支付方式 */
.payment-methods {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 20px;
}

.method-option {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  border: 2px solid #e5e7eb;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s ease;
  background: #fafbfc;
}

.method-option:hover {
  border-color: #c7d2fe;
  background: #f0f0ff;
}

.method-option.active {
  border-color: #1677ff;
  background: #eff6ff;
  box-shadow: 0 0 0 3px rgba(22, 119, 255, 0.1);
}

.method-icon {
  width: 36px;
  height: 36px;
  font-size: 22px;
  color: #6b7280;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: #f3f4f6;
  flex-shrink: 0;
}

.method-option.active .method-icon {
  color: #1677ff;
  background: #dbeafe;
}

.method-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.method-name {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}

.method-desc {
  font-size: 12px;
  color: #9ca3af;
}

.method-check {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid #d1d5db;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.2s ease;
}

.method-option.active .method-check {
  border-color: #1677ff;
  background: #1677ff;
}

.check-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #fff;
}

.payment-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.pay-btn {
  min-width: 160px;
  font-weight: 600;
  font-size: 15px;
  border-radius: 8px;
}

.balance-tip {
  border-radius: 8px;
}

/* 余额显示 */
.balance-display {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f9fafb;
  border-radius: 8px;
  margin-bottom: 16px;
}

.balance-label {
  font-size: 14px;
  color: #6b7280;
}

.balance-amount {
  font-size: 18px;
  font-weight: 700;
  color: #10b981;
}

.balance-amount.insufficient {
  color: #ef4444;
}

/* 组合支付明细 */
.combo-detail {
  margin: 16px 0;
  padding: 14px 18px;
  background: #fffbeb;
  border: 1px solid #fcd34d;
  border-radius: 10px;
}

.combo-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  padding: 4px 0;
  color: #374151;
}

.combo-row.total {
  font-weight: 700;
  font-size: 15px;
  color: #1f2937;
  padding-top: 4px;
}

.insufficient-tip {
  margin: 12px 0 16px;
}

.closed-card {
  border-radius: 12px;
  text-align: center;
}

.expire-notice {
  margin-top: -12px;
  margin-bottom: 0;
}
</style>
