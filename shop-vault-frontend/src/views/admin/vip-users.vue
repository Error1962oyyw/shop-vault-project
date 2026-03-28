<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Medal, Search } from '@element-plus/icons-vue'
import { AdminPageLayout } from '@/components/admin'
import { usePagination } from '@/composables'
import { getVipUsers, extendVip, updateVipLevel, type VipUser } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

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

const handleSearch = () => {
  pagination.current = 1
  fetchVipUsers()
}

const handleExtend = async (row: VipUser) => {
  try {
    const { value: days } = await ElMessageBox.prompt('请输入延期天数', '延期VIP会员', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /^[1-9]\d*$/,
      inputErrorMessage: '请输入有效的天数（正整数）'
    })
    await extendVip(row.userId, parseInt(days))
    ElMessage.success('延期成功')
    fetchVipUsers()
  } catch {
    // 用户取消
  }
}

const handleAdjustLevel = async (row: VipUser) => {
  try {
    const { value: level } = await ElMessageBox.prompt('请输入会员等级（0-普通 1-VIP 2-SVIP）', '调整会员等级', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /^[0-2]$/,
      inputErrorMessage: '请输入有效的等级（0、1或2）'
    })
    await updateVipLevel(row.userId, parseInt(level))
    ElMessage.success('等级调整成功')
    fetchVipUsers()
  } catch {
    // 用户取消
  }
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
            <div class="user-avatar">
              <Medal class="avatar-icon" />
            </div>
            <div class="user-details">
              <h4 class="user-name">{{ row.username }}</h4>
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
            {{ formatDate(row.vipExpireTime) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="totalVipDays" label="累计天数" width="100" align="center" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="isExpired(row.vipExpireTime) ? 'info' : 'success'" size="small">
            {{ isExpired(row.vipExpireTime) ? '已过期' : '生效中' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="center">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleExtend(row)">延期</el-button>
          <el-button type="warning" link size="small" @click="handleAdjustLevel(row)">调整</el-button>
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

.user-avatar {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  background: linear-gradient(135deg, #f0e6ff 0%, #d3adf7 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.avatar-icon {
  font-size: 20px;
  color: #722ed1;
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
