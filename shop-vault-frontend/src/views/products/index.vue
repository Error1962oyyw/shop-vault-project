<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Filter, Sort, ArrowRight, Search, Refresh } from '@element-plus/icons-vue'
import UserLayout from '@/components/layout/UserLayout.vue'
import { ProductCard, Pagination } from '@/components/common'
import { getProductList, getCategoryList } from '@/api/product'
import type { Product, Category, PageResult } from '@/types/api'

const route = useRoute()

const loading = ref(false)
const categories = ref<Category[]>([])
const products = ref<Product[]>([])
const pagination = reactive({
  current: 1,
  size: 12,
  total: 0
})

const filters = reactive({
  categoryId: undefined as number | undefined,
  keyword: '',
  minPrice: undefined as number | undefined,
  maxPrice: undefined as number | undefined,
  sortBy: 'default',
  sortOrder: 'desc' as 'asc' | 'desc'
})

const sortOptions = [
  { label: '综合排序', value: 'default', order: 'desc' },
  { label: '销量优先', value: 'sales', order: 'desc' },
  { label: '价格从低到高', value: 'price', order: 'asc' },
  { label: '价格从高到低', value: 'price', order: 'desc' },
  { label: '最新上架', value: 'createTime', order: 'desc' }
]

const currentSort = computed({
  get: () => `${filters.sortBy}-${filters.sortOrder}`,
  set: (val: string) => {
    const [sortBy, sortOrder] = val.split('-')
    filters.sortBy = sortBy
    filters.sortOrder = sortOrder as 'asc' | 'desc'
  }
})

const activeCategoryName = computed(() => {
  if (!filters.categoryId) return '全部分类'
  const findCategory = (cats: Category[]): Category | undefined => {
    for (const cat of cats) {
      if (cat.id === filters.categoryId) return cat
      if (cat.children?.length) {
        const found = findCategory(cat.children)
        if (found) return found
      }
    }
  }
  const found = findCategory(categories.value)
  return found?.name || '全部分类'
})

const fetchCategories = async () => {
  try {
    categories.value = await getCategoryList()
  } catch (error) {
    console.error('获取分类失败', error)
  }
}

const fetchProducts = async () => {
  loading.value = true
  try {
    const res: PageResult<Product> = await getProductList({
      categoryId: filters.categoryId,
      keyword: filters.keyword,
      minPrice: filters.minPrice,
      maxPrice: filters.maxPrice,
      sortBy: filters.sortBy,
      sortOrder: filters.sortOrder,
      pageNum: pagination.current,
      pageSize: pagination.size
    })
    products.value = res.records
    pagination.total = res.total
  } catch (error) {
    console.error('获取商品列表失败', error)
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page: number) => {
  pagination.current = page
  fetchProducts()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  fetchProducts()
}

const handleSortChange = () => {
  pagination.current = 1
  fetchProducts()
}

const handleCategorySelect = (categoryId: number | undefined) => {
  filters.categoryId = categoryId
  pagination.current = 1
  fetchProducts()
}

const handleSearch = () => {
  pagination.current = 1
  fetchProducts()
}

const resetFilters = () => {
  filters.categoryId = undefined
  filters.keyword = ''
  filters.minPrice = undefined
  filters.maxPrice = undefined
  filters.sortBy = 'default'
  filters.sortOrder = 'desc'
  pagination.current = 1
  fetchProducts()
}

watch(() => route.query, (query) => {
  if (query.categoryId) {
    filters.categoryId = Number(query.categoryId)
  }
  if (query.keyword) {
    filters.keyword = String(query.keyword)
  }
  fetchProducts()
}, { immediate: true })

onMounted(() => {
  fetchCategories()
})
</script>

<template>
  <UserLayout>
    <div class="products-page">
      <div class="page-header">
        <div class="header-content">
          <div class="header-left">
            <h1 class="page-title">
              <span class="title-icon">🛍️</span>
              全部商品
            </h1>
            <p class="page-subtitle">发现您心仪的好物</p>
          </div>
          <div class="header-right">
            <div class="active-filter" v-if="filters.categoryId || filters.keyword">
              <el-tag type="primary" size="large" closable @close="filters.categoryId = undefined; fetchProducts()">
                {{ activeCategoryName }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>

      <div class="page-container">
        <div class="main-layout">
          <aside class="filter-sidebar">
            <div class="filter-card">
              <div class="filter-header">
                <el-icon class="filter-icon"><Filter /></el-icon>
                <span>筛选条件</span>
              </div>

              <div class="filter-section">
                <h4 class="filter-title">商品分类</h4>
                <div class="category-list">
                  <div 
                    class="category-item"
                    :class="{ active: !filters.categoryId }"
                    @click="handleCategorySelect(undefined)"
                  >
                    <span class="category-dot"></span>
                    <span class="category-name">全部分类</span>
                    <span class="category-count" v-if="!filters.categoryId">✓</span>
                  </div>
                  <template v-for="category in categories" :key="category.id">
                    <div 
                      class="category-item"
                      :class="{ active: filters.categoryId === category.id }"
                      @click="handleCategorySelect(category.id)"
                    >
                      <span class="category-dot"></span>
                      <span class="category-name">{{ category.name }}</span>
                      <span class="category-count" v-if="filters.categoryId === category.id">✓</span>
                    </div>
                    <template v-if="category.children?.length">
                      <div 
                        v-for="child in category.children" 
                        :key="child.id"
                        class="category-item category-child"
                        :class="{ active: filters.categoryId === child.id }"
                        @click="handleCategorySelect(child.id)"
                      >
                        <span class="category-dot"></span>
                        <span class="category-name">{{ child.name }}</span>
                        <span class="category-count" v-if="filters.categoryId === child.id">✓</span>
                      </div>
                    </template>
                  </template>
                </div>
              </div>

              <div class="filter-section">
                <h4 class="filter-title">价格区间</h4>
                <div class="price-inputs">
                  <el-input 
                    v-model.number="filters.minPrice" 
                    placeholder="最低价" 
                    size="large"
                    type="number"
                    class="price-input"
                  />
                  <span class="price-separator">—</span>
                  <el-input 
                    v-model.number="filters.maxPrice" 
                    placeholder="最高价" 
                    size="large"
                    type="number"
                    class="price-input"
                  />
                </div>
                <el-button 
                  type="primary" 
                  size="large"
                  class="filter-btn"
                  @click="handleSearch"
                >
                  <el-icon><Search /></el-icon>
                  <span>确定筛选</span>
                </el-button>
              </div>

              <el-button 
                type="default" 
                size="large"
                class="reset-btn"
                @click="resetFilters"
              >
                <el-icon><Refresh /></el-icon>
                <span>重置筛选</span>
              </el-button>
            </div>
          </aside>

          <main class="products-main">
            <div class="toolbar-card">
              <div class="toolbar-content">
                <div class="sort-section">
                  <span class="sort-label">
                    <el-icon><Sort /></el-icon>
                    排序方式
                  </span>
                  <div class="sort-options">
                    <el-radio-group v-model="currentSort" size="large" @change="handleSortChange">
                      <el-radio-button 
                        v-for="option in sortOptions" 
                        :key="`${option.value}-${option.order}`"
                        :value="`${option.value}-${option.order}`"
                      >
                        {{ option.label }}
                      </el-radio-button>
                    </el-radio-group>
                  </div>
                </div>
                <div class="result-info">
                  <span class="result-text">共</span>
                  <span class="result-count">{{ pagination.total }}</span>
                  <span class="result-text">件商品</span>
                </div>
              </div>
            </div>

            <div v-loading="loading" class="products-container">
              <template v-if="products.length > 0">
                <div class="products-grid">
                  <ProductCard 
                    v-for="product in products" 
                    :key="product.id" 
                    :product="product"
                  />
                </div>
                <div class="pagination-wrapper">
                  <Pagination 
                    :total="pagination.total"
                    :current-page="pagination.current"
                    :page-size="pagination.size"
                    @change="handlePageChange"
                    @update:page-size="handleSizeChange"
                  />
                </div>
              </template>
              <template v-else-if="!loading">
                <div class="empty-state">
                  <div class="empty-icon">📦</div>
                  <h3 class="empty-title">暂无商品</h3>
                  <p class="empty-desc">试试调整筛选条件或浏览其他分类</p>
                  <el-button type="primary" size="large" @click="resetFilters" class="empty-btn">
                    <span>查看全部商品</span>
                    <el-icon><ArrowRight /></el-icon>
                  </el-button>
                </div>
              </template>
            </div>
          </main>
        </div>
      </div>
    </div>
  </UserLayout>
</template>

<style scoped>
.products-page {
  background: linear-gradient(135deg, #f7f8fa 0%, #e6f4ff 50%, #f0f5ff 100%);
  min-height: 100vh;
}

.page-header {
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
  padding: 40px 0 32px;
  margin-bottom: 0;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  color: #ffffff;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 12px;
  color: #ffffff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.title-icon {
  font-size: 36px;
}

.page-subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.95);
  margin: 0;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.active-filter :deep(.el-tag) {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
  color: #fff;
  padding: 8px 16px;
  font-size: 14px;
}

.active-filter :deep(.el-tag .el-tag__close) {
  color: #fff;
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.main-layout {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.filter-sidebar {
  width: 280px;
  flex-shrink: 0;
  position: sticky;
  top: 100px;
}

.filter-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.filter-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-light);
}

.filter-icon {
  color: var(--primary-color);
  font-size: 20px;
}

.filter-section {
  margin-bottom: 24px;
}

.filter-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 16px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-title::before {
  content: '';
  width: 3px;
  height: 14px;
  background: var(--primary-color);
  border-radius: 2px;
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-height: 320px;
  overflow-y: auto;
  padding-right: 8px;
}

.category-list::-webkit-scrollbar {
  width: 4px;
}

.category-list::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 2px;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
  color: var(--text-regular);
}

.category-item:hover {
  background: var(--primary-50);
  color: var(--primary-color);
}

.category-item.active {
  background: linear-gradient(135deg, var(--primary-50) 0%, #e6f4ff 100%);
  color: var(--primary-color);
  font-weight: 500;
}

.category-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--border-color);
  flex-shrink: 0;
}

.category-item.active .category-dot {
  background: var(--primary-color);
}

.category-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.category-count {
  font-size: 12px;
  color: var(--primary-color);
}

.category-child {
  padding-left: 28px;
  font-size: 13px;
  color: var(--text-secondary);
}

.category-child:hover {
  color: var(--primary-color);
}

.price-inputs {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.price-input {
  flex: 1;
}

.price-input :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px var(--border-color) inset;
  padding: 0 12px;
}

.price-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--primary-color) inset;
}

.price-separator {
  color: var(--text-secondary);
  font-size: 14px;
}

.filter-btn {
  width: 100%;
  height: 44px;
  border-radius: 10px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.reset-btn {
  width: 100%;
  height: 44px;
  border-radius: 10px;
  border: 1px solid var(--border-color);
  color: var(--text-regular);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.reset-btn:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.products-main {
  flex: 1;
  min-width: 0;
}

.toolbar-card {
  background: #fff;
  border-radius: 16px;
  padding: 16px 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.toolbar-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.sort-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.sort-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--text-secondary);
  white-space: nowrap;
}

.sort-options :deep(.el-radio-button__inner) {
  border-radius: 10px !important;
  border: 1px solid var(--border-color) !important;
  padding: 10px 20px;
  font-weight: 500;
  transition: all 0.2s;
  margin: 0 3px;
  white-space: nowrap;
}

.sort-options :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: var(--primary-color) !important;
  border-color: var(--primary-color) !important;
  color: #fff !important;
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3);
}

.result-info {
  display: flex;
  align-items: center;
  gap: 4px;
}

.result-text {
  font-size: 14px;
  color: var(--text-secondary);
}

.result-count {
  font-size: 24px;
  font-weight: 700;
  color: var(--primary-color);
  margin: 0 4px;
}

.products-container {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  min-height: 400px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.pagination-wrapper {
  margin-top: 32px;
  display: flex;
  justify-content: center;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.empty-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px 0;
}

.empty-desc {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0 0 24px 0;
}

.empty-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border-radius: 10px;
}

@media (max-width: 1200px) {
  .products-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 992px) {
  .main-layout {
    flex-direction: column;
  }

  .filter-sidebar {
    width: 100%;
    position: static;
  }

  .category-list {
    max-height: none;
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
  }

  .category-item {
    justify-content: center;
    text-align: center;
  }

  .category-dot {
    display: none;
  }

  .category-child {
    padding-left: 12px;
  }
}

@media (max-width: 768px) {
  .page-header {
    padding: 24px 0;
  }

  .page-title {
    font-size: 24px;
  }

  .title-icon {
    font-size: 28px;
  }

  .page-container {
    padding: 16px;
  }

  .toolbar-content {
    flex-direction: column;
    align-items: flex-start;
  }

  .sort-section {
    flex-direction: column;
    align-items: flex-start;
    width: 100%;
  }

  .sort-options {
    width: 100%;
    overflow-x: auto;
    padding-bottom: 8px;
  }

  .products-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .products-container {
    padding: 16px;
  }

  .category-list {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .products-grid {
    grid-template-columns: 1fr;
  }
}
</style>
