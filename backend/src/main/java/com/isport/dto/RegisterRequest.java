package com.isport.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "Le login est obligatoire")
    @Size(max = 50, message = "Le login ne doit pas dépasser 50 caractères")
    String login,

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email n'est pas valide")
    String email,

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    String password,

    @NotBlank(message = "La confirmation du mot de passe est obligatoire")
    String confirmPassword
) {
}
