package com.vsp.user.controller;

import com.vsp.user.model.entity.User;
import com.vsp.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PutMapping("/{id}/profile")
    public User updateProfile(@PathVariable Long id, @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(id, request.displayName(), request.avatarUrl());
    }

    public record UpdateProfileRequest(String displayName, String avatarUrl) {
    }
}
