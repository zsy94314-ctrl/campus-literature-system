# 系统图表汇总

本文档集中保存正式报告可引用的 Mermaid 图表。报告正文建议只插入图号、图名和简要说明，完整 Mermaid 源码以本文档为准。

## 图 4-1 系统总体架构图

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
