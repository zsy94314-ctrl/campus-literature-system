package com.campus.literature.service.impl;

import com.campus.literature.entity.Literature;
import com.campus.literature.entity.LlmConfig;
import com.campus.literature.service.LlmReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.SocketTimeoutException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            8. 输出必须完整包含六个部分，缺一不可：
               一、研究背景
               二、研究现状
               三、主要研究方向
               四、存在问题
               五、发展趋势
               六、参考文献来源
            9. 不得在“五、发展趋势”处提前结束输出。
            10. 最后一节“六、参考文献来源”必须完整列出所有输入文献，不得遗漏任何一篇。
            11. 不得输出不存在的文献编号。
            12. 生成完成前不要停止，必须输出完整六个部分后再结束。
            """;

    private static final Pattern CITATION_PATTERN = Pattern.compile("\\[(\\d+)\\]");
    private static final Set<String> TRUNCATED_ENDINGS = Set.of(
            "、", "，", "和", "可", "的", "与", "或", "及", "在", "是", "有", "了", "而",
            "对", "为", "以", "将", "向", "从", "到", "于", "中", "上", "下", "内",
            "等", "如", "例如", "包括", "以及", "随着", "通过", "基于", "针对", "面向",
            "进一步", "不断", "逐步", "日益", "持续", "加快", "深入", "广泛", "显著"
    );

    @Override
    public String generateReview(String topic, List<Literature> literatures, LlmConfig config) throws Exception {
        long startTime = System.currentTimeMillis();
        String finalUrl = null;
        Integer timeoutSeconds = null;
        String model = null;
        String baseUrl = null;

        try {
            if (config == null || config.getApiKey() == null || config.getApiKey().isEmpty()) {
                log.warn("[LLM_REVIEW] EMPTY_API_KEY");
                throw new IllegalStateException("EMPTY_API_KEY");
            }

            baseUrl = config.getBaseUrl();
            model = config.getModel();
            timeoutSeconds = config.getTimeoutSeconds();
            if (timeoutSeconds == null || timeoutSeconds < 30) {
                timeoutSeconds = 120;
            }

            String url = baseUrl;
            if (url.endsWith("/")) {
                url = url.substring(0, url.length() - 1);
            }
            url += "/chat/completions";
            finalUrl = url;

            String userPrompt = buildUserPrompt(topic, literatures);
            int promptLength = userPrompt.length();

            log.info("[LLM_REVIEW] START | baseUrl={} | model={} | timeoutSeconds={} | finalUrl={} | promptChars={} | litCount={}",
                    baseUrl, model, timeoutSeconds, finalUrl, promptLength, literatures.size());

            // 第一次调用
            LlmResult first = callLlm(url, config, model, userPrompt, timeoutSeconds, 2500);
            log.info("[LLM_REVIEW] FIRST | maxTokens={} | status=200 | finishReason={} | contentLength={}",
                    2500, first.finishReason, first.content != null ? first.content.length() : 0);

            String content = first.content;
            boolean needsRetry = "length".equals(first.finishReason);

            if (needsRetry) {
                log.warn("[LLM_REVIEW] RETRY | reason=finish_reason=length");
                LlmResult second = callLlm(url, config, model, userPrompt, timeoutSeconds, 3000);
                log.info("[LLM_REVIEW] SECOND | maxTokens={} | status=200 | finishReason={} | contentLength={}",
                        3000, second.finishReason, second.content != null ? second.content.length() : 0);
                content = second.content;
                if ("length".equals(second.finishReason)) {
                    log.warn("[LLM_REVIEW] INCOMPLETE | reason=finish_reason=length after retry");
                    throw new RuntimeException("FINISH_REASON_LENGTH: 两次均因 max_tokens 截断");
                }
            }

            // 内容为空检查
            if (content == null || content.isBlank()) {
                log.warn("[LLM_REVIEW] EMPTY_RESPONSE");
                throw new RuntimeException("EMPTY_RESPONSE");
            }

            content = content.trim();
            log.info("[LLM_REVIEW] RAW | contentLength={} | finishReason={}", content.length(), first.finishReason);

            // 诊断：各章节是否存在
            String[] allSections = {"研究背景", "研究现状", "主要研究方向", "存在问题", "发展趋势", "参考文献来源"};
            String[] bodySections = {"研究背景", "研究现状", "主要研究方向", "存在问题", "发展趋势"};
            boolean hasRefSection = content.contains("参考文献来源");
            int bodySectionCount = 0;
            List<String> missingBodySections = new ArrayList<>();
            for (String s : bodySections) {
                if (content.contains(s)) {
                    bodySectionCount++;
                } else {
                    missingBodySections.add(s);
                }
            }
            log.info("[LLM_REVIEW] DIAGNOSE | bodySections={}/5 | missingBodySections={} | hasRefSection={}",
                    bodySectionCount, missingBodySections, hasRefSection);

            // 前五个正文部分必须完整
            if (bodySectionCount < 5) {
                log.warn("[LLM_REVIEW] MISSING_SECTION | missingBodySections={} | contentPreview={}",
                        missingBodySections, content.length() > 300 ? content.substring(0, 300) : content);
                throw new RuntimeException("MISSING_SECTION: 缺少正文部分 " + missingBodySections);
            }

            // 结尾截断校验：只看最后一行是否以截断词结尾
            if (isTruncatedEnding(content)) {
                String lastLine = getLastLine(content);
                log.warn("[LLM_REVIEW] TRUNCATED_ENDING | lastLine={}", lastLine);
                throw new RuntimeException("TRUNCATED_ENDING: 内容结尾明显截断");
            }

            // 参考文献后端补全（正文完整即可补全，不降级）
            boolean needEnsureRefs = !hasRefSection;
            content = ensureReferencesComplete(content, literatures);
            log.info("[LLM_REVIEW] ENSURE_REFS | triggered={} | afterLength={}", needEnsureRefs, content.length());

            // 清理非法引用编号
            content = cleanInvalidCitations(content, literatures.size());

            // 最终确认包含六、参考文献来源
            if (!content.contains("参考文献来源")) {
                log.warn("[LLM_REVIEW] INCOMPLETE_REFERENCES: 补全后仍缺少参考文献来源");
                throw new RuntimeException("INCOMPLETE_REFERENCES: 补全后仍缺少参考文献来源");
            }

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("[LLM_REVIEW] SUCCESS | elapsedMs={} | contentLength={} | bodySections={} | finishReason={}",
                    elapsed, content.length(), bodySectionCount, first.finishReason);
            return content;
        } catch (ResourceAccessException e) {
            long elapsed = System.currentTimeMillis() - startTime;
            String exceptionType = "ResourceAccessException";
            if (e.getCause() instanceof SocketTimeoutException) {
                exceptionType = "SocketTimeoutException";
            }
            log.warn("[LLM_REVIEW] {} | elapsedMs={} | baseUrl={} | model={} | timeoutSeconds={} | finalUrl={} | error={}",
                    exceptionType, elapsed, baseUrl, model, timeoutSeconds, finalUrl, e.getMessage());
            throw new RuntimeException("TIMEOUT: " + e.getMessage(), e);
        } catch (HttpStatusCodeException e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.warn("[LLM_REVIEW] HttpStatusCodeException | elapsedMs={} | baseUrl={} | model={} | timeoutSeconds={} | finalUrl={} | status={} | body={}",
                    elapsed, baseUrl, model, timeoutSeconds, finalUrl, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("HTTP_ERROR: " + e.getStatusCode(), e);
        } catch (RuntimeException e) {
            long elapsed = System.currentTimeMillis() - startTime;
            String msg = e.getMessage() != null ? e.getMessage() : "";
            if (msg.startsWith("TIMEOUT:") || msg.startsWith("HTTP_ERROR:") || msg.startsWith("EMPTY_RESPONSE")
                    || msg.startsWith("MISSING_SECTION") || msg.startsWith("TRUNCATED_ENDING")
                    || msg.startsWith("FINISH_REASON_LENGTH") || msg.startsWith("INCOMPLETE_REFERENCES")) {
                throw e;
            }
            if (msg.contains("JSON") || msg.contains("json") || msg.contains("parse")) {
                log.warn("[LLM_REVIEW] JSON_PARSE_ERROR | elapsedMs={} | baseUrl={} | model={} | timeoutSeconds={} | finalUrl={} | error={}",
                        elapsed, baseUrl, model, timeoutSeconds, finalUrl, e.getMessage());
                throw new RuntimeException("JSON_PARSE_ERROR: " + e.getMessage(), e);
            }
            log.warn("[LLM_REVIEW] UNKNOWN_ERROR | elapsedMs={} | baseUrl={} | model={} | timeoutSeconds={} | finalUrl={} | error={}",
                    elapsed, baseUrl, model, timeoutSeconds, finalUrl, e.getMessage());
            throw new RuntimeException("UNKNOWN_ERROR: " + e.getMessage(), e);
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.warn("[LLM_REVIEW] UNKNOWN_ERROR | elapsedMs={} | baseUrl={} | model={} | timeoutSeconds={} | finalUrl={} | error={}",
                    elapsed, baseUrl, model, timeoutSeconds, finalUrl, e.getMessage());
            throw new RuntimeException("UNKNOWN_ERROR: " + e.getMessage(), e);
        }
    }

    private LlmResult callLlm(String url, LlmConfig config, String model, String userPrompt,
                               int timeoutSeconds, int maxTokens) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("temperature", 0.3);
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", SYSTEM_PROMPT),
                Map.of("role", "user", "content", userPrompt)
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getApiKey());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(timeoutSeconds * 1000);
        RestTemplate restTemplate = new RestTemplate(factory);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            log.warn("[LLM_REVIEW] HTTP_ERROR | status={}", response.getStatusCode());
            throw new RuntimeException("HTTP_ERROR: " + response.getStatusCode());
        }

        String content = extractContent(response.getBody());
        String finishReason = extractFinishReason(response.getBody());
        return new LlmResult(content != null ? content : "", finishReason);
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
        sb.append("8. 字数建议 900～1200 字。\n");
        sb.append("9. 必须完整输出六个部分，不得在“五、发展趋势”处提前结束。\n");
        sb.append("10. 最后一节“六、参考文献来源”必须完整列出所有输入文献 [1] 到 [").append(literatures.size()).append("]，不得遗漏任何一篇。\n");
        sb.append("11. 不得输出不存在的文献编号。\n");
        sb.append("12. 生成完成前不要停止。\n\n");
        sb.append("文献材料：\n\n");

        int maxAbstractLen = literatures.size() <= 3 ? 300 : 200;
        int maxContentLen = literatures.size() <= 3 ? 400 : 300;

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

            String abstractText = lit.getAbstractText();
            if (abstractText != null && !abstractText.isBlank()) {
                if (abstractText.length() > maxAbstractLen) {
                    abstractText = abstractText.substring(0, maxAbstractLen) + "...";
                }
                sb.append("摘要：").append(abstractText).append("\n");
            }

            String content = lit.getContent();
            if (content != null && !content.isBlank()) {
                boolean contentContainsAbstract = false;
                if (lit.getAbstractText() != null && !lit.getAbstractText().isBlank()) {
                    String absPrefix = lit.getAbstractText().trim();
                    if (absPrefix.length() > 80) {
                        absPrefix = absPrefix.substring(0, 80);
                    }
                    if (content.contains(absPrefix)) {
                        contentContainsAbstract = true;
                    }
                }

                if (!contentContainsAbstract) {
                    if (content.length() > maxContentLen) {
                        content = content.substring(0, maxContentLen) + "...";
                    }
                    sb.append("正文节选：").append(content).append("\n");
                }
            }
            sb.append("\n");
        }

        sb.append("请输出完整综述。");
        return sb.toString();
    }

    private boolean isTruncatedEnding(String content) {
        String lastLine = getLastLine(content);
        if (lastLine == null || lastLine.isBlank()) return false;
        String trimmed = lastLine.trim();
        for (String ending : TRUNCATED_ENDINGS) {
            if (trimmed.endsWith(ending)) {
                return true;
            }
        }
        return false;
    }

    private String getLastLine(String content) {
        if (content == null || content.isBlank()) return "";
        String[] lines = content.split("\n");
        for (int i = lines.length - 1; i >= 0; i--) {
            if (!lines[i].trim().isEmpty()) {
                return lines[i].trim();
            }
        }
        return "";
    }

    private String ensureReferencesComplete(String content, List<Literature> literatures) {
        String standardRefs = buildStandardReferences(literatures);
        int refIndex = content.indexOf("六、参考文献来源");
        if (refIndex == -1) {
            // 没有参考文献章节，直接追加
            return content + "\n\n六、参考文献来源\n\n" + standardRefs;
        }
        // 找到该章节标题后的第一个换行位置，替换该章节内容
        int lineEnd = content.indexOf('\n', refIndex);
        if (lineEnd == -1) {
            lineEnd = refIndex + "六、参考文献来源".length();
        }
        String before = content.substring(0, lineEnd);
        return before + "\n\n" + standardRefs;
    }

    private String buildStandardReferences(List<Literature> literatures) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < literatures.size(); i++) {
            Literature lit = literatures.get(i);
            sb.append("[").append(i + 1).append("] ")
                    .append(lit.getAuthors() != null ? lit.getAuthors() : "")
                    .append(". ")
                    .append(lit.getTitle() != null ? lit.getTitle() : "")
                    .append(". 《")
                    .append(lit.getJournal() != null ? lit.getJournal() : "")
                    .append("》, ")
                    .append(lit.getPublishYear() != null ? lit.getPublishYear() : "")
                    .append(".\n");
        }
        return sb.toString().trim();
    }

    private String cleanInvalidCitations(String content, int maxRef) {
        Matcher matcher = CITATION_PATTERN.matcher(content);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            int num = Integer.parseInt(matcher.group(1));
            if (num < 1 || num > maxRef) {
                matcher.appendReplacement(sb, "");
            }
        }
        matcher.appendTail(sb);
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

    @SuppressWarnings("unchecked")
    private String extractFinishReason(Map<String, Object> body) {
        Object choices = body.get("choices");
        if (choices instanceof List && !((List<?>) choices).isEmpty()) {
            Object first = ((List<?>) choices).get(0);
            if (first instanceof Map) {
                Object finishReason = ((Map<?, ?>) first).get("finish_reason");
                return finishReason != null ? finishReason.toString() : null;
            }
        }
        return null;
    }

    private static class LlmResult {
        final String content;
        final String finishReason;
        LlmResult(String content, String finishReason) {
            this.content = content;
            this.finishReason = finishReason;
        }
    }
}
