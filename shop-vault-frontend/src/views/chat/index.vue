<script setup lang="ts">
import { ref, onMounted, nextTick, computed } from 'vue'
import UserLayout from '@/components/layout/UserLayout.vue'
import { getChatHistory, sendMessage } from '@/api/chat'
import { useUserStore } from '@/stores/user'
import type { ChatMessage } from '@/types/api'

const userStore = useUserStore()

const messages = ref<ChatMessage[]>([])
const inputMessage = ref('')
const loading = ref(false)
const sending = ref(false)
const messagesContainer = ref<HTMLElement>()

const currentUserId = computed(() => userStore.userInfo?.id)

const fetchMessages = async () => {
  loading.value = true
  try {
    messages.value = await getChatHistory()
    scrollToBottom()
  } catch (error) {
    console.error('获取聊天记录失败', error)
  } finally {
    loading.value = false
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
    console.error('发送消息失败', error)
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
  return msg.senderId === currentUserId.value
}

onMounted(() => {
  fetchMessages()
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
                    <el-avatar :size="40" class="avatar-service">
                      客服
                    </el-avatar>
                  </div>
                  
                  <div class="message-body">
                    <div class="message-bubble" :class="isMyMessage(msg) ? 'bubble-blue' : 'bubble-white'">
                      <p class="message-text">{{ msg.content }}</p>
                    </div>
                    <div class="message-time" :class="isMyMessage(msg) ? 'time-right' : 'time-left'">
                      {{ formatTime(msg.createTime) }}
                    </div>
                  </div>
                  
                  <div class="message-avatar" v-if="isMyMessage(msg)">
                    <el-avatar :size="40" class="avatar-user">
                      {{ userStore.userInfo?.nickname?.charAt(0) || '我' }}
                    </el-avatar>
                  </div>
                </div>
              </div>
            </template>
            <el-empty v-else description="暂无聊天记录，发送消息开始咨询" />
          </div>

          <div class="input-area">
            <div class="input-wrapper">
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
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
}

.avatar-service {
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
  color: #fff;
  font-size: 14px;
}

.avatar-user {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  color: #fff;
  font-size: 14px;
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
  }

  .message-body {
    max-width: 70%;
  }
}
</style>
