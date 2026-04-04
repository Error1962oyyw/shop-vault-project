<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import UserLayout from '@/components/layout/UserLayout.vue'
import { payOrder, getOrderDetail } from '@/api/order'
import type { OrderDetail } from '@/types/api'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const paying = ref(false)
const order = ref<OrderDetail | null>(null)
const countdown = ref(900)

const orderId = computed(() => Number(route.params.orderNo))

const formatTime = computed(() => {
  const minutes = Math.floor(countdown.value / 60)
  const seconds = countdown.value % 60
  return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
})

const fetchOrder = async () => {
  try {
    order.value = await getOrderDetail(orderId.value)
  } catch (error) {
    console.error('获取订单失败', error)
    ElMessage.error('订单不存在')
    router.push('/orders')
  } finally {
    loading.value = false
  }
}

const handlePay = async () => {
  paying.value = true
  try {
    await payOrder(orderId.value, { paymentMethod: 'BALANCE' })
    ElMessage.success('支付成功')
    router.push(`/order/success/${route.params.orderNo}`)
  } catch (error) {
    console.error('支付失败', error)
  } finally {
    paying.value = false
  }
}

const handleCancel = () => {
  router.push('/orders')
}

onMounted(() => {
  fetchOrder()
  
  const timer = setInterval(() => {
    if (countdown.value > 0) {
      countdown.value--
    } else {
      clearInterval(timer)
      ElMessage.warning('订单已超时')
      router.push('/orders')
    }
  }, 1000)
})
</script>

<template>
  <UserLayout>
    <div class="bg-gray-50 min-h-screen">
      <div class="page-container py-12">
        <div v-loading="loading" class="max-w-2xl mx-auto">
          <div v-if="order" class="bg-white rounded-lg shadow-lg p-8">
            <div class="text-center mb-8">
              <div class="w-20 h-20 mx-auto mb-4 rounded-full bg-blue-100 flex items-center justify-center">
                <el-icon size="40" class="text-blue-500"><Clock /></el-icon>
              </div>
              <h1 class="text-2xl font-bold mb-2">订单待支付</h1>
              <p class="text-gray-500">
                请在 <span class="text-red-500 font-bold">{{ formatTime }}</span> 内完成支付
              </p>
            </div>

            <div class="border-t border-b border-gray-100 py-6 mb-6">
              <div class="flex justify-between mb-4">
                <span class="text-gray-500">订单编号</span>
                <span class="font-medium">{{ order.orderNo }}</span>
              </div>
              <div class="flex justify-between mb-4">
                <span class="text-gray-500">收货人</span>
                <span>{{ order.receiverName }} {{ order.receiverPhone }}</span>
              </div>
              <div class="flex justify-between mb-4">
                <span class="text-gray-500">收货地址</span>
                <span>{{ order.receiverAddress }}</span>
              </div>
            </div>

            <div class="mb-6">
              <h3 class="font-medium mb-4">商品信息</h3>
              <div class="flex items-center gap-4">
                <img
                  :src="order.productImage"
                  :alt="order.productName"
                  class="w-16 h-16 rounded object-cover"
                />
                <div class="flex-1 min-w-0">
                  <h4 class="font-medium line-clamp-1">{{ order.productName }}</h4>
                  <p class="text-gray-500 text-sm">¥{{ order.productPrice.toFixed(2) }} × {{ order.quantity }}</p>
                </div>
                <span class="text-red-500 font-bold">¥{{ order.totalAmount.toFixed(2) }}</span>
              </div>
            </div>

            <div class="border-t border-gray-100 pt-6 mb-6">
              <div class="flex justify-between items-baseline mb-2">
                <span class="text-gray-500">商品金额</span>
                <span>¥{{ order.totalAmount.toFixed(2) }}</span>
              </div>
              <div v-if="order.pointsAmount > 0" class="flex justify-between items-baseline mb-2">
                <span class="text-gray-500">积分抵扣</span>
                <span class="text-red-500">-¥{{ order.pointsAmount.toFixed(2) }}</span>
              </div>
              <div class="flex justify-between items-baseline text-xl">
                <span class="font-bold">应付金额</span>
                <span class="text-red-500 font-bold">¥{{ order.payAmount.toFixed(2) }}</span>
              </div>
            </div>

            <div class="flex gap-4">
              <el-button 
                size="large" 
                class="flex-1"
                @click="handleCancel"
              >
                取消支付
              </el-button>
              <el-button 
                type="primary" 
                size="large" 
                class="flex-1"
                :loading="paying"
                @click="handlePay"
              >
                立即支付 ¥{{ order.payAmount.toFixed(2) }}
              </el-button>
            </div>

            <div class="mt-6 p-4 bg-gray-50 rounded-lg">
              <p class="text-sm text-gray-500 text-center">
                这是一个模拟支付功能，点击"立即支付"即可完成订单
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </UserLayout>
</template>
