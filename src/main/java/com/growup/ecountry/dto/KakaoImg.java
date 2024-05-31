package com.growup.ecountry.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoImg {
    private String id;
    private Long seed;
    private String image;
    private Boolean nsfw_content_detected;
    private Double nsfw_score;
}
