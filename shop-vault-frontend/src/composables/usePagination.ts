import { reactive, computed, type ComputedRef } from 'vue'

interface PaginationState {
  current: number
  size: number
  total: number
}

interface UsePaginationOptions {
  defaultPageSize?: number
  onPageChange?: (page: number) => void
  onSizeChange?: (size: number) => void
}

interface UsePaginationReturn {
  pagination: PaginationState
  currentPage: ComputedRef<number>
  pageSize: ComputedRef<number>
  total: ComputedRef<number>
  totalPages: ComputedRef<number>
  handleCurrentChange: (page: number) => void
  handleSizeChange: (size: number) => void
  resetPagination: () => void
  setTotal: (total: number) => void
  getParams: () => { pageNum: number; pageSize: number }
}

export function usePagination(options: UsePaginationOptions = {}): UsePaginationReturn {
  const { defaultPageSize = 10, onPageChange, onSizeChange } = options

  const pagination = reactive<PaginationState>({
    current: 1,
    size: defaultPageSize,
    total: 0
  })

  const totalPages = computed(() => {
    return Math.ceil(pagination.total / pagination.size) || 1
  })

  const handleCurrentChange = (page: number) => {
    pagination.current = page
    onPageChange?.(page)
  }

  const handleSizeChange = (size: number) => {
    pagination.size = size
    pagination.current = 1
    onSizeChange?.(size)
  }

  const resetPagination = () => {
    pagination.current = 1
    pagination.total = 0
  }

  const setTotal = (total: number) => {
    pagination.total = total
  }

  const getParams = () => ({
    pageNum: pagination.current,
    pageSize: pagination.size
  })

  return {
    pagination,
    currentPage: computed(() => pagination.current),
    pageSize: computed(() => pagination.size),
    total: computed(() => pagination.total),
    totalPages,
    handleCurrentChange,
    handleSizeChange,
    resetPagination,
    setTotal,
    getParams
  }
}
