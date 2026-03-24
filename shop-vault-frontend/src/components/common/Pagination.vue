<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  total: number
  pageSize?: number
  currentPage?: number
}>()

const emit = defineEmits<{
  (e: 'update:currentPage', page: number): void
  (e: 'update:pageSize', size: number): void
  (e: 'change', page: number): void
}>()

const currentPageModel = computed({
  get: () => props.currentPage || 1,
  set: (val: number) => {
    emit('update:currentPage', val)
    emit('change', val)
  }
})

const pageSizeModel = computed({
  get: () => props.pageSize || 12,
  set: (val: number) => emit('update:pageSize', val)
})
</script>

<template>
  <div class="flex justify-center py-8">
    <el-pagination
      v-model:currentPage="currentPageModel"
      v-model:pageSize="pageSizeModel"
      :pageSizes="[12, 24, 36, 48]"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
    />
  </div>
</template>
