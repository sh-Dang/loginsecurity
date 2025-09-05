package com.sinse.loginsecurity.service;

import com.sinse.loginsecurity.config.CustomUserDetails;
import com.sinse.loginsecurity.domain.User;
import com.sinse.loginsecurity.repository.JpaUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class JpaUserDetailsService implements UserDetailsService {
    private final JpaUserRepository jpaUserRepository;
    public JpaUserDetailsService(JpaUserRepository jpaUserRepository) {
        this.jpaUserRepository = jpaUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = jpaUserRepository.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        // DB에서 찾은 user 객체를 CustomUserDetails로 감싸서 반환합니다.
        return new CustomUserDetails(user);
    }
}
