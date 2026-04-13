import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { signIn } from '@/api/marketing'
import { useUserStore } from '@/stores/user'

export function useSignIn() {
  const signingIn = ref(false)
  const userStore = useUserStore()

  const handleSignIn = async () => {
    if (!userStore.isLoggedIn) {
      ElMessage.warning('请先登录')
      return false
    }

    signingIn.value = true
    try {
      await signIn()
      ElMessage.success('签到成功，积分+10')
      return true
    } catch (error: any) {
      const msg = error?.response?.data?.msg || error?.message || '签到失败'
      ElMessage.error(msg)
      return false
    } finally {
      signingIn.value = false
    }
  }

  return {
    signingIn,
    handleSignIn
  }
}
