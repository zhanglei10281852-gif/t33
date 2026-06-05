package com.prison.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class InmateDTO {
    @NotBlank(message = "编号不能为空")
    private String inmateNo;

    @NotBlank(message = "姓名不能为空")
    private String name;

    @NotBlank(message = "身份证号不能为空")
    @Size(min = 18, max = 18, message = "身份证号必须18位")
    private String idCard;

    @NotBlank(message = "性别不能为空")
    private String gender;

    @NotNull(message = "出生日期不能为空")
    private String birthDate;

    @NotBlank(message = "罪名不能为空")
    private String crime;

    @NotNull(message = "刑期不能为空")
    @Min(value = 1, message = "刑期至少1个月")
    private Integer sentenceMonths;

    @NotNull(message = "入监日期不能为空")
    private String admissionDate;

    @NotBlank(message = "监区不能为空")
    private String ward;

    @NotBlank(message = "监室号不能为空")
    private String cellNo;
}
