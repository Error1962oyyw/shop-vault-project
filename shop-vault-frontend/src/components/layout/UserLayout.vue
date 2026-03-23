<script setup lang="ts">
import UserHeader from './UserHeader.vue'
import UserFooter from './UserFooter.vue'
import MobileNav from './MobileNav.vue'
</script>

<template>
  <div class="user-layout">
    <UserHeader />
    <main class="main-content">
      <slot />
    </main>
    <UserFooter class="footer-desktop" />
    <MobileNav />
  </div>
</template>

<style scoped>
.user-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
}

.footer-desktop {
  display: block;
}

@media (max-width: 768px) {
  .footer-desktop {
    display: none;
  }
  
  .main-content {
    padding-bottom: 70px;
  }
}
</style>
