package com.sinse.loginsecurity.domain;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="role")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int roleId;
    private String roleName;
}
