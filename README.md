# 小铺宝库(ShopVault) (ShopVault Smart E-commerce System)

> 基于 **Spring Boot 3.5** + **Vue.js 3.5** + **YOLOv26** 的智能电商平台

## 项目概述

小铺宝库(ShopVault)是一个融合**AI视觉搜索**、**协同过滤推荐**和**VIP会员营销体系**的创新型智能电子商务系统。系统采用前后端分离架构，后端基于Spring Boot生态，前端采用Vue.js 3 Composition API，AI服务通过Python Flask独立部署。

## 技术栈

| 层面    | 技术                             | 版本        |
| ----- | ------------------------------ | --------- |
| 后端框架  | Spring Boot                    | 3.5.12    |
| 安全认证  | Spring Security + JWT (双Token) | -         |
| ORM框架 | MyBatis Plus                   | 3.5.16    |
| 缓存/消息 | Redis                          | 7.2+      |
| 前端框架  | Vue.js + TypeScript            | 3.5 / 5.9 |
| 构建工具  | Vite                           | 8.0       |
| UI组件库 | Element Plus                   | 2.13      |
| 状态管理  | Pinia                          | 3.0       |
| 图表库   | ECharts                        | 5.5       |
| 数据库   | MySQL                          | 8.0       |
| AI服务  | YOLOv26 (Ultralytics) + Flask  | -         |

## 项目规模统计

| 指标            | 数量        | 说明                                        |
| ------------- | --------- | ----------------------------------------- |
| Java源文件总数     | **240**   | 含Controller/Service/Entity/Mapper/Config等 |
| Controller控制器 | **37**    | 覆盖9大功能模块                                  |
| RESTful API端点 | **\~232** | 含GET/POST/PUT/DELETE                      |
| Service服务类    | **69**    | 接口+实现(含impl子包)                            |
| Entity实体类     | **31**    | 对应31张DB表                                  |
| Mapper数据访问层   | **31**    | MyBatis Plus BaseMapper扩展                 |
| DTO数据传输对象     | **28**    | 前后端交互数据结构                                 |
| 数据库表          | **31**    | oms\_(5) + pms\_(9) + sms\_(9) + sys\_(8) |
| Vue组件         | **59**    | 单文件组件(.vue)                               |
| TS/JS文件       | **5554**  | 含路由/API/Store/工具函数                        |

## 功能模块架构

### 1. 用户服务模块 (3 Controllers)

- `AuthController` - 注册/登录/JWT Token签发与刷新
- `UserController` - 用户信息管理/头像修改/密码变更
- `OnboardingController` - 新手引导/偏好采集/个性化初始化
- **核心技术**: JwtUtils双Token(AccessToken+RefreshToken) + SecurityConfig过滤器链 + UserDetailsServiceImpl

### 2. AI视觉导购模块 (1 Controller + 1 Integration Service)

- `YoloMappingController` - 上传图片→YOLO识别→商品匹配
- `YoloClientService` - HTTP客户端封装，调用Flask-YOLO微服务
- `sys_yolo_mapping` 表 - COCO 80类标签→业务商品分类映射
- **特色**: 支持HEIC格式(通过pillow-heif自动转换), yolo26x.pt模型

### 3. 智能推荐模块 (1 Controller + 1 Manager Engine)

- `RecommendationController` - 首页推荐/猜你喜欢/相关推荐
- `RecommendationEngine` - Item-Based CF算法实现
- **算法**: 共现矩阵 + 行为权重(浏览1:收藏3:加购4:购买5) + Redis结果缓存
- **数据来源**: sys\_user\_behavior(行为记录) + sys\_user\_preference(偏好标签)

### 4. 统一交易模块 (4 Controllers)

- `UnifiedOrderController` - **核心创新**: order\_type统一4种订单
  - type=0: 普通订单 | type=1: VIP专享价订单
  - type=2: SVIP专享价订单 | type=3: 积分兑换订单
- `OrderController` - 订单列表/详情/确认收货/取消
- `CartItemController` - 购物车增删改查/批量操作
- `AfterSalesController` - 售后申请/审核/退款/退货

### 5. 会员营销模块 (5 Controllers)

- `VipController` - VIP开通/续费/权益查看
- `VipConstants` - 定价常量定义:
  - VIP月卡: ¥99 | VIP年卡: ¥999 | SVIP年卡: ¥1499
- `CouponController` - 优惠券领取/核销(16个端点)
- `PointsMallController` - 积分商城/兑换/记录
- `ActivityController` + `MemberDayController` - 促销活动/会员日特权

### 6. 后台管理模块 (8 Controllers)

- `AdminController` - 数据仪表盘/用户管理
- `AdminVipController` - VIP审核/权益配置
- `AdminOrderController` - 订单管理/发货
- `AdminAfterSalesController` - 售后处理
- `AdminActivityController` - 活动创建/编辑
- `AdminPointsProductController` - 积分商品管理
- `AdminPointsRuleController` - 积分规则配置
- `AdminMessageController` - 站内信群发

### 7. 商品管理模块 (7 Controllers)

- `ProductController` - 商品CRUD/上下架/搜索
- `CategoryController` - 多级分类树管理
- `ProductSkuController` - SKU库存/价格管理(11个端点)
- `ProductSpecController` - 商品规格组管理(10个端点)
- `SpecController` - 全局规格名/值管理
- `CommentController` - 评论发布/回复/删除
- `FavoriteController` - 收藏夹管理

### 8. 消息通信模块 (3 Controllers)

- `ChatMessageController` - WebSocket实时聊天
- `MessagePushController` - 系统消息推送(站内信)
- `WebSocketStatusController` - 在线状态查询
- **支撑**: WebSocketConfig + WebSocketService + WebSocketAuthChannelInterceptor

### 9. 基础支撑模块 (3 Controllers)

- `UploadController` - 文件/图片上传(本地存储)
- `AddressController` - 收货地址CRUD/默认地址设置
- `DashboardController` - 个人中心统计数据

## 数据库设计 (31张表)

```
shop-vault-backend/shop-vault.sql
├── oms_*  (5张) - 订单交易域
│   ├── oms_order          -- 订单主表(含order_type字段)
│   ├── oms_order_item     -- 订单商品明细
│   ├── oms_cart_item      -- 购物车
│   ├── oms_after_sales    -- 售后申请
│   └── oms_payment_record -- 支付记录
├── pms_*  (9张) - 商品管理域
│   ├── pms_product        -- 商品SPU
│   ├── pms_category       -- 商品分类
│   ├── pms_product_sku    -- 商品SKU
│   ├── pms_spec / pms_spec_value           -- 全局规格
│   └── pms_product_spec / pms_product_spec_value -- 商品规格绑定
├── sms_*  (9张) - 营销会员域
│   ├── sms_vip_membership / sms_user_vip_info -- VIP会员
│   ├── sms_coupon_template / sms_user_coupon  -- 优惠券
│   ├── sms_points_product / sms_points_record  -- 积分
│   ├── sms_points_rule                        -- 积分规则
│   ├── sms_activity                            -- 促销活动
│   └── sms_balance_record                      -- 余额变动
└── sys_*  (8张) - 系统基础域
    ├── sys_user             -- 用户账户
    ├── sys_address          -- 收货地址
    ├── sys_user_behavior    -- 行为轨迹(推荐用)
    ├── sys_user_preference  -- 用户偏好
    ├── sys_yolo_mapping     -- YOLO标签映射
    ├── sys_chat_message     -- 聊天记录
    ├── sys_message_push     -- 推送消息
    └── sys_log              -- 操作日志
```

## 项目结构

```
shop-vault-project/
├── shop-vault-backend/          # Spring Boot后端
│   └── src/main/java/com/TsukasaChan/ShopVault/
│       ├── controller/          # 37个Controller (9个子包)
│       │   ├── admin/          # 后台管理(8个)
│       │   ├── marketing/      # 营销会员(5个)
│       │   ├── order/          # 交易订单(4个)
│       │   ├── product/        # 商品管理(7个)
│       │   └── system/         # 系统服务(9个含Base)
│       ├── service/            # 69个Service (接口+impl)
│       ├── manager/            # 核心业务引擎(RecommendationEngine等)
│       ├── integration/        # 外部服务集成(YoloClientService)
│       ├── security/           # 安全认证(JwtUtils/SecurityConfig)
│       ├── common/             # 公共组件(VipConstants/Result/PageResult等)
│       ├── config/             # 配置类(Redis/WebSocket/CORS等)
│       ├── entity/             # 31个实体类
│       ├── mapper/             # 31个Mapper接口
│       ├── dto/                # 28个数据传输对象
│       ├── task/               # 定时任务(订单超时/积分过期/库存预警)
│       ├── websocket/          # WebSocket(5个类)
│       └── annotation/         # 自定义注解(LogOperation)
├── shop-vault-frontend/        # Vue.js前端 (59个组件)
├── shop-vault-yolo/            # Flask YOLO AI服务
│   ├── yolo_api.py             # Flask应用主文件
│   └── ultralytics/            # YOLOv26依赖
├── documents/                  # 项目文档
│   ├── generates/              # 文档生成脚本(Python)
│   └── output/                 # 输出文档(Word)
└── README.md                   # 本文件
```

## 核心创新点

1. **统一订单系统**: 通过`order_type`一字段实现4种订单类型的统一处理，避免代码重复
2. **YOLO标签映射表**: `sys_yolo_mapping`桥接COCO通用标签与电商细分品类，支持运营动态调整
3. **行为权重推荐**: 浏览(1):收藏(3):加购(4):购买(5)的五级权重体系，更精准反映用户意图
4. **JWT双Token机制**: AccessToken(短期访问) + RefreshToken(长期刷新)平衡安全性与用户体验
5. **VIP三级体系**: VipConstants集中管理定价规则，支持月卡/年卡/SVIP多档位

## 快速启动

### 后端

```bash
cd shop-vault-backend
# 配置 application.yml 中的 MySQL/Redis 连接信息
mvn spring-boot:run
# 访问 http://localhost:8080
```

### 前端

```bash
cd shop-vault-frontend
npm install
npm run dev
# 访问 http://localhost:5173
```

### AI/YOLO服务

```bash
cd shop-vault-yolo
pip install -r requirements.txt
python yolo_api.py
# Flask服务运行于 http://localhost:5000/detect
```

<br />

## 许可证

本项目仅用于毕业设计（论文）教学目的。
