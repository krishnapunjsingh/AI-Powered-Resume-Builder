package com.resumeai.jobmatch.controller;

import com.resumeai.jobmatch.dto.JobListingResponse;
import com.resumeai.jobmatch.dto.JobMatchAnalyzeRequest;
import com.resumeai.jobmatch.dto.JobMatchResponse;
import com.resumeai.jobmatch.dto.TailoringRecommendationResponse;
import com.resumeai.jobmatch.service.JobMatchService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/job-matches")
@RequiredArgsConstructor
public class JobMatchController {

    private final JobMatchService jobMatchService;

    @PostMapping("/analyze")
    @ResponseStatus(HttpStatus.CREATED)
    public JobMatchResponse analyzeJobFit(@Valid @RequestBody JobMatchAnalyzeRequest request) {
        return jobMatchService.analyzeJobFit(request);
    }

    @GetMapping("/{id}")
    public JobMatchResponse getMatchById(@PathVariable Long id) {
        return jobMatchService.getMatchById(id);
    }

    @GetMapping("/resume/{resumeId}")
    public List<JobMatchResponse> getMatchesByResume(@PathVariable Long resumeId) {
        return jobMatchService.getMatchesByResume(resumeId);
    }

    @GetMapping("/user/{userId}")
    public List<JobMatchResponse> getMatchesByUser(@PathVariable Long userId) {
        return jobMatchService.getMatchesByUser(userId);
    }

    @GetMapping("/top")
    public List<JobMatchResponse> getTopMatches(@RequestParam Long userId) {
        return jobMatchService.getTopMatches(userId);
    }

    @PostMapping("/{id}/bookmark")
    public JobMatchResponse bookmarkMatch(@PathVariable Long id) {
        return jobMatchService.bookmarkMatch(id);
    }

    @PostMapping("/fetch/linkedin")
    public List<JobListingResponse> fetchJobsFromLinkedIn(@RequestParam(required = false) String keywords,
                                                          @RequestParam(required = false) String location) {
        return jobMatchService.fetchJobsFromLinkedIn(keywords, location);
    }

    @PostMapping("/fetch/naukri")
    public List<JobListingResponse> fetchJobsFromNaukri(@RequestParam(required = false) String keywords,
                                                        @RequestParam(required = false) String location) {
        return jobMatchService.fetchJobsFromNaukri(keywords, location);
    }

    @GetMapping("/recommendations")
    public TailoringRecommendationResponse getTailoringRecommendations(@RequestParam String jobTitle,
                                                                       @RequestParam String jobDescription,
                                                                       @RequestParam String resumeContent) {
        return jobMatchService.getTailoringRecommendations(jobTitle, jobDescription, resumeContent);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMatch(@PathVariable Long id) {
        jobMatchService.deleteMatch(id);
    }
}