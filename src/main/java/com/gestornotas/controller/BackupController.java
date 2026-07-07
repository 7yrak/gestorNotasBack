package com.gestornotas.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.gestornotas.service.BackupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/backups")
public class BackupController {
    private final BackupService backupService;

    public BackupController(BackupService backupService) {
        this.backupService = backupService;
    }

    @PostMapping("/import/{userId}")
    public ResponseEntity<BackupService.ImportResult> importBackup(
            @PathVariable UUID userId,
            @RequestBody JsonNode backup) {
        return ResponseEntity.ok(backupService.importBackup(userId, backup));
    }
}
