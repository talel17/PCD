package com.smartinterview.backend.controller;

import com.smartinterview.backend.dto.AdminStatsResponse;
import com.smartinterview.backend.dto.AdminUserResponse;
import com.smartinterview.backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class AdminController {

    private final AdminService adminService;

    // Liste des candidats
    @GetMapping("/users")
    public ResponseEntity<List<AdminUserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    // Activer / désactiver un compte
    @PutMapping("/users/{id}/toggle")
    public ResponseEntity<AdminUserResponse> toggleUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.toggleUserStatus(id));
    }

    // Statistiques globales
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }
}