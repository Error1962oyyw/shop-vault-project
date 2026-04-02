# 小铺宝库 - 智慧电商系统

> 基于Spring Boot 3 + Vue 3的智慧电商平台，集成YOLO视觉搜索与协同过滤推荐算法

## 项目概述

"小铺宝库"是一个面向中小型商家的全功能智慧电商系统，采用前后端分离架构设计。系统创新性地将YOLO目标检测算法与协同过滤推荐算法集成于Spring Boot微服务架构中，构建"交易+会员+AI+营销"的完整业务闭环。

### 核心特性

- **智慧导购**：基于YOLO算法的视觉搜索，实现"以图搜物"的直观购物体验
- **个性化推荐**：基于协同过滤算法的商品推荐，解决冷门商品曝光问题
- **会员营销体系**：VIP/SVIP会员、积分商城（含签到/兑换）、优惠券、会员日活动等闭环运营机制
- **统一订单系统**：支持普通商品/VIP/SVIP/积分兑换四种场景的订单创建与支付流程
- **全流程交易**：购物车、订单、支付（余额/积分/支付宝/微信）、物流、售后完整业务链
- **管理后台**：商品管理、订单管理、售后处理、营销配置、用户管理等完整后台

---

## 系统架构

### 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                        表现层 (Vue 3)                        │
│  Vue Router │ Pinia状态管理 │ Element Plus │ TypeScript     │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    业务逻辑层 (Spring Boot 3)                 │
│  Spring Security │ JWT认证 │ MyBatis Plus │ Spring AOP      │
│  Redis分布式锁 │ 定时任务 │ WebSocket实时通讯                 │
└─────────────────────────────────────────────────────────────┘
                              │
              ┌───────────────┼───────────────┐
              ▼               ▼               ▼
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│   MySQL 8.x    │ │   Redis 8.x    │ │   YOLO Service  │
│   数据持久化     │ │  缓存/分布式锁  │ │   AI识别服务     │
└─────────────────┘ └─────────────────┘ └─────────────────┘
```

### Controller 层职责划分

```
controller/
├── admin/                    ← 管理员专用接口（@PreAuthorize + BaseController）
│   ├── AdminController       → /api/admin          用户管理(列表/状态)
│   ├── AdminOrderController  → /api/admin/orders   订单管理(列表/详情/发货)
│   ├── AdminAfterSalesController → /api/admin/after-sales 售后管理
│   ├── AdminActivityController→ /api/admin/activities 活动CRUD
│   ├── AdminMessageController → /api/admin/messages 消息管理
│   ├── AdminPointsProductController → /api/admin/points-products 积分商品
│   ├── AdminPointsRuleController  → /api/admin/points-rules 积分规则
│   └── AdminVipController     → /api/admin/vip       VIP用户管理
│
├── order/                    ← 用户端订单接口
│   ├── OrderController       → /api/order           下单/支付/收货/取消
│   ├── AfterSalesController  → /api/after-sales     申请/取消/退货物流
│   └── UnifiedOrderController→ /api/orders          统一创建/支付/列表
│
├── marketing/                ← 营销接口
│   ├── MarketingController   → /api/marketing       签到/积分记录
│   ├── VipController         → /api/vip             VIP查看/购买/兑换
│   ├── PointsMallController  → /api/points-mall     积分商城浏览/兑换
│   ├── ActivityController    → /api/activity        活动领券
│   └── CouponController      → /api/coupons         优惠券领取/我的
│
├── product/                  ← 商品接口
├── system/                   ← 系统接口(用户/消息推送)
└── product-spec/             ← 商品规格值自定义
```

### 技术栈

#### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.5.12 | 核心框架 |
| Spring Security | - | 安全认证（RBAC权限控制） |
| MyBatis Plus | 3.5.16 | ORM框架 |
| MySQL | 8.x | 关系型数据库 |
| Redis | 8.x | 缓存/分布式锁/Token存储 |
| JWT | 0.13.0 | 无状态认证 |
| Lombok | - | 简化代码 |
| WebSocket | - | 实时通讯 |
| Spring Mail | - | 邮件服务 |
| Maven | 3.9+ | 项目构建 |

#### 前端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5+ | 渐进式框架（Composition API） |
| TypeScript | 5.9+ | 类型支持 |
| Vite | 8.0+ | 构建工具 |
| Vue Router | 4.6+ | 路由管理 |
| Pinia | 3.0+ | 状态管理 |
| Element Plus | 2.13+ | UI组件库 |
| Axios | 1.13+ | HTTP客户端 |
| Tailwind CSS | 4.2+ | 样式框架 |

#### AI服务

| 技术 | 说明 |
|------|------|
| YOLO (Ultralytics) | 目标检测算法，用于视觉搜索 |
| Python 3.11+ | Flask API 服务 |

---

## 核心功能模块

### 用户端功能

| 模块 | 功能点 | 说明 |
|------|--------|------|
| 注册登录 | 手机号/邮箱注册、JWT认证、密码重置 | 支持邮箱验证码 |
| 个人中心 | 头像上传、昵称/手机修改、密码管理、收货地址CRUD | - |
| 智慧导购 | YOLO视觉搜索、协同过滤推荐 | 以图搜物 |
| 商品浏览 | 商品列表、分类筛选、详情页、规格选择、收藏 | 支持规格值自定义 |
| 购物车 | 添加/删除/数量修改、批量结算 | - |
| 统一下单 | 普通商品/VIP月卡/SVIP年卡/积分兑换 四种场景 | 24小时自动过期 |
| 订单支付 | 余额支付、积分支付（纯积分兑换不支持混合） | VIP/SVIP不享受折扣 |
| 订单管理 | 列表/详情、确认收货、取消订单、申请售后 | - |
| 积分体系 | 每日签到（支持不限次数/连续签到奖励）、积分获取/消耗记录 | 规则可配置 |
| 积分商城 | 积分商品浏览、积分兑换下单 | - |
| VIP会员 | VIP月卡(¥99)、SVIP年卡(¥1499)购买/积分兑换 | 含权益展示 |
| 优惠券 | 领取优惠券、我的优惠券、使用/过期 | 已领自动过滤 |
| 售后服务 | 申请售后、填写退货物流、退款进度 | - |
| 客服聊天 | WebSocket实时消息通讯 | - |

### 管理端功能

| 模块 | 功能点 | 说明 |
|------|--------|------|
| 数据大屏 | 销售统计、趋势图表、用户增长 | ECharts可视化 |
| 商品管理 | 商品发布/编辑/上下架、SKU管理、库存管理、规格值自定义 | - |
| 订单管理 | 全部订单查询/详情、状态更新、发货（输入物流信息） | - |
| 售后管理 | 售后列表、同意/拒绝申请 | - |
| 优惠券管理 | 优惠券模板CRUD、发放/过期处理 | - |
| 会员日活动 | 活动CRUD、领券入口配置 | - |
| 积分商品管理 | 积分商品CRUD | - |
| 积分规则管理 | 签到规则配置（每日上限/不限次数/积分值） | - |
| VIP用户管理 | VIP用户列表/等级/到期时间 | - |
| 用户管理 | 用户列表/状态/余额调整 | - |
| YOLO映射 | 物体类别↔商品分类映射配置 | - |
| 消息中心 | 站内消息管理 | - |

---

## 统一订单与支付系统

### 支持的业务场景

| 场景 | 订单类型 | 支付方式 | 折扣优惠 | 优惠券 |
|------|---------|---------|---------|--------|
| 普通商品购买 | `ORDER_TYPE_NORMAL(0)` | 余额/支付宝/微信 | ✅ 可用 | ✅ 可用 |
| VIP月卡购买 | `ORDER_TYPE_VIP(1)` | 余额/支付宝/微信 | ❌ 禁用 | ❌ 隐藏 |
| SVIP年卡购买 | `ORDER_TYPE_SVIP(2)` | 余额/支付宝/微信 | ❌ 禁用 | ❌ 隐藏 |
| 积分兑换 | `ORDER_TYPE_POINTS_EXCHANGE(3)` | 仅积分支付 | ❌ 禁用 | ❌ 隐藏 |

### 订单生命周期

```
待支付(0) → 待发货(1) → 待收货(2) → 已完成(3)
                ↓               ↓
           售后中(5)      申请售后
                ↓
           已关闭(4) ← 24h超时自动取消 / 用户主动取消
```

### 核心 API

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/orders/create` | 创建普通商品订单 |
| POST | `/api/orders/vip` | 创建VIP/SVIP订单 |
| POST | `/api/orders/points-exchange` | 创建积分兑换订单 |
| POST | `/api/orders/{id}/pay` | 支付订单（BALANCE/POINTS） |
| POST | `/api/orders/{id}/cancel` | 取消订单 |
| GET | `/api/orders` | 用户订单列表 |
| GET | `/api/orders/{id}` | 订单详情 |

---

## 积分与签到系统

### 签到规则

- 支持**每日次数限制**配置（管理员可在后台设置 `dailyLimit`）
- 设置为 **0 表示不限次数**
- 支持**连续签到奖励**（7天/30天里程碑额外积分）

### 签到 API

| 方法 | 路径 | 返回值 | 说明 |
|------|------|--------|------|
| POST | `/api/marketing/sign-in` | SignInResult | 执行签到 |
| GET | `/api/marketing/sign-in/status` | SignInStatus | 获取签到状态 |

**SignInResult 结构**：
```json
{
  "points": 15,
  "todayCount": 2,
  "consecutiveDays": 5,
  "dailyLimit": 0,
  "remaining": 2147483647
}
```

---

## VIP 会员体系

### 会员类型

| 类型 | 价格 | 获得积分 | 有效期 | 权益 |
|------|------|---------|--------|------|
| VIP月卡 | ¥99 | 3000 | 30天 | 基础会员折扣 |
| SVIP年卡 | ¥1499 | 20000 | 365天 | 高级会员权益 |

### VIP 相关常量（集中管理）

所有 VIP 相关常量定义在 `VipConstants.java` 中：

```java
VipConstants.VIP_MONTHLY_PRICE      // ¥99.00
VipConstants.SVIP_YEARLY_PRICE      // ¥1499.00
VipConstants.VIP_MONTHLY_POINTS     // 3000
VipConstants.TYPE_VIP_MONTHLY        // 1
VipConstants.TYPE_SVIP_YEARLY        // 3
VipConstants.DURATION_VIP_MONTHLY    // 30 天
VipConstants.LEVEL_VIP               // 1
VipConstants.LEVEL_SVIP              // 2
```

---

## 环境配置与安装

### 环境要求

| 组件 | 版本要求 |
|------|----------|
| JDK | 21+ |
| Node.js | 18+ |
| MySQL | 8.x |
| Redis | 6.x+ |
| Maven | 3.9+ |
| Python | 3.11+ (YOLO服务可选) |

### 快速启动

#### 1. 后端

```bash
cd shop-vault-backend

# 配置环境变量
export JWT_SECRET="your_secret_key_at_least_32_characters"
export MYSQL_ACCOUNT=root
export MYSQL_PASSWORD=your_password
export REDIS_PASSWORD=your_redis_password

# 导入数据库
mysql -u root -p < shop-vault.sql
mysql -u root -p < shop-vault-data.sql

# 启动
mvn spring-boot:run
# 服务端口: 8080
```

#### 2. 前端

```bash
cd shop-vault-frontend

npm install
npm run dev
# 开发服务器: http://localhost:5173

npm run build
# 生产构建输出: dist/
```

#### 3. YOLO服务（可选）

```bash
cd shop-vault-yolo
pip install ultralytics flask
python yolo_api.py
# 服务端口: 5000
```

---

## 项目结构

```
shop-vault-project/
├── shop-vault-backend/              # Spring Boot 后端
│   └── src/main/java/com/TsukasaChan/ShopVault/
│       ├── annotation/              # @LogOperation 自定义注解
│       ├── aspect/                  # 操作日志 AOP 切面
│       ├── common/                  # Result/PageResult/BaseController/SecurityUtils/VipConstants
│       ├── config/                  # Security/WebSocket/Mail/CORS 配置
│       ├── controller/
│       │   ├── admin/               # 8个管理员 Controller
│       │   ├── order/               # 3个订单 Controller
│       │   ├── marketing/           # 5个营销 Controller
│       │   ├── product/             # 商品 Controller
│       │   ├── system/              # 系统 Controller
│       │   └── product-spec/        # 规格值 Controller
│       ├── dto/                     # CreateOrderDto/AfterSalesHandleDto 等
│       ├── entity/                  # 实体类（order/marketing/product/system）
│       ├── infrastructure/          # Redis 分布式锁
│       ├── mapper/                  # MyBatis Mapper 接口
│       ├── security/                # JWT Filter / UserDetails
│       ├── service/                 # 服务层接口
│       │   └── impl/                # 服务实现类
│       ├── task/                   # 定时任务（订单超时/VIP过期）
│       ├── util/                   # OrderNoGenerator 工具类
│       └── websocket/              # WebSocket 服务
│
├── shop-vault-frontend/             # Vue 3 前端
│   └── src/
│       ├── api/                    # 14个 API 模块文件
│       ├── components/             # 公共/布局/UI 组件
│       ├── views/
│       │   ├── admin/              # 16个管理页面
│       │   ├── orders/             # 用户订单
│       │   ├── points/             # 积中心(签到/优惠券/VIP)
│       │   ├── points-mall/        # 积分商城
│       │   └── ...                 # 其他用户页面
│       ├── stores/                 # Pinia 状态
│       ├── types/                  # TypeScript 类型定义
│       └── utils/                  # 工具函数
│
└── shop-vault-yolo/                # YOLO AI 服务
    └── yolo_api.py                # Flask API
```

---

## 开发规范

### 后端规范

- Controller 层按职责分离：用户端接口在业务包下，管理端接口统一在 `controller/admin/`
- 所有 Admin Controller 必须继承 `BaseController` 并使用 `@PreAuthorize("hasRole('ADMIN')")`
- VIP 相关常量统一使用 `VipConstants` 类，禁止硬编码
- 订单号生成使用 `OrderNoGenerator.generate()` 工具方法
- 使用 Lombok 注解简化实体类（`@Data`, `@RequiredArgsConstructor`）

### 前端规范

- 使用 `<script setup lang="ts">` Composition API
- API 调用统一封装在 `src/api/` 目录
- TypeScript 类型定义集中在 `types/api.d.ts`
- 使用 Element Plus 组件库
- 路径别名：`@/` = `src/`

---

## 注意事项

1. **JWT 配置必须设置**：`JWT_SECRET` 至少32字符，否则服务无法启动
2. **数据库初始化顺序**：先执行 `shop-vault.sql`（表结构），再执行 `shop-vault-data.sql`（初始数据）
3. **Redis 必须启动**：用于 Token 存储、分布式锁、缓存等核心功能
4. **订单超时自动取消**：通过定时任务每分钟检查，24小时未支付自动关闭
5. **VIP/SVIP 不享受折扣**：系统自动设置 `discountDisabled=1` 和 `afterSalesDisabled=1`
6. **积分兑换仅支持纯积分支付**：不允许混合支付方式

---

## 许可证

本项目仅供学习交流使用，未经授权不得用于商业用途。
