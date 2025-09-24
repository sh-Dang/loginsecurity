package com.sinse.loginsecurity.repository;

import com.sinse.loginsecurity.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaRoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleName(String roleName);
}
