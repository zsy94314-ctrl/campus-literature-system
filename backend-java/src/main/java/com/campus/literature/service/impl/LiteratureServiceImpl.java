package com.campus.literature.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.literature.common.ErrorCode;
import com.campus.literature.common.PageResult;
import com.campus.literature.dto.LiteratureCreateRequest;
import com.campus.literature.dto.LiteratureSearchRequest;
import com.campus.literature.dto.LiteratureUpdateRequest;
import com.campus.literature.entity.Category;
import com.campus.literature.entity.Literature;
import com.campus.literature.exception.BusinessException;
import com.campus.literature.mapper.CategoryMapper;
import com.campus.literature.mapper.LiteratureMapper;
import com.campus.literature.service.LiteratureService;
import com.campus.literature.vo.LiteratureDetailVO;
import com.campus.literature.vo.LiteratureListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 文献服务实现
 */
@Service
@RequiredArgsConstructor
public class LiteratureServiceImpl implements LiteratureService {

    private final LiteratureMapper literatureMapper;
    private final CategoryMapper categoryMapper;

    @Override
    public PageResult<LiteratureListVO> search(LiteratureSearchRequest request) {
        // 使用自定义 SQL 查询（分页由 MyBatis-Plus 分页插件自动处理）
        int pageNum = request.getPage() != null ? request.getPage() : 1;
        int pageSize = request.getSize() != null ? request.getSize() : 10;
        Page<Literature> page = new Page<>(pageNum, pageSize);
        // 由于自定义SQL需要分页参数传递，这里改为用 LambdaQueryWrapper 方式
        LambdaQueryWrapper<Literature> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w.like(Literature::getTitle, request.getKeyword())
                    .or()
                    .like(Literature::getAbstractText, request.getKeyword())
                    .or()
                    .like(Literature::getKeywords, request.getKeyword()));
        }
        if (StringUtils.hasText(request.getAuthor())) {
            wrapper.like(Literature::getAuthors, request.getAuthor());
        }
        if (request.getCategoryId() != null) {
            wrapper.eq(Literature::getCategoryId, request.getCategoryId());
        }
        if (request.getYear() != null) {
            wrapper.eq(Literature::getPublishYear, request.getYear());
        }

        // 排序
        if ("year".equals(request.getSortBy())) {
            wrapper.orderByDesc(Literature::getPublishYear);
        } else if ("citation".equals(request.getSortBy())) {
            wrapper.orderByDesc(Literature::getCitationCount);
        } else {
            wrapper.orderByDesc(Literature::getId);
        }

        Page<Literature> resultPage = literatureMapper.selectPage(page, wrapper);

        List<LiteratureListVO> records = resultPage.getRecords().stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());

        return new PageResult<>(resultPage.getTotal(), records);
    }

    @Override
    public LiteratureDetailVO getById(Long id) {
        Literature literature = literatureMapper.selectById(id);
        if (literature == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return convertToDetailVO(literature);
    }

    @Override
    public void create(LiteratureCreateRequest request) {
        Literature literature = new Literature();
        literature.setTitle(request.getTitle());
        literature.setAuthors(request.getAuthors());
        literature.setAbstractText(request.getAbstractText());
        literature.setKeywords(request.getKeywords());
        literature.setJournal(request.getJournal());
        literature.setPublishYear(request.getPublishYear());
        literature.setDoi(request.getDoi());
        literature.setCategoryId(request.getCategoryId());
        literature.setCitationCount(request.getCitationCount() != null ? request.getCitationCount() : 0);
        literature.setSource("系统录入");
        literatureMapper.insert(literature);
    }

    @Override
    public void update(Long id, LiteratureUpdateRequest request) {
        Literature literature = literatureMapper.selectById(id);
        if (literature == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        literature.setTitle(request.getTitle());
        literature.setAuthors(request.getAuthors());
        literature.setAbstractText(request.getAbstractText());
        literature.setKeywords(request.getKeywords());
        literature.setJournal(request.getJournal());
        literature.setPublishYear(request.getPublishYear());
        literature.setDoi(request.getDoi());
        literature.setCategoryId(request.getCategoryId());
        literature.setCitationCount(request.getCitationCount());
        literatureMapper.updateById(literature);
    }

    @Override
    public void delete(Long id) {
        Literature literature = literatureMapper.selectById(id);
        if (literature == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        literatureMapper.deleteById(id);
    }

    @Override
    public List<LiteratureListVO> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<Literature> list = literatureMapper.selectBatchIds(ids);
        return list.stream().map(this::convertToListVO).collect(Collectors.toList());
    }

    private LiteratureListVO convertToListVO(Literature literature) {
        LiteratureListVO vo = new LiteratureListVO();
        vo.setId(literature.getId());
        vo.setTitle(literature.getTitle());
        vo.setAuthors(literature.getAuthors());
        vo.setKeywords(literature.getKeywords());
        vo.setJournal(literature.getJournal());
        vo.setPublishYear(literature.getPublishYear());
        vo.setCitationCount(literature.getCitationCount());
        return vo;
    }

    private LiteratureDetailVO convertToDetailVO(Literature literature) {
        LiteratureDetailVO vo = new LiteratureDetailVO();
        vo.setId(literature.getId());
        vo.setTitle(literature.getTitle());
        vo.setAuthors(literature.getAuthors());
        vo.setAbstractText(literature.getAbstractText());
        vo.setKeywords(literature.getKeywords());
        vo.setJournal(literature.getJournal());
        vo.setPublishYear(literature.getPublishYear());
        vo.setDoi(literature.getDoi());
        vo.setCitationCount(literature.getCitationCount());
        vo.setCategoryId(literature.getCategoryId());
        if (literature.getCategoryId() != null) {
            Category category = categoryMapper.selectById(literature.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }
        return vo;
    }
}
