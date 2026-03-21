import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { getCartList, addToCart, updateCartQuantity, deleteCartItems } from '@/api/cart';
import type { CartItem } from '@/types/api';
import { ElMessage } from 'element-plus';

export const useCartStore = defineStore('cart', () => {
  const cartItems = ref<CartItem[]>([]);
  const loading = ref(false);

  const totalCount = computed(() => {
    return cartItems.value.reduce((sum, item) => sum + item.quantity, 0);
  });

  const selectedItems = computed(() => {
    return cartItems.value.filter(item => item.selected);
  });

  const selectedCount = computed(() => {
    return selectedItems.value.reduce((sum, item) => sum + item.quantity, 0);
  });

  const selectedAmount = computed(() => {
    return selectedItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0);
  });

  const isAllSelected = computed({
    get: () => cartItems.value.length > 0 && cartItems.value.every(item => item.selected),
    set: (value: boolean) => {
      cartItems.value.forEach(item => {
        item.selected = value;
      });
    }
  });

  const fetchCartList = async () => {
    loading.value = true;
    try {
      cartItems.value = await getCartList();
    } catch (error) {
      console.error('获取购物车失败', error);
    } finally {
      loading.value = false;
    }
  };

  const addItem = async (productId: number, quantity: number = 1) => {
    try {
      await addToCart({ productId, quantity });
      ElMessage.success('已加入购物车');
      await fetchCartList();
    } catch (error) {
      console.error('加入购物车失败', error);
    }
  };

  const updateQuantity = async (id: number, quantity: number) => {
    try {
      await updateCartQuantity(id, quantity);
      const item = cartItems.value.find(i => i.id === id);
      if (item) {
        item.quantity = quantity;
      }
    } catch (error) {
      console.error('更新数量失败', error);
      await fetchCartList();
    }
  };

  const removeItems = async (ids: number[]) => {
    try {
      await deleteCartItems(ids);
      cartItems.value = cartItems.value.filter(item => !ids.includes(item.id));
      ElMessage.success('删除成功');
    } catch (error) {
      console.error('删除失败', error);
    }
  };

  const toggleSelect = (id: number) => {
    const item = cartItems.value.find(i => i.id === id);
    if (item) {
      item.selected = !item.selected;
    }
  };

  return {
    cartItems,
    loading,
    totalCount,
    selectedItems,
    selectedCount,
    selectedAmount,
    isAllSelected,
    fetchCartList,
    addItem,
    updateQuantity,
    removeItems,
    toggleSelect
  };
});
