package com.prison.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class VisitorDTO {
    @NotNull(message = "服刑人员ID不能为空")
    private Long inmateId;

    @NotBlank(message = "探视人姓名不能为空")
    private String visitorName;

    @NotBlank(message = "探视人身份证不能为空")
    @Size(min = 18, max = 18)
    private String visitorIdCard;

    @NotBlank(message = "与被探视人关系不能为空")
    private String relationship;

    private String phone;

    @NotNull(message = "探视日期不能为空")
    private String visitDate;

    @NotBlank(message = "时段不能为空")
    private String timeSlot;

    private String remark;
}
