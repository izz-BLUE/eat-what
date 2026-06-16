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
- **意见反馈**：提交使用建议或问题反馈，无需登录即可使用

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

### 环境要求

- JDK 17+
- Docker Desktop（含 WSL2）
- Maven 3.8+（或使用项目自带的 mvnw）

### 后端

```bash
# 1. 启动 MySQL（Docker）
docker compose up -d

# 2. 设置环境变量
$env:SPRING_PROFILES_ACTIVE="dev"
$env:JWT_SECRET="eat-what-dev-jwt-secret-at-least-32-bytes"
$env:WECHAT_MOCK_ENABLED="true"

# 3. 运行测试（不需要 MySQL）
cd backend-java
.\mvnw.cmd clean test

# 4. 启动后端（需要 MySQL）
.\mvnw.cmd spring-boot:run

# 5. 验证服务
# 浏览器访问：http://localhost:8080/api/health

# PowerShell 验证（推荐，中文显示正常）：
Invoke-RestMethod http://localhost:8080/api/health | ConvertTo-Json -Depth 8

# 微信登录获取 token
$loginResult = Invoke-RestMethod -Method POST -Uri "http://localhost:8080/api/v1/user/login" `
  -ContentType "application/json; charset=utf-8" `
  -Body ([System.Text.Encoding]::UTF8.GetBytes('{"code":"dev-user-1"}'))
$token = $loginResult.data.token

# 查询菜品
Invoke-RestMethod http://localhost:8080/api/v1/foods | ConvertTo-Json -Depth 8

# 一键推荐（无 token，基础推荐）
Invoke-RestMethod "http://localhost:8080/api/v1/recommend?mealType=晚餐" | ConvertTo-Json -Depth 8

# 一键推荐（有 token，个性化推荐）
Invoke-RestMethod "http://localhost:8080/api/v1/recommend?mealType=晚餐" `
  -Headers @{Authorization="Bearer $token"} | ConvertTo-Json -Depth 8
```

**环境变量说明**：
- `SPRING_PROFILES_ACTIVE`：激活 dev Profile（启用微信 Mock）
- `JWT_SECRET`：JWT 签名密钥，至少 32 字节
- `WECHAT_MOCK_ENABLED`：启用微信 Mock 模式（仅 dev/test 生效）

**数据库说明**：
- 使用 Docker MySQL 8.0，容器名：eat-what-mysql
- 数据库名：eat_what，用户名：eatwhat，密码：eatwhat_dev
- 使用 Flyway 管理数据库脚本
- 首次启动会自动创建表结构和初始化 30 种食物测试数据
- 迁移脚本位于 `src/main/resources/db/migration/`
- 请勿手动修改数据库结构，所有变更通过 Flyway 脚本管理

**Docker 常用命令**：
```bash
docker compose up -d          # 启动 MySQL
docker compose down            # 停止 MySQL
docker compose logs -f mysql   # 查看 MySQL 日志
docker compose down -v         # 停止并删除数据卷（清空数据）

# 如果遇到中文乱码问题，重建数据卷：
docker compose down -v         # 删除数据卷
docker volume rm eat-what_mysql_data  # 确保删除
docker compose up -d           # 重新启动
cd backend-java
.\mvnw.cmd flyway:clean       # 清除 Flyway 记录（需要配置 flyway.clean-disabled=false）
# 或者直接重建整个环境：
docker compose down -v && docker compose up -d
```

### 前端（微信小程序）

```bash
# 1. 使用微信开发者工具打开 miniprogram 目录

# 2. 本地调试设置
#    - 详情 → 本地设置 → 勾选"不校验合法域名"
#    - 这样可以访问 http://localhost:8080

# 3. 确认后端已启动
#    - API 地址配置在 miniprogram/config/index.ts
#    - 开发环境默认 http://localhost:8080

# 4. 编译运行
#    - 点击"编译"按钮即可预览
```

**小程序功能**：
- 首页：选择餐段/价格/口味/分类，点击"今天吃啥"获取推荐
- 换一个：最多连续换 5 次
- 我就吃它：记录用餐信息（需登录）
- 历史记录：查看吃过记录
- 黑名单管理：加入/移出黑名单
- 不想吃管理：临时限制分类
- 登录：微信一键登录
- **意见反馈**：提交使用建议或问题反馈，无需登录即可使用
- **筛选偏好记忆**：首页会记住上次筛选条件（餐段、预算、口味、分类）
- **一键重置**：筛选区域提供"重置筛选条件"，恢复首次使用默认值
- 偏好使用小程序本地存储（`wx.setStorageSync`），不跨设备同步

**本地调试说明**：
- 微信开发者工具需开启"不校验合法域名"才能访问 localhost
- 后端需使用 dev Profile 启用微信 Mock
- Mock 模式下 wx.login 会返回稳定的测试 openid

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
