package com.smartinterview.backend.service;

import com.smartinterview.backend.dto.QuestionResponse;
import com.smartinterview.backend.dto.SessionConfigRequest;
import com.smartinterview.backend.dto.SessionResponse;
import com.smartinterview.backend.entity.InterviewSession;
import com.smartinterview.backend.entity.User;
import com.smartinterview.backend.repository.InterviewQuestionRepository;
import com.smartinterview.backend.repository.InterviewSessionRepository;
import com.smartinterview.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewSessionRepository sessionRepository;
    private final InterviewQuestionRepository questionRepository;
    private final UserRepository userRepository;

    // Créer une nouvelle session configurée
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

        return toSessionResponse(session, Collections.emptyList());
    }

    // Récupérer toutes mes sessions
    public List<SessionResponse> getMySessions(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        return sessionRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(session -> {
                    List<QuestionResponse> questions = questionRepository
                            .findBySessionOrderByOrdreAsc(session)
                            .stream()
                            .map(q -> new QuestionResponse(
                                    q.getId(),
                                    q.getQuestion(),
                                    q.getReponse(),
                                    q.getFeedback(),
                                    q.getOrdre()
                            ))
                            .collect(Collectors.toList());
                    return toSessionResponse(session, questions);
                })
                .collect(Collectors.toList());
    }

    // Récupérer une session par id
    public SessionResponse getSessionById(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        InterviewSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session introuvable"));

        // Sécurité — un candidat ne peut voir que SES sessions
        if (!session.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Accès refusé");
        }

        List<QuestionResponse> questions = questionRepository
                .findBySessionOrderByOrdreAsc(session)
                .stream()
                .map(q -> new QuestionResponse(
                        q.getId(),
                        q.getQuestion(),
                        q.getReponse(),
                        q.getFeedback(),
                        q.getOrdre()
                ))
                .collect(Collectors.toList());

        return toSessionResponse(session, questions);
    }

    // Méthode utilitaire — convertir Session en SessionResponse
    private SessionResponse toSessionResponse(InterviewSession session,
                                               List<QuestionResponse> questions) {
        return new SessionResponse(
                session.getId(),
                session.getPoste(),
                session.getType(),
                session.getDifficulte(),
                session.getLangue(),
                session.getNbQuestions(),
                session.getScoreGlobal(),
                session.getStatut(),
                session.getCreatedAt(),
                questions
        );
    }
}