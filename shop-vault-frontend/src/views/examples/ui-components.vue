<template>
  <UserLayout>
    <div class="example-page animate-fade-in">
      <div class="page-container">
        <PageHeader
          title="组件示例页面"
          subtitle="展示统一UI组件库的使用方法"
          icon="🎨"
          gradient="primary"
        >
          <template #extra>
            <div class="points-display">
              <span class="points-value">1,234</span>
              <span class="points-label">可用积分</span>
            </div>
          </template>
        </PageHeader>

        <div class="content-section">
          <h2 class="section-title">按钮组件示例</h2>
          <div class="button-group">
            <Button type="primary" size="lg">主要按钮</Button>
            <Button type="secondary" size="base">次要按钮</Button>
            <Button type="success" size="sm">成功按钮</Button>
            <Button type="warning">警告按钮</Button>
            <Button type="danger">危险按钮</Button>
            <Button type="text">文本按钮</Button>
          </div>
          
          <div class="button-group">
            <Button type="primary" :loading="loading" @click="handleLoading">
              加载状态
            </Button>
            <Button type="primary" :disabled="true">禁用状态</Button>
            <Button type="primary" icon="🛒">带图标</Button>
            <Button type="primary" round>圆角按钮</Button>
            <Button type="primary" circle icon="+" />
          </div>
          
          <div class="button-group">
            <Button type="primary" block>块级按钮</Button>
          </div>
        </div>

        <div class="content-section">
          <h2 class="section-title">卡片组件示例</h2>
          <div class="card-grid">
            <Card
              title="基础卡片"
              subtitle="这是一个基础卡片"
              size="base"
              shadow="md"
            >
              <p>这是卡片的内容区域，可以放置任何内容。</p>
              <template #footer>
                <Button type="primary" size="sm">确认</Button>
                <Button type="secondary" size="sm">取消</Button>
              </template>
            </Card>

            <Card
              title="可悬停卡片"
              subtitle="鼠标悬停时会上浮"
              size="lg"
              shadow="md"
              :hoverable="true"
            >
              <p>这个卡片在鼠标悬停时会有上浮效果。</p>
            </Card>

            <Card
              title="可点击卡片"
              subtitle="点击卡片触发事件"
              size="base"
              shadow="md"
              :clickable="true"
              :hoverable="true"
              @click="handleCardClick"
            >
              <p>点击这个卡片会触发点击事件。</p>
            </Card>

            <Card
              title="加载中卡片"
              subtitle="显示加载状态"
              size="base"
              shadow="md"
              :loading="loading"
            >
              <p>这个卡片正在加载中...</p>
            </Card>
          </div>
        </div>

        <div class="content-section">
          <h2 class="section-title">表单组件示例</h2>
          <Card title="登录表单" size="lg">
            <form @submit.prevent="handleSubmit">
              <Input
                v-model="formData.username"
                label="用户名"
                placeholder="请输入用户名"
                prefix-icon="👤"
                :clearable="true"
                :required="true"
                :error="errors.username"
              />
              
              <Input
                v-model="formData.password"
                type="password"
                label="密码"
                placeholder="请输入密码"
                prefix-icon="🔒"
                :required="true"
                :error="errors.password"
              />
              
              <Input
                v-model="formData.email"
                type="email"
                label="邮箱"
                placeholder="请输入邮箱"
                prefix-icon="📧"
                :clearable="true"
              />
              
              <Select
                v-model="formData.category"
                label="用户类型"
                :options="categoryOptions"
                placeholder="请选择用户类型"
                :clearable="true"
                :required="true"
              />
              
              <div class="form-actions">
                <Button type="primary" size="lg" :loading="submitting">
                  提交
                </Button>
                <Button type="secondary" size="lg" @click="handleReset">
                  重置
                </Button>
              </div>
            </form>
          </Card>
        </div>

        <div class="content-section">
          <h2 class="section-title">不同渐变色的头部</h2>
          <div class="header-examples">
            <PageHeader
              title="橙色主题"
              subtitle="积分商城"
              icon="🎁"
              gradient="orange"
            />
            
            <PageHeader
              title="紫色主题"
              subtitle="会员中心"
              icon="⭐"
              gradient="purple"
            />
            
            <PageHeader
              title="成功主题"
              subtitle="操作成功"
              icon="✅"
              gradient="success"
            />
          </div>
        </div>
      </div>
    </div>
  </UserLayout>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import UserLayout from '@/components/layout/UserLayout.vue'
import { PageHeader, Card, Button, Input, Select } from '@/components/ui'

const loading = ref(false)
const submitting = ref(false)

const formData = reactive({
  username: '',
  password: '',
  email: '',
  category: ''
})

const errors = reactive({
  username: '',
  password: ''
})

const categoryOptions = [
  { label: '普通用户', value: 'normal' },
  { label: 'VIP用户', value: 'vip' },
  { label: '企业用户', value: 'enterprise' }
]

const handleLoading = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 2000)
}

const handleCardClick = () => {
  alert('卡片被点击了！')
}

const handleSubmit = () => {
  errors.username = ''
  errors.password = ''
  
  if (!formData.username) {
    errors.username = '请输入用户名'
    return
  }
  
  if (!formData.password) {
    errors.password = '请输入密码'
    return
  }
  
  submitting.value = true
  setTimeout(() => {
    submitting.value = false
    alert('表单提交成功！')
  }, 1500)
}

const handleReset = () => {
  formData.username = ''
  formData.password = ''
  formData.email = ''
  formData.category = ''
  errors.username = ''
  errors.password = ''
}
</script>

<style scoped>
.example-page {
  min-height: 100vh;
  background: var(--background-color-page);
}

.page-container {
  max-width: var(--container-xl);
  margin: 0 auto;
  padding: 0 var(--spacing-6);
}

.points-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-2);
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  padding: var(--spacing-5) var(--spacing-8);
  border-radius: var(--radius-lg);
  border: 1px solid rgba(255, 255, 255, 0.3);
  min-width: 160px;
}

.points-value {
  font-size: var(--font-size-4xl);
  font-weight: var(--font-weight-extrabold);
  line-height: 1;
}

.points-label {
  font-size: var(--font-size-sm);
  opacity: 0.9;
}

.content-section {
  margin-bottom: var(--spacing-10);
}

.section-title {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--text-color-primary);
  margin-bottom: var(--spacing-6);
  padding-bottom: var(--spacing-3);
  border-bottom: 2px solid var(--color-primary);
}

.button-group {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-3);
  margin-bottom: var(--spacing-4);
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--spacing-6);
}

.form-actions {
  display: flex;
  gap: var(--spacing-3);
  margin-top: var(--spacing-6);
}

.header-examples {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-6);
}

@media (max-width: 768px) {
  .page-container {
    padding: 0 var(--spacing-4);
  }
  
  .card-grid {
    grid-template-columns: 1fr;
  }
}
</style>
