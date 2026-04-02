<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminOrderList, shipOrder } from '@/api/order'
import type { Order, PageResult } from '@/types/api'

const loading = ref(false)
const orders = ref<Order[]>([])
const activeTab = ref('all')
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const tabs = [
  { label: '全部订单', value: 'all', status: undefined },
  { label: '待发货', value: 'unshipped', status: 1 },
  { label: '已发货', value: 'shipped', status: 2 },
  { label: '已完成', value: 'completed', status: 3 },
  { label: '已取消', value: 'cancelled', status: 4 }
]

const fetchOrders = async () => {
  loading.value = true
  try {
    const tab = tabs.find(t => t.value === activeTab.value)
    const res: PageResult<Order> = await getAdminOrderList({
      status: tab?.status,
      pageNum: pagination.current,
      pageSize: pagination.size
    })
    orders.value = res.records
    pagination.total = res.total
  } catch (error: any) {
    console.error('获取订单失败', error)
    const msg = error?.response?.data?.message || error?.message || '获取订单列表失败，请稍后重试'
    ElMessage.error(msg)
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  pagination.current = 1
  fetchOrders()
}

const handleCurrentChange = (val: number) => {
  pagination.current = val
  fetchOrders()
}

const handleShip = async (orderNo: string) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入物流单号', '发货', {
      confirmButtonText: '确定发货',
      cancelButtonText: '取消',
      inputPattern: /^[\w\d]{5,30}$/,
      inputErrorMessage: '请输入正确的物流单号(5-30位字符)'
    })
    await shipOrder(orderNo, { trackingCompany: '通用物流', trackingNo: value })
    ElMessage.success('发货成功')
    fetchOrders()
  } catch (error) {
    // 用户取消或请求失败
  }
}

const getStatusColor = (status: number): 'success' | 'primary' | 'warning' | 'info' | 'danger' => {
  const colors: Record<number, 'success' | 'primary' | 'warning' | 'info' | 'danger'> = {
    0: 'warning',
    1: 'primary',
    2: 'primary',
    3: 'success',
    4: 'info',
    5: 'danger'
  }
  return colors[status] || 'info'
}

onMounted(() => {
  fetchOrders()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">订单管理</h2>
        <p class="page-subtitle">查看和处理所有订单</p>
      </div>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="custom-tabs">
      <el-tab-pane 
        v-for="tab in tabs" 
        :key="tab.value"
        :label="tab.label"
        :name="tab.value"
      />
    </el-tabs>

    <div class="content-card">
      <el-table :data="orders" v-loading="loading" stripe>
        <el-table-column prop="orderNo" label="订单编号" width="200" />
        <el-table-column label="商品信息" min-width="250">
          <template #default="{ row }">
            <div class="order-items">
              <img 
                v-for="item in row.items.slice(0, 2)" 
                :key="item.id"
                :src="item.productImage" 
                :alt="item.productName"
                class="item-image"
              />
              <span v-if="row.items.length > 2" class="more-items">
                +{{ row.items.length - 2 }}
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="收货人" width="120">
          <template #default="{ row }">
            <div class="receiver-info">
              <p class="receiver-name">{{ row.receiverName }}</p>
              <p class="receiver-phone">{{ row.receiverPhone }}</p>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="100">
          <template #default="{ row }">
            <span class="amount">¥{{ row.payAmount.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)" size="small">
              {{ row.statusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="180" />
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <template v-if="row.status === 1">
              <el-button type="primary" link size="small" @click="handleShip(row.orderNo)">
                发货
              </el-button>
            </template>
            <template v-else>
              <el-button type="primary" link size="small">
                详情
              </el-button>
            </template>
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
    </div>
  </div>
</template>

<style scoped>
.page-container {
  padding: 24px;
  background: linear-gradient(135deg, #f7f8fa 0%, #f0f5ff 50%, #f7f8fa 100%);
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

.order-items {
  display: flex;
  align-items: center;
  gap: 8px;
}

.item-image {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  object-fit: cover;
}

.more-items {
  font-size: 12px;
  color: #6b7280;
  background: #f3f4f6;
  padding: 2px 8px;
  border-radius: 6px;
}

.receiver-info {
  text-align: left;
}

.receiver-name {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  margin: 0 0 2px 0;
}

.receiver-phone {
  font-size: 12px;
  color: #6b7280;
  margin: 0;
}

.amount {
  color: #ff4d4f;
  font-weight: 700;
  font-size: 15px;
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
