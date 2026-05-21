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

        // 获取文献列表
        List<Literature> literatures = literatureMapper.selectBatchIds(request.getLiteratureIds());
        if (literatures.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        String displayTitle = formatTopic(request.getTopic());
        StringBuilder content = new StringBuilder();
        content.append("# ").append(displayTitle).append("\n\n");

        // 研究背景
        content.append("## 一、研究背景\n\n");
        content.append("随着科学技术的快速发展，").append(request.getTopic())
                .append("逐渐成为学术界和产业界关注的热点。");
        content.append("本文基于").append(literatures.size()).append("篇相关文献，对该领域的研究现状进行综述。\n\n");

        // 研究现状
        content.append("## 二、研究现状\n\n");
        for (int i = 0; i < literatures.size(); i++) {
            Literature lit = literatures.get(i);
            content.append(i + 1).append(". **").append(lit.getTitle()).append("**\n");
            content.append("   - 作者：").append(lit.getAuthors()).append("\n");
            content.append("   - 摘要：").append(lit.getAbstractText()).append("\n\n");
        }

        // 主要研究方向
        content.append("## 三、主要研究方向\n\n");
        content.append("根据上述文献分析，该领域的主要研究方向包括：\n");
        content.append("1. 理论研究：深入探讨相关概念、模型和算法。\n");
        content.append("2. 应用实践：将研究成果应用于实际场景中。\n");
        content.append("3. 系统开发：设计和实现相关的智能系统或平台。\n\n");

        // 存在问题
        content.append("## 四、存在问题\n\n");
        content.append("当前研究仍面临一些挑战：\n");
        content.append("1. 数据质量和规模限制。\n");
        content.append("2. 算法的泛化能力有待提升。\n");
        content.append("3. 理论与实践的结合不够紧密。\n\n");

        // 未来发展趋势
        content.append("## 五、发展趋势\n\n");
        content.append("展望未来，该领域的发展趋势包括：\n");
        content.append("1. 多模态融合与跨领域应用。\n");
        content.append("2. 大模型技术的深入应用。\n");
        content.append("3. 个性化与智能化服务。\n\n");

        // 参考来源
        content.append("## 六、参考文献来源\n\n");
        for (Literature lit : literatures) {
            content.append("- ").append(lit.getTitle()).append("（").append(lit.getAuthors()).append("）\n");
        }

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
        record.setTopic(request.getTopic());
        record.setLiteratureIds(String.join(",", request.getLiteratureIds().stream().map(String::valueOf).collect(Collectors.toList())));
        record.setContent(content.toString());
        record.setReferenceText(refText);
        reviewRecordMapper.insert(record);

        // 构建返回
        ReviewRecordVO vo = new ReviewRecordVO();
        vo.setId(record.getId());
        vo.setTopic(record.getTopic());
        vo.setContent(record.getContent());
        vo.setReferences(references);
        // 数据库自动填充可能未及时回填，使用当前时间
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
        return t + "领域研究综述";
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
            vo.setReferences(new java.util.ArrayList<>());
        }
        return vo;
    }
}
