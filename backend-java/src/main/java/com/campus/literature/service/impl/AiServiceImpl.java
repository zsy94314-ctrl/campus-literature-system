package com.campus.literature.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.literature.dto.AiDocumentDTO;
import com.campus.literature.dto.AiRecommendRequest;
import com.campus.literature.entity.Category;
import com.campus.literature.entity.Literature;
import com.campus.literature.exception.BusinessException;
import com.campus.literature.mapper.CategoryMapper;
import com.campus.literature.mapper.LiteratureMapper;
import com.campus.literature.service.AiService;
import com.campus.literature.vo.AiSearchResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AI 服务实现（接入 backend-ai）
 * 支持多学科主题画像重排
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final LiteratureMapper literatureMapper;
    private final CategoryMapper categoryMapper;

    @Value("${ai.service.base-url}")
    private String aiBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // ==================== 多学科主题画像定义 ====================

    /**
     * 主题画像
     */
    private static class TopicProfile {
        final String code;
        final String label;
        final List<String> detectWords;
        final List<String> expansionWords;
        final Map<String, Double> categoryWeights;
        final boolean isSpecific;

        TopicProfile(String code, String label,
                     List<String> detectWords, List<String> expansionWords,
                     Map<String, Double> categoryWeights, boolean isSpecific) {
            this.code = code;
            this.label = label;
            this.detectWords = detectWords;
            this.expansionWords = expansionWords;
            this.categoryWeights = categoryWeights;
            this.isSpecific = isSpecific;
        }
    }

    // 1. 教育主题
    private static final TopicProfile TP_EDUCATION = new TopicProfile(
            "EDUCATION", "教育主题",
            List.of("教学", "课堂", "教育", "学习", "学生", "高校", "课程", "智慧课堂",
                    "个性化学习", "教学评价", "学习行为", "智能助教", "教育大模型",
                    "混合式教学", "学习分析", "教育技术", "教育公平", "课程教学"),
            List.of("教学", "课堂", "教育", "学习", "学生", "高校", "课程", "智慧课堂",
                    "个性化学习", "教学评价", "学习行为", "智能助教", "教育大模型",
                    "混合式教学", "学习分析", "教育技术", "教育公平", "课程教学"),
            Map.ofEntries(
                    Map.entry("教育学", 1.0),
                    Map.entry("计算机科学与人工智能", 0.8),
                    Map.entry("数据科学与数据挖掘", 0.7),
                    Map.entry("心理学", 0.6),
                    Map.entry("心理健康", 0.6),
                    Map.entry("图书情报与档案管理", 0.4),
                    Map.entry("软件工程", 0.4),
                    Map.entry("管理学", 0.3)
            ),
            true
    );

    // 2. 医学健康主题
    private static final TopicProfile TP_MEDICAL_HEALTH = new TopicProfile(
            "MEDICAL_HEALTH", "医学健康主题",
            List.of("医学", "医疗", "健康", "临床", "诊断", "疾病", "患者", "医院",
                    "医学影像", "辅助诊断", "电子病历", "远程医疗", "公共健康", "健康管理",
                    "医学信息学", "基础医学", "临床医学", "药物研发", "精准医疗",
                    "智能医疗", "ai医疗", "人工智能医疗", "疾病预测", "风险预测",
                    "患者管理", "临床决策", "健康监测", "智能诊断"),
            List.of("医学", "医疗", "健康", "临床", "诊断", "疾病", "患者", "医院",
                    "医学影像", "辅助诊断", "电子病历", "远程医疗", "公共健康", "健康管理",
                    "医学信息学", "基础医学", "临床医学", "药物研发", "精准医疗",
                    "智能医疗", "疾病预测", "风险预测", "患者管理", "临床决策", "健康监测", "智能诊断"),
            Map.ofEntries(
                    Map.entry("临床医学", 1.0),
                    Map.entry("基础医学", 0.9),
                    Map.entry("医学信息学", 0.9),
                    Map.entry("公共健康", 0.8),
                    Map.entry("健康管理", 0.8),
                    Map.entry("心理健康", 0.6),
                    Map.entry("生物技术", 0.5),
                    Map.entry("计算机科学与人工智能", 0.5),
                    Map.entry("数据科学与数据挖掘", 0.5)
            ),
            true
    );

    // 3. 心理健康主题
    private static final TopicProfile TP_PSYCHOLOGY_HEALTH = new TopicProfile(
            "PSYCHOLOGY_HEALTH", "心理健康主题",
            List.of("心理健康", "情绪识别", "风险预警", "学生画像", "心理干预",
                    "压力管理", "健康管理", "行为分析", "情绪调节", "心理咨询",
                    "大学生群体", "心理服务", "心理危机", "心理测评"),
            List.of("心理健康", "情绪识别", "风险预警", "学生画像", "心理干预",
                    "压力管理", "健康管理", "行为分析", "情绪调节", "心理咨询",
                    "大学生群体", "心理服务", "心理危机", "心理测评"),
            Map.ofEntries(
                    Map.entry("心理健康", 1.0),
                    Map.entry("心理学", 0.9),
                    Map.entry("公共健康", 0.7),
                    Map.entry("健康管理", 0.7),
                    Map.entry("医学信息学", 0.6),
                    Map.entry("数据科学与数据挖掘", 0.6),
                    Map.entry("计算机科学与人工智能", 0.5),
                    Map.entry("教育学", 0.4)
            ),
            true
    );

    // 4. 文献检索主题
    private static final TopicProfile TP_LITERATURE_SEARCH = new TopicProfile(
            "LITERATURE_SEARCH", "文献检索主题",
            List.of("学术文献", "文献检索", "智能检索", "语义检索", "向量检索",
                    "知识服务", "数字图书馆", "文献计量", "开放获取", "科研数据管理",
                    "知识图谱", "faiss", "rag", "学术资源", "信息检索", "学术搜索", "推荐系统"),
            List.of("学术文献", "文献检索", "智能检索", "语义检索", "向量检索",
                    "知识服务", "数字图书馆", "文献计量", "开放获取", "科研数据管理",
                    "知识图谱", "faiss", "rag", "学术资源", "信息检索", "学术搜索", "推荐系统"),
            Map.ofEntries(
                    Map.entry("图书情报与档案管理", 1.0),
                    Map.entry("计算机科学与人工智能", 0.8),
                    Map.entry("数据科学与数据挖掘", 0.7),
                    Map.entry("信息与通信工程", 0.4),
                    Map.entry("教育学", 0.3),
                    Map.entry("管理学", 0.2)
            ),
            true
    );

    // 5. 农业生态主题
    private static final TopicProfile TP_AGRICULTURE_ECOLOGY = new TopicProfile(
            "AGRICULTURE_ECOLOGY", "农业生态主题",
            List.of("农业", "农学", "作物", "种植", "土壤", "粮食", "生态", "生态系统",
                    "生物多样性", "遥感监测", "精准农业", "智慧农业", "食品安全",
                    "食品科学", "农业遥感", "病虫害", "农业管理", "生态保护"),
            List.of("农业", "农学", "作物", "种植", "土壤", "粮食", "生态", "生态系统",
                    "生物多样性", "遥感监测", "精准农业", "智慧农业", "食品安全",
                    "食品科学", "农业遥感", "病虫害", "农业管理", "生态保护"),
            Map.ofEntries(
                    Map.entry("农学", 1.0),
                    Map.entry("生态学", 0.9),
                    Map.entry("食品科学", 0.8),
                    Map.entry("生物技术", 0.7),
                    Map.entry("环境科学", 0.6),
                    Map.entry("地球科学", 0.5),
                    Map.entry("数据科学与数据挖掘", 0.4),
                    Map.entry("计算机科学与人工智能", 0.4)
            ),
            true
    );

    // 6. 法学治理主题
    private static final TopicProfile TP_LAW_GOVERNANCE = new TopicProfile(
            "LAW_GOVERNANCE", "法学治理主题",
            List.of("法律", "法学", "知识产权", "隐私保护", "数据治理", "算法治理",
                    "平台治理", "合规", "伦理", "责任", "监管", "公共治理",
                    "社会治理", "数字治理", "人工智能伦理", "个人信息保护"),
            List.of("法律", "法学", "知识产权", "隐私保护", "数据治理", "算法治理",
                    "平台治理", "合规", "伦理", "责任", "监管", "公共治理",
                    "社会治理", "数字治理", "人工智能伦理", "个人信息保护"),
            Map.ofEntries(
                    Map.entry("法学", 1.0),
                    Map.entry("社会学", 0.7),
                    Map.entry("管理学", 0.6),
                    Map.entry("新闻传播学", 0.5),
                    Map.entry("计算机科学与人工智能", 0.4),
                    Map.entry("公共健康", 0.2)
            ),
            true
    );

    // 7. 经济管理主题
    private static final TopicProfile TP_ECONOMY_MANAGEMENT = new TopicProfile(
            "ECONOMY_MANAGEMENT", "经济管理主题",
            List.of("经济", "经济学", "管理", "管理学", "数字经济", "产业发展",
                    "企业管理", "供应链", "市场", "金融", "风险管理", "绩效评价",
                    "决策支持", "运营管理", "资源配置", "创新管理"),
            List.of("经济", "经济学", "管理", "管理学", "数字经济", "产业发展",
                    "企业管理", "供应链", "市场", "金融", "风险管理", "绩效评价",
                    "决策支持", "运营管理", "资源配置", "创新管理"),
            Map.ofEntries(
                    Map.entry("经济学", 1.0),
                    Map.entry("管理学", 1.0),
                    Map.entry("数据科学与数据挖掘", 0.6),
                    Map.entry("计算机科学与人工智能", 0.5),
                    Map.entry("社会学", 0.4),
                    Map.entry("法学", 0.3)
            ),
            true
    );

    // 8. 人文艺术主题
    private static final TopicProfile TP_HUMANITIES_ARTS = new TopicProfile(
            "HUMANITIES_ARTS", "人文艺术主题",
            List.of("文学", "历史", "哲学", "语言", "艺术", "文化", "文本分析",
                    "文化传播", "数字人文", "史料整理", "文学研究", "语言学",
                    "艺术学", "文化研究", "网络文学"),
            List.of("文学", "历史", "哲学", "语言", "艺术", "文化", "文本分析",
                    "文化传播", "数字人文", "史料整理", "文学研究", "语言学",
                    "艺术学", "文化研究", "网络文学"),
            Map.ofEntries(
                    Map.entry("文学", 1.0),
                    Map.entry("历史学", 1.0),
                    Map.entry("哲学", 1.0),
                    Map.entry("语言学", 1.0),
                    Map.entry("艺术学", 1.0),
                    Map.entry("文化研究", 1.0),
                    Map.entry("新闻传播学", 0.5),
                    Map.entry("计算机科学与人工智能", 0.3)
            ),
            true
    );

    // 9. 工程技术主题
    private static final TopicProfile TP_ENGINEERING_TECH = new TopicProfile(
            "ENGINEERING_TECH", "工程技术主题",
            List.of("工程", "技术", "软件", "系统", "平台", "通信", "电子", "制造",
                    "自动化", "工业物联网", "智能制造", "能源", "传感器",
                    "控制系统", "信息通信", "软件工程", "系统设计", "算法模型"),
            List.of("工程", "技术", "软件", "系统", "平台", "通信", "电子", "制造",
                    "自动化", "工业物联网", "智能制造", "能源", "传感器",
                    "控制系统", "信息通信", "软件工程", "系统设计", "算法模型"),
            Map.ofEntries(
                    Map.entry("智能制造与自动化", 1.0),
                    Map.entry("软件工程", 0.9),
                    Map.entry("信息与通信工程", 0.9),
                    Map.entry("电子工程", 0.9),
                    Map.entry("能源与环境工程", 0.8),
                    Map.entry("计算机科学与人工智能", 0.8),
                    Map.entry("数据科学与数据挖掘", 0.7)
            ),
            true
    );

    // 10. 自然科学主题
    private static final TopicProfile TP_NATURAL_SCIENCE = new TopicProfile(
            "NATURAL_SCIENCE", "自然科学主题",
            List.of("数学", "物理", "化学", "地球科学", "生物科学", "环境科学",
                    "模型", "实验", "理论", "材料", "气候", "生态环境", "分子",
                    "统计建模", "自然科学"),
            List.of("数学", "物理", "化学", "地球科学", "生物科学", "环境科学",
                    "模型", "实验", "理论", "材料", "气候", "生态环境", "分子",
                    "统计建模", "自然科学"),
            Map.ofEntries(
                    Map.entry("数学", 1.0),
                    Map.entry("物理学", 1.0),
                    Map.entry("化学", 1.0),
                    Map.entry("地球科学", 1.0),
                    Map.entry("生物科学", 1.0),
                    Map.entry("环境科学", 1.0),
                    Map.entry("数据科学与数据挖掘", 0.4)
            ),
            true
    );

    // 11. 人工智能通用主题
    private static final TopicProfile TP_AI_GENERAL = new TopicProfile(
            "AI_GENERAL", "人工智能通用主题",
            List.of("人工智能", "ai", "大模型", "机器学习", "深度学习", "智能问答",
                    "推荐系统", "知识图谱", "自然语言处理", "语义检索",
                    "算法模型", "智能系统", "数据挖掘", "神经网络"),
            List.of("人工智能", "ai", "大模型", "机器学习", "深度学习", "智能问答",
                    "推荐系统", "知识图谱", "自然语言处理", "语义检索",
                    "算法模型", "智能系统", "数据挖掘", "神经网络"),
            Map.ofEntries(
                    Map.entry("计算机科学与人工智能", 1.0),
                    Map.entry("数据科学与数据挖掘", 0.8),
                    Map.entry("软件工程", 0.6),
                    Map.entry("信息与通信工程", 0.5)
            ),
            false
    );

    private static final List<TopicProfile> ALL_TOPICS = List.of(
            TP_EDUCATION, TP_MEDICAL_HEALTH, TP_PSYCHOLOGY_HEALTH,
            TP_LITERATURE_SEARCH, TP_AGRICULTURE_ECOLOGY, TP_LAW_GOVERNANCE,
            TP_ECONOMY_MANAGEMENT, TP_HUMANITIES_ARTS, TP_ENGINEERING_TECH,
            TP_NATURAL_SCIENCE, TP_AI_GENERAL
    );

    // 明显冲突分类（人文艺术与其他学科冲突最显著）
    private static final Set<String> CONFLICT_CATEGORIES = Set.of(
            "文学", "历史学", "哲学", "语言学", "艺术学", "文化研究"
    );

    // ==================== 健康检查 ====================

    @Override
    public Map<String, Object> health() {
        String url = aiBaseUrl + "/health";
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            throw new BusinessException(500, "智能检索服务未启动，请先启动 backend-ai");
        } catch (ResourceAccessException e) {
            log.warn("backend-ai 连接失败: {}", e.getMessage());
            throw new BusinessException(500, "智能检索服务未启动，请先启动 backend-ai");
        } catch (RestClientException e) {
            log.warn("backend-ai 请求异常: {}", e.getMessage());
            throw new BusinessException(500, "智能检索服务未启动，请先启动 backend-ai");
        }
    }

    // ==================== 重建索引 ====================

    @Override
    public Map<String, Object> rebuildIndex() {
        List<Literature> literatures = literatureMapper.selectList(null);
        if (literatures == null || literatures.isEmpty()) {
            throw new BusinessException(500, "暂无文献数据，无法重建索引");
        }

        List<Category> categories = categoryMapper.selectList(null);
        Map<Long, String> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));

        List<AiDocumentDTO> documents = literatures.stream().map(lit -> {
            AiDocumentDTO dto = new AiDocumentDTO();
            dto.setId(lit.getId());
            dto.setTitle(lit.getTitle());
            dto.setCategoryName(categoryMap.getOrDefault(lit.getCategoryId(), ""));
            dto.setDocumentType(lit.getDocumentType());
            dto.setKeywords(lit.getKeywords());
            dto.setAbstractText(lit.getAbstractText());
            dto.setContent(lit.getContent());
            return dto;
        }).collect(Collectors.toList());

        String url = aiBaseUrl + "/rebuild-index";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("documents", documents);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestBody, Map.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new BusinessException(500, "智能检索服务未启动，请先启动 backend-ai");
            }
            Map<String, Object> body = response.getBody();
            if (!isSuccessCode(body.get("code"))) {
                Object message = body.get("message");
                throw new BusinessException(500, message != null ? message.toString() : "索引重建失败");
            }
            Object data = body.get("data");
            return data instanceof Map ? (Map<String, Object>) data : Collections.singletonMap("count", documents.size());
        } catch (ResourceAccessException e) {
            log.warn("backend-ai 连接失败: {}", e.getMessage());
            throw new BusinessException(500, "智能检索服务未启动，请先启动 backend-ai");
        } catch (RestClientException e) {
            log.warn("backend-ai 请求异常: {}", e.getMessage());
            throw new BusinessException(500, "智能检索服务未启动，请先启动 backend-ai");
        }
    }

    // ==================== 语义检索（多学科主题画像重排）====================

    @Override
    public List<AiSearchResultVO> semanticSearch(String query, Integer topK) {
        if (topK == null || topK <= 0) {
            topK = 10;
        }

        // 1. 调整召回数量
        int recallTopK = Math.min(Math.max(topK * 5, 100), 150);
        String url = aiBaseUrl + "/semantic-search";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", query);
        requestBody.put("topK", recallTopK);

        List<Map<String, Object>> faissResults = callAiSearchApi(url, requestBody);
        if (faissResults == null) {
            faissResults = List.of();
        }

        // 2. 主题识别
        Set<TopicProfile> detectedThemes = detectThemes(query);

        // 3. MySQL 关键词补充召回（仅针对具体主题）
        List<Literature> extraLiteratures = mysqlRecallByThemes(detectedThemes);

        // 4. 统一重排
        List<RankedItem> ranked = scoreAndRank(faissResults, extraLiteratures, query, detectedThemes);

        // 5. 按 finalScore 降序，取 topK
        return ranked.stream()
                .sorted(Comparator.comparingDouble(RankedItem::getFinalScore).reversed())
                .limit(topK)
                .map(RankedItem::getVo)
                .collect(Collectors.toList());
    }

    // ==================== 相似推荐（不受主题画像影响）====================

    @Override
    public List<AiSearchResultVO> recommend(Long literatureId) {
        String url = aiBaseUrl + "/recommend";
        AiRecommendRequest request = new AiRecommendRequest();
        request.setLiteratureId(literatureId);
        request.setTopK(5);

        List<Map<String, Object>> results = callAiSearchApi(url, request);
        results = results.stream()
                .filter(r -> !literatureId.equals(convertToLong(r.get("literatureId"))))
                .collect(Collectors.toList());
        return mapToAiSearchResultVO(results);
    }

    // ==================== MySQL 关键词补充召回 ====================

    private List<Literature> mysqlRecallByThemes(Set<TopicProfile> themes) {
        List<Literature> allExtra = new ArrayList<>();
        Set<Long> seenIds = new HashSet<>();

        for (TopicProfile theme : themes) {
            if (!theme.isSpecific) continue; // AI_GENERAL 不单独补充召回

            List<String> words = theme.expansionWords;
            if (words == null || words.isEmpty()) continue;

            try {
                List<Literature> extras = literatureMapper.searchByExpansionWords(words, 50);
                for (Literature lit : extras) {
                    if (lit.getId() != null && seenIds.add(lit.getId())) {
                        allExtra.add(lit);
                    }
                }
            } catch (Exception e) {
                log.warn("MySQL 补充召回失败 theme={}: {}", theme.code, e.getMessage());
            }
        }
        return allExtra;
    }

    // ==================== 召回与重排核心逻辑 ====================

    private List<RankedItem> scoreAndRank(List<Map<String, Object>> faissResults,
                                          List<Literature> extraLiteratures,
                                          String query,
                                          Set<TopicProfile> themes) {
        // FAISS 结果映射: id -> similarity
        Map<Long, Double> faissSimMap = new HashMap<>();
        Set<Long> faissIds = new HashSet<>();
        for (Map<String, Object> r : faissResults) {
            Long id = convertToLong(r.get("literatureId"));
            Double sim = convertToDouble(r.get("similarity"));
            if (id != null && sim != null) {
                faissSimMap.put(id, sim);
                faissIds.add(id);
            }
        }

        // 批量查询 FAISS 对应的文献
        Map<Long, Literature> literatureMap = new HashMap<>();
        if (!faissIds.isEmpty()) {
            List<Literature> faissLits = literatureMapper.selectBatchIds(new ArrayList<>(faissIds));
            for (Literature lit : faissLits) {
                literatureMap.put(lit.getId(), lit);
            }
        }

        // 将 MySQL 补充召回加入候选池
        boolean hasExtra = false;
        for (Literature lit : extraLiteratures) {
            if (lit.getId() != null && !literatureMap.containsKey(lit.getId())) {
                literatureMap.put(lit.getId(), lit);
                hasExtra = true;
            }
        }

        if (literatureMap.isEmpty()) {
            return List.of();
        }

        // 查询分类名称
        List<Long> categoryIds = literatureMap.values().stream()
                .map(Literature::getCategoryId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> categoryNameMap = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            List<Category> categories = categoryMapper.selectBatchIds(categoryIds);
            categoryNameMap = categories.stream()
                    .collect(Collectors.toMap(Category::getId, Category::getName));
        }

        // 获取主题扩展词
        Set<String> expansionWords = getExpansionWords(themes);

        // 提取 query tokens
        Set<String> queryTokens = extractQueryTokens(query, expansionWords);

        // 收集所有具体主题的相关分类
        Set<String> allRelevantCategories = themes.stream()
                .filter(t -> t.isSpecific)
                .flatMap(t -> t.categoryWeights.keySet().stream())
                .collect(Collectors.toSet());

        List<RankedItem> ranked = new ArrayList<>();
        for (Map.Entry<Long, Literature> entry : literatureMap.entrySet()) {
            Long id = entry.getKey();
            Literature lit = entry.getValue();
            if (lit == null) continue;

            String categoryName = categoryNameMap.getOrDefault(lit.getCategoryId(), "");

            // semanticSimilarity：FAISS 有则用，否则默认 0.45（关键词补充召回）
            Double semanticSimilarity = faissSimMap.get(id);
            boolean isExtraRecall = semanticSimilarity == null;
            if (isExtraRecall) {
                semanticSimilarity = 0.45;
            }

            String title = (lit.getTitle() != null ? lit.getTitle() : "").toLowerCase();
            String keywords = (lit.getKeywords() != null ? lit.getKeywords() : "").toLowerCase();
            String abstractText = (lit.getAbstractText() != null ? lit.getAbstractText() : "").toLowerCase();
            String content = (lit.getContent() != null ? lit.getContent() : "").toLowerCase();
            String combinedText = title + " " + keywords + " " + abstractText + " " + content;

            // keywordScore + matchReason
            KeywordScoreResult ks = computeKeywordScore(queryTokens, lit, categoryName);

            // categoryScore（具体主题优先于 AI_GENERAL）
            double categoryScore = computeCategoryScore(categoryName, themes);

            // finalScore 基础值
            double finalScore = semanticSimilarity * 0.60 + ks.score * 0.30 + categoryScore * 0.10;

            // weakPenalty
            boolean hitsExpansion = expansionWords.stream().anyMatch(combinedText::contains);
            double weakPenalty = computeWeakPenalty(categoryName, allRelevantCategories, hitsExpansion, themes);
            finalScore = Math.max(0.0, finalScore - weakPenalty);

            // matchReason 构建
            StringBuilder reason = new StringBuilder();
            if (isExtraRecall) {
                reason.append("关键词补充召回");
            } else {
                reason.append("语义相似度：").append(String.format("%.1f%%", semanticSimilarity * 100));
            }

            // 命中主题
            List<String> themeLabels = themes.stream()
                    .map(t -> t.label)
                    .collect(Collectors.toList());
            if (!themeLabels.isEmpty()) {
                reason.append("；主题：").append(String.join("、", themeLabels));
            }

            // keywordScore 原因
            if (StringUtils.hasText(ks.reason)) {
                reason.append("；").append(ks.reason);
            }

            // 命中扩展词
            if (!expansionWords.isEmpty()) {
                Set<String> hitWords = expansionWords.stream()
                        .filter(combinedText::contains)
                        .limit(3)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                if (!hitWords.isEmpty()) {
                    reason.append("；命中扩展词：").append(String.join("、", hitWords));
                }
            }

            // 分类相关
            if (StringUtils.hasText(categoryName)) {
                reason.append("；分类相关：").append(categoryName);
            }

            // 弱相关降权
            if (weakPenalty > 0) {
                reason.append("；弱相关降权：-").append(String.format("%.0f%%", weakPenalty * 100));
            } else if (!themes.isEmpty()) {
                reason.append("；无降权");
            }

            // 构建 VO
            AiSearchResultVO vo = new AiSearchResultVO();
            vo.setId(lit.getId());
            vo.setTitle(lit.getTitle());
            vo.setAuthors(lit.getAuthors());
            vo.setAbstractText(lit.getAbstractText());
            vo.setKeywords(lit.getKeywords());
            vo.setJournal(lit.getJournal());
            vo.setPublishYear(lit.getPublishYear());
            vo.setCategoryId(lit.getCategoryId());
            vo.setCategoryName(categoryName);
            vo.setCitationCount(lit.getCitationCount());
            vo.setDocumentType(lit.getDocumentType());
            vo.setSimilarity(semanticSimilarity);
            vo.setFinalScore(finalScore);
            vo.setMatchReason(reason.toString());

            ranked.add(new RankedItem(vo, finalScore));
        }
        return ranked;
    }

    // ==================== 主题识别 ====================

    private Set<TopicProfile> detectThemes(String query) {
        Set<TopicProfile> themes = new LinkedHashSet<>();
        String q = query.toLowerCase();

        for (TopicProfile tp : ALL_TOPICS) {
            if (tp == TP_AI_GENERAL) continue; // AI_GENERAL 最后处理
            for (String kw : tp.detectWords) {
                if (q.contains(kw.toLowerCase())) {
                    themes.add(tp);
                    break;
                }
            }
        }

        // 文献检索主题组合规则增强
        if (q.contains("文献") && q.contains("检索")) themes.add(TP_LITERATURE_SEARCH);
        if (q.contains("学术") && q.contains("检索")) themes.add(TP_LITERATURE_SEARCH);
        if (q.contains("文献") && q.contains("智能")) themes.add(TP_LITERATURE_SEARCH);
        if (q.contains("学术文献")) themes.add(TP_LITERATURE_SEARCH);
        if (q.contains("智能检索")) themes.add(TP_LITERATURE_SEARCH);
        if (q.contains("语义检索")) themes.add(TP_LITERATURE_SEARCH);
        if (q.contains("向量检索")) themes.add(TP_LITERATURE_SEARCH);

        // 医学主题：同时包含 AI 词 + 医学词，也判定为医学
        boolean hasAiWord = TP_AI_GENERAL.detectWords.stream().anyMatch(w -> q.contains(w.toLowerCase()));
        boolean hasMedicalWord = TP_MEDICAL_HEALTH.detectWords.stream().anyMatch(w -> q.contains(w.toLowerCase()));
        if (hasMedicalWord) {
            themes.add(TP_MEDICAL_HEALTH);
        }

        // 如果没有任何具体主题命中，但包含 AI 词，则加入 AI_GENERAL
        if (themes.isEmpty() && hasAiWord) {
            themes.add(TP_AI_GENERAL);
        }

        // 若已有具体主题且同时包含 AI 词，也加入 AI_GENERAL 作为补充
        if (!themes.isEmpty() && hasAiWord) {
            themes.add(TP_AI_GENERAL);
        }

        return themes;
    }

    // ==================== Query Expansion ====================

    private Set<String> getExpansionWords(Set<TopicProfile> themes) {
        Set<String> words = new LinkedHashSet<>();
        // 优先加入具体主题扩展词
        for (TopicProfile tp : themes) {
            if (tp.isSpecific && tp.expansionWords != null) {
                words.addAll(tp.expansionWords);
            }
        }
        // 再加入 AI_GENERAL 扩展词
        for (TopicProfile tp : themes) {
            if (!tp.isSpecific && tp.expansionWords != null) {
                words.addAll(tp.expansionWords);
            }
        }
        return words;
    }

    private Set<String> extractQueryTokens(String query, Set<String> expansionWords) {
        Set<String> tokens = new LinkedHashSet<>();
        if (!StringUtils.hasText(query)) return tokens;

        String q = query.toLowerCase().trim();
        tokens.add(q);

        // 所有主题触发词
        for (TopicProfile tp : ALL_TOPICS) {
            for (String kw : tp.detectWords) {
                if (q.contains(kw.toLowerCase())) {
                    tokens.add(kw.toLowerCase());
                }
            }
        }

        // 扩展词
        tokens.addAll(expansionWords);

        // 按空格拆分
        for (String part : q.split("\\s+")) {
            if (part.length() >= 2) tokens.add(part);
        }
        return tokens;
    }

    // ==================== keywordScore 计算 ====================

    private KeywordScoreResult computeKeywordScore(Set<String> tokens, Literature lit, String categoryName) {
        if (tokens.isEmpty()) return new KeywordScoreResult(0.0, "");

        String title = (lit.getTitle() != null ? lit.getTitle() : "").toLowerCase();
        String keywords = (lit.getKeywords() != null ? lit.getKeywords() : "").toLowerCase();
        String abstractText = (lit.getAbstractText() != null ? lit.getAbstractText() : "").toLowerCase();
        String content = (lit.getContent() != null ? lit.getContent() : "").toLowerCase();
        String catName = (categoryName != null ? categoryName : "").toLowerCase();

        double score = 0.0;
        List<String> reasons = new ArrayList<>();

        if (tokens.stream().anyMatch(title::contains)) {
            score += 0.35;
            reasons.add("标题命中");
        }
        if (tokens.stream().anyMatch(keywords::contains)) {
            score += 0.30;
            reasons.add("关键词命中");
        }
        if (tokens.stream().anyMatch(abstractText::contains)) {
            score += 0.20;
            reasons.add("摘要命中");
        }
        if (tokens.stream().anyMatch(content::contains)) {
            score += 0.10;
            reasons.add("正文命中");
        }
        if (tokens.stream().anyMatch(catName::contains)) {
            score += 0.05;
            reasons.add("分类命中");
        }

        score = Math.min(score, 1.0);
        String reason = String.join("；", reasons);
        return new KeywordScoreResult(score, reason);
    }

    // ==================== categoryScore 计算 ====================

    private double computeCategoryScore(String categoryName, Set<TopicProfile> themes) {
        if (!StringUtils.hasText(categoryName) || themes.isEmpty()) return 0.0;

        double specificMaxScore = 0.0;
        double aiGeneralScore = 0.0;

        for (TopicProfile tp : themes) {
            Double score = tp.categoryWeights.get(categoryName);
            if (score == null) score = 0.0;
            if (tp.isSpecific) {
                specificMaxScore = Math.max(specificMaxScore, score);
            } else {
                aiGeneralScore = Math.max(aiGeneralScore, score);
            }
        }

        // 具体学科主题优先；AI_GENERAL 最多作为补充，权重减半
        if (specificMaxScore > 0) {
            return Math.max(specificMaxScore, aiGeneralScore * 0.5);
        }
        // 只有 AI_GENERAL
        return aiGeneralScore;
    }

    // ==================== weakPenalty 弱相关降权 ====================

    private double computeWeakPenalty(String categoryName,
                                      Set<String> allRelevantCategories,
                                      boolean hitsExpansion,
                                      Set<TopicProfile> themes) {
        if (!StringUtils.hasText(categoryName) || hitsExpansion) return 0.0;

        // 如果没有命中任何具体主题，不惩罚（AI_GENERAL 太宽泛）
        boolean hasSpecificTheme = themes.stream().anyMatch(t -> t.isSpecific);
        if (!hasSpecificTheme) return 0.0;

        // 如果分类在任何具体主题的相关分类中，不惩罚
        if (allRelevantCategories.contains(categoryName)) return 0.0;

        // 若属于明显冲突分类，加重惩罚
        if (CONFLICT_CATEGORIES.contains(categoryName)) {
            return 0.20;
        }
        return 0.15;
    }

    // ==================== 内部数据结构 ====================

    private static class KeywordScoreResult {
        final double score;
        final String reason;
        KeywordScoreResult(double score, String reason) {
            this.score = score;
            this.reason = reason;
        }
    }

    private static class RankedItem {
        private final AiSearchResultVO vo;
        private final double finalScore;
        RankedItem(AiSearchResultVO vo, double finalScore) {
            this.vo = vo;
            this.finalScore = finalScore;
        }
        AiSearchResultVO getVo() { return vo; }
        double getFinalScore() { return finalScore; }
    }

    // ==================== 通用工具方法 ====================

    private List<Map<String, Object>> callAiSearchApi(String url, Object requestBody) {
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestBody, Map.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new BusinessException(500, "智能检索服务未启动，请先启动 backend-ai");
            }
            Map<String, Object> body = response.getBody();
            if (!isSuccessCode(body.get("code"))) {
                Object message = body.get("message");
                throw new BusinessException(500, message != null ? message.toString() : "智能检索服务调用失败");
            }
            Object data = body.get("data");
            if (data instanceof Map) {
                Object results = ((Map<?, ?>) data).get("results");
                if (results instanceof List) {
                    return (List<Map<String, Object>>) results;
                }
            }
            return List.of();
        } catch (ResourceAccessException e) {
            log.warn("backend-ai 连接失败: {}", e.getMessage());
            throw new BusinessException(500, "智能检索服务未启动，请先启动 backend-ai");
        } catch (RestClientException e) {
            log.warn("backend-ai 请求异常: {}", e.getMessage());
            throw new BusinessException(500, "智能检索服务未启动，请先启动 backend-ai");
        }
    }

    private boolean isSuccessCode(Object code) {
        if (code == null) return false;
        if (code instanceof Number) return ((Number) code).intValue() == 200;
        try {
            return Integer.parseInt(code.toString()) == 200;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private List<AiSearchResultVO> mapToAiSearchResultVO(List<Map<String, Object>> results) {
        if (results == null || results.isEmpty()) {
            return List.of();
        }

        List<Long> ids = results.stream()
                .map(r -> convertToLong(r.get("literatureId")))
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (ids.isEmpty()) {
            return List.of();
        }

        List<Literature> literatures = literatureMapper.selectBatchIds(ids);
        Map<Long, Literature> literatureMap = literatures.stream()
                .collect(Collectors.toMap(Literature::getId, lit -> lit));

        List<Long> categoryIds = literatures.stream()
                .map(Literature::getCategoryId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> categoryNameMap = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            List<Category> categories = categoryMapper.selectBatchIds(categoryIds);
            categoryNameMap = categories.stream()
                    .collect(Collectors.toMap(Category::getId, Category::getName));
        }

        List<AiSearchResultVO> voList = new ArrayList<>();
        for (Map<String, Object> r : results) {
            Long id = convertToLong(r.get("literatureId"));
            Double similarity = convertToDouble(r.get("similarity"));
            Literature lit = literatureMap.get(id);
            if (lit == null) continue;
            AiSearchResultVO vo = new AiSearchResultVO();
            vo.setId(lit.getId());
            vo.setTitle(lit.getTitle());
            vo.setAuthors(lit.getAuthors());
            vo.setAbstractText(lit.getAbstractText());
            vo.setKeywords(lit.getKeywords());
            vo.setJournal(lit.getJournal());
            vo.setPublishYear(lit.getPublishYear());
            vo.setCategoryId(lit.getCategoryId());
            vo.setCategoryName(categoryNameMap.getOrDefault(lit.getCategoryId(), ""));
            vo.setCitationCount(lit.getCitationCount());
            vo.setDocumentType(lit.getDocumentType());
            vo.setSimilarity(similarity);
            voList.add(vo);
        }
        return voList;
    }

    private Long convertToLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double convertToDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
