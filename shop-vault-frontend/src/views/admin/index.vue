<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Shop, SwitchButton, Bell, User, List, Money, RefreshLeft,
  TrendCharts, DataAnalysis, Timer, ShoppingCart, Coin, Present,
  ArrowDown
} from '@element-plus/icons-vue'
import { getDashboardStats } from '@/api/dashboard'
import { useUserStore } from '@/stores/user'
import type { DashboardStats } from '@/types/api'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const stats = ref<DashboardStats | null>(null)
const realtimeTime = ref<string>('')
const autoRefreshInterval = ref<number | null>(null)

const isMainDashboard = computed(() => route.path === '/admin')

interface MenuItem {
  path: string;
  icon: string;
  title: string;
  isMain?: boolean;
}

interface MenuGroup {
  title: string;
  icon: string;
  items: MenuItem[];
}

const menuGroups: MenuGroup[] = [
  {
    title: '数据大屏',
    icon: 'DataAnalysis',
    items: [
      { path: '/admin', icon: 'DataAnalysis', title: '数据大屏', isMain: true }
    ]
  },
  {
    title: '商品订单',
    icon: 'Goods',
    items: [
      { path: '/admin/products', icon: 'Goods', title: '商品管理' },
      { path: '/admin/orders', icon: 'List', title: '订单管理' }
    ]
  },
  {
    title: '售后服务',
    icon: 'RefreshLeft',
    items: [
      { path: '/admin/after-sales', icon: 'RefreshLeft', title: '售后管理' },
      { path: '/admin/comments', icon: 'ChatDotRound', title: '评价管理' },
      { path: '/admin/chat', icon: 'ChatDotRound', title: '客服消息' }
    ]
  },
  {
    title: '会员营销',
    icon: 'User',
    items: [
      { path: '/admin/users', icon: 'User', title: '用户管理' },
      { path: '/admin/vip-users', icon: 'Medal', title: 'VIP会员' },
      { path: '/admin/member-days', icon: 'Calendar', title: '会员日活动' },
      { path: '/admin/points-rules', icon: 'Coin', title: '积分倍率' },
      { path: '/admin/points-products', icon: 'Present', title: '积分商城' }
    ]
  }
]

const expandedGroups = ref<string[]>(['数据监控'])

const toggleGroup = (title: string) => {
  const index = expandedGroups.value.indexOf(title)
  if (index > -1) {
    expandedGroups.value.splice(index, 1)
  } else {
    expandedGroups.value.push(title)
  }
}

const isGroupExpanded = (title: string) => expandedGroups.value.includes(title)

const isItemActive = (path: string) => route.path === path

const isGroupActive = (items: Array<{ path: string }>) => items.some(item => route.path === item.path)

const getCurrentTitle = () => {
  for (const group of menuGroups) {
    const item = group.items.find(i => route.path === i.path)
    if (item) return item.title
  }
  return '管理后台'
}

const conversionRate = computed(() => {
  if (!stats.value?.totalUsers || !stats.value?.totalOrders) return 0
  return ((stats.value.totalOrders / stats.value.totalUsers) * 100).toFixed(1)
})

const avgOrderAmount = computed(() => {
  if (!stats.value?.totalSales || !stats.value?.totalOrders) return 0
  return (stats.value.totalSales / stats.value.totalOrders).toFixed(2)
})

const fetchStats = async () => {
  loading.value = true
  try {
    stats.value = await getDashboardStats()
    realtimeTime.value = new Date().toLocaleTimeString()
  } catch (error) {
    console.error('获取统计数据失败', error)
  } finally {
    loading.value = false
  }
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.clearToken()
    ElMessage.success('已退出登录')
    router.push('/admin/login')
  }).catch(() => {})
}

const handleMenuClick = (path: string, isMain: boolean) => {
  if (isMain) {
    router.push('/admin')
  } else {
    router.push(path)
  }
}

const startAutoRefresh = () => {
  autoRefreshInterval.value = window.setInterval(() => {
    fetchStats()
  }, 30000)
}

const stopAutoRefresh = () => {
  if (autoRefreshInterval.value) {
    clearInterval(autoRefreshInterval.value)
    autoRefreshInterval.value = null
  }
}

const getBarHeight = (count: number, trend: any[] | undefined) => {
  if (!trend || trend.length === 0) return 0
  const maxCount = Math.max(...trend.map(t => t.count))
  if (maxCount === 0) return 0
  return (count / maxCount) * 100
}

onMounted(() => {
  fetchStats()
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <div class="sidebar-header">
        <div class="logo-wrapper">
          <el-icon size="28" class="logo-icon"><Shop /></el-icon>
        </div>
        <div class="brand-info">
          <h1 class="brand-title">小铺宝库</h1>
          <p class="brand-subtitle">管理后台</p>
        </div>
      </div>

      <nav class="sidebar-nav">
        <div
          v-for="group in menuGroups"
          :key="group.title"
          class="nav-group"
        >
          <div 
            class="nav-group-header"
            :class="{ 'nav-group-active': isGroupActive(group.items) }"
            @click="toggleGroup(group.title)"
          >
            <el-icon class="nav-group-icon"><component :is="group.icon" /></el-icon>
            <span class="nav-group-title">{{ group.title }}</span>
            <el-icon class="nav-arrow" :class="{ 'nav-arrow-expanded': isGroupExpanded(group.title) }">
              <ArrowDown />
            </el-icon>
          </div>
          <div v-show="isGroupExpanded(group.title)" class="nav-group-items">
            <div
              v-for="item in group.items"
              :key="item.path"
              class="nav-item"
              :class="{ 'nav-item-active': isItemActive(item.path) }"
              @click="handleMenuClick(item.path, !!item.isMain)"
            >
              <el-icon class="nav-icon"><component :is="item.icon" /></el-icon>
              <span class="nav-text">{{ item.title }}</span>
            </div>
          </div>
        </div>
      </nav>

      <div class="sidebar-footer">
        <div class="logout-btn" @click="handleLogout">
          <el-icon class="logout-icon"><SwitchButton /></el-icon>
          <span class="logout-text">退出登录</span>
        </div>
      </div>
    </aside>

    <main class="admin-main">
      <header class="admin-header">
        <h2 class="header-title">
          {{ getCurrentTitle() }}
        </h2>
        <div class="header-actions">
          <el-badge :value="stats?.pendingOrders || 0" :hidden="!stats?.pendingOrders">
            <el-button circle @click="router.push('/admin/orders')">
              <el-icon><Bell /></el-icon>
            </el-button>
          </el-badge>
          <el-avatar :size="36" class="user-avatar">
            {{ userStore.userInfo?.username?.charAt(0) || 'A' }}
          </el-avatar>
        </div>
      </header>

      <div class="admin-content">
        <router-view v-if="!isMainDashboard" />

        <div v-else v-loading="loading" class="dashboard-container">
          <div class="dashboard-header">
            <div class="header-left">
              <h2 class="dashboard-title">实时数据监控</h2>
              <p class="dashboard-subtitle">数据每30秒自动刷新</p>
            </div>
            <div class="header-right">
              <span class="realtime-time">
                <el-icon><Timer /></el-icon>
                最后更新: {{ realtimeTime }}
              </span>
              <el-button type="primary" :loading="loading" @click="fetchStats">
                <el-icon><RefreshLeft /></el-icon>
                刷新数据
              </el-button>
            </div>
          </div>

          <div class="stats-grid">
            <div class="stat-card stat-card-primary">
              <div class="stat-info">
                <p class="stat-label">总用户数</p>
                <p class="stat-value">{{ stats?.totalUsers || 0 }}</p>
                <p class="stat-trend stat-trend-up">
                  <span>今日新增 {{ stats?.todayNewUsers || 0 }}</span>
                </p>
              </div>
              <div class="stat-icon stat-icon-primary">
                <el-icon size="28"><User /></el-icon>
              </div>
            </div>

            <div class="stat-card stat-card-success">
              <div class="stat-info">
                <p class="stat-label">总订单数</p>
                <p class="stat-value">{{ stats?.totalOrders || 0 }}</p>
                <p class="stat-trend stat-trend-up">
                  <span>今日 {{ stats?.todayOrders || 0 }} 单</span>
                </p>
              </div>
              <div class="stat-icon stat-icon-success">
                <el-icon size="28"><List /></el-icon>
              </div>
            </div>

            <div class="stat-card stat-card-warning">
              <div class="stat-info">
                <p class="stat-label">总销售额</p>
                <p class="stat-value">¥{{ (stats?.totalSales || 0).toFixed(0) }}</p>
                <p class="stat-trend stat-trend-up">
                  <span>今日 ¥{{ (stats?.todaySales || 0).toFixed(2) }}</span>
                </p>
              </div>
              <div class="stat-icon stat-icon-warning">
                <el-icon size="28"><Money /></el-icon>
              </div>
            </div>

            <div class="stat-card stat-card-info">
              <div class="stat-info">
                <p class="stat-label">转化率</p>
                <p class="stat-value">{{ conversionRate }}%</p>
                <p class="stat-trend">
                  <span>客单价 ¥{{ avgOrderAmount }}</span>
                </p>
              </div>
              <div class="stat-icon stat-icon-info">
                <el-icon size="28"><TrendCharts /></el-icon>
              </div>
            </div>
          </div>

          <div class="metrics-row">
            <div class="metric-card">
              <div class="metric-icon metric-icon-dau">
                <el-icon><User /></el-icon>
              </div>
              <div class="metric-info">
                <span class="metric-value">{{ stats?.dau || 0 }}</span>
                <span class="metric-label">日活用户(DAU)</span>
              </div>
            </div>
            <div class="metric-card">
              <div class="metric-icon metric-icon-wau">
                <el-icon><User /></el-icon>
              </div>
              <div class="metric-info">
                <span class="metric-value">{{ stats?.wau || 0 }}</span>
                <span class="metric-label">周活用户(WAU)</span>
              </div>
            </div>
            <div class="metric-card">
              <div class="metric-icon metric-icon-mau">
                <el-icon><User /></el-icon>
              </div>
              <div class="metric-info">
                <span class="metric-value">{{ stats?.mau || 0 }}</span>
                <span class="metric-label">月活用户(MAU)</span>
              </div>
            </div>
            <div class="metric-card">
              <div class="metric-icon metric-icon-points">
                <el-icon><Coin /></el-icon>
              </div>
              <div class="metric-info">
                <span class="metric-value">{{ stats?.totalPointsIssued || 0 }}</span>
                <span class="metric-label">累计发放积分</span>
              </div>
            </div>
            <div class="metric-card">
              <div class="metric-icon metric-icon-used">
                <el-icon><Present /></el-icon>
              </div>
              <div class="metric-info">
                <span class="metric-value">{{ stats?.totalPointsUsed || 0 }}</span>
                <span class="metric-label">累计消耗积分</span>
              </div>
            </div>
          </div>

          <div class="dashboard-grid">
            <div class="dashboard-card dashboard-card-large">
              <h3 class="card-title">
                <el-icon><TrendCharts /></el-icon>
                订单趋势（近7天）
              </h3>
              <div class="chart-container">
                <div class="simple-chart">
                  <div class="chart-bars">
                    <div 
                      v-for="(item, index) in stats?.orderTrend || []" 
                      :key="index"
                      class="chart-bar-wrapper"
                    >
                      <div class="chart-bar-container">
                        <div 
                          class="chart-bar" 
                          :style="{ height: getBarHeight(item.count, stats?.orderTrend) + '%' }"
                        >
                          <span class="bar-tooltip">{{ item.count }}单</span>
                        </div>
                      </div>
                      <span class="chart-label">{{ item.date }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="dashboard-card">
              <h3 class="card-title">
                <el-icon><DataAnalysis /></el-icon>
                今日数据
              </h3>
              <div class="data-list">
                <div class="data-item">
                  <span class="data-label">今日订单</span>
                  <span class="data-value">{{ stats?.todayOrders || 0 }}</span>
                </div>
                <div class="data-item">
                  <span class="data-label">今日销售额</span>
                  <span class="data-value data-value-highlight">¥{{ (stats?.todaySales || 0).toFixed(2) }}</span>
                </div>
                <div class="data-item">
                  <span class="data-label">今日新用户</span>
                  <span class="data-value">{{ stats?.todayNewUsers || 0 }}</span>
                </div>
              </div>
            </div>

            <div class="dashboard-card">
              <h3 class="card-title">
                <el-icon><ShoppingCart /></el-icon>
                热销商品TOP5
              </h3>
              <div class="hot-products">
                <div 
                  v-for="(product, index) in (stats?.hotProducts || []).slice(0, 5)" 
                  :key="product.productId"
                  class="hot-product-item"
                >
                  <span class="product-rank" :class="'rank-' + (index + 1)">{{ index + 1 }}</span>
                  <img v-if="product.productImage" :src="product.productImage" class="product-image" />
                  <div class="product-info">
                    <span class="product-name">{{ product.productName }}</span>
                    <span class="product-sales">销量: {{ product.sales }}</span>
                  </div>
                  <span class="product-amount">¥{{ product.amount?.toFixed(0) || 0 }}</span>
                </div>
                <div v-if="!stats?.hotProducts?.length" class="empty-data">
                  暂无热销商品数据
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: linear-gradient(135deg, #f0f2f5 0%, #e8ecf1 100%);
}

.admin-sidebar {
  width: 260px;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 100;
  box-shadow: 4px 0 20px rgba(0, 0, 0, 0.15);
}

.sidebar-header {
  padding: 28px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  gap: 14px;
}

.logo-wrapper {
  width: 52px;
  height: 52px;
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 16px rgba(22, 119, 255, 0.4);
}

.logo-icon {
  color: white;
}

.brand-info {
  flex: 1;
}

.brand-title {
  font-size: 18px;
  font-weight: 700;
  color: white;
  margin: 0 0 4px 0;
}

.brand-subtitle {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
}

.sidebar-nav {
  flex: 1;
  padding: 16px 12px;
  overflow-y: auto;
}

.nav-group {
  margin-bottom: 8px;
}

.nav-group-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: rgba(255, 255, 255, 0.7);
}

.nav-group-header:hover {
  background: rgba(255, 255, 255, 0.08);
  color: white;
}

.nav-group-active {
  color: white;
  background: rgba(22, 119, 255, 0.2);
}

.nav-group-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.nav-group-title {
  flex: 1;
  font-size: 14px;
  font-weight: 600;
}

.nav-arrow {
  font-size: 12px;
  transition: transform 0.3s ease;
}

.nav-arrow-expanded {
  transform: rotate(180deg);
}

.nav-group-items {
  margin-top: 4px;
  padding-left: 12px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  border-radius: 12px;
  margin-bottom: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: rgba(255, 255, 255, 0.7);
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.08);
  color: white;
  transform: translateX(4px);
}

.nav-item-active {
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
  color: white;
  box-shadow: 0 4px 16px rgba(22, 119, 255, 0.4);
}

.nav-item-active:hover {
  transform: none;
}

.nav-icon {
  font-size: 20px;
  flex-shrink: 0;
}

.nav-text {
  font-size: 15px;
  font-weight: 500;
}

.sidebar-footer {
  padding: 20px 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.logout-btn {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #ffffff;
  background: rgba(255, 77, 79, 0.4);
  border: 1px solid rgba(255, 77, 79, 0.6);
}

.logout-btn:hover {
  background: rgba(255, 77, 79, 0.6);
  color: #ffffff;
  border-color: rgba(255, 77, 79, 0.8);
  transform: translateX(4px);
}

.logout-icon {
  font-size: 20px;
  flex-shrink: 0;
}

.logout-text {
  font-size: 15px;
  font-weight: 500;
}

.admin-main {
  flex: 1;
  margin-left: 260px;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.admin-header {
  background: white;
  padding: 20px 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 50;
}

.header-title {
  font-size: 20px;
  font-weight: 700;
  color: #1f2329;
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-avatar {
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
  color: white;
  font-weight: 600;
}

.admin-content {
  flex: 1;
  padding: 28px 32px;
}

.dashboard-container {
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-bottom: 28px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  border: 1px solid rgba(0, 0, 0, 0.04);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #86909c;
  margin: 0 0 8px 0;
  font-weight: 500;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #1f2329;
  margin: 0;
  line-height: 1.2;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon-primary {
  background: linear-gradient(135deg, #e6f4ff 0%, #bae0ff 100%);
  color: #1677ff;
}

.stat-icon-success {
  background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
  color: #52c41a;
}

.stat-icon-warning {
  background: linear-gradient(135deg, #fffbe6 0%, #ffe58f 100%);
  color: #faad14;
}

.stat-icon-danger {
  background: linear-gradient(135deg, #fff1f0 0%, #ffccc7 100%);
  color: #ff4d4f;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.dashboard-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(0, 0, 0, 0.04);
}

.card-title {
  font-size: 18px;
  font-weight: 700;
  color: #1f2329;
  margin: 0 0 20px 0;
  padding-bottom: 16px;
  border-bottom: 1px solid #f2f3f5;
}

.data-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.data-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.data-label {
  font-size: 15px;
  color: #4e5969;
}

.data-value {
  font-size: 18px;
  font-weight: 700;
  color: #1f2329;
}

.data-value-highlight {
  color: #ff4d4f;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.quick-action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  border: 1px solid #e5e6eb;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.quick-action-item:hover {
  border-color: #1677ff;
  background: #f0f5ff;
  transform: translateY(-2px);
}

.quick-action-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
}

.quick-action-icon-primary {
  background: linear-gradient(135deg, #e6f4ff 0%, #bae0ff 100%);
  color: #1677ff;
}

.quick-action-icon-success {
  background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
  color: #52c41a;
}

.quick-action-icon-warning {
  background: linear-gradient(135deg, #fffbe6 0%, #ffe58f 100%);
  color: #faad14;
}

.quick-action-icon-danger {
  background: linear-gradient(135deg, #fff1f0 0%, #ffccc7 100%);
  color: #ff4d4f;
}

.quick-action-text {
  font-size: 14px;
  font-weight: 600;
  color: #1f2329;
  margin: 0;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px 24px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.dashboard-title {
  font-size: 24px;
  font-weight: 700;
  color: #1f2329;
  margin: 0;
}

.dashboard-subtitle {
  font-size: 14px;
  color: #86909c;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.realtime-time {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #86909c;
}

.stat-trend {
  font-size: 12px;
  color: #86909c;
  margin-top: 4px;
}

.stat-trend-up {
  color: #52c41a;
}

.stat-card-info {
  border-left: 4px solid #1677ff;
}

.stat-icon-info {
  background: linear-gradient(135deg, #e6f4ff 0%, #bae0ff 100%);
  color: #1677ff;
}

.metrics-row {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.metric-card {
  background: white;
  border-radius: 12px;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.metric-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.metric-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.metric-icon-dau {
  background: linear-gradient(135deg, #e6f4ff 0%, #bae0ff 100%);
  color: #1677ff;
}

.metric-icon-wau {
  background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
  color: #52c41a;
}

.metric-icon-mau {
  background: linear-gradient(135deg, #fffbe6 0%, #ffe58f 100%);
  color: #faad14;
}

.metric-icon-points {
  background: linear-gradient(135deg, #fff1f0 0%, #ffccc7 100%);
  color: #ff4d4f;
}

.metric-icon-used {
  background: linear-gradient(135deg, #f9f0ff 0%, #efdbff 100%);
  color: #722ed1;
}

.metric-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.metric-value {
  font-size: 20px;
  font-weight: 700;
  color: #1f2329;
}

.metric-label {
  font-size: 12px;
  color: #86909c;
}

.dashboard-card-large {
  grid-column: span 2;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.chart-container {
  height: 200px;
  padding: 16px 0;
}

.simple-chart {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chart-bars {
  flex: 1;
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  gap: 12px;
}

.chart-bar-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
}

.chart-bar-container {
  flex: 1;
  width: 100%;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.chart-bar {
  width: 60%;
  max-width: 40px;
  background: linear-gradient(180deg, #1677ff 0%, #4096ff 100%);
  border-radius: 6px 6px 0 0;
  position: relative;
  transition: all 0.3s ease;
  min-height: 4px;
}

.chart-bar:hover {
  background: linear-gradient(180deg, #4096ff 0%, #69b1ff 100%);
}

.bar-tooltip {
  position: absolute;
  top: -28px;
  left: 50%;
  transform: translateX(-50%);
  background: #1f2329;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  white-space: nowrap;
  opacity: 0;
  transition: opacity 0.2s;
}

.chart-bar:hover .bar-tooltip {
  opacity: 1;
}

.chart-label {
  font-size: 12px;
  color: #86909c;
  margin-top: 8px;
}

.hot-products {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hot-product-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f7f8fa;
  border-radius: 10px;
  transition: all 0.2s;
}

.hot-product-item:hover {
  background: #f0f5ff;
}

.product-rank {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  background: #e5e6eb;
  color: #86909c;
}

.rank-1 {
  background: linear-gradient(135deg, #ffd591 0%, #fa8c16 100%);
  color: white;
}

.rank-2 {
  background: linear-gradient(135deg, #bfbfbf 0%, #8c8c8c 100%);
  color: white;
}

.rank-3 {
  background: linear-gradient(135deg, #ffc069 0%, #d48806 100%);
  color: white;
}

.product-image {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  object-fit: cover;
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.product-name {
  font-size: 14px;
  font-weight: 500;
  color: #1f2329;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-sales {
  font-size: 12px;
  color: #86909c;
}

.product-amount {
  font-size: 14px;
  font-weight: 600;
  color: #ff4d4f;
}

.empty-data {
  text-align: center;
  padding: 24px;
  color: #86909c;
  font-size: 14px;
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .metrics-row {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .admin-sidebar {
    width: 80px;
  }

  .sidebar-header {
    padding: 16px 12px;
    flex-direction: column;
    text-align: center;
  }

  .logo-wrapper {
    width: 44px;
    height: 44px;
  }

  .brand-info {
    display: none;
  }

  .nav-text {
    display: none;
  }

  .nav-item {
    justify-content: center;
    padding: 14px;
  }

  .sidebar-footer {
    padding: 12px;
  }

  .logout-btn {
    justify-content: center;
    padding: 14px;
  }

  .logout-text {
    display: none;
  }

  .admin-main {
    margin-left: 80px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .metrics-row {
    grid-template-columns: repeat(2, 1fr);
  }

  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}
</style>
