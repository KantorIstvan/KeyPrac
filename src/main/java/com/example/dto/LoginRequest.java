package com.example.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Please provide a valid email address")
    public String email;

    @NotBlank(message = "Password cannot be empty")
    public String password;
}
