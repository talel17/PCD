package com.smartinterview.backend.controller;

import com.smartinterview.backend.dto.SessionConfigRequest;
import com.smartinterview.backend.dto.SessionResponse;
import com.smartinterview.backend.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class InterviewController {

    private final InterviewService interviewService;

    // Créer une nouvelle session
    @PostMapping("/configure")
    public ResponseEntity<SessionResponse> configure(
            @Valid @RequestBody SessionConfigRequest request,
            Principal principal) {
        return ResponseEntity.ok(
            interviewService.createSession(request, principal.getName())
        );
    }

    // Mes sessions
    @GetMapping
    public ResponseEntity<List<SessionResponse>> getMySessions(Principal principal) {
        return ResponseEntity.ok(
            interviewService.getMySessions(principal.getName())
        );
    }

    // Détail d'une session
    @GetMapping("/{id}")
    public ResponseEntity<SessionResponse> getSession(
            @PathVariable Long id,
            Principal principal) {
        return ResponseEntity.ok(
            interviewService.getSessionById(id, principal.getName())
        );
    }


    // Endpoint public pour l'agent Python
    @GetMapping("/{id}/public")
    public ResponseEntity<SessionResponse> getSessionPublic(@PathVariable Long id) {
        return ResponseEntity.ok(
                interviewService.getSessionPublic(id)
            );
    }

}