package com.resumeai.jobmatch.dto;

public record JobListingResponse(
        String title,
        String company,
        String location,
        String url,
        String source
) {
}