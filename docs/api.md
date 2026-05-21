# 校园学术文献智能检索与综述生成系统接口文档

建议文件位置：docs/api.md

## 1. 基本说明

后端地址：

```text
http://localhost:8080/api
```

统一返回格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

常见状态码：

| code | 含义 |
|---|---|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录或登录失效 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器错误 |

需要登录的接口，请求头携带：

```text
Authorization: Bearer token
```

---

## 2. 用户认证接口

### 2.1 用户注册

```http
POST /auth/register
```

请求参数：

```json
{
  "username": "student01",
  "password": "123456",
  "realName": "张三",
  "email": "student01@example.com"
}
```

返回：

```json
{
  "code": 200,
  "message": "注册成功",
  "data": null
}
```

### 2.2 用户登录

```http
POST /auth/login
```

请求参数：

```json
{
  "username": "student01",
  "password": "123456"
}
```

返回：

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "xxx",
    "user": {
      "id": 1,
      "username": "student01",
      "realName": "张三",
      "role": "USER"
    }
  }
}
```

---

## 3. 文献接口

### 3.1 文献检索

```http
GET /literatures/search
```

请求参数：

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| keyword | string | 否 | 关键词 |
| author | string | 否 | 作者 |
| categoryId | number | 否 | 分类ID |
| year | number | 否 | 发表年份 |
| sortBy | string | 否 | 排序字段：year / citation / relevance |
| page | number | 否 | 页码 |
| size | number | 否 | 每页数量 |

示例：

```http
GET /literatures/search?keyword=人工智能&page=1&size=10
```

返回：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 3,
    "records": [
      {
        "id": 1,
        "title": "人工智能在高校教学中的应用研究",
        "authors": "张三, 李四",
        "keywords": "人工智能,高校教学,个性化学习",
        "journal": "教育信息化研究",
        "publishYear": 2023,
        "citationCount": 12
      }
    ]
  }
}
```

### 3.2 查看文献详情

```http
GET /literatures/{id}
```

返回：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "title": "人工智能在高校教学中的应用研究",
    "authors": "张三, 李四",
    "abstractText": "本文分析了人工智能技术在高校教学中的应用场景……",
    "keywords": "人工智能,高校教学,个性化学习",
    "journal": "教育信息化研究",
    "publishYear": 2023,
    "doi": "10.0000/example001",
    "citationCount": 12
  }
}
```

### 3.3 新增文献，管理员

```http
POST /literatures
```

请求参数：

```json
{
  "title": "文献标题",
  "authors": "作者A, 作者B",
  "abstractText": "文献摘要",
  "keywords": "关键词1,关键词2",
  "journal": "期刊名称",
  "publishYear": 2024,
  "doi": "10.xxxx/xxxx",
  "categoryId": 1
}
```

### 3.4 修改文献，管理员

```http
PUT /literatures/{id}
```

请求参数同新增文献。

### 3.5 删除文献，管理员

```http
DELETE /literatures/{id}
```

---

## 4. 收藏接口

### 4.1 收藏文献

```http
POST /favorites/{literatureId}
```

### 4.2 取消收藏

```http
DELETE /favorites/{literatureId}
```

### 4.3 查看我的收藏

```http
GET /favorites
```

返回：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "literatureId": 1,
      "title": "人工智能在高校教学中的应用研究",
      "authors": "张三, 李四",
      "createTime": "2026-05-21 10:00:00"
    }
  ]
}
```

---

## 5. 检索历史接口

### 5.1 查看检索历史

```http
GET /search-history
```

返回：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "keyword": "人工智能",
      "searchType": "NORMAL",
      "resultCount": 3,
      "createTime": "2026-05-21 10:00:00"
    }
  ]
}
```

---

## 6. 智能检索接口

### 6.1 语义检索

```http
POST /ai/semantic-search
```

请求参数：

```json
{
  "query": "人工智能如何辅助高校教学",
  "topK": 10
}
```

返回：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "literatureId": 1,
      "title": "人工智能在高校教学中的应用研究",
      "similarity": 0.86
    }
  ]
}
```

### 6.2 相似文献推荐

```http
GET /ai/recommend/{literatureId}
```

返回：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "literatureId": 3,
      "title": "面向学术文献的智能检索系统设计",
      "similarity": 0.82
    }
  ]
}
```

---

## 7. 综述生成接口

### 7.1 生成综述

```http
POST /reviews/generate
```

请求参数：

```json
{
  "topic": "人工智能在高校教学中的应用",
  "literatureIds": [1, 2, 3]
}
```

返回：

```json
{
  "code": 200,
  "message": "生成成功",
  "data": {
    "id": 1,
    "topic": "人工智能在高校教学中的应用",
    "content": "近年来，人工智能在高校教学中的应用逐渐受到关注……",
    "references": [
      {
        "literatureId": 1,
        "title": "人工智能在高校教学中的应用研究"
      }
    ]
  }
}
```

### 7.2 查看综述记录

```http
GET /reviews/history
```

### 7.3 查看综述详情

```http
GET /reviews/{id}
```

### 7.4 删除综述记录

```http
DELETE /reviews/{id}
```

---

## 8. 管理员接口

### 8.1 查看用户列表

```http
GET /admin/users
```

### 8.2 修改用户状态

```http
PUT /admin/users/{id}/status
```

请求参数：

```json
{
  "status": 0
}
```

### 8.3 查看系统统计

```http
GET /admin/statistics
```

返回：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userCount": 20,
    "literatureCount": 120,
    "searchCount": 350,
    "reviewCount": 45
  }
}
```

---

## 9. 分类接口

### 9.1 查看分类列表

```http
GET /categories
```

### 9.2 新增分类，管理员

```http
POST /categories
```

请求参数：

```json
{
  "name": "人工智能",
  "parentId": 0,
  "sortOrder": 1
}
```

### 9.3 修改分类，管理员

```http
PUT /categories/{id}
```

### 9.4 删除分类，管理员

```http
DELETE /categories/{id}
```
