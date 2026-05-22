package com.campus.literature.service.impl;

import com.campus.literature.common.ErrorCode;
import com.campus.literature.dto.ReviewGenerateRequest;
import com.campus.literature.entity.Literature;
import com.campus.literature.entity.ReviewRecord;
import com.campus.literature.exception.BusinessException;
import com.campus.literature.mapper.LiteratureMapper;
import com.campus.literature.mapper.ReviewRecordMapper;
import com.campus.literature.security.UserContext;
import com.campus.literature.service.ReviewRecordService;
import com.campus.literature.vo.ReviewRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 综述记录服务实现
 */
@Service
@RequiredArgsConstructor
public class ReviewRecordServiceImpl implements ReviewRecordService {

    private final ReviewRecordMapper reviewRecordMapper;
    private final LiteratureMapper literatureMapper;

    @Override
    public ReviewRecordVO generate(ReviewGenerateRequest request) {
        Long userId = UserContext.getCurrentUserId();
        String topic = request.getTopic();
        List<Long> literatureIds = request.getLiteratureIds();

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

        String content = buildReviewContent(topic, literatures);

        // 构建参考列表
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

        // 保存记录
        ReviewRecord record = new ReviewRecord();
        record.setUserId(userId);
        record.setTopic(topic);
        record.setLiteratureIds(literatureIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
        record.setContent(content);
        record.setReferenceText(refText);
        reviewRecordMapper.insert(record);

        // 构建返回
        ReviewRecordVO vo = new ReviewRecordVO();
        vo.setId(record.getId());
        vo.setTopic(record.getTopic());
        vo.setContent(record.getContent());
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

    private String buildReviewContent(String topic, List<Literature> literatures) {
        StringBuilder sb = new StringBuilder();
        sb.append("《").append(formatTopic(topic)).append("》\n\n");

        // 一、研究背景
        sb.append("一、研究背景\n\n");
        sb.append(topic).append("是当前学术研究的重要议题，涉及理论探索与实践应用的双重维度。");
        sb.append("随着相关技术的不断发展，该领域吸引了越来越多的学者关注。");
        sb.append("为全面把握该领域的研究脉络，本文选取了").append(literatures.size()).append("篇代表性文献进行深入分析，");
        sb.append("力求从研究现状、核心方向、存在问题及未来趋势等方面进行系统性综述。\n\n");

        // 二、研究现状
        sb.append("二、研究现状\n\n");
        for (int i = 0; i < literatures.size(); i++) {
            Literature lit = literatures.get(i);
            String sourceText = StringUtils.hasText(lit.getContent()) ? lit.getContent() : lit.getAbstractText();
            sb.append("[").append(i + 1).append("] ").append(lit.getTitle()).append("\n");
            sb.append("作者：").append(lit.getAuthors()).append("；发表于《").append(lit.getJournal()).append("》（").append(lit.getPublishYear()).append("）\n");
            if (StringUtils.hasText(sourceText)) {
                String excerpt = extractExcerpt(sourceText, 180);
                // 避免数据库内容本身已带"正文节选："前缀导致重复显示
                if (excerpt.startsWith("正文节选：")) {
                    excerpt = excerpt.substring("正文节选：".length());
                }
                sb.append("内容概述：").append(excerpt).append("\n");
            }
            if (StringUtils.hasText(lit.getKeywords())) {
                sb.append("关键词：").append(lit.getKeywords()).append("\n");
            }
            sb.append("\n");
        }

        // 三、主要研究方向
        sb.append("三、主要研究方向\n\n");
        sb.append("基于上述文献的系统分析，可将").append(topic).append("领域的研究归纳为以下几个主要方向：\n\n");

        String[][] directionPool = {
                {"理论基础与方法论研究", "相关学者致力于构建该领域的理论框架，探索适用于该主题的研究方法与分析模型。"},
                {"系统平台与关键技术开发", "研究聚焦于设计与实现支持该领域应用的技术平台与核心算法。"},
                {"应用场景与实践验证", "通过案例研究、实验验证等方式，检验相关理论和方法在实际场景中的适用性。"},
                {"效果评价与治理机制研究", "关注该领域应用效果的评估指标构建，以及相应的规范治理与风险防控机制。"}
        };

        for (int i = 0; i < literatures.size() && i < directionPool.length; i++) {
            Literature lit = literatures.get(i);
            String[] dir = directionPool[i];
            sb.append(i + 1).append(". ").append(dir[0]).append("\n");
            String sourceText = StringUtils.hasText(lit.getContent()) ? lit.getContent() : lit.getAbstractText();
            if (StringUtils.hasText(sourceText)) {
                String excerpt = extractExcerpt(sourceText, 120);
                if (excerpt.startsWith("正文节选：")) {
                    excerpt = excerpt.substring("正文节选：".length());
                }
                sb.append("   ").append(excerpt).append("\n");
            } else {
                sb.append("   ").append(dir[1]).append("\n");
            }
            sb.append("\n");
        }

        // 四、存在问题
        sb.append("四、存在问题\n\n");
        sb.append("尽管").append(topic).append("取得了阶段性成果，但在发展过程中仍暴露出若干亟待解决的问题：\n\n");

        int problemCount = 1;
        for (Literature lit : literatures) {
            String abs = lit.getAbstractText();
            if (StringUtils.hasText(abs)) {
                String problem = extractProblemSentence(abs);
                if (StringUtils.hasText(problem) && hasNegativeKeyword(problem)) {
                    sb.append(problemCount++).append(". ").append(lit.getTitle()).append("的研究指出，")
                            .append(problem).append("\n");
                }
            }
        }
        if (problemCount <= 2) {
            sb.append(problemCount++).append(". 数据来源相对单一，实验样本的覆盖范围和代表性有待进一步扩大，影响了研究结论的普适性。\n");
            sb.append(problemCount++).append(". 现有模型或算法的可解释性不足，在实际推广应用中面临可信度与透明度的双重挑战。\n");
            sb.append(problemCount++).append(". 跨领域、跨场景的系统性验证仍然缺乏，研究的深度与广度均有待拓展。\n");
        }
        sb.append("\n");

        // 五、发展趋势
        sb.append("五、发展趋势\n\n");
        sb.append("展望未来，").append(topic).append("将朝着以下方向持续演进：\n\n");
        String[] trends = {
                "多源数据融合与智能分析：整合文本、行为、图像等多模态数据，借助语义检索与知识图谱技术，实现更全面、更深入的知识发现。",
                "跨学科协同与理论创新：打破单一学科壁垒，促进教育学、计算机科学、管理学等领域的深度交叉，催生新的理论增长点。",
                "可解释性与公平性提升：在追求技术性能的同时，更加注重算法的透明性、可解释性以及结果的公平性与伦理合规。",
                "智能化服务与决策支持：基于大数据与人工智能技术，构建面向高校教学、科研管理与公共服务等场景的智能决策支持系统。",
                "评价体系的完善与治理优化：建立科学、全面的效果评价指标体系，完善相关的规范治理机制，推动该领域的健康可持续发展。"
        };
        for (int i = 0; i < trends.length; i++) {
            sb.append(i + 1).append(". ").append(trends[i]).append("\n");
        }
        sb.append("\n");

        // 六、参考文献来源
        sb.append("六、参考文献来源\n\n");
        for (int i = 0; i < literatures.size(); i++) {
            Literature lit = literatures.get(i);
            sb.append("[").append(i + 1).append("] ").append(lit.getAuthors())
                    .append(". ").append(lit.getTitle())
                    .append(". 《").append(lit.getJournal()).append("》, ")
                    .append(lit.getPublishYear()).append(".\n");
        }

        return sb.toString();
    }

    private String extractExcerpt(String text, int maxLen) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String clean = text.replaceAll("\\s+", " ").trim();
        if (clean.length() <= maxLen) {
            return clean;
        }
        int end = clean.lastIndexOf("。", maxLen);
        if (end <= maxLen / 2) {
            end = clean.lastIndexOf(".", maxLen);
        }
        if (end <= maxLen / 2) {
            end = clean.lastIndexOf("；", maxLen);
        }
        if (end <= maxLen / 2) {
            end = maxLen;
        }
        return clean.substring(0, end) + (end < clean.length() ? "..." : "");
    }

    private String extractProblemSentence(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String[] sentences = text.split("[。；.\\n]");
        // 优先匹配明确的问题关键词
        for (String s : sentences) {
            String t = s.trim();
            if (t.contains("不足") || t.contains("局限") || t.contains("缺陷") || t.contains("挑战")
                    || t.contains("困难") || t.contains("瓶颈") || t.contains("薄弱") || t.contains("欠缺")) {
                return t;
            }
        }
        // 其次匹配"问题"，但要排除常见非问题表述
        for (String s : sentences) {
            String t = s.trim();
            if (t.contains("问题") && !t.contains("研究问题") && !t.contains("分析问题")
                    && !t.contains("问题分析") && !t.contains("解决问题") && !t.contains("提升问题")) {
                return t;
            }
        }
        return "";
    }

    private boolean hasNegativeKeyword(String text) {
        if (!StringUtils.hasText(text)) {
            return false;
        }
        String t = text;
        return t.contains("不足") || t.contains("局限") || t.contains("缺陷") || t.contains("挑战")
                || t.contains("困难") || t.contains("瓶颈") || t.contains("薄弱") || t.contains("欠缺")
                || t.contains("短板") || t.contains("滞后") || t.contains("不足");
    }

    private ReviewRecordVO convertToVO(ReviewRecord record) {
        ReviewRecordVO vo = new ReviewRecordVO();
        vo.setId(record.getId());
        vo.setTopic(record.getTopic());
        vo.setContent(record.getContent());
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
