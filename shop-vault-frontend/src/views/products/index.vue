<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Filter, Sort, ArrowRight, Search } from '@element-plus/icons-vue'
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
  sortBy: 'createTime',
  sortOrder: 'desc' as 'asc' | 'desc'
})

const sortOptions = [
  { label: '综合排序', value: 'createTime', order: 'desc' },
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
  filters.sortBy = 'createTime'
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
    <div class="bg-gradient-to-b from-slate-50 to-white min-h-screen">
      <div class="page-container py-8">
        <div class="flex flex-col lg:flex-row gap-6">
          <aside class="w-full lg:w-64 shrink-0">
            <div class="bg-white rounded-xl shadow-sm p-6 sticky top-24 border border-gray-100">
              <h3 class="font-bold text-lg mb-6 flex items-center gap-2 section-title">
                <el-icon class="text-blue-500"><Filter /></el-icon>
                筛选条件
              </h3>

              <div class="mb-8">
                <h4 class="font-semibold mb-4 text-gray-700 flex items-center gap-2">
                  <span class="w-1 h-4 bg-blue-500 rounded-full"></span>
                  商品分类
                </h4>
                <div class="space-y-2">
                  <div 
                    class="cursor-pointer py-2.5 px-4 rounded-lg transition-all duration-200 font-medium"
                    :class="!filters.categoryId ? 'bg-gradient-to-r from-blue-50 to-cyan-50 text-blue-600 border border-blue-200' : 'text-gray-600 hover:bg-gray-50'"
                    @click="handleCategorySelect(undefined)"
                  >
                    全部分类
                  </div>
                  <template v-for="category in categories" :key="category.id">
                    <div 
                      class="cursor-pointer py-2.5 px-4 rounded-lg transition-all duration-200 font-medium"
                      :class="filters.categoryId === category.id ? 'bg-gradient-to-r from-blue-50 to-cyan-50 text-blue-600 border border-blue-200' : 'text-gray-600 hover:bg-gray-50'"
                      @click="handleCategorySelect(category.id)"
                    >
                      {{ category.name }}
                    </div>
                    <template v-if="category.children?.length">
                      <div 
                        v-for="child in category.children" 
                        :key="child.id"
                        class="cursor-pointer py-2 px-4 pl-8 rounded-lg transition-all duration-200 text-sm"
                        :class="filters.categoryId === child.id ? 'bg-gradient-to-r from-blue-50 to-cyan-50 text-blue-600 border border-blue-200' : 'text-gray-500 hover:bg-gray-50'"
                        @click="handleCategorySelect(child.id)"
                      >
                        {{ child.name }}
                      </div>
                    </template>
                  </template>
                </div>
              </div>

              <div class="mb-8">
                <h4 class="font-semibold mb-4 text-gray-700 flex items-center gap-2">
                  <span class="w-1 h-4 bg-blue-500 rounded-full"></span>
                  价格区间
                </h4>
                <div class="flex items-center gap-3">
                  <el-input 
                    v-model.number="filters.minPrice" 
                    placeholder="最低价" 
                    size="default"
                    type="number"
                    class="input-field"
                  />
                  <span class="text-gray-400 font-semibold">-</span>
                  <el-input 
                    v-model.number="filters.maxPrice" 
                    placeholder="最高价" 
                    size="default"
                    type="number"
                    class="input-field"
                  />
                </div>
                <el-button 
                  type="primary" 
                  size="default" 
                  class="w-full mt-4 h-11 rounded-lg btn-primary"
                  @click="handleSearch"
                >
                  <span class="flex items-center justify-center gap-2">
                    <Search class="w-4 h-4" />
                    确定筛选
                  </span>
                </el-button>
              </div>

              <el-button 
                type="default" 
                size="default" 
                class="w-full h-11 rounded-lg border-gray-200 hover:border-gray-300 hover:bg-gray-50 text-gray-600"
                @click="resetFilters"
              >
                重置筛选
              </el-button>
            </div>
          </aside>

          <main class="flex-1">
            <div class="bg-white rounded-xl shadow-sm p-6 mb-6 border border-gray-100">
              <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                <div class="flex flex-wrap items-center gap-4">
                  <span class="text-gray-500 font-medium flex items-center gap-2">
                    <Sort class="w-4 h-4" />
                    排序：
                  </span>
                  <div class="flex flex-wrap gap-2">
                    <el-radio-group v-model="currentSort" size="default" @change="handleSortChange">
                      <el-radio-button 
                        v-for="option in sortOptions" 
                        :key="`${option.value}-${option.order}`"
                        :value="`${option.value}-${option.order}`"
                        class="rounded-lg"
                      >
                        {{ option.label }}
                      </el-radio-button>
                    </el-radio-group>
                  </div>
                </div>
                <div class="text-gray-600">
                  共 <span class="text-blue-500 font-bold text-xl">{{ pagination.total }}</span> 件商品
                </div>
              </div>
            </div>

            <div v-loading="loading" class="bg-white rounded-xl shadow-sm p-6 border border-gray-100">
              <template v-if="products.length > 0">
                <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                  <ProductCard 
                    v-for="product in products" 
                    :key="product.id" 
                    :product="product"
                  />
                </div>
                <div class="mt-8 flex justify-center">
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
                <div class="py-16 text-center">
                  <el-empty description="暂无商品" :image-size="120">
                    <el-button type="primary" @click="resetFilters" class="rounded-lg btn-primary">
                      <span class="flex items-center gap-2">
                        查看全部商品
                        <ArrowRight class="w-4 h-4" />
                      </span>
                    </el-button>
                  </el-empty>
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
.section-title {
  padding-left: 0;
}

.section-title::before {
  display: none;
}

:deep(.el-radio-button__inner) {
  border-radius: var(--radius-sm) !important;
  border: 1px solid var(--border-color) !important;
  margin: 0 2px;
  transition: all 0.2s;
}

:deep(.el-radio-button__orig-radio:checked + .el-radio-button__inner) {
  background: var(--primary-color) !important;
  border-color: var(--primary-color) !important;
  color: white !important;
}

:deep(.el-input__wrapper) {
  border-radius: var(--radius-sm) !important;
  box-shadow: 0 0 0 1px var(--border-color) inset !important;
  transition: all 0.2s ease !important;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--primary-color) inset !important;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--primary-color) inset, 0 0 0 2px var(--primary-opacity-10) !important;
}
</style>
