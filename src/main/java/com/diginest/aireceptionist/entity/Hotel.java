package com.diginest.aireceptionist.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "hotels")
@Getter
@Setter
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String address;

    private String phone;

    private String email;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "monthly_token_limit")
    private Integer monthlyTokenLimit = 100000;
}
