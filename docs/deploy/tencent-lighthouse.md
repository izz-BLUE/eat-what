# 腾讯云轻量应用服务器 — 部署指南

> 适用版本：v0.1.x+
> 目标：Docker Compose + Nginx HTTPS，零公网端口暴露（除 80/443）

---

## 一、服务器选型建议

| 项 | 推荐 |
|---|------|
| 类型 | 腾讯云轻量应用服务器 |
| 地域 | 中国大陆（备案要求） |
| 镜像 | Ubuntu 22.04 LTS |
| CPU | 2 核 |
| 内存 | 2 GB（最低），4 GB 推荐 |
| 带宽 | 3-5 Mbps |
| 系统盘 | 50 GB SSD |

小程序后端对算力要求极低，2C2G 足够支撑数千 DAU。

---

## 二、防火墙 / 安全组

在腾讯云控制台 → 轻量应用服务器 → 防火墙中，开放以下端口：

| 端口 | 协议 | 用途 |
|------|------|------|
| 22 | TCP | SSH 管理 |
| 80 | TCP | HTTP（ACME 证书验证 + 重定向到 HTTPS） |
| 443 | TCP | HTTPS API 服务 |

**不要开放 3306（MySQL）和 8080（后端）**。这些都走 Docker 内部网络。

---

## 三、环境准备

### 3.1 SSH 登录后安装 Docker

```bash
# 安装 Docker（官方脚本）
curl -fsSL https://get.docker.com | bash

# 将当前用户加入 docker 组（免 sudo）
sudo usermod -aG docker $USER

# 重新登录使权限生效
exit
ssh user@your-server-ip
```

**网络要求**：服务器需要能访问 Docker Hub（`docker.io`）拉取基础镜像。如果遇到拉取缓慢或超时，可配置腾讯云 Docker 镜像加速：

```bash
# 编辑 Docker 配置
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<'EOF'
{
  "registry-mirrors": ["https://mirror.ccs.tencentyun.com"]
}
EOF
sudo systemctl daemon-reload
sudo systemctl restart docker
```

### 3.2 验证安装

```bash
docker --version
# 预期：Docker version 24.x.x 或更高

docker compose version
# 预期：Docker Compose version v2.x.x
```

---

## 四、代码部署

### 4.1 上传代码

**方式 A：git clone（推荐）**

```bash
git clone https://github.com/your-org/eat-what.git
cd eat-what
```

**方式 B：压缩包上传**

```bash
# 本地打包（排除 node_modules, target 等）
cd eat-what
tar -czf eat-what.tar.gz \
    --exclude='node_modules' \
    --exclude='backend-java/target' \
    --exclude='.git' \
    .

# 上传到服务器
scp eat-what.tar.gz user@your-server-ip:~/
# 在服务器上解压
ssh user@your-server-ip
tar -xzf eat-what.tar.gz -C eat-what
```

### 4.2 切换到目标分支/版本

```bash
cd eat-what
git checkout main           # 或指定版本 tag
git pull origin main
```

---

## 五、配置文件

### 5.1 创建 .env

```bash
cp deploy/.env.example deploy/.env
nano deploy/.env
```

填写真实值：

```ini
SPRING_PROFILES_ACTIVE=prod

WECHAT_APPID=wx_your_real_appid
WECHAT_SECRET=your_real_secret
WECHAT_MOCK_ENABLED=false

JWT_SECRET=<openssl rand -base64 48 的输出>
ADMIN_TOKEN=<openssl rand -base64 24 的输出>

DB_HOST=mysql
DB_PORT=3306
DB_NAME=eat_what
DB_USER=eatwhat
DB_PASSWORD=<强随机密码>
MYSQL_ROOT_PASSWORD=<强随机密码>
```

**重要**：
- `JWT_SECRET` 至少 32 字节，泄露后可以伪造任意用户 token
- `ADMIN_TOKEN` 用于管理后台 API 认证
- 不要使用示例/默认密码

### 5.2 替换 Nginx 域名

编辑 `deploy/nginx/eat-what.conf`，将 `api.example.com` 替换为你的真实 API 域名：

```bash
sed -i 's/api.example.com/api.your-domain.com/g' deploy/nginx/eat-what.conf
```

---

## 六、HTTPS 证书

### 6.1 使用 acme.sh（推荐，免费）

```bash
# 安装 acme.sh
curl https://get.acme.sh | sh
source ~/.bashrc

# 签发证书（HTTP 验证方式，需要 80 端口已开放）
acme.sh --issue -d api.your-domain.com -w /path/to/eat-what/deploy/nginx/acme

# 安装证书到 Nginx 证书目录
mkdir -p deploy/nginx/certs
acme.sh --install-cert -d api.your-domain.com \
  --key-file       deploy/nginx/certs/privkey.pem \
  --fullchain-file deploy/nginx/certs/fullchain.pem \
  --reloadcmd      "docker exec eat-what-nginx-prod nginx -s reload"
```

证书每 60 天自动续期（acme.sh 默认定时任务）。

### 6.2 使用 certbot（备选）

```bash
sudo apt install certbot
sudo certbot certonly --webroot -w deploy/nginx/acme -d api.your-domain.com
```

### 6.3 证书目录结构

```
deploy/nginx/certs/
├── fullchain.pem    # 完整证书链
└── privkey.pem      # 私钥（600 权限）
```

---

## 七、启动服务

```bash
cd eat-what

# 构建并启动
docker compose -f deploy/docker-compose.prod.yml --env-file deploy/.env up -d --build

# 查看启动日志
docker compose -f deploy/docker-compose.prod.yml logs -f

# 查看各容器状态
docker compose -f deploy/docker-compose.prod.yml ps
```

预期输出：三个容器均为 `Up` 状态（healthy）。

### 内存用量估算（4GB 单机）

| 组件 | 容器限制 | 预期实际 |
|------|---------|---------|
| MySQL | 1 GB | ~400MB（含 buffer pool 256MB） |
| Backend JVM | 1 GB | ~850MB（堆 768MB + Metaspace ~80MB） |
| Nginx | 128 MB | ~20MB |
| OS + 缓冲 | ~1.5 GB | ~1.5GB |
| **合计** | **~3.6 GB** | **~2.8GB** |

4GB 服务器运行绰绰有余。2GB 服务器紧张但也可用（需调小 buffer pool 和堆）。

### 启动后验证

```bash
# 健康检查（替换为真实域名）
curl https://api.your-domain.com/api/health
# 预期：{"code":0,"data":{"status":"UP"}}

# 匿名推荐
curl "https://api.your-domain.com/api/v1/recommend"
# 预期：返回一道菜品
```

---

## 八、数据验证

Flyway 迁移由后端启动时自动执行。手动验证：

```bash
# 进入 MySQL 容器
docker exec -it eat-what-mysql-prod mysql -u eatwhat -p eat_what

# 查看 Flyway 迁移记录
SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;

# 验证菜品数据
SELECT COUNT(*) AS total, SUM(enabled=1) AS enabled FROM foods;

# 验证无重复菜品名
SELECT name, COUNT(*) c FROM foods GROUP BY name HAVING c > 1;

# 验证唯一索引
SHOW INDEX FROM foods WHERE Key_name = 'uk_foods_name';
```

预期：
- V1–V14 全部 `success=1`
- `total=202, enabled=202`
- 无重复 name
- `uk_foods_name` 存在，`Non_unique=0`

---

## 九、日志查看

```bash
# 所有服务日志
docker compose -f deploy/docker-compose.prod.yml logs -f

# 仅后端
docker compose -f deploy/docker-compose.prod.yml logs -f backend

# 仅 Nginx
docker compose -f deploy/docker-compose.prod.yml logs -f nginx

# 最近 100 行
docker compose -f deploy/docker-compose.prod.yml logs --tail=100 backend

# 日志已配置 max-size=10m / max-file=3，不会撑满磁盘
```

---

## 十、小程序配置

### 10.1 request 合法域名

在微信小程序后台 → 开发管理 → 开发设置 → 服务器域名中配置：

| 类型 | 域名 |
|------|------|
| request 合法域名 | `https://api.your-domain.com` |

### 10.2 前端 baseUrl

修改 `miniprogram/config/index.ts`：

```typescript
const PRODUCTION_BASE_URL = 'https://api.your-domain.com'  // 替换为备案域名
```

正式版自动使用 `PRODUCTION_BASE_URL`，开发版/体验版自动使用 `http://localhost:8080`。无需手动切换开关。

---

## 十一、回滚

```bash
# 回滚到指定版本
git checkout v0.1.1

# 重新构建并启动
docker compose -f deploy/docker-compose.prod.yml --env-file deploy/.env up -d --build

# 如果数据库需要回滚（慎用！）
# Flyway 不支持自动回滚，需要手动执行反向 SQL
# 建议数据库只前进不后退，代码回滚即可
```

**数据库回滚警告**：Flyway 不支持自动 undo。如果新版本 migration 有问题：
1. 先回滚代码
2. 手动执行 `DELETE FROM flyway_schema_history WHERE version > X;`
3. 手动执行反向 DDL（如 `DROP TABLE`）
4. **不要在生产环境直接操作，先在测试环境验证**

---

## 十二、常见故障排查

### 后端启动失败

```bash
# 查看后端日志
docker logs eat-what-backend-prod

# 检查 MySQL 是否 healthy
docker ps --filter name=mysql

# 检查环境变量是否正确注入
docker exec eat-what-backend-prod env | grep -E "SPRING|DB_|JWT"
```

**常见原因**：
- MySQL 未就绪（等待 health check 通过）
- 数据库密码不匹配
- Flyway migration 冲突（检查 `flyway_schema_history` 表）
- JWT_SECRET 长度不足 32 字节

### Nginx 502 Bad Gateway

```bash
# 检查后端是否运行
docker ps --filter name=backend

# 检查 Nginx 能否解析 backend 域名
docker exec eat-what-nginx-prod ping -c 1 backend

# 检查后端 8080 是否可达
docker exec eat-what-nginx-prod wget -qO- http://backend:8080/api/health
```

### 证书错误

```bash
# 验证证书文件存在且正确
docker exec eat-what-nginx-prod nginx -t

# 检查证书有效期
openssl x509 -in deploy/nginx/certs/fullchain.pem -noout -dates

# 重新加载 Nginx 配置
docker exec eat-what-nginx-prod nginx -s reload
```

### 磁盘空间不足

```bash
# 查看 Docker 磁盘占用
docker system df

# 清理未使用的镜像和容器
docker system prune -a
```

---

## 十三、重要提醒

1. **备案**：域名未完成 ICP 备案前，小程序无法提交正式审核。备案周期约 15-20 个工作日，请提前准备。
2. **备案通过前不要提审**：微信小程序要求 `request 合法域名` 已完成备案，否则审核会被拒。
3. **证书续期**：acme.sh 默认自动续期，部署后确认 crontab 中有续期任务（`crontab -l`）。
4. **数据库备份**：建议配置腾讯云自动快照，或手动 `mysqldump` 定期备份。
5. **密钥轮换**：`JWT_SECRET` 和 `ADMIN_TOKEN` 泄露后立即更换，旧 token 会立即失效。
6. **监控**：建议接入腾讯云云监控，设置 CPU/内存/磁盘告警。
