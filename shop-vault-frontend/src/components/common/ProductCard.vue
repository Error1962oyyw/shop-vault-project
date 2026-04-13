<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ShoppingCart, Star, View } from '@element-plus/icons-vue'
import type { Product } from '@/types/api'

const props = defineProps<{
  product: Product
}>()

const router = useRouter()

const discount = computed(() => {
  if (props.product.originalPrice > props.product.price) {
    return Math.round((1 - props.product.price / props.product.originalPrice) * 100)
  }
  return 0
})

const goToDetail = () => {
  router.push(`/product/${props.product.id}`)
}
</script>

<template>
  <div 
    class="product-card bg-white rounded-xl overflow-hidden cursor-pointer border border-gray-100 hover:border-blue-200 group"
    @click="goToDetail"
  >
    <div class="relative aspect-square overflow-hidden bg-gradient-to-br from-gray-50 to-gray-100">
      <img 
        :src="product.mainImage || '/placeholder.png'" 
        :alt="product.name"
        class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500"
      />
      
      <div class="absolute top-3 left-3 flex gap-2">
        <div 
          v-if="discount" 
          class="bg-gradient-to-r from-red-500 to-rose-500 text-white text-xs font-semibold px-3 py-1.5 rounded-full shadow-sm"
        >
          {{ discount }}%OFF
        </div>
        <div 
          v-if="product.sales > 100" 
          class="bg-gradient-to-r from-blue-500 to-cyan-500 text-white text-xs font-semibold px-3 py-1.5 rounded-full shadow-sm flex items-center gap-1"
        >
          <Star class="w-3 h-3" />
          热销
        </div>
      </div>
      
      <div 
        v-if="product.stock === 0" 
        class="absolute inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center"
      >
        <div class="text-center">
          <div class="text-white text-xl font-bold mb-1">已售罄</div>
          <div class="text-white/70 text-sm">敬请期待</div>
        </div>
      </div>
      
      <div class="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/60 to-transparent p-4 translate-y-full group-hover:translate-y-0 transition-transform duration-300">
        <div class="flex gap-2">
          <el-button 
            type="primary" 
            size="small" 
            class="flex-1 bg-white/20 backdrop-blur-sm border-0 hover:bg-white/30 text-white rounded-lg font-medium"
            @click.stop
          >
            <View class="w-4 h-4 mr-1" />
            查看详情
          </el-button>
          <el-button 
            type="primary" 
            size="small" 
            class="bg-blue-500 hover:bg-blue-600 border-0 rounded-lg"
            @click.stop
          >
            <ShoppingCart class="w-4 h-4" />
          </el-button>
        </div>
      </div>
    </div>
    
    <div class="p-5">
      <h3 class="text-sm font-medium line-clamp-2 h-10 mb-3 text-gray-800 group-hover:text-blue-500 transition-colors">
        {{ product.name }}
      </h3>
      
      <div class="flex items-baseline gap-2 mb-3">
        <span class="text-xl font-extrabold text-red-500">
          ¥{{ product.price.toFixed(2) }}
        </span>
        <span 
          v-if="product.originalPrice > product.price" 
          class="text-sm text-gray-400 line-through"
        >
          ¥{{ product.originalPrice.toFixed(2) }}
        </span>
      </div>
      
      <div class="flex items-center justify-between text-xs text-gray-400">
        <div class="flex items-center gap-1">
          <span class="bg-gray-100 px-2 py-1 rounded text-gray-500">
            已售 {{ product.sales }}
          </span>
        </div>
        <span class="text-gray-400">{{ product.categoryName }}</span>
      </div>
    </div>
  </div>
</template>
