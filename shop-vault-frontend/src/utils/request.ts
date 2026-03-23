import axios, { InternalAxiosRequestConfig, AxiosResponse } from 'axios';
import { ElMessage } from 'element-plus';
import router from '@/router';
import { useUserStore } from '@/stores/user';

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
});

let isRedirecting = false;

service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

service.interceptors.response.use(
  (response: AxiosResponse) => {
    const { code, msg, message, data } = response.data;
    const errorMsg = message || msg;

    if (code === 200) {
      return data as any;
    } else {
      return Promise.reject({ 
        response: { 
          data: { msg: errorMsg, code } 
        },
        message: errorMsg || 'Error'
      });
    }
  },
  (error) => {
    if (error.response) {
      const status = error.response.status;
      const msg = error.response.data?.message || error.response.data?.msg;
      
      if (status === 401) {
        if (!isRedirecting) {
          isRedirecting = true;
          const userStore = useUserStore();
          userStore.clearToken();
          userStore.setIsGuest(true);
          
          ElMessage.warning('请先登录');
          
          const currentPath = router.currentRoute.value.fullPath;
          const isLoginPage = router.currentRoute.value.path === '/login' || 
                              router.currentRoute.value.path === '/admin/login';
          
          if (!isLoginPage) {
            router.push({ 
              path: '/login', 
              query: { redirect: currentPath } 
            }).finally(() => {
              isRedirecting = false;
            });
          } else {
            isRedirecting = false;
          }
        }
      } else if (status === 403) {
        return Promise.reject({
          response: { 
            data: { msg: msg || '权限不足' } 
          },
          message: msg || '权限不足'
        });
      } else if (status === 404) {
        return Promise.reject({
          response: { 
            data: { msg: msg || '请求的资源不存在' } 
          },
          message: msg || '请求的资源不存在'
        });
      } else if (status === 500) {
        return Promise.reject({
          response: { 
            data: { msg: msg || '服务器内部错误' } 
          },
          message: msg || '服务器内部错误'
        });
      } else {
        return Promise.reject({
          response: { 
            data: { msg: msg || '请求失败' } 
          },
          message: msg || '请求失败'
        });
      }
    } else {
      return Promise.reject({
        message: '无法连接到服务器'
      });
    }
  }
);

interface RequestConfig {
  url: string;
  method?: 'get' | 'post' | 'put' | 'delete' | 'patch';
  data?: any;
  params?: any;
  headers?: any;
  timeout?: number;
}

export default function request<T>(config: RequestConfig): Promise<T> {
  return service(config) as Promise<T>;
}

export { service as axiosInstance };
