package com.smartinterview.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "interview_sessions")
@Data
public class InterviewSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 150)
    private String poste;

    @Column(length = 50)
    private String type;

    @Column(length = 20)
    private String difficulte;

    @Column(length = 10)
    private String langue;

    @Column(name = "nb_questions")
    private Integer nbQuestions;

    @Column(name = "score_global", precision = 4, scale = 2)
    private BigDecimal scoreGlobal;

    @Column(nullable = false, length = 20)
    private String statut = "EN_COURS";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    
}