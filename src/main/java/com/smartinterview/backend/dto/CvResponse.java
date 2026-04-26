package com.smartinterview.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CvResponse {
    private Long id;
    private String fileName;
    private String filePath;
    private LocalDateTime uploadedAt;
}