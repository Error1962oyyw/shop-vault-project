<script setup lang="ts">
import { ref, onMounted, nextTick, computed } from 'vue'
import { getChatUsers, getAdminChatHistory, adminReply, adminReplyImage, type ChatUser } from '@/api/chat'
import { useUserStore } from '@/stores/user'
import type { ChatMessage } from '@/types/api'
import { ElMessage } from 'element-plus'
import { Picture, Search, ChatDotRound } from '@element-plus/icons-vue'

const userStore = useUserStore()

const chatUsers = ref<ChatUser[]>([])
const selectedUser = ref<ChatUser | null>(null)
const messages = ref<ChatMessage[]>([])
const inputMessage = ref('')
const loading = ref(false)
const sending = ref(false)
const messagesContainer = ref<HTMLElement>()
const searchKeyword = ref('')
const imageInputRef = ref<HTMLInputElement | null>(null)

const filteredUsers = computed(() => {
  if (!searchKeyword.value.trim()) return chatUsers.value
  const keyword = searchKeyword.value.toLowerCase()
  return chatUsers.value.filter(user => 
    user.username.toLowerCase().includes(keyword) ||
    user.nickname?.toLowerCase().includes(keyword) ||
    user.id.toString().includes(keyword)
  )
})

const totalUnreadCount = computed(() => {
  return chatUsers.value.reduce((sum, user) => sum + user.unreadCount, 0)
})

const fetchChatUsers = async () => {
  try {
    chatUsers.value = await getChatUsers()
  } catch (error) {
    console.error('获取用户列表失败', error)
  }
}

const selectUser = async (user: ChatUser) => {
  selectedUser.value = user
  await fetchMessages()
}

const fetchMessages = async () => {
  if (!selectedUser.value) return
  
  loading.value = true
  try {
    messages.value = await getAdminChatHistory(selectedUser.value.id)
    scrollToBottom()
    await fetchChatUsers()
  } catch (error) {
    console.error('获取聊天记录失败', error)
  } finally {
    loading.value = false
  }
}

const handleSend = async () => {
  if (!inputMessage.value.trim() || !selectedUser.value) return
  
  const content = inputMessage.value.trim()
  inputMessage.value = ''
  
  sending.value = true
  try {
    await adminReply({ receiverId: selectedUser.value.id, content })
    await fetchMessages()
  } catch (error) {
    console.error('发送消息失败', error)
    ElMessage.error('发送失败')
  } finally {
    sending.value = false
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const formatTime = (time: string) => {
  const date = new Date(time)
  const now = new Date()
  const isToday = date.toDateString() === now.toDateString()
  
  if (isToday) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

const isMyMessage = (msg: ChatMessage) => {
  return msg.senderId === userStore.userInfo?.id
}

const isImageMessage = (msg: ChatMessage) => {
  return msg.msgType === 2 || (msg.content && (msg.content.startsWith('/uploads/') || msg.content.startsWith('http')))
}

const getFullImageUrl = (url: string) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
  return baseUrl + url
}

const getAvatarUrl = (url: string) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
  return baseUrl + url
}

const handleImageClick = (imageUrl: string) => {
  window.open(getFullImageUrl(imageUrl), '_blank')
}

const triggerImageUpload = () => {
  imageInputRef.value?.click()
}

const handleImageChange = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file || !selectedUser.value) return

  const allowedTypes = ['image/jpeg', 'image/png', 'image/jpg', 'image/webp', 'image/gif']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('仅支持 JPG、PNG、WEBP、GIF 格式的图片')
    return
  }

  const maxSize = 5 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('图片大小不能超过 5MB')
    return
  }

  sending.value = true
  try {
    await adminReplyImage(selectedUser.value.id, file)
    await fetchMessages()
    ElMessage.success('图片发送成功')
  } catch (error: any) {
    const msg = error?.response?.data?.msg || error?.message || '图片发送失败'
    ElMessage.error(msg)
  } finally {
    sending.value = false
    if (target) {
      target.value = ''
    }
  }
}

const formatLastTime = (time: string) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
}

onMounted(() => {
  fetchChatUsers()
})
</script>

<template>
  <div class="admin-chat-page">
    <div class="chat-container">
      <div class="sidebar">
        <div class="sidebar-header">
          <h2>客服消息</h2>
          <div class="unread-badge" v-if="totalUnreadCount > 0">
            {{ totalUnreadCount > 99 ? '99+' : totalUnreadCount }}
          </div>
        </div>
        
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索用户ID/用户名"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
        
        <div class="user-list">
          <div
            v-for="user in filteredUsers"
            :key="user.id"
            class="user-item"
            :class="{ active: selectedUser?.id === user.id }"
            @click="selectUser(user)"
          >
            <div class="user-avatar">
              <el-avatar :size="48" :src="user.avatar">
                {{ user.nickname?.charAt(0) || user.username?.charAt(0) }}
              </el-avatar>
              <span class="online-dot" v-if="user.unreadCount > 0"></span>
            </div>
            <div class="user-info">
              <div class="user-header">
                <span class="user-name">{{ user.nickname || user.username }}</span>
                <span class="user-id">ID: {{ user.id }}</span>
              </div>
              <div class="user-last-msg">
                <span class="last-msg">{{ user.lastMessage }}</span>
                <span class="last-time">{{ formatLastTime(user.lastMessageTime) }}</span>
              </div>
            </div>
            <div class="unread-count" v-if="user.unreadCount > 0">
              {{ user.unreadCount > 99 ? '99+' : user.unreadCount }}
            </div>
          </div>
          
          <el-empty v-if="filteredUsers.length === 0" description="暂无聊天用户" />
        </div>
      </div>
      
      <div class="chat-main">
        <template v-if="selectedUser">
          <div class="chat-header">
            <div class="header-avatar">
              <el-avatar :size="44" :src="selectedUser.avatar">
                {{ selectedUser.nickname?.charAt(0) || selectedUser.username?.charAt(0) }}
              </el-avatar>
            </div>
            <div class="header-info">
              <h3>{{ selectedUser.nickname || selectedUser.username }}</h3>
              <p>用户ID: {{ selectedUser.id }}</p>
            </div>
          </div>
          
          <div 
            ref="messagesContainer"
            v-loading="loading"
            class="messages-area"
          >
            <template v-if="messages.length > 0">
              <div 
                v-for="msg in messages" 
                :key="msg.id"
                class="message-wrapper"
                :class="isMyMessage(msg) ? 'my-message' : 'other-message'"
              >
                <div class="message-content">
                  <div class="message-avatar" v-if="!isMyMessage(msg)">
                    <el-avatar :size="40" :src="getAvatarUrl(selectedUser.avatar)" class="avatar-user">
                      {{ selectedUser.nickname?.charAt(0) || selectedUser.username?.charAt(0) }}
                    </el-avatar>
                  </div>
                  
                  <div class="message-body">
                    <div class="message-bubble" :class="isMyMessage(msg) ? 'bubble-blue' : 'bubble-white'">
                      <template v-if="isImageMessage(msg)">
                        <img 
                          :src="getFullImageUrl(msg.content)" 
                          class="message-image" 
                          @click="handleImageClick(msg.content)"
                          alt="聊天图片"
                          @error="(e) => (e.target as HTMLImageElement).style.display='none'"
                        />
                      </template>
                      <template v-else>
                        <p class="message-text">{{ msg.content }}</p>
                      </template>
                    </div>
                    <div class="message-time" :class="isMyMessage(msg) ? 'time-right' : 'time-left'">
                      {{ formatTime(msg.createTime) }}
                    </div>
                  </div>
                  
                  <div class="message-avatar" v-if="isMyMessage(msg)">
                    <el-avatar :size="40" :src="getAvatarUrl(userStore.userInfo?.avatar || '')" class="avatar-admin">
                      {{ userStore.userInfo?.nickname?.charAt(0) || '管' }}
                    </el-avatar>
                  </div>
                </div>
              </div>
            </template>
            <el-empty v-else description="暂无聊天记录" />
          </div>
          
          <div class="input-area">
            <div class="input-wrapper">
              <input 
                ref="imageInputRef"
                type="file"
                accept="image/jpeg,image/png,image/jpg,image/webp,image/gif"
                style="display: none"
                @change="handleImageChange"
              />
              <el-button 
                class="image-btn"
                :loading="sending"
                @click="triggerImageUpload"
              >
                <el-icon><Picture /></el-icon>
              </el-button>
              <el-input 
                v-model="inputMessage"
                placeholder="输入消息..."
                class="message-input"
                @keyup.enter="handleSend"
              />
              <el-button 
                type="primary" 
                class="send-btn"
                :loading="sending"
                :disabled="!inputMessage.trim()"
                @click="handleSend"
              >
                发送
              </el-button>
            </div>
          </div>
        </template>
        
        <div v-else class="no-chat-selected">
          <el-icon class="empty-icon"><ChatDotRound /></el-icon>
          <p>选择一个用户开始聊天</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-chat-page {
  height: calc(100vh - 120px);
  padding: 24px;
  background: linear-gradient(135deg, #f7f8fa 0%, #e6f4ff 50%, #f0f5ff 100%);
  animation: fadeIn 0.4s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.chat-container {
  max-width: 1400px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  background: #fff;
  border-radius: 20px;
  box-shadow: var(--shadow-xl);
  overflow: hidden;
}

.sidebar {
  width: 320px;
  border-right: 1px solid var(--border-light);
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--border-light);
}

.sidebar-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.unread-badge {
  background: var(--danger-color);
  color: #fff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
}

.search-box {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-light);
}

.search-box :deep(.el-input__wrapper) {
  border-radius: 10px;
}

.user-list {
  flex: 1;
  overflow-y: auto;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  cursor: pointer;
  transition: background 0.2s;
  border-bottom: 1px solid var(--border-light);
}

.user-item:hover {
  background: var(--primary-50);
}

.user-item.active {
  background: var(--primary-50);
  border-left: 3px solid var(--primary-color);
}

.user-avatar {
  position: relative;
  flex-shrink: 0;
}

.online-dot {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 10px;
  height: 10px;
  background: var(--danger-color);
  border: 2px solid #fff;
  border-radius: 50%;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.user-name {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary);
}

.user-id {
  font-size: 12px;
  color: var(--text-secondary);
}

.user-last-msg {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.last-msg {
  font-size: 13px;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 140px;
}

.last-time {
  font-size: 12px;
  color: var(--text-placeholder);
}

.unread-count {
  background: var(--danger-color);
  color: #fff;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 10px;
  min-width: 20px;
  text-align: center;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-header {
  padding: 16px 24px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid var(--border-light);
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
}

.header-info h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
}

.header-info p {
  margin: 4px 0 0 0;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
}

.messages-area {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: linear-gradient(180deg, #f7f8fa 0%, #fff 100%);
}

.message-wrapper {
  margin-bottom: 20px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-content {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.my-message .message-content {
  justify-content: flex-end;
}

.my-message .message-body {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.message-avatar {
  flex-shrink: 0;
}

.avatar-user {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  color: #fff;
}

.avatar-admin {
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
  color: #fff;
}

.message-body {
  max-width: 60%;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 16px;
}

.bubble-blue {
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
  color: #fff;
  border-bottom-right-radius: 4px;
}

.bubble-white {
  background: #fff;
  color: var(--text-primary);
  border-bottom-left-radius: 4px;
  box-shadow: var(--shadow-sm);
}

.message-text {
  margin: 0;
  font-size: 15px;
  line-height: 1.5;
  word-break: break-word;
}

.message-image {
  max-width: 200px;
  max-height: 200px;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s;
}

.message-image:hover {
  transform: scale(1.02);
}

.message-time {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
  padding: 0 4px;
}

.time-right {
  text-align: right;
}

.time-left {
  text-align: left;
}

.input-area {
  padding: 16px 24px;
  background: #fff;
  border-top: 1px solid var(--border-light);
}

.input-wrapper {
  display: flex;
  gap: 12px;
}

.message-input {
  flex: 1;
}

.message-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  padding: 8px 16px;
  box-shadow: none;
  border: 2px solid var(--border-light);
}

.message-input :deep(.el-input__wrapper:focus-within) {
  border-color: var(--primary-color);
}

.send-btn {
  height: 44px;
  padding: 0 24px;
  border-radius: 12px;
  font-weight: 600;
}

.image-btn {
  height: 44px;
  width: 44px;
  border-radius: 12px;
  border: 2px solid var(--border-light);
  background: #fff;
  color: var(--text-regular);
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-btn:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.image-btn .el-icon {
  font-size: 20px;
}

.no-chat-selected {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
}

.empty-icon {
  font-size: 64px;
  color: var(--border-light);
  margin-bottom: 16px;
}

.no-chat-selected p {
  font-size: 16px;
}

.messages-area::-webkit-scrollbar,
.user-list::-webkit-scrollbar {
  width: 6px;
}

.messages-area::-webkit-scrollbar-track,
.user-list::-webkit-scrollbar-track {
  background: transparent;
}

.messages-area::-webkit-scrollbar-thumb,
.user-list::-webkit-scrollbar-thumb {
  background: #d9d9d9;
  border-radius: 3px;
}

@media (max-width: 768px) {
  .admin-chat-page {
    padding: 0;
  }
  
  .chat-container {
    border-radius: 0;
  }
  
  .sidebar {
    width: 280px;
  }
  
  .message-body {
    max-width: 70%;
  }
}
</style>
