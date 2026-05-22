# backend-ai 语义检索服务

校园学术文献智能检索系统的 AI 后端服务，基于 FastAPI + sentence-transformers + FAISS 实现语义向量检索。

## 技术栈

- Python 3.10+
- FastAPI
- uvicorn
- sentence-transformers
- faiss-cpu
- numpy
- pydantic

## 模型

默认使用 `sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2`，首次运行时会自动下载到本地缓存。

## 安装依赖

```bash
pip install -r requirements.txt
```

## 启动服务

```bash
uvicorn main:app --reload --port 8000
```

服务启动后将监听 `http://localhost:8000`。

## 接口文档

启动后访问：`http://localhost:8000/docs`

## 核心接口

### 1. 健康检查

```bash
curl http://localhost:8000/health
```

### 2. 重建索引

```bash
curl -X POST http://localhost:8000/rebuild-index \
  -H "Content-Type: application/json" \
  -d '{
    "documents": [
      {
        "id": 1,
        "title": "示例文献标题",
        "categoryName": "软件工程",
        "documentType": "期刊论文",
        "keywords": "示例,关键词",
        "abstractText": "这是一段示例摘要。",
        "content": "这是一段示例正文节选。"
      }
    ]
  }'
```

### 3. 语义检索

```bash
curl -X POST http://localhost:8000/semantic-search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "AI 如何辅助高校课堂教学",
    "topK": 10
  }'
```

### 4. 相似文献推荐

```bash
curl -X POST http://localhost:8000/recommend \
  -H "Content-Type: application/json" \
  -d '{
    "literatureId": 1,
    "topK": 5
  }'
```

## 数据存储

- `data/faiss.index`：FAISS 向量索引文件
- `data/id_map.json`：向量位置到 literatureId 的映射

## 向量化字段

每篇文献用于语义向量化的字段：

1. title（标题，重复一次以提高权重）
2. categoryName（学科分类）
3. documentType（文献类型）
4. keywords（关键词，重复一次以提高权重）
5. abstractText（摘要）
6. content（正文节选）

不放入向量化的字段：id、doi、sourceUrl、citationCount、publishYear。
