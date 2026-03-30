<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  modelValue?: string | number
  type?: 'text' | 'password' | 'email' | 'number' | 'tel' | 'url'
  size?: 'sm' | 'base' | 'lg'
  placeholder?: string
  disabled?: boolean
  readonly?: boolean
  clearable?: boolean
  showPassword?: boolean
  error?: string
  label?: string
  required?: boolean
  prefixIcon?: string
  suffixIcon?: string
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  type: 'text',
  size: 'base',
  placeholder: '',
  disabled: false,
  readonly: false,
  clearable: false,
  showPassword: false,
  required: false
})

const emit = defineEmits<{
  'update:modelValue': [value: string | number]
  focus: [event: FocusEvent]
  blur: [event: FocusEvent]
  clear: []
}>()

const inputClass = computed(() => {
  return [
    `input-size-${props.size}`,
    {
      'input-disabled': props.disabled,
      'input-error': props.error,
      'input-with-prefix': props.prefixIcon,
      'input-with-suffix': props.suffixIcon || props.clearable || props.showPassword
    }
  ]
})

const handleInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  emit('update:modelValue', target.value)
}

const handleFocus = (event: FocusEvent) => {
  emit('focus', event)
}

const handleBlur = (event: FocusEvent) => {
  emit('blur', event)
}

const handleClear = () => {
  emit('update:modelValue', '')
  emit('clear')
}
</script>

<template>
  <div class="form-item">
    <label v-if="label" class="form-label">
      {{ label }}
      <span v-if="required" class="required-mark">*</span>
    </label>
    <div class="input-wrapper" :class="inputClass">
      <span v-if="prefixIcon" class="input-prefix">
        <span class="prefix-icon">{{ prefixIcon }}</span>
      </span>
      <input
        :type="type"
        :value="modelValue"
        :placeholder="placeholder"
        :disabled="disabled"
        :readonly="readonly"
        class="input-inner"
        @input="handleInput"
        @focus="handleFocus"
        @blur="handleBlur"
      />
      <span v-if="clearable && modelValue" class="input-suffix" @click="handleClear">
        <svg viewBox="0 0 1024 1024" class="clear-icon">
          <path fill="currentColor" d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64zm165.4 618.2 -66-.3L512 563.4l-99.3 118.4-66.1.3c-4.4 0-8-3.5-8-8 0-1.9.7-3.7 1.9-5.2l130.1-155L340.5 358.6a8.32 8.32 0 0 1-1.9-5.2c0-4.4 3.6-8 8-8l66.1.3L512 464.6l99.3-118.4 66-.3c4.4 0 8 3.5 8 8 0 1.9-.7 3.7-1.9 5.2L553.5 514l130 155c1.2 1.5 1.9 3.3 1.9 5.2 0 4.4-3.6 8-8 8z"/>
        </svg>
      </span>
      <span v-if="suffixIcon && !clearable" class="input-suffix">
        <span class="suffix-icon">{{ suffixIcon }}</span>
      </span>
    </div>
    <div v-if="error" class="form-error">{{ error }}</div>
  </div>
</template>

<style scoped>
.form-item {
  margin-bottom: var(--spacing-4);
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

.input-wrapper {
  position: relative;
  display: inline-flex;
  align-items: center;
  width: 100%;
  background: var(--background-color-card);
  border: 1px solid var(--border-color-default);
  border-radius: var(--radius-md);
  transition: all var(--transition-base);
}

.input-wrapper:hover:not(.input-disabled) {
  border-color: var(--color-primary-light);
}

.input-wrapper:focus-within:not(.input-disabled) {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 2px rgba(22, 119, 255, 0.1);
}

.input-size-sm {
  height: var(--input-height-sm);
}

.input-size-base {
  height: var(--input-height-base);
}

.input-size-lg {
  height: var(--input-height-lg);
}

.input-disabled {
  background: var(--background-color-secondary);
  cursor: not-allowed;
  opacity: 0.6;
}

.input-error {
  border-color: var(--color-danger);
}

.input-error:focus-within {
  border-color: var(--color-danger);
  box-shadow: 0 0 0 2px rgba(255, 77, 79, 0.1);
}

.input-prefix,
.input-suffix {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 var(--spacing-3);
  color: var(--text-color-secondary);
}

.input-suffix {
  cursor: pointer;
}

.clear-icon {
  width: 16px;
  height: 16px;
  color: var(--text-color-secondary);
  transition: color var(--transition-base);
}

.clear-icon:hover {
  color: var(--text-color-primary);
}

.prefix-icon,
.suffix-icon {
  font-size: var(--font-size-base);
}

.input-inner {
  flex: 1;
  height: 100%;
  padding: 0 var(--spacing-3);
  border: none;
  outline: none;
  background: transparent;
  font-size: var(--font-size-sm);
  color: var(--text-color-primary);
  font-family: var(--font-family-base);
}

.input-inner::placeholder {
  color: var(--text-color-placeholder);
}

.input-inner:disabled {
  cursor: not-allowed;
}

.input-with-prefix .input-inner {
  padding-left: 0;
}

.input-with-suffix .input-inner {
  padding-right: 0;
}

.form-error {
  font-size: var(--font-size-xs);
  color: var(--color-danger);
  margin-top: var(--spacing-1);
}
</style>
