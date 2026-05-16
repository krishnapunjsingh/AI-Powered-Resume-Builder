package com.resumeai.ai.service;

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
import java.util.List;

public interface AiService {

    AiRequestResponse generateSummary(SummaryRequest request);

    AiRequestResponse generateBulletPoints(BulletPointsRequest request);

    AiRequestResponse generateCoverLetter(CoverLetterRequest request);

    AiRequestResponse improveSection(ImproveSectionRequest request);

    AtsReportResponse checkAtsCompatibility(AtsCheckRequest request);

    AiRequestResponse suggestSkills(SkillsSuggestionRequest request);

    AiRequestResponse tailorForJob(TailorResumeRequest request);

    AiRequestResponse translateResume(TranslateResumeRequest request);

    List<AiRequestResponse> getAiHistory(Long userId, Long resumeId);

    QuotaResponse getRemainingQuota(Long userId);

    Long getTotalUsageCount();

    Double getTotalCost();
}
