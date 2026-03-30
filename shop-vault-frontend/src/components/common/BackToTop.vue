<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

interface Props {
  visibilityHeight?: number
  right?: number
  bottom?: number
}

const props = withDefaults(defineProps<Props>(), {
  visibilityHeight: 400,
  right: 40,
  bottom: 40
})

const visible = ref(false)
const isScrolling = ref(false)

const handleScroll = () => {
  visible.value = window.pageYOffset > props.visibilityHeight
}

const scrollToTop = () => {
  if (isScrolling.value) return
  
  isScrolling.value = true
  const start = window.pageYOffset
  const startTime = performance.now()
  const duration = 500
  
  const easeInOutQuad = (t: number, b: number, c: number, d: number) => {
    t /= d / 2
    if (t < 1) return c / 2 * t * t + b
    t--
    return -c / 2 * (t * (t - 2) - 1) + b
  }
  
  const animateScroll = (currentTime: number) => {
    const elapsed = currentTime - startTime
    const progress = Math.min(elapsed / duration, 1)
    
    window.scrollTo(0, easeInOutQuad(progress, start, -start, 1))
    
    if (progress < 1) {
      requestAnimationFrame(animateScroll)
    } else {
      isScrolling.value = false
    }
  }
  
  requestAnimationFrame(animateScroll)
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  handleScroll()
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<template>
  <Transition name="fade">
    <button
      v-show="visible"
      class="back-to-top"
      :style="{ right: `${right}px`, bottom: `${bottom}px` }"
      @click="scrollToTop"
    >
      <svg viewBox="0 0 1024 1024" class="icon">
        <path fill="currentColor" d="M512 320l-320 320h640z"/>
      </svg>
    </button>
  </Transition>
</template>

<style scoped>
.back-to-top {
  position: fixed;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: var(--color-primary);
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-lg);
  transition: all var(--transition-base);
  z-index: var(--z-index-fixed);
}

.back-to-top:hover {
  background: var(--color-primary-dark);
  transform: translateY(-4px);
  box-shadow: var(--shadow-xl);
}

.back-to-top:active {
  transform: scale(0.95);
}

.icon {
  width: 24px;
  height: 24px;
}

.fade-enter-active,
.fade-leave-active {
  transition: all var(--transition-normal);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(20px);
}
</style>
