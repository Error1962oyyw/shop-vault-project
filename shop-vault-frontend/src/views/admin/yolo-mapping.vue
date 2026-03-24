<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { 
  getYoloMappings, 
  getAvailableYoloLabels, 
  addYoloMapping, 
  updateYoloMapping, 
  deleteYoloMapping, 
  toggleYoloMapping 
} from '@/api/admin'
import { getCategoryList } from '@/api/product'
import type { YoloMapping, Category } from '@/types/api'

const loading = ref(false)
const mappings = ref<YoloMapping[]>([])
const categories = ref<Category[]>([])
const availableLabels = ref<string[]>([])
const showDialog = ref(false)
const dialogType = ref<'create' | 'edit'>('create')
const editingMapping = ref<YoloMapping | null>(null)

const form = ref({
  yoloLabel: '',
  categoryId: 0
})

const fetchMappings = async () => {
  loading.value = true
  try {
    mappings.value = await getYoloMappings()
  } catch (error) {
    console.error('获取映射列表失败', error)
  } finally {
    loading.value = false
  }
}

const fetchCategories = async () => {
  try {
    categories.value = await getCategoryList()
  } catch (error) {
    console.error('获取分类列表失败', error)
  }
}

const fetchAvailableLabels = async () => {
  try {
    availableLabels.value = await getAvailableYoloLabels()
  } catch (error) {
    console.error('获取可用标签失败', error)
  }
}

const handleCreate = () => {
  dialogType.value = 'create'
  editingMapping.value = null
  form.value = {
    yoloLabel: '',
    categoryId: 0
  }
  showDialog.value = true
}

const handleEdit = (row: YoloMapping) => {
  dialogType.value = 'edit'
  editingMapping.value = row
  form.value = {
    yoloLabel: row.yoloLabel,
    categoryId: row.categoryId
  }
  showDialog.value = true
}

const handleSubmit = async () => {
  if (!form.value.yoloLabel || !form.value.categoryId) {
    ElMessage.warning('请填写完整信息')
    return
  }

  try {
    if (dialogType.value === 'create') {
      await addYoloMapping(form.value)
      ElMessage.success('创建成功')
    } else if (editingMapping.value) {
      await updateYoloMapping({
        id: editingMapping.value.id,
        ...form.value
      })
      ElMessage.success('更新成功')
    }
    showDialog.value = false
    fetchMappings()
  } catch (error) {
    console.error('操作失败', error)
  }
}

const handleDelete = async (row: YoloMapping) => {
  try {
    await ElMessageBox.confirm('确定要删除该映射吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteYoloMapping(row.id)
    ElMessage.success('删除成功')
    fetchMappings()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
    }
  }
}

const handleToggle = async (row: YoloMapping) => {
  try {
    await toggleYoloMapping(row.id)
    ElMessage.success('状态切换成功')
    fetchMappings()
  } catch (error) {
    console.error('切换状态失败', error)
  }
}

const formatDate = (date: string) => {
  return date ? date.split('T')[0] : ''
}

onMounted(() => {
  fetchMappings()
  fetchCategories()
  fetchAvailableLabels()
})
</script>

<template>
  <div class="p-6">
    <div class="flex items-center justify-between mb-6">
      <div>
        <h2 class="text-xl font-bold">YOLO标签映射管理</h2>
        <p class="text-gray-500 text-sm mt-1">管理YOLO识别标签与商品分类的映射关系</p>
      </div>
      <el-button type="primary" @click="handleCreate">
        <el-icon class="mr-1"><Plus /></el-icon>
        新建映射
      </el-button>
    </div>

    <el-table v-loading="loading" :data="mappings" stripe>
      <el-table-column prop="yoloLabel" label="YOLO标签" min-width="150">
        <template #default="{ row }">
          <el-tag type="info">{{ row.yoloLabel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="categoryName" label="映射分类" min-width="150">
        <template #default="{ row }">
          <el-tag>{{ row.categoryName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="isActive" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.isActive ? 'success' : 'danger'">
            {{ row.isActive ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="120">
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleEdit(row)">
            编辑
          </el-button>
          <el-button 
            :type="row.isActive ? 'warning' : 'success'" 
            link 
            size="small"
            @click="handleToggle(row)"
          >
            {{ row.isActive ? '禁用' : '启用' }}
          </el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && mappings.length === 0" description="暂无映射数据" />

    <el-dialog v-model="showDialog" :title="dialogType === 'create' ? '新建映射' : '编辑映射'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="YOLO标签" required>
          <el-select 
            v-model="form.yoloLabel" 
            placeholder="请选择或输入YOLO标签" 
            class="w-full"
            filterable
            allow-create
          >
            <el-option 
              v-for="label in availableLabels" 
              :key="label"
              :label="label"
              :value="label"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="映射分类" required>
          <el-select v-model="form.categoryId" placeholder="请选择分类" class="w-full">
            <el-option 
              v-for="category in categories" 
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>
