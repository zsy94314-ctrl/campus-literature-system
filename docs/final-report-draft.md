# 校园学术文献智能检索与综述生成系统正式报告初稿

## 摘要

随着高校课程学习和科研训练中学术资料数量不断增加，传统关键词检索在自然语言理解、跨学科主题识别和综述整理方面存在一定不足。本文设计并实现了一个校园学术文献智能检索与综述生成系统。系统采用 React + Vite + TypeScript 构建前端，采用 Spring Boot + MyBatis-Plus + MySQL 构建主后端，采用 FastAPI + sentence-transformers + FAISS 构建智能语义检索服务。

系统支持用户注册登录、文献普通检索、高级检索、多学科智能语义检索、文献详情、收藏、相似文献推荐、智能推荐参考文献、离线综述生成、在线 LLM 增强综述生成以及综述记录管理。管理员端支持文献管理、分类管理、用户管理、数据统计、重建智能检索索引和 LLM API 配置管理。智能检索部分在 FAISS 语义召回基础上，增加多学科主题画像、Query Expansion、MySQL 关键词补充召回、`keywordScore`、`categoryScore`、`weakPenalty`、`finalScore` 和 `matchReason`，缓解跨学科检索跑偏问题。综述生成部分支持离线稳定模式和在线 LLM 增强模式，在线不可用或输出不完整时自动降级为离线生成。

系统文献数据为课程项目模拟数据，不是真实论文库。在线 LLM 只作为增强模式，离线综述生成是默认稳定方案。系统对 LLM 输出进行完整性校验，并由后端补全参考文献来源，避免模型编造不存在的参考文献。

关键词：学术文献检索；多学科语义检索；综述生成；FAISS；LLM API；Spring Boot

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

此处插入系统运行截图，详见 `docs/screenshots-guide.md`。建议在正文中选择展示以下关键截图：

- 此处插入图 6-6 首页运行截图，详见 `docs/screenshots-guide.md`。
- 此处插入图 6-7 普通检索运行截图，详见 `docs/screenshots-guide.md`。
- 此处插入图 6-8 智能检索结果截图，详见 `docs/screenshots-guide.md`。
- 此处插入图 6-9 文献详情与相似文献推荐截图，详见 `docs/screenshots-guide.md`。
- 此处插入图 6-10 综述生成双模式选择截图，详见 `docs/screenshots-guide.md`。
- 此处插入图 6-11 在线 LLM 综述生成结果截图，详见 `docs/screenshots-guide.md`。
- 此处插入图 6-12 综述记录 generationMode 标签和筛选截图，详见 `docs/screenshots-guide.md`。

### 6.2 Java 后端实现

Java 后端按 Controller、Service、Mapper、Entity、DTO、VO 分层。`JwtInterceptor` 对 `/api/**` 进行登录态解析和权限控制。`AiServiceImpl` 负责调用 backend-ai 并进行多学科重排。`ReviewRecordServiceImpl` 负责综述生成分流与记录保存。`LlmReviewServiceImpl` 负责在线 LLM 调用、输出校验和参考文献补全。

此处插入图 6-4 管理员重建智能索引时序图，详见 `docs/diagrams.md`。

### 6.3 Python 智能检索服务实现

backend-ai 使用 FastAPI 提供四个接口。重建索引时，将文献文本向量化并写入 FAISS。语义检索时，将查询向量化后检索相似文献。相似推荐时，使用目标文献向量查找相近文献。

### 6.4 数据库实现

数据库脚本位于 `database/`。`init.sql` 创建基础表，`update_categories.sql` 重建两级分类，`literature_seed_500_high_quality.sql` 导入 500 条模拟文献，`update_llm_config.sql` 创建 LLM 配置表并增加 `generation_mode` 字段。

### 6.5 管理员后台运行截图说明

管理员后台截图用于展示系统管理能力。建议在正文或附录中插入管理员后台首页、重建智能索引、LLM API 管理、LLM 测试连接成功和三个服务启动终端截图。涉及 LLM API 管理页面时，必须遮挡 API Key、Token、数据库密码等敏感信息。

- 此处插入图 6-13 管理员后台首页截图，详见 `docs/screenshots-guide.md`。
- 此处插入图 6-14 重建智能索引入口或成功提示截图，详见 `docs/screenshots-guide.md`。
- 此处插入图 6-15 LLM API 管理页面截图，详见 `docs/screenshots-guide.md`。
- 此处插入图 6-16 LLM 测试连接成功截图，详见 `docs/screenshots-guide.md`。
- 此处插入图 6-17 backend-ai、backend-java、frontend 三个服务启动终端截图，详见 `docs/screenshots-guide.md`。

## 7 系统测试

### 7.1 测试环境

测试环境包括 Windows、MySQL、JDK 17、Maven、Node.js、Python、Chrome 或 Edge 浏览器。backend-ai 默认端口 8000，backend-java 默认端口 8080，frontend 默认端口 5173。

### 7.2 测试内容

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

### 7.3 测试结论

系统主流程能够完成文献检索、智能检索、详情查看、收藏、相似推荐、综述生成和管理员管理。智能检索通过主题画像与综合重排改善跨学科检索相关性。综述生成通过离线和在线双模式兼顾稳定性与表达质量。在线 LLM 异常时自动降级，保证系统可用性。

## 8 部署与使用说明

启动顺序：

1. 启动 MySQL。
2. 执行数据库 SQL 脚本。
3. 启动 backend-ai。
4. 启动 backend-java。
5. 启动 frontend。
6. 管理员重建智能检索索引。
7. 如需在线 LLM，管理员新增并激活 LLM API 配置。

backend-ai：

```powershell
cd F:\campus-literature-system\backend-ai
pip install -r requirements.txt
uvicorn main:app --reload --port 8000
```

backend-java：

```powershell
cd F:\campus-literature-system\backend-java
mvn spring-boot:run
```

frontend：

```powershell
cd F:\campus-literature-system\frontend
npm install
npm run dev
```

## 9 维护计划与后续展望

维护内容包括文献数据维护、分类维护、智能索引重建、LLM API 配置测试、API Key 安全管理和日志检查。

后续展望包括接入真实论文数据库、PDF 上传与全文解析、BM25 + 向量混合检索、Cross-Encoder Rerank、LLM 流式输出、Docker 部署、API Key 加密存储和更严格的引用校验。上述功能当前尚未实现，只作为后续扩展方向。

## 10 项目总结

本项目完成了校园学术文献智能检索与综述生成系统的主要功能。系统通过前后端分离、MySQL 结构化存储、backend-ai 向量检索和 Spring Boot 多学科重排，实现了文献检索和智能推荐能力。综述生成部分采用离线规则与在线 LLM 双模式，在线不稳定时自动降级，增强了系统可靠性。

项目不足包括：文献数据仍为课程项目模拟数据，尚未接入真实论文库；前端当前主要展示语义相似度，智能检索推荐原因可进一步可视化；在线 LLM 依赖外部 API；课程演示环境中的 API Key 存储方案不适用于生产环境。

### 10.3 小组成员分工及工作比例

本项目由燕山大学计算机专业 1 班第 3 组完成，小组共 5 人。

| 序号 | 姓名 | 学号 | 角色与主要方向 | 主要分工 | 自评工作比例 |
| --- | --- | --- | --- | --- | --- |
| 1 | 周思源 | 202411040239 | 组长，主要开发与统筹 | 在项目中承担主要工作，负责项目总体规划、需求分析、系统架构设计、前后端主体搭建、智能检索与综述生成核心功能实现、前后端联调、项目文档主体撰写、最终汇报与演讲准备。 | 40% |
| 2 | 黄文政 | 202411040232 | 后端辅助开发 | 参与 Spring Boot 接口调试、权限控制检查、智能检索接口联调和后端功能测试。 | 18% |
| 3 | 李俊宇 | 202411040254 | 前端辅助开发 | 参与页面功能测试、前端交互调整、检索与综述页面辅助完善。 | 16% |
| 4 | 郝思怡 | 202411040237 | 文档润色与格式整理 | 参与需求分析、测试记录整理、报告内容校对和文字规范化。 | 14% |
| 5 | 王淑珍 | 202411040256 | PPT 制作与展示材料整理 | 参与系统截图收集、演示流程梳理和答辩辅助准备。 | 12% |

总体来看，周思源作为组长承担了项目主要规划与核心实现工作，其他成员围绕后端接口、前端页面、文档整理、测试记录、截图收集和展示材料准备进行协作，保证了系统实现、文档整理和最终汇报材料能够同步推进。详细成员信息可参考 `docs/team-info.md`。

## 参考文献

此处填写课程教材、Spring Boot、React、Vite、MyBatis-Plus、FastAPI、sentence-transformers、FAISS、OpenAI-compatible API 等参考资料。

## 附录

### 附录 A 数据库脚本

此处插入核心建表语句。

### 附录 B 接口说明

详见 `docs/api.md`。

### 附录 C 测试用例

详见 `docs/test-cases.md`。

### 附录 D 系统运行截图

此处插入首页、检索页、文献详情、智能检索、综述生成、综述记录、管理员后台、LLM API 管理等截图，截图清单详见 `docs/screenshots-guide.md`。

### 附录 E AI 辅助记录

详见 `docs/ai-records.md`。

### 附录 F 项目开发日志

详见 `docs/dev-log.md`。
