package com.resumeai.ai.controller;

import com.resumeai.ai.dto.AiRequestResponse;
import com.resumeai.ai.dto.AtsCheckRequest;
import com.resumeai.ai.dto.AtsReportResponse;
import com.resumeai.ai.dto.BulletPointsRequest;
import com.resumeai.ai.dto.CoverLetterRequest;
import com.resumeai.ai.dto.ImproveSectionRequest;
import com.resumeai.ai.dto.QuotaResponse;
import com.resumeai.ai.dto.SkillsSuggestionRequest;
import com.resumeai.ai.dto.SummaryRequest;
import com.resumeai.ai.dto.TailorResumeRequest;
import com.resumeai.ai.dto.TranslateResumeRequest;
import com.resumeai.ai.service.AiService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiResource {

    private final AiService aiService;

    @PostMapping({"/generate-summary", "/generateSummary"})
    public ResponseEntity<AiRequestResponse> generateSummary(@Valid @RequestBody SummaryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(aiService.generateSummary(request));
    }

    @PostMapping({"/generate-bullets", "/generateBullets"})
    public ResponseEntity<AiRequestResponse> generateBullets(@Valid @RequestBody BulletPointsRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(aiService.generateBulletPoints(request));
    }

    @PostMapping({"/generate-cover-letter", "/generateCoverLetter"})
    public ResponseEntity<AiRequestResponse> generateCoverLetter(@Valid @RequestBody CoverLetterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(aiService.generateCoverLetter(request));
    }

    @PostMapping({"/improve-section", "/improveSection"})
    public ResponseEntity<AiRequestResponse> improveSection(@Valid @RequestBody ImproveSectionRequest request) {
        return ResponseEntity.ok(aiService.improveSection(request));
    }

    @PostMapping({"/check-ats", "/checkAts"})
    public ResponseEntity<AtsReportResponse> checkAtsCompatibility(@Valid @RequestBody AtsCheckRequest request) {
        return ResponseEntity.ok(aiService.checkAtsCompatibility(request));
    }

    @PostMapping({"/suggest-skills", "/suggestSkills"})
    public ResponseEntity<AiRequestResponse> suggestSkills(@Valid @RequestBody SkillsSuggestionRequest request) {
        return ResponseEntity.ok(aiService.suggestSkills(request));
    }

    @PostMapping({"/tailor-for-job", "/tailorForJob"})
    public ResponseEntity<AiRequestResponse> tailorForJob(@Valid @RequestBody TailorResumeRequest request) {
        return ResponseEntity.ok(aiService.tailorForJob(request));
    }

    @PostMapping({"/translate", "/translateResume"})
    public ResponseEntity<AiRequestResponse> translateResume(@Valid @RequestBody TranslateResumeRequest request) {
        return ResponseEntity.ok(aiService.translateResume(request));
    }

    @GetMapping("/history")
    public ResponseEntity<List<AiRequestResponse>> getHistory(@RequestParam Long userId,
                                                              @RequestParam(required = false) Long resumeId) {
        return ResponseEntity.ok(aiService.getAiHistory(userId, resumeId));
    }

    @GetMapping("/quota")
    public ResponseEntity<QuotaResponse> getRemainingQuota(@RequestParam Long userId) {
        return ResponseEntity.ok(aiService.getRemainingQuota(userId));
    }

    @GetMapping("/admin/usage/count")
    public ResponseEntity<Long> getTotalUsageCount() {
        return ResponseEntity.ok(aiService.getTotalUsageCount());
    }

    @GetMapping("/admin/cost/total")
    public ResponseEntity<Double> getTotalCost() {
        return ResponseEntity.ok(aiService.getTotalCost());
    }
}
