import type { Directive, DirectiveBinding } from 'vue'

interface LazyLoadElement extends HTMLElement {
  _lazyLoadObserver?: IntersectionObserver
}

const lazyLoadDirective: Directive<LazyLoadElement> = {
  mounted(el: LazyLoadElement, binding: DirectiveBinding) {
    const options = {
      root: null,
      rootMargin: '50px',
      threshold: 0.1
    }

    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          const img = entry.target as HTMLImageElement
          const src = binding.value || img.dataset.src
          
          if (src) {
            img.src = src
            img.classList.add('lazy-loaded')
            img.classList.remove('lazy-loading')
          }
          
          observer.unobserve(entry.target)
        }
      })
    }, options)

    el._lazyLoadObserver = observer
    el.classList.add('lazy-loading')
    observer.observe(el)
  },
  
  unmounted(el: LazyLoadElement) {
    if (el._lazyLoadObserver) {
      el._lazyLoadObserver.disconnect()
    }
  }
}

export default lazyLoadDirective
