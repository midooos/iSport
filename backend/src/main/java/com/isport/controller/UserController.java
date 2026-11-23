package com.isport.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.isport.dto.UserResponse;
import com.isport.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /api/users/me : { id, login, email } de l'utilisateur connecté
    // Le JwtAuthFilter place l'id de l'utilisateur connecté comme « principal »
    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal Long userId) {
        return userService.getCurrentUser(userId);
    }
}
