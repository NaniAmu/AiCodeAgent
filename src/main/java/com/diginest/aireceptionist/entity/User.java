package com.diginest.aireceptionist.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.HOTEL_ADMIN;

    @Column(name = "is_active")
    private Boolean isActive = true;

    public enum Role {
        SUPER_ADMIN,
        HOTEL_ADMIN,
        HOTEL_STAFF
    }
}
