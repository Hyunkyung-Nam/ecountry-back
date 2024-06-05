package com.growup.ecountry.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class News {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, updatable = false, insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;

    @ManyToOne
    @JoinColumn(name = "country_id", insertable = false, updatable = false)
    private Countries countries;

    @Column(name = "country_id")
    private Long countryId;

    @ManyToOne
    @JoinColumn(name = "writer_id", insertable = false, updatable = false)
    private Students students;

    @Column(name = "writer_id")
    private Long writerId;

    public News(Long writerId, Long countryId){
        this.writerId = writerId;
        this.countryId = countryId;
    }
}
