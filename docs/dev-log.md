# 开发日志

## 阶段一：项目初始化与 MVP

- 创建项目目录结构：`frontend`、`backend-java`、`backend-ai`、`database`、`docs`。
- 明确系统目标：文献检索、收藏、相似推荐、综述生成和管理员后台。
- 编写 `database/init.sql`，创建用户、分类、文献、收藏、搜索历史、综述记录、管理员日志和向量映射辅助表。
- 搭建 Spring Boot 后端，完成 Controller、Service、Mapper、DTO、VO 分层。
- 搭建 React + Vite + TypeScript 前端，完成首页、登录注册、检索、详情、收藏、综述和管理员页面初版。
- 完成 JWT 登录认证和管理员权限控制。

## 阶段二：文献数据与两级分类

- 扩展 `literature` 字段，加入文献类型、来源链接、正文节选等信息。
- 将分类体系调整为两级结构：6 个一级分类、37 个二级分类。
- 编写 `database/update_categories.sql`。
- 编写 `database/literature_seed_500_high_quality.sql`，提供 500 条课程项目模拟文献数据。
- 明确文献数据为课程项目模拟数据，不是真实论文库。

## 阶段三：backend-ai 智能检索服务

- 使用 FastAPI 搭建 `backend-ai`。
- 接入 `sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2`。
- 使用标题、分类、文献类型、关键词、摘要、正文节选进行向量化。
- 使用 FAISS `IndexFlatIP` 建立语义向量索引。
- 实现接口：
  - `GET /health`
  - `POST /rebuild-index`
  - `POST /semantic-search`
  - `POST /recommend`
- 将索引持久化到：
  - `backend-ai/data/faiss.index`
  - `backend-ai/data/id_map.json`

## 阶段四：Spring Boot 接入智能检索

- 新增 `/api/ai/health`、`/api/ai/rebuild-index`、`/api/ai/semantic-search`、`/api/ai/recommend/{literatureId}`。
- 管理员后台增加“重建智能索引”入口。
- 文献详情页接入相似文献推荐。
- 检索页增加“普通检索 / 智能检索”切换。
- 对 backend-ai 未启动、索引未重建等场景增加友好错误提示。

## 阶段五：智能检索多学科优化

早期纯 FAISS 检索在跨学科场景中出现跑偏，例如“人工智能在医学中的应用”容易返回过多计算机类文献。

修复与优化：

- 增加多学科主题画像。
- 支持教育、医学健康、心理健康、文献检索、农业生态、法学治理、经济管理、人文艺术、工程技术、自然科学、人工智能通用主题。
- 增加 Query Expansion 查询扩展。
- 增加 MySQL 关键词补充召回，匹配 `title`、`keywords`、`abstract_text`、`content`。
- 增加 `keywordScore` 字段命中评分。
- 增加 `categoryScore` 分类相关度评分。
- 增加 `weakPenalty` 弱相关降权。
- 增加 `finalScore` 综合排序。
- 增加 `matchReason` 推荐原因解释。

## 阶段六：首页分类问题修复

### 首页分类统计为 0

原因：

- 早期统计逻辑未基于全部文献和二级分类做真实聚合。

修复：

- 后端 `CategoryServiceImpl.getCategoryStatistics()` 查询所有二级分类，并遍历全部文献按 `categoryId` 聚合计数。
- 首页调用 `/api/categories/statistics` 显示真实二级分类数量。

### 首页分类跳转不生效

原因：

- 前端早期跳转时传递分类名称，后端检索实际需要 `categoryId`。

修复：

- 首页分类链接传递 `categoryId`。
- 搜索页读取 URL 参数并自动触发普通检索。

## 阶段七：离线综述生成升级

早期综述生成偏模板化，内容容易像摘要堆砌。

修复与优化：

- 将综述生成升级为综合归纳型生成。
- 增加综述主题识别。
- 增加关键词提取。
- 增加研究方向自动归纳。
- 增加主题化问题池和趋势池。
- 输出固定六部分：
  - 研究背景
  - 研究现状
  - 主要研究方向
  - 存在问题
  - 发展趋势
  - 参考文献来源
- 限制用户选择 2 到 5 篇参考文献。
- 保存综述记录到 `review_record`。

## 阶段八：在线 LLM 增强综述生成

- 新增 `llm_config` 表。
- 为 `review_record` 增加 `generation_mode` 字段。
- 管理员后台新增 LLM API 管理页面。
- 支持新增、编辑、删除 LLM 配置。
- 支持设置 active 配置。
- 支持测试 LLM API 连接。
- 支持长文本综述测试接口 `/api/admin/llm-configs/{id}/test-review`。
- 前端只展示 `apiKeyMasked`，不展示完整 API Key。
- 用户端综述生成支持两种模式：
  - `rule`：离线综述生成
  - `llm`：在线 LLM 增强综述生成
- 在线 LLM 不可用时自动降级为离线生成，记录 `generationMode=llm_fallback_rule`。

## 阶段九：LLM 输出完整性修复

### LLM 输出截断

原因：

- 模型可能返回 `finish_reason=length`，或内容在第五节附近停止。

修复：

- 检查 `finish_reason`。
- 首次使用 `max_tokens=2500`。
- 若 `finish_reason=length`，使用 `max_tokens=3000` 重试一次。
- 两次均被截断时降级为离线生成。

### LLM 参考文献不完整

原因：

- 模型可能漏写“六、参考文献来源”，或只列出部分文献。

修复：

- 后端根据用户选择的文献构造标准参考文献列表。
- 若缺少第六节，则追加。
- 若已有第六节，则替换为后端标准列表。
- 不允许 LLM 编造参考文献。

### 第六节参考文献被误判为截断

原因：

- 早期逻辑可能把“缺少第六节”直接判为截断，导致后端补全逻辑无法执行。

修复：

- 截断检查只检查最后一行是否以明显截断词结尾。
- 只要前五个正文部分完整，就允许进入参考文献后端补全。
- 补全后再检查是否包含“参考文献来源”。

### LLM 输出不存在的引用编号

原因：

- 模型可能生成 `[4]`、`[99]` 等超出所选文献范围的引用。

修复：

- 后端使用正则清理小于 1 或大于所选文献数量的引用编号。

## 阶段十：文档整理

- 根据真实代码和 SQL 更新根目录 `README.md`。
- 新增 `docs/report-outline-final.md`。
- 新增 `docs/writing-outline-final.md`。
- 新增 `docs/api.md`。
- 更新 `docs/database-design.md`。
- 更新 `docs/test-cases.md`。
- 新增 `docs/dev-log.md`。
- 更新 `docs/ai-records.md`。
- 新增 `docs/final-report-draft.md`。

## 当前已知注意点

- `.gitignore` 当前未在根目录发现。
- `backend-ai/README.md` 显示为乱码，但本阶段限制只修改根 `README.md` 和 `docs` 下 Markdown，因此未修改该文件。
- 后端已返回智能检索 `matchReason`，当前前端检索结果主要展示 `similarity`，推荐原因展示可作为后续界面优化。
- 管理员统计接口实际返回用户数、文献数、分类数、综述数；前端趋势图为默认展示数据，不来自后端。
- 课程演示环境可保存 LLM 配置，但生产环境应加密保存 API Key。
