package com.smartinterview.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminStatsResponse {
    private Long totalUsers;
    private Long totalSessions;
    private Long sessionsTerminees;
    private Long sessionsEnCours;
    private Long totalReports;
}