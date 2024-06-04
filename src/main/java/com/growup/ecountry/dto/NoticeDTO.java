package com.growup.ecountry.dto;

import lombok.*;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeDTO {
    private Long id;
    private String content;
    private Boolean isChecked;
    private Date createdAt;
    private List<Long> studentId;
}
