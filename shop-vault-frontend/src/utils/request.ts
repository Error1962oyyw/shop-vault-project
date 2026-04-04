import axios, { InternalAxiosRequestConfig, AxiosResponse, AxiosError } from 'axios';
import { ElMessage } from 'element-plus';
import router from '@/router';
import { useUserStore } from '@/stores/user';

interface ApiError {
  response?: {
    data?: {
      msg?: string;
      message?: string;
      code?: number;
    };
    status?: number;
  };
  message?: string;
}

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
});

let isRedirecting = false;

service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = sessionStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
      if (config.url?.includes('/api/user/profile') || config.url?.includes('/api/auth/')) {
        console.log(`[API Request] ${config.method?.toUpperCase()} ${config.url}`, {
          hasToken: true,
          tokenPrefix: token.substring(0, 20) + '...'
        });
      }
    } else {
      const authRequiredEndpoints = ['/api/user/profile', '/api/cart/', '/api/order/', '/api/vip/', '/api/marketing/'];
      const requiresAuth = authRequiredEndpoints.some(endpoint => config.url?.includes(endpoint));
      if (requiresAuth) {
        console.warn(`[API Request] No token for authenticated endpoint: ${config.url}`);
      }
    }
    return config;
  },
  (error: AxiosError) => {
    console.error('请求拦截器错误:', error);
    return Promise.reject(error);
  }
);

service.interceptors.response.use(
  (response: AxiosResponse) => {
    const { code, msg, message, data } = response.data;
    const errorMsg = message || msg;

    console.log(`[API Response] ${response.config?.method?.toUpperCase()} ${response.config?.url}`, {
      code,
      hasData: !!data,
      dataType: typeof data,
      message: errorMsg
    });

    if (code === 0 || code === 200) {
      console.log(`[API Success] Request to ${response.config?.url} succeeded`);
      return data as any;
    } else {
      console.error(`[API Error] Code ${code} for ${response.config?.url}:`, errorMsg);
      const error: ApiError = {
        response: {
          data: { msg: errorMsg, code }
        },
        message: errorMsg || '请求失败'
      };
      return Promise.reject(error);
    }
  },
  (error: AxiosError) => {
    const apiError: ApiError = {
      message: error.message
    };
    
    if (error.response) {
      const status = error.response.status;
      const data = error.response.data as { msg?: string; message?: string } | undefined;
      const msg = data?.message || data?.msg;
      
      switch (status) {
        case 401:
          handleUnauthorized(error.config?.url, msg || '');
          break;
        case 403:
          apiError.response = { 
            data: { msg: msg || '权限不足' } 
          };
          apiError.message = msg || '权限不足';
          break;
        case 404:
          apiError.response = { 
            data: { msg: msg || '请求的资源不存在' } 
          };
          apiError.message = msg || '请求的资源不存在';
          break;
        case 423:
          apiError.response = { 
            data: { msg: msg || '账户已锁定' } 
          };
          apiError.message = msg || '账户已锁定';
          break;
        case 500:
          apiError.response = { 
            data: { msg: msg || '服务器内部错误' } 
          };
          apiError.message = msg || '服务器内部错误';
          break;
        case 502:
        case 503:
        case 504:
          apiError.response = { 
            data: { msg: '服务暂时不可用，请稍后重试' } 
          };
          apiError.message = '服务暂时不可用，请稍后重试';
          break;
        default:
          apiError.response = { 
            data: { msg: msg || `请求失败 (${status})` } 
          };
          apiError.message = msg || `请求失败 (${status})`;
      }
    } else if (error.code === 'ECONNABORTED') {
      apiError.response = { 
        data: { msg: '请求超时，请检查网络连接' } 
      };
      apiError.message = '请求超时，请检查网络连接';
    } else if (!navigator.onLine) {
      apiError.response = { 
        data: { msg: '网络连接已断开' } 
      };
      apiError.message = '网络连接已断开';
    } else {
      apiError.response = { 
        data: { msg: '无法连接到服务器' } 
      };
      apiError.message = '无法连接到服务器';
    }
    
    return Promise.reject(apiError);
  }
);

function isAuthEndpoint(url: string): boolean {
  return url.includes('/api/auth/login') || url.includes('/api/auth/admin/login');
}

function handleUnauthorized(url?: string, errorMsg?: string) {
  if (isAuthEndpoint(url || '')) {
    return;
  }

  if (errorMsg && (errorMsg.includes('暂停') || errorMsg.includes('禁用') || errorMsg.includes('Disabled') || errorMsg.includes('suspended'))) {
    return;
  }
  
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
}

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
