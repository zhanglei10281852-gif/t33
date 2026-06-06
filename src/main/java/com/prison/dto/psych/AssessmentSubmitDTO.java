package com.prison.dto.psych;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class AssessmentSubmitDTO {

    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    @NotNull(message = "服刑人员ID不能为空")
    private Long inmateId;

    @NotNull(message = "量表ID不能为空")
    private Long scaleId;

    @NotEmpty(message = "作答列表不能为空")
    private List<AnswerItemDTO> answers;

    private String remark;

    @Data
    public static class AnswerItemDTO {
        @NotNull(message = "题目ID不能为空")
        private Long questionId;

        @NotNull(message = "题号不能为空")
        private Integer questionNo;

        private Integer selectedOptionNo;

        private String answerValue;
    }
}
