<script setup lang="ts">
import UserHeader from './UserHeader.vue'
import UserFooter from './UserFooter.vue'
import MobileNav from './MobileNav.vue'
</script>

<template>
  <div class="min-h-screen flex flex-col">
    <UserHeader />
    <main class="flex-1 safe-area-bottom">
      <slot />
    </main>
    <UserFooter class="hide-mobile" />
    <MobileNav />
  </div>
</template>
