import { ref, watch, type Ref } from 'vue'

export function useDebounce<T extends (...args: any[]) => any>(
  fn: T,
  delay: number = 300
): T & { cancel: () => void } {
  let timer: ReturnType<typeof setTimeout> | null = null

  const debouncedFn = ((...args: Parameters<T>) => {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn(...args)
      timer = null
    }, delay)
  }) as T & { cancel: () => void }

  debouncedFn.cancel = () => {
    if (timer) {
      clearTimeout(timer)
      timer = null
    }
  }

  return debouncedFn
}

export function useDebouncedRef<T>(value: T, delay: number = 300): Ref<T> {
  const debouncedValue = ref(value) as Ref<T>
  let timer: ReturnType<typeof setTimeout> | null = null

  watch(
    () => value,
    (newVal) => {
      if (timer) clearTimeout(timer)
      timer = setTimeout(() => {
        debouncedValue.value = newVal
        timer = null
      }, delay)
    }
  )

  return debouncedValue
}
