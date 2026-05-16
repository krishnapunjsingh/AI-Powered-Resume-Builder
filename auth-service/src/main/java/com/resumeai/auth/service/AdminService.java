package com.resumeai.auth.service;

import com.resumeai.auth.dto.UserResponse;
import com.resumeai.auth.dto.AdminStatsResponse;

import java.util.List;

public interface AdminService {
    List<UserResponse> getAllUsers();
    UserResponse getUserById(Long id);
    UserResponse suspendUser(Long id);
    UserResponse reactivateUser(Long id);
    void deleteUser(Long id);
    UserResponse updateUserRole(Long id, String role);
    AdminStatsResponse getAdminStats();
    
    // Template methods
    List<TemplateResponse> getAllTemplates();
    TemplateResponse createTemplate(TemplateRequest request);
    TemplateResponse updateTemplate(Long id, TemplateRequest request);
    void deleteTemplate(Long id);
    
    // Analytics methods
    PlatformStatsResponse getPlatformStats();
    AiUsageStatsResponse getAiUsageStats();
    ExportStatsResponse getExportStats();
    SystemHealthResponse getSystemHealth();
    
    // Notification method
    void sendBroadcastNotification(BroadcastNotificationRequest request);
    
    // DTOs
    record TemplateRequest(String name, String category, String description, List<String> features) {}
    record TemplateResponse(Long id, String name, String category, String description, List<String> features) {}
    record PlatformStatsResponse(Long totalUsers, Long totalResumes, Long totalTemplates, Long recentActivity, Long activeUsers, Long suspendedUsers, Long usersThisWeek, Long resumesThisWeek, Long templatesThisMonth) {}
    record AiUsageStatsResponse(Double totalCost, Long totalTokens, Long gpt4oTokens, Long claudeTokens) {}
    record ExportStatsResponse(Long pdfDownloads, Long wordExports, Long htmlDownloads) {}
    record SystemHealthResponse(Integer apiResponseTime, Double uptime, Double errorRate) {}
    record BroadcastNotificationRequest(String message, String type, String targetAudience) {}
}
