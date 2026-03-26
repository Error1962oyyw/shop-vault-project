# 小铺宝库 - 智慧电商系统

> 基于Spring Boot + Vue 3的智慧电商平台，集成YOLO视觉搜索与协同过滤推荐算法

## 项目概述

"小铺宝库"是一个面向中小型商家的智慧电商系统，采用前后端分离架构设计。系统创新性地将YOLO目标检测算法与协同过滤推荐算法集成于Spring Boot微服务架构中，构建"交易+会员+AI"的业务闭环，为用户提供智能化的购物体验。

### 核心特性

- **智慧导购**：基于YOLO算法的视觉搜索，实现"以图搜物"的直观购物体验
- **个性化推荐**：基于协同过滤算法的商品推荐，解决冷门商品曝光问题
- **会员营销体系**：积分商城、会员日活动等闭环运营机制
- **全流程交易**：购物车、订单、支付、物流、售后完整业务链

---

## 研究背景与意义

### 选题意义

随着移动互联网与人工智能技术的深度融合，传统电商模式正面临"信息过载"与"交互单一"的双重挑战。消费者对购物体验的要求已从单纯的商品交易转向个性化、智能化与综合服务，而传统电商平台在功能集成及中小商户赋能方面仍存在不足。

本系统的设计意义体现在三个方面：

1. **AI辅助导航**：针对用户"难以准确描述商品名称"的场景，利用YOLO算法提供"视觉分类入口"，快速定位商品所属大类，实现"AI粗筛+人工细选"的创新检索模式

2. **个性化服务**：利用协同过滤算法分析用户行为，实现千人千面的商品推荐，解决冷门商品曝光不足问题

3. **中小商家赋能**：构建闭环会员营销体系，提供低成本、高适配的数字化经营解决方案

### 技术创新

- YOLO目标检测与电商场景的深度融合
- 基于物品的协同过滤推荐算法
- Spring Boot微服务架构下的AI服务解耦设计

---

## 核心功能

### 用户端功能

| 模块 | 功能描述 |
|------|----------|
| 注册登录 | 支持手机号/邮箱注册、JWT身份认证 |
| 个人中心 | 头像/昵称修改、密码管理、收货地址管理 |
| 智慧导购 | YOLO视觉搜索、协同过滤个性化推荐 |
| 购物车 | 商品添加、数量修改、批量结算 |
| 订单交易 | 下单、模拟支付、物流查询、确认收货 |
| 商品评价 | 评分、图文评价 |
| 积分体系 | 积分获取、积分兑换、会员日活动 |
| 客服聊天 | WebSocket实时通讯 |

### 管理端功能

| 模块 | 功能描述 |
|------|----------|
| 商品管理 | 商品发布、SKU管理、库存管理 |
| 订单管理 | 订单查询、发货处理 |
| 售后管理 | 退款审核、退货处理 |
| 营销管理 | 优惠券、会员日活动、积分规则 |
| 用户管理 | 用户查询、状态管理 |
| 数据大屏 | 销售统计、趋势分析 |
| YOLO映射 | 物体类别与商品分类映射配置 |

---

## 技术架构

### 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                        表现层 (Vue.js)                        │
│  组件化开发 │ Vue Router │ Pinia状态管理 │ Element Plus      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    业务逻辑层 (Spring Boot)                   │
│  Spring Security │ JWT认证 │ MyBatis Plus │ Spring AOP      │
└─────────────────────────────────────────────────────────────┘
                              │
              ┌───────────────┼───────────────┐
              ▼               ▼               ▼
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│   MySQL 8.4.8   │ │   Redis 8.6.0   │ │   YOLO Service  │
│   数据持久化     │ │  缓存/Token     │ │   AI识别服务     │
└─────────────────┘ └─────────────────┘ └─────────────────┘
```

### 技术栈

#### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.5.12 | 核心框架 |
| Spring Security | - | 安全认证 |
| MyBatis Plus | 3.5.16 | ORM框架 |
| MySQL | 8.4.8 | 关系型数据库 |
| Redis | 8.6.0 | 缓存中间件 |
| JWT | 0.13.0 | 无状态认证 |
| Hutool | 5.8.44 | 工具库 |
| WebSocket | - | 实时通讯 |
| Spring Mail | - | 邮件服务 |
| Maven | 3.9.14 | 项目构建 |

#### 前端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5.30 | 渐进式框架 |
| TypeScript | 5.9.3 | 类型支持 |
| Vite | 8.0.0 | 构建工具 |
| Vue Router | 4.6.4 | 路由管理 |
| Pinia | 3.0.4 | 状态管理 |
| Element Plus | 2.13.6 | UI组件库 |
| Tailwind CSS | 4.2.1 | 样式框架 |
| Axios | 1.13.6 | HTTP客户端 |
| Node.js | 24.14.0 | 运行环境 |

#### AI服务

| 技术 | 版本 | 说明 |
|------|------|------|
| YOLO | - | 目标检测算法，用于视觉搜索 |
| Ultralytics | - | YOLO模型框架 |
| Python | 3.11.14 | AI服务运行环境 |

---

## 环境配置与安装

### 环境要求

| 组件 | 版本要求 |
|------|----------|
| JDK | 21+ |
| Node.js | 24.14.0+ |
| MySQL | 8.4.8+ |
| Redis | 8.6.0+ |
| Maven | 3.9.14+ |
| Python | 3.11.14+ |

### 后端配置

1. **克隆项目**

```bash
git clone <repository-url>
cd shop-vault-project
```

2. **配置环境变量**

创建环境变量或修改 `application.yml`：

```bash
# MySQL配置
MYSQL_ACCOUNT=your_mysql_account
MYSQL_PASSWORD=your_mysql_password

# Redis配置
REDIS_PASSWORD=your_redis_password

# JWT配置（必须配置）
JWT_SECRET=your_jwt_secret_key_at_least_32_characters
JWT_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=604800000
JWT_MIN_SECRET_LENGTH=32

# 邮件服务配置
QQ_MAIL_ACCOUNT=your_qq_email
QQ_MAIL_AUTHORIZATION=your_mail_auth_code
```

3. **初始化数据库**

```bash
mysql -u root -p < shop-vault-backend/shop-vault.sql
```

4. **启动后端服务**

```bash
cd shop-vault-backend
./mvnw spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动

### 前端配置

1. **安装依赖**

```bash
cd shop-vault-frontend
npm install
```

2. **启动开发服务器**

```bash
npm run dev
```

前端服务将在 `http://localhost:5173` 启动

### YOLO服务配置

1. **进入YOLO目录**

```bash
cd shop-vault-yolo
```

2. **安装依赖**

```bash
pip install ultralytics flask
```

3. **启动服务**

```bash
python yolo_api.py
```

YOLO服务将在 `http://localhost:5000` 启动

---

## JWT环境变量配置说明

| 环境变量名 | 配置键名 | 说明 | 建议值/格式 |
|------------|----------|------|-------------|
| `JWT_SECRET` | `shop-vault.jwt.secret` | JWT签名密钥 | 至少32字符的随机字符串，如：`your_jwt_secret_key_at_least_32_characters` |
| `JWT_EXPIRATION` | `shop-vault.jwt.expiration` | Token有效期（毫秒） | 默认值：`3600000`（1小时） |
| `JWT_REFRESH_EXPIRATION` | `shop-vault.jwt.refresh-expiration` | 刷新Token有效期（毫秒） | 默认值：`604800000`（7天） |
| `JWT_MIN_SECRET_LENGTH` | `shop-vault.jwt.min-secret-length` | 密钥最小长度 | 默认值：`32` |

### 配置方法

**Windows系统环境变量配置：**
```powershell
# 临时配置（当前终端有效）
$env:JWT_SECRET="your_secret_key_at_least_32_characters"

# 永久配置（系统环境变量）
[Environment]::SetEnvironmentVariable("JWT_SECRET", "your_secret_key_at_least_32_characters", "User")
```

**Linux/Mac系统环境变量配置：**
```bash
# 临时配置
export JWT_SECRET="your_secret_key_at_least_32_characters"

# 永久配置（添加到 ~/.bashrc 或 ~/.zshrc）
echo 'export JWT_SECRET="your_secret_key_at_least_32_characters"' >> ~/.bashrc
source ~/.bashrc
```

---

## 项目结构

```
shop-vault-project/
├── shop-vault-backend/           # 后端项目
│   ├── src/main/java/
│   │   └── com/TsukasaChan/ShopVault/
│   │       ├── annotation/       # 自定义注解
│   │       ├── aspect/           # AOP切面
│   │       ├── common/           # 公共组件
│   │       ├── config/           # 配置类
│   │       ├── controller/       # 控制器层
│   │       │   ├── admin/        # 管理端接口
│   │       │   ├── marketing/    # 营销接口
│   │       │   ├── order/        # 订单接口
│   │       │   ├── product/      # 商品接口
│   │       │   └── system/       # 系统接口
│   │       ├── dto/              # 数据传输对象
│   │       ├── entity/           # 实体类
│   │       │   ├── marketing/    # 营销相关实体
│   │       │   ├── order/        # 订单相关实体
│   │       │   ├── product/      # 商品相关实体
│   │       │   └── system/       # 系统相关实体
│   │       ├── infrastructure/   # 基础设施服务
│   │       ├── integration/      # 外部服务集成
│   │       ├── manager/          # 业务管理器
│   │       ├── mapper/           # MyBatis Mapper
│   │       ├── security/         # 安全认证
│   │       ├── service/          # 服务层
│   │       ├── task/             # 定时任务
│   │       ├── util/             # 工具类
│   │       └── websocket/        # WebSocket
│   └── src/main/resources/
│       └── application.yml       # 配置文件
│
├── shop-vault-frontend/          # 前端项目
│   ├── src/
│   │   ├── api/                  # API接口
│   │   ├── assets/               # 静态资源
│   │   ├── components/           # 组件
│   │   │   ├── admin/            # 管理端组件
│   │   │   ├── common/           # 公共组件
│   │   │   └── layout/           # 布局组件
│   │   ├── composables/          # 组合式函数
│   │   ├── router/               # 路由配置
│   │   ├── stores/               # Pinia状态
│   │   ├── types/                # TypeScript类型
│   │   ├── utils/                # 工具函数
│   │   └── views/                # 页面视图
│   │       ├── admin/            # 管理端页面
│   │       ├── ai-search/        # AI搜索
│   │       ├── cart/             # 购物车
│   │       ├── checkout/         # 结算
│   │       ├── home/             # 首页
│   │       ├── login/            # 登录
│   │       ├── member-day/       # 会员日
│   │       ├── orders/           # 订单
│   │       ├── points/           # 积分
│   │       ├── product/          # 商品详情
│   │       ├── products/         # 商品列表
│   │       └── profile/          # 个人中心
│   └── vite.config.ts            # Vite配置
│
└── shop-vault-yolo/              # YOLO AI服务
    └── yolo_api.py               # 预测服务
```

---

## API文档

### 认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/register | 用户注册 |
| POST | /api/auth/login | 用户登录 |
| POST | /api/auth/refresh | 刷新Token |
| POST | /api/auth/logout | 用户登出 |

### 商品接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/products | 商品列表 |
| GET | /api/products/{id} | 商品详情 |
| GET | /api/categories | 分类列表 |
| GET | /api/products/recommendations | 个性化推荐 |

### AI搜索接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/yolo/search | YOLO视觉搜索 |

### 订单接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/orders | 创建订单 |
| GET | /api/orders | 订单列表 |
| PUT | /api/orders/{id}/pay | 支付订单 |
| PUT | /api/orders/{id}/confirm | 确认收货 |

---

## 核心算法

### YOLO视觉搜索

系统使用YOLO目标检测算法实现视觉搜索功能：

1. 用户上传图片
2. 后端调用YOLO服务识别图片中的物体
3. 通过白名单过滤器筛选电商相关类别
4. 将识别结果映射到商品分类
5. 返回对应分类的商品列表

### 协同过滤推荐

采用基于物品的协同过滤算法（Item-based CF）：

1. 收集用户浏览、购买行为数据
2. 计算物品相似度矩阵
3. 根据用户历史行为推荐相似商品
4. 解决冷门商品曝光问题

---

## 开发指南

### 构建生产版本

```bash
# 后端
cd shop-vault-backend
./mvnw clean package

# 前端
cd shop-vault-frontend
npm run build
```

### 运行测试

```bash
# 后端测试
cd shop-vault-backend
./mvnw test
```

### 代码规范

- 后端遵循阿里巴巴Java开发规范
- 前端遵循Vue官方风格指南
- 使用TypeScript强类型约束

---

## 参考文献

1. 任建新,王一鸣,李鑫,等. 基于Java Web的智慧商城购物系统设计[J]. 信息技术与信息化, 2022
2. Panjaitan B, Nauli S B. Web Based E-Commerce System Development[J]. IJAR, 2024
3. Zhang Z, et al. ZZ-YOLOv11: A Lightweight Vehicle Detection Model[J]. Sensors, 2025
4. 沈剑翘, 朱天唯. 协同过滤推荐算法及其在电子商城中的应用[J]. 电脑与电信, 2020
5. Kevin Santiago Rey Rodriguez, et al. Secure Development Methodology for Full Stack Web Applications[J]. CMC, 2025

---

## 致谢

感谢指导教师的悉心指导，感谢Gemini 3在开发过程中提供的辅助支持。

---

## 许可证

本项目仅供学习交流使用，未经授权不得用于商业用途。
