<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import UserLayout from '@/components/layout/UserLayout.vue'
import { 
  getAvailableCoupons, 
  getMyCoupons, 
  receiveCoupon 
} from '@/api/marketing'
import { ElMessage } from 'element-plus'
import type { CouponTemplate, UserCoupon } from '@/types/api'

const activeTab = ref('available')
const availableCoupons = ref<CouponTemplate[]>([])
const myCoupons = ref<UserCoupon[]>([])
const myCouponStatus = ref<number | undefined>(undefined)
const loading = ref(false)

const couponStatusOptions = [
  { label: '全部', value: undefined },
  { label: '未使用', value: 0 },
  { label: '已使用', value: 1 },
  { label: '已过期', value: 2 }
]

const filteredMyCoupons = computed(() => {
  if (myCouponStatus.value === undefined) {
    return myCoupons.value
  }
  return myCoupons.value.filter(c => c.status === myCouponStatus.value)
})

const fetchAvailableCoupons = async () => {
  try {
    availableCoupons.value = await getAvailableCoupons()
  } catch (error) {
    console.error('获取可领取优惠券失败', error)
  }
}

const fetchMyCoupons = async () => {
  try {
    myCoupons.value = await getMyCoupons()
  } catch (error) {
    console.error('获取我的优惠券失败', error)
  }
}

const handleReceiveCoupon = async (coupon: CouponTemplate) => {
  try {
    await receiveCoupon(coupon.id)
    ElMessage.success('领取成功！')
    await fetchAvailableCoupons()
    await fetchMyCoupons()
  } catch (error) {
    console.error('领取优惠券失败', error)
  }
}

const getCouponTypeText = (type: number) => {
  switch (type) {
    case 1: return '满减券'
    case 2: return '折扣券'
    case 3: return '无门槛券'
    default: return '优惠券'
  }
}

const getCouponTypeColor = (type: number) => {
  switch (type) {
    case 1: return 'bg-gradient-to-r from-red-500 to-orange-500'
    case 2: return 'bg-gradient-to-r from-purple-500 to-pink-500'
    case 3: return 'bg-gradient-to-r from-blue-500 to-cyan-500'
    default: return 'bg-gradient-to-r from-gray-500 to-gray-600'
  }
}

const getStatusText = (status: number) => {
  switch (status) {
    case 0: return '未使用'
    case 1: return '已使用'
    case 2: return '已过期'
    default: return '未知'
  }
}

const getStatusClass = (status: number) => {
  switch (status) {
    case 0: return 'bg-green-100 text-green-600'
    case 1: return 'bg-gray-100 text-gray-500'
    case 2: return 'bg-red-100 text-red-500'
    default: return 'bg-gray-100 text-gray-500'
  }
}

const formatDate = (date: string) => {
  return date ? date.split('T')[0] : ''
}

onMounted(() => {
  loading.value = true
  Promise.all([
    fetchAvailableCoupons(),
    fetchMyCoupons()
  ]).finally(() => {
    loading.value = false
  })
})
</script>

<template>
  <UserLayout>
    <div class="bg-gray-50 min-h-screen py-6">
      <div class="page-container">
        <div class="bg-white rounded-lg shadow-sm">
          <div class="p-6 border-b">
            <h1 class="text-2xl font-bold text-gray-800">优惠券中心</h1>
            <p class="text-gray-500 mt-1">领取优惠券，享受更多优惠</p>
          </div>

          <el-tabs v-model="activeTab" class="px-6 pt-4">
            <el-tab-pane label="可领取" name="available">
              <div v-loading="loading" class="grid grid-cols-2 gap-4 pb-6">
                <div 
                  v-for="coupon in availableCoupons" 
                  :key="coupon.id"
                  class="coupon-card flex overflow-hidden rounded-lg shadow-sm hover:shadow-md transition-shadow"
                >
                  <div 
                    class="w-32 flex flex-col items-center justify-center text-white p-4"
                    :class="getCouponTypeColor(coupon.type)"
                  >
                    <span class="text-sm">{{ getCouponTypeText(coupon.type) }}</span>
                    <div class="flex items-baseline mt-2">
                      <span class="text-2xl font-bold">¥</span>
                      <span class="text-4xl font-bold">{{ coupon.value }}</span>
                    </div>
                    <span v-if="coupon.minAmount > 0" class="text-xs mt-1 opacity-80">
                      满{{ coupon.minAmount }}可用
                    </span>
                  </div>
                  <div class="flex-1 p-4 flex flex-col justify-between">
                    <div>
                      <h3 class="font-bold text-gray-800">{{ coupon.name }}</h3>
                      <p v-if="coupon.description" class="text-sm text-gray-500 mt-1">
                        {{ coupon.description }}
                      </p>
                      <div class="text-xs text-gray-400 mt-2">
                        {{ formatDate(coupon.startTime) }} - {{ formatDate(coupon.endTime) }}
                      </div>
                    </div>
                    <div class="flex items-center justify-between mt-3">
                      <span class="text-xs text-gray-400">
                        剩余 {{ coupon.totalCount - coupon.usedCount }} 张
                      </span>
                      <el-button 
                        type="primary" 
                        size="small"
                        :disabled="coupon.totalCount - coupon.usedCount <= 0"
                        @click="handleReceiveCoupon(coupon)"
                      >
                        立即领取
                      </el-button>
                    </div>
                  </div>
                </div>

                <el-empty 
                  v-if="!loading && availableCoupons.length === 0" 
                  description="暂无可领取的优惠券"
                  class="col-span-2 py-12"
                />
              </div>
            </el-tab-pane>

            <el-tab-pane label="我的优惠券" name="my">
              <div class="pb-6">
                <div class="flex gap-2 mb-4">
                  <el-radio-group v-model="myCouponStatus" size="small">
                    <el-radio-button 
                      v-for="option in couponStatusOptions" 
                      :key="option.label"
                      :value="option.value"
                    >
                      {{ option.label }}
                    </el-radio-button>
                  </el-radio-group>
                </div>

                <div v-loading="loading" class="grid grid-cols-2 gap-4">
                  <div 
                    v-for="coupon in filteredMyCoupons" 
                    :key="coupon.id"
                    class="coupon-card flex overflow-hidden rounded-lg shadow-sm"
                    :class="{ 'opacity-60': coupon.status !== 0 }"
                  >
                    <div 
                      class="w-32 flex flex-col items-center justify-center text-white p-4 relative"
                      :class="getCouponTypeColor(coupon.type)"
                    >
                      <span class="text-sm">{{ getCouponTypeText(coupon.type) }}</span>
                      <div class="flex items-baseline mt-2">
                        <span class="text-2xl font-bold">¥</span>
                        <span class="text-4xl font-bold">{{ coupon.value }}</span>
                      </div>
                      <span v-if="coupon.minAmount > 0" class="text-xs mt-1 opacity-80">
                        满{{ coupon.minAmount }}可用
                      </span>
                      <div 
                        v-if="coupon.status !== 0"
                        class="absolute inset-0 flex items-center justify-center bg-black/30"
                      >
                        <span class="text-white text-lg font-bold">{{ getStatusText(coupon.status) }}</span>
                      </div>
                    </div>
                    <div class="flex-1 p-4 flex flex-col justify-between">
                      <div>
                        <h3 class="font-bold text-gray-800">{{ coupon.couponName }}</h3>
                        <div class="text-xs text-gray-400 mt-2">
                          {{ formatDate(coupon.startTime) }} - {{ formatDate(coupon.endTime) }}
                        </div>
                      </div>
                      <div class="flex items-center justify-between mt-3">
                        <span 
                          class="text-xs px-2 py-1 rounded"
                          :class="getStatusClass(coupon.status)"
                        >
                          {{ getStatusText(coupon.status) }}
                        </span>
                        <el-button 
                          v-if="coupon.status === 0"
                          type="primary" 
                          size="small"
                          @click="$router.push('/products')"
                        >
                          去使用
                        </el-button>
                      </div>
                    </div>
                  </div>

                  <el-empty 
                    v-if="!loading && filteredMyCoupons.length === 0" 
                    description="暂无优惠券"
                    class="col-span-2 py-12"
                  />
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>
  </UserLayout>
</template>

<style scoped>
.coupon-card {
  position: relative;
}

.coupon-card::before,
.coupon-card::after {
  content: '';
  position: absolute;
  width: 16px;
  height: 16px;
  background: #f9fafb;
  border-radius: 50%;
  left: 120px;
}

.coupon-card::before {
  top: -8px;
}

.coupon-card::after {
  bottom: -8px;
}
</style>
