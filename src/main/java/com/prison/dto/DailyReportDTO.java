package com.prison.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class DailyReportDTO {
    @NotNull(message = "服刑人员ID不能为空")
    private Long inmateId;

    @NotNull(message = "日期不能为空")
    private String reportDate;

    @NotBlank(message = "健康状况不能为空")
    private String healthStatus;

    private String behaviorNote;

    @NotBlank(message = "情绪状态不能为空")
    private String moodLevel;

    @NotBlank(message = "记录人不能为空")
    private String reporter;
}
