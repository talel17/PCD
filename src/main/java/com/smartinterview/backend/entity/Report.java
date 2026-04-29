package com.smartinterview.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Data
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private InterviewSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "email_sent", nullable = false)
    private Boolean emailSent = false;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "candidate_name", length = 200)
    private String candidateName;

    @Column(name = "overall_score", precision = 4, scale = 2)
    private BigDecimal overallScore;

    @Column(name = "score_justification", columnDefinition = "TEXT")
    private String scoreJustification;

    @Column(name = "strengths", columnDefinition = "TEXT")
    private String strengths;

    @Column(name = "weaknesses", columnDefinition = "TEXT")
    private String weaknesses;

    @Column(name = "tips", columnDefinition = "TEXT")
    private String tips;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "room_name", length = 200)
    private String roomName;

    @Column(name = "interview_date", length = 50)
    private String interviewDate;
}