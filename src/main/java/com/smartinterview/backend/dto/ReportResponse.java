package com.smartinterview.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ReportResponse {
    private Long id;
    private Long sessionId;
    private String poste;
    private Boolean emailSent;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
}