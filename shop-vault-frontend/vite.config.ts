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
  'use strict';
  
  var _SUPPRESS_PATTERNS = ['MaxListenersExceededWarning', 'vscode:icube:webview:browserUse'];
  
  function _shouldSuppress(msg) {
    if (!msg) return false;
    var s = typeof msg === 'string' ? msg : (msg.name || msg.message || String(msg));
    for (var i = 0; i < _SUPPRESS_PATTERNS.length; i++) {
      if (s.indexOf(_SUPPRESS_PATTERNS[i]) === -1) return false;
    }
    return true;
  }
  
  var _origWarn = console.warn.bind(console);
  var _origError = console.error.bind(console);
  var _origInfo = console.info.bind(console);
  var _origLog = console.log.bind(console);
  
  console.warn = function() {
    if (_shouldSuppress(arguments[0])) return;
    _origWarn.apply(console, arguments);
  };
  console.error = function() {
    if (_shouldSuppress(arguments[0])) return;
    _origError.apply(console, arguments);
  };
  console.info = function() {
    if (_shouldSuppress(arguments[0])) return;
    _origInfo.apply(console, arguments);
  };
  console.log = function() {
    if (_shouldSuppress(arguments[0])) return;
    _origLog.apply(console, arguments);
  };

  if (typeof window !== 'undefined') {
    var _origErrorEvent = window.onerror || null;
    window.onerror = function(msg, url, line, col, error) {
      if (_shouldSuppress(msg) || _shouldSuppress(error)) return true;
      if (_origErrorEvent) return _origErrorEvent.apply(window, arguments);
      return false;
    };
    
    var _unhandledRejection = window.onunhandledrejection || null;
    window.addEventListener('unhandledrejection', function(e) {
      if (_shouldSuppress(e.reason)) {
        e.preventDefault();
      } else if (_unhandledRejection) {
        _unhandledRejection.call(window, e);
      }
    });
  }

  try {
    if (typeof process !== 'undefined' && process.setMaxListeners) {
      process.setMaxListeners(100);
    }
    if (typeof process !== 'undefined' && process.on) {
      process.on('warning', function(warning) {
        if (warning && _shouldSuppress(warning)) return;
      });
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
    port: 5173,
    host: '127.0.0.1',
    strictPort: false,
    hmr: {
      overlay: true,
      timeout: 30000,
      clientPort: 5173
    },
    watch: {
      usePolling: false,
      interval: 100
    },
    proxy: {
      '/api': {
        target: 'http://localhost:9090',
        changeOrigin: true,
        secure: false,
        configure: (proxy, _options) => {
          proxy.on('error', (err, _req, _res) => {
            console.log('[Proxy Error]', err.message);
          });
          proxy.on('proxyReq', (_proxyReq, req, _res) => {
            if (req.url?.includes('/auth/') || req.url?.includes('/user/profile')) {
              console.log(`[Proxy] ${req.method} ${req.url} → http://localhost:9090`);
            }
          });
        }
      }
    }
  },
  build: {
    chunkSizeWarningLimit: 1500,
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: process.env.NODE_ENV === 'production',
        drop_debugger: process.env.NODE_ENV === 'production'
      }
    }
  },
  optimizeDeps: {
    include: ['element-plus', '@element-plus/icons-vue', 'pinia', 'vue-router'],
    force: false
  }
})
