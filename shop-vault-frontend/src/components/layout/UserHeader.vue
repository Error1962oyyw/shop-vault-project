<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ArrowDown, User, List, Star, Coin, SwitchButton, 
  Shop, Search, ShoppingCart, Service, Camera, Menu,
  HomeFilled, Goods, Present
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { useCategoryStore } from '@/stores/category'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()
const categoryStore = useCategoryStore()

const searchKeyword = ref('')
const showMobileMenu = ref(false)
const isMobile = ref(false)

const isLoggedIn = computed(() => !!userStore.token)
const cartCount = computed(() => cartStore.totalCount)

const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/products', query: { keyword: searchKeyword.value.trim() } })
    searchKeyword.value = ''
    showMobileMenu.value = false
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
    router.push('/login')
  }).catch(() => {})
}

const handleCommand = (command: string) => {
  showMobileMenu.value = false
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'orders':
      router.push('/orders')
      break
    case 'favorites':
      router.push('/favorites')
      break
    case 'cart':
      router.push('/cart')
      break
    case 'points':
      router.push('/points')
      break
    case 'points-mall':
      router.push('/points-mall')
      break
    case 'logout':
      handleLogout()
      break
  }
}

const protectedPaths = ['/chat', '/favorites', '/ai-search', '/cart', '/orders', '/points', '/points-mall', '/profile', '/coupons', '/member-day']

const requireLogin = (path: string) => {
  if (!isLoggedIn.value && protectedPaths.some(p => path.startsWith(p))) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return true
  }
  return false
}

const goTo = (path: string) => {
  if (requireLogin(path)) return
  router.push(path)
  showMobileMenu.value = false
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  categoryStore.fetchCategories()
  if (isLoggedIn.value) {
    cartStore.fetchCartList()
  }
})
</script>

<template>
  <header class="header-wrapper">
    <div class="header-top">
      <div class="header-container">
        <div class="top-left">
          <template v-if="isLoggedIn">
            <el-dropdown @command="handleCommand">
              <span class="user-dropdown">
                <el-avatar :size="24" :src="userStore.userInfo?.avatar">
                  {{ (userStore.userInfo?.nickname || userStore.userInfo?.username || 'U')?.charAt(0) }}
                </el-avatar>
                <span class="user-name">{{ userStore.userInfo?.nickname || userStore.userInfo?.username || '用户' }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>个人中心
                  </el-dropdown-item>
                  <el-dropdown-item command="orders">
                    <el-icon><List /></el-icon>我的订单
                  </el-dropdown-item>
                  <el-dropdown-item command="favorites">
                    <el-icon><Star /></el-icon>我的收藏
                  </el-dropdown-item>
                  <el-dropdown-item command="cart">
                    <el-icon><ShoppingCart /></el-icon>购物车
                    <el-badge v-if="cartCount > 0" :value="cartCount" :max="99" class="cart-badge" />
                  </el-dropdown-item>
                  <el-dropdown-item command="points">
                    <el-icon><Coin /></el-icon>会员中心
                  </el-dropdown-item>
                  <el-dropdown-item command="points-mall">
                    <el-icon><Present /></el-icon>积分商城
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon>退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <span class="guest-info">
              <el-avatar :size="24">
                游
              </el-avatar>
              <span class="guest-name">游客</span>
            </span>
            <router-link to="/login" class="top-link">登录</router-link>
            <router-link to="/register" class="top-link">注册</router-link>
          </template>
        </div>
        <div class="top-right">
          <router-link v-if="isLoggedIn" to="/orders" class="top-link">我的订单</router-link>
          <router-link v-if="isLoggedIn" to="/favorites" class="top-link">我的收藏</router-link>
          <router-link to="/products" class="top-link">全部商品</router-link>
        </div>
      </div>
    </div>

    <div class="header-main">
      <div class="header-container">
        <div class="main-content">
          <router-link to="/" class="logo-section">
            <div class="logo-icon">
              <el-icon><Shop /></el-icon>
            </div>
            <span class="logo-text">小铺宝库</span>
          </router-link>

          <div class="search-section">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索商品..."
              class="search-input"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" class="search-btn" @click="handleSearch">
              搜索
            </el-button>
          </div>

          <div class="action-section">
            <div class="action-item hide-mobile" @click="goTo('/ai-search')">
              <div class="action-icon action-icon-ai">
                <el-icon><Camera /></el-icon>
              </div>
              <span class="action-label">AI识图</span>
            </div>
            <div class="action-item hide-mobile" @click="goTo('/chat')">
              <div class="action-icon">
                <el-icon><Service /></el-icon>
              </div>
              <span class="action-label">客服</span>
            </div>
            <el-button 
              class="show-mobile mobile-menu-btn" 
              circle 
              @click="showMobileMenu = !showMobileMenu"
            >
              <el-icon><Menu /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <nav class="header-nav hide-mobile">
      <div class="header-container">
        <div class="nav-content">
          <div class="nav-links">
            <router-link 
              to="/" 
              class="nav-link"
              :class="{ active: route.path === '/' }"
            >
              首页
            </router-link>
            <router-link 
              to="/products"
              class="nav-link"
              :class="{ active: route.path.startsWith('/products') }"
            >
              全部商品
            </router-link>
            <router-link
              to="/points"
              class="nav-link"
              :class="{ active: route.path === '/points' }"
              @click.prevent="requireLogin('/points') || null"
            >
              会员中心
            </router-link>
            <router-link
              to="/points-mall"
              class="nav-link"
              :class="{ active: route.path === '/points-mall' }"
              @click.prevent="requireLogin('/points-mall') || null"
            >
              积分商城
            </router-link>
          </div>
        </div>
      </div>
    </nav>

    <el-drawer
      v-model="showMobileMenu"
      direction="rtl"
      size="80%"
      title="菜单"
      class="mobile-drawer"
    >
      <div class="mobile-menu">
        <template v-if="isLoggedIn">
          <div class="mobile-user-card">
            <el-avatar :size="56" :src="userStore.userInfo?.avatar">
              {{ userStore.userInfo?.nickname?.charAt(0) || userStore.userInfo?.username?.charAt(0) }}
            </el-avatar>
            <div class="mobile-user-info">
              <div class="mobile-user-name">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</div>
              <div class="mobile-user-points">积分: {{ userStore.userInfo?.points || 0 }}</div>
            </div>
          </div>
          
          <div class="mobile-quick-actions">
            <div class="quick-action-item" @click="goTo('/orders')">
              <el-icon><List /></el-icon>
              <span>订单</span>
            </div>
            <div class="quick-action-item" @click="goTo('/favorites')">
              <el-icon><Star /></el-icon>
              <span>收藏</span>
            </div>
            <div class="quick-action-item" @click="goTo('/points-mall')">
              <el-icon><Present /></el-icon>
              <span>积分商城</span>
            </div>
            <div class="quick-action-item" @click="goTo('/points')">
              <el-icon><Coin /></el-icon>
              <span>会员中心</span>
            </div>
          </div>
        </template>
        
        <el-divider />
        
        <div class="mobile-nav-list">
          <div class="mobile-nav-item" @click="goTo('/')">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </div>
          <div class="mobile-nav-item" @click="goTo('/products')">
            <el-icon><Goods /></el-icon>
            <span>全部商品</span>
          </div>
          <div class="mobile-nav-item" @click="goTo('/ai-search')">
            <el-icon><Camera /></el-icon>
            <span>AI识图搜索</span>
          </div>
          <div class="mobile-nav-item" @click="goTo('/points')">
            <el-icon><Coin /></el-icon>
            <span>会员中心</span>
          </div>
          <div class="mobile-nav-item" @click="goTo('/points-mall')">
            <el-icon><Present /></el-icon>
            <span>积分商城</span>
          </div>
          <div class="mobile-nav-item" @click="goTo('/chat')">
            <el-icon><Service /></el-icon>
            <span>在线客服</span>
          </div>
          <div v-if="isLoggedIn" class="mobile-nav-item" @click="goTo('/profile')">
            <el-icon><User /></el-icon>
            <span>个人中心</span>
          </div>
        </div>
        
        <el-divider content-position="left">商品分类</el-divider>
        
        <div class="mobile-category-grid">
          <div
            v-for="category in categoryStore.categories"
            :key="category.id"
            class="mobile-category-item"
            @click="goTo(`/products?categoryId=${category.id}`)"
          >
            {{ category.name }}
          </div>
        </div>
        
        <template v-if="isLoggedIn">
          <el-divider />
          <div class="mobile-logout" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            <span>退出登录</span>
          </div>
        </template>
        <template v-else>
          <el-divider />
          <div class="mobile-auth-btns">
            <el-button type="primary" class="flex-1" @click="goTo('/login')">登录</el-button>
            <el-button class="flex-1" @click="goTo('/register')">注册</el-button>
          </div>
        </template>
      </div>
    </el-drawer>
  </header>
</template>

<style scoped>
.header-wrapper {
  background: #fff;
  box-shadow: var(--shadow-sm);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.header-top {
  background: #f7f8fa;
  border-bottom: 1px solid var(--border-light);
  font-size: 13px;
}

.header-top .header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 36px;
}

.top-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: var(--text-primary);
  transition: color 0.2s;
}

.user-dropdown:hover {
  color: var(--primary-color);
}

.user-name {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 36px;
  font-size: 13px;
}

.guest-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-right: 16px;
}

.user-dropdown .el-avatar {
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
  color: white;
  font-weight: 600;
}

.guest-info .el-avatar {
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
  color: white;
  font-size: 12px;
  font-weight: 600;
}

.guest-name {
  font-size: 13px;
  color: var(--text-secondary);
}

.top-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.top-link {
  color: var(--text-secondary);
  text-decoration: none;
  transition: color 0.2s;
}

.top-link:hover {
  color: var(--primary-color);
}

.divider {
  color: var(--border-light);
}

.header-main {
  padding: 16px 0;
}

.main-content {
  display: flex;
  align-items: center;
  gap: 32px;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  flex-shrink: 0;
}

.logo-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 22px;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.search-section {
  flex: 1;
  max-width: 500px;
  display: flex;
  align-items: stretch;
}

.search-input {
  flex: 1;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 12px 0 0 12px;
  border-right: none;
  box-shadow: 0 0 0 1px var(--primary-color);
  height: 40px;
}

.search-input :deep(.el-input__inner) {
  height: 38px;
  line-height: 38px;
}

.search-input :deep(.el-input__wrapper:focus-within) {
  box-shadow: 0 0 0 2px var(--primary-color);
}

.search-btn {
  border-radius: 0 12px 12px 0;
  height: 40px;
  padding: 0 24px;
  font-weight: 600;
  display: flex;
  align-items: center;
}

.action-section {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-shrink: 0;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.action-item:hover {
  color: var(--primary-color);
}

.action-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: var(--primary-50);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: var(--primary-color);
  transition: all 0.2s;
}

.action-item:hover .action-icon {
  background: var(--primary-color);
  color: #fff;
}

.action-icon-ai {
  background: linear-gradient(135deg, #f0e6ff 0%, #d3adf7 100%);
  color: #722ed1;
}

.action-item:hover .action-icon-ai {
  background: linear-gradient(135deg, #722ed1 0%, #9c27b0 100%);
  color: #fff;
}

.action-label {
  font-size: 12px;
  color: var(--text-secondary);
}

.cart-badge {
  margin-left: 8px;
}

.cart-badge :deep(.el-badge__content) {
  font-size: 10px;
}

.mobile-menu-btn {
  display: none;
}

.header-nav {
  border-top: 1px solid var(--border-light);
  background: #fff;
}

.nav-content {
  display: flex;
  align-items: center;
}

.nav-category-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: var(--primary-color);
  color: #fff;
  border-radius: 8px 8px 0 0;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
}

.nav-category-btn.active {
  background: var(--primary-dark);
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 32px;
  margin-left: 32px;
}

.nav-link {
  padding: 12px 0;
  color: var(--text-primary);
  text-decoration: none;
  font-weight: 500;
  position: relative;
  transition: color 0.2s;
}

.nav-link:hover {
  color: var(--primary-color);
}

.nav-link.active {
  color: var(--primary-color);
}

.nav-link.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: var(--primary-color);
  border-radius: 1px;
}

.nav-link.highlight {
  color: #ff6b6b;
}

.nav-link.highlight:hover {
  color: #ff4757;
}

.category-dropdown {
  width: 240px;
  background: #fff;
  border-radius: 0 0 12px 12px;
  box-shadow: var(--shadow-lg);
  overflow: hidden;
  max-height: 400px;
  overflow-y: auto;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  color: var(--text-primary);
  text-decoration: none;
  transition: all 0.2s;
}

.category-item:hover {
  background: var(--primary-50);
  color: var(--primary-color);
}

.category-child {
  padding-left: 36px;
  font-size: 13px;
  color: var(--text-secondary);
}

.mobile-drawer :deep(.el-drawer__header) {
  margin-bottom: 0;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-light);
}

.mobile-menu {
  padding: 16px;
  padding-bottom: calc(80px + env(safe-area-inset-bottom));
  min-height: calc(100vh - 60px);
}

.mobile-user-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
  border-radius: 16px;
  color: #fff;
}

.mobile-user-card .el-avatar {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  font-weight: 600;
}

.mobile-user-info {
  flex: 1;
}

.mobile-user-name {
  font-size: 16px;
  font-weight: 600;
}

.mobile-user-points {
  font-size: 13px;
  opacity: 0.9;
  margin-top: 4px;
}

.mobile-quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-top: 16px;
}

.quick-action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 12px;
  background: var(--primary-50);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.quick-action-item:hover {
  background: var(--primary-100);
}

.quick-action-item .el-icon {
  font-size: 24px;
  color: var(--primary-color);
}

.quick-action-item span {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
}

.mobile-nav-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.mobile-nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: #f7f8fa;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.mobile-nav-item:hover {
  background: var(--primary-50);
  color: var(--primary-color);
}

.mobile-category-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.mobile-category-item {
  padding: 12px;
  background: #f7f8fa;
  border-radius: 10px;
  text-align: center;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.mobile-category-item:hover {
  background: var(--primary-50);
  color: var(--primary-color);
}

.mobile-logout {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 16px;
  background: #fff1f0;
  border-radius: 12px;
  color: #ff4d4f;
  cursor: pointer;
  font-weight: 500;
  margin-top: 8px;
}

.mobile-auth-btns {
  display: flex;
  gap: 12px;
}

.show-mobile {
  display: none;
}

@media (max-width: 768px) {
  .hide-mobile {
    display: none !important;
  }
  
  .show-mobile {
    display: flex;
  }
  
  .header-top {
    display: none;
  }
  
  .header-main {
    padding: 12px 0;
  }
  
  .main-content {
    gap: 12px;
    flex-wrap: wrap;
  }
  
  .logo-section {
    flex-shrink: 0;
  }
  
  .logo-icon {
    width: 36px;
    height: 36px;
    font-size: 18px;
  }
  
  .logo-text {
    font-size: 18px;
  }
  
  .search-section {
    order: 3;
    width: 100%;
    max-width: none;
    display: flex;
    align-items: stretch;
  }
  
  .search-input {
    flex: 1;
  }
  
  .search-input :deep(.el-input__wrapper) {
    border-radius: 12px 0 0 12px;
    height: 40px;
  }
  
  .search-input :deep(.el-input__inner) {
    height: 38px;
    line-height: 38px;
  }
  
  .search-btn {
    flex-shrink: 0;
    border-radius: 0 12px 12px 0;
    height: 40px;
    display: flex;
    align-items: center;
  }
  
  .action-section {
    gap: 8px;
  }
  
  .action-icon {
    width: 32px;
    height: 32px;
    font-size: 16px;
  }
  
  .action-label {
    display: none;
  }
  
  .header-nav {
    display: none;
  }
}
</style>
