<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ShoppingCart, Delete } from '@element-plus/icons-vue'
import UserLayout from '@/components/layout/UserLayout.vue'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const selectedItems = computed(() => cartStore.selectedItems)
const selectedAmount = computed(() => cartStore.selectedAmount)
const selectedCount = computed(() => cartStore.selectedCount)

const handleSelectAll = (val: string | number | boolean) => {
  cartStore.isAllSelected = !!val
}

const handleQuantityChange = async (id: number, val: number | undefined) => {
  if (!val || val < 1) return
  await cartStore.updateQuantity(id, val)
}

const handleDelete = async (ids: number[]) => {
  try {
    await ElMessageBox.confirm('确定要删除选中的商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await cartStore.removeItems(ids)
  } catch {
    // 用户取消
  }
}

const handleDeleteSelected = async () => {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请选择要删除的商品')
    return
  }
  await handleDelete(selectedItems.value.map(item => item.id))
}

const handleCheckout = async () => {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请选择要结算的商品')
    return
  }

  const itemCount = selectedItems.value.reduce((sum, item) => sum + item.quantity, 0)

  try {
    await ElMessageBox.confirm(
      `已选 ${selectedItems.value.length} 件商品，共 ${itemCount} 件\n应付金额：¥${selectedAmount.value.toFixed(2)}`,
      '确认结算',
      {
        confirmButtonText: '去结算',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
  } catch {
    return
  }

  const ids = selectedItems.value.map(item => item.id).join(',')
  router.push(`/checkout?cartIds=${ids}`)
}

onMounted(() => {
  if (userStore.token) {
    cartStore.fetchCartList()
  }
})
</script>

<template>
  <UserLayout>
    <div class="cart-container animate-fade-in">
      <div class="page-container py-8">
        <div class="cart-card">
          <div class="cart-header">
            <div class="cart-title-section">
              <h1 class="cart-title">
                <el-icon class="title-icon"><ShoppingCart /></el-icon>
                我的购物车
                <span class="cart-count">({{ cartStore.totalCount }}件)</span>
              </h1>
            </div>
            <el-button 
              v-if="cartStore.cartItems.length > 0"
              type="danger" 
              plain 
              class="delete-btn"
              @click="handleDeleteSelected"
            >
              <el-icon><Delete /></el-icon>
              删除选中
            </el-button>
          </div>

          <div v-loading="cartStore.loading" class="cart-content">
            <template v-if="cartStore.cartItems.length > 0">
              <div class="cart-table-header">
                <div class="header-row">
                  <el-checkbox 
                    :model-value="cartStore.isAllSelected"
                    @change="handleSelectAll"
                    class="header-checkbox"
                  >
                    全选
                  </el-checkbox>
                  <span class="header-item header-info">商品信息</span>
                  <span class="header-item header-price">单价</span>
                  <span class="header-item header-quantity">数量</span>
                  <span class="header-item header-subtotal">小计</span>
                  <span class="header-item header-action">操作</span>
                </div>
              </div>

              <div class="cart-items">
                <div 
                  v-for="item in cartStore.cartItems" 
                  :key="item.id"
                  class="cart-item"
                >
                  <el-checkbox 
                    :model-value="item.selected"
                    @change="cartStore.toggleSelect(item.id)"
                    class="item-checkbox"
                  />
                  
                  <div class="item-info">
                    <router-link :to="`/product/${item.productId}`" class="item-image-link">
                      <img 
                        :src="item.productImage" 
                        :alt="item.productName"
                        class="item-image"
                      />
                    </router-link>
                    <div class="item-details">
                      <router-link 
                        :to="`/product/${item.productId}`"
                        class="item-name"
                      >
                        {{ item.productName }}
                      </router-link>
                    </div>
                  </div>

                  <div class="item-price">
                    <span class="price-text">¥{{ item.price.toFixed(2) }}</span>
                  </div>

                  <div class="item-quantity">
                    <el-input-number 
                      :model-value="item.quantity"
                      :min="1"
                      :max="item.stock"
                      size="default"
                      class="quantity-input"
                      @change="(val: number | undefined) => handleQuantityChange(item.id, val)"
                    />
                  </div>

                  <div class="item-subtotal">
                    <span class="subtotal-text">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
                  </div>

                  <div class="item-action">
                    <el-button 
                      type="danger" 
                      link 
                      class="delete-item-btn"
                      @click="handleDelete([item.id])"
                    >
                      删除
                    </el-button>
                  </div>
                </div>
              </div>

              <div class="cart-footer">
                <div class="footer-left">
                  <el-checkbox 
                    :model-value="cartStore.isAllSelected"
                    @change="handleSelectAll"
                    class="footer-checkbox"
                  >
                    全选
                  </el-checkbox>
                  <el-button 
                    type="danger" 
                    plain 
                    class="footer-delete-btn"
                    @click="handleDeleteSelected"
                  >
                    删除选中
                  </el-button>
                </div>
                
                <div class="footer-right">
                  <div class="selected-info">
                    已选择 <span class="selected-count">{{ selectedCount }}</span> 件商品
                  </div>
                  <div class="total-section">
                    <span class="total-label">合计：</span>
                    <span class="total-amount">¥{{ selectedAmount.toFixed(2) }}</span>
                  </div>
                  <el-button 
                    type="primary" 
                    class="checkout-btn"
                    size="large"
                    :disabled="selectedCount === 0"
                    @click="handleCheckout"
                  >
                    去结算
                  </el-button>
                </div>
              </div>
            </template>

            <div v-else class="empty-cart">
              <el-empty description="购物车是空的">
                <el-button type="primary" @click="router.push('/products')" class="go-shopping-btn">
                  去购物
                </el-button>
              </el-empty>
            </div>
          </div>
        </div>
      </div>
    </div>
  </UserLayout>
</template>

<style scoped>
.cart-container {
  background: linear-gradient(180deg, #f7f8fa 0%, #ffffff 100%);
  min-height: calc(100vh - 140px);
}

.cart-card {
  background: var(--card-bg);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
  overflow: hidden;
}

.cart-header {
  padding: 24px 32px;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, rgba(22, 119, 255, 0.03) 0%, rgba(64, 150, 255, 0.03) 100%);
}

.cart-title-section {
  display: flex;
  align-items: center;
}

.cart-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0;
}

.title-icon {
  color: var(--primary-color);
  font-size: 18px;
  width: 24px;
  height: 24px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cart-count {
  font-size: 16px;
  font-weight: 400;
  color: var(--text-secondary);
}

.delete-btn {
  border-radius: var(--radius-sm);
  font-weight: 500;
  transition: all 0.2s ease;
}

.delete-btn:hover {
  transform: translateY(-1px);
}

.cart-content {
  padding: 0;
}

.cart-table-header {
  background: var(--bg-color);
  padding: 16px 32px;
  border-bottom: 1px solid var(--border-color);
}

.header-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-checkbox {
  margin-right: 0;
}

.header-item {
  color: var(--text-secondary);
  font-weight: 500;
  font-size: 14px;
}

.header-info {
  flex: 1;
}

.header-price {
  width: 120px;
  text-align: center;
}

.header-quantity {
  width: 160px;
  text-align: center;
}

.header-subtotal {
  width: 120px;
  text-align: center;
}

.header-action {
  width: 100px;
  text-align: center;
}

.cart-items {
  padding: 0;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px 32px;
  border-bottom: 1px solid var(--border-color);
  transition: background-color 0.2s ease;
}

.cart-item:hover {
  background-color: rgba(22, 119, 255, 0.02);
}

.item-checkbox {
  margin-right: 0;
}

.item-info {
  flex: 1;
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.item-image-link {
  flex-shrink: 0;
  transition: transform 0.2s ease;
}

.item-image-link:hover {
  transform: scale(1.02);
}

.item-image {
  width: 100px;
  height: 100px;
  border-radius: var(--radius-sm);
  object-fit: cover;
  border: 1px solid var(--border-color);
}

.item-details {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: 16px;
  font-weight: 500;
  color: var(--text-primary);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-decoration: none;
  transition: color 0.2s ease;
}

.item-name:hover {
  color: var(--primary-color);
}

.item-price {
  width: 120px;
  text-align: center;
}

.price-text {
  font-size: 16px;
  font-weight: 600;
  color: var(--danger-color);
}

.item-quantity {
  width: 160px;
  display: flex;
  justify-content: center;
}

.quantity-input {
  width: 120px;
}

:deep(.quantity-input .el-input__wrapper) {
  border-radius: var(--radius-sm) !important;
}

.item-subtotal {
  width: 120px;
  text-align: center;
}

.subtotal-text {
  font-size: 18px;
  font-weight: 700;
  color: var(--danger-color);
}

.item-action {
  width: 100px;
  text-align: center;
}

.delete-item-btn {
  font-weight: 500;
}

.cart-footer {
  position: sticky;
  bottom: 0;
  background: var(--card-bg);
  padding: 20px 32px;
  border-top: 1px solid var(--border-color);
  box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.04);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 24px;
}

.footer-checkbox {
  margin-right: 0;
}

.footer-delete-btn {
  border-radius: var(--radius-sm);
  font-weight: 500;
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 32px;
}

.selected-info {
  font-size: 14px;
  color: var(--text-secondary);
}

.selected-count {
  font-size: 18px;
  font-weight: 700;
  color: var(--primary-color);
}

.total-section {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.total-label {
  font-size: 16px;
  color: var(--text-secondary);
}

.total-amount {
  font-size: 28px;
  font-weight: 700;
  color: var(--danger-color);
}

.checkout-btn {
  height: 48px;
  padding: 0 40px;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--radius-sm);
  transition: all 0.2s ease;
}

.checkout-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.checkout-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.empty-cart {
  padding: 80px 0;
}

.go-shopping-btn {
  height: 44px;
  padding: 0 32px;
  font-weight: 500;
  border-radius: var(--radius-sm);
}

@media (max-width: 768px) {
  .cart-header {
    padding: 16px;
  }
  
  .cart-title {
    font-size: 20px;
  }
  
  .cart-table-header {
    display: none;
  }
  
  .cart-item {
    padding: 16px;
    flex-wrap: wrap;
    gap: 12px;
  }
  
  .item-info {
    width: 100%;
  }
  
  .item-price,
  .item-quantity,
  .item-subtotal,
  .item-action {
    width: auto;
    flex: 1;
    text-align: left;
  }
  
  .cart-footer {
    padding: 16px;
    flex-direction: column;
    gap: 16px;
  }
  
  .footer-left,
  .footer-right {
    width: 100%;
    justify-content: space-between;
    gap: 12px;
  }
  
  .total-amount {
    font-size: 24px;
  }
}
</style>
