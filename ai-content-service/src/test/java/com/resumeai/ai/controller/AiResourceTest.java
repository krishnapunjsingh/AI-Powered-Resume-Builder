package com.resumeai.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeai.ai.dto.AiRequestResponse;
import com.resumeai.ai.dto.AtsReportResponse;
import com.resumeai.ai.dto.BulletPointsRequest;
import com.resumeai.ai.dto.CoverLetterRequest;
import com.resumeai.ai.dto.ImproveSectionRequest;
import com.resumeai.ai.dto.QuotaResponse;
import com.resumeai.ai.dto.SkillsSuggestionRequest;
import com.resumeai.ai.dto.SummaryRequest;
import com.resumeai.ai.dto.TailorResumeRequest;
import com.resumeai.ai.dto.TranslateResumeRequest;
import com.resumeai.ai.security.JwtAuthenticationFilter;
import com.resumeai.ai.service.AiService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AiResource.class)
@AutoConfigureMockMvc(addFilters = false)
class AiResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AiService aiService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void generateSummaryReturnsCreatedResponse() throws Exception {
        SummaryRequest request = new SummaryRequest(1L, 10L, "Backend Engineer", "Built APIs for 3 years.", "Platform Engineer", "GPT-4o");
        AiRequestResponse response = requestResponse("req-1", 1L, 10L, "SUMMARY", "GPT-4o", "Summary text");

        when(aiService.generateSummary(request)).thenReturn(response);

        mockMvc.perform(post("/ai/generate-summary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isCreated())
                .andExpect(content().json(json(response)));

        verify(aiService).generateSummary(request);
    }

    @Test
    void generateBulletsReturnsCreatedResponse() throws Exception {
        BulletPointsRequest request = new BulletPointsRequest(1L, 10L, "Experience", "Led migrations and built APIs.", "GPT-4o");
        AiRequestResponse response = requestResponse("req-2", 1L, 10L, "BULLETS", "GPT-4o", "- Led migrations");

        when(aiService.generateBulletPoints(request)).thenReturn(response);

        mockMvc.perform(post("/ai/generate-bullets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isCreated())
                .andExpect(content().json(json(response)));

        verify(aiService).generateBulletPoints(request);
    }

    @Test
    void generateCoverLetterReturnsCreatedResponse() throws Exception {
        CoverLetterRequest request = new CoverLetterRequest(1L, 10L, "Ava Smith", "BridgeLab", "Java Engineer", "Built scalable services.", "CLAUDE");
        AiRequestResponse response = requestResponse("req-3", 1L, 10L, "COVER_LETTER", "CLAUDE", "Dear Hiring Team...");

        when(aiService.generateCoverLetter(request)).thenReturn(response);

        mockMvc.perform(post("/ai/generate-cover-letter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isCreated())
                .andExpect(content().json(json(response)));

        verify(aiService).generateCoverLetter(request);
    }

    @Test
    void improveSectionReturnsOkResponse() throws Exception {
        ImproveSectionRequest request = new ImproveSectionRequest(1L, 10L, "Summary", "short summary", "Make it more results-focused", "GPT-4o");
        AiRequestResponse response = requestResponse("req-4", 1L, 10L, "IMPROVE", "GPT-4o", "Improved summary");

        when(aiService.improveSection(request)).thenReturn(response);

        mockMvc.perform(post("/ai/improve-section")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(json(response)));

        verify(aiService).improveSection(request);
    }

    @Test
    void checkAtsReturnsReportResponse() throws Exception {
        var request = new com.resumeai.ai.dto.AtsCheckRequest(1L, 10L, "Java Spring Boot APIs", "Need Java and Spring Boot", "GPT-4o");
        AtsReportResponse response = new AtsReportResponse(
                "req-5",
                1L,
                10L,
                "ATS",
                "GPT-4o",
                50,
                "COMPLETED",
                90,
                List.of("java", "spring"),
                List.of("boot"),
                List.of("Add more keywords"),
                LocalDateTime.of(2026, 4, 21, 12, 0),
                LocalDateTime.of(2026, 4, 21, 12, 1)
        );

        when(aiService.checkAtsCompatibility(request)).thenReturn(response);

        mockMvc.perform(post("/ai/check-ats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(json(response)));

        verify(aiService).checkAtsCompatibility(request);
    }

    @Test
    void suggestSkillsReturnsOkResponse() throws Exception {
        SkillsSuggestionRequest request = new SkillsSuggestionRequest(1L, 10L, "Java Spring", "Need Spring Boot and SQL", List.of("Java"), "GPT-4o");
        AiRequestResponse response = requestResponse("req-6", 1L, 10L, "SKILLS", "GPT-4o", "Spring Boot\nSQL");

        when(aiService.suggestSkills(request)).thenReturn(response);

        mockMvc.perform(post("/ai/suggest-skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(json(response)));

        verify(aiService).suggestSkills(request);
    }

    @Test
    void tailorForJobReturnsOkResponse() throws Exception {
        TailorResumeRequest request = new TailorResumeRequest(1L, 10L, "{\"summary\":\"Engineer\"}", "Need Java and Spring", "GPT-4o");
        AiRequestResponse response = requestResponse("req-7", 1L, 10L, "TAILOR", "GPT-4o", "{\"tailoredSummary\":\"...\"}");

        when(aiService.tailorForJob(request)).thenReturn(response);

        mockMvc.perform(post("/ai/tailor-for-job")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(json(response)));

        verify(aiService).tailorForJob(request);
    }

    @Test
    void translateResumeReturnsOkResponse() throws Exception {
        TranslateResumeRequest request = new TranslateResumeRequest(1L, 10L, "Experience with Java", "Spanish", "GPT-4o");
        AiRequestResponse response = requestResponse("req-8", 1L, 10L, "TRANSLATE", "GPT-4o", "Experiencia con Java");

        when(aiService.translateResume(request)).thenReturn(response);

        mockMvc.perform(post("/ai/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(json(response)));

        verify(aiService).translateResume(request);
    }

    @Test
    void historyReturnsList() throws Exception {
        AiRequestResponse response = requestResponse("req-9", 1L, 10L, "SUMMARY", "GPT-4o", "Summary text");
        when(aiService.getAiHistory(1L, 10L)).thenReturn(List.of(response));

        mockMvc.perform(get("/ai/history").param("userId", "1").param("resumeId", "10"))
                .andExpect(status().isOk())
                .andExpect(content().json(json(List.of(response))));

        verify(aiService).getAiHistory(1L, 10L);
    }

    @Test
    void quotaReturnsQuotaResponse() throws Exception {
        QuotaResponse response = new QuotaResponse(1L, 30, 4, 26, 120);
        when(aiService.getRemainingQuota(1L)).thenReturn(response);

        mockMvc.perform(get("/ai/quota").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json(json(response)));

        verify(aiService).getRemainingQuota(1L);
    }

    @Test
    void summaryValidationFailsForBlankText() throws Exception {
        SummaryRequest request = new SummaryRequest(1L, 10L, "Backend Engineer", "", null, null);

        mockMvc.perform(post("/ai/generate-summary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));

        verify(aiService, never()).generateSummary(request);
    }

    private AiRequestResponse requestResponse(String requestId,
                                              Long userId,
                                              Long resumeId,
                                              String requestType,
                                              String model,
                                              String aiResponse) {
        return new AiRequestResponse(
                requestId,
                userId,
                resumeId,
                requestType,
                model,
                42,
                "COMPLETED",
                aiResponse,
                LocalDateTime.of(2026, 4, 21, 12, 0),
                LocalDateTime.of(2026, 4, 21, 12, 1)
        );
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
