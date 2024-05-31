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
public class Countries {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String school;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer grade;

    @Column(nullable = false)
    private Integer classroom;

    @Column(nullable = false)
    private String unit;

    @ColumnDefault("0")
    private Integer treasury;

    @Column(nullable = false, updatable = false)
    private Integer salaryDate;

    //국가 활성화 여부(기본 TRUE)
    @Column(columnDefinition = "TINYINT(1) default 1")
    private Boolean available;

    @Column(nullable = false, updatable = false)
    private String eduOfficeCode;

    @Column(nullable = false, updatable = false)
    private String schoolCode;
    
    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private Users users;

    @Column(name = "user_id")
    private Long userId;

    @Builder.Default
    @OneToMany(mappedBy = "countries")
    private List<Students> students = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "countries")
    private List<Rules> rules = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "countries")
    private List<Invests> invests = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "countries")
    private List<Taxes> taxes = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "countries")
    private List<Seats> seats = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "countries", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<News> news = new ArrayList<>();

    public Countries(Long userId){
        this.userId = userId;
    }
}
