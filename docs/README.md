# docs 目录说明

本目录保存《校园学术文献智能检索与综述生成系统》的最终版文档、图表说明和后续报告素材。根目录仅保留正式报告写作需要直接使用的主文档；旧版或兼容文档统一归档到 `archive/`。

## 主文档

| 文件 | 用途 |
| --- | --- |
| `final-report-draft.md` | 正式报告初稿，后续 Word 排版可基于此文档整理 |
| `report-outline-final.md` | 最终正式目录，作为报告章节结构基准 |
| `writing-outline-final.md` | 正文写作细纲，说明每章写作重点、图表插入建议和注意事项 |
| `api.md` | 接口文档，覆盖前后端业务接口、管理员接口、LLM API 管理接口和 backend-ai 接口 |
| `database-design.md` | 数据库设计文档，说明 MySQL 表结构、逻辑关联和 FAISS 文件存储位置 |
| `test-cases.md` | 测试用例文档，覆盖构建、认证、检索、综述、LLM、管理员和异常权限测试 |
| `dev-log.md` | 项目开发日志，记录从 MVP 到智能检索、综述生成、LLM 接入和文档整理的过程 |
| `ai-records.md` | AI 辅助交互记录，说明 AI 在需求分析、技术讨论、问题排查、测试设计和文档整理中的辅助作用 |
| `diagrams.md` | Mermaid 图表汇总，包含系统架构图、功能结构图、E-R 图、用例图、流程图和时序图 |
| `screenshots-guide.md` | 运行截图清单，说明后续应截图的页面、建议文件名和敏感信息遮挡要求 |

## 素材目录

| 目录 | 用途 |
| --- | --- |
| `diagrams/` | 后续存放从 Mermaid 或其他工具导出的图表图片，空目录使用 `.gitkeep` 保留 |
| `screenshots/` | 后续存放系统运行截图，空目录使用 `.gitkeep` 保留 |
| `archive/` | 旧版文档归档，仅作历史参考，不作为正式报告写作基准 |

## 使用建议

- 写正式报告时优先参考 `report-outline-final.md`、`writing-outline-final.md` 和 `final-report-draft.md`。
- 查接口以 `api.md` 为准，旧版接口文档已归档。
- 查开发过程以 `dev-log.md` 为准，旧版 `records/dev-log.md` 已归档为 `archive/records-dev-log.md`。
- 图表正文引用 `diagrams.md` 中的图号和标题，导出的图片可放入 `diagrams/`。
- 系统截图按 `screenshots-guide.md` 的文件名放入 `screenshots/`，涉及 API Key 的页面必须遮挡敏感信息。
