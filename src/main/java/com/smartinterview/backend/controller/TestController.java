package com.smartinterview.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/test")
    public ResponseEntity<String> test(Principal principal) {
        return ResponseEntity.ok("Bonjour " + principal.getName() + " ! Tu es bien authentifié.");
    }
}