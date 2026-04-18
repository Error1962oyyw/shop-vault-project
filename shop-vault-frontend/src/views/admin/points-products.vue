<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Plus, Edit, Delete, Upload } from '@element-plus/icons-vue'
import { AdminPageLayout } from '@/components/admin'
import { usePagination } from '@/composables'
import type { PageResult } from '@/types/api'
import { getPointsProducts } from '@/api/vip'
import { uploadImage } from '@/api/product'
import type { PointsProduct } from '@/api/vip'
import request from '@/utils/request'

const loading = ref(false)
const products = ref<PointsProduct[]>([])
const showDialog = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const imageUploading = ref(false)
const productForm = ref({
  id: 0,
  name: '',
  description: '',
  image: '',
  type: 1,
  pointsCost: 0,
  stock: 0,
  dailyLimit: 0,
  originalPrice: 0,
  relatedId: null as number | null,
  sortOrder: 0,
  status: 1,
  couponType: 1,
  couponThreshold: 0,
  couponAmount: 0,
  vipDays: 30
})

const { pagination, handleCurrentChange, setTotal, getParams } = usePagination({
  onPageChange: () => fetchProducts()
})

const productTypes = [
  { value: 1, label: '小商品' },
  { value: 2, label: '优惠券' },
  { value: 3, label: 'VIP月卡' },
  { value: 4, label: 'SVIP年卡' }
]

const fetchProducts = async () => {
  loading.value = true
  try {
    const params = getParams()
    const res: PageResult<PointsProduct> = await request({
      url: '/api/admin/points-products',
      method: 'get',
      params: {
        pageNum: params.pageNum,
        pageSize: params.pageSize
      }
    })
    products.value = res.records || []
    setTotal(res.total || 0)
  } catch (error: any) {
    console.error('获取积分商品失败', error)
    ElMessage.error(error?.response?.data?.msg || error?.message || '获取积分商品失败')
    try {
      const fallbackProducts = await getPointsProducts()
      products.value = fallbackProducts || []
      setTotal(fallbackProducts?.length || 0)
    } catch (fallbackError) {
      console.error('备用获取也失败', fallbackError)
      products.value = []
      setTotal(0)
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchProducts()
})

const handleAdd = () => {
  dialogType.value = 'add'
  productForm.value = {
    id: 0,
    name: '',
    description: '',
    image: '',
    type: 1,
    pointsCost: 0,
    stock: 0,
    dailyLimit: 0,
    originalPrice: 0,
    relatedId: null,
    sortOrder: 0,
    status: 1,
    couponType: 1,
    couponThreshold: 0,
    couponAmount: 0,
    vipDays: 30
  }
  showDialog.value = true
}

const handleEdit = (row: PointsProduct) => {
  dialogType.value = 'edit'
  productForm.value = {
    id: row.id,
    name: row.name,
    description: row.description || '',
    image: row.image || '',
    type: row.type,
    pointsCost: row.pointsCost,
    stock: row.stock,
    dailyLimit: row.dailyLimit || 0,
    originalPrice: row.originalPrice || 0,
    relatedId: row.relatedId,
    sortOrder: row.sortOrder || 0,
    status: row.status,
    couponType: row.couponType || 1,
    couponThreshold: row.couponThreshold || 0,
    couponAmount: row.couponAmount || 0,
    vipDays: row.vipDays || 30
  }
  showDialog.value = true
}

const handleDelete = async (id: number) => {
  try {
    await request({
      url: `/api/admin/points-products/${id}`,
      method: 'delete'
    })
    ElMessage.success('删除成功')
    fetchProducts()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleSubmit = async () => {
  try {
    if (dialogType.value === 'add') {
      await request({
        url: '/api/admin/points-products',
        method: 'post',
        data: productForm.value
      })
      ElMessage.success('添加成功')
    } else {
      await request({
        url: `/api/admin/points-products/${productForm.value.id}`,
        method: 'put',
        data: productForm.value
      })
      ElMessage.success('更新成功')
    }
    showDialog.value = false
    fetchProducts()
  } catch (error) {
    ElMessage.error(dialogType.value === 'add' ? '添加失败' : '更新失败')
  }
}

const handleStatusChange = async (row: PointsProduct) => {
  try {
    await request({
      url: `/api/admin/points-products/${row.id}/status`,
      method: 'put',
      params: { status: row.status }
    })
    ElMessage.success('状态更新成功')
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error('状态更新失败')
  }
}

const getTypeLabel = (type: number) => {
  return productTypes.find(t => t.value === type)?.label || '其他'
}

const formatPrice = (price: number) => {
  return price ? `¥${price.toFixed(2)}` : '-'
}

const handleImageUpload = async (options: { file: File }) => {
  imageUploading.value = true
  try {
    const url = await uploadImage(options.file)
    productForm.value.image = url
    ElMessage.success('图片上传成功')
  } catch (error) {
    console.error('上传失败', error)
    ElMessage.error('上传失败，请重试')
  } finally {
    imageUploading.value = false
  }
}

const handleImageDelete = () => {
  productForm.value.image = ''
}
</script>

<template>
  <AdminPageLayout title="积分商城管理" subtitle="管理积分兑换商品">
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
      <el-button type="primary" class="add-btn" @click="handleAdd">
        <el-icon class="mr-1"><Plus /></el-icon>
        添加商品
      </el-button>
    </template>

    <el-table :data="products" v-loading="loading" stripe>
      <el-table-column label="商品信息" min-width="200">
        <template #default="{ row }">
          <div class="product-info">
            <div class="product-image">
              <img v-if="row.image" :src="row.image" :alt="row.name" />
              <div v-else class="image-placeholder">
                <el-icon><Plus /></el-icon>
              </div>
            </div>
            <div class="product-details">
              <h4 class="product-name">{{ row.name }}</h4>
              <p class="product-desc">{{ row.description || '-' }}</p>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="类型" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.type === 3 || row.type === 4 ? 'danger' : row.type === 2 ? 'warning' : 'success'" size="small">
            {{ getTypeLabel(row.type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="所需积分" width="100" align="center">
        <template #default="{ row }">
          <span class="points-value">{{ row.pointsCost }}</span>
        </template>
      </el-table-column>
      <el-table-column label="原价" width="100" align="center">
        <template #default="{ row }">
          {{ formatPrice(row.originalPrice) }}
        </template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="80" align="center" />
      <el-table-column label="每日限制" width="90" align="center">
        <template #default="{ row }">
          {{ row.dailyLimit || '不限' }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-switch 
            v-model="row.status" 
            :active-value="1" 
            :inactive-value="0"
            @change="handleStatusChange(row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="center">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleEdit(row)">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row.id)">
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
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

  <el-dialog v-model="showDialog" :title="dialogType === 'add' ? '添加商品' : '编辑商品'" width="600px" class="custom-dialog" :close-on-click-modal="false">
    <el-form :model="productForm" label-width="100px">
      <el-form-item label="商品名称" required>
        <el-input v-model="productForm.name" placeholder="请输入商品名称" />
      </el-form-item>
      <el-form-item label="商品描述">
        <el-input v-model="productForm.description" type="textarea" :rows="2" placeholder="请输入商品描述" />
      </el-form-item>
      <el-form-item label="商品图片">
        <div class="image-upload-section">
          <div v-if="productForm.image" class="image-preview">
            <img :src="productForm.image" alt="商品图片" />
            <div class="image-actions">
              <el-button type="danger" size="small" :icon="Delete" @click="handleImageDelete">
                删除
              </el-button>
            </div>
          </div>
          <el-upload
            v-else
            class="image-uploader"
            :show-file-list="false"
            :http-request="handleImageUpload"
            accept="image/*"
          >
            <div class="upload-placeholder" :class="{ 'is-uploading': imageUploading }">
              <el-icon v-if="!imageUploading" class="upload-icon"><Upload /></el-icon>
              <span v-if="imageUploading">上传中...</span>
              <span v-else>点击上传图片</span>
            </div>
          </el-upload>
          <div class="upload-tip">建议尺寸: 200x200px，支持 JPG、PNG 格式</div>
        </div>
      </el-form-item>
      <el-form-item label="商品类型" required>
        <el-select v-model="productForm.type" class="w-full">
          <el-option v-for="t in productTypes" :key="t.value" :label="t.label" :value="t.value" />
        </el-select>
      </el-form-item>
      <template v-if="productForm.type === 2">
        <el-form-item label="优惠券类型">
          <el-radio-group v-model="productForm.couponType">
            <el-radio :value="1">满减券</el-radio>
            <el-radio :value="2">无门槛券</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="productForm.couponType === 1" label="门槛金额">
          <el-input-number v-model="productForm.couponThreshold" :min="1" :max="10000" :precision="2" />
          <span class="form-hint">满{{ productForm.couponThreshold }}元可用</span>
        </el-form-item>
        <el-form-item label="减免金额">
          <el-input-number v-model="productForm.couponAmount" :min="1" :max="1000" :precision="2" />
          <span class="form-hint">减{{ productForm.couponAmount }}元</span>
        </el-form-item>
      </template>
      <template v-if="productForm.type === 3 || productForm.type === 4">
        <el-form-item label="会员天数">
          <el-input-number v-model="productForm.vipDays" :min="1" :max="365" />
          <span class="form-hint">天</span>
        </el-form-item>
      </template>
      <el-form-item label="所需积分" required>
        <el-input-number v-model="productForm.pointsCost" :min="0" />
      </el-form-item>
      <el-form-item label="原价">
        <el-input-number v-model="productForm.originalPrice" :min="0" :precision="2" />
      </el-form-item>
      <el-form-item label="库存" required>
        <el-input-number v-model="productForm.stock" :min="0" />
      </el-form-item>
      <el-form-item label="每日限制">
        <el-input-number v-model="productForm.dailyLimit" :min="0" />
        <span class="form-hint">0表示不限</span>
      </el-form-item>
      <el-form-item label="排序">
        <el-input-number v-model="productForm.sortOrder" :min="0" />
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="productForm.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showDialog = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit">确定</el-button>
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
  width: 56px;
  height: 56px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  background: #f3f4f6;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9ca3af;
}

.product-details {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 4px;
}

.product-desc {
  font-size: 12px;
  color: #6b7280;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.points-value {
  color: #fa8c16;
  font-weight: 700;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 16px 0 0;
}

.mr-1 {
  margin-right: 4px;
}

.w-full {
  width: 100%;
}

.form-hint {
  font-size: 12px;
  color: #9ca3af;
  margin-left: 8px;
}

:deep(.el-table) {
  border-radius: 16px;
  overflow: hidden;
}

:deep(.el-table th) {
  background: #f8fafc;
  color: #374151;
  font-weight: 600;
}

:deep(.custom-dialog .el-dialog) {
  border-radius: 16px;
}

.image-upload-section {
  width: 100%;
}

.image-preview {
  position: relative;
  width: 150px;
  height: 150px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-preview .image-actions {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 6px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.6));
  display: flex;
  justify-content: center;
}

.image-uploader {
  width: 150px;
  height: 150px;
}

.upload-placeholder {
  width: 150px;
  height: 150px;
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
  font-size: 28px;
  color: #909399;
  margin-bottom: 6px;
}

.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}
</style>
