<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Message, Key, Lock } from '@element-plus/icons-vue'
import { sendCode, resetPassword } from '@/api/auth'

const router = useRouter()

const formRef = ref()
const loading = ref(false)
const step = ref(1)

const form = reactive({
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const countdown = ref(0)
const canSendCode = computed(() => {
  return form.email && countdown.value === 0
})

const rules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (value !== form.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const handleSendCode = async () => {
  if (!form.email) {
    ElMessage.warning('请输入邮箱')
    return
  }
  try {
    await sendCode(form.email)
    ElMessage.success('验证码已发送')
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error: any) {
    const msg = error?.response?.data?.msg || error?.message || '发送验证码失败'
    ElMessage.error(msg)
  }
}

const handleNext = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  if (step.value === 1) {
    step.value = 2
  } else if (step.value === 2) {
    loading.value = true
    try {
      await resetPassword({
        email: form.email,
        code: form.code,
        newPassword: form.newPassword
      })
      ElMessage.success('密码重置成功，请登录')
      router.push('/login')
    } catch (error: any) {
      const msg = error?.response?.data?.msg || error?.message || '重置密码失败'
      ElMessage.error(msg)
    } finally {
      loading.value = false
    }
  }
}

const goBack = () => {
  if (step.value > 1) {
    step.value--
  } else {
    router.push('/login')
  }
}
</script>

<template>
  <div class="forgot-password-page">
    <div class="page-wrapper">
      <div class="forgot-card">
        <div class="card-header">
          <h2 class="card-title">重置密码</h2>
          <p class="card-subtitle">
            {{ step === 1 ? '请输入您的邮箱地址' : '请设置新密码' }}
          </p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          size="large"
          class="forgot-form"
        >
          <template v-if="step === 1">
            <el-form-item label="邮箱" prop="email" class="form-item">
              <el-input 
                v-model="form.email" 
                placeholder="请输入注册时使用的邮箱"
                :prefix-icon="Message"
                class="form-input"
              />
            </el-form-item>

            <el-form-item label="验证码" prop="code" class="form-item">
              <div class="code-input-wrapper">
                <el-input 
                  v-model="form.code" 
                  placeholder="请输入验证码"
                  :prefix-icon="Key"
                  class="code-input"
                />
                <el-button 
                  :disabled="!canSendCode"
                  @click="handleSendCode"
                  class="code-btn"
                >
                  {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
          </template>

          <template v-else>
            <el-form-item label="新密码" prop="newPassword" class="form-item">
              <el-input 
                v-model="form.newPassword" 
                type="password" 
                placeholder="请输入新密码"
                show-password
                :prefix-icon="Lock"
                class="form-input"
              />
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword" class="form-item">
              <el-input 
                v-model="form.confirmPassword" 
                type="password" 
                placeholder="请再次输入新密码"
                show-password
                :prefix-icon="Lock"
                class="form-input"
                @keyup.enter="handleNext"
              />
            </el-form-item>
          </template>

          <el-button 
            type="primary" 
            class="submit-btn"
            :loading="loading"
            @click="handleNext"
          >
            {{ step === 1 ? '下一步' : '重置密码' }}
          </el-button>
        </el-form>

        <div class="back-link">
          <el-button link type="primary" @click="goBack" class="back-btn">
            返回登录
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.forgot-password-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.forgot-password-page::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
  animation: rotate 30s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.page-wrapper {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  position: relative;
  z-index: 1;
}

.forgot-card {
  width: 100%;
  max-width: 440px;
  background: #ffffff;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  padding: 48px 40px;
  animation: fadeInUp 0.6s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.card-header {
  text-align: center;
  margin-bottom: 40px;
}

.card-title {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.card-subtitle {
  font-size: 15px;
  color: #6b7280;
  margin: 0;
}

.forgot-form {
  margin-bottom: 8px;
}

.form-item {
  margin-bottom: 24px;
}

.form-item :deep(.el-form-item__label) {
  font-weight: 600;
  color: #374151;
  font-size: 14px;
  padding-bottom: 8px;
}

.form-input {
  width: 100%;
}

.form-input :deep(.el-input__wrapper) {
  border-radius: var(--radius-sm);
  box-shadow: 0 0 0 1px #e5e7eb inset;
  transition: all 0.3s ease;
  padding: 8px 16px;
}

.form-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--primary-color) inset;
}

.form-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px var(--primary-opacity-10), 0 0 0 1px var(--primary-color) inset;
}

.code-input-wrapper {
  display: flex;
  gap: 12px;
}

.code-input {
  flex: 1;
}

.code-btn {
  height: 48px;
  padding: 0 20px;
  font-weight: 600;
  border-radius: var(--radius-sm);
  white-space: nowrap;
  transition: all 0.3s ease;
}

.code-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(22, 119, 255, 0.3);
}

.submit-btn {
  width: 100%;
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--radius-md);
  margin-top: 8px;
  transition: all 0.3s ease;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(22, 119, 255, 0.3);
}

.back-link {
  text-align: center;
  margin-top: 24px;
}

.back-btn {
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.back-btn:hover {
  transform: translateX(-4px);
}

@media (max-width: 640px) {
  .forgot-card {
    padding: 32px 24px;
  }
  
  .card-title {
    font-size: 24px;
  }
  
  .code-input-wrapper {
    flex-direction: column;
  }
  
  .code-btn {
    width: 100%;
  }
}
</style>
