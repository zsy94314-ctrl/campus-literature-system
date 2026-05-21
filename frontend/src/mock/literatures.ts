export interface Literature {
  id: string;
  title: string;
  authors: string[];
  abstract: string;
  keywords: string[];
  journal: string;
  year: number;
  doi: string;
  citations: number;
  category: string;
  categoryId?: string;
}

export const mockLiteratures: Literature[] = [
  {
    id: "1",
    title: "基于深度学习的图像识别研究进展",
    authors: ["张三", "李四"],
    abstract:
      "本文综述了近年来深度学习在图像识别领域的最新进展，重点介绍了卷积神经网络、注意力机制以及Transformer架构在视觉任务中的应用，并对未来研究方向进行了展望。",
    keywords: ["深度学习", "图像识别", "卷积神经网络", "Transformer"],
    journal: "计算机学报",
    year: 2023,
    doi: "10.1000/cs.2023.001",
    citations: 128,
    category: "计算机科学",
  },
  {
    id: "2",
    title: "大语言模型在教育领域的应用与挑战",
    authors: ["王五", "赵六", "孙七"],
    abstract:
      "随着GPT等大语言模型的发展，其在教育场景的应用日益广泛。本文分析了大语言模型在个性化学习、智能问答、自动评测等方面的应用现状，并讨论了面临的伦理与技术挑战。",
    keywords: ["大语言模型", "教育技术", "GPT", "个性化学习"],
    journal: "教育研究",
    year: 2024,
    doi: "10.1000/edu.2024.012",
    citations: 56,
    category: "教育学",
  },
  {
    id: "3",
    title: "量子计算的最新发展与产业前景",
    authors: ["陈八"],
    abstract:
      "量子计算正在从实验室走向产业化。本文系统梳理了量子比特、量子算法及量子纠错的最新研究成果，分析了IBM、Google等公司的产业布局。",
    keywords: ["量子计算", "量子算法", "量子纠错"],
    journal: "物理学报",
    year: 2023,
    doi: "10.1000/phy.2023.045",
    citations: 89,
    category: "物理学",
  },
  {
    id: "4",
    title: "可再生能源并网技术综述",
    authors: ["周九", "吴十"],
    abstract:
      "可再生能源高比例接入电网带来诸多技术挑战，本文从功率波动平抑、电力电子接口、储能配置等角度综述了并网关键技术。",
    keywords: ["可再生能源", "电网", "储能", "电力电子"],
    journal: "电力系统自动化",
    year: 2022,
    doi: "10.1000/ee.2022.078",
    citations: 201,
    category: "工程学",
  },
  {
    id: "5",
    title: "新冠疫苗免疫机制研究",
    authors: ["郑十一", "王十二"],
    abstract:
      "本文综述了mRNA疫苗、灭活疫苗、腺病毒载体疫苗的免疫学机制，比较其有效性与安全性，并讨论了变异毒株背景下的疫苗策略。",
    keywords: ["新冠疫苗", "免疫", "mRNA", "公共卫生"],
    journal: "中华医学杂志",
    year: 2022,
    doi: "10.1000/med.2022.099",
    citations: 342,
    category: "医学",
  },
  {
    id: "6",
    title: "区块链在供应链金融中的应用",
    authors: ["黄十三"],
    abstract:
      "本文探讨了区块链技术在解决供应链金融信息不对称、信任缺失等问题中的应用，并通过案例分析说明其落地路径。",
    keywords: ["区块链", "供应链", "金融科技"],
    journal: "管理评论",
    year: 2023,
    doi: "10.1000/mgmt.2023.033",
    citations: 47,
    category: "经济学",
  },
];

export const mockCategories = [
  { id: "1", name: "计算机科学", count: 1280 },
  { id: "2", name: "教育学", count: 540 },
  { id: "3", name: "物理学", count: 720 },
  { id: "4", name: "工程学", count: 980 },
  { id: "5", name: "医学", count: 1530 },
  { id: "6", name: "经济学", count: 460 },
];
