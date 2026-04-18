export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:9090'

export const AUTH_API = {
  LOGIN: '/api/auth/login',
  ADMIN_LOGIN: '/api/auth/admin/login',
  REFRESH: '/api/auth/refresh',
  LOGOUT: '/api/auth/logout',
  SEND_CODE: '/api/auth/send-code',
  CHECK_EMAIL: '/api/auth/check-email',
  REGISTER: '/api/auth/register',
  RESET_PASSWORD: '/api/auth/reset-password',
} as const

export const USER_API = {
  PROFILE: '/api/user/profile',
  PASSWORD: '/api/user/password',
} as const

export const ADDRESS_API = {
  LIST: '/api/address/list',
  ADD: '/api/address/add',
  UPDATE: '/api/address/update',
  DELETE: (id: number) => `/api/address/delete/${id}`,
  SET_DEFAULT: (id: number) => `/api/address/default/${id}`,
} as const

export const PRODUCT_API = {
  LIST: '/api/product/list',
  DETAIL: (id: number) => `/api/product/detail/${id}`,
  PUBLISH: '/api/product/publish',
  YOLO_SEARCH: '/api/product/yolo-search',
  HOT_CATEGORIES: '/api/product/hot-categories',
} as const

export const CATEGORY_API = {
  LIST: '/api/category/list',
} as const

export const CART_API = {
  LIST: '/api/cart/list',
  ADD: '/api/cart/add',
  UPDATE_QUANTITY: (id: number) => `/api/cart/update-quantity/${id}`,
  DELETE: '/api/cart/delete',
} as const

export const ORDER_API = {
  BUY_NOW: '/api/order/buy-now',
  CART_CHECKOUT: '/api/order/cart-checkout',
  PAY: (orderNo: string) => `/api/order/pay/${orderNo}`,
  SHIP: (orderId: number) => `/api/admin/orders/${orderId}/ship`,
  RECEIVE: (orderNo: string) => `/api/order/receive/${orderNo}`,
  EXTEND: (orderNo: string) => `/api/order/extend/${orderNo}`,
  CANCEL: (orderNo: string) => `/api/order/cancel/${orderNo}`,
  LIST: '/api/orders',
  DETAIL: (orderId: number) => `/api/orders/${orderId}`,
  ADMIN_LIST: '/api/admin/orders',
} as const

export const AFTER_SALES_API = {
  APPLY: '/api/after-sales/apply',
  MY_LIST: '/api/after-sales/my-list',
  CANCEL: '/api/after-sales/cancel',
  RETURN_LOGISTICS: '/api/after-sales/return-logistics',
  RESOLVE: (id: number) => `/api/admin/after-sales/${id}/resolve`,
  ADMIN_LIST: '/api/admin/after-sales',
  DETAIL: (orderNo: string) => `/api/after-sales/detail/${orderNo}`,
} as const

export const COUPON_API = {
  AVAILABLE: '/api/coupons/available',
  DETAIL: (id: number) => `/api/coupons/${id}`,
  RECEIVE: (id: number) => `/api/coupons/${id}/receive`,
  MY: '/api/coupons/my',
  MY_AVAILABLE: '/api/coupons/my/available',
  APPLICABLE: '/api/coupons/my/applicable',
  BEST: '/api/coupons/my/best',
  USE: (id: number) => `/api/coupons/${id}/use`,
} as const

export const MARKETING_API = {
  SIGN_IN: '/api/marketing/sign-in',
  POINTS_RECORDS: '/api/marketing/points/records',
} as const

export const MEMBER_DAY_API = {
  CURRENT: '/api/member-day/current',
  UPCOMING: '/api/member-day/upcoming',
  INFO: (id: number) => `/api/member-day/info/${id}`,
  CHECK: '/api/member-day/check',
} as const

export const ACTIVITY_API = {
  EXCHANGE: (activityId: number) => `/api/activity/exchange/${activityId}`,
  COUPONS_AVAILABLE: '/api/activity/coupons/available',
  COUPONS_CLAIM: (activityId: number) => `/api/activity/coupons/claim/${activityId}`,
} as const

export const FAVORITE_API = {
  TOGGLE: (productId: number) => `/api/favorite/toggle/${productId}`,
  MY_LIST: '/api/favorite/my-list',
} as const

export const COMMENT_API = {
  ADD: '/api/comment/add',
  LIST: (productId: number) => `/api/comment/list/${productId}`,
  LIKE: (commentId: number) => `/api/comment/like/${commentId}`,
  REPORT: (commentId: number) => `/api/comment/report/${commentId}`,
  ADMIN_DELETE: (commentId: number) => `/api/comment/admin/delete/${commentId}`,
} as const

export const RECOMMENDATION_API = {
  GUESS_YOU_LIKE: '/api/recommendation/guess-you-like',
  WITH_EXPLANATION: '/api/recommendation/with-explanation',
  CHECK_PREFERENCE: '/api/recommendation/check-preference',
  CATEGORIES: '/api/recommendation/categories',
  SET_PREFERENCE: '/api/recommendation/set-preference',
  NEW_USER_GUIDE: '/api/recommendation/new-user-guide',
} as const

export const CHAT_API = {
  SEND: '/api/chat/send',
  HISTORY: '/api/chat/history',
  ADMIN_REPLY: '/api/chat/admin/reply',
  ADMIN_HISTORY: (userId: number) => `/api/chat/admin/history/${userId}`,
} as const

export const MESSAGE_API = {
  MY: '/api/messages/my',
  READ: (id: number) => `/api/messages/read/${id}`,
  PUSH_USER: (userId: number) => `/api/messages/push/user/${userId}`,
  PUSH_ROLE: (role: string) => `/api/messages/push/role/${role}`,
  PUSH_ALL: '/api/messages/push/all',
  RETRY_FAILED: '/api/messages/retry-failed',
} as const

export const WEBSOCKET_API = {
  ONLINE_COUNT: '/api/websocket/online-count',
} as const

export const ONBOARDING_API = {
  STATUS: '/api/onboarding/status',
  CATEGORIES: '/api/onboarding/categories',
  COMPLETE: '/api/onboarding/complete',
  SKIP: '/api/onboarding/skip',
} as const

export const DASHBOARD_API = {
  STATS: '/api/dashboard/stats',
  REALTIME: '/api/dashboard/realtime',
} as const

export const UPLOAD_API = {
  IMAGE: '/api/upload/image',
} as const

export const ADMIN_API = {
  COUPONS: '/api/admin/coupons',
  COUPON_DETAIL: (id: number) => `/api/admin/coupons/${id}`,
  COUPON_STATUS: (id: number) => `/api/admin/coupons/${id}/status`,
  COUPONS_EXPIRE: '/api/admin/coupons/expire',
  
  SKUS: '/api/admin/skus',
  SKU_DETAIL: (id: number) => `/api/admin/skus/${id}`,
  SKU_BY_CODE: (skuCode: string) => `/api/admin/skus/code/${skuCode}`,
  SKU_BY_PRODUCT: (productId: number) => `/api/admin/skus/product/${productId}`,
  SKU_BATCH: '/api/admin/skus/batch',
  SKU_STOCK_ADD: (id: number) => `/api/admin/skus/${id}/stock/add`,
  SKU_STOCK_DEDUCT: (id: number) => `/api/admin/skus/${id}/stock/deduct`,
  
  SPECS: '/api/admin/specs',
  SPEC_DETAIL: (id: number) => `/api/admin/specs/${id}`,
  SPEC_VALUES: (specId: number) => `/api/admin/specs/${specId}/values`,
  SPEC_VALUE_DELETE: (valueId: number) => `/api/admin/specs/values/${valueId}`,
  
  YOLO_MAPPING_LIST: '/api/yolo-mapping/list',
  YOLO_MAPPING_ACTIVE: '/api/yolo-mapping/active',
  YOLO_MAPPING_LABELS: '/api/yolo-mapping/available-labels',
  YOLO_MAPPING_ADD: '/api/yolo-mapping/add',
  YOLO_MAPPING_UPDATE: '/api/yolo-mapping/update',
  YOLO_MAPPING_DELETE: (id: number) => `/api/yolo-mapping/${id}`,
  YOLO_MAPPING_TOGGLE: (id: number) => `/api/yolo-mapping/toggle/${id}`,
  YOLO_MAPPING_CATEGORIES: '/api/yolo-mapping/categories',
} as const
