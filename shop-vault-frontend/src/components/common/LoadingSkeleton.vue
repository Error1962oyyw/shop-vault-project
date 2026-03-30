<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  loading?: boolean
  type?: 'card' | 'list' | 'table' | 'detail'
  count?: number
}

const props = withDefaults(defineProps<Props>(), {
  loading: true,
  type: 'card',
  count: 3
})

const skeletonCount = computed(() => Array.from({ length: props.count }, (_, i) => i))
</script>

<template>
  <div v-if="loading" class="loading-skeleton" :class="`skeleton-${type}`">
    <div v-for="i in skeletonCount" :key="i" class="skeleton-item">
      <div v-if="type === 'card'" class="skeleton-card">
        <div class="skeleton-image"></div>
        <div class="skeleton-content">
          <div class="skeleton-title"></div>
          <div class="skeleton-text"></div>
          <div class="skeleton-text" style="width: 60%"></div>
        </div>
      </div>
      
      <div v-else-if="type === 'list'" class="skeleton-list-item">
        <div class="skeleton-avatar"></div>
        <div class="skeleton-content">
          <div class="skeleton-title"></div>
          <div class="skeleton-text"></div>
        </div>
      </div>
      
      <div v-else-if="type === 'table'" class="skeleton-table-row">
        <div class="skeleton-cell"></div>
        <div class="skeleton-cell"></div>
        <div class="skeleton-cell"></div>
        <div class="skeleton-cell"></div>
      </div>
      
      <div v-else-if="type === 'detail'" class="skeleton-detail">
        <div class="skeleton-header">
          <div class="skeleton-image-large"></div>
          <div class="skeleton-info">
            <div class="skeleton-title-large"></div>
            <div class="skeleton-text"></div>
            <div class="skeleton-text"></div>
            <div class="skeleton-text" style="width: 40%"></div>
          </div>
        </div>
        <div class="skeleton-body">
          <div class="skeleton-text"></div>
          <div class="skeleton-text"></div>
          <div class="skeleton-text" style="width: 80%"></div>
        </div>
      </div>
    </div>
  </div>
  <slot v-else></slot>
</template>

<style scoped>
.loading-skeleton {
  width: 100%;
}

.skeleton-card,
.skeleton-list-item,
.skeleton-table-row,
.skeleton-detail {
  background: var(--background-color-card);
  border-radius: var(--radius-md);
  padding: var(--spacing-4);
  margin-bottom: var(--spacing-4);
}

.skeleton-card {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-3);
}

.skeleton-image,
.skeleton-image-large {
  background: linear-gradient(
    90deg,
    var(--color-neutral-100) 25%,
    var(--color-neutral-200) 50%,
    var(--color-neutral-100) 75%
  );
  background-size: 200% 100%;
  animation: skeleton-shimmer 1.5s ease-in-out infinite;
  border-radius: var(--radius-sm);
}

.skeleton-image {
  width: 100%;
  height: 200px;
}

.skeleton-image-large {
  width: 300px;
  height: 300px;
}

.skeleton-content {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-2);
}

.skeleton-title,
.skeleton-title-large,
.skeleton-text,
.skeleton-cell,
.skeleton-avatar {
  background: linear-gradient(
    90deg,
    var(--color-neutral-100) 25%,
    var(--color-neutral-200) 50%,
    var(--color-neutral-100) 75%
  );
  background-size: 200% 100%;
  animation: skeleton-shimmer 1.5s ease-in-out infinite;
  border-radius: var(--radius-xs);
}

.skeleton-title {
  width: 80%;
  height: 20px;
}

.skeleton-title-large {
  width: 100%;
  height: 32px;
}

.skeleton-text {
  width: 100%;
  height: 16px;
}

.skeleton-list-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-3);
}

.skeleton-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  flex-shrink: 0;
}

.skeleton-table-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-3);
}

.skeleton-cell {
  height: 20px;
}

.skeleton-detail {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-6);
}

.skeleton-header {
  display: flex;
  gap: var(--spacing-6);
}

.skeleton-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-3);
}

.skeleton-body {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-2);
}

@keyframes skeleton-shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

@media (max-width: 768px) {
  .skeleton-header {
    flex-direction: column;
    align-items: center;
  }
  
  .skeleton-image-large {
    width: 100%;
    max-width: 300px;
  }
  
  .skeleton-table-row {
    grid-template-columns: 1fr;
  }
}
</style>
