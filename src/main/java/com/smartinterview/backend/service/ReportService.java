package com.smartinterview.backend.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.smartinterview.backend.dto.ReportResponse;
import com.smartinterview.backend.entity.InterviewQuestion;
import com.smartinterview.backend.entity.InterviewSession;
import com.smartinterview.backend.entity.Report;
import com.smartinterview.backend.entity.User;
import com.smartinterview.backend.repository.InterviewQuestionRepository;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final InterviewSessionRepository sessionRepository;
    private final InterviewQuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final JavaMailSender mailSender;

    // Générer + sauvegarder le PDF
    public byte[] generatePdf(Long sessionId, String email) throws Exception {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session introuvable"));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Accès refusé");
        }

        List<InterviewQuestion> questions =
                questionRepository.findBySessionOrderByOrdreAsc(session);

        // Générer le PDF en mémoire
        byte[] pdfBytes = buildPdf(user, session, questions);

        // Sauvegarder sur le disque
        Path reportDir = Paths.get("uploads/reports");
        if (!Files.exists(reportDir)) {
            Files.createDirectories(reportDir);
        }

        String fileName = "rapport_session_" + sessionId + "_"
                + System.currentTimeMillis() + ".pdf";
        Path filePath = reportDir.resolve(fileName);
        Files.write(filePath, pdfBytes);

        // Sauvegarder en BDD (un seul rapport par session)
        Report report = reportRepository.findBySessionId(sessionId)
                .orElse(new Report());
        report.setSession(session);
        report.setUser(user);
        report.setFilePath(filePath.toString());
        reportRepository.save(report);

        return pdfBytes;
    }

    // Envoyer par email
    public void sendReportByEmail(Long sessionId, String email) throws Exception {
        byte[] pdfBytes = generatePdf(sessionId, email);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(email);
        helper.setSubject("Votre rapport d'entretien — Smart Interview");
        helper.setText(
            "Bonjour,\n\n" +
            "Veuillez trouver ci-joint votre rapport d'entretien Smart Interview.\n\n" +
            "Bonne continuation !\n" +
            "L'équipe Smart Interview",
            false
        );
        helper.addAttachment("rapport_entretien.pdf",
                new ByteArrayResource(pdfBytes), "application/pdf");

        mailSender.send(message);

        // Mettre à jour le statut email en BDD
        reportRepository.findBySessionId(sessionId).ifPresent(report -> {
            report.setEmailSent(true);
            report.setSentAt(LocalDateTime.now());
            reportRepository.save(report);
        });
    }

    // Récupérer tous mes rapports
    public List<ReportResponse> getMyReports(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        return reportRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(r -> new ReportResponse(
                        r.getId(),
                        r.getSession().getId(),
                        r.getSession().getPoste(),
                        r.getEmailSent(),
                        r.getSentAt(),
                        r.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    // Télécharger un rapport depuis le disque
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
            // Si le fichier a été supprimé, on le régénère
            return generatePdf(report.getSession().getId(), email);
        }

        return Files.readAllBytes(filePath);
    }

    

    // Construction du PDF iText
    private byte[] buildPdf(User user, InterviewSession session,
                             List<InterviewQuestion> questions) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        document.add(new Paragraph("Smart Interview — Rapport d'entretien")
                .setFontSize(20).setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.DARK_GRAY));

        document.add(new Paragraph(" "));

        document.add(new Paragraph("Candidat : " + user.getNom()).setFontSize(12));
        document.add(new Paragraph("Email : " + user.getEmail()).setFontSize(12));
        document.add(new Paragraph("Date : " +
                session.getCreatedAt().format(formatter)).setFontSize(12));

        document.add(new Paragraph(" "));

        document.add(new Paragraph("Détails de l'entretien")
                .setFontSize(14).setBold());

        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .setWidth(UnitValue.createPercentValue(100));

        infoTable.addCell(cellGray("Poste"));
        infoTable.addCell(cellWhite(session.getPoste()));
        infoTable.addCell(cellGray("Type"));
        infoTable.addCell(cellWhite(session.getType()));
        infoTable.addCell(cellGray("Difficulté"));
        infoTable.addCell(cellWhite(session.getDifficulte()));
        infoTable.addCell(cellGray("Langue"));
        infoTable.addCell(cellWhite(session.getLangue()));
        infoTable.addCell(cellGray("Nb questions"));
        infoTable.addCell(cellWhite(String.valueOf(session.getNbQuestions())));
        infoTable.addCell(cellGray("Score global"));
        infoTable.addCell(cellWhite(
                session.getScoreGlobal() != null
                        ? session.getScoreGlobal() + " / 10"
                        : "Non évalué"
        ));

        document.add(infoTable);
        document.add(new Paragraph(" "));

        document.add(new Paragraph("Questions & Feedbacks")
                .setFontSize(14).setBold());
        document.add(new Paragraph(" "));

        for (int i = 0; i < questions.size(); i++) {
            InterviewQuestion q = questions.get(i);

            document.add(new Paragraph("Question " + (i + 1))
                    .setFontSize(12).setBold()
                    .setFontColor(ColorConstants.DARK_GRAY));

            document.add(new Paragraph(
                    q.getQuestion() != null ? q.getQuestion() : "—")
                    .setFontSize(11));

            document.add(new Paragraph("Réponse du candidat :")
                    .setFontSize(11).setBold());

            document.add(new Paragraph(
                    q.getReponse() != null ? q.getReponse() : "Aucune réponse")
                    .setFontSize(11).setItalic());

            document.add(new Paragraph("Feedback :")
                    .setFontSize(11).setBold());

            document.add(new Paragraph(
                    q.getFeedback() != null ? q.getFeedback() : "Aucun feedback")
                    .setFontSize(11).setFontColor(ColorConstants.DARK_GRAY));

            document.add(new Paragraph(" "));
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
}