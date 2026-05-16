package com.resumeai.auth.controller;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.resumeai.auth.dto.AdminStatsResponse;
import com.resumeai.auth.dto.UserResponse;
import com.resumeai.auth.service.AdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    @PutMapping("/users/{id}/suspend")
    public ResponseEntity<UserResponse> suspendUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.suspendUser(id));
    }

    @PutMapping("/users/{id}/reactivate")
    public ResponseEntity<UserResponse> reactivateUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.reactivateUser(id));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable Long id, 
            @RequestBody @Valid RoleUpdateRequest request) {
        return ResponseEntity.ok(adminService.updateUserRole(id, request.role()));
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getAdminStats() {
        return ResponseEntity.ok(adminService.getAdminStats());
    }

    // Template endpoints
    @GetMapping("/templates")
    public ResponseEntity<List<AdminService.TemplateResponse>> getAllTemplates() {
        return ResponseEntity.ok(adminService.getAllTemplates());
    }

    @PostMapping("/templates")
    public ResponseEntity<AdminService.TemplateResponse> createTemplate(@RequestBody @Valid AdminService.TemplateRequest request) {
        return ResponseEntity.ok(adminService.createTemplate(request));
    }

    @PutMapping("/templates/{id}")
    public ResponseEntity<AdminService.TemplateResponse> updateTemplate(@PathVariable Long id, @RequestBody @Valid AdminService.TemplateRequest request) {
        return ResponseEntity.ok(adminService.updateTemplate(id, request));
    }

    @DeleteMapping("/templates/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        adminService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }

    // Analytics endpoints
    @GetMapping("/analytics/platform")
    public ResponseEntity<AdminService.PlatformStatsResponse> getPlatformStats() {
        return ResponseEntity.ok(adminService.getPlatformStats());
    }

    @GetMapping("/analytics/ai-usage")
    public ResponseEntity<AdminService.AiUsageStatsResponse> getAiUsageStats() {
        return ResponseEntity.ok(adminService.getAiUsageStats());
    }

    @GetMapping("/analytics/export-stats")
    public ResponseEntity<AdminService.ExportStatsResponse> getExportStats() {
        return ResponseEntity.ok(adminService.getExportStats());
    }

    @GetMapping("/analytics/system-health")
    public ResponseEntity<AdminService.SystemHealthResponse> getSystemHealth() {
        return ResponseEntity.ok(adminService.getSystemHealth());
    }

    // Notification endpoint
    @PostMapping("/notifications/broadcast")
    public ResponseEntity<Void> sendBroadcastNotification(@RequestBody AdminService.BroadcastNotificationRequest request) {
        adminService.sendBroadcastNotification(request);
        return ResponseEntity.ok().build();
    }

    // DTOs
    record RoleUpdateRequest(String role) {}
}
