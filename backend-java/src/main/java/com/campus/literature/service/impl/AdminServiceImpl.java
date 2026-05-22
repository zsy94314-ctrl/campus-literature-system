package com.campus.literature.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.literature.common.ErrorCode;
import com.campus.literature.dto.UserStatusRequest;
import com.campus.literature.entity.AdminLog;
import com.campus.literature.entity.Category;
import com.campus.literature.entity.Literature;
import com.campus.literature.entity.LlmConfig;
import com.campus.literature.entity.ReviewRecord;
import com.campus.literature.entity.SearchHistory;
import com.campus.literature.entity.User;
import com.campus.literature.exception.BusinessException;
import com.campus.literature.mapper.AdminLogMapper;
import com.campus.literature.mapper.CategoryMapper;
import com.campus.literature.mapper.FavoriteMapper;
import com.campus.literature.mapper.LiteratureMapper;
import com.campus.literature.mapper.LlmConfigMapper;
import com.campus.literature.mapper.ReviewRecordMapper;
import com.campus.literature.mapper.SearchHistoryMapper;
import com.campus.literature.mapper.UserMapper;
import com.campus.literature.security.UserContext;
import com.campus.literature.service.AdminService;
import com.campus.literature.vo.CategoryStatVO;
import com.campus.literature.vo.DocumentTypeDistributionVO;
import com.campus.literature.vo.ReviewModeDistributionVO;
import com.campus.literature.vo.SearchTypeDistributionVO;
import com.campus.literature.vo.StatisticsVO;
import com.campus.literature.vo.UserVO;
import com.campus.literature.vo.YearDistributionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 管理员服务实现
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final LiteratureMapper literatureMapper;
    private final CategoryMapper categoryMapper;
    private final ReviewRecordMapper reviewRecordMapper;
    private final AdminLogMapper adminLogMapper;
    private final FavoriteMapper favoriteMapper;
    private final SearchHistoryMapper searchHistoryMapper;
    private final LlmConfigMapper llmConfigMapper;

    @Override
    public List<UserVO> getUserList() {
        List<User> users = userMapper.selectList(null);
        return users.stream().map(this::convertToVO).toList();
    }

    @Override
    public void updateUserStatus(Long id, UserStatusRequest request) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        user.setStatus(request.getStatus());
        userMapper.updateById(user);

        // 记录管理员日志
        AdminLog log = new AdminLog();
        log.setAdminId(UserContext.getCurrentUserId());
        log.setOperation("UPDATE_USER_STATUS");
        log.setTargetType("USER");
        log.setTargetId(id);
        log.setDetail("修改用户状态为: " + request.getStatus());
        adminLogMapper.insert(log);
    }

    @Override
    public StatisticsVO getStatistics() {
        StatisticsVO vo = new StatisticsVO();

        // 1. 基础计数
        vo.setUserCount(safeCount(userMapper.selectCount(null)));
        vo.setLiteratureCount(safeCount(literatureMapper.selectCount(null)));
        vo.setReviewCount(safeCount(reviewRecordMapper.selectCount(null)));
        vo.setCategoryCount(safeCount(categoryMapper.selectCount(null)));

        // 2. 扩展计数
        vo.setFavoriteCount(safeCount(favoriteMapper.selectCount(null)));
        vo.setSearchHistoryCount(safeCount(searchHistoryMapper.selectCount(null)));
        vo.setLlmConfigCount(safeCount(llmConfigMapper.selectCount(null)));

        // 3. 当前活跃 LLM 配置
        LlmConfig activeConfig = llmConfigMapper.selectActive();
        if (activeConfig != null) {
            vo.setActiveLlmName(activeConfig.getName());
            vo.setActiveLlmProvider(activeConfig.getProvider());
            vo.setActiveLlmModel(activeConfig.getModel());
            vo.setActiveLlmEnabled(activeConfig.getEnabled() != null && activeConfig.getEnabled() == 1);
        } else {
            vo.setActiveLlmName(null);
            vo.setActiveLlmProvider(null);
            vo.setActiveLlmModel(null);
            vo.setActiveLlmEnabled(false);
        }

        // 4. 分类 Top 10
        vo.setCategoryTop(buildCategoryTop());

        // 5. 年份分布
        vo.setYearDistribution(buildYearDistribution());

        // 6. 文献类型分布
        vo.setDocumentTypeDistribution(buildDocumentTypeDistribution());

        // 7. 综述生成方式分布
        vo.setReviewModeDistribution(buildReviewModeDistribution());

        // 8. 检索类型分布
        vo.setSearchTypeDistribution(buildSearchTypeDistribution());

        return vo;
    }

    private Long safeCount(Long count) {
        return count == null ? 0L : count;
    }

    private List<CategoryStatVO> buildCategoryTop() {
        try {
            List<Map<String, Object>> maps = literatureMapper.selectMaps(
                    new QueryWrapper<Literature>()
                            .select("category_id", "count(*) as cnt")
                            .groupBy("category_id")
                            .orderByDesc("cnt")
                            .last("LIMIT 10")
            );
            if (maps == null || maps.isEmpty()) {
                return Collections.emptyList();
            }
            List<CategoryStatVO> result = new ArrayList<>();
            for (Map<String, Object> map : maps) {
                Object catIdObj = map.get("category_id");
                Long catId = catIdObj == null ? null : Long.valueOf(catIdObj.toString());
                Long cnt = map.get("cnt") == null ? 0L : Long.valueOf(map.get("cnt").toString());

                String name = "未分类";
                if (catId != null) {
                    Category category = categoryMapper.selectById(catId);
                    if (category != null) {
                        name = category.getName();
                    }
                }
                CategoryStatVO item = new CategoryStatVO();
                item.setName(name);
                item.setCount(cnt);
                result.add(item);
            }
            return result;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private List<YearDistributionVO> buildYearDistribution() {
        try {
            List<Map<String, Object>> maps = literatureMapper.selectMaps(
                    new QueryWrapper<Literature>()
                            .select("publish_year", "count(*) as cnt")
                            .groupBy("publish_year")
                            .orderByDesc("publish_year")
            );
            if (maps == null || maps.isEmpty()) {
                return Collections.emptyList();
            }
            List<YearDistributionVO> result = new ArrayList<>();
            for (Map<String, Object> map : maps) {
                Object yearObj = map.get("publish_year");
                Integer year = yearObj == null ? null : Integer.valueOf(yearObj.toString());
                Long cnt = map.get("cnt") == null ? 0L : Long.valueOf(map.get("cnt").toString());
                if (year == null) {
                    continue;
                }
                YearDistributionVO item = new YearDistributionVO();
                item.setYear(year);
                item.setCount(cnt);
                result.add(item);
            }
            return result;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private List<DocumentTypeDistributionVO> buildDocumentTypeDistribution() {
        try {
            List<Map<String, Object>> maps = literatureMapper.selectMaps(
                    new QueryWrapper<Literature>()
                            .select("document_type", "count(*) as cnt")
                            .groupBy("document_type")
                            .orderByDesc("cnt")
            );
            if (maps == null || maps.isEmpty()) {
                return Collections.emptyList();
            }
            List<DocumentTypeDistributionVO> result = new ArrayList<>();
            for (Map<String, Object> map : maps) {
                String type = map.get("document_type") == null ? "未分类" : map.get("document_type").toString();
                Long cnt = map.get("cnt") == null ? 0L : Long.valueOf(map.get("cnt").toString());
                DocumentTypeDistributionVO item = new DocumentTypeDistributionVO();
                item.setType(type);
                item.setCount(cnt);
                result.add(item);
            }
            return result;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private List<ReviewModeDistributionVO> buildReviewModeDistribution() {
        try {
            List<Map<String, Object>> maps = reviewRecordMapper.selectMaps(
                    new QueryWrapper<ReviewRecord>()
                            .select("generation_mode", "count(*) as cnt")
                            .groupBy("generation_mode")
            );
            if (maps == null || maps.isEmpty()) {
                return Collections.emptyList();
            }
            List<ReviewModeDistributionVO> result = new ArrayList<>();
            for (Map<String, Object> map : maps) {
                String mode = map.get("generation_mode") == null ? "unknown" : map.get("generation_mode").toString();
                Long cnt = map.get("cnt") == null ? 0L : Long.valueOf(map.get("cnt").toString());

                String label;
                switch (mode) {
                    case "rule":
                        label = "离线综述生成";
                        break;
                    case "llm":
                        label = "在线 LLM 综述生成";
                        break;
                    case "llm_fallback_rule":
                        label = "LLM 降级离线生成";
                        break;
                    case "unknown":
                        label = "未知";
                        break;
                    default:
                        label = "未知";
                        break;
                }

                ReviewModeDistributionVO item = new ReviewModeDistributionVO();
                item.setMode(mode);
                item.setLabel(label);
                item.setCount(cnt);
                result.add(item);
            }
            return result;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private List<SearchTypeDistributionVO> buildSearchTypeDistribution() {
        try {
            List<Map<String, Object>> maps = searchHistoryMapper.selectMaps(
                    new QueryWrapper<SearchHistory>()
                            .select("search_type", "count(*) as cnt")
                            .groupBy("search_type")
            );
            if (maps == null || maps.isEmpty()) {
                return Collections.emptyList();
            }
            List<SearchTypeDistributionVO> result = new ArrayList<>();
            for (Map<String, Object> map : maps) {
                String type = map.get("search_type") == null ? "UNKNOWN" : map.get("search_type").toString();
                Long cnt = map.get("cnt") == null ? 0L : Long.valueOf(map.get("cnt").toString());

                String label;
                switch (type) {
                    case "NORMAL":
                        label = "普通检索";
                        break;
                    case "SEMANTIC":
                        label = "语义检索";
                        break;
                    default:
                        label = "其他";
                        break;
                }

                SearchTypeDistributionVO item = new SearchTypeDistributionVO();
                item.setType(type);
                item.setLabel(label);
                item.setCount(cnt);
                result.add(item);
            }
            return result;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setEmail(user.getEmail());
        return vo;
    }
}
