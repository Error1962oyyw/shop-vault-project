import request from '@/utils/request';
import type { Product, ProductListParams, PageResult, Category, Comment, Favorite, YoloSearchResult, AddCommentParams, ProductSku } from '@/types/api';

export const getProductList = (params: ProductListParams) => {
  return request<PageResult<Product>>({
    url: '/api/product/list',
    method: 'get',
    params
  });
};

export const getProductDetail = (id: number) => {
  return request<Product>({
    url: `/api/product/detail/${id}`,
    method: 'get'
  });
};

export const getProductSkus = (productId: number) => {
  return request<ProductSku[]>({
    url: `/api/product/skus/${productId}`,
    method: 'get'
  });
};

export const yoloSearch = (file: File) => {
  const formData = new FormData();
  formData.append('file', file);
  return request<YoloSearchResult>({
    url: '/api/product/yolo-search',
    method: 'post',
    data: formData,
    timeout: 30000
  });
};

export const getCategoryList = () => {
  return request<Category[]>({
    url: '/api/category/list',
    method: 'get'
  });
};

export const getCategoryListWithYolo = () => {
  return request<Category[]>({
    url: '/api/category/list-with-yolo',
    method: 'get'
  });
};

export const getCategoryById = (id: number) => {
  return request<Category>({
    url: `/api/category/detail/${id}`,
    method: 'get'
  });
};

export const getCategoryByYoloLabel = (yoloLabel: string) => {
  return request<Category>({
    url: `/api/category/by-yolo-label/${yoloLabel}`,
    method: 'get'
  });
};

export const getCommentList = (productId: number, params: { pageNum: number; pageSize: number }) => {
  return request<PageResult<Comment>>({
    url: `/api/comment/list/${productId}`,
    method: 'get',
    params
  });
};

export const addComment = (data: AddCommentParams) => {
  return request<void>({
    url: '/api/comment/add',
    method: 'post',
    data
  });
};

export const likeComment = (commentId: number) => {
  return request<void>({
    url: `/api/comment/like/${commentId}`,
    method: 'post'
  });
};

export const reportComment = (commentId: number) => {
  return request<void>({
    url: `/api/comment/report/${commentId}`,
    method: 'post'
  });
};

export const toggleFavorite = (productId: number) => {
  return request<boolean>({
    url: `/api/favorite/toggle/${productId}`,
    method: 'post'
  });
};

export const getFavoriteList = () => {
  return request<Favorite[]>({
    url: '/api/favorite/my-list',
    method: 'get'
  });
};

export const getRecommendations = () => {
  return request<Product[]>({
    url: '/api/recommendation/guess-you-like',
    method: 'get'
  });
};

export const uploadImage = (file: File) => {
  const formData = new FormData();
  formData.append('file', file);
  return request<string>({
    url: '/api/upload/image',
    method: 'post',
    data: formData
  });
};

export const publishProduct = (data: {
  name: string;
  categoryId: number;
  price: number;
  originalPrice?: number;
  stock: number;
  subTitle?: string;
  mainImage?: string;
  detailImages?: string;
  freight?: number;
}) => {
  return request<void>({
    url: '/api/product/publish',
    method: 'post',
    data
  });
};

export const updateProduct = (id: number, data: Record<string, unknown>) => {
  return request<void>({
    url: `/api/product/update/${id}`,
    method: 'put',
    data
  });
};

export const deleteComment = (commentId: number) => {
  return request<void>({
    url: `/api/comment/admin/delete/${commentId}`,
    method: 'delete'
  });
};

export const getAdminCommentList = (params: { pageNum: number; pageSize: number }) => {
  return request<PageResult<Comment>>({
    url: '/api/comment/admin/list',
    method: 'get',
    params
  });
};
