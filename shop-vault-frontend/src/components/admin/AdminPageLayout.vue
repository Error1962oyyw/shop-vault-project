<script setup lang="ts">
defineProps<{
  title: string
  subtitle?: string
  background?: 'default' | 'purple' | 'blue' | 'green'
}>()

const backgroundClasses: Record<string, string> = {
  default: 'bg-default',
  purple: 'bg-purple',
  blue: 'bg-blue',
  green: 'bg-green'
}
</script>

<template>
  <div class="admin-page-container" :class="backgroundClasses[background || 'default']">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">{{ title }}</h2>
        <p v-if="subtitle" class="page-subtitle">{{ subtitle }}</p>
      </div>
      <div class="header-actions">
        <slot name="actions" />
      </div>
    </div>

    <div class="content-card">
      <slot />
    </div>
  </div>
</template>

<style scoped>
.admin-page-container {
  padding: 24px;
  min-height: calc(100vh - 120px);
  animation: fadeIn 0.4s ease-out;
}

.bg-default {
  background: linear-gradient(135deg, #f7f8fa 0%, #f0f5ff 50%, #f7f8fa 100%);
}

.bg-purple {
  background: linear-gradient(135deg, #f7f8fa 0%, #f0e6ff 50%, #f5f0ff 100%);
}

.bg-blue {
  background: linear-gradient(135deg, #f7f8fa 0%, #e6fffb 50%, #f0f5ff 100%);
}

.bg-green {
  background: linear-gradient(135deg, #f7f8fa 0%, #e6fff0 50%, #f0fff5 100%);
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px 24px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.header-left {
  flex: 1;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 4px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.content-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
</style>
