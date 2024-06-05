package com.growup.ecountry.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@Builder
public class Students {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer rollNumber;

    @Column(nullable = false)
    private String pw;

    @ColumnDefault("5")
    private Integer rating;

    private String img;

    private String jobImg;

    @Column(columnDefinition = "TINYINT(1) default 1")
    private Boolean available;

    @ManyToOne
    @JoinColumn(name = "country_id",insertable=false, updatable=false)
    private Countries countries;

    @Column(name = "country_id")
    private Long countryId;

    @ManyToOne
    @JoinColumn(name = "job_id", insertable = false, updatable = false)
    private Jobs jobs;

    @Column(name = "job_id")
    private Long jobId;

    @Builder.Default
    @OneToMany(mappedBy = "students", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<News> news = new ArrayList<>();

    public Students(Long countryId,Long jobId) {
        this.countryId = countryId;
        this.jobId = jobId;
    }
}
