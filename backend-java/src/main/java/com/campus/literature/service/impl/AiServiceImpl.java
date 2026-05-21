package com.campus.literature.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.literature.entity.Literature;
import com.campus.literature.mapper.LiteratureMapper;
import com.campus.literature.service.AiService;
import com.campus.literature.vo.SemanticSearchVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AI 服务实现（模拟版本）
 */
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final LiteratureMapper literatureMapper;

    @Override
    public List<SemanticSearchVO> semanticSearch(String query, Integer topK) {
        if (!StringUtils.hasText(query)) {
            return List.of();
        }
        if (topK == null || topK <= 0) {
            topK = 10;
        }

        // 获取所有文献，模拟语义相似度计算
        List<Literature> allLiteratures = literatureMapper.selectList(null);
        String[] queryWords = query.split("\\s+");

        List<SemanticSearchVO> results = new ArrayList<>();
        for (Literature lit : allLiteratures) {
            double score = calcSimilarity(queryWords, lit);
            if (score > 0) {
                SemanticSearchVO vo = new SemanticSearchVO();
                vo.setLiteratureId(lit.getId());
                vo.setTitle(lit.getTitle());
                vo.setSimilarity(Math.min(score, 0.99));
                results.add(vo);
            }
        }

        // 按相似度排序并截取 topK
        return results.stream()
                .sorted(Comparator.comparingDouble(SemanticSearchVO::getSimilarity).reversed())
                .limit(topK)
                .collect(Collectors.toList());
    }

    @Override
    public List<SemanticSearchVO> recommend(Long literatureId) {
        Literature target = literatureMapper.selectById(literatureId);
        if (target == null) {
            return List.of();
        }

        // 获取同分类的其他文献
        LambdaQueryWrapper<Literature> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(Literature::getId, literatureId);
        if (target.getCategoryId() != null) {
            wrapper.eq(Literature::getCategoryId, target.getCategoryId());
        }
        List<Literature> candidates = literatureMapper.selectList(wrapper);

        // 根据关键词和标题相似度计算推荐分数
        List<SemanticSearchVO> results = new ArrayList<>();
        for (Literature lit : candidates) {
            double score = calcRecommendScore(target, lit);
            SemanticSearchVO vo = new SemanticSearchVO();
            vo.setLiteratureId(lit.getId());
            vo.setTitle(lit.getTitle());
            vo.setSimilarity(Math.min(score, 0.99));
            results.add(vo);
        }

        return results.stream()
                .sorted(Comparator.comparingDouble(SemanticSearchVO::getSimilarity).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * 计算查询与文献的相似度（基于关键词匹配模拟）
     */
    private double calcSimilarity(String[] queryWords, Literature literature) {
        String text = (literature.getTitle() + " " + literature.getAbstractText() + " " + literature.getKeywords()).toLowerCase();
        int matchCount = 0;
        for (String word : queryWords) {
            if (text.contains(word.toLowerCase())) {
                matchCount++;
            }
        }
        if (matchCount == 0) {
            return 0;
        }
        // 基础分 + 匹配比例
        return 0.3 + 0.6 * ((double) matchCount / queryWords.length);
    }

    /**
     * 计算两篇文献的推荐相似度
     */
    private double calcRecommendScore(Literature target, Literature other) {
        double score = 0.0;

        // 同分类加分
        if (target.getCategoryId() != null && target.getCategoryId().equals(other.getCategoryId())) {
            score += 0.3;
        }

        // 关键词相似度
        if (StringUtils.hasText(target.getKeywords()) && StringUtils.hasText(other.getKeywords())) {
            Set<String> targetKeywords = new HashSet<>(Arrays.asList(target.getKeywords().split(",|，|\\s+")));
            Set<String> otherKeywords = new HashSet<>(Arrays.asList(other.getKeywords().split(",|，|\\s+")));
            long common = targetKeywords.stream().filter(otherKeywords::contains).count();
            if (!targetKeywords.isEmpty()) {
                score += 0.4 * ((double) common / targetKeywords.size());
            }
        }

        // 标题相似度（简单判断包含关系）
        if (StringUtils.hasText(target.getTitle()) && StringUtils.hasText(other.getTitle())) {
            String[] targetWords = target.getTitle().split("\\s+");
            String otherTitle = other.getTitle();
            int match = 0;
            for (String word : targetWords) {
                if (word.length() > 1 && otherTitle.contains(word)) {
                    match++;
                }
            }
            if (targetWords.length > 0) {
                score += 0.3 * ((double) match / targetWords.length);
            }
        }

        return score;
    }
}
