package com.resumeai.section.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeai.section.dto.SectionRequest;
import com.resumeai.section.dto.SectionResponse;
import com.resumeai.section.security.JwtAuthenticationFilter;
import com.resumeai.section.service.SectionService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SectionController.class)
@AutoConfigureMockMvc(addFilters = false)
class SectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SectionService sectionService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void createSectionReturnsCreatedResponse() throws Exception {
        SectionRequest request = new SectionRequest(10L, "SUMMARY", "Seasoned engineer", 1);
        SectionResponse response = sectionResponse(1L, 10L, "SUMMARY", "Seasoned engineer", 1);

        when(sectionService.create(request)).thenReturn(response);

        mockMvc.perform(post("/sections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isCreated())
                .andExpect(content().json(json(response)));

        verify(sectionService).create(request);
    }

    @Test
    void updateSectionReturnsUpdatedResponse() throws Exception {
        SectionRequest request = new SectionRequest(10L, "EXPERIENCE", "Updated content", 2);
        SectionResponse response = sectionResponse(1L, 10L, "EXPERIENCE", "Updated content", 2);

        when(sectionService.update(1L, request)).thenReturn(response);

        mockMvc.perform(put("/sections/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(json(response)));

        verify(sectionService).update(1L, request);
    }

    @Test
    void getSectionsByResumeReturnsList() throws Exception {
        SectionResponse response = sectionResponse(1L, 10L, "SUMMARY", "Seasoned engineer", 1);

        when(sectionService.findByResumeId(10L)).thenReturn(List.of(response));

        mockMvc.perform(get("/sections").param("resumeId", "10"))
                .andExpect(status().isOk())
                .andExpect(content().json(json(List.of(response))));

        verify(sectionService).findByResumeId(10L);
    }

    @Test
    void deleteSectionReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/sections/1"))
                .andExpect(status().isNoContent());

        verify(sectionService).delete(1L);
    }

    @Test
    void createSectionWithInvalidPayloadReturnsBadRequest() throws Exception {
        SectionRequest request = new SectionRequest(10L, "", "Seasoned engineer", 1);

        mockMvc.perform(post("/sections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest());

        verify(sectionService, never()).create(request);
    }

    private SectionResponse sectionResponse(Long id,
                                            Long resumeId,
                                            String sectionName,
                                            String content,
                                            Integer displayOrder) {
        return new SectionResponse(
                id,
                resumeId,
                sectionName,
                content,
                displayOrder,
                LocalDateTime.of(2026, 4, 21, 11, 0),
                LocalDateTime.of(2026, 4, 21, 11, 10)
        );
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
