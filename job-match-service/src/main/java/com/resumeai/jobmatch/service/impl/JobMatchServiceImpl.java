package com.resumeai.jobmatch.service.impl;

import com.resumeai.jobmatch.dto.JobMatchAnalyzeRequest;
import com.resumeai.jobmatch.dto.JobListingResponse;
import com.resumeai.jobmatch.dto.JobMatchResponse;
import com.resumeai.jobmatch.dto.TailoringRecommendationResponse;
import com.resumeai.jobmatch.entity.JobMatch;
import com.resumeai.jobmatch.exception.JobMatchNotFoundException;
import com.resumeai.jobmatch.repository.JobMatchRepository;
import com.resumeai.jobmatch.service.JobMatchService;
import com.resumeai.jobmatch.service.external.ExternalJobPortalClient;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JobMatchServiceImpl implements JobMatchService {

    private static final List<String> SKILL_CATALOG = List.of(
            "java",
            "spring boot",
            "spring",
            "microservices",
            "rest",
            "sql",
            "mysql",
            "postgresql",
            "hibernate",
            "jpa",
            "jwt",
            "security",
            "docker",
            "kubernetes",
            "aws",
            "azure",
            "git",
            "maven",
            "gradle",
            "kafka",
            "rabbitmq",
            "redis",
            "testing",
            "junit",
            "communication",
            "leadership",
            "javascript",
            "typescript",
            "react",
            "angular",
            "python"
    );

    private static final Set<String> STOP_WORDS = Set.of(
            "and",
            "the",
            "with",
            "for",
            "from",
            "that",
            "this",
            "into",
            "your",
            "our",
            "you",
            "are",
            "will",
            "have",
            "has",
            "job",
            "role",
            "team",
            "work"
    );

    private final JobMatchRepository jobMatchRepository;
    private final ExternalJobPortalClient externalJobPortalClient;

    @Override
    @Transactional
    public JobMatchResponse analyzeJobFit(JobMatchAnalyzeRequest request) {
        JobAnalysis analysis = analyzeText(request.jobTitle(), request.jobDescription(), request.resumeContent());

        JobMatch jobMatch = JobMatch.builder()
                .resumeId(request.resumeId())
                .userId(request.userId())
                .jobTitle(request.jobTitle())
                .jobDescription(request.jobDescription())
                .matchScore(analysis.matchScore())
                .missingSkills(String.join(", ", analysis.missingSkills()))
                .recommendations(buildRecommendations(request.jobTitle(), analysis))
                .source(normalizeSource(request.source()))
                .bookmarked(false)
                .build();

        return mapToResponse(jobMatchRepository.save(jobMatch));
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobMatchResponse> getMatchesByResume(Long resumeId) {
        return jobMatchRepository.findByResumeIdOrderByMatchScoreDescMatchedAtDesc(resumeId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobMatchResponse> getMatchesByUser(Long userId) {
        return jobMatchRepository.findByUserIdOrderByMatchScoreDescMatchedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public JobMatchResponse getMatchById(Long id) {
        return mapToResponse(findMatch(id));
    }

    @Override
    @Transactional
    public JobMatchResponse bookmarkMatch(Long id) {
        JobMatch jobMatch = findMatch(id);
        jobMatch.setBookmarked(true);
        return mapToResponse(jobMatchRepository.save(jobMatch));
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobListingResponse> fetchJobsFromLinkedIn(String keywords, String location) {
        return externalJobPortalClient.fetchLinkedInJobs(keywords, location);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobListingResponse> fetchJobsFromNaukri(String keywords, String location) {
        return externalJobPortalClient.fetchNaukriJobs(keywords, location);
    }

    @Override
    @Transactional(readOnly = true)
    public TailoringRecommendationResponse getTailoringRecommendations(String jobTitle,
                                                                       String jobDescription,
                                                                       String resumeContent) {
        JobAnalysis analysis = analyzeText(jobTitle, jobDescription, resumeContent);
        return new TailoringRecommendationResponse(
                jobTitle,
                buildRecommendations(jobTitle, analysis),
                analysis.missingSkills(),
                analysis.matchScore()
        );
    }

    @Override
    @Transactional
    public void deleteMatch(Long id) {
        if (!jobMatchRepository.existsById(id)) {
            throw new JobMatchNotFoundException(id);
        }
        jobMatchRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobMatchResponse> getTopMatches(Long userId) {
        return jobMatchRepository.findByUserIdOrderByMatchScoreDescMatchedAtDesc(userId)
                .stream()
                .limit(5)
                .map(this::mapToResponse)
                .toList();
    }

    private JobMatch findMatch(Long id) {
        return jobMatchRepository.findByMatchId(id)
                .orElseThrow(() -> new JobMatchNotFoundException(id));
    }

    private String normalizeSource(String source) {
        if (source == null || source.isBlank()) {
            return "MANUAL";
        }
        return source.trim().toUpperCase(Locale.ROOT);
    }

    private JobAnalysis analyzeText(String jobTitle, String jobDescription, String resumeContent) {
        String combinedJobText = normalize(jobTitle + " " + jobDescription);
        String normalizedResume = normalize(resumeContent);

        LinkedHashSet<String> candidateSkills = new LinkedHashSet<>();
        for (String skill : SKILL_CATALOG) {
            if (combinedJobText.contains(skill)) {
                candidateSkills.add(skill);
            }
        }

        if (candidateSkills.isEmpty()) {
            candidateSkills.addAll(extractTerms(combinedJobText));
        }

        List<String> missingSkills = candidateSkills.stream()
                .filter(skill -> !normalizedResume.contains(skill))
                .toList();

        int matchScore = scoreMatches(candidateSkills, missingSkills, normalizedResume, combinedJobText);
        return new JobAnalysis(missingSkills, matchScore);
    }

    private int scoreMatches(Set<String> candidateSkills, List<String> missingSkills, String resumeText, String jobText) {
        if (candidateSkills.isEmpty()) {
            return clamp(heuristicScore(resumeText, jobText), 0, 100);
        }

        int matchedSkills = candidateSkills.size() - missingSkills.size();
        int score = (int) Math.round((double) matchedSkills / candidateSkills.size() * 100);
        return clamp(score, 0, 100);
    }

    private int heuristicScore(String resumeText, String jobText) {
        Set<String> resumeTerms = new LinkedHashSet<>(extractTerms(resumeText));
        Set<String> jobTerms = new LinkedHashSet<>(extractTerms(jobText));
        if (jobTerms.isEmpty()) {
            return 50;
        }
        resumeTerms.retainAll(jobTerms);
        return (int) Math.round((double) resumeTerms.size() / jobTerms.size() * 100);
    }

    private List<String> extractTerms(String text) {
        return Arrays.stream(text.split("[^a-z0-9#+.-]+"))
                .map(String::trim)
                .filter(token -> token.length() > 2)
                .filter(token -> !STOP_WORDS.contains(token))
                .distinct()
                .toList();
    }

    private String buildRecommendations(String jobTitle, JobAnalysis analysis) {
        if (analysis.missingSkills().isEmpty()) {
            return "Strong match for " + jobTitle + ". Emphasize recent outcomes, quantified impact, and keywords from the posting.";
        }
        return "Tailor your resume for " + jobTitle + " by highlighting: "
                + String.join(", ", analysis.missingSkills())
                + ". Rework your summary and recent bullets to mirror the job language.";
    }

    private JobMatchResponse mapToResponse(JobMatch jobMatch) {
        return new JobMatchResponse(
                jobMatch.getMatchId(),
                jobMatch.getResumeId(),
                jobMatch.getUserId(),
                jobMatch.getJobTitle(),
                jobMatch.getJobDescription(),
                jobMatch.getMatchScore(),
                jobMatch.getMissingSkills(),
                jobMatch.getRecommendations(),
                jobMatch.getSource(),
                jobMatch.isBookmarked(),
                jobMatch.getMatchedAt()
        );
    }

    private String normalize(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    private record JobAnalysis(List<String> missingSkills, int matchScore) {
    }
}