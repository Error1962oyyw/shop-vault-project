<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  title?: string
  subtitle?: string
  size?: 'sm' | 'base' | 'lg' | 'xl'
  shadow?: 'none' | 'sm' | 'md' | 'lg'
  hoverable?: boolean
  clickable?: boolean
  bordered?: boolean
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  size: 'base',
  shadow: 'md',
  hoverable: false,
  clickable: false,
  bordered: false,
  loading: false
})

const emit = defineEmits<{
  click: []
}>()

const cardClass = computed(() => {
  return [
    `card-size-${props.size}`,
    `card-shadow-${props.shadow}`,
    {
      'card-hoverable': props.hoverable,
      'card-clickable': props.clickable,
      'card-bordered': props.bordered,
      'card-loading': props.loading
    }
  ]
})

const handleClick = () => {
  if (props.clickable && !props.loading) {
    emit('click')
  }
}
</script>

<template>
  <div class="card" :class="cardClass" @click="handleClick">
    <div v-if="loading" class="card-loading-overlay">
      <div class="loading-spinner"></div>
    </div>
    
    <div v-if="title || $slots.header" class="card-header">
      <div class="header-content">
        <h3 v-if="title" class="card-title">{{ title }}</h3>
        <p v-if="subtitle" class="card-subtitle">{{ subtitle }}</p>
      </div>
      <div v-if="$slots.extra" class="header-extra">
        <slot name="extra"></slot>
      </div>
    </div>
    
    <div class="card-body">
      <slot></slot>
    </div>
    
    <div v-if="$slots.footer" class="card-footer">
      <slot name="footer"></slot>
    </div>
  </div>
</template>

<style scoped>
.card {
  position: relative;
  background: var(--background-color-card);
  border-radius: var(--radius-lg);
  transition: all var(--transition-normal);
  overflow: hidden;
}

.card-size-sm {
  padding: var(--card-padding-sm);
}

.card-size-base {
  padding: var(--card-padding-base);
}

.card-size-lg {
  padding: var(--card-padding-lg);
}

.card-size-xl {
  padding: var(--card-padding-xl);
}

.card-shadow-none {
  box-shadow: none;
}

.card-shadow-sm {
  box-shadow: var(--shadow-sm);
}

.card-shadow-md {
  box-shadow: var(--shadow-md);
}

.card-shadow-lg {
  box-shadow: var(--shadow-lg);
}

.card-bordered {
  border: 1px solid var(--border-color-default);
}

.card-hoverable:hover {
  box-shadow: var(--shadow-lg);
  transform: translateY(-4px);
}

.card-clickable {
  cursor: pointer;
  user-select: none;
}

.card-clickable:active {
  transform: scale(0.98);
}

.card-loading {
  pointer-events: none;
}

.card-loading-overlay {
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--border-color-light);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--spacing-4);
  padding-bottom: var(--spacing-4);
  border-bottom: 1px solid var(--border-color-light);
}

.header-content {
  flex: 1;
  min-width: 0;
}

.card-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-color-primary);
  margin: 0 0 var(--spacing-1) 0;
}

.card-subtitle {
  font-size: var(--font-size-sm);
  color: var(--text-color-secondary);
  margin: 0;
}

.header-extra {
  flex-shrink: 0;
  margin-left: var(--spacing-4);
}

.card-body {
  color: var(--text-color-regular);
}

.card-footer {
  margin-top: var(--spacing-4);
  padding-top: var(--spacing-4);
  border-top: 1px solid var(--border-color-light);
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-3);
}
</style>
