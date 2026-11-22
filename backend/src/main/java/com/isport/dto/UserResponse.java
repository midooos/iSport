package com.isport.dto;

import com.isport.entity.User;

public record UserResponse(Long id, String login, String email) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getLogin(), user.getEmail());
    }
}
