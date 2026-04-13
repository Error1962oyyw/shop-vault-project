<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Picture, Service } from '@element-plus/icons-vue'
import UserLayout from '@/components/layout/UserLayout.vue'
import { getChatHistory, sendMessage, sendImageMessage } from '@/api/chat'
import { useUserStore } from '@/stores/user'
import { extractErrorMessage } from '@/composables/useErrorMessage'
import { API_BASE_URL } from '@/api/constants'
import type { ChatMessage } from '@/types/api'

const POLL_INTERVAL = 5000
let pollTimer: ReturnType<typeof setInterval> | null = null

const userStore = useUserStore()

const messages = ref<ChatMessage[]>([])
const inputMessage = ref('')
const loading = ref(false)
const sending = ref(false)
const messagesContainer = ref<HTMLElement>()
const imageInputRef = ref<HTMLInputElement | null>(null)

const currentUserId = computed(() => userStore.userInfo?.id)

const fetchMessages = async (silent = false) => {
  if (!silent) loading.value = true
  try {
    const newMessages = await getChatHistory()
    const hasNew = newMessages.length > messages.value.length
    messages.value = newMessages
    if (hasNew || !silent) scrollToBottom()
  } catch (error) {
    if (!silent) {
      ElMessage.error(extractErrorMessage(error, '获取聊天记录失败'))
    }
  } finally {
    loading.value = false
  }
}

const startPolling = () => {
  if (pollTimer) return
  pollTimer = setInterval(() => fetchMessages(true), POLL_INTERVAL)
}

const stopPolling = () => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

const handleSend = async () => {
  if (!inputMessage.value.trim()) return
  
  const content = inputMessage.value.trim()
  inputMessage.value = ''
  
  sending.value = true
  try {
    await sendMessage({ content })
    await fetchMessages()
  } catch (error) {
    ElMessage.error(extractErrorMessage(error, '发送消息失败'))
  } finally {
    sending.value = false
  }
}

const triggerImageUpload = () => {
  imageInputRef.value?.click()
}

const handleImageChange = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

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
    await sendImageMessage(file)
    await fetchMessages()
    ElMessage.success('图片发送成功')
  } catch (error) {
    ElMessage.error(extractErrorMessage(error, '图片发送失败'))
  } finally {
    sending.value = false
    if (target) {
      target.value = ''
    }
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
  return msg.senderId === currentUserId.value
}

const isImageMessage = (msg: ChatMessage) => {
  return msg.msgType === 2 || (msg.content && (msg.content.startsWith('/uploads/') || msg.content.startsWith('http')))
}

const getFullImageUrl = (url: string) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return API_BASE_URL + url
}

const getAvatarUrl = (url: string) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return API_BASE_URL + url
}

const getSenderAvatar = (msg: ChatMessage) => {
  if (isMyMessage(msg)) {
    return userStore.userInfo?.avatar ? getAvatarUrl(userStore.userInfo.avatar) : ''
  }
  return msg.senderAvatar ? getAvatarUrl(msg.senderAvatar) : ''
}

const getCustomerServiceAvatar = (msg: ChatMessage) => {
  return msg.senderAvatar ? getAvatarUrl(msg.senderAvatar) : ''
}

const handleImageClick = (imageUrl: string) => {
  window.open(getFullImageUrl(imageUrl), '_blank')
}

onMounted(() => {
  fetchMessages()
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<template>
  <UserLayout>
    <div class="chat-page">
      <div class="chat-container">
        <div class="chat-window">
          <div class="chat-header">
            <div class="header-avatar">
              <el-icon class="header-icon"><Service /></el-icon>
            </div>
            <div class="header-info">
              <h2>在线客服</h2>
              <p>工作时间：9:00 - 18:00</p>
            </div>
            <div class="header-status">
              <span class="status-dot"></span>
              <span>在线</span>
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
                    <el-avatar :size="40" :src="getCustomerServiceAvatar(msg)" class="avatar-service">
                      客服
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
                    <el-avatar :size="40" :src="getSenderAvatar(msg)" class="avatar-user">
                      {{ userStore.userInfo?.nickname?.charAt(0) || userStore.userInfo?.username?.charAt(0) || '我' }}
                    </el-avatar>
                  </div>
                </div>
              </div>
            </template>
            <el-empty v-else description="暂无聊天记录，发送消息开始咨询" />
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
        </div>
      </div>
    </div>
  </UserLayout>
</template>

<style scoped>
.chat-page {
  background: linear-gradient(135deg, #f7f8fa 0%, #e6f4ff 50%, #f0f5ff 100%);
  min-height: 100vh;
  padding: 24px 0;
}

.chat-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 24px;
}

.chat-window {
  background: #fff;
  border-radius: 20px;
  box-shadow: var(--shadow-xl);
  overflow: hidden;
  height: calc(100vh - 160px);
  display: flex;
  flex-direction: column;
  min-height: 500px;
}

.chat-header {
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
  padding: 20px 24px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-avatar {
  width: 52px;
  height: 52px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-icon {
  font-size: 28px;
  color: #fff;
}

.header-info {
  flex: 1;
}

.header-info h2 {
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 4px 0;
}

.header-info p {
  color: rgba(255, 255, 255, 0.8);
  font-size: 13px;
  margin: 0;
}

.header-status {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #fff;
  font-size: 13px;
  background: rgba(255, 255, 255, 0.15);
  padding: 6px 12px;
  border-radius: 20px;
}

.status-dot {
  width: 8px;
  height: 8px;
  background: #52c41a;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
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

.avatar-service {
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
  color: white;
  font-size: 14px;
  font-weight: 600;
}

.avatar-user {
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
  color: white;
  font-size: 14px;
  font-weight: 600;
}

.message-body {
  max-width: 60%;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 16px;
  position: relative;
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
  flex-shrink: 0;
  position: sticky;
  bottom: 0;
  z-index: 10;
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
  transition: border-color 0.2s;
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

.messages-area::-webkit-scrollbar {
  width: 6px;
}

.messages-area::-webkit-scrollbar-track {
  background: transparent;
}

.messages-area::-webkit-scrollbar-thumb {
  background: #d9d9d9;
  border-radius: 3px;
}

.messages-area::-webkit-scrollbar-thumb:hover {
  background: #bfbfbf;
}

@media (max-width: 768px) {
  .chat-page {
    padding: 0;
  }

  .chat-container {
    padding: 0;
    max-width: 100%;
  }

  .chat-window {
    border-radius: 0;
    height: calc(100vh - 60px);
    height: calc(100dvh - 60px);
  }

  .message-body {
    max-width: 70%;
  }

  .messages-area {
    flex: 1;
    overflow-y: auto;
    padding-bottom: 16px;
  }

  .input-area {
    flex-shrink: 0;
    position: sticky;
    bottom: 0;
    padding: 12px 16px;
    padding-bottom: calc(12px + env(safe-area-inset-bottom));
    background: #fff;
    box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
  }

  .input-wrapper {
    gap: 8px;
  }

  .send-btn {
    height: 40px;
    padding: 0 16px;
  }

  .image-btn {
    height: 40px;
    width: 40px;
  }

  .message-input :deep(.el-input__wrapper) {
    padding: 6px 12px;
  }
}
</style>
