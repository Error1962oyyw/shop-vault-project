<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import UserLayout from '@/components/layout/UserLayout.vue'
import { getMyMessages, markMessageRead } from '@/api/message'
import { ElMessage } from 'element-plus'
import { Bell, Document, Present, Ticket, ChatDotRound } from '@element-plus/icons-vue'
import type { Component } from 'vue'
import type { MessagePush } from '@/types/api'

const messages = ref<MessagePush[]>([])
const loading = ref(false)
const activeType = ref<number | undefined>(undefined)

const messageTypes = [
  { label: '全部消息', value: undefined },
  { label: '系统通知', value: 1 },
  { label: '订单消息', value: 2 },
  { label: '活动通知', value: 3 },
  { label: '优惠提醒', value: 4 }
]

const filteredMessages = computed(() => {
  if (activeType.value === undefined) {
    return messages.value
  }
  return messages.value.filter(m => m.type === activeType.value)
})

const unreadCount = computed(() => {
  return messages.value.filter(m => !m.isRead).length
})

const fetchMessages = async () => {
  try {
    loading.value = true
    messages.value = await getMyMessages()
  } catch (error) {
    console.error('获取消息列表失败', error)
  } finally {
    loading.value = false
  }
}

const handleReadMessage = async (message: MessagePush) => {
  if (message.isRead) return
  
  try {
    await markMessageRead(message.id)
    message.isRead = true
    ElMessage.success('已标记为已读')
  } catch (error) {
    console.error('标记已读失败', error)
  }
}

const handleReadAll = async () => {
  try {
    const unreadMessages = messages.value.filter(m => !m.isRead)
    await Promise.all(unreadMessages.map(m => markMessageRead(m.id)))
    messages.value.forEach(m => m.isRead = true)
    ElMessage.success('已全部标记为已读')
  } catch (error) {
    console.error('全部标记已读失败', error)
  }
}

const getMessageTypeText = (type: number) => {
  switch (type) {
    case 1: return '系统通知'
    case 2: return '订单消息'
    case 3: return '活动通知'
    case 4: return '优惠提醒'
    default: return '通知'
  }
}

const getMessageTypeClass = (type: number) => {
  switch (type) {
    case 1: return 'bg-blue-100 text-blue-600'
    case 2: return 'bg-green-100 text-green-600'
    case 3: return 'bg-purple-100 text-purple-600'
    case 4: return 'bg-orange-100 text-orange-600'
    default: return 'bg-gray-100 text-gray-600'
  }
}

const getMessageTypeIcon = (type: number): Component => {
  const icons: Record<number, Component> = {
    1: Bell,
    2: Document,
    3: Present,
    4: Ticket
  }
  return icons[type] || ChatDotRound
}

const formatTime = (date: string) => {
  if (!date) return ''
  const d = new Date(date)
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

onMounted(() => {
  fetchMessages()
})
</script>

<template>
  <UserLayout>
    <div class="bg-gray-50 min-h-screen py-6 animate-fade-in">
      <div class="page-container">
        <div class="bg-white rounded-lg shadow-sm">
          <div class="p-6 border-b flex items-center justify-between">
            <div>
              <h1 class="text-2xl font-bold text-gray-800">消息中心</h1>
              <p class="text-gray-500 mt-1">
                共 {{ messages.length }} 条消息
                <span v-if="unreadCount > 0" class="text-red-500 ml-2">
                  {{ unreadCount }} 条未读
                </span>
              </p>
            </div>
            <el-button 
              v-if="unreadCount > 0"
              type="primary" 
              plain
              @click="handleReadAll"
            >
              全部标记已读
            </el-button>
          </div>

          <div class="p-4 border-b">
            <el-radio-group v-model="activeType" size="small">
              <el-radio-button 
                v-for="type in messageTypes" 
                :key="type.label"
                :value="type.value"
              >
                {{ type.label }}
              </el-radio-button>
            </el-radio-group>
          </div>

          <div v-loading="loading" class="divide-y">
            <div 
              v-for="message in filteredMessages" 
              :key="message.id"
              class="p-4 hover:bg-gray-50 cursor-pointer transition-colors"
              :class="{ 'bg-blue-50/50': !message.isRead }"
              @click="handleReadMessage(message)"
            >
              <div class="flex items-start gap-4">
                <div 
                  class="w-10 h-10 rounded-full flex items-center justify-center flex-shrink-0"
                  :class="getMessageTypeClass(message.type)"
                >
                  <el-icon size="20">
                    <component :is="getMessageTypeIcon(message.type)" />
                  </el-icon>
                </div>
                
                <div class="flex-1 min-w-0">
                  <div class="flex items-center gap-2 mb-1">
                    <span 
                      class="text-xs px-2 py-0.5 rounded"
                      :class="getMessageTypeClass(message.type)"
                    >
                      {{ getMessageTypeText(message.type) }}
                    </span>
                    <span v-if="!message.isRead" class="w-2 h-2 bg-red-500 rounded-full"></span>
                  </div>
                  
                  <h3 
                    class="font-medium text-gray-800 truncate"
                    :class="{ 'font-bold': !message.isRead }"
                  >
                    {{ message.title }}
                  </h3>
                  
                  <p class="text-sm text-gray-500 mt-1 line-clamp-2">
                    {{ message.content }}
                  </p>
                  
                  <div class="text-xs text-gray-400 mt-2">
                    {{ formatTime(message.createTime) }}
                  </div>
                </div>
              </div>
            </div>

            <el-empty 
              v-if="!loading && filteredMessages.length === 0" 
              description="暂无消息"
              class="py-12"
            />
          </div>
        </div>
      </div>
    </div>
  </UserLayout>
</template>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
