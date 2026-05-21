package com.campus.literature.service;

import com.campus.literature.common.PageResult;
import com.campus.literature.dto.LiteratureCreateRequest;
import com.campus.literature.dto.LiteratureSearchRequest;
import com.campus.literature.dto.LiteratureUpdateRequest;
import com.campus.literature.vo.LiteratureDetailVO;
import com.campus.literature.vo.LiteratureListVO;

import java.util.List;

/**
 * 文献服务接口
 */
public interface LiteratureService {

    PageResult<LiteratureListVO> search(LiteratureSearchRequest request);

    LiteratureDetailVO getById(Long id);

    void create(LiteratureCreateRequest request);

    void update(Long id, LiteratureUpdateRequest request);

    void delete(Long id);

    List<LiteratureListVO> listByIds(List<Long> ids);
}
