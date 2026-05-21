package com.campus.literature.service;

import com.campus.literature.dto.ReviewGenerateRequest;
import com.campus.literature.vo.ReviewRecordVO;

import java.util.List;

/**
 * 综述记录服务接口
 */
public interface ReviewRecordService {

    ReviewRecordVO generate(ReviewGenerateRequest request);

    List<ReviewRecordVO> getMyHistory();

    ReviewRecordVO getById(Long id);

    void delete(Long id);
}
