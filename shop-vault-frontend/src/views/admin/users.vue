<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, updateUserStatus } from '@/api/admin'
import { Search, Lock, Unlock } from '@element-plus/icons-vue'
import { usePagination } from '@/composables'
import { AdminPageLayout } from '@/components/admin'
import { getAvatarUrl } from '@/utils/format'

interface UserItem {
  id: number
  username: string
  nickname: string
  email: string
  phone: string
  avatar: string
  role: string
  status: number
  points: number
  createTime: string
}

const loading = ref(false)
const users = ref<UserItem[]>([])
const searchKeyword = ref('')

const { pagination, handleCurrentChange, setTotal, getParams } = usePagination({
  onPageChange: () => fetchUsers()
})

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await getUserList({
      ...getParams(),
      keyword: searchKeyword.value
    })
    users.value = res.records
    setTotal(res.total)
  } catch (error) {
    console.error('获取用户列表失败', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchUsers()
}

const handleToggleStatus = async (user: UserItem) => {
  const action = user.status === 1 ? '暂停' : '启用'
  try {
    await ElMessageBox.confirm(
      `确定要${action}用户"${user.nickname || user.username}"吗？`,
      `${action}用户`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const newStatus = user.status === 1 ? 0 : 1
    await updateUserStatus(user.id, newStatus)
    ElMessage.success(`${action}成功`)
    fetchUsers()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error(`${action}失败`, error)
    }
  }
}

const getStatusText = (status: number) => {
  return status === 1 ? '正常' : '已暂停'
}

const getStatusType = (status: number): 'success' | 'danger' => {
  return status === 1 ? 'success' : 'danger'
}

const getRoleText = (role: string) => {
  return role === 'ADMIN' ? '管理员' : '普通用户'
}

const getRoleType = (role: string): 'warning' | 'info' => {
  return role === 'ADMIN' ? 'warning' : 'info'
}

onMounted(() => {
  fetchUsers()
})
</script>

<template>
  <AdminPageLayout title="用户管理" subtitle="管理系统用户账号状态" background="green">
    <template #actions>
      <el-input
        v-model="searchKeyword"
        placeholder="搜索用户名/昵称/邮箱"
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

    <el-table :data="users" v-loading="loading" stripe>
      <el-table-column label="用户信息" min-width="200">
        <template #default="{ row }">
          <div class="user-info">
            <el-avatar :size="40" :src="getAvatarUrl(row.avatar)" class="user-avatar">
              {{ (row.nickname || row.username)?.charAt(0) }}
            </el-avatar>
            <div class="user-details">
              <p class="user-name">{{ row.nickname || row.username }}</p>
              <p class="user-email">{{ row.email }}</p>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column label="角色" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getRoleType(row.role)" size="small">
            {{ getRoleText(row.role) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="points" label="积分" width="80" align="center" />
      <el-table-column prop="createTime" label="注册时间" width="180" />
      <el-table-column label="操作" width="120" align="center">
        <template #default="{ row }">
          <el-button
            v-if="row.role !== 'ADMIN'"
            :type="row.status === 1 ? 'danger' : 'success'"
            link
            size="small"
            @click="handleToggleStatus(row)"
          >
            <el-icon class="mr-1">
              <Lock v-if="row.status === 1" />
              <Unlock v-else />
            </el-icon>
            {{ row.status === 1 ? '暂停' : '启用' }}
          </el-button>
          <span v-else class="text-gray-400">-</span>
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
  width: 280px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
  color: white;
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
  margin: 0 0 4px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-email {
  font-size: 12px;
  color: #6b7280;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 16px 0 0 0;
}

.text-gray-400 {
  color: #9ca3af;
}

.mr-1 {
  margin-right: 4px;
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
  padding: 16px 0;
}
</style>
