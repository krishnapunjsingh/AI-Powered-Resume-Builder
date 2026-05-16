package com.resumeai.jobmatch.dto;

import java.time.LocalDateTime;

public record JobMatchResponse(
        Long matchId,
        Long resumeId,
        Long userId,
        String jobTitle,
        String jobDescription,
        Integer matchScore,
        String missingSkills,
        String recommendations,
        String source,
        boolean bookmarked,
        LocalDateTime matchedAt
) {
}