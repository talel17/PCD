package com.smartinterview.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuestionResponse {
    private Long id;
    private String question;
    private String reponse;
    private String feedback;
    private Integer ordre;
}