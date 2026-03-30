# 小铺宝库设计规范文档

## 目录

1. [设计系统概述](#设计系统概述)
2. [颜色系统](#颜色系统)
3. [字体系统](#字体系统)
4. [间距系统](#间距系统)
5. [动画规范](#动画规范)
6. [组件库](#组件库)
7. [最佳实践](#最佳实践)

---

## 设计系统概述

小铺宝库设计系统是一套完整的设计语言和组件库，旨在为用户提供一致、美观、易用的购物体验。本设计系统遵循以下原则：

### 核心原则

1. **一致性**：所有页面和组件保持统一的视觉风格和交互行为
2. **可访问性**：确保所有用户都能轻松使用，包括色盲、视力障碍等用户
3. **可扩展性**：设计系统易于扩展和维护，支持未来的功能迭代
4. **性能优先**：优化动画和交互，确保流畅的用户体验

### 技术实现

- 使用 CSS 变量统一管理所有设计属性
- 基于 Vue 3 Composition API 构建组件库
- 支持主题定制和响应式设计

---

## 颜色系统

### 主色调（Primary）

主色调用于主要操作按钮、链接、强调元素等。

```css
--color-primary: #1677ff;        /* 主色 */
--color-primary-light: #4096ff;  /* 浅主色 */
--color-primary-dark: #0958d9;   /* 深主色 */
```

**色阶**：
- 50: `#e6f4ff` - 最浅，用于背景
- 100: `#bae0ff` - 浅色背景
- 200: `#91caff` - 悬停状态
- 300: `#69b1ff` - 禁用状态
- 400: `#4096ff` - 浅主色
- 500: `#1677ff` - 主色
- 600: `#0958d9` - 深主色
- 700: `#003eb3` - 更深
- 800: `#061178` - 深色
- 900: `#030852` - 最深

### 功能色

#### 成功色（Success）
```css
--color-success: #52c41a;
--gradient-success: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
```

#### 警告色（Warning）
```css
--color-warning: #faad14;
--gradient-warning: linear-gradient(135deg, #faad14 0%, #ffc53d 100%);
```

#### 错误色（Danger）
```css
--color-danger: #ff4d4f;
--gradient-danger: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
```

#### 信息色（Info）
```css
--color-info: #1890ff;
```

### 辅助色

#### 橙色系
```css
--color-orange: #fa8c16;
--gradient-orange: linear-gradient(135deg, #fa8c16 0%, #ffc53d 100%);
```

#### 紫色系
```css
--color-purple: #722ed1;
--gradient-purple: linear-gradient(135deg, #722ed1 0%, #9c27b0 100%);
```

### 中性色

```css
--color-neutral-50: #f7f8fa;   /* 页面背景 */
--color-neutral-100: #f2f3f5;  /* 次级背景 */
--color-neutral-200: #e5e6eb;  /* 边框 */
--color-neutral-300: #d0d3d9;  /* 分割线 */
--color-neutral-400: #86909c;  /* 占位符 */
--color-neutral-500: #4e5969;  /* 正文 */
--color-neutral-600: #272e3b;  /* 标题 */
--color-neutral-700: #1d2129;  /* 主标题 */
```

### 文本颜色

```css
--text-color-primary: #1d2129;      /* 主要文本 */
--text-color-regular: #4e5969;      /* 常规文本 */
--text-color-secondary: #86909c;    /* 次要文本 */
--text-color-placeholder: #86909c;  /* 占位符 */
--text-color-disabled: #c9cdd4;     /* 禁用文本 */
```

### 渐变色

```css
--gradient-primary: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
--gradient-success: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
--gradient-warning: linear-gradient(135deg, #faad14 0%, #ffc53d 100%);
--gradient-danger: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
--gradient-orange: linear-gradient(135deg, #fa8c16 0%, #ffc53d 100%);
--gradient-purple: linear-gradient(135deg, #722ed1 0%, #9c27b0 100%);
```

---

## 字体系统

### 字体族

```css
--font-family-base: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif;
--font-family-mono: 'SF Mono', Monaco, 'Cascadia Code', 'Roboto Mono', Consolas, 'Courier New', monospace;
```

### 字体大小

```css
--font-size-xs: 12px;    /* 辅助文本、标签 */
--font-size-sm: 14px;    /* 小号文本、按钮 */
--font-size-base: 16px;  /* 基础文本 */
--font-size-lg: 18px;    /* 大号文本 */
--font-size-xl: 20px;    /* 小标题 */
--font-size-2xl: 24px;   /* 中标题 */
--font-size-3xl: 28px;   /* 大标题 */
--font-size-4xl: 32px;   /* 页面标题 */
--font-size-5xl: 40px;   /* 特大标题 */
--font-size-6xl: 48px;   /* 超大标题 */
```

### 字重

```css
--font-weight-thin: 100;
--font-weight-light: 300;
--font-weight-normal: 400;    /* 常规 */
--font-weight-medium: 500;    /* 中等 */
--font-weight-semibold: 600;  /* 半粗 */
--font-weight-bold: 700;      /* 粗体 */
--font-weight-extrabold: 800; /* 特粗 */
```

### 行高

```css
--line-height-tight: 1.25;    /* 紧凑 */
--line-height-normal: 1.5;    /* 正常 */
--line-height-relaxed: 1.75;  /* 宽松 */
--line-height-loose: 2;       /* 超宽松 */
```

### 标题层级

| 层级 | 大小 | 字重 | 行高 | 使用场景 |
|------|------|------|------|----------|
| H1 | 32px | 700 | 1.25 | 页面主标题 |
| H2 | 24px | 700 | 1.25 | 区块标题 |
| H3 | 20px | 600 | 1.5 | 卡片标题 |
| H4 | 18px | 600 | 1.5 | 小标题 |
| H5 | 16px | 500 | 1.5 | 标签标题 |
| H6 | 14px | 500 | 1.5 | 辅助标题 |

---

## 间距系统

### 基础间距

采用 4px 为基础单位，所有间距必须是 4 的倍数。

```css
--spacing-0: 0;
--spacing-1: 4px;   /* 极小间距 */
--spacing-2: 8px;   /* 小间距 */
--spacing-3: 12px;  /* 中小间距 */
--spacing-4: 16px;  /* 中等间距 */
--spacing-5: 20px;  /* 中大间距 */
--spacing-6: 24px;  /* 大间距 */
--spacing-8: 32px;  /* 较大间距 */
--spacing-10: 40px; /* 大间距 */
--spacing-12: 48px; /* 超大间距 */
--spacing-16: 64px; /* 巨大间距 */
```

### 组件间距规范

| 组件类型 | 内边距 | 外边距 | 说明 |
|----------|--------|--------|------|
| 按钮（小） | 0 12px | - | height: 32px |
| 按钮（中） | 0 20px | - | height: 40px |
| 按钮（大） | 0 24px | - | height: 48px |
| 卡片（小） | 12px | - | 紧凑卡片 |
| 卡片（中） | 16px | - | 常规卡片 |
| 卡片（大） | 24px | - | 宽松卡片 |
| 输入框（小） | 0 12px | - | height: 32px |
| 输入框（中） | 0 16px | - | height: 40px |
| 输入框（大） | 0 20px | - | height: 48px |

### 圆角

```css
--radius-xs: 4px;     /* 小圆角 */
--radius-sm: 8px;     /* 小圆角 */
--radius-md: 12px;    /* 中圆角 */
--radius-lg: 16px;    /* 大圆角 */
--radius-xl: 20px;    /* 超大圆角 */
--radius-2xl: 28px;   /* 巨大圆角 */
--radius-full: 9999px; /* 圆形 */
```

---

## 动画规范

### 过渡时间

```css
--transition-fast: 150ms ease;    /* 快速过渡 */
--transition-base: 200ms ease;    /* 基础过渡 */
--transition-normal: 300ms ease;  /* 正常过渡 */
--transition-slow: 500ms ease;    /* 慢速过渡 */
```

### 缓动函数

```css
--ease-linear: linear;                              /* 线性 */
--ease-in: cubic-bezier(0.4, 0, 1, 1);             /* 缓入 */
--ease-out: cubic-bezier(0, 0, 0.2, 1);            /* 缓出 */
--ease-in-out: cubic-bezier(0.4, 0, 0.2, 1);       /* 缓入缓出 */
--ease-bounce: cubic-bezier(0.68, -0.55, 0.265, 1.55); /* 弹跳 */
```

### 阴影

```css
--shadow-xs: 0 1px 2px rgba(0, 0, 0, 0.04);              /* 极小阴影 */
--shadow-sm: 0 2px 8px rgba(0, 0, 0, 0.06);              /* 小阴影 */
--shadow-md: 0 4px 16px rgba(0, 0, 0, 0.08);             /* 中阴影 */
--shadow-lg: 0 8px 24px rgba(0, 0, 0, 0.12);             /* 大阴影 */
--shadow-xl: 0 12px 32px rgba(0, 0, 0, 0.15);            /* 超大阴影 */
--shadow-2xl: 0 20px 48px rgba(0, 0, 0, 0.18);           /* 巨大阴影 */
--shadow-primary: 0 8px 24px rgba(22, 119, 255, 0.15);   /* 主色阴影 */
--shadow-success: 0 8px 24px rgba(82, 196, 26, 0.15);    /* 成功阴影 */
--shadow-warning: 0 8px 24px rgba(250, 173, 20, 0.15);   /* 警告阴影 */
--shadow-danger: 0 8px 24px rgba(255, 77, 79, 0.15);     /* 错误阴影 */
```

### 页面淡入动画

所有页面统一使用 `animate-fade-in` 类实现淡入动画：

```css
.animate-fade-in {
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
```

---

## 组件库

### PageHeader 页面头部

统一的页面头部组件，支持多种渐变背景。

#### 使用示例

```vue
<template>
  <PageHeader
    title="积分商城"
    subtitle="用积分兑换精彩好礼"
    icon="🎁"
    gradient="orange"
    :show-back="true"
    @back="handleBack"
  >
    <template #extra>
      <div class="points-display">
        <span class="points-value">{{ userPoints }}</span>
        <span class="points-label">可用积分</span>
      </div>
    </template>
  </PageHeader>
</template>

<script setup lang="ts">
import { PageHeader } from '@/components/ui'

const handleBack = () => {
  // 返回逻辑
}
</script>
```

#### Props

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| title | string | - | 页面标题（必填） |
| subtitle | string | - | 副标题 |
| icon | string | - | 标题图标 |
| gradient | string | 'primary' | 渐变类型：primary/orange/purple/success/warning/danger |
| showBack | boolean | false | 是否显示返回按钮 |
| backText | string | '返回' | 返回按钮文本 |

#### Events

| 事件 | 参数 | 说明 |
|------|------|------|
| back | - | 点击返回按钮时触发 |

#### Slots

| 插槽 | 说明 |
|------|------|
| extra | 右侧额外内容 |

---

### Card 卡片

统一的卡片容器组件。

#### 使用示例

```vue
<template>
  <Card
    title="商品信息"
    subtitle="最新上架"
    size="lg"
    shadow="md"
    :hoverable="true"
    :clickable="true"
    @click="handleCardClick"
  >
    <div class="product-content">
      <!-- 卡片内容 -->
    </div>
    
    <template #extra>
      <Button size="sm">查看详情</Button>
    </template>
    
    <template #footer>
      <Button type="primary">立即购买</Button>
      <Button type="secondary">加入购物车</Button>
    </template>
  </Card>
</template>

<script setup lang="ts">
import { Card, Button } from '@/components/ui'

const handleCardClick = () => {
  // 卡片点击逻辑
}
</script>
```

#### Props

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| title | string | - | 卡片标题 |
| subtitle | string | - | 副标题 |
| size | string | 'base' | 尺寸：sm/base/lg/xl |
| shadow | string | 'md' | 阴影：none/sm/md/lg |
| hoverable | boolean | false | 是否可悬停（悬停时上浮） |
| clickable | boolean | false | 是否可点击 |
| bordered | boolean | false | 是否显示边框 |
| loading | boolean | false | 是否加载中 |

#### Events

| 事件 | 参数 | 说明 |
|------|------|------|
| click | - | 点击卡片时触发（需 clickable=true） |

#### Slots

| 插槽 | 说明 |
|------|------|
| default | 卡片主体内容 |
| header | 自定义头部 |
| extra | 头部额外内容 |
| footer | 卡片底部内容 |

---

### Button 按钮

统一的按钮组件，支持多种类型和尺寸。

#### 使用示例

```vue
<template>
  <!-- 主要按钮 -->
  <Button type="primary" size="lg" @click="handleClick">
    立即购买
  </Button>
  
  <!-- 次要按钮 -->
  <Button type="secondary" icon="🛒">
    加入购物车
  </Button>
  
  <!-- 加载状态 -->
  <Button type="primary" :loading="loading">
    提交中...
  </Button>
  
  <!-- 块级按钮 -->
  <Button type="primary" block>
    确认订单
  </Button>
  
  <!-- 圆形按钮 -->
  <Button type="primary" circle icon="+" />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Button } from '@/components/ui'

const loading = ref(false)

const handleClick = () => {
  // 点击逻辑
}
</script>
```

#### Props

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| type | string | 'primary' | 类型：primary/secondary/success/warning/danger/info/text |
| size | string | 'base' | 尺寸：sm/base/lg/xl |
| disabled | boolean | false | 是否禁用 |
| loading | boolean | false | 是否加载中 |
| block | boolean | false | 是否为块级按钮 |
| icon | string | - | 按钮图标 |
| round | boolean | false | 是否为圆角按钮 |
| circle | boolean | false | 是否为圆形按钮 |

#### Events

| 事件 | 参数 | 说明 |
|------|------|------|
| click | event: MouseEvent | 点击按钮时触发 |

---

### Input 输入框

统一的输入框组件。

#### 使用示例

```vue
<template>
  <Input
    v-model="username"
    label="用户名"
    placeholder="请输入用户名"
    prefix-icon="👤"
    :clearable="true"
    :required="true"
    :error="errors.username"
    @focus="handleFocus"
    @blur="handleBlur"
  />
  
  <Input
    v-model="password"
    type="password"
    label="密码"
    placeholder="请输入密码"
    prefix-icon="🔒"
    :show-password="true"
    :required="true"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Input } from '@/components/ui'

const username = ref('')
const password = ref('')
const errors = ref({
  username: ''
})

const handleFocus = () => {
  // 聚焦逻辑
}

const handleBlur = () => {
  // 失焦逻辑
}
</script>
```

#### Props

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| modelValue | string/number | '' | 绑定值 |
| type | string | 'text' | 类型：text/password/email/number/tel/url |
| size | string | 'base' | 尺寸：sm/base/lg |
| placeholder | string | '' | 占位符 |
| disabled | boolean | false | 是否禁用 |
| readonly | boolean | false | 是否只读 |
| clearable | boolean | false | 是否可清空 |
| showPassword | boolean | false | 是否显示密码切换 |
| error | string | - | 错误信息 |
| label | string | - | 标签文本 |
| required | boolean | false | 是否必填 |
| prefixIcon | string | - | 前缀图标 |
| suffixIcon | string | - | 后缀图标 |

#### Events

| 事件 | 参数 | 说明 |
|------|------|------|
| update:modelValue | value: string/number | 值变化时触发 |
| focus | event: FocusEvent | 聚焦时触发 |
| blur | event: FocusEvent | 失焦时触发 |
| clear | - | 清空时触发 |

---

### Select 选择器

统一的下拉选择器组件。

#### 使用示例

```vue
<template>
  <Select
    v-model="selectedCategory"
    label="商品分类"
    :options="categories"
    placeholder="请选择分类"
    :clearable="true"
    :required="true"
    @change="handleCategoryChange"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Select } from '@/components/ui'

const selectedCategory = ref('')
const categories = [
  { label: '电子产品', value: 'electronics' },
  { label: '服装', value: 'clothing' },
  { label: '食品', value: 'food', disabled: true },
  { label: '图书', value: 'books' }
]

const handleCategoryChange = (value: string | number) => {
  console.log('选中的分类:', value)
}
</script>
```

#### Props

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| modelValue | string/number | '' | 绑定值 |
| options | Option[] | [] | 选项列表 |
| size | string | 'base' | 尺寸：sm/base/lg |
| placeholder | string | '请选择' | 占位符 |
| disabled | boolean | false | 是否禁用 |
| clearable | boolean | false | 是否可清空 |
| error | string | - | 错误信息 |
| label | string | - | 标签文本 |
| required | boolean | false | 是否必填 |

#### Events

| 事件 | 参数 | 说明 |
|------|------|------|
| update:modelValue | value: string/number | 值变化时触发 |
| change | value: string/number | 选择变化时触发 |

---

## 最佳实践

### 1. 使用 CSS 变量

始终使用 CSS 变量而不是硬编码的值：

```css
/* ❌ 不推荐 */
.button {
  background: #1677ff;
  padding: 16px;
  border-radius: 12px;
}

/* ✅ 推荐 */
.button {
  background: var(--color-primary);
  padding: var(--spacing-4);
  border-radius: var(--radius-md);
}
```

### 2. 组件复用

优先使用统一的组件库，而不是重复编写样式：

```vue
<!-- ❌ 不推荐 -->
<div class="custom-card">
  <h3>标题</h3>
  <p>内容</p>
</div>

<!-- ✅ 推荐 -->
<Card title="标题">
  <p>内容</p>
</Card>
```

### 3. 响应式设计

使用断点变量实现响应式布局：

```css
@media (max-width: var(--breakpoint-md)) {
  .container {
    padding: var(--spacing-4);
  }
}
```

### 4. 动画性能

使用 `transform` 和 `opacity` 实现动画，避免触发重排：

```css
/* ❌ 不推荐 */
.element {
  transition: left 0.3s ease;
  left: 0;
}

.element:hover {
  left: 10px;
}

/* ✅ 推荐 */
.element {
  transition: transform 0.3s ease;
  transform: translateX(0);
}

.element:hover {
  transform: translateX(10px);
}
```

### 5. 可访问性

- 确保文本对比度至少为 4.5:1
- 为交互元素添加焦点样式
- 使用语义化 HTML 标签
- 为图标添加 aria-label

### 6. 命名规范

#### CSS 类名
- 使用小写字母和连字符
- 遵循 BEM 命名规范
- 语义化命名

```css
/* ✅ 推荐 */
.card-header {}
.card-body {}
.card-footer {}
```

#### CSS 变量
- 使用语义化命名
- 分组命名（color-*, spacing-*, font-*）

```css
--color-primary
--spacing-4
--font-size-base
```

---

## 更新日志

### v1.0.0 (2026-03-31)
- ✅ 完成设计系统 CSS 变量定义
- ✅ 创建 PageHeader 组件
- ✅ 创建 Card 组件
- ✅ 创建 Button 组件
- ✅ 创建 Input 组件
- ✅ 创建 Select 组件
- ✅ 完成设计规范文档

---

## 联系方式

如有问题或建议，请联系开发团队。
