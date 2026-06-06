package com.prison.dto.psych;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CrisisInterventionDTO {

    @NotNull(message = "服刑人员ID不能为空")
    private Long inmateId;

    private Long taskId;

    private Long resultId;

    private String urgency;

    private String assignedCounselor;

    private String riskLevel;

    private String interventionDate;

    private String interventionMethod;

    private String interventionContent;

    private String postInterventionAssessment;

    private Boolean riskReduced;

    private String followUpPlan;

    private String followUpDate;
}
