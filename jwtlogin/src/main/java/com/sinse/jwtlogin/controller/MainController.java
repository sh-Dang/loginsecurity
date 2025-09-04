package com.sinse.jwtlogin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    //스프링 시큐리티의 기본으로 제공되는 폼 로그인 => 무조건 '/'로 redirect함
    //요청처리 필요
    @GetMapping("/")
    public String getMain(){
        return "index.html";
    }
}
