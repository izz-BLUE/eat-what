# 今天吃啥 - 开发规范

## 项目概述

微信小程序"今天吃啥"，无 AI、低成本、面向消费者的吃饭决策工具。

**核心定位**：推荐"吃什么"，不是"去哪吃"。第一版聚焦菜品/食物推荐，不涉及餐厅、商家、地理位置。

技术栈：微信小程序原生 + TypeScript | Spring Boot 3 + Java 17 | MySQL 8.0

## 代码风格

### Java 后端

- 使用 Spring Boot 3 + Java 17
- 遵循阿里巴巴 Java 开发手册
- 分层架构：Controller → Service → Repository → Entity
- 命名规范：
  - 类名：PascalCase（如 `UserService`）
  - 方法名：camelCase（如 `getUserById`）
  - 常量：UPPER_SNAKE_CASE（如 `MAX_RETRY_COUNT`）
  - 数据库表名：snake_case（如 `eat_records`）
- 异常处理：统一使用 `@RestControllerAdvice` 全局异常处理
- 日志：使用 SLF4J + Logback
- 注释：公共 API 必须有 Javadoc

### 小程序前端

- 使用 TypeScript
- 组件命名：PascalCase（如 `RecommendCard`）
- 页面文件命名：kebab-case（如 `index/index.ts`）
- API 调用统一封装在 `services/` 目录
- 样式使用 rpx 单位，适配不同屏幕

## 目录规范

```
backend-java/
├── src/main/java/com/eatwhat/
│   ├── controller/       # REST 控制器
│   ├── service/          # 业务逻辑
│   │   └── impl/         # 接口实现
│   ├── repository/       # 数据访问（MyBatis Mapper 或 JPA Repository）
│   ├── entity/           # 数据库实体
│   ├── dto/              # 数据传输对象
│   ├── config/           # 配置类
│   ├── exception/        # 自定义异常
│   └── util/             # 工具类

miniprogram/
├── pages/                # 页面
│   ├── index/            # 首页（一键推荐）
│   ├── history/          # 吃过记录
│   ├── vote/             # 饭局投票
│   └── profile/          # 个人中心
├── components/           # 公共组件
├── services/             # API 调用封装
├── utils/                # 工具函数
└── types/                # TypeScript 类型定义
```

## Git 提交规范

提交信息格式：
```
<type>(<scope>): <subject>

<body>

<footer>
```

类型（type）：
- `feat`: 新功能
- `fix`: 修复 bug
- `docs`: 文档变更
- `style`: 代码格式（不影响功能）
- `refactor`: 重构
- `perf`: 性能优化
- `test`: 测试相关
- `chore`: 构建/工具变更

示例：
```
feat(recommend): 添加一键推荐接口
fix(vote): 修复投票计数不准确问题
docs(api): 更新接口文档
```

## 数据库规范

- 表名：snake_case，复数形式（如 `users`, `foods`）
- 字段名：snake_case（如 `created_at`, `user_id`）
- 主键：统一使用 `id`，BIGINT 自增
- 时间字段：`created_at`, `updated_at`，使用 DATETIME
- 软删除：使用 `deleted_at` 字段（NULL 表示未删除）
- 索引：高频查询字段必须建索引
- 核心表：`foods`（菜品）、`eat_records`（吃过记录）

## 用户认证规范

**第一版认证方式**：微信登录 + 后端自定义登录态 token

- 小程序通过 `wx.login()` 获取 code
- 后端用 code 调用微信接口获取 openid
- 后端生成自定义 token 返回给前端
- 前端后续请求通过 `Authorization: Bearer {token}` 传递
- **禁止**前端直接传递 openid 作为可信身份

**token 实现方式**（后续可选）：
- JWT（无状态，推荐第一版使用）
- Redis Session（有状态，适合需要踢人下线场景）

## 接口规范

- RESTful 风格
- 统一响应格式：
```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```
- 错误码：
  - 0: 成功
  - 1xxx: 参数错误
  - 2xxx: 业务错误
  - 5xxx: 系统错误
- 分页参数：`page`（从 1 开始），`pageSize`（默认 20）

## 推荐算法约束

**重要：第一版不接入任何 AI 服务**

推荐算法基于规则打分，流程：
1. 黑名单过滤（直接排除）
2. 今日不想吃过滤（直接排除或强扣分）
3. 最近吃过按天数扣分
4. 餐段、预算、口味、场景加分
5. 从 Top 5 中随机选一个（避免结果固定）

详见 [recommendation.md](./docs/recommendation.md)

## MVP 范围约束

**第一版只做**：
- 一键推荐、换一个、我就吃它
- 吃过记录、黑名单、不想吃
- 饭局投票

**第一版不做**：
- 餐厅/商家信息
- 地图/定位/附近
- 商家后台
- 复杂统计

## 测试要求

- 后端：Service 层必须有单元测试
- 前端：关键组件需要测试
- 接口：使用 Postman 或 curl 测试

## 环境配置

### 开发环境
- JDK 17
- MySQL 8.0
- Maven 3.8+
- 微信开发者工具

### 配置文件
```
backend-java/src/main/resources/
├── application.yml           # 公共配置
├── application-dev.yml       # 开发环境
└── application-prod.yml      # 生产环境
```

## 注意事项

1. 不要引入 AI 相关依赖（OpenAI、LangChain 等）
2. 不要引入地图 SDK（高德、百度等）
3. 第一版不做商家后台
4. 核心业务是"吃什么"，不是"去哪吃"
5. 保持代码简洁，避免过度设计
6. 优先完成功能，优化可以后续迭代
