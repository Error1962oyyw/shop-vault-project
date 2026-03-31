<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Plus, Upload, Delete, Setting } from '@element-plus/icons-vue'
import { getProductList, publishProduct, getCategoryList, uploadImage } from '@/api/product'
import { getSpecTemplates, getProductSpecs, addProductSpec, deleteProductSpec, addProductSpecValue, deleteProductSpecValue } from '@/api/spec'
import { usePagination } from '@/composables'
import { AdminPageLayout } from '@/components/admin'
import { formatMoney } from '@/utils/format'
import type { Product, Category, PageResult, Spec, ProductSpec } from '@/types/api'

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
  mainImage: '',
  detailImages: [] as string[]
})

const categorySearchKeyword = ref('')
const mainImageUploading = ref(false)
const detailImagesUploading = ref(false)

const specTemplates = ref<Spec[]>([])
const productSpecs = ref<ProductSpec[]>([])
const showSpecDialog = ref(false)
const currentEditProductId = ref<number | null>(null)
const newSpecName = ref('')
const newSpecValue = ref('')
const selectedSpecTemplate = ref<number | undefined>()

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

const fetchSpecTemplates = async () => {
  try {
    specTemplates.value = await getSpecTemplates()
  } catch (error) {
    console.error('获取规格模板失败', error)
  }
}

const handleMainImageUpload = async (options: { file: File }) => {
  mainImageUploading.value = true
  try {
    const url = await uploadImage(options.file)
    publishForm.value.mainImage = url
    ElMessage.success('主图上传成功')
  } catch (error) {
    console.error('上传失败', error)
    ElMessage.error('上传失败，请重试')
  } finally {
    mainImageUploading.value = false
  }
}

const handleMainImageDelete = () => {
  publishForm.value.mainImage = ''
}

const handleDetailImagesUpload = async (options: { file: File }) => {
  detailImagesUploading.value = true
  try {
    const url = await uploadImage(options.file)
    publishForm.value.detailImages.push(url)
    ElMessage.success('详情图上传成功')
  } catch (error) {
    console.error('上传失败', error)
    ElMessage.error('上传失败，请重试')
  } finally {
    detailImagesUploading.value = false
  }
}

const handleDetailImageDelete = (index: number) => {
  publishForm.value.detailImages.splice(index, 1)
}

const moveDetailImage = (index: number, direction: 'left' | 'right') => {
  const images = publishForm.value.detailImages
  if (direction === 'left' && index > 0) {
    [images[index], images[index - 1]] = [images[index - 1], images[index]]
  } else if (direction === 'right' && index < images.length - 1) {
    [images[index], images[index + 1]] = [images[index + 1], images[index]]
  }
}

const handlePublish = async () => {
  if (!publishForm.value.name.trim()) {
    ElMessage.warning('请输入商品名称')
    return
  }
  if (!publishForm.value.categoryId) {
    ElMessage.warning('请选择商品分类')
    return
  }
  if (publishForm.value.price <= 0) {
    ElMessage.warning('请输入有效的商品价格')
    return
  }
  if (publishForm.value.stock < 0) {
    ElMessage.warning('库存不能为负数')
    return
  }
  
  try {
    await publishProduct({
      name: publishForm.value.name,
      categoryId: publishForm.value.categoryId,
      price: publishForm.value.price,
      originalPrice: publishForm.value.originalPrice || undefined,
      stock: publishForm.value.stock,
      description: publishForm.value.description || undefined,
      mainImage: publishForm.value.mainImage || undefined,
      detailImages: publishForm.value.detailImages.length > 0 ? publishForm.value.detailImages : undefined
    })
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
    mainImage: '',
    detailImages: []
  }
  categorySearchKeyword.value = ''
}

const openPublishDialog = () => {
  resetForm()
  showPublishDialog.value = true
}

const openSpecDialog = async (productId: number) => {
  currentEditProductId.value = productId
  showSpecDialog.value = true
  await loadProductSpecs(productId)
}

const loadProductSpecs = async (productId: number) => {
  try {
    productSpecs.value = await getProductSpecs(productId)
  } catch (error) {
    console.error('获取商品规格失败', error)
    productSpecs.value = []
  }
}

const handleAddCustomSpec = async () => {
  if (!newSpecName.value.trim()) {
    ElMessage.warning('请输入规格名称')
    return
  }
  if (!currentEditProductId.value) return

  try {
    await addProductSpec(currentEditProductId.value, {
      specName: newSpecName.value.trim(),
      isCustom: 1,
      values: []
    })
    ElMessage.success('添加规格成功')
    newSpecName.value = ''
    await loadProductSpecs(currentEditProductId.value)
  } catch (error) {
    console.error('添加规格失败', error)
    ElMessage.error('添加规格失败')
  }
}

const handleAddSpecFromTemplate = async () => {
  if (!selectedSpecTemplate.value) {
    ElMessage.warning('请选择规格模板')
    return
  }
  if (!currentEditProductId.value) return

  const template = specTemplates.value.find(s => s.id === selectedSpecTemplate.value)
  if (!template) return

  try {
    await addProductSpec(currentEditProductId.value, {
      specId: template.id,
      specName: template.name,
      isCustom: 0,
      values: template.values?.map((v, index) => ({
        specValueId: v.id,
        value: v.value,
        sortOrder: index,
        isCustom: 0
      })) || []
    })
    ElMessage.success('添加规格成功')
    selectedSpecTemplate.value = undefined
    await loadProductSpecs(currentEditProductId.value)
  } catch (error) {
    console.error('添加规格失败', error)
    ElMessage.error('添加规格失败')
  }
}

const handleDeleteSpec = async (specId: number) => {
  try {
    await deleteProductSpec(specId)
    ElMessage.success('删除成功')
    if (currentEditProductId.value) {
      await loadProductSpecs(currentEditProductId.value)
    }
  } catch (error) {
    console.error('删除规格失败', error)
    ElMessage.error('删除失败')
  }
}

const handleAddSpecValue = async (productSpecId: number) => {
  if (!newSpecValue.value.trim()) {
    ElMessage.warning('请输入规格值')
    return
  }

  try {
    await addProductSpecValue(productSpecId, {
      value: newSpecValue.value.trim(),
      isCustom: 1
    })
    ElMessage.success('添加规格值成功')
    newSpecValue.value = ''
    if (currentEditProductId.value) {
      await loadProductSpecs(currentEditProductId.value)
    }
  } catch (error) {
    console.error('添加规格值失败', error)
    ElMessage.error('添加规格值失败')
  }
}

const handleDeleteSpecValue = async (valueId: number) => {
  try {
    await deleteProductSpecValue(valueId)
    ElMessage.success('删除成功')
    if (currentEditProductId.value) {
      await loadProductSpecs(currentEditProductId.value)
    }
  } catch (error) {
    console.error('删除规格值失败', error)
    ElMessage.error('删除失败')
  }
}

watch(showSpecDialog, (val) => {
  if (!val) {
    currentEditProductId.value = null
    productSpecs.value = []
    newSpecName.value = ''
    newSpecValue.value = ''
    selectedSpecTemplate.value = undefined
  }
})

onMounted(() => {
  fetchProducts()
  fetchCategories()
  fetchSpecTemplates()
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
      <el-table-column label="操作" width="200" align="center">
        <template #default="{ row }">
          <el-button type="primary" link size="small">编辑</el-button>
          <el-button type="warning" link size="small" @click="openSpecDialog(row.id)">
            <el-icon class="mr-1"><Setting /></el-icon>
            规格
          </el-button>
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
      
      <el-form-item label="商品主图">
        <div class="image-upload-section">
          <div v-if="publishForm.mainImage" class="main-image-preview">
            <img :src="publishForm.mainImage" alt="主图预览" />
            <div class="image-actions">
              <el-button type="danger" size="small" :icon="Delete" @click="handleMainImageDelete">
                删除
              </el-button>
            </div>
          </div>
          <el-upload
            v-else
            class="main-image-uploader"
            :show-file-list="false"
            :before-upload="() => false"
            :http-request="handleMainImageUpload"
            accept="image/*"
          >
            <div class="upload-placeholder" :class="{ 'is-uploading': mainImageUploading }">
              <el-icon v-if="!mainImageUploading" class="upload-icon"><Upload /></el-icon>
              <span v-if="mainImageUploading">上传中...</span>
              <span v-else>点击上传主图</span>
            </div>
          </el-upload>
          <div class="upload-tip">建议尺寸: 800x800px，支持 JPG、PNG 格式</div>
        </div>
      </el-form-item>
      
      <el-form-item label="详情图片">
        <div class="detail-images-section">
          <div class="detail-images-list">
            <div 
              v-for="(img, index) in publishForm.detailImages" 
              :key="index" 
              class="detail-image-item"
            >
              <img :src="img" :alt="`详情图${index + 1}`" />
              <div class="image-actions-overlay">
                <el-button-group size="small">
                  <el-button 
                    :disabled="index === 0" 
                    @click="moveDetailImage(index, 'left')"
                  >
                    ←
                  </el-button>
                  <el-button 
                    :disabled="index === publishForm.detailImages.length - 1" 
                    @click="moveDetailImage(index, 'right')"
                  >
                    →
                  </el-button>
                </el-button-group>
                <el-button type="danger" size="small" :icon="Delete" @click="handleDetailImageDelete(index)">
                  删除
                </el-button>
              </div>
              <span class="image-index">{{ index + 1 }}</span>
            </div>
            
            <el-upload
              class="detail-image-uploader"
              :show-file-list="false"
              :before-upload="() => false"
              :http-request="handleDetailImagesUpload"
              accept="image/*"
              multiple
            >
              <div class="add-detail-image" :class="{ 'is-uploading': detailImagesUploading }">
                <el-icon v-if="!detailImagesUploading" class="add-icon"><Plus /></el-icon>
                <span v-if="detailImagesUploading">上传中...</span>
                <span v-else>添加图片</span>
              </div>
            </el-upload>
          </div>
          <div class="upload-tip">支持多张图片上传，可拖拽排序，建议尺寸: 750x750px</div>
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showPublishDialog = false">取消</el-button>
      <el-button type="primary" @click="handlePublish">发布</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="showSpecDialog" title="商品规格管理" width="700px" class="custom-dialog">
    <div class="spec-manager">
      <div class="spec-add-section">
        <el-divider content-position="left">添加规格</el-divider>
        <div class="add-spec-row">
          <el-input 
            v-model="newSpecName" 
            placeholder="输入自定义规格名称，如：重量、容量" 
            class="spec-name-input"
            @keyup.enter="handleAddCustomSpec"
          />
          <el-button type="primary" @click="handleAddCustomSpec">
            <el-icon><Plus /></el-icon>
            添加自定义规格
          </el-button>
        </div>
        <div class="add-spec-row">
          <el-select 
            v-model="selectedSpecTemplate" 
            placeholder="选择规格模板"
            class="spec-template-select"
          >
            <el-option 
              v-for="spec in specTemplates" 
              :key="spec.id" 
              :label="`${spec.name} (${spec.values?.length || 0}个值)`" 
              :value="spec.id"
            />
          </el-select>
          <el-button type="success" @click="handleAddSpecFromTemplate">
            从模板添加
          </el-button>
        </div>
      </div>

      <el-divider content-position="left">已配置规格</el-divider>
      
      <div v-if="productSpecs.length === 0" class="empty-spec">
        <el-empty description="暂无规格配置，请添加规格" :image-size="80" />
      </div>

      <div v-else class="spec-list">
        <div v-for="spec in productSpecs" :key="spec.id" class="spec-item">
          <div class="spec-header">
            <div class="spec-title">
              <span class="spec-name">{{ spec.specName }}</span>
              <el-tag v-if="spec.isCustom === 1" type="warning" size="small">自定义</el-tag>
              <el-tag v-else type="info" size="small">模板</el-tag>
            </div>
            <el-button type="danger" link size="small" @click="handleDeleteSpec(spec.id)">
              删除规格
            </el-button>
          </div>
          
          <div class="spec-values">
            <el-tag 
              v-for="value in spec.values" 
              :key="value.id" 
              closable
              class="spec-value-tag"
              @close="value.id && handleDeleteSpecValue(value.id)"
            >
              {{ value.value }}
            </el-tag>
            
            <el-input 
              v-model="newSpecValue"
              placeholder="添加规格值"
              class="add-value-input"
              size="small"
              @keyup.enter="handleAddSpecValue(spec.id)"
            >
              <template #append>
                <el-button :icon="Plus" @click="handleAddSpecValue(spec.id)" />
              </template>
            </el-input>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="showSpecDialog = false">关闭</el-button>
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

.image-upload-section {
  width: 100%;
}

.main-image-preview {
  position: relative;
  width: 200px;
  height: 200px;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
}

.main-image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.main-image-preview .image-actions {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 8px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.6));
  display: flex;
  justify-content: center;
}

.main-image-uploader {
  width: 200px;
  height: 200px;
}

.upload-placeholder {
  width: 200px;
  height: 200px;
  border: 2px dashed #d9d9d9;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  background: #fafafa;
}

.upload-placeholder:hover {
  border-color: #1677ff;
  background: #f0f7ff;
}

.upload-placeholder.is-uploading {
  border-color: #1677ff;
  background: #f0f7ff;
  cursor: not-allowed;
}

.upload-icon {
  font-size: 32px;
  color: #909399;
  margin-bottom: 8px;
}

.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.detail-images-section {
  width: 100%;
}

.detail-images-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.detail-image-item {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
}

.detail-image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-image-item .image-actions-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 4px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.3s;
}

.detail-image-item:hover .image-actions-overlay {
  opacity: 1;
}

.detail-image-item .image-index {
  position: absolute;
  top: 4px;
  left: 4px;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
}

.detail-image-uploader {
  width: 120px;
  height: 120px;
}

.add-detail-image {
  width: 120px;
  height: 120px;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  background: #fafafa;
}

.add-detail-image:hover {
  border-color: #1677ff;
  background: #f0f7ff;
}

.add-detail-image.is-uploading {
  border-color: #1677ff;
  background: #f0f7ff;
  cursor: not-allowed;
}

.add-icon {
  font-size: 24px;
  color: #909399;
  margin-bottom: 4px;
}

.spec-manager {
  min-height: 300px;
}

.spec-add-section {
  margin-bottom: 20px;
}

.add-spec-row {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.spec-name-input {
  flex: 1;
}

.spec-template-select {
  flex: 1;
}

.empty-spec {
  padding: 20px 0;
}

.spec-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.spec-item {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px;
  background: #fafafa;
}

.spec-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.spec-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.spec-name {
  font-weight: 600;
  font-size: 15px;
  color: #1f2937;
}

.spec-values {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.spec-value-tag {
  margin: 0;
}

.add-value-input {
  width: 150px;
}
</style>
