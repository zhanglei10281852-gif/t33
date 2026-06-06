package com.prison.dto.psych;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssessmentTaskDTO {

    @NotNull(message = "服刑人员ID不能为空")
    private Long inmateId;

    @NotNull(message = "量表ID不能为空")
    private Long scaleId;

    private String taskType;

    @NotBlank(message = "计划完成日期不能为空")
    private String plannedDate;

    private String assignedCounselor;

    private String remark;
}
