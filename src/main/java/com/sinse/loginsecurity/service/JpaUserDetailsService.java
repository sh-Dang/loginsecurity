package com.sinse.loginsecurity.service;

import com.sinse.loginsecurity.config.CustomUserDetails;
import com.sinse.loginsecurity.domain.User;
import com.sinse.loginsecurity.repository.JpaUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class JpaUserDetailsService implements UserDetailsService {
    private final JpaUserRepository jpaUserRepository;
    public JpaUserDetailsService(JpaUserRepository jpaUserRepository) {
        this.jpaUserRepository = jpaUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = jpaUserRepository.findByUsername(username);
        log.debug("12. 유저이름으로 꺼내온 유저 객체에 담긴 정보는 " + user.toString());
        log.debug("13. 유저이름으로 꺼내온 유저 객체에 담긴 userName은 === "+user.getUsername());
        log.debug("14. 유저이름으로 꺼내온 유저 객체에 담긴 password는 === "+user.getPassword());
        log.debug("15. 유저이름으로 꺼내온 유저 객체에 담긴 age는 === "+user.getAge());

        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        // DB에서 찾은 user 객체를 CustomUserDetails로 감싸서 반환합니다.
        return new CustomUserDetails(user);
    }
}
