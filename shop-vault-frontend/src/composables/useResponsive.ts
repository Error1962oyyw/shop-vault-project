import { ref, onMounted, onUnmounted } from 'vue'

export type Breakpoint = 'xs' | 'sm' | 'md' | 'lg' | 'xl' | '2xl'

export interface BreakpointConfig {
  xs: number
  sm: number
  md: number
  lg: number
  xl: number
  '2xl': number
}

const breakpoints: BreakpointConfig = {
  xs: 480,
  sm: 640,
  md: 768,
  lg: 1024,
  xl: 1280,
  '2xl': 1536
}

export function useBreakpoint() {
  const currentBreakpoint = ref<Breakpoint>('md')
  const isMobile = ref(false)
  const isTablet = ref(false)
  const isDesktop = ref(true)

  const updateBreakpoint = () => {
    const width = window.innerWidth
    
    if (width < breakpoints.sm) {
      currentBreakpoint.value = 'xs'
      isMobile.value = true
      isTablet.value = false
      isDesktop.value = false
    } else if (width < breakpoints.md) {
      currentBreakpoint.value = 'sm'
      isMobile.value = true
      isTablet.value = false
      isDesktop.value = false
    } else if (width < breakpoints.lg) {
      currentBreakpoint.value = 'md'
      isMobile.value = false
      isTablet.value = true
      isDesktop.value = false
    } else if (width < breakpoints.xl) {
      currentBreakpoint.value = 'lg'
      isMobile.value = false
      isTablet.value = false
      isDesktop.value = true
    } else if (width < breakpoints['2xl']) {
      currentBreakpoint.value = 'xl'
      isMobile.value = false
      isTablet.value = false
      isDesktop.value = true
    } else {
      currentBreakpoint.value = '2xl'
      isMobile.value = false
      isTablet.value = false
      isDesktop.value = true
    }
  }

  onMounted(() => {
    updateBreakpoint()
    window.addEventListener('resize', updateBreakpoint)
  })

  onUnmounted(() => {
    window.removeEventListener('resize', updateBreakpoint)
  })

  return {
    currentBreakpoint,
    isMobile,
    isTablet,
    isDesktop,
    breakpoints
  }
}

export function useMediaQuery(query: string) {
  const matches = ref(false)
  
  const updateMatches = () => {
    matches.value = window.matchMedia(query).matches
  }

  onMounted(() => {
    updateMatches()
    window.matchMedia(query).addEventListener('change', updateMatches)
  })

  onUnmounted(() => {
    window.matchMedia(query).removeEventListener('change', updateMatches)
  })

  return matches
}

export function useResponsiveGrid() {
  const { currentBreakpoint } = useBreakpoint()
  
  const gridColumns = ref(4)
  
  const updateGridColumns = () => {
    switch (currentBreakpoint.value) {
      case 'xs':
      case 'sm':
        gridColumns.value = 1
        break
      case 'md':
        gridColumns.value = 2
        break
      case 'lg':
        gridColumns.value = 3
        break
      case 'xl':
      case '2xl':
        gridColumns.value = 4
        break
    }
  }

  onMounted(updateGridColumns)
  
  return {
    gridColumns,
    updateGridColumns
  }
}

export function useResponsiveSpacing() {
  const { currentBreakpoint } = useBreakpoint()
  
  const getSpacing = (base: number) => {
    switch (currentBreakpoint.value) {
      case 'xs':
      case 'sm':
        return base * 0.5
      case 'md':
        return base * 0.75
      default:
        return base
    }
  }
  
  return {
    getSpacing
  }
}
