package com.campus.literature.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 生成综述请求
 */
@Data
public class ReviewGenerateRequest {

    @NotBlank(message = "综述主题不能为空")
    private String topic;

    @NotEmpty(message = "文献列表不能为空")
    @Size(min = 2, max = 5, message = "参考文献数量必须在2-5篇之间")
    private List<Long> literatureIds;

    private String mode;
}
