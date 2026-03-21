<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Camera, Upload, Refresh, MagicStick, Goods, Close, Loading } from '@element-plus/icons-vue'
import UserLayout from '@/components/layout/UserLayout.vue'
import { yoloSearch, getProductList } from '@/api/product'
import type { YoloSearchResult, Product } from '@/types/api'

const router = useRouter()

const loading = ref(false)
const uploadedImage = ref('')
const uploadRef = ref()
const searchResults = ref<YoloSearchResult[]>([])
const matchedProducts = ref<Product[]>([])
const selectedCategory = ref<number | null>(null)

const handleUpload = async (options: any) => {
  const { file } = options
  if (!file) return

  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('请上传图片文件')
    return
  }

  const reader = new FileReader()
  reader.onload = async (e) => {
    uploadedImage.value = e.target?.result as string
  }
  reader.readAsDataURL(file)

  loading.value = true
  searchResults.value = []
  matchedProducts.value = []
  selectedCategory.value = null

  try {
    const results = await yoloSearch(file)
    searchResults.value = results
    
    if (results.length > 0) {
      const firstResult = results[0]
      selectedCategory.value = firstResult.categoryId
      await fetchMatchedProducts(firstResult.categoryId)
    }
  } catch (error) {
    console.error('AI识别失败', error)
    ElMessage.error('识别失败，请重试')
  } finally {
    loading.value = false
  }
}

const fetchMatchedProducts = async (categoryId: number) => {
  try {
    const res = await getProductList({
      categoryId,
      pageNum: 1,
      pageSize: 8
    })
    matchedProducts.value = res.records
  } catch (error) {
    console.error('获取商品失败', error)
  }
}

const handleCategoryClick = (result: YoloSearchResult) => {
  selectedCategory.value = result.categoryId
  fetchMatchedProducts(result.categoryId)
}

const goToProduct = (productId: number) => {
  router.push(`/product/${productId}`)
}

const goToCategory = (categoryId: number) => {
  router.push({ path: '/products', query: { categoryId } })
}

const reset = () => {
  uploadedImage.value = ''
  searchResults.value = []
  matchedProducts.value = []
  selectedCategory.value = null
}

const triggerUpload = () => {
  const el = uploadRef.value?.$el as HTMLElement | undefined
  el?.querySelector('input')?.click()
}
</script>

<template>
  <UserLayout>
    <div class="page-wrapper py-lg">
      <div class="page-container">
        <div class="section-header animate-fade-in">
          <h1 class="section-title text-3xl mb-md">
            <el-icon class="text-primary-color"><Camera /></el-icon>
            AI智能识图搜索
          </h1>
          <p class="section-subtitle">上传图片，AI自动识别物品并推荐相关商品</p>
        </div>

        <div class="max-w-4xl mx-auto">
          <div class="card p-xl mb-xl animate-slide-up">
            <div class="flex flex-col lg:flex-row gap-xl">
              <div class="flex-1">
                <div 
                  v-if="!uploadedImage"
                  class="upload-area border-2 border-dashed border-color rounded-md p-xl text-center cursor-pointer transition-colors hover:border-primary-color hover:bg-primary-opacity-10"
                  @click="triggerUpload"
                >
                  <el-icon :size="64" class="text-secondary mb-md"><Upload /></el-icon>
                  <p class="text-regular mb-sm">点击或拖拽上传图片</p>
                  <p class="text-secondary text-sm">支持 JPG、PNG 格式</p>
                </div>
                
                <div v-else class="image-preview relative rounded-md overflow-hidden">
                  <img 
                    :src="uploadedImage" 
                    alt="上传的图片"
                    class="w-full rounded-md"
                  />
                  <el-button 
                    type="danger" 
                    circle
                    class="absolute top-sm right-sm"
                    @click.stop="reset"
                  >
                    <el-icon><Close /></el-icon>
                  </el-button>
                </div>

                <div class="mt-lg flex justify-center">
                  <el-upload
                    ref="uploadRef"
                    :show-file-list="false"
                    :before-upload="() => false"
                    :on-change="handleUpload"
                    accept="image/*"
                    class="hidden"
                  />
                  
                  <el-button 
                    type="primary" 
                    size="large"
                    class="btn-lg"
                    :loading="loading"
                    @click="triggerUpload"
                  >
                    <el-icon class="mr-xs">
                      <component :is="uploadedImage ? Refresh : Upload" />
                    </el-icon>
                    {{ uploadedImage ? '重新上传' : '选择图片' }}
                  </el-button>
                </div>
              </div>

              <div class="w-full lg:w-80">
                <h3 class="font-bold text-lg mb-md flex items-center gap-sm">
                  <el-icon class="text-primary-color"><MagicStick /></el-icon>
                  识别结果
                </h3>
                
                <div v-if="loading" class="flex-center h-64">
                  <div class="text-center">
                    <el-icon class="text-4xl text-primary-color animate-spin mb-md"><Loading /></el-icon>
                    <p class="text-secondary">AI正在识别中...</p>
                  </div>
                </div>

                <div v-else-if="searchResults.length > 0" class="space-y-sm">
                  <div 
                    v-for="(result, index) in searchResults" 
                    :key="index"
                    class="p-md rounded-sm border transition-all cursor-pointer"
                    :class="selectedCategory === result.categoryId ? 'border-primary-color bg-primary-opacity-10' : 'border-border-color hover:border-primary-color'"
                    @click="handleCategoryClick(result)"
                  >
                    <div class="flex-between items-center mb-xs">
                      <span class="font-medium">{{ result.label }}</span>
                      <el-tag type="success" size="small">
                        {{ (result.confidence * 100).toFixed(1) }}%
                      </el-tag>
                    </div>
                    <div class="text-sm text-secondary">
                      匹配分类：<span class="text-primary-color">{{ result.categoryName }}</span>
                    </div>
                  </div>
                  
                  <el-button 
                    type="primary" 
                    class="w-full mt-md"
                    @click="goToCategory(searchResults[0].categoryId)"
                  >
                    查看该分类全部商品
                  </el-button>
                </div>

                <div v-else-if="uploadedImage && !loading" class="text-center py-xl">
                  <el-empty description="未识别到相关物品" :image-size="80" />
                </div>
                <div v-else class="text-center py-xl">
                  <el-empty description="上传图片开始识别" :image-size="80" />
                </div>
              </div>
            </div>
          </div>

          <div v-if="matchedProducts.length > 0" class="card p-xl animate-slide-up">
            <div class="flex-between items-center mb-lg">
              <h3 class="font-bold text-xl flex items-center gap-sm">
                <el-icon class="text-primary-color"><Goods /></el-icon>
                相关商品推荐
              </h3>
              <el-button 
                type="primary" 
                link
                @click="goToCategory(selectedCategory!)"
              >
                查看更多 &gt;
              </el-button>
            </div>
            
            <div class="grid grid-cols-2 md:grid-cols-4 gap-md">
              <div 
                v-for="product in matchedProducts" 
                :key="product.id"
                class="product-card card overflow-hidden cursor-pointer"
                @click="goToProduct(product.id)"
              >
                <div class="aspect-square overflow-hidden bg-secondary">
                  <img 
                    :src="product.mainImage" 
                    :alt="product.name"
                    class="w-full h-full object-cover hover-scale-lg transition-transform duration-300"
                  />
                </div>
                <div class="p-sm">
                  <h4 class="text-sm font-medium line-clamp-2 h-10 mb-sm">{{ product.name }}</h4>
                  <div class="flex items-baseline gap-xs">
                    <span class="text-danger font-bold">¥{{ product.price.toFixed(2) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="mt-xl gradient-bg rounded-lg p-xl animate-slide-up">
            <h3 class="font-bold text-xl mb-lg text-center">使用说明</h3>
            <div class="grid grid-cols-1 md:grid-cols-3 gap-lg">
              <div class="text-center">
                <div class="w-16 h-16 mx-auto mb-md rounded-full bg-primary-opacity-10 flex-center">
                  <el-icon :size="32" class="text-primary-color"><Upload /></el-icon>
                </div>
                <h4 class="font-medium mb-sm">1. 上传图片</h4>
                <p class="text-sm text-secondary">选择包含您想要搜索物品的图片</p>
              </div>
              <div class="text-center">
                <div class="w-16 h-16 mx-auto mb-md rounded-full bg-primary-opacity-10 flex-center">
                  <el-icon :size="32" class="text-primary-color"><MagicStick /></el-icon>
                </div>
                <h4 class="font-medium mb-sm">2. AI识别</h4>
                <p class="text-sm text-secondary">基于YOLO算法智能识别图片中的物品</p>
              </div>
              <div class="text-center">
                <div class="w-16 h-16 mx-auto mb-md rounded-full bg-primary-opacity-10 flex-center">
                  <el-icon :size="32" class="text-primary-color"><Goods /></el-icon>
                </div>
                <h4 class="font-medium mb-sm">3. 商品推荐</h4>
                <p class="text-sm text-secondary">自动匹配相关分类并推荐商品</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </UserLayout>
</template>

<style scoped>
.upload-area {
  transition: all 0.3s ease;
}

.upload-area:hover {
  background: var(--primary-opacity-10);
}

.product-card {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-hover);
}
</style>
