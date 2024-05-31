package com.growup.ecountry.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CountryDTO {
    private Long id;
    private String school;
    private String name;
    private Integer grade;
    private Integer classroom;
    private String unit;
    private Integer treasury;
    private Integer salaryDate;
    private Boolean available;
    private String eduOfficeCode;
    private String schoolCode;
}
