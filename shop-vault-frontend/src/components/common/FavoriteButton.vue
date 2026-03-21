<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { toggleFavorite } from '@/api/product'

const props = defineProps<{
  productId: number
  isFavorite: boolean
}>()

const emit = defineEmits<{
  (e: 'update:isFavorite', value: boolean): void
  (e: 'change'): void
}>()

const loading = ref(false)
const isFav = computed({
  get: () => props.isFavorite,
  set: (val) => emit('update:isFavorite', val)
})

const handleToggle = async () => {
  loading.value = true
  try {
    const result = await toggleFavorite(props.productId)
    isFav.value = result
    ElMessage.success(result ? '已收藏' : '已取消收藏')
    emit('change')
  } catch (error) {
    console.error('收藏操作失败', error)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <el-button
    :type="isFav ? 'warning' : 'default'"
    :loading="loading"
    circle
    @click.stop="handleToggle"
  >
    <el-icon>
      <StarFilled v-if="isFav" />
      <Star v-else />
    </el-icon>
  </el-button>
</template>
