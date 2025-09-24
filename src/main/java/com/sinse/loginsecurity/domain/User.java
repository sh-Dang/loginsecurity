package com.sinse.loginsecurity.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="user")
public class User {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int userId;
    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private int age;
    @ManyToOne
    @JoinColumn(name="role_id")
    private Role role;
}
