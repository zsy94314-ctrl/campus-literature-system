# 《校园学术文献智能检索与综述生成系统》写作细纲

> 用途：本文件不是最终报告目录，而是写正文时的内部提纲。最终目录应使用 `report-outline.md`。本版已同步最新功能：多学科智能语义检索、离线规则综述生成、在线 LLM 增强综述生成、管理员 LLM API 管理、综述记录生成方式标识、LLM 输出完整性校验与参考文献后端补全。

---

## 封面

- 软件工程三级项目
- 课程设计题目
- 姓名、班级、学号
- 小组成员
- 指导教师
- 日期

---

## 摘要

写作要点：

- 系统建设背景：校园学术文献数量增长，传统关键词检索在语义理解、跨学科检索和综述整理方面存在不足。
- 系统目标：实现文献管理、普通检索、高级检索、多学科智能语义检索、相似文献推荐和综述生成。
- 技术路线：React + Vite + TypeScript + Spring Boot + MySQL + FastAPI + sentence-transformers + FAISS。
- 智能检索特色：在 FAISS 语义召回基础上，引入多学科主题画像、Query Expansion、MySQL 关键词补充召回、keywordScore、categoryScore、weakPenalty 和 finalScore 综合重排。
- 综述生成特色：支持“离线规则生成”和“在线 LLM 增强生成”双模式。离线模式保证稳定可用，在线模式调用管理员配置的大模型 API 提升语言表达和归纳质量。
- 稳定性保障：在线 LLM 失败、超时、输出截断或参考文献不完整时，系统可自动补全参考文献或降级为离线生成。
- 系统成果：完成普通用户端、管理员端、两级学科分类体系、500 条高质量模拟文献库、多学科智能检索、相似推荐、综述生成、LLM API 管理和综述记录管理。

---

## 关键词

建议：

- 学术文献检索
- 多学科智能语义检索
- 综述生成
- 大模型 API
- FAISS
- RAG
- Spring Boot

---

## 1 引言

### 1.1 项目背景

写作要点：

- 高校师生在课程学习、科研训练和论文写作中需要查阅大量学术文献。
- 传统关键词检索更多依赖标题或关键词匹配，难以充分理解自然语言研究主题。
- 单纯语义向量检索虽然具备语义理解能力，但在跨学科场景下可能出现语义泛化。
- 文献综述撰写需要阅读、筛选、归纳多篇文献，人工完成耗时较长。
- 大模型可以提升综述表达质量，但在线 API 存在延迟、失败和成本问题，因此系统需要保留离线稳定生成方案。

### 1.2 项目意义

写作要点：

- 对普通用户：提高文献检索、相关文献发现、收藏和综述整理效率。
- 对管理员：实现文献资源维护、分类管理、用户管理、数据统计、智能索引重建和 LLM API 配置管理。
- 对课程项目：体现需求分析、系统设计、编码实现、测试与维护的软件工程全过程。
- 对技术实践：融合前后端分离、语义向量检索、多学科主题画像、FAISS 向量索引、RAG 思想和 LLM API 工程化接入。

### 1.3 项目目标

写作要点：

- 建立校园学术文献管理与检索平台。
- 支持普通检索、高级检索和智能语义检索。
- 支持多学科主题下的相关文献推荐。
- 支持相似文献推荐。
- 支持基于智能推荐文献的结构化综述生成。
- 支持离线规则综述生成与在线 LLM 增强综述生成。
- 支持管理员管理文献、分类、用户、统计数据、智能检索索引和 LLM API 配置。
- 构建覆盖多学科的 500 条高质量模拟文献库。

### 1.4 系统主要功能

可写：

- 用户注册与登录
- 文献普通检索
- 文献高级检索
- 多学科智能语义检索
- 文献详情查看
- 文献收藏
- 相似文献推荐
- 离线综述生成
- 在线 LLM 增强综述生成
- 综述记录管理与生成方式标识
- 管理员文献管理、分类管理、用户管理、数据统计
- 管理员重建智能检索索引
- 管理员 LLM API 管理

### 1.5 项目特色与创新点

重点写：

- 两级正规学科分类体系：6 个一级学科，37 个二级学科。
- 500 条高质量多学科模拟文献数据。
- 基于 sentence-transformers + FAISS 的语义向量召回。
- 多学科主题画像重排机制，覆盖教育、医学健康、心理健康、文献检索、农业生态、法学治理、经济管理、人文艺术、工程技术、自然科学和人工智能通用主题。
- Query Expansion 与 MySQL 关键词补充召回，减少纯向量检索语义漂移。
- finalScore 综合排序：semanticSimilarity、keywordScore、categoryScore 与 weakPenalty 结合。
- matchReason 推荐原因解释，提高智能检索可解释性。
- 离线规则综述生成：稳定、快速、无需外部 API。
- 在线 LLM 增强综述生成：调用管理员配置的大模型 API，提高语言表达质量。
- LLM 输出完整性校验：检测章节完整性、finish_reason、截断结尾和参考文献完整性。
- 后端强制补全参考文献来源，避免大模型漏写或编造参考文献。
- LLM 失败自动降级为离线规则生成，保证系统稳定可用。
- 管理员 LLM API 管理模块支持新增、编辑、设为当前、测试连接、删除和 API Key 脱敏展示。
- RAG 风格综述生成流程：先检索，再选择文献，再基于参考文献生成综述。

### 1.6 项目进度安排

建议按阶段写：

| 阶段 | 主要任务 |
|---|---|
| 第一阶段 | 需求分析，明确系统功能、用户角色和数据结构 |
| 第二阶段 | 前端原型与后端基础接口设计 |
| 第三阶段 | MVP 联调，完成登录、检索、详情、收藏、综述、管理后台 |
| 第四阶段 | 数据增强，扩展文献字段、两级学科分类和 500 条高质量模拟文献 |
| 第五阶段 | backend-ai 智能检索服务，完成 FastAPI、sentence-transformers 和 FAISS |
| 第六阶段 | Spring Boot 接入 backend-ai，完成索引重建、语义检索和相似推荐 |
| 第七阶段 | 前端接入智能检索和相似推荐 |
| 第八阶段 | 综述生成升级，完成智能推荐参考文献和主题感知型综述生成 |
| 第九阶段 | 智能检索优化，完成多学科主题画像、查询扩展、补充召回和重排 |
| 第十阶段 | LLM 增强综述生成，完成管理员 LLM API 管理和用户端双模式综述生成 |
| 第十一阶段 | 测试、截图、文档和答辩准备 |

### 1.7 小组成员分工及工作比例

写作要点：需求分析、前端开发、后端开发、数据库与数据构造、智能检索服务、LLM API 接入与综述生成、测试与文档、AI 辅助开发与整合说明。

### 1.8 参考资料与开发工具

可写：IntelliJ IDEA、VS Code、MySQL、Maven、Node.js、Python、PowerShell、Git、curl / Apifox、Draw.io / ProcessOn、DeepSeek API / OpenAI-compatible API、AI 辅助工具。

---

## 2 可行性研究

### 2.1 技术可行性

写作要点：

- React + Vite + TypeScript 适合构建响应式前端页面。
- Spring Boot + MyBatis-Plus 适合构建 RESTful 后端服务。
- MySQL 适合存储用户、文献、分类、收藏、综述记录和 LLM 配置。
- FastAPI 适合独立封装 Python 智能检索服务。
- sentence-transformers 可生成多语言文本语义向量。
- FAISS 可实现高效向量相似度检索。
- Spring Boot 与 backend-ai 通过 HTTP 接口解耦。
- LLM API 采用 OpenAI-compatible 调用方式，管理员可配置 DeepSeek 等模型服务。
- 在线 LLM 失败时自动降级离线生成，技术风险可控。

### 2.2 经济可行性

写作要点：开源框架、本地开发、模拟文献数据降低成本；在线 LLM 是可选增强，默认离线生成保证无 API 成本时也能运行。

### 2.3 操作可行性

写作要点：用户检索和综述生成流程清晰；管理员后台提供文献、分类、用户、统计、索引重建和 LLM API 配置入口。

### 2.4 法律与学术规范可行性

写作要点：模拟文献不冒充真实论文；报告说明 AI 辅助；在线 LLM 只能基于用户选择的文献生成，不允许新增或编造参考文献；API Key 前端脱敏显示，生产环境应加密存储。

### 2.5 可行性结论

写作要点：技术可实现、成本可控、操作清晰、数据来源和学术规范可控、智能检索与综述生成均具有稳定兜底方案。

---

## 3 需求分析

### 3.1 用户角色分析

普通用户：注册登录、普通检索、高级检索、智能检索、详情查看、收藏、相似推荐、选择离线/在线方式生成综述、查看综述记录和生成方式。

管理员：文献管理、分类管理、用户管理、数据统计、重建智能索引、管理 LLM API 配置、测试连接、设置当前启用的大模型配置。

### 3.2 功能需求分析

#### 用户注册与登录

- 输入：用户名、密码、邮箱等。
- 处理：校验用户信息，登录成功后生成 JWT。
- 输出：登录状态、用户信息和权限信息。

#### 文献普通检索

- 输入：关键词、分类、分页参数。
- 处理：数据库模糊查询标题、摘要、关键词等字段。
- 输出：文献列表、分页信息和总数。

#### 文献高级检索

- 输入：标题、作者、期刊、DOI、分类、文献类型、年份范围、排序方式。
- 处理：多条件组合查询。
- 输出：符合条件的文献结果。

#### 多学科智能语义检索

- 输入：自然语言研究主题和 topK。
- 处理：Spring Boot 调用 backend-ai 进行 FAISS 语义召回，并结合多学科主题画像、查询扩展、MySQL 关键词补充召回、keywordScore、categoryScore、weakPenalty 和 finalScore 进行重排。
- 输出：语义相关文献、similarity、finalScore 和 matchReason。

#### 相似文献推荐

- 输入：当前文献 ID。
- 处理：backend-ai 根据当前文献向量检索相似文献。
- 输出：相似文献列表和相似度。

#### 离线综述生成

- 输入：综述主题、用户选择的 2～5 篇参考文献 ID、mode=rule。
- 处理：后端根据文献标题、作者、年份、期刊、关键词、摘要、正文节选和主题类型生成结构化综述。
- 输出：六段式综述内容，generationMode=rule。

#### 在线 LLM 综述生成

- 输入：综述主题、用户选择的 2～5 篇参考文献 ID、mode=llm。
- 处理：后端读取当前 active 的 LLM 配置，构造严格 prompt，调用 OpenAI-compatible Chat Completions API 生成综述；随后校验章节完整性、参考文献完整性和引用编号合法性。
- 输出：在线 LLM 生成的综述内容，generationMode=llm。
- 异常处理：无配置、API Key 为空、调用超时、HTTP 错误、JSON 解析失败、输出截断或正文严重不完整时，自动降级为离线规则生成，generationMode=llm_fallback_rule。

#### 综述记录管理

- 输入：用户身份信息。
- 处理：查询用户生成过的综述记录。
- 输出：综述主题、生成时间、参考文献、正文内容和 generationMode。
- 前端支持按“全部 / 离线综述生成 / 在线 LLM 综述生成 / LLM 降级离线生成”筛选。

#### 管理员 LLM API 管理

- 新增、编辑、删除 LLM API 配置。
- 设置某个配置为当前 active。
- 测试连接。
- API Key 前端脱敏展示。
- 普通用户不能访问 LLM 配置管理接口。

### 3.3 非功能需求分析

性能需求：检索分页、智能检索候选池限制、FAISS 索引预构建、在线 LLM 生成显示等待提示。

安全性需求：JWT 身份认证、管理员接口权限控制、API Key 不在前端明文展示、不打印到日志。

可靠性需求：backend-ai 未启动友好错误；在线 LLM 不可用自动降级离线；参考文献来源由后端校验和补全。

易用性需求：普通/智能检索区分明显；综述生成页面清楚展示两种生成方式；综述记录显示 generationMode。

可维护性需求：前后端分离；Java 后端与 Python 智能服务解耦；LLM API 配置由管理员端维护，不写死在代码中。

可扩展性需求：后续可接入真实论文库、BM25、Rerank、更多 OpenAI-compatible 模型服务、PDF 上传和流式 LLM 输出。

### 3.4 系统业务流程分析

建议画图：普通用户业务流程、管理员业务流程、智能检索业务流程、综述生成业务流程、LLM API 管理业务流程。

### 3.5 数据流图

建议绘制：顶层 DFD、0 层 DFD、智能检索 1 层 DFD、离线/在线综述生成 1 层 DFD、LLM API 管理 1 层 DFD。

### 3.6 数据字典

建议列：用户信息、文献信息、分类信息、收藏信息、综述记录、generationMode、LLM 配置信息、智能检索请求、智能检索结果、主题画像、关键词扩展词、综述生成请求、综述内容。

---

## 4 系统总体设计

### 4.1 系统设计目标

写作要点：模块清晰、前后端分离、智能检索服务独立部署、数据结构规范、支持多学科文献检索与综述生成、支持离线与在线双模式综述生成、在线 LLM 失败时保证系统可用。

### 4.2 系统总体架构

写架构：React 前端、Spring Boot 主后端、MySQL 数据库、FastAPI 智能检索服务、sentence-transformers、FAISS、DeepSeek / OpenAI-compatible LLM API。

可画：用户 → React → Spring Boot → MySQL；Spring Boot → backend-ai → FAISS；Spring Boot → LLM API。

### 4.3 系统功能模块划分

用户认证、文献检索、智能检索、相似推荐、综述生成、LLM API 管理、收藏、管理员、分类管理、数据统计、智能索引维护。

### 4.4 功能结构图

普通用户端：文献检索、高级检索、智能检索、详情、收藏、离线综述生成、在线 LLM 综述生成、综述记录。

管理员端：文献管理、分类管理、用户管理、数据统计、重建智能索引、LLM API 管理。

### 4.5 系统接口设计

重点列：

- `/api/auth/login`
- `/api/auth/register`
- `/api/literatures/search`
- `/api/literatures/{id}`
- `/api/favorites`
- `/api/categories`
- `/api/categories/statistics`
- `/api/ai/health`
- `/api/ai/rebuild-index`
- `/api/ai/semantic-search`
- `/api/ai/recommend/{id}`
- `/api/reviews/generate`
- `/api/reviews/history`
- `/api/admin/users`
- `/api/admin/llm-configs`
- `/api/admin/llm-configs/{id}/activate`
- `/api/admin/llm-configs/{id}/test`

### 4.6 数据库设计

表：user、literature、category、favorite、review_record、llm_config。

重点说明：review_record 存储 generation_mode；llm_config 存储管理员配置的大模型 API 信息；FAISS 索引文件和 id_map.json 属于 backend-ai 文件存储。

### 4.7 两级学科分类体系设计

写：category.parent_id 支持一级、二级分类；一级分类 6 个；二级分类 37 个；文献绑定二级分类；学科交叉通过标题、关键词、摘要和正文节选体现。

### 4.8 高质量模拟文献数据设计

写：共 500 条，覆盖 37 个二级分类，字段包括标题、作者、摘要、关键词、期刊、年份、DOI、引用次数、文献类型、来源链接、正文节选；数据用于智能检索和综述生成。

---

## 5 面向对象设计

### 5.1 用例分析

普通用户用例：登录、搜索、智能检索、详情、收藏、相似推荐、离线综述生成、在线 LLM 综述生成、查看综述记录、按生成方式筛选。

管理员用例：登录、文献管理、分类管理、用户管理、统计、重建智能索引、管理 LLM API 配置、测试连接、设置当前模型配置。

### 5.2 用例图

建议画：普通用户用例图、管理员用例图。

### 5.3 类图设计

建议包含：User、Literature、Category、Favorite、ReviewRecord、LlmConfig、AiDocumentDTO、AiSearchRequest、AiSearchResultVO、ReviewGenerateRequest、ReviewRecordVO、LlmConfigVO、Controller / Service / Mapper 关系。

重点说明：AiServiceImpl、TopicProfile、LlmConfigServiceImpl、LlmReviewServiceImpl、ReviewRecordServiceImpl。

### 5.4 时序图设计

建议画：登录、普通检索、多学科智能检索、相似推荐、离线综述生成、在线 LLM 综述生成、管理员文献管理、重建索引、LLM API 配置测试。

---

## 6 核心功能专项设计

### 6.1 文献检索模块设计

写：普通检索基于 MySQL 模糊查询；高级检索支持标题、作者、期刊、DOI、分类、文献类型、年份范围和排序；支持分页、分类筛选和首页分类跳转。

### 6.2 多学科智能语义检索模块设计

#### 6.2.1 基础语义召回

- backend-ai 采用 sentence-transformers 模型生成语义向量。
- FAISS 使用向量相似度进行候选文献召回。
- 文献向量化字段包括标题、分类、文献类型、关键词、摘要和正文节选。

#### 6.2.2 扩大召回与候选池构建

- Spring Boot 内部计算 recallTopK，如 `min(max(topK * 5, 100), 150)`。
- backend-ai 返回 FAISS 候选后，Spring Boot 回查 MySQL 获取完整文献信息。

#### 6.2.3 多学科主题画像识别

支持教育、医学健康、心理健康、文献检索、农业生态、法学治理、经济管理、人文艺术、工程技术、自然科学和人工智能通用主题。

TopicProfile 包含：code、label、detectWords、expansionWords、categoryWeights、isSpecific。

#### 6.2.4 Query Expansion 查询扩展

- 根据 query 命中的主题，自动加入该主题扩展词。
- 扩展词参与 keywordScore 和 MySQL 关键词补充召回。

#### 6.2.5 MySQL 关键词补充召回

- 用扩展词在 title、keywords、abstract_text、content 中匹配。
- 补充召回结果与 FAISS 候选按 literatureId 去重。
- 没有 FAISS similarity 的补充文献设默认相似度。

#### 6.2.6 综合重排 finalScore

```text
finalScore = semanticSimilarity * 0.60
           + keywordScore * 0.30
           + categoryScore * 0.10
           - weakPenalty
```

说明：semanticSimilarity 是原始向量相似度；keywordScore 是字段命中得分；categoryScore 是主题与分类相关权重；weakPenalty 用于弱相关分类降权。

#### 6.2.7 AI_GENERAL 与具体学科主题关系

当 query 同时命中人工智能通用主题和具体学科主题时，具体学科优先。例如“人工智能在医学中的应用”应优先医学信息学、临床医学、健康管理等文献。

#### 6.2.8 推荐原因 matchReason

包含语义相似度、是否来自关键词补充召回、命中主题、命中扩展词、分类相关性、是否弱相关降权。

### 6.3 相似文献推荐模块设计

写：文献详情页调用 `/api/ai/recommend/{id}`；backend-ai 根据当前文献向量检索相似文献；排除自身；返回相似文献和相似度。

### 6.4 综述生成模块设计

写：用户输入主题；系统智能推荐相关文献；用户选择 2～5 篇；选择离线/在线生成方式；后端生成六段式综述；保存 review_record 和 generation_mode。

### 6.5 离线综述生成与在线 LLM 增强生成设计

#### 6.5.1 离线综述生成

- 默认生成方式。
- 不依赖外部网络和 API。
- 基于主题识别、关键词提取、方向分配、问题池和趋势池生成六段式综述。
- 作为在线 LLM 失败时的兜底方案。

#### 6.5.2 在线 LLM 增强生成

- 管理员配置 LLM API 后可用。
- 兼容 OpenAI-compatible Chat Completions API。
- 后端读取 active 且 enabled 的 LLM 配置。
- Prompt 要求模型只能基于用户选择的文献生成，不得编造文献、作者、期刊和年份。
- 输出必须包含六部分：研究背景、研究现状、主要研究方向、存在问题、发展趋势、参考文献来源。

#### 6.5.3 LLM 输出完整性校验

- 解析 finish_reason。
- 如果 finish_reason=length，可提高 max_tokens 重试一次。
- 检查前五个正文部分是否完整。
- 检查结尾是否明显截断。
- 第六节参考文献来源可以由后端补全。
- 清理不存在的引用编号。
- 正文严重不完整时降级为离线规则生成。

#### 6.5.4 参考文献后端补全

- 后端根据数据库文献信息构造标准参考文献列表。
- 如果 LLM 未输出“六、参考文献来源”，则追加该章节。
- 如果 LLM 输出的参考文献不完整，则替换第六节。
- 参考文献编号必须与用户选择的文献一一对应。

#### 6.5.5 自动降级机制

降级场景：无 active 配置、API Key 为空、连接失败、超时、非 200、JSON 解析失败、返回空、finish_reason=length 且重试失败、前五节正文缺失或明显截断。

降级结果：调用离线生成，generationMode=`llm_fallback_rule`，前端显示降级提示。

### 6.6 智能检索与综述生成联动设计

写：智能检索是综述生成前置能力；生成不是从全部文献盲选，而是从语义相关、主题相关候选文献中选择；离线和在线模式均基于用户选择的文献材料。

### 6.7 RAG 思想在系统中的应用

写：先检索相关文献，再基于用户选择的文献生成综述；在线 LLM 也受限于检索材料，不允许凭空生成参考文献；后端参考文献补全和引用编号校验提高可追溯性。

### 6.8 AI 辅助功能边界说明

写：智能检索不是真实大型学术搜索引擎；文献数据为模拟数据；在线 LLM 用于提升表达质量但不替代人工学术判断；API Key 当前为课程演示存储，生产环境应加密。

---

## 7 详细设计

### 7.1 用户管理模块详细设计

写：注册、登录、JWT 认证、用户角色、权限控制、管理员与普通用户权限区分。

### 7.2 文献管理模块详细设计

写：新增、编辑、删除、字段回显、分类选择、文献类型、来源链接、正文节选维护；文献变化后需要重建智能索引。

### 7.3 文献检索模块详细设计

写：普通检索、高级检索、分类筛选、年份范围、分页、首页分类跳转、二级分类真实统计。

### 7.4 智能检索模块详细设计

写：`/search` 普通/智能切换；调用 `aiApi.semanticSearch`；后端流程包括 recallTopK、FAISS 候选、主题识别、query expansion、MySQL 补充召回、finalScore 重排、返回 similarity/finalScore/matchReason。

### 7.5 综述生成模块详细设计

写：主题输入、智能推荐参考文献、选择 2～5 篇、选择生成方式、离线生成流程、在线 LLM 生成流程、generationMode 返回与保存、综述记录显示。

### 7.6 LLM API 管理模块详细设计

写：管理员路径 `/admin/llm-configs`；后端路径 `/api/admin/llm-configs/**`；功能包括列表、新增、编辑、删除、设为当前、测试连接；字段包括 name、provider、baseUrl、model、apiKey、enabled、active、timeoutSeconds、remark；API Key 脱敏展示；仅管理员可访问。

DeepSeek 示例配置：Provider=deepseek，Base URL=https://api.deepseek.com，Model=deepseek-chat 或 deepseek-v4-flash，Timeout 建议 180～240 秒。

### 7.7 管理员功能模块详细设计

写：文献管理、分类管理、用户管理、数据统计、重建智能检索索引、LLM API 管理。

### 7.8 关键算法设计

包括：普通检索匹配算法、高级检索筛选算法、文献向量化算法、FAISS 语义召回算法、多学科主题识别算法、Query Expansion、MySQL 关键词补充召回、finalScore 综合排序、相似文献推荐、离线综述生成、在线 LLM 综述生成、LLM 降级算法。

### 7.9 输入输出设计

写：登录、普通检索、智能检索、索引重建、相似推荐、离线综述生成、在线 LLM 综述生成、LLM API 配置、管理员文献管理输入输出。

### 7.10 界面原型设计

写：首页、检索页、高级检索页、详情页、综述生成双模式、综述记录 generationMode 标签、管理员后台、重建智能索引、LLM API 管理页。

### 7.11 代码规范说明

写：前端组件命名、API 封装、Controller/Service/Mapper 分层、DTO/VO 命名、统一 Result、异常处理、API Key 不打印日志且返回前端时脱敏。

---

## 8 编码实现

### 8.1 开发环境

写：Windows、JDK、Maven、Node.js、Python、MySQL、VS Code / IntelliJ IDEA。

### 8.2 运行环境

写：MySQL 3306、backend-ai 8000、backend-java 8080、frontend 5173、DeepSeek / OpenAI-compatible API。

### 8.3 技术栈说明

写：React + Vite + TypeScript、Tailwind CSS / shadcn/ui、Spring Boot + MyBatis-Plus、MySQL、FastAPI、sentence-transformers、FAISS、JWT、DeepSeek API / OpenAI-compatible Chat Completions。

### 8.4 项目目录结构

写：frontend、backend-java、backend-ai、database、docs。

### 8.5 前端实现

写：路由设计、request.ts、literatureApi / aiApi / reviewApi / llmConfigApi、检索页、详情页、综述生成双模式、综述记录 generationMode 标签、管理员重建索引、管理员 LLM API 管理。

### 8.6 Java 后端实现

写：Controller、Service、Mapper、JWT、AiServiceImpl、ReviewRecordServiceImpl、LlmConfigServiceImpl、LlmReviewServiceImpl、Result、GlobalExceptionHandler。

### 8.7 Python 智能检索服务实现

写：FastAPI 接口 `/health`、`/rebuild-index`、`/semantic-search`、`/recommend`；模型；FAISS 索引；id_map。

### 8.8 数据库初始化与数据导入

写：init.sql、update_categories.sql、literature_seed_500_high_quality.sql、update_llm_config.sql；llm_config 表；review_record.generation_mode 字段；数据导入后重建索引。

### 8.9 LLM API 配置与调用实现

写：管理员配置 LLM API；配置字段；DeepSeek 示例；调用 `POST {baseUrl}/chat/completions`；请求头 Authorization；请求体 model/messages/temperature/max_tokens；输出校验 finish_reason、章节完整性、参考文献完整性、引用编号合法性。

### 8.10 核心代码片段

建议放：backend-ai 向量化与 FAISS；Spring Boot 调 backend-ai；TopicProfile；finalScore；MySQL 补充召回；离线 buildReviewContent；LlmReviewService 调 API；ensureReferencesComplete；前端双模式生成；管理员 LLM 配置管理。

### 8.11 版本管理说明

写：Git 阶段提交，覆盖 MVP、分类数据、backend-ai、前后端智能检索、综述生成、多学科重排、LLM API 管理、LLM 输出完整性修复、文档测试。

---

## 9 系统测试

### 9.1 测试目标

写：验证普通检索、高级检索、多学科智能检索、离线综述生成、在线 LLM 综述生成、降级机制、综述记录 generationMode、管理员权限、索引重建、LLM API 配置。

### 9.2 测试环境

写：Windows、MySQL、backend-ai、backend-java、frontend、Chrome/Edge、curl/PowerShell、DeepSeek API 配置。

### 9.3 测试计划

写：单元测试、接口测试、集成测试、页面测试、权限测试、多学科智能检索测试、离线综述生成测试、在线 LLM 综述生成测试、LLM 降级测试、LLM API 管理测试。

### 9.4 测试方法

写：`python -m py_compile main.py`、`mvn clean package -DskipTests`、`npx tsc --noEmit`、`npm run build`、curl、浏览器手动测试、管理员后台测试 LLM 连接。

### 9.5 测试用例设计

重点测试：登录、普通检索、高级检索、智能检索、多学科智能检索、相似推荐、离线综述、在线 LLM 综述、LLM 输出完整性、LLM 降级、综述记录 generationMode、LLM API 管理、文献管理、分类管理、用户管理、数据导入、索引重建。

### 9.6 测试结果分析

写：主流程稳定；智能检索能返回目标学科文献；离线生成速度快；在线 LLM 生成语言更自然；LLM 失败时能补全参考文献或降级；综述记录显示生成方式；管理员能够配置 LLM API。

### 9.7 缺陷记录与修复说明

可写：分类统计为 0、分类跳转参数错误、纯向量检索语义泛化、医学主题检索跑偏、综述模板化、LLM 生成耗时长、LLM 输出截断、LLM 参考文献不完整、缺第六节被误判截断、PowerShell curl JSON 转义问题。

---

## 10 系统部署与用户手册

### 10.1 系统安装环境

写：JDK、Maven、Node.js、Python、MySQL、Git、DeepSeek API Key（如需在线 LLM）。

### 10.2 数据库部署步骤

写：创建数据库；执行 init.sql、update_categories.sql、update_llm_config.sql；导入 500 条文献；检查 llm_config 表和 review_record.generation_mode。

### 10.3 backend-ai 智能检索服务部署步骤

```bash
cd backend-ai
pip install -r requirements.txt
uvicorn main:app --reload --port 8000
```

### 10.4 Spring Boot 后端服务部署步骤

```bash
cd backend-java
mvn spring-boot:run
```

### 10.5 React 前端服务部署步骤

```bash
cd frontend
npm run dev
```

### 10.6 系统启动顺序说明

顺序：MySQL → backend-ai → backend-java → frontend → 管理员重建智能索引 → 如需在线 LLM，管理员配置并激活 LLM API。

### 10.7 普通用户操作说明

写：登录注册、普通检索、高级检索、智能检索、详情、相似推荐、收藏、选择离线/在线生成综述、查看和筛选综述记录。

### 10.8 管理员操作说明

写：管理文献、分类、用户、统计，重建智能索引，进入“LLM API 管理”配置在线综述生成使用的大模型 API。

### 10.9 LLM API 配置说明

DeepSeek 示例：配置名称 DeepSeek Academic Review；Provider deepseek；Base URL `https://api.deepseek.com`；Model `deepseek-chat` 或 `deepseek-v4-flash`；Timeout 建议 180～240 秒；Enabled 开启；Active 设为当前。

注意：API Key 不写进前端代码或 Git；前端只显示脱敏 Key；在线 LLM 不可用时系统自动降级离线生成。

### 10.10 常见问题及解决方法

写：backend-ai 未启动、端口占用、MySQL 乱码、索引未重建、LLM API 测试失败、在线 LLM 耗时长、在线 LLM 自动降级、页面缓存。

---

## 11 软件维护计划

### 11.1 纠错性维护

修复运行错误、接口异常、页面显示问题、主题词表遗漏、LLM API 调用异常和输出完整性问题。

### 11.2 适应性维护

适配 Python 包、Spring Boot、Node 版本变化，适配数据库迁移和不同 OpenAI-compatible 大模型服务商。

### 11.3 完善性维护

增加 PDF 上传、真实全文解析、真实论文库、BM25 + 向量混合检索、Cross-Encoder Rerank、流式 LLM 输出。

### 11.4 数据维护计划

定期更新文献数据，检查分类关联，清理无效记录，新增文献后重建智能索引。

### 11.5 智能检索索引维护计划

文献数据变化后重建索引，定期检查 `faiss.index` 和 `id_map.json`，维护主题词表和分类权重。

### 11.6 LLM API 配置与密钥维护计划

定期测试 active LLM 配置；API Key 前端脱敏显示；生产环境应加密存储、设置密钥轮换、访问审计和调用限额；LLM 不可用时保留离线规则生成兜底。

### 11.7 后续功能扩展计划

BM25 + 向量混合检索、Rerank、大模型流式输出、引用校验、PDF 上传与解析、Docker 部署、关键词词云、关键词共现网络、作者合作网络。

---

## 12 项目总结

### 12.1 项目成果总结

写：完成前后端分离系统、普通用户端和管理员端、500 条多学科模拟文献、两级分类体系、backend-ai、Spring Boot AI 接入、多学科智能检索、相似推荐、离线规则综述、在线 LLM 增强综述、管理员 LLM API 管理、综述记录生成方式追踪。

### 12.2 项目特色与创新点

写：两级分类、高质量模拟文献库、语义向量检索、多学科主题画像、查询扩展、MySQL 补充召回、finalScore、matchReason、RAG 风格综述、离线 + 在线双模式、LLM API 管理、Key 脱敏、LLM 输出完整性校验、参考文献补全和降级机制。

### 12.3 存在的问题与不足

写：文献数据仍为模拟数据；未接入真实数据库；离线综述表达受规则限制；在线 LLM 依赖外部 API 且响应较慢；主题画像词表需维护；未引入 Cross-Encoder Rerank；课程演示中 API Key 明文存储，生产环境需加密；未完成 Docker 化。

### 12.4 后续展望

写：接入真实文献数据库、PDF 上传与全文解析、大模型流式输出、BM25 + 向量混合检索、Cross-Encoder 精排、引用校验、关键词词云、API Key 加密和 Docker 部署。

### 12.5 小组成员自评

写每个人贡献和自评。

---

## 13 结论

写作要点：项目完成完整软件工程流程；实现文献检索、智能检索、相似推荐和综述生成；多学科主题画像提高跨学科检索相关性；“先检索、再选择、再生成”体现 RAG 思想；离线规则生成和在线 LLM 增强生成在稳定性与表达质量之间取得平衡。

---

## 参考文献

建议方向：软件工程教材、Spring Boot、React/Vite、MyBatis-Plus、FastAPI、sentence-transformers、FAISS、RAG、信息检索、DeepSeek API / OpenAI-compatible API 文档。

---

## 附录

### 附录 A 核心代码

放 backend-ai 向量化与 FAISS、Spring Boot 调 backend-ai、TopicProfile、finalScore、离线综述生成、LLM 调用、ensureReferencesComplete、前端双模式生成、管理员 LLM 配置管理代码。

### 附录 B 详细测试报告

放测试表格和结果截图。

### 附录 C 数据库建表语句

放 user、literature、category、favorite、review_record、llm_config 等表结构。

### 附录 D 接口说明文档

放认证、文献、智能检索、综述生成、LLM 配置管理、管理员接口。

### 附录 E 系统运行截图

放首页、普通检索、智能检索、详情、相似推荐、综述生成双模式、在线 LLM 结果、综述记录 generationMode、管理员后台、重建索引、LLM API 管理截图。

### 附录 F AI 辅助交互记录

说明 AI 在需求分析、技术路线讨论、Prompt 设计、代码生成与调试、测试思路和文档整理中的辅助作用，同时说明最终设计、运行验证和项目整合由小组完成。
