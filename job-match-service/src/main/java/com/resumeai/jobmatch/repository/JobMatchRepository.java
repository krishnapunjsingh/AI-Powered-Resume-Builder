package com.resumeai.jobmatch.repository;

import com.resumeai.jobmatch.entity.JobMatch;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobMatchRepository extends JpaRepository<JobMatch, Long> {

    Optional<JobMatch> findByMatchId(Long matchId);

    List<JobMatch> findByResumeIdOrderByMatchScoreDescMatchedAtDesc(Long resumeId);

    List<JobMatch> findByUserIdOrderByMatchScoreDescMatchedAtDesc(Long userId);

    List<JobMatch> findByUserIdAndBookmarkedTrueOrderByMatchScoreDescMatchedAtDesc(Long userId);

    List<JobMatch> findByMatchScoreGreaterThanOrderByMatchScoreDesc(Integer matchScore);

    List<JobMatch> findByJobTitleContainingIgnoreCaseAndBookmarkedTrueOrderByMatchedAtDesc(String jobTitle);

    long countByUserId(Long userId);
}