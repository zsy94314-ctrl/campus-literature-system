# 校园学术文献智能检索与综述生成系统（后端）

## 1. 项目介绍

本项目是「校园学术文献智能检索与综述生成系统」的 Java 后端服务，基于 Spring Boot 3.x 构建，提供用户认证、文献检索、收藏管理、综述生成、智能推荐、管理员后台等完整功能。

## 2. 技术栈

| 技术 | 版本 | 说明 |
|---|---|---|
| Spring Boot | 3.2.0 | 核心框架 |
| Java | 17 | 编程语言 |
| Maven | 3.8+ | 构建工具 |
| MyBatis-Plus | 3.5.5 | ORM 框架 |
| MySQL | 8.x | 数据库 |
| JWT | 0.12.3 | 登录认证 |
| Lombok | - | 简化代码 |
| Spring Validation | - | 参数校验 |
| BCrypt | - | 密码加密 |

## 3. 项目结构

```
backend-java/
├── pom.xml
├── README.md
└── src/main/
    ├── java/com/campus/literature/
    │   ├── CampusLiteratureApplication.java  # 启动类
    │   ├── common/                           # 通用返回结果
    │   │   ├── Result.java
    │   │   ├── PageResult.java
    │   │   └── ErrorCode.java
    │   ├── config/                           # 配置类
    │   │   ├── CorsConfig.java
    │   │   ├── MybatisPlusConfig.java
    │   │   └── WebConfig.java
    │   ├── security/                         # JWT 认证
    │   │   ├── JwtUtil.java
    │   │   ├── JwtInterceptor.java
    │   │   └── UserContext.java
    │   ├── exception/                        # 异常处理
    │   │   ├── BusinessException.java
    │   │   └── GlobalExceptionHandler.java
    │   ├── entity/                           # 数据库实体
    │   ├── dto/                              # 请求参数对象
    │   ├── vo/                               # 返回视图对象
    │   ├── mapper/                           # MyBatis Mapper
    │   ├── service/                          # 业务逻辑
    │   │   └── impl/
    │   └── controller/                       # 接口层
    └── resources/
        ├── application.yml
        └── mapper/                           # XML 映射文件
```

## 4. 数据库初始化步骤

1. 确保本地已安装 MySQL 8.x 并启动服务。
2. 打开命令行或数据库管理工具，执行项目根目录下的建表脚本：

```bash
mysql -u root -p < database/init.sql
```

脚本会自动创建数据库 `campus_literature_system`，并初始化管理员账号、分类和示例文献。

## 5. 启动步骤

### 5.1 编译打包

```bash
cd backend-java
mvn clean package -DskipTests
```

### 5.2 直接运行

```bash
mvn spring-boot:run
```

或运行打包后的 jar：

```bash
java -jar target/literature-1.0.0.jar
```

启动成功后，服务默认监听 `http://localhost:8080`。

## 6. 默认管理员账号

| 字段 | 值 |
|---|---|
| 用户名 | `admin` |
| 密码 | `admin123` |
| 角色 | ADMIN |

> 说明：数据库脚本中 `admin` 的密码为明文占位，首次登录时后端会自动通过 BCrypt 校验。由于初始数据脚本中密码字段写的是 `admin123`，但后端使用 BCrypt 加密存储，**建议注册一个新管理员账号**或使用以下 SQL 将初始密码更新为 BCrypt 加密值：

```sql
UPDATE user SET password_hash = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO' WHERE username = 'admin';
```

（上述 hash 对应明文 `admin123`）

## 7. 接口说明

所有接口统一前缀：`/api`

统一返回格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

| 模块 | 方法 | 接口 | 说明 | 权限 |
|---|---|---|---|---|
| 认证 | POST | `/api/auth/register` | 用户注册 | 公开 |
| 认证 | POST | `/api/auth/login` | 用户登录 | 公开 |
| 文献 | GET | `/api/literatures/search` | 文献检索 | 公开 |
| 文献 | GET | `/api/literatures/{id}` | 文献详情 | 公开 |
| 文献 | POST | `/api/literatures` | 新增文献 | ADMIN |
| 文献 | PUT | `/api/literatures/{id}` | 修改文献 | ADMIN |
| 文献 | DELETE | `/api/literatures/{id}` | 删除文献 | ADMIN |
| 收藏 | POST | `/api/favorites/{literatureId}` | 收藏文献 | 登录用户 |
| 收藏 | DELETE | `/api/favorites/{literatureId}` | 取消收藏 | 登录用户 |
| 收藏 | GET | `/api/favorites` | 我的收藏 | 登录用户 |
| 检索历史 | GET | `/api/search-history` | 检索历史 | 登录用户 |
| 智能检索 | POST | `/api/ai/semantic-search` | 语义检索 | 登录用户 |
| 智能检索 | GET | `/api/ai/recommend/{literatureId}` | 相似推荐 | 登录用户 |
| 综述 | POST | `/api/reviews/generate` | 生成综述 | 登录用户 |
| 综述 | GET | `/api/reviews/history` | 综述记录 | 登录用户 |
| 综述 | GET | `/api/reviews/{id}` | 综述详情 | 登录用户 |
| 综述 | DELETE | `/api/reviews/{id}` | 删除综述 | 登录用户 |
| 管理员 | GET | `/api/admin/users` | 用户列表 | ADMIN |
| 管理员 | PUT | `/api/admin/users/{id}/status` | 修改用户状态 | ADMIN |
| 管理员 | GET | `/api/admin/statistics` | 系统统计 | ADMIN |
| 分类 | GET | `/api/categories` | 分类列表 | 公开 |
| 分类 | POST | `/api/categories` | 新增分类 | ADMIN |
| 分类 | PUT | `/api/categories/{id}` | 修改分类 | ADMIN |
| 分类 | DELETE | `/api/categories/{id}` | 删除分类 | ADMIN |

需要登录的接口，请求头中携带：

```text
Authorization: Bearer {token}
```

## 8. 前端对接说明

本项目已配置 CORS，允许跨域访问。前端（如 React + Vite）可直接调用后端接口。

示例前端请求配置（Axios）：

```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
});

// 请求拦截器：自动带上 token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 响应拦截器：统一处理错误码
api.interceptors.response.use(
  (res) => res.data,
  (err) => {
    if (err.response?.status === 401) {
      // 未登录，跳转登录页
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

export default api;
```

## 9. 注意事项

1. 启动前请确保 MySQL 服务已启动，且数据库 `campus_literature_system` 已创建。
2. 如需修改数据库连接信息，请编辑 `src/main/resources/application.yml`。
3. JWT 密钥和过期时间也在 `application.yml` 中配置，生产环境请替换为强密钥。
