package com.sinse.loginsecurity;

import com.sinse.loginsecurity.domain.User;
import com.sinse.loginsecurity.repository.JpaUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class LoginsecurityApplication {

    /**
     * 임의로 insert하기 위한 테스트 케이스
     * */
    @Bean
    public CommandLineRunner commandLineRunner(JpaUserRepository jpaUserRepository,  PasswordEncoder passwordEncoder) {
        return args ->{
            if (jpaUserRepository.findByUsername("what") == null) {
                User user = new User();
                String password = passwordEncoder.encode("1234");
                user.setUsername("what");
                user.setPassword(password);
                user.setRole("ROLE_USER");

                jpaUserRepository.save(user);
            }
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(LoginsecurityApplication.class, args);
    }

}
