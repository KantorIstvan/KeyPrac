package com.example.dto;

public class LoginResponse {
    public String access_token;
    public String refresh_token;
    public String token_type;
    public int expires_in;

    public LoginResponse() {}

    public LoginResponse(String access_token, String refresh_token, String token_type, int expires_in) {
        this.access_token = access_token;
        this.refresh_token = refresh_token;
        this.token_type = token_type;
        this.expires_in = expires_in;
    }
}
