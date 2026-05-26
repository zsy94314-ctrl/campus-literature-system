# Docker Compose 部署指南

本文档说明如何在 Ubuntu 服务器上使用 Docker Compose 一键部署《校园学术文献智能检索与综述生成系统》。

## 环境要求

- Ubuntu 20.04/22.04/24.04 LTS（或其他支持 Docker 的 Linux 发行版）
- Docker 24.0+ 和 Docker Compose Plugin
- 建议配置：2 核 CPU / 2GB 内存 / 20GB 磁盘
- 服务器可访问互联网（首次启动需下载模型和依赖）

## 服务器准备

### 1. 安装 Docker

```bash
sudo apt update
sudo apt install -y docker.io docker-compose-plugin
sudo systemctl enable docker --now
sudo usermod -aG docker $USER
# 退出并重新登录，使 docker 组生效
```

### 2. 配置安全组/防火墙

| 端口 | 用途 | 是否对外暴露 |
|---|---|---|
| 22 | SSH 远程管理 | 是（建议限制 IP） |
| 80 | HTTP 前端访问 | 是 |
| 443 | HTTPS（如需配置 SSL） | 是 |
| 3306 | MySQL | **否** |
| 8080 | Java 后端 API | **否** |
| 8000 | Python AI 服务 | **否** |

> **安全警告**：不要把 3306、8080、8000 暴露到公网。frontend 的 Nginx 已统一将 `/api/` 反向代理到 backend-java，外部只需访问 80 端口。

### 3. 克隆项目

```bash
git clone <你的仓库地址>
cd campus-literature-system
```

## 配置环境变量

```bash
cp .env.example .env
nano .env  # 或 vim .env
```

必须修改的项：

```env
# MySQL root 密码，建议设置高强度密码
MYSQL_ROOT_PASSWORD=YourStrongPassword123!

# Java 后端连接密码，建议与上面一致
DB_PASSWORD=YourStrongPassword123!

# JWT 签名密钥，建议 32 位以上随机字符串
JWT_SECRET=ChangeMeToAVeryLongRandomSecretKey2024!!!
```

其他项通常保持默认即可：

| 变量 | 说明 | Docker 内部默认值 |
|---|---|---|
| `MYSQL_DATABASE` | 创建的数据库名 | `campus_literature_system` |
| `DB_URL` | JDBC 连接地址 | 指向 `mysql:3306` |
| `DB_USERNAME` | 数据库用户 | `root` |
| `AI_SERVICE_BASE_URL` | AI 服务地址 | `http://backend-ai:8000` |

> `.env` 文件包含敏感信息，**不要提交到 Git**。

## 启动服务

```bash
docker compose up -d --build
```

首次构建可能需要 5–15 分钟，取决于网络速度和服务器性能。

### 服务启动顺序

Docker Compose 已配置依赖关系：

1. `mysql` 先启动，健康检查通过后继续
2. `backend-ai` 并行启动，健康检查通过后继续
3. `backend-java` 等待 mysql 和 backend-ai 都健康后启动
4. `frontend` 等待 backend-java 启动后启动

## 初始化与验证

### 1. 检查容器状态

```bash
docker compose ps
```

所有服务应为 `healthy` 或 `running` 状态。

### 2. 查看日志

```bash
# 查看全部服务日志
docker compose logs -f

# 只看某个服务
docker compose logs -f backend-java
docker compose logs -f backend-ai
docker compose logs -f mysql
docker compose logs -f frontend
```

### 3. 数据库初始化说明

MySQL 容器首次启动时会自动执行 `database/init-all.sql`，按以下顺序完成：

1. 创建表结构（`init.sql`）
2. 插入两级学科分类（`update_categories.sql`）
3. 导入 500 条高质量模拟文献数据（`literature_seed_500_high_quality.sql`）
4. 初始化 LLM 配置占位（`update_llm_config.sql`）

> 如果 `mysql-data` 卷已存在（即之前启动过），MySQL 不会重复执行初始化脚本。如需重新初始化，请先 `docker compose down -v` 删除数据卷。

### 4. 重建智能检索索引

系统首次运行后，**必须**管理员登录后台重建索引，否则语义检索不可用：

1. 打开 `http://服务器公网IP`
2. 使用管理员账号登录（默认管理员需在数据库中查看或初始化脚本中预设）
3. 进入后台 → 点击"重建智能检索索引"
4. 等待 `backend-ai` 日志显示索引重建完成

### 5. 配置在线 LLM（可选）

如需使用在线 LLM 增强综述生成：

1. 管理员后台 → LLM API 管理
2. 新增配置：填写 Provider、Base URL、Model、API Key
3. 激活配置
4. 测试连接

## 常用运维命令

### 重启单个服务

```bash
docker compose restart backend-java
docker compose restart backend-ai
```

### 停止所有服务（保留数据）

```bash
docker compose down
```

### 停止并删除数据卷（慎用，所有数据丢失）

```bash
docker compose down -v
```

### 重新构建并启动（代码更新后）

```bash
docker compose up -d --build
```

### 进入容器内部排查

```bash
docker compose exec mysql mysql -u root -p
docker compose exec backend-java sh
docker compose exec backend-ai sh
```

## 常见问题

### Q1: 服务器只有 2GB 内存，backend-ai 启动失败或非常慢

`sentence-transformers` 首次会自动下载 `paraphrase-multilingual-MiniLM-L12-v2` 模型，需要约 500MB–1GB 内存。2GB 服务器在同时运行 MySQL、Java、Python 时可能内存不足。

**解决方案：添加 Swap**

```bash
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
# 持久化：echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

### Q2: backend-java 报错 "Communications link failure"

通常是 MySQL 尚未完成初始化，backend-java 过早尝试连接。Docker Compose 已配置 `depends_on` + `condition: service_healthy`，但如果 MySQL 初始化脚本执行时间过长，仍可能超时。

**解决方案**：

```bash
docker compose logs -f mysql
# 确认 MySQL 已完成 init-all.sql 执行
docker compose restart backend-java
```

### Q3: 语义检索返回空结果

确认管理员已在后台执行"重建智能检索索引"，并检查 backend-ai 日志：

```bash
docker compose logs -f backend-ai
```

### Q4: 前端页面刷新后 404

Nginx 已配置 `try_files` 支持 React Router history fallback。如仍出现 404，检查 frontend 容器是否正常运行：

```bash
docker compose logs frontend
```

### Q5: 如何更新代码后重新部署？

```bash
git pull
docker compose down
docker compose up -d --build
```

MySQL 数据会保留在 `mysql-data` 卷中，不会丢失。

## 安全注意事项

1. **不要暴露内部端口**：3306（MySQL）、8080（Java）、8000（Python）只应在 Docker 内部网络访问，安全组不要放行。
2. **修改默认密码**：`.env` 中的 `MYSQL_ROOT_PASSWORD`、`DB_PASSWORD`、`JWT_SECRET` 必须使用高强度随机值。
3. **保护 API Key**：在线 LLM 的 API Key 通过管理后台录入，存储在 MySQL 中。生产环境建议对数据库敏感字段加密。
4. **定期备份**：重要数据通过 `mysql-data` Docker 卷持久化，建议定期备份：
   ```bash
   docker compose exec mysql mysqldump -u root -p campus_literature_system > backup.sql
   ```
5. **HTTPS 建议**：生产环境建议在 Nginx 前增加反向代理（如 Nginx Proxy Manager、Traefik、Cloudflare Tunnel）实现 HTTPS，不要直接对外暴露 80 端口处理敏感数据。

## 目录与数据持久化

| 持久化卷 | 对应容器路径 | 说明 |
|---|---|---|
| `mysql-data` | `/var/lib/mysql` | MySQL 数据库文件 |
| `backend-ai-data` | `/app/data` | FAISS 向量索引和 ID 映射 |

这两个卷由 Docker 自动管理，即使容器删除，数据也不会丢失（除非执行 `docker compose down -v`）。
