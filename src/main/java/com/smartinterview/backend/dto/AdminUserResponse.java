package com.smartinterview.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AdminUserResponse {
    private Long id;
    private String nom;
    private String email;
    private String role;
    private Boolean actif;
    private LocalDateTime createdAt;
}