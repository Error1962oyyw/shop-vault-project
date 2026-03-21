import request from '@/utils/request';
import type { OnboardingStatus, Category, UserPreference } from '@/types/api';

export const getOnboardingStatus = () => {
  return request<OnboardingStatus>({
    url: '/api/onboarding/status',
    method: 'get'
  });
};

export const getOnboardingCategories = () => {
  return request<Category[]>({
    url: '/api/onboarding/categories',
    method: 'get'
  });
};

export const completeOnboarding = (data: { categoryIds: number[] }) => {
  return request<void>({
    url: '/api/onboarding/complete',
    method: 'post',
    data
  });
};

export const skipOnboarding = () => {
  return request<void>({
    url: '/api/onboarding/skip',
    method: 'post'
  });
};

export const checkPreference = () => {
  return request<UserPreference>({
    url: '/api/recommendation/check-preference',
    method: 'get'
  });
};

export const setPreference = (data: { categoryIds: number[] }) => {
  return request<string>({
    url: '/api/recommendation/set-preference',
    method: 'post',
    data
  });
};

export const getPreferenceCategories = () => {
  return request<Category[]>({
    url: '/api/recommendation/categories',
    method: 'get'
  });
};

export const getNewUserGuide = () => {
  return request<{ hasPreference: boolean; recommendedCategories: Category[] }>({
    url: '/api/recommendation/new-user-guide',
    method: 'get'
  });
};
