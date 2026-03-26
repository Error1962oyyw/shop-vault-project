<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { getProductList, publishProduct, getCategoryList } from '@/api/product'
import { usePagination } from '@/composables'
import { AdminPageLayout } from '@/components/admin'
import { formatMoney } from '@/utils/format'
import type { Product, Category, PageResult } from '@/types/api'

const loading = ref(false)
const products = ref<Product[]>([])
const categories = ref<Category[]>([])

const { pagination, handleCurrentChange, setTotal, getParams } = usePagination({
  onPageChange: () => fetchProducts()
})

const showPublishDialog = ref(false)
const publishForm = ref({
  name: '',
  parentCategoryId: undefined as number | undefined,
  categoryId: undefined as number | undefined,
  price: 0,
  originalPrice: 0,
  stock: 0,
  description: '',
  mainImage: ''
})

const categorySearchKeyword = ref('')

const parentCategories = computed(() => {
  return categories.value.filter(cat => cat.level === 1)
})

const subCategories = computed(() => {
  if (!publishForm.value.parentCategoryId) return []
  const parent = categories.value.find(cat => cat.id === publishForm.value.parentCategoryId)
  return parent?.children || []
})

const filteredSubCategories = computed(() => {
  if (!categorySearchKeyword.value) return subCategories.value
  const keyword = categorySearchKeyword.value.toLowerCase()
  return subCategories.value.filter(cat => 
    cat.name.toLowerCase().includes(keyword) ||
    (cat.yoloLabel && cat.yoloLabel.toLowerCase().includes(keyword))
  )
})

const selectedCategory = computed(() => {
  if (!publishForm.value.categoryId) return null
  return subCategories.value.find(cat => cat.id === publishForm.value.categoryId)
})

const handleParentCategoryChange = () => {
  publishForm.value.categoryId = undefined
  categorySearchKeyword.value = ''
}

const handleCategoryChange = () => {
  // Category selected
}

const getCategoryDisplayName = (category: Category) => {
  if (category.cocoId) {
    return `${category.name} (${category.cocoId}: ${category.yoloLabel})`
  }
  return category.name
}

const fetchProducts = async () => {
  loading.value = true
  try {
    const res: PageResult<Product> = await getProductList(getParams())
    products.value = res.records
    setTotal(res.total)
  } catch (error) {
    console.error('获取商品失败', error)
  } finally {
    loading.value = false
  }
}

const fetchCategories = async () => {
  try {
    categories.value = await getCategoryList()
  } catch (error) {
    console.error('获取分类失败', error)
  }
}

const handlePublish = async () => {
  if (!publishForm.value.categoryId) {
    ElMessage.warning('请选择商品分类')
    return
  }
  
  try {
    const formData = new FormData()
    formData.append('name', publishForm.value.name)
    formData.append('categoryId', String(publishForm.value.categoryId))
    formData.append('price', String(publishForm.value.price))
    formData.append('originalPrice', String(publishForm.value.originalPrice))
    formData.append('stock', String(publishForm.value.stock))
    formData.append('description', publishForm.value.description)
    formData.append('mainImage', publishForm.value.mainImage)
    
    await publishProduct(formData)
    ElMessage.success('发布成功')
    showPublishDialog.value = false
    resetForm()
    fetchProducts()
  } catch (error) {
    console.error('发布失败', error)
    ElMessage.error('发布失败，请重试')
  }
}

const resetForm = () => {
  publishForm.value = {
    name: '',
    parentCategoryId: undefined,
    categoryId: undefined,
    price: 0,
    originalPrice: 0,
    stock: 0,
    description: '',
    mainImage: ''
  }
  categorySearchKeyword.value = ''
}

const openPublishDialog = () => {
  resetForm()
  showPublishDialog.value = true
}

onMounted(() => {
  fetchProducts()
  fetchCategories()
})
</script>

<template>
  <AdminPageLayout title="商品管理" subtitle="管理商品信息、上下架状态">
    <template #actions>
      <el-input 
        placeholder="搜索商品" 
        class="search-input"
        clearable
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" class="add-btn" @click="openPublishDialog">
        <el-icon class="mr-1"><Plus /></el-icon>
        发布商品
      </el-button>
    </template>

    <el-table :data="products" v-loading="loading" stripe>
      <el-table-column label="商品信息" min-width="280">
        <template #default="{ row }">
          <div class="product-info">
            <img 
              :src="row.mainImage" 
              :alt="row.name"
              class="product-image"
            />
            <div class="product-details">
              <h4 class="product-name">{{ row.name }}</h4>
              <p class="product-category">{{ row.categoryName }}</p>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="价格" width="120">
        <template #default="{ row }">
          <span class="price">{{ formatMoney(row.price) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="100" align="center" />
      <el-table-column prop="sales" label="销量" width="100" align="center" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="center">
        <template #default>
          <el-button type="primary" link size="small">编辑</el-button>
          <el-button type="danger" link size="small">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination
        :current-page="pagination.current"
        :page-size="pagination.size"
        :total="pagination.total"
        layout="total, prev, pager, next"
        @current-change="handleCurrentChange"
      />
    </div>
  </AdminPageLayout>

  <el-dialog v-model="showPublishDialog" title="发布商品" width="650px" class="custom-dialog" @close="resetForm">
    <el-form :model="publishForm" label-width="100px" class="publish-form">
      <el-form-item label="商品名称" required>
        <el-input v-model="publishForm.name" placeholder="请输入商品名称" />
      </el-form-item>
      
      <el-form-item label="商品分类" required>
        <div class="category-selector">
          <el-select 
            v-model="publishForm.parentCategoryId"
            placeholder="选择大类"
            class="parent-category-select"
            @change="handleParentCategoryChange"
          >
            <el-option 
              v-for="cat in parentCategories" 
              :key="cat.id" 
              :label="cat.name" 
              :value="cat.id"
            >
              <div class="category-option">
                <span>{{ cat.name }}</span>
                <span class="category-count">{{ cat.children?.length || 0 }} 个小类</span>
              </div>
            </el-option>
          </el-select>
          
          <el-select 
            v-model="publishForm.categoryId"
            placeholder="选择小类"
            class="sub-category-select"
            :disabled="!publishForm.parentCategoryId"
            filterable
            :filter-method="(query: string) => categorySearchKeyword = query"
            @change="handleCategoryChange"
          >
            <el-option 
              v-for="cat in filteredSubCategories" 
              :key="cat.id" 
              :label="getCategoryDisplayName(cat)"
              :value="cat.id"
            >
              <div class="subcategory-option">
                <span class="subcategory-name">{{ cat.name }}</span>
                <span v-if="cat.cocoId" class="coco-info">
                  COCO #{{ cat.cocoId }}: {{ cat.yoloLabel }}
                </span>
              </div>
            </el-option>
          </el-select>
        </div>
        
        <div v-if="selectedCategory" class="selected-category-info">
          <el-tag type="success" size="small">
            已选择: {{ selectedCategory.name }}
            <span v-if="selectedCategory.cocoId">
              (COCO #{{ selectedCategory.cocoId }})
            </span>
          </el-tag>
        </div>
      </el-form-item>
      
      <el-form-item label="售价" required>
        <el-input-number v-model="publishForm.price" :min="0" :precision="2" />
        <span class="price-unit">元</span>
      </el-form-item>
      <el-form-item label="原价">
        <el-input-number v-model="publishForm.originalPrice" :min="0" :precision="2" />
        <span class="price-unit">元</span>
      </el-form-item>
      <el-form-item label="库存" required>
        <el-input-number v-model="publishForm.stock" :min="0" />
        <span class="price-unit">件</span>
      </el-form-item>
      <el-form-item label="商品描述">
        <el-input 
          v-model="publishForm.description" 
          type="textarea" 
          :rows="3"
          placeholder="请输入商品描述"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showPublishDialog = false">取消</el-button>
      <el-button type="primary" @click="handlePublish">发布</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.search-input {
  width: 260px;
}

.add-btn {
  border-radius: 10px;
  font-weight: 600;
}

.product-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-image {
  width: 64px;
  height: 64px;
  border-radius: 10px;
  object-fit: cover;
  flex-shrink: 0;
}

.product-details {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 4px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-category {
  font-size: 12px;
  color: #6b7280;
  margin: 0;
}

.price {
  color: #ff4d4f;
  font-weight: 700;
  font-size: 15px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 16px 0 0 0;
}

.mr-1 {
  margin-right: 4px;
}

.category-selector {
  display: flex;
  gap: 12px;
  width: 100%;
}

.parent-category-select {
  width: 180px;
}

.sub-category-select {
  flex: 1;
}

.category-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.category-count {
  font-size: 12px;
  color: #909399;
}

.subcategory-option {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.subcategory-name {
  font-weight: 500;
}

.coco-info {
  font-size: 12px;
  color: #909399;
}

.selected-category-info {
  margin-top: 8px;
}

.price-unit {
  margin-left: 8px;
  color: #909399;
  font-size: 14px;
}

:deep(.el-table) {
  border-radius: 16px;
  overflow: hidden;
}

:deep(.el-table th) {
  background: #f8fafc;
  color: #374151;
  font-weight: 600;
  font-size: 14px;
}

:deep(.el-table td) {
  padding: 16px 0;
}

:deep(.custom-dialog .el-dialog) {
  border-radius: 16px;
}

:deep(.custom-dialog .el-dialog__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #f2f3f5;
}

:deep(.custom-dialog .el-dialog__title) {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
}

:deep(.custom-dialog .el-dialog__body) {
  padding: 24px;
}

:deep(.custom-dialog .el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #f2f3f5;
}
</style>
