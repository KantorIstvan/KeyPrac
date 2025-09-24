package com.example.service;

import com.example.User;
import com.example.dto.UserCreateRequest;
import com.example.dto.UserResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class UserService {

    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        // Check if user already exists
        if (User.findByUsername(request.username) != null) {
            throw new BadRequestException("Username already exists");
        }
        if (User.findByEmail(request.email) != null) {
            throw new BadRequestException("Email already exists");
        }

        User user = new User();
        user.username = request.username;
        user.email = request.email;
        user.firstName = request.firstName;
        user.lastName = request.lastName;
        user.roles = Set.of(User.Role.USER); // Default role
        user.persist();

        return new UserResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        return User.<User>listAll().stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        User user = User.findById(id);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        return new UserResponse(user);
    }

    public UserResponse getUserByUsername(String username) {
        User user = User.findByUsername(username);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        return new UserResponse(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = User.findById(id);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        user.delete();
    }

    @Transactional
    public UserResponse updateUserRoles(Long id, Set<User.Role> roles) {
        User user = User.findById(id);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        user.roles = roles;
        return new UserResponse(user);
    }
}
