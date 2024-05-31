package com.growup.ecountry.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class KakaoResponse {
    private String id;
    private String model_version;
    private List<KakaoImg> images;
}
