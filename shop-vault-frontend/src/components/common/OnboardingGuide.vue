<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { 
  getOnboardingStatus, 
  getOnboardingCategories, 
  completeOnboarding, 
  skipOnboarding 
} from '@/api/onboarding'
import { ElMessage } from 'element-plus'
import type { Category } from '@/types/api'

const emit = defineEmits<{
  (e: 'complete'): void
  (e: 'skip'): void
}>()

const visible = ref(false)
const loading = ref(false)
const categories = ref<Category[]>([])
const selectedCategoryIds = ref<number[]>([])
const currentStep = ref(0)

const steps = [
  { title: '欢迎来到小铺宝库', description: '让我们为您推荐最适合的商品' },
  { title: '选择您感兴趣的分类', description: '我们会根据您的喜好推荐商品' },
  { title: '开始您的购物之旅', description: '享受个性化的购物体验' }
]

const canProceed = computed(() => {
  if (currentStep.value === 1) {
    return selectedCategoryIds.value.length >= 1
  }
  return true
})

const checkOnboardingStatus = async () => {
  try {
    const status = await getOnboardingStatus()
    if (!status.completed && !status.skipped) {
      visible.value = true
      selectedCategoryIds.value = status.selectedCategories || []
      currentStep.value = status.currentStep || 0
      await fetchCategories()
    }
  } catch (error) {
    console.error('获取引导状态失败', error)
  }
}

const fetchCategories = async () => {
  try {
    categories.value = await getOnboardingCategories()
  } catch (error) {
    console.error('获取分类失败', error)
  }
}

const handleNext = () => {
  if (currentStep.value < steps.length - 1) {
    currentStep.value++
  }
}

const handlePrev = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

const handleComplete = async () => {
  if (selectedCategoryIds.value.length === 0) {
    ElMessage.warning('请至少选择一个分类')
    return
  }

  try {
    loading.value = true
    await completeOnboarding({ categoryIds: selectedCategoryIds.value })
    ElMessage.success('设置成功！')
    visible.value = false
    emit('complete')
  } catch (error) {
    console.error('完成引导失败', error)
  } finally {
    loading.value = false
  }
}

const handleSkip = async () => {
  try {
    await skipOnboarding()
    visible.value = false
    emit('skip')
  } catch (error) {
    console.error('跳过引导失败', error)
  }
}

const toggleCategory = (categoryId: number) => {
  const index = selectedCategoryIds.value.indexOf(categoryId)
  if (index > -1) {
    selectedCategoryIds.value.splice(index, 1)
  } else {
    selectedCategoryIds.value.push(categoryId)
  }
}

onMounted(() => {
  checkOnboardingStatus()
})

defineExpose({
  show: () => { visible.value = true },
  hide: () => { visible.value = false }
})
</script>

<template>
  <el-dialog 
    v-model="visible" 
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :show-close="false"
    width="600px"
    class="onboarding-dialog"
  >
    <div class="text-center py-4">
      <div class="flex justify-center gap-2 mb-6">
        <div 
          v-for="(_, index) in steps" 
          :key="index"
          class="w-8 h-1 rounded-full transition-colors"
          :class="index <= currentStep ? 'bg-purple-500' : 'bg-gray-200'"
        />
      </div>

      <transition name="fade" mode="out-in">
        <div :key="currentStep">
          <div v-if="currentStep === 0" class="py-8">
            <div class="w-24 h-24 mx-auto bg-purple-100 rounded-full flex items-center justify-center mb-6">
              <el-icon size="48" class="text-purple-500"><ShoppingBag /></el-icon>
            </div>
            <h2 class="text-2xl font-bold text-gray-800 mb-2">{{ steps[0].title }}</h2>
            <p class="text-gray-500">{{ steps[0].description }}</p>
          </div>

          <div v-else-if="currentStep === 1" class="py-4">
            <h2 class="text-xl font-bold text-gray-800 mb-2">{{ steps[1].title }}</h2>
            <p class="text-gray-500 mb-6">{{ steps[1].description }}</p>
            
            <div class="grid grid-cols-4 gap-3 max-h-64 overflow-y-auto">
              <div 
                v-for="category in categories" 
                :key="category.id"
                class="category-item p-4 rounded-lg border-2 cursor-pointer transition-all"
                :class="[
                  selectedCategoryIds.includes(category.id) 
                    ? 'border-purple-500 bg-purple-50' 
                    : 'border-gray-200 hover:border-purple-300'
                ]"
                @click="toggleCategory(category.id)"
              >
                <div 
                  class="w-12 h-12 mx-auto rounded-full flex items-center justify-center mb-2"
                  :class="selectedCategoryIds.includes(category.id) ? 'bg-purple-500 text-white' : 'bg-gray-100'"
                >
                  <el-icon size="24">
                    <component :is="category.icon || 'Goods'" />
                  </el-icon>
                </div>
                <div class="text-sm text-center text-gray-700">{{ category.name }}</div>
              </div>
            </div>
            
            <p class="text-sm text-gray-400 mt-4">
              已选择 {{ selectedCategoryIds.length }} 个分类
            </p>
          </div>

          <div v-else-if="currentStep === 2" class="py-8">
            <div class="w-24 h-24 mx-auto bg-green-100 rounded-full flex items-center justify-center mb-6">
              <el-icon size="48" class="text-green-500"><CircleCheck /></el-icon>
            </div>
            <h2 class="text-2xl font-bold text-gray-800 mb-2">{{ steps[2].title }}</h2>
            <p class="text-gray-500">{{ steps[2].description }}</p>
          </div>
        </div>
      </transition>
    </div>

    <template #footer>
      <div class="flex justify-between items-center">
        <el-button 
          v-if="currentStep === 0"
          text 
          @click="handleSkip"
        >
          跳过引导
        </el-button>
        <el-button 
          v-else
          @click="handlePrev"
        >
          上一步
        </el-button>
        
        <div class="flex gap-2">
          <el-button 
            v-if="currentStep < steps.length - 1"
            type="primary" 
            :disabled="!canProceed"
            @click="handleNext"
          >
            下一步
          </el-button>
          <el-button 
            v-else
            type="primary" 
            :loading="loading"
            @click="handleComplete"
          >
            开始购物
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.onboarding-dialog :deep(.el-dialog__header) {
  display: none;
}

.onboarding-dialog :deep(.el-dialog__body) {
  padding: 20px;
}

.onboarding-dialog :deep(.el-dialog__footer) {
  padding: 20px;
  border-top: 1px solid #eee;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.category-item {
  user-select: none;
}
</style>
