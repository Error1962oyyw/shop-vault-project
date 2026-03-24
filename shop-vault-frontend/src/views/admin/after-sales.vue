<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminAfterSalesList, resolveAfterSales } from '@/api/order'
import type { AfterSales } from '@/types/api'

const loading = ref(false)
const afterSalesList = ref<AfterSales[]>([])
const activeTab = ref('pending')

const fetchAfterSales = async () => {
  loading.value = true
  try {
    afterSalesList.value = await getAdminAfterSalesList()
  } catch (error) {
    console.error('获取售后列表失败', error)
  } finally {
    loading.value = false
  }
}

const handleResolve = async (item: AfterSales, agree: boolean) => {
  const action = agree ? '同意' : '拒绝'
  try {
    const { value: refundAmount } = agree ? await ElMessageBox.prompt(
      '请输入退款金额',
      '同意售后',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /^\d+(\.\d{1,2})?$/,
        inputErrorMessage: '请输入正确的金额',
        inputValue: String(item.refundAmount)
      }
    ) : await ElMessageBox.confirm(
      '确定要拒绝该售后申请吗？',
      '拒绝售后',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await resolveAfterSales({
      id: item.id,
      status: agree ? 1 : 2,
      refundAmount: agree ? Number(refundAmount) : undefined
    })
    
    ElMessage.success(`${action}成功`)
    fetchAfterSales()
  } catch (error) {
    // 用户取消
  }
}

const getTypeText = (type: number) => {
  return type === 1 ? '仅退款' : '退货退款'
}

const getStatusText = (status: number) => {
  const texts: Record<number, string> = {
    0: '待处理',
    1: '已同意',
    2: '已拒绝'
  }
  return texts[status] || '未知'
}

const getStatusColor = (status: number): 'success' | 'warning' | 'danger' | 'info' => {
  const colors: Record<number, 'success' | 'warning' | 'danger' | 'info'> = {
    0: 'warning',
    1: 'success',
    2: 'danger'
  }
  return colors[status] || 'info'
}

onMounted(() => {
  fetchAfterSales()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">售后管理</h2>
        <p class="page-subtitle">处理用户的售后申请</p>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="custom-tabs">
      <el-tab-pane label="待处理" name="pending" />
      <el-tab-pane label="已处理" name="processed" />
    </el-tabs>

    <div class="content-card">
      <el-table 
        :data="afterSalesList.filter(item => activeTab === 'pending' ? item.status === 0 : item.status !== 0)" 
        v-loading="loading"
        stripe
      >
        <el-table-column prop="orderNo" label="订单编号" width="200" />
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ getTypeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="原因" min-width="150" />
        <el-table-column label="退款金额" width="100" align="center">
          <template #default="{ row }">
            <span class="refund-amount">¥{{ row.refundAmount.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="180" />
        <el-table-column label="操作" width="150" align="center">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button type="success" link size="small" @click="handleResolve(row, true)">
                同意
              </el-button>
              <el-button type="danger" link size="small" @click="handleResolve(row, false)">
                拒绝
              </el-button>
            </template>
            <template v-else>
              <span class="handled-text">{{ row.handleRemark || '已处理' }}</span>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<style scoped>
.page-container {
  padding: 24px;
  background: linear-gradient(135deg, #f7f8fa 0%, #fff7e6 50%, #fffbf0 100%);
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

.custom-tabs {
  margin-bottom: 20px;
  background: #fff;
  padding: 0 16px;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

:deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background: #e5e6eb;
}

:deep(.el-tabs__item) {
  font-size: 15px;
  color: #6b7280;
  padding: 0 20px;
  height: 48px;
  line-height: 48px;
}

:deep(.el-tabs__item.is-active) {
  color: #1677ff;
  font-weight: 600;
}

:deep(.el-tabs__active-bar) {
  height: 3px;
  border-radius: 3px;
}

.content-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.refund-amount {
  color: #ff4d4f;
  font-weight: 700;
  font-size: 15px;
}

.handled-text {
  font-size: 13px;
  color: #6b7280;
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
  padding: 16px 0;
}
</style>
