package com.sinse.loginsecurity.config;

import com.sinse.loginsecurity.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        // 사용자가 Role 정보를 가지고 있는지 확인
        if (user.getRole() != null) {
            // Spring Security의 표준에 따라 "ROLE_" 접두사를 붙여서 권한을 추가합니다.
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getRoleName()));
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    // UserDetails의 나머지 메서드들은 기본값(true)을 반환하도록 오버라이드하는 것이 좋습니다.
    // 계정이 만료되지 않았는가?
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 계정이 잠기지 않았는가?
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 자격 증명(비밀번호)이 만료되지 않았는가?
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 계정이 활성화되었는가?
    @Override
    public boolean isEnabled() {
        return true;
    }
}