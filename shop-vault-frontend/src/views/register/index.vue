<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Message, Key, Lock, ArrowRight, Shop } from '@element-plus/icons-vue'
import { sendCode, register } from '@/api/auth'

const router = useRouter()

const formRef = ref()
const loading = ref(false)

const registerForm = reactive({
  nickname: '',
  email: '',
  code: '',
  password: '',
  confirmPassword: ''
})

const countdown = ref(0)
const canSendCode = computed(() => {
  return registerForm.email && countdown.value === 0
})

const registerRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度为2-20个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (value !== registerForm.password) {
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
  if (!registerForm.email) {
    ElMessage.warning('请输入邮箱')
    return
  }
  try {
    await sendCode(registerForm.email)
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

const handleRegister = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    await register({
      email: registerForm.email,
      code: registerForm.code,
      password: registerForm.password,
      nickname: registerForm.nickname
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error: any) {
    const msg = error?.response?.data?.msg || error?.message || '注册失败，请重试'
    ElMessage.error(msg)
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<template>
  <div class="register-page animate-fade-in">
    <div class="page-wrapper">
      <div class="register-card">
        <div class="card-header">
          <div class="logo-wrapper">
            <el-icon :size="56" class="logo-icon"><Shop /></el-icon>
          </div>
          <h1 class="page-title">创建账号</h1>
          <p class="page-subtitle">开启您的智慧购物之旅</p>
        </div>

        <el-form
          ref="formRef"
          :model="registerForm"
          :rules="registerRules"
          label-position="top"
          size="large"
          class="register-form"
        >
          <el-form-item label="昵称" prop="nickname" class="form-item">
            <el-input 
              v-model="registerForm.nickname" 
              placeholder="请输入昵称"
              :prefix-icon="User"
              class="form-input"
            />
          </el-form-item>

          <el-form-item label="邮箱" prop="email" class="form-item">
            <el-input 
              v-model="registerForm.email" 
              placeholder="请输入邮箱"
              :prefix-icon="Message"
              class="form-input"
            />
          </el-form-item>

          <el-form-item label="验证码" prop="code" class="form-item">
            <div class="code-input-wrapper">
              <el-input 
                v-model="registerForm.code" 
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

          <el-form-item label="密码" prop="password" class="form-item">
            <el-input 
              v-model="registerForm.password" 
              type="password" 
              placeholder="请输入密码"
              show-password
              :prefix-icon="Lock"
              class="form-input"
            />
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword" class="form-item">
            <el-input 
              v-model="registerForm.confirmPassword" 
              type="password" 
              placeholder="请再次输入密码"
              show-password
              :prefix-icon="Lock"
              class="form-input"
              @keyup.enter="handleRegister"
            />
          </el-form-item>

          <el-button 
            type="primary" 
            class="submit-btn"
            :loading="loading"
            @click="handleRegister"
          >
            <span class="button-content">
              注册
              <ArrowRight class="button-icon" />
            </span>
          </el-button>
        </el-form>

        <div class="back-link">
          <span class="back-text">已有账号？</span>
          <el-button link type="primary" @click="goToLogin" class="login-btn">
            立即登录
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.register-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.register-page::before {
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

.register-card {
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

.logo-wrapper {
  width: 88px;
  height: 88px;
  margin: 0 auto 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.3);
  animation: pulse 3s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 4px 20px rgba(102, 126, 234, 0.3);
  }
  50% {
    box-shadow: 0 4px 30px rgba(102, 126, 234, 0.5);
  }
}

.logo-icon {
  color: #ffffff;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 15px;
  color: #6b7280;
  margin: 0;
}

.register-form {
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
  box-shadow: 0 0 0 1px #667eea inset;
}

.form-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1), 0 0 0 1px #667eea inset;
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
}

.code-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
}

.code-btn:disabled {
  background: #f0f2f5;
  color: #86909c;
}

.submit-btn {
  width: 100%;
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--radius-md);
  margin-top: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  transition: all 0.3s ease;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
}

.button-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.button-icon {
  width: 20px;
  height: 20px;
}

.back-link {
  text-align: center;
  margin-top: 28px;
}

.back-text {
  color: #6b7280;
  font-size: 14px;
}

.login-btn {
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s ease;
}

.login-btn:hover {
  transform: translateX(-4px);
}

@media (max-width: 640px) {
  .register-card {
    padding: 32px 24px;
  }
  
  .page-title {
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
