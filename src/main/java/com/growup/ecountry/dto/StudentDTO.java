package com.growup.ecountry.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentDTO {
    private Long id;
    private String name;
    private Integer rollNumber;
    private String pw;
    private Integer rating;
    private String img;
    private String jobImg;
    private Boolean available;
    private Long countryId;
    private Long jobId;
}
