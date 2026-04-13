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
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:9090'
  return baseUrl + url
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

export function formatTime(date: string | Date | null | undefined): string {
  if (!date) return ''
  const d = typeof date === 'string' ? new Date(date) : date
  if (isNaN(d.getTime())) return ''
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}

export function formatCouponValue(coupon: { type: number; discountRate?: number; discountAmount?: number }): string {
  if (coupon.type === 1 && coupon.discountRate) {
    return `${Math.round((1 - coupon.discountRate) * 100)}%OFF`
  } else if (coupon.type === 2 && coupon.discountAmount) {
    return `¥${coupon.discountAmount}`
  }
  return '优惠券'
}

export function getOrderDisplayName(order: { productName?: string; orderType?: number }): string {
  if (order.productName) return order.productName
  switch (order.orderType) {
    case 1: return 'VIP购买'
    case 2: return 'SVIP购买'
    case 3: return '积分兑换'
    default: return '普通订单'
  }
}

export function getOrderTypeLabel(orderType?: number): string {
  switch (orderType) {
    case 1: return 'VIP'
    case 2: return 'SVIP'
    case 3: return '积分兑换'
    default: return '普通'
  }
}
