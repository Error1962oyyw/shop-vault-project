import request from '@/utils/request';
import type { DashboardStats } from '@/types/api';

export const getDashboardStats = () => {
  return request<DashboardStats>({
    url: '/api/dashboard/stats',
    method: 'get'
  });
};
