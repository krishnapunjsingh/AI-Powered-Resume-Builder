package com.resumeai.resume.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeai.resume.dto.ResumeDetailsResponse;
import com.resumeai.resume.dto.ResumeRequest;
import com.resumeai.resume.dto.ResumeResponse;
import com.resumeai.resume.dto.SectionSummary;
import com.resumeai.resume.service.ResumeService;
import com.resumeai.resume.security.JwtAuthenticationFilter;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ResumeController.class)
@AutoConfigureMockMvc(addFilters = false)
class ResumeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ResumeService resumeService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void createResumeReturnsCreatedResponse() throws Exception {
        ResumeRequest request = new ResumeRequest(7L, "Backend Engineer", "Builds APIs", "DRAFT");
        ResumeResponse response = resumeResponse(1L, 7L, "Backend Engineer", "Builds APIs", "DRAFT", false);

        when(resumeService.create(request)).thenReturn(response);

        mockMvc.perform(post("/resumes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isCreated())
                .andExpect(content().json(json(response)));

        verify(resumeService).create(request);
    }

    @Test
    void updateResumeReturnsUpdatedResponse() throws Exception {
        ResumeRequest request = new ResumeRequest(7L, "Updated Title", "Updated summary", "COMPLETE");
        ResumeResponse response = resumeResponse(1L, 7L, "Updated Title", "Updated summary", "COMPLETE", true);

        when(resumeService.update(1L, request)).thenReturn(response);

        mockMvc.perform(put("/resumes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(json(response)));

        verify(resumeService).update(1L, request);
    }

    @Test
    void getResumeByIdReturnsResponse() throws Exception {
        ResumeResponse response = resumeResponse(1L, 7L, "Backend Engineer", "Builds APIs", "DRAFT", false);

        when(resumeService.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/resumes/1"))
                .andExpect(status().isOk())
                .andExpect(content().json(json(response)));

        verify(resumeService).getById(1L);
    }

    @Test
    void getResumeWithSectionsReturnsDetails() throws Exception {
        ResumeResponse response = resumeResponse(1L, 7L, "Backend Engineer", "Builds APIs", "DRAFT", true);
        SectionSummary section = new SectionSummary(
                11L,
                1L,
                "Summary",
                "Short professional summary",
                1,
                LocalDateTime.of(2026, 4, 21, 10, 0),
                LocalDateTime.of(2026, 4, 21, 10, 15)
        );
        ResumeDetailsResponse details = new ResumeDetailsResponse(response, List.of(section));

        when(resumeService.getResumeWithSections(1L, "Bearer test-token")).thenReturn(details);

        mockMvc.perform(get("/resumes/1/details")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(content().json(json(details)));

        verify(resumeService).getResumeWithSections(1L, "Bearer test-token");
    }

    @Test
    void getResumesByUserIdReturnsList() throws Exception {
        ResumeResponse response = resumeResponse(1L, 7L, "Backend Engineer", "Builds APIs", "DRAFT", false);

        when(resumeService.getByUserId(7L)).thenReturn(List.of(response));

        mockMvc.perform(get("/resumes").param("userId", "7"))
                .andExpect(status().isOk())
                .andExpect(content().json(json(List.of(response))));

        verify(resumeService).getByUserId(7L);
    }

    @Test
    void duplicateResumeReturnsCreatedResponse() throws Exception {
        ResumeResponse response = resumeResponse(2L, 7L, "Copy of Backend Engineer", "Builds APIs", "DRAFT", false);

        when(resumeService.duplicate(1L)).thenReturn(response);

        mockMvc.perform(post("/resumes/1/duplicate"))
                .andExpect(status().isCreated())
                .andExpect(content().json(json(response)));

        verify(resumeService).duplicate(1L);
    }

    @Test
    void publishResumeReturnsUpdatedResponse() throws Exception {
        ResumeResponse response = resumeResponse(1L, 7L, "Backend Engineer", "Builds APIs", "DRAFT", true);

        when(resumeService.publish(1L)).thenReturn(response);

        mockMvc.perform(put("/resumes/1/publish"))
                .andExpect(status().isOk())
                .andExpect(content().json(json(response)));

        verify(resumeService).publish(1L);
    }

    @Test
    void unpublishResumeReturnsUpdatedResponse() throws Exception {
        ResumeResponse response = resumeResponse(1L, 7L, "Backend Engineer", "Builds APIs", "DRAFT", false);

        when(resumeService.unpublish(1L)).thenReturn(response);

        mockMvc.perform(put("/resumes/1/unpublish"))
                .andExpect(status().isOk())
                .andExpect(content().json(json(response)));

        verify(resumeService).unpublish(1L);
    }

    @Test
    void deleteResumeReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/resumes/1"))
                .andExpect(status().isNoContent());

        verify(resumeService).delete(1L);
    }

    @Test
    void createResumeWithInvalidPayloadReturnsBadRequest() throws Exception {
        ResumeRequest request = new ResumeRequest(7L, "", "Builds APIs", "DRAFT");

        mockMvc.perform(post("/resumes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("must not be blank"))
                .andExpect(jsonPath("$.path").value("/resumes"));

        verify(resumeService, never()).create(request);
    }

    private ResumeResponse resumeResponse(Long id,
                                          Long userId,
                                          String title,
                                          String summary,
                                          String status,
                                          boolean isPublic) {
        return new ResumeResponse(
                id,
                userId,
                title,
                summary,
                status,
                isPublic,
                LocalDateTime.of(2026, 4, 21, 10, 0),
                LocalDateTime.of(2026, 4, 21, 10, 15)
        );
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
