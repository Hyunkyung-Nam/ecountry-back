package com.growup.ecountry.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class UserDTO {
    private Long id;
    private String name;
    private String userId;
    private String pw;
    private String img;
    private Boolean available;
}