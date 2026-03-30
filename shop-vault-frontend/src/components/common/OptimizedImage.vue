<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'

interface Props {
  src: string
  alt?: string
  width?: string | number
  height?: string | number
  lazy?: boolean
  placeholder?: string
  fallback?: string
  objectFit?: 'cover' | 'contain' | 'fill' | 'none' | 'scale-down'
  borderRadius?: string
  aspectRatio?: string
}

const props = withDefaults(defineProps<Props>(), {
  alt: '',
  lazy: true,
  objectFit: 'cover',
  borderRadius: '0'
})

const emit = defineEmits<{
  load: [event: Event]
  error: [event: Event]
}>()

const imageRef = ref<HTMLImageElement>()
const isLoading = ref(true)
const hasError = ref(false)
const imageSrc = ref(props.placeholder || '')

const imageStyle = computed(() => ({
  width: typeof props.width === 'number' ? `${props.width}px` : props.width,
  height: typeof props.height === 'number' ? `${props.height}px` : props.height,
  objectFit: props.objectFit,
  borderRadius: props.borderRadius,
  aspectRatio: props.aspectRatio
}))

const handleLoad = (event: Event) => {
  isLoading.value = false
  hasError.value = false
  emit('load', event)
}

const handleError = (event: Event) => {
  isLoading.value = false
  hasError.value = true
  if (props.fallback) {
    imageSrc.value = props.fallback
  }
  emit('error', event)
}

onMounted(() => {
  if (props.lazy && 'IntersectionObserver' in window) {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            imageSrc.value = props.src
            observer.unobserve(entry.target)
          }
        })
      },
      { rootMargin: '50px' }
    )
    
    if (imageRef.value) {
      observer.observe(imageRef.value)
    }
  } else {
    imageSrc.value = props.src
  }
})
</script>

<template>
  <div class="optimized-image" :style="{ borderRadius }">
    <img
      ref="imageRef"
      :src="imageSrc"
      :alt="alt"
      :style="imageStyle"
      :class="['image', { 'image-loading': isLoading, 'image-error': hasError }]"
      @load="handleLoad"
      @error="handleError"
    />
    
    <div v-if="isLoading" class="image-placeholder">
      <div class="placeholder-shimmer"></div>
    </div>
    
    <div v-if="hasError && !fallback" class="image-error-placeholder">
      <svg viewBox="0 0 1024 1024" class="error-icon">
        <path fill="currentColor" d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64zm0 820c-205.4 0-372-166.6-372-372s166.6-372 372-372 372 166.6 372 372-166.6 372-372 372z"/>
        <path fill="currentColor" d="M464 336a48 48 0 1 0 96 0 48 48 0 1 0-96 0zm72 112h-48c-4.4 0-8 3.6-8 8v272c0 4.4 3.6 8 8 8h48c4.4 0 8-3.6 8-8V456c0-4.4-3.6-8-8-8z"/>
      </svg>
      <span class="error-text">图片加载失败</span>
    </div>
  </div>
</template>

<style scoped>
.optimized-image {
  position: relative;
  overflow: hidden;
  display: inline-block;
}

.image {
  display: block;
  max-width: 100%;
  height: auto;
  transition: opacity var(--transition-normal);
}

.image-loading {
  opacity: 0;
}

.image-error {
  opacity: 0;
}

.image-placeholder {
  position: absolute;
  inset: 0;
  background: var(--color-neutral-100);
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder-shimmer {
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    var(--color-neutral-100) 25%,
    var(--color-neutral-200) 50%,
    var(--color-neutral-100) 75%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s ease-in-out infinite;
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

.image-error-placeholder {
  position: absolute;
  inset: 0;
  background: var(--color-neutral-100);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-2);
}

.error-icon {
  width: 48px;
  height: 48px;
  color: var(--color-neutral-400);
}

.error-text {
  font-size: var(--font-size-sm);
  color: var(--color-neutral-400);
}
</style>
