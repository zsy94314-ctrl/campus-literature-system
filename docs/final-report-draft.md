**软件工程三级项目**

**课程设计题目：校园学术文献智能检索与综述生成系统**

## 封面信息

| 项目 | 内容 |
| --- | --- |
| 学校 | 燕山大学 |
| 课程设计题目 | 校园学术文献智能检索与综述生成系统 |
| 专业班级 | 计算机专业 1 班 |
| 组别 | 第 3 组 |
| 组长 | 周思源 |
| 组员 | 黄文政、李俊宇、郝思怡、王淑珍 |
| 日期 | 2026 年 5 月，正式提交时按实际日期填写 |

封面成员信息如下，正式 Word 文档可按老师模板调整为居中封面格式。

| 序号 | 学号 | 姓名 | 班级 | 角色 | 自评成绩百分比 |
| --- | --- | --- | --- | --- | --- |
| 1 | 202411040239 | 周思源 | 计算机专业 1 班 | 组长 | 40% |
| 2 | 202411040232 | 黄文政 | 计算机专业 1 班 | 组员 | 18% |
| 3 | 202411040254 | 李俊宇 | 计算机专业 1 班 | 组员 | 16% |
| 4 | 202411040237 | 郝思怡 | 计算机专业 1 班 | 组员 | 14% |
| 5 | 202411040256 | 王淑珍 | 计算机专业 1 班 | 组员 | 12% |

正式排版时建议使用 A4 页面打印，左侧装订，不设置页眉，设置页脚页码，并在封面中保留课程设计题目、组号、组长、组员、班级、学号、自评成绩百分比和日期等信息。

## 目录

正式 Word 文档中目录由 Word 自动生成，目录只保留三级标题，不在 Markdown 中手动写死页码。


## 摘要

随着高校课程学习和科研训练中学术资料数量不断增加，传统关键词检索在自然语言理解、跨学科主题识别和综述整理方面存在一定不足。本文设计并实现了一个校园学术文献智能检索与综述生成系统。系统采用 React + Vite + TypeScript 构建前端，采用 Spring Boot + MyBatis-Plus + MySQL 构建主后端，采用 FastAPI + sentence-transformers + FAISS 构建智能语义检索服务。

系统支持用户注册登录、文献普通检索、高级检索、多学科智能语义检索、文献详情、收藏、相似文献推荐、智能推荐参考文献、离线综述生成、在线 LLM 增强综述生成以及综述记录管理。管理员端支持文献管理、分类管理、用户管理、数据统计、重建智能检索索引和 LLM API 配置管理。智能检索部分在 FAISS 语义召回基础上，增加多学科主题画像、Query Expansion、MySQL 关键词补充召回、`keywordScore`、`categoryScore`、`weakPenalty`、`finalScore` 和 `matchReason`，缓解跨学科检索跑偏问题。综述生成部分支持离线稳定模式和在线 LLM 增强模式，在线不可用或输出不完整时自动降级为离线生成。

系统文献数据为课程项目模拟数据，不是真实论文库。在线 LLM 只作为增强模式，离线综述生成是默认稳定方案。系统对 LLM 输出进行完整性校验，并由后端补全参考文献来源，避免模型编造不存在的参考文献。

**关键词：** 学术文献检索；多学科语义检索；综述生成；FAISS；LLM API；Spring Boot

## Abstract

With the increasing number of academic materials in university courses and research training, traditional keyword retrieval has limitations in natural language understanding, cross-disciplinary topic identification, and literature review compilation. This paper designs and implements a campus academic literature intelligent retrieval and review generation system. The frontend is built with React + Vite + TypeScript, the main backend with Spring Boot + MyBatis-Plus + MySQL, and the intelligent semantic retrieval service with FastAPI + sentence-transformers + FAISS.

The system supports user registration and login, literature keyword retrieval, advanced retrieval, multi-disciplinary intelligent semantic retrieval, literature detail viewing, favorites, similar literature recommendation, intelligent reference recommendation, offline rule-based review generation, online LLM-enhanced review generation, and review record management. The admin panel supports literature management, category management, user management, real-data statistics, intelligent index rebuilding, and LLM API configuration management. The intelligent retrieval module improves cross-disciplinary relevance through topic profiling, query expansion, MySQL keyword supplement recall, and a comprehensive re-ranking algorithm with `finalScore` and `matchReason`. The review generation module adopts a dual-mode design where offline rule-based generation serves as the stable default and online LLM generation serves as an enhancement, with automatic fallback when LLM is unavailable.

All literature data in the system are simulated course project data, not a real paper database. The administrator statistics panel is based on real database aggregation, without mock trend data.

**Keywords:** academic literature retrieval; multi-disciplinary semantic retrieval; literature review generation; FAISS; LLM API; Spring Boot

---

## 1 引言

### 1.1 项目背景

高校师生在课程学习、论文写作和科研训练中需要检索、筛选和整理大量学术文献。传统关键词检索虽然实现简单，但对自然语言研究主题和跨学科语义关系的理解能力有限。仅依赖向量语义检索也可能在“人工智能”等宽泛主题下出现学科泛化，导致结果与用户真实意图不完全一致。

另一方面，文献综述撰写需要阅读多篇文献并综合归纳研究背景、现状、方向、问题和趋势，人工完成耗时较长。大模型可改善语言表达和归纳质量，但在线 API 存在网络依赖、成本、超时和输出截断等风险。因此，本系统采用“离线规则生成为默认稳定模式，在线 LLM 生成为增强模式”的双模式方案。

### 1.2 项目目标

本项目目标是实现一个可运行、可演示、可维护的校园学术文献智能检索与综述生成系统，具体包括：

- 实现用户注册、登录和权限控制。
- 实现文献普通检索、高级检索和详情查看。
- 实现多学科智能语义检索和相似文献推荐。
- 实现文献收藏和我的收藏。
- 实现智能推荐参考文献，并支持用户选择 2 到 5 篇文献生成综述。
- 实现离线综述生成和在线 LLM 增强综述生成。
- 实现综述记录 `generationMode` 标签和筛选。
- 实现管理员文献、分类、用户、统计、索引重建和 LLM API 管理。

### 1.3 项目意义

对普通用户而言，系统可以提高文献发现、筛选、收藏和综述整理效率。对管理员而言，系统提供了模拟文献库维护、学科分类维护、用户管理、统计和智能索引维护能力。对课程项目而言，系统体现了需求分析、系统设计、编码实现、测试验证和文档整理的软件工程全过程。

### 1.4 系统特色

系统特色主要体现在：

- 使用课程项目模拟文献数据，覆盖多学科分类。
- 使用两级学科分类体系，包含 6 个一级分类和 37 个二级分类。
- 使用 sentence-transformers 与 FAISS 实现基础语义召回。
- 在 Spring Boot 中实现多学科主题画像和综合重排。
- 使用 `matchReason` 提供推荐原因解释。
- 支持离线综述生成和在线 LLM 增强综述生成。
- 后端校验 LLM 输出完整性并补全参考文献。
- 管理员可配置、激活和测试 LLM API，前端只展示脱敏 Key。
- 管理员数据统计页面基于数据库真实数据，展示核心指标、分类分布、年份分布、文献类型分布、综述生成方式分布、检索类型分布和 LLM 状态，不使用假趋势数据。

### 1.5 参考资料

本项目设计与实现参考了软件工程课程教材、Spring Boot 官方文档、React 官方文档、MySQL 官方文档、FastAPI 官方文档、FAISS 相似度检索资料、sentence-transformers 文本向量化资料以及 RAG/LLM 工程化接入相关论文和在线文档。项目文档还参考了课程提供的《软件工程三级项目设计文档》结构和《软件工程 A 三级项目实施方案》，在报告中补充了数据流图、数据字典、接口设计、测试计划、维护计划、用户手册和小组分工等内容。

## 2 可行性分析

### 2.1 技术可行性

前端使用 React + Vite + TypeScript，能够快速构建单页应用和管理后台。后端使用 Spring Boot + MyBatis-Plus，适合 REST API、权限控制和 MySQL 数据访问。智能检索服务独立为 FastAPI，便于使用 Python 生态中的 sentence-transformers 和 FAISS。Java 后端通过 HTTP 调用 backend-ai，模块边界清晰。

在线 LLM 使用 OpenAI-compatible Chat Completions API，管理员可配置 DeepSeek 或其他兼容服务。在线生成失败时系统自动降级为离线规则生成，降低外部服务不稳定带来的风险。

### 2.2 经济可行性

系统主要依赖开源框架和本地开发环境。文献数据为课程项目模拟数据，不需要购买真实文献数据库。在线 LLM 是可选增强功能，没有 API Key 时仍可使用离线综述生成。

### 2.3 操作可行性

普通用户流程清晰：检索文献、查看详情、收藏、查看相似推荐、输入主题、智能推荐参考文献、选择生成方式、查看综述记录。管理员流程清晰：维护文献和分类、管理用户、查看统计、重建智能索引、维护 LLM API 配置。

### 2.4 安全与规范可行性

系统使用 JWT 管理登录态，使用角色字段区分普通用户和管理员。用户密码由后端使用 BCrypt 保存。LLM API Key 前端脱敏展示，真实 Key 不应写入文档和仓库。生产环境应进一步采用加密存储、密钥轮换和访问审计。

### 2.5 可行性结论

综合技术、经济、操作和安全规范分析，本系统作为软件工程课程三级项目具备实施可行性。项目采用前后端分离、MySQL 结构化存储、FastAPI 智能检索服务和可选在线 LLM 增强方式，各模块边界清晰，能够在本地开发环境中完成部署、演示和测试。系统文献数据为课程项目模拟数据，离线综述生成是默认稳定方案，在线 LLM 只作为增强能力，因此即使没有可用大模型 API，系统仍能完成主要业务流程。

### 2.6 同类系统或竞品分析

与高校图书馆检索系统、知网/万方等论文检索平台相比，本系统的数据规模和权威性不能替代真实论文数据库，但更适合课程项目展示完整的软件工程流程。与通用大模型问答工具相比，本系统不允许模型自由编造参考文献，而是基于用户选择的模拟文献材料生成综述，并由后端补全参考来源。系统的特点是将普通检索、高级检索、语义检索、相似推荐、综述生成和后台管理整合到一个可运行的课程项目中，便于展示需求分析、设计、实现、测试和维护的完整过程。

## 3 需求分析

### 3.1 用户角色

普通用户功能包括注册登录、文献检索、智能检索、文献详情、收藏、相似推荐、智能推荐参考文献、离线或在线综述生成、综述记录查看和筛选。

管理员功能包括文献管理、分类管理、用户管理、数据统计、重建智能检索索引和 LLM API 管理。

此处插入图 5-1 普通用户用例图，详见 `docs/diagrams.md`。

此处插入图 5-2 管理员用例图，详见 `docs/diagrams.md`。

### 3.2 功能需求

文献检索支持普通关键词检索和高级检索。普通检索匹配标题、摘要、关键词和正文节选。高级检索支持标题、作者、期刊、DOI、分类、文献类型、年份范围和排序。

智能检索支持自然语言查询，后端返回语义相关文献、相似度、综合分和推荐原因。相似推荐支持在文献详情页根据当前文献向量推荐相近文献。

综述生成支持用户输入主题，系统先智能推荐候选文献，用户勾选 2 到 5 篇后生成综述。离线模式不依赖外部 API，在线 LLM 模式调用管理员配置的大模型 API。

### 3.3 非功能需求

系统需要具备可用性、可靠性、安全性和可维护性。backend-ai 未启动、索引未重建、LLM API 不可用等异常应给出明确提示或自动降级。敏感信息不能出现在前端和文档中。

### 3.4 业务流程分析

普通用户主要流程为：注册或登录系统，使用普通检索、高级检索或智能检索查找文献，进入文献详情页查看摘要、关键词和相似推荐，根据需要收藏文献；在综述生成页面输入主题，系统智能推荐参考文献，用户选择 2 到 5 篇后选择离线或在线模式生成综述，最后在综述记录中查看 `generationMode` 标签和筛选结果。

管理员主要流程为：登录后台，维护文献、分类和用户，查看真实数据统计面板；当文献数据发生变化后，管理员通过后台入口重建智能检索索引；如需使用在线 LLM 增强综述生成，管理员可维护 LLM API 配置、设置 active 配置并测试连接。

### 3.5 数据流图说明

本系统的数据流图按顶层图、0 层图和 1 层图组织，完整 Mermaid 图表见 `docs/diagrams.md`。

此处插入图 3-1 顶层数据流图，详见 `docs/diagrams.md`。

此处插入图 3-2 0 层数据流图，详见 `docs/diagrams.md`。

此处插入图 3-3 智能检索 1 层数据流图，详见 `docs/diagrams.md`。

此处插入图 3-4 综述生成 1 层数据流图，详见 `docs/diagrams.md`。

顶层 DFD 中，外部实体包括普通用户、管理员和在线 LLM API；系统内部通过 Spring Boot 后端协调 MySQL 数据库、backend-ai 服务和 FAISS 索引文件。0 层 DFD 将系统拆分为用户认证、文献检索、智能检索、收藏管理、综述生成、后台管理和 LLM API 管理等处理过程。1 层图重点展开智能检索和综述生成两个核心流程。

### 3.6 数据字典说明

数据字典用于说明系统中的主要数据项、数据流、数据存储和数据处理，详细数据库字段见 `docs/database-design.md`。

**表 3-1 数据字典概要**

| 类别 | 名称 | 说明 |
| --- | --- | --- |
| 数据项 | 用户编号、用户名、角色 | 标识系统用户及其权限类型 |
| 数据项 | 文献编号、标题、作者、摘要、关键词、分类编号 | 描述文献基本信息和所属学科分类 |
| 数据项 | 综述编号、`generationMode` | 标识综述记录及其生成方式 |
| 数据项 | LLM 配置编号 | 标识管理员维护的大模型 API 配置 |
| 数据流 | 登录请求 | 用户名和密码提交到认证接口 |
| 数据流 | 检索请求 | 普通检索、高级检索或智能检索条件 |
| 数据流 | 智能检索候选结果 | backend-ai 和 MySQL 补充召回后的候选文献 |
| 数据流 | 收藏请求 | 用户对文献执行收藏或取消收藏操作 |
| 数据流 | 综述生成请求 | 主题、文献 ID 列表和生成模式 |
| 数据流 | 索引重建请求 | 管理员触发文献数据同步与 FAISS 索引重建 |
| 数据存储 | `user`、`literature`、`category`、`favorite`、`search_history` | 用户、文献、分类、收藏和检索历史数据 |
| 数据存储 | `review_record`、`llm_config` | 综述记录和 LLM API 配置数据 |
| 数据存储 | FAISS 索引文件 | backend-ai/data 中的向量索引和 ID 映射文件 |
| 数据处理 | 用户认证、普通检索、高级检索、智能检索、相似推荐 | 检索与推荐相关业务处理 |
| 数据处理 | 综述生成、LLM 输出校验、索引重建、后台统计 | 综述、索引和后台管理相关处理 |

## 4 系统总体设计

### 4.1 系统架构

此处插入图 4-1 系统总体架构图，详见 `docs/diagrams.md`。

### 4.2 功能结构

此处插入图 4-2 功能结构图，详见 `docs/diagrams.md`。

普通用户端包括首页、普通检索、高级检索、智能检索、文献详情、收藏、综述生成和综述记录。管理员端包括管理首页、文献管理、分类管理、用户管理、数据统计、智能索引重建和 LLM API 管理。

### 4.3 数据库设计

此处插入图 4-3 数据库 E-R 图，详见 `docs/diagrams.md`。

核心表包括：

- `user`
- `category`
- `literature`
- `favorite`
- `search_history`
- `review_record`
- `llm_config`
- `admin_log`
- `literature_vector`

`category.parent_id` 支持两级分类。`review_record.generation_mode` 记录综述生成方式。`llm_config.api_key` 后端保存，前端脱敏展示；生产环境应加密存储。FAISS 索引文件不存 MySQL，而是保存在 `backend-ai/data`。

### 4.4 两级学科分类设计

系统分类表通过 `category.parent_id` 实现两级学科分类。一级分类用于归纳学科大类，二级分类用于绑定具体文献。当前数据库脚本提供 6 个一级分类和 37 个二级分类，500 条课程项目模拟文献均绑定到二级分类。该设计便于普通检索、高级检索、分类统计和智能检索中的 `categoryScore` 计算。

### 4.5 接口总体设计

前端通过统一请求封装访问 Spring Boot 的 `/api/**` 接口。Spring Boot 负责用户认证、文献检索、收藏、分类、综述生成、管理员后台和 LLM API 管理，并在智能检索和相似推荐场景下调用 backend-ai。backend-ai 提供 `/health`、`/rebuild-index`、`/semantic-search` 和 `/recommend` 接口。在线 LLM 增强综述生成通过 OpenAI-compatible Chat Completions API 调用管理员配置的 active LLM 服务。详细接口路径、请求参数和响应字段见 `docs/api.md`。

### 4.6 模块结构图

系统模块结构图可与功能结构图配合说明：普通用户端包括文献检索、文献详情、收藏、智能推荐参考文献、综述生成和综述记录；管理员端包括文献管理、分类管理、用户管理、数据统计、索引重建和 LLM API 管理；backend-ai 智能检索服务包括语义向量化、FAISS 索引管理、语义召回和相似推荐；在线 LLM 增强服务包括 active 配置读取、API 调用、输出校验和降级处理。

### 4.7 接口设计

接口设计遵循前后端分离原则。普通用户接口主要包括 `/api/auth`、`/api/literatures`、`/api/favorites`、`/api/categories`、`/api/ai`、`/api/reviews` 和 `/api/search-history`。管理员接口包括文献、分类、用户、统计、索引重建和 `/api/admin/llm-configs`。管理员 LLM API 管理接口支持新增、编辑、删除、设置 active、测试连接和长文本测试。涉及 API Key 的返回值只展示脱敏字段，文档和截图中不写入真实密钥。

## 5 核心功能设计

### 5.1 文献检索

普通检索由 `/api/literatures/search` 提供，支持关键词、作者、分类、年份、文献类型和分页。高级检索复用同一接口，增加标题、期刊、DOI、起止年份等条件。

### 5.2 多学科智能语义检索

智能检索不是简单 FAISS 检索，而是多阶段流程：

此处插入图 6-1 智能检索流程图，详见 `docs/diagrams.md`。

1. backend-ai 使用 sentence-transformers 生成语义向量。
2. FAISS 进行基础语义召回。
3. Spring Boot 扩大召回候选池。
4. 识别多学科主题画像。
5. Query Expansion 查询扩展。
6. MySQL 关键词补充召回。
7. 计算 `keywordScore`。
8. 计算 `categoryScore`。
9. 使用 `weakPenalty` 弱相关降权。
10. 计算 `finalScore` 综合排序。
11. 返回 `matchReason` 推荐原因解释。

主题画像包括教育主题、医学健康主题、心理健康主题、文献检索主题、农业生态主题、法学治理主题、经济管理主题、人文艺术主题、工程技术主题、自然科学主题和人工智能通用主题。

### 5.3 相似文献推荐

文献详情页调用 `/api/ai/recommend/{literatureId}`。Spring Boot 再调用 backend-ai `/recommend`，backend-ai 根据当前文献向量检索相似文献并排除自身。

### 5.4 综述生成双模式

此处插入图 6-2 综述生成流程图，详见 `docs/diagrams.md`。

离线综述生成：

- 请求 `mode=rule`。
- 记录 `generationMode=rule`。
- 默认稳定模式。
- 不依赖外部 API。
- 基于主题识别、关键词提取、研究方向归纳、问题与趋势生成。

在线 LLM 增强综述生成：

此处插入图 6-3 在线 LLM 综述生成时序图，详见 `docs/diagrams.md`。

- 请求 `mode=llm`。
- 成功时记录 `generationMode=llm`。
- 调用管理员配置的 active LLM API。
- 支持 DeepSeek / OpenAI-compatible API。

自动降级：

- 用户请求 `mode=llm`。
- 若 LLM 不可用、超时、返回空、正文不完整或明显截断，则自动调用离线生成。
- 记录 `generationMode=llm_fallback_rule`。

### 5.5 LLM 输出校验

后端校验包括：

- 检查 `finish_reason`。
- `finish_reason=length` 时重试一次。
- 检查前五个正文部分是否完整。
- 检查结尾是否明显截断。
- 第六节“参考文献来源”由后端强制补全或替换。
- 清理不存在的引用编号。
- 不允许 LLM 编造参考文献。

### 5.6 LLM API 管理

此处插入图 6-5 LLM API 管理流程图，详见 `docs/diagrams.md`。

管理员可新增、编辑、删除 LLM 配置，设置 active 配置，测试连接，执行长文本综述测试。配置字段包括名称、Provider、Base URL、Model、API Key、启用状态、active 状态、Timeout 和备注。API Key 前端仅展示脱敏值。

## 6 详细设计与实现

### 6.1 前端实现

前端使用 TanStack Router 管理路由。`src/api` 下封装认证、文献、收藏、分类、智能检索、综述、检索历史、管理员和 LLM 配置接口。`review-generate.tsx` 支持离线和在线生成方式切换，`review-history.tsx` 支持 generationMode 标签和筛选，`admin.llm-configs.tsx` 支持 LLM API 管理。

系统前端运行截图如下：

**图 6-6 首页运行效果**

![首页运行效果](docs/screenshots/01-home.png)

**图 6-7 普通检索运行效果**

![普通检索运行效果](docs/screenshots/02-search-normal.png)

**图 6-8 智能检索运行效果**

![智能检索运行效果](docs/screenshots/03-search-ai.png)

**图 6-9 文献详情与相似文献推荐**

![文献详情与相似文献推荐](docs/screenshots/04-literature-detail-recommend.png)

**图 6-10 综述生成双模式选择**

![综述生成双模式选择](docs/screenshots/05-review-generate-mode.png)

**图 6-11 在线 LLM 综述生成结果**

![在线 LLM 综述生成结果](docs/screenshots/06-review-llm-result.png)

**图 6-12 综述记录 generationMode 标签和筛选**

![综述记录 generationMode 标签和筛选](docs/screenshots/07-review-history-generation-mode.png)

### 6.2 Java 后端实现

Java 后端按 Controller、Service、Mapper、Entity、DTO、VO 分层。`JwtInterceptor` 对 `/api/**` 进行登录态解析和权限控制。`AiServiceImpl` 负责调用 backend-ai 并进行多学科重排。`ReviewRecordServiceImpl` 负责综述生成分流与记录保存。`LlmReviewServiceImpl` 负责在线 LLM 调用、输出校验和参考文献补全。

此处插入图 6-4 管理员重建智能索引时序图，详见 `docs/diagrams.md`。

### 6.3 Python 智能检索服务实现

backend-ai 使用 FastAPI 提供四个接口。重建索引时，将文献文本向量化并写入 FAISS。语义检索时，将查询向量化后检索相似文献。相似推荐时，使用目标文献向量查找相近文献。

### 6.4 数据库实现

数据库脚本位于 `database/`。`init.sql` 创建基础表，`update_categories.sql` 重建两级分类，`literature_seed_500_high_quality.sql` 导入 500 条模拟文献，`update_llm_config.sql` 创建 LLM 配置表并增加 `generation_mode` 字段。

### 6.5 管理员后台运行截图说明

管理员后台运行截图如下。涉及 LLM API 管理页面时，API Key 已做脱敏处理。

**图 6-13 管理员后台首页**

![管理员后台首页](docs/screenshots/08-admin-dashboard.png)

**图 6-14 重建智能索引成功提示**

![重建智能索引成功提示](docs/screenshots/09-admin-rebuild-index.png)

**图 6-15 LLM API 管理页面**

![LLM API 管理页面](docs/screenshots/10-admin-llm-config.png)

**图 6-16 LLM 测试连接成功**

![LLM 测试连接成功](docs/screenshots/11-llm-test-success.png)

**图 6-17 三服务启动与项目运行终端截图**

![三服务启动与项目运行终端截图](docs/screenshots/12-services-running.png)

> 注：该截图展示项目运行与 Git 提交记录等终端信息，用于说明系统运行和版本管理情况。

**图 6-18 管理员数据统计页面**

![管理员数据统计页面](docs/screenshots/20-admin-statistics.png)

> 注：管理员数据统计页面基于数据库真实统计，展示用户总数、文献总数、分类数量、收藏总数、综述生成数、检索记录数、LLM 配置数等核心指标，以及分类 Top 10 分布、文献年份分布、文献类型分布、综述生成方式分布、检索类型分布和当前 LLM API 状态，不使用模拟趋势数据。

### 6.6 面向对象设计

Java 后端采用典型分层设计。Entity 层对应数据库表，如 `User`、`Literature`、`Category`、`Favorite`、`SearchHistory`、`ReviewRecord`、`LlmConfig` 等；DTO 层承载登录、检索、综述生成和 LLM 配置等请求参数；VO 层负责向前端返回脱敏或聚合后的数据；Controller 层提供 REST API；Service 层实现业务逻辑；Mapper 层负责数据库访问；Security 相关类负责 JWT 拦截和用户上下文。系统中的实体关系主要通过字段进行逻辑关联，SQL 中未为所有关系显式声明外键。

### 6.7 关键算法设计

系统核心算法包括普通检索匹配、高级检索筛选、语义向量检索、相似文献推荐、智能检索重排、离线综述生成和在线 LLM 输出校验与降级。普通检索主要匹配标题、摘要、关键词和正文节选；高级检索在普通检索基础上增加标题、作者、期刊、DOI、分类、文献类型和年份范围等组合条件；语义检索由 backend-ai 使用 sentence-transformers 编码并通过 FAISS 召回；相似推荐基于目标文献向量检索相近文献并排除自身。

智能检索重排算法在 FAISS 候选基础上增加多学科主题画像、Query Expansion 和 MySQL 关键词补充召回，再计算 `keywordScore`、`categoryScore`、`weakPenalty` 和 `finalScore`。离线综述生成算法基于主题识别、关键词提取、研究方向归纳、问题与趋势生成和参考来源整理。在线 LLM 算法重点在输出控制：检查 `finish_reason`，校验前五节正文完整性，识别明显截断，补全或替换第六节参考文献来源，清理不存在的引用编号，失败时降级为 `llm_fallback_rule`。

### 6.8 输入输出设计

输入设计包括登录表单、注册表单、普通检索关键词、高级检索多字段条件、智能检索自然语言 query、综述主题、参考文献勾选列表、生成模式选择、管理员文献表单、分类表单、用户状态操作和 LLM API 配置表单。输出设计包括文献列表、文献详情、相似文献推荐、收藏状态、智能检索相似度、综述正文、`generationMode` 标签、管理员统计面板、索引重建提示和 LLM 测试结果。涉及敏感信息的输出必须脱敏，LLM API Key 不在前端完整展示，也不写入报告。

### 6.9 界面原型与运行截图说明

本项目以实际运行页面作为界面原型展示材料。首页展示系统入口、热门文献和学科分类；检索页展示普通检索和智能检索切换；详情页展示摘要、关键词、收藏和相似推荐；综述生成页展示主题输入、参考文献推荐和双模式选择；综述记录页展示 `generationMode` 标签和筛选；管理员后台展示统计、管理入口、索引重建和 LLM API 配置。完整截图见附录 D，截图清单见 `docs/screenshots-guide.md`。

### 6.10 代码规范说明

前端代码按 API 封装、路由页面和公共组件分层组织，使用 TypeScript 类型约束接口数据。Spring Boot 后端按 Controller、Service、Mapper、Entity、DTO、VO 和 Security 分层，Controller 只负责请求入口，核心业务逻辑放在 Service 层。backend-ai 使用 FastAPI 组织接口，向量索引文件统一放入 `backend-ai/data`。代码和文档中不写真实 API Key、数据库密码或 Token。

### 6.11 编码实现与代码结构说明

项目代码结构包括 `frontend/`、`backend-java/`、`backend-ai/`、`database/` 和 `docs/`。`frontend/src/routes` 存放页面路由，`frontend/src/api` 封装后端请求；`backend-java/src/main/java` 按实体、DTO、VO、控制器、服务、Mapper 和安全拦截等模块组织；`backend-ai/main.py` 提供健康检查、索引重建、语义检索和相似推荐接口；`database/` 存放建表、分类更新、模拟文献导入和 LLM 配置更新脚本；`docs/` 存放正式报告、接口、数据库、测试、图表和截图说明等文档。

## 7 系统测试

### 7.1 测试环境

测试环境包括 Windows、MySQL、JDK 17、Maven、Node.js、Python、Chrome 或 Edge 浏览器。backend-ai 默认端口 8000，backend-java 默认端口 8080，frontend 默认端口 5173。

### 7.2 测试计划

测试计划以本地课程项目环境为基础，采用构建检查、接口验证、页面操作和异常场景验证相结合的方式。测试对象包括 React 前端页面、Spring Boot 后端接口、MySQL 数据库、backend-ai 智能检索服务、FAISS 索引文件、在线 LLM API 管理和权限控制。测试重点是验证主业务流程可运行、关键异常可提示或降级、管理员功能有权限控制，完整测试用例见 `docs/test-cases.md`。

### 7.3 测试内容

测试覆盖：

- 编译构建测试
- 用户认证测试
- 文献检索测试
- 收藏测试
- 智能检索测试
- 相似推荐测试
- 离线综述生成测试
- 在线 LLM 综述生成测试
- LLM 降级测试
- 综述记录 generationMode 测试
- 管理员功能测试
- LLM API 管理测试
- 权限与异常处理测试

### 7.4 边界条件测试

边界条件测试包括：登录用户名或密码为空、密码错误、普通用户访问管理员页面、检索关键词为空、年份输入非法、智能检索 query 为空、backend-ai 未启动、FAISS 索引未重建、综述生成少于 2 篇文献、超过 5 篇文献、无 active LLM 配置、LLM 超时或输出不完整等。系统应对上述异常给出提示、拒绝非法操作或自动降级为离线综述生成。

### 7.5 缺陷分析与修复说明

**表 7-1 缺陷分析与修复说明**

| 缺陷现象 | 原因分析 | 修复与验证 |
| --- | --- | --- |
| 首页分类统计为 0 | 分类统计逻辑与实际分类数据不匹配 | 调整分类统计查询和前端展示后，首页可显示各分类文献数量 |
| 首页分类跳转不生效 | 首页分类入口与检索页参数同步不完整 | 检索页监听 URL 分类参数，点击分类后可自动检索 |
| 智能检索跨学科跑偏 | 纯语义召回容易被“人工智能”等宽泛词泛化 | 增加主题画像、Query Expansion、MySQL 补充召回和 `finalScore` 重排 |
| 综述生成模板化 | 早期规则生成表达固定、差异性不足 | 增加主题识别、关键词提取、研究方向归纳和问题趋势生成 |
| LLM 输出截断 | 在线模型输出 token 不足或 `finish_reason=length` | 检查 `finish_reason`，必要时提高 `max_tokens` 重试 |
| LLM 参考文献不完整 | 模型可能遗漏或编造参考来源 | 第六节由后端补全或替换，参考文献只来自用户选择文献 |
| 第六节参考文献被误判为截断 | 校验逻辑过度依赖第六节是否存在 | 改为重点检查前五节正文完整性，第六节由后端补全 |
| 管理员数据统计页存在假趋势数据 | 前端早期使用演示兜底趋势 | 后改为真实统计面板，展示数据库聚合指标和分布信息 |

### 7.6 测试结论

系统主流程能够完成文献检索、智能检索、详情查看、收藏、相似推荐、综述生成和管理员管理。智能检索通过主题画像与综合重排改善跨学科检索相关性。综述生成通过离线和在线双模式兼顾稳定性与表达质量。在线 LLM 异常时自动降级，保证系统可用性。

## 8 部署与用户手册

### 8.1 数据库部署

启动顺序：

1. 启动 MySQL。
2. 执行数据库 SQL 脚本。
3. 启动 backend-ai。
4. 启动 backend-java。
5. 启动 frontend。
6. 管理员重建智能检索索引。
7. 如需在线 LLM，管理员新增并激活 LLM API 配置。

### 8.2 backend-ai 启动

backend-ai：

```powershell
cd F:\campus-literature-system\backend-ai
pip install -r requirements.txt
uvicorn main:app --reload --port 8000
```

### 8.3 backend-java 启动

backend-java：

```powershell
cd F:\campus-literature-system\backend-java
mvn spring-boot:run
```

### 8.4 frontend 启动

frontend：

```powershell
cd F:\campus-literature-system\frontend
npm install
npm run dev
```

### 8.5 索引重建与 LLM 配置

系统启动后，管理员应在后台首页执行智能索引重建，使 backend-ai 的 FAISS 索引与 MySQL 文献数据同步。如需使用在线 LLM 增强综述生成，管理员应在 LLM API 管理页面新增配置、测试连接并设置 active 配置。

### 8.6 普通用户使用指南

普通用户进入系统后可注册或登录账号。登录后可在首页输入关键词快速检索，也可进入文献检索页使用普通检索或智能检索；高级检索页面支持按标题、作者、期刊、DOI、分类、文献类型和年份范围筛选。用户可进入文献详情页查看摘要、关键词、正文节选和相似文献推荐，也可收藏或取消收藏文献。在综述生成页面，用户输入综述主题，系统智能推荐参考文献，用户选择 2 到 5 篇文献后选择离线规则生成或在线 LLM 增强生成。生成结果会保存到综述记录，记录中展示 `generationMode` 标签并支持筛选。

### 8.7 管理员使用指南

管理员登录后进入后台首页，可查看真实数据统计面板和快捷操作入口。文献管理用于新增、编辑和删除课程项目模拟文献；分类管理用于维护两级学科分类；用户管理用于查看用户并调整状态；数据统计用于查看用户、文献、分类、收藏、综述、检索和 LLM 配置等聚合指标。文献数据更新后，管理员需要在后台重建智能检索索引。如需在线 LLM 增强综述生成，管理员应在 LLM API 管理页面新增配置、测试连接并设置 active 配置，页面只显示脱敏后的 API Key。

### 8.8 常见问题说明

**表 8-1 常见问题与解决方法**

| 问题 | 解决方法 |
| --- | --- |
| 智能检索无结果或提示服务不可用 | 确认 backend-ai 已启动，并由管理员重建智能检索索引 |
| 文献新增后智能检索结果未更新 | 管理员重新执行索引重建 |
| 在线 LLM 生成失败 | 检查 active LLM 配置、网络和 API 可用性；系统会自动降级为离线生成 |
| 综述生成提示文献数量不合法 | 选择 2 到 5 篇参考文献后再生成 |
| API Key 不应出现在截图中 | 只截取脱敏列表页，不打开含明文输入的编辑弹窗 |

## 9 维护计划与后续展望

### 9.1 纠错性维护

纠错性维护主要针对系统运行中发现的缺陷进行修复，包括登录认证异常、检索结果异常、权限控制问题、索引重建失败、综述生成失败、LLM 输出不完整和前端页面展示问题。维护过程中应记录问题现象、原因、修复措施和验证结果。

### 9.2 适应性维护

适应性维护主要应对运行环境和依赖变化，包括 JDK、Maven、Node.js、Python、MySQL、浏览器、FastAPI、sentence-transformers、FAISS 以及 OpenAI-compatible API 的版本变化。若第三方 LLM API 的请求格式或返回字段发生变化，需要同步调整 LLM 调用和输出校验逻辑。

### 9.3 完善性维护

完善性维护包括优化前端交互、补充推荐原因展示、完善测试用例、整理运行截图、改进报告排版和提升异常提示友好度。智能检索中后端已返回 `matchReason`，前端可在后续版本中进一步展示推荐原因。

### 9.4 数据与智能索引维护

文献和分类数据应保持一致。文献新增、修改或删除后，管理员需要重建 backend-ai 的 FAISS 索引，使语义检索和相似推荐结果与 MySQL 数据保持同步。课程项目模拟数据不能写成真实论文库。

### 9.5 LLM API 密钥维护

课程演示环境中 LLM API Key 由管理员维护，前端只显示脱敏值，不应写入文档、截图或仓库。生产环境应进一步采用加密存储、密钥轮换和访问审计，避免将课程演示存储方案写成生产级安全方案。

### 9.6 后续功能展望

后续展望包括接入真实论文数据库、PDF 上传与全文解析、BM25 + 向量混合检索、Cross-Encoder Rerank、LLM 流式输出、Docker 部署、API Key 加密存储和更严格的引用校验。上述功能当前尚未实现，只作为后续扩展方向。

## 10 项目总结

### 10.1 完成情况

本项目完成情况概括如下：

1. 完成普通用户注册登录、普通检索、高级检索、文献详情、收藏、相似文献推荐和综述记录查看等基础业务流程。
2. 完成基于 backend-ai、sentence-transformers、FAISS 和 Spring Boot 综合重排的多学科智能语义检索，实现 `finalScore` 排序和 `matchReason` 推荐原因。
3. 完成离线规则综述生成、在线 LLM 增强综述生成和 `llm_fallback_rule` 自动降级，保证系统在无可用 LLM 时仍可运行。
4. 完成管理员文献管理、分类管理、用户管理、真实数据统计、智能索引重建和 LLM API 配置管理等后台功能。
5. 完成数据库脚本、接口文档、测试用例、图表说明、截图清单、开发日志和 AI 辅助记录等课程项目文档整理。

### 10.2 项目不足

项目不足包括：文献数据仍为课程项目模拟数据，尚未接入真实论文库；前端当前主要展示语义相似度，智能检索推荐原因可进一步可视化；在线 LLM 依赖外部 API；课程演示环境中的 API Key 存储方案不适用于生产环境。

### 10.3 小组成员分工及工作比例

本项目由燕山大学计算机专业 1 班第 3 组完成，小组共 5 人。

**表 10-1 小组成员分工及工作比例**

| 序号 | 姓名 | 学号 | 角色与主要方向 | 主要分工 | 自评工作比例 |
| --- | --- | --- | --- | --- | --- |
| 1 | 周思源 | 202411040239 | 组长，主要开发与统筹 | 在项目中承担主要工作，负责项目总体规划、需求分析、系统架构设计、前后端主体搭建、智能检索与综述生成核心功能实现、前后端联调、项目文档主体撰写、最终汇报与演讲准备。 | 40% |
| 2 | 黄文政 | 202411040232 | 后端辅助开发 | 参与 Spring Boot 接口调试、权限控制检查、智能检索接口联调和后端功能测试。 | 18% |
| 3 | 李俊宇 | 202411040254 | 前端辅助开发 | 参与页面功能测试、前端交互调整、检索与综述页面辅助完善。 | 16% |
| 4 | 郝思怡 | 202411040237 | 文档润色与格式整理 | 参与需求分析、测试记录整理、报告内容校对和文字规范化。 | 14% |
| 5 | 王淑珍 | 202411040256 | PPT 制作与展示材料整理 | 参与系统截图收集、演示流程梳理和答辩辅助准备。 | 12% |

总体来看，周思源作为组长承担了项目主要规划与核心实现工作，其他成员围绕后端接口、前端页面、文档整理、测试记录、截图收集和展示材料准备进行协作，保证了系统实现、文档整理和最终汇报材料能够同步推进。详细成员信息可参考 `docs/team-info.md`。

## 参考文献

[1] VMware Tanzu. Spring Boot Reference Documentation[EB/OL]. https://spring.io/projects/spring-boot

[2] Meta Open Source. React Documentation[EB/OL]. https://react.dev/

[3] Oracle. MySQL 8.0 Reference Manual[EB/OL]. https://dev.mysql.com/doc/refman/8.0/en/

[4] Sebastián Ramírez. FastAPI Documentation[EB/OL]. https://fastapi.tiangolo.com/

[5] Johnson J, Douze M, Jégou H. Billion-scale similarity search with GPUs[J]. IEEE Transactions on Big Data, 2019, 7(1): 535-547.

[6] Reimers N, Gurevych I. Sentence-BERT: Sentence Embeddings using Siamese BERT-Networks[C]. Proceedings of the 2019 Conference on Empirical Methods in Natural Language Processing and the 9th International Joint Conference on Natural Language Processing (EMNLP-IJCNLP), 2019: 3982-3992.

[7] Lewis P, Perez E, Piktus A, et al. Retrieval-Augmented Generation for Knowledge-Intensive NLP Tasks[C]. Advances in Neural Information Processing Systems (NeurIPS), 2020, 33: 9459-9474.

[8] MyBatis-Plus. MyBatis-Plus Documentation[EB/OL]. https://baomidou.com/

[9] TanStack. TanStack Router Documentation[EB/OL]. https://tanstack.com/router/

[10] OpenAI. OpenAI API Reference[EB/OL]. https://platform.openai.com/docs/api-reference

## 附录

### 附录 A 数据库脚本

系统数据库采用 MySQL 8.0，核心表包括 `user`、`category`、`literature`、`favorite`、`search_history`、`review_record`、`llm_config`、`admin_log` 和 `literature_vector`。以下为简化建表说明，完整 SQL 脚本见 `database/init.sql`、`database/update_categories.sql`、`database/literature_seed_500_high_quality.sql` 和 `database/update_llm_config.sql`。

**user 表（用户表）**

```sql
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50),
    email VARCHAR(100),
    role VARCHAR(20) DEFAULT 'USER',
    status INT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

**category 表（分类表）**

```sql
CREATE TABLE category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    sort_order INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

**literature 表（文献表）**

```sql
CREATE TABLE literature (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    authors VARCHAR(255),
    abstract_text TEXT,
    keywords VARCHAR(255),
    journal VARCHAR(255),
    publish_year INT,
    doi VARCHAR(100),
    category_id BIGINT,
    citation_count INT DEFAULT 0,
    document_type VARCHAR(50),
    content TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

**favorite 表（收藏表）**

```sql
CREATE TABLE favorite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    literature_id BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

**search_history 表（检索历史表）**

```sql
CREATE TABLE search_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    keyword VARCHAR(255) NOT NULL,
    search_type VARCHAR(30) DEFAULT 'NORMAL',
    result_count INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

**review_record 表（综述记录表）**

```sql
CREATE TABLE review_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    topic VARCHAR(255) NOT NULL,
    literature_ids TEXT,
    content LONGTEXT,
    reference_text TEXT,
    generation_mode VARCHAR(30),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

**llm_config 表（LLM 配置表）**

```sql
CREATE TABLE llm_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    provider VARCHAR(50),
    base_url VARCHAR(255),
    model VARCHAR(100),
    api_key VARCHAR(255),
    enabled INT DEFAULT 1,
    active INT DEFAULT 0,
    timeout_seconds INT DEFAULT 120,
    remark VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

### 附录 B 接口说明

详见 `docs/api.md`。

### 附录 C 测试用例

详见 `docs/test-cases.md`。

### 附录 D 系统运行截图

以下截图按模块分类排列，所有截图文件位于 `docs/screenshots/` 目录。

#### D.1 基础访问与用户认证

**图 D-1 首页运行效果** (`01-home.png`)

![首页运行效果](docs/screenshots/01-home.png)

**图 D-13 用户登录页面截图** (`13-login.png`)

![用户登录页面截图](docs/screenshots/13-login.png)

**图 D-14 用户注册页面截图** (`14-register.png`)

![用户注册页面截图](docs/screenshots/14-register.png)

#### D.2 文献检索与详情

**图 D-2 普通检索运行效果** (`02-search-normal.png`)

![普通检索运行效果](docs/screenshots/02-search-normal.png)

**图 D-15 高级检索页面截图** (`15-advanced-search.png`)

![高级检索页面截图](docs/screenshots/15-advanced-search.png)

**图 D-3 智能检索运行效果** (`03-search-ai.png`)

![智能检索运行效果](docs/screenshots/03-search-ai.png)

**图 D-4 文献详情与相似文献推荐** (`04-literature-detail-recommend.png`)

![文献详情与相似文献推荐](docs/screenshots/04-literature-detail-recommend.png)

**图 D-16 我的收藏页面截图** (`16-favorites.png`)

![我的收藏页面截图](docs/screenshots/16-favorites.png)

#### D.3 综述生成与记录

**图 D-5 综述生成双模式选择** (`05-review-generate-mode.png`)

![综述生成双模式选择](docs/screenshots/05-review-generate-mode.png)

**图 D-21 离线综述生成结果截图** (`21-review-rule-result.png`)

![离线综述生成结果截图](docs/screenshots/21-review-rule-result.png)

**图 D-6 在线 LLM 综述生成结果** (`06-review-llm-result.png`)

![在线 LLM 综述生成结果](docs/screenshots/06-review-llm-result.png)

**图 D-7 综述记录 generationMode 标签和筛选** (`07-review-history-generation-mode.png`)

![综述记录 generationMode 标签和筛选](docs/screenshots/07-review-history-generation-mode.png)

**图 D-22 综述记录按生成方式筛选结果截图** (`22-review-history-filter.png`)

![综述记录按生成方式筛选结果截图](docs/screenshots/22-review-history-filter.png)

#### D.4 管理员后台

**图 D-8 管理员后台首页** (`08-admin-dashboard.png`)

![管理员后台首页](docs/screenshots/08-admin-dashboard.png)

**图 D-17 管理员文献管理页面截图** (`17-admin-literature.png`)

![管理员文献管理页面截图](docs/screenshots/17-admin-literature.png)

**图 D-18 管理员分类管理页面截图** (`18-admin-category.png`)

![管理员分类管理页面截图](docs/screenshots/18-admin-category.png)

**图 D-19 管理员用户管理页面截图** (`19-admin-users.png`)

![管理员用户管理页面截图](docs/screenshots/19-admin-users.png)

**图 D-20 管理员数据统计页面截图** (`20-admin-statistics.png`)

![管理员数据统计页面](docs/screenshots/20-admin-statistics.png)

**图 D-9 重建智能索引成功提示** (`09-admin-rebuild-index.png`)

![重建智能索引成功提示](docs/screenshots/09-admin-rebuild-index.png)

**图 D-10 LLM API 管理页面** (`10-admin-llm-config.png`)

![LLM API 管理页面](docs/screenshots/10-admin-llm-config.png)

**图 D-11 LLM 测试连接成功** (`11-llm-test-success.png`)

![LLM 测试连接成功](docs/screenshots/11-llm-test-success.png)

#### D.5 系统运行与版本记录

**图 D-12 三服务启动与项目运行终端截图** (`12-services-running.png`)

![三服务启动与项目运行终端截图](docs/screenshots/12-services-running.png)

> 注：该截图展示项目运行与 Git 提交记录等终端信息，用于说明系统运行和版本管理情况。

### 附录 E AI 辅助记录

详见 `docs/ai-records.md`。

### 附录 F 项目开发日志

详见 `docs/dev-log.md`。

### 附录 G 核心代码结构与图表说明

核心代码结构包括 `frontend/src/api`、`frontend/src/routes`、`backend-java/src/main/java`、`backend-ai/main.py` 和 `database/` 下的 SQL 脚本。系统架构图、功能结构图、数据流图、E-R 图、用例图、流程图和时序图详见 `docs/diagrams.md`，接口明细详见 `docs/api.md`，数据库设计详见 `docs/database-design.md`。
