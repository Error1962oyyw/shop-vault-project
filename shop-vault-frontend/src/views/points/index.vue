<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Coin, Ticket, Present, Star, Medal, Timer, Check } from '@element-plus/icons-vue'
import UserLayout from '@/components/layout/UserLayout.vue'
import { signIn, getPointsRecords, getAvailableCoupons, receiveCoupon, getMyCoupons, todaySigned } from '@/api/marketing'
import { getProfile } from '@/api/user'
import { getVipInfo, exchangeVip, getVipHistory } from '@/api/vip'
import type { PointsRecord, CouponTemplate, UserCoupon } from '@/types/api'
import type { VipInfo, VipMembership } from '@/api/vip'

const activeTab = ref('points')
const userInfo = ref({ points: 0, memberLevel: 1 })
const pointsRecords = ref<PointsRecord[]>([])
const availableCoupons = ref<CouponTemplate[]>([])
const myCoupons = ref<UserCoupon[]>([])
const loading = ref(false)
const signInLoading = ref(false)
const hasSignedIn = ref(false)

const vipInfo = ref<VipInfo | null>(null)
const vipHistory = ref<VipMembership[]>([])
const vipLoading = ref(false)
const exchangeLoading = ref(false)

const isVip = computed(() => vipInfo.value?.isVip ?? false)
const remainingDays = computed(() => vipInfo.value?.remainingDays ?? 0)

const vipCards = [
  {
    type: 1,
    name: 'VIP月卡',
    points: 1000,
    originalPrice: 99,
    duration: '30天',
    benefits: ['全场商品98折', '专属会员日活动', '1.5倍积分特权', '优先客服通道']
  },
  {
    type: 2,
    name: 'VIP年卡',
    points: 10000,
    originalPrice: 999,
    duration: '365天',
    benefits: ['全场商品98折', '专属会员日活动', '1.5倍积分特权', '优先客服通道', '生日专属礼遇', '新品优先体验'],
    recommended: true
  }
]

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

const fetchVipInfo = async () => {
  vipLoading.value = true
  try {
    vipInfo.value = await getVipInfo()
  } catch (error) {
    console.error('获取VIP信息失败', error)
  } finally {
    vipLoading.value = false
  }
}

const fetchVipHistory = async () => {
  try {
    vipHistory.value = await getVipHistory()
  } catch (error) {
    console.error('获取VIP历史失败', error)
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

const handleExchangeVip = async (type: number) => {
  const card = vipCards.find(c => c.type === type)
  if (!card) return
  
  if (userInfo.value.points < card.points) {
    ElMessage.warning(`积分不足，需要${card.points}积分`)
    return
  }
  
  exchangeLoading.value = true
  try {
    await exchangeVip(type)
    ElMessage.success('VIP开通成功，享受95折优惠！')
    await fetchUserInfo()
    await fetchVipInfo()
    await fetchVipHistory()
  } catch (error: any) {
    const msg = error?.response?.data?.msg || error?.message || '开通失败'
    ElMessage.error(msg)
  } finally {
    exchangeLoading.value = false
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

const getStatusText = (status: number) => {
  const texts: Record<number, string> = {
    0: '未激活',
    1: '生效中',
    2: '已过期'
  }
  return texts[status] || '未知'
}

const getStatusColor = (status: number): 'info' | 'success' | 'danger' => {
  const colors: Record<number, 'info' | 'success' | 'danger'> = {
    0: 'info',
    1: 'success',
    2: 'danger'
  }
  return colors[status] || 'info'
}

const getTypeText = (type: number) => {
  return type === 1 ? '月卡' : '年卡'
}

const memberBenefits = [
  { icon: Coin, title: '积分翻倍', desc: '会员日双倍积分', color: 'orange' },
  { icon: Ticket, title: '专属优惠券', desc: '每月领取特权', color: 'blue' },
  { icon: Present, title: '生日礼遇', desc: '生日专属惊喜', color: 'purple' },
  { icon: Star, title: '优先发货', desc: '极速物流体验', color: 'green' }
]

const vipBenefits = [
  { icon: Ticket, title: '全场98折', desc: '购物自动享受优惠', color: 'orange' },
  { icon: Present, title: '会员日活动', desc: '专属折扣与福利', color: 'blue' },
  { icon: Star, title: '1.5倍积分', desc: '消费积分翻倍', color: 'purple' },
  { icon: Medal, title: '优先客服', desc: '专属服务通道', color: 'green' }
]

onMounted(() => {
  fetchUserInfo()
  checkTodaySigned()
  fetchPointsRecords()
  fetchCoupons()
  fetchVipInfo()
  fetchVipHistory()
})
</script>

<template>
  <UserLayout>
    <div class="points-page">
      <div class="page-container">
        <div class="member-header">
          <div class="header-bg"></div>
          <div class="header-content">
            <div class="header-left">
              <div class="member-badge">
                <Medal v-if="isVip" class="badge-icon vip" />
                <Star v-else class="badge-icon" />
                <span>{{ isVip ? 'VIP会员' : '普通会员' }}</span>
              </div>
              <h1 class="header-title">会员中心</h1>
              <p class="header-subtitle">积分越多，福利越多</p>
            </div>
            <div class="header-right">
              <div class="points-display">
                <div class="points-value">{{ userInfo.points }}</div>
                <div class="points-label">可用积分</div>
              </div>
              <el-button 
                :type="hasSignedIn ? 'info' : 'primary'"
                :loading="signInLoading"
                :disabled="hasSignedIn"
                class="sign-btn"
                @click="handleSignIn"
              >
                <Coin class="btn-icon" />
                {{ hasSignedIn ? '今日已签到' : '签到领积分' }}
              </el-button>
            </div>
          </div>
        </div>

        <div v-if="isVip" class="vip-status-card">
          <div class="status-content">
            <div class="status-icon">
              <Medal />
            </div>
            <div class="status-info">
              <div class="status-title">VIP会员生效中</div>
              <div class="status-desc">
                剩余 <span class="highlight">{{ remainingDays }}</span> 天
                <span v-if="vipInfo?.vipExpireTime">
                  · 到期时间: {{ formatDate(vipInfo.vipExpireTime) }}
                </span>
              </div>
            </div>
            <div class="status-discount">
              <div class="discount-value">{{ ((vipInfo?.discountRate ?? 1) * 10).toFixed(1) }}</div>
              <div class="discount-label">折</div>
            </div>
          </div>
        </div>

        <section class="benefits-section">
          <div class="section-header">
            <h2 class="section-title">
              <Present class="title-icon" />
              {{ isVip ? 'VIP专属权益' : '会员权益' }}
            </h2>
          </div>
          <div class="benefits-grid">
            <div 
              v-for="benefit in isVip ? vipBenefits : memberBenefits" 
              :key="benefit.title"
              class="benefit-card"
            >
              <div class="benefit-icon" :class="`benefit-icon-${benefit.color}`">
                <component :is="benefit.icon" />
              </div>
              <div class="benefit-info">
                <h4>{{ benefit.title }}</h4>
                <p>{{ benefit.desc }}</p>
              </div>
            </div>
          </div>
        </section>

        <section class="content-section">
          <el-tabs v-model="activeTab" class="content-tabs">
            <el-tab-pane label="积分明细" name="points">
              <div v-loading="loading" class="tab-content">
                <template v-if="pointsRecords.length > 0">
                  <div class="records-list">
                    <div 
                      v-for="record in pointsRecords" 
                      :key="record.id"
                      class="record-item"
                    >
                      <div class="record-left">
                        <el-tag :type="getPointsTypeColor(record.type)" size="small" class="record-tag">
                          {{ getPointsTypeText(record.type) }}
                        </el-tag>
                        <div class="record-info">
                          <div class="record-desc">{{ record.description || '-' }}</div>
                          <div class="record-time">{{ record.createTime }}</div>
                        </div>
                      </div>
                      <div class="record-points" :class="record.points > 0 ? 'points-add' : 'points-minus'">
                        {{ record.points > 0 ? '+' : '' }}{{ record.points }}
                      </div>
                    </div>
                  </div>
                </template>
                <el-empty v-else description="暂无积分记录" class="empty-state" />
              </div>
            </el-tab-pane>

            <el-tab-pane label="VIP会员" name="vip">
              <div v-loading="vipLoading" class="tab-content">
                <div class="vip-cards-grid">
                  <div 
                    v-for="card in vipCards" 
                    :key="card.type"
                    :class="['vip-card', { recommended: card.recommended }]"
                  >
                    <div v-if="card.recommended" class="recommended-badge">推荐</div>
                    <div class="card-header">
                      <h3 class="card-name">{{ card.name }}</h3>
                      <div class="card-duration">
                        <Timer class="duration-icon" />
                        {{ card.duration }}
                      </div>
                    </div>
                    <div class="card-price">
                      <div class="points-price">
                        <span class="points-value">{{ card.points }}</span>
                        <span class="points-label">积分</span>
                      </div>
                      <div class="original-price">价值 ¥{{ card.originalPrice }}</div>
                    </div>
                    <div class="card-benefits">
                      <div v-for="(benefit, index) in card.benefits" :key="index" class="benefit-item">
                        <Check class="check-icon" />
                        {{ benefit }}
                      </div>
                    </div>
                    <el-button 
                      :type="card.recommended ? 'warning' : 'primary'"
                      :loading="exchangeLoading"
                      :disabled="userInfo.points < card.points"
                      class="exchange-btn"
                      @click="handleExchangeVip(card.type)"
                    >
                      {{ userInfo.points < card.points ? '积分不足' : '立即开通' }}
                    </el-button>
                  </div>
                </div>

                <div v-if="vipHistory.length > 0" class="vip-history-section">
                  <h3 class="history-title">会员记录</h3>
                  <div class="history-list">
                    <div 
                      v-for="record in vipHistory" 
                      :key="record.id"
                      class="history-item"
                    >
                      <div class="history-left">
                        <div class="history-icon">
                          <Medal />
                        </div>
                        <div class="history-info">
                          <div class="history-name">VIP{{ getTypeText(record.type) }}</div>
                          <div class="history-time">
                            {{ formatDate(record.startTime) }} ~ {{ formatDate(record.endTime) }}
                          </div>
                        </div>
                      </div>
                      <div class="history-right">
                        <el-tag :type="getStatusColor(record.status)" size="small">
                          {{ getStatusText(record.status) }}
                        </el-tag>
                        <div class="history-cost">{{ record.pointsCost }}积分</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="优惠券" name="coupons">
              <div v-loading="loading" class="tab-content">
                <div class="coupon-section">
                  <h3 class="coupon-title">
                    <Ticket class="title-icon-small" />
                    可领取优惠券
                  </h3>
                  <div v-if="availableCoupons.length > 0" class="coupons-grid">
                    <div 
                      v-for="coupon in availableCoupons" 
                      :key="coupon.id"
                      class="coupon-card coupon-available"
                    >
                      <div class="coupon-left">
                        <div class="coupon-value">¥{{ coupon.value }}</div>
                        <div class="coupon-condition">满{{ coupon.minAmount }}元可用</div>
                      </div>
                      <div class="coupon-right">
                        <div class="coupon-name">{{ coupon.name }}</div>
                        <div class="coupon-date">{{ formatDate(coupon.startTime) }} ~ {{ formatDate(coupon.endTime) }}</div>
                        <el-button type="warning" size="small" class="claim-btn" @click="handleClaimCoupon(coupon.id)">
                          立即领取
                        </el-button>
                      </div>
                      <div class="coupon-badge">可领取</div>
                    </div>
                  </div>
                  <el-empty v-else description="暂无可领取优惠券" :image-size="80" />
                </div>

                <div class="coupon-section">
                  <h3 class="coupon-title">
                    <Ticket class="title-icon-small" />
                    我的优惠券
                  </h3>
                  <template v-if="myCoupons.length > 0">
                    <div class="coupons-grid">
                      <div 
                        v-for="coupon in myCoupons" 
                        :key="coupon.id"
                        class="coupon-card"
                        :class="coupon.status === 0 ? 'coupon-usable' : 'coupon-used'"
                      >
                        <div class="coupon-left">
                          <div class="coupon-value">¥{{ coupon.value }}</div>
                          <div class="coupon-condition">满{{ coupon.minAmount }}元可用</div>
                        </div>
                        <div class="coupon-right">
                          <div class="coupon-name">{{ coupon.couponName }}</div>
                          <div class="coupon-date">{{ formatDate(coupon.startTime) }} ~ {{ formatDate(coupon.endTime) }}</div>
                          <el-tag :type="coupon.status === 0 ? 'success' : 'info'" size="small">
                            {{ coupon.status === 0 ? '未使用' : coupon.status === 1 ? '已使用' : '已过期' }}
                          </el-tag>
                        </div>
                      </div>
                    </div>
                  </template>
                  <el-empty v-else description="暂无优惠券" :image-size="80" />
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </section>
      </div>
    </div>
  </UserLayout>
</template>

<style scoped>
.points-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f7f8fa 0%, #e6f4ff 50%, #f0f5ff 100%);
  padding-bottom: 40px;
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.member-header {
  position: relative;
  border-radius: 20px;
  overflow: hidden;
  margin-bottom: 24px;
  margin-top: 24px;
}

.header-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 50%, #69b1ff 100%);
}

.header-content {
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 40px;
  color: #fff;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.member-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 255, 255, 0.2);
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  width: fit-content;
}

.badge-icon {
  font-size: 14px;
  width: 16px;
  height: 16px;
}

.badge-icon.vip {
  color: #ffd591;
}

.header-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0;
}

.header-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 32px;
}

.points-display {
  text-align: center;
}

.points-value {
  font-size: 48px;
  font-weight: 800;
  line-height: 1;
}

.points-label {
  font-size: 14px;
  opacity: 0.9;
  margin-top: 8px;
}

.sign-btn {
  height: 48px;
  padding: 0 28px;
  font-weight: 600;
  border-radius: 12px;
  background: #fff;
  color: #1677ff;
  border: none;
  font-size: 15px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.sign-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.95);
  color: #1677ff;
}

.sign-btn:disabled {
  background: rgba(255, 255, 255, 0.5);
  color: #1677ff;
}

.btn-icon {
  font-size: 16px;
  width: 16px;
  height: 16px;
}

.vip-status-card {
  background: linear-gradient(135deg, #fff7e6 0%, #ffd591 100%);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
}

.status-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.status-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #fa8c16 0%, #ffc53d 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 28px;
}

.status-icon :deep(svg) {
  width: 28px;
  height: 28px;
}

.status-info {
  flex: 1;
}

.status-title {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 4px;
}

.status-desc {
  font-size: 14px;
  color: #6b7280;
}

.highlight {
  color: #fa8c16;
  font-weight: 700;
  font-size: 18px;
}

.status-discount {
  text-align: center;
  background: rgba(255, 255, 255, 0.5);
  padding: 12px 20px;
  border-radius: 12px;
}

.discount-value {
  font-size: 32px;
  font-weight: 800;
  color: #fa8c16;
}

.discount-label {
  font-size: 14px;
  color: #6b7280;
}

.benefits-section {
  margin-bottom: 24px;
}

.section-header {
  margin-bottom: 16px;
}

.section-title {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon {
  font-size: 18px;
  color: #1677ff;
  width: 24px;
  height: 24px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.title-icon :deep(svg) {
  width: 18px;
  height: 18px;
}

.benefits-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.benefit-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.benefit-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.benefit-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.benefit-icon :deep(svg) {
  width: 18px;
  height: 18px;
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

.benefit-icon-green {
  background: linear-gradient(135deg, #f6ffed 0%, #b7eb8f 100%);
  color: #52c41a;
}

.benefit-info h4 {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 4px 0;
}

.benefit-info p {
  font-size: 13px;
  color: #6b7280;
  margin: 0;
}

.content-section {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.content-tabs {
  padding: 0 24px 24px;
}

.content-tabs :deep(.el-tabs__header) {
  margin: 0 0 24px;
  border-bottom: 1px solid #f3f4f6;
}

.content-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.content-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 500;
  padding: 0 24px;
  height: 48px;
  line-height: 48px;
}

.content-tabs :deep(.el-tabs__item.is-active) {
  color: #1677ff;
  font-weight: 600;
}

.content-tabs :deep(.el-tabs__active-bar) {
  height: 3px;
  border-radius: 2px;
}

.tab-content {
  min-height: 300px;
}

.records-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.record-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #f9fafb;
  border-radius: 12px;
  transition: all 0.2s ease;
}

.record-item:hover {
  background: #f3f4f6;
}

.record-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.record-tag {
  border-radius: 6px;
}

.record-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.record-desc {
  font-size: 14px;
  color: #1f2937;
  font-weight: 500;
}

.record-time {
  font-size: 12px;
  color: #9ca3af;
}

.record-points {
  font-size: 18px;
  font-weight: 700;
}

.points-add {
  color: #52c41a;
}

.points-minus {
  color: #f56c6c;
}

.empty-state {
  padding: 60px 0;
}

.vip-cards-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 32px;
}

.vip-card {
  background: #f9fafb;
  border-radius: 20px;
  padding: 24px;
  position: relative;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.vip-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.vip-card.recommended {
  border-color: #fa8c16;
  background: linear-gradient(135deg, #fffbe6 0%, #fff7e6 100%);
}

.recommended-badge {
  position: absolute;
  top: -1px;
  right: 24px;
  background: linear-gradient(135deg, #fa8c16 0%, #ffc53d 100%);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  padding: 6px 16px;
  border-radius: 0 0 8px 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-name {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.card-duration {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #6b7280;
}

.duration-icon {
  font-size: 14px;
  width: 14px;
  height: 14px;
}

.card-price {
  margin-bottom: 20px;
}

.points-price {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.points-value {
  font-size: 32px;
  font-weight: 800;
  color: #fa8c16;
}

.points-label {
  font-size: 14px;
  color: #6b7280;
}

.original-price {
  font-size: 14px;
  color: #9ca3af;
  text-decoration: line-through;
  margin-top: 4px;
}

.card-benefits {
  margin-bottom: 20px;
}

.benefit-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #374151;
  margin-bottom: 8px;
}

.check-icon {
  font-size: 14px;
  color: #52c41a;
  width: 14px;
  height: 14px;
}

.exchange-btn {
  width: 100%;
  height: 44px;
  border-radius: 10px;
  font-weight: 600;
  font-size: 15px;
}

.vip-history-section {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #f3f4f6;
}

.history-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 16px;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #f9fafb;
  border-radius: 12px;
}

.history-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.history-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #f0e6ff 0%, #d3adf7 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #722ed1;
  font-size: 18px;
}

.history-icon :deep(svg) {
  width: 18px;
  height: 18px;
}

.history-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.history-name {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.history-time {
  font-size: 12px;
  color: #9ca3af;
}

.history-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.history-cost {
  font-size: 14px;
  color: #fa8c16;
  font-weight: 600;
}

.coupon-section {
  margin-bottom: 32px;
}

.coupon-section:last-child {
  margin-bottom: 0;
}

.coupon-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon-small {
  font-size: 14px;
  color: #fa8c16;
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.title-icon-small :deep(svg) {
  width: 14px;
  height: 14px;
}

.coupons-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.coupon-card {
  display: flex;
  border-radius: 12px;
  overflow: hidden;
  position: relative;
  transition: all 0.3s ease;
}

.coupon-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.coupon-available {
  background: linear-gradient(135deg, #fff7e6 0%, #ffd591 100%);
  border: 1px solid #ffd591;
}

.coupon-usable {
  background: linear-gradient(135deg, #f6ffed 0%, #b7eb8f 100%);
  border: 1px solid #b7eb8f;
}

.coupon-used {
  background: #f5f5f5;
  border: 1px solid #e5e7eb;
}

.coupon-left {
  padding: 20px 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-right: 1px dashed rgba(0, 0, 0, 0.1);
  min-width: 100px;
}

.coupon-value {
  font-size: 28px;
  font-weight: 800;
  color: #fa8c16;
}

.coupon-usable .coupon-value {
  color: #52c41a;
}

.coupon-used .coupon-value {
  color: #9ca3af;
}

.coupon-condition {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

.coupon-right {
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.coupon-name {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.coupon-date {
  font-size: 12px;
  color: #6b7280;
}

.claim-btn {
  width: fit-content;
  margin-top: auto;
}

.coupon-badge {
  position: absolute;
  top: 0;
  right: 0;
  background: #fa8c16;
  color: #fff;
  font-size: 11px;
  padding: 4px 12px;
  border-radius: 0 0 0 8px;
}

@media (max-width: 1024px) {
  .benefits-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .coupons-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .vip-cards-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    text-align: center;
    gap: 24px;
    padding: 32px 24px;
  }

  .header-left {
    align-items: center;
  }

  .header-right {
    flex-direction: column;
    gap: 20px;
  }

  .points-value {
    font-size: 36px;
  }

  .benefits-grid {
    grid-template-columns: 1fr;
  }

  .coupons-grid {
    grid-template-columns: 1fr;
  }

  .record-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .record-points {
    align-self: flex-end;
  }

  .status-content {
    flex-direction: column;
    text-align: center;
  }

  .history-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .history-right {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
