<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Setting, Warning } from '@element-plus/icons-vue'
import { adminLogin } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const errorMessage = ref('')
const emailError = ref('')
const passwordError = ref('')

const loginForm = reactive({
  email: '',
  password: ''
})

const rules = {
  email: [
    { required: true, message: '请输入管理员账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  errorMessage.value = ''
  emailError.value = ''
  passwordError.value = ''
  
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    const tokenResponse = await adminLogin(loginForm)
    userStore.setToken(tokenResponse.accessToken, tokenResponse.refreshToken)

    try {
      await userStore.fetchUserInfo()

      if (!userStore.isAdmin) {
        ElMessage.error('该账号没有管理员权限')
        userStore.clearToken()
        loading.value = false
        return
      }

      ElMessage.success('登录成功')
    } catch (fetchError) {
      userStore.clearToken()
      ElMessage.error('登录失败，无法验证管理员身份')
      loading.value = false
      return
    }

    router.push('/admin')
  } catch (error: any) {
    let errorMsg = '登录失败，请重试'
    const msg = error?.response?.data?.msg || error?.message || ''
    
    if (msg.includes('用户不存在') || msg.includes('用户名不存在') || msg.includes('not found') || msg.includes('Not found')) {
      errorMsg = '管理员账号不存在，请检查用户名'
      emailError.value = '账号不存在'
    } else if (msg.includes('密码错误') || msg.includes('密码不正确') || msg.includes('Invalid password') || msg.includes('密码不匹配')) {
      errorMsg = '用户名或密码错误，请重新输入'
      passwordError.value = '用户名或密码错误'
    } else if (msg.includes('权限不足') || msg.includes('Permission denied') || msg.includes('Not admin')) {
      errorMsg = '该账号没有管理员权限'
    } else if (msg) {
      errorMsg = msg
    }
    
    errorMessage.value = errorMsg
  } finally {
    loading.value = false
  }
}

const clearError = () => {
  errorMessage.value = ''
  emailError.value = ''
  passwordError.value = ''
}
</script>

<template>
  <div class="admin-login-page">
    <div class="page-wrapper">
      <div class="admin-card">
        <div class="card-header">
          <div class="icon-wrapper">
            <el-icon size="48" class="header-icon"><Setting /></el-icon>
          </div>
          <h1 class="page-title">小铺宝库</h1>
          <p class="page-subtitle">管理后台</p>
        </div>

        <div v-if="errorMessage" class="error-alert">
          <el-icon class="error-icon"><Warning /></el-icon>
          <span class="error-text">{{ errorMessage }}</span>
        </div>

        <el-form
          ref="formRef"
          :model="loginForm"
          :rules="rules"
          label-position="top"
          size="large"
          class="login-form"
        >
          <el-form-item prop="email" class="form-item" :error="emailError">
            <el-input 
              v-model="loginForm.email" 
              placeholder="管理员账号"
              :prefix-icon="User"
              class="form-input"
              @input="clearError"
            />
          </el-form-item>
          
          <el-form-item prop="password" class="form-item" :error="passwordError">
            <el-input 
              v-model="loginForm.password" 
              type="password" 
              placeholder="密码"
              show-password
              :prefix-icon="Lock"
              class="form-input"
              @keyup.enter="handleLogin"
              @input="clearError"
            />
          </el-form-item>

          <el-button 
            type="primary" 
            class="submit-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form>

        <div class="back-link">
          <router-link to="/login" class="back-btn">
            返回用户登录
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #334155 100%);
  position: relative;
  overflow: hidden;
}

.admin-login-page::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.03) 0%, transparent 60%);
  animation: float 20s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) rotate(0deg);
  }
  33% {
    transform: translate(30px, -30px) rotate(5deg);
  }
  66% {
    transform: translate(-20px, 20px) rotate(-5deg);
  }
}

.admin-login-page::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    radial-gradient(circle at 20% 80%, rgba(59, 130, 246, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(139, 92, 246, 0.1) 0%, transparent 50%);
  pointer-events: none;
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

.admin-card {
  width: 100%;
  max-width: 420px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: var(--radius-lg);
  box-shadow: 
    0 20px 60px rgba(0, 0, 0, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  padding: 48px 40px;
  animation: fadeInUp 0.6s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.card-header {
  text-align: center;
  margin-bottom: 32px;
}

.icon-wrapper {
  width: 88px;
  height: 88px;
  margin: 0 auto 20px;
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(59, 130, 246, 0.2);
  box-shadow: 0 4px 20px rgba(59, 130, 246, 0.25);
  animation: pulse 3s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 4px 20px rgba(59, 130, 246, 0.25);
  }
  50% {
    box-shadow: 0 4px 30px rgba(59, 130, 246, 0.4);
  }
}

.header-icon {
  color: #ffffff;
  filter: drop-shadow(0 2px 8px rgba(0, 0, 0, 0.15));
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 6px 0;
}

.page-subtitle {
  font-size: 15px;
  color: #6b7280;
  margin: 0;
  font-weight: 500;
}

.error-alert {
  display: flex;
  align-items: center;
  gap: 10px;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: var(--radius-sm);
  padding: 12px 16px;
  margin-bottom: 24px;
  animation: shake 0.5s ease-out;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}

.error-icon {
  color: #dc2626;
  flex-shrink: 0;
  width: 20px;
  height: 20px;
}

.error-text {
  color: #991b1b;
  font-size: 14px;
  font-weight: 500;
  line-height: 1.5;
}

.login-form {
  margin-bottom: 8px;
}

.form-item {
  margin-bottom: 24px;
}

.form-item :deep(.el-form-item__label) {
  display: none;
}

.form-input {
  width: 100%;
}

.form-input :deep(.el-input__wrapper) {
  background: #f9fafb;
  border: 1px solid #d1d5db;
  border-radius: var(--radius-sm);
  box-shadow: none;
  transition: all 0.3s ease;
  padding: 10px 16px;
}

.form-input :deep(.el-input__wrapper:hover) {
  background: #ffffff;
  border-color: #9ca3af;
}

.form-input :deep(.el-input__wrapper.is-focus) {
  background: #ffffff;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-input :deep(.el-input__inner) {
  color: #1f2937;
  font-weight: 500;
}

.form-input :deep(.el-input__inner::placeholder) {
  color: #9ca3af;
}

.form-input :deep(.el-input__prefix) {
  color: #6b7280;
}

.submit-btn {
  width: 100%;
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--radius-md);
  margin-top: 8px;
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  border: none;
  color: #ffffff;
  box-shadow: 0 4px 15px rgba(59, 130, 246, 0.4);
  transition: all 0.3s ease;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(59, 130, 246, 0.5);
}

.submit-btn:active {
  transform: translateY(0);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.back-link {
  text-align: center;
  margin-top: 28px;
}

.back-btn {
  color: #4b5563;
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.3s ease;
  display: inline-block;
}

.back-btn:hover {
  color: #1f2937;
  transform: translateX(-4px);
}

@media (max-width: 640px) {
  .admin-card {
    padding: 36px 28px;
  }
  
  .icon-wrapper {
    width: 76px;
    height: 76px;
  }
  
  .header-icon {
    font-size: 40px;
  }
  
  .page-title {
    font-size: 24px;
  }
}
</style>
