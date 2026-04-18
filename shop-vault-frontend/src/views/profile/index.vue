<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import UserLayout from '@/components/layout/UserLayout.vue'
import { getProfile, updateProfile, updatePassword, getAddressList, addAddress, updateAddress, deleteAddress, setDefaultAddress, uploadAvatar } from '@/api/user'
import { regionOptions } from '@/utils/region'
import { getSignInStatus, signIn } from '@/api/marketing'
import { sendCode } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import type { UserInfo, Address } from '@/types/api'
import { Coin, User, Lock, Location, Plus, Camera, Key, Message } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const activeMenu = ref('profile')
const userInfo = ref<UserInfo | null>(null)
const addresses = ref<Address[]>([])
const loading = ref(false)
const hasSignedToday = ref(false)
const avatarInputRef = ref<HTMLInputElement | null>(null)
const isEditing = ref(false)

const profileForm = ref({
  nickname: '',
  phone: '',
  gender: 0,
  birthday: ''
})

const originalProfileForm = ref({
  nickname: '',
  phone: '',
  gender: 0,
  birthday: ''
})

const passwordStep = ref(1)
const passwordForm = ref({
  email: '',
  code: '',
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const passwordCountdown = ref(0)
const canSendPasswordCode = computed(() => {
  return passwordForm.value.email && passwordCountdown.value === 0
})

const avatarUrl = computed(() => {
  if (!userInfo.value?.avatar) return ''
  const url = userInfo.value.avatar
  let fullUrl = url
  if (!url.startsWith('http')) {
    const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:9090'
    fullUrl = baseUrl + url
  }
  const separator = fullUrl.includes('?') ? '&' : '?'
  return `${fullUrl}${separator}t=${Date.now()}`
})

const addressForm = ref({
  id: 0,
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  region: '',
  detailAddress: ''
})

const showAddressDialog = ref(false)
const addressFormRef = ref()
const regionValue = ref<string[]>([])



const phoneError = ref('')

const validatePhone = (phone: string): boolean => {
  if (!phone) {
    phoneError.value = ''
    return true
  }
  const phoneReg = /^1[3-9]\d{9}$/
  if (!phoneReg.test(phone)) {
    phoneError.value = '请输入11位有效手机号（以1开头，第二位为3-9）'
    return false
  }
  phoneError.value = ''
  return true
}

const handlePhoneInput = () => {
  validatePhone(profileForm.value.phone)
}

const fetchProfile = async () => {
  loading.value = true
  try {
    userInfo.value = await getProfile()
    profileForm.value = {
      nickname: userInfo.value.nickname || '',
      phone: userInfo.value.phone || '',
      gender: userInfo.value.gender ?? 0,
      birthday: userInfo.value.birthday || ''
    }
    originalProfileForm.value = { ...profileForm.value }
  } catch (error) {
    console.error('获取用户信息失败', error)
  } finally {
    loading.value = false
  }
}

const fetchAddresses = async () => {
  try {
    addresses.value = await getAddressList()
  } catch (error) {
    console.error('获取地址失败', error)
  }
}

const checkTodaySigned = async () => {
  if (!userStore.token) return
  try {
    const status = await getSignInStatus()
    hasSignedToday.value = status.todaySigned
  } catch (error) {
    console.error('检查签到状态失败', error)
  }
}

const handleRegionChange = (value: unknown) => {
  if (value && Array.isArray(value) && value.length >= 3) {
    addressForm.value.province = String(value[0])
    addressForm.value.city = String(value[1])
    addressForm.value.region = String(value[2])
  }
}

const handleSignIn = async () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (hasSignedToday.value) {
    ElMessage.info('今日已签到，请明天再来！')
    return
  }
  try {
    const res = await signIn()
    ElMessage.success(`签到成功，获得${res.points}积分`)
    hasSignedToday.value = true
    await userStore.fetchUserInfo()
  } catch (error: any) {
    const msg = error?.response?.data?.msg || error?.message || '签到失败，请重试'
    ElMessage.error(msg)
    if (msg.includes('今日已签到')) {
      hasSignedToday.value = true
    }
  }
}

const startEdit = () => {
  originalProfileForm.value = { ...profileForm.value }
  isEditing.value = true
}

const cancelEdit = () => {
  profileForm.value = { ...originalProfileForm.value }
  phoneError.value = ''
  isEditing.value = false
}

const handleUpdateProfile = async () => {
  if (!validatePhone(profileForm.value.phone)) {
    ElMessage.error(phoneError.value)
    return
  }
  
  try {
    await updateProfile(profileForm.value)
    ElMessage.success('更新成功')
    isEditing.value = false
    await fetchProfile()
    userStore.fetchUserInfo()
  } catch (error) {
    console.error('更新失败', error)
  }
}

const triggerAvatarUpload = () => {
  avatarInputRef.value?.click()
}

const handleAvatarChange = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  const allowedTypes = ['image/jpeg', 'image/png', 'image/jpg', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('仅支持 JPG、PNG、WEBP 格式的图片')
    return
  }

  const maxSize = 2 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('图片大小不能超过 2MB')
    return
  }

  try {
    loading.value = true
    await uploadAvatar(file)
    await fetchProfile()
    await userStore.fetchUserInfo()
    ElMessage.success('头像更新成功')
  } catch (error: any) {
    const msg = error?.response?.data?.msg || error?.message || '头像上传失败'
    ElMessage.error(msg)
  } finally {
    loading.value = false
    if (target) {
      target.value = ''
    }
  }
}

const handleSendPasswordCode = async () => {
  if (!passwordForm.value.email) {
    ElMessage.warning('请输入邮箱')
    return
  }
  try {
    await sendCode(passwordForm.value.email)
    ElMessage.success('验证码已发送')
    passwordCountdown.value = 60
    const timer = setInterval(() => {
      passwordCountdown.value--
      if (passwordCountdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error: any) {
    const msg = error?.response?.data?.msg || error?.message || '发送验证码失败'
    ElMessage.error(msg)
  }
}

const handleNextPasswordStep = async () => {
  if (passwordStep.value === 1) {
    if (!passwordForm.value.email) {
      ElMessage.warning('请输入注册邮箱')
      return
    }
    if (!passwordForm.value.code) {
      ElMessage.warning('请输入验证码')
      return
    }
    passwordStep.value = 2
  } else if (passwordStep.value === 2) {
    if (!passwordForm.value.oldPassword) {
      ElMessage.warning('请输入当前密码')
      return
    }
    if (passwordForm.value.newPassword.length < 6 || passwordForm.value.newPassword.length > 20) {
      ElMessage.error('新密码长度为6-20个字符')
      return
    }
    if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
      ElMessage.error('两次输入的密码不一致')
      return
    }
    try {
      await updatePassword({
        oldPassword: passwordForm.value.oldPassword,
        newPassword: passwordForm.value.newPassword
      })
      ElMessage.success('密码修改成功')
      passwordStep.value = 1
      passwordForm.value = { 
        email: '', 
        code: '', 
        oldPassword: '', 
        newPassword: '', 
        confirmPassword: '' 
      }
      passwordCountdown.value = 0
      await userStore.fetchUserInfo()
    } catch (error: any) {
      const msg = error?.response?.data?.msg || error?.message || '修改密码失败'
      ElMessage.error(msg)
    }
  }
}

const handleResetPasswordStep = () => {
  if (passwordStep.value > 1) {
    passwordStep.value--
  }
}

const handleAddAddress = () => {
  addressForm.value = {
    id: 0,
    receiverName: '',
    receiverPhone: '',
    province: '',
    city: '',
    region: '',
    detailAddress: ''
  }
  regionValue.value = []
  showAddressDialog.value = true
}

const handleEditAddress = (address: Address) => {
  addressForm.value = { ...address }
  regionValue.value = [address.province, address.city, address.region]
  showAddressDialog.value = true
}

const handleDeleteAddress = async (id: number) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该收货地址吗？',
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await deleteAddress(id)
    ElMessage.success('删除成功')
    fetchAddresses()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
    }
  }
}

const handleSetDefaultAddress = async (id: number) => {
  try {
    await setDefaultAddress(id)
    ElMessage.success('已设为默认地址')
    fetchAddresses()
  } catch (error) {
    console.error('设置失败', error)
  }
}

const handleSaveAddress = async () => {
  await addressFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      try {
        if (addressForm.value.id) {
          await updateAddress({ ...addressForm.value, isDefault: 0 })
        } else {
          await addAddress(addressForm.value)
        }
        ElMessage.success('保存成功')
        showAddressDialog.value = false
        fetchAddresses()
      } catch (error) {
        console.error('保存失败', error)
      }
    }
  })
}

const validateAddressPhone = (_rule: any, value: any, callback: any) => {
  const phoneReg = /^1[3-9]\d{9}$/
  if (!value) {
    callback(new Error('请输入手机号'))
  } else if (!phoneReg.test(value)) {
    callback(new Error('请输入11位有效手机号'))
  } else {
    callback()
  }
}

onMounted(() => {
  fetchProfile()
  fetchAddresses()
  checkTodaySigned()
})
</script>

<template>
  <UserLayout>
    <div class="profile-page animate-fade-in">
      <div class="page-container">
        <div class="profile-layout">
          <aside class="profile-sidebar">
            <div class="sidebar-card">
              <div class="user-profile">
                <div class="avatar-wrapper" @click="triggerAvatarUpload">
                  <el-avatar :size="80" :src="avatarUrl" class="user-avatar">
                    {{ userInfo?.nickname?.charAt(0) || userInfo?.username?.charAt(0) }}
                  </el-avatar>
                  <div class="avatar-overlay">
                    <el-icon><Camera /></el-icon>
                    <span>更换头像</span>
                  </div>
                </div>
                <input 
                  ref="avatarInputRef" 
                  type="file" 
                  accept="image/*" 
                  style="display: none" 
                  @change="handleAvatarChange"
                />
                <h3 class="user-name">{{ userInfo?.nickname || userInfo?.username }}</h3>
                <p class="user-points">
                  <el-icon><Coin /></el-icon>
                  积分：{{ userInfo?.points || 0 }}
                </p>
                <el-button 
                  :type="hasSignedToday ? 'info' : 'primary'"
                  :disabled="hasSignedToday"
                  @click="handleSignIn"
                  class="sign-btn"
                >
                  {{ hasSignedToday ? '今日已签到' : '每日签到' }}
                </el-button>
              </div>
              
              <el-menu 
                :default-active="activeMenu" 
                @select="(key: string) => activeMenu = key"
                class="profile-menu"
              >
                <el-menu-item index="profile">
                  <el-icon><User /></el-icon>
                  <span>个人资料</span>
                </el-menu-item>
                <el-menu-item index="password">
                  <el-icon><Lock /></el-icon>
                  <span>修改密码</span>
                </el-menu-item>
                <el-menu-item index="address">
                  <el-icon><Location /></el-icon>
                  <span>收货地址</span>
                </el-menu-item>
              </el-menu>
            </div>
          </aside>

          <main class="profile-main">
            <div v-loading="loading" class="main-card">
              <template v-if="activeMenu === 'profile'">
                <div class="section-header">
                  <h2 class="section-title">个人资料</h2>
                </div>
                <el-form :model="profileForm" label-width="100px" class="profile-form">
                  <el-form-item label="用户名">
                    <el-input :value="userInfo?.username" disabled class="form-input" />
                  </el-form-item>
                  <el-form-item label="邮箱">
                    <el-input :value="userInfo?.email" disabled class="form-input" />
                  </el-form-item>
                  <el-form-item label="昵称">
                    <el-input 
                      v-model="profileForm.nickname" 
                      placeholder="请输入昵称" 
                      class="form-input" 
                      :disabled="!isEditing"
                    />
                  </el-form-item>
                  <el-form-item label="手机号">
                    <el-input 
                      v-model="profileForm.phone" 
                      placeholder="请输入手机号" 
                      class="form-input"
                      :disabled="!isEditing"
                      maxlength="11"
                      @input="handlePhoneInput"
                    />
                    <div v-if="phoneError" class="error-text">{{ phoneError }}</div>
                  </el-form-item>
                  <el-form-item label="性别">
                    <el-radio-group v-model="profileForm.gender" :disabled="!isEditing">
                      <el-radio :value="1">男</el-radio>
                      <el-radio :value="2">女</el-radio>
                      <el-radio :value="0">保密</el-radio>
                    </el-radio-group>
                  </el-form-item>
                  <el-form-item label="生日">
                    <el-date-picker 
                      v-model="profileForm.birthday" 
                      type="date"
                      placeholder="选择日期"
                      value-format="YYYY-MM-DD"
                      class="form-input"
                      :disabled="!isEditing"
                    />
                  </el-form-item>
                  <el-form-item>
                    <el-button 
                      v-if="!isEditing" 
                      type="primary" 
                      @click="startEdit" 
                      class="save-btn"
                    >
                      修改个人信息
                    </el-button>
                    <div v-else class="button-group">
                      <el-button @click="cancelEdit" class="cancel-btn">
                        取消修改
                      </el-button>
                      <el-button type="primary" @click="handleUpdateProfile" class="save-btn">
                        保存修改
                      </el-button>
                    </div>
                  </el-form-item>
                </el-form>
              </template>

              <template v-else-if="activeMenu === 'password'">
                <div class="section-header">
                  <h2 class="section-title">修改密码</h2>
                </div>
                
                <div v-if="passwordStep === 1" class="password-step">
                  <div class="step-indicator">
                    <div class="step-dot step-active"></div>
                    <div class="step-line"></div>
                    <div class="step-dot"></div>
                  </div>
                  <p class="step-hint">步骤一：验证邮箱身份</p>
                  <el-form label-position="top" class="profile-form">
                    <el-form-item label="注册邮箱">
                      <el-input 
                        v-model="passwordForm.email" 
                        placeholder="请输入注册时使用的邮箱"
                        :prefix-icon="Message"
                        class="form-input"
                      />
                    </el-form-item>
                    <el-form-item label="验证码">
                      <div class="code-input-wrapper">
                        <el-input 
                          v-model="passwordForm.code" 
                          placeholder="请输入验证码"
                          :prefix-icon="Key"
                          class="code-input"
                        />
                        <el-button 
                          :disabled="!canSendPasswordCode"
                          @click="handleSendPasswordCode"
                          class="code-btn"
                        >
                          {{ passwordCountdown > 0 ? `${passwordCountdown}s` : '获取验证码' }}
                        </el-button>
                      </div>
                    </el-form-item>
                    <el-form-item>
                      <el-button type="primary" @click="handleNextPasswordStep" class="save-btn">
                        下一步
                      </el-button>
                    </el-form-item>
                  </el-form>
                </div>

                <div v-else class="password-step">
                  <div class="step-indicator">
                    <div class="step-dot step-completed"></div>
                    <div class="step-line step-line-completed"></div>
                    <div class="step-dot step-active"></div>
                  </div>
                  <p class="step-hint">步骤二：验证当前密码并设置新密码</p>
                  <el-form label-position="top" class="profile-form">
                    <el-form-item label="当前密码">
                      <el-input 
                        v-model="passwordForm.oldPassword" 
                        type="password"
                        show-password
                        placeholder="请输入当前密码"
                        class="form-input"
                      />
                    </el-form-item>
                    <el-form-item label="新密码">
                      <el-input 
                        v-model="passwordForm.newPassword" 
                        type="password" 
                        show-password
                        placeholder="请输入新密码(6-20位)" 
                        class="form-input"
                      />
                    </el-form-item>
                    <el-form-item label="确认新密码">
                      <el-input 
                        v-model="passwordForm.confirmPassword" 
                        type="password" 
                        show-password
                        placeholder="请再次输入新密码" 
                        class="form-input"
                      />
                    </el-form-item>
                    <el-form-item>
                      <div class="button-group">
                        <el-button @click="handleResetPasswordStep" class="back-btn">
                          上一步
                        </el-button>
                        <el-button type="primary" @click="handleNextPasswordStep" class="save-btn">
                          确认修改
                        </el-button>
                      </div>
                    </el-form-item>
                  </el-form>
                </div>
              </template>

              <template v-else-if="activeMenu === 'address'">
                <div class="section-header">
                  <h2 class="section-title">收货地址</h2>
                  <el-button type="primary" @click="handleAddAddress" class="add-btn">
                    <el-icon class="icon"><Plus /></el-icon>
                    新增地址
                  </el-button>
                </div>

                <div class="address-list">
                  <div 
                    v-for="address in addresses" 
                    :key="address.id"
                    class="address-card"
                  >
                    <div class="address-content">
                      <div class="address-header">
                        <span class="receiver-name">{{ address.receiverName }}</span>
                        <span class="receiver-phone">{{ address.receiverPhone }}</span>
                        <el-tag v-if="address.isDefault === 1" type="success" size="small" class="default-tag">
                          默认
                        </el-tag>
                      </div>
                      <p class="address-detail">
                        {{ address.province }}{{ address.city }}{{ address.region }}{{ address.detailAddress }}
                      </p>
                    </div>
                    <div class="address-actions">
                      <el-button 
                        v-if="address.isDefault !== 1"
                        type="primary" 
                        link 
                        size="small"
                        @click="handleSetDefaultAddress(address.id)"
                      >
                        设为默认
                      </el-button>
                      <el-button 
                        type="primary" 
                        link 
                        size="small"
                        @click="handleEditAddress(address)"
                      >
                        编辑
                      </el-button>
                      <el-button 
                        type="danger" 
                        link 
                        size="small"
                        @click="handleDeleteAddress(address.id)"
                      >
                        删除
                      </el-button>
                    </div>
                  </div>
                </div>

                <el-empty v-if="addresses.length === 0" description="暂无收货地址" class="empty-state" />
              </template>
            </div>
          </main>
        </div>
      </div>
    </div>

    <el-dialog 
      v-model="showAddressDialog" 
      :title="addressForm.id ? '编辑地址' : '新增地址'"
      width="500px"
      :close-on-click-modal="false"
      class="address-dialog"
    >
      <el-form 
        ref="addressFormRef"
        :model="addressForm"
        label-width="80px"
        :rules="{
          receiverName: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
          receiverPhone: [{ required: true, validator: validateAddressPhone, trigger: 'blur' }],
          province: [{ required: true, message: '请选择省份', trigger: 'change' }],
          city: [{ required: true, message: '请选择城市', trigger: 'change' }],
          region: [{ required: true, message: '请选择区县', trigger: 'change' }],
          detailAddress: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
        }"
      >
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="addressForm.receiverName" placeholder="请输入收货人姓名" class="form-input" />
        </el-form-item>
        <el-form-item label="手机号" prop="receiverPhone">
          <el-input v-model="addressForm.receiverPhone" placeholder="请输入11位手机号" class="form-input" maxlength="11" />
        </el-form-item>
        <el-form-item label="省市区" prop="province">
          <el-cascader
            v-model="regionValue"
            :options="regionOptions as any"
            @change="handleRegionChange"
            placeholder="请选择省市区"
            class="form-input"
            clearable
          />
        </el-form-item>
        <el-form-item label="详细地址" prop="detailAddress">
          <el-input 
            v-model="addressForm.detailAddress" 
            type="textarea"
            :rows="2"
            placeholder="请输入详细地址" 
            class="form-input"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddressDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveAddress">保存</el-button>
      </template>
    </el-dialog>
  </UserLayout>
</template>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f0f5ff 0%, #e6f7ff 50%, #f5f7fa 100%);
  padding: 24px 0;
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.profile-layout {
  display: flex;
  gap: 24px;
}

.profile-sidebar {
  width: 260px;
  flex-shrink: 0;
}

.sidebar-card {
  background: #ffffff;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

.user-profile {
  text-align: center;
  padding: 32px 24px 24px;
  border-bottom: 1px solid #f3f4f6;
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
  cursor: pointer;
  margin-bottom: 16px;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  color: #fff;
}

.avatar-overlay .el-icon {
  font-size: 24px;
  margin-bottom: 4px;
}

.avatar-overlay span {
  font-size: 12px;
}

.user-avatar {
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
  color: white;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.user-name {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 8px;
}

.user-points {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #6b7280;
  font-size: 14px;
  margin-bottom: 16px;
}

.sign-btn {
  width: 100%;
  height: 40px;
  font-weight: 600;
  border-radius: var(--radius-sm);
  transition: all 0.3s ease;
}

.sign-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(22, 119, 255, 0.3);
}

.profile-menu {
  border: none;
}

.profile-menu :deep(.el-menu-item) {
  height: 48px;
  line-height: 48px;
  margin: 4px 12px;
  border-radius: var(--radius-sm);
}

.profile-main {
  flex: 1;
}

.main-card {
  background: #ffffff;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  padding: 32px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 32px;
}

.section-title {
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.profile-form {
  max-width: 500px;
}

.form-input {
  width: 100%;
}

.save-btn {
  height: 44px;
  padding: 0 32px;
  font-weight: 600;
  border-radius: var(--radius-sm);
  transition: all 0.3s ease;
}

.save-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(22, 119, 255, 0.3);
}

.password-step {
  max-width: 500px;
}

.step-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
}

.step-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #e5e7eb;
  transition: all 0.3s ease;
}

.step-dot.step-active {
  background: #1677ff;
  box-shadow: 0 0 0 4px rgba(22, 119, 255, 0.2);
}

.step-dot.step-completed {
  background: #52c41a;
}

.step-line {
  width: 100px;
  height: 2px;
  background: #e5e7eb;
  margin: 0 8px;
  transition: all 0.3s ease;
}

.step-line.step-line-completed {
  background: #52c41a;
}

.step-hint {
  text-align: center;
  color: #6b7280;
  font-size: 14px;
  margin-bottom: 24px;
}

.code-input-wrapper {
  display: flex;
  gap: 12px;
}

.code-input {
  flex: 1;
}

.code-btn {
  height: 44px;
  padding: 0 20px;
  font-weight: 600;
  border-radius: var(--radius-sm);
  white-space: nowrap;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  transition: all 0.3s ease;
}

.code-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
}

.code-btn:disabled {
  background: #f0f2f5;
  color: #86909c;
}

.button-group {
  display: flex;
  gap: 12px;
}

.cancel-btn {
  height: 44px;
  padding: 0 24px;
  font-weight: 600;
  border-radius: var(--radius-sm);
  transition: all 0.3s ease;
}

.error-text {
  color: #f56c6c;
  font-size: 12px;
  line-height: 1;
  padding-top: 4px;
}

.back-btn {
  height: 44px;
  padding: 0 24px;
  font-weight: 600;
  border-radius: var(--radius-sm);
  transition: all 0.3s ease;
}

.add-btn {
  height: 40px;
  padding: 0 20px;
  font-weight: 600;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s ease;
}

.add-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(22, 119, 255, 0.3);
}

.add-btn .icon {
  font-size: 16px;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.address-card {
  border: 1px solid #e5e7eb;
  border-radius: var(--radius-md);
  padding: 20px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  transition: all 0.3s ease;
}

.address-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
  border-color: var(--primary-opacity-10);
}

.address-content {
  flex: 1;
}

.address-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.receiver-name {
  font-weight: 600;
  color: #1f2937;
  font-size: 16px;
}

.receiver-phone {
  color: #6b7280;
  font-size: 14px;
}

.default-tag {
  font-weight: 500;
}

.address-detail {
  color: #4b5563;
  margin: 0;
  line-height: 1.6;
}

.address-actions {
  display: flex;
  gap: 8px;
}

.empty-state {
  padding: 60px 0;
}

.address-dialog :deep(.el-dialog__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #f3f4f6;
}

.address-dialog :deep(.el-dialog__body) {
  padding: 24px;
}

.address-dialog :deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #f3f4f6;
}

@media (max-width: 1024px) {
  .profile-layout {
    flex-direction: column;
  }
  
  .profile-sidebar {
    width: 100%;
  }
  
  .address-card {
    flex-direction: column;
    gap: 16px;
  }
  
  .address-actions {
    width: 100%;
    justify-content: flex-end;
  }
}

@media (max-width: 640px) {
  .profile-page {
    padding: 12px 0;
  }
  
  .main-card {
    padding: 20px;
  }
  
  .section-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .add-btn {
    width: 100%;
  }
  
  .profile-form {
    max-width: 100%;
  }
}
</style>
