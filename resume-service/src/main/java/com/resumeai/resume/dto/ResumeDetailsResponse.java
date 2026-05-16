package com.resumeai.resume.dto;

import java.util.List;

public record ResumeDetailsResponse(
        ResumeResponse resume,
        List<SectionSummary> sections
) {
}
