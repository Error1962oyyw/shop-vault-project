<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Camera, Upload, Refresh, MagicStick, Goods, Close, Loading, WarningFilled } from '@element-plus/icons-vue'
import UserLayout from '@/components/layout/UserLayout.vue'
import { yoloSearch, getProductList } from '@/api/product'
import type { YoloSearchResult, YoloCategoryInfo, Product } from '@/types/api'

const router = useRouter()

const loading = ref(false)
const uploadProgress = ref(0)
const uploadedImage = ref('')
const fileInputRef = ref<HTMLInputElement | null>(null)
const searchResult = ref<YoloSearchResult | null>(null)
const allMatchedProducts = ref<Map<number, Product[]>>(new Map())
const selectedCategoryId = ref<number | null>(null)
const errorMessage = ref('')

const MAX_FILE_SIZE = 5 * 1024 * 1024
const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/jpg', 'image/webp']

const matchedCategories = computed(() => {
  return searchResult.value?.matchedCategories || []
})

const currentProducts = computed(() => {
  if (selectedCategoryId.value === null) {
    const allProducts: Product[] = []
    allMatchedProducts.value.forEach(products => {
      allProducts.push(...products)
    })
    return allProducts.slice(0, 16)
  }
  return allMatchedProducts.value.get(selectedCategoryId.value) || []
})

const validateFile = (file: File): boolean => {
  errorMessage.value = ''

  if (!ALLOWED_TYPES.includes(file.type)) {
    errorMessage.value = '仅支持 JPG、PNG、WEBP 格式的图片'
    ElMessage.error(errorMessage.value)
    return false
  }

  if (file.size > MAX_FILE_SIZE) {
    errorMessage.value = '图片大小不能超过 5MB'
    ElMessage.error(errorMessage.value)
    return false
  }

  return true
}

const processFile = async (file: File) => {
  if (!validateFile(file)) return

  uploadProgress.value = 0

  const reader = new FileReader()

  reader.onprogress = (e) => {
    if (e.lengthComputable) {
      uploadProgress.value = Math.round((e.loaded / e.total) * 50)
    }
  }

  reader.onload = async (e) => {
    uploadedImage.value = e.target?.result as string
    uploadProgress.value = 50

    loading.value = true
    searchResult.value = null
    allMatchedProducts.value.clear()
    selectedCategoryId.value = null
    errorMessage.value = ''

    try {
      const result = await yoloSearch(file)
      uploadProgress.value = 80
      searchResult.value = result

      if (result.success && result.matchedCategories && result.matchedCategories.length > 0) {
        const categoryIds = result.matchedCategories.map(c => c.id)
        await Promise.all(categoryIds.map(cid => fetchMatchedProducts(cid)))
        uploadProgress.value = 100
        ElMessage.success(`识别成功！发现${result.matchedCategories.length}种物品`)
      } else if (result.hotCategories && result.hotCategories.length > 0) {
        uploadProgress.value = 100
        ElMessage.warning(result.message || '未识别到匹配的商品分类')
      } else {
        uploadProgress.value = 100
        errorMessage.value = result.message || '未识别到相关物品，请尝试其他图片'
        ElMessage.warning(errorMessage.value)
      }
    } catch (error: any) {
      console.error('AI识别失败', error)
      errorMessage.value = error?.response?.data?.msg || '识别失败，请重试'
      ElMessage.error(errorMessage.value)
    } finally {
      loading.value = false
      setTimeout(() => {
        uploadProgress.value = 0
      }, 1000)
    }
  }

  reader.onerror = () => {
    errorMessage.value = '图片读取失败，请重试'
    ElMessage.error(errorMessage.value)
    loading.value = false
  }

  reader.readAsDataURL(file)
}

const handleFileChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (file) {
    processFile(file)
  }
  target.value = ''
}

const handleDrop = (e: DragEvent) => {
  e.preventDefault()
  const files = e.dataTransfer?.files
  if (files && files.length > 0) {
    processFile(files[0])
  }
}

const handleDragOver = (e: DragEvent) => {
  e.preventDefault()
}

const fetchMatchedProducts = async (categoryId: number) => {
  try {
    const res = await getProductList({
      categoryId,
      pageNum: 1,
      pageSize: 8
    })
    allMatchedProducts.value.set(categoryId, res.records)
  } catch (error) {
    console.error('获取商品失败', error)
  }
}

const handleCategoryClick = (category: YoloCategoryInfo) => {
  if (selectedCategoryId.value === category.id) {
    selectedCategoryId.value = null
  } else {
    selectedCategoryId.value = category.id
  }
}

const clearCategoryFilter = () => {
  selectedCategoryId.value = null
}

const goToProduct = (productId: number) => {
  router.push(`/product/${productId}`)
}

const goToCategory = (categoryId: number) => {
  router.push({ path: '/products', query: { categoryId: String(categoryId) } })
}

const reset = () => {
  uploadedImage.value = ''
  searchResult.value = null
  allMatchedProducts.value.clear()
  selectedCategoryId.value = null
  errorMessage.value = ''
  uploadProgress.value = 0
}

const triggerUpload = () => {
  fileInputRef.value?.click()
}
</script>

<template>
  <UserLayout>
    <div class="ai-search-page">
      <div class="page-container">
        <div class="page-header">
          <h1 class="page-title">
            <el-icon class="title-icon"><Camera /></el-icon>
            AI智能识图搜索
          </h1>
          <p class="page-subtitle">上传图片，AI自动识别物品并推荐相关商品</p>
        </div>

        <div class="main-content">
          <div class="upload-section">
            <div class="upload-card">
              <div class="upload-area-wrapper">
                <div 
                  v-if="!uploadedImage"
                  class="upload-area"
                  :class="{ 'drag-over': false }"
                  @click="triggerUpload"
                  @drop="handleDrop"
                  @dragover="handleDragOver"
                >
                  <el-icon class="upload-icon"><Upload /></el-icon>
                  <p class="upload-text">点击或拖拽上传图片</p>
                  <p class="upload-hint">支持 JPG、PNG、WEBP 格式，每次仅限1张图片</p>
                  <p class="upload-size-hint">文件大小不超过 5MB</p>
                </div>
                
                <div v-else class="image-preview">
                  <img :src="uploadedImage" alt="上传的图片" />
                  <el-button 
                    type="danger" 
                    circle
                    class="remove-btn"
                    @click.stop="reset"
                  >
                    <el-icon><Close /></el-icon>
                  </el-button>
                </div>
              </div>

              <div v-if="uploadProgress > 0 && uploadProgress < 100" class="progress-bar">
                <el-progress 
                  :percentage="uploadProgress" 
                  :stroke-width="8"
                  :show-text="true"
                />
                <p class="progress-text">
                  {{ uploadProgress < 50 ? '正在上传图片...' : '正在AI识别...' }}
                </p>
              </div>

              <div v-if="errorMessage" class="error-message">
                <el-icon><WarningFilled /></el-icon>
                <span>{{ errorMessage }}</span>
              </div>

              <div class="upload-actions">
                <input
                  ref="fileInputRef"
                  type="file"
                  accept="image/jpeg,image/png,image/jpg,image/webp"
                  style="display: none"
                  @change="handleFileChange"
                />
                
                <el-button 
                  type="primary" 
                  size="large"
                  class="upload-btn"
                  :loading="loading"
                  @click="triggerUpload"
                >
                  <el-icon class="btn-icon">
                    <component :is="uploadedImage ? Refresh : Upload" />
                  </el-icon>
                  {{ uploadedImage ? '重新上传' : '选择图片' }}
                </el-button>
              </div>
            </div>

            <div class="result-section">
              <h3 class="result-title">
                <el-icon class="result-icon"><MagicStick /></el-icon>
                识别结果
              </h3>
              
              <div v-if="loading" class="loading-state">
                <el-icon class="loading-icon"><Loading /></el-icon>
                <p>AI正在识别中...</p>
                <p class="loading-hint">请稍候，这可能需要几秒钟</p>
              </div>

              <div v-else-if="searchResult && matchedCategories.length > 0" class="result-list">
                <p class="result-hint">
                  <el-icon><MagicStick /></el-icon>
                  识别到 {{ matchedCategories.length }} 种物品，点击可筛选对应商品
                </p>
                <div
                  v-for="(category, index) in matchedCategories"
                  :key="index"
                  class="result-item"
                  :class="{ active: selectedCategoryId === category.id }"
                  @click="handleCategoryClick(category)"
                >
                  <div class="result-header">
                    <span class="result-label">{{ category.name }}</span>
                    <el-tag type="success" size="small">
                      {{ category.productCount }}件商品
                    </el-tag>
                  </div>
                </div>

                <div class="result-actions">
                  <el-button
                    v-if="selectedCategoryId !== null"
                    type="info"
                    plain
                    class="clear-btn"
                    @click="clearCategoryFilter"
                  >
                    显示全部商品
                  </el-button>
                  <el-button
                    v-if="selectedCategoryId !== null"
                    type="primary"
                    class="view-all-btn"
                    @click="goToCategory(selectedCategoryId!)"
                  >
                    查看该分类全部商品
                  </el-button>
                </div>
              </div>

              <div v-else-if="errorMessage && uploadedImage" class="empty-state error">
                <el-icon class="empty-icon"><WarningFilled /></el-icon>
                <p>{{ errorMessage }}</p>
                <el-button type="primary" plain @click="reset">重新上传</el-button>
              </div>
              <div v-else-if="searchResult && matchedCategories.length === 0 && searchResult.hotCategories && searchResult.hotCategories.length > 0" class="empty-state error">
                <el-icon class="empty-icon"><WarningFilled /></el-icon>
                <p>{{ searchResult.message || '未识别到匹配的商品' }}</p>
                <el-button type="primary" plain @click="reset">重新上传</el-button>
              </div>
              <div v-else class="empty-state">
                <el-empty description="上传图片开始识别" :image-size="80" />
              </div>
            </div>
          </div>

          <div v-if="currentProducts.length > 0" class="products-section">
            <div class="products-header">
              <h3 class="products-title">
                <el-icon class="products-icon"><Goods /></el-icon>
                相关商品推荐
                <el-tag v-if="selectedCategoryId !== null" size="small" type="primary" class="filter-tag">
                  已筛选：{{ matchedCategories.find(c => c.id === selectedCategoryId)?.name }}
                </el-tag>
              </h3>
              <el-button
                v-if="selectedCategoryId !== null"
                type="primary"
                link
                @click="goToCategory(selectedCategoryId!)"
              >
                查看更多 &gt;
              </el-button>
            </div>
            
            <div class="products-grid">
              <div 
                v-for="product in currentProducts" 
                :key="product.id"
                class="product-card"
                @click="goToProduct(product.id)"
              >
                <div class="product-image">
                  <img :src="product.mainImage" :alt="product.name" />
                </div>
                <div class="product-info">
                  <h4 class="product-name">{{ product.name }}</h4>
                  <div class="product-price">
                    <span class="price-current">¥{{ product.price.toFixed(2) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="guide-section">
            <h3 class="guide-title">使用说明</h3>
            <div class="guide-grid">
              <div class="guide-item">
                <div class="guide-icon">
                  <el-icon><Upload /></el-icon>
                </div>
                <h4 class="guide-step">1. 上传图片</h4>
                <p class="guide-desc">选择包含您想要搜索物品的图片（仅限1张）</p>
              </div>
              <div class="guide-item">
                <div class="guide-icon">
                  <el-icon><MagicStick /></el-icon>
                </div>
                <h4 class="guide-step">2. AI识别</h4>
                <p class="guide-desc">基于YOLO算法智能识别图片中的所有物品</p>
              </div>
              <div class="guide-item">
                <div class="guide-icon">
                  <el-icon><Goods /></el-icon>
                </div>
                <h4 class="guide-step">3. 筛选购买</h4>
                <p class="guide-desc">点击识别结果筛选商品，选择心仪商品购买</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </UserLayout>
</template>

<style scoped>
.ai-search-page {
  background: linear-gradient(135deg, #f7f8fa 0%, #e6f4ff 50%, #f0f5ff 100%);
  min-height: 100vh;
  padding: 32px 0;
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
  animation: fadeIn 0.6s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 12px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.title-icon {
  font-size: 36px;
  color: var(--primary-color);
}

.page-subtitle {
  font-size: 16px;
  color: var(--text-secondary);
  margin: 0;
}

.main-content {
  animation: slideUp 0.6s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.upload-section {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
  margin-bottom: 32px;
}

.upload-card {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  box-shadow: var(--shadow-md);
}

.upload-area-wrapper {
  min-height: 300px;
}

.upload-area {
  height: 100%;
  min-height: 300px;
  border: 2px dashed var(--border-color);
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.upload-area:hover {
  border-color: var(--primary-color);
  background: var(--primary-50);
}

.upload-icon {
  font-size: 64px;
  color: var(--text-secondary);
  margin-bottom: 16px;
}

.upload-text {
  font-size: 18px;
  color: var(--text-primary);
  margin: 0 0 8px 0;
}

.upload-hint {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0 0 4px 0;
}

.upload-size-hint {
  font-size: 12px;
  color: var(--text-placeholder);
  margin: 0;
}

.image-preview {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  min-height: 300px;
}

.image-preview img {
  width: 100%;
  height: 100%;
  min-height: 300px;
  object-fit: contain;
  background: #f7f8fa;
}

.remove-btn {
  position: absolute;
  top: 12px;
  right: 12px;
}

.progress-bar {
  margin-top: 20px;
  padding: 16px;
  background: var(--primary-50);
  border-radius: 12px;
}

.progress-text {
  text-align: center;
  font-size: 13px;
  color: var(--primary-color);
  margin: 8px 0 0 0;
}

.error-message {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 16px;
  padding: 12px 16px;
  background: #fff1f0;
  border-radius: 10px;
  color: #ff4d4f;
  font-size: 14px;
}

.upload-actions {
  margin-top: 20px;
  text-align: center;
}

.upload-btn {
  min-width: 160px;
  height: 44px;
  border-radius: 12px;
  font-weight: 600;
}

.btn-icon {
  margin-right: 8px;
}

.result-section {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  box-shadow: var(--shadow-md);
}

.result-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 20px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.result-icon {
  color: var(--primary-color);
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
}

.loading-icon {
  font-size: 48px;
  color: var(--primary-color);
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.loading-state p {
  color: var(--text-secondary);
  margin: 0;
}

.loading-hint {
  font-size: 12px;
  color: var(--text-placeholder);
  margin-top: 8px !important;
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.result-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--primary-color);
  background: var(--primary-50);
  padding: 10px 14px;
  border-radius: 10px;
  margin: 0 0 8px 0;
}

.result-item {
  padding: 14px 16px;
  border: 2px solid var(--border-light);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.result-item:hover {
  border-color: var(--primary-color);
  background: var(--primary-50);
}

.result-item.active {
  border-color: var(--primary-color);
  background: var(--primary-50);
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.result-label {
  font-weight: 600;
  color: var(--text-primary);
}

.result-category {
  font-size: 13px;
  color: var(--text-secondary);
}

.category-name {
  color: var(--primary-color);
  font-weight: 500;
}

.result-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 16px;
}

.clear-btn {
  width: 100%;
}

.view-all-btn {
  width: 100%;
}

.empty-state {
  padding: 40px 0;
}

.empty-state.error {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.empty-state.error .empty-icon {
  font-size: 48px;
  color: #ff4d4f;
}

.empty-state.error p {
  color: var(--text-secondary);
  margin: 0;
}

.products-section {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  box-shadow: var(--shadow-md);
  margin-bottom: 32px;
}

.products-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.products-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.products-icon {
  color: var(--primary-color);
}

.filter-tag {
  margin-left: 12px;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.product-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: var(--shadow-sm);
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.product-image {
  aspect-ratio: 1;
  overflow: hidden;
  background: #f7f8fa;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.product-card:hover .product-image img {
  transform: scale(1.05);
}

.product-info {
  padding: 14px;
}

.product-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  margin: 0 0 10px 0;
  line-height: 1.4;
  height: 40px;
  overflow: hidden;
  display: -webkit-box;
  line-clamp: 2;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-price {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.price-current {
  font-size: 18px;
  font-weight: 700;
  color: #ff4d4f;
}

.guide-section {
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
  border-radius: 20px;
  padding: 32px;
}

.guide-title {
  font-size: 20px;
  font-weight: 600;
  color: #fff;
  text-align: center;
  margin: 0 0 32px 0;
}

.guide-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 32px;
}

.guide-item {
  text-align: center;
}

.guide-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: #fff;
}

.guide-step {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  margin: 0 0 8px 0;
}

.guide-desc {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
}

@media (max-width: 1024px) {
  .upload-section {
    grid-template-columns: 1fr;
  }
  
  .products-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .ai-search-page {
    padding: 16px 0;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .products-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  
  .guide-grid {
    grid-template-columns: 1fr;
    gap: 24px;
  }
}
</style>
