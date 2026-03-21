<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import UserLayout from '@/components/layout/UserLayout.vue'
import { signIn, getPointsRecords, getAvailableCoupons, receiveCoupon, getMyCoupons, todaySigned } from '@/api/marketing'
import { getProfile } from '@/api/user'
import type { PointsRecord, CouponTemplate, UserCoupon } from '@/types/api'

const activeTab = ref('points')
const userInfo = ref({ points: 0, memberLevel: 1 })
const pointsRecords = ref<PointsRecord[]>([])
const availableCoupons = ref<CouponTemplate[]>([])
const myCoupons = ref<UserCoupon[]>([])
const loading = ref(false)
const signInLoading = ref(false)
const hasSignedIn = ref(false)

const fetchUserInfo = async () => {
  try {
    const res = await getProfile()
    userInfo.value = {
      points: res.points || 0,
      memberLevel: res.memberLevel || 1
    }
  } catch (error) {
    console.error('获取用户信息失败', error)
  }
}

const checkTodaySigned = async () => {
  try {
    const signed = await todaySigned()
    hasSignedIn.value = signed
  } catch (error) {
    console.error('检查签到状态失败', error)
  }
}

const fetchPointsRecords = async () => {
  loading.value = true
  try {
    pointsRecords.value = await getPointsRecords()
  } catch (error) {
    console.error('获取积分记录失败', error)
    ElMessage.error('获取积分记录失败')
  } finally {
    loading.value = false
  }
}

const fetchCoupons = async () => {
  loading.value = true
  try {
    availableCoupons.value = await getAvailableCoupons()
    myCoupons.value = await getMyCoupons()
  } catch (error) {
    console.error('获取优惠券失败', error)
  } finally {
    loading.value = false
  }
}

const handleSignIn = async () => {
  if (hasSignedIn.value || signInLoading.value) return
  
  signInLoading.value = true
  try {
    const res = await signIn()
    ElMessage.success(`签到成功，获得${res.points}积分`)
    hasSignedIn.value = true
    await fetchUserInfo()
    await fetchPointsRecords()
  } catch (error: any) {
    const msg = error?.response?.data?.msg || error?.message || '签到失败'
    if (msg.includes('已签到')) {
      hasSignedIn.value = true
    }
    ElMessage.error(msg)
  } finally {
    signInLoading.value = false
  }
}

const handleClaimCoupon = async (couponId: number) => {
  try {
    await receiveCoupon(couponId)
    ElMessage.success('领取成功')
    await fetchCoupons()
  } catch (error) {
    console.error('领取失败', error)
  }
}

const getPointsTypeText = (type: string) => {
  const types: Record<string, string> = {
    'SIGN_IN': '签到',
    'PURCHASE': '购物',
    'REVIEW': '评价',
    'EXCHANGE': '兑换',
    'REFUND_DEDUCT': '退款扣除',
    'EXPIRE': '过期',
    'ADMIN_ADJUST': '管理员调整'
  }
  return types[type] || '其他'
}

const getPointsTypeColor = (type: string) => {
  if (['SIGN_IN', 'PURCHASE', 'REVIEW'].includes(type)) return 'success'
  if (type === 'EXCHANGE') return 'warning'
  if (type === 'EXPIRE') return 'danger'
  return 'primary'
}

const formatDate = (date: string) => {
  return date ? date.split('T')[0] : ''
}

onMounted(() => {
  fetchUserInfo()
  checkTodaySigned()
  fetchPointsRecords()
  fetchCoupons()
})
</script>

<template>
  <UserLayout>
    <div class="bg-gray-50 min-h-screen">
      <div class="page-container py-6">
        <div class="bg-gradient-to-r from-purple-500 to-pink-500 rounded-2xl p-8 text-white mb-6 shadow-lg">
          <div class="flex items-center justify-between">
            <div>
              <h1 class="text-2xl font-bold mb-2">会员中心</h1>
              <p class="opacity-80">积分越多，福利越多</p>
            </div>
            <div class="text-right">
              <div class="text-4xl font-bold">{{ userInfo.points }}</div>
              <div class="opacity-80">可用积分</div>
            </div>
          </div>
          
          <div class="mt-6 flex items-center gap-8">
            <div class="flex items-center gap-2">
              <el-icon size="24"><Coin /></el-icon>
              <span>会员等级：Lv.{{ userInfo.memberLevel }}</span>
            </div>
            <el-button 
              :type="hasSignedIn ? 'info' : 'primary'"
              :loading="signInLoading"
              :disabled="hasSignedIn"
              class="transition-all duration-300"
              :class="hasSignedIn ? 'bg-gray-400 cursor-not-allowed' : 'bg-white/20 border-white hover:bg-white/30'"
              @click="handleSignIn"
            >
              {{ hasSignedIn ? '今日已签到' : '每日签到' }}
            </el-button>
          </div>
        </div>

        <el-tabs v-model="activeTab" class="bg-white rounded-lg p-6 shadow-sm">
          <el-tab-pane label="积分明细" name="points">
            <div v-loading="loading">
              <template v-if="pointsRecords.length > 0">
                <el-table :data="pointsRecords" stripe>
                  <el-table-column label="类型" width="120">
                    <template #default="{ row }">
                      <el-tag :type="getPointsTypeColor(row.type)" size="small">
                        {{ getPointsTypeText(row.type) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="积分变化" width="120">
                    <template #default="{ row }">
                      <span :class="row.points > 0 ? 'text-green-500 font-bold' : 'text-red-500 font-bold'">
                        {{ row.points > 0 ? '+' : '' }}{{ row.points }}
                      </span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="description" label="描述" />
                  <el-table-column prop="createTime" label="时间" width="180" />
                </el-table>
              </template>
              <el-empty v-else description="暂无积分记录" />
            </div>
          </el-tab-pane>

          <el-tab-pane label="优惠券" name="coupons">
            <div v-loading="loading">
              <h3 class="font-bold mb-4 text-gray-700">可领取优惠券</h3>
              <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
                <div 
                  v-for="coupon in availableCoupons" 
                  :key="coupon.id"
                  class="border border-orange-200 bg-gradient-to-br from-orange-50 to-orange-100 rounded-lg p-4 relative overflow-hidden hover:shadow-md transition-shadow cursor-pointer"
                >
                  <div class="absolute top-0 right-0 bg-orange-500 text-white text-xs px-2 py-1 rounded-bl">
                    可领取
                  </div>
                  <div class="text-orange-500 text-2xl font-bold mb-2">
                    ¥{{ coupon.value }}
                  </div>
                  <div class="text-sm text-gray-500 mb-2">
                    满{{ coupon.minAmount }}元可用
                  </div>
                  <div class="text-xs text-gray-400 mb-3">
                    {{ formatDate(coupon.startTime) }} ~ {{ formatDate(coupon.endTime) }}
                  </div>
                  <el-button 
                    type="warning" 
                    size="small"
                    @click="handleClaimCoupon(coupon.id)"
                  >
                    立即领取
                  </el-button>
                </div>
              </div>

              <h3 class="font-bold mb-4 text-gray-700">我的优惠券</h3>
              <template v-if="myCoupons.length > 0">
                <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div 
                    v-for="coupon in myCoupons" 
                    :key="coupon.id"
                    class="border rounded-lg p-4 hover:shadow-md transition-shadow"
                    :class="coupon.status === 0 ? 'border-green-200 bg-gradient-to-br from-green-50 to-green-100' : 'border-gray-200 bg-gray-50'"
                  >
                    <div class="text-green-500 text-2xl font-bold mb-2">
                      ¥{{ coupon.value }}
                    </div>
                    <div class="text-sm text-gray-500 mb-2">
                      满{{ coupon.minAmount }}元可用
                    </div>
                    <div class="text-xs text-gray-400 mb-3">
                      {{ formatDate(coupon.startTime) }} ~ {{ formatDate(coupon.endTime) }}
                    </div>
                    <el-tag :type="coupon.status === 0 ? 'success' : 'info'" size="small">
                      {{ coupon.status === 0 ? '未使用' : coupon.status === 1 ? '已使用' : '已过期' }}
                    </el-tag>
                  </div>
                </div>
              </template>
              <el-empty v-else description="暂无优惠券" />
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </UserLayout>
</template>

<style scoped>
.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
}

.cursor-not-allowed {
  cursor: not-allowed !important;
}
</style>
