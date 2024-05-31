package com.growup.ecountry.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String pw;

    private String img;

    @Column(columnDefinition = "TINYINT(1) default 1")
    private Boolean available;

    @Builder.Default
    @OneToMany(mappedBy = "users")
    private List<Countries> countries = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "users", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<News> news = new ArrayList<>();

}
