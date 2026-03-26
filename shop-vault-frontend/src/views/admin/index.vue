<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Shop, SwitchButton, Bell, User, List, Money, Goods, RefreshLeft, ChatDotRound, Present, Medal } from '@element-plus/icons-vue'
import { getDashboardStats } from '@/api/dashboard'
import { useUserStore } from '@/stores/user'
import type { DashboardStats } from '@/types/api'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const stats = ref<DashboardStats | null>(null)

const isMainDashboard = computed(() => route.path === '/admin')

const menuItems: Array<{ path: string; icon: string; title: string; isMain?: boolean }> = [
  { path: '/admin', icon: 'DataAnalysis', title: '数据大屏', isMain: true },
  { path: '/admin/products', icon: 'Goods', title: '商品管理' },
  { path: '/admin/orders', icon: 'List', title: '订单管理' },
  { path: '/admin/after-sales', icon: 'RefreshLeft', title: '售后管理' },
  { path: '/admin/comments', icon: 'ChatDotRound', title: '评价管理' },
  { path: '/admin/users', icon: 'User', title: '用户管理' },
  { path: '/admin/member-days', icon: 'Calendar', title: '会员日活动' },
  { path: '/admin/points-rules', icon: 'Coin', title: '积分倍率' },
  { path: '/admin/points-products', icon: 'Present', title: '积分商城' },
  { path: '/admin/vip-users', icon: 'Medal', title: 'VIP会员' },
  { path: '/admin/chat', icon: 'ChatDotRound', title: '客服消息' }
]

const activeMenu = computed(() => {
  const item = menuItems.find(m => route.path === m.path)
  return item ? item.path : menuItems[0].path
})

const fetchStats = async () => {
  loading.value = true
  try {
    stats.value = await getDashboardStats()
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

onMounted(() => {
  fetchStats()
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
          v-for="item in menuItems"
          :key="item.path"
          class="nav-item"
          :class="{ 'nav-item-active': activeMenu === item.path }"
          @click="handleMenuClick(item.path, !!item.isMain)"
        >
          <el-icon class="nav-icon"><component :is="item.icon" /></el-icon>
          <span class="nav-text">{{ item.title }}</span>
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
          {{ menuItems.find(m => m.path === activeMenu)?.title || '管理后台' }}
        </h2>
        <div class="header-actions">
          <el-badge :value="stats?.pendingOrders || 0" :hidden="!stats?.pendingOrders">
            <el-button circle>
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
          <div class="stats-grid">
            <div class="stat-card stat-card-primary">
              <div class="stat-info">
                <p class="stat-label">总用户数</p>
                <p class="stat-value">{{ stats?.totalUsers || 0 }}</p>
              </div>
              <div class="stat-icon stat-icon-primary">
                <el-icon size="28"><User /></el-icon>
              </div>
            </div>

            <div class="stat-card stat-card-success">
              <div class="stat-info">
                <p class="stat-label">总订单数</p>
                <p class="stat-value">{{ stats?.totalOrders || 0 }}</p>
              </div>
              <div class="stat-icon stat-icon-success">
                <el-icon size="28"><List /></el-icon>
              </div>
            </div>

            <div class="stat-card stat-card-warning">
              <div class="stat-info">
                <p class="stat-label">总销售额</p>
                <p class="stat-value">¥{{ (stats?.totalSales || 0).toFixed(0) }}</p>
              </div>
              <div class="stat-icon stat-icon-warning">
                <el-icon size="28"><Money /></el-icon>
              </div>
            </div>

            <div class="stat-card stat-card-danger">
              <div class="stat-info">
                <p class="stat-label">商品数量</p>
                <p class="stat-value">{{ stats?.totalProducts || 0 }}</p>
              </div>
              <div class="stat-icon stat-icon-danger">
                <el-icon size="28"><Goods /></el-icon>
              </div>
            </div>
          </div>

          <div class="dashboard-grid">
            <div class="dashboard-card">
              <h3 class="card-title">今日数据</h3>
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
                  <span class="data-label">待处理订单</span>
                  <el-tag type="warning">{{ stats?.pendingOrders || 0 }}</el-tag>
                </div>
                <div class="data-item">
                  <span class="data-label">待处理售后</span>
                  <el-tag type="danger">{{ stats?.pendingAfterSales || 0 }}</el-tag>
                </div>
              </div>
            </div>

            <div class="dashboard-card">
              <h3 class="card-title">快捷操作</h3>
              <div class="quick-actions">
                <div class="quick-action-item" @click="router.push('/admin/products')">
                  <div class="quick-action-icon quick-action-icon-primary">
                    <el-icon size="24"><Goods /></el-icon>
                  </div>
                  <p class="quick-action-text">商品管理</p>
                </div>
                <div class="quick-action-item" @click="router.push('/admin/orders')">
                  <div class="quick-action-icon quick-action-icon-success">
                    <el-icon size="24"><List /></el-icon>
                  </div>
                  <p class="quick-action-text">订单管理</p>
                </div>
                <div class="quick-action-item" @click="router.push('/admin/after-sales')">
                  <div class="quick-action-icon quick-action-icon-warning">
                    <el-icon size="24"><RefreshLeft /></el-icon>
                  </div>
                  <p class="quick-action-text">售后处理</p>
                </div>
                <div class="quick-action-item" @click="router.push('/admin/comments')">
                  <div class="quick-action-icon quick-action-icon-danger">
                    <el-icon size="24"><ChatDotRound /></el-icon>
                  </div>
                  <p class="quick-action-text">评价审核</p>
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
  padding: 20px 12px;
  overflow-y: auto;
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

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
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

  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}
</style>
