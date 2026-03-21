import { defineStore } from 'pinia';
import { ref } from 'vue';
import { getCategoryList } from '@/api/product';
import type { Category } from '@/types/api';

export const useCategoryStore = defineStore('category', () => {
  const categories = ref<Category[]>([]);
  const loading = ref(false);

  const fetchCategories = async () => {
    if (categories.value.length > 0) return;
    
    loading.value = true;
    try {
      categories.value = await getCategoryList();
    } catch (error) {
      console.error('获取分类失败', error);
    } finally {
      loading.value = false;
    }
  };

  const getCategoryName = (id: number): string => {
    const findCategory = (list: Category[], categoryId: number): Category | null => {
      for (const cat of list) {
        if (cat.id === categoryId) return cat;
        if (cat.children) {
          const found = findCategory(cat.children, categoryId);
          if (found) return found;
        }
      }
      return null;
    };
    const category = findCategory(categories.value, id);
    return category?.name || '';
  };

  return {
    categories,
    loading,
    fetchCategories,
    getCategoryName
  };
});
