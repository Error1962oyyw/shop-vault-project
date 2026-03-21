<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  Search, Shop, Camera, MagicStick, Coin,
  TrendCharts, ArrowRight, Calendar as CalendarIcon, CircleCheck,
  Goods, Ticket, Timer
} from '@element-plus/icons-vue'
import UserLayout from '@/components/layout/UserLayout.vue'
import { ProductCard } from '@/components/common'
import { getProductList, getRecommendations, getCategoryList } from '@/api/product'
import { signIn, todaySigned, checkMemberDay, getAvailableActivities } from '@/api/marketing'
import { getMyMessages } from '@/api/message'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import type { Product, Category, Activity } from '@/types/api'

const router = useRouter()
const userStore = useUserStore()

const banners = ref([
  { id: 1, title: '新品上市', subtitle: '探索最新潮流好物', link: '/products', gradient: 'linear-gradient(135deg, #1677ff 0%, #4096ff 100%)' },
  { id: 2, title: '限时特惠', subtitle: '精选商品 限时折扣', link: '/products', gradient: 'linear-gradient(135deg, #ff6b6b 0%, #ff8e53 100%)' },
  { id: 3, title: '会员专享', subtitle: '会员日 双倍积分', link: '/member-day', gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' }
])

const categories = ref<Category[]>([])
const hotProducts = ref<Product[]>([])
const recommendProducts = ref<Product[]>([])
const activities = ref<Activity[]>([])
const loading = ref(false)
const isMemberDay = ref(false)
const unreadMessages = ref(0)
const searchKeyword = ref('')
const hasSignedToday = ref(false)

const isLoggedIn = computed(() => !!userStore.token)

const fetchCategories = async () => {
  try {
    categories.value = await getCategoryList()
  } catch (error) {
    console.error('获取分类失败', error)
  }
}

const fetchHotProducts = async () => {
  try {
    const res = await getProductList({ pageNum: 1, pageSize: 8, sortBy: 'sales', sortOrder: 'desc' })
    hotProducts.value = res.records
  } catch (error) {
    console.error('获取热门商品失败', error)
  }
}

const fetchRecommendations = async () => {
  if (!userStore.token) return
  try {
    recommendProducts.value = await getRecommendations()
  } catch (error) {
    console.error('获取推荐商品失败', error)
  }
}

const fetchMemberDayStatus = async () => {
  try {
    isMemberDay.value = await checkMemberDay()
  } catch (error) {
    console.error('检查会员日失败', error)
  }
}

const fetchActivities = async () => {
  try {
    activities.value = await getAvailableActivities()
  } catch (error) {
    console.error('获取活动失败', error)
  }
}

const fetchUnreadCount = async () => {
  if (!userStore.token) return
  try {
    const messages = await getMyMessages()
    unreadMessages.value = messages.filter((m: { isRead: boolean }) => !m.isRead).length
  } catch (error) {
    console.error('获取消息失败', error)
  }
}

const checkTodaySigned = async () => {
  if (!userStore.token) return
  try {
    hasSignedToday.value = await todaySigned()
  } catch (error) {
    console.error('检查签到状态失败', error)
  }
}

const handleSignIn = async () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (hasSignedToday.value) {
    ElMessage.info('今日已签到，请明天再来！')
    return
  }
  try {
    const res = await signIn()
    ElMessage.success(`签到成功，获得${res.points}积分`)
    hasSignedToday.value = true
  } catch (error: any) {
    const msg = error?.response?.data?.msg || error?.message || '签到失败，请重试'
    ElMessage.error(msg)
    if (msg.includes('今日已签到')) {
      hasSignedToday.value = true
    }
  }
}

const goToCategory = (categoryId: number) => {
  router.push({ path: '/products', query: { categoryId } })
}

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/products', query: { keyword: searchKeyword.value } })
  }
}

const handleProtectedNavigation = (path: string) => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  router.push(path)
}

onMounted(() => {
  loading.value = true
  Promise.all([
    fetchCategories(),
    fetchHotProducts(),
    fetchRecommendations(),
    fetchMemberDayStatus(),
    fetchActivities(),
    fetchUnreadCount(),
    checkTodaySigned()
  ]).finally(() => {
    loading.value = false
  })
})
</script>

<template>
  <UserLayout>
    <div class="home-page">
      <section class="hero-section">
        <div class="hero-content">
          <div class="hero-left">
            <h1 class="hero-title">
              <span class="gradient-text">小铺宝库</span>
              <span class="hero-subtitle-inline">智慧电商系统</span>
            </h1>
            <p class="hero-description">基于AI视觉搜索与智能推荐的现代化购物体验</p>
            
            <div class="search-container">
              <div class="search-box">
                <el-input
                  v-model="searchKeyword"
                  class="search-input"
                  placeholder="搜索商品..."
                  @keyup.enter="handleSearch"
                  clearable
                  size="large"
                >
                  <template #prefix>
                    <Search class="search-icon" />
                  </template>
                </el-input>
                <el-button class="search-btn" type="primary" @click="handleSearch">
                  搜索
                </el-button>
              </div>
              <div class="hot-keywords">
                <span class="hot-label">热门搜索：</span>
                <a v-for="keyword in ['手机', '电脑', '服装', '数码']" :key="keyword"
                   class="keyword-tag"
                   @click.prevent="searchKeyword = keyword; handleSearch()">
                  {{ keyword }}
                </a>
              </div>
            </div>

            <div class="quick-actions">
              <div class="action-item" @click="handleProtectedNavigation('/ai-search')">
                <div class="action-icon action-icon-blue">
                  <Camera />
                </div>
                <span>AI识图</span>
              </div>
              <div class="action-item" @click="handleProtectedNavigation('/coupons')">
                <div class="action-icon action-icon-orange">
                  <Ticket />
                </div>
                <span>优惠券</span>
              </div>
              <div class="action-item" @click="handleProtectedNavigation('/member-day')">
                <div class="action-icon action-icon-purple">
                  <CalendarIcon />
                </div>
                <span>会员日</span>
              </div>
              <div class="action-item" @click="handleProtectedNavigation('/points')">
                <div class="action-icon action-icon-green">
                  <Coin />
                </div>
                <span>积分</span>
              </div>
            </div>
          </div>

          <div class="hero-right">
            <el-carousel height="320px" :interval="5000" arrow="hover" class="hero-carousel">
              <el-carousel-item v-for="banner in banners" :key="banner.id">
                <div
                  class="banner-slide"
                  :style="{ background: banner.gradient }"
                  @click="router.push(banner.link)"
                >
                  <div class="banner-content">
                    <h2>{{ banner.title }}</h2>
                    <p>{{ banner.subtitle }}</p>
                    <el-button class="banner-btn">立即查看</el-button>
                  </div>
                  <div class="banner-visual">
                    <Shop class="banner-icon" />
                  </div>
                </div>
              </el-carousel-item>
            </el-carousel>
          </div>
        </div>
      </section>

      <section class="features-section">
        <div class="features-grid">
          <div class="feature-card">
            <div class="feature-icon feature-icon-blue">
              <Camera />
            </div>
            <div class="feature-info">
              <h4>AI识图搜索</h4>
              <p>拍照识别商品</p>
            </div>
          </div>
          <div class="feature-card">
            <div class="feature-icon feature-icon-cyan">
              <MagicStick />
            </div>
            <div class="feature-info">
              <h4>智能推荐</h4>
              <p>精准好物推荐</p>
            </div>
          </div>
          <div class="feature-card">
            <div class="feature-icon feature-icon-green">
              <Coin />
            </div>
            <div class="feature-info">
              <h4>会员积分</h4>
              <p>签到领好礼</p>
            </div>
          </div>
          <div class="feature-card">
            <div class="feature-icon feature-icon-orange">
              <CircleCheck />
            </div>
            <div class="feature-info">
              <h4>品质保障</h4>
              <p>7天无理由退换</p>
            </div>
          </div>
        </div>
      </section>

      <section class="categories-section">
        <div class="section-header">
          <h2 class="section-title">
            <Goods class="title-icon" />
            商品分类
          </h2>
          <router-link to="/products" class="view-more">
            查看全部 <ArrowRight />
          </router-link>
        </div>
        <div class="categories-grid">
          <div
            v-for="category in categories.slice(0, 8)"
            :key="category.id"
            class="category-card"
            @click="goToCategory(category.id)"
          >
            <div class="category-icon-wrapper">
              <el-icon class="category-icon">
                <component :is="category.icon || 'Goods'" />
              </el-icon>
            </div>
            <span class="category-name">{{ category.name }}</span>
          </div>
        </div>
      </section>

      <section class="products-section">
        <div class="section-header">
          <h2 class="section-title">
            <TrendCharts class="title-icon" />
            热销商品
          </h2>
          <router-link to="/products?sort=sales" class="view-more">
            查看全部 <ArrowRight />
          </router-link>
        </div>
        <div v-loading="loading" class="products-grid">
          <ProductCard
            v-for="product in hotProducts"
            :key="product.id"
            :product="product"
          />
        </div>
      </section>

      <section v-if="recommendProducts.length > 0" class="products-section">
        <div class="section-header">
          <h2 class="section-title">
            <MagicStick class="title-icon title-icon-purple" />
            猜你喜欢
            <el-tag size="small" class="ai-tag">AI推荐</el-tag>
          </h2>
        </div>
        <div class="products-grid">
          <ProductCard
            v-for="product in recommendProducts"
            :key="product.id"
            :product="product"
          />
        </div>
      </section>

      <section v-if="activities.length > 0" class="activities-section">
        <div class="section-header">
          <h2 class="section-title">
            <Timer class="title-icon" />
            热门活动
          </h2>
          <router-link to="/coupons" class="view-more">
            查看全部 <ArrowRight />
          </router-link>
        </div>
        <div class="activities-grid">
          <div
            v-for="activity in activities.slice(0, 4)"
            :key="activity.id"
            class="activity-card"
            @click="router.push('/coupons')"
          >
            <div class="activity-image">
              <Coin class="activity-icon" />
            </div>
            <div class="activity-info">
              <h4>{{ activity.name }}</h4>
              <p>{{ activity.description }}</p>
            </div>
          </div>
        </div>
      </section>

      <section class="member-section" v-if="isLoggedIn">
        <div class="member-card">
          <div class="member-left">
            <el-avatar :size="56" :src="userStore.userInfo?.avatar" class="member-avatar">
              {{ userStore.userInfo?.nickname?.charAt(0) || userStore.userInfo?.username?.charAt(0) }}
            </el-avatar>
            <div class="member-info">
              <h3>{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</h3>
              <p class="member-points">
                <Coin class="points-icon" />
                积分：{{ userStore.userInfo?.points || 0 }}
              </p>
            </div>
          </div>
          <div class="member-actions">
            <el-button
              :type="hasSignedToday ? 'info' : 'primary'"
              :disabled="hasSignedToday"
              @click="handleSignIn"
              class="sign-btn"
            >
              {{ hasSignedToday ? '今日已签到' : '签到领积分' }}
            </el-button>
            <el-button @click="router.push('/profile')" class="profile-btn">
              个人中心
            </el-button>
          </div>
        </div>
      </section>

      <section class="member-section" v-else>
        <div class="member-card guest-card">
          <div class="guest-content">
            <h3>欢迎来到小铺宝库</h3>
            <p>登录后享受更多专属服务</p>
          </div>
          <div class="guest-actions">
            <el-button type="primary" @click="router.push('/login')" class="login-btn">
              立即登录
            </el-button>
            <el-button @click="router.push('/register')" class="register-btn">
              注册账号
            </el-button>
          </div>
        </div>
      </section>
    </div>
  </UserLayout>
</template>

<style scoped>
.home-page {
  background: linear-gradient(135deg, #f7f8fa 0%, #e6f4ff 50%, #f0f5ff 100%);
  min-height: 100vh;
}

.hero-section {
  padding: 32px 0;
}

.hero-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
  align-items: center;
}

.hero-left {
  animation: slideUp 0.6s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.hero-title {
  font-size: 42px;
  font-weight: 800;
  margin: 0 0 16px 0;
  line-height: 1.2;
}

.gradient-text {
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 50%, #1677ff 100%);
  background-size: 200% 100%;
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: gradientFlow 3s ease infinite;
}

@keyframes gradientFlow {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

.hero-subtitle-inline {
  display: block;
  font-size: 18px;
  font-weight: 500;
  color: #4e5969;
  margin-top: 8px;
  -webkit-text-fill-color: #4e5969;
}

.hero-description {
  font-size: 16px;
  color: #86909c;
  margin: 0 0 32px 0;
  line-height: 1.6;
}

.search-container {
  margin-bottom: 32px;
}

.search-box {
  display: flex;
  align-items: stretch;
  border: 2px solid var(--primary-color);
  border-radius: 16px;
  overflow: hidden;
  background: white;
  box-shadow: var(--shadow-md);
  height: 52px;
}

.search-input {
  flex: 1;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 0;
  box-shadow: none;
  padding: 0 20px;
  font-size: 16px;
  height: 100%;
}

.search-input :deep(.el-input__inner) {
  height: 100%;
}

.search-icon {
  color: var(--text-secondary);
  font-size: 20px;
}

.search-btn {
  background: var(--primary-color);
  border: none;
  border-radius: 0;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  padding: 0 32px;
  height: 100%;
  flex-shrink: 0;
}

.search-btn:hover {
  background: var(--primary-light);
  color: #fff;
}

.hot-keywords {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
  padding-left: 4px;
}

.hot-label {
  font-size: 13px;
  color: var(--text-secondary);
}

.keyword-tag {
  font-size: 13px;
  color: var(--text-secondary);
  text-decoration: none;
  padding: 4px 12px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 20px;
  transition: all 0.2s;
  cursor: pointer;
}

.keyword-tag:hover {
  color: var(--primary-color);
  background: var(--primary-50);
}

.quick-actions {
  display: flex;
  gap: 16px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 24px;
  background: white;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: var(--shadow-sm);
}

.action-item:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.action-icon-blue {
  background: linear-gradient(135deg, #e6f4ff 0%, #bae0ff 100%);
  color: #1677ff;
}

.action-icon-orange {
  background: linear-gradient(135deg, #fff7e6 0%, #ffd591 100%);
  color: #fa8c16;
}

.action-icon-purple {
  background: linear-gradient(135deg, #f0e6ff 0%, #d3adf7 100%);
  color: #722ed1;
}

.action-icon-green {
  background: linear-gradient(135deg, #f6ffed 0%, #b7eb8f 100%);
  color: #52c41a;
}

.action-item span {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.hero-right {
  animation: slideLeft 0.6s ease-out;
}

@keyframes slideLeft {
  from {
    opacity: 0;
    transform: translateX(30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.hero-carousel {
  border-radius: 20px;
  overflow: hidden;
  box-shadow: var(--shadow-xl);
}

.banner-slide {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 48px;
  cursor: pointer;
}

.banner-content {
  color: #fff;
}

.banner-content h2 {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 12px 0;
}

.banner-content p {
  font-size: 16px;
  margin: 0 0 24px 0;
  opacity: 0.9;
}

.banner-btn {
  background: #fff;
  border: none;
  color: var(--primary-color);
  font-weight: 600;
  border-radius: 12px;
}

.banner-btn:hover {
  background: rgba(255, 255, 255, 0.9);
  color: var(--primary-color);
}

.banner-visual {
  width: 160px;
  height: 160px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.banner-icon {
  font-size: 80px;
  color: rgba(255, 255, 255, 0.8);
}

.features-section {
  padding: 24px 0;
}

.features-grid {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.feature-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background: white;
  border-radius: 16px;
  box-shadow: var(--shadow-sm);
  transition: all 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.feature-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  flex-shrink: 0;
}

.feature-icon-blue {
  background: linear-gradient(135deg, #e6f4ff 0%, #bae0ff 100%);
  color: #1677ff;
}

.feature-icon-cyan {
  background: linear-gradient(135deg, #e6fffb 0%, #b5f5ec 100%);
  color: #13c2c2;
}

.feature-icon-green {
  background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
  color: #52c41a;
}

.feature-icon-orange {
  background: linear-gradient(135deg, #fff7e6 0%, #ffe58f 100%);
  color: #fa8c16;
}

.feature-info h4 {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 4px 0;
}

.feature-info p {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0;
}

.section-header {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-icon {
  font-size: 24px;
  color: var(--primary-color);
}

.title-icon-purple {
  color: #722ed1;
}

.ai-tag {
  background: linear-gradient(135deg, #722ed1 0%, #9c27b0 100%);
  border: none;
  color: #fff;
  margin-left: 8px;
}

.view-more {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: var(--text-secondary);
  text-decoration: none;
  transition: color 0.2s;
}

.view-more:hover {
  color: var(--primary-color);
}

.categories-section {
  padding: 40px 0;
}

.categories-grid {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 16px;
}

.category-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 24px 16px;
  background: white;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: var(--shadow-sm);
}

.category-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-color);
}

.category-icon-wrapper {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  background: linear-gradient(135deg, var(--primary-50) 0%, var(--primary-100) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.category-icon {
  font-size: 28px;
  color: var(--primary-color);
}

.category-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  text-align: center;
}

.products-section {
  padding: 40px 0;
}

.products-grid {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.activities-section {
  padding: 40px 0;
}

.activities-grid {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.activity-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: var(--shadow-sm);
}

.activity-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.activity-image {
  height: 100px;
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.activity-icon {
  font-size: 40px;
  color: #fff;
}

.activity-info {
  padding: 16px;
}

.activity-info h4 {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 6px 0;
}

.activity-info p {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.member-section {
  padding: 40px 0;
}

.member-card {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
  border-radius: 20px;
  padding: 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: var(--shadow-xl);
}

.member-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.member-avatar {
  background: rgba(255, 255, 255, 0.2);
  border: 3px solid rgba(255, 255, 255, 0.5);
}

.member-info h3 {
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 8px 0;
}

.member-points {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
}

.points-icon {
  font-size: 16px;
}

.member-actions {
  display: flex;
  gap: 12px;
}

.sign-btn {
  height: 44px;
  padding: 0 24px;
  font-weight: 600;
  border-radius: 12px;
  background: #fff;
  color: var(--primary-color);
  border: none;
}

.sign-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.9);
  color: var(--primary-color);
}

.profile-btn {
  height: 44px;
  padding: 0 24px;
  font-weight: 600;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  border: 2px solid rgba(255, 255, 255, 0.5);
}

.profile-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  color: #fff;
}

.guest-card {
  background: white;
  text-align: center;
  flex-direction: column;
  gap: 20px;
}

.guest-content h3 {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 8px 0;
}

.guest-content p {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

.guest-actions {
  display: flex;
  gap: 12px;
}

.login-btn {
  height: 44px;
  padding: 0 32px;
  font-weight: 600;
  border-radius: 12px;
}

.register-btn {
  height: 44px;
  padding: 0 32px;
  font-weight: 600;
  border-radius: 12px;
}

.hero-carousel :deep(.el-carousel__indicators--outside) {
  bottom: 16px;
}

.hero-carousel :deep(.el-carousel__indicator--active .el-carousel__button) {
  background: #fff;
  width: 24px;
}

@media (max-width: 1200px) {
  .categories-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 1024px) {
  .hero-content {
    grid-template-columns: 1fr;
    gap: 32px;
  }

  .hero-right {
    order: -1;
  }

  .hero-title {
    font-size: 32px;
    text-align: center;
  }

  .hero-subtitle-inline {
    text-align: center;
  }

  .hero-description {
    text-align: center;
  }

  .search-container {
    max-width: 600px;
    margin: 0 auto 32px;
  }

  .quick-actions {
    justify-content: center;
  }

  .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .products-grid {
    grid-template-columns: repeat(3, 1fr);
  }

  .activities-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .hero-section {
    padding: 20px 0;
  }

  .hero-title {
    font-size: 28px;
  }

  .hero-description {
    font-size: 14px;
  }

  .quick-actions {
    flex-wrap: wrap;
    gap: 12px;
  }

  .action-item {
    padding: 12px 16px;
  }

  .action-icon {
    width: 40px;
    height: 40px;
    font-size: 20px;
  }

  .features-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .feature-card {
    padding: 16px;
  }

  .feature-icon {
    width: 44px;
    height: 44px;
    font-size: 22px;
  }

  .categories-grid {
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;
  }

  .category-card {
    padding: 16px 12px;
  }

  .category-icon-wrapper {
    width: 48px;
    height: 48px;
  }

  .category-icon {
    font-size: 24px;
  }

  .category-name {
    font-size: 12px;
  }

  .products-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }

  .activities-grid {
    grid-template-columns: 1fr;
  }

  .member-card {
    flex-direction: column;
    text-align: center;
    gap: 20px;
    padding: 24px;
  }

  .member-left {
    flex-direction: column;
  }

  .member-actions {
    width: 100%;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .features-grid {
    grid-template-columns: 1fr;
  }

  .categories-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
