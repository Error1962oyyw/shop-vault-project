<script setup lang="ts">
interface Props {
  type?: 'empty' | 'error' | 'no-data' | 'no-result' | 'network-error'
  title?: string
  description?: string
  icon?: string
}

const props = withDefaults(defineProps<Props>(), {
  type: 'empty'
})

const emit = defineEmits<{
  action: []
}>()

const defaultConfig = {
  empty: {
    title: '暂无数据',
    description: '这里还没有任何内容',
    icon: '📭'
  },
  error: {
    title: '出错了',
    description: '抱歉，发生了错误',
    icon: '❌'
  },
  'no-data': {
    title: '没有数据',
    description: '当前没有任何数据',
    icon: '📊'
  },
  'no-result': {
    title: '没有找到结果',
    description: '尝试其他搜索条件',
    icon: '🔍'
  },
  'network-error': {
    title: '网络错误',
    description: '请检查网络连接后重试',
    icon: '🌐'
  }
}

const config = computed(() => ({
  ...defaultConfig[props.type],
  title: props.title || defaultConfig[props.type].title,
  description: props.description || defaultConfig[props.type].description,
  icon: props.icon || defaultConfig[props.type].icon
}))

import { computed } from 'vue'
</script>

<template>
  <div class="empty-state">
    <div class="empty-icon">{{ config.icon }}</div>
    <h3 class="empty-title">{{ config.title }}</h3>
    <p class="empty-description">{{ config.description }}</p>
    <slot name="action">
      <button v-if="type === 'error' || type === 'network-error'" class="action-button" @click="emit('action')">
        重试
      </button>
    </slot>
  </div>
</template>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-16) var(--spacing-6);
  text-align: center;
  min-height: 400px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: var(--spacing-6);
  opacity: 0.8;
}

.empty-title {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-semibold);
  color: var(--text-color-primary);
  margin: 0 0 var(--spacing-3) 0;
}

.empty-description {
  font-size: var(--font-size-base);
  color: var(--text-color-secondary);
  margin: 0 0 var(--spacing-6) 0;
  max-width: 400px;
}

.action-button {
  padding: var(--spacing-3) var(--spacing-6);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: white;
  background: var(--gradient-primary);
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-base);
}

.action-button:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-primary);
}

.action-button:active {
  transform: scale(0.98);
}

@media (max-width: 768px) {
  .empty-state {
    padding: var(--spacing-12) var(--spacing-4);
    min-height: 300px;
  }
  
  .empty-icon {
    font-size: 48px;
  }
  
  .empty-title {
    font-size: var(--font-size-lg);
  }
  
  .empty-description {
    font-size: var(--font-size-sm);
  }
}
</style>
