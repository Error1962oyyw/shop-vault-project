export interface UserInfo {
  id: number;
  username: string;
  nickname: string;
  email: string;
  phone: string;
  avatar: string;
  gender: number;
  birthday: string;
  points: number;
  balance: number;
  creditScore: number;
  memberLevel: number;
  createTime: string;
}

export interface Category {
  id: number;
  name: string;
  parentId: number;
  level: number;
  icon: string;
  sort: number;
  children?: Category[];
}

export interface Product {
  id: number;
  name: string;
  categoryId: number;
  categoryName: string;
  mainImage: string;
  images: string[];
  price: number;
  originalPrice: number;
  stock: number;
  sales: number;
  description: string;
  detail: string;
  status: number;
  isFavorite: boolean;
  createTime: string;
}

export interface ProductListParams {
  categoryId?: number;
  keyword?: string;
  minPrice?: number;
  maxPrice?: number;
  sortBy?: string;
  sortOrder?: string;
  pageNum: number;
  pageSize: number;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  pages: number;
  current: number;
  size: number;
}

export interface CartItem {
  id: number;
  productId: number;
  productName: string;
  productImage: string;
  price: number;
  quantity: number;
  stock: number;
  selected: boolean;
}

export interface Address {
  id: number;
  receiverName: string;
  receiverPhone: string;
  province: string;
  city: string;
  region: string;
  detailAddress: string;
  isDefault: boolean;
}

export interface Order {
  orderNo: string;
  status: number;
  statusText: string;
  totalAmount: number;
  payAmount: number;
  freightAmount: number;
  discountAmount: number;
  receiverName: string;
  receiverPhone: string;
  receiverAddress: string;
  paymentTime: string;
  deliveryTime: string;
  receiveTime: string;
  createTime: string;
  items: OrderItem[];
}

export interface OrderItem {
  id: number;
  productId: number;
  productName: string;
  productImage: string;
  price: number;
  quantity: number;
  totalAmount: number;
}

export interface CreateOrderParams {
  addressId: number;
  cartItemIds?: number[];
  productId?: number;
  quantity?: number;
  remark?: string;
  couponId?: number;
}

export interface Comment {
  id: number;
  userId: number;
  userName: string;
  userAvatar: string;
  productId: number;
  content: string;
  rating: number;
  images: string[];
  likes: number;
  isLiked: boolean;
  createTime: string;
}

export interface AddCommentParams {
  productId: number;
  orderNo: string;
  content: string;
  rating: number;
  images: string[];
}

export interface Favorite {
  id: number;
  productId: number;
  productName: string;
  productImage: string;
  price: number;
  createTime: string;
}

export interface AfterSales {
  id: number;
  orderNo: string;
  type: number;
  reason: string;
  description: string;
  images: string[];
  status: number;
  statusText: string;
  refundAmount: number;
  createTime: string;
  handleTime: string;
  handleRemark: string;
}

export interface ApplyAfterSalesParams {
  orderNo: string;
  type: number;
  reason: string;
  description: string;
  images: string[];
}

export interface Coupon {
  id: number;
  name: string;
  type: number;
  value: number;
  minAmount: number;
  startTime: string;
  endTime: string;
  status: number;
}

export interface PointsRecord {
  id: number;
  type: number;
  points: number;
  description: string;
  createTime: string;
}

export interface ChatMessage {
  id: number;
  senderId: number;
  receiverId: number;
  content: string;
  type: number;
  msgType?: number;
  isRead: boolean;
  createTime: string;
  senderRole?: string;
  senderAvatar?: string;
  receiverAvatar?: string;
}

export interface DashboardStats {
  totalUsers: number;
  totalOrders: number;
  totalSales: number;
  totalProducts: number;
  todayOrders: number;
  todaySales: number;
  pendingOrders: number;
  pendingAfterSales: number;
  salesTrend: { date: string; amount: number }[];
  categorySales: { name: string; value: number }[];
}

export interface YoloSearchResult {
  success: boolean;
  message: string;
  detectedLabels: string[];
  matchedCategories: YoloCategoryInfo[];
  hotCategories: YoloCategoryInfo[];
}

export interface YoloCategoryInfo {
  id: number;
  name: string;
  icon: string;
  productCount: number;
}

export interface LoginParams {
  email: string;
  password: string;
}

export interface AdminLoginParams {
  email: string;
  password: string;
}

export interface RegisterParams {
  username: string;
  password: string;
  email: string;
  code: string;
}

export interface SendCodeParams {
  email: string;
  type: 'register' | 'reset';
}

export interface CouponTemplate {
  id: number;
  name: string;
  type: number;
  value: number;
  minAmount: number;
  maxAmount?: number;
  discount?: number;
  totalCount: number;
  usedCount: number;
  perUserLimit: number;
  startTime: string;
  endTime: string;
  status: number;
  categoryId?: number;
  productId?: number;
  description?: string;
  createTime: string;
}

export interface UserCoupon {
  id: number;
  couponId: number;
  couponName: string;
  type: number;
  value: number;
  minAmount: number;
  maxAmount?: number;
  discount?: number;
  startTime: string;
  endTime: string;
  status: number;
  usedTime?: string;
  orderId?: number;
  categoryId?: number;
  productId?: number;
}

export interface MemberDay {
  id: number;
  name: string;
  description: string;
  startTime: string;
  endTime: string;
  discountRate: number;
  pointsMultiplier: number;
  status: number;
  benefits: MemberDayBenefit[];
}

export interface MemberDayBenefit {
  id: number;
  type: string;
  title: string;
  description: string;
  value: number;
  icon: string;
}

export interface MessagePush {
  id: number;
  type: number;
  title: string;
  content: string;
  isRead: boolean;
  createTime: string;
  extra?: Record<string, unknown>;
}

export interface OnboardingStatus {
  completed: boolean;
  skipped: boolean;
  selectedCategories: number[];
  currentStep: number;
}

export interface ProductSku {
  id: number;
  productId: number;
  skuCode: string;
  skuName: string;
  specJson: string;
  price: number;
  originalPrice: number;
  stock: number;
  sales: number;
  image: string;
  status: number;
}

export interface Spec {
  id: number;
  name: string;
  description?: string;
  sort: number;
  values: SpecValue[];
}

export interface SpecValue {
  id: number;
  specId: number;
  value: string;
  sort: number;
}

export interface Activity {
  id: number;
  name: string;
  type: number;
  description: string;
  startTime: string;
  endTime: string;
  status: number;
  imageUrl: string;
  discountRate?: number;
  pointsReward?: number;
}

export interface Recommendation {
  product: Product;
  reason: string;
  score: number;
}

export interface UserPreference {
  categoryIds: number[];
  priceRange: string;
  brands: string[];
}

export interface YoloMapping {
  id: number;
  yoloLabel: string;
  categoryId: number;
  categoryName: string;
  isActive: boolean;
  createTime: string;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

export interface RefreshTokenParams {
  refreshToken: string;
}

export interface CouponTemplateParams {
  name: string;
  type: number;
  value: number;
  minAmount: number;
  maxAmount?: number;
  discount?: number;
  totalCount: number;
  perUserLimit: number;
  startTime: string;
  endTime: string;
  categoryId?: number;
  productId?: number;
  description?: string;
}

export interface SkuCreateParams {
  productId: number;
  skuCode: string;
  skuName: string;
  specJson: string;
  price: number;
  originalPrice: number;
  stock: number;
  image?: string;
}

export interface SpecCreateParams {
  name: string;
  description?: string;
  sort?: number;
  values?: string[];
}

export interface ReturnLogisticsParams {
  orderNo: string;
  logisticsCompany: string;
  logisticsNo: string;
  remark?: string;
}

export interface AfterSalesHandleParams {
  orderNo: string;
  agree: boolean;
  refundAmount?: number;
  remark?: string;
}

export interface MessagePushParams {
  type: number;
  title: string;
  content: string;
}
