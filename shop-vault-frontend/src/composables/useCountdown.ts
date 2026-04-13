import { ref, computed, onUnmounted } from 'vue'

export function useCountdown(seconds: number = 60) {
  const countdown = ref(0)
  let timer: ReturnType<typeof setInterval> | null = null

  const isActive = computed(() => countdown.value > 0)

  const start = () => {
    countdown.value = seconds
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        if (timer) {
          clearInterval(timer)
          timer = null
        }
      }
    }, 1000)
  }

  onUnmounted(() => {
    if (timer) {
      clearInterval(timer)
      timer = null
    }
  })

  return {
    countdown,
    isActive,
    start
  }
}
