package com.sinse.loginsecurity.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class LoginController {

    //테스트 용 함수 debug가 찍히는 지 봅시다.
    @GetMapping("/loginform")
    public String test() {
        log.debug("컨트롤러에 접근이 됐습니다.");
        return "/loginform.html";
    }
}
