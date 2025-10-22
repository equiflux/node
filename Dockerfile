# Equiflux Node Dockerfile
# 多阶段构建，优化镜像大小和安全性

# 阶段1: 构建阶段
FROM openjdk:21-jdk-slim AS builder

# 设置工作目录
WORKDIR /app

# 安装必要的工具
RUN apt-get update && apt-get install -y \
    maven \
    git \
    && rm -rf /var/lib/apt/lists/*

# 复制Maven配置文件
COPY pom.xml .

# 下载依赖（利用Docker缓存层）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src

# 构建应用
RUN mvn clean package -DskipTests -B

# 阶段2: 运行时阶段
FROM openjdk:21-jre-slim AS runtime

# 创建非root用户
RUN groupadd --system equiflux && \
    useradd --system --gid equiflux --shell /bin/bash --create-home equiflux

# 设置工作目录
WORKDIR /app

# 安装必要的运行时工具
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    procps \
    && rm -rf /var/lib/apt/lists/*

# 从构建阶段复制JAR文件
COPY --from=builder /app/target/equiflux-node-*.jar app.jar

# 创建必要的目录
RUN mkdir -p /app/logs /app/data /app/config && \
    chown -R equiflux:equiflux /app

# 设置JVM参数
ENV JAVA_OPTS="-Xms2g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -XX:+OptimizeStringConcat"

# 设置应用参数
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080
ENV MANAGEMENT_SERVER_PORT=8081

# 暴露端口
EXPOSE 8080 8081

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

# 切换到非root用户
USER equiflux

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# 标签信息
LABEL maintainer="Equiflux Team" \
      version="1.0.0" \
      description="Equiflux公链节点实现 - 三层混合共识协议" \
      org.opencontainers.image.title="Equiflux Node" \
      org.opencontainers.image.description="Equiflux公链节点实现" \
      org.opencontainers.image.version="1.0.0" \
      org.opencontainers.image.source="https://github.com/equiflux/node"
