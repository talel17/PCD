package com.smartinterview.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SessionConfigRequest {

    @NotBlank(message = "Le poste est obligatoire")
    private String poste;

    @NotBlank(message = "Le type est obligatoire")
    private String type; // TECHNIQUE | RH | COMPORTEMENTAL | MIXTE

    @NotBlank(message = "La difficulté est obligatoire")
    private String difficulte; // JUNIOR | MID | SENIOR

    @NotBlank(message = "La langue est obligatoire")
    private String langue; // FR | EN

    @Min(value = 1, message = "Minimum 1 questions")
    @Max(value = 20, message = "Maximum 20 questions")
    private Integer nbQuestions;

    private Long cvId; // optionnel — id du CV à utiliser
}