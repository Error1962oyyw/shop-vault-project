<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Filter, Sort, ArrowRight, Search, Refresh, ArrowDown } from '@element-plus/icons-vue'
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
  categoryIds: [] as number[],
  keyword: '',
  minPrice: undefined as number | undefined,
  maxPrice: undefined as number | undefined,
  sortBy: 'default',
  sortOrder: 'desc' as 'asc' | 'desc'
})

const categoryDropdownVisible = ref(false)
const expandedCategories = ref<number[]>([])

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

const selectedCategoriesDisplay = computed(() => {
  if (filters.categoryIds.length === 0) return []
  
  const result: { id: number; name: string; parentId?: number }[] = []
  
  const findCategory = (cats: Category[], targetId: number): Category | undefined => {
    for (const cat of cats) {
      if (cat.id === targetId) return cat
      if (cat.children?.length) {
        const found = findCategory(cat.children, targetId)
        if (found) return found
      }
    }
  }
  
  for (const id of filters.categoryIds) {
    const cat = findCategory(categories.value, id)
    if (cat) {
      result.push({ id: cat.id, name: cat.name, parentId: cat.parentId })
    }
  }
  
  return result
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
    const categoryId = filters.categoryIds.length > 0 ? filters.categoryIds[0] : undefined
    
    const res: PageResult<Product> = await getProductList({
      categoryId: categoryId,
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

const handleSortChange = () => {
  pagination.current = 1
  fetchProducts()
}

const toggleCategoryExpand = (categoryId: number) => {
  const index = expandedCategories.value.indexOf(categoryId)
  if (index > -1) {
    expandedCategories.value.splice(index, 1)
  } else {
    expandedCategories.value.push(categoryId)
  }
}

const isCategoryExpanded = (categoryId: number) => {
  return expandedCategories.value.includes(categoryId)
}

const isCategorySelected = (categoryId: number) => {
  return filters.categoryIds.includes(categoryId)
}

const handleCategoryToggle = (category: Category) => {
  if (category.children?.length) {
    toggleCategoryExpand(category.id)
  }
}

const handleCategorySelect = (category: Category, isParent: boolean) => {
  if (isParent) {
    const allChildIds = category.children?.map(c => c.id) || []
    const parentId = category.id
    
    if (filters.categoryIds.includes(parentId)) {
      filters.categoryIds = filters.categoryIds.filter(id => id !== parentId && !allChildIds.includes(id))
    } else {
      filters.categoryIds = [parentId]
    }
  } else {
    const index = filters.categoryIds.indexOf(category.id)
    if (index > -1) {
      filters.categoryIds.splice(index, 1)
    } else {
      const parentId = category.parentId
      if (parentId && filters.categoryIds.includes(parentId)) {
        filters.categoryIds = filters.categoryIds.filter(id => id !== parentId)
      }
      filters.categoryIds.push(category.id)
    }
  }
  
  pagination.current = 1
  fetchProducts()
}

const selectAllCategories = () => {
  filters.categoryIds = []
  pagination.current = 1
  fetchProducts()
}

const removeCategory = (categoryId: number) => {
  filters.categoryIds = filters.categoryIds.filter(id => id !== categoryId)
  pagination.current = 1
  fetchProducts()
}

const clearAllCategories = () => {
  filters.categoryIds = []
  pagination.current = 1
  fetchProducts()
}

const handleSearch = () => {
  pagination.current = 1
  fetchProducts()
}

const resetFilters = () => {
  filters.categoryIds = []
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
    filters.categoryIds = [Number(query.categoryId)]
  }
  if (query.keyword) {
    filters.keyword = String(query.keyword)
  }
  fetchProducts()
}, { immediate: true })

watch(() => pagination.current, () => {
  fetchProducts()
})

watch(() => pagination.size, () => {
  pagination.current = 1
  fetchProducts()
})

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
                <div class="category-dropdown">
                  <div 
                    class="category-dropdown-trigger"
                    @click="categoryDropdownVisible = !categoryDropdownVisible"
                  >
                    <span>{{ filters.categoryIds.length === 0 ? '全部分类' : `已选 ${filters.categoryIds.length} 个分类` }}</span>
                    <el-icon :class="{ 'is-rotate': categoryDropdownVisible }"><ArrowDown /></el-icon>
                  </div>
                  
                  <Transition name="dropdown">
                    <div v-show="categoryDropdownVisible" class="category-dropdown-menu">
                      <div 
                        class="category-option all-option"
                        :class="{ active: filters.categoryIds.length === 0 }"
                        @click="selectAllCategories"
                      >
                        <span class="option-checkbox" :class="{ checked: filters.categoryIds.length === 0 }">
                          <span v-if="filters.categoryIds.length === 0" class="check-icon">✓</span>
                        </span>
                        <span class="option-label">全部分类</span>
                      </div>
                      
                      <div v-for="category in categories" :key="category.id" class="category-group">
                        <div 
                          class="category-option parent-option"
                          :class="{ active: isCategorySelected(category.id), expanded: isCategoryExpanded(category.id) }"
                        >
                          <span 
                            class="option-checkbox" 
                            :class="{ checked: isCategorySelected(category.id) }"
                            @click.stop="handleCategorySelect(category, true)"
                          >
                            <span v-if="isCategorySelected(category.id)" class="check-icon">✓</span>
                          </span>
                          <span class="option-label" @click="handleCategoryToggle(category)">{{ category.name }}</span>
                          <el-icon 
                            v-if="category.children?.length" 
                            class="expand-icon" 
                            :class="{ expanded: isCategoryExpanded(category.id) }"
                            @click="handleCategoryToggle(category)"
                          >
                            <ArrowDown />
                          </el-icon>
                        </div>
                        
                        <Transition name="expand">
                          <div v-show="isCategoryExpanded(category.id) && category.children?.length" class="category-children">
                            <div 
                              v-for="child in category.children" 
                              :key="child.id"
                              class="category-option child-option"
                              :class="{ active: isCategorySelected(child.id) }"
                              @click.stop="handleCategorySelect(child, false)"
                            >
                              <span class="option-checkbox" :class="{ checked: isCategorySelected(child.id) }">
                                <span v-if="isCategorySelected(child.id)" class="check-icon">✓</span>
                              </span>
                              <span class="option-label">{{ child.name }}</span>
                            </div>
                          </div>
                        </Transition>
                      </div>
                    </div>
                  </Transition>
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
              
              <div v-if="selectedCategoriesDisplay.length > 0" class="selected-tags">
                <span class="tags-label">已选分类：</span>
                <div class="tags-container">
                  <el-tag 
                    v-for="cat in selectedCategoriesDisplay" 
                    :key="cat.id"
                    type="primary"
                    size="large"
                    closable
                    @close="removeCategory(cat.id)"
                  >
                    {{ cat.name }}
                  </el-tag>
                  <el-button 
                    v-if="selectedCategoriesDisplay.length > 1"
                    type="primary" 
                    link 
                    size="small"
                    @click="clearAllCategories"
                  >
                    清空全部
                  </el-button>
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
                    v-model:current-page="pagination.current"
                    v-model:page-size="pagination.size"
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
  font-size: 24px;
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.page-subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.95);
  margin: 0;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
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

.category-dropdown {
  position: relative;
}

.category-dropdown-trigger {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f7f8fa;
  border: 1px solid var(--border-color);
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  color: var(--text-primary);
  transition: all 0.2s;
}

.category-dropdown-trigger:hover {
  border-color: var(--primary-color);
}

.category-dropdown-trigger .is-rotate {
  transform: rotate(180deg);
}

.category-dropdown-menu {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  right: 0;
  background: #fff;
  border: 1px solid var(--border-color);
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  max-height: 400px;
  overflow-y: auto;
  z-index: 100;
  padding: 8px;
}

.category-dropdown-menu::-webkit-scrollbar {
  width: 4px;
}

.category-dropdown-menu::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 2px;
}

.category-option {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
  color: var(--text-regular);
}

.category-option:hover {
  background: var(--primary-50);
  color: var(--primary-color);
}

.category-option.active {
  background: linear-gradient(135deg, var(--primary-50) 0%, #e6f4ff 100%);
  color: var(--primary-color);
}

.option-checkbox {
  width: 18px;
  height: 18px;
  border: 2px solid var(--border-color);
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.2s;
}

.option-checkbox.checked {
  background: var(--primary-color);
  border-color: var(--primary-color);
}

.check-icon {
  color: #fff;
  font-size: 12px;
  font-weight: bold;
}

.option-label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.expand-icon {
  font-size: 12px;
  color: var(--text-secondary);
  transition: transform 0.3s;
}

.expand-icon.expanded {
  transform: rotate(180deg);
}

.category-children {
  margin-left: 16px;
  padding-left: 8px;
  border-left: 2px solid var(--border-light);
}

.child-option {
  font-size: 13px;
  color: var(--text-secondary);
}

.all-option {
  font-weight: 500;
  border-bottom: 1px solid var(--border-light);
  margin-bottom: 8px;
  padding-bottom: 12px;
}

.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.2s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.expand-enter-active,
.expand-leave-active {
  transition: all 0.3s ease;
  overflow: hidden;
}

.expand-enter-from,
.expand-leave-to {
  opacity: 0;
  max-height: 0;
}

.expand-enter-to,
.expand-leave-from {
  opacity: 1;
  max-height: 500px;
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
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.sort-section {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 16px;
}

.sort-label {
  display: flex;
  flex-direction: row;
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
  display: inline-flex;
  flex-direction: row;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
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

.selected-tags {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--border-light);
  flex-wrap: wrap;
}

.tags-label {
  font-size: 14px;
  color: var(--text-secondary);
  white-space: nowrap;
}

.tags-container {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.tags-container :deep(.el-tag) {
  border-radius: 8px;
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

  .category-dropdown-menu {
    position: static;
    max-height: none;
    box-shadow: none;
    border: none;
    padding: 0;
    margin-top: 8px;
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
    font-size: 20px;
    width: 28px;
    height: 28px;
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
}

@media (max-width: 480px) {
  .products-grid {
    grid-template-columns: 1fr;
  }
}
</style>
