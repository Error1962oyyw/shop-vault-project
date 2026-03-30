<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { getMemberDayActivities, createActivity, updateActivity, deleteActivity, updateActivityStatus } from '@/api/admin'
import type { Activity } from '@/api/admin'

interface ActivityForm extends Partial<Activity> {
  ruleType?: 'none' | 'weekly' | 'monthly' | 'both'
}

const loading = ref(false)
const activities = ref<Activity[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const form = ref<ActivityForm>({
  name: '',
  type: 1,
  status: 1,
  activityType: 1,
  discountRate: 0.85,
  pointsMultiplier: 2.0,
  startTime: '',
  endTime: '',
  description: '',
  ruleExpression: '',
  ruleType: 'none',
  couponType: 1,
  couponThreshold: 0,
  couponAmount: 0,
  couponTotal: 100
})

const rules = {
  name: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const weekDays = [
  { value: '1', label: '周一' },
  { value: '2', label: '周二' },
  { value: '3', label: '周三' },
  { value: '4', label: '周四' },
  { value: '5', label: '周五' },
  { value: '6', label: '周六' },
  { value: '7', label: '周日' }
]

const monthDays = Array.from({ length: 31 }, (_, i) => ({
  value: String(i + 1),
  label: `${i + 1}号`
}))

const selectedWeekDays = ref<string[]>([])
const selectedMonthDays = ref<string[]>([])

watch(() => form.value.ruleType, (newType, oldType) => {
  if (oldType === 'weekly' && newType === 'monthly') {
    selectedWeekDays.value = []
  } else if (oldType === 'monthly' && newType === 'weekly') {
    selectedMonthDays.value = []
  } else if (newType === 'none') {
    selectedWeekDays.value = []
    selectedMonthDays.value = []
  }
})

const ruleExpressionDisplay = computed(() => {
  const parts: string[] = []
  if (selectedWeekDays.value.length > 0) {
    const days = selectedWeekDays.value.map(v => weekDays.find(d => d.value === v)?.label).join('、')
    parts.push(`每${days}`)
  }
  if (selectedMonthDays.value.length > 0) {
    const days = selectedMonthDays.value.map(v => `${v}号`).join('、')
    parts.push(`每月${days}`)
  }
  return parts.join('；') || '无固定规则'
})

const parseRuleExpression = (expr: string) => {
  selectedWeekDays.value = []
  selectedMonthDays.value = []
  
  if (!expr) {
    form.value.ruleType = 'none'
    return
  }
  
  const parts = expr.split(';')
  parts.forEach(part => {
    part = part.trim()
    if (part.startsWith('week:')) {
      selectedWeekDays.value = part.replace('week:', '').split(',').filter(Boolean)
    } else if (part.startsWith('month:')) {
      selectedMonthDays.value = part.replace('month:', '').split(',').filter(Boolean)
    }
  })
  
  if (selectedWeekDays.value.length > 0 && selectedMonthDays.value.length > 0) {
    form.value.ruleType = 'both'
  } else if (selectedWeekDays.value.length > 0) {
    form.value.ruleType = 'weekly'
  } else if (selectedMonthDays.value.length > 0) {
    form.value.ruleType = 'monthly'
  } else {
    form.value.ruleType = 'none'
  }
}

const buildRuleExpression = () => {
  const parts: string[] = []
  if (selectedWeekDays.value.length > 0) {
    parts.push(`week:${selectedWeekDays.value.join(',')}`)
  }
  if (selectedMonthDays.value.length > 0) {
    parts.push(`month:${selectedMonthDays.value.join(',')}`)
  }
  return parts.join(';')
}

const fetchActivities = async () => {
  loading.value = true
  try {
    activities.value = await getMemberDayActivities()
  } catch (error) {
    console.error('获取会员日活动失败', error)
    ElMessage.error('获取会员日活动失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增会员日活动'
  form.value = {
    name: '',
    type: 1,
    status: 1,
    activityType: 1,
    discountRate: 0.85,
    pointsMultiplier: 2.0,
    startTime: '',
    endTime: '',
    description: '',
    ruleExpression: '',
    ruleType: 'none',
    couponType: 1,
    couponThreshold: 0,
    couponAmount: 0,
    couponTotal: 100
  }
  selectedWeekDays.value = []
  selectedMonthDays.value = []
  dialogVisible.value = true
}

const handleEdit = (row: Activity) => {
  dialogTitle.value = '编辑会员日活动'
  form.value = { ...row, ruleType: 'none' }
  parseRuleExpression(row.ruleExpression || '')
  dialogVisible.value = true
}

const handleDelete = async (row: Activity) => {
  try {
    await ElMessageBox.confirm('确定要删除该活动吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteActivity(row.id)
    ElMessage.success('删除成功')
    fetchActivities()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleStatusChange = async (row: Activity) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    await updateActivityStatus(row.id, newStatus)
    ElMessage.success(newStatus === 1 ? '活动已启用' : '活动已停用')
    fetchActivities()
  } catch (error) {
    ElMessage.error('状态更新失败')
  }
}

const handleSubmit = async () => {
  await formRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      try {
        form.value.ruleExpression = buildRuleExpression()
        if (form.value.id) {
          await updateActivity(form.value.id, form.value)
          ElMessage.success('更新成功')
        } else {
          await createActivity(form.value)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchActivities()
      } catch (error) {
        ElMessage.error('保存失败')
      }
    }
  })
}

const formatTime = (time: string) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

const formatRuleExpression = (expr: string) => {
  if (!expr) return '-'
  const parts: string[] = []
  const segments = expr.split(';')
  segments.forEach(segment => {
    segment = segment.trim()
    if (segment.startsWith('week:')) {
      const days = segment.replace('week:', '').split(',').map(v => weekDays.find(d => d.value === v)?.label).filter(Boolean).join('、')
      if (days) parts.push(`每${days}`)
    } else if (segment.startsWith('month:')) {
      const days = segment.replace('month:', '').split(',').map(v => `${v}号`).join('、')
      if (days) parts.push(`每月${days}`)
    }
  })
  return parts.join('；') || expr
}

onMounted(() => {
  fetchActivities()
})
</script>

<template>
  <div class="member-days-page">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">会员日活动管理</h2>
        <p class="page-subtitle">管理会员日促销活动，设置折扣率和积分倍率</p>
      </div>
      <el-button type="primary" @click="handleAdd">
        <el-icon class="mr-2"><Plus /></el-icon>
        新增活动
      </el-button>
    </div>

    <el-card class="table-card" v-loading="loading">
      <el-table :data="activities" stripe>
        <el-table-column prop="name" label="活动名称" min-width="150" />
        <el-table-column label="活动类别" width="100">
          <template #default="{ row }">
            <el-tag :type="row.activityType === 2 ? 'warning' : 'primary'" size="small">
              {{ row.activityType === 2 ? '优惠券' : '全场折扣' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="活动时间" min-width="200">
          <template #default="{ row }">
            <div class="time-range">
              {{ formatTime(row.startTime) }} ~ {{ formatTime(row.endTime) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="discountRate" label="折扣/优惠" width="120">
          <template #default="{ row }">
            <span v-if="row.activityType === 1">{{ row.discountRate ? `${(row.discountRate * 10).toFixed(1)}折` : '-' }}</span>
            <span v-else-if="row.activityType === 2 && row.couponAmount">
              {{ row.couponType === 1 ? `满${row.couponThreshold}减${row.couponAmount}` : `无门槛减${row.couponAmount}` }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="ruleExpression" label="规则表达式" width="180">
          <template #default="{ row }">
            {{ formatRuleExpression(row.ruleExpression) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              <el-icon class="mr-1"><Edit /></el-icon>
              编辑
            </el-button>
            <el-button 
              :type="row.status === 1 ? 'warning' : 'success'" 
              link 
              size="small" 
              @click="handleStatusChange(row)"
            >
              {{ row.status === 1 ? '停用' : '启用' }}
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
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="活动名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="选择开始时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="选择结束时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="活动类别" prop="activityType">
          <el-radio-group v-model="form.activityType">
            <el-radio :value="1">全场折扣</el-radio>
            <el-radio :value="2">优惠券</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.activityType === 1" label="折扣率">
          <el-input-number v-model="form.discountRate" :min="0.1" :max="1" :step="0.05" :precision="2" />
          <span class="ml-2 text-gray-500">{{ form.discountRate ? `${(form.discountRate * 10).toFixed(1)}折` : '' }}</span>
        </el-form-item>
        <template v-if="form.activityType === 2">
          <el-form-item label="优惠券类型">
            <el-radio-group v-model="form.couponType">
              <el-radio :value="1">满减券</el-radio>
              <el-radio :value="2">无门槛券</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="form.couponType === 1" label="门槛金额">
            <el-input-number v-model="form.couponThreshold" :min="1" :max="10000" :precision="2" />
            <span class="ml-2 text-gray-500">满{{ form.couponThreshold || 0 }}元可用</span>
          </el-form-item>
          <el-form-item label="减免金额">
            <el-input-number v-model="form.couponAmount" :min="1" :max="1000" :precision="2" />
            <span class="ml-2 text-gray-500">减{{ form.couponAmount || 0 }}元</span>
          </el-form-item>
          <el-form-item label="发行总量">
            <el-input-number v-model="form.couponTotal" :min="1" :max="100000" />
            <span class="ml-2 text-gray-500">张</span>
          </el-form-item>
        </template>
        <el-form-item label="规则类型">
          <el-radio-group v-model="form.ruleType">
            <el-radio value="none">无固定规则</el-radio>
            <el-radio value="weekly">每周特定日期</el-radio>
            <el-radio value="monthly">每月特定日期</el-radio>
            <el-radio value="both">周和月组合</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.ruleType === 'weekly' || form.ruleType === 'both'" label="每周日期">
          <el-checkbox-group v-model="selectedWeekDays">
            <el-checkbox v-for="day in weekDays" :key="day.value" :value="day.value">
              {{ day.label }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item v-if="form.ruleType === 'monthly' || form.ruleType === 'both'" label="每月日期">
          <el-select v-model="selectedMonthDays" multiple placeholder="选择每月特定日期" style="width: 100%">
            <el-option v-for="day in monthDays" :key="day.value" :label="day.label" :value="day.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="规则预览">
          <span class="rule-preview">{{ ruleExpressionDisplay }}</span>
        </el-form-item>
        <el-form-item label="活动描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入活动描述" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
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
.member-days-page {
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

.time-range {
  font-size: 13px;
  color: #4b5563;
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

.rule-preview {
  color: #1677ff;
  font-weight: 500;
}
</style>
