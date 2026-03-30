# 前端优化组件使用指南

本文档提供了新创建的优化组件和工具的使用示例。

## 📦 组件使用

### 1. 骨架屏组件

#### Skeleton 基础骨架

```vue
<template>
  <div class="loading-container">
    <!-- 文本骨架 -->
    <Skeleton type="text" :rows="3" />
    
    <!-- 卡片骨架 -->
    <Skeleton type="card" width="300px" height="400px" />
    
    <!-- 图片骨架 -->
    <Skeleton type="image" width="100%" height="200px" />
    
    <!-- 头像骨架 -->
    <Skeleton type="avatar" width="48px" height="48px" />
    
    <!-- 按钮骨架 -->
    <Skeleton type="button" width="120px" height="40px" />
  </div>
</template>

<script setup lang="ts">
import { Skeleton } from '@/components/common'
</script>
```

#### LoadingSkeleton 高级骨架

```vue
<template>
  <!-- 卡片列表骨架 -->
  <LoadingSkeleton :loading="isLoading" type="card" :count="6">
    <div class="product-grid">
      <ProductCard v-for="product in products" :key="product.id" :product="product" />
    </div>
  </LoadingSkeleton>
  
  <!-- 列表骨架 -->
  <LoadingSkeleton :loading="isLoading" type="list" :count="5">
    <div class="list">
      <ListItem v-for="item in items" :key="item.id" :item="item" />
    </div>
  </LoadingSkeleton>
  
  <!-- 表格骨架 -->
  <LoadingSkeleton :loading="isLoading" type="table" :count="10">
    <table>
      <!-- 表格内容 -->
    </table>
  </LoadingSkeleton>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { LoadingSkeleton } from '@/components/common'

const isLoading = ref(true)
</script>
```

---

### 2. 优化图片组件

```vue
<template>
  <div class="image-gallery">
    <!-- 基础用法 -->
    <OptimizedImage
      src="/product.jpg"
      alt="商品图片"
      :lazy="true"
    />
    
    <!-- 带占位符和回退 -->
    <OptimizedImage
      src="/product.jpg"
      alt="商品图片"
      placeholder="/placeholder.jpg"
      fallback="/error.jpg"
      :lazy="true"
      object-fit="cover"
      border-radius="8px"
    />
    
    <!-- 固定尺寸 -->
    <OptimizedImage
      src="/product.jpg"
      alt="商品图片"
      :width="300"
      :height="300"
      object-fit="contain"
    />
    
    <!-- 响应式尺寸 -->
    <OptimizedImage
      src="/product.jpg"
      alt="商品图片"
      width="100%"
      height="auto"
      aspect-ratio="16/9"
    />
  </div>
</template>

<script setup lang="ts">
import { OptimizedImage } from '@/components/common'
</script>
```

---

### 3. 回到顶部按钮

```vue
<template>
  <div class="page">
    <!-- 页面内容 -->
    <div class="content">
      <!-- 长内容 -->
    </div>
    
    <!-- 回到顶部按钮 -->
    <BackToTop
      :visibility-height="300"
      :right="40"
      :bottom="40"
    />
  </div>
</template>

<script setup lang="ts">
import { BackToTop } from '@/components/common'
</script>
```

---

### 4. 加载更多组件

```vue
<template>
  <div class="product-list">
    <div class="grid">
      <ProductCard v-for="product in products" :key="product.id" :product="product" />
    </div>
    
    <LoadMore
      :has-more="hasMore"
      :loading="loading"
      :threshold="100"
      @load="loadMore"
    >
      <template #loading>
        <div class="custom-loading">
          <div class="spinner"></div>
          <span>加载更多商品...</span>
        </div>
      </template>
      
      <template #no-more>
        <div class="no-more">
          已加载全部商品
        </div>
      </template>
    </LoadMore>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { LoadMore } from '@/components/common'

const products = ref([])
const hasMore = ref(true)
const loading = ref(false)

const loadMore = async () => {
  if (loading.value || !hasMore.value) return
  
  loading.value = true
  try {
    const newProducts = await fetchProducts()
    products.value.push(...newProducts)
    hasMore.value = newProducts.length > 0
  } finally {
    loading.value = false
  }
}
</script>
```

---

### 5. 空状态组件

```vue
<template>
  <div class="page">
    <!-- 无数据 -->
    <EmptyState
      v-if="items.length === 0"
      type="empty"
      title="暂无商品"
      description="还没有添加任何商品"
    >
      <template #action>
        <button @click="addProduct">添加商品</button>
      </template>
    </EmptyState>
    
    <!-- 搜索无结果 -->
    <EmptyState
      v-if="searchResults.length === 0 && hasSearched"
      type="no-result"
      title="未找到相关商品"
      description="尝试其他搜索关键词"
    >
      <template #action>
        <button @click="clearSearch">清除搜索</button>
      </template>
    </EmptyState>
    
    <!-- 网络错误 -->
    <EmptyState
      v-if="networkError"
      type="network-error"
      @action="retry"
    />
    
    <!-- 一般错误 -->
    <EmptyState
      v-if="hasError"
      type="error"
      title="加载失败"
      description="商品数据加载失败，请稍后重试"
      @action="reload"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { EmptyState } from '@/components/common'

const items = ref([])
const searchResults = ref([])
const hasSearched = ref(false)
const networkError = ref(false)
const hasError = ref(false)

const addProduct = () => { /* 添加商品 */ }
const clearSearch = () => { /* 清除搜索 */ }
const retry = () => { /* 重试 */ }
const reload = () => { /* 重新加载 */ }
</script>
```

---

## 🛠️ 工具函数使用

### 1. 性能优化工具

```typescript
import { 
  debounce, 
  throttle, 
  loadImage, 
  prefetchImages,
  measurePerformance 
} from '@/utils/performance'

// 防抖 - 搜索输入
const handleSearch = debounce((query: string) => {
  searchProducts(query)
}, 300)

// 节流 - 滚动事件
const handleScroll = throttle(() => {
  updateScrollPosition()
}, 100)

// 图片预加载
const preloadProductImages = async (products: Product[]) => {
  const urls = products.map(p => p.image)
  await prefetchImages(urls)
}

// 性能测量
measurePerformance('商品列表渲染', () => {
  renderProductList()
})
```

---

### 2. 响应式工具

```vue
<template>
  <div class="page">
    <!-- 根据设备类型显示不同内容 -->
    <div v-if="isMobile" class="mobile-view">
      <!-- 移动端视图 -->
    </div>
    <div v-else-if="isTablet" class="tablet-view">
      <!-- 平板视图 -->
    </div>
    <div v-else class="desktop-view">
      <!-- 桌面视图 -->
    </div>
    
    <!-- 响应式网格 -->
    <div class="grid" :style="{ gridTemplateColumns: `repeat(${gridColumns}, 1fr)` }">
      <ProductCard v-for="product in products" :key="product.id" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { useBreakpoint, useResponsiveGrid } from '@/composables'

const { isMobile, isTablet, isDesktop, currentBreakpoint } = useBreakpoint()
const { gridColumns } = useResponsiveGrid()
</script>
```

---

### 3. 懒加载指令

```vue
<template>
  <div class="image-list">
    <!-- 使用 v-lazy 指令 -->
    <img
      v-for="image in images"
      :key="image.id"
      v-lazy="image.url"
      :alt="image.title"
    />
    
    <!-- 或者使用 data-src -->
    <img
      v-for="image in images"
      :key="image.id"
      data-src="image.url"
      v-lazy
      :alt="image.title"
    />
  </div>
</template>
```

---

## 🎨 完整示例

### 商品列表页面优化示例

```vue
<template>
  <UserLayout>
    <div class="products-page animate-fade-in">
      <PageHeader
        title="全部商品"
        subtitle="发现您心仪的好物"
        icon="🛍️"
        gradient="primary"
      />
      
      <!-- 加载状态 -->
      <LoadingSkeleton v-if="loading" type="card" :count="12" />
      
      <!-- 空状态 -->
      <EmptyState
        v-else-if="products.length === 0"
        type="empty"
        title="暂无商品"
        description="还没有添加任何商品"
      >
        <template #action>
          <Button type="primary" @click="loadProducts">刷新</Button>
        </template>
      </EmptyState>
      
      <!-- 商品列表 -->
      <div v-else class="products-grid">
        <Card
          v-for="product in products"
          :key="product.id"
          :title="product.name"
          :hoverable="true"
          :clickable="true"
          @click="viewProduct(product.id)"
        >
          <OptimizedImage
            :src="product.image"
            :alt="product.name"
            :lazy="true"
            object-fit="cover"
            border-radius="8px"
          />
          <div class="price">¥{{ product.price }}</div>
        </Card>
        
        <!-- 加载更多 -->
        <LoadMore
          :has-more="hasMore"
          :loading="loadingMore"
          @load="loadMore"
        />
      </div>
      
      <!-- 回到顶部 -->
      <BackToTop />
    </div>
  </UserLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { UserLayout } from '@/components/layout'
import { PageHeader, Card, Button } from '@/components/ui'
import { 
  LoadingSkeleton, 
  OptimizedImage, 
  EmptyState, 
  LoadMore, 
  BackToTop 
} from '@/components/common'
import { debounce } from '@/utils/performance'

const loading = ref(true)
const loadingMore = ref(false)
const products = ref([])
const hasMore = ref(true)
const page = ref(1)

const loadProducts = async () => {
  loading.value = true
  try {
    products.value = await fetchProducts({ page: 1 })
    page.value = 1
    hasMore.value = true
  } finally {
    loading.value = false
  }
}

const loadMore = debounce(async () => {
  if (loadingMore.value || !hasMore.value) return
  
  loadingMore.value = true
  try {
    const newProducts = await fetchProducts({ page: page.value + 1 })
    products.value.push(...newProducts)
    page.value++
    hasMore.value = newProducts.length > 0
  } finally {
    loadingMore.value = false
  }
}, 300)

const viewProduct = (id: string) => {
  router.push(`/product/${id}`)
}

onMounted(loadProducts)
</script>

<style scoped>
.products-page {
  min-height: 100vh;
  background: var(--background-color-page);
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--spacing-6);
  padding: var(--spacing-6);
}

.price {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-danger);
  margin-top: var(--spacing-3);
}
</style>
```

---

## 📚 相关文档

- [设计规范文档](./DESIGN_SYSTEM.md)
- [优化报告](./OPTIMIZATION_REPORT.md)
- [UI 组件库文档](./DESIGN_SYSTEM.md#组件库)

---

## 💡 最佳实践

1. **性能优化**
   - 使用骨架屏代替简单的 loading 提示
   - 图片使用懒加载
   - 搜索输入使用防抖
   - 滚动事件使用节流

2. **用户体验**
   - 提供清晰的加载状态
   - 友好的空状态提示
   - 平滑的动画过渡
   - 便捷的交互功能

3. **响应式设计**
   - 使用响应式工具检测设备
   - 根据屏幕尺寸调整布局
   - 优化移动端体验

4. **代码质量**
   - 使用统一的 UI 组件
   - 复用工具函数
   - 遵循设计规范
   - 保持代码整洁

---

如有疑问，请参考相关文档或联系开发团队。
