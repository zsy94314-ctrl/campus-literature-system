# 系统运行截图清单

本清单用于准备正式报告附录 D。截图建议统一放入 `docs/screenshots/` 目录，文件名保持两位序号，便于报告引用和排序。

## 截图要求

- 截图前确认 backend-ai、backend-java、frontend 均已正常启动。
- 涉及 LLM API 管理页面时，必须遮挡 API Key、Token、数据库密码等敏感信息。
- 截图应尽量使用同一浏览器窗口尺寸，避免页面过度缩放。
- 截图内容只展示课程项目模拟数据，不要声称是真实论文库。
- 附录 D 可按本清单顺序插入截图，并在每张图下方添加图名。

## 建议截图

| 文件名 | 截图内容 | 截图要点 |
| --- | --- | --- |
| `01-home.png` | 首页 | 展示系统名称、分类入口、统计或推荐区域 |
| `02-search-normal.png` | 普通检索 | 展示关键词检索结果列表和分页 |
| `03-search-ai.png` | 智能检索结果 | 展示自然语言查询、语义相似度和智能检索结果 |
| `04-literature-detail-recommend.png` | 文献详情与相似文献推荐 | 展示文献详情、收藏按钮和相似文献推荐区域 |
| `05-review-generate-mode.png` | 综述生成双模式选择 | 展示主题输入、推荐参考文献、`rule` 与 `llm` 模式选择 |
| `06-review-llm-result.png` | 在线 LLM 综述生成结果 | 展示在线增强生成后的综述正文，避免出现真实密钥信息 |
| `07-review-history-generation-mode.png` | 综述记录 generationMode 标签和筛选 | 展示离线、在线、降级等标签或筛选效果 |
| `08-admin-dashboard.png` | 管理员后台首页 | 展示管理员统计卡片和后台导航 |
| `09-admin-rebuild-index.png` | 重建智能索引入口或成功提示 | 展示重建索引按钮、操作提示或成功结果 |
| `10-admin-llm-config.png` | LLM API 管理页面 | 必须遮挡 API Key；展示配置列表、active 状态和脱敏 Key |
| `11-llm-test-success.png` | LLM 测试连接成功 | 展示测试连接成功提示，遮挡所有敏感配置 |
| `12-services-running.png` | 服务启动与项目运行终端截图 | 展示项目运行与 Git 提交记录等终端信息，用于说明系统运行和版本管理情况 |

## 附录 D 引用建议

正式报告附录 D 可写为：

- 图 D-1 首页运行效果：`docs/screenshots/01-home.png`
- 图 D-2 普通检索运行效果：`docs/screenshots/02-search-normal.png`
- 图 D-3 智能检索运行效果：`docs/screenshots/03-search-ai.png`
- 图 D-4 文献详情与相似推荐：`docs/screenshots/04-literature-detail-recommend.png`
- 图 D-5 综述生成双模式选择：`docs/screenshots/05-review-generate-mode.png`
- 图 D-6 在线 LLM 综述生成结果：`docs/screenshots/06-review-llm-result.png`
- 图 D-7 综述记录 generationMode：`docs/screenshots/07-review-history-generation-mode.png`
- 图 D-8 管理员后台首页：`docs/screenshots/08-admin-dashboard.png`
- 图 D-9 重建智能索引：`docs/screenshots/09-admin-rebuild-index.png`
- 图 D-10 LLM API 管理：`docs/screenshots/10-admin-llm-config.png`
- 图 D-11 LLM 测试连接成功：`docs/screenshots/11-llm-test-success.png`
- 图 D-12 三服务启动与项目运行终端截图：`docs/screenshots/12-services-running.png`
