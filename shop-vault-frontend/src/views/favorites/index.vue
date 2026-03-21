<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import UserLayout from '@/components/layout/UserLayout.vue'
import { getFavoriteList, toggleFavorite } from '@/api/product'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Favorite } from '@/types/api'

const router = useRouter()

const favorites = ref<Favorite[]>([])
const loading = ref(false)
const selectedIds = ref<number[]>([])

const fetchFavorites = async () => {
  try {
    loading.value = true
    favorites.value = await getFavoriteList()
  } catch (error) {
    console.error('获取收藏列表失败', error)
  } finally {
    loading.value = false
  }
}

const handleRemoveFavorite = async (favorite: Favorite) => {
  try {
    await ElMessageBox.confirm('确定要取消收藏该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await toggleFavorite(favorite.productId)
    favorites.value = favorites.value.filter(f => f.id !== favorite.id)
    selectedIds.value = selectedIds.value.filter(id => id !== favorite.id)
    ElMessage.success('已取消收藏')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消收藏失败', error)
    }
  }
}

const handleBatchRemove = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要取消收藏的商品')
    return
  }

  try {
    await ElMessageBox.confirm(`确定要取消收藏选中的 ${selectedIds.value.length} 个商品吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const selectedFavorites = favorites.value.filter(f => selectedIds.value.includes(f.id))
    await Promise.all(selectedFavorites.map(f => toggleFavorite(f.productId)))
    
    favorites.value = favorites.value.filter(f => !selectedIds.value.includes(f.id))
    selectedIds.value = []
    ElMessage.success('批量取消收藏成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量取消收藏失败', error)
    }
  }
}

const handleSelectAll = (val: string | number | boolean) => {
  if (val) {
    selectedIds.value = favorites.value.map(f => f.id)
  } else {
    selectedIds.value = []
  }
}

const goToProduct = (productId: number) => {
  router.push(`/product/${productId}`)
}

const formatDate = (date: string) => {
  return date ? date.split('T')[0] : ''
}

onMounted(() => {
  fetchFavorites()
})
</script>

<template>
  <UserLayout>
    <div class="favorites-page">
      <div class="page-container">
        <div class="favorites-card">
          <div class="card-header">
            <div>
              <h1 class="page-title">我的收藏</h1>
              <p class="page-subtitle">共 {{ favorites.length }} 件商品</p>
            </div>
            <el-button 
              v-if="selectedIds.length > 0"
              type="danger" 
              plain
              @click="handleBatchRemove"
              class="batch-btn"
            >
              取消收藏 ({{ selectedIds.length }})
            </el-button>
          </div>

          <div v-if="favorites.length > 0" class="select-bar">
            <el-checkbox 
              :model-value="selectedIds.length === favorites.length"
              :indeterminate="selectedIds.length > 0 && selectedIds.length < favorites.length"
              @change="handleSelectAll"
            >
              全选
            </el-checkbox>
          </div>

          <div v-loading="loading" class="favorites-content">
            <div class="favorites-grid">
              <div 
                v-for="favorite in favorites" 
                :key="favorite.id"
                class="favorite-item"
              >
                <div class="item-wrapper">
                  <div class="item-header">
                    <el-checkbox 
                      :model-value="selectedIds.includes(favorite.id)"
                      :value="favorite.id"
                      class="select-checkbox"
                      @change="() => {
                        const index = selectedIds.indexOf(favorite.id)
                        if (index > -1) {
                          selectedIds.splice(index, 1)
                        } else {
                          selectedIds.push(favorite.id)
                        }
                      }"
                    />
                    <div class="delete-btn-wrapper">
                      <el-button 
                        type="danger" 
                        size="small" 
                        circle
                        @click.stop="handleRemoveFavorite(favorite)"
                        class="delete-btn"
                      >
                        <el-icon><Delete /></el-icon>
                      </el-button>
                    </div>
                  </div>
                  <img 
                    :src="favorite.productImage || '/placeholder.png'" 
                    :alt="favorite.productName"
                    class="product-image"
                    @click="goToProduct(favorite.productId)"
                  />
                  
                  <div class="item-body">
                    <h3 
                      class="product-name"
                      @click="goToProduct(favorite.productId)"
                    >
                      {{ favorite.productName }}
                    </h3>
                    
                    <div class="price-row">
                      <span class="price">¥{{ favorite.price }}</span>
                    </div>
                    
                    <div class="date-info">
                      收藏于 {{ formatDate(favorite.createTime) }}
                    </div>
                    
                    <el-button 
                      type="primary" 
                      size="small" 
                      class="view-btn"
                      @click="goToProduct(favorite.productId)"
                    >
                      查看详情
                    </el-button>
                  </div>
                </div>
              </div>
            </div>

            <el-empty 
              v-if="!loading && favorites.length === 0" 
              description="暂无收藏商品"
              class="empty-state"
            >
              <el-button type="primary" @click="$router.push('/products')" class="empty-btn">
                去逛逛
              </el-button>
            </el-empty>
          </div>
        </div>
      </div>
    </div>
  </UserLayout>
</template>

<style scoped>
.favorites-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f0f5ff 0%, #e6f7ff 50%, #f5f7fa 100%);
  padding: 24px 0;
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.favorites-card {
  background: #ffffff;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

.card-header {
  padding: 24px 32px;
  border-bottom: 1px solid #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 4px 0;
}

.page-subtitle {
  color: #6b7280;
  margin: 0;
  font-size: 14px;
}

.batch-btn {
  border-radius: var(--radius-sm);
  transition: all 0.3s ease;
}

.batch-btn:hover {
  transform: translateY(-2px);
}

.select-bar {
  padding: 16px 32px;
  background: #f9fafb;
  border-bottom: 1px solid #f3f4f6;
}

.favorites-content {
  padding: 32px;
}

.favorites-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.favorite-item {
  transition: all 0.3s ease;
}

.item-wrapper {
  border: 1px solid #e5e7eb;
  border-radius: var(--radius-md);
  overflow: hidden;
  background: #ffffff;
  transition: all 0.3s ease;
}

.item-wrapper:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-4px);
  border-color: var(--primary-opacity-10);
}

.item-header {
  position: relative;
  padding: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.select-checkbox {
  z-index: 10;
}

.delete-btn-wrapper {
  opacity: 0;
  transition: opacity 0.3s ease;
}

.item-wrapper:hover .delete-btn-wrapper {
  opacity: 1;
}

.delete-btn {
  transition: all 0.3s ease;
}

.delete-btn:hover {
  transform: scale(1.1);
}

.product-image {
  width: 100%;
  height: 220px;
  object-fit: cover;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.product-image:hover {
  transform: scale(1.05);
}

.item-body {
  padding: 16px;
}

.product-name {
  font-size: 15px;
  font-weight: 500;
  color: #1f2937;
  margin: 0 0 12px 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  cursor: pointer;
  transition: color 0.3s ease;
}

.product-name:hover {
  color: var(--primary-color);
}

.price-row {
  margin-bottom: 8px;
}

.price {
  font-size: 22px;
  font-weight: 700;
  color: #ef4444;
}

.date-info {
  font-size: 13px;
  color: #9ca3af;
  margin-bottom: 16px;
}

.view-btn {
  width: 100%;
  height: 40px;
  font-weight: 600;
  border-radius: var(--radius-sm);
  transition: all 0.3s ease;
}

.view-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(22, 119, 255, 0.3);
}

.empty-state {
  padding: 80px 0;
}

.empty-btn {
  height: 44px;
  padding: 0 32px;
  font-weight: 600;
  border-radius: var(--radius-sm);
  transition: all 0.3s ease;
}

.empty-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(22, 119, 255, 0.3);
}

@media (max-width: 1024px) {
  .favorites-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
  }
}

@media (max-width: 768px) {
  .favorites-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }
  
  .favorites-page {
    padding: 12px 0;
  }
  
  .card-header {
    padding: 20px;
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .batch-btn {
    width: 100%;
  }
  
  .select-bar {
    padding: 12px 20px;
  }
  
  .favorites-content {
    padding: 20px;
  }
  
  .product-image {
    height: 180px;
  }
}

@media (max-width: 480px) {
  .favorites-grid {
    grid-template-columns: 1fr;
  }
  
  .product-image {
    height: 240px;
  }
}
</style>
