import request from '@/utils/request';
import type { Spec, SpecValue, ProductSpec, ProductSpecValue } from '@/types/api';

export const getSpecList = () => {
  return request<Spec[]>({
    url: '/api/admin/specs',
    method: 'get'
  });
};

export const getSpecDetail = (id: number) => {
  return request<Spec>({
    url: `/api/admin/specs/${id}`,
    method: 'get'
  });
};

export const createSpec = (data: { name: string; sortOrder?: number; values?: string[] }) => {
  return request<Spec>({
    url: '/api/admin/specs',
    method: 'post',
    data
  });
};

export const updateSpec = (id: number, data: { name: string; sortOrder?: number; values?: string[] }) => {
  return request<Spec>({
    url: `/api/admin/specs/${id}`,
    method: 'put',
    data
  });
};

export const deleteSpec = (id: number) => {
  return request<void>({
    url: `/api/admin/specs/${id}`,
    method: 'delete'
  });
};

export const getSpecValues = (specId: number) => {
  return request<SpecValue[]>({
    url: `/api/admin/specs/${specId}/values`,
    method: 'get'
  });
};

export const addSpecValue = (specId: number, data: { value: string; sortOrder?: number }) => {
  return request<SpecValue>({
    url: `/api/admin/specs/${specId}/values`,
    method: 'post',
    data
  });
};

export const deleteSpecValue = (valueId: number) => {
  return request<void>({
    url: `/api/admin/specs/values/${valueId}`,
    method: 'delete'
  });
};

export const getProductSpecs = (productId: number) => {
  return request<ProductSpec[]>({
    url: `/api/admin/product-specs/product/${productId}`,
    method: 'get'
  });
};

export const addProductSpec = (productId: number, data: {
  specId?: number;
  specName: string;
  sortOrder?: number;
  isCustom?: number;
  values?: ProductSpecValue[];
}) => {
  return request<ProductSpec>({
    url: `/api/admin/product-specs/product/${productId}`,
    method: 'post',
    data
  });
};

export const updateProductSpec = (id: number, data: {
  specName?: string;
  sortOrder?: number;
  values?: ProductSpecValue[];
}) => {
  return request<ProductSpec>({
    url: `/api/admin/product-specs/${id}`,
    method: 'put',
    data
  });
};

export const deleteProductSpec = (id: number) => {
  return request<void>({
    url: `/api/admin/product-specs/${id}`,
    method: 'delete'
  });
};

export const addProductSpecValue = (productSpecId: number, data: {
  specValueId?: number;
  value: string;
  sortOrder?: number;
  isCustom?: number;
}) => {
  return request<ProductSpecValue>({
    url: `/api/admin/product-specs/${productSpecId}/values`,
    method: 'post',
    data
  });
};

export const updateProductSpecValue = (valueId: number, data: {
  value?: string;
  sortOrder?: number;
}) => {
  return request<ProductSpecValue>({
    url: `/api/admin/product-specs/values/${valueId}`,
    method: 'put',
    data
  });
};

export const deleteProductSpecValue = (valueId: number) => {
  return request<void>({
    url: `/api/admin/product-specs/values/${valueId}`,
    method: 'delete'
  });
};

export const getSpecTemplates = () => {
  return request<Spec[]>({
    url: '/api/admin/product-specs/templates',
    method: 'get'
  });
};

export const getSpecTemplateValues = (specId: number) => {
  return request<SpecValue[]>({
    url: `/api/admin/product-specs/templates/${specId}/values`,
    method: 'get'
  });
};
