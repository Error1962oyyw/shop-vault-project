<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Shop, Camera, MagicStick, Coin, ArrowRight, CircleCheck, Warning } from '@element-plus/icons-vue'
import { login } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { parseLoginError } from '@/utils/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()

const formRef = ref()
const loading = ref(false)
const errorMessage = ref('')
const passwordError = ref('')

const loginForm = reactive({
  email: '',
  password: ''
})

const loginRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  errorMessage.value = ''
  passwordError.value = ''
  
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    const tokenResponse = await login(loginForm)
    userStore.setToken(tokenResponse.accessToken, tokenResponse.refreshToken)

    try {
      await userStore.fetchUserInfo()
      await cartStore.fetchCartList()
      ElMessage.success('登录成功')
    } catch (fetchError: unknown) {
      userStore.clearToken()
      ElMessage.error('登录失败，无法获取用户信息')
      loading.value = false
      return
    }

    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch (error: any) {
    const { errorMessage: errMsg, passwordError: pwdErr } = parseLoginError(error)
    errorMessage.value = errMsg
    passwordError.value = pwdErr
  } finally {
    loading.value = false
  }
}

const handleGuestMode = () => {
  userStore.enterGuestMode()
  ElMessage.success('已进入游客模式')
  router.push('/')
}

const goToRegister = () => {
  router.push('/register')
}

const clearError = () => {
  errorMessage.value = ''
  passwordError.value = ''
}
</script>

<template>
  <div class="login-container animate-fade-in">
    <div class="login-left">
      <div class="background-decoration">
        <div class="decoration-circle circle-1"></div>
        <div class="decoration-circle circle-2"></div>
        <div class="decoration-circle circle-3"></div>
      </div>
      
      <div class="brand-content">
        <div class="brand-logo">
          <el-icon :size="80" class="brand-icon"><Shop /></el-icon>
        </div>
        <h1 class="brand-title">小铺宝库</h1>
        <p class="brand-subtitle">
          基于SpringBoot的智慧电商系统<br />
          智能推荐 · AI搜索 · 全新购物体验
        </p>
        
        <div class="feature-grid">
          <div class="feature-item">
            <div class="feature-icon">
              <el-icon :size="40" class="icon-blue"><Camera /></el-icon>
            </div>
            <p class="feature-text">AI识图搜索</p>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <el-icon :size="40" class="icon-cyan"><MagicStick /></el-icon>
            </div>
            <p class="feature-text">智能推荐</p>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <el-icon :size="40" class="icon-indigo"><Coin /></el-icon>
            </div>
            <p class="feature-text">会员积分</p>
          </div>
        </div>

        <div class="trust-section">
          <div class="trust-item">
            <CircleCheck class="trust-icon" />
            <span>安全保障</span>
          </div>
          <div class="trust-item">
            <CircleCheck class="trust-icon" />
            <span>极速发货</span>
          </div>
          <div class="trust-item">
            <CircleCheck class="trust-icon" />
            <span>贴心服务</span>
          </div>
        </div>
      </div>
    </div>

    <div class="login-right">
      <div class="form-decoration">
        <div class="form-circle circle-top-right"></div>
        <div class="form-circle circle-bottom-left"></div>
      </div>
      
      <div class="form-wrapper">
        <div class="form-card">
          <div class="form-header">
            <h2 class="form-title">欢迎回来</h2>
            <p class="form-subtitle">登录以继续购物</p>
          </div>

          <div v-if="errorMessage" class="error-alert">
            <el-icon class="error-icon"><Warning /></el-icon>
            <span class="error-text">{{ errorMessage }}</span>
          </div>

          <el-form
            ref="formRef"
            :model="loginForm"
            :rules="loginRules"
            label-position="top"
            size="large"
            class="login-form"
          >
            <el-form-item 
              label="邮箱" 
              prop="email" 
              class="form-item"
            >
              <el-input 
                v-model="loginForm.email" 
                placeholder="请输入邮箱"
                :prefix-icon="User"
                class="form-input"
                @input="clearError"
              />
            </el-form-item>
            
            <el-form-item 
              label="密码" 
              prop="password" 
              class="form-item"
              :error="passwordError"
            >
              <el-input 
                v-model="loginForm.password" 
                type="password" 
                placeholder="请输入密码"
                show-password
                :prefix-icon="Lock"
                class="form-input"
                @keyup.enter="handleLogin"
                @input="clearError"
              />
            </el-form-item>

            <div class="forgot-password">
              <router-link to="/forgot-password" class="forgot-link">
                忘记密码？
              </router-link>
            </div>

            <el-button 
              type="primary" 
              class="submit-button"
              :loading="loading"
              @click="handleLogin"
            >
              <span class="button-content">
                登录
                <ArrowRight class="button-icon" />
              </span>
            </el-button>
          </el-form>

          <div class="switch-section">
            <span class="switch-text">还没有账号？</span>
            <el-button link type="primary" @click="goToRegister" class="switch-button">
              立即注册
            </el-button>
          </div>

          <div class="divider">
            <div class="divider-line"></div>
            <span class="divider-text">或</span>
            <div class="divider-line"></div>
          </div>

          <el-button 
            class="guest-button"
            @click="handleGuestMode"
          >
            游客模式浏览
          </el-button>
        </div>

        <div class="admin-link">
          <router-link to="/admin/login" class="admin-text">
            管理员登录入口
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  min-height: 100vh;
  overflow: hidden;
  background: linear-gradient(135deg, #f7f8fa 0%, #e6f4ff 100%);
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

.login-left {
  display: none;
  position: relative;
  width: 50%;
  align-items: center;
  justify-content: center;
  padding: 48px;
  overflow: hidden;
}

@media (min-width: 1024px) {
  .login-left {
    display: flex;
  }
}

.background-decoration {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  mix-blend-mode: multiply;
  filter: blur(48px);
  opacity: 0.7;
  animation: pulse 4s ease-in-out infinite;
}

.circle-1 {
  top: -160px;
  left: -160px;
  width: 320px;
  height: 320px;
  background: #93c5fd;
}

.circle-2 {
  bottom: -160px;
  right: -160px;
  width: 320px;
  height: 320px;
  background: #a5f3fc;
  animation-delay: 2s;
}

.circle-3 {
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 384px;
  height: 384px;
  background: #c7d2fe;
  opacity: 0.5;
  animation-delay: 4s;
}

@keyframes pulse {
  0%, 100% { opacity: 0.7; }
  50% { opacity: 0.5; }
}

.brand-content {
  position: relative;
  z-index: 10;
  text-align: center;
  max-width: 480px;
  animation: fadeIn 0.8s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.brand-logo {
  width: 128px;
  height: 128px;
  margin: 0 auto 32px;
  border-radius: 24px;
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-xl);
  transition: transform 0.3s ease;
}

.brand-logo:hover {
  transform: scale(1.05);
}

.brand-icon {
  color: white;
}

.brand-title {
  font-size: 48px;
  font-weight: 800;
  margin-bottom: 16px;
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 50%, #1677ff 100%);
  background-size: 200% 100%;
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: gradientFlow 3s ease infinite;
}

@keyframes gradientFlow {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

.brand-subtitle {
  font-size: 20px;
  color: #4e5969;
  margin-bottom: 40px;
  line-height: 1.8;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin-bottom: 48px;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.feature-icon {
  width: 80px;
  height: 80px;
  margin-bottom: 12px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(4px);
  box-shadow: var(--shadow-md);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s ease;
}

.feature-item:hover .feature-icon {
  transform: scale(1.1);
}

.icon-blue {
  color: #1677ff;
}

.icon-cyan {
  color: #4096ff;
}

.icon-indigo {
  color: #0958d9;
}

.feature-text {
  font-size: 14px;
  font-weight: 500;
  color: #1f2329;
}

.trust-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 32px;
}

.trust-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #86909c;
}

.trust-icon {
  width: 20px;
  height: 20px;
  color: #52c41a;
}

.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  position: relative;
}

@media (min-width: 640px) {
  .login-right {
    padding: 48px;
  }
}

.form-decoration {
  position: absolute;
  pointer-events: none;
}

.form-circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(32px);
  opacity: 0.5;
}

.circle-top-right {
  top: 32px;
  right: 32px;
  width: 128px;
  height: 128px;
  background: #93c5fd;
}

.circle-bottom-left {
  bottom: 32px;
  left: 32px;
  width: 96px;
  height: 96px;
  background: #a5f3fc;
}

.form-wrapper {
  width: 100%;
  max-width: 448px;
  position: relative;
  z-index: 10;
}

.form-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  box-shadow: var(--shadow-xl);
  padding: 32px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  animation: slideUp 0.6s ease-out;
}

@media (min-width: 640px) {
  .form-card {
    padding: 40px;
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.form-header {
  text-align: center;
  margin-bottom: 32px;
}

.form-title {
  font-size: 30px;
  font-weight: 700;
  color: #1f2329;
  margin-bottom: 8px;
}

.form-subtitle {
  color: #86909c;
  font-size: 16px;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-item {
  margin-bottom: 0;
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

.forgot-password {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 24px;
}

.forgot-link {
  font-size: 14px;
  font-weight: 500;
  color: #1677ff;
  text-decoration: none;
  transition: color 0.2s;
}

.forgot-link:hover {
  color: #4096ff;
}

.submit-button {
  width: 100%;
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--radius-md);
  background: var(--primary-color);
  border: none;
  transition: all 0.3s ease;
  margin-bottom: 24px;
}

.submit-button:hover {
  background: var(--primary-light);
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(22, 119, 255, 0.3);
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

.switch-section {
  text-align: center;
  margin-bottom: 24px;
}

.switch-text {
  color: #6b7280;
  font-size: 14px;
}

.switch-button {
  font-weight: 600;
  color: #1677ff;
  font-size: 14px;
}

.switch-button:hover {
  color: #4096ff;
}

.divider {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.divider-line {
  flex: 1;
  height: 1px;
  background: #e5e7eb;
}

.divider-text {
  color: #9ca3af;
  font-size: 14px;
}

.guest-button {
  width: 100%;
  height: 48px;
  font-size: 15px;
  font-weight: 600;
  border-radius: var(--radius-md);
  background: transparent;
  border: 2px solid #1677ff;
  color: #1677ff;
  transition: all 0.3s ease;
}

.guest-button:hover {
  background: var(--primary-opacity-10);
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(22, 119, 255, 0.15);
}

.admin-link {
  margin-top: 24px;
  text-align: center;
}

.admin-text {
  color: #86909c;
  font-size: 14px;
  text-decoration: none;
  transition: color 0.2s;
}

.admin-text:hover {
  color: #4e5969;
}

@media (max-width: 640px) {
  .form-card {
    padding: 32px 24px;
  }
}
</style>
