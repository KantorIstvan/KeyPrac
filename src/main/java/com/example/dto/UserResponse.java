package com.example.dto;

import com.example.User;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public class UserResponse {
    public UUID id;
    public String username;
    public String email;
    public String firstName;
    public String lastName;
    public Set<User.Role> roles;
    public LocalDateTime createdAt;
    public boolean active;

    public UserResponse() {}

    public UserResponse(User user) {
        this.id = user.id != null ? UUID.nameUUIDFromBytes(user.id.toString().getBytes()) : null;
        this.username = user.username;
        this.email = user.email;
        this.firstName = user.firstName;
        this.lastName = user.lastName;
        this.roles = user.roles;
        this.createdAt = user.createdAt;
        this.active = user.active;
    }
}
