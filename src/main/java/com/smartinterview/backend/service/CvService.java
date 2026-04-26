package com.smartinterview.backend.service;

import com.smartinterview.backend.dto.CvResponse;
import com.smartinterview.backend.entity.Cv;
import com.smartinterview.backend.entity.User;
import com.smartinterview.backend.repository.CvRepository;
import com.smartinterview.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CvService {

    private final CvRepository cvRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    public CvResponse uploadCv(MultipartFile file, String email) throws IOException {

        if (!file.getContentType().equals("application/pdf")) {
            throw new RuntimeException("Seuls les fichiers PDF sont acceptés");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalName = file.getOriginalFilename();
        String uniqueName = UUID.randomUUID() + "_" + originalName;
        Path filePath = uploadPath.resolve(uniqueName);

        Files.copy(file.getInputStream(), filePath);

        Cv cv = new Cv();
        cv.setUser(user);
        cv.setFilePath(filePath.toString());
        cvRepository.save(cv);

        return new CvResponse(
            cv.getId(),
            originalName,
            cv.getFilePath(),
            cv.getUploadedAt()
        );
    }

    public List<CvResponse> getMyCvs(String email) {
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

    return cvRepository.findByUserOrderByUploadedAtDesc(user)
            .stream()
            .map(cv -> {
                String fullName = Paths.get(cv.getFilePath()).getFileName().toString();
                // Supprimer l'UUID du début : "uuid_nomfichier.pdf" → "nomfichier.pdf"
                String originalName = fullName.contains("_")
                        ? fullName.substring(fullName.indexOf("_") + 1)
                        : fullName;
                return new CvResponse(
                        cv.getId(),
                        originalName,
                        cv.getFilePath(),
                        cv.getUploadedAt()
                );
            })
            .collect(Collectors.toList());
}
}