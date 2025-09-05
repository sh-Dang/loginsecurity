package com.sinse.loginsecurity.repository;

import com.sinse.loginsecurity.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserRepository extends JpaRepository<User, Integer> {
    public User findByUsername(String username); //username으로 user찾기
}
