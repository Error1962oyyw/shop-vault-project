<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  title: string
  subtitle?: string
  icon?: string
  gradient?: 'primary' | 'orange' | 'purple' | 'success' | 'warning' | 'danger'
  showBack?: boolean
  backText?: string
}

const props = withDefaults(defineProps<Props>(), {
  gradient: 'primary',
  showBack: false,
  backText: '返回'
})

const emit = defineEmits<{
  back: []
}>()

const gradientClass = computed(() => {
  const gradients = {
    primary: 'gradient-primary',
    orange: 'gradient-orange',
    purple: 'gradient-purple',
    success: 'gradient-success',
    warning: 'gradient-warning',
    danger: 'gradient-danger'
  }
  return gradients[props.gradient]
})

const handleBack = () => {
  emit('back')
}
</script>

<template>
  <div class="page-header" :class="gradientClass">
    <div class="header-bg"></div>
    <div class="header-content">
      <div class="header-left">
        <button v-if="showBack" class="back-button" @click="handleBack">
          <svg viewBox="0 0 1024 1024" class="back-icon">
            <path fill="currentColor" d="M609.408 149.376 277.76 489.6a32 32 0 0 0 0 44.672l331.648 340.352a29.12 29.12 0 0 0 41.728 0 30.59 30.59 0 0 0 0-42.752L339.264 511.936l311.872-319.872a30.59 30.59 0 0 0 0-42.688 29.12 29.12 0 0 0-41.728 0"/>
          </svg>
          <span>{{ backText }}</span>
        </button>
        <h1 class="header-title">
          <span v-if="icon" class="title-icon">{{ icon }}</span>
          {{ title }}
        </h1>
        <p v-if="subtitle" class="header-subtitle">{{ subtitle }}</p>
      </div>
      <div class="header-right">
        <slot name="extra"></slot>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-header {
  position: relative;
  border-radius: var(--radius-xl);
  overflow: hidden;
  margin-bottom: var(--spacing-6);
  margin-top: var(--spacing-6);
}

.header-bg {
  position: absolute;
  inset: 0;
}

.gradient-primary .header-bg {
  background: var(--gradient-primary);
}

.gradient-orange .header-bg {
  background: var(--gradient-orange);
}

.gradient-purple .header-bg {
  background: var(--gradient-purple);
}

.gradient-success .header-bg {
  background: var(--gradient-success);
}

.gradient-warning .header-bg {
  background: var(--gradient-warning);
}

.gradient-danger .header-bg {
  background: var(--gradient-danger);
}

.header-content {
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-10) var(--spacing-10);
  color: #fff;
  gap: var(--spacing-10);
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-2);
  flex: 1;
  min-width: 0;
}

.back-button {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-2);
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  padding: var(--spacing-2) var(--spacing-4);
  border-radius: var(--radius-md);
  color: #fff;
  font-size: var(--font-size-sm);
  cursor: pointer;
  transition: all var(--transition-base);
  margin-bottom: var(--spacing-2);
  width: fit-content;
}

.back-button:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateX(-4px);
}

.back-icon {
  width: 16px;
  height: 16px;
}

.header-title {
  font-size: var(--font-size-4xl);
  font-weight: var(--font-weight-bold);
  margin: 0;
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: var(--spacing-3);
}

.title-icon {
  font-size: var(--font-size-3xl);
}

.header-subtitle {
  font-size: var(--font-size-base);
  opacity: 0.9;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--spacing-4);
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    text-align: center;
    gap: var(--spacing-5);
    padding: var(--spacing-8) var(--spacing-6);
  }

  .header-left {
    align-items: center;
  }

  .header-title {
    font-size: var(--font-size-2xl);
  }

  .back-button {
    align-self: flex-start;
  }
}
</style>
