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
import org.springframework.stereotype.Service;
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
        RestTemplate restTemplate = new RestTemplate();

        int timeout = config.getTimeoutSeconds() != null ? config.getTimeoutSeconds() : 30;
        restTemplate.getMessageConverters();

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
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
                            return content.toString().trim();
                        }
                    }
                }
            }
            return "连接成功";
        } catch (ResourceAccessException e) {
            log.warn("LLM API 连接失败: {}", e.getMessage());
            throw new BusinessException(500, "LLM API 连接超时或无法访问");
        } catch (RestClientException e) {
            log.warn("LLM API 请求异常: {}", e.getMessage());
            throw new BusinessException(500, "LLM API 请求失败：" + e.getMessage());
        }
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
