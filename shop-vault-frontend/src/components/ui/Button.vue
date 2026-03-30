<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  type?: 'primary' | 'secondary' | 'success' | 'warning' | 'danger' | 'info' | 'text'
  size?: 'sm' | 'base' | 'lg' | 'xl'
  disabled?: boolean
  loading?: boolean
  block?: boolean
  icon?: string
  round?: boolean
  circle?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  type: 'primary',
  size: 'base',
  disabled: false,
  loading: false,
  block: false,
  round: false,
  circle: false
})

const emit = defineEmits<{
  click: [event: MouseEvent]
}>()

const buttonClass = computed(() => {
  return [
    `btn-type-${props.type}`,
    `btn-size-${props.size}`,
    {
      'btn-disabled': props.disabled || props.loading,
      'btn-loading': props.loading,
      'btn-block': props.block,
      'btn-round': props.round,
      'btn-circle': props.circle
    }
  ]
})

const handleClick = (event: MouseEvent) => {
  if (!props.disabled && !props.loading) {
    emit('click', event)
  }
}
</script>

<template>
  <button
    class="custom-button"
    :class="buttonClass"
    :disabled="disabled || loading"
    @click="handleClick"
  >
    <span v-if="loading" class="btn-loading-icon">
      <svg viewBox="0 0 1024 1024" class="loading-svg">
        <path fill="currentColor" d="M512 64a32 32 0 0 1 32 32v192a32 32 0 0 1-64 0V96a32 32 0 0 1 32-32zm0 640a32 32 0 0 1 32 32v192a32 32 0 0 1-64 0V736a32 32 0 0 1 32-32zm448-192a32 32 0 0 1-32 32H736a32 32 0 1 1 0-64h192a32 32 0 0 1 32 32zm-640 0a32 32 0 0 1-32 32H96a32 32 0 0 1 0-64h192a32 32 0 0 1 32 32zM195.2 195.2a32 32 0 0 1 45.248 0L376.32 331.008a32 32 0 0 1-45.248 45.248L195.2 240.448a32 32 0 0 1 0-45.248zm452.544 452.544a32 32 0 0 1 45.248 0L828.8 783.552a32 32 0 0 1-45.248 45.248L647.744 692.992a32 32 0 0 1 0-45.248zM828.8 195.2a32 32 0 0 1 0 45.248L692.992 376.32a32 32 0 0 1-45.248-45.248L783.552 195.2a32 32 0 0 1 45.248 0zM376.32 647.744a32 32 0 0 1 0 45.248L240.448 828.8a32 32 0 0 1-45.248-45.248l135.808-135.808a32 32 0 0 1 45.248 0z"/>
      </svg>
    </span>
    <span v-if="icon && !loading" class="btn-icon">{{ icon }}</span>
    <span v-if="$slots.default" class="btn-text">
      <slot></slot>
    </span>
  </button>
</template>

<style scoped>
.custom-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-2);
  border: none;
  cursor: pointer;
  font-family: var(--font-family-base);
  font-weight: var(--font-weight-medium);
  transition: all var(--transition-base);
  white-space: nowrap;
  user-select: none;
}

.btn-size-sm {
  height: var(--button-height-sm);
  padding: 0 var(--spacing-3);
  font-size: var(--font-size-sm);
  border-radius: var(--radius-sm);
}

.btn-size-base {
  height: var(--button-height-base);
  padding: 0 var(--spacing-5);
  font-size: var(--font-size-sm);
  border-radius: var(--radius-md);
}

.btn-size-lg {
  height: var(--button-height-lg);
  padding: 0 var(--spacing-6);
  font-size: var(--font-size-base);
  border-radius: var(--radius-lg);
}

.btn-size-xl {
  height: var(--button-height-xl);
  padding: 0 var(--spacing-8);
  font-size: var(--font-size-lg);
  border-radius: var(--radius-xl);
}

.btn-type-primary {
  background: var(--gradient-primary);
  color: var(--text-color-on-primary);
  box-shadow: var(--shadow-primary);
}

.btn-type-primary:hover:not(.btn-disabled) {
  box-shadow: var(--shadow-lg);
  transform: translateY(-2px);
}

.btn-type-primary:active:not(.btn-disabled) {
  transform: translateY(0);
}

.btn-type-secondary {
  background: var(--background-color-card);
  color: var(--text-color-primary);
  border: 1px solid var(--border-color-default);
}

.btn-type-secondary:hover:not(.btn-disabled) {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.btn-type-success {
  background: var(--gradient-success);
  color: var(--text-color-on-success);
  box-shadow: var(--shadow-success);
}

.btn-type-success:hover:not(.btn-disabled) {
  box-shadow: var(--shadow-lg);
  transform: translateY(-2px);
}

.btn-type-warning {
  background: var(--gradient-warning);
  color: var(--text-color-on-warning);
  box-shadow: var(--shadow-warning);
}

.btn-type-warning:hover:not(.btn-disabled) {
  box-shadow: var(--shadow-lg);
  transform: translateY(-2px);
}

.btn-type-danger {
  background: var(--gradient-danger);
  color: var(--text-color-on-danger);
  box-shadow: var(--shadow-danger);
}

.btn-type-danger:hover:not(.btn-disabled) {
  box-shadow: var(--shadow-lg);
  transform: translateY(-2px);
}

.btn-type-info {
  background: var(--color-info);
  color: var(--text-color-on-primary);
}

.btn-type-info:hover:not(.btn-disabled) {
  background: var(--color-info-600);
}

.btn-type-text {
  background: transparent;
  color: var(--color-primary);
  padding: 0 var(--spacing-2);
}

.btn-type-text:hover:not(.btn-disabled) {
  background: var(--color-primary-50);
}

.btn-disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-loading {
  position: relative;
}

.btn-loading-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.loading-svg {
  width: 16px;
  height: 16px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.btn-icon {
  display: inline-flex;
  align-items: center;
  font-size: 1.2em;
}

.btn-text {
  display: inline-flex;
  align-items: center;
}

.btn-block {
  width: 100%;
}

.btn-round {
  border-radius: var(--radius-full);
}

.btn-circle {
  border-radius: 50%;
  padding: 0;
}

.btn-circle.btn-size-sm {
  width: var(--button-height-sm);
}

.btn-circle.btn-size-base {
  width: var(--button-height-base);
}

.btn-circle.btn-size-lg {
  width: var(--button-height-lg);
}

.btn-circle.btn-size-xl {
  width: var(--button-height-xl);
}
</style>
