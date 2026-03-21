<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyMessages, pushToUser, pushToRole, pushToAll } from '@/api/message'
import type { MessagePush, MessagePushParams } from '@/types/api'

const loading = ref(false)
const messages = ref<MessagePush[]>([])
const showDialog = ref(false)
const form = ref<MessagePushParams>({
  type: 1,
  title: '',
  content: ''
})
const pushTarget = ref<'user' | 'role' | 'all'>('all')
const targetId = ref('')

const messageTypes = [
  { label: '系统通知', value: 1 },
  { label: '订单消息', value: 2 },
  { label: '活动通知', value: 3 },
  { label: '优惠提醒', value: 4 }
]

const fetchMessages = async () => {
  loading.value = true
  try {
    messages.value = await getMyMessages()
  } catch (error) {
    console.error('获取消息列表失败', error)
  } finally {
    loading.value = false
  }
}

const handlePush = async () => {
  if (!form.value.title || !form.value.content) {
    ElMessage.warning('请填写标题和内容')
    return
  }

  try {
    switch (pushTarget.value) {
      case 'user':
        if (!targetId.value) {
          ElMessage.warning('请输入用户ID')
          return
        }
        await pushToUser(Number(targetId.value), form.value)
        break
      case 'role':
        if (!targetId.value) {
          ElMessage.warning('请输入角色')
          return
        }
        await pushToRole(String(targetId.value), form.value)
        break
      case 'all':
        await pushToAll(form.value)
        break
    }
    
    ElMessage.success('推送成功')
    showDialog.value = false
    fetchMessages()
  } catch (error) {
    console.error('推送失败', error)
  }
}

const getMessageTypeText = (type: number) => {
  return messageTypes.find(t => t.value === type)?.label || '未知'
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

const formatTime = (date: string) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

onMounted(() => {
  fetchMessages()
})
</script>

<template>
  <div class="p-6">
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-xl font-bold">消息推送管理</h2>
      <el-button type="primary" @click="showDialog = true">
        <el-icon class="mr-1"><Bell /></el-icon>
        发送消息
      </el-button>
    </div>

    <el-table v-loading="loading" :data="messages" stripe>
      <el-table-column prop="type" label="类型" width="100">
        <template #default="{ row }">
          <span 
            class="px-2 py-1 rounded text-xs"
            :class="getMessageTypeClass(row.type)"
          >
            {{ getMessageTypeText(row.type) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column prop="content" label="内容" min-width="300">
        <template #default="{ row }">
          <span class="line-clamp-2">{{ row.content }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="isRead" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.isRead ? 'info' : 'success'" size="small">
            {{ row.isRead ? '已读' : '未读' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="发送时间" width="160">
        <template #default="{ row }">
          {{ formatTime(row.createTime) }}
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showDialog" title="发送消息" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="推送目标">
          <el-radio-group v-model="pushTarget">
            <el-radio value="all">全部用户</el-radio>
            <el-radio value="user">指定用户</el-radio>
            <el-radio value="role">指定角色</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item v-if="pushTarget === 'user'" label="用户ID">
          <el-input v-model="targetId" placeholder="请输入用户ID" />
        </el-form-item>
        
        <el-form-item v-if="pushTarget === 'role'" label="角色">
          <el-select v-model="targetId" placeholder="请选择角色" class="w-full">
            <el-option label="普通用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="消息类型">
          <el-select v-model="form.type" class="w-full">
            <el-option 
              v-for="type in messageTypes" 
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="消息标题">
          <el-input v-model="form.title" placeholder="请输入消息标题" />
        </el-form-item>
        
        <el-form-item label="消息内容">
          <el-input 
            v-model="form.content" 
            type="textarea"
            :rows="4"
            placeholder="请输入消息内容"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handlePush">发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
