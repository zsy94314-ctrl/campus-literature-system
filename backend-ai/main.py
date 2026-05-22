"""
backend-ai: 校园学术文献智能语义检索服务
技术栈: FastAPI + sentence-transformers + FAISS
"""

import os
import json
import faiss
import numpy as np
from typing import List, Optional
from fastapi import FastAPI
from pydantic import BaseModel, Field
from sentence_transformers import SentenceTransformer

# ==================== 配置 ====================
MODEL_NAME = "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"
DATA_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "data")
FAISS_INDEX_PATH = os.path.join(DATA_DIR, "faiss.index")
ID_MAP_PATH = os.path.join(DATA_DIR, "id_map.json")

os.makedirs(DATA_DIR, exist_ok=True)

# ==================== FastAPI 实例 ====================
app = FastAPI(title="backend-ai", version="1.0.0", description="学术文献语义检索服务")

# ==================== 全局状态 ====================
model: Optional[SentenceTransformer] = None
faiss_index: Optional[faiss.IndexFlatIP] = None
id_map: List[int] = []  # faiss 向量位置 -> literatureId


# ==================== Pydantic 模型 ====================
class DocumentItem(BaseModel):
    """单篇文献向量化输入"""
    id: int = Field(..., description="文献唯一ID")
    title: str = Field(..., description="文献标题")
    categoryName: str = Field("", description="学科分类名称")
    documentType: str = Field("", description="文献类型")
    keywords: str = Field("", description="关键词，逗号分隔")
    abstractText: str = Field("", description="摘要")
    content: str = Field("", description="正文节选")


class RebuildIndexRequest(BaseModel):
    """重建索引请求"""
    documents: List[DocumentItem] = Field(..., description="文献列表，不能为空")


class SemanticSearchRequest(BaseModel):
    """语义检索请求"""
    query: str = Field(..., description="检索语句，不能为空")
    topK: int = Field(10, ge=1, le=100, description="返回结果数量")


class RecommendRequest(BaseModel):
    """相似文献推荐请求"""
    literatureId: int = Field(..., description="目标文献ID")
    topK: int = Field(5, ge=1, le=100, description="返回结果数量")


class SearchResultItem(BaseModel):
    """检索结果项"""
    literatureId: int
    similarity: float


class SuccessResponse(BaseModel):
    """统一成功响应"""
    code: int = 200
    message: str = "success"
    data: Optional[dict] = None


class ErrorResponse(BaseModel):
    """统一错误响应"""
    code: int = 500
    message: str
    data: Optional[dict] = None


# ==================== 工具函数 ====================
def load_model() -> SentenceTransformer:
    """加载 sentence-transformers 模型，支持首次自动下载"""
    global model
    if model is None:
        try:
            model = SentenceTransformer(MODEL_NAME)
        except Exception as e:
            raise RuntimeError(f"模型加载失败: {e}")
    return model


def build_vector_text(doc: DocumentItem) -> str:
    """
    拼接文献向量化文本。
    标题和关键词重复一次以提高语义权重。
    """
    parts = [
        f"标题：{doc.title}",
        f"标题：{doc.title}",
        f"学科分类：{doc.categoryName}",
        f"文献类型：{doc.documentType}",
        f"关键词：{doc.keywords}",
        f"关键词：{doc.keywords}",
        f"摘要：{doc.abstractText}",
        f"正文节选：{doc.content}",
    ]
    return "\n".join(parts)


def normalize_embeddings(embeddings: np.ndarray) -> np.ndarray:
    """对 embedding 做 L2 归一化，使内积等价于余弦相似度"""
    norms = np.linalg.norm(embeddings, axis=1, keepdims=True)
    # 避免除零
    norms = np.where(norms == 0, 1, norms)
    return embeddings / norms


def save_index(index: faiss.IndexFlatIP, ids: List[int]) -> None:
    """持久化 FAISS 索引与 ID 映射"""
    faiss.write_index(index, FAISS_INDEX_PATH)
    with open(ID_MAP_PATH, "w", encoding="utf-8") as f:
        json.dump(ids, f, ensure_ascii=False, indent=2)


def load_index() -> bool:
    """加载 FAISS 索引与 ID 映射，返回是否加载成功"""
    global faiss_index, id_map
    if not os.path.exists(FAISS_INDEX_PATH) or not os.path.exists(ID_MAP_PATH):
        return False
    try:
        faiss_index = faiss.read_index(FAISS_INDEX_PATH)
        with open(ID_MAP_PATH, "r", encoding="utf-8") as f:
            id_map = json.load(f)
        return True
    except Exception:
        faiss_index = None
        id_map = []
        return False


def check_index_ready() -> Optional[ErrorResponse]:
    """检查索引是否就绪，未就绪返回错误响应"""
    global faiss_index, id_map
    if faiss_index is None or not id_map:
        loaded = load_index()
        if not loaded:
            return ErrorResponse(message="请先重建智能检索索引")
    return None


# ==================== 生命周期 ====================
@app.on_event("startup")
def startup_event():
    """服务启动时自动加载模型与索引"""
    try:
        load_model()
        load_index()
    except Exception as e:
        # 模型加载失败允许服务启动，但后续接口会报错
        print(f"[startup warning] {e}")


# ==================== 接口实现 ====================
@app.get("/health")
def health_check():
    """健康检查接口"""
    return {"status": "ok", "message": "backend-ai is running"}


@app.post("/rebuild-index", response_model=SuccessResponse)
def rebuild_index(request: RebuildIndexRequest):
    """
    重建语义检索索引。
    将文献列表转成向量，建立 FAISS 索引并持久化。
    """
    # 1. 校验输入
    if not request.documents:
        return ErrorResponse(message="documents 不能为空")

    try:
        # 2. 加载模型
        st_model = load_model()

        # 3. 拼接文本并生成 embedding
        texts = [build_vector_text(doc) for doc in request.documents]
        embeddings = st_model.encode(texts, show_progress_bar=False, convert_to_numpy=True)

        # 4. 归一化
        embeddings = normalize_embeddings(embeddings)

        # 5. 建立 FAISS 索引（IndexFlatIP 适合已归一化的向量，内积即余弦相似度）
        dim = embeddings.shape[1]
        index = faiss.IndexFlatIP(dim)
        index.add(embeddings.astype(np.float32))

        # 6. 构建 ID 映射
        ids = [doc.id for doc in request.documents]

        # 7. 持久化
        save_index(index, ids)

        # 8. 更新全局状态
        global faiss_index, id_map
        faiss_index = index
        id_map = ids

        return SuccessResponse(
            message="索引重建成功",
            data={"count": len(ids)}
        )

    except RuntimeError as e:
        return ErrorResponse(message=str(e))
    except Exception as e:
        return ErrorResponse(message=f"索引重建异常: {e}")


@app.post("/semantic-search", response_model=SuccessResponse)
def semantic_search(request: SemanticSearchRequest):
    """
    语义检索接口。
    将查询语句转成向量，在 FAISS 中检索最相似的文献。
    """
    # 1. 校验输入
    if not request.query or not request.query.strip():
        return ErrorResponse(message="query 不能为空")

    # 2. 检查索引
    err = check_index_ready()
    if err:
        return err

    try:
        # 3. 加载模型并编码查询
        st_model = load_model()
        query_embedding = st_model.encode(
            [request.query.strip()],
            show_progress_bar=False,
            convert_to_numpy=True
        )

        # 4. 归一化查询向量
        query_embedding = normalize_embeddings(query_embedding)

        # 5. FAISS 检索
        top_k = min(request.topK, len(id_map))
        distances, indices = faiss_index.search(query_embedding.astype(np.float32), top_k)

        # 6. 组装结果（distances 已是归一化后的内积，范围 [-1, 1]，取正值映射到 [0, 1]）
        results: List[dict] = []
        for dist, idx in zip(distances[0], indices[0]):
            if idx < 0 or idx >= len(id_map):
                continue
            # IndexFlatIP 在归一化后，内积 = cosine similarity，范围 [-1, 1]
            # 通常语义相似度不会为负，直接截断到 [0, 1]
            similarity = float(np.clip(dist, 0.0, 1.0))
            results.append({
                "literatureId": id_map[idx],
                "similarity": round(similarity, 4)
            })

        return SuccessResponse(data={"results": results})

    except RuntimeError as e:
        return ErrorResponse(message=str(e))
    except Exception as e:
        return ErrorResponse(message=f"语义检索异常: {e}")


@app.post("/recommend", response_model=SuccessResponse)
def recommend(request: RecommendRequest):
    """
    相似文献推荐接口。
    根据指定文献的向量，检索与其最相似的其他文献。
    """
    # 1. 检查索引
    err = check_index_ready()
    if err:
        return err

    # 2. 校验目标文献是否存在
    if request.literatureId not in id_map:
        return ErrorResponse(message=f"literatureId {request.literatureId} 不存在于索引中")

    try:
        # 3. 找到目标文献在 FAISS 中的位置
        target_idx = id_map.index(request.literatureId)

        # 4. 获取该位置的向量（ reconstruct 方法从索引中还原向量）
        target_vector = np.zeros((1, faiss_index.d), dtype=np.float32)
        faiss_index.reconstruct(target_idx, target_vector[0])

        # 5. 检索 topK+1，以便排除自身
        search_k = min(request.topK + 1, len(id_map))
        distances, indices = faiss_index.search(target_vector, search_k)

        # 6. 组装结果并排除自身
        results: List[dict] = []
        for dist, idx in zip(distances[0], indices[0]):
            if idx < 0 or idx >= len(id_map):
                continue
            if id_map[idx] == request.literatureId:
                continue
            similarity = float(np.clip(dist, 0.0, 1.0))
            results.append({
                "literatureId": id_map[idx],
                "similarity": round(similarity, 4)
            })
            if len(results) >= request.topK:
                break

        return SuccessResponse(data={"results": results})

    except RuntimeError as e:
        return ErrorResponse(message=str(e))
    except Exception as e:
        return ErrorResponse(message=f"相似推荐异常: {e}")
