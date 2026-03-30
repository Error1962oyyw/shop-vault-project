<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

interface Props {
  hasMore?: boolean
  loading?: boolean
  threshold?: number
}

const props = withDefaults(defineProps<Props>(), {
  hasMore: true,
  loading: false,
  threshold: 100
})

const emit = defineEmits<{
  load: []
}>()

const sentinelRef = ref<HTMLDivElement>()
const observer = ref<IntersectionObserver>()

onMounted(() => {
  if ('IntersectionObserver' in window) {
    observer.value = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting && props.hasMore && !props.loading) {
            emit('load')
          }
        })
      },
      {
        rootMargin: `${props.threshold}px`
      }
    )
    
    if (sentinelRef.value) {
      observer.value.observe(sentinelRef.value)
    }
  }
})

onUnmounted(() => {
  if (observer.value) {
    observer.value.disconnect()
  }
})
</script>

<template>
  <div class="load-more">
    <slot name="loading">
      <div v-if="loading" class="loading-indicator">
        <div class="spinner"></div>
        <span class="loading-text">加载中...</span>
      </div>
    </slot>
    
    <slot name="no-more">
      <div v-if="!hasMore && !loading" class="no-more">
        <span class="no-more-text">没有更多了</span>
      </div>
    </slot>
    
    <div ref="sentinelRef" class="sentinel"></div>
  </div>
</template>

<style scoped>
.load-more {
  width: 100%;
  padding: var(--spacing-6) 0;
}

.loading-indicator {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-3);
}

.spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--color-neutral-200);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-text {
  font-size: var(--font-size-sm);
  color: var(--text-color-secondary);
}

.no-more {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-4) 0;
}

.no-more-text {
  font-size: var(--font-size-sm);
  color: var(--text-color-secondary);
}

.sentinel {
  height: 1px;
  width: 100%;
}
</style>
