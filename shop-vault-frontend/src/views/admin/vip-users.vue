<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { AdminPageLayout } from '@/components/admin'
import { usePagination } from '@/composables'
import { getVipUsers, updateVipLevel, type VipUser } from '@/api/admin'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const vipUsers = ref<VipUser[]>([])
const searchKeyword = ref('')

const { pagination, handleCurrentChange, setTotal } = usePagination({
  onPageChange: () => fetchVipUsers()
})

const vipLevels = [
  { value: 0, label: '普通用户', color: 'info' },
  { value: 1, label: 'VIP会员', color: 'warning' },
  { value: 2, label: 'SVIP会员', color: 'danger' }
]

const adjustDialogVisible = ref(false)
const adjustForm = ref({
  userId: 0,
  vipLevel: 0,
  days: 1
})

const fetchVipUsers = async () => {
  loading.value = true
  try {
    const res = await getVipUsers({
      pageNum: pagination.current,
      pageSize: pagination.size
    })
    vipUsers.value = res.records
    setTotal(res.total)
  } catch (error) {
    console.error('获取VIP用户列表失败', error)
    ElMessage.error('获取VIP用户列表失败')
  } finally {
    loading.value = false
  }
}

const getVipLevelLabel = (level: number) => {
  const found = vipLevels.find(v => v.value === level)
  return found ? found.label : '未知'
}

const getVipLevelColor = (level: number): 'info' | 'warning' | 'danger' => {
  const colors: Record<number, 'info' | 'warning' | 'danger'> = {
    0: 'info',
    1: 'warning',
    2: 'danger'
  }
  return colors[level] || 'info'
}

const formatDate = (date: string | null) => {
  if (!date) return '-'
  return date.split('T')[0]
}

const isExpired = (expireTime: string | null) => {
  if (!expireTime) return true
  return new Date(expireTime) < new Date()
}

const getStatusInfo = (row: VipUser) => {
  if (row.vipLevel === 0) {
    return { text: '已过期', type: 'info' as const }
  }
  if (isExpired(row.vipExpireTime)) {
    return { text: '已过期', type: 'info' as const }
  }
  return { text: '生效中', type: 'success' as const }
}

const handleSearch = () => {
  pagination.current = 1
  fetchVipUsers()
}

const openAdjustDialog = (row: VipUser) => {
  adjustForm.value = {
    userId: row.userId,
    vipLevel: row.vipLevel,
    days: 1
  }
  adjustDialogVisible.value = true
}

const handleAdjustConfirm = async () => {
  try {
    const days = adjustForm.value.vipLevel === 0 ? null : adjustForm.value.days
    await updateVipLevel(adjustForm.value.userId, adjustForm.value.vipLevel, days)
    ElMessage.success('调整成功')
    adjustDialogVisible.value = false
    fetchVipUsers()
  } catch (error) {
    ElMessage.error('调整失败')
  }
}

const getAvatarUrl = (row: VipUser) => {
  if (row.avatar) return row.avatar
  return undefined
}

const getDisplayName = (row: VipUser) => {
  return row.nickname || row.username || `用户${row.userId}`
}

onMounted(() => {
  fetchVipUsers()
})
</script>

<template>
  <AdminPageLayout title="VIP会员管理" subtitle="管理会员信息与权益配置">
    <template #actions>
      <el-input 
        v-model="searchKeyword"
        placeholder="搜索用户名/手机号" 
        class="search-input"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </template>

    <el-table :data="vipUsers" v-loading="loading" stripe>
      <el-table-column label="用户信息" min-width="200">
        <template #default="{ row }">
          <div class="user-info">
            <el-avatar :size="40" :src="getAvatarUrl(row)">
              {{ getDisplayName(row).charAt(0) }}
            </el-avatar>
            <div class="user-details">
              <h4 class="user-name">{{ getDisplayName(row) }}</h4>
              <p class="user-id">ID: {{ row.userId }}</p>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="会员等级" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="getVipLevelColor(row.vipLevel)" size="small">
            {{ getVipLevelLabel(row.vipLevel) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="折扣率" width="100" align="center">
        <template #default="{ row }">
          <span class="discount-rate">{{ (row.discountRate * 10).toFixed(1) }}折</span>
        </template>
      </el-table-column>
      <el-table-column label="到期时间" width="140" align="center">
        <template #default="{ row }">
          <span :class="['expire-time', { expired: isExpired(row.vipExpireTime) }]">
            {{ row.vipLevel === 0 ? '-' : formatDate(row.vipExpireTime) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusInfo(row).type" size="small">
            {{ getStatusInfo(row).text }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" align="center">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="openAdjustDialog(row)">调整</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="adjustDialogVisible" title="调整会员权益" width="400px">
      <el-form :model="adjustForm" label-width="100px">
        <el-form-item label="会员等级">
          <el-select v-model="adjustForm.vipLevel" placeholder="请选择会员等级">
            <el-option
              v-for="level in vipLevels"
              :key="level.value"
              :label="level.label"
              :value="level.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="adjustForm.vipLevel > 0" label="会员天数">
          <el-input-number 
            v-model="adjustForm.days" 
            :min="1" 
            :max="10000" 
            :step="1"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdjustConfirm">确认</el-button>
      </template>
    </el-dialog>

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
</template>

<style scoped>
.search-input {
  width: 260px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info .el-avatar {
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
  color: white;
  font-weight: 600;
  flex-shrink: 0;
}

.user-details {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 2px 0;
}

.user-id {
  font-size: 12px;
  color: #9ca3af;
  margin: 0;
}

.discount-rate {
  font-size: 14px;
  font-weight: 600;
  color: #fa8c16;
}

.expire-time {
  font-size: 14px;
}

.expire-time.expired {
  color: #f56c6c;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 16px 0 0 0;
}

:deep(.el-table) {
  border-radius: 16px;
  overflow: hidden;
}

:deep(.el-table th) {
  background: #f8fafc;
  color: #374151;
  font-weight: 600;
  font-size: 14px;
}

:deep(.el-table td) {
  padding: 14px 0;
}
</style>
