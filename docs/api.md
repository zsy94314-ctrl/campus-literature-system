# 接口说明文档

## 1 基本约定

Spring Boot 主服务默认地址：

```text
http://localhost:8080
```

前端统一请求基地址：

```text
http://localhost:8080/api
```

本文档接口路径均写完整 `/api/...`。前端 `request.ts` 已配置 `/api` 前缀，因此前端 API 文件中会省略 `/api`。

统一返回格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

需要登录的接口请求头：

```text
Authorization: Bearer <token>
```

管理员接口必须使用 `role=ADMIN` 的 Token。公开接口包括注册、登录、文献检索、文献详情、分类列表、分类统计、AI 健康检查、智能检索和相似推荐。

## 2 用户认证接口

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| POST | `/api/auth/register` | 公开 | 用户注册 |
| POST | `/api/auth/login` | 公开 | 用户登录 |

### POST /api/auth/register

请求体：

```json
{
  "username": "student01",
  "password": "password",
  "realName": "张三",
  "email": "student01@example.com"
}
```

说明：`username`、`password` 必填。密码由后端使用 BCrypt 保存为 `password_hash`。

### POST /api/auth/login

请求体：

```json
{
  "username": "student01",
  "password": "password"
}
```

返回数据：

```json
{
  "token": "<jwt_token>",
  "user": {
    "id": 2,
    "username": "student01",
    "realName": "张三",
    "role": "USER"
  }
}
```

## 3 文献接口

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/api/literatures/search` | 公开 | 普通检索 / 高级检索 |
| GET | `/api/literatures/{id}` | 公开 | 文献详情 |
| POST | `/api/literatures` | 管理员 | 新增文献 |
| PUT | `/api/literatures/{id}` | 管理员 | 修改文献 |
| DELETE | `/api/literatures/{id}` | 管理员 | 删除文献 |

### GET /api/literatures/search

查询参数：

| 参数 | 类型 | 说明 |
|---|---|---|
| keyword | string | 普通关键词，匹配标题、摘要、关键词、正文节选 |
| title | string | 标题 |
| author | string | 作者 |
| journal | string | 期刊或会议名称 |
| doi | string | DOI |
| categoryId | number | 分类 ID |
| documentType | string | 文献类型 |
| year | number | 指定年份 |
| startYear | number | 起始年份 |
| endYear | number | 截止年份 |
| sortBy | string | `relevance`、`year_desc`、`year_asc`、`citation_desc`、`citation_asc` |
| page | number | 页码，默认 1 |
| size | number | 每页数量，默认 10 |

返回数据：

```json
{
  "total": 500,
  "records": [
    {
      "id": 1,
      "title": "文献标题",
      "authors": "作者A, 作者B",
      "abstractText": "摘要",
      "keywords": "关键词1,关键词2",
      "journal": "期刊名称",
      "publishYear": 2024,
      "doi": "10.1000/example",
      "categoryId": 30,
      "categoryName": "教育学",
      "citationCount": 12,
      "documentType": "期刊论文"
    }
  ]
}
```

说明：登录用户执行普通检索时，后端会记录 `search_history`，`searchType=NORMAL`。当前智能检索接口未写入搜索历史。

### GET /api/literatures/{id}

返回字段包括列表字段，并额外包含：

- `sourceUrl`
- `content`

### POST /api/literatures

请求体：

```json
{
  "title": "文献标题",
  "authors": "作者A, 作者B",
  "abstractText": "摘要",
  "keywords": "关键词1,关键词2",
  "journal": "期刊名称",
  "publishYear": 2024,
  "doi": "10.1000/example",
  "categoryId": 30,
  "citationCount": 12,
  "documentType": "期刊论文",
  "sourceUrl": "https://example.edu/literature/0001",
  "content": "正文节选"
}
```

说明：文献新增、编辑或删除后，应由管理员重建智能检索索引。

## 4 收藏接口

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/api/favorites` | 登录用户 | 我的收藏 |
| POST | `/api/favorites/{literatureId}` | 登录用户 | 收藏文献 |
| DELETE | `/api/favorites/{literatureId}` | 登录用户 | 取消收藏 |

### GET /api/favorites

返回数据：

```json
[
  {
    "id": 1,
    "literatureId": 10,
    "title": "文献标题",
    "authors": "作者A, 作者B",
    "createTime": "2026-05-22T10:00:00"
  }
]
```

## 5 分类接口

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/api/categories` | 公开 | 分类列表 |
| GET | `/api/categories/statistics` | 公开 | 二级分类文献统计 |
| POST | `/api/categories` | 管理员 | 新增分类 |
| PUT | `/api/categories/{id}` | 管理员 | 修改分类 |
| DELETE | `/api/categories/{id}` | 管理员 | 删除分类 |

### GET /api/categories

返回数据：

```json
[
  {
    "id": 1,
    "name": "自然科学",
    "parentId": 0,
    "sortOrder": 1,
    "createTime": "2026-05-22T10:00:00"
  },
  {
    "id": 7,
    "name": "数学",
    "parentId": 1,
    "sortOrder": 1,
    "createTime": "2026-05-22T10:00:00"
  }
]
```

说明：`parentId=0` 表示一级分类，非 0 表示二级分类。文献通常绑定到二级分类。

### GET /api/categories/statistics

返回数据：

```json
[
  {
    "categoryId": 30,
    "categoryName": "教育学",
    "parentId": 5,
    "count": 16
  }
]
```

说明：当前实现统计二级分类，首页分类入口使用该接口。

## 6 智能检索接口

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/api/ai/health` | 公开 | backend-ai 健康检查代理 |
| POST | `/api/ai/rebuild-index` | 管理员 | 重建智能检索索引 |
| POST | `/api/ai/semantic-search` | 公开 | 多学科智能语义检索 |
| GET | `/api/ai/recommend/{literatureId}` | 公开 | 相似文献推荐 |

### GET /api/ai/health

说明：Spring Boot 调用 backend-ai 的 `GET /health`。

返回数据：

```json
{
  "status": "ok",
  "message": "backend-ai is running"
}
```

### POST /api/ai/rebuild-index

说明：

- Spring Boot 从 MySQL 查询全部文献。
- 传递标题、分类、文献类型、关键词、摘要、正文节选给 backend-ai。
- backend-ai 生成 `backend-ai/data/faiss.index` 和 `backend-ai/data/id_map.json`。
- 文献更新后需要重新调用。

返回数据：

```json
{
  "count": 500
}
```

`count` 为实际同步的文献数量，不固定等于 500。

### POST /api/ai/semantic-search

请求体：

```json
{
  "query": "人工智能在医学中的应用",
  "topK": 10
}
```

返回数据：

```json
[
  {
    "id": 247,
    "title": "文献标题",
    "authors": "作者A, 作者B",
    "abstractText": "摘要",
    "keywords": "医学影像,辅助诊断,人工智能",
    "journal": "医学信息学杂志",
    "publishYear": 2024,
    "categoryId": 18,
    "categoryName": "医学信息学",
    "citationCount": 88,
    "documentType": "期刊论文",
    "similarity": 0.674,
    "finalScore": 0.805,
    "matchReason": "语义相似度：67.4%；主题：医学健康主题、人工智能通用主题；命中扩展词：医学影像、辅助诊断；分类相关：医学信息学；无降权"
  }
]
```

智能检索流程：

1. backend-ai 使用 sentence-transformers 生成语义向量。
2. FAISS 做基础语义召回。
3. Spring Boot 将 `topK` 扩大到候选池。
4. 识别多学科主题画像。
5. Query Expansion 查询扩展。
6. MySQL 关键词补充召回。
7. 计算 `keywordScore`。
8. 计算 `categoryScore`。
9. 使用 `weakPenalty` 对弱相关分类降权。
10. 计算 `finalScore` 综合排序。
11. 返回 `matchReason` 推荐原因。

### GET /api/ai/recommend/{literatureId}

说明：根据当前文献向量检索相似文献，排除自身。

返回数据字段与智能检索结果类似，通常包含 `similarity`，相似推荐不进行多学科主题画像重排。

## 7 综述生成接口

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| POST | `/api/reviews/generate` | 登录用户 | 生成综述 |
| GET | `/api/reviews/history` | 登录用户 | 我的综述记录 |
| GET | `/api/reviews/{id}` | 登录用户 | 综述详情 |
| DELETE | `/api/reviews/{id}` | 登录用户 | 删除综述 |

### POST /api/reviews/generate

说明：该接口的综述生成流程体现 RAG 思想，即先基于主题和智能检索推荐相关文献，再基于用户选定的文献材料生成综述。请求中的 `literatureIds` 是用户选定的参考文献集合，后端会基于这些文献的标题、关键词、摘要和正文节选组织生成内容，并校验和补全参考文献来源。该接口不是新增标准 RAG 框架接口，也不表示完整工业级 RAG 平台。

请求体：

```json
{
  "topic": "人工智能在医学中的应用",
  "literatureIds": [247, 248, 249],
  "mode": "llm"
}
```

参数说明：

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| topic | string | 是 | 综述主题 |
| literatureIds | number[] | 是 | 参考文献 ID，数量必须为 2 到 5 |
| mode | string | 否 | `rule` 或 `llm`，默认前端使用 `rule` |

生成方式：

| 请求 mode | 返回 generationMode | 说明 |
|---|---|---|
| `rule` | `rule` | 离线综述生成 |
| `llm` | `llm` | 在线 LLM 增强综述生成成功 |
| `llm` | `llm_fallback_rule` | 在线 LLM 不可用或输出不完整，自动降级离线生成 |

RAG 风格约束：

- `literatureIds` 指定本次综述生成可使用的本地文献材料。
- `rule` 模式基于选中文献进行规则归纳，不依赖外部 API。
- `llm` 模式将选中文献内容作为受约束上下文调用管理员配置的在线 LLM。
- `generationMode` 记录最终生成方式，包括 `rule`、`llm` 和 `llm_fallback_rule`。
- 参考文献来源由后端校验和补全，不能由 LLM 自由编造。

返回数据：

```json
{
  "id": 3,
  "topic": "人工智能在医学中的应用",
  "content": "《人工智能在医学中的应用研究综述》\n\n一、研究背景\n...",
  "generationMode": "llm_fallback_rule",
  "references": [
    {
      "literatureId": 247,
      "title": "文献标题"
    }
  ],
  "createTime": "2026-05-22T15:30:00"
}
```

LLM 输出控制：

- 检查 `finish_reason`。
- `finish_reason=length` 时提高 `max_tokens` 重试一次。
- 检查前五个正文部分是否完整。
- 检查结尾是否明显截断。
- 第六节“参考文献来源”由后端强制补全或替换。
- 清理不存在的引用编号。
- 不允许 LLM 编造参考文献。
- 保存 `generationMode`。

### GET /api/reviews/history

说明：返回当前登录用户的综述记录，按创建时间倒序。

前端支持按：

- 全部
- `rule`
- `llm`
- `llm_fallback_rule`

进行筛选。

## 8 检索历史接口

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/api/search-history` | 登录用户 | 查看我的检索历史 |
| DELETE | `/api/search-history/{id}` | 登录用户 | 删除单条历史 |
| DELETE | `/api/search-history` | 登录用户 | 清空历史 |

返回数据：

```json
[
  {
    "id": 1,
    "keyword": "人工智能",
    "searchType": "NORMAL",
    "resultCount": 10,
    "createTime": "2026-05-22T10:00:00"
  }
]
```

说明：当前 Java 代码在普通文献检索中保存 `NORMAL` 历史，语义检索暂未写入 `SEMANTIC` 历史。

## 9 管理员接口

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/api/admin/users` | 管理员 | 用户列表 |
| PUT | `/api/admin/users/{id}/status` | 管理员 | 修改用户状态 |
| GET | `/api/admin/statistics` | 管理员 | 系统统计 |

### PUT /api/admin/users/{id}/status

请求体：

```json
{
  "status": 0
}
```

`status=1` 表示正常，`status=0` 表示禁用。

### GET /api/admin/statistics

返回数据：

```json
{
  "userCount": 20,
  "literatureCount": 500,
  "reviewCount": 45,
  "categoryCount": 43
}
```

说明：后端实际返回以上四个计数字段。前端统计页的“近 7 日趋势”目前由前端提供默认展示数据，不来自后端接口。

## 10 LLM API 管理接口

全部接口仅管理员可访问。

本模块实际接口清单：

- `GET /api/admin/llm-configs`
- `GET /api/admin/llm-configs/active`
- `POST /api/admin/llm-configs`
- `PUT /api/admin/llm-configs/{id}`
- `DELETE /api/admin/llm-configs/{id}`
- `POST /api/admin/llm-configs/{id}/activate`
- `POST /api/admin/llm-configs/{id}/test`
- `POST /api/admin/llm-configs/{id}/test-review`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/admin/llm-configs` | 获取 LLM 配置列表 |
| GET | `/api/admin/llm-configs/active` | 获取当前 active 配置 |
| POST | `/api/admin/llm-configs` | 新增 LLM 配置 |
| PUT | `/api/admin/llm-configs/{id}` | 修改 LLM 配置 |
| DELETE | `/api/admin/llm-configs/{id}` | 删除 LLM 配置 |
| POST | `/api/admin/llm-configs/{id}/activate` | 设置 active 配置 |
| POST | `/api/admin/llm-configs/{id}/test` | 测试 LLM API 连接 |
| POST | `/api/admin/llm-configs/{id}/test-review` | 长文本综述测试，实际已实现 |

### GET /api/admin/llm-configs

返回数据：

```json
[
  {
    "id": 1,
    "name": "DeepSeek Academic Review",
    "provider": "deepseek",
    "baseUrl": "https://api.example.com/v1",
    "model": "model-name",
    "apiKeyMasked": "<masked_api_key>",
    "enabled": 1,
    "active": 1,
    "timeoutSeconds": 180,
    "remark": "在线综述生成配置",
    "createTime": "2026-05-22T10:00:00",
    "updateTime": "2026-05-22T10:00:00"
  }
]
```

说明：后端不返回 API Key 明文，只返回 `apiKeyMasked`。

### POST /api/admin/llm-configs

请求体：

```json
{
  "name": "DeepSeek Academic Review",
  "provider": "deepseek",
  "baseUrl": "https://api.example.com/v1",
  "model": "model-name",
  "apiKey": "<api_key>",
  "enabled": 1,
  "active": 1,
  "timeoutSeconds": 180,
  "remark": "在线综述生成配置"
}
```

说明：

- 如果 `active=1`，后端会将其他配置的 active 置为 0。
- 不要在文档、代码或仓库中写真实 API Key。

### PUT /api/admin/llm-configs/{id}

请求体与新增一致。

说明：

- `apiKey` 为空字符串时不修改旧 Key。
- 如果填写新 `apiKey`，则更新数据库中的 Key。

### POST /api/admin/llm-configs/{id}/activate

说明：

- 指定配置 `active=1`。
- 其他配置 `active=0`。
- 当前配置 `enabled=1`。

### POST /api/admin/llm-configs/{id}/test

说明：

- 调用 `{baseUrl}/chat/completions`。
- 使用配置中的 `model` 和 `apiKey`。
- 测试 prompt 为“请回复：连接成功”。
- 返回连接响应文本和耗时。

### POST /api/admin/llm-configs/{id}/test-review

说明：

- 当前代码已实现该接口。
- 用固定的医学影像测试材料进行长文本综述测试。
- 返回 `success`、`elapsedMs`、`contentLength`、`sectionCount`、`validated`、`preview` 或错误信息。

## 11 backend-ai 接口

backend-ai 默认地址：

```text
http://localhost:8000
```

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/health` | 健康检查 |
| POST | `/rebuild-index` | 重建 FAISS 索引 |
| POST | `/semantic-search` | 基础语义召回 |
| POST | `/recommend` | 相似文献推荐 |

### GET /health

返回：

```json
{
  "status": "ok",
  "message": "backend-ai is running"
}
```

### POST /rebuild-index

请求体：

```json
{
  "documents": [
    {
      "id": 1,
      "title": "文献标题",
      "categoryName": "教育学",
      "documentType": "期刊论文",
      "keywords": "人工智能,教学",
      "abstractText": "摘要",
      "content": "正文节选"
    }
  ]
}
```

返回：

```json
{
  "code": 200,
  "message": "索引重建成功",
  "data": {
    "count": 1
  }
}
```

### POST /semantic-search

请求体：

```json
{
  "query": "AI 如何辅助高校课堂教学",
  "topK": 10
}
```

返回：

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

### POST /recommend

请求体：

```json
{
  "literatureId": 1,
  "topK": 5
}
```

返回：

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

## 12 常见错误

| code | 场景 | 说明 |
|---|---|---|
| 400 | 参数错误 | 例如综述文献数量不足或字段校验失败 |
| 401 | 未登录 | 缺少 Token 或 Token 无效 |
| 403 | 无权限 | 普通用户访问管理员接口 |
| 404 | 资源不存在 | 文献、配置或综述记录不存在 |
| 500 | backend-ai 未启动 | Spring Boot 调用 backend-ai 失败 |
| 500 | 索引未重建 | backend-ai 找不到 `faiss.index` 或 `id_map.json` |

LLM 生成过程中的大多数异常不会直接返回错误给用户，而是自动降级为离线生成，并返回 `generationMode=llm_fallback_rule`。
