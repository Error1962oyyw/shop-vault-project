<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Star, Calendar, Coin, Ticket, ShoppingBag, Present, InfoFilled, CircleCheck } from '@element-plus/icons-vue'
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
  const icons: Record<string, typeof Coin> = {
    discount: Coin,
    points: Coin,
    coupon: Ticket,
    gift: Present,
    exclusive: Star
  }
  return icons[type] || Present
}

const getBenefitColor = (type: string) => {
  const colors: Record<string, string> = {
    discount: 'orange',
    points: 'blue',
    coupon: 'purple',
    gift: 'pink',
    exclusive: 'green'
  }
  return colors[type] || 'blue'
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

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})
</script>

<template>
  <UserLayout>
    <div class="member-day-page animate-fade-in">
      <div class="page-container">
        <div v-if="isMemberDay && currentMemberDay" class="active-event">
          <div class="event-banner">
            <div class="banner-bg"></div>
            <div class="banner-decor">
              <div class="decor-circle decor-1"></div>
              <div class="decor-circle decor-2"></div>
              <div class="decor-circle decor-3"></div>
            </div>
            <div class="banner-content">
              <div class="event-badge">
                <Star class="badge-icon" />
                <span>会员日特惠进行中</span>
              </div>
              <h1 class="event-title">{{ currentMemberDay.name }}</h1>
              <p class="event-desc">{{ currentMemberDay.description }}</p>
              
              <div class="countdown-wrapper">
                <div class="countdown-item">
                  <div class="countdown-value">{{ countdown.days }}</div>
                  <div class="countdown-label">天</div>
                </div>
                <div class="countdown-separator">:</div>
                <div class="countdown-item">
                  <div class="countdown-value">{{ String(countdown.hours).padStart(2, '0') }}</div>
                  <div class="countdown-label">时</div>
                </div>
                <div class="countdown-separator">:</div>
                <div class="countdown-item">
                  <div class="countdown-value">{{ String(countdown.minutes).padStart(2, '0') }}</div>
                  <div class="countdown-label">分</div>
                </div>
                <div class="countdown-separator">:</div>
                <div class="countdown-item">
                  <div class="countdown-value">{{ String(countdown.seconds).padStart(2, '0') }}</div>
                  <div class="countdown-label">秒</div>
                </div>
              </div>

              <div class="action-buttons">
                <el-button type="primary" size="large" class="action-btn primary-btn" @click="goToProducts">
                  <ShoppingBag class="btn-icon" />
                  立即抢购
                </el-button>
                <el-button size="large" class="action-btn secondary-btn" @click="goToCoupons">
                  <Ticket class="btn-icon" />
                  领取优惠券
                </el-button>
              </div>
            </div>
          </div>

          <section class="benefits-section">
            <div class="section-header">
              <h2 class="section-title">
                <Present class="title-icon" />
                会员日专属权益
              </h2>
            </div>
            <div class="benefits-grid">
              <div 
                v-for="benefit in currentMemberDay.benefits" 
                :key="benefit.id"
                class="benefit-card"
              >
                <div class="benefit-icon" :class="`benefit-icon-${getBenefitColor(benefit.type)}`">
                  <component :is="getBenefitIcon(benefit.type)" />
                </div>
                <h3 class="benefit-title">{{ benefit.title }}</h3>
                <p class="benefit-desc">{{ benefit.description }}</p>
                <div class="benefit-value">{{ benefit.value }}</div>
              </div>
            </div>
          </section>

          <section class="rules-section">
            <div class="section-header">
              <h2 class="section-title">
                <InfoFilled class="title-icon orange" />
                活动规则
              </h2>
            </div>
            <div class="rules-card">
              <ul class="rules-list">
                <li class="rule-item">
                  <CircleCheck class="rule-icon" />
                  <span>会员日期间，商品享受{{ currentMemberDay.discountRate }}折优惠</span>
                </li>
                <li class="rule-item">
                  <CircleCheck class="rule-icon" />
                  <span>购物积分翻倍，每消费1元获得 {{ currentMemberDay.pointsMultiplier }} 积分</span>
                </li>
                <li class="rule-item">
                  <CircleCheck class="rule-icon" />
                  <span>活动时间：{{ formatTime(currentMemberDay.startTime) }} 至 {{ formatTime(currentMemberDay.endTime) }}</span>
                </li>
                <li class="rule-item">
                  <CircleCheck class="rule-icon" />
                  <span>优惠券数量有限，先到先得</span>
                </li>
              </ul>
            </div>
          </section>
        </div>

        <div v-else class="no-event">
          <div class="no-event-card">
            <div class="no-event-icon">
              <Calendar />
            </div>
            <h2 class="no-event-title">会员日即将到来</h2>
            <p class="no-event-desc">敬请期待更多精彩活动</p>
            
            <div v-if="upcomingMemberDays.length > 0" class="upcoming-section">
              <h3 class="upcoming-title">即将到来的会员日</h3>
              <div class="upcoming-grid">
                <div 
                  v-for="activity in upcomingMemberDays" 
                  :key="activity.id"
                  class="upcoming-card"
                >
                  <div class="upcoming-name">{{ activity.name }}</div>
                  <div class="upcoming-time">
                    {{ formatTime(activity.startTime) }}
                  </div>
                  <div class="upcoming-end">
                    至 {{ formatTime(activity.endTime) }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </UserLayout>
</template>

<style scoped>
.member-day-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f7f8fa 0%, #e6f4ff 50%, #f0f5ff 100%);
  padding-bottom: 40px;
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.event-banner {
  position: relative;
  border-radius: 20px;
  overflow: hidden;
  margin: 24px 0;
  min-height: 420px;
}

.banner-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #722ed1 0%, #9c27b0 50%, #e91e63 100%);
}

.banner-decor {
  position: absolute;
  inset: 0;
  overflow: hidden;
  opacity: 0.15;
}

.decor-circle {
  position: absolute;
  border-radius: 50%;
  background: #fff;
}

.decor-1 {
  width: 200px;
  height: 200px;
  top: -50px;
  left: -50px;
}

.decor-2 {
  width: 300px;
  height: 300px;
  bottom: -100px;
  right: -50px;
}

.decor-3 {
  width: 150px;
  height: 150px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.banner-content {
  position: relative;
  padding: 48px;
  text-align: center;
  color: #fff;
}

.event-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  padding: 8px 20px;
  border-radius: 24px;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 24px;
}

.badge-icon {
  font-size: 18px;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.event-title {
  font-size: 42px;
  font-weight: 800;
  margin: 0 0 12px;
}

.event-desc {
  font-size: 18px;
  opacity: 0.9;
  margin: 0 0 32px;
}

.countdown-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-bottom: 32px;
}

.countdown-item {
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 16px 20px;
  min-width: 80px;
}

.countdown-value {
  font-size: 36px;
  font-weight: 800;
  line-height: 1;
}

.countdown-label {
  font-size: 13px;
  opacity: 0.8;
  margin-top: 4px;
}

.countdown-separator {
  font-size: 32px;
  font-weight: 700;
  opacity: 0.6;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.action-btn {
  height: 52px;
  padding: 0 32px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.primary-btn {
  background: #fff;
  color: #722ed1;
  border: none;
}

.primary-btn:hover {
  background: rgba(255, 255, 255, 0.95);
  color: #722ed1;
}

.secondary-btn {
  background: transparent;
  color: #fff;
  border: 2px solid rgba(255, 255, 255, 0.5);
}

.secondary-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.8);
  color: #fff;
}

.btn-icon {
  font-size: 20px;
}

.section-header {
  margin-bottom: 20px;
}

.section-title {
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-icon {
  font-size: 18px;
  color: #722ed1;
  width: 24px;
  height: 24px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.title-icon.orange {
  color: #fa8c16;
}

.benefits-section {
  margin-bottom: 24px;
}

.benefits-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.benefit-card {
  background: #fff;
  border-radius: 16px;
  padding: 28px 24px;
  text-align: center;
  box-shadow: var(--shadow-sm);
  transition: all 0.3s ease;
}

.benefit-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.benefit-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
}

.benefit-icon-orange {
  background: linear-gradient(135deg, #fff7e6 0%, #ffd591 100%);
  color: #fa8c16;
}

.benefit-icon-blue {
  background: linear-gradient(135deg, #e6f4ff 0%, #bae0ff 100%);
  color: #1677ff;
}

.benefit-icon-purple {
  background: linear-gradient(135deg, #f0e6ff 0%, #d3adf7 100%);
  color: #722ed1;
}

.benefit-icon-pink {
  background: linear-gradient(135deg, #fff0f6 0%, #ffadd2 100%);
  color: #eb2f96;
}

.benefit-icon-green {
  background: linear-gradient(135deg, #f6ffed 0%, #b7eb8f 100%);
  color: #52c41a;
}

.benefit-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px;
}

.benefit-desc {
  font-size: 13px;
  color: #6b7280;
  margin: 0 0 12px;
}

.benefit-value {
  font-size: 18px;
  font-weight: 700;
  color: #722ed1;
}

.rules-section {
  margin-bottom: 24px;
}

.rules-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px 32px;
  box-shadow: var(--shadow-sm);
}

.rules-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
}

.rule-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #4b5563;
  background: #f9fafb;
  padding: 12px 16px;
  border-radius: 8px;
  flex: 1;
  min-width: 200px;
}

.rule-icon {
  font-size: 16px;
  color: #52c41a;
  flex-shrink: 0;
}

.no-event {
  padding: 48px 0;
}

.no-event-card {
  background: #fff;
  border-radius: 20px;
  padding: 60px 48px;
  text-align: center;
  box-shadow: var(--shadow-sm);
}

.no-event-icon {
  width: 96px;
  height: 96px;
  margin: 0 auto 24px;
  background: linear-gradient(135deg, #f0e6ff 0%, #d3adf7 100%);
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  color: #722ed1;
}

.no-event-title {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 12px;
}

.no-event-desc {
  font-size: 16px;
  color: #6b7280;
  margin: 0;
}

.upcoming-section {
  margin-top: 48px;
  text-align: left;
}

.upcoming-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 20px;
}

.upcoming-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.upcoming-card {
  background: #f9fafb;
  border-radius: 12px;
  padding: 20px;
  transition: all 0.3s ease;
}

.upcoming-card:hover {
  background: #f3f4f6;
}

.upcoming-name {
  font-size: 15px;
  font-weight: 600;
  color: #722ed1;
  margin-bottom: 8px;
}

.upcoming-time {
  font-size: 13px;
  color: #4b5563;
}

.upcoming-end {
  font-size: 12px;
  color: #6b7280;
}

@media (max-width: 1024px) {
  .benefits-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .upcoming-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .event-banner {
    min-height: auto;
  }

  .banner-content {
    padding: 32px 24px;
  }

  .event-title {
    font-size: 28px;
  }

  .event-desc {
    font-size: 15px;
  }

  .countdown-wrapper {
    flex-wrap: wrap;
    gap: 8px;
  }

  .countdown-item {
    min-width: 60px;
    padding: 12px 16px;
  }

  .countdown-value {
    font-size: 24px;
  }

  .countdown-separator {
    font-size: 20px;
  }

  .action-buttons {
    flex-direction: column;
    align-items: center;
  }

  .action-btn {
    width: 100%;
    max-width: 280px;
  }

  .benefits-grid {
    grid-template-columns: 1fr;
  }

  .upcoming-grid {
    grid-template-columns: 1fr;
  }

  .no-event-card {
    padding: 40px 24px;
  }
}
</style>
