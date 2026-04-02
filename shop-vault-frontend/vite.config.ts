import { defineConfig, Plugin } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

function suppressWarningsPlugin(): Plugin {
  return {
    name: 'suppress-warnings',
    enforce: 'pre',
    transformIndexHtml(html) {
      const suppressScript = `
<script>
(function(){
  try {
    var _origWarn = console.warn;
    var _origError = console.error;
    var _origLog = console.log;
    
    var _isIDEWarning = function(msg) {
      if (!msg) return false;
      var str = '';
      if (typeof msg === 'string') str = msg;
      else if (msg && typeof msg === 'object' && msg.name) str = msg.name;
      else if (msg && msg.toString) str = msg.toString();
      return str.indexOf('MaxListenersExceededWarning') !== -1 && str.indexOf('vscode:icube:webview:browserUse') !== -1;
    };
    
    console.warn = function() {
      if (_isIDEWarning(arguments[0])) return;
      _origWarn.apply(console, arguments);
    };
    console.error = function() {
      if (_isIDEWarning(arguments[0])) return;
      _origError.apply(console, arguments);
    };
    console.log = function() {
      if (arguments[0] && typeof arguments[0] === 'string' && _isIDEWarning(arguments[0])) return;
      _origLog.apply(console, arguments);
    };

    if (typeof window !== 'undefined') {
      window.addEventListener('error', function(e) {
        if (e && e.message && _isIDEWarning(e.message)) {
          e.preventDefault();
          e.stopPropagation();
          return false;
        }
      }, true);
    }
  } catch(e) {}
})();
</script>`;
      return html.replace('<head>', '<head>' + suppressScript);
    }
  };
}

export default defineConfig({
  plugins: [
    suppressWarningsPlugin(),
    vue({
      template: {
        compilerOptions: {
          isCustomElement: (tag) => tag.startsWith('trae-')
        }
      }
    })
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  server: {
    port: 3000,
    strictPort: false,
    hmr: {
      overlay: true,
      timeout: 30000,
      clientPort: 3000
    },
    watch: {
      usePolling: false,
      interval: 100
    }
  },
  build: {
    chunkSizeWarningLimit: 1500,
    rollupOptions: {
      output: {
        manualChunks: {
          'element-plus': ['element-plus'],
          'element-plus-icons': ['@element-plus/icons-vue'],
          'vue-vendor': ['vue', 'vue-router', 'pinia']
        }
      }
    }
  },
  optimizeDeps: {
    include: ['element-plus', '@element-plus/icons-vue', 'pinia', 'vue-router'],
    force: false
  }
})
