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

        String url = aiBaseUrl + "/semantic-search";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", query);
        requestBody.put("topK", topK);

        List<Map<String, Object>> results = callAiSearchApi(url, requestBody);
        return mapToAiSearchResultVO(results);
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
        if (code == null) {
            return false;
        }
        if (code instanceof Number) {
            return ((Number) code).intValue() == 200;
        }
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

        List<AiSearchResultVO> voList = new ArrayList<>();
        for (Map<String, Object> r : results) {
            Long id = convertToLong(r.get("literatureId"));
            Double similarity = convertToDouble(r.get("similarity"));
            Literature lit = literatureMap.get(id);
            if (lit == null) {
                continue;
            }
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
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double convertToDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
