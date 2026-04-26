package com.smartinterview.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class SessionResponse {
    private Long id;
    private String poste;
    private String type;
    private String difficulte;
    private String langue;
    private Integer nbQuestions;
    private BigDecimal scoreGlobal;
    private String statut;
    private LocalDateTime createdAt;
    private List<QuestionResponse> questions;
}