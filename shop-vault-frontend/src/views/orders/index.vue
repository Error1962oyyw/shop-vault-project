<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Clock, List } from '@element-plus/icons-vue'
import { getUserOrders, cancelOrder } from '@/api/order'
import { formatMoney, formatDateTime } from '@/utils/format'
import type { OrderDetail } from '@/types/api'
import UserLayout from '@/components/layout/UserLayout.vue'

const router = useRouter()
const loading = ref(false)
const orders = ref<OrderDetail[]>([])
const currentStatus = ref<number | undefined>(undefined)
const total = ref(0)
const page = ref(1)
const size = ref(10)

const statusTabs = [
  { label: '全部', value: undefined },
  { label: '待付款', value: 0 },
  { label: '待发货', value: 1 },
  { label: '待收货', value: 2 },
  { label: '已完成', value: 3 },
  { label: '已关闭', value: 4 }
]

const countdownMap = ref<Record<number, { hours: number; minutes: number; seconds: number; expired: boolean }>>({})

const canPay = (order: OrderDetail) => {
  return order.status === 0 && (!order.expireTime || new Date(order.expireTime) > new Date())
}

const canCancel = (order: OrderDetail) => {
  return order.status === 0 && (!order.expireTime || new Date(order.expireTime) > new Date())
}

const getOrderDisplayName = (order: OrderDetail) => {
  if (order.productName && order.productName !== 'VIP会员') return order.productName
  if (order.orderType === 1) return 'VIP会员'
  if (order.orderType === 2) return 'SVIP年卡'
  if (order.orderType === 3) return '积分兑换商品'
  return '商品'
}

const getProductMetaText = (order: OrderDetail): string[] => {
  const meta: string[] = []
  if (order.quantity) meta.push(`数量: ${order.quantity}`)
  if (order.orderType === 1) meta.push('类型: VIP会员')
  else if (order.orderType === 2) meta.push('类型: SVIP年卡')
  else if (order.orderType === 3) meta.push('类型: 积分兑换')
  if (order.status === 0 && !order.paymentMethodName) meta.push('状态: 待付款')
  if (order.status === 4) meta.push('状态: 已关闭')
  if (order.paymentMethodName && [1, 2].includes(order.status)) meta.push(`支付方式: ${order.paymentMethodName}`)
  return meta
}

const getCountdown = (order: OrderDetail) => {
  if (!order.expireTime || order.status !== 0) return null
  if (!countdownMap.value[order.id]) updateCountdown(order)
  return countdownMap.value[order.id]
}

const updateCountdown = (order: OrderDetail) => {
  if (!order.expireTime) return
  const now = new Date().getTime()
  const end = new Date(order.expireTime).getTime()
  const diff = end - now

  if (diff <= 0) {
    countdownMap.value[order.id] = { hours: 0, minutes: 0, seconds: 0, expired: true }
    return
  }

  const hours = Math.floor(diff / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  const seconds = Math.floor((diff % (1000 * 60)) / 1000)
  countdownMap.value[order.id] = { hours, minutes, seconds, expired: false }
}

let countdownTimer: ReturnType<typeof setInterval> | null = null

const fetchOrders = async () => {
  loading.value = true
  try {
    const res = await getUserOrders({
      status: currentStatus.value,
      page: page.value,
      size: size.value
    })
    orders.value = res.records
    total.value = res.total
    orders.value.forEach(order => {
      if (order.status === 0 && order.expireTime) {
        updateCountdown(order)
      }
    })
  } catch (error) {
    console.error('获取订单列表失败', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

const handleStatusChange = (status: number | undefined) => {
  currentStatus.value = status
  page.value = 1
  fetchOrders()
}

const handlePageChange = (newPage: number) => {
  page.value = newPage
  fetchOrders()
}

const handleCancel = async (order: OrderDetail) => {
  try {
    await cancelOrder(order.id, { reason: '用户主动取消' })
    ElMessage.success('订单已取消')
    fetchOrders()
  } catch (error: any) {
    ElMessage.error(error.message || '取消失败')
  }
}

const viewOrderDetail = (orderId: number) => {
  router.push(`/orders/${orderId}`)
}

const getStatusClass = (status: number) => {
  switch (status) {
    case 0: return 'status-pending'
    case 1: return 'status-processing'
    case 2: return 'status-shipping'
    case 3: return 'status-completed'
    case 4: return 'status-closed'
    default: return ''
  }
}

const getOrderTypeLabel = (type: number) => {
  switch (type) {
    case 1: return 'VIP购买'
    case 2: return 'VIP购买'
    case 3: return '积分兑换'
    default: return '普通商品'
  }
}

const isPointsOrder = (order: OrderDetail) => {
  return order.orderType === 3
}

onMounted(() => {
  fetchOrders()
  countdownTimer = setInterval(() => {
    orders.value.forEach(order => {
      if (order.status === 0 && order.expireTime) {
        updateCountdown(order)
      }
    })
  }, 1000)
})

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
})
</script>

<template>
  <UserLayout>
    <div class="orders-page animate-fade-in">
      <div class="page-header">
        <h1>
          <List class="header-icon" />
          我的订单
        </h1>
      </div>

    <div class="status-tabs">
      <div 
        v-for="tab in statusTabs" 
        :key="tab.label"
        :class="['tab-item', { active: currentStatus === tab.value }]"
        @click="handleStatusChange(tab.value)"
      >
        {{ tab.label }}
      </div>
    </div>

    <div class="orders-list" v-loading="loading">
      <div v-if="orders.length === 0" class="empty-orders">
        <el-empty description="暂无订单">
          <el-button type="primary" @click="router.push('/products')">去购物</el-button>
        </el-empty>
      </div>

      <div v-else class="order-card" v-for="order in orders" :key="order.id">
        <div class="order-header">
          <div class="order-info">
            <span class="order-no">订单号: {{ order.orderNo }}</span>
            <el-tag size="small" :class="getStatusClass(order.status)">
              {{ order.statusName }}
            </el-tag>
            <el-tag v-if="order.orderType !== 0" size="small" type="info">
              {{ getOrderTypeLabel(order.orderType) }}
            </el-tag>
          </div>
          <div class="order-time">
            <el-icon><Clock /></el-icon>
            {{ formatDateTime(order.createTime) }}
          </div>
        </div>

        <div class="order-body" @click="viewOrderDetail(order.id)">
          <div class="product-info">
            <div class="product-image" v-if="order.productImage">
              <img :src="order.productImage" :alt="order.productName" />
            </div>
            <div class="product-detail">
              <h3 class="product-name">{{ getOrderDisplayName(order) }}</h3>
              <p class="product-meta">
                <span v-for="(meta, idx) in getProductMetaText(order)" :key="idx">{{ meta }}</span>
              </p>
            </div>
          </div>

          <div class="order-amount">
            <div v-if="isPointsOrder(order)" class="points-amount">
              <span class="amount-label">积分</span>
              <span class="amount-value">{{ order.pointsAmount }}</span>
            </div>
            <div v-else class="money-amount">
              <span class="amount-value">{{ formatMoney(order.payAmount) }}</span>
            </div>
          </div>
        </div>

        <div class="order-footer">
          <div class="expire-tip" v-if="order.status === 0 && order.expireTime">
            <el-icon><Clock /></el-icon>
            <template v-if="getCountdown(order) && !getCountdown(order)!.expired">
              剩余 {{ getCountdown(order)!.hours }}小时{{ getCountdown(order)!.minutes }}分钟{{ getCountdown(order)!.seconds > 0 ? getCountdown(order)!.seconds + '秒' : '' }} 自动取消订单
            </template>
            <template v-else>
              订单已过期
            </template>
          </div>
          
          <div class="order-actions">
            <el-button 
              v-if="canCancel(order)" 
              type="info" 
              plain 
              size="small"
              @click.stop="handleCancel(order)"
            >
              取消订单
            </el-button>
            <el-button 
              v-if="canPay(order)" 
              type="primary" 
              size="small"
              @click.stop="viewOrderDetail(order.id)"
            >
              立即支付
            </el-button>
            <el-button 
              v-if="order.status === 2" 
              type="success" 
              plain 
              size="small"
            >
              确认收货
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <div class="pagination-wrapper" v-if="total > size">
      <el-pagination
        :current-page="page"
        :page-size="size"
        :total="total"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>

    </div>
  </UserLayout>
</template>

<style scoped>
.orders-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-icon {
  width: 28px;
  height: 28px;
  color: #1677ff;
}

.header-icon :deep(svg) {
  width: 28px;
  height: 28px;
}

.status-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  padding: 12px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.tab-item {
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #6b7280;
  transition: all 0.2s;
}

.tab-item:hover {
  background: #f3f4f6;
}

.tab-item.active {
  background: #1677ff;
  color: #fff;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.empty-orders {
  padding: 60px 0;
  background: #fff;
  border-radius: 12px;
}

.order-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
}

.order-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.order-no {
  font-size: 14px;
  color: #374151;
}

.order-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #9ca3af;
}

.order-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  cursor: pointer;
  transition: background 0.2s;
}

.order-body:hover {
  background: #f9fafb;
}

.product-info {
  display: flex;
  gap: 12px;
  flex: 1;
}

.product-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-detail {
  flex: 1;
}

.product-name {
  font-size: 15px;
  font-weight: 500;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.product-meta {
  font-size: 13px;
  color: #6b7280;
  margin: 0;
}

.product-meta span {
  margin-right: 16px;
}

.order-amount {
  text-align: right;
}

.amount-value {
  font-size: 20px;
  font-weight: 600;
  color: #ff4d4f;
}

.points-amount .amount-value {
  color: #faad14;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-top: 1px solid #e5e7eb;
}

.expire-tip {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #f56c6c;
}

.order-actions {
  display: flex;
  gap: 8px;
}

.status-pending {
  color: #faad14;
  background: #fffbe6;
  border-color: #ffe58f;
}

.status-processing {
  color: #1677ff;
  background: #e6f4ff;
  border-color: #91caff;
}

.status-shipping {
  color: #52c41a;
  background: #f6ffed;
  border-color: #b7eb8f;
}

.status-completed {
  color: #52c41a;
  background: #f6ffed;
  border-color: #b7eb8f;
}

.status-closed {
  color: #9ca3af;
  background: #f3f4f6;
  border-color: #e5e7eb;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
