package com.isport.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.isport.dto.LoginRequest;
import com.isport.dto.LoginResponse;
import com.isport.dto.RegisterRequest;
import com.isport.dto.UserResponse;
import com.isport.entity.User;
import com.isport.exception.ApiException;
import com.isport.repository.UserRepository;
import com.isport.security.JwtService;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public UserResponse register(RegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La confirmation ne correspond pas au mot de passe");
        }

        String login = request.login().trim();
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Cet email est déjà utilisé");
        }
        if (userRepository.existsByLogin(login)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ce login est déjà utilisé");
        }

        User user = new User(login, email, passwordEncoder.encode(request.password()));
        return UserResponse.from(userRepository.save(user));
    }

    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
            .filter(found -> passwordEncoder.matches(request.password(), found.getPasswordHash()))
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Email ou mot de passe incorrect"));

        return new LoginResponse(jwtService.generateToken(user.getId()), UserResponse.from(user));
    }
}
