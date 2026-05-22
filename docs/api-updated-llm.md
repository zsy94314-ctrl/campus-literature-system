# 校园学术文献智能检索与综述生成系统接口文档

建议文件位置：`docs/api.md`

---

## 1. 基本说明

### 1.1 后端地址

```text
http://localhost:8080/api
```

### 1.2 统一返回格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 1.3 常见状态码

| code | 含义 |
|---|---|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录或登录失效 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器错误 |

### 1.4 登录认证

需要登录的接口，请求头携带：

```text
Authorization: Bearer <token>
```

管理员接口需要管理员 Token。

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

返回示例：

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

返回示例：

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
| categoryId | number | 否 | 分类 ID |
| year | number | 否 | 发表年份 |
| documentType | string | 否 | 文献类型 |
| sortBy | string | 否 | 排序字段：year / citation / relevance |
| page | number | 否 | 页码，默认 1 |
| size | number | 否 | 每页数量，默认 10 |

请求示例：

```http
GET /literatures/search?keyword=人工智能&page=1&size=10
```

返回示例：

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
        "abstractText": "本文分析了人工智能技术在高校教学中的应用场景……",
        "keywords": "人工智能,高校教学,个性化学习",
        "journal": "教育信息化研究",
        "publishYear": 2023,
        "doi": "10.0000/example001",
        "citationCount": 12,
        "categoryId": 30,
        "categoryName": "教育学",
        "documentType": "期刊论文",
        "sourceUrl": "https://example.com/paper/1",
        "content": "正文节选……"
      }
    ]
  }
}
```

### 3.2 查看文献详情

```http
GET /literatures/{id}
```

返回示例：

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
    "citationCount": 12,
    "categoryId": 30,
    "categoryName": "教育学",
    "documentType": "期刊论文",
    "sourceUrl": "https://example.com/paper/1",
    "content": "正文节选……"
  }
}
```

### 3.3 新增文献，管理员

```http
POST /literatures
```

请求头：

```text
Authorization: Bearer <admin_token>
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
  "citationCount": 10,
  "categoryId": 30,
  "documentType": "期刊论文",
  "sourceUrl": "https://example.com/paper",
  "content": "正文节选"
}
```

返回示例：

```json
{
  "code": 200,
  "message": "新增成功",
  "data": null
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

返回示例：

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

## 4. 收藏接口

### 4.1 收藏文献

```http
POST /favorites/{literatureId}
```

请求头：

```text
Authorization: Bearer <token>
```

返回示例：

```json
{
  "code": 200,
  "message": "收藏成功",
  "data": null
}
```

### 4.2 取消收藏

```http
DELETE /favorites/{literatureId}
```

返回示例：

```json
{
  "code": 200,
  "message": "取消收藏成功",
  "data": null
}
```

### 4.3 查看我的收藏

```http
GET /favorites
```

返回示例：

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
      "journal": "教育信息化研究",
      "publishYear": 2023,
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

返回示例：

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

### 6.1 AI 服务健康检查

```http
GET /ai/health
```

说明：

- Spring Boot 代理调用 backend-ai 的 `/health`。
- backend-ai 未启动时返回友好错误。

返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "status": "ok",
    "message": "backend-ai is running"
  }
}
```

backend-ai 未启动时返回示例：

```json
{
  "code": 500,
  "message": "智能检索服务未启动，请先启动 backend-ai",
  "data": null
}
```

### 6.2 重建智能检索索引，管理员

```http
POST /ai/rebuild-index
```

请求头：

```text
Authorization: Bearer <admin_token>
```

说明：

- 从 MySQL 查询全部文献。
- 组装文献标题、分类、文献类型、关键词、摘要、正文节选等字段。
- 调用 backend-ai 的 `/rebuild-index`。
- 生成 FAISS 索引和 id_map 映射。
- 文献数据更新后建议重新调用该接口。

返回示例：

```json
{
  "code": 200,
  "message": "索引重建成功",
  "data": {
    "count": 500
  }
}
```

无文献数据时返回示例：

```json
{
  "code": 400,
  "message": "暂无文献数据，无法重建索引",
  "data": null
}
```

### 6.3 多学科智能语义检索

```http
POST /ai/semantic-search
```

请求参数：

```json
{
  "query": "人工智能在医学中的应用",
  "topK": 10
}
```

说明：

- backend-ai 负责 FAISS 语义召回。
- Spring Boot 负责多学科主题画像识别、Query Expansion、MySQL 关键词补充召回、keywordScore、categoryScore、weakPenalty 和 finalScore 综合重排。
- 返回结果中的 `similarity` 是原始语义相似度。
- `finalScore` 是综合排序分。
- `matchReason` 是推荐原因说明。

返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 247,
      "title": "面向临床场景的医学影像辅助诊断研究",
      "authors": "王明, 李华",
      "abstractText": "本文围绕医学影像辅助诊断展开研究……",
      "keywords": "医学影像,辅助诊断,人工智能,临床医学",
      "journal": "医学信息学杂志",
      "publishYear": 2024,
      "categoryId": 18,
      "categoryName": "医学信息学",
      "citationCount": 88,
      "documentType": "期刊论文",
      "similarity": 0.674,
      "finalScore": 0.805,
      "matchReason": "语义相似度：67.4%；主题：医学健康主题、人工智能通用主题；命中扩展词：医学影像、辅助诊断；分类相关：医学信息学"
    }
  ]
}
```

### 6.4 相似文献推荐

```http
GET /ai/recommend/{literatureId}
```

说明：

- 根据当前文献向量检索相似文献。
- 自动排除当前文献自身。

返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 3,
      "title": "面向学术文献的智能检索系统设计",
      "authors": "李四, 王五",
      "abstractText": "本文设计了一种面向学术文献的智能检索系统……",
      "keywords": "智能检索,学术文献,语义检索",
      "journal": "计算机应用研究",
      "publishYear": 2023,
      "categoryId": 35,
      "categoryName": "图书情报与档案管理",
      "citationCount": 45,
      "documentType": "期刊论文",
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

请求头：

```text
Authorization: Bearer <token>
```

请求参数：

```json
{
  "topic": "人工智能在医学中的应用",
  "literatureIds": [247, 248, 229],
  "mode": "llm"
}
```

参数说明：

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| topic | string | 是 | 综述主题 |
| literatureIds | number[] | 是 | 参考文献 ID，数量必须为 2～5 篇 |
| mode | string | 否 | 生成方式：rule / llm，默认 rule |

`mode` 取值说明：

| mode | 含义 |
|---|---|
| rule | 离线综述生成，使用本地规则生成 |
| llm | 在线 LLM 综述生成，调用管理员配置的大模型 API |
| llm_fallback_rule | 返回字段值，表示在线 LLM 不可用，已自动降级为离线生成 |

返回示例，离线生成：

```json
{
  "code": 200,
  "message": "生成成功",
  "data": {
    "id": 1,
    "topic": "人工智能在医学中的应用",
    "content": "《人工智能在医学中的应用研究综述》\n\n一、研究背景\n……",
    "references": [
      {
        "literatureId": 247,
        "title": "面向临床场景的医学影像辅助诊断研究"
      }
    ],
    "generationMode": "rule",
    "createTime": "2026-05-22 15:30:00"
  }
}
```

返回示例，在线 LLM 生成：

```json
{
  "code": 200,
  "message": "生成成功",
  "data": {
    "id": 2,
    "topic": "人工智能在医学中的应用",
    "content": "《人工智能在医学中的应用研究综述》\n\n一、研究背景\n……\n\n六、参考文献来源\n[1] 王明. 面向临床场景的医学影像辅助诊断研究. 《医学信息学杂志》, 2024.",
    "references": [
      {
        "literatureId": 247,
        "title": "面向临床场景的医学影像辅助诊断研究"
      }
    ],
    "generationMode": "llm",
    "createTime": "2026-05-22 15:40:00"
  }
}
```

返回示例，在线 LLM 失败并自动降级：

```json
{
  "code": 200,
  "message": "生成成功",
  "data": {
    "id": 3,
    "topic": "人工智能在医学中的应用",
    "content": "《人工智能在医学中的应用研究综述》\n\n一、研究背景\n……",
    "references": [
      {
        "literatureId": 247,
        "title": "面向临床场景的医学影像辅助诊断研究"
      }
    ],
    "generationMode": "llm_fallback_rule",
    "createTime": "2026-05-22 15:50:00"
  }
}
```

错误示例，文献数量不足：

```json
{
  "code": 400,
  "message": "参考文献数量必须在2-5篇之间",
  "data": null
}
```

### 7.2 查看综述记录

```http
GET /reviews/history
```

返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "topic": "人工智能在医学中的应用",
      "content": "《人工智能在医学中的应用研究综述》……",
      "references": [
        {
          "literatureId": 247,
          "title": "面向临床场景的医学影像辅助诊断研究"
        }
      ],
      "generationMode": "llm",
      "createTime": "2026-05-22 15:40:00"
    },
    {
      "id": 2,
      "topic": "人工智能在教学中的应用",
      "content": "《人工智能在教学中的应用研究综述》……",
      "references": [],
      "generationMode": "rule",
      "createTime": "2026-05-22 15:20:00"
    }
  ]
}
```

### 7.3 查看综述详情

```http
GET /reviews/{id}
```

返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "topic": "人工智能在医学中的应用",
    "content": "《人工智能在医学中的应用研究综述》……",
    "references": [
      {
        "literatureId": 247,
        "title": "面向临床场景的医学影像辅助诊断研究"
      }
    ],
    "generationMode": "llm",
    "createTime": "2026-05-22 15:40:00"
  }
}
```

### 7.4 删除综述记录

```http
DELETE /reviews/{id}
```

返回示例：

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

## 8. 分类接口

### 8.1 查看分类列表

```http
GET /categories
```

返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "自然科学",
      "parentId": 0,
      "sortOrder": 1
    },
    {
      "id": 7,
      "name": "数学",
      "parentId": 1,
      "sortOrder": 1
    }
  ]
}
```

### 8.2 查看分类统计

```http
GET /categories/statistics
```

说明：

- 首页学科分类使用该接口。
- 仅统计二级分类。
- 无需登录。

返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "categoryId": 30,
      "categoryName": "教育学",
      "parentId": 5,
      "count": 16
    },
    {
      "categoryId": 18,
      "categoryName": "医学信息学",
      "parentId": 3,
      "count": 12
    }
  ]
}
```

### 8.3 新增分类，管理员

```http
POST /categories
```

请求头：

```text
Authorization: Bearer <admin_token>
```

请求参数：

```json
{
  "name": "人工智能",
  "parentId": 0,
  "sortOrder": 1
}
```

### 8.4 修改分类，管理员

```http
PUT /categories/{id}
```

请求参数同新增分类。

### 8.5 删除分类，管理员

```http
DELETE /categories/{id}
```

---

## 9. 管理员接口

### 9.1 查看用户列表

```http
GET /admin/users
```

请求头：

```text
Authorization: Bearer <admin_token>
```

返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "username": "student01",
      "realName": "张三",
      "email": "student01@example.com",
      "role": "USER",
      "status": 1,
      "createTime": "2026-05-21 10:00:00"
    }
  ]
}
```

### 9.2 修改用户状态

```http
PUT /admin/users/{id}/status
```

请求参数：

```json
{
  "status": 0
}
```

返回示例：

```json
{
  "code": 200,
  "message": "修改成功",
  "data": null
}
```

### 9.3 查看系统统计

```http
GET /admin/statistics
```

返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userCount": 20,
    "literatureCount": 500,
    "categoryCount": 43,
    "favoriteCount": 120,
    "reviewCount": 45
  }
}
```

---

## 10. LLM API 管理接口，管理员

### 10.1 获取 LLM 配置列表

```http
GET /admin/llm-configs
```

请求头：

```text
Authorization: Bearer <admin_token>
```

返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "DeepSeek Academic Review",
      "provider": "deepseek",
      "baseUrl": "https://api.deepseek.com",
      "model": "deepseek-chat",
      "apiKeyMasked": "sk-1a****2b3c",
      "enabled": true,
      "active": true,
      "timeoutSeconds": 180,
      "remark": "DeepSeek online LLM review generation",
      "createTime": "2026-05-22 16:00:00",
      "updateTime": "2026-05-22 16:10:00"
    }
  ]
}
```

说明：

- 后端只返回 `apiKeyMasked`。
- 不返回 API Key 明文。

### 10.2 获取当前 active 的 LLM 配置

```http
GET /admin/llm-configs/active
```

返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "DeepSeek Academic Review",
    "provider": "deepseek",
    "baseUrl": "https://api.deepseek.com",
    "model": "deepseek-chat",
    "apiKeyMasked": "sk-1a****2b3c",
    "enabled": true,
    "active": true,
    "timeoutSeconds": 180,
    "remark": "DeepSeek online LLM review generation"
  }
}
```

### 10.3 新增 LLM 配置

```http
POST /admin/llm-configs
```

请求参数：

```json
{
  "name": "DeepSeek Academic Review",
  "provider": "deepseek",
  "baseUrl": "https://api.deepseek.com",
  "model": "deepseek-chat",
  "apiKey": "sk-xxxxxxxx",
  "enabled": true,
  "active": true,
  "timeoutSeconds": 180,
  "remark": "DeepSeek online LLM review generation"
}
```

返回示例：

```json
{
  "code": 200,
  "message": "新增成功",
  "data": null
}
```

说明：

- 如果 `active=true`，后端会将其他配置的 active 置为 false。
- API Key 由后端保存，前端后续只展示脱敏结果。
- 课程项目中可明文保存，生产环境应加密存储。

### 10.4 修改 LLM 配置

```http
PUT /admin/llm-configs/{id}
```

请求参数：

```json
{
  "name": "DeepSeek Academic Review",
  "provider": "deepseek",
  "baseUrl": "https://api.deepseek.com",
  "model": "deepseek-chat",
  "apiKey": "",
  "enabled": true,
  "active": true,
  "timeoutSeconds": 180,
  "remark": "更新后的备注"
}
```

说明：

- `apiKey` 为空或 null 时表示不修改旧 API Key。
- 如果填写新的 `apiKey`，则更新 API Key。
- 如果 `active=true`，后端会取消其他配置的 active 状态。

### 10.5 删除 LLM 配置

```http
DELETE /admin/llm-configs/{id}
```

返回示例：

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

### 10.6 设置当前使用的 LLM 配置

```http
POST /admin/llm-configs/{id}/activate
```

说明：

- 将指定配置设为 active。
- 同时将其他配置 active 置为 false。
- 当前配置 enabled 置为 true。

返回示例：

```json
{
  "code": 200,
  "message": "设置成功",
  "data": null
}
```

### 10.7 测试 LLM 连接

```http
POST /admin/llm-configs/{id}/test
```

说明：

- 调用配置对应的 OpenAI-compatible Chat Completions API。
- 测试 prompt 通常为：“请回复：连接成功”。
- 仅用于验证 API 基本可用。
- 长文本综述生成可能耗时更久，建议 timeoutSeconds 设置为 180～240 秒。

返回示例，成功：

```json
{
  "code": 200,
  "message": "LLM API 连接成功",
  "data": "连接成功"
}
```

返回示例，失败：

```json
{
  "code": 500,
  "message": "LLM API 连接失败，请检查 Base URL、Model、API Key 或网络",
  "data": null
}
```

---

## 11. backend-ai 服务接口，Python 服务

backend-ai 默认地址：

```text
http://localhost:8000
```

### 11.1 健康检查

```http
GET /health
```

返回示例：

```json
{
  "status": "ok",
  "message": "backend-ai is running"
}
```

### 11.2 重建索引

```http
POST /rebuild-index
```

请求参数：

```json
{
  "documents": [
    {
      "id": 1,
      "title": "示例文献标题",
      "categoryName": "软件工程",
      "documentType": "期刊论文",
      "keywords": "示例,关键词",
      "abstractText": "这是一段示例摘要。",
      "content": "这是一段示例正文节选。"
    }
  ]
}
```

返回示例：

```json
{
  "code": 200,
  "message": "索引重建成功",
  "data": {
    "count": 1
  }
}
```

### 11.3 语义检索

```http
POST /semantic-search
```

请求参数：

```json
{
  "query": "AI 如何辅助高校课堂教学",
  "topK": 10
}
```

返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "results": [
      {
        "literatureId": 1,
        "similarity": 0.6694
      }
    ]
  }
}
```

### 11.4 相似文献推荐

```http
POST /recommend
```

请求参数：

```json
{
  "literatureId": 1,
  "topK": 5
}
```

返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "results": [
      {
        "literatureId": 2,
        "similarity": 0.82
      }
    ]
  }
}
```

---

## 12. DeepSeek / OpenAI-compatible LLM 调用说明

该接口由后端 `LlmReviewServiceImpl` 调用，前端不直接访问。

### 12.1 请求地址

当管理员配置：

```text
baseUrl = https://api.deepseek.com
```

后端最终调用：

```http
POST https://api.deepseek.com/chat/completions
```

### 12.2 请求头

```http
Authorization: Bearer <apiKey>
Content-Type: application/json
```

### 12.3 请求体示例

```json
{
  "model": "deepseek-chat",
  "messages": [
    {
      "role": "system",
      "content": "你是一个严谨的中文学术综述写作助手……"
    },
    {
      "role": "user",
      "content": "综述主题：人工智能在医学中的应用……"
    }
  ],
  "temperature": 0.3,
  "max_tokens": 2500
}
```

### 12.4 后端校验与补全

在线 LLM 生成后，后端会进行：

- `finish_reason` 检查。
- 前五个正文部分完整性校验。
- 结尾截断检查。
- “六、参考文献来源”后端补全。
- 非法引用编号清理。
- 失败时自动降级为离线生成。

---

## 13. 权限说明

| 接口类型 | 是否需要登录 | 权限 |
|---|---|---|
| 注册 / 登录 | 否 | 公开 |
| 分类列表 / 分类统计 | 否 | 公开 |
| 文献检索 / 文献详情 | 否或普通用户可访问 | 公开或用户 |
| 收藏接口 | 是 | 普通用户 |
| 综述生成 / 综述记录 | 是 | 普通用户 |
| AI 健康检查 / 智能检索 / 相似推荐 | 否或普通用户可访问 | 公开或用户 |
| AI 索引重建 | 是 | 管理员 |
| 文献新增 / 修改 / 删除 | 是 | 管理员 |
| 分类新增 / 修改 / 删除 | 是 | 管理员 |
| 用户管理 | 是 | 管理员 |
| LLM API 管理 | 是 | 管理员 |

---

## 14. 常见错误返回

### 14.1 未登录

```json
{
  "code": 401,
  "message": "未登录或登录已失效",
  "data": null
}
```

### 14.2 无权限

```json
{
  "code": 403,
  "message": "无权限访问",
  "data": null
}
```

### 14.3 backend-ai 未启动

```json
{
  "code": 500,
  "message": "智能检索服务未启动，请先启动 backend-ai",
  "data": null
}
```

### 14.4 智能检索索引未重建

```json
{
  "code": 500,
  "message": "请先重建智能检索索引",
  "data": null
}
```

### 14.5 综述参考文献数量错误

```json
{
  "code": 400,
  "message": "参考文献数量必须在2-5篇之间",
  "data": null
}
```

### 14.6 LLM API 不可用

说明：

- 在线 LLM 生成中，大多数 LLM 调用异常不会直接返回错误。
- 系统会自动降级为离线综述生成。
- 返回 `generationMode=llm_fallback_rule`。

返回示例：

```json
{
  "code": 200,
  "message": "生成成功",
  "data": {
    "id": 10,
    "topic": "人工智能研究",
    "content": "离线规则生成的综述内容……",
    "generationMode": "llm_fallback_rule"
  }
}
```
