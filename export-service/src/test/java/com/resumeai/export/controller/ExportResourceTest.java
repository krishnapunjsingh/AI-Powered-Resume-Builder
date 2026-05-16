package com.resumeai.export.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeai.export.config.JwtProperties;
import com.resumeai.export.dto.ExportRequest;
import com.resumeai.export.dto.ExportResponse;
import com.resumeai.export.dto.ExportStatsResponse;
import com.resumeai.export.service.ExportService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ExportResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

        @Autowired
        private JwtProperties jwtProperties;

    @MockBean
    private ExportService exportService;

    private ExportResponse mockExportResponse;
    private ExportRequest mockExportRequest;
        private String validJwt;

    @BeforeEach
    void setUp() {
        mockExportRequest = ExportRequest.builder()
                .resumeId(1L)
                .format("PDF")
                .templateId(1L)
                .build();

        mockExportResponse = ExportResponse.builder()
                .id(1L)
                .jobId("test-job-id")
                .resumeId(1L)
                .format("PDF")
                .status("QUEUED")
                .requestedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        validJwt = Jwts.builder()
                .subject("test-user@example.com")
                .claim("userId", 1L)
                .claim("role", "ROLE_USER")
                .issuedAt(java.util.Date.from(ZonedDateTime.now().minusMinutes(1).toInstant()))
                .expiration(java.util.Date.from(ZonedDateTime.now().plusHours(1).toInstant()))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.secret())))
                .compact();
    }

    @Test
    void testSubmitExport_Success() throws Exception {
        when(exportService.submitExport(any(ExportRequest.class), anyLong()))
                .thenReturn(mockExportResponse);

        mockMvc.perform(post("/exports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockExportRequest))
                .header("Authorization", "Bearer " + validJwt))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.jobId").value("test-job-id"))
                .andExpect(jsonPath("$.format").value("PDF"));

        verify(exportService).submitExport(any(ExportRequest.class), anyLong());
    }

    @Test
    void testSubmitExport_MissingResumeId() throws Exception {
        ExportRequest invalidRequest = ExportRequest.builder()
                .format("PDF")
                .build();

        mockMvc.perform(post("/exports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .header("Authorization", "Bearer " + validJwt))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetJobStatus_Success() throws Exception {
        when(exportService.getJobStatus(anyString()))
                .thenReturn(mockExportResponse);

        mockMvc.perform(get("/exports/job/test-job-id")
                .header("Authorization", "Bearer " + validJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value("test-job-id"))
                .andExpect(jsonPath("$.status").value("QUEUED"));

        verify(exportService).getJobStatus("test-job-id");
    }

    @Test
    void testDownloadFile_Success() throws Exception {
        ExportResponse completedExport = ExportResponse.builder()
                .id(1L)
                .jobId("test-job-id")
                .format("PDF")
                .status("COMPLETED")
                .fileUrl("https://s3.example.com/file.pdf")
                .build();

        when(exportService.downloadFile(anyString()))
                .thenReturn(completedExport);

        mockMvc.perform(get("/exports/download/test-job-id")
                .header("Authorization", "Bearer " + validJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.fileUrl").exists());

        verify(exportService).downloadFile("test-job-id");
    }

    @Test
    void testGetExportsByUser_Success() throws Exception {
        when(exportService.getExportsByUser(anyLong()))
                .thenReturn(List.of(mockExportResponse));

        mockMvc.perform(get("/exports/user")
                .header("Authorization", "Bearer " + validJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].jobId").value("test-job-id"));

        verify(exportService).getExportsByUser(anyLong());
    }

    @Test
    void testGetExportsByResumeId_Success() throws Exception {
        when(exportService.getExportsByResumeId(anyLong()))
                .thenReturn(List.of(mockExportResponse));

        mockMvc.perform(get("/exports/resume/1")
                .header("Authorization", "Bearer " + validJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].resumeId").value(1));

        verify(exportService).getExportsByResumeId(1L);
    }

    @Test
    void testGetExportStats_Success() throws Exception {
        ExportStatsResponse stats = ExportStatsResponse.builder()
                .totalExports(10L)
                .totalByUser(5L)
                .completedCount(3L)
                .failedCount(1L)
                .queuedCount(1L)
                .pdfCount(4L)
                .docxCount(1L)
                .jsonCount(0L)
                .averageFileSize(256.5)
                .build();

        when(exportService.getExportStats(anyLong()))
                .thenReturn(stats);

        mockMvc.perform(get("/exports/stats")
                .header("Authorization", "Bearer " + validJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalExports").value(10))
                .andExpect(jsonPath("$.totalByUser").value(5));

        verify(exportService).getExportStats(anyLong());
    }

    @Test
    void testDeleteExport_Success() throws Exception {
        mockMvc.perform(delete("/exports/test-job-id")
                .header("Authorization", "Bearer " + validJwt))
                .andExpect(status().isNoContent());

        verify(exportService).deleteExport("test-job-id");
    }

    @Test
    void testSubmitExport_WithoutAuthorization() throws Exception {
        mockMvc.perform(post("/exports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockExportRequest)))
                .andExpect(status().isUnauthorized());
    }
}
