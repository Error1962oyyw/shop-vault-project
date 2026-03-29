<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { getPointsRules, createPointsRule, updatePointsRule, deletePointsRule } from '@/api/admin'
import type { PointsRule } from '@/api/admin'

const loading = ref(false)
const rules = ref<PointsRule[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const form = ref<Partial<PointsRule>>({
  ruleCode: '',
  ruleName: '',
  description: '',
  pointsValue: 0,
  pointsRatio: 1,
  ruleType: 1,
  dailyLimit: 0,
  isActive: true,
  sortOrder: 0
})

const formRules = {
  ruleCode: [{ required: true, message: '请输入规则编码', trigger: 'blur' }],
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }]
}

const ruleTypes = [
  { value: 1, label: '签到' },
  { value: 2, label: '消费' },
  { value: 3, label: '评价' },
  { value: 4, label: '分享' },
  { value: 5, label: '首购' }
]

const fetchRules = async () => {
  loading.value = true
  try {
    rules.value = await getPointsRules()
  } catch (error) {
    console.error('获取积分规则失败', error)
    ElMessage.error('获取积分规则失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增积分规则'
  form.value = {
    ruleCode: '',
    ruleName: '',
    description: '',
    pointsValue: 0,
    pointsRatio: 1,
    ruleType: 1,
    dailyLimit: 0,
    isActive: true,
    sortOrder: 0
  }
  dialogVisible.value = true
}

const handleEdit = (row: PointsRule) => {
  dialogTitle.value = '编辑积分规则'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = async (row: PointsRule) => {
  try {
    await ElMessageBox.confirm('确定要删除该规则吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deletePointsRule(row.id)
    ElMessage.success('删除成功')
    fetchRules()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSubmit = async () => {
  await formRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      try {
        if (form.value.id) {
          await updatePointsRule(form.value.id, form.value)
          ElMessage.success('更新成功')
        } else {
          await createPointsRule(form.value)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchRules()
      } catch (error) {
        ElMessage.error('保存失败')
      }
    }
  })
}

const getRuleTypeName = (type: number) => {
  const found = ruleTypes.find(r => r.value === type)
  return found ? found.label : '未知'
}

onMounted(() => {
  fetchRules()
})
</script>

<template>
  <div class="points-rules-page">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">积分倍率配置</h2>
        <p class="page-subtitle">配置不同场景的积分获取规则和倍率设置</p>
      </div>
      <el-button type="primary" @click="handleAdd">
        <el-icon class="mr-2"><Plus /></el-icon>
        新增规则
      </el-button>
    </div>

    <el-card class="table-card" v-loading="loading">
      <el-table :data="rules" stripe>
        <el-table-column prop="ruleCode" label="规则编码" width="120" />
        <el-table-column prop="ruleName" label="规则名称" min-width="120" />
        <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
        <el-table-column prop="ruleType" label="规则类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ getRuleTypeName(row.ruleType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="pointsValue" label="固定积分值" width="100">
          <template #default="{ row }">
            {{ row.pointsValue || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="pointsRatio" label="积分倍率" width="100">
          <template #default="{ row }">
            <span>{{ row.pointsRatio || 1 }}倍</span>
          </template>
        </el-table-column>
        <el-table-column prop="dailyLimit" label="每日限制" width="100">
          <template #default="{ row }">
            {{ row.dailyLimit || '无限制' }}
          </template>
        </el-table-column>
        <el-table-column prop="isActive" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isActive ? 'success' : 'info'">
              {{ row.isActive ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              <el-icon class="mr-1"><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              <el-icon class="mr-1"><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="规则编码" prop="ruleCode">
          <el-input v-model="form.ruleCode" placeholder="如: SIGN_IN, PURCHASE" />
        </el-form-item>
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="form.ruleName" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="规则类型" prop="ruleType">
          <el-select v-model="form.ruleType" placeholder="请选择规则类型" style="width: 100%">
            <el-option v-for="item in ruleTypes" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="请输入规则描述" />
        </el-form-item>
        <el-form-item label="固定积分值">
          <el-input-number v-model="form.pointsValue" :min="0" :max="10000" />
          <span class="ml-2 text-gray-500">签到/评价等固定积分</span>
        </el-form-item>
        <el-form-item label="积分倍率">
          <el-input-number v-model="form.pointsRatio" :min="0.1" :max="100" :step="0.1" :precision="1" />
          <span class="ml-2 text-gray-500">消费积分倍率</span>
        </el-form-item>
        <el-form-item label="每日限制">
          <el-input-number v-model="form.dailyLimit" :min="0" />
          <span class="ml-2 text-gray-500">0表示无限制</span>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.isActive" active-text="启用" inactive-text="停用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.points-rules-page {
  padding: 24px;
  background: linear-gradient(135deg, #f7f8fa 0%, #e6f4ff 50%, #f0f5ff 100%);
  min-height: calc(100vh - 120px);
  animation: fadeIn 0.4s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px 24px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.header-left {
  flex: 1;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 4px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.table-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.table-card :deep(.el-card__body) {
  padding: 0;
}

.table-card :deep(.el-table) {
  border-radius: 16px;
}

.table-card :deep(.el-table th) {
  background: #f8fafc;
  font-weight: 600;
  color: #374151;
}

.table-card :deep(.el-table td) {
  padding: 16px 0;
}

.ratio-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mr-2 {
  margin-right: 8px;
}

.mr-1 {
  margin-right: 4px;
}

.ml-2 {
  margin-left: 8px;
}

.text-gray-500 {
  color: #6b7280;
}
</style>
