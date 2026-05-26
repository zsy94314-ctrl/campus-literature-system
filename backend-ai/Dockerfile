FROM python:3.11-slim

WORKDIR /app

# 安装系统依赖（sentence-transformers 可能需要编译扩展）
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    curl \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

# 创建数据目录用于挂载卷
RUN mkdir -p /app/data

EXPOSE 8000

# 首次启动时 sentence-transformers 会自动下载模型到本地缓存
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=5 \
    CMD curl -f http://localhost:8000/health || exit 1

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
