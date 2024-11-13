package com.example.demo.security.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
