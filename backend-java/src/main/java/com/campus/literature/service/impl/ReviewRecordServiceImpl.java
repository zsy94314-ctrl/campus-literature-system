package com.campus.literature.service.impl;

import com.campus.literature.common.ErrorCode;
import com.campus.literature.dto.ReviewGenerateRequest;
import com.campus.literature.entity.Literature;
import com.campus.literature.entity.LlmConfig;
import com.campus.literature.entity.ReviewRecord;
import com.campus.literature.exception.BusinessException;
import com.campus.literature.mapper.LiteratureMapper;
import com.campus.literature.mapper.LlmConfigMapper;
import com.campus.literature.mapper.ReviewRecordMapper;
import com.campus.literature.security.UserContext;
import com.campus.literature.service.LlmReviewService;
import com.campus.literature.service.ReviewRecordService;
import com.campus.literature.vo.ReviewRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 综述记录服务实现（综合归纳型结构化综述生成）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewRecordServiceImpl implements ReviewRecordService {

    private final ReviewRecordMapper reviewRecordMapper;
    private final LiteratureMapper literatureMapper;
    private final LlmConfigMapper llmConfigMapper;
    private final LlmReviewService llmReviewService;

    // ==================== 综述主题枚举 ====================

    private enum ReviewTopic {
        EDUCATION, MEDICAL, PSYCHOLOGY, LITERATURE_SEARCH,
        AGRICULTURE, LAW, ECONOMY, HUMANITIES, ENGINEERING, GENERAL
    }

    // ==================== 主题检测词 ====================

    private static final List<String> EDU_WORDS = List.of(
            "教学", "课堂", "教育", "学习", "学生", "高校", "课程", "智慧课堂",
            "个性化学习", "教学评价", "学习行为", "智能助教", "教育大模型", "混合式教学");
    private static final List<String> MEDICAL_WORDS = List.of(
            "医学", "医疗", "健康", "临床", "诊断", "疾病", "患者", "医院",
            "医学影像", "辅助诊断", "电子病历", "远程医疗", "公共健康", "健康管理", "医学信息学");
    private static final List<String> PSYCHOLOGY_WORDS = List.of(
            "心理健康", "情绪识别", "风险预警", "学生画像", "心理干预",
            "压力管理", "心理咨询", "心理服务", "心理危机", "心理测评", "情绪调节");
    private static final List<String> SEARCH_WORDS = List.of(
            "文献检索", "学术文献", "智能检索", "语义检索", "向量检索",
            "知识服务", "数字图书馆", "文献计量", "开放获取", "科研数据", "知识图谱");
    private static final List<String> AGRICULTURE_WORDS = List.of(
            "农业", "农学", "作物", "种植", "土壤", "智慧农业", "精准农业",
            "农业遥感", "病虫害", "食品安全", "生态保护", "生态学");
    private static final List<String> LAW_WORDS = List.of(
            "法律", "法学", "知识产权", "隐私保护", "数据治理", "算法治理",
            "平台治理", "合规", "伦理", "监管", "个人信息保护", "社会治理");
    private static final List<String> ECONOMY_WORDS = List.of(
            "经济", "经济学", "管理", "管理学", "数字经济", "企业管理",
            "供应链", "金融", "风险管理", "绩效评价", "决策支持", "创新管理");
    private static final List<String> HUMANITIES_WORDS = List.of(
            "文学", "历史", "哲学", "语言", "艺术", "文化", "文本分析",
            "数字人文", "史料", "语言学", "艺术学", "文化研究", "网络文学");
    private static final List<String> ENGINEERING_WORDS = List.of(
            "工程", "工业物联网", "智能制造", "软件", "系统", "平台",
            "传感器", "嵌入式", "电子", "自动化", "通信", "能源");

    // ==================== 方向定义 ====================

    private static class DirectionDef {
        final String title;
        final List<String> keywords;
        final String template;
        DirectionDef(String title, List<String> keywords, String template) {
            this.title = title;
            this.keywords = keywords;
            this.template = template;
        }
    }

    private static final List<DirectionDef> EDU_DIRECTIONS = List.of(
            new DirectionDef("智能教学支持与个性化学习",
                    List.of("教学", "课堂", "学习", "个性化", "智能问答", "智能助教", "自适应"),
                    "该方向聚焦于人工智能技术在课堂教学与个性化学习中的支撑作用。现有研究关注智能问答系统、学习路径推荐以及自适应学习环境的构建，旨在根据学生特征提供差异化教学支持。相关研究如{refs}，分别从{aspects}等角度展开，表明智能教学正从通用内容推送向精准化、场景化服务演进。"),
            new DirectionDef("学习行为分析与教学评价",
                    List.of("学习行为", "教学评价", "学习分析", "行为分析", "评价指标", "过程评价"),
                    "该方向强调通过对学习过程数据的采集与分析，挖掘学习行为规律并优化教学评价机制。研究涉及学习行为建模、多模态数据采集以及评价指标体系构建，力图实现从结果评价向过程评价的转型。相关研究如{refs}，围绕{aspects}等主题展开，为教学改进提供了数据支撑。"),
            new DirectionDef("智慧课堂与教育数据治理",
                    List.of("智慧课堂", "教育大模型", "数据治理", "混合式教学", "数字校园", "教育技术"),
                    "该方向关注智慧课堂环境的构建与教育数据的安全高效治理。研究探讨了混合式教学模式、课堂互动数据的实时分析以及教育大模型在课程内容生成中的应用。相关研究如{refs}，结合{aspects}等视角，推动了课堂教学从经验驱动向数据驱动的转变。"),
            new DirectionDef("教育公平与教师协同发展",
                    List.of("教育公平", "教师发展", "课程教学", "协同", "资源配置"),
                    "该方向关注人工智能背景下教育资源的均衡配置与教师专业能力的协同发展。研究涉及智能资源推荐、教师培训模式创新以及课程教学质量保障机制。相关研究如{refs}，从{aspects}等维度展开，为缩小教育差距提供了理论与实践参考。")
    );

    private static final List<DirectionDef> MEDICAL_DIRECTIONS = List.of(
            new DirectionDef("智能诊断与辅助决策",
                    List.of("诊断", "辅助诊断", "疾病预测", "临床决策", "智能诊断", "决策支持"),
                    "该方向聚焦于利用人工智能技术提升临床诊断的准确性与效率。现有研究探索了基于深度学习、知识图谱等技术的辅助诊断方法，旨在为医生提供决策支持并降低误诊率。相关研究如{refs}，分别从{aspects}等角度展开，表明智能诊断已成为医学人工智能的核心应用方向之一。"),
            new DirectionDef("医学数据治理与电子病历分析",
                    List.of("电子病历", "医学数据", "数据治理", "医学信息学", "健康数据", "病历分析"),
                    "该方向关注医疗数据的标准化治理与电子病历的深度分析。研究涉及医学信息学框架构建、病历数据结构化处理以及跨机构数据共享机制，力图释放医疗数据的潜在价值。相关研究如{refs}，围绕{aspects}等主题展开，为医学数据的高效利用奠定了基础。"),
            new DirectionDef("健康管理与风险预测",
                    List.of("健康管理", "风险预测", "健康监测", "公共卫生", "健康画像", "风险评估"),
                    "该方向强调面向个体与群体的健康管理及早期风险预测。研究关注健康画像模型构建、慢性病风险预警以及公共卫生事件的智能监测，致力于实现从治疗为主向预防为主的医学模式转变。相关研究如{refs}，结合{aspects}等视角，丰富了健康管理的理论与实践。"),
            new DirectionDef("医学影像与多模态分析",
                    List.of("医学影像", "影像", "多模态", "远程医疗", "图像识别", "病理分析"),
                    "该方向聚焦于医学影像数据的智能分析以及多模态医学信息的融合处理。研究探索了影像自动标注、病灶检测以及远程医疗诊断系统的构建。相关研究如{refs}，从{aspects}等维度切入，推动了医学影像分析从人工判读向智能辅助的转变。")
    );

    private static final List<DirectionDef> PSYCHOLOGY_DIRECTIONS = List.of(
            new DirectionDef("心理风险识别与智能预警",
                    List.of("风险预警", "心理健康", "情绪识别", "预警", "危机识别", "风险识别"),
                    "该方向聚焦于利用数据挖掘与人工智能技术实现心理风险的早期识别与智能预警。研究关注情绪识别模型、心理压力评估指标以及预警系统的构建，旨在及时发现潜在心理危机。相关研究如{refs}，分别从{aspects}等角度展开，为校园心理健康管理提供了技术支撑。"),
            new DirectionDef("学生画像与行为分析",
                    List.of("学生画像", "行为分析", "学习行为", "大学生", "群体特征", "画像模型"),
                    "该方向强调通过对学生多维度数据的整合分析，构建精准的学生画像并挖掘行为规律。研究涉及学习行为追踪、社交关系分析以及群体特征识别，力图实现对学生发展状况的全面把握。相关研究如{refs}，围绕{aspects}等主题展开，为精准化心理服务提供了数据基础。"),
            new DirectionDef("心理干预与服务机制",
                    List.of("心理干预", "心理咨询", "心理服务", "干预策略", "辅导机制", "服务体系"),
                    "该方向关注心理危机发生后的干预策略与服务机制优化。研究探讨了心理咨询效果评估、干预方案个性化设计以及线上线下协同服务模式的构建。相关研究如{refs}，结合{aspects}等视角，推动了心理健康服务从被动响应向主动干预的转型。"),
            new DirectionDef("健康管理与数字化支持",
                    List.of("健康管理", "数字化", "健康监测", "压力管理", "健康服务", "在线支持"),
                    "该方向探索数字化手段在心理健康管理与日常健康维护中的应用路径。研究涉及健康监测平台建设、压力管理工具开发以及心理健康知识普及模式创新。相关研究如{refs}，从{aspects}等维度切入，促进了心理健康管理的普惠化与便捷化。")
    );

    private static final List<DirectionDef> SEARCH_DIRECTIONS = List.of(
            new DirectionDef("语义检索与向量化表示",
                    List.of("语义检索", "向量检索", "智能检索", "检索", "语义匹配", "向量表示"),
                    "该方向聚焦于提升学术文献检索的语义理解与匹配精度。现有研究探索了基于深度学习与向量表示的语义检索方法，力图突破传统关键词匹配的局限。相关研究如{refs}，分别从{aspects}等角度展开，表明语义检索正成为学术资源发现的核心技术路径。"),
            new DirectionDef("知识服务与数字图书馆",
                    List.of("知识服务", "数字图书馆", "学术资源", "知识组织", "资源发现", "图书馆服务"),
                    "该方向关注面向科研人员的知识服务创新与数字图书馆建设。研究涉及知识图谱构建、学科知识组织以及个性化资源推荐，旨在提升学术服务的精准性与效率。相关研究如{refs}，围绕{aspects}等主题展开，推动了图书馆从资源存储向知识服务的转型。"),
            new DirectionDef("文献计量与科研数据管理",
                    List.of("文献计量", "科研数据", "数据管理", "开放获取", "评价体系", "计量分析"),
                    "该方向强调通过文献计量与科研数据管理方法揭示学科发展规律。研究关注引文分析、研究热点识别以及开放数据共享机制，为科研决策与学术评价提供量化依据。相关研究如{refs}，结合{aspects}等视角，丰富了学术研究的分析工具与方法论。"),
            new DirectionDef("检索评价与推荐机制",
                    List.of("推荐系统", "评价指标", "检索效果", "用户体验", "结果排序", "推荐算法"),
                    "该方向探索学术检索结果的评价方法与智能推荐机制的优化。研究涉及检索效果评估指标、用户行为反馈分析以及个性化推荐算法设计，力图提升学术资源发现的精准度与用户满意度。相关研究如{refs}，从{aspects}等维度切入，促进了检索服务的持续改进。")
    );

    private static final List<DirectionDef> AGRICULTURE_DIRECTIONS = List.of(
            new DirectionDef("智慧农业与精准种植",
                    List.of("智慧农业", "精准农业", "作物", "种植", "智能灌溉", "农业模型"),
                    "该方向聚焦于信息技术在农业生产中的深度应用，推动传统农业向智慧化、精准化转型。现有研究探索了作物生长模型、智能灌溉决策以及精准施肥技术，旨在提升农业生产效率与资源利用率。相关研究如{refs}，分别从{aspects}等角度展开，为智慧农业发展提供了技术方案。"),
            new DirectionDef("农业遥感与病虫害监测",
                    List.of("遥感", "病虫害", "农业遥感", "监测", "卫星遥感", "灾害预警"),
                    "该方向强调利用遥感技术实现农业资源监测与病虫害早期预警。研究涉及多源遥感数据融合、作物长势评估以及病虫害智能识别方法，力图提升农业灾害防控的时效性与准确性。相关研究如{refs}，围绕{aspects}等主题展开，为农业精细化管理提供了数据支撑。"),
            new DirectionDef("生态保护与食品安全",
                    List.of("生态", "食品安全", "生态保护", "土壤", "环境质量", "绿色农业"),
                    "该方向关注农业生态系统的可持续保护与食品安全保障机制。研究探讨了土壤质量监测、农业面源污染防控以及食品溯源体系建设，旨在实现农业发展与生态保护的协调统一。相关研究如{refs}，结合{aspects}等视角，为绿色农业发展提供了理论参考。"),
            new DirectionDef("农业大数据与决策支持",
                    List.of("大数据", "决策支持", "农业管理", "数据平台", "预测模型", "智能决策"),
                    "该方向探索农业大数据的整合分析与智能决策支持系统的构建。研究涉及农业数据平台建设、产量预测模型以及农产品市场分析，为农业管理与政策制定提供数据驱动支持。相关研究如{refs}，从{aspects}等维度切入，推动了农业决策从经验判断向数据驱动转变。")
    );

    private static final List<DirectionDef> LAW_DIRECTIONS = List.of(
            new DirectionDef("知识产权与数据合规",
                    List.of("知识产权", "数据合规", "隐私保护", "个人信息", "版权", "数据安全"),
                    "该方向聚焦于人工智能时代的知识产权保护与数据合规治理。现有研究关注算法创新成果的法律保护、用户数据使用边界以及合规审查机制构建。相关研究如{refs}，分别从{aspects}等角度展开，为数字时代的知识产权制度完善提供了理论支撑。"),
            new DirectionDef("算法治理与平台监管",
                    List.of("算法治理", "平台治理", "监管", "合规", "算法透明", "自动化决策"),
                    "该方向强调对人工智能算法及其应用平台的规范治理与有效监管。研究涉及算法透明度要求、平台责任界定以及自动化决策的审查机制，力图在技术发展与公共利益之间寻求平衡。相关研究如{refs}，围绕{aspects}等主题展开，为算法治理实践提供了制度参考。"),
            new DirectionDef("人工智能伦理与责任界定",
                    List.of("伦理", "责任", "人工智能伦理", "道德", "价值对齐", "伦理审查"),
                    "该方向关注人工智能应用中的伦理挑战与责任归属问题。研究探讨了技术偏见的识别与纠正、伦理审查框架构建以及人机协作中的价值对齐，旨在引导人工智能向负责任的方向发展。相关研究如{refs}，结合{aspects}等视角，丰富了人工智能伦理研究的理论内涵。"),
            new DirectionDef("数字治理与社会治理创新",
                    List.of("数字治理", "社会治理", "公共治理", "协同治理", "治理体系", "公共服务"),
                    "该方向探索数字化手段在社会治理与公共服务中的应用路径与制度创新。研究涉及数字政府建设、公共数据共享机制以及多元主体协同治理模式，力图提升治理效能与公共服务水平。相关研究如{refs}，从{aspects}等维度切入，为治理现代化提供了实践方案。")
    );

    private static final List<DirectionDef> ECONOMY_DIRECTIONS = List.of(
            new DirectionDef("数字经济与产业转型",
                    List.of("数字经济", "产业转型", "数字化", "创新", "产业升级", "新业态"),
                    "该方向聚焦于数字技术驱动下的经济结构转型与产业创新发展。现有研究关注数字化转型路径、新兴产业培育以及数字平台经济模式，旨在揭示数字经济时代的增长动力与变革逻辑。相关研究如{refs}，分别从{aspects}等角度展开，为产业数字化发展提供了理论指导。"),
            new DirectionDef("企业管理与供应链优化",
                    List.of("企业管理", "供应链", "运营管理", "绩效", "组织管理", "流程优化"),
                    "该方向强调现代企业管理模式的创新与供应链体系的优化升级。研究涉及智能化管理工具应用、供应链风险管理以及企业运营效率提升策略，力图增强企业的市场竞争力与韧性。相关研究如{refs}，围绕{aspects}等主题展开，为企业管理实践提供了决策参考。"),
            new DirectionDef("风险管理与决策支持",
                    List.of("风险", "决策支持", "资源配置", "评价", "风险评估", "决策模型"),
                    "该方向关注经济与管理活动中的风险识别、评估与防控机制。研究探讨了风险预测模型构建、智能决策支持系统以及资源配置优化方法，为组织在不确定环境下的稳健运营提供支撑。相关研究如{refs}，结合{aspects}等视角，丰富了风险管理的方法论体系。"),
            new DirectionDef("金融科技与市场分析",
                    List.of("金融", "市场", "金融科技", "投资", "资本市场", "普惠金融"),
                    "该方向探索金融科技在市场分析与金融服务中的应用创新与监管挑战。研究涉及智能投顾、信贷风险评估以及金融市场预测模型，力图提升金融服务的效率与普惠性。相关研究如{refs}，从{aspects}等维度切入，为金融科技健康发展提供了分析框架。")
    );

    private static final List<DirectionDef> HUMANITIES_DIRECTIONS = List.of(
            new DirectionDef("数字人文与文本分析",
                    List.of("数字人文", "文本分析", "文化传播", "史料", "语料库", "文本挖掘"),
                    "该方向聚焦于数字技术在人文研究中的方法创新与应用拓展。现有研究关注大规模文本的自动分析、历史史料的数字化处理以及文化传播路径的智能挖掘，力图为人文研究提供新的技术工具与视角。相关研究如{refs}，分别从{aspects}等角度展开，推动了人文研究的数字化转型。"),
            new DirectionDef("文学研究与网络文学",
                    List.of("文学", "网络文学", "文学研究", "阅读", "叙事", "文学传播"),
                    "该方向关注传统文学研究的深化与网络文学新形态的批判性分析。研究涉及文学作品的主题挖掘、网络文学的社会影响以及数字阅读行为分析，力图在媒介变革背景下重新审视文学的价值与功能。相关研究如{refs}，围绕{aspects}等主题展开，丰富了文学研究的当代维度。"),
            new DirectionDef("历史文化与遗产保护",
                    List.of("历史", "文化遗产", "史料", "传统", "遗产保护", "历史记忆"),
                    "该方向强调历史文化的传承保护与文化遗产的数字化管理。研究探讨了历史文献的整理与解读、文化遗产的数字化展示以及历史记忆的保存与传播策略。相关研究如{refs}，结合{aspects}等视角，为文化保护与传承提供了学术支撑。"),
            new DirectionDef("语言艺术与跨文化传播",
                    List.of("语言", "艺术", "跨文化", "传播", "语言学", "艺术传播"),
                    "该方向探索语言艺术的审美特征与跨文化传播的有效路径。研究涉及语言变迁分析、艺术作品的跨文化解读以及传播效果评估，力图促进不同文化之间的理解与对话。相关研究如{refs}，从{aspects}等维度切入，为文化交流研究提供了理论参考。")
    );

    private static final List<DirectionDef> ENGINEERING_DIRECTIONS = List.of(
            new DirectionDef("工业物联网与智能制造",
                    List.of("工业物联网", "智能制造", "物联网", "制造", "智能工厂", "生产优化"),
                    "该方向聚焦于工业物联网技术在智能制造中的深度融合与应用创新。现有研究关注设备互联、生产流程优化以及质量智能监控，旨在提升制造业的自动化水平与柔性生产能力。相关研究如{refs}，分别从{aspects}等角度展开，为智能制造提供了系统解决方案。"),
            new DirectionDef("软件系统与平台架构",
                    List.of("软件", "系统", "平台", "架构", "系统设计", "软件开发"),
                    "该方向关注复杂软件系统的架构设计与平台化开发方法。研究涉及微服务架构、系统可靠性保障以及平台可扩展性优化，力图支撑大规模应用的稳定高效运行。相关研究如{refs}，围绕{aspects}等主题展开，为软件工程实践提供了方法论指导。"),
            new DirectionDef("传感器与嵌入式系统",
                    List.of("传感器", "嵌入式", "电子", "控制", "硬件", "信号处理"),
                    "该方向强调传感器技术与嵌入式系统在信息采集与实时控制中的关键作用。研究探索了新型传感材料、低功耗嵌入式设计以及信号处理算法优化，为物联网与智能设备的底层支撑提供了技术基础。相关研究如{refs}，结合{aspects}等视角，推动了感知与计算的一体化发展。"),
            new DirectionDef("能源优化与可持续工程",
                    List.of("能源", "环境", "可持续", "优化", "节能", "绿色工程"),
                    "该方向探索能源系统的优化管理与环境可持续的工程解决方案。研究涉及能源消耗预测、清洁能源利用以及环境影响评估，力图在工程实践中实现经济效益与生态效益的协调。相关研究如{refs}，从{aspects}等维度切入，为可持续工程发展提供了决策依据。")
    );

    private static final List<DirectionDef> GENERAL_DIRECTIONS = List.of(
            new DirectionDef("理论基础与方法论研究",
                    List.of("理论", "方法", "模型", "框架", "分析"),
                    "该方向聚焦于相关领域的基础理论构建与方法论创新。现有研究致力于建立系统的理论框架，探索适用于该主题的研究方法与分析模型，为后续应用研究奠定基础。相关研究如{refs}，分别从{aspects}等角度展开，丰富了该领域的理论深度。"),
            new DirectionDef("系统平台与关键技术开发",
                    List.of("系统", "平台", "技术", "算法", "开发"),
                    "该方向关注支持该领域应用的技术平台设计与核心算法开发。研究涉及系统架构优化、关键算法改进以及平台性能提升，力图通过技术创新解决实际应用中的瓶颈问题。相关研究如{refs}，围绕{aspects}等主题展开，为技术落地提供了实现路径。"),
            new DirectionDef("应用场景与实践验证",
                    List.of("应用", "场景", "实践", "验证", "案例"),
                    "该方向强调理论与方法在具体场景中的实践应用与效果验证。研究通过案例研究、实验验证等方式，检验相关成果在实际环境中的适用性与有效性。相关研究如{refs}，结合{aspects}等视角，为推广转化提供了实证依据。"),
            new DirectionDef("效果评价与治理机制研究",
                    List.of("评价", "治理", "效果", "机制", "规范"),
                    "该方向探索该领域应用效果的科学评价与规范治理机制。研究关注评价指标体系构建、治理模式创新以及风险防控策略，力图推动该领域的健康可持续发展。相关研究如{refs}，从{aspects}等维度切入，为制度建设提供了参考框架。")
    );

    private static final Map<ReviewTopic, List<DirectionDef>> DIRECTION_POOL = Map.ofEntries(
            Map.entry(ReviewTopic.EDUCATION, EDU_DIRECTIONS),
            Map.entry(ReviewTopic.MEDICAL, MEDICAL_DIRECTIONS),
            Map.entry(ReviewTopic.PSYCHOLOGY, PSYCHOLOGY_DIRECTIONS),
            Map.entry(ReviewTopic.LITERATURE_SEARCH, SEARCH_DIRECTIONS),
            Map.entry(ReviewTopic.AGRICULTURE, AGRICULTURE_DIRECTIONS),
            Map.entry(ReviewTopic.LAW, LAW_DIRECTIONS),
            Map.entry(ReviewTopic.ECONOMY, ECONOMY_DIRECTIONS),
            Map.entry(ReviewTopic.HUMANITIES, HUMANITIES_DIRECTIONS),
            Map.entry(ReviewTopic.ENGINEERING, ENGINEERING_DIRECTIONS),
            Map.entry(ReviewTopic.GENERAL, GENERAL_DIRECTIONS)
    );

    // ==================== 问题池 ====================

    private static final Map<ReviewTopic, List<String>> PROBLEM_POOL = Map.ofEntries(
            Map.entry(ReviewTopic.EDUCATION, List.of(
                    "学习数据的质量与隐私保护问题仍然突出，数据采集过程中的伦理边界尚不清晰，影响了智能教育应用的推广。",
                    "教师对人工智能工具的接受度与教学融合能力参差不齐，部分应用场景存在技术与教学脱节的现象。",
                    "教学效果的长期追踪与科学评估机制不够完善，现有研究多为短期实验验证，缺乏持续性证据支撑。",
                    "教育公平与算法偏差问题值得关注，个性化推荐可能加剧资源分配不均，需要建立相应的纠偏机制。"
            )),
            Map.entry(ReviewTopic.MEDICAL, List.of(
                    "临床数据来源与隐私保护之间存在矛盾，高质量标注数据的获取成本高且受伦理约束，限制了模型训练。",
                    "模型的可解释性不足导致医生信任度有限，复杂深度学习模型的黑箱特性与临床决策的透明性要求相冲突。",
                    "多中心、大样本的系统验证仍然缺乏，现有研究多在单一医院或数据集上验证，结论的普适性有待检验。",
                    "医学伦理与责任边界问题尚未明确，人工智能辅助诊断出现失误时的责任归属缺乏清晰的法律界定。"
            )),
            Map.entry(ReviewTopic.PSYCHOLOGY, List.of(
                    "心理数据的敏感性与隐私保护要求高，学生心理健康信息的采集与使用面临严格的伦理审查。",
                    "心理风险识别模型的准确性与泛化能力有待提升，不同群体、不同文化背景下的适用性仍需验证。",
                    "心理干预的长期效果追踪不足，现有研究多关注即时干预效果，缺乏对持续影响的系统评估。",
                    "技术与专业服务的融合深度不够，智能工具难以完全替代专业心理咨询师的角色与能力。"
            )),
            Map.entry(ReviewTopic.LITERATURE_SEARCH, List.of(
                    "学术资源的语义理解与精准匹配仍面临挑战，跨学科、跨语言检索的效果有待进一步提升。",
                    "知识服务的个性化与覆盖面之间存在张力，深度专业化服务与广泛普及需求难以同时满足。",
                    "开放获取与知识产权保护的平衡问题突出，学术数据的开放共享受到版权与商业利益的双重制约。",
                    "检索效果评价体系尚不完善，现有指标多关注查全率与查准率，对用户真实需求的满足程度衡量不足。"
            )),
            Map.entry(ReviewTopic.AGRICULTURE, List.of(
                    "农业数据的采集成本与覆盖范围有限，偏远地区的数据获取困难，影响了模型的泛化能力。",
                    "智能农业技术的推广受农户接受度与经济成本制约，小规模农户难以承担高昂的设备投入。",
                    "生态环境因素的复杂性与不确定性增加了预测难度，气候变化等外部变量对模型稳定性构成挑战。",
                    "多学科协同研究不足，农业工程、生态学与数据科学之间的知识壁垒限制了综合解决方案的形成。"
            )),
            Map.entry(ReviewTopic.LAW, List.of(
                    "技术迭代速度与法律规制节奏之间存在落差，新兴技术应用往往先于法律规范的出台。",
                    "跨域执法与管辖权界定困难，数字空间的虚拟性与跨国性给传统法律适用带来了挑战。",
                    "算法透明性与商业保密之间的冲突突出，平台企业以商业秘密为由拒绝公开算法逻辑。",
                    "伦理规范的法律化路径尚不清晰，现有伦理准则多为指导性文件，缺乏强制性约束力。"
            )),
            Map.entry(ReviewTopic.ECONOMY, List.of(
                    "数据质量与来源可靠性影响分析结论，部分研究依赖非公开数据，结果的可复现性受到质疑。",
                    "模型假设与现实经济环境存在偏差，简化模型难以完全捕捉复杂市场机制的动态特征。",
                    "短期效果评估多而长期影响研究少，数字化转型对企业绩效的持续作用机制尚不清晰。",
                    "区域差异与行业异质性考虑不足，统一模型在不同情境下的适用性需要进一步验证。"
            )),
            Map.entry(ReviewTopic.HUMANITIES, List.of(
                    "大规模文本分析可能忽视语境与文化差异，自动化处理方法对深层意义的捕捉能力有限。",
                    "数字资源的版权与开放获取之间存在张力，人文资料的数字化传播受知识产权约束较多。",
                    "技术工具与人文思辨的结合深度不足，部分研究存在技术展示替代问题分析的倾向。",
                    "跨学科对话机制不够成熟，人文学者与技术人员之间的概念差异影响了协作效率。"
            )),
            Map.entry(ReviewTopic.ENGINEERING, List.of(
                    "复杂系统的可靠性与安全性验证面临挑战，实际工况的多样性增加了测试覆盖的难度。",
                    "硬件成本与能耗约束限制了部分技术的推广应用，尤其在资源受限场景下面临现实瓶颈。",
                    "标准体系与 interoperability 建设滞后，不同厂商系统之间的兼容性影响了产业生态发展。",
                    "工程伦理与社会影响评估重视不够，技术发展中的潜在风险未能得到充分的前瞻性考量。"
            )),
            Map.entry(ReviewTopic.GENERAL, List.of(
                    "数据来源相对单一，实验样本的覆盖范围和代表性有待进一步扩大，影响了研究结论的普适性。",
                    "现有模型或方法的可解释性不足，在实际推广应用中面临可信度与透明度的双重挑战。",
                    "跨领域、跨场景的系统性验证仍然缺乏，研究的深度与广度均有待拓展。",
                    "理论研究与实践应用之间存在脱节，部分成果难以有效转化为实际生产力。"
            ))
    );

    // ==================== 趋势池 ====================

    private static final Map<ReviewTopic, List<String>> TREND_POOL = Map.ofEntries(
            Map.entry(ReviewTopic.EDUCATION, List.of(
                    "教育大模型与智能助教将成为重要发展方向，通过生成式人工智能实现教学内容的动态生成与个性化辅导。",
                    "多模态学习分析技术的成熟将推动教学评价从单一维度向综合素养评估转变，实现对学生发展的全方位刻画。",
                    "教师与人工智能的协同教学模式将日益普及，技术将更多承担辅助角色而非替代教师。",
                    "教育数据治理与隐私保护技术将同步发展，在释放数据价值的同时建立可信的教育数据生态。"
            )),
            Map.entry(ReviewTopic.MEDICAL, List.of(
                    "多模态医学数据融合将成为主流，影像、文本、基因等多源数据的联合分析将提升诊断的全面性。",
                    "可解释智能诊断技术将受到更多关注，模型透明度的提升有助于增强医生的信任与采纳意愿。",
                    "隐私保护计算将在医学数据共享中发挥关键作用，联邦学习等技术有望破解数据孤岛困境。",
                    "医生与人工智能的协同决策模式将逐步成熟，人机协作而非人机替代将成为临床实践的新常态。"
            )),
            Map.entry(ReviewTopic.PSYCHOLOGY, List.of(
                    "心理风险识别的实时化与精准化将持续推进，可穿戴设备与移动终端的结合将实现心理健康状态的动态监测。",
                    "个性化心理干预方案的自动生成与推送将成为可能，基于用户画像的精准服务将提升干预效果。",
                    "心理健康服务的数字化普及将加速，在线咨询与自助干预工具将覆盖更广泛的人群。",
                    "心理健康数据治理与伦理规范建设将同步完善，在技术应用与隐私保护之间建立平衡机制。"
            )),
            Map.entry(ReviewTopic.LITERATURE_SEARCH, List.of(
                    "大规模语言模型将深度融入学术检索，基于语义理解与知识推理的检索方式将显著提升用户体验。",
                    "开放科学与数据共享机制将持续完善，学术资源的获取壁垒将进一步降低。",
                    "知识图谱与科研评价的结合将更加紧密，基于多维证据的学术影响力评估将逐步替代单一指标。",
                    "跨学科、跨语言的知识发现能力将显著增强，促进不同领域之间的交叉创新。"
            )),
            Map.entry(ReviewTopic.AGRICULTURE, List.of(
                    "空天地一体化监测网络将逐步建成，实现农业生产全过程的精细化感知与智能调控。",
                    "农业知识图谱与决策大模型将推动农业生产从经验驱动向知识驱动转变。",
                    "绿色低碳农业技术将受到更多重视，生态效益与经济效益的协同优化成为核心目标。",
                    "小农户与智能农业的衔接机制将不断创新，降低技术应用门槛以促进普惠发展。"
            )),
            Map.entry(ReviewTopic.LAW, List.of(
                    "适应性治理模式将逐步形成，法律规范将建立动态更新机制以跟上技术发展步伐。",
                    "算法审计与监管科技将成为新的治理工具，实现技术规制与技术赋能的有机结合。",
                    "全球数字治理合作将日益深化，跨国数据流动与管辖权协调规则将逐步完善。",
                    "人工智能伦理规范的法律化进程将加速，从软法约束向硬法保障转变。"
            )),
            Map.entry(ReviewTopic.ECONOMY, List.of(
                    "数字孪生技术将在企业管理和产业规划中发挥更大作用，实现虚实映射与动态优化。",
                    "因果推断方法的普及将提升经济分析的严谨性，从相关性分析向机制识别转变。",
                    "平台治理与反垄断规制将持续深化，促进数字经济的公平竞争与健康发展。",
                    "可持续发展目标将深度融入经济管理决策，ESG 评价成为企业战略的重要组成部分。"
            )),
            Map.entry(ReviewTopic.HUMANITIES, List.of(
                    "人工智能辅助的人文研究将从工具应用向方法创新转变，催生新的研究范式与问题意识。",
                    "数字人文基础设施将日趋完善，大规模人文数据库与开放工具平台将降低研究门槛。",
                    "跨媒介叙事与多模态文化分析将成为新的研究热点，适应媒介融合时代的文化形态。",
                    "文化遗产的数字化保护与活态传承将受到更多重视，技术手段与人文关怀的有机结合成为关键。"
            )),
            Map.entry(ReviewTopic.ENGINEERING, List.of(
                    "数字孪生与工业元宇宙将推动工程设计与运维模式的变革，实现全生命周期的智能化管理。",
                    "边缘计算与端侧智能将提升系统的实时响应能力，满足工业场景的低延迟要求。",
                    "绿色工程与可持续设计将成为工程实践的核心原则，全生命周期环境影响评估成为标配。",
                    "人机协作与自适应系统将日益成熟，机器从执行工具向协作伙伴的角色演进。"
            )),
            Map.entry(ReviewTopic.GENERAL, List.of(
                    "多源数据融合与智能分析将推动研究方法的持续升级，实现更全面、更深入的知识发现。",
                    "跨学科协同与理论创新将打破单一学科壁垒，催生新的理论增长点。",
                    "可解释性与公平性将受到更多重视，在追求性能的同时兼顾透明与伦理。",
                    "智能化服务与决策支持将渗透更多场景，构建面向多元需求的智能应用生态。"
            ))
    );

    // ==================== 停用词 ====================

    private static final Set<String> STOP_WORDS = Set.of(
            "研究", "分析", "探讨", "基于", "视角", "方法", "应用", "系统", "平台",
            "模型", "框架", "机制", "路径", "策略", "方案", "实践", "理论", "实证",
            "面向", "融合", "协同", "优化", "构建", "设计", "实现", "开展", "进行",
            "相关", "有关", "本文", "本文围绕", "结果表明", "结果显示", "结论",
            "建议", "提出", "发现", "显示", "表明", "指出", "认为", "通过", "采用",
            "利用", "结合", "整合", "针对", "关于", "随着", "日益", "不断", "逐步",
            "进一步", "相应", "因此", "从而", "为此", "此外", "同时",
            "首先", "其次", "再次", "最后", "一是", "二是", "三是", "第一", "第二", "第三"
    );

    // ==================== 业务方法 ====================

    @Override
    public ReviewRecordVO generate(ReviewGenerateRequest request) {
        Long userId = UserContext.getCurrentUserId();
        String topic = request.getTopic();
        List<Long> literatureIds = request.getLiteratureIds();
        String mode = request.getMode();

        if (!StringUtils.hasText(topic)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "综述主题不能为空");
        }
        if (literatureIds == null || literatureIds.size() < 2) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "请至少选择 2 篇参考文献");
        }
        if (literatureIds.size() > 5) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "最多选择 5 篇参考文献");
        }

        List<Literature> literatures = literatureMapper.selectBatchIds(literatureIds);
        if (literatures.size() != literatureIds.size()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "部分文献不存在，请重新选择");
        }

        String content;
        String generationMode;

        // 判断生成模式
        boolean useLlm = "llm".equalsIgnoreCase(mode);
        if (useLlm) {
            LlmConfig activeConfig = llmConfigMapper.selectActive();
            if (activeConfig == null || activeConfig.getApiKey() == null || activeConfig.getApiKey().isEmpty()) {
                log.warn("[REVIEW_GENERATE] 降级原因=NO_ACTIVE_CONFIG | mode=llm");
                content = buildReviewContent(topic, literatures);
                generationMode = "llm_fallback_rule";
            } else {
                try {
                    content = llmReviewService.generateReview(topic, literatures, activeConfig);
                    generationMode = "llm";
                } catch (Exception e) {
                    String msg = e.getMessage() != null ? e.getMessage() : "";
                    String reason = "UNKNOWN_ERROR";
                    if (msg.startsWith("EMPTY_API_KEY")) {
                        reason = "EMPTY_API_KEY";
                    } else if (msg.startsWith("TIMEOUT:")) {
                        reason = "TIMEOUT";
                    } else if (msg.startsWith("HTTP_ERROR:")) {
                        reason = "HTTP_ERROR";
                    } else if (msg.startsWith("JSON_PARSE_ERROR")) {
                        reason = "JSON_PARSE_ERROR";
                    } else if (msg.startsWith("EMPTY_RESPONSE")) {
                        reason = "EMPTY_RESPONSE";
                    } else if (msg.startsWith("FINISH_REASON_LENGTH")) {
                        reason = "FINISH_REASON_LENGTH";
                    } else if (msg.startsWith("MISSING_SECTION")) {
                        reason = "MISSING_SECTION";
                    } else if (msg.startsWith("TRUNCATED_ENDING")) {
                        reason = "TRUNCATED_ENDING";
                    } else if (msg.startsWith("INCOMPLETE_REFERENCES")) {
                        reason = "INCOMPLETE_REFERENCES";
                    } else if (msg.startsWith("INVALID_REFERENCE_INDEX")) {
                        reason = "INVALID_REFERENCE_INDEX";
                    }
                    log.warn("[REVIEW_GENERATE] 降级原因={} | mode=llm | error={}", reason, msg);
                    content = buildReviewContent(topic, literatures);
                    generationMode = "llm_fallback_rule";
                }
            }
        } else {
            content = buildReviewContent(topic, literatures);
            generationMode = "rule";
        }

        List<ReviewRecordVO.ReferenceVO> references = literatures.stream()
                .map(lit -> {
                    ReviewRecordVO.ReferenceVO ref = new ReviewRecordVO.ReferenceVO();
                    ref.setLiteratureId(lit.getId());
                    ref.setTitle(lit.getTitle());
                    return ref;
                }).collect(Collectors.toList());

        String refText = references.stream()
                .map(r -> r.getLiteratureId() + ":" + r.getTitle())
                .collect(Collectors.joining("|"));

        ReviewRecord record = new ReviewRecord();
        record.setUserId(userId);
        record.setTopic(topic);
        record.setLiteratureIds(literatureIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
        record.setContent(content);
        record.setReferenceText(refText);
        record.setGenerationMode(generationMode);
        reviewRecordMapper.insert(record);

        ReviewRecordVO vo = new ReviewRecordVO();
        vo.setId(record.getId());
        vo.setTopic(record.getTopic());
        vo.setContent(record.getContent());
        vo.setGenerationMode(generationMode);
        vo.setReferences(references);
        vo.setCreateTime(java.time.LocalDateTime.now());
        return vo;
    }

    @Override
    public List<ReviewRecordVO> getMyHistory() {
        Long userId = UserContext.getCurrentUserId();
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ReviewRecord> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(ReviewRecord::getUserId, userId)
                .orderByDesc(ReviewRecord::getCreateTime);
        List<ReviewRecord> list = reviewRecordMapper.selectList(wrapper);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public ReviewRecordVO getById(Long id) {
        Long userId = UserContext.getCurrentUserId();
        ReviewRecord record = reviewRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return convertToVO(record);
    }

    @Override
    public void delete(Long id) {
        Long userId = UserContext.getCurrentUserId();
        ReviewRecord record = reviewRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        reviewRecordMapper.deleteById(id);
    }

    // ==================== 综述内容构建核心 ====================

    private String buildReviewContent(String topic, List<Literature> literatures) {
        ReviewTopic rt = detectReviewTopic(topic);
        List<String> topKeywords = extractKeywords(literatures);
        List<DirectionAssignment> assignments = assignDirections(rt, literatures);

        StringBuilder sb = new StringBuilder();
        sb.append("《").append(formatTopic(topic)).append("》\n\n");

        sb.append("一、研究背景\n\n");
        sb.append(buildBackground(topic, rt, topKeywords)).append("\n\n");

        sb.append("二、研究现状\n\n");
        sb.append(buildResearchStatus(topic, assignments)).append("\n\n");

        sb.append("三、主要研究方向\n\n");
        sb.append(buildResearchDirections(assignments)).append("\n");

        sb.append("四、存在问题\n\n");
        sb.append(buildProblems(rt)).append("\n");

        sb.append("五、发展趋势\n\n");
        sb.append(buildTrends(rt)).append("\n");

        sb.append("六、参考文献来源\n\n");
        sb.append(buildReferences(literatures));

        return sb.toString();
    }

    // ==================== 主题检测 ====================

    private ReviewTopic detectReviewTopic(String topic) {
        String q = topic.toLowerCase();
        if (containsAny(q, MEDICAL_WORDS)) return ReviewTopic.MEDICAL;
        if (containsAny(q, PSYCHOLOGY_WORDS)) return ReviewTopic.PSYCHOLOGY;
        if (containsAny(q, EDU_WORDS)) return ReviewTopic.EDUCATION;
        if (containsAny(q, SEARCH_WORDS)) return ReviewTopic.LITERATURE_SEARCH;
        if (containsAny(q, AGRICULTURE_WORDS)) return ReviewTopic.AGRICULTURE;
        if (containsAny(q, LAW_WORDS)) return ReviewTopic.LAW;
        if (containsAny(q, ECONOMY_WORDS)) return ReviewTopic.ECONOMY;
        if (containsAny(q, HUMANITIES_WORDS)) return ReviewTopic.HUMANITIES;
        if (containsAny(q, ENGINEERING_WORDS)) return ReviewTopic.ENGINEERING;
        return ReviewTopic.GENERAL;
    }

    private boolean containsAny(String text, List<String> words) {
        for (String w : words) {
            if (text.contains(w.toLowerCase())) return true;
        }
        return false;
    }

    // ==================== 关键词提取 ====================

    private List<String> extractKeywords(List<Literature> literatures) {
        Map<String, Integer> freq = new HashMap<>();
        for (Literature lit : literatures) {
            String kws = lit.getKeywords();
            if (!StringUtils.hasText(kws)) continue;
            for (String kw : kws.split("[,，、;；]")) {
                String clean = kw.trim();
                if (clean.length() >= 2 && !STOP_WORDS.contains(clean)) {
                    freq.merge(clean, 1, Integer::sum);
                }
            }
        }
        return freq.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(8)
                .collect(Collectors.toList());
    }

    // ==================== 文献分配到方向 ====================

    private static class DirectionAssignment {
        final DirectionDef def;
        final List<Integer> refs; // 1-based
        final List<Literature> literatures;
        DirectionAssignment(DirectionDef def, List<Integer> refs, List<Literature> literatures) {
            this.def = def;
            this.refs = refs;
            this.literatures = literatures;
        }
    }

    private List<DirectionAssignment> assignDirections(ReviewTopic rt, List<Literature> literatures) {
        List<DirectionDef> pool = DIRECTION_POOL.getOrDefault(rt, GENERAL_DIRECTIONS);
        int[] litBestDir = new int[literatures.size()];
        for (int i = 0; i < literatures.size(); i++) {
            Literature lit = literatures.get(i);
            String text = ((lit.getTitle() != null ? lit.getTitle() : "") + " " + (lit.getKeywords() != null ? lit.getKeywords() : "")).toLowerCase();
            int bestDir = 0;
            int bestScore = -1;
            for (int d = 0; d < pool.size(); d++) {
                int score = 0;
                for (String kw : pool.get(d).keywords) {
                    if (text.contains(kw.toLowerCase())) score++;
                }
                if (score > bestScore) {
                    bestScore = score;
                    bestDir = d;
                }
            }
            litBestDir[i] = bestDir;
        }

        Map<Integer, List<Integer>> dirToRefs = new HashMap<>();
        for (int i = 0; i < litBestDir.length; i++) {
            dirToRefs.computeIfAbsent(litBestDir[i], k -> new ArrayList<>()).add(i + 1);
        }

        List<Map.Entry<Integer, List<Integer>>> sorted = dirToRefs.entrySet().stream()
                .sorted((a, b) -> b.getValue().size() - a.getValue().size())
                .collect(Collectors.toList());

        List<DirectionAssignment> result = new ArrayList<>();
        Set<Integer> usedDirs = new HashSet<>();
        for (Map.Entry<Integer, List<Integer>> e : sorted) {
            if (result.size() >= 3) break;
            int dirIdx = e.getKey();
            usedDirs.add(dirIdx);
            List<Integer> refs = e.getValue();
            List<Literature> dirLits = refs.stream().map(r -> literatures.get(r - 1)).collect(Collectors.toList());
            result.add(new DirectionAssignment(pool.get(dirIdx), refs, dirLits));
        }

        for (int i = 0; i < pool.size() && result.size() < 3; i++) {
            if (!usedDirs.contains(i)) {
                usedDirs.add(i);
                result.add(new DirectionAssignment(pool.get(i), List.of(), List.of()));
            }
        }
        return result;
    }

    // ==================== 研究背景 ====================

    private String buildBackground(String topic, ReviewTopic rt, List<String> topKeywords) {
        String kw = topKeywords.isEmpty() ? "相关技术" : String.join("、", topKeywords.subList(0, Math.min(3, topKeywords.size())));
        return switch (rt) {
            case MEDICAL -> topic + "是当前医学与人工智能交叉领域的重要议题。随着医学数据规模的快速增长和临床诊断需求的不断提升，" + kw + "等技术在辅助诊断、健康管理和风险预测中的价值日益凸显。该领域的研究对于提升医疗服务效率、降低诊断误差具有重要意义，已吸引了大量学者的关注。";
            case PSYCHOLOGY -> topic + "是心理健康与信息技术融合研究的前沿方向。在" + kw + "等需求的推动下，人工智能技术正被广泛应用于心理风险识别、学生画像构建和干预服务优化中。该领域对于促进校园心理健康管理、提升心理服务质量具有重要的实践价值。";
            case EDUCATION -> topic + "是教育信息化发展中的核心议题。在" + kw + "等需求的推动下，人工智能技术正深度融入教学场景，为个性化学习、智慧课堂和教学评价提供了新的技术路径。该领域的研究对推动教育公平与教学质量提升具有重要实践价值。";
            case LITERATURE_SEARCH -> topic + "是图书情报与信息科学领域的长期研究主题。随着学术资源规模的持续扩大，" + kw + "等技术为提升文献检索效率与知识服务质量提供了新的可能。该领域对于促进学术交流、支撑科研创新具有重要的基础设施意义。";
            case AGRICULTURE -> topic + "是农业现代化与信息技术交叉的重要领域。在" + kw + "等方向的推动下，人工智能技术正逐步改变传统农业生产方式。该领域对于提升农业资源利用效率、保障粮食安全具有重要的战略价值。";
            case LAW -> topic + "是法学与人工智能交叉领域的新兴议题。随着数字技术的深入应用，" + kw + "等问题日益突出，亟需从法律与治理角度进行系统性研究。该领域对于规范技术发展、维护公共利益具有重要的制度意义。";
            case ECONOMY -> topic + "是数字经济与管理科学交叉研究的重要方向。在" + kw + "等趋势的推动下，人工智能技术正深刻改变企业运营与产业组织模式。该领域对于理解技术驱动的经济转型、优化资源配置具有重要的理论价值。";
            case HUMANITIES -> topic + "是数字技术与人文学科融合的新兴研究领域。在" + kw + "等方法的支撑下，传统人文研究正经历方法论层面的深刻变革。该领域对于拓展人文研究的视野、促进文化传承具有重要的学术意义。";
            case ENGINEERING -> topic + "是工程技术与智能系统融合发展的关键方向。在" + kw + "等技术的推动下，工业系统正朝着智能化、网络化方向加速演进。该领域对于提升工程效率、实现智能制造具有重要的产业价值。";
            default -> topic + "是当前学术研究的重要议题，涉及理论探索与实践应用的双重维度。在" + kw + "等方向的推动下，该领域吸引了越来越多的学者关注，相关研究成果不断涌现，亟需通过系统性综述把握整体脉络。";
        };
    }

    // ==================== 研究现状 ====================

    private String buildResearchStatus(String topic, List<DirectionAssignment> assignments) {
        if (assignments.isEmpty()) {
            return "现有研究围绕" + topic + "展开了多方面的探索，相关成果为该领域的进一步发展奠定了基础。";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("从所选文献看，").append(topic).append("领域的现有研究主要围绕");
        List<String> titles = assignments.stream().map(a -> a.def.title).collect(Collectors.toList());
        sb.append(String.join("、", titles));
        sb.append("等方向展开。");

        for (DirectionAssignment da : assignments) {
            if (da.refs.isEmpty()) continue;
            String refs = formatRefs(da.refs);
            List<String> aspects = extractCommonAspects(da.literatures);
            sb.append("在").append(da.def.title).append("方面，").append(refs).append("进行了相关探索");
            if (!aspects.isEmpty()) {
                sb.append("，涉及").append(String.join("、", aspects));
            }
            sb.append("。");
        }

        sb.append("整体而言，该领域已从早期理论探索逐步转向实践应用与系统优化，多学科交叉融合的趋势日益明显。");
        return sb.toString();
    }

    // ==================== 主要研究方向 ====================

    private String buildResearchDirections(List<DirectionAssignment> assignments) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < assignments.size(); i++) {
            DirectionAssignment da = assignments.get(i);
            sb.append(i + 1).append(". ").append(da.def.title).append("\n\n");
            String desc;
            if (!da.refs.isEmpty()) {
                desc = da.def.template;
                desc = desc.replace("{refs}", formatRefs(da.refs));
                List<String> aspects = extractCommonAspects(da.literatures);
                if (!aspects.isEmpty()) {
                    desc = desc.replace("{aspects}", String.join("、", aspects));
                } else {
                    desc = desc.replace("{aspects}", "相关技术");
                }
            } else {
                desc = "该方向聚焦于" + da.def.title + "，强调利用相关技术手段解决该领域的实际问题。随着技术持续进步与方法不断完善，该方向正逐渐成为该领域的重要组成部分，值得后续研究者给予更多关注。";
            }
            sb.append("   ").append(desc).append("\n\n");
        }
        return sb.toString();
    }

    // ==================== 存在问题 ====================

    private String buildProblems(ReviewTopic rt) {
        List<String> pool = PROBLEM_POOL.getOrDefault(rt, PROBLEM_POOL.get(ReviewTopic.GENERAL));
        StringBuilder sb = new StringBuilder();
        sb.append("尽管相关研究取得了一定进展，但在发展过程中仍暴露出若干亟待解决的问题：\n\n");
        int count = Math.min(pool.size(), 4);
        for (int i = 0; i < count; i++) {
            sb.append(i + 1).append(". ").append(pool.get(i)).append("\n");
        }
        return sb.toString();
    }

    // ==================== 发展趋势 ====================

    private String buildTrends(ReviewTopic rt) {
        List<String> pool = TREND_POOL.getOrDefault(rt, TREND_POOL.get(ReviewTopic.GENERAL));
        StringBuilder sb = new StringBuilder();
        sb.append("展望未来，该领域将朝着以下方向持续演进：\n\n");
        int count = Math.min(pool.size(), 4);
        for (int i = 0; i < count; i++) {
            sb.append(i + 1).append(". ").append(pool.get(i)).append("\n");
        }
        return sb.toString();
    }

    // ==================== 参考文献 ====================

    private String buildReferences(List<Literature> literatures) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < literatures.size(); i++) {
            Literature lit = literatures.get(i);
            sb.append("[").append(i + 1).append("] ").append(lit.getAuthors())
                    .append(". ").append(lit.getTitle())
                    .append(". 《").append(lit.getJournal()).append("》, ")
                    .append(lit.getPublishYear()).append(".\n");
        }
        return sb.toString();
    }

    // ==================== 辅助工具方法 ====================

    private String formatTopic(String topic) {
        if (topic == null || topic.trim().isEmpty()) {
            return "研究综述";
        }
        String t = topic.trim();
        if (t.contains("综述")) {
            return t;
        }
        return t + "研究综述";
    }

    private String formatRefs(List<Integer> refs) {
        if (refs == null || refs.isEmpty()) return "";
        String nums = refs.stream().map(String::valueOf).collect(Collectors.joining("]["));
        return "文献[" + nums + "]";
    }

    private List<String> extractAspectKeywords(Literature lit) {
        String text = (lit.getTitle() != null ? lit.getTitle() : "") + " " + (lit.getKeywords() != null ? lit.getKeywords() : "");
        Set<String> result = new LinkedHashSet<>();
        for (String part : text.split("[,，、;；\\s]")) {
            String clean = part.trim();
            if (clean.length() >= 2 && clean.length() <= 10 && !STOP_WORDS.contains(clean)) {
                result.add(clean);
            }
        }
        return new ArrayList<>(result).subList(0, Math.min(5, result.size()));
    }

    private List<String> extractCommonAspects(List<Literature> literatures) {
        Map<String, Integer> freq = new HashMap<>();
        for (Literature lit : literatures) {
            for (String kw : extractAspectKeywords(lit)) {
                freq.merge(kw, 1, Integer::sum);
            }
        }
        return freq.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(3)
                .collect(Collectors.toList());
    }

    private ReviewRecordVO convertToVO(ReviewRecord record) {
        ReviewRecordVO vo = new ReviewRecordVO();
        vo.setId(record.getId());
        vo.setTopic(record.getTopic());
        vo.setContent(record.getContent());
        vo.setGenerationMode(record.getGenerationMode());
        vo.setCreateTime(record.getCreateTime());

        if (record.getReferenceText() != null && !record.getReferenceText().isEmpty()) {
            List<ReviewRecordVO.ReferenceVO> refs = Arrays.stream(record.getReferenceText().split("\\|"))
                    .map(s -> {
                        String[] parts = s.split(":", 2);
                        ReviewRecordVO.ReferenceVO ref = new ReviewRecordVO.ReferenceVO();
                        ref.setLiteratureId(Long.valueOf(parts[0]));
                        ref.setTitle(parts.length > 1 ? parts[1] : "");
                        return ref;
                    }).collect(Collectors.toList());
            vo.setReferences(refs);
        } else {
            vo.setReferences(new ArrayList<>());
        }
        return vo;
    }
}
