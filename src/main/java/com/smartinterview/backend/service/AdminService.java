package com.smartinterview.backend.service;

import com.smartinterview.backend.dto.AdminStatsResponse;
import com.smartinterview.backend.dto.AdminUserResponse;
import com.smartinterview.backend.entity.User;
import com.smartinterview.backend.repository.InterviewSessionRepository;
import com.smartinterview.backend.repository.ReportRepository;
import com.smartinterview.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final InterviewSessionRepository sessionRepository;
    private final ReportRepository reportRepository;

    // Liste de tous les candidats
    public List<AdminUserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .filter(u -> u.getRole().equals("CANDIDATE"))
                .map(u -> new AdminUserResponse(
                        u.getId(),
                        u.getNom(),
                        u.getEmail(),
                        u.getRole(),
                        u.getActif(),
                        u.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    // Activer / désactiver un compte
    public AdminUserResponse toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        user.setActif(!user.getActif());
        userRepository.save(user);

        return new AdminUserResponse(
                user.getId(),
                user.getNom(),
                user.getEmail(),
                user.getRole(),
                user.getActif(),
                user.getCreatedAt()
        );
    }

    // Statistiques globales
    public AdminStatsResponse getStats() {
        long totalUsers = userRepository.findAll()
                .stream()
                .filter(u -> u.getRole().equals("CANDIDATE"))
                .count();

        long totalSessions = sessionRepository.count();

        long sessionsTerminees = sessionRepository.findAll()
                .stream()
                .filter(s -> s.getStatut().equals("TERMINE"))
                .count();

        long sessionsEnCours = sessionRepository.findAll()
                .stream()
                .filter(s -> s.getStatut().equals("EN_COURS"))
                .count();

        long totalReports = reportRepository.count();

        return new AdminStatsResponse(
                totalUsers,
                totalSessions,
                sessionsTerminees,
                sessionsEnCours,
                totalReports
        );
    }
}