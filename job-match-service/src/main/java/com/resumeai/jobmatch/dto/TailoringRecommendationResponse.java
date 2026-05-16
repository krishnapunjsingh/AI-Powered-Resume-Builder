package com.resumeai.jobmatch.dto;

import java.util.List;

public record TailoringRecommendationResponse(
        String jobTitle,
        String recommendations,
        List<String> missingSkills,
        Integer matchScore
) {
}