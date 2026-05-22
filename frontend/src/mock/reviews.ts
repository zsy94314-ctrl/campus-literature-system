export interface Review {
  id: string;
  topic: string;
  content: string;
  generationMode?: string;
  references: string[]; // literature ids
  createdAt: string;
}

export const mockReviews: Review[] = [
  {
    id: "r1",
    topic: "深度学习在图像识别中的应用",
    content:
      "近年来，深度学习技术在图像识别领域取得了突破性进展。卷积神经网络（CNN）作为该领域的基础架构，被广泛应用于图像分类、目标检测和语义分割等任务 [1]。随着Transformer的兴起，Vision Transformer 等模型展现出超越传统CNN的潜力。本综述系统梳理了相关技术演进路径，并展望了未来研究方向。\n\n总体而言，深度学习推动了计算机视觉的快速发展，但模型可解释性、数据偏差等问题仍待解决。",
    references: ["1", "3"],
    createdAt: "2024-09-20",
  },
  {
    id: "r2",
    topic: "大模型在高等教育中的机遇",
    content:
      "大语言模型为高等教育带来了新的可能。研究表明，其在个性化辅导、自动评分和学术写作辅助方面具有显著价值 [2]。然而其在学术诚信、数据隐私上仍存在挑战。",
    references: ["2"],
    createdAt: "2024-10-05",
  },
];

export const mockSearchHistory = [
  { id: "h1", keyword: "深度学习", time: "2024-10-12 14:23" },
  { id: "h2", keyword: "区块链 供应链", time: "2024-10-10 09:15" },
  { id: "h3", keyword: "量子计算", time: "2024-10-08 16:40" },
  { id: "h4", keyword: "新冠疫苗 mRNA", time: "2024-10-01 11:02" },
];

export const mockStatistics = {
  userCount: 1268,
  literatureCount: 5430,
  searchCount: 28910,
  reviewCount: 432,
};
