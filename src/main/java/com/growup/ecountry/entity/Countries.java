package com.growup.ecountry.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
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

    @Column(nullable = false)
    private Integer treasury;

    @Column(nullable = false)
    private Integer salary_date;

    @ManyToOne
    @JoinTable(name = "user_id")
    private Users users;

    @OneToMany(mappedBy = "countries")
    private List<Students> students = new ArrayList<>();

    @OneToMany(mappedBy = "countries")
    private List<Rules> rules = new ArrayList<>();
}
