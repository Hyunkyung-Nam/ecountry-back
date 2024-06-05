package com.growup.ecountry.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleDTO {
    private Long id;
    private String rule;
    private Long countryId;

}
