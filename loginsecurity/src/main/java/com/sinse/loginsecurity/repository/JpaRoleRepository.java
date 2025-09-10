package com.sinse.loginsecurity.repository;

import com.sinse.loginsecurity.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaRoleRepository extends JpaRepository<Role, Integer> {
    public Role findByRoleName(String roleName);
}
