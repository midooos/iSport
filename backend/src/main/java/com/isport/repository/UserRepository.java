package com.isport.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.isport.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    boolean existsByLogin(String login);

    Optional<User> findByEmail(String email);
}
