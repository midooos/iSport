package com.isport.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.isport.dto.UserResponse;
import com.isport.exception.ApiException;
import com.isport.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // US-18 : profil de l'utilisateur connecté
    // Si le compte n'existe plus (token d'un compte supprimé), on répond 401 comme pour un token invalide
    public UserResponse getCurrentUser(Long userId) {
        return userRepository.findById(userId)
            .map(UserResponse::from)
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Vous devez être connecté"));
    }
}
