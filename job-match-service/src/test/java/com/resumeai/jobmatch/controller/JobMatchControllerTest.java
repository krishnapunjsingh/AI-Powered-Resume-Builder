package com.resumeai.jobmatch.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeai.jobmatch.dto.JobListingResponse;
import com.resumeai.jobmatch.dto.JobMatchAnalyzeRequest;
import com.resumeai.jobmatch.dto.JobMatchResponse;
import com.resumeai.jobmatch.dto.TailoringRecommendationResponse;
import com.resumeai.jobmatch.security.JwtAuthenticationFilter;
import com.resumeai.jobmatch.service.JobMatchService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = JobMatchController.class)
@AutoConfigureMockMvc(addFilters = false)
class JobMatchControllerTest {

        private static final String JOB_TITLE = "Java Backend Engineer";
        private static final String JOB_DESCRIPTION = "Need Spring and Docker";
        private static final String RESUME_CONTENT = "Spring Boot developer";
        private static final String SOURCE_MANUAL = "MANUAL";
        private static final String LOCATION_REMOTE = "remote";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JobMatchService jobMatchService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void analyzeJobFitReturnsCreatedResponse() throws Exception {
        JobMatchAnalyzeRequest request = new JobMatchAnalyzeRequest(
                8L,
                21L,
                JOB_TITLE,
                "Looking for Spring Boot, SQL, and Docker.",
                "Built Java APIs with Spring Boot and SQL.",
                SOURCE_MANUAL,
                "Remote",
                "spring boot java"
        );
        JobMatchResponse response = response(1L, 21L, 8L, JOB_TITLE, 78, false);

        when(jobMatchService.analyzeJobFit(request)).thenReturn(response);

        mockMvc.perform(post("/job-matches/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isCreated())
                .andExpect(content().json(json(response)));

        verify(jobMatchService).analyzeJobFit(request);
    }

    @Test
    void getMatchByIdReturnsResponse() throws Exception {
                JobMatchResponse response = response(1L, 21L, 8L, JOB_TITLE, 78, true);
        when(jobMatchService.getMatchById(1L)).thenReturn(response);

        mockMvc.perform(get("/job-matches/1"))
                .andExpect(status().isOk())
                .andExpect(content().json(json(response)));

        verify(jobMatchService).getMatchById(1L);
    }

    @Test
    void getMatchesByResumeReturnsList() throws Exception {
                JobMatchResponse response = response(1L, 21L, 8L, JOB_TITLE, 78, false);
        when(jobMatchService.getMatchesByResume(21L)).thenReturn(List.of(response));

        mockMvc.perform(get("/job-matches/resume/21"))
                .andExpect(status().isOk())
                .andExpect(content().json(json(List.of(response))));

        verify(jobMatchService).getMatchesByResume(21L);
    }

    @Test
    void getMatchesByUserReturnsList() throws Exception {
                JobMatchResponse response = response(1L, 21L, 8L, JOB_TITLE, 78, false);
        when(jobMatchService.getMatchesByUser(8L)).thenReturn(List.of(response));

        mockMvc.perform(get("/job-matches/user/8"))
                .andExpect(status().isOk())
                .andExpect(content().json(json(List.of(response))));

        verify(jobMatchService).getMatchesByUser(8L);
    }

    @Test
    void bookmarkMatchReturnsUpdatedResponse() throws Exception {
                JobMatchResponse response = response(1L, 21L, 8L, JOB_TITLE, 78, true);
        when(jobMatchService.bookmarkMatch(1L)).thenReturn(response);

        mockMvc.perform(post("/job-matches/1/bookmark"))
                .andExpect(status().isOk())
                .andExpect(content().json(json(response)));

        verify(jobMatchService).bookmarkMatch(1L);
    }

    @Test
    void fetchLinkedInReturnsListings() throws Exception {
        JobListingResponse listing = new JobListingResponse("Java Engineer", "BridgeLab", "Remote", "https://example.com", "LINKEDIN");
        when(jobMatchService.fetchJobsFromLinkedIn("java", LOCATION_REMOTE)).thenReturn(List.of(listing));

        mockMvc.perform(post("/job-matches/fetch/linkedin")
                        .param("keywords", "java")
                        .param("location", LOCATION_REMOTE))
                .andExpect(status().isOk())
                .andExpect(content().json(json(List.of(listing))));

        verify(jobMatchService).fetchJobsFromLinkedIn("java", LOCATION_REMOTE);
    }

    @Test
    void recommendationsReturnPayload() throws Exception {
        TailoringRecommendationResponse response = new TailoringRecommendationResponse(
                JOB_TITLE,
                "Highlight Docker and SQL",
                List.of("docker", "sql"),
                72
        );

        when(jobMatchService.getTailoringRecommendations(JOB_TITLE, JOB_DESCRIPTION, RESUME_CONTENT))
                .thenReturn(response);

        mockMvc.perform(get("/job-matches/recommendations")
                        .param("jobTitle", JOB_TITLE)
                        .param("jobDescription", JOB_DESCRIPTION)
                        .param("resumeContent", RESUME_CONTENT))
                .andExpect(status().isOk())
                .andExpect(content().json(json(response)));

        verify(jobMatchService).getTailoringRecommendations(JOB_TITLE, JOB_DESCRIPTION, RESUME_CONTENT);
    }

    @Test
    void deleteMatchReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/job-matches/1"))
                .andExpect(status().isNoContent());

        verify(jobMatchService).deleteMatch(1L);
    }

    @Test
    void analyzeWithInvalidPayloadReturnsBadRequest() throws Exception {
        JobMatchAnalyzeRequest request = new JobMatchAnalyzeRequest(
                8L,
                21L,
                "",
                "Looking for Spring Boot, SQL, and Docker.",
                "Built Java APIs with Spring Boot and SQL.",
                SOURCE_MANUAL,
                "Remote",
                "spring boot java"
        );

        mockMvc.perform(post("/job-matches/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest());

        verify(jobMatchService, never()).analyzeJobFit(request);
    }

    private JobMatchResponse response(Long id,
                                      Long resumeId,
                                      Long userId,
                                      String jobTitle,
                                      Integer matchScore,
                                      boolean bookmarked) {
        return new JobMatchResponse(
                id,
                resumeId,
                userId,
                jobTitle,
                "Job description",
                matchScore,
                "sql, docker",
                "Tailor the summary and recent experience.",
                "MANUAL",
                bookmarked,
                LocalDateTime.of(2026, 4, 21, 12, 0)
        );
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}