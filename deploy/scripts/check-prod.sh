#!/usr/bin/env bash
# ============================================================
# 今天吃啥 — 上线后健康检查脚本
# ============================================================
# 用法：
#   bash deploy/scripts/check-prod.sh
#
# 检查项：
#   1. 容器状态
#   2. 后端健康接口
#   3. Flyway 迁移记录
#   4. foods 数据完整性
#   5. Nginx 配置
#   6. 系统资源
# ============================================================

set -euo pipefail

# ---- 颜色 ----
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

COMPOSE_FILE="deploy/docker-compose.prod.yml"
ENV_FILE="deploy/.env"

PASS=0
FAIL=0
WARN=0

_pass() {
  echo -e "  ${GREEN}[PASS]${NC} $1"
  PASS=$((PASS + 1))
}

_fail() {
  echo -e "  ${RED}[FAIL]${NC} $1"
  FAIL=$((FAIL + 1))
}

_warn() {
  echo -e "  ${YELLOW}[WARN]${NC} $1"
  WARN=$((WARN + 1))
}

# ---- 前置检查 ----
echo "========================================"
echo "  今天吃啥 — 健康检查"
echo "========================================"
echo ""

if [ ! -f "$COMPOSE_FILE" ]; then
  echo -e "${RED}[ERROR]${NC} 请在项目根目录下执行本脚本"
  exit 1
fi

if [ ! -f "$ENV_FILE" ]; then
  echo -e "${RED}[ERROR]${NC} deploy/.env 不存在，无法连接数据库"
  exit 1
fi

# 读取 MySQL 连接凭据（兼容多种变量名）
DB_USER=$(grep -E '^DB_USER=' "$ENV_FILE" | cut -d'=' -f2-)
if [ -z "$DB_USER" ]; then
  DB_USER=$(grep -E '^SPRING_DATASOURCE_USERNAME=' "$ENV_FILE" | cut -d'=' -f2-)
fi
DB_USER="${DB_USER:-eatwhat}"

DB_PASSWORD=$(grep -E '^DB_PASSWORD=' "$ENV_FILE" | cut -d'=' -f2-)
if [ -z "$DB_PASSWORD" ]; then
  DB_PASSWORD=$(grep -E '^SPRING_DATASOURCE_PASSWORD=' "$ENV_FILE" | cut -d'=' -f2-)
fi
if [ -z "$DB_PASSWORD" ]; then
  echo -e "${RED}[FAIL]${NC} 缺少数据库密码：deploy/.env 中 DB_PASSWORD 和 SPRING_DATASOURCE_PASSWORD 均为空"
  exit 1
fi

# ============================================================
# 1. 容器状态
# ============================================================
echo "--- 1. 容器状态 ---"

# 获取运行中容器
RUNNING=$(docker compose -f "$COMPOSE_FILE" ps --status running -q 2>/dev/null || true)
TOTAL=$(docker compose -f "$COMPOSE_FILE" ps -q 2>/dev/null || true)

if [ -z "$TOTAL" ]; then
  _fail "没有容器在运行，请先执行 deploy-prod.sh"
  echo ""
  echo -e "${RED}健康检查终止 — 容器未运行${NC}"
  exit 1
fi

RUNNING_COUNT=$(echo "$RUNNING" | grep -c . || true)
TOTAL_COUNT=$(echo "$TOTAL" | grep -c . || true)

if [ "$RUNNING_COUNT" -eq "$TOTAL_COUNT" ] && [ "$TOTAL_COUNT" -eq 3 ]; then
  _pass "3/3 容器运行中"
else
  _fail "容器状态异常：${RUNNING_COUNT}/${TOTAL_COUNT} 运行中（预期 3/3）"
fi

docker compose -f "$COMPOSE_FILE" ps 2>/dev/null || true
echo ""

# ============================================================
# 2. 后端健康检查
# ============================================================
echo "--- 2. 后端健康检查 ---"

if docker compose -f "$COMPOSE_FILE" ps --status running backend 2>/dev/null | grep -q backend; then
  HEALTH=$(docker compose -f "$COMPOSE_FILE" exec -T backend wget -qO- http://localhost:8080/api/health 2>&1 || true)
  if echo "$HEALTH" | grep -q '"status":"UP"'; then
    _pass "后端健康接口 UP"
  else
    _fail "后端健康接口异常: ${HEALTH:-无响应}"
  fi
else
  _fail "后端容器未运行"
fi
echo ""

# ============================================================
# 3. Flyway 迁移记录
# ============================================================
echo "--- 3. Flyway 迁移 ---"

run_mysql() {
  docker compose -f "$COMPOSE_FILE" exec -T mysql \
    mysql -u "${DB_USER}" -p"${DB_PASSWORD}" eat_what -N -B -e "$1" 2>&1 || true
}

if docker compose -f "$COMPOSE_FILE" ps --status running mysql 2>/dev/null | grep -q mysql; then
  # 检查所有 migration 是否成功
  FAILED_MIGRATIONS=$(run_mysql \
    "SELECT COUNT(*) FROM flyway_schema_history WHERE success=0;" 2>/dev/null || echo "ERROR")

  if [ "$FAILED_MIGRATIONS" = "0" ]; then
    _pass "所有 Flyway 迁移 success=1"
  elif [ "$FAILED_MIGRATIONS" = "ERROR" ]; then
    _fail "无法查询 flyway_schema_history（表可能不存在）"
  else
    _fail "$FAILED_MIGRATIONS 条 Flyway 迁移失败"
  fi

  # 检查最新版本
  LATEST_VERSION=$(run_mysql \
    "SELECT version FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 1;" 2>/dev/null || echo "N/A")

  EXPECTED_VERSION="14"
  if [ "$LATEST_VERSION" = "$EXPECTED_VERSION" ]; then
    _pass "Flyway 最新版本 = ${EXPECTED_VERSION}"
  elif [ "$LATEST_VERSION" = "N/A" ]; then
    _fail "无法获取 Flyway 版本"
  else
    _fail "Flyway 最新版本 = ${LATEST_VERSION}（预期 ${EXPECTED_VERSION}）"
  fi
else
  _fail "MySQL 容器未运行，跳过 Flyway 检查"
fi
echo ""

# ============================================================
# 4. foods 数据完整性
# ============================================================
echo "--- 4. foods 数据 ---"

if docker compose -f "$COMPOSE_FILE" ps --status running mysql 2>/dev/null | grep -q mysql; then
  # 菜品总数
  FOODS_COUNT=$(run_mysql \
    "SELECT COUNT(*) AS total FROM foods WHERE enabled=1;" 2>/dev/null || echo "0")

  EXPECTED_FOODS="202"
  if [ "$FOODS_COUNT" = "$EXPECTED_FOODS" ]; then
    _pass "foods 总数 = ${EXPECTED_FOODS}"
  elif [ "$FOODS_COUNT" = "0" ]; then
    _fail "foods 表为空或无法查询"
  else
    _fail "foods 总数 = ${FOODS_COUNT}（预期 ${EXPECTED_FOODS}）"
  fi

  # enabled 数
  ENABLED_COUNT=$(run_mysql \
    "SELECT COUNT(*) FROM foods WHERE enabled=1;" 2>/dev/null || echo "0")
  if [ "$ENABLED_COUNT" = "$EXPECTED_FOODS" ]; then
    _pass "foods enabled=1 共 ${EXPECTED_FOODS} 道"
  elif [ "$ENABLED_COUNT" != "$FOODS_COUNT" ]; then
    _warn "foods 总数=${FOODS_COUNT}, enabled=${ENABLED_COUNT}（部分未启用）"
  fi

  # 重复菜名
  DUPLICATES=$(run_mysql \
    "SELECT COUNT(*) FROM (SELECT name, COUNT(*) c FROM foods GROUP BY name HAVING c > 1) AS dup;" 2>/dev/null || echo "ERROR")

  if [ "$DUPLICATES" = "0" ]; then
    _pass "无重复菜名"
  elif [ "$DUPLICATES" = "ERROR" ]; then
    _fail "无法检查重复菜名"
  else
    _fail "${DUPLICATES} 个重复菜名（预期 0）"
  fi
else
  _fail "MySQL 容器未运行，跳过 foods 检查"
fi
echo ""

# ============================================================
# 5. Nginx 配置
# ============================================================
echo "--- 5. Nginx ---"

if docker compose -f "$COMPOSE_FILE" ps --status running nginx 2>/dev/null | grep -q nginx; then
  if docker compose -f "$COMPOSE_FILE" exec -T nginx nginx -t 2>&1 | grep -q "successful"; then
    _pass "Nginx 配置语法正确"
  else
    _fail "Nginx 配置测试失败"
    docker compose -f "$COMPOSE_FILE" exec -T nginx nginx -t 2>&1 || true
  fi
else
  _warn "Nginx 容器未运行（不影响后端，检查是否需要启动 HTTPS）"
fi
echo ""

# ============================================================
# 6. 系统资源
# ============================================================
echo "--- 6. 系统资源 ---"

echo "  Docker 容器资源："
docker stats --no-stream --format "  {{.Name}}: CPU {{.CPUPerc}} | MEM {{.MemUsage}} | NET {{.NetIO}}" 2>/dev/null || _warn "无法获取 docker stats"
echo ""

echo "  磁盘使用："
df -h / 2>/dev/null | tail -1 | awk '{printf "  Root: %s / %s (%s used)\n", $3, $2, $5}' || _warn "无法获取磁盘信息"

# 检查数据卷目录磁盘
DEPLOY_DIR="$(pwd)/deploy"
if [ -d "$DEPLOY_DIR" ]; then
  df -h "$DEPLOY_DIR" 2>/dev/null | tail -1 | awk '{printf "  Deploy: %s / %s (%s used)\n", $3, $2, $5}' || true
fi
echo ""

# ============================================================
# 汇总
# ============================================================
echo "========================================"
echo "  检查结果"
echo "========================================"
echo -e "  ${GREEN}PASS${NC}: ${PASS}"
echo -e "  ${RED}FAIL${NC}: ${FAIL}"
echo -e "  ${YELLOW}WARN${NC}: ${WARN}"
echo ""

if [ "$FAIL" -gt 0 ]; then
  echo -e "${RED}健康检查未通过 — 请修复 FAIL 项后重试${NC}"
  exit 1
else
  echo -e "${GREEN}健康检查全部通过${NC}"
  exit 0
fi
