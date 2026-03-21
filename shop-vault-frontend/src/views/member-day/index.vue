<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import UserLayout from '@/components/layout/UserLayout.vue'
import { 
  getCurrentMemberDay, 
  getUpcomingMemberDays, 
  checkMemberDay
} from '@/api/marketing'
import type { MemberDay, Activity } from '@/types/api'

const router = useRouter()

const currentMemberDay = ref<MemberDay | null>(null)
const upcomingMemberDays = ref<Activity[]>([])
const isMemberDay = ref(false)
const loading = ref(false)
const countdown = ref({ days: 0, hours: 0, minutes: 0, seconds: 0 })

let countdownTimer: ReturnType<typeof setInterval> | null = null

const fetchCurrentMemberDay = async () => {
  try {
    currentMemberDay.value = await getCurrentMemberDay()
    if (currentMemberDay.value) {
      startCountdown(currentMemberDay.value.endTime)
    }
  } catch (error) {
    console.error('获取当前会员日活动失败', error)
  }
}

const fetchUpcomingMemberDays = async () => {
  try {
    upcomingMemberDays.value = await getUpcomingMemberDays()
  } catch (error) {
    console.error('获取即将到来的会员日失败', error)
  }
}

const checkIsMemberDay = async () => {
  try {
    isMemberDay.value = await checkMemberDay()
  } catch (error) {
    console.error('检查会员日失败', error)
  }
}

const startCountdown = (endTime: string) => {
  const updateCountdown = () => {
    const now = new Date().getTime()
    const end = new Date(endTime).getTime()
    const diff = end - now

    if (diff <= 0) {
      countdown.value = { days: 0, hours: 0, minutes: 0, seconds: 0 }
      if (countdownTimer) {
        clearInterval(countdownTimer)
      }
      return
    }

    countdown.value = {
      days: Math.floor(diff / (1000 * 60 * 60 * 24)),
      hours: Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)),
      minutes: Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60)),
      seconds: Math.floor((diff % (1000 * 60)) / 1000)
    }
  }

  updateCountdown()
  countdownTimer = setInterval(updateCountdown, 1000)
}

const formatTime = (date: string) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const getBenefitIcon = (type: string) => {
  const icons: Record<string, string> = {
    discount: 'Discount',
    points: 'Coin',
    coupon: 'Ticket',
    gift: 'Present',
    exclusive: 'Star'
  }
  return icons[type] || 'Present'
}

const goToProducts = () => {
  router.push('/products')
}

const goToCoupons = () => {
  router.push('/coupons')
}

onMounted(() => {
  loading.value = true
  Promise.all([
    fetchCurrentMemberDay(),
    fetchUpcomingMemberDays(),
    checkIsMemberDay()
  ]).finally(() => {
    loading.value = false
  })
})
</script>

<template>
  <UserLayout>
    <div class="bg-gradient-to-b from-purple-50 to-white min-h-screen">
      <div v-loading="loading">
        <div 
          v-if="isMemberDay && currentMemberDay"
          class="member-day-banner relative overflow-hidden"
        >
          <div class="absolute inset-0 bg-gradient-to-r from-purple-600 via-pink-500 to-red-500"></div>
          <div class="absolute inset-0 opacity-20">
            <div class="absolute top-10 left-10 w-32 h-32 bg-white rounded-full blur-3xl"></div>
            <div class="absolute bottom-10 right-20 w-48 h-48 bg-yellow-300 rounded-full blur-3xl"></div>
          </div>
          
          <div class="relative page-container py-12 text-white text-center">
            <div class="inline-flex items-center gap-2 bg-white/20 rounded-full px-4 py-1 mb-4">
              <el-icon class="animate-pulse"><Star /></el-icon>
              <span class="text-sm font-medium">会员日特惠进行中</span>
            </div>
            
            <h1 class="text-4xl font-bold mb-2">{{ currentMemberDay.name }}</h1>
            <p class="text-lg opacity-90 mb-6">{{ currentMemberDay.description }}</p>
            
            <div class="flex justify-center gap-4 mb-8">
              <div class="bg-white/20 backdrop-blur rounded-lg px-6 py-4">
                <div class="text-3xl font-bold">{{ countdown.days }}</div>
                <div class="text-sm opacity-80">天</div>
              </div>
              <div class="bg-white/20 backdrop-blur rounded-lg px-6 py-4">
                <div class="text-3xl font-bold">{{ countdown.hours }}</div>
                <div class="text-sm opacity-80">时</div>
              </div>
              <div class="bg-white/20 backdrop-blur rounded-lg px-6 py-4">
                <div class="text-3xl font-bold">{{ countdown.minutes }}</div>
                <div class="text-sm opacity-80">分</div>
              </div>
              <div class="bg-white/20 backdrop-blur rounded-lg px-6 py-4">
                <div class="text-3xl font-bold">{{ countdown.seconds }}</div>
                <div class="text-sm opacity-80">秒</div>
              </div>
            </div>

            <div class="flex justify-center gap-4">
              <el-button 
                type="primary" 
                size="large"
                class="bg-white text-purple-600 border-0 hover:bg-gray-100"
                @click="goToProducts"
              >
                <el-icon class="mr-2"><ShoppingBag /></el-icon>
                立即抢购
              </el-button>
              <el-button 
                size="large"
                class="bg-transparent border-white text-white hover:bg-white/10"
                @click="goToCoupons"
              >
                <el-icon class="mr-2"><Ticket /></el-icon>
                领取优惠券
              </el-button>
            </div>
          </div>
        </div>

        <div v-else class="page-container py-12">
          <div class="bg-white rounded-2xl shadow-lg p-8 text-center">
            <div class="w-24 h-24 mx-auto bg-purple-100 rounded-full flex items-center justify-center mb-6">
              <el-icon size="48" class="text-purple-500"><Calendar /></el-icon>
            </div>
            <h2 class="text-2xl font-bold text-gray-800 mb-2">会员日即将到来</h2>
            <p class="text-gray-500 mb-6">敬请期待更多精彩活动</p>
            
            <div v-if="upcomingMemberDays.length > 0" class="mt-8">
              <h3 class="text-lg font-bold text-gray-800 mb-4">即将到来的会员日</h3>
              <div class="grid grid-cols-3 gap-4">
                <div 
                  v-for="activity in upcomingMemberDays" 
                  :key="activity.id"
                  class="bg-gray-50 rounded-lg p-4 text-left"
                >
                  <div class="font-bold text-purple-600">{{ activity.name }}</div>
                  <div class="text-sm text-gray-500 mt-1">
                    {{ formatTime(activity.startTime) }}
                  </div>
                  <div class="text-sm text-gray-400">
                    至 {{ formatTime(activity.endTime) }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="currentMemberDay" class="page-container py-8">
          <div class="bg-white rounded-lg shadow-sm p-6">
            <h2 class="text-xl font-bold text-gray-800 mb-6 flex items-center gap-2">
              <el-icon class="text-purple-500"><Present /></el-icon>
              会员日专属权益
            </h2>
            
            <div class="grid grid-cols-4 gap-4">
              <div 
                v-for="benefit in currentMemberDay.benefits" 
                :key="benefit.id"
                class="bg-gradient-to-br from-purple-50 to-pink-50 rounded-lg p-6 text-center hover:shadow-md transition-shadow"
              >
                <div class="w-16 h-16 mx-auto bg-white rounded-full flex items-center justify-center mb-4 shadow-sm">
                  <el-icon size="32" class="text-purple-500">
                    <component :is="getBenefitIcon(benefit.type)" />
                  </el-icon>
                </div>
                <h3 class="font-bold text-gray-800 mb-1">{{ benefit.title }}</h3>
                <p class="text-sm text-gray-500">{{ benefit.description }}</p>
                <div class="mt-3 text-purple-600 font-bold">
                  {{ benefit.value }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="currentMemberDay" class="page-container pb-8">
          <div class="bg-white rounded-lg shadow-sm p-6">
            <h2 class="text-xl font-bold text-gray-800 mb-4 flex items-center gap-2">
              <el-icon class="text-orange-500"><InfoFilled /></el-icon>
              活动规则
            </h2>
            <ul class="space-y-2 text-gray-600">
              <li class="flex items-start gap-2">
                <el-icon class="text-green-500 mt-1"><CircleCheck /></el-icon>
                <span>会员日期间，全场商品享受 {{ currentMemberDay.discountRate }} 折优惠</span>
              </li>
              <li class="flex items-start gap-2">
                <el-icon class="text-green-500 mt-1"><CircleCheck /></el-icon>
                <span>购物积分翻倍，每消费1元获得 {{ currentMemberDay.pointsMultiplier }} 积分</span>
              </li>
              <li class="flex items-start gap-2">
                <el-icon class="text-green-500 mt-1"><CircleCheck /></el-icon>
                <span>活动时间：{{ formatTime(currentMemberDay.startTime) }} 至 {{ formatTime(currentMemberDay.endTime) }}</span>
              </li>
              <li class="flex items-start gap-2">
                <el-icon class="text-green-500 mt-1"><CircleCheck /></el-icon>
                <span>优惠券数量有限，先到先得</span>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </UserLayout>
</template>

<style scoped>
.member-day-banner {
  min-height: 400px;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.animate-pulse {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}
</style>
