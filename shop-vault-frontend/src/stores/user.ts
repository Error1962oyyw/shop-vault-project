import { defineStore } from 'pinia';
import { ref } from 'vue';
import { getProfile } from '@/api/user';
import type { UserInfo } from '@/types/api';

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '');
  const userInfo = ref<UserInfo | null>(null);
  const isAdmin = ref(localStorage.getItem('isAdmin') === 'true');
  const isGuest = ref(localStorage.getItem('isGuest') === 'true');

  const setToken = (newToken: string) => {
    token.value = newToken;
    localStorage.setItem('token', newToken);
    isGuest.value = false;
    localStorage.removeItem('isGuest');
  };

  const setIsAdmin = (value: boolean) => {
    isAdmin.value = value;
    localStorage.setItem('isAdmin', String(value));
  };

  const setIsGuest = (value: boolean) => {
    isGuest.value = value;
    if (value) {
      localStorage.setItem('isGuest', 'true');
    } else {
      localStorage.removeItem('isGuest');
    }
  };

  const clearToken = () => {
    token.value = '';
    userInfo.value = null;
    isAdmin.value = false;
    isGuest.value = false;
    localStorage.removeItem('token');
    localStorage.removeItem('isAdmin');
    localStorage.removeItem('isGuest');
  };

  const fetchUserInfo = async () => {
    try {
      const res = await getProfile();
      userInfo.value = res;
    } catch (error) {
      console.error('获取用户信息失败', error);
    }
  };

  const enterGuestMode = () => {
    clearToken();
    setIsGuest(true);
  };

  return {
    token,
    userInfo,
    isAdmin,
    isGuest,
    setToken,
    setIsAdmin,
    setIsGuest,
    clearToken,
    fetchUserInfo,
    enterGuestMode
  };
});
