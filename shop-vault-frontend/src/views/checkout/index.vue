<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import UserLayout from '@/components/layout/UserLayout.vue'
import { getAddressList } from '@/api/user'
import { getCartList } from '@/api/cart'
import { cartCheckout, buyNow } from '@/api/order'
import { getApplicableCoupons, getBestCoupon, getCurrentMemberDay } from '@/api/marketing'
import { useUserStore } from '@/stores/user'
import type { Address, CartItem, UserCoupon, MemberDay } from '@/types/api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

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

const cartIds = computed(() => {
  const ids = route.query.cartIds as string
  return ids ? ids.split(',').map(Number) : []
})

const productId = computed(() => route.query.productId ? Number(route.query.productId) : null)
const quantity = computed(() => route.query.quantity ? Number(route.query.quantity) : 1)

const productAmount = computed(() => {
  return cartItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
})

const freightAmount = computed(() => {
  return productAmount.value >= 99 ? 0 : 10
})

const discountAmount = computed(() => {
  if (selectedCoupon.value) {
    if (productAmount.value >= selectedCoupon.value.minAmount) {
      return selectedCoupon.value.value
    }
  }
  return 0
})

const memberDayDiscount = computed(() => {
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
  loading.value = true
  try {
    if (productId.value) {
      const allItems = await getCartList()
      cartItems.value = allItems.filter(item => item.productId === productId.value)
      if (cartItems.value.length > 0) {
        const item = cartItems.value[0]
        cartItems.value = [{
          id: item.id,
          productId: item.productId,
          productName: item.productName,
          productImage: item.productImage,
          price: item.price,
          quantity: quantity.value,
          stock: item.stock,
          selected: true
        }]
      }
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

const handleSubmit = async () => {
  if (!selectedAddress.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  
  submitting.value = true
  try {
    let orderNo: string
    if (productId.value) {
      orderNo = await buyNow({ productId: productId.value, quantity: quantity.value })
    } else {
      orderNo = await cartCheckout({
        cartItemIds: cartIds.value,
        addressId: selectedAddress.value.id,
        remark: remark.value,
        userCouponId: selectedCoupon.value?.id
      })
    }
    
    ElMessage.success('订单创建成功')
    router.push(`/order/pay/${orderNo}`)
  } catch (error) {
    console.error('提交订单失败', error)
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  if (!userStore.token) {
    router.push('/login')
    return
  }
  await fetchAddresses()
  await fetchCartItems()
  await Promise.all([
    fetchCoupons(),
    fetchMemberDay()
  ])
})
</script>

<template>
  <UserLayout>
    <div class="bg-gray-50 min-h-screen">
      <div class="page-container py-6">
        <h1 class="text-xl font-bold mb-6">确认订单</h1>

        <div v-loading="loading" class="grid grid-cols-3 gap-6">
          <div class="col-span-2 space-y-6">
            <div class="bg-white rounded-lg p-6">
              <h2 class="font-bold text-lg mb-4 flex items-center gap-2">
                <el-icon><Location /></el-icon>
                收货地址
              </h2>
              
              <template v-if="addresses.length > 0">
                <div class="grid grid-cols-2 gap-4">
                  <div 
                    v-for="address in addresses" 
                    :key="address.id"
                    class="border rounded-lg p-4 cursor-pointer transition-all"
                    :class="selectedAddress?.id === address.id ? 'border-blue-500 bg-blue-50' : 'border-gray-200 hover:border-blue-300'"
                    @click="selectedAddress = address"
                  >
                    <div class="flex items-start justify-between mb-2">
                      <div>
                        <span class="font-medium">{{ address.receiverName }}</span>
                        <span class="ml-2 text-gray-500">{{ address.receiverPhone }}</span>
                      </div>
                      <el-tag v-if="address.isDefault === 1" type="success" size="small">默认</el-tag>
                    </div>
                    <div class="text-gray-500 text-sm">
                      {{ address.province }}{{ address.city }}{{ address.region }}{{ address.detailAddress }}
                    </div>
                  </div>
                </div>
                <div class="mt-4">
                  <el-button type="primary" link @click="router.push('/profile')">
                    管理收货地址
                  </el-button>
                </div>
              </template>
              
              <el-empty v-else description="暂无收货地址">
                <el-button type="primary" @click="router.push('/profile')">
                  添加地址
                </el-button>
              </el-empty>
            </div>

            <div class="bg-white rounded-lg p-6">
              <h2 class="font-bold text-lg mb-4 flex items-center gap-2">
                <el-icon><Goods /></el-icon>
                商品清单
              </h2>
              
              <div class="divide-y divide-gray-100">
                <div 
                  v-for="item in cartItems" 
                  :key="item.id"
                  class="flex items-center gap-4 py-4"
                >
                  <img 
                    :src="item.productImage" 
                    :alt="item.productName"
                    class="w-20 h-20 rounded object-cover"
                  />
                  <div class="flex-1 min-w-0">
                    <h3 class="font-medium line-clamp-2">{{ item.productName }}</h3>
                  </div>
                  <div class="text-gray-500">¥{{ item.price.toFixed(2) }} × {{ item.quantity }}</div>
                  <div class="text-red-500 font-bold w-24 text-right">
                    ¥{{ (item.price * item.quantity).toFixed(2) }}
                  </div>
                </div>
              </div>
            </div>

            <div class="bg-white rounded-lg p-6">
              <h2 class="font-bold text-lg mb-4 flex items-center gap-2">
                <el-icon><Ticket /></el-icon>
                优惠券
              </h2>
              
              <div v-if="selectedCoupon" class="flex items-center justify-between p-4 bg-orange-50 rounded-lg">
                <div class="flex items-center gap-3">
                  <div class="w-12 h-12 bg-orange-500 rounded flex items-center justify-center text-white font-bold">
                    {{ formatCouponValue(selectedCoupon) }}
                  </div>
                  <div>
                    <div class="font-medium">{{ selectedCoupon.couponName }}</div>
                    <div class="text-sm text-gray-500">满{{ selectedCoupon.minAmount }}可用</div>
                  </div>
                </div>
                <el-button type="danger" link @click="removeCoupon">
                  不使用
                </el-button>
              </div>
              
              <div v-else-if="applicableCoupons.length > 0" class="flex items-center justify-between">
                <div class="text-gray-500">
                  有 {{ applicableCoupons.length }} 张可用优惠券
                </div>
                <el-button type="primary" link @click="showCouponDialog = true">
                  选择优惠券
                </el-button>
              </div>
              
              <div v-else class="text-gray-400">
                暂无可用优惠券
              </div>
            </div>

            <div class="bg-white rounded-lg p-6">
              <h2 class="font-bold text-lg mb-4">备注</h2>
              <el-input 
                v-model="remark"
                type="textarea"
                :rows="3"
                placeholder="选填，可以告诉卖家您的特殊需求"
              />
            </div>
          </div>

          <div class="col-span-1">
            <div class="bg-white rounded-lg p-6 sticky top-6">
              <h2 class="font-bold text-lg mb-4">订单结算</h2>
              
              <div class="space-y-3 text-sm">
                <div class="flex justify-between">
                  <span class="text-gray-500">商品金额</span>
                  <span>¥{{ productAmount.toFixed(2) }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-gray-500">运费</span>
                  <span v-if="freightAmount === 0" class="text-green-500">免运费</span>
                  <span v-else>¥{{ freightAmount.toFixed(2) }}</span>
                </div>
                
                <div v-if="memberDayDiscount > 0" class="flex justify-between text-orange-500">
                  <span>会员日优惠</span>
                  <span>-¥{{ memberDayDiscount.toFixed(2) }}</span>
                </div>
                
                <div v-if="discountAmount > 0" class="flex justify-between text-orange-500">
                  <span>优惠券抵扣</span>
                  <span>-¥{{ discountAmount.toFixed(2) }}</span>
                </div>
                
                <el-divider />
                
                <div class="flex justify-between items-baseline">
                  <span class="text-gray-500">应付金额</span>
                  <span class="text-2xl font-bold text-red-500">¥{{ totalAmount.toFixed(2) }}</span>
                </div>
              </div>
              
              <div class="mt-6 space-y-3">
                <el-button 
                  type="danger" 
                  size="large"
                  class="w-full"
                  :loading="submitting"
                  :disabled="!selectedAddress || cartItems.length === 0"
                  @click="handleSubmit"
                >
                  提交订单
                </el-button>
                
                <div class="text-center text-xs text-gray-400">
                  提交订单即表示您同意<a href="#" class="text-blue-500">《服务协议》</a>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog 
      v-model="showCouponDialog" 
      title="选择优惠券" 
      width="500px"
    >
      <div class="max-h-96 overflow-y-auto space-y-3">
        <div 
          v-for="coupon in applicableCoupons" 
          :key="coupon.id"
          class="coupon-item flex overflow-hidden rounded-lg border cursor-pointer transition-all"
          :class="[
            selectedCoupon?.id === coupon.id ? 'border-orange-500' : 'border-gray-200 hover:border-orange-300',
            { 'opacity-50': productAmount < coupon.minAmount }
          ]"
          @click="selectCoupon(coupon)"
        >
          <div class="w-24 bg-gradient-to-r from-orange-500 to-red-500 flex flex-col items-center justify-center text-white p-2">
            <span class="text-xl font-bold">{{ formatCouponValue(coupon) }}</span>
            <span class="text-xs opacity-80">满{{ coupon.minAmount }}可用</span>
          </div>
          <div class="flex-1 p-3">
            <div class="font-medium">{{ coupon.couponName }}</div>
            <div class="text-xs text-gray-400 mt-1">
              {{ coupon.startTime?.split('T')[0] }} - {{ coupon.endTime?.split('T')[0] }}
            </div>
          </div>
          <div v-if="selectedCoupon?.id === coupon.id" class="flex items-center pr-3">
            <el-icon class="text-orange-500" size="20"><CircleCheck /></el-icon>
          </div>
        </div>
        
        <el-empty v-if="applicableCoupons.length === 0" description="暂无可用优惠券" />
      </div>
      
      <template #footer>
        <el-button @click="showCouponDialog = false">取消</el-button>
        <el-button type="primary" @click="showCouponDialog = false">确定</el-button>
      </template>
    </el-dialog>
  </UserLayout>
</template>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
