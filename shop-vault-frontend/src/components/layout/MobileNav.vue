<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { HomeFilled, Goods, ShoppingCart, User } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const isLoggedIn = computed(() => !!userStore.token)
const cartCount = computed(() => cartStore.totalCount)

const navItems = computed(() => [
  { path: '/', icon: HomeFilled, label: '首页' },
  { path: '/products', icon: Goods, label: '商品' },
  { path: '/cart', icon: ShoppingCart, label: '购物车', badge: cartCount.value },
  { path: isLoggedIn.value ? '/profile' : '/login', icon: User, label: isLoggedIn.value ? '我的' : '登录' }
])

const isActive = (path: string) => {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}

const navigate = (path: string) => {
  router.push(path)
}
</script>

<template>
  <nav class="mobile-nav hide-desktop">
    <div
      v-for="item in navItems"
      :key="item.path"
      class="mobile-nav-item touch-action"
      :class="{ active: isActive(item.path) }"
      @click="navigate(item.path)"
    >
      <el-badge :value="item.badge" :hidden="!item.badge" :max="99" class="nav-badge">
        <el-icon :size="22">
          <component :is="item.icon" />
        </el-icon>
      </el-badge>
      <span class="nav-label">{{ item.label }}</span>
    </div>
  </nav>
</template>

<style scoped>
.mobile-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-around;
  align-items: center;
  background: #fff;
  border-top: 1px solid var(--border-color);
  padding: 8px 0;
  padding-bottom: calc(8px + env(safe-area-inset-bottom));
  z-index: 1000;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
}

.mobile-nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  padding: 8px 0;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--text-secondary);
}

.mobile-nav-item.active {
  color: var(--primary-color);
}

.mobile-nav-item:active {
  transform: scale(0.95);
}

.nav-badge {
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-label {
  font-size: 10px;
  margin-top: 2px;
  font-weight: 500;
}

@media (min-width: 769px) {
  .hide-desktop {
    display: none !important;
  }
}
</style>
