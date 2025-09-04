package com.sinse.jwtlogin.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

//BCrypt 알고리즘을 적용한 비밀번호 생성 클래스
public class PasswordGenerator {
    public static String convert(String password){
        //매개변수로 전달된 평문 비밀번호를 BCrypt 알고리즘을 적용하여 반환
        //기본적으로 salt가 적용되어 있음
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String result = bCryptPasswordEncoder.encode(password);
        return result;
    }

    public static void main(String[] args) {
        System.out.println(convert("1234"));
    }
}
