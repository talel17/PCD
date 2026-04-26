package com.smartinterview.backend.controller;

import com.smartinterview.backend.dto.CvResponse;
import com.smartinterview.backend.service.CvService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

import com.smartinterview.backend.repository.CvRepository;
import com.smartinterview.backend.repository.UserRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/cv")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class CvController {

    private final CvService cvService;
    private final CvRepository cvRepository;      
    private final UserRepository userRepository;
    @PostMapping("/upload")
    public ResponseEntity<CvResponse> upload(
            @RequestParam("file") MultipartFile file,
            Principal principal) throws IOException {

        CvResponse response = cvService.uploadCv(file, principal.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mine")
    public ResponseEntity<List<CvResponse>> getMyCvs(Principal principal) {
        return ResponseEntity.ok(cvService.getMyCvs(principal.getName()));
    }

    // Télécharger un CV par son id
@GetMapping("/{cvId}/download")
public ResponseEntity<byte[]> downloadCv(
        @PathVariable Long cvId,
        Principal principal) throws Exception {

    var cv = cvRepository.findById(cvId)
            .orElseThrow(() -> new RuntimeException("CV introuvable"));

    var user = userRepository.findByEmail(principal.getName())
            .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

    // Sécurité — un candidat ne peut télécharger que SES propres CVs
    if (!cv.getUser().getId().equals(user.getId())) {
        return ResponseEntity.status(403).build();
    }

    byte[] cvBytes = java.nio.file.Files.readAllBytes(
            java.nio.file.Paths.get(cv.getFilePath()));

    String originalName = cv.getFilePath()
            .substring(cv.getFilePath().lastIndexOf("_") + 1);

    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=" + originalName)
            .contentType(MediaType.APPLICATION_PDF)
            .body(cvBytes);
}
}