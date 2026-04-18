<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { 
  getAdminProductSkus, 
  createSku, 
  updateSku, 
  deleteSku,
  deductSkuStock,
  addSkuStock
} from '@/api/admin'
import type { ProductSku, SkuCreateParams } from '@/types/api'

const loading = ref(false)
const skus = ref<ProductSku[]>([])
const productId = ref<number | undefined>()
const showDialog = ref(false)
const dialogType = ref<'create' | 'edit'>('create')
const editingSku = ref<ProductSku | null>(null)
const stockDialog = ref(false)
const stockType = ref<'add' | 'deduct'>('add')
const stockQuantity = ref(1)
const selectedSku = ref<ProductSku | null>(null)

const form = ref<SkuCreateParams>({
  productId: 0,
  skuCode: '',
  specJson: '{}',
  price: 0,
  stock: 0,
  image: ''
})

const fetchSkus = async () => {
  if (!productId.value) {
    ElMessage.warning('请输入商品ID')
    return
  }
  
  loading.value = true
  try {
    skus.value = await getAdminProductSkus(productId.value)
  } catch (error) {
    console.error('获取SKU列表失败', error)
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  if (!productId.value) {
    ElMessage.warning('请先输入商品ID并查询')
    return
  }
  
  dialogType.value = 'create'
  editingSku.value = null
  form.value = {
    productId: productId.value,
    skuCode: '',
    specJson: '{}',
    price: 0,
    stock: 0,
    image: ''
  }
  showDialog.value = true
}

const handleEdit = (row: ProductSku) => {
  dialogType.value = 'edit'
  editingSku.value = row
  form.value = {
    productId: row.productId,
    skuCode: row.skuCode,
    specJson: row.specJson,
    price: row.price,
    stock: row.stock,
    image: row.image || ''
  }
  showDialog.value = true
}

const handleSubmit = async () => {
  if (!form.value.skuCode) {
    ElMessage.warning('请填写SKU编码')
    return
  }

  try {
    if (dialogType.value === 'create') {
      await createSku(form.value)
      ElMessage.success('创建成功')
    } else if (editingSku.value) {
      await updateSku(editingSku.value.id, form.value)
      ElMessage.success('更新成功')
    }
    showDialog.value = false
    fetchSkus()
  } catch (error) {
    console.error('操作失败', error)
  }
}

const handleDelete = async (row: ProductSku) => {
  try {
    await ElMessageBox.confirm('确定要删除该SKU吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteSku(row.id)
    ElMessage.success('删除成功')
    fetchSkus()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
    }
  }
}

const handleStock = (row: ProductSku, type: 'add' | 'deduct') => {
  selectedSku.value = row
  stockType.value = type
  stockQuantity.value = 1
  stockDialog.value = true
}

const submitStockChange = async () => {
  if (!selectedSku.value || stockQuantity.value <= 0) {
    ElMessage.warning('请输入有效的数量')
    return
  }

  try {
    if (stockType.value === 'add') {
      await addSkuStock(selectedSku.value.id, stockQuantity.value)
      ElMessage.success('库存增加成功')
    } else {
      await deductSkuStock(selectedSku.value.id, stockQuantity.value)
      ElMessage.success('库存扣减成功')
    }
    stockDialog.value = false
    fetchSkus()
  } catch (error) {
    console.error('库存操作失败', error)
  }
}

const formatSpec = (specJson: string) => {
  try {
    const spec = JSON.parse(specJson)
    return Object.entries(spec).map(([k, v]) => `${k}: ${v}`).join(', ')
  } catch {
    return specJson
  }
}

onMounted(() => {
  // 可以从路由参数获取productId
})
</script>

<template>
  <div class="p-6">
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-xl font-bold">SKU管理</h2>
      <div class="flex items-center gap-4">
        <el-input 
          v-model="productId" 
          placeholder="输入商品ID" 
          style="width: 200px"
          type="number"
        />
        <el-button type="primary" @click="fetchSkus">查询</el-button>
        <el-button type="success" @click="handleCreate">
          <el-icon class="mr-1"><Plus /></el-icon>
          新建SKU
        </el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="skus" stripe>
      <el-table-column prop="skuCode" label="SKU编码" width="150" />
      <el-table-column prop="specJson" label="规格" min-width="200">
        <template #default="{ row }">
          {{ formatSpec(row.specJson) }}
        </template>
      </el-table-column>
      <el-table-column prop="price" label="售价" width="100">
        <template #default="{ row }">
          <span class="text-red-500 font-bold">¥{{ row.price }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="100">
        <template #default="{ row }">
          <el-tag :type="row.stock > 10 ? 'success' : row.stock > 0 ? 'warning' : 'danger'">
            {{ row.stock }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleEdit(row)">
            编辑
          </el-button>
          <el-button type="success" link size="small" @click="handleStock(row, 'add')">
            加库存
          </el-button>
          <el-button type="warning" link size="small" @click="handleStock(row, 'deduct')">
            减库存
          </el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && skus.length === 0" description="请输入商品ID查询SKU" />

    <el-dialog v-model="showDialog" :title="dialogType === 'create' ? '新建SKU' : '编辑SKU'" width="500px" :close-on-click-modal="false">
      <el-form :model="form" label-width="100px">
        <el-form-item label="SKU编码" required>
          <el-input v-model="form.skuCode" placeholder="请输入SKU编码" />
        </el-form-item>
        <el-form-item label="规格JSON">
          <el-input 
            v-model="form.specJson" 
            type="textarea"
            :rows="3"
            placeholder='{"颜色": "红色", "尺寸": "XL"}'
          />
        </el-form-item>
        <el-form-item label="售价" required>
          <el-input-number v-model="form.price" :min="0" :precision="2" class="w-full" />
        </el-form-item>
        <el-form-item label="库存" required>
          <el-input-number v-model="form.stock" :min="0" class="w-full" />
        </el-form-item>
        <el-form-item label="图片URL">
          <el-input v-model="form.image" placeholder="请输入图片URL" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="stockDialog" :title="stockType === 'add' ? '增加库存' : '扣减库存'" width="400px" :close-on-click-modal="false">
      <el-form label-width="80px">
        <el-form-item label="当前库存">
          <span class="font-bold">{{ selectedSku?.stock }}</span>
        </el-form-item>
        <el-form-item :label="stockType === 'add' ? '增加数量' : '扣减数量'">
          <el-input-number v-model="stockQuantity" :min="1" class="w-full" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="stockDialog = false">取消</el-button>
        <el-button :type="stockType === 'add' ? 'success' : 'warning'" @click="submitStockChange">
          确认
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
