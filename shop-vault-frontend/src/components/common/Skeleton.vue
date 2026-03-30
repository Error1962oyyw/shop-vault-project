<script setup lang="ts">
interface Props {
  type?: 'text' | 'card' | 'image' | 'avatar' | 'button'
  width?: string
  height?: string
  rows?: number
  animated?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  type: 'text',
  width: '100%',
  height: 'auto',
  rows: 1,
  animated: true
})
</script>

<template>
  <div
    class="skeleton"
    :class="[`skeleton-${type}`, { 'skeleton-animated': animated }]"
    :style="{ width, height }"
  >
    <div v-if="type === 'card'" class="skeleton-card-content">
      <div class="skeleton-card-image"></div>
      <div class="skeleton-card-body">
        <div class="skeleton-line" style="width: 80%"></div>
        <div class="skeleton-line" style="width: 60%"></div>
        <div class="skeleton-line" style="width: 40%"></div>
      </div>
    </div>
    
    <div v-else-if="type === 'text'" class="skeleton-text-content">
      <div
        v-for="i in rows"
        :key="i"
        class="skeleton-line"
        :style="{ width: i === rows ? '60%' : '100%' }"
      ></div>
    </div>
    
    <div v-else-if="type === 'image'" class="skeleton-image-content"></div>
    
    <div v-else-if="type === 'avatar'" class="skeleton-avatar-content"></div>
    
    <div v-else-if="type === 'button'" class="skeleton-button-content"></div>
  </div>
</template>

<style scoped>
.skeleton {
  background: linear-gradient(
    90deg,
    var(--color-neutral-100) 25%,
    var(--color-neutral-200) 50%,
    var(--color-neutral-100) 75%
  );
  background-size: 200% 100%;
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.skeleton-animated {
  animation: skeleton-loading 1.5s ease-in-out infinite;
}

@keyframes skeleton-loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

.skeleton-card-content {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
}

.skeleton-card-image {
  width: 100%;
  height: 200px;
  background: var(--color-neutral-200);
}

.skeleton-card-body {
  padding: var(--spacing-4);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-2);
}

.skeleton-text-content {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-2);
  padding: var(--spacing-2);
}

.skeleton-line {
  height: 16px;
  background: var(--color-neutral-200);
  border-radius: var(--radius-xs);
}

.skeleton-image-content {
  width: 100%;
  height: 100%;
  min-height: 200px;
  background: var(--color-neutral-200);
}

.skeleton-avatar-content {
  width: 100%;
  height: 100%;
  min-width: 40px;
  min-height: 40px;
  border-radius: 50%;
  background: var(--color-neutral-200);
}

.skeleton-button-content {
  width: 100%;
  height: 40px;
  background: var(--color-neutral-200);
  border-radius: var(--radius-md);
}
</style>
