<script setup lang="ts">
</script>

<template>
  <footer class="site-footer">
    <div class="footer-container">
      <div class="footer-grid">
        <div class="footer-brand">
          <div class="brand-logo">
            <el-icon class="logo-icon"><Shop /></el-icon>
          </div>
          <div class="brand-text">
            <h3 class="brand-name">小铺宝库</h3>
            <p class="brand-desc">基于SpringBoot的智慧电商系统</p>
            <p class="brand-desc">集成AI视觉搜索与协同过滤推荐</p>
          </div>
        </div>

        <div class="footer-section">
          <h4 class="footer-title">购物指南</h4>
          <ul class="footer-links">
            <li><a href="#" class="footer-link">购物流程</a></li>
            <li><a href="#" class="footer-link">会员介绍</a></li>
            <li><a href="#" class="footer-link">积分规则</a></li>
            <li><a href="#" class="footer-link">常见问题</a></li>
          </ul>
        </div>

        <div class="footer-section">
          <h4 class="footer-title">配送方式</h4>
          <ul class="footer-links">
            <li><a href="#" class="footer-link">配送范围</a></li>
            <li><a href="#" class="footer-link">配送费用</a></li>
            <li><a href="#" class="footer-link">配送时效</a></li>
            <li><a href="#" class="footer-link">验货签收</a></li>
          </ul>
        </div>

        <div class="footer-section">
          <h4 class="footer-title">售后服务</h4>
          <ul class="footer-links">
            <li><a href="#" class="footer-link">退换货政策</a></li>
            <li><a href="#" class="footer-link">退款说明</a></li>
            <li><a href="#" class="footer-link">联系客服</a></li>
            <li><a href="#" class="footer-link">投诉建议</a></li>
          </ul>
        </div>
      </div>

      <el-divider class="footer-divider" />

      <div class="footer-bottom">
        <div class="copyright">
          <span class="copyright-text">© 2026 小铺宝库 - 毕业设计作品</span>
          <span class="separator">|</span>
          <span class="copyright-text">基于 Spring Boot + Vue 3</span>
        </div>
        <div class="legal-links">
          <a href="#" class="legal-link">隐私政策</a>
          <a href="#" class="legal-link">用户协议</a>
        </div>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.site-footer {
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  padding: 48px 0 32px;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.footer-grid {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr;
  gap: 48px;
}

.footer-brand {
  display: flex;
  gap: 16px;
}

.brand-logo {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.logo-icon {
  font-size: 28px;
  color: #fff;
}

.brand-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.brand-name {
  font-size: 20px;
  font-weight: 700;
  color: #ffffff;
  margin: 0;
}

.brand-desc {
  font-size: 14px;
  color: #c9d1d9;
  margin: 0;
  line-height: 1.6;
}

.footer-section {
  display: flex;
  flex-direction: column;
}

.footer-title {
  font-size: 15px;
  font-weight: 600;
  color: #ffffff;
  margin: 0 0 20px 0;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
}

.footer-links {
  display: flex;
  flex-direction: column;
  gap: 12px;
  list-style: none;
  padding: 0;
  margin: 0;
}

.footer-links li {
  margin: 0;
}

.footer-link {
  color: #c9d1d9;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.2s ease;
  display: inline-block;
}

.footer-link:hover {
  color: #58a6ff;
}

.footer-divider {
  margin: 32px 0;
  border-color: rgba(255, 255, 255, 0.1);
}

.footer-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.copyright {
  display: flex;
  align-items: center;
  gap: 12px;
}

.copyright-text {
  font-size: 14px;
  color: #c9d1d9;
}

.separator {
  color: rgba(255, 255, 255, 0.3);
}

.legal-links {
  display: flex;
  gap: 24px;
}

.legal-link {
  font-size: 14px;
  color: #c9d1d9;
  text-decoration: none;
  transition: color 0.2s ease;
}

.legal-link:hover {
  color: #58a6ff;
}

@media (max-width: 1024px) {
  .footer-grid {
    grid-template-columns: 1fr 1fr;
    gap: 32px;
  }

  .footer-brand {
    grid-column: 1 / -1;
  }
}

@media (max-width: 768px) {
  .site-footer {
    padding: 32px 0 24px;
  }

  .footer-grid {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .footer-brand {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .footer-section {
    text-align: center;
  }

  .footer-links {
    align-items: center;
  }

  .footer-bottom {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }

  .copyright {
    flex-direction: column;
    gap: 8px;
  }

  .separator {
    display: none;
  }
}
</style>
