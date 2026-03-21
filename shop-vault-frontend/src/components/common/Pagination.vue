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

const currentPage = computed({
  get: () => props.currentPage || 1,
  set: (val) => emit('update:currentPage', val)
})

const pageSize = computed({
  get: () => props.pageSize || 12,
  set: (val) => emit('update:pageSize', val)
})

const handleCurrentChange = (val: number) => {
  emit('change', val)
}
</script>

<template>
  <div class="flex justify-center py-8">
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :page-sizes="[12, 24, 36, 48]"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      @current-change="handleCurrentChange"
    />
  </div>
</template>
