package com.smartinterview.backend.service;

import com.smartinterview.backend.dto.SessionConfigRequest;
import com.smartinterview.backend.dto.SessionResponse;
import com.smartinterview.backend.entity.InterviewSession;
import com.smartinterview.backend.entity.User;
import com.smartinterview.backend.repository.InterviewSessionRepository;
import com.smartinterview.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewSessionRepository sessionRepository;
    private final UserRepository userRepository;

    public SessionResponse createSession(SessionConfigRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        InterviewSession session = new InterviewSession();
        session.setUser(user);
        session.setPoste(request.getPoste());
        session.setType(request.getType());
        session.setDifficulte(request.getDifficulte());
        session.setLangue(request.getLangue());
        session.setNbQuestions(request.getNbQuestions());
        session.setStatut("EN_COURS");

        sessionRepository.save(session);
        return toResponse(session);
    }

    public List<SessionResponse> getMySessions(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        return sessionRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public SessionResponse getSessionById(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        InterviewSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session introuvable"));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Accès refusé");
        }

        return toResponse(session);
    }

    private SessionResponse toResponse(InterviewSession session) {
        return new SessionResponse(
                session.getId(),
                session.getPoste(),
                session.getType(),
                session.getDifficulte(),
                session.getLangue(),
                session.getNbQuestions(),
                session.getScoreGlobal(),
                session.getStatut(),
                session.getCreatedAt()
        );
    }
}