package com.smartinterview.backend.controller;

import com.smartinterview.backend.dto.ReportFromAiRequest;
import com.smartinterview.backend.dto.ReportResponse;
import com.smartinterview.backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class ReportController {

    private final ReportService reportService;

    // ── Lire le JSON depuis reports/ et générer le PDF ──
    // Test via : POST /api/report/session/1/from-file
    // Body : { "fileName": "report.json" }
    @PostMapping("/session/{sessionId}/from-file")
    public ResponseEntity<ReportResponse> processFromFile(
            @PathVariable Long sessionId,
            @RequestBody Map<String, String> body) throws Exception {

        String fileName = body.get("fileName");
        return ResponseEntity.ok(
            reportService.processJsonFile(sessionId, fileName)
        );
    }

    // ── Recevoir JSON directement (pour FastAPI plus tard) ──
    @PostMapping("/session/{sessionId}/ai-report")
    public ResponseEntity<ReportResponse> receiveAiReport(
            @PathVariable Long sessionId,
            @RequestBody ReportFromAiRequest aiReport) throws Exception {
        return ResponseEntity.ok(
            reportService.receiveAiReport(sessionId, aiReport)
        );
    }

    // ── Télécharger un rapport ──
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

    // ── Envoyer par email ──
    @PostMapping("/{reportId}/send-email")
    public ResponseEntity<String> sendEmail(
            @PathVariable Long reportId,
            Principal principal) throws Exception {
        reportService.sendReportByEmail(reportId, principal.getName());
        return ResponseEntity.ok("Rapport envoyé par email avec succès !");
    }

    // ── Historique ──
    @GetMapping("/mine")
    public ResponseEntity<List<ReportResponse>> getMyReports(
            Principal principal) {
        return ResponseEntity.ok(
            reportService.getMyReports(principal.getName())
        );
    }
}