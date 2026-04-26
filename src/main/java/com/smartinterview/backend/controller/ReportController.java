package com.smartinterview.backend.controller;

import com.smartinterview.backend.dto.ReportResponse;
import com.smartinterview.backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ReportController {

    private final ReportService reportService;

    // Générer + télécharger le rapport d'une session
    @GetMapping("/session/{sessionId}/download")
    public ResponseEntity<byte[]> downloadBySession(
            @PathVariable Long sessionId,
            Principal principal) throws Exception {

        byte[] pdfBytes = reportService.generatePdf(sessionId, principal.getName());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=rapport_entretien.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    // Envoyer le rapport par email
    @PostMapping("/session/{sessionId}/send-email")
    public ResponseEntity<String> sendEmail(
            @PathVariable Long sessionId,
            Principal principal) throws Exception {

        reportService.sendReportByEmail(sessionId, principal.getName());
        return ResponseEntity.ok("Rapport envoyé par email avec succès !");
    }

    // Mes rapports (historique)
    @GetMapping("/mine")
    public ResponseEntity<List<ReportResponse>> getMyReports(
            Principal principal) {
        return ResponseEntity.ok(
            reportService.getMyReports(principal.getName())
        );
    }

    // Télécharger un rapport depuis l'historique
    @GetMapping("/{reportId}/download")
    public ResponseEntity<byte[]> downloadReport(
            @PathVariable Long reportId,
            Principal principal) throws Exception {

        byte[] pdfBytes = reportService.downloadReport(reportId, principal.getName());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=rapport_entretien.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    
}