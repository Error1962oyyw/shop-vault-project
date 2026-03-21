<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import UserLayout from '@/components/layout/UserLayout.vue'
import { getOrderList, cancelOrder, receiveOrder, extendOrder } from '@/api/order'
import type { Order } from '@/types/api'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const orders = ref<Order[]>([])
const activeTab = ref('all')

const tabs = [
  { label: '全部订单', value: 'all', status: undefined },
  { label: '待付款', value: 'unpaid', status: 0 },
  { label: '待发货', value: 'unshipped', status: 1 },
  { label: '待收货', value: 'unreceived', status: 2 },
  { label: '已完成', value: 'completed', status: 3 },
  { label: '已取消', value: 'cancelled', status: 4 }
]

const currentStatus = computed(() => {
  const tab = tabs.find(t => t.value === activeTab.value)
  return tab?.status
})

const fetchOrders = async () => {
  loading.value = true
  try {
    const res = await getOrderList({ status: currentStatus.value })
    orders.value = res || []
  } catch (error: any) {
    console.error('获取订单失败', error)
    ElMessage.error('获取订单失败')
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  fetchOrders()
}

const handleCancel = async (orderNo: string) => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await cancelOrder(orderNo)
    ElMessage.success('订单已取消')
    fetchOrders()
  } catch (error) {
  }
}

const handlePay = (orderNo: string) => {
  router.push(`/order/pay/${orderNo}`)
}

const handleReceive = async (orderNo: string) => {
  try {
    await ElMessageBox.confirm('确认已收到货物吗？', '确认收货', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    await receiveOrder(orderNo)
    ElMessage.success('已确认收货')
    fetchOrders()
  } catch (error) {
  }
}

const handleExtend = async (orderNo: string) => {
  try {
    await extendOrder(orderNo)
    ElMessage.success('已延长收货时间')
  } catch (error) {
    console.error('延长收货失败', error)
  }
}

const getStatusText = (status: number) => {
  const texts: Record<number, string> = {
    0: '待付款',
    1: '待发货',
    2: '待收货',
    3: '已完成',
    4: '已取消',
    5: '售后中'
  }
  return texts[status] || '未知'
}

const getStatusColor = (status: number): 'warning' | 'primary' | 'success' | 'info' | 'danger' => {
  const colors: Record<number, 'warning' | 'primary' | 'success' | 'info' | 'danger'> = {
    0: 'warning',
    1: 'primary',
    2: 'primary',
    3: 'success',
    4: 'info',
    5: 'danger'
  }
  return colors[status] || 'info'
}

const goToDetail = (orderNo: string) => {
  router.push(`/order/detail/${orderNo}`)
}

onMounted(() => {
  if (route.query.status) {
    activeTab.value = route.query.status as string
  }
  fetchOrders()
})
</script>

<template>
  <UserLayout>
    <div class="orders-page">
      <div class="page-container">
        <div class="orders-card">
          <el-tabs v-model="activeTab" class="orders-tabs" @tab-change="handleTabChange">
            <el-tab-pane 
              v-for="tab in tabs" 
              :key="tab.value"
              :label="tab.label"
              :name="tab.value"
            />
          </el-tabs>

          <div v-loading="loading" class="orders-content">
            <template v-if="orders.length > 0">
              <div class="orders-list">
                <div 
                  v-for="order in orders" 
                  :key="order.orderNo"
                  class="order-item"
                >
                  <div class="order-header">
                    <div class="order-info">
                      <span class="info-item">订单编号：{{ order.orderNo }}</span>
                      <span class="info-item">{{ order.createTime }}</span>
                    </div>
                    <el-tag :type="getStatusColor(order.status)" size="small" class="status-tag">
                      {{ getStatusText(order.status) }}
                    </el-tag>
                  </div>

                  <div class="order-body">
                    <div 
                      v-for="item in order.items" 
                      :key="item.id"
                      class="order-product"
                    >
                      <img 
                        :src="item.productImage" 
                        :alt="item.productName"
                        class="product-image"
                        @click="goToDetail(order.orderNo)"
                      />
                      <div class="product-info">
                        <h3 
                          class="product-name"
                          @click="goToDetail(order.orderNo)"
                        >
                          {{ item.productName }}
                        </h3>
                        <p class="product-price">
                          ¥{{ item.price.toFixed(2) }} × {{ item.quantity }}
                        </p>
                      </div>
                      <div class="product-total">
                        <span class="total-price">
                          ¥{{ item.totalAmount.toFixed(2) }}
                        </span>
                      </div>
                    </div>
                  </div>

                  <div class="order-footer">
                    <div class="receiver-info">
                      收货人：{{ order.receiverName }} {{ order.receiverPhone }}
                    </div>
                    <div class="order-summary">
                      <span class="summary-text">
                        共 {{ order.items?.length || 0 }} 件商品，实付
                        <span class="pay-amount">
                          ¥{{ order.payAmount?.toFixed(2) || '0.00' }}
                        </span>
                      </span>
                      
                      <div class="order-actions">
                        <template v-if="order.status === 0">
                          <el-button size="small" @click="handleCancel(order.orderNo)">
                            取消订单
                          </el-button>
                          <el-button type="primary" size="small" @click="handlePay(order.orderNo)">
                            去支付
                          </el-button>
                        </template>
                        
                        <template v-else-if="order.status === 2">
                          <el-button size="small" @click="handleExtend(order.orderNo)">
                            延长收货
                          </el-button>
                          <el-button type="primary" size="small" @click="handleReceive(order.orderNo)">
                            确认收货
                          </el-button>
                        </template>
                        
                        <el-button size="small" @click="goToDetail(order.orderNo)">
                          订单详情
                        </el-button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </template>

            <el-empty v-else description="暂无订单" class="empty-state">
              <el-button type="primary" @click="router.push('/products')">
                去购物
              </el-button>
            </el-empty>
          </div>
        </div>
      </div>
    </div>
  </UserLayout>
</template>

<style scoped>
.orders-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f0f5ff 0%, #e6f7ff 50%, #f5f7fa 100%);
  padding: 24px 0;
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.orders-card {
  background: #ffffff;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

.orders-tabs {
  padding: 8px 32px 0;
}

.orders-content {
  padding: 32px;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.order-item {
  border: 1px solid #e5e7eb;
  border-radius: var(--radius-md);
  overflow: hidden;
  transition: all 0.3s ease;
}

.order-item:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.order-header {
  background: linear-gradient(135deg, #f9fafb 0%, #f3f4f6 100%);
  padding: 16px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e5e7eb;
}

.order-info {
  display: flex;
  align-items: center;
  gap: 24px;
}

.info-item {
  color: #6b7280;
  font-size: 14px;
}

.status-tag {
  font-weight: 500;
}

.order-body {
  padding: 24px;
}

.order-product {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.order-product:last-child {
  margin-bottom: 0;
}

.product-image {
  width: 96px;
  height: 96px;
  border-radius: var(--radius-sm);
  object-fit: cover;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.product-image:hover {
  transform: scale(1.05);
}

.product-info {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 8px;
  cursor: pointer;
  transition: color 0.3s ease;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-name:hover {
  color: var(--primary-color);
}

.product-price {
  color: #6b7280;
  font-size: 14px;
}

.product-total {
  text-align: right;
}

.total-price {
  color: #ef4444;
  font-weight: 700;
  font-size: 16px;
}

.order-footer {
  background: #fafafa;
  padding: 16px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-top: 1px solid #e5e7eb;
}

.receiver-info {
  color: #6b7280;
  font-size: 14px;
}

.order-summary {
  display: flex;
  align-items: center;
  gap: 24px;
}

.summary-text {
  color: #6b7280;
  font-size: 14px;
}

.pay-amount {
  color: #ef4444;
  font-weight: 700;
  font-size: 20px;
}

.order-actions {
  display: flex;
  gap: 12px;
}

.empty-state {
  padding: 60px 0;
}

@media (max-width: 1024px) {
  .order-footer {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .order-summary {
    width: 100%;
    justify-content: space-between;
  }
}

@media (max-width: 640px) {
  .orders-page {
    padding: 12px 0;
  }
  
  .orders-tabs {
    padding: 8px 20px 0;
  }
  
  .orders-content {
    padding: 20px;
  }
  
  .order-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .order-info {
    flex-direction: column;
    gap: 4px;
  }
  
  .order-product {
    flex-wrap: wrap;
  }
  
  .product-total {
    width: 100%;
    text-align: right;
  }
  
  .order-summary {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .order-actions {
    width: 100%;
    flex-wrap: wrap;
  }
  
  .order-actions .el-button {
    flex: 1;
    min-width: 0;
  }
}
</style>
