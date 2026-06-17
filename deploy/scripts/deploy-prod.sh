#!/usr/bin/env bash
# ============================================================
# 今天吃啥 — 一键部署脚本
# ============================================================
# 用法：
#   bash deploy/scripts/deploy-prod.sh
#
# 要求：
#   - 在项目根目录执行
#   - deploy/.env 已配置真实密钥
#   - Docker 已安装且当前用户在 docker 组
# ============================================================

set -euo pipefail

# ---- 颜色 ----
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

COMPOSE_FILE="deploy/docker-compose.prod.yml"
ENV_FILE="deploy/.env"
ENV_EXAMPLE="deploy/.env.example"

# ---- 1. 项目根目录检查 ----
echo "========================================"
echo "  今天吃啥 — 生产部署"
echo "========================================"
echo ""

if [ ! -f "$COMPOSE_FILE" ]; then
  echo -e "${RED}[ERROR]${NC} 请在项目根目录下执行本脚本"
  echo ""
  echo "  当前目录: $(pwd)"
  echo "  预期:     eat-what/"
  echo ""
  exit 1
fi
echo -e "${GREEN}[OK]${NC} 当前在项目根目录: $(pwd)"

# ---- 2. .env 检查 ----
if [ ! -f "$ENV_FILE" ]; then
  echo -e "${RED}[ERROR]${NC} deploy/.env 不存在"
  echo ""
  echo "  请先创建生产环境变量文件："
  echo "    cp ${ENV_EXAMPLE} ${ENV_FILE}"
  echo "    # 编辑 deploy/.env 填入真实密钥"
  echo "    nano ${ENV_FILE}"
  echo ""
  exit 1
fi
echo -e "${GREEN}[OK]${NC} deploy/.env 已就绪"

# ---- 3. 拉取最新代码 ----
echo ""
echo ">>> 拉取最新代码 ..."
git pull --ff-only
echo -e "${GREEN}[OK]${NC} 代码已更新"

# ---- 4. 构建后端镜像 ----
echo ""
echo ">>> 构建后端镜像 ..."
docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" build backend
echo -e "${GREEN}[OK]${NC} 后端镜像构建完成"

# ---- 5. 启动服务 ----
echo ""
echo ">>> 启动服务 ..."
docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d
echo -e "${GREEN}[OK]${NC} 服务已启动"

# ---- 6. 显示状态 ----
echo ""
echo ">>> 容器状态 ..."
sleep 2
docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" ps
echo ""

echo -e "${GREEN}========================================"
echo "  部署完成"
echo "========================================"
echo ""
echo "  运行健康检查："
echo "    bash deploy/scripts/check-prod.sh"
echo ""
echo "  查看日志："
echo "    docker compose -f deploy/docker-compose.prod.yml logs -f"
echo -e "${NC}"
