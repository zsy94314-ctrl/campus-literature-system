# 校园学术文献智能检索与综述生成系统

## 项目简介

本项目是一个面向课程实践的校园学术文献检索与综述生成系统，采用前后端分离架构，提供文献普通检索、高级检索、多学科智能语义检索、相似文献推荐、文献收藏、综述生成和管理员后台管理等功能。

系统中的文献数据来自课程项目模拟数据和 SQL 种子脚本，用于功能演示、检索测试和综述生成验证，不是真实论文库，也不代表真实学术数据源。

## 技术栈

| 模块 | 技术 |
|---|---|
| frontend | React 19、Vite、TypeScript、TanStack Router、Axios、Tailwind CSS、shadcn/ui、lucide-react |
| backend-java | Spring Boot 3.2、Java 17、MyBatis-Plus、MySQL、JWT、BCrypt |
| backend-ai | FastAPI、sentence-transformers、FAISS、NumPy、Pydantic |
| database | MySQL 8.x、SQL 初始化脚本、模拟文献种子数据 |
| 在线 LLM | OpenAI-compatible Chat Completions API，可配置 DeepSeek / OpenAI-compatible 服务 |

## 系统功能

普通用户功能：

- 注册与登录
- 文献普通检索
- 文献高级检索
- 多学科智能语义检索
- 文献详情查看
- 文献收藏与取消收藏
- 我的收藏
- 相似文献推荐
- 智能推荐参考文献
- 离线综述生成
- 在线 LLM 增强综述生成
- 综述记录查看
- 综述记录 `generationMode` 标签和筛选

管理员功能：

- 管理员登录
- 文献管理
- 分类管理
- 用户管理
- 数据统计
- 重建智能检索索引
- LLM API 管理
- 新增、编辑、删除 LLM 配置
- 设置 active LLM 配置
- 测试 LLM API 连接
- API Key 脱敏展示

backend-ai 功能：

- `GET /health`
- `POST /rebuild-index`
- `POST /semantic-search`
- `POST /recommend`
- 使用 `sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2` 生成文本向量
- 使用 FAISS 构建向量索引
- 索引文件位于 `backend-ai/data/faiss.index`
- 文献 ID 映射位于 `backend-ai/data/id_map.json`

## 项目结构

```text
campus-literature-system/
├── README.md
├── backend-ai/
│   ├── main.py
│   ├── requirements.txt
│   └── data/
│       ├── faiss.index
│       └── id_map.json
├── backend-java/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/campus/literature/
│       └── resources/
├── database/
│   ├── init.sql
│   ├── update_categories.sql
│   ├── literature_seed_100.sql
│   ├── literature_seed_500_high_quality.sql
│   └── update_llm_config.sql
├── docs/
└── frontend/
    ├── package.json
    └── src/
```

## 数据库说明

MySQL 数据库用于存储结构化业务数据，主要表包括：

- `user`：用户和管理员账号
- `category`：两级学科分类，`parent_id = 0` 表示一级分类，非 0 表示二级分类
- `literature`：文献元数据、摘要、关键词、正文节选等
- `favorite`：用户收藏关系
- `search_history`：用户普通检索历史
- `review_record`：综述记录，包含 `generation_mode`
- `llm_config`：管理员配置的在线 LLM API 信息
- `admin_log`、`literature_vector`：管理员操作日志和向量映射辅助表

FAISS 向量索引不存储在 MySQL 中，而是由 `backend-ai` 保存到 `backend-ai/data` 目录。

建议初始化顺序：

1. 执行 `database/init.sql`
2. 如需重建两级分类，执行 `database/update_categories.sql`
3. 导入模拟文献数据，例如 `database/literature_seed_500_high_quality.sql`
4. 执行 `database/update_llm_config.sql`
5. 启动系统后由管理员点击“重建智能索引”

注意：数据库连接用户名和密码请在本地 `backend-java/src/main/resources/application.yml` 中按实际环境配置，不要提交真实密码。

## 启动顺序

推荐启动顺序：

1. 启动 MySQL，并完成数据库脚本初始化
2. 启动 `backend-ai`
3. 启动 `backend-java`
4. 启动 `frontend`
5. 管理员登录后重建智能检索索引
6. 如需在线 LLM 增强综述生成，管理员在后台新增并激活 LLM 配置

## backend-ai 启动方式

```powershell
cd F:\campus-literature-system\backend-ai
pip install -r requirements.txt
uvicorn main:app --reload --port 8000
```

启动后访问：

```text
http://localhost:8000/health
```

首次运行时，`sentence-transformers` 可能需要下载模型。索引未重建时，语义检索和相似推荐会提示先重建智能检索索引。

## backend-java 启动方式

```powershell
cd F:\campus-literature-system\backend-java
mvn spring-boot:run
```

默认服务地址：

```text
http://localhost:8080/api
```

请确认：

- MySQL 已启动
- `application.yml` 中数据库连接信息与本地环境一致
- `ai.service.base-url` 指向 `http://localhost:8000`
- 不要把真实数据库密码、API Key、Token 写入文档或提交到仓库

## frontend 启动方式

```powershell
cd F:\campus-literature-system\frontend
npm install
npm run dev
```

默认前端开发地址通常为：

```text
http://localhost:5173
```

前端 API 基地址在 `frontend/src/api/request.ts` 中配置为 `http://localhost:8080/api`。

## 智能检索说明

系统的智能检索不是简单 FAISS 检索，而是“backend-ai 语义召回 + Spring Boot 多学科重排”的组合流程：

1. `backend-ai` 使用 sentence-transformers 生成查询与文献向量
2. FAISS 进行基础语义召回
3. Spring Boot 扩大召回候选池
4. 识别多学科主题画像
5. 根据主题进行 Query Expansion 查询扩展
6. 使用 MySQL 关键词补充召回
7. 计算 `keywordScore`
8. 计算 `categoryScore`
9. 使用 `weakPenalty` 对弱相关结果降权
10. 计算 `finalScore` 综合排序
11. 返回 `matchReason` 推荐原因解释

当前主题画像包括：

- 教育主题
- 医学健康主题
- 心理健康主题
- 文献检索主题
- 农业生态主题
- 法学治理主题
- 经济管理主题
- 人文艺术主题
- 工程技术主题
- 自然科学主题
- 人工智能通用主题

文献新增、编辑、删除或批量导入后，需要管理员重新执行“重建智能索引”，否则 `backend-ai/data/faiss.index` 与 MySQL 文献数据可能不同步。

## 综述生成说明

综述生成支持双模式：

| 模式 | 请求 `mode` | 记录 `generationMode` | 说明 |
|---|---|---|---|
| 离线综述生成 | `rule` | `rule` | 默认稳定模式，不依赖外部 API |
| 在线 LLM 增强综述生成 | `llm` | `llm` | 调用管理员配置的大模型 API |
| LLM 降级离线生成 | `llm` | `llm_fallback_rule` | 在线 LLM 不可用或输出不完整时自动降级 |

离线综述生成是系统默认稳定模式，基于本地规则实现，支持主题识别、关键词提取、研究方向归纳、存在问题与发展趋势生成。

在线 LLM 是增强模式，不是系统唯一生成方式。在线模式使用管理员配置的 OpenAI-compatible API，基于用户选择的 2 到 5 篇文献生成综述。

LLM 输出控制包括：

- 检查 `finish_reason`
- `finish_reason=length` 时尝试提高 `max_tokens` 重试
- 检查前五个正文部分是否完整
- 检查结尾是否明显截断
- 第六节“参考文献来源”由后端强制补全或替换
- 清理不存在的引用编号
- 不允许 LLM 编造参考文献
- 使用 `generationMode` 记录最终生成方式

## LLM API 配置说明

管理员可在后台“LLM API 管理”中维护在线综述生成配置，字段包括：

- 配置名称
- Provider
- Base URL
- Model
- API Key
- 是否启用
- 是否为当前 active 配置
- Timeout
- 备注

后端兼容 OpenAI-compatible Chat Completions API，最终调用地址形如：

```text
{baseUrl}/chat/completions
```

安全注意：

- 不要提交真实 API Key
- 前端只展示 `apiKeyMasked`
- 课程演示环境中配置可保存到数据库
- 生产环境应使用加密存储、环境变量、密钥轮换、访问审计和调用限额

## 文档目录说明

主要文档位于 `docs/`：

- `docs/report-outline-final.md`：正式报告目录
- `docs/writing-outline-final.md`：正文写作细纲
- `docs/api.md`：接口说明文档
- `docs/database-design.md`：数据库设计文档
- `docs/test-cases.md`：系统测试用例
- `docs/dev-log.md`：开发日志
- `docs/ai-records.md`：AI 辅助交互记录
- `docs/final-report-draft.md`：正式报告 Markdown 初稿

## 注意事项

- 文献数据为课程项目模拟数据，不是真实论文库。
- 在线 LLM 是增强模式，不是系统唯一生成方式。
- 离线综述生成是默认稳定模式。
- 文献更新后需要管理员重建智能检索索引。
- 不要提交真实 API Key、数据库密码、Token。
- 当前项目未实现 PDF 上传、真实论文库接入、Docker 部署、LLM 流式输出、Cross-Encoder Rerank；这些内容只能作为后续展望。
