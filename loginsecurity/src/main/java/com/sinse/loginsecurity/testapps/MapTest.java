package com.sinse.loginsecurity.testapps;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Map의 구조 및 메서드를 위한 MapTest.java
 * @author 이세형
 * */
public class MapTest {
    @Getter
    @Setter
    public static class Test{
        private int testId;
        private String testName;
    }

    public static void main(String[] args) throws Exception {
        // test 객체 생성
        Test test = new Test();
        test.setTestId(1);
        test.setTestName("test");

        Test test2 = new Test();
        test2.setTestId(2);
        test2.setTestName("test2");

        // test객체를 바꿔 담을 Map 객체 생성
        // Map.of => new Map();이랑 같은거라고 보면됨
        Map map = Map.of("test", test, "test2", test2);

        // Jackson ObjectMapper 생성
        ObjectMapper mapper = new ObjectMapper();

        // Map을 JSON 문자열로 변환
        String json = mapper.writeValueAsString(map);

        // 콘솔에 출력
        System.out.println("map의 구조는요" + json);

    }
}
