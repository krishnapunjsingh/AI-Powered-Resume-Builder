package com.resumeai.template.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeai.template.dto.TemplateRequest;
import com.resumeai.template.dto.TemplateResponse;
import com.resumeai.template.security.JwtAuthenticationFilter;
import com.resumeai.template.service.TemplateService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TemplateResource.class)
@AutoConfigureMockMvc(addFilters = false)
class TemplateResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TemplateService templateService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void createTemplateReturnsCreatedResponse() throws Exception {
        TemplateRequest request = templateRequest();
        TemplateResponse response = templateResponse(1);

        when(templateService.createTemplate(request)).thenReturn(response);

        mockMvc.perform(post("/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isCreated())
                .andExpect(content().json(json(response)));

        verify(templateService).createTemplate(request);
    }

    @Test
    void getTemplateByIdReturnsResponse() throws Exception {
        TemplateResponse response = templateResponse(1);
        when(templateService.getTemplateById(1)).thenReturn(response);

        mockMvc.perform(get("/templates/1"))
                .andExpect(status().isOk())
                .andExpect(content().json(json(response)));

        verify(templateService).getTemplateById(1);
    }

    @Test
    void getAllTemplatesReturnsResponse() throws Exception {
        TemplateResponse response = templateResponse(1);
        when(templateService.getAllTemplates()).thenReturn(List.of(response));

        mockMvc.perform(get("/templates"))
                .andExpect(status().isOk())
                .andExpect(content().json(json(List.of(response))));

        verify(templateService).getAllTemplates();
    }

    @Test
    void getFreeTemplatesReturnsResponse() throws Exception {
        TemplateResponse response = templateResponse(1);
        when(templateService.getFreeTemplates()).thenReturn(List.of(response));

        mockMvc.perform(get("/templates/free"))
                .andExpect(status().isOk())
                .andExpect(content().json(json(List.of(response))));

        verify(templateService).getFreeTemplates();
    }

    @Test
    void getPremiumTemplatesReturnsResponse() throws Exception {
        TemplateResponse response = templateResponse(2);
        when(templateService.getPremiumTemplates()).thenReturn(List.of(response));

        mockMvc.perform(get("/templates/premium"))
                .andExpect(status().isOk())
                .andExpect(content().json(json(List.of(response))));

        verify(templateService).getPremiumTemplates();
    }

    @Test
    void getByCategoryReturnsResponse() throws Exception {
        TemplateResponse response = templateResponse(1);
        when(templateService.getByCategory("MODERN")).thenReturn(List.of(response));

        mockMvc.perform(get("/templates/category/MODERN"))
                .andExpect(status().isOk())
                .andExpect(content().json(json(List.of(response))));

        verify(templateService).getByCategory("MODERN");
    }

    @Test
    void getPopularReturnsResponse() throws Exception {
        TemplateResponse response = templateResponse(1);
        when(templateService.getPopularTemplates()).thenReturn(List.of(response));

        mockMvc.perform(get("/templates/popular"))
                .andExpect(status().isOk())
                .andExpect(content().json(json(List.of(response))));

        verify(templateService).getPopularTemplates();
    }

    @Test
    void updateTemplateReturnsResponse() throws Exception {
        TemplateRequest request = templateRequest();
        TemplateResponse response = templateResponse(1);
        when(templateService.updateTemplate(1, request)).thenReturn(response);

        mockMvc.perform(put("/templates/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(json(response)));

        verify(templateService).updateTemplate(1, request);
    }

    @Test
    void deactivateTemplateReturnsNoContent() throws Exception {
        mockMvc.perform(put("/templates/1/deactivate"))
                .andExpect(status().isNoContent());

        verify(templateService).deactivateTemplate(1);
    }

    @Test
    void incrementUsageReturnsNoContent() throws Exception {
        mockMvc.perform(put("/templates/1/usage/increment"))
                .andExpect(status().isNoContent());

        verify(templateService).incrementUsage(1);
    }

    @Test
    void createTemplateWithInvalidPayloadReturnsBadRequest() throws Exception {
        TemplateRequest request = new TemplateRequest(
                "",
                "desc",
                "thumb",
                "<html></html>",
                "css",
                "MODERN",
                true,
                true
        );

        mockMvc.perform(post("/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest());

        verify(templateService, never()).createTemplate(request);
    }

    private TemplateRequest templateRequest() {
        return new TemplateRequest(
                "Modern Blue",
                "Clean modern layout",
                "https://example.com/thumb.png",
                "<div>resume</div>",
                ".root{color:#123456;}",
                "MODERN",
                false,
                true
        );
    }

    private TemplateResponse templateResponse(int id) {
        return new TemplateResponse(
                id,
                "Modern Blue",
                "Clean modern layout",
                "https://example.com/thumb.png",
                "<div>resume</div>",
                ".root{color:#123456;}",
                "MODERN",
                false,
                true,
                7,
                LocalDateTime.of(2026, 4, 21, 11, 0),
                LocalDateTime.of(2026, 4, 21, 11, 10)
        );
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}