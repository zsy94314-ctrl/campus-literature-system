# 系统图表汇总

本文档集中保存正式报告可引用的 Mermaid 图表。报告正文建议只插入图号、图名和简要说明，完整 Mermaid 源码以本文档为准。

## 图 3-1 顶层数据流图

导出图片：docs/diagrams/figure-3-1-dfd-context.png

```mermaid
flowchart LR
    U["外部实体：普通用户"]
    A["外部实体：管理员"]
    L["外部实体：在线 LLM API<br/>DeepSeek / OpenAI-compatible"]
    S(("校园学术文献智能检索与综述生成系统"))
    D[("数据存储：MySQL<br/>用户、文献、分类、收藏、综述、LLM 配置")]
    F[("数据存储：FAISS 索引文件<br/>faiss.index / id_map.json")]

    U -->|"登录请求 / 检索请求 / 收藏请求 / 综述生成请求"| S
    S -->|"文献检索结果 / 智能检索结果 / 综述内容 / 综述记录"| U
    A -->|"后台管理请求 / 索引重建请求 / LLM 配置请求"| S
    S -->|"管理结果 / 统计数据 / 测试结果"| A
    S -->|"读取与写入业务数据"| D
    S -->|"语义召回 / 相似推荐 / 索引重建"| F
    S -->|"在线综述生成请求"| L
    L -->|"LLM 生成文本 / finish_reason / 错误信息"| S
```

说明：顶层 DFD 表达系统与外部实体、MySQL 数据库、FAISS 索引文件和在线 LLM API 之间的数据交换。在线 LLM 只用于增强综述生成，不是系统唯一生成方式。

## 图 3-2 0 层数据流图

导出图片：docs/diagrams/figure-3-2-dfd-level0.png

```mermaid
flowchart TB
    U["普通用户"]
    A["管理员"]
    L["在线 LLM API"]
    D1[("user / category / literature")]
    D2[("favorite / search_history / review_record")]
    D3[("llm_config")]
    D4[("FAISS 索引文件")]

    P1(("1 用户认证"))
    P2(("2 文献检索"))
    P3(("3 智能检索"))
    P4(("4 收藏管理"))
    P5(("5 综述生成"))
    P6(("6 后台管理"))
    P7(("7 LLM API 管理"))

    U -->|"登录请求"| P1
    P1 -->|"登录结果 / JWT"| U
    P1 -->|"读写用户信息"| D1

    U -->|"文献检索条件"| P2
    P2 -->|"文献列表 / 文献详情"| U
    P2 -->|"查询文献与分类 / 写入检索历史"| D1
    P2 -->|"检索历史"| D2

    U -->|"智能检索 query"| P3
    P3 -->|"similarity / finalScore / matchReason"| U
    P3 -->|"读取文献数据 / MySQL 补充召回"| D1
    P3 -->|"语义召回"| D4

    U -->|"收藏或取消收藏请求"| P4
    P4 -->|"收藏状态 / 我的收藏"| U
    P4 -->|"读写收藏数据"| D2

    U -->|"综述主题 / 文献 ID / mode"| P5
    P5 -->|"综述内容 / generationMode"| U
    P5 -->|"读取文献 / 保存综述记录"| D1
    P5 -->|"保存 review_record"| D2
    P5 -->|"读取 active LLM 配置"| D3
    P5 -->|"在线生成请求"| L
    L -->|"生成文本或错误"| P5

    A -->|"文献、分类、用户、统计、索引请求"| P6
    P6 -->|"后台管理结果 / 统计数据"| A
    P6 -->|"读写业务数据"| D1
    P6 -->|"读写统计相关数据"| D2
    P6 -->|"重建索引"| D4

    A -->|"LLM 配置增删改查 / 测试 / 激活"| P7
    P7 -->|"配置列表 / 脱敏 Key / 测试结果"| A
    P7 -->|"读写 LLM 配置"| D3
    P7 -->|"测试连接请求"| L
    L -->|"连接测试结果"| P7
```

## 图 3-3 智能检索 1 层数据流图

导出图片：docs/diagrams/figure-3-3-dfd-ai-search.png

```mermaid
flowchart TD
    U["普通用户"]
    D1[("MySQL：literature / category")]
    D2[("FAISS 索引文件")]

    P31(("3.1 接收 query 与 topK"))
    P32(("3.2 扩大 recallTopK"))
    P33(("3.3 backend-ai 语义召回"))
    P34(("3.4 多学科主题画像识别"))
    P35(("3.5 Query Expansion"))
    P36(("3.6 MySQL 关键词补充召回"))
    P37(("3.7 合并去重候选"))
    P38(("3.8 keywordScore / categoryScore / weakPenalty"))
    P39(("3.9 finalScore 综合重排"))

    U -->|"query / topK"| P31
    P31 -->|"检索参数"| P32
    P32 -->|"recallTopK"| P33
    P33 -->|"查询向量与候选 ID"| D2
    D2 -->|"FAISS 候选 / similarity"| P33
    P33 -->|"基础候选"| P34
    P34 -->|"主题画像"| P35
    P35 -->|"扩展词"| P36
    P36 -->|"按标题、关键词、摘要、正文补充召回"| D1
    D1 -->|"补充候选文献"| P36
    P33 -->|"FAISS 候选"| P37
    P36 -->|"MySQL 补充候选"| P37
    P37 -->|"去重候选集"| P38
    P38 -->|"综合评分要素"| P39
    P39 -->|"similarity / finalScore / matchReason"| U
```

## 图 3-4 综述生成 1 层数据流图

导出图片：docs/diagrams/figure-3-4-dfd-review-generation.png

```mermaid
flowchart TD
    U["普通用户"]
    L["在线 LLM API"]
    D1[("MySQL：literature")]
    D2[("MySQL：llm_config")]
    D3[("MySQL：review_record")]
    D4[("FAISS 索引文件")]

    P51(("5.1 输入综述主题"))
    P52(("5.2 智能推荐参考文献"))
    P53(("5.3 用户选择 2 到 5 篇文献"))
    P54(("5.4 判断生成模式 rule / llm"))
    P55(("5.5 离线规则生成"))
    P56(("5.6 读取 active LLM 配置"))
    P57(("5.7 在线 LLM 生成"))
    P58(("5.8 LLM 输出校验"))
    P59(("5.9 参考文献补全与非法引用清理"))
    P60(("5.10 降级为 llm_fallback_rule"))
    P61(("5.11 保存 review_record"))

    U -->|"综述主题"| P51
    P51 -->|"主题 query"| P52
    P52 -->|"语义召回"| D4
    P52 -->|"读取候选文献"| D1
    P52 -->|"推荐参考文献列表"| U
    U -->|"选择文献 ID / mode"| P53
    P53 -->|"文献 ID 列表"| P54
    P54 -->|"mode=rule"| P55
    P55 -->|"离线综述内容 / generationMode=rule"| P61
    P54 -->|"mode=llm"| P56
    P56 -->|"读取 active 配置"| D2
    D2 -->|"Base URL / Model / 服务端密钥配置 / Timeout"| P57
    P57 -->|"在线生成请求"| L
    L -->|"生成文本 / finish_reason / 错误"| P57
    P57 -->|"LLM 输出"| P58
    P58 -->|"校验通过"| P59
    P59 -->|"补全文献来源后的综述 / generationMode=llm"| P61
    P58 -->|"超时、空响应、截断或章节缺失"| P60
    P60 -->|"离线兜底综述 / generationMode=llm_fallback_rule"| P61
    P61 -->|"保存综述记录"| D3
    P61 -->|"综述内容 / generationMode"| U
```

## 图 4-1 系统总体架构图

导出图片：docs/diagrams/figure-4-1-system-architecture.png

```mermaid
flowchart LR
    U["普通用户 / 管理员"] --> FE["React 前端<br/>Vite + TypeScript"]
    FE --> BE["Spring Boot 后端<br/>REST API / JWT / MyBatis-Plus"]
    BE --> DB["MySQL<br/>用户、文献、分类、收藏、综述、LLM 配置"]
    BE --> AI["FastAPI backend-ai<br/>智能检索服务"]
    AI --> ST["sentence-transformers<br/>文本向量化"]
    AI --> FAISS["FAISS 索引文件<br/>backend-ai/data/faiss.index<br/>backend-ai/data/id_map.json"]
    BE --> LLM["OpenAI-compatible LLM API<br/>DeepSeek 等兼容服务"]
```

说明：Spring Boot 是主业务后端，负责权限、数据库访问、智能检索重排和综述生成分流。backend-ai 负责向量化、FAISS 召回和相似推荐。在线 LLM 是增强能力，离线综述生成仍是默认稳定模式。

## 图 4-2 功能结构图

导出图片：docs/diagrams/figure-4-2-function-structure.png

```mermaid
flowchart TB
    SYS["校园学术文献智能检索与综述生成系统"]

    SYS --> USER["普通用户端"]
    USER --> U1["注册与登录"]
    USER --> U2["普通检索 / 高级检索"]
    USER --> U3["多学科智能语义检索"]
    USER --> U4["文献详情 / 收藏 / 我的收藏"]
    USER --> U5["相似文献推荐"]
    USER --> U6["智能推荐参考文献"]
    USER --> U7["离线综述生成"]
    USER --> U8["在线 LLM 增强综述生成"]
    USER --> U9["综述记录查看与 generationMode 筛选"]

    SYS --> ADMIN["管理员端"]
    ADMIN --> A1["管理员登录"]
    ADMIN --> A2["文献管理"]
    ADMIN --> A3["分类管理"]
    ADMIN --> A4["用户管理"]
    ADMIN --> A5["数据统计"]
    ADMIN --> A6["重建智能检索索引"]
    ADMIN --> A7["LLM API 管理"]
    ADMIN --> A8["测试 LLM API 连接"]

    SYS --> AI["backend-ai 智能检索服务"]
    AI --> I1["健康检查"]
    AI --> I2["重建 FAISS 索引"]
    AI --> I3["语义召回"]
    AI --> I4["相似推荐"]

    SYS --> LLM["在线 LLM 增强服务"]
    LLM --> L1["读取 active LLM 配置"]
    LLM --> L2["调用 OpenAI-compatible API"]
    LLM --> L3["输出完整性校验"]
    LLM --> L4["参考文献补全与引用清理"]
    LLM --> L5["失败时降级为 llm_fallback_rule"]
```

## 图 4-3 数据库 E-R 图

导出图片：docs/diagrams/figure-4-3-er-diagram.png

```mermaid
erDiagram
    user ||--o{ favorite : "逻辑关联 user_id"
    literature ||--o{ favorite : "逻辑关联 literature_id"
    user ||--o{ search_history : "逻辑关联 user_id"
    user ||--o{ review_record : "逻辑关联 user_id"
    category ||--o{ category : "逻辑关联 parent_id"
    category ||--o{ literature : "逻辑关联 category_id"
    literature ||--o{ literature_vector : "辅助表 literature_id"
    user ||--o{ admin_log : "辅助表 admin_id"

    user {
        BIGINT id PK
        VARCHAR username
        VARCHAR password
        VARCHAR email
        VARCHAR role
        TINYINT status
        DATETIME created_at
        DATETIME updated_at
    }

    category {
        BIGINT id PK
        VARCHAR name
        BIGINT parent_id
        VARCHAR description
        INT sort_order
        DATETIME created_at
        DATETIME updated_at
    }

    literature {
        BIGINT id PK
        VARCHAR title
        VARCHAR authors
        TEXT abstract_text
        TEXT keywords
        VARCHAR journal
        INT publish_year
        VARCHAR doi
        BIGINT category_id
        VARCHAR document_type
        TEXT content
        INT view_count
        INT favorite_count
        DATETIME created_at
        DATETIME updated_at
    }

    favorite {
        BIGINT id PK
        BIGINT user_id
        BIGINT literature_id
        DATETIME created_at
    }

    search_history {
        BIGINT id PK
        BIGINT user_id
        VARCHAR keyword
        VARCHAR search_type
        INT result_count
        DATETIME created_at
    }

    review_record {
        BIGINT id PK
        BIGINT user_id
        VARCHAR topic
        TEXT literature_ids
        TEXT content
        VARCHAR generation_mode
        DATETIME created_at
        DATETIME updated_at
    }

    llm_config {
        BIGINT id PK
        VARCHAR name
        VARCHAR provider
        VARCHAR base_url
        VARCHAR model
        TEXT api_key
        TINYINT enabled
        TINYINT active
        INT timeout_seconds
        VARCHAR remark
        DATETIME created_at
        DATETIME updated_at
    }

    admin_log {
        BIGINT id PK
        BIGINT admin_id
        VARCHAR action
        VARCHAR target_type
        BIGINT target_id
        TEXT detail
        DATETIME created_at
    }

    literature_vector {
        BIGINT id PK
        BIGINT literature_id
        VARCHAR vector_id
        VARCHAR embedding_model
        DATETIME created_at
        DATETIME updated_at
    }
```

说明：当前 SQL 主要通过字段建立逻辑关联，并未为所有关系显式声明外键。`admin_log` 和 `literature_vector` 是辅助表；实际 FAISS 向量索引文件保存在 `backend-ai/data`，不存入 MySQL。

## 图 5-1 普通用户用例图

导出图片：docs/diagrams/figure-5-1-user-usecase.png

```mermaid
flowchart LR
    USER(("普通用户"))

    subgraph UC["普通用户用例"]
        UC1["登录注册"]
        UC2["普通检索"]
        UC3["高级检索"]
        UC4["智能检索"]
        UC5["查看文献详情"]
        UC6["收藏文献"]
        UC7["相似文献推荐"]
        UC8["离线综述生成"]
        UC9["在线 LLM 综述生成"]
        UC10["查看综述记录"]
    end

    USER --> UC1
    USER --> UC2
    USER --> UC3
    USER --> UC4
    USER --> UC5
    USER --> UC6
    USER --> UC7
    USER --> UC8
    USER --> UC9
    USER --> UC10
```

## 图 5-2 管理员用例图

导出图片：docs/diagrams/figure-5-2-admin-usecase.png

```mermaid
flowchart LR
    ADMIN(("管理员"))

    subgraph AC["管理员用例"]
        AC1["文献管理"]
        AC2["分类管理"]
        AC3["用户管理"]
        AC4["数据统计"]
        AC5["重建智能索引"]
        AC6["LLM API 管理"]
        AC7["测试 LLM 连接"]
    end

    ADMIN --> AC1
    ADMIN --> AC2
    ADMIN --> AC3
    ADMIN --> AC4
    ADMIN --> AC5
    ADMIN --> AC6
    ADMIN --> AC7
```

## 图 6-1 智能检索流程图

导出图片：docs/diagrams/figure-6-1-semantic-search-flow.png

```mermaid
flowchart TD
    Q["用户 query"] --> SB["Spring Boot 接收 /api/ai/semantic-search"]
    SB --> RTK["扩大 recallTopK<br/>min(max(topK * 5, 100), 150)"]
    RTK --> BAI["backend-ai FAISS 语义召回"]
    BAI --> STV["sentence-transformers 生成查询向量"]
    STV --> FR["FAISS 返回基础候选与 similarity"]
    FR --> TP["多学科主题画像识别"]
    TP --> QE["Query Expansion 查询扩展"]
    QE --> MR["MySQL 关键词补充召回"]
    MR --> MERGE["合并去重候选文献"]
    MERGE --> KS["计算 keywordScore"]
    KS --> CS["计算 categoryScore"]
    CS --> WP["计算 weakPenalty"]
    WP --> FS["finalScore 综合重排"]
    FS --> OUT["返回 similarity / finalScore / matchReason"]
```

## 图 6-2 综述生成流程图

导出图片：docs/diagrams/figure-6-2-review-generation-flow.png

```mermaid
flowchart TD
    T["输入综述主题"] --> REC["智能推荐参考文献"]
    REC --> SEL["用户选择 2 到 5 篇文献"]
    SEL --> MODE{"选择生成模式"}

    MODE -->|rule| RULE["离线规则生成"]
    RULE --> SAVE1["保存 review_record<br/>generationMode=rule"]

    MODE -->|llm| LLM["在线 LLM 增强生成"]
    LLM --> CHECK["LLM 输出校验<br/>finish_reason / 正文完整性 / 结尾截断"]
    CHECK --> REF["参考文献补全或替换<br/>非法引用编号清理"]
    REF --> OK{"校验是否通过"}
    OK -->|通过| SAVE2["保存 review_record<br/>generationMode=llm"]
    OK -->|失败| FALLBACK["降级为离线生成<br/>llm_fallback_rule"]
    FALLBACK --> SAVE3["保存 review_record<br/>generationMode=llm_fallback_rule"]

    SAVE1 --> HISTORY["综述记录展示 generationMode"]
    SAVE2 --> HISTORY
    SAVE3 --> HISTORY
```

## 图 6-3 在线 LLM 综述生成时序图

导出图片：docs/diagrams/figure-6-3-llm-review-sequence.png

```mermaid
sequenceDiagram
    actor U as 用户
    participant F as 前端
    participant S as Spring Boot
    participant DB as MySQL / llm_config
    participant L as DeepSeek 或 OpenAI-compatible LLM API
    participant R as review_record

    U->>F: 输入主题并选择 2 到 5 篇文献
    U->>F: 选择 llm 在线生成
    F->>S: POST /api/reviews/generate mode=llm
    S->>DB: 查询 active=1 且 enabled=1 的 LLM 配置
    DB-->>S: 返回 Base URL / Model / API Key / Timeout
    S->>L: 调用 Chat Completions API
    L-->>S: 返回生成文本和 finish_reason
    S->>S: 校验正文完整性、截断和引用编号
    S->>S: 补全或替换第六节参考文献来源
    alt LLM 输出有效
        S->>R: 保存 generation_mode=llm
        S-->>F: 返回 generationMode=llm
    else LLM 不可用或输出不完整
        S->>S: 调用离线规则生成
        S->>R: 保存 generation_mode=llm_fallback_rule
        S-->>F: 返回 generationMode=llm_fallback_rule
    end
    F-->>U: 展示综述结果
```

## 图 6-4 管理员重建智能索引时序图

导出图片：docs/diagrams/figure-6-4-rebuild-index-sequence.png

```mermaid
sequenceDiagram
    actor A as 管理员
    participant F as 前端
    participant S as Spring Boot
    participant DB as MySQL
    participant B as backend-ai
    participant ST as sentence-transformers
    participant FA as FAISS

    A->>F: 点击重建智能检索索引
    F->>S: POST /api/ai/rebuild-index
    S->>DB: 查询文献数据
    DB-->>S: 返回文献列表
    S->>B: POST /rebuild-index
    B->>ST: 对标题、分类、关键词、摘要、正文节选向量化
    ST-->>B: 返回文本向量
    B->>FA: 写入 faiss.index 和 id_map.json
    FA-->>B: 索引保存成功
    B-->>S: 返回重建数量和状态
    S-->>F: 返回成功提示
    F-->>A: 展示重建结果
```

## 图 6-5 LLM API 管理流程图

导出图片：docs/diagrams/figure-6-5-llm-api-management-flow.png

```mermaid
flowchart TD
    START["管理员进入 LLM API 管理"] --> LIST["查看配置列表<br/>API Key 脱敏展示"]
    LIST --> OP{"选择操作"}

    OP --> ADD["新增配置<br/>Provider / Base URL / Model / API Key / Timeout"]
    ADD --> MASK["保存后列表仅显示 apiKeyMasked"]

    OP --> EDIT["编辑配置"]
    EDIT --> KEEP["API Key 留空时保留旧 Key"]
    KEEP --> MASK

    OP --> TEST["测试连接"]
    TEST --> CALL["调用 OpenAI-compatible Chat Completions API"]
    CALL --> RESULT["返回连接成功或失败信息"]

    OP --> ACTIVE["设置 active"]
    ACTIVE --> DEACT["先取消其他 active 配置"]
    DEACT --> SET["当前配置 active=1"]

    MASK --> LIST
    RESULT --> LIST
    SET --> LIST

    LIST --> GEN["用户发起在线综述生成"]
    GEN --> READ["Spring Boot 读取 active=1 且 enabled=1 配置"]
    READ --> LLM["调用在线 LLM 增强生成"]
```

## 图 6-6 RAG 风格综述生成流程图

导出图片：docs/diagrams/figure-6-6-rag-review-flow.png

```mermaid
flowchart TD
    T["用户输入综述主题"] --> REC["智能检索推荐相关文献"]
    REC --> FAISS["FAISS 语义召回"]
    REC --> MYSQL["MySQL 读取文献元数据与正文节选"]
    FAISS --> CAND["候选参考文献"]
    MYSQL --> CAND
    CAND --> SEL["用户选择 2 到 5 篇文献"]
    SEL --> EXT["提取标题 / 关键词 / 摘要 / 正文节选"]
    EXT --> CTX["组织生成上下文"]
    CTX --> MODE{"选择生成模式"}

    MODE -->|rule| RULE["离线规则归纳生成"]
    MODE -->|llm| LLM["在线 LLM 增强生成<br/>受约束 prompt"]

    RULE --> REF["参考文献来源补全或替换"]
    LLM --> CHECK["LLM 输出校验<br/>正文完整性 / finish_reason / 截断检查"]
    CHECK --> REF
    REF --> CLEAN["清理非法引用编号"]
    CLEAN --> SAVE["保存 review_record"]
    SAVE --> GM["展示 generationMode<br/>rule / llm / llm_fallback_rule"]

    CHECK -->|失败| FALLBACK["降级为离线规则生成"]
    FALLBACK --> REF
```

说明：本图表达的是本项目采用的 RAG 思想和检索增强式综述生成流程，不表示完整工业级 RAG 平台。系统没有实现复杂 chunk 管理、专门向量数据库服务、多轮 RAG Agent 或流式 RAG。

## 图 4-4 模块结构图（SC 图）

导出图片：docs/diagrams/figure-4-4-module-structure-sc.png

```mermaid
flowchart TD
    SYS["校园学术文献智能检索与综述生成系统"]

    SYS --> USER["普通用户端"]
    USER --> U1["注册登录"]
    USER --> U2["普通检索 / 高级检索"]
    USER --> U3["智能检索"]
    USER --> U4["文献详情 / 收藏 / 相似推荐"]
    USER --> U5["综述生成"]
    USER --> U6["综述记录与 generationMode 筛选"]

    SYS --> ADMIN["管理员端"]
    ADMIN --> A1["文献管理"]
    ADMIN --> A2["分类管理"]
    ADMIN --> A3["用户管理"]
    ADMIN --> A4["真实数据统计"]
    ADMIN --> A5["智能索引重建"]
    ADMIN --> A6["LLM API 管理"]

    SYS --> SPRING["Spring Boot 主业务模块"]
    SPRING --> C1["Controller 接口层"]
    SPRING --> C2["Service 业务层"]
    SPRING --> C3["Mapper 数据访问层"]
    SPRING --> C4["Security 权限控制"]

    SYS --> AI["backend-ai 智能检索模块"]
    AI --> B1["健康检查"]
    AI --> B2["索引重建"]
    AI --> B3["语义检索"]
    AI --> B4["相似推荐"]

    SYS --> DATA["数据存储模块"]
    DATA --> D1["MySQL 业务数据"]
    DATA --> D2["FAISS 索引文件"]
    DATA --> D3["id_map.json 映射文件"]

    SYS --> LLM["在线 LLM 增强模块"]
    LLM --> L1["读取 active LLM 配置"]
    LLM --> L2["调用 OpenAI-compatible API"]
    LLM --> L3["输出完整性校验"]
    LLM --> L4["参考文献补全与降级"]
```

说明：SC 图强调系统模块层次和调用职责。普通用户端和管理员端通过 Spring Boot 访问业务能力；backend-ai 只负责语义向量检索和索引文件；在线 LLM 是可选增强模块，不是系统唯一生成方式。

## 图 5-3 面向对象类图

导出图片：docs/diagrams/figure-5-3-class-diagram.png

```mermaid
classDiagram
    class User {
        Long id
        String username
        String passwordHash
        String role
        Integer status
    }

    class Category {
        Long id
        String name
        Long parentId
        Integer sortOrder
    }

    class Literature {
        Long id
        String title
        String authors
        String abstractText
        String keywords
        Long categoryId
        String content
    }

    class Favorite {
        Long id
        Long userId
        Long literatureId
    }

    class SearchHistory {
        Long id
        Long userId
        String keyword
        String searchType
        Integer resultCount
    }

    class ReviewRecord {
        Long id
        Long userId
        String topic
        String literatureIds
        String content
        String referenceText
        String generationMode
    }

    class LlmConfig {
        Long id
        String provider
        String baseUrl
        String model
        String apiKey
        Boolean active
        Integer timeoutSeconds
    }

    class Controller {
        接收 REST 请求
        参数校验
        返回统一 Result
    }

    class Service {
        业务规则
        智能检索重排
        综述生成
        LLM 校验降级
    }

    class Mapper {
        MyBatis-Plus 数据访问
    }

    User "1" --> "*" Favorite : user_id 逻辑关联
    Literature "1" --> "*" Favorite : literature_id 逻辑关联
    User "1" --> "*" SearchHistory : user_id 逻辑关联
    User "1" --> "*" ReviewRecord : user_id 逻辑关联
    Category "1" --> "*" Category : parent_id 两级分类
    Category "1" --> "*" Literature : category_id 逻辑关联
    ReviewRecord "*" --> "*" Literature : literature_ids 保存选中文献
    Controller --> Service
    Service --> Mapper
    Mapper --> User
    Mapper --> Literature
    Mapper --> Category
    Mapper --> ReviewRecord
    Service --> LlmConfig
```

说明：本图体现面向对象设计中的实体类、业务层和数据访问层。SQL 中多数关系以逻辑关联实现，报告中不应写成数据库强制外键。

## 图 5-4 智能检索重排算法图

导出图片：docs/diagrams/figure-5-4-semantic-rerank-algorithm.png

```mermaid
flowchart TD
    Q["用户 query"] --> RECALL["扩大 recallTopK"]
    RECALL --> FAISS["backend-ai / FAISS 语义召回"]
    Q --> TOPIC["多学科主题画像识别"]
    TOPIC --> EXP["Query Expansion 查询扩展"]
    EXP --> MYSQL["MySQL 关键词补充召回"]

    FAISS --> MERGE["合并候选"]
    MYSQL --> MERGE
    MERGE --> DEDUP["按 literatureId 去重"]
    DEDUP --> SCORE["逐篇计算得分"]

    SCORE --> SEM["semanticSimilarity"]
    SCORE --> KEY["keywordScore"]
    SCORE --> CAT["categoryScore"]
    SCORE --> PEN["weakPenalty"]

    SEM --> FORMULA["finalScore = semantic * 0.60 + keyword * 0.30 + category * 0.10 - weakPenalty"]
    KEY --> FORMULA
    CAT --> FORMULA
    PEN --> FORMULA

    FORMULA --> REASON["生成 matchReason"]
    REASON --> SORT["按 finalScore 降序排序"]
    SORT --> RESULT["返回 similarity / finalScore / matchReason"]
```

说明：本图来自 `AiServiceImpl.java` 中的智能检索实现。它体现本项目不是直接返回 FAISS TopK，而是将语义召回、关键词补充召回、分类得分和弱相关降权合并为最终排序。

## 图 5-5 LLM 输出校验与降级流程图

导出图片：docs/diagrams/figure-5-5-llm-validation-fallback.png

```mermaid
flowchart TD
    START["用户选择 llm 在线综述生成"] --> ACTIVE{"是否存在 active LLM 配置"}
    ACTIVE -->|否| RULE["离线 rule 生成"]
    ACTIVE -->|是| PROMPT["基于选中文献组织受约束 prompt"]
    PROMPT --> CALL["调用 OpenAI-compatible LLM API"]
    CALL --> EMPTY{"响应是否为空或异常"}
    EMPTY -->|是| FALLBACK["降级 llm_fallback_rule"]
    EMPTY -->|否| FINISH{"finish_reason 是否为 length"}
    FINISH -->|是| RETRY["提高 max_tokens 重试"]
    RETRY --> LENGTH2{"是否仍 length 截断"}
    LENGTH2 -->|是| FALLBACK
    LENGTH2 -->|否| SECTION["检查前五节正文完整性"]
    FINISH -->|否| SECTION
    SECTION --> MISS{"是否缺少正文部分"}
    MISS -->|是| FALLBACK
    MISS -->|否| ENDING{"结尾是否明显截断"}
    ENDING -->|是| FALLBACK
    ENDING -->|否| REF["后端补全或替换参考文献来源"]
    REF --> CLEAN["清理未选择文献的引用编号"]
    CLEAN --> SAVE["保存 review_record / generationMode=llm"]
    RULE --> SAVE_RULE["保存 review_record / generationMode=rule"]
    FALLBACK --> RULE2["离线 rule 生成"]
    RULE2 --> SAVE_FB["保存 review_record / generationMode=llm_fallback_rule"]
```

说明：本图来自 `ReviewRecordServiceImpl.java` 和 `LlmReviewServiceImpl.java` 的实际逻辑。第六节参考文献来源由后端补全或替换，避免 LLM 编造不存在的参考文献。

## 图 6-20 项目代码结构图

导出图片：docs/diagrams/figure-6-20-code-structure.png

```mermaid
flowchart TD
    ROOT["campus-literature-system"]

    ROOT --> FE["frontend"]
    FE --> FE_API["src/api<br/>请求封装"]
    FE --> FE_ROUTES["src/routes<br/>页面路由"]
    FE --> FE_COMP["src/components<br/>通用组件"]

    ROOT --> BJ["backend-java"]
    BJ --> BJ_CTRL["controller<br/>REST 接口"]
    BJ --> BJ_SERVICE["service/impl<br/>业务实现"]
    BJ --> BJ_MAPPER["mapper<br/>数据库访问"]
    BJ --> BJ_ENTITY["entity / dto / vo<br/>数据对象"]
    BJ --> BJ_SECURITY["security<br/>JWT 与权限拦截"]

    ROOT --> BA["backend-ai"]
    BA --> BA_MAIN["main.py<br/>FastAPI 服务"]
    BA --> BA_DATA["data/faiss.index<br/>data/id_map.json"]
    BA --> BA_REQ["requirements.txt"]

    ROOT --> DB["database"]
    DB --> DB_INIT["init.sql"]
    DB --> DB_CAT["update_categories.sql"]
    DB --> DB_SEED["literature_seed_500_high_quality.sql"]
    DB --> DB_LLM["update_llm_config.sql"]

    ROOT --> DOCS["docs"]
    DOCS --> REPORT["final-report-draft.md / final-report.docx"]
    DOCS --> DIAG["diagrams.md / diagrams 图片"]
    DOCS --> SHOT["screenshots 运行截图"]
```

说明：本图用于编码实现章节，说明项目目录和核心代码位置。它不表示新增代码结构，只是对现有仓库组织进行可视化整理。

## 图 7-1 测试与缺陷闭环图

导出图片：docs/diagrams/figure-7-1-test-defect-loop.png

```mermaid
flowchart LR
    PLAN["制定测试计划"] --> CASE["设计测试用例"]
    CASE --> EXEC["执行构建 / 接口 / 页面 / 边界测试"]
    EXEC --> RECORD{"是否发现问题"}
    RECORD -->|否| PASS["记录通过结果"]
    RECORD -->|是| BUG["记录缺陷现象"]
    BUG --> ANALYZE["分析原因"]
    ANALYZE --> FIX["修复实现或配置"]
    FIX --> REG["回归验证"]
    REG --> RESULT{"是否修复"}
    RESULT -->|否| ANALYZE
    RESULT -->|是| PASS
    PASS --> REPORT["整理测试报告和缺陷分析"]

    EXEC --> BUILD["编译构建测试"]
    EXEC --> API["接口测试"]
    EXEC --> UI["页面操作测试"]
    EXEC --> SEC["权限与异常测试"]
    EXEC --> LLM["LLM 降级与脱敏测试"]
```

说明：本图对应系统测试章节，用于展示从测试计划、用例执行、缺陷记录、修复到回归验证的闭环过程。
