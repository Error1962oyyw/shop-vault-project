<script setup lang="ts">
import { ref, onMounted } from 'vue'
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
  categoryId: undefined as number | undefined,
  price: 0,
  originalPrice: 0,
  stock: 0,
  description: '',
  mainImage: ''
})

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
  try {
    const formData = new FormData()
    Object.entries(publishForm.value).forEach(([key, value]) => {
      if (value !== undefined && value !== '') {
        formData.append(key, String(value))
      }
    })
    await publishProduct(formData)
    ElMessage.success('发布成功')
    showPublishDialog.value = false
    fetchProducts()
  } catch (error) {
    console.error('发布失败', error)
  }
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
      <el-button type="primary" class="add-btn" @click="showPublishDialog = true">
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

  <el-dialog v-model="showPublishDialog" title="发布商品" width="600px" class="custom-dialog">
    <el-form :model="publishForm" label-width="100px" class="publish-form">
      <el-form-item label="商品名称" required>
        <el-input v-model="publishForm.name" placeholder="请输入商品名称" />
      </el-form-item>
      <el-form-item label="商品分类" required>
        <el-select v-model="publishForm.categoryId" placeholder="请选择分类" class="w-full">
          <el-option 
            v-for="cat in categories" 
            :key="cat.id" 
            :label="cat.name" 
            :value="cat.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="售价" required>
        <el-input-number v-model="publishForm.price" :min="0" :precision="2" />
      </el-form-item>
      <el-form-item label="原价">
        <el-input-number v-model="publishForm.originalPrice" :min="0" :precision="2" />
      </el-form-item>
      <el-form-item label="库存" required>
        <el-input-number v-model="publishForm.stock" :min="0" />
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
