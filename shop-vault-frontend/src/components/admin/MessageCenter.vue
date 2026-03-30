<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Bell, Check, Delete } from '@element-plus/icons-vue'
import { 
  getAdminMessages, 
  getAdminUnreadCount, 
  markMessageAsRead, 
  markAllMessagesAsRead,
  type AdminMessage 
} from '@/api/admin'
import { ElMessage } from 'element-plus'

const router = useRouter()
const visible = defineModel<boolean>('visible')

const messages = ref<AdminMessage[]>([])
const unreadCount = ref(0)
const loading = ref(false)
const pollingInterval = ref<number | null>(null)

const displayCount = computed(() => {
  if (unreadCount.value > 99) return '99+'
  return unreadCount.value.toString()
})

const fetchMessages = async () => {
  loading.value = true
  try {
    messages.value = await getAdminMessages()
  } catch (error) {
    console.error('获取消息失败', error)
  } finally {
    loading.value = false
  }
}

const fetchUnreadCount = async () => {
  try {
    const result = await getAdminUnreadCount()
    unreadCount.value = result.count
  } catch (error) {
    console.error('获取未读数量失败', error)
  }
}

const handleMessageClick = async (message: AdminMessage) => {
  if (message.status !== 2) {
    try {
      await markMessageAsRead(message.id)
      message.status = 2
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (error) {
      console.error('标记已读失败', error)
    }
  }
  
  visible.value = false
  
  if (message.linkUrl) {
    router.push(message.linkUrl)
  }
}

const handleMarkAllRead = async () => {
  try {
    await markAllMessagesAsRead()
    messages.value.forEach(msg => msg.status = 2)
    unreadCount.value = 0
    ElMessage.success('全部消息已标记为已读')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const formatTime = (timeStr: string) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return date.toLocaleDateString()
}

const startPolling = () => {
  pollingInterval.value = window.setInterval(() => {
    fetchUnreadCount()
  }, 30000)
}

const stopPolling = () => {
  if (pollingInterval.value) {
    clearInterval(pollingInterval.value)
    pollingInterval.value = null
  }
}

onMounted(() => {
  fetchUnreadCount()
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})

defineExpose({
  fetchMessages,
  fetchUnreadCount
})
</script>

<template>
  <el-popover
    v-model:visible="visible"
    placement="bottom-end"
    :width="380"
    trigger="click"
    @show="fetchMessages"
  >
    <template #reference>
      <el-badge 
        :value="displayCount" 
        :hidden="unreadCount === 0 || visible"
        :max="99"
        class="message-badge"
      >
        <el-button circle>
          <el-icon><Bell /></el-icon>
        </el-button>
      </el-badge>
    </template>

    <div class="message-center">
      <div class="message-header">
        <span class="message-title">消息中心</span>
        <el-button 
          v-if="unreadCount > 0"
          type="primary" 
          link 
          size="small"
          @click="handleMarkAllRead"
        >
          <el-icon><Check /></el-icon>
          全部已读
        </el-button>
      </div>

      <el-divider style="margin: 12px 0" />

      <div v-loading="loading" class="message-list">
        <div v-if="messages.length === 0" class="empty-message">
          <el-empty description="暂无消息" :image-size="80" />
        </div>

        <div
          v-for="message in messages"
          :key="message.id"
          class="message-item"
          :class="{ 'message-unread': message.status !== 2 }"
          @click="handleMessageClick(message)"
        >
          <div class="message-dot" v-if="message.status !== 2"></div>
          <div class="message-content">
            <div class="message-text">{{ message.content }}</div>
            <div class="message-time">{{ formatTime(message.createTime) }}</div>
          </div>
          <el-icon class="message-arrow"><Delete /></el-icon>
        </div>
      </div>
    </div>
  </el-popover>
</template>

<style scoped>
.message-badge {
  display: inline-flex;
}

.message-center {
  max-height: 450px;
  display: flex;
  flex-direction: column;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 4px;
}

.message-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  max-height: 350px;
}

.empty-message {
  padding: 20px 0;
}

.message-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

.message-item:hover {
  background: #f5f7fa;
}

.message-unread {
  background: #ecf5ff;
}

.message-unread:hover {
  background: #d9ecff;
}

.message-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #f56c6c;
  flex-shrink: 0;
  margin-top: 6px;
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-text {
  font-size: 14px;
  color: #303133;
  line-height: 1.5;
  word-break: break-all;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.message-arrow {
  color: #c0c4cc;
  font-size: 14px;
  flex-shrink: 0;
  margin-top: 4px;
}
</style>
