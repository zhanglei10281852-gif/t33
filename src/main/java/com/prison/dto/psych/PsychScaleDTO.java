package com.prison.dto.psych;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PsychScaleDTO {

    @NotBlank(message = "量表名称不能为空")
    private String name;

    @NotBlank(message = "量表编码不能为空")
    private String code;

    private String description;

    private Integer questionCount;

    @NotBlank(message = "量表类型不能为空")
    private String scaleType;

    private String scoringRule;
}
