package com.sinse.loginsecurity.util;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class LogCounter {
    private final int count;

    public LogCounter(@Value("${loginsecurity.log.count}") int count) {
        this.count = count;
    }
}
