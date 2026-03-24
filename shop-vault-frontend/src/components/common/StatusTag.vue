<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  status: number
  statusMap?: Record<number, { text: string; type: 'success' | 'warning' | 'danger' | 'info' | 'primary' }>
}>()

const defaultStatusMap: Record<number, { text: string; type: 'success' | 'warning' | 'danger' | 'info' | 'primary' }> = {
  0: { text: '未开始', type: 'info' },
  1: { text: '进行中', type: 'success' },
  2: { text: '已结束', type: 'danger' }
}

const statusInfo = computed(() => {
  const map = props.statusMap || defaultStatusMap
  return map[props.status] || { text: '未知', type: 'info' }
})
</script>

<template>
  <el-tag :type="statusInfo.type" size="small">
    {{ statusInfo.text }}
  </el-tag>
</template>
