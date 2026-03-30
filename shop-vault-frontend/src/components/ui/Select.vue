<script setup lang="ts">
import { ref, computed } from 'vue'

interface Option {
  label: string
  value: string | number
  disabled?: boolean
}

interface Props {
  modelValue?: string | number
  options: Option[]
  size?: 'sm' | 'base' | 'lg'
  placeholder?: string
  disabled?: boolean
  clearable?: boolean
  error?: string
  label?: string
  required?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  size: 'base',
  placeholder: '请选择',
  disabled: false,
  clearable: false,
  required: false
})

const emit = defineEmits<{
  'update:modelValue': [value: string | number]
  change: [value: string | number]
}>()

const isOpen = ref(false)

const selectClass = computed(() => {
  return [
    `select-size-${props.size}`,
    {
      'select-disabled': props.disabled,
      'select-error': props.error,
      'select-open': isOpen.value
    }
  ]
})

const selectedLabel = computed(() => {
  const option = props.options.find(opt => opt.value === props.modelValue)
  return option ? option.label : ''
})

const handleSelect = (option: Option) => {
  if (option.disabled) return
  emit('update:modelValue', option.value)
  emit('change', option.value)
  isOpen.value = false
}

const handleClear = (event: Event) => {
  event.stopPropagation()
  emit('update:modelValue', '')
  emit('change', '')
}

const toggleDropdown = () => {
  if (!props.disabled) {
    isOpen.value = !isOpen.value
  }
}
</script>

<template>
  <div class="form-item">
    <label v-if="label" class="form-label">
      {{ label }}
      <span v-if="required" class="required-mark">*</span>
    </label>
    <div class="select-wrapper" :class="selectClass" @click="toggleDropdown">
      <div class="select-input">
        <span v-if="selectedLabel" class="select-value">{{ selectedLabel }}</span>
        <span v-else class="select-placeholder">{{ placeholder }}</span>
      </div>
      <span class="select-arrow" :class="{ 'arrow-up': isOpen }">
        <svg viewBox="0 0 1024 1024">
          <path fill="currentColor" d="M512 714.667c-17.067 0-32-6.4-44.8-19.2L147.2 375.467c-25.6-25.6-25.6-64 0-89.6s64-25.6 89.6 0L512 561.067l275.2-275.2c25.6-25.6 64-25.6 89.6 0s25.6 64 0 89.6L556.8 695.467c-12.8 12.8-27.733 19.2-44.8 19.2z"/>
        </svg>
      </span>
      <span v-if="clearable && selectedLabel" class="select-clear" @click="handleClear">
        <svg viewBox="0 0 1024 1024">
          <path fill="currentColor" d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64zm165.4 618.2-66-.3L512 563.4l-99.3 118.4-66.1.3c-4.4 0-8-3.5-8-8 0-1.9.7-3.7 1.9-5.2l130.1-155L340.5 358.6a8.32 8.32 0 0 1-1.9-5.2c0-4.4 3.6-8 8-8l66.1.3L512 464.6l99.3-118.4 66-.3c4.4 0 8 3.5 8 8 0 1.9-.7 3.7-1.9 5.2L553.5 514l130 155c1.2 1.5 1.9 3.3 1.9 5.2 0 4.4-3.6 8-8 8z"/>
        </svg>
      </span>
    </div>
    <div v-if="isOpen" class="select-dropdown">
      <div
        v-for="option in options"
        :key="option.value"
        class="select-option"
        :class="{
          'option-selected': option.value === modelValue,
          'option-disabled': option.disabled
        }"
        @click="handleSelect(option)"
      >
        {{ option.label }}
      </div>
    </div>
    <div v-if="error" class="form-error">{{ error }}</div>
  </div>
</template>

<style scoped>
.form-item {
  margin-bottom: var(--spacing-4);
  position: relative;
}

.form-label {
  display: block;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--text-color-primary);
  margin-bottom: var(--spacing-2);
}

.required-mark {
  color: var(--color-danger);
  margin-left: var(--spacing-1);
}

.select-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  width: 100%;
  background: var(--background-color-card);
  border: 1px solid var(--border-color-default);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-base);
}

.select-wrapper:hover:not(.select-disabled) {
  border-color: var(--color-primary-light);
}

.select-wrapper.select-open {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 2px rgba(22, 119, 255, 0.1);
}

.select-size-sm {
  height: var(--input-height-sm);
  padding: 0 var(--spacing-3);
}

.select-size-base {
  height: var(--input-height-base);
  padding: 0 var(--spacing-4);
}

.select-size-lg {
  height: var(--input-height-lg);
  padding: 0 var(--spacing-5);
}

.select-disabled {
  background: var(--background-color-secondary);
  cursor: not-allowed;
  opacity: 0.6;
}

.select-error {
  border-color: var(--color-danger);
}

.select-input {
  flex: 1;
  overflow: hidden;
}

.select-value {
  font-size: var(--font-size-sm);
  color: var(--text-color-primary);
}

.select-placeholder {
  font-size: var(--font-size-sm);
  color: var(--text-color-placeholder);
}

.select-arrow {
  display: inline-flex;
  align-items: center;
  margin-left: var(--spacing-2);
  color: var(--text-color-secondary);
  transition: transform var(--transition-base);
}

.select-arrow svg {
  width: 12px;
  height: 12px;
}

.select-arrow.arrow-up {
  transform: rotate(180deg);
}

.select-clear {
  display: inline-flex;
  align-items: center;
  margin-left: var(--spacing-2);
  color: var(--text-color-secondary);
  transition: color var(--transition-base);
}

.select-clear svg {
  width: 14px;
  height: 14px;
}

.select-clear:hover {
  color: var(--text-color-primary);
}

.select-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: var(--spacing-1);
  background: var(--background-color-card);
  border: 1px solid var(--border-color-default);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  max-height: 256px;
  overflow-y: auto;
  z-index: var(--z-index-dropdown);
}

.select-option {
  padding: var(--spacing-3) var(--spacing-4);
  font-size: var(--font-size-sm);
  color: var(--text-color-primary);
  cursor: pointer;
  transition: background var(--transition-base);
}

.select-option:hover:not(.option-disabled) {
  background: var(--color-primary-50);
}

.select-option.option-selected {
  background: var(--color-primary-100);
  color: var(--color-primary);
  font-weight: var(--font-weight-medium);
}

.select-option.option-disabled {
  color: var(--text-color-disabled);
  cursor: not-allowed;
}

.form-error {
  font-size: var(--font-size-xs);
  color: var(--color-danger);
  margin-top: var(--spacing-1);
}
</style>
