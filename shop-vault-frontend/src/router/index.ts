import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/index.vue'),
      meta: { guest: true }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/register/index.vue'),
      meta: { guest: true }
    },
    {
      path: '/forgot-password',
      name: 'ForgotPassword',
      component: () => import('@/views/forgot-password/index.vue'),
      meta: { guest: true }
    },
    {
      path: '/',
      name: 'Home',
      component: () => import('@/views/home/index.vue'),
      meta: { allowGuest: true }
    },
    {
      path: '/products',
      name: 'Products',
      component: () => import('@/views/products/index.vue'),
      meta: { allowGuest: true }
    },
    {
      path: '/product/:id',
      name: 'ProductDetail',
      component: () => import('@/views/product/index.vue'),
      meta: { allowGuest: true }
    },
    {
      path: '/ai-search',
      name: 'AISearch',
      component: () => import('@/views/ai-search/index.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/cart',
      name: 'Cart',
      component: () => import('@/views/cart/index.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/checkout',
      name: 'Checkout',
      component: () => import('@/views/checkout/index.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/orders',
      name: 'Orders',
      component: () => import('@/views/orders/index.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/order/pay/:orderNo',
      name: 'OrderPay',
      component: () => import('@/views/order/pay.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/profile',
      name: 'Profile',
      component: () => import('@/views/profile/index.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/points',
      name: 'Points',
      component: () => import('@/views/points/index.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/points-mall',
      name: 'PointsMall',
      component: () => import('@/views/points-mall/index.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/chat',
      name: 'Chat',
      component: () => import('@/views/chat/index.vue'),
      meta: { requiresAuth: true }
    },
    {
          path: '/member-day',
          name: 'MemberDay',
          component: () => import('@/views/member-day/index.vue'),
          meta: { requiresAuth: true }
        },
    {
      path: '/messages',
      name: 'Messages',
      component: () => import('@/views/messages/index.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/favorites',
      name: 'Favorites',
      component: () => import('@/views/favorites/index.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/admin/login',
      name: 'AdminLogin',
      component: () => import('@/views/admin/login.vue'),
      meta: { guest: true }
    },
    {
      path: '/admin',
      name: 'Admin',
      component: () => import('@/views/admin/index.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        {
          path: 'products',
          name: 'AdminProducts',
          component: () => import('@/views/admin/products.vue')
        },
        {
          path: 'orders',
          name: 'AdminOrders',
          component: () => import('@/views/admin/orders.vue')
        },
        {
          path: 'after-sales',
          name: 'AdminAfterSales',
          component: () => import('@/views/admin/after-sales.vue')
        },
        {
          path: 'comments',
          name: 'AdminComments',
          component: () => import('@/views/admin/comments.vue')
        },
        {
          path: 'coupons',
          name: 'AdminCoupons',
          component: () => import('@/views/admin/coupons.vue')
        },
        {
          path: 'messages',
          name: 'AdminMessages',
          component: () => import('@/views/admin/messages.vue')
        },
        {
          path: 'skus',
          name: 'AdminSkus',
          component: () => import('@/views/admin/skus.vue')
        },
        {
          path: 'yolo-mapping',
          name: 'AdminYoloMapping',
          component: () => import('@/views/admin/yolo-mapping.vue')
        },
        {
          path: 'users',
          name: 'AdminUsers',
          component: () => import('@/views/admin/users.vue')
        },
        {
          path: 'chat',
          name: 'AdminChat',
          component: () => import('@/views/admin/chat.vue')
        },
        {
          path: 'member-days',
          name: 'AdminMemberDays',
          component: () => import('@/views/admin/member-days.vue')
        },
        {
          path: 'points-rules',
          name: 'AdminPointsRules',
          component: () => import('@/views/admin/points-rules.vue')
        },
        {
          path: 'points-products',
          name: 'AdminPointsProducts',
          component: () => import('@/views/admin/points-products.vue')
        },
        {
          path: 'vip-users',
          name: 'AdminVipUsers',
          component: () => import('@/views/admin/vip-users.vue')
        }
      ]
    }
  ]
})

router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    ElMessage.warning('请先登录管理员账号')
    next({ name: 'AdminLogin' })
    return
  }
  
  if (to.meta.requiresAuth) {
    if (!userStore.token) {
      userStore.setIsGuest(true)
      ElMessage.warning('请先登录')
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }
    
    if (userStore.token && !userStore.userInfo) {
      try {
        await userStore.fetchUserInfo()
        next()
      } catch (error) {
        userStore.clearToken()
        userStore.setIsGuest(true)
        ElMessage.warning('请先登录')
        next({ name: 'Login', query: { redirect: to.fullPath } })
        return
      }
    } else {
      next()
    }
    return
  }
  
  if (to.meta.guest && userStore.token) {
    if (userStore.isAdmin) {
      next({ name: 'Admin' })
    } else {
      next({ name: 'Home' })
    }
    return
  }
  
  if (userStore.token && !userStore.userInfo && !to.meta.guest) {
    try {
      await userStore.fetchUserInfo()
    } catch (error) {
      userStore.clearToken()
      userStore.setIsGuest(true)
    }
  }
  
  next()
})

export default router
