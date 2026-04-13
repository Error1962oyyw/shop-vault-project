<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { 
  getAdminCoupons, 
  createCoupon, 
  updateCoupon, 
  deleteCoupon, 
  updateCouponStatus 
} from '@/api/admin'
import type { CouponTemplate, CouponTemplateParams, PageResult } from '@/types/api'

const loading = ref(false)
const coupons = ref<CouponTemplate[]>([])
const pagination = ref({ current: 1, size: 10, total: 0 })
const showDialog = ref(false)
const dialogType = ref<'create' | 'edit'>('create')
const editingCoupon = ref<CouponTemplate | null>(null)

const form = ref<CouponTemplateParams>({
  name: '',
  type: 1,
  value: 10,
  minAmount: 0,
  totalCount: 100,
  perLimit: 1,
  validStartTime: '',
  validEndTime: ''
})

const couponTypes = [
  { label: '满减券', value: 1 },
  { label: '折扣券', value: 2 },
  { label: '无门槛券', value: 3 }
]

const fetchCoupons = async () => {
  loading.value = true
  try {
    const res: PageResult<CouponTemplate> = await getAdminCoupons({
      pageNum: pagination.value.current,
      pageSize: pagination.value.size
    })
    coupons.value = res.records
    pagination.value.total = res.total
  } catch (error) {
    console.error('获取优惠券列表失败', error)
  } finally {
    loading.value = false
  }
}

const handleCurrentChange = (val: number) => {
  pagination.value.current = val
  fetchCoupons()
}

const handleSizeChange = (val: number) => {
  pagination.value.size = val
  pagination.value.current = 1
  fetchCoupons()
}

const handleCreate = () => {
  dialogType.value = 'create'
  editingCoupon.value = null
  form.value = {
    name: '',
    type: 1,
    value: 10,
    minAmount: 0,
    totalCount: 100,
    perLimit: 1,
    validStartTime: '',
    validEndTime: ''
  }
  showDialog.value = true
}

const handleEdit = (row: CouponTemplate) => {
  dialogType.value = 'edit'
  editingCoupon.value = row
  form.value = {
    name: row.name,
    type: row.type,
    value: row.value,
    minAmount: row.minAmount,
    totalCount: row.totalCount,
    perLimit: row.perLimit || row.perUserLimit || 1,
    validStartTime: row.validStartTime || row.startTime || '',
    validEndTime: row.validEndTime || row.endTime || ''
  }
  showDialog.value = true
}

const handleSubmit = async () => {
  if (!form.value.name || !form.value.validStartTime || !form.value.validEndTime) {
    ElMessage.warning('请填写完整信息')
    return
  }

  try {
    if (dialogType.value === 'create') {
      await createCoupon(form.value)
      ElMessage.success('创建成功')
    } else if (editingCoupon.value) {
      await updateCoupon(editingCoupon.value.id, form.value)
      ElMessage.success('更新成功')
    }
    showDialog.value = false
    fetchCoupons()
  } catch (error) {
    console.error('操作失败', error)
  }
}

const handleDelete = async (row: CouponTemplate) => {
  try {
    await ElMessageBox.confirm('确定要删除该优惠券吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteCoupon(row.id)
    ElMessage.success('删除成功')
    fetchCoupons()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
    }
  }
}

const handleToggleStatus = async (row: CouponTemplate) => {
  try {
    await updateCouponStatus(row.id, row.status === 1 ? 0 : 1)
    ElMessage.success('状态更新成功')
    fetchCoupons()
  } catch (error) {
    console.error('更新状态失败', error)
  }
}

const formatDate = (date: string) => {
  return date ? date.split('T')[0] : ''
}

const getStatusText = (status: number) => {
  switch (status) {
    case 0: return '未开始'
    case 1: return '进行中'
    case 2: return '已结束'
    default: return '未知'
  }
}

const getStatusType = (status: number) => {
  switch (status) {
    case 0: return 'info'
    case 1: return 'success'
    case 2: return 'danger'
    default: return 'info'
  }
}

onMounted(() => {
  fetchCoupons()
})
</script>

<template>
  <div class="p-6">
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-xl font-bold">优惠券管理</h2>
      <el-button type="primary" @click="handleCreate">
        <el-icon class="mr-1"><Plus /></el-icon>
        新建优惠券
      </el-button>
    </div>

    <el-table v-loading="loading" :data="coupons" stripe>
      <el-table-column prop="name" label="优惠券名称" min-width="150" />
      <el-table-column prop="type" label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.type === 1 ? 'danger' : row.type === 2 ? 'warning' : 'success'">
            {{ couponTypes.find(t => t.value === row.type)?.label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="value" label="优惠额度" width="100">
        <template #default="{ row }">
          <span class="text-red-500 font-bold">
            {{ row.type === 2 ? `${row.discount}折` : `¥${row.value}` }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="minAmount" label="使用门槛" width="100">
        <template #default="{ row }">
          {{ row.minAmount > 0 ? `满${row.minAmount}` : '无门槛' }}
        </template>
      </el-table-column>
      <el-table-column prop="totalCount" label="发放/已领" width="100">
        <template #default="{ row }">
          {{ row.totalCount }} / {{ row.usedCount }}
        </template>
      </el-table-column>
      <el-table-column label="有效期" min-width="180">
        <template #default="{ row }">
          {{ formatDate(row.startTime) }} ~ {{ formatDate(row.endTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleEdit(row)">
            编辑
          </el-button>
          <el-button 
            :type="row.status === 1 ? 'warning' : 'success'" 
            link 
            size="small"
            @click="handleToggleStatus(row)"
          >
            {{ row.status === 1 ? '停用' : '启用' }}
          </el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="flex justify-end mt-4">
      <el-pagination
        :current-page="pagination.current"
        :page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="handleCurrentChange"
        @size-change="handleSizeChange"
      />
    </div>

    <el-dialog v-model="showDialog" :title="dialogType === 'create' ? '新建优惠券' : '编辑优惠券'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="优惠券名称" required>
          <el-input v-model="form.name" placeholder="请输入优惠券名称" />
        </el-form-item>
        <el-form-item label="优惠券类型" required>
          <el-select v-model="form.type" class="w-full">
            <el-option 
              v-for="type in couponTypes" 
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="优惠额度" required>
          <el-input-number 
            v-model="form.value" 
            :min="0" 
            :precision="2"
            class="w-full"
          />
        </el-form-item>
        <el-form-item label="使用门槛">
          <el-input-number 
            v-model="form.minAmount" 
            :min="0" 
            :precision="2"
            class="w-full"
          />
        </el-form-item>
        <el-form-item label="发放数量" required>
          <el-input-number v-model="form.totalCount" :min="1" class="w-full" />
        </el-form-item>
        <el-form-item label="每人限领" required>
          <el-input-number v-model="form.perLimit" :min="1" class="w-full" />
        </el-form-item>
        <el-form-item label="有效期" required>
          <el-date-picker
            v-model="form.validStartTime"
            type="datetime"
            placeholder="开始时间"
            class="w-full mb-2"
          />
          <el-date-picker
            v-model="form.validEndTime"
            type="datetime"
            placeholder="结束时间"
            class="w-full"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>
