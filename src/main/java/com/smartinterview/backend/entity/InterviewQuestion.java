package com.smartinterview.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "interview_questions")
@Data
public class InterviewQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private InterviewSession session;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(columnDefinition = "TEXT")
    private String reponse;

    @Column(precision = 4, scale = 2)
    private BigDecimal score;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(nullable = false)
    private Integer ordre;
}