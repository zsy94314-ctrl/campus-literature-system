package com.campus.literature.service.impl;

import com.campus.literature.dto.LlmConfigCreateRequest;
import com.campus.literature.dto.LlmConfigUpdateRequest;
import com.campus.literature.entity.LlmConfig;
import com.campus.literature.exception.BusinessException;
import com.campus.literature.mapper.LlmConfigMapper;
import com.campus.literature.service.LlmConfigService;
import com.campus.literature.vo.LlmConfigVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * LLM 配置服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmConfigServiceImpl implements LlmConfigService {

    private final LlmConfigMapper llmConfigMapper;

    @Override
    public List<LlmConfigVO> listAll() {
        List<LlmConfig> list = llmConfigMapper.selectList(null);
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public LlmConfigVO getActive() {
        LlmConfig config = llmConfigMapper.selectActive();
        if (config == null) return null;
        return toVO(config);
    }

    @Override
    public LlmConfigVO create(LlmConfigCreateRequest request) {
        LlmConfig config = new LlmConfig();
        config.setName(request.getName());
        config.setProvider(request.getProvider());
        config.setBaseUrl(request.getBaseUrl());
        config.setModel(request.getModel());
        config.setApiKey(request.getApiKey());
        config.setEnabled(request.getEnabled());
        config.setActive(request.getActive());
        config.setTimeoutSeconds(request.getTimeoutSeconds());
        config.setRemark(request.getRemark());

        if (Boolean.TRUE.equals(request.getActive() != null && request.getActive() == 1)) {
            llmConfigMapper.deactivateAll();
        }

        llmConfigMapper.insert(config);
        return toVO(config);
    }

    @Override
    public LlmConfigVO update(Long id, LlmConfigUpdateRequest request) {
        LlmConfig existing = llmConfigMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "配置不存在");
        }

        existing.setName(request.getName());
        existing.setProvider(request.getProvider());
        existing.setBaseUrl(request.getBaseUrl());
        existing.setModel(request.getModel());
        if (request.getApiKey() != null && !request.getApiKey().isEmpty()) {
            existing.setApiKey(request.getApiKey());
        }
        existing.setEnabled(request.getEnabled());
        existing.setActive(request.getActive());
        existing.setTimeoutSeconds(request.getTimeoutSeconds());
        existing.setRemark(request.getRemark());

        if (Boolean.TRUE.equals(request.getActive() != null && request.getActive() == 1)) {
            llmConfigMapper.deactivateAll();
        }

        llmConfigMapper.updateById(existing);
        return toVO(existing);
    }

    @Override
    public void delete(Long id) {
        llmConfigMapper.deleteById(id);
    }

    @Override
    public void activate(Long id) {
        LlmConfig config = llmConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException(404, "配置不存在");
        }
        llmConfigMapper.deactivateAll();
        config.setActive(1);
        config.setEnabled(1);
        llmConfigMapper.updateById(config);
    }

    @Override
    public String testConnection(Long id) {
        LlmConfig config = llmConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException(404, "配置不存在");
        }
        if (config.getApiKey() == null || config.getApiKey().isEmpty()) {
            throw new BusinessException(400, "API Key 未配置");
        }

        String url = config.getBaseUrl();
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        url += "/chat/completions";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getModel());
        requestBody.put("temperature", 0.3);
        requestBody.put("messages", List.of(
                Map.of("role", "user", "content", "请回复：连接成功")
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getApiKey());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        int timeout = config.getTimeoutSeconds() != null ? config.getTimeoutSeconds() : 30;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(timeout * 1000);
        RestTemplate restTemplate = new RestTemplate(factory);

        long start = System.currentTimeMillis();
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            long elapsed = System.currentTimeMillis() - start;
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new BusinessException(500, "LLM API 返回非成功状态码");
            }
            Map<String, Object> body = response.getBody();
            Object choices = body.get("choices");
            if (choices instanceof List && !((List<?>) choices).isEmpty()) {
                Object first = ((List<?>) choices).get(0);
                if (first instanceof Map) {
                    Object message = ((Map<?, ?>) first).get("message");
                    if (message instanceof Map) {
                        Object content = ((Map<?, ?>) message).get("content");
                        if (content != null) {
                            return content.toString().trim() + "（耗时 " + elapsed + " ms）";
                        }
                    }
                }
            }
            return "连接成功（耗时 " + elapsed + " ms）";
        } catch (ResourceAccessException e) {
            long elapsed = System.currentTimeMillis() - start;
            log.warn("[LLM_TEST] 连接失败 | elapsedMs={} | error={}", elapsed, e.getMessage());
            throw new BusinessException(500, "LLM API 连接超时或无法访问（耗时 " + elapsed + " ms）");
        } catch (HttpStatusCodeException e) {
            long elapsed = System.currentTimeMillis() - start;
            log.warn("[LLM_TEST] HTTP 错误 | elapsedMs={} | status={} | body={}", elapsed, e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessException(500, "LLM API 请求失败：" + e.getStatusCode() + "（耗时 " + elapsed + " ms）");
        } catch (RestClientException e) {
            long elapsed = System.currentTimeMillis() - start;
            log.warn("[LLM_TEST] 请求异常 | elapsedMs={} | error={}", elapsed, e.getMessage());
            throw new BusinessException(500, "LLM API 请求失败：" + e.getMessage() + "（耗时 " + elapsed + " ms）");
        }
    }

    @Override
    public Map<String, Object> testReview(Long id) {
        LlmConfig config = llmConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException(404, "配置不存在");
        }
        if (config.getApiKey() == null || config.getApiKey().isEmpty()) {
            throw new BusinessException(400, "API Key 未配置");
        }

        String url = config.getBaseUrl();
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        url += "/chat/completions";

        int timeout = config.getTimeoutSeconds() != null ? config.getTimeoutSeconds() : 30;
        if (timeout < 30) timeout = 120;

        String shortPrompt = """
                综述主题：人工智能在医学影像中的应用
                请基于以下文献材料生成一篇结构化研究综述。
                要求：
                1. 标题为《人工智能在医学影像中的应用研究综述》。
                2. 不要逐篇堆砌摘要。
                3. 要进行综合、比较和归纳。
                4. 字数建议 700～1000 字。
                5. 输出必须包含六个部分：研究背景、研究现状、主要研究方向、存在问题、发展趋势、参考文献来源。

                文献材料：
                [1]
                标题：基于深度学习的肺部CT影像结节检测方法研究
                作者：张三, 李四
                年份：2023
                期刊：中华医学影像技术杂志
                关键词：深度学习；肺部CT；结节检测；卷积神经网络
                摘要：本文提出了一种基于改进卷积神经网络的肺部CT影像结节检测方法，通过引入注意力机制提升小结节检出率，实验结果表明该方法在公开数据集上的敏感度达到95.2%。
                正文节选：肺部结节是肺癌早期筛查的重要征象，传统人工阅片效率低且易受主观因素影响。本文提出的改进YOLOv5模型在LUNA16数据集上进行训练...

                [2]
                标题：人工智能辅助眼底病变筛查系统的设计与实现
                作者：王五, 赵六
                年份：2022
                期刊：中国生物医学工程学报
                关键词：人工智能；眼底病变；筛查系统；迁移学习
                摘要：本研究设计了一套基于迁移学习的眼底病变自动筛查系统，利用预训练EfficientNet模型对眼底图像进行分类，系统对糖尿病视网膜病变的识别准确率达到92.8%。
                正文节选：糖尿病视网膜病变是全球范围内导致视力损伤的主要病因之一。早期筛查与及时干预可有效延缓病情进展。本系统采用端到端的深度学习框架...

                请输出完整综述。
                """;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getModel());
        requestBody.put("temperature", 0.3);
        requestBody.put("max_tokens", 1200);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "你是一个严谨的中文学术综述写作助手。只能依据用户提供的文献材料生成内容，不得编造不存在的文献、作者、期刊、年份和数据。输出必须为中文。"),
                Map.of("role", "user", "content", shortPrompt)
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getApiKey());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(timeout * 1000);
        RestTemplate restTemplate = new RestTemplate(factory);

        long start = System.currentTimeMillis();
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            long elapsed = System.currentTimeMillis() - start;

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("elapsedMs", elapsed);
                result.put("errorType", "HTTP_ERROR");
                result.put("error", "LLM API 返回非成功状态码: " + response.getStatusCode());
                return result;
            }

            String content = extractContent(response.getBody());
            if (content == null || content.isBlank()) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("elapsedMs", elapsed);
                result.put("errorType", "EMPTY_RESPONSE");
                result.put("error", "LLM 返回内容为空");
                return result;
            }

            int sectionCount = 0;
            String[] sections = {"研究背景", "研究现状", "主要研究方向", "存在问题", "发展趋势", "参考文献来源"};
            for (String s : sections) {
                if (content.contains(s)) sectionCount++;
            }
            boolean validated = sectionCount >= 4;

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("elapsedMs", elapsed);
            result.put("contentLength", content.length());
            result.put("sectionCount", sectionCount);
            result.put("validated", validated);
            result.put("preview", content.length() > 200 ? content.substring(0, 200) + "..." : content);
            return result;
        } catch (ResourceAccessException e) {
            long elapsed = System.currentTimeMillis() - start;
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("elapsedMs", elapsed);
            result.put("errorType", "TIMEOUT");
            result.put("error", "LLM API 连接超时或无法访问: " + e.getMessage());
            return result;
        } catch (HttpStatusCodeException e) {
            long elapsed = System.currentTimeMillis() - start;
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("elapsedMs", elapsed);
            result.put("errorType", "HTTP_ERROR");
            result.put("error", "LLM API 请求失败: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
            return result;
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("elapsedMs", elapsed);
            result.put("errorType", "UNKNOWN_ERROR");
            result.put("error", e.getMessage());
            return result;
        }
    }

    @SuppressWarnings("unchecked")
    private String extractContent(Map<String, Object> body) {
        Object choices = body.get("choices");
        if (choices instanceof List && !((List<?>) choices).isEmpty()) {
            Object first = ((List<?>) choices).get(0);
            if (first instanceof Map) {
                Object message = ((Map<?, ?>) first).get("message");
                if (message instanceof Map) {
                    Object content = ((Map<?, ?>) message).get("content");
                    return content != null ? content.toString() : null;
                }
            }
        }
        return null;
    }

    private LlmConfigVO toVO(LlmConfig config) {
        LlmConfigVO vo = new LlmConfigVO();
        vo.setId(config.getId());
        vo.setName(config.getName());
        vo.setProvider(config.getProvider());
        vo.setBaseUrl(config.getBaseUrl());
        vo.setModel(config.getModel());
        vo.setApiKeyMasked(maskApiKey(config.getApiKey()));
        vo.setEnabled(config.getEnabled());
        vo.setActive(config.getActive());
        vo.setTimeoutSeconds(config.getTimeoutSeconds());
        vo.setRemark(config.getRemark());
        vo.setCreateTime(config.getCreateTime());
        vo.setUpdateTime(config.getUpdateTime());
        return vo;
    }

    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() <= 8) {
            return apiKey != null && !apiKey.isEmpty() ? "****" : "";
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }
}
