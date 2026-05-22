package com.campus.literature.service.impl;

import com.campus.literature.entity.Literature;
import com.campus.literature.entity.LlmConfig;
import com.campus.literature.service.LlmReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * LLM 综述生成服务实现
 * 兼容 OpenAI-compatible Chat Completions API
 */
@Slf4j
@Service
public class LlmReviewServiceImpl implements LlmReviewService {

    private static final String SYSTEM_PROMPT = """
            你是一个严谨的中文学术综述写作助手。
            你的任务是根据用户提供的文献材料生成结构化综述。
            你必须遵守以下规则：
            1. 只能依据用户提供的文献材料生成内容。
            2. 不得编造不存在的文献、作者、期刊、年份和数据。
            3. 不得添加未提供的参考文献。
            4. 正文引用只能使用 [1]、[2]、[3] 这类编号。
            5. 参考文献来源必须与输入文献一一对应。
            6. 如果材料不足，应说明研究材料有限，不得虚构。
            7. 输出必须为中文。
            8. 输出必须包含六个部分：
               一、研究背景
               二、研究现状
               三、主要研究方向
               四、存在问题
               五、发展趋势
               六、参考文献来源
            """;

    @Override
    public String generateReview(String topic, List<Literature> literatures, LlmConfig config) throws Exception {
        if (config == null || config.getApiKey() == null || config.getApiKey().isEmpty()) {
            throw new IllegalStateException("LLM 配置不可用");
        }

        String userPrompt = buildUserPrompt(topic, literatures);

        String url = config.getBaseUrl();
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        url += "/chat/completions";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getModel());
        requestBody.put("temperature", 0.3);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", SYSTEM_PROMPT),
                Map.of("role", "user", "content", userPrompt)
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getApiKey());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new RuntimeException("LLM API 返回非成功状态码: " + response.getStatusCode());
            }

            String content = extractContent(response.getBody());
            if (content == null || content.isBlank()) {
                throw new RuntimeException("LLM 返回内容为空");
            }

            // 简单校验：必须包含六个章节标题中的至少四个
            int sectionCount = 0;
            String[] sections = {"研究背景", "研究现状", "主要研究方向", "存在问题", "发展趋势", "参考文献来源"};
            for (String s : sections) {
                if (content.contains(s)) sectionCount++;
            }
            if (sectionCount < 4) {
                throw new RuntimeException("LLM 返回内容格式不符合要求，仅包含 " + sectionCount + " 个章节");
            }

            return content.trim();
        } catch (ResourceAccessException e) {
            log.warn("LLM API 连接失败: {}", e.getMessage());
            throw new RuntimeException("LLM API 连接超时或无法访问");
        } catch (RestClientException e) {
            log.warn("LLM API 请求异常: {}", e.getMessage());
            throw new RuntimeException("LLM API 请求失败: " + e.getMessage());
        }
    }

    private String buildUserPrompt(String topic, List<Literature> literatures) {
        StringBuilder sb = new StringBuilder();
        sb.append("综述主题：\n").append(topic).append("\n\n");
        sb.append("请基于以下文献材料生成一篇结构化研究综述。\n\n");
        sb.append("要求：\n");
        sb.append("1. 标题为《").append(topic).append("研究综述》。\n");
        sb.append("2. 不要逐篇堆砌摘要。\n");
        sb.append("3. 要进行综合、比较和归纳。\n");
        sb.append("4. 研究现状应概括已有研究集中在哪些方面。\n");
        sb.append("5. 主要研究方向应归纳为 3 个左右方向。\n");
        sb.append("6. 存在问题和发展趋势要结合主题与文献材料。\n");
        sb.append("7. 参考文献来源必须使用下方提供的文献，不得新增。\n");
        sb.append("8. 字数建议不少于 900 字。\n\n");
        sb.append("文献材料：\n\n");

        for (int i = 0; i < literatures.size(); i++) {
            Literature lit = literatures.get(i);
            sb.append("[").append(i + 1).append("]\n");
            sb.append("标题：").append(lit.getTitle()).append("\n");
            sb.append("作者：").append(lit.getAuthors()).append("\n");
            sb.append("年份：").append(lit.getPublishYear()).append("\n");
            sb.append("期刊：").append(lit.getJournal()).append("\n");
            if (lit.getKeywords() != null) {
                sb.append("关键词：").append(lit.getKeywords()).append("\n");
            }
            if (lit.getAbstractText() != null) {
                sb.append("摘要：").append(lit.getAbstractText()).append("\n");
            }
            String content = lit.getContent() != null && !lit.getContent().isEmpty()
                    ? lit.getContent()
                    : lit.getAbstractText();
            if (content != null && content.length() > 500) {
                content = content.substring(0, 500) + "...";
            }
            if (content != null) {
                sb.append("正文节选：").append(content).append("\n");
            }
            sb.append("\n");
        }

        sb.append("请输出完整综述。");
        return sb.toString();
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
}
