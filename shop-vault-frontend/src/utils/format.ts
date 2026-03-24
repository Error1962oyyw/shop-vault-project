export function formatDate(date: string | Date | null | undefined): string {
  if (!date) return ''
  const d = typeof date === 'string' ? new Date(date) : date
  if (isNaN(d.getTime())) return ''
  
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  
  return `${year}-${month}-${day}`
}

export function formatDateTime(date: string | Date | null | undefined): string {
  if (!date) return ''
  const d = typeof date === 'string' ? new Date(date) : date
  if (isNaN(d.getTime())) return ''
  
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

export function formatPrice(price: number | string | null | undefined): string {
  if (price === null || price === undefined) return '0.00'
  const num = typeof price === 'string' ? parseFloat(price) : price
  if (isNaN(num)) return '0.00'
  return num.toFixed(2)
}

export function formatMoney(price: number | string | null | undefined): string {
  return `¥${formatPrice(price)}`
}

export function getAvatarUrl(url: string | null | undefined): string {
  if (!url) return ''
  if (url.startsWith('http')) return url
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
  return baseUrl + url
}

export function getImageUrl(path: string | null | undefined): string {
  if (!path) return ''
  if (path.startsWith('http')) return path
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
  return baseUrl + path
}

export function truncateText(text: string | null | undefined, maxLength: number = 50): string {
  if (!text) return ''
  if (text.length <= maxLength) return text
  return text.slice(0, maxLength) + '...'
}

export function getStatusText(status: number, statusMap: Record<number, string>): string {
  return statusMap[status] || '未知'
}

export function getStatusType(
  status: number, 
  typeMap: Record<number, 'success' | 'warning' | 'danger' | 'info' | 'primary'>
): 'success' | 'warning' | 'danger' | 'info' | 'primary' {
  return typeMap[status] || 'info'
}
