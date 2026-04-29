package com.smartinterview.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class ReportFromAiRequest {

    @JsonProperty("candidate_name")
    private String candidateName;

    @JsonProperty("interview_date")
    private String interviewDate;

    @JsonProperty("room_name")
    private String roomName;

    @JsonProperty("overall_score")
    private Double overallScore;

    @JsonProperty("score_justification")
    private String scoreJustification;

    private List<StrengthWeakness> strengths;
    private List<StrengthWeakness> weaknesses;

    @JsonProperty("tips_for_future_interviews")
    private List<String> tips;

    private String summary;

    @Data
    public static class StrengthWeakness {
        private String title;
        private String description;
    }
}