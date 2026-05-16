package com.resumeai.jobmatch.service;

import com.resumeai.jobmatch.dto.JobListingResponse;
import com.resumeai.jobmatch.dto.JobMatchAnalyzeRequest;
import com.resumeai.jobmatch.dto.JobMatchResponse;
import com.resumeai.jobmatch.dto.TailoringRecommendationResponse;
import java.util.List;

public interface JobMatchService {

    JobMatchResponse analyzeJobFit(JobMatchAnalyzeRequest request);

    List<JobMatchResponse> getMatchesByResume(Long resumeId);

    List<JobMatchResponse> getMatchesByUser(Long userId);

    JobMatchResponse getMatchById(Long id);

    JobMatchResponse bookmarkMatch(Long id);

    List<JobListingResponse> fetchJobsFromLinkedIn(String keywords, String location);

    List<JobListingResponse> fetchJobsFromNaukri(String keywords, String location);

    TailoringRecommendationResponse getTailoringRecommendations(String jobTitle, String jobDescription, String resumeContent);

    void deleteMatch(Long id);

    List<JobMatchResponse> getTopMatches(Long userId);
}