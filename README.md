# 今天吃啥

> 一个帮你决定吃什么的微信小程序。无 AI、低成本、面向消费者。

## 项目简介

"今天吃啥"是一个吃饭决策小程序，解决"不知道吃什么"的日常难题。

核心功能：
- **一键推荐**：根据用户偏好和历史，推荐今天吃什么
- **换一个**：不满意？换一个推荐
- **我就吃它**：锁定推荐，开始记录
- **吃过记录**：记录吃过的食物和评价
- **黑名单**：再也不想吃的食物
- **不想吃**：临时排除某类食物
- **饭局投票**：多人聚餐，投票决定吃什么

**第一版定位**：菜品/食物推荐，不是餐厅推荐。推荐的是"吃什么"，不是"去哪吃"。

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | 微信小程序原生 + TypeScript |
| 后端 | Spring Boot 3 + Java 17 |
| 数据库 | MySQL 8.0 |

## 目录结构

```
eat-what/
├── miniprogram/          # 微信小程序前端
│   ├── pages/            # 页面
│   ├── components/       # 组件
│   ├── services/         # API 调用
│   ├── utils/            # 工具函数
│   └── app.json          # 小程序配置
├── backend-java/         # Spring Boot 后端
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   └── pom.xml
├── docs/                 # 项目文档
│   ├── product.md        # 产品文档
│   ├── database.md       # 数据库设计
│   ├── recommendation.md # 推荐算法
│   └── api.md            # 接口文档
└── README.md
```

## 快速开始

### 后端

```bash
cd backend-java

# 1. 创建数据库
mysql -u root -p < src/main/resources/schema.sql

# 2. 修改配置
# 编辑 src/main/resources/application.yml
# 配置数据库连接信息

# 3. 启动
./mvnw spring-boot:run
```

### 前端

```bash
cd miniprogram

# 1. 安装依赖（如果有）
npm install

# 2. 使用微信开发者工具打开 miniprogram 目录

# 3. 修改 API 地址
# 编辑 services/api.ts，配置后端地址
```

## 开发规范

详见 [CLAUDE.md](./CLAUDE.md)

## 文档

- [产品文档](./docs/product.md) - 产品功能和用户故事
- [数据库设计](./docs/database.md) - 表结构和字段说明
- [推荐算法](./docs/recommendation.md) - 规则打分算法设计
- [接口文档](./docs/api.md) - RESTful API 定义

## 版本规划

### v1.0（MVP）
- 一键推荐
- 换一个
- 我就吃它
- 吃过记录
- 黑名单
- 不想吃
- 饭局投票

### v1.1（未来）
- 位置距离筛选（附近餐厅）
- 餐厅/商家信息
- 朋友推荐

### v2.0（远期）
- 商家入驻
- 优惠券
- 社交分享

## License

MIT
