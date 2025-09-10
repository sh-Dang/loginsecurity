package com.sinse.loginsecurity.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private String username;
    private String password;
    private int age;
    private String role;
}
