# ============================================================
# 今天吃啥 — 后端 Docker 镜像（多阶段构建）
# ============================================================
# 构建：docker build -t eat-what-backend:latest -f backend-java/Dockerfile backend-java
# 运行：容器内部监听 8080，不暴露公网端口，由 Nginx 反向代理

# ---- Stage 1: 构建 jar ----
FROM maven:3.8-eclipse-temurin-17 AS builder

WORKDIR /build

# Maven 镜像源（阿里云公共仓库，加速国内下载）
COPY .docker/settings.xml /root/.m2/settings.xml

# 先复制依赖描述文件，利用 Docker 层缓存加速重复构建
# 使用 maven 镜像自带的 mvn，不再依赖 ./mvnw
COPY pom.xml ./
RUN mvn dependency:go-offline -B -q || true

# 复制源码并打包（跳过测试，测试在 CI 中独立运行）
COPY src src
RUN mvn package -DskipTests -B -q

# ---- Stage 2: JRE 运行 ----
FROM eclipse-temurin:17-jre-alpine

# 安全：非 root 运行
RUN addgroup -S eatwhat && adduser -S eatwhat -G eatwhat

WORKDIR /app

# 从构建阶段复制 jar
COPY --from=builder /build/target/*.jar app.jar

# 暴露内部端口（不映射到宿主机，仅供 Nginx 容器访问）
EXPOSE 8080

USER eatwhat

# JVM 参数（4G 单机同时跑 MySQL + backend + nginx）：
# - -Xmx768m：堆上限 768MB，配合容器 mem_limit=1g，留 256MB 给 Metaspace/线程栈
# - -XX:+UseZGC：低延迟 GC，Java 17+ ZGC 在小堆上也表现良好
# - 不写死密钥，通过 docker-compose 环境变量注入
ENTRYPOINT ["java", "-Xmx768m", "-XX:+UseZGC", "-jar", "app.jar"]
