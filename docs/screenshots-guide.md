# 系统运行截图清单

本清单用于准备正式报告附录 D。截图建议统一放入 `docs/screenshots/` 目录，文件名保持两位序号，便于报告引用和排序。

## 截图要求

- 截图前确认 backend-ai、backend-java、frontend 均已正常启动。
- 涉及 LLM API 管理页面时，必须遮挡 API Key、Token、数据库密码等敏感信息。
- 截图应尽量使用同一浏览器窗口尺寸，避免页面过度缩放。
- 截图内容只展示课程项目模拟数据，不要声称是真实论文库。
- 附录 D 可按本清单顺序插入截图，并在每张图下方添加图名。

## 建议截图

### A. 基础访问与用户认证

| 文件名 | 截图内容 | 截图要点 |
| --- | --- | --- |
| `01-home.png` | 首页 | 展示系统名称、分类入口、统计或推荐区域 |
| `13-login.png` | 登录页面 | 展示用户登录表单和界面布局 |
| `14-register.png` | 注册页面 | 展示用户注册表单和界面布局 |

### B. 文献检索与详情

| 文件名 | 截图内容 | 截图要点 |
| --- | --- | --- |
| `02-search-normal.png` | 普通检索 | 展示关键词检索结果列表和分页 |
| `15-advanced-search.png` | 高级检索页面 | 展示多条件组合检索（标题、作者、期刊、DOI、分类、年份等） |
| `03-search-ai.png` | 智能检索结果 | 展示自然语言查询、语义相似度和智能检索结果 |
| `04-literature-detail-recommend.png` | 文献详情与相似文献推荐 | 展示文献详情、收藏按钮和相似文献推荐区域 |
| `16-favorites.png` | 我的收藏页面 | 展示用户收藏的文献列表和管理功能 |

### C. 综述生成与记录

| 文件名 | 截图内容 | 截图要点 |
| --- | --- | --- |
| `05-review-generate-mode.png` | 综述生成双模式选择 | 展示主题输入、推荐参考文献、`rule` 与 `llm` 模式选择 |
| `21-review-rule-result.png` | 离线综述生成结果 | 展示离线规则生成的综述正文效果 |
| `06-review-llm-result.png` | 在线 LLM 综述生成结果 | 展示在线增强生成后的综述正文，避免出现真实密钥信息 |
| `07-review-history-generation-mode.png` | 综述记录 generationMode 标签和筛选 | 展示离线、在线、降级等标签或筛选效果 |
| `22-review-history-filter.png` | 综述记录按生成方式筛选结果 | 展示按 rule / llm / llm_fallback_rule 筛选后的记录列表 |

### D. 管理员后台

| 文件名 | 截图内容 | 截图要点 |
| --- | --- | --- |
| `08-admin-dashboard.png` | 管理员后台首页 | 展示管理员统计卡片和后台导航 |
| `17-admin-literature.png` | 管理员文献管理页面 | 展示文献列表、增删改查操作 |
| `18-admin-category.png` | 管理员分类管理页面 | 展示两级学科分类的维护界面 |
| `19-admin-users.png` | 管理员用户管理页面 | 展示用户列表和权限管理 |
| `20-admin-statistics.png` | 管理员数据统计页面 | 展示系统运营统计数据和图表 |
| `09-admin-rebuild-index.png` | 重建智能索引入口或成功提示 | 展示重建索引按钮、操作提示或成功结果 |
| `10-admin-llm-config.png` | LLM API 管理页面 | 必须遮挡 API Key；展示配置列表、active 状态和脱敏 Key |
| `11-llm-test-success.png` | LLM 测试连接成功 | 展示测试连接成功提示，遮挡所有敏感配置 |

### E. 系统运行与版本记录

| 文件名 | 截图内容 | 截图要点 |
| --- | --- | --- |
| `12-services-running.png` | 服务启动与项目运行终端截图 | 展示项目运行与 Git 提交记录等终端信息，用于说明系统运行和版本管理情况 |

## 附录 D 引用建议

正式报告附录 D 可写为：

### A. 基础访问与用户认证

- 图 D-1 首页运行效果：`docs/screenshots/01-home.png`
- 图 D-13 用户登录页面：`docs/screenshots/13-login.png`
- 图 D-14 用户注册页面：`docs/screenshots/14-register.png`

### B. 文献检索与详情

- 图 D-2 普通检索运行效果：`docs/screenshots/02-search-normal.png`
- 图 D-15 高级检索页面：`docs/screenshots/15-advanced-search.png`
- 图 D-3 智能检索运行效果：`docs/screenshots/03-search-ai.png`
- 图 D-4 文献详情与相似推荐：`docs/screenshots/04-literature-detail-recommend.png`
- 图 D-16 我的收藏页面：`docs/screenshots/16-favorites.png`

### C. 综述生成与记录

- 图 D-5 综述生成双模式选择：`docs/screenshots/05-review-generate-mode.png`
- 图 D-21 离线综述生成结果：`docs/screenshots/21-review-rule-result.png`
- 图 D-6 在线 LLM 综述生成结果：`docs/screenshots/06-review-llm-result.png`
- 图 D-7 综述记录 generationMode：`docs/screenshots/07-review-history-generation-mode.png`
- 图 D-22 综述记录按生成方式筛选结果：`docs/screenshots/22-review-history-filter.png`

### D. 管理员后台

- 图 D-8 管理员后台首页：`docs/screenshots/08-admin-dashboard.png`
- 图 D-17 管理员文献管理页面：`docs/screenshots/17-admin-literature.png`
- 图 D-18 管理员分类管理页面：`docs/screenshots/18-admin-category.png`
- 图 D-19 管理员用户管理页面：`docs/screenshots/19-admin-users.png`
- 图 D-20 管理员数据统计页面：`docs/screenshots/20-admin-statistics.png`
- 图 D-9 重建智能索引：`docs/screenshots/09-admin-rebuild-index.png`
- 图 D-10 LLM API 管理：`docs/screenshots/10-admin-llm-config.png`
- 图 D-11 LLM 测试连接成功：`docs/screenshots/11-llm-test-success.png`

### E. 系统运行与版本记录

- 图 D-12 三服务启动与项目运行终端截图：`docs/screenshots/12-services-running.png`
