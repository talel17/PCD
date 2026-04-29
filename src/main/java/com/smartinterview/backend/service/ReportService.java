package com.smartinterview.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.smartinterview.backend.dto.ReportFromAiRequest;
import com.smartinterview.backend.dto.ReportResponse;
import com.smartinterview.backend.entity.InterviewSession;
import com.smartinterview.backend.entity.Report;
import com.smartinterview.backend.entity.User;
import com.smartinterview.backend.repository.InterviewSessionRepository;
import com.smartinterview.backend.repository.ReportRepository;
import com.smartinterview.backend.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final InterviewSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final JavaMailSender mailSender;
    private final ObjectMapper objectMapper;

    // ── Lire le JSON depuis le dossier reports/ et générer le PDF ──
    public ReportResponse processJsonFile(Long sessionId, String jsonFileName) throws Exception {

        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session introuvable"));

        User user = session.getUser();

        // Lire le fichier JSON depuis reports/
        Path jsonPath = Paths.get("reports/" + jsonFileName);
        if (!Files.exists(jsonPath)) {
            throw new RuntimeException("Fichier JSON introuvable : " + jsonPath);
        }

        String jsonContent = new String(Files.readAllBytes(jsonPath));
        ReportFromAiRequest aiReport = objectMapper.readValue(jsonContent, ReportFromAiRequest.class);

        return saveAndGeneratePdf(sessionId, session, user, aiReport);
    }

    // ── Recevoir le JSON directement via POST (pour plus tard) ──
    public ReportResponse receiveAiReport(Long sessionId,
                                          ReportFromAiRequest aiReport) throws Exception {

        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session introuvable"));

        User user = session.getUser();

        return saveAndGeneratePdf(sessionId, session, user, aiReport);
    }

    // ── Logique commune : générer PDF + stocker + mettre à jour BDD ──
    private ReportResponse saveAndGeneratePdf(Long sessionId,
                                               InterviewSession session,
                                               User user,
                                               ReportFromAiRequest aiReport) throws Exception {
        // Générer le PDF
        byte[] pdfBytes = buildPdf(user, session, aiReport);

        // Sauvegarder le PDF sur le disque
        Path reportDir = Paths.get("uploads/reports");
        if (!Files.exists(reportDir)) Files.createDirectories(reportDir);

        String fileName = "rapport_session_" + sessionId + "_"
                + System.currentTimeMillis() + ".pdf";
        Path filePath = reportDir.resolve(fileName);
        Files.write(filePath, pdfBytes);

        // Sauvegarder en BDD
        Report report = reportRepository.findBySessionId(sessionId)
                .orElse(new Report());
        report.setSession(session);
        report.setUser(user);
        report.setFilePath(filePath.toString());
        report.setCandidateName(aiReport.getCandidateName());
        report.setOverallScore(BigDecimal.valueOf(aiReport.getOverallScore()));
        report.setScoreJustification(aiReport.getScoreJustification());
        report.setStrengths(objectMapper.writeValueAsString(aiReport.getStrengths()));
        report.setWeaknesses(objectMapper.writeValueAsString(aiReport.getWeaknesses()));
        report.setTips(objectMapper.writeValueAsString(aiReport.getTips()));
        report.setSummary(aiReport.getSummary());
        report.setRoomName(aiReport.getRoomName());
        report.setInterviewDate(aiReport.getInterviewDate());
        reportRepository.save(report);

        // Mettre à jour la session
        session.setStatut("TERMINE");
        session.setScoreGlobal(BigDecimal.valueOf(aiReport.getOverallScore()));
        sessionRepository.save(session);

        return toReportResponse(report);
    }

    // ── Télécharger un rapport existant ──
    public byte[] downloadReport(Long reportId, String email) throws Exception {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Rapport introuvable"));

        if (!report.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Accès refusé");
        }

        Path filePath = Paths.get(report.getFilePath());
        if (!Files.exists(filePath)) {
            throw new RuntimeException("Fichier PDF introuvable sur le serveur");
        }

        return Files.readAllBytes(filePath);
    }

    // ── Envoyer par email ──
    public void sendReportByEmail(Long reportId, String email) throws Exception {
        byte[] pdfBytes = downloadReport(reportId, email);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(email);
        helper.setSubject("Votre rapport d'entretien — Smart Interview");
        helper.setText(
            "Bonjour,\n\nVeuillez trouver ci-joint votre rapport d'entretien.\n\nL'équipe Smart Interview",
            false
        );
        helper.addAttachment("rapport_entretien.pdf",
                new ByteArrayResource(pdfBytes), "application/pdf");
        mailSender.send(message);

        reportRepository.findById(reportId).ifPresent(r -> {
            r.setEmailSent(true);
            r.setSentAt(LocalDateTime.now());
            reportRepository.save(r);
        });
    }

    // ── Mes rapports ──
    public List<ReportResponse> getMyReports(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        return reportRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toReportResponse)
                .collect(Collectors.toList());
    }

    // ── Construction PDF ──
    private byte[] buildPdf(User user, InterviewSession session,
                             ReportFromAiRequest r) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        DeviceRgb blue    = new DeviceRgb(59, 130, 246);
        DeviceRgb green   = new DeviceRgb(16, 185, 129);
        DeviceRgb red     = new DeviceRgb(239, 68, 68);
        DeviceRgb gray    = new DeviceRgb(107, 114, 128);
        DeviceRgb bgLight = new DeviceRgb(241, 245, 249);

        // Titre
        document.add(new Paragraph("Smart Interview — Rapport d'entretien")
                .setFontSize(22).setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(blue));
        document.add(new Paragraph(" "));

        // Infos
        document.add(new Paragraph("Informations du candidat")
                .setFontSize(14).setBold().setFontColor(blue));

        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .setWidth(UnitValue.createPercentValue(100));
        infoTable.addCell(cellGray("Candidat"));
        infoTable.addCell(cellWhite(r.getCandidateName() != null ? r.getCandidateName() : user.getNom()));
        infoTable.addCell(cellGray("Email"));
        infoTable.addCell(cellWhite(user.getEmail()));
        infoTable.addCell(cellGray("Date entretien"));
        infoTable.addCell(cellWhite(r.getInterviewDate() != null ? r.getInterviewDate() : "—"));
        infoTable.addCell(cellGray("Poste"));
        infoTable.addCell(cellWhite(session.getPoste() != null ? session.getPoste() : "—"));
        infoTable.addCell(cellGray("Type"));
        infoTable.addCell(cellWhite(session.getType() != null ? session.getType() : "—"));
        infoTable.addCell(cellGray("Difficulté"));
        infoTable.addCell(cellWhite(session.getDifficulte() != null ? session.getDifficulte() : "—"));
        document.add(infoTable);
        document.add(new Paragraph(" "));

        // Score
        DeviceRgb scoreColor = r.getOverallScore() >= 7 ? green :
                               r.getOverallScore() >= 5 ? blue : red;
        document.add(new Paragraph("Score global : " + r.getOverallScore() + " / 10")
                .setFontSize(18).setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(scoreColor));

        if (r.getScoreJustification() != null) {
            document.add(new Paragraph(r.getScoreJustification())
                    .setFontSize(11).setFontColor(gray).setItalic()
                    .setTextAlignment(TextAlignment.CENTER));
        }
        document.add(new Paragraph(" "));

        // Résumé
        if (r.getSummary() != null) {
            document.add(new Paragraph("Résumé global")
                    .setFontSize(13).setBold().setFontColor(blue));
            document.add(new Paragraph(r.getSummary())
                    .setFontSize(11).setBackgroundColor(bgLight).setPadding(10));
            document.add(new Paragraph(" "));
        }

        // Points forts
        if (r.getStrengths() != null && !r.getStrengths().isEmpty()) {
                document.add(new Paragraph("Points forts")
                .setFontSize(13).setBold().setFontColor(green));
                for (ReportFromAiRequest.StrengthWeakness s : r.getStrengths()) {
                        document.add(new Paragraph("• " + s.getTitle() + " : " + s.getDescription())
                                .setFontSize(11)
                                .setMarginLeft(15));
                }
                document.add(new Paragraph(" "));
        }

        // Points faibles
        if (r.getWeaknesses() != null && !r.getWeaknesses().isEmpty()) {
                document.add(new Paragraph("Points a ameliorer")
                        .setFontSize(13).setBold().setFontColor(red));
                for (ReportFromAiRequest.StrengthWeakness w : r.getWeaknesses()) {
                        document.add(new Paragraph("• " + w.getTitle() + " : " + w.getDescription())
                                .setFontSize(11)
                                .setMarginLeft(15));
                }
                document.add(new Paragraph(" "));
        }
        // Conseils
        if (r.getTips() != null && !r.getTips().isEmpty()) {
                document.add(new Paragraph("Conseils pour les prochains entretiens")
                        .setFontSize(13).setBold().setFontColor(blue));
                for (String tip : r.getTips()) {
                        document.add(new Paragraph("• " + tip)
                                .setFontSize(11)
                                .setMarginLeft(15));
                }
        }

        document.close();
        return baos.toByteArray();
    }

    private Cell cellGray(String text) {
        return new Cell().add(new Paragraph(text).setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY).setPadding(6);
    }

    private Cell cellWhite(String text) {
        return new Cell().add(new Paragraph(text)).setPadding(6);
    }

    private ReportResponse toReportResponse(Report r) {
        return new ReportResponse(
                r.getId(),
                r.getSession().getId(),
                r.getSession().getPoste(),
                r.getEmailSent(),
                r.getSentAt(),
                r.getCreatedAt()
        );
    }
}