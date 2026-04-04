import { defineStore } from 'pinia';
import { ref } from 'vue';
import { getProfile } from '@/api/user';
import type { UserInfo } from '@/types/api';

export const useUserStore = defineStore('user', () => {
  const token = ref(sessionStorage.getItem('token') || '');
  const userInfo = ref<UserInfo | null>(null);
  const isAdmin = ref(sessionStorage.getItem('isAdmin') === 'true');
  const isGuest = ref(sessionStorage.getItem('isGuest') === 'true');

  const setToken = (newToken: string) => {
    token.value = newToken;
    userInfo.value = null;
    isAdmin.value = false;
    sessionStorage.setItem('token', newToken);
    sessionStorage.removeItem('isAdmin');
    sessionStorage.removeItem('isGuest');
    isGuest.value = false;
  };

  const setIsAdmin = (value: boolean) => {
    isAdmin.value = value;
    sessionStorage.setItem('isAdmin', String(value));
  };

  const setIsGuest = (value: boolean) => {
    isGuest.value = value;
    if (value) {
      sessionStorage.setItem('isGuest', 'true');
    } else {
      sessionStorage.removeItem('isGuest');
    }
  };

  const clearToken = () => {
    token.value = '';
    userInfo.value = null;
    isAdmin.value = false;
    isGuest.value = false;
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('isAdmin');
    sessionStorage.removeItem('isGuest');
  };

  const fetchUserInfo = async () => {
    console.log('[UserStore] fetchUserInfo called, token exists:', !!token.value);

    if (!token.value) {
      console.error('[UserStore Error] No token available');
      throw new Error('No token');
    }

    try {
      console.log('[UserStore] Calling getProfile API...');
      const res = await getProfile();
      console.log('[UserStore] Profile data received:', {
        hasData: !!res,
        userId: res?.id,
        username: res?.username,
        email: res?.email,
        role: res?.role
      });

      userInfo.value = res;
      isAdmin.value = res.role === 'ADMIN';
      sessionStorage.setItem('isAdmin', String(isAdmin.value));

      console.log('[UserStore] User state updated:', {
        isAdmin: isAdmin.value,
        userInfoSet: !!userInfo.value
      });

      return res;
    } catch (error) {
      console.error('[UserStore Error] Failed to fetch user profile:', error);
      userInfo.value = null;
      throw error;
    }
  };

  const updateUserInfo = (updates: Partial<UserInfo>) => {
    if (userInfo.value) {
      userInfo.value = { ...userInfo.value, ...updates };
    }
  };

  const enterGuestMode = () => {
    clearToken();
    setIsGuest(true);
  };

  const isLoggedIn = () => {
    return !!token.value;
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
    updateUserInfo,
    enterGuestMode,
    isLoggedIn
  };
});
