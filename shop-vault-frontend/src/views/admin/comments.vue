<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCommentList, deleteComment } from '@/api/product'
import type { Comment, PageResult } from '@/types/api'

const loading = ref(false)
const comments = ref<Comment[]>([])
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const fetchComments = async () => {
  loading.value = true
  try {
    const res: PageResult<Comment> = await getCommentList(0, {
      pageNum: pagination.current,
      pageSize: pagination.size
    })
    comments.value = res.records
    pagination.total = res.total
  } catch (error) {
    console.error('获取评价失败', error)
  } finally {
    loading.value = false
  }
}

const handleDelete = async (commentId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该评价吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteComment(commentId)
    ElMessage.success('删除成功')
    fetchComments()
  } catch (error) {
    // 用户取消
  }
}

const handlePageChange = (page: number) => {
  pagination.current = page
  fetchComments()
}

onMounted(() => {
  fetchComments()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">评价管理</h2>
        <p class="page-subtitle">管理用户商品评价</p>
      </div>
    </div>

    <div class="content-card">
      <el-table :data="comments" v-loading="loading" stripe>
        <el-table-column label="用户" width="150">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :src="row.userAvatar" :size="36">
                {{ row.userName?.charAt(0) }}
              </el-avatar>
              <span class="user-name">{{ row.userName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="评价内容" min-width="300">
          <template #default="{ row }">
            <div class="comment-content">
              <el-rate :model-value="row.rating" disabled size="small" class="mb-1" />
              <p class="comment-text">{{ row.content }}</p>
              <div v-if="row.images?.length" class="comment-images">
                <el-image 
                  v-for="(img, index) in row.images.slice(0, 3)" 
                  :key="index"
                  :src="img"
                  :preview-src-list="row.images"
                  fit="cover"
                  class="comment-image"
                />
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="点赞数" width="80" align="center">
          <template #default="{ row }">
            <span class="likes-count">{{ row.likes }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="评价时间" width="180" />
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button type="danger" link size="small" @click="handleDelete(row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          :current-page="pagination.current"
          :page-size="pagination.size"
          :total="pagination.total"
          layout="total, prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-container {
  animation: fadeIn 0.4s ease-out;
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
}

.header-left {
  flex: 1;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #1f2329;
  margin: 0 0 4px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #86909c;
  margin: 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #1f2329;
}

.comment-content {
  text-align: left;
}

.comment-text {
  font-size: 14px;
  color: #4e5969;
  margin: 0 0 8px 0;
  line-height: 1.6;
}

.comment-images {
  display: flex;
  gap: 8px;
}

.comment-image {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  cursor: pointer;
}

.likes-count {
  font-size: 14px;
  color: #86909c;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 16px 0 0 0;
}

:deep(.el-table) {
  border-radius: 12px;
  overflow: hidden;
}

:deep(.el-table th) {
  background: #fafafa;
  color: #1f2329;
  font-weight: 600;
  font-size: 14px;
}

:deep(.el-table td) {
  padding: 16px 0;
}
</style>
