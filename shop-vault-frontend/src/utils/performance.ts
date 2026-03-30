export function debounce<T extends (...args: any[]) => any>(
  func: T,
  wait: number
): (...args: Parameters<T>) => void {
  let timeout: ReturnType<typeof setTimeout> | null = null
  
  return function (this: any, ...args: Parameters<T>) {
    if (timeout) clearTimeout(timeout)
    timeout = setTimeout(() => func.apply(this, args), wait)
  }
}

export function throttle<T extends (...args: any[]) => any>(
  func: T,
  limit: number
): (...args: Parameters<T>) => void {
  let inThrottle: boolean
  
  return function (this: any, ...args: Parameters<T>) {
    if (!inThrottle) {
      func.apply(this, args)
      inThrottle = true
      setTimeout(() => (inThrottle = false), limit)
    }
  }
}

export function loadImage(src: string): Promise<HTMLImageElement> {
  return new Promise((resolve, reject) => {
    const img = new Image()
    img.onload = () => resolve(img)
    img.onerror = reject
    img.src = src
  })
}

export function prefetchImages(urls: string[]): Promise<HTMLImageElement[]> {
  return Promise.all(urls.map(url => loadImage(url)))
}

export function isElementInViewport(el: Element): boolean {
  const rect = el.getBoundingClientRect()
  return (
    rect.top >= 0 &&
    rect.left >= 0 &&
    rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) &&
    rect.right <= (window.innerWidth || document.documentElement.clientWidth)
  )
}

export function scrollToTop(smooth: boolean = true): void {
  if (smooth) {
    window.scrollTo({
      top: 0,
      behavior: 'smooth'
    })
  } else {
    window.scrollTo(0, 0)
  }
}

export function scrollToElement(selector: string, offset: number = 0): void {
  const element = document.querySelector(selector)
  if (element) {
    const top = element.getBoundingClientRect().top + window.pageYOffset - offset
    window.scrollTo({
      top,
      behavior: 'smooth'
    })
  }
}

export function getScrollPercentage(): number {
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop
  const scrollHeight = document.documentElement.scrollHeight - document.documentElement.clientHeight
  return (scrollTop / scrollHeight) * 100
}

export function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

export function measurePerformance(name: string, fn: () => void): void {
  const start = performance.now()
  fn()
  const end = performance.now()
  console.log(`${name} took ${end - start} milliseconds`)
}

export async function measureAsyncPerformance(name: string, fn: () => Promise<void>): Promise<void> {
  const start = performance.now()
  await fn()
  const end = performance.now()
  console.log(`${name} took ${end - start} milliseconds`)
}

export function preloadRoute(route: () => Promise<any>): void {
  route()
}

export function isMobile(): boolean {
  return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)
}

export function isTablet(): boolean {
  return /(tablet|ipad|playbook|silk)|(android(?!.*mobi))/i.test(navigator.userAgent)
}

export function getDeviceType(): 'mobile' | 'tablet' | 'desktop' {
  if (isMobile()) return 'mobile'
  if (isTablet()) return 'tablet'
  return 'desktop'
}

export function supportsWebP(): boolean {
  return document.createElement('canvas').toDataURL('image/webp').indexOf('data:image/webp') === 0
}

export function supportsIntersectionObserver(): boolean {
  return 'IntersectionObserver' in window && 'IntersectionObserverEntry' in window
}

export function supportsResizeObserver(): boolean {
  return 'ResizeObserver' in window
}
