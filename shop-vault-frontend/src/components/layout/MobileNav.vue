<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const isLoggedIn = computed(() => !!userStore.token)
const cartCount = computed(() => cartStore.totalCount)

const navItems = computed(() => [
  { path: '/', icon: 'HomeFilled', label: '首页' },
  { path: '/products', icon: 'Goods', label: '商品' },
  { path: '/cart', icon: 'ShoppingCart', label: '购物车', badge: cartCount.value },
  { path: isLoggedIn.value ? '/profile' : '/login', icon: 'User', label: isLoggedIn.value ? '我的' : '登录' }
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
    <div class="grid grid-cols-4">
      <div
        v-for="item in navItems"
        :key="item.path"
        class="mobile-nav-item touch-action"
        :class="{ active: isActive(item.path) }"
        @click="navigate(item.path)"
      >
        <el-badge :value="item.badge" :hidden="!item.badge" :max="99">
          <el-icon :size="22">
            <component :is="item.icon" />
          </el-icon>
        </el-badge>
        <span>{{ item.label }}</span>
      </div>
    </div>
  </nav>
</template>
