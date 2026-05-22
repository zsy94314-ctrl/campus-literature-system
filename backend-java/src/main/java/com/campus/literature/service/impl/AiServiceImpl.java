package com.campus.literature.service.impl;

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

    // ==================== 主题触发词表 ====================
    private static final List<String> EDU_KEYWORDS = List.of(
            "教学", "课堂", "教育", "学习", "学生", "高校", "课程", "智慧课堂",
            "个性化学习", "教学评价", "学习行为", "智能助教", "教育大模型",
            "混合式教学", "学习分析"
    );
    private static final List<String> AI_KEYWORDS = List.of(
            "人工智能", "ai", "大模型", "机器学习", "深度学习", "智能问答",
            "推荐系统", "知识图谱", "自然语言处理", "语义检索"
    );
    private static final List<String> MENTAL_KEYWORDS = List.of(
            "心理健康", "情绪识别", "风险预警", "学生画像", "心理干预",
            "压力管理", "健康管理", "行为分析"
    );
    private static final List<String> SEARCH_KEYWORDS = List.of(
            "文献检索", "语义检索", "学术检索", "知识服务", "数字图书馆",
            "文献计量", "开放获取", "faiss", "向量检索", "知识图谱"
    );

    // ==================== 主题扩展词表（query expansion）====================
    private static final List<String> EDU_EXPAND_WORDS = List.of(
            "教学", "课堂", "教育", "学习", "学生", "高校", "课程", "智慧课堂",
            "个性化学习", "教学评价", "学习行为", "智能助教", "教育大模型",
            "混合式教学", "学习分析"
    );
    private static final List<String> MENTAL_EXPAND_WORDS = List.of(
            "心理健康", "风险预警", "学生画像", "心理干预", "压力管理",
            "健康管理", "行为分析", "情绪识别", "大学生群体", "心理咨询"
    );
    private static final List<String> SEARCH_EXPAND_WORDS = List.of(
            "学术文献", "文献检索", "智能检索", "语义检索", "向量检索",
            "知识服务", "数字图书馆", "文献计量", "开放获取", "科研数据管理",
            "知识图谱", "faiss", "rag", "学术资源"
    );

    // ==================== 分类权重映射 ====================
    private static final Map<String, Double> EDU_CATEGORY_SCORES = Map.ofEntries(
            Map.entry("教育学", 1.0),
            Map.entry("计算机科学与人工智能", 0.8),
            Map.entry("数据科学与数据挖掘", 0.7),
            Map.entry("心理学", 0.6),
            Map.entry("心理健康", 0.6),
            Map.entry("图书情报与档案管理", 0.5),
            Map.entry("软件工程", 0.4),
            Map.entry("管理学", 0.3)
    );
    private static final Map<String, Double> MENTAL_CATEGORY_SCORES = Map.ofEntries(
            Map.entry("心理健康", 1.0),
            Map.entry("心理学", 0.9),
            Map.entry("公共健康", 0.7),
            Map.entry("健康管理", 0.7),
            Map.entry("医学信息学", 0.6),
            Map.entry("数据科学与数据挖掘", 0.6),
            Map.entry("计算机科学与人工智能", 0.5),
            Map.entry("教育学", 0.4)
    );
    private static final Map<String, Double> SEARCH_CATEGORY_SCORES = Map.ofEntries(
            Map.entry("图书情报与档案管理", 1.0),
            Map.entry("计算机科学与人工智能", 0.8),
            Map.entry("数据科学与数据挖掘", 0.7),
            Map.entry("信息与通信工程", 0.4),
            Map.entry("教育学", 0.3),
            Map.entry("管理学", 0.2)
    );

    // ==================== 弱相关分类集合 ====================
    private static final Set<String> SEARCH_WEAK_CATEGORIES = Set.of(
            "文学", "历史学", "语言学", "艺术学", "文化研究",
            "农学", "食品科学", "智能制造与自动化", "能源与环境工程", "电子工程"
    );
    private static final Set<String> EDU_WEAK_CATEGORIES = Set.of(
            "智能制造与自动化", "能源与环境工程", "电子工程", "农学", "食品科学"
    );

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

    @Override
    public Map<String, Object> rebuildIndex() {
        // 1. 查询所有文献
        List<Literature> literatures = literatureMapper.selectList(null);
        if (literatures == null || literatures.isEmpty()) {
            throw new BusinessException(500, "暂无文献数据，无法重建索引");
        }

        // 2. 查询所有分类，构建 id->name 映射
        List<Category> categories = categoryMapper.selectList(null);
        Map<Long, String> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));

        // 3. 构建 DTO 列表
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

        // 4. 调用 backend-ai
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

    @Override
    public List<AiSearchResultVO> semanticSearch(String query, Integer topK) {
        if (topK == null || topK <= 0) {
            topK = 10;
        }

        // 1. 扩大召回
        int recallTopK = Math.max(topK * 3, 60);
        String url = aiBaseUrl + "/semantic-search";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", query);
        requestBody.put("topK", recallTopK);

        List<Map<String, Object>> results = callAiSearchApi(url, requestBody);
        if (results == null || results.isEmpty()) {
            return List.of();
        }

        // 2. 识别 query 主题
        Set<String> themes = detectThemes(query);

        // 3. 回查 MySQL 并计算 finalScore、keywordScore、categoryScore、weakPenalty
        List<RankedItem> ranked = scoreAndRank(results, query, themes);

        // 4. 按 finalScore 降序，取 topK
        return ranked.stream()
                .sorted(Comparator.comparingDouble(RankedItem::getFinalScore).reversed())
                .limit(topK)
                .map(RankedItem::getVo)
                .collect(Collectors.toList());
    }

    @Override
    public List<AiSearchResultVO> recommend(Long literatureId) {
        String url = aiBaseUrl + "/recommend";
        AiRecommendRequest request = new AiRecommendRequest();
        request.setLiteratureId(literatureId);
        request.setTopK(5);

        List<Map<String, Object>> results = callAiSearchApi(url, request);
        // 排除自身（backend-ai 已经排除，但再保险一次）
        results = results.stream()
                .filter(r -> !literatureId.equals(convertToLong(r.get("literatureId"))))
                .collect(Collectors.toList());
        return mapToAiSearchResultVO(results);
    }

    // ==================== 召回与重排核心逻辑 ====================

    private List<RankedItem> scoreAndRank(List<Map<String, Object>> results, String query, Set<String> themes) {
        List<Long> ids = results.stream()
                .map(r -> convertToLong(r.get("literatureId")))
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (ids.isEmpty()) {
            return List.of();
        }

        // 批量查询文献
        List<Literature> literatures = literatureMapper.selectBatchIds(ids);
        Map<Long, Literature> literatureMap = literatures.stream()
                .collect(Collectors.toMap(Literature::getId, lit -> lit));

        // 查询分类名称
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

        // 语义相似度映射
        Map<Long, Double> semanticMap = new HashMap<>();
        for (Map<String, Object> r : results) {
            Long id = convertToLong(r.get("literatureId"));
            Double sim = convertToDouble(r.get("similarity"));
            if (id != null && sim != null) {
                semanticMap.put(id, sim);
            }
        }

        // 获取主题扩展词（query expansion）
        Set<String> expansionWords = getExpansionWords(themes);

        // 提取 query tokens（含扩展词）
        Set<String> queryTokens = extractQueryTokens(query, expansionWords);

        List<RankedItem> ranked = new ArrayList<>();
        for (Long id : ids) {
            Literature lit = literatureMap.get(id);
            if (lit == null) continue;

            String categoryName = categoryNameMap.getOrDefault(lit.getCategoryId(), "");
            Double semanticSimilarity = semanticMap.getOrDefault(id, 0.0);

            String title = (lit.getTitle() != null ? lit.getTitle() : "").toLowerCase();
            String keywords = (lit.getKeywords() != null ? lit.getKeywords() : "").toLowerCase();
            String abstractText = (lit.getAbstractText() != null ? lit.getAbstractText() : "").toLowerCase();
            String content = (lit.getContent() != null ? lit.getContent() : "").toLowerCase();
            String combinedText = title + " " + keywords + " " + abstractText + " " + content;

            // keywordScore + matchReason
            KeywordScoreResult ks = computeKeywordScore(queryTokens, lit, categoryName);

            // categoryScore
            double categoryScore = computeCategoryScore(categoryName, themes, query);

            // finalScore 基础值
            double finalScore = semanticSimilarity * 0.60 + ks.score * 0.30 + categoryScore * 0.10;

            // weakPenalty：若属于弱相关分类且未命中主题扩展词，则降权
            boolean hitsExpansion = expansionWords.stream().anyMatch(combinedText::contains);
            double weakPenalty = computeWeakPenalty(categoryName, themes, hitsExpansion);
            finalScore = Math.max(0.0, finalScore - weakPenalty);

            // matchReason 构建
            StringBuilder reason = new StringBuilder();
            reason.append("语义相似度：").append(String.format("%.1f%%", semanticSimilarity * 100));
            if (StringUtils.hasText(ks.reason)) {
                reason.append("；").append(ks.reason);
            }
            if (!expansionWords.isEmpty() && hitsExpansion) {
                Set<String> hitWords = expansionWords.stream()
                        .filter(combinedText::contains)
                        .limit(3)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                if (!hitWords.isEmpty()) {
                    reason.append("；命中扩展词：").append(String.join("、", hitWords));
                }
            }
            if (categoryScore > 0 && StringUtils.hasText(categoryName)) {
                reason.append("；分类相关：").append(categoryName);
            }
            if (weakPenalty > 0) {
                reason.append("；弱相关降权：-").append(String.format("%.0f%%", weakPenalty * 100));
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

    private Set<String> detectThemes(String query) {
        Set<String> themes = new HashSet<>();
        String q = query.toLowerCase();

        for (String kw : EDU_KEYWORDS) {
            if (q.contains(kw.toLowerCase())) { themes.add("edu"); break; }
        }
        for (String kw : AI_KEYWORDS) {
            if (q.contains(kw.toLowerCase())) { themes.add("ai"); break; }
        }
        for (String kw : MENTAL_KEYWORDS) {
            if (q.contains(kw.toLowerCase())) { themes.add("mental"); break; }
        }
        for (String kw : SEARCH_KEYWORDS) {
            if (q.contains(kw.toLowerCase())) { themes.add("search"); break; }
        }

        // 文献检索主题组合规则增强
        if (q.contains("文献") && q.contains("检索")) themes.add("search");
        if (q.contains("学术") && q.contains("检索")) themes.add("search");
        if (q.contains("文献") && q.contains("智能")) themes.add("search");
        if (q.contains("学术文献")) themes.add("search");
        if (q.contains("智能检索")) themes.add("search");
        if (q.contains("语义检索")) themes.add("search");
        if (q.contains("向量检索")) themes.add("search");

        return themes;
    }

    // ==================== Query Expansion ====================

    private Set<String> getExpansionWords(Set<String> themes) {
        Set<String> words = new HashSet<>();
        if (themes.contains("edu")) {
            words.addAll(EDU_EXPAND_WORDS);
        }
        if (themes.contains("mental")) {
            words.addAll(MENTAL_EXPAND_WORDS);
        }
        if (themes.contains("search")) {
            words.addAll(SEARCH_EXPAND_WORDS);
        }
        return words;
    }

    private Set<String> extractQueryTokens(String query, Set<String> expansionWords) {
        Set<String> tokens = new HashSet<>();
        if (!StringUtils.hasText(query)) return tokens;

        String q = query.toLowerCase().trim();
        tokens.add(q);

        // 主题触发词
        for (String kw : EDU_KEYWORDS) if (q.contains(kw.toLowerCase())) tokens.add(kw.toLowerCase());
        for (String kw : AI_KEYWORDS) if (q.contains(kw.toLowerCase())) tokens.add(kw.toLowerCase());
        for (String kw : MENTAL_KEYWORDS) if (q.contains(kw.toLowerCase())) tokens.add(kw.toLowerCase());
        for (String kw : SEARCH_KEYWORDS) if (q.contains(kw.toLowerCase())) tokens.add(kw.toLowerCase());

        // 扩展词
        tokens.addAll(expansionWords);

        // 按空格拆分英文/混合词
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

    private double computeCategoryScore(String categoryName, Set<String> themes, String query) {
        if (!StringUtils.hasText(categoryName)) return 0.0;

        double score = 0.0;
        if (themes.contains("edu")) {
            score = Math.max(score, EDU_CATEGORY_SCORES.getOrDefault(categoryName, 0.0));
        }
        if (themes.contains("mental")) {
            score = Math.max(score, MENTAL_CATEGORY_SCORES.getOrDefault(categoryName, 0.0));
        }
        if (themes.contains("search")) {
            score = Math.max(score, SEARCH_CATEGORY_SCORES.getOrDefault(categoryName, 0.0));
        }

        // 若不属于任何主题，按 query 是否包含 categoryName 给分
        if (score == 0.0 && themes.isEmpty()) {
            String q = query.toLowerCase();
            if (q.contains(categoryName.toLowerCase())) {
                score = 0.3;
            }
        }
        return score;
    }

    // ==================== weakPenalty 弱相关降权 ====================

    private double computeWeakPenalty(String categoryName, Set<String> themes, boolean hitsExpansion) {
        if (!StringUtils.hasText(categoryName) || hitsExpansion) return 0.0;

        if (themes.contains("search") && SEARCH_WEAK_CATEGORIES.contains(categoryName)) {
            return 0.15;
        }
        if (themes.contains("edu") && EDU_WEAK_CATEGORIES.contains(categoryName)) {
            return 0.15;
        }
        return 0.0;
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
